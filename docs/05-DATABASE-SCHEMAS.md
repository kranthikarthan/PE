# Database Schemas - Complete Design

## Overview
This document contains complete database schemas for all microservices. Each service owns its database (database-per-service pattern).

> **📋 PRIMARY REFERENCE**: This document is aligned with the **Enhanced Feature Breakdown Tree** (`docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`). The system follows an **8-phase implementation strategy** with **22 microservices** and **50 features**.

---

## Database Technology Mapping (22 Services)

| # | Service Name | Phase | Database | Reason |
|---|--------------|-------|----------|--------|
| **Core Services (Phase 1)** |
| 1 | Payment Initiation | Phase 1 | PostgreSQL | ACID compliance, complex queries |
| 2 | Validation Service | Phase 1 | PostgreSQL + Redis | Structured data + caching rules |
| 3 | Account Adapter | Phase 1 | PostgreSQL | ACID compliance, financial data |
| 4 | Routing Service | Phase 1 | Redis | Fast lookups, caching |
| 5 | Transaction Processing | Phase 1 | PostgreSQL | Event sourcing, ledger |
| 6 | Saga Orchestrator | Phase 1 | PostgreSQL | State machine, consistency |
| **Clearing Adapters (Phase 2)** |
| 7 | SAMOS Adapter | Phase 2 | PostgreSQL | Message tracking, audit trail |
| 8 | BankservAfrica Adapter | Phase 2 | PostgreSQL | Message tracking, audit trail |
| 9 | RTC Adapter | Phase 2 | PostgreSQL | Message tracking, audit trail |
| 10 | PayShap Adapter | Phase 2 | PostgreSQL | Message tracking, audit trail |
| 11 | SWIFT Adapter | Phase 2 | PostgreSQL | Message tracking, audit trail |
| **Platform Services (Phase 3)** |
| 12 | Tenant Management | Phase 3 | PostgreSQL | Multi-tenancy, hierarchy, configs |
| 13 | IAM Service | Phase 3 | PostgreSQL + Azure AD | User data + identity |
| 14 | Audit Service | Phase 3 | CosmosDB | High write throughput, append-only |
| 15 | Notification Service | Phase 3 | PostgreSQL | Delivery tracking |
| 16 | Reporting Service | Phase 3 | PostgreSQL + Synapse | OLTP + OLAP |
| **Advanced Features (Phase 4)** |
| 17 | Batch Processing | Phase 4 | PostgreSQL | Financial calculations, batching |
| 18 | Settlement Service | Phase 4 | PostgreSQL | Financial calculations, batching |
| 19 | Reconciliation Service | Phase 4 | PostgreSQL | Complex matching queries |
| 20 | Internal API Gateway | Phase 4 | Redis | Fast lookups, caching |
| **Operations & Channel Management (Phase 7)** |
| 21 | Operations Management | Phase 7 | PostgreSQL | Service monitoring, circuit breakers |
| 22 | Metrics Aggregation | Phase 7 | PostgreSQL + Redis | Metrics storage + caching |

---

## 0. Tenant Management Service Database (NEW)

### Database Name: `tenant_management_db`

**⚠️ IMPORTANT**: This is the foundational database that must be deployed FIRST. All other services depend on tenant data.

**Multi-Tenancy Model**: 3-level hierarchy
- **Level 1**: Tenant (Bank/Financial Institution)
- **Level 2**: Business Unit (Division: Retail, Corporate, Investment)
- **Level 3**: Customer (End user)

**Data Isolation**: Every table in every service has `tenant_id` column with PostgreSQL Row-Level Security (RLS) policies.

```sql
-- For complete schema, see docs/12-TENANT-MANAGEMENT.md
-- Below is a summary of key tables:

-- 1. tenants: Top-level tenant records
-- 2. business_units: Divisions within tenants
-- 3. tenant_configs: Tenant-specific configurations
-- 4. tenant_users: Admin users per tenant
-- 5. tenant_api_keys: API keys for programmatic access
-- 6. tenant_metrics: Usage tracking per tenant
-- 7. tenant_audit_log: Audit trail for tenant operations

-- See Section 12-TENANT-MANAGEMENT.md for:
-- - Complete table definitions
-- - Row-Level Security policies
-- - Tenant onboarding procedures
-- - Tenant context propagation
-- - Configuration management
```

---

## 1. Payment Initiation Service Database

### Database Name: `payment_initiation_db`

```sql
-- =====================================================
-- PAYMENTS TABLE
-- =====================================================
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    
    -- MULTI-TENANCY (NEW)
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    idempotency_key VARCHAR(100) NOT NULL,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    reference VARCHAR(200),
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('EFT', 'RTC', 'RTGS', 'DEBIT_ORDER')),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    priority VARCHAR(10) DEFAULT 'NORMAL' CHECK (priority IN ('NORMAL', 'HIGH', 'URGENT')),
    initiated_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    CONSTRAINT chk_different_accounts CHECK (source_account != destination_account),
    CONSTRAINT uk_idempotency_tenant UNIQUE (tenant_id, idempotency_key)
);

-- Indexes
CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_payments_tenant_bu ON payments(tenant_id, business_unit_id);
CREATE INDEX idx_payments_tenant_status ON payments(tenant_id, status);
CREATE INDEX idx_payments_source_account ON payments(tenant_id, source_account);
CREATE INDEX idx_payments_destination_account ON payments(tenant_id, destination_account);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);
CREATE INDEX idx_payments_initiated_by ON payments(initiated_by);
CREATE INDEX idx_payments_composite ON payments(tenant_id, source_account, created_at DESC);

-- Row-Level Security
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON payments
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- PAYMENT STATUS HISTORY
-- =====================================================
CREATE TABLE payment_status_history (
    history_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    
    CONSTRAINT fk_payment_status_history FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_status_history_payment_id ON payment_status_history(payment_id);

-- =====================================================
-- DEBIT ORDER DETAILS (for DEBIT_ORDER payment type)
-- =====================================================
CREATE TABLE debit_order_details (
    debit_order_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    mandate_reference VARCHAR(100) NOT NULL,
    mandate_date DATE NOT NULL,
    max_amount DECIMAL(18,2),
    frequency VARCHAR(20) CHECK (frequency IN ('ONCE_OFF', 'MONTHLY', 'WEEKLY', 'DAILY')),
    debicheck_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_debit_order_payment FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

CREATE INDEX idx_debit_order_payment_id ON debit_order_details(payment_id);

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

CREATE TRIGGER update_payments_updated_at 
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Auto-track status changes
CREATE OR REPLACE FUNCTION track_payment_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO payment_status_history (payment_id, from_status, to_status, reason)
        VALUES (NEW.payment_id, OLD.status, NEW.status, 'Status changed');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER track_payment_status 
    AFTER UPDATE OF status ON payments
    FOR EACH ROW EXECUTE FUNCTION track_payment_status_change();
```

