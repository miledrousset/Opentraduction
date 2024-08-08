package com.cnrs.opentraduction.config;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import jakarta.servlet.ServletContext;


public class SpringUtils {

    public static ApplicationContext getApplicationContext(ServletContext servletContext) {
        return WebApplicationContextUtils.getWebApplicationContext(servletContext);
    }

    public static <T> T getBean(ServletContext servletContext, Class<T> beanClass) {
        return getApplicationContext(servletContext).getBean(beanClass);
    }
}

