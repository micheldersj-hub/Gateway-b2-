package com.gatewayb2.kyc.service;

import com.gatewayb2.common.domain.KycStatus;
import com.gatewayb2.common.exception.BusinessException;
import com.gatewayb2.common.exception.ResourceNotFoundException;
import com.gatewayb2.common.util.DocumentValidator;
import com.gatewayb2.kyc.domain.Customer;
import com.gatewayb2.kyc.messaging.KycEventPublisher;
import com.gatewayb2.kyc.repository.CustomerRepository;
import com.gatewayb2.kyc.web.dto.CreateCustomerRequest;
import com.gatewayb2.kyc.web.dto.ReviewDecisionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class KycService {

    private final CustomerRepository customerRepository;
    private final RiskScoringEngine riskScoringEngine;
    private final KycEventPublisher eventPublisher;

    public KycService(CustomerRepository customerRepository, RiskScoringEngine riskScoringEngine,
                       KycEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.riskScoringEngine = riskScoringEngine;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Customer onboard(CreateCustomerRequest request) {
        String document = DocumentValidator.onlyDigits(request.document());
        boolean validDocument = switch (request.personType()) {
            case INDIVIDUAL -> DocumentValidator.isValidCpf(document);
            case BUSINESS -> DocumentValidator.isValidCnpj(document);
        };
        if (!validDocument) {
            throw new BusinessException("DOCUMENT_INVALID", "CPF/CNPJ inválido", HttpStatus.BAD_REQUEST);
        }
        if (customerRepository.existsByDocument(document)) {
            throw new BusinessException("DOCUMENT_ALREADY_REGISTERED", "Documento já cadastrado", HttpStatus.CONFLICT);
        }

        Customer customer = Customer.create(request.personType(), document, request.name(), request.email(),
                request.phone(), request.birthDate());
        customerRepository.save(customer);

        runAutomaticScreening(customer);
        return customer;
    }

    private void runAutomaticScreening(Customer customer) {
        KycStatus previous = customer.getStatus();
        customer.transitionTo(KycStatus.IN_REVIEW);
        eventPublisher.publishStatusChanged(customer, previous);

        int score = riskScoringEngine.score(customer.getDocument());
        customer.setRiskScore(score);

        if (score < RiskScoringEngine.AUTO_APPROVE_THRESHOLD) {
            approve(customer, "Aprovação automática: score de risco baixo (" + score + ")");
        } else if (score >= RiskScoringEngine.AUTO_REJECT_THRESHOLD) {
            reject(customer, "Rejeição automática: score de risco elevado (" + score + ")");
        }
        // score intermediário permanece IN_REVIEW aguardando decisão manual do analista de compliance
    }

    @Transactional
    public Customer decide(UUID customerId, ReviewDecisionRequest request) {
        Customer customer = findById(customerId);
        if (customer.getStatus() != KycStatus.IN_REVIEW) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "Somente clientes em análise (IN_REVIEW) podem receber decisão manual", HttpStatus.CONFLICT);
        }
        if (request.decision() == ReviewDecisionRequest.Decision.APPROVE) {
            approve(customer, request.reason());
        } else {
            reject(customer, request.reason());
        }
        return customer;
    }

    private void approve(Customer customer, String reason) {
        KycStatus previous = customer.getStatus();
        customer.transitionTo(KycStatus.APPROVED);
        customer.setRejectionReason(null);
        eventPublisher.publishStatusChanged(customer, previous);
    }

    private void reject(Customer customer, String reason) {
        KycStatus previous = customer.getStatus();
        customer.transitionTo(KycStatus.REJECTED);
        customer.setRejectionReason(reason);
        eventPublisher.publishStatusChanged(customer, previous);
    }

    @Transactional(readOnly = true)
    public Customer findById(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", customerId));
    }
}
