package com.cnrs.opentraduction.views.settings;

import com.cnrs.opentraduction.models.SettingPart;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;


@Data
@Slf4j
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    private final ConsultationSettingBean consultationBean;
    private final ReferenceSettingBean referenceBean;
    private final GroupsSettingBean groupsSettingBean;
    private final UsersSettingBean usersSettingBean;

    private SettingPart selectedSetting;
    private Integer userConnectedId;


    public void initialInterface(Integer userConnectedId) {
        this.userConnectedId = userConnectedId;
        selectedSetting = SettingPart.USER_MANAGEMENT;

        usersSettingBean.initialInterface(userConnectedId);
        groupsSettingBean.initialInterface();
        consultationBean.initialInterface();
        referenceBean.initialInterface();
    }

    public String getMenuItemClass(String settingItem) {
        return (settingItem.equals(selectedSetting.name())) ? "active" : "";
    }

    public void setMenuItem(String settingItem) {
        if (!settingItem.equals(selectedSetting.name())) {
            selectedSetting = SettingPart.valueOf(settingItem);
            switch (settingItem) {
                case "USER_MANAGEMENT":
                    usersSettingBean.initialInterface(userConnectedId);
                    break;
                case "GROUP_MANAGEMENT":
                    groupsSettingBean.initialInterface();
                    break;
                case "THESAURUS_REFERENCE":
                    referenceBean.initialInterface();
                    break;
                default:
                    consultationBean.initialInterface();
            }
        }
    }
}
