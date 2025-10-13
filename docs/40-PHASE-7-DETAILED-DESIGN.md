# Phase 7: Operations & Channel Management - Detailed Design

## Executive Summary

**Phase 7** addresses the **60% gap** identified for operations team requirements and adds self-service channel onboarding capabilities.

**Total New Features**: 11  
**New Microservices**: 2  
**Enhanced Microservices**: 4  
**New Backend Endpoints**: ~38  
**New React Components**: ~150  
**Total Lines of Code**: ~12,100  
**Estimated Duration**: 62-82 agent-days (6-8 days with 11 agents in parallel)  

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Backend Services (Features 7.1-7.6)](#backend-services-features-71-76)
3. [Frontend UIs (Features 7.7-7.11)](#frontend-uis-features-77-711)
4. [API Specifications](#api-specifications)
5. [Database Schema](#database-schema)
6. [Security & RBAC](#security--rbac)
7. [Deployment Architecture](#deployment-architecture)
8. [Testing Strategy](#testing-strategy)
9. [Monitoring & Observability](#monitoring--observability)
10. [Implementation Roadmap](#implementation-roadmap)

---

## 1. Architecture Overview

### 1.1 Phase 7 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    REACT OPERATIONS PORTAL (Port 3000)                       │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  1. Dashboard | 2. Service Mgmt | 3. Payment Repair | 4. Enquiries  │  │
│  │  5. Reconciliation | 6. Monitoring | 7. Channel Mgmt | 8. Config     │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────────────────┘
                                │ GraphQL (Web BFF - redesigned for ops)
                                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     WEB BFF (Operations-Focused GraphQL)                     │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  - Service health aggregation                                         │  │
│  │  - Payment repair orchestration                                       │  │
│  │  - Transaction search federation                                      │  │
│  │  - Monitoring data aggregation                                        │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└───┬────────┬────────┬────────┬────────┬────────┬────────┬────────┬──────────┘
    │        │        │        │        │        │        │        │
    ▼        ▼        ▼        ▼        ▼        ▼        ▼        ▼
┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│ NEW:   ││ NEW:   ││Payment ││ Saga   ││Report- ││ Recon- ││Tenant  ││ IAM    │
│ Ops    ││Metrics ││ Init   ││ Orch.  ││  ing   ││  cil.  ││ Mgmt   ││Service │
│ Mgmt   ││Aggr.   ││Service ││Service ││Service ││Service ││Service ││        │
│#21 🆕  ││#22 🆕  ││#1 (enh)││#6 (enh)││#15(enh)││#12(enh)││#17     ││#18     │
│        ││        ││        ││        ││        ││        ││(+Chan  ││        │
│        ││        ││        ││        ││        ││        ││ APIs)  ││        │
└───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘└────────┘└────────┘
    │         │         │         │         │         │
    │         │         │         │         │         │
    ▼         ▼         ▼         ▼         ▼         ▼
┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│K8s API ││Prom.   ││Event   ││State   ││Elastic-││Database│
│        ││        ││  Bus   ││Machine ││ search ││        │
└────────┘└────────┘└────────┘└────────┘└────────┘└────────┘
```

---

### 1.2 Component Interaction Flow

**Operations Flow**:
```
Ops User (React Portal)
    ↓ GraphQL Query
Web BFF (Operations)
    ↓ REST API
Operations Management Service (#21)
    ↓ K8s API / Unleash API
Kubernetes Cluster / Feature Flag Service
```

**Payment Repair Flow**:
```
Ops User (React Portal)
    ↓ GraphQL Mutation (retryPayment)
Web BFF
    ↓ POST /api/ops/v1/payments/{id}/retry
Payment Initiation Service (#1) - Enhanced
    ↓ Republish Event
Azure Service Bus
    ↓ PaymentInitiatedEvent
Validation Service → Routing → Clearing Adapter
```

**Transaction Search Flow**:
```
Ops User (React Portal)
    ↓ GraphQL Query (searchTransactions)
Web BFF
    ↓ POST /api/ops/v1/transactions/search
Reporting Service (#15) - Enhanced
    ↓ Elasticsearch Query
Elasticsearch
    ↓ Results + Aggregation
Web BFF → React Portal
```

**Channel Onboarding Flow**:
```
Channel Admin (React Portal)
    ↓ REST API (createChannel)
Web BFF
    ↓ POST /api/v1/channels
Tenant Management Service (#17) - Enhanced
    ↓ Store in Database
PostgreSQL (channel_configurations table)
    ↓ Auto-create Kafka topic (if pattern = KAFKA)
Kafka Admin API
```

---

## 2. Backend Services (Features 7.1-7.6)

### 2.1 Operations Management Service (#21) - Feature 7.1

**Type**: NEW Microservice  
**Port**: 8021  
**Database**: Redis (cache), PostgreSQL (config)

**Responsibilities**:
1. Service health monitoring (all 22 services)
2. Circuit breaker management (Resilience4j metrics)
3. Feature flag management (Unleash integration)
4. Kubernetes pod management (restart, scale, logs)
5. Error log aggregation

**API Endpoints** (10 total):

```yaml
# Service Health
GET    /api/ops/v1/services
GET    /api/ops/v1/services/{service}/health
GET    /api/ops/v1/services/{service}/metrics
GET    /api/ops/v1/services/{service}/errors
POST   /api/ops/v1/services/{service}/restart

# Circuit Breaker Management
GET    /api/ops/v1/circuit-breakers
GET    /api/ops/v1/circuit-breakers/{service}
POST   /api/ops/v1/circuit-breakers/{service}/open
POST   /api/ops/v1/circuit-breakers/{service}/close

# Feature Flag Management
GET    /api/ops/v1/feature-flags
PUT    /api/ops/v1/feature-flags/{flag}/toggle
PUT    /api/ops/v1/feature-flags/{flag}/rollout
```

**Key Implementation**:

```java
@Service
public class ServiceHealthAggregator {
    
    @Autowired
    private KubernetesClient k8sClient;
    
    @Autowired
    private ActuatorHealthClient actuatorClient;
    
    public List<ServiceHealth> getAllServicesHealth() {
        List<ServiceHealth> healthList = new ArrayList<>();
        
        // Get all services from Kubernetes
        ServiceList services = k8sClient.services()
            .inNamespace("payments")
            .withLabel("app.kubernetes.io/part-of", "payments-engine")
            .list();
        
        // For each service, aggregate health
        for (Service service : services.getItems()) {
            ServiceHealth health = ServiceHealth.builder()
                .name(service.getMetadata().getName())
                .status(getServiceStatus(service))
                .pods(getPodCount(service))
                .requestRate(getRequestRate(service))
                .errorRate(getErrorRate(service))
                .circuitBreakerState(getCircuitBreakerState(service))
                .build();
            
            healthList.add(health);
        }
        
        return healthList;
    }
}
```

**Dependencies**:
- Kubernetes Java Client
- Unleash Java SDK
- Spring Boot Actuator
- Resilience4j metrics

---

### 2.2 Metrics Aggregation Service (#22) - Feature 7.2

**Type**: NEW Microservice  
**Port**: 8022  
**Database**: Redis (cache), TimescaleDB (metrics)

**Responsibilities**:
1. Real-time metrics aggregation (Prometheus)
2. Dashboard data generation
3. Alert management (Alertmanager)
4. Service-level metrics (per service)
5. Clearing system metrics (per system)
6. Tenant metrics (per tenant)

**API Endpoints** (10 total):

```yaml
# Metrics
GET    /api/ops/v1/metrics/realtime
GET    /api/ops/v1/metrics/services/{service}
GET    /api/ops/v1/metrics/clearing/{system}
GET    /api/ops/v1/metrics/tenants/{tenant}

# Alerts
GET    /api/ops/v1/alerts/active
GET    /api/ops/v1/alerts/history
POST   /api/ops/v1/alerts/{id}/acknowledge
POST   /api/ops/v1/alerts/{id}/resolve
POST   /api/ops/v1/alerts/create
GET    /api/ops/v1/alerts/rules
```

**Key Implementation**:

```java
@Service
public class PrometheusMetricsAggregator {
    
    @Autowired
    private WebClient prometheusClient;
    
    public Mono<RealtimeMetrics> getRealtimeMetrics() {
        // Query Prometheus with PromQL
        String promql = "sum(rate(http_server_requests_seconds_count[1m])) by (service)";
        
        return prometheusClient.get()
            .uri("/api/v1/query?query=" + promql)
            .retrieve()
            .bodyToMono(PrometheusResponse.class)
            .map(response -> {
                // Parse Prometheus response
                // Aggregate metrics
                return RealtimeMetrics.builder()
                    .tps(calculateTPS(response))
                    .successRate(calculateSuccessRate(response))
                    .errorRate(calculateErrorRate(response))
                    .build();
            })
            .cache(Duration.ofSeconds(5));  // Cache for 5s
    }
}
```

**Dependencies**:
- Prometheus Java Client
- Spring WebFlux (reactive)
- Redis (caching)

---

### 2.3 Payment Repair APIs - Feature 7.3

**Type**: Enhance Payment Initiation Service (#1)  
**New Endpoints**: 6

**API Endpoints**:

```yaml
GET    /api/ops/v1/payments/failed
POST   /api/ops/v1/payments/{id}/retry
POST   /api/ops/v1/payments/bulk-retry
POST   /api/ops/v1/payments/{id}/compensate
PUT    /api/ops/v1/payments/{id}/status
POST   /api/ops/v1/payments/{id}/override
```

**Key Implementation**:

```java
@RestController
@RequestMapping("/api/ops/v1/payments")
public class PaymentRepairController {
    
    @PostMapping("/{id}/retry")
    public Payment retryPayment(
        @PathVariable String id,
        @RequestHeader("X-User-ID") String userId
    ) {
        // 1. Load payment
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException(id));
        
        // 2. Validate retryable
        if (!payment.getStatus().equals(PaymentStatus.FAILED)) {
            throw new InvalidStateException("Payment is not in FAILED state");
        }
        
        // 3. Reset status
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setRetryCount(payment.getRetryCount() + 1);
        payment.setLastRetriedBy(userId);
        payment.setLastRetriedAt(Instant.now());
        
        // 4. Save
        payment = paymentRepository.save(payment);
        
        // 5. Republish PaymentInitiatedEvent
        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
            .paymentId(payment.getPaymentId())
            .amount(payment.getAmount())
            .status(PaymentStatus.INITIATED)
            .isRetry(true)
            .build();
        
        eventPublisher.publish(event);
        
        // 6. Audit log
        auditService.log(AuditAction.PAYMENT_RETRY, userId, payment.getPaymentId());
        
        return payment;
    }
}
```

---

### 2.4 Saga Management APIs - Feature 7.4

**Type**: Enhance Saga Orchestrator (#6)  
**New Endpoints**: 5

**API Endpoints**:

```yaml
GET    /api/ops/v1/sagas/pending
GET    /api/ops/v1/sagas/{sagaId}
GET    /api/ops/v1/sagas/{sagaId}/history
POST   /api/ops/v1/sagas/{sagaId}/resume
POST   /api/ops/v1/sagas/{sagaId}/compensate
```

**Key Implementation**:

```java
@RestController
@RequestMapping("/api/ops/v1/sagas")
public class SagaManagementController {
    
    @GetMapping("/pending")
    public List<SagaInstance> getPendingSagas(
        @RequestParam(required = false) Integer olderThanHours
    ) {
        Instant cutoff = Instant.now().minus(
            Duration.ofHours(olderThanHours != null ? olderThanHours : 1)
        );
        
        return sagaRepository.findByStatusAndCreatedAtBefore(
            SagaStatus.PENDING,
            cutoff
        );
    }
    
    @PostMapping("/{sagaId}/resume")
    public SagaInstance resumeSaga(
        @PathVariable String sagaId,
        @RequestHeader("X-User-ID") String userId
    ) {
        // 1. Load saga state
        SagaInstance saga = sagaRepository.findById(sagaId)
            .orElseThrow(() -> new SagaNotFoundException(sagaId));
        
        // 2. Load state machine
        StateMachine<SagaState, SagaEvent> stateMachine = 
            stateMachineFactory.getStateMachine(sagaId);
        
        // 3. Resume from last state
        stateMachine.sendEvent(SagaEvent.RESUME);
        
        // 4. Update saga
        saga.setCurrentState(stateMachine.getState().getId());
        saga.setResumedBy(userId);
        saga.setResumedAt(Instant.now());
        
        // 5. Save
        saga = sagaRepository.save(saga);
        
        // 6. Audit log
        auditService.log(AuditAction.SAGA_RESUME, userId, sagaId);
        
        return saga;
    }
}
```

---

### 2.5 Transaction Search APIs - Feature 7.5

**Type**: Enhance Reporting Service (#15)  
**New Endpoints**: 8  
**New Dependency**: Elasticsearch (full-text search)

**API Endpoints**:

```yaml
POST   /api/ops/v1/transactions/search
GET    /api/ops/v1/transactions/{id}/detail
GET    /api/ops/v1/transactions/{id}/audit
GET    /api/ops/v1/transactions/{id}/events
GET    /api/ops/v1/transactions/{id}/clearing
GET    /api/ops/v1/transactions/{id}/settlement
POST   /api/ops/v1/transactions/export/csv
POST   /api/ops/v1/transactions/export/excel
```

**Key Implementation**:

```java
@Service
public class TransactionSearchService {
    
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    public Page<TransactionSummary> searchTransactions(
        SearchCriteria criteria,
        Pageable pageable
    ) {
        // Build Elasticsearch query
        BoolQuery.Builder query = new BoolQuery.Builder();
        
        if (criteria.getPaymentId() != null) {
            query.must(m -> m.term(t -> t.field("paymentId").value(criteria.getPaymentId())));
        }
        
        if (criteria.getAccountNumber() != null) {
            query.should(s -> s.term(t -> t.field("debtorAccount").value(criteria.getAccountNumber())));
            query.should(s -> s.term(t -> t.field("creditorAccount").value(criteria.getAccountNumber())));
        }
        
        if (criteria.getStatus() != null) {
            query.must(m -> m.term(t -> t.field("status").value(criteria.getStatus())));
        }
        
        if (criteria.getDateRange() != null) {
            query.must(m -> m.range(r -> r
                .field("createdAt")
                .gte(JsonData.of(criteria.getDateRange().getFrom()))
                .lte(JsonData.of(criteria.getDateRange().getTo()))
            ));
        }
        
        // Execute search
        SearchResponse<TransactionDocument> response = elasticsearchClient.search(s -> s
            .index("payments")
            .query(q -> q.bool(query.build()))
            .from(pageable.getPageNumber() * pageable.getPageSize())
            .size(pageable.getPageSize())
            .sort(so -> so.field(f -> f.field("createdAt").order(SortOrder.Desc)))
        , TransactionDocument.class);
        
        // Convert to TransactionSummary
        List<TransactionSummary> results = response.hits().hits().stream()
            .map(hit -> toTransactionSummary(hit.source()))
            .toList();
        
        return new PageImpl<>(results, pageable, response.hits().total().value());
    }
}
```

---

### 2.6 Reconciliation Management APIs - Feature 7.6

**Type**: Enhance Reconciliation Service (#12)  
**New Endpoints**: 5

**API Endpoints**:

```yaml
GET    /api/ops/v1/reconciliation/unmatched/payments
GET    /api/ops/v1/reconciliation/unmatched/responses
GET    /api/ops/v1/reconciliation/aging
POST   /api/ops/v1/reconciliation/match
POST   /api/ops/v1/reconciliation/bulk-match
```

**Key Implementation**:

```java
@RestController
@RequestMapping("/api/ops/v1/reconciliation")
public class ReconciliationManagementController {
    
    @PostMapping("/match")
    public MatchResult manualMatch(
        @RequestBody MatchRequest request,
        @RequestHeader("X-User-ID") String userId
    ) {
        // 1. Validate payment and clearing response exist
        Payment payment = paymentRepository.findById(request.getPaymentId())
            .orElseThrow();
        
        ClearingResponse response = clearingResponseRepository.findById(request.getResponseId())
            .orElseThrow();
        
        // 2. Validate match (amount, account)
        if (!payment.getAmount().equals(response.getAmount())) {
            throw new AmountMismatchException();
        }
        
        // 3. Create match record
        ReconciliationMatch match = ReconciliationMatch.builder()
            .matchId(UUID.randomUUID().toString())
            .paymentId(payment.getPaymentId())
            .clearingResponseId(response.getResponseId())
            .matchedBy(userId)
            .matchedAt(Instant.now())
            .matchType(MatchType.MANUAL)
            .build();
        
        match = matchRepository.save(match);
        
        // 4. Update payment status
        payment.setStatus(PaymentStatus.MATCHED);
        payment.setClearingReference(response.getClearingReference());
        paymentRepository.save(payment);
        
        // 5. Audit log
        auditService.log(AuditAction.MANUAL_MATCH, userId, payment.getPaymentId());
        
        return MatchResult.success(match.getMatchId());
    }
}
```

---

## 3. Frontend UIs (Features 7.7-7.11)

### 3.1 Service Management UI - Feature 7.7

**Pages**: 5 pages

**Main Components**:

```tsx
// src/pages/ServiceManagement/ServiceListPage.tsx

import React from 'react';
import { Grid, Card, CardContent, Typography, Chip, Box } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { opsApi } from '../../api/opsApi';

export default function ServiceListPage() {
  const { data: services, isLoading } = useQuery({
    queryKey: ['services'],
    queryFn: () => opsApi.getAllServicesHealth(),
    refetchInterval: 5000,  // Auto-refresh every 5s
  });
  
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'UP': return 'success';
      case 'DOWN': return 'error';
      case 'DEGRADED': return 'warning';
      default: return 'default';
    }
  };
  
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Service Management
      </Typography>
      
      <Grid container spacing={2}>
        {services?.map((service: any) => (
          <Grid item xs={12} sm={6} md={4} key={service.name}>
            <Card>
              <CardContent>
                <Typography variant="h6">{service.name}</Typography>
                <Chip
                  label={service.status}
                  color={getStatusColor(service.status)}
                  size="small"
                  sx={{ mt: 1 }}
                />
                <Box sx={{ mt: 2 }}>
                  <Typography variant="caption" color="text.secondary">
                    TPS: <strong>{service.requestRate.toFixed(0)}</strong>
                  </Typography>
                  <br />
                  <Typography variant="caption" color="text.secondary">
                    Error Rate: <strong>{service.errorRate.toFixed(2)}%</strong>
                  </Typography>
                  <br />
                  <Typography variant="caption" color="text.secondary">
                    Circuit Breaker: <Chip label={service.circuitBreakerState} size="small" />
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
```

**Component Structure**:
```
src/pages/ServiceManagement/
├── ServiceListPage.tsx           // 22 service cards (auto-refresh 5s)
├── ServiceDetailPage.tsx         // Metrics, errors, logs
├── CircuitBreakerPanel.tsx       // Manual open/close CBs
├── FeatureFlagPanel.tsx          // Toggle flags, rollout %
└── PodManagementPanel.tsx        // Restart, logs
```

**Lines**: ~1,500 lines

---

### 3.2 Payment Repair UI - Feature 7.8

**Pages**: 2 pages  
**Dialogs**: 3 dialogs

**Main Components**:

```tsx
// src/pages/PaymentRepair/FailedPaymentsPage.tsx

import React from 'react';
import { DataGrid } from '@mui/x-data-grid';
import { Button, Box, Typography } from '@mui/material';
import { useQuery, useMutation } from '@tanstack/react-query';
import { opsApi } from '../../api/opsApi';

export default function FailedPaymentsPage() {
  const { data: failedPayments, refetch } = useQuery({
    queryKey: ['failed-payments'],
    queryFn: () => opsApi.getFailedPayments(),
  });
  
  const retryMutation = useMutation({
    mutationFn: (paymentId: string) => opsApi.retryPayment(paymentId),
    onSuccess: () => {
      refetch();
    },
  });
  
  const columns = [
    { field: 'paymentId', headerName: 'Payment ID', width: 200 },
    { field: 'amount', headerName: 'Amount', width: 120 },
    { field: 'status', headerName: 'Status', width: 120 },
    { field: 'failureReason', headerName: 'Failure Reason', width: 250 },
    { field: 'retryCount', headerName: 'Retries', width: 80 },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 200,
      renderCell: (params) => (
        <Button
          size="small"
          variant="contained"
          onClick={() => retryMutation.mutate(params.row.paymentId)}
          disabled={!params.row.canRetry}
        >
          Retry
        </Button>
      ),
    },
  ];
  
  return (
    <Box sx={{ p: 3, height: 600 }}>
      <Typography variant="h4" gutterBottom>
        Failed Payments
      </Typography>
      
      <DataGrid
        rows={failedPayments || []}
        columns={columns}
        getRowId={(row) => row.paymentId}
        pagination
        pageSize={25}
      />
    </Box>
  );
}
```

**Component Structure**:
```
src/pages/PaymentRepair/
├── FailedPaymentsPage.tsx        // Failed payments table
├── PaymentRepairDialog.tsx       // Retry, compensate, override
├── BulkRetryDialog.tsx           // Bulk retry (all failed)
├── SagaManagementPage.tsx        // Pending sagas list
└── SagaDetailDialog.tsx          // Saga state machine (React Flow)
```

**Lines**: ~1,800 lines

---

### 3.3 Transaction Enquiries UI - Feature 7.9

**Pages**: 1 main page  
**Modals**: 1 detail modal

**Main Components**:

```tsx
// src/pages/TransactionEnquiries/TransactionSearchPage.tsx

import React from 'react';
import { Box, TextField, Button, Stack } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { opsApi } from '../../api/opsApi';
import TransactionDetailModal from './TransactionDetailModal';

export default function TransactionSearchPage() {
  const [selectedTransaction, setSelectedTransaction] = React.useState(null);
  const { register, handleSubmit } = useForm();
  
  const searchMutation = useMutation({
    mutationFn: (criteria: any) => opsApi.searchTransactions(criteria),
  });
  
  const onSearch = (data: any) => {
    searchMutation.mutate(data);
  };
  
  const columns = [
    { field: 'paymentId', headerName: 'Payment ID', width: 200 },
    { field: 'amount', headerName: 'Amount', width: 120 },
    { field: 'status', headerName: 'Status', width: 120 },
    { field: 'createdAt', headerName: 'Date', width: 180 },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 150,
      renderCell: (params) => (
        <Button
          size="small"
          onClick={() => setSelectedTransaction(params.row)}
        >
          View Detail
        </Button>
      ),
    },
  ];
  
  return (
    <Box sx={{ p: 3 }}>
      <form onSubmit={handleSubmit(onSearch)}>
        <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
          <TextField
            {...register('paymentId')}
            label="Payment ID"
            size="small"
          />
          <TextField
            {...register('accountNumber')}
            label="Account Number"
            size="small"
          />
          <TextField
            {...register('status')}
            label="Status"
            size="small"
          />
          <Button type="submit" variant="contained">
            Search
          </Button>
        </Stack>
      </form>
      
      <DataGrid
        rows={searchMutation.data || []}
        columns={columns}
        getRowId={(row) => row.paymentId}
        pagination
        pageSize={25}
        loading={searchMutation.isPending}
      />
      
      {selectedTransaction && (
        <TransactionDetailModal
          transaction={selectedTransaction}
          onClose={() => setSelectedTransaction(null)}
        />
      )}
    </Box>
  );
}
```

**Component Structure**:
```
src/pages/TransactionEnquiries/
├── TransactionSearchPage.tsx     // Search form + results table
├── AdvancedSearchForm.tsx        // Multi-criteria search
├── TransactionDetailModal.tsx    // Full detail (6 tabs)
└── components/
    ├── AuditTrailTimeline.tsx    // Audit trail (who, what, when)
    ├── EventHistoryList.tsx      // All events for payment
    └── ExportButton.tsx          // Export CSV/Excel
```

**Lines**: ~1,500 lines

---

### 3.4 Reconciliation & Monitoring UI - Feature 7.10

**Pages**: 4 pages

**Main Components**:

```tsx
// src/pages/Reconciliation/ManualMatchingInterface.tsx

import React from 'react';
import { Box, Paper, Typography } from '@mui/material';
import { DndProvider, useDrag, useDrop } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';

function DraggablePayment({ payment }: any) {
  const [{ isDragging }, drag] = useDrag({
    type: 'PAYMENT',
    item: { id: payment.paymentId, type: 'PAYMENT' },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  });
  
  return (
    <Paper
      ref={drag}
      sx={{
        p: 2,
        mb: 1,
        opacity: isDragging ? 0.5 : 1,
        cursor: 'move',
      }}
    >
      <Typography variant="body2">{payment.paymentId}</Typography>
      <Typography variant="caption">R {payment.amount}</Typography>
    </Paper>
  );
}

function DropZone({ onDrop }: any) {
  const [{ isOver }, drop] = useDrop({
    accept: ['PAYMENT', 'RESPONSE'],
    drop: (item: any) => onDrop(item),
    collect: (monitor) => ({
      isOver: monitor.isOver(),
    }),
  });
  
  return (
    <Box
      ref={drop}
      sx={{
        border: 2,
        borderColor: isOver ? 'primary.main' : 'divider',
        borderStyle: 'dashed',
        p: 3,
        minHeight: 200,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <Typography variant="body2" color="text.secondary">
        Drop items here to match
      </Typography>
    </Box>
  );
}

export default function ManualMatchingInterface() {
  const handleDrop = (item: any) => {
    console.log('Dropped:', item);
    // Call API to match payment and response
  };
  
  return (
    <DndProvider backend={HTML5Backend}>
      <Box sx={{ display: 'flex', gap: 2 }}>
        {/* Left: Unmatched Payments */}
        <Box sx={{ flex: 1 }}>
          <Typography variant="h6">Unmatched Payments</Typography>
          {/* List of draggable payments */}
        </Box>
        
        {/* Center: Drop Zone */}
        <Box sx={{ flex: 1 }}>
          <DropZone onDrop={handleDrop} />
        </Box>
        
        {/* Right: Unmatched Clearing Responses */}
        <Box sx={{ flex: 1 }}>
          <Typography variant="h6">Unmatched Responses</Typography>
          {/* List of draggable responses */}
        </Box>
      </Box>
    </DndProvider>
  );
}
```

**Component Structure**:
```
src/pages/Reconciliation/
├── ReconciliationPage.tsx        // Main page
├── ManualMatchingInterface.tsx   // Drag-and-drop matching
├── AgingReportPage.tsx           // Aging chart

src/pages/Monitoring/
├── DashboardPage.tsx             // Real-time dashboard
├── AlertManagementPage.tsx       // Alert management
└── components/
    ├── RealtimeChart.tsx         // TPS, success rate, error rate
    ├── AlertTable.tsx            // Active alerts
    └── MetricsCard.tsx           // Metrics card
```

**Lines**: ~1,200 lines

---

### 3.5 Channel Onboarding UI - Feature 7.11

**Pages**: 2 pages (onboarding wizard, channel list)  
**Steps**: 4 (channel type, response pattern, configuration, review & test)

**Main Components**:

```tsx
// src/pages/ChannelOnboarding/ChannelOnboardingPage.tsx

import React, { useState } from 'react';
import { Stepper, Step, StepLabel, Box, Button, Paper } from '@mui/material';
import { useForm, FormProvider } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import ChannelTypeSelector from './ChannelTypeSelector';
import ResponsePatternSelector from './ResponsePatternSelector';
import WebhookConfiguration from './WebhookConfiguration';
import KafkaConfiguration from './KafkaConfiguration';
import ChannelSummary from './ChannelSummary';
import { channelApi } from '../../api/channelApi';

const steps = ['Channel Type', 'Response Pattern', 'Configuration', 'Review & Test'];

export default function ChannelOnboardingPage() {
  const [activeStep, setActiveStep] = useState(0);
  const methods = useForm();
  const responsePattern = methods.watch('responsePattern');
  
  const createChannelMutation = useMutation({
    mutationFn: (data: any) => channelApi.createChannel(data),
  });
  
  const renderStepContent = (step: number) => {
    switch (step) {
      case 0: return <ChannelTypeSelector />;
      case 1: return <ResponsePatternSelector />;
      case 2:
        switch (responsePattern) {
          case 'WEBHOOK': return <WebhookConfiguration />;
          case 'KAFKA': return <KafkaConfiguration />;
          case 'WEBSOCKET': return <WebSocketConfiguration />;
          case 'POLLING': return <PollingConfiguration />;
          case 'PUSH': return <PushConfiguration />;
        }
      case 3: return <ChannelSummary />;
    }
  };
  
  return (
    <FormProvider {...methods}>
      <Paper sx={{ p: 3 }}>
        <Stepper activeStep={activeStep}>
          {steps.map((label) => (
            <Step key={label}><StepLabel>{label}</StepLabel></Step>
          ))}
        </Stepper>
        
        <Box sx={{ mt: 3 }}>
          {renderStepContent(activeStep)}
        </Box>
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
          <Button
            disabled={activeStep === 0}
            onClick={() => setActiveStep(prev => prev - 1)}
          >
            Back
          </Button>
          <Button
            variant="contained"
            onClick={() => {
              if (activeStep === steps.length - 1) {
                methods.handleSubmit(data => createChannelMutation.mutate(data))();
              } else {
                setActiveStep(prev => prev + 1);
              }
            }}
          >
            {activeStep === steps.length - 1 ? 'Create Channel' : 'Next'}
          </Button>
        </Box>
      </Paper>
    </FormProvider>
  );
}
```

**Component Structure**:
```
src/pages/ChannelOnboarding/
├── ChannelOnboardingPage.tsx     // Main wizard (4 steps)
├── ChannelTypeSelector.tsx       // Step 1: WEB, MOBILE, PARTNER, etc.
├── ResponsePatternSelector.tsx   // Step 2: WEBHOOK, KAFKA, WEBSOCKET, etc.
├── WebhookConfiguration.tsx      // Webhook config form
├── KafkaConfiguration.tsx        // Kafka config form
├── WebSocketConfiguration.tsx    // WebSocket config form
├── PollingConfiguration.tsx      // Polling config form
├── PushConfiguration.tsx         // Push config form
├── ChannelSummary.tsx           // Review step
├── TestConnection.tsx           // Test functionality
└── ChannelListPage.tsx          // Channel management
```

**Lines**: ~1,300 lines

---

## 4. API Specifications

### 4.1 Operations Management API

**Base URL**: `/api/ops/v1`

**Service Health**:
```yaml
GET /services:
  Response:
    [
      {
        "name": "payment-initiation-service",
        "status": "UP",
        "uptime": 99.98,
        "requestRate": 150.5,
        "errorRate": 0.12,
        "responseTime": {"p50": 50, "p95": 120, "p99": 250},
        "circuitBreaker": {"state": "CLOSED", "failureRate": 0.1},
        "pods": [
          {"name": "payment-init-0", "status": "Running", "cpu": "50%", "memory": "512Mi"}
        ]
      }
    ]
```

**Circuit Breaker Control**:
```yaml
POST /circuit-breakers/{service}/open:
  Request: {}
  Response:
    {
      "service": "payment-initiation-service",
      "state": "OPEN",
      "openedAt": "2025-10-12T10:00:00Z",
      "openedBy": "USER-123"
    }
```

**Feature Flag Toggle**:
```yaml
PUT /feature-flags/{flag}/toggle:
  Request:
    {
      "enabled": true,
      "rolloutPercentage": 50
    }
  Response:
    {
      "flag": "new-fraud-engine",
      "enabled": true,
      "rolloutPercentage": 50,
      "updatedAt": "2025-10-12T10:00:00Z",
      "updatedBy": "USER-123"
    }
```

---

### 4.2 Metrics Aggregation API

**Real-Time Metrics**:
```yaml
GET /metrics/realtime:
  Response:
    {
      "tps": 1250.5,
      "successRate": 99.82,
      "errorRate": 0.18,
      "activePayments": 3456,
      "completedToday": 125000,
      "failedToday": 230,
      "avgResponseTime": 85.3,
      "timestamp": "2025-10-12T10:00:00Z"
    }
```

**Alert Management**:
```yaml
GET /alerts/active:
  Response:
    [
      {
        "alertId": "ALERT-123",
        "severity": "CRITICAL",
        "service": "payment-initiation-service",
        "message": "Error rate > 5%",
        "firedAt": "2025-10-12T09:50:00Z",
        "status": "FIRING"
      }
    ]

POST /alerts/{id}/acknowledge:
  Request:
    {
      "acknowledgedBy": "USER-123",
      "comment": "Investigating root cause"
    }
  Response:
    {
      "alertId": "ALERT-123",
      "status": "ACKNOWLEDGED",
      "acknowledgedAt": "2025-10-12T10:00:00Z",
      "acknowledgedBy": "USER-123"
    }
```

---

## 5. Database Schema

### 5.1 New Tables

**channel_configurations** (for Feature 7.11):
```sql
CREATE TABLE channel_configurations (
    channel_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    channel_name VARCHAR(100) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    response_pattern VARCHAR(50) NOT NULL,
    
    -- Webhook config
    webhook_url VARCHAR(500),
    webhook_method VARCHAR(10),
    webhook_headers JSONB,
    webhook_retry_count INTEGER DEFAULT 3,
    webhook_timeout_ms INTEGER DEFAULT 30000,
    
    -- Kafka config
    kafka_topic VARCHAR(255),
    kafka_consumer_group VARCHAR(255),
    kafka_partition_count INTEGER DEFAULT 5,
    
    -- WebSocket config
    websocket_enabled BOOLEAN DEFAULT FALSE,
    websocket_namespace VARCHAR(100),
    
    -- Polling config
    polling_enabled BOOLEAN DEFAULT TRUE,
    polling_rate_limit INTEGER DEFAULT 100,
    
    -- Push config
    push_enabled BOOLEAN DEFAULT FALSE,
    push_platform VARCHAR(20),
    push_server_key VARCHAR(500),
    
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    
    CONSTRAINT fk_channel_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
);
```

**operations_audit_log** (for all ops actions):
```sql
CREATE TABLE operations_audit_log (
    audit_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,  -- PAYMENT_RETRY, SAGA_RESUME, CB_OPEN, FLAG_TOGGLE, etc.
    entity_type VARCHAR(50) NOT NULL,  -- PAYMENT, SAGA, CIRCUIT_BREAKER, FEATURE_FLAG
    entity_id VARCHAR(50) NOT NULL,
    action_details JSONB,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    
    INDEX idx_tenant_action (tenant_id, action_type),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_user (user_id),
    INDEX idx_performed_at (performed_at)
);
```

---

## 6. Security & RBAC

### 6.1 Roles & Permissions

**RBAC Model**:

```yaml
Roles:
  PLATFORM_ADMIN:
    permissions:
      - ops:services:view
      - ops:services:restart
      - ops:circuit-breakers:control
      - ops:feature-flags:toggle
      - ops:payments:repair
      - ops:sagas:manage
      - ops:transactions:search
      - ops:reconciliation:match
      - ops:alerts:manage
      - ops:channels:manage
  
  OPS_ADMIN:
    permissions:
      - ops:services:view
      - ops:circuit-breakers:control
      - ops:feature-flags:toggle
      - ops:payments:repair
      - ops:sagas:manage
      - ops:reconciliation:match
  
  OPS_OPERATOR:
    permissions:
      - ops:services:view
      - ops:transactions:search
      - ops:reconciliation:view
      - ops:alerts:view
  
  OPS_VIEWER:
    permissions:
      - ops:services:view (read-only)
      - ops:transactions:search (read-only)
      - ops:alerts:view (read-only)
```

**Implementation** (Spring Security):

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class OpsSecurityConfig {
    
    @Bean
    public SecurityFilterChain opsApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/ops/v1/**")
            .authorizeHttpRequests(auth -> auth
                // Service Management
                .requestMatchers(HttpMethod.GET, "/api/ops/v1/services/**").hasAnyRole("OPS_VIEWER", "OPS_OPERATOR", "OPS_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/ops/v1/services/*/restart").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Circuit Breakers
                .requestMatchers(HttpMethod.GET, "/api/ops/v1/circuit-breakers/**").hasAnyRole("OPS_VIEWER", "OPS_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/ops/v1/circuit-breakers/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Feature Flags
                .requestMatchers(HttpMethod.GET, "/api/ops/v1/feature-flags/**").hasAnyRole("OPS_VIEWER", "OPS_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/ops/v1/feature-flags/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Payment Repair
                .requestMatchers("/api/ops/v1/payments/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Saga Management
                .requestMatchers("/api/ops/v1/sagas/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Transaction Search
                .requestMatchers("/api/ops/v1/transactions/**").hasAnyRole("OPS_OPERATOR", "OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Reconciliation
                .requestMatchers(HttpMethod.GET, "/api/ops/v1/reconciliation/**").hasAnyRole("OPS_OPERATOR", "OPS_ADMIN", "PLATFORM_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/ops/v1/reconciliation/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                // Channels
                .requestMatchers("/api/v1/channels/**").hasAnyRole("OPS_ADMIN", "PLATFORM_ADMIN")
                
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }
}
```

---

## 7. Deployment Architecture

### 7.1 Kubernetes Deployment

**Operations Management Service**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: operations-management-service
  namespace: payments
spec:
  replicas: 3
  selector:
    matchLabels:
      app: operations-management-service
  template:
    metadata:
      labels:
        app: operations-management-service
    spec:
      serviceAccountName: ops-management-sa  # K8s API access
      containers:
      - name: operations-management
        image: payments/operations-management:1.0.0
        ports:
        - containerPort: 8021
        env:
        - name: KUBERNETES_NAMESPACE
          value: "payments"
        - name: UNLEASH_API_URL
          value: "http://unleash:4242"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

**RBAC for K8s API Access**:
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ops-management-sa
  namespace: payments

---

apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: ops-management-role
rules:
- apiGroups: [""]
  resources: ["services", "pods", "pods/log"]
  verbs: ["get", "list", "watch"]
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["delete"]  # For pod restart
- apiGroups: ["apps"]
  resources: ["deployments", "statefulsets"]
  verbs: ["get", "list", "patch"]  # For scaling

---

apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: ops-management-binding
subjects:
- kind: ServiceAccount
  name: ops-management-sa
  namespace: payments
roleRef:
  kind: ClusterRole
  name: ops-management-role
  apiGroup: rbac.authorization.k8s.io
```

---

## 8. Testing Strategy

### 8.1 Unit Testing

**Backend Unit Tests**:

```java
@SpringBootTest
@AutoConfigureMockMvc
class OperationsManagementControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private KubernetesClient k8sClient;
    
    @Test
    void testGetAllServicesHealth() throws Exception {
        // Mock K8s API response
        when(k8sClient.services().list()).thenReturn(mockServiceList());
        
        // Execute request
        mockMvc.perform(get("/api/ops/v1/services")
                .header("Authorization", "Bearer " + mockJwtToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("payment-initiation-service"))
            .andExpect(jsonPath("$[0].status").value("UP"));
    }
    
    @Test
    void testRestartService() throws Exception {
        // Mock K8s API response
        when(k8sClient.pods().withName("payment-init-0").delete()).thenReturn(true);
        
        // Execute request
        mockMvc.perform(post("/api/ops/v1/services/payment-initiation-service/restart")
                .header("Authorization", "Bearer " + mockJwtToken("OPS_ADMIN")))
            .andExpect(status().isOk());
        
        // Verify K8s API called
        verify(k8sClient.pods()).withName("payment-init-0");
    }
}
```

**Frontend Unit Tests**:

```tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import ServiceListPage from './ServiceListPage';

describe('ServiceListPage', () => {
  it('renders service health cards', async () => {
    const queryClient = new QueryClient();
    
    render(
      <QueryClientProvider client={queryClient}>
        <ServiceListPage />
      </QueryClientProvider>
    );
    
    // Wait for services to load
    const serviceCard = await screen.findByText('payment-initiation-service');
    expect(serviceCard).toBeInTheDocument();
  });
  
  it('auto-refreshes every 5 seconds', async () => {
    jest.useFakeTimers();
    
    render(<ServiceListPage />);
    
    // Initial render
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    
    // Advance timer by 5 seconds
    jest.advanceTimersByTime(5000);
    
    // Should re-fetch
    await screen.findByText('payment-initiation-service');
    
    jest.useRealTimers();
  });
});
```

---

### 8.2 Integration Testing

**Backend Integration Tests** (Testcontainers):

```java
@SpringBootTest
@Testcontainers
class PaymentRepairIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7");
    
    @Test
    void testRetryPaymentEndToEnd() {
        // 1. Create failed payment
        Payment payment = createFailedPayment();
        
        // 2. Retry via API
        ResponseEntity<Payment> response = restTemplate.postForEntity(
            "/api/ops/v1/payments/" + payment.getPaymentId() + "/retry",
            null,
            Payment.class
        );
        
        // 3. Verify payment status updated
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PaymentStatus.INITIATED, response.getBody().getStatus());
        
        // 4. Verify event published (check event bus)
        PaymentInitiatedEvent event = eventBus.getLastEvent();
        assertEquals(payment.getPaymentId(), event.getPaymentId());
        assertTrue(event.isRetry());
    }
}
```

---

### 8.3 E2E Testing (Cypress)

**Frontend E2E Tests**:

```typescript
// cypress/e2e/payment-repair.cy.ts

describe('Payment Repair Flow', () => {
  beforeEach(() => {
    cy.login('ops-admin');  // Login as OPS_ADMIN
    cy.visit('/payment-repair');
  });
  
  it('should retry failed payment', () => {
    // 1. Navigate to failed payments
    cy.contains('Failed Payments').click();
    
    // 2. Find failed payment
    cy.get('[data-testid="failed-payment-table"]')
      .contains('PAY-2025-FAILED-001')
      .parent()
      .within(() => {
        cy.get('[data-testid="retry-button"]').click();
      });
    
    // 3. Verify success message
    cy.contains('Payment retry initiated successfully').should('be.visible');
    
    // 4. Verify status updated
    cy.get('[data-testid="failed-payment-table"]')
      .should('not.contain', 'PAY-2025-FAILED-001');
  });
  
  it('should resume stuck saga', () => {
    // 1. Navigate to saga management
    cy.contains('Saga Management').click();
    
    // 2. Find pending saga
    cy.get('[data-testid="pending-sagas-table"]')
      .contains('SAGA-2025-STUCK-001')
      .parent()
      .within(() => {
        cy.get('[data-testid="resume-button"]').click();
      });
    
    // 3. Verify confirmation dialog
    cy.get('[data-testid="confirm-dialog"]').within(() => {
      cy.contains('Resume Saga').should('be.visible');
      cy.get('[data-testid="confirm-button"]').click();
    });
    
    // 4. Verify success
    cy.contains('Saga resumed successfully').should('be.visible');
  });
});
```

---

## 9. Monitoring & Observability

### 9.1 Metrics

**Operations Management Service Metrics**:
```yaml
Prometheus Metrics:
  - ops_service_health_check_duration_seconds (histogram)
  - ops_circuit_breaker_state (gauge: 0=CLOSED, 1=OPEN, 2=HALF_OPEN)
  - ops_feature_flag_toggle_total (counter)
  - ops_pod_restart_total (counter)
  - ops_errors_aggregated_total (counter)
```

**Metrics Aggregation Service Metrics**:
```yaml
Prometheus Metrics:
  - ops_metrics_query_duration_seconds (histogram)
  - ops_alert_acknowledgment_total (counter)
  - ops_dashboard_requests_total (counter)
  - ops_realtime_metrics_latency_seconds (histogram)
```

---

### 9.2 Distributed Tracing

**OpenTelemetry Integration**:

```java
@Service
public class OperationsManagementService {
    
    @WithSpan("ops.service.health.aggregate")
    public List<ServiceHealth> getAllServicesHealth() {
        Span span = Span.current();
        span.setAttribute("operation", "service.health.aggregate");
        span.setAttribute("service.count", 22);
        
        // Aggregate service health
        List<ServiceHealth> healthList = aggregateServiceHealth();
        
        span.setAttribute("result.count", healthList.size());
        return healthList;
    }
}
```

**Trace Example**:
```
Trace ID: trace-abc-123
├─ Span 1: GraphQL Query (Web BFF) - 150ms
│  ├─ Span 2: Service Health Query (Ops Mgmt Service) - 120ms
│  │  ├─ Span 3: K8s API Call (Pod List) - 50ms
│  │  ├─ Span 4: Actuator Health Check - 30ms
│  │  └─ Span 5: Metrics Aggregation - 40ms
│  └─ Span 6: GraphQL Response Transform - 30ms
```

---

## 10. Implementation Roadmap

### 10.1 Week-by-Week Plan

**Week 1-2: Backend Services (Features 7.1-7.6)**

| Week | Tasks | Agents | Status |
|------|-------|--------|--------|
| Week 1 | 7.1: Operations Management Service | Agent #41 | Parallel |
| Week 1 | 7.2: Metrics Aggregation Service | Agent #42 | Parallel |
| Week 1 | 7.3: Payment Repair APIs | Agent #43 | Parallel |
| Week 2 | 7.4: Saga Management APIs | Agent #44 | Parallel |
| Week 2 | 7.5: Transaction Search APIs | Agent #45 | Parallel |
| Week 2 | 7.6: Reconciliation Management APIs | Agent #46 | Parallel |

**Deliverables**:
- ✅ 2 new microservices deployed
- ✅ 4 services enhanced with `/ops/v1/*` APIs
- ✅ ~38 new endpoints available
- ✅ OpenAPI specs updated

**Week 3-4: Frontend UIs (Features 7.7-7.11)**

| Week | Tasks | Agents | Status |
|------|-------|--------|--------|
| Week 3 | 7.7: Service Management UI | Agent #47 | Parallel |
| Week 3 | 7.8: Payment Repair UI | Agent #48 | Parallel |
| Week 4 | 7.9: Transaction Enquiries UI | Agent #49 | Parallel |
| Week 4 | 7.10: Reconciliation & Monitoring UI | Agent #50 | Parallel |
| Week 4 | 7.11: Channel Onboarding UI | Agent #51 | Parallel |

**Deliverables**:
- ✅ 5 React UI sections deployed
- ✅ ~150 React components
- ✅ E2E tests passing

**Week 5: Testing & UAT**

| Tasks | Duration | Assignee |
|-------|----------|----------|
| Integration testing (backend) | 2 days | QA Team |
| E2E testing (frontend) | 2 days | QA Team |
| Load testing (100 concurrent ops users) | 1 day | Performance Team |
| User acceptance testing (ops team) | 2 days | Ops Team |

**Week 6: Production Deployment**

| Tasks | Duration | Assignee |
|-------|----------|----------|
| Deploy to staging | 1 day | DevOps |
| Smoke testing | 1 day | QA Team |
| Deploy to production (blue-green) | 1 day | DevOps |
| Monitor production usage | Ongoing | SRE Team |

**Total Duration**: 6 weeks (42 days)

---

### 10.2 Dependency Matrix

| Feature | Depends On | Can Start After |
|---------|------------|-----------------|
| 7.1 | Phase 5.4 (Unleash), Phase 5.1 (Istio) | Phase 6 complete |
| 7.2 | Phase 5.2 (Prometheus), Phase 5.3 (Grafana) | Phase 6 complete |
| 7.3 | Phase 1.1 (Payment Initiation) | Phase 6 complete |
| 7.4 | Phase 1.6 (Saga Orchestrator) | Phase 6 complete |
| 7.5 | Phase 3.5 (Reporting), Elasticsearch | Phase 6 complete |
| 7.6 | Phase 4.3 (Reconciliation) | Phase 6 complete |
| 7.7 | Feature 7.1 (Ops Mgmt API) | Week 2 (7.1 complete) |
| 7.8 | Feature 7.3, 7.4 (Repair APIs) | Week 2 (7.3, 7.4 complete) |
| 7.9 | Feature 7.5 (Search APIs) | Week 2 (7.5 complete) |
| 7.10 | Feature 7.2, 7.6 (Metrics, Recon APIs) | Week 2 (7.2, 7.6 complete) |
| 7.11 | Tenant Mgmt Service (Channel APIs) | Week 2 (Channel APIs ready) |

---

## Conclusion

**Phase 7 adds 11 critical features** to enable:
1. ✅ Operations team to manage the Payments Engine (service health, payment repair, transaction search)
2. ✅ Channels to self-service onboard with flexible response patterns (Webhook, Kafka, WebSocket, Polling, Push)

**Key Benefits**:
- 60% gap in ops team requirements → **RESOLVED**
- Self-service channel onboarding → **NO MORE MANUAL CONFIGURATION**
- Real-time monitoring → **PROACTIVE ISSUE DETECTION**
- Payment repair workflows → **REDUCED MTTR (Mean Time To Repair)**
- Advanced transaction search → **FASTER TROUBLESHOOTING**

**Status**: ✅ **DESIGN COMPLETE** - Ready for AI-Driven Implementation

---

**Document Version**: 1.0  
**Created**: 2025-10-12  
**Total Pages**: 40+  
**Total Features**: 11  
**Related Documents**:
- `docs/38-REACT-FRONTEND-OPS-ANALYSIS.md` (Gap Analysis)
- `docs/39-CHANNEL-INTEGRATION-MECHANISMS.md` (Channel Integration)
- `PHASE-7-SUMMARY.md` (Summary)
- `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` (Feature Tree)
