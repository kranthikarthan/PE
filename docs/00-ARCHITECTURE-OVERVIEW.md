# Payments Engine - High-Level Architecture Overview

## Executive Summary

This document outlines a **highly modular, AI-agent-buildable payments engine** designed for South Africa's financial ecosystem with international payment capabilities. The architecture follows modern patterns including microservices, event-driven architecture, hexagonal architecture, and Saga patterns to ensure each component can be developed independently by AI agents.

**System Scope**: 20 microservices covering domestic (SA), international (SWIFT), real-time, and batch payment processing.

## Core Design Principles

### 1. AI-Agent Buildability
- **Single Responsibility**: Each module has ONE clear purpose
- **Clear Contracts**: Well-defined interfaces with OpenAPI/AsyncAPI specs
- **Minimal Dependencies**: Modules communicate via events and APIs only
- **Self-Contained**: Each module includes its own database, tests, and documentation
- **Size Constraint**: No module should exceed 500 lines of core business logic

### 2. Architecture Patterns

#### Hexagonal Architecture (Ports & Adapters)
```
┌─────────────────────────────────────────┐
│         Application Core                │
│    (Domain Logic - Technology Agnostic) │
│                                          │
│  ┌────────────┐      ┌────────────┐    │
│  │  Ports     │      │  Ports     │    │
│  │  (Input)   │      │  (Output)  │    │
│  └────────────┘      └────────────┘    │
└─────────────────────────────────────────┘
         ▲                        ▲
         │                        │
    ┌────┴────┐              ┌───┴────┐
    │ Adapters│              │Adapters│
    │ (REST,  │              │(DB,    │
    │  gRPC)  │              │ Queue) │
    └─────────┘              └────────┘
```

#### Event-Driven Architecture
- **Event Bus**: Azure Service Bus for inter-service communication
- **Event Sourcing**: Critical payment events are immutable
- **CQRS**: Separate read and write models for scalability

#### Saga Pattern for Distributed Transactions
- **Orchestration-Based**: Centralized saga orchestrator
- **Compensation**: Each step has a compensating transaction
- **State Machine**: Clear state transitions for payment flows

## System Architecture

### Architectural Layers

```
┌──────────────────────────────────────────────────────────────┐
│                     CHANNEL LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Web Portal   │  │ Mobile API   │  │  API Gateway │      │
│  │  (React)     │  │  (React)     │  │   (APIM)     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  ORCHESTRATION LAYER                          │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │ API Gateway      │  │ Saga Orchestrator│                 │
│  │ (Spring Cloud    │  │ (Payment Flow    │                 │
│  │  Gateway)        │  │  Coordinator)    │                 │
│  └──────────────────┘  └──────────────────┘                 │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    CORE SERVICES LAYER                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │Payment   │ │Transaction│ │Validation│ │Routing   │       │
│  │Initiation│ │Processing │ │Service   │ │Service   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│                                                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │Account   │ │Settlement│ │Clearing  │ │Notification│      │
│  │Adapter   │ │Service   │ │Adapter   │ │Service   │       │
│  │(Orchestr)│ │          │ │          │ │            │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└──────────────────────────────────────────────────────────────┘
            │                       │
            │                       ▼
            │       ┌──────────────────────────────────────────┐
            │       │   INFRASTRUCTURE LAYER                   │
            │       │  ┌────────────┐  ┌──────────────────┐   │
            │       │  │ Event Bus  │  │ Audit & Logging  │   │
            │       │  │ (Azure     │  │ (Application     │   │
            │       │  │ Service    │  │  Insights)       │   │
            │       │  │  Bus)      │  │                  │   │
            │       │  └────────────┘  └──────────────────┘   │
            │       └──────────────────────────────────────────┘
            │                       │
            ▼                       ▼
┌─────────────────────────┐ ┌──────────────────────────────────┐
│ CORE BANKING SYSTEMS    │ │   CLEARING SYSTEMS                │
│ (External - REST APIs)  │ │                                   │
├─────────────────────────┤ │ ┌──────────────┐                 │
│ ┌─────────────────────┐ │ │ │SAMOS/SASWITCH│                 │
│ │ Current Accounts    │ │ │ │(Real-time)   │                 │
│ │ /debit  /credit     │ │ │ └──────────────┘                 │
│ └─────────────────────┘ │ │                                   │
│ ┌─────────────────────┐ │ │ ┌──────────────┐                 │
│ │ Savings Accounts    │ │ │ │BankservAfrica│                 │
│ │ /debit  /credit     │ │ │ │(ACH/EFT)     │                 │
│ └─────────────────────┘ │ │ └──────────────┘                 │
│ ┌─────────────────────┐ │ │                                   │
│ │ Investment Accounts │ │ │ ┌──────────────┐                 │
│ │ /debit  /credit     │ │ │ │RTC (Real-Time│                 │
│ └─────────────────────┘ │ │ │ Clearing)    │                 │
│ ┌─────────────────────┐ │ │ └──────────────┘                 │
│ │ Card Accounts       │ │ │                                   │
│ │ /debit  /credit     │ │ └──────────────────────────────────┘
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ Home Loan           │ │
│ │ /debit  /credit     │ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ Car Loan            │ │
│ │ /debit  /credit     │ │
│ └─────────────────────┘ │
└─────────────────────────┘
```

