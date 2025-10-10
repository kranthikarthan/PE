-- Payment Engine - Initial Database Schema
-- Flyway Migration V1
-- Created: 2025-10-10

-- =============================================================================
-- TRANSACTION REPAIR TABLE
-- Handles failed transaction compensation and repair
-- =============================================================================
CREATE TABLE IF NOT EXISTS transaction_repair (
    id BIGSERIAL PRIMARY KEY,
    transaction_reference VARCHAR(255) NOT NULL UNIQUE,
    tenant_id VARCHAR(100) NOT NULL,
    repair_type VARCHAR(50) NOT NULL,
    repair_status VARCHAR(50) NOT NULL,
    failure_reason TEXT,
    error_code VARCHAR(100),
    error_message TEXT,
    
    -- Transaction details
    from_account_number VARCHAR(100),
    to_account_number VARCHAR(100),
    amount DECIMAL(19,4),
    currency VARCHAR(3),
    payment_type VARCHAR(100),
    
    -- Debit/Credit tracking
    debit_status VARCHAR(50),
    debit_reference VARCHAR(255),
    debit_response JSONB,
    credit_status VARCHAR(50),
    credit_reference VARCHAR(255),
    credit_response JSONB,
    
    -- Original request
    original_request JSONB,
    
    -- Retry configuration
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    last_retry_at TIMESTAMP,
    
    -- Priority and timeout
    priority INTEGER DEFAULT 1,
    timeout_at TIMESTAMP,
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Indexes
    CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_transaction_repair_tenant ON transaction_repair(tenant_id);
CREATE INDEX idx_transaction_repair_status ON transaction_repair(repair_status);
CREATE INDEX idx_transaction_repair_type ON transaction_repair(repair_type);
CREATE INDEX idx_transaction_repair_created ON transaction_repair(created_at);
CREATE INDEX idx_transaction_repair_next_retry ON transaction_repair(next_retry_at);

-- =============================================================================
-- IDEMPOTENCY TABLE
-- Tracks processed requests to prevent duplicate processing
-- =============================================================================
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    tenant_id VARCHAR(100) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    
    -- Request details
    http_method VARCHAR(10),
    request_body JSONB,
    request_headers JSONB,
    
    -- Response details
    response_status INTEGER,
    response_body JSONB,
    response_headers JSONB,
    
    -- Processing info
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_idempotency_key ON idempotency_keys(idempotency_key);
CREATE INDEX idx_idempotency_tenant ON idempotency_keys(tenant_id);
CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
CREATE INDEX idx_idempotency_hash ON idempotency_keys(request_hash);

-- Auto-delete expired keys (run daily)
-- Note: This can be handled by a scheduled job or TTL in PostgreSQL 14+

-- =============================================================================
-- OUTBOX TABLE (for Transactional Outbox Pattern)
-- Ensures at-least-once delivery of domain events to Kafka
-- =============================================================================
CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    
    -- Event payload
    payload JSONB NOT NULL,
    
    -- Kafka details
    topic VARCHAR(255) NOT NULL,
    partition_key VARCHAR(255),
    
    -- Correlation tracking
    correlation_id VARCHAR(255),
    tenant_id VARCHAR(100),
    
    -- Processing status
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    last_error TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_outbox_status ON outbox_events(status);
CREATE INDEX idx_outbox_created ON outbox_events(created_at);
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_topic ON outbox_events(topic);

-- =============================================================================
-- MESSAGE FLOW TRACKING
-- Tracks ISO20022 message flows and correlations
-- =============================================================================
CREATE TABLE IF NOT EXISTS message_flow_history (
    id BIGSERIAL PRIMARY KEY,
    correlation_id VARCHAR(255) NOT NULL,
    message_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    
    -- Flow information
    flow_direction VARCHAR(50) NOT NULL,
    flow_status VARCHAR(50) NOT NULL,
    
    -- Message details
    message_payload JSONB,
    metadata JSONB,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Tenant
    tenant_id VARCHAR(100)
);

CREATE INDEX idx_message_flow_correlation ON message_flow_history(correlation_id);
CREATE INDEX idx_message_flow_message ON message_flow_history(message_id);
CREATE INDEX idx_message_flow_type ON message_flow_history(message_type);
CREATE INDEX idx_message_flow_created ON message_flow_history(created_at);

-- =============================================================================
-- AUDIT LOG TABLE
-- Comprehensive audit trail for all operations
-- =============================================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100),
    
    -- Action details
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(255),
    
    -- Changes
    old_value JSONB,
    new_value JSONB,
    
    -- Context
    ip_address VARCHAR(45),
    user_agent TEXT,
    correlation_id VARCHAR(255),
    
    -- Timestamp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_tenant ON audit_logs(tenant_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_audit_correlation ON audit_logs(correlation_id);

-- =============================================================================
-- TRIGGERS FOR UPDATED_AT
-- =============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_transaction_repair_updated_at 
    BEFORE UPDATE ON transaction_repair 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- INITIAL DATA / SEED DATA
-- =============================================================================

-- Insert default tenant (if needed)
-- INSERT INTO tenants (id, name, status) VALUES ('default', 'Default Tenant', 'ACTIVE');

-- =============================================================================
-- VIEWS FOR COMMON QUERIES
-- =============================================================================

-- View for pending repairs
CREATE OR REPLACE VIEW pending_repairs AS
SELECT 
    id,
    transaction_reference,
    tenant_id,
    repair_type,
    failure_reason,
    retry_count,
    max_retries,
    next_retry_at,
    created_at
FROM transaction_repair
WHERE repair_status = 'PENDING'
  AND (next_retry_at IS NULL OR next_retry_at <= CURRENT_TIMESTAMP)
  AND (timeout_at IS NULL OR timeout_at > CURRENT_TIMESTAMP)
  AND retry_count < max_retries
ORDER BY priority DESC, created_at ASC;

-- View for recent message flows
CREATE OR REPLACE VIEW recent_message_flows AS
SELECT 
    correlation_id,
    COUNT(*) as message_count,
    MIN(created_at) as started_at,
    MAX(created_at) as last_updated_at,
    ARRAY_AGG(message_type ORDER BY created_at) as message_types,
    ARRAY_AGG(flow_status ORDER BY created_at) as statuses
FROM message_flow_history
WHERE created_at > CURRENT_TIMESTAMP - INTERVAL '24 hours'
GROUP BY correlation_id
ORDER BY last_updated_at DESC;

-- =============================================================================
-- GRANTS (adjust based on your user setup)
-- =============================================================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO payment_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO payment_user;

COMMIT;
