-- =====================================================
-- PAYMENT INITIATION SERVICE DATABASE
-- =====================================================
-- Based on existing V2, V17, V18 migrations
-- Maintains compatibility with existing Java entities

-- =====================================================
-- PAYMENTS TABLE (Enhanced from V2 + V18)
-- =====================================================
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    
    -- MULTI-TENANCY (CRITICAL)
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    -- PAYMENT DETAILS
    idempotency_key VARCHAR(100) NOT NULL,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    reference VARCHAR(200),
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('EFT', 'RTC', 'RTGS', 'DEBIT_ORDER', 'CARD', 'WALLET')),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED' CHECK (status IN ('INITIATED', 'VALIDATING', 'VALIDATED', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED')),
    priority VARCHAR(10) DEFAULT 'NORMAL' CHECK (priority IN ('NORMAL', 'HIGH', 'URGENT')),
    
    -- AUDIT FIELDS
    initiated_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- ADDITIONAL TIMESTAMP FIELDS (for Java entity compatibility)
    initiated_at TIMESTAMP,
    validated_at TIMESTAMP,
    submitted_to_clearing_at TIMESTAMP,
    cleared_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(500),
    
    -- VERSION FIELD (for optimistic locking)
    version BIGINT NOT NULL DEFAULT 0,
    
    -- ISO 20022 CORRELATION FIELDS (from V18)
    pain001_message_id VARCHAR(35),
    pain001_correlation_id VARCHAR(255),
    iso20022_compliant BOOLEAN DEFAULT FALSE,
    message_format VARCHAR(10) DEFAULT 'REST' CHECK (message_format IN ('REST', 'XML', 'JSON')),
    original_message_id VARCHAR(35),
    pain002_message_id VARCHAR(35),
    pain002_correlation_id VARCHAR(255),
    iso20022_status_code VARCHAR(10) CHECK (iso20022_status_code IN ('ACCP', 'RJCT', 'PDNG', 'ACSC', 'CANC', 'PART') OR iso20022_status_code IS NULL),
    status_reason_code VARCHAR(10),
    status_additional_info VARCHAR(500),
    
    -- CONSTRAINTS
    CONSTRAINT chk_different_accounts CHECK (source_account != destination_account),
    CONSTRAINT uk_idempotency_tenant UNIQUE (tenant_id, idempotency_key)
);

-- PERFORMANCE INDEXES (Critical for multi-tenant queries)
CREATE INDEX idx_payments_tenant_id ON payments(tenant_id);
CREATE INDEX idx_payments_tenant_bu ON payments(tenant_id, business_unit_id);
CREATE INDEX idx_payments_tenant_status ON payments(tenant_id, status);
CREATE INDEX idx_payments_source_account ON payments(tenant_id, source_account);
CREATE INDEX idx_payments_destination_account ON payments(tenant_id, destination_account);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);
CREATE INDEX idx_payments_initiated_by ON payments(initiated_by);
CREATE INDEX idx_payments_composite ON payments(tenant_id, source_account, created_at DESC);
CREATE INDEX idx_payments_payment_type ON payments(payment_type);
CREATE INDEX idx_payments_priority ON payments(priority);
CREATE INDEX idx_payments_initiated_at ON payments(initiated_at);
CREATE INDEX idx_payments_version ON payments(version);

-- ISO 20022 INDEXES (from V18)
CREATE INDEX idx_payments_pain001_message_id ON payments(pain001_message_id);
CREATE INDEX idx_payments_pain001_correlation_id ON payments(pain001_correlation_id);
CREATE INDEX idx_payments_iso20022_compliant ON payments(iso20022_compliant);
CREATE INDEX idx_payments_message_format ON payments(message_format);
CREATE INDEX idx_payments_original_message_id ON payments(original_message_id);
CREATE INDEX idx_payments_pain002_message_id ON payments(pain002_message_id);
CREATE INDEX idx_payments_pain002_correlation_id ON payments(pain002_correlation_id);
CREATE INDEX idx_payments_iso20022_status_code ON payments(iso20022_status_code);
CREATE INDEX idx_payments_status_reason_code ON payments(status_reason_code);

