package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.config.LocaleManagement;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConsultationCollectionDao;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.DateUtils;
import com.cnrs.opentraduction.utils.MessageUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.MessageSource;
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
import java.util.Locale;
import java.util.stream.Collectors;


@Slf4j
@Data
@SessionScoped
@Named(value = "userSettingsBean")
public class UserSettingsBean implements Serializable {

    private final MessageSource messageSource;
    private final LocaleManagement localeManagement;

    private final ThesaurusService thesaurusService;
    private final UserService userService;

    private Users userConnected;
    private ReferenceInstances referenceInstance;

    private List<ConsultationInstances> consultationInstances;

    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;
    private String idReferenceCollectionSelected;

    private List<ConsultationCollectionDao> consultationThesaurusList;

    private String dialogTitle;
    private String thesaurusSelected, collectionSelected, subCollectionIdSelected;
    private List<String> thesaurusList, collectionList;
    private Thesaurus consultationCollectionSelected;
    private List<CollectionElementDao> subCollectionList;


    public void initialInterface(Users userConnected) {

        this.userConnected = userConnected;

        referenceInstance = userConnected.getGroup().getReferenceInstances();

        referenceCollectionList = new ArrayList<>();
        referenceCollectionList.add(new CollectionElementDao("ALL", "Dans la racine"));
        referenceCollectionList.addAll(thesaurusService.searchCollections(referenceInstance.getUrl(),
                referenceInstance.getThesaurus().getIdThesaurus(),
                referenceInstance.getThesaurus().getIdCollection()));

        if (!CollectionUtils.isEmpty(userConnected.getThesauruses())) {
            setSelectedReferenceSubCollection();
            setSelectedConsultationSubCollection();
        }
    }

    private void setSelectedReferenceSubCollection() {
        var selectedReferenceInstance = userConnected.getThesauruses().stream()
                .filter(element -> !ObjectUtils.isEmpty(element.getReferenceInstances()))
                .findFirst();
        selectedReferenceInstance.ifPresent(thesaurus -> collectionReferenceSelected = referenceCollectionList.stream()
                .filter(element -> element.getId().equals(thesaurus.getIdCollection()))
                .findFirst()
                .get());
    }

