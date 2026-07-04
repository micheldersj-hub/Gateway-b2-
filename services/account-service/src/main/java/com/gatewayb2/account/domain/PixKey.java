package com.gatewayb2.account.domain;

import com.gatewayb2.common.domain.PixKeyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pix_keys", uniqueConstraints = @UniqueConstraint(name = "uk_pix_keys_value", columnNames = "key_value"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PixKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 20)
    private PixKeyType keyType;

    @Column(name = "key_value", nullable = false, length = 180)
    private String keyValue;

    @Column(nullable = false)
    private Instant createdAt;

    public static PixKey of(PixKeyType type, String value) {
        PixKey key = new PixKey();
        key.keyType = type;
        key.keyValue = value;
        key.createdAt = Instant.now();
        return key;
    }
}
