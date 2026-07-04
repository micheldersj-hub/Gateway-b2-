package com.gatewayb2.ledger.domain;

public enum LedgerAccountKind {
    /** Conta de depósito de um cliente (passivo do banco) - saldo normal credor. */
    CUSTOMER_DEPOSIT,
    /** Conta interna de liquidação com um trilho de pagamento (ativo do banco) - saldo normal devedor. */
    SETTLEMENT
}
