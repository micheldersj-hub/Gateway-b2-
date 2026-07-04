package com.gatewayb2.card.web.dto;

import com.gatewayb2.card.service.CardService;

/**
 * Resposta única da emissão do cartão, contendo o PAN e o CVV em texto claro. Estes dados
 * nunca mais são retornados por nenhum outro endpoint (nem sequer armazenados em claro).
 */
public record CardIssuedResponse(CardResponse card, String pan, String cvv) {
    public static CardIssuedResponse from(CardService.IssuedCard issued) {
        return new CardIssuedResponse(CardResponse.from(issued.card()), issued.pan(), issued.cvv());
    }
}
