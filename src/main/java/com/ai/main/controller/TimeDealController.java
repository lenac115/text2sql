package com.ai.main.controller;

import com.ai.main.dto.*;
import com.ai.main.service.TimeDealService;
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
public class TimeDealController {

    private final TimeDealService timeDealService;

    @GetMapping("/active")
    public ResponseEntity<List<TimeDealResponse>> getActiveDeals() {
        return ResponseEntity.ok(timeDealService.getActiveDeals());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<TimeDealResponse>> getUpcomingDeals() {
        return ResponseEntity.ok(timeDealService.getUpcomingDeals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeDealResponse> getDeal(@PathVariable Long id) {
        return ResponseEntity.ok(timeDealService.getDeal(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimeDealResponse> createDeal(@Valid @RequestBody TimeDealCreateRequest request) {
        return ResponseEntity.ok(timeDealService.createDeal(request));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<OrderResponse> purchaseDeal(
            @PathVariable Long id,
            @Valid @RequestBody TimeDealPurchaseRequest request,
            Authentication auth) {
        return ResponseEntity.ok(timeDealService.purchaseDeal(auth.getName(), id, request));
    }
}