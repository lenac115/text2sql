package com.ai.main.controller;

import com.ai.main.dto.auth.AuthResponse;
import com.ai.main.dto.auth.LoginRequest;
import com.ai.main.dto.auth.RefreshRequest;
import com.ai.main.dto.auth.RegisterRequest;
import com.ai.main.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Tag(name = "01. 인증", description = "회원가입 / 로그인 / 토큰 갱신 / 로그아웃")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "회원가입 (+ 토큰 발급)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "로그인 (Access 15분 / Refresh 14일)")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Access Token 갱신 (Refresh Token Rotation)")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(userService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 (Redis의 Refresh Token 제거)")
    public ResponseEntity<Void> logout(Authentication authentication) {
        userService.logout(authentication);
        return ResponseEntity.noContent().build();
    }
}