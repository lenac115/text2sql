package com.ai.main.dto;

public record IndexInfo(
    String tableName,
    String indexName,
    String columnName
) { }
