package com.ai.main.dto;

public record ColumnInfo (
        String tableName,
        String columnName,
        String dataType,
        String columnKey,
        String columnType
) { }
