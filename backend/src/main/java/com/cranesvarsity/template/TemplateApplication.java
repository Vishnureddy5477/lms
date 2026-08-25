package com.cranesvarsity.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Application entry point.
 * Run with:  mvn spring-boot:run
 * API base:  http://localhost:8181/api
 *
 * UserDetailsServiceAutoConfiguration is excluded because auth is entirely
 * JWT-based (see security/JwtAuthFilter) — without this, Spring Boot still
 * stands up an unused in-memory user with a random generated password and
 * logs it on every startup.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateApplication.class, args);
    }
}
