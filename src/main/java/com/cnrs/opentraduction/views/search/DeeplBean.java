package com.cnrs.opentraduction.views.search;

import com.cnrs.opentraduction.clients.DeeplClient;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import java.io.Serializable;


@Slf4j
@Data
@SessionScoped
@Named(value = "deeplBean")
public class DeeplBean implements Serializable {

    private final MessageService messageService;
    private final DeeplClient deeplService;

    private String termToTranslate, termTranslated;
    private String definitionToTranslate, definitionTranslated;


    public void initInterface() {
        termToTranslate = "";
        termTranslated = "";
        definitionToTranslate = "";
        definitionTranslated = "";
    }

    public void translateTerm(boolean toArabic) {
        if (StringUtils.isEmpty(termToTranslate)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.deepl.error.msg1");
            termTranslated = "";
            return;
        }

        termTranslated = messageService.getMessage("application.deepl.term")
                + getLangCible(toArabic)
                + deeplService.translate(termToTranslate, getFromLang(toArabic), getToLang(toArabic));
    }

    public void translateDefinition(boolean toArabic) {
        if (StringUtils.isEmpty(definitionToTranslate)) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.deepl.error.msg2");
            definitionTranslated = "";
            return;
        }

        definitionTranslated = messageService.getMessage("application.deepl.definition")
                + getLangCible(toArabic)
                + deeplService.translate(definitionToTranslate, getFromLang(toArabic), getToLang(toArabic));
    }

    private String getLangCible(boolean toArabic) {
        return toArabic ? " (FR) : " : " (AR) : ";
    }

    private String getFromLang(boolean toArabic) {
        return toArabic ? "fr" : "ar";
    }

    private String getToLang(boolean toArabic) {
        return toArabic ? "ar" : "fr";
    }
}
