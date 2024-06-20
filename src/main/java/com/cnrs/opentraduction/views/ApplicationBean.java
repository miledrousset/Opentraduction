package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.config.LocaleManagement;
import com.cnrs.opentraduction.models.ConnexionModel;
import com.cnrs.opentraduction.utils.MessageUtil;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.MenuItem;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.views.settings.ApplicationSettingBean;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.MessageSource;
import org.springframework.util.ObjectUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;


@Data
@Slf4j
@SessionScoped
@Named(value = "applicationBean")
public class ApplicationBean implements Serializable {

    private final ApplicationSettingBean applicationSettingBean;
    private final UserSettingsBean userSettingsBean;
    private final MessageSource messageSource;
    private final LocaleManagement localeManagement;
    private final UserService userService;

    private MenuItem menuItemSelected;
    private ConnexionModel connexionModel = new ConnexionModel();
    private boolean connected;
    private Users userConnected;


    public void logout() {

        var userName = userConnected.getFullName();
        connexionModel = new ConnexionModel();
        connected = false;
        userConnected = null;
        menuItemSelected = MenuItem.HOME;

        MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Déconnexion effectuée avec sucée !");
        log.info("Déconnexion effectué avec sucée de {}", userName);
    }

    public void login() {

        log.info("Début de l'authentification");
        userConnected = userService.authentification(connexionModel);
        if (!ObjectUtils.isEmpty(userConnected)) {
            connected = true;
            menuItemSelected = MenuItem.HOME;

            PrimeFaces.current().executeScript("PF('login').hide();");

            MessageUtil.showMessage(FacesMessage.SEVERITY_INFO, "Utilisateur connecté avec sucée !");

            log.info("Authentification terminé avec sucée de {}", userConnected.getLogin());
        } else {
            log.error("Erreur pendant l'authentification !");
        }
    }

    public String getUserNameConnected() {
        var label = connected ? " " + userConnected.getFullName() : "";
        return messageSource.getMessage("application.home.welcome", null, localeManagement.getCurrentLocale()) + label;
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
                userSettingsBean.initialInterface(userConnected);
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

    public void openWebPage(String logoCode) {
        String urlToOpen;
        switch(logoCode) {
            case "LOGO_1":
                urlToOpen = "https://tn.ambafrance.org/";
                break;
            case "LOGO_2":
                urlToOpen = "https://www.institutfrancais-tunisie.com/#/";
                break;
            case "LOGO_3":
                urlToOpen = "https://www.cnrs.fr/fr";
                break;
            case "LOGO_4":
                urlToOpen = "http://majlis-remomm.fr/";
                break;
            case "LOGO_5":
                urlToOpen = "https://www.mom.fr/";
                break;
            case "LOGO_6":
                urlToOpen = "https://www.huma-num.fr/";
                break;
            case "LOGO_7":
                urlToOpen = "https://opentheso.huma-num.fr/opentheso/";
                break;
            case "LOGO_8":
                urlToOpen = "https://www.cjb.ma/";
                break;
            case "LOGO_9":
                urlToOpen = "https://irmcmaghreb.org/";
                break;
            case "LOGO_10":
                urlToOpen = "https://www.ifao.egnet.net/";
                break;
            default:
                urlToOpen = "https://www.ifporient.org/";
        }
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(urlToOpen);
        } catch (IOException e) {
            // Gérer l'exception, par exemple en affichant un message d'erreur
            e.printStackTrace();
        }
    }

}
