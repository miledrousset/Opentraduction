package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.client.ThesaurusElementModel;
import com.cnrs.opentraduction.services.ConsultationService;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Data
@Slf4j
@SessionScoped
@Named(value = "consultationBean")
public class ConsultationSettingBean implements Serializable {

    private final ConsultationService consultationInstanceService;
    private final ThesaurusService thesaurusService;
    private final MessageService messageService;

    private ConsultationInstances instanceSelected;
    private List<ConsultationInstanceDao> consultationList;

    private List<ThesaurusElementModel> thesaurusList;
    private ThesaurusElementModel thesaurusSelected;
    private String thesaurusIdSelected;

    private List<CollectionElementDao> collectionList;
    private List<CollectionElementDao> selectedCollections;
    private List<String> selectedIdCollections;

    private boolean thesaurusListStatut, collectionsListStatut, validateBtnStatut;
    private String dialogTitle;


    public void initialInterface() {
        log.info("Initialisation d'interface gestion des projets de consultation");
        consultationList = consultationInstanceService.getAllConsultationInstances();

        thesaurusListStatut = false;
        collectionsListStatut = false;

        selectedCollections = new ArrayList<>();
    }

    public void initialAddInstanceDialog() {

        log.info("Initialisation de la boite de dialog pour l'ajout d'un projet de consultation");
        dialogTitle = messageService.getMessage("system.consultation.add");

        thesaurusList = List.of();
        collectionList = List.of();
        selectedCollections = List.of();
        selectedIdCollections = List.of();

        thesaurusListStatut = false;
        collectionsListStatut = false;
        validateBtnStatut = false;
        instanceSelected = new ConsultationInstances();
        PrimeFaces.current().executeScript("PF('consultationInstanceDialog').show();");
    }

    public void searchThesaurus() {
        log.info("Recherche de la liste des thésaurus");
        thesaurusList = thesaurusService.searchThesaurus(instanceSelected.getUrl());

        if (!CollectionUtils.isEmpty(thesaurusList)) {
            thesaurusListStatut = true;
            thesaurusSelected = thesaurusList.get(0);
            thesaurusIdSelected = thesaurusSelected.getId();
            searchCollections();
        }
    }

