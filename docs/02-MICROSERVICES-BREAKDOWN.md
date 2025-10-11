# Microservices Detailed Breakdown

## Purpose
This document provides detailed specifications for each microservice, including responsibilities, APIs, dependencies, and database schemas. Each service is designed to be built independently by AI agents.

---

## Service Matrix Overview

| # | Service Name | Lines of Code | AI Agent Time | Database | Dependencies |
|---|--------------|---------------|---------------|----------|--------------|
| 1 | Payment Initiation | 400 | 3h | PostgreSQL | Validation Service |
| 2 | Validation Service | 450 | 3h | PostgreSQL + Redis | Account Service, Fraud API |
| 3 | Account Service | 350 | 2h | PostgreSQL | - |
| 4 | Routing Service | 300 | 2h | Redis | - |
| 5 | Transaction Processing | 500 | 4h | PostgreSQL | Settlement Service |
| 6 | Clearing Adapter (SAMOS) | 400 | 3h | PostgreSQL | - |
| 7 | Clearing Adapter (Bankserv) | 400 | 3h | PostgreSQL | - |
| 8 | Clearing Adapter (RTC) | 400 | 3h | PostgreSQL | - |
| 9 | Settlement Service | 450 | 3h | PostgreSQL | - |
| 10 | Reconciliation Service | 400 | 3h | PostgreSQL | - |
| 11 | Notification Service | 250 | 2h | PostgreSQL | - |
| 12 | Reporting Service | 350 | 3h | PostgreSQL + Synapse | - |
| 13 | Saga Orchestrator | 500 | 4h | PostgreSQL | All Core Services |
| 14 | API Gateway | 300 | 2h | Redis | - |
| 15 | IAM Service | 400 | 3h | PostgreSQL + Azure AD | - |
| 16 | Audit Service | 300 | 2h | CosmosDB | - |

---

## 0. Tenant Management Service (NEW)

### Responsibilities
- Manage tenant lifecycle (onboarding, activation, suspension, deactivation)
- Maintain tenant hierarchy (Tenant → Business Unit → Customer)
- Store tenant-specific configurations (limits, fraud rules, clearing credentials)
- Provide tenant context lookup for all services
- Track tenant usage metrics and enforce quotas
- Manage tenant users and API keys
- Audit all tenant-related operations

### Technology Stack
- **Language**: Java 17, Spring Boot 3.x
- **Database**: PostgreSQL 15 (Row-Level Security enabled)
- **Cache**: Redis (tenant configs, lookups)
- **Authentication**: Azure AD B2C (multi-tenant setup)
- **API**: REST (synchronous), Internal gRPC for fast lookups

### API Endpoints

```java
// Tenant Management (Platform Admin only)
POST   /api/v1/platform/tenants                  // Onboard new tenant
GET    /api/v1/platform/tenants                  // List all tenants
GET    /api/v1/platform/tenants/{tenantId}       // Get tenant details
PUT    /api/v1/platform/tenants/{tenantId}       // Update tenant
DELETE /api/v1/platform/tenants/{tenantId}       // Deactivate tenant

// Business Unit Management (Tenant Admin)
POST   /api/v1/tenant/business-units             // Create business unit
GET    /api/v1/tenant/business-units             // List business units
GET    /api/v1/tenant/business-units/{buId}      // Get BU details
PUT    /api/v1/tenant/business-units/{buId}      // Update BU

// Tenant Configuration (Tenant Admin)
GET    /api/v1/tenant/config                     // Get all configs
GET    /api/v1/tenant/config/{key}               // Get specific config
PUT    /api/v1/tenant/config/{key}               // Update config
DELETE /api/v1/tenant/config/{key}               // Delete config

// Tenant Lookup (Internal - used by all services)
GET    /api/internal/v1/tenant/lookup/{tenantId}     // Get tenant info
GET    /api/internal/v1/tenant/validate/{tenantId}   // Validate tenant
GET    /api/internal/v1/tenant/config/{tenantId}/{key} // Get config

// Tenant Metrics (Platform Monitoring)
GET    /api/v1/platform/metrics/tenants          // All tenant metrics
GET    /api/v1/platform/metrics/tenants/{tenantId} // Single tenant metrics
```

### Database Tables
- `tenants`: Top-level tenant records
- `business_units`: Divisions within tenants  
- `tenant_configs`: Tenant-specific configurations
- `tenant_users`: Admin users per tenant
- `tenant_api_keys`: API keys for programmatic access
- `tenant_metrics`: Daily usage metrics per tenant
- `tenant_audit_log`: Audit trail for all tenant operations

### Events Published
- `TenantCreatedEvent`
- `TenantActivatedEvent`
- `TenantSuspendedEvent`
- `TenantConfigChangedEvent`
- `BusinessUnitCreatedEvent`

### Events Subscribed
- `PaymentCompletedEvent` → Update tenant metrics
- `PaymentFailedEvent` → Track failures per tenant

### Key Implementation

```java
@RestController
@RequestMapping("/api/v1/platform/tenants")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class TenantManagementController {
    
    @Autowired
    private TenantOnboardingService onboardingService;
    
    /**
     * Onboard new tenant
     */
    @PostMapping
    public ResponseEntity<TenantResponse> onboardTenant(
        @RequestBody @Valid TenantOnboardingRequest request
    ) {
        TenantResponse response = onboardingService.onboardTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

@Service
public class TenantOnboardingService {
    
    @Transactional
    public TenantResponse onboardTenant(TenantOnboardingRequest request) {
        // 1. Create tenant record
        Tenant tenant = createTenant(request);
        
        // 2. Create business units
        List<BusinessUnit> businessUnits = createBusinessUnits(tenant, request);
        
        // 3. Provision resources (Kafka topics, Redis namespace)
        provisionResources(tenant);
        
        // 4. Set default configurations
        setDefaultConfigs(tenant, request);
        
        // 5. Create admin user
        createAdminUser(tenant, request);
        
        // 6. Activate tenant
        activateTenant(tenant);
        
        // 7. Publish TenantCreatedEvent
        publishTenantCreatedEvent(tenant);
        
        return TenantResponse.from(tenant, businessUnits);
    }
}

// Tenant Context Filter (applied to all services)
@Component
@Order(1)
public class TenantContextFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Extract tenant ID from header
            String tenantId = request.getHeader("X-Tenant-ID");
            
            if (tenantId == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "Missing X-Tenant-ID header");
                return;
            }
            
            // Validate tenant
            Tenant tenant = validateTenant(tenantId);
            
            // Set ThreadLocal context
            TenantContext.setTenantId(tenantId);
            TenantContext.setTenantName(tenant.getTenantName());
            
            // Continue
            filterChain.doFilter(request, response);
            
        } finally {
            TenantContext.clear();
        }
    }
}
```

