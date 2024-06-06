package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.config.LocaleBean;
import com.cnrs.opentraduction.utils.MessageUtil;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.MenuItem;
import com.cnrs.opentraduction.services.UserService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.MessageSource;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;


@Data
@Slf4j
@SessionScoped
@Named(value = "applicationBean")
public class ApplicationBean implements Serializable {

    @Inject
    private ApplicationSettingBean applicationSettingBean;

    @Inject
    private MessageSource messageSource;

    @Inject
    private LocaleBean localeBean;

    @Inject
    private UserService userService;

    private MenuItem menuItemSelected;
    private boolean connected;
    private Users userConnected;

    private String login, password;


    public void logout() {

        connected = false;
        userConnected = null;
        menuItemSelected = MenuItem.HOME;
    }

    public void login() {

        log.info("Début de l'authentification");
        try {
            userConnected = userService.authentification(login, password);

            connected = true;
            menuItemSelected = MenuItem.HOME;

            PrimeFaces.current().executeScript("PF('login').hide();");
            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Utilisateur connecté avec sucée !");
            log.info("Authentification terminé avec sucée de {}", userConnected.getLogin());
        } catch (Exception ex) {
            login = "";
            password = "";
            log.error("Erreur pendant l'authentification : " + ex.getMessage());
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage());
        }

    }

    public String getUserNameConnected() {
        var label = connected ? " " + userConnected.getFullName() : "";
        return messageSource.getMessage("application.home.welcome", null, localeBean.getCurrentLocale()) + label;
    }

    public String getMenuItemClass(String menuItem) {
        return (menuItem.equals(menuItemSelected.name())) ? "active" : "";
    }

    public void navigateToPage(String menuItem) throws IOException {
        menuItemSelected = MenuItem.valueOf(menuItem);
        switch(menuItemSelected) {
            case SEARCH:
                FacesContext.getCurrentInstance().getExternalContext().redirect("search.xhtml");
                break;
            case USER_SETTINGS:
                FacesContext.getCurrentInstance().getExternalContext().redirect("user-settings.xhtml");
                break;
            case SYSTEM_SETTINGS:
                applicationSettingBean.initialInterface();
                FacesContext.getCurrentInstance().getExternalContext().redirect("admin-settings.xhtml");
                break;
            case CONTACT_US:
                FacesContext.getCurrentInstance().getExternalContext().redirect("contact-us.xhtml");
                break;
            default:
        }
    }

}
