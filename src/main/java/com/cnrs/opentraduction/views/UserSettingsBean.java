package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConsultationCollectionDao;
import com.cnrs.opentraduction.services.ThesaurusService;
import com.cnrs.opentraduction.utils.DateUtils;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Data
@SessionScoped
@Named(value = "userSettingsBean")
public class UserSettingsBean implements Serializable {

    private ThesaurusService thesaurusService;

    private Users userConnected;
    private ReferenceInstances referenceInstances;

    private List<CollectionElementDao> referenceCollectionList;
    private CollectionElementDao collectionReferenceSelected;
    private String idReferenceCollectionSelected;

    private List<ConsultationCollectionDao> consultationThesaurusList;


    public UserSettingsBean(ThesaurusService thesaurusService) {
        this.thesaurusService = thesaurusService;
    }


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
        if (selectedReferenceInstance.isPresent()) {
            collectionReferenceSelected = referenceCollectionList.stream()
                    .filter(element -> element.getId().equals(selectedReferenceInstance.get().getIdCollection()))
                    .findFirst()
                    .get();
        }
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
}
