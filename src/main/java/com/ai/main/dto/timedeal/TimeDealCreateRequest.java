package com.ai.main.dto.timedeal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TimeDealCreateRequest(
        @NotBlank String title,
        @NotNull Long productId,
        @Min(0) int dealPrice,
        @Min(1) int totalStock,
        @Min(0) int maxPerUser,   // 0 = 무제한
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt
) {}