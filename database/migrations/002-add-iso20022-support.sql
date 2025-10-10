-- ISO 20022 Database Schema Extensions
-- Adds support for ISO 20022 message processing and tracking

-- Set search path
SET search_path TO payment_engine, public;

-- ============================================================================
-- ISO 20022 MESSAGE TRACKING
-- ============================================================================

-- ISO 20022 Messages table for tracking all message types
CREATE TABLE iso20022_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message_id VARCHAR(35) UNIQUE NOT NULL,
    message_type VARCHAR(20) NOT NULL, -- pain001, pain007, pacs008, camt055, etc.
    message_version VARCHAR(10) DEFAULT '001.03',
    direction VARCHAR(10) NOT NULL, -- INBOUND, OUTBOUND
    status VARCHAR(20) DEFAULT 'RECEIVED',
    correlation_id VARCHAR(100),
    original_message_id VARCHAR(35), -- For responses and related messages
    end_to_end_id VARCHAR(35),
    instruction_id VARCHAR(35),
    transaction_id VARCHAR(35),
    uetr VARCHAR(36), -- Unique End-to-End Transaction Reference
    message_content JSONB NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    response_message_id VARCHAR(35),
    error_code VARCHAR(10),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_message_status CHECK (status IN ('RECEIVED', 'VALIDATED', 'PROCESSING', 'PROCESSED', 'FAILED', 'REJECTED')),
    CONSTRAINT chk_direction CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    CONSTRAINT chk_message_type CHECK (message_type IN ('pain001', 'pain002', 'pain007', 'pain008', 'pacs008', 'pacs002', 'pacs004', 'camt053', 'camt054', 'camt055', 'camt056', 'camt029'))
);

-- Account Extensions for ISO 20022
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS iban VARCHAR(34);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS bic VARCHAR(11);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS scheme_member_id VARCHAR(35);
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS clearing_system_code VARCHAR(5);

-- Add unique constraint for IBAN
CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_iban ON accounts(iban) WHERE iban IS NOT NULL;

-- Customer Extensions for ISO 20022
ALTER TABLE customers ADD COLUMN IF NOT EXISTS lei VARCHAR(20); -- Legal Entity Identifier
ALTER TABLE customers ADD COLUMN IF NOT EXISTS bic VARCHAR(11); -- For corporate customers
ALTER TABLE customers ADD COLUMN IF NOT EXISTS country_of_residence VARCHAR(2);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS tax_id VARCHAR(35);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS registration_id VARCHAR(35);

-- Transaction Extensions for ISO 20022
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS iso20022_message_id VARCHAR(35);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS end_to_end_id VARCHAR(35);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS instruction_id VARCHAR(35);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS uetr VARCHAR(36);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS original_end_to_end_id VARCHAR(35); -- For reversals/cancellations
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS charge_bearer VARCHAR(4); -- DEBT, CRED, SHAR, SLEV
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS purpose_code VARCHAR(4); -- CBFF, CHAR, CORT, etc.
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS local_instrument VARCHAR(35); -- RTP, ACH, WIRE, etc.
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS service_level VARCHAR(4); -- SEPA, URGP, NURG
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS clearing_system VARCHAR(5); -- USABA, CHAPS, etc.
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS settlement_method VARCHAR(4); -- INDA, INGA, COVE, CLRG
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS regulatory_reporting JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS tax_information JSONB;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS structured_remittance JSONB;

-- ============================================================================
-- CANCELLATION AND REVERSAL TRACKING
-- ============================================================================

