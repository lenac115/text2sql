package com.ai.main.dto.query;

public record QueryExecutionContext(
        String question,
        String generatedSql,
        ColumnRowData data,
        long executionTimeMs,
        boolean cached
) {
}
