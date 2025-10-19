-- V13__Create_settlement_monitoring_tables.sql

-- Table for Settlement Monitoring
CREATE TABLE settlement_monitoring (
    id BIGSERIAL PRIMARY KEY,
    monitoring_id VARCHAR(100) NOT NULL UNIQUE,
    monitoring_name VARCHAR(255) NOT NULL,
    description TEXT,
    monitoring_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(19,4),
    metric_unit VARCHAR(20),
    threshold_value DECIMAL(19,4),
    alert_level VARCHAR(20),
    monitoring_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_seconds BIGINT,
    participant_id VARCHAR(50),
    workflow_id BIGINT,
    orchestration_id BIGINT,
    currency VARCHAR(3),
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_settlement_monitoring_monitoring_id ON settlement_monitoring (monitoring_id);
CREATE INDEX idx_settlement_monitoring_tenant_id ON settlement_monitoring (tenant_id);
CREATE INDEX idx_settlement_monitoring_monitoring_type ON settlement_monitoring (monitoring_type);
CREATE INDEX idx_settlement_monitoring_status ON settlement_monitoring (status);
CREATE INDEX idx_settlement_monitoring_metric_name ON settlement_monitoring (metric_name);
CREATE INDEX idx_settlement_monitoring_participant_id ON settlement_monitoring (participant_id);
CREATE INDEX idx_settlement_monitoring_workflow_id ON settlement_monitoring (workflow_id);
CREATE INDEX idx_settlement_monitoring_orchestration_id ON settlement_monitoring (orchestration_id);
CREATE INDEX idx_settlement_monitoring_currency ON settlement_monitoring (currency);
CREATE INDEX idx_settlement_monitoring_business_unit_id ON settlement_monitoring (business_unit_id);
CREATE INDEX idx_settlement_monitoring_monitoring_timestamp ON settlement_monitoring (monitoring_timestamp);
CREATE INDEX idx_settlement_monitoring_alert_level ON settlement_monitoring (alert_level);

-- RLS Policy for settlement_monitoring
ALTER TABLE settlement_monitoring ENABLE ROW LEVEL SECURITY;
CREATE POLICY settlement_monitoring_isolation_policy
    ON settlement_monitoring
    USING (tenant_id = current_setting('app.tenant_id'));

-- Table for Settlement Alerts
CREATE TABLE settlement_alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(100) NOT NULL UNIQUE,
    alert_name VARCHAR(255) NOT NULL,
    description TEXT,
    alert_type VARCHAR(20) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    alert_message TEXT,
    alert_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    acknowledged_by VARCHAR(100),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by VARCHAR(100),
    resolution_notes TEXT,
    participant_id VARCHAR(50),
    workflow_id BIGINT,
    orchestration_id BIGINT,
    monitoring_id BIGINT,
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_settlement_alerts_alert_id ON settlement_alerts (alert_id);
CREATE INDEX idx_settlement_alerts_tenant_id ON settlement_alerts (tenant_id);
CREATE INDEX idx_settlement_alerts_alert_type ON settlement_alerts (alert_type);
CREATE INDEX idx_settlement_alerts_severity ON settlement_alerts (severity);
CREATE INDEX idx_settlement_alerts_status ON settlement_alerts (status);
CREATE INDEX idx_settlement_alerts_participant_id ON settlement_alerts (participant_id);
CREATE INDEX idx_settlement_alerts_workflow_id ON settlement_alerts (workflow_id);
CREATE INDEX idx_settlement_alerts_orchestration_id ON settlement_alerts (orchestration_id);
CREATE INDEX idx_settlement_alerts_monitoring_id ON settlement_alerts (monitoring_id);
CREATE INDEX idx_settlement_alerts_business_unit_id ON settlement_alerts (business_unit_id);
CREATE INDEX idx_settlement_alerts_alert_timestamp ON settlement_alerts (alert_timestamp);
CREATE INDEX idx_settlement_alerts_acknowledged_at ON settlement_alerts (acknowledged_at);
CREATE INDEX idx_settlement_alerts_resolved_at ON settlement_alerts (resolved_at);

-- RLS Policy for settlement_alerts
ALTER TABLE settlement_alerts ENABLE ROW LEVEL SECURITY;
CREATE POLICY settlement_alerts_isolation_policy
    ON settlement_alerts
    USING (tenant_id = current_setting('app.tenant_id'));

-- Table for Settlement Metrics
CREATE TABLE settlement_metrics (
    id BIGSERIAL PRIMARY KEY,
    metrics_id VARCHAR(100) NOT NULL UNIQUE,
    metrics_name VARCHAR(255) NOT NULL,
    description TEXT,
    metrics_type VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(19,4),
    metric_unit VARCHAR(20),
    baseline_value DECIMAL(19,4),
    target_value DECIMAL(19,4),
    threshold_min DECIMAL(19,4),
    threshold_max DECIMAL(19,4),
    metrics_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE,
    period_end TIMESTAMP WITH TIME ZONE,
    participant_id VARCHAR(50),
    workflow_id BIGINT,
    orchestration_id BIGINT,
    currency VARCHAR(3),
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_settlement_metrics_metrics_id ON settlement_metrics (metrics_id);
CREATE INDEX idx_settlement_metrics_tenant_id ON settlement_metrics (tenant_id);
CREATE INDEX idx_settlement_metrics_metrics_type ON settlement_metrics (metrics_type);
CREATE INDEX idx_settlement_metrics_category ON settlement_metrics (category);
CREATE INDEX idx_settlement_metrics_metric_name ON settlement_metrics (metric_name);
CREATE INDEX idx_settlement_metrics_participant_id ON settlement_metrics (participant_id);
CREATE INDEX idx_settlement_metrics_workflow_id ON settlement_metrics (workflow_id);
CREATE INDEX idx_settlement_metrics_orchestration_id ON settlement_metrics (orchestration_id);
CREATE INDEX idx_settlement_metrics_currency ON settlement_metrics (currency);
CREATE INDEX idx_settlement_metrics_business_unit_id ON settlement_metrics (business_unit_id);
CREATE INDEX idx_settlement_metrics_metrics_timestamp ON settlement_metrics (metrics_timestamp);
CREATE INDEX idx_settlement_metrics_period_start ON settlement_metrics (period_start);
CREATE INDEX idx_settlement_metrics_period_end ON settlement_metrics (period_end);

-- RLS Policy for settlement_metrics
ALTER TABLE settlement_metrics ENABLE ROW LEVEL SECURITY;
CREATE POLICY settlement_metrics_isolation_policy
    ON settlement_metrics
    USING (tenant_id = current_setting('app.tenant_id'));

-- Trigger to update 'updated_at' column automatically for settlement_monitoring
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_settlement_monitoring_updated_at
    BEFORE UPDATE ON settlement_monitoring
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update 'updated_at' column automatically for settlement_alerts
CREATE TRIGGER update_settlement_alerts_updated_at
    BEFORE UPDATE ON settlement_alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update 'updated_at' column automatically for settlement_metrics
CREATE TRIGGER update_settlement_metrics_updated_at
    BEFORE UPDATE ON settlement_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