-- Composite indexes for common query patterns
CREATE INDEX idx_payments_tenant_iso20022_compliant ON payments(tenant_id, iso20022_compliant);
CREATE INDEX idx_payments_tenant_message_format ON payments(tenant_id, message_format);
CREATE INDEX idx_payments_tenant_iso20022_status ON payments(tenant_id, iso20022_status_code);
CREATE INDEX idx_payments_pain001_pain002_correlation ON payments(pain001_message_id, pain002_message_id);

-- =====================================================
-- PAYMENT STATUS HISTORY (from V2)
-- =====================================================
CREATE TABLE payment_status_history (
    history_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_payment_status_history FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Indexes for status history
CREATE INDEX idx_payment_status_history_payment_id ON payment_status_history(payment_id);
CREATE INDEX idx_payment_status_history_tenant_id ON payment_status_history(tenant_id);
CREATE INDEX idx_payment_status_history_tenant_bu ON payment_status_history(tenant_id, business_unit_id);
CREATE INDEX idx_payment_status_history_changed_at ON payment_status_history(changed_at DESC);

-- =====================================================
-- DEBIT ORDER DETAILS (from V2)
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
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_debit_order_payment FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Indexes for debit order details
CREATE INDEX idx_debit_order_payment_id ON debit_order_details(payment_id);
CREATE INDEX idx_debit_order_tenant_id ON debit_order_details(tenant_id);
CREATE INDEX idx_debit_order_tenant_bu ON debit_order_details(tenant_id, business_unit_id);
CREATE INDEX idx_debit_order_mandate_reference ON debit_order_details(mandate_reference);

-- =====================================================
-- PAYMENT VALIDATION RESULTS (from V2)
-- =====================================================
CREATE TABLE payment_validation_results (
    validation_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    validation_status VARCHAR(20) NOT NULL CHECK (validation_status IN ('VALID', 'INVALID', 'PENDING')),
    validation_rules JSONB NOT NULL,
    failed_rules JSONB,
    validation_score DECIMAL(5,4) CHECK (validation_score >= 0 AND validation_score <= 1),
    validated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validator_service VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_payment_validation FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Indexes for validation results
CREATE INDEX idx_payment_validation_payment_id ON payment_validation_results(payment_id);
CREATE INDEX idx_payment_validation_tenant_id ON payment_validation_results(tenant_id);
CREATE INDEX idx_payment_validation_tenant_bu ON payment_validation_results(tenant_id, business_unit_id);
CREATE INDEX idx_payment_validation_status ON payment_validation_results(validation_status);
CREATE INDEX idx_payment_validation_validated_at ON payment_validation_results(validated_at DESC);

-- =====================================================
-- PAYMENT FEES (from V2)
-- =====================================================
CREATE TABLE payment_fees (
    fee_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    fee_type VARCHAR(50) NOT NULL CHECK (fee_type IN ('PROCESSING', 'CLEARING', 'SETTLEMENT', 'CURRENCY_CONVERSION', 'PRIORITY')),
    fee_amount DECIMAL(18,2) NOT NULL CHECK (fee_amount >= 0),
    fee_currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    fee_description TEXT,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_payment_fee FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Indexes for payment fees
CREATE INDEX idx_payment_fees_payment_id ON payment_fees(payment_id);
CREATE INDEX idx_payment_fees_tenant_id ON payment_fees(tenant_id);
CREATE INDEX idx_payment_fees_tenant_bu ON payment_fees(tenant_id, business_unit_id);
CREATE INDEX idx_payment_fees_type ON payment_fees(fee_type);

-- =====================================================
-- PAYMENT NOTIFICATIONS (from V2)
-- =====================================================
CREATE TABLE payment_notifications (
    notification_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (notification_type IN ('INITIATED', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    recipient_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'PUSH', 'WEBHOOK')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED')),
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failure_reason TEXT,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_payment_notification FOREIGN KEY (payment_id) 
        REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Indexes for payment notifications
CREATE INDEX idx_payment_notifications_payment_id ON payment_notifications(payment_id);
CREATE INDEX idx_payment_notifications_tenant_id ON payment_notifications(tenant_id);
CREATE INDEX idx_payment_notifications_tenant_bu ON payment_notifications(tenant_id, business_unit_id);
CREATE INDEX idx_payment_notifications_type ON payment_notifications(notification_type);
CREATE INDEX idx_payment_notifications_status ON payment_notifications(status);

-- =====================================================
-- PAIN.001 MESSAGES (from V17)
-- =====================================================
CREATE TABLE pain001_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id VARCHAR(35) UNIQUE NOT NULL,  -- ISO 20022 MsgId
    creation_date_time TIMESTAMP NOT NULL,   -- ISO 20022 CreDtTm
    number_of_transactions INTEGER NOT NULL, -- ISO 20022 NbOfTxs
    control_sum DECIMAL(19,2) NOT NULL,     -- ISO 20022 CtrlSum
    initiating_party_name VARCHAR(140),      -- ISO 20022 InitgPty.Nm
    initiating_party_id VARCHAR(35),        -- ISO 20022 InitgPty.Id
    message_format VARCHAR(10) NOT NULL,     -- 'XML' or 'JSON'
    raw_message TEXT NOT NULL,               -- Full pain.001 message
    parsed_message JSONB,                   -- Parsed message structure
    validation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- 'PENDING', 'VALID', 'INVALID'
    validation_errors JSONB,                -- Validation error details
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain001_message_format CHECK (message_format IN ('XML', 'JSON')),
    CONSTRAINT chk_pain001_validation_status CHECK (validation_status IN ('PENDING', 'VALID', 'INVALID')),
    CONSTRAINT chk_pain001_control_sum CHECK (control_sum >= 0),
    CONSTRAINT chk_pain001_number_of_transactions CHECK (number_of_transactions > 0)
);

-- Indexes for pain001 messages
CREATE INDEX idx_pain001_messages_tenant_id ON pain001_messages(tenant_id);
CREATE INDEX idx_pain001_messages_tenant_bu ON pain001_messages(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_messages_status ON pain001_messages(validation_status);
CREATE INDEX idx_pain001_messages_created_at ON pain001_messages(created_at DESC);
CREATE INDEX idx_pain001_messages_message_id ON pain001_messages(message_id);

-- =====================================================
-- PAIN.001 PAYMENT INFORMATION (from V17)
-- =====================================================
CREATE TABLE pain001_payment_information (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    payment_information_id VARCHAR(35) NOT NULL, -- ISO 20022 PmtInfId
    payment_method VARCHAR(10) NOT NULL,         -- ISO 20022 PmtMtd
    batch_booking BOOLEAN NOT NULL,              -- ISO 20022 BtchBookg
    number_of_transactions INTEGER NOT NULL,    -- ISO 20022 NbOfTxs
    control_sum DECIMAL(19,2) NOT NULL,         -- ISO 20022 CtrlSum
    required_execution_date DATE NOT NULL,       -- ISO 20022 ReqdExctnDt
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain001_payment_method CHECK (payment_method IN ('TRF', 'TRA', 'CHK', 'DD', 'TEL', 'CHQ')),
    CONSTRAINT chk_pain001_payment_control_sum CHECK (control_sum >= 0),
    CONSTRAINT chk_pain001_payment_number_of_transactions CHECK (number_of_transactions > 0)
);

-- Indexes for pain001 payment information
CREATE INDEX idx_pain001_payment_info_tenant_id ON pain001_payment_information(tenant_id);
CREATE INDEX idx_pain001_payment_info_tenant_bu ON pain001_payment_information(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_payment_info_message_id ON pain001_payment_information(pain001_message_id);
CREATE INDEX idx_pain001_payment_info_payment_method ON pain001_payment_information(payment_method);

-- =====================================================
-- PAIN.001 DEBTOR INFORMATION (from V17)
-- =====================================================
CREATE TABLE pain001_debtor_information (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    debtor_name VARCHAR(140),                    -- ISO 20022 Dbtr.Nm
    debtor_id VARCHAR(35),                      -- ISO 20022 Dbtr.Id
    debtor_account VARCHAR(35),                 -- ISO 20022 DbtrAcct.Id
    debtor_agent_bic VARCHAR(11),              -- ISO 20022 DbtrAgt.BICFI
    debtor_agent_name VARCHAR(140),            -- ISO 20022 DbtrAgt.Nm
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for pain001 debtor information
CREATE INDEX idx_pain001_debtor_tenant_id ON pain001_debtor_information(tenant_id);
CREATE INDEX idx_pain001_debtor_tenant_bu ON pain001_debtor_information(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_debtor_message_id ON pain001_debtor_information(pain001_message_id);

-- =====================================================
-- PAIN.001 CREDITOR INFORMATION (from V17)
-- =====================================================
CREATE TABLE pain001_creditor_information (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    creditor_name VARCHAR(140),                 -- ISO 20022 Cdtr.Nm
    creditor_id VARCHAR(35),                   -- ISO 20022 Cdtr.Id
    creditor_account VARCHAR(35),              -- ISO 20022 CdtrAcct.Id
    creditor_agent_bic VARCHAR(11),           -- ISO 20022 CdtrAgt.BICFI
    creditor_agent_name VARCHAR(140),          -- ISO 20022 CdtrAgt.Nm
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for pain001 creditor information
CREATE INDEX idx_pain001_creditor_tenant_id ON pain001_creditor_information(tenant_id);
CREATE INDEX idx_pain001_creditor_tenant_bu ON pain001_creditor_information(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_creditor_message_id ON pain001_creditor_information(pain001_message_id);

-- =====================================================
-- PAIN.001 CREDIT TRANSFER TRANSACTIONS (from V17)
-- =====================================================
CREATE TABLE pain001_credit_transfer_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    payment_information_id UUID NOT NULL REFERENCES pain001_payment_information(id) ON DELETE CASCADE,
    transaction_id VARCHAR(35) NOT NULL,        -- ISO 20022 TxId
    end_to_end_id VARCHAR(35) NOT NULL,         -- ISO 20022 EndToEndId
    instruction_id VARCHAR(35),                 -- ISO 20022 InstrId
    transaction_amount DECIMAL(19,2) NOT NULL, -- ISO 20022 InstdAmt
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR', -- ISO 20022 Ccy
    exchange_rate DECIMAL(19,9),                -- ISO 20022 XchgRate
    exchange_rate_date DATE,                    -- ISO 20022 XchgRateDt
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain001_transaction_amount CHECK (transaction_amount > 0),
    CONSTRAINT chk_pain001_exchange_rate CHECK (exchange_rate > 0 OR exchange_rate IS NULL)
);

-- Indexes for pain001 credit transfer transactions
CREATE INDEX idx_pain001_transactions_tenant_id ON pain001_credit_transfer_transactions(tenant_id);
CREATE INDEX idx_pain001_transactions_tenant_bu ON pain001_credit_transfer_transactions(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_transactions_message_id ON pain001_credit_transfer_transactions(pain001_message_id);
CREATE INDEX idx_pain001_transactions_payment_info_id ON pain001_credit_transfer_transactions(payment_information_id);
CREATE INDEX idx_pain001_transactions_end_to_end_id ON pain001_credit_transfer_transactions(end_to_end_id);

-- =====================================================
-- PAIN.001 AUDIT LOG (from V17)
-- =====================================================
CREATE TABLE pain001_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATED', 'UPDATED', 'PROCESSED', 'FAILED')),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for pain001 audit log
CREATE INDEX idx_pain001_audit_tenant_id ON pain001_audit_log(tenant_id);
CREATE INDEX idx_pain001_audit_tenant_bu ON pain001_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_audit_action ON pain001_audit_log(action);
CREATE INDEX idx_pain001_audit_created_at ON pain001_audit_log(created_at DESC);
CREATE INDEX idx_pain001_audit_message_id ON pain001_audit_log(pain001_message_id);

-- =====================================================
-- PAIN.002 STATUS REPORTS (from V17)
-- =====================================================
CREATE TABLE pain002_status_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id VARCHAR(35) UNIQUE NOT NULL,     -- ISO 20022 MsgId
    original_message_id VARCHAR(35) NOT NULL,  -- ISO 20022 OrgnlMsgId
    creation_date_time TIMESTAMP NOT NULL,     -- ISO 20022 CreDtTm
    number_of_transactions INTEGER NOT NULL,   -- ISO 20022 NbOfTxs
    control_sum DECIMAL(19,2) NOT NULL,       -- ISO 20022 CtrlSum
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PROCESSED', 'FAILED'
    raw_message TEXT NOT NULL,                 -- Full pain.002 message
    parsed_message JSONB,                     -- Parsed message structure
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain002_status CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    CONSTRAINT chk_pain002_control_sum CHECK (control_sum >= 0),
    CONSTRAINT chk_pain002_number_of_transactions CHECK (number_of_transactions > 0)
);

-- Indexes for pain002 status reports
CREATE INDEX idx_pain002_reports_tenant_id ON pain002_status_reports(tenant_id);
CREATE INDEX idx_pain002_reports_tenant_bu ON pain002_status_reports(tenant_id, business_unit_id);
CREATE INDEX idx_pain002_reports_status ON pain002_status_reports(status);
CREATE INDEX idx_pain002_reports_created_at ON pain002_status_reports(created_at DESC);
CREATE INDEX idx_pain002_reports_message_id ON pain002_status_reports(message_id);
CREATE INDEX idx_pain002_reports_original_message_id ON pain002_status_reports(original_message_id);

-- =====================================================
-- PAIN.002 TRANSACTION STATUS (from V17)
-- =====================================================
CREATE TABLE pain002_transaction_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain002_status_report_id UUID NOT NULL REFERENCES pain002_status_reports(id) ON DELETE CASCADE,
    transaction_id VARCHAR(35) NOT NULL,        -- ISO 20022 TxId
    end_to_end_id VARCHAR(35) NOT NULL,         -- ISO 20022 EndToEndId
    instruction_id VARCHAR(35),                 -- ISO 20022 InstrId
    status_code VARCHAR(10) NOT NULL,           -- ISO 20022 Sts
    reason_code VARCHAR(10),                     -- ISO 20022 Rsn
    additional_information VARCHAR(500),         -- ISO 20022 AddtlInf
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain002_transaction_status_code CHECK (status_code IN ('ACCP', 'RJCT', 'PDNG', 'ACSC', 'CANC', 'PART'))
);

-- Indexes for pain002 transaction status
CREATE INDEX idx_pain002_transaction_status_tenant_id ON pain002_transaction_status(tenant_id);
CREATE INDEX idx_pain002_transaction_status_tenant_bu ON pain002_transaction_status(tenant_id, business_unit_id);
CREATE INDEX idx_pain002_transaction_status_report_id ON pain002_transaction_status(pain002_status_report_id);
CREATE INDEX idx_pain002_transaction_status_code ON pain002_transaction_status(status_code);
CREATE INDEX idx_pain002_transaction_end_to_end_id ON pain002_transaction_status(end_to_end_id);

-- =====================================================
-- PAIN.002 AUDIT LOG (from V17)
-- =====================================================
CREATE TABLE pain002_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain002_status_report_id UUID NOT NULL REFERENCES pain002_status_reports(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATED', 'UPDATED', 'PROCESSED', 'FAILED')),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for pain002 audit log
CREATE INDEX idx_pain002_audit_tenant_id ON pain002_audit_log(tenant_id);
CREATE INDEX idx_pain002_audit_tenant_bu ON pain002_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_pain002_audit_action ON pain002_audit_log(action);
CREATE INDEX idx_pain002_audit_created_at ON pain002_audit_log(created_at DESC);
CREATE INDEX idx_pain002_audit_status_report_id ON pain002_audit_log(pain002_status_report_id);

-- =====================================================
-- PAIN.001/PAIN.002 CORRELATION (from V17)
-- =====================================================
CREATE TABLE pain001_pain002_correlation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id VARCHAR(35) NOT NULL,
    pain002_message_id VARCHAR(35) NOT NULL,
    correlation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (correlation_status IN ('PENDING', 'MATCHED', 'MISMATCH', 'NOT_FOUND')),
    correlation_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_notes TEXT,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT uk_pain001_pain002_correlation UNIQUE (pain001_message_id, pain002_message_id)
);

-- Indexes for pain001/pain002 correlation
CREATE INDEX idx_pain_correlation_tenant_id ON pain001_pain002_correlation(tenant_id);
CREATE INDEX idx_pain_correlation_tenant_bu ON pain001_pain002_correlation(tenant_id, business_unit_id);
CREATE INDEX idx_pain_correlation_pain001_message_id ON pain001_pain002_correlation(pain001_message_id);
CREATE INDEX idx_pain_correlation_pain002_message_id ON pain001_pain002_correlation(pain002_message_id);
CREATE INDEX idx_pain_correlation_status ON pain001_pain002_correlation(correlation_status);

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

CREATE TRIGGER update_pain001_messages_updated_at 
    BEFORE UPDATE ON pain001_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pain001_payment_information_updated_at 
    BEFORE UPDATE ON pain001_payment_information
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pain001_debtor_information_updated_at 
    BEFORE UPDATE ON pain001_debtor_information
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pain001_creditor_information_updated_at 
    BEFORE UPDATE ON pain001_creditor_information
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pain001_credit_transfer_transactions_updated_at 
    BEFORE UPDATE ON pain001_credit_transfer_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pain002_status_reports_updated_at 
    BEFORE UPDATE ON pain002_status_reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pain002_transaction_status_updated_at 
    BEFORE UPDATE ON pain002_transaction_status
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Auto-track status changes
CREATE OR REPLACE FUNCTION track_payment_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO payment_status_history (payment_id, from_status, to_status, reason, tenant_id, business_unit_id)
        VALUES (NEW.payment_id, OLD.status, NEW.status, 'Status changed', NEW.tenant_id, NEW.business_unit_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER track_payment_status 
    AFTER UPDATE OF status ON payments
    FOR EACH ROW EXECUTE FUNCTION track_payment_status_change();

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_status_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE debit_order_details ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_validation_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_fees ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_payment_information ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_debtor_information ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_creditor_information ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_credit_transfer_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain002_status_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain002_transaction_status ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain002_audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_pain002_correlation ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY tenant_isolation_payments ON payments
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_payment_status_history ON payment_status_history
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_debit_order_details ON debit_order_details
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_payment_validation_results ON payment_validation_results
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_payment_fees ON payment_fees
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_payment_notifications ON payment_notifications
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_messages ON pain001_messages
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_payment_information ON pain001_payment_information
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_debtor_information ON pain001_debtor_information
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_creditor_information ON pain001_creditor_information
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_credit_transfer_transactions ON pain001_credit_transfer_transactions
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_audit_log ON pain001_audit_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain002_status_reports ON pain002_status_reports
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain002_transaction_status ON pain002_transaction_status
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain002_audit_log ON pain002_audit_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_pain002_correlation ON pain001_pain002_correlation
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View for payment summary with tenant context
CREATE VIEW payment_summary AS
SELECT 
    p.payment_id,
    p.tenant_id,
    p.business_unit_id,
    p.source_account,
    p.destination_account,
    p.amount,
    p.currency,
    p.payment_type,
    p.status,
    p.priority,
    p.created_at,
    p.completed_at,
    p.initiated_by,
    p.iso20022_compliant,
    p.message_format,
    p.pain001_message_id,
    p.pain002_message_id,
    p.iso20022_status_code,
    CASE 
        WHEN p.status = 'COMPLETED' THEN p.completed_at - p.created_at
        ELSE NULL
    END AS processing_time,
    (SELECT COUNT(*) FROM payment_status_history psh WHERE psh.payment_id = p.payment_id) AS status_changes
FROM payments p
WHERE p.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- View for payment analytics
CREATE VIEW payment_analytics AS
SELECT 
    p.tenant_id,
    p.business_unit_id,
    DATE(p.created_at) AS payment_date,
    p.payment_type,
    p.status,
    p.message_format,
    p.iso20022_compliant,
    COUNT(*) AS payment_count,
    SUM(p.amount) AS total_amount,
    AVG(p.amount) AS average_amount,
    MIN(p.amount) AS min_amount,
    MAX(p.amount) AS max_amount
FROM payments p
WHERE p.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY p.tenant_id, p.business_unit_id, DATE(p.created_at), p.payment_type, p.status, p.message_format, p.iso20022_compliant;

-- =====================================================
-- FUNCTIONS
-- =====================================================

-- Function to get payment statistics for a tenant
CREATE OR REPLACE FUNCTION get_tenant_payment_stats(p_tenant_id VARCHAR(20), p_date_from DATE, p_date_to DATE)
RETURNS TABLE (
    total_payments BIGINT,
    total_amount DECIMAL(18,2),
    successful_payments BIGINT,
    failed_payments BIGINT,
    avg_processing_time INTERVAL,
    iso20022_payments BIGINT,
    rest_payments BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT AS total_payments,
        COALESCE(SUM(p.amount), 0) AS total_amount,
        COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END)::BIGINT AS successful_payments,
        COUNT(CASE WHEN p.status = 'FAILED' THEN 1 END)::BIGINT AS failed_payments,
        AVG(CASE 
            WHEN p.status = 'COMPLETED' AND p.completed_at IS NOT NULL 
            THEN p.completed_at - p.created_at 
        END) AS avg_processing_time,
        COUNT(CASE WHEN p.iso20022_compliant = true THEN 1 END)::BIGINT AS iso20022_payments,
        COUNT(CASE WHEN p.message_format = 'REST' THEN 1 END)::BIGINT AS rest_payments
    FROM payments p
    WHERE p.tenant_id = p_tenant_id
      AND DATE(p.created_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE payments IS 'Main payments table with multi-tenancy support and ISO 20022 correlation';
COMMENT ON TABLE payment_status_history IS 'Audit trail of payment status changes';
COMMENT ON TABLE debit_order_details IS 'Additional details for debit order payments';
COMMENT ON TABLE payment_validation_results IS 'Results of payment validation checks';
COMMENT ON TABLE payment_fees IS 'Fees associated with payments';
COMMENT ON TABLE payment_notifications IS 'Notification tracking for payments';
COMMENT ON TABLE pain001_messages IS 'ISO 20022 pain.001 payment initiation messages';
COMMENT ON TABLE pain001_payment_information IS 'Payment instruction details from pain.001 messages';
COMMENT ON TABLE pain001_debtor_information IS 'Debtor information from pain.001 messages';
COMMENT ON TABLE pain001_creditor_information IS 'Creditor information from pain.001 messages';
COMMENT ON TABLE pain001_credit_transfer_transactions IS 'Credit transfer transaction details from pain.001 messages';
COMMENT ON TABLE pain001_audit_log IS 'Audit log for pain.001 message processing';
COMMENT ON TABLE pain002_status_reports IS 'ISO 20022 pain.002 payment status report messages';
COMMENT ON TABLE pain002_transaction_status IS 'Transaction status details from pain.002 messages';
COMMENT ON TABLE pain002_audit_log IS 'Audit log for pain.002 message processing';
COMMENT ON TABLE pain001_pain002_correlation IS 'Correlation between pain.001 and pain.002 messages';

COMMENT ON COLUMN payments.tenant_id IS 'Tenant identifier for multi-tenancy isolation';
COMMENT ON COLUMN payments.business_unit_id IS 'Business unit within tenant for additional isolation';
COMMENT ON COLUMN payments.idempotency_key IS 'Unique key to prevent duplicate payments';
COMMENT ON COLUMN payments.payment_type IS 'Type of payment (EFT, RTC, RTGS, etc.)';
COMMENT ON COLUMN payments.status IS 'Current status of the payment';
COMMENT ON COLUMN payments.priority IS 'Processing priority level';
COMMENT ON COLUMN payments.version IS 'Version field for optimistic locking';
COMMENT ON COLUMN payments.iso20022_compliant IS 'Whether this payment is ISO 20022 compliant';
COMMENT ON COLUMN payments.message_format IS 'Format of the original message (REST, XML, JSON)';
COMMENT ON COLUMN payments.pain001_message_id IS 'Correlation to pain.001 message';
COMMENT ON COLUMN payments.pain002_message_id IS 'Correlation to pain.002 message';
COMMENT ON COLUMN payments.iso20022_status_code IS 'ISO 20022 status code';
COMMENT ON COLUMN payments.status_reason_code IS 'ISO 20022 reason code';
COMMENT ON COLUMN payments.status_additional_info IS 'Additional status information';