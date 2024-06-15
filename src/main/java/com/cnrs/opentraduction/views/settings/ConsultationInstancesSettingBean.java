package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.CollectionElementModel;
import com.cnrs.opentraduction.models.InstanceModel;
import com.cnrs.opentraduction.models.ThesaurusElementModel;
import com.cnrs.opentraduction.services.ConsultationInstanceService;
import com.cnrs.opentraduction.utils.MessageUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@Slf4j
@SessionScoped
@Named(value = "consultationInstancesBean")
public class ConsultationInstancesSettingBean implements Serializable {

    private ConsultationInstanceService consultationInstanceService;

    private ConsultationInstances instanceSelected;
    private List<InstanceModel> consultationInstances;

    private List<ThesaurusElementModel> thesaurusList;
    private ThesaurusElementModel thesaurusSelected;

    private List<CollectionElementModel> collectionList;
    private CollectionElementModel collectionSelected;
    private boolean thesaurusListStatut, collectionsListStatut, validateBtnStatut;

    private String instanceName, instanceUrl;
    private String dialogTitle;


    public ConsultationInstancesSettingBean(ConsultationInstanceService consultationInstanceService) {

        this.consultationInstanceService = consultationInstanceService;
    }

    public void initialInterface() {
        consultationInstances = consultationInstanceService.getAllInstances();

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
        instanceSelected = new ConsultationInstances();
        PrimeFaces.current().executeScript("PF('instanceDialog').show();");
    }

    public void searchThesaurus() {
        collectionsListStatut = false;

        thesaurusList = consultationInstanceService.searchThesaurus(instanceUrl);
        thesaurusListStatut = !CollectionUtils.isEmpty(thesaurusList);

        if (thesaurusListStatut) {
            thesaurusSelected = thesaurusList.get(0);
            searchCollections();
        }
    }

    public void searchCollections() {

        collectionList = consultationInstanceService.searchCollections(instanceUrl, thesaurusSelected.getId());

        validateBtnStatut = true;

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionSelected = collectionList.get(0);
            collectionsListStatut = true;
        }
    }

    public void deleteInstance(InstanceModel instance) {
        if (!ObjectUtils.isEmpty(instance)) {
            consultationInstanceService.deleteInstance(instance.getId());
            consultationInstances = consultationInstanceService.getAllInstances();
            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "L'instance a été supprimée avec succès !");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'instance que vous voulez supprimer n'existe pas !");
        }
    }

    public void initialUpdateInstanceDialog(InstanceModel instance) {

        if (!ObjectUtils.isEmpty(instance)) {

            dialogTitle = "Modifier l'instance " + instance.getName();

            instanceSelected = consultationInstanceService.getInstanceById(instance.getId());

            instanceName = instanceSelected.getName();
            instanceUrl = instanceSelected.getUrl();

            validateBtnStatut = false;
            thesaurusListStatut = false;
            collectionsListStatut = false;

            thesaurusList = List.of();
            thesaurusSelected = null;

            collectionList = List.of();
            collectionSelected = null;

            if (!CollectionUtils.isEmpty(instanceSelected.getThesauruses())) {
                var thesaurusSaved = instanceSelected.getThesauruses().stream().findFirst();

                thesaurusList = consultationInstanceService.searchThesaurus(instanceUrl);

                if (!CollectionUtils.isEmpty(thesaurusList)) {
                    thesaurusListStatut = true;
                    var thesaurusTmp = thesaurusList.stream()
                            .filter(element -> element.getId().equals(thesaurusSaved.get().getIdThesaurus()))
                            .findFirst();
                    if (thesaurusTmp.isPresent()) {
                        thesaurusSelected = thesaurusTmp.get();

                        validateBtnStatut = true;
                        collectionList = consultationInstanceService.searchCollections(instanceUrl, thesaurusSelected.getId());
                        if (!CollectionUtils.isEmpty(collectionList)) {
                            var collectionTmp = collectionList.stream()
                                    .filter(element -> element.getId().equals(thesaurusSaved.get().getIdCollection()))
                                    .findFirst();
                            collectionsListStatut = true;
                            if (collectionTmp.isPresent()) {
                                collectionSelected = collectionTmp.get();
                            }
                        }
                    }
                }
            }

            PrimeFaces.current().executeScript("PF('instanceDialog').show();");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'instance que vous voulez modifier n'existe pas !");
        }
    }

    public void instanceManagement() {

        instanceSelected.setName(instanceName);
        instanceSelected.setUrl(instanceUrl);

        Thesaurus thesaurus = new Thesaurus();
        thesaurus.setInstance(instanceSelected);
        thesaurus.setName(thesaurusSelected.getLabel());
        thesaurus.setIdThesaurus(thesaurusSelected.getId());
        thesaurus.setCollection(collectionSelected.getLabel());
        thesaurus.setIdCollection(collectionSelected.getId());

        consultationInstanceService.saveInstance(instanceSelected,  thesaurus);

        consultationInstances = consultationInstanceService.getAllInstances();

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Instance enregistrée avec succès");

        PrimeFaces.current().executeScript("PF('instanceDialog').hide();");
        log.info("Instance enregistrée avec succès !");
    }
}
