package com.bookstore.common.event;

import com.bookstore.common.dto.BookDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event object for book-related operations
 * Published to Kafka when books are created, updated, or deleted
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookEvent {

    private String eventId;
    
    private BookEventType eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Long bookId;
    
    private BookDTO book;

    /**
     * Create a new book event with generated ID and current timestamp
     */
    public static BookEvent create(BookEventType eventType, Long bookId, BookDTO book) {
        return BookEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .bookId(bookId)
                .book(book)
                .build();
    }
}

// Made with Bob
