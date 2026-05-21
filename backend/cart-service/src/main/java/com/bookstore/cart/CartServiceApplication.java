package com.bookstore.cart;

import com.bookstore.cart.config.StripeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Cart Service
 */
@SpringBootApplication(scanBasePackages = {"com.bookstore.cart", "com.bookstore.common"})
@EnableJpaRepositories(basePackages = "com.bookstore.cart.repository")
@EntityScan(basePackages = "com.bookstore.cart.entity")
@EnableConfigurationProperties(StripeProperties.class)
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}

// Made with Bob
