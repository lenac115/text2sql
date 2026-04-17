package com.ai.main.dto;

public record FkInfo(
        String tableName,
        String columnName,
        String referencedTable,
        String referencedColumn
) { }
