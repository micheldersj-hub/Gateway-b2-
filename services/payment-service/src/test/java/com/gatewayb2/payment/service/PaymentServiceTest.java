package com.gatewayb2.payment.service;

import com.gatewayb2.common.domain.AccountStatus;
import com.gatewayb2.common.domain.PaymentMethod;
import com.gatewayb2.common.domain.PaymentStatus;
import com.gatewayb2.payment.client.AccountClient;
import com.gatewayb2.payment.client.AccountDto;
import com.gatewayb2.payment.client.LedgerClient;
import com.gatewayb2.payment.domain.Payment;
import com.gatewayb2.payment.messaging.PaymentEventPublisher;
import com.gatewayb2.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private AccountClient accountClient;
    private LedgerClient ledgerClient;
    private PaymentEventPublisher eventPublisher;
    private PaymentService paymentService;

    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        accountClient = mock(AccountClient.class);
        ledgerClient = mock(LedgerClient.class);
        eventPublisher = mock(PaymentEventPublisher.class);
        paymentService = new PaymentService(paymentRepository, accountClient, ledgerClient, eventPublisher);

        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountClient.getAccount(accountId))
                .thenReturn(new AccountDto(accountId, UUID.randomUUID(), "52998224725", "Fulano", AccountStatus.ACTIVE));
    }

    @Test
    void failsPaymentWhenBalanceIsInsufficient() {
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(ledgerClient.getAvailableBalance(accountId)).thenReturn(new BigDecimal("10.00"));

        Payment payment = paymentService.sendOutbound(accountId, PaymentMethod.PIX, new BigDecimal("100.00"), "BRL",
                "Fornecedor", "11222333000181", "chave-pix", "key-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureReason()).contains("Saldo insuficiente");
        verify(eventPublisher).publishFailed(payment);
        verify(eventPublisher, never()).publishCompleted(any());
    }

    @Test
    void completesPaymentWhenBalanceIsSufficient() {
        when(paymentRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        when(ledgerClient.getAvailableBalance(accountId)).thenReturn(new BigDecimal("500.00"));

        Payment payment = paymentService.sendOutbound(accountId, PaymentMethod.PIX, new BigDecimal("100.00"), "BRL",
                "Fornecedor", "11222333000181", "chave-pix", "key-2");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(eventPublisher).publishCompleted(payment);
        verify(eventPublisher, never()).publishFailed(any());
    }

    @Test
    void isIdempotentForSameKey() {
        Payment existing = Payment.create(accountId, PaymentMethod.PIX, com.gatewayb2.common.domain.PaymentDirection.OUTBOUND,
                new BigDecimal("50.00"), "BRL", "Fornecedor", null, "chave-pix", "key-3");
        when(paymentRepository.findByIdempotencyKey("key-3")).thenReturn(Optional.of(existing));

        Payment result = paymentService.sendOutbound(accountId, PaymentMethod.PIX, new BigDecimal("50.00"), "BRL",
                "Fornecedor", null, "chave-pix", "key-3");

        assertThat(result).isSameAs(existing);
        verifyNoInteractions(accountClient, ledgerClient);
    }
}
