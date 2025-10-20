-- Metrics Aggregation Service Database Schema
-- Migration: V1__Create_metrics_aggregation_tables.sql

-- Enable TimescaleDB extension for time-series data
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Metrics Data Table (Time-series)
CREATE TABLE metrics_data (
    id BIGSERIAL,
    service_name VARCHAR(50) NOT NULL,
    metric_type VARCHAR(20) NOT NULL CHECK (metric_type IN ('COUNTER', 'GAUGE', 'HISTOGRAM', 'TIMER', 'CUSTOM')),
    metric_name VARCHAR(100) NOT NULL,
    value DECIMAL(20,6) NOT NULL,
    unit VARCHAR(20),
    tags JSONB,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, timestamp)
);

-- Convert to TimescaleDB hypertable for time-series optimization
SELECT create_hypertable('metrics_data', 'timestamp', chunk_time_interval => INTERVAL '1 hour');

-- Alert Rules Table
CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    service_name VARCHAR(50) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    condition_type VARCHAR(30) NOT NULL CHECK (condition_type IN (
        'GREATER_THAN', 'LESS_THAN', 'EQUALS', 'NOT_EQUALS', 
        'GREATER_THAN_OR_EQUAL', 'LESS_THAN_OR_EQUAL'
    )),
    threshold_value DECIMAL(20,6) NOT NULL,
    evaluation_window_seconds INTEGER NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    notification_channels JSONB,
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Alert Events Table
CREATE TABLE alert_events (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(50) NOT NULL UNIQUE,
    rule_id BIGINT NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    service_name VARCHAR(50) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(20,6) NOT NULL,
    threshold_value DECIMAL(20,6) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('TRIGGERED', 'ACKNOWLEDGED', 'RESOLVED', 'SUPPRESSED')),
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    message TEXT,
    acknowledged_by VARCHAR(100),
    acknowledged_at TIMESTAMP,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP,
    triggered_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_metrics_data_service_timestamp ON metrics_data(service_name, timestamp DESC);
CREATE INDEX idx_metrics_data_metric_timestamp ON metrics_data(metric_name, timestamp DESC);
CREATE INDEX idx_metrics_data_type_timestamp ON metrics_data(metric_type, timestamp DESC);

CREATE INDEX idx_alert_rules_service ON alert_rules(service_name);
CREATE INDEX idx_alert_rules_enabled ON alert_rules(is_enabled);
CREATE INDEX idx_alert_rules_metric ON alert_rules(service_name, metric_name);

CREATE INDEX idx_alert_events_rule ON alert_events(rule_id);
CREATE INDEX idx_alert_events_status ON alert_events(status);
CREATE INDEX idx_alert_events_service ON alert_events(service_name);
CREATE INDEX idx_alert_events_triggered_at ON alert_events(triggered_at DESC);
CREATE INDEX idx_alert_events_severity ON alert_events(severity);

-- Update triggers for updated_at columns
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_alert_rules_updated_at 
    BEFORE UPDATE ON alert_rules 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Data retention policy for metrics_data (keep 30 days)
SELECT add_retention_policy('metrics_data', INTERVAL '30 days');

-- Compression policy for metrics_data (compress data older than 1 day)
SELECT add_compression_policy('metrics_data', INTERVAL '1 day');

-- Continuous aggregates for common queries
CREATE MATERIALIZED VIEW metrics_hourly_summary
WITH (timescaledb.continuous) AS
SELECT 
    service_name,
    metric_name,
    time_bucket('1 hour', timestamp) AS hour,
    AVG(value) as avg_value,
    MIN(value) as min_value,
    MAX(value) as max_value,
    COUNT(*) as data_points
FROM metrics_data
GROUP BY service_name, metric_name, hour;

-- Refresh policy for continuous aggregate
SELECT add_continuous_aggregate_policy('metrics_hourly_summary',
    start_offset => INTERVAL '2 hours',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour');

-- Sample alert rules
INSERT INTO alert_rules (rule_name, description, service_name, metric_name, condition_type, threshold_value, evaluation_window_seconds, severity, notification_channels, created_by) VALUES
('High Error Rate', 'Alert when error rate exceeds 5%', 'payment-initiation-service', 'http_server_requests_error_rate', 'GREATER_THAN', 0.05, 300, 'HIGH', '["email", "slack"]', 'system'),
('High Response Time', 'Alert when response time exceeds 1 second', 'payment-initiation-service', 'http_server_requests_duration_seconds', 'GREATER_THAN', 1.0, 300, 'MEDIUM', '["email"]', 'system'),
('Low Throughput', 'Alert when TPS drops below 10', 'payment-initiation-service', 'http_server_requests_total', 'LESS_THAN', 10.0, 600, 'LOW', '["slack"]', 'system'),
('Memory Usage High', 'Alert when memory usage exceeds 80%', 'payment-initiation-service', 'jvm_memory_used_bytes', 'GREATER_THAN', 0.8, 300, 'CRITICAL', '["email", "slack", "webhook"]', 'system');
