package com.ai.main.dto;

public record OrderItemResponse(
        Long productId,
        String productName,
        int quantity,
        int unitPrice,
        int subtotal
) {}