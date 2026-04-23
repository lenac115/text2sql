package com.ai.main.dto;

import java.util.Map;

public record QueryError(
        String code,
        String message,
        Map<String, Object> detail
) {
}
