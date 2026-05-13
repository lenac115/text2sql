package com.ai.main.dto.coupon;

import com.ai.main.domain.Coupon;

import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String code,
        String name,
        Coupon.DiscountType discountType,
        int discountValue,
        int minOrderAmount,
        int maxDiscountAmount,
        int totalQuantity,
        int issuedQuantity,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static CouponResponse from(Coupon c) {
        return new CouponResponse(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getDiscountType(),
                c.getDiscountValue(),
                c.getMinOrderAmount(),
                c.getMaxDiscountAmount(),
                c.getTotalQuantity(),
                c.getIssuedQuantity(),
                c.getExpiresAt(),
                c.getCreatedAt()
        );
    }
}
