package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.client.ThesaurusElementModel;
import com.cnrs.opentraduction.services.ConsultationInstanceService;
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
import java.util.stream.Collectors;


@Data
@Slf4j
@SessionScoped
@Named(value = "consultationInstancesBean")
public class ConsultationInstancesSettingBean implements Serializable {

    private final ConsultationInstanceService consultationInstanceService;
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

    private String instanceName, instanceUrl;
    private String dialogTitle;


    public void initialInterface() {
        consultationList = consultationInstanceService.getAllConsultationInstances();

        thesaurusListStatut = false;
        collectionsListStatut = false;

        selectedCollections = new ArrayList<>();
    }

    public void initialAddInstanceDialog() {

        dialogTitle = messageService.getMessage("system.consultation.add");

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
            thesaurusIdSelected = thesaurusSelected.getId();
            searchCollections();
        }
    }

    public void searchCollections() {

        thesaurusSelected = thesaurusList.stream()
                .filter(element -> element.getId().equals(thesaurusIdSelected))
                .findFirst().get();

        collectionList = thesaurusService.searchTopCollections(instanceUrl, thesaurusSelected.getId());

        validateBtnStatut = true;

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionsListStatut = true;
        }
    }

    public void deleteInstance(ConsultationInstanceDao instance) {
        if (!ObjectUtils.isEmpty(instance)) {
            consultationInstanceService.deleteInstance(instance.getId());
            consultationList = consultationInstanceService.getAllConsultationInstances();
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "system.consultation.success.msg1");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.consultation.failed.msg1");
        }
    }

    public void initialUpdateInstanceDialog(ConsultationInstanceDao instance) {

        if (!ObjectUtils.isEmpty(instance)) {

            dialogTitle = messageService.getMessage("system.consultation.update") + instance.getName();

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
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.consultation.failed.msg1");
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
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.reference.failed.msg2");
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

            messageService.showMessage(FacesMessage.SEVERITY_INFO, "system.consultation.success.msg2");

            PrimeFaces.current().executeScript("PF('consultationInstanceDialog').hide();");
            log.info("Instance enregistrée avec succès !");
        }  else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "system.consultation.failed.msg2");
            instanceName = "";
            instanceUrl = "";
            log.error("Erreur pendant l'enregistrée l'instance de référence !");
        }
    }
}
