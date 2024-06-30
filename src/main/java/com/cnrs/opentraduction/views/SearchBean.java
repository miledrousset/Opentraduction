package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.client.ConceptModel;
import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.utils.MessageService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
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
    private List<ConceptDao> conceptsReferenceFoundList, conceptsConsultationFoundList;

    private boolean searchResultDisplay, addCandidatDisplay, addPropositionDisplay;


    public void initSearchInterface(Users userConnected) {
        log.info("Initialisation de l'interface recherche");
        termValue = "";
        toArabic = false;
        this.userConnected = userConnected;
        conceptsReferenceFoundList = new ArrayList<>();
        conceptsConsultationFoundList = new ArrayList<>();

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;
    }

    public void searchTerm(boolean fromMain) throws IOException {

        if (StringUtils.isEmpty(termValue)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg1");
            return;
        }

        referenceProjectPart();

        conceptProjectsPart();

        if (CollectionUtils.isEmpty(conceptsReferenceFoundList) && CollectionUtils.isEmpty(conceptsConsultationFoundList)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg3");
            return;
        }

        if (fromMain) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("search.xhtml");
        }
    }

    private void conceptProjectsPart() {
        log.info("Vérification s'il y a des thésaurus de consultation");
        conceptsConsultationFoundList = new ArrayList<>();
        var consultationProjects = userConnected.getThesauruses().stream()
                .map(Thesaurus::getConsultationInstances)
                .filter(consultationInstances -> !ObjectUtils.isEmpty(consultationInstances))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(consultationProjects)) {
            log.info("Il existe {} projet de consultation !", consultationProjects.size());
            for (ConsultationInstances project : consultationProjects) {
                project.getThesauruses().forEach(thesaurus -> conceptsReferenceFoundList.addAll(searchInThesaurus(thesaurus, project.getUrl())));
            }
        }
    }

    private void referenceProjectPart() {
        conceptsReferenceFoundList = new ArrayList<>();

        var referenceProject = userConnected.getThesauruses().stream()
                .filter(element -> !ObjectUtils.isEmpty(element.getReferenceInstances()))
                .findAny();
        if (referenceProject.isPresent() && !ObjectUtils.isEmpty(referenceProject.get().getReferenceInstances())) {
            conceptsReferenceFoundList.addAll(searchInThesaurus(referenceProject.get().getReferenceInstances().getThesaurus(),
                    referenceProject.get().getReferenceInstances().getUrl()));
        } else {
            log.error("Aucun Thésaurus de référence n'est présent !: ");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg2");
        }
    }

    private List<ConceptDao> searchInThesaurus(Thesaurus thesaurus, String url) {

        log.info("Thésaurus de référence : " + thesaurus.getName());

        var idCollection = "ALL".equalsIgnoreCase(thesaurus.getIdCollection())
                ? userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection()
                : thesaurus.getIdCollection();

        var languageToSearch = toArabic ? "ar" : "fr";

        var referenceResult = openthesoClient.searchTerm(
                url,
                thesaurus.getIdThesaurus(),
                termValue,
                languageToSearch,
                idCollection);

        log.info("Résultat trouvée dans le Thésaurus de référence : " + referenceResult.length);
        return Arrays.stream(referenceResult)
                .map(element -> toConceptDao(element, thesaurus.getName()))
                .collect(Collectors.toList());
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
            log.error("Erreur lors du formatage de l'URL");
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
