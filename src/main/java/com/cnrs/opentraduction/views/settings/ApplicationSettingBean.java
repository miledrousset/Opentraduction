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

    private ConsultationInstancesSettingBean consultationInstancesBean;
    private GroupsSettingBean groupsSettingBean;
    private UsersSettingBean usersSettingBean;

    private SettingPart selectedSetting;


    public ApplicationSettingBean(UsersSettingBean usersSettingBean,
                                  GroupsSettingBean groupsSettingBean,
                                  ConsultationInstancesSettingBean consultationInstancesBean) {

        this.usersSettingBean = usersSettingBean;
        this.groupsSettingBean = groupsSettingBean;
        this.consultationInstancesBean = consultationInstancesBean;

        selectedSetting = SettingPart.USER_MANAGEMENT;
    }


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;

        usersSettingBean.initialInterface();
        consultationInstancesBean.initialInterface();
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
