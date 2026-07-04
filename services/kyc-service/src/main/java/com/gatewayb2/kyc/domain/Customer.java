package com.gatewayb2.kyc.domain;

import com.gatewayb2.common.domain.KycStatus;
import com.gatewayb2.common.domain.PersonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(name = "uk_customers_document", columnNames = "document"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PersonType personType;

    @Column(nullable = false, length = 14)
    private String document;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 20)
    private String phone;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus status;

    private Integer riskScore;

    @Column(length = 255)
    private String rejectionReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static Customer create(PersonType personType, String document, String name, String email,
                                   String phone, LocalDate birthDate) {
        Customer customer = new Customer();
        customer.personType = personType;
        customer.document = document;
        customer.name = name;
        customer.email = email;
        customer.phone = phone;
        customer.birthDate = birthDate;
        customer.status = KycStatus.PENDING;
        Instant now = Instant.now();
        customer.createdAt = now;
        customer.updatedAt = now;
        return customer;
    }

    public void transitionTo(KycStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }
}
