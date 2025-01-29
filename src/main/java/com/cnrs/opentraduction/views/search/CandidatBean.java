package com.cnrs.opentraduction.views.search;

import com.cnrs.opentraduction.clients.DeeplClient;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.client.opentheso.candidat.CandidateModel;
import com.cnrs.opentraduction.models.client.opentheso.concept.ElementModel;
import com.cnrs.opentraduction.models.dao.CandidatDao;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.models.dao.NodeIdValue;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.component.commandbutton.CommandButton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
@SessionScoped
@Named(value = "candidatBean")
public class CandidatBean implements Serializable {

    @Value("${deepl.enable}")
    private boolean deeplEnable;

    private final static String AR = "ar";
    private final static String FR = "fr";

    private final MessageService messageService;
    private final ThesaurusService thesaurusService;
    private final DeeplClient deeplService;
    private final GroupService groupService;

    private CandidatDao candidatDao;
    private Users userConnected;
    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;
    private String labelReferenceCollectionSelected;
    private boolean deeplDisponible;
    private List<NodeIdValue> conceptsList;
    private String idConceptSelected;


    public void initInterface(Users userConnected, ConceptDao conceptDaoTmp) {
        log.info("Initialisation de l'interface candidat");
        this.userConnected = userConnected;
        candidatDao = new CandidatDao();

        if (conceptDaoTmp != null) {
            candidatDao.setTitleFr(conceptDaoTmp.getLabelFr());
            candidatDao.setTitleAr(conceptDaoTmp.getLabelAr());

            candidatDao.setDefinitionAr(conceptDaoTmp.getDefinitionAr());
            candidatDao.setDefinitionFr(conceptDaoTmp.getDefinitionFr());

            candidatDao.setNoteAr(conceptDaoTmp.getNoteAr());
            candidatDao.setNoteFr(conceptDaoTmp.getNoteFr());

            candidatDao.setVarianteAr(conceptDaoTmp.getNoteAr());
            candidatDao.setVarianteFr(conceptDaoTmp.getNoteFr());
        }

        //Disable Deepl Translate function
        deeplDisponible = false;

        var url = this.userConnected.getGroup().getReferenceInstances().getUrl();
        var idThesaurus = this.userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus();

        referenceCollectionList = new ArrayList<>();
        referenceCollectionList.add(new CollectionElementDao("ALL", messageService.getMessage("user.settings.consultation.racine")));
        if ("ALL".equals(this.userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())) {
            referenceCollectionList.addAll(thesaurusService.searchCollections(url, idThesaurus));
        } else {
            var idCollection = this.userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
            referenceCollectionList.addAll(thesaurusService.searchSubCollections(url, idThesaurus, idCollection));
        }
        collectionReferenceSelected = referenceCollectionList.get(0);

        searchConceptByGroup();
        if (!CollectionUtils.isEmpty(conceptsList)) {
            idConceptSelected = conceptsList.get(0).getIdConcept();
        }
    }

    public void searchConceptByGroup() {
        String idGroup;
        if ("ALL".equals(labelReferenceCollectionSelected)) {
            idGroup = userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
        } else {
            idGroup = collectionReferenceSelected.getId();
        }
        conceptsList = groupService.searchConceptByGroup(userConnected, idGroup);
        if (CollectionUtils.isEmpty(conceptsList)) {
            conceptsList = List.of(NodeIdValue.builder().prefLabel("Aucun concept n'est présent").build());
        }
    }

