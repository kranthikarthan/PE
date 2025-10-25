-- V2__Create_reconciliation_management_tables.sql
-- Reconciliation Management Tables for Operations and Monitoring

-- Reconciliation run entity table
CREATE TABLE reconciliation_run_entity (
    run_id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    clearing_system VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255),
    started_by VARCHAR(255),
    stopped_by VARCHAR(255),
    stop_reason VARCHAR(500),
    total_records INTEGER DEFAULT 0,
    matched_records INTEGER DEFAULT 0,
    unmatched_records INTEGER DEFAULT 0,
    processing_time_seconds BIGINT,
    error_message VARCHAR(1000),
    metadata JSONB,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT DEFAULT 0
);

-- Reconciliation exception entity table
CREATE TABLE reconciliation_exception_entity (
    exception_id VARCHAR(255) PRIMARY KEY,
    run_id VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    details TEXT,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255),
    transaction_id VARCHAR(255),
    payment_id VARCHAR(255),
    clearing_system VARCHAR(100),
    resolution VARCHAR(1000),
    resolution_notes VARCHAR(2000),
    resolved_by VARCHAR(255),
    resolved_at TIMESTAMP WITH TIME ZONE,
    priority VARCHAR(20),
    category VARCHAR(100),
    subcategory VARCHAR(100),
    tags VARCHAR(1000),
    notes VARCHAR(2000),
    is_urgent BOOLEAN DEFAULT FALSE,
    is_high_priority BOOLEAN DEFAULT FALSE,
    is_escalated BOOLEAN DEFAULT FALSE,
    escalation_level INTEGER,
    escalated_by VARCHAR(255),
    escalated_at TIMESTAMP WITH TIME ZONE,
    escalation_reason VARCHAR(500),
    assigned_to VARCHAR(255),
    assigned_at TIMESTAMP WITH TIME ZONE,
    due_date TIMESTAMP WITH TIME ZONE,
    sla_breach BOOLEAN DEFAULT FALSE,
    sla_breach_reason VARCHAR(500),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    last_retry_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Reconciliation management audit log
CREATE TABLE reconciliation_management_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id VARCHAR(255),
    exception_id VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Reconciliation management statistics
