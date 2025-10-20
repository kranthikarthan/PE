-- V2__Create_transaction_search_tables.sql
-- Transaction Search Tables for Reporting and Analytics

-- Transaction entity table
CREATE TABLE transaction_entity (
    transaction_id VARCHAR(255) PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    clearing_system VARCHAR(100),
    channel VARCHAR(100),
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255),
    error_message VARCHAR(1000),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    processing_time_seconds BIGINT,
    retry_count INTEGER DEFAULT 0,
    external_reference VARCHAR(255),
    internal_reference VARCHAR(255),
    description VARCHAR(500),
    initiated_by VARCHAR(255),
    approved_by VARCHAR(255),
    rejected_by VARCHAR(255),
    rejection_reason VARCHAR(500),
    priority VARCHAR(20),
    category VARCHAR(100),
    subcategory VARCHAR(100),
    tags VARCHAR(1000),
    notes VARCHAR(1000),
    is_urgent BOOLEAN DEFAULT FALSE,
    is_high_value BOOLEAN DEFAULT FALSE,
    is_suspicious BOOLEAN DEFAULT FALSE,
    risk_score INTEGER,
    compliance_status VARCHAR(50),
    regulatory_reporting_required BOOLEAN DEFAULT FALSE,
    regulatory_reporting_status VARCHAR(50),
    regulatory_reporting_date TIMESTAMP WITH TIME ZONE,
    audit_trail JSONB,
    version BIGINT DEFAULT 0
);

-- Transaction search audit log
CREATE TABLE transaction_search_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(255) NOT NULL,
    search_type VARCHAR(100) NOT NULL,
    search_criteria JSONB,
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transaction search statistics
CREATE TABLE transaction_search_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    total_transactions INTEGER NOT NULL DEFAULT 0,
    successful_transactions INTEGER NOT NULL DEFAULT 0,
    failed_transactions INTEGER NOT NULL DEFAULT 0,
    pending_transactions INTEGER NOT NULL DEFAULT 0,
    total_volume DECIMAL(19,2) DEFAULT 0,
    average_amount DECIMAL(19,2) DEFAULT 0,
    success_rate DECIMAL(5,2) DEFAULT 0,
    average_processing_time_seconds DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transaction search configurations
