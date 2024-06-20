package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.models.GroupModel;
import com.cnrs.opentraduction.services.GroupService;
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
import java.util.Set;


@Data
@Slf4j
@SessionScoped
@Named(value = "groupsSettingBean")
public class GroupsSettingBean implements Serializable {

    private GroupService groupService;
    private ConsultationInstanceService consultationInstanceService;
    private ConsultationInstancesSettingBean consultationInstancesBean;

    private List<GroupModel> groups;
    private Groups groupSelected;

    private Integer idInstanceSelected;
    private String dialogTitle;


    public GroupsSettingBean(GroupService groupService,
                             ConsultationInstanceService consultationInstanceService,
                             ConsultationInstancesSettingBean consultationInstancesBean) {

        this.groupService = groupService;
        this.consultationInstanceService = consultationInstanceService;
        this.consultationInstancesBean = consultationInstancesBean;
    }

    public void initialInterface() {
        groups = groupService.getAllGroups();
    }

    public void groupManager() {

        var instanceSelected = consultationInstancesBean.getConsultationList().stream()
                .filter(instance -> instance.getId().intValue() == idInstanceSelected.intValue())
                .findFirst();
        if (instanceSelected.isPresent()) {
            var consultationInstances = consultationInstanceService.getInstanceById(instanceSelected.get().getId());
            groupSelected.setConsultationInstances(Set.of(consultationInstances));
        }

        groupService.saveGroup(groupSelected);

        initialInterface();

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Group enregistré avec succès");
        PrimeFaces.current().executeScript("PF('groupDialog').hide();");
        log.info("Group enregistré avec sucée !");
    }

    public void initialAddingGroup() {

        dialogTitle = "Ajouter un nouveau group";
        groupSelected = new Groups();
        if (!CollectionUtils.isEmpty(consultationInstancesBean.getConsultationList())) {
            idInstanceSelected = consultationInstancesBean.getConsultationList().get(0).getId();
        }
        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void initialUpdateGroup(Groups groups) {

        dialogTitle = "Modifier le group " + groups.getName();
        groupSelected = groups;
        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void deleteGroup(GroupModel group) {

        if (!ObjectUtils.isEmpty(group)) {
            groupService.deleteGroup(group.getId());
            groups = groupService.getAllGroups();
            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Le group a été supprimée avec succès !");
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le group que vous voulez supprimer n'existe pas !");
        }
    }
}
