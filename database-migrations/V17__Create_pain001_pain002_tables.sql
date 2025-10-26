-- =====================================================
-- ISO 20022 pain.001 and pain.002 Support Tables
-- =====================================================
-- Complete pain.001 (Payment Initiation) and pain.002 (Payment Status Report) support
-- with proper correlation, validation, and multi-tenancy

-- =====================================================
-- pain.001 MESSAGE STORAGE
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

-- =====================================================
-- pain.001 PAYMENT INFORMATION
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

-- =====================================================
-- pain.001 DEBTOR INFORMATION
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

-- =====================================================
-- pain.001 CREDITOR INFORMATION
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

-- =====================================================
-- pain.001 CREDIT TRANSFER TRANSACTIONS
-- =====================================================
CREATE TABLE pain001_credit_transfer_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    instruction_id VARCHAR(35),                 -- ISO 20022 PmtId.InstrId
    end_to_end_id VARCHAR(35),                -- ISO 20022 PmtId.EndToEndId
    transaction_id VARCHAR(35),               -- ISO 20022 PmtId.TxId
    instructed_amount DECIMAL(19,2) NOT NULL,  -- ISO 20022 Amt.InstdAmt
    currency VARCHAR(3) NOT NULL,             -- ISO 20022 Amt.InstdAmt.Ccy
    remittance_information VARCHAR(140),       -- ISO 20022 RmtInf.Ustrd
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain001_instructed_amount CHECK (instructed_amount > 0)
);

-- =====================================================
-- pain.002 STATUS REPORTS
-- =====================================================
CREATE TABLE pain002_status_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id VARCHAR(35) UNIQUE NOT NULL,  -- ISO 20022 MsgId
    creation_date_time TIMESTAMP NOT NULL,   -- ISO 20022 CreDtTm
    original_message_id VARCHAR(35) NOT NULL, -- ISO 20022 OrgnlMsgId
    original_message_name_id VARCHAR(35),     -- ISO 20022 OrgnlMsgNmId
    original_creation_date_time TIMESTAMP,   -- ISO 20022 OrgnlCreDtTm
    group_status VARCHAR(10) NOT NULL,       -- ISO 20022 GrpSts
    message_format VARCHAR(10) NOT NULL,     -- 'XML' or 'JSON'
    raw_message TEXT NOT NULL,               -- Full pain.002 message
    parsed_message JSONB,                   -- Parsed message structure
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain002_message_format CHECK (message_format IN ('XML', 'JSON')),
    CONSTRAINT chk_pain002_group_status CHECK (group_status IN ('ACCP', 'RJCT', 'PDNG'))
);

-- =====================================================
-- pain.002 TRANSACTION STATUS
-- =====================================================
CREATE TABLE pain002_transaction_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain002_report_id UUID NOT NULL REFERENCES pain002_status_reports(id) ON DELETE CASCADE,
    status_id VARCHAR(35) NOT NULL,          -- ISO 20022 StsId
    original_instruction_id VARCHAR(35),     -- ISO 20022 OrgnlInstrId
    original_end_to_end_id VARCHAR(35),      -- ISO 20022 OrgnlEndToEndId
    transaction_status VARCHAR(10) NOT NULL, -- ISO 20022 TxSts
    status_reason_code VARCHAR(10),          -- ISO 20022 StsRsnInf.Rsn.Cd
    additional_information VARCHAR(500),     -- ISO 20022 StsRsnInf.AddtlInf
    charges_amount DECIMAL(19,2),            -- ISO 20022 ChrgsInf.Amt.InstdAmt
    charges_currency VARCHAR(3),             -- ISO 20022 ChrgsInf.Amt.InstdAmt.Ccy
    charges_agent_bic VARCHAR(11),           -- ISO 20022 ChrgsInf.Agt.BICFI
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain002_transaction_status CHECK (transaction_status IN ('ACSC', 'RJCT', 'PDNG', 'CANC', 'PART')),
    CONSTRAINT chk_pain002_charges_amount CHECK (charges_amount >= 0)
);