CREATE TABLE transaction_search_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Transaction search bookmarks
CREATE TABLE transaction_search_bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    search_criteria JSONB NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    is_shared BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_transaction_entity_tenant_business ON transaction_entity(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_entity_payment_id ON transaction_entity(payment_id);
CREATE INDEX idx_transaction_entity_status ON transaction_entity(status);
CREATE INDEX idx_transaction_entity_created_at ON transaction_entity(created_at);
CREATE INDEX idx_transaction_entity_clearing_system ON transaction_entity(clearing_system);
CREATE INDEX idx_transaction_entity_channel ON transaction_entity(channel);
CREATE INDEX idx_transaction_entity_currency ON transaction_entity(currency);
CREATE INDEX idx_transaction_entity_amount ON transaction_entity(amount);
CREATE INDEX idx_transaction_entity_completed_at ON transaction_entity(completed_at);
CREATE INDEX idx_transaction_entity_correlation_id ON transaction_entity(correlation_id);
CREATE INDEX idx_transaction_entity_external_reference ON transaction_entity(external_reference);
CREATE INDEX idx_transaction_entity_priority ON transaction_entity(priority);
CREATE INDEX idx_transaction_entity_category ON transaction_entity(category);
CREATE INDEX idx_transaction_entity_is_high_value ON transaction_entity(is_high_value);
CREATE INDEX idx_transaction_entity_is_urgent ON transaction_entity(is_urgent);
CREATE INDEX idx_transaction_entity_is_suspicious ON transaction_entity(is_suspicious);
CREATE INDEX idx_transaction_entity_risk_score ON transaction_entity(risk_score);
CREATE INDEX idx_transaction_entity_compliance_status ON transaction_entity(compliance_status);

CREATE INDEX idx_transaction_search_audit_log_transaction_id ON transaction_search_audit_log(transaction_id);
CREATE INDEX idx_transaction_search_audit_log_tenant_business ON transaction_search_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_search_audit_log_performed_at ON transaction_search_audit_log(performed_at);
CREATE INDEX idx_transaction_search_audit_log_search_type ON transaction_search_audit_log(search_type);

CREATE INDEX idx_transaction_search_statistics_tenant_business ON transaction_search_statistics(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_search_statistics_date ON transaction_search_statistics(date);

CREATE INDEX idx_transaction_search_configs_tenant_business ON transaction_search_configs(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_search_configs_key ON transaction_search_configs(config_key);
CREATE INDEX idx_transaction_search_configs_active ON transaction_search_configs(is_active);

CREATE INDEX idx_transaction_search_bookmarks_tenant_business ON transaction_search_bookmarks(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_search_bookmarks_created_by ON transaction_search_bookmarks(created_by);
CREATE INDEX idx_transaction_search_bookmarks_shared ON transaction_search_bookmarks(is_shared);

-- Add foreign key constraints
ALTER TABLE transaction_search_audit_log 
ADD CONSTRAINT fk_transaction_search_audit_log_transaction 
FOREIGN KEY (transaction_id) REFERENCES transaction_entity(transaction_id) ON DELETE CASCADE;

-- Add RLS (Row Level Security) policies for multi-tenancy
ALTER TABLE transaction_entity ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_search_audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_search_statistics ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_search_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_search_bookmarks ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY transaction_entity_tenant_policy ON transaction_entity
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY transaction_search_audit_log_tenant_policy ON transaction_search_audit_log
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY transaction_search_statistics_tenant_policy ON transaction_search_statistics
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY transaction_search_configs_tenant_policy ON transaction_search_configs
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY transaction_search_bookmarks_tenant_policy ON transaction_search_bookmarks
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

-- Insert default configurations
INSERT INTO transaction_search_configs (tenant_id, business_unit_id, config_key, config_value, description, created_by, updated_by)
VALUES 
    ('default', 'default', 'search.max_results', '1000', 'Maximum number of results per search', 'system', 'system'),
    ('default', 'default', 'search.default_page_size', '20', 'Default page size for search results', 'system', 'system'),
    ('default', 'default', 'search.max_export_records', '10000', 'Maximum number of records for export', 'system', 'system'),
    ('default', 'default', 'search.cache_ttl_seconds', '300', 'Search result cache TTL in seconds', 'system', 'system'),
    ('default', 'default', 'search.enable_audit', 'true', 'Enable search audit logging', 'system', 'system'),
    ('default', 'default', 'search.enable_bookmarks', 'true', 'Enable search bookmarks', 'system', 'system'),
    ('default', 'default', 'search.enable_export', 'true', 'Enable CSV export functionality', 'system', 'system'),
    ('default', 'default', 'search.enable_trends', 'true', 'Enable transaction trends analysis', 'system', 'system');

-- Create a function to update transaction statistics
CREATE OR REPLACE FUNCTION update_transaction_statistics(p_tenant_id VARCHAR, p_business_unit_id VARCHAR)
RETURNS VOID AS $$
DECLARE
    v_total_transactions INTEGER;
    v_successful_transactions INTEGER;
    v_failed_transactions INTEGER;
    v_pending_transactions INTEGER;
    v_total_volume DECIMAL(19,2);
    v_average_amount DECIMAL(19,2);
    v_success_rate DECIMAL(5,2);
    v_average_processing_time DECIMAL(10,2);
BEGIN
    -- Calculate statistics
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE status = 'COMPLETED'),
        COUNT(*) FILTER (WHERE status = 'FAILED'),
        COUNT(*) FILTER (WHERE status = 'PENDING'),
        COALESCE(SUM(amount), 0),
        COALESCE(AVG(amount), 0),
        (COUNT(*) FILTER (WHERE status = 'COMPLETED')::DECIMAL / NULLIF(COUNT(*), 0) * 100),
        COALESCE(AVG(processing_time_seconds), 0)
    INTO v_total_transactions, v_successful_transactions, v_failed_transactions, v_pending_transactions,
         v_total_volume, v_average_amount, v_success_rate, v_average_processing_time
    FROM transaction_entity 
    WHERE tenant_id = p_tenant_id AND business_unit_id = p_business_unit_id;
    
    -- Insert or update statistics
    INSERT INTO transaction_search_statistics (
        tenant_id, business_unit_id, date, total_transactions, successful_transactions, 
        failed_transactions, pending_transactions, total_volume, average_amount, 
        success_rate, average_processing_time_seconds
    ) VALUES (
        p_tenant_id, p_business_unit_id, CURRENT_DATE, v_total_transactions, v_successful_transactions,
        v_failed_transactions, v_pending_transactions, v_total_volume, v_average_amount,
        v_success_rate, v_average_processing_time
    )
    ON CONFLICT (tenant_id, business_unit_id, date) 
    DO UPDATE SET
        total_transactions = EXCLUDED.total_transactions,
        successful_transactions = EXCLUDED.successful_transactions,
        failed_transactions = EXCLUDED.failed_transactions,
        pending_transactions = EXCLUDED.pending_transactions,
        total_volume = EXCLUDED.total_volume,
        average_amount = EXCLUDED.average_amount,
        success_rate = EXCLUDED.success_rate,
        average_processing_time_seconds = EXCLUDED.average_processing_time_seconds,
        updated_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Create a function to check for transaction alerts
CREATE OR REPLACE FUNCTION check_transaction_alerts(p_tenant_id VARCHAR, p_business_unit_id VARCHAR)
RETURNS VOID AS $$
DECLARE
    v_failed_count INTEGER;
    v_high_value_count INTEGER;
    v_suspicious_count INTEGER;
BEGIN
    -- Check for failed transactions
    SELECT COUNT(*) INTO v_failed_count
    FROM transaction_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND status = 'FAILED'
    AND created_at > CURRENT_TIMESTAMP - INTERVAL '1 hour';
    
    IF v_failed_count > 10 THEN
        INSERT INTO transaction_search_audit_log (
            transaction_id, search_type, search_criteria, performed_by, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'FAILED_TRANSACTIONS_THRESHOLD', 
            json_build_object('failed_count', v_failed_count, 'threshold', 10),
            'SYSTEM', p_tenant_id, p_business_unit_id
        );
    END IF;
    
    -- Check for high-value transactions
    SELECT COUNT(*) INTO v_high_value_count
    FROM transaction_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND is_high_value = true
    AND created_at > CURRENT_TIMESTAMP - INTERVAL '1 hour';
    
    IF v_high_value_count > 5 THEN
        INSERT INTO transaction_search_audit_log (
            transaction_id, search_type, search_criteria, performed_by, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'HIGH_VALUE_TRANSACTIONS_THRESHOLD', 
            json_build_object('high_value_count', v_high_value_count, 'threshold', 5),
            'SYSTEM', p_tenant_id, p_business_unit_id
        );
    END IF;
    
    -- Check for suspicious transactions
    SELECT COUNT(*) INTO v_suspicious_count
    FROM transaction_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND is_suspicious = true
    AND created_at > CURRENT_TIMESTAMP - INTERVAL '1 hour';
    
    IF v_suspicious_count > 0 THEN
        INSERT INTO transaction_search_audit_log (
            transaction_id, search_type, search_criteria, performed_by, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'SUSPICIOUS_TRANSACTIONS_DETECTED', 
            json_build_object('suspicious_count', v_suspicious_count),
            'SYSTEM', p_tenant_id, p_business_unit_id
        );
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create a function to get transaction trends
CREATE OR REPLACE FUNCTION get_transaction_trends(
    p_tenant_id VARCHAR,
    p_business_unit_id VARCHAR,
    p_granularity VARCHAR,
    p_start_date TIMESTAMP WITH TIME ZONE
)
RETURNS TABLE (
    timestamp TIMESTAMP WITH TIME ZONE,
    count BIGINT,
    volume DECIMAL(19,2),
    success_rate DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        DATE_TRUNC(p_granularity, t.created_at) as timestamp,
        COUNT(*) as count,
        COALESCE(SUM(t.amount), 0) as volume,
        CASE WHEN COUNT(*) > 0 THEN 
            (COUNT(*) FILTER (WHERE t.status = 'COMPLETED') * 100.0 / COUNT(*))
        ELSE 0 END as success_rate
    FROM transaction_entity t
    WHERE t.tenant_id = p_tenant_id 
    AND t.business_unit_id = p_business_unit_id
    AND t.created_at >= p_start_date
    GROUP BY DATE_TRUNC(p_granularity, t.created_at)
    ORDER BY timestamp;
END;
$$ LANGUAGE plpgsql;
