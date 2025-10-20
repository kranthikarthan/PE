-- V2__Create_saga_management_tables.sql
-- Saga Management Tables for Operations and Monitoring

-- Saga management audit log
CREATE TABLE saga_management_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    details JSONB,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Saga management statistics
CREATE TABLE saga_management_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    total_sagas INTEGER NOT NULL DEFAULT 0,
    running_sagas INTEGER NOT NULL DEFAULT 0,
    completed_sagas INTEGER NOT NULL DEFAULT 0,
    failed_sagas INTEGER NOT NULL DEFAULT 0,
    average_execution_time_seconds DECIMAL(10,2),
    success_rate DECIMAL(5,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Saga management alerts
CREATE TABLE saga_management_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_id VARCHAR(255) NOT NULL,
    alert_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by VARCHAR(255)
);

-- Saga management configurations
CREATE TABLE saga_management_configs (
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

-- Indexes for performance
CREATE INDEX idx_saga_management_audit_log_saga_id ON saga_management_audit_log(saga_id);
CREATE INDEX idx_saga_management_audit_log_tenant_business ON saga_management_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_saga_management_audit_log_performed_at ON saga_management_audit_log(performed_at);
CREATE INDEX idx_saga_management_audit_log_action ON saga_management_audit_log(action);

CREATE INDEX idx_saga_management_statistics_tenant_business ON saga_management_statistics(tenant_id, business_unit_id);
CREATE INDEX idx_saga_management_statistics_date ON saga_management_statistics(date);

CREATE INDEX idx_saga_management_alerts_saga_id ON saga_management_alerts(saga_id);
CREATE INDEX idx_saga_management_alerts_tenant_business ON saga_management_alerts(tenant_id, business_unit_id);
CREATE INDEX idx_saga_management_alerts_status ON saga_management_alerts(status);
CREATE INDEX idx_saga_management_alerts_created_at ON saga_management_alerts(created_at);

CREATE INDEX idx_saga_management_configs_tenant_business ON saga_management_configs(tenant_id, business_unit_id);
CREATE INDEX idx_saga_management_configs_key ON saga_management_configs(config_key);
CREATE INDEX idx_saga_management_configs_active ON saga_management_configs(is_active);

-- Add foreign key constraints
ALTER TABLE saga_management_audit_log 
ADD CONSTRAINT fk_saga_management_audit_log_saga 
FOREIGN KEY (saga_id) REFERENCES saga_entity(saga_id) ON DELETE CASCADE;

ALTER TABLE saga_management_alerts 
ADD CONSTRAINT fk_saga_management_alerts_saga 
FOREIGN KEY (saga_id) REFERENCES saga_entity(saga_id) ON DELETE CASCADE;

-- Add RLS (Row Level Security) policies for multi-tenancy
ALTER TABLE saga_management_audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_management_statistics ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_management_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_management_configs ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY saga_management_audit_log_tenant_policy ON saga_management_audit_log
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY saga_management_statistics_tenant_policy ON saga_management_statistics
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY saga_management_alerts_tenant_policy ON saga_management_alerts
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

CREATE POLICY saga_management_configs_tenant_policy ON saga_management_configs
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id'));

-- Insert default configurations
INSERT INTO saga_management_configs (tenant_id, business_unit_id, config_key, config_value, description, created_by, updated_by)
VALUES 
    ('default', 'default', 'saga.timeout.seconds', '300', 'Default saga timeout in seconds', 'system', 'system'),
    ('default', 'default', 'saga.retry.max_attempts', '3', 'Maximum retry attempts for failed sagas', 'system', 'system'),
    ('default', 'default', 'saga.alert.failed_threshold', '5', 'Alert threshold for failed sagas', 'system', 'system'),
    ('default', 'default', 'saga.alert.execution_time_threshold', '600', 'Alert threshold for execution time in seconds', 'system', 'system');

-- Create a function to update statistics
CREATE OR REPLACE FUNCTION update_saga_statistics(p_tenant_id VARCHAR, p_business_unit_id VARCHAR)
RETURNS VOID AS $$
DECLARE
    v_total_sagas INTEGER;
    v_running_sagas INTEGER;
    v_completed_sagas INTEGER;
    v_failed_sagas INTEGER;
    v_average_execution_time DECIMAL(10,2);
    v_success_rate DECIMAL(5,2);
BEGIN
    -- Calculate statistics
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE status = 'RUNNING'),
        COUNT(*) FILTER (WHERE status = 'COMPLETED'),
        COUNT(*) FILTER (WHERE status = 'FAILED'),
        AVG(EXTRACT(EPOCH FROM (completed_at - created_at))) FILTER (WHERE status = 'COMPLETED'),
        (COUNT(*) FILTER (WHERE status = 'COMPLETED')::DECIMAL / NULLIF(COUNT(*), 0) * 100)
    INTO v_total_sagas, v_running_sagas, v_completed_sagas, v_failed_sagas, v_average_execution_time, v_success_rate
    FROM saga_entity 
    WHERE tenant_id = p_tenant_id AND business_unit_id = p_business_unit_id;
    
    -- Insert or update statistics
    INSERT INTO saga_management_statistics (
        tenant_id, business_unit_id, date, total_sagas, running_sagas, 
        completed_sagas, failed_sagas, average_execution_time_seconds, success_rate
    ) VALUES (
        p_tenant_id, p_business_unit_id, CURRENT_DATE, v_total_sagas, v_running_sagas,
        v_completed_sagas, v_failed_sagas, v_average_execution_time, v_success_rate
    )
    ON CONFLICT (tenant_id, business_unit_id, date) 
    DO UPDATE SET
        total_sagas = EXCLUDED.total_sagas,
        running_sagas = EXCLUDED.running_sagas,
        completed_sagas = EXCLUDED.completed_sagas,
        failed_sagas = EXCLUDED.failed_sagas,
        average_execution_time_seconds = EXCLUDED.average_execution_time_seconds,
        success_rate = EXCLUDED.success_rate,
        updated_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Create a function to check for saga alerts
CREATE OR REPLACE FUNCTION check_saga_alerts(p_tenant_id VARCHAR, p_business_unit_id VARCHAR)
RETURNS VOID AS $$
DECLARE
    v_failed_count INTEGER;
    v_long_running_count INTEGER;
BEGIN
    -- Check for failed sagas
    SELECT COUNT(*) INTO v_failed_count
    FROM saga_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND status = 'FAILED'
    AND created_at > CURRENT_TIMESTAMP - INTERVAL '1 hour';
    
    IF v_failed_count > 5 THEN
        INSERT INTO saga_management_alerts (
            saga_id, alert_type, severity, message, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'FAILED_SAGAS_THRESHOLD', 'HIGH', 
            'High number of failed sagas detected: ' || v_failed_count,
            p_tenant_id, p_business_unit_id
        );
    END IF;
    
    -- Check for long-running sagas
    SELECT COUNT(*) INTO v_long_running_count
    FROM saga_entity 
    WHERE tenant_id = p_tenant_id 
    AND business_unit_id = p_business_unit_id 
    AND status = 'RUNNING'
    AND created_at < CURRENT_TIMESTAMP - INTERVAL '10 minutes';
    
    IF v_long_running_count > 0 THEN
        INSERT INTO saga_management_alerts (
            saga_id, alert_type, severity, message, tenant_id, business_unit_id
        ) VALUES (
            'SYSTEM', 'LONG_RUNNING_SAGAS', 'MEDIUM', 
            'Long-running sagas detected: ' || v_long_running_count,
            p_tenant_id, p_business_unit_id
        );
    END IF;
END;
$$ LANGUAGE plpgsql;
