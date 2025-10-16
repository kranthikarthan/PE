-- Validation Service Database Schema
-- Creates tables for validation results, rules, and audit trail

-- Validation Results Table
CREATE TABLE validation_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    validation_id VARCHAR(255) NOT NULL UNIQUE,
    payment_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PASSED', 'FAILED')),
    risk_level VARCHAR(50) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    fraud_score INTEGER DEFAULT 0 CHECK (fraud_score >= 0 AND fraud_score <= 100),
    risk_score INTEGER DEFAULT 0 CHECK (risk_score >= 0 AND risk_score <= 100),
    applied_rules TEXT[], -- Array of rule IDs that were applied
    validation_metadata JSONB, -- Additional validation metadata
    validated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(255),
    created_by VARCHAR(255) DEFAULT 'validation-service'
);

-- Validation Failed Rules Table
CREATE TABLE validation_failed_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    validation_result_id UUID NOT NULL REFERENCES validation_results(id) ON DELETE CASCADE,
    rule_id VARCHAR(255) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('BUSINESS', 'COMPLIANCE', 'FRAUD', 'RISK')),
    failure_reason TEXT NOT NULL,
    rule_metadata JSONB, -- Additional rule execution metadata
    failed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Validation Rules Configuration Table
CREATE TABLE validation_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id VARCHAR(255) NOT NULL UNIQUE,
    rule_name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('BUSINESS', 'COMPLIANCE', 'FRAUD', 'RISK')),
    rule_definition TEXT NOT NULL, -- Rule logic/expression
    rule_priority INTEGER DEFAULT 100,
    is_active BOOLEAN DEFAULT true,
    tenant_id VARCHAR(255), -- NULL means global rule
    business_unit_id VARCHAR(255), -- NULL means tenant-wide rule
    effective_from TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255)
);

-- Validation Audit Trail Table
CREATE TABLE validation_audit_trail (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    validation_result_id UUID REFERENCES validation_results(id),
    payment_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL, -- VALIDATION_STARTED, VALIDATION_COMPLETED, RULE_EXECUTED, etc.
    details JSONB, -- Action-specific details
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(255) DEFAULT 'validation-service',
    correlation_id VARCHAR(255)
);

-- Fraud Detection Results Table
CREATE TABLE fraud_detection_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    validation_result_id UUID REFERENCES validation_results(id),
    payment_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    fraud_score INTEGER NOT NULL CHECK (fraud_score >= 0 AND fraud_score <= 100),
    fraud_reasons TEXT[], -- Array of fraud detection reasons
    fraud_metadata JSONB, -- Additional fraud detection metadata
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(255)
);

-- Risk Assessment Results Table
CREATE TABLE risk_assessment_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    validation_result_id UUID REFERENCES validation_results(id),
    payment_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    risk_level VARCHAR(50) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    risk_score INTEGER NOT NULL CHECK (risk_score >= 0 AND risk_score <= 100),
    risk_factors JSONB, -- Risk assessment factors and scores
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(255)
);

-- Indexes for Performance
CREATE INDEX idx_validation_results_payment_id ON validation_results(payment_id);
CREATE INDEX idx_validation_results_tenant_id ON validation_results(tenant_id);
CREATE INDEX idx_validation_results_business_unit_id ON validation_results(business_unit_id);
CREATE INDEX idx_validation_results_validated_at ON validation_results(validated_at);
CREATE INDEX idx_validation_results_correlation_id ON validation_results(correlation_id);

CREATE INDEX idx_validation_failed_rules_validation_result_id ON validation_failed_rules(validation_result_id);
CREATE INDEX idx_validation_failed_rules_rule_id ON validation_failed_rules(rule_id);

CREATE INDEX idx_validation_rules_tenant_id ON validation_rules(tenant_id);
CREATE INDEX idx_validation_rules_business_unit_id ON validation_rules(business_unit_id);
CREATE INDEX idx_validation_rules_rule_type ON validation_rules(rule_type);
CREATE INDEX idx_validation_rules_is_active ON validation_rules(is_active);
CREATE INDEX idx_validation_rules_effective_from ON validation_rules(effective_from);

CREATE INDEX idx_validation_audit_trail_payment_id ON validation_audit_trail(payment_id);
CREATE INDEX idx_validation_audit_trail_tenant_id ON validation_audit_trail(tenant_id);
CREATE INDEX idx_validation_audit_trail_performed_at ON validation_audit_trail(performed_at);
CREATE INDEX idx_validation_audit_trail_correlation_id ON validation_audit_trail(correlation_id);

CREATE INDEX idx_fraud_detection_results_payment_id ON fraud_detection_results(payment_id);
CREATE INDEX idx_fraud_detection_results_tenant_id ON fraud_detection_results(tenant_id);
CREATE INDEX idx_fraud_detection_results_detected_at ON fraud_detection_results(detected_at);

CREATE INDEX idx_risk_assessment_results_payment_id ON risk_assessment_results(payment_id);
CREATE INDEX idx_risk_assessment_results_tenant_id ON risk_assessment_results(tenant_id);
CREATE INDEX idx_risk_assessment_results_assessed_at ON risk_assessment_results(assessed_at);

-- Update triggers for updated_at columns
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_validation_results_updated_at 
    BEFORE UPDATE ON validation_results 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_validation_rules_updated_at 
    BEFORE UPDATE ON validation_rules 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