    private void setSelectedConsultationSubCollection() {

        consultationThesaurusList = new ArrayList<>();

        var selectedReferenceInstance = userConnected.getThesauruses().stream()
                .filter(element -> !ObjectUtils.isEmpty(element.getConsultationInstances()))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(selectedReferenceInstance)) {
            for (Thesaurus thesaurus : selectedReferenceInstance) {
                consultationThesaurusList.add(ConsultationCollectionDao.builder()
                        .id(thesaurus.getId())
                        .name(thesaurus.getConsultationInstances().getName())
                        .url(thesaurus.getConsultationInstances().getUrl())
                        .thesaurusName(thesaurus.getName())
                        .thesaurusId(thesaurus.getIdThesaurus())
                        .collectionId(thesaurus.getIdCollection())
                        .collectionName(thesaurus.getCollection())
                        .build());
            }
        }
    }

    public void setSelectedReferenceCollection() {
        if (!StringUtils.isEmpty(idReferenceCollectionSelected)) {
            collectionReferenceSelected = referenceCollectionList.stream()
                    .filter(element -> element.getId().equals(idReferenceCollectionSelected))
                    .findFirst()
                    .get();
        }
    }

    public String getUserAudit() {
        var created = messageSource.getMessage("user.settings.created", null, localeManagement.getCurrentLocale());
        var str = created + DateUtils.formatLocalDate(userConnected.getCreated(), DateUtils.DATE_TIME_FORMAT);
        if (!ObjectUtils.isEmpty(userConnected.getModified())) {
            var updated = messageSource.getMessage("user.settings.updated", null, localeManagement.getCurrentLocale());
            str += updated + DateUtils.formatLocalDate(userConnected.getModified(), DateUtils.DATE_TIME_FORMAT);
        }
        return str;
    }

    public String getCollectionReferenceAudit() {
        var created = messageSource.getMessage("user.settings.created", null, localeManagement.getCurrentLocale());
        var str = created + DateUtils.formatLocalDate(referenceInstance.getCreated(), DateUtils.DATE_TIME_FORMAT);
        if (!ObjectUtils.isEmpty(referenceInstance.getModified())) {
            var updated = messageSource.getMessage("user.settings.updated", null, localeManagement.getCurrentLocale());
            str += updated + DateUtils.formatLocalDate(referenceInstance.getModified(), DateUtils.DATE_TIME_FORMAT);
        }
        return str;
    }

    public String getCollectionCollectionAudit() {
        var created = messageSource.getMessage("user.settings.created", null, localeManagement.getCurrentLocale());
        var str = created + DateUtils.formatLocalDate(userConnected.getCreated(), DateUtils.DATE_TIME_FORMAT);
        if (!ObjectUtils.isEmpty(userConnected.getModified())) {
            var updated = messageSource.getMessage("user.settings.updated", null, localeManagement.getCurrentLocale());
            str += updated + DateUtils.formatLocalDate(userConnected.getModified(), DateUtils.DATE_TIME_FORMAT);
        }
        return str;
    }

    public void saveUserInformations() {

        log.info("Début de l'enregistrement des informations utilisateur !");

        if (StringUtils.isEmpty(userConnected.getMail())) {
            errorCase("user.settings.error.msg1");
            return;
        }

        if (StringUtils.isEmpty(userConnected.getLogin())) {
            errorCase("user.settings.error.msg2");
            return;
        }

        if (StringUtils.isEmpty(userConnected.getPassword())) {
            errorCase("user.settings.error.msg3");
            return;
        }

        if (userService.saveUser(userConnected)) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, messageSource.getMessage("user.settings.ok.msg0",
                    null, localeManagement.getCurrentLocale()));
            log.info("Enregistrement effectuée avec succès !");
        }  else {
            errorCase("user.settings.error.msg0");
        }
    }

    public String getInstanceReferenceUrl() {

        return userConnected.getGroup().getReferenceInstances().getUrl();
    }

    public String getThesaurusReferenceUrl() {

        return getInstanceReferenceUrl() + "/?idt=" + userConnected.getGroup().getReferenceInstances()
                .getThesaurus().getIdThesaurus();
    }

    public void saveCollectionReference() {

        if (userService.addThesaurusToUser(userConnected.getId(), referenceInstance.getThesaurus(), collectionReferenceSelected)) {

            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, messageSource.getMessage("user.settings.ok.msg1",
                    null, localeManagement.getCurrentLocale()));
            log.info("Collection de référence enregistrée avec succès !");
        } else {

            errorCase("user.settings.error.msg4");
        }
    }

    @Transactional
    public void deleteCollectionConsultation(ConsultationCollectionDao consultationCollection) {

        if (userService.deleteConsultationCollection(consultationCollection.getId(), userConnected.getId())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, messageSource.getMessage("user.settings.ok.msg1",
                    null, localeManagement.getCurrentLocale()));
            log.info("Collection de conllection supprimée avec succès !");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, messageSource.getMessage("user.settings.ok.msg1",
                    null, localeManagement.getCurrentLocale()));
            log.info("Erreur pendant la suppression de la collection de consultation !");
        }

        var user = userService.getUserById(userConnected.getId());
        initialInterface(user);
    }

    public void initialConsultationDialogForInsert() {

        dialogTitle = "Ajouter une nouvelle collection de consultation";
        thesaurusSelected = "";
        collectionSelected = "";
        collectionList = List.of();
        subCollectionList = List.of();
        subCollectionIdSelected = null;
        thesaurusList = getThesaurusForConsultation();
        PrimeFaces.current().executeScript("PF('consultationThesaurusDialog').show();");
    }

    public List<String> getThesaurusForConsultation() {

        var thesaurusList = userConnected.getGroup().getConsultationInstances().stream()
                .flatMap(outer -> outer.getThesauruses().stream())
                .map(Thesaurus::getName)
                .collect(Collectors.toSet());

        if (!CollectionUtils.isEmpty(thesaurusList)) {
            thesaurusSelected = thesaurusList.stream().findFirst().get();
            searchCollections();
        }

        return thesaurusList.stream().collect(Collectors.toList());
    }

    public void searchCollections() {
        var tmp = userConnected.getGroup().getConsultationInstances().stream()
                .flatMap(outer -> outer.getThesauruses().stream())
                .filter(element -> element.getName().equals(thesaurusSelected))
                .map(Thesaurus::getCollection)
                .collect(Collectors.toSet());

        collectionList = tmp.stream().collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionSelected = collectionList.get(0);
            consultationCollectionSelected = userConnected.getGroup().getConsultationInstances().stream()
                    .flatMap(outer -> outer.getThesauruses().stream())
                    .filter(element -> element.getCollection().equals(collectionSelected))
                    .findFirst()
                    .get();
            searchSubCollections();
        }
    }

    public void searchSubCollections() {

        subCollectionList = new ArrayList<>();
        subCollectionList.add(new CollectionElementDao("ALL", "Dans la racine"));
        subCollectionList.addAll(thesaurusService.searchCollections(consultationCollectionSelected.getConsultationInstances().getUrl(),
                consultationCollectionSelected.getIdThesaurus(),
                consultationCollectionSelected.getIdCollection()));
        subCollectionIdSelected = null;
    }

    private void errorCase(String codeMessage) {
        this.userConnected = userService.getUserById(userConnected.getId());
        MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, messageSource.getMessage(codeMessage, null, localeManagement.getCurrentLocale()));
        log.error(messageSource.getMessage(codeMessage, null, new Locale("fr")));
    }
}
