-- Operations Management Service Database Schema
-- Migration: V1__Create_operations_management_tables.sql

-- Service Health Table
CREATE TABLE service_health (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('UP', 'DOWN', 'DEGRADED', 'UNKNOWN')),
    uptime_percentage DECIMAL(5,2),
    request_rate DECIMAL(10,2),
    error_rate DECIMAL(5,2),
    response_time_p50 BIGINT,
    response_time_p95 BIGINT,
    response_time_p99 BIGINT,
    circuit_breaker_state VARCHAR(20) CHECK (circuit_breaker_state IN ('CLOSED', 'OPEN', 'HALF_OPEN', 'DISABLED')),
    circuit_breaker_failure_rate DECIMAL(5,2),
    pod_count INTEGER,
    cpu_usage_percentage DECIMAL(5,2),
    memory_usage_mb BIGINT,
    last_health_check TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Pod Information Table
CREATE TABLE pod_info (
    id BIGSERIAL PRIMARY KEY,
    pod_name VARCHAR(255) NOT NULL,
    namespace VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('RUNNING', 'PENDING', 'FAILED', 'SUCCEEDED', 'UNKNOWN')),
    cpu_usage_percentage DECIMAL(5,2),
    memory_usage_mb BIGINT,
    restart_count INTEGER DEFAULT 0,
    age_seconds BIGINT,
    node_name VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    service_health_id BIGINT REFERENCES service_health(id) ON DELETE CASCADE
);

-- Operations Audit Log Table
CREATE TABLE operations_audit_log (
    id BIGSERIAL PRIMARY KEY,
    audit_id VARCHAR(50) NOT NULL UNIQUE,
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN (
        'PAYMENT_RETRY', 'SAGA_RESUME', 'SAGA_COMPENSATE', 'CIRCUIT_BREAKER_OPEN', 
        'CIRCUIT_BREAKER_CLOSE', 'FEATURE_FLAG_TOGGLE', 'FEATURE_FLAG_ROLLOUT', 
        'POD_RESTART', 'SERVICE_SCALE', 'SERVICE_RESTART', 'ALERT_ACKNOWLEDGE', 'ALERT_RESOLVE'
    )),
    entity_type VARCHAR(50) NOT NULL CHECK (entity_type IN (
        'PAYMENT', 'SAGA', 'CIRCUIT_BREAKER', 'FEATURE_FLAG', 'POD', 'SERVICE', 'ALERT'
    )),
    entity_id VARCHAR(50) NOT NULL,
    action_details JSONB,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_service_health_name ON service_health(service_name);
CREATE INDEX idx_service_health_status ON service_health(status);
CREATE INDEX idx_service_health_updated_at ON service_health(updated_at);

CREATE INDEX idx_pod_info_service_health_id ON pod_info(service_health_id);
CREATE INDEX idx_pod_info_name ON pod_info(pod_name);
CREATE INDEX idx_pod_info_status ON pod_info(status);

CREATE INDEX idx_audit_log_tenant_id ON operations_audit_log(tenant_id);
CREATE INDEX idx_audit_log_user_id ON operations_audit_log(user_id);
CREATE INDEX idx_audit_log_action_type ON operations_audit_log(action_type);
CREATE INDEX idx_audit_log_entity_type ON operations_audit_log(entity_type);
CREATE INDEX idx_audit_log_entity_id ON operations_audit_log(entity_id);
CREATE INDEX idx_audit_log_performed_at ON operations_audit_log(performed_at);

-- Update triggers for updated_at columns
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_service_health_updated_at 
    BEFORE UPDATE ON service_health 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pod_info_updated_at 
    BEFORE UPDATE ON pod_info 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
