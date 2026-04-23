package com.ai.main.controller;

import com.ai.main.service.QueryService;
import com.ai.main.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/query")
public class QueryController {

    private final QueryService queryService;

    @PostMapping("/process")
    public ResponseEntity<QueryResponse> processQuery(@RequestBody QueryRequest request) {
        QueryResponse response = queryService.processQuery(request.question());

        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
