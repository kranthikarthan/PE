package com.payments.paymentinitiation.service;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.contracts.events.PaymentValidatedEvent;
import com.payments.contracts.events.PaymentFailedEvent;
import com.payments.contracts.events.PaymentCompletedEvent;
import com.payments.domain.payment.Payment;
import com.payments.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment Event Publisher
 * 
 * Publishes domain events for payment lifecycle events
 * following the Domain-Driven Design pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish payment initiated event
     * 
     * @param payment Payment that was initiated
     * @param correlationId Correlation ID for tracing
     */
    public void publishPaymentInitiatedEvent(Payment payment, String correlationId) {
        log.info("Publishing payment initiated event for payment: {}", payment.getId());
        
        PaymentInitiatedEvent event = new PaymentInitiatedEvent();
        event.setEventId(UUID.randomUUID());
        event.setEventType("payment.payment.initiated.v1");
        event.setTimestamp(Instant.now());
        event.setCorrelationId(UUID.fromString(correlationId));
        event.setSource("PaymentInitiationService");
        event.setVersion("1.0.0");
        event.setTenantId(payment.getTenantContext().getTenantId());
        event.setBusinessUnitId(payment.getTenantContext().getBusinessUnitId());
        event.setPaymentId(payment.getId());
        event.setIdempotencyKey(payment.getIdempotencyKey());
        event.setSourceAccount(payment.getSourceAccount().getValue());
        event.setDestinationAccount(payment.getDestinationAccount().getValue());
        event.setAmount(payment.getAmount());
        event.setReference(payment.getReference().getValue());
        event.setPaymentType(com.payments.contracts.payment.PaymentType.valueOf(payment.getPaymentType().name()));
        event.setPriority(com.payments.contracts.payment.Priority.valueOf(payment.getPriority().name()));
        event.setTenantContext(payment.getTenantContext());
        event.setInitiatedBy(payment.getInitiatedBy());
        event.setInitiatedAt(payment.getInitiatedAt());
        
        eventPublisher.publishEvent(event);
        log.debug("Payment initiated event published: {}", event.getEventId());
    }

    /**
     * Publish payment validated event
     * 
     * @param payment Payment that was validated
     * @param correlationId Correlation ID for tracing
     */
    public void publishPaymentValidatedEvent(Payment payment, String correlationId) {
        log.info("Publishing payment validated event for payment: {}", payment.getId());
        
        PaymentValidatedEvent event = new PaymentValidatedEvent();
        event.setEventId(UUID.randomUUID());
        event.setEventType("payment.payment.validated.v1");
        event.setTimestamp(Instant.now());
        event.setCorrelationId(UUID.fromString(correlationId));
        event.setSource("PaymentInitiationService");
        event.setVersion("1.0.0");
        event.setTenantId(payment.getTenantContext().getTenantId());
        event.setBusinessUnitId(payment.getTenantContext().getBusinessUnitId());
        event.setPaymentId(payment.getId());
        event.setTenantContext(payment.getTenantContext());
        event.setValidatedAt(payment.getValidatedAt());
        event.setRiskLevel(com.payments.contracts.validation.RiskLevel.LOW); // Default risk level
        event.setFraudScore(0); // Default fraud score
        
        eventPublisher.publishEvent(event);
        log.debug("Payment validated event published: {}", event.getEventId());
    }

    /**
     * Publish payment failed event
     * 
     * @param payment Payment that failed
     * @param reason Failure reason
     * @param correlationId Correlation ID for tracing
     */
    public void publishPaymentFailedEvent(Payment payment, String reason, String correlationId) {
        log.info("Publishing payment failed event for payment: {}", payment.getId());
        
        PaymentFailedEvent event = new PaymentFailedEvent();
        event.setEventId(UUID.randomUUID());
        event.setEventType("payment.payment.failed.v1");
        event.setTimestamp(Instant.now());
        event.setCorrelationId(UUID.fromString(correlationId));
        event.setSource("PaymentInitiationService");
        event.setVersion("1.0.0");
        event.setTenantId(payment.getTenantContext().getTenantId());
        event.setBusinessUnitId(payment.getTenantContext().getBusinessUnitId());
        event.setPaymentId(payment.getId());
        event.setTenantContext(payment.getTenantContext());
        event.setFailedAt(payment.getFailedAt());
        event.setFailureReason(reason);
        
        eventPublisher.publishEvent(event);
        log.debug("Payment failed event published: {}", event.getEventId());
    }

    /**
     * Publish payment completed event
     * 
     * @param payment Payment that was completed
     * @param correlationId Correlation ID for tracing
     */
    public void publishPaymentCompletedEvent(Payment payment, String correlationId) {
        log.info("Publishing payment completed event for payment: {}", payment.getId());
        
        PaymentCompletedEvent event = new PaymentCompletedEvent();
        event.setEventId(UUID.randomUUID());
        event.setEventType("payment.payment.completed.v1");
        event.setTimestamp(Instant.now());
        event.setCorrelationId(UUID.fromString(correlationId));
        event.setSource("PaymentInitiationService");
        event.setVersion("1.0.0");
        event.setTenantId(payment.getTenantContext().getTenantId());
        event.setBusinessUnitId(payment.getTenantContext().getBusinessUnitId());
        event.setPaymentId(payment.getId());
        event.setTenantContext(payment.getTenantContext());
        event.setCompletedAt(payment.getCompletedAt());
        
        eventPublisher.publishEvent(event);
        log.debug("Payment completed event published: {}", event.getEventId());
    }

    /**
     * Publish payment status changed event
     * 
     * @param payment Payment with changed status
     * @param newStatus New payment status
     * @param reason Reason for status change
     */
    public void publishPaymentStatusChangedEvent(Payment payment, PaymentStatus newStatus, String reason) {
        log.info("Publishing status changed event for payment: {} to {}", payment.getId(), newStatus);
        
        // Publish specific event based on status
        switch (newStatus) {
            case VALIDATED -> publishPaymentValidatedEvent(payment, payment.getId().getValue());
            case FAILED -> publishPaymentFailedEvent(payment, reason, payment.getId().getValue());
            case COMPLETED -> publishPaymentCompletedEvent(payment, payment.getId().getValue());
            default -> log.debug("No specific event to publish for status: {}", newStatus);
        }
    }
}
