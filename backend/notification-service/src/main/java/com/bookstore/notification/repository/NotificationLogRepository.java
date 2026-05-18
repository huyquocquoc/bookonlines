package com.bookstore.notification.repository;

import com.bookstore.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for NotificationLog entity
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Find notifications by book ID
     */
    List<NotificationLog> findByBookId(Long bookId);

    /**
     * Find notifications by event type
     */
    List<NotificationLog> findByEventType(String eventType);

    /**
     * Find notifications by status
     */
    List<NotificationLog> findByStatus(String status);
}

// Made with Bob
