package com.ai.main.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String email,
        String name
) {
    public static AuthResponse of(String accessToken, String refreshToken, String email, String name) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", email, name);
    }
}