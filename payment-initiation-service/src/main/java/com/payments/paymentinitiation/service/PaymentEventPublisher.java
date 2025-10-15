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
        
        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("payment.payment.initiated.v1")
                .timestamp(Instant.now())
                .correlationId(UUID.fromString(correlationId))
                .source("PaymentInitiationService")
                .version("1.0.0")
                .tenantId(payment.getTenantContext().getTenantId())
                .businessUnitId(payment.getTenantContext().getBusinessUnitId())
                .paymentId(payment.getId())
                .idempotencyKey(payment.getIdempotencyKey())
                .sourceAccount(payment.getSourceAccount().getAccountNumber())
                .destinationAccount(payment.getDestinationAccount().getAccountNumber())
                .amount(payment.getAmount())
                .reference(payment.getReference())
                .paymentType(payment.getPaymentType())
                .priority(payment.getPriority())
                .tenantContext(payment.getTenantContext())
                .initiatedBy(payment.getInitiatedBy())
                .initiatedAt(payment.getInitiatedAt())
                .build();
        
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
        
        PaymentValidatedEvent event = PaymentValidatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("payment.payment.validated.v1")
                .timestamp(Instant.now())
                .correlationId(UUID.fromString(correlationId))
                .source("PaymentInitiationService")
                .version("1.0.0")
                .tenantId(payment.getTenantContext().getTenantId())
                .businessUnitId(payment.getTenantContext().getBusinessUnitId())
                .paymentId(payment.getId())
                .tenantContext(payment.getTenantContext())
                .validatedAt(payment.getValidatedAt())
                .build();
        
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
        
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("payment.payment.failed.v1")
                .timestamp(Instant.now())
                .correlationId(UUID.fromString(correlationId))
                .source("PaymentInitiationService")
                .version("1.0.0")
                .tenantId(payment.getTenantContext().getTenantId())
                .businessUnitId(payment.getTenantContext().getBusinessUnitId())
                .paymentId(payment.getId())
                .tenantContext(payment.getTenantContext())
                .failedAt(payment.getFailedAt())
                .failureReason(reason)
                .build();
        
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
        
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("payment.payment.completed.v1")
                .timestamp(Instant.now())
                .correlationId(UUID.fromString(correlationId))
                .source("PaymentInitiationService")
                .version("1.0.0")
                .tenantId(payment.getTenantContext().getTenantId())
                .businessUnitId(payment.getTenantContext().getBusinessUnitId())
                .paymentId(payment.getId())
                .tenantContext(payment.getTenantContext())
                .completedAt(payment.getCompletedAt())
                .build();
        
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
