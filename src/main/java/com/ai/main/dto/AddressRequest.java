package com.ai.main.dto;

import com.ai.main.domain.Address;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String recipient,
        @NotBlank String phone,
        @NotBlank String zipCode,
        @NotBlank String addressLine1,
        String addressLine2
) {
    public Address toEntity() {
        return Address.builder()
                .recipient(recipient)
                .phone(phone)
                .zipCode(zipCode)
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .build();
    }
}