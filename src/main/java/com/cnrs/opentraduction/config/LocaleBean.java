package com.cnrs.opentraduction.config;

import org.springframework.stereotype.Component;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;


@Component
public class LocaleBean implements Serializable {

    public void changeLanguage(String language) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(new Locale(language));
    }
}
