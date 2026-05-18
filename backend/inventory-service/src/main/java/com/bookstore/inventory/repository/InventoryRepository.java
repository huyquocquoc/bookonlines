package com.bookstore.inventory.repository;

import com.bookstore.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Inventory entity
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by book ID
     */
    Optional<Inventory> findByBookId(Long bookId);

    /**
     * Find inventory by ISBN
     */
    Optional<Inventory> findByIsbn(String isbn);

    /**
     * Check if inventory exists for book ID
     */
    boolean existsByBookId(Long bookId);

    /**
     * Delete inventory by book ID
     */
    void deleteByBookId(Long bookId);
}

// Made with Bob
