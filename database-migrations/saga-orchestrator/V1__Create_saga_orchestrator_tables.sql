-- =====================================================
-- SAGA ORCHESTRATOR SERVICE DATABASE
-- =====================================================
-- Independent microservice database schema
-- No cross-service dependencies

-- =====================================================
-- SAGA INSTANCES
-- =====================================================
CREATE TABLE saga_instances (
    saga_id VARCHAR(50) PRIMARY KEY,
    saga_type VARCHAR(100) NOT NULL,
    saga_status VARCHAR(20) NOT NULL DEFAULT 'STARTED' 
        CHECK (saga_status IN ('STARTED', 'RUNNING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'COMPENSATED')),
    correlation_id VARCHAR(50) NOT NULL,
    payment_id VARCHAR(50) NOT NULL,
    
    -- SAGA DATA
    saga_data JSONB NOT NULL,
    current_step VARCHAR(100),
    completed_steps JSONB DEFAULT '[]',
    failed_steps JSONB DEFAULT '[]',
    
    -- TIMING
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- ERROR HANDLING
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL
);

-- Indexes for saga instances
CREATE INDEX idx_saga_instances_tenant_id ON saga_instances(tenant_id);
CREATE INDEX idx_saga_instances_tenant_bu ON saga_instances(tenant_id, business_unit_id);
CREATE INDEX idx_saga_instances_saga_type ON saga_instances(saga_type);
CREATE INDEX idx_saga_instances_saga_status ON saga_instances(saga_status);
CREATE INDEX idx_saga_instances_correlation_id ON saga_instances(correlation_id);
CREATE INDEX idx_saga_instances_payment_id ON saga_instances(payment_id);
CREATE INDEX idx_saga_instances_started_at ON saga_instances(started_at DESC);
CREATE INDEX idx_saga_instances_next_retry_at ON saga_instances(next_retry_at);

-- =====================================================
-- SAGA STEPS
-- =====================================================
CREATE TABLE saga_steps (
    step_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_order INTEGER NOT NULL,
    step_type VARCHAR(50) NOT NULL CHECK (step_type IN ('ACTION', 'COMPENSATION')),
    step_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (step_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED')),
    
    -- STEP CONFIGURATION
    service_name VARCHAR(100) NOT NULL,
    operation VARCHAR(100) NOT NULL,
    timeout_ms INTEGER DEFAULT 30000,
    retry_attempts INTEGER DEFAULT 3,
    
    -- STEP DATA
    input_data JSONB,
    output_data JSONB,
    error_data JSONB,
    
    -- TIMING
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    
    -- ERROR HANDLING
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_saga_steps_saga FOREIGN KEY (saga_id) 
        REFERENCES saga_instances(saga_id) ON DELETE CASCADE
);

-- Indexes for saga steps
CREATE INDEX idx_saga_steps_tenant_id ON saga_steps(tenant_id);
CREATE INDEX idx_saga_steps_tenant_bu ON saga_steps(tenant_id, business_unit_id);
CREATE INDEX idx_saga_steps_saga_id ON saga_steps(saga_id);
CREATE INDEX idx_saga_steps_step_status ON saga_steps(step_status);
CREATE INDEX idx_saga_steps_step_order ON saga_steps(step_order);
CREATE INDEX idx_saga_steps_service_name ON saga_steps(service_name);

-- =====================================================
-- SAGA EVENTS
-- =====================================================
CREATE TABLE saga_events (
    event_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(50),
    causation_id VARCHAR(50),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_saga_events_saga FOREIGN KEY (saga_id) 
        REFERENCES saga_instances(saga_id) ON DELETE CASCADE
);

-- Indexes for saga events
CREATE INDEX idx_saga_events_tenant_id ON saga_events(tenant_id);
CREATE INDEX idx_saga_events_tenant_bu ON saga_events(tenant_id, business_unit_id);
CREATE INDEX idx_saga_events_saga_id ON saga_events(saga_id);
CREATE INDEX idx_saga_events_event_type ON saga_events(event_type);
CREATE INDEX idx_saga_events_event_timestamp ON saga_events(event_timestamp DESC);
CREATE INDEX idx_saga_events_correlation_id ON saga_events(correlation_id);

-- =====================================================
-- SAGA COMPENSATION LOG
-- =====================================================
CREATE TABLE saga_compensation_log (
    compensation_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_id VARCHAR(50) NOT NULL,
    compensation_type VARCHAR(50) NOT NULL CHECK (compensation_type IN ('AUTOMATIC', 'MANUAL', 'SCHEDULED')),
    compensation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (compensation_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    
    -- COMPENSATION DATA
    compensation_data JSONB,
    error_data JSONB,
    
    -- TIMING
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    
    -- ERROR HANDLING
    failure_reason TEXT,
    retry_count INTEGER DEFAULT 0,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_saga_compensation_saga FOREIGN KEY (saga_id) 
        REFERENCES saga_instances(saga_id) ON DELETE CASCADE,
    CONSTRAINT fk_saga_compensation_step FOREIGN KEY (step_id) 
        REFERENCES saga_steps(step_id) ON DELETE CASCADE
);

-- Indexes for saga compensation log
CREATE INDEX idx_saga_compensation_tenant_id ON saga_compensation_log(tenant_id);
CREATE INDEX idx_saga_compensation_tenant_bu ON saga_compensation_log(tenant_id, business_unit_id);
CREATE INDEX idx_saga_compensation_saga_id ON saga_compensation_log(saga_id);
CREATE INDEX idx_saga_compensation_step_id ON saga_compensation_log(step_id);
CREATE INDEX idx_saga_compensation_status ON saga_compensation_log(compensation_status);
CREATE INDEX idx_saga_compensation_initiated_at ON saga_compensation_log(initiated_at DESC);

-- =====================================================
-- SAGA TIMEOUTS
-- =====================================================
CREATE TABLE saga_timeouts (
    timeout_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_id VARCHAR(50),
    timeout_type VARCHAR(50) NOT NULL CHECK (timeout_type IN ('STEP_TIMEOUT', 'SAGA_TIMEOUT', 'COMPENSATION_TIMEOUT')),
    timeout_duration_ms INTEGER NOT NULL,
    timeout_at TIMESTAMP NOT NULL,
    timeout_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (timeout_status IN ('PENDING', 'TRIGGERED', 'CANCELLED', 'EXPIRED')),
    
    -- TIMEOUT DATA
    timeout_data JSONB,
    
    -- TIMING
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    triggered_at TIMESTAMP,
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_saga_timeouts_saga FOREIGN KEY (saga_id) 
        REFERENCES saga_instances(saga_id) ON DELETE CASCADE,
    CONSTRAINT fk_saga_timeouts_step FOREIGN KEY (step_id) 
        REFERENCES saga_steps(step_id) ON DELETE CASCADE
);

-- Indexes for saga timeouts
CREATE INDEX idx_saga_timeouts_tenant_id ON saga_timeouts(tenant_id);
CREATE INDEX idx_saga_timeouts_tenant_bu ON saga_timeouts(tenant_id, business_unit_id);
CREATE INDEX idx_saga_timeouts_saga_id ON saga_timeouts(saga_id);
CREATE INDEX idx_saga_timeouts_step_id ON saga_timeouts(step_id);
CREATE INDEX idx_saga_timeouts_timeout_at ON saga_timeouts(timeout_at);
CREATE INDEX idx_saga_timeouts_timeout_status ON saga_timeouts(timeout_status);

-- =====================================================
-- SAGA AUDIT LOG
-- =====================================================
CREATE TABLE saga_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    audit_type VARCHAR(50) NOT NULL CHECK (audit_type IN ('SAGA_STARTED', 'SAGA_COMPLETED', 'SAGA_FAILED', 'STEP_STARTED', 'STEP_COMPLETED', 'STEP_FAILED', 'COMPENSATION_STARTED', 'COMPENSATION_COMPLETED', 'COMPENSATION_FAILED')),
    audit_data JSONB,
    audit_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audit_by VARCHAR(100),
    
    -- MULTI-TENANCY
    tenant_id VARCHAR(20) NOT NULL,
    business_unit_id VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_saga_audit_saga FOREIGN KEY (saga_id) 
        REFERENCES saga_instances(saga_id) ON DELETE CASCADE
);

-- Indexes for saga audit log
CREATE INDEX idx_saga_audit_tenant_id ON saga_audit_log(tenant_id);
CREATE INDEX idx_saga_audit_tenant_bu ON saga_audit_log(tenant_id, business_unit_id);
CREATE INDEX idx_saga_audit_saga_id ON saga_audit_log(saga_id);
CREATE INDEX idx_saga_audit_type ON saga_audit_log(audit_type);
CREATE INDEX idx_saga_audit_timestamp ON saga_audit_log(audit_timestamp DESC);

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Auto-update last_updated_at timestamp
CREATE OR REPLACE FUNCTION update_saga_last_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_saga_instances_last_updated_at 
    BEFORE UPDATE ON saga_instances
    FOR EACH ROW EXECUTE FUNCTION update_saga_last_updated_at();

-- Auto-cleanup expired timeouts
CREATE OR REPLACE FUNCTION cleanup_expired_timeouts()
RETURNS void AS $$
BEGIN
    DELETE FROM saga_timeouts 
    WHERE timeout_at < CURRENT_TIMESTAMP 
      AND timeout_status = 'PENDING';
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- ROW LEVEL SECURITY (RLS) - Multi-tenancy enforcement
-- =====================================================

-- Enable RLS on all tables
ALTER TABLE saga_instances ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_steps ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_compensation_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_timeouts ENABLE ROW LEVEL SECURITY;
ALTER TABLE saga_audit_log ENABLE ROW LEVEL SECURITY;

-- Create RLS policies for tenant isolation
CREATE POLICY tenant_isolation_saga_instances ON saga_instances
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_saga_steps ON saga_steps
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_saga_events ON saga_events
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_saga_compensation_log ON saga_compensation_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_saga_timeouts ON saga_timeouts
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

CREATE POLICY tenant_isolation_saga_audit_log ON saga_audit_log
    USING (tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR);

-- =====================================================
-- VIEWS FOR REPORTING
-- =====================================================

-- View for saga performance dashboard
CREATE VIEW saga_performance_dashboard AS
SELECT 
    si.saga_id,
    si.saga_type,
    si.saga_status,
    si.correlation_id,
    si.payment_id,
    si.started_at,
    si.completed_at,
    si.failed_at,
    CASE 
        WHEN si.saga_status = 'COMPLETED' AND si.completed_at IS NOT NULL 
        THEN si.completed_at - si.started_at 
        ELSE NULL 
    END AS processing_time,
    (SELECT COUNT(*) FROM saga_steps ss WHERE ss.saga_id = si.saga_id) AS total_steps,
    (SELECT COUNT(*) FROM saga_steps ss WHERE ss.saga_id = si.saga_id AND ss.step_status = 'COMPLETED') AS completed_steps,
    (SELECT COUNT(*) FROM saga_steps ss WHERE ss.saga_id = si.saga_id AND ss.step_status = 'FAILED') AS failed_steps,
    (SELECT COUNT(*) FROM saga_compensation_log scl WHERE scl.saga_id = si.saga_id) AS compensation_count
FROM saga_instances si
WHERE si.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;

-- View for saga step performance
CREATE VIEW saga_step_performance AS
SELECT 
    ss.step_id,
    ss.saga_id,
    ss.step_name,
    ss.step_order,
    ss.step_type,
    ss.step_status,
    ss.service_name,
    ss.operation,
    ss.started_at,
    ss.completed_at,
    ss.failed_at,
    CASE 
        WHEN ss.step_status = 'COMPLETED' AND ss.completed_at IS NOT NULL 
        THEN ss.completed_at - ss.started_at 
        ELSE NULL 
    END AS processing_time,
    ss.retry_count,
    ss.failure_reason
FROM saga_steps ss
WHERE ss.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
ORDER BY ss.saga_id, ss.step_order;

-- View for saga error analysis
CREATE VIEW saga_error_analysis AS
SELECT 
    si.saga_type,
    ss.service_name,
    ss.operation,
    ss.failure_reason,
    COUNT(*) AS error_count,
    AVG(ss.retry_count) AS avg_retry_count,
    MAX(ss.retry_count) AS max_retry_count,
    DATE(ss.failed_at) AS error_date
FROM saga_instances si
JOIN saga_steps ss ON si.saga_id = ss.saga_id
WHERE ss.step_status = 'FAILED'
  AND si.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR
GROUP BY si.saga_type, ss.service_name, ss.operation, ss.failure_reason, DATE(ss.failed_at);

-- =====================================================
-- FUNCTIONS
-- =====================================================

-- Function to get saga statistics
CREATE OR REPLACE FUNCTION get_saga_stats(p_tenant_id VARCHAR(20), p_date_from DATE, p_date_to DATE)
RETURNS TABLE (
    total_sagas BIGINT,
    completed_sagas BIGINT,
    failed_sagas BIGINT,
    avg_processing_time INTERVAL,
    total_steps BIGINT,
    completed_steps BIGINT,
    failed_steps BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT AS total_sagas,
        COUNT(CASE WHEN si.saga_status = 'COMPLETED' THEN 1 END)::BIGINT AS completed_sagas,
        COUNT(CASE WHEN si.saga_status = 'FAILED' THEN 1 END)::BIGINT AS failed_sagas,
        AVG(CASE 
            WHEN si.saga_status = 'COMPLETED' AND si.completed_at IS NOT NULL 
            THEN si.completed_at - si.started_at 
        END) AS avg_processing_time,
        (SELECT COUNT(*) FROM saga_steps ss WHERE ss.saga_id = si.saga_id)::BIGINT AS total_steps,
        (SELECT COUNT(*) FROM saga_steps ss WHERE ss.saga_id = si.saga_id AND ss.step_status = 'COMPLETED')::BIGINT AS completed_steps,
        (SELECT COUNT(*) FROM saga_steps ss WHERE ss.saga_id = si.saga_id AND ss.step_status = 'FAILED')::BIGINT AS failed_steps
    FROM saga_instances si
    WHERE si.tenant_id = p_tenant_id
      AND DATE(si.started_at) BETWEEN p_date_from AND p_date_to;
END;
$$ LANGUAGE plpgsql;

-- Function to get saga by correlation ID
CREATE OR REPLACE FUNCTION get_saga_by_correlation_id(p_correlation_id VARCHAR(50))
RETURNS TABLE (
    saga_id VARCHAR(50),
    saga_type VARCHAR(100),
    saga_status VARCHAR(20),
    payment_id VARCHAR(50),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        si.saga_id,
        si.saga_type,
        si.saga_status,
        si.payment_id,
        si.started_at,
        si.completed_at,
        si.failed_at,
        si.failure_reason
    FROM saga_instances si
    WHERE si.correlation_id = p_correlation_id
      AND si.tenant_id = current_setting('app.current_tenant_id', true)::VARCHAR;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE saga_instances IS 'Main saga orchestration instances';
COMMENT ON TABLE saga_steps IS 'Individual steps within a saga';
COMMENT ON TABLE saga_events IS 'Event sourcing log for saga state changes';
COMMENT ON TABLE saga_compensation_log IS 'Compensation actions for failed saga steps';
COMMENT ON TABLE saga_timeouts IS 'Timeout tracking for saga steps and instances';
COMMENT ON TABLE saga_audit_log IS 'Audit trail for all saga operations';

COMMENT ON COLUMN saga_instances.saga_type IS 'Type of saga (PAYMENT_PROCESSING, etc.)';
COMMENT ON COLUMN saga_instances.saga_status IS 'Current status of the saga';
COMMENT ON COLUMN saga_instances.saga_data IS 'JSONB containing saga-specific data';
COMMENT ON COLUMN saga_instances.current_step IS 'Currently executing step';
COMMENT ON COLUMN saga_instances.completed_steps IS 'JSONB array of completed step IDs';
COMMENT ON COLUMN saga_instances.failed_steps IS 'JSONB array of failed step IDs';

COMMENT ON COLUMN saga_steps.step_type IS 'Type of step (ACTION or COMPENSATION)';
COMMENT ON COLUMN saga_steps.step_status IS 'Current status of the step';
COMMENT ON COLUMN saga_steps.service_name IS 'Target service for this step';
COMMENT ON COLUMN saga_steps.operation IS 'Operation to execute on the target service';
COMMENT ON COLUMN saga_steps.input_data IS 'JSONB containing step input data';
COMMENT ON COLUMN saga_steps.output_data IS 'JSONB containing step output data';
COMMENT ON COLUMN saga_steps.error_data IS 'JSONB containing step error data';

COMMENT ON COLUMN saga_events.event_type IS 'Type of saga event';
COMMENT ON COLUMN saga_events.event_data IS 'JSONB containing event-specific data';
COMMENT ON COLUMN saga_events.correlation_id IS 'Correlation ID for distributed tracing';
COMMENT ON COLUMN saga_events.causation_id IS 'ID of the event that caused this event';

COMMENT ON COLUMN saga_compensation_log.compensation_type IS 'Type of compensation (AUTOMATIC, MANUAL, SCHEDULED)';
COMMENT ON COLUMN saga_compensation_log.compensation_status IS 'Current status of the compensation';
COMMENT ON COLUMN saga_compensation_log.compensation_data IS 'JSONB containing compensation-specific data';

COMMENT ON COLUMN saga_timeouts.timeout_type IS 'Type of timeout (STEP_TIMEOUT, SAGA_TIMEOUT, COMPENSATION_TIMEOUT)';
COMMENT ON COLUMN saga_timeouts.timeout_duration_ms IS 'Timeout duration in milliseconds';
COMMENT ON COLUMN saga_timeouts.timeout_at IS 'When the timeout should trigger';
COMMENT ON COLUMN saga_timeouts.timeout_status IS 'Current status of the timeout';
