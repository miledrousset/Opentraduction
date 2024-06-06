package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.Instances;
import com.cnrs.opentraduction.models.User;
import com.cnrs.opentraduction.repositories.InstanceRepository;
import com.cnrs.opentraduction.services.GroupService;
import com.cnrs.opentraduction.services.InstanceService;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.utils.MessageUtil;
import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.SettingPart;
import com.cnrs.opentraduction.repositories.GroupRepository;
import com.cnrs.opentraduction.repositories.UserRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;

import javax.annotation.ManagedBean;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
@ManagedBean
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    private final UserService userService;
    private final GroupService groupService;
    private final InstanceService instanceService;

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final InstanceRepository instanceRepository;

    private SettingPart selectedSetting;

    private List<Users> users;
    private List<Groups> groups;
    private List<Instances> instances;

    private User userSelected;
    private Groups groupSelected;
    private Instances instanceSelected;
    private List<Instances> instancesSelected;

    private DualListModel<Instances> instanceModel;


    public ApplicationSettingBean(UserRepository userRepository,
                                  GroupRepository groupRepository,
                                  InstanceRepository instanceRepository,
                                  UserService userService,
                                  GroupService groupService,
                                  InstanceService instanceService) {

        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.instanceRepository = instanceRepository;

        this.userService = userService;
        this.groupService = groupService;
        this.instanceService = instanceService;

        selectedSetting = SettingPart.USER_MANAGEMENT;
    }


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;
        userSelected = new User();

        users = userRepository.findAll();
        groups = groupRepository.findAll();
        instances = instanceRepository.findAll();

        instancesSelected = new ArrayList<>();
        instanceModel = new DualListModel<>(instances, instancesSelected);
    }

    public void initialAddUserDialog() {

        userSelected = User.builder().active(true).build();
        PrimeFaces.current().executeScript("PF('userDialog').show();");
    }

    public void initialUpdateUserDialog(User user) {

        userSelected = user;
        PrimeFaces.current().executeScript("PF('userDialog').show();");
    }

    public void userManagement() {

        userService.saveUser(userSelected);

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Utilisateur enregistré avec succès");
        PrimeFaces.current().executeScript("PF('userDialog').show();");
        log.info("Utilisateur enregistré avec sucée !");
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

        instancesSelected = new ArrayList<>();
        instanceModel = new DualListModel<>(instances, instancesSelected);

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
