-- V14__Create_reconciliation_exception_tables.sql

-- Table for Reconciliation Exceptions
CREATE TABLE reconciliation_exceptions (
    id BIGSERIAL PRIMARY KEY,
    exception_id VARCHAR(100) NOT NULL UNIQUE,
    run_id BIGINT NOT NULL,
    exception_type VARCHAR(50) NOT NULL,
    internal_transaction_id VARCHAR(100),
    clearing_transaction_id VARCHAR(100),
    amount_difference DECIMAL(19,4),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    description VARCHAR(500),
    details TEXT,
    priority VARCHAR(20),
    assigned_to VARCHAR(100),
    resolution VARCHAR(500),
    resolution_notes TEXT,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by VARCHAR(100),
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_reconciliation_exception_run FOREIGN KEY (run_id) REFERENCES reconciliation_runs(id) ON DELETE CASCADE
);

CREATE INDEX idx_reconciliation_exceptions_exception_id ON reconciliation_exceptions (exception_id);
CREATE INDEX idx_reconciliation_exceptions_run_id ON reconciliation_exceptions (run_id);
CREATE INDEX idx_reconciliation_exceptions_tenant_id ON reconciliation_exceptions (tenant_id);
CREATE INDEX idx_reconciliation_exceptions_exception_type ON reconciliation_exceptions (exception_type);
CREATE INDEX idx_reconciliation_exceptions_status ON reconciliation_exceptions (status);
CREATE INDEX idx_reconciliation_exceptions_assigned_to ON reconciliation_exceptions (assigned_to);
CREATE INDEX idx_reconciliation_exceptions_internal_txn_id ON reconciliation_exceptions (internal_transaction_id);
CREATE INDEX idx_reconciliation_exceptions_clearing_txn_id ON reconciliation_exceptions (clearing_transaction_id);
CREATE INDEX idx_reconciliation_exceptions_priority ON reconciliation_exceptions (priority);
CREATE INDEX idx_reconciliation_exceptions_resolved_at ON reconciliation_exceptions (resolved_at);
CREATE INDEX idx_reconciliation_exceptions_created_at ON reconciliation_exceptions (created_at);

-- RLS Policy for reconciliation_exceptions
ALTER TABLE reconciliation_exceptions ENABLE ROW LEVEL SECURITY;
CREATE POLICY reconciliation_exceptions_isolation_policy
    ON reconciliation_exceptions
    USING (tenant_id = current_setting('app.tenant_id'));

-- Trigger to update 'updated_at' column automatically for reconciliation_exceptions
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_reconciliation_exceptions_updated_at
    BEFORE UPDATE ON reconciliation_exceptions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
