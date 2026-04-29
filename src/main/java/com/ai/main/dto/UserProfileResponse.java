package com.ai.main.dto;

import com.ai.main.domain.Users;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String role,
        AddressResponse defaultAddress,
        LocalDateTime createdAt
) {
    public static UserProfileResponse from(Users user) {
        return new UserProfileResponse(
                user.getId(), user.getEmail(), user.getName(),
                user.getRole().name(),
                AddressResponse.from(user.getDefaultAddress()),
                user.getCreatedAt()
        );
    }
}