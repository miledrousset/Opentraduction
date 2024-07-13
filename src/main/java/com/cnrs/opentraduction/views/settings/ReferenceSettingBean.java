package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ReferenceInstanceDao;
import com.cnrs.opentraduction.models.client.ThesaurusElementModel;
import com.cnrs.opentraduction.services.ReferenceService;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
@SessionScoped
@Named(value = "referenceBean")
public class ReferenceSettingBean implements Serializable {

    private final ReferenceService referenceService;
    private final ThesaurusService thesaurusService;
    private final MessageService messageService;
    
    private ReferenceInstances referenceSelected;
    private List<ReferenceInstanceDao> referenceInstances;

    private List<ThesaurusElementModel> thesaurusList;
    private ThesaurusElementModel thesaurusSelected;
    private String idThesaurusSelected;

    private List<CollectionElementDao> collectionList;
    private CollectionElementDao collectionSelected;
    private String idCollectionSelected;

    private boolean thesaurusListStatut, collectionsListStatut, validateBtnStatut;

    private String dialogTitle;


    public void setSelectedThesaurus() {
        if (!StringUtils.isEmpty(idThesaurusSelected)) {
            thesaurusSelected = thesaurusList.stream()
                    .filter(element -> element.getId().equals(idThesaurusSelected))
                    .findFirst()
                    .get();

            searchCollections();
        }
    }

    public void setSelectedCollection() {
        if (!StringUtils.isEmpty(idCollectionSelected)) {
            collectionSelected = collectionList.stream()
                    .filter(element -> element.getId().equals(idCollectionSelected))
                    .findFirst()
                    .get();
        }
    }

    public void initialInterface() {
        referenceInstances = referenceService.getAllInstances();

        thesaurusListStatut = false;
        collectionsListStatut = false;
    }

    public void initialAddInstanceDialog() {

        dialogTitle = "Ajouter une nouvelle référence";

        referenceSelected = new ReferenceInstances();

        thesaurusList = List.of();
        collectionList = List.of();

        thesaurusListStatut = false;
        collectionsListStatut = false;
        validateBtnStatut = false;
        referenceSelected = new ReferenceInstances();
        PrimeFaces.current().executeScript("PF('referenceDialog').show();");
    }

    public void searchThesaurus() {
        collectionsListStatut = false;

        thesaurusList = thesaurusService.searchThesaurus(referenceSelected.getUrl());
        thesaurusListStatut = !CollectionUtils.isEmpty(thesaurusList);

        if (thesaurusListStatut) {
            thesaurusSelected = thesaurusList.get(0);
            searchCollections();
        }
    }

    public void searchCollections() {

        collectionList = new ArrayList<>();
        collectionList.add(new CollectionElementDao());
        collectionList.addAll(thesaurusService.searchTopCollections(referenceSelected.getUrl(), thesaurusSelected.getId()));

        validateBtnStatut = true;

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionSelected = collectionList.get(0);
            collectionsListStatut = true;
        }
    }

    @Transactional
    public void deleteInstance(ReferenceInstanceDao instance) {
        if (!ObjectUtils.isEmpty(instance)) {
            referenceService.deleteInstance(instance.getId());
            referenceInstances = referenceService.getAllInstances();
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "system.reference.success.msg2");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg3");
        }
    }

    public void initialUpdateInstanceDialog(ReferenceInstanceDao instance) {

        if (!ObjectUtils.isEmpty(instance)) {

            dialogTitle =  messageService.getMessage("system.reference.update.title") + " " + instance.getName();

            referenceSelected = referenceService.getInstanceById(instance.getId());

            validateBtnStatut = false;
            thesaurusListStatut = false;
            collectionsListStatut = false;

            thesaurusList = List.of();
            thesaurusSelected = null;

            collectionList = List.of();
            collectionSelected = null;

            if (!ObjectUtils.isEmpty(referenceSelected.getThesaurus())) {
                var thesaurusSaved = referenceSelected.getThesaurus();

                thesaurusList = thesaurusService.searchThesaurus(referenceSelected.getUrl());

                if (!CollectionUtils.isEmpty(thesaurusList)) {
                    thesaurusListStatut = true;
                    var thesaurusTmp = thesaurusList.stream()
                            .filter(element -> element.getId().equals(thesaurusSaved.getIdThesaurus()))
                            .findFirst();
                    if (thesaurusTmp.isPresent()) {
                        thesaurusSelected = thesaurusTmp.get();
                        idThesaurusSelected = thesaurusTmp.get().getId();

                        validateBtnStatut = true;
                        collectionList = new ArrayList<>();
                        collectionList.add(new CollectionElementDao());
                        collectionList.addAll(thesaurusService.searchTopCollections(referenceSelected.getUrl(), thesaurusSelected.getId()));
                        if (!CollectionUtils.isEmpty(collectionList)) {
                            collectionsListStatut = true;
                            if (thesaurusSaved.getIdCollection().equals("ALL")) {
                                collectionSelected = collectionList.get(0);
                                idCollectionSelected = collectionList.get(0).getId();
                            } else {
                                var collectionTmp = collectionList.stream()
                                        .filter(element -> thesaurusSaved.getCollection().equals(element.getLabel()))
                                        .findFirst();
                                if (collectionTmp.isPresent()) {
                                    collectionSelected = collectionTmp.get();
                                    idCollectionSelected = collectionTmp.get().getId();
                                }
                            }
                        }
                    }
                }
            }
            PrimeFaces.current().executeScript("PF('referenceDialog').show();");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg3");
        }
    }

    @Transactional
    public void instanceManagement() {

        if (StringUtils.isEmpty(referenceSelected.getName())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg2");
            return;
        }

        if (ObjectUtils.isEmpty(referenceSelected.getId()) && referenceService.checkExistenceByName(referenceSelected.getName())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg4");
            return;
        }

        Thesaurus thesaurus = new Thesaurus();
        thesaurus.setReferenceInstances(referenceSelected);
        thesaurus.setName(thesaurusSelected.getLabel());
        thesaurus.setIdThesaurus(thesaurusSelected.getId());
        thesaurus.setCollection(collectionSelected.getLabel());
        thesaurus.setIdCollection(collectionSelected.getId());

        if (referenceService.saveInstance(referenceSelected,  thesaurus)) {
            referenceInstances = referenceService.getAllInstances();

            messageService.showMessage(FacesMessage.SEVERITY_INFO, "system.reference.success.msg1");

            PrimeFaces.current().executeScript("PF('referenceDialog').hide();");
            log.info("Thésaurus/collection enregistrée avec succès !");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg1");
            referenceSelected = new ReferenceInstances();
            log.error("Erreur pendant l'enregistrée du thésaurus/collection de référence !");
        }
    }
}
