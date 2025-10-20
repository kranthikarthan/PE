-- =====================================================
-- ADD MISSING PAYMENT COLUMNS
-- =====================================================
-- Add columns that JPA entity expects but are missing from V2 migration

-- Add missing timestamp columns
ALTER TABLE payments ADD COLUMN IF NOT EXISTS initiated_at TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS validated_at TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS submitted_to_clearing_at TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS cleared_at TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS failed_at TIMESTAMP;

-- Add missing failure reason column
ALTER TABLE payments ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(500);

-- Add version column for optimistic locking
ALTER TABLE payments ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Update existing records to have proper timestamps
UPDATE payments SET initiated_at = created_at WHERE initiated_at IS NULL;
UPDATE payments SET updated_at = created_at WHERE updated_at IS NULL;

-- Make initiated_at NOT NULL after setting values
ALTER TABLE payments ALTER COLUMN initiated_at SET NOT NULL;
ALTER TABLE payments ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE payments ALTER COLUMN version SET NOT NULL;

-- Add indexes for new columns
CREATE INDEX IF NOT EXISTS idx_payments_initiated_at ON payments(initiated_at DESC);
CREATE INDEX IF NOT EXISTS idx_payments_validated_at ON payments(validated_at DESC);
CREATE INDEX IF NOT EXISTS idx_payments_cleared_at ON payments(cleared_at DESC);
CREATE INDEX IF NOT EXISTS idx_payments_failed_at ON payments(failed_at DESC);
