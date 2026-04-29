package com.ai.main.dto.category;

import com.ai.main.domain.Category;

public record CategoryResponse(
        Long id,
        String name,
        int productCount
) {
    public static CategoryResponse from(Category category) {
        int count = category.getProducts() != null ? category.getProducts().size() : 0;
        return new CategoryResponse(category.getId(), category.getName(), count);
    }
}