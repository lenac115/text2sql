package com.ai.main.dto;

import org.springframework.data.repository.aot.generate.QueryMetadata;

import java.util.List;
import java.util.Map;

public record QueryResultData(
        String summary,           // LLM이 생성한 자연어 요약
        String generatedSql,      // 생성된 SQL (투명성)
        List<String> columns,     // 동적 컬럼명
        List<Map<String, Object>> rows,  // 동적 행 데이터
        QueryMetadata metadata
) { }
