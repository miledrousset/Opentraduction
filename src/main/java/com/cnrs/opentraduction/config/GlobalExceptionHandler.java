package com.cnrs.opentraduction.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import javax.faces.application.ViewExpiredException;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ViewExpiredException.class)
    public ModelAndView handleViewExpiredException(ViewExpiredException ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/index.xhtml");
        return modelAndView;
    }

}