## Microservices Breakdown

### Core Payment Services (11 Independent Services)

1. **Payment Initiation Service**
   - Accept payment requests from channels
   - Generate unique payment IDs
   - Initial validation
   - Tech: Spring Boot, PostgreSQL, Kafka

2. **Validation Service**
   - Business rules validation
   - Fraud detection integration
   - Compliance checks (FICA)
   - Tech: Spring Boot, Redis (caching rules)

3. **Account Adapter Service** (formerly Account Service)
   - **Orchestrates** calls to external core banking systems
   - Routes requests to appropriate system (Current, Savings, Investment, etc.)
   - Aggregates responses from multiple systems
   - Caches account metadata and routing information
   - Does NOT store account balances or transaction data
   - Tech: Spring Boot, Redis (caching), REST clients

4. **Routing Service**
   - Determine payment channel (EFT, RTC, Card)
   - Select clearing system
   - Load balancing
   - Tech: Spring Boot, Redis

5. **Transaction Processing Service**
   - Execute payment transactions
   - State machine management
   - Double-entry bookkeeping
   - Tech: Spring Boot, PostgreSQL (Event Store)

6. **Clearing Adapter Service**
   - Interface with SAMOS/SASWITCH
   - Interface with BankservAfrica
   - Interface with RTC
   - Tech: Spring Boot, Message Queues

7. **Settlement Service**
   - Settlement calculations
   - Net settlement position
   - Settlement file generation
   - Tech: Spring Boot, PostgreSQL

8. **Reconciliation Service**
   - Match transactions
   - Exception handling
   - Dispute management
   - Tech: Spring Boot, PostgreSQL

9. **Notification Service**
   - SMS/Email notifications
   - Webhook callbacks
   - Status updates
   - Tech: Spring Boot, Azure Notification Hub

10. **Reporting Service**
    - Transaction reports
    - Analytics
    - Compliance reports
    - Tech: Spring Boot, Azure Synapse (Analytics)

11. **Saga Orchestrator Service**
    - Coordinate distributed transactions
    - Compensation logic
    - Timeout management
    - Tech: Spring Boot, PostgreSQL (Saga State)

### Supporting Services (5 Services)

12. **API Gateway Service**
    - Request routing
    - Authentication/Authorization
    - Rate limiting
    - Tech: Spring Cloud Gateway

13. **Identity & Access Management**
    - User authentication
    - OAuth2/OIDC
    - Token management
    - Tech: Spring Security, Azure AD B2C

14. **Audit & Compliance Service**
    - Audit trail
    - Regulatory reporting
    - Compliance monitoring
    - Tech: Spring Boot, CosmosDB

15. **Configuration Service**
    - Centralized configuration
    - Feature flags
    - Tech: Spring Cloud Config

16. **Monitoring & Observability**
    - Distributed tracing
    - Metrics collection
    - Alerting
    - Tech: Azure Monitor, Application Insights

## Event-Driven Communication

### Event Categories

1. **Command Events** (Request-Response)
   - `InitiatePaymentCommand`
   - `ValidateAccountCommand`
   - `ProcessTransactionCommand`

2. **Domain Events** (Fire-and-Forget)
   - `PaymentInitiatedEvent`
   - `PaymentValidatedEvent`
   - `PaymentCompletedEvent`
   - `PaymentFailedEvent`

