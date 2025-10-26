-- =====================================================
-- ENHANCE PAYMENTS TABLE FOR pain.001/pain.002 SUPPORT
-- =====================================================
-- Add pain.001 and pain.002 correlation fields to existing payments table
-- to support complete ISO 20022 payment initiation and status reporting cycle

-- =====================================================
-- ADD pain.001 CORRELATION FIELDS
-- =====================================================
ALTER TABLE payments ADD COLUMN pain001_message_id VARCHAR(35);
ALTER TABLE payments ADD COLUMN pain001_correlation_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN iso20022_compliant BOOLEAN DEFAULT FALSE;
ALTER TABLE payments ADD COLUMN message_format VARCHAR(10) DEFAULT 'REST'; -- 'REST', 'XML', 'JSON'
ALTER TABLE payments ADD COLUMN original_message_id VARCHAR(35); -- ISO 20022 MsgId

-- =====================================================
-- ADD pain.002 CORRELATION FIELDS
-- =====================================================
ALTER TABLE payments ADD COLUMN pain002_message_id VARCHAR(35);
ALTER TABLE payments ADD COLUMN pain002_correlation_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN iso20022_status_code VARCHAR(10); -- ISO 20022 status codes
ALTER TABLE payments ADD COLUMN status_reason_code VARCHAR(10); -- ISO 20022 reason codes
ALTER TABLE payments ADD COLUMN status_additional_info VARCHAR(500); -- Additional status information

-- =====================================================
-- ADD CONSTRAINTS
-- =====================================================
ALTER TABLE payments ADD CONSTRAINT chk_payments_message_format 
    CHECK (message_format IN ('REST', 'XML', 'JSON'));

ALTER TABLE payments ADD CONSTRAINT chk_payments_iso20022_status_code 
    CHECK (iso20022_status_code IN ('ACCP', 'RJCT', 'PDNG', 'ACSC', 'CANC', 'PART') OR iso20022_status_code IS NULL);

-- =====================================================
-- ADD PERFORMANCE INDEXES
-- =====================================================
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
-- ADD FOREIGN KEY CONSTRAINTS (Optional - for referential integrity)
-- =====================================================
-- Note: These are commented out to avoid circular dependencies
-- Uncomment if you want strict referential integrity

-- ALTER TABLE payments ADD CONSTRAINT fk_payments_pain001_message 
--     FOREIGN KEY (pain001_message_id) REFERENCES pain001_messages(message_id);

-- ALTER TABLE payments ADD CONSTRAINT fk_payments_pain002_message 
--     FOREIGN KEY (pain002_message_id) REFERENCES pain002_status_reports(message_id);

-- =====================================================
-- UPDATE EXISTING PAYMENTS
-- =====================================================
-- Set default values for existing payments
UPDATE payments 
SET 
    iso20022_compliant = FALSE,
    message_format = 'REST',
    updated_at = CURRENT_TIMESTAMP
WHERE iso20022_compliant IS NULL;

-- =====================================================
-- CREATE UPDATED VIEWS
-- =====================================================

-- Drop existing views to recreate them
DROP VIEW IF EXISTS payment_summary;
DROP VIEW IF EXISTS payment_analytics;

-- Enhanced payment summary view with ISO 20022 support
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
    p.original_message_id,
    p.pain001_message_id,
    p.pain002_message_id,
    p.iso20022_status_code,
    p.status_reason_code,
    CASE 
        WHEN p.status = 'COMPLETED' THEN p.completed_at - p.created_at
        ELSE NULL
    END AS processing_time,
    (SELECT COUNT(*) FROM payment_status_history psh WHERE psh.payment_id = p.payment_id) AS status_changes
FROM payments p
WHERE p.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- Enhanced payment analytics view with ISO 20022 support
CREATE VIEW payment_analytics AS
SELECT 
    p.tenant_id,
    p.business_unit_id,
    DATE(p.created_at) AS payment_date,
    p.payment_type,
    p.status,
    p.message_format,
    p.iso20022_compliant,
    p.iso20022_status_code,
    COUNT(*) AS payment_count,
    SUM(p.amount) AS total_amount,
    AVG(p.amount) AS average_amount,
    MIN(p.amount) AS min_amount,
    MAX(p.amount) AS max_amount
FROM payments p
WHERE p.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY p.tenant_id, p.business_unit_id, DATE(p.created_at), p.payment_type, 
         p.status, p.message_format, p.iso20022_compliant, p.iso20022_status_code;

-- New view for ISO 20022 payment analytics
CREATE VIEW iso20022_payment_analytics AS
SELECT 
    p.tenant_id,
    p.business_unit_id,
    DATE(p.created_at) AS payment_date,
    p.message_format,
    p.iso20022_status_code,
    COUNT(*) AS payment_count,
    SUM(p.amount) AS total_amount,
    AVG(p.amount) AS average_amount,
    COUNT(CASE WHEN p.pain001_message_id IS NOT NULL THEN 1 END) AS pain001_initiated_count,
    COUNT(CASE WHEN p.pain002_message_id IS NOT NULL THEN 1 END) AS pain002_reported_count,
    COUNT(CASE WHEN p.pain001_message_id IS NOT NULL AND p.pain002_message_id IS NOT NULL THEN 1 END) AS complete_cycle_count
FROM payments p
WHERE p.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
  AND p.iso20022_compliant = TRUE
GROUP BY p.tenant_id, p.business_unit_id, DATE(p.created_at), p.message_format, p.iso20022_status_code;

