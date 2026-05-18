package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Shopping Cart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long id;

    private String sessionId;

    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();

    private BigDecimal totalAmount;

    private Integer totalItems;
}

// Made with Bob