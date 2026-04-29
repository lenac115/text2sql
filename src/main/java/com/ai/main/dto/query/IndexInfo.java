package com.ai.main.dto.query;

public record IndexInfo(
    String tableName,
    String indexName,
    String columnName
) { }
