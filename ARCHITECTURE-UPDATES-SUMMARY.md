# Architecture Updates Summary

## 📋 Overview

This document provides a comprehensive summary of all architecture updates made to the Payments Engine design based on the following requirements:

1. ✅ **External Core Banking Integration**: Accounts stored in remote systems (Current, Savings, Investment, Cards, Loans)
2. ✅ **Customer Limit Management**: Multi-level transaction limit checking and enforcement
3. ✅ **Fraud Scoring API Integration**: Real-time fraud detection using external ML-based API
4. ✅ **Confluent Kafka Option**: Alternative to Azure Service Bus for Saga pattern with event sourcing

---

## 🎯 Complete Architecture

### Key Principles

1. **Separation of Concerns**
   - Payments Engine = Orchestration & workflow
   - Core Banking Systems = Account balances & master data
   - Clear API boundaries

2. **Limit-Based Risk Management**
   - Multi-level limits (daily, monthly, per-type, per-transaction)
   - Real-time enforcement
   - Automatic resets

3. **AI-Agent Buildable**
   - Small, focused modules (< 500 lines each)
   - Clear interfaces
   - Independent development

---

## 📚 Complete Documentation Set

### Core Documents (9 + 1 Quick Reference)

| # | Document | Pages | Description |
|---|----------|-------|-------------|
| 00 | [ARCHITECTURE-OVERVIEW](docs/00-ARCHITECTURE-OVERVIEW.md) | 510 lines | High-level architecture, patterns, microservices overview |
| 01 | [ASSUMPTIONS](docs/01-ASSUMPTIONS.md) | 546 lines | **ALL assumptions** - Including external systems and limits |
| 02 | [MICROSERVICES-BREAKDOWN](docs/02-MICROSERVICES-BREAKDOWN.md) | 1,607 lines | 16 microservices with APIs, DBs, tech stack |
| 03 | [EVENT-SCHEMAS](docs/03-EVENT-SCHEMAS.md) | 1,232 lines | AsyncAPI specs for all events including limit events |
| 04 | [AI-AGENT-TASK-BREAKDOWN](docs/04-AI-AGENT-TASK-BREAKDOWN.md) | 1,607 lines | Task assignments for 25 AI agents |
| 05 | [DATABASE-SCHEMAS](docs/05-DATABASE-SCHEMAS.md) | 1,594 lines | Complete DB designs including limit tables |
| 06 | [SOUTH-AFRICA-CLEARING](docs/06-SOUTH-AFRICA-CLEARING.md) | 806 lines | SAMOS, BankservAfrica, RTC, SASWITCH integration |
| 07 | [AZURE-INFRASTRUCTURE](docs/07-AZURE-INFRASTRUCTURE.md) | 1,040 lines | AKS, networking, security, Terraform configs |
| 08 | [CORE-BANKING-INTEGRATION](docs/08-CORE-BANKING-INTEGRATION.md) | 763 lines | **Integration with 8 external systems** |
| 09 | [LIMIT-MANAGEMENT](docs/09-LIMIT-MANAGEMENT.md) | 643 lines | **Customer limit management system** |
| - | [QUICK-REFERENCE](docs/QUICK-REFERENCE.md) | 473 lines | Quick lookup guide |

**Total: ~11 documents, ~12,600 lines (~320 pages)**

### Summary Documents (3)

| Document | Purpose |
|----------|---------|
| [README.md](README.md) | Project overview and navigation |
| [EXTERNAL-CORE-BANKING-SUMMARY.md](EXTERNAL-CORE-BANKING-SUMMARY.md) | Summary of external systems integration |
| [LIMIT-MANAGEMENT-FEATURE-SUMMARY.md](LIMIT-MANAGEMENT-FEATURE-SUMMARY.md) | Summary of limit management feature |

---

## 🔄 Feature 1: External Core Banking Integration

### What Changed

**Before**: Account Service stored accounts and balances in PostgreSQL

**After**: Account Adapter Service orchestrates calls to 8 external systems

### External Systems Integrated

