package com.ai.main.dto.coupon;

import com.ai.main.domain.Coupon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CouponCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull Coupon.DiscountType discountType,
        int discountValue,
        int minOrderAmount,
        int maxDiscountAmount,
        int totalQuantity,        // 0 = 무제한
        @NotNull LocalDateTime expiresAt
) {}