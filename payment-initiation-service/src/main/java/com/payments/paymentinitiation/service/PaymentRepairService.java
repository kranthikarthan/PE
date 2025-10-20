package com.payments.paymentinitiation.service;

import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.paymentinitiation.api.PaymentRepairController.*;
import com.payments.paymentinitiation.entity.PaymentEntity;
import com.payments.paymentinitiation.entity.PaymentRepairLogEntity;
import com.payments.paymentinitiation.repository.PaymentRepository;
import com.payments.paymentinitiation.repository.PaymentRepairLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment Repair Service
 * 
 * Provides repair and management functionality for payments.
 * Enables operations teams to retry, cancel, and manage failed payments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRepairService {

    private final PaymentRepository paymentRepository;
    private final PaymentRepairLogRepository paymentRepairLogRepository;
    private final PaymentInitiationService paymentInitiationService;

    /**
     * Retry a failed payment
     */
    @Transactional
    public PaymentInitiationResponse retryPayment(String paymentId, RetryRequest retryRequest, 
                                                 String userId, String correlationId, 
                                                 String tenantId, String businessUnitId) {
        log.info("Retrying payment: {} with reason: {} by user: {}", 
                paymentId, retryRequest.getReason(), userId);
        
        // Find the payment
        PaymentEntity payment = paymentRepository.findByPaymentIdAndTenantIdAndBusinessUnitId(
            paymentId, tenantId, businessUnitId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Validate payment can be retried
        if (!canRetryPayment(payment) && !retryRequest.getForceRetry()) {
            throw new IllegalArgumentException("Payment cannot be retried in current state: " + payment.getStatus());
        }
        
        // Log the repair action
        logRepairAction(paymentId, "RETRY", userId, retryRequest.getReason(), "INITIATED");
        
        try {
            // Create a new payment initiation request based on the original payment
            var retryRequestData = createRetryRequestFromPayment(payment, retryRequest);
            
            // Initiate the retry payment
            PaymentInitiationResponse response = paymentInitiationService.initiatePayment(
                retryRequestData, correlationId, tenantId, businessUnitId);
            
            // Update the original payment status
            payment.setStatus("RETRY_INITIATED");
            payment.setLastUpdated(Instant.now());
            paymentRepository.save(payment);
            
            // Log successful retry
            logRepairAction(paymentId, "RETRY", userId, retryRequest.getReason(), "SUCCESS");
            
            log.info("Payment retry initiated successfully: {} -> {}", paymentId, response.getPaymentId());
            return response;
            
        } catch (Exception e) {
            log.error("Failed to retry payment: {}", paymentId, e);
            logRepairAction(paymentId, "RETRY", userId, retryRequest.getReason(), "FAILED: " + e.getMessage());
            throw new RuntimeException("Payment retry failed", e);
        }
    }

    /**
     * Cancel a payment
     */
    @Transactional
    public PaymentInitiationResponse cancelPayment(String paymentId, CancelRequest cancelRequest, 
                                                 String userId, String correlationId, 
                                                 String tenantId, String businessUnitId) {
        log.info("Cancelling payment: {} with reason: {} by user: {}", 
                paymentId, cancelRequest.getReason(), userId);
        
        // Find the payment
        PaymentEntity payment = paymentRepository.findByPaymentIdAndTenantIdAndBusinessUnitId(
            paymentId, tenantId, businessUnitId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Validate payment can be cancelled
        if (!canCancelPayment(payment) && !cancelRequest.getForceCancel()) {
            throw new IllegalArgumentException("Payment cannot be cancelled in current state: " + payment.getStatus());
        }
        
        // Log the repair action
        logRepairAction(paymentId, "CANCEL", userId, cancelRequest.getReason(), "INITIATED");
        
        try {
            // Update payment status to cancelled
            payment.setStatus("CANCELLED");
            payment.setLastUpdated(Instant.now());
            paymentRepository.save(payment);
            
            // Log successful cancellation
            logRepairAction(paymentId, "CANCEL", userId, cancelRequest.getReason(), "SUCCESS");
            
            // Create response
            PaymentInitiationResponse response = PaymentInitiationResponse.builder()
                .paymentId(paymentId)
                .status("CANCELLED")
                .message("Payment cancelled successfully")
                .build();
            
            log.info("Payment cancelled successfully: {}", paymentId);
            return response;
            
        } catch (Exception e) {
            log.error("Failed to cancel payment: {}", paymentId, e);
            logRepairAction(paymentId, "CANCEL", userId, cancelRequest.getReason(), "FAILED: " + e.getMessage());
            throw new RuntimeException("Payment cancellation failed", e);
        }
    }

    /**
     * Get payment repair history
     */
    public List<RepairHistoryItem> getPaymentRepairHistory(String paymentId, String correlationId, 
                                                          String tenantId, String businessUnitId) {
        log.info("Retrieving repair history for payment: {}", paymentId);
        
        // Verify payment exists
        paymentRepository.findByPaymentIdAndTenantIdAndBusinessUnitId(paymentId, tenantId, businessUnitId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        // Get repair history
        List<PaymentRepairLogEntity> repairLogs = paymentRepairLogRepository.findByPaymentIdOrderByTimestampDesc(paymentId);
        
        return repairLogs.stream()
            .map(this::mapToRepairHistoryItem)
            .collect(Collectors.toList());
    }

    /**
     * Get failed payments for repair
     */
    public List<PaymentInitiationResponse> getFailedPayments(int page, int size, String correlationId, 
                                                           String tenantId, String businessUnitId) {
        log.info("Retrieving failed payments for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        PageRequest pageRequest = PageRequest.of(page, size);
        List<PaymentEntity> failedPayments = paymentRepository.findByTenantIdAndBusinessUnitIdAndStatusIn(
            tenantId, businessUnitId, List.of("FAILED", "TIMEOUT", "ERROR"), pageRequest);
        
        return failedPayments.stream()
            .map(this::mapToPaymentResponse)
            .collect(Collectors.toList());
    }

    /**
     * Bulk retry failed payments
     */
    @Transactional
    public BulkRetryResponse bulkRetryPayments(BulkRetryRequest bulkRetryRequest, String userId, 
                                              String correlationId, String tenantId, String businessUnitId) {
        log.info("Bulk retrying {} payments by user: {}", bulkRetryRequest.getPaymentIds().size(), userId);
        
        int totalProcessed = 0;
        int successCount = 0;
        int failureCount = 0;
        List<String> failedPaymentIds = new java.util.ArrayList<>();
        
        for (String paymentId : bulkRetryRequest.getPaymentIds()) {
            try {
                RetryRequest retryRequest = RetryRequest.builder()
                    .reason(bulkRetryRequest.getReason())
                    .forceRetry(bulkRetryRequest.getForceRetry())
                    .build();
                
                retryPayment(paymentId, retryRequest, userId, correlationId, tenantId, businessUnitId);
                successCount++;
                
            } catch (Exception e) {
                log.error("Failed to retry payment in bulk: {}", paymentId, e);
                failedPaymentIds.add(paymentId);
                failureCount++;
            }
            totalProcessed++;
        }
        
        log.info("Bulk retry completed: {} processed, {} success, {} failures", 
                totalProcessed, successCount, failureCount);
        
        return BulkRetryResponse.builder()
            .totalProcessed(totalProcessed)
            .successCount(successCount)
            .failureCount(failureCount)
            .failedPaymentIds(failedPaymentIds)
            .build();
    }

    /**
     * Check if payment can be retried
     */
    private boolean canRetryPayment(PaymentEntity payment) {
        return List.of("FAILED", "TIMEOUT", "ERROR", "RETRY_INITIATED").contains(payment.getStatus());
    }

    /**
     * Check if payment can be cancelled
     */
    private boolean canCancelPayment(PaymentEntity payment) {
        return List.of("PENDING", "PROCESSING", "VALIDATED").contains(payment.getStatus());
    }

    /**
     * Create retry request from original payment
     */
    private com.payments.contracts.payment.PaymentInitiationRequest createRetryRequestFromPayment(
            PaymentEntity payment, RetryRequest retryRequest) {
        return com.payments.contracts.payment.PaymentInitiationRequest.builder()
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .debtorAccount(payment.getDebtorAccount())
            .creditorAccount(payment.getCreditorAccount())
            .creditorName(payment.getCreditorName())
            .paymentReference(payment.getPaymentReference())
            .remittanceInformation(payment.getRemittanceInformation())
            .build();
    }

    /**
     * Log repair action
     */
    private void logRepairAction(String paymentId, String action, String userId, String reason, String result) {
        try {
            PaymentRepairLogEntity repairLog = PaymentRepairLogEntity.builder()
                .repairId(UUID.randomUUID().toString())
                .paymentId(paymentId)
                .action(action)
                .performedBy(userId)
                .reason(reason)
                .result(result)
                .timestamp(Instant.now())
                .build();
            
            paymentRepairLogRepository.save(repairLog);
            log.info("Logged repair action: {} for payment: {} by user: {}", action, paymentId, userId);
            
        } catch (Exception e) {
            log.error("Failed to log repair action for payment: {}", paymentId, e);
        }
    }

    /**
     * Map repair log entity to history item
     */
    private RepairHistoryItem mapToRepairHistoryItem(PaymentRepairLogEntity repairLog) {
        return RepairHistoryItem.builder()
            .action(repairLog.getAction())
            .timestamp(repairLog.getTimestamp().toString())
            .performedBy(repairLog.getPerformedBy())
            .reason(repairLog.getReason())
            .result(repairLog.getResult())
            .build();
    }

    /**
     * Map payment entity to response
     */
    private PaymentInitiationResponse mapToPaymentResponse(PaymentEntity payment) {
        return PaymentInitiationResponse.builder()
            .paymentId(payment.getPaymentId())
            .status(payment.getStatus())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .debtorAccount(payment.getDebtorAccount())
            .creditorAccount(payment.getCreditorAccount())
            .creditorName(payment.getCreditorName())
            .paymentReference(payment.getPaymentReference())
            .remittanceInformation(payment.getRemittanceInformation())
            .createdAt(payment.getCreatedAt().toString())
            .lastUpdated(payment.getLastUpdated().toString())
            .build();
    }
}
