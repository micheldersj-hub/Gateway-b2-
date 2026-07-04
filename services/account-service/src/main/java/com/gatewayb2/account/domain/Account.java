package com.gatewayb2.account.domain;

import com.gatewayb2.common.domain.AccountStatus;
import com.gatewayb2.common.domain.AccountType;
import com.gatewayb2.common.domain.PersonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(name = "uk_accounts_number", columnNames = "account_number"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PersonType personType;

    @Column(nullable = false, length = 14)
    private String document;

    @Column(nullable = false, length = 180)
    private String holderName;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 10)
    private String branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PixKey> pixKeys = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static Account open(UUID customerId, PersonType personType, String document, String holderName,
                                String accountNumber, String branch, AccountType accountType) {
        Account account = new Account();
        account.customerId = customerId;
        account.personType = personType;
        account.document = document;
        account.holderName = holderName;
        account.accountNumber = accountNumber;
        account.branch = branch;
        account.accountType = accountType;
        account.status = AccountStatus.ACTIVE;
        Instant now = Instant.now();
        account.createdAt = now;
        account.updatedAt = now;
        return account;
    }

    public void addPixKey(PixKey key) {
        key.setAccount(this);
        this.pixKeys.add(key);
    }

    public void block() {
        this.status = AccountStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void unblock() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
        this.updatedAt = Instant.now();
    }
}
