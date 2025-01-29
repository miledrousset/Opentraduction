package com.cnrs.opentraduction.views.search;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.services.SearchService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
@SessionScoped
@Named(value = "searchBean")
public class SearchBean implements Serializable {

    private final MessageService messageService;
    private final CandidatBean candidatBean;
    private final PropositionBean propositionBean;
    private final DeeplBean deeplBean;
    private final UserService userService;
    private final SearchService searchService;

    private Users userConnected;
    private ConceptDao conceptSelected;
    private String termValue, idReferenceCollectionSelected, selectedExternSource;
    private boolean toArabic, searchDone, userApiKeyAlert, showExternSources;
    private boolean searchResultDisplay, addCandidatDisplay, addPropositionDisplay, searchInOpenTheso, searchInSpecificSource, searchInAllSources;
    private List<ConceptDao> conceptsReferenceFoundList, conceptsConsultationFoundList, resultGeoNameSearch, resultWikiDataSearch, rdRefSearch;
    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;

    private boolean wikiDataSelected, geoNamesSelected, idRefSelected;


    public void initSearchInterface(Integer userConnectedId) {
        log.info("Initialisation de l'interface recherche");
        this.userConnected = userService.getUserById(userConnectedId);

        toArabic = "Arabe".equalsIgnoreCase(this.userConnected.getDefaultTargetTraduction());
        termValue = "";
        searchDone = false;
        conceptsReferenceFoundList = null;
        conceptsConsultationFoundList = null;

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;

        log.info("Vérification de la présence de clé API utilisateur");
        userApiKeyAlert = StringUtils.isEmpty(this.userConnected.getApiKey());

        log.info("Préparation des projets de consultation");
        referenceCollectionList = searchService.searchReferenceCollectionList(userConnected);

        termValue = "";
        selectedExternSource = "";
        searchInOpenTheso = false;
        searchInAllSources = false;
        searchInSpecificSource = false;
        showExternSources = false;
    }

    public String getReferenceThesaurusName() {
        return userConnected.getGroup().getReferenceInstances().getThesaurus().getName();
    }

    public String getReferenceCollectionName() {
        return userConnected.getGroup().getReferenceInstances().getThesaurus().getCollection();
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
            referenceCollectionList = searchService.searchReferenceCollectionList(userConnected);
        }

        searchInOpenTheso = true;
        searchInSpecificSource = false;
        searchInAllSources = false;

        conceptsReferenceFoundList = searchService.searchInReferenceProject(userConnected, termValue, toArabic);
        conceptsConsultationFoundList = searchService.searchInConsultationThesaurus(userConnected, conceptsReferenceFoundList, termValue, toArabic);

        showExternSources = CollectionUtils.isEmpty(conceptsReferenceFoundList);

        if (fromMain) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("search.xhtml");
        }
        searchDone = true;
    }

    public void searchInAllExternSources() throws IOException, ParserConfigurationException, InterruptedException, SAXException {

        var languageToSearch = toArabic ? "fr" : "ar";

        geoNamesSelected = true;
        wikiDataSelected = true;
        idRefSelected = true;

        selectedExternSource = "";

        resultGeoNameSearch = searchService.geoNamesSearch(termValue, languageToSearch);
        resultWikiDataSearch = searchService.wikiDataSearch(termValue, languageToSearch);
        rdRefSearch = searchService.idRefSearch(termValue, languageToSearch);

        searchInOpenTheso = false;
        searchInSpecificSource = false;
        searchInAllSources = true;
    }

    public void searchInSpecificSource() throws IOException, ParserConfigurationException, InterruptedException, SAXException {
        searchInOpenTheso = false;
        searchInAllSources = false;
        searchInSpecificSource = true;

        geoNamesSelected = false;
        wikiDataSelected = false;
        idRefSelected = false;
        var languageToSearch = toArabic ? "fr" : "ar";

        switch(selectedExternSource) {
            case "GeoNames":
                geoNamesSelected = true;
                resultGeoNameSearch = searchService.geoNamesSearch(termValue, languageToSearch);
                break;
            case "WikiData":
                wikiDataSelected = true;
                resultWikiDataSearch = searchService.wikiDataSearch(termValue, languageToSearch);
                break;
            case "IdRef":
                idRefSelected = true;
                rdRefSearch = searchService.idRefSearch(termValue, languageToSearch);
                break;
        }
    }

    public void initAddProposition(ConceptDao conceptToUpdate) {

        this.conceptSelected = conceptToUpdate;

        searchResultDisplay = false;
        addCandidatDisplay = false;
        addPropositionDisplay = true;

        var langFrom = toArabic ? "FR" : "AR";
        propositionBean.initInterface(userConnected, conceptToUpdate, langFrom);
    }

    public void initAddCandidat(ConceptDao conceptDao) {

        searchResultDisplay = false;
        addCandidatDisplay = true;
        addPropositionDisplay = false;
        candidatBean.initInterface(userConnected, conceptDao);
    }

    public void backToSearchScrean() {

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;
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
        return toArabic ? " (FR)" : " (AR)";
    }

    private String getLangCible() {
        return toArabic ? " (AR)" : " (FR)";
    }

    public boolean isConcept(String status) {
        return !"CA".equals(status);
    }

    public boolean canSendProposition(String status) {
        return isConcept(status) && !StringUtils.isEmpty(userConnected.getApiKey());
    }

    public boolean canSendCandidat() {
        return !StringUtils.isEmpty(userConnected.getApiKey());
    }

    public void setTraductionDirection(AjaxBehaviorEvent event) {
        UIComponent component = event.getComponent();
        if (component instanceof UIInput) {
            UIInput inputComponent = (UIInput) component;
            toArabic = (Boolean) inputComponent.getValue();
        }
    }

    public boolean showReferenceResult() {
        return CollectionUtils.isEmpty(conceptsReferenceFoundList);
    }

    public boolean showConsultationResult() {
        return CollectionUtils.isEmpty(conceptsConsultationFoundList);
    }

    public boolean searchBtnEnabled() {
        return CollectionUtils.isEmpty(userConnected.getGroup().getConsultationInstances())
                && ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances());
    }

    public boolean showGeoNamesResult() {
        return CollectionUtils.isEmpty(resultGeoNameSearch);
    }

    public boolean showWikiDataResult() {
        return CollectionUtils.isEmpty(resultWikiDataSearch);
    }

    public boolean showRdRefResult() {
        return CollectionUtils.isEmpty(rdRefSearch);
    }

    public boolean showExternSources() {
        return searchInOpenTheso && showReferenceResult() && showConsultationResult();
    }
}
