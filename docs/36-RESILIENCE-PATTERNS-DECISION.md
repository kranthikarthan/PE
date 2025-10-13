# Resilience Patterns Decision: Istio vs Resilience4j

## Overview

This document clarifies the **architectural decision** regarding resilience patterns (circuit breakers, retries, timeouts, rate limiting) and resolves the potential **duplication/conflict** between:

1. **Istio Service Mesh** (infrastructure-level)
2. **Resilience4j** (application-level)

**Date**: 2025-10-12  
**Status**: ✅ Architectural Decision Record (ADR)

---

## Problem Statement

The Payments Engine architecture implements resilience patterns in **TWO places**:

1. **Istio Service Mesh** (Feature 5.1):
   - Circuit breakers (configured via Istio `DestinationRule`)
   - Retries (configured via Istio `VirtualService`)
   - Timeouts (configured via Istio `VirtualService`)
   - Rate limiting (configured via Istio `EnvoyFilter`)

2. **Resilience4j** (Spring Boot annotations):
   - `@CircuitBreaker` in Account Adapter Service
   - `@Retry` in Account Adapter Service
   - `@Bulkhead` in Account Adapter Service
   - `@Timeout` in Account Adapter Service
   - `@RateLimiter` in Partner BFF

**Conflict**: This creates duplication, confusion, and potential conflicts. Which pattern should be used where?

---

## Decision

### ✅ Use **Istio** for EAST-WEST Traffic (Internal Service-to-Service)

**EAST-WEST** = Communication between services **INSIDE** the Kubernetes cluster

**Examples**:
- Payment Initiation Service → Validation Service
- Validation Service → Routing Service
- Saga Orchestrator → Transaction Processing Service
- Web BFF → Payment Initiation Service

**Why Istio?**
- ✅ **Zero code changes**: No need to add `@CircuitBreaker` in every service
- ✅ **Consistent policies**: Centralized configuration for all 20 microservices
- ✅ **Automatic mTLS**: Encryption + authentication out of the box
- ✅ **Observability**: Automatic metrics, tracing, logging
- ✅ **Dynamic routing**: Canary deployments, A/B testing, traffic shifting
- ✅ **Multi-language support**: Works for Java, Python, Go, Node.js services

**Configuration** (Declarative YAML):
```yaml
# Istio DestinationRule - Circuit Breaker
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: payment-service-circuit-breaker
spec:
  host: payment-service.default.svc.cluster.local
  trafficPolicy:
    connectionPool:
      http:
        http1MaxPendingRequests: 100
        maxRequestsPerConnection: 2
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 60s
      maxEjectionPercent: 50
      minHealthPercent: 50
```

```yaml
# Istio VirtualService - Retry
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: payment-service-retry
spec:
  hosts:
    - payment-service.default.svc.cluster.local
  http:
    - route:
        - destination:
            host: payment-service.default.svc.cluster.local
      retries:
        attempts: 3
        perTryTimeout: 2s
        retryOn: 5xx,reset,connect-failure,refused-stream
```

---

### ✅ Use **Resilience4j** for NORTH-SOUTH Traffic (External API Calls)

**NORTH-SOUTH** = Communication with services **OUTSIDE** the Kubernetes cluster

**Examples**:
- Account Adapter Service → **External Core Banking Systems** (Current, Savings, Investment, Card, Loan)
- SWIFT Adapter → **External SWIFT Alliance Lite2 API**
- SAMOS Adapter → **External SAMOS RTGS System**
- BankservAfrica Adapter → **External BankservAfrica ACH**
- PayShap Adapter → **External PayShap P2P System**
- RTC Adapter → **External RTC Real-Time System**
- Fraud Service → **External Fraud Scoring API**
- Sanctions Screening → **External OFAC/UN/EU API**
- Notification Service → **External IBM MQ Broker** (optional)

**Why Resilience4j?**
- ✅ **Istio doesn't control external traffic**: Istio sidecars only intercept traffic within the mesh
- ✅ **Fine-grained control**: Per-client configuration (different circuit breakers for each core banking system)
- ✅ **Business logic integration**: Fallback methods can implement business-specific logic
- ✅ **Caching integration**: Fallback to cached data when external API fails
- ✅ **Spring Boot integration**: Native annotation-based approach for Spring developers