### For Complete Details
See **[12-TENANT-MANAGEMENT.md](12-TENANT-MANAGEMENT.md)** for:
- Complete database schema
- Tenant hierarchy design
- Row-Level Security implementation
- Tenant context propagation
- Configuration management
- Onboarding process
- Testing strategy

---

## 1. Payment Initiation Service

### Responsibilities
- Accept payment requests from frontend/API channels
- Generate unique payment IDs
- Perform basic validation (field presence, format)
- Publish `PaymentInitiatedEvent` to event bus
- Store initial payment request

### API Endpoints

```yaml
POST /api/v1/payments
  Description: Initiate a new payment
  Request Body:
    {
      "idempotencyKey": "uuid",
      "sourceAccount": "1234567890",
      "destinationAccount": "0987654321",
      "amount": 1000.00,
      "currency": "ZAR",
      "reference": "Payment for Invoice #123",
      "paymentType": "EFT" | "RTC" | "RTGS",
      "debitOrderDetails": { ... } // optional
    }
  Response: 201 Created
    {
      "paymentId": "PAY-2025-XXXXXX",
      "status": "INITIATED",
      "timestamp": "2025-10-11T10:30:00Z"
    }

GET /api/v1/payments/{paymentId}
  Description: Get payment status
  Response: 200 OK
    {
      "paymentId": "PAY-2025-XXXXXX",
      "status": "PROCESSING",
      "amount": 1000.00,
      "currency": "ZAR",
      "createdAt": "2025-10-11T10:30:00Z",
      "updatedAt": "2025-10-11T10:30:05Z"
    }

GET /api/v1/payments
  Description: List payments (with pagination)
  Query Params: page, size, status, fromDate, toDate
  Response: 200 OK
    {
      "content": [...],
      "totalElements": 1000,
      "totalPages": 10,
      "number": 0
    }
```

### Events Published
```json
{
  "eventType": "PaymentInitiatedEvent",
  "eventId": "evt-uuid",
  "paymentId": "PAY-2025-XXXXXX",
  "timestamp": "2025-10-11T10:30:00Z",
  "payload": {
    "sourceAccount": "1234567890",
    "destinationAccount": "0987654321",
    "amount": 1000.00,
    "currency": "ZAR",
    "paymentType": "EFT"
  }
}
```

### Database Schema
```sql
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference VARCHAR(200),
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    INDEX idx_source_account (source_account),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Web (REST)
- Spring Data JPA
- PostgreSQL
- Azure Service Bus SDK

### AI Agent Instructions
1. Create Spring Boot project with dependencies
2. Implement PaymentInitiationController
3. Implement PaymentService (business logic)
4. Implement PaymentRepository
5. Implement EventPublisher for Azure Service Bus
6. Add validation annotations
7. Write unit tests (80% coverage)
8. Generate OpenAPI spec
9. Create Dockerfile
10. Document in README.md

---

## 2. Validation Service

### Responsibilities
- Validate business rules and compliance
- **Check customer transaction limits (per payment type, daily, monthly)**
- **Track and update used limits**
- **Enforce limit controls to prevent overspending**
- **Call external fraud scoring API for real-time risk assessment**
- **Evaluate fraud score and apply risk-based decisions**
- Check account status and KYC compliance
- Check FICA compliance
- Publish `PaymentValidatedEvent` or `ValidationFailedEvent`

### External Integration: Fraud Scoring API

**Provider**: Third-party fraud detection service (e.g., Simility, Feedzai, SAS Fraud Management)  
**Integration Type**: Synchronous REST API  
**Authentication**: API Key or OAuth 2.0  
**Timeout**: 5 seconds  
**Retry**: 3 attempts with exponential backoff  
**Circuit Breaker**: Enabled (fallback to rule-based detection)

### API Endpoints

```yaml
POST /api/v1/validate/payment
  Description: Comprehensive payment validation including limit checks
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "customerId": "CUST-123456",
      "sourceAccount": "1234567890",
      "destinationAccount": "0987654321",
      "amount": 1000.00,
      "currency": "ZAR",
      "paymentType": "EFT"
    }
  Response: 200 OK (Valid)
    {
      "valid": true,
      "validationErrors": [],
      "fraudScore": 0.15,
      "riskLevel": "LOW",
      "limitCheck": {
        "dailyLimitAvailable": 49000.00,
        "monthlyLimitAvailable": 199000.00,
        "paymentTypeLimitAvailable": 24000.00,
        "sufficientLimit": true
      }
    }
  Response: 400 Bad Request (Limit Exceeded)
    {
      "valid": false,
      "validationErrors": [
        {
          "code": "DAILY_LIMIT_EXCEEDED",
          "message": "Daily limit exceeded. Available: R5000, Requested: R10000",
          "field": "amount"
        }
      ],
      "fraudScore": 0.15,
      "riskLevel": "LOW",
      "limitCheck": {
        "dailyLimitAvailable": 5000.00,
        "monthlyLimitAvailable": 50000.00,
        "paymentTypeLimitAvailable": 5000.00,
        "sufficientLimit": false
      }
    }

GET /api/v1/limits/customer/{customerId}
  Description: Get customer's limit configuration and usage
  Response: 200 OK
    {
      "customerId": "CUST-123456",
      "customerProfile": "INDIVIDUAL_PREMIUM",
      "limits": {
        "daily": {
          "limit": 100000.00,
          "used": 45000.00,
          "available": 55000.00,
          "resetsAt": "2025-10-12T00:00:00Z"
        },
        "monthly": {
          "limit": 500000.00,
          "used": 180000.00,
          "available": 320000.00,
          "resetsAt": "2025-11-01T00:00:00Z"
        },
        "perTransaction": {
          "limit": 50000.00
        },
        "byPaymentType": [
          {
            "paymentType": "EFT",
            "dailyLimit": 50000.00,
            "dailyUsed": 15000.00,
            "dailyAvailable": 35000.00
          },
          {
            "paymentType": "RTC",
            "dailyLimit": 100000.00,
            "dailyUsed": 30000.00,
            "dailyAvailable": 70000.00
          }
        ]
      },
      "lastUpdated": "2025-10-11T10:30:00Z"
    }

POST /api/v1/limits/customer/{customerId}/check
  Description: Check if customer has sufficient limit for a payment
  Request Body:
    {
      "amount": 10000.00,
      "paymentType": "EFT"
    }
  Response: 200 OK
    {
      "sufficient": true,
      "dailyLimitCheck": {
        "limit": 100000.00,
        "used": 45000.00,
        "available": 55000.00,
        "afterTransaction": 45000.00,
        "withinLimit": true
      },
      "monthlyLimitCheck": {
        "limit": 500000.00,
        "used": 180000.00,
        "available": 320000.00,
        "afterTransaction": 310000.00,
        "withinLimit": true
      },
      "paymentTypeLimitCheck": {
        "paymentType": "EFT",
        "limit": 50000.00,
        "used": 15000.00,
        "available": 35000.00,
        "afterTransaction": 25000.00,
        "withinLimit": true
      }
    }

