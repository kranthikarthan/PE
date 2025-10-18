-- Reconciliation tables per docs/02-MICROSERVICES-BREAKDOWN.md
CREATE TABLE IF NOT EXISTS reconciliation_runs (
    reconciliation_id VARCHAR(50) PRIMARY KEY,
    run_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_transactions INTEGER NOT NULL,
    matched_count INTEGER NOT NULL,
    unmatched_count INTEGER NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reconciliation_runs_run_date ON reconciliation_runs(run_date);

CREATE TABLE IF NOT EXISTS reconciliation_exceptions (
    exception_id VARCHAR(50) PRIMARY KEY,
    reconciliation_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50),
    clearing_reference VARCHAR(100),
    exception_type VARCHAR(50) NOT NULL,
    exception_reason TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    FOREIGN KEY (reconciliation_id) REFERENCES reconciliation_runs(reconciliation_id)
);


