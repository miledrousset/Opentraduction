package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.models.dao.ConnexionDto;
import com.cnrs.opentraduction.utils.MessageService;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.MenuItem;
import com.cnrs.opentraduction.services.UserService;
import com.cnrs.opentraduction.views.search.SearchBean;
import com.cnrs.opentraduction.views.settings.ApplicationSettingBean;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
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
    private final SearchBean searchBean;
    private final MessageService messageService;
    private final UserService userService;

    private MenuItem menuItemSelected;
    private ConnexionDto connexionModel = new ConnexionDto();
    private boolean connected;
    private Users userConnected;


    public void logout() {

        var userName = userConnected.getFullName();
        connexionModel = new ConnexionDto();
        connected = false;
        userConnected = null;
        menuItemSelected = MenuItem.HOME;

        messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.user.error.msg1");
        log.info("Déconnexion effectué avec sucée de {}", userName);
    }

    public void login() {

        log.info("Début de l'authentification");
        userConnected = userService.authentification(connexionModel);
        if (!ObjectUtils.isEmpty(userConnected)) {
            connected = true;
            menuItemSelected = MenuItem.HOME;

            PrimeFaces.current().executeScript("PF('login').hide();");

            searchBean.setUserConnected(userConnected);

            messageService.showMessage(FacesMessage.SEVERITY_INFO, "application.user.ok.msg1");

            log.info("Authentification terminé avec sucée de {}", userConnected.getLogin());
        } else {
            log.error("Erreur pendant l'authentification !");
        }
    }

    public String getUserNameConnected() {
        var label = connected ? " " + userConnected.getFullName() : "";
        return messageService.getMessage("application.home.welcome") + label;
    }

    public String getMenuItemClass(String menuItem) {
        return (menuItem.equals(menuItemSelected.name())) ? "active" : "";
    }

    public void navigateToPage(String menuItem) throws IOException {
        menuItemSelected = MenuItem.valueOf(menuItem);
        switch(menuItemSelected) {
            case SEARCH:
                log.info("Navigation vers l'interface de recherche");
                searchBean.initSearchInterface();
                FacesContext.getCurrentInstance().getExternalContext().redirect("search.xhtml");
                break;
            case USER_SETTINGS:
                log.info("Navigation vers l'interface paramètres de l'utilisateur");
                userSettingsBean.initialInterface(userConnected);
                FacesContext.getCurrentInstance().getExternalContext().redirect("user-settings.xhtml");
                break;
            case SYSTEM_SETTINGS:
                log.info("Navigation vers l'interface paramètres du systhème");
                applicationSettingBean.initialInterface();
                FacesContext.getCurrentInstance().getExternalContext().redirect("admin-settings.xhtml");
                break;
            default:
                log.info("Navigation vers l'interface principale");
                searchBean.setTermValue("");
                FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        }
    }

    public void openWebPage(String logoCode) throws IOException {
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
        FacesContext.getCurrentInstance().getExternalContext().redirect(urlToOpen);
    }

}
