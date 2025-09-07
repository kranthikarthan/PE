-- Migration: Add Fraud/Risk Monitoring System
-- Description: Creates tables for fraud/risk monitoring configurations and assessments
-- Version: 008
-- Date: 2024-01-15

-- Create fraud_risk_configurations table
CREATE TABLE IF NOT EXISTS payment_engine.fraud_risk_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    configuration_name VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50),
    local_instrumentation_code VARCHAR(50),
    clearing_system_code VARCHAR(50),
    payment_source VARCHAR(20) NOT NULL CHECK (payment_source IN ('BANK_CLIENT', 'CLEARING_SYSTEM', 'BOTH')),
    risk_assessment_type VARCHAR(50) NOT NULL CHECK (risk_assessment_type IN ('REAL_TIME', 'BATCH', 'HYBRID', 'CUSTOM')),
    external_api_config JSONB,
    risk_rules JSONB,
    decision_criteria JSONB,
    thresholds JSONB,
    timeout_config JSONB,
    retry_config JSONB,
    circuit_breaker_config JSONB,
    fallback_config JSONB,
    monitoring_config JSONB,
    alerting_config JSONB,
    is_enabled BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 1,
    version VARCHAR(20) DEFAULT '1.0',
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create fraud_risk_assessments table
CREATE TABLE IF NOT EXISTS payment_engine.fraud_risk_assessments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id VARCHAR(100) NOT NULL UNIQUE,
    transaction_reference VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50),
    local_instrumentation_code VARCHAR(50),
    clearing_system_code VARCHAR(50),
    payment_source VARCHAR(20) NOT NULL CHECK (payment_source IN ('BANK_CLIENT', 'CLEARING_SYSTEM', 'BOTH')),
    risk_assessment_type VARCHAR(50) NOT NULL CHECK (risk_assessment_type IN ('REAL_TIME', 'BATCH', 'HYBRID', 'CUSTOM')),
    configuration_id UUID REFERENCES payment_engine.fraud_risk_configurations(id),
    external_api_used VARCHAR(100),
    risk_score DECIMAL(5,4),
    risk_level VARCHAR(20) CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    decision VARCHAR(20) NOT NULL CHECK (decision IN ('APPROVE', 'REJECT', 'MANUAL_REVIEW', 'HOLD', 'ESCALATE')),
    decision_reason VARCHAR(500),
    external_api_request JSONB,
    external_api_response JSONB,
    risk_factors JSONB,
    assessment_details JSONB,
    processing_time_ms BIGINT,
    external_api_response_time_ms BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'ERROR', 'TIMEOUT', 'CANCELLED')),
    error_message VARCHAR(1000),
    retry_count INTEGER DEFAULT 0,
    assessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for fraud_risk_configurations
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_tenant_id ON payment_engine.fraud_risk_configurations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_payment_type ON payment_engine.fraud_risk_configurations(payment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_local_instrument ON payment_engine.fraud_risk_configurations(local_instrumentation_code);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_clearing_system ON payment_engine.fraud_risk_configurations(clearing_system_code);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_payment_source ON payment_engine.fraud_risk_configurations(payment_source);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_risk_assessment_type ON payment_engine.fraud_risk_configurations(risk_assessment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_enabled ON payment_engine.fraud_risk_configurations(is_enabled);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_priority ON payment_engine.fraud_risk_configurations(priority);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_created_at ON payment_engine.fraud_risk_configurations(created_at);

-- Create indexes for fraud_risk_assessments
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_assessment_id ON payment_engine.fraud_risk_assessments(assessment_id);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_transaction_reference ON payment_engine.fraud_risk_assessments(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_tenant_id ON payment_engine.fraud_risk_assessments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_payment_type ON payment_engine.fraud_risk_assessments(payment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_local_instrument ON payment_engine.fraud_risk_assessments(local_instrumentation_code);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_clearing_system ON payment_engine.fraud_risk_assessments(clearing_system_code);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_payment_source ON payment_engine.fraud_risk_assessments(payment_source);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_risk_assessment_type ON payment_engine.fraud_risk_assessments(risk_assessment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_configuration_id ON payment_engine.fraud_risk_assessments(configuration_id);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_external_api_used ON payment_engine.fraud_risk_assessments(external_api_used);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_risk_score ON payment_engine.fraud_risk_assessments(risk_score);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_risk_level ON payment_engine.fraud_risk_assessments(risk_level);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_decision ON payment_engine.fraud_risk_assessments(decision);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_status ON payment_engine.fraud_risk_assessments(status);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_assessed_at ON payment_engine.fraud_risk_assessments(assessed_at);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_expires_at ON payment_engine.fraud_risk_assessments(expires_at);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_retry_count ON payment_engine.fraud_risk_assessments(retry_count);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_tenant_payment_source ON payment_engine.fraud_risk_configurations(tenant_id, payment_source);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_tenant_risk_type ON payment_engine.fraud_risk_configurations(tenant_id, risk_assessment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_tenant_payment_type ON payment_engine.fraud_risk_configurations(tenant_id, payment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_tenant_local_instrument ON payment_engine.fraud_risk_configurations(tenant_id, local_instrumentation_code);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_tenant_clearing_system ON payment_engine.fraud_risk_configurations(tenant_id, clearing_system_code);

CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_tenant_status ON payment_engine.fraud_risk_assessments(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_tenant_decision ON payment_engine.fraud_risk_assessments(tenant_id, decision);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_tenant_risk_level ON payment_engine.fraud_risk_assessments(tenant_id, risk_level);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_tenant_payment_source ON payment_engine.fraud_risk_assessments(tenant_id, payment_source);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_tenant_risk_type ON payment_engine.fraud_risk_assessments(tenant_id, risk_assessment_type);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_tenant_assessed_at ON payment_engine.fraud_risk_assessments(tenant_id, assessed_at);

-- Create partial indexes for performance
CREATE INDEX IF NOT EXISTS idx_fraud_risk_configurations_enabled_active ON payment_engine.fraud_risk_configurations(tenant_id, priority) WHERE is_enabled = true;
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_pending ON payment_engine.fraud_risk_assessments(tenant_id, assessed_at) WHERE status IN ('PENDING', 'IN_PROGRESS');
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_completed ON payment_engine.fraud_risk_assessments(tenant_id, assessed_at) WHERE status = 'COMPLETED';
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_failed ON payment_engine.fraud_risk_assessments(tenant_id, updated_at) WHERE status IN ('FAILED', 'ERROR', 'TIMEOUT');
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_approved ON payment_engine.fraud_risk_assessments(tenant_id, assessed_at) WHERE decision = 'APPROVE';
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_rejected ON payment_engine.fraud_risk_assessments(tenant_id, assessed_at) WHERE decision = 'REJECT';
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_manual_review ON payment_engine.fraud_risk_assessments(tenant_id, assessed_at) WHERE decision = 'MANUAL_REVIEW';
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_high_risk ON payment_engine.fraud_risk_assessments(tenant_id, risk_score) WHERE risk_level = 'HIGH';
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_critical_risk ON payment_engine.fraud_risk_assessments(tenant_id, risk_score) WHERE risk_level = 'CRITICAL';
CREATE INDEX IF NOT EXISTS idx_fraud_risk_assessments_expired ON payment_engine.fraud_risk_assessments(expires_at) WHERE expires_at IS NOT NULL;

-- Create triggers for updated_at
CREATE OR REPLACE FUNCTION update_fraud_risk_configurations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_fraud_risk_assessments_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_fraud_risk_configurations_updated_at
    BEFORE UPDATE ON payment_engine.fraud_risk_configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_fraud_risk_configurations_updated_at();

CREATE TRIGGER trigger_fraud_risk_assessments_updated_at
    BEFORE UPDATE ON payment_engine.fraud_risk_assessments
    FOR EACH ROW
    EXECUTE FUNCTION update_fraud_risk_assessments_updated_at();

-- Insert sample fraud/risk configurations
INSERT INTO payment_engine.fraud_risk_configurations (
    configuration_name,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    payment_source,
    risk_assessment_type,
    external_api_config,
    risk_rules,
    decision_criteria,
    thresholds,
    timeout_config,
    retry_config,
    circuit_breaker_config,
    fallback_config,
    monitoring_config,
    alerting_config,
    is_enabled,
    priority,
    version,
    description,
    created_by,
    updated_by
) VALUES 
(
    'Default Bank Client Real-time Assessment',
    'tenant-001',
    NULL,
    NULL,
    'BANK_CLIENT',
    'REAL_TIME',
    '{
        "apiName": "FICO_FALCON",
        "apiUrl": "https://api.fico.com/falcon/v1/assess",
        "httpMethod": "POST",
        "headers": {
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        "authentication": {
            "type": "API_KEY",
            "apiKey": "fico-api-key-123",
            "apiKeyHeader": "X-API-Key"
        },
        "requestTemplate": {
            "transactionId": "${transactionReference}",
            "amount": "${amount}",
            "currency": "${currency}",
            "accountNumber": "${fromAccountNumber}",
            "beneficiaryAccount": "${toAccountNumber}"
        }
    }',
    '{
        "amountRules": {
            "highAmountThreshold": 10000,
            "veryHighAmountThreshold": 50000,
            "roundAmountRisk": true
        },
        "frequencyRules": {
            "dailyThreshold": 10,
            "weeklyThreshold": 50
        },
        "locationRules": {
            "highRiskCountries": ["XX", "YY"],
            "suspiciousIpRanges": ["192.168.0.0/16", "10.0.0.0/8"]
        },
        "timeRules": {
            "offHoursRisk": true,
            "weekendRisk": true
        },
        "accountRules": {
            "newAccountRisk": true,
            "crossBorderRisk": true
        },
        "deviceRules": {
            "newDeviceRisk": true,
            "suspiciousUserAgents": ["bot", "crawler", "scraper"]
        },
        "patternRules": {
            "suspiciousReferencePatterns": ["TEST.*", "DEMO.*"],
            "suspiciousDescriptionPatterns": ["test.*", "demo.*"]
        }
    }',
    '{
        "autoApprove": {
            "maxRiskScore": 0.3,
            "maxAmount": 1000,
            "condition_1": "${amount} < 1000 AND ${riskScore} < 0.3"
        },
        "autoReject": {
            "minRiskScore": 0.8,
            "minAmount": 100000,
            "condition_1": "${riskScore} >= 0.8 OR ${amount} >= 100000"
        },
        "manualReview": {
            "minRiskScore": 0.5,
            "condition_1": "${riskScore} >= 0.5 AND ${riskScore} < 0.8"
        }
    }',
    '{
        "approveThreshold": 0.3,
        "rejectThreshold": 0.8,
        "manualReviewThreshold": 0.5,
        "holdThreshold": 0.7,
        "escalateThreshold": 0.9
    }',
    '{
        "timeoutSeconds": 30,
        "timeoutMinutes": 1
    }',
    '{
        "maxRetries": 3,
        "retryDelayMs": 1000,
        "backoffMultiplier": 2
    }',
    '{
        "enabled": true,
        "failureThreshold": 5,
        "recoveryTimeout": 60000,
        "halfOpenMaxCalls": 3
    }',
    '{
        "decision": "MANUAL_REVIEW",
        "riskLevel": "MEDIUM",
        "reason": "External API unavailable"
    }',
    '{
        "enabled": true,
        "metricsEnabled": true,
        "loggingEnabled": true
    }',
    '{
        "enabled": true,
        "highRiskAlerts": true,
        "criticalRiskAlerts": true,
        "apiFailureAlerts": true
    }',
    true,
    1,
    '1.0',
    'Default fraud/risk assessment configuration for bank client payments',
    'system',
    'system'
),
(
    'Default Clearing System Real-time Assessment',
    'tenant-001',
    NULL,
    NULL,
    'CLEARING_SYSTEM',
    'REAL_TIME',
    '{
        "apiName": "SAS_FRAUD_MANAGEMENT",
        "apiUrl": "https://api.sas.com/fraud/v1/assess",
        "httpMethod": "POST",
        "headers": {
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        "authentication": {
            "type": "BEARER_TOKEN",
            "bearerToken": "sas-bearer-token-456"
        },
        "requestTemplate": {
            "transactionId": "${transactionReference}",
            "amount": "${amount}",
            "currency": "${currency}",
            "customerId": "${customerId}",
            "accountNumber": "${fromAccountNumber}",
            "beneficiaryAccount": "${toAccountNumber}",
            "paymentType": "${paymentType}",
            "clearingSystem": "${clearingSystemCode}"
        }
    }',
    '{
        "amountRules": {
            "highAmountThreshold": 25000,
            "veryHighAmountThreshold": 100000,
            "roundAmountRisk": true
        },
        "frequencyRules": {
            "dailyThreshold": 20,
            "weeklyThreshold": 100
        },
        "locationRules": {
            "highRiskCountries": ["XX", "YY", "ZZ"],
            "suspiciousIpRanges": ["192.168.0.0/16", "10.0.0.0/8", "172.16.0.0/12"]
        },
        "timeRules": {
            "offHoursRisk": false,
            "weekendRisk": false
        },
        "accountRules": {
            "newAccountRisk": true,
            "crossBorderRisk": true
        },
        "deviceRules": {
            "newDeviceRisk": false,
            "suspiciousUserAgents": ["bot", "crawler", "scraper"]
        },
        "patternRules": {
            "suspiciousReferencePatterns": ["TEST.*", "DEMO.*", "CLEARING.*"],
            "suspiciousDescriptionPatterns": ["test.*", "demo.*", "clearing.*"]
        }
    }',
    '{
        "autoApprove": {
            "maxRiskScore": 0.4,
            "maxAmount": 5000,
            "condition_1": "${amount} < 5000 AND ${riskScore} < 0.4"
        },
        "autoReject": {
            "minRiskScore": 0.7,
            "minAmount": 200000,
            "condition_1": "${riskScore} >= 0.7 OR ${amount} >= 200000"
        },
        "manualReview": {
            "minRiskScore": 0.4,
            "condition_1": "${riskScore} >= 0.4 AND ${riskScore} < 0.7"
        }
    }',
    '{
        "approveThreshold": 0.4,
        "rejectThreshold": 0.7,
        "manualReviewThreshold": 0.4,
        "holdThreshold": 0.6,
        "escalateThreshold": 0.8
    }',
    '{
        "timeoutSeconds": 45,
        "timeoutMinutes": 1
    }',
    '{
        "maxRetries": 2,
        "retryDelayMs": 2000,
        "backoffMultiplier": 2
    }',
    '{
        "enabled": true,
        "failureThreshold": 3,
        "recoveryTimeout": 30000,
        "halfOpenMaxCalls": 2
    }',
    '{
        "decision": "MANUAL_REVIEW",
        "riskLevel": "HIGH",
        "reason": "External API unavailable"
    }',
    '{
        "enabled": true,
        "metricsEnabled": true,
        "loggingEnabled": true
    }',
    '{
        "enabled": true,
        "highRiskAlerts": true,
        "criticalRiskAlerts": true,
        "apiFailureAlerts": true
    }',
    true,
    1,
    '1.0',
    'Default fraud/risk assessment configuration for clearing system payments',
    'system',
    'system'
);