POST /api/v1/limits/customer/{customerId}/reserve
  Description: Reserve limit for a payment (before execution)
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "amount": 10000.00,
      "paymentType": "EFT"
    }
  Response: 201 Created
    {
      "reservationId": "RES-XXXXX",
      "status": "RESERVED",
      "expiresAt": "2025-10-11T11:00:00Z"
    }

POST /api/v1/limits/customer/{customerId}/consume
  Description: Consume (use) limit after successful payment
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "reservationId": "RES-XXXXX",
      "amount": 10000.00,
      "paymentType": "EFT"
    }
  Response: 200 OK
    {
      "status": "CONSUMED",
      "newDailyUsed": 55000.00,
      "newMonthlyUsed": 190000.00
    }

POST /api/v1/limits/customer/{customerId}/release
  Description: Release reserved limit (if payment fails/cancelled)
  Request Body:
    {
      "reservationId": "RES-XXXXX"
    }
  Response: 200 OK
    {
      "status": "RELEASED"
    }

PUT /api/v1/limits/customer/{customerId}
  Description: Update customer limit configuration (admin only)
  Request Body:
    {
      "dailyLimit": 150000.00,
      "monthlyLimit": 600000.00,
      "perTransactionLimit": 75000.00,
      "paymentTypeLimits": [
        {
          "paymentType": "EFT",
          "dailyLimit": 75000.00
        },
        {
          "paymentType": "RTC",
          "dailyLimit": 150000.00
        }
      ]
    }
  Response: 200 OK

GET /api/v1/validate/rules
  Description: Get current validation rules
  Response: 200 OK
    {
      "rules": [
        {
          "ruleId": "RULE-001",
          "name": "Daily transaction limit",
          "type": "LIMIT",
          "value": 50000.00
        }
      ]
    }
```

### Events Consumed
- `PaymentInitiatedEvent`

### Events Published
```json
// Validation Success
{
  "eventType": "PaymentValidatedEvent",
  "paymentId": "PAY-2025-XXXXXX",
  "validationResult": {
    "valid": true,
    "fraudScore": 0.15,
    "riskLevel": "LOW",
    "limitReservationId": "RES-XXXXX"
  }
}

// Validation Failure - Limit Exceeded
{
  "eventType": "ValidationFailedEvent",
  "paymentId": "PAY-2025-XXXXXX",
  "failureReason": "DAILY_LIMIT_EXCEEDED",
  "validationResult": {
    "valid": false,
    "failureReasons": [
      {
        "code": "DAILY_LIMIT_EXCEEDED",
        "message": "Daily limit exceeded",
        "currentLimit": 100000.00,
        "usedAmount": 95000.00,
        "requestedAmount": 10000.00
      }
    ]
  }
}

// Limit Consumed (after successful payment)
{
  "eventType": "LimitConsumedEvent",
  "customerId": "CUST-123456",
  "paymentId": "PAY-2025-XXXXXX",
  "amount": 10000.00,
  "paymentType": "EFT",
  "newDailyUsed": 55000.00,
  "newMonthlyUsed": 190000.00
}
```

### Validation Rules & Limit Checks

#### 1. Limit Validation (NEW)
- **Daily Limit Check**: `usedToday + paymentAmount <= customerDailyLimit`
- **Monthly Limit Check**: `usedThisMonth + paymentAmount <= customerMonthlyLimit`
- **Payment Type Limit**: `usedTodayForType + paymentAmount <= paymentTypeDailyLimit`
- **Per Transaction Limit**: `paymentAmount <= perTransactionLimit`
- **Transaction Count Limit**: `countToday < maxTransactionsPerDay`

#### 2. Compliance Validation
- **Account Status**: Must be ACTIVE
- **KYC Status**: Must be VERIFIED
- **FICA Status**: Must be COMPLIANT

#### 3. Fraud & Risk Validation
- **Fraud Score**: Must be < 0.7 (70%)
- **Velocity Check**: Max transactions per hour
- **Suspicious Pattern Detection**: Unusual transaction patterns

#### 4. Business Rules
- **Amount Validation**: Positive, within clearing system limits
- **Currency**: Must be ZAR
- **Account Verification**: Source account must exist and be accessible

### Limit Check Flow

```
1. Payment Initiated
   ↓
2. Validation Service receives PaymentInitiatedEvent
   ↓
3. Load Customer Limits (from database/cache)
   ↓
4. Calculate Available Limits
   - Daily: limit - used
   - Monthly: limit - used
   - Payment Type: limit - used
   ↓
5. Check if Payment Amount <= Available Limit
   ↓
6a. If SUFFICIENT:
    - Reserve limit (temporary hold)
    - Mark reservation with paymentId
    - Publish PaymentValidatedEvent
   ↓
6b. If INSUFFICIENT:
    - Publish ValidationFailedEvent (LIMIT_EXCEEDED)
    - Payment flow stops
   ↓
7. After successful payment:
    - Convert reservation to consumed
    - Update used limits
    - Publish LimitConsumedEvent
   ↓
8. If payment fails:
    - Release reservation
    - Restore available limit
```

### Database Schema
```sql
CREATE TABLE validation_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    rule_condition JSONB NOT NULL,
    priority INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE validation_results (
    validation_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    validation_status VARCHAR(20) NOT NULL,
    fraud_score DECIMAL(5,4),
    risk_level VARCHAR(20),
    failed_rules JSONB,
    validated_at TIMESTAMP NOT NULL,
    INDEX idx_payment_id (payment_id)
);
```

### Fraud API Integration

#### Fraud API Request

```http
POST https://fraud-api.example.com/api/v1/score
Content-Type: application/json
Authorization: Bearer {api_key}
X-Request-ID: {correlation_id}

{
  "transactionId": "PAY-2025-XXXXXX",
  "customerId": "CUST-123456",
  "sourceAccount": {
    "accountNumber": "1234567890",
    "accountType": "CURRENT",
    "accountAge_days": 365
  },
  "destinationAccount": {
    "accountNumber": "0987654321",
    "bankCode": "ABSA"
  },
  "transaction": {
    "amount": 10000.00,
    "currency": "ZAR",
    "paymentType": "RTC",
    "reference": "Payment for invoice"
  },
  "context": {
    "timestamp": "2025-10-11T10:30:00Z",
    "channel": "WEB",
    "deviceFingerprint": "device-hash-xxxxx",
    "ipAddress": "192.168.1.100",
    "geolocation": {
      "country": "ZA",
      "city": "Johannesburg",
      "coordinates": "-26.2041,28.0473"
    },
    "userAgent": "Mozilla/5.0..."
  },
  "customerProfile": {
    "accountAge_days": 730,
    "averageTransactionAmount": 5000.00,
    "transactionCount_30days": 25,
    "totalVolume_30days": 125000.00,
    "previousFraudIncidents": 0,
    "kycStatus": "VERIFIED",
    "ficaStatus": "COMPLIANT"
  }
}
```

#### Fraud API Response

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Request-ID: {correlation_id}
X-Response-Time: 245

{
  "transactionId": "PAY-2025-XXXXXX",
  "fraudScore": 0.15,
  "riskLevel": "LOW",
  "recommendation": "APPROVE",
  "confidence": 0.92,
  "fraudIndicators": [
    {
      "indicator": "VELOCITY_CHECK",
      "score": 0.05,
      "description": "Normal transaction velocity"
    },
    {
      "indicator": "AMOUNT_PATTERN",
      "score": 0.10,
      "description": "Amount within typical range"
    },
    {
      "indicator": "GEOLOCATION",
      "score": 0.00,
      "description": "Transaction from usual location"
    }
  ],
  "reasons": [],
  "modelVersion": "v2.5.1",
  "processingTime_ms": 245
}
```

