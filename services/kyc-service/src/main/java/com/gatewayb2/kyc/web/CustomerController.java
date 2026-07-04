package com.gatewayb2.kyc.web;

import com.gatewayb2.kyc.domain.Customer;
import com.gatewayb2.kyc.service.KycService;
import com.gatewayb2.kyc.web.dto.CreateCustomerRequest;
import com.gatewayb2.kyc.web.dto.CustomerResponse;
import com.gatewayb2.kyc.web.dto.ReviewDecisionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "KYC", description = "Onboarding e verificação de identidade de clientes")
public class CustomerController {

    private final KycService kycService;

    public CustomerController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping
    @Operation(summary = "Inicia o onboarding de um novo cliente PF/PJ")
    public ResponseEntity<CustomerResponse> onboard(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = kycService.onboard(request);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + customer.getId()))
                .body(CustomerResponse.from(customer));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Consulta um cliente pelo identificador")
    public CustomerResponse getById(@PathVariable UUID customerId) {
        return CustomerResponse.from(kycService.findById(customerId));
    }

    @PostMapping("/{customerId}/decision")
    @Operation(summary = "Registra a decisão manual de um analista de compliance para um cliente em análise")
    public CustomerResponse decide(@PathVariable UUID customerId, @Valid @RequestBody ReviewDecisionRequest request) {
        return CustomerResponse.from(kycService.decide(customerId, request));
    }
}
