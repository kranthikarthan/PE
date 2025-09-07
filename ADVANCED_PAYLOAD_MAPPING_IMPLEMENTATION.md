# Advanced Payload Mapping Implementation

## Overview

This document describes the comprehensive implementation of an **Advanced Payload Mapping System** that provides flexible mapping between database/input payloads with support for static values, derived values, conditional logic, and auto-generated IDs. The system is fully configurable per tenant, payment type, local instrumentation code, and clearing system, providing complete flexibility for payload transformation requirements.

## Key Features Implemented

### ✅ **Flexible Value Assignment**
- **Static Values**: Assign fixed values to target fields
- **Derived Values**: Calculate values based on expressions and database fields
- **Auto-Generated IDs**: Generate UUIDs, timestamps, sequential IDs, and custom values
- **Conditional Logic**: Apply different values based on conditions
- **Transformation Rules**: Apply data transformations (uppercase, formatting, etc.)

### ✅ **Tenant-Specific Configuration**
- **Per-Tenant Mappings**: Different mappings for different tenants
- **Payment Type Specificity**: Mappings specific to payment types
- **Local Instrumentation Code**: Mappings based on local instrumentation codes
- **Clearing System Specificity**: Mappings for specific clearing systems
- **Priority-Based Processing**: Configurable priority for mapping application

### ✅ **Advanced Mapping Types**
- **Field Mapping**: Direct field-to-field mapping
- **Value Assignment Mapping**: Static value assignments
- **Derived Value Mapping**: Calculated values based on expressions
- **Auto-Generation Mapping**: Auto-generated values
- **Conditional Mapping**: Conditional value assignment
- **Transformation Mapping**: Data transformation rules

### ✅ **Expression Engine**
- **Field References**: Reference source and target fields using `${source.field}` and `${target.field}`
- **Function Calls**: Built-in functions like `${uuid()}`, `${timestamp()}`, `${now()}`
- **Mathematical Expressions**: Support for arithmetic operations
- **String Operations**: String concatenation and manipulation
- **Conditional Expressions**: If-then-else logic in expressions

### ✅ **React Frontend Interface**
- **Visual Mapping Configuration**: Intuitive interface for creating mappings
- **JSON Editors**: Rich JSON editing for complex configurations
- **Statistics Dashboard**: Real-time statistics and metrics
- **Advanced Filtering**: Filter by type, direction, tenant, etc.
- **Mapping Management**: Complete CRUD operations for mappings

## Architecture Components

### 1. Database Schema

#### Advanced Payload Mappings Table
```sql
CREATE TABLE payment_engine.advanced_payload_mappings (
    id UUID PRIMARY KEY,
    mapping_name VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50),
    local_instrumentation_code VARCHAR(50),
    clearing_system_code VARCHAR(50),
    mapping_type VARCHAR(50) NOT NULL, -- FIELD_MAPPING, VALUE_ASSIGNMENT_MAPPING, etc.
    direction VARCHAR(20) NOT NULL, -- REQUEST, RESPONSE, BIDIRECTIONAL
    source_schema JSONB,
    target_schema JSONB,
    field_mappings JSONB,
    value_assignments JSONB, -- Static value assignments
    conditional_mappings JSONB, -- Conditional logic
    derived_value_rules JSONB, -- Derived value calculations
    auto_generation_rules JSONB, -- Auto-generation rules
    transformation_rules JSONB, -- Data transformations
    validation_rules JSONB,
    default_values JSONB,
    array_handling_config JSONB,
    nested_object_config JSONB,
    error_handling_config JSONB,
    performance_config JSONB,
    version VARCHAR(20) DEFAULT '1.0',
    priority INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    description VARCHAR(1000)
);
```

### 2. Backend Services

#### Advanced Payload Transformation Service
**File**: `services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/AdvancedPayloadTransformationService.java`

**Features**:
- Flexible payload transformation with multiple mapping types
- Expression evaluation engine for derived values
- Auto-generation service for IDs and timestamps
- Conditional logic engine for dynamic value assignment
- Tenant-specific configuration support
- Priority-based mapping application

