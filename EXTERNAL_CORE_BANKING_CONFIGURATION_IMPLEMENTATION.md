# External Core Banking Configuration Implementation

## Overview

This document describes the comprehensive implementation of **endpoint-level configuration and payload schema mapping** for external core banking integration. The system now provides complete configurability from the React frontend for both endpoint-level settings and payload transformations between internal data models and external core banking system payloads.

## Key Features Implemented

### ✅ **Complete Endpoint-Level Configuration**
- **Dynamic Endpoint Resolution**: Runtime configuration of endpoints without code changes
- **HTTP Method Configuration**: Support for GET, POST, PUT, DELETE, PATCH
- **Custom Endpoint Paths**: Configurable URL patterns with parameter substitution
- **Request Headers**: Customizable HTTP headers per endpoint
- **Query Parameters**: Dynamic query parameter configuration
- **Authentication Configuration**: Endpoint-specific authentication settings
- **Timeout & Retry Configuration**: Per-endpoint timeout and retry settings
- **Circuit Breaker Configuration**: Endpoint-specific circuit breaker settings
- **Rate Limiting Configuration**: Per-endpoint rate limiting rules

### ✅ **Advanced Payload Schema Mapping**
- **Field-Level Mapping**: Direct field-to-field mapping between schemas
- **Object Mapping**: Complex object structure transformations
- **Array Mapping**: Array handling and transformation
- **Nested Object Mapping**: Deep nested object transformations
- **Conditional Mapping**: Rule-based conditional transformations
- **Data Transformation Rules**: Built-in and custom transformation functions
- **Validation Rules**: Comprehensive payload validation
- **Default Values**: Configurable default values for missing fields
- **Bidirectional Mapping**: Support for both request and response transformations

### ✅ **React Frontend Configuration Interface**
- **Endpoint Management**: Complete CRUD operations for endpoint configurations
- **Payload Mapping Management**: Visual configuration of schema mappings
- **JSON Editor Integration**: Rich JSON editing for complex configurations
- **Real-time Validation**: Form validation with error handling
- **Testing Capabilities**: Endpoint testing and validation
- **Visual Mapping Tools**: Drag-and-drop style mapping configuration
- **Version Management**: Schema mapping versioning support

## Architecture Components

### 1. Database Schema

#### Core Banking Endpoint Configurations Table
```sql
CREATE TABLE payment_engine.core_banking_endpoint_configurations (
    id UUID PRIMARY KEY,
    core_banking_config_id UUID REFERENCES core_banking_configurations(id),
    endpoint_name VARCHAR(100) NOT NULL,
    endpoint_type VARCHAR(50) NOT NULL, -- ACCOUNT_INFO, DEBIT_TRANSACTION, etc.
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
    description VARCHAR(1000)
);
```

#### Payload Schema Mappings Table
```sql
CREATE TABLE payment_engine.payload_schema_mappings (
    id UUID PRIMARY KEY,
    endpoint_config_id UUID REFERENCES core_banking_endpoint_configurations(id),
    mapping_name VARCHAR(100) NOT NULL,
    mapping_type VARCHAR(50) NOT NULL, -- FIELD_MAPPING, OBJECT_MAPPING, etc.
    direction VARCHAR(20) NOT NULL, -- REQUEST, RESPONSE, BIDIRECTIONAL
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
    description VARCHAR(1000)
);
```

### 2. Backend Services

#### Dynamic Endpoint Resolution Service
**File**: `services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/DynamicEndpointResolutionService.java`

**Features**:
- Runtime endpoint resolution based on tenant and operation type
- Caching for performance optimization
- Support for endpoint-specific configurations
- Fallback mechanisms for missing configurations
- Health check integration

**Key Methods**:
```java
public Optional<EndpointResolution> resolveEndpoint(String tenantId, EndpointType endpointType)
public Optional<EndpointResolution> resolveEndpointByName(String tenantId, String endpointName)
public List<EndpointResolution> getAllEndpoints(String tenantId)
public boolean isEndpointAvailable(String tenantId, EndpointType endpointType)
public String getEndpointUrl(String tenantId, EndpointType endpointType)
public Map<String, String> getRequestHeaders(String tenantId, EndpointType endpointType)
```

#### Payload Transformation Service
**File**: `services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/PayloadTransformationService.java`

**Features**:
- Dynamic payload transformation based on configurable mappings
- Support for complex transformation rules
- Validation against schema mappings
- Error handling and rollback mechanisms
- Caching for performance

