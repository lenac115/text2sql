package com.ai.main.controller;

import com.ai.main.dto.order.OrderResponse;
import com.ai.main.dto.timedeal.*;
import com.ai.main.service.TimeDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/deals")
@Tag(name = "09. 타임딜", description = "한정 수량 핫딜 (active/upcoming 목록은 Redis 캐시)")
public class TimeDealController {

    private final TimeDealService timeDealService;

    @GetMapping("/active")
    @SecurityRequirements
    @Operation(summary = "진행중 타임딜 목록 (캐시 10s)")
    public ResponseEntity<List<TimeDealResponse>> getActiveDeals() {
        return ResponseEntity.ok(timeDealService.getActiveDeals());
    }

    @GetMapping("/upcoming")
    @SecurityRequirements
    @Operation(summary = "예정 타임딜 목록 (캐시 30s)")
    public ResponseEntity<List<TimeDealResponse>> getUpcomingDeals() {
        return ResponseEntity.ok(timeDealService.getUpcomingDeals());
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "타임딜 단건 조회")
    public ResponseEntity<TimeDealResponse> getDeal(@PathVariable Long id) {
        return ResponseEntity.ok(timeDealService.getDeal(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] 타임딜 생성")
    public ResponseEntity<TimeDealResponse> createDeal(@Valid @RequestBody TimeDealCreateRequest request) {
        return ResponseEntity.ok(timeDealService.createDeal(request));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] 전체 타임딜 목록 (ENDED 포함)")
    public ResponseEntity<List<TimeDealResponse>> getAllDeals() {
        return ResponseEntity.ok(timeDealService.getAllDeals());
    }

    @PostMapping("/{id}/purchase")
    @Operation(summary = "타임딜 구매 (PESSIMISTIC_WRITE 락 + 1인 한도, 즉시 PAID 주문 생성)")
    public ResponseEntity<OrderResponse> purchaseDeal(
            @PathVariable Long id,
            @Valid @RequestBody TimeDealPurchaseRequest request,
            Authentication auth) {
        return ResponseEntity.ok(timeDealService.purchaseDeal(auth.getName(), id, request));
    }
}