---

## 2. Validation Service Database

### Database Name: `validation_db`

```sql
-- =====================================================
-- VALIDATION RULES
-- =====================================================
CREATE TABLE validation_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('LIMIT', 'STATUS', 'KYC', 'FICA', 'FRAUD', 'VELOCITY')),
    rule_description TEXT,
    rule_condition JSONB NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX idx_validation_rules_active ON validation_rules(active);
CREATE INDEX idx_validation_rules_priority ON validation_rules(priority);
CREATE INDEX idx_validation_rules_type ON validation_rules(rule_type);

-- Example rule condition (JSONB):
-- {
--   "field": "amount",
--   "operator": "<=",
--   "value": 50000,
--   "errorMessage": "Daily limit exceeded"
-- }

-- =====================================================
-- VALIDATION RESULTS
-- =====================================================
CREATE TABLE validation_results (
    validation_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    validation_status VARCHAR(20) NOT NULL CHECK (validation_status IN ('VALID', 'INVALID')),
    fraud_score DECIMAL(5,4) CHECK (fraud_score >= 0 AND fraud_score <= 1),
    risk_level VARCHAR(20) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    failed_rules JSONB,
    validated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validator_service VARCHAR(100)
);

CREATE INDEX idx_validation_results_payment_id ON validation_results(payment_id);
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
    window_type VARCHAR(20) NOT NULL CHECK (window_type IN ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY'))
);

CREATE INDEX idx_velocity_account ON velocity_tracking(account_number);
CREATE INDEX idx_velocity_window ON velocity_tracking(window_end);

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
    api_provider VARCHAR(100)
);

CREATE INDEX idx_fraud_log_payment_id ON fraud_detection_log(payment_id);
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
    error_rate DECIMAL(5,2)
);

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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fraud_rules_active ON fraud_rules(active);
CREATE INDEX idx_fraud_rules_type ON fraud_rules(rule_type);

-- Example fraud rules for fallback
INSERT INTO fraud_rules (rule_id, rule_name, rule_type, rule_condition, risk_score_contribution, active) VALUES
    ('FRAUD-RULE-001', 'High velocity check', 'VELOCITY', '{"max_transactions_per_hour": 10}', 0.30, TRUE),
    ('FRAUD-RULE-002', 'Unusual amount', 'AMOUNT', '{"multiplier_of_average": 5}', 0.25, TRUE),
    ('FRAUD-RULE-003', 'Foreign IP address', 'GEOLOCATION', '{"allowed_countries": ["ZA"]}', 0.20, TRUE);

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
    updated_by VARCHAR(100)
);

CREATE INDEX idx_customer_limits_profile ON customer_limits(customer_profile);
CREATE INDEX idx_customer_limits_active ON customer_limits(is_active);

-- =====================================================
-- PAYMENT TYPE LIMITS (Limits per Payment Type per Customer)
-- =====================================================
CREATE TABLE payment_type_limits (
    limit_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('EFT', 'RTC', 'RTGS', 'DEBIT_ORDER', 'CARD')),
    daily_limit DECIMAL(18,2) NOT NULL,
    per_transaction_limit DECIMAL(18,2),
    max_transactions_per_day INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_type_limits_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_payment_type UNIQUE (customer_id, payment_type)
);

CREATE INDEX idx_payment_type_limits_customer_id ON payment_type_limits(customer_id);
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
    
    CONSTRAINT fk_limit_usage_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_usage_date UNIQUE (customer_id, usage_date)
);

CREATE INDEX idx_limit_usage_customer_id ON customer_limit_usage(customer_id);
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
    
    CONSTRAINT fk_payment_type_usage_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_payment_type_usage UNIQUE (customer_id, payment_type, usage_date)
);

CREATE INDEX idx_payment_type_usage_customer ON payment_type_limit_usage(customer_id);
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
    
    CONSTRAINT fk_reservation_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE
);

CREATE INDEX idx_reservations_customer_id ON limit_reservations(customer_id);
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
    
    CONSTRAINT fk_limit_history_customer FOREIGN KEY (customer_id) 
        REFERENCES customer_limits(customer_id) ON DELETE CASCADE
);

CREATE INDEX idx_limit_history_customer ON limit_usage_history(customer_id);
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
-- SEED DATA (Example Customer Profiles)
-- =====================================================

-- Example: Individual Standard customer
INSERT INTO customer_limits (customer_id, customer_profile, daily_limit, monthly_limit, per_transaction_limit, effective_from)
VALUES ('CUST-EXAMPLE-001', 'INDIVIDUAL_STANDARD', 50000.00, 200000.00, 25000.00, CURRENT_DATE);

-- Example payment type limits for the customer
INSERT INTO payment_type_limits (customer_id, payment_type, daily_limit, per_transaction_limit)
VALUES 
    ('CUST-EXAMPLE-001', 'EFT', 25000.00, 10000.00),
    ('CUST-EXAMPLE-001', 'RTC', 50000.00, 25000.00),
    ('CUST-EXAMPLE-001', 'RTGS', 50000.00, 50000.00);
```

