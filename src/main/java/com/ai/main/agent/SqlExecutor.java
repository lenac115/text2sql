package com.ai.main.agent;

import com.ai.main.dto.query.ColumnRowData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SqlExecutor {

    private final JdbcTemplate jdbcTemplate;
    private static final Pattern LIMIT_PATTERN = Pattern.compile("\\bLIMIT\\b", Pattern.CASE_INSENSITIVE);

    @Autowired
    public SqlExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ColumnRowData execute(String sql) {
        String upperCaseSql = sql.toUpperCase();
        if (!LIMIT_PATTERN.matcher(upperCaseSql).matches()) {
            sql = sql + " LIMIT 1000";
        }

        long start = System.currentTimeMillis();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        long elapsed = System.currentTimeMillis() - start;
        return new ColumnRowData(rows, elapsed);
    }
}
