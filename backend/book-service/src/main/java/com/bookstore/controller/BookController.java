package com.bookstore.controller;

import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.response.ApiResponse;
import com.bookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Book operations
 * Base path: /api/books
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    /**
     * GET /api/books - Get all books with pagination
     * Default page size: 30
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        log.info("GET /api/books - page: {}, size: {}", page, size);
        Page<BookDTO> books = bookService.getAllBooks(page, size);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * GET /api/books/{id} - Get book by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        log.info("GET /api/books/{}", id);
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    /**
     * GET /api/books/search - Search books
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        log.info("GET /api/books/search?query={}&page={}&size={}", query, page, size);
        Page<BookDTO> books = bookService.searchBooks(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * GET /api/books/category/{category} - Get books by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        log.info("GET /api/books/category/{} - page: {}, size: {}", category, page, size);
        Page<BookDTO> books = bookService.getBooksByCategory(category, page, size);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * GET /api/books/author/{author} - Get books by author
     */
    @GetMapping("/author/{author}")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getBooksByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        log.info("GET /api/books/author/{} - page: {}, size: {}", author, page, size);
        Page<BookDTO> books = bookService.getBooksByAuthor(author, page, size);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    /**
     * POST /api/books - Create new book
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody BookDTO bookDTO) {
        log.info("POST /api/books - Creating book: {}", bookDTO.getTitle());
        BookDTO createdBook = bookService.createBook(bookDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdBook, "Book created successfully"));
    }

    /**
     * PUT /api/books/{id} - Update existing book
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookDTO bookDTO) {
        
        log.info("PUT /api/books/{} - Updating book", id);
        BookDTO updatedBook = bookService.updateBook(id, bookDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedBook, "Book updated successfully"));
    }

    /**
     * DELETE /api/books/{id} - Delete book (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        log.info("DELETE /api/books/{}", id);
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Book deleted successfully"));
    }

    /**
     * DELETE /api/books/{id}/hard - Hard delete book (permanent)
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteBook(@PathVariable Long id) {
        log.info("DELETE /api/books/{}/hard", id);
        bookService.hardDeleteBook(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Book permanently deleted"));
    }

    /**
     * GET /api/books/count - Get total count of active books
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalActiveBooks() {
        log.info("GET /api/books/count");
        long count = bookService.getTotalActiveBooks();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

// Made with Bob
