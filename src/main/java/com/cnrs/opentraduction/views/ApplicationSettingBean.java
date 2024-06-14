package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.MessageUtil;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.SettingPart;

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

    private InstancesSettingBean instancesSettingBean;
    private GroupsSettingBean groupsSettingBean;

    private SettingPart selectedSetting;

    private List<Users> users;
    private Users userSelected;
    private UserService userService;

    private Integer idGroupSelected;
    private GroupService groupService;
    private String dialogTitle;


    public ApplicationSettingBean(UserService userService,
                                  GroupService groupService,
                                  GroupsSettingBean groupsSettingBean,
                                  InstancesSettingBean instancesSettingBean) {

        this.userService = userService;
        this.groupService = groupService;
        this.groupsSettingBean = groupsSettingBean;
        this.instancesSettingBean = instancesSettingBean;

        selectedSetting = SettingPart.USER_MANAGEMENT;
    }


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;
        userSelected = new Users();

        users = userService.getAllUsers();

        instancesSettingBean.initialInterface();
        groupsSettingBean.initialInterface();
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

        var groupSelected = groupsSettingBean.getGroups().stream()
                .filter(group -> group.getId().intValue() == idGroupSelected.intValue())
                .findFirst();
        if (groupSelected.isPresent()) {
            var group = groupService.getGroupById(groupSelected.get().getId());
            userSelected.setGroup(group);
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


    public String getMenuItemClass(String settingItem) {
        return (settingItem.equals(selectedSetting.name())) ? "active" : "";
    }

    public void setMenuItem(String settingItem) {
        if (!settingItem.equals(selectedSetting.name())) {
            selectedSetting = SettingPart.valueOf(settingItem);
        }
    }
}
