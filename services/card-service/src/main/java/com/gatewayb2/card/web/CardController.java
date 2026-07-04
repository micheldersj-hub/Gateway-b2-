package com.gatewayb2.card.web;

import com.gatewayb2.card.domain.CardTransaction;
import com.gatewayb2.card.service.CardService;
import com.gatewayb2.common.domain.Money;
import com.gatewayb2.card.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Cartões", description = "Emissão e autorização de transações de cartão")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Emite um novo cartão (virtual ou físico) para uma conta")
    public CardIssuedResponse issue(@Valid @RequestBody IssueCardRequest request) {
        CardService.IssuedCard issued = cardService.issue(request.accountId(), request.cardType(), request.holderName());
        return CardIssuedResponse.from(issued);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Consulta um cartão pelo identificador (dados mascarados)")
    public CardResponse getById(@PathVariable UUID cardId) {
        return CardResponse.from(cardService.findById(cardId));
    }

    @PostMapping("/{cardId}/activate")
    @Operation(summary = "Ativa um cartão físico após confirmação do portador")
    public CardResponse activate(@PathVariable UUID cardId) {
        return CardResponse.from(cardService.activate(cardId));
    }

    @PostMapping("/{cardId}/block")
    @Operation(summary = "Bloqueia um cartão")
    public CardResponse block(@PathVariable UUID cardId) {
        return CardResponse.from(cardService.block(cardId));
    }

    @PostMapping("/{cardId}/unblock")
    @Operation(summary = "Desbloqueia um cartão")
    public CardResponse unblock(@PathVariable UUID cardId) {
        return CardResponse.from(cardService.unblock(cardId));
    }

    @PostMapping("/{cardId}/cancel")
    @Operation(summary = "Cancela definitivamente um cartão")
    public CardResponse cancel(@PathVariable UUID cardId) {
        return CardResponse.from(cardService.cancel(cardId));
    }

    @PostMapping("/{cardId}/authorize")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Simula uma requisição de autorização vinda da rede da bandeira/adquirente")
    public CardTransactionResponse authorize(@PathVariable UUID cardId,
                                              @RequestHeader("Idempotency-Key") String idempotencyKey,
                                              @Valid @RequestBody AuthorizeTransactionRequest request) {
        CardTransaction tx = cardService.authorize(cardId, request.amount(), Money.BRL.getCurrencyCode(),
                request.merchantName(), idempotencyKey);
        return CardTransactionResponse.from(tx);
    }
}
