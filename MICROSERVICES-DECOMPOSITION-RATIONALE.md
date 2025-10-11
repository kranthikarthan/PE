# Microservices Decomposition Rationale

## Why 17 Microservices? 🤔

This document explains the **rationale, principles, and decision-making process** behind decomposing the Payments Engine into **17 microservices** (not 5, not 50, but exactly 17).

---

## 🎯 Core Decomposition Principles

### 1. Domain-Driven Design (DDD) - Bounded Contexts

**Principle**: Each microservice should represent a **distinct bounded context** with clear business boundaries.

```
Bounded Context Definition:
- A bounded context is a logical boundary within which a particular domain model is defined and applicable
- Each context has its own ubiquitous language
- Context boundaries align with business capabilities

Example: Payment Processing Context
├─ Has its own definition of "Payment" (domain model)
├─ Uses terms like "initiate", "validate", "process", "complete"
├─ Owns payment lifecycle state machine
└─ Independent of how accounts or notifications define their models

Anti-Pattern: One Large Context ❌
├─ All business logic in one service
├─ Shared database across all domains
├─ Tight coupling between unrelated concepts
└─ Cannot scale or deploy independently
```

### 2. Business Capabilities Mapping

**Principle**: Services align with **distinct business capabilities** that the organization provides.

```
Business Capability Hierarchy:

Level 1: Core Business Functions
├─ Payment Processing (accept & process payments)
├─ Account Management (manage customer accounts)
├─ Clearing & Settlement (interact with banks)
└─ Customer Management (tenant & customer data)

Level 2: Supporting Functions
├─ Validation & Compliance (fraud, limits, rules)
├─ Routing & Orchestration (payment routing logic)
├─ Reporting & Analytics (business intelligence)
└─ Notifications (customer communication)

Level 3: Infrastructure Functions
├─ API Gateway (external interface)
├─ Saga Orchestration (distributed transactions)
└─ Tenant Management (multi-tenancy)

Each Level 1-2 capability = 1 microservice ✅
```

### 3. Single Responsibility Principle (SRP)

**Principle**: Each service should have **one reason to change**.

```
Example: Payment Service

Responsibilities: ✅ (Single, cohesive)
├─ Initiate payment
├─ Track payment status
├─ Manage payment lifecycle
└─ Store payment data

Does NOT handle: ✅ (Delegated to other services)
├─ Account balance validation → Account Adapter Service
├─ Fraud checks → Fraud Detection Service
├─ Clearing submission → Clearing Service
└─ Customer notifications → Notification Service

Why? Each of these has DIFFERENT reasons to change:
- Payment logic changes when payment rules change
- Fraud logic changes when fraud patterns change
- Clearing logic changes when clearing systems change
- Notification logic changes when communication channels change

If all in one service: Every change impacts the entire service ❌
```

### 4. Independent Deployability

**Principle**: Services should be **independently deployable** without affecting others.

```
Scenario: Update Fraud Detection Algorithm

With 17 Services: ✅
├─ Deploy ONLY Fraud Detection Service
├─ Other 16 services: Unchanged
├─ Risk: Isolated to fraud detection
└─ Rollback: Only fraud service

With 1 Monolith: ❌
├─ Deploy ENTIRE application
├─ Risk: All features affected
├─ Rollback: Entire application
└─ Testing: Regression test everything

Independent deployment enables:
✅ Faster release cycles (deploy fraud changes without waiting for payment features)
✅ Lower risk (blast radius contained)
✅ Easier rollback (only one service)
✅ Parallel development (teams work independently)
```

### 5. Team Autonomy

**Principle**: Each service should be **owned by a small, autonomous team** (2-pizza rule).

```
Service Ownership Model:

Payment Service Team (3-5 engineers):
├─ Owns: Payment Service, Transaction Service
├─ Responsible for: Payment processing logic, SLAs
├─ Can deploy: Independently (no dependency on other teams)
└─ Expertise: Payment domain knowledge

Validation Service Team (3-5 engineers):
├─ Owns: Validation Service, Fraud Detection, Limit Management
├─ Responsible for: Compliance, fraud prevention
├─ Can deploy: Independently
└─ Expertise: Fraud detection, regulatory compliance

If services > teams can handle:
├─ Problem: Cognitive overload, context switching
├─ Impact: Slower development, bugs
└─ Solution: Right-size services to team capacity

Optimal: 1-2 services per team ✅
```