-- Payment Cancellations table
CREATE TABLE payment_cancellations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cancellation_id VARCHAR(35) UNIQUE NOT NULL,
    original_transaction_id UUID NOT NULL REFERENCES transactions(id),
    original_message_id VARCHAR(35),
    original_end_to_end_id VARCHAR(35) NOT NULL,
    cancellation_type VARCHAR(10) NOT NULL, -- CAMT055, CAMT056, PAIN007
    cancellation_reason_code VARCHAR(4) NOT NULL, -- CUST, DUPL, FRAD, etc.
    cancellation_reason TEXT,
    requested_by VARCHAR(100),
    status VARCHAR(20) DEFAULT 'REQUESTED',
    processed_at TIMESTAMP WITH TIME ZONE,
    resolution_message_id VARCHAR(35),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cancellation_status CHECK (status IN ('REQUESTED', 'PROCESSING', 'ACCEPTED', 'REJECTED', 'COMPLETED')),
    CONSTRAINT chk_cancellation_type CHECK (cancellation_type IN ('CAMT055', 'CAMT056', 'PAIN007'))
);

-- Payment Returns table (for pacs.004)
CREATE TABLE payment_returns (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    return_id VARCHAR(35) UNIQUE NOT NULL,
    original_transaction_id UUID NOT NULL REFERENCES transactions(id),
    original_message_id VARCHAR(35),
    original_end_to_end_id VARCHAR(35) NOT NULL,
    return_reason_code VARCHAR(4) NOT NULL, -- AC01, AM04, etc.
    return_reason TEXT,
    return_amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    returned_to_scheme BOOLEAN DEFAULT false,
    scheme_reference VARCHAR(35),
    status VARCHAR(20) DEFAULT 'INITIATED',
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_return_status CHECK (status IN ('INITIATED', 'PROCESSING', 'SENT_TO_SCHEME', 'ACKNOWLEDGED', 'FAILED'))
);

-- ============================================================================
-- SCHEME AND NETWORK INTEGRATION
-- ============================================================================

-- Payment Schemes table
CREATE TABLE payment_schemes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    scheme_code VARCHAR(10) UNIQUE NOT NULL, -- USABA, CHAPS, TARGET2, etc.
    scheme_name VARCHAR(100) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    settlement_method VARCHAR(4) NOT NULL,
    operating_hours JSONB,
    cutoff_times JSONB,
    processing_calendar JSONB,
    is_active BOOLEAN DEFAULT true,
    configuration JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Scheme Transactions table (for pacs.008 processing)
CREATE TABLE scheme_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    scheme_message_id VARCHAR(35) NOT NULL,
    scheme_code VARCHAR(10) NOT NULL REFERENCES payment_schemes(scheme_code),
    transaction_id UUID REFERENCES transactions(id),
    original_end_to_end_id VARCHAR(35) NOT NULL,
    scheme_reference VARCHAR(35),
    settlement_date DATE,
    settlement_amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    debtor_agent_bic VARCHAR(11),
    creditor_agent_bic VARCHAR(11),
    status VARCHAR(20) DEFAULT 'RECEIVED',
    acknowledgment_sent BOOLEAN DEFAULT false,
    acknowledgment_message_id VARCHAR(35),
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_scheme_status CHECK (status IN ('RECEIVED', 'VALIDATED', 'PROCESSING', 'SETTLED', 'RETURNED', 'FAILED'))
);

-- ============================================================================
-- FINANCIAL INSTITUTION DATA
-- ============================================================================

-- Financial Institutions table (for BIC/SWIFT data)
CREATE TABLE financial_institutions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bic VARCHAR(11) UNIQUE NOT NULL,
    institution_name VARCHAR(140) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    city VARCHAR(35),
    branch_code VARCHAR(3),
    is_active BOOLEAN DEFAULT true,
    swift_network_member BOOLEAN DEFAULT true,
    clearing_system_memberships JSONB, -- Array of clearing systems
    correspondent_relationships JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- REGULATORY AND COMPLIANCE
-- ============================================================================

-- Regulatory Reporting table
CREATE TABLE regulatory_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    reporting_authority VARCHAR(100) NOT NULL,
    report_type VARCHAR(50) NOT NULL, -- BALANCE_OF_PAYMENTS, TRADE, etc.
    reporting_code VARCHAR(10),
    report_amount DECIMAL(15,2),
    currency_code VARCHAR(3),
    reporting_date DATE NOT NULL,
    filing_deadline DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    report_content JSONB,
    filed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_report_status CHECK (status IN ('PENDING', 'FILED', 'ACKNOWLEDGED', 'REJECTED', 'AMENDED'))
);

