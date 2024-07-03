package com.cnrs.opentraduction.config;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;


@Data
@Component
@ApplicationScoped
@Named(value = "localeBean")
public class LocaleManagement implements Serializable {

    private Locale currentLocale;

    public void changeLanguage(String language) {
        var context = FacesContext.getCurrentInstance();
        currentLocale = new Locale(language);
        context.getViewRoot().setLocale(currentLocale);
    }

    public String getLanguageImg() {
        var url = "/assets/img/flags/";
        if (!ObjectUtils.isEmpty(currentLocale)) {
            return "ar".equals(currentLocale.getLanguage()) ? url + "ar.png" : url + "fr.png";
        }
        return url + "fr.png";
    }
}