-- Create views for common queries
CREATE OR REPLACE VIEW payment_engine.fraud_risk_configurations_active AS
SELECT 
    id,
    configuration_name,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    payment_source,
    risk_assessment_type,
    is_enabled,
    priority,
    version,
    description,
    created_at,
    updated_at
FROM payment_engine.fraud_risk_configurations
WHERE is_enabled = true
ORDER BY priority ASC, created_at ASC;

CREATE OR REPLACE VIEW payment_engine.fraud_risk_assessments_recent AS
SELECT 
    id,
    assessment_id,
    transaction_reference,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    payment_source,
    risk_assessment_type,
    external_api_used,
    risk_score,
    risk_level,
    decision,
    decision_reason,
    status,
    processing_time_ms,
    external_api_response_time_ms,
    assessed_at,
    created_at
FROM payment_engine.fraud_risk_assessments
WHERE assessed_at >= CURRENT_TIMESTAMP - INTERVAL '7 days'
ORDER BY assessed_at DESC;

CREATE OR REPLACE VIEW payment_engine.fraud_risk_assessments_pending_review AS
SELECT 
    id,
    assessment_id,
    transaction_reference,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    payment_source,
    risk_assessment_type,
    risk_score,
    risk_level,
    decision,
    decision_reason,
    assessed_at,
    created_at
