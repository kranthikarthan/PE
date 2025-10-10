-- Advanced Payload Mapping Configurations for Fraud and Core Banking APIs
-- This script creates sample mapping configurations for various API integrations

-- ============================================================================
-- FRAUD API MAPPING CONFIGURATIONS
-- ============================================================================

-- FICO Falcon Fraud API Request Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'FICO Falcon Fraud API Request Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'FRAUD_API_REQUEST',
    '{
        "transactionId": "assessment.transactionReference",
        "amount": "amount",
        "currency": "currency",
        "timestamp": "timestamp",
        "accountNumber": "fromAccountNumber",
        "beneficiaryAccount": "toAccountNumber",
        "paymentType": "paymentType",
        "localInstrumentCode": "localInstrumentCode",
        "channel": "API",
        "deviceId": "deviceId",
        "ipAddress": "ipAddress"
    }'::jsonb,
    '{
        "channel": "API",
        "apiVersion": "2.0",
        "requestType": "FRAUD_ASSESSMENT"
    }'::jsonb,
    '{
        "riskScore": {
            "expression": "${source.amount} > 10000 ? 0.8 : 0.3",
            "type": "NUMBER"
        },
        "customerSegment": {
            "expression": "${source.amount} > 50000 ? \"PREMIUM\" : \"STANDARD\"",
            "type": "STRING"
        }
    }'::jsonb,
    '{
        "sessionId": {
            "type": "UUID"
        },
        "requestId": {
            "type": "SEQUENTIAL",
            "prefix": "FICO-",
            "length": 10
        }
    }'::jsonb,
    '{
        "amount > 100000": {
            "target": "priority",
            "source": "HIGH"
        },
        "currency == \"USD\"": {
            "target": "region",
            "source": "US"
        }
    }'::jsonb,
    '{
        "timestamp": "uppercase",
        "accountNumber": "mask"
    }'::jsonb,
    '{
        "timeout": 30000,
        "retryAttempts": 3
    }'::jsonb,
    '1.0',
    1,
    true,
    'Mapping for FICO Falcon fraud API request transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- SAS Fraud Management API Request Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'SAS Fraud Management API Request Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'FRAUD_API_REQUEST',
    '{
        "transactionId": "assessment.transactionReference",
        "amount": "amount",
        "currency": "currency",
        "transactionDate": "timestamp",
        "customerId": "customerId",
        "accountNumber": "fromAccountNumber",
        "beneficiaryAccount": "toAccountNumber",
        "paymentType": "paymentType",
        "clearingSystem": "clearingSystemCode",
        "riskFactors": "riskFactors"
    }'::jsonb,
    '{
        "apiVersion": "1.0",
        "requestType": "REAL_TIME_FRAUD_CHECK",
        "environment": "PRODUCTION"
    }'::jsonb,
    '{
        "riskLevel": {
            "expression": "${source.amount} > 50000 ? \"HIGH\" : \"MEDIUM\"",
            "type": "STRING"
        },
        "fraudScore": {
            "expression": "${source.amount} * 0.01",
            "type": "NUMBER"
        }
    }'::jsonb,
    '{
        "correlationId": {
            "type": "UUID"
        },
        "batchId": {
            "type": "SEQUENTIAL",
            "prefix": "SAS-",
            "length": 8
        }
    }'::jsonb,
    '{
        "paymentType == \"CREDIT_TRANSFER\"": {
            "target": "transactionCategory",
            "source": "WIRE_TRANSFER"
        }
    }'::jsonb,
    '{
        "transactionDate": "date_format",
        "amount": "number_format"
    }'::jsonb,
    '{
        "timeout": 25000,
        "retryAttempts": 2
    }'::jsonb,
    '1.0',
    2,
    true,
    'Mapping for SAS Fraud Management API request transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- Fraud API Response Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Fraud API Response Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'FRAUD_API_RESPONSE',
    '{
        "riskScore": "riskScore",
        "riskLevel": "riskLevel",
        "decision": "decision",
        "assessmentDetails": "assessmentDetails",
        "fraudIndicators": "fraudIndicators",
        "confidence": "confidence"
    }'::jsonb,
    '{
        "processedAt": "timestamp",
        "apiVersion": "1.0"
    }'::jsonb,
    '{
        "normalizedRiskLevel": {
            "expression": "${source.riskLevel} == \"HIGH\" ? \"CRITICAL\" : ${source.riskLevel}",
            "type": "STRING"
        },
        "riskScorePercentage": {
            "expression": "${source.riskScore} * 100",
            "type": "NUMBER"
        }
    }'::jsonb,
    '{
        "responseId": {
            "type": "UUID"
        }
    }'::jsonb,
    '{
        "decision == \"APPROVE\"": {
            "target": "status",
            "source": "APPROVED"
        },
        "decision == \"REJECT\"": {
            "target": "status",
            "source": "REJECTED"
        }
    }'::jsonb,
    '{
        "riskScore": "number_format",
        "confidence": "number_format"
    }'::jsonb,
    '{
        "timeout": 30000,
        "retryAttempts": 3
    }'::jsonb,
    '1.0',
    1,
    true,
    'Mapping for fraud API response transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- ============================================================================
