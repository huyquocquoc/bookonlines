package com.bookstore.notification.service;

import com.bookstore.common.event.BookEvent;
import com.bookstore.notification.entity.NotificationLog;
import com.bookstore.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    /**
     * Send notification for book created event
     */
    public void sendBookCreatedNotification(BookEvent event) {
        log.info("Sending notification for book created: {}", event.getBook().getTitle());
        
        String message = String.format(
            "New book added to catalog: '%s' by %s. ISBN: %s, Price: $%.2f",
            event.getBook().getTitle(),
            event.getBook().getAuthor(),
            event.getBook().getIsbn(),
            event.getBook().getPrice()
        );
        
        logNotification(event, "BOOK_CREATED", message);
        
        // In a real implementation, you would send email/SMS here
        log.info("Notification sent: {}", message);
    }

    /**
     * Send notification for book updated event
     */
    public void sendBookUpdatedNotification(BookEvent event) {
        log.info("Sending notification for book updated: {}", event.getBook().getTitle());
        
        String message = String.format(
            "Book updated: '%s' by %s. ISBN: %s",
            event.getBook().getTitle(),
            event.getBook().getAuthor(),
            event.getBook().getIsbn()
        );
        
        logNotification(event, "BOOK_UPDATED", message);
        
        log.info("Notification sent: {}", message);
    }

    /**
     * Send notification for book deleted event
     */
    public void sendBookDeletedNotification(BookEvent event) {
        log.info("Sending notification for book deleted: {}", event.getBook().getTitle());
        
        String message = String.format(
            "Book removed from catalog: '%s' by %s. ISBN: %s",
            event.getBook().getTitle(),
            event.getBook().getAuthor(),
            event.getBook().getIsbn()
        );
        
        logNotification(event, "BOOK_DELETED", message);
        
        log.info("Notification sent: {}", message);
    }

    /**
     * Send low stock alert
     */
    public void sendLowStockAlert(Long bookId, String bookTitle, int availableQuantity) {
        log.warn("Low stock alert for book ID: {} - Available: {}", bookId, availableQuantity);
        
        String message = String.format(
            "Low stock alert: '%s' has only %d units remaining",
            bookTitle,
            availableQuantity
        );
        
        NotificationLog notificationLog = NotificationLog.builder()
                .eventId("LOW_STOCK_" + bookId)
                .eventType("LOW_STOCK_ALERT")
                .bookId(bookId)
                .bookTitle(bookTitle)
                .notificationType("ALERT")
                .message(message)
                .status("SENT")
                .build();
        
        notificationLogRepository.save(notificationLog);
        
        log.info("Low stock alert sent: {}", message);
    }

    /**
     * Log notification to database
     */
    private void logNotification(BookEvent event, String notificationType, String message) {
        NotificationLog notificationLog = NotificationLog.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType().name())
                .bookId(event.getBookId())
                .bookTitle(event.getBook().getTitle())
                .notificationType(notificationType)
                .message(message)
                .status("SENT")
                .build();
        
        notificationLogRepository.save(notificationLog);
        log.debug("Notification logged to database for event: {}", event.getEventId());
    }

    /**
     * Get notification history for a book
     */
    @Transactional(readOnly = true)
    public java.util.List<NotificationLog> getNotificationHistory(Long bookId) {
        return notificationLogRepository.findByBookId(bookId);
    }
}

// Made with Bob