#### High-Risk Response Example

```json
{
  "transactionId": "PAY-2025-XXXXXX",
  "fraudScore": 0.85,
  "riskLevel": "CRITICAL",
  "recommendation": "REJECT",
  "confidence": 0.88,
  "fraudIndicators": [
    {
      "indicator": "UNUSUAL_AMOUNT",
      "score": 0.45,
      "description": "Amount significantly higher than average"
    },
    {
      "indicator": "SUSPICIOUS_VELOCITY",
      "score": 0.30,
      "description": "Multiple transactions in short time period"
    },
    {
      "indicator": "GEOLOCATION_MISMATCH",
      "score": 0.10,
      "description": "Transaction from unusual location"
    }
  ],
  "reasons": [
    "Transaction amount 10x higher than average",
    "5 transactions in last 10 minutes",
    "IP address from high-risk country"
  ],
  "modelVersion": "v2.5.1",
  "processingTime_ms": 312
}
```

#### Integration Code

```java
@Service
public class FraudScoringService {
    
    @Value("${fraud.api.url}")
    private String fraudApiUrl;
    
    @Value("${fraud.api.key}")
    private String fraudApiKey;
    
    @Autowired
    private RestTemplate fraudApiRestTemplate;
    
    @CircuitBreaker(name = "fraudApi", fallbackMethod = "fraudApiFallback")
    @Retry(name = "fraudApi")
    @Timed(value = "fraud.api.call")
    public FraudScoreResponse scoreFraudRisk(PaymentValidationRequest request) {
        
        // Build fraud scoring request
        FraudScoringRequest fraudRequest = FraudScoringRequest.builder()
            .transactionId(request.getPaymentId())
            .customerId(request.getCustomerId())
            .sourceAccount(buildAccountInfo(request.getSourceAccount()))
            .destinationAccount(buildAccountInfo(request.getDestinationAccount()))
            .transaction(buildTransactionInfo(request))
            .context(buildContextInfo(request))
            .customerProfile(buildCustomerProfile(request.getCustomerId()))
            .build();
        
        // Call fraud API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(fraudApiKey);
        headers.set("X-Request-ID", request.getCorrelationId());
        
        HttpEntity<FraudScoringRequest> httpEntity = new HttpEntity<>(fraudRequest, headers);
        
        try {
            ResponseEntity<FraudScoreResponse> response = fraudApiRestTemplate.postForEntity(
                fraudApiUrl + "/api/v1/score",
                httpEntity,
                FraudScoreResponse.class
            );
            
            FraudScoreResponse fraudScore = response.getBody();
            
            // Log fraud check
            logFraudCheck(request.getPaymentId(), fraudScore);
            
            return fraudScore;
            
        } catch (RestClientException e) {
            log.error("Fraud API call failed for payment {}", request.getPaymentId(), e);
            throw new FraudApiException("Failed to get fraud score", e);
        }
    }
    
    /**
     * Fallback method when fraud API is unavailable
     */
    private FraudScoreResponse fraudApiFallback(PaymentValidationRequest request, Exception e) {
        log.warn("Fraud API unavailable, using fallback for payment {}", request.getPaymentId());
        
        // Strategy 1: Fail-open (allow transaction with monitoring)
        if (fraudApiFailOpenEnabled) {
            return FraudScoreResponse.builder()
                .fraudScore(0.5)
                .riskLevel("MEDIUM")
                .recommendation("APPROVE_WITH_MONITORING")
                .reasons(List.of("Fraud API unavailable - using fallback"))
                .fallbackUsed(true)
                .build();
        }
        
        // Strategy 2: Use rule-based fraud detection
        return ruleBasedFraudDetection(request);
    }
    
    private FraudScoreResponse ruleBasedFraudDetection(PaymentValidationRequest request) {
        // Simple rule-based fraud detection as fallback
        double score = 0.0;
        List<String> reasons = new ArrayList<>();
        
        // Check velocity (multiple transactions in short time)
        if (hasHighVelocity(request.getCustomerId())) {
            score += 0.3;
            reasons.add("High transaction velocity detected");
        }
        
        // Check unusual amount
        if (isUnusualAmount(request.getAmount(), request.getCustomerId())) {
            score += 0.2;
            reasons.add("Transaction amount unusual for customer");
        }
        
        // Determine risk level
        String riskLevel = determineRiskLevel(score);
        String recommendation = score < 0.7 ? "APPROVE" : "REJECT";
        
        return FraudScoreResponse.builder()
            .fraudScore(score)
            .riskLevel(riskLevel)
            .recommendation(recommendation)
            .reasons(reasons)
            .fallbackUsed(true)
            .build();
    }
    
    private String determineRiskLevel(double score) {
        if (score < 0.3) return "LOW";
        if (score < 0.6) return "MEDIUM";
        if (score < 0.8) return "HIGH";
        return "CRITICAL";
    }
}
```

#### Fraud Score Evaluation

```java
@Service
public class ValidationService {
    
    @Autowired
    private FraudScoringService fraudScoringService;
    
    public ValidationResult validatePayment(PaymentValidationRequest request) {
        
        // 1. Check customer limits
        LimitCheckResult limitCheck = checkCustomerLimits(request);
        if (!limitCheck.isSufficient()) {
            return ValidationResult.failed("LIMIT_EXCEEDED", limitCheck);
        }
        
        // 2. Call fraud scoring API
        FraudScoreResponse fraudScore = fraudScoringService.scoreFraudRisk(request);
        
        // 3. Evaluate fraud score
        if (fraudScore.getFraudScore() >= 0.8) {
            // CRITICAL risk - Auto-reject
            return ValidationResult.failed(
                "FRAUD_RISK_HIGH",
                "Transaction rejected due to high fraud risk",
                fraudScore
            );
        } else if (fraudScore.getFraudScore() >= 0.6) {
            // HIGH risk - Require additional verification
            return ValidationResult.requiresVerification(
                "ADDITIONAL_VERIFICATION_REQUIRED",
                fraudScore
            );
        }
        
        // 4. Check compliance (KYC, FICA)
        ComplianceCheckResult compliance = checkCompliance(request);
        if (!compliance.isCompliant()) {
            return ValidationResult.failed("COMPLIANCE_FAILED", compliance);
        }
        
        // All checks passed
        return ValidationResult.success(limitCheck, fraudScore, compliance);
    }
}
```

### Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Redis (caching rules)
- **Spring Cloud Circuit Breaker (Resilience4j)**
- **External Fraud Scoring API (REST client)**
- **RestTemplate / WebClient for API integration**

---

## 3. Account Adapter Service

### Responsibilities
- **Orchestrate** calls to external core banking systems
- Route account requests to appropriate backend system
- Aggregate responses from multiple systems
- Cache account metadata and routing rules
- Provide unified API interface for all account types
- Handle circuit breaking and failover for external systems
- **Does NOT store** account balances, transactions, or account master data

### External Core Banking Systems

The Account Adapter integrates with multiple backend systems:

| System | Account Types | Base URL Pattern | Authentication |
|--------|---------------|------------------|----------------|
| Current Accounts System | CURRENT, CHEQUE | `https://current-accounts.bank.internal/api/v1` | OAuth 2.0 |
| Savings System | SAVINGS, MONEY_MARKET | `https://savings.bank.internal/api/v1` | OAuth 2.0 |
| Investment System | INVESTMENT, UNIT_TRUST | `https://investments.bank.internal/api/v1` | OAuth 2.0 |
| Card System | CREDIT_CARD, DEBIT_CARD | `https://cards.bank.internal/api/v1` | OAuth 2.0 |
| Home Loan System | HOME_LOAN, MORTGAGE | `https://home-loans.bank.internal/api/v1` | OAuth 2.0 |
| Car Loan System | CAR_LOAN, VEHICLE_FINANCE | `https://vehicle-finance.bank.internal/api/v1` | OAuth 2.0 |
| Personal Loan System | PERSONAL_LOAN | `https://personal-loans.bank.internal/api/v1` | OAuth 2.0 |
| Business Banking | BUSINESS_CURRENT, BUSINESS_SAVINGS | `https://business-banking.bank.internal/api/v1` | OAuth 2.0 |

### API Endpoints

```yaml
GET /api/v1/accounts/{accountNumber}
  Description: Get account details (proxies to backend system)
  Response: 200 OK
    {
      "accountNumber": "1234567890",
      "accountHolder": "John Doe",
      "accountType": "CURRENT",
      "status": "ACTIVE",
      "balance": 10000.00,
      "availableBalance": 9500.00,
      "currency": "ZAR",
      "backendSystem": "CURRENT_ACCOUNTS_SYSTEM"
    }

POST /api/v1/accounts/{accountNumber}/debit
  Description: Debit account (calls backend system)
  Request Body:
    {
      "idempotencyKey": "uuid",
      "amount": 1000.00,
      "currency": "ZAR",
      "reference": "PAY-2025-XXXXXX",
      "description": "Payment to merchant"
    }
  Response: 200 OK
    {
      "transactionId": "TXN-BACKEND-XXXXX",
      "status": "COMPLETED",
      "newBalance": 9000.00
    }

POST /api/v1/accounts/{accountNumber}/credit
  Description: Credit account (calls backend system)
  Request Body:
    {
      "idempotencyKey": "uuid",
      "amount": 1000.00,
      "currency": "ZAR",
      "reference": "PAY-2025-XXXXXX",
      "description": "Payment received"
    }
  Response: 200 OK
    {
      "transactionId": "TXN-BACKEND-XXXXX",
      "status": "COMPLETED",
      "newBalance": 11000.00
    }

POST /api/v1/accounts/{accountNumber}/holds
  Description: Place a hold on account (calls backend system)
  Request Body:
    {
      "idempotencyKey": "uuid",
      "amount": 1000.00,
      "reference": "PAY-2025-XXXXXX",
      "expiryMinutes": 30
    }
  Response: 201 Created
    {
      "holdId": "HOLD-BACKEND-XXXXX",
      "status": "PLACED",
      "expiresAt": "2025-10-11T11:00:00Z"
    }

DELETE /api/v1/accounts/holds/{holdId}
  Description: Release a hold
  Response: 204 No Content

POST /api/v1/accounts/verify
  Description: Verify account ownership
  Request Body:
    {
      "accountNumber": "1234567890",
      "idNumber": "8001010000000"
    }
  Response: 200 OK
    {
      "verified": true,
      "accountHolder": "John Doe"
    }

GET /api/v1/accounts/route/{accountNumber}
  Description: Get routing information for an account
  Response: 200 OK
    {
      "accountNumber": "1234567890",
      "backendSystem": "CURRENT_ACCOUNTS_SYSTEM",
      "baseUrl": "https://current-accounts.bank.internal/api/v1",
      "accountType": "CURRENT"
    }
```

### Database Schema (Minimal - Routing & Caching Only)

```sql
-- Account routing metadata (determines which backend system to call)
CREATE TABLE account_routing (
    account_number VARCHAR(50) PRIMARY KEY,
    backend_system VARCHAR(50) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    base_url VARCHAR(200) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_verified TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_backend_system (backend_system),
    INDEX idx_account_type (account_type)
);

-- Backend system configuration
CREATE TABLE backend_systems (
    system_id VARCHAR(50) PRIMARY KEY,
    system_name VARCHAR(100) NOT NULL,
    base_url VARCHAR(200) NOT NULL,
    auth_type VARCHAR(20) NOT NULL,
    oauth_client_id VARCHAR(100),
    timeout_ms INTEGER DEFAULT 5000,
    retry_attempts INTEGER DEFAULT 3,
    circuit_breaker_enabled BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    health_check_url VARCHAR(200),
    last_health_check TIMESTAMP,
    health_status VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cache of recent account inquiries (to reduce backend calls)
CREATE TABLE account_cache (
    account_number VARCHAR(50) PRIMARY KEY,
    account_data JSONB NOT NULL,
    cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_expires_at (expires_at)
);

-- API call audit trail
CREATE TABLE api_call_log (
    call_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    backend_system VARCHAR(50) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    request_data JSONB,
    response_status INTEGER,
    response_time_ms INTEGER,
    success BOOLEAN,
    error_message TEXT,
    called_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_number (account_number),
    INDEX idx_backend_system (backend_system),
    INDEX idx_called_at (called_at DESC)
);
```

### Backend System Integration

#### REST Client Configuration

```java
@Configuration
public class BackendSystemClientConfig {
    
    @Bean
    public RestTemplate currentAccountsClient() {
        return createRestTemplate("CURRENT_ACCOUNTS_SYSTEM");
    }
    
    @Bean
    public RestTemplate savingsClient() {
        return createRestTemplate("SAVINGS_SYSTEM");
    }
    
    // ... other clients
    
    private RestTemplate createRestTemplate(String systemId) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add interceptors
        restTemplate.getInterceptors().add(new OAuth2Interceptor(systemId));
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        restTemplate.getInterceptors().add(new IdempotencyInterceptor());
        
        // Configure timeout
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        restTemplate.setRequestFactory(factory);
        
        return restTemplate;
    }
}
```