-- CORE BANKING API MAPPING CONFIGURATIONS
-- ============================================================================

-- Core Banking Debit Request Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Core Banking Debit Request Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'CORE_BANKING_DEBIT_REQUEST',
    '{
        "transactionReference": "transactionReference",
        "tenantId": "tenantId",
        "accountNumber": "accountNumber",
        "amount": "amount",
        "currency": "currency",
        "paymentType": "paymentType",
        "localInstrumentCode": "localInstrumentCode",
        "description": "description",
        "reference": "reference",
        "valueDate": "valueDate",
        "requestedExecutionDate": "requestedExecutionDate",
        "chargeBearer": "chargeBearer",
        "remittanceInfo": "remittanceInfo"
    }'::jsonb,
    '{
        "transactionType": "DEBIT",
        "processingMode": "REAL_TIME",
        "apiVersion": "2.0"
    }'::jsonb,
    '{
        "transactionCategory": {
            "expression": "${source.paymentType} == \"CREDIT_TRANSFER\" ? \"WIRE_TRANSFER\" : \"PAYMENT\"",
            "type": "STRING"
        },
        "priority": {
            "expression": "${source.amount} > 100000 ? \"HIGH\" : \"NORMAL\"",
            "type": "STRING"
        }
    }'::jsonb,
    '{
        "coreBankingTransactionId": {
            "type": "SEQUENTIAL",
            "prefix": "CB-",
            "length": 12
        },
        "processingTimestamp": {
            "type": "TIMESTAMP"
        }
    }'::jsonb,
    '{
        "amount > 50000": {
            "target": "requiresApproval",
            "source": true
        },
        "currency == \"USD\"": {
            "target": "regulatoryCheck",
            "source": "OFAC"
        }
    }'::jsonb,
    '{
        "amount": "number_format",
        "valueDate": "date_format",
        "requestedExecutionDate": "date_format"
    }'::jsonb,
    '{
        "timeout": 30000,
        "retryAttempts": 3,
        "requiresApproval": false
    }'::jsonb,
    '1.0',
    1,
    true,
    'Mapping for core banking debit request transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- Core Banking Credit Request Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Core Banking Credit Request Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'CORE_BANKING_CREDIT_REQUEST',
    '{
        "transactionReference": "transactionReference",
        "tenantId": "tenantId",
        "accountNumber": "accountNumber",
        "amount": "amount",
        "currency": "currency",
        "paymentType": "paymentType",
        "localInstrumentCode": "localInstrumentCode",
        "description": "description",
        "reference": "reference",
        "valueDate": "valueDate",
        "requestedExecutionDate": "requestedExecutionDate",
        "chargeBearer": "chargeBearer",
        "remittanceInfo": "remittanceInfo"
    }'::jsonb,
    '{
        "transactionType": "CREDIT",
        "processingMode": "REAL_TIME",
        "apiVersion": "2.0"
    }'::jsonb,
    '{
        "transactionCategory": {
            "expression": "${source.paymentType} == \"CREDIT_TRANSFER\" ? \"WIRE_TRANSFER\" : \"PAYMENT\"",
            "type": "STRING"
        },
        "priority": {
            "expression": "${source.amount} > 100000 ? \"HIGH\" : \"NORMAL\"",
            "type": "STRING"
        }
    }'::jsonb,
    '{
        "coreBankingTransactionId": {
            "type": "SEQUENTIAL",
            "prefix": "CB-",
            "length": 12
        },
        "processingTimestamp": {
            "type": "TIMESTAMP"
        }
    }'::jsonb,
    '{
        "amount > 50000": {
            "target": "requiresApproval",
            "source": true
        },
        "currency == \"USD\"": {
            "target": "regulatoryCheck",
            "source": "OFAC"
        }
    }'::jsonb,
    '{
        "amount": "number_format",
        "valueDate": "date_format",
        "requestedExecutionDate": "date_format"
    }'::jsonb,
    '{
        "timeout": 30000,
        "retryAttempts": 3,
        "requiresApproval": false
    }'::jsonb,
    '1.0',
    1,
    true,
    'Mapping for core banking credit request transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- Core Banking Response Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Core Banking Response Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'CORE_BANKING_DEBIT_RESPONSE',
    '{
        "transactionReference": "transactionReference",
        "status": "status",
        "statusMessage": "statusMessage",
        "errorMessage": "errorMessage",
        "processedAt": "processedAt",
        "transactionId": "transactionId",
        "balanceAfter": "balanceAfter"
    }'::jsonb,
    '{
        "apiVersion": "2.0",
        "responseType": "TRANSACTION_RESULT"
    }'::jsonb,
    '{
        "normalizedStatus": {
            "expression": "${source.status} == \"SUCCESS\" ? \"COMPLETED\" : ${source.status}",
            "type": "STRING"
        },
        "isSuccessful": {
            "expression": "${source.status} == \"SUCCESS\"",
            "type": "BOOLEAN"
        }
    }'::jsonb,
    '{
        "responseId": {
            "type": "UUID"
        }
    }'::jsonb,
    '{
        "status == \"SUCCESS\"": {
            "target": "finalStatus",
            "source": "COMPLETED"
        },
        "status == \"FAILED\"": {
            "target": "finalStatus",
            "source": "FAILED"
        }
    }'::jsonb,
    '{
        "balanceAfter": "number_format",
        "processedAt": "date_format"
    }'::jsonb,
    '{
        "timeout": 30000,
        "retryAttempts": 3
    }'::jsonb,
    '1.0',
    1,
    true,
    'Mapping for core banking response transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- ============================================================================
