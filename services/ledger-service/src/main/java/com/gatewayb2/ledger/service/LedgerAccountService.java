package com.gatewayb2.ledger.service;

import com.gatewayb2.common.exception.ResourceNotFoundException;
import com.gatewayb2.ledger.domain.LedgerAccount;
import com.gatewayb2.ledger.repository.LedgerAccountRepository;
import com.gatewayb2.ledger.repository.PostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LedgerAccountService {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final PostingRepository postingRepository;

    public LedgerAccountService(LedgerAccountRepository ledgerAccountRepository, PostingRepository postingRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.postingRepository = postingRepository;
    }

    @Transactional
    public LedgerAccount getOrCreateCustomerDeposit(UUID externalAccountId, String currency) {
        return ledgerAccountRepository.findByExternalAccountId(externalAccountId)
                .orElseGet(() -> ledgerAccountRepository.save(LedgerAccount.customerDeposit(externalAccountId, currency)));
    }

    @Transactional
    public LedgerAccount getOrCreateSettlement(String code, String name, String currency) {
        return ledgerAccountRepository.findByCode(code)
                .orElseGet(() -> ledgerAccountRepository.save(LedgerAccount.settlement(code, name, currency)));
    }

    @Transactional(readOnly = true)
    public LedgerAccount findById(UUID ledgerAccountId) {
        return ledgerAccountRepository.findById(ledgerAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta do ledger", ledgerAccountId));
    }

    @Transactional(readOnly = true)
    public LedgerAccount findByExternalAccountId(UUID externalAccountId) {
        return ledgerAccountRepository.findByExternalAccountId(externalAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta do ledger para a conta externa", externalAccountId));
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID ledgerAccountId) {
        return postingRepository.computeBalance(ledgerAccountId);
    }
}
