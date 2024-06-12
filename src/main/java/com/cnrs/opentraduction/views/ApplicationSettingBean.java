package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.Instances;
import com.cnrs.opentraduction.repositories.InstanceRepository;
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
import org.primefaces.model.DualListModel;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    private final UserService userService;
    private final GroupService groupService;
    private final InstanceService instanceService;

    private final GroupRepository groupRepository;
    private final InstanceRepository instanceRepository;

    private SettingPart selectedSetting;

    private List<Users> users;
    private List<Groups> groups;
    private List<Instances> instances;

    private Users userSelected;
    private Groups groupSelected;
    private Instances instanceSelected;
    private List<Instances> instancesSelected;

    private DualListModel<Instances> instanceModel;
    private Integer idGroupSelected;
    private String dialogTitle;


    public ApplicationSettingBean(GroupRepository groupRepository,
                                  InstanceRepository instanceRepository,
                                  UserService userService,
                                  GroupService groupService,
                                  InstanceService instanceService) {

        this.groupRepository = groupRepository;
        this.instanceRepository = instanceRepository;

        this.userService = userService;
        this.groupService = groupService;
        this.instanceService = instanceService;

        selectedSetting = SettingPart.USER_MANAGEMENT;
    }


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;
        userSelected = new Users();

        users = userService.getAllUsers();
        groups = groupRepository.findAll();
        instances = instanceRepository.findAll();

        instancesSelected = new ArrayList<>();
        instanceModel = new DualListModel<>(instances, instancesSelected);
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

        var groupSelected = groups.stream().filter(group -> group.getId() == idGroupSelected).findFirst();
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




    public void initialAddInstanceDialog() {

        instanceSelected = new Instances();
        PrimeFaces.current().executeScript("PF('instanceDialog').show();");
    }

    public void initialUpdateInstanceDialog(Instances instance) {

        instanceSelected = instance;
        PrimeFaces.current().executeScript("PF('instanceDialog').show();");
    }

    public void instanceManagement() {

        instanceService.saveInstance(instanceSelected);

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Instance enregistrée avec succès");
        PrimeFaces.current().executeScript("PF('instanceDialog').show();");
        log.info("Instance enregistrée avec succès !");
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

    public void searchListThesaurus() {

    }
}
