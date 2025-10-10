# Complete Payment Engine API Documentation

## Overview

The Payment Engine is a **multi-tenant, highly configurable** banking platform that provides comprehensive REST APIs for:
- **Multi-tenant operations** with complete data isolation
- **Runtime configuration management** without restarts
- **ISO 20022 message processing** (pain, pacs, camt message types)
- **Feature flag management** with gradual rollouts
- **Dynamic payment type onboarding** via APIs
- **Real-time monitoring** and alerting per tenant

## Multi-Tenancy Support

**All APIs support multi-tenancy** through the `X-Tenant-ID` header:

```bash
curl -X GET /api/v1/transactions \
  -H "Authorization: Bearer jwt-token" \
  -H "X-Tenant-ID: bank-abc"
```

### Tenant Context Sources (Priority Order)
1. **HTTP Header**: `X-Tenant-ID: bank-abc`
2. **Query Parameter**: `?tenantId=bank-abc`
3. **Path Parameter**: `/api/v1/tenants/bank-abc/...`
4. **JWT Token Claims**: `tenantId` claim in JWT
5. **Default**: `default` tenant if none specified

## Base URLs

- **Production**: `https://api.payment-engine.com`
- **Staging**: `https://staging-api.payment-engine.com`
- **Development**: `http://localhost:8080`

## Authentication

OAuth 2.0 with JWT tokens. All requests require `Authorization: Bearer <token>` header.

### Authentication Endpoints

#### Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user@bank.com",
  "password": "password123"
}

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600,
  "user": {
    "id": "user-123",
    "username": "user@bank.com",
    "tenantId": "bank-abc",
    "permissions": ["transaction:read", "transaction:create", "tenant:config:read"]
  }
}
```

---

## 🏢 **TENANT MANAGEMENT APIs**

### Create Tenant
```bash
POST /api/v1/config/tenants
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "tenantId": "regional-bank",
  "tenantName": "Regional Bank Corp",
  "tenantType": "BANK",
  "subscriptionTier": "PREMIUM",
  "configuration": {
    "features": {
      "iso20022": true,
      "bulkProcessing": true,
      "advancedMonitoring": true
    },
    "limits": {
      "transactionsPerDay": 500000,
      "apiCallsPerHour": 25000
    }
  }
}

# Response
{
  "tenantId": "regional-bank",
  "status": "CREATED",
  "message": "Tenant created successfully"
}
```

### Get Tenant Information
```bash
GET /api/v1/config/tenants/{tenantId}
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "tenantId": "regional-bank",
  "tenantName": "Regional Bank Corp",
  "tenantType": "BANK",
  "status": "ACTIVE",
  "subscriptionTier": "PREMIUM",
  "configuration": {...},
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### Get All Tenant Configuration
```bash
GET /api/v1/config/tenants/{tenantId}/config
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "payment.default_currency": "USD",
  "payment.max_daily_limit": "1000000.00",
  "security.jwt_expiry_minutes": "60",
  "iso20022.validation_strict": "true",
  "kafka.batch_size": "16384"
}
```

---

## ⚙️ **CONFIGURATION MANAGEMENT APIs**

### Set Configuration Value
```bash
POST /api/v1/config/tenants/{tenantId}/config
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "configKey": "payment.max_daily_limit",
  "configValue": "2000000.00",
  "environment": "production"
}

# Response
{
  "message": "Configuration updated successfully",
  "tenantId": "regional-bank",
  "configKey": "payment.max_daily_limit",
  "environment": "production"
}
```

### Get Configuration Value
```bash
GET /api/v1/config/tenants/{tenantId}/config/{configKey}
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "tenantId": "regional-bank",
  "configKey": "payment.max_daily_limit",
  "configValue": "2000000.00"
}
```

---

## 💳 **DYNAMIC PAYMENT TYPE MANAGEMENT**