**Configuration** (Application-level Java):
```java
// Account Adapter Service - External API Call
@Service
public class CurrentAccountService {
    
    @Autowired
    private CurrentAccountClient currentAccountClient; // Feign client
    
    @CircuitBreaker(name = "currentAccountSystem", fallbackMethod = "debitFallback")
    @Retry(name = "currentAccountSystem")
    @Bulkhead(name = "currentAccountSystem")
    @Timeout(name = "currentAccountSystem")
    public DebitResponse debit(String accountId, DebitRequest request) {
        // Call EXTERNAL core banking system (OUTSIDE Kubernetes cluster)
        return currentAccountClient.debit(accountId, request);
    }
    
    // Fallback method - use cached balance
    private DebitResponse debitFallback(String accountId, DebitRequest request, Exception e) {
        log.error("Circuit breaker activated for currentAccountSystem", e);
        
        // Business logic: Try to use cached balance
        Balance cachedBalance = cacheService.getBalance(accountId);
        if (cachedBalance != null && cachedBalance.isValid()) {
            return DebitResponse.fromCache(cachedBalance);
        }
        
        // Last resort: Reject transaction
        return DebitResponse.failure("SERVICE_UNAVAILABLE", 
                                     "Current account system is temporarily unavailable");
    }
}
```

```yaml
# application.yml - Resilience4j configuration
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 60s
        failureRateThreshold: 50
        slowCallRateThreshold: 100
        slowCallDurationThreshold: 2s
    instances:
      currentAccountSystem:
        baseConfig: default
      savingsAccountSystem:
        baseConfig: default
      swiftSystem:
        slidingWindowSize: 20  # Higher threshold for SWIFT
        waitDurationInOpenState: 120s
  
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 500ms
        retryExceptions:
          - org.springframework.web.client.HttpServerErrorException
          - java.net.SocketTimeoutException
    instances:
      currentAccountSystem:
        baseConfig: default
      swiftSystem:
        maxAttempts: 2  # Lower retries for SWIFT (expensive)
  
  bulkhead:
    configs:
      default:
        maxConcurrentCalls: 25
    instances:
      currentAccountSystem:
        maxConcurrentCalls: 50  # Higher for current accounts
      swiftSystem:
        maxConcurrentCalls: 10  # Lower for SWIFT
  
  timeout:
    configs:
      default:
        timeoutDuration: 5s
    instances:
      currentAccountSystem:
        timeoutDuration: 3s
      swiftSystem:
        timeoutDuration: 10s  # Higher for international calls
```

---

## Decision Matrix

| Traffic Type | Pattern | Technology | When to Use | Example |
|--------------|---------|------------|-------------|---------|
| **EAST-WEST** (Internal) | Circuit Breaker, Retry, Timeout | **Istio** | Service-to-service within Kubernetes cluster | Payment Service → Validation Service |
| **NORTH-SOUTH** (External) | Circuit Breaker, Retry, Timeout, Bulkhead | **Resilience4j** | Calls to external APIs outside Kubernetes | Account Adapter → Core Banking System (on-premise) |
| **API Gateway** (Ingress) | Rate Limiting, Throttling | **Azure API Management** | External client requests entering the system | Mobile App → Azure API Management → Web BFF |
| **BFF Layer** (Partner API) | Rate Limiting (per partner) | **Resilience4j @RateLimiter** | Fine-grained rate limiting per partner/tenant | Partner BFF → 100 req/min per partner |

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           EXTERNAL WORLD                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐           │
│  │ Core Banking   │  │ SWIFT API      │  │ Fraud API      │           │
│  │ (On-Premise)   │  │ (External)     │  │ (External)     │           │
│  └────────┬───────┘  └────────┬───────┘  └────────┬───────┘           │
│           │                   │                   │                     │
│           │ ⚡ Resilience4j   │ ⚡ Resilience4j   │ ⚡ Resilience4j    │
│           │   Circuit Breaker │   Circuit Breaker │   Circuit Breaker  │
│           │   Retry           │   Retry           │   Retry            │
│           │   Timeout         │   Timeout         │   Timeout          │
│           │   Bulkhead        │   Bulkhead        │   Bulkhead         │
│           │                   │                   │                     │
└───────────┼───────────────────┼───────────────────┼─────────────────────┘
            │                   │                   │
            │                   │                   │
