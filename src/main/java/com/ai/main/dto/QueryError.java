package com.ai.main.dto;

import java.util.Map;

public record QueryError(
        String code,       // QUERY_BLOCKED, DANGEROUS_QUERY 등
        String message,
        Map<String, Object> detail
) {
}
