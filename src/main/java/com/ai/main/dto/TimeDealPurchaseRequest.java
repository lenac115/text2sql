package com.ai.main.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

public record TimeDealPurchaseRequest(
        @Min(1) int quantity,
        @Valid AddressRequest shippingAddress  // null이면 유저 기본 배송지 사용
) {}