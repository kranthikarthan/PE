-- Migration: Add Advanced Payload Mapping System
-- Description: Creates tables for advanced payload mapping with flexible value assignment, derived values, conditional logic, and auto-generated IDs

-- Create advanced_payload_mappings table
CREATE TABLE IF NOT EXISTS payment_engine.advanced_payload_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mapping_name VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50),
    local_instrumentation_code VARCHAR(50),
    clearing_system_code VARCHAR(50),
    mapping_type VARCHAR(50) NOT NULL CHECK (mapping_type IN (
        'FIELD_MAPPING', 'OBJECT_MAPPING', 'ARRAY_MAPPING', 'NESTED_MAPPING', 
        'CONDITIONAL_MAPPING', 'TRANSFORMATION_MAPPING', 'VALUE_ASSIGNMENT_MAPPING', 
        'DERIVED_VALUE_MAPPING', 'AUTO_GENERATION_MAPPING', 'CUSTOM_MAPPING'
    )),
    direction VARCHAR(20) NOT NULL CHECK (direction IN ('REQUEST', 'RESPONSE', 'BIDIRECTIONAL')),
    source_schema JSONB,
    target_schema JSONB,
    field_mappings JSONB,
    value_assignments JSONB,
    conditional_mappings JSONB,
    derived_value_rules JSONB,
    auto_generation_rules JSONB,
    transformation_rules JSONB,
    validation_rules JSONB,
    default_values JSONB,
    array_handling_config JSONB,
    nested_object_config JSONB,
    error_handling_config JSONB,
    performance_config JSONB,
    version VARCHAR(20) DEFAULT '1.0',
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for advanced_payload_mappings
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_tenant_id ON payment_engine.advanced_payload_mappings(tenant_id);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_payment_type ON payment_engine.advanced_payload_mappings(payment_type);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_local_instrument ON payment_engine.advanced_payload_mappings(local_instrumentation_code);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_clearing_system ON payment_engine.advanced_payload_mappings(clearing_system_code);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_mapping_type ON payment_engine.advanced_payload_mappings(mapping_type);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_direction ON payment_engine.advanced_payload_mappings(direction);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_active ON payment_engine.advanced_payload_mappings(is_active);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_priority ON payment_engine.advanced_payload_mappings(priority);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_version ON payment_engine.advanced_payload_mappings(version);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_name ON payment_engine.advanced_payload_mappings(mapping_name);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_tenant_payment ON payment_engine.advanced_payload_mappings(tenant_id, payment_type);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_tenant_instrument ON payment_engine.advanced_payload_mappings(tenant_id, local_instrumentation_code);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_tenant_clearing ON payment_engine.advanced_payload_mappings(tenant_id, clearing_system_code);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_tenant_direction ON payment_engine.advanced_payload_mappings(tenant_id, direction);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_tenant_type ON payment_engine.advanced_payload_mappings(tenant_id, mapping_type);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_tenant_active ON payment_engine.advanced_payload_mappings(tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_advanced_mapping_priority_active ON payment_engine.advanced_payload_mappings(priority, is_active);

-- Create unique constraint for mapping name and tenant
CREATE UNIQUE INDEX IF NOT EXISTS idx_advanced_mapping_unique ON payment_engine.advanced_payload_mappings(tenant_id, mapping_name) WHERE is_active = true;

-- Create trigger for updated_at
CREATE TRIGGER update_advanced_payload_mappings_updated_at 
    BEFORE UPDATE ON payment_engine.advanced_payload_mappings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample advanced payload mappings
INSERT INTO payment_engine.advanced_payload_mappings (
    mapping_name, tenant_id, payment_type, local_instrumentation_code, mapping_type, direction,
    field_mappings, value_assignments, derived_value_rules, auto_generation_rules, 
    conditional_mappings, transformation_rules, default_values, priority, description, created_by, updated_by
) VALUES 
(
    'PAIN001_TO_PACS008_MAPPING',
    'tenant1',
    'TRANSFER',
    'LOCAL_INSTRUMENT_001',
    'TRANSFORMATION_MAPPING',
    'REQUEST',
    '{
        "transactionReference": "transactionReference",
        "fromAccountNumber": "fromAccountNumber",
        "toAccountNumber": "toAccountNumber",
        "amount": "amount",
        "currency": "currency",
        "description": "description"
    }',
    '{
        "messageId": "PAIN001-{{uuid()}}",
        "creationDateTime": "{{timestamp()}}",
        "messageType": "pain.001",
        "version": "2013",
        "source": "payment-engine"
    }',
    '{
        "totalAmount": {
            "expression": "${source.amount}",
            "type": "NUMBER"
        },
        "formattedAmount": {
            "expression": "${source.amount} * 100",
            "type": "NUMBER"
        },
        "displayAmount": {
            "expression": "${source.currency} ${source.amount}",
            "type": "STRING"
        }
    }',
    '{
        "messageId": {
            "type": "UUID"
        },
        "creationDateTime": {
            "type": "TIMESTAMP"
        },
        "transactionId": {
            "type": "SEQUENTIAL",
            "prefix": "TXN-",
            "suffix": "-PAIN001",
            "length": 15
        }
    }',
    '{
        "paymentType == \"TRANSFER\"": {
            "target": "paymentTypeCode",
            "source": "TRA"
        },
        "paymentType == \"PAYMENT\"": {
            "target": "paymentTypeCode",
            "source": "PAY"
        },
        "amount > 10000": {
            "target": "requiresApproval",
            "source": "true"
        }
    }',
    '{
        "transactionReference": "uppercase",
        "fromAccountNumber": "uppercase",
        "toAccountNumber": "uppercase",
        "currency": "uppercase"
    }',
    '{
        "processingMode": "IMMEDIATE",
        "priority": "NORMAL",
        "channel": "API"
    }',
    1,
    'Advanced mapping for PAIN.001 to PACS.008 transformation with value assignments and derived values',
    'system',
    'system'
),
(
    'PACS008_TO_PACS002_MAPPING',
    'tenant1',
    'TRANSFER',
    'LOCAL_INSTRUMENT_001',
    'TRANSFORMATION_MAPPING',
    'RESPONSE',
    '{
        "transactionReference": "transactionReference",
        "status": "status",
        "statusMessage": "statusMessage",
        "coreBankingReference": "coreBankingReference"
    }',
    '{
        "messageId": "PACS002-{{uuid()}}",
        "creationDateTime": "{{timestamp()}}",
        "messageType": "pacs.002",
        "version": "2013",
        "source": "clearing-system"
    }',
    '{
        "responseCode": {
            "expression": "${source.status} == \"SUCCESS\" ? \"ACSP\" : \"RJCT\"",
            "type": "STRING"
        },
        "responseMessage": {
            "expression": "${source.status} == \"SUCCESS\" ? \"Accepted\" : \"Rejected\"",
            "type": "STRING"
        }
    }',
    '{
        "messageId": {
            "type": "UUID"
        },
        "creationDateTime": {
            "type": "TIMESTAMP"
        },
        "responseId": {
            "type": "SEQUENTIAL",
            "prefix": "RESP-",
            "suffix": "-PACS002",
            "length": 15
        }
    }',
    '{
        "status == \"SUCCESS\"": {
            "target": "responseCode",
            "source": "ACSP"
        },
        "status == \"FAILED\"": {
            "target": "responseCode",
            "source": "RJCT"
        }
    }',
    '{
        "status": "uppercase",
        "statusMessage": "trim"
    }',
    '{
        "processingMode": "IMMEDIATE",
        "priority": "NORMAL"
    }',
    1,
    'Advanced mapping for PACS.008 to PACS.002 response transformation',
    'system',
    'system'
),
(
    'CLEARING_SYSTEM_MAPPING',
    'tenant1',
    'TRANSFER',
    'LOCAL_INSTRUMENT_002',
    'VALUE_ASSIGNMENT_MAPPING',
    'REQUEST',
    '{
        "transactionReference": "transactionReference",
        "amount": "amount",
        "currency": "currency"
    }',
    '{
        "clearingSystemCode": "CLEARING_001",
        "routingCode": "ROUTE_001",
        "institutionId": "INST_001",
        "messageFormat": "ISO20022",
        "protocol": "REST",
        "endpoint": "/api/v1/clearing/process"
    }',
    '{
        "clearingReference": {
            "expression": "CLEARING_001-${source.transactionReference}",
            "type": "STRING"
        },
        "routingInfo": {
            "expression": "${source.currency}-${source.amount}",
            "type": "STRING"
        }
    }',
    '{
        "clearingId": {
            "type": "UUID"
        },
        "timestamp": {
            "type": "TIMESTAMP"
        }
    }',
    '{
        "currency == \"USD\"": {
            "target": "clearingSystemCode",
            "source": "CLEARING_USD"
        },
        "currency == \"EUR\"": {
            "target": "clearingSystemCode",
            "source": "CLEARING_EUR"
        }
    }',
    '{
        "transactionReference": "uppercase",
        "currency": "uppercase"
    }',
    '{
        "timeout": 30000,
        "retryAttempts": 3
    }',
    2,
    'Clearing system specific mapping with value assignments',
    'system',
    'system'
),
(
    'TENANT_SPECIFIC_MAPPING',
    'tenant2',
    'PAYMENT',
    'LOCAL_INSTRUMENT_003',
    'DERIVED_VALUE_MAPPING',
    'BIDIRECTIONAL',
    '{
        "accountNumber": "accountNumber",
        "amount": "amount",
        "currency": "currency"
    }',
    '{
        "tenantId": "tenant2",
        "bankCode": "BANK_002",
        "region": "EUROPE"
    }',
    '{
        "accountType": {
            "expression": "${source.accountNumber} startsWith \"ACC\" ? \"BUSINESS\" : \"PERSONAL\"",
            "type": "STRING"
        },
        "processingFee": {
            "expression": "${source.amount} * 0.001",
            "type": "NUMBER"
        },
        "totalAmount": {
            "expression": "${source.amount} + (${source.amount} * 0.001)",
            "type": "NUMBER"
        }
    }',
    '{
        "requestId": {
            "type": "UUID"
        },
        "timestamp": {
            "type": "TIMESTAMP"
        }
    }',
    '{
        "accountType == \"BUSINESS\"": {
            "target": "requiresApproval",
            "source": "false"
        },
        "accountType == \"PERSONAL\"": {
            "target": "requiresApproval",
            "source": "true"
        }
    }',
    '{
        "accountNumber": "uppercase",
        "currency": "uppercase"
    }',
    '{
        "processingMode": "BATCH",
        "priority": "HIGH"
    }',
    1,
    'Tenant-specific mapping with derived values and conditional logic',
    'system',
    'system'
);

