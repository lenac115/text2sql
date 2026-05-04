package com.ai.main.controller;

import com.ai.main.dto.order.OrderCreateRequest;
import com.ai.main.dto.order.OrderResponse;
import com.ai.main.dto.order.UpdateOrderStatusRequest;
import com.ai.main.service.OrderService;
import com.ai.main.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
@Tag(name = "06. 주문", description = "주문 생성/조회/취소 + 상태 SSE 구독 + (ADMIN) 상태 변경")
public class OrderController {

    private final OrderService orderService;
    private final SseEmitterService sseEmitterService;

    @PostMapping
    @Operation(summary = "주문 생성 (PAYMENT_PENDING 상태)")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(orderService.createOrder(auth.getName(), request));
    }

    @GetMapping
    @Operation(summary = "내 주문 목록")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문 단건 조회")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            Authentication auth) {
        return ResponseEntity.ok(orderService.getOrder(auth.getName(), orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소 (재고/쿠폰 복원)")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            Authentication auth) {
        return ResponseEntity.ok(orderService.cancelOrder(auth.getName(), orderId));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "[ADMIN] 주문 상태 변경 (상태머신 전이만 허용)")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request.status()));
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "주문 상태 SSE 구독 (event: order-status)")
    public SseEmitter subscribe(Authentication auth) {
        return sseEmitterService.subscribe(auth.getName());
    }
}