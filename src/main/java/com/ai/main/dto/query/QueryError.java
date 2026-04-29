package com.ai.main.dto.query;

import java.util.Map;

public record QueryError(
        String code,
        String message,
        Map<String, Object> detail
) {
}
