package com.gatewayb2.account.web.dto;

import com.gatewayb2.account.domain.PixKey;
import com.gatewayb2.common.domain.PixKeyType;

import java.time.Instant;
import java.util.UUID;

public record PixKeyResponse(UUID id, PixKeyType keyType, String keyValue, Instant createdAt) {
    public static PixKeyResponse from(PixKey key) {
        return new PixKeyResponse(key.getId(), key.getKeyType(), key.getKeyValue(), key.getCreatedAt());
    }
}
