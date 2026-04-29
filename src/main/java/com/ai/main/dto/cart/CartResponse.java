package com.ai.main.dto.cart;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        int totalAmount
) {}