**Key Methods**:
```java
public Optional<Map<String, Object>> transformRequestPayload(UUID endpointConfigId, String mappingName, Map<String, Object> sourcePayload)
public Optional<Map<String, Object>> transformResponsePayload(UUID endpointConfigId, String mappingName, Map<String, Object> sourcePayload)
public ValidationResult validatePayload(UUID endpointConfigId, String mappingName, Map<String, Object> payload, Direction direction)
public List<PayloadSchemaMapping> getAvailableMappings(UUID endpointConfigId)
```

### 3. Frontend Components

#### Endpoint Configuration Component
**File**: `frontend/src/components/EndpointConfiguration.tsx`

**Features**:
- Tabbed interface for endpoints and payload mappings
- Form validation with real-time error handling
- JSON editor integration for complex configurations
- Endpoint testing capabilities
- Visual mapping tools
- CRUD operations with confirmation dialogs

**Key Sections**:
- **Endpoint Management**: Create, edit, delete, and test endpoints
- **Payload Mapping Management**: Configure schema mappings with visual tools
- **Configuration Forms**: Comprehensive forms with validation
- **JSON Editors**: Rich JSON editing for complex configurations

## Configuration Examples

### 1. Endpoint Configuration Example

```json
{
  "endpointName": "Get Account Info",
  "endpointType": "ACCOUNT_INFO",
  "httpMethod": "GET",
  "endpointPath": "/api/v1/accounts/{accountNumber}",
  "requestHeaders": {
    "Content-Type": "application/json",
    "Accept": "application/json",
    "Authorization": "Bearer {token}"
  },
  "queryParameters": {
    "includeBalance": "true",
    "includeTransactions": "false"
  },
  "authenticationConfig": {
    "type": "API_KEY",
    "headerName": "X-API-Key",
    "value": "{apiKey}"
  },
  "timeoutMs": 30000,
  "retryAttempts": 3,
  "circuitBreakerConfig": {
    "failureThreshold": 5,
    "timeoutDuration": 60000,
    "retryInterval": 10000
  },
  "rateLimitingConfig": {
    "requestsPerMinute": 100,
    "burstSize": 10
  },
  "validationRules": {
    "accountNumber": {
      "required": true,
      "pattern": "^[A-Z0-9]{10,20}$"
    }
  }
}
```

### 2. Payload Schema Mapping Example

```json
{
  "mappingName": "Account Info Request Mapping",
  "mappingType": "FIELD_MAPPING",
  "direction": "REQUEST",
  "fieldMappings": {
    "accountNumber": "accountNumber",
    "tenantId": "tenantId",
    "requestId": "requestId",
    "timestamp": "timestamp"
  },
  "transformationRules": {
    "accountNumber": "uppercase",
    "timestamp": "date_format",
    "requestId": "uuid_generate"
  },
  "validationRules": {
    "accountNumber": {
      "required": true,
      "type": "string",
      "minLength": 10,
      "maxLength": 20,
      "pattern": "^[A-Z0-9]+$"
    },
    "tenantId": {
      "required": true,
      "type": "string",
      "minLength": 1,
      "maxLength": 50
    }
  },
  "defaultValues": {
    "requestId": "{{uuid()}}",
    "timestamp": "{{now()}}",
    "source": "payment-engine"
  },
  "conditionalMappings": {
    "accountType == 'BUSINESS'": {
      "target": "includeBusinessDetails",
      "source": "true"
    },
    "accountType == 'PERSONAL'": {
      "target": "includePersonalDetails",
      "source": "true"
    }
  }
}
```

### 3. Response Mapping Example

```json
{
  "mappingName": "Account Info Response Mapping",
  "mappingType": "OBJECT_MAPPING",
  "direction": "RESPONSE",
  "fieldMappings": {
    "accountNumber": "accountNumber",
    "accountName": "accountName",
    "accountType": "accountType",
    "currency": "currency",
    "balance": "balance",
    "availableBalance": "availableBalance",
    "status": "status",
    "bankCode": "bankCode",
    "bankName": "bankName",
    "lastUpdated": "lastUpdated"
  },
  "transformationRules": {
    "balance": "currency_format",
    "availableBalance": "currency_format",
    "lastUpdated": "date_format",
    "status": "uppercase"
  },
  "validationRules": {
    "accountNumber": {
      "required": true,
      "type": "string"
    },
    "balance": {
      "required": true,
      "type": "number",
      "minimum": 0
    },
    "availableBalance": {
      "required": true,
      "type": "number",
      "minimum": 0
    }
  },
  "nestedObjectConfig": {
    "address": {
      "street": "address.street",
      "city": "address.city",
      "country": "address.country",
      "postalCode": "address.postalCode"
    },
    "contact": {
      "phone": "contact.phone",
      "email": "contact.email"
    }
  }
}
```

