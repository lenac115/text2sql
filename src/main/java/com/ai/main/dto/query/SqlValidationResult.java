package com.ai.main.dto.query;

import java.util.Map;

public record SqlValidationResult (
        boolean allowed,
        boolean retryable,
        String code,
        String message,
        Map<String, Object> detail
) {

    public static SqlValidationResult blocked(String code, String message, Map<String, Object> detail, boolean retryable) {
        return new SqlValidationResult(false, retryable, code, message, detail);
    }

    public static SqlValidationResult passed() {
        return new SqlValidationResult(true, false, null, null, null);
    }

    public static SqlValidationResult warning(String message) {
        return new SqlValidationResult(true, false, null, message, null);
    }
}