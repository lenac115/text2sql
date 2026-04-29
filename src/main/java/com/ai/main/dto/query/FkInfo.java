package com.ai.main.dto.query;

public record FkInfo(
        String tableName,
        String columnName,
        String referencedTable,
        String referencedColumn
) { }
