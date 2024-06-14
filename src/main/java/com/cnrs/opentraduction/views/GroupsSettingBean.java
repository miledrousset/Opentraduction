package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.models.GroupModel;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.InstanceService;
import com.cnrs.opentraduction.utils.MessageUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.util.CollectionUtils;

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
    private InstanceService instanceService;
    private InstancesSettingBean instancesSettingBean;

    private List<GroupModel> groups;
    private Groups groupSelected;

    private Integer idInstanceSelected;
    private String dialogTitle;


    public GroupsSettingBean(GroupService groupService,
                             InstanceService instanceService,
                             InstancesSettingBean instancesSettingBean) {

        this.groupService = groupService;
        this.instanceService = instanceService;
        this.instancesSettingBean = instancesSettingBean;
    }

    public void initialInterface() {
        groups = groupService.getAllGroups();
    }

    public void groupManager() {

        var instanceSelected = instancesSettingBean.getInstances().stream()
                .filter(instance -> instance.getId().intValue() == idInstanceSelected.intValue())
                .findFirst();
        if (instanceSelected.isPresent()) {
            var instance = instanceService.getInstanceById(instanceSelected.get().getId());
            groupSelected.setInstances(Set.of(instance));
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
        if (!CollectionUtils.isEmpty(instancesSettingBean.getInstances())) {
            idInstanceSelected = instancesSettingBean.getInstances().get(0).getId();
        }
        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void initialUpdateGroup(Groups groups) {

        dialogTitle = "Modifier le group " + groups.getName();
        groupSelected = groups;
        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }
}
