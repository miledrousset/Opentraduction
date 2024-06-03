package com.cnrs.opentraduction.config;

import lombok.Data;
import org.springframework.stereotype.Component;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;


@Data
@Component
@ApplicationScoped
@Named(value = "localeBean")
public class LocaleBean implements Serializable {

    private Locale currentLocale;

    public void changeLanguage(String language) {
        var context = FacesContext.getCurrentInstance();
        currentLocale = new Locale(language);
        context.getViewRoot().setLocale(currentLocale);
    }

    public String getLanguageSelected() {
        if (currentLocale != null) {
            return currentLocale.getLanguage();
        }
        return "fr";
    }
}
