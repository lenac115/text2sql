package com.ai.main.dto;

public record QueryMetaData(
        long executionTimeMs,
        int rowCount,
        boolean cached
) { }
