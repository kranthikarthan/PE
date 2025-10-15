package com.payments.paymentinitiation.service;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.contracts.payment.PaymentStatus;
import com.payments.domain.payment.Payment;
import com.payments.domain.payment.PaymentId;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment Initiation Service
 * 
 * Handles payment initiation business logic:
 * - Validates payment requests
 * - Enforces business rules
 * - Manages idempotency
 * - Emits domain events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentInitiationService {

    private final PaymentRepositoryPort paymentRepository;
    private final IdempotencyService idempotencyService;
    private final PaymentDomainService paymentDomainService;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Initiate a new payment
     * 
     * @param request Payment initiation request
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @return Payment initiation response
     */
    @Transactional
    public PaymentInitiationResponse initiatePayment(
            PaymentInitiationRequest request,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Processing payment initiation for payment: {}", request.getPaymentId());
        
        // Check idempotency
        if (idempotencyService.isDuplicate(request.getIdempotencyKey(), tenantId)) {
            log.warn("Duplicate payment request detected for idempotency key: {}", 
                    request.getIdempotencyKey());
            throw new IllegalArgumentException("Duplicate payment request");
        }
        
        // Create tenant context
        TenantContext tenantContext = TenantContext.builder()
                .tenantId(tenantId)
                .businessUnitId(businessUnitId)
                .build();
        
        // Create domain objects
        AccountNumber sourceAccount = new AccountNumber(request.getSourceAccount());
        AccountNumber destinationAccount = new AccountNumber(request.getDestinationAccount());
        Money amount = request.getAmount();
        
        // Business validation
        validatePaymentRequest(request, sourceAccount, destinationAccount);
        
        // Create payment aggregate
        Payment payment = Payment.initiate(
                request.getPaymentId(),
                request.getIdempotencyKey(),
                sourceAccount,
                destinationAccount,
                amount,
                request.getReference(),
                request.getPaymentType(),
                request.getPriority(),
                tenantContext,
                request.getInitiatedBy()
        );
        
        // Validate business rules
        paymentDomainService.validatePaymentBusinessRules(payment, tenantContext);
        
        // Persist payment
        paymentRepository.save(payment);
        
        // Record idempotency
        idempotencyService.recordIdempotency(request.getIdempotencyKey(), tenantId, payment.getId());
        
        // Publish domain event
        eventPublisher.publishPaymentInitiatedEvent(payment, correlationId);
        
        log.info("Payment initiated successfully: {}", payment.getId());
        
        // Build response
        return PaymentInitiationResponse.builder()
                .paymentId(payment.getId())
                .status(mapToContractStatus(payment.getStatus()))
                .tenantContext(tenantContext)
                .initiatedAt(Instant.now())
                .build();
    }

    /**
     * Get payment status
     * 
     * @param paymentId Payment ID
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @return Payment status response
     */
    @Transactional(readOnly = true)
    public PaymentInitiationResponse getPaymentStatus(
            String paymentId,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Retrieving payment status for: {}", paymentId);
        
        PaymentId domainPaymentId = new PaymentId(paymentId);
        Payment payment = paymentRepository.findById(domainPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Verify tenant access
        if (!payment.getTenantContext().getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }
        
        return PaymentInitiationResponse.builder()
                .paymentId(payment.getId())
                .status(mapToContractStatus(payment.getStatus()))
                .tenantContext(payment.getTenantContext())
                .initiatedAt(payment.getInitiatedAt())
                .build();
    }

    /**
     * Validate payment request business rules
     */
    private void validatePaymentRequest(
            PaymentInitiationRequest request,
            AccountNumber sourceAccount,
            AccountNumber destinationAccount) {
        
        // Amount must be positive
        if (request.getAmount().getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        
        // Source and destination must be different
        if (sourceAccount.equals(destinationAccount)) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }
        
        // Reference must not be empty
        if (request.getReference() == null || request.getReference().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment reference is required");
        }
        
        log.debug("Payment request validation passed for: {}", request.getPaymentId());
    }

    /**
     * Validate payment
     * 
     * @param paymentId Payment ID
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @return Payment validation response
     */
    @Transactional
    public PaymentInitiationResponse validatePayment(
            String paymentId,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Validating payment: {}", paymentId);
        
        PaymentId domainPaymentId = new PaymentId(paymentId);
        Payment payment = paymentRepository.findByIdAndTenantId(domainPaymentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Validate business rules
        paymentDomainService.validatePaymentBusinessRules(payment, payment.getTenantContext());
        
        // Update payment status
        paymentDomainService.processPaymentStatusChange(
                payment, 
                com.payments.domain.payment.PaymentStatus.VALIDATED, 
                "Payment validation successful"
        );
        
        log.info("Payment validated successfully: {}", payment.getId());
        
        return PaymentInitiationResponse.builder()
                .paymentId(payment.getId())
                .status(mapToContractStatus(payment.getStatus()))
                .tenantContext(payment.getTenantContext())
                .initiatedAt(payment.getInitiatedAt())
                .build();
    }

    /**
     * Fail payment
     * 
     * @param paymentId Payment ID
     * @param reason Failure reason
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @return Payment failure response
     */
    @Transactional
    public PaymentInitiationResponse failPayment(
            String paymentId,
            String reason,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Failing payment: {} with reason: {}", paymentId, reason);
        
        PaymentId domainPaymentId = new PaymentId(paymentId);
        Payment payment = paymentRepository.findByIdAndTenantId(domainPaymentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Update payment status
        paymentDomainService.processPaymentStatusChange(
                payment, 
                com.payments.domain.payment.PaymentStatus.FAILED, 
                reason
        );
        
        log.info("Payment failed: {}", payment.getId());
        
        return PaymentInitiationResponse.builder()
                .paymentId(payment.getId())
                .status(mapToContractStatus(payment.getStatus()))
                .tenantContext(payment.getTenantContext())
                .initiatedAt(payment.getInitiatedAt())
                .errorMessage(reason)
                .build();
    }

    /**
     * Complete payment
     * 
     * @param paymentId Payment ID
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @return Payment completion response
     */
    @Transactional
    public PaymentInitiationResponse completePayment(
            String paymentId,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Completing payment: {}", paymentId);
        
        PaymentId domainPaymentId = new PaymentId(paymentId);
        Payment payment = paymentRepository.findByIdAndTenantId(domainPaymentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Update payment status
        paymentDomainService.processPaymentStatusChange(
                payment, 
                com.payments.domain.payment.PaymentStatus.COMPLETED, 
                "Payment completed successfully"
        );
        
        log.info("Payment completed: {}", payment.getId());
        
        return PaymentInitiationResponse.builder()
                .paymentId(payment.getId())
                .status(mapToContractStatus(payment.getStatus()))
                .tenantContext(payment.getTenantContext())
                .initiatedAt(payment.getInitiatedAt())
                .build();
    }

    /**
     * Get payment history
     * 
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param correlationId Correlation ID for tracing
     * @return List of payments
     */
    @Transactional(readOnly = true)
    public java.util.List<PaymentInitiationResponse> getPaymentHistory(
            String tenantId,
            String businessUnitId,
            String correlationId) {
        
        log.info("Retrieving payment history for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        var payments = paymentRepository.findByTenantIdAndBusinessUnitId(
                tenantId, 
                businessUnitId, 
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();
        
        return payments.stream()
                .map(payment -> PaymentInitiationResponse.builder()
                        .paymentId(payment.getId())
                        .status(mapToContractStatus(payment.getStatus()))
                        .tenantContext(payment.getTenantContext())
                        .initiatedAt(payment.getInitiatedAt())
                        .build())
                .toList();
    }

    /**
     * Map domain status to contract status
     */
    private PaymentStatus mapToContractStatus(com.payments.domain.payment.PaymentStatus domainStatus) {
        return switch (domainStatus) {
            case INITIATED -> PaymentStatus.INITIATED;
            case VALIDATED -> PaymentStatus.VALIDATED;
            case SUBMITTED_TO_CLEARING -> PaymentStatus.SUBMITTED_TO_CLEARING;
            case CLEARED -> PaymentStatus.CLEARED;
            case COMPLETED -> PaymentStatus.COMPLETED;
            case FAILED -> PaymentStatus.FAILED;
        };
    }
}
