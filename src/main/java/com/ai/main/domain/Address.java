package com.ai.main.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @NotBlank
    private String recipient;

    @NotBlank
    private String phone;

    @NotBlank
    private String zipCode;

    @NotBlank
    private String addressLine1;

    private String addressLine2;
}