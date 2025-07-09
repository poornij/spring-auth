package com.devstaq.auth;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * The UserConfiguration class is a Spring Boot configuration class that provides configuration for the Devstaq Spring Boot User Framework
 * Library. This class is used to configure the user framework library, including enabling asynchronous processing and scheduling, and scanning for
 * components and repositories.
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
@EnableMethodSecurity
@ComponentScan(basePackages = "com.devstaq.auth")
@Import(UserAutoConfigurationRegistrar.class)
public class UserConfiguration {


    /**
     * Logs a message when the UserConfiguration class is loaded to indicate that the Devstaq Spring Boot User Framework Library has been
     * loaded.
     */
    @PostConstruct
    public void onStartup() {
        log.info("Devstaq SpringBoot User Framework Library loaded.");
    }

}
