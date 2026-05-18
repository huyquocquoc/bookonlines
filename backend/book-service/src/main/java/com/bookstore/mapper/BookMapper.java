package com.bookstore.mapper;

import com.bookstore.common.dto.BookDTO;
import com.bookstore.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for converting between Book entity and BookDTO
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookMapper {

    /**
     * Convert Book entity to BookDTO
     */
    BookDTO toDTO(Book book);

    /**
     * Convert BookDTO to Book entity
     */
    Book toEntity(BookDTO bookDTO);

    /**
     * Update existing Book entity from BookDTO
     * Ignores null values in the DTO
     */
    void updateEntityFromDTO(BookDTO bookDTO, @MappingTarget Book book);
}

// Made with Bob
