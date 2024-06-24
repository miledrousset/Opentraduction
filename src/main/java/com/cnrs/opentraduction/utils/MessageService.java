package com.cnrs.opentraduction.utils;

import com.cnrs.opentraduction.config.LocaleManagement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;


@Data
@Slf4j
@Service
public class MessageService {

    private final MessageSource messageSource;
    private final LocaleManagement localeManagement;


    public void showMessage(FacesMessage.Severity severity, String codeMessage) {

        var msg = new FacesMessage(severity, "", getMessage(codeMessage));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("message");
    }

    public String getMessage(String codeMessage) {

        return messageSource.getMessage(codeMessage, null, localeManagement.getCurrentLocale());
    }
}