**Key Methods**:
```java
public Optional<Map<String, Object>> transformPayload(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, Direction direction, Map<String, Object> sourcePayload)
private List<AdvancedPayloadMapping> findApplicableMappings(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, Direction direction)
private Map<String, Object> applyAdvancedMapping(Map<String, Object> sourcePayload, Map<String, Object> targetPayload, AdvancedPayloadMapping mapping)
private Object calculateDerivedValue(Map<String, Object> sourcePayload, Map<String, Object> targetPayload, Object ruleConfig)
private Object generateValue(Object ruleConfig)
private String processExpression(String expression, Map<String, Object> sourcePayload, Map<String, Object> targetPayload)
private boolean evaluateCondition(Map<String, Object> sourcePayload, Map<String, Object> targetPayload, String condition)
```

### 3. Frontend Components

#### Advanced Payload Mapping Component
**File**: `frontend/src/components/AdvancedPayloadMapping.tsx`

**Features**:
- Comprehensive mapping configuration interface
- JSON editors for complex configurations
- Statistics dashboard with real-time metrics
- Advanced filtering and search capabilities
- Visual mapping type indicators
- Priority-based mapping management

**Key Sections**:
- **Statistics Dashboard**: Real-time metrics and KPIs
- **Mappings List**: Filterable and searchable mappings table
- **Mapping Configuration Dialog**: Visual interface for creating/editing mappings
- **JSON Editors**: Rich JSON editing for complex configurations
- **Filtering and Search**: Advanced filtering capabilities

## Configuration Examples

### 1. Static Value Assignment

```json
{
  "valueAssignments": {
    "messageId": "PAIN001-{{uuid()}}",
    "creationDateTime": "{{timestamp()}}",
    "messageType": "pain.001",
    "version": "2013",
    "source": "payment-engine",
    "clearingSystemCode": "CLEARING_001",
    "routingCode": "ROUTE_001",
    "institutionId": "INST_001",
    "messageFormat": "ISO20022",
    "protocol": "REST",
    "endpoint": "/api/v1/clearing/process"
  }
}
```

### 2. Derived Value Rules

```json
{
  "derivedValueRules": {
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
    },
    "clearingReference": {
      "expression": "CLEARING_001-${source.transactionReference}",
      "type": "STRING"
    },
    "routingInfo": {
      "expression": "${source.currency}-${source.amount}",
      "type": "STRING"
    },
    "responseCode": {
      "expression": "${source.status} == \"SUCCESS\" ? \"ACSP\" : \"RJCT\"",
      "type": "STRING"
    },
    "responseMessage": {
      "expression": "${source.status} == \"SUCCESS\" ? \"Accepted\" : \"Rejected\"",
      "type": "STRING"
    }
  }
}
```

### 3. Auto-Generation Rules

```json
{
  "autoGenerationRules": {
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
    },
    "clearingId": {
      "type": "UUID"
    },
    "timestamp": {
      "type": "TIMESTAMP"
    },
    "requestId": {
      "type": "UUID"
    }
  }
}
```

### 4. Conditional Mappings

```json
{
  "conditionalMappings": {
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
    },
    "currency == \"USD\"": {
      "target": "clearingSystemCode",
      "source": "CLEARING_USD"
    },
    "currency == \"EUR\"": {
      "target": "clearingSystemCode",
      "source": "CLEARING_EUR"
    },
    "status == \"SUCCESS\"": {
      "target": "responseCode",
      "source": "ACSP"
    },
    "status == \"FAILED\"": {
      "target": "responseCode",
      "source": "RJCT"
    }
  }
}
```

### 5. Transformation Rules

```json
{
  "transformationRules": {
    "transactionReference": "uppercase",
    "fromAccountNumber": "uppercase",
    "toAccountNumber": "uppercase",
    "currency": "uppercase",
    "status": "uppercase",
    "statusMessage": "trim"
  }
}
```

### 6. Field Mappings

```json
{
  "fieldMappings": {
    "transactionReference": "transactionReference",
    "fromAccountNumber": "fromAccountNumber",
    "toAccountNumber": "toAccountNumber",
    "amount": "amount",
    "currency": "currency",
    "description": "description",
    "status": "status",
    "statusMessage": "statusMessage",
    "coreBankingReference": "coreBankingReference"
  }
}
```

## Usage Scenarios

### 1. PAIN.001 to PACS.008 Transformation

