package com.cnrs.opentraduction.views.search;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.client.CandidateModel;
import com.cnrs.opentraduction.models.client.ElementModel;
import com.cnrs.opentraduction.models.dao.CandidatDao;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
@SessionScoped
@Named(value = "candidatBean")
public class CandidatBean implements Serializable {

    private final static String AR = "ar";
    private final static String FR = "fr";

    private final MessageService messageService;
    private final ThesaurusService thesaurusService;

    private CandidatDao candidatDao;
    private Users userConnected;
    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;
    private String labelReferenceCollectionSelected;


    public void initInterface(Users userConnected) {
        log.info("Initialisation de l'interface candidat");
        this.userConnected = userConnected;
        candidatDao = new CandidatDao();

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
                .terme(termes)
                .definition(definitions)
                .note(notes)
                .collectionId(collectionReferenceSelected.getId())
                .synonymes(synonymes)
                .comment(candidatDao.getComment())
                .build();

        log.info("Enregistrer le nouveau candidat");

        thesaurusService.saveCandidat(candidate,
                userConnected.getGroup().getReferenceInstances().getUrl(),
                userConnected.getApiKey());

        log.info("Enregistrement du candidat terminé");
        messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.candidat.success.msg1");
    }

    public void setSelectedReferenceCollection() {
        if (!StringUtils.isEmpty(labelReferenceCollectionSelected)) {
            var collectionReferenceTmp = referenceCollectionList.stream()
                    .filter(element -> element.getLabel().equals(labelReferenceCollectionSelected))
                    .findFirst();
            collectionReferenceTmp.ifPresent(collectionElementDao -> collectionReferenceSelected = collectionElementDao);
        }
    }

}
