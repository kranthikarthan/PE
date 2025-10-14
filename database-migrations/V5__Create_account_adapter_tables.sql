-- =====================================================
-- ACCOUNT ADAPTER SERVICE DATABASE
-- =====================================================
-- Routing metadata, caching, and external system integration

-- =====================================================
-- ACCOUNT ROUTING (determines which backend system to call)
-- =====================================================
CREATE TABLE account_routing (
    account_number VARCHAR(50) PRIMARY KEY,
    backend_system VARCHAR(50) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    base_url VARCHAR(200) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_verified TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for account routing
CREATE INDEX idx_routing_backend_system ON account_routing(backend_system);
CREATE INDEX idx_routing_tenant_id ON account_routing(tenant_id);
CREATE INDEX idx_routing_tenant_bu ON account_routing(tenant_id, business_unit_id);
CREATE INDEX idx_routing_account_type ON account_routing(account_type);
CREATE INDEX idx_routing_is_active ON account_routing(is_active);

-- =====================================================
-- BACKEND SYSTEMS (external core banking systems)
-- =====================================================
CREATE TABLE backend_systems (
    system_id VARCHAR(50) PRIMARY KEY,
    system_name VARCHAR(100) NOT NULL UNIQUE,
    base_url VARCHAR(200) NOT NULL,
    auth_type VARCHAR(20) NOT NULL CHECK (auth_type IN ('OAUTH2', 'MTLS', 'BASIC', 'API_KEY')),
    oauth_token_url VARCHAR(200),
    oauth_client_id VARCHAR(100),
    oauth_client_secret_key_vault_ref VARCHAR(200),
    timeout_ms INTEGER DEFAULT 5000,
    retry_attempts INTEGER DEFAULT 3,
    circuit_breaker_enabled BOOLEAN DEFAULT TRUE,
    circuit_breaker_failure_threshold INTEGER DEFAULT 5,
    circuit_breaker_wait_duration_ms INTEGER DEFAULT 60000,
    is_active BOOLEAN DEFAULT TRUE,
    health_check_url VARCHAR(200),
    last_health_check TIMESTAMP,
    health_status VARCHAR(20) CHECK (health_status IN ('HEALTHY', 'DEGRADED', 'DOWN', 'UNKNOWN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for backend systems
CREATE INDEX idx_backend_systems_tenant_id ON backend_systems(tenant_id);
CREATE INDEX idx_backend_systems_tenant_bu ON backend_systems(tenant_id, business_unit_id);
CREATE INDEX idx_backend_systems_is_active ON backend_systems(is_active);
CREATE INDEX idx_backend_systems_health_status ON backend_systems(health_status);
CREATE INDEX idx_backend_systems_auth_type ON backend_systems(auth_type);

-- =====================================================
-- ACCOUNT CACHE (temporary cache of account data)
-- =====================================================
CREATE TABLE account_cache (
    account_number VARCHAR(50) PRIMARY KEY,
    account_data JSONB NOT NULL,
    backend_system VARCHAR(50) NOT NULL,
    cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    hit_count INTEGER DEFAULT 0,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for account cache
CREATE INDEX idx_cache_expires_at ON account_cache(expires_at);
CREATE INDEX idx_cache_tenant_id ON account_cache(tenant_id);
CREATE INDEX idx_cache_tenant_bu ON account_cache(tenant_id, business_unit_id);
CREATE INDEX idx_cache_backend_system ON account_cache(backend_system);
CREATE INDEX idx_cache_cached_at ON account_cache(cached_at DESC);

-- =====================================================
-- API CALL LOG (audit trail of external system calls)
-- =====================================================
CREATE TABLE api_call_log (
    call_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(50),
    backend_system VARCHAR(50) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    http_method VARCHAR(10),
    endpoint_url VARCHAR(500),
    request_data JSONB,
    response_status INTEGER,
    response_data JSONB,
    response_time_ms INTEGER,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    idempotency_key VARCHAR(100),
    correlation_id VARCHAR(100),
    called_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for API call log
CREATE INDEX idx_api_log_account_number ON api_call_log(account_number);
CREATE INDEX idx_api_log_tenant_id ON api_call_log(tenant_id);
CREATE INDEX idx_api_log_tenant_bu ON api_call_log(tenant_id, business_unit_id);
CREATE INDEX idx_api_log_backend_system ON api_call_log(backend_system);
CREATE INDEX idx_api_log_operation ON api_call_log(operation);
CREATE INDEX idx_api_log_called_at ON api_call_log(called_at DESC);
CREATE INDEX idx_api_log_success ON api_call_log(success);
CREATE INDEX idx_api_log_idempotency_key ON api_call_log(idempotency_key);
CREATE INDEX idx_api_log_correlation_id ON api_call_log(correlation_id);

-- =====================================================
-- BACKEND SYSTEM METRICS (monitoring)
-- =====================================================
CREATE TABLE backend_system_metrics (
    metric_id BIGSERIAL PRIMARY KEY,
    backend_system VARCHAR(50) NOT NULL,
    metric_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_calls INTEGER NOT NULL DEFAULT 0,
    successful_calls INTEGER NOT NULL DEFAULT 0,
    failed_calls INTEGER NOT NULL DEFAULT 0,
    avg_response_time_ms INTEGER,
    p95_response_time_ms INTEGER,
    p99_response_time_ms INTEGER,
    circuit_breaker_status VARCHAR(20),
    error_rate DECIMAL(5,2),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_metrics_backend_system FOREIGN KEY (backend_system) 
        REFERENCES backend_systems(system_id)
);

-- Indexes for backend system metrics
CREATE INDEX idx_metrics_backend_system ON backend_system_metrics(backend_system);
CREATE INDEX idx_metrics_tenant_id ON backend_system_metrics(tenant_id);
CREATE INDEX idx_metrics_tenant_bu ON backend_system_metrics(tenant_id, business_unit_id);
CREATE INDEX idx_metrics_timestamp ON backend_system_metrics(metric_timestamp DESC);

-- =====================================================
-- IDEMPOTENCY TRACKING (for backend calls)
-- =====================================================
CREATE TABLE idempotency_records (
    idempotency_key VARCHAR(100) PRIMARY KEY,
    account_number VARCHAR(50),
    operation VARCHAR(50) NOT NULL,
    backend_system VARCHAR(50) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_data JSONB,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for idempotency records
CREATE INDEX idx_idempotency_expires_at ON idempotency_records(expires_at);
CREATE INDEX idx_idempotency_tenant_id ON idempotency_records(tenant_id);
CREATE INDEX idx_idempotency_tenant_bu ON idempotency_records(tenant_id, business_unit_id);
CREATE INDEX idx_idempotency_account_number ON idempotency_records(account_number);
CREATE INDEX idx_idempotency_status ON idempotency_records(status);

-- =====================================================
-- CIRCUIT BREAKER STATE (track circuit breaker status)
-- =====================================================
CREATE TABLE circuit_breaker_state (
    backend_system VARCHAR(50) PRIMARY KEY,
    state VARCHAR(20) NOT NULL CHECK (state IN ('CLOSED', 'OPEN', 'HALF_OPEN')),
    failure_count INTEGER DEFAULT 0,
    last_failure TIMESTAMP,
    last_success TIMESTAMP,
    state_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_retry_at TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_circuit_backend_system FOREIGN KEY (backend_system) 
        REFERENCES backend_systems(system_id)
);

-- Indexes for circuit breaker state
CREATE INDEX idx_circuit_state ON circuit_breaker_state(state);
CREATE INDEX idx_circuit_tenant_id ON circuit_breaker_state(tenant_id);
CREATE INDEX idx_circuit_tenant_bu ON circuit_breaker_state(tenant_id, business_unit_id);
CREATE INDEX idx_circuit_next_retry_at ON circuit_breaker_state(next_retry_at);

-- =====================================================
-- TRIGGERS & FUNCTIONS
-- =====================================================

-- Auto-cleanup expired cache entries
CREATE OR REPLACE FUNCTION cleanup_expired_cache()
RETURNS void AS $$
BEGIN
    DELETE FROM account_cache WHERE expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Auto-cleanup expired idempotency records
CREATE OR REPLACE FUNCTION cleanup_expired_idempotency()
RETURNS void AS $$
BEGIN
    DELETE FROM idempotency_records WHERE expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_backend_systems_updated_at 
    BEFORE UPDATE ON backend_systems
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_account_routing_updated_at 
    BEFORE UPDATE ON account_routing
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE account_routing ENABLE ROW LEVEL SECURITY;
ALTER TABLE backend_systems ENABLE ROW LEVEL SECURITY;
ALTER TABLE account_cache ENABLE ROW LEVEL SECURITY;
ALTER TABLE api_call_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE backend_system_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE idempotency_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE circuit_breaker_state ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY tenant_isolation_account_routing ON account_routing
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_backend_systems ON backend_systems
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_account_cache ON account_cache
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_api_call_log ON api_call_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_backend_system_metrics ON backend_system_metrics
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_idempotency_records ON idempotency_records
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_circuit_breaker_state ON circuit_breaker_state
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View for backend system health dashboard
CREATE VIEW backend_system_health AS
SELECT 
    bs.system_id,
    bs.system_name,
    bs.base_url,
    bs.is_active,
    bs.health_status,
    bs.last_health_check,
    cbs.state AS circuit_breaker_state,
    cbs.failure_count,
    (SELECT COUNT(*) FROM api_call_log 
     WHERE backend_system = bs.system_id 
       AND called_at > NOW() - INTERVAL '1 hour') AS calls_last_hour,
    (SELECT COUNT(*) FROM api_call_log 
     WHERE backend_system = bs.system_id 
       AND success = false 
       AND called_at > NOW() - INTERVAL '1 hour') AS failures_last_hour,
    (SELECT AVG(response_time_ms) FROM api_call_log 
     WHERE backend_system = bs.system_id 
       AND called_at > NOW() - INTERVAL '1 hour') AS avg_response_time_ms
FROM backend_systems bs
LEFT JOIN circuit_breaker_state cbs ON bs.system_id = cbs.backend_system
WHERE bs.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- View for account routing lookup
CREATE VIEW account_routing_info AS
SELECT 
    ar.account_number,
    ar.account_type,
    ar.backend_system,
    bs.system_name,
    bs.base_url,
    bs.is_active AS system_active,
    bs.health_status,
    ar.is_active AS routing_active,
    ar.last_verified
FROM account_routing ar
JOIN backend_systems bs ON ar.backend_system = bs.system_id
WHERE ar.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- View for API call performance metrics
CREATE VIEW api_call_performance AS
SELECT 
    acl.backend_system,
    acl.operation,
    DATE(acl.called_at) AS call_date,
    COUNT(*) AS total_calls,
    COUNT(CASE WHEN acl.success = true THEN 1 END) AS successful_calls,
    COUNT(CASE WHEN acl.success = false THEN 1 END) AS failed_calls,
    AVG(acl.response_time_ms) AS avg_response_time_ms,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY acl.response_time_ms) AS p95_response_time_ms,
    PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY acl.response_time_ms) AS p99_response_time_ms
FROM api_call_log acl
WHERE acl.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY acl.backend_system, acl.operation, DATE(acl.called_at);

-- =====================================================
-- SEED DATA
-- =====================================================

-- Insert default backend systems
INSERT INTO backend_systems (system_id, system_name, base_url, auth_type, health_check_url, tenant_id, business_unit_id) VALUES
    ('CURRENT_ACCOUNTS', 'Current Accounts System', 'https://current-accounts.bank.internal/api/v1', 'OAUTH2', 'https://current-accounts.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('SAVINGS', 'Savings System', 'https://savings.bank.internal/api/v1', 'OAUTH2', 'https://savings.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('INVESTMENTS', 'Investment System', 'https://investments.bank.internal/api/v1', 'OAUTH2', 'https://investments.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('CARDS', 'Card System', 'https://cards.bank.internal/api/v1', 'OAUTH2', 'https://cards.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('HOME_LOANS', 'Home Loan System', 'https://home-loans.bank.internal/api/v1', 'OAUTH2', 'https://home-loans.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('CAR_LOANS', 'Car Loan System', 'https://vehicle-finance.bank.internal/api/v1', 'OAUTH2', 'https://vehicle-finance.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('PERSONAL_LOANS', 'Personal Loan System', 'https://personal-loans.bank.internal/api/v1', 'OAUTH2', 'https://personal-loans.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('BUSINESS_BANKING', 'Business Banking System', 'https://business-banking.bank.internal/api/v1', 'OAUTH2', 'https://business-banking.bank.internal/health', 'PLATFORM', 'PLATFORM-DEFAULT');

-- =====================================================
-- FUNCTIONS
-- =====================================================

-- Function to get account routing information
CREATE OR REPLACE FUNCTION get_account_routing(p_account_number VARCHAR(50))
RETURNS TABLE (
    account_number VARCHAR(50),
    backend_system VARCHAR(50),
    system_name VARCHAR(100),
    base_url VARCHAR(200),
    account_type VARCHAR(30),
    is_active BOOLEAN,
    health_status VARCHAR(20)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ar.account_number,
        ar.backend_system,
        bs.system_name,
        bs.base_url,
        ar.account_type,
        ar.is_active,
        bs.health_status
    FROM account_routing ar
    JOIN backend_systems bs ON ar.backend_system = bs.system_id
    WHERE ar.account_number = p_account_number
      AND ar.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;
END;
$$ LANGUAGE plpgsql;

-- Function to get backend system metrics
CREATE OR REPLACE FUNCTION get_backend_system_metrics(p_backend_system VARCHAR(50), p_hours INTEGER DEFAULT 24)
RETURNS TABLE (
    total_calls BIGINT,
    successful_calls BIGINT,
    failed_calls BIGINT,
    avg_response_time_ms NUMERIC,
    p95_response_time_ms NUMERIC,
    p99_response_time_ms NUMERIC,
    error_rate NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT AS total_calls,
        COUNT(CASE WHEN acl.success = true THEN 1 END)::BIGINT AS successful_calls,
        COUNT(CASE WHEN acl.success = false THEN 1 END)::BIGINT AS failed_calls,
        AVG(acl.response_time_ms) AS avg_response_time_ms,
        PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY acl.response_time_ms) AS p95_response_time_ms,
        PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY acl.response_time_ms) AS p99_response_time_ms,
        (COUNT(CASE WHEN acl.success = false THEN 1 END)::NUMERIC / COUNT(*)::NUMERIC * 100) AS error_rate
    FROM api_call_log acl
    WHERE acl.backend_system = p_backend_system
      AND acl.called_at > NOW() - INTERVAL '1 hour' * p_hours
      AND acl.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE account_routing IS 'Routing configuration for account numbers to backend systems';
COMMENT ON TABLE backend_systems IS 'Configuration for external core banking systems';
COMMENT ON TABLE account_cache IS 'Temporary cache of account data from backend systems';
COMMENT ON TABLE api_call_log IS 'Audit trail of all API calls to backend systems';
COMMENT ON TABLE backend_system_metrics IS 'Performance metrics for backend systems';
COMMENT ON TABLE idempotency_records IS 'Idempotency tracking for backend API calls';
COMMENT ON TABLE circuit_breaker_state IS 'Circuit breaker state for backend systems';

COMMENT ON COLUMN account_routing.backend_system IS 'Backend system identifier for routing';
COMMENT ON COLUMN account_routing.account_type IS 'Type of account (CURRENT, SAVINGS, etc.)';
COMMENT ON COLUMN account_routing.is_active IS 'Whether the routing is currently active';

COMMENT ON COLUMN backend_systems.auth_type IS 'Authentication type for the backend system';
COMMENT ON COLUMN backend_systems.oauth_client_secret_key_vault_ref IS 'Reference to secret in Azure Key Vault';
COMMENT ON COLUMN backend_systems.circuit_breaker_enabled IS 'Whether circuit breaker is enabled';
COMMENT ON COLUMN backend_systems.health_status IS 'Current health status of the backend system';

COMMENT ON COLUMN account_cache.account_data IS 'JSONB containing cached account information';
COMMENT ON COLUMN account_cache.hit_count IS 'Number of times this cache entry has been accessed';
COMMENT ON COLUMN account_cache.expires_at IS 'When this cache entry expires';

COMMENT ON COLUMN api_call_log.request_data IS 'JSONB containing the request payload';
COMMENT ON COLUMN api_call_log.response_data IS 'JSONB containing the response payload';
COMMENT ON COLUMN api_call_log.idempotency_key IS 'Idempotency key for duplicate request prevention';
COMMENT ON COLUMN api_call_log.correlation_id IS 'Correlation ID for distributed tracing';

COMMENT ON COLUMN idempotency_records.request_hash IS 'SHA-256 hash of the request for duplicate detection';
COMMENT ON COLUMN idempotency_records.response_data IS 'Cached response data for duplicate requests';
COMMENT ON COLUMN idempotency_records.status IS 'Current status of the idempotency record';

COMMENT ON COLUMN circuit_breaker_state.state IS 'Current circuit breaker state';
COMMENT ON COLUMN circuit_breaker_state.failure_count IS 'Number of consecutive failures';
COMMENT ON COLUMN circuit_breaker_state.next_retry_at IS 'When the next retry attempt is allowed';
