package com.cnrs.opentraduction.views.search;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.client.NotePropModel;
import com.cnrs.opentraduction.models.client.PropositionModel;
import com.cnrs.opentraduction.models.client.SynonymPropModel;
import com.cnrs.opentraduction.models.client.TraductionPropModel;
import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.models.dao.PropositionDao;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.component.commandbutton.CommandButton;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
@SessionScoped
@Named(value = "propositionBean")
public class PropositionBean implements Serializable {

    private final static String FR = "fr";
    private final static String AR = "ar";

    private final MessageService messageService;
    private final ThesaurusService thesaurusService;

    private SearchBean searchBean;
    private Users userConnected;
    private PropositionDao propositionDao;
    private ConceptDao conceptToUpdate;

    private boolean termFrInitialDisable, termFrDisable;
    private boolean termArInitialDisable, termArDisable;

    private boolean synonymeFrInitialDisable, synonymeFrDisable;
    private boolean synonymeArInitialDisable, synonymeArDisable;

    private boolean definitionFrInitialDisable, definitionFrDisable;
    private boolean definitionArInitialDisable, definitionArDisable;

    private boolean noteFrInitialDisable, noteFrDisable;
    private boolean noteArInitialDisable, noteArDisable;

    private String langFrom;


    public void initInterface(Users userConnected, ConceptDao conceptToUpdate, String langFrom) {

        log.info("Initialisation de l'interface proposition");
        this.userConnected = userConnected;
        this.conceptToUpdate = conceptToUpdate;
        this.langFrom = langFrom;

        termFrInitialDisable = true;
        termFrDisable = false;

        termArInitialDisable = true;
        termArDisable = false;

        definitionFrInitialDisable = true;
        definitionFrDisable = false;

        definitionArInitialDisable = true;
        definitionArDisable = false;

        noteFrInitialDisable = true;
        noteFrDisable = false;

        noteArInitialDisable = true;
        noteArDisable = false;

        synonymeFrInitialDisable = true;
        synonymeFrDisable = false;

        synonymeArInitialDisable = true;
        synonymeArDisable = false;

        propositionDao = PropositionDao.builder()
                .collectionId("")
                .termeFr(conceptToUpdate.getLabelFr())
                .termeAr(conceptToUpdate.getLabelAr())
                .varianteFr(conceptToUpdate.getVarianteFr())
                .varianteAr(conceptToUpdate.getVarianteAr())
                .definitionFr(conceptToUpdate.getDefinitionFr())
                .definitionAr(conceptToUpdate.getDefinitionAr())
                .noteFr(conceptToUpdate.getNoteFr())
                .noteAr(conceptToUpdate.getNoteAr())
                .comment("")
                .build();
    }

    public void saveProposition() {
        log.info("Début de l'enregistrement de la proposition");

        if (CollectionUtils.isEmpty(getDefinitions())
                && CollectionUtils.isEmpty(getNotes())
                && CollectionUtils.isEmpty(getSynonymes())
                && CollectionUtils.isEmpty(getTraductions())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.proposition.msg2");
            return;
        }

        var proposition = PropositionModel.builder()
                .conceptID(conceptToUpdate.getConceptId())
                .IdTheso(conceptToUpdate.getThesaurusId())
                .commentaire(propositionDao.getComment())
                .traductionsProp(getTraductions())
                .synonymsProp(getSynonymes())
                .notes(getNotes())
                .definitions(getDefinitions())
                .build();

        log.info("Envoie de la nouvelle proposition au serveur opentheso");
        if (thesaurusService.saveProposition(proposition,
                userConnected.getGroup().getReferenceInstances().getUrl(),
                userConnected.getApiKey())) {

            log.info("Affichage de message");
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.proposition.msg1");

            triggerCancelButton();
        }

        log.info("Fin de l'enregistrement de la proposition");
    }

