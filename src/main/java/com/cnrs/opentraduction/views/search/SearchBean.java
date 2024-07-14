package com.cnrs.opentraduction.views.search;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.client.ConceptModel;
import com.cnrs.opentraduction.models.client.ElementModel;
import com.cnrs.opentraduction.models.dao.CollectionDao;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.services.ThesaurusService;
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
    private final ThesaurusService thesaurusService;
    private final OpenthesoClient openthesoClient;
    private final CandidatBean candidatBean;
    private final PropositionBean propositionBean;
    private final DeeplBean deeplBean;

    private Users userConnected;
    private String termValue;
    private boolean toArabic, searchDone;
    private ConceptDao conceptSelected;
    private List<ConceptDao> conceptsReferenceFoundList, conceptsConsultationFoundList;

    private boolean searchResultDisplay, addCandidatDisplay, addPropositionDisplay;

    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;
    private String idReferenceCollectionSelected;

    public void initSearchInterface() {
        log.info("Initialisation de l'interface recherche");
        termValue = "";
        toArabic = false;
        searchDone = false;
        conceptsReferenceFoundList = new ArrayList<>();
        conceptsConsultationFoundList = new ArrayList<>();

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;

        log.info("Préparation des projets de consultation");
        searchReferenceCollectionList();
    }

    private void searchReferenceCollectionList() {

        referenceCollectionList = new ArrayList<>();
        referenceCollectionList.add(new CollectionElementDao("ALL", "--"));
        if (!ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            if ("ALL".equals(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())) {
                referenceCollectionList.addAll(thesaurusService.searchTopCollections(
                        userConnected.getGroup().getReferenceInstances().getUrl(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus()));
            } else {
                referenceCollectionList.addAll(thesaurusService.searchSubCollections(
                        userConnected.getGroup().getReferenceInstances().getUrl(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection()));
            }
        }
    }

    public String getReferenceThesaurus() {
        return CollectionUtils.isEmpty(conceptsReferenceFoundList) ? "" : conceptsReferenceFoundList.get(0).getThesaurusName();
    }

    public void searchTerm(boolean fromMain) throws IOException {

        if (StringUtils.isEmpty(termValue)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg1");
            return;
        }

        deeplBean.initInterface();

        if (fromMain) {
            searchDone = false;
            conceptsReferenceFoundList = new ArrayList<>();
            conceptsConsultationFoundList = new ArrayList<>();
            searchResultDisplay = true;
            addCandidatDisplay = false;
            addPropositionDisplay = false;

            log.info("Préparation des projets de consultation");
            searchReferenceCollectionList();
        }

        referenceProjectPart();

        conceptProjectsPart();

        if (CollectionUtils.isEmpty(conceptsReferenceFoundList) && CollectionUtils.isEmpty(conceptsConsultationFoundList)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg3");
            return;
        }

        searchDone = true;

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
                project.getThesauruses().forEach(thesaurus -> {
                    var tmp = searchInThesaurus(thesaurus, project.getUrl());
                    if (!CollectionUtils.isEmpty(tmp)) {
                        conceptsConsultationFoundList.addAll(tmp.stream()
                                .filter(element -> !isAlreadyFoundInReferenceThesaurus(element.getThesaurusId(), element.getConceptId()))
                                .collect(Collectors.toList()));
                    }
                });
            }
        }
    }

    private boolean isAlreadyFoundInReferenceThesaurus(String conceptId, String thesaurusId) {
        if (CollectionUtils.isEmpty(conceptsReferenceFoundList)) {
            return false;
        }

        return conceptsReferenceFoundList.stream()
                .filter(element -> element.getThesaurusId().equals(thesaurusId) && element.getConceptId().equals(conceptId))
                .findFirst()
                .isPresent();
    }

    private void referenceProjectPart() {
        conceptsReferenceFoundList = new ArrayList<>();
        var referenceProject = userConnected.getThesauruses().stream()
                .filter(element -> !ObjectUtils.isEmpty(element.getReferenceInstances()))
                .findAny();
        if (referenceProject.isPresent() && !ObjectUtils.isEmpty(referenceProject.get().getReferenceInstances())) {
            try {
                var tmp = searchInThesaurus(referenceProject.get().getReferenceInstances().getThesaurus(),
                        referenceProject.get().getReferenceInstances().getUrl());
                conceptsReferenceFoundList.addAll(tmp);
            } catch (Exception ex) {
                log.info("Aucune résultat trouvé pour le thésaurus de référence {}",
                        referenceProject.get().getReferenceInstances().getThesaurus().getName());
            }
        } else {
            log.error("Aucun Thésaurus de référence n'est présent !: ");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg2");
        }
    }

    private List<ConceptDao> searchInThesaurus(Thesaurus thesaurus, String url) {

        log.info("Thésaurus de référence : " + thesaurus.getName());

        var defaultIdGroup = "ALL".equalsIgnoreCase(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection()) ?
                null : userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
        var idCollection = "ALL".equalsIgnoreCase(thesaurus.getIdCollection())
                ? defaultIdGroup
                : thesaurus.getIdCollection();

        var languageToSearch = toArabic ? "ar" : "fr";

        var referenceResult = openthesoClient.searchTerm(url, thesaurus.getIdThesaurus(), termValue, languageToSearch, idCollection);

        log.info("Résultat trouvée dans le Thésaurus de référence : " + referenceResult.length);
        return Arrays.stream(referenceResult)
                .map(element -> toConceptDao(element, thesaurus.getName(), thesaurus.getIdThesaurus(),
                        StringUtils.isEmpty(thesaurus.getReferenceInstances())
                                ? thesaurus.getConsultationInstances().getUrl()
                                : thesaurus.getReferenceInstances().getUrl()))
                .collect(Collectors.toList());
    }

    public void initAddProposition(ConceptDao conceptToUpdate) {

        this.conceptSelected = conceptToUpdate;

        searchResultDisplay = false;
        addCandidatDisplay = false;
        addPropositionDisplay = true;

        var langFrom = toArabic ? "AR" : "FR";
        propositionBean.initInterface(userConnected, conceptToUpdate, langFrom);
    }

    public void initAddCandidat() {

        searchResultDisplay = false;
        addCandidatDisplay = true;
        addPropositionDisplay = false;
        candidatBean.initInterface(userConnected);
    }

    public void backToSearchScrean() {

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;
    }

    private ConceptDao toConceptDao(ConceptModel conceptModel, String thesaurusName, String thesaurusId, String baseUrl) {

        return ConceptDao.builder()
                .conceptId(conceptModel.getIdConcept())
                .status(conceptModel.getStatus())
                .thesaurusId(thesaurusId)
                .thesaurusName(thesaurusName)
                .collections(conceptModel.getCollections().stream()
                        .map(element -> CollectionDao.builder().id(element.getId()).name(element.getValue()).build())
                        .collect(Collectors.toList()))
                .labelFr(getConceptTerm(conceptModel, "fr"))
                .labelAr(getConceptTerm(conceptModel, "ar"))
                .definitionFr(getConceptDefinition(conceptModel, "fr"))
                .definitionAr(getConceptDefinition(conceptModel, "ar"))
                .varianteAr(getConceptSynonyme(conceptModel, "ar"))
                .varianteFr(getConceptSynonyme(conceptModel, "fr"))
                .noteFr(getConceptNote(conceptModel, "fr"))
                .noteAr(getConceptNote(conceptModel, "ar"))
                .url(String.format("%s/?idc=%s&idt=%s", baseUrl, conceptModel.getIdConcept(), thesaurusId))
                .build();
    }

    private String getConceptTerm(ConceptModel conceptModel, String lang) {
        var tmp = conceptModel.getTerms().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst();
        return tmp.isPresent() ? tmp.get() : "";
    }

    private String getConceptSynonyme(ConceptModel conceptModel, String lang) {
        var tmp = conceptModel.getSynonymes().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst();
        return tmp.isPresent() ? tmp.get() : "";
    }

    private String getConceptDefinition(ConceptModel conceptModel, String lang) {
        var tmp = conceptModel.getDefinitions().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst();
        return tmp.isPresent() ? tmp.get() : "";
    }

    private String getConceptNote(ConceptModel conceptModel, String lang) {
        var tmp = conceptModel.getNotes().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst();
        return tmp.isPresent() ? tmp.get() : "";
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

    public String getThesaurusReferenceUrl() {

        if (!ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            return getInstanceReferenceUrl() + "/?idt="
                    + userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus();
        } else {
            return "";
        }
    }

    public String getInstanceReferenceUrl() {

        if (!ObjectUtils.isEmpty(userConnected.getGroup())
                && !ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            return userConnected.getGroup().getReferenceInstances().getUrl();
        }
        return "";
    }

    public void setSelectedReferenceCollection() {
        if (!StringUtils.isEmpty(idReferenceCollectionSelected)) {
            var collectionReferenceTmp = referenceCollectionList.stream()
                    .filter(element -> element.getLabel().equals(idReferenceCollectionSelected))
                    .findFirst();
            collectionReferenceTmp.ifPresent(collectionElementDao -> collectionReferenceSelected = collectionElementDao);
        }
    }

    public String getCollectionReferenceName() {
        return "ALL".equals(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())
                ? messageService.getMessage("user.settings.consultation.racine")
                : userConnected.getGroup().getReferenceInstances().getThesaurus().getCollection();
    }

    public String getTermSourceLabel() {
        return messageService.getMessage("application.deepl.term") + getLangSource();
    }

    public String getTermCibleLabel() {
        return messageService.getMessage("application.deepl.term") + getLangCible();
    }

    public String getDefinitionSourceLabel() {
        return messageService.getMessage("application.deepl.definition") + getLangSource();
    }

    public String getDefinitionCibleLabel() {
        return messageService.getMessage("application.deepl.definition") + getLangCible();
    }

    private String getLangSource() {
        return toArabic ? " (AR)" : " (FR)";
    }

    private String getLangCible() {
        return toArabic ? " (FR)" : " (AR)";
    }

    public boolean isConcept(String status) {
        return "CO".equals(status);
    }
}
