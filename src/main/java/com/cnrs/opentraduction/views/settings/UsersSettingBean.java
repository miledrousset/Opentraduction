package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.util.StringUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
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

    private List<Groups> groups;
    private List<Users> users;
    private Users userSelected;

    private Integer idGroupSelected;
    private String dialogTitle, defaultTraduction, francaisLabel, arabeLabel;


    public void initialInterface() {

        log.info("Initialisation de l'interface gestion des utilisateur");
        userSelected = new Users();
        users = userService.getAllUsers();
        groups = groupService.getGroups();

        francaisLabel = messageService.getMessage("application.language.french");
        arabeLabel = messageService.getMessage("application.language.arabic");
    }

    public void initialAddUser() {

        log.info("Initialiser la boite de dialog pour l'ajout d'un nouvel utilisateur");
        userSelected = new Users();
        userSelected.setActive(true);
        dialogTitle = messageService.getMessage("user.settings.add.user");
        PrimeFaces.current().executeScript("PF('userDialog').show();");
    }

    public void initialUpdateUser(Users user) {

        log.info("Initialiser la boite de dialog pour la modification de l'utilisateur " + user.getFullName());

        userSelected = user;
        idGroupSelected = userSelected.getGroup().getId();

        dialogTitle = messageService.getMessage("application.user.update.title") + user.getFullName();
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

        var groupSelected = groups.stream()
                .filter(group -> group.getId().intValue() == idGroupSelected.intValue())
                .findFirst();
        if (groupSelected.isPresent()) {
            var group = groupService.getGroupById(groupSelected.get().getId());
            userSelected.setGroup(group);
        }

        if (userService.saveUser(userSelected)) {

            log.info("Enregistrement de l'utilisateur effectué, actualisation du tableau des utilisateurs !");
            users = userService.getAllUsers();

            messageService.showMessage(FacesMessage.SEVERITY_INFO, "user.settings.ok.msg2");
            PrimeFaces.current().executeScript("PF('userDialog').hide();");
            log.info("Utilisateur enregistré avec sucée !");
        }
    }

    public void deleteUser(Users user) {

        log.info("Début de suppression de l'utilisateur {}", user.getFullName());
        userService.deleteUser(user);

        users = userService.getAllUsers();

        messageService.showMessage(FacesMessage.SEVERITY_INFO, "user.settings.ok.msg3");
        log.info("L'utilisateur a été supprimé !");
    }
}
