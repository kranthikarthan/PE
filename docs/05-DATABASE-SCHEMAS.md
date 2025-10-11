# Database Schemas - Complete Design

## Overview
This document contains complete database schemas for all microservices. Each service owns its database (database-per-service pattern).

---

## Database Technology Mapping

| Service | Database | Reason |
|---------|----------|--------|
| Payment Initiation | PostgreSQL | ACID compliance, complex queries |
| Validation | PostgreSQL + Redis | Structured data + caching rules |
| Account | PostgreSQL | ACID compliance, financial data |
| Routing | Redis | Fast lookups, caching |
| Transaction Processing | PostgreSQL | Event sourcing, ledger |
| Clearing Adapters | PostgreSQL | Message tracking, audit trail |
| Settlement | PostgreSQL | Financial calculations, batching |
| Reconciliation | PostgreSQL | Complex matching queries |
| Notification | PostgreSQL | Delivery tracking |
| Reporting | PostgreSQL + Synapse | OLTP + OLAP |
| Saga Orchestrator | PostgreSQL | State machine, consistency |
| IAM | PostgreSQL + Azure AD | User data + identity |
| Audit | CosmosDB | High write throughput, append-only |

---

## 1. Payment Initiation Service Database

### Database Name: `payment_initiation_db`

```sql
-- =====================================================
-- PAYMENTS TABLE
-- =====================================================
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    reference VARCHAR(200),
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('EFT', 'RTC', 'RTGS', 'DEBIT_ORDER')),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    priority VARCHAR(10) DEFAULT 'NORMAL' CHECK (priority IN ('NORMAL', 'HIGH', 'URGENT')),
    initiated_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    CONSTRAINT chk_different_accounts CHECK (source_account != destination_account)
);

-- Indexes
CREATE INDEX idx_payments_source_account ON payments(source_account);
CREATE INDEX idx_payments_destination_account ON payments(destination_account);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);
CREATE INDEX idx_payments_initiated_by ON payments(initiated_by);
CREATE INDEX idx_payments_composite ON payments(source_account, created_at DESC);

-- =====================================================
-- PAYMENT STATUS HISTORY
-- =====================================================
CREATE TABLE payment_status_history (
    history_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    
    CONSTRAINT fk_payment_status_history FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_status_history_payment_id ON payment_status_history(payment_id);

-- =====================================================
-- DEBIT ORDER DETAILS (for DEBIT_ORDER payment type)
-- =====================================================
CREATE TABLE debit_order_details (
    debit_order_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    mandate_reference VARCHAR(100) NOT NULL,
    mandate_date DATE NOT NULL,
    max_amount DECIMAL(18,2),
    frequency VARCHAR(20) CHECK (frequency IN ('ONCE_OFF', 'MONTHLY', 'WEEKLY', 'DAILY')),
    debicheck_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_debit_order_payment FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

CREATE INDEX idx_debit_order_payment_id ON debit_order_details(payment_id);

-- =====================================================
-- TRIGGERS
-- =====================================================
-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_payments_updated_at 
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Auto-track status changes
CREATE OR REPLACE FUNCTION track_payment_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO payment_status_history (payment_id, from_status, to_status, reason)
        VALUES (NEW.payment_id, OLD.status, NEW.status, 'Status changed');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER track_payment_status 
    AFTER UPDATE OF status ON payments
    FOR EACH ROW EXECUTE FUNCTION track_payment_status_change();
```

---

## 2. Validation Service Database

### Database Name: `validation_db`

