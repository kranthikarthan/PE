package com.payments.paymentinitiation.service;

import com.payments.domain.payment.Payment;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.payment.PaymentStatus;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Payment Domain Service
 * 
 * Handles complex business logic that doesn't belong to a single aggregate:
 * - Business rule validation
 * - Cross-aggregate operations
 * - Domain event coordination
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    private final PaymentRepositoryPort paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    /**
     * Validate payment business rules
     * 
     * @param payment Payment to validate
     * @param tenantContext Tenant context
     */
    public void validatePaymentBusinessRules(Payment payment, TenantContext tenantContext) {
        log.debug("Validating business rules for payment: {}", payment.getId());
        
        // Check daily limits
        validateDailyLimits(payment, tenantContext);
        
        // Check velocity limits
        validateVelocityLimits(payment, tenantContext);
        
        // Check account restrictions
        validateAccountRestrictions(payment, tenantContext);
        
        // Check compliance rules
        validateComplianceRules(payment, tenantContext);
        
        log.debug("Business rules validation passed for payment: {}", payment.getId());
    }

    /**
     * Validate daily payment limits
     */
    private void validateDailyLimits(Payment payment, TenantContext tenantContext) {
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant tomorrow = today.plus(1, ChronoUnit.DAYS);
        
        // Get today's payments for the tenant
        List<Payment> todayPayments = paymentRepository.findByTenantIdAndDateRange(
                tenantContext.getTenantId(), today, tomorrow, 
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();
        
        // Calculate total amount for today
        BigDecimal todayTotal = todayPayments.stream()
                .filter(p -> p.getStatus() != PaymentStatus.FAILED)
                .map(p -> p.getAmount().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Add current payment amount
        BigDecimal newTotal = todayTotal.add(payment.getAmount().getAmount());
        
        // Check daily limit (configurable per tenant)
        BigDecimal dailyLimit = getDailyLimitForTenant(tenantContext.getTenantId());
        if (newTotal.compareTo(dailyLimit) > 0) {
            throw new IllegalArgumentException(
                String.format("Daily payment limit exceeded. Current: %s, Limit: %s", 
                    newTotal, dailyLimit));
        }
        
        log.debug("Daily limit validation passed for payment: {}", payment.getId());
    }

    /**
     * Validate velocity limits (payments per hour)
     */
    private void validateVelocityLimits(Payment payment, TenantContext tenantContext) {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant now = Instant.now();
        
        // Get payments from last hour
        List<Payment> recentPayments = paymentRepository.findByTenantIdAndDateRange(
                tenantContext.getTenantId(), oneHourAgo, now,
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent();
        
        // Count successful payments in last hour
        long paymentCount = recentPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.INITIATED || 
                           p.getStatus() == PaymentStatus.VALIDATED ||
                           p.getStatus() == PaymentStatus.COMPLETED)
                .count();
        
        // Check velocity limit (configurable per tenant)
        int velocityLimit = getVelocityLimitForTenant(tenantContext.getTenantId());
        if (paymentCount >= velocityLimit) {
            throw new IllegalArgumentException(
                String.format("Payment velocity limit exceeded. Count: %d, Limit: %d", 
                    paymentCount, velocityLimit));
        }
        
        log.debug("Velocity limit validation passed for payment: {}", payment.getId());
    }

    /**
     * Validate account restrictions
     */
    private void validateAccountRestrictions(Payment payment, TenantContext tenantContext) {
        // Check if source account is restricted
        if (isAccountRestricted(payment.getSourceAccount(), tenantContext)) {
            throw new IllegalArgumentException("Source account is restricted");
        }
        
        // Check if destination account is restricted
        if (isAccountRestricted(payment.getDestinationAccount(), tenantContext)) {
            throw new IllegalArgumentException("Destination account is restricted");
        }
        
        log.debug("Account restrictions validation passed for payment: {}", payment.getId());
    }

    /**
     * Validate compliance rules
     */
    private void validateComplianceRules(Payment payment, TenantContext tenantContext) {
        // Check if payment amount requires additional compliance checks
        BigDecimal complianceThreshold = getComplianceThreshold(tenantContext.getTenantId());
        if (payment.getAmount().getAmount().compareTo(complianceThreshold) > 0) {
            // Additional compliance checks would be performed here
            log.info("High-value payment requires compliance review: {}", payment.getId());
        }
        
        log.debug("Compliance rules validation passed for payment: {}", payment.getId());
    }

    /**
     * Process payment status change
     * 
     * @param payment Payment to process
     * @param newStatus New status
     * @param reason Reason for status change
     */
    @Transactional
    public void processPaymentStatusChange(Payment payment, PaymentStatus newStatus, String reason) {
        log.info("Processing status change for payment: {} from {} to {}", 
                payment.getId(), payment.getStatus(), newStatus);
        
        // Validate status transition
        validateStatusTransition(payment.getStatus(), newStatus);
        
        // Update payment status
        payment.updateStatus(newStatus, reason);
        
        // Save payment
        paymentRepository.save(payment);
        
        // Publish domain event
        eventPublisher.publishPaymentStatusChangedEvent(payment, newStatus, reason);
        
        log.info("Status change processed for payment: {}", payment.getId());
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        // Define valid status transitions
        boolean isValidTransition = switch (currentStatus) {
            case INITIATED -> newStatus == PaymentStatus.VALIDATED || 
                             newStatus == PaymentStatus.FAILED;
            case VALIDATED -> newStatus == PaymentStatus.SUBMITTED_TO_CLEARING || 
                             newStatus == PaymentStatus.FAILED;
            case SUBMITTED_TO_CLEARING -> newStatus == PaymentStatus.CLEARING || 
                                         newStatus == PaymentStatus.FAILED;
            case CLEARING -> newStatus == PaymentStatus.CLEARED || 
                           newStatus == PaymentStatus.FAILED;
            case CLEARED -> newStatus == PaymentStatus.COMPLETED || 
                           newStatus == PaymentStatus.FAILED;
            case COMPLETED, FAILED -> false; // Terminal states
        };
        
        if (!isValidTransition) {
            throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    /**
     * Get daily limit for tenant
     */
    private BigDecimal getDailyLimitForTenant(String tenantId) {
        // In a real implementation, this would be retrieved from tenant configuration
        return BigDecimal.valueOf(1000000.00); // 1M ZAR default
    }

    /**
     * Get velocity limit for tenant
     */
    private int getVelocityLimitForTenant(String tenantId) {
        // In a real implementation, this would be retrieved from tenant configuration
        return 100; // 100 payments per hour default
    }

    /**
     * Check if account is restricted
     */
    private boolean isAccountRestricted(AccountNumber accountNumber, TenantContext tenantContext) {
        // In a real implementation, this would check against a restricted accounts list
        return false;
    }

    /**
     * Get compliance threshold for tenant
     */
    private BigDecimal getComplianceThreshold(String tenantId) {
        // In a real implementation, this would be retrieved from tenant configuration
        return BigDecimal.valueOf(50000.00); // 50K ZAR default
    }
}
