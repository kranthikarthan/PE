-- V15__Create_reconciliation_service_tables.sql

-- Table for Reconciliation Runs
CREATE TABLE reconciliation_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(100) NOT NULL UNIQUE,
    run_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_internal INTEGER DEFAULT 0,
    total_clearing INTEGER DEFAULT 0,
    matched_count INTEGER DEFAULT 0,
    exception_count INTEGER DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_reconciliation_runs_run_id ON reconciliation_runs (run_id);
CREATE INDEX idx_reconciliation_runs_tenant_id ON reconciliation_runs (tenant_id);
CREATE INDEX idx_reconciliation_runs_status ON reconciliation_runs (status);
CREATE INDEX idx_reconciliation_runs_run_date ON reconciliation_runs (run_date);
CREATE INDEX idx_reconciliation_runs_started_at ON reconciliation_runs (started_at);
CREATE INDEX idx_reconciliation_runs_completed_at ON reconciliation_runs (completed_at);
CREATE INDEX idx_reconciliation_runs_business_unit_id ON reconciliation_runs (business_unit_id);

-- RLS Policy for reconciliation_runs
ALTER TABLE reconciliation_runs ENABLE ROW LEVEL SECURITY;
CREATE POLICY reconciliation_runs_isolation_policy
    ON reconciliation_runs
    USING (tenant_id = current_setting('app.tenant_id'));

-- Table for Reconciliation Matches
CREATE TABLE reconciliation_matches (
    id BIGSERIAL PRIMARY KEY,
    match_id VARCHAR(100) NOT NULL UNIQUE,
    run_id BIGINT NOT NULL,
    internal_transaction_id VARCHAR(100) NOT NULL,
    clearing_transaction_id VARCHAR(100) NOT NULL,
    match_type VARCHAR(20) NOT NULL,
    match_confidence DECIMAL(5,2) DEFAULT 100.00,
    amount_difference DECIMAL(19,4) DEFAULT 0,
    date_difference INTEGER DEFAULT 0,
    match_score DECIMAL(5,2) DEFAULT 100.00,
    matched_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    matched_by VARCHAR(100),
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_reconciliation_match_run FOREIGN KEY (run_id) REFERENCES reconciliation_runs(id) ON DELETE CASCADE
);

CREATE INDEX idx_reconciliation_matches_match_id ON reconciliation_matches (match_id);
CREATE INDEX idx_reconciliation_matches_run_id ON reconciliation_matches (run_id);
CREATE INDEX idx_reconciliation_matches_tenant_id ON reconciliation_matches (tenant_id);
CREATE INDEX idx_reconciliation_matches_internal_txn_id ON reconciliation_matches (internal_transaction_id);
CREATE INDEX idx_reconciliation_matches_clearing_txn_id ON reconciliation_matches (clearing_transaction_id);
CREATE INDEX idx_reconciliation_matches_match_type ON reconciliation_matches (match_type);
CREATE INDEX idx_reconciliation_matches_match_confidence ON reconciliation_matches (match_confidence);
CREATE INDEX idx_reconciliation_matches_matched_at ON reconciliation_matches (matched_at);
CREATE INDEX idx_reconciliation_matches_business_unit_id ON reconciliation_matches (business_unit_id);

-- RLS Policy for reconciliation_matches
ALTER TABLE reconciliation_matches ENABLE ROW LEVEL SECURITY;
CREATE POLICY reconciliation_matches_isolation_policy
    ON reconciliation_matches
    USING (tenant_id = current_setting('app.tenant_id'));

-- Table for Reconciliation Statistics
CREATE TABLE reconciliation_statistics (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL,
    statistic_name VARCHAR(100) NOT NULL,
    statistic_value DECIMAL(19,4) NOT NULL,
    statistic_unit VARCHAR(20),
    statistic_category VARCHAR(50),
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_reconciliation_statistics_run FOREIGN KEY (run_id) REFERENCES reconciliation_runs(id) ON DELETE CASCADE
);

CREATE INDEX idx_reconciliation_statistics_run_id ON reconciliation_statistics (run_id);
CREATE INDEX idx_reconciliation_statistics_tenant_id ON reconciliation_statistics (tenant_id);
CREATE INDEX idx_reconciliation_statistics_statistic_name ON reconciliation_statistics (statistic_name);
CREATE INDEX idx_reconciliation_statistics_statistic_category ON reconciliation_statistics (statistic_category);
CREATE INDEX idx_reconciliation_statistics_calculated_at ON reconciliation_statistics (calculated_at);
CREATE INDEX idx_reconciliation_statistics_business_unit_id ON reconciliation_statistics (business_unit_id);

-- RLS Policy for reconciliation_statistics
ALTER TABLE reconciliation_statistics ENABLE ROW LEVEL SECURITY;
CREATE POLICY reconciliation_statistics_isolation_policy
    ON reconciliation_statistics
    USING (tenant_id = current_setting('app.tenant_id'));

-- Table for Reconciliation Reports
CREATE TABLE reconciliation_reports (
    id BIGSERIAL PRIMARY KEY,
    report_id VARCHAR(100) NOT NULL UNIQUE,
    run_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    report_format VARCHAR(20) NOT NULL,
    report_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    file_path VARCHAR(500),
    file_size BIGINT,
    generated_at TIMESTAMP WITH TIME ZONE,
    generated_by VARCHAR(100),
    download_count INTEGER DEFAULT 0,
    last_downloaded_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    business_unit_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_reconciliation_report_run FOREIGN KEY (run_id) REFERENCES reconciliation_runs(id) ON DELETE CASCADE
);

CREATE INDEX idx_reconciliation_reports_report_id ON reconciliation_reports (report_id);
CREATE INDEX idx_reconciliation_reports_run_id ON reconciliation_reports (run_id);
CREATE INDEX idx_reconciliation_reports_tenant_id ON reconciliation_reports (tenant_id);
CREATE INDEX idx_reconciliation_reports_report_type ON reconciliation_reports (report_type);
CREATE INDEX idx_reconciliation_reports_report_status ON reconciliation_reports (report_status);
CREATE INDEX idx_reconciliation_reports_generated_at ON reconciliation_reports (generated_at);
CREATE INDEX idx_reconciliation_reports_expires_at ON reconciliation_reports (expires_at);
CREATE INDEX idx_reconciliation_reports_business_unit_id ON reconciliation_reports (business_unit_id);

-- RLS Policy for reconciliation_reports
ALTER TABLE reconciliation_reports ENABLE ROW LEVEL SECURITY;
CREATE POLICY reconciliation_reports_isolation_policy
    ON reconciliation_reports
    USING (tenant_id = current_setting('app.tenant_id'));

-- Trigger to update 'updated_at' column automatically for reconciliation_runs
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_reconciliation_runs_updated_at
    BEFORE UPDATE ON reconciliation_runs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update 'updated_at' column automatically for reconciliation_matches
CREATE TRIGGER update_reconciliation_matches_updated_at
    BEFORE UPDATE ON reconciliation_matches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update 'updated_at' column automatically for reconciliation_statistics
CREATE TRIGGER update_reconciliation_statistics_updated_at
    BEFORE UPDATE ON reconciliation_statistics
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to update 'updated_at' column automatically for reconciliation_reports
CREATE TRIGGER update_reconciliation_reports_updated_at
    BEFORE UPDATE ON reconciliation_reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
