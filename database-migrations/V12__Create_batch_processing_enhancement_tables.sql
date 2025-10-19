-- =====================================================
-- V12: Create Batch Processing Enhancement Tables
-- =====================================================
-- This migration creates comprehensive database schema
-- for enhanced batch processing capabilities including
-- job metadata, scheduling, metrics, and audit trails.
--
-- Features:
-- - Job execution metadata and tracking
-- - Job scheduling and trigger management
-- - Performance metrics and monitoring
-- - Configuration and parameter management
-- - Audit trail and history tracking
-- - Multi-tenancy support with RLS
-- - Performance optimization with indexes
--
-- Author: Payment Engine Team
-- Date: 2025-10-19
-- Version: V12
-- =====================================================

-- =====================================================
-- 1. BATCH JOB EXECUTION METADATA
-- =====================================================

-- Enhanced job execution metadata table
CREATE TABLE batch_job_execution_metadata (
    id BIGSERIAL PRIMARY KEY,
    job_execution_id BIGINT NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    execution_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL', -- MANUAL, SCHEDULED, API, WEBHOOK
    priority INTEGER NOT NULL DEFAULT 5,
    status VARCHAR(50) NOT NULL, -- PENDING, RUNNING, COMPLETED, FAILED, STOPPED, PAUSED
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_seconds INTEGER,
    exit_code VARCHAR(50),
    exit_description TEXT,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    estimated_completion_time TIMESTAMP WITH TIME ZONE,
    current_step VARCHAR(255),
    step_progress DECIMAL(5,2) DEFAULT 0.00,
    steps_completed INTEGER DEFAULT 0,
    total_steps INTEGER DEFAULT 0,
    records_processed BIGINT DEFAULT 0,
    records_failed BIGINT DEFAULT 0,
    records_skipped BIGINT DEFAULT 0,
    total_records BIGINT DEFAULT 0,
    processing_rate DECIMAL(10,2), -- records per second
    memory_usage_mb DECIMAL(10,2),
    cpu_usage_percentage DECIMAL(5,2),
    error_message TEXT,
    stack_trace TEXT,
    parameters JSONB,
    configuration JSONB,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- =====================================================
-- 2. JOB SCHEDULING AND TRIGGERS
-- =====================================================

-- Job scheduling table
CREATE TABLE batch_job_schedules (
    id BIGSERIAL PRIMARY KEY,
    schedule_id VARCHAR(100) UNIQUE NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    schedule_name VARCHAR(255) NOT NULL,
    description TEXT,
    cron_expression VARCHAR(255),
    scheduled_time TIMESTAMP WITH TIME ZONE,
    time_zone VARCHAR(100) DEFAULT 'UTC',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, PAUSED, DISABLED
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    max_executions INTEGER,
    execution_count INTEGER DEFAULT 0,
    last_execution_time TIMESTAMP WITH TIME ZONE,
    last_execution_status VARCHAR(50),
    next_execution_time TIMESTAMP WITH TIME ZONE,
    priority INTEGER DEFAULT 5,
    async BOOLEAN DEFAULT FALSE,
    parameters JSONB,
    configuration JSONB,
    notification_settings JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Job trigger history table
CREATE TABLE batch_job_trigger_history (
    id BIGSERIAL PRIMARY KEY,
    schedule_id VARCHAR(100) NOT NULL,
    job_execution_id BIGINT,
    trigger_time TIMESTAMP WITH TIME ZONE NOT NULL,
    trigger_type VARCHAR(50) NOT NULL, -- SCHEDULED, MANUAL, API, WEBHOOK
    status VARCHAR(50) NOT NULL, -- TRIGGERED, EXECUTING, COMPLETED, FAILED, SKIPPED
    execution_time TIMESTAMP WITH TIME ZONE,
    completion_time TIMESTAMP WITH TIME ZONE,
    duration_seconds INTEGER,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- =====================================================
-- 3. JOB METRICS AND PERFORMANCE TRACKING
-- =====================================================

-- Job performance metrics table
CREATE TABLE batch_job_metrics (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    metric_date DATE NOT NULL,
    metric_hour INTEGER NOT NULL, -- 0-23
    total_executions BIGINT DEFAULT 0,
    successful_executions BIGINT DEFAULT 0,
    failed_executions BIGINT DEFAULT 0,
    running_executions BIGINT DEFAULT 0,
    success_rate DECIMAL(5,2) DEFAULT 0.00,
    failure_rate DECIMAL(5,2) DEFAULT 0.00,
    average_execution_time DECIMAL(10,2) DEFAULT 0.00,
    min_execution_time DECIMAL(10,2) DEFAULT 0.00,
    max_execution_time DECIMAL(10,2) DEFAULT 0.00,
    total_execution_time DECIMAL(10,2) DEFAULT 0.00,
    average_processing_rate DECIMAL(10,2) DEFAULT 0.00,
    total_records_processed BIGINT DEFAULT 0,
    total_records_failed BIGINT DEFAULT 0,
    total_records_skipped BIGINT DEFAULT 0,
    average_memory_usage DECIMAL(10,2) DEFAULT 0.00,
    max_memory_usage DECIMAL(10,2) DEFAULT 0.00,
    average_cpu_usage DECIMAL(5,2) DEFAULT 0.00,
    max_cpu_usage DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Job execution trends table
CREATE TABLE batch_job_execution_trends (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    trend_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    execution_count BIGINT DEFAULT 0,
    success_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    average_execution_time DECIMAL(10,2) DEFAULT 0.00,
    average_processing_rate DECIMAL(10,2) DEFAULT 0.00,
    total_records_processed BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. JOB CONFIGURATION AND PARAMETERS
-- =====================================================

-- Job configuration table
CREATE TABLE batch_job_configurations (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    configuration_name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(50) NOT NULL DEFAULT '1.0',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER DEFAULT 5,
    timeout_seconds INTEGER DEFAULT 3600,
    retry_count INTEGER DEFAULT 3,
    chunk_size INTEGER DEFAULT 1000,
    page_size INTEGER DEFAULT 50,
    async BOOLEAN DEFAULT FALSE,
    max_concurrent_executions INTEGER DEFAULT 1,
    parameters JSONB,
    configuration JSONB,
    validation_rules JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(job_name, tenant_id, configuration_name)
);

-- Job parameter templates table
CREATE TABLE batch_job_parameter_templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    description TEXT,
    parameters JSONB NOT NULL,
    validation_schema JSONB,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(template_name, job_name, tenant_id)
);

-- =====================================================
-- 5. JOB AUDIT TRAIL AND HISTORY
-- =====================================================

-- Job audit trail table
CREATE TABLE batch_job_audit_trail (
    id BIGSERIAL PRIMARY KEY,
    job_execution_id BIGINT,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    action VARCHAR(100) NOT NULL, -- STARTED, STOPPED, PAUSED, RESUMED, COMPLETED, FAILED
    action_time TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id VARCHAR(100),
    user_name VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    details JSONB,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Job execution history table (denormalized for performance)
CREATE TABLE batch_job_execution_history (
    id BIGSERIAL PRIMARY KEY,
    job_execution_id BIGINT NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    business_unit_id VARCHAR(50),
    execution_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_seconds INTEGER,
    exit_code VARCHAR(50),
    exit_description TEXT,
    records_processed BIGINT DEFAULT 0,
    records_failed BIGINT DEFAULT 0,
    records_skipped BIGINT DEFAULT 0,
    total_records BIGINT DEFAULT 0,
    processing_rate DECIMAL(10,2),
    memory_usage_mb DECIMAL(10,2),
    cpu_usage_percentage DECIMAL(5,2),
    error_message TEXT,
    parameters JSONB,
    configuration JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- =====================================================
-- 6. INDEXES FOR PERFORMANCE
-- =====================================================

-- Batch job execution metadata indexes
CREATE INDEX idx_batch_job_execution_metadata_job_execution_id ON batch_job_execution_metadata(job_execution_id);
CREATE INDEX idx_batch_job_execution_metadata_job_name ON batch_job_execution_metadata(job_name);
CREATE INDEX idx_batch_job_execution_metadata_tenant_id ON batch_job_execution_metadata(tenant_id);
CREATE INDEX idx_batch_job_execution_metadata_status ON batch_job_execution_metadata(status);
CREATE INDEX idx_batch_job_execution_metadata_start_time ON batch_job_execution_metadata(start_time);
CREATE INDEX idx_batch_job_execution_metadata_created_at ON batch_job_execution_metadata(created_at);
CREATE INDEX idx_batch_job_execution_metadata_tenant_job ON batch_job_execution_metadata(tenant_id, job_name);
CREATE INDEX idx_batch_job_execution_metadata_status_tenant ON batch_job_execution_metadata(status, tenant_id);

-- Job scheduling indexes
CREATE INDEX idx_batch_job_schedules_schedule_id ON batch_job_schedules(schedule_id);
CREATE INDEX idx_batch_job_schedules_job_name ON batch_job_schedules(job_name);
CREATE INDEX idx_batch_job_schedules_tenant_id ON batch_job_schedules(tenant_id);
CREATE INDEX idx_batch_job_schedules_status ON batch_job_schedules(status);
CREATE INDEX idx_batch_job_schedules_enabled ON batch_job_schedules(enabled);
CREATE INDEX idx_batch_job_schedules_next_execution ON batch_job_schedules(next_execution_time);
CREATE INDEX idx_batch_job_schedules_tenant_job ON batch_job_schedules(tenant_id, job_name);

-- Job trigger history indexes
CREATE INDEX idx_batch_job_trigger_history_schedule_id ON batch_job_trigger_history(schedule_id);
CREATE INDEX idx_batch_job_trigger_history_job_execution_id ON batch_job_trigger_history(job_execution_id);
CREATE INDEX idx_batch_job_trigger_history_trigger_time ON batch_job_trigger_history(trigger_time);
CREATE INDEX idx_batch_job_trigger_history_status ON batch_job_trigger_history(status);

-- Job metrics indexes
CREATE INDEX idx_batch_job_metrics_job_name ON batch_job_metrics(job_name);
CREATE INDEX idx_batch_job_metrics_tenant_id ON batch_job_metrics(tenant_id);
CREATE INDEX idx_batch_job_metrics_metric_date ON batch_job_metrics(metric_date);
CREATE INDEX idx_batch_job_metrics_tenant_job_date ON batch_job_metrics(tenant_id, job_name, metric_date);

-- Job execution trends indexes
CREATE INDEX idx_batch_job_execution_trends_job_name ON batch_job_execution_trends(job_name);
CREATE INDEX idx_batch_job_execution_trends_tenant_id ON batch_job_execution_trends(tenant_id);
CREATE INDEX idx_batch_job_execution_trends_timestamp ON batch_job_execution_trends(trend_timestamp);
CREATE INDEX idx_batch_job_execution_trends_tenant_job ON batch_job_execution_trends(tenant_id, job_name);

-- Job configuration indexes
CREATE INDEX idx_batch_job_configurations_job_name ON batch_job_configurations(job_name);
CREATE INDEX idx_batch_job_configurations_tenant_id ON batch_job_configurations(tenant_id);
CREATE INDEX idx_batch_job_configurations_enabled ON batch_job_configurations(enabled);
CREATE INDEX idx_batch_job_configurations_tenant_job ON batch_job_configurations(tenant_id, job_name);

-- Job parameter templates indexes
CREATE INDEX idx_batch_job_parameter_templates_template_name ON batch_job_parameter_templates(template_name);
CREATE INDEX idx_batch_job_parameter_templates_job_name ON batch_job_parameter_templates(job_name);
CREATE INDEX idx_batch_job_parameter_templates_tenant_id ON batch_job_parameter_templates(tenant_id);
CREATE INDEX idx_batch_job_parameter_templates_is_default ON batch_job_parameter_templates(is_default);

-- Job audit trail indexes
CREATE INDEX idx_batch_job_audit_trail_job_execution_id ON batch_job_audit_trail(job_execution_id);
CREATE INDEX idx_batch_job_audit_trail_job_name ON batch_job_audit_trail(job_name);
CREATE INDEX idx_batch_job_audit_trail_tenant_id ON batch_job_audit_trail(tenant_id);
CREATE INDEX idx_batch_job_audit_trail_action ON batch_job_audit_trail(action);
CREATE INDEX idx_batch_job_audit_trail_action_time ON batch_job_audit_trail(action_time);
CREATE INDEX idx_batch_job_audit_trail_user_id ON batch_job_audit_trail(user_id);

-- Job execution history indexes
CREATE INDEX idx_batch_job_execution_history_job_execution_id ON batch_job_execution_history(job_execution_id);
CREATE INDEX idx_batch_job_execution_history_job_name ON batch_job_execution_history(job_name);
CREATE INDEX idx_batch_job_execution_history_tenant_id ON batch_job_execution_history(tenant_id);
CREATE INDEX idx_batch_job_execution_history_status ON batch_job_execution_history(status);
CREATE INDEX idx_batch_job_execution_history_start_time ON batch_job_execution_history(start_time);
CREATE INDEX idx_batch_job_execution_history_created_at ON batch_job_execution_history(created_at);

-- =====================================================
-- 7. ROW-LEVEL SECURITY (RLS) POLICIES
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE batch_job_execution_metadata ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_schedules ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_trigger_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_metrics ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_execution_trends ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_configurations ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_parameter_templates ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_audit_trail ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_job_execution_history ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY batch_job_execution_metadata_tenant_policy ON batch_job_execution_metadata
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_schedules_tenant_policy ON batch_job_schedules
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_trigger_history_tenant_policy ON batch_job_trigger_history
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_metrics_tenant_policy ON batch_job_metrics
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_execution_trends_tenant_policy ON batch_job_execution_trends
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_configurations_tenant_policy ON batch_job_configurations
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_parameter_templates_tenant_policy ON batch_job_parameter_templates
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_audit_trail_tenant_policy ON batch_job_audit_trail
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

CREATE POLICY batch_job_execution_history_tenant_policy ON batch_job_execution_history
    FOR ALL TO payment_engine_app
    USING (tenant_id = current_setting('app.current_tenant_id', true));

-- =====================================================
-- 8. TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at columns
CREATE TRIGGER update_batch_job_execution_metadata_updated_at
    BEFORE UPDATE ON batch_job_execution_metadata
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_batch_job_schedules_updated_at
    BEFORE UPDATE ON batch_job_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_batch_job_metrics_updated_at
    BEFORE UPDATE ON batch_job_metrics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_batch_job_configurations_updated_at
    BEFORE UPDATE ON batch_job_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_batch_job_parameter_templates_updated_at
    BEFORE UPDATE ON batch_job_parameter_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 9. COMMENTS AND DOCUMENTATION
-- =====================================================

-- Add table comments
COMMENT ON TABLE batch_job_execution_metadata IS 'Enhanced metadata for batch job executions with performance tracking';
COMMENT ON TABLE batch_job_schedules IS 'Job scheduling configuration with cron expressions and time-based triggers';
COMMENT ON TABLE batch_job_trigger_history IS 'History of job trigger events and their execution status';
COMMENT ON TABLE batch_job_metrics IS 'Aggregated performance metrics for batch jobs by hour and day';
COMMENT ON TABLE batch_job_execution_trends IS 'Time-series data for job execution trends and patterns';
COMMENT ON TABLE batch_job_configurations IS 'Job configuration templates and parameter management';
COMMENT ON TABLE batch_job_parameter_templates IS 'Reusable parameter templates for job executions';
COMMENT ON TABLE batch_job_audit_trail IS 'Audit trail for all job-related actions and events';
COMMENT ON TABLE batch_job_execution_history IS 'Denormalized job execution history for performance queries';

-- Add column comments for key tables
COMMENT ON COLUMN batch_job_execution_metadata.job_execution_id IS 'Reference to Spring Batch job execution ID';
COMMENT ON COLUMN batch_job_execution_metadata.execution_type IS 'Type of execution: MANUAL, SCHEDULED, API, WEBHOOK';
COMMENT ON COLUMN batch_job_execution_metadata.progress_percentage IS 'Job completion percentage (0.00 to 100.00)';
COMMENT ON COLUMN batch_job_execution_metadata.processing_rate IS 'Records processed per second';
COMMENT ON COLUMN batch_job_execution_metadata.memory_usage_mb IS 'Memory usage in megabytes';
COMMENT ON COLUMN batch_job_execution_metadata.cpu_usage_percentage IS 'CPU usage percentage (0.00 to 100.00)';

COMMENT ON COLUMN batch_job_schedules.cron_expression IS 'Cron expression for scheduled execution';
COMMENT ON COLUMN batch_job_schedules.next_execution_time IS 'Calculated next execution time based on schedule';
COMMENT ON COLUMN batch_job_schedules.max_executions IS 'Maximum number of executions (NULL for unlimited)';
COMMENT ON COLUMN batch_job_schedules.execution_count IS 'Current number of executions';

COMMENT ON COLUMN batch_job_metrics.metric_date IS 'Date for the metrics aggregation';
COMMENT ON COLUMN batch_job_metrics.metric_hour IS 'Hour of the day (0-23) for hourly metrics';
COMMENT ON COLUMN batch_job_metrics.success_rate IS 'Success rate percentage (0.00 to 100.00)';
COMMENT ON COLUMN batch_job_metrics.average_execution_time IS 'Average execution time in seconds';

-- =====================================================
-- 10. INITIAL DATA AND DEFAULTS
-- =====================================================

-- Insert default system configurations
INSERT INTO batch_job_configurations (
    job_name, tenant_id, configuration_name, description, version, enabled,
    priority, timeout_seconds, retry_count, chunk_size, page_size, async,
    max_concurrent_executions, parameters, configuration, created_by
) VALUES (
    'SYSTEM', 'SYSTEM', 'DEFAULT_CONFIG', 'Default system configuration for all jobs',
    '1.0', TRUE, 5, 3600, 3, 1000, 50, FALSE, 1,
    '{"defaultChunkSize": 1000, "defaultPageSize": 50}',
    '{"enableMetrics": true, "enableAuditLogging": true}',
    'SYSTEM'
);

-- =====================================================
-- MIGRATION COMPLETE
-- =====================================================

-- Log migration completion
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES (
    (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
    'V12',
    'Create batch processing enhancement tables',
    'SQL',
    'V12__Create_batch_processing_enhancement_tables.sql',
    NULL,
    current_user,
    CURRENT_TIMESTAMP,
    0,
    TRUE
);