```sql
-- =====================================================
-- VALIDATION RULES
-- =====================================================
CREATE TABLE validation_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('LIMIT', 'STATUS', 'KYC', 'FICA', 'FRAUD', 'VELOCITY')),
    rule_description TEXT,
    rule_condition JSONB NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX idx_validation_rules_active ON validation_rules(active);
CREATE INDEX idx_validation_rules_priority ON validation_rules(priority);
CREATE INDEX idx_validation_rules_type ON validation_rules(rule_type);

-- Example rule condition (JSONB):
-- {
--   "field": "amount",
--   "operator": "<=",
--   "value": 50000,
--   "errorMessage": "Daily limit exceeded"
-- }

-- =====================================================
-- VALIDATION RESULTS
-- =====================================================
CREATE TABLE validation_results (
    validation_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    validation_status VARCHAR(20) NOT NULL CHECK (validation_status IN ('VALID', 'INVALID')),
    fraud_score DECIMAL(5,4) CHECK (fraud_score >= 0 AND fraud_score <= 1),
    risk_level VARCHAR(20) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    failed_rules JSONB,
    validated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validator_service VARCHAR(100)
);

CREATE INDEX idx_validation_results_payment_id ON validation_results(payment_id);
CREATE INDEX idx_validation_results_status ON validation_results(validation_status);
CREATE INDEX idx_validation_results_risk_level ON validation_results(risk_level);
CREATE INDEX idx_validation_results_validated_at ON validation_results(validated_at DESC);

-- =====================================================
-- VELOCITY TRACKING (for velocity checks)
-- =====================================================
CREATE TABLE velocity_tracking (
    tracking_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    transaction_count INTEGER NOT NULL DEFAULT 1,
    total_amount DECIMAL(18,2) NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    window_type VARCHAR(20) NOT NULL CHECK (window_type IN ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY'))
);

CREATE INDEX idx_velocity_account ON velocity_tracking(account_number);
CREATE INDEX idx_velocity_window ON velocity_tracking(window_end);

-- =====================================================
-- FRAUD DETECTION LOG
-- =====================================================
CREATE TABLE fraud_detection_log (
    log_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    fraud_score DECIMAL(5,4) NOT NULL,
    fraud_indicators JSONB,
    external_fraud_service_response JSONB,
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fraud_log_payment_id ON fraud_detection_log(payment_id);
CREATE INDEX idx_fraud_log_score ON fraud_detection_log(fraud_score DESC);
```

---

## 3. Account Service Database

### Database Name: `account_db`

```sql
-- =====================================================
-- ACCOUNTS
-- =====================================================
CREATE TABLE accounts (
    account_number VARCHAR(50) PRIMARY KEY,
    account_holder VARCHAR(200) NOT NULL,
    id_number VARCHAR(20) NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('CURRENT', 'SAVINGS', 'CREDIT', 'MONEY_MARKET')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED', 'DORMANT')),
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    overdraft_limit DECIMAL(18,2) DEFAULT 0.00,
    kyc_status VARCHAR(20) DEFAULT 'PENDING' CHECK (kyc_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    kyc_verified_at TIMESTAMP,
    fica_status VARCHAR(20) DEFAULT 'PENDING' CHECK (fica_status IN ('PENDING', 'COMPLIANT', 'NON_COMPLIANT')),
    fica_verified_at TIMESTAMP,
    opened_date DATE NOT NULL,
    closed_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_balance_non_negative CHECK (balance >= -overdraft_limit),
    CONSTRAINT chk_available_balance CHECK (available_balance <= balance + overdraft_limit)
);

CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_account_holder ON accounts(account_holder);
CREATE INDEX idx_accounts_id_number ON accounts(id_number);
CREATE INDEX idx_accounts_kyc_status ON accounts(kyc_status);
CREATE INDEX idx_accounts_fica_status ON accounts(fica_status);

-- =====================================================
-- ACCOUNT HOLDS (temporary reservations)
-- =====================================================
CREATE TABLE account_holds (
    hold_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    reference VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PLACED' CHECK (status IN ('PLACED', 'RELEASED', 'EXPIRED', 'CONSUMED')),
    placed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    released_at TIMESTAMP,
    released_by VARCHAR(100),
    release_reason TEXT,
    
    CONSTRAINT fk_hold_account FOREIGN KEY (account_number) 
        REFERENCES accounts(account_number) ON DELETE CASCADE
);

CREATE INDEX idx_holds_account_number ON account_holds(account_number);
CREATE INDEX idx_holds_status ON account_holds(status);
CREATE INDEX idx_holds_expires_at ON account_holds(expires_at);
CREATE INDEX idx_holds_reference ON account_holds(reference);

-- =====================================================
-- ACCOUNT BALANCE HISTORY (for auditing)
-- =====================================================
CREATE TABLE account_balance_history (
    history_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    old_balance DECIMAL(18,2) NOT NULL,
    new_balance DECIMAL(18,2) NOT NULL,
    change_amount DECIMAL(18,2) NOT NULL,
    change_type VARCHAR(20) NOT NULL CHECK (change_type IN ('DEBIT', 'CREDIT', 'HOLD', 'RELEASE')),
    reference VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    
    CONSTRAINT fk_balance_history_account FOREIGN KEY (account_number) 
        REFERENCES accounts(account_number) ON DELETE CASCADE
);

CREATE INDEX idx_balance_history_account ON account_balance_history(account_number);
CREATE INDEX idx_balance_history_changed_at ON account_balance_history(changed_at DESC);

-- =====================================================
-- TRIGGERS
-- =====================================================
-- Auto-expire holds
CREATE OR REPLACE FUNCTION auto_expire_holds()
RETURNS void AS $$
BEGIN
    UPDATE account_holds 
    SET status = 'EXPIRED'
    WHERE status = 'PLACED' 
      AND expires_at < CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- Schedule this function to run every minute via pg_cron or application scheduler

-- Track balance changes
CREATE OR REPLACE FUNCTION track_balance_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.balance != NEW.balance THEN
        INSERT INTO account_balance_history 
            (account_number, old_balance, new_balance, change_amount, change_type, reference)
        VALUES 
            (NEW.account_number, OLD.balance, NEW.balance, NEW.balance - OLD.balance, 
             CASE WHEN NEW.balance > OLD.balance THEN 'CREDIT' ELSE 'DEBIT' END, 'Balance update');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER track_balance 
    AFTER UPDATE OF balance ON accounts
    FOR EACH ROW EXECUTE FUNCTION track_balance_change();
```

