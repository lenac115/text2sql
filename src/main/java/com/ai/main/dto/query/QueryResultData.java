package com.ai.main.dto.query;

import java.util.List;
import java.util.Map;

public record QueryResultData(
        String summary,
        String generatedSql,
        List<String> columns,
        List<Map<String, Object>> rows,
        QueryMetaData metadata
) { }
