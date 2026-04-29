package com.ai.main.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCreateRequest(
        @NotBlank String name,
        String description,
        String imageUrl,
        @Min(0) int price,
        @Min(0) int stock,
        @NotNull Long categoryId
) {}