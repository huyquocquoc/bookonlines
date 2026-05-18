package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Book entity representing the books table in PostgreSQL
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_isbn", columnList = "isbn"),
    @Index(name = "idx_books_title", columnList = "title"),
    @Index(name = "idx_books_author", columnList = "author"),
    @Index(name = "idx_books_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "author", nullable = false, length = 255)
    private String author;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "language", length = 50)
    private String language = "English";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    //@Column(name = "published_date")
    //private LocalDate publishedDate;
    //private java.sql.Date publishedDate;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "active")
    private Boolean active = true;

    // @CreatedDate
    // @Column(name = "created_at", nullable = false, updatable = false)
    // private LocalDateTime createdAt;

    // @LastModifiedDate
    // @Column(name = "updated_at")
    // private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        // if (createdAt == null) {
        //     createdAt = LocalDateTime.now();
        // }
        // if (updatedAt == null) {
        //     updatedAt = LocalDateTime.now();
        // }
        if (language == null) {
            language = "English";
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        if (rating == null) {
            rating = BigDecimal.ZERO;
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        //updatedAt = LocalDateTime.now();
    }
}

// Made with Bob
