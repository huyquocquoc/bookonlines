package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Book entity
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find book by ISBN
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Find all active books with pagination
     */
    Page<Book> findByActiveTrue(Pageable pageable);

    /**
     * Search books by title, author, or category
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Book> searchBooks(@Param("search") String search, Pageable pageable);

    /**
     * Find books by category
     */
    Page<Book> findByCategory(String category, Pageable pageable);

    /**
     * Find books by author
     */
    Page<Book> findByAuthor(String author, Pageable pageable);

    /**
     * Find books by category and active status
     */
    Page<Book> findByCategoryAndActiveTrue(String category, Pageable pageable);

    /**
     * Find books by author and active status
     */
    Page<Book> findByAuthorAndActiveTrue(String author, Pageable pageable);

    /**
     * Check if book exists by ISBN
     */
    boolean existsByIsbn(String isbn);

    /**
     * Count active books
     */
    long countByActiveTrue();
}

// Made with Bob
