package com.bookstore.inventory.service;

import com.bookstore.common.dto.BookDTO;
import com.bookstore.inventory.entity.Inventory;
import com.bookstore.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing inventory operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * Initialize inventory when a new book is created
     */
    public void initializeInventory(Long bookId, BookDTO bookDTO) {
        log.info("Initializing inventory for book ID: {}", bookId);
        
        if (inventoryRepository.existsByBookId(bookId)) {
            log.warn("Inventory already exists for book ID: {}", bookId);
            return;
        }
        
        Inventory inventory = Inventory.builder()
                .bookId(bookId)
                .isbn(bookDTO.getIsbn())
                .title(bookDTO.getTitle())
                .availableQuantity(bookDTO.getStockQuantity())
                .totalQuantity(bookDTO.getStockQuantity())
                .reservedQuantity(0)
                .reorderLevel(10)
                .build();
        
        inventoryRepository.save(inventory);
        
        if (inventory.isLowStock()) {
            log.warn("Low stock alert for book ID: {} - Available: {}", 
                bookId, inventory.getAvailableQuantity());
        }
        
        log.info("Inventory initialized for book ID: {} with quantity: {}", 
            bookId, inventory.getAvailableQuantity());
    }

    /**
     * Update inventory when a book is updated
     */
    public void updateInventory(Long bookId, BookDTO bookDTO) {
        log.info("Updating inventory for book ID: {}", bookId);
        
        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseGet(() -> {
                    log.info("Inventory not found for book ID: {}, creating new", bookId);
                    return Inventory.builder()
                            .bookId(bookId)
                            .isbn(bookDTO.getIsbn())
                            .title(bookDTO.getTitle())
                            .availableQuantity(0)
                            .totalQuantity(0)
                            .reservedQuantity(0)
                            .build();
                });
        
        // Update inventory details
        inventory.setIsbn(bookDTO.getIsbn());
        inventory.setTitle(bookDTO.getTitle());
        
        // Update quantities if stock quantity changed
        if (bookDTO.getStockQuantity() != null) {
            int difference = bookDTO.getStockQuantity() - inventory.getTotalQuantity();
            inventory.setTotalQuantity(bookDTO.getStockQuantity());
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + difference);
        }
        
        inventoryRepository.save(inventory);
        
        if (inventory.isLowStock()) {
            log.warn("Low stock alert for book ID: {} - Available: {}", 
                bookId, inventory.getAvailableQuantity());
        }
        
        log.info("Inventory updated for book ID: {} - Available: {}, Total: {}", 
            bookId, inventory.getAvailableQuantity(), inventory.getTotalQuantity());
    }

    /**
     * Remove inventory when a book is deleted
     */
    public void removeInventory(Long bookId) {
        log.info("Removing inventory for book ID: {}", bookId);
        
        inventoryRepository.findByBookId(bookId).ifPresent(inventory -> {
            inventoryRepository.delete(inventory);
            log.info("Inventory removed for book ID: {}", bookId);
        });
    }

    /**
     * Reserve stock for an order
     */
    public boolean reserveStock(Long bookId, int quantity) {
        log.info("Reserving {} units for book ID: {}", quantity, bookId);
        
        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for book ID: " + bookId));
        
        if (inventory.getAvailableQuantity() < quantity) {
            log.warn("Insufficient stock for book ID: {} - Requested: {}, Available: {}", 
                bookId, quantity, inventory.getAvailableQuantity());
            return false;
        }
        
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
        
        log.info("Stock reserved for book ID: {} - Reserved: {}, Available: {}", 
            bookId, quantity, inventory.getAvailableQuantity());
        return true;
    }

    /**
     * Release reserved stock
     */
    public void releaseStock(Long bookId, int quantity) {
        log.info("Releasing {} units for book ID: {}", quantity, bookId);
        
        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for book ID: " + bookId));
        
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventoryRepository.save(inventory);
        
        log.info("Stock released for book ID: {} - Released: {}, Available: {}", 
            bookId, quantity, inventory.getAvailableQuantity());
    }

    /**
     * Get inventory by book ID
     */
    @Transactional(readOnly = true)
    public Inventory getInventoryByBookId(Long bookId) {
        return inventoryRepository.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for book ID: " + bookId));
    }
}

// Made with Bob