## Usage Scenarios

### 1. Adding a New Core Banking System

1. **Configure Core Banking System**:
   - Set up basic connection details (URL, authentication)
   - Configure adapter type (REST/gRPC)

2. **Configure Endpoints**:
   - Define endpoint paths for each operation
   - Set up request headers and authentication
   - Configure timeouts and retry policies

3. **Configure Payload Mappings**:
   - Map internal data model to external system format
   - Set up validation rules
   - Configure transformation rules

4. **Test Configuration**:
   - Test individual endpoints
   - Validate payload transformations
   - Verify end-to-end integration

### 2. Modifying Existing Integration

1. **Update Endpoint Configuration**:
   - Modify endpoint paths or headers
   - Adjust timeout or retry settings
   - Update authentication configuration

2. **Update Payload Mappings**:
   - Modify field mappings
   - Add new transformation rules
   - Update validation rules

3. **Version Management**:
   - Create new versions of mappings
   - Test new configurations
   - Deploy with rollback capability

### 3. Multi-Tenant Configuration

1. **Tenant-Specific Endpoints**:
   - Different endpoints per tenant
   - Tenant-specific authentication
   - Custom timeout and retry settings

2. **Tenant-Specific Mappings**:
   - Custom field mappings per tenant
   - Tenant-specific validation rules
   - Custom transformation logic

## Benefits

### 1. **Complete Configurability**
- No code changes required for new integrations
- Runtime configuration updates
- Visual configuration interface

### 2. **Flexibility**
- Support for any REST API structure
- Custom payload transformations
- Conditional mapping logic

### 3. **Maintainability**
- Centralized configuration management
- Version control for mappings
- Easy rollback capabilities

### 4. **Scalability**
- Multi-tenant support
- Performance optimization through caching
- Load balancing and circuit breaker patterns

### 5. **Reliability**
- Comprehensive validation
- Error handling and recovery
- Health monitoring and alerting

### 6. **Developer Experience**
- Visual configuration tools
- Real-time validation
- Testing capabilities
- Comprehensive documentation

## Security Considerations

1. **Authentication Configuration**: Secure storage of API keys and certificates
2. **Input Validation**: Comprehensive validation of all configuration inputs
3. **Access Control**: Role-based access to configuration management
4. **Audit Logging**: Complete audit trail of configuration changes
5. **Encryption**: Encryption of sensitive configuration data
6. **Network Security**: TLS/SSL for all external communications

## Monitoring and Observability

1. **Configuration Metrics**: Track configuration usage and performance
2. **Transformation Metrics**: Monitor payload transformation performance
3. **Error Tracking**: Comprehensive error logging and alerting
4. **Health Checks**: Regular health checks for configured endpoints
5. **Performance Monitoring**: Track response times and throughput
6. **Usage Analytics**: Monitor configuration usage patterns

## Future Enhancements

1. **GraphQL Support**: Add GraphQL endpoint configuration
2. **WebSocket Support**: Real-time communication configuration
3. **Advanced Transformations**: More sophisticated transformation functions
4. **AI-Powered Mapping**: Machine learning for automatic mapping suggestions
5. **Visual Mapping Tools**: Drag-and-drop mapping interface
6. **API Documentation Integration**: Automatic API documentation generation
7. **Testing Framework**: Comprehensive testing framework for configurations
8. **Performance Optimization**: Advanced caching and optimization strategies

## Conclusion

The external core banking configuration implementation provides a comprehensive, flexible, and maintainable solution for integrating with external core banking systems. It offers complete configurability from the React frontend, supporting both endpoint-level configuration and sophisticated payload schema mapping. The system is designed for enterprise-scale deployments with multi-tenant support, comprehensive security, and extensive monitoring capabilities.

The implementation enables rapid integration with new core banking systems without code changes, provides visual configuration tools for non-technical users, and includes comprehensive testing and validation capabilities. It represents a significant advancement in the flexibility and maintainability of core banking integrations.