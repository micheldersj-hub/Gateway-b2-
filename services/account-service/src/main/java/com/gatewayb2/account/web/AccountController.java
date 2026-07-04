package com.gatewayb2.account.web;

import com.gatewayb2.account.domain.PixKey;
import com.gatewayb2.account.service.AccountService;
import com.gatewayb2.account.web.dto.AccountResponse;
import com.gatewayb2.account.web.dto.PixKeyResponse;
import com.gatewayb2.account.web.dto.RegisterPixKeyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Contas", description = "Gestão de contas digitais e chaves PIX")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Consulta uma conta pelo identificador")
    public AccountResponse getById(@PathVariable UUID accountId) {
        return AccountResponse.from(accountService.findById(accountId));
    }

    @GetMapping("/by-customer/{customerId}")
    @Operation(summary = "Consulta a conta de um cliente")
    public AccountResponse getByCustomer(@PathVariable UUID customerId) {
        return AccountResponse.from(accountService.findByCustomerId(customerId));
    }

    @GetMapping("/pix-keys/{keyValue}")
    @Operation(summary = "Resolve uma chave PIX para a conta destinatária (usado pelo payment-service)")
    public AccountResponse resolvePixKey(@PathVariable String keyValue) {
        return AccountResponse.from(accountService.findByPixKey(keyValue));
    }

    @PostMapping("/{accountId}/pix-keys")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma nova chave PIX (e-mail, telefone ou aleatória) para a conta")
    public PixKeyResponse registerPixKey(@PathVariable UUID accountId, @Valid @RequestBody RegisterPixKeyRequest request) {
        PixKey key = accountService.registerPixKey(accountId, request.keyType(), request.keyValue());
        return PixKeyResponse.from(key);
    }

    @PostMapping("/{accountId}/block")
    @Operation(summary = "Bloqueia uma conta")
    public AccountResponse block(@PathVariable UUID accountId) {
        return AccountResponse.from(accountService.block(accountId));
    }

    @PostMapping("/{accountId}/unblock")
    @Operation(summary = "Desbloqueia uma conta")
    public AccountResponse unblock(@PathVariable UUID accountId) {
        return AccountResponse.from(accountService.unblock(accountId));
    }

    @PostMapping("/{accountId}/close")
    @Operation(summary = "Encerra uma conta")
    public AccountResponse close(@PathVariable UUID accountId) {
        return AccountResponse.from(accountService.close(accountId));
    }
}
