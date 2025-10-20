-- Audit Service Database Schema
-- This migration creates tables for audit logging functionality

-- Audit logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    resource_id UUID,
    result VARCHAR(20) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    session_id VARCHAR(255),
    correlation_id VARCHAR(255),
    causation_id VARCHAR(255),
    source VARCHAR(100),
    version VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_audit_log_result CHECK (result IN ('SUCCESS', 'DENIED', 'ERROR')),
    CONSTRAINT chk_audit_log_action_length CHECK (LENGTH(action) >= 1)
);

-- Audit event processing table
CREATE TABLE audit_event_processing (
    id UUID PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_audit_event_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_audit_event_retry_count CHECK (retry_count >= 0)
);

-- Audit batch processing table
CREATE TABLE audit_batch_processing (
    id UUID PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    tenant_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_events INTEGER NOT NULL,
    processed_events INTEGER DEFAULT 0,
    failed_events INTEGER DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    
    CONSTRAINT chk_audit_batch_status CHECK (status IN ('STARTED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_audit_batch_events_positive CHECK (total_events > 0),
    CONSTRAINT chk_audit_batch_processed_events CHECK (processed_events >= 0),
    CONSTRAINT chk_audit_batch_failed_events CHECK (failed_events >= 0)
);

-- Indexes for performance
CREATE INDEX idx_audit_logs_tenant_timestamp ON audit_logs(tenant_id, timestamp DESC);
CREATE INDEX idx_audit_logs_user_timestamp ON audit_logs(user_id, timestamp DESC);
CREATE INDEX idx_audit_logs_action_timestamp ON audit_logs(action, timestamp DESC);
CREATE INDEX idx_audit_logs_result ON audit_logs(result);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource, resource_id);

CREATE INDEX idx_audit_event_processing_tenant_id ON audit_event_processing(tenant_id);
CREATE INDEX idx_audit_event_processing_status ON audit_event_processing(status);
CREATE INDEX idx_audit_event_processing_created_at ON audit_event_processing(created_at);

CREATE INDEX idx_audit_batch_processing_tenant_id ON audit_batch_processing(tenant_id);
CREATE INDEX idx_audit_batch_processing_status ON audit_batch_processing(status);
CREATE INDEX idx_audit_batch_processing_started_at ON audit_batch_processing(started_at);

-- Row Level Security for multi-tenancy
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_event_processing ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_batch_processing ENABLE ROW LEVEL SECURITY;

-- RLS Policies: Users can only access records for their tenant
CREATE POLICY audit_logs_tenant_isolation ON audit_logs
    FOR ALL TO PUBLIC
    USING (tenant_id = current_setting('app.current_tenant_id', true)::UUID);

CREATE POLICY audit_event_processing_tenant_isolation ON audit_event_processing
    FOR ALL TO PUBLIC
    USING (tenant_id = current_setting('app.current_tenant_id', true)::UUID);

CREATE POLICY audit_batch_processing_tenant_isolation ON audit_batch_processing
    FOR ALL TO PUBLIC
    USING (tenant_id = current_setting('app.current_tenant_id', true)::UUID);

-- Comments
COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail for all system operations';
COMMENT ON TABLE audit_event_processing IS 'Audit event processing status tracking';
COMMENT ON TABLE audit_batch_processing IS 'Batch processing status for audit events';

