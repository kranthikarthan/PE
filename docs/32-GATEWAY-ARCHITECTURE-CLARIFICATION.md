# Gateway Architecture - Layer Clarification

## Overview

This document provides a **definitive clarification** of all "Gateway" components in the Payments Engine architecture, eliminating confusion about their distinct roles and responsibilities.

**Problem Identified**: Multiple components use the term "Gateway", causing confusion about what does what.

**Solution**: This document clearly defines each gateway layer, its purpose, and technology.

---

## Gateway Layers (4 Distinct Layers)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         INTERNET                                     │
│  (Web Browsers, Mobile Apps, Partner Systems, Corporate Clients)    │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTPS
                             ▼
╔═════════════════════════════════════════════════════════════════════╗
║  LAYER 1: EDGE GATEWAY (Azure Application Gateway)                  ║
║  ┌──────────────────────────────────────────────────────────────┐  ║
║  │  Type: Azure Infrastructure Service                          │  ║
║  │  Purpose: Security, SSL, DDoS Protection                     │  ║
║  │  Technology: Azure Application Gateway v2                    │  ║
║  └──────────────────────────────────────────────────────────────┘  ║
╚════════════════════════════┬════════════════════════════════════════╝
                             │
                             ▼
╔═════════════════════════════════════════════════════════════════════╗
║  LAYER 2: API MANAGEMENT GATEWAY (Azure APIM)                       ║
║  ┌──────────────────────────────────────────────────────────────┐  ║
║  │  Type: Azure Managed Service                                 │  ║
║  │  Purpose: External API Facade, Versioning, Rate Limiting     │  ║
║  │  Technology: Azure API Management (APIM)                     │  ║
║  └──────────────────────────────────────────────────────────────┘  ║
╚════════════════════════════┬════════════════════════════════════════╝
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
╔═════════════════════════╗   ╔═════════════════════════════════════╗
║  LAYER 3: BFF LAYER     ║   ║  LAYER 3: DIRECT API ACCESS         ║
║  (Backend for Frontend) ║   ║  (Partner/Corporate APIs)           ║
║  ┌────────────────────┐ ║   ║  ┌─────────────────────────────┐  ║
║  │ Web BFF (GraphQL)  │ ║   ║  │ Partner API BFF (REST)      │  ║
║  │ Mobile BFF (REST)  │ ║   ║  │ Corporate API BFF (REST)    │  ║
║  └────────────────────┘ ║   ║  └─────────────────────────────┘  ║
╚═══════════┬═════════════╝   ╚═══════════┬═════════════════════════╝
            │                             │
            └─────────────┬───────────────┘
                          │
                          ▼