-- Tax Information table
CREATE TABLE tax_information (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    tax_type VARCHAR(20) NOT NULL, -- INCOME, VAT, WITHHOLDING, etc.
    tax_authority VARCHAR(100),
    tax_id VARCHAR(35),
    tax_rate DECIMAL(5,2),
    tax_amount DECIMAL(15,2),
    taxable_amount DECIMAL(15,2),
    currency_code VARCHAR(3),
    tax_period_start DATE,
    tax_period_end DATE,
    certificate_id VARCHAR(35),
    forms_code VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- INDEXES FOR ISO 20022 PERFORMANCE
-- ============================================================================

-- ISO 20022 Messages indexes
CREATE INDEX idx_iso20022_messages_message_id ON iso20022_messages(message_id);
CREATE INDEX idx_iso20022_messages_type ON iso20022_messages(message_type);
CREATE INDEX idx_iso20022_messages_status ON iso20022_messages(status);
CREATE INDEX idx_iso20022_messages_end_to_end_id ON iso20022_messages(end_to_end_id);
CREATE INDEX idx_iso20022_messages_correlation_id ON iso20022_messages(correlation_id);
CREATE INDEX idx_iso20022_messages_created_at ON iso20022_messages(created_at);

-- Transaction ISO 20022 indexes
CREATE INDEX idx_transactions_iso20022_message_id ON transactions(iso20022_message_id);
CREATE INDEX idx_transactions_end_to_end_id ON transactions(end_to_end_id);
CREATE INDEX idx_transactions_instruction_id ON transactions(instruction_id);
CREATE INDEX idx_transactions_uetr ON transactions(uetr);
CREATE INDEX idx_transactions_original_end_to_end_id ON transactions(original_end_to_end_id);

-- Account ISO 20022 indexes  
CREATE INDEX idx_accounts_iban ON accounts(iban) WHERE iban IS NOT NULL;
CREATE INDEX idx_accounts_bic ON accounts(bic) WHERE bic IS NOT NULL;

-- Cancellation indexes
CREATE INDEX idx_cancellations_original_transaction_id ON payment_cancellations(original_transaction_id);
CREATE INDEX idx_cancellations_original_end_to_end_id ON payment_cancellations(original_end_to_end_id);
CREATE INDEX idx_cancellations_status ON payment_cancellations(status);

-- Scheme transaction indexes
CREATE INDEX idx_scheme_transactions_scheme_code ON scheme_transactions(scheme_code);
CREATE INDEX idx_scheme_transactions_end_to_end_id ON scheme_transactions(original_end_to_end_id);
CREATE INDEX idx_scheme_transactions_status ON scheme_transactions(status);

-- Financial institution indexes
CREATE INDEX idx_financial_institutions_bic ON financial_institutions(bic);
CREATE INDEX idx_financial_institutions_country ON financial_institutions(country_code);

-- Regulatory reporting indexes
CREATE INDEX idx_regulatory_reports_transaction_id ON regulatory_reports(transaction_id);
CREATE INDEX idx_regulatory_reports_authority ON regulatory_reports(reporting_authority);
CREATE INDEX idx_regulatory_reports_date ON regulatory_reports(reporting_date);
CREATE INDEX idx_regulatory_reports_status ON regulatory_reports(status);

-- ============================================================================
-- VIEWS FOR ISO 20022 OPERATIONS
-- ============================================================================

-- ISO 20022 Message Summary View
CREATE VIEW iso20022_message_summary AS
SELECT 
    m.id,
    m.message_id,
    m.message_type,
    m.direction,
    m.status,
    m.end_to_end_id,
    m.correlation_id,
    t.transaction_reference,
    t.amount,
    t.currency_code,
    t.status AS transaction_status,
    m.created_at AS message_created_at,
    m.processed_at AS message_processed_at,
    EXTRACT(EPOCH FROM (COALESCE(m.processed_at, CURRENT_TIMESTAMP) - m.created_at)) AS processing_time_seconds
FROM iso20022_messages m
LEFT JOIN transactions t ON m.end_to_end_id = t.end_to_end_id
ORDER BY m.created_at DESC;

-- Transaction with ISO 20022 Details View
CREATE VIEW transaction_iso20022_view AS
SELECT 
    t.id,
    t.transaction_reference,
    t.external_reference,
    t.amount,
    t.currency_code,
    t.status,
    t.transaction_type,
    t.description,
    t.iso20022_message_id,
    t.end_to_end_id,
    t.instruction_id,
    t.uetr,
    t.charge_bearer,
    t.purpose_code,
    t.local_instrument,
    t.service_level,
    t.clearing_system,
    fa.account_number AS from_account_number,
    fa.iban AS from_iban,
    ta.account_number AS to_account_number,
    ta.iban AS to_iban,
    fc.first_name || ' ' || fc.last_name AS from_customer_name,
    tc.first_name || ' ' || tc.last_name AS to_customer_name,
    pt.name AS payment_type_name,
    t.created_at,
    t.updated_at
FROM transactions t
LEFT JOIN accounts fa ON t.from_account_id = fa.id
LEFT JOIN accounts ta ON t.to_account_id = ta.id
LEFT JOIN customers fc ON fa.customer_id = fc.id
LEFT JOIN customers tc ON ta.customer_id = tc.id
LEFT JOIN payment_types pt ON t.payment_type_id = pt.id;

-- Cancellation Summary View
CREATE VIEW cancellation_summary AS
SELECT 
    c.id,
    c.cancellation_id,
    c.original_end_to_end_id,
    c.cancellation_type,
    c.cancellation_reason_code,
    c.cancellation_reason,
    c.status AS cancellation_status,
    t.transaction_reference,
    t.amount,
    t.currency_code,
    t.status AS transaction_status,
    CONCAT(cust.first_name, ' ', cust.last_name) AS customer_name,
    c.created_at AS cancellation_requested_at,
    c.processed_at AS cancellation_processed_at
FROM payment_cancellations c
JOIN transactions t ON c.original_transaction_id = t.id
LEFT JOIN accounts acc ON (t.from_account_id = acc.id OR t.to_account_id = acc.id)
LEFT JOIN customers cust ON acc.customer_id = cust.id;

-- Scheme Processing View
CREATE VIEW scheme_processing_summary AS
SELECT 
    st.id,
    st.scheme_message_id,
    st.scheme_code,
    ps.scheme_name,
    st.original_end_to_end_id,
    st.settlement_amount,
    st.currency_code,
    st.debtor_agent_bic,
    st.creditor_agent_bic,
    st.status AS scheme_status,
    t.transaction_reference,
    t.status AS transaction_status,
    st.settlement_date,
    st.created_at AS received_at,
    st.processed_at
FROM scheme_transactions st
JOIN payment_schemes ps ON st.scheme_code = ps.scheme_code
LEFT JOIN transactions t ON st.transaction_id = t.id
ORDER BY st.created_at DESC;

-- ============================================================================
-- FUNCTIONS FOR ISO 20022 OPERATIONS
-- ============================================================================

-- Function to generate end-to-end ID
CREATE OR REPLACE FUNCTION generate_end_to_end_id()
RETURNS VARCHAR(35) AS $$
BEGIN
    RETURN 'E2E-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS') || '-' || 
           UPPER(SUBSTR(MD5(RANDOM()::TEXT), 1, 8));