CREATE TABLE reconciliation_management_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    total_runs INTEGER NOT NULL DEFAULT 0,
    successful_runs INTEGER NOT NULL DEFAULT 0,
    failed_runs INTEGER NOT NULL DEFAULT 0,
    running_runs INTEGER NOT NULL DEFAULT 0,
    total_exceptions INTEGER NOT NULL DEFAULT 0,
    resolved_exceptions INTEGER NOT NULL DEFAULT 0,
    unresolved_exceptions INTEGER NOT NULL DEFAULT 0,
    average_processing_time_seconds DECIMAL(10,2),
    success_rate DECIMAL(5,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Reconciliation management configurations
CREATE TABLE reconciliation_management_configs (
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

-- Reconciliation management reports
CREATE TABLE reconciliation_management_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id VARCHAR(255) NOT NULL,
    report_type VARCHAR(100) NOT NULL,
    format VARCHAR(50) NOT NULL,
    generated_by VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    parameters JSONB,
    file_path VARCHAR(1000),
    file_size BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'GENERATING',
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_reconciliation_run_entity_tenant_business ON reconciliation_run_entity(tenant_id, business_unit_id);
CREATE INDEX idx_reconciliation_run_entity_status ON reconciliation_run_entity(status);
CREATE INDEX idx_reconciliation_run_entity_clearing_system ON reconciliation_run_entity(clearing_system);
CREATE INDEX idx_reconciliation_run_entity_started_at ON reconciliation_run_entity(started_at);
CREATE INDEX idx_reconciliation_run_entity_completed_at ON reconciliation_run_entity(completed_at);
CREATE INDEX idx_reconciliation_run_entity_started_by ON reconciliation_run_entity(started_by);
CREATE INDEX idx_reconciliation_run_entity_stopped_by ON reconciliation_run_entity(stopped_by);
CREATE INDEX idx_reconciliation_run_entity_correlation_id ON reconciliation_run_entity(correlation_id);

CREATE INDEX idx_reconciliation_exception_entity_tenant_business ON reconciliation_exception_entity(tenant_id, business_unit_id);
CREATE INDEX idx_reconciliation_exception_entity_run_id ON reconciliation_exception_entity(run_id);
CREATE INDEX idx_reconciliation_exception_entity_status ON reconciliation_exception_entity(status);
CREATE INDEX idx_reconciliation_exception_entity_severity ON reconciliation_exception_entity(severity);
CREATE INDEX idx_reconciliation_exception_entity_type ON reconciliation_exception_entity(type);
CREATE INDEX idx_reconciliation_exception_entity_created_at ON reconciliation_exception_entity(created_at);
CREATE INDEX idx_reconciliation_exception_entity_resolved_at ON reconciliation_exception_entity(resolved_at);
CREATE INDEX idx_reconciliation_exception_entity_assigned_to ON reconciliation_exception_entity(assigned_to);
CREATE INDEX idx_reconciliation_exception_entity_due_date ON reconciliation_exception_entity(due_date);
CREATE INDEX idx_reconciliation_exception_entity_transaction_id ON reconciliation_exception_entity(transaction_id);
CREATE INDEX idx_reconciliation_exception_entity_payment_id ON reconciliation_exception_entity(payment_id);
CREATE INDEX idx_reconciliation_exception_entity_clearing_system ON reconciliation_exception_entity(clearing_system);

CREATE INDEX idx_reconciliation_management_audit_log_run_id ON reconciliation_management_audit_log(run_id);
CREATE INDEX idx_reconciliation_management_audit_log_exception_id ON reconciliation_management_audit_log(exception_id);
CREATE INDEX idx_reconciliation_management_audit_log_tenant_business ON reconciliation_management_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_reconciliation_management_audit_log_performed_at ON reconciliation_management_audit_log(performed_at);
CREATE INDEX idx_reconciliation_management_audit_log_action ON reconciliation_management_audit_log(action);

CREATE INDEX idx_reconciliation_management_statistics_tenant_business ON reconciliation_management_statistics(tenant_id, business_unit_id);
CREATE INDEX idx_reconciliation_management_statistics_date ON reconciliation_management_statistics(date);

CREATE INDEX idx_reconciliation_management_configs_tenant_business ON reconciliation_management_configs(tenant_id, business_unit_id);
CREATE INDEX idx_reconciliation_management_configs_key ON reconciliation_management_configs(config_key);
CREATE INDEX idx_reconciliation_management_configs_active ON reconciliation_management_configs(is_active);

CREATE INDEX idx_reconciliation_management_reports_tenant_business ON reconciliation_management_reports(tenant_id, business_unit_id);
CREATE INDEX idx_reconciliation_management_reports_report_id ON reconciliation_management_reports(report_id);
CREATE INDEX idx_reconciliation_management_reports_generated_by ON reconciliation_management_reports(generated_by);
CREATE INDEX idx_reconciliation_management_reports_generated_at ON reconciliation_management_reports(generated_at);
CREATE INDEX idx_reconciliation_management_reports_status ON reconciliation_management_reports(status);

-- Add foreign key constraints
ALTER TABLE reconciliation_exception_entity 
ADD CONSTRAINT fk_reconciliation_exception_entity_run 
FOREIGN KEY (run_id) REFERENCES reconciliation_run_entity(run_id) ON DELETE CASCADE;

ALTER TABLE reconciliation_management_audit_log 
ADD CONSTRAINT fk_reconciliation_management_audit_log_run 
FOREIGN KEY (run_id) REFERENCES reconciliation_run_entity(run_id) ON DELETE CASCADE;

ALTER TABLE reconciliation_management_audit_log 
ADD CONSTRAINT fk_reconciliation_management_audit_log_exception 
FOREIGN KEY (exception_id) REFERENCES reconciliation_exception_entity(exception_id) ON DELETE CASCADE;

-- Add RLS (Row Level Security) policies for multi-tenancy
ALTER TABLE reconciliation_run_entity ENABLE ROW LEVEL SECURITY;
ALTER TABLE reconciliation_exception_entity ENABLE ROW LEVEL SECURITY;
ALTER TABLE reconciliation_management_audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE reconciliation_management_statistics ENABLE ROW LEVEL SECURITY;
ALTER TABLE reconciliation_management_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE reconciliation_management_reports ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY reconciliation_run_entity_tenant_policy ON reconciliation_run_entity
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY reconciliation_exception_entity_tenant_policy ON reconciliation_exception_entity
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY reconciliation_management_audit_log_tenant_policy ON reconciliation_management_audit_log
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY reconciliation_management_statistics_tenant_policy ON reconciliation_management_statistics
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY reconciliation_management_configs_tenant_policy ON reconciliation_management_configs
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY reconciliation_management_reports_tenant_policy ON reconciliation_management_reports
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

-- Insert default configurations
INSERT INTO reconciliation_management_configs (tenant_id, business_unit_id, config_key, config_value, description, created_by, updated_by)
VALUES 
    ('default', 'default', 'reconciliation.max_running_runs', '5', 'Maximum number of concurrent reconciliation runs', 'system', 'system'),
    ('default', 'default', 'reconciliation.timeout_minutes', '60', 'Reconciliation run timeout in minutes', 'system', 'system'),
    ('default', 'default', 'reconciliation.retry_attempts', '3', 'Maximum retry attempts for failed reconciliations', 'system', 'system'),
    ('default', 'default', 'reconciliation.exception_threshold', '100', 'Alert threshold for reconciliation exceptions', 'system', 'system'),
    ('default', 'default', 'reconciliation.processing_time_threshold', '30', 'Alert threshold for processing time in minutes', 'system', 'system'),
    ('default', 'default', 'reconciliation.enable_audit', 'true', 'Enable reconciliation audit logging', 'system', 'system'),
    ('default', 'default', 'reconciliation.enable_reports', 'true', 'Enable reconciliation report generation', 'system', 'system'),
    ('default', 'default', 'reconciliation.enable_escalation', 'true', 'Enable exception escalation', 'system', 'system');

-- Create a function to update reconciliation statistics
CREATE OR REPLACE FUNCTION update_reconciliation_statistics(p_tenant_id VARCHAR, p_business_unit_id VARCHAR)
RETURNS VOID AS $$
DECLARE
    v_total_runs INTEGER;
    v_successful_runs INTEGER;
    v_failed_runs INTEGER;
    v_running_runs INTEGER;
    v_total_exceptions INTEGER;
    v_resolved_exceptions INTEGER;
    v_unresolved_exceptions INTEGER;
    v_average_processing_time DECIMAL(10,2);
    v_success_rate DECIMAL(5,2);
BEGIN
    -- Calculate reconciliation run statistics
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE status = 'COMPLETED'),
        COUNT(*) FILTER (WHERE status = 'FAILED'),
        COUNT(*) FILTER (WHERE status = 'RUNNING'),
        COALESCE(AVG(processing_time_seconds), 0),
        (COUNT(*) FILTER (WHERE status = 'COMPLETED')::DECIMAL / NULLIF(COUNT(*), 0) * 100)
    INTO v_total_runs, v_successful_runs, v_failed_runs, v_running_runs,
         v_average_processing_time, v_success_rate
    FROM reconciliation_run_entity 
    WHERE tenant_id = p_tenant_id AND business_unit_id = p_business_unit_id;
    
    -- Calculate exception statistics
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE status = 'RESOLVED'),
        COUNT(*) FILTER (WHERE status = 'OPEN')
    INTO v_total_exceptions, v_resolved_exceptions, v_unresolved_exceptions
    FROM reconciliation_exception_entity 
    WHERE tenant_id = p_tenant_id AND business_unit_id = p_business_unit_id;
    
    -- Insert or update statistics
    INSERT INTO reconciliation_management_statistics (
        tenant_id, business_unit_id, date, total_runs, successful_runs, 
        failed_runs, running_runs, total_exceptions, resolved_exceptions, 
        unresolved_exceptions, average_processing_time_seconds, success_rate
    ) VALUES (
        p_tenant_id, p_business_unit_id, CURRENT_DATE, v_total_runs, v_successful_runs,
        v_failed_runs, v_running_runs, v_total_exceptions, v_resolved_exceptions,
        v_unresolved_exceptions, v_average_processing_time, v_success_rate
    )
    ON CONFLICT (tenant_id, business_unit_id, date) 
    DO UPDATE SET
        total_runs = EXCLUDED.total_runs,
        successful_runs = EXCLUDED.successful_runs,
        failed_runs = EXCLUDED.failed_runs,
        running_runs = EXCLUDED.running_runs,
        total_exceptions = EXCLUDED.total_exceptions,
        resolved_exceptions = EXCLUDED.resolved_exceptions,
        unresolved_exceptions = EXCLUDED.unresolved_exceptions,
        average_processing_time_seconds = EXCLUDED.average_processing_time_seconds,
        success_rate = EXCLUDED.success_rate,
        updated_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Create a function to check for reconciliation alerts