| # | System | Account Types | API Operations |
|---|--------|---------------|----------------|
| 1 | Current Accounts | CURRENT, CHEQUE | GET, /debit, /credit, /holds |
| 2 | Savings | SAVINGS, MONEY_MARKET | GET, /debit, /credit, /holds |
| 3 | Investment | INVESTMENT, UNIT_TRUST | GET, /debit, /credit, /holds |
| 4 | Cards | CREDIT_CARD, DEBIT_CARD | GET, /debit, /credit, /holds |
| 5 | Home Loans | HOME_LOAN, MORTGAGE | GET, /credit |
| 6 | Car Loans | CAR_LOAN | GET, /credit |
| 7 | Personal Loans | PERSONAL_LOAN | GET, /credit |
| 8 | Business Banking | BUSINESS_CURRENT | GET, /debit, /credit, /holds |

### Standard API Contract

**Authentication**: OAuth 2.0 Client Credentials  
**Idempotency**: Via `Idempotency-Key` header  
**Format**: JSON over HTTPS  
**Timeout**: 5 seconds  
**Retry**: 3 attempts with exponential backoff

### Resilience Patterns

- ✅ Circuit Breaker (Resilience4j)
- ✅ Retry with exponential backoff
- ✅ Timeout protection
- ✅ Fallback to cache
- ✅ Bulkhead isolation

### Database Changes

**Removed**:
- accounts table (balances, master data)
- account_holds table
- account_balance_history table

**Added**:
- account_routing table (routing metadata)
- backend_systems table (system configs)
- account_cache table (temporary cache)
- api_call_log table (audit trail)
- backend_system_metrics table (monitoring)
- circuit_breaker_state table (resilience)

**Size Impact**: 100 GB → 50 GB (50% reduction)

---

## 💰 Feature 2: Customer Limit Management

### What Was Added

#### Multi-Level Limits

```
Customer: CUST-123456 (INDIVIDUAL_PREMIUM)
├── Daily Limit: R100,000
│   └── Used: R45,000, Available: R55,000
├── Monthly Limit: R500,000
│   └── Used: R180,000, Available: R320,000
├── Per Transaction: R50,000 (max)
├── Payment Type Limits:
│   ├── EFT: R50,000/day (Used: R15,000, Avail: R35,000)
│   ├── RTC: R100,000/day (Used: R30,000, Avail: R70,000)
│   └── RTGS: R100,000/day (Used: R0, Avail: R100,000)
└── Transaction Count: 100/day (Used: 25, Remaining: 75)
```

### Limit Profiles

| Profile | Daily | Monthly | Per Txn | Max Txns/Day |
|---------|-------|---------|---------|--------------|
| **Individual - Standard** | R50K | R200K | R25K | 100 |
| **Individual - Premium** | R100K | R500K | R50K | 200 |
| **SME** | R500K | R2M | R250K | 500 |
| **Corporate** | R5M | R50M | R5M | 1,000 |

### Payment Type Limits (Example: Individual Premium)

| Payment Type | Daily Limit | Per Transaction | Max Txns/Day |
|--------------|-------------|-----------------|--------------|
| EFT | R50,000 | R10,000 | 50 |
| RTC | R100,000 | R50,000 | 100 |
| RTGS | R100,000 | R100,000 | 20 |
| Debit Order | R25,000 | R5,000 | 20 |

### API Endpoints Added

| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/limits/customer/{customerId}` | View limits and usage |
| `POST /api/v1/limits/customer/{customerId}/check` | Check if sufficient |
| `POST /api/v1/limits/customer/{customerId}/reserve` | Reserve limit |
| `POST /api/v1/limits/customer/{customerId}/consume` | Consume after success |
| `POST /api/v1/limits/customer/{customerId}/release` | Release if failed |
| `PUT /api/v1/limits/customer/{customerId}` | Update limits (admin) |

### Database Tables Added

| Table | Purpose | Rows (Year 1) |
|-------|---------|---------------|
| `customer_limits` | Limit configuration | 10 million |
| `payment_type_limits` | Type-specific limits | 40 million |
| `customer_limit_usage` | Daily/monthly usage tracking | 3.6 billion |
| `payment_type_limit_usage` | Usage per type | 14 billion |
| `limit_reservations` | Temporary holds | ~100K active |
| `limit_usage_history` | Audit trail | 18 billion |

### Events Added

| Event | When Published | Subscribers |
|-------|----------------|-------------|
| `LimitConsumedEvent` | After successful payment | Audit, Notification, Reporting |
| `LimitReleasedEvent` | When payment fails/cancelled | Audit |

### Error Codes Added

| Code | Description |
|------|-------------|
| `DAILY_LIMIT_EXCEEDED` | Daily limit exceeded |
| `MONTHLY_LIMIT_EXCEEDED` | Monthly limit exceeded |
| `PAYMENT_TYPE_LIMIT_EXCEEDED` | Payment type limit exceeded |
| `PER_TRANSACTION_LIMIT_EXCEEDED` | Single transaction too large |
| `TRANSACTION_COUNT_EXCEEDED` | Too many transactions |

---

## 🔄 Updated Payment Flow

### Complete Flow with Both Features

```
1. User initiates payment via frontend
   ↓
2. Payment Initiation Service
   - Generate Payment ID
   - Publish: PaymentInitiatedEvent
   ↓
3. Validation Service: LIMIT CHECKS ⭐ NEW
   ├─ Load customer limits (daily, monthly, per-type)
   ├─ Calculate available limits
   ├─ Check if payment amount <= available
   │
   ├─ If LIMIT EXCEEDED:
   │  └─ Publish: ValidationFailedEvent (LIMIT_EXCEEDED)
   │     └─ STOP (payment rejected)
   │
   └─ If SUFFICIENT:
      └─ Reserve limit (30-min expiry) ⭐ NEW
         └─ Continue to compliance checks
   ↓
4. Validation Service: COMPLIANCE CHECKS
   ├─ KYC, FICA verification
   ├─ Fraud detection
   ├─ Business rules
   │
   └─ If ALL PASS:
      └─ Publish: PaymentValidatedEvent (includes reservationId)
   ↓
5. Account Adapter: RESERVE FUNDS ⭐ NEW
   ├─ Determine backend system (Current/Savings/Investment)
   ├─ Call backend: POST /accounts/{id}/holds
   │
   └─ If INSUFFICIENT FUNDS:
      ├─ Release limit reservation ⭐ NEW
      └─ Publish: InsufficientFundsEvent
   ↓
6. Routing Service
   - Determine clearing channel (SAMOS/RTC/ACH)
   - Publish: RoutingDeterminedEvent
   ↓
7. Transaction Processing Service
   - Create transaction records
   - Publish: TransactionCreatedEvent
   ↓
8. Clearing Adapter (SAMOS/RTC/Bankserv) ⭐ UPDATED
   - Format message (ISO 20022 / ISO 8583)
   - Submit to clearing system
   - Publish: ClearingSubmittedEvent
   ↓
9. Receive clearing response
   ├─ If SUCCESS:
   │  ├─ Account Adapter: POST /accounts/{id}/debit (source) ⭐ NEW
   │  ├─ Account Adapter: POST /accounts/{id}/credit (dest) ⭐ NEW
   │  ├─ Validation Service: Consume limit ⭐ NEW
   │  │  └─ Publish: LimitConsumedEvent ⭐ NEW
   │  ├─ Publish: PaymentCompletedEvent
   │  └─ Notification Service: Send success notification
   │
   └─ If FAILURE:
      ├─ Account Adapter: Release funds hold ⭐ NEW
      ├─ Validation Service: Release limit reservation ⭐ NEW
      │  └─ Publish: LimitReleasedEvent ⭐ NEW
      ├─ Publish: PaymentFailedEvent
      └─ Notification Service: Send failure notification