---

## 4. Transaction Processing Service Database

### Database Name: `transaction_db`

```sql
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
    failure_reason TEXT
);

CREATE INDEX idx_transactions_payment_id ON transactions(payment_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_debit_account ON transactions(debit_account);
CREATE INDEX idx_transactions_credit_account ON transactions(credit_account);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX idx_transactions_clearing_reference ON transactions(clearing_reference);

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
    
    CONSTRAINT fk_transaction_events FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE INDEX idx_transaction_events_transaction_id ON transaction_events(transaction_id);
CREATE INDEX idx_transaction_events_sequence ON transaction_events(event_sequence);
CREATE INDEX idx_transaction_events_type ON transaction_events(event_type);
CREATE INDEX idx_transaction_events_occurred_at ON transaction_events(occurred_at DESC);

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
    
    CONSTRAINT fk_ledger_transaction FOREIGN KEY (transaction_id) 
        REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE INDEX idx_ledger_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_account_number ON ledger_entries(account_number);
CREATE INDEX idx_ledger_entry_date ON ledger_entries(entry_date DESC);
CREATE INDEX idx_ledger_composite ON ledger_entries(account_number, entry_date DESC);

-- =====================================================
-- CONSTRAINTS
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
```

---

## 5. Clearing Adapter Service Database

### Database Name: `clearing_db` (shared by all clearing adapters)