---

## 3. Account Adapter Service Database

### Database Name: `account_adapter_db`

**Note**: This service does NOT store account balances or transaction data. It only stores routing metadata and caches responses from external core banking systems.

```sql
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_routing_backend_system ON account_routing(backend_system);
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_backend_systems_is_active ON backend_systems(is_active);
CREATE INDEX idx_backend_systems_health_status ON backend_systems(health_status);

-- Example data
INSERT INTO backend_systems (system_id, system_name, base_url, auth_type, health_check_url) VALUES
    ('CURRENT_ACCOUNTS', 'Current Accounts System', 'https://current-accounts.bank.internal/api/v1', 'OAUTH2', 'https://current-accounts.bank.internal/health'),
    ('SAVINGS', 'Savings System', 'https://savings.bank.internal/api/v1', 'OAUTH2', 'https://savings.bank.internal/health'),
    ('INVESTMENTS', 'Investment System', 'https://investments.bank.internal/api/v1', 'OAUTH2', 'https://investments.bank.internal/health'),
    ('CARDS', 'Card System', 'https://cards.bank.internal/api/v1', 'OAUTH2', 'https://cards.bank.internal/health'),
    ('HOME_LOANS', 'Home Loan System', 'https://home-loans.bank.internal/api/v1', 'OAUTH2', 'https://home-loans.bank.internal/health'),
    ('CAR_LOANS', 'Car Loan System', 'https://vehicle-finance.bank.internal/api/v1', 'OAUTH2', 'https://vehicle-finance.bank.internal/health'),
    ('PERSONAL_LOANS', 'Personal Loan System', 'https://personal-loans.bank.internal/api/v1', 'OAUTH2', 'https://personal-loans.bank.internal/health'),
    ('BUSINESS_BANKING', 'Business Banking System', 'https://business-banking.bank.internal/api/v1', 'OAUTH2', 'https://business-banking.bank.internal/health');

-- =====================================================
-- ACCOUNT CACHE (temporary cache of account data)
-- =====================================================
CREATE TABLE account_cache (
    account_number VARCHAR(50) PRIMARY KEY,
    account_data JSONB NOT NULL,
    backend_system VARCHAR(50) NOT NULL,
    cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    hit_count INTEGER DEFAULT 0
);

CREATE INDEX idx_cache_expires_at ON account_cache(expires_at);
CREATE INDEX idx_cache_backend_system ON account_cache(backend_system);

-- Auto-cleanup expired cache entries
CREATE OR REPLACE FUNCTION cleanup_expired_cache()
RETURNS void AS $$
BEGIN
    DELETE FROM account_cache WHERE expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

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
    called_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_api_log_account_number ON api_call_log(account_number);
CREATE INDEX idx_api_log_backend_system ON api_call_log(backend_system);
CREATE INDEX idx_api_log_operation ON api_call_log(operation);
CREATE INDEX idx_api_log_called_at ON api_call_log(called_at DESC);
CREATE INDEX idx_api_log_success ON api_call_log(success);
CREATE INDEX idx_api_log_idempotency_key ON api_call_log(idempotency_key);

-- Partition by month for better performance
-- CREATE TABLE api_call_log_2025_10 PARTITION OF api_call_log
--     FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');

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
    
    CONSTRAINT fk_metrics_backend_system FOREIGN KEY (backend_system) 
        REFERENCES backend_systems(system_id)
);

CREATE INDEX idx_metrics_backend_system ON backend_system_metrics(backend_system);
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
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_idempotency_expires_at ON idempotency_records(expires_at);
CREATE INDEX idx_idempotency_account_number ON idempotency_records(account_number);

-- Auto-cleanup expired idempotency records
CREATE OR REPLACE FUNCTION cleanup_expired_idempotency()
RETURNS void AS $$
BEGIN
    DELETE FROM idempotency_records WHERE expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

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
    
    CONSTRAINT fk_circuit_backend_system FOREIGN KEY (backend_system) 
        REFERENCES backend_systems(system_id)
);

CREATE INDEX idx_circuit_state ON circuit_breaker_state(state);

-- =====================================================
-- VIEWS
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
LEFT JOIN circuit_breaker_state cbs ON bs.system_id = cbs.backend_system;

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
JOIN backend_systems bs ON ar.backend_system = bs.system_id;
```

---

## 4. Transaction Processing Service Database

### Database Name: `transaction_db`

