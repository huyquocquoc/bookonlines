package com.bookstore.inventory.kafka;

import com.bookstore.common.event.BookEvent;
import com.bookstore.common.event.BookEventType;
import com.bookstore.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for book events
 * Listens to book-events topic and updates inventory accordingly
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventConsumer {

    private final InventoryService inventoryService;

    /**
     * Consume book events from Kafka
     */
    @KafkaListener(
        topics = "${kafka.topic.book-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBookEvent(BookEvent event) {
        log.info("Received book event: {} for book ID: {}", event.getEventType(), event.getBookId());
        
        try {
            switch (event.getEventType()) {
                case BOOK_CREATED:
                    handleBookCreated(event);
                    break;
                case BOOK_UPDATED:
                    handleBookUpdated(event);
                    break;
                case BOOK_DELETED:
                    handleBookDeleted(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing book event: {} for book ID: {}", 
                event.getEventType(), event.getBookId(), e);
        }
    }

    /**
     * Handle BOOK_CREATED event
     */
    private void handleBookCreated(BookEvent event) {
        log.info("Processing BOOK_CREATED event for book ID: {}", event.getBookId());
        inventoryService.initializeInventory(event.getBookId(), event.getBook());
    }

    /**
     * Handle BOOK_UPDATED event
     */
    private void handleBookUpdated(BookEvent event) {
        log.info("Processing BOOK_UPDATED event for book ID: {}", event.getBookId());
        inventoryService.updateInventory(event.getBookId(), event.getBook());
    }

    /**
     * Handle BOOK_DELETED event
     */
    private void handleBookDeleted(BookEvent event) {
        log.info("Processing BOOK_DELETED event for book ID: {}", event.getBookId());
        inventoryService.removeInventory(event.getBookId());
    }
}

// Made with Bob