```sql
-- =====================================================
-- CLEARING SUBMISSIONS
-- =====================================================
CREATE TABLE clearing_submissions (
    submission_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    clearing_system VARCHAR(50) NOT NULL CHECK (clearing_system IN ('SAMOS', 'BANKSERV_ACH', 'BANKSERV_RTC', 'SASWITCH')),
    clearing_reference VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'SUBMITTED', 'ACKNOWLEDGED', 'PROCESSING', 'COMPLETED', 'FAILED', 'TIMEOUT')),
    request_message TEXT NOT NULL,
    request_format VARCHAR(20) CHECK (request_format IN ('ISO20022', 'ISO8583', 'PROPRIETARY')),
    response_message TEXT,
    response_code VARCHAR(10),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    last_retry_at TIMESTAMP
);

CREATE INDEX idx_clearing_transaction_id ON clearing_submissions(transaction_id);
CREATE INDEX idx_clearing_system ON clearing_submissions(clearing_system);
CREATE INDEX idx_clearing_status ON clearing_submissions(status);
CREATE INDEX idx_clearing_reference ON clearing_submissions(clearing_reference);
CREATE INDEX idx_clearing_submitted_at ON clearing_submissions(submitted_at DESC);

-- =====================================================
-- CLEARING BATCHES (for ACH batch processing)
-- =====================================================
CREATE TABLE clearing_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    clearing_system VARCHAR(50) NOT NULL,
    batch_date DATE NOT NULL,
    batch_cutoff_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' 
        CHECK (status IN ('OPEN', 'CLOSED', 'SUBMITTED', 'ACKNOWLEDGED', 'COMPLETED', 'FAILED')),
    transaction_count INTEGER NOT NULL DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    batch_file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    submitted_at TIMESTAMP,
    acknowledged_at TIMESTAMP
);

CREATE INDEX idx_batch_system ON clearing_batches(clearing_system);
CREATE INDEX idx_batch_date ON clearing_batches(batch_date DESC);
CREATE INDEX idx_batch_status ON clearing_batches(status);

-- =====================================================
-- BATCH SUBMISSIONS (link transactions to batches)
-- =====================================================
CREATE TABLE batch_submissions (
    batch_submission_id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    submission_id VARCHAR(50) NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_batch_submissions_batch FOREIGN KEY (batch_id) 
        REFERENCES clearing_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT fk_batch_submissions_submission FOREIGN KEY (submission_id) 
        REFERENCES clearing_submissions(submission_id) ON DELETE CASCADE,
    CONSTRAINT uk_batch_submission UNIQUE (batch_id, submission_id)
);

CREATE INDEX idx_batch_submissions_batch_id ON batch_submissions(batch_id);
CREATE INDEX idx_batch_submissions_submission_id ON batch_submissions(submission_id);

-- =====================================================
-- CLEARING RESPONSES (for tracking responses from clearing systems)
-- =====================================================
CREATE TABLE clearing_responses (
    response_id BIGSERIAL PRIMARY KEY,
    submission_id VARCHAR(50) NOT NULL,
    response_type VARCHAR(50) NOT NULL CHECK (response_type IN ('ACKNOWLEDGMENT', 'COMPLETION', 'REJECTION', 'TIMEOUT')),
    response_code VARCHAR(10),
    response_message TEXT,
    response_timestamp TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_clearing_responses_submission FOREIGN KEY (submission_id) 
        REFERENCES clearing_submissions(submission_id) ON DELETE CASCADE
);

CREATE INDEX idx_responses_submission_id ON clearing_responses(submission_id);
CREATE INDEX idx_responses_type ON clearing_responses(response_type);
CREATE INDEX idx_responses_timestamp ON clearing_responses(response_timestamp DESC);
```

---

## 6. Settlement Service Database

### Database Name: `settlement_db`

```sql
-- =====================================================
-- SETTLEMENT BATCHES
-- =====================================================
CREATE TABLE settlement_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    batch_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'CALCULATING', 'CALCULATED', 'SUBMITTED', 'COMPLETED', 'FAILED')),
    transaction_count INTEGER NOT NULL DEFAULT 0,
    total_debit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_credit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    net_position DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    settlement_file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    calculated_at TIMESTAMP,
    submitted_at TIMESTAMP,
    finalized_at TIMESTAMP,
    settlement_reference VARCHAR(100)
);

CREATE INDEX idx_settlement_batch_date ON settlement_batches(batch_date DESC);
CREATE INDEX idx_settlement_clearing_system ON settlement_batches(clearing_system);
CREATE INDEX idx_settlement_status ON settlement_batches(status);

-- =====================================================
-- SETTLEMENT TRANSACTIONS
-- =====================================================
CREATE TABLE settlement_transactions (
    settlement_txn_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    settlement_status VARCHAR(20) NOT NULL DEFAULT 'INCLUDED' 
        CHECK (settlement_status IN ('INCLUDED', 'SETTLED', 'FAILED', 'EXCLUDED')),
    included_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP,
    exclusion_reason TEXT,
    
    CONSTRAINT fk_settlement_txn_batch FOREIGN KEY (batch_id) 
        REFERENCES settlement_batches(batch_id) ON DELETE CASCADE
);

CREATE INDEX idx_settlement_txn_batch_id ON settlement_transactions(batch_id);
CREATE INDEX idx_settlement_txn_transaction_id ON settlement_transactions(transaction_id);
CREATE INDEX idx_settlement_txn_status ON settlement_transactions(settlement_status);

-- =====================================================
-- SETTLEMENT POSITIONS (per account)
-- =====================================================
CREATE TABLE settlement_positions (
    position_id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    debit_count INTEGER NOT NULL DEFAULT 0,
    credit_count INTEGER NOT NULL DEFAULT 0,
    total_debit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_credit DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    net_position DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_position_batch FOREIGN KEY (batch_id) 
        REFERENCES settlement_batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT uk_position UNIQUE (batch_id, account_number)
);

CREATE INDEX idx_position_batch_id ON settlement_positions(batch_id);
CREATE INDEX idx_position_account_number ON settlement_positions(account_number);

-- =====================================================
-- SETTLEMENT FILES (generated files)
-- =====================================================
CREATE TABLE settlement_files (
    file_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    file_type VARCHAR(50) NOT NULL CHECK (file_type IN ('POSITION', 'TRANSACTION', 'SUMMARY')),
    file_format VARCHAR(20) NOT NULL CHECK (file_format IN ('CSV', 'XML', 'EXCEL', 'PDF')),
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by VARCHAR(100),
    
    CONSTRAINT fk_settlement_file_batch FOREIGN KEY (batch_id) 
        REFERENCES settlement_batches(batch_id) ON DELETE CASCADE
);

CREATE INDEX idx_file_batch_id ON settlement_files(batch_id);
CREATE INDEX idx_file_generated_at ON settlement_files(generated_at DESC);
```

