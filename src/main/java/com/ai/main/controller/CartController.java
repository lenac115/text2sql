package com.ai.main.controller;

import com.ai.main.dto.cart.*;
import com.ai.main.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication auth) {
        return ResponseEntity.ok(cartService.getCart(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addItem(
            @Valid @RequestBody CartItemRequest request,
            Authentication auth) {
        return ResponseEntity.ok(cartService.addItem(auth.getName(), request));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(cartService.updateQuantity(auth.getName(), itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long itemId,
            Authentication auth) {
        cartService.removeItem(auth.getName(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication auth) {
        cartService.clearCart(auth.getName());
        return ResponseEntity.noContent().build();
    }
}