```sql
-- =====================================================
-- TRANSACTIONS (Main ledger)
-- =====================================================
CREATE TABLE transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    debit_account VARCHAR(50) NOT NULL,
    credit_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' 
        CHECK (status IN ('CREATED', 'VALIDATED', 'PROCESSING', 'CLEARING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'REVERSED')),
    transaction_type VARCHAR(50) NOT NULL DEFAULT 'PAYMENT',
    clearing_system VARCHAR(50),
    clearing_reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT
);

CREATE INDEX idx_transactions_payment_id ON transactions(payment_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_debit_account ON transactions(debit_account);
CREATE INDEX idx_transactions_credit_account ON transactions(credit_account);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_clearing_reference ON transactions(clearing_reference);

-- =====================================================
-- TRANSACTION EVENTS (Event Sourcing)
-- =====================================================
CREATE TABLE transaction_events (
    event_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    event_sequence BIGSERIAL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(50),
    causation_id VARCHAR(50),
    
    CONSTRAINT fk_transaction_events FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE INDEX idx_transaction_events_transaction_id ON transaction_events(transaction_id);
CREATE INDEX idx_transaction_events_sequence ON transaction_events(event_sequence);
CREATE INDEX idx_transaction_events_type ON transaction_events(event_type);
CREATE INDEX idx_transaction_events_occurred_at ON transaction_events(occurred_at DESC);

-- =====================================================
-- LEDGER ENTRIES (Double-Entry Bookkeeping)
-- =====================================================
CREATE TABLE ledger_entries (
    entry_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    balance_before DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ledger_transaction FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE INDEX idx_ledger_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_account_number ON ledger_entries(account_number);
CREATE INDEX idx_ledger_entry_date ON ledger_entries(entry_date DESC);
CREATE INDEX idx_ledger_composite ON ledger_entries(account_number, entry_date DESC);

-- =====================================================
-- CONSTRAINTS
-- =====================================================
-- Ensure double-entry: every transaction has exactly 2 ledger entries
CREATE OR REPLACE FUNCTION validate_double_entry()
RETURNS TRIGGER AS $$
DECLARE
    entry_count INTEGER;
    debit_sum DECIMAL(18,2);
    credit_sum DECIMAL(18,2);
BEGIN
    SELECT COUNT(*), 
           SUM(CASE WHEN entry_type = 'DEBIT' THEN amount ELSE 0 END),
           SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE 0 END)
    INTO entry_count, debit_sum, credit_sum
    FROM ledger_entries
    WHERE transaction_id = NEW.transaction_id;
    
    IF entry_count = 2 AND debit_sum != credit_sum THEN
        RAISE EXCEPTION 'Double-entry violation: debits (%) != credits (%)', debit_sum, credit_sum;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_double_entry_trigger
    AFTER INSERT ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION validate_double_entry();
```

---

## 5. Clearing Adapter Service Database

### Database Name: `clearing_db` (shared by all clearing adapters)

```sql
-- =====================================================
-- CLEARING SUBMISSIONS
-- =====================================================
CREATE TABLE clearing_submissions (
    submission_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    clearing_system VARCHAR(50) NOT NULL CHECK (clearing_system IN ('SAMOS', 'BANKSERV_ACH', 'BANKSERV_RTC', 'SASWITCH')),
    clearing_reference VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'SUBMITTED', 'ACKNOWLEDGED', 'PROCESSING', 'COMPLETED', 'FAILED', 'TIMEOUT')),
    request_message TEXT NOT NULL,
    request_format VARCHAR(20) CHECK (request_format IN ('ISO20022', 'ISO8583', 'PROPRIETARY')),
    response_message TEXT,
    response_code VARCHAR(10),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    last_retry_at TIMESTAMP
);

CREATE INDEX idx_clearing_transaction_id ON clearing_submissions(transaction_id);
CREATE INDEX idx_clearing_system ON clearing_submissions(clearing_system);
CREATE INDEX idx_clearing_status ON clearing_submissions(status);
CREATE INDEX idx_clearing_reference ON clearing_submissions(clearing_reference);
CREATE INDEX idx_clearing_submitted_at ON clearing_submissions(submitted_at DESC);

-- =====================================================
-- CLEARING BATCHES (for ACH batch processing)
-- =====================================================
CREATE TABLE clearing_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    clearing_system VARCHAR(50) NOT NULL,
    batch_date DATE NOT NULL,
    batch_cutoff_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' 
        CHECK (status IN ('OPEN', 'CLOSED', 'SUBMITTED', 'ACKNOWLEDGED', 'COMPLETED', 'FAILED')),
    transaction_count INTEGER NOT NULL DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    batch_file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    submitted_at TIMESTAMP,
    acknowledged_at TIMESTAMP
);

CREATE INDEX idx_batch_system ON clearing_batches(clearing_system);
CREATE INDEX idx_batch_date ON clearing_batches(batch_date DESC);
CREATE INDEX idx_batch_status ON clearing_batches(status);

-- =====================================================
-- BATCH SUBMISSIONS (link transactions to batches)
-- =====================================================
CREATE TABLE batch_submissions (
    batch_submission_id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    submission_id VARCHAR(50) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_batch_submissions_batch FOREIGN KEY (batch_id) 
        REFERENCES clearing_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT fk_batch_submissions_submission FOREIGN KEY (submission_id) 
        REFERENCES clearing_submissions(submission_id) ON DELETE CASCADE,
    CONSTRAINT uk_batch_submission UNIQUE (batch_id, submission_id)
);

CREATE INDEX idx_batch_submissions_batch_id ON batch_submissions(batch_id);
CREATE INDEX idx_batch_submissions_submission_id ON batch_submissions(submission_id);

-- =====================================================
-- CLEARING RESPONSES (for tracking responses from clearing systems)
-- =====================================================
CREATE TABLE clearing_responses (
    response_id BIGSERIAL PRIMARY KEY,
    submission_id VARCHAR(50) NOT NULL,
    response_type VARCHAR(50) NOT NULL CHECK (response_type IN ('ACKNOWLEDGMENT', 'COMPLETION', 'REJECTION', 'TIMEOUT')),
    response_code VARCHAR(10),
    response_message TEXT,
    response_timestamp TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_clearing_responses_submission FOREIGN KEY (submission_id) 
        REFERENCES clearing_submissions(submission_id) ON DELETE CASCADE
);

CREATE INDEX idx_responses_submission_id ON clearing_responses(submission_id);
CREATE INDEX idx_responses_type ON clearing_responses(response_type);
CREATE INDEX idx_responses_timestamp ON clearing_responses(response_timestamp DESC);
```

---

## 6. Settlement Service Database

### Database Name: `settlement_db`