---

## 7. Reconciliation Service Database

### Database Name: `reconciliation_db`

```sql
-- =====================================================
-- RECONCILIATION RUNS
-- =====================================================
CREATE TABLE reconciliation_runs (
    reconciliation_id VARCHAR(50) PRIMARY KEY,
    run_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED' 
        CHECK (status IN ('INITIATED', 'RUNNING', 'COMPLETED', 'FAILED', 'PARTIALLY_COMPLETED')),
    total_transactions INTEGER NOT NULL DEFAULT 0,
    matched_count INTEGER NOT NULL DEFAULT 0,
    unmatched_count INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    run_by VARCHAR(100)
);

CREATE INDEX idx_recon_run_date ON reconciliation_runs(run_date DESC);
CREATE INDEX idx_recon_clearing_system ON reconciliation_runs(clearing_system);
CREATE INDEX idx_recon_status ON reconciliation_runs(status);

-- =====================================================
-- RECONCILIATION MATCHES
-- =====================================================
CREATE TABLE reconciliation_matches (
    match_id VARCHAR(50) PRIMARY KEY,
    reconciliation_id VARCHAR(50) NOT NULL,
    internal_transaction_id VARCHAR(50) NOT NULL,
    external_reference VARCHAR(100) NOT NULL,
    match_status VARCHAR(20) NOT NULL DEFAULT 'MATCHED' 
        CHECK (match_status IN ('MATCHED', 'AMOUNT_MISMATCH', 'DATE_MISMATCH', 'STATUS_MISMATCH')),
    internal_amount DECIMAL(18,2),
    external_amount DECIMAL(18,2),
    amount_difference DECIMAL(18,2),
    matched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_match_recon FOREIGN KEY (reconciliation_id) 
        REFERENCES reconciliation_runs(reconciliation_id) ON DELETE CASCADE
);

CREATE INDEX idx_match_recon_id ON reconciliation_matches(reconciliation_id);
CREATE INDEX idx_match_internal_txn ON reconciliation_matches(internal_transaction_id);
CREATE INDEX idx_match_status ON reconciliation_matches(match_status);

-- =====================================================
-- RECONCILIATION EXCEPTIONS
-- =====================================================
CREATE TABLE reconciliation_exceptions (
    exception_id VARCHAR(50) PRIMARY KEY,
    reconciliation_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50),
    clearing_reference VARCHAR(100),
    exception_type VARCHAR(50) NOT NULL 
        CHECK (exception_type IN ('MISSING_INTERNAL', 'MISSING_EXTERNAL', 'AMOUNT_MISMATCH', 'DUPLICATE', 'STATUS_CONFLICT')),
    exception_reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'INVESTIGATING', 'RESOLVED', 'ESCALATED', 'CLOSED')),
    severity VARCHAR(20) DEFAULT 'MEDIUM' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    assigned_to VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    
    CONSTRAINT fk_exception_recon FOREIGN KEY (reconciliation_id) 
        REFERENCES reconciliation_runs(reconciliation_id) ON DELETE CASCADE
);

CREATE INDEX idx_exception_recon_id ON reconciliation_exceptions(reconciliation_id);
CREATE INDEX idx_exception_status ON reconciliation_exceptions(status);
CREATE INDEX idx_exception_severity ON reconciliation_exceptions(severity);
CREATE INDEX idx_exception_assigned_to ON reconciliation_exceptions(assigned_to);
CREATE INDEX idx_exception_created_at ON reconciliation_exceptions(created_at DESC);

-- =====================================================
-- EXTERNAL CLEARING DATA (imported from clearing systems)
-- =====================================================
CREATE TABLE external_clearing_data (
    external_id BIGSERIAL PRIMARY KEY,
    clearing_system VARCHAR(50) NOT NULL,
    clearing_reference VARCHAR(100) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(20),
    raw_data JSONB NOT NULL,
    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reconciled BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT uk_external_clearing UNIQUE (clearing_system, clearing_reference)
);

CREATE INDEX idx_external_clearing_system ON external_clearing_data(clearing_system);
CREATE INDEX idx_external_reference ON external_clearing_data(clearing_reference);
CREATE INDEX idx_external_reconciled ON external_clearing_data(reconciled);
CREATE INDEX idx_external_transaction_date ON external_clearing_data(transaction_date DESC);
```

