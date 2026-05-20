package com.bookstore.mapper;

import com.bookstore.common.dto.BookDTO;
import com.bookstore.entity.Book;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Book entity and BookDTO.
 */
@Component
public class BookMapper {

    public BookDTO toDTO(Book book) {
        if (book == null) {
            return null;
        }

        return BookDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .category(book.getCategory())
                .language(book.getLanguage())
                .description(book.getDescription())
                .price(book.getPrice())
                .stockQuantity(book.getStockQuantity())
                .pageCount(book.getPageCount())
                .coverImageUrl(book.getCoverImageUrl())
                .rating(book.getRating())
                .active(book.getActive())
                .build();
    }

    public Book toEntity(BookDTO bookDTO) {
        if (bookDTO == null) {
            return null;
        }

        return Book.builder()
                .id(bookDTO.getId())
                .isbn(bookDTO.getIsbn())
                .title(bookDTO.getTitle())
                .author(bookDTO.getAuthor())
                .publisher(bookDTO.getPublisher())
                .category(bookDTO.getCategory())
                .language(bookDTO.getLanguage())
                .description(bookDTO.getDescription())
                .price(bookDTO.getPrice())
                .stockQuantity(bookDTO.getStockQuantity())
                .pageCount(bookDTO.getPageCount())
                .coverImageUrl(bookDTO.getCoverImageUrl())
                .rating(bookDTO.getRating())
                .active(bookDTO.getActive())
                .build();
    }

    public void updateEntityFromDTO(BookDTO bookDTO, Book book) {
        if (bookDTO == null || book == null) {
            return;
        }

        if (bookDTO.getId() != null) {
            book.setId(bookDTO.getId());
        }
        if (bookDTO.getIsbn() != null) {
            book.setIsbn(bookDTO.getIsbn());
        }
        if (bookDTO.getTitle() != null) {
            book.setTitle(bookDTO.getTitle());
        }
        if (bookDTO.getAuthor() != null) {
            book.setAuthor(bookDTO.getAuthor());
        }
        if (bookDTO.getPublisher() != null) {
            book.setPublisher(bookDTO.getPublisher());
        }
        if (bookDTO.getCategory() != null) {
            book.setCategory(bookDTO.getCategory());
        }
        if (bookDTO.getLanguage() != null) {
            book.setLanguage(bookDTO.getLanguage());
        }
        if (bookDTO.getDescription() != null) {
            book.setDescription(bookDTO.getDescription());
        }
        if (bookDTO.getPrice() != null) {
            book.setPrice(bookDTO.getPrice());
        }
        if (bookDTO.getStockQuantity() != null) {
            book.setStockQuantity(bookDTO.getStockQuantity());
        }
        if (bookDTO.getPageCount() != null) {
            book.setPageCount(bookDTO.getPageCount());
        }
        if (bookDTO.getCoverImageUrl() != null) {
            book.setCoverImageUrl(bookDTO.getCoverImageUrl());
        }
        if (bookDTO.getRating() != null) {
            book.setRating(bookDTO.getRating());
        }
        if (bookDTO.getActive() != null) {
            book.setActive(bookDTO.getActive());
        }
    }
}

// Made with Bob
