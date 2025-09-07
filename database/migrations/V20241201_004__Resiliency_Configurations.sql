-- Resiliency Configurations Migration
-- This script creates tables and sample configurations for resiliency patterns

-- ============================================================================
-- RESILIENCY CONFIGURATIONS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS payment_engine.resiliency_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    endpoint_pattern VARCHAR(500),
    circuit_breaker_config JSONB,
    retry_config JSONB,
    bulkhead_config JSONB,
    timeout_config JSONB,
    fallback_config JSONB,
    health_check_config JSONB,
    monitoring_config JSONB,
    is_active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 1 CHECK (priority >= 1 AND priority <= 100),
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================================================
-- QUEUED MESSAGES TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS payment_engine.queued_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id VARCHAR(100) NOT NULL UNIQUE,
    message_type VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    endpoint_url VARCHAR(500),
    http_method VARCHAR(10) DEFAULT 'POST',
    payload JSONB,
    headers JSONB,
    metadata JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INTEGER DEFAULT 1,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    processing_started_at TIMESTAMP,
    processing_completed_at TIMESTAMP,
    processing_time_ms BIGINT,
    result JSONB,
    error_message VARCHAR(2000),
    error_details JSONB,
    correlation_id VARCHAR(100),
    parent_message_id VARCHAR(100),
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- ============================================================================
-- SAMPLE RESILIENCY CONFIGURATIONS
-- ============================================================================

-- Fraud API Resiliency Configuration
INSERT INTO payment_engine.resiliency_configurations (
    service_name, tenant_id, endpoint_pattern,
    circuit_breaker_config, retry_config, bulkhead_config, timeout_config, fallback_config,
    health_check_config, monitoring_config, priority, description, created_by, updated_by
) VALUES (
    'fraud-api-fico',
    'tenant-001',
    '/api/v1/fraud/**',
    '{
        "failureThreshold": 5,
        "successThreshold": 3,
        "waitDurationSeconds": 60,
        "slowCallThresholdSeconds": 5,
        "slowCallRateThreshold": 0.5,
        "permittedCallsInHalfOpen": 3,
        "automaticTransitionFromOpenToHalfOpen": true
    }'::jsonb,
    '{
        "maxAttempts": 3,
        "waitDurationSeconds": 1,
        "exponentialBackoffMultiplier": 2.0,
        "maxWaitDurationSeconds": 30,
        "retryOnExceptions": "java.net.ConnectException,java.net.SocketTimeoutException",
        "ignoreExceptions": "java.lang.IllegalArgumentException"
    }'::jsonb,
    '{
        "maxConcurrentCalls": 25,
        "maxWaitDurationSeconds": 5,
        "threadPoolSize": 10,
        "queueCapacity": 100,
        "keepAliveDurationSeconds": 60
    }'::jsonb,
    '{
        "timeoutDurationSeconds": 30,
        "cancelRunningFuture": true,
        "timeoutExceptionMessage": "Fraud API call timed out"
    }'::jsonb,
    '{
        "fallbackMethod": "handleFraudApiFallback",
        "fallbackEnabled": true,
        "fallbackTimeoutSeconds": 5,
        "fallbackRetryAttempts": 1
    }'::jsonb,
    '{
        "healthCheckEnabled": true,
        "healthCheckIntervalSeconds": 30,
        "healthCheckTimeoutSeconds": 5,
        "healthCheckEndpoint": "/health",
        "healthCheckMethod": "GET",
        "expectedStatusCodes": "200,201,202"
    }'::jsonb,
    '{
        "metricsEnabled": true,
        "alertingEnabled": true,
        "alertThresholdFailureRate": 0.5,
        "alertThresholdResponseTimeMs": 5000,
        "alertThresholdCircuitBreakerOpen": true,
        "notificationChannels": "email,slack"
    }'::jsonb,
    1,
    'Resiliency configuration for FICO fraud API',
    'system',
    'system'
);

