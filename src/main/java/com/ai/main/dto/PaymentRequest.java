package com.ai.main.dto;

import com.ai.main.domain.Payment;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Payment.PaymentMethod method
) {}