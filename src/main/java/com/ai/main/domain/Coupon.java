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
public class Coupon {

    @Id
    @Column(name = "coupon_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String code;

    @NotNull
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DiscountType discountType;

    private int discountValue;      // 정액: 원 단위 / 정률: 1~100 (%)
    private int minOrderAmount;     // 최소 주문 금액 (0 = 제한 없음)
    private int maxDiscountAmount;  // 최대 할인 한도 (0 = 제한 없음, 정률일 때만 유효)
    private int totalQuantity;      // 0 = 무제한
    private int issuedQuantity;

    @Version
    private Long version;

    @NotNull
    private LocalDateTime expiresAt;

    @NotNull
    private LocalDateTime createdAt;

    public enum DiscountType {
        FIXED, PERCENTAGE
    }

    public int calculateDiscount(int orderAmount) {
        if (discountType == DiscountType.FIXED) {
            return Math.min(discountValue, orderAmount);
        }
        int discount = orderAmount * discountValue / 100;
        if (maxDiscountAmount > 0) {
            discount = Math.min(discount, maxDiscountAmount);
        }
        return discount;
    }

    public boolean canIssue() {
        return (totalQuantity == 0 || issuedQuantity < totalQuantity)
                && LocalDateTime.now().isBefore(expiresAt);
    }

    public void issue() {
        if (!canIssue()) {
            throw new IllegalStateException("발급 가능한 쿠폰이 없습니다.");
        }
        this.issuedQuantity++;
    }
}