┌───────────┼───────────────────┼───────────────────┼─────────────────────┐
│           │     KUBERNETES CLUSTER (AKS)          │                     │
├───────────┼───────────────────┼───────────────────┼─────────────────────┤
│           ▼                   ▼                   ▼                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ Account Adapter │  │ SWIFT Adapter   │  │ Fraud Service   │        │
│  │ (Spring Boot)   │  │ (Spring Boot)   │  │ (Spring Boot)   │        │
│  ├─────────────────┤  ├─────────────────┤  ├─────────────────┤        │
│  │ Envoy Proxy    │  │ Envoy Proxy    │  │ Envoy Proxy    │        │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘        │
│           │                    │                    │                  │
│           │ ⚡ Istio           │ ⚡ Istio           │ ⚡ Istio         │
│           │   Circuit Breaker  │   Circuit Breaker  │   Circuit Breaker│
│           │   Retry            │   Retry            │   Retry          │
│           │   Timeout          │   Timeout          │   Timeout        │
│           │   mTLS             │   mTLS             │   mTLS           │
│           │                    │                    │                  │
│           ▼                    ▼                    ▼                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ Payment Service │  │ Validation Svc  │  │ Saga Orch. Svc  │        │
│  │ (Spring Boot)   │  │ (Spring Boot)   │  │ (Spring Boot)   │        │
│  ├─────────────────┤  ├─────────────────┤  ├─────────────────┤        │
│  │ Envoy Proxy    │  │ Envoy Proxy    │  │ Envoy Proxy    │        │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘        │
│                                                                         │
│  ✅ INTERNAL (EAST-WEST) = Istio handles resilience                    │
│  ✅ EXTERNAL (NORTH-SOUTH) = Resilience4j handles resilience           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Specific Service Guidance

### Services Using **ONLY Istio** (No Resilience4j)

These services only communicate with other services **inside** Kubernetes:

1. ✅ **Payment Initiation Service** - calls Validation, Routing (EAST-WEST)
2. ✅ **Validation Service** - calls Routing, Transaction Processing (EAST-WEST)
3. ✅ **Routing Service** - calls Clearing Adapters (EAST-WEST)
4. ✅ **Transaction Processing Service** - calls Account Adapter, Fraud (EAST-WEST)
5. ✅ **Saga Orchestrator Service** - calls multiple services (EAST-WEST)
6. ✅ **Tenant Management Service** - database only (no external APIs)
7. ✅ **IAM Service** - database + Azure AD B2C (managed by Azure SDK)
8. ✅ **Audit Service** - database only
9. ✅ **Reporting Service** - database + Azure Synapse (managed by Azure SDK)
10. ✅ **Settlement Service** - database only
11. ✅ **Reconciliation Service** - database only
12. ✅ **Internal API Gateway** - routes to internal services (EAST-WEST)
13. ✅ **Web BFF** - calls internal services (EAST-WEST)
14. ✅ **Mobile BFF** - calls internal services (EAST-WEST)
15. ✅ **Partner BFF** - calls internal services (EAST-WEST) + **Resilience4j @RateLimiter** (per-partner rate limiting)

**Action**: Remove `@CircuitBreaker`, `@Retry`, `@Timeout` annotations. Rely on Istio.

---

### Services Using **BOTH Istio + Resilience4j**

These services communicate with **BOTH** internal services (Istio) and external APIs (Resilience4j):

1. ✅ **Account Adapter Service**:
   - **Istio**: For calls TO Account Adapter from other services (EAST-WEST)
   - **Resilience4j**: For calls FROM Account Adapter to external core banking systems (NORTH-SOUTH)

