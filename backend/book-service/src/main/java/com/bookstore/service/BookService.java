package com.bookstore.service;

import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.event.BookEvent;
import com.bookstore.common.event.BookEventType;
import com.bookstore.entity.Book;
import com.bookstore.kafka.BookEventProducer;
import com.bookstore.mapper.BookMapper;
import com.bookstore.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for book operations
 * Implements caching and Kafka event publishing
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookEventProducer bookEventProducer;

    /**
     * Get all books with pagination
     * Note: Pagination results are not cached due to complexity of Page serialization
     */
    @Transactional(readOnly = true)
    public Page<BookDTO> getAllBooks(int page, int size) {
        log.info("Fetching books - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Book> books = bookRepository.findAll(pageable);
        return books.map(bookMapper::toDTO);
    }

    /**
     * Get book by ID
     * Cached with book ID as key
     */
    @Cacheable(value = "book", key = "#id")
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long id) {
        log.info("Fetching book by ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return bookMapper.toDTO(book);
    }

    /**
     * Search books by query
     * Cached with search query and pagination
     */
    @Cacheable(value = "books", key = "'search:' + #query + ':page:' + #page + ':size:' + #size")
    @Transactional(readOnly = true)
    public Page<BookDTO> searchBooks(String query, int page, int size) {
        log.info("Searching books with query: '{}' - page: {}, size: {}", query, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Book> books = bookRepository.searchBooks(query, pageable);
        return books.map(bookMapper::toDTO);
    }

    /**
     * Get books by category
     */
    @Cacheable(value = "books", key = "'category:' + #category + ':page:' + #page + ':size:' + #size")
    @Transactional(readOnly = true)
    public Page<BookDTO> getBooksByCategory(String category, int page, int size) {
        log.info("Fetching books by category: {} - page: {}, size: {}", category, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Book> books = bookRepository.findByCategoryAndActiveTrue(category, pageable);
        return books.map(bookMapper::toDTO);
    }

    /**
     * Get books by author
     */
    @Cacheable(value = "books", key = "'author:' + #author + ':page:' + #page + ':size:' + #size")
    @Transactional(readOnly = true)
    public Page<BookDTO> getBooksByAuthor(String author, int page, int size) {
        log.info("Fetching books by author: {} - page: {}, size: {}", author, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Book> books = bookRepository.findByAuthorAndActiveTrue(author, pageable);
        return books.map(bookMapper::toDTO);
    }

    /**
     * Create new book
     * Evicts all list caches and publishes BOOK_CREATED event
     */
    @Caching(evict = {
        @CacheEvict(value = "books", allEntries = true)
    })
    public BookDTO createBook(BookDTO bookDTO) {
        log.info("Creating new book: {}", bookDTO.getTitle());
        
        // Check if ISBN already exists
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
        }
        
        Book book = bookMapper.toEntity(bookDTO);
        Book savedBook = bookRepository.save(book);
        BookDTO savedBookDTO = bookMapper.toDTO(savedBook);
        
        // Publish BOOK_CREATED event to Kafka
        BookEvent event = BookEvent.create(BookEventType.BOOK_CREATED, savedBook.getId(), savedBookDTO);
        bookEventProducer.publishBookEvent(event);
        
        log.info("Book created successfully with ID: {}", savedBook.getId());
        return savedBookDTO;
    }

    /**
     * Update existing book
     * Evicts specific book cache and all list caches, publishes BOOK_UPDATED event
     */
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#id"),
        @CacheEvict(value = "books", allEntries = true)
    })
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        log.info("Updating book with ID: {}", id);
        
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        // Check if ISBN is being changed and if it already exists
        if (!existingBook.getIsbn().equals(bookDTO.getIsbn()) && 
            bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
        }
        
        bookMapper.updateEntityFromDTO(bookDTO, existingBook);
        Book updatedBook = bookRepository.save(existingBook);
        BookDTO updatedBookDTO = bookMapper.toDTO(updatedBook);
        
        // Publish BOOK_UPDATED event to Kafka
        BookEvent event = BookEvent.create(BookEventType.BOOK_UPDATED, updatedBook.getId(), updatedBookDTO);
        bookEventProducer.publishBookEvent(event);
        
        log.info("Book updated successfully with ID: {}", id);
        return updatedBookDTO;
    }

    /**
     * Delete book (soft delete by setting active to false)
     * Evicts specific book cache and all list caches, publishes BOOK_DELETED event
     */
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#id"),
        @CacheEvict(value = "books", allEntries = true)
    })
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        BookDTO bookDTO = bookMapper.toDTO(book);
        
        // Soft delete - set active to false
        book.setActive(false);
        bookRepository.save(book);
        
        // Publish BOOK_DELETED event to Kafka
        BookEvent event = BookEvent.create(BookEventType.BOOK_DELETED, id, bookDTO);
        bookEventProducer.publishBookEvent(event);
        
        log.info("Book deleted successfully with ID: {}", id);
    }

    /**
     * Hard delete book (permanently remove from database)
     */
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#id"),
        @CacheEvict(value = "books", allEntries = true)
    })
    public void hardDeleteBook(Long id) {
        log.info("Hard deleting book with ID: {}", id);
        
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        BookDTO bookDTO = bookMapper.toDTO(book);
        
        bookRepository.deleteById(id);
        
        // Publish BOOK_DELETED event to Kafka
        BookEvent event = BookEvent.create(BookEventType.BOOK_DELETED, id, bookDTO);
        bookEventProducer.publishBookEvent(event);
        
        log.info("Book hard deleted successfully with ID: {}", id);
    }

    /**
     * Get total count of active books
     */
    @Transactional(readOnly = true)
    public long getTotalActiveBooks() {
        return bookRepository.countByActiveTrue();
    }
}

// Made with Bob
