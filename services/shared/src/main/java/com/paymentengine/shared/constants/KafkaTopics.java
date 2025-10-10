package com.paymentengine.shared.constants;

/**
 * Centralized definition of all Kafka topics used in the Payment Engine.
 * This ensures consistency across all services and prevents topic name mismatches.
 */
public final class KafkaTopics {
    
    // Transaction Events
    public static final String TRANSACTION_CREATED = "payment.transaction.created";
    public static final String TRANSACTION_UPDATED = "payment.transaction.updated";
    public static final String TRANSACTION_COMPLETED = "payment.transaction.completed";
    public static final String TRANSACTION_FAILED = "payment.transaction.failed";
    
    // Account Events
    public static final String ACCOUNT_BALANCE_UPDATED = "payment.account.balance.updated";
    public static final String ACCOUNT_CREATED = "payment.account.created";
    public static final String ACCOUNT_UPDATED = "payment.account.updated";
    public static final String ACCOUNT_CLOSED = "payment.account.closed";
    
    // Notification Events
    public static final String NOTIFICATION_EMAIL = "payment.notification.email";
    public static final String NOTIFICATION_SMS = "payment.notification.sms";
    public static final String NOTIFICATION_PUSH = "payment.notification.push";
    
    // Audit and Monitoring
    public static final String AUDIT_LOG = "payment.audit.log";
    public static final String ERROR_LOG = "payment.error.log";
    public static final String METRICS = "payment.metrics";
    
    // Customer Events
    public static final String CUSTOMER_CREATED = "payment.customer.created";
    public static final String CUSTOMER_UPDATED = "payment.customer.updated";
    public static final String CUSTOMER_KYC_UPDATED = "payment.customer.kyc.updated";
    
    // Security Events
    public static final String SECURITY_LOGIN_ATTEMPT = "payment.security.login.attempt";
    public static final String SECURITY_LOGIN_SUCCESS = "payment.security.login.success";
    public static final String SECURITY_LOGIN_FAILURE = "payment.security.login.failure";
    public static final String SECURITY_FRAUD_DETECTED = "payment.security.fraud.detected";
    
    // System Events
    public static final String SYSTEM_HEALTH_CHECK = "payment.system.health.check";
    public static final String SYSTEM_CONFIG_UPDATED = "payment.system.config.updated";
    
    // Payment Type Events
    public static final String PAYMENT_TYPE_CREATED = "payment.type.created";
    public static final String PAYMENT_TYPE_UPDATED = "payment.type.updated";
    public static final String PAYMENT_TYPE_DISABLED = "payment.type.disabled";
    
    // Dead Letter Topics
    public static final String DLT_TRANSACTION = "payment.dlt.transaction";
    public static final String DLT_NOTIFICATION = "payment.dlt.notification";
    public static final String DLT_AUDIT = "payment.dlt.audit";
    
    // Retry Topics
    public static final String RETRY_TRANSACTION = "payment.retry.transaction";
    public static final String RETRY_NOTIFICATION = "payment.retry.notification";

    private KafkaTopics() {
        // Utility class - prevent instantiation
    }

    /**
     * Get all topic names as an array
     */
    public static String[] getAllTopics() {
        return new String[] {
            TRANSACTION_CREATED,
            TRANSACTION_UPDATED,
            TRANSACTION_COMPLETED,
            TRANSACTION_FAILED,
            ACCOUNT_BALANCE_UPDATED,
            ACCOUNT_CREATED,
            ACCOUNT_UPDATED,
            ACCOUNT_CLOSED,
            NOTIFICATION_EMAIL,
            NOTIFICATION_SMS,
            NOTIFICATION_PUSH,
            AUDIT_LOG,
            ERROR_LOG,
            METRICS,
            CUSTOMER_CREATED,
            CUSTOMER_UPDATED,
            CUSTOMER_KYC_UPDATED,
            SECURITY_LOGIN_ATTEMPT,
            SECURITY_LOGIN_SUCCESS,
            SECURITY_LOGIN_FAILURE,
            SECURITY_FRAUD_DETECTED,
            SYSTEM_HEALTH_CHECK,
            SYSTEM_CONFIG_UPDATED,
            PAYMENT_TYPE_CREATED,
            PAYMENT_TYPE_UPDATED,
            PAYMENT_TYPE_DISABLED,
            DLT_TRANSACTION,
            DLT_NOTIFICATION,
            DLT_AUDIT,
            RETRY_TRANSACTION,
            RETRY_NOTIFICATION
        };
    }

    /**
     * Check if a topic is a dead letter topic
     */
    public static boolean isDeadLetterTopic(String topic) {
        return topic != null && topic.startsWith("payment.dlt.");
    }

    /**
     * Check if a topic is a retry topic
     */
    public static boolean isRetryTopic(String topic) {
        return topic != null && topic.startsWith("payment.retry.");
    }

    /**
     * Get the dead letter topic for a given topic
     */
    public static String getDeadLetterTopic(String originalTopic) {
        if (originalTopic == null) {
            return null;
        }
        
        if (originalTopic.contains("transaction")) {
            return DLT_TRANSACTION;
        } else if (originalTopic.contains("notification")) {
            return DLT_NOTIFICATION;
        } else if (originalTopic.contains("audit")) {
            return DLT_AUDIT;
        }
        
        // Default DLT
        return "payment.dlt.default";
    }

    /**
     * Get the retry topic for a given topic
     */
    public static String getRetryTopic(String originalTopic) {
        if (originalTopic == null) {
            return null;
        }
        
        if (originalTopic.contains("transaction")) {
            return RETRY_TRANSACTION;
        } else if (originalTopic.contains("notification")) {
            return RETRY_NOTIFICATION;
        }
        
        // Default retry
        return "payment.retry.default";
    }
}