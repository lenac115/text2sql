package com.ai.main.controller;

import com.ai.main.dto.payment.PaymentRequest;
import com.ai.main.dto.payment.PaymentResponse;
import com.ai.main.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
@Tag(name = "07. 결제", description = "Mock PG 결제 (약 5% 실패율, 실패 시 주문 자동 취소)")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    @Operation(summary = "결제 요청 (응답의 status로 SUCCEEDED/FAILED 판별)")
    public ResponseEntity<PaymentResponse> pay(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request,
            Authentication auth) {
        return ResponseEntity.ok(paymentService.pay(auth.getName(), orderId, request));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "주문의 결제 정보 조회")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long orderId,
            Authentication auth) {
        return ResponseEntity.ok(paymentService.getPayment(auth.getName(), orderId));
    }
}