    public void saveCandidat() {
        log.info("Début de l'enregistrement du candidat");

        if (StringUtils.isEmpty(candidatDao.getTitleFr()) && StringUtils.isEmpty(candidatDao.getTitleAr())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.candidat.error.msg1");
            return;
        }

        candidatDao.setThesoId(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus());

        List<ElementModel> termes = new ArrayList<>();
        if (!StringUtils.isEmpty(candidatDao.getTitleFr())) {
            termes.add(ElementModel.builder().value(candidatDao.getTitleFr()).lang("fr").build());
        }
        if (!StringUtils.isEmpty(candidatDao.getTitleAr())) {
            termes.add(ElementModel.builder().value(candidatDao.getTitleAr()).lang("ar").build());
        }

        List<ElementModel> definitions = new ArrayList<>();
        if (!StringUtils.isEmpty(candidatDao.getDefinitionFr())) {
            definitions.add(ElementModel.builder().value(candidatDao.getDefinitionFr()).lang("fr").build());
        }
        if (!StringUtils.isEmpty(candidatDao.getDefinitionAr())) {
            definitions.add(ElementModel.builder().value(candidatDao.getDefinitionAr()).lang("ar").build());
        }

        List<ElementModel> notes = new ArrayList<>();
        if (!StringUtils.isEmpty(candidatDao.getNoteFr())) {
            notes.add(ElementModel.builder().value(candidatDao.getNoteFr()).lang("fr").build());
        }
        if (!StringUtils.isEmpty(candidatDao.getNoteAr())) {
            notes.add(ElementModel.builder().value(candidatDao.getNoteAr()).lang("ar").build());
        }

        List<ElementModel> synonymes = new ArrayList<>();
        if (!StringUtils.isEmpty(candidatDao.getVarianteFr())) {
            synonymes.add(ElementModel.builder().value(candidatDao.getVarianteFr()).lang("fr").build());
        }
        if (!StringUtils.isEmpty(candidatDao.getVarianteFr())) {
            synonymes.add(ElementModel.builder().value(candidatDao.getVarianteAr()).lang("ar").build());
        }

        var candidate = CandidateModel.builder()
                .thesoId(candidatDao.getThesoId())
                .conceptGenericId(idConceptSelected)
                .terme(termes)
                .definition(definitions)
                .note(notes)
                .collectionId(getSelectedCollectionId())
                .synonymes(synonymes)
                .comment(candidatDao.getComment())
                .build();

        log.info("Enregistrer le nouveau candidat");

        if (thesaurusService.saveCandidat(candidate,
                userConnected.getGroup().getReferenceInstances().getUrl(),
                userConnected.getApiKey())) {

            triggerCancelButton();

            log.info("Enregistrement du candidat terminé");
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.candidat.success.msg1");
        }
    }

    private String getSelectedCollectionId() {
        if ("ALL".equalsIgnoreCase(collectionReferenceSelected.getId())) {
            return "ALL".equals(this.userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())
                    ? "" : this.userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
        } else {
            return collectionReferenceSelected.getId();
        }
    }

    public void setSelectedReferenceCollection() {
        if (!StringUtils.isEmpty(labelReferenceCollectionSelected)) {
            var collectionReferenceTmp = referenceCollectionList.stream()
                    .filter(element -> element.getLabel().equals(labelReferenceCollectionSelected))
                    .findFirst();
            collectionReferenceTmp.ifPresent(collectionElementDao -> collectionReferenceSelected = collectionElementDao);
            searchConceptByGroup();
        }
    }

    public void deeplTranslate(String value, String valueName) {

        if (deeplEnable) {
            if (StringUtils.isEmpty(value)) {
                messageService.showMessage(FacesMessage.SEVERITY_WARN, "application.candidat.error.emty.transition.value");
                return;
            }

            switch (valueName) {
                case "termFr":
                    candidatDao.setTitleAr(deeplService.translate(value, "fr", "ar"));
                    break;
                case "termAr":
                    candidatDao.setTitleFr(deeplService.translate(value, "ar", "fr"));
                    break;
                case "varianteFr":
                    candidatDao.setVarianteAr(deeplService.translate(value, "fr", "ar"));
                    break;
                case "varianteAr":
                    candidatDao.setVarianteFr(deeplService.translate(value, "ar", "fr"));
                    break;
                case "definitionFr":
                    candidatDao.setDefinitionAr(deeplService.translate(value, "fr", "ar"));
                    break;
                case "definitionAr":
                    candidatDao.setDefinitionFr(deeplService.translate(value, "ar", "fr"));
                    break;
                case "noteFr":
                    candidatDao.setNoteAr(deeplService.translate(value, "fr", "ar"));
                    break;
                case "noteAr":
                    candidatDao.setNoteFr(deeplService.translate(value, "ar", "fr"));
                    break;
            }
            log.info("Traduction finish");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_WARN, "application.candidat.deepl.disable");
        }
    }

    public void triggerCancelButton() {
        var component = findComponent(FacesContext.getCurrentInstance().getViewRoot(), "annulerCandidatBtn");
        if (!ObjectUtils.isEmpty(component) && component instanceof CommandButton) {
            var cancelButton = (CommandButton) component;
            cancelButton.queueEvent(new ActionEvent(cancelButton));
        }
    }

    private UIComponent findComponent(UIComponent base, String id) {
        if (id.equals(base.getId())) {
            return base;
        }

        for (int i = 0; i < base.getChildCount(); i++) {
            var kid = (UIComponent) base.getChildren().get(i);
            if (id.equals(kid.getId())) {
                return kid;
            }
            var result = findComponent(kid, id);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
