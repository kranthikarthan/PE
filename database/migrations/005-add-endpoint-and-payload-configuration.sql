-- Migration: Add Endpoint and Payload Configuration Support
-- Description: Creates tables for dynamic endpoint configuration and payload schema mapping

-- Create core_banking_endpoint_configurations table
CREATE TABLE IF NOT EXISTS payment_engine.core_banking_endpoint_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    core_banking_config_id UUID NOT NULL REFERENCES payment_engine.core_banking_configurations(id) ON DELETE CASCADE,
    endpoint_name VARCHAR(100) NOT NULL,
    endpoint_type VARCHAR(50) NOT NULL CHECK (endpoint_type IN (
        'ACCOUNT_INFO', 'ACCOUNT_BALANCE', 'ACCOUNT_HOLDER', 'DEBIT_TRANSACTION', 
        'CREDIT_TRANSACTION', 'TRANSFER_TRANSACTION', 'TRANSACTION_STATUS', 
        'HOLD_FUNDS', 'RELEASE_FUNDS', 'ISO20022_PAYMENT', 'ISO20022_RESPONSE', 
        'ISO20022_VALIDATION', 'BATCH_TRANSACTIONS', 'RECONCILIATION', 
        'HEALTH_CHECK', 'CUSTOM'
    )),
    http_method VARCHAR(10) DEFAULT 'POST',
    endpoint_path VARCHAR(500) NOT NULL,
    base_url_override VARCHAR(500),
    request_headers JSONB,
    query_parameters JSONB,
    authentication_config JSONB,
    timeout_ms INTEGER,
    retry_attempts INTEGER,
    circuit_breaker_config JSONB,
    rate_limiting_config JSONB,
    request_transformation_config JSONB,
    response_transformation_config JSONB,
    validation_rules JSONB,
    error_handling_config JSONB,
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create payload_schema_mappings table
CREATE TABLE IF NOT EXISTS payment_engine.payload_schema_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_config_id UUID NOT NULL REFERENCES payment_engine.core_banking_endpoint_configurations(id) ON DELETE CASCADE,
    mapping_name VARCHAR(100) NOT NULL,
    mapping_type VARCHAR(50) NOT NULL CHECK (mapping_type IN (
        'FIELD_MAPPING', 'OBJECT_MAPPING', 'ARRAY_MAPPING', 'NESTED_MAPPING', 
        'CONDITIONAL_MAPPING', 'TRANSFORMATION_MAPPING', 'CUSTOM_MAPPING'
    )),
    direction VARCHAR(20) NOT NULL CHECK (direction IN ('REQUEST', 'RESPONSE', 'BIDIRECTIONAL')),
    source_schema JSONB,
    target_schema JSONB,
    field_mappings JSONB,
    transformation_rules JSONB,
    validation_rules JSONB,
    default_values JSONB,
    conditional_mappings JSONB,
    array_handling_config JSONB,
    nested_object_config JSONB,
    error_handling_config JSONB,
    version VARCHAR(20) DEFAULT '1.0',
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for core_banking_endpoint_configurations
CREATE INDEX IF NOT EXISTS idx_endpoint_config_core_banking_id ON payment_engine.core_banking_endpoint_configurations(core_banking_config_id);
CREATE INDEX IF NOT EXISTS idx_endpoint_config_name ON payment_engine.core_banking_endpoint_configurations(endpoint_name);
CREATE INDEX IF NOT EXISTS idx_endpoint_config_type ON payment_engine.core_banking_endpoint_configurations(endpoint_type);
CREATE INDEX IF NOT EXISTS idx_endpoint_config_active ON payment_engine.core_banking_endpoint_configurations(is_active);
CREATE INDEX IF NOT EXISTS idx_endpoint_config_priority ON payment_engine.core_banking_endpoint_configurations(priority);
CREATE INDEX IF NOT EXISTS idx_endpoint_config_http_method ON payment_engine.core_banking_endpoint_configurations(http_method);
CREATE UNIQUE INDEX IF NOT EXISTS idx_endpoint_config_unique ON payment_engine.core_banking_endpoint_configurations(core_banking_config_id, endpoint_name) WHERE is_active = true;

