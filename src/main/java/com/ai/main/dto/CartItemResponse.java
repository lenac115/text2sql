package com.ai.main.dto;

import com.ai.main.domain.CartItem;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        int unitPrice,
        int quantity,
        int subtotal
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getPrice() * item.getQuantity()
        );
    }
}