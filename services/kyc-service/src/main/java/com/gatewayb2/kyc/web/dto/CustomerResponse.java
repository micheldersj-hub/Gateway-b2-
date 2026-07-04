package com.gatewayb2.kyc.web.dto;

import com.gatewayb2.common.domain.KycStatus;
import com.gatewayb2.common.domain.PersonType;
import com.gatewayb2.kyc.domain.Customer;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        PersonType personType,
        String document,
        String name,
        String email,
        String phone,
        LocalDate birthDate,
        KycStatus status,
        Integer riskScore,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getPersonType(), c.getDocument(), c.getName(), c.getEmail(),
                c.getPhone(), c.getBirthDate(), c.getStatus(), c.getRiskScore(), c.getRejectionReason(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