#### Service Implementation

```java
@Service
public class AccountAdapterService {
    
    @Autowired
    private AccountRoutingRepository routingRepository;
    
    @Autowired
    private Map<String, RestTemplate> backendClients;
    
    @Cacheable(value = "accounts", key = "#accountNumber", unless = "#result == null")
    public AccountDTO getAccount(String accountNumber) {
        // 1. Determine which backend system to call
        AccountRouting routing = routingRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        
        // 2. Get appropriate REST client
        RestTemplate client = backendClients.get(routing.getBackendSystem());
        
        // 3. Call backend system
        String url = routing.getBaseUrl() + "/accounts/" + accountNumber;
        
        try {
            ResponseEntity<AccountDTO> response = client.getForEntity(url, AccountDTO.class);
            return response.getBody();
        } catch (RestClientException e) {
            handleBackendError(routing.getBackendSystem(), e);
            throw new BackendSystemUnavailableException(routing.getBackendSystem());
        }
    }
    
    public DebitResponse debitAccount(String accountNumber, DebitRequest request) {
        AccountRouting routing = getRouting(accountNumber);
        RestTemplate client = backendClients.get(routing.getBackendSystem());
        
        String url = routing.getBaseUrl() + "/accounts/" + accountNumber + "/debit";
        
        // Add idempotency key
        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", request.getIdempotencyKey());
        
        HttpEntity<DebitRequest> httpEntity = new HttpEntity<>(request, headers);
        
        return client.postForObject(url, httpEntity, DebitResponse.class);
    }
    
    public CreditResponse creditAccount(String accountNumber, CreditRequest request) {
        AccountRouting routing = getRouting(accountNumber);
        RestTemplate client = backendClients.get(routing.getBackendSystem());
        
        String url = routing.getBaseUrl() + "/accounts/" + accountNumber + "/credit";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", request.getIdempotencyKey());
        
        HttpEntity<CreditRequest> httpEntity = new HttpEntity<>(request, headers);
        
        return client.postForObject(url, httpEntity, CreditResponse.class);
    }
}
```

#### Circuit Breaker Pattern

```java
@Service
public class ResilientAccountAdapterService {
    
    @Autowired
    private AccountAdapterService accountService;
    
    @CircuitBreaker(name = "currentAccountsSystem", fallbackMethod = "getAccountFallback")
    @Retry(name = "currentAccountsSystem")
    @Bulkhead(name = "currentAccountsSystem")
    public AccountDTO getAccount(String accountNumber) {
        return accountService.getAccount(accountNumber);
    }
    
    private AccountDTO getAccountFallback(String accountNumber, Exception e) {
        // Return cached data if available
        return accountCacheService.getCachedAccount(accountNumber)
            .orElseThrow(() -> new ServiceUnavailableException(
                "Backend system unavailable and no cached data available"));
    }
}
```

### Technology Stack
- Spring Boot 3.x
- Spring Cloud (Circuit Breaker, Resilience4j)
- Redis (caching account data, routing metadata)
- PostgreSQL (routing configuration, audit logs)
- REST Template / WebClient
- OAuth 2.0 Client

---

## 4. Routing Service

### Responsibilities
- Determine payment channel (SAMOS, RTC, ACH/EFT)
- Select appropriate clearing system
- Load balance across clearing connections
- Apply routing rules based on amount, time, destination

### API Endpoints

```yaml
POST /api/v1/routing/determine
  Description: Determine routing for payment
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "amount": 1000.00,
      "destinationBank": "FNB",
      "paymentType": "EFT",
      "priority": "NORMAL" | "HIGH"
    }
  Response: 200 OK
    {
      "clearingSystem": "RTC",
      "channel": "BANKSERV_RTC",
      "estimatedCompletionTime": "2025-10-11T10:35:00Z"
    }
```

### Routing Rules (Examples)
1. **Amount > R5,000,000**: Route to SAMOS (RTGS)
2. **Amount <= R5,000,000 AND Priority = HIGH**: Route to RTC
3. **Amount <= R5,000,000 AND Priority = NORMAL**: Route to ACH/EFT
4. **After 15:30 CAT**: Route to RTC (SAMOS closed)
5. **Destination Bank = Same Bank**: Route to internal transfer

### Database Schema
```sql
CREATE TABLE routing_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    priority INTEGER NOT NULL,
    condition_json JSONB NOT NULL,
    target_system VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE routing_decisions (
    decision_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    selected_system VARCHAR(50) NOT NULL,
    rule_applied VARCHAR(50),
    decided_at TIMESTAMP NOT NULL,
    INDEX idx_payment_id (payment_id)
);
```

### Technology Stack
- Spring Boot 3.x
- Redis (caching routing tables)
- Rule engine (Drools or custom)

---

## 5. Transaction Processing Service

### Responsibilities
- Create transaction records (double-entry bookkeeping)
- Manage transaction state machine
- Update transaction status
- Implement event sourcing for transactions
- Coordinate with settlement service

### API Endpoints

```yaml
POST /api/v1/transactions
  Description: Create a transaction
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "debitAccount": "1234567890",
      "creditAccount": "0987654321",
      "amount": 1000.00,
      "currency": "ZAR",
      "transactionType": "PAYMENT"
    }
  Response: 201 Created
    {
      "transactionId": "TXN-2025-XXXXXX",
      "status": "CREATED"
    }

GET /api/v1/transactions/{transactionId}
  Description: Get transaction details
  Response: 200 OK

PATCH /api/v1/transactions/{transactionId}/status
  Description: Update transaction status
  Request Body:
    {
      "status": "COMPLETED",
      "reason": "Cleared successfully"
    }
```

### Transaction State Machine
```
CREATED → VALIDATED → PROCESSING → CLEARING → COMPLETED
                ↓           ↓           ↓
              FAILED      FAILED     FAILED
                            ↓
                       COMPENSATING → REVERSED
```

### Database Schema
```sql
CREATE TABLE transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    debit_account VARCHAR(50) NOT NULL,
    credit_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status)
);

CREATE TABLE transaction_events (
    event_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    INDEX idx_transaction_id (transaction_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);

CREATE TABLE ledger_entries (
    entry_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    entry_type VARCHAR(10) NOT NULL, -- DEBIT or CREDIT
    amount DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_account_number (account_number)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL (with event store)
- Event sourcing library

---

## 6-8. Clearing Adapter Services (SAMOS, Bankserv, RTC)

### Shared Responsibilities
- Format messages for clearing system (ISO 20022 or ISO 8583)
- Send messages to clearing system
- Receive responses and acknowledgments
- Handle timeouts and retries
- Parse clearing responses

### Common API Endpoints

```yaml
POST /api/v1/clearing/submit
  Description: Submit payment to clearing system
  Request Body:
    {
      "transactionId": "TXN-2025-XXXXXX",
      "paymentDetails": { ... }
    }
  Response: 202 Accepted
    {
      "clearingReference": "CLR-XXXXX",
      "status": "SUBMITTED"
    }

