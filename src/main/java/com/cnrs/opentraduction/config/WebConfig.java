package com.cnrs.opentraduction.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<SessionTimeoutFilter> sessionTimeoutFilter() {
        FilterRegistrationBean<SessionTimeoutFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SessionTimeoutFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