### 6. Data Ownership

**Principle**: Each service should **own its data** (no shared databases).

```
Database-per-Service Pattern:

Payment Service:
├─ Owns: payments table
├─ Schema: payment_id, amount, status, created_at
└─ Access: Only Payment Service can write/read

Account Adapter Service:
├─ Owns: account_adapter_cache table
├─ Schema: account_number, balance_cache, last_updated
└─ Access: Only Account Adapter can write/read

Why NOT shared database? ❌
├─ Tight coupling (schema changes break multiple services)
├─ Deployment dependency (need coordination)
├─ Performance contention (services compete for DB resources)
└─ Transaction boundaries (distributed transactions needed)

Database-per-service benefits: ✅
├─ Schema independence (change DB schema without affecting others)
├─ Technology choice (Payment uses PostgreSQL, Audit uses CosmosDB)
├─ Scalability (scale databases independently)
└─ Failure isolation (Payment DB down ≠ Account DB down)
```

### 7. Scalability Requirements

**Principle**: Services with **different scalability needs** should be separate.

```
Scalability Comparison:

Payment Service:
├─ Load: 50,000 req/sec (high volume)
├─ Replicas: 10-30 pods (auto-scaling)
├─ Resources: CPU-intensive (payment processing)
└─ Scaling trigger: Request rate

Reporting Service:
├─ Load: 50 req/sec (low volume, batch queries)
├─ Replicas: 2-3 pods (static)
├─ Resources: Memory-intensive (data aggregation)
└─ Scaling trigger: Query complexity

If combined in one service: ❌
├─ Over-provision: Reporting doesn't need 30 pods
├─ Under-provision: Payment needs more than 3 pods
├─ Waste: Resources allocated but not used
└─ Cost: 10x higher infrastructure cost

Separate services: ✅
├─ Payment: Scales to 30 pods during peak
├─ Reporting: Stays at 2-3 pods
├─ Cost: Optimal (pay for what you need)
└─ Performance: Each scales independently
```

### 8. Technology Heterogeneity

**Principle**: Services can use **different technologies** based on their needs.

```
Technology Choices Per Service:

Payment Service:
├─ Language: Java (Spring Boot)
├─ Database: PostgreSQL (transactional)
├─ Reason: Strong typing, ACID transactions
└─ Reactive: Spring WebFlux (high throughput)

Notification Service:
├─ Language: Node.js (Express)
├─ Database: Redis (fast queue)
├─ Reason: Non-blocking I/O, lightweight
└─ Event-driven: Kafka consumer

Reporting Service:
├─ Language: Python (Pandas)
├─ Database: Azure Synapse (analytics)
├─ Reason: Data science libraries, SQL analytics
└─ Batch: Scheduled jobs

If monolith: ❌
├─ One language for all (Java or Python, not both)
├─ One database type (PostgreSQL for analytics = slow)
├─ Sub-optimal: Wrong tool for some jobs
└─ Technical debt: Forced to use wrong technology

Microservices: ✅
├─ Right tool for each job
├─ Team expertise (Java team for payments, Python for analytics)
└─ Best-in-class: Each service optimized
```

---

## 📊 The 17 Services Breakdown

### Why Exactly 17? (Not 5, Not 50)

**Answer**: Because there are **17 distinct business capabilities** with clear boundaries.

