package com.ai.main.service;

import com.ai.main.agent.SqlExecutor;
import com.ai.main.agent.SqlGeneratorAgent;
import com.ai.main.agent.SqlValidator;
import com.ai.main.dto.query.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

    private static final int MAX_RETRIES = 2;

    private final SqlExecutor sqlExecutor;
    private final SqlGeneratorAgent sqlGeneratorAgent;
    private final SqlValidator sqlValidator;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public QueryResponse processQuery(String question) {

        // 1. 캐시 확인
        try {
            Object cached = redisTemplate.opsForValue().get(question);
            if (cached != null) return objectMapper.convertValue(cached, QueryResponse.class);
        } catch (Exception ex) {
            log.warn("캐시 조회 실패, LLM 호출 진행 : {}" , ex.getMessage());
        }

        // 2. SQL 생성 + 검증 (재시도 가능한 차단은 피드백 후 재생성)
        String sql = sqlGeneratorAgent.generateSql(question);
        SqlValidationResult result = sqlValidator.validate(sql);

        for (int attempt = 1; attempt <= MAX_RETRIES && !result.allowed() && result.retryable(); attempt++) {
            log.warn("SQL 검증 차단 — 재시도 {}/{}. code={}, sql={}",
                    attempt, MAX_RETRIES, result.code(), sql);
            sql = sqlGeneratorAgent.regenerateSql(question, sql, result.message());
            result = sqlValidator.validate(sql);
        }

        if (!result.allowed()) {
            log.warn("SQL 최종 차단. code={}, retryable={}", result.code(), result.retryable());
            return new QueryResponse(false, null,
                    new QueryError(result.code(), result.message(), result.detail()));
        }

        // 3. 실행
        ColumnRowData data = sqlExecutor.execute(sql);

        // 4. 응답 조립
        QueryResponse response = buildResponse(new QueryExecutionContext(question, sql, data, data.elapsed(), false));

        // 5. 캐싱
        redisTemplate.opsForValue().set(question, response, Duration.ofMinutes(10));

        return response;
    }

    private QueryResponse buildResponse(QueryExecutionContext context) {
        String summary = sqlGeneratorAgent.summarize(
                context.question(),
                context.data().columnRowDataList()
        );

        QueryResultData resultData = new QueryResultData(
                summary,
                context.generatedSql(),
                extractColumns(context.data()),
                context.data().columnRowDataList(),
                new QueryMetaData(
                        context.executionTimeMs(),
                        context.data().columnRowDataList().size(),
                        context.cached()
                )
        );

        return new QueryResponse(true, resultData, null);
    }

    private List<String> extractColumns(ColumnRowData data) {
        List<Map<String, Object>> rows = data.columnRowDataList();
        if (rows.isEmpty()) return List.of();
        return new ArrayList<>(rows.get(0).keySet());
    }
}
