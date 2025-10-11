package com.paymentengine.paymentprocessing.service;

import com.paymentengine.shared.constants.KafkaTopics;
import com.paymentengine.shared.event.PaymentNotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Notification service for sending emails, SMS, and webhooks
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final JavaMailSender mailSender;
    private final WebClient webClient;
    
    @Value("${payment-engine.notification.email-enabled:true}")
    private boolean emailEnabled;
    
    @Value("${payment-engine.notification.sms-enabled:false}")
    private boolean smsEnabled;
    
    @Value("${payment-engine.notification.webhook-enabled:true}")
    private boolean webhookEnabled;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${notification.sms.api-url:}")
    private String smsApiUrl;
    
    @Value("${notification.sms.api-key:}")
    private String smsApiKey;
    
    @Autowired
    public NotificationService(JavaMailSender mailSender, WebClient.Builder webClientBuilder) {
        this.mailSender = mailSender;
        this.webClient = webClientBuilder
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
    
    /**
     * Listen for email notification events
     */
    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EMAIL, groupId = "notification-email-group")
    public void handleEmailNotification(@Payload PaymentNotificationEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      Acknowledgment acknowledgment) {
        logger.info("Processing email notification: {}", event.getEventId());
        
        try {
            if (emailEnabled) {
                sendEmail(event).join(); // Wait for completion
            } else {
                logger.info("Email notifications are disabled, skipping email: {}", event.getEventId());
            }
            
            acknowledgment.acknowledge();
            logger.debug("Email notification processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing email notification {}: {}", event.getEventId(), e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
    
    /**
     * Listen for SMS notification events
     */
    @KafkaListener(topics = KafkaTopics.NOTIFICATION_SMS, groupId = "notification-sms-group")
    public void handleSmsNotification(@Payload PaymentNotificationEvent event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    Acknowledgment acknowledgment) {
        logger.info("Processing SMS notification: {}", event.getEventId());
        
        try {
            if (smsEnabled) {
                sendSms(event).join(); // Wait for completion
            } else {
                logger.info("SMS notifications are disabled, skipping SMS: {}", event.getEventId());
            }
            
            acknowledgment.acknowledge();
            logger.debug("SMS notification processed successfully: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing SMS notification {}: {}", event.getEventId(), e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
    
    /**
     * Send email notification asynchronously
     */
    @Async
    public CompletableFuture<Void> sendEmail(PaymentNotificationEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Sending email to: {}", event.getRecipient());
                
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(event.getRecipient());
                message.setSubject(event.getSubject());
                message.setText(buildEmailContent(event));
                
                mailSender.send(message);
                
                logger.info("Email sent successfully to: {}", event.getRecipient());
                
            } catch (Exception e) {
                logger.error("Failed to send email to {}: {}", event.getRecipient(), e.getMessage(), e);
                throw new RuntimeException("Email sending failed", e);
            }
        });
    }
    
    /**
     * Send SMS notification asynchronously
     */
    @Async
    public CompletableFuture<Void> sendSms(PaymentNotificationEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                    logger.warn("SMS API URL not configured, skipping SMS to: {}", event.getRecipient());
                    return;
                }
                
                logger.debug("Sending SMS to: {}", event.getRecipient());
                
                Map<String, Object> smsRequest = Map.of(
                    "to", event.getRecipient(),
                    "message", event.getMessage(),
                    "priority", event.getPriority() != null ? event.getPriority() : "MEDIUM"
                );
                
                Mono<String> response = webClient.post()
                    .uri(smsApiUrl)
                    .header("Authorization", "Bearer " + smsApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(smsRequest)
                    .retrieve()
                    .bodyToMono(String.class);
                
                String result = response.block();
                logger.info("SMS sent successfully to: {}, response: {}", event.getRecipient(), result);
                
            } catch (Exception e) {
                logger.error("Failed to send SMS to {}: {}", event.getRecipient(), e.getMessage(), e);
                throw new RuntimeException("SMS sending failed", e);
            }
        });
    }
    
    /**
     * Send webhook notification asynchronously
     */
    @Async
    public CompletableFuture<Void> sendWebhook(String webhookUrl, Map<String, Object> payload, String secret) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!webhookEnabled) {
                    logger.info("Webhook notifications are disabled, skipping webhook to: {}", webhookUrl);
                    return;
                }
                
                logger.debug("Sending webhook to: {}", webhookUrl);
                
                // Generate webhook signature
                String signature = generateWebhookSignature(payload, secret);
                
                Mono<String> response = webClient.post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Signature", signature)
                    .header("X-Webhook-Timestamp", String.valueOf(Instant.now().getEpochSecond()))
                    .header("User-Agent", "PaymentEngine-Webhook/1.0")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class);
                
                String result = response.block();
                logger.info("Webhook sent successfully to: {}, response: {}", webhookUrl, result);
                
            } catch (Exception e) {
                logger.error("Failed to send webhook to {}: {}", webhookUrl, e.getMessage(), e);
                throw new RuntimeException("Webhook sending failed", e);
            }
        });
    }
    
    /**
     * Send transaction completion notification
     */
    public void sendTransactionNotification(String transactionId, String transactionReference, 
                                          String customerEmail, String customerPhone,
                                          String notificationType, Map<String, Object> transactionData) {
        try {
            logger.info("Sending transaction notification for: {}", transactionReference);
            
            // Send email notification
            if ("email".equalsIgnoreCase(notificationType) || "both".equalsIgnoreCase(notificationType)) {
                if (customerEmail != null && !customerEmail.isEmpty()) {
                    PaymentNotificationEvent emailEvent = createEmailNotification(
                        customerEmail, transactionReference, transactionData);
                    sendEmail(emailEvent);
                }
            }
            
            // Send SMS notification
            if ("sms".equalsIgnoreCase(notificationType) || "both".equalsIgnoreCase(notificationType)) {
                if (customerPhone != null && !customerPhone.isEmpty()) {
                    PaymentNotificationEvent smsEvent = createSmsNotification(
                        customerPhone, transactionReference, transactionData);
                    sendSms(smsEvent);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error sending transaction notification for {}: {}", transactionReference, e.getMessage(), e);
        }
    }
    
    /**
     * Send bulk notifications
     */
    @Async
    public CompletableFuture<Void> sendBulkNotifications(List<PaymentNotificationEvent> notifications) {
        return CompletableFuture.runAsync(() -> {
            logger.info("Processing {} bulk notifications", notifications.size());
            
            notifications.parallelStream().forEach(notification -> {
                try {
                    switch (notification.getNotificationType().toUpperCase()) {
                        case "EMAIL":
                            if (emailEnabled) sendEmail(notification).join();
                            break;
                        case "SMS":
                            if (smsEnabled) sendSms(notification).join();
                            break;
                        default:
                            logger.warn("Unknown notification type: {}", notification.getNotificationType());
                    }
                } catch (Exception e) {
                    logger.error("Error sending bulk notification {}: {}", 
                               notification.getEventId(), e.getMessage());
                }
            });
            
            logger.info("Bulk notifications processing completed");
        });
    }
    
    /**
     * Generate webhook signature for security
     */
    public String generateWebhookSignature(Map<String, Object> payload, String secret) {
        try {
            // Convert payload to JSON string
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonPayload = mapper.writeValueAsString(payload);
            
            // Generate HMAC-SHA256 signature
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(jsonPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return "sha256=" + hexString.toString();
            
        } catch (Exception e) {
            logger.error("Error generating webhook signature: {}", e.getMessage(), e);
            return "";
        }
    }
    
    // Helper methods
    
    private String buildEmailContent(PaymentNotificationEvent event) {
        StringBuilder content = new StringBuilder();
        content.append(event.getMessage()).append("\n\n");
        
        if (event.getTransactionReference() != null) {
            content.append("Transaction Reference: ").append(event.getTransactionReference()).append("\n");
        }
        
        if (event.getAmount() != null) {
            content.append("Amount: ").append(event.getCurrencyCode()).append(" ")
                   .append(event.getAmount()).append("\n");
        }
        
        content.append("\nTimestamp: ").append(Instant.now().toString()).append("\n");
        content.append("\n---\nPayment Engine\nDo not reply to this email.");
        
        return content.toString();
    }
    
    private PaymentNotificationEvent createEmailNotification(String email, String transactionReference, 
                                                           Map<String, Object> transactionData) {
        PaymentNotificationEvent event = new PaymentNotificationEvent(
            "EMAIL",
            email,
            "Transaction Notification - " + transactionReference,
            buildTransactionMessage(transactionData)
        );
        
        event.setTransactionReference(transactionReference);
        event.setTemplateData(transactionData);
        
        return event;
    }
    
    private PaymentNotificationEvent createSmsNotification(String phone, String transactionReference, 
                                                         Map<String, Object> transactionData) {
        PaymentNotificationEvent event = new PaymentNotificationEvent(
            "SMS",
            phone,
            "Transaction Alert",
            buildSmsMessage(transactionReference, transactionData)
        );
        
        event.setTransactionReference(transactionReference);
        
        return event;
    }
    
    private String buildTransactionMessage(Map<String, Object> transactionData) {
        String status = (String) transactionData.get("status");
        Object amount = transactionData.get("amount");
        String currency = (String) transactionData.getOrDefault("currencyCode", "USD");
        
        return String.format(
            "Your transaction has been %s. Amount: %s %s. Thank you for using our payment services.",
            status.toLowerCase(),
            currency,
            amount
        );
    }
    
    private String buildSmsMessage(String transactionReference, Map<String, Object> transactionData) {
        String status = (String) transactionData.get("status");
        Object amount = transactionData.get("amount");
        String currency = (String) transactionData.getOrDefault("currencyCode", "USD");
        
        return String.format(
            "Payment Alert: Transaction %s %s. Amount: %s %s. Ref: %s",
            transactionReference,
            status.toLowerCase(),
            currency,
            amount,
            transactionReference
        );
    }
    
    // duplicate removed
}