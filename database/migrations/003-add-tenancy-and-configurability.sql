-- Multi-Tenancy and Advanced Configurability Support
-- Adds support for multi-tenant architecture and dynamic configuration

-- Set search path
SET search_path TO payment_engine, config, public;

-- ============================================================================
-- MULTI-TENANCY SUPPORT
-- ============================================================================

-- Tenants table
CREATE TABLE config.tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) UNIQUE NOT NULL,
    tenant_name VARCHAR(100) NOT NULL,
    tenant_type VARCHAR(20) DEFAULT 'BANK', -- BANK, FINTECH, CORPORATE, GOVERNMENT
    status VARCHAR(20) DEFAULT 'ACTIVE',
    subscription_tier VARCHAR(20) DEFAULT 'STANDARD', -- BASIC, STANDARD, PREMIUM, ENTERPRISE
    configuration JSONB DEFAULT '{}',
    billing_config JSONB DEFAULT '{}',
    compliance_config JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP WITH TIME ZONE,
    deactivated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_tenant_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_ACTIVATION', 'DECOMMISSIONED')),
    CONSTRAINT chk_tenant_type CHECK (tenant_type IN ('BANK', 'FINTECH', 'CORPORATE', 'GOVERNMENT', 'CREDIT_UNION')),
    CONSTRAINT chk_subscription_tier CHECK (subscription_tier IN ('BASIC', 'STANDARD', 'PREMIUM', 'ENTERPRISE'))
);

-- Add tenant_id to existing tables
ALTER TABLE payment_engine.customers ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) DEFAULT 'default';
ALTER TABLE payment_engine.accounts ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) DEFAULT 'default';
ALTER TABLE payment_engine.transactions ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) DEFAULT 'default';
ALTER TABLE payment_engine.payment_types ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
ALTER TABLE payment_engine.users ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50) DEFAULT 'default';
ALTER TABLE config.system_config ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
ALTER TABLE config.kafka_topics ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);
ALTER TABLE config.api_endpoints ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(50);

-- Tenant Resource Limits
CREATE TABLE config.tenant_limits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    resource_type VARCHAR(50) NOT NULL, -- TRANSACTIONS_PER_DAY, API_CALLS_PER_HOUR, STORAGE_GB, etc.
    limit_value BIGINT NOT NULL,
    current_usage BIGINT DEFAULT 0,
    reset_period VARCHAR(20) DEFAULT 'DAILY', -- HOURLY, DAILY, MONTHLY
    last_reset_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, resource_type)
);

-- Tenant Configurations (overrides global config)
CREATE TABLE config.tenant_configurations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    config_category VARCHAR(50) NOT NULL, -- PAYMENT_PROCESSING, SECURITY, MONITORING, etc.
    config_key VARCHAR(100) NOT NULL,
    config_value JSONB NOT NULL,
    environment VARCHAR(20) DEFAULT 'production',
    is_encrypted BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, config_category, config_key, environment)
);

-- ============================================================================
-- ADVANCED CONFIGURABILITY
-- ============================================================================

-- Feature Flags
CREATE TABLE config.feature_flags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id), -- NULL for global
    feature_name VARCHAR(100) NOT NULL,
    config_value JSONB NOT NULL DEFAULT '{"enabled": false}',
    rollout_percentage INTEGER DEFAULT 0, -- For gradual rollouts
    target_groups JSONB DEFAULT '[]', -- Specific user groups
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, feature_name)
);

