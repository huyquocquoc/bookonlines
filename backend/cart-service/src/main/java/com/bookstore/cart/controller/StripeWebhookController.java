package com.bookstore.cart.controller;

import com.bookstore.cart.service.StripeCheckoutService;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Stripe webhooks.
 */
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripeCheckoutService stripeCheckoutService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature) {
        Event event = stripeCheckoutService.parseWebhookEvent(payload, stripeSignature);
        stripeCheckoutService.handleWebhookEvent(event);
        log.info("Processed Stripe webhook event {}", event.getType());
        return ResponseEntity.ok("ok");
    }
}

// Made with Bob
