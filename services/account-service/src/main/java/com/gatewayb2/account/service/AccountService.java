package com.gatewayb2.account.service;

import com.gatewayb2.account.domain.Account;
import com.gatewayb2.account.domain.PixKey;
import com.gatewayb2.account.messaging.AccountEventPublisher;
import com.gatewayb2.account.repository.AccountRepository;
import com.gatewayb2.account.repository.PixKeyRepository;
import com.gatewayb2.common.domain.PersonType;
import com.gatewayb2.common.domain.PixKeyType;
import com.gatewayb2.common.event.KycStatusChangedEvent;
import com.gatewayb2.common.exception.BusinessException;
import com.gatewayb2.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final PixKeyRepository pixKeyRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountEventPublisher eventPublisher;

    public AccountService(AccountRepository accountRepository, PixKeyRepository pixKeyRepository,
                           AccountNumberGenerator accountNumberGenerator, AccountEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.pixKeyRepository = pixKeyRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void openAccountForApprovedCustomer(KycStatusChangedEvent event) {
        if (accountRepository.existsByCustomerId(event.customerId())) {
            log.info("Conta já existente para o cliente {}, ignorando evento duplicado", event.customerId());
            return;
        }

        String accountNumber;
        do {
            accountNumber = accountNumberGenerator.generate();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.open(event.customerId(), event.personType(), event.document(),
                event.document(), accountNumber, AccountNumberGenerator.DEFAULT_BRANCH,
                com.gatewayb2.common.domain.AccountType.CHECKING);

        PixKeyType keyType = event.personType() == PersonType.INDIVIDUAL ? PixKeyType.CPF : PixKeyType.CNPJ;
        account.addPixKey(PixKey.of(keyType, event.document()));

        accountRepository.save(account);
        log.info("Conta {} aberta automaticamente para o cliente {}", account.getAccountNumber(), event.customerId());
        eventPublisher.publishAccountCreated(account);
    }

    @Transactional
    public PixKey registerPixKey(UUID accountId, PixKeyType type, String value) {
        Account account = findById(accountId);
        if (pixKeyRepository.existsByKeyValue(value)) {
            throw new BusinessException("PIX_KEY_ALREADY_IN_USE", "Chave PIX já está em uso", HttpStatus.CONFLICT);
        }
        PixKey key = PixKey.of(type, value);
        account.addPixKey(key);
        accountRepository.save(account);
        return key;
    }

    @Transactional
    public Account block(UUID accountId) {
        Account account = findById(accountId);
        account.block();
        return account;
    }

    @Transactional
    public Account unblock(UUID accountId) {
        Account account = findById(accountId);
        account.unblock();
        return account;
    }

    @Transactional
    public Account close(UUID accountId) {
        Account account = findById(accountId);
        account.close();
        return account;
    }

    @Transactional(readOnly = true)
    public Account findById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta", accountId));
    }

    @Transactional(readOnly = true)
    public Account findByCustomerId(UUID customerId) {
        return accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta do cliente", customerId));
    }

    @Transactional(readOnly = true)
    public Account findByPixKey(String keyValue) {
        PixKey key = pixKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(() -> new ResourceNotFoundException("Chave PIX", keyValue));
        Account account = key.getAccount();
        account.getPixKeys().size(); // força a inicialização da coleção lazy dentro da transação
        return account;
    }
}
