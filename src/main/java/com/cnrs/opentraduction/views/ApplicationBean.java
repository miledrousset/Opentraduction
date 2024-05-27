package com.cnrs.opentraduction.views;

import com.cnrs.opentraduction.models.MenuItem;

import lombok.Data;
import org.primefaces.PrimeFaces;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;


@Data
@SessionScoped
@Named(value = "applicationBean")
public class ApplicationBean implements Serializable {

    private MenuItem menuItemSelected;
    private boolean connected;


    public void logout() {

        connected = false;
        menuItemSelected = MenuItem.HOME;
    }

    public void login() {

        connected = true;
        menuItemSelected = MenuItem.HOME;
        PrimeFaces.current().executeScript("PF('login').hide();");
    }

    public String getUserNameConnected() {
        return "Bienvenu" + (connected ? " Firas GABSI" : "");
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
                    FacesContext.getCurrentInstance().getExternalContext().redirect("admin-settings.xhtml");
                    break;
                case CONTACT_US:
                    FacesContext.getCurrentInstance().getExternalContext().redirect("contact-us.xhtml");
                    break;
                default:
            }
        }
    }
}