```java
// Transform PAIN.001 to PACS.008
Map<String, Object> pain001Payload = Map.of(
    "transactionReference", "TXN-001",
    "fromAccountNumber", "ACC-001",
    "toAccountNumber", "ACC-002",
    "amount", 1000.00,
    "currency", "USD",
    "description", "Payment transfer"
);

Optional<Map<String, Object>> pacs008Payload = transformationService.transformPayload(
    "tenant1",           // tenantId
    "TRANSFER",          // paymentType
    "LOCAL_INSTRUMENT_001", // localInstrumentationCode
    "CLEARING_001",      // clearingSystemCode
    Direction.REQUEST,   // direction
    pain001Payload       // source payload
);
```

**Result**:
```json
{
  "transactionReference": "TXN-001",
  "fromAccountNumber": "ACC-001",
  "toAccountNumber": "ACC-002",
  "amount": 1000.00,
  "currency": "USD",
  "description": "Payment transfer",
  "messageId": "PAIN001-550e8400-e29b-41d4-a716-446655440000",
  "creationDateTime": "2024-01-15T10:30:00",
  "messageType": "pain.001",
  "version": "2013",
  "source": "payment-engine",
  "totalAmount": 1000.00,
  "formattedAmount": 100000,
  "displayAmount": "USD 1000.00",
  "transactionId": "TXN-123456789012345-PAIN001",
  "paymentTypeCode": "TRA",
  "requiresApproval": "false",
  "processingMode": "IMMEDIATE",
  "priority": "NORMAL",
  "channel": "API"
}
```

### 2. PACS.008 to PACS.002 Response Transformation

```java
// Transform PACS.008 response to PACS.002
Map<String, Object> pacs008Response = Map.of(
    "transactionReference", "TXN-001",
    "status", "SUCCESS",
    "statusMessage", "Transaction processed successfully",
    "coreBankingReference", "CB-REF-001"
);

Optional<Map<String, Object>> pacs002Payload = transformationService.transformPayload(
    "tenant1",           // tenantId
    "TRANSFER",          // paymentType
    "LOCAL_INSTRUMENT_001", // localInstrumentationCode
    "CLEARING_001",      // clearingSystemCode
    Direction.RESPONSE,  // direction
    pacs008Response      // source payload
);
```

**Result**:
```json
{
  "transactionReference": "TXN-001",
  "status": "SUCCESS",
  "statusMessage": "Transaction processed successfully",
  "coreBankingReference": "CB-REF-001",
  "messageId": "PACS002-550e8400-e29b-41d4-a716-446655440001",
  "creationDateTime": "2024-01-15T10:31:00",
  "messageType": "pacs.002",
  "version": "2013",
  "source": "clearing-system",
  "responseCode": "ACSP",
  "responseMessage": "Accepted",
  "responseId": "RESP-123456789012345-PACS002",
  "processingMode": "IMMEDIATE",
  "priority": "NORMAL"
}
```

### 3. Clearing System Specific Mapping

```java
// Transform for specific clearing system
Map<String, Object> clearingPayload = Map.of(
    "transactionReference", "TXN-002",
    "amount", 2500.00,
    "currency", "EUR"
);

Optional<Map<String, Object>> clearingRequest = transformationService.transformPayload(
    "tenant1",           // tenantId
    "TRANSFER",          // paymentType
    "LOCAL_INSTRUMENT_002", // localInstrumentationCode
    "CLEARING_001",      // clearingSystemCode
    Direction.REQUEST,   // direction
    clearingPayload      // source payload
);
```

**Result**:
```json
{
  "transactionReference": "TXN-002",
  "amount": 2500.00,
  "currency": "EUR",
  "clearingSystemCode": "CLEARING_EUR",
  "routingCode": "ROUTE_001",
  "institutionId": "INST_001",
  "messageFormat": "ISO20022",
  "protocol": "REST",
  "endpoint": "/api/v1/clearing/process",
  "clearingReference": "CLEARING_001-TXN-002",
  "routingInfo": "EUR-2500.00",
  "clearingId": "550e8400-e29b-41d4-a716-446655440002",
  "timestamp": "2024-01-15T10:32:00",
  "timeout": 30000,
  "retryAttempts": 3
}
```

## Expression Engine Features

### 1. Field References
- `${source.field}` - Reference source payload fields
- `${target.field}` - Reference target payload fields
- `${source.nested.field}` - Reference nested fields

