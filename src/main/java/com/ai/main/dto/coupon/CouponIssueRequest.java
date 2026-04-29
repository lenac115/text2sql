package com.ai.main.dto.coupon;

import jakarta.validation.constraints.NotBlank;

public record CouponIssueRequest(
        @NotBlank String code
) {}