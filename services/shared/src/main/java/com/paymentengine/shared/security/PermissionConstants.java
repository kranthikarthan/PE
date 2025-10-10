package com.paymentengine.shared.security;

import java.util.List;

/**
 * Constants for permissions used throughout the Payment Engine system
 */
public final class PermissionConstants {
    
    // Transaction permissions
    public static final String TRANSACTION_CREATE = "transaction:create";
    public static final String TRANSACTION_READ = "transaction:read";
    public static final String TRANSACTION_UPDATE = "transaction:update";
    public static final String TRANSACTION_DELETE = "transaction:delete";
    public static final String TRANSACTION_CANCEL = "transaction:cancel";
    public static final String TRANSACTION_REVERSE = "transaction:reverse";
    
    // Account permissions
    public static final String ACCOUNT_CREATE = "account:create";
    public static final String ACCOUNT_READ = "account:read";
    public static final String ACCOUNT_UPDATE = "account:update";
    public static final String ACCOUNT_DELETE = "account:delete";
    public static final String ACCOUNT_CLOSE = "account:close";
    public static final String ACCOUNT_BALANCE_READ = "account:balance:read";
    public static final String ACCOUNT_BALANCE_UPDATE = "account:balance:update";
    
    // Customer permissions
    public static final String CUSTOMER_CREATE = "customer:create";
    public static final String CUSTOMER_READ = "customer:read";
    public static final String CUSTOMER_UPDATE = "customer:update";
    public static final String CUSTOMER_DELETE = "customer:delete";
    public static final String CUSTOMER_KYC_UPDATE = "customer:kyc:update";
    
    // Payment type permissions
    public static final String PAYMENT_TYPE_CREATE = "payment-type:create";
    public static final String PAYMENT_TYPE_READ = "payment-type:read";
    public static final String PAYMENT_TYPE_UPDATE = "payment-type:update";
    public static final String PAYMENT_TYPE_DELETE = "payment-type:delete";
    public static final String PAYMENT_TYPE_ACTIVATE = "payment-type:activate";
    public static final String PAYMENT_TYPE_DEACTIVATE = "payment-type:deactivate";
    
    // User management permissions
    public static final String USER_CREATE = "user:create";
    public static final String USER_READ = "user:read";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_PASSWORD_RESET = "user:password:reset";
    public static final String USER_ROLE_ASSIGN = "user:role:assign";
    
    // Role management permissions
    public static final String ROLE_CREATE = "role:create";
    public static final String ROLE_READ = "role:read";
    public static final String ROLE_UPDATE = "role:update";
    public static final String ROLE_DELETE = "role:delete";
    public static final String ROLE_PERMISSION_ASSIGN = "role:permission:assign";
    
    // System administration permissions
    public static final String SYSTEM_CONFIG_READ = "system:config:read";
    public static final String SYSTEM_CONFIG_UPDATE = "system:config:update";
    public static final String SYSTEM_HEALTH_READ = "system:health:read";
    public static final String SYSTEM_METRICS_READ = "system:metrics:read";
    public static final String SYSTEM_LOGS_READ = "system:logs:read";
    
    // Dashboard permissions
    public static final String DASHBOARD_READ = "dashboard:read";
    public static final String DASHBOARD_ADMIN = "dashboard:admin";
    
    // Audit permissions
    public static final String AUDIT_READ = "audit:read";
    public static final String AUDIT_EXPORT = "audit:export";
    
    // Notification permissions
    public static final String NOTIFICATION_SEND = "notification:send";
    public static final String NOTIFICATION_READ = "notification:read";
    public static final String NOTIFICATION_CONFIG = "notification:config";
    
    // Webhook permissions
    public static final String WEBHOOK_CREATE = "webhook:create";
    public static final String WEBHOOK_READ = "webhook:read";
    public static final String WEBHOOK_UPDATE = "webhook:update";
    public static final String WEBHOOK_DELETE = "webhook:delete";
    
    // API key permissions
    public static final String API_KEY_CREATE = "api-key:create";
    public static final String API_KEY_READ = "api-key:read";
    public static final String API_KEY_UPDATE = "api-key:update";
    public static final String API_KEY_DELETE = "api-key:delete";
    
    // Super admin permission
    public static final String SUPER_ADMIN = "super:admin";
    
