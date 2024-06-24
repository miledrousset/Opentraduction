package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.MessageService;

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

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;

    private GroupsSettingBean groupsSettingBean;

    private List<Users> users;
    private Users userSelected;

    private Integer idGroupSelected;
    private String dialogTitle;


    public void initialInterface() {

        userSelected = new Users();
        users = userService.getAllUsers();
    }

    public void initialAddUser() {

        userSelected = new Users();
        userSelected.setActive(true);
        dialogTitle = messageService.getMessage("user.settings.add.user");
        PrimeFaces.current().executeScript("PF('userDialog').show();");
    }

    public void initialUpdateUser(Users user) {

        userSelected = user;
        dialogTitle = messageService.getMessage("user.settings.update.title") + user.getFullName();
        PrimeFaces.current().executeScript("PF('userDialog').show();");
    }

    public void userManagement() {

        if (StringUtils.isEmpty(userSelected.getMail())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg1");
            return;
        }

        if (StringUtils.isEmpty(userSelected.getPassword())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg3");
            return;
        }

        if (StringUtils.isEmpty(userSelected.getLogin())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg5");
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

        messageService.showMessage(FacesMessage.SEVERITY_INFO, "user.settings.ok.msg2");
        PrimeFaces.current().executeScript("PF('userDialog').hide();");
        log.info("Utilisateur enregistré avec sucée !");
    }

    public void deleteUser(Users user) {

        userService.deleteUser(user);

        users = userService.getAllUsers();

        messageService.showMessage(FacesMessage.SEVERITY_INFO, "user.settings.ok.msg2");
        log.info("Utilisateur supprimé avec sucée !");
    }
}
