-- Migration: Add Transaction Repair System
-- Description: Creates tables for transaction repair management, corrective actions, and automated retry logic

-- Create transaction_repairs table
CREATE TABLE IF NOT EXISTS payment_engine.transaction_repairs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_reference VARCHAR(100) NOT NULL,
    parent_transaction_id VARCHAR(100),
    tenant_id VARCHAR(50) NOT NULL,
    repair_type VARCHAR(50) NOT NULL CHECK (repair_type IN (
        'DEBIT_FAILED', 'CREDIT_FAILED', 'DEBIT_TIMEOUT', 'CREDIT_TIMEOUT', 
        'DEBIT_CREDIT_MISMATCH', 'PARTIAL_SUCCESS', 'SYSTEM_ERROR', 'MANUAL_REVIEW'
    )),
    repair_status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (repair_status IN (
        'PENDING', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'FAILED', 'CANCELLED'
    )),
    failure_reason VARCHAR(500),
    error_code VARCHAR(50),
    error_message VARCHAR(1000),
    from_account_number VARCHAR(50),
    to_account_number VARCHAR(50),
    amount DECIMAL(15,2),
    currency VARCHAR(3),
    payment_type VARCHAR(50),
    debit_status VARCHAR(50) CHECK (debit_status IN (
        'PENDING', 'SUCCESS', 'FAILED', 'TIMEOUT', 'CANCELLED'
    )),
    credit_status VARCHAR(50) CHECK (credit_status IN (
        'PENDING', 'SUCCESS', 'FAILED', 'TIMEOUT', 'CANCELLED'
    )),
    debit_reference VARCHAR(100),
    credit_reference VARCHAR(100),
    debit_response JSONB,
    credit_response JSONB,
    original_request JSONB,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    timeout_at TIMESTAMP,
    priority INTEGER DEFAULT 1,
    assigned_to VARCHAR(100),
    corrective_action VARCHAR(50) CHECK (corrective_action IN (
        'RETRY_DEBIT', 'RETRY_CREDIT', 'RETRY_BOTH', 'REVERSE_DEBIT', 'REVERSE_CREDIT', 
        'REVERSE_BOTH', 'MANUAL_CREDIT', 'MANUAL_DEBIT', 'MANUAL_BOTH', 
        'CANCEL_TRANSACTION', 'ESCALATE', 'NO_ACTION'
    )),
    corrective_action_details JSONB,
    resolution_notes VARCHAR(2000),
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for transaction_repairs
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_transaction_ref ON payment_engine.transaction_repairs(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_tenant_id ON payment_engine.transaction_repairs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_repair_type ON payment_engine.transaction_repairs(repair_type);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_repair_status ON payment_engine.transaction_repairs(repair_status);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_assigned_to ON payment_engine.transaction_repairs(assigned_to);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_priority ON payment_engine.transaction_repairs(priority);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_retry_count ON payment_engine.transaction_repairs(retry_count);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_next_retry_at ON payment_engine.transaction_repairs(next_retry_at);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_timeout_at ON payment_engine.transaction_repairs(timeout_at);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_created_at ON payment_engine.transaction_repairs(created_at);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_resolved_at ON payment_engine.transaction_repairs(resolved_at);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_debit_status ON payment_engine.transaction_repairs(debit_status);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_credit_status ON payment_engine.transaction_repairs(credit_status);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_corrective_action ON payment_engine.transaction_repairs(corrective_action);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_error_code ON payment_engine.transaction_repairs(error_code);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_parent_transaction_id ON payment_engine.transaction_repairs(parent_transaction_id);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_tenant_status ON payment_engine.transaction_repairs(tenant_id, repair_status);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_tenant_type ON payment_engine.transaction_repairs(tenant_id, repair_type);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_assigned_status ON payment_engine.transaction_repairs(assigned_to, repair_status);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_priority_status ON payment_engine.transaction_repairs(priority, repair_status);
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_retry_ready ON payment_engine.transaction_repairs(repair_status, retry_count, next_retry_at) WHERE repair_status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_transaction_repairs_timeout_check ON payment_engine.transaction_repairs(timeout_at, repair_status) WHERE timeout_at IS NOT NULL;

-- Create unique constraint for transaction reference and tenant
CREATE UNIQUE INDEX IF NOT EXISTS idx_transaction_repairs_unique ON payment_engine.transaction_repairs(transaction_reference, tenant_id);

-- Create trigger for updated_at
CREATE TRIGGER update_transaction_repairs_updated_at 
    BEFORE UPDATE ON payment_engine.transaction_repairs 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample transaction repair records
INSERT INTO payment_engine.transaction_repairs (
    transaction_reference, tenant_id, repair_type, repair_status, failure_reason,
    error_code, error_message, from_account_number, to_account_number, amount, currency,
    payment_type, debit_status, credit_status, retry_count, max_retries, priority,
    original_request, created_by, updated_by
) VALUES 
(
    'TXN-001-REPAIR',
    'tenant1',
    'CREDIT_FAILED',
    'PENDING',
    'Credit operation failed after successful debit',
    'CREDIT_TIMEOUT',
    'Credit operation timed out after 60 seconds',
    'ACC-001',
    'ACC-002',
    1000.00,
    'USD',
    'TRANSFER',
    'SUCCESS',
    'FAILED',
    0,
    3,
    5,
    '{"transactionReference": "TXN-001", "fromAccountNumber": "ACC-001", "toAccountNumber": "ACC-002", "amount": 1000.00, "currency": "USD", "paymentType": "TRANSFER"}',
    'system',
    'system'
),
(
    'TXN-002-REPAIR',
    'tenant1',
    'DEBIT_FAILED',
    'ASSIGNED',
    'Debit operation failed - insufficient funds',
    'INSUFFICIENT_FUNDS',
    'Account balance is insufficient for the requested amount',
    'ACC-003',
    'ACC-004',
    2500.00,
    'USD',
    'TRANSFER',
    'FAILED',
    'PENDING',
    1,
    3,
    8,
    '{"transactionReference": "TXN-002", "fromAccountNumber": "ACC-003", "toAccountNumber": "ACC-004", "amount": 2500.00, "currency": "USD", "paymentType": "TRANSFER"}',
    'system',
    'system'
),
(
    'TXN-003-REPAIR',
    'tenant2',
    'DEBIT_TIMEOUT',
    'PENDING',
    'Debit operation timed out',
    'DEBIT_TIMEOUT',
    'Debit operation timed out after 60 seconds',
    'ACC-005',
    'ACC-006',
    500.00,
    'EUR',
    'PAYMENT',
    'TIMEOUT',
    'PENDING',
    0,
    3,
    3,
    '{"transactionReference": "TXN-003", "fromAccountNumber": "ACC-005", "toAccountNumber": "ACC-006", "amount": 500.00, "currency": "EUR", "paymentType": "PAYMENT"}',
    'system',
    'system'
),
(
    'TXN-004-REPAIR',
    'tenant1',
    'MANUAL_REVIEW',
    'IN_PROGRESS',
    'Transaction requires manual review due to unusual activity',
    'MANUAL_REVIEW',
    'Transaction flagged for manual review',
    'ACC-007',
    'ACC-008',
    10000.00,
    'USD',
    'TRANSFER',
    'SUCCESS',
    'SUCCESS',
    0,
    3,
    10,
    '{"transactionReference": "TXN-004", "fromAccountNumber": "ACC-007", "toAccountNumber": "ACC-008", "amount": 10000.00, "currency": "USD", "paymentType": "TRANSFER"}',
    'system',
    'system'
),
(
    'TXN-005-REPAIR',
    'tenant2',
    'SYSTEM_ERROR',
    'RESOLVED',
    'System error during transaction processing',
    'SYSTEM_ERROR',
    'Database connection timeout during transaction processing',
    'ACC-009',
    'ACC-010',
    750.00,
    'GBP',
    'PAYMENT',
    'PENDING',
    'PENDING',
    2,
    3,
    6,
    '{"transactionReference": "TXN-005", "fromAccountNumber": "ACC-009", "toAccountNumber": "ACC-010", "amount": 750.00, "currency": "GBP", "paymentType": "PAYMENT"}',
    'system',
    'system'
);

-- Update some records with additional details
UPDATE payment_engine.transaction_repairs 
SET 
    assigned_to = 'admin@example.com',
    corrective_action = 'RETRY_CREDIT',
    corrective_action_details = '{"retryReason": "Credit timeout - retrying with extended timeout", "timeoutSeconds": 120}',
    next_retry_at = CURRENT_TIMESTAMP + INTERVAL '30 minutes'
WHERE transaction_reference = 'TXN-001-REPAIR';

UPDATE payment_engine.transaction_repairs 
SET 
    assigned_to = 'support@example.com',
    corrective_action = 'MANUAL_CREDIT',
    corrective_action_details = '{"manualAction": "Process credit manually due to insufficient funds", "notes": "Customer to be contacted for fund transfer"}'
WHERE transaction_reference = 'TXN-002-REPAIR';

UPDATE payment_engine.transaction_repairs 
SET 
    corrective_action = 'RETRY_DEBIT',
    corrective_action_details = '{"retryReason": "Debit timeout - retrying with extended timeout", "timeoutSeconds": 120}',
    next_retry_at = CURRENT_TIMESTAMP + INTERVAL '15 minutes'
WHERE transaction_reference = 'TXN-003-REPAIR';

UPDATE payment_engine.transaction_repairs 
SET 
    assigned_to = 'compliance@example.com',
    corrective_action = 'ESCALATE',
    corrective_action_details = '{"escalationReason": "High value transaction requiring compliance review", "escalationLevel": "HIGH"}'
WHERE transaction_reference = 'TXN-004-REPAIR';

UPDATE payment_engine.transaction_repairs 
SET 
    resolved_by = 'admin@example.com',
    resolved_at = CURRENT_TIMESTAMP,
    resolution_notes = 'System error resolved - transaction reprocessed successfully',
    corrective_action = 'RETRY_BOTH',
    corrective_action_details = '{"retryReason": "System error resolved", "retryCount": 1}'
WHERE transaction_reference = 'TXN-005-REPAIR';

-- Add comments
COMMENT ON TABLE payment_engine.transaction_repairs IS 'Transaction repair records for failed or incomplete debit/credit operations';

COMMENT ON COLUMN payment_engine.transaction_repairs.repair_type IS 'Type of repair needed: DEBIT_FAILED, CREDIT_FAILED, DEBIT_TIMEOUT, etc.';
COMMENT ON COLUMN payment_engine.transaction_repairs.repair_status IS 'Current status of the repair: PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, etc.';
COMMENT ON COLUMN payment_engine.transaction_repairs.debit_status IS 'Status of the debit operation: PENDING, SUCCESS, FAILED, TIMEOUT, CANCELLED';
COMMENT ON COLUMN payment_engine.transaction_repairs.credit_status IS 'Status of the credit operation: PENDING, SUCCESS, FAILED, TIMEOUT, CANCELLED';
COMMENT ON COLUMN payment_engine.transaction_repairs.debit_response IS 'Response from debit operation (JSON)';
COMMENT ON COLUMN payment_engine.transaction_repairs.credit_response IS 'Response from credit operation (JSON)';
COMMENT ON COLUMN payment_engine.transaction_repairs.original_request IS 'Original transaction request details (JSON)';
COMMENT ON COLUMN payment_engine.transaction_repairs.retry_count IS 'Number of retry attempts made';
COMMENT ON COLUMN payment_engine.transaction_repairs.max_retries IS 'Maximum number of retry attempts allowed';
COMMENT ON COLUMN payment_engine.transaction_repairs.next_retry_at IS 'Timestamp when next retry should be attempted';
COMMENT ON COLUMN payment_engine.transaction_repairs.timeout_at IS 'Timestamp when repair should timeout';
COMMENT ON COLUMN payment_engine.transaction_repairs.priority IS 'Priority level (1-10, 10 being highest)';
COMMENT ON COLUMN payment_engine.transaction_repairs.assigned_to IS 'User assigned to handle this repair';
COMMENT ON COLUMN payment_engine.transaction_repairs.corrective_action IS 'Corrective action to be taken: RETRY_DEBIT, RETRY_CREDIT, etc.';
COMMENT ON COLUMN payment_engine.transaction_repairs.corrective_action_details IS 'Details of the corrective action (JSON)';
COMMENT ON COLUMN payment_engine.transaction_repairs.resolution_notes IS 'Notes about how the repair was resolved';
COMMENT ON COLUMN payment_engine.transaction_repairs.resolved_by IS 'User who resolved the repair';
COMMENT ON COLUMN payment_engine.transaction_repairs.resolved_at IS 'Timestamp when repair was resolved';

-- Create view for active transaction repairs
CREATE OR REPLACE VIEW payment_engine.active_transaction_repairs AS
SELECT 
    tr.*,
    CASE 
        WHEN tr.repair_status = 'PENDING' AND tr.retry_count < tr.max_retries AND (tr.next_retry_at IS NULL OR tr.next_retry_at <= CURRENT_TIMESTAMP) 
        THEN 'READY_FOR_RETRY'
        WHEN tr.timeout_at IS NOT NULL AND tr.timeout_at <= CURRENT_TIMESTAMP 
        THEN 'TIMED_OUT'
        WHEN tr.priority >= 8 
        THEN 'HIGH_PRIORITY'
        WHEN tr.repair_type = 'MANUAL_REVIEW' OR tr.corrective_action = 'ESCALATE' 
        THEN 'NEEDS_MANUAL_REVIEW'
        ELSE 'NORMAL'
    END as repair_category,
    CASE 
        WHEN tr.debit_status = 'SUCCESS' AND tr.credit_status = 'SUCCESS' 
        THEN 'BOTH_SUCCESS'
        WHEN tr.debit_status = 'SUCCESS' AND tr.credit_status = 'FAILED' 
        THEN 'DEBIT_SUCCESS_CREDIT_FAILED'
        WHEN tr.debit_status = 'FAILED' AND tr.credit_status = 'PENDING' 
        THEN 'DEBIT_FAILED_CREDIT_PENDING'
        WHEN tr.debit_status = 'TIMEOUT' AND tr.credit_status = 'PENDING' 
        THEN 'DEBIT_TIMEOUT_CREDIT_PENDING'
        ELSE 'OTHER'
    END as operation_status
FROM payment_engine.transaction_repairs tr
WHERE tr.repair_status NOT IN ('RESOLVED', 'CANCELLED');

-- Create view for transaction repair statistics
CREATE OR REPLACE VIEW payment_engine.transaction_repair_statistics AS
SELECT 
    tenant_id,
    COUNT(*) as total_repairs,
    COUNT(CASE WHEN repair_status = 'PENDING' THEN 1 END) as pending_repairs,
    COUNT(CASE WHEN repair_status = 'ASSIGNED' THEN 1 END) as assigned_repairs,
    COUNT(CASE WHEN repair_status = 'IN_PROGRESS' THEN 1 END) as in_progress_repairs,
    COUNT(CASE WHEN repair_status = 'RESOLVED' THEN 1 END) as resolved_repairs,
    COUNT(CASE WHEN repair_status = 'FAILED' THEN 1 END) as failed_repairs,
    COUNT(CASE WHEN repair_status = 'CANCELLED' THEN 1 END) as cancelled_repairs,
    COUNT(CASE WHEN repair_type = 'DEBIT_FAILED' THEN 1 END) as debit_failed_repairs,
    COUNT(CASE WHEN repair_type = 'CREDIT_FAILED' THEN 1 END) as credit_failed_repairs,
    COUNT(CASE WHEN repair_type = 'DEBIT_TIMEOUT' THEN 1 END) as debit_timeout_repairs,
    COUNT(CASE WHEN repair_type = 'CREDIT_TIMEOUT' THEN 1 END) as credit_timeout_repairs,
    COUNT(CASE WHEN repair_type = 'MANUAL_REVIEW' THEN 1 END) as manual_review_repairs,
    COUNT(CASE WHEN repair_type = 'SYSTEM_ERROR' THEN 1 END) as system_error_repairs,
    COUNT(CASE WHEN priority >= 8 THEN 1 END) as high_priority_repairs,
    COUNT(CASE WHEN retry_count >= max_retries THEN 1 END) as max_retries_reached,
    AVG(priority) as average_priority,
    AVG(retry_count) as average_retry_count,
    MAX(created_at) as last_repair_created,
    MAX(resolved_at) as last_repair_resolved
FROM payment_engine.transaction_repairs
GROUP BY tenant_id;

-- Create function to get repair statistics for a specific tenant
CREATE OR REPLACE FUNCTION payment_engine.get_tenant_repair_statistics(p_tenant_id VARCHAR(50))
RETURNS TABLE (
    total_repairs BIGINT,
    pending_repairs BIGINT,
    assigned_repairs BIGINT,
    in_progress_repairs BIGINT,
    resolved_repairs BIGINT,
    failed_repairs BIGINT,
    cancelled_repairs BIGINT,
    high_priority_repairs BIGINT,
    ready_for_retry BIGINT,
    timed_out BIGINT,
    needing_manual_review BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_repairs,
        COUNT(CASE WHEN repair_status = 'PENDING' THEN 1 END) as pending_repairs,
        COUNT(CASE WHEN repair_status = 'ASSIGNED' THEN 1 END) as assigned_repairs,
        COUNT(CASE WHEN repair_status = 'IN_PROGRESS' THEN 1 END) as in_progress_repairs,
        COUNT(CASE WHEN repair_status = 'RESOLVED' THEN 1 END) as resolved_repairs,
        COUNT(CASE WHEN repair_status = 'FAILED' THEN 1 END) as failed_repairs,
        COUNT(CASE WHEN repair_status = 'CANCELLED' THEN 1 END) as cancelled_repairs,
        COUNT(CASE WHEN priority >= 8 THEN 1 END) as high_priority_repairs,
        COUNT(CASE WHEN repair_status = 'PENDING' AND retry_count < max_retries AND (next_retry_at IS NULL OR next_retry_at <= CURRENT_TIMESTAMP) THEN 1 END) as ready_for_retry,
        COUNT(CASE WHEN timeout_at IS NOT NULL AND timeout_at <= CURRENT_TIMESTAMP THEN 1 END) as timed_out,
        COUNT(CASE WHEN repair_type = 'MANUAL_REVIEW' OR corrective_action = 'ESCALATE' THEN 1 END) as needing_manual_review
    FROM payment_engine.transaction_repairs
    WHERE tenant_id = p_tenant_id;
END;
$$ LANGUAGE plpgsql;