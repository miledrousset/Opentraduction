package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.client.ConceptModel;
import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.utils.MessageService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Data
@SessionScoped
@Named(value = "searchBean")
public class SearchBean implements Serializable {

    private final MessageService messageService;
    private final OpenthesoClient openthesoClient;

    private Users userConnected;
    private String termValue;
    private boolean toArabic;
    private ConceptDao conceptSelected;
    private List<ConceptDao> conceptsReferenceFoundList;

    private boolean searchResultDisplay, addCandidatDisplay, addPropositionDisplay;


    public void initSearchInterface(Users userConnected) {
        log.info("Initialisation de l'interface recherche");
        termValue = "";
        toArabic = false;
        this.userConnected = userConnected;
        conceptsReferenceFoundList = new ArrayList<>();

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;
    }

    public void searchTerm(boolean fromMain) throws IOException {

        if (StringUtils.isEmpty(termValue)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg1");
            return;
        }

        conceptsReferenceFoundList = new ArrayList<>();

        var languageToSearch = toArabic ? "ar" : "fr";

        var projectReference = userConnected.getThesauruses().stream()
                .filter(element -> !ObjectUtils.isEmpty(element.getReferenceInstances()))
                .findAny();
        if (projectReference.isPresent()) {
            var thesaurusReference = projectReference.get().getReferenceInstances().getThesaurus();

            log.info("Thésaurus de référence : " + thesaurusReference.getName());

            var idCollection = "ALL".equalsIgnoreCase(thesaurusReference.getIdCollection())
                    ? userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection()
                    : thesaurusReference.getIdCollection();

            var referenceResult = openthesoClient.searchTerm(
                    thesaurusReference.getReferenceInstances().getUrl(),
                    thesaurusReference.getIdThesaurus(),
                    termValue,
                    languageToSearch,
                    idCollection);

            log.info("Résultat trouvée dans le Thésaurus de référence : " + referenceResult.length);
            conceptsReferenceFoundList.addAll(Arrays.asList(referenceResult).stream()
                    .map(element -> toConceptDao(element, thesaurusReference.getName()))
                    .collect(Collectors.toList()));
        } else {
            log.error("Aucun Thésaurus de référence n'est présent !: ");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg2");
            return;
        }

        if (fromMain) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("search.xhtml");
        }
    }

    public void initAddProposition(ConceptDao conceptToUpdate) {

        this.conceptSelected = conceptToUpdate;
        searchResultDisplay = false;
        addCandidatDisplay = false;
        addPropositionDisplay = true;
    }

    public void initAddCandidat() {

        searchResultDisplay = false;
        addCandidatDisplay = true;
        addPropositionDisplay = false;
    }

    public void backToSearchScrean() {

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;
    }

    private ConceptDao toConceptDao(ConceptModel conceptModel, String thesaurusName) {
        String idThesaurus = "", idConcept = "";
        try {
            var url = new URL(conceptModel.getUri());
            idThesaurus = extractDataFromUri(url.getQuery(), "idt=([^&]+)");
            idConcept = extractDataFromUri(url.getQuery(), "idc=([^&]+)");
        } catch (Exception ex) {

        }

        String labelAr = "", definitionAr = "";
        String labelFr = "", definitionFr = "";
        if (toArabic) {
            labelAr = conceptModel.getLabel();
            definitionAr = conceptModel.getDefinition();
        } else {
            labelFr = conceptModel.getLabel();
            definitionFr = conceptModel.getDefinition();
        }

        return ConceptDao.builder()
                .uri(conceptModel.getUri())
                .conceptId(idConcept)
                .thesaurusId(idThesaurus)
                .thesaurusName(thesaurusName)
                .labelAr(labelAr)
                .definitionAr(definitionAr)
                .labelFr(labelFr)
                .definitionFr(definitionFr)
                .build();
    }

    private String extractDataFromUri(String query, String patternString) {
        var pattern = Pattern.compile(patternString);
        var matcher = pattern.matcher(query);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }
}
