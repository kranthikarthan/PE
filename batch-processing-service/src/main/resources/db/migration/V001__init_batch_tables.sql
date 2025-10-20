-- Batch Processing Service Database Schema
-- This migration creates tables for batch processing functionality

-- Batch job execution metadata table
CREATE TABLE batch_job_execution_metadata (
    id BIGSERIAL PRIMARY KEY,
    job_execution_id BIGINT NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    execution_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_ms BIGINT,
    records_processed BIGINT DEFAULT 0,
    records_failed BIGINT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_batch_job_execution_type CHECK (execution_type IN ('MANUAL', 'SCHEDULED', 'TRIGGERED')),
    CONSTRAINT chk_batch_job_status CHECK (status IN ('STARTED', 'RUNNING', 'COMPLETED', 'FAILED', 'STOPPED'))
);

-- Batch job metrics table
CREATE TABLE batch_job_metrics (
    id BIGSERIAL PRIMARY KEY,
    job_execution_id BIGINT NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(19,4) NOT NULL,
    metric_unit VARCHAR(20),
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_batch_job_metrics_execution FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution_metadata(id) ON DELETE CASCADE
);

-- Batch job schedules table
CREATE TABLE batch_job_schedules (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    cron_expression VARCHAR(100) NOT NULL,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_batch_job_schedule_job_tenant UNIQUE (job_name, tenant_id)
);

-- Processed payments table
CREATE TABLE processed_payments (
    id BIGSERIAL PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    payment_id VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    debtor_account VARCHAR(50) NOT NULL,
    creditor_account VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    
    CONSTRAINT chk_processed_payment_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_processed_payment_different_accounts CHECK (debtor_account != creditor_account),
    CONSTRAINT chk_processed_payment_status CHECK (status IN ('PROCESSED', 'FAILED', 'SKIPPED'))
);

-- Batch records table (legacy support)
CREATE TABLE batch_records (
    id UUID PRIMARY KEY,
    payment_id VARCHAR(100) NOT NULL,
    debtor_account VARCHAR(50) NOT NULL,
    creditor_account VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_batch_record_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_batch_record_different_accounts CHECK (debtor_account != creditor_account)
);

-- Indexes for performance
CREATE INDEX idx_batch_job_execution_metadata_tenant_id ON batch_job_execution_metadata(tenant_id);
CREATE INDEX idx_batch_job_execution_metadata_job_name ON batch_job_execution_metadata(job_name);
CREATE INDEX idx_batch_job_execution_metadata_status ON batch_job_execution_metadata(status);
CREATE INDEX idx_batch_job_execution_metadata_start_time ON batch_job_execution_metadata(start_time);

CREATE INDEX idx_batch_job_metrics_execution_id ON batch_job_metrics(job_execution_id);
CREATE INDEX idx_batch_job_metrics_name ON batch_job_metrics(metric_name);
CREATE INDEX idx_batch_job_metrics_recorded_at ON batch_job_metrics(recorded_at);

CREATE INDEX idx_batch_job_schedules_tenant_id ON batch_job_schedules(tenant_id);
CREATE INDEX idx_batch_job_schedules_active ON batch_job_schedules(is_active);

CREATE INDEX idx_processed_payments_batch_id ON processed_payments(batch_id);
CREATE INDEX idx_processed_payments_payment_id ON processed_payments(payment_id);
CREATE INDEX idx_processed_payments_tenant_id ON processed_payments(tenant_id);
CREATE INDEX idx_processed_payments_status ON processed_payments(status);
CREATE INDEX idx_processed_payments_processed_at ON processed_payments(processed_at);

CREATE INDEX idx_batch_records_payment_id ON batch_records(payment_id);
CREATE INDEX idx_batch_records_debtor_account ON batch_records(debtor_account);
CREATE INDEX idx_batch_records_creditor_account ON batch_records(creditor_account);