GET /api/v1/clearing/{clearingReference}/status
  Description: Get clearing status
  Response: 200 OK
    {
      "clearingReference": "CLR-XXXXX",
      "status": "COMPLETED",
      "settledAt": "2025-10-11T10:35:00Z"
    }
```

### 6. SAMOS Adapter Specifics
- **Format**: ISO 20022 (pacs.008, pacs.002)
- **Protocol**: SWIFT
- **Threshold**: > R5 million
- **Operating Hours**: 08:00-15:30 CAT

### 7. BankservAfrica Adapter Specifics
- **Format**: Proprietary + ISO 8583
- **Protocol**: TCP/IP
- **Batch Cutoffs**: 08:00, 10:00, 12:00, 14:00 CAT
- **Settlement**: T+0 (RTC), T+1 (ACH)

### 8. RTC Adapter Specifics
- **Format**: ISO 20022
- **Protocol**: REST API
- **Availability**: 24/7/365
- **Response Time**: < 10 seconds

### Common Database Schema
```sql
CREATE TABLE clearing_submissions (
    submission_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    clearing_reference VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    request_message TEXT NOT NULL,
    response_message TEXT,
    submitted_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    completed_at TIMESTAMP,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status)
);
```

### Technology Stack
- Spring Boot 3.x
- ISO 20022 library (jaxb)
- ISO 8583 library (jPOS)
- HTTP/TCP clients

---

## 9. Settlement Service

### Responsibilities
- Calculate net settlement positions
- Generate settlement files
- Track settlement batches
- Reconcile with clearing systems
- Update account balances post-settlement

### API Endpoints

```yaml
POST /api/v1/settlement/batches
  Description: Create settlement batch
  Request Body:
    {
      "batchDate": "2025-10-11",
      "clearingSystem": "RTC"
    }
  Response: 201 Created
    {
      "batchId": "BATCH-2025-XXXXXX",
      "status": "PENDING"
    }

GET /api/v1/settlement/batches/{batchId}
  Description: Get settlement batch details

POST /api/v1/settlement/batches/{batchId}/finalize
  Description: Finalize and submit settlement batch

GET /api/v1/settlement/positions
  Description: Get current settlement positions
  Response: 200 OK
    {
      "positions": [
        {
          "account": "1234567890",
          "netPosition": -5000.00,
          "currency": "ZAR"
        }
      ]
    }
```

### Database Schema
```sql
CREATE TABLE settlement_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    batch_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_debit DECIMAL(18,2) NOT NULL,
    total_credit DECIMAL(18,2) NOT NULL,
    net_position DECIMAL(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    finalized_at TIMESTAMP,
    INDEX idx_batch_date (batch_date),
    INDEX idx_status (status)
);