2. ✅ **SAMOS Adapter**:
   - **Istio**: For calls TO SAMOS Adapter from Routing Service (EAST-WEST)
   - **Resilience4j**: For calls FROM SAMOS Adapter to external SAMOS RTGS API (NORTH-SOUTH)

3. ✅ **BankservAfrica Adapter**:
   - **Istio**: For calls TO BankservAfrica Adapter from Routing (EAST-WEST)
   - **Resilience4j**: For calls FROM BankservAfrica to external ACH API (NORTH-SOUTH)

4. ✅ **RTC Adapter**:
   - **Istio**: For calls TO RTC Adapter from Routing (EAST-WEST)
   - **Resilience4j**: For calls FROM RTC Adapter to external RTC API (NORTH-SOUTH)

5. ✅ **PayShap Adapter**:
   - **Istio**: For calls TO PayShap Adapter from Routing (EAST-WEST)
   - **Resilience4j**: For calls FROM PayShap to external PayShap P2P API (NORTH-SOUTH)

6. ✅ **SWIFT Adapter**:
   - **Istio**: For calls TO SWIFT Adapter from Routing (EAST-WEST)
   - **Resilience4j**: For calls FROM SWIFT to external SWIFT Alliance Lite2, Sanctions API, FX API (NORTH-SOUTH)

7. ✅ **Notification Service** (if using IBM MQ):
   - **Istio**: For calls TO Notification Service from other services (EAST-WEST)
   - **Resilience4j**: For calls FROM Notification to external IBM MQ broker (NORTH-SOUTH)

8. ✅ **Batch Processing Service** (if using SFTP to external systems):
   - **Istio**: For calls TO Batch Processing from other services (EAST-WEST)
   - **Resilience4j**: For SFTP connections to external file servers (NORTH-SOUTH)

**Action**: Keep BOTH. Use Istio for incoming traffic, Resilience4j for outgoing external calls.

---

## Configuration Strategy

### 1. Default Istio Policies (Apply to ALL 20 services)

```yaml
# istio-defaults.yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: default-circuit-breaker
  namespace: default
spec:
  host: "*.default.svc.cluster.local"  # Apply to all services in default namespace
  trafficPolicy:
    connectionPool:
      http:
        http1MaxPendingRequests: 100
        maxRequestsPerConnection: 2
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 60s
      maxEjectionPercent: 50

---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: default-retry
  namespace: default
spec:
  hosts:
    - "*.default.svc.cluster.local"
  http:
    - retries:
        attempts: 3
        perTryTimeout: 2s
        retryOn: 5xx,reset,connect-failure
```

### 2. Service-Specific Istio Overrides (For critical services)

```yaml
# payment-service-override.yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: payment-service-circuit-breaker
spec:
  host: payment-service.default.svc.cluster.local
  trafficPolicy:
    outlierDetection:
      consecutiveErrors: 10  # Higher threshold for critical service
      baseEjectionTime: 30s  # Shorter ejection time
```

### 3. Resilience4j Defaults (application.yml)

```yaml
# application-resilience-defaults.yml (shared across all services)
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 60s
        failureRateThreshold: 50
  
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 500ms
  
  timeout:
    configs:
      default:
        timeoutDuration: 5s
```

---

## Migration Path (Remove Redundant Code)

### Step 1: Identify Services with ONLY Internal Calls

Run this audit:
```bash
# Find services with @CircuitBreaker but NO external API calls
grep -r "@CircuitBreaker" services/*/src --include="*.java" | \
  xargs -I {} sh -c 'echo {} && grep -L "FeignClient\|RestTemplate" {}'
```

### Step 2: Remove Resilience4j from Internal-Only Services

For services like Payment Initiation, Validation, Routing:

**Before** (❌ Redundant):
```java
@Service
public class PaymentService {
    
    @Autowired
    private ValidationServiceClient validationClient;
    
    @CircuitBreaker(name = "validationService")  // ❌ Remove - Istio handles this
    @Retry(name = "validationService")           // ❌ Remove - Istio handles this
    public ValidationResponse validate(Payment payment) {
        return validationClient.validate(payment);
    }
}
```

