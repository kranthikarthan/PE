-- =====================================================
-- PAYMENT INITIATION SERVICE DATABASE
-- =====================================================
-- Multi-tenant payment processing with comprehensive audit trail

-- =====================================================
-- PAYMENTS TABLE
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
-- PAYMENT VALIDATION RESULTS
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
-- PAYMENT FEES
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
-- PAYMENT NOTIFICATIONS
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

-- Create RLS policies for tenant isolation
-- Note: Application will set tenant context using: SET LOCAL app.current_tenant_id = 'TENANT-ID'

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
    COUNT(*) AS payment_count,
    SUM(p.amount) AS total_amount,
    AVG(p.amount) AS average_amount,
    MIN(p.amount) AS min_amount,
    MAX(p.amount) AS max_amount
FROM payments p
WHERE p.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY p.tenant_id, p.business_unit_id, DATE(p.created_at), p.payment_type, p.status;

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
    avg_processing_time INTERVAL
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
        END) AS avg_processing_time
    FROM payments p
    WHERE p.tenant_id = p_tenant_id
      AND DATE(p.created_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE payments IS 'Main payments table with multi-tenancy support';
COMMENT ON TABLE payment_status_history IS 'Audit trail of payment status changes';
COMMENT ON TABLE debit_order_details IS 'Additional details for debit order payments';
COMMENT ON TABLE payment_validation_results IS 'Results of payment validation checks';
COMMENT ON TABLE payment_fees IS 'Fees associated with payments';
COMMENT ON TABLE payment_notifications IS 'Notification tracking for payments';

COMMENT ON COLUMN payments.tenant_id IS 'Tenant identifier for multi-tenancy isolation';
COMMENT ON COLUMN payments.business_unit_id IS 'Business unit within tenant for additional isolation';
COMMENT ON COLUMN payments.idempotency_key IS 'Unique key to prevent duplicate payments';
COMMENT ON COLUMN payments.payment_type IS 'Type of payment (EFT, RTC, RTGS, etc.)';
COMMENT ON COLUMN payments.status IS 'Current status of the payment';
COMMENT ON COLUMN payments.priority IS 'Processing priority level';

COMMENT ON COLUMN payment_status_history.from_status IS 'Previous status before change';
COMMENT ON COLUMN payment_status_history.to_status IS 'New status after change';
COMMENT ON COLUMN payment_status_history.reason IS 'Reason for status change';

COMMENT ON COLUMN debit_order_details.mandate_reference IS 'Reference to the debit order mandate';
COMMENT ON COLUMN debit_order_details.debicheck_verified IS 'Whether Debicheck verification was performed';

COMMENT ON COLUMN payment_validation_results.validation_rules IS 'JSON array of validation rules applied';
COMMENT ON COLUMN payment_validation_results.failed_rules IS 'JSON array of rules that failed validation';
COMMENT ON COLUMN payment_validation_results.validation_score IS 'Overall validation score (0-1)';

COMMENT ON COLUMN payment_fees.fee_type IS 'Type of fee (processing, clearing, etc.)';
COMMENT ON COLUMN payment_fees.fee_amount IS 'Amount of the fee in the specified currency';

COMMENT ON COLUMN payment_notifications.notification_type IS 'Type of notification sent';
COMMENT ON COLUMN payment_notifications.recipient_id IS 'Identifier of the notification recipient';
COMMENT ON COLUMN payment_notifications.channel IS 'Communication channel used for notification';
