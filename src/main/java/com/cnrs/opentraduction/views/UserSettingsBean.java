package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.entities.UsersThesaurus;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConsultationCollectionDao;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.DateUtils;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Data
@SessionScoped
@Named(value = "userSettingsBean")
public class UserSettingsBean implements Serializable {

    private final MessageService messageService;
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

    private boolean displayDialog;
    private String errorMessage;


    public void initialInterface(Users userConnected) {

        log.info("Initialisation de l'interface de paramétrage de l'utilisateur connecté {}", userConnected.getFullName());
        this.userConnected = userConnected;

        log.info("Vérification de la présence de clé API utilisateur");
        if (StringUtils.isEmpty(userConnected.getApiKey())) {
            log.error("L'utilisateur {} ne dispose de pas de clé API", userConnected.getFullName());
            displayDialog = true;
            errorMessage = messageService.getMessage("application.user.error.msg2");
        }


        log.info("Préparation du projet de référence");
        referenceInstance = userConnected.getGroup().getReferenceInstances();

        if (!ObjectUtils.isEmpty(referenceInstance)) {
            displayDialog = true;
            errorMessage = messageService.getMessage("application.user.error.msg3");
        }

        log.info("Préparation des projets de consultation");
        referenceCollectionList = new ArrayList<>();
        referenceCollectionList.add(new CollectionElementDao("ALL", messageService.getMessage("user.settings.consultation.racine")));
        if (!ObjectUtils.isEmpty(referenceInstance)) {
            referenceCollectionList.addAll(thesaurusService.searchSubCollections(referenceInstance.getUrl(),
                    referenceInstance.getThesaurus().getIdThesaurus(),
                    referenceInstance.getThesaurus().getIdCollection()));
        }

        if (!CollectionUtils.isEmpty(userConnected.getThesauruses())) {
            setSelectedReferenceSubCollection();
            setSelectedConsultationSubCollection();
        }
    }