```
Decision Framework:

Too Few Services (e.g., 5 services): ❌
├─ Problem: Each service too large (low cohesion)
├─ Example: "Payment Service" handles payments + validation + clearing + reporting
├─ Impact:
│  ├─ Tight coupling (changes affect multiple capabilities)
│  ├─ Large teams needed (10+ engineers per service)
│  ├─ Slow deployment (risky, large blast radius)
│  └─ Cannot scale independently (over-provision resources)
└─ Result: Distributed Monolith

Too Many Services (e.g., 50 services): ❌
├─ Problem: Over-decomposition (high operational overhead)
├─ Example: Separate services for "InitiatePayment", "ValidatePayment", "ProcessPayment"
├─ Impact:
│  ├─ Network chattiness (too many inter-service calls)
│  ├─ Distributed transaction complexity (Saga hell)
│  ├─ Operational overhead (50 services to monitor, deploy, debug)
│  └─ Team coordination nightmare (which team owns what?)
└─ Result: Microservices Hell

Just Right (17 services): ✅
├─ Each service: Single business capability
├─ High cohesion: Related logic grouped together
├─ Low coupling: Services communicate via well-defined APIs/events
├─ Right size: 3-5 engineers can own 1-2 services
├─ Scalability: Each service scales independently
└─ Balance: Manageable complexity, operational overhead
```

---

## 🔍 Service-by-Service Rationale

### **Core Services (8 Services)** - Payment Processing Domain

#### 1. Payment Initiation Service
**Business Capability**: Accept and initiate payment requests  
**Why Separate?**
- **High volume**: 50K req/sec (needs independent scaling)
- **Critical path**: Customer-facing (99.99% SLA)
- **Reactive**: Needs Spring WebFlux for throughput
- **Reason to change**: Payment initiation rules, payment types

**Data Owned**: `payments` table (payment_id, amount, status, created_at)

