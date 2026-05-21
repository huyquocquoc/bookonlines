package com.bookstore.cart.controller;

import com.bookstore.cart.service.CartService;
import com.bookstore.common.dto.CartDTO;
import com.bookstore.common.dto.CartItemDTO;
import com.bookstore.common.dto.CheckoutSessionRequest;
import com.bookstore.common.dto.CheckoutSessionResponse;
import com.bookstore.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for shopping cart operations
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;
    private final com.bookstore.cart.service.StripeCheckoutService stripeCheckoutService;

    /**
     * Get or create cart by session ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@PathVariable String sessionId) {
        log.info("GET /api/cart/{} - Get cart", sessionId);
        
        CartDTO cart = cartService.getOrCreateCart(sessionId);
        
        return ResponseEntity.ok(
                ApiResponse.success(cart, "Cart retrieved successfully")
        );
    }

    /**
     * Add item to cart
     */
    @PostMapping("/{sessionId}/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @PathVariable String sessionId,
            @Valid @RequestBody CartItemDTO itemDTO) {
        log.info("POST /api/cart/{}/items - Add item to cart", sessionId);
        
        CartDTO cart = cartService.addItemToCart(sessionId, itemDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(cart, "Item added to cart successfully")
        );
    }

    /**
     * Update item quantity
     */
    @PutMapping("/{sessionId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItemQuantity(
            @PathVariable String sessionId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("PUT /api/cart/{}/items/{} - Update quantity to {}", sessionId, itemId, quantity);
        
        CartDTO cart = cartService.updateItemQuantity(sessionId, itemId, quantity);
        
        return ResponseEntity.ok(
                ApiResponse.success(cart, "Item quantity updated successfully")
        );
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/{sessionId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable String sessionId,
            @PathVariable Long itemId) {
        log.info("DELETE /api/cart/{}/items/{} - Remove item from cart", sessionId, itemId);
        
        CartDTO cart = cartService.removeItemFromCart(sessionId, itemId);
        
        return ResponseEntity.ok(
                ApiResponse.success(cart, "Item removed from cart successfully")
        );
    }

    /**
     * Clear cart
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable String sessionId) {
        log.info("DELETE /api/cart/{} - Clear cart", sessionId);
        
        cartService.clearCart(sessionId);
        
        return ResponseEntity.ok(
                ApiResponse.success(null, "Cart cleared successfully")
        );
    }

    /**
     * Create Stripe Checkout session for the current cart.
     */
    @PostMapping("/{sessionId}/checkout")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @PathVariable String sessionId,
            @RequestBody(required = false) CheckoutSessionRequest request) {
        log.info("POST /api/cart/{}/checkout - Create Stripe Checkout session", sessionId);

        CartDTO cart = cartService.getOrCreateCart(sessionId);
        CheckoutSessionResponse response = stripeCheckoutService.createCheckoutSession(sessionId, cart, request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Stripe checkout session created successfully")
        );
    }

    /**
     * Generate new session ID
     */
    @GetMapping("/session/new")
    public ResponseEntity<ApiResponse<String>> generateSessionId() {
        log.info("GET /api/cart/session/new - Generate new session ID");
        
        String sessionId = cartService.generateSessionId();
        
        return ResponseEntity.ok(
                ApiResponse.success(sessionId, "Session ID generated successfully")
        );
    }
}

// Made with Bob
