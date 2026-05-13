package com.ai.main.dto.order;

import com.ai.main.domain.Orders;

import java.time.LocalDateTime;

public record AdminOrderSummary(
        Long orderId,
        String userEmail,
        String userName,
        int totalAmount,
        int discountAmount,
        int finalAmount,
        String status,
        LocalDateTime orderedAt
) {
    public static AdminOrderSummary from(Orders o) {
        return new AdminOrderSummary(
                o.getId(),
                o.getUsers().getEmail(),
                o.getUsers().getName(),
                o.getTotalAmount(),
                o.getDiscountAmount(),
                o.getFinalAmount(),
                o.getOrderStatus().name(),
                o.getOrderedAt()
        );
    }
}
