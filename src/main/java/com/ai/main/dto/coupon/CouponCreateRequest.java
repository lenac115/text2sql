package com.ai.main.dto.coupon;

import com.ai.main.domain.Coupon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record CouponCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull Coupon.DiscountType discountType,
        @Positive int discountValue,                  // FIXED: 원, PERCENTAGE: 1~100 (서비스에서 추가 검증)
        @PositiveOrZero int minOrderAmount,
        @PositiveOrZero int maxDiscountAmount,        // 0 = 한도 없음
        @PositiveOrZero int totalQuantity,            // 0 = 무제한
        @NotNull LocalDateTime expiresAt
) {}