-- Core Banking Resiliency Configuration
INSERT INTO payment_engine.resiliency_configurations (
    service_name, tenant_id, endpoint_pattern,
    circuit_breaker_config, retry_config, bulkhead_config, timeout_config, fallback_config,
    health_check_config, monitoring_config, priority, description, created_by, updated_by
) VALUES (
    'core-banking-debit',
    'tenant-001',
    '/api/v1/transactions/debit',
    '{
        "failureThreshold": 3,
        "successThreshold": 2,
        "waitDurationSeconds": 30,
        "slowCallThresholdSeconds": 10,
        "slowCallRateThreshold": 0.3,
        "permittedCallsInHalfOpen": 2,
        "automaticTransitionFromOpenToHalfOpen": true
    }'::jsonb,
    '{
        "maxAttempts": 5,
        "waitDurationSeconds": 2,
        "exponentialBackoffMultiplier": 1.5,
        "maxWaitDurationSeconds": 60,
        "retryOnExceptions": "java.net.ConnectException,java.net.SocketTimeoutException,java.sql.SQLException",
        "ignoreExceptions": "java.lang.IllegalArgumentException"
    }'::jsonb,
    '{
        "maxConcurrentCalls": 50,
        "maxWaitDurationSeconds": 10,
        "threadPoolSize": 20,
        "queueCapacity": 200,
        "keepAliveDurationSeconds": 120
    }'::jsonb,
    '{
        "timeoutDurationSeconds": 60,
        "cancelRunningFuture": true,
        "timeoutExceptionMessage": "Core banking debit call timed out"
    }'::jsonb,
    '{
        "fallbackMethod": "handleDebitFallback",
        "fallbackEnabled": true,
        "fallbackTimeoutSeconds": 10,
        "fallbackRetryAttempts": 2
    }'::jsonb,
    '{
        "healthCheckEnabled": true,
        "healthCheckIntervalSeconds": 15,
        "healthCheckTimeoutSeconds": 10,
        "healthCheckEndpoint": "/health",
        "healthCheckMethod": "GET",
        "expectedStatusCodes": "200"
    }'::jsonb,
    '{
        "metricsEnabled": true,
        "alertingEnabled": true,
        "alertThresholdFailureRate": 0.3,
        "alertThresholdResponseTimeMs": 10000,
        "alertThresholdCircuitBreakerOpen": true,
        "notificationChannels": "email,slack,pagerduty"
    }'::jsonb,
    1,
    'Resiliency configuration for core banking debit operations',
    'system',
    'system'
);

-- Core Banking Credit Resiliency Configuration
INSERT INTO payment_engine.resiliency_configurations (
    service_name, tenant_id, endpoint_pattern,
    circuit_breaker_config, retry_config, bulkhead_config, timeout_config, fallback_config,
    health_check_config, monitoring_config, priority, description, created_by, updated_by
) VALUES (
    'core-banking-credit',
    'tenant-001',
    '/api/v1/transactions/credit',
    '{
        "failureThreshold": 3,
        "successThreshold": 2,
        "waitDurationSeconds": 30,
        "slowCallThresholdSeconds": 10,
        "slowCallRateThreshold": 0.3,
        "permittedCallsInHalfOpen": 2,
        "automaticTransitionFromOpenToHalfOpen": true
    }'::jsonb,
    '{
        "maxAttempts": 5,
        "waitDurationSeconds": 2,
        "exponentialBackoffMultiplier": 1.5,
        "maxWaitDurationSeconds": 60,
        "retryOnExceptions": "java.net.ConnectException,java.net.SocketTimeoutException,java.sql.SQLException",
        "ignoreExceptions": "java.lang.IllegalArgumentException"
    }'::jsonb,
    '{
        "maxConcurrentCalls": 50,
        "maxWaitDurationSeconds": 10,
        "threadPoolSize": 20,
        "queueCapacity": 200,
        "keepAliveDurationSeconds": 120
    }'::jsonb,
    '{
        "timeoutDurationSeconds": 60,
        "cancelRunningFuture": true,
        "timeoutExceptionMessage": "Core banking credit call timed out"
    }'::jsonb,
    '{
        "fallbackMethod": "handleCreditFallback",
        "fallbackEnabled": true,
        "fallbackTimeoutSeconds": 10,
        "fallbackRetryAttempts": 2
    }'::jsonb,
    '{
        "healthCheckEnabled": true,
        "healthCheckIntervalSeconds": 15,
        "healthCheckTimeoutSeconds": 10,
        "healthCheckEndpoint": "/health",
        "healthCheckMethod": "GET",
        "expectedStatusCodes": "200"
    }'::jsonb,
    '{
        "metricsEnabled": true,
        "alertingEnabled": true,
        "alertThresholdFailureRate": 0.3,
        "alertThresholdResponseTimeMs": 10000,
        "alertThresholdCircuitBreakerOpen": true,
        "notificationChannels": "email,slack,pagerduty"
    }'::jsonb,
    1,
    'Resiliency configuration for core banking credit operations',
    'system',
    'system'
);

