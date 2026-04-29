package com.ai.main.dto;

import jakarta.validation.constraints.NotBlank;

public record CouponIssueRequest(
        @NotBlank String code
) {}