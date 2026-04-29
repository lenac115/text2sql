package com.ai.main.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_payment_order_id", columnList = "orders_id"),
        @Index(name = "idx_payment_status_requested_at", columnList = "status, requestedAt")
})
public class Payment {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", unique = true)
    private Orders orders;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentStatus status;

    @NotNull
    private int amount;

    private String pgTransactionId;
    private String failureReason;

    @NotNull
    private LocalDateTime requestedAt;

    private LocalDateTime confirmedAt;

    public enum PaymentMethod {
        CARD, KAKAO_PAY, NAVER_PAY, BANK_TRANSFER
    }

    public enum PaymentStatus {
        PENDING, SUCCEEDED, FAILED, CANCELLED
    }

    public void markSucceeded(String pgTransactionId) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }
        this.status = PaymentStatus.SUCCEEDED;
        this.pgTransactionId = pgTransactionId;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markCancelled() {
        this.status = PaymentStatus.CANCELLED;
        this.confirmedAt = LocalDateTime.now();
    }
}