-- SCHEME API MAPPING CONFIGURATIONS
-- ============================================================================

-- Scheme Request Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Scheme Request Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    'SWIFT',
    'CUSTOM_MAPPING',
    'SCHEME_REQUEST',
    '{
        "messageType": "messageType",
        "clearingSystemCode": "clearingSystemCode",
        "endpointUrl": "endpointUrl",
        "payload": "payload"
    }'::jsonb,
    '{
        "apiVersion": "1.0",
        "requestType": "SCHEME_MESSAGE",
        "format": "JSON"
    }'::jsonb,
    '{
        "priority": {
            "expression": "${source.clearingSystemCode} == \"SWIFT\" ? \"HIGH\" : \"NORMAL\"",
            "type": "STRING"
        },
        "timeout": {
            "expression": "${source.clearingSystemCode} == \"SWIFT\" ? 60000 : 30000",
            "type": "NUMBER"
        }
    }'::jsonb,
    '{
        "messageId": {
            "type": "SEQUENTIAL",
            "prefix": "MSG-",
            "length": 10
        },
        "correlationId": {
            "type": "SEQUENTIAL",
            "prefix": "CORR-",
            "length": 10
        }
    }'::jsonb,
    '{
        "clearingSystemCode == \"SWIFT\"": {
            "target": "requiresEncryption",
            "source": true
        }
    }'::jsonb,
    '{
        "payload": "encrypt"
    }'::jsonb,
    '{
        "timeout": 30000,
        "retryAttempts": 3
    }'::jsonb,
    '1.0',
    1,
    true,
    'Mapping for scheme request transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- Scheme Response Mapping
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Scheme Response Mapping',
    'tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    'SWIFT',
    'CUSTOM_MAPPING',
    'SCHEME_RESPONSE',
    '{
        "status": "status",
        "responseCode": "responseCode",
        "responseMessage": "responseMessage",
        "payload": "payload",
        "processingTimeMs": "processingTimeMs",
        "timestamp": "timestamp"
    }'::jsonb,
    '{
        "apiVersion": "1.0",
        "responseType": "SCHEME_RESPONSE"
    }'::jsonb,
    '{
        "normalizedStatus": {
            "expression": "${source.status} == \"SUCCESS\" ? \"COMPLETED\" : ${source.status}",
            "type": "STRING"
        },
        "isSuccessful": {
            "expression": "${source.status} == \"SUCCESS\"",
            "type": "BOOLEAN"
        }
    }'::jsonb,
    '{
        "responseId": {
            "type": "UUID"
        }
    }'::jsonb,
    '{
        "status == \"SUCCESS\"": {
            "target": "finalStatus",
            "source": "COMPLETED"
        },
        "status == \"FAILED\"": {
            "target": "finalStatus",
            "source": "FAILED"
        }
    }'::jsonb,
    '{
        "payload": "decrypt",
        "processingTimeMs": "number_format"
    }'::jsonb,
    '{
        "timeout": 30000,
        "retryAttempts": 3
    }'::jsonb,
    '1.0',
    1,
    true,
    'Mapping for scheme response transformation',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- ============================================================================