-- Add comments
COMMENT ON TABLE payment_engine.advanced_payload_mappings IS 'Advanced payload mapping configurations with flexible value assignment, derived values, conditional logic, and auto-generated IDs';

COMMENT ON COLUMN payment_engine.advanced_payload_mappings.mapping_type IS 'Type of mapping: FIELD_MAPPING, VALUE_ASSIGNMENT_MAPPING, DERIVED_VALUE_MAPPING, etc.';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.direction IS 'Direction of mapping: REQUEST, RESPONSE, or BIDIRECTIONAL';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.source_schema IS 'Source schema definition';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.target_schema IS 'Target schema definition';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.field_mappings IS 'Field mapping configuration';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.value_assignments IS 'Static value assignments';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.conditional_mappings IS 'Conditional mapping rules';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.derived_value_rules IS 'Derived value calculation rules';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.auto_generation_rules IS 'Auto-generation rules for IDs and timestamps';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.transformation_rules IS 'Data transformation rules';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.validation_rules IS 'Validation rules for payload';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.default_values IS 'Default values for fields';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.array_handling_config IS 'Array handling configuration';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.nested_object_config IS 'Nested object handling configuration';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.error_handling_config IS 'Error handling configuration for mapping';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.performance_config IS 'Performance optimization configuration';

