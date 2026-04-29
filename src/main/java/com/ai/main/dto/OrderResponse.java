package com.ai.main.dto;

import com.ai.main.domain.Orders;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        int totalAmount,
        int discountAmount,
        int finalAmount,
        String status,
        LocalDateTime orderedAt,
        AddressResponse shippingAddress,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Orders order) {
        List<OrderItemResponse> itemResponses = order.getOrderItemsList().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getFinalAmount(),
                order.getOrderStatus().name(),
                order.getOrderedAt(),
                AddressResponse.from(order.getShippingAddress()),
                itemResponses
        );
    }
}