CREATE OR REPLACE FUNCTION check_reconciliation_alerts(p_tenant_id VARCHAR, p_business_unit_id VARCHAR)
RETURNS VOID AS $$
DECLARE
    v_failed_runs INTEGER;
    v_long_running_runs INTEGER;
    v_high_exception_runs INTEGER;
    v_unresolved_exceptions INTEGER;
BEGIN
    -- Check for failed reconciliation runs
    SELECT COUNT(*) INTO v_failed_runs
    FROM reconciliation_run_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND status = 'FAILED'
    AND started_at > CURRENT_TIMESTAMP - INTERVAL '1 hour';
    
    IF v_failed_runs > 3 THEN
        INSERT INTO reconciliation_management_audit_log (
            run_id, action, performed_by, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'FAILED_RECONCILIATION_RUNS_THRESHOLD', 'SYSTEM', p_tenant_id, p_business_unit_id
        );
    END IF;
    
    -- Check for long-running reconciliation runs
    SELECT COUNT(*) INTO v_long_running_runs
    FROM reconciliation_run_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND status = 'RUNNING'
    AND started_at < CURRENT_TIMESTAMP - INTERVAL '30 minutes';
    
    IF v_long_running_runs > 0 THEN
        INSERT INTO reconciliation_management_audit_log (
            run_id, action, performed_by, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'LONG_RUNNING_RECONCILIATION_RUNS', 'SYSTEM', p_tenant_id, p_business_unit_id
        );
    END IF;
    
    -- Check for high exception count runs
    SELECT COUNT(*) INTO v_high_exception_runs
    FROM reconciliation_run_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND unmatched_records > 100
    AND started_at > CURRENT_TIMESTAMP - INTERVAL '1 hour';
    
    IF v_high_exception_runs > 0 THEN
        INSERT INTO reconciliation_management_audit_log (
            run_id, action, performed_by, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'HIGH_EXCEPTION_COUNT_RUNS', 'SYSTEM', p_tenant_id, p_business_unit_id
        );
    END IF;
    
    -- Check for unresolved exceptions
    SELECT COUNT(*) INTO v_unresolved_exceptions
    FROM reconciliation_exception_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND status = 'OPEN'
    AND created_at > CURRENT_TIMESTAMP - INTERVAL '1 hour';
    
    IF v_unresolved_exceptions > 50 THEN
        INSERT INTO reconciliation_management_audit_log (
            exception_id, action, performed_by, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'HIGH_UNRESOLVED_EXCEPTIONS_THRESHOLD', 'SYSTEM', p_tenant_id, p_business_unit_id
        );
    END IF;
END;
$$ LANGUAGE plpgsql;