    private PermissionConstants() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Get all permissions as a list
     */
    public static List<String> getAllPermissions() {
        return List.of(
            // Transaction permissions
            TRANSACTION_CREATE, TRANSACTION_READ, TRANSACTION_UPDATE, 
            TRANSACTION_DELETE, TRANSACTION_CANCEL, TRANSACTION_REVERSE,
            
            // Account permissions
            ACCOUNT_CREATE, ACCOUNT_READ, ACCOUNT_UPDATE, ACCOUNT_DELETE, 
            ACCOUNT_CLOSE, ACCOUNT_BALANCE_READ, ACCOUNT_BALANCE_UPDATE,
            
            // Customer permissions
            CUSTOMER_CREATE, CUSTOMER_READ, CUSTOMER_UPDATE, 
            CUSTOMER_DELETE, CUSTOMER_KYC_UPDATE,
            
            // Payment type permissions
            PAYMENT_TYPE_CREATE, PAYMENT_TYPE_READ, PAYMENT_TYPE_UPDATE, 
            PAYMENT_TYPE_DELETE, PAYMENT_TYPE_ACTIVATE, PAYMENT_TYPE_DEACTIVATE,
            
            // User management permissions
            USER_CREATE, USER_READ, USER_UPDATE, USER_DELETE, 
            USER_PASSWORD_RESET, USER_ROLE_ASSIGN,
            
            // Role management permissions
            ROLE_CREATE, ROLE_READ, ROLE_UPDATE, ROLE_DELETE, ROLE_PERMISSION_ASSIGN,
            
            // System administration permissions
            SYSTEM_CONFIG_READ, SYSTEM_CONFIG_UPDATE, SYSTEM_HEALTH_READ, 
            SYSTEM_METRICS_READ, SYSTEM_LOGS_READ,
            
            // Dashboard permissions
            DASHBOARD_READ, DASHBOARD_ADMIN,
            
            // Audit permissions
            AUDIT_READ, AUDIT_EXPORT,
            
            // Notification permissions
            NOTIFICATION_SEND, NOTIFICATION_READ, NOTIFICATION_CONFIG,
            
            // Webhook permissions
            WEBHOOK_CREATE, WEBHOOK_READ, WEBHOOK_UPDATE, WEBHOOK_DELETE,
            
            // API key permissions
            API_KEY_CREATE, API_KEY_READ, API_KEY_UPDATE, API_KEY_DELETE,
            
            // Super admin
            SUPER_ADMIN
        );
    }
    
    /**
     * Get permissions by category
     */
    public static List<String> getTransactionPermissions() {
        return List.of(
            TRANSACTION_CREATE, TRANSACTION_READ, TRANSACTION_UPDATE, 
            TRANSACTION_DELETE, TRANSACTION_CANCEL, TRANSACTION_REVERSE
        );
    }
    
    public static List<String> getAccountPermissions() {
        return List.of(
            ACCOUNT_CREATE, ACCOUNT_READ, ACCOUNT_UPDATE, ACCOUNT_DELETE, 
            ACCOUNT_CLOSE, ACCOUNT_BALANCE_READ, ACCOUNT_BALANCE_UPDATE
        );
    }
    
    public static List<String> getCustomerPermissions() {
        return List.of(
            CUSTOMER_CREATE, CUSTOMER_READ, CUSTOMER_UPDATE, 
            CUSTOMER_DELETE, CUSTOMER_KYC_UPDATE
        );
    }
    
    public static List<String> getAdminPermissions() {
        return List.of(
            USER_CREATE, USER_READ, USER_UPDATE, USER_DELETE, 
            USER_PASSWORD_RESET, USER_ROLE_ASSIGN,
            ROLE_CREATE, ROLE_READ, ROLE_UPDATE, ROLE_DELETE, ROLE_PERMISSION_ASSIGN,
            SYSTEM_CONFIG_READ, SYSTEM_CONFIG_UPDATE, SYSTEM_HEALTH_READ, 
            SYSTEM_METRICS_READ, SYSTEM_LOGS_READ,
            SUPER_ADMIN
        );
    }
    
    /**
     * Check if a permission is administrative
     */
    public static boolean isAdminPermission(String permission) {
        return getAdminPermissions().contains(permission) || SUPER_ADMIN.equals(permission);
    }
    
    /**
     * Check if a permission is read-only
     */
    public static boolean isReadOnlyPermission(String permission) {
        return permission != null && permission.endsWith(":read");
    }
}