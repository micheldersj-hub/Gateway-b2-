package com.gatewayb2.account.web.dto;

import com.gatewayb2.common.domain.PixKeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterPixKeyRequest(
        @NotNull PixKeyType keyType,
        @NotBlank String keyValue
) {
}
