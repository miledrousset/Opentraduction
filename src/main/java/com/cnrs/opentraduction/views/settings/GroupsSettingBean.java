package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.models.GroupModel;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.ConsultationInstanceService;
import com.cnrs.opentraduction.utils.MessageService;

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

    private final GroupService groupService;
    private final MessageService messageService;
    private final ConsultationInstanceService consultationInstanceService;
    private final ConsultationInstancesSettingBean consultationInstancesBean;

    private List<GroupModel> groups;
    private Groups groupSelected;

    private Integer idInstanceSelected;
    private String dialogTitle;


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

        messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.group.ok.msg2");
        PrimeFaces.current().executeScript("PF('groupDialog').hide();");
        log.info("Group enregistré avec sucée !");
    }

    public void initialAddingGroup() {

        dialogTitle = messageService.getMessage("application.group.add.title");
        groupSelected = new Groups();
        if (!CollectionUtils.isEmpty(consultationInstancesBean.getConsultationList())) {
            idInstanceSelected = consultationInstancesBean.getConsultationList().get(0).getId();
        }
        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void initialUpdateGroup(Groups groups) {

        dialogTitle = messageService.getMessage("application.group.update.title") + groups.getName();
        groupSelected = groups;
        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void deleteGroup(GroupModel group) {

        if (!ObjectUtils.isEmpty(group)) {
            groupService.deleteGroup(group.getId());
            groups = groupService.getAllGroups();
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.group.ok.msg1");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.group.error.msg1");
        }
    }
}
