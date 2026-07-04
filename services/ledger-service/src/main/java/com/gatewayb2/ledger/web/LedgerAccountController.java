package com.gatewayb2.ledger.web;

import com.gatewayb2.ledger.domain.LedgerAccount;
import com.gatewayb2.ledger.service.LedgerAccountService;
import com.gatewayb2.ledger.web.dto.BalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger-accounts")
@Tag(name = "Ledger", description = "Consulta de saldo das contas do ledger")
public class LedgerAccountController {

    private final LedgerAccountService ledgerAccountService;

    public LedgerAccountController(LedgerAccountService ledgerAccountService) {
        this.ledgerAccountService = ledgerAccountService;
    }

    @GetMapping("/{ledgerAccountId}/balance")
    @Operation(summary = "Consulta o saldo de uma conta do ledger pelo identificador interno")
    public BalanceResponse getBalance(@PathVariable UUID ledgerAccountId) {
        LedgerAccount account = ledgerAccountService.findById(ledgerAccountId);
        return toResponse(account);
    }

    @GetMapping("/by-external-account/{externalAccountId}/balance")
    @Operation(summary = "Consulta o saldo da conta de depósito de uma conta digital (account-service)")
    public BalanceResponse getBalanceByExternalAccount(@PathVariable UUID externalAccountId) {
        LedgerAccount account = ledgerAccountService.findByExternalAccountId(externalAccountId);
        return toResponse(account);
    }

    private BalanceResponse toResponse(LedgerAccount account) {
        return new BalanceResponse(account.getId(), account.getExternalAccountId(), account.getCurrency(),
                ledgerAccountService.getBalance(account.getId()));
    }
}
