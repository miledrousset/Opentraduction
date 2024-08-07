package com.cnrs.opentraduction.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        // Configure the AJP connector with a secret
        factory.addAdditionalTomcatConnectors(createAjpConnector());

        // Set the application port
        factory.setPort(8087);

        return factory;
    }

    private Connector createAjpConnector() {
        Connector connector = new Connector("AJP/1.3");
        AjpNioProtocol protocol = (AjpNioProtocol) connector.getProtocolHandler();
        protocol.setPort(9090); // AJP port
        protocol.setSecretRequired(false); // Enable secret requirement
        return connector;
    }
}
