package com.ai.main.dto.query;

import java.util.HashMap;
import java.util.Map;

public record SqlValidationResult (
        boolean allowed,
        String code,
        String message,
        Map<String, Object> detail
) {

    // 차단
    public static SqlValidationResult blocked(String code, String message, String keyword) {
        Map<String, Object> detail = new HashMap<>();
        if (keyword != null) detail.put("detectedKeyword", keyword);
        return new SqlValidationResult(false, code, message, detail);
    }

    public static SqlValidationResult passed() {
        return new SqlValidationResult(true, null, null, null);
    }

    public static SqlValidationResult warning(String message) {
        return new SqlValidationResult(true, null, message, null);
    }
}
