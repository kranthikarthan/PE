# React Frontend - Operations Team Feature Analysis

## Executive Summary

**Critical Finding**: The current Web BFF (Feature 4.5) is designed for **general payment operations** but is **NOT aligned** with the specific needs of an **Operations/Admin team** for service management, payment repair, and transaction enquiries.

**Purpose**: Operations dashboard for internal ops team to:
- ✅ Manage all 20 microservices
- ✅ Repair failed payments
- ✅ Enquire transactions
- ❌ **NOT client-facing** (no customer payment initiation)

**Status**: ⚠️ **MISALIGNMENT DETECTED** - Requires new features

---

## Table of Contents

1. [Current Backend Services](#current-backend-services)
2. [Current Web BFF Design](#current-web-bff-design)
3. [Operations Team Requirements](#operations-team-requirements)
4. [Gap Analysis](#gap-analysis)
5. [Recommended Architecture](#recommended-architecture)
6. [New API Requirements](#new-api-requirements)
7. [React Frontend Features](#react-frontend-features)
8. [Implementation Plan](#implementation-plan)

---

## 1. Current Backend Services

### Available Microservices (20 services)

| # | Service | Operations-Relevant APIs |
|---|---------|-------------------------|
| 1 | **Payment Initiation** | ✅ Query payments, ❌ No repair APIs |
| 2 | **Validation Service** | ❌ No admin APIs |
| 3 | **Account Adapter** | ❌ No monitoring APIs |
| 4 | **Routing Service** | ❌ No admin APIs |
| 5 | **Transaction Processing** | ⚠️ Limited query APIs |
| 6 | **Saga Orchestrator** | ⚠️ Limited saga status APIs |
| 7 | **SAMOS Adapter** | ❌ No clearing status APIs |
| 8 | **BankservAfrica Adapter** | ❌ No clearing status APIs |
| 9 | **RTC Adapter** | ❌ No clearing status APIs |
| 10 | **PayShap Adapter** | ❌ No clearing status APIs |
| 11 | **SWIFT Adapter** | ❌ No clearing status APIs |
| 12 | **Settlement Service** | ⚠️ Reports only, no admin |
| 13 | **Reconciliation Service** | ⚠️ No manual match APIs |
| 14 | **Notification Service** | ❌ No admin APIs |
| 15 | **Reporting Service** | ✅ Has report APIs |
| 16 | **Batch Processing** | ⚠️ Limited job monitoring |
| 17 | **Tenant Management** | ✅ Has admin APIs |
| 18 | **IAM Service** | ✅ Has user management |
| 19 | **Audit Service** | ⚠️ Query only, no dashboard |
| 20 | **Internal API Gateway** | ❌ No monitoring APIs |

**Summary**: 
- ✅ **Available for Ops**: 3 services (15%)
- ⚠️ **Partial**: 5 services (25%)
- ❌ **Missing Ops APIs**: 12 services (60%)

---

## 2. Current Web BFF Design

### Existing GraphQL Schema (Feature 4.5)

```graphql
type Payment {
  id: ID!
  amount: Float!
  currency: String!
  status: PaymentStatus!
  debtorAccount: Account!
  creditorAccount: Account!
  createdAt: DateTime!
}

type Query {
  payment(id: ID!): Payment
  payments(filter: PaymentFilter, page: Int, size: Int): PaymentPage
  account(id: ID!): Account
}

type Mutation {
  createPayment(input: CreatePaymentInput!): Payment
  cancelPayment(id: ID!): Payment
}
```

### Limitations for Ops Team

❌ **Missing**:
- No service health monitoring
- No payment repair mutations
- No saga management
- No reconciliation queries
- No batch job monitoring
- No feature flag management
- No clearing system status
- No audit trail viewing
- No manual interventions
- No alert management

---

## 3. Operations Team Requirements

### 3.1 Service Management

**Requirement**: Monitor and manage all 20 microservices

**Needed Features**:
```yaml
Service Health Dashboard:
  - Service status (UP, DOWN, DEGRADED)
  - Circuit breaker state (OPEN, HALF_OPEN, CLOSED)
  - Request rate (TPS)
  - Error rate (%)
  - Response time (p50, p95, p99)
  - Pod count and health
  - Recent errors (last 100)

Circuit Breaker Management:
  - View circuit breaker state per service
  - Manually open/close circuit breakers
  - View failure metrics
  - Configure thresholds

Feature Flag Management:
  - List all feature flags
  - Toggle flags (on/off)
  - View flag status per tenant
  - Rollout percentage adjustment

Kubernetes Management:
  - Pod list and status
  - Restart pods
  - View logs (last 1000 lines)
  - Scale services (HPA)
```

**Backend APIs Required**:
```java
// NEW: Operations Management Service
GET    /api/ops/v1/services                      // All service health
GET    /api/ops/v1/services/{service}/health     // Service health
GET    /api/ops/v1/services/{service}/metrics    // Service metrics
GET    /api/ops/v1/services/{service}/errors     // Recent errors
POST   /api/ops/v1/services/{service}/restart    // Restart service

GET    /api/ops/v1/circuit-breakers              // All circuit breakers
GET    /api/ops/v1/circuit-breakers/{service}    // Service CB state
POST   /api/ops/v1/circuit-breakers/{service}/open   // Manually open
POST   /api/ops/v1/circuit-breakers/{service}/close  // Manually close

GET    /api/ops/v1/feature-flags                 // All flags
PUT    /api/ops/v1/feature-flags/{flag}/toggle   // Toggle flag
PUT    /api/ops/v1/feature-flags/{flag}/rollout  // Adjust rollout %
```

---

### 3.2 Payment Repair

**Requirement**: Fix failed payments, retry transactions, manual compensation

**Needed Features**:
```yaml
Failed Payment Dashboard:
  - List failed payments (last 24h, 7d, 30d)
  - Filter by failure reason (validation, fraud, limit, clearing, timeout)
  - Search by payment ID, account, amount
  - Bulk operations (retry all, cancel all)

Payment Repair Actions:
  - Retry payment (resubmit to clearing)
  - Manual compensation (reverse transaction)
  - Update payment status (FAILED → PENDING)
  - Override validation (skip limits, fraud)
  - Reprocess saga (restart from failed step)
  - Manual settlement (force settle)

Saga Management:
  - List pending sagas (stuck > 1 hour)
  - View saga state machine (current state)
  - View saga history (all state transitions)
  - Manual saga compensation
  - Resume saga from checkpoint

Clearing System Retry:
  - View clearing submission status
  - Retry clearing submission (SAMOS, Bankserv, etc.)
  - Manual acknowledgment (if clearing response lost)
  - View clearing errors
```

**Backend APIs Required**:
```java
// Payment Repair APIs (NEW endpoints in Payment Initiation Service)
GET    /api/ops/v1/payments/failed               // List failed payments
POST   /api/ops/v1/payments/{id}/retry           // Retry payment
POST   /api/ops/v1/payments/{id}/compensate      // Manual compensation
PUT    /api/ops/v1/payments/{id}/status          // Update status
POST   /api/ops/v1/payments/{id}/override        // Override validation

// Saga Management APIs (NEW endpoints in Saga Orchestrator)
GET    /api/ops/v1/sagas/pending                 // List pending sagas
GET    /api/ops/v1/sagas/{sagaId}                // Saga details
GET    /api/ops/v1/sagas/{sagaId}/history        // Saga state history
POST   /api/ops/v1/sagas/{sagaId}/compensate     // Manual compensation
POST   /api/ops/v1/sagas/{sagaId}/resume         // Resume saga

// Clearing System APIs (NEW endpoints in Clearing Adapters)
GET    /api/ops/v1/clearing/{system}/status      // Clearing system status
GET    /api/ops/v1/clearing/{system}/pending     // Pending submissions
POST   /api/ops/v1/clearing/{system}/{id}/retry  // Retry submission
POST   /api/ops/v1/clearing/{system}/{id}/ack    // Manual ACK
```

---

### 3.3 Transaction Enquiries

**Requirement**: Search and view transaction details with full audit trail

**Needed Features**:
```yaml
Advanced Search:
  - Search by payment ID
  - Search by account number (debtor or creditor)
  - Search by amount (exact, range)
  - Search by status (INITIATED, COMPLETED, FAILED, etc.)
  - Search by date range
  - Search by tenant ID
  - Search by clearing system (SAMOS, Bankserv, RTC, etc.)
  - Search by reference number
  - Full-text search (customer name, description)

Transaction Detail View:
  - Payment metadata (ID, amount, currency, accounts)
  - Current status and timestamp
  - Saga state (if applicable)
  - Validation results
  - Fraud score
  - Limit check results
  - Clearing submission status
  - Settlement status
  - All events (timeline view)
  - Audit trail (who did what when)

Audit Trail:
  - User actions (created, updated, retried)
  - System events (validated, routed, submitted, acknowledged)
  - State transitions (INITIATED → VALIDATED → DEBITED → etc.)
  - Timestamp and actor (user or system)
  - Related correlation ID

Event History:
  - All events for a payment (PaymentInitiatedEvent, PaymentValidatedEvent, etc.)
  - Event payload (JSON)
  - Event timestamp
  - Event source service
```

**Backend APIs Required**:
```java
// Advanced Search APIs (NEW endpoints in Reporting Service)
POST   /api/ops/v1/transactions/search           // Advanced search
GET    /api/ops/v1/transactions/{id}/detail      // Full transaction detail
GET    /api/ops/v1/transactions/{id}/audit       // Audit trail
GET    /api/ops/v1/transactions/{id}/events      // Event history
GET    /api/ops/v1/transactions/{id}/saga        // Saga details (if saga payment)

// Export APIs
POST   /api/ops/v1/transactions/export/csv       // Export to CSV
POST   /api/ops/v1/transactions/export/excel     // Export to Excel
```

---

### 3.4 Reconciliation

**Requirement**: View unmatched payments and manually reconcile

**Needed Features**:
```yaml
Reconciliation Dashboard:
  - Unmatched payments (no clearing response)
  - Unmatched clearing responses (no payment)
  - Aging report (payments pending > 24h, 48h, 7d)
  - Manual matching interface

Manual Matching:
  - Search unmatched payments
  - Search unmatched clearing responses
  - Drag-and-drop matching
  - Match confirmation
  - Bulk matching (CSV upload)

Settlement Reports:
  - Daily settlement report
  - Net positions per bank
  - Settlement instructions
  - Export to PDF/Excel
```

**Backend APIs Required**:
```java
// Reconciliation APIs (NEW endpoints in Reconciliation Service)
GET    /api/ops/v1/reconciliation/unmatched/payments        // Unmatched payments
GET    /api/ops/v1/reconciliation/unmatched/responses       // Unmatched responses
GET    /api/ops/v1/reconciliation/aging                     // Aging report
POST   /api/ops/v1/reconciliation/match                     // Manual match
POST   /api/ops/v1/reconciliation/bulk-match                // Bulk match (CSV)

// Settlement APIs (NEW endpoints in Settlement Service)
GET    /api/ops/v1/settlement/daily/{date}                  // Daily report
GET    /api/ops/v1/settlement/net-positions/{date}          // Net positions
GET    /api/ops/v1/settlement/instructions/{date}           // Instructions
GET    /api/ops/v1/settlement/export/pdf/{date}             // Export PDF
```

---

### 3.5 Monitoring & Alerts

**Requirement**: Real-time monitoring dashboards and alert management

**Needed Features**:
```yaml
Real-Time Dashboards:
  - Payment volume (TPS)
  - Success rate (%)
  - Failure rate (%)
  - Average response time
  - Top failure reasons
  - Payment by clearing system
  - Payment by tenant
  - P50, P95, P99 latency

Alerts:
  - Active alerts (CRITICAL, WARNING, INFO)
  - Alert history
  - Acknowledge alerts
  - Resolve alerts
  - Create manual alerts
  - Alert rules management

Performance Metrics:
  - Service-level metrics (per microservice)
  - Database metrics (connection pool, query time)
  - Cache metrics (hit rate, miss rate)
  - Queue metrics (message lag, processing time)
```

**Backend APIs Required**:
```java
// Monitoring APIs (NEW: Metrics Aggregation Service)
GET    /api/ops/v1/metrics/realtime                // Real-time metrics
GET    /api/ops/v1/metrics/services/{service}      // Service metrics
GET    /api/ops/v1/metrics/clearing/{system}       // Clearing metrics
GET    /api/ops/v1/metrics/tenants/{tenant}        // Tenant metrics

// Alert APIs (NEW endpoints)
GET    /api/ops/v1/alerts/active                   // Active alerts
GET    /api/ops/v1/alerts/history                  // Alert history
POST   /api/ops/v1/alerts/{id}/acknowledge         // Acknowledge
POST   /api/ops/v1/alerts/{id}/resolve             // Resolve
POST   /api/ops/v1/alerts/create                   // Create manual alert
```

---

## 4. Gap Analysis

### 4.1 Missing Backend APIs

| Category | Missing APIs | Impact | Priority |
|----------|-------------|--------|----------|
| **Service Management** | Service health, CB management, Feature flags | ⚠️ HIGH | P0 |
| **Payment Repair** | Retry, compensate, override, saga management | 🔴 CRITICAL | P0 |
| **Transaction Search** | Advanced search, full detail, audit trail | ⚠️ HIGH | P0 |
| **Reconciliation** | Manual matching, aging reports | ⚠️ MEDIUM | P1 |
| **Monitoring** | Real-time dashboards, metrics aggregation | ⚠️ HIGH | P0 |
| **Alerts** | Alert management, acknowledgment | ⚠️ MEDIUM | P1 |

**Total Missing APIs**: ~40 new endpoints across 8 areas

---

### 4.2 Missing Microservices

| # | New Service | Purpose | Priority |
|---|-------------|---------|----------|
| 21 | **Operations Management Service** | Service health, CB, feature flags, K8s mgmt | P0 |
| 22 | **Metrics Aggregation Service** | Real-time metrics, dashboards | P0 |

**Note**: Most missing APIs can be added to existing services as new `/ops/v1/*` endpoints.

---

### 4.3 Web BFF Enhancements Required

**Current**: GraphQL for basic payment queries  
**Needed**: Operations-focused GraphQL schema

```graphql
# NEW: Operations Schema

type ServiceHealth {
  name: String!
  status: ServiceStatus!
  uptime: Float!
  requestRate: Float!
  errorRate: Float!
  responseTime: ResponseTimeMetrics!
  circuitBreaker: CircuitBreakerState!
  pods: [Pod!]!
}

type FailedPayment {
  id: ID!
  amount: Float!
  status: String!
  failureReason: String!
  failureTimestamp: DateTime!
  retryCount: Int!
  canRetry: Boolean!
  canCompensate: Boolean!
}

type SagaInstance {
  sagaId: ID!
  paymentId: ID!
  currentState: String!
  stateHistory: [SagaStateTransition!]!
  isPending: Boolean!
  canResume: Boolean!
  canCompensate: Boolean!
}

type TransactionDetail {
  payment: Payment!
  saga: SagaInstance
  validationResult: ValidationResult!
  fraudScore: FraudScore!
  limitCheck: LimitCheckResult!
  clearingStatus: ClearingStatus!
  settlementStatus: SettlementStatus!
  auditTrail: [AuditEntry!]!
  events: [PaymentEvent!]!
}

type Query {
  # Service Management
  services: [ServiceHealth!]!
  service(name: String!): ServiceHealth!
  featureFlags: [FeatureFlag!]!
  
  # Payment Repair
  failedPayments(filter: FailedPaymentFilter): [FailedPayment!]!
  pendingSagas: [SagaInstance!]!
  saga(sagaId: ID!): SagaInstance!
  
  # Transaction Enquiries
  searchTransactions(criteria: SearchCriteria!): [TransactionSummary!]!
  transaction(id: ID!): TransactionDetail!
  
  # Reconciliation
  unmatchedPayments: [Payment!]!
  unmatchedResponses: [ClearingResponse!]!
  
  # Monitoring
  realtimeMetrics: RealtimeMetrics!
  activeAlerts: [Alert!]!
}

type Mutation {
  # Service Management
  restartService(name: String!): Boolean!
  toggleFeatureFlag(flag: String!, enabled: Boolean!): FeatureFlag!
  openCircuitBreaker(service: String!): Boolean!
  closeCircuitBreaker(service: String!): Boolean!
  
  # Payment Repair
  retryPayment(id: ID!): Payment!
  compensatePayment(id: ID!): Payment!
  updatePaymentStatus(id: ID!, status: String!): Payment!
  resumeSaga(sagaId: ID!): SagaInstance!
  compensateSaga(sagaId: ID!): SagaInstance!
  
  # Reconciliation
  matchPayment(paymentId: ID!, responseId: ID!): Boolean!
  
  # Alerts
  acknowledgeAlert(id: ID!): Alert!
  resolveAlert(id: ID!): Alert!
}
```

---

## 5. Recommended Architecture

### 5.1 New Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     React Operations Portal                      │
│  (Service Mgmt | Payment Repair | Transaction Search | Alerts)  │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                │ GraphQL
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│              Web BFF (Operations-Focused GraphQL)                │
│  - Service health aggregation                                    │
│  - Payment repair orchestration                                  │
│  - Transaction search federation                                 │
│  - Monitoring data aggregation                                   │
└───┬───────┬───────┬──────┬──────┬──────┬──────┬──────┬──────────┘
    │       │       │      │      │      │      │      │
    ▼       ▼       ▼      ▼      ▼      ▼      ▼      ▼
┌────────┐┌──────┐┌────┐┌────┐┌────┐┌────┐┌────┐┌────────┐
│Payment ││ Saga ││IAM ││Audit││Recon││Batch││Clear││ NEW:   │
│Service ││Orch. ││Svc ││Svc  ││Svc  ││Svc  ││Adpt ││Ops Mgmt│
│        ││      ││    ││     ││     ││     ││     ││Service │
│+ Ops   ││+ Ops ││    ││     ││+ Ops││+ Ops││+ Ops││        │
│  APIs  ││  APIs││    ││     ││  APIs││ APIs││  APIs││        │
└────────┘└──────┘└────┘└────┘└────┘└────┘└────┘└────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│              NEW: Metrics Aggregation Service                    │
│  - Prometheus metrics collection                                 │
│  - Real-time dashboard data                                      │
│  - Alert aggregation from all services                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. New API Requirements

### 6.1 Operations Management Service (NEW Service #21)

```java
@RestController
@RequestMapping("/api/ops/v1")
public class OperationsManagementController {
    
    // Service Health
    @GetMapping("/services")
    public List<ServiceHealth> getAllServicesHealth();
    
    @GetMapping("/services/{service}/health")
    public ServiceHealth getServiceHealth(@PathVariable String service);
    
    @GetMapping("/services/{service}/metrics")
    public ServiceMetrics getServiceMetrics(@PathVariable String service);
    
    @GetMapping("/services/{service}/errors")
    public List<ErrorLog> getRecentErrors(@PathVariable String service);
    
    @PostMapping("/services/{service}/restart")
    public void restartService(@PathVariable String service);
    
    // Circuit Breaker Management
    @GetMapping("/circuit-breakers")
    public List<CircuitBreakerInfo> getAllCircuitBreakers();
    
    @GetMapping("/circuit-breakers/{service}")
    public CircuitBreakerState getCircuitBreakerState(@PathVariable String service);
    
    @PostMapping("/circuit-breakers/{service}/open")
    public void openCircuitBreaker(@PathVariable String service);
    
    @PostMapping("/circuit-breakers/{service}/close")
    public void closeCircuitBreaker(@PathVariable String service);
    
    // Feature Flag Management (delegates to Unleash)
    @GetMapping("/feature-flags")
    public List<FeatureFlag> getAllFeatureFlags();
    
    @PutMapping("/feature-flags/{flag}/toggle")
    public FeatureFlag toggleFeatureFlag(@PathVariable String flag, @RequestBody ToggleRequest request);
    
    @PutMapping("/feature-flags/{flag}/rollout")
    public FeatureFlag adjustRollout(@PathVariable String flag, @RequestBody RolloutRequest request);
}
```

---

### 6.2 Enhanced Payment Initiation Service (Add Ops APIs)

```java
@RestController
@RequestMapping("/api/ops/v1/payments")
public class PaymentRepairController {
    
    @GetMapping("/failed")
    public Page<FailedPayment> getFailedPayments(
        @RequestParam(required = false) String failureReason,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        Pageable pageable
    );
    
    @PostMapping("/{id}/retry")
    public Payment retryPayment(@PathVariable String id);
    
    @PostMapping("/{id}/compensate")
    public Payment compensatePayment(@PathVariable String id, @RequestBody CompensationRequest request);
    
    @PutMapping("/{id}/status")
    public Payment updatePaymentStatus(@PathVariable String id, @RequestBody StatusUpdateRequest request);
    
    @PostMapping("/{id}/override")
    public Payment overrideValidation(@PathVariable String id, @RequestBody OverrideRequest request);
}
```

---

### 6.3 Enhanced Saga Orchestrator (Add Ops APIs)

```java
@RestController
@RequestMapping("/api/ops/v1/sagas")
public class SagaManagementController {
    
    @GetMapping("/pending")
    public List<SagaInstance> getPendingSagas(@RequestParam(required = false) Integer olderThanHours);
    
    @GetMapping("/{sagaId}")
    public SagaInstanceDetail getSagaDetail(@PathVariable String sagaId);
    
    @GetMapping("/{sagaId}/history")
    public List<SagaStateTransition> getSagaHistory(@PathVariable String sagaId);
    
    @PostMapping("/{sagaId}/compensate")
    public SagaInstance compensateSaga(@PathVariable String sagaId);
    
    @PostMapping("/{sagaId}/resume")
    public SagaInstance resumeSaga(@PathVariable String sagaId, @RequestBody ResumeRequest request);
}
```

---

### 6.4 Enhanced Reporting Service (Add Search APIs)

```java
@RestController
@RequestMapping("/api/ops/v1/transactions")
public class TransactionSearchController {
    
    @PostMapping("/search")
    public Page<TransactionSummary> searchTransactions(@RequestBody SearchCriteria criteria, Pageable pageable);
    
    @GetMapping("/{id}/detail")
    public TransactionDetail getTransactionDetail(@PathVariable String id);
    
    @GetMapping("/{id}/audit")
    public List<AuditEntry> getAuditTrail(@PathVariable String id);
    
    @GetMapping("/{id}/events")
    public List<PaymentEvent> getEventHistory(@PathVariable String id);
    
    @PostMapping("/export/csv")
    public byte[] exportToCsv(@RequestBody SearchCriteria criteria);
    
    @PostMapping("/export/excel")
    public byte[] exportToExcel(@RequestBody SearchCriteria criteria);
}
```

---

### 6.5 Enhanced Reconciliation Service (Add Manual Match APIs)

```java
@RestController
@RequestMapping("/api/ops/v1/reconciliation")
public class ReconciliationManagementController {
    
    @GetMapping("/unmatched/payments")
    public List<UnmatchedPayment> getUnmatchedPayments(@RequestParam(required = false) Integer agingDays);
    
    @GetMapping("/unmatched/responses")
    public List<UnmatchedClearingResponse> getUnmatchedResponses(@RequestParam(required = false) Integer agingDays);
    
    @GetMapping("/aging")
    public AgingReport getAgingReport();
    
    @PostMapping("/match")
    public MatchResult manualMatch(@RequestBody MatchRequest request);
    
    @PostMapping("/bulk-match")
    public BulkMatchResult bulkMatch(@RequestParam MultipartFile csvFile);
}
```

---

### 6.6 Metrics Aggregation Service (NEW Service #22)

```java
@RestController
@RequestMapping("/api/ops/v1/metrics")
public class MetricsAggregationController {
    
    @GetMapping("/realtime")
    public RealtimeMetrics getRealtimeMetrics();
    
    @GetMapping("/services/{service}")
    public ServiceMetrics getServiceMetrics(@PathVariable String service);
    
    @GetMapping("/clearing/{system}")
    public ClearingMetrics getClearingMetrics(@PathVariable String system);
    
    @GetMapping("/tenants/{tenant}")
    public TenantMetrics getTenantMetrics(@PathVariable String tenant);
}

@RestController
@RequestMapping("/api/ops/v1/alerts")
public class AlertManagementController {
    
    @GetMapping("/active")
    public List<Alert> getActiveAlerts(@RequestParam(required = false) AlertSeverity severity);
    
    @GetMapping("/history")
    public Page<Alert> getAlertHistory(@RequestParam LocalDate from, @RequestParam LocalDate to, Pageable pageable);
    
    @PostMapping("/{id}/acknowledge")
    public Alert acknowledgeAlert(@PathVariable String id, @RequestBody AckRequest request);
    
    @PostMapping("/{id}/resolve")
    public Alert resolveAlert(@PathVariable String id, @RequestBody ResolveRequest request);
    
    @PostMapping("/create")
    public Alert createManualAlert(@RequestBody CreateAlertRequest request);
}
```

---

## 7. React Frontend Features

### 7.1 Page Structure

```
Operations Portal (React)
│
├─ 1. Dashboard (Home)
│  ├─ Real-time metrics (TPS, success rate, error rate)
│  ├─ Service health grid (20 services)
│  ├─ Recent alerts
│  ├─ Failed payments count
│  └─ Pending sagas count
│
├─ 2. Service Management
│  ├─ Service List (20 services, health status)
│  ├─ Service Detail (metrics, errors, logs)
│  ├─ Circuit Breaker Management
│  ├─ Feature Flag Management
│  └─ Kubernetes Pods (list, restart, logs)
│
├─ 3. Payment Repair
│  ├─ Failed Payments List (filters, search)
│  ├─ Payment Repair Actions (retry, compensate, override)
│  ├─ Saga Management (list, detail, resume, compensate)
│  └─ Batch Repair (bulk retry)
│
├─ 4. Transaction Enquiries
│  ├─ Advanced Search (multi-criteria)
│  ├─ Transaction Detail View (full info, audit trail, events)
│  ├─ Export (CSV, Excel)
│  └─ Saved Searches
│
├─ 5. Reconciliation
│  ├─ Unmatched Payments
│  ├─ Unmatched Clearing Responses
│  ├─ Manual Matching Interface
│  ├─ Aging Report
│  └─ Settlement Reports
│
├─ 6. Monitoring
│  ├─ Real-Time Dashboards (Grafana-like)
│  ├─ Performance Metrics (per service)
│  ├─ Alert Management
│  └─ Log Viewer
│
├─ 7. Configuration
│  ├─ Tenant Management
│  ├─ User Management (IAM)
│  └─ System Configuration
│
└─ 8. Reports
   ├─ Daily Settlement Report
   ├─ Reconciliation Report
   ├─ Performance Report
   └─ Custom Reports
```

---

### 7.2 Key React Components

```tsx
// 1. Service Health Dashboard
<ServiceHealthDashboard>
  <ServiceGrid services={20} />
  <MetricsChart type="realtime" />
  <AlertPanel alerts={activeAlerts} />
</ServiceHealthDashboard>

// 2. Failed Payments List
<FailedPaymentsList>
  <SearchFilters />
  <PaymentTable 
    columns={['ID', 'Amount', 'Status', 'Failure Reason', 'Retry Count', 'Actions']}
    actions={['Retry', 'Compensate', 'View Detail']}
  />
  <BulkActions buttons={['Retry All', 'Export']} />
</FailedPaymentsList>

// 3. Transaction Search
<TransactionSearch>
  <AdvancedSearchForm 
    fields={['paymentId', 'accountNumber', 'amount', 'status', 'dateRange', 'tenant']}
  />
  <SearchResults />
  <TransactionDetailModal>
    <PaymentInfo />
    <SagaInfo />
    <AuditTrail />
    <EventHistory />
  </TransactionDetailModal>
</TransactionSearch>

// 4. Saga Management
<SagaManagement>
  <PendingSagasList filter="older than 1 hour" />
  <SagaDetail>
    <StateMachine currentState="DEBITED" />
    <StateHistory />
    <Actions buttons={['Resume', 'Compensate']} />
  </SagaDetail>
</SagaManagement>

// 5. Reconciliation
<ReconciliationWorkbench>
  <UnmatchedPayments />
  <UnmatchedResponses />
  <DragDropMatcher />
  <AgingChart />
</ReconciliationWorkbench>
```

---

## 8. Implementation Plan

### Phase 1: Backend APIs (6 weeks)

**Week 1-2: Operations Management Service (NEW)**
- Service health monitoring
- Circuit breaker management
- Feature flag integration
- Kubernetes API integration

**Week 3-4: Enhance Existing Services with Ops APIs**
- Payment Initiation: Add payment repair APIs
- Saga Orchestrator: Add saga management APIs
- Reporting: Add advanced search APIs
- Reconciliation: Add manual matching APIs
- Clearing Adapters: Add clearing status APIs

**Week 5-6: Metrics Aggregation Service (NEW)**
- Real-time metrics aggregation (Prometheus)
- Alert management
- Dashboard data APIs

---

### Phase 2: Web BFF Enhancement (2 weeks)

**Week 1: GraphQL Schema Redesign**
- Design operations-focused GraphQL schema
- Implement queries (services, failed payments, transactions, etc.)
- Implement mutations (retry, compensate, toggle flags, etc.)

**Week 2: Data Aggregation & Caching**
- Aggregate data from multiple services
- Implement Redis caching (60s TTL)
- Optimize for ops team use cases (fast search, bulk operations)

---

### Phase 3: React Frontend (4 weeks)

**Week 1: Dashboard & Service Management**
- Home dashboard (real-time metrics)
- Service health grid
- Circuit breaker management
- Feature flag management

**Week 2: Payment Repair & Saga Management**
- Failed payments list
- Payment repair actions
- Saga management UI
- Bulk operations

**Week 3: Transaction Enquiries & Reconciliation**
- Advanced search
- Transaction detail view
- Unmatched items
- Manual matching interface

**Week 4: Monitoring & Reports**
- Alert management
- Performance dashboards
- Settlement reports
- Export functionality

---

### Phase 4: Testing & Deployment (2 weeks)

**Week 1: Testing**
- Unit tests (80%+ coverage)
- Integration tests
- E2E tests (Cypress)
- Load testing (1000 concurrent ops users)

**Week 2: Deployment**
- Deploy to dev/staging
- User acceptance testing (ops team)
- Production deployment
- Training documentation

**Total Duration**: 14 weeks (~3.5 months)

---

## 9. Technology Stack

### Frontend (React)
```yaml
Core:
  - React 18 (Hooks, Context API)
  - TypeScript 5
  - Vite (build tool)

State Management:
  - React Query (server state, caching)
  - Zustand (client state)

UI Framework:
  - Material-UI v5 (MUI)
  - React Table v8 (data grids)
  - Recharts (charts)
  - React Flow (saga visualization)

GraphQL:
  - Apollo Client
  - GraphQL Code Generator

Testing:
  - Jest (unit tests)
  - React Testing Library
  - Cypress (E2E)
```

### Backend (Web BFF + New Services)
```yaml
Core:
  - Java 17, Spring Boot 3.x
  - Spring GraphQL
  - Spring WebFlux (reactive)

Monitoring:
  - Prometheus (metrics)
  - Micrometer (instrumentation)
  - OpenTelemetry (tracing)

Caching:
  - Redis (60s TTL for ops queries)

Security:
  - JWT (Azure AD B2C)
  - RBAC (ops roles: ADMIN, OPERATOR, VIEWER)
```

---

## 10. Estimation Summary

### Backend Work

| Task | Estimated Lines of Code | Agent Days | Priority |
|------|------------------------|------------|----------|
| Operations Management Service | 1,200 | 6 days | P0 |
| Metrics Aggregation Service | 1,000 | 5 days | P0 |
| Payment Repair APIs | 800 | 4 days | P0 |
| Saga Management APIs | 600 | 3 days | P0 |
| Transaction Search APIs | 700 | 4 days | P0 |
| Reconciliation APIs | 500 | 3 days | P1 |
| Clearing Status APIs | 400 | 2 days | P1 |
| **Total Backend** | **5,200** | **27 days** | - |

### Frontend Work

| Task | Estimated Lines of Code | Agent Days | Priority |
|------|------------------------|------------|----------|
| Web BFF GraphQL Schema | 1,500 | 5 days | P0 |
| React Dashboard | 2,000 | 6 days | P0 |
| Service Management UI | 1,500 | 5 days | P0 |
| Payment Repair UI | 1,800 | 6 days | P0 |
| Transaction Search UI | 1,500 | 5 days | P0 |
| Reconciliation UI | 1,200 | 4 days | P1 |
| Monitoring UI | 1,000 | 3 days | P1 |
| Reports UI | 800 | 3 days | P1 |
| **Total Frontend** | **11,300** | **37 days** | - |

### Testing & Deployment

| Task | Agent Days | Priority |
|------|-----------|----------|
| Unit Tests | 10 days | P0 |
| Integration Tests | 5 days | P0 |
| E2E Tests | 5 days | P0 |
| Load Testing | 3 days | P1 |
| Deployment | 3 days | P0 |
| **Total Testing** | **26 days** | - |

**Grand Total**: 90 agent-days (~4.5 months with 2 agents, or ~3 months with 3 agents)

---

## 11. Critical Recommendations

### 11.1 Immediate Actions

1. ⚠️ **STOP Feature 4.5 (Web BFF) as currently designed** - It doesn't meet ops team needs
2. 🔴 **Add 2 new microservices**: Operations Management Service, Metrics Aggregation Service
3. ⚠️ **Enhance 7 existing services** with `/ops/v1/*` endpoints
4. ✅ **Redesign Web BFF** with operations-focused GraphQL schema
5. ✅ **Build React Ops Portal** (not customer-facing)

---

### 11.2 Architecture Alignment

**Current**: Web BFF → Payment Service (basic queries)  
**Needed**: Web BFF → 20 services + 2 new services (ops-focused aggregation)

**Current Design Gap**: 60% of operations features are missing from backend  
**Recommended**: Add ~40 new `/ops/v1/*` endpoints across services

---

### 11.3 Security Considerations

```yaml
RBAC Roles:
  - PLATFORM_ADMIN: Full access (all ops features)
  - OPS_ADMIN: Payment repair, service management
  - OPS_OPERATOR: Transaction search, reconciliation
  - OPS_VIEWER: Read-only access

API Security:
  - All /ops/v1/* endpoints require authentication
  - JWT token with ops role claim
  - Audit log all ops actions (who did what when)
  - Rate limiting (100 req/min per user)
  - No customer PII in logs (mask account numbers)
```

---

## 12. Conclusion

### Summary

**Finding**: The current Web BFF (Feature 4.5) is **NOT aligned** with operations team requirements.

**Gap**: 
- ❌ Missing 60% of required backend APIs
- ❌ GraphQL schema is customer-focused, not ops-focused
- ❌ No service management features
- ❌ No payment repair workflows
- ❌ Limited transaction search
- ❌ No reconciliation tools
- ❌ No real-time monitoring

**Recommendation**: 
1. Add 2 new microservices (Operations Management, Metrics Aggregation)
2. Enhance 7 existing services with `/ops/v1/*` endpoints
3. Redesign Web BFF with operations-focused GraphQL
4. Build React Operations Portal (8 major sections)

**Effort**: 90 agent-days (~3-4 months with parallel development)

**Priority**: P0 (Critical for operations team to manage production system)

---

**Status**: ⚠️ **ACTION REQUIRED** - Architecture needs significant enhancement for ops team

**Next Steps**: 
1. Review and approve this analysis
2. Prioritize Phase 1 (Backend APIs) - 6 weeks
3. Design operations GraphQL schema - 1 week
4. Build React Ops Portal - 4 weeks

---

**Created**: 2025-10-12  
**Version**: 1.0  
**Owner**: Architecture Team  
**Reviewed By**: [Pending]
