-- Migration: Add Fraud API Toggle Configurations
-- Description: Creates table for managing fraud API enable/disable at different levels
--              Supports tenant, payment type, local instrument, and clearing system levels
-- Version: 009
-- Date: 2024-01-15

-- Create fraud_api_toggle_configurations table
CREATE TABLE IF NOT EXISTS payment_engine.fraud_api_toggle_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50),
    local_instrumentation_code VARCHAR(50),
    clearing_system_code VARCHAR(50),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    enabled_reason VARCHAR(500),
    disabled_reason VARCHAR(500),
    effective_from TIMESTAMP WITH TIME ZONE,
    effective_until TIMESTAMP WITH TIME ZONE,
    priority INTEGER NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_fraud_toggle_effective_times CHECK (
        effective_from IS NULL OR effective_until IS NULL OR effective_from <= effective_until
    ),
    CONSTRAINT chk_fraud_toggle_priority CHECK (priority > 0),
    CONSTRAINT chk_fraud_toggle_reason CHECK (
        (is_enabled = TRUE AND enabled_reason IS NOT NULL AND disabled_reason IS NULL) OR
        (is_enabled = FALSE AND disabled_reason IS NOT NULL AND enabled_reason IS NULL) OR
        (enabled_reason IS NULL AND disabled_reason IS NULL)
    )
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_tenant_id ON payment_engine.fraud_api_toggle_configurations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_tenant_payment_type ON payment_engine.fraud_api_toggle_configurations(tenant_id, payment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_tenant_payment_local ON payment_engine.fraud_api_toggle_configurations(tenant_id, payment_type, local_instrumentation_code);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_tenant_payment_local_clearing ON payment_engine.fraud_api_toggle_configurations(tenant_id, payment_type, local_instrumentation_code, clearing_system_code);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_enabled ON payment_engine.fraud_api_toggle_configurations(is_enabled);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_active ON payment_engine.fraud_api_toggle_configurations(is_active);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_priority ON payment_engine.fraud_api_toggle_configurations(priority);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_effective_from ON payment_engine.fraud_api_toggle_configurations(effective_from);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_effective_until ON payment_engine.fraud_api_toggle_configurations(effective_until);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_created_at ON payment_engine.fraud_api_toggle_configurations(created_at);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_tenant_active ON payment_engine.fraud_api_toggle_configurations(tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_tenant_enabled ON payment_engine.fraud_api_toggle_configurations(tenant_id, is_enabled);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_active_priority ON payment_engine.fraud_api_toggle_configurations(is_active, priority);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_effective_active ON payment_engine.fraud_api_toggle_configurations(effective_from, effective_until, is_active);

-- Create partial indexes for performance
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_active_enabled ON payment_engine.fraud_api_toggle_configurations(tenant_id, priority) WHERE is_active = true AND is_enabled = true;
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_active_disabled ON payment_engine.fraud_api_toggle_configurations(tenant_id, priority) WHERE is_active = true AND is_enabled = false;
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_currently_effective ON payment_engine.fraud_api_toggle_configurations(tenant_id, priority) 
    WHERE is_active = true 
    AND (effective_from IS NULL OR effective_from <= CURRENT_TIMESTAMP)
    AND (effective_until IS NULL OR effective_until >= CURRENT_TIMESTAMP);
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_future_effective ON payment_engine.fraud_api_toggle_configurations(effective_from) 
    WHERE is_active = true AND effective_from > CURRENT_TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_fraud_toggle_expired ON payment_engine.fraud_api_toggle_configurations(effective_until) 
    WHERE is_active = true AND effective_until < CURRENT_TIMESTAMP;

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_fraud_api_toggle_configurations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_fraud_api_toggle_configurations_updated_at
    BEFORE UPDATE ON payment_engine.fraud_api_toggle_configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_fraud_api_toggle_configurations_updated_at();

-- Insert sample fraud API toggle configurations
INSERT INTO payment_engine.fraud_api_toggle_configurations (
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    is_enabled,
    enabled_reason,
    disabled_reason,
    effective_from,
    effective_until,
    priority,
    is_active,
    created_by,
    updated_by
) VALUES 
-- Tenant-level configurations
(
    'tenant-001',
    NULL,
    NULL,
    NULL,
    TRUE,
    'Default tenant-level fraud API enabled',
    NULL,
    NULL,
    NULL,
    100,
    TRUE,
    'system',
    'system'
),
(
    'tenant-002',
    NULL,
    NULL,
    NULL,
    FALSE,
    NULL,
    'Tenant-level fraud API disabled for maintenance',
    NULL,
    NULL,
    100,
    TRUE,
    'system',
    'system'
),

-- Payment type level configurations
(
    'tenant-001',
    'SEPA_CREDIT_TRANSFER',
    NULL,
    NULL,
    TRUE,
    'SEPA credit transfers require fraud checking',
    NULL,
    NULL,
    NULL,
    90,
    TRUE,
    'system',
    'system'
),
(
    'tenant-001',
    'DOMESTIC_TRANSFER',
    NULL,
    NULL,
    FALSE,
    NULL,
    'Domestic transfers below threshold, fraud API disabled',
    NULL,
    NULL,
    90,
    TRUE,
    'system',
    'system'
),

-- Local instrumentation code level configurations
(
    'tenant-001',
    'SEPA_CREDIT_TRANSFER',
    'SEPA_CT',
    NULL,
    TRUE,
    'SEPA CT requires enhanced fraud checking',
    NULL,
    NULL,
    NULL,
    80,
    TRUE,
    'system',
    'system'
),
(
    'tenant-001',
    'SEPA_CREDIT_TRANSFER',
    'SEPA_CT_INSTANT',
    NULL,
    FALSE,
    NULL,
    'SEPA instant payments have built-in fraud protection',
    NULL,
    NULL,
    80,
    TRUE,
    'system',
    'system'
),

-- Clearing system level configurations
(
    'tenant-001',
    'SEPA_CREDIT_TRANSFER',
    'SEPA_CT',
    'TARGET2',
    TRUE,
    'TARGET2 clearing requires fraud API validation',
    NULL,
    NULL,
    NULL,
    70,
    TRUE,
    'system',
    'system'
),
(
    'tenant-001',
    'SEPA_CREDIT_TRANSFER',
    'SEPA_CT',
    'EBA_CLEARING',
    FALSE,
    NULL,
    'EBA Clearing has its own fraud detection',
    NULL,
    NULL,
    70,
    TRUE,
    'system',
    'system'
),

-- Scheduled configurations (future effective)
(
    'tenant-001',
    'HIGH_VALUE_TRANSFER',
    NULL,
    NULL,
    TRUE,
    'High value transfers will require fraud checking from next month',
    NULL,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    NULL,
    85,
    TRUE,
    'system',
    'system'
),

-- Scheduled configurations (expired)
(
    'tenant-001',
    'TEST_TRANSFER',
    NULL,
    NULL,
    FALSE,
    NULL,
    'Test transfers fraud API disabled during testing period',
    CURRENT_TIMESTAMP - INTERVAL '7 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    95,
    TRUE,
    'system',
    'system'
);

-- Create views for common queries
CREATE OR REPLACE VIEW payment_engine.fraud_api_toggle_configurations_active AS
SELECT 
    id,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    is_enabled,
    enabled_reason,
    disabled_reason,
    effective_from,
    effective_until,
    priority,
    is_active,
    created_by,
    updated_by,
    created_at,
    updated_at,
    CASE 
        WHEN clearing_system_code IS NOT NULL THEN 'CLEARING_SYSTEM'
        WHEN local_instrumentation_code IS NOT NULL THEN 'LOCAL_INSTRUMENT'
        WHEN payment_type IS NOT NULL THEN 'PAYMENT_TYPE'
        ELSE 'TENANT'
    END as configuration_level
FROM payment_engine.fraud_api_toggle_configurations
WHERE is_active = true
ORDER BY tenant_id, priority ASC, created_at ASC;

CREATE OR REPLACE VIEW payment_engine.fraud_api_toggle_configurations_currently_effective AS
SELECT 
    id,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    is_enabled,
    enabled_reason,
    disabled_reason,
    priority,
    created_by,
    updated_by,
    created_at,
    updated_at,
    CASE 
        WHEN clearing_system_code IS NOT NULL THEN 'CLEARING_SYSTEM'
        WHEN local_instrumentation_code IS NOT NULL THEN 'LOCAL_INSTRUMENT'
        WHEN payment_type IS NOT NULL THEN 'PAYMENT_TYPE'
        ELSE 'TENANT'
    END as configuration_level
FROM payment_engine.fraud_api_toggle_configurations
WHERE is_active = true
AND (effective_from IS NULL OR effective_from <= CURRENT_TIMESTAMP)
AND (effective_until IS NULL OR effective_until >= CURRENT_TIMESTAMP)
ORDER BY tenant_id, priority ASC, created_at ASC;

CREATE OR REPLACE VIEW payment_engine.fraud_api_toggle_configurations_future_effective AS
SELECT 
    id,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    is_enabled,
    enabled_reason,
    disabled_reason,
    effective_from,
    priority,
    created_by,
    created_at,
    CASE 
        WHEN clearing_system_code IS NOT NULL THEN 'CLEARING_SYSTEM'
        WHEN local_instrumentation_code IS NOT NULL THEN 'LOCAL_INSTRUMENT'
        WHEN payment_type IS NOT NULL THEN 'PAYMENT_TYPE'
        ELSE 'TENANT'
    END as configuration_level
FROM payment_engine.fraud_api_toggle_configurations
WHERE is_active = true
AND effective_from > CURRENT_TIMESTAMP
ORDER BY effective_from ASC;

CREATE OR REPLACE VIEW payment_engine.fraud_api_toggle_configurations_expired AS
SELECT 
    id,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    is_enabled,
    enabled_reason,
    disabled_reason,
    effective_until,
    priority,
    created_by,
    created_at,
    CASE 
        WHEN clearing_system_code IS NOT NULL THEN 'CLEARING_SYSTEM'
        WHEN local_instrumentation_code IS NOT NULL THEN 'LOCAL_INSTRUMENT'
        WHEN payment_type IS NOT NULL THEN 'PAYMENT_TYPE'
        ELSE 'TENANT'
    END as configuration_level
FROM payment_engine.fraud_api_toggle_configurations
WHERE is_active = true
AND effective_until < CURRENT_TIMESTAMP
ORDER BY effective_until DESC;

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON payment_engine.fraud_api_toggle_configurations TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_api_toggle_configurations_active TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_api_toggle_configurations_currently_effective TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_api_toggle_configurations_future_effective TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_api_toggle_configurations_expired TO payment_engine_user;

-- Add comments
COMMENT ON TABLE payment_engine.fraud_api_toggle_configurations IS 'Configuration for enabling/disabling fraud API at different levels (tenant, payment type, local instrument, clearing system)';

COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.payment_type IS 'Payment type (optional) - if specified, applies to specific payment type only';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.local_instrumentation_code IS 'Local instrumentation code (optional) - if specified, applies to specific local instrument only';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.clearing_system_code IS 'Clearing system code (optional) - if specified, applies to specific clearing system only';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.is_enabled IS 'Whether fraud API is enabled (true) or disabled (false)';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.enabled_reason IS 'Reason for enabling fraud API (required when is_enabled = true)';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.disabled_reason IS 'Reason for disabling fraud API (required when is_enabled = false)';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.effective_from IS 'When this configuration becomes effective (optional)';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.effective_until IS 'When this configuration expires (optional)';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.priority IS 'Priority order for applying configurations (lower number = higher priority)';
COMMENT ON COLUMN payment_engine.fraud_api_toggle_configurations.is_active IS 'Whether this configuration is active (can be deactivated without deletion)';

COMMENT ON VIEW payment_engine.fraud_api_toggle_configurations_active IS 'All active fraud API toggle configurations';
COMMENT ON VIEW payment_engine.fraud_api_toggle_configurations_currently_effective IS 'Currently effective fraud API toggle configurations';
COMMENT ON VIEW payment_engine.fraud_api_toggle_configurations_future_effective IS 'Future effective fraud API toggle configurations';
COMMENT ON VIEW payment_engine.fraud_api_toggle_configurations_expired IS 'Expired fraud API toggle configurations';