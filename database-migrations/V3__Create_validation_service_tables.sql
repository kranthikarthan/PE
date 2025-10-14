-- =====================================================
-- VALIDATION SERVICE DATABASE
-- =====================================================
-- Comprehensive validation rules, fraud detection, and limit management

-- =====================================================
-- VALIDATION RULES
-- =====================================================
CREATE TABLE validation_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('LIMIT', 'STATUS', 'KYC', 'FICA', 'FRAUD', 'VELOCITY', 'AMOUNT', 'PATTERN')),
    rule_description TEXT,
    rule_condition JSONB NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for validation rules
CREATE INDEX idx_validation_rules_tenant_id ON validation_rules(tenant_id);
CREATE INDEX idx_validation_rules_tenant_bu ON validation_rules(tenant_id, business_unit_id);
CREATE INDEX idx_validation_rules_active ON validation_rules(active);
CREATE INDEX idx_validation_rules_priority ON validation_rules(priority);
CREATE INDEX idx_validation_rules_type ON validation_rules(rule_type);

-- =====================================================
-- VALIDATION RESULTS
-- =====================================================
CREATE TABLE validation_results (
    validation_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    validation_status VARCHAR(20) NOT NULL CHECK (validation_status IN ('VALID', 'INVALID', 'PENDING')),
    fraud_score DECIMAL(5,4) CHECK (fraud_score >= 0 AND fraud_score <= 1),
    risk_level VARCHAR(20) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    failed_rules JSONB,
    validated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validator_service VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for validation results
CREATE INDEX idx_validation_results_payment_id ON validation_results(payment_id);
CREATE INDEX idx_validation_results_tenant_id ON validation_results(tenant_id);
CREATE INDEX idx_validation_results_tenant_bu ON validation_results(tenant_id, business_unit_id);
CREATE INDEX idx_validation_results_status ON validation_results(validation_status);
CREATE INDEX idx_validation_results_risk_level ON validation_results(risk_level);
CREATE INDEX idx_validation_results_validated_at ON validation_results(validated_at DESC);

-- =====================================================
-- VELOCITY TRACKING (for velocity checks)
-- =====================================================
CREATE TABLE velocity_tracking (
    tracking_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    transaction_count INTEGER NOT NULL DEFAULT 1,
    total_amount DECIMAL(18,2) NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    window_type VARCHAR(20) NOT NULL CHECK (window_type IN ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY')),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for velocity tracking
CREATE INDEX idx_velocity_account ON velocity_tracking(account_number);
CREATE INDEX idx_velocity_tenant_id ON velocity_tracking(tenant_id);
CREATE INDEX idx_velocity_tenant_bu ON velocity_tracking(tenant_id, business_unit_id);
CREATE INDEX idx_velocity_window ON velocity_tracking(window_end);
CREATE INDEX idx_velocity_window_type ON velocity_tracking(window_type);

-- =====================================================
-- FRAUD DETECTION LOG (External Fraud API Calls)
-- =====================================================
CREATE TABLE fraud_detection_log (
    log_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50),
    fraud_score DECIMAL(5,4) NOT NULL CHECK (fraud_score >= 0 AND fraud_score <= 1),
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    recommendation VARCHAR(50) CHECK (recommendation IN ('APPROVE', 'APPROVE_WITH_MONITORING', 'REQUIRE_VERIFICATION', 'REJECT')),
    confidence DECIMAL(5,4),
    fraud_indicators JSONB,
    fraud_reasons JSONB,
    model_version VARCHAR(50),
    api_response_time_ms INTEGER,
    fallback_used BOOLEAN DEFAULT FALSE,
    api_request_data JSONB,
    api_response_data JSONB,
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    api_provider VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for fraud detection log
CREATE INDEX idx_fraud_log_payment_id ON fraud_detection_log(payment_id);
CREATE INDEX idx_fraud_log_tenant_id ON fraud_detection_log(tenant_id);
CREATE INDEX idx_fraud_log_tenant_bu ON fraud_detection_log(tenant_id, business_unit_id);
CREATE INDEX idx_fraud_log_customer_id ON fraud_detection_log(customer_id);
CREATE INDEX idx_fraud_log_score ON fraud_detection_log(fraud_score DESC);
CREATE INDEX idx_fraud_log_risk_level ON fraud_detection_log(risk_level);
CREATE INDEX idx_fraud_log_recommendation ON fraud_detection_log(recommendation);
CREATE INDEX idx_fraud_log_detected_at ON fraud_detection_log(detected_at DESC);
CREATE INDEX idx_fraud_log_fallback ON fraud_detection_log(fallback_used);

-- =====================================================
-- FRAUD API METRICS (Monitor API performance)
-- =====================================================
CREATE TABLE fraud_api_metrics (
    metric_id BIGSERIAL PRIMARY KEY,
    metric_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_calls INTEGER NOT NULL DEFAULT 0,
    successful_calls INTEGER NOT NULL DEFAULT 0,
    failed_calls INTEGER NOT NULL DEFAULT 0,
    timeout_calls INTEGER NOT NULL DEFAULT 0,
    fallback_calls INTEGER NOT NULL DEFAULT 0,
    avg_response_time_ms INTEGER,
    p95_response_time_ms INTEGER,
    p99_response_time_ms INTEGER,
    circuit_breaker_status VARCHAR(20),
    error_rate DECIMAL(5,2),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for fraud API metrics
CREATE INDEX idx_fraud_api_metrics_tenant_id ON fraud_api_metrics(tenant_id);
CREATE INDEX idx_fraud_api_metrics_tenant_bu ON fraud_api_metrics(tenant_id, business_unit_id);
CREATE INDEX idx_fraud_api_metrics_timestamp ON fraud_api_metrics(metric_timestamp DESC);

-- =====================================================
-- FRAUD RULES (Fallback Rule-Based Detection)
-- =====================================================
CREATE TABLE fraud_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('VELOCITY', 'AMOUNT', 'GEOLOCATION', 'DEVICE', 'PATTERN')),
    rule_condition JSONB NOT NULL,
    risk_score_contribution DECIMAL(5,4) NOT NULL,
    priority INTEGER DEFAULT 100,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for fraud rules
CREATE INDEX idx_fraud_rules_tenant_id ON fraud_rules(tenant_id);
CREATE INDEX idx_fraud_rules_tenant_bu ON fraud_rules(tenant_id, business_unit_id);
CREATE INDEX idx_fraud_rules_active ON fraud_rules(active);
CREATE INDEX idx_fraud_rules_type ON fraud_rules(rule_type);

-- =====================================================
-- CUSTOMER LIMITS (Limit Configuration per Customer)
-- =====================================================
CREATE TABLE customer_limits (
    customer_id VARCHAR(50) PRIMARY KEY,
    customer_profile VARCHAR(50) NOT NULL CHECK (customer_profile IN ('INDIVIDUAL_STANDARD', 'INDIVIDUAL_PREMIUM', 'SME', 'CORPORATE')),
    daily_limit DECIMAL(18,2) NOT NULL,
    monthly_limit DECIMAL(18,2) NOT NULL,
    per_transaction_limit DECIMAL(18,2) NOT NULL,
    max_transactions_per_day INTEGER DEFAULT 100,
    max_transactions_per_hour INTEGER DEFAULT 20,
    is_active BOOLEAN DEFAULT TRUE,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for customer limits
CREATE INDEX idx_customer_limits_tenant_id ON customer_limits(tenant_id);
CREATE INDEX idx_customer_limits_tenant_bu ON customer_limits(tenant_id, business_unit_id);
CREATE INDEX idx_customer_limits_profile ON customer_limits(customer_profile);
CREATE INDEX idx_customer_limits_active ON customer_limits(is_active);

-- =====================================================
-- PAYMENT TYPE LIMITS (Limits per Payment Type per Customer)
-- =====================================================
CREATE TABLE payment_type_limits (
    limit_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('EFT', 'RTC', 'RTGS', 'DEBIT_ORDER', 'CARD', 'WALLET')),
    daily_limit DECIMAL(18,2) NOT NULL,
    per_transaction_limit DECIMAL(18,2),
    max_transactions_per_day INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_payment_type_limits_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_payment_type UNIQUE (customer_id, payment_type)
);

-- Indexes for payment type limits
CREATE INDEX idx_payment_type_limits_customer_id ON payment_type_limits(customer_id);
CREATE INDEX idx_payment_type_limits_tenant_id ON payment_type_limits(tenant_id);
CREATE INDEX idx_payment_type_limits_tenant_bu ON payment_type_limits(tenant_id, business_unit_id);
CREATE INDEX idx_payment_type_limits_payment_type ON payment_type_limits(payment_type);

-- =====================================================
-- CUSTOMER LIMIT USAGE (Track Used Limits - Daily/Monthly)
-- =====================================================
CREATE TABLE customer_limit_usage (
    usage_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    usage_date DATE NOT NULL,
    daily_used DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    daily_transaction_count INTEGER NOT NULL DEFAULT 0,
    monthly_used DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    monthly_transaction_count INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_limit_usage_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_usage_date UNIQUE (customer_id, usage_date)
);

-- Indexes for customer limit usage
CREATE INDEX idx_limit_usage_customer_id ON customer_limit_usage(customer_id);
CREATE INDEX idx_limit_usage_tenant_id ON customer_limit_usage(tenant_id);
CREATE INDEX idx_limit_usage_tenant_bu ON customer_limit_usage(tenant_id, business_unit_id);
CREATE INDEX idx_limit_usage_date ON customer_limit_usage(usage_date DESC);

-- =====================================================
-- PAYMENT TYPE LIMIT USAGE (Track Used Limits per Payment Type)
-- =====================================================
CREATE TABLE payment_type_limit_usage (
    usage_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    usage_date DATE NOT NULL,
    daily_used DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    daily_transaction_count INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_payment_type_usage_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_payment_type_usage UNIQUE (customer_id, payment_type, usage_date)
);

-- Indexes for payment type limit usage
CREATE INDEX idx_payment_type_usage_customer ON payment_type_limit_usage(customer_id);
CREATE INDEX idx_payment_type_usage_tenant_id ON payment_type_limit_usage(tenant_id);
CREATE INDEX idx_payment_type_usage_tenant_bu ON payment_type_limit_usage(tenant_id, business_unit_id);
CREATE INDEX idx_payment_type_usage_date ON payment_type_limit_usage(usage_date DESC);

-- =====================================================
-- LIMIT RESERVATIONS (Temporary Holds on Limits Before Payment Execution)
-- =====================================================
CREATE TABLE limit_reservations (
    reservation_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_id VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED' 
        CHECK (status IN ('RESERVED', 'CONSUMED', 'RELEASED', 'EXPIRED')),
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    released_at TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_reservation_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE
);

-- Indexes for limit reservations
CREATE INDEX idx_reservations_customer_id ON limit_reservations(customer_id);
CREATE INDEX idx_reservations_tenant_id ON limit_reservations(tenant_id);
CREATE INDEX idx_reservations_tenant_bu ON limit_reservations(tenant_id, business_unit_id);
CREATE INDEX idx_reservations_payment_id ON limit_reservations(payment_id);
CREATE INDEX idx_reservations_status ON limit_reservations(status);
CREATE INDEX idx_reservations_expires_at ON limit_reservations(expires_at);

-- =====================================================
-- LIMIT USAGE HISTORY (Audit Trail of Limit Operations)
-- =====================================================
CREATE TABLE limit_usage_history (
    history_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    operation VARCHAR(20) NOT NULL CHECK (operation IN ('RESERVE', 'CONSUME', 'RELEASE')),
    daily_used_before DECIMAL(18,2) NOT NULL,
    daily_used_after DECIMAL(18,2) NOT NULL,
    monthly_used_before DECIMAL(18,2) NOT NULL,
    monthly_used_after DECIMAL(18,2) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_limit_history_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE
);

-- Indexes for limit usage history
CREATE INDEX idx_limit_history_customer ON limit_usage_history(customer_id);
CREATE INDEX idx_limit_history_tenant_id ON limit_usage_history(tenant_id);
CREATE INDEX idx_limit_history_tenant_bu ON limit_usage_history(tenant_id, business_unit_id);
CREATE INDEX idx_limit_history_payment_id ON limit_usage_history(payment_id);
CREATE INDEX idx_limit_history_occurred_at ON limit_usage_history(occurred_at DESC);

-- =====================================================
-- TRIGGERS & FUNCTIONS
-- =====================================================

-- Auto-expire limit reservations
CREATE OR REPLACE FUNCTION auto_expire_limit_reservations()
RETURNS void AS $$
BEGIN
    UPDATE limit_reservations 
    SET status = 'EXPIRED',
        released_at = CURRENT_TIMESTAMP
    WHERE status = 'RESERVED' 
      AND expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Function to check if customer has sufficient limit
CREATE OR REPLACE FUNCTION check_customer_limit(
    p_customer_id VARCHAR(50),
    p_amount DECIMAL(18,2),
    p_payment_type VARCHAR(20)
) RETURNS TABLE (
    sufficient BOOLEAN,
    daily_available DECIMAL(18,2),
    monthly_available DECIMAL(18,2),
    payment_type_available DECIMAL(18,2)
) AS $$
DECLARE
    v_daily_limit DECIMAL(18,2);
    v_monthly_limit DECIMAL(18,2);
    v_daily_used DECIMAL(18,2) := 0;
    v_monthly_used DECIMAL(18,2) := 0;
    v_payment_type_limit DECIMAL(18,2);
    v_payment_type_used DECIMAL(18,2) := 0;
    v_daily_avail DECIMAL(18,2);
    v_monthly_avail DECIMAL(18,2);
    v_payment_type_avail DECIMAL(18,2);
BEGIN
    -- Get customer limits
    SELECT daily_limit, monthly_limit
    INTO v_daily_limit, v_monthly_limit
    FROM customer_limits
    WHERE customer_id = p_customer_id AND is_active = TRUE;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Customer % not found', p_customer_id;
    END IF;
    
    -- Get current usage
    SELECT COALESCE(daily_used, 0), COALESCE(monthly_used, 0)
    INTO v_daily_used, v_monthly_used
    FROM customer_limit_usage
    WHERE customer_id = p_customer_id AND usage_date = CURRENT_DATE;
    
    -- Get payment type limits
    SELECT ptl.daily_limit, COALESCE(ptlu.daily_used, 0)
    INTO v_payment_type_limit, v_payment_type_used
    FROM payment_type_limits ptl
    LEFT JOIN payment_type_limit_usage ptlu 
        ON ptl.customer_id = ptlu.customer_id 
        AND ptl.payment_type = ptlu.payment_type 
        AND ptlu.usage_date = CURRENT_DATE
    WHERE ptl.customer_id = p_customer_id 
      AND ptl.payment_type = p_payment_type
      AND ptl.is_active = TRUE;
    
    -- Calculate available limits
    v_daily_avail := v_daily_limit - v_daily_used;
    v_monthly_avail := v_monthly_limit - v_monthly_used;
    v_payment_type_avail := v_payment_type_limit - v_payment_type_used;
    
    -- Return results
    RETURN QUERY SELECT 
        (v_daily_avail >= p_amount AND v_monthly_avail >= p_amount AND v_payment_type_avail >= p_amount),
        v_daily_avail,
        v_monthly_avail,
        v_payment_type_avail;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View for customer limit summary with real-time usage
CREATE VIEW customer_limit_summary AS
SELECT 
    cl.customer_id,
    cl.tenant_id,
    cl.business_unit_id,
    cl.customer_profile,
    cl.daily_limit,
    COALESCE(clu.daily_used, 0) AS daily_used,
    cl.daily_limit - COALESCE(clu.daily_used, 0) AS daily_available,
    cl.monthly_limit,
    COALESCE(clu.monthly_used, 0) AS monthly_used,
    cl.monthly_limit - COALESCE(clu.monthly_used, 0) AS monthly_available,
    cl.per_transaction_limit,
    COALESCE(clu.daily_transaction_count, 0) AS transactions_today,
    cl.max_transactions_per_day,
    cl.max_transactions_per_day - COALESCE(clu.daily_transaction_count, 0) AS transactions_remaining,
    clu.usage_date,
    cl.is_active
FROM customer_limits cl
LEFT JOIN customer_limit_usage clu ON cl.customer_id = clu.customer_id 
    AND clu.usage_date = CURRENT_DATE;

-- View for payment type limit summary
CREATE VIEW payment_type_limit_summary AS
SELECT 
    ptl.customer_id,
    ptl.tenant_id,
    ptl.business_unit_id,
    ptl.payment_type,
    ptl.daily_limit,
    COALESCE(ptlu.daily_used, 0) AS daily_used,
    ptl.daily_limit - COALESCE(ptlu.daily_used, 0) AS daily_available,
    ptl.per_transaction_limit,
    COALESCE(ptlu.daily_transaction_count, 0) AS transactions_today,
    ptl.max_transactions_per_day,
    ptlu.usage_date,
    ptl.is_active
FROM payment_type_limits ptl
LEFT JOIN payment_type_limit_usage ptlu ON ptl.customer_id = ptlu.customer_id 
    AND ptl.payment_type = ptlu.payment_type 
    AND ptlu.usage_date = CURRENT_DATE;

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE validation_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE validation_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE velocity_tracking ENABLE ROW LEVEL SECURITY;
ALTER TABLE fraud_detection_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE fraud_api_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE fraud_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_limits ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_type_limits ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_limit_usage ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_type_limit_usage ENABLE ROW LEVEL SECURITY;
ALTER TABLE limit_reservations ENABLE ROW LEVEL SECURITY;
ALTER TABLE limit_usage_history ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY tenant_isolation_validation_rules ON validation_rules
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_validation_results ON validation_results
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_velocity_tracking ON velocity_tracking
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_fraud_detection_log ON fraud_detection_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_fraud_api_metrics ON fraud_api_metrics
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_fraud_rules ON fraud_rules
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_customer_limits ON customer_limits
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_payment_type_limits ON payment_type_limits
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_customer_limit_usage ON customer_limit_usage
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_payment_type_limit_usage ON payment_type_limit_usage
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_limit_reservations ON limit_reservations
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_limit_usage_history ON limit_usage_history
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- SEED DATA
-- =====================================================

-- Example fraud rules for fallback
INSERT INTO fraud_rules (rule_id, rule_name, rule_type, rule_condition, risk_score_contribution, active, tenant_id, business_unit_id) VALUES
    ('FRAUD-RULE-001', 'High velocity check', 'VELOCITY', '{"max_transactions_per_hour": 10}', 0.30, TRUE, 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('FRAUD-RULE-002', 'Unusual amount', 'AMOUNT', '{"multiplier_of_average": 5}', 0.25, TRUE, 'PLATFORM', 'PLATFORM-DEFAULT'),
    ('FRAUD-RULE-003', 'Foreign IP address', 'GEOLOCATION', '{"allowed_countries": ["ZA"]}', 0.20, TRUE, 'PLATFORM', 'PLATFORM-DEFAULT');

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE validation_rules IS 'Configurable validation rules for payments';
COMMENT ON TABLE validation_results IS 'Results of payment validation checks';
COMMENT ON TABLE velocity_tracking IS 'Transaction velocity tracking for fraud detection';
COMMENT ON TABLE fraud_detection_log IS 'Log of fraud detection API calls and results';
COMMENT ON TABLE fraud_api_metrics IS 'Performance metrics for fraud detection APIs';
COMMENT ON TABLE fraud_rules IS 'Fallback fraud detection rules';
COMMENT ON TABLE customer_limits IS 'Customer-specific transaction limits';
COMMENT ON TABLE payment_type_limits IS 'Payment type specific limits per customer';
COMMENT ON TABLE customer_limit_usage IS 'Daily and monthly limit usage tracking';
COMMENT ON TABLE payment_type_limit_usage IS 'Payment type specific usage tracking';
COMMENT ON TABLE limit_reservations IS 'Temporary holds on customer limits';
COMMENT ON TABLE limit_usage_history IS 'Audit trail of limit operations';

COMMENT ON COLUMN validation_rules.rule_condition IS 'JSONB condition for rule evaluation';
COMMENT ON COLUMN validation_results.fraud_score IS 'Fraud risk score from 0.0 to 1.0';
COMMENT ON COLUMN validation_results.failed_rules IS 'JSONB array of rules that failed';
COMMENT ON COLUMN fraud_detection_log.fraud_indicators IS 'JSONB array of fraud indicators detected';
COMMENT ON COLUMN fraud_detection_log.fraud_reasons IS 'JSONB array of reasons for fraud score';
COMMENT ON COLUMN customer_limits.customer_profile IS 'Customer risk profile for limit calculation';
COMMENT ON COLUMN limit_reservations.status IS 'Current status of the limit reservation';
COMMENT ON COLUMN limit_usage_history.operation IS 'Type of limit operation performed';