CREATE TABLE settlement_transactions (
    settlement_txn_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    settlement_status VARCHAR(20) NOT NULL,
    included_at TIMESTAMP NOT NULL,
    FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Batch processing (Spring Batch)

---

## 10. Reconciliation Service

### Responsibilities
- Match internal transactions with clearing system responses
- Identify exceptions and discrepancies
- Generate reconciliation reports
- Handle dispute management
- Automated reconciliation for matched items

### API Endpoints

```yaml
POST /api/v1/reconciliation/run
  Description: Run reconciliation process
  Request Body:
    {
      "date": "2025-10-11",
      "clearingSystem": "RTC"
    }
  Response: 202 Accepted
    {
      "reconciliationId": "RECON-2025-XXXXXX",
      "status": "RUNNING"
    }

GET /api/v1/reconciliation/{reconciliationId}
  Description: Get reconciliation results

GET /api/v1/reconciliation/exceptions
  Description: Get unmatched transactions
  Response: 200 OK
    {
      "exceptions": [
        {
          "transactionId": "TXN-2025-XXXXXX",
          "reason": "Amount mismatch",
          "status": "PENDING_REVIEW"
        }
      ]
    }
```

### Database Schema
```sql
CREATE TABLE reconciliation_runs (
    reconciliation_id VARCHAR(50) PRIMARY KEY,
    run_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_transactions INTEGER NOT NULL,
    matched_count INTEGER NOT NULL,
    unmatched_count INTEGER NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    INDEX idx_run_date (run_date)
);

CREATE TABLE reconciliation_exceptions (
    exception_id VARCHAR(50) PRIMARY KEY,
    reconciliation_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50),
    clearing_reference VARCHAR(100),
    exception_type VARCHAR(50) NOT NULL,
    exception_reason TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    FOREIGN KEY (reconciliation_id) REFERENCES reconciliation_runs(reconciliation_id)
);
```

---

## 11. Notification Service

### Responsibilities
- Send SMS notifications
- Send email notifications
- Send push notifications (PWA)
- Webhook callbacks to external systems
- Manage notification templates

### API Endpoints

```yaml
POST /api/v1/notifications/send
  Description: Send notification
  Request Body:
    {
      "recipientId": "user-123",
      "channel": "SMS" | "EMAIL" | "PUSH",
      "templateId": "PAYMENT_SUCCESS",
      "parameters": {
        "paymentId": "PAY-2025-XXXXXX",
        "amount": "1000.00"
      }
    }
  Response: 202 Accepted
    {
      "notificationId": "NOTIF-XXXXX",
      "status": "QUEUED"
    }
```

### Events Consumed
- `PaymentCompletedEvent`
- `PaymentFailedEvent`
- `ValidationFailedEvent`

### Database Schema
```sql
CREATE TABLE notifications (
    notification_id VARCHAR(50) PRIMARY KEY,
    recipient_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    template_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_status (status)
);

CREATE TABLE notification_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    template_content TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Technology Stack
- Spring Boot 3.x
- Azure Notification Hub
- Twilio (SMS)
- SendGrid (Email)

---

## 12. Reporting Service

### Responsibilities
- Generate transaction reports
- Analytics and dashboards
- Compliance reports (SARB, FICA)
- Export data in various formats (PDF, Excel, CSV)
- Scheduled report generation

### API Endpoints

```yaml
POST /api/v1/reports/generate
  Description: Generate report
  Request Body:
    {
      "reportType": "TRANSACTION_SUMMARY",
      "dateRange": {
        "from": "2025-10-01",
        "to": "2025-10-11"
      },
      "format": "PDF" | "EXCEL" | "CSV",
      "filters": { ... }
    }
  Response: 202 Accepted
    {
      "reportId": "RPT-2025-XXXXXX",
      "status": "GENERATING"
    }

GET /api/v1/reports/{reportId}
  Description: Get report status

GET /api/v1/reports/{reportId}/download
  Description: Download generated report
  Response: Binary file
```

### Technology Stack
- Spring Boot 3.x
- Azure Synapse Analytics (data warehouse)
- JasperReports or Apache POI (report generation)

---

## 13. Saga Orchestrator Service

### Responsibilities
- Coordinate distributed transactions across services
- Implement compensation logic for failures
- Manage saga state machine
- Handle timeouts and retries
- Ensure eventual consistency

### Saga Definition Example

```yaml
PaymentSaga:
  steps:
    - name: ValidatePayment
      service: ValidationService
      action: POST /api/v1/validate/payment
      compensation: None
      
    - name: ReserveFunds
      service: AccountService
      action: POST /api/v1/accounts/{accountNumber}/holds
      compensation: DELETE /api/v1/accounts/holds/{holdId}
      
    - name: DetermineRouting
      service: RoutingService
      action: POST /api/v1/routing/determine
      compensation: None
      
    - name: CreateTransaction
      service: TransactionProcessingService
      action: POST /api/v1/transactions
      compensation: PATCH /api/v1/transactions/{transactionId}/status (CANCELLED)
      
    - name: SubmitToClearing
      service: ClearingAdapterService
      action: POST /api/v1/clearing/submit
      compensation: POST /api/v1/clearing/cancel
      
    - name: ProcessSettlement
      service: SettlementService
      action: POST /api/v1/settlement/process
      compensation: POST /api/v1/settlement/reverse
      
    - name: SendNotification
      service: NotificationService
      action: POST /api/v1/notifications/send
      compensation: None
```

### API Endpoints

```yaml
POST /api/v1/sagas/start
  Description: Start a new saga
  Request Body:
    {
      "sagaType": "PAYMENT_SAGA",
      "payload": { ... }
    }
  Response: 201 Created
    {
      "sagaId": "SAGA-2025-XXXXXX",
      "status": "RUNNING"
    }

GET /api/v1/sagas/{sagaId}
  Description: Get saga status

POST /api/v1/sagas/{sagaId}/compensate
  Description: Manually trigger compensation
```

### Database Schema
```sql
CREATE TABLE sagas (
    saga_id VARCHAR(50) PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_step VARCHAR(50),
    payload JSONB NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    INDEX idx_status (status)
);

CREATE TABLE saga_steps (
    step_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_status VARCHAR(20) NOT NULL,
    request_data JSONB,
    response_data JSONB,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    FOREIGN KEY (saga_id) REFERENCES sagas(saga_id)
);
```

### Technology Stack
- Spring Boot 3.x
- State machine library (Spring Statemachine or custom)
- PostgreSQL

---

## 14. API Gateway Service

### Responsibilities
- Route requests to appropriate microservices
- Authentication and authorization
- Rate limiting
- Request/response logging
- API versioning
- CORS handling

### Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: payment-service
          uri: lb://payment-initiation-service
          predicates:
            - Path=/api/v1/payments/**
          filters:
            - name: RateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
            - name: CircuitBreaker
              args:
                name: paymentServiceCircuitBreaker
        
        - id: account-service
          uri: lb://account-service
          predicates:
            - Path=/api/v1/accounts/**
```

### Technology Stack
- Spring Cloud Gateway
- Spring Security OAuth2 Resource Server
- Redis (rate limiting)

---

## 15. IAM Service

### Responsibilities
- User authentication (OAuth2/OIDC)
- User authorization (RBAC)
- Token management (JWT)
- User profile management
- Integration with Azure AD B2C

### API Endpoints

```yaml
POST /api/v1/auth/login
  Description: Authenticate user
  Request Body:
    {
      "username": "user@example.com",
      "password": "********"
    }
  Response: 200 OK
    {
      "accessToken": "jwt-token",
      "refreshToken": "refresh-token",
      "expiresIn": 900
    }

POST /api/v1/auth/refresh
  Description: Refresh access token

POST /api/v1/auth/logout
  Description: Logout user

GET /api/v1/users/me
  Description: Get current user profile
```

### Technology Stack
- Spring Boot 3.x
- Spring Security OAuth2
- Azure AD B2C
- JWT library

---

## 16. Audit Service

### Responsibilities
- Log all API calls
- Log all events
- Compliance audit trail
- Log retention and archival
- Audit report generation

### Database Schema
```sql
-- CosmosDB document structure
{
  "id": "audit-uuid",
  "eventType": "API_CALL",
  "timestamp": "2025-10-11T10:30:00Z",
  "userId": "user-123",
  "service": "PaymentInitiationService",
  "action": "POST /api/v1/payments",
  "requestData": { ... },
  "responseStatus": 201,
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "correlationId": "corr-uuid"
}
```

### Technology Stack
- Spring Boot 3.x
- Azure CosmosDB
- Azure Monitor

---

## Service Communication Matrix

| Service | Calls | Called By | Events Published | Events Consumed |
|---------|-------|-----------|------------------|-----------------|
| Payment Initiation | Validation | API Gateway | PaymentInitiatedEvent | - |
| Validation | Account, Fraud API | Saga Orchestrator | PaymentValidatedEvent | PaymentInitiatedEvent |
| Account | - | Validation, Saga | FundsReservedEvent | - |
| Routing | - | Saga Orchestrator | RoutingDeterminedEvent | PaymentValidatedEvent |
| Transaction Processing | Settlement | Saga Orchestrator | TransactionCreatedEvent | RoutingDeterminedEvent |
| Clearing Adapters | External Systems | Saga Orchestrator | ClearingSubmittedEvent | TransactionCreatedEvent |
| Settlement | - | Transaction Processing | SettlementCompleteEvent | ClearingCompletedEvent |
| Notification | SMS/Email APIs | - | - | PaymentCompletedEvent |
| Saga Orchestrator | All Core Services | Event Bus | SagaCompletedEvent | All Events |

---

## Build Order for AI Agents

### Phase 1: Foundation (Parallel)
1. Common Libraries (error handling, logging, DTOs)
2. API Gateway skeleton
3. Event Bus setup (Azure Service Bus topics/subscriptions)
4. Database setup scripts

### Phase 2: Independent Services (Parallel)
5. Account Service
6. Validation Service
7. Routing Service
8. Notification Service

### Phase 3: Core Processing (Sequential)
9. Payment Initiation Service
10. Transaction Processing Service
11. Clearing Adapters (can be parallel)

### Phase 4: Supporting Services (Parallel)
12. Settlement Service
13. Reconciliation Service
14. Reporting Service

### Phase 5: Orchestration
15. Saga Orchestrator (requires all core services)

### Phase 6: Security & Monitoring
16. IAM Service
17. Audit Service

---

**Next**: See `03-API-CONTRACTS.md` for detailed OpenAPI specifications
**Next**: See `04-EVENT-SCHEMAS.md` for AsyncAPI event schemas