---

## 8. Notification Service Database

### Database Name: `notification_db`

```sql
-- =====================================================
-- NOTIFICATIONS
-- =====================================================
CREATE TABLE notifications (
    notification_id VARCHAR(50) PRIMARY KEY,
    recipient_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'PUSH', 'WEBHOOK')),
    template_id VARCHAR(50) NOT NULL,
    subject VARCHAR(200),
    message_content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED' 
        CHECK (status IN ('QUEUED', 'SENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),
    priority VARCHAR(10) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    external_message_id VARCHAR(100)
);

CREATE INDEX idx_notification_recipient ON notifications(recipient_id);
CREATE INDEX idx_notification_channel ON notifications(channel);
CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notification_priority ON notifications(priority, status);

-- =====================================================
-- NOTIFICATION TEMPLATES
-- =====================================================
CREATE TABLE notification_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'PUSH', 'WEBHOOK')),
    subject_template VARCHAR(200),
    body_template TEXT NOT NULL,
    template_variables JSONB,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT uk_template_name UNIQUE (template_name, channel)
);

CREATE INDEX idx_template_channel ON notification_templates(channel);
CREATE INDEX idx_template_active ON notification_templates(active);

-- Example template:
-- subject_template: "Payment Confirmation"
-- body_template: "Your payment of R{{amount}} to {{recipient}} was successful. Reference: {{paymentId}}"
-- template_variables: ["amount", "recipient", "paymentId"]

-- =====================================================
-- NOTIFICATION DELIVERY LOG
-- =====================================================
CREATE TABLE notification_delivery_log (
    log_id BIGSERIAL PRIMARY KEY,
    notification_id VARCHAR(50) NOT NULL,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),
    response_code VARCHAR(10),
    response_message TEXT,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_delivery_log_notification FOREIGN KEY (notification_id) 
        REFERENCES notifications(notification_id) ON DELETE CASCADE
);

CREATE INDEX idx_delivery_log_notification_id ON notification_delivery_log(notification_id);
CREATE INDEX idx_delivery_log_attempted_at ON notification_delivery_log(attempted_at DESC);

-- =====================================================
-- RECIPIENT PREFERENCES
-- =====================================================
CREATE TABLE recipient_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    recipient_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notification_types JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_recipient_channel UNIQUE (recipient_id, channel)
);

CREATE INDEX idx_preferences_recipient ON recipient_preferences(recipient_id);
```

---

## 9. Saga Orchestrator Service Database

### Database Name: `saga_db`