### Add Payment Type at Runtime
```bash
POST /api/v1/config/tenants/{tenantId}/payment-types
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "code": "CRYPTO_TRANSFER",
  "name": "Cryptocurrency Transfer",
  "description": "Blockchain-based cryptocurrency transfers",
  "isSynchronous": true,
  "maxAmount": 1000000.00,
  "minAmount": 0.01,
  "processingFee": 5.00,
  "configuration": {
    "blockchainNetwork": "ethereum",
    "confirmationsRequired": 6,
    "gasLimit": 21000,
    "supportedTokens": ["ETH", "USDC", "USDT"]
  }
}

# Response
{
  "message": "Payment type added successfully",
  "tenantId": "regional-bank",
  "paymentTypeCode": "CRYPTO_TRANSFER"
}
```

### Update Payment Type Configuration
```bash
PUT /api/v1/config/tenants/{tenantId}/payment-types/{paymentTypeCode}
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "isActive": true,
  "maxAmount": 2000000.00,
  "processingFee": 3.50,
  "configuration": {
    "blockchainNetwork": "ethereum",
    "gasLimit": 25000,
    "supportedTokens": ["ETH", "USDC", "USDT", "DAI"]
  }
}

# Response
{
  "message": "Payment type updated successfully",
  "tenantId": "regional-bank",
  "paymentTypeCode": "CRYPTO_TRANSFER"
}
```

### Get Payment Types (Tenant-Aware)
```bash
GET /api/v1/payment-types
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
[
  {
    "id": "1",
    "code": "RTP",
    "name": "Real-Time Payment",
    "tenantId": "regional-bank",
    "isActive": true,
    "maxAmount": 100000.00,
    "configuration": {...}
  },
  {
    "id": "15",
    "code": "CRYPTO_TRANSFER", 
    "name": "Cryptocurrency Transfer",
    "tenantId": "regional-bank",
    "isActive": true,
    "maxAmount": 2000000.00,
    "configuration": {...}
  }
]
```

---

## 🚩 **FEATURE FLAG MANAGEMENT**

### Check Feature Flag
```bash
GET /api/v1/config/tenants/{tenantId}/features/{featureName}
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "tenantId": "regional-bank",
  "featureName": "advanced-fraud-detection",
  "enabled": true,
  "rolloutPercentage": 75
}
```

### Set Feature Flag
```bash
POST /api/v1/config/tenants/{tenantId}/features/{featureName}
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "enabled": true,
  "config": {
    "rolloutPercentage": 50,
    "mlModel": "fraud-detector-v2.1",
    "confidenceThreshold": 0.85,
    "targetGroups": ["premium-customers"]
  }
}

# Response
{
  "message": "Feature flag updated successfully",
  "tenantId": "regional-bank",
  "featureName": "advanced-fraud-detection",
  "enabled": "true"
}
```

---

## 🔄 **RATE LIMITING CONFIGURATION**

### Get Rate Limit Configuration
```bash
GET /api/v1/config/tenants/{tenantId}/rate-limits?endpoint=/api/v1/transactions
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "tenantId": "regional-bank",
  "endpoint": "/api/v1/transactions",
  "rateLimitConfig": {
    "rateLimitPerMinute": 5000,
    "burstCapacity": 7500,
    "windowSizeSeconds": 60
  }
}
```

### Update Rate Limit Configuration
```bash
PUT /api/v1/config/tenants/{tenantId}/rate-limits?endpoint=/api/v1/transactions
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "rateLimitPerMinute": 10000,
  "burstCapacity": 15000,
  "windowSizeSeconds": 60
}

# Response
{
  "message": "Rate limit configuration updated successfully",
  "tenantId": "regional-bank",
  "endpoint": "/api/v1/transactions"
}
```

---

## 📨 **KAFKA TOPIC MANAGEMENT**

### Add Kafka Topic Dynamically
```bash
POST /api/v1/config/tenants/{tenantId}/kafka-topics
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "topicName": "regional-bank.custom.events",
  "partitions": 6,
  "replicationFactor": 3,
  "configuration": {
    "retention.ms": "604800000",
    "cleanup.policy": "delete",
    "compression.type": "snappy"
  }
}

# Response
{
  "message": "Kafka topic added successfully",
  "tenantId": "regional-bank",
  "topicName": "regional-bank.custom.events"
}
```

### Update Kafka Topic Configuration
```bash
PUT /api/v1/config/tenants/{tenantId}/kafka-topics/{topicName}
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "retention.ms": "1209600000",
  "compression.type": "lz4"
}

# Response
{
  "message": "Kafka topic configuration updated successfully",
  "tenantId": "regional-bank",
  "topicName": "regional-bank.custom.events"
}
```

