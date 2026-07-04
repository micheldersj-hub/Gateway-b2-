package com.gatewayb2.kyc.web.dto;

import com.gatewayb2.common.domain.PersonType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CreateCustomerRequest(
        @NotNull PersonType personType,
        @NotBlank String document,
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        @Past LocalDate birthDate
) {
}
