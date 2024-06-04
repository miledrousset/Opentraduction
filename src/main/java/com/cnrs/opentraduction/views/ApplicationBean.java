package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.config.LocaleBean;
import com.cnrs.opentraduction.entities.User;
import com.cnrs.opentraduction.models.MenuItem;
import com.cnrs.opentraduction.services.UserService;

import lombok.Data;
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
    private User userConnected;


    public void logout() {

        connected = false;
        menuItemSelected = MenuItem.HOME;
    }

    public void login() {

        connected = true;
        menuItemSelected = MenuItem.HOME;

        /*try {
            userConnected = userService.authentification("admin", "admin");
            PrimeFaces.current().executeScript("PF('login').hide();");
        } catch (Exception ex) {
            showMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage());
        }*/
        userConnected = User.builder().firstName("Firas").lastName("GABSI").admin(true).build();

    }

    public String getUserNameConnected() {

        return messageSource.getMessage("application.home.welcome", null, localeBean.getCurrentLocale()) + (connected ? " Firas GABSI" : "");
    }

    public String getMenuItemClass(String menuItem) {
        return (menuItem.equals(menuItemSelected.name())) ? "active" : "";
    }

    public void navigateToPage(String menuItem) throws IOException {
        if (!menuItem.equals(menuItemSelected)) {
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

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("message");
    }

}
