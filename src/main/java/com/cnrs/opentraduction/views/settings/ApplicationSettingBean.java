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

    private final ConsultationInstancesSettingBean consultationInstancesBean;
    private final ReferenceInstancesSettingBean referenceInstancesBean;
    private final GroupsSettingBean groupsSettingBean;
    private final UsersSettingBean usersSettingBean;

    private SettingPart selectedSetting;


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;

        usersSettingBean.initialInterface();
        groupsSettingBean.initialInterface();
        consultationInstancesBean.initialInterface();
        referenceInstancesBean.initialInterface();
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
