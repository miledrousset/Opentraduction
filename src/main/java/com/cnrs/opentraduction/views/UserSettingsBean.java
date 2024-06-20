package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.config.LocaleManagement;
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
import org.springframework.context.MessageSource;
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
    private ReferenceInstances referenceInstances;

    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;
    private String idReferenceCollectionSelected;

    private List<ConsultationCollectionDao> consultationThesaurusList;


    public void initialInterface(Users userConnected) {

        this.userConnected = userConnected;

        referenceInstances = userConnected.getGroup().getReferenceInstances();

        referenceCollectionList = new ArrayList<>();
        referenceCollectionList.add(new CollectionElementDao("ALL", "Dans la racine"));
        referenceCollectionList.addAll(thesaurusService.searchCollections(referenceInstances.getUrl(),
                referenceInstances.getThesaurus().getIdThesaurus(),
                referenceInstances.getThesaurus().getIdCollection()));

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
        var str = "Créé le : " + DateUtils.formatLocalDate(userConnected.getCreated(), DateUtils.DATE_TIME_FORMAT);
        if (!ObjectUtils.isEmpty(userConnected.getModified())) {
            str += ", dernière mise à jour effectuée le : " + DateUtils.formatLocalDate(userConnected.getModified(), DateUtils.DATE_TIME_FORMAT);
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
            log.info("Enregistrement effectuée avec sucée !");
        }  else {
            errorCase("user.settings.error.msg0");
        }
    }

    private void errorCase(String codeMessage) {
        this.userConnected = userService.getUserById(userConnected.getId());
        MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, messageSource.getMessage(codeMessage, null, localeManagement.getCurrentLocale()));
        log.error(messageSource.getMessage(codeMessage, null, new Locale("fr")));
    }
}
