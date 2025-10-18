-- Flyway Migration V8: Create Notification Service Tables
-- Purpose: Multi-channel notification system with templates, preferences, and audit
-- Date: 2025-10-18

-- 1. Create notification_queue table (pending and sent notifications)
CREATE TABLE notification_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    template_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (
        notification_type IN (
            'PAYMENT_INITIATED', 'PAYMENT_VALIDATED', 'PAYMENT_CLEARED',
            'PAYMENT_FAILED', 'PAYMENT_REVERSED', 'TENANT_ALERT',
            'SYSTEM_NOTIFICATION', 'MARKETING'
        )
    ),
    channel_type VARCHAR(20) NOT NULL CHECK (channel_type IN ('EMAIL', 'SMS', 'PUSH')),
    recipient_address VARCHAR(255) NOT NULL,
    template_data TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RETRY', 'SENT', 'FAILED')),
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts BETWEEN 0 AND 3),
    last_attempt_at TIMESTAMP NULL,
    sent_at TIMESTAMP NULL,
    failure_reason TEXT NULL,
    provider_message_id VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to notification_templates
    CONSTRAINT fk_notification_template FOREIGN KEY (template_id) 
        REFERENCES notification_templates(id) ON DELETE RESTRICT
);

-- 2. Create indexes on notification_queue for optimal query performance
CREATE INDEX idx_notification_tenant_user_created 
    ON notification_queue(tenant_id, user_id, created_at DESC);
    
CREATE INDEX idx_notification_tenant_status_updated 
    ON notification_queue(tenant_id, status, updated_at);
    
CREATE INDEX idx_notification_retry_candidates 
    ON notification_queue(status, last_attempt_at);

-- 3. Enable RLS (Row Level Security) on notification_queue
ALTER TABLE notification_queue ENABLE ROW LEVEL SECURITY;

CREATE POLICY notification_queue_tenant_isolation ON notification_queue
    USING (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR)
    WITH CHECK (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR);

-- 4. Create notification_templates table
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (
        notification_type IN (
            'PAYMENT_INITIATED', 'PAYMENT_VALIDATED', 'PAYMENT_CLEARED',
            'PAYMENT_FAILED', 'PAYMENT_REVERSED', 'TENANT_ALERT',
            'SYSTEM_NOTIFICATION', 'MARKETING'
        )
    ),
    email_template TEXT NULL,
    email_subject VARCHAR(255) NULL,
    sms_template VARCHAR(160) NULL,
    push_template TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_template_tenant_name UNIQUE (tenant_id, name),
    CONSTRAINT check_template_not_empty CHECK (
        (email_template IS NOT NULL) OR (sms_template IS NOT NULL) OR (push_template IS NOT NULL)
    )
);

-- 5. Create indexes on notification_templates
CREATE INDEX idx_template_tenant_type 
    ON notification_templates(tenant_id, notification_type);
    
CREATE INDEX idx_template_tenant_active 
    ON notification_templates(tenant_id, is_active);

-- 6. Enable RLS on notification_templates
ALTER TABLE notification_templates ENABLE ROW LEVEL SECURITY;

CREATE POLICY notification_templates_tenant_isolation ON notification_templates
    USING (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR)
    WITH CHECK (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR);

-- 7. Create template_channels junction table (many-to-many)
CREATE TABLE template_channels (
    template_id UUID NOT NULL,
    channel_type VARCHAR(20) NOT NULL CHECK (channel_type IN ('EMAIL', 'SMS', 'PUSH')),
    
    PRIMARY KEY (template_id, channel_type),
    CONSTRAINT fk_template_channels FOREIGN KEY (template_id) 
        REFERENCES notification_templates(id) ON DELETE CASCADE
);

-- 8. Create notification_preferences table
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    quiet_hours_start TIME NULL,
    quiet_hours_end TIME NULL,
    transaction_alerts_opt_in BOOLEAN NOT NULL DEFAULT true,
    marketing_opt_in BOOLEAN NOT NULL DEFAULT false,
    system_notifications_opt_in BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_preference_tenant_user UNIQUE (tenant_id, user_id)
);

-- 9. Create indexes on notification_preferences
CREATE INDEX idx_preference_tenant_user 
    ON notification_preferences(tenant_id, user_id);

-- 10. Enable RLS on notification_preferences
ALTER TABLE notification_preferences ENABLE ROW LEVEL SECURITY;

CREATE POLICY notification_preferences_tenant_isolation ON notification_preferences
    USING (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR)
    WITH CHECK (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR);

-- 11. Create preference channel collections
CREATE TABLE preference_preferred_channels (
    preference_id UUID NOT NULL,
    channel_type VARCHAR(20) NOT NULL CHECK (channel_type IN ('EMAIL', 'SMS', 'PUSH')),
    
    PRIMARY KEY (preference_id, channel_type),
    CONSTRAINT fk_preferred_channels FOREIGN KEY (preference_id) 
        REFERENCES notification_preferences(id) ON DELETE CASCADE
);