FROM payment_engine.fraud_risk_assessments
WHERE decision = 'MANUAL_REVIEW'
ORDER BY assessed_at ASC;

CREATE OR REPLACE VIEW payment_engine.fraud_risk_assessments_high_risk AS
SELECT 
    id,
    assessment_id,
    transaction_reference,
    tenant_id,
    payment_type,
    local_instrumentation_code,
    clearing_system_code,
    payment_source,
    risk_assessment_type,
    risk_score,
    risk_level,
    decision,
    decision_reason,
    assessed_at,
    created_at
FROM payment_engine.fraud_risk_assessments
WHERE risk_level IN ('HIGH', 'CRITICAL')
ORDER BY risk_score DESC, assessed_at DESC;

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON payment_engine.fraud_risk_configurations TO payment_engine_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON payment_engine.fraud_risk_assessments TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_risk_configurations_active TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_risk_assessments_recent TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_risk_assessments_pending_review TO payment_engine_user;
GRANT SELECT ON payment_engine.fraud_risk_assessments_high_risk TO payment_engine_user;

-- Add comments
COMMENT ON TABLE payment_engine.fraud_risk_configurations IS 'Configuration for fraud/risk monitoring and assessment';
COMMENT ON TABLE payment_engine.fraud_risk_assessments IS 'Results of fraud/risk assessments performed on payments';