    public void searchCollections() {

        log.info("Recherche des collections");
        thesaurusSelected = thesaurusList.stream()
                .filter(element -> element.getId().equals(thesaurusIdSelected))
                .findFirst()
                .get();

        log.info("Thésaurus cible : {}", thesaurusSelected.getLabel());
        collectionList = thesaurusService.searchTopCollections(instanceSelected.getUrl(), thesaurusSelected.getId());

        validateBtnStatut = true;

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionsListStatut = true;
            selectedCollections = collectionList;
            selectedIdCollections = collectionList.stream()
                    .map(CollectionElementDao::getId)
                    .collect(Collectors.toList());
        }
    }

    public void deleteInstance(ConsultationInstanceDao consultationInstanceDao) {
        if (!ObjectUtils.isEmpty(consultationInstanceDao)) {
            log.info("Suppression du projet de consultation {}", consultationInstanceDao.getName());
            if (consultationInstanceService.deleteInstance(consultationInstanceDao.getId())) {
                consultationList = consultationInstanceService.getAllConsultationInstances();
                messageService.showMessage(FacesMessage.SEVERITY_INFO, "system.consultation.success.msg1");
            }
        } else {
            log.error("Aucun projet de consultation n'est sélectionné !");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.consultation.failed.msg1");
        }
    }

    public void initialUpdateInstanceDialog(ConsultationInstanceDao instance) {

        log.info("Initialisation de la boite de dialog pour la mise à jour du projet de consultation");
        if (!ObjectUtils.isEmpty(instance)) {

            dialogTitle = messageService.getMessage("system.consultation.update") + instance.getName();

            instanceSelected = consultationInstanceService.getInstanceById(instance.getId());

            instanceSelected.setName(instanceSelected.getName());
            instanceSelected.setUrl(instanceSelected.getUrl());

            validateBtnStatut = false;
            thesaurusListStatut = false;
            collectionsListStatut = false;

            thesaurusList = List.of();
            thesaurusSelected = null;

            collectionList = List.of();
            selectedIdCollections = List.of();
            selectedCollections = List.of();

            if (!CollectionUtils.isEmpty(instanceSelected.getThesauruses())) {
                var thesaurusSaved = instanceSelected.getThesauruses().stream().findFirst();

                thesaurusList = thesaurusService.searchThesaurus(instanceSelected.getUrl());

                if (!CollectionUtils.isEmpty(thesaurusList)) {
                    thesaurusListStatut = true;
                    var thesaurusTmp = thesaurusList.stream()
                            .filter(element -> element.getId().equals(thesaurusSaved.get().getIdThesaurus()))
                            .findFirst();
                    if (thesaurusTmp.isPresent()) {
                        thesaurusSelected = thesaurusTmp.get();

                        validateBtnStatut = true;
                        collectionList = thesaurusService.searchTopCollections(instanceSelected.getUrl(), thesaurusSelected.getId());

                        if (!CollectionUtils.isEmpty(collectionList)) {
                            var collectionsSaved = thesaurusSaved.get().getConsultationInstances().getThesauruses().stream()
                                    .map(Thesaurus::getIdCollection)
                                    .collect(Collectors.toList());

                            collectionsListStatut = true;

                            if (collectionsSaved.size() == 1 && collectionsSaved.get(0).equals("ALL")) {
                                selectedCollections = collectionList;
                                selectedIdCollections = collectionList.stream()
                                        .map(CollectionElementDao::getId)
                                        .collect(Collectors.toList());
                            } else {
                                var collectionTmp = collectionList.stream()
                                        .filter(element -> collectionsSaved.stream()
                                                .filter(collection -> element.getId().equals(collection))
                                                .findAny()
                                                .isPresent())
                                        .collect(Collectors.toList());
                                if (!CollectionUtils.isEmpty(collectionTmp)) {
                                    selectedCollections = collectionTmp;
                                    selectedIdCollections = collectionTmp.stream()
                                            .map(element -> element.getId())
                                            .collect(Collectors.toList());
                                }
                            }
                        }
                    }
                }
            }

            PrimeFaces.current().executeScript("PF('consultationInstanceDialog').show();");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.consultation.failed.msg1");
        }
    }

    public void onCollectionSelectedUpdate() {
        log.info("Mise à jour de la collection sélectionnée");
        selectedCollections = new ArrayList<>();
        if (!CollectionUtils.isEmpty(selectedIdCollections)) {
            selectedIdCollections.forEach(idCollection -> {
                var collection = collectionList.stream()
                        .filter(element -> element.getId().equals(idCollection))
                        .findFirst();
                if (collection.isPresent()) {
                    selectedCollections.add(collection.get());
                }
            });
        }
    }

    public void instanceManagement() {

        if (StringUtils.isEmpty(instanceSelected.getName())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg2");
            return;
        }

        if (consultationInstanceService.checkExistingName(instanceSelected.getName())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg4");
            return;
        }

        log.info("Recherche de la liste des thésaurus");
        List<Thesaurus> thesaurusToSave = getThesaurus();

        log.info("Enregistrement du projet de consultation");
        if (consultationInstanceService.saveConsultationInstance(instanceSelected,  thesaurusToSave)) {

            log.info("Mise à jour de la list des projets de consultation");
            consultationList = consultationInstanceService.getAllConsultationInstances();

            messageService.showMessage(FacesMessage.SEVERITY_INFO, "system.consultation.success.msg2");

            PrimeFaces.current().executeScript("PF('consultationInstanceDialog').hide();");
            log.info("Instance enregistrée avec succès !");
        }  else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.consultation.failed.msg2");
            instanceSelected = new ConsultationInstances();
            log.error("Erreur pendant l'enregistrée l'instance de référence !");
        }
    }

    private List<Thesaurus> getThesaurus() {
        if (selectedCollections.size() == collectionList.size()) {
            return List.of(Thesaurus.builder()
                    .consultationInstances(instanceSelected)
                    .name(thesaurusSelected.getLabel())
                    .idThesaurus(thesaurusSelected.getId())
                    .collection("Toutes les collection")
                    .idCollection("ALL")
                    .created(LocalDateTime.now())
                    .modified(LocalDateTime.now())
                    .build());
        } else {
            return selectedCollections.stream()
                    .map(collection -> Thesaurus.builder()
                            .consultationInstances(instanceSelected)
                            .name(thesaurusSelected.getLabel())
                            .idThesaurus(thesaurusSelected.getId())
                            .collection(collection.getLabel())
                            .idCollection(collection.getId())
                            .created(LocalDateTime.now())
                            .modified(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}
