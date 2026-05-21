package com.bookstore.cart.service;

import com.bookstore.cart.config.StripeProperties;
import com.bookstore.common.dto.CartDTO;
import com.bookstore.common.dto.CartItemDTO;
import com.bookstore.common.dto.CheckoutSessionRequest;
import com.bookstore.common.dto.CheckoutSessionResponse;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for Stripe Checkout integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutService {

    private final StripeProperties stripeProperties;
    private final CartService cartService;

    public CheckoutSessionResponse createCheckoutSession(String sessionId,
                                                         CartDTO cart,
                                                         CheckoutSessionRequest request) {
        if (!StringUtils.hasText(stripeProperties.getSecretKey())) {
            throw new RuntimeException("Stripe secret key is not configured");
        }
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create checkout session for an empty cart");
        }

        Stripe.apiKey = stripeProperties.getSecretKey();

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(sessionId)
                .setSuccessUrl(resolveSuccessUrl(request))
                .setCancelUrl(resolveCancelUrl(request))
                .putMetadata("cartSessionId", sessionId)
                .putMetadata("totalItems", String.valueOf(cart.getTotalItems()))
                .putMetadata("totalAmount", String.valueOf(cart.getTotalAmount()));

        for (CartItemDTO item : cart.getItems()) {
            builder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(item.getQuantity().longValue())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(stripeProperties.getCurrency())
                                            .setUnitAmount(toMinorUnits(item.getBookPrice()))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getBookTitle())
                                                            .setDescription(buildDescription(item))
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        try {
            Session session = Session.create(builder.build());
            log.info("Created Stripe checkout session {} for cart session {}", session.getId(), sessionId);
            return CheckoutSessionResponse.builder()
                    .sessionId(session.getId())
                    .checkoutUrl(session.getUrl())
                    .build();
        } catch (StripeException ex) {
            log.error("Failed to create Stripe checkout session for cart session {}: {}", sessionId, ex.getMessage());
            throw new RuntimeException("Failed to create Stripe checkout session", ex);
        }
    }

    public Event parseWebhookEvent(String payload, String stripeSignature) {
        if (!StringUtils.hasText(stripeProperties.getWebhookSecret())) {
            throw new RuntimeException("Stripe webhook secret is not configured");
        }
        if (!StringUtils.hasText(stripeSignature)) {
            throw new RuntimeException("Missing Stripe-Signature header");
        }

        try {
            return Webhook.constructEvent(payload, stripeSignature, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            log.error("Stripe webhook signature verification failed: {}", ex.getMessage());
            throw new RuntimeException("Invalid Stripe webhook signature", ex);
        }
    }

    public void handleWebhookEvent(Event event) {
        if (!"checkout.session.completed".equals(event.getType())) {
            log.info("Ignoring unsupported Stripe webhook event type: {}", event.getType());
            return;
        }

        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new RuntimeException("Unable to deserialize Stripe checkout session payload"));

        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            log.info("Skipping checkout session {} because payment status is {}", session.getId(), session.getPaymentStatus());
            return;
        }

        String cartSessionId = session.getMetadata() != null
                ? session.getMetadata().get("cartSessionId")
                : null;

        if (!StringUtils.hasText(cartSessionId)) {
            cartSessionId = session.getClientReferenceId();
        }

        if (!StringUtils.hasText(cartSessionId)) {
            throw new RuntimeException("Stripe checkout session is missing cart session metadata");
        }

        cartService.clearCartIfExists(cartSessionId);
        log.info("Finalized paid Stripe checkout session {} for cart session {}", session.getId(), cartSessionId);
    }

    private String resolveSuccessUrl(CheckoutSessionRequest request) {
        if (request != null && StringUtils.hasText(request.getSuccessUrl())) {
            return request.getSuccessUrl();
        }
        if (StringUtils.hasText(stripeProperties.getSuccessUrl())) {
            return stripeProperties.getSuccessUrl();
        }
        throw new RuntimeException("Stripe success URL is not configured");
    }

    private String resolveCancelUrl(CheckoutSessionRequest request) {
        if (request != null && StringUtils.hasText(request.getCancelUrl())) {
            return request.getCancelUrl();
        }
        if (StringUtils.hasText(stripeProperties.getCancelUrl())) {
            return stripeProperties.getCancelUrl();
        }
        throw new RuntimeException("Stripe cancel URL is not configured");
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private String buildDescription(CartItemDTO item) {
        String author = StringUtils.hasText(item.getBookAuthor()) ? item.getBookAuthor() : "Unknown author";
        String isbn = StringUtils.hasText(item.getBookIsbn()) ? "ISBN: " + item.getBookIsbn() : null;
        return isbn == null ? "by " + author : "by " + author + " | " + isbn;
    }
}

// Made with Bob