-- Create indexes for payload_schema_mappings
CREATE INDEX IF NOT EXISTS idx_payload_mapping_endpoint_id ON payment_engine.payload_schema_mappings(endpoint_config_id);
CREATE INDEX IF NOT EXISTS idx_payload_mapping_name ON payment_engine.payload_schema_mappings(mapping_name);
CREATE INDEX IF NOT EXISTS idx_payload_mapping_type ON payment_engine.payload_schema_mappings(mapping_type);
CREATE INDEX IF NOT EXISTS idx_payload_mapping_direction ON payment_engine.payload_schema_mappings(direction);
CREATE INDEX IF NOT EXISTS idx_payload_mapping_active ON payment_engine.payload_schema_mappings(is_active);
CREATE INDEX IF NOT EXISTS idx_payload_mapping_priority ON payment_engine.payload_schema_mappings(priority);
CREATE INDEX IF NOT EXISTS idx_payload_mapping_version ON payment_engine.payload_schema_mappings(version);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payload_mapping_unique ON payment_engine.payload_schema_mappings(endpoint_config_id, mapping_name) WHERE is_active = true;

-- Create triggers for updated_at
CREATE TRIGGER update_endpoint_configurations_updated_at 
    BEFORE UPDATE ON payment_engine.core_banking_endpoint_configurations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payload_schema_mappings_updated_at 
    BEFORE UPDATE ON payment_engine.payload_schema_mappings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample endpoint configurations
INSERT INTO payment_engine.core_banking_endpoint_configurations (
    core_banking_config_id, endpoint_name, endpoint_type, http_method, endpoint_path,
    request_headers, timeout_ms, retry_attempts, priority, description, created_by, updated_by
) 
SELECT 
    cbc.id,
    'Get Account Info',
    'ACCOUNT_INFO',
    'GET',
    '/api/v1/accounts/{accountNumber}',
    '{"Content-Type": "application/json", "Accept": "application/json"}',
    30000,
    3,
    1,
    'Get account information endpoint',
    'system',
    'system'
FROM payment_engine.core_banking_configurations cbc 
WHERE cbc.tenant_id = 'tenant1' AND cbc.adapter_type = 'REST';

INSERT INTO payment_engine.core_banking_endpoint_configurations (
    core_banking_config_id, endpoint_name, endpoint_type, http_method, endpoint_path,
    request_headers, timeout_ms, retry_attempts, priority, description, created_by, updated_by
) 
SELECT 
    cbc.id,
    'Get Account Balance',
    'ACCOUNT_BALANCE',
    'GET',
    '/api/v1/accounts/{accountNumber}/balance',
    '{"Content-Type": "application/json", "Accept": "application/json"}',
    30000,
    3,
    1,
    'Get account balance endpoint',
    'system',
    'system'
FROM payment_engine.core_banking_configurations cbc 
WHERE cbc.tenant_id = 'tenant1' AND cbc.adapter_type = 'REST';

INSERT INTO payment_engine.core_banking_endpoint_configurations (
    core_banking_config_id, endpoint_name, endpoint_type, http_method, endpoint_path,
    request_headers, timeout_ms, retry_attempts, priority, description, created_by, updated_by
) 
SELECT 
    cbc.id,
    'Process Debit Transaction',
    'DEBIT_TRANSACTION',
    'POST',
    '/api/v1/transactions/debit',
    '{"Content-Type": "application/json", "Accept": "application/json"}',
    30000,
    3,
    1,
    'Process debit transaction endpoint',
    'system',
    'system'
FROM payment_engine.core_banking_configurations cbc 
WHERE cbc.tenant_id = 'tenant1' AND cbc.adapter_type = 'REST';