```

---

## 📊 Architecture Statistics

### Microservices

| # | Service | Lines of Code | Database | Changed? |
|---|---------|---------------|----------|----------|
| 1 | Payment Initiation | ~400 | PostgreSQL | No |
| 2 | **Validation** | ~550 | PostgreSQL + Redis | ✅ Enhanced (+100 lines) |
| 3 | **Account Adapter** | ~400 | PostgreSQL + Redis | ✅ Redesigned |
| 4 | Routing | ~300 | Redis | No |
| 5 | Transaction Processing | ~500 | PostgreSQL | No |
| 6-8 | Clearing Adapters | ~400 each | PostgreSQL | No |
| 9 | Settlement | ~450 | PostgreSQL | No |
| 10 | Reconciliation | ~400 | PostgreSQL | No |
| 11 | Notification | ~250 | PostgreSQL | No |
| 12 | Reporting | ~350 | PostgreSQL + Synapse | No |
| 13 | Saga Orchestrator | ~500 | PostgreSQL | Minor |
| 14 | API Gateway | ~300 | Redis | No |
| 15 | IAM | ~400 | PostgreSQL | No |
| 16 | Audit | ~300 | CosmosDB | No |

**Total Services**: 16  
**Services Changed**: 2 (Validation, Account Adapter)  
**Services Enhanced**: 1 (Saga Orchestrator - minor)

### Database Tables

**Total Tables**: 47  
**Added**: 12 (6 for limits + 6 for external system integration)  
**Removed**: 3 (account storage tables)  
**Net Increase**: +9 tables

### API Endpoints

**Total Endpoints**: ~80  
**Added**: 13 (7 for limits + 6 for account adapter)  
**Changed**: 2 (validation endpoints now include limit info)

### Events

**Total Events**: 22  
**Added**: 2 (LimitConsumedEvent, LimitReleasedEvent)

---

## 🎯 By Requirement

### Requirement 1: External Core Banking Systems ✅

**What was implemented**:
- ✅ Account Adapter Service acts as orchestration layer
- ✅ 8 external systems integrated (Current, Savings, Investment, Cards, 3 Loan types, Business)
- ✅ Standard API contract: `/debit`, `/credit`, `/holds` endpoints
- ✅ Each system has one endpoint per operation (unified interface)
- ✅ OAuth 2.0 authentication
- ✅ Circuit breaker for resilience
- ✅ Caching to reduce load
- ✅ Routing table to determine which system to call

**Documents**:
- Section 1.5 in 01-ASSUMPTIONS.md
- Section 3 in 02-MICROSERVICES-BREAKDOWN.md
- Section 3 in 05-DATABASE-SCHEMAS.md
- Complete guide: 08-CORE-BANKING-INTEGRATION.md
- Summary: EXTERNAL-CORE-BANKING-SUMMARY.md

### Requirement 2: Customer Limit Management ✅

**What was implemented**:
- ✅ Multi-level limit checking:
  - Daily limits per customer
  - Monthly limits per customer
  - Payment type-specific limits (EFT, RTC, RTGS, etc.)
  - Per transaction limits
  - Transaction count limits
- ✅ Customer profile-based default limits
- ✅ Real-time limit checking before payment execution
- ✅ Limit reservation system (prevent race conditions)
- ✅ Used limit tracking and updates
- ✅ Auto-reset logic (daily at midnight, monthly on 1st)
- ✅ Fail payments if insufficient limit
- ✅ API endpoints for viewing and checking limits
- ✅ Admin API for updating limits

**Documents**:
- Section 1.4.1 in 01-ASSUMPTIONS.md
- Section 2 in 02-MICROSERVICES-BREAKDOWN.md (Validation Service)
- New event schemas in 03-EVENT-SCHEMAS.md
- Limit tables in 05-DATABASE-SCHEMAS.md (Section 2)
- Complete guide: 09-LIMIT-MANAGEMENT.md
- Summary: LIMIT-MANAGEMENT-FEATURE-SUMMARY.md

---

## 🏗️ Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    CHANNEL LAYER                             │
│           React Web Portal │ Mobile App                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│               ORCHESTRATION LAYER                            │
│      API Gateway │ Saga Orchestrator                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                 CORE SERVICES LAYER                          │
│  Payment │ Validation │ Account  │ Transaction │ ...        │
│          │ (Limits) ✨│ Adapter✨│ Processing  │            │
└───────────┬────────────┴─────┬────────────────────────────┘
            │                   │
            │    ┌──────────────┴─────────────────┐
            │    │                                 │
            ▼    ▼                                 ▼
┌─────────────────────┐              ┌──────────────────────┐
│  EVENT BUS          │              │ CLEARING SYSTEMS     │
│  (Limit Events) ✨  │              │ SAMOS, Bankserv, RTC │
└─────────────────────┘              └──────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│              EXTERNAL CORE BANKING SYSTEMS ✨                │
│  Current │ Savings │ Investment │ Cards │ Loans (x3)       │
│  /debit  │ /debit  │ /debit     │/debit │ /credit          │
│  /credit │ /credit │ /credit    │/credit│                  │
└─────────────────────────────────────────────────────────────┘
```

