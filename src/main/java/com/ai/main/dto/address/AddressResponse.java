package com.ai.main.dto.address;

import com.ai.main.domain.Address;

public record AddressResponse(
        String recipient,
        String phone,
        String zipCode,
        String addressLine1,
        String addressLine2
) {
    public static AddressResponse from(Address address) {
        if (address == null) return null;
        return new AddressResponse(
                address.getRecipient(),
                address.getPhone(),
                address.getZipCode(),
                address.getAddressLine1(),
                address.getAddressLine2()
        );
    }
}