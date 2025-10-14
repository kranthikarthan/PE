-- =====================================================
-- TENANT MANAGEMENT SERVICE DATABASE
-- =====================================================
-- This is the foundational database that must be deployed FIRST
-- All other services depend on tenant data for multi-tenancy

-- =====================================================
-- TENANTS (Top-level tenant records)
-- =====================================================
CREATE TABLE tenants (
    tenant_id VARCHAR(20) PRIMARY KEY,
    tenant_name VARCHAR(200) NOT NULL,
    tenant_type VARCHAR(50) NOT NULL CHECK (tenant_type IN ('BANK', 'FINANCIAL_INSTITUTION', 'FINTECH', 'CORPORATE')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_APPROVAL')),
    registration_number VARCHAR(50),
    tax_number VARCHAR(50),
    contact_email VARCHAR(200) NOT NULL,
    contact_phone VARCHAR(20),
    address_line1 VARCHAR(200),
    address_line2 VARCHAR(200),
    city VARCHAR(100),
    province VARCHAR(100),
    postal_code VARCHAR(10),
    country VARCHAR(3) DEFAULT 'ZAR',
    timezone VARCHAR(50) DEFAULT 'Africa/Johannesburg',
    currency VARCHAR(3) DEFAULT 'ZAR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Indexes for tenant queries
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_tenants_type ON tenants(tenant_type);
CREATE INDEX idx_tenants_created_at ON tenants(created_at DESC);

-- =====================================================
-- BUSINESS UNITS (Divisions within tenants)
-- =====================================================
CREATE TABLE business_units (
    business_unit_id VARCHAR(30) PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_name VARCHAR(200) NOT NULL,
    business_unit_type VARCHAR(50) NOT NULL CHECK (business_unit_type IN ('RETAIL', 'CORPORATE', 'INVESTMENT', 'PRIVATE_BANKING', 'TREASURY')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    parent_business_unit_id VARCHAR(30),
    hierarchy_level INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_business_unit_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_business_unit_parent FOREIGN KEY (parent_business_unit_id) 
        REFERENCES business_units(business_unit_id) ON DELETE SET NULL
);

-- Indexes for business unit queries
CREATE INDEX idx_business_units_tenant_id ON business_units(tenant_id);
CREATE INDEX idx_business_units_type ON business_units(business_unit_type);
CREATE INDEX idx_business_units_status ON business_units(status);
CREATE INDEX idx_business_units_parent ON business_units(parent_business_unit_id);

-- =====================================================
-- TENANT CONFIGURATIONS
-- =====================================================
CREATE TABLE tenant_configs (
    config_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT NOT NULL,
    config_type VARCHAR(20) NOT NULL CHECK (config_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON', 'ENCRYPTED')),
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_sensitive BOOLEAN DEFAULT FALSE,
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_tenant_config_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_config_business_unit FOREIGN KEY (business_unit_id) 
        REFERENCES business_units(business_unit_id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_config UNIQUE (tenant_id, business_unit_id, config_key, effective_from)
);

-- Indexes for configuration queries
CREATE INDEX idx_tenant_configs_tenant_id ON tenant_configs(tenant_id);
CREATE INDEX idx_tenant_configs_business_unit_id ON tenant_configs(business_unit_id);
CREATE INDEX idx_tenant_configs_key ON tenant_configs(config_key);
CREATE INDEX idx_tenant_configs_effective_from ON tenant_configs(effective_from);

-- =====================================================
-- TENANT USERS (Admin users per tenant)
-- =====================================================
CREATE TABLE tenant_users (
    tenant_user_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),
    user_id VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('TENANT_ADMIN', 'BUSINESS_UNIT_ADMIN', 'OPERATOR', 'VIEWER')),
    permissions JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    last_accessed_at TIMESTAMP,
    
    CONSTRAINT fk_tenant_user_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_user_business_unit FOREIGN KEY (business_unit_id) 
        REFERENCES business_units(business_unit_id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_user UNIQUE (tenant_id, user_id)
);

-- Indexes for tenant user queries
CREATE INDEX idx_tenant_users_tenant_id ON tenant_users(tenant_id);
CREATE INDEX idx_tenant_users_business_unit_id ON tenant_users(business_unit_id);
CREATE INDEX idx_tenant_users_user_id ON tenant_users(user_id);
CREATE INDEX idx_tenant_users_role ON tenant_users(role);
CREATE INDEX idx_tenant_users_active ON tenant_users(is_active);

-- =====================================================
-- TENANT API KEYS (API keys for programmatic access)
-- =====================================================
CREATE TABLE tenant_api_keys (
    api_key_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),
    api_key_name VARCHAR(200) NOT NULL,
    api_key_hash VARCHAR(255) NOT NULL,
    permissions JSONB NOT NULL,
    rate_limit_per_minute INTEGER DEFAULT 1000,
    rate_limit_per_hour INTEGER DEFAULT 10000,
    rate_limit_per_day INTEGER DEFAULT 100000,
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT fk_tenant_api_key_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_api_key_business_unit FOREIGN KEY (business_unit_id) 
        REFERENCES business_units(business_unit_id) ON DELETE CASCADE
);

-- Indexes for API key queries
CREATE INDEX idx_tenant_api_keys_tenant_id ON tenant_api_keys(tenant_id);
CREATE INDEX idx_tenant_api_keys_business_unit_id ON tenant_api_keys(business_unit_id);
CREATE INDEX idx_tenant_api_keys_active ON tenant_api_keys(is_active);
CREATE INDEX idx_tenant_api_keys_expires_at ON tenant_api_keys(expires_at);

-- =====================================================
-- TENANT METRICS (Usage tracking per tenant)
-- =====================================================
CREATE TABLE tenant_metrics (
    metric_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),
    metric_date DATE NOT NULL,
    metric_type VARCHAR(50) NOT NULL CHECK (metric_type IN ('API_CALLS', 'PAYMENTS', 'TRANSACTIONS', 'STORAGE_GB', 'COMPUTE_HOURS')),
    metric_value DECIMAL(18,2) NOT NULL,
    metric_unit VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tenant_metrics_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_metrics_business_unit FOREIGN KEY (business_unit_id) 
        REFERENCES business_units(business_unit_id) ON DELETE CASCADE,
    CONSTRAINT uk_tenant_metrics UNIQUE (tenant_id, business_unit_id, metric_date, metric_type)
);

-- Indexes for metrics queries
CREATE INDEX idx_tenant_metrics_tenant_id ON tenant_metrics(tenant_id);
CREATE INDEX idx_tenant_metrics_business_unit_id ON tenant_metrics(business_unit_id);
CREATE INDEX idx_tenant_metrics_date ON tenant_metrics(metric_date DESC);
CREATE INDEX idx_tenant_metrics_type ON tenant_metrics(metric_type);

-- =====================================================
-- TENANT AUDIT LOG (Audit trail for tenant operations)
-- =====================================================
CREATE TABLE tenant_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30),
    user_id VARCHAR(50),
    event_type VARCHAR(50) NOT NULL CHECK (event_type IN ('TENANT_CREATED', 'TENANT_UPDATED', 'TENANT_SUSPENDED', 'CONFIG_CHANGED', 'USER_ADDED', 'USER_REMOVED', 'API_KEY_CREATED', 'API_KEY_REVOKED')),
    event_description TEXT NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tenant_audit_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_audit_business_unit FOREIGN KEY (business_unit_id) 
        REFERENCES business_units(business_unit_id) ON DELETE CASCADE
);

