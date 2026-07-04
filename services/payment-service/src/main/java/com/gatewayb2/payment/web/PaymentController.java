package com.gatewayb2.payment.web;

import com.gatewayb2.common.domain.Money;
import com.gatewayb2.common.domain.PaymentMethod;
import com.gatewayb2.payment.client.AccountClient;
import com.gatewayb2.payment.client.AccountDto;
import com.gatewayb2.payment.domain.Payment;
import com.gatewayb2.payment.service.PaymentService;
import com.gatewayb2.payment.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagamentos", description = "PIX, TED e Boleto")
public class PaymentController {

    private final PaymentService paymentService;
    private final AccountClient accountClient;

    public PaymentController(PaymentService paymentService, AccountClient accountClient) {
        this.paymentService = paymentService;
        this.accountClient = accountClient;
    }

    @GetMapping("/pix/lookup/{pixKey}")
    @Operation(summary = "Consulta o titular de uma chave PIX antes de confirmar um envio (simula o DICT)")
    public PixLookupResponse lookupPixKey(@PathVariable String pixKey) {
        AccountDto account = accountClient.resolvePixKey(pixKey);
        return new PixLookupResponse(account.holderName(), account.document());
    }

    @PostMapping("/pix/send")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia um PIX a partir de uma conta da plataforma")
    public PaymentResponse sendPix(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                    @Valid @RequestBody SendPixRequest request) {
        Payment payment = paymentService.sendOutbound(request.accountId(), PaymentMethod.PIX, request.amount(),
                Money.BRL.getCurrencyCode(), request.counterpartyName(), request.counterpartyDocument(),
                request.pixKey(), idempotencyKey);
        return PaymentResponse.from(payment);
    }

    @PostMapping("/pix/receive")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Simula o recebimento de um PIX vindo de outra instituição (webhook do SPI/Bacen)")
    public PaymentResponse receivePix(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                       @Valid @RequestBody ReceivePixRequest request) {
        Payment payment = paymentService.receiveInbound(request.destinationPixKey(), request.amount(),
                Money.BRL.getCurrencyCode(), request.payerName(), request.payerDocument(), idempotencyKey);
        return PaymentResponse.from(payment);
    }

    @PostMapping("/ted/send")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia uma TED a partir de uma conta da plataforma")
    public PaymentResponse sendTed(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                    @Valid @RequestBody SendTedRequest request) {
        Payment payment = paymentService.sendOutbound(request.accountId(), PaymentMethod.TED, request.amount(),
                Money.BRL.getCurrencyCode(), request.counterpartyName(), request.counterpartyDocument(),
                request.destinationDescription(), idempotencyKey);
        return PaymentResponse.from(payment);
    }

    @PostMapping("/boleto/pay")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Paga um boleto a partir de uma conta da plataforma")
    public PaymentResponse payBoleto(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                      @Valid @RequestBody PayBoletoRequest request) {
        Payment payment = paymentService.sendOutbound(request.accountId(), PaymentMethod.BOLETO, request.amount(),
                Money.BRL.getCurrencyCode(), request.recipientName(), null, request.barcode(), idempotencyKey);
        return PaymentResponse.from(payment);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Consulta um pagamento pelo identificador")
    public PaymentResponse getById(@PathVariable UUID paymentId) {
        return PaymentResponse.from(paymentService.findById(paymentId));
    }

    public record PixLookupResponse(String holderName, String document) {
    }
}
