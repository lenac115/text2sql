package com.ai.main.dto.product;

import com.ai.main.domain.Product;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        int price,
        int stock,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCreatedAt()
        );
    }
}