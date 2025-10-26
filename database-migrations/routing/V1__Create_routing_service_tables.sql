-- =====================================================
-- ROUTING SERVICE DATABASE
-- =====================================================
-- Independent microservice database schema
-- No cross-service dependencies

-- =====================================================
-- ROUTING RULES
-- =====================================================
CREATE TABLE routing_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    rule_description TEXT,
    priority INTEGER NOT NULL DEFAULT 100,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- ROUTING CRITERIA
    service_level VARCHAR(50),
    local_instrument VARCHAR(50),
    category_purpose VARCHAR(50),
    payment_method VARCHAR(50),
    clearing_system VARCHAR(50),
    currency VARCHAR(3),
    min_amount DECIMAL(18,2),
    max_amount DECIMAL(18,2),
    source_account_pattern VARCHAR(200),
    destination_account_pattern VARCHAR(200),
    
    -- ROUTING DECISION
    target_clearing_system VARCHAR(50) NOT NULL,
    target_adapter VARCHAR(50) NOT NULL,
    routing_metadata JSONB,
    
    -- AUDIT FIELDS
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for routing rules
CREATE INDEX idx_routing_rules_tenant_id ON routing_rules(tenant_id);
CREATE INDEX idx_routing_rules_tenant_bu ON routing_rules(tenant_id, business_unit_id);
CREATE INDEX idx_routing_rules_priority ON routing_rules(priority);
CREATE INDEX idx_routing_rules_is_active ON routing_rules(is_active);
CREATE INDEX idx_routing_rules_service_level ON routing_rules(service_level);
CREATE INDEX idx_routing_rules_clearing_system ON routing_rules(clearing_system);
CREATE INDEX idx_routing_rules_currency ON routing_rules(currency);
CREATE INDEX idx_routing_rules_target_clearing_system ON routing_rules(target_clearing_system);