### 2. Built-in Functions
- `${uuid()}` - Generate UUID
- `${timestamp()}` - Generate timestamp
- `${now()}` - Current date/time
- `${date()}` - Current date
- `${time()}` - Current time

### 3. Mathematical Expressions
- `${source.amount} * 100` - Arithmetic operations
- `${source.amount} + ${source.fee}` - Addition
- `${source.amount} - ${source.discount}` - Subtraction
- `${source.amount} / 100` - Division

### 4. String Operations
- `${source.currency} ${source.amount}` - String concatenation
- `CLEARING_001-${source.transactionReference}` - Prefix/suffix

### 5. Conditional Expressions
- `${source.status} == "SUCCESS" ? "ACSP" : "RJCT"` - Ternary operator
- `${source.amount} > 10000 ? "HIGH" : "NORMAL"` - Conditional logic

### 6. Conditional Mappings
- `paymentType == "TRANSFER"` - String equality
- `amount > 10000` - Numeric comparison
- `currency == "USD"` - Currency-specific logic
- `status == "SUCCESS"` - Status-based logic

## Auto-Generation Types

### 1. UUID Generation
```json
{
  "messageId": {
    "type": "UUID"
  }
}
```

### 2. Timestamp Generation
```json
{
  "creationDateTime": {
    "type": "TIMESTAMP"
  }
}
```

### 3. Sequential ID Generation
```json
{
  "transactionId": {
    "type": "SEQUENTIAL",
    "prefix": "TXN-",
    "suffix": "-PAIN001",
    "length": 15
  }
}
```

### 4. Random String Generation
```json
{
  "randomId": {
    "type": "RANDOM_STRING",
    "length": 10
  }
}
```

### 5. Custom Value Generation
```json
{
  "customValue": {
    "type": "CUSTOM",
    "value": "CUSTOM_VALUE"
  }
}
```

## Benefits

### 1. **Complete Flexibility**
- Static values, derived values, and auto-generated values
- Conditional logic for dynamic value assignment
- Expression engine for complex calculations
- Multiple mapping types for different scenarios

### 2. **Tenant-Specific Configuration**
- Different mappings per tenant
- Payment type specific configurations
- Local instrumentation code specific mappings
- Clearing system specific configurations

### 3. **Maintainability**
- Centralized configuration management
- Version control for mappings
- Easy rollback capabilities
- Comprehensive documentation

### 4. **Performance**
- Caching for frequently used mappings
- Priority-based processing
- Optimized expression evaluation
- Efficient field resolution

### 5. **User Experience**
- Visual configuration interface
- JSON editors for complex configurations
- Real-time validation
- Comprehensive statistics and monitoring

### 6. **Scalability**
- Multi-tenant support
- Configurable priority levels
- Efficient database queries
- Horizontal scaling support

## Security Considerations

1. **Expression Validation**: Validate all expressions for security
2. **Input Sanitization**: Sanitize all input values
3. **Access Control**: Role-based access to mapping configuration
4. **Audit Logging**: Complete audit trail of mapping changes
5. **Data Protection**: Encrypt sensitive mapping data
6. **Validation Rules**: Comprehensive validation of all configurations

## Monitoring and Observability

1. **Mapping Metrics**: Track mapping usage and performance
2. **Expression Evaluation**: Monitor expression evaluation performance
3. **Error Tracking**: Track and alert on mapping errors
4. **Usage Analytics**: Monitor mapping usage patterns
5. **Performance Monitoring**: Track transformation performance
6. **Health Checks**: Regular health checks for mapping services

## Future Enhancements

1. **Machine Learning**: AI-powered mapping suggestions
2. **Visual Mapping Tools**: Drag-and-drop mapping interface
3. **Advanced Expressions**: More sophisticated expression language
4. **Real-time Validation**: Live validation of mapping configurations
5. **API Integration**: RESTful API for external integrations
6. **Workflow Automation**: Automated mapping generation

## Conclusion

The Advanced Payload Mapping System provides a comprehensive solution for flexible payload transformation with support for static values, derived values, conditional logic, and auto-generated IDs. It offers complete configurability per tenant, payment type, local instrumentation code, and clearing system, enabling rapid adaptation to different integration requirements without code changes.

The system is designed for enterprise-scale deployments with multi-tenant support, comprehensive security, extensive monitoring, and automated processing capabilities. It represents a significant advancement in payload transformation flexibility and provides the tools necessary for effective integration management.