-- Scheme Processing Resiliency Configuration
INSERT INTO payment_engine.resiliency_configurations (
    service_name, tenant_id, endpoint_pattern,
    circuit_breaker_config, retry_config, bulkhead_config, timeout_config, fallback_config,
    health_check_config, monitoring_config, priority, description, created_by, updated_by
) VALUES (
    'scheme-processing',
    'tenant-001',
    '/api/v1/scheme/**',
    '{
        "failureThreshold": 4,
        "successThreshold": 3,
        "waitDurationSeconds": 45,
        "slowCallThresholdSeconds": 8,
        "slowCallRateThreshold": 0.4,
        "permittedCallsInHalfOpen": 3,
        "automaticTransitionFromOpenToHalfOpen": true
    }'::jsonb,
    '{
        "maxAttempts": 4,
        "waitDurationSeconds": 3,
        "exponentialBackoffMultiplier": 2.0,
        "maxWaitDurationSeconds": 45,
        "retryOnExceptions": "java.net.ConnectException,java.net.SocketTimeoutException",
        "ignoreExceptions": "java.lang.IllegalArgumentException"
    }'::jsonb,
    '{
        "maxConcurrentCalls": 30,
        "maxWaitDurationSeconds": 8,
        "threadPoolSize": 15,
        "queueCapacity": 150,
        "keepAliveDurationSeconds": 90
    }'::jsonb,
    '{
        "timeoutDurationSeconds": 45,
        "cancelRunningFuture": true,
        "timeoutExceptionMessage": "Scheme processing call timed out"
    }'::jsonb,
    '{
        "fallbackMethod": "handleSchemeFallback",
        "fallbackEnabled": true,
        "fallbackTimeoutSeconds": 8,
        "fallbackRetryAttempts": 2
    }'::jsonb,
    '{
        "healthCheckEnabled": true,
        "healthCheckIntervalSeconds": 20,
        "healthCheckTimeoutSeconds": 8,
        "healthCheckEndpoint": "/health",
        "healthCheckMethod": "GET",
        "expectedStatusCodes": "200,201,202"
    }'::jsonb,
    '{
        "metricsEnabled": true,
        "alertingEnabled": true,
        "alertThresholdFailureRate": 0.4,
        "alertThresholdResponseTimeMs": 8000,
        "alertThresholdCircuitBreakerOpen": true,
        "notificationChannels": "email,slack"
    }'::jsonb,
    1,
    'Resiliency configuration for scheme processing operations',
    'system',
    'system'
);

-- Kafka Resiliency Configuration
INSERT INTO payment_engine.resiliency_configurations (
    service_name, tenant_id, endpoint_pattern,
    circuit_breaker_config, retry_config, bulkhead_config, timeout_config, fallback_config,
    health_check_config, monitoring_config, priority, description, created_by, updated_by
) VALUES (
    'kafka-producer',
    'tenant-001',
    'kafka://**',
    '{
        "failureThreshold": 6,
        "successThreshold": 4,
        "waitDurationSeconds": 90,
        "slowCallThresholdSeconds": 15,
        "slowCallRateThreshold": 0.6,
        "permittedCallsInHalfOpen": 4,
        "automaticTransitionFromOpenToHalfOpen": true
    }'::jsonb,
    '{
        "maxAttempts": 6,
        "waitDurationSeconds": 5,
        "exponentialBackoffMultiplier": 2.5,
        "maxWaitDurationSeconds": 120,
        "retryOnExceptions": "org.apache.kafka.common.errors.NetworkException,org.apache.kafka.common.errors.TimeoutException",
        "ignoreExceptions": "org.apache.kafka.common.errors.RecordTooLargeException"
    }'::jsonb,
    '{
        "maxConcurrentCalls": 100,
        "maxWaitDurationSeconds": 15,
        "threadPoolSize": 50,
        "queueCapacity": 500,
        "keepAliveDurationSeconds": 180
    }'::jsonb,
    '{
        "timeoutDurationSeconds": 90,
        "cancelRunningFuture": true,
        "timeoutExceptionMessage": "Kafka producer call timed out"
    }'::jsonb,
    '{
        "fallbackMethod": "handleKafkaFallback",
        "fallbackEnabled": true,
        "fallbackTimeoutSeconds": 15,
        "fallbackRetryAttempts": 3
    }'::jsonb,
    '{
        "healthCheckEnabled": true,
        "healthCheckIntervalSeconds": 60,
        "healthCheckTimeoutSeconds": 15,
        "healthCheckEndpoint": "/health",
        "healthCheckMethod": "GET",
        "expectedStatusCodes": "200"
    }'::jsonb,
    '{
        "metricsEnabled": true,
        "alertingEnabled": true,
        "alertThresholdFailureRate": 0.6,
        "alertThresholdResponseTimeMs": 15000,
        "alertThresholdCircuitBreakerOpen": true,
        "notificationChannels": "email,slack,pagerduty"
    }'::jsonb,
    1,
    'Resiliency configuration for Kafka producer operations',
    'system',
    'system'
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Resiliency Configurations Indexes
CREATE INDEX IF NOT EXISTS idx_resiliency_configs_service_tenant 
ON payment_engine.resiliency_configurations (service_name, tenant_id) 
WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_resiliency_configs_priority 
ON payment_engine.resiliency_configurations (priority DESC) 
WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_resiliency_configs_endpoint_pattern 
ON payment_engine.resiliency_configurations (endpoint_pattern) 
WHERE is_active = true AND endpoint_pattern IS NOT NULL;

