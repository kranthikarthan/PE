# Gateway Clarification - Resolving Redundancies

## Overview

This document clarifies the different "Gateway" components in the Payments Engine architecture and resolves naming redundancies.

---

## Gateway Components Identified

### Current State (Confusing)

Multiple gateway terms are used inconsistently across documentation:

1. **Azure API Management (APIM)** - External API gateway
2. **API Gateway Service (#18)** - Internal microservice (Spring Cloud Gateway)
3. **Azure Application Gateway** - Azure infrastructure component (WAF + Load Balancer)
4. **Payment Gateway Operator** - Kubernetes custom operator (not a gateway!)

**Problem**: This creates confusion about what does what.

---

## Clarified Architecture

### 1. External Layer (Internet → Azure)

**Component**: **Azure Application Gateway**
- **Type**: Azure Infrastructure Service
- **Purpose**: 
  - WAF (Web Application Firewall) - blocks attacks
  - SSL/TLS termination
  - Load balancer
  - DDoS protection
- **Location**: Azure edge (public-facing)
- **Handles**: HTTPS traffic from internet
- **Routes to**: Azure API Management

**Naming**: Keep as **Azure Application Gateway** (clear it's Azure infrastructure)

---

### 2. External API Layer (Azure → BFF/Services)

**Component**: **Azure API Management (APIM)**
- **Type**: Azure Managed Service
- **Purpose**:
  - External API facade
  - API versioning
  - Rate limiting (external clients)
  - API key management
  - Developer portal
  - OAuth 2.0 / Azure AD B2C integration
- **Location**: Azure API Management service
- **Handles**: External API calls (from partners, corporate clients)
- **Routes to**: BFF or Internal API Gateway

**Naming**: Keep as **Azure API Management (APIM)** (clear it's Azure service)

---

### 3. Internal API Layer (BFF → Services)

**Component**: **Internal API Gateway Service** (#18)
- **Type**: Microservice (Spring Cloud Gateway)
- **Purpose**:
  - Internal API routing
  - Service-to-service routing
  - Authentication/Authorization (internal)
  - Rate limiting (internal)
  - Circuit breaking
  - Load balancing (internal services)
- **Location**: AKS cluster (internal only)
- **Handles**: Internal API calls between microservices
- **Routes to**: Individual microservices

**Current Name**: API Gateway Service (#18)
**Proposed Rename**: **Internal API Gateway Service** (#18)

**Naming**: Rename to **Internal API Gateway Service** (clarifies it's internal)

---

### 4. Backend for Frontend (BFF) Layer

**Component**: **BFF Gateways** (3 separate)
- **Type**: BFF Microservices
- **Purpose**:
  - Optimize APIs for specific clients
  - Aggregate multiple service calls
  - Transform data for client needs
- **Location**: AKS cluster
- **Three BFFs**:
  1. **Web BFF** - Optimized for web portal (GraphQL)
  2. **Mobile BFF** - Optimized for mobile app (REST lightweight)
  3. **Partner BFF** - Optimized for partner APIs (REST comprehensive)

**Naming**: Keep as **BFF** (Backend for Frontend) - industry standard term

---

### 5. Kubernetes Operator (NOT a Gateway!)

**Component**: **Payment Service Operator**
- **Type**: Kubernetes Custom Operator
- **Purpose**:
  - Manage payment service lifecycle
  - Day 2 operations
  - Auto-scaling, health checks
- **Location**: Kubernetes control plane

**Current Name**: Payment Gateway Operator
**Proposed Rename**: **Payment Service Operator**

**Naming**: Rename to **Payment Service Operator** (it's not a gateway!)

---

## Corrected Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         INTERNET                                     │
│  (Web Browsers, Mobile Apps, Partner Systems, Corporate Clients)    │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  1. AZURE APPLICATION GATEWAY (Azure Infrastructure)                │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  • WAF (Web Application Firewall)                            │  │
│  │  • SSL/TLS Termination                                       │  │
│  │  • DDoS Protection                                           │  │
│  │  • Load Balancing (L7)                                       │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  2. AZURE API MANAGEMENT - APIM (Azure Managed Service)             │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  • External API Facade                                       │  │
│  │  • API Versioning (/v1, /v2)                                 │  │
│  │  • Rate Limiting (External clients)                          │  │
│  │  • API Key Management                                        │  │
│  │  • Developer Portal                                          │  │
│  │  • OAuth 2.0 / Azure AD B2C                                  │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
┌──────────────────────────┐   ┌──────────────────────────┐
│ Web/Mobile Apps          │   │ Partner/Corporate APIs   │
│ (via BFF)                │   │ (direct)                 │
└──────────┬───────────────┘   └──────────┬───────────────┘
           │                              │
           ▼                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│  3. BACKEND FOR FRONTEND (BFF) LAYER (3 BFF Services)               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────┐  │
│  │  Web BFF     │  │  Mobile BFF  │  │  Partner API BFF         │  │
│  │  (GraphQL)   │  │  (REST Lite) │  │  (REST Comprehensive)    │  │
│  └──────────────┘  └──────────────┘  └──────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  4. INTERNAL API GATEWAY SERVICE (#18) (Spring Cloud Gateway)       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  • Internal Service Routing                                  │  │
│  │  • Authentication/Authorization (internal)                   │  │
│  │  • Rate Limiting (internal)                                  │  │
│  │  • Circuit Breaking (Resilience4j)                           │  │
│  │  • Load Balancing (internal services)                        │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  5. MICROSERVICES (20 Services)                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  #1: Payment Initiation                                      │  │
│  │  #2: Validation Service                                      │  │
│  │  #3: Account Adapter                                         │  │
│  │  ... (all 20 services)                                       │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Traffic Flow Examples

### Example 1: Web User Makes Payment

```
User Browser
    │
    ▼ HTTPS
Azure Application Gateway (WAF, SSL termination)
    │
    ▼
Azure API Management (API versioning, rate limiting)
    │
    ▼
Web BFF (GraphQL, aggregates multiple calls)
    │
    ▼
Internal API Gateway Service (#18) (routes to services)
    │
    ├──▶ Payment Initiation Service (#1)
    ├──▶ Validation Service (#2)
    └──▶ Account Adapter Service (#3)
```

### Example 2: Partner API Call

```
Partner System
    │
    ▼ HTTPS + API Key
Azure Application Gateway (WAF, SSL termination)
    │
    ▼
Azure API Management (API key validation, rate limiting)
    │
    ▼
Partner API BFF (REST comprehensive, no aggregation)
    │
    ▼
Internal API Gateway Service (#18) (routes to services)
    │
    └──▶ Payment Initiation Service (#1)
```

### Example 3: Internal Service-to-Service

```
Payment Initiation Service (#1)
    │
    ▼
Internal API Gateway Service (#18) (service mesh alternative)
    │
    ├──▶ Validation Service (#2)
    ├──▶ Account Adapter Service (#3)
    └──▶ Routing Service (#4)
```

---

## Summary Table

| Component | Type | Layer | Purpose | Technology | Rename Required |
|-----------|------|-------|---------|------------|-----------------|
| **Azure Application Gateway** | Azure Infrastructure | Edge | WAF, SSL, DDoS, LB | Azure App Gateway | ❌ No |
| **Azure API Management** | Azure Managed Service | External API | API facade, versioning, external rate limiting | Azure APIM | ❌ No |
| **Web/Mobile/Partner BFF** | Microservices (3) | BFF Layer | Client-optimized APIs, aggregation | Spring Boot, GraphQL | ❌ No |
| **Internal API Gateway Service** | Microservice (#18) | Internal API | Internal routing, auth, rate limiting | Spring Cloud Gateway | ✅ Yes (add "Internal") |
| **Payment Service Operator** | Kubernetes Operator | K8s Control Plane | Day 2 operations, lifecycle | Operator SDK | ✅ Yes (remove "Gateway") |

---

## Recommended Changes

### 1. Rename "API Gateway Service" → "Internal API Gateway Service"

**Files to Update**:
- `docs/00-ARCHITECTURE-OVERVIEW.md`
- `docs/02-MICROSERVICES-BREAKDOWN.md`
- `README.md`
- All summary documents
- All diagram files

**Change**:
```
OLD: 18. **API Gateway Service** (#18)
NEW: 18. **Internal API Gateway Service** (#18)
```

### 2. Rename "Payment Gateway Operator" → "Payment Service Operator"

**Files to Update**:
- `docs/30-KUBERNETES-OPERATORS-DAY2.md`
- `KUBERNETES-OPERATORS-SUMMARY.md`

**Change**:
```
OLD: Payment Gateway Operator
NEW: Payment Service Operator
```

### 3. Add Clear Definitions to Architecture Overview

**Add to `docs/00-ARCHITECTURE-OVERVIEW.md`**:

```markdown
## Gateway Layer Clarification

The Payments Engine uses multiple gateway components at different layers:

1. **Azure Application Gateway** - Azure infrastructure (WAF, SSL, LB)
2. **Azure API Management (APIM)** - External API facade
3. **BFF Layer** - 3 Backend-for-Frontend services (Web, Mobile, Partner)
4. **Internal API Gateway Service (#18)** - Internal routing (Spring Cloud Gateway)

Each serves a distinct purpose and should not be confused.
```

---

## Action Items

- [ ] Rename "API Gateway Service" to "Internal API Gateway Service" across all docs
- [ ] Rename "Payment Gateway Operator" to "Payment Service Operator"
- [ ] Update all diagrams to show clear gateway layers
- [ ] Add gateway clarification section to architecture overview
- [ ] Update all summary documents
- [ ] Verify consistency across 66 files

---

**Priority**: HIGH - This confusion affects understanding of the entire architecture

**Estimated Effort**: 1-2 hours to update all documents

---

**Last Updated**: 2025-10-12  
**Status**: ⚠️ Action Required
