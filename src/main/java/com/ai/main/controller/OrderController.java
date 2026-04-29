package com.ai.main.controller;

import com.ai.main.domain.Orders;
import com.ai.main.dto.order.OrderCreateRequest;
import com.ai.main.dto.order.OrderResponse;
import com.ai.main.dto.order.UpdateOrderStatusRequest;
import com.ai.main.service.OrderService;
import com.ai.main.service.SseEmitterService;
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
public class OrderController {

    private final OrderService orderService;
    private final SseEmitterService sseEmitterService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(orderService.createOrder(auth.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            Authentication auth) {
        return ResponseEntity.ok(orderService.getOrder(auth.getName(), orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            Authentication auth) {
        return ResponseEntity.ok(orderService.cancelOrder(auth.getName(), orderId));
    }

    /**
     * 관리자/내부 시스템용 주문 상태 변경 (예: PAID, SHIPPING, DELIVERED, REFUNDED)
     * 실제 운영 시 @PreAuthorize("hasRole('ADMIN')") 추가 필요
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request.status()));
    }

    /**
     * SSE 구독 - 클라이언트는 연결 후 order-status 이벤트를 수신합니다.
     * Authorization: Bearer <accessToken> 헤더 필요
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication auth) {
        return sseEmitterService.subscribe(auth.getName());
    }
}