package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.entities.User;
import com.cnrs.opentraduction.models.SettingPart;
import com.cnrs.opentraduction.repositories.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@SessionScoped
@Named(value = "applicationSettingBean")
public class ApplicationSettingBean implements Serializable {

    @Autowired
    private UserRepository userRepository;

    private SettingPart selectedSetting = SettingPart.USER_MANAGEMENT;
    private List<User> users;


    public void initialInterface() {
        selectedSetting = SettingPart.USER_MANAGEMENT;

        //searchAllUsers
        /*
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
        users = List.of(user, user, user, user);*/
        users = (List<User>) userRepository.findAll();
    }

    public String getMenuItemClass(String settingItem) {
        return (settingItem.equals(selectedSetting.name())) ? "active" : "";
    }
}
