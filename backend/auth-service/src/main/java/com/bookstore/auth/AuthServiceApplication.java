package com.bookstore.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Auth Service
 */
@SpringBootApplication(scanBasePackages = {"com.bookstore.auth", "com.bookstore.common"})
@EnableJpaRepositories(basePackages = "com.bookstore.auth.repository")
@EntityScan(basePackages = "com.bookstore.auth.entity")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

// Made with Bob