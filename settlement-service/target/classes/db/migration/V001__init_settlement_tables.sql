-- Settlement tables per docs/02-MICROSERVICES-BREAKDOWN.md
CREATE TABLE IF NOT EXISTS settlement_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    batch_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_debit DECIMAL(18,2) NOT NULL,
    total_credit DECIMAL(18,2) NOT NULL,
    net_position DECIMAL(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    finalized_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_settlement_batches_batch_date ON settlement_batches(batch_date);
CREATE INDEX IF NOT EXISTS idx_settlement_batches_status ON settlement_batches(status);

CREATE TABLE IF NOT EXISTS settlement_transactions (
    settlement_txn_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    settlement_status VARCHAR(20) NOT NULL,
    included_at TIMESTAMP NOT NULL,
    FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id)
);