-- View for pain.001 to pain.002 flow tracking
CREATE VIEW pain001_pain002_flow_tracking AS
SELECT 
    p.payment_id,
    p.tenant_id,
    p.business_unit_id,
    p.pain001_message_id,
    p.pain002_message_id,
    p.original_message_id,
    p.iso20022_status_code,
    p.status_reason_code,
    p.created_at AS payment_created_at,
    pm.creation_date_time AS pain001_creation_time,
    psr.creation_date_time AS pain002_creation_time,
    CASE 
        WHEN p.pain001_message_id IS NOT NULL AND p.pain002_message_id IS NOT NULL THEN 'COMPLETE'
        WHEN p.pain001_message_id IS NOT NULL AND p.pain002_message_id IS NULL THEN 'PENDING_STATUS'
        WHEN p.pain001_message_id IS NULL THEN 'REST_ONLY'
        ELSE 'UNKNOWN'
    END AS flow_status
FROM payments p
LEFT JOIN pain001_messages pm ON p.pain001_message_id = pm.message_id
LEFT JOIN pain002_status_reports psr ON p.pain002_message_id = psr.message_id
WHERE p.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- =====================================================
-- UPDATE EXISTING FUNCTIONS
-- =====================================================

-- Drop existing function to recreate with ISO 20022 support
DROP FUNCTION IF EXISTS get_tenant_payment_stats(VARCHAR(20), DATE, DATE);

-- Enhanced function to get payment statistics with ISO 20022 support
CREATE OR REPLACE FUNCTION get_tenant_payment_stats(p_tenant_id VARCHAR(20), p_date_from DATE, p_date_to DATE)
RETURNS TABLE (
    total_payments BIGINT,
    total_amount DECIMAL(18,2),
    successful_payments BIGINT,
    failed_payments BIGINT,
    avg_processing_time INTERVAL,
    iso20022_compliant_payments BIGINT,
    pain001_initiated_payments BIGINT,
    pain002_reported_payments BIGINT,
    complete_iso20022_cycle_payments BIGINT
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
        COUNT(CASE WHEN p.iso20022_compliant = TRUE THEN 1 END)::BIGINT AS iso20022_compliant_payments,
        COUNT(CASE WHEN p.pain001_message_id IS NOT NULL THEN 1 END)::BIGINT AS pain001_initiated_payments,
        COUNT(CASE WHEN p.pain002_message_id IS NOT NULL THEN 1 END)::BIGINT AS pain002_reported_payments,
        COUNT(CASE WHEN p.pain001_message_id IS NOT NULL AND p.pain002_message_id IS NOT NULL THEN 1 END)::BIGINT AS complete_iso20022_cycle_payments
    FROM payments p
    WHERE p.tenant_id = p_tenant_id
      AND DATE(p.created_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- New function to get ISO 20022 flow statistics
CREATE OR REPLACE FUNCTION get_tenant_iso20022_flow_stats(p_tenant_id VARCHAR(20), p_date_from DATE, p_date_to DATE)
RETURNS TABLE (
    total_iso20022_payments BIGINT,
    pain001_only_payments BIGINT,
    pain002_only_payments BIGINT,
    complete_cycle_payments BIGINT,
    avg_pain001_to_pain002_time INTERVAL,
    success_rate DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(CASE WHEN p.iso20022_compliant = TRUE THEN 1 END)::BIGINT AS total_iso20022_payments,
        COUNT(CASE WHEN p.pain001_message_id IS NOT NULL AND p.pain002_message_id IS NULL THEN 1 END)::BIGINT AS pain001_only_payments,
        COUNT(CASE WHEN p.pain001_message_id IS NULL AND p.pain002_message_id IS NOT NULL THEN 1 END)::BIGINT AS pain002_only_payments,
        COUNT(CASE WHEN p.pain001_message_id IS NOT NULL AND p.pain002_message_id IS NOT NULL THEN 1 END)::BIGINT AS complete_cycle_payments,
        AVG(CASE 
            WHEN p.pain001_message_id IS NOT NULL AND p.pain002_message_id IS NOT NULL 
            THEN p.updated_at - p.created_at 
        END) AS avg_pain001_to_pain002_time,
        ROUND(
            (COUNT(CASE WHEN p.status = 'COMPLETED' AND p.iso20022_compliant = TRUE THEN 1 END)::DECIMAL / 
             NULLIF(COUNT(CASE WHEN p.iso20022_compliant = TRUE THEN 1 END), 0)) * 100, 2
        ) AS success_rate
    FROM payments p
    WHERE p.tenant_id = p_tenant_id
      AND DATE(p.created_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON COLUMN payments.pain001_message_id IS 'Reference to pain.001 message that initiated this payment';
COMMENT ON COLUMN payments.pain001_correlation_id IS 'Correlation ID for pain.001 message processing';
COMMENT ON COLUMN payments.iso20022_compliant IS 'Whether this payment was processed using ISO 20022 standards';
COMMENT ON COLUMN payments.message_format IS 'Format of the original message (REST, XML, JSON)';
COMMENT ON COLUMN payments.original_message_id IS 'Original ISO 20022 message ID from pain.001';
COMMENT ON COLUMN payments.pain002_message_id IS 'Reference to pain.002 status report for this payment';
COMMENT ON COLUMN payments.pain002_correlation_id IS 'Correlation ID for pain.002 message processing';
COMMENT ON COLUMN payments.iso20022_status_code IS 'ISO 20022 status code (ACCP, RJCT, PDNG, ACSC, CANC, PART)';
COMMENT ON COLUMN payments.status_reason_code IS 'ISO 20022 reason code for status';
COMMENT ON COLUMN payments.status_additional_info IS 'Additional status information from pain.002';

COMMENT ON VIEW iso20022_payment_analytics IS 'Analytics for ISO 20022 compliant payments';
COMMENT ON VIEW pain001_pain002_flow_tracking IS 'Tracking of pain.001 to pain.002 message flow';
