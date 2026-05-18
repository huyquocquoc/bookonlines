package com.bookstore.common.event;

/**
 * Enum representing different types of book events
 */
public enum BookEventType {
    /**
     * Event fired when a new book is created
     */
    BOOK_CREATED,
    
    /**
     * Event fired when an existing book is updated
     */
    BOOK_UPDATED,
    
    /**
     * Event fired when a book is deleted
     */
    BOOK_DELETED
}

// Made with Bob
