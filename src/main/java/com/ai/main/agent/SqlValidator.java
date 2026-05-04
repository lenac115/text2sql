package com.ai.main.agent;

import com.ai.main.dto.query.SqlValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class SqlValidator {

    private final JdbcTemplate jdbcTemplate;

    public SqlValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private SqlValidationResult validateExplainPlan(String sql) {
        try {
            String explainSql = "EXPLAIN " + sql;
            List<Map<String, Object>> explainResults = jdbcTemplate.queryForList(explainSql);
            log.debug("EXPLAIN {} -> {}", explainSql, explainResults);

            for (Map<String, Object> row : explainResults) {
                String type = (String) row.get("type");
                Long estimatedRows = ((Number) row.get("rows")).longValue();
                String extra = (String) row.get("Extra");

                log.debug("Table: {}, type: {}, rows: {}, Extra: {}",
                        row.get("table"), type, estimatedRows, extra);

                // 풀 테이블 스캔 + 대량 행 → 차단 (재시도 가능: 인덱스 활용 SQL 재생성 유도)
                if ("ALL".equals(type) && estimatedRows > 30000) {
                    log.warn("차단됨! 테이블: {}, type: {}, rows: {}",
                            row.get("table"), type, estimatedRows);
                    Map<String, Object> detail = Map.of(
                            "reason", "FULL_TABLE_SCAN",
                            "table", row.get("table"),
                            "estimatedRows", estimatedRows
                    );
                    return SqlValidationResult.blocked(
                            "QUERY_BLOCKED",
                            "풀 테이블 스캔이 감지되어 실행하지 않았습니다. "
                                    + "예상 스캔 행 수: " + estimatedRows,
                            detail,
                            true
                    );
                }

                // 풀 인덱스 스캔 → 경고만
                if ("index".equals(type) && estimatedRows > 10000) {
                    return SqlValidationResult.warning(
                            "풀 인덱스 스캔이 감지되었습니다. 성능에 영향을 줄 수 있습니다."
                    );
                }

                // filesort + 대량 행 → 경고
                if (extra != null && extra.contains("Using filesort")
                        && estimatedRows > 5000) {
                    return SqlValidationResult.warning(
                            "파일 정렬이 감지되었습니다. 대량 데이터에서 성능 저하 가능성이 있습니다."
                    );
                }
            }

            return SqlValidationResult.passed();

        } catch (Exception e) {
            // 문법 오류로 EXPLAIN 실패 → 재시도 가능
            return SqlValidationResult.blocked(
                    "EXPLAIN_FAILED",
                    "실행계획 분석에 실패했습니다: " + e.getMessage(),
                    null,
                    true
            );
        }
    }

    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
            "DELETE", "UPDATE", "INSERT", "DROP", "ALTER",
            "TRUNCATE", "CREATE", "REPLACE", "GRANT", "REVOKE"
    );

    public SqlValidationResult validate(String sql) {
        String upperSql = sql.trim().toUpperCase();

        for (String keyword : BLOCKED_KEYWORDS) {
            if (upperSql.startsWith(keyword)) {
                // 데이터 변경 의도가 있는 SQL → 치명적, 재시도 금지
                return SqlValidationResult.blocked(
                        "DANGEROUS_QUERY",
                        "데이터 변경 쿼리는 실행할 수 없습니다.",
                        Map.of("detectedKeyword", keyword),
                        false
                );
            }
        }

        // SELECT 외(WITH/CTE 등)는 재시도 유도
        if (!upperSql.startsWith("SELECT")) {
            return SqlValidationResult.blocked(
                    "INVALID_QUERY",
                    "SELECT 문만 실행할 수 있습니다.",
                    null,
                    true
            );
        }

        return validateExplainPlan(sql);
    }
}