╔═════════════════════════════════════════════════════════════════════╗
║  LAYER 4: INTERNAL API GATEWAY (#18)                                ║
║  ┌──────────────────────────────────────────────────────────────┐  ║
║  │  Type: Microservice (Spring Cloud Gateway)                   │  ║
║  │  Purpose: Internal Service Routing, Circuit Breaking         │  ║
║  │  Technology: Spring Cloud Gateway + Redis                    │  ║
║  └──────────────────────────────────────────────────────────────┘  ║
╚════════════════════════════┬════════════════════════════════════════╝
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  MICROSERVICES (20 Services)                                         │
│  #1: Payment Initiation, #2: Validation, #3: Account Adapter, ...   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Layer 1: Edge Gateway

### Azure Application Gateway

**Type**: Azure Infrastructure Service (PaaS)

**Responsibilities**:
- SSL/TLS termination (HTTPS → HTTP)
- Web Application Firewall (WAF) - OWASP Top 10 protection
- DDoS protection (Azure DDoS Protection Standard)
- Load balancing (Layer 7)
- URL-based routing
- Session affinity

**Technology**:
- Azure Application Gateway v2 (WAF_v2 SKU)
- Zone-redundant deployment
- Autoscaling (2-125 instances)

**Configuration**:
```hcl
resource "azurerm_application_gateway" "payments" {
  name                = "payments-appgw"
  resource_group_name = "payments-rg"
  location            = "southafricanorth"
  
  sku {
    name     = "WAF_v2"
    tier     = "WAF_v2"
    capacity = 2
  }
  
  waf_configuration {
    enabled          = true
    firewall_mode    = "Prevention"
    rule_set_type    = "OWASP"
    rule_set_version = "3.2"
  }
  
  # SSL Certificate
  ssl_certificate {
    name     = "payments-ssl-cert"
    data     = file("cert.pfx")
    password = var.ssl_password
  }
  
  # Backend pool (APIM)
  backend_address_pool {
    name = "apim-backend"
    fqdns = ["payments-apim.azure-api.net"]
  }
}
```

**Incoming Traffic**: Internet (Web, Mobile, Partners)  
**Outgoing Traffic**: Azure API Management (APIM)

**Not to be confused with**: Internal API Gateway Service (#18)

---

## Layer 2: API Management Gateway

### Azure API Management (APIM)

**Type**: Azure Managed Service (PaaS)

**Responsibilities**:
- External API facade (/api/v1/payments)
- API versioning (/v1, /v2)
- Rate limiting (per client, per subscription)
- API key management
- OAuth 2.0 / Azure AD B2C integration
- Developer portal
- API analytics
- Request/response transformation
- Caching (external APIs)

**Technology**:
- Azure API Management (Premium tier)
- Multi-region deployment
- VNet integration
- Self-hosted gateway (optional)

**Configuration**:
```hcl
resource "azurerm_api_management" "payments" {
  name                = "payments-apim"
  location            = "southafricanorth"
  resource_group_name = "payments-rg"
  publisher_name      = "Payments Engine"
  publisher_email     = "api@payments.io"
  
  sku_name = "Premium_1"
  
  # VNet integration
  virtual_network_type = "Internal"
  virtual_network_configuration {
    subnet_id = azurerm_subnet.apim.id
  }
  
  # Policies
  policy {
    xml_content = <<XML
<policies>
  <inbound>
    <rate-limit calls="100" renewal-period="60" />
    <quota calls="10000" renewal-period="86400" />
    <cors allow-credentials="true">
      <allowed-origins>
        <origin>https://portal.payments.io</origin>
      </allowed-origins>
    </cors>
    <validate-jwt header-name="Authorization">
      <openid-config url="https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration" />
    </validate-jwt>
  </inbound>
</policies>
XML
  }
}
```

**API Definitions**:
```yaml
# Example API in APIM
apis:
  - name: Payments API v1
    path: /api/v1
    protocols: [https]
    operations:
      - POST /payments
      - GET /payments/{id}
      - GET /payments/status/{id}
    backend: https://web-bff.payments.svc.cluster.local
    rateLimit: 1000 req/min
```

**Incoming Traffic**: Azure Application Gateway  
**Outgoing Traffic**: BFFs or Internal API Gateway

**Not to be confused with**: Internal API Gateway Service (#18)

---

## Layer 3: Backend for Frontend (BFF)

### Three BFF Services

**Type**: Microservices (Spring Boot)

**Purpose**: Optimize APIs for specific client types

#### 3a. Web BFF

**Responsibilities**:
- GraphQL API for web portal
- Aggregate multiple service calls
- Transform data for web UI
- Batch operations

**Technology**: Spring Boot, Spring GraphQL

**Example**:
```graphql
type Query {
  payment(id: ID!): Payment
  payments(customerId: ID!, page: Int): PaymentConnection
  account(accountId: ID!): Account
}

type Mutation {
  createPayment(input: PaymentInput!): PaymentResult
  cancelPayment(id: ID!): CancelResult
}
```

#### 3b. Mobile BFF

**Responsibilities**:
- Lightweight REST API for mobile
- Minimize payload size
- Offline support
- Push notification registration

**Technology**: Spring Boot, REST

**Example**:
```json
POST /api/mobile/v1/payments
{
  "amount": 1000,
  "to": "+27821234567",  // Simplified mobile number
  "note": "Lunch money"
}

Response (lightweight):
{
  "id": "PAY-123",
  "status": "PENDING"
}
```

#### 3c. Partner API BFF

**Responsibilities**:
- Comprehensive REST API for partners
- Batch operations
- Webhook support
- Detailed responses

**Technology**: Spring Boot, REST, AsyncAPI

**Example**:
```json
POST /api/partner/v1/payments/batch
{
  "batchId": "BATCH-001",
  "payments": [ ... 1000 payments ... ],
  "callbackUrl": "https://partner.com/webhook"
}

Response (comprehensive):
{
  "batchId": "BATCH-001",
  "status": "PROCESSING",
  "submittedAt": "2025-10-12T10:00:00Z",
  "estimatedCompletion": "2025-10-12T10:15:00Z",
  "webhookRegistered": true,
  "totalPayments": 1000,
  "trackingUrl": "https://api.payments.io/batch/BATCH-001"
}
```

**Incoming Traffic**: Azure API Management  
**Outgoing Traffic**: Internal API Gateway Service (#18)

**Not to be confused with**: API Gateway Service

---

## Layer 4: Internal API Gateway

### Internal API Gateway Service (#18)

**Type**: Microservice (Spring Cloud Gateway)

**Responsibilities**:
- **Internal-only** routing (not exposed externally)
- Service-to-service communication
- Internal authentication/authorization
- Circuit breaking (Resilience4j)
- Rate limiting (internal)
- Load balancing (round-robin, weighted)
- Request/response logging
- Metrics collection

**Technology**:
- Spring Cloud Gateway
- Redis (for rate limiting)
- Resilience4j (circuit breakers)

**Configuration**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        # Route to Payment Initiation
        - id: payment-initiation-route
          uri: http://payment-initiation-service:8080
          predicates:
            - Path=/internal/payments/**
          filters:
            - name: CircuitBreaker
              args:
                name: paymentInitiationCB
                fallbackUri: forward:/fallback/payment-initiation
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 1000
                redis-rate-limiter.burstCapacity: 2000
        
        # Route to Validation Service
        - id: validation-route
          uri: http://validation-service:8080
          predicates:
            - Path=/internal/validation/**
          filters:
            - CircuitBreaker
        
        # ... routes for all 20 services
```

**Key Features**:
- ✅ **Internal only** (not exposed to internet)
- ✅ Circuit breaking (fails fast if service down)
- ✅ Rate limiting (prevent service overload)
- ✅ Load balancing (distribute traffic)
- ✅ Request tracing (correlation ID propagation)

**Incoming Traffic**: BFFs, Internal services  
**Outgoing Traffic**: Individual microservices (#1-#20)

**NOT FOR**: External API exposure (use APIM for that)

---

## Comparison Table

| Layer | Component | Type | Exposed to Internet | Purpose | Technology | Port |
|-------|-----------|------|---------------------|---------|------------|------|
| **1** | **Azure Application Gateway** | Azure Infrastructure | ✅ Yes | WAF, SSL, DDoS, LB | Azure App Gateway | 443 |
| **2** | **Azure API Management** | Azure Managed Service | ✅ Yes (via App Gw) | External API facade, versioning | Azure APIM | 443 |
| **3** | **BFF Layer** | Microservices (3) | ❌ No (via APIM) | Client-optimized APIs | Spring Boot | 8080 |
| **4** | **Internal API Gateway** | Microservice (#18) | ❌ No (internal only) | Internal routing | Spring Cloud Gateway | 8080 |

---

## Traffic Flow Patterns

### Pattern 1: Web User Payment

```
User Browser (https://portal.payments.io)
    │
    ├─ DNS resolves to Azure Application Gateway public IP
    │
    ▼ HTTPS (443)
┌─────────────────────────────────────────────┐
│ LAYER 1: Azure Application Gateway          │
│ • WAF checks request (block if malicious)   │
│ • SSL termination (HTTPS → HTTP)            │
│ • DDoS protection                           │
└─────────────────┬───────────────────────────┘
                  │ HTTP (internal)
                  ▼
┌─────────────────────────────────────────────┐
│ LAYER 2: Azure API Management (APIM)        │
│ • API key validation                        │
│ • Rate limit check (100 req/min per user)  │
│ • OAuth 2.0 token validation                │
│ • Route to /api/v1/payments                 │
└─────────────────┬───────────────────────────┘
                  │ HTTP (VNet)
                  ▼
┌─────────────────────────────────────────────┐
│ LAYER 3: Web BFF (GraphQL)                  │
│ • Parse GraphQL query                       │
│ • Aggregate calls (payment + account data)  │
│ • Transform to web-optimized response       │
└─────────────────┬───────────────────────────┘
                  │ HTTP (cluster)
                  ▼
┌─────────────────────────────────────────────┐
│ LAYER 4: Internal API Gateway (#18)         │
│ • Route to Payment Initiation (#1)          │
│ • Circuit breaker check                     │
│ • Internal rate limiting                    │
└─────────────────┬───────────────────────────┘
                  │ HTTP (cluster)
                  ▼
┌─────────────────────────────────────────────┐
│ Payment Initiation Service (#1)             │
│ • Create payment                            │
│ • Publish PaymentInitiatedEvent             │
└─────────────────────────────────────────────┘
```

### Pattern 2: Partner API Call

```
Partner System (https://api.payments.io)
    │
    ▼ HTTPS + API Key
Azure Application Gateway (Layer 1)
    │ WAF, SSL, DDoS
    ▼
Azure API Management (Layer 2)
    │ API key validation, Rate limiting
    ▼
Partner API BFF (Layer 3)
    │ REST comprehensive, minimal aggregation
    ▼
Internal API Gateway (#18) (Layer 4)
    │ Internal routing
    ▼
Payment Initiation Service (#1)
```

### Pattern 3: Internal Service-to-Service

```
Payment Initiation Service (#1)
    │
    ▼ (Option A: Direct call)
Validation Service (#2)

    OR

Payment Initiation Service (#1)
    │
    ▼ (Option B: Via Internal API Gateway)
Internal API Gateway (#18)
    │ Circuit breaking, load balancing
    ▼
Validation Service (#2)
```

**Recommendation**: Use **Service Mesh (Istio)** for service-to-service, bypass Internal API Gateway for internal calls.

---

## Decision Matrix: Which Gateway When?

| Use Case | Layer | Component |
|----------|-------|-----------|
| **External web users** | 1+2+3+4 | App Gateway → APIM → Web BFF → Internal API GW |
| **External mobile users** | 1+2+3+4 | App Gateway → APIM → Mobile BFF → Internal API GW |
| **External partner APIs** | 1+2+3+4 | App Gateway → APIM → Partner BFF → Internal API GW |
| **Internal service calls** | 4 or Direct | Internal API GW (optional) or Direct (via Istio) |
| **Batch file upload** | 1+2+4 | App Gateway → APIM → Internal API GW → Batch Service |

---

## Redundancy Resolution

### ❌ Remove These Terms:

1. **"API Gateway" (ambiguous)** → Replace with specific layer name
2. **"Payment Gateway" (incorrect)** → This is NOT a payment gateway (like Stripe), it's a payment processing engine
3. **"Payment Gateway Operator"** → Rename to "Payment Service Operator"

### ✅ Use These Terms:

1. **"Azure Application Gateway"** - Layer 1 (edge)
2. **"Azure API Management (APIM)"** - Layer 2 (external API)
3. **"Web/Mobile/Partner BFF"** - Layer 3 (client-optimized)
4. **"Internal API Gateway Service (#18)"** - Layer 4 (internal routing)

---

## Updated Naming Convention

### Before (Confusing)

```
❌ API Gateway Service (#18) - What does this do?
❌ Payment Gateway Operator - Is this a payment processor?
❌ Application Gateway - Which one?
❌ API Gateway (Spring Cloud) - Same as above?
```

### After (Clear)

```
✅ Azure Application Gateway - Edge layer (WAF, SSL, DDoS)
✅ Azure API Management (APIM) - External API layer
✅ Web/Mobile/Partner BFF - Client-optimized layer
✅ Internal API Gateway Service (#18) - Internal routing layer
✅ Payment Service Operator - Kubernetes operator (not a gateway!)
```

---

## When to Use Each Gateway

### Azure Application Gateway (Layer 1)
**Use When**: 
- ✅ Need WAF protection
- ✅ Need SSL/TLS termination
- ✅ Need DDoS protection
- ✅ Traffic from internet

**Don't Use When**:
- ❌ Internal service-to-service communication

---

### Azure API Management (Layer 2)
**Use When**:
- ✅ Exposing APIs to external partners
- ✅ Need API versioning (/v1, /v2)
- ✅ Need developer portal
- ✅ Need API key management
- ✅ Need external rate limiting

**Don't Use When**:
- ❌ Internal service-to-service communication
- ❌ Already have BFF aggregation

---

### BFF Layer (Layer 3)
**Use When**:
- ✅ Need client-specific API optimization
- ✅ Need to aggregate multiple service calls
- ✅ Need different API styles (GraphQL vs REST)
- ✅ Need to transform data for UI

**Don't Use When**:
- ❌ Simple pass-through APIs (use APIM directly)

---

### Internal API Gateway Service (Layer 4)
**Use When**:
- ✅ Need circuit breaking
- ✅ Need internal rate limiting
- ✅ Need centralized internal routing
- ✅ Don't have service mesh

**Don't Use When**:
- ❌ Have Istio service mesh (use Istio instead)
- ❌ External traffic (use APIM)

**Note**: With Istio service mesh, this layer becomes **optional** (Istio provides circuit breaking, load balancing, mTLS).

---

## Simplified Recommendation

### For Production Deployment

**Recommended Layers** (without redundancy):

```
Internet
    │
    ▼
Layer 1: Azure Application Gateway (WAF, SSL, DDoS)
    │
    ▼
Layer 2: Azure API Management (External API facade)
    │
    ├──▶ Web BFF (GraphQL)
    ├──▶ Mobile BFF (REST lightweight)
    └──▶ Partner BFF (REST comprehensive)
          │
          ▼
    ┌─────────────────────────┐
    │ Direct Service Calls    │
    │ (via Istio Service Mesh)│
    └─────────────────────────┘
          │
          ▼
    20 Microservices
```

**Internal API Gateway Service (#18)**: **OPTIONAL** (use only if NOT using Istio)

---

## Final Architecture (Clarified)

### Gateway Layers: 2-4 depending on deployment

**Minimum (Development)**:
1. Internal API Gateway (#18) → Microservices

**Standard (Production without Service Mesh)**:
1. Azure Application Gateway (WAF)
2. Azure API Management (APIM)
3. BFF Layer (3 BFFs)
4. Internal API Gateway (#18)
5. Microservices (20)

**Recommended (Production with Service Mesh)**:
1. Azure Application Gateway (WAF)
2. Azure API Management (APIM)
3. BFF Layer (3 BFFs)
4. ~~Internal API Gateway (#18)~~ → **REMOVED** (Istio replaces this)
5. Microservices (20) with Istio Service Mesh

---

## Implementation Decision

### Option A: Keep Internal API Gateway (#18)

**Pros**:
- Centralized internal routing
- Familiar Spring Cloud Gateway
- Easy to configure

**Cons**:
- Redundant with Istio
- Extra hop (latency)
- Extra service to manage

---

### Option B: Remove Internal API Gateway (#18) - Use Istio

**Pros**:
- ✅ No redundancy
- ✅ Lower latency (no extra hop)
- ✅ Istio provides: circuit breaking, load balancing, mTLS, retries
- ✅ One less service to manage (19 microservices instead of 20)

**Cons**:
- Requires Istio deployment
- Learning curve for Istio

**Recommendation**: **Option B** - Remove Internal API Gateway (#18), use Istio service mesh

---

## Revised Microservices Count

### If Keeping Internal API Gateway (#18)
**Total**: 20 microservices

### If Removing Internal API Gateway (#18) - RECOMMENDED
**Total**: 19 microservices

**Rationale**: Istio service mesh provides all Internal API Gateway capabilities:
- Circuit breaking ✅
- Load balancing ✅
- mTLS ✅
- Rate limiting ✅
- Retries ✅
- Metrics ✅

**No need for separate Internal API Gateway service.**

---

## Summary

### Gateway Layers Clarified

1. ✅ **Azure Application Gateway** - Edge layer (WAF, SSL, DDoS)
2. ✅ **Azure API Management** - External API layer (versioning, rate limiting)
3. ✅ **BFF Layer (3 services)** - Client-optimized APIs (Web, Mobile, Partner)
4. ⚠️ **Internal API Gateway Service (#18)** - **OPTIONAL** (use Istio instead)

### Redundancies Resolved

- ❌ "API Gateway" (ambiguous) → ✅ Specific layer names
- ❌ "Payment Gateway Operator" → ✅ "Payment Service Operator"
- ⚠️ Internal API Gateway (#18) → **OPTIONAL** (redundant with Istio)

### Final Recommendation

**Remove Internal API Gateway Service (#18)**, reducing microservices from **20 → 19**.

Use Istio service mesh for all internal routing needs.

---

**Last Updated**: 2025-10-12  
**Status**: ✅ Redundancy Identified, Resolution Proposed  
**Decision Required**: Remove Internal API Gateway (#18)?