---

## 🌐 **API ENDPOINT MANAGEMENT**

### Add API Endpoint Dynamically
```bash
POST /api/v1/config/tenants/{tenantId}/api-endpoints
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "endpointPath": "/api/v1/custom/crypto-wallet",
  "httpMethod": "POST",
  "serviceName": "core-banking",
  "configuration": {
    "rateLimitPerMinute": 100,
    "requiresAuth": true,
    "enableLogging": true,
    "circuitBreakerEnabled": true
  }
}

# Response
{
  "message": "API endpoint added successfully",
  "tenantId": "regional-bank",
  "endpointPath": "/api/v1/custom/crypto-wallet",
  "httpMethod": "POST"
}
```

---

## 💰 **TRANSACTION APIs (Multi-Tenant)**

### Create Transaction
```bash
POST /api/v1/transactions
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "fromAccountId": "ACC001001",
  "toAccountId": "ACC002002", 
  "amount": 1500.00,
  "currency": "USD",
  "paymentType": "RTP",
  "description": "Invoice payment",
  "metadata": {
    "invoiceId": "INV-2024-001",
    "departmentId": "DEPT-001"
  }
}

# Response  
{
  "transactionId": "TXN-20240115-001",
  "status": "SUCCESS",
  "tenantId": "regional-bank",
  "amount": 1500.00,
  "processingTime": "2.3s",
  "fees": 2.50,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Get Transactions (Tenant-Filtered)
```bash
GET /api/v1/transactions?status=SUCCESS&limit=50
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response - Only returns transactions for the specified tenant
{
  "transactions": [
    {
      "transactionId": "TXN-20240115-001",
      "tenantId": "regional-bank",
      "fromAccountId": "ACC001001",
      "toAccountId": "ACC002002",
      "amount": 1500.00,
      "status": "SUCCESS",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "total": 1,
    "page": 1,
    "limit": 50
  }
}
```

---

## 💳 **ACCOUNT APIs (Multi-Tenant)**

### Get Accounts (Tenant-Filtered)
```bash
GET /api/v1/accounts
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response - Only returns accounts for the specified tenant
{
  "accounts": [
    {
      "accountId": "ACC001001",
      "tenantId": "regional-bank",
      "customerId": "CUST001",
      "accountNumber": "1234567890",
      "balance": 50000.00,
      "currency": "USD",
      "status": "ACTIVE",
      "iban": "US33XXXX1234567890",
      "bic": "REGBANKUS33"
    }
  ]
}
```

---

## 📋 **ISO 20022 MESSAGE APIs**

### Process pain.001 (Payment Initiation) - **Enhanced with Configurable Response Modes**

The pain.001 endpoint now supports **three configurable response modes**:
- **SYNCHRONOUS**: Immediate pain.002 response in API call
- **ASYNCHRONOUS**: Callback-based response delivery
- **🆕 KAFKA_TOPIC**: pain.002 published to payment-type-specific Kafka topics

```bash
POST /api/v1/iso20022/pain001
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "CstmrCdtTrfInitn": {
    "GrpHdr": {
      "MsgId": "MSG-20240115-001",
      "CreDtTm": "2024-01-15T10:30:00.000Z",
      "NbOfTxs": "1",
      "CtrlSum": "1000.00",
      "InitgPty": {"Nm": "Regional Bank Corp"}
    },
    "PmtInf": {
      "PmtInfId": "PMT-20240115-001",
      "PmtMtd": "TRF",
      "Dbtr": {"Nm": "John Doe"},
      "DbtrAcct": {
        "Id": {"IBAN": "US33REGB1234567890"},
        "Ccy": "USD"
      },
      "CdtTrfTxInf": {
        "PmtId": {"EndToEndId": "E2E-20240115-001"},
        "Amt": {"InstdAmt": {"Ccy": "USD", "Value": "1000.00"}},
        "Cdtr": {"Nm": "Jane Smith"},
        "CdtrAcct": {"Id": {"IBAN": "US33OTHR9876543210"}}
      }
    }
  }
}

