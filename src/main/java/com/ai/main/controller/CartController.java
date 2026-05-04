package com.ai.main.controller;

import com.ai.main.dto.cart.*;
import com.ai.main.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/cart")
@Tag(name = "05. 장바구니", description = "내 장바구니 조회/추가/수정/삭제")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "장바구니 조회 (항목 + 합계)")
    public ResponseEntity<CartResponse> getCart(Authentication auth) {
        return ResponseEntity.ok(cartService.getCart(auth.getName()));
    }

    @PostMapping
    @Operation(summary = "장바구니에 상품 추가 (이미 있으면 수량 합산)")
    public ResponseEntity<CartItemResponse> addItem(
            @Valid @RequestBody CartItemRequest request,
            Authentication auth) {
        return ResponseEntity.ok(cartService.addItem(auth.getName(), request));
    }

    @PatchMapping("/{itemId}")
    @Operation(summary = "장바구니 항목 수량 변경")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(cartService.updateQuantity(auth.getName(), itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "장바구니 항목 단건 삭제")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long itemId,
            Authentication auth) {
        cartService.removeItem(auth.getName(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "장바구니 비우기")
    public ResponseEntity<Void> clearCart(Authentication auth) {
        cartService.clearCart(auth.getName());
        return ResponseEntity.noContent().build();
    }
}