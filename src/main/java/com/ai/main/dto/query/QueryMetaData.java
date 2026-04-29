package com.ai.main.dto.query;

public record QueryMetaData(
        long executionTimeMs,
        int rowCount,
        boolean cached
) { }
