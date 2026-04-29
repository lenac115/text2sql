package com.ai.main.dto;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        int totalAmount
) {}