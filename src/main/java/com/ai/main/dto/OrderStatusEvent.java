package com.ai.main.dto;

import java.time.LocalDateTime;

public record OrderStatusEvent(
        Long orderId,
        String previousStatus,
        String currentStatus,
        LocalDateTime changedAt
) {}