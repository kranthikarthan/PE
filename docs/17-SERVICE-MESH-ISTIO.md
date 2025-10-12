# Service Mesh with Istio - Architecture Design

## Overview

This document provides the **Service Mesh architecture design** using Istio for the Payments Engine. A service mesh adds a dedicated infrastructure layer for service-to-service communication, providing traffic management, security, and observability without code changes.

---

## Why Service Mesh?

### Current Challenge (20 Microservices)

```
Payment Service → Account Service
- How to secure this call?
- How to retry on failure?
- How to route traffic (canary deployment)?
- How to monitor latency?

Answer: Each service implements its own:
- Circuit breaker code ❌
- Retry logic ❌
- Security (mTLS) ❌
- Metrics collection ❌

Problem: Code duplicated across 17 services!
```

### With Service Mesh

```
Payment Service → [Istio Sidecar Proxy] → [Istio Sidecar Proxy] → Account Service

Sidecar handles:
✅ Automatic mTLS (encryption + authentication)
✅ Circuit breaking (no code needed)
✅ Retries and timeouts (configured, not coded)
✅ Traffic routing (canary deployments)
✅ Metrics collection (automatic)
✅ Distributed tracing (integrated)

Result: Remove infrastructure code from services!
```

---

## Service Mesh Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────────┐
│                    KUBERNETES (AKS)                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Istio Control Plane                          │  │
│  │                                                            │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │  │
│  │  │   Pilot     │  │  Citadel    │  │   Galley    │      │  │
│  │  │ (Traffic    │  │ (Security   │  │ (Config)    │      │  │
│  │  │  Management)│  │  certs)     │  │             │      │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            │                                    │
│                            │ Configuration                      │
│                            ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Data Plane (Sidecar Proxies)                 │  │
│  │                                                            │  │
│  │  ┌─────────────────┐    ┌─────────────────┐              │  │
│  │  │  Payment Pod    │    │  Account Pod    │              │  │
│  │  ├─────────────────┤    ├─────────────────┤              │  │
│  │  │ Payment Service │    │ Account Service │              │  │
│  │  │                 │    │                 │              │  │
│  │  ├─────────────────┤    ├─────────────────┤              │  │
│  │  │ Envoy Proxy    │◄───┤ Envoy Proxy    │              │  │
│  │  │ (Sidecar)      │mTLS │ (Sidecar)      │              │  │
│  │  └─────────────────┘    └─────────────────┘              │  │
│  │                                                            │  │
│  │  ┌─────────────────┐    ┌─────────────────┐              │  │
│  │  │ Validation Pod  │    │   Saga Pod      │              │  │
│  │  ├─────────────────┤    ├─────────────────┤              │  │
│  │  │ Validation Svc  │    │ Saga Orch. Svc  │              │  │
│  │  ├─────────────────┤    ├─────────────────┤              │  │
│  │  │ Envoy Proxy    │◄───┤ Envoy Proxy    │              │  │
│  │  └─────────────────┘    └─────────────────┘              │  │
│  │                                                            │  │
│  │  ... (13 more services with sidecars)                     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Sidecar Pattern

```
┌────────────────────────────────────────┐
│         Kubernetes Pod                 │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │   Payment Service Container      │ │
│  │   (Your application)             │ │
│  │   Port: 8080                     │ │
│  └──────────────┬───────────────────┘ │
│                 │ localhost           │
│                 ▼                     │
│  ┌──────────────────────────────────┐ │
│  │   Envoy Proxy (Sidecar)          │ │
│  │   - Intercepts all traffic       │ │
│  │   - Applies policies             │ │
│  │   - Collects metrics             │ │
│  │   Port: 15001                    │ │
│  └──────────────┬───────────────────┘ │
│                 │                     │
└─────────────────┼─────────────────────┘
                  │
                  │ mTLS encrypted
                  ▼
        Other Service Sidecars
```

---

## Key Capabilities

### 1. Automatic mTLS (Mutual TLS)

