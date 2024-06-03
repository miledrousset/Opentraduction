package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.Group;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.User;
import com.cnrs.opentraduction.models.SettingPart;
import lombok.Data;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Data
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    private SettingPart selectedSetting = SettingPart.USER_MANAGEMENT;
    private List<User> users;


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;

        //searchAllUsers
        Group group = Group.builder().name("Tunis GROUP").build();
        Thesaurus thesaurus = Thesaurus.builder().name("Pactole").build();
        User user = User.builder()
                .id(1)
                .firstName("Firas")
                .lastName("GABSI")
                .login("TEST LOGIN")
                .password("aaaaa")
                .admin(true)
                .mail("firas.gabsi@gmail.com")
                .group(group)
                .thesauruses(Set.of(thesaurus))
                .created(LocalDateTime.now())
                .modified(LocalDateTime.now())
                .build();
        users = List.of(user, user, user, user);
    }

    public String getMenuItemClass(String settingItem) {
        return (settingItem.equals(selectedSetting.name())) ? "active" : "";
    }
}