END;
$$ LANGUAGE plpgsql;

-- Function to generate message ID
CREATE OR REPLACE FUNCTION generate_message_id(prefix VARCHAR(10) DEFAULT 'MSG')
RETURNS VARCHAR(35) AS $$
BEGIN
    RETURN prefix || '-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS') || '-' || 
           UPPER(SUBSTR(MD5(RANDOM()::TEXT), 1, 8));
END;
$$ LANGUAGE plpgsql;

-- Function to validate IBAN
CREATE OR REPLACE FUNCTION validate_iban(iban_code VARCHAR(34))
RETURNS BOOLEAN AS $$
BEGIN
    -- Basic IBAN validation (simplified)
    IF iban_code IS NULL OR LENGTH(iban_code) < 15 OR LENGTH(iban_code) > 34 THEN
        RETURN FALSE;
    END IF;
    
    -- Check country code (first 2 characters should be letters)
    IF NOT (SUBSTRING(iban_code, 1, 2) ~ '^[A-Z]{2}$') THEN
        RETURN FALSE;
    END IF;
    
    -- Check digits (3rd and 4th characters should be numbers)
    IF NOT (SUBSTRING(iban_code, 3, 2) ~ '^[0-9]{2}$') THEN
        RETURN FALSE;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function to validate BIC