**Without Istio**:
```java
// Each service implements TLS
@Configuration
public class SecurityConfig {
    @Bean
    public RestTemplate restTemplate() {
        SSLContext sslContext = ... // Configure TLS
        HttpClient client = HttpClients.custom()
            .setSSLContext(sslContext)
            .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
    }
}
// Repeated in ALL 17 services ❌
```

**With Istio**:
```yaml
# Single configuration for ALL services
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: payments
spec:
  mtls:
    mode: STRICT  # Enforce mTLS for all services

# Remove all TLS code from services ✅
```

**Result**:
- ✅ All service-to-service communication encrypted automatically
- ✅ Certificate rotation handled by Istio (no expired certs!)
- ✅ No code changes required
- ✅ Zero-trust security (mutual authentication)

### 2. Circuit Breaking

**Without Istio**:
```java
// Each service implements circuit breaker
@Service
public class AccountService {
    
    @CircuitBreaker(name = "account-service", fallbackMethod = "fallback")
    @Retry(name = "account-service")
    public AccountBalance getBalance(String accountNumber) {
        return restTemplate.getForObject(url, AccountBalance.class);
    }
    
    public AccountBalance fallback(Exception e) {
        return AccountBalance.unavailable();
    }
}
// Repeated in ALL services ❌
```

**With Istio**:
```yaml
# Declare circuit breaker policy (no code!)
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: account-service-circuit-breaker
spec:
  host: account-service
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 50
        http2MaxRequests: 100
        maxRequestsPerConnection: 2
    
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50
      minHealthPercent: 50

# Remove circuit breaker code ✅
```

**Result**:
- ✅ Circuit breaking applied declaratively
- ✅ No Resilience4j dependency needed
- ✅ Policy changed without code deployment
- ✅ Consistent across all services

### 3. Traffic Management (Canary Deployments)

**Scenario**: Deploy new version of Payment Service to 10% of traffic

**Without Istio**:
```
Deploy v2 alongside v1
Configure load balancer
Hope it works correctly
Complex rollback if issues

Complexity: HIGH ❌
```

**With Istio**:
```yaml
# Route 90% to v1, 10% to v2 (canary)
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: payment-service-canary
spec:
  hosts:
    - payment-service
  http:
    - match:
        - headers:
            x-tenant-id:
              exact: "STD-001"  # Standard Bank gets v2
      route:
        - destination:
            host: payment-service
            subset: v2
          weight: 100
    
    - route:
        - destination:
            host: payment-service
            subset: v1
          weight: 90
        - destination:
            host: payment-service
            subset: v2
          weight: 10

---
# Define versions
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: payment-service-versions
spec:
  host: payment-service
  subsets:
    - name: v1
      labels:
        version: v1
    - name: v2
      labels:
        version: v2
```

**Canary Deployment Flow**:
```
Day 1: 90% v1, 10% v2 (monitor metrics)
Day 2: 70% v1, 30% v2 (if stable)
Day 3: 50% v1, 50% v2
Day 4: 20% v1, 80% v2
Day 5: 0% v1, 100% v2 (complete migration)

If issues at any point: Instant rollback (change weights)
```

**Result**:
- ✅ Zero-downtime deployments
- ✅ Gradual rollout with monitoring
- ✅ Instant rollback capability
- ✅ Per-tenant routing (Standard Bank gets v2 first)

### 4. Fault Injection (Testing)

**Test Scenario**: How does system behave when Account Service is slow?

```yaml
# Inject 5-second delay for 50% of requests (testing resilience)
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: account-service-fault
spec:
  hosts:
    - account-service
  http:
    - fault:
        delay:
          percentage:
            value: 50.0
          fixedDelay: 5s
      route:
        - destination:
            host: account-service
```

**Use Cases**:
- ✅ Chaos engineering in production
- ✅ Test circuit breakers work correctly
- ✅ Validate timeout configurations
- ✅ Measure user experience under degradation

### 5. Observability (Automatic Metrics)

**Without Istio**:
```java
// Manual metrics collection
@Service
public class PaymentService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public Payment process(PaymentRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Payment payment = doProcess(request);
            sample.stop(Timer.builder("payment.process")
                .tag("status", "success")
                .register(meterRegistry));
            return payment;
        } catch (Exception e) {
            sample.stop(Timer.builder("payment.process")
                .tag("status", "failure")
                .register(meterRegistry));
            throw e;
        }
    }
}
// Repeated everywhere ❌
```

**With Istio**:
```yaml
# Automatic metrics for ALL services (no code!)
# Istio Envoy collects:
- Request rate (req/sec)
- Error rate (%)
- Latency (p50, p95, p99)
- Request size
- Response size
- Success/failure counts

# Available in Prometheus/Grafana automatically ✅
```

**Metrics Dashboard Example**:
```
Service: payment-service
├─ Request Rate: 1,250 req/sec
├─ Error Rate: 0.2%
├─ Latency p50: 45ms
├─ Latency p95: 120ms
├─ Latency p99: 250ms
├─ Success Rate: 99.8%
└─ Connections: 85 active

Service: account-adapter
├─ Request Rate: 2,100 req/sec
├─ Error Rate: 1.5% ⚠️ (investigating)
├─ Latency p95: 200ms
└─ Circuit Breaker: Open (5/5 errors)
```

---

## Istio Architecture Components

### Control Plane

```
┌────────────────────────────────────────────────────────┐
│              Istio Control Plane                        │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Pilot (istiod)                                 │   │
│  │  - Service discovery                            │   │
│  │  - Traffic management (VirtualService, etc.)    │   │
│  │  - Resilience (circuit breakers, retries)      │   │
│  │  - Pushes config to Envoy sidecars              │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Citadel (Certificate Authority)                │   │
│  │  - Issues certificates to services              │   │
│  │  - Rotates certificates automatically           │   │
│  │  - Enables mTLS                                 │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Galley                                         │   │
│  │  - Configuration validation                     │   │
│  │  - Configuration distribution                   │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

### Data Plane (Envoy Proxies)

```
┌────────────────────────────────────────────────────────┐
│         Envoy Proxy (Sidecar)                          │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Listeners                                      │   │
│  │  - Receive inbound/outbound traffic            │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Filters                                        │   │
│  │  - mTLS encryption/decryption                   │   │
│  │  - Load balancing                               │   │
│  │  - Circuit breaking                             │   │
│  │  - Retries                                      │   │
│  │  - Fault injection                              │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Telemetry                                      │   │
│  │  - Request metrics                              │   │
│  │  - Distributed tracing                          │   │
│  │  - Access logs                                  │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

---

## Deployment Strategy

### Phase 1: Install Istio (1 week)

```
Tasks:
1. Install Istio control plane on AKS
2. Create "payments" namespace with Istio injection enabled
3. Redeploy 1 service (e.g., Payment Service) to verify sidecar injection
4. Validate mTLS working
5. Create basic VirtualService and DestinationRule
6. Test traffic routing
```

### Phase 2: Rollout to All Services (1 week)

```
Rollout Strategy (gradual):
Day 1: 3 services (Payment, Account, Validation)
Day 2: 5 more services (Routing, Transaction, Saga, etc.)
Day 3: Remaining 9 services
Day 4-5: Monitoring, validation, optimization

Per service:
- Enable sidecar injection
- Redeploy
- Verify metrics appearing in Grafana
- Create DestinationRule for circuit breaker
- Test resilience
```

### Phase 3: Advanced Features (1 week)

```
Tasks:
1. Configure canary deployments for critical services
2. Set up fault injection for chaos testing
3. Fine-tune circuit breaker policies
4. Create Istio dashboards in Grafana
5. Document runbooks for common scenarios
```

---

## Multi-Tenancy with Istio

### Tenant-Specific Routing

```yaml
# Route Standard Bank traffic to dedicated pod pool
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: payment-service-tenant-routing
spec:
  hosts:
    - payment-service
  http:
    # Standard Bank → Dedicated pod pool (high capacity)
    - match:
        - headers:
            x-tenant-id:
              exact: "STD-001"
      route:
        - destination:
            host: payment-service
            subset: high-capacity
    
    # Nedbank → Dedicated pod pool
    - match:
        - headers:
            x-tenant-id:
              exact: "NED-001"
      route:
        - destination:
            host: payment-service
            subset: medium-capacity
    
    # Other tenants → Shared pool
    - route:
        - destination:
            host: payment-service
            subset: shared

---
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: payment-service-tenant-pools
spec:
  host: payment-service
  subsets:
    - name: high-capacity
      labels:
        tenant-tier: premium
    - name: medium-capacity
      labels:
        tenant-tier: standard
    - name: shared
      labels:
        tenant-tier: basic
```

### Tenant-Specific Rate Limiting

```yaml
# Rate limit per tenant
apiVersion: networking.istio.io/v1beta1
kind: EnvoyFilter
metadata:
  name: tenant-rate-limit
spec:
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit
            stat_prefix: http_local_rate_limiter
            token_bucket:
              max_tokens: 10000  # Max burst
              tokens_per_fill: 1000
              fill_interval: 1s  # 1000 requests per second
            filter_enabled:
              runtime_key: local_rate_limit_enabled
              default_value:
                numerator: 100
                denominator: HUNDRED
            filter_enforced:
              runtime_key: local_rate_limit_enforced
              default_value:
                numerator: 100
                denominator: HUNDRED
```

---

## Integration with Existing Architecture

### With OpenTelemetry (Distributed Tracing)

```
Istio Envoy → OpenTelemetry Collector → Jaeger

Benefits:
✅ Istio automatically creates spans for HTTP calls
✅ Integrates with your existing OpenTelemetry setup
✅ No code changes needed
✅ Enhanced trace detail (network-level metrics)
```

### With Azure Monitor

```
Istio Metrics → Prometheus → Azure Monitor

Integration Points:
- Istio metrics exported to Prometheus
- Azure Monitor scrapes Prometheus
- Unified dashboard in Azure Portal
- Alerts on Istio metrics
```

### With Multi-Tenancy

```
Tenant Context Propagation:
1. API Gateway adds X-Tenant-ID header
2. Istio propagates header through all services
3. VirtualService routes based on tenant
4. DestinationRule applies tenant-specific policies
5. Metrics tagged with tenant_id
```

---

## Security Benefits

### Zero-Trust Network

```
Traditional Model:
- Services trust each other (same network)
- Any compromised service can access all services

With Istio mTLS:
- Every service must authenticate (certificate)
- Encrypted communication
- Compromised service can't impersonate others
- Authorization policies per service
```

### Authorization Policies

```yaml
# Only Payment Service can call Account Service
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: account-service-authz
  namespace: payments
spec:
  selector:
    matchLabels:
      app: account-service
  rules:
    - from:
        - source:
            principals: ["cluster.local/ns/payments/sa/payment-service"]
      to:
        - operation:
            methods: ["GET", "POST"]
            paths: ["/api/v1/accounts/*"]

# Block all other access ✅
```

---

## Monitoring & Observability

### Service Graph

```
Istio provides visual service dependency graph:

                    ┌──────────┐
                    │ API GW   │
                    └────┬─────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
    ┌─────────┐    ┌─────────┐    ┌─────────┐
    │ Payment │    │ Account │    │ Notif.  │
    └────┬────┘    └────┬────┘    └─────────┘
         │              │
         ▼              ▼
    ┌─────────┐    ┌─────────┐
    │Validation│   │ Saga    │
    └─────────┘    └─────────┘

Metrics on each edge:
- Request rate
- Error rate
- Latency
```

### Kiali Dashboard

```
Kiali (Istio observability console):
- Service topology
- Traffic flow visualization
- Health status per service
- Configuration validation
- Distributed tracing integration

Real-time insights:
✅ Which services are communicating?
✅ What's the error rate between services?
✅ Where are the bottlenecks?
✅ Is mTLS enabled everywhere?
```

---

## Cost-Benefit Analysis

### Costs

| Cost Type | Impact |
|-----------|--------|
| **CPU Overhead** | Envoy sidecar adds 10-20% CPU per pod |
| **Memory Overhead** | +50-100MB per pod (sidecar) |
| **Complexity** | Learning curve for team |
| **Deployment Time** | Initial setup: 2-3 weeks |

**Total**: ~$500-800/month additional infrastructure cost (AKS)

### Benefits

| Benefit | Value |
|---------|-------|
| **Security** | mTLS for all 17 services (no code) |
| **Resilience** | Circuit breakers (remove Resilience4j code) |
| **Observability** | Automatic metrics (remove manual instrumentation) |
| **Deployment** | Canary deployments (zero-downtime) |
| **Development Time Saved** | ~2 weeks (no infrastructure code) |

**Total**: ~$10,000 value (developer time saved) + enhanced security/reliability

**ROI**: 10-15x return on investment

---

## Alternatives Considered

### Option 1: Istio ✅ (RECOMMENDED)

**Pros**:
- ✅ Most mature service mesh
- ✅ Azure support (via AKS)
- ✅ Large community
- ✅ Rich feature set

**Cons**:
- ⚠️ Complex (many CRDs)
- ⚠️ Resource intensive

### Option 2: Linkerd

**Pros**:
- ✅ Simpler than Istio
- ✅ Lower resource usage
- ✅ Fast startup

**Cons**:
- ⚠️ Less feature-rich
- ⚠️ Smaller community
- ⚠️ Less Azure integration

### Option 3: Consul (by HashiCorp)

**Pros**:
- ✅ Multi-cloud
- ✅ Service registry built-in

**Cons**:
- ⚠️ Less Kubernetes-native
- ⚠️ Different architecture

**Decision**: Istio (best fit for Azure AKS + Kubernetes-native)

---

## Implementation Checklist

### Pre-Production

- [ ] Istio control plane installed on AKS
- [ ] Namespace configured with sidecar injection
- [ ] mTLS enabled (STRICT mode)
- [ ] All 17 services redeployed with sidecars
- [ ] Circuit breaker policies created for critical services
- [ ] Canary deployment tested (1 service)
- [ ] Kiali dashboard accessible
- [ ] Grafana dashboards created for Istio metrics
- [ ] Team trained on Istio concepts
- [ ] Runbooks created for common scenarios

### Post-Production

- [ ] Monitor sidecar resource usage
- [ ] Optimize circuit breaker thresholds
- [ ] Implement fault injection for chaos testing
- [ ] Roll out canary deployments for all services
- [ ] Create tenant-specific routing policies
- [ ] Set up alerts for circuit breaker events
- [ ] Regular Istio version upgrades (quarterly)

---

## Summary

### What Service Mesh Provides

✅ **Security**: Automatic mTLS for all 17 services  
✅ **Resilience**: Circuit breakers without code  
✅ **Traffic Management**: Canary deployments, A/B testing  
✅ **Observability**: Automatic metrics, service graph  
✅ **Multi-Tenancy**: Tenant-specific routing and rate limiting  

### Implementation Effort

**Total**: 2-3 weeks
- Week 1: Install Istio, configure 3 pilot services
- Week 2: Roll out to all 17 services
- Week 3: Advanced features, monitoring, documentation

### Result

**Remove infrastructure code from 17 services**, replacing it with declarative policies. Enhanced security (mTLS), resilience (circuit breakers), and observability (automatic metrics) with **zero code changes**.

---

## Related Documents

- **[13-MODERN-ARCHITECTURE-PATTERNS.md](13-MODERN-ARCHITECTURE-PATTERNS.md)** - Service Mesh overview
- **[16-DISTRIBUTED-TRACING.md](16-DISTRIBUTED-TRACING.md)** - Integration with tracing
- **[07-AZURE-INFRASTRUCTURE.md](07-AZURE-INFRASTRUCTURE.md)** - AKS infrastructure

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Phase**: 2 (Production Hardening)
