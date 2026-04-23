package com.ai.main.agent;

import com.ai.main.dto.ColumnRowData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SqlExecutor {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SqlExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public ColumnRowData execute(String sql) {

        jdbcTemplate.setQueryTimeout(5);

        String upperCaseSql = sql.toUpperCase();
        if(!upperCaseSql.contains("LIMIT")) {
            sql = sql + " LIMIT 1000";
        }

        long start = System.currentTimeMillis();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        long elapsed = System.currentTimeMillis() - start;
        return new ColumnRowData(rows, elapsed);
    }
}
