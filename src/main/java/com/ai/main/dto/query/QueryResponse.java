package com.ai.main.dto.query;

public record QueryResponse(
        boolean success,
        QueryResultData data,
        QueryError error
) {
}