-- Create view for active advanced payload mappings
CREATE OR REPLACE VIEW payment_engine.active_advanced_payload_mappings AS
SELECT 
    apm.*,
    CASE 
        WHEN apm.mapping_type = 'VALUE_ASSIGNMENT_MAPPING' THEN 'STATIC_VALUES'
        WHEN apm.mapping_type = 'DERIVED_VALUE_MAPPING' THEN 'DERIVED_VALUES'
        WHEN apm.mapping_type = 'AUTO_GENERATION_MAPPING' THEN 'AUTO_GENERATED'
        WHEN apm.mapping_type = 'CONDITIONAL_MAPPING' THEN 'CONDITIONAL'
        ELSE 'STANDARD'
    END as mapping_category,
    CASE 
        WHEN apm.direction = 'BIDIRECTIONAL' THEN 'BOTH'
        ELSE apm.direction
    END as effective_direction
FROM payment_engine.advanced_payload_mappings apm
WHERE apm.is_active = true;

-- Create view for advanced payload mapping statistics
CREATE OR REPLACE VIEW payment_engine.advanced_payload_mapping_statistics AS
SELECT 
    tenant_id,
    COUNT(*) as total_mappings,
    COUNT(CASE WHEN mapping_type = 'FIELD_MAPPING' THEN 1 END) as field_mappings,
    COUNT(CASE WHEN mapping_type = 'VALUE_ASSIGNMENT_MAPPING' THEN 1 END) as value_assignment_mappings,
    COUNT(CASE WHEN mapping_type = 'DERIVED_VALUE_MAPPING' THEN 1 END) as derived_value_mappings,
    COUNT(CASE WHEN mapping_type = 'AUTO_GENERATION_MAPPING' THEN 1 END) as auto_generation_mappings,
    COUNT(CASE WHEN mapping_type = 'CONDITIONAL_MAPPING' THEN 1 END) as conditional_mappings,
    COUNT(CASE WHEN mapping_type = 'TRANSFORMATION_MAPPING' THEN 1 END) as transformation_mappings,
    COUNT(CASE WHEN direction = 'REQUEST' THEN 1 END) as request_mappings,
    COUNT(CASE WHEN direction = 'RESPONSE' THEN 1 END) as response_mappings,
    COUNT(CASE WHEN direction = 'BIDIRECTIONAL' THEN 1 END) as bidirectional_mappings,
    COUNT(CASE WHEN payment_type IS NOT NULL THEN 1 END) as payment_type_specific,
    COUNT(CASE WHEN local_instrumentation_code IS NOT NULL THEN 1 END) as instrument_specific,
    COUNT(CASE WHEN clearing_system_code IS NOT NULL THEN 1 END) as clearing_system_specific,
    AVG(priority) as average_priority,
    MAX(created_at) as last_mapping_created,
    MAX(updated_at) as last_mapping_updated
