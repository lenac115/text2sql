package com.ai.main.dto.product;

public record ProductUpdateRequest(
        String name,
        String description,
        String imageUrl,
        Integer price,
        Integer stock,
        Long categoryId
) {}