**After** (✅ Clean):
```java
@Service
public class PaymentService {
    
    @Autowired
    private ValidationServiceClient validationClient;
    
    // ✅ No annotations - Istio handles circuit breaking, retries, timeouts
    public ValidationResponse validate(Payment payment) {
        return validationClient.validate(payment);
    }
}
```

### Step 3: Keep Resilience4j for External Calls ONLY

For Account Adapter, SWIFT Adapter, clearing adapters:

**Keep** (✅ Correct):
```java
@Service
public class CurrentAccountService {
    
    @Autowired
    private CurrentAccountClient currentAccountClient; // External API
    
    @CircuitBreaker(name = "currentAccountSystem", fallbackMethod = "debitFallback")
    @Retry(name = "currentAccountSystem")
    @Timeout(name = "currentAccountSystem")
    // ✅ Keep - This is an EXTERNAL call (NORTH-SOUTH)
    public DebitResponse debit(String accountId, DebitRequest request) {
        return currentAccountClient.debit(accountId, request);
    }
    
    private DebitResponse debitFallback(String accountId, DebitRequest request, Exception e) {
        return DebitResponse.fromCache(cacheService.getBalance(accountId));
    }
}
```

---

## Benefits of This Approach

### 1. **Clear Separation of Concerns**
- ✅ **Istio**: Infrastructure-level resilience (EAST-WEST)
- ✅ **Resilience4j**: Application-level resilience (NORTH-SOUTH)
- ✅ No overlap, no duplication

### 2. **Reduced Code Complexity**
- ✅ 15 services remove ALL resilience code (rely on Istio)
- ✅ 5 services keep resilience code ONLY for external calls
- ✅ ~40% less application code

### 3. **Centralized Configuration**
- ✅ Istio policies configured ONCE for all internal traffic
- ✅ Resilience4j configured per external API (fine-grained control)

### 4. **Better Observability**
- ✅ Istio metrics for ALL internal traffic (automatic)
- ✅ Resilience4j metrics for external API failures
- ✅ Clear separation in Grafana dashboards

### 5. **Easier Testing**
- ✅ Test Istio policies using Istio testing tools (e.g., `istioctl analyze`)
- ✅ Test Resilience4j using WireMock (external API mocks)

---

## Documentation Updates Required

### 1. Update Feature 1.3 (Account Adapter Service)
- ✅ Clarify: Use Resilience4j for external core banking calls ONLY
- ✅ Add note: Istio handles internal traffic to Account Adapter

### 2. Update Feature 5.1 (Service Mesh)
- ✅ Clarify: Istio handles EAST-WEST traffic ONLY
- ✅ Add decision matrix (Istio vs Resilience4j)

### 3. Update All Clearing Adapter Features (2.1-2.5)
- ✅ Clarify: Use Resilience4j for external clearing system calls
- ✅ Remove redundant circuit breaker mentions

### 4. Update Prompt Templates (docs/35-AI-AGENT-PROMPT-TEMPLATES.md)
- ✅ Add guidance: "Use Istio for internal, Resilience4j for external"
- ✅ Update code examples to remove redundant annotations

---

## Summary

| Pattern | Istio | Resilience4j |
|---------|-------|--------------|
| **Scope** | EAST-WEST (Internal) | NORTH-SOUTH (External) |
| **Traffic** | Service-to-service within K8s | External API calls outside K8s |
| **Configuration** | Declarative YAML | Annotation-based Java |
| **Examples** | Payment → Validation | Account Adapter → Core Banking |
| **Benefits** | Zero code, centralized, automatic | Fine-grained, fallback logic, caching |
| **Services Using** | ALL 20 services | 8 services (adapters) |

**Key Rule**: If the call goes **OUTSIDE Kubernetes**, use **Resilience4j**. Otherwise, use **Istio**.

---

**Status**: ✅ Architectural Decision Approved  
**Next Steps**: Update 4 documents (Account Adapter, Service Mesh, Clearing Adapters, Prompt Templates)

