package com.gatewayb2.ledger.service;

/** Códigos das contas internas de liquidação com cada trilho de pagamento. */
public final class SettlementAccounts {

    public static final String PIX = "PIX_SETTLEMENT";
    public static final String TED = "TED_SETTLEMENT";
    public static final String BOLETO = "BOLETO_SETTLEMENT";
    public static final String CARD = "CARD_SETTLEMENT";

    private SettlementAccounts() {
    }
}