✨ = New or significantly enhanced

---

## 📈 Complexity Impact

### AI Agent Development

| Agent | Original Time | New Time | Change | Reason |
|-------|---------------|----------|--------|--------|
| Agent Service-1 (Account) | 2h | 3h | +1h | External system integration, circuit breaker |
| Agent Service-6 (Validation) | 3h | 4h | +1h | Limit management logic |
| All Others | - | - | No change | - |

**Total Agent Time**: 89h → 91h (minimal impact)

**Why minimal impact?**
- Limit management contained in Validation Service
- External system integration contained in Account Adapter
- Other services unchanged
- Still highly parallelizable

---

## ✅ Quality Assurance

### Testing Coverage

**Account Adapter Service**:
- Unit tests: Mock backend systems with WireMock
- Integration tests: Test circuit breaker behavior
- Load tests: Concurrent requests to multiple backends
- Failover tests: Backend system unavailable scenarios

**Validation Service (Limits)**:
- Unit tests: Limit calculation logic
- Concurrency tests: Prevent race conditions
- Reset tests: Daily/monthly limit reset
- Reservation tests: Reserve → Consume → Release flows

### Performance Impact

| Metric | Before | After | Change |
|--------|--------|-------|--------|
| Validation Time | ~50ms | ~100ms | +50ms (limit checks) |
| Account Query | ~10ms (local DB) | ~200ms (external API + cache) | +190ms |
| Total Payment Flow | ~10s | ~10.3s | +0.3s |

**Mitigation**:
- Aggressive caching (30-second TTL)
- Parallel API calls where possible
- Circuit breaker prevents slow backends

---

## 🎓 Implementation Guide

### For AI Agents

**Enhanced Services**:

1. **Account Adapter Service** (Agent Service-1)
   - Reference: Section 3 in 02-MICROSERVICES-BREAKDOWN.md
   - New document: 08-CORE-BANKING-INTEGRATION.md
   - Database: Section 3 in 05-DATABASE-SCHEMAS.md
   - Time: 3 hours
   - Complexity: MEDIUM

2. **Validation Service** (Agent Service-6)
   - Reference: Section 2 in 02-MICROSERVICES-BREAKDOWN.md
   - New document: 09-LIMIT-MANAGEMENT.md
   - Database: Section 2 in 05-DATABASE-SCHEMAS.md
   - Time: 4 hours (was 3 hours)
   - Complexity: HIGH (was MEDIUM)

**All other services**: No changes required

---

## 🔐 Security Considerations

### External System Access
- OAuth 2.0 for all backend systems
- Client credentials stored in Azure Key Vault
- mTLS option for high-security environments
- Network isolation (VNet service endpoints)

### Limit Management
- Admin-only endpoints for limit updates
- Audit trail of all limit changes
- Rate limiting on limit check APIs
- Monitoring for suspicious limit patterns

---

## 📊 Monitoring Dashboard

### External Systems Health

```
Backend System Dashboard
├── Current Accounts System
│   ├── Status: HEALTHY ✅
│   ├── Circuit Breaker: CLOSED
│   ├── Success Rate: 99.8%
│   ├── Avg Response: 180ms
│   └── Calls/hour: 45,000
├── Savings System
│   ├── Status: DEGRADED ⚠️
│   ├── Circuit Breaker: HALF_OPEN
│   ├── Success Rate: 92.3%
│   ├── Avg Response: 1,200ms
│   └── Calls/hour: 15,000
└── [... other systems]
```

