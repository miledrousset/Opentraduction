package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.dao.ReferenceInstanceDao;
import com.cnrs.opentraduction.models.client.ThesaurusElementModel;
import com.cnrs.opentraduction.services.ConsultationInstanceService;
import com.cnrs.opentraduction.services.ReferenceInstanceService;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.utils.MessageUtil;

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
import java.util.stream.Collectors;


@Data
@Slf4j
@SessionScoped
@Named(value = "consultationInstancesBean")
public class ConsultationInstancesSettingBean implements Serializable {

    private ConsultationInstanceService consultationInstanceService;
    private ReferenceInstanceService referenceInstanceService;
    private ThesaurusService thesaurusService;

    private ConsultationInstances instanceSelected;
    private List<ConsultationInstanceDao> consultationList;

    private List<ThesaurusElementModel> thesaurusList;
    private ThesaurusElementModel thesaurusSelected;

    private List<CollectionElementDao> collectionList;
    private List<CollectionElementDao> selectedCollections;
    private List<String> selectedIdCollections;

    private boolean thesaurusListStatut, collectionsListStatut, validateBtnStatut;

    private String instanceName, instanceUrl;
    private String dialogTitle;


    public ConsultationInstancesSettingBean(ConsultationInstanceService consultationInstanceService,
                                            ReferenceInstanceService referenceInstanceService,
                                            ThesaurusService thesaurusService) {

        this.consultationInstanceService = consultationInstanceService;
        this.referenceInstanceService = referenceInstanceService;
        this.thesaurusService = thesaurusService;
    }

    public void initialInterface() {
        consultationList = consultationInstanceService.getAllConsultationInstances();

        thesaurusListStatut = false;
        collectionsListStatut = false;

        selectedCollections = new ArrayList<>();
    }

    public void initialAddInstanceDialog() {

        dialogTitle = "Ajouter une nouvelle instance";

        instanceName = "";
        instanceUrl = "";

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

        thesaurusList = thesaurusService.searchThesaurus(instanceUrl);

        if (!CollectionUtils.isEmpty(thesaurusList)) {
            thesaurusListStatut = true;
            thesaurusSelected = thesaurusList.get(0);
            searchCollections();
        }
    }

    public void searchCollections() {

        collectionList = thesaurusService.searchTopCollections(instanceUrl, thesaurusSelected.getId());

        validateBtnStatut = true;

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionsListStatut = true;
        }
    }

    public void deleteInstance(ReferenceInstanceDao instance) {
        if (!ObjectUtils.isEmpty(instance)) {
            consultationInstanceService.deleteInstance(instance.getId());
            consultationList = consultationInstanceService.getAllConsultationInstances();
            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "L'instance a été supprimée avec succès !");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'instance que vous voulez supprimer n'existe pas !");
        }
    }

    public void initialUpdateInstanceDialog(ReferenceInstanceDao instance) {

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

            if (!CollectionUtils.isEmpty(instanceSelected.getThesauruses())) {
                var thesaurusSaved = instanceSelected.getThesauruses().stream().findFirst();

                thesaurusList = thesaurusService.searchThesaurus(instanceUrl);

                if (!CollectionUtils.isEmpty(thesaurusList)) {
                    thesaurusListStatut = true;
                    var thesaurusTmp = thesaurusList.stream()
                            .filter(element -> element.getId().equals(thesaurusSaved.get().getIdThesaurus()))
                            .findFirst();
                    if (thesaurusTmp.isPresent()) {
                        thesaurusSelected = thesaurusTmp.get();

                        validateBtnStatut = true;
                        collectionList = thesaurusService.searchTopCollections(instanceUrl, thesaurusSelected.getId());
                        if (!CollectionUtils.isEmpty(collectionList)) {
                            var collectionTmp = collectionList.stream()
                                    .filter(element -> element.getId().equals(thesaurusSaved.get().getIdCollection()))
                                    .findFirst();
                            collectionsListStatut = true;
                            if (collectionTmp.isPresent()) {
                                //collectionSelected = collectionTmp.get();
                            }
                        }
                    }
                }
            }

            PrimeFaces.current().executeScript("PF('consultationInstanceDialog').show();");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'instance que vous voulez modifier n'existe pas !");
        }
    }

    public void onCollectionSelectedUpdate() {
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

    @Transactional
    public void instanceManagement() {

        if (StringUtils.isEmpty(instanceName)) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le nom de l'instance est obligatoire !");
            return;
        }

        instanceSelected.setName(instanceName);
        instanceSelected.setUrl(instanceUrl);

        var thesaurusList = selectedCollections.stream()
                .map(collection -> Thesaurus.builder()
                        .consultationInstances(instanceSelected)
                        .name(thesaurusSelected.getLabel())
                        .idThesaurus(thesaurusSelected.getId())
                        .collection(collection.getLabel())
                        .idCollection(collection.getId())
                        .build())
                .collect(Collectors.toList());

        if (consultationInstanceService.saveConsultationInstance(instanceSelected,  thesaurusList)) {

            consultationList = consultationInstanceService.getAllConsultationInstances();

            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Instance enregistrée avec succès");

            PrimeFaces.current().executeScript("PF('consultationInstanceDialog').hide();");
            log.info("Instance enregistrée avec succès !");
        }  else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Instance enregistrée avec succès");
            instanceName = "";
            instanceUrl = "";
            log.error("Erreur pendant l'enregistrée l'instance de référence !");
        }
    }
}
