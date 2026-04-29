package com.ai.main.dto.query;

import java.util.List;
import java.util.Map;

public record ColumnRowData(
        List<Map<String, Object>> columnRowDataList,
        long elapsed
) {
}
