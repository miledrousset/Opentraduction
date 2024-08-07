package com.cnrs.opentraduction.views.search;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.services.ThesaurusService;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Data
@SessionScoped
@Named(value = "searchBean")
public class SearchBean implements Serializable {

    private final static String ALL = "ALL";

    private final MessageService messageService;
    private final ThesaurusService thesaurusService;
    private final OpenthesoClient openthesoClient;
    private final CandidatBean candidatBean;
    private final PropositionBean propositionBean;
    private final DeeplBean deeplBean;
    private final UserService userService;

    private Users userConnected;
    private String termValue, idReferenceCollectionSelected;
    private boolean toArabic, searchDone, userApiKeyAlert;
    private boolean searchResultDisplay, addCandidatDisplay, addPropositionDisplay;
    private ConceptDao conceptSelected;
    private List<ConceptDao> conceptsReferenceFoundList, conceptsConsultationFoundList;
    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;


    public void initSearchInterface(Integer userConnectedId) {
        log.info("Initialisation de l'interface recherche");
        this.userConnected = userService.getUserById(userConnectedId);

        toArabic = "Arabe".equalsIgnoreCase(this.userConnected.getDefaultTargetTraduction());
        termValue = "";
        searchDone = false;
        conceptsReferenceFoundList = new ArrayList<>();
        conceptsConsultationFoundList = new ArrayList<>();

        searchResultDisplay = true;
        addCandidatDisplay = false;
        addPropositionDisplay = false;

        log.info("Vérification de la présence de clé API utilisateur");
        userApiKeyAlert = StringUtils.isEmpty(this.userConnected.getApiKey());

        log.info("Préparation des projets de consultation");
        searchReferenceCollectionList();
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

        searchInReferenceProject();

        searchInConsultationThesaurus();

        if (CollectionUtils.isEmpty(conceptsReferenceFoundList) && CollectionUtils.isEmpty(conceptsConsultationFoundList)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg3");
            return;
        }

        searchDone = true;

        if (fromMain) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("search.xhtml");
        }
    }

    private void searchInConsultationThesaurus() {
        log.info("Vérification s'il y a des thésaurus de consultation");
        conceptsConsultationFoundList = new ArrayList<>();
        var consultationProjects = userConnected.getGroup().getConsultationInstances();
        if (!CollectionUtils.isEmpty(consultationProjects)) {
            log.info("Il existe {} projet de consultation !", consultationProjects.size());
            for (ConsultationInstances project : consultationProjects) {
                project.getThesauruses().forEach(thesaurus -> {
                    var defaultIdGroup = ALL.equalsIgnoreCase(thesaurus.getIdCollection())
                            ? null
                            : userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
                    var tmp = searchInThesaurus(thesaurus, project.getUrl(), defaultIdGroup);
                    if (!CollectionUtils.isEmpty(tmp)) {
                        conceptsConsultationFoundList.addAll(tmp.stream()
                                .filter(element -> !isAlreadyFoundInReferenceThesaurus(element.getThesaurusId(), element.getConceptId()))
                                .collect(Collectors.toList()));
                    }
                });
            }
        }
    }

    private boolean isAlreadyFoundInReferenceThesaurus(String thesaurusId, String conceptId) {
        if (CollectionUtils.isEmpty(conceptsReferenceFoundList)) {
            return false;
        }

        log.info("Rechercher des doublons dans le résultat de référence");
        var foundInReferenceThesaurus = conceptsReferenceFoundList.stream()
                .anyMatch(element -> element.getThesaurusId().equals(thesaurusId) && element.getConceptId().equals(conceptId));

        log.info("Rechercher des doublons dans le résultat de consultation");
        var foundInConsultationThesaurus = conceptsConsultationFoundList.stream()
                .anyMatch(element -> element.getThesaurusId().equals(thesaurusId) && element.getConceptId().equals(conceptId));

        return foundInReferenceThesaurus || foundInConsultationThesaurus;
    }

    private void searchInReferenceProject() {
        conceptsReferenceFoundList = new ArrayList<>();
        var referenceProject = userConnected.getGroup().getReferenceInstances();
        if (!ObjectUtils.isEmpty(referenceProject)) {
            try {
                var defaultIdGroup = ALL.equalsIgnoreCase(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())
                        ? null
                        : userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
                var tmp = searchInThesaurus(referenceProject.getThesaurus(), referenceProject.getUrl(), defaultIdGroup);
                conceptsReferenceFoundList.addAll(tmp);
            } catch (Exception ex) {
                log.info("Aucune résultat trouvé pour le thésaurus de référence {}", referenceProject.getThesaurus().getName());
            }
        } else {
            log.error("Aucun Thésaurus de référence n'est présent !: ");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.search.failed.msg2");
        }
    }

    private List<ConceptDao> searchInThesaurus(Thesaurus thesaurus, String url, String defaultIdGroup) {

        log.info("Thésaurus de référence : " + thesaurus.getName());

        var idCollection = ALL.equalsIgnoreCase(thesaurus.getIdCollection())
                ? defaultIdGroup
                : thesaurus.getIdCollection();

        var languageToSearch = toArabic ? "fr" : "ar";

        return thesaurusService.searchConcepts(thesaurus, url, termValue, languageToSearch, idCollection);
    }

    public void initAddProposition(ConceptDao conceptToUpdate) {

        this.conceptSelected = conceptToUpdate;

        searchResultDisplay = false;
        addCandidatDisplay = false;
        addPropositionDisplay = true;

        var langFrom = toArabic ? "FR" : "AR";
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
        return ALL.equals(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())
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

    private void searchReferenceCollectionList() {

        referenceCollectionList = new ArrayList<>();
        referenceCollectionList.add(new CollectionElementDao(ALL, "--"));
        if (!ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            if (ALL.equals(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())) {
                referenceCollectionList.addAll(thesaurusService.searchCollections(
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
}