-- Rate Limiting Configuration
CREATE TABLE config.rate_limits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id), -- NULL for global
    endpoint_pattern VARCHAR(255) NOT NULL, -- /api/v1/transactions/*, etc.
    http_method VARCHAR(10) DEFAULT '*', -- GET, POST, *, etc.
    rate_limit_per_minute INTEGER NOT NULL DEFAULT 1000,
    burst_capacity INTEGER DEFAULT 1500,
    window_size_seconds INTEGER DEFAULT 60,
    configuration JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, endpoint_pattern, http_method)
);

-- Circuit Breaker Configuration
CREATE TABLE config.circuit_breaker_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id), -- NULL for global
    service_name VARCHAR(100) NOT NULL,
    failure_threshold INTEGER DEFAULT 50, -- Percentage
    timeout_duration INTEGER DEFAULT 10000, -- Milliseconds
    half_open_max_calls INTEGER DEFAULT 3,
    sliding_window_size INTEGER DEFAULT 10,
    minimum_calls INTEGER DEFAULT 5,
    configuration JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, service_name)
);

-- Dynamic Payment Type Configuration
CREATE TABLE config.dynamic_payment_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    payment_type_code VARCHAR(50) NOT NULL,
    configuration JSONB NOT NULL,
    effective_from TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    effective_until TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT true,
    created_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, payment_type_code, effective_from)
);

-- API Gateway Route Configuration
CREATE TABLE config.dynamic_routes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id), -- NULL for global
    route_id VARCHAR(100) NOT NULL,
    route_path VARCHAR(255) NOT NULL,
    target_service VARCHAR(100) NOT NULL,
    http_methods VARCHAR(50) DEFAULT 'GET,POST,PUT,DELETE',
    filters JSONB DEFAULT '[]',
    predicates JSONB DEFAULT '[]',
    metadata JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 100,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, route_id)
);

-- Configuration Change History
CREATE TABLE config.configuration_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id),
    config_type VARCHAR(50) NOT NULL, -- PAYMENT_TYPE, RATE_LIMIT, FEATURE_FLAG, etc.
    config_key VARCHAR(100) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    change_reason TEXT,
    changed_by VARCHAR(100) NOT NULL,
    change_source VARCHAR(50) DEFAULT 'API', -- API, UI, SCRIPT, MIGRATION
    rollback_possible BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- SELF-HEALING AND AUTOMATION
-- ============================================================================

-- Health Check Configuration
CREATE TABLE config.health_check_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id),
    service_name VARCHAR(100) NOT NULL,
    check_type VARCHAR(50) NOT NULL, -- HTTP, DATABASE, KAFKA, REDIS, etc.
    check_endpoint VARCHAR(255),
    check_interval_seconds INTEGER DEFAULT 30,
    timeout_seconds INTEGER DEFAULT 10,
    failure_threshold INTEGER DEFAULT 3,
    recovery_threshold INTEGER DEFAULT 2,
    auto_remediation JSONB DEFAULT '{}',
    alert_config JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, service_name, check_type)
);

-- Auto-Scaling Configuration
CREATE TABLE config.auto_scaling_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id),
    service_name VARCHAR(100) NOT NULL,
    min_replicas INTEGER DEFAULT 2,
    max_replicas INTEGER DEFAULT 10,
    target_cpu_percentage INTEGER DEFAULT 70,
    target_memory_percentage INTEGER DEFAULT 80,
    scale_up_threshold INTEGER DEFAULT 80,
    scale_down_threshold INTEGER DEFAULT 30,
    scale_up_cooldown_seconds INTEGER DEFAULT 300,
    scale_down_cooldown_seconds INTEGER DEFAULT 300,
    custom_metrics JSONB DEFAULT '[]',
    configuration JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, service_name)
);

-- Rollback Configuration
CREATE TABLE config.rollback_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id),
    trigger_type VARCHAR(50) NOT NULL, -- ERROR_RATE, RESPONSE_TIME, MANUAL, etc.
    trigger_threshold DECIMAL(10,2) NOT NULL,
    measurement_window_minutes INTEGER DEFAULT 5,
    auto_rollback_enabled BOOLEAN DEFAULT false,
    rollback_strategy VARCHAR(50) DEFAULT 'PREVIOUS_VERSION', -- PREVIOUS_VERSION, SAFE_MODE, CIRCUIT_BREAK
    notification_config JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, trigger_type)
);

-- ============================================================================
-- BUSINESS RULE CONFIGURATION
-- ============================================================================

-- Dynamic Business Rules
CREATE TABLE config.business_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    rule_name VARCHAR(100) NOT NULL,
    rule_category VARCHAR(50) NOT NULL, -- FRAUD_DETECTION, COMPLIANCE, LIMITS, etc.
    rule_expression TEXT NOT NULL, -- JSON rules or expression language
    rule_priority INTEGER DEFAULT 100,
    is_active BOOLEAN DEFAULT true,
    effective_from TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    effective_until TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, rule_name, effective_from)
);

-- Transaction Limits Configuration
CREATE TABLE config.transaction_limits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    account_type VARCHAR(50), -- NULL for all account types
    payment_type VARCHAR(50), -- NULL for all payment types
    limit_type VARCHAR(50) NOT NULL, -- DAILY, MONTHLY, SINGLE_TRANSACTION, VELOCITY
    limit_amount DECIMAL(15,2),
    limit_count INTEGER,
    limit_period_hours INTEGER,
    currency_code VARCHAR(3) DEFAULT 'USD',
    risk_level VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH
    override_allowed BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, account_type, payment_type, limit_type)
);

-- ============================================================================
-- MONITORING AND ALERTING CONFIGURATION
-- ============================================================================

-- Alert Rules Configuration
CREATE TABLE config.alert_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) REFERENCES config.tenants(tenant_id),
    rule_name VARCHAR(100) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    threshold_value DECIMAL(15,2) NOT NULL,
    comparison_operator VARCHAR(10) NOT NULL, -- GT, LT, EQ, GTE, LTE
    evaluation_window_minutes INTEGER DEFAULT 5,
    severity VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    notification_channels JSONB DEFAULT '[]', -- EMAIL, SLACK, PAGERDUTY, etc.
    auto_remediation JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, rule_name)
);

-- SLA/SLO Configuration
CREATE TABLE config.sla_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    sla_name VARCHAR(100) NOT NULL,
    sla_type VARCHAR(50) NOT NULL, -- AVAILABILITY, RESPONSE_TIME, THROUGHPUT, ERROR_RATE
    target_value DECIMAL(10,4) NOT NULL,
    measurement_period VARCHAR(20) DEFAULT 'MONTHLY', -- HOURLY, DAILY, WEEKLY, MONTHLY
    error_budget_percentage DECIMAL(5,2) DEFAULT 0.1,
    burn_rate_threshold DECIMAL(10,4) DEFAULT 1.0,
    notification_config JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, sla_name)
);

-- ============================================================================
-- INTEGRATION CONFIGURATION
-- ============================================================================

-- External System Configurations
CREATE TABLE config.external_systems (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    system_name VARCHAR(100) NOT NULL,
    system_type VARCHAR(50) NOT NULL, -- CORE_BANKING, PAYMENT_SCHEME, REGULATORY, NOTIFICATION
    endpoint_url VARCHAR(500),
    authentication_config JSONB DEFAULT '{}',
    timeout_config JSONB DEFAULT '{"connect": 5000, "read": 30000}',
    retry_config JSONB DEFAULT '{"maxAttempts": 3, "backoffMultiplier": 2}',
    circuit_breaker_config JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, system_name)
);

-- Webhook Configuration
CREATE TABLE config.webhook_endpoints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    endpoint_name VARCHAR(100) NOT NULL,
    webhook_url VARCHAR(500) NOT NULL,
    event_types JSONB NOT NULL DEFAULT '[]',
    secret_key VARCHAR(255),
    headers JSONB DEFAULT '{}',
    retry_config JSONB DEFAULT '{"maxAttempts": 3, "backoffSeconds": [1, 2, 4]}',
    timeout_seconds INTEGER DEFAULT 30,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, endpoint_name)
);

-- ============================================================================
-- COMPLIANCE AND REGULATORY CONFIGURATION
-- ============================================================================

-- Compliance Rules Configuration
CREATE TABLE config.compliance_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL REFERENCES config.tenants(tenant_id),
    rule_name VARCHAR(100) NOT NULL,
    jurisdiction VARCHAR(10) NOT NULL, -- US, EU, UK, CA, etc.
    regulation_type VARCHAR(50) NOT NULL, -- AML, KYC, PCI_DSS, GDPR, etc.
    rule_definition JSONB NOT NULL,
    reporting_requirements JSONB DEFAULT '{}',
    enforcement_level VARCHAR(20) DEFAULT 'STRICT', -- STRICT, MODERATE, ADVISORY
    effective_from TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    effective_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, rule_name, jurisdiction)
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Tenant indexes
CREATE INDEX idx_tenants_status ON config.tenants(status);
CREATE INDEX idx_tenants_type ON config.tenants(tenant_type);

-- Multi-tenant data indexes
CREATE INDEX idx_customers_tenant_id ON payment_engine.customers(tenant_id);
CREATE INDEX idx_accounts_tenant_id ON payment_engine.accounts(tenant_id);
CREATE INDEX idx_transactions_tenant_id ON payment_engine.transactions(tenant_id);
CREATE INDEX idx_users_tenant_id ON payment_engine.users(tenant_id);

-- Configuration indexes
CREATE INDEX idx_tenant_configurations_tenant_category ON config.tenant_configurations(tenant_id, config_category);
CREATE INDEX idx_feature_flags_tenant_feature ON config.feature_flags(tenant_id, feature_name);
CREATE INDEX idx_rate_limits_tenant_endpoint ON config.rate_limits(tenant_id, endpoint_pattern);
CREATE INDEX idx_business_rules_tenant_category ON config.business_rules(tenant_id, rule_category);

-- ============================================================================
-- ROW LEVEL SECURITY (RLS) FOR MULTI-TENANCY
-- ============================================================================

-- Enable RLS on tenant-aware tables
ALTER TABLE payment_engine.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_engine.accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_engine.transactions ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY tenant_isolation_customers ON payment_engine.customers
    FOR ALL TO payment_engine_role
    USING (tenant_id = current_setting('app.current_tenant', true));

CREATE POLICY tenant_isolation_accounts ON payment_engine.accounts
    FOR ALL TO payment_engine_role
    USING (tenant_id = current_setting('app.current_tenant', true));

CREATE POLICY tenant_isolation_transactions ON payment_engine.transactions
    FOR ALL TO payment_engine_role
    USING (tenant_id = current_setting('app.current_tenant', true));

-- ============================================================================
-- CONFIGURATION FUNCTIONS
-- ============================================================================

-- Function to set current tenant context
CREATE OR REPLACE FUNCTION set_tenant_context(p_tenant_id VARCHAR(50))
RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.current_tenant', p_tenant_id, true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get current tenant
CREATE OR REPLACE FUNCTION get_current_tenant()
RETURNS VARCHAR(50) AS $$
BEGIN
    RETURN current_setting('app.current_tenant', true);
END;
$$ LANGUAGE plpgsql;

-- Function to check tenant resource usage
CREATE OR REPLACE FUNCTION check_tenant_resource_usage(p_tenant_id VARCHAR(50), p_resource_type VARCHAR(50))
RETURNS BOOLEAN AS $$
DECLARE
    current_limit BIGINT;
    current_usage BIGINT;
BEGIN
    SELECT limit_value, current_usage INTO current_limit, current_usage
    FROM config.tenant_limits
    WHERE tenant_id = p_tenant_id AND resource_type = p_resource_type;
    
    IF current_limit IS NULL THEN
        RETURN true; -- No limit configured
    END IF;
    
    RETURN current_usage < current_limit;
END;
$$ LANGUAGE plpgsql;

-- Function to increment tenant resource usage
CREATE OR REPLACE FUNCTION increment_tenant_usage(p_tenant_id VARCHAR(50), p_resource_type VARCHAR(50), p_increment INTEGER DEFAULT 1)
RETURNS VOID AS $$
BEGIN
    INSERT INTO config.tenant_limits (tenant_id, resource_type, current_usage, limit_value)
    VALUES (p_tenant_id, p_resource_type, p_increment, 999999999)
    ON CONFLICT (tenant_id, resource_type)
    DO UPDATE SET 
        current_usage = tenant_limits.current_usage + p_increment,
        updated_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Function to reset tenant usage (for daily/monthly resets)
CREATE OR REPLACE FUNCTION reset_tenant_usage(p_tenant_id VARCHAR(50), p_resource_type VARCHAR(50))
RETURNS VOID AS $$
BEGIN
    UPDATE config.tenant_limits
    SET current_usage = 0,
        last_reset_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE tenant_id = p_tenant_id 
    AND (p_resource_type IS NULL OR resource_type = p_resource_type);
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- TRIGGERS FOR CONFIGURATION MANAGEMENT
-- ============================================================================

-- Configuration change audit trigger
CREATE OR REPLACE FUNCTION audit_configuration_changes()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO config.configuration_history 
    (tenant_id, config_type, config_key, old_value, new_value, changed_by, change_source)
    VALUES (
        COALESCE(NEW.tenant_id, OLD.tenant_id),
        TG_TABLE_NAME,
        COALESCE(NEW.config_key, NEW.feature_name, NEW.rule_name, NEW.endpoint_pattern),
        CASE WHEN TG_OP = 'DELETE' THEN to_jsonb(OLD) ELSE to_jsonb(OLD) END,
        CASE WHEN TG_OP = 'DELETE' THEN NULL ELSE to_jsonb(NEW) END,
        current_user,
        'DATABASE'
    );
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Apply audit triggers to configuration tables
CREATE TRIGGER audit_system_config_changes
    AFTER INSERT OR UPDATE OR DELETE ON config.system_config
    FOR EACH ROW EXECUTE FUNCTION audit_configuration_changes();

CREATE TRIGGER audit_feature_flags_changes
    AFTER INSERT OR UPDATE OR DELETE ON config.feature_flags
    FOR EACH ROW EXECUTE FUNCTION audit_configuration_changes();

CREATE TRIGGER audit_rate_limits_changes
    AFTER INSERT OR UPDATE OR DELETE ON config.rate_limits
    FOR EACH ROW EXECUTE FUNCTION audit_configuration_changes();

-- ============================================================================
-- SAMPLE DATA FOR MULTI-TENANCY
-- ============================================================================

-- Insert default tenant
INSERT INTO config.tenants (tenant_id, tenant_name, tenant_type, subscription_tier, configuration) VALUES
    ('default', 'Default Tenant', 'BANK', 'ENTERPRISE', '{"features": {"iso20022": true, "bulk_processing": true, "advanced_monitoring": true}}'),
    ('demo-bank', 'Demo Bank', 'BANK', 'STANDARD', '{"features": {"iso20022": true, "bulk_processing": false, "advanced_monitoring": false}}'),
    ('fintech-corp', 'FinTech Corporation', 'FINTECH', 'PREMIUM', '{"features": {"iso20022": true, "bulk_processing": true, "advanced_monitoring": true}}');

-- Insert default tenant limits
INSERT INTO config.tenant_limits (tenant_id, resource_type, limit_value, reset_period) VALUES
    ('default', 'TRANSACTIONS_PER_DAY', 100000, 'DAILY'),
    ('default', 'API_CALLS_PER_HOUR', 50000, 'HOURLY'),
    ('default', 'STORAGE_GB', 1000, 'MONTHLY'),
    ('demo-bank', 'TRANSACTIONS_PER_DAY', 10000, 'DAILY'),
    ('demo-bank', 'API_CALLS_PER_HOUR', 5000, 'HOURLY'),
    ('demo-bank', 'STORAGE_GB', 100, 'MONTHLY'),
    ('fintech-corp', 'TRANSACTIONS_PER_DAY', 50000, 'DAILY'),
    ('fintech-corp', 'API_CALLS_PER_HOUR', 25000, 'HOURLY'),
    ('fintech-corp', 'STORAGE_GB', 500, 'MONTHLY');

-- Insert default feature flags
INSERT INTO config.feature_flags (tenant_id, feature_name, config_value, rollout_percentage) VALUES
    (NULL, 'iso20022_processing', '{"enabled": true, "version": "1.0"}', 100),
    (NULL, 'bulk_processing', '{"enabled": true, "max_batch_size": 1000}', 100),
    (NULL, 'advanced_fraud_detection', '{"enabled": false, "ml_model": "v2.1"}', 0),
    ('demo-bank', 'bulk_processing', '{"enabled": false}', 0),
    ('fintech-corp', 'advanced_fraud_detection', '{"enabled": true, "ml_model": "v2.1"}', 50);

-- Insert default rate limits
INSERT INTO config.rate_limits (tenant_id, endpoint_pattern, http_method, rate_limit_per_minute, burst_capacity) VALUES
    (NULL, '/api/v1/transactions', 'POST', 100, 150),
    (NULL, '/api/v1/iso20022/pain001', 'POST', 100, 150),
    (NULL, '/api/v1/iso20022/camt055', 'POST', 50, 75),
    ('demo-bank', '/api/v1/transactions', 'POST', 50, 75),
    ('fintech-corp', '/api/v1/transactions', 'POST', 200, 300);

-- Insert default business rules
INSERT INTO config.business_rules (tenant_id, rule_name, rule_category, rule_expression) VALUES
    ('default', 'high_value_transaction_check', 'COMPLIANCE', '{"condition": "amount > 10000", "actions": ["require_approval", "enhanced_monitoring"]}'),
    ('default', 'velocity_check', 'FRAUD_DETECTION', '{"condition": "transaction_count_1h > 10 AND total_amount_1h > 50000", "actions": ["flag_for_review"]}'),
    ('demo-bank', 'demo_transaction_limit', 'LIMITS', '{"condition": "amount > 5000", "actions": ["reject_transaction"]}');

-- ============================================================================
-- VIEWS FOR CONFIGURATION MANAGEMENT
-- ============================================================================

-- Tenant Configuration Overview
CREATE VIEW config.tenant_configuration_overview AS
SELECT 
    t.tenant_id,
    t.tenant_name,
    t.tenant_type,
    t.status,
    t.subscription_tier,
    COUNT(DISTINCT c.id) as custom_configs,
    COUNT(DISTINCT f.id) as feature_flags,
    COUNT(DISTINCT r.id) as rate_limits,
    COUNT(DISTINCT br.id) as business_rules,
    t.created_at,
    t.updated_at
FROM config.tenants t
LEFT JOIN config.tenant_configurations c ON t.tenant_id = c.tenant_id
LEFT JOIN config.feature_flags f ON t.tenant_id = f.tenant_id
LEFT JOIN config.rate_limits r ON t.tenant_id = r.tenant_id
LEFT JOIN config.business_rules br ON t.tenant_id = br.tenant_id
GROUP BY t.tenant_id, t.tenant_name, t.tenant_type, t.status, t.subscription_tier, t.created_at, t.updated_at;

-- Active Configuration Summary
CREATE VIEW config.active_configuration_summary AS
SELECT 
    'system_config' as config_type,
    tenant_id,
    config_key as name,
    config_value as value,
    environment,
    updated_at
FROM config.system_config
WHERE tenant_id IS NOT NULL

UNION ALL

SELECT 
    'feature_flag' as config_type,
    tenant_id,
    feature_name as name,
    config_value as value,
    'production' as environment,
    updated_at
FROM config.feature_flags
WHERE tenant_id IS NOT NULL

UNION ALL

SELECT 
    'rate_limit' as config_type,
    tenant_id,
    endpoint_pattern as name,
    jsonb_build_object('rate_limit_per_minute', rate_limit_per_minute, 'burst_capacity', burst_capacity) as value,
    'production' as environment,
    updated_at
FROM config.rate_limits
WHERE tenant_id IS NOT NULL

ORDER BY tenant_id, config_type, name;