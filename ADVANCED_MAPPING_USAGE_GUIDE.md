# Advanced Mapping Usage Guide

## Overview

The Payment Engine now uses the advanced mapping system for all API interactions, including fraud API calls, core banking debit/credit operations, and scheme processing. This provides flexible, configurable payload transformations without requiring code changes.

## Table of Contents

1. [Advanced Mapping Integration](#advanced-mapping-integration)
2. [Fraud API Mapping](#fraud-api-mapping)
3. [Core Banking API Mapping](#core-banking-api-mapping)
4. [Scheme Processing Mapping](#scheme-processing-mapping)
5. [Mapping Configuration](#mapping-configuration)
6. [React Frontend Configuration](#react-frontend-configuration)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

## Advanced Mapping Integration

### Key Benefits

- **No Code Changes**: Configure API transformations without modifying application code
- **Flexible Mapping**: Support for static values, derived values, conditional logic, and auto-generated IDs
- **Tenant-Specific**: Different mappings per tenant, payment type, and clearing system
- **Fallback Support**: Graceful fallback to original logic when no mapping is configured
- **Performance Optimized**: Cached transformations for better performance

### Integration Points

The advanced mapping system is integrated into:

1. **Fraud API Calls**: Request and response transformation
2. **Core Banking Operations**: Debit and credit request/response mapping
3. **Scheme Processing**: Clearing system request/response transformation
4. **PAIN.001 to PACS.008**: Message transformation with advanced mapping

## Fraud API Mapping

### Enhanced Fraud API Service

**Location**: `/workspace/services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/impl/ExternalFraudApiServiceImpl.java`

**Key Changes**:

1. **Request Transformation**: Uses advanced mapping for fraud API request building
2. **Response Transformation**: Transforms fraud API responses using advanced mapping
3. **Fallback Support**: Falls back to original logic if no mapping is configured

**Implementation**:

```java
// Try to use advanced mapping for fraud API request transformation
Optional<Map<String, Object>> transformedPayload = advancedPayloadTransformationService.transformPayload(
        tenantId, paymentType, localInstrumentCode, clearingSystemCode,
        AdvancedPayloadMapping.Direction.FRAUD_API_REQUEST, sourcePayload);

Map<String, Object> apiRequest;
if (transformedPayload.isPresent()) {
    logger.debug("Using advanced mapping for fraud API request transformation");
    apiRequest = transformedPayload.get();
} else {
    logger.debug("No advanced mapping found, using fallback request building");
    // Fallback to original logic
}
```

### Fraud API Request Mapping

**Direction**: `FRAUD_API_REQUEST`

**Sample Configuration**:

```json
{
  "mappingName": "FICO Falcon Fraud API Request Mapping",
  "direction": "FRAUD_API_REQUEST",
  "fieldMappings": {
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
  },
  "valueAssignments": {
    "channel": "API",
    "apiVersion": "2.0",
    "requestType": "FRAUD_ASSESSMENT"
  },
  "derivedValueRules": {
    "riskScore": {
      "expression": "${source.amount} > 10000 ? 0.8 : 0.3",
      "type": "NUMBER"
    },
    "customerSegment": {
      "expression": "${source.amount} > 50000 ? \"PREMIUM\" : \"STANDARD\"",
      "type": "STRING"
    }
  },
  "autoGenerationRules": {
    "sessionId": {
      "type": "UUID"
    },
    "requestId": {
      "type": "SEQUENTIAL",
      "prefix": "FICO-",
      "length": 10
    }
  }
}
```

### Fraud API Response Mapping

**Direction**: `FRAUD_API_RESPONSE`

**Sample Configuration**:

```json
{
  "mappingName": "Fraud API Response Mapping",
  "direction": "FRAUD_API_RESPONSE",
  "fieldMappings": {
    "riskScore": "riskScore",
    "riskLevel": "riskLevel",
    "decision": "decision",
    "assessmentDetails": "assessmentDetails",
    "fraudIndicators": "fraudIndicators",
    "confidence": "confidence"
  },
  "derivedValueRules": {
    "normalizedRiskLevel": {
      "expression": "${source.riskLevel} == \"HIGH\" ? \"CRITICAL\" : ${source.riskLevel}",
      "type": "STRING"
    },
    "riskScorePercentage": {
      "expression": "${source.riskScore} * 100",
      "type": "NUMBER"
    }
  }
}
```

## Core Banking API Mapping

### Enhanced Core Banking Adapter

**Location**: `/workspace/services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/impl/RestCoreBankingAdapter.java`

**Key Changes**:

1. **Debit Request Mapping**: Uses advanced mapping for debit transaction requests
2. **Credit Request Mapping**: Uses advanced mapping for credit transaction requests
3. **Response Mapping**: Transforms core banking responses using advanced mapping
4. **Helper Methods**: Added conversion methods between objects and maps

**Implementation**:

```java
// Try to use advanced mapping for core banking debit request transformation
Optional<Map<String, Object>> transformedRequest = advancedPayloadTransformationService.transformPayload(
        request.getTenantId(), request.getPaymentType(), request.getLocalInstrumentCode(), null,
        AdvancedPayloadMapping.Direction.CORE_BANKING_DEBIT_REQUEST, requestMap);

DebitTransactionRequest finalRequest = request;
if (transformedRequest.isPresent()) {
    logger.debug("Using advanced mapping for core banking debit request transformation");
    finalRequest = convertMapToDebitRequest(transformedRequest.get(), request);
} else {
    logger.debug("No advanced mapping found, using original request");
}
```

### Core Banking Debit Request Mapping

**Direction**: `CORE_BANKING_DEBIT_REQUEST`

**Sample Configuration**:

```json
{
  "mappingName": "Core Banking Debit Request Mapping",
  "direction": "CORE_BANKING_DEBIT_REQUEST",
  "fieldMappings": {
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
  },
  "valueAssignments": {
    "transactionType": "DEBIT",
    "processingMode": "REAL_TIME",
    "apiVersion": "2.0"
  },
  "derivedValueRules": {
    "transactionCategory": {
      "expression": "${source.paymentType} == \"CREDIT_TRANSFER\" ? \"WIRE_TRANSFER\" : \"PAYMENT\"",
      "type": "STRING"
    },
    "priority": {
      "expression": "${source.amount} > 100000 ? \"HIGH\" : \"NORMAL\"",
      "type": "STRING"
    }
  },
  "autoGenerationRules": {
    "coreBankingTransactionId": {
      "type": "SEQUENTIAL",
      "prefix": "CB-",
      "length": 12
    },
    "processingTimestamp": {
      "type": "TIMESTAMP"
    }
  }
}
```

### Core Banking Credit Request Mapping

**Direction**: `CORE_BANKING_CREDIT_REQUEST`

**Sample Configuration**:

```json
{
  "mappingName": "Core Banking Credit Request Mapping",
  "direction": "CORE_BANKING_CREDIT_REQUEST",
  "fieldMappings": {
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
  },
  "valueAssignments": {
    "transactionType": "CREDIT",
    "processingMode": "REAL_TIME",
    "apiVersion": "2.0"
  }
}
```

### Core Banking Response Mapping

**Direction**: `CORE_BANKING_DEBIT_RESPONSE` / `CORE_BANKING_CREDIT_RESPONSE`

**Sample Configuration**:

```json
{
  "mappingName": "Core Banking Response Mapping",
  "direction": "CORE_BANKING_DEBIT_RESPONSE",
  "fieldMappings": {
    "transactionReference": "transactionReference",
    "status": "status",
    "statusMessage": "statusMessage",
    "errorMessage": "errorMessage",
    "processedAt": "processedAt",
    "transactionId": "transactionId",
    "balanceAfter": "balanceAfter"
  },
  "derivedValueRules": {
    "normalizedStatus": {
      "expression": "${source.status} == \"SUCCESS\" ? \"COMPLETED\" : ${source.status}",
      "type": "STRING"
    },
    "isSuccessful": {
      "expression": "${source.status} == \"SUCCESS\"",
      "type": "BOOLEAN"
    }
  }
}
```

## Scheme Processing Mapping

### Enhanced Scheme Processing Service

**Location**: `/workspace/services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/impl/SchemeProcessingServiceImpl.java`

**Key Changes**:

1. **Request Transformation**: Uses advanced mapping for scheme request transformation
2. **Response Transformation**: Transforms scheme responses using advanced mapping
3. **Context Extraction**: Extracts tenant and payment context from messages
4. **Helper Methods**: Added methods for extracting context from messages

**Implementation**:

```java
// Try to use advanced mapping for scheme request transformation
Optional<Map<String, Object>> transformedMessage = advancedPayloadTransformationService.transformPayload(
        tenantId, paymentType, localInstrumentCode, clearingSystemCode,
        AdvancedPayloadMapping.Direction.SCHEME_REQUEST, sourcePayload);

Map<String, Object> finalMessage = message;
if (transformedMessage.isPresent()) {
    logger.debug("Using advanced mapping for scheme request transformation");
    finalMessage = transformedMessage.get();
} else {
    logger.debug("No advanced mapping found, using original message");
}
```

### Scheme Request Mapping

**Direction**: `SCHEME_REQUEST`

**Sample Configuration**:

```json
{
  "mappingName": "Scheme Request Mapping",
  "direction": "SCHEME_REQUEST",
  "fieldMappings": {
    "messageType": "messageType",
    "clearingSystemCode": "clearingSystemCode",
    "endpointUrl": "endpointUrl",
    "payload": "payload"
  },
  "valueAssignments": {
    "apiVersion": "1.0",
    "requestType": "SCHEME_MESSAGE",
    "format": "JSON"
  },
  "derivedValueRules": {
    "priority": {
      "expression": "${source.clearingSystemCode} == \"SWIFT\" ? \"HIGH\" : \"NORMAL\"",
      "type": "STRING"
    },
    "timeout": {
      "expression": "${source.clearingSystemCode} == \"SWIFT\" ? 60000 : 30000",
      "type": "NUMBER"
    }
  },
  "autoGenerationRules": {
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
  }
}
```

### Scheme Response Mapping

**Direction**: `SCHEME_RESPONSE`

**Sample Configuration**:

```json
{
  "mappingName": "Scheme Response Mapping",
  "direction": "SCHEME_RESPONSE",
  "fieldMappings": {
    "status": "status",
    "responseCode": "responseCode",
    "responseMessage": "responseMessage",
    "payload": "payload",
    "processingTimeMs": "processingTimeMs",
    "timestamp": "timestamp"
  },
  "derivedValueRules": {
    "normalizedStatus": {
      "expression": "${source.status} == \"SUCCESS\" ? \"COMPLETED\" : ${source.status}",
      "type": "STRING"
    },
    "isSuccessful": {
      "expression": "${source.status} == \"SUCCESS\"",
      "type": "BOOLEAN"
    }
  }
}
```

## Mapping Configuration

### Database Configuration

**Location**: `/workspace/database/migrations/V20241201_003__Advanced_Mapping_Configurations.sql`

**Sample Mappings Included**:

1. **FICO Falcon Fraud API Request Mapping**
2. **SAS Fraud Management API Request Mapping**
3. **Fraud API Response Mapping**
4. **Core Banking Debit Request Mapping**
5. **Core Banking Credit Request Mapping**
6. **Core Banking Response Mapping**
7. **Scheme Request Mapping**
8. **Scheme Response Mapping**
9. **Premium Tenant High-Value Transaction Mapping**

### Configuration Structure

```sql
INSERT INTO payment_engine.advanced_payload_mappings (
    id, mapping_name, tenant_id, payment_type, local_instrumentation_code, clearing_system_code,
    mapping_type, direction, field_mappings, value_assignments, derived_value_rules,
    auto_generation_rules, conditional_mappings, transformation_rules, default_values,
    version, priority, is_active, description, created_at, updated_at, created_by, updated_by
) VALUES (
    gen_random_uuid(),
    'Mapping Name',
    'tenant-id',
    'CREDIT_TRANSFER',
    'WIRE',
    NULL,
    'CUSTOM_MAPPING',
    'FRAUD_API_REQUEST',
    '{"field1": "source.field1", "field2": "source.field2"}'::jsonb,
    '{"staticField": "staticValue"}'::jsonb,
    '{"derivedField": {"expression": "${source.amount} > 1000", "type": "BOOLEAN"}}'::jsonb,
    '{"autoField": {"type": "UUID"}}'::jsonb,
    '{"condition": {"target": "field", "source": "value"}}'::jsonb,
    '{"field": "transformation"}'::jsonb,
    '{"defaultField": "defaultValue"}'::jsonb,
    '1.0',
    1,
    true,
    'Description',
    NOW(),
    NOW(),
    'system',
    'system'
);
```

## React Frontend Configuration

### Enhanced Advanced Payload Mapping Component

**Location**: `/workspace/frontend/src/components/AdvancedPayloadMapping.tsx`

**New Features**:

1. **Extended Direction Options**: Added fraud API, core banking, and scheme directions
2. **Template Buttons**: Quick template creation for common mapping types
3. **Template Functions**: Pre-configured templates for fraud, core banking, and scheme mappings

**New Direction Options**:

- `FRAUD_API_REQUEST` - Fraud API Request
- `FRAUD_API_RESPONSE` - Fraud API Response
- `CORE_BANKING_DEBIT_REQUEST` - Core Banking Debit Request
- `CORE_BANKING_DEBIT_RESPONSE` - Core Banking Debit Response
- `CORE_BANKING_CREDIT_REQUEST` - Core Banking Credit Request
- `CORE_BANKING_CREDIT_RESPONSE` - Core Banking Credit Response
- `SCHEME_REQUEST` - Scheme Request
- `SCHEME_RESPONSE` - Scheme Response

**Template Buttons**:

```typescript
<Button
  variant="outlined"
  startIcon={<TransformIcon />}
  onClick={() => createTemplateMapping('FRAUD_API_REQUEST')}
>
  Fraud API Template
</Button>

<Button
  variant="outlined"
  startIcon={<SettingsIcon />}
  onClick={() => createTemplateMapping('CORE_BANKING_DEBIT_REQUEST')}
>
  Core Banking Template
</Button>

<Button
  variant="outlined"
  startIcon={<SchemaIcon />}
  onClick={() => createTemplateMapping('SCHEME_REQUEST')}
>
  Scheme Template
</Button>
```

### Template Functions

**Fraud API Template**:

```typescript
'FRAUD_API_REQUEST': {
  mappingName: 'Fraud API Request Template',
  direction: 'FRAUD_API_REQUEST',
  mappingType: 'CUSTOM_MAPPING',
  fieldMappings: {
    "transactionId": "assessment.transactionReference",
    "amount": "amount",
    "currency": "currency",
    "timestamp": "timestamp",
    "accountNumber": "fromAccountNumber",
    "beneficiaryAccount": "toAccountNumber",
    "paymentType": "paymentType",
    "localInstrumentCode": "localInstrumentCode"
  },
  valueAssignments: {
    "channel": "API",
    "apiVersion": "2.0",
    "requestType": "FRAUD_ASSESSMENT"
  },
  derivedValueRules: {
    "riskScore": {
      "expression": "${source.amount} > 10000 ? 0.8 : 0.3",
      "type": "NUMBER"
    }
  },
  autoGenerationRules: {
    "sessionId": {
      "type": "UUID"
    },
    "requestId": {
      "type": "SEQUENTIAL",
      "prefix": "FRAUD-",
      "length": 10
    }
  }
}
```

## Best Practices

### 1. Mapping Design

- **Use Descriptive Names**: Choose clear, descriptive mapping names
- **Set Appropriate Priorities**: Higher priority mappings are applied first
- **Include Fallbacks**: Always have fallback logic in case mapping fails
- **Test Thoroughly**: Test mappings with various payload scenarios

### 2. Performance Optimization

- **Cache Mappings**: Mappings are cached for better performance
- **Minimize Complexity**: Keep derived value rules simple
- **Use Indexes**: Database indexes are created for common query patterns
- **Monitor Performance**: Monitor mapping execution times

### 3. Error Handling

- **Graceful Fallbacks**: Always fall back to original logic on mapping errors
- **Comprehensive Logging**: Log all mapping decisions and errors
- **Validation**: Validate mapping configurations before deployment
- **Testing**: Test error scenarios and fallback behavior

### 4. Security Considerations

- **Input Validation**: Validate all input data before transformation
- **Sensitive Data**: Handle sensitive data appropriately in mappings
- **Access Control**: Ensure proper access control for mapping configuration
- **Audit Trail**: Maintain audit trail for mapping changes

### 5. Maintenance

- **Version Control**: Use version control for mapping configurations
- **Documentation**: Document all custom mappings
- **Regular Review**: Regularly review and update mappings
- **Monitoring**: Monitor mapping usage and performance

## Troubleshooting

### Common Issues

1. **Mapping Not Applied**
   - Check if mapping is active (`is_active = true`)
   - Verify tenant, payment type, and clearing system match
   - Check mapping priority and direction
   - Review application logs for mapping decisions

2. **Transformation Errors**
   - Validate field mappings and expressions
   - Check for null values in source data
   - Verify expression syntax
   - Review error logs for specific issues

3. **Performance Issues**
   - Check mapping complexity
   - Review database query performance
   - Monitor cache hit rates
   - Consider mapping optimization

4. **Fallback Behavior**
   - Verify fallback logic is working
   - Check if original logic is still functional
   - Review error handling in services
   - Test fallback scenarios

### Debug Commands

```sql
-- Check active mappings for a tenant
SELECT * FROM payment_engine.advanced_payload_mappings 
WHERE tenant_id = 'tenant-001' AND is_active = true 
ORDER BY priority DESC;

-- Check mappings for specific direction
SELECT * FROM payment_engine.advanced_payload_mappings 
WHERE direction = 'FRAUD_API_REQUEST' AND is_active = true;

-- Check mapping usage statistics
SELECT direction, COUNT(*) as mapping_count
FROM payment_engine.advanced_payload_mappings 
WHERE is_active = true 
GROUP BY direction;
```

### Monitoring Queries

```sql
-- Monitor mapping performance
SELECT 
    direction,
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at))) as avg_processing_time
FROM payment_engine.advanced_payload_mappings 
WHERE created_at >= NOW() - INTERVAL '1 day'
GROUP BY direction;

-- Check mapping errors
SELECT 
    mapping_name,
    direction,
    error_message,
    created_at
FROM payment_engine.advanced_payload_mappings 
WHERE error_message IS NOT NULL
ORDER BY created_at DESC;
```

## Conclusion

The advanced mapping system provides:

- **Flexible API Integration**: Configure API transformations without code changes
- **Comprehensive Coverage**: Support for fraud, core banking, and scheme APIs
- **Performance Optimized**: Cached transformations and efficient processing
- **User-Friendly Configuration**: React frontend with templates and validation
- **Robust Error Handling**: Graceful fallbacks and comprehensive logging

This implementation significantly reduces the need for code changes when integrating with different external systems, providing a flexible and maintainable solution for API payload transformations.