```sql
-- =====================================================
-- SETTLEMENT BATCHES
-- =====================================================
CREATE TABLE settlement_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    batch_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'CALCULATING', 'CALCULATED', 'SUBMITTED', 'COMPLETED', 'FAILED')),
    transaction_count INTEGER NOT NULL DEFAULT 0,
    total_debit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_credit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    net_position DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    settlement_file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    calculated_at TIMESTAMP,
    submitted_at TIMESTAMP,
    finalized_at TIMESTAMP,
    settlement_reference VARCHAR(100)
);

CREATE INDEX idx_settlement_batch_date ON settlement_batches(batch_date DESC);
CREATE INDEX idx_settlement_clearing_system ON settlement_batches(clearing_system);
CREATE INDEX idx_settlement_status ON settlement_batches(status);

-- =====================================================
-- SETTLEMENT TRANSACTIONS
-- =====================================================
CREATE TABLE settlement_transactions (
    settlement_txn_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    settlement_status VARCHAR(20) NOT NULL DEFAULT 'INCLUDED' 
        CHECK (settlement_status IN ('INCLUDED', 'SETTLED', 'FAILED', 'EXCLUDED')),
    included_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP,
    exclusion_reason TEXT,
    
    CONSTRAINT fk_settlement_txn_batch FOREIGN KEY (batch_id) 
        REFERENCES settlement_batches(batch_id) ON DELETE CASCADE
);

CREATE INDEX idx_settlement_txn_batch_id ON settlement_transactions(batch_id);
CREATE INDEX idx_settlement_txn_transaction_id ON settlement_transactions(transaction_id);
CREATE INDEX idx_settlement_txn_status ON settlement_transactions(settlement_status);

-- =====================================================
-- SETTLEMENT POSITIONS (per account)
-- =====================================================
CREATE TABLE settlement_positions (
    position_id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    debit_count INTEGER NOT NULL DEFAULT 0,
    credit_count INTEGER NOT NULL DEFAULT 0,
    total_debit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_credit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    net_position DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_position_batch FOREIGN KEY (batch_id) 
        REFERENCES settlement_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT uk_position UNIQUE (batch_id, account_number)
);

CREATE INDEX idx_position_batch_id ON settlement_positions(batch_id);
CREATE INDEX idx_position_account_number ON settlement_positions(account_number);

-- =====================================================
-- SETTLEMENT FILES (generated files)
-- =====================================================
CREATE TABLE settlement_files (
    file_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    file_type VARCHAR(50) NOT NULL CHECK (file_type IN ('POSITION', 'TRANSACTION', 'SUMMARY')),
    file_format VARCHAR(20) NOT NULL CHECK (file_format IN ('CSV', 'XML', 'EXCEL', 'PDF')),
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by VARCHAR(100),
    
    CONSTRAINT fk_settlement_file_batch FOREIGN KEY (batch_id) 
        REFERENCES settlement_batches(batch_id) ON DELETE CASCADE
);

CREATE INDEX idx_file_batch_id ON settlement_files(batch_id);
CREATE INDEX idx_file_generated_at ON settlement_files(generated_at DESC);
```

---

## 7. Reconciliation Service Database

### Database Name: `reconciliation_db`

```sql
-- =====================================================
-- RECONCILIATION RUNS
-- =====================================================
CREATE TABLE reconciliation_runs (
    reconciliation_id VARCHAR(50) PRIMARY KEY,
    run_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED' 
        CHECK (status IN ('INITIATED', 'RUNNING', 'COMPLETED', 'FAILED', 'PARTIALLY_COMPLETED')),
    total_transactions INTEGER NOT NULL DEFAULT 0,
    matched_count INTEGER NOT NULL DEFAULT 0,
    unmatched_count INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    run_by VARCHAR(100)
);

CREATE INDEX idx_recon_run_date ON reconciliation_runs(run_date DESC);
CREATE INDEX idx_recon_clearing_system ON reconciliation_runs(clearing_system);
CREATE INDEX idx_recon_status ON reconciliation_runs(status);

-- =====================================================
-- RECONCILIATION MATCHES
-- =====================================================
CREATE TABLE reconciliation_matches (
    match_id VARCHAR(50) PRIMARY KEY,
    reconciliation_id VARCHAR(50) NOT NULL,
    internal_transaction_id VARCHAR(50) NOT NULL,
    external_reference VARCHAR(100) NOT NULL,
    match_status VARCHAR(20) NOT NULL DEFAULT 'MATCHED' 
        CHECK (match_status IN ('MATCHED', 'AMOUNT_MISMATCH', 'DATE_MISMATCH', 'STATUS_MISMATCH')),
    internal_amount DECIMAL(18,2),
    external_amount DECIMAL(18,2),
    amount_difference DECIMAL(18,2),
    matched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_match_recon FOREIGN KEY (reconciliation_id) 
        REFERENCES reconciliation_runs(reconciliation_id) ON DELETE CASCADE
);

CREATE INDEX idx_match_recon_id ON reconciliation_matches(reconciliation_id);
CREATE INDEX idx_match_internal_txn ON reconciliation_matches(internal_transaction_id);
CREATE INDEX idx_match_status ON reconciliation_matches(match_status);

-- =====================================================
-- RECONCILIATION EXCEPTIONS
-- =====================================================
CREATE TABLE reconciliation_exceptions (
    exception_id VARCHAR(50) PRIMARY KEY,
    reconciliation_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50),
    clearing_reference VARCHAR(100),
    exception_type VARCHAR(50) NOT NULL 
        CHECK (exception_type IN ('MISSING_INTERNAL', 'MISSING_EXTERNAL', 'AMOUNT_MISMATCH', 'DUPLICATE', 'STATUS_CONFLICT')),
    exception_reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'INVESTIGATING', 'RESOLVED', 'ESCALATED', 'CLOSED')),
    severity VARCHAR(20) DEFAULT 'MEDIUM' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    assigned_to VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    
    CONSTRAINT fk_exception_recon FOREIGN KEY (reconciliation_id) 
        REFERENCES reconciliation_runs(reconciliation_id) ON DELETE CASCADE
);

CREATE INDEX idx_exception_recon_id ON reconciliation_exceptions(reconciliation_id);
CREATE INDEX idx_exception_status ON reconciliation_exceptions(status);
CREATE INDEX idx_exception_severity ON reconciliation_exceptions(severity);
CREATE INDEX idx_exception_assigned_to ON reconciliation_exceptions(assigned_to);
CREATE INDEX idx_exception_created_at ON reconciliation_exceptions(created_at DESC);

-- =====================================================
-- EXTERNAL CLEARING DATA (imported from clearing systems)
-- =====================================================
CREATE TABLE external_clearing_data (
    external_id BIGSERIAL PRIMARY KEY,
    clearing_system VARCHAR(50) NOT NULL,
    clearing_reference VARCHAR(100) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(20),
    raw_data JSONB NOT NULL,
    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reconciled BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT uk_external_clearing UNIQUE (clearing_system, clearing_reference)
);

CREATE INDEX idx_external_clearing_system ON external_clearing_data(clearing_system);
CREATE INDEX idx_external_reference ON external_clearing_data(clearing_reference);
CREATE INDEX idx_external_reconciled ON external_clearing_data(reconciled);
CREATE INDEX idx_external_transaction_date ON external_clearing_data(transaction_date DESC);
```