CREATE OR REPLACE FUNCTION validate_bic(bic_code VARCHAR(11))
RETURNS BOOLEAN AS $$
BEGIN
    -- Basic BIC validation
    IF bic_code IS NULL OR LENGTH(bic_code) NOT IN (8, 11) THEN
        RETURN FALSE;
    END IF;
    
    -- Bank code (4 letters)
    IF NOT (SUBSTRING(bic_code, 1, 4) ~ '^[A-Z]{4}$') THEN
        RETURN FALSE;
    END IF;
    
    -- Country code (2 letters)
    IF NOT (SUBSTRING(bic_code, 5, 2) ~ '^[A-Z]{2}$') THEN
        RETURN FALSE;
    END IF;
    
    -- Location code (2 alphanumeric)
    IF NOT (SUBSTRING(bic_code, 7, 2) ~ '^[A-Z0-9]{2}$') THEN
        RETURN FALSE;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- TRIGGERS FOR ISO 20022 SUPPORT
-- ============================================================================

-- Auto-generate end-to-end ID if not provided
CREATE OR REPLACE FUNCTION auto_generate_end_to_end_id()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.end_to_end_id IS NULL THEN
        NEW.end_to_end_id = generate_end_to_end_id();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER auto_generate_end_to_end_id_trigger
    BEFORE INSERT ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION auto_generate_end_to_end_id();

-- Update timestamp triggers for new tables
CREATE TRIGGER update_iso20022_messages_updated_at 
    BEFORE UPDATE ON iso20022_messages 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_cancellations_updated_at 
    BEFORE UPDATE ON payment_cancellations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_returns_updated_at 
    BEFORE UPDATE ON payment_returns 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_schemes_updated_at 
    BEFORE UPDATE ON payment_schemes 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_scheme_transactions_updated_at 
    BEFORE UPDATE ON scheme_transactions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_financial_institutions_updated_at 
    BEFORE UPDATE ON financial_institutions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- CONSTRAINTS AND VALIDATIONS
-- ============================================================================

-- Add check constraints for ISO 20022 fields
ALTER TABLE accounts ADD CONSTRAINT chk_iban_format 
    CHECK (iban IS NULL OR validate_iban(iban));

ALTER TABLE accounts ADD CONSTRAINT chk_bic_format 
    CHECK (bic IS NULL OR validate_bic(bic));

ALTER TABLE financial_institutions ADD CONSTRAINT chk_fi_bic_format 
    CHECK (validate_bic(bic));

-- Ensure end-to-end IDs are unique within a reasonable time window
CREATE UNIQUE INDEX idx_transactions_end_to_end_id_unique 
    ON transactions(end_to_end_id) 
    WHERE end_to_end_id IS NOT NULL;

-- Ensure message IDs are unique
CREATE UNIQUE INDEX idx_iso20022_messages_message_id_unique 
    ON iso20022_messages(message_id);

-- ============================================================================
-- SAMPLE DATA FOR ISO 20022
-- ============================================================================