CREATE TABLE preference_unsubscribed_channels (
    preference_id UUID NOT NULL,
    channel_type VARCHAR(20) NOT NULL CHECK (channel_type IN ('EMAIL', 'SMS', 'PUSH')),
    
    PRIMARY KEY (preference_id, channel_type),
    CONSTRAINT fk_unsubscribed_channels FOREIGN KEY (preference_id) 
        REFERENCES notification_preferences(id) ON DELETE CASCADE
);

-- 12. Create notification_channels table (external provider configs)
CREATE TABLE notification_channels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    channel_type VARCHAR(20) NOT NULL CHECK (channel_type IN ('EMAIL', 'SMS', 'PUSH')),
    provider VARCHAR(50) NOT NULL,
    api_key TEXT NOT NULL,
    api_secret TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    retry_policy JSONB NOT NULL DEFAULT '{"maxAttempts": 3, "backoffMs": 1000}',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_channel_tenant_type_provider UNIQUE (tenant_id, channel_type, provider)
);

-- 13. Create indexes on notification_channels
CREATE INDEX idx_channel_tenant_active 
    ON notification_channels(tenant_id, is_active);

-- 14. Enable RLS on notification_channels
ALTER TABLE notification_channels ENABLE ROW LEVEL SECURITY;

CREATE POLICY notification_channels_tenant_isolation ON notification_channels
    USING (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR)
    WITH CHECK (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR);

-- 15. Create audit table for notification events (compliance)
CREATE TABLE notification_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    notification_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_details JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_audit_tenant_created 
    ON notification_audit_log(tenant_id, created_at DESC);

-- 16. Enable RLS on notification_audit_log
ALTER TABLE notification_audit_log ENABLE ROW LEVEL SECURITY;

CREATE POLICY notification_audit_tenant_isolation ON notification_audit_log
    USING (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR)
    WITH CHECK (tenant_id = CURRENT_SETTING('app.tenant_id')::VARCHAR);

-- 17. Insert default notification templates
-- These templates can be customized per tenant
INSERT INTO notification_templates (
    id, tenant_id, name, notification_type, email_template, email_subject, 
    sms_template, push_template, is_active, created_at, updated_at
) VALUES
-- Payment Initiated
(
    '12345678-1234-1234-1234-123456789001'::UUID,
    'system',
    'Payment Initiated',
    'PAYMENT_INITIATED',
    '<html><body><p>Your payment of {{amount}} {{currency}} has been initiated.</p><p>Transaction ID: {{transactionId}}</p></body></html>',
    'Payment Initiated - {{transactionId}}',
    'Payment {{transactionId}} for {{amount}} {{currency}} initiated.',
    '{"title":"Payment Initiated","body":"{{transactionId}}: {{amount}} {{currency}}"}',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Payment Cleared
(
    '12345678-1234-1234-1234-123456789002'::UUID,
    'system',
    'Payment Cleared',
    'PAYMENT_CLEARED',
    '<html><body><p>Your payment of {{amount}} {{currency}} has been cleared successfully.</p><p>Transaction ID: {{transactionId}}</p></body></html>',
    'Payment Cleared - {{transactionId}}',
    'Payment {{transactionId}} for {{amount}} {{currency}} cleared.',
    '{"title":"Payment Cleared","body":"{{transactionId}}: {{amount}} {{currency}}"}',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Payment Failed
(
    '12345678-1234-1234-1234-123456789003'::UUID,
    'system',
    'Payment Failed',
    'PAYMENT_FAILED',
    '<html><body><p>Your payment of {{amount}} {{currency}} has failed.</p><p>Transaction ID: {{transactionId}}</p><p>Reason: {{failureReason}}</p></body></html>',
    'Payment Failed - {{transactionId}}',
    'Payment {{transactionId}} failed: {{failureReason}}',
    '{"title":"Payment Failed","body":"{{failureReason}}"}',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 18. Insert default channels in template_channels
INSERT INTO template_channels (template_id, channel_type)
VALUES
('12345678-1234-1234-1234-123456789001'::UUID, 'EMAIL'),
('12345678-1234-1234-1234-123456789001'::UUID, 'SMS'),
('12345678-1234-1234-1234-123456789001'::UUID, 'PUSH'),
('12345678-1234-1234-1234-123456789002'::UUID, 'EMAIL'),
('12345678-1234-1234-1234-123456789002'::UUID, 'SMS'),
('12345678-1234-1234-1234-123456789002'::UUID, 'PUSH'),
('12345678-1234-1234-1234-123456789003'::UUID, 'EMAIL'),
('12345678-1234-1234-1234-123456789003'::UUID, 'SMS'),
('12345678-1234-1234-1234-123456789003'::UUID, 'PUSH');

-- 19. Create table for tracking notification service operations
CREATE TABLE notification_service_operations (
    operation_id VARCHAR(100) PRIMARY KEY,
    operation_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO notification_service_operations (operation_id, operation_name, description)
VALUES
('OP_QUEUE_NOTIFICATION', 'Queue Notification', 'Queue a new notification for delivery'),
('OP_SEND_NOTIFICATION', 'Send Notification', 'Send notification via channel adapter'),
('OP_RETRY_NOTIFICATION', 'Retry Notification', 'Retry failed notification delivery'),
('OP_UPDATE_PREFERENCES', 'Update Preferences', 'Update user notification preferences'),
('OP_FETCH_HISTORY', 'Fetch History', 'Query notification delivery history');