```sql
-- =====================================================
-- SAGAS
-- =====================================================
CREATE TABLE sagas (
    saga_id VARCHAR(50) PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL CHECK (saga_type IN ('PAYMENT_SAGA', 'SETTLEMENT_SAGA', 'RECONCILIATION_SAGA')),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED' 
        CHECK (status IN ('INITIATED', 'RUNNING', 'COMPLETED', 'COMPENSATING', 'COMPENSATED', 'FAILED')),
    current_step VARCHAR(50),
    current_step_number INTEGER DEFAULT 0,
    total_steps INTEGER NOT NULL,
    payload JSONB NOT NULL,
    correlation_id VARCHAR(50),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    compensated_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    timeout_minutes INTEGER DEFAULT 30
);

CREATE INDEX idx_saga_status ON sagas(status);
CREATE INDEX idx_saga_type ON sagas(saga_type);
CREATE INDEX idx_saga_started_at ON sagas(started_at DESC);
CREATE INDEX idx_saga_correlation_id ON sagas(correlation_id);

-- =====================================================
-- SAGA STEPS
-- =====================================================
CREATE TABLE saga_steps (
    step_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_number INTEGER NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_service VARCHAR(100) NOT NULL,
    step_action VARCHAR(200) NOT NULL,
    compensation_action VARCHAR(200),
    step_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (step_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'COMPENSATED', 'SKIPPED')),
    request_data JSONB,
    response_data JSONB,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    compensated_at TIMESTAMP,
    failed_at TIMESTAMP,
    
    CONSTRAINT fk_saga_step FOREIGN KEY (saga_id) 
        REFERENCES sagas(saga_id) ON DELETE CASCADE,
    CONSTRAINT uk_saga_step_number UNIQUE (saga_id, step_number)
);

CREATE INDEX idx_saga_step_saga_id ON saga_steps(saga_id);
CREATE INDEX idx_saga_step_status ON saga_steps(step_status);
CREATE INDEX idx_saga_step_number ON saga_steps(saga_id, step_number);

-- =====================================================
-- SAGA EVENTS (event log)
-- =====================================================
CREATE TABLE saga_events (
    event_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_saga_event FOREIGN KEY (saga_id) 
        REFERENCES sagas(saga_id) ON DELETE CASCADE
);

CREATE INDEX idx_saga_event_saga_id ON saga_events(saga_id);
CREATE INDEX idx_saga_event_occurred_at ON saga_events(occurred_at DESC);

-- =====================================================
-- SAGA COMPENSATION LOG
-- =====================================================
CREATE TABLE saga_compensation_log (
    compensation_id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_id VARCHAR(50) NOT NULL,
    compensation_status VARCHAR(20) NOT NULL 
        CHECK (compensation_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    compensation_request JSONB,
    compensation_response JSONB,
    retry_count INTEGER DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    error_message TEXT,
    
    CONSTRAINT fk_compensation_saga FOREIGN KEY (saga_id) 
        REFERENCES sagas(saga_id) ON DELETE CASCADE,
    CONSTRAINT fk_compensation_step FOREIGN KEY (step_id) 
        REFERENCES saga_steps(step_id) ON DELETE CASCADE
);

CREATE INDEX idx_compensation_saga_id ON saga_compensation_log(saga_id);
CREATE INDEX idx_compensation_step_id ON saga_compensation_log(step_id);
CREATE INDEX idx_compensation_status ON saga_compensation_log(compensation_status);
```

---

## 10. IAM Service Database

### Database Name: `iam_db`

```sql
-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED')),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(100),
    azure_ad_object_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_azure_ad_object_id ON users(azure_ad_object_id);

-- =====================================================
-- ROLES
-- =====================================================
CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    role_description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_name ON roles(role_name);

-- Seed roles
INSERT INTO roles (role_id, role_name, role_description) VALUES
    ('ROLE-001', 'USER', 'Standard user role'),
    ('ROLE-002', 'ADMIN', 'Administrator role'),
    ('ROLE-003', 'OPERATOR', 'Operator role for exception handling'),
    ('ROLE-004', 'AUDITOR', 'Read-only auditor role');

-- =====================================================
-- USER ROLES
-- =====================================================
CREATE TABLE user_roles (
    user_role_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    role_id VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),
    
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) 
        REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- =====================================================
-- PERMISSIONS
-- =====================================================
CREATE TABLE permissions (
    permission_id VARCHAR(50) PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    permission_description TEXT,
    
    CONSTRAINT uk_permission UNIQUE (resource, action)
);

CREATE INDEX idx_permissions_resource ON permissions(resource);

-- =====================================================
-- ROLE PERMISSIONS
-- =====================================================
CREATE TABLE role_permissions (
    role_permission_id BIGSERIAL PRIMARY KEY,
    role_id VARCHAR(50) NOT NULL,
    permission_id VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) 
        REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) 
        REFERENCES permissions(permission_id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- =====================================================
-- REFRESH TOKENS
-- =====================================================
CREATE TABLE refresh_tokens (
    token_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revoked_reason VARCHAR(200),
    
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);

-- =====================================================
-- AUDIT LOG (authentication events)
-- =====================================================
CREATE TABLE auth_audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50),
    event_type VARCHAR(50) NOT NULL 
        CHECK (event_type IN ('LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT', 'PASSWORD_CHANGE', 'MFA_ENABLED', 'MFA_DISABLED', 'ACCOUNT_LOCKED')),
    ip_address VARCHAR(45),
    user_agent TEXT,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details JSONB
);

CREATE INDEX idx_auth_audit_user_id ON auth_audit_log(user_id);
CREATE INDEX idx_auth_audit_event_type ON auth_audit_log(event_type);
CREATE INDEX idx_auth_audit_occurred_at ON auth_audit_log(occurred_at DESC);
```