# Response (varies based on payment type configuration):

# SYNCHRONOUS Mode Response:
{
  "responseMode": "SYNCHRONOUS",
  "pain002Message": {
    "GrpHdr": {"MsgId": "PAIN002-1705312200123", "CreDtTm": "2024-01-15T10:30:00.123Z"},
    "OrgnlGrpInfAndSts": {"OrgnlMsgId": "MSG-20240115-001", "GrpSts": "ACCP"},
    "TxInfAndSts": [{"OrgnlEndToEndId": "E2E-20240115-001", "TxSts": "ACCP"}]
  },
  "transactionId": "TXN-20240115-002",
  "status": "SUCCESS",
  "immediate": true
}

# KAFKA_TOPIC Mode Response:
{
  "responseMode": "KAFKA_TOPIC",
  "transactionId": "TXN-20240115-002",
  "status": "ACCEPTED_FOR_PROCESSING",
  "message": "Payment accepted. Status response will be published to Kafka topic.",
  "kafkaTopicName": "payment-engine.regional-bank.responses.ach_credit.pain002",
  "originalMessageId": "MSG-20240115-001",
  "responseMessageId": "PAIN002-1705312200123",
  "kafkaPublished": true
}
```

### Process camt.055 (Payment Cancellation Request)
```bash
POST /api/v1/iso20022/camt055
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "CstmrPmtCxlReq": {
    "GrpHdr": {
      "MsgId": "CXLREQ-20240115-001",
      "CreDtTm": "2024-01-15T11:00:00.000Z",
      "InitgPty": {"Nm": "Regional Bank Corp"}
    },
    "Undrlyg": {
      "TxInf": {
        "CxlId": "CXL-20240115-001",
        "OrgnlEndToEndId": "E2E-20240115-001",
        "CxlRsnInf": {
          "Rsn": {"Cd": "DUPL"},
          "AddtlInf": "Duplicate payment detected"
        }
      }
    }
  }
}

# Response
{
  "messageId": "CXLREQ-20240115-001",
  "status": "ACCEPTED",
  "tenantId": "regional-bank",
  "originalTransactionId": "TXN-20240115-002",
  "cancellationId": "CXL-20240115-001"
}
```

### Bulk ISO 20022 Processing
```bash
POST /api/v1/iso20022/bulk
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "messages": [
    {
      "messageType": "pain.001.001.03",
      "message": {...}
    },
    {
      "messageType": "pain.007.001.03", 
      "message": {...}
    }
  ]
}

# Response
{
  "batchId": "BATCH-20240115-001",
  "tenantId": "regional-bank",
  "totalMessages": 2,
  "processedMessages": 2,
  "failedMessages": 0,
  "results": [
    {
      "messageId": "MSG-20240115-001",
      "status": "ACCEPTED",
      "transactionId": "TXN-20240115-003"
    },
    {
      "messageId": "MSG-20240115-002", 
      "status": "ACCEPTED",
      "transactionId": "TXN-20240115-004"
    }
  ]
}
```

---

## ✅ **VALIDATION AND TESTING**

### Validate Configuration
```bash
POST /api/v1/config/tenants/{tenantId}/config/validate
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "configurations": {
    "payment.max_daily_limit": "5000000.00",
    "security.jwt_expiry_minutes": "120"
  }
}

# Response
{
  "valid": true,
  "tenantId": "regional-bank",
  "warnings": [],
  "errors": []
}
```

### Configuration Change History
```bash
GET /api/v1/config/tenants/{tenantId}/config/history?page=0&size=20
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "tenantId": "regional-bank",
  "changes": [
    {
      "configType": "PAYMENT_TYPE",
      "configKey": "CRYPTO_TRANSFER",
      "oldValue": null,
      "newValue": {...},
      "changedBy": "admin@regionalbank.com",
      "changeSource": "API",
      "timestamp": "2024-01-15T10:30:00Z"
    }
  ],
  "totalChanges": 1,
  "page": 0,
  "size": 20
}
```

---

## 🔍 **HEALTH AND MONITORING**

### Configuration Service Health
```bash
GET /api/v1/config/health
Authorization: Bearer <token>