### Limit Usage Dashboard

```
Customer Limit Dashboard
├── Top 10 Customers by Usage
│   ├── CUST-001: 95% of daily limit (Alert ⚠️)
│   ├── CUST-002: 88% of daily limit (Warning)
│   └── ...
├── Limit Exceeded Events: 1,250 today
├── Average Usage: 42% of daily limit
└── Customers at >80%: 5,432
```

---

## 🎯 Business Benefits

### Risk Management
- ✅ Limit exposure to fraud
- ✅ Prevent overspending
- ✅ Configurable controls per segment

### Operational Efficiency
- ✅ Leverage existing core banking investments
- ✅ No duplicate account storage
- ✅ Automated limit enforcement
- ✅ Self-service limit viewing

### Scalability
- ✅ Backend systems scale independently
- ✅ No single point of failure
- ✅ Each system optimizes for its domain

### Compliance
- ✅ Data remains in systems of record
- ✅ Complete audit trail
- ✅ Regulatory-compliant controls

---

## 📚 Document Navigation

### Start Here
1. [README.md](README.md) - Project overview
2. [01-ASSUMPTIONS.md](docs/01-ASSUMPTIONS.md) - Review assumptions (Section 1.5 & 1.4.1)

### External Systems Integration
3. [08-CORE-BANKING-INTEGRATION.md](docs/08-CORE-BANKING-INTEGRATION.md) - Complete guide
4. [EXTERNAL-CORE-BANKING-SUMMARY.md](EXTERNAL-CORE-BANKING-SUMMARY.md) - Quick summary

### Limit Management
5. [09-LIMIT-MANAGEMENT.md](docs/09-LIMIT-MANAGEMENT.md) - Complete guide
6. [LIMIT-MANAGEMENT-FEATURE-SUMMARY.md](LIMIT-MANAGEMENT-FEATURE-SUMMARY.md) - Quick summary

### Implementation Details
7. [02-MICROSERVICES-BREAKDOWN.md](docs/02-MICROSERVICES-BREAKDOWN.md) - Service specs (Sections 2 & 3)
8. [05-DATABASE-SCHEMAS.md](docs/05-DATABASE-SCHEMAS.md) - Database designs (Sections 2 & 3)
9. [03-EVENT-SCHEMAS.md](docs/03-EVENT-SCHEMAS.md) - Event schemas (updated)
10. [04-AI-AGENT-TASK-BREAKDOWN.md](docs/04-AI-AGENT-TASK-BREAKDOWN.md) - AI agent tasks (updated)

---

## ✅ Completion Checklist

### Architecture Design
- ✅ External core banking systems architecture
- ✅ Customer limit management system
- ✅ Updated all affected documents
- ✅ Added new comprehensive guides
- ✅ Updated assumptions
- ✅ Updated database schemas
- ✅ Updated event schemas
- ✅ Updated API specifications
- ✅ Updated AI agent tasks

### Documentation
- ✅ 11 comprehensive documents
- ✅ 3 summary documents
- ✅ Quick reference guide
- ✅ ~320 pages total

### Ready for Implementation
- ✅ All specifications complete
- ✅ Database schemas ready
- ✅ API contracts defined
- ✅ Event schemas defined
- ✅ AI agent tasks updated
- ✅ Infrastructure requirements documented

---

---

## 🔄 Feature 3: Fraud Scoring API Integration ✅

### What was implemented:
- ✅ External fraud scoring API integration (third-party SaaS)
- ✅ Real-time fraud risk assessment for all transactions
- ✅ ML-based fraud score (0.0-1.0 scale)
- ✅ Risk-based decision logic (LOW, MEDIUM, HIGH, CRITICAL)
- ✅ Circuit breaker with 3 fallback strategies:
  - Fail-Open: Allow with monitoring
  - Fail-Close: Reject all
  - Rule-Based: Use internal rules
