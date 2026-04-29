package com.ai.main.dto.payment;

import com.ai.main.domain.Payment;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Payment.PaymentMethod method
) {}