# Response
{
  "status": "UP",
  "service": "configuration-service", 
  "features": "multi-tenancy, dynamic-config, feature-flags",
  "tenants": {
    "total": 15,
    "active": 12,
    "inactive": 3
  }
}
```

### Tenant-Specific Metrics
```bash
GET /api/v1/metrics/tenant/{tenantId}
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}

# Response
{
  "tenantId": "regional-bank",
  "metrics": {
    "transactionsPerSecond": 45.2,
    "errorRate": 0.02,
    "resourceUsage": {
      "transactionsPerDay": 125000,
      "transactionsDailyLimit": 500000,
      "usagePercentage": 25.0
    },
    "featureFlags": {
      "advanced-fraud-detection": {"enabled": true, "rollout": 75},
      "bulk-processing": {"enabled": true, "rollout": 100}
    }
  }
}
```

---

## 🔐 **SECURITY CONSIDERATIONS**

### Required Permissions

| Endpoint Pattern | Required Permission |
|-----------------|-------------------|
| `GET /api/v1/config/tenants/{id}` | `tenant:read` |
| `POST /api/v1/config/tenants` | `tenant:create` |
| `POST /api/v1/config/tenants/{id}/config` | `tenant:config:update` |
| `GET /api/v1/config/tenants/{id}/config/*` | `tenant:config:read` |
| `POST /api/v1/config/tenants/{id}/payment-types` | `payment-type:create` |
| `PUT /api/v1/config/tenants/{id}/payment-types/*` | `payment-type:update` |
| `POST /api/v1/config/tenants/{id}/features/*` | `feature:update` |
| `PUT /api/v1/config/tenants/{id}/rate-limits` | `rate-limit:update` |
| `POST /api/v1/config/tenants/{id}/kafka-topics` | `kafka:create` |

### Tenant Access Control
- Users can only access their own tenant's data
- Admin users can access multiple tenants
- Tenant ID is validated against JWT claims
- Row-level security enforces database isolation

---

## 📊 **RATE LIMITS**

### Default Rate Limits (Per Tenant)

| Endpoint Pattern | Rate Limit | Burst Capacity |
|-----------------|------------|----------------|
| `/api/v1/transactions` | 100/min | 150 |
| `/api/v1/iso20022/pain001` | 100/min | 150 |
| `/api/v1/iso20022/camt055` | 50/min | 75 |
| `/api/v1/config/**` | 200/min | 300 |
| `/api/v1/accounts/**` | 1000/min | 1500 |

Rate limits can be customized per tenant using the Rate Limiting Configuration APIs.

---

## 🚀 **API VERSIONING**

All APIs are versioned using URL path versioning:
- **Current Version**: `v1` 
- **Format**: `/api/v1/endpoint`
- **Backward Compatibility**: Maintained for at least 2 versions
- **Deprecation**: 6-month notice for breaking changes

---

## 📝 **RESPONSE FORMATS**

### Success Response Format
```json
{
  "status": "success",
  "data": {...},
  "metadata": {
    "tenantId": "regional-bank",
    "requestId": "req_1705312200_abc123",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### Error Response Format
```json
{
  "status": "error",
  "error": {
    "code": "TENANT_ACCESS_DENIED",
    "message": "Access denied for tenant: regional-bank",
    "details": "User does not have permission to access this tenant"
  },
  "metadata": {
    "tenantId": "regional-bank",
    "requestId": "req_1705312200_abc123", 
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

---

## 🔄 **WEBHOOK SUPPORT**

The Payment Engine supports tenant-specific webhooks for real-time notifications:

### Configure Webhook
```bash
POST /api/v1/config/tenants/{tenantId}/webhooks
Authorization: Bearer <token>
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "endpointName": "transaction-notifications",
  "webhookUrl": "https://regionalbank.com/webhooks/payments",
  "eventTypes": ["transaction.completed", "transaction.failed", "payment.cancelled"],
  "secretKey": "webhook-secret-key",
  "retryConfig": {
    "maxAttempts": 3,
    "backoffSeconds": [1, 2, 4]
  }
}
```

---

This documentation reflects the **complete implemented system** with multi-tenancy, runtime configurability, and comprehensive API coverage. All endpoints have been implemented and tested in the codebase.