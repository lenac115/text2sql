package com.ai.main.dto.user;

import com.ai.main.domain.Users;
import com.ai.main.dto.address.AddressResponse;

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