    public void triggerCancelButton() {
        var component = findComponent(FacesContext.getCurrentInstance().getViewRoot(), "annulerPropositionBtn");
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

    public String getConceptLabel() {
        return "FR".equalsIgnoreCase(langFrom) ? conceptToUpdate.getLabelFr() : conceptToUpdate.getLabelAr();
    }

    private List<TraductionPropModel> getTraductions() {
        log.info("Construction de la liste des traductions");
        var traductions = new ArrayList<TraductionPropModel>();
        if (!termFrInitialDisable) {
            traductions.add(TraductionPropModel.builder()
                    .lexicalValue(propositionDao.getTermeFr())
                    .lang(FR)
                    .oldValue(conceptToUpdate.getLabelFr())
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getLabelFr()) && !StringUtils.isEmpty(propositionDao.getTermeFr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getLabelFr())
                            && !StringUtils.isEmpty(propositionDao.getTermeFr())
                            && !propositionDao.getTermeFr().equals(conceptToUpdate.getLabelFr()))
                    .toRemove(termFrDisable)
                    .build());
        }

        if (!termArInitialDisable) {
            traductions.add(TraductionPropModel.builder()
                    .lexicalValue(propositionDao.getTermeAr())
                    .lang(AR)
                    .oldValue(conceptToUpdate.getLabelAr())
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getLabelAr()) && !StringUtils.isEmpty(propositionDao.getTermeAr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getLabelAr())
                            && !StringUtils.isEmpty(propositionDao.getTermeAr())
                            && !propositionDao.getTermeAr().equals(conceptToUpdate.getLabelAr()))
                    .toRemove(termArDisable)
                    .build());
        }
        return traductions;
    }

    private List<SynonymPropModel> getSynonymes() {
        log.info("Construction de la liste des synonymes");
        var synonymes = new ArrayList<SynonymPropModel>();
        if (!synonymeFrInitialDisable) {
            synonymes.add(SynonymPropModel.builder()
                    .lexical_value(propositionDao.getVarianteFr())
                    .hiden(false)
                    .lang(FR)
                    .oldValue(conceptToUpdate.getVarianteFr())
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getVarianteFr()) && !StringUtils.isEmpty(propositionDao.getVarianteFr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getVarianteFr())
                            && !StringUtils.isEmpty(propositionDao.getVarianteFr())
                            && !propositionDao.getVarianteFr().equals(conceptToUpdate.getVarianteFr()))
                    .toRemove(synonymeFrDisable)
                    .build());
        }

        if (!synonymeArInitialDisable) {
            synonymes.add(SynonymPropModel.builder()
                    .lexical_value(propositionDao.getVarianteAr())
                    .hiden(false)
                    .lang(AR)
                    .oldValue(conceptToUpdate.getVarianteAr())
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getVarianteAr()) && !StringUtils.isEmpty(propositionDao.getVarianteAr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getVarianteAr())
                            && !StringUtils.isEmpty(propositionDao.getVarianteAr())
                            && !propositionDao.getVarianteFr().equals(conceptToUpdate.getVarianteAr()))
                    .toRemove(synonymeArDisable)
                    .build());
        }
        return synonymes;
    }

    private List<NotePropModel> getDefinitions() {
        log.info("Construction de la liste des définitions");
        var definitions = new ArrayList<NotePropModel>();
        if (!definitionFrInitialDisable) {
            definitions.add(NotePropModel.builder()
                    .lexicalvalue(propositionDao.getDefinitionFr())
                    .oldValue(conceptToUpdate.getDefinitionFr())
                    .lang(FR)
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getDefinitionFr()) && !StringUtils.isEmpty(propositionDao.getDefinitionFr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getDefinitionFr())
                            && !StringUtils.isEmpty(propositionDao.getDefinitionFr())
                            && !propositionDao.getDefinitionFr().equals(conceptToUpdate.getDefinitionFr()))
                    .toRemove(definitionFrDisable)
                    .build());
        }

        if (!definitionArInitialDisable) {
            definitions.add(NotePropModel.builder()
                    .lexicalvalue(propositionDao.getDefinitionAr())
                    .oldValue(conceptToUpdate.getDefinitionAr())
                    .lang(AR)
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getDefinitionAr()) && !StringUtils.isEmpty(propositionDao.getDefinitionAr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getDefinitionAr())
                            && !StringUtils.isEmpty(propositionDao.getDefinitionAr())
                            && !propositionDao.getDefinitionAr().equals(conceptToUpdate.getDefinitionAr()))
                    .toRemove(definitionArDisable)
                    .build());
        }
        return definitions;
    }

    private List<NotePropModel> getNotes() {
        log.info("Construction de la liste des notes");
        var notes = new ArrayList<NotePropModel>();
        if (!noteFrInitialDisable) {
            notes.add(NotePropModel.builder()
                    .lexicalvalue(propositionDao.getNoteFr())
                    .oldValue(conceptToUpdate.getNoteFr())
                    .lang(FR)
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getNoteFr()) && !StringUtils.isEmpty(propositionDao.getNoteFr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getNoteFr())
                            && !StringUtils.isEmpty(propositionDao.getNoteFr())
                            && !propositionDao.getNoteFr().equals(conceptToUpdate.getNoteFr()))
                    .toRemove(noteFrDisable)
                    .build());
        }

        if (!noteArInitialDisable) {
            notes.add(NotePropModel.builder()
                    .lexicalvalue(propositionDao.getNoteAr())
                    .oldValue(conceptToUpdate.getNoteAr())
                    .lang(AR)
                    .toAdd(StringUtils.isEmpty(conceptToUpdate.getNoteAr()) && !StringUtils.isEmpty(propositionDao.getNoteAr()))
                    .toUpdate(!StringUtils.isEmpty(conceptToUpdate.getNoteAr())
                            && !StringUtils.isEmpty(propositionDao.getNoteAr())
                            && !propositionDao.getNoteAr().equals(conceptToUpdate.getNoteAr()))
                    .toRemove(noteArDisable)
                    .build());
        }
        return notes;
    }

    /********** Term FR **********/
    public void onTermFrChange(AjaxBehaviorEvent event) {
        termFrInitialDisable = !ObjectUtils.isEmpty(propositionDao.getTermeFr()) && propositionDao.getTermeFr().equals(conceptToUpdate.getLabelFr());
    }

    public void initialTermFr() {
        propositionDao.setTermeFr(conceptToUpdate.getLabelFr());
        termFrInitialDisable = true;
        termFrDisable = false;
    }

    public void deleteTermFr() {
        propositionDao.setTermeFr(conceptToUpdate.getLabelFr());
        termFrInitialDisable = false;
        termFrDisable = true;
    }

    public String getTermFrStyleClass() {
        return termFrInitialDisable ? "" : "bold-text";
    }

    /********** Term AR **********/
    public void onTermArChange(AjaxBehaviorEvent event) {
        termArInitialDisable = !ObjectUtils.isEmpty(propositionDao.getTermeAr()) && propositionDao.getTermeAr().equals(conceptToUpdate.getLabelAr());
    }

    public void initialTermAr() {
        propositionDao.setTermeAr(conceptToUpdate.getLabelAr());
        termArInitialDisable = true;
        termArDisable = false;
    }

    public void deleteTermAr() {
        propositionDao.setTermeAr(conceptToUpdate.getLabelAr());
        termArInitialDisable = false;
        termArDisable = true;
    }

    public String getTermArStyleClass() {
        return termArInitialDisable ? "" : "bold-text";
    }

    /********** Definition FR **********/
    public void onDefinitionFrChange(AjaxBehaviorEvent event) {
        definitionFrInitialDisable = !ObjectUtils.isEmpty(propositionDao.getDefinitionFr())
                && propositionDao.getDefinitionFr().equals(conceptToUpdate.getDefinitionFr());
    }

    public void initialDefinitionFr() {
        propositionDao.setDefinitionFr(conceptToUpdate.getDefinitionFr());
        definitionFrInitialDisable = true;
        definitionFrDisable = false;
    }

    public void deleteDefinitionFr() {
        propositionDao.setDefinitionFr(conceptToUpdate.getDefinitionFr());
        definitionFrInitialDisable = false;
        definitionFrDisable = true;
    }

    public String getDefinitionFrStyleClass() {
        return definitionFrInitialDisable ? "" : "bold-text";
    }

    /********** Definition AR **********/
    public void onDefinitionArChange(AjaxBehaviorEvent event) {
        definitionArInitialDisable = !ObjectUtils.isEmpty(propositionDao.getDefinitionAr())
                && propositionDao.getDefinitionAr().equals(conceptToUpdate.getDefinitionAr());
    }

    public void initialDefinitionAr() {
        propositionDao.setDefinitionAr(conceptToUpdate.getDefinitionAr());
        definitionArInitialDisable = true;
        definitionArDisable = false;
    }

    public void deleteDefinitionAr() {
        propositionDao.setDefinitionAr(conceptToUpdate.getDefinitionAr());
        definitionArInitialDisable = false;
        definitionArDisable = true;
    }

    public String getDefinitionArStyleClass() {
        return definitionArInitialDisable ? "" : "bold-text";
    }

    /********** Note FR **********/
    public void onNoteFrChange(AjaxBehaviorEvent event) {
        noteFrInitialDisable = !ObjectUtils.isEmpty(propositionDao.getNoteFr())
                && propositionDao.getNoteFr().equals(conceptToUpdate.getNoteFr());
    }

    public void initialNoteFr() {
        propositionDao.setNoteFr(conceptToUpdate.getNoteFr());
        noteFrInitialDisable = true;
        noteFrDisable = false;
    }

    public void deleteNoteFr() {
        propositionDao.setNoteFr(conceptToUpdate.getNoteFr());
        noteFrInitialDisable = false;
        noteFrDisable = true;
    }

    public String getNoteFrStyleClass() {
        return noteFrInitialDisable ? "" : "bold-text";
    }

    /********** Note AR **********/
    public void onNoteArChange(AjaxBehaviorEvent event) {
        noteArInitialDisable = !ObjectUtils.isEmpty(propositionDao.getNoteFr())
                && propositionDao.getNoteFr().equals(conceptToUpdate.getNoteFr());
    }

    public void initialNoteAr() {
        propositionDao.setNoteAr(conceptToUpdate.getNoteAr());
        noteArInitialDisable = true;
        noteArDisable = false;
    }

    public void deleteNoteAr() {
        propositionDao.setNoteAr(conceptToUpdate.getNoteAr());
        noteArInitialDisable = false;
        noteArDisable = true;
    }

    public String getNoteArStyleClass() {
        return noteArInitialDisable ? "" : "bold-text";
    }

    /********** Synonyme FR **********/
    public void onSynonymeFrChange(AjaxBehaviorEvent event) {
        synonymeFrInitialDisable = !ObjectUtils.isEmpty(propositionDao.getVarianteFr())
                && propositionDao.getVarianteFr().equals(conceptToUpdate.getVarianteFr());
    }

    public void initialSynonymeFr() {
        propositionDao.setVarianteFr(conceptToUpdate.getVarianteFr());
        synonymeFrInitialDisable = true;
        synonymeFrDisable = false;
    }

    public void deleteSynonymeFr() {
        propositionDao.setVarianteFr(conceptToUpdate.getVarianteFr());
        synonymeFrInitialDisable = false;
        synonymeFrDisable = true;
    }

    public String getSynonymeFrStyleClass() {
        return synonymeFrInitialDisable ? "" : "bold-text";
    }

    /********** Synonyme AR **********/
    public void onSynonymeArChange(AjaxBehaviorEvent event) {
        synonymeArInitialDisable = !ObjectUtils.isEmpty(propositionDao.getVarianteAr())
                && propositionDao.getVarianteAr().equals(conceptToUpdate.getVarianteAr());
    }

    public void initialSynonymeAr() {
        propositionDao.setVarianteAr(conceptToUpdate.getVarianteAr());
        synonymeArInitialDisable = true;
        synonymeArDisable = false;
    }

    public void deleteSynonymeAr() {
        propositionDao.setVarianteAr(conceptToUpdate.getVarianteAr());
        synonymeArInitialDisable = false;
        synonymeArDisable = true;
    }

    public String getSynonymeArStyleClass() {
        return synonymeArInitialDisable ? "" : "bold-text";
    }
}