3. **Integration Events** (External Systems)
   - `ClearingRequestEvent`
   - `ClearingResponseEvent`
   - `SettlementCompleteEvent`

### Event Bus Topology

```
Azure Service Bus
├── Topics
│   ├── payment.initiated
│   ├── payment.validated
│   ├── payment.processing
│   ├── payment.completed
│   ├── payment.failed
│   ├── clearing.request
│   ├── clearing.response
│   └── settlement.complete
└── Subscriptions (per service)
    ├── validation-service-subscription
    ├── processing-service-subscription
    ├── notification-service-subscription
    └── audit-service-subscription
```

## Technology Stack

### Backend Services
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **API**: REST (Spring Web) + gRPC (high-throughput)
- **Security**: Spring Security + OAuth2
- **Data**: Spring Data JPA + QueryDSL

### Frontend
- **Framework**: React 18+
- **State Management**: Redux Toolkit + RTK Query
- **UI Library**: Material-UI (MUI) or Ant Design
- **Forms**: React Hook Form + Zod validation
- **Charts**: Recharts or Apache ECharts

### Azure Services
- **Compute**: Azure Kubernetes Service (AKS)
- **Messaging**: Azure Service Bus (Premium tier)
- **Databases**: 
  - Azure PostgreSQL Flexible Server (transactional)
  - Azure CosmosDB (audit logs, high-write)
- **Cache**: Azure Cache for Redis
- **Storage**: Azure Blob Storage (documents)
- **Monitoring**: Application Insights + Log Analytics
- **API Management**: Azure API Management
- **Identity**: Azure AD B2C
- **Security**: Azure Key Vault
- **Analytics**: Azure Synapse Analytics

### Development Tools
- **Containerization**: Docker
- **Orchestration**: Kubernetes (AKS)
- **CI/CD**: Azure DevOps Pipelines
- **IaC**: Terraform
- **API Docs**: OpenAPI 3.0 (Swagger)
- **Event Docs**: AsyncAPI 2.0

## South Africa Clearing Systems Integration

### 1. SAMOS (South African Multiple Option Settlement)
- **Purpose**: Real-time gross settlement (RTGS)
- **Use Case**: High-value payments (> R5 million)
- **Integration**: ISO 20022 messages via SWIFT

### 2. BankservAfrica
- **ACH (Automated Clearing House)**: Batch payments
- **EFT (Electronic Funds Transfer)**: Retail payments
- **DebiCheck**: Debit order authentication
- **Integration**: Proprietary format + ISO 8583

### 3. RTC (Real-Time Clearing)
- **Purpose**: Instant retail payments (< R5 million)
- **Use Case**: Immediate payment processing
- **Integration**: ISO 20022 messages

### 4. SASWITCH
- **Purpose**: Card payment switching
- **Use Case**: Card transactions routing
- **Integration**: ISO 8583 messages

## Payment Flow Example

### Real-Time Payment Flow (Saga Pattern)

```
1. User initiates payment via React frontend
   ↓
2. API Gateway → Payment Initiation Service
   - Generate Payment ID: PAY-2025-XXXXXX
   - Publish: PaymentInitiatedEvent
   ↓
3. Saga Orchestrator receives event
   - Create Saga Instance
   - Start State Machine
   ↓
4. Validation Service (Step 1)
   - Validate account
   - Check fraud rules
   - Check compliance
   - Publish: PaymentValidatedEvent OR ValidationFailedEvent
   ↓
5. Account Service (Step 2)
   - Place hold on funds
   - Publish: FundsReservedEvent OR InsufficientFundsEvent
   ↓
6. Routing Service (Step 3)
   - Select clearing channel (SAMOS/RTC/ACH)
   - Determine routing path
   - Publish: RoutingDeterminedEvent
   ↓
7. Transaction Processing Service (Step 4)
   - Create transaction records
   - Update state to PROCESSING
   - Publish: TransactionCreatedEvent
   ↓
8. Clearing Adapter Service (Step 5)
   - Format message (ISO 20022)
   - Send to clearing system
   - Wait for acknowledgment
   - Publish: ClearingSubmittedEvent
   ↓
9. Receive response from clearing system
   - Update transaction state
   - Publish: PaymentCompletedEvent OR PaymentFailedEvent
   ↓
10. Settlement Service (Step 6)
    - Update settlement position
    - Generate settlement entry
    ↓
11. Notification Service (Step 7)
    - Send SMS/Email to user
    - Webhook callback to frontend
    ↓
12. Saga Orchestrator
    - Mark saga as COMPLETED
    - Release resources

If any step fails:
    - Saga Orchestrator triggers compensation
    - Reverse steps in order (LIFO)
    - Notify user of failure
```

