-- Transaction Processing Service Database Schema
-- This migration creates the core tables for the transaction processing service

-- Create transactions table
CREATE TABLE transactions (
    transaction_id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50) NOT NULL,
    payment_id VARCHAR(255) NOT NULL,
    debit_account VARCHAR(50) NOT NULL,
    credit_account VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    clearing_system VARCHAR(50),
    clearing_reference VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    
    CONSTRAINT chk_transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transaction_different_accounts CHECK (debit_account != credit_account),
    CONSTRAINT chk_transaction_status CHECK (status IN ('CREATED', 'PROCESSING', 'CLEARING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEBIT', 'CREDIT', 'REVERSAL'))
);

-- Create ledger_entries table
CREATE TABLE ledger_entries (
    entry_id VARCHAR(255) PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    balance_before DECIMAL(19,4),
    balance_after DECIMAL(19,4),
    entry_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ledger_entry_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    CONSTRAINT chk_ledger_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_entry_amount_positive CHECK (amount > 0)
);

-- Create transaction_events table
CREATE TABLE transaction_events (
    event_id VARCHAR(255) PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    event_sequence BIGINT,
    correlation_id VARCHAR(255),
    causation_id VARCHAR(255),
    
    CONSTRAINT fk_transaction_event_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_transactions_tenant_id ON transactions(tenant_id);
CREATE INDEX idx_transactions_payment_id ON transactions(payment_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_clearing_system_ref ON transactions(clearing_system, clearing_reference);

CREATE INDEX idx_ledger_entries_tenant_account ON ledger_entries(tenant_id, account_number);
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entries_entry_date ON ledger_entries(entry_date);
CREATE INDEX idx_ledger_entries_entry_type ON ledger_entries(entry_type);

CREATE INDEX idx_transaction_events_transaction_id ON transaction_events(transaction_id);
CREATE INDEX idx_transaction_events_tenant_id ON transaction_events(tenant_id);
CREATE INDEX idx_transaction_events_event_type ON transaction_events(event_type);
CREATE INDEX idx_transaction_events_occurred_at ON transaction_events(occurred_at);
CREATE INDEX idx_transaction_events_correlation_id ON transaction_events(correlation_id);

-- Create composite indexes for common queries
CREATE INDEX idx_transactions_tenant_status ON transactions(tenant_id, status);
CREATE INDEX idx_transactions_tenant_created_at ON transactions(tenant_id, created_at);
CREATE INDEX idx_ledger_entries_tenant_account_date ON ledger_entries(tenant_id, account_number, entry_date);

-- Insert sample data for testing
INSERT INTO transactions (
    transaction_id, tenant_id, business_unit_id, payment_id, debit_account, credit_account,
    amount, currency, status, transaction_type, created_at
) VALUES (
    'TXN-SAMPLE-001', 'tenant-001', 'bu-001', 'PAY-001', 'ACC-001', 'ACC-002',
    1000.00, 'USD', 'CREATED', 'DEBIT', CURRENT_TIMESTAMP
);

INSERT INTO ledger_entries (
    entry_id, transaction_id, tenant_id, business_unit_id, account_number,
    entry_type, amount, balance_before, balance_after, entry_date, created_at
) VALUES 
(
    'LED-SAMPLE-001', 'TXN-SAMPLE-001', 'tenant-001', 'bu-001', 'ACC-001',
    'DEBIT', 1000.00, 5000.00, 4000.00, CURRENT_DATE, CURRENT_TIMESTAMP
),
(
    'LED-SAMPLE-002', 'TXN-SAMPLE-001', 'tenant-001', 'bu-001', 'ACC-002',
    'CREDIT', 1000.00, 2000.00, 3000.00, CURRENT_DATE, CURRENT_TIMESTAMP
);

INSERT INTO transaction_events (
    event_id, transaction_id, tenant_id, business_unit_id, event_type,
    event_data, occurred_at, event_sequence
) VALUES (
    'EVT-SAMPLE-001', 'TXN-SAMPLE-001', 'tenant-001', 'bu-001', 'TransactionCreated',
    '{"description": "Transaction created successfully"}', CURRENT_TIMESTAMP, 1
);






