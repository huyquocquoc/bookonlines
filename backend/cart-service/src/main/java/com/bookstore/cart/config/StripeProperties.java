package com.bookstore.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Stripe configuration properties.
 */
@Data
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private String secretKey;

    private String successUrl;

    private String cancelUrl;

    private String webhookSecret;

    private String currency = "usd";
}

// Made with Bob
