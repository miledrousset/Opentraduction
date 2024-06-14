package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.models.SettingPart;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;


@Data
@Slf4j
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    private InstancesSettingBean instancesSettingBean;
    private GroupsSettingBean groupsSettingBean;
    private UsersSettingBean usersSettingBean;

    private SettingPart selectedSetting;


    public ApplicationSettingBean(UsersSettingBean usersSettingBean,
                                  GroupsSettingBean groupsSettingBean,
                                  InstancesSettingBean instancesSettingBean) {

        this.usersSettingBean = usersSettingBean;
        this.groupsSettingBean = groupsSettingBean;
        this.instancesSettingBean = instancesSettingBean;

        selectedSetting = SettingPart.USER_MANAGEMENT;
    }


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;

        usersSettingBean.initialInterface();
        instancesSettingBean.initialInterface();
        groupsSettingBean.initialInterface();
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
