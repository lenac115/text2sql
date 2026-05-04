package com.ai.main.controller;

import com.ai.main.dto.query.QueryRequest;
import com.ai.main.dto.query.QueryResponse;
import com.ai.main.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/query")
@Tag(name = "10. Text2SQL", description = "[ADMIN] 자연어 → SQL 생성/실행/요약")
public class QueryController {

    private final QueryService queryService;

    @PostMapping("/process")
    @Operation(summary = "[ADMIN] 자연어 쿼리 처리 (실패 시 success=false + error)")
    public ResponseEntity<QueryResponse> processQuery(@RequestBody QueryRequest request) {
        QueryResponse response = queryService.processQuery(request.question());
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}