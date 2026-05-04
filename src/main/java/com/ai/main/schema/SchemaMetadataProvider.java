package com.ai.main.schema;

import com.ai.main.dto.query.ColumnInfo;
import com.ai.main.dto.query.FkInfo;
import com.ai.main.dto.query.IndexInfo;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SchemaMetadataProvider {

    private final JdbcTemplate jdbcTemplate;
    private String cachedSchemaPrompt;

    public SchemaMetadataProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        this.cachedSchemaPrompt = generateSchemaPrompt();
    }

    public String getSchemaPrompt() {
        return cachedSchemaPrompt;
    }


    public String generateSchemaPrompt() {
        List<ColumnInfo> columns = fetchColumns();
        List<IndexInfo> indexes = fetchIndexes();
        List<FkInfo> foreignKeys = fetchForeignKeys();

        return buildPromptText(columns, indexes, foreignKeys);
    }

    private String buildPromptText(List<ColumnInfo> columns, List<IndexInfo> indexes, List<FkInfo> foreignKeys) {
        StringBuilder sb = new StringBuilder();
        sb.append("아래는 MySQL 데이터베이스의 스키마 정보입니다.\n\n");

        Map<String, List<ColumnInfo>> tableColumns = columns.stream()
                .collect(Collectors.groupingBy(ColumnInfo::tableName));

        Map<String, List<IndexInfo>> tableIndexes = indexes.stream()
                .collect(Collectors.groupingBy(IndexInfo::tableName));

        for (String tableName : tableColumns.keySet()) {
            sb.append("테이블: ").append(tableName).append("\n");

            sb.append("컬럼:\n");
            for (ColumnInfo col : tableColumns.get(tableName)) {
                sb.append("  - ").append(col.columnName())
                        .append(" (").append(col.columnType()).append(")");

                if ("PRI".equals(col.columnKey())) sb.append(" [PK]");
                if ("MUL".equals(col.columnKey())) sb.append(" [FK]");

                sb.append("\n");
            }

            List<IndexInfo> idxList = tableIndexes.getOrDefault(tableName, List.of());
            if (!idxList.isEmpty()) {
                Map<String, List<String>> grouped = idxList.stream()
                        .collect(Collectors.groupingBy(
                                IndexInfo::indexName,
                                LinkedHashMap::new,
                                Collectors.mapping(IndexInfo::columnName, Collectors.toList())
                        ));

                sb.append("인덱스:\n");
                for (var entry : grouped.entrySet()) {
                    sb.append("  - ").append(entry.getKey())
                            .append("(").append(String.join(", ", entry.getValue())).append(")\n");
                }
            }

            sb.append("\n");
        }

        if (!foreignKeys.isEmpty()) {
            sb.append("외래키 관계:\n");
            for (FkInfo fk : foreignKeys) {
                sb.append("  - ").append(fk.tableName()).append(".").append(fk.columnName())
                        .append(" → ").append(fk.referencedTable()).append(".").append(fk.referencedColumn())
                        .append("\n");
            }
        }

        return sb.toString();
    }

    private List<ColumnInfo> fetchColumns() {
        String sql = """
                SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, COLUMN_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                ORDER BY TABLE_NAME, ORDINAL_POSITION
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ColumnInfo(
                rs.getString("TABLE_NAME"),
                rs.getString("COLUMN_NAME"),
                rs.getString("DATA_TYPE"),
                rs.getString("COLUMN_KEY"),
                rs.getString("COLUMN_TYPE")
        ));
    }

    private List<IndexInfo> fetchIndexes() {
        String sql = """
                SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                    AND INDEX_NAME != 'PRIMARY'
                ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new IndexInfo(
                rs.getString("TABLE_NAME"),
                rs.getString("INDEX_NAME"),
                rs.getString("COLUMN_NAME")
        ));
    }

    private List<FkInfo> fetchForeignKeys() {
        String sql = """
                SELECT TABLE_NAME, COLUMN_NAME,
                       REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
                FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND REFERENCED_TABLE_NAME IS NOT NULL
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new FkInfo(
                rs.getString("TABLE_NAME"),
                rs.getString("COLUMN_NAME"),
                rs.getString("REFERENCED_TABLE_NAME"),
                rs.getString("REFERENCED_COLUMN_NAME")
        ));
    }
}