-- =====================================================
-- pain.001 to pain.002 CORRELATION
-- =====================================================
CREATE TABLE pain001_pain002_correlation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id VARCHAR(35) NOT NULL,
    pain002_message_id VARCHAR(35) NOT NULL,
    original_instruction_id VARCHAR(35) NOT NULL,
    original_end_to_end_id VARCHAR(35) NOT NULL,
    status_report_id VARCHAR(35) NOT NULL,
    correlation_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT uk_pain001_pain002_correlation UNIQUE (pain001_message_id, pain002_message_id)
);

-- =====================================================
-- pain.001 VALIDATION RESULTS
-- =====================================================
CREATE TABLE pain001_validation_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    validation_type VARCHAR(20) NOT NULL,     -- 'XSD', 'YAML', 'BUSINESS'
    validation_status VARCHAR(20) NOT NULL,   -- 'PASS', 'FAIL', 'WARNING'
    validation_errors JSONB,                 -- Detailed error information
    validation_warnings JSONB,               -- Warning information
    validated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validator_version VARCHAR(20),            -- Schema version used
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain001_validation_type CHECK (validation_type IN ('XSD', 'YAML', 'BUSINESS')),
    CONSTRAINT chk_pain001_validation_status CHECK (validation_status IN ('PASS', 'FAIL', 'WARNING'))
);

-- =====================================================
-- pain.001 AUDIT LOG
-- =====================================================
CREATE TABLE pain001_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pain001_message_id UUID NOT NULL REFERENCES pain001_messages(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,              -- 'RECEIVED', 'PARSED', 'VALIDATED', 'PROCESSED', 'FAILED'
    action_details JSONB,                    -- Action-specific details
    performed_by VARCHAR(255),               -- User or system that performed action
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(255),             -- For tracing
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT chk_pain001_action CHECK (action IN ('RECEIVED', 'PARSED', 'VALIDATED', 'PROCESSED', 'FAILED'))
);

-- =====================================================
-- PERFORMANCE INDEXES
-- =====================================================

