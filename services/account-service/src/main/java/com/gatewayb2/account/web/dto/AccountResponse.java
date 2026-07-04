package com.gatewayb2.account.web.dto;

import com.gatewayb2.account.domain.Account;
import com.gatewayb2.common.domain.AccountStatus;
import com.gatewayb2.common.domain.AccountType;
import com.gatewayb2.common.domain.PersonType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId,
        PersonType personType,
        String document,
        String holderName,
        String accountNumber,
        String branch,
        AccountType accountType,
        AccountStatus status,
        List<PixKeyResponse> pixKeys,
        Instant createdAt
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getCustomerId(), a.getPersonType(), a.getDocument(), a.getHolderName(),
                a.getAccountNumber(), a.getBranch(), a.getAccountType(), a.getStatus(),
                a.getPixKeys().stream().map(PixKeyResponse::from).toList(), a.getCreatedAt());
    }
}
