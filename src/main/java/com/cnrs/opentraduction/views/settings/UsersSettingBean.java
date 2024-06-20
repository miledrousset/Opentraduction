package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.MessageUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.util.StringUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@Slf4j
@SessionScoped
@Named(value = "usersSettingBean")
public class UsersSettingBean implements Serializable {

    private UserService userService;
    private GroupService groupService;

    private GroupsSettingBean groupsSettingBean;

    private List<Users> users;
    private Users userSelected;

    private Integer idGroupSelected;
    private String dialogTitle;


    public UsersSettingBean(UserService userService,
                            GroupService groupService,
                            GroupsSettingBean groupsSettingBean) {

        this.userService = userService;
        this.groupService = groupService;
        this.groupsSettingBean = groupsSettingBean;
    }

    public void initialInterface() {

        userSelected = new Users();
        users = userService.getAllUsers();
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

        if (StringUtils.isEmpty(userSelected.getMail())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le mail est obligatoire !");
            return;
        }

        if (StringUtils.isEmpty(userSelected.getPassword())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le mot de passe est obligatoire !");
            return;
        }

        if (StringUtils.isEmpty(userSelected.getLogin())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le nom d'utilisateur est obligatoire !");
            return;
        }

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
}
