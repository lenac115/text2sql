package com.ai.main.dto.query;

public record ColumnInfo (
        String tableName,
        String columnName,
        String dataType,
        String columnKey,
        String columnType
) { }