COMMENT ON COLUMN payment_engine.fraud_risk_configurations.configuration_name IS 'Unique name for the fraud/risk configuration';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.payment_source IS 'Source of payment: BANK_CLIENT, CLEARING_SYSTEM, or BOTH';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.risk_assessment_type IS 'Type of risk assessment: REAL_TIME, BATCH, HYBRID, or CUSTOM';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.external_api_config IS 'Configuration for external fraud API integration';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.risk_rules IS 'Rules for risk evaluation and scoring';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.decision_criteria IS 'Criteria for automatic decision making';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.thresholds IS 'Risk score thresholds for different decisions';
COMMENT ON COLUMN payment_engine.fraud_risk_configurations.priority IS 'Priority order for applying configurations (lower number = higher priority)';

COMMENT ON COLUMN payment_engine.fraud_risk_assessments.assessment_id IS 'Unique identifier for the assessment';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.transaction_reference IS 'Reference to the payment transaction being assessed';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.risk_score IS 'Calculated risk score (0.0 to 1.0)';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.risk_level IS 'Risk level: LOW, MEDIUM, HIGH, or CRITICAL';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.decision IS 'Final decision: APPROVE, REJECT, MANUAL_REVIEW, HOLD, or ESCALATE';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.external_api_request IS 'Request sent to external fraud API';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.external_api_response IS 'Response received from external fraud API';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.risk_factors IS 'Individual risk factors and their scores';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.processing_time_ms IS 'Total time taken to complete the assessment';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.external_api_response_time_ms IS 'Time taken for external API call';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.status IS 'Assessment status: PENDING, IN_PROGRESS, COMPLETED, FAILED, ERROR, TIMEOUT, or CANCELLED';
COMMENT ON COLUMN payment_engine.fraud_risk_assessments.expires_at IS 'When the assessment expires (for caching)';