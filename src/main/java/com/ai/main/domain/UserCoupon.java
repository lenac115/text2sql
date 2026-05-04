package com.ai.main.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {

    @Id
    @Column(name = "user_coupon_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Builder.Default
    private boolean used = false;

    @Version
    private Long version;

    private LocalDateTime usedAt;
    private Long orderId;

    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    public void use(Long orderId) {
        if (used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (LocalDateTime.now().isAfter(coupon.getExpiresAt())) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    public void restore() {
        if (!used) {
            throw new IllegalStateException("사용되지 않은 쿠폰입니다.");
        }
        this.used = false;
        this.usedAt = null;
        this.orderId = null;
    }
}