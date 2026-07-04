package com.gatewayb2.ledger.service;

import com.gatewayb2.common.domain.EntryType;
import com.gatewayb2.ledger.domain.LedgerAccount;

import java.math.BigDecimal;

public record PostingLineCommand(LedgerAccount ledgerAccount, EntryType entryType, BigDecimal amount, String currency) {
}