---

## 8. Notification Service Database

### Database Name: `notification_db`

```sql
-- =====================================================
-- NOTIFICATIONS
-- =====================================================
CREATE TABLE notifications (
    notification_id VARCHAR(50) PRIMARY KEY,
    recipient_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'PUSH', 'WEBHOOK')),
    template_id VARCHAR(50) NOT NULL,
    subject VARCHAR(200),
    message_content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED' 
        CHECK (status IN ('QUEUED', 'SENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),
    priority VARCHAR(10) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    external_message_id VARCHAR(100)
);

CREATE INDEX idx_notification_recipient ON notifications(recipient_id);
CREATE INDEX idx_notification_channel ON notifications(channel);
CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notification_priority ON notifications(priority, status);

-- =====================================================
-- NOTIFICATION TEMPLATES
-- =====================================================
CREATE TABLE notification_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'PUSH', 'WEBHOOK')),
    subject_template VARCHAR(200),
    body_template TEXT NOT NULL,
    template_variables JSONB,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT uk_template_name UNIQUE (template_name, channel)
);

CREATE INDEX idx_template_channel ON notification_templates(channel);
CREATE INDEX idx_template_active ON notification_templates(active);

-- Example template:
-- subject_template: "Payment Confirmation"
-- body_template: "Your payment of R{{amount}} to {{recipient}} was successful. Reference: {{paymentId}}"
-- template_variables: ["amount", "recipient", "paymentId"]

-- =====================================================
-- NOTIFICATION DELIVERY LOG
-- =====================================================
CREATE TABLE notification_delivery_log (
    log_id BIGSERIAL PRIMARY KEY,
    notification_id VARCHAR(50) NOT NULL,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),
    response_code VARCHAR(10),
    response_message TEXT,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_delivery_log_notification FOREIGN KEY (notification_id) 
        REFERENCES notifications(notification_id) ON DELETE CASCADE
);

CREATE INDEX idx_delivery_log_notification_id ON notification_delivery_log(notification_id);
CREATE INDEX idx_delivery_log_attempted_at ON notification_delivery_log(attempted_at DESC);

-- =====================================================
-- RECIPIENT PREFERENCES
-- =====================================================
CREATE TABLE recipient_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    recipient_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notification_types JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_recipient_channel UNIQUE (recipient_id, channel)
);

CREATE INDEX idx_preferences_recipient ON recipient_preferences(recipient_id);
```

---

## 9. Saga Orchestrator Service Database

### Database Name: `saga_db`

```sql
-- =====================================================
-- SAGAS
-- =====================================================
CREATE TABLE sagas (
    saga_id VARCHAR(50) PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL CHECK (saga_type IN ('PAYMENT_SAGA', 'SETTLEMENT_SAGA', 'RECONCILIATION_SAGA')),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED' 
        CHECK (status IN ('INITIATED', 'RUNNING', 'COMPLETED', 'COMPENSATING', 'COMPENSATED', 'FAILED')),
    current_step VARCHAR(50),
    current_step_number INTEGER DEFAULT 0,
    total_steps INTEGER NOT NULL,
    payload JSONB NOT NULL,
    correlation_id VARCHAR(50),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    compensated_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    timeout_minutes INTEGER DEFAULT 30
);

CREATE INDEX idx_saga_status ON sagas(status);
CREATE INDEX idx_saga_type ON sagas(saga_type);
CREATE INDEX idx_saga_started_at ON sagas(started_at DESC);
CREATE INDEX idx_saga_correlation_id ON sagas(correlation_id);

-- =====================================================
-- SAGA STEPS
-- =====================================================
CREATE TABLE saga_steps (
    step_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_number INTEGER NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_service VARCHAR(100) NOT NULL,
    step_action VARCHAR(200) NOT NULL,
    compensation_action VARCHAR(200),
    step_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (step_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'COMPENSATED', 'SKIPPED')),
    request_data JSONB,
    response_data JSONB,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    compensated_at TIMESTAMP,
    failed_at TIMESTAMP,
    
    CONSTRAINT fk_saga_step FOREIGN KEY (saga_id) 
        REFERENCES sagas(saga_id) ON DELETE CASCADE,
    CONSTRAINT uk_saga_step_number UNIQUE (saga_id, step_number)
);

CREATE INDEX idx_saga_step_saga_id ON saga_steps(saga_id);
CREATE INDEX idx_saga_step_status ON saga_steps(step_status);
CREATE INDEX idx_saga_step_number ON saga_steps(saga_id, step_number);

-- =====================================================
-- SAGA EVENTS (event log)
-- =====================================================
CREATE TABLE saga_events (
    event_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_saga_event FOREIGN KEY (saga_id) 
        REFERENCES sagas(saga_id) ON DELETE CASCADE
);

CREATE INDEX idx_saga_event_saga_id ON saga_events(saga_id);
CREATE INDEX idx_saga_event_occurred_at ON saga_events(occurred_at DESC);

-- =====================================================
-- SAGA COMPENSATION LOG
-- =====================================================
CREATE TABLE saga_compensation_log (
    compensation_id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_id VARCHAR(50) NOT NULL,
    compensation_status VARCHAR(20) NOT NULL 
        CHECK (compensation_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    compensation_request JSONB,
    compensation_response JSONB,
    retry_count INTEGER DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    
    CONSTRAINT fk_compensation_saga FOREIGN KEY (saga_id) 
        REFERENCES sagas(saga_id) ON DELETE CASCADE,
    CONSTRAINT fk_compensation_step FOREIGN KEY (step_id) 
        REFERENCES saga_steps(step_id) ON DELETE CASCADE
);

CREATE INDEX idx_compensation_saga_id ON saga_compensation_log(saga_id);
CREATE INDEX idx_compensation_step_id ON saga_compensation_log(step_id);
CREATE INDEX idx_compensation_status ON saga_compensation_log(compensation_status);
```