FROM payment_engine.advanced_payload_mappings
WHERE is_active = true
GROUP BY tenant_id;

-- Create function to get mappings for specific criteria
CREATE OR REPLACE FUNCTION payment_engine.get_advanced_mappings(
    p_tenant_id VARCHAR(50),
    p_payment_type VARCHAR(50) DEFAULT NULL,
    p_local_instrumentation_code VARCHAR(50) DEFAULT NULL,
    p_clearing_system_code VARCHAR(50) DEFAULT NULL,
    p_direction VARCHAR(20) DEFAULT NULL
)
RETURNS TABLE (
    id UUID,
    mapping_name VARCHAR(100),
    mapping_type VARCHAR(50),
    direction VARCHAR(20),
    priority INTEGER,
    description VARCHAR(1000)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        apm.id,
        apm.mapping_name,
        apm.mapping_type,
        apm.direction,
        apm.priority,
        apm.description
    FROM payment_engine.advanced_payload_mappings apm
    WHERE apm.tenant_id = p_tenant_id
        AND apm.is_active = true
        AND (p_payment_type IS NULL OR apm.payment_type = p_payment_type)
        AND (p_local_instrumentation_code IS NULL OR apm.local_instrumentation_code = p_local_instrumentation_code)
        AND (p_clearing_system_code IS NULL OR apm.clearing_system_code = p_clearing_system_code)
        AND (p_direction IS NULL OR apm.direction = p_direction OR apm.direction = 'BIDIRECTIONAL')
    ORDER BY apm.priority ASC, apm.created_at ASC;
END;
$$ LANGUAGE plpgsql;

-- Create function to get mapping statistics for a tenant
CREATE OR REPLACE FUNCTION payment_engine.get_tenant_mapping_statistics(p_tenant_id VARCHAR(50))
RETURNS TABLE (
    total_mappings BIGINT,
    field_mappings BIGINT,
    value_assignment_mappings BIGINT,
    derived_value_mappings BIGINT,
    auto_generation_mappings BIGINT,
    conditional_mappings BIGINT,
    transformation_mappings BIGINT,
    request_mappings BIGINT,
    response_mappings BIGINT,
    bidirectional_mappings BIGINT,
    payment_type_specific BIGINT,
    instrument_specific BIGINT,
    clearing_system_specific BIGINT,
    average_priority NUMERIC,
    last_mapping_created TIMESTAMP,
    last_mapping_updated TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_mappings,
        COUNT(CASE WHEN mapping_type = 'FIELD_MAPPING' THEN 1 END) as field_mappings,
        COUNT(CASE WHEN mapping_type = 'VALUE_ASSIGNMENT_MAPPING' THEN 1 END) as value_assignment_mappings,
        COUNT(CASE WHEN mapping_type = 'DERIVED_VALUE_MAPPING' THEN 1 END) as derived_value_mappings,
        COUNT(CASE WHEN mapping_type = 'AUTO_GENERATION_MAPPING' THEN 1 END) as auto_generation_mappings,
        COUNT(CASE WHEN mapping_type = 'CONDITIONAL_MAPPING' THEN 1 END) as conditional_mappings,
        COUNT(CASE WHEN mapping_type = 'TRANSFORMATION_MAPPING' THEN 1 END) as transformation_mappings,
        COUNT(CASE WHEN direction = 'REQUEST' THEN 1 END) as request_mappings,
        COUNT(CASE WHEN direction = 'RESPONSE' THEN 1 END) as response_mappings,
        COUNT(CASE WHEN direction = 'BIDIRECTIONAL' THEN 1 END) as bidirectional_mappings,
        COUNT(CASE WHEN payment_type IS NOT NULL THEN 1 END) as payment_type_specific,
        COUNT(CASE WHEN local_instrumentation_code IS NOT NULL THEN 1 END) as instrument_specific,
        COUNT(CASE WHEN clearing_system_code IS NOT NULL THEN 1 END) as clearing_system_specific,
        AVG(priority) as average_priority,
        MAX(created_at) as last_mapping_created,
        MAX(updated_at) as last_mapping_updated
    FROM payment_engine.advanced_payload_mappings
    WHERE tenant_id = p_tenant_id AND is_active = true;
END;
$$ LANGUAGE plpgsql;