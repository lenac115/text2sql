package com.ai.main.dto;

import com.ai.main.domain.TimeDeal;

import java.time.LocalDateTime;

public record TimeDealResponse(
        Long id,
        String title,
        Long productId,
        String productName,
        int originalPrice,
        int dealPrice,
        int discountRate,       // 할인율 (%)
        int totalStock,
        int remainingStock,
        int maxPerUser,
        String status,          // UPCOMING, ACTIVE, SOLD_OUT, ENDED
        LocalDateTime startAt,
        LocalDateTime endAt
) {
    public static TimeDealResponse from(TimeDeal deal) {
        int originalPrice = deal.getProduct().getPrice();
        int discountRate = originalPrice > 0
                ? (int) ((1.0 - (double) deal.getDealPrice() / originalPrice) * 100)
                : 0;

        return new TimeDealResponse(
                deal.getId(),
                deal.getTitle(),
                deal.getProduct().getId(),
                deal.getProduct().getName(),
                originalPrice,
                deal.getDealPrice(),
                discountRate,
                deal.getTotalStock(),
                deal.getRemainingStock(),
                deal.getMaxPerUser(),
                resolveStatus(deal),
                deal.getStartAt(),
                deal.getEndAt()
        );
    }

    private static String resolveStatus(TimeDeal deal) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(deal.getStartAt())) return "UPCOMING";
        if (now.isAfter(deal.getEndAt())) return "ENDED";
        if (deal.getRemainingStock() <= 0) return "SOLD_OUT";
        return "ACTIVE";
    }
}