## Data Management Strategy

### Database Per Service Pattern

Each microservice owns its database:
- **Payment Initiation**: PostgreSQL (payment_requests table)
- **Account Service**: PostgreSQL (accounts, balances)
- **Transaction Processing**: PostgreSQL (transactions, event_store)
- **Settlement**: PostgreSQL (settlement_batches, positions)
- **Audit**: CosmosDB (audit_logs - append-only)

### Event Sourcing for Critical Entities

Store all state changes as events:
```java
PaymentCreatedEvent → PaymentValidatedEvent → PaymentProcessedEvent → PaymentCompletedEvent
```

Benefits:
- Complete audit trail
- Ability to replay events
- Temporal queries
- Easy debugging

## Security Architecture

### Defense in Depth

1. **Network Layer**: Azure VNet, NSG, Azure Firewall
2. **API Layer**: API Management, OAuth2, JWT tokens
3. **Application Layer**: Spring Security, role-based access
4. **Data Layer**: Encryption at rest, TLS in transit
5. **Secrets Management**: Azure Key Vault

### Compliance Requirements

- **PCI DSS**: For card payment processing
- **POPIA**: South African privacy law
- **FICA**: Financial Intelligence Centre Act
- **SARB**: South African Reserve Bank regulations

## Scalability & Performance

### Horizontal Scaling
- Kubernetes auto-scaling based on CPU/memory
- Service Bus partitioning for parallel processing

### Caching Strategy
- Redis for:
  - Account balance lookups
  - Validation rules
  - Routing tables
  - Session management

### Performance Targets
- Payment initiation: < 200ms (p95)
- End-to-end payment (RTC): < 10 seconds
- Throughput: 10,000 TPS (transactions per second)
- Availability: 99.95% uptime

## Disaster Recovery

- **RTO (Recovery Time Objective)**: 1 hour
- **RPO (Recovery Point Objective)**: 5 minutes
- **Multi-Region**: Primary (South Africa North), Secondary (South Africa West)
- **Backup**: Automated daily backups, 30-day retention
- **Failover**: Automated failover for critical services

## Development Workflow for AI Agents

### Phase 1: Foundation (Agent Team Alpha)
- Agent 1: API Gateway skeleton
- Agent 2: Event Bus setup
- Agent 3: Database schemas
- Agent 4: Common libraries (error handling, logging)

### Phase 2: Core Services (Agent Team Beta)
- Agent 5: Payment Initiation Service
- Agent 6: Validation Service
- Agent 7: Account Service
- Agent 8: Routing Service
- Agent 9: Transaction Processing Service

### Phase 3: Integration (Agent Team Gamma)
- Agent 10: Clearing Adapter (SAMOS)
- Agent 11: Clearing Adapter (BankservAfrica)
- Agent 12: Clearing Adapter (RTC)
- Agent 13: Settlement Service

### Phase 4: Supporting Services (Agent Team Delta)
- Agent 14: Notification Service
- Agent 15: Reporting Service
- Agent 16: Reconciliation Service
- Agent 17: Saga Orchestrator

### Phase 5: Frontend (Agent Team Epsilon)
- Agent 18: Payment initiation UI
- Agent 19: Transaction history UI
- Agent 20: Reporting dashboard UI
- Agent 21: Admin console UI

### Phase 6: Consolidation (Master Agent)
- Integration testing
- End-to-end flow validation
- Performance testing
- Documentation consolidation

## Next Steps

1. Review and approve assumptions (see ASSUMPTIONS.md)
2. Detailed API contract design (see API-CONTRACTS.md)
3. Event schema definitions (see EVENT-SCHEMAS.md)
4. Database schema design (see DATABASE-SCHEMAS.md)
5. AI agent task breakdown (see AI-AGENT-TASKS.md)
6. Infrastructure as Code (see INFRASTRUCTURE.md)