    private void setSelectedReferenceSubCollection() {
        var selectedReferenceInstance = userConnected.getThesauruses().stream()
                .filter(element -> !ObjectUtils.isEmpty(element.getReferenceInstances()))
                .findFirst();

        selectedReferenceInstance.ifPresent(thesaurus -> {
            var referenceInstanceTmp = referenceCollectionList.stream()
                    .filter(element -> element.getId().equals(thesaurus.getIdCollection()))
                    .findFirst();
            if (referenceInstanceTmp.isPresent()) {
                collectionReferenceSelected = referenceInstanceTmp.get();
            }
        });
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
            var collectionReferenceTmp = referenceCollectionList.stream()
                    .filter(element -> element.getLabel().equals(idReferenceCollectionSelected))
                    .findFirst();
            collectionReferenceTmp.ifPresent(collectionElementDao -> collectionReferenceSelected = collectionElementDao);
        }
    }

    public String getUserAudit() {

        return ObjectUtils.isEmpty(userConnected.getModified()) ? getCreatedLabel(userConnected.getCreated())
                : (getCreatedLabel(userConnected.getCreated()) + getUpdateLabel(userConnected.getModified()));
    }

    public String getCollectionReferenceAudit() {
        log.info("Génération de la phrase d'audit pour la brique projet de référence");
        if (!ObjectUtils.isEmpty(referenceInstance)) {
            return ObjectUtils.isEmpty(referenceInstance.getModified()) ? getCreatedLabel(referenceInstance.getCreated())
                    : (getCreatedLabel(referenceInstance.getCreated()) + getUpdateLabel(referenceInstance.getModified()));
        } else {
            return "";
        }
    }

    public String getCollectionCollectionAudit() {

        var userCollectionsConsultation = userService.getUserConsultationCollections(userConnected.getId());

        if (!CollectionUtils.isEmpty(userCollectionsConsultation)) {
            userCollectionsConsultation.sort(Comparator.comparing(UsersThesaurus::getModified).reversed());

            var lastCollectionUpdated = userCollectionsConsultation.get(0);

            return ObjectUtils.isEmpty(lastCollectionUpdated.getModified()) ? getCreatedLabel(lastCollectionUpdated.getCreated())
                    : (getCreatedLabel(lastCollectionUpdated.getCreated()) + getUpdateLabel(lastCollectionUpdated.getModified()));
        } else {
            return "";
        }
    }

    private String getUpdateLabel(LocalDateTime date) {
        return messageService.getMessage("user.settings.updated")
                + DateUtils.formatLocalDate(date, DateUtils.DATE_TIME_FORMAT);
    }

    private String getCreatedLabel(LocalDateTime date) {
        return messageService.getMessage("user.settings.created")
                + DateUtils.formatLocalDate(date, DateUtils.DATE_TIME_FORMAT);
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
            messageService.showMessage(FacesMessage.SEVERITY_INFO, ("user.settings.ok.msg0"));
            log.info("Enregistrement effectuée avec succès !");
        }  else {
            errorCase("user.settings.error.msg0");
        }
    }

    public String getInstanceReferenceUrl() {

        if (!ObjectUtils.isEmpty(userConnected.getGroup())
                && !ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            return userConnected.getGroup().getReferenceInstances().getUrl();
        }
        return "";
    }

    public String getThesaurusReferenceUrl() {

        if (!ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            return getInstanceReferenceUrl() + "/?idt="
                    + userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus();
        } else {
            return "";
        }
    }

    public void saveCollectionReference() {

        log.info("Enregistrement des informations sur le projet de référence");
        if (userService.addThesaurusToUser(userConnected.getId(), referenceInstance.getThesaurus(), collectionReferenceSelected)) {
            log.info("Affichage des informations suite à la mise à jour du projet de référence");
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "user.settings.ok.msg1");
            userConnected = userService.getUserById(userConnected.getId());
            referenceInstance = userConnected.getGroup().getReferenceInstances();
            log.info("Collection de référence enregistrée avec succès !");
        } else {
            log.error("Erreur pendant la mise à jour du projet de référence");
            errorCase("user.settings.error.msg4");
        }
    }

    @Transactional
    public void deleteCollectionConsultation(ConsultationCollectionDao consultationCollection) {

        if (userService.deleteConsultationCollection(consultationCollection.getId(), userConnected.getId())) {
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "user.settings.ok.msg1");
            log.info("Collection de conllection supprimée avec succès !");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.ok.msg1");
            log.info("Erreur pendant la suppression de la collection de consultation !");
        }

        var user = userService.getUserById(userConnected.getId());
        initialInterface(user);
    }

    public void initialConsultationDialogForInsert() {

        dialogTitle = messageService.getMessage("user.settings.consultation.add.title");
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

        return new ArrayList<>(thesaurusList);
    }

    public void searchCollections() {
        var tmp = userConnected.getGroup().getConsultationInstances().stream()
                .flatMap(outer -> outer.getThesauruses().stream())
                .filter(element -> element.getName().equals(thesaurusSelected))
                .map(Thesaurus::getCollection)
                .collect(Collectors.toSet());

        collectionList = new ArrayList<>(tmp);

        if (!CollectionUtils.isEmpty(collectionList)) {
            collectionSelected = collectionList.get(0);
            var consultationCollectionTmp = userConnected.getGroup().getConsultationInstances().stream()
                    .flatMap(outer -> outer.getThesauruses().stream())
                    .filter(element -> element.getCollection().equals(collectionSelected))
                    .findFirst();
            if (consultationCollectionTmp.isPresent()) {
                consultationCollectionSelected = consultationCollectionTmp.get();
                searchSubCollections();
            }
        }
    }

    public void searchSubCollections() {

        subCollectionList = new ArrayList<>();
        subCollectionList.add(new CollectionElementDao("ALL", messageService.getMessage("user.settings.consultation.racine")));
        subCollectionList.addAll(thesaurusService.searchSubCollections(consultationCollectionSelected.getConsultationInstances().getUrl(),
                consultationCollectionSelected.getIdThesaurus(),
                consultationCollectionSelected.getIdCollection()));
        subCollectionIdSelected = null;
    }

    private void errorCase(String codeMessage) {
        this.userConnected = userService.getUserById(userConnected.getId());
        messageService.showMessage(FacesMessage.SEVERITY_ERROR, codeMessage);
        log.error(messageService.getMessage(codeMessage));
    }

    public boolean showConsultationProjects() {
        return !CollectionUtils.isEmpty(consultationThesaurusList);
    }

    public boolean showReferenceProject() {
        return !ObjectUtils.isEmpty(referenceInstance);
    }
}