-- Indexes for audit log queries
CREATE INDEX idx_tenant_audit_tenant_id ON tenant_audit_log(tenant_id);
CREATE INDEX idx_tenant_audit_business_unit_id ON tenant_audit_log(business_unit_id);
CREATE INDEX idx_tenant_audit_user_id ON tenant_audit_log(user_id);
CREATE INDEX idx_tenant_audit_event_type ON tenant_audit_log(event_type);
CREATE INDEX idx_tenant_audit_occurred_at ON tenant_audit_log(occurred_at DESC);

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with updated_at column
CREATE TRIGGER update_tenants_updated_at 
    BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_business_units_updated_at 
    BEFORE UPDATE ON business_units
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tenant_configs_updated_at 
    BEFORE UPDATE ON tenant_configs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- SEED DATA
-- =====================================================

-- Insert default tenant for platform operations
INSERT INTO tenants (tenant_id, tenant_name, tenant_type, contact_email, created_by) VALUES
    ('PLATFORM', 'Payments Engine Platform', 'FINTECH', 'platform@paymentsengine.co.za', 'system');

-- Insert default business unit for platform
INSERT INTO business_units (business_unit_id, tenant_id, business_unit_name, business_unit_type, created_by) VALUES
    ('PLATFORM-DEFAULT', 'PLATFORM', 'Platform Operations', 'TREASURY', 'system');

