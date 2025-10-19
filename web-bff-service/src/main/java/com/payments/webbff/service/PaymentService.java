package com.payments.webbff.service;

import com.payments.webbff.client.PaymentInitiationClient;
import com.payments.webbff.dto.CreatePaymentInput;
import com.payments.webbff.dto.PaymentDto;
import com.payments.webbff.dto.UpdatePaymentInput;
import com.payments.webbff.type.PaymentStatus;
import com.payments.webbff.type.PaymentType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for Payment operations in the Web BFF.
 *
 * <p>This service handles all payment-related operations, including CRUD operations,
 * filtering, and integration with the Payment Initiation Service.
 *
 * @since PE-414
 */
@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentInitiationClient paymentInitiationClient;

    /**
     * Retrieves a list of payments with optional filtering and pagination.
     *
     * @param tenantId the tenant ID
     * @param businessUnitId optional business unit ID
     * @param status optional payment status filter
     * @param paymentType optional payment type filter
     * @param limit maximum number of results
     * @param offset offset for pagination
     * @return list of payments
     */
    @Cacheable(value = "payments", key = "#tenantId + '_' + #businessUnitId + '_' + #status + '_' + #paymentType + '_' + #limit + '_' + #offset")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentsFallback")
    @Retry(name = "payment-service")
    public List<PaymentDto> getPayments(
            UUID tenantId,
            UUID businessUnitId,
            PaymentStatus status,
            PaymentType paymentType,
            Integer limit,
            Integer offset) {
        
        log.debug("Retrieving payments for tenant: {}, businessUnit: {}, status: {}, type: {}, limit: {}, offset: {}", 
                tenantId, businessUnitId, status, paymentType, limit, offset);
        
        return paymentInitiationClient.getPayments(
                tenantId,
                businessUnitId,
                status,
                paymentType,
                limit,
                offset
        );
    }

    /**
     * Retrieves a specific payment by ID.
     *
     * @param id the payment ID
     * @return the payment or null if not found
     */
    @Cacheable(value = "payment", key = "#id")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentFallback")
    @Retry(name = "payment-service")
    public PaymentDto getPayment(UUID id) {
        log.debug("Retrieving payment with ID: {}", id);
        return paymentInitiationClient.getPayment(id);
    }

    /**
     * Creates a new payment.
     *
     * @param input the payment creation input
     * @return the created payment
     */
    @CircuitBreaker(name = "payment-service", fallbackMethod = "createPaymentFallback")
    @Retry(name = "payment-service")
    public PaymentDto createPayment(CreatePaymentInput input) {
        log.debug("Creating payment with reference: {}", input.getTransactionReference());
        return paymentInitiationClient.createPayment(input);
    }

    /**
     * Updates an existing payment.
     *
     * @param id the payment ID
     * @param input the payment update input
     * @return the updated payment
     */
    @CircuitBreaker(name = "payment-service", fallbackMethod = "updatePaymentFallback")
    @Retry(name = "payment-service")
    public PaymentDto updatePayment(UUID id, UpdatePaymentInput input) {
        log.debug("Updating payment with ID: {}", id);
        return paymentInitiationClient.updatePayment(id, input);
    }

    /**
     * Cancels a payment.
     *
     * @param id the payment ID
     * @return the cancelled payment
     */
    @CircuitBreaker(name = "payment-service", fallbackMethod = "cancelPaymentFallback")
    @Retry(name = "payment-service")
    public PaymentDto cancelPayment(UUID id) {
        log.debug("Cancelling payment with ID: {}", id);
        return paymentInitiationClient.cancelPayment(id);
    }

    // Fallback methods for circuit breaker
    public List<PaymentDto> getPaymentsFallback(
            UUID tenantId,
            UUID businessUnitId,
            PaymentStatus status,
            PaymentType paymentType,
            Integer limit,
            Integer offset,
            Exception ex) {
        log.error("Fallback: Unable to retrieve payments for tenant: {}", tenantId, ex);
        return List.of(); // Return empty list as fallback
    }

    public PaymentDto getPaymentFallback(UUID id, Exception ex) {
        log.error("Fallback: Unable to retrieve payment with ID: {}", id, ex);
        return null; // Return null as fallback
    }

    public PaymentDto createPaymentFallback(CreatePaymentInput input, Exception ex) {
        log.error("Fallback: Unable to create payment with reference: {}", input.getTransactionReference(), ex);
        throw new RuntimeException("Payment service is currently unavailable", ex);
    }

    public PaymentDto updatePaymentFallback(UUID id, UpdatePaymentInput input, Exception ex) {
        log.error("Fallback: Unable to update payment with ID: {}", id, ex);
        throw new RuntimeException("Payment service is currently unavailable", ex);
    }

    public PaymentDto cancelPaymentFallback(UUID id, Exception ex) {
        log.error("Fallback: Unable to cancel payment with ID: {}", id, ex);
        throw new RuntimeException("Payment service is currently unavailable", ex);
    }
}
