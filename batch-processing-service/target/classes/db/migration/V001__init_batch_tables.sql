CREATE TABLE IF NOT EXISTS batch_records (
    id UUID PRIMARY KEY,
    payment_id VARCHAR(100) NOT NULL,
    debtor_account VARCHAR(50) NOT NULL,
    creditor_account VARCHAR(50) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency CHAR(3) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_batch_payment_id ON batch_records (payment_id);

