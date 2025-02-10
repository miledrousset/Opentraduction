package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.dao.ConsultationCollectionDao;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.DateUtils;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
@SessionScoped
@Named(value = "userSettingsBean")
public class UserSettingsBean implements Serializable {

    private final MessageService messageService;
    private final ThesaurusService thesaurusService;
    private final UserService userService;

    private Users userConnected;
    private List<ConsultationCollectionDao> consultationThesaurusList;
    private boolean userApiKeyAlert, referenceProjetAlert;
    private String dialogTitle, francaisLabel, arabeLabel;


    public void initialInterface(Integer userConnectedId) {

        this.userConnected = userService.getUserById(userConnectedId);
        log.info("Initialisation de l'interface de paramétrage de l'utilisateur connecté {}", userConnected.getFullName());

        log.info("Vérification de la présence de clé API utilisateur");
        userApiKeyAlert = StringUtils.isEmpty(this.userConnected.getApiKey());

        log.info("Vérification de la présence d'un projet de référence'");
        if (!ObjectUtils.isEmpty(userConnected.getGroup())) {
            referenceProjetAlert = ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances());
        }

        log.info("Préparation du projet de consultation");
        searchConsultationThesaurus();

        francaisLabel = messageService.getMessage("application.language.french");
        arabeLabel = messageService.getMessage("application.language.arabic");
    }

    private void searchConsultationThesaurus() {

        consultationThesaurusList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(userConnected.getGroup())) {
            var consultationThesaurus = userConnected.getGroup().getConsultationInstances();

            if (!CollectionUtils.isEmpty(consultationThesaurus)) {
                for (ConsultationInstances consultationInstances : consultationThesaurus) {
                    for (Thesaurus thesaurus : consultationInstances.getThesauruses()) {
                        consultationThesaurusList.add(ConsultationCollectionDao.builder()
                                .id(thesaurus.getId())
                                .name(consultationInstances.getName())
                                .url(consultationInstances.getUrl())
                                .thesaurusName(thesaurus.getName())
                                .thesaurusId(thesaurus.getIdThesaurus())
                                .collectionId(thesaurus.getIdCollection())
                                .collectionName(thesaurus.getCollection())
                                .build());
                    }
                }
            }
        }
    }

    public String getUserAudit() {

        return ObjectUtils.isEmpty(userConnected.getModified()) ? getCreatedLabel(userConnected.getCreated())
                : (getCreatedLabel(userConnected.getCreated()) + getUpdateLabel(userConnected.getModified()));
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

        if (userService.saveUser(userConnected, userConnected.getApiKey())) {
            userApiKeyAlert = StringUtils.isEmpty(userConnected.getApiKey());
            referenceProjetAlert = ObjectUtils.isEmpty(userConnected.getGroup()) && ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances());
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

    private void errorCase(String codeMessage) {
        this.userConnected = userService.getUserById(userConnected.getId());
        messageService.showMessage(FacesMessage.SEVERITY_ERROR, codeMessage);
        log.error(messageService.getMessage(codeMessage));
    }

    public boolean showConsultationProjects() {
        return !CollectionUtils.isEmpty(consultationThesaurusList);
    }

    public boolean showReferenceProject() {
        return !ObjectUtils.isEmpty(userConnected.getGroup())
                && !ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances());
    }

    public String getCollectionName() {
        return "ALL".equalsIgnoreCase(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())
                ? messageService.getMessage("system.reference.root")
                : userConnected.getGroup().getReferenceInstances().getThesaurus().getCollection();
    }

    public String getThesaurusReferenceUrl() {

        if (!ObjectUtils.isEmpty(userConnected.getGroup())
                && !ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            if ("ALL".equalsIgnoreCase(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())) {
                return String.format("%s/?idt=%s", getInstanceReferenceUrl(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus());
            } else {
                return String.format("%s/?idg=%s&idt=%s", getInstanceReferenceUrl(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus());
            }
        } else {
            return "";
        }
    }
}