-- Queued Messages Indexes
CREATE INDEX IF NOT EXISTS idx_queued_messages_status_tenant 
ON payment_engine.queued_messages (status, tenant_id);

CREATE INDEX IF NOT EXISTS idx_queued_messages_service_tenant 
ON payment_engine.queued_messages (service_name, tenant_id);

CREATE INDEX IF NOT EXISTS idx_queued_messages_next_retry 
ON payment_engine.queued_messages (next_retry_at) 
WHERE status = 'FAILED' AND next_retry_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_queued_messages_created_at 
ON payment_engine.queued_messages (created_at);

CREATE INDEX IF NOT EXISTS idx_queued_messages_expires_at 
ON payment_engine.queued_messages (expires_at) 
WHERE expires_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_queued_messages_correlation_id 
ON payment_engine.queued_messages (correlation_id) 
WHERE correlation_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_queued_messages_parent_message_id 
ON payment_engine.queued_messages (parent_message_id) 
WHERE parent_message_id IS NOT NULL;

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_resiliency_configurations_updated_at 
    BEFORE UPDATE ON payment_engine.resiliency_configurations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_queued_messages_updated_at 
    BEFORE UPDATE ON payment_engine.queued_messages 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON TABLE payment_engine.resiliency_configurations IS 'Resiliency configurations for different services and endpoints';
COMMENT ON COLUMN payment_engine.resiliency_configurations.circuit_breaker_config IS 'Circuit breaker configuration including failure thresholds and timeouts';
COMMENT ON COLUMN payment_engine.resiliency_configurations.retry_config IS 'Retry configuration including max attempts and backoff settings';
COMMENT ON COLUMN payment_engine.resiliency_configurations.bulkhead_config IS 'Bulkhead configuration for resource isolation';
COMMENT ON COLUMN payment_engine.resiliency_configurations.timeout_config IS 'Timeout configuration for service calls';
COMMENT ON COLUMN payment_engine.resiliency_configurations.fallback_config IS 'Fallback configuration for when services fail';
COMMENT ON COLUMN payment_engine.resiliency_configurations.health_check_config IS 'Health check configuration for service monitoring';
COMMENT ON COLUMN payment_engine.resiliency_configurations.monitoring_config IS 'Monitoring and alerting configuration';

COMMENT ON TABLE payment_engine.queued_messages IS 'Messages queued for processing when services are unavailable';
COMMENT ON COLUMN payment_engine.queued_messages.message_type IS 'Type of message: FRAUD_API_REQUEST, CORE_BANKING_DEBIT_REQUEST, etc.';
COMMENT ON COLUMN payment_engine.queued_messages.status IS 'Message status: PENDING, PROCESSING, PROCESSED, FAILED, RETRY, EXPIRED, CANCELLED';
COMMENT ON COLUMN payment_engine.queued_messages.payload IS 'Message payload in JSON format';
COMMENT ON COLUMN payment_engine.queued_messages.metadata IS 'Additional metadata for message processing';
COMMENT ON COLUMN payment_engine.queued_messages.result IS 'Processing result in JSON format';
COMMENT ON COLUMN payment_engine.queued_messages.error_details IS 'Detailed error information in JSON format';