-- Payment Initiation Service Database Schema
-- This migration creates tables for payment initiation functionality

-- Idempotency tracking table
CREATE TABLE idempotency_records (
    id VARCHAR(36) PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(20) NOT NULL,
    payment_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_idempotency_key_tenant UNIQUE (idempotency_key, tenant_id)
);

-- Indexes for performance
CREATE INDEX idx_idempotency_key_tenant ON idempotency_records (idempotency_key, tenant_id);
CREATE INDEX idx_idempotency_created_at ON idempotency_records (created_at);

-- Row Level Security for multi-tenancy
ALTER TABLE idempotency_records ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can only access records for their tenant
CREATE POLICY idempotency_tenant_isolation ON idempotency_records
    FOR ALL TO PUBLIC
    USING (tenant_id = current_setting('app.current_tenant_id', true));

-- Comments
COMMENT ON TABLE idempotency_records IS 'Tracks idempotency keys to prevent duplicate payment processing';
COMMENT ON COLUMN idempotency_records.id IS 'Unique record identifier';
COMMENT ON COLUMN idempotency_records.idempotency_key IS 'Idempotency key provided by client';
COMMENT ON COLUMN idempotency_records.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN idempotency_records.payment_id IS 'Associated payment identifier';
COMMENT ON COLUMN idempotency_records.created_at IS 'Record creation timestamp';
