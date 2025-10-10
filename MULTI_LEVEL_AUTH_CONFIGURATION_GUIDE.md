# Multi-Level Authentication Configuration Guide

## Overview

This document provides a comprehensive guide for the multi-level authentication configuration system that supports JWS, JWT, and client ID+secret configuration at multiple levels:

1. **Clearing System Level** - Global clearing system configuration
2. **Local Instrumentation Code Level** - Code-level configuration
3. **Payment Type Level** - Different authentication for different payment types
4. **Downstream Call Per Tenant Level** - Tenant-specific downstream call configuration

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Configuration Levels](#configuration-levels)
3. [Configuration Hierarchy](#configuration-hierarchy)
4. [Authentication Methods](#authentication-methods)
5. [Implementation Details](#implementation-details)
6. [API Usage](#api-usage)
7. [Frontend Integration](#frontend-integration)
8. [Testing](#testing)
9. [Best Practices](#best-practices)

---

## Architecture Overview

### Multi-Level Configuration System

```
┌─────────────────────────────────────────────────────────────────┐
│                Multi-Level Authentication Configuration        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              Configuration Hierarchy                       │ │
│  │                                                             │ │
│  │  Priority 1: Downstream Call Level (Highest)              │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │  Tenant + Service Type + Endpoint + Payment Type       │ │ │
│  │  │  Example: tenant-001/fraud//fraud/SEPA                 │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │                                                             │ │
│  │  Priority 2: Payment Type Level                            │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │  Tenant + Payment Type                                 │ │ │
│  │  │  Example: tenant-001/SEPA                              │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │                                                             │ │
│  │  Priority 3: Tenant Level                                  │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │  Tenant Only                                           │ │ │
│  │  │  Example: tenant-001                                   │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │                                                             │ │
│  │  Priority 4: Clearing System Level (Lowest)               │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │  Environment Only                                      │ │ │
│  │  │  Example: dev/staging/prod                             │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Configuration Resolution Process

```
┌─────────────────────────────────────────────────────────────────┐
│                Configuration Resolution Flow                    │
│                                                                 │
│  Request: tenant-001/fraud//fraud/SEPA                        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  1. Start with Clearing System Level (dev)                 │ │
│  │     - JWT with dev-jwt-secret                              │ │
│  │     - Client headers enabled                               │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  2. Apply Tenant Level (tenant-001)                        │ │
│  │     - Override to JWS with tenant-jws-secret               │ │
│  │     - Keep client headers enabled                          │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  3. Apply Payment Type Level (SEPA)                        │ │
│  │     - Override to OAuth2 with sepa-oauth2-config           │ │
│  │     - Override client headers to sepa-specific             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  4. Apply Downstream Call Level (fraud//fraud)             │ │
│  │     - Override to API Key with fraud-api-key               │ │
│  │     - Override timeout to 30s                              │ │
│  │     - Override retry attempts to 3                         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  Final Resolved Configuration:                             │ │
│  │  - Auth Method: API Key                                    │ │
│  │  - API Key: fraud-api-key                                  │ │
│  │  - Client Headers: sepa-specific                           │ │
│  │  - Timeout: 30s                                            │ │
│  │  - Retry Attempts: 3                                       │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Configuration Levels

### 1. Clearing System Level (Priority 4 - Lowest)

**Purpose**: Global configuration for the entire clearing system environment.

**Scope**: Applies to all operations in the environment (dev, staging, prod).

**Configuration**:
```json
{
  "environment": "dev",
  "authMethod": "JWT",
  "jwtSecret": "dev-jwt-secret-key",
  "jwtIssuer": "payment-engine-dev",
  "jwtAudience": "payment-engine-api",
  "jwtExpirationSeconds": 3600,
  "includeClientHeaders": true,
  "clientId": "dev-client-id",
  "clientSecret": "dev-client-secret",
  "clientIdHeaderName": "X-Client-ID",
  "clientSecretHeaderName": "X-Client-Secret",
  "isActive": true,
  "description": "Default development environment configuration"
}
```

**Use Cases**:
- Default authentication method for the environment
- Global client header configuration
- Environment-specific security settings
- Fallback configuration when no higher-level config exists

### 2. Tenant Level (Priority 3)

**Purpose**: Tenant-specific configuration that overrides clearing system level.

**Scope**: Applies to all operations for a specific tenant.

**Configuration**:
```json
{
  "tenantId": "tenant-001",
  "authMethod": "JWS",
  "jwsSecret": "tenant-001-jws-secret",
  "jwsAlgorithm": "HS256",
  "jwsIssuer": "payment-engine-tenant-001",
  "jwsAudience": "payment-engine-api",
  "jwsExpirationSeconds": 1800,
  "includeClientHeaders": true,
  "clientId": "tenant-001-client-id",
  "clientSecret": "tenant-001-client-secret",
  "clientIdHeaderName": "X-Tenant-Client-ID",
  "clientSecretHeaderName": "X-Tenant-Client-Secret",
  "isActive": true,
  "description": "Tenant-001 specific configuration"
}
```

**Use Cases**:
- Tenant-specific authentication methods
- Tenant-specific client credentials
- Tenant-specific security policies
- Override global environment settings

### 3. Payment Type Level (Priority 2)

**Purpose**: Configuration specific to payment types (SEPA, SWIFT, ACH, etc.).

**Scope**: Applies to specific payment types for a tenant.

**Configuration**:
```json
{
  "tenantId": "tenant-001",
  "paymentType": "SEPA",
  "authMethod": "OAUTH2",
  "oauth2TokenEndpoint": "https://sepa-auth.example.com/oauth/token",
  "oauth2ClientId": "sepa-client-id",
  "oauth2ClientSecret": "sepa-client-secret",
  "oauth2Scope": "sepa:payments",
  "includeClientHeaders": true,
  "clientId": "sepa-client-id",
  "clientSecret": "sepa-client-secret",
  "clientIdHeaderName": "X-SEPA-Client-ID",
  "clientSecretHeaderName": "X-SEPA-Client-Secret",
  "clearingSystem": "SEPA_CLEARING",
  "currency": "EUR",
  "isHighValue": false,
  "isActive": true,
  "description": "SEPA payment type configuration for tenant-001"
}
```

**Use Cases**:
- Payment type specific authentication
- Different clearing systems per payment type
- Currency-specific configurations
- High-value payment handling

### 4. Downstream Call Level (Priority 1 - Highest)

**Purpose**: Most granular configuration for specific downstream calls.

**Scope**: Applies to specific service type + endpoint combinations.

**Configuration**:
```json
{
  "tenantId": "tenant-001",
  "serviceType": "fraud",
  "endpoint": "/fraud",
  "paymentType": "SEPA",
  "authMethod": "API_KEY",
  "apiKey": "fraud-api-key-12345",
  "apiKeyHeaderName": "X-Fraud-API-Key",
  "includeClientHeaders": true,
  "clientId": "fraud-client-id",
  "clientSecret": "fraud-client-secret",
  "clientIdHeaderName": "X-Fraud-Client-ID",
  "clientSecretHeaderName": "X-Fraud-Client-Secret",
  "targetHost": "fraud.bank-nginx.example.com",
  "targetPort": 443,
  "targetProtocol": "HTTPS",
  "targetPath": "/fraud",
  "timeoutSeconds": 30,
  "retryAttempts": 3,
  "retryDelaySeconds": 5,
  "isActive": true,
  "description": "Fraud system configuration for tenant-001 SEPA payments"
}
```

**Use Cases**:
- Service-specific authentication
- Endpoint-specific configurations
- Custom timeout and retry settings
- Target host and port configuration

---

## Configuration Hierarchy

### Precedence Rules

1. **Downstream Call Level** (Highest Priority)
   - Overrides all other levels
   - Most specific configuration
   - Used for individual service calls

2. **Payment Type Level** (Second Priority)
   - Overrides tenant and clearing system levels
   - Used for payment type specific operations
   - Falls back to downstream call level if not configured

3. **Tenant Level** (Third Priority)
   - Overrides clearing system level
   - Used for tenant-specific operations
   - Falls back to payment type level if not configured

4. **Clearing System Level** (Lowest Priority)
   - Used as fallback when no higher-level configuration exists
   - Global environment configuration
   - Always available as default

### Configuration Merging

```java
// Configuration resolution process
public ResolvedAuthConfiguration getResolvedConfiguration(
    String tenantId, String serviceType, String endpoint, String paymentType) {
    
    ResolvedAuthConfiguration resolved = new ResolvedAuthConfiguration();
    
    // 1. Start with clearing system level (lowest priority)
    Optional<ClearingSystemAuthConfiguration> clearingSystemConfig = 
        clearingSystemAuthConfigRepository.findByEnvironmentAndIsActive(currentEnvironment, true);
    if (clearingSystemConfig.isPresent()) {
        resolved.mergeFrom(clearingSystemConfig.get());
    }
    
    // 2. Apply tenant level configuration
    Optional<TenantAuthConfiguration> tenantConfig = 
        tenantAuthConfigRepository.findByTenantIdAndIsActive(tenantId, true);
    if (tenantConfig.isPresent()) {
        resolved.mergeFrom(tenantConfig.get());
    }
    
    // 3. Apply payment type level configuration
    if (paymentType != null) {
        Optional<PaymentTypeAuthConfiguration> paymentTypeConfig = 
            paymentTypeAuthConfigRepository.findByTenantIdAndPaymentTypeAndIsActive(tenantId, paymentType, true);
        if (paymentTypeConfig.isPresent()) {
            resolved.mergeFrom(paymentTypeConfig.get());
        }
    }
    
    // 4. Apply downstream call level configuration (highest priority)
    Optional<DownstreamCallAuthConfiguration> downstreamConfig = 
        downstreamCallAuthConfigRepository.findByTenantIdAndServiceTypeAndEndpointAndIsActive(
            tenantId, serviceType, endpoint, true);
    if (downstreamConfig.isPresent()) {
        resolved.mergeFrom(downstreamConfig.get());
    }
    
    return resolved;
}
```

---

## Authentication Methods

### 1. JWT (JSON Web Token)

**Configuration**:
```json
{
  "authMethod": "JWT",
  "jwtSecret": "your-jwt-secret-key",
  "jwtIssuer": "payment-engine",
  "jwtAudience": "payment-engine-api",
  "jwtExpirationSeconds": 3600
}
```

**Usage**:
- Standard JWT tokens with HS256/HS512 algorithms
- Configurable issuer and audience
- Configurable expiration time
- Used for stateless authentication

### 2. JWS (JSON Web Signature)

**Configuration**:
```json
{
  "authMethod": "JWS",
  "jwsSecret": "your-jws-secret-key",
  "jwsAlgorithm": "HS256",
  "jwsIssuer": "payment-engine",
  "jwsAudience": "payment-engine-api",
  "jwsExpirationSeconds": 1800
}
```

**Usage**:
- Enhanced JWT with additional signature validation
- Support for HS256, HS384, HS512, RS256, RS384, RS512 algorithms
- More secure than standard JWT
- Configurable signature algorithms

### 3. OAuth2

**Configuration**:
```json
{
  "authMethod": "OAUTH2",
  "oauth2TokenEndpoint": "https://auth.example.com/oauth/token",
  "oauth2ClientId": "your-client-id",
  "oauth2ClientSecret": "your-client-secret",
  "oauth2Scope": "payments:read payments:write"
}
```

**Usage**:
- Industry standard OAuth2 authentication
- Configurable token endpoint
- Configurable scopes
- Used for third-party integrations

### 4. API Key

**Configuration**:
```json
{
  "authMethod": "API_KEY",
  "apiKey": "your-api-key-12345",
  "apiKeyHeaderName": "X-API-Key"
}
```

**Usage**:
- Simple API key authentication
- Configurable header name
- Used for service-to-service communication
- Quick and easy to implement

### 5. Basic Authentication

**Configuration**:
```json
{
  "authMethod": "BASIC",
  "basicAuthUsername": "username",
  "basicAuthPassword": "password"
}
```

**Usage**:
- HTTP Basic Authentication
- Username and password based
- Used for legacy system integration
- Simple credential-based authentication

---

## Implementation Details

### Database Schema

#### Clearing System Auth Configuration
```sql
CREATE TABLE clearing_system_auth_configuration (
    id UUID PRIMARY KEY,
    environment VARCHAR(50) NOT NULL,
    auth_method VARCHAR(20) NOT NULL,
    jwt_secret VARCHAR(500),
    jwt_issuer VARCHAR(100),
    jwt_audience VARCHAR(100),
    jwt_expiration_seconds INTEGER,
    jws_secret VARCHAR(500),
    jws_algorithm VARCHAR(20),
    jws_issuer VARCHAR(100),
    jws_audience VARCHAR(100),
    jws_expiration_seconds INTEGER,
    oauth2_token_endpoint VARCHAR(500),
    oauth2_client_id VARCHAR(500),
    oauth2_client_secret VARCHAR(500),
    oauth2_scope VARCHAR(100),
    api_key VARCHAR(500),
    api_key_header_name VARCHAR(100),
    basic_auth_username VARCHAR(100),
    basic_auth_password VARCHAR(500),
    include_client_headers BOOLEAN DEFAULT FALSE NOT NULL,
    client_id VARCHAR(100),
    client_secret VARCHAR(500),
    client_id_header_name VARCHAR(100),
    client_secret_header_name VARCHAR(100),
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Payment Type Auth Configuration
```sql
CREATE TABLE payment_type_auth_configuration (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    auth_method VARCHAR(20) NOT NULL,
    -- ... authentication fields ...
    clearing_system VARCHAR(100),
    routing_code VARCHAR(100),
    currency VARCHAR(100),
    is_high_value BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Downstream Call Auth Configuration
```sql
CREATE TABLE downstream_call_auth_configuration (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    endpoint VARCHAR(200) NOT NULL,
    payment_type VARCHAR(50),
    auth_method VARCHAR(20) NOT NULL,
    -- ... authentication fields ...
    target_host VARCHAR(500),
    target_port INTEGER,
    target_protocol VARCHAR(20),
    target_path VARCHAR(100),
    timeout_seconds INTEGER,
    retry_attempts INTEGER,
    retry_delay_seconds INTEGER,
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Service Implementation

#### Multi-Level Auth Configuration Service
```java
@Service
@Transactional
public class MultiLevelAuthConfigurationService {
    
    public ResolvedAuthConfiguration getResolvedConfiguration(
        String tenantId, String serviceType, String endpoint, String paymentType) {
        
        ResolvedAuthConfiguration resolved = new ResolvedAuthConfiguration();
        
        // Apply configurations in order of precedence
        applyClearingSystemLevel(resolved);
        applyTenantLevel(resolved, tenantId);
        applyPaymentTypeLevel(resolved, tenantId, paymentType);
        applyDownstreamCallLevel(resolved, tenantId, serviceType, endpoint);
        
        return resolved;
    }
    
    private void applyClearingSystemLevel(ResolvedAuthConfiguration resolved) {
        // Implementation for clearing system level
    }
    
    private void applyTenantLevel(ResolvedAuthConfiguration resolved, String tenantId) {
        // Implementation for tenant level
    }
    
    private void applyPaymentTypeLevel(ResolvedAuthConfiguration resolved, String tenantId, String paymentType) {
        // Implementation for payment type level
    }
    
    private void applyDownstreamCallLevel(ResolvedAuthConfiguration resolved, String tenantId, String serviceType, String endpoint) {
        // Implementation for downstream call level
    }
}
```

#### Enhanced Downstream Routing Service
```java
@Service
public class EnhancedDownstreamRoutingService {
    
    @Autowired
    private MultiLevelAuthConfigurationService multiLevelAuthConfigService;
    
    public <T> ResponseEntity<T> callExternalService(
        String tenantId, String serviceType, String endpoint, String paymentType,
        Object requestBody, Class<T> responseType, Map<String, String> additionalHeaders) {
        
        // Resolve configuration from all levels
        ResolvedAuthConfiguration config = multiLevelAuthConfigService.getResolvedConfiguration(
            tenantId, serviceType, endpoint, paymentType);
        
        // Build URL from resolved configuration
        String url = buildUrl(config);
        
        // Create headers with resolved configuration
        HttpHeaders headers = createHeaders(tenantId, serviceType, endpoint, paymentType, config, additionalHeaders);
        
        // Make the call
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
    }
}
```

---

## API Usage

### Enhanced Downstream Routing API

#### Call External Service
```bash
curl -X POST https://payment-engine.local/api/v1/enhanced-downstream/call \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{
    "tenantId": "tenant-001",
    "serviceType": "fraud",
    "endpoint": "/fraud",
    "paymentType": "SEPA",
    "requestBody": {
      "transaction_id": "TXN-123",
      "amount": 1000,
      "fraud_check": true
    }
  }'
```

#### Call Fraud System
```bash
curl -X POST https://payment-engine.local/api/v1/enhanced-downstream/fraud/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{
    "transaction_id": "TXN-123",
    "amount": 1000,
    "fraud_check": true
  }'
```

#### Call Clearing System
```bash
curl -X POST https://payment-engine.local/api/v1/enhanced-downstream/clearing/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{
    "transaction_id": "TXN-123",
    "clearing_reference": "CLR-456",
    "amount": 1000
  }'
```

#### Get Resolved Configuration
```bash
curl -X GET "https://payment-engine.local/api/v1/enhanced-downstream/config/tenant-001/fraud//fraud?paymentType=SEPA" \
  -H "X-Tenant-ID: tenant-001"
```

#### Validate Tenant Access
```bash
curl -X GET "https://payment-engine.local/api/v1/enhanced-downstream/validate/tenant-001/fraud//fraud?paymentType=SEPA" \
  -H "X-Tenant-ID: tenant-001"
```

### Configuration Management API

#### Create Clearing System Configuration
```bash
curl -X POST https://payment-engine.local/api/v1/clearing-system-auth-configurations \
  -H "Content-Type: application/json" \
  -d '{
    "environment": "dev",
    "authMethod": "JWT",
    "jwtSecret": "dev-jwt-secret",
    "jwtIssuer": "payment-engine-dev",
    "jwtAudience": "payment-engine-api",
    "jwtExpirationSeconds": 3600,
    "includeClientHeaders": true,
    "clientId": "dev-client-id",
    "clientSecret": "dev-client-secret",
    "clientIdHeaderName": "X-Client-ID",
    "clientSecretHeaderName": "X-Client-Secret",
    "isActive": true,
    "description": "Development environment configuration"
  }'
```

#### Create Payment Type Configuration
```bash
curl -X POST https://payment-engine.local/api/v1/payment-type-auth-configurations \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "paymentType": "SEPA",
    "authMethod": "JWS",
    "jwsSecret": "sepa-jws-secret",
    "jwsAlgorithm": "HS256",
    "jwsIssuer": "payment-engine-sepa",
    "jwsAudience": "sepa-api",
    "jwsExpirationSeconds": 1800,
    "includeClientHeaders": true,
    "clientId": "sepa-client-id",
    "clientSecret": "sepa-client-secret",
    "clientIdHeaderName": "X-SEPA-Client-ID",
    "clientSecretHeaderName": "X-SEPA-Client-Secret",
    "clearingSystem": "SEPA_CLEARING",
    "currency": "EUR",
    "isHighValue": false,
    "isActive": true,
    "description": "SEPA payment type configuration for tenant-001"
  }'
```

#### Create Downstream Call Configuration
```bash
curl -X POST https://payment-engine.local/api/v1/downstream-call-auth-configurations \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "serviceType": "fraud",
    "endpoint": "/fraud",
    "paymentType": "SEPA",
    "authMethod": "API_KEY",
    "apiKey": "fraud-api-key-12345",
    "apiKeyHeaderName": "X-Fraud-API-Key",
    "includeClientHeaders": true,
    "clientId": "fraud-client-id",
    "clientSecret": "fraud-client-secret",
    "clientIdHeaderName": "X-Fraud-Client-ID",
    "clientSecretHeaderName": "X-Fraud-Client-Secret",
    "targetHost": "fraud.bank-nginx.example.com",
    "targetPort": 443,
    "targetProtocol": "HTTPS",
    "targetPath": "/fraud",
    "timeoutSeconds": 30,
    "retryAttempts": 3,
    "retryDelaySeconds": 5,
    "isActive": true,
    "description": "Fraud system configuration for tenant-001 SEPA payments"
  }'
```

---

## Frontend Integration

### React Component Usage

```tsx
import React from 'react';
import MultiLevelAuthConfigurationManager from './components/multiLevelAuth/MultiLevelAuthConfigurationManager';

const TenantConfigurationPage: React.FC = () => {
  const [tenantId, setTenantId] = useState('tenant-001');
  
  const handleConfigurationChange = () => {
    // Handle configuration changes
    console.log('Configuration changed');
  };

  return (
    <div>
      <h1>Multi-Level Authentication Configuration</h1>
      <MultiLevelAuthConfigurationManager
        tenantId={tenantId}
        onConfigurationChange={handleConfigurationChange}
      />
    </div>
  );
};
```

### TypeScript Types

```typescript
import {
  ClearingSystemAuthConfigurationRequest,
  PaymentTypeAuthConfigurationRequest,
  DownstreamCallAuthConfigurationRequest,
  ResolvedAuthConfiguration,
  AuthMethod,
  Environment,
  PaymentType,
  ServiceType
} from '../types/multiLevelAuth';

// Usage example
const clearingSystemConfig: ClearingSystemAuthConfigurationRequest = {
  environment: 'dev',
  authMethod: 'JWT',
  jwtSecret: 'dev-jwt-secret',
  jwtIssuer: 'payment-engine-dev',
  jwtAudience: 'payment-engine-api',
  jwtExpirationSeconds: 3600,
  includeClientHeaders: true,
  clientId: 'dev-client-id',
  clientSecret: 'dev-client-secret',
  clientIdHeaderName: 'X-Client-ID',
  clientSecretHeaderName: 'X-Client-Secret',
  isActive: true,
  description: 'Development environment configuration'
};
```

### API Service Usage

```typescript
import {
  clearingSystemAuthApi,
  paymentTypeAuthApi,
  downstreamCallAuthApi,
  enhancedDownstreamRoutingApi
} from '../services/multiLevelAuthApi';

// Create clearing system configuration
const createClearingSystemConfig = async () => {
  try {
    const response = await clearingSystemAuthApi.createConfiguration({
      environment: 'dev',
      authMethod: 'JWT',
      // ... other configuration
    });
    console.log('Configuration created:', response.data);
  } catch (error) {
    console.error('Error creating configuration:', error);
  }
};

// Call enhanced downstream service
const callFraudSystem = async () => {
  try {
    const response = await enhancedDownstreamRoutingApi.callFraudSystem(
      'tenant-001',
      'SEPA',
      {
        transaction_id: 'TXN-123',
        amount: 1000,
        fraud_check: true
      }
    );
    console.log('Fraud system response:', response.data);
  } catch (error) {
    console.error('Error calling fraud system:', error);
  }
};
```

---

## Testing

### Automated Testing

```bash
# Test multi-level authentication configuration system
./scripts/test-multi-level-auth-config.sh

# Test specific tenant
./scripts/test-multi-level-auth-config.sh --test-tenant tenant-001

# Test specific level
./scripts/test-multi-level-auth-config.sh --test-level clearing-system

# Test with custom URL
./scripts/test-multi-level-auth-config.sh --url https://payment-engine.local
```

### Manual Testing

```bash
# Test configuration hierarchy
curl -X GET "https://payment-engine.local/api/v1/enhanced-downstream/config/tenant-001/fraud//fraud?paymentType=SEPA" \
  -H "X-Tenant-ID: tenant-001"

# Test enhanced downstream call
curl -X POST https://payment-engine.local/api/v1/enhanced-downstream/fraud/tenant-001 \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-001" \
  -d '{"transaction_id": "test-123", "fraud_check": true}'

# Test configuration validation
curl -X GET "https://payment-engine.local/api/v1/enhanced-downstream/validate/tenant-001/fraud//fraud?paymentType=SEPA" \
  -H "X-Tenant-ID: tenant-001"
```

### Test Scenarios

1. **Configuration Hierarchy Testing**
   - Test configuration resolution at each level
   - Verify precedence rules
   - Test fallback behavior

2. **Authentication Method Testing**
   - Test JWT authentication
   - Test JWS authentication
   - Test OAuth2 authentication
   - Test API Key authentication
   - Test Basic Authentication

3. **Client Header Testing**
   - Test client header injection
   - Test header name customization
   - Test header value configuration

4. **Tenant Isolation Testing**
   - Verify tenant isolation
   - Test cross-tenant access prevention
   - Test tenant-specific configurations

5. **Payment Type Testing**
   - Test payment type specific configurations
   - Test currency-specific settings
   - Test high-value payment handling

---

## Best Practices

### 1. Configuration Management

- **Use Environment-Specific Configurations**: Create separate configurations for dev, staging, and prod environments
- **Implement Configuration Validation**: Validate configurations before activation
- **Use Descriptive Names**: Use clear, descriptive names for configurations
- **Document Configuration Purpose**: Always include descriptions explaining the purpose of each configuration

### 2. Security Best Practices

- **Secure Secret Storage**: Store secrets securely and never log them
- **Use Strong Secrets**: Use cryptographically strong secrets for JWT/JWS
- **Implement Secret Rotation**: Regularly rotate secrets and API keys
- **Use HTTPS**: Always use HTTPS for external communications
- **Validate Inputs**: Validate all configuration inputs

### 3. Performance Optimization

- **Cache Resolved Configurations**: Cache resolved configurations to avoid repeated database queries
- **Use Connection Pooling**: Configure appropriate connection pools for external services
- **Implement Timeouts**: Set appropriate timeouts for external calls
- **Use Circuit Breakers**: Implement circuit breakers for resilience

### 4. Monitoring and Observability

- **Log Configuration Changes**: Log all configuration changes for audit purposes
- **Monitor Authentication Failures**: Monitor and alert on authentication failures
- **Track Configuration Usage**: Track which configurations are being used
- **Implement Health Checks**: Implement health checks for configuration services

### 5. Error Handling

- **Graceful Degradation**: Implement graceful degradation when configurations are missing
- **Clear Error Messages**: Provide clear error messages for configuration issues
- **Fallback Configurations**: Always have fallback configurations available
- **Retry Logic**: Implement appropriate retry logic for failed calls

### 6. Testing Best Practices

- **Test All Configuration Levels**: Test configurations at all levels
- **Test Configuration Hierarchy**: Verify configuration precedence works correctly
- **Test Authentication Methods**: Test all supported authentication methods
- **Test Error Scenarios**: Test error scenarios and edge cases
- **Automate Testing**: Automate configuration testing as much as possible

---

## Conclusion

The multi-level authentication configuration system provides:

✅ **Granular Configuration**: Configuration at clearing system, tenant, payment type, and downstream call levels
✅ **Flexible Authentication**: Support for JWT, JWS, OAuth2, API Key, and Basic Authentication
✅ **Configuration Hierarchy**: Clear precedence rules with automatic resolution
✅ **Client Header Management**: Configurable client ID and secret headers
✅ **Tenant Isolation**: Complete isolation between tenants
✅ **Payment Type Support**: Different configurations for different payment types
✅ **External Service Integration**: Seamless integration with external services
✅ **Comprehensive Testing**: Full test suite for all configuration levels
✅ **Frontend Integration**: Complete React frontend for configuration management

This system eliminates the need for hardcoded authentication configurations and provides a flexible, scalable solution for managing authentication across multiple levels and contexts.