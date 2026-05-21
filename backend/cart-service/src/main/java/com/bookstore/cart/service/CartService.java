package com.bookstore.cart.service;

import com.bookstore.cart.entity.Cart;
import com.bookstore.cart.entity.CartItem;
import com.bookstore.cart.repository.CartRepository;
import com.bookstore.common.dto.CartDTO;
import com.bookstore.common.dto.CartItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing shopping carts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    /**
     * Get or create cart by session ID
     */
    @Transactional
    public CartDTO getOrCreateCart(String sessionId) {
        log.info("Getting or creating cart for session: {}", sessionId);
        
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .sessionId(sessionId)
                            .totalAmount(BigDecimal.ZERO)
                            .totalItems(0)
                            .build();
                    return cartRepository.save(newCart);
                });
        
        return convertToDTO(cart);
    }

    /**
     * Add item to cart
     */
    @Transactional
    public CartDTO addItemToCart(String sessionId, CartItemDTO itemDTO) {
        log.info("Adding item to cart - Session: {}, Book ID: {}", sessionId, itemDTO.getBookId());
        
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .sessionId(sessionId)
                            .totalAmount(BigDecimal.ZERO)
                            .totalItems(0)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Check if item already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getBookId().equals(itemDTO.getBookId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + itemDTO.getQuantity());
            existingItem.calculateSubtotal();
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .bookId(itemDTO.getBookId())
                    .bookTitle(itemDTO.getBookTitle())
                    .bookAuthor(itemDTO.getBookAuthor())
                    .bookIsbn(itemDTO.getBookIsbn())
                    .bookPrice(itemDTO.getBookPrice())
                    .quantity(itemDTO.getQuantity())
                    .build();
            newItem.calculateSubtotal();
            cart.addItem(newItem);
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Item added to cart. Total items: {}, Total amount: {}", 
                savedCart.getTotalItems(), savedCart.getTotalAmount());
        
        return convertToDTO(savedCart);
    }

    /**
     * Update item quantity in cart
     */
    @Transactional
    public CartDTO updateItemQuantity(String sessionId, Long itemId, Integer quantity) {
        log.info("Updating item quantity - Session: {}, Item ID: {}, Quantity: {}", 
                sessionId, itemId, quantity);
        
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found for session: " + sessionId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart: " + itemId));

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            item.setQuantity(quantity);
            item.calculateSubtotal();
            cart.recalculateTotals();
        }

        Cart savedCart = cartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public CartDTO removeItemFromCart(String sessionId, Long itemId) {
        log.info("Removing item from cart - Session: {}, Item ID: {}", sessionId, itemId);
        
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found for session: " + sessionId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart: " + itemId));

        cart.removeItem(item);
        Cart savedCart = cartRepository.save(cart);
        
        return convertToDTO(savedCart);
    }

    /**
     * Clear cart
     */
    @Transactional
    public void clearCart(String sessionId) {
        log.info("Clearing cart for session: {}", sessionId);
        
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Cart not found for session: " + sessionId));

        cart.getItems().clear();
        cart.recalculateTotals();
        cartRepository.save(cart);
    }

    /**
     * Clear cart if it exists. Safe for webhook retries.
     */
    @Transactional
    public void clearCartIfExists(String sessionId) {
        log.info("Clearing cart if it exists for session: {}", sessionId);

        cartRepository.findBySessionId(sessionId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.recalculateTotals();
            cartRepository.save(cart);
        });
    }

    /**
     * Generate a new session ID
     */
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Convert Cart entity to DTO
     */
    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .sessionId(cart.getSessionId())
                .items(itemDTOs)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .build();
    }

    /**
     * Convert CartItem entity to DTO
     */
    private CartItemDTO convertItemToDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .bookId(item.getBookId())
                .bookTitle(item.getBookTitle())
                .bookAuthor(item.getBookAuthor())
                .bookIsbn(item.getBookIsbn())
                .bookPrice(item.getBookPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}

// Made with Bob