---

## 11. Audit Service Database (CosmosDB)

### Database Name: `audit_db`

**CosmosDB Collection**: `audit_events`

```json
{
  "id": "unique-event-id",
  "partitionKey": "2025-10-11",  // Date-based partitioning
  "eventType": "API_CALL",
  "eventCategory": "SECURITY" | "BUSINESS" | "TECHNICAL",
  "timestamp": "2025-10-11T10:30:00Z",
  "userId": "user-123",
  "sessionId": "session-uuid",
  "service": "PaymentInitiationService",
  "action": "POST /api/v1/payments",
  "resource": "payments",
  "httpMethod": "POST",
  "httpStatusCode": 201,
  "requestData": {
    "sourceAccount": "1234567890",
    "destinationAccount": "0987654321",
    "amount": 1000.00
    // Sensitive data masked
  },
  "responseData": {
    "paymentId": "PAY-2025-XXXXXX",
    "status": "INITIATED"
  },
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "correlationId": "corr-uuid",
  "duration_ms": 150,
  "errors": [],
  "tags": ["payment", "initiation"],
  "ttl": 220752000  // 7 years in seconds
}
```

**CosmosDB Indexing Policy**:
```json
{
  "indexingMode": "consistent",
  "automatic": true,
  "includedPaths": [
    { "path": "/eventType/?" },
    { "path": "/eventCategory/?" },
    { "path": "/timestamp/?" },
    { "path": "/userId/?" },
    { "path": "/service/?" },
    { "path": "/correlationId/?" }
  ],
  "excludedPaths": [
    { "path": "/requestData/*" },
    { "path": "/responseData/*" }
  ]
}
```

---

## Database Sizing Estimates

| Database | Size (Year 1) | Growth Rate | Backup Strategy |
|----------|---------------|-------------|-----------------|
| payment_initiation_db | 500 GB | 40 GB/month | Daily full + hourly incremental |
| account_db | 100 GB | 5 GB/month | Daily full + hourly incremental |
| transaction_db | 1 TB | 80 GB/month | Daily full + hourly incremental |
| clearing_db | 800 GB | 60 GB/month | Daily full + hourly incremental |
| settlement_db | 300 GB | 25 GB/month | Daily full + hourly incremental |
| reconciliation_db | 200 GB | 15 GB/month | Daily full + hourly incremental |
| saga_db | 150 GB | 10 GB/month | Daily full + hourly incremental |
| audit_db (CosmosDB) | 2 TB | 150 GB/month | Continuous (built-in) |

---

## Database Maintenance

### Automated Tasks
1. **Daily Backups**: All PostgreSQL databases
2. **Index Maintenance**: Weekly REINDEX for heavy-write tables
3. **Statistics Update**: Daily ANALYZE for query optimizer
4. **Partition Management**: Monthly partitioning for time-series tables
5. **Archival**: Quarterly move old data to cold storage

### Monitoring Metrics
- Connection pool usage
- Query performance (slow query log)
- Database size and growth
- Lock contention
- Replication lag (for read replicas)

---

**Next**: See `06-SOUTH-AFRICA-CLEARING.md` for clearing system integration
**Next**: See `07-AZURE-INFRASTRUCTURE.md` for infrastructure design