INSERT INTO payment_engine.core_banking_endpoint_configurations (
    core_banking_config_id, endpoint_name, endpoint_type, http_method, endpoint_path,
    request_headers, timeout_ms, retry_attempts, priority, description, created_by, updated_by
) 
SELECT 
    cbc.id,
    'Process Credit Transaction',
    'CREDIT_TRANSACTION',
    'POST',
    '/api/v1/transactions/credit',
    '{"Content-Type": "application/json", "Accept": "application/json"}',
    30000,
    3,
    1,
    'Process credit transaction endpoint',
    'system',
    'system'
FROM payment_engine.core_banking_configurations cbc 
WHERE cbc.tenant_id = 'tenant1' AND cbc.adapter_type = 'REST';

INSERT INTO payment_engine.core_banking_endpoint_configurations (
    core_banking_config_id, endpoint_name, endpoint_type, http_method, endpoint_path,
    request_headers, timeout_ms, retry_attempts, priority, description, created_by, updated_by
) 
SELECT 
    cbc.id,
    'Process Transfer Transaction',
    'TRANSFER_TRANSACTION',
    'POST',
    '/api/v1/transactions/transfer',
    '{"Content-Type": "application/json", "Accept": "application/json"}',
    30000,
    3,
    1,
    'Process transfer transaction endpoint',
    'system',
    'system'
FROM payment_engine.core_banking_configurations cbc 
WHERE cbc.tenant_id = 'tenant1' AND cbc.adapter_type = 'REST';

-- Insert sample payload schema mappings
INSERT INTO payment_engine.payload_schema_mappings (
    endpoint_config_id, mapping_name, mapping_type, direction,
    field_mappings, validation_rules, default_values, description, created_by, updated_by
)
SELECT 
    ec.id,
    'Account Info Request Mapping',
    'FIELD_MAPPING',
    'REQUEST',
    '{
        "accountNumber": "accountNumber",
        "tenantId": "tenantId"
    }',
    '{
        "accountNumber": {
            "required": true,
            "type": "string",
            "minLength": 1,
            "maxLength": 50
        },
        "tenantId": {
            "required": true,
            "type": "string",
            "minLength": 1,
            "maxLength": 50
        }
    }',
    '{
        "requestId": "{{uuid()}}",
        "timestamp": "{{now()}}"
    }',
    'Mapping for account info request payload',
    'system',
    'system'
FROM payment_engine.core_banking_endpoint_configurations ec
WHERE ec.endpoint_type = 'ACCOUNT_INFO';

INSERT INTO payment_engine.payload_schema_mappings (
    endpoint_config_id, mapping_name, mapping_type, direction,
    field_mappings, validation_rules, default_values, description, created_by, updated_by
)
SELECT 
    ec.id,
    'Account Info Response Mapping',
    'FIELD_MAPPING',
    'RESPONSE',
    '{
        "accountNumber": "accountNumber",
        "accountName": "accountName",
        "accountType": "accountType",
        "currency": "currency",
        "balance": "balance",
        "availableBalance": "availableBalance",
        "status": "status",
        "bankCode": "bankCode",
        "bankName": "bankName"
    }',
    '{
        "accountNumber": {
            "required": true,
            "type": "string"
        },
        "balance": {
            "required": true,
            "type": "number"
        },
        "availableBalance": {
            "required": true,
            "type": "number"
        }
    }',
    '{}',
    'Mapping for account info response payload',
    'system',
    'system'
FROM payment_engine.core_banking_endpoint_configurations ec
WHERE ec.endpoint_type = 'ACCOUNT_INFO';

