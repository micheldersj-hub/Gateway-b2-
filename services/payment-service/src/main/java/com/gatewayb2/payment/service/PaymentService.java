package com.gatewayb2.payment.service;

import com.gatewayb2.common.domain.AccountStatus;
import com.gatewayb2.common.domain.PaymentDirection;
import com.gatewayb2.common.domain.PaymentMethod;
import com.gatewayb2.common.exception.BusinessException;
import com.gatewayb2.common.exception.ResourceNotFoundException;
import com.gatewayb2.payment.client.AccountClient;
import com.gatewayb2.payment.client.AccountDto;
import com.gatewayb2.payment.client.LedgerClient;
import com.gatewayb2.payment.domain.Payment;
import com.gatewayb2.payment.messaging.PaymentEventPublisher;
import com.gatewayb2.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final AccountClient accountClient;
    private final LedgerClient ledgerClient;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository, AccountClient accountClient, LedgerClient ledgerClient,
                           PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.accountClient = accountClient;
        this.ledgerClient = ledgerClient;
        this.eventPublisher = eventPublisher;
    }

    /** Envio de PIX/TED ou pagamento de boleto: dinheiro sai da conta do cliente. */
    @Transactional
    public Payment sendOutbound(UUID accountId, PaymentMethod method, BigDecimal amount, String currency,
                                 String counterpartyName, String counterpartyDocument, String counterpartyIdentifier,
                                 String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> processOutbound(accountId, method, amount, currency, counterpartyName,
                        counterpartyDocument, counterpartyIdentifier, idempotencyKey));
    }

    private Payment processOutbound(UUID accountId, PaymentMethod method, BigDecimal amount, String currency,
                                     String counterpartyName, String counterpartyDocument, String counterpartyIdentifier,
                                     String idempotencyKey) {
        AccountDto account = accountClient.getAccount(accountId);
        if (account.status() != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "A conta não está ativa para realizar pagamentos", HttpStatus.CONFLICT);
        }

        Payment payment = Payment.create(accountId, method, PaymentDirection.OUTBOUND, amount, currency,
                counterpartyName, counterpartyDocument, counterpartyIdentifier, idempotencyKey);
        paymentRepository.save(payment);

        BigDecimal availableBalance = ledgerClient.getAvailableBalance(accountId);
        if (availableBalance.compareTo(amount) < 0) {
            payment.markFailed("Saldo insuficiente: disponível " + availableBalance + ", solicitado " + amount);
            eventPublisher.publishFailed(payment);
            return payment;
        }

        payment.markProcessing();
        boolean settled = simulateRailProcessing(method);
        if (!settled) {
            payment.markFailed("Recusado pelo trilho de pagamento (" + method + ")");
            eventPublisher.publishFailed(payment);
            return payment;
        }

        payment.markCompleted();
        eventPublisher.publishCompleted(payment);
        return payment;
    }

    /** Recebimento simulado (webhook do trilho de pagamento): dinheiro entra na conta do cliente. */
    @Transactional
    public Payment receiveInbound(String destinationPixKey, BigDecimal amount, String currency, String payerName,
                                   String payerDocument, String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> {
                    AccountDto account = accountClient.resolvePixKey(destinationPixKey);
                    Payment payment = Payment.create(account.id(), PaymentMethod.PIX, PaymentDirection.INBOUND, amount,
                            currency, payerName, payerDocument, destinationPixKey, idempotencyKey);
                    payment.markCompleted();
                    paymentRepository.save(payment);
                    eventPublisher.publishCompleted(payment);
                    return payment;
                });
    }

    /** Simula a comunicação com o trilho externo (SPI/Bacen para PIX, STR para TED, arranjo de cobrança para Boleto). */
    private boolean simulateRailProcessing(PaymentMethod method) {
        log.info("Simulando liquidação via trilho externo: {}", method);
        return true;
    }

    @Transactional(readOnly = true)
    public Payment findById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", paymentId));
    }
}