-- =====================================================
-- CLEARING SYSTEMS
-- =====================================================
CREATE TABLE clearing_systems (
    system_id VARCHAR(50) PRIMARY KEY,
    system_name VARCHAR(100) NOT NULL,
    system_type VARCHAR(50) NOT NULL CHECK (system_type IN ('SAMOS', 'BANKSERV_AFRICA', 'RTC', 'PAYSHAP', 'SWIFT', 'CUSTOM')),
    base_url VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    
    -- CONFIGURATION
    timeout_ms INTEGER DEFAULT 5000,
    retry_attempts INTEGER DEFAULT 3,
    circuit_breaker_enabled BOOLEAN DEFAULT TRUE,
    circuit_breaker_failure_threshold INTEGER DEFAULT 5,
    circuit_breaker_wait_duration_ms INTEGER DEFAULT 60000,
    
    -- LIMITS
    min_amount DECIMAL(18,2),
    max_amount DECIMAL(18,2),
    supported_currencies VARCHAR(100),
    supported_payment_types VARCHAR(200),
    
    -- HEALTH CHECK
    health_check_url VARCHAR(200),
    last_health_check TIMESTAMP,
    health_status VARCHAR(20) CHECK (health_status IN ('HEALTHY', 'DEGRADED', 'DOWN', 'UNKNOWN')),
    
    -- AUDIT FIELDS
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for clearing systems
CREATE INDEX idx_clearing_systems_tenant_id ON clearing_systems(tenant_id);
CREATE INDEX idx_clearing_systems_tenant_bu ON clearing_systems(tenant_id, business_unit_id);
CREATE INDEX idx_clearing_systems_system_type ON clearing_systems(system_type);
CREATE INDEX idx_clearing_systems_is_active ON clearing_systems(is_active);
CREATE INDEX idx_clearing_systems_health_status ON clearing_systems(health_status);

-- =====================================================
-- ROUTING DECISIONS (Audit trail)
-- =====================================================
CREATE TABLE routing_decisions (
    decision_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    correlation_id VARCHAR(50) NOT NULL,
    
    -- INPUT CRITERIA
    service_level VARCHAR(50),
    local_instrument VARCHAR(50),
    category_purpose VARCHAR(50),
    payment_method VARCHAR(50),
    clearing_system VARCHAR(50),
    currency VARCHAR(3),
    amount DECIMAL(18,2),
    source_account VARCHAR(50),
    destination_account VARCHAR(50),
    
    -- ROUTING DECISION
    matched_rule_id VARCHAR(50),
    target_clearing_system VARCHAR(50) NOT NULL,
    target_adapter VARCHAR(50) NOT NULL,
    routing_metadata JSONB,
    decision_reason TEXT,
    
    -- AUDIT FIELDS
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processing_time_ms INTEGER,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_routing_decision_rule FOREIGN KEY (matched_rule_id) 
        REFERENCES routing_rules(rule_id),
    CONSTRAINT fk_routing_decision_clearing_system FOREIGN KEY (target_clearing_system) 
        REFERENCES clearing_systems(system_id)
);

-- Indexes for routing decisions
CREATE INDEX idx_routing_decisions_tenant_id ON routing_decisions(tenant_id);
CREATE INDEX idx_routing_decisions_tenant_bu ON routing_decisions(tenant_id, business_unit_id);
CREATE INDEX idx_routing_decisions_payment_id ON routing_decisions(payment_id);
CREATE INDEX idx_routing_decisions_correlation_id ON routing_decisions(correlation_id);
CREATE INDEX idx_routing_decisions_target_clearing_system ON routing_decisions(target_clearing_system);
CREATE INDEX idx_routing_decisions_created_at ON routing_decisions(created_at DESC);

-- =====================================================
-- CLEARING SYSTEM METRICS
-- =====================================================
CREATE TABLE clearing_system_metrics (
    metric_id BIGSERIAL PRIMARY KEY,
    clearing_system_id VARCHAR(50) NOT NULL,
    metric_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- PERFORMANCE METRICS
    total_requests INTEGER NOT NULL DEFAULT 0,
    successful_requests INTEGER NOT NULL DEFAULT 0,
    failed_requests INTEGER NOT NULL DEFAULT 0,
    avg_response_time_ms INTEGER,
    p95_response_time_ms INTEGER,
    p99_response_time_ms INTEGER,
    
    -- CIRCUIT BREAKER METRICS
    circuit_breaker_state VARCHAR(20),
    circuit_breaker_failure_count INTEGER DEFAULT 0,
    
    -- ERROR METRICS
    error_rate DECIMAL(5,2),
    timeout_count INTEGER DEFAULT 0,
    connection_error_count INTEGER DEFAULT 0,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_clearing_metrics_system FOREIGN KEY (clearing_system_id) 
        REFERENCES clearing_systems(system_id)
);

-- Indexes for clearing system metrics
CREATE INDEX idx_clearing_metrics_system_id ON clearing_system_metrics(clearing_system_id);
CREATE INDEX idx_clearing_metrics_tenant_id ON clearing_system_metrics(tenant_id);
CREATE INDEX idx_clearing_metrics_tenant_bu ON clearing_system_metrics(tenant_id, business_unit_id);
CREATE INDEX idx_clearing_metrics_timestamp ON clearing_system_metrics(metric_timestamp DESC);

-- =====================================================
-- ROUTING RULE EVALUATION LOG
-- =====================================================
CREATE TABLE routing_rule_evaluations (
    evaluation_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    correlation_id VARCHAR(50) NOT NULL,
    rule_id VARCHAR(50) NOT NULL,
    
    -- EVALUATION DETAILS
    rule_priority INTEGER NOT NULL,
    evaluation_result BOOLEAN NOT NULL,
    evaluation_reason TEXT,
    evaluation_time_ms INTEGER,
    
    -- AUDIT FIELDS
    evaluated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_routing_evaluation_rule FOREIGN KEY (rule_id) 
        REFERENCES routing_rules(rule_id)
);

-- Indexes for routing rule evaluations
CREATE INDEX idx_routing_evaluations_tenant_id ON routing_rule_evaluations(tenant_id);
CREATE INDEX idx_routing_evaluations_tenant_bu ON routing_rule_evaluations(tenant_id, business_unit_id);
CREATE INDEX idx_routing_evaluations_payment_id ON routing_rule_evaluations(payment_id);
CREATE INDEX idx_routing_evaluations_correlation_id ON routing_rule_evaluations(correlation_id);
CREATE INDEX idx_routing_evaluations_rule_id ON routing_rule_evaluations(rule_id);
CREATE INDEX idx_routing_evaluations_evaluated_at ON routing_rule_evaluations(evaluated_at DESC);

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

CREATE TRIGGER update_routing_rules_updated_at 
    BEFORE UPDATE ON routing_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_clearing_systems_updated_at 
    BEFORE UPDATE ON clearing_systems
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE routing_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE clearing_systems ENABLE ROW LEVEL SECURITY;
ALTER TABLE routing_decisions ENABLE ROW LEVEL SECURITY;
ALTER TABLE clearing_system_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE routing_rule_evaluations ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY tenant_isolation_routing_rules ON routing_rules
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_clearing_systems ON clearing_systems
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_routing_decisions ON routing_decisions
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_clearing_system_metrics ON clearing_system_metrics
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_routing_rule_evaluations ON routing_rule_evaluations
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View for routing performance dashboard
CREATE VIEW routing_performance_dashboard AS
SELECT 
    cs.system_id,
    cs.system_name,
    cs.system_type,
    cs.is_active,
    cs.health_status,
    cs.last_health_check,
    csm.circuit_breaker_state,
    csm.total_requests,
    csm.successful_requests,
    csm.failed_requests,
    csm.avg_response_time_ms,
    csm.error_rate,
    csm.circuit_breaker_failure_count
FROM clearing_systems cs
LEFT JOIN LATERAL (
    SELECT *
    FROM clearing_system_metrics
    WHERE clearing_system_id = cs.system_id
    ORDER BY metric_timestamp DESC
    LIMIT 1
) csm ON true
WHERE cs.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- View for routing rule effectiveness
CREATE VIEW routing_rule_effectiveness AS
SELECT 
    rr.rule_id,
    rr.rule_name,
    rr.priority,
    rr.is_active,
    rr.target_clearing_system,
    COUNT(rd.decision_id) AS total_decisions,
    COUNT(CASE WHEN rd.matched_rule_id = rr.rule_id THEN 1 END) AS matched_decisions,
    ROUND(
        COUNT(CASE WHEN rd.matched_rule_id = rr.rule_id THEN 1 END)::DECIMAL / 
        NULLIF(COUNT(rd.decision_id), 0) * 100, 2
    ) AS match_percentage,
    AVG(rd.processing_time_ms) AS avg_processing_time_ms
FROM routing_rules rr
LEFT JOIN routing_decisions rd ON rr.rule_id = rd.matched_rule_id
WHERE rr.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY rr.rule_id, rr.rule_name, rr.priority, rr.is_active, rr.target_clearing_system;

-- View for clearing system health
CREATE VIEW clearing_system_health AS
SELECT 
    cs.system_id,
    cs.system_name,
    cs.system_type,
    cs.is_active,
    cs.health_status,
    cs.last_health_check,
    csm.circuit_breaker_state,
    csm.circuit_breaker_failure_count,
    csm.error_rate,
    csm.avg_response_time_ms,
    CASE 
        WHEN cs.health_status = 'HEALTHY' AND csm.circuit_breaker_state = 'CLOSED' THEN 'OPERATIONAL'
        WHEN cs.health_status = 'DEGRADED' OR csm.circuit_breaker_state = 'HALF_OPEN' THEN 'DEGRADED'
        WHEN cs.health_status = 'DOWN' OR csm.circuit_breaker_state = 'OPEN' THEN 'DOWN'
        ELSE 'UNKNOWN'
    END AS overall_status
FROM clearing_systems cs
LEFT JOIN LATERAL (
    SELECT *
    FROM clearing_system_metrics
    WHERE clearing_system_id = cs.system_id
    ORDER BY metric_timestamp DESC
    LIMIT 1
) csm ON true
WHERE cs.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- =====================================================
-- FUNCTIONS
-- =====================================================

-- Function to get routing decision for a payment
CREATE OR REPLACE FUNCTION get_routing_decision(
    p_payment_id VARCHAR(50),
    p_service_level VARCHAR(50),
    p_local_instrument VARCHAR(50),
    p_category_purpose VARCHAR(50),
    p_payment_method VARCHAR(50),
    p_clearing_system VARCHAR(50),
    p_currency VARCHAR(3),
    p_amount DECIMAL(18,2),
    p_source_account VARCHAR(50),
    p_destination_account VARCHAR(50)
)
RETURNS TABLE (
    target_clearing_system VARCHAR(50),
    target_adapter VARCHAR(50),
    routing_metadata JSONB,
    decision_reason TEXT
) AS $$
DECLARE
    matched_rule routing_rules%ROWTYPE;
BEGIN
    -- Find the first matching rule ordered by priority
    SELECT * INTO matched_rule
    FROM routing_rules
    WHERE is_active = true
      AND tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
      AND (service_level IS NULL OR service_level = p_service_level)
      AND (local_instrument IS NULL OR local_instrument = p_local_instrument)
      AND (category_purpose IS NULL OR category_purpose = p_category_purpose)
      AND (payment_method IS NULL OR payment_method = p_payment_method)
      AND (clearing_system IS NULL OR clearing_system = p_clearing_system)
      AND (currency IS NULL OR currency = p_currency)
      AND (min_amount IS NULL OR p_amount >= min_amount)
      AND (max_amount IS NULL OR p_amount <= max_amount)
      AND (source_account_pattern IS NULL OR p_source_account ~ source_account_pattern)
      AND (destination_account_pattern IS NULL OR p_destination_account ~ destination_account_pattern)
    ORDER BY priority ASC
    LIMIT 1;
    
    IF matched_rule.rule_id IS NOT NULL THEN
        RETURN QUERY
        SELECT 
            matched_rule.target_clearing_system,
            matched_rule.target_adapter,
            matched_rule.routing_metadata,
            'Matched rule: ' || matched_rule.rule_name AS decision_reason;
    ELSE
        RETURN QUERY
        SELECT 
            'DEFAULT'::VARCHAR(50) AS target_clearing_system,
            'DEFAULT_ADAPTER'::VARCHAR(50) AS target_adapter,
            '{}'::JSONB AS routing_metadata,
            'No matching rule found, using default routing' AS decision_reason;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to get clearing system metrics
CREATE OR REPLACE FUNCTION get_clearing_system_metrics(
    p_clearing_system_id VARCHAR(50),
    p_hours INTEGER DEFAULT 24
)
RETURNS TABLE (
    total_requests BIGINT,
    successful_requests BIGINT,
    failed_requests BIGINT,
    avg_response_time_ms NUMERIC,
    p95_response_time_ms NUMERIC,
    p99_response_time_ms NUMERIC,
    error_rate NUMERIC,
    circuit_breaker_state VARCHAR(20)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT AS total_requests,
        COUNT(CASE WHEN successful_requests > 0 THEN 1 END)::BIGINT AS successful_requests,
        COUNT(CASE WHEN failed_requests > 0 THEN 1 END)::BIGINT AS failed_requests,
        AVG(avg_response_time_ms) AS avg_response_time_ms,
        PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY avg_response_time_ms) AS p95_response_time_ms,
        PERCENTILE_CONT(0.99) WITHIN GROUP (ORDER BY avg_response_time_ms) AS p99_response_time_ms,
        AVG(error_rate) AS error_rate,
        MODE() WITHIN GROUP (ORDER BY circuit_breaker_state) AS circuit_breaker_state
    FROM clearing_system_metrics
    WHERE clearing_system_id = p_clearing_system_id
      AND metric_timestamp > NOW() - INTERVAL '1 hour' * p_hours
      AND tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- SEED DATA
-- =====================================================

-- Insert default clearing systems
INSERT INTO clearing_systems (system_id, system_name, system_type, is_active, tenant_id, business_unit_id, created_by) VALUES
    ('SAMOS', 'SAMOS Clearing System', 'SAMOS', true, 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('BANKSERV_AFRICA', 'BankservAfrica Clearing System', 'BANKSERV_AFRICA', true, 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('RTC', 'Real-Time Clearing System', 'RTC', true, 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('PAYSHAP', 'PayShap Clearing System', 'PAYSHAP', true, 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('SWIFT', 'SWIFT Clearing System', 'SWIFT', true, 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM');

-- Insert default routing rules
INSERT INTO routing_rules (rule_id, rule_name, rule_description, priority, service_level, target_clearing_system, target_adapter, tenant_id, business_unit_id, created_by) VALUES
    ('RULE_PAYSHAP_HIGH_VALUE', 'PayShap High Value Payments', 'Route high-value payments to PayShap', 10, 'HIGH_VALUE', 'PAYSHAP', 'PAYSHAP_ADAPTER', 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('RULE_SAMOS_EFT', 'SAMOS EFT Payments', 'Route EFT payments to SAMOS', 20, 'EFT', 'SAMOS', 'SAMOS_ADAPTER', 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('RULE_RTC_REAL_TIME', 'RTC Real-Time Payments', 'Route real-time payments to RTC', 30, 'REAL_TIME', 'RTC', 'RTC_ADAPTER', 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('RULE_BANKSERV_AFRICA_BULK', 'BankservAfrica Bulk Payments', 'Route bulk payments to BankservAfrica', 40, 'BULK', 'BANKSERV_AFRICA', 'BANKSERV_AFRICA_ADAPTER', 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM'),
    ('RULE_SWIFT_INTERNATIONAL', 'SWIFT International Payments', 'Route international payments to SWIFT', 50, 'INTERNATIONAL', 'SWIFT', 'SWIFT_ADAPTER', 'PLATFORM', 'PLATFORM-DEFAULT', 'SYSTEM');

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE routing_rules IS 'Routing rules for payment clearing system selection';
COMMENT ON TABLE clearing_systems IS 'Configuration for clearing systems';
COMMENT ON TABLE routing_decisions IS 'Audit trail of routing decisions';
COMMENT ON TABLE clearing_system_metrics IS 'Performance metrics for clearing systems';
COMMENT ON TABLE routing_rule_evaluations IS 'Detailed evaluation log for routing rules';

COMMENT ON COLUMN routing_rules.priority IS 'Rule priority (lower number = higher priority)';
COMMENT ON COLUMN routing_rules.service_level IS 'Service level to match (HIGH_VALUE, EFT, REAL_TIME, etc.)';
COMMENT ON COLUMN routing_rules.target_clearing_system IS 'Target clearing system for this rule';
COMMENT ON COLUMN routing_rules.target_adapter IS 'Target adapter for this rule';

COMMENT ON COLUMN clearing_systems.system_type IS 'Type of clearing system (SAMOS, BANKSERV_AFRICA, etc.)';
COMMENT ON COLUMN clearing_systems.health_status IS 'Current health status of the clearing system';
COMMENT ON COLUMN clearing_systems.circuit_breaker_enabled IS 'Whether circuit breaker is enabled';

COMMENT ON COLUMN routing_decisions.matched_rule_id IS 'ID of the rule that matched';
COMMENT ON COLUMN routing_decisions.decision_reason IS 'Reason for the routing decision';
COMMENT ON COLUMN routing_decisions.processing_time_ms IS 'Time taken to make the routing decision';

COMMENT ON COLUMN clearing_system_metrics.circuit_breaker_state IS 'Current circuit breaker state';
COMMENT ON COLUMN clearing_system_metrics.error_rate IS 'Error rate percentage';

COMMENT ON COLUMN routing_rule_evaluations.evaluation_result IS 'Whether the rule matched (true/false)';
COMMENT ON COLUMN routing_rule_evaluations.evaluation_reason IS 'Reason for the evaluation result';