INSERT INTO payment_engine.payload_schema_mappings (
    endpoint_config_id, mapping_name, mapping_type, direction,
    field_mappings, validation_rules, default_values, description, created_by, updated_by
)
SELECT 
    ec.id,
    'Debit Transaction Request Mapping',
    'FIELD_MAPPING',
    'REQUEST',
    '{
        "transactionReference": "transactionReference",
        "accountNumber": "accountNumber",
        "amount": "amount",
        "currency": "currency",
        "description": "description",
        "tenantId": "tenantId"
    }',
    '{
        "transactionReference": {
            "required": true,
            "type": "string",
            "minLength": 1,
            "maxLength": 100
        },
        "accountNumber": {
            "required": true,
            "type": "string",
            "minLength": 1,
            "maxLength": 50
        },
        "amount": {
            "required": true,
            "type": "number"
        },
        "currency": {
            "required": true,
            "type": "string",
            "minLength": 3,
            "maxLength": 3
        }
    }',
    '{
        "requestId": "{{uuid()}}",
        "timestamp": "{{now()}}",
        "source": "payment-engine"
    }',
    'Mapping for debit transaction request payload',
    'system',
    'system'
FROM payment_engine.core_banking_endpoint_configurations ec
WHERE ec.endpoint_type = 'DEBIT_TRANSACTION';

INSERT INTO payment_engine.payload_schema_mappings (
    endpoint_config_id, mapping_name, mapping_type, direction,
    field_mappings, validation_rules, default_values, description, created_by, updated_by
)
SELECT 
    ec.id,
    'Debit Transaction Response Mapping',
    'FIELD_MAPPING',
    'RESPONSE',
    '{
        "transactionReference": "transactionReference",
        "status": "status",
        "statusMessage": "statusMessage",
        "amount": "amount",
        "currency": "currency",
        "processedAt": "processedAt",
        "coreBankingReference": "coreBankingReference"
    }',
    '{
        "transactionReference": {
            "required": true,
            "type": "string"
        },
        "status": {
            "required": true,
            "type": "string"
        }
    }',
    '{}',
    'Mapping for debit transaction response payload',
    'system',
    'system'
FROM payment_engine.core_banking_endpoint_configurations ec
WHERE ec.endpoint_type = 'DEBIT_TRANSACTION';

-- Add comments
COMMENT ON TABLE payment_engine.core_banking_endpoint_configurations IS 'Configuration for specific endpoints within core banking systems';
COMMENT ON TABLE payment_engine.payload_schema_mappings IS 'Mapping configurations between internal data models and external core banking payloads';

COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.endpoint_type IS 'Type of endpoint: ACCOUNT_INFO, DEBIT_TRANSACTION, etc.';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.request_headers IS 'HTTP headers to include in requests';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.query_parameters IS 'Query parameters to include in requests';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.authentication_config IS 'Authentication configuration for this endpoint';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.circuit_breaker_config IS 'Circuit breaker configuration for this endpoint';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.rate_limiting_config IS 'Rate limiting configuration for this endpoint';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.request_transformation_config IS 'Request transformation configuration';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.response_transformation_config IS 'Response transformation configuration';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.validation_rules IS 'Validation rules for this endpoint';
COMMENT ON COLUMN payment_engine.core_banking_endpoint_configurations.error_handling_config IS 'Error handling configuration for this endpoint';

COMMENT ON COLUMN payment_engine.payload_schema_mappings.mapping_type IS 'Type of mapping: FIELD_MAPPING, OBJECT_MAPPING, etc.';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.direction IS 'Direction of mapping: REQUEST, RESPONSE, or BIDIRECTIONAL';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.source_schema IS 'Source schema definition';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.target_schema IS 'Target schema definition';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.field_mappings IS 'Field mapping configuration';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.transformation_rules IS 'Data transformation rules';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.validation_rules IS 'Validation rules for payload';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.default_values IS 'Default values for fields';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.conditional_mappings IS 'Conditional mapping rules';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.array_handling_config IS 'Array handling configuration';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.nested_object_config IS 'Nested object handling configuration';
COMMENT ON COLUMN payment_engine.payload_schema_mappings.error_handling_config IS 'Error handling configuration for mapping';