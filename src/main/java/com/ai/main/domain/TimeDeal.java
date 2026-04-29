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
        @Index(name = "idx_time_deal_start_end", columnList = "start_at, end_at"),
})
public class TimeDeal {

    @Id
    @Column(name = "time_deal_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int dealPrice;
    private int totalStock;
    private int remainingStock;
    private int maxPerUser;       // 1인당 구매 제한 (0 = 무제한)

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    @NotNull
    private LocalDateTime createdAt;

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return remainingStock > 0 && now.isAfter(startAt) && now.isBefore(endAt);
    }

    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startAt);
    }

    public void purchase(int quantity) {
        if (!isActive()) {
            throw new IllegalStateException("현재 구매할 수 없는 딜입니다.");
        }
        if (remainingStock < quantity) {
            throw new IllegalStateException("남은 수량이 부족합니다. (남은: " + remainingStock + ")");
        }
        this.remainingStock -= quantity;
    }

    public void restoreStock(int quantity) {
        this.remainingStock = Math.min(this.remainingStock + quantity, this.totalStock);
    }
}