**Would it work combined with Payment Processing Service?** ❌
- Different scaling needs (initiation >> processing)
- Different SLAs (initiation = customer-facing, processing = internal)
- Blast radius (initiation failure shouldn't stop processing)

---

#### 2. Payment Validation Service
**Business Capability**: Validate payments against business rules  
**Why Separate?**
- **Complex logic**: Fraud, limits, account validation (different domain)
- **Frequent changes**: Fraud rules change weekly
- **Team expertise**: Compliance/fraud team (not payment team)
- **Reason to change**: New fraud patterns, regulatory changes

**Data Owned**: `validation_rules` table, `fraud_scores` cache

**Would it work combined with Payment Initiation?** ❌
- Different rate of change (validation rules >> payment initiation)
- Different team (fraud team vs payment team)
- Independent deployment (fraud rule update shouldn't redeploy payment initiation)

---

#### 3. Payment Processing Service
**Business Capability**: Execute core payment processing logic  
**Why Separate?**
- **Business critical**: Core payment engine
- **Stateful**: Manages payment state machine
- **Complex orchestration**: Coordinates with multiple services
- **Reason to change**: Payment processing rules, workflows

**Data Owned**: `payment_processing` table (state transitions, processing logs)

**Why not merge with Payment Initiation?** ❌
- Initiation = synchronous (fast response)
- Processing = asynchronous (can take seconds/minutes)
- Different performance profiles

---

#### 4. Account Adapter Service
**Business Capability**: Integrate with external core banking systems  
**Why Separate?**
- **External dependency**: Calls 8+ core banking systems
- **Circuit breaker**: Needs isolation (external failures shouldn't affect payments)
- **Caching**: Needs aggressive caching (reduce external API calls)
- **Reason to change**: New core banking system, API changes
- **Reactive**: High volume, non-blocking I/O

**Data Owned**: `account_cache` table, `core_banking_endpoints` config

**Why not part of Payment Service?** ❌
- External integration complexity (different failure modes)
- Circuit breaker isolation (failures contained)
- Independent scaling (external API limits)
- Technology choice (needs reactive, non-blocking)

---

#### 5. Routing Service
**Business Capability**: Route payments to appropriate clearing systems  
**Why Separate?**
- **Business logic**: Complex routing rules (SAMOS, BankservAfrica, SASWITCH)
- **Frequent changes**: Routing rules change (new banks, new clearing systems)
- **Team expertise**: Clearing integration team
- **Reason to change**: New clearing system, routing rule updates

**Data Owned**: `routing_rules` table, `clearing_endpoints` config

**Why not merge with Clearing Service?** ❌
- Routing = decision logic (which clearing system?)
- Clearing = execution logic (submit to clearing system)
- Different rate of change (routing rules >> clearing integration)

---

#### 6. Clearing Service
**Business Capability**: Submit payments to clearing systems (SAMOS, BankservAfrica, etc.)  
**Why Separate?**
- **External integration**: 3+ clearing systems (SAMOS, BankservAfrica, SASWITCH)
- **Protocol complexity**: ISO 20022, ISO 8583
- **Circuit breaker**: External failures isolated
- **Reason to change**: Clearing system API changes, new clearing systems

**Data Owned**: `clearing_submissions` table, `clearing_responses` table

**Why not part of Routing Service?** ❌
- Routing = decision (which system?)
- Clearing = execution (submit to system)
- Different failure modes (routing logic vs network failures)
- Independent deployment (clearing protocol update doesn't affect routing)

---

#### 7. Transaction Service
**Business Capability**: Record and track transaction history  
**Why Separate?**
- **Data-intensive**: Stores all transaction records (billions of rows)
- **Audit trail**: Immutable log (cannot be mixed with mutable payment data)
- **Query pattern**: Read-heavy (reporting queries)
- **Reason to change**: Compliance requirements, retention policies

**Data Owned**: `transactions` table (immutable audit log)

**Why not part of Payment Service?** ❌
- Different data model (immutable vs mutable)
- Different query patterns (write-heavy vs read-heavy)
- Compliance separation (audit trail must be isolated)
- Database optimization (separate indexes for queries)

---

#### 8. Saga Orchestrator Service
**Business Capability**: Orchestrate distributed transactions and compensation  
**Why Separate?**
- **Cross-cutting**: Coordinates multiple services (payment, account, clearing)
- **Compensation logic**: Rollback logic (different from forward flow)
- **Reason to change**: New saga patterns, new compensation workflows

**Data Owned**: `saga_instances` table, `saga_steps` table

**Why not distributed across services?** ❌
- Centralized orchestration (single view of saga state)
- Simpler debugging (one place to look)
- Easier to add new sagas (no changes to existing services)

---

### **Supporting Services (6 Services)** - Cross-Cutting Concerns

#### 9. Limit Management Service
**Business Capability**: Enforce customer payment limits  
**Why Separate?**
- **Stateful**: Tracks used limits (concurrency-sensitive)
- **Real-time**: Needs immediate consistency (limit reservation)
- **Reason to change**: New limit types, limit policies

**Data Owned**: `customer_limits` table, `limit_usage` table

**Why not part of Validation Service?** ❌
- Different data consistency needs (limits need ACID transactions)
- Independent scaling (limit checks = high volume)
- Separate team (risk management team)

---

#### 10. Fraud Detection Service
**Business Capability**: Detect and prevent fraudulent payments  
**Why Separate?**
- **ML models**: Fraud scoring algorithms (Python)
- **External API**: Calls fraud scoring service
- **Frequent updates**: Models retrained weekly
- **Reason to change**: New fraud patterns, model updates

**Data Owned**: `fraud_scores` cache, `fraud_rules` table

**Why not part of Validation Service?** ❌
- Different technology (Python ML vs Java validation)
- Different rate of change (models updated weekly)
- Separate team (data science team vs compliance team)

---

#### 11. Notification Service
**Business Capability**: Send notifications to customers (SMS, email, push)  
**Why Separate?**
- **Non-critical**: Notification failure shouldn't fail payment
- **Asynchronous**: Event-driven (Kafka consumer)
- **High volume**: 80K notifications/sec (bulk processing)
- **Reason to change**: New notification channels, templates
- **Reactive**: Non-blocking I/O for high throughput

**Data Owned**: `notification_queue` table, `notification_history` table

**Why not part of Payment Service?** ❌
- Different priority (payment critical, notification non-critical)
- Different failure mode (notification retry doesn't affect payment)
- Independent scaling (notifications >> payments)

---

#### 12. Notification Queue Service
**Business Capability**: Queue and prioritize notifications  
**Why Separate from Notification Service?**
- **Queue management**: Prioritization, throttling, batching
- **Buffer**: Protects Notification Service from overload
- **Reason to change**: Queue policies, prioritization rules

**Data Owned**: `notification_priority_queue` table

**Why separate from Notification Service?** 
- **Decoupling**: Queue management logic vs sending logic
- **Scalability**: Queue can buffer during notification service downtime
- **Rate limiting**: Throttle based on provider limits (SMS provider = 100 req/sec)

---

#### 13. Reporting Service
**Business Capability**: Generate business reports and analytics  
**Why Separate?**
- **Read-only**: Queries data (no writes)
- **Resource-intensive**: Complex SQL queries (memory/CPU heavy)
- **Different database**: Azure Synapse (optimized for analytics)
- **Technology**: Python (Pandas for data processing)
- **Reason to change**: New reports, dashboard changes

**Data Owned**: Read replicas of payment, transaction data (Azure Synapse)

**Why not part of Transaction Service?** ❌
- Different database (OLTP vs OLAP)
- Different query patterns (simple vs complex aggregations)
- Resource isolation (reporting queries shouldn't slow down payments)

---

#### 14. Audit Service
**Business Capability**: Maintain immutable audit logs for compliance  
**Why Separate?**
- **Compliance**: Regulatory requirement (7-year retention)
- **Immutable**: Append-only (no updates/deletes)
- **Different database**: CosmosDB (optimized for append-only, long-term storage)
- **Reason to change**: Compliance requirements, retention policies

**Data Owned**: `audit_logs` table (CosmosDB, 7-year retention)

**Why not part of Transaction Service?** ❌
- Different database technology (CosmosDB vs PostgreSQL)
- Different retention (7 years vs 90 days)
- Compliance isolation (audit cannot be tampered with)

---

#### 15. Tenant Management Service
**Business Capability**: Manage multi-tenant configuration  
**Why Separate?**
- **Cross-cutting**: All services need tenant context
- **Configuration**: Tenant-specific settings (limits, fraud rules, clearing endpoints)
- **Reason to change**: New tenant onboarding, tenant configuration updates

**Data Owned**: `tenants` table, `business_units` table, `tenant_config` table

**Why not distributed across services?** ❌
- Centralized tenant management (single source of truth)
- Consistent tenant context (all services query same data)
- Simpler onboarding (one service to update)

---

### **API Services (2 Services)** - External Interface

#### 16. BFF - Web/Mobile/Partner API
**Business Capability**: Backend for Frontend (3 BFFs for different clients)  
**Why Separate from Payment Service?**
- **Client-specific**: Web (GraphQL), Mobile (REST lightweight), Partner (REST comprehensive)
- **API aggregation**: Combines data from multiple services
- **Security**: API gateway authentication/authorization
- **Reason to change**: Frontend requirements, API versioning

**Why 3 BFFs instead of 1?**
- Web: Needs rich queries (GraphQL)
- Mobile: Needs lightweight responses (battery, bandwidth)
- Partner: Needs comprehensive data (B2B integration)

---

#### 17. API Gateway Facade
**Business Capability**: External API gateway (Kong)  
**Why Separate?**
- **Security**: Rate limiting, authentication, authorization
- **Routing**: Route requests to appropriate BFF/service
- **Cross-cutting**: Logging, monitoring, tracing
- **Reason to change**: Security policies, rate limits

---

## 📊 Decomposition Analysis

### Service Size Analysis

```
Service Complexity Distribution:

Simple Services (3-5 endpoints):
├─ Tenant Management (4 endpoints)
├─ Limit Management (5 endpoints)
└─ Notification Queue (3 endpoints)

Medium Services (6-10 endpoints):
├─ Payment Initiation (8 endpoints)
├─ Validation (7 endpoints)
├─ Fraud Detection (6 endpoints)
├─ Notification (9 endpoints)
└─ Reporting (10 endpoints)

Complex Services (11-15 endpoints):
├─ Payment Processing (12 endpoints)
├─ Account Adapter (15 endpoints)
├─ Routing (11 endpoints)
├─ Clearing (13 endpoints)
└─ Transaction (14 endpoints)

Very Complex Services (16+ endpoints):
├─ Saga Orchestrator (18 endpoints)
├─ Audit (16 endpoints)
└─ BFF (20+ endpoints, GraphQL)

Average: 10 endpoints per service ✅
```

### Team-to-Service Mapping

```
Team Structure (5 teams, 17 services):

Team 1: Payment Team (4 engineers)
├─ Owns: Payment Initiation, Payment Processing, Transaction
├─ Services: 3
└─ Expertise: Payment domain

Team 2: Validation & Compliance Team (4 engineers)
├─ Owns: Validation, Fraud Detection, Limit Management
├─ Services: 3
└─ Expertise: Compliance, fraud

Team 3: Integration Team (4 engineers)
├─ Owns: Account Adapter, Routing, Clearing
├─ Services: 3
└─ Expertise: External integrations

Team 4: Platform Team (3 engineers)
├─ Owns: Saga Orchestrator, Audit, Tenant Management
├─ Services: 3
└─ Expertise: Infrastructure, multi-tenancy

Team 5: User Experience Team (3 engineers)
├─ Owns: BFF (3 BFFs), Notification, Notification Queue, Reporting
├─ Services: 5
└─ Expertise: Frontend, API design

Average: 3.4 services per team ✅
```

---

## ⚖️ Trade-offs Analysis

### Advantages of 17 Services

✅ **High Cohesion**: Each service has single responsibility  
✅ **Low Coupling**: Services communicate via APIs/events  
✅ **Independent Deployment**: Deploy 1 service without affecting others  
✅ **Independent Scaling**: Scale high-volume services (Payment) independently  
✅ **Team Autonomy**: Each team owns 2-4 services  
✅ **Technology Diversity**: Use best tool for each job  
✅ **Failure Isolation**: Blast radius contained (max 1 service)  
✅ **Clear Ownership**: No ambiguity (who owns what?)  

### Disadvantages of 17 Services

❌ **Distributed Complexity**: Network calls, latency, failures  
❌ **Operational Overhead**: 17 services to monitor, deploy, debug  
❌ **Distributed Transactions**: Saga pattern needed  
❌ **Data Consistency**: Eventual consistency (not immediate)  
❌ **Testing Complexity**: Integration testing harder  
❌ **Infrastructure Cost**: 17 deployments (10 pods each = 170 pods minimum)  

### Is It Worth It? ✅ YES

For a **multi-tenant, multi-bank, hyperscale payments platform** serving **100+ banks** with **875K+ req/sec**:

✅ Benefits >> Costs  
✅ Operational complexity is managed (Kubernetes, Istio, GitOps)  
✅ Independent scaling saves more than infrastructure overhead  
✅ Failure isolation prevents catastrophic outages  
✅ Team autonomy accelerates development (5 teams work in parallel)  

---

## 🎯 Key Takeaways

### Why 17 Services?

1. **Domain-Driven**: 17 distinct bounded contexts identified
2. **Business-Aligned**: Each service = 1 business capability
3. **Right-Sized**: Not too few (5), not too many (50)
4. **Team-Sized**: 5 teams can own 17 services (3.4 services per team)
5. **Independently Scalable**: High-volume services scale independently
6. **Data-Owned**: Each service owns its data (14 databases)
7. **Technology-Diverse**: Use best tool for each job (Java, Python, Node.js)

### The Golden Rule

**"If in doubt, keep it together. Split only when you have a clear reason."**

We split into 17 services because we have **17 clear reasons** (17 bounded contexts with distinct business capabilities, scaling needs, and rates of change).

### Alternative Approaches (Why We Didn't Choose Them)

**5 Services (Macro-Services)**: ❌
- Too large, low cohesion
- Cannot scale independently
- Distributed monolith

**50 Services (Nano-Services)**: ❌
- Over-decomposition
- Network chattiness
- Operational nightmare

**17 Services (Goldilocks)**: ✅
- Just right
- High cohesion, low coupling
- Manageable complexity

---

## 📚 Further Reading

- **Domain-Driven Design** (Eric Evans) - Bounded contexts
- **Building Microservices** (Sam Newman) - Service decomposition
- **Microservices Patterns** (Chris Richardson) - Service boundaries
- **Team Topologies** (Matthew Skelton) - Team-to-service mapping

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Internal
