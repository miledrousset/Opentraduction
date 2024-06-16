package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.CollectionElementModel;
import com.cnrs.opentraduction.models.InstanceModel;
import com.cnrs.opentraduction.models.ThesaurusElementModel;
import com.cnrs.opentraduction.services.ReferenceInstanceService;
import com.cnrs.opentraduction.utils.MessageUtil;

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
import java.util.List;


@Data
@Slf4j
@SessionScoped
@Named(value = "referenceInstancesBean")
public class ReferenceInstancesSettingBean implements Serializable {

    private ReferenceInstanceService referenceInstanceService;

    private ReferenceInstances referenceSelected;
    private List<InstanceModel> referenceInstances;

    private List<ThesaurusElementModel> thesaurusList;
    private ThesaurusElementModel thesaurusSelected;
    private String idThesaurusSelected;

    private List<CollectionElementModel> collectionList;
    private CollectionElementModel collectionSelected;
    private String idCollectionSelected;

    private boolean thesaurusListStatut, collectionsListStatut, validateBtnStatut;

    private String instanceName, instanceUrl;
    private String dialogTitle;


    public ReferenceInstancesSettingBean(ReferenceInstanceService referenceInstanceService) {

        this.referenceInstanceService = referenceInstanceService;
    }

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
        referenceInstances = referenceInstanceService.getAllInstances();

        thesaurusListStatut = false;
        collectionsListStatut = false;
    }

    public void initialAddInstanceDialog() {

        dialogTitle = "Ajouter une nouvelle instance";

        instanceName = "";
        instanceUrl = "";

        thesaurusList = List.of();
        collectionList = List.of();

        thesaurusListStatut = false;
        collectionsListStatut = false;
        validateBtnStatut = false;
        referenceSelected = new ReferenceInstances();
        PrimeFaces.current().executeScript("PF('referenceInstanceDialog').show();");
    }

    public void searchThesaurus() {
        collectionsListStatut = false;

        thesaurusList = referenceInstanceService.searchThesaurus(instanceUrl);
        thesaurusListStatut = !CollectionUtils.isEmpty(thesaurusList);

        if (thesaurusListStatut) {
            thesaurusSelected = thesaurusList.get(0);
            searchCollections();
        }
    }

    public void searchCollections() {

        collectionList = referenceInstanceService.searchCollections(instanceUrl, thesaurusSelected.getId());

        validateBtnStatut = true;

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionSelected = collectionList.get(0);
            collectionsListStatut = true;
        }
    }

    public void deleteInstance(InstanceModel instance) {
        if (!ObjectUtils.isEmpty(instance)) {
            referenceInstanceService.deleteInstance(instance.getId());
            referenceInstances = referenceInstanceService.getAllInstances();
            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "L'instance a été supprimée avec succès !");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'instance que vous voulez supprimer n'existe pas !");
        }
    }

    public void initialUpdateInstanceDialog(InstanceModel instance) {

        if (!ObjectUtils.isEmpty(instance)) {

            dialogTitle = "Modifier l'instance " + instance.getName();

            referenceSelected = referenceInstanceService.getInstanceById(instance.getId());

            instanceName = referenceSelected.getName();
            instanceUrl = referenceSelected.getUrl();

            validateBtnStatut = false;
            thesaurusListStatut = false;
            collectionsListStatut = false;

            thesaurusList = List.of();
            thesaurusSelected = null;

            collectionList = List.of();
            collectionSelected = null;

            if (!ObjectUtils.isEmpty(referenceSelected.getThesaurus())) {
                var thesaurusSaved = referenceSelected.getThesaurus();

                thesaurusList = referenceInstanceService.searchThesaurus(instanceUrl);

                if (!CollectionUtils.isEmpty(thesaurusList)) {
                    thesaurusListStatut = true;
                    var thesaurusTmp = thesaurusList.stream()
                            .filter(element -> element.getId().equals(thesaurusSaved.getIdThesaurus()))
                            .findFirst();
                    if (thesaurusTmp.isPresent()) {
                        thesaurusSelected = thesaurusTmp.get();

                        validateBtnStatut = true;
                        collectionList = referenceInstanceService.searchCollections(instanceUrl, thesaurusSelected.getId());
                        if (!CollectionUtils.isEmpty(collectionList)) {
                            var collectionTmp = collectionList.stream()
                                    .filter(element -> element.getId().equals(thesaurusSaved.getIdCollection()))
                                    .findFirst();
                            collectionsListStatut = true;
                            if (collectionTmp.isPresent()) {
                                collectionSelected = collectionTmp.get();
                            }
                        }
                    }
                }
            }

            PrimeFaces.current().executeScript("PF('referenceInstanceDialog').show();");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'instance que vous voulez modifier n'existe pas !");
        }
    }

    public void instanceManagement() {

        referenceSelected.setName(instanceName);
        referenceSelected.setUrl(instanceUrl);

        Thesaurus thesaurus = new Thesaurus();
        thesaurus.setReferenceInstances(referenceSelected);
        thesaurus.setName(thesaurusSelected.getLabel());
        thesaurus.setIdThesaurus(thesaurusSelected.getId());
        thesaurus.setCollection(collectionSelected.getLabel());
        thesaurus.setIdCollection(collectionSelected.getId());

        referenceInstanceService.saveInstance(referenceSelected,  thesaurus);

        referenceInstances = referenceInstanceService.getAllInstances();

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Instance enregistrée avec succès");

        PrimeFaces.current().executeScript("PF('referenceInstanceDialog').hide();");
        log.info("Instance enregistrée avec succès !");
    }
}
