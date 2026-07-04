package com.gatewayb2.ledger.messaging;

import com.gatewayb2.common.domain.EntryType;
import com.gatewayb2.common.domain.PaymentDirection;
import com.gatewayb2.common.domain.PaymentMethod;
import com.gatewayb2.common.event.PaymentCompletedEvent;
import com.gatewayb2.ledger.domain.LedgerAccount;
import com.gatewayb2.ledger.service.LedgerAccountService;
import com.gatewayb2.ledger.service.PostingLineCommand;
import com.gatewayb2.ledger.service.PostingService;
import com.gatewayb2.ledger.service.SettlementAccounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduz pagamentos liquidados (PIX/TED/Boleto) em lançamentos contábeis balanceados.
 * Regra: em um recebimento (INBOUND) a conta de liquidação do trilho é debitada (entrada de
 * caixa) e a conta do cliente é creditada (aumento do passivo de depósito); em um envio
 * (OUTBOUND) é o inverso.
 */
@Component
public class PaymentCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedListener.class);

    private final LedgerAccountService ledgerAccountService;
    private final PostingService postingService;

    public PaymentCompletedListener(LedgerAccountService ledgerAccountService, PostingService postingService) {
        this.ledgerAccountService = ledgerAccountService;
        this.postingService = postingService;
    }

    @KafkaListener(topics = PaymentCompletedEvent.TOPIC, groupId = "ledger-service")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Evento PaymentCompletedEvent recebido: paymentId={}, method={}, direction={}",
                event.paymentId(), event.method(), event.direction());

        LedgerAccount customerAccount = ledgerAccountService.getOrCreateCustomerDeposit(event.accountId(), event.currency());
        LedgerAccount settlementAccount = ledgerAccountService.getOrCreateSettlement(
                settlementCodeFor(event.method()), settlementNameFor(event.method()), event.currency());

        List<PostingLineCommand> lines = event.direction() == PaymentDirection.INBOUND
                ? List.of(
                        new PostingLineCommand(settlementAccount, EntryType.DEBIT, event.amount(), event.currency()),
                        new PostingLineCommand(customerAccount, EntryType.CREDIT, event.amount(), event.currency()))
                : List.of(
                        new PostingLineCommand(customerAccount, EntryType.DEBIT, event.amount(), event.currency()),
                        new PostingLineCommand(settlementAccount, EntryType.CREDIT, event.amount(), event.currency()));

        String description = "%s %s - %s".formatted(event.method(), event.direction(), event.counterpartyName());
        postingService.post(description, "payment:" + event.paymentId(), "PAYMENT", lines);
    }

    private String settlementCodeFor(PaymentMethod method) {
        return switch (method) {
            case PIX -> SettlementAccounts.PIX;
            case TED -> SettlementAccounts.TED;
            case BOLETO -> SettlementAccounts.BOLETO;
        };
    }

    private String settlementNameFor(PaymentMethod method) {
        return switch (method) {
            case PIX -> "Liquidação PIX (SPI/Bacen)";
            case TED -> "Liquidação TED (STR/Bacen)";
            case BOLETO -> "Liquidação de Boletos";
        };
    }
}
