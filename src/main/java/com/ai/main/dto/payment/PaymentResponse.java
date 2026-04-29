package com.ai.main.dto.payment;

import com.ai.main.domain.Payment;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        String method,
        String status,
        int amount,
        String pgTransactionId,
        String failureReason,
        LocalDateTime requestedAt,
        LocalDateTime confirmedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrders().getId(),
                payment.getMethod().name(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getPgTransactionId(),
                payment.getFailureReason(),
                payment.getRequestedAt(),
                payment.getConfirmedAt()
        );
    }
}