-- Insert default tenant configurations
INSERT INTO tenant_configs (tenant_id, business_unit_id, config_key, config_value, config_type, created_by) VALUES
    ('PLATFORM', 'PLATFORM-DEFAULT', 'max_daily_transactions', '1000000', 'NUMBER', 'system'),
    ('PLATFORM', 'PLATFORM-DEFAULT', 'max_daily_amount', '1000000000.00', 'NUMBER', 'system'),
    ('PLATFORM', 'PLATFORM-DEFAULT', 'timezone', 'Africa/Johannesburg', 'STRING', 'system'),
    ('PLATFORM', 'PLATFORM-DEFAULT', 'currency', 'ZAR', 'STRING', 'system'),
    ('PLATFORM', 'PLATFORM-DEFAULT', 'fraud_detection_enabled', 'true', 'BOOLEAN', 'system'),
    ('PLATFORM', 'PLATFORM-DEFAULT', 'audit_retention_days', '2555', 'NUMBER', 'system'); -- 7 years

-- =====================================================
-- VIEWS
-- =====================================================

-- View for tenant hierarchy
CREATE VIEW tenant_hierarchy AS
SELECT 
    t.tenant_id,
    t.tenant_name,
    t.tenant_type,
    t.status AS tenant_status,
    bu.business_unit_id,
    bu.business_unit_name,
    bu.business_unit_type,
    bu.status AS business_unit_status,
    bu.hierarchy_level,
    bu.parent_business_unit_id
FROM tenants t
LEFT JOIN business_units bu ON t.tenant_id = bu.tenant_id
WHERE t.status = 'ACTIVE' AND (bu.status IS NULL OR bu.status = 'ACTIVE');

-- View for active tenant configurations
CREATE VIEW active_tenant_configs AS
SELECT 
    tc.tenant_id,
    tc.business_unit_id,
    tc.config_key,
    tc.config_value,
    tc.config_type,
    tc.is_encrypted,
    tc.is_sensitive,
    tc.effective_from,
    tc.effective_to
FROM tenant_configs tc
WHERE tc.effective_from <= CURRENT_TIMESTAMP 
  AND (tc.effective_to IS NULL OR tc.effective_to > CURRENT_TIMESTAMP);

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE business_units ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_api_keys ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_audit_log ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
-- Note: These policies will be applied by the application layer
-- The actual policy creation depends on the application's tenant context management

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE tenants IS 'Top-level tenant records for multi-tenancy support';
COMMENT ON TABLE business_units IS 'Divisions within tenants (Retail, Corporate, Investment, etc.)';
COMMENT ON TABLE tenant_configs IS 'Tenant-specific configuration settings with versioning';
COMMENT ON TABLE tenant_users IS 'User assignments to tenants and business units';
COMMENT ON TABLE tenant_api_keys IS 'API keys for programmatic access with rate limiting';
COMMENT ON TABLE tenant_metrics IS 'Usage metrics and billing data per tenant';
COMMENT ON TABLE tenant_audit_log IS 'Audit trail for all tenant-related operations';

COMMENT ON COLUMN tenants.tenant_id IS 'Unique tenant identifier (e.g., STD-001, FNB-001)';
COMMENT ON COLUMN business_units.hierarchy_level IS 'Level in business unit hierarchy (1=top level)';
COMMENT ON COLUMN tenant_configs.is_encrypted IS 'Whether the config value is encrypted at rest';
COMMENT ON COLUMN tenant_configs.is_sensitive IS 'Whether the config contains sensitive data';
COMMENT ON COLUMN tenant_api_keys.api_key_hash IS 'SHA-256 hash of the API key (never store plain text)';
COMMENT ON COLUMN tenant_metrics.metric_value IS 'Numeric value of the metric';
COMMENT ON COLUMN tenant_audit_log.old_values IS 'JSON representation of values before change';
COMMENT ON COLUMN tenant_audit_log.new_values IS 'JSON representation of values after change';
