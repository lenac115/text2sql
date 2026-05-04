package com.ai.main.controller;

import com.ai.main.dto.address.AddressRequest;
import com.ai.main.dto.user.PasswordUpdateRequest;
import com.ai.main.dto.user.UserProfileResponse;
import com.ai.main.dto.user.UserUpdateRequest;
import com.ai.main.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
@Tag(name = "02. 사용자", description = "내 프로필 / 비밀번호 / 기본 배송지")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(userService.getProfile(auth.getName()));
    }

    @PutMapping("/me")
    @Operation(summary = "프로필(이름) 변경")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(userService.updateProfile(auth.getName(), request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "비밀번호 변경")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request,
            Authentication auth) {
        userService.updatePassword(auth, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/address")
    @Operation(summary = "기본 배송지 등록/수정")
    public ResponseEntity<UserProfileResponse> updateDefaultAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication auth) {
        return ResponseEntity.ok(userService.updateDefaultAddress(auth.getName(), request));
    }
}