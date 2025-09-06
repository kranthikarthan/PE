package com.paymentengine.middleware.webhook;

import com.paymentengine.middleware.service.NotificationService;
import com.paymentengine.middleware.service.WebhookService;
import com.paymentengine.shared.constants.KafkaTopics;
import com.paymentengine.shared.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka event listener for processing webhook events
 */
@Component
public class WebhookEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebhookEventListener.class);
    
    private final WebhookService webhookService;
    private final NotificationService notificationService;
    
    @Autowired
    public WebhookEventListener(WebhookService webhookService, NotificationService notificationService) {
        this.webhookService = webhookService;
        this.notificationService = notificationService;
    }
    
    /**
     * Handle transaction created events
     */
    @KafkaListener(topics = KafkaTopics.TRANSACTION_CREATED, groupId = "webhook-transaction-created")
    public void handleTransactionCreated(@Payload TransactionCreatedEvent event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       Acknowledgment acknowledgment) {
        logger.info("Processing transaction created webhook: {}", event.getTransactionReference());
        
        try {
            Map<String, Object> webhookPayload = Map.of(
                "eventType", "transaction.created",
                "eventId", event.getEventId().toString(),
                "timestamp", event.getTimestamp().toString(),
                "data", Map.of(
                    "transactionId", event.getTransactionId().toString(),
                    "transactionReference", event.getTransactionReference(),
                    "amount", event.getAmount(),
                    "currencyCode", event.getCurrencyCode(),
                    "fromAccountId", event.getFromAccountId() != null ? event.getFromAccountId().toString() : null,
                    "toAccountId", event.getToAccountId() != null ? event.getToAccountId().toString() : null,
                    "paymentTypeId", event.getPaymentTypeId().toString(),
                    "description", event.getDescription(),
                    "metadata", event.getMetadata()
                )
            );
            
            webhookService.sendWebhooksForEvent("transaction.created", webhookPayload);
            
            acknowledgment.acknowledge();
            logger.debug("Transaction created webhook processed: {}", event.getTransactionReference());
            
        } catch (Exception e) {
            logger.error("Error processing transaction created webhook {}: {}", 
                        event.getTransactionReference(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle transaction completed events
     */
    @KafkaListener(topics = KafkaTopics.TRANSACTION_COMPLETED, groupId = "webhook-transaction-completed")
    public void handleTransactionCompleted(@Payload TransactionCompletedEvent event,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         Acknowledgment acknowledgment) {
        logger.info("Processing transaction completed webhook: {}", event.getTransactionReference());
        
        try {
            Map<String, Object> webhookPayload = Map.of(
                "eventType", "transaction.completed",
                "eventId", event.getEventId().toString(),
                "timestamp", event.getTimestamp().toString(),
                "data", Map.of(
                    "transactionId", event.getTransactionId().toString(),
                    "transactionReference", event.getTransactionReference(),
                    "amount", event.getAmount(),
                    "currencyCode", event.getCurrencyCode(),
                    "fromAccountId", event.getFromAccountId() != null ? event.getFromAccountId().toString() : null,
                    "toAccountId", event.getToAccountId() != null ? event.getToAccountId().toString() : null,
                    "completedAt", event.getCompletedAt().toString(),
                    "confirmationCode", event.getConfirmationCode(),
                    "processingTimeMs", event.getProcessingTimeMs()
                )
            );
            
            webhookService.sendWebhooksForEvent("transaction.completed", webhookPayload);
            
            // Also send customer notifications
            sendCustomerNotifications(event);
            
            acknowledgment.acknowledge();
            logger.debug("Transaction completed webhook processed: {}", event.getTransactionReference());
            
        } catch (Exception e) {
            logger.error("Error processing transaction completed webhook {}: {}", 
                        event.getTransactionReference(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle transaction failed events
     */
    @KafkaListener(topics = KafkaTopics.TRANSACTION_FAILED, groupId = "webhook-transaction-failed")
    public void handleTransactionFailed(@Payload TransactionFailedEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        logger.info("Processing transaction failed webhook: {}", event.getTransactionReference());
        
        try {
            Map<String, Object> webhookPayload = Map.of(
                "eventType", "transaction.failed",
                "eventId", event.getEventId().toString(),
                "timestamp", event.getTimestamp().toString(),
                "data", Map.of(
                    "transactionId", event.getTransactionId().toString(),
                    "transactionReference", event.getTransactionReference(),
                    "failureReason", event.getFailureReason(),
                    "errorCode", event.getErrorCode() != null ? event.getErrorCode() : "",
                    "errorMessage", event.getErrorMessage() != null ? event.getErrorMessage() : "",
                    "failedAt", event.getFailedAt().toString(),
                    "isRetryable", event.isRetryable(),
                    "retryCount", event.getRetryCount() != null ? event.getRetryCount() : 0
                )
            );
            
            webhookService.sendWebhooksForEvent("transaction.failed", webhookPayload);
            
            acknowledgment.acknowledge();
            logger.debug("Transaction failed webhook processed: {}", event.getTransactionReference());
            
        } catch (Exception e) {
            logger.error("Error processing transaction failed webhook {}: {}", 
                        event.getTransactionReference(), e.getMessage(), e);
        }
    }
    
    /**
     * Handle account balance updated events
     */
    @KafkaListener(topics = KafkaTopics.ACCOUNT_BALANCE_UPDATED, groupId = "webhook-balance-updated")
    public void handleAccountBalanceUpdated(@Payload AccountBalanceUpdatedEvent event,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          Acknowledgment acknowledgment) {
        logger.debug("Processing account balance updated webhook: {}", event.getAccountNumber());
        
        try {
            Map<String, Object> webhookPayload = Map.of(
                "eventType", "account.balance.updated",
                "eventId", event.getEventId().toString(),
                "timestamp", event.getTimestamp().toString(),
                "data", Map.of(
                    "accountId", event.getAccountId().toString(),
                    "accountNumber", event.getAccountNumber(),
                    "previousBalance", event.getPreviousBalance(),
                    "newBalance", event.getNewBalance(),
                    "previousAvailableBalance", event.getPreviousAvailableBalance(),
                    "newAvailableBalance", event.getNewAvailableBalance(),
                    "currencyCode", event.getCurrencyCode(),
                    "transactionId", event.getTransactionId() != null ? event.getTransactionId().toString() : null,
                    "updateReason", event.getUpdateReason()
                )
            );
            
            webhookService.sendWebhooksForEvent("account.balance.updated", webhookPayload);
            
            acknowledgment.acknowledge();
            logger.debug("Account balance updated webhook processed: {}", event.getAccountNumber());
            
        } catch (Exception e) {
            logger.error("Error processing account balance updated webhook {}: {}", 
                        event.getAccountNumber(), e.getMessage(), e);
        }
    }
    
    private void sendCustomerNotifications(TransactionCompletedEvent event) {
        try {
            // This would typically look up customer notification preferences
            // For now, we'll create a basic notification
            
            Map<String, Object> transactionData = Map.of(
                "transactionReference", event.getTransactionReference(),
                "amount", event.getAmount(),
                "currencyCode", event.getCurrencyCode(),
                "status", "completed",
                "completedAt", event.getCompletedAt().toString()
            );
            
            // Send notification (email/SMS based on customer preferences)
            notificationService.sendTransactionNotification(
                event.getTransactionId().toString(),
                event.getTransactionReference(),
                "customer@example.com", // Would be retrieved from customer service
                "+1234567890", // Would be retrieved from customer service
                "email", // Would be based on customer preferences
                transactionData
            );
            
        } catch (Exception e) {
            logger.error("Error sending customer notifications for transaction {}: {}", 
                        event.getTransactionReference(), e.getMessage());
        }
    }
}