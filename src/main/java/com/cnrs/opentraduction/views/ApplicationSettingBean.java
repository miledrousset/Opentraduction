package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.models.SettingPart;
import lombok.Data;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;


@Data
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    private SettingPart selectedSetting = SettingPart.USER_MANAGEMENT;


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;

        //searchAllUsers
    }

    public String getMenuItemClass(String settingItem) {
        return (settingItem.equals(selectedSetting.name())) ? "active" : "";
    }
}
