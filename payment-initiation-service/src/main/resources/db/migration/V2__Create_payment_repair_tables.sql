-- Payment Repair Tables
-- Migration: V2__Create_payment_repair_tables.sql

-- Payment Repair Log Table
CREATE TABLE payment_repair_log (
    id BIGSERIAL PRIMARY KEY,
    repair_id VARCHAR(50) NOT NULL UNIQUE,
    payment_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('RETRY', 'CANCEL', 'FORCE_COMPLETE', 'FORCE_FAIL', 'STATUS_UPDATE')),
    performed_by VARCHAR(100) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    result VARCHAR(1000) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_payment_repair_payment_id ON payment_repair_log(payment_id);
CREATE INDEX idx_payment_repair_action ON payment_repair_log(action);
CREATE INDEX idx_payment_repair_timestamp ON payment_repair_log(timestamp);
CREATE INDEX idx_payment_repair_performed_by ON payment_repair_log(performed_by);

-- Add repair-related columns to existing payments table
ALTER TABLE payments ADD COLUMN IF NOT EXISTS repair_count INTEGER DEFAULT 0;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS last_repair_action VARCHAR(20);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS last_repair_timestamp TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS last_repair_by VARCHAR(100);

-- Create trigger to update repair count
CREATE OR REPLACE FUNCTION update_payment_repair_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE payments 
    SET repair_count = repair_count + 1,
        last_repair_action = NEW.action,
        last_repair_timestamp = NEW.timestamp,
        last_repair_by = NEW.performed_by
    WHERE payment_id = NEW.payment_id;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_payment_repair_count_trigger
    AFTER INSERT ON payment_repair_log
    FOR EACH ROW EXECUTE FUNCTION update_payment_repair_count();

-- Create view for payment repair summary
CREATE VIEW payment_repair_summary AS
SELECT 
    p.payment_id,
    p.status,
    p.amount,
    p.currency,
    p.repair_count,
    p.last_repair_action,
    p.last_repair_timestamp,
    p.last_repair_by,
    p.created_at,
    p.last_updated
FROM payments p
WHERE p.repair_count > 0
ORDER BY p.last_repair_timestamp DESC;

-- Create view for repair statistics
CREATE VIEW repair_statistics AS
SELECT 
    action,
    COUNT(*) as total_actions,
    COUNT(CASE WHEN result LIKE 'SUCCESS%' THEN 1 END) as success_count,
    COUNT(CASE WHEN result LIKE 'FAILED%' THEN 1 END) as failure_count,
    COUNT(CASE WHEN result LIKE 'INITIATED%' THEN 1 END) as initiated_count
FROM payment_repair_log
GROUP BY action
ORDER BY total_actions DESC;
