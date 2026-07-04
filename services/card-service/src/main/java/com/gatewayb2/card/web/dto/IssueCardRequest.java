package com.gatewayb2.card.web.dto;

import com.gatewayb2.common.domain.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueCardRequest(
        @NotNull UUID accountId,
        @NotNull CardType cardType,
        @NotBlank String holderName
) {
}
