package com.bookstore.cart.repository;

import com.bookstore.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by session ID
     */
    Optional<Cart> findBySessionId(String sessionId);

    /**
     * Check if cart exists by session ID
     */
    boolean existsBySessionId(String sessionId);
}

// Made with Bob