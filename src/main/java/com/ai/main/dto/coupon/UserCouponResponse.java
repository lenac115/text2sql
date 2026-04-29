package com.ai.main.dto.coupon;

import com.ai.main.domain.UserCoupon;

import java.time.LocalDateTime;

public record UserCouponResponse(
        Long id,
        String couponName,
        String code,
        String discountType,
        int discountValue,
        int minOrderAmount,
        int maxDiscountAmount,
        boolean used,
        LocalDateTime expiresAt,
        LocalDateTime issuedAt
) {
    public static UserCouponResponse from(UserCoupon uc) {
        return new UserCouponResponse(
                uc.getId(),
                uc.getCoupon().getName(),
                uc.getCoupon().getCode(),
                uc.getCoupon().getDiscountType().name(),
                uc.getCoupon().getDiscountValue(),
                uc.getCoupon().getMinOrderAmount(),
                uc.getCoupon().getMaxDiscountAmount(),
                uc.isUsed(),
                uc.getCoupon().getExpiresAt(),
                uc.getIssuedAt()
        );
    }
}