-- Insert sample payment schemes
INSERT INTO payment_schemes (scheme_code, scheme_name, country_code, currency_code, settlement_method, operating_hours, cutoff_times, is_active) VALUES
    ('USABA', 'US Automated Clearing House', 'US', 'USD', 'CLRG', '{"start": "06:00", "end": "18:00"}', '{"ach": "15:00", "wire": "16:00"}', true),
    ('FEDWIRE', 'Federal Reserve Wire Network', 'US', 'USD', 'RTGS', '{"start": "21:00", "end": "19:00"}', '{"cutoff": "18:30"}', true),
    ('RTP', 'Real-Time Payments', 'US', 'USD', 'RTGS', '{"start": "00:00", "end": "23:59"}', '{"realtime": "23:59"}', true),
    ('CHAPS', 'Clearing House Automated Payment System', 'GB', 'GBP', 'RTGS', '{"start": "06:00", "end": "18:00"}', '{"cutoff": "16:20"}', true),
    ('TARGET2', 'Trans-European Automated Real-time Gross Settlement', 'EU', 'EUR', 'RTGS', '{"start": "07:00", "end": "18:00"}', '{"cutoff": "16:00"}', true),
    ('SEPA', 'Single Euro Payments Area', 'EU', 'EUR', 'CLRG', '{"start": "06:00", "end": "17:00"}', '{"cutoff": "14:00"}', true);

-- Insert sample financial institutions
INSERT INTO financial_institutions (bic, institution_name, country_code, city, swift_network_member, clearing_system_memberships) VALUES
    ('PAYMENTUS33XXX', 'Payment Engine Bank', 'US', 'New York', true, '["USABA", "FEDWIRE", "RTP"]'),
    ('CHASUS33XXX', 'JPMorgan Chase Bank', 'US', 'New York', true, '["USABA", "FEDWIRE", "RTP"]'),
    ('BOFAUS3NXXX', 'Bank of America', 'US', 'Charlotte', true, '["USABA", "FEDWIRE", "RTP"]'),
    ('WFBIUS6SXXX', 'Wells Fargo Bank', 'US', 'San Francisco', true, '["USABA", "FEDWIRE", "RTP"]'),
    ('DEUTDEFFXXX', 'Deutsche Bank AG', 'DE', 'Frankfurt', true, '["TARGET2", "SEPA"]'),
    ('CHBSGB2LXXX', 'J.P. Morgan Europe Limited', 'GB', 'London', true, '["CHAPS", "TARGET2"]');

-- Update existing accounts with IBAN and BIC data
UPDATE accounts SET 
    iban = CASE 
        WHEN account_number = 'ACC001001' THEN 'US64SVBKUS6S3300958879'
        WHEN account_number = 'ACC001002' THEN 'US64SVBKUS6S3300958880'
        WHEN account_number = 'ACC002001' THEN 'US64SVBKUS6S3300958881'
        WHEN account_number = 'ACC002002' THEN 'US64SVBKUS6S3300958882'
        WHEN account_number = 'ACC003001' THEN 'US64SVBKUS6S3300958883'
        WHEN account_number = 'ACC004001' THEN 'US64SVBKUS6S3300958884'
        WHEN account_number = 'ACC005001' THEN 'US64SVBKUS6S3300958885'
    END,
    bic = 'PAYMENTUS33XXX',
    clearing_system_code = 'USABA'
WHERE account_number IN ('ACC001001', 'ACC001002', 'ACC002001', 'ACC002002', 'ACC003001', 'ACC004001', 'ACC005001');

-- Update existing customers with ISO 20022 data
UPDATE customers SET 
    country_of_residence = 'US',
    tax_id = CASE 
        WHEN customer_number = 'CUST001' THEN '123-45-6789'
        WHEN customer_number = 'CUST002' THEN '234-56-7890'
        WHEN customer_number = 'CUST003' THEN '345-67-8901'
        WHEN customer_number = 'CUST004' THEN '456-78-9012'
        WHEN customer_number = 'CUST005' THEN '567-89-0123'
    END
WHERE customer_number IN ('CUST001', 'CUST002', 'CUST003', 'CUST004', 'CUST005');