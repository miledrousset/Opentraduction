package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.InstanceService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.MessageUtil;
import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.SettingPart;
import com.cnrs.opentraduction.repositories.GroupRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@Slf4j
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    private InstanceService instanceService;
    private InstancesSettingBean instancesSettingBean;

    private final UserService userService;
    private final GroupService groupService;

    private final GroupRepository groupRepository;

    private SettingPart selectedSetting;

    private List<Users> users;
    private List<Groups> groups;

    private Users userSelected;
    private Groups groupSelected;

    private Integer idGroupSelected;
    private String dialogTitle;


    public ApplicationSettingBean(GroupRepository groupRepository,
                                  UserService userService,
                                  GroupService groupService,
                                  InstanceService instanceService,
                                  InstancesSettingBean instancesSettingBean) {

        this.groupRepository = groupRepository;

        this.userService = userService;
        this.groupService = groupService;
        this.instanceService = instanceService;

        this.instancesSettingBean = instancesSettingBean;

        selectedSetting = SettingPart.USER_MANAGEMENT;
    }


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;
        userSelected = new Users();

        users = userService.getAllUsers();
        groups = groupRepository.findAll();

        instancesSettingBean.setInstances(instanceService.getAllInstances());
        instancesSettingBean.setThesaurusListStatut(false);
        instancesSettingBean.setCollectionsListStatut(false);
    }


    public void initialAddUser() {

        userSelected = new Users();
        userSelected.setActive(true);
        dialogTitle = "Ajouter un nouveau utilisateur";
        PrimeFaces.current().executeScript("PF('userDialog').show();");
    }

    public void initialUpdateUser(Users user) {

        userSelected = user;
        dialogTitle = "Modifier l'utilisateur " + user.getFullName();
        PrimeFaces.current().executeScript("PF('userDialog').show();");
    }

    public void userManagement() {

        var groupSelected = groups.stream().filter(group -> group.getId().intValue() == idGroupSelected.intValue()).findFirst();
        if (groupSelected.isPresent()) {
            userSelected.setGroup(groupSelected.get());
        }

        userService.saveUser(userSelected);

        users = userService.getAllUsers();

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Utilisateur enregistré avec succès");
        PrimeFaces.current().executeScript("PF('userDialog').hide();");
        log.info("Utilisateur enregistré avec sucée !");
    }

    public void deleteUser(Users user) {

        userService.deleteUser(user);

        users = userService.getAllUsers();

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Utilisateur enregistré avec succès");
        log.info("Utilisateur supprimé avec sucée !");

    }





    public void initialAddingGroup() {

        groupSelected = new Groups();

        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public void initialUpdateGroup(Groups groups) {

        groupSelected = groups;
        PrimeFaces.current().executeScript("PF('groupDialog').show();");
    }

    public String getMenuItemClass(String settingItem) {
        return (settingItem.equals(selectedSetting.name())) ? "active" : "";
    }

    public void setMenuItem(String settingItem) {
        if (!settingItem.equals(selectedSetting.name())) {
            selectedSetting = SettingPart.valueOf(settingItem);
        }
    }
}
