-- V9: SAMOS Settlement Account Management Tables
-- Purpose: Create tables for managing SARB settlement accounts for RTGS payments

-- SAMOS Settlement Accounts Table
CREATE TABLE IF NOT EXISTS samos_settlement_accounts (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    
    -- Account Information
    account_number VARCHAR(9) NOT NULL,
    bank_code VARCHAR(6) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    
    -- Balance Information
    current_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    reserved_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    -- Limits and Collateral
    daily_debit_limit DECIMAL(18,2) NOT NULL,
    collateral_requirement DECIMAL(18,2) NOT NULL,
    collateral_pledged DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    -- Account Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    account_type VARCHAR(20) NOT NULL DEFAULT 'SETTLEMENT',
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    
    -- Settlement Tracking
    last_balance_update TIMESTAMP,
    last_settlement_time TIMESTAMP,
    settlements_today INTEGER DEFAULT 0,
    amount_settled_today DECIMAL(18,2) DEFAULT 0.00,
    
    -- SARB Contact Information
    sarb_contact_email VARCHAR(255),
    sarb_contact_phone VARCHAR(20),
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    -- Foreign Key
    samos_adapter_id VARCHAR(36),
    
    -- Constraints
    CONSTRAINT uk_samos_settlement_tenant_account UNIQUE (tenant_id, account_number),
    CONSTRAINT uk_samos_settlement_tenant_bank UNIQUE (tenant_id, bank_code),
    CONSTRAINT chk_samos_settlement_account_number CHECK (account_number ~ '^\d{9}$'),
    CONSTRAINT chk_samos_settlement_balance CHECK (current_balance >= 0),
    CONSTRAINT chk_samos_settlement_reserved CHECK (reserved_balance >= 0),
    CONSTRAINT chk_samos_settlement_collateral_pledged CHECK (collateral_pledged >= 0),
    CONSTRAINT chk_samos_settlement_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT chk_samos_settlement_account_type CHECK (account_type IN ('SETTLEMENT', 'COLLATERAL', 'BACKUP')),
    CONSTRAINT chk_samos_settlement_currency CHECK (currency = 'ZAR')
);

-- Indexes for SAMOS Settlement Accounts
CREATE INDEX idx_samos_settlement_tenant ON samos_settlement_accounts(tenant_id);
CREATE INDEX idx_samos_settlement_account_number ON samos_settlement_accounts(account_number);
CREATE INDEX idx_samos_settlement_bank_code ON samos_settlement_accounts(bank_code);
CREATE INDEX idx_samos_settlement_status ON samos_settlement_accounts(status);
CREATE INDEX idx_samos_settlement_adapter ON samos_settlement_accounts(samos_adapter_id);

-- Comments for Documentation
COMMENT ON TABLE samos_settlement_accounts IS 'SARB settlement accounts for RTGS (Real-Time Gross Settlement) payments through SAMOS';
COMMENT ON COLUMN samos_settlement_accounts.account_number IS 'SARB settlement account number (9-digit)';
COMMENT ON COLUMN samos_settlement_accounts.bank_code IS 'SARB member bank code';
COMMENT ON COLUMN samos_settlement_accounts.current_balance IS 'Current available balance in ZAR';
COMMENT ON COLUMN samos_settlement_accounts.reserved_balance IS 'Reserved balance for pending settlements in ZAR';
COMMENT ON COLUMN samos_settlement_accounts.daily_debit_limit IS 'Maximum daily debit limit in ZAR';
COMMENT ON COLUMN samos_settlement_accounts.collateral_requirement IS 'Minimum collateral requirement in ZAR';
COMMENT ON COLUMN samos_settlement_accounts.collateral_pledged IS 'Current collateral pledged in ZAR';
COMMENT ON COLUMN samos_settlement_accounts.settlements_today IS 'Number of settlements processed today';
COMMENT ON COLUMN samos_settlement_accounts.amount_settled_today IS 'Total amount settled today in ZAR';

-- Row-Level Security (RLS) Policy for Multi-Tenancy
ALTER TABLE samos_settlement_accounts ENABLE ROW LEVEL SECURITY;

CREATE POLICY samos_settlement_tenant_isolation ON samos_settlement_accounts
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE));

-- Trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_samos_settlement_account_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_samos_settlement_account_timestamp
    BEFORE UPDATE ON samos_settlement_accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_samos_settlement_account_timestamp();

-- Grant Permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON samos_settlement_accounts TO payment_engine_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO payment_engine_app;