---

## 10. IAM Service Database

### Database Name: `iam_db`

```sql
-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED')),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(100),
    azure_ad_object_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_azure_ad_object_id ON users(azure_ad_object_id);

-- =====================================================
-- ROLES
-- =====================================================
CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    role_description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_name ON roles(role_name);

-- Seed roles
INSERT INTO roles (role_id, role_name, role_description) VALUES
    ('ROLE-001', 'USER', 'Standard user role'),
    ('ROLE-002', 'ADMIN', 'Administrator role'),
    ('ROLE-003', 'OPERATOR', 'Operator role for exception handling'),
    ('ROLE-004', 'AUDITOR', 'Read-only auditor role');

-- =====================================================
-- USER ROLES
-- =====================================================
CREATE TABLE user_roles (
    user_role_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    role_id VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),
    
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) 
        REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- =====================================================
-- PERMISSIONS
-- =====================================================
CREATE TABLE permissions (
    permission_id VARCHAR(50) PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    permission_description TEXT,
    
    CONSTRAINT uk_permission UNIQUE (resource, action)
);

CREATE INDEX idx_permissions_resource ON permissions(resource);

-- =====================================================
-- ROLE PERMISSIONS
-- =====================================================
CREATE TABLE role_permissions (
    role_permission_id BIGSERIAL PRIMARY KEY,
    role_id VARCHAR(50) NOT NULL,
    permission_id VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) 
        REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) 
        REFERENCES permissions(permission_id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- =====================================================
-- REFRESH TOKENS
-- =====================================================
CREATE TABLE refresh_tokens (
    token_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revoked_reason VARCHAR(200),
    
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- =====================================================
-- AUDIT LOG (authentication events)
-- =====================================================
CREATE TABLE auth_audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50),
    event_type VARCHAR(50) NOT NULL 
        CHECK (event_type IN ('LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT', 'PASSWORD_CHANGE', 'MFA_ENABLED', 'MFA_DISABLED', 'ACCOUNT_LOCKED')),
    ip_address VARCHAR(45),
    user_agent TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB
);

CREATE INDEX idx_auth_audit_user_id ON auth_audit_log(user_id);
CREATE INDEX idx_auth_audit_event_type ON auth_audit_log(event_type);
CREATE INDEX idx_auth_audit_occurred_at ON auth_audit_log(occurred_at DESC);
```

---

## 11. Audit Service Database (CosmosDB)

### Database Name: `audit_db`

**CosmosDB Collection**: `audit_events`

```json
{
  "id": "unique-event-id",
  "partitionKey": "2025-10-11",  // Date-based partitioning
  "eventType": "API_CALL",
  "eventCategory": "SECURITY" | "BUSINESS" | "TECHNICAL",
  "timestamp": "2025-10-11T10:30:00Z",
  "userId": "user-123",
  "sessionId": "session-uuid",
  "service": "PaymentInitiationService",
  "action": "POST /api/v1/payments",
  "resource": "payments",
  "httpMethod": "POST",
  "httpStatusCode": 201,
  "requestData": {
    "sourceAccount": "1234567890",
    "destinationAccount": "0987654321",
    "amount": 1000.00
    // Sensitive data masked
  },
  "responseData": {
    "paymentId": "PAY-2025-XXXXXX",
    "status": "INITIATED"
  },
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "correlationId": "corr-uuid",
  "duration_ms": 150,
  "errors": [],
  "tags": ["payment", "initiation"],
  "ttl": 220752000  // 7 years in seconds
}
```

**CosmosDB Indexing Policy**:
```json
{
  "indexingMode": "consistent",
  "automatic": true,
  "includedPaths": [
    { "path": "/eventType/?" },
    { "path": "/eventCategory/?" },
    { "path": "/timestamp/?" },
    { "path": "/userId/?" },
    { "path": "/service/?" },
    { "path": "/correlationId/?" }
  ],
  "excludedPaths": [
    { "path": "/requestData/*" },
    { "path": "/responseData/*" }
  ]
}
```

---

## Database Sizing Estimates