- ✅ Comprehensive fraud indicators (velocity, amount, geolocation, device, patterns)
- ✅ Complete API request/response specifications
- ✅ Cost optimization strategies

### API Integration:
- **Endpoint**: `POST https://fraud-api.provider.com/api/v1/score`
- **Authentication**: API Key or OAuth 2.0
- **Timeout**: 5 seconds
- **Response Time**: < 500ms (p95)
- **Circuit Breaker**: Enabled
- **Retry**: 3 attempts

### Risk Thresholds:
- **0.0-0.3**: LOW - Auto-approve
- **0.3-0.6**: MEDIUM - Approve with monitoring
- **0.6-0.8**: HIGH - Require verification (2FA/OTP)
- **0.8-1.0**: CRITICAL - Auto-reject

### Database Changes:
- ✅ Enhanced `fraud_detection_log` table (10 new fields)
- ✅ Added `fraud_api_metrics` table (performance monitoring)
- ✅ Added `fraud_rules` table (fallback rules)

### Cost Estimate:
- **Base**: $1M/month (50M transactions × $0.02)
- **Optimized**: $500K/month (with selective scoring, sampling, caching)

**Documents**:
- Section 4.4 in 01-ASSUMPTIONS.md
- Section 2 in 02-MICROSERVICES-BREAKDOWN.md (Validation Service - Fraud API Integration)
- Enhanced fraud tables in 05-DATABASE-SCHEMAS.md
- Complete guide: 10-FRAUD-SCORING-INTEGRATION.md
- Summary: FRAUD-SCORING-FEATURE-SUMMARY.md

---

---

## 🔄 Feature 4: Confluent Kafka Option for Saga Pattern ✅

### What was implemented:
- ✅ Confluent Kafka added as alternative to Azure Service Bus
- ✅ Event sourcing-based Saga implementation
- ✅ Orchestration AND Choreography patterns supported
- ✅ Exactly-once semantics configuration
- ✅ Event replay capability for debugging and recovery
- ✅ Kafka Streams for saga state management
- ✅ 3 deployment options:
  - Confluent Cloud (managed)
  - Self-hosted on AKS
  - Azure Event Hubs (Kafka-compatible)
- ✅ Migration path from Service Bus to Kafka
- ✅ Dual-write pattern for gradual migration

### Comparison:

| Feature | Azure Service Bus | Confluent Kafka |
|---------|-------------------|-----------------|
| Throughput | 20K msg/sec | 1M msg/sec |
| Event Replay | Limited | Full capability |
| Event Sourcing | Basic | Native support |
| Cost | $650/month | $2,700/month |
| Complexity | Low | Medium-High |

### Recommendation:
- **Start**: Azure Service Bus (simpler, cheaper)
- **Migrate**: To Kafka when throughput > 100K msg/sec or event sourcing needed
- **Architecture**: Supports both via abstract EventPublisher interface

### Kafka Topics:
- **Saga Events**: `saga.payment.events` (event sourcing)
- **Saga State**: `saga.payment.state` (compacted)
- **Commands**: `saga.commands.*` (orchestrator → services)
- **Responses**: `saga.responses.*` (services → orchestrator)
- **Total Topics**: 20+ for saga orchestration

### Code Examples:
- ✅ Event-sourced saga orchestrator (200+ lines)
- ✅ Kafka Streams state management (150+ lines)
- ✅ Exactly-once producer/consumer config
- ✅ Event replay logic
- ✅ Compensation handling
- ✅ Circuit breaker integration

**Documents**:
- Section 2.3 in 01-ASSUMPTIONS.md (Event Platform Comparison)
- Complete guide: 11-KAFKA-SAGA-IMPLEMENTATION.md
- Summary: KAFKA-SAGA-OPTION-SUMMARY.md

---

**Status**: ✅ **COMPLETE** - Ready for AI Agent Implementation

**Last Updated**: 2025-10-11  
**Version**: 4.0  
**Total Documentation**: 18 files, ~16,000 lines, ~420 pages
