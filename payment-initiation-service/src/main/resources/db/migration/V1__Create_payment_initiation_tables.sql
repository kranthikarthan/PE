-- Payment Initiation Service Database Schema
-- This migration creates tables for payment initiation functionality

-- Payments table (main entity)
CREATE TABLE payments (
    payment_id VARCHAR(36) PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    source_account VARCHAR(11) NOT NULL,
    destination_account VARCHAR(11) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference VARCHAR(35) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    initiated_by VARCHAR(255) NOT NULL,
    initiated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validated_at TIMESTAMP WITH TIME ZONE,
    submitted_to_clearing_at TIMESTAMP WITH TIME ZONE,
    cleared_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    failure_reason VARCHAR(500),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_payment_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payment_different_accounts CHECK (source_account != destination_account),
    CONSTRAINT chk_payment_status CHECK (status IN ('INITIATED', 'VALIDATED', 'SUBMITTED_TO_CLEARING', 'CLEARED', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_payment_type CHECK (payment_type IN ('DOMESTIC', 'INTERNATIONAL', 'INSTANT', 'BATCH')),
    CONSTRAINT chk_payment_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

-- Payment status history table
CREATE TABLE payment_status_history (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_status_history_payment FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Idempotency tracking table
CREATE TABLE idempotency_records (
    id VARCHAR(36) PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(20) NOT NULL,
    payment_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_idempotency_key_tenant UNIQUE (idempotency_key, tenant_id),
    CONSTRAINT fk_idempotency_payment FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_payments_tenant_id ON payments(tenant_id);
CREATE INDEX idx_payments_business_unit_id ON payments(business_unit_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_initiated_at ON payments(initiated_at);
CREATE INDEX idx_payments_source_account ON payments(source_account);
CREATE INDEX idx_payments_destination_account ON payments(destination_account);

CREATE INDEX idx_payment_status_history_payment_id ON payment_status_history(payment_id);
CREATE INDEX idx_payment_status_history_changed_at ON payment_status_history(changed_at);

CREATE INDEX idx_idempotency_key_tenant ON idempotency_records (idempotency_key, tenant_id);
CREATE INDEX idx_idempotency_created_at ON idempotency_records (created_at);

-- Row Level Security for multi-tenancy
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_status_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE idempotency_records ENABLE ROW LEVEL SECURITY;

-- RLS Policies: Users can only access records for their tenant
CREATE POLICY payments_tenant_isolation ON payments
    FOR ALL TO PUBLIC
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY payment_status_history_tenant_isolation ON payment_status_history
    FOR ALL TO PUBLIC
    USING (payment_id IN (SELECT payment_id FROM payments WHERE tenant_id = current_setting('app.current_tenant_id', true)));

CREATE POLICY idempotency_tenant_isolation ON idempotency_records
    FOR ALL TO PUBLIC
    USING (tenant_id = current_setting('app.current_tenant_id', true));

-- Comments
COMMENT ON TABLE payments IS 'Payment records with full lifecycle tracking';
COMMENT ON TABLE payment_status_history IS 'Audit trail of payment status changes';
COMMENT ON TABLE idempotency_records IS 'Tracks idempotency keys to prevent duplicate payment processing';