| Database | Size (Year 1) | Growth Rate | Backup Strategy |
|----------|---------------|-------------|-----------------|
| payment_initiation_db | 500 GB | 40 GB/month | Daily full + hourly incremental |
| account_adapter_db | 50 GB | 3 GB/month | Daily full + hourly incremental |
| transaction_db | 1 TB | 80 GB/month | Daily full + hourly incremental |
| clearing_db | 800 GB | 60 GB/month | Daily full + hourly incremental |
| settlement_db | 300 GB | 25 GB/month | Daily full + hourly incremental |
| reconciliation_db | 200 GB | 15 GB/month | Daily full + hourly incremental |
| saga_db | 150 GB | 10 GB/month | Daily full + hourly incremental |
| audit_db (CosmosDB) | 2 TB | 150 GB/month | Continuous (built-in) |

**Note**: Account Adapter DB is much smaller as it only stores routing metadata and API call logs, not account balances or transactions.

---

## Database Maintenance

### Automated Tasks
1. **Daily Backups**: All PostgreSQL databases
2. **Index Maintenance**: Weekly REINDEX for heavy-write tables
3. **Statistics Update**: Daily ANALYZE for query optimizer
4. **Partition Management**: Monthly partitioning for time-series tables
5. **Archival**: Quarterly move old data to cold storage

### Monitoring Metrics
- Connection pool usage
- Query performance (slow query log)
- Database size and growth
- Lock contention
- Replication lag (for read replicas)

---

## Multi-Tenancy Implementation Notes

### Global Changes Applied to ALL Tables

**⚠️ CRITICAL**: Every table in every database now includes:

```sql
-- Added to ALL tables
ALTER TABLE <table_name> ADD COLUMN tenant_id VARCHAR(20) NOT NULL;
ALTER TABLE <table_name> ADD COLUMN business_unit_id VARCHAR(30);

-- Indexes for tenant filtering (performance critical)
CREATE INDEX idx_<table>_tenant ON <table_name>(tenant_id);
CREATE INDEX idx_<table>_tenant_bu ON <table_name>(tenant_id, business_unit_id);

-- Row-Level Security (data isolation)
ALTER TABLE <table_name> ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON <table_name>
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);
```

### Tables Updated

✅ **Payment Initiation Service**: payments, payment_status_history, debit_order_details  
✅ **Validation Service**: validation_rules, fraud_detection_log, customer_limits, payment_type_limits, limit_reservations  
✅ **Account Adapter Service**: account_routing, backend_systems, api_call_log  
✅ **Transaction Service**: transactions, transaction_events, ledger_entries  
✅ **Clearing Services**: clearing_messages, clearing_responses  
✅ **Settlement Service**: settlement_batches, settlement_items  
✅ **Reconciliation Service**: reconciliation_runs, reconciliation_items  
✅ **Notification Service**: notifications, notification_templates  
✅ **Reporting Service**: payment_summary, customer_analytics  
✅ **Saga Orchestrator**: saga_instances, saga_steps  
✅ **IAM Service**: users, roles, permissions  
✅ **Audit Service**: audit_logs (CosmosDB includes tenant_id in partition key)  

### Migration Script

```sql
-- Run this script to add tenant_id to all existing tables
-- WARNING: Requires downtime or careful zero-downtime migration

DO $$
DECLARE
    table_rec RECORD;
    schema_name TEXT := 'public';
BEGIN
    -- Loop through all tables (except tenant management tables)
    FOR table_rec IN 
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = schema_name
          AND tablename NOT LIKE 'tenant%'
          AND tablename NOT IN ('pg_%', 'sql_%')
    LOOP
        -- Add tenant_id column
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(20)', table_rec.tablename);
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS business_unit_id VARCHAR(30)', table_rec.tablename);
        
        -- Add indexes
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_tenant ON %I(tenant_id)', 
            table_rec.tablename, table_rec.tablename);
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_tenant_bu ON %I(tenant_id, business_unit_id)', 
            table_rec.tablename, table_rec.tablename);
        
        -- Enable RLS
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', table_rec.tablename);
        
        -- Create isolation policy
        EXECUTE format('
            DROP POLICY IF EXISTS tenant_isolation_policy ON %I;
            CREATE POLICY tenant_isolation_policy ON %I
                USING (tenant_id = current_setting(''app.current_tenant_id'', true)::VARCHAR)
        ', table_rec.tablename, table_rec.tablename);
        
        RAISE NOTICE 'Updated table: %', table_rec.tablename;
    END LOOP;
END $$;
```

### Query Examples

```sql
-- Application sets tenant context at transaction start
SET LOCAL app.current_tenant_id = 'STD-001';

-- Query automatically filtered by RLS
SELECT * FROM payments 
WHERE status = 'INITIATED';
-- Returns only payments for tenant STD-001 (enforced by RLS)

-- Explicit tenant filtering (belt-and-suspenders approach)
SELECT * FROM payments 
WHERE tenant_id = 'STD-001' 
  AND status = 'INITIATED';
```

### Performance Considerations

1. **Indexes**: All tenant queries use `idx_<table>_tenant` for fast filtering
2. **Partitioning**: Consider partitioning large tables by `tenant_id` for high-volume tenants
3. **Connection Pooling**: Maintain separate connection pools per tenant for better isolation
4. **Query Optimization**: Always include `tenant_id` in WHERE clauses (even though RLS enforces it)

### Security Considerations

1. **RLS Bypass**: Only `postgres` superuser and roles with `BYPASSRLS` can bypass policies
2. **Application User**: Application connects as limited user without `BYPASSRLS`
3. **Admin Queries**: Platform admin queries use `SET app.is_platform_admin = TRUE` to view all tenants
4. **Audit Trail**: All tenant data access logged in `tenant_audit_log`

---

**Last Updated**: 2025-10-11  
**Version**: 4.0 (Multi-Tenancy Added)

---

**Next**: See `06-SOUTH-AFRICA-CLEARING.md` for clearing system integration  
**Next**: See `07-AZURE-INFRASTRUCTURE.md` for infrastructure design  
**Next**: See `12-TENANT-MANAGEMENT.md` for complete tenant hierarchy design
