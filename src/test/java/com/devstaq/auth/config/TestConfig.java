package com.devstaq.auth.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"com.devstaq.auth.service.**", "com.devstaq.auth.roles.**",
                "com.devstaq.auth.mail.**", "com.devstaq.auth.persistence.model.**"})
public class TestConfig {
}
