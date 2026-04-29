package com.ai.main.controller;

import com.ai.main.dto.PaymentRequest;
import com.ai.main.dto.PaymentResponse;
import com.ai.main.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> pay(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request,
            Authentication auth) {
        return ResponseEntity.ok(paymentService.pay(auth.getName(), orderId, request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long orderId,
            Authentication auth) {
        return ResponseEntity.ok(paymentService.getPayment(auth.getName(), orderId));
    }
}