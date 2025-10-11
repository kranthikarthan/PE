# Reactive Architecture - Design Document

## Overview

This document provides the **Reactive Architecture design** for high-throughput services in the Payments Engine. Reactive programming enables non-blocking, asynchronous processing with built-in backpressure, dramatically improving throughput and resource utilization.

---

## Why Reactive Architecture?

### Current Architecture (Thread-per-Request)

```
Traditional Spring MVC:

Request arrives
  ↓
Assign thread from pool (200 threads)
  ↓
Thread BLOCKS waiting for:
  - Database query (50ms)
  - External API call (100ms)
  - Kafka publish (5ms)
  ↓
Thread held for 155ms doing nothing! ❌
  ↓
Response returned, thread released

Problem:
- Limited by thread pool size (200 threads)
- Threads waste time blocking
- At 200 concurrent requests → new requests wait
- Max throughput: ~5,000 req/sec
```

### Reactive Architecture (Event-Loop)

```
Reactive (Spring WebFlux):

Request arrives
  ↓
Event-loop thread handles request (NO BLOCKING)
  ↓
Initiate database query (async) → returns immediately
  ↓
Thread handles other requests while waiting
  ↓
Database response arrives → callback executes
  ↓
Initiate API call (async) → returns immediately
  ↓
Thread handles other requests
  ↓
API response arrives → callback executes
  ↓
Response returned

Benefits:
- Event-loop threads never block
- 8-16 threads handle thousands of requests
- Max throughput: ~50,000 req/sec ✅
- 10x improvement!
```

---

## Architecture Comparison

### Thread-per-Request vs Event-Loop

```
┌─────────────────────────────────────────────────────────────┐
│          Thread-per-Request (Spring MVC)                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Request 1 → Thread 1 ■■■■■■■■■■ (blocked waiting)         │
│  Request 2 → Thread 2 ■■■■■■■■■■ (blocked waiting)         │
│  Request 3 → Thread 3 ■■■■■■■■■■ (blocked waiting)         │
│  ...                                                         │
│  Request 200 → Thread 200 ■■■■■■■■■■ (blocked)             │
│  Request 201 → WAIT (no threads available) ❌               │
│                                                              │
│  Threads: 200                                                │
│  Utilization: 30% (70% spent blocking)                      │
│  Max throughput: 5,000 req/sec                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│             Event-Loop (Spring WebFlux)                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Request 1 → Event Loop ████ (processing)                   │
│  Request 2 → Event Loop ████ (processing)                   │
│  Request 3 → Event Loop ████ (processing)                   │
│  ...                                                         │
│  Request 1000 → Event Loop ████ (processing)                │
│  Request 1001 → Event Loop ████ (processing) ✅             │
│                                                              │
│  (Requests 1-1000 handled by same 8 threads!)               │
│                                                              │
│  Threads: 8                                                  │
│  Utilization: 95% (no blocking)                             │
│  Max throughput: 50,000 req/sec                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Which Services Should Be Reactive?

### Decision Matrix

| Service | Current Load | I/O Bound? | Recommended | Priority |
|---------|--------------|------------|-------------|----------|
| **Payment Initiation** | 10K req/sec | ✅ Yes (DB, events) | ✅ **Reactive** | **HIGH** |
| **Validation** | 10K req/sec | ✅ Yes (DB, fraud API, limits) | ✅ **Reactive** | **HIGH** |
| **Account Adapter** | 15K req/sec | ✅ Yes (external APIs) | ✅ **Reactive** | **HIGH** |
| **Notification** | 20K req/sec | ✅ Yes (DB, external APIs) | ✅ **Reactive** | **MEDIUM** |
| Web BFF | 5K req/sec | ✅ Yes (aggregates) | ✅ **Reactive** | **MEDIUM** |
| Mobile BFF | 8K req/sec | ✅ Yes (aggregates) | ✅ **Reactive** | **MEDIUM** |
| **Saga Orchestrator** | 2K req/sec | ❌ No (complex state) | ❌ **Keep MVC** | **N/A** |
| **Reporting** | Batch | ❌ No (batch processing) | ❌ **Keep MVC** | **N/A** |
| **Tenant Management** | 100 req/sec | ❌ No (low volume) | ❌ **Keep MVC** | **N/A** |

### Selection Criteria

**Use Reactive For**:
- ✅ High throughput (> 5K req/sec)
- ✅ I/O-bound operations (DB, external APIs)
- ✅ Services with many external calls
- ✅ Services that need to scale under load

**Keep Traditional For**:
- ✅ Complex business logic (easier to code imperatively)
- ✅ Batch processing (no concurrency benefit)
- ✅ Low-volume services (< 1K req/sec)
- ✅ State machines (Saga orchestrator)

---

## Reactive Architecture Design

### Technology Stack

```
┌─────────────────────────────────────────────────────────────┐
│                  Reactive Stack                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Spring WebFlux (Web Framework)                    │     │
│  │  - Reactive HTTP server (Netty)                    │     │
│  │  - Non-blocking request handling                   │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Project Reactor (Reactive Streams)                │     │
│  │  - Mono<T>: 0..1 element                          │     │
│  │  - Flux<T>: 0..N elements                         │     │
│  │  - Operators: map, flatMap, filter, etc.          │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │  R2DBC (Reactive Database Driver)                  │     │
│  │  - Non-blocking PostgreSQL driver                  │     │
│  │  - Reactive repository support                     │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Reactive Kafka Client                             │     │
│  │  - Non-blocking Kafka producer/consumer            │     │
│  │  - Backpressure support                            │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │  WebClient (HTTP Client)                           │     │
│  │  - Non-blocking HTTP calls                         │     │
│  │  - Replaces RestTemplate                           │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Reactive Data Flow

```
┌───────────────────────────────────────────────────────────┐
│       Payment Initiation (Reactive)                        │
├───────────────────────────────────────────────────────────┤
│                                                            │
│  HTTP Request                                              │
│       │                                                    │
│       ▼                                                    │
│  ┌─────────────────┐                                      │
│  │   Controller    │  Returns Mono<PaymentResponse>       │
│  │  (WebFlux)      │  (immediately, no blocking)          │
│  └────────┬────────┘                                      │
│           │                                                │
│           ▼                                                │
│  ┌─────────────────┐                                      │
│  │    Service      │  Chains reactive operations          │
│  │   (Reactive)    │  (all async)                        │
│  └────────┬────────┘                                      │
│           │                                                │
│           ▼                                                │
│  ┌─────────────────┐                                      │
│  │  Repository     │  Mono<Payment> (R2DBC)              │
│  │   (R2DBC)       │  Non-blocking DB access              │
│  └────────┬────────┘                                      │
│           │                                                │
│           ▼                                                │
│  ┌─────────────────┐                                      │
│  │ External API    │  Mono<ValidationResult>             │
│  │  (WebClient)    │  Non-blocking HTTP                   │
│  └────────┬────────┘                                      │
│           │                                                │
│           ▼                                                │
│  ┌─────────────────┐                                      │
│  │  Kafka Publish  │  Mono<Void>                         │
│  │   (Reactive)    │  Non-blocking                        │
│  └────────┬────────┘                                      │
│           │                                                │
│           ▼                                                │
│  HTTP Response                                             │
│  (when all operations complete)                           │
│                                                            │
│  Thread was NEVER blocked! ✅                              │
│  Thread handled other requests while waiting              │
└───────────────────────────────────────────────────────────┘
```

---

## Reactive Patterns

### Pattern 1: Sequential Operations

**Scenario**: Payment → Validate → Save

```
Reactive Chain:

Mono.just(request)
    .flatMap(req -> validate(req))      // Async validation
    .flatMap(valid -> savePayment(valid)) // Async save
    .flatMap(payment -> publishEvent(payment)) // Async publish
    .map(payment -> toResponse(payment))  // Transform

Operations execute sequentially but NON-BLOCKING
Thread handles other requests while waiting for each step
```

### Pattern 2: Parallel Operations

**Scenario**: Fetch payment summary + accounts + notifications (dashboard)

```
Reactive Parallel:

Mono.zip(
    getPaymentSummary(customerId),     // Executes in parallel
    getAccounts(customerId),            // Executes in parallel
    getNotifications(customerId)        // Executes in parallel
)
.map(tuple -> Dashboard.builder()
    .summary(tuple.getT1())
    .accounts(tuple.getT2())
    .notifications(tuple.getT3())
    .build()
)

All 3 operations execute concurrently
Result returned when ALL complete
Much faster than sequential (100ms vs 300ms)
```

### Pattern 3: Backpressure (Slow Consumer)

```
Scenario: 1000 events/sec published, consumer processes 100 events/sec

Without Backpressure:
- Producer overwhelms consumer
- OutOfMemoryError ❌

With Reactive Backpressure:
- Consumer signals: "I can handle 10 events"
- Producer sends 10 events
- Consumer processes them
- Consumer signals: "Ready for 10 more"
- Producer sends next 10
- Flow controlled automatically ✅
```

---

## Performance Characteristics

### Throughput Comparison

| Metric | Traditional (MVC) | Reactive (WebFlux) | Improvement |
|--------|------------------|-------------------|-------------|
| **Max RPS** | 5,000 | 50,000 | **10x** |
| **Threads** | 200 | 8-16 | **12x fewer** |
| **Memory** | 2 GB | 500 MB | **4x less** |
| **Latency (p50)** | 50ms | 45ms | 10% faster |
| **Latency (p95)** | 200ms | 80ms | **2.5x faster** |
| **CPU Utilization** | 30% | 85% | **Better** |

### Resource Utilization

```
Traditional (200 threads):
- Each thread: 1MB stack = 200MB
- Context switching overhead
- Thread creation/destruction cost
- Poor CPU utilization (blocked threads)

Reactive (8 threads):
- Event-loop threads: 8MB total
- No context switching
- Threads always busy (no blocking)
- Excellent CPU utilization (85-95%)

Savings: ~95% memory, ~50% CPU for same throughput
```

---

## Migration Strategy

### Phase 1: Pilot Service (Week 1-2)

```
Service: Payment Initiation (highest volume)

Tasks:
1. Create new reactive module (payment-service-reactive)
2. Convert controller to WebFlux
3. Convert repository to R2DBC
4. Convert external calls to WebClient
5. Convert Kafka to reactive
6. Load test (compare vs MVC)
7. Deploy canary (10% traffic)
8. Monitor metrics
9. Full rollout if successful

Success Criteria:
- 2x throughput improvement
- Reduced latency under load
- No errors/issues
```

### Phase 2: High-Volume Services (Week 3-4)

```
Services: Validation, Account Adapter, Notification

Per service:
- Apply learnings from Payment Service
- Parallel conversion (3 teams)
- Canary deployment
- Gradual rollout

Expected: 1 week per service (parallel)
```

### Phase 3: Optional Services (Future)

```
Services: Web BFF, Mobile BFF

Convert when:
- Traffic increases (> 5K req/sec)
- Performance issues observed
- Team comfortable with reactive

Keep as MVC:
- Saga Orchestrator (complex state)
- Reporting (batch processing)
- Tenant Management (low volume)
```

---

## Challenges & Solutions

### Challenge 1: Learning Curve

**Problem**: Reactive programming is harder to learn than imperative

**Solution**:
- ✅ Training sessions (2 days)
- ✅ Code review guidelines
- ✅ Pair programming for first service
- ✅ Start with simple services
- ✅ Comprehensive documentation

### Challenge 2: Debugging Complexity

**Problem**: Stack traces are harder to read in reactive code

**Solution**:
- ✅ Reactor Hooks (better stack traces in dev)
- ✅ Comprehensive logging at each step
- ✅ Distributed tracing (OpenTelemetry)
- ✅ Reactor debug mode in development

### Challenge 3: Blocking Operations

**Problem**: Accidentally blocking in reactive code kills performance

**Solution**:
- ✅ BlockHound (detects blocking at runtime)
- ✅ Code review checklist
- ✅ Wrap blocking calls in elastic scheduler
- ✅ Use reactive drivers (R2DBC, WebClient)

### Challenge 4: Testing

**Problem**: Testing reactive code requires different approach

**Solution**:
- ✅ StepVerifier (test Mono/Flux)
- ✅ WebTestClient (test controllers)
- ✅ Testcontainers (integration tests)
- ✅ Load testing (JMeter/Gatling)

---

## Architecture Decision Records

### ADR 1: Selective Reactive (Not Full Reactive)

**Decision**: Convert only high-throughput services to reactive

**Rationale**:
- ✅ Maximize benefit (10x throughput where needed)
- ✅ Minimize complexity (keep simple services simple)
- ✅ Phased adoption (less risk)
- ✅ Team learning curve (gradual)

**Alternative Rejected**: Full reactive (all 17 services)
- ❌ Unnecessary complexity for low-volume services
- ❌ Longer implementation time
- ❌ Steeper learning curve

### ADR 2: Project Reactor (Not RxJava)

**Decision**: Use Project Reactor

**Rationale**:
- ✅ Native Spring WebFlux integration
- ✅ Better performance (benchmarks)
- ✅ Active development (VMware)
- ✅ Reactive Streams compliant

**Alternative Rejected**: RxJava
- ❌ Less Spring integration
- ❌ Android-focused

### ADR 3: R2DBC (Not JDBC)

**Decision**: Use R2DBC for reactive database access

**Rationale**:
- ✅ Non-blocking PostgreSQL driver
- ✅ Reactive repository support
- ✅ No blocking in reactive chain

**Alternative Rejected**: JDBC + subscribeOn(elastic)
- ❌ Still blocks threads (wrapped blocking)
- ❌ Not truly reactive
- ❌ Resource pool limits

---

## Integration with Existing Architecture

### With Domain-Driven Design

```
Reactive doesn't change domain model:

@Entity
public class Payment {  // Same aggregate
    private PaymentId id;
    private Money amount;
    
    public void validate(ValidationResult result) {
        // Same business logic
    }
}

Repository changes:
Traditional: PaymentRepository extends JpaRepository
Reactive:    PaymentRepository extends ReactiveCrudRepository

Return type changes:
Traditional: Payment save(Payment payment)
Reactive:    Mono<Payment> save(Payment payment)
```

### With Multi-Tenancy

```
Tenant context propagation in reactive:

Use Reactor Context (thread-local replacement):

Mono.deferContextual(ctx -> {
    String tenantId = ctx.get("tenantId");
    return paymentService.process(paymentId, tenantId);
})
.contextWrite(Context.of("tenantId", getTenantFromHeader()));

Context flows with reactive chain ✅
```

### With Distributed Tracing

```
OpenTelemetry works with reactive:

@WithSpan("payment.process")
public Mono<Payment> process(PaymentRequest request) {
    return Mono.just(request)
        .flatMap(this::validate)
        .doOnNext(p -> Span.current().addEvent("validated"))
        .flatMap(this::save);
}

Trace context propagates automatically ✅
```

---

## Monitoring & Metrics

### Reactive-Specific Metrics

```
Metrics to Monitor:

1. Event Loop Utilization
   - Should be 80-95%
   - If < 50%: Not enough load
   - If > 98%: Need more workers

2. Pending Tasks
   - Tasks waiting to execute
   - Should be < 100
   - If > 1000: Backpressure issue

3. Buffer Usage
   - Reactive buffers (default: 256)
   - If full: Slow consumer issue

4. Subscription Count
   - Active reactive subscriptions
   - Monitor for leaks

5. Error Rate
   - Errors in reactive chains
   - Should be < 0.1%
```

### Performance Dashboards

```
Reactive Service Dashboard:

┌─────────────────────────────────────────────────┐
│  Payment Service (Reactive)                     │
├─────────────────────────────────────────────────┤
│  Throughput:        45,000 req/sec              │
│  Latency p95:       85ms                        │
│  Event Loop Util:   87%  ✅                     │
│  Threads:           8                           │
│  Memory:            450 MB                      │
│  CPU:               75%                         │
│  Error Rate:        0.05%                       │
├─────────────────────────────────────────────────┤
│  Compared to Traditional:                       │
│  Throughput:        9x improvement ✅           │
│  Latency:           2.5x faster ✅              │
│  Memory:            4x less ✅                  │
│  Threads:           25x fewer ✅                │
└─────────────────────────────────────────────────┘
```

---

## Cost-Benefit Analysis

### Costs

| Cost Type | Impact |
|-----------|--------|
| **Development Time** | 3-4 weeks (conversion) |
| **Learning Curve** | 2-3 weeks (team training) |
| **Testing Time** | 1 week (load testing) |
| **Code Complexity** | Higher (reactive paradigm) |

**Total**: ~$40,000 (developer time)

### Benefits

| Benefit | Value |
|---------|-------|
| **Infrastructure Savings** | $2,000/month (fewer pods needed) |
| **Performance** | 10x throughput (handle growth) |
| **Scalability** | Support 500K req/sec (vs 50K) |
| **User Experience** | 2.5x faster under load |

**Total**: $24,000/year savings + better UX

**ROI**: 8-10x return (savings + growth enablement)

---

## Implementation Checklist

### Pre-Implementation

- [ ] Team training completed (2 days)
- [ ] Pilot service selected (Payment Initiation)
- [ ] Development environment set up (R2DBC, WebFlux)
- [ ] Load testing tools ready (Gatling)
- [ ] Monitoring dashboards created
- [ ] BlockHound enabled (detect blocking)
- [ ] Code review guidelines updated

### Per Service Conversion

- [ ] Controller converted to WebFlux
- [ ] Repository converted to R2DBC
- [ ] External calls converted to WebClient
- [ ] Kafka converted to reactive
- [ ] Unit tests updated (StepVerifier)
- [ ] Integration tests passing
- [ ] Load testing completed (2x throughput)
- [ ] No blocking detected (BlockHound)
- [ ] Canary deployment (10% traffic)
- [ ] Metrics validated
- [ ] Full rollout

### Post-Implementation

- [ ] Monitor event loop utilization
- [ ] Monitor error rates
- [ ] Optimize buffer sizes
- [ ] Document lessons learned
- [ ] Train team on patterns discovered
- [ ] Plan next service conversion

---

## Summary

### What Reactive Provides

✅ **10x Throughput**: 50K req/sec vs 5K req/sec  
✅ **2.5x Faster Latency**: p95 85ms vs 200ms under load  
✅ **4x Less Memory**: 500MB vs 2GB  
✅ **12x Fewer Threads**: 8 vs 200  
✅ **Backpressure**: Built-in flow control  

### When to Use

**YES** for:
- High-throughput services (> 5K req/sec)
- I/O-bound operations (DB, APIs)
- Services that need to scale

**NO** for:
- Complex state machines
- Batch processing
- Low-volume services (< 1K req/sec)

### Implementation Effort

**Total**: 3-4 weeks
- Week 1: Training + pilot service selection
- Week 2: Convert Payment Initiation (pilot)
- Week 3-4: Convert 3 more services (parallel)

### Result

**Handle 10x more traffic** with same infrastructure. Enable future growth to **500K req/sec** without major architecture changes. **$24K/year infrastructure savings**.

---

## Related Documents

- **[13-MODERN-ARCHITECTURE-PATTERNS.md](13-MODERN-ARCHITECTURE-PATTERNS.md)** - Reactive overview
- **[14-DDD-IMPLEMENTATION.md](14-DDD-IMPLEMENTATION.md)** - Domain model (unchanged)
- **[16-DISTRIBUTED-TRACING.md](16-DISTRIBUTED-TRACING.md)** - Tracing integration

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Phase**: 2 (Production Hardening)
