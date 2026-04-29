package com.ai.main.dto;

import com.ai.main.domain.Orders;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull Orders.OrderStatus status
) {}