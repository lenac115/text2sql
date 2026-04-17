package com.ai.main.dto;

public record QueryResponse(
        boolean success,
        QueryResultData data,
        QueryError error
) {
}
