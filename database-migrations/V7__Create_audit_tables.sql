-- V7: Create Audit Service Tables
-- Purpose: POPIA/FICA/PCI-DSS compliance audit trail
-- Retention: 7 years minimum
-- Immutable: No updates/deletes after creation

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    resource_id UUID,
    result VARCHAR(20) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes for Common Queries
CREATE INDEX idx_audit_tenant_timestamp ON audit_logs(tenant_id, timestamp DESC);
CREATE INDEX idx_audit_user_timestamp ON audit_logs(user_id, timestamp DESC);
CREATE INDEX idx_audit_action_timestamp ON audit_logs(action, timestamp DESC);
CREATE INDEX idx_audit_result ON audit_logs(result);

-- Composite Index for Multi-tenant Time Range Queries
CREATE INDEX idx_audit_tenant_result_timestamp 
  ON audit_logs(tenant_id, result, timestamp DESC);

-- Foreign Key Constraints (soft references - no hard constraints for flexibility)
-- audit_logs.tenant_id references tenants.tenant_id (no hard FK)
-- audit_logs.resource_id can reference any resource UUID

-- Immutability Enforcement (via application layer)
-- - No UPDATE statements allowed
-- - No DELETE statements allowed (archival only)
-- - Only INSERT operations permitted

-- Row-Level Security for Multi-Tenancy
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY audit_logs_tenant_policy 
  ON audit_logs 
  FOR SELECT 
  USING (tenant_id = current_setting('app.current_tenant_id')::UUID);

-- Audit Log Table for the Audit Service Itself (meta-audit)
CREATE TABLE IF NOT EXISTS audit_service_operations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation VARCHAR(100) NOT NULL,
    result VARCHAR(20) NOT NULL,
    records_processed INT,
    duration_ms BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT
);

CREATE INDEX idx_audit_service_timestamp 
  ON audit_service_operations(timestamp DESC);

-- Grant Permissions
GRANT SELECT, INSERT ON audit_logs TO payments_engine_app_role;
GRANT SELECT, INSERT ON audit_service_operations TO payments_engine_app_role;

-- Audit Trail Comment
COMMENT ON TABLE audit_logs IS 
  'Immutable compliance audit trail for POPIA/FICA/PCI-DSS. 7-year retention. No updates/deletes.';

COMMENT ON COLUMN audit_logs.tenant_id IS 
  'Multi-tenant isolation key. All queries filtered by tenant_id.';

COMMENT ON COLUMN audit_logs.result IS 
  'Audit outcome: SUCCESS, DENIED (auth failure), ERROR (system failure).';

COMMENT ON COLUMN audit_logs.timestamp IS 
  'Immutable creation timestamp. Used for compliance time-based queries.';