-- pain.001 indexes
CREATE INDEX idx_pain001_messages_tenant_id ON pain001_messages(tenant_id);
CREATE INDEX idx_pain001_messages_tenant_bu ON pain001_messages(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_messages_message_id ON pain001_messages(message_id);
CREATE INDEX idx_pain001_messages_creation_dt ON pain001_messages(creation_date_time);
CREATE INDEX idx_pain001_messages_validation_status ON pain001_messages(validation_status);
CREATE INDEX idx_pain001_messages_created_at ON pain001_messages(created_at DESC);

-- pain.001 payment information indexes
CREATE INDEX idx_pain001_payment_info_tenant_id ON pain001_payment_information(tenant_id);
CREATE INDEX idx_pain001_payment_info_tenant_bu ON pain001_payment_information(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_payment_info_message_id ON pain001_payment_information(pain001_message_id);
CREATE INDEX idx_pain001_payment_info_payment_id ON pain001_payment_information(payment_information_id);

-- pain.001 debtor information indexes
CREATE INDEX idx_pain001_debtor_tenant_id ON pain001_debtor_information(tenant_id);
CREATE INDEX idx_pain001_debtor_tenant_bu ON pain001_debtor_information(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_debtor_message_id ON pain001_debtor_information(pain001_message_id);

-- pain.001 creditor information indexes
CREATE INDEX idx_pain001_creditor_tenant_id ON pain001_creditor_information(tenant_id);
CREATE INDEX idx_pain001_creditor_tenant_bu ON pain001_creditor_information(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_creditor_message_id ON pain001_creditor_information(pain001_message_id);

-- pain.001 credit transfer transactions indexes
CREATE INDEX idx_pain001_credit_transfer_tenant_id ON pain001_credit_transfer_transactions(tenant_id);
CREATE INDEX idx_pain001_credit_transfer_tenant_bu ON pain001_credit_transfer_transactions(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_credit_transfer_message_id ON pain001_credit_transfer_transactions(pain001_message_id);
CREATE INDEX idx_pain001_credit_transfer_instruction_id ON pain001_credit_transfer_transactions(instruction_id);
CREATE INDEX idx_pain001_credit_transfer_end_to_end_id ON pain001_credit_transfer_transactions(end_to_end_id);

-- pain.002 indexes
CREATE INDEX idx_pain002_reports_tenant_id ON pain002_status_reports(tenant_id);
CREATE INDEX idx_pain002_reports_tenant_bu ON pain002_status_reports(tenant_id, business_unit_id);
CREATE INDEX idx_pain002_reports_message_id ON pain002_status_reports(message_id);
CREATE INDEX idx_pain002_reports_original_msg_id ON pain002_status_reports(original_message_id);
CREATE INDEX idx_pain002_reports_group_status ON pain002_status_reports(group_status);
CREATE INDEX idx_pain002_reports_created_at ON pain002_status_reports(created_at DESC);

-- pain.002 transaction status indexes
CREATE INDEX idx_pain002_transaction_tenant_id ON pain002_transaction_status(tenant_id);
CREATE INDEX idx_pain002_transaction_tenant_bu ON pain002_transaction_status(tenant_id, business_unit_id);
CREATE INDEX idx_pain002_transaction_report_id ON pain002_transaction_status(pain002_report_id);
CREATE INDEX idx_pain002_transaction_status_id ON pain002_transaction_status(status_id);
CREATE INDEX idx_pain002_transaction_instruction_id ON pain002_transaction_status(original_instruction_id);
CREATE INDEX idx_pain002_transaction_end_to_end_id ON pain002_transaction_status(original_end_to_end_id);

-- Correlation indexes
CREATE INDEX idx_pain001_pain002_correlation_tenant_id ON pain001_pain002_correlation(tenant_id);
CREATE INDEX idx_pain001_pain002_correlation_tenant_bu ON pain001_pain002_correlation(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_pain002_correlation_pain001 ON pain001_pain002_correlation(pain001_message_id);
CREATE INDEX idx_pain001_pain002_correlation_pain002 ON pain001_pain002_correlation(pain002_message_id);
CREATE INDEX idx_pain001_pain002_correlation_instruction ON pain001_pain002_correlation(original_instruction_id);
CREATE INDEX idx_pain001_pain002_correlation_end_to_end ON pain001_pain002_correlation(original_end_to_end_id);

-- Validation results indexes
CREATE INDEX idx_pain001_validation_tenant_id ON pain001_validation_results(tenant_id);
CREATE INDEX idx_pain001_validation_tenant_bu ON pain001_validation_results(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_validation_message_id ON pain001_validation_results(pain001_message_id);
CREATE INDEX idx_pain001_validation_type ON pain001_validation_results(validation_type);
CREATE INDEX idx_pain001_validation_status ON pain001_validation_results(validation_status);

-- Audit log indexes
CREATE INDEX idx_pain001_audit_tenant_id ON pain001_audit_log(tenant_id);
CREATE INDEX idx_pain001_audit_tenant_bu ON pain001_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_pain001_audit_message_id ON pain001_audit_log(pain001_message_id);
CREATE INDEX idx_pain001_audit_action ON pain001_audit_log(action);
CREATE INDEX idx_pain001_audit_performed_at ON pain001_audit_log(performed_at DESC);

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Auto-update updated_at timestamp for all tables
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

CREATE TRIGGER update_pain001_pain002_correlation_updated_at 
    BEFORE UPDATE ON pain001_pain002_correlation
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all pain.001/pain.002 tables
ALTER TABLE pain001_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_payment_information ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_debtor_information ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_creditor_information ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_credit_transfer_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain002_status_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain002_transaction_status ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_pain002_correlation ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_validation_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE pain001_audit_log ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
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

CREATE POLICY tenant_isolation_pain002_status_reports ON pain002_status_reports
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain002_transaction_status ON pain002_transaction_status
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_pain002_correlation ON pain001_pain002_correlation
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_validation_results ON pain001_validation_results
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_pain001_audit_log ON pain001_audit_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View for pain.001 message summary
CREATE VIEW pain001_message_summary AS
SELECT 
    pm.id,
    pm.message_id,
    pm.creation_date_time,
    pm.number_of_transactions,
    pm.control_sum,
    pm.initiating_party_name,
    pm.message_format,
    pm.validation_status,
    pm.created_at,
    pm.tenant_id,
    pm.business_unit_id,
    COUNT(pi.id) AS payment_information_count,
    COUNT(ctt.id) AS transaction_count
FROM pain001_messages pm
LEFT JOIN pain001_payment_information pi ON pm.id = pi.pain001_message_id
LEFT JOIN pain001_credit_transfer_transactions ctt ON pm.id = ctt.pain001_message_id
WHERE pm.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY pm.id, pm.message_id, pm.creation_date_time, pm.number_of_transactions, 
         pm.control_sum, pm.initiating_party_name, pm.message_format, 
         pm.validation_status, pm.created_at, pm.tenant_id, pm.business_unit_id;

-- View for pain.002 status report summary
CREATE VIEW pain002_status_summary AS
SELECT 
    psr.id,
    psr.message_id,
    psr.creation_date_time,
    psr.original_message_id,
    psr.group_status,
    psr.message_format,
    psr.created_at,
    psr.tenant_id,
    psr.business_unit_id,
    COUNT(pts.id) AS transaction_status_count
FROM pain002_status_reports psr
LEFT JOIN pain002_transaction_status pts ON psr.id = pts.pain002_report_id
WHERE psr.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY psr.id, psr.message_id, psr.creation_date_time, psr.original_message_id,
         psr.group_status, psr.message_format, psr.created_at, psr.tenant_id, psr.business_unit_id;

-- View for pain.001 to pain.002 correlation
CREATE VIEW pain001_pain002_correlation_summary AS
SELECT 
    c.id,
    c.pain001_message_id,
    c.pain002_message_id,
    c.original_instruction_id,
    c.original_end_to_end_id,
    c.correlation_created_at,
    pm1.creation_date_time AS pain001_creation_time,
    psr.creation_date_time AS pain002_creation_time,
    psr.group_status,
    c.tenant_id,
    c.business_unit_id
FROM pain001_pain002_correlation c
LEFT JOIN pain001_messages pm1 ON c.pain001_message_id = pm1.message_id
LEFT JOIN pain002_status_reports psr ON c.pain002_message_id = psr.message_id
WHERE c.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- =====================================================
-- FUNCTIONS
-- =====================================================

-- Function to get pain.001 statistics for a tenant
CREATE OR REPLACE FUNCTION get_tenant_pain001_stats(p_tenant_id VARCHAR(20), p_date_from DATE, p_date_to DATE)
RETURNS TABLE (
    total_messages BIGINT,
    valid_messages BIGINT,
    invalid_messages BIGINT,
    total_transactions BIGINT,
    total_amount DECIMAL(19,2),
    avg_processing_time INTERVAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(pm.id)::BIGINT AS total_messages,
        COUNT(CASE WHEN pm.validation_status = 'VALID' THEN 1 END)::BIGINT AS valid_messages,
        COUNT(CASE WHEN pm.validation_status = 'INVALID' THEN 1 END)::BIGINT AS invalid_messages,
        COALESCE(SUM(pm.number_of_transactions), 0)::BIGINT AS total_transactions,
        COALESCE(SUM(pm.control_sum), 0) AS total_amount,
        AVG(CASE 
            WHEN pm.validation_status = 'VALID' AND pm.updated_at IS NOT NULL 
            THEN pm.updated_at - pm.created_at 
        END) AS avg_processing_time
    FROM pain001_messages pm
    WHERE pm.tenant_id = p_tenant_id
      AND DATE(pm.created_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- Function to get pain.002 statistics for a tenant
CREATE OR REPLACE FUNCTION get_tenant_pain002_stats(p_tenant_id VARCHAR(20), p_date_from DATE, p_date_to DATE)
RETURNS TABLE (
    total_reports BIGINT,
    accepted_reports BIGINT,
    rejected_reports BIGINT,
    pending_reports BIGINT,
    total_transaction_statuses BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(psr.id)::BIGINT AS total_reports,
        COUNT(CASE WHEN psr.group_status = 'ACCP' THEN 1 END)::BIGINT AS accepted_reports,
        COUNT(CASE WHEN psr.group_status = 'RJCT' THEN 1 END)::BIGINT AS rejected_reports,
        COUNT(CASE WHEN psr.group_status = 'PDNG' THEN 1 END)::BIGINT AS pending_reports,
        COALESCE(SUM(pts_count.count), 0)::BIGINT AS total_transaction_statuses
    FROM pain002_status_reports psr
    LEFT JOIN (
        SELECT pain002_report_id, COUNT(*) as count 
        FROM pain002_transaction_status 
        GROUP BY pain002_report_id
    ) pts_count ON psr.id = pts_count.pain002_report_id
    WHERE psr.tenant_id = p_tenant_id
      AND DATE(psr.created_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE pain001_messages IS 'ISO 20022 pain.001 Customer Credit Transfer Initiation messages';
COMMENT ON TABLE pain001_payment_information IS 'Payment information from pain.001 messages';
COMMENT ON TABLE pain001_debtor_information IS 'Debtor information from pain.001 messages';
COMMENT ON TABLE pain001_creditor_information IS 'Creditor information from pain.001 messages';
COMMENT ON TABLE pain001_credit_transfer_transactions IS 'Credit transfer transactions from pain.001 messages';
COMMENT ON TABLE pain002_status_reports IS 'ISO 20022 pain.002 Payment Status Report messages';
COMMENT ON TABLE pain002_transaction_status IS 'Transaction status information from pain.002 messages';
COMMENT ON TABLE pain001_pain002_correlation IS 'Correlation between pain.001 and pain.002 messages';
COMMENT ON TABLE pain001_validation_results IS 'Validation results for pain.001 messages';
COMMENT ON TABLE pain001_audit_log IS 'Audit trail for pain.001 message processing';

COMMENT ON COLUMN pain001_messages.message_id IS 'ISO 20022 Message Identifier (MsgId)';
COMMENT ON COLUMN pain001_messages.creation_date_time IS 'ISO 20022 Creation Date Time (CreDtTm)';
COMMENT ON COLUMN pain001_messages.number_of_transactions IS 'ISO 20022 Number of Transactions (NbOfTxs)';
COMMENT ON COLUMN pain001_messages.control_sum IS 'ISO 20022 Control Sum (CtrlSum)';
COMMENT ON COLUMN pain001_messages.initiating_party_name IS 'ISO 20022 Initiating Party Name (InitgPty.Nm)';
COMMENT ON COLUMN pain001_messages.raw_message IS 'Complete pain.001 message in original format';
COMMENT ON COLUMN pain001_messages.parsed_message IS 'Parsed pain.001 message structure as JSON';

COMMENT ON COLUMN pain002_status_reports.message_id IS 'ISO 20022 Status Report Message Identifier (MsgId)';
COMMENT ON COLUMN pain002_status_reports.original_message_id IS 'ISO 20022 Original Message Identifier (OrgnlMsgId)';
COMMENT ON COLUMN pain002_status_reports.group_status IS 'ISO 20022 Group Status (GrpSts)';
COMMENT ON COLUMN pain002_status_reports.raw_message IS 'Complete pain.002 message in original format';
COMMENT ON COLUMN pain002_status_reports.parsed_message IS 'Parsed pain.002 message structure as JSON';

COMMENT ON COLUMN pain001_pain002_correlation.pain001_message_id IS 'Reference to original pain.001 message';
COMMENT ON COLUMN pain001_pain002_correlation.pain002_message_id IS 'Reference to corresponding pain.002 message';
COMMENT ON COLUMN pain001_pain002_correlation.original_instruction_id IS 'Original instruction ID for correlation';
COMMENT ON COLUMN pain001_pain002_correlation.original_end_to_end_id IS 'Original end-to-end ID for correlation';
