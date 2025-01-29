package com.cnrs.opentraduction.config;

import com.cnrs.opentraduction.models.MenuItem;
import com.cnrs.opentraduction.models.dao.ConnexionDao;
import com.cnrs.opentraduction.views.ApplicationBean;

import jakarta.enterprise.context.SessionScoped;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;


@SessionScoped
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserController implements Serializable {

    private final ApplicationBean applicationBean;

    @PostMapping("/logout")
    public void sayHello() {

        applicationBean.setConnexionModel(new ConnexionDao());
        applicationBean.setConnected(false);
        applicationBean.setUserConnected(null);
        applicationBean.setMenuItemSelected(MenuItem.HOME);
    }
}