-- TENANT-SPECIFIC MAPPING CONFIGURATIONS
-- ============================================================================

-- High-Value Transaction Mapping for Premium Tenant
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Premium Tenant High-Value Transaction Mapping',
    'premium-tenant-001',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'FRAUD_API_REQUEST',
    '{
        "transactionId": "assessment.transactionReference",
        "amount": "amount",
        "currency": "currency",
        "customerId": "customerId",
        "accountNumber": "fromAccountNumber",
        "beneficiaryAccount": "toAccountNumber",
        "paymentType": "paymentType",
        "localInstrumentCode": "localInstrumentCode"
    }'::jsonb,
    '{
        "channel": "PREMIUM_API",
        "apiVersion": "3.0",
        "requestType": "ENHANCED_FRAUD_ASSESSMENT",
        "priority": "HIGH"
    }'::jsonb,
    '{
        "riskScore": {
            "expression": "${source.amount} > 100000 ? 0.9 : 0.4",
            "type": "NUMBER"
        },
        "customerTier": {
            "expression": "PREMIUM",
            "type": "STRING"
        }
    }'::jsonb,
    '{
        "sessionId": {
            "type": "UUID"
        },
        "requestId": {
            "type": "SEQUENTIAL",
            "prefix": "PREMIUM-",
            "length": 12
        }
    }'::jsonb,
    '{
        "amount > 500000": {
            "target": "requiresManualReview",
            "source": true
        }
    }'::jsonb,
    '{
        "timestamp": "uppercase",
        "accountNumber": "mask"
    }'::jsonb,
    '{
        "timeout": 45000,
        "retryAttempts": 5
    }'::jsonb,
    '1.0',
    1,
    true,
    'Enhanced mapping for premium tenant high-value transactions',
    NOW(),
    NOW(),
    'system',
    'system'
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_advanced_payload_mappings_tenant_direction 
ON payment_engine.advanced_payload_mappings (tenant_id, direction) 
WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_advanced_payload_mappings_payment_type 
ON payment_engine.advanced_payload_mappings (payment_type, local_instrumentation_code) 
WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_advanced_payload_mappings_clearing_system 
ON payment_engine.advanced_payload_mappings (clearing_system_code) 
WHERE is_active = true;

-- Add comments for documentation
COMMENT ON TABLE payment_engine.advanced_payload_mappings IS 'Advanced payload mapping configurations for flexible API transformations';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.direction IS 'Direction of mapping: REQUEST, RESPONSE, FRAUD_API_REQUEST, FRAUD_API_RESPONSE, CORE_BANKING_DEBIT_REQUEST, CORE_BANKING_DEBIT_RESPONSE, CORE_BANKING_CREDIT_REQUEST, CORE_BANKING_CREDIT_RESPONSE, SCHEME_REQUEST, SCHEME_RESPONSE';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.field_mappings IS 'JSON mapping of source fields to target fields';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.derived_value_rules IS 'JSON rules for calculating derived values using expressions';
COMMENT ON COLUMN payment_engine.advanced_payload_mappings.conditional_mappings IS 'JSON conditional mappings based on field values';