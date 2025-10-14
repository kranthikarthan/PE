-- =====================================================
-- TRANSACTION PROCESSING SERVICE DATABASE
-- =====================================================
-- Event sourcing, double-entry bookkeeping, and transaction ledger

-- =====================================================
-- TRANSACTIONS (Main ledger)
-- =====================================================
CREATE TABLE transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    debit_account VARCHAR(50) NOT NULL,
    credit_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' 
        CHECK (status IN ('CREATED', 'VALIDATED', 'PROCESSING', 'CLEARING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'REVERSED')),
    transaction_type VARCHAR(50) NOT NULL DEFAULT 'PAYMENT',
    clearing_system VARCHAR(50),
    clearing_reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for transactions
CREATE INDEX idx_transactions_payment_id ON transactions(payment_id);
CREATE INDEX idx_transactions_tenant_id ON transactions(tenant_id);
CREATE INDEX idx_transactions_tenant_bu ON transactions(tenant_id, business_unit_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_debit_account ON transactions(debit_account);
CREATE INDEX idx_transactions_credit_account ON transactions(credit_account);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_clearing_reference ON transactions(clearing_reference);
CREATE INDEX idx_transactions_clearing_system ON transactions(clearing_system);

-- =====================================================
-- TRANSACTION EVENTS (Event Sourcing)
-- =====================================================
CREATE TABLE transaction_events (
    event_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    event_sequence BIGSERIAL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(50),
    causation_id VARCHAR(50),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_transaction_events FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- Indexes for transaction events
CREATE INDEX idx_transaction_events_transaction_id ON transaction_events(transaction_id);
CREATE INDEX idx_transaction_events_tenant_id ON transaction_events(tenant_id);
CREATE INDEX idx_transaction_events_tenant_bu ON transaction_events(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_events_sequence ON transaction_events(event_sequence);
CREATE INDEX idx_transaction_events_type ON transaction_events(event_type);
CREATE INDEX idx_transaction_events_occurred_at ON transaction_events(occurred_at DESC);
CREATE INDEX idx_transaction_events_correlation_id ON transaction_events(correlation_id);

-- =====================================================
-- LEDGER ENTRIES (Double-Entry Bookkeeping)
-- =====================================================
CREATE TABLE ledger_entries (
    entry_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    balance_before DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    entry_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_ledger_transaction FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- Indexes for ledger entries
CREATE INDEX idx_ledger_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_tenant_id ON ledger_entries(tenant_id);
CREATE INDEX idx_ledger_tenant_bu ON ledger_entries(tenant_id, business_unit_id);
CREATE INDEX idx_ledger_account_number ON ledger_entries(account_number);
CREATE INDEX idx_ledger_entry_date ON ledger_entries(entry_date DESC);
CREATE INDEX idx_ledger_composite ON ledger_entries(account_number, entry_date DESC);
CREATE INDEX idx_ledger_entry_type ON ledger_entries(entry_type);

-- =====================================================
-- ACCOUNT BALANCES (Current account balances)
-- =====================================================
CREATE TABLE account_balances (
    account_number VARCHAR(50) PRIMARY KEY,
    current_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    reserved_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_transaction_id VARCHAR(50),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for account balances
CREATE INDEX idx_account_balances_tenant_id ON account_balances(tenant_id);
CREATE INDEX idx_account_balances_tenant_bu ON account_balances(tenant_id, business_unit_id);
CREATE INDEX idx_account_balances_last_updated ON account_balances(last_updated DESC);

-- =====================================================
-- TRANSACTION FEES
-- =====================================================
CREATE TABLE transaction_fees (
    fee_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    fee_type VARCHAR(50) NOT NULL CHECK (fee_type IN ('PROCESSING', 'CLEARING', 'SETTLEMENT', 'CURRENCY_CONVERSION', 'PRIORITY')),
    fee_amount DECIMAL(18,2) NOT NULL CHECK (fee_amount >= 0),
    fee_currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    fee_description TEXT,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_transaction_fee FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- Indexes for transaction fees
CREATE INDEX idx_transaction_fees_transaction_id ON transaction_fees(transaction_id);
CREATE INDEX idx_transaction_fees_tenant_id ON transaction_fees(tenant_id);
CREATE INDEX idx_transaction_fees_tenant_bu ON transaction_fees(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_fees_type ON transaction_fees(fee_type);

-- =====================================================
-- TRANSACTION REVERSALS
-- =====================================================
CREATE TABLE transaction_reversals (
    reversal_id VARCHAR(50) PRIMARY KEY,
    original_transaction_id VARCHAR(50) NOT NULL,
    reversal_transaction_id VARCHAR(50) NOT NULL,
    reversal_reason VARCHAR(200) NOT NULL,
    reversal_amount DECIMAL(18,2) NOT NULL CHECK (reversal_amount > 0),
    reversal_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (reversal_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    initiated_by VARCHAR(100) NOT NULL,
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_reversal_original FOREIGN KEY (original_transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    CONSTRAINT fk_reversal_reversal FOREIGN KEY (reversal_transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- Indexes for transaction reversals
CREATE INDEX idx_transaction_reversals_original_id ON transaction_reversals(original_transaction_id);
CREATE INDEX idx_transaction_reversals_reversal_id ON transaction_reversals(reversal_transaction_id);
CREATE INDEX idx_transaction_reversals_tenant_id ON transaction_reversals(tenant_id);
CREATE INDEX idx_transaction_reversals_tenant_bu ON transaction_reversals(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_reversals_status ON transaction_reversals(reversal_status);
CREATE INDEX idx_transaction_reversals_initiated_at ON transaction_reversals(initiated_at DESC);

-- =====================================================
-- TRANSACTION AUDIT LOG
-- =====================================================
CREATE TABLE transaction_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    audit_type VARCHAR(50) NOT NULL CHECK (audit_type IN ('CREATED', 'UPDATED', 'STATUS_CHANGED', 'REVERSED', 'CANCELLED')),
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason TEXT,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_transaction_audit FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- Indexes for transaction audit log
CREATE INDEX idx_transaction_audit_transaction_id ON transaction_audit_log(transaction_id);
CREATE INDEX idx_transaction_audit_tenant_id ON transaction_audit_log(tenant_id);
CREATE INDEX idx_transaction_audit_tenant_bu ON transaction_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_transaction_audit_type ON transaction_audit_log(audit_type);
CREATE INDEX idx_transaction_audit_changed_at ON transaction_audit_log(changed_at DESC);

-- =====================================================
-- CONSTRAINTS & TRIGGERS
-- =====================================================

-- Ensure double-entry: every transaction has exactly 2 ledger entries
CREATE OR REPLACE FUNCTION validate_double_entry()
RETURNS TRIGGER AS $$
DECLARE
    entry_count INTEGER;
    debit_sum DECIMAL(18,2);
    credit_sum DECIMAL(18,2);
BEGIN
    SELECT COUNT(*), 
           SUM(CASE WHEN entry_type = 'DEBIT' THEN amount ELSE 0 END),
           SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE 0 END)
    INTO entry_count, debit_sum, credit_sum
    FROM ledger_entries
    WHERE transaction_id = NEW.transaction_id;
    
    IF entry_count = 2 AND debit_sum != credit_sum THEN
        RAISE EXCEPTION 'Double-entry violation: debits (%) != credits (%)', debit_sum, credit_sum;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_double_entry_trigger
    AFTER INSERT ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION validate_double_entry();

-- Auto-update account balances when ledger entries are created
CREATE OR REPLACE FUNCTION update_account_balance()
RETURNS TRIGGER AS $$
DECLARE
    current_bal DECIMAL(18,2);
    new_bal DECIMAL(18,2);
BEGIN
    -- Get current balance
    SELECT COALESCE(current_balance, 0) INTO current_bal
    FROM account_balances
    WHERE account_number = NEW.account_number;
    
    -- Calculate new balance
    IF NEW.entry_type = 'DEBIT' THEN
        new_bal := current_bal - NEW.amount;
    ELSE
        new_bal := current_bal + NEW.amount;
    END IF;
    
    -- Update or insert account balance
    INSERT INTO account_balances (account_number, current_balance, last_transaction_id, tenant_id, business_unit_id)
    VALUES (NEW.account_number, new_bal, NEW.transaction_id, NEW.tenant_id, NEW.business_unit_id)
    ON CONFLICT (account_number) 
    DO UPDATE SET 
        current_balance = new_bal,
        last_transaction_id = NEW.transaction_id,
        last_updated = CURRENT_TIMESTAMP;
    
    -- Update the ledger entry with balance information
    UPDATE ledger_entries 
    SET balance_before = current_bal, balance_after = new_bal
    WHERE entry_id = NEW.entry_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_account_balance_trigger
    AFTER INSERT ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION update_account_balance();

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_transactions_updated_at 
    BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE ledger_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE account_balances ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_fees ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_reversals ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_audit_log ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY tenant_isolation_transactions ON transactions
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_transaction_events ON transaction_events
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_ledger_entries ON ledger_entries
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_account_balances ON account_balances
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_transaction_fees ON transaction_fees
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_transaction_reversals ON transaction_reversals
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_transaction_audit_log ON transaction_audit_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View for transaction summary with balances
CREATE VIEW transaction_summary AS
SELECT 
    t.transaction_id,
    t.payment_id,
    t.tenant_id,
    t.business_unit_id,
    t.debit_account,
    t.credit_account,
    t.amount,
    t.currency,
    t.status,
    t.transaction_type,
    t.clearing_system,
    t.clearing_reference,
    t.created_at,
    t.completed_at,
    ab_debit.current_balance AS debit_account_balance,
    ab_credit.current_balance AS credit_account_balance,
    (SELECT COUNT(*) FROM transaction_events te WHERE te.transaction_id = t.transaction_id) AS event_count,
    (SELECT COUNT(*) FROM ledger_entries le WHERE le.transaction_id = t.transaction_id) AS ledger_entry_count
FROM transactions t
LEFT JOIN account_balances ab_debit ON t.debit_account = ab_debit.account_number
LEFT JOIN account_balances ab_credit ON t.credit_account = ab_credit.account_number
WHERE t.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- View for account transaction history
CREATE VIEW account_transaction_history AS
SELECT 
    le.account_number,
    le.tenant_id,
    le.business_unit_id,
    le.entry_type,
    le.amount,
    le.balance_before,
    le.balance_after,
    le.entry_date,
    le.created_at,
    t.transaction_id,
    t.payment_id,
    t.status AS transaction_status,
    t.transaction_type
FROM ledger_entries le
JOIN transactions t ON le.transaction_id = t.transaction_id
WHERE le.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
ORDER BY le.account_number, le.created_at DESC;

-- View for daily transaction summary
CREATE VIEW daily_transaction_summary AS
SELECT 
    t.tenant_id,
    t.business_unit_id,
    DATE(t.created_at) AS transaction_date,
    t.transaction_type,
    t.status,
    COUNT(*) AS transaction_count,
    SUM(t.amount) AS total_amount,
    AVG(t.amount) AS average_amount,
    MIN(t.amount) AS min_amount,
    MAX(t.amount) AS max_amount
FROM transactions t
WHERE t.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY t.tenant_id, t.business_unit_id, DATE(t.created_at), t.transaction_type, t.status;

-- =====================================================
-- FUNCTIONS
-- =====================================================

-- Function to get account balance
CREATE OR REPLACE FUNCTION get_account_balance(p_account_number VARCHAR(50))
RETURNS TABLE (
    account_number VARCHAR(50),
    current_balance DECIMAL(18,2),
    available_balance DECIMAL(18,2),
    reserved_balance DECIMAL(18,2),
    currency VARCHAR(3),
    last_updated TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ab.account_number,
        ab.current_balance,
        ab.available_balance,
        ab.reserved_balance,
        ab.currency,
        ab.last_updated
    FROM account_balances ab
    WHERE ab.account_number = p_account_number
      AND ab.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;
END;
$$ LANGUAGE plpgsql;

-- Function to get transaction statistics
CREATE OR REPLACE FUNCTION get_transaction_stats(p_tenant_id VARCHAR(20), p_date_from DATE, p_date_to DATE)
RETURNS TABLE (
    total_transactions BIGINT,
    total_amount DECIMAL(18,2),
    successful_transactions BIGINT,
    failed_transactions BIGINT,
    avg_processing_time INTERVAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT AS total_transactions,
        COALESCE(SUM(t.amount), 0) AS total_amount,
        COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END)::BIGINT AS successful_transactions,
        COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END)::BIGINT AS failed_transactions,
        AVG(CASE 
            WHEN t.status = 'COMPLETED' AND t.completed_at IS NOT NULL 
            THEN t.completed_at - t.created_at 
        END) AS avg_processing_time
    FROM transactions t
    WHERE t.tenant_id = p_tenant_id
      AND DATE(t.created_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE transactions IS 'Main transaction ledger with event sourcing support';
COMMENT ON TABLE transaction_events IS 'Event sourcing log for transaction state changes';
COMMENT ON TABLE ledger_entries IS 'Double-entry bookkeeping entries for each transaction';
COMMENT ON TABLE account_balances IS 'Current account balances maintained in real-time';
COMMENT ON TABLE transaction_fees IS 'Fees associated with transactions';
COMMENT ON TABLE transaction_reversals IS 'Transaction reversal tracking and audit';
COMMENT ON TABLE transaction_audit_log IS 'Audit trail for all transaction operations';

COMMENT ON COLUMN transactions.transaction_id IS 'Unique transaction identifier';
COMMENT ON COLUMN transactions.payment_id IS 'Reference to the originating payment';
COMMENT ON COLUMN transactions.debit_account IS 'Account to be debited';
COMMENT ON COLUMN transactions.credit_account IS 'Account to be credited';
COMMENT ON COLUMN transactions.clearing_system IS 'Clearing system used (SAMOS, BankservAfrica, etc.)';
COMMENT ON COLUMN transactions.clearing_reference IS 'Reference from the clearing system';

COMMENT ON COLUMN transaction_events.event_sequence IS 'Sequential event number for ordering';
COMMENT ON COLUMN transaction_events.event_type IS 'Type of event (TransactionCreated, StatusChanged, etc.)';
COMMENT ON COLUMN transaction_events.event_data IS 'JSONB data specific to the event type';
COMMENT ON COLUMN transaction_events.correlation_id IS 'Correlation ID for distributed tracing';
COMMENT ON COLUMN transaction_events.causation_id IS 'ID of the event that caused this event';

COMMENT ON COLUMN ledger_entries.entry_type IS 'Type of ledger entry (DEBIT or CREDIT)';
COMMENT ON COLUMN ledger_entries.balance_before IS 'Account balance before this entry';
COMMENT ON COLUMN ledger_entries.balance_after IS 'Account balance after this entry';

COMMENT ON COLUMN account_balances.current_balance IS 'Current account balance';
COMMENT ON COLUMN account_balances.available_balance IS 'Available balance (current - reserved)';
COMMENT ON COLUMN account_balances.reserved_balance IS 'Amount reserved for pending transactions';

COMMENT ON COLUMN transaction_reversals.original_transaction_id IS 'ID of the transaction being reversed';
COMMENT ON COLUMN transaction_reversals.reversal_transaction_id IS 'ID of the reversal transaction';
COMMENT ON COLUMN transaction_reversals.reversal_reason IS 'Reason for the reversal';
