package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.models.dao.ConsultationInstanceDao;
import com.cnrs.opentraduction.models.dao.GroupDao;
import com.cnrs.opentraduction.models.dao.ReferenceInstanceDao;
import com.cnrs.opentraduction.services.ConsultationInstanceService;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.ReferenceInstanceService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
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
@Named(value = "groupsSettingBean")
public class GroupsSettingBean implements Serializable {

    private final GroupService groupService;
    private final MessageService messageService;
    private final ReferenceInstanceService referenceInstanceService;
    private final ConsultationInstanceService consultationInstanceService;

    private List<GroupDao> groups;
    private GroupDao groupSelected;

    private String dialogTitle, groupName;

    private List<ReferenceInstanceDao> referenceProjects;
    private ReferenceInstanceDao referenceProjectSelected;
    private String referenceProjectNameSelected;

    private List<ConsultationInstanceDao> consultationProjects;
    private List<ConsultationInstanceDao> consultationProjectsSelected;
    private List<String> consultationProjectsNamesSelected;


    public void initialInterface() {

        groups = groupService.getAllGroups();

        referenceProjects = referenceInstanceService.getAllInstances();
        consultationProjects = consultationInstanceService.getAllConsultationInstances();
    }

    public void onConsultationProjectSelected() {
        consultationProjectsSelected = new ArrayList<>();
        if (!CollectionUtils.isEmpty(consultationProjectsNamesSelected)) {
            consultationProjectsNamesSelected.forEach(projectName -> {
                var collection = consultationProjects.stream()
                        .filter(element -> projectName.equals(element.getName()))
                        .findFirst();
                collection.ifPresent(consultationInstanceDao -> consultationProjectsSelected.add(consultationInstanceDao));
            });
        }
    }

    public void setReferenceThesaurus() {
        if (!StringUtils.isEmpty(referenceProjectNameSelected)) {
            referenceProjectSelected = referenceProjects.stream()
                    .filter(element -> element.getName().equals(referenceProjectNameSelected))
                    .findFirst()
                    .get();
        }
    }

    public void groupManager() {

        if (StringUtils.isEmpty(groupSelected.getName())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.group.error.msg3");
            return;
        }

        if (ObjectUtils.isEmpty(referenceProjectSelected)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.group.error.msg2");
            return;
        }

        groupService.saveGroup(groupSelected, referenceProjectSelected, consultationProjectsSelected);

        groups = groupService.getAllGroups();

        messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.group.ok.msg2");
        PrimeFaces.current().executeScript("PF('groupDialog').hide();");
        log.info("Group enregistré avec sucée !");
    }

    public void initialAddingGroup() {

        dialogTitle = messageService.getMessage("application.group.add.title");

        groupSelected = new GroupDao();

        if (!CollectionUtils.isEmpty(referenceProjects)) {
            referenceProjectNameSelected = referenceProjects.get(0).getName();
            referenceProjectSelected = referenceProjects.get(0);
        }

        consultationProjectsNamesSelected = new ArrayList<>();
        consultationProjectsSelected = new ArrayList<>();

        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void initialUpdateGroup(GroupDao groups) {

        dialogTitle = messageService.getMessage("application.group.update.title") + groups.getName();

        groupSelected = groups;

        referenceProjectSelected = referenceProjects.stream()
                .filter(element -> element.getName().equals(groupSelected.getReferenceProject().getName()))
                .findFirst()
                .get();
        referenceProjectNameSelected = referenceProjectSelected.getName();

        var consultationNames = consultationProjects.stream().map(ConsultationInstanceDao::getName).collect(Collectors.toList());
        consultationProjectsSelected = groupSelected.getConsultationProjectsList().stream()
                .filter(element -> consultationNames.stream().anyMatch(consultantProject -> consultantProject.equals(element.getName())))
                .collect(Collectors.toList());
        consultationProjectsNamesSelected = consultationProjectsSelected.stream()
                .map(ConsultationInstanceDao::getName)
                .collect(Collectors.toList());

        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void deleteGroup(GroupDao group) {

        if (!ObjectUtils.isEmpty(group)) {
            groupService.deleteGroup(group.getId());
            groups = groupService.getAllGroups();
            messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.group.ok.msg1");
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.group.error.msg1");
        }
    }
}
