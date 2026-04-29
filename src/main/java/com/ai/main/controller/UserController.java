package com.ai.main.controller;

import com.ai.main.dto.address.AddressRequest;
import com.ai.main.dto.user.PasswordUpdateRequest;
import com.ai.main.dto.user.UserProfileResponse;
import com.ai.main.dto.user.UserUpdateRequest;
import com.ai.main.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(userService.getProfile(auth.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication auth) {
        return ResponseEntity.ok(userService.updateProfile(auth.getName(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody PasswordUpdateRequest request,
            Authentication auth) {
        userService.updatePassword(auth.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/address")
    public ResponseEntity<UserProfileResponse> updateDefaultAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication auth) {
        return ResponseEntity.ok(userService.updateDefaultAddress(auth.getName(), request));
    }
}