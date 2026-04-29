package com.ai.main.dto.order;

public record OrderItemResponse(
        Long productId,
        String productName,
        int quantity,
        int unitPrice,
        int subtotal
) {}