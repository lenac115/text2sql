package com.ai.main.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        @NotEmpty @Valid List<OrderItemRequest> items,
        Long userCouponId,             // nullable, 쿠폰 미사용 시 null
        @Valid AddressRequest shippingAddress  // null이면 유저 기본 배송지 사용
) {}