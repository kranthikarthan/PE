# AI Agent Prompt Templates - Complete Context per Feature

## Overview

This document provides **detailed prompt templates** for each of the **40+ features**, ensuring each AI agent has sufficient context to complete:

1. **HLD (High-Level Design)** - Architecture overview
2. **LLD (Low-Level Design)** - Detailed design, class diagrams
3. **Implementation** - Complete working code
4. **Unit Testing** - 80%+ test coverage
5. **Documentation** - README, API docs, setup guide

**Purpose**: Enable any AI coding agent to independently build a feature with minimal clarification needed.

---

## Table of Contents

1. [Prompt Template Structure](#prompt-template-structure)
2. [Context Verification Checklist](#context-verification-checklist)
3. [Phase 0: Foundation (5 Features)](#phase-0-foundation-5-features)
4. [Phase 1: Core Services (6 Features)](#phase-1-core-services-6-features)
5. [Phase 2: Clearing Adapters (5 Features)](#phase-2-clearing-adapters-5-features)
6. [Phase 3: Platform Services (5 Features)](#phase-3-platform-services-5-features)
7. [Phase 4: Advanced Features (5 Features)](#phase-4-advanced-features-5-features)
8. [Phase 5: Infrastructure (5 Features)](#phase-5-infrastructure-5-features)
9. [Phase 6: Testing (5 Features)](#phase-6-testing-5-features)
10. [Context Sufficiency Analysis](#context-sufficiency-analysis)

---

## Prompt Template Structure

Each agent prompt follows this structure:

```yaml
Feature ID: [Phase.Number]
Feature Name: [Name]
Agent Name: [Specialized Agent]
Phase: [0-6]
Estimated Time: [X days]

Role & Expertise:
  You are a [role] with expertise in [technologies].

Task:
  Build [feature description] for the Payments Engine.

Context Provided:
  1. Architecture Documents:
     - [List of docs with specific sections/line numbers]
  2. Schemas:
     - Database schemas
     - Event schemas (AsyncAPI)
     - Domain models
  3. Dependencies:
     - Interfaces of dependent services
     - Shared libraries
  4. Examples:
     - Reference implementations
     - Code samples

Expected Deliverables:
  1. HLD (High-Level Design):
     - Architecture diagram
     - Component interactions
     - Technology choices
  
  2. LLD (Low-Level Design):
     - Class diagrams
     - Sequence diagrams
     - Database schema (if applicable)
     - API contracts (OpenAPI/AsyncAPI)
  
  3. Implementation:
     - Complete source code
     - Configuration files
     - Dockerfile
     - Kubernetes manifests
  
  4. Unit Testing:
     - 80%+ code coverage
     - Unit tests (JUnit)
     - Integration tests
     - Test data setup
  
  5. Documentation:
     - README.md (setup, run, test)
     - API documentation
     - Environment variables
     - Troubleshooting guide

Success Criteria:
  - [ ] All tests pass (green CI/CD)
  - [ ] Code coverage ≥ 80%
  - [ ] SonarQube quality gate passes
  - [ ] Docker image builds successfully
  - [ ] Service deploys to AKS
  - [ ] Health check endpoint responds
  - [ ] API endpoints functional
  - [ ] Documentation complete

Validation Checklist:
  - [ ] HLD reviewed and approved
  - [ ] LLD reviewed and approved
  - [ ] Code review passed
  - [ ] Tests reviewed and passing
  - [ ] Documentation reviewed
  - [ ] Security scan passed
  - [ ] Performance benchmarks met
```

---

## Context Verification Checklist

For each feature, verify the following context is provided:

### ✅ Architecture Context
- [ ] High-level architecture diagram
- [ ] Service interactions
- [ ] Data flow
- [ ] Technology stack

### ✅ Design Context
- [ ] Database schema (if applicable)
- [ ] Event schemas (if event-driven)
- [ ] API contracts (REST/GraphQL/gRPC)
- [ ] Domain models

### ✅ Implementation Context
- [ ] Code structure/skeleton
- [ ] Shared libraries to use
- [ ] Configuration examples
- [ ] Error handling patterns

### ✅ Testing Context
- [ ] Test strategy
- [ ] Test data requirements
- [ ] Mock/stub specifications
- [ ] Performance benchmarks

### ✅ Deployment Context
- [ ] Dockerfile template
- [ ] Kubernetes manifest templates
- [ ] Environment variables
- [ ] Secrets/ConfigMaps

### ✅ Integration Context
- [ ] Dependent service interfaces
- [ ] External API specifications
- [ ] Event contracts
- [ ] Authentication/authorization

---

## Phase 0: Foundation (5 Features)

### Feature 0.1: Database Schemas

```yaml
Feature ID: 0.1
Feature Name: Database Schemas
Agent Name: Database Schema Agent
Phase: 0 (Foundation)
Estimated Time: 3 days

Role & Expertise:
  You are a Database Architect with expertise in PostgreSQL, database design,
  normalization, indexing, partitioning, and Row-Level Security (RLS).

Task:
  Design and implement complete database schemas for all 20 microservices in
  the Payments Engine, including tables, indexes, constraints, and RLS policies.

Context Provided:

  1. Architecture Documents (READ THESE FIRST):
     📄 docs/05-DATABASE-SCHEMAS.md (COMPLETE FILE - 2,500 lines)
        - All table definitions
        - Indexes and constraints
        - Row-Level Security policies
        - Tenant isolation patterns
     
     📄 docs/12-TENANT-MANAGEMENT.md (Sections: Tenant Data Model)
        - Tenant hierarchy (Tenant → Business Unit → Customer)
        - tenant_id propagation
     
     📄 docs/14-DDD-IMPLEMENTATION.md (Sections: Aggregates, Entities)
        - Domain model → table mapping
        - Aggregate boundaries
  
  2. Requirements:
     - PostgreSQL 15
     - Multi-tenancy support (RLS)
     - All tables must have tenant_id
     - Audit columns (created_at, updated_at, created_by, updated_by)
     - Soft deletes (is_deleted)
     - Optimistic locking (version)
  
  3. Services Requiring Databases:
     Service #1:  Payment Initiation (payments table)
     Service #2:  Validation (validation_rules, limit_checks)
     Service #3:  Account Adapter (account_routing)
     Service #4:  Routing (routing_rules)
     Service #5:  Transaction Processing (transactions)
     Service #6:  Saga Orchestrator (saga_state)
     Service #7:  SAMOS Adapter (samos_submissions)
     Service #8:  BankservAfrica Adapter (bankserv_submissions)
     Service #9:  RTC Adapter (rtc_submissions)
     Service #10: PayShap Adapter (payshap_submissions)
     Service #11: SWIFT Adapter (swift_submissions)
     Service #12: Batch Processing (batch_jobs)
     Service #13: Settlement (settlements)
     Service #14: Reconciliation (reconciliation_records)
     Service #15: Tenant Management (tenants, business_units)
     Service #17: Reporting (reports)
     Service #19: IAM (users, roles, permissions)
     Service #20: Audit (audit_log - CosmosDB, NOT PostgreSQL)
  
  4. Constraints:
     - All tables MUST have PRIMARY KEY
     - All foreign keys MUST have indexes
     - All tenant_id columns MUST be NOT NULL (except tenant table itself)
     - All timestamp columns MUST use TIMESTAMP WITH TIME ZONE
     - All monetary amounts MUST use DECIMAL(19,4)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 ER Diagram (Entity-Relationship)
        - All tables across all 20 services
        - Relationships (foreign keys)
        - Cardinality (1:1, 1:N, N:M)
     
     📊 Database Distribution Strategy
        - Database per service (20 PostgreSQL databases)
        - Shared vs separate schemas
        - Connection pooling strategy
  
  2. LLD (Low-Level Design):
     📋 Table Definitions (DDL)
        - CREATE TABLE statements
        - Column types, constraints, defaults
        - Primary keys, foreign keys
     
     📋 Index Strategy
        - Primary indexes
        - Secondary indexes (for queries)
        - Composite indexes
        - Partial indexes
     
     📋 RLS Policies
        - Row-Level Security per table
        - Tenant isolation
        - Policy definitions
     
     📋 Partitioning Strategy (if applicable)
        - Large tables (payments, transactions, audit_log)
        - Partition by date (monthly/yearly)
  
  3. Implementation:
     📁 /database/migrations/ (Flyway)
        ├─ V001__create_payment_tables.sql
        ├─ V002__create_validation_tables.sql
        ├─ V003__create_account_adapter_tables.sql
        ├─ V004__create_routing_tables.sql
        ├─ V005__create_transaction_tables.sql
        ├─ V006__create_saga_tables.sql
        ├─ V007__create_samos_tables.sql
        ├─ V008__create_bankserv_tables.sql
        ├─ V009__create_rtc_tables.sql
        ├─ V010__create_payshap_tables.sql
        ├─ V011__create_swift_tables.sql
        ├─ V012__create_batch_tables.sql
        ├─ V013__create_settlement_tables.sql
        ├─ V014__create_reconciliation_tables.sql
        ├─ V015__create_tenant_tables.sql
        ├─ V016__create_reporting_tables.sql
        ├─ V017__create_iam_tables.sql
        ├─ V100__create_indexes.sql
        ├─ V101__enable_rls.sql
        └─ V102__create_partitions.sql
     
     📁 /database/seed-data/
        ├─ R__seed_tenants.sql (repeatable)
        ├─ R__seed_validation_rules.sql
        └─ R__seed_routing_rules.sql
     
     📁 /database/test-data/
        ├─ test_payments.sql
        ├─ test_accounts.sql
        └─ test_tenants.sql
  
  4. Unit Testing:
     ✅ Schema Validation Tests
        - All tables created successfully
        - All constraints enforced
        - RLS policies active
     
     ✅ Data Integrity Tests
        - Foreign key constraints work
        - Check constraints enforced
        - Unique constraints work
     
     ✅ RLS Tests
        - Tenant isolation works
        - Cross-tenant access blocked
        - Admin bypass works
     
     ✅ Performance Tests
        - Indexes improve query performance
        - Large inserts acceptable (<1s for 1000 rows)
        - Queries use indexes (EXPLAIN ANALYZE)
     
     📁 /database/tests/
        ├─ test_schema_creation.sql
        ├─ test_rls_policies.sql
        ├─ test_data_integrity.sql
        └─ test_query_performance.sql
  
  5. Documentation:
     📄 README.md
        - Database architecture overview
        - How to run migrations locally
        - How to seed data
        - How to run tests
     
     📄 SCHEMA-REFERENCE.md
        - Complete table reference
        - Column descriptions
        - Index strategy
        - RLS policies
     
     📄 MIGRATION-GUIDE.md
        - How to create new migrations
        - How to rollback
        - Best practices

Success Criteria:
  ✅ All migrations run successfully (Flyway)
  ✅ All tables created with correct schema
  ✅ All indexes created
  ✅ RLS enabled and tested
  ✅ Seed data loads successfully
  ✅ All schema tests pass (100%)
  ✅ Query performance acceptable (<100ms for simple queries)
  ✅ Documentation complete

Validation Checklist:
  - [ ] ER diagram reviewed
  - [ ] All 17 service databases defined
  - [ ] All tables have tenant_id (except tenant table)
  - [ ] All foreign keys have indexes
  - [ ] RLS policies defined for all tables
  - [ ] Migrations tested (up and down)
  - [ ] Performance tested (explain analyze)
  - [ ] Documentation reviewed

Context Sufficiency: ✅ SUFFICIENT
  - Complete schema definitions in docs/05-DATABASE-SCHEMAS.md
  - Tenant model in docs/12-TENANT-MANAGEMENT.md
  - Domain model in docs/14-DDD-IMPLEMENTATION.md
  - Clear requirements and constraints
  - Examples and templates provided
```

---

### Feature 0.2: Event Schemas (AsyncAPI)

```yaml
Feature ID: 0.2
Feature Name: Event Schemas (AsyncAPI)
Agent Name: Event Schema Agent
Phase: 0 (Foundation)
Estimated Time: 1 day

Role & Expertise:
  You are an Event-Driven Architecture Specialist with expertise in AsyncAPI,
  message design, event sourcing, and schema evolution.

Task:
  Design and implement complete event schemas for all 25+ events in the
  Payments Engine using AsyncAPI 2.0 specification.

Context Provided:

  1. Architecture Documents:
     📄 docs/03-EVENT-SCHEMAS.md (COMPLETE FILE - 1,200 lines)
        - All event definitions
        - Event payloads (JSON Schema)
        - Event channels (topics/queues)
        - Pub/sub patterns
     
     📄 docs/00-ARCHITECTURE-OVERVIEW.md (Section: Event-Driven Architecture)
        - Event-driven principles
        - Pub/sub topology
     
     📄 docs/29-ENTERPRISE-INTEGRATION-PATTERNS.md (Sections: Event Message)
        - Event message patterns
        - Message structure
  
  2. Requirements:
     - AsyncAPI 2.6.0 specification
     - JSON Schema for payloads
     - CloudEvents format (optional)
     - Versioning strategy (v1, v2)
     - Backward compatibility
  
  3. Event Categories:
     Payment Lifecycle Events:
       - PaymentInitiatedEvent
       - PaymentValidatedEvent
       - PaymentRoutedEvent
       - PaymentProcessedEvent
       - PaymentCompletedEvent
       - PaymentFailedEvent
     
     Clearing Events:
       - ClearingSubmittedEvent
       - ClearingAcknowledgedEvent
       - ClearingCompletedEvent
       - ClearingFailedEvent
     
     Account Events:
       - AccountDebitedEvent
       - AccountCreditedEvent
       - BalanceCheckedEvent
     
     Limit Events:
       - LimitCheckedEvent
       - LimitConsumedEvent
       - LimitReleasedEvent
       - LimitExceededEvent
     
     Fraud Events:
       - FraudCheckInitiatedEvent
       - FraudScoreComputedEvent
       - FraudDetectedEvent
     
     Tenant Events:
       - TenantCreatedEvent
       - TenantUpdatedEvent
       - TenantDeactivatedEvent
     
     Saga Events:
       - SagaStartedEvent
       - SagaCompletedEvent
       - SagaFailedEvent
       - CompensationTriggeredEvent
  
  4. Common Event Structure:
     {
       "eventId": "uuid",
       "eventType": "PaymentInitiatedEvent",
       "eventVersion": "v1",
       "timestamp": "2025-10-12T10:00:00Z",
       "source": "payment-initiation-service",
       "correlationId": "uuid",
       "causationId": "uuid",
       "tenantContext": {
         "tenantId": "BANK-001",
         "businessUnitId": "BU-001",
         "userId": "user-123"
       },
       "payload": {
         // Event-specific data
       }
     }

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Event Catalog
        - All 25+ events listed
        - Event categories
        - Producers and consumers
     
     📊 Event Flow Diagram
        - Event sequence across services
        - Pub/sub topology
  
  2. LLD (Low-Level Design):
     📋 AsyncAPI Specification
        - asyncapi.yaml (master file)
        - All channels defined
        - All messages defined
        - JSON Schema for each event
     
     📋 Event Payload Schemas
        - JSON Schema per event
        - Required vs optional fields
        - Data types and formats
        - Validation rules
     
     📋 Versioning Strategy
        - How to version events (v1, v2)
        - Backward compatibility rules
        - Migration strategy
  
  3. Implementation:
     📁 /events/
        ├─ asyncapi.yaml (AsyncAPI 2.6.0 spec)
        ├─ schemas/
        │   ├─ payment-initiated-event.json
        │   ├─ payment-validated-event.json
        │   ├─ payment-routed-event.json
        │   ├─ payment-processed-event.json
        │   ├─ payment-completed-event.json
        │   ├─ payment-failed-event.json
        │   ├─ clearing-submitted-event.json
        │   ├─ clearing-acknowledged-event.json
        │   ├─ account-debited-event.json
        │   ├─ account-credited-event.json
        │   ├─ limit-checked-event.json
        │   ├─ fraud-score-computed-event.json
        │   ├─ tenant-created-event.json
        │   ├─ saga-started-event.json
        │   └─ ... (all 25+ events)
        ├─ examples/
        │   ├─ payment-initiated-example.json
        │   ├─ payment-validated-example.json
        │   └─ ... (example payloads)
        └─ avro/ (optional - for Kafka)
            ├─ payment-initiated.avsc
            └─ ... (Avro schemas)
  
  4. Unit Testing:
     ✅ Schema Validation Tests
        - All JSON Schemas valid
        - AsyncAPI spec valid
        - Examples validate against schemas
     
     ✅ Backward Compatibility Tests
        - v2 payloads validate against v1 schema (with defaults)
        - No breaking changes
     
     📁 /events/tests/
        ├─ test_schema_validation.js (JSON Schema validator)
        ├─ test_asyncapi_spec.js (AsyncAPI validator)
        └─ test_examples.js (validate examples)
  
  5. Documentation:
     📄 README.md
        - Event schema overview
        - How to use AsyncAPI spec
        - How to validate events
        - How to version events
     
     📄 EVENT-CATALOG.md
        - Complete event reference
        - Event descriptions
        - Payload structure
        - Producers and consumers
     
     📄 VERSIONING-GUIDE.md
        - Versioning strategy
        - How to add new events
        - How to evolve existing events
        - Breaking vs non-breaking changes

Success Criteria:
  ✅ AsyncAPI spec valid (asyncapi validate)
  ✅ All JSON Schemas valid
  ✅ All examples validate against schemas
  ✅ Documentation complete
  ✅ Backward compatibility maintained

Validation Checklist:
  - [ ] All 25+ events defined
  - [ ] JSON Schema per event
  - [ ] AsyncAPI spec validated
  - [ ] Examples provided
  - [ ] Versioning strategy documented
  - [ ] Backward compatibility tested

Context Sufficiency: ✅ SUFFICIENT
  - Complete event list in docs/03-EVENT-SCHEMAS.md
  - Event patterns in docs/29-ENTERPRISE-INTEGRATION-PATTERNS.md
  - Clear structure and requirements
  - Examples provided
```

---

### Feature 0.3: Domain Models

```yaml
Feature ID: 0.3
Feature Name: Domain Models
Agent Name: Domain Model Agent
Phase: 0 (Foundation)
Estimated Time: 2 days

Role & Expertise:
  You are a Domain-Driven Design Expert with expertise in aggregate design,
  value objects, entities, domain events, and bounded contexts.

Task:
  Design and implement complete domain models for all bounded contexts in
  the Payments Engine using DDD principles.

Context Provided:

  1. Architecture Documents:
     📄 docs/14-DDD-IMPLEMENTATION.md (COMPLETE FILE - 2,000 lines)
        - Bounded contexts
        - Aggregates and aggregate roots
        - Entities vs value objects
        - Domain events
        - Repositories
     
     📄 docs/00-ARCHITECTURE-OVERVIEW.md (Section: Domain Model)
        - High-level domain overview
     
     📄 docs/05-DATABASE-SCHEMAS.md (All tables)
        - Database schema (entity mapping)
  
  2. Requirements:
     - Java 17
     - Immutable value objects
     - Aggregate boundaries enforced
     - Rich domain models (not anemic)
     - Domain events for state changes
  
  3. Bounded Contexts:
     1. Payment Context
        - Payment (Aggregate Root)
        - PaymentId (Value Object)
        - Amount (Value Object)
        - PaymentStatus (Enum)
        - PaymentType (Enum)
     
     2. Account Context
        - Account (Aggregate Root)
        - AccountId (Value Object)
        - AccountType (Enum)
        - Balance (Value Object)
     
     3. Validation Context
        - ValidationRule (Entity)
        - ValidationResult (Value Object)
        - RuleType (Enum)
     
     4. Routing Context
        - RoutingRule (Entity)
        - ClearingSystem (Enum)
        - RoutingDecision (Value Object)
     
     5. Transaction Context
        - Transaction (Aggregate Root)
        - TransactionId (Value Object)
        - TransactionStatus (Enum)
     
     6. Clearing Context
        - ClearingSubmission (Aggregate Root)
        - ClearingResponse (Value Object)
     
     7. Settlement Context
        - Settlement (Aggregate Root)
        - SettlementStatus (Enum)
     
     8. Tenant Context
        - Tenant (Aggregate Root)
        - BusinessUnit (Entity)
        - TenantId (Value Object)
  
  4. Domain Model Principles:
     - Aggregates enforce business invariants
     - Value objects are immutable
     - Entities have identity
     - Domain events for state changes
     - No public setters (encapsulation)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Bounded Context Map
        - All 8 bounded contexts
        - Context relationships
        - Shared kernel
        - Anti-corruption layers
     
     📊 Aggregate Diagram
        - Aggregate roots
        - Entities within aggregates
        - Value objects
        - Aggregate boundaries
  
  2. LLD (Low-Level Design):
     📋 Class Diagrams (per bounded context)
        - Aggregate classes
        - Entity classes
        - Value object classes
        - Domain event classes
        - Repository interfaces
     
     📋 Domain Event Catalog
        - Events raised by aggregates
        - Event payloads
  
  3. Implementation:
     📁 /shared-domain/src/main/java/com/payments/domain/
        ├─ payment/
        │   ├─ Payment.java (Aggregate Root)
        │   ├─ PaymentId.java (Value Object)
        │   ├─ Amount.java (Value Object)
        │   ├─ Currency.java (Enum)
        │   ├─ PaymentStatus.java (Enum)
        │   ├─ PaymentType.java (Enum)
        │   ├─ Beneficiary.java (Entity)
        │   ├─ PaymentRepository.java (Interface)
        │   └─ events/
        │       ├─ PaymentInitiatedEvent.java
        │       ├─ PaymentValidatedEvent.java
        │       └─ PaymentCompletedEvent.java
        ├─ account/
        │   ├─ Account.java (Aggregate Root)
        │   ├─ AccountId.java (Value Object)
        │   ├─ AccountType.java (Enum)
        │   ├─ Balance.java (Value Object)
        │   └─ AccountRepository.java (Interface)
        ├─ validation/
        │   ├─ ValidationRule.java (Entity)
        │   ├─ ValidationResult.java (Value Object)
        │   ├─ RuleType.java (Enum)
        │   └─ ValidationRepository.java (Interface)
        ├─ routing/
        │   ├─ RoutingRule.java (Entity)
        │   ├─ ClearingSystem.java (Enum)
        │   ├─ RoutingDecision.java (Value Object)
        │   └─ RoutingRepository.java (Interface)
        ├─ transaction/
        │   ├─ Transaction.java (Aggregate Root)
        │   ├─ TransactionId.java (Value Object)
        │   ├─ TransactionStatus.java (Enum)
        │   └─ TransactionRepository.java (Interface)
        ├─ clearing/
        │   ├─ ClearingSubmission.java (Aggregate Root)
        │   ├─ ClearingResponse.java (Value Object)
        │   └─ ClearingRepository.java (Interface)
        ├─ settlement/
        │   ├─ Settlement.java (Aggregate Root)
        │   ├─ SettlementStatus.java (Enum)
        │   └─ SettlementRepository.java (Interface)
        ├─ tenant/
        │   ├─ Tenant.java (Aggregate Root)
        │   ├─ BusinessUnit.java (Entity)
        │   ├─ TenantId.java (Value Object)
        │   └─ TenantRepository.java (Interface)
        └─ common/
            ├─ Entity.java (Base class)
            ├─ ValueObject.java (Base class)
            ├─ AggregateRoot.java (Base class)
            ├─ DomainEvent.java (Base interface)
            └─ Repository.java (Base interface)
     
     Example Implementation (Payment.java):
     ```java
     package com.payments.domain.payment;
     
     import com.payments.domain.common.AggregateRoot;
     import lombok.Getter;
     import java.math.BigDecimal;
     import java.time.Instant;
     import java.util.ArrayList;
     import java.util.List;
     
     @Getter
     public class Payment extends AggregateRoot<PaymentId> {
         private PaymentId paymentId;
         private Amount amount;
         private Currency currency;
         private String debtorAccountId;
         private String creditorAccountId;
         private PaymentStatus status;
         private PaymentType type;
         private String tenantId;
         private Instant createdAt;
         private List<DomainEvent> domainEvents;
         
         // Private constructor (use factory methods)
         private Payment() {
             this.domainEvents = new ArrayList<>();
         }
         
         // Factory method: Create new payment
         public static Payment create(
             Amount amount,
             Currency currency,
             String debtorAccountId,
             String creditorAccountId,
             PaymentType type,
             String tenantId
         ) {
             Payment payment = new Payment();
             payment.paymentId = PaymentId.generate();
             payment.amount = amount;
             payment.currency = currency;
             payment.debtorAccountId = debtorAccountId;
             payment.creditorAccountId = creditorAccountId;
             payment.type = type;
             payment.tenantId = tenantId;
             payment.status = PaymentStatus.INITIATED;
             payment.createdAt = Instant.now();
             
             // Raise domain event
             payment.addDomainEvent(new PaymentInitiatedEvent(
                 payment.paymentId,
                 payment.amount,
                 payment.debtorAccountId,
                 payment.creditorAccountId
             ));
             
             return payment;
         }
         
         // Business method: Validate payment
         public void validate() {
             if (this.status != PaymentStatus.INITIATED) {
                 throw new IllegalStateException(
                     "Payment can only be validated from INITIATED status"
                 );
             }
             
             this.status = PaymentStatus.VALIDATED;
             
             // Raise domain event
             this.addDomainEvent(new PaymentValidatedEvent(
                 this.paymentId,
                 Instant.now()
             ));
         }
         
         // Business method: Complete payment
         public void complete() {
             if (this.status != PaymentStatus.PROCESSING) {
                 throw new IllegalStateException(
                     "Payment can only be completed from PROCESSING status"
                 );
             }
             
             this.status = PaymentStatus.COMPLETED;
             
             // Raise domain event
             this.addDomainEvent(new PaymentCompletedEvent(
                 this.paymentId,
                 Instant.now()
             ));
         }
         
         // Business method: Fail payment
         public void fail(String reason) {
             this.status = PaymentStatus.FAILED;
             
             // Raise domain event
             this.addDomainEvent(new PaymentFailedEvent(
                 this.paymentId,
                 reason,
                 Instant.now()
             ));
         }
         
         // No public setters (encapsulation)
     }
     ```
  
  4. Unit Testing:
     ✅ Aggregate Tests
        - Aggregate creation
        - Business rules enforced
        - State transitions valid
        - Domain events raised
     
     ✅ Value Object Tests
        - Immutability
        - Equality by value
        - Validation rules
     
     ✅ Entity Tests
        - Identity equality
        - Business logic
     
     📁 /shared-domain/src/test/java/com/payments/domain/
        ├─ payment/PaymentTest.java
        ├─ account/AccountTest.java
        ├─ validation/ValidationRuleTest.java
        └─ ... (all domain model tests)
  
  5. Documentation:
     📄 README.md
        - Domain model overview
        - How to use aggregates
        - How to work with value objects
        - How to handle domain events
     
     📄 BOUNDED-CONTEXTS.md
        - All bounded contexts
        - Context map
        - Relationships
     
     📄 DOMAIN-MODEL-REFERENCE.md
        - Complete class reference
        - Aggregate descriptions
        - Business rules
        - Domain events

Success Criteria:
  ✅ All aggregates implemented
  ✅ All value objects immutable
  ✅ All entities have identity
  ✅ Business rules enforced
  ✅ Domain events raised correctly
  ✅ All tests pass (80%+ coverage)
  ✅ Documentation complete

Validation Checklist:
  - [ ] 8 bounded contexts defined
  - [ ] Aggregate boundaries clear
  - [ ] Value objects immutable
  - [ ] No public setters
  - [ ] Domain events raised
  - [ ] Tests passing
  - [ ] Class diagrams reviewed

Context Sufficiency: ✅ SUFFICIENT
  - Complete DDD guide in docs/14-DDD-IMPLEMENTATION.md
  - Database schemas for entity mapping
  - Clear bounded context definitions
  - Examples and patterns provided

Dependencies:
  - Feature 0.1 (Database Schemas) - for entity mapping
```

---

## Phase 1: Core Services (6 Features)

**Note**: Phase 1 detailed prompts follow the same structure as Feature 1.1 (Payment Initiation Service) shown above. All 6 core services have complete context from Phase 0 foundation.

---

## Phase 2: Clearing Adapters (5 Features)

### Feature 1.1: Payment Initiation Service

```yaml
Feature ID: 1.1
Feature Name: Payment Initiation Service
Agent Name: Payment Initiation Agent
Phase: 1 (Core Services)
Estimated Time: 3 days

Role & Expertise:
  You are a Backend Engineer with expertise in Java Spring Boot, REST APIs,
  event-driven architecture, and microservices.

Task:
  Build the Payment Initiation Service - the entry point for all payment
  requests. This service validates input, generates payment IDs, persists
  payments, and publishes PaymentInitiatedEvent.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #1 section ONLY)
        Lines 140-200 (Payment Initiation Service section)
        - Responsibilities
        - API endpoints
        - Technology stack
        - Database schema
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 1.1)
        - Step-by-step implementation guide
     
     📄 docs/33-FEATURE-FLAGS.md (Section: Release Toggles)
        - How to use feature flags
  
  2. Domain Models (from Phase 0):
     ✅ Payment.java (Aggregate Root)
     ✅ PaymentId.java (Value Object)
     ✅ Amount.java (Value Object)
     ✅ Currency.java (Enum)
     ✅ PaymentStatus.java (Enum)
     ✅ PaymentType.java (Enum)
  
  3. Event Schemas (from Phase 0):
     ✅ PaymentInitiatedEvent.json
        {
          "eventId": "uuid",
          "eventType": "PaymentInitiatedEvent",
          "paymentId": "PAY-2025-XXXXXX",
          "amount": 10000.00,
          "currency": "ZAR",
          "debtorAccountId": "ACC-123",
          "creditorAccountId": "ACC-456",
          "paymentType": "EFT",
          "tenantId": "BANK-001",
          "timestamp": "2025-10-12T10:00:00Z"
        }
  
  4. Database Schema (from Phase 0):
     ✅ Table: payments
        - payment_id (PK)
        - tenant_id (FK, NOT NULL)
        - amount (DECIMAL)
        - currency (VARCHAR)
        - debtor_account_id (VARCHAR)
        - creditor_account_id (VARCHAR)
        - payment_type (VARCHAR)
        - status (VARCHAR)
        - created_at (TIMESTAMP)
  
  5. Shared Libraries (from Phase 0):
     ✅ payment-common.jar
        - TenantContextHolder
        - IdempotencyHandler
        - CorrelationIdFilter
     ✅ event-publisher.jar
        - ServiceBusPublisher
        - EventPublisher interface
  
  6. Technology Stack:
     - Java 17
     - Spring Boot 3.2
     - Spring Data JPA
     - Azure Service Bus (event publishing)
     - PostgreSQL
     - Lombok
     - MapStruct (DTO mapping)
     - OpenAPI 3.0 (API docs)
  
  7. API Endpoints to Implement:
     POST   /api/v1/payments
     GET    /api/v1/payments/{id}
     GET    /api/v1/payments/status/{id}
  
  8. Configuration:
     application.yml:
       spring:
         application:
           name: payment-initiation-service
         datasource:
           url: jdbc:postgresql://localhost:5432/payment_initiation_db
         jpa:
           hibernate:
             ddl-auto: validate
       azure:
         servicebus:
           connection-string: ${AZURE_SERVICEBUS_CONNECTION_STRING}
           topic-name: payment-events
       unleash:
         api:
           url: http://unleash:4242/api
           token: ${UNLEASH_API_TOKEN}

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Component Diagram
        - REST Controller
        - Service Layer
        - Repository Layer
        - Event Publisher
        - External dependencies
     
     📊 Sequence Diagram
        - POST /api/v1/payments flow
        - Event publishing flow
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - PaymentController
        - PaymentInitiationService
        - PaymentRepository
        - PaymentEventPublisher
        - DTOs (PaymentRequest, PaymentResponse)
     
     📋 API Contract (OpenAPI 3.0)
        ```yaml
        openapi: 3.0.0
        info:
          title: Payment Initiation API
          version: v1
        paths:
          /api/v1/payments:
            post:
              summary: Create payment
              requestBody:
                content:
                  application/json:
                    schema:
                      $ref: '#/components/schemas/PaymentRequest'
              responses:
                '201':
                  description: Payment created
                  content:
                    application/json:
                      schema:
                        $ref: '#/components/schemas/PaymentResponse'
        ```
  
  3. Implementation:
     📁 /services/payment-initiation-service/
        ├─ src/main/java/com/payments/initiation/
        │   ├─ PaymentInitiationApplication.java (Main class)
        │   ├─ controller/
        │   │   └─ PaymentController.java
        │   ├─ service/
        │   │   ├─ PaymentInitiationService.java
        │   │   └─ PaymentEventPublisher.java
        │   ├─ repository/
        │   │   └─ PaymentRepository.java (JPA)
        │   ├─ model/
        │   │   └─ PaymentEntity.java (JPA Entity)
        │   ├─ dto/
        │   │   ├─ PaymentRequest.java
        │   │   ├─ PaymentResponse.java
        │   │   └─ PaymentStatusResponse.java
        │   ├─ mapper/
        │   │   └─ PaymentMapper.java (MapStruct)
        │   ├─ config/
        │   │   ├─ ServiceBusConfig.java
        │   │   ├─ SecurityConfig.java
        │   │   └─ OpenApiConfig.java
        │   └─ exception/
        │       ├─ PaymentNotFoundException.java
        │       └─ GlobalExceptionHandler.java
        ├─ src/main/resources/
        │   ├─ application.yml
        │   ├─ application-dev.yml
        │   └─ application-prod.yml
        ├─ src/test/java/com/payments/initiation/
        │   ├─ controller/PaymentControllerTest.java
        │   ├─ service/PaymentInitiationServiceTest.java
        │   └─ integration/PaymentIntegrationTest.java
        ├─ Dockerfile
        ├─ k8s/
        │   ├─ deployment.yaml
        │   ├─ service.yaml
        │   └─ configmap.yaml
        ├─ pom.xml
        └─ README.md
     
     Key Implementation (PaymentInitiationService.java):
     ```java
     @Service
     @Slf4j
     public class PaymentInitiationService {
         
         @Autowired
         private PaymentRepository paymentRepository;
         
         @Autowired
         private PaymentEventPublisher eventPublisher;
         
         @Autowired
         private FeatureFlagService featureFlags;
         
         @Transactional
         public PaymentResponse createPayment(PaymentRequest request) {
             log.info("Creating payment: amount={}, type={}", 
                 request.getAmount(), request.getPaymentType());
             
             // 1. Validate input
             validatePaymentRequest(request);
             
             // 2. Get tenant context
             String tenantId = TenantContextHolder.getTenantId();
             
             // 3. Create domain aggregate
             Payment payment = Payment.create(
                 new Amount(request.getAmount()),
                 Currency.valueOf(request.getCurrency()),
                 request.getDebtorAccountId(),
                 request.getCreditorAccountId(),
                 PaymentType.valueOf(request.getPaymentType()),
                 tenantId
             );
             
             // 4. Persist to database
             PaymentEntity entity = PaymentMapper.toEntity(payment);
             paymentRepository.save(entity);
             
             // 5. Publish event
             PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                 payment.getPaymentId().getValue(),
                 payment.getAmount().getValue(),
                 payment.getCurrency().name(),
                 payment.getDebtorAccountId(),
                 payment.getCreditorAccountId(),
                 payment.getType().name(),
                 tenantId
             );
             eventPublisher.publish(event);
             
             log.info("Payment created successfully: paymentId={}", 
                 payment.getPaymentId().getValue());
             
             // 6. Return response
             return PaymentMapper.toResponse(payment);
         }
         
         @Transactional(readOnly = true)
         public PaymentResponse getPayment(String paymentId) {
             PaymentEntity entity = paymentRepository.findById(paymentId)
                 .orElseThrow(() -> new PaymentNotFoundException(paymentId));
             
             return PaymentMapper.toResponse(entity);
         }
         
         private void validatePaymentRequest(PaymentRequest request) {
             if (request.getAmount() <= 0) {
                 throw new IllegalArgumentException("Amount must be positive");
             }
             if (request.getDebtorAccountId() == null) {
                 throw new IllegalArgumentException("Debtor account required");
             }
             if (request.getCreditorAccountId() == null) {
                 throw new IllegalArgumentException("Creditor account required");
             }
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Controller Tests (MockMvc)
        - POST /api/v1/payments (201 Created)
        - GET /api/v1/payments/{id} (200 OK)
        - GET /api/v1/payments/{id} (404 Not Found)
        - Validation errors (400 Bad Request)
     
     ✅ Service Tests (Mockito)
        - createPayment() success
        - createPayment() validation failure
        - getPayment() success
        - getPayment() not found
        - Event published correctly
     
     ✅ Integration Tests (TestContainers)
        - End-to-end payment creation
        - Database persistence
        - Event publishing
     
     📁 /src/test/java/
        ├─ controller/PaymentControllerTest.java (20+ tests)
        ├─ service/PaymentInitiationServiceTest.java (15+ tests)
        └─ integration/PaymentIntegrationTest.java (10+ tests)
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - Service overview
        - How to run locally
        - How to run tests
        - API endpoints
        - Environment variables
        - Troubleshooting
     
     📄 API-DOCUMENTATION.md (OpenAPI generated)
        - All endpoints
        - Request/response examples
        - Error codes
     
     📄 DEPLOYMENT.md
        - Docker build
        - Kubernetes deployment
        - Configuration

Success Criteria:
  ✅ Service builds successfully (mvn clean install)
  ✅ All tests pass (45+ tests)
  ✅ Code coverage ≥ 80%
  ✅ SonarQube quality gate passes
  ✅ Docker image builds
  ✅ Service deploys to AKS
  ✅ Health check responds (GET /actuator/health)
  ✅ API endpoints functional
  ✅ Events published to Service Bus
  ✅ Documentation complete

Validation Checklist:
  - [ ] HLD reviewed (component + sequence diagrams)
  - [ ] LLD reviewed (class diagram, API contract)
  - [ ] Code review passed (no critical issues)
  - [ ] Unit tests passing (80%+ coverage)
  - [ ] Integration tests passing
  - [ ] Docker image builds
  - [ ] Kubernetes manifests valid
  - [ ] OpenAPI spec generated
  - [ ] Documentation reviewed

Context Sufficiency: ✅ SUFFICIENT
  - Detailed service spec in docs/02-MICROSERVICES-BREAKDOWN.md
  - Domain models available (Payment, PaymentId, etc.)
  - Event schemas defined (PaymentInitiatedEvent)
  - Database schema ready
  - Shared libraries available
  - Technology stack clear
  - API endpoints specified
  - Configuration examples provided

Dependencies:
  ✅ Feature 0.1 (Database Schemas) - COMPLETE
  ✅ Feature 0.2 (Event Schemas) - COMPLETE
  ✅ Feature 0.3 (Domain Models) - COMPLETE
  ✅ Feature 0.4 (Shared Libraries) - COMPLETE
  ✅ Feature 0.5 (Infrastructure Setup) - COMPLETE
```

---

## Phase 2: Clearing Adapters (5 Features)

### Feature 2.1: SAMOS Adapter

```yaml
Feature ID: 2.1
Feature Name: SAMOS Adapter (High-Value RTGS)
Agent Name: SAMOS Adapter Agent
Phase: 2 (Clearing Adapters)
Estimated Time: 4 days

Role & Expertise:
  You are a Payments Integration Specialist with expertise in ISO 20022 messaging,
  RTGS (Real-Time Gross Settlement) systems, SWIFT-like messaging, and South African
  payment systems (SARB SAMOS).

Task:
  Build the SAMOS Adapter Service - integrates with South African Reserve Bank's
  SAMOS (South African Multiple Option Settlement) system for high-value RTGS
  payments. This service consumes PaymentRoutedEvent, builds ISO 20022 pacs.008
  messages, submits to SAMOS, handles responses, and publishes clearing events.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #7 section)
        Lines 174-220 (SAMOS Adapter section)
        - Responsibilities
        - ISO 20022 message types
        - Technology stack
     
     📄 docs/06-SOUTH-AFRICA-CLEARING.md (SAMOS section)
        Lines 50-250 (complete SAMOS specification)
        - SAMOS overview
        - RTGS characteristics
        - ISO 20022 pacs.008 structure
        - ISO 20022 pacs.002 (acknowledgment)
        - Settlement flow
        - Error handling
        - Connection details
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 2.1)
        - Step-by-step implementation guide
  
  2. Domain Models (from Phase 0):
     ✅ ClearingSubmission.java (Aggregate Root)
     ✅ ClearingSystem.java (Enum - includes SAMOS)
     ✅ ClearingResponse.java (Value Object)
  
  3. Event Schemas (from Phase 0):
     ✅ PaymentRoutedEvent.json (consumed)
        {
          "paymentId": "PAY-2025-XXXXXX",
          "routingDecision": "SAMOS",
          "amount": 5000000.00,
          "currency": "ZAR"
        }
     
     ✅ ClearingSubmittedEvent.json (published)
        {
          "paymentId": "PAY-2025-XXXXXX",
          "clearingSystem": "SAMOS",
          "clearingReference": "SAMOS-20251012-001",
          "status": "SUBMITTED"
        }
     
     ✅ ClearingCompletedEvent.json (published)
     ✅ ClearingFailedEvent.json (published)
  
  4. Database Schema (from Phase 0):
     ✅ Table: samos_submissions
        - submission_id (PK)
        - payment_id (FK)
        - tenant_id (FK)
        - samos_reference (VARCHAR)
        - iso_message (TEXT) -- pacs.008 XML
        - status (VARCHAR)
        - submitted_at (TIMESTAMP)
        - acknowledged_at (TIMESTAMP)
        - completed_at (TIMESTAMP)
        - error_code (VARCHAR)
        - error_message (TEXT)
  
  5. ISO 20022 Message Formats:
     
     ✅ pacs.008.001.08 (FIToFICustCredit)
        Credit transfer between financial institutions
        ```xml
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
          <FIToFICstmrCdtTrf>
            <GrpHdr>
              <MsgId>SAMOS-20251012-001</MsgId>
              <CreDtTm>2025-10-12T10:00:00Z</CreDtTm>
              <NbOfTxs>1</NbOfTxs>
              <TtlIntrBkSttlmAmt Ccy="ZAR">5000000.00</TtlIntrBkSttlmAmt>
              <IntrBkSttlmDt>2025-10-12</IntrBkSttlmDt>
            </GrpHdr>
            <CdtTrfTxInf>
              <PmtId>
                <InstrId>PAY-2025-XXXXXX</InstrId>
                <EndToEndId>E2E-001</EndToEndId>
                <TxId>TXN-001</TxId>
              </PmtId>
              <IntrBkSttlmAmt Ccy="ZAR">5000000.00</IntrBkSttlmAmt>
              <ChrgBr>SLEV</ChrgBr>
              <Dbtr>
                <Nm>Debtor Name</Nm>
                <Id>
                  <OrgId>
                    <Othr>
                      <Id>DEBTOR-BANK-BIC</Id>
                    </Othr>
                  </OrgId>
                </Id>
              </Dbtr>
              <DbtrAcct>
                <Id>
                  <Othr>
                    <Id>ACC-123</Id>
                  </Othr>
                </Id>
              </DbtrAcct>
              <DbtrAgt>
                <FinInstnId>
                  <BICFI>DEBTORBICXXX</BICFI>
                </FinInstnId>
              </DbtrAgt>
              <CdtrAgt>
                <FinInstnId>
                  <BICFI>CREDITORBICXXX</BICFI>
                </FinInstnId>
              </CdtrAgt>
              <Cdtr>
                <Nm>Creditor Name</Nm>
              </Cdtr>
              <CdtrAcct>
                <Id>
                  <Othr>
                    <Id>ACC-456</Id>
                  </Othr>
                </Id>
              </CdtrAcct>
            </CdtTrfTxInf>
          </FIToFICstmrCdtTrf>
        </Document>
        ```
     
     ✅ pacs.002.001.10 (FIToFIPmtStsRpt)
        Payment status report (acknowledgment/response)
        ```xml
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
          <FIToFIPmtStsRpt>
            <GrpHdr>
              <MsgId>SAMOS-ACK-001</MsgId>
              <CreDtTm>2025-10-12T10:01:00Z</CreDtTm>
            </GrpHdr>
            <TxInfAndSts>
              <OrgnlInstrId>PAY-2025-XXXXXX</OrgnlInstrId>
              <OrgnlEndToEndId>E2E-001</OrgnlEndToEndId>
              <TxSts>ACCP</TxSts> <!-- Accepted -->
              <StsRsnInf>
                <Rsn>
                  <Cd>0000</Cd>
                </Rsn>
              </StsRsnInf>
            </TxInfAndSts>
          </FIToFIPmtStsRpt>
        </Document>
        ```
  
  6. SAMOS API Specifications:
     - Protocol: HTTPS REST (or MQ if legacy)
     - Authentication: Mutual TLS (client certificates)
     - Base URL: https://samos.resbank.co.za/api/v1
     - Endpoints:
       - POST /submissions (submit pacs.008)
       - GET /submissions/{reference}/status
     - Timeout: 30 seconds
     - Idempotency: Required (InstrId)
  
  7. Technology Stack:
     - Java 17
     - Spring Boot 3.2
     - Spring Integration (for XML processing)
     - JAXB (for ISO 20022 XML marshalling/unmarshalling)
     - Azure Service Bus (event consumption/publishing)
     - PostgreSQL
     - Resilience4j (circuit breaker, retry)
  
  8. Configuration:
     application.yml:
       samos:
         api:
           url: https://samos.resbank.co.za/api/v1
           timeout: 30000
           retry:
             max-attempts: 3
             backoff: 5000
         certificate:
           keystore-path: ${SAMOS_KEYSTORE_PATH}
           keystore-password: ${SAMOS_KEYSTORE_PASSWORD}
           truststore-path: ${SAMOS_TRUSTSTORE_PATH}
         participant-code: ${SAMOS_PARTICIPANT_CODE}

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Component Diagram
        - Event Consumer (PaymentRoutedEvent)
        - ISO 20022 Message Builder
        - SAMOS API Client
        - Response Handler
        - Event Publisher (Clearing events)
     
     📊 Sequence Diagram
        - Payment routing → SAMOS submission flow
        - SAMOS acknowledgment flow
        - Error handling flow
     
     📊 SAMOS Integration Flow
        ```
        PaymentRoutedEvent (clearingSystem=SAMOS)
            ↓
        SAMOS Adapter
            ↓
        1. Build pacs.008 (ISO 20022 XML)
        2. Submit to SAMOS API (HTTPS + mTLS)
        3. Wait for pacs.002 acknowledgment (30s timeout)
        4. Parse response
        5. Publish ClearingSubmittedEvent or ClearingFailedEvent
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - SAMOSEventConsumer (consumes PaymentRoutedEvent)
        - SAMOSService (orchestration)
        - ISO20022MessageBuilder (builds pacs.008)
        - SAMOSApiClient (REST client with mTLS)
        - SAMOSResponseParser (parses pacs.002)
        - SAMOSRepository (JPA)
        - SAMOSEventPublisher (publishes clearing events)
     
     📋 ISO 20022 Schema
        - XSD schemas for pacs.008, pacs.002
        - JAXB generated classes
     
     📋 Error Handling
        - Connection timeout → Retry 3 times
        - SAMOS rejection → Publish ClearingFailedEvent
        - Invalid XML → Log and fail
        - Circuit breaker: Open after 5 consecutive failures
  
  3. Implementation:
     📁 /services/samos-adapter/
        ├─ src/main/java/com/payments/samos/
        │   ├─ SAMOSAdapterApplication.java
        │   ├─ consumer/
        │   │   └─ PaymentEventConsumer.java
        │   ├─ service/
        │   │   ├─ SAMOSService.java
        │   │   ├─ ISO20022MessageBuilder.java
        │   │   └─ SAMOSEventPublisher.java
        │   ├─ client/
        │   │   ├─ SAMOSApiClient.java
        │   │   ├─ SAMOSResponseParser.java
        │   │   └─ MutualTLSConfig.java
        │   ├─ repository/
        │   │   └─ SAMOSSubmissionRepository.java
        │   ├─ model/
        │   │   ├─ SAMOSSubmissionEntity.java
        │   │   └─ iso20022/
        │   │       ├─ Pacs008Document.java (JAXB)
        │   │       ├─ Pacs002Document.java (JAXB)
        │   │       └─ ... (ISO 20022 classes)
        │   ├─ dto/
        │   │   ├─ SAMOSSubmissionRequest.java
        │   │   └─ SAMOSSubmissionResponse.java
        │   ├─ mapper/
        │   │   └─ SAMOSMapper.java
        │   ├─ config/
        │   │   ├─ ServiceBusConfig.java
        │   │   ├─ RestTemplateConfig.java
        │   │   └─ CircuitBreakerConfig.java
        │   └─ exception/
        │       ├─ SAMOSApiException.java
        │       ├─ SAMOSTimeoutException.java
        │       └─ ISO20022ValidationException.java
        ├─ src/main/resources/
        │   ├─ application.yml
        │   ├─ xsd/
        │   │   ├─ pacs.008.001.08.xsd
        │   │   └─ pacs.002.001.10.xsd
        │   └─ certificates/
        │       ├─ samos-client.jks
        │       └─ samos-truststore.jks
        ├─ src/test/java/com/payments/samos/
        │   ├─ service/SAMOSServiceTest.java
        │   ├─ client/SAMOSApiClientTest.java
        │   ├─ builder/ISO20022MessageBuilderTest.java
        │   └─ integration/SAMOSIntegrationTest.java
        ├─ Dockerfile
        ├─ k8s/
        │   ├─ deployment.yaml
        │   ├─ service.yaml
        │   ├─ configmap.yaml
        │   └─ secret.yaml (for certificates)
        ├─ pom.xml
        └─ README.md
     
     Key Implementation (SAMOSService.java):
     ```java
     @Service
     @Slf4j
     public class SAMOSService {
         
         @Autowired
         private ISO20022MessageBuilder messageBuilder;
         
         @Autowired
         private SAMOSApiClient samosClient;
         
         @Autowired
         private SAMOSSubmissionRepository repository;
         
         @Autowired
         private SAMOSEventPublisher eventPublisher;
         
         @CircuitBreaker(name = "samos", fallbackMethod = "submitFallback")
         @Retry(name = "samos", fallbackMethod = "submitFallback")
         @Transactional
         public void submitToSAMOS(PaymentRoutedEvent event) {
             log.info("Submitting payment to SAMOS: paymentId={}", event.getPaymentId());
             
             try {
                 // 1. Build ISO 20022 pacs.008 message
                 Pacs008Document pacs008 = messageBuilder.buildPacs008(event);
                 String xmlMessage = messageBuilder.marshalToXml(pacs008);
                 
                 // 2. Validate XML against XSD
                 messageBuilder.validate(xmlMessage);
                 
                 // 3. Persist submission
                 SAMOSSubmissionEntity submission = new SAMOSSubmissionEntity();
                 submission.setPaymentId(event.getPaymentId());
                 submission.setTenantId(event.getTenantId());
                 submission.setSamosReference(pacs008.getMsgId());
                 submission.setIsoMessage(xmlMessage);
                 submission.setStatus("PENDING");
                 submission.setSubmittedAt(Instant.now());
                 repository.save(submission);
                 
                 // 4. Submit to SAMOS API (with mTLS)
                 SAMOSSubmissionResponse response = samosClient.submitPayment(xmlMessage);
                 
                 // 5. Parse pacs.002 response
                 String txStatus = response.getTransactionStatus();
                 
                 if ("ACCP".equals(txStatus)) {
                     // Accepted
                     submission.setStatus("SUBMITTED");
                     submission.setAcknowledgedAt(Instant.now());
                     repository.save(submission);
                     
                     // Publish success event
                     eventPublisher.publishClearingSubmitted(
                         event.getPaymentId(),
                         "SAMOS",
                         submission.getSamosReference()
                     );
                     
                     log.info("Payment submitted to SAMOS successfully: reference={}",
                         submission.getSamosReference());
                 } else {
                     // Rejected
                     submission.setStatus("FAILED");
                     submission.setErrorCode(response.getReasonCode());
                     submission.setErrorMessage(response.getReasonText());
                     repository.save(submission);
                     
                     // Publish failure event
                     eventPublisher.publishClearingFailed(
                         event.getPaymentId(),
                         "SAMOS",
                         response.getReasonText()
                     );
                     
                     log.error("Payment rejected by SAMOS: reference={}, reason={}",
                         submission.getSamosReference(), response.getReasonText());
                 }
             } catch (Exception e) {
                 log.error("Failed to submit payment to SAMOS: paymentId={}",
                     event.getPaymentId(), e);
                 throw new SAMOSApiException("SAMOS submission failed", e);
             }
         }
         
         // Fallback method (circuit breaker open or all retries exhausted)
         private void submitFallback(PaymentRoutedEvent event, Exception e) {
             log.error("SAMOS submission fallback triggered: paymentId={}",
                 event.getPaymentId(), e);
             
             eventPublisher.publishClearingFailed(
                 event.getPaymentId(),
                 "SAMOS",
                 "SAMOS service unavailable: " + e.getMessage()
             );
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Service Tests
        - submitToSAMOS() success (ACCP)
        - submitToSAMOS() rejection (RJCT)
        - Circuit breaker triggers
        - Retry mechanism
        - Fallback method
     
     ✅ ISO 20022 Builder Tests
        - pacs.008 XML generation
        - XML validation against XSD
        - All mandatory fields present
        - XML marshalling/unmarshalling
     
     ✅ API Client Tests
        - Successful submission (200 OK)
        - Timeout handling (30s)
        - Connection errors
        - mTLS certificate validation
     
     ✅ Integration Tests (TestContainers + WireMock)
        - End-to-end SAMOS submission
        - Mock SAMOS API responses
        - Database persistence
        - Event publishing
     
     📁 /src/test/java/
        ├─ service/SAMOSServiceTest.java (25+ tests)
        ├─ builder/ISO20022MessageBuilderTest.java (20+ tests)
        ├─ client/SAMOSApiClientTest.java (15+ tests)
        └─ integration/SAMOSIntegrationTest.java (10+ tests)
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - SAMOS Adapter overview
        - ISO 20022 message formats
        - How to configure mTLS certificates
        - How to test locally (with mock)
        - Troubleshooting
     
     📄 ISO20022-REFERENCE.md
        - pacs.008 field reference
        - pacs.002 status codes
        - SAMOS-specific requirements
        - XML examples
     
     📄 SAMOS-INTEGRATION-GUIDE.md
        - SAMOS connection setup
        - Certificate installation
        - Participant code registration
        - Testing with SAMOS UAT environment
     
     📄 DEPLOYMENT.md
        - Docker build
        - Kubernetes deployment
        - Secret management (certificates)

Success Criteria:
  ✅ Service builds successfully
  ✅ All tests pass (70+ tests)
  ✅ Code coverage ≥ 80%
  ✅ ISO 20022 XML validates against XSD
  ✅ mTLS connection to SAMOS successful
  ✅ Circuit breaker/retry working
  ✅ Docker image builds
  ✅ Service deploys to AKS
  ✅ Events consumed and published correctly
  ✅ Documentation complete

Validation Checklist:
  - [ ] HLD reviewed (component + sequence diagrams)
  - [ ] LLD reviewed (class diagram, ISO 20022 schemas)
  - [ ] pacs.008 message validates
  - [ ] pacs.002 parsing correct
  - [ ] mTLS certificates configured
  - [ ] Circuit breaker tested
  - [ ] Tests passing (80%+ coverage)
  - [ ] Kubernetes secret for certificates
  - [ ] Documentation reviewed

Context Sufficiency: ✅ SUFFICIENT
  - Complete SAMOS spec in docs/06-SOUTH-AFRICA-CLEARING.md
  - Service details in docs/02-MICROSERVICES-BREAKDOWN.md
  - ISO 20022 message formats provided
  - mTLS configuration examples
  - Domain models available
  - Event schemas defined
  - Database schema ready
  - Error handling patterns

Dependencies:
  ✅ Feature 0.1 (Database Schemas) - COMPLETE
  ✅ Feature 0.2 (Event Schemas) - COMPLETE
  ✅ Feature 0.3 (Domain Models) - COMPLETE
  ✅ Feature 1.4 (Routing Service) - publishes PaymentRoutedEvent
```

---

### Feature 2.2: BankservAfrica Adapter

```yaml
Feature ID: 2.2
Feature Name: BankservAfrica Adapter (EFT/ACH Batch)
Agent Name: BankservAfrica Adapter Agent
Phase: 2 (Clearing Adapters)
Estimated Time: 4 days

Role & Expertise:
  You are a Payments Integration Specialist with expertise in ISO 8583 messaging,
  ACH/EFT batch processing, file-based clearing, and South African payment systems
  (BankservAfrica).

Task:
  Build the BankservAfrica Adapter Service - integrates with BankservAfrica for
  low-value ACH/EFT batch payments. This service consumes PaymentRoutedEvent,
  batches payments, builds ISO 8583 messages or proprietary batch files, submits
  to BankservAfrica, and publishes clearing events.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #8 section)
     📄 docs/06-SOUTH-AFRICA-CLEARING.md (BankservAfrica section)
        Lines 250-450 (complete BankservAfrica specification)
        - ACH/EFT overview
        - ISO 8583 message structure
        - Batch file format
        - Settlement cycles (multiple per day)
        - Acknowledgment processing
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 2.2)
  
  2. BankservAfrica Specifications:
     - Protocol: SFTP (file-based) or API (modern)
     - Message Format: ISO 8583 (legacy) or CSV batch (newer)
     - Batch Cycles: 3 per day (09:00, 13:00, 17:00)
     - File Format: Fixed-width or CSV
     - Acknowledgment: Separate acknowledgment file
     - Settlement: T+1 (next day)
  
  3. Batch File Format (CSV):
     ```csv
     HEADER,BATCH-20251012-001,2025-10-12,10:00:00,100,1000000.00
     DETAIL,PAY-001,ACC-123,ACC-456,1000.00,EFT,Customer A,Customer B
     DETAIL,PAY-002,ACC-124,ACC-457,2000.00,EFT,Customer C,Customer D
     TRAILER,100,1000000.00,CHECKSUM-ABC123
     ```
  
  4. ISO 8583 Message Format:
     Field 0: MTI (0200 - Financial Transaction)
     Field 2: PAN (Account Number)
     Field 3: Processing Code (000000 - Purchase)
     Field 4: Amount (12 digits, right-justified)
     Field 7: Transmission Date/Time
     Field 11: STAN (System Trace Audit Number)
     Field 32: Acquiring Institution ID
     Field 37: Retrieval Reference Number
     Field 41: Terminal ID
     Field 49: Currency Code (710 - ZAR)
  
  5. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring Batch (for batch processing)
     - Apache Camel (for file routing)
     - SFTP Client (JSch or Apache Commons VFS)
     - ISO 8583 library (jPOS)
  
  6. Configuration:
     bankserv:
       sftp:
         host: sftp.bankserv.co.za
         port: 22
         username: ${BANKSERV_USERNAME}
         private-key-path: ${BANKSERV_PRIVATE_KEY}
         upload-dir: /inbound
         download-dir: /outbound
       batch:
         max-size: 1000  # Max payments per batch
         schedule: "0 0 9,13,17 * * *"  # 09:00, 13:00, 17:00
       participant-code: ${BANKSERV_PARTICIPANT_CODE}

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Batch Processing Flow
        ```
        PaymentRoutedEvent (clearingSystem=BANKSERV)
            ↓
        Batch Accumulator (in-memory or Redis)
            ↓
        Scheduled Trigger (09:00, 13:00, 17:00)
            ↓
        Build Batch File (CSV or ISO 8583)
            ↓
        Upload to SFTP
            ↓
        Poll for Acknowledgment File
            ↓
        Parse Acknowledgment
            ↓
        Publish ClearingSubmittedEvent (per payment)
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - BankservEventConsumer
        - BatchAccumulator (accumulates payments)
        - BatchFileBuilder (builds CSV/ISO 8583)
        - SFTPUploader
        - AcknowledgmentPoller
        - AcknowledgmentParser
     
     📋 Batch Job Configuration (Spring Batch)
        - ItemReader: Read payments from accumulator
        - ItemProcessor: Validate and transform
        - ItemWriter: Write to batch file
     
     📋 SFTP Integration
        - Upload batch file
        - Download acknowledgment file
        - Archive processed files
  
  3. Implementation:
     📁 /services/bankserv-adapter/
        ├─ src/main/java/com/payments/bankserv/
        │   ├─ BankservAdapterApplication.java
        │   ├─ consumer/
        │   │   └─ PaymentEventConsumer.java
        │   ├─ service/
        │   │   ├─ BankservService.java
        │   │   ├─ BatchAccumulator.java
        │   │   ├─ BatchFileBuilder.java
        │   │   └─ BankservEventPublisher.java
        │   ├─ batch/
        │   │   ├─ BankservBatchJob.java
        │   │   ├─ PaymentItemReader.java
        │   │   ├─ PaymentItemProcessor.java
        │   │   └─ BatchFileWriter.java
        │   ├─ sftp/
        │   │   ├─ SFTPUploader.java
        │   │   ├─ SFTPDownloader.java
        │   │   └─ AcknowledgmentPoller.java
        │   ├─ parser/
        │   │   ├─ BatchFileParser.java
        │   │   ├─ AcknowledgmentParser.java
        │   │   └─ ISO8583MessageBuilder.java (if using ISO 8583)
        │   ├─ repository/
        │   │   └─ BankservSubmissionRepository.java
        │   ├─ model/
        │   │   ├─ BankservSubmissionEntity.java
        │   │   └─ BatchFileMetadata.java
        │   ├─ scheduler/
        │   │   └─ BatchSubmissionScheduler.java
        │   ├─ config/
        │   │   ├─ BatchConfig.java
        │   │   ├─ SFTPConfig.java
        │   │   └─ SchedulerConfig.java
        │   └─ exception/
        │       ├─ BankservApiException.java
        │       └─ BatchFileException.java
        ├─ src/test/java/
        │   ├─ service/BankservServiceTest.java
        │   ├─ batch/BatchFileBuilderTest.java
        │   ├─ sftp/SFTPUploaderTest.java (with embedded SFTP)
        │   └─ integration/BankservIntegrationTest.java
        ├─ Dockerfile
        ├─ k8s/deployment.yaml
        └─ README.md
     
     Key Implementation (BatchSubmissionScheduler.java):
     ```java
     @Component
     @Slf4j
     public class BatchSubmissionScheduler {
         
         @Autowired
         private BatchAccumulator accumulator;
         
         @Autowired
         private BatchFileBuilder fileBuilder;
         
         @Autowired
         private SFTPUploader sftpUploader;
         
         @Scheduled(cron = "0 0 9,13,17 * * *")
         @Transactional
         public void submitBatch() {
             log.info("Starting BankservAfrica batch submission");
             
             // 1. Get accumulated payments
             List<Payment> payments = accumulator.getAndClear();
             
             if (payments.isEmpty()) {
                 log.info("No payments to submit");
                 return;
             }
             
             log.info("Submitting {} payments to BankservAfrica", payments.size());
             
             // 2. Build batch file
             String batchId = "BATCH-" + LocalDateTime.now().format(
                 DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
             File batchFile = fileBuilder.buildBatchFile(batchId, payments);
             
             // 3. Upload to SFTP
             sftpUploader.upload(batchFile, "/inbound/" + batchFile.getName());
             
             // 4. Archive locally
             archiveBatchFile(batchFile);
             
             log.info("Batch submitted successfully: batchId={}", batchId);
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Batch File Builder Tests
        - CSV generation
        - Header/detail/trailer format
        - Checksum calculation
     
     ✅ SFTP Tests (with embedded SFTP server)
        - Upload successful
        - Download acknowledgment
        - Connection errors
     
     ✅ Scheduler Tests
        - Batch triggered at correct times
        - Empty batch handling
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - BankservAfrica overview
        - Batch processing flow
        - SFTP setup
        - Testing
     
     📄 BATCH-FILE-FORMAT.md
        - CSV format specification
        - ISO 8583 format (if used)
        - Acknowledgment format

Success Criteria:
  ✅ Batch file generation correct
  ✅ SFTP upload/download working
  ✅ Scheduler triggers correctly
  ✅ Tests pass (80%+ coverage)
  ✅ Documentation complete

Context Sufficiency: ✅ SUFFICIENT
  - Complete BankservAfrica spec in docs/06
  - Batch file formats provided
  - SFTP configuration examples
  - Spring Batch patterns

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Feature 1.4 (Routing Service) - COMPLETE
```

---

### Feature 2.3: RTC Adapter

```yaml
Feature ID: 2.3
Feature Name: RTC Adapter (Real-Time Clearing)
Agent Name: RTC Adapter Agent
Phase: 2 (Clearing Adapters)
Estimated Time: 3 days

Role & Expertise:
  You are a Payments Integration Specialist with expertise in real-time payment
  systems, ISO 20022 messaging, and South African payment systems (RTC).

Task:
  Build the RTC Adapter Service - integrates with BankservAfrica's RTC (Real-Time
  Clearing) for instant low-value payments. Similar to SAMOS but for lower amounts
  (<R5,000) with faster settlement.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #9 section)
     📄 docs/06-SOUTH-AFRICA-CLEARING.md (RTC section)
        Lines 450-600 (complete RTC specification)
        - RTC overview
        - Real-time characteristics (instant settlement)
        - ISO 20022 pacs.008 (same as SAMOS)
        - Amount limit: R5,000
        - 24/7/365 availability
  
  2. RTC Specifications:
     - Similar to SAMOS but for lower amounts
     - Protocol: HTTPS REST
     - Message Format: ISO 20022 pacs.008/pacs.002
     - Settlement: Real-time (within seconds)
     - Amount Limit: R5,000 per transaction
     - Availability: 24/7/365
  
  3. Key Differences from SAMOS:
     - Lower amount threshold (R5,000 vs R5,000,000)
     - Faster settlement (seconds vs minutes)
     - 24/7 availability (vs business hours)
     - Less stringent participant requirements

Expected Deliverables:
  (Similar structure to SAMOS Adapter - Feature 2.1)
  
  1. HLD: Component + sequence diagrams
  2. LLD: Class diagram, ISO 20022 schemas
  3. Implementation: Complete service with ISO 20022 builder
  4. Testing: 80%+ coverage
  5. Documentation: README, ISO 20022 reference

Success Criteria:
  ✅ RTC submission successful
  ✅ Real-time settlement working
  ✅ Amount validation (≤ R5,000)
  ✅ 24/7 availability tested
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT
  - RTC spec in docs/06 (similar to SAMOS)
  - ISO 20022 formats (reuse from SAMOS)
  - Amount validation logic clear

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Feature 2.1 (SAMOS Adapter) - can reuse ISO 20022 builder
```

---

### Feature 2.4: PayShap Adapter

```yaml
Feature ID: 2.4
Feature Name: PayShap Adapter (Instant P2P Payments)
Agent Name: PayShap Adapter Agent
Phase: 2 (Clearing Adapters)
Estimated Time: 4 days

Role & Expertise:
  You are a Payments Integration Specialist with expertise in instant payment
  systems, proxy-based payments (mobile/email), ISO 20022 messaging, and
  PayShap (South Africa's instant payment system).

Task:
  Build the PayShap Adapter Service - integrates with PayShap for instant
  peer-to-peer payments using mobile numbers or email addresses as proxies.
  Real-time, 24/7/365, with R3,000 limit per transaction.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #10 section)
     📄 docs/26-PAYSHAP-INTEGRATION.md (COMPLETE FILE - 1,800 lines)
        - PayShap overview
        - Instant payment characteristics
        - Proxy registry (mobile/email → account)
        - ISO 20022 pacs.008/pacs.002/pacs.004
        - Amount limit: R3,000
        - 24/7/365 availability
        - QR code payments
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 2.4)
  
  2. PayShap Specifications:
     - Protocol: HTTPS REST API
     - Message Format: ISO 20022 pacs.008 (credit transfer)
     - Proxy Resolution: API call to proxy registry
     - Settlement: Real-time (within seconds)
     - Amount Limit: R3,000 per transaction
     - Availability: 24/7/365
     - Participant: Any bank/wallet provider
  
  3. Proxy Types:
     - Mobile Number: +27821234567
     - Email Address: customer@example.com
     - QR Code: PAYSHAP-QR-12345
     - Account Number: Traditional (fallback)
  
  4. PayShap API Endpoints:
     - POST /proxy-registry/resolve (resolve proxy → account)
     - POST /payments (submit pacs.008)
     - GET /payments/{reference}/status
  
  5. Proxy Resolution Example:
     ```json
     POST /proxy-registry/resolve
     {
       "proxyType": "MOBILE",
       "proxyValue": "+27821234567"
     }
     
     Response:
     {
       "accountId": "ACC-789",
       "accountName": "John Doe",
       "bank": "BANK-002",
       "participantId": "PART-002"
     }
     ```
  
  6. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring WebFlux (reactive for high throughput)
     - ISO 20022 (JAXB)
     - Redis (proxy cache)
     - Circuit breaker, retry

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 PayShap Flow
        ```
        PaymentRoutedEvent (clearingSystem=PAYSHAP)
            ↓
        PayShap Adapter
            ↓
        1. Resolve Proxy (mobile/email → account)
        2. Cache proxy mapping (Redis, TTL 24h)
        3. Build pacs.008 (ISO 20022 XML)
        4. Submit to PayShap API
        5. Wait for pacs.002 acknowledgment (<5s)
        6. Publish ClearingCompletedEvent (instant)
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - PayShapEventConsumer
        - PayShapService
        - ProxyRegistryClient (resolve proxies)
        - ISO20022MessageBuilder (reuse from SAMOS)
        - PayShapApiClient
        - PayShapEventPublisher
     
     📋 Proxy Cache Strategy
        - Key: proxyType:proxyValue
        - Value: accountId, accountName, bank
        - TTL: 24 hours
        - Cache miss → API call → cache update
  
  3. Implementation:
     📁 /services/payshap-adapter/
        ├─ src/main/java/com/payments/payshap/
        │   ├─ PayShapAdapterApplication.java
        │   ├─ consumer/
        │   │   └─ PaymentEventConsumer.java
        │   ├─ service/
        │   │   ├─ PayShapService.java
        │   │   ├─ ProxyResolutionService.java
        │   │   └─ PayShapEventPublisher.java
        │   ├─ client/
        │   │   ├─ ProxyRegistryClient.java
        │   │   ├─ PayShapApiClient.java
        │   │   └─ ISO20022MessageBuilder.java (shared)
        │   ├─ cache/
        │   │   └─ ProxyCacheService.java (Redis)
        │   ├─ repository/
        │   │   └─ PayShapSubmissionRepository.java
        │   ├─ model/
        │   │   ├─ PayShapSubmissionEntity.java
        │   │   ├─ ProxyMapping.java
        │   │   └─ iso20022/ (JAXB classes)
        │   ├─ dto/
        │   │   ├─ ProxyResolutionRequest.java
        │   │   └─ ProxyResolutionResponse.java
        │   ├─ config/
        │   │   ├─ RedisConfig.java
        │   │   ├─ WebClientConfig.java (WebFlux)
        │   │   └─ CircuitBreakerConfig.java
        │   └─ exception/
        │       ├─ ProxyNotFoundException.java
        │       └─ PayShapApiException.java
        ├─ src/test/java/
        │   ├─ service/PayShapServiceTest.java
        │   ├─ client/ProxyRegistryClientTest.java
        │   ├─ cache/ProxyCacheServiceTest.java
        │   └─ integration/PayShapIntegrationTest.java
        ├─ Dockerfile
        ├─ k8s/deployment.yaml
        └─ README.md
     
     Key Implementation (ProxyResolutionService.java):
     ```java
     @Service
     @Slf4j
     public class ProxyResolutionService {
         
         @Autowired
         private ProxyRegistryClient registryClient;
         
         @Autowired
         private ProxyCacheService cacheService;
         
         public ProxyMapping resolveProxy(String proxyType, String proxyValue) {
             log.info("Resolving proxy: type={}, value={}", proxyType, proxyValue);
             
             // 1. Check cache first
             String cacheKey = proxyType + ":" + proxyValue;
             ProxyMapping cached = cacheService.get(cacheKey);
             
             if (cached != null) {
                 log.info("Proxy found in cache: {}", cached.getAccountId());
                 return cached;
             }
             
             // 2. Cache miss - call proxy registry API
             ProxyMapping mapping = registryClient.resolve(proxyType, proxyValue);
             
             if (mapping == null) {
                 throw new ProxyNotFoundException(
                     "Proxy not found: " + proxyType + "=" + proxyValue);
             }
             
             // 3. Cache the result (TTL 24 hours)
             cacheService.set(cacheKey, mapping, Duration.ofHours(24));
             
             log.info("Proxy resolved: {} → {}", proxyValue, mapping.getAccountId());
             return mapping;
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Proxy Resolution Tests
        - Cache hit
        - Cache miss → API call
        - Proxy not found
     
     ✅ PayShap Service Tests
        - Full flow: resolve proxy + submit payment
        - Amount validation (≤ R3,000)
        - ISO 20022 message building
     
     ✅ Redis Cache Tests (with embedded Redis)
        - Cache set/get
        - TTL expiration
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - PayShap overview
        - Proxy resolution flow
        - Amount limits
        - Testing
     
     📄 PROXY-TYPES.md
        - Supported proxy types
        - Proxy registration process
        - Proxy validation rules

Success Criteria:
  ✅ Proxy resolution working
  ✅ Redis caching functional
  ✅ PayShap submission successful
  ✅ Amount validation (≤ R3,000)
  ✅ Tests pass (80%+ coverage)
  ✅ Documentation complete

Context Sufficiency: ✅ SUFFICIENT
  - Complete PayShap spec in docs/26 (1,800 lines)
  - Proxy resolution API defined
  - ISO 20022 formats (reuse from SAMOS)
  - Redis caching patterns clear

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Feature 2.1 (SAMOS Adapter) - can reuse ISO 20022 builder
```

---

### Feature 2.5: SWIFT Adapter

```yaml
Feature ID: 2.5
Feature Name: SWIFT Adapter (International Payments)
Agent Name: SWIFT Adapter Agent
Phase: 2 (Clearing Adapters)
Estimated Time: 5 days

Role & Expertise:
  You are a Payments Integration Specialist with expertise in international
  payments, SWIFT messaging (MT103, MX/ISO 20022), sanctions screening,
  FX rates, correspondent banking, and cross-border compliance.

Task:
  Build the SWIFT Adapter Service - integrates with SWIFT network for
  international cross-border payments. Handles MT103 (legacy) and pacs.008
  (modern) messages, mandatory sanctions screening (OFAC, UN, EU), FX rate
  lookup, and correspondent bank routing.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #11 section)
     📄 docs/27-SWIFT-INTEGRATION.md (COMPLETE FILE - 2,200 lines)
        - SWIFT overview
        - MT103 format (legacy SWIFT message)
        - MX pacs.008 format (ISO 20022)
        - Sanctions screening (mandatory!)
        - FX rate conversion
        - Correspondent banking
        - SWIFT gpi tracking
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 2.5)
  
  2. SWIFT Specifications:
     - Protocol: SWIFT Alliance Lite2 or SWIFT API
     - Message Formats:
       - MT103: Legacy (free-form text, 35 chars/line)
       - MX pacs.008: Modern (ISO 20022 XML)
     - Sanctions Screening: Mandatory (OFAC, UN, EU lists)
     - FX Rates: Required for currency conversion
     - Correspondent Banks: Routing via intermediary banks
     - Settlement: T+1 to T+3 (varies by corridor)
  
  3. MT103 Message Format (Legacy):
     ```
     {1:F01DEBTORBICAXXX0000000000}
     {2:O1031200251012CREDITORBICXXXX0000000000}
     {3:{108:MT103 001}}
     {4:
     :20:REF-12345
     :23B:CRED
     :32A:251012ZAR100000,00
     :50K:/ACC-123
     DEBTOR NAME
     ADDRESS LINE 1
     :52A:DEBTORBICAXXX
     :53A:CORRESBICAXXX
     :57A:CREDITORBICAXXX
     :59:/ACC-456
     CREDITOR NAME
     ADDRESS LINE 1
     :70:INVOICE 12345
     :71A:OUR
     -}
     ```
  
  4. SWIFT pacs.008 (ISO 20022) - Modern:
     (Similar to SAMOS pacs.008 but with additional fields)
     - Currency conversion details
     - Intermediary agents (correspondent banks)
     - Purpose of payment codes
     - Regulatory reporting
  
  5. Sanctions Screening:
     - OFAC (US Treasury): Specially Designated Nationals (SDN)
     - UN Sanctions List
     - EU Sanctions List
     - Local sanctions (South Africa)
     - Screening: Debtor, Creditor, Intermediaries
     - Match Threshold: Fuzzy matching (80%+)
     - Action on match: Block payment, alert compliance
  
  6. FX Rate Lookup:
     - Source: Reuters, Bloomberg, or internal rates
     - Rate Type: Spot, forward, or fixed
     - Spread: Bank markup (e.g., 0.5%)
     - Rate expiry: 60 seconds
  
  7. Correspondent Banking:
     - Corridor: ZAR → USD → EUR (example)
     - Correspondent 1: South African bank → US bank
     - Correspondent 2: US bank → EU bank
     - Routing: BIC codes for each hop
  
  8. Technology Stack:
     - Java 17, Spring Boot 3.2
     - SWIFT Alliance Lite2 SDK or SWIFT API client
     - ISO 20022 (JAXB)
     - Sanctions Screening Library (WorldCheck API or local DB)
     - FX Rate API (Reuters/Bloomberg)
     - Circuit breaker, retry

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 SWIFT Flow
        ```
        PaymentRoutedEvent (clearingSystem=SWIFT)
            ↓
        SWIFT Adapter
            ↓
        1. Sanctions Screening (mandatory!)
           - Screen debtor name
           - Screen creditor name
           - Screen any intermediaries
           - If match → BLOCK payment
        2. FX Rate Lookup (if currency conversion needed)
        3. Correspondent Bank Routing
        4. Build MT103 or pacs.008 message
        5. Submit to SWIFT network
        6. Wait for ACK/NAK
        7. Track via SWIFT gpi (optional)
        8. Publish ClearingSubmittedEvent
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - SWIFTEventConsumer
        - SWIFTService
        - SanctionsScreeningService (critical!)
        - FXRateService
        - CorrespondentBankRoutingService
        - MT103MessageBuilder
        - Pacs008MessageBuilder (ISO 20022)
        - SWIFTApiClient
        - SWIFTEventPublisher
     
     📋 Sanctions Screening
        - Input: Name, address, country
        - API: WorldCheck API or local sanctions DB
        - Fuzzy matching: Levenshtein distance
        - Threshold: 80% match → flag for review
        - Action: Block payment if match, alert compliance
     
     📋 FX Rate Service
        - API: Reuters Elektron or internal rates
        - Rate calculation: Spot rate + spread
        - Rate expiry: 60 seconds (re-quote if expired)
  
  3. Implementation:
     📁 /services/swift-adapter/
        ├─ src/main/java/com/payments/swift/
        │   ├─ SWIFTAdapterApplication.java
        │   ├─ consumer/
        │   │   └─ PaymentEventConsumer.java
        │   ├─ service/
        │   │   ├─ SWIFTService.java
        │   │   ├─ SanctionsScreeningService.java (critical!)
        │   │   ├─ FXRateService.java
        │   │   ├─ CorrespondentBankRoutingService.java
        │   │   └─ SWIFTEventPublisher.java
        │   ├─ client/
        │   │   ├─ SWIFTApiClient.java
        │   │   ├─ WorldCheckClient.java (sanctions)
        │   │   └─ ReutersAPIClient.java (FX rates)
        │   ├─ builder/
        │   │   ├─ MT103MessageBuilder.java
        │   │   └─ Pacs008MessageBuilder.java
        │   ├─ repository/
        │   │   └─ SWIFTSubmissionRepository.java
        │   ├─ model/
        │   │   ├─ SWIFTSubmissionEntity.java
        │   │   ├─ SanctionsScreeningResult.java
        │   │   ├─ FXRate.java
        │   │   └─ CorrespondentBankRoute.java
        │   ├─ dto/
        │   │   ├─ MT103Message.java
        │   │   └─ SWIFTAcknowledgment.java
        │   ├─ config/
        │   │   ├─ SWIFTClientConfig.java
        │   │   └─ CircuitBreakerConfig.java
        │   └─ exception/
        │       ├─ SanctionsMatchException.java (critical!)
        │       ├─ FXRateUnavailableException.java
        │       └─ SWIFTApiException.java
        ├─ src/test/java/
        │   ├─ service/SWIFTServiceTest.java
        │   ├─ service/SanctionsScreeningServiceTest.java
        │   ├─ service/FXRateServiceTest.java
        │   ├─ builder/MT103MessageBuilderTest.java
        │   └─ integration/SWIFTIntegrationTest.java
        ├─ Dockerfile
        ├─ k8s/deployment.yaml
        └─ README.md
     
     Key Implementation (SanctionsScreeningService.java):
     ```java
     @Service
     @Slf4j
     public class SanctionsScreeningService {
         
         @Autowired
         private WorldCheckClient worldCheckClient;
         
         /**
          * CRITICAL: Mandatory sanctions screening
          * MUST be called before EVERY SWIFT payment
          */
         public SanctionsScreeningResult screenPayment(Payment payment) {
             log.info("Starting sanctions screening: paymentId={}", payment.getPaymentId());
             
             List<SanctionsMatch> matches = new ArrayList<>();
             
             // 1. Screen debtor
             SanctionsMatch debtorMatch = screenEntity(
                 payment.getDebtorName(),
                 payment.getDebtorAddress(),
                 payment.getDebtorCountry()
             );
             if (debtorMatch != null) {
                 matches.add(debtorMatch);
             }
             
             // 2. Screen creditor
             SanctionsMatch creditorMatch = screenEntity(
                 payment.getCreditorName(),
                 payment.getCreditorAddress(),
                 payment.getCreditorCountry()
             );
             if (creditorMatch != null) {
                 matches.add(creditorMatch);
             }
             
             // 3. Screen intermediaries (if any)
             for (Intermediary intermediary : payment.getIntermediaries()) {
                 SanctionsMatch match = screenEntity(
                     intermediary.getName(),
                     intermediary.getAddress(),
                     intermediary.getCountry()
                 );
                 if (match != null) {
                     matches.add(match);
                 }
             }
             
             // 4. Determine action
             if (!matches.isEmpty()) {
                 log.error("SANCTIONS MATCH DETECTED: paymentId={}, matches={}",
                     payment.getPaymentId(), matches.size());
                 
                 // CRITICAL: Block payment immediately
                 return SanctionsScreeningResult.blocked(matches);
             }
             
             log.info("Sanctions screening passed: paymentId={}", payment.getPaymentId());
             return SanctionsScreeningResult.passed();
         }
         
         private SanctionsMatch screenEntity(String name, String address, String country) {
             // Call WorldCheck API or local sanctions DB
             SanctionsScreeningRequest request = new SanctionsScreeningRequest();
             request.setName(name);
             request.setAddress(address);
             request.setCountry(country);
             
             SanctionsScreeningResponse response = worldCheckClient.screen(request);
             
             // Fuzzy matching (80% threshold)
             if (response.hasMatch() && response.getMatchScore() >= 80) {
                 return new SanctionsMatch(
                     name,
                     response.getMatchedName(),
                     response.getMatchScore(),
                     response.getSanctionsList() // OFAC, UN, EU
                 );
             }
             
             return null;
         }
     }
     ```
     
     Key Implementation (SWIFTService.java):
     ```java
     @Service
     @Slf4j
     public class SWIFTService {
         
         @Autowired
         private SanctionsScreeningService sanctionsService;
         
         @Autowired
         private FXRateService fxRateService;
         
         @Autowired
         private CorrespondentBankRoutingService routingService;
         
         @Autowired
         private MT103MessageBuilder mt103Builder;
         
         @Autowired
         private SWIFTApiClient swiftClient;
         
         @Transactional
         public void submitToSWIFT(PaymentRoutedEvent event) {
             log.info("Submitting payment to SWIFT: paymentId={}", event.getPaymentId());
             
             try {
                 // 1. CRITICAL: Sanctions screening (mandatory!)
                 SanctionsScreeningResult screening = sanctionsService.screenPayment(event);
                 
                 if (screening.isBlocked()) {
                     log.error("Payment blocked due to sanctions match: paymentId={}",
                         event.getPaymentId());
                     
                     // Publish failure event
                     eventPublisher.publishClearingFailed(
                         event.getPaymentId(),
                         "SWIFT",
                         "SANCTIONS_MATCH: " + screening.getMatches()
                     );
                     return;
                 }
                 
                 // 2. FX Rate lookup (if currency conversion needed)
                 FXRate fxRate = null;
                 if (!event.getDebtorCurrency().equals(event.getCreditorCurrency())) {
                     fxRate = fxRateService.getRate(
                         event.getDebtorCurrency(),
                         event.getCreditorCurrency()
                     );
                 }
                 
                 // 3. Correspondent bank routing
                 CorrespondentBankRoute route = routingService.determineRoute(
                     event.getDebtorCountry(),
                     event.getCreditorCountry(),
                     event.getCurrency()
                 );
                 
                 // 4. Build MT103 message
                 MT103Message mt103 = mt103Builder.build(event, fxRate, route);
                 
                 // 5. Submit to SWIFT
                 SWIFTAcknowledgment ack = swiftClient.submitMessage(mt103);
                 
                 if (ack.isAccepted()) {
                     // Publish success event
                     eventPublisher.publishClearingSubmitted(
                         event.getPaymentId(),
                         "SWIFT",
                         ack.getReference()
                     );
                     
                     log.info("Payment submitted to SWIFT: reference={}", ack.getReference());
                 } else {
                     // Publish failure event
                     eventPublisher.publishClearingFailed(
                         event.getPaymentId(),
                         "SWIFT",
                         ack.getReasonText()
                     );
                 }
             } catch (Exception e) {
                 log.error("SWIFT submission failed: paymentId={}", event.getPaymentId(), e);
                 throw new SWIFTApiException("SWIFT submission failed", e);
             }
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Sanctions Screening Tests (critical!)
        - No match (pass)
        - Debtor match (block)
        - Creditor match (block)
        - Intermediary match (block)
        - Fuzzy matching (80% threshold)
     
     ✅ FX Rate Tests
        - Rate lookup successful
        - Rate expiry handling
        - Rate unavailable (fallback)
     
     ✅ MT103 Builder Tests
        - MT103 message generation
        - Field validation
        - Currency conversion
     
     ✅ SWIFT Service Tests
        - Full flow: screening → FX → routing → submit
        - Sanctions block scenario
        - Successful submission
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - SWIFT overview
        - Sanctions screening (mandatory!)
        - MT103 vs pacs.008
        - Testing
     
     📄 SANCTIONS-SCREENING.md
        - Screening requirements
        - Sanctions lists (OFAC, UN, EU)
        - Fuzzy matching algorithm
        - Compliance procedures
     
     📄 MT103-REFERENCE.md
        - MT103 field reference
        - Message examples
        - Validation rules
     
     📄 CORRESPONDENT-BANKING.md
        - Corridor routing
        - Correspondent bank list
        - BIC codes

Success Criteria:
  ✅ Sanctions screening working (mandatory!)
  ✅ FX rate lookup successful
  ✅ MT103 message generation correct
  ✅ SWIFT submission successful
  ✅ Tests pass (80%+ coverage)
  ✅ Documentation complete

Validation Checklist:
  - [ ] Sanctions screening ALWAYS called
  - [ ] Payment blocked if sanctions match
  - [ ] FX rate lookup (if currency conversion)
  - [ ] Correspondent routing correct
  - [ ] MT103 format validated
  - [ ] SWIFT API integration tested
  - [ ] Tests passing (80%+ coverage)
  - [ ] Compliance documentation complete

Context Sufficiency: ✅ SUFFICIENT
  - Complete SWIFT spec in docs/27 (2,200 lines)
  - MT103 format detailed
  - pacs.008 format (ISO 20022)
  - Sanctions screening requirements
  - FX rate lookup approach
  - Correspondent banking patterns
  - Code examples provided

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Feature 2.1 (SAMOS Adapter) - can reuse ISO 20022 builder
  ✅ External APIs: WorldCheck (sanctions), Reuters (FX rates)
```

---

## Phase 3: Platform Services (5 Features)

### Feature 3.1: Tenant Management Service

```yaml
Feature ID: 3.1
Feature Name: Tenant Management Service
Agent Name: Tenant Management Agent
Phase: 3 (Platform Services)
Estimated Time: 4 days

Role & Expertise:
  You are a Backend Engineer with expertise in multi-tenancy, hierarchical data
  models, Row-Level Security (PostgreSQL RLS), API design, and tenant lifecycle
  management.

Task:
  Build the Tenant Management Service - manages tenant lifecycle, hierarchical
  structure (Tenant → Business Unit → Customer), tenant-specific configurations,
  and provides tenant context lookup for all services.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #15 section)
        - Responsibilities
        - API endpoints (8 endpoints)
        - Tenant hierarchy
     
     📄 docs/12-TENANT-MANAGEMENT.md (COMPLETE FILE - 1,500 lines)
        - Multi-tenancy overview
        - 3-level hierarchy (Tenant → Business Unit → Customer)
        - Row-Level Security (RLS)
        - Tenant context propagation (X-Tenant-ID header)
        - Tenant-specific configuration
        - Automated onboarding (7 steps)
        - Tenant usage tracking
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 3.1)
  
  2. Domain Models (from Phase 0):
     ✅ Tenant.java (Aggregate Root)
     ✅ BusinessUnit.java (Entity)
     ✅ Customer.java (Entity)
     ✅ TenantId.java (Value Object)
     ✅ TenantConfiguration.java (Value Object)
  
  3. Database Schema (from Phase 0):
     ✅ Table: tenants
        - tenant_id (PK)
        - tenant_name (VARCHAR, UNIQUE)
        - status (VARCHAR) -- ACTIVE, SUSPENDED, DEACTIVATED
        - created_at, updated_at
     
     ✅ Table: business_units
        - business_unit_id (PK)
        - tenant_id (FK)
        - name (VARCHAR)
        - created_at
     
     ✅ Table: tenant_configurations
        - config_id (PK)
        - tenant_id (FK)
        - config_key (VARCHAR)
        - config_value (TEXT) -- JSON
        - config_type (VARCHAR)
  
  4. Tenant Hierarchy:
     ```
     Tenant (Bank/FI)
       ├─ Business Unit 1 (Retail Banking)
       │   ├─ Customer A
       │   ├─ Customer B
       │   └─ Customer C
       ├─ Business Unit 2 (Corporate Banking)
       │   ├─ Customer D
       │   └─ Customer E
       └─ Business Unit 3 (Investment Banking)
           └─ Customer F
     ```
  
  5. API Endpoints to Implement:
     POST   /api/v1/tenants (create tenant)
     GET    /api/v1/tenants/{id} (get tenant)
     PUT    /api/v1/tenants/{id} (update tenant)
     DELETE /api/v1/tenants/{id} (deactivate tenant)
     POST   /api/v1/tenants/{id}/business-units (create BU)
     GET    /api/v1/tenants/{id}/business-units (list BUs)
     GET    /api/v1/tenants/{id}/config (get tenant config)
     PUT    /api/v1/tenants/{id}/config (update tenant config)
     GET    /api/v1/tenants/{id}/usage (usage metrics)
  
  6. Tenant Configuration Types:
     - Limit Configuration (daily, monthly limits per payment type)
     - Fraud Rules (risk thresholds, alert settings)
     - Clearing Credentials (SAMOS participant code, etc.)
     - Core Banking Endpoints (URLs for 6 core banking systems)
     - Notification Settings (email, SMS templates)
     - Fee Configuration (fee structure per payment type)
  
  7. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring Data JPA
     - PostgreSQL (with RLS)
     - Redis (tenant config cache)
     - gRPC (for fast tenant lookups by other services)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Component Diagram
        - REST Controller (8 endpoints)
        - Tenant Service (business logic)
        - Configuration Service
        - Usage Tracking Service
        - Repository Layer
        - gRPC Server (tenant lookup)
     
     📊 Tenant Hierarchy Diagram
        - Tenant → Business Unit → Customer
        - Relationship cardinality
     
     📊 Automated Onboarding Flow
        ```
        POST /api/v1/tenants
            ↓
        1. Create tenant record
        2. Create default business unit
        3. Create tenant configuration (with defaults)
        4. Enable RLS for tenant
        5. Publish TenantCreatedEvent
        6. Create API keys
        7. Return tenant credentials
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - TenantController
        - TenantService
        - BusinessUnitService
        - ConfigurationService
        - UsageTrackingService
        - TenantRepository
        - BusinessUnitRepository
        - ConfigurationRepository
        - TenantLookupGrpcService (gRPC)
     
     📋 API Contract (OpenAPI 3.0)
        ```yaml
        paths:
          /api/v1/tenants:
            post:
              summary: Create tenant
              requestBody:
                schema:
                  type: object
                  properties:
                    tenantName:
                      type: string
                    contactEmail:
                      type: string
                    plan:
                      type: string
                      enum: [BASIC, PREMIUM, ENTERPRISE]
              responses:
                '201':
                  description: Tenant created
                  schema:
                    $ref: '#/components/schemas/TenantResponse'
        ```
     
     📋 gRPC Service Definition
        ```protobuf
        service TenantLookupService {
          rpc GetTenantById(TenantIdRequest) returns (TenantResponse);
          rpc GetTenantByName(TenantNameRequest) returns (TenantResponse);
          rpc GetTenantConfig(TenantConfigRequest) returns (TenantConfigResponse);
        }
        
        message TenantIdRequest {
          string tenant_id = 1;
        }
        
        message TenantResponse {
          string tenant_id = 1;
          string tenant_name = 2;
          string status = 3;
          map<string, string> config = 4;
        }
        ```
  
  3. Implementation:
     📁 /services/tenant-management-service/
        ├─ src/main/java/com/payments/tenant/
        │   ├─ TenantManagementApplication.java
        │   ├─ controller/
        │   │   ├─ TenantController.java
        │   │   └─ BusinessUnitController.java
        │   ├─ service/
        │   │   ├─ TenantService.java
        │   │   ├─ BusinessUnitService.java
        │   │   ├─ ConfigurationService.java
        │   │   ├─ UsageTrackingService.java
        │   │   └─ TenantEventPublisher.java
        │   ├─ grpc/
        │   │   └─ TenantLookupGrpcService.java
        │   ├─ repository/
        │   │   ├─ TenantRepository.java
        │   │   ├─ BusinessUnitRepository.java
        │   │   └─ ConfigurationRepository.java
        │   ├─ model/
        │   │   ├─ TenantEntity.java
        │   │   ├─ BusinessUnitEntity.java
        │   │   └─ TenantConfigurationEntity.java
        │   ├─ dto/
        │   │   ├─ TenantRequest.java
        │   │   ├─ TenantResponse.java
        │   │   ├─ BusinessUnitRequest.java
        │   │   └─ ConfigurationRequest.java
        │   ├─ mapper/
        │   │   └─ TenantMapper.java
        │   ├─ config/
        │   │   ├─ RedisConfig.java
        │   │   ├─ GrpcConfig.java
        │   │   └─ SecurityConfig.java
        │   └─ exception/
        │       ├─ TenantNotFoundException.java
        │       └─ TenantAlreadyExistsException.java
        ├─ src/main/proto/
        │   └─ tenant_lookup.proto (gRPC service definition)
        ├─ src/test/java/
        │   ├─ controller/TenantControllerTest.java
        │   ├─ service/TenantServiceTest.java
        │   ├─ grpc/TenantLookupGrpcServiceTest.java
        │   └─ integration/TenantIntegrationTest.java
        ├─ Dockerfile
        ├─ k8s/deployment.yaml
        └─ README.md
     
     Key Implementation (TenantService.java):
     ```java
     @Service
     @Slf4j
     public class TenantService {
         
         @Autowired
         private TenantRepository tenantRepository;
         
         @Autowired
         private ConfigurationService configService;
         
         @Autowired
         private TenantEventPublisher eventPublisher;
         
         @Transactional
         public TenantResponse createTenant(TenantRequest request) {
             log.info("Creating tenant: name={}", request.getTenantName());
             
             // 1. Validate uniqueness
             if (tenantRepository.existsByTenantName(request.getTenantName())) {
                 throw new TenantAlreadyExistsException(request.getTenantName());
             }
             
             // 2. Create tenant
             TenantEntity tenant = new TenantEntity();
             tenant.setTenantId(UUID.randomUUID().toString());
             tenant.setTenantName(request.getTenantName());
             tenant.setContactEmail(request.getContactEmail());
             tenant.setPlan(request.getPlan());
             tenant.setStatus(TenantStatus.ACTIVE);
             tenant.setCreatedAt(Instant.now());
             tenantRepository.save(tenant);
             
             // 3. Create default configuration
             configService.createDefaultConfiguration(tenant.getTenantId(), request.getPlan());
             
             // 4. Publish event
             eventPublisher.publishTenantCreated(tenant);
             
             log.info("Tenant created successfully: tenantId={}", tenant.getTenantId());
             
             return TenantMapper.toResponse(tenant);
         }
         
         @Transactional(readOnly = true)
         public TenantResponse getTenant(String tenantId) {
             TenantEntity tenant = tenantRepository.findById(tenantId)
                 .orElseThrow(() -> new TenantNotFoundException(tenantId));
             
             return TenantMapper.toResponse(tenant);
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Controller Tests
        - Create tenant (201 Created)
        - Get tenant (200 OK)
        - Update tenant (200 OK)
        - Tenant not found (404)
        - Duplicate tenant (409 Conflict)
     
     ✅ Service Tests
        - createTenant() success
        - createTenant() duplicate
        - Hierarchy management
        - Configuration CRUD
     
     ✅ gRPC Tests
        - Tenant lookup by ID
        - Tenant lookup by name
        - Config retrieval
     
     ✅ Integration Tests
        - End-to-end tenant creation
        - Hierarchy creation
        - RLS validation
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - Tenant management overview
        - API endpoints
        - gRPC service
        - How to run locally
     
     📄 TENANT-HIERARCHY.md
        - 3-level hierarchy explained
        - Hierarchy management
        - RLS policies
     
     📄 TENANT-ONBOARDING.md
        - Automated onboarding flow
        - Configuration defaults
        - API key generation

Success Criteria:
  ✅ Service builds successfully
  ✅ All tests pass (60+ tests)
  ✅ Code coverage ≥ 80%
  ✅ REST API functional
  ✅ gRPC service functional
  ✅ RLS working (tenant isolation)
  ✅ Docker image builds
  ✅ Service deploys to AKS
  ✅ Documentation complete

Context Sufficiency: ✅ SUFFICIENT
  - Complete tenant spec in docs/12-TENANT-MANAGEMENT.md
  - Hierarchy model clear
  - RLS patterns provided
  - gRPC examples included

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
```

---

### Feature 3.2: IAM Service

```yaml
Feature ID: 3.2
Feature Name: IAM Service (Identity & Access Management)
Agent Name: IAM Agent
Phase: 3 (Platform Services)
Estimated Time: 5 days

Role & Expertise:
  You are a Security Engineer with expertise in OAuth 2.0, OIDC, JWT tokens,
  RBAC (Role-Based Access Control), ABAC (Attribute-Based Access Control),
  Azure AD B2C, and Spring Security.

Task:
  Build the IAM Service - handles user authentication (login/logout),
  authorization (RBAC/ABAC), JWT token generation/validation, integration
  with Azure AD B2C, and multi-tenant user management.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #19 section)
     📄 docs/21-SECURITY-ARCHITECTURE.md (Sections: Authentication, Authorization)
        Lines 150-450 (complete IAM specification)
        - OAuth 2.0 / OIDC flow
        - JWT token structure
        - RBAC roles (Admin, Operator, Viewer)
        - ABAC attributes (tenant, business unit, payment type)
        - Azure AD B2C integration
        - MFA (Multi-Factor Authentication)
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 3.2)
  
  2. Authentication Flow (OAuth 2.0):
     ```
     1. User → POST /api/v1/auth/login
        {username, password}
     
     2. IAM Service → Azure AD B2C (validate credentials)
     
     3. Azure AD B2C → Response (user validated)
     
     4. IAM Service → Generate JWT token
        {
          "sub": "user-123",
          "tenantId": "BANK-001",
          "roles": ["OPERATOR"],
          "permissions": ["payment:create", "payment:read"],
          "exp": 1633536000
        }
     
     5. Response → JWT token
     
     6. User → All subsequent requests with JWT in Authorization header
     
     7. Services → Validate JWT (symmetric or asymmetric)
     ```
  
  3. RBAC Roles:
     - **Admin**: Full access (all operations)
     - **Operator**: Create/update payments, view reports
     - **Viewer**: Read-only access
     - **Compliance**: Access to audit logs, sanctions screening
     - **Support**: Limited read access, no modifications
  
  4. ABAC Attributes:
     - Tenant ID (user belongs to which bank)
     - Business Unit ID (user's business unit)
     - Payment Type (user can process which payment types)
     - Amount Limit (max amount user can approve)
     - Geo-location (IP-based restrictions)
  
  5. JWT Token Structure:
     ```json
     {
       "sub": "user-123",
       "email": "operator@bank001.com",
       "name": "John Doe",
       "tenantId": "BANK-001",
       "businessUnitId": "BU-001",
       "roles": ["OPERATOR"],
       "permissions": [
         "payment:create",
         "payment:read",
         "payment:update",
         "report:read"
       ],
       "attributes": {
         "maxAmount": 100000,
         "allowedPaymentTypes": ["EFT", "RTC"]
       },
       "iss": "https://iam.payments.io",
       "aud": "payments-engine",
       "exp": 1633536000,
       "iat": 1633532400
     }
     ```
  
  6. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring Security 6.x
     - OAuth 2.0 Resource Server
     - Azure AD B2C (external IdP)
     - JWT (io.jsonwebtoken)
     - PostgreSQL (users, roles, permissions)
     - Redis (token blacklist for logout)
  
  7. Configuration:
     application.yml:
       azure:
         activedirectory:
           b2c:
             tenant: payments-b2c
             client-id: ${AZURE_AD_CLIENT_ID}
             client-secret: ${AZURE_AD_CLIENT_SECRET}
             user-flow: B2C_1_signupsignin
       jwt:
         secret: ${JWT_SECRET}  # For symmetric signing
         expiration: 3600  # 1 hour
         refresh-expiration: 86400  # 24 hours

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Authentication Flow Diagram (OAuth 2.0)
     📊 Authorization Flow Diagram (RBAC + ABAC)
     📊 Token Validation Flow
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - AuthController
        - AuthenticationService
        - AuthorizationService
        - JwtTokenProvider
        - AzureADB2CClient
        - UserRepository
        - RoleRepository
        - PermissionRepository
     
     📋 RBAC Model
        - User → Roles (N:M)
        - Role → Permissions (N:M)
        - Permission → Resources (1:N)
     
     📋 JWT Token Strategy
        - Signing algorithm: HS256 (symmetric) or RS256 (asymmetric)
        - Token expiry: 1 hour
        - Refresh token: 24 hours
        - Token blacklist (for logout)
  
  3. Implementation:
     📁 /services/iam-service/
        ├─ src/main/java/com/payments/iam/
        │   ├─ IAMServiceApplication.java
        │   ├─ controller/
        │   │   ├─ AuthController.java
        │   │   └─ UserController.java
        │   ├─ service/
        │   │   ├─ AuthenticationService.java
        │   │   ├─ AuthorizationService.java
        │   │   └─ UserService.java
        │   ├─ security/
        │   │   ├─ JwtTokenProvider.java
        │   │   ├─ JwtAuthenticationFilter.java
        │   │   ├─ AzureADB2CClient.java
        │   │   └─ TokenBlacklistService.java (Redis)
        │   ├─ repository/
        │   │   ├─ UserRepository.java
        │   │   ├─ RoleRepository.java
        │   │   └─ PermissionRepository.java
        │   ├─ model/
        │   │   ├─ UserEntity.java
        │   │   ├─ RoleEntity.java
        │   │   └─ PermissionEntity.java
        │   ├─ dto/
        │   │   ├─ LoginRequest.java
        │   │   ├─ LoginResponse.java
        │   │   ├─ TokenResponse.java
        │   │   └─ UserResponse.java
        │   ├─ config/
        │   │   ├─ SecurityConfig.java
        │   │   ├─ AzureADB2CConfig.java
        │   │   └─ JwtConfig.java
        │   └─ exception/
        │       ├─ InvalidCredentialsException.java
        │       ├─ TokenExpiredException.java
        │       └─ UnauthorizedException.java
        ├─ src/test/java/
        │   ├─ controller/AuthControllerTest.java
        │   ├─ service/AuthenticationServiceTest.java
        │   ├─ security/JwtTokenProviderTest.java
        │   └─ integration/IAMIntegrationTest.java
        ├─ Dockerfile
        ├─ k8s/deployment.yaml
        └─ README.md
     
     Key Implementation (JwtTokenProvider.java):
     ```java
     @Component
     @Slf4j
     public class JwtTokenProvider {
         
         @Value("${jwt.secret}")
         private String jwtSecret;
         
         @Value("${jwt.expiration}")
         private long jwtExpiration;
         
         public String generateToken(User user) {
             Map<String, Object> claims = new HashMap<>();
             claims.put("tenantId", user.getTenantId());
             claims.put("roles", user.getRoles().stream()
                 .map(Role::getName).collect(Collectors.toList()));
             claims.put("permissions", user.getPermissions());
             
             return Jwts.builder()
                 .setClaims(claims)
                 .setSubject(user.getUserId())
                 .setIssuedAt(new Date())
                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                 .signWith(SignatureAlgorithm.HS256, jwtSecret)
                 .compact();
         }
         
         public boolean validateToken(String token) {
             try {
                 Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
                 return true;
             } catch (ExpiredJwtException e) {
                 log.error("JWT token expired", e);
                 return false;
             } catch (Exception e) {
                 log.error("JWT token invalid", e);
                 return false;
             }
         }
         
         public String getTenantIdFromToken(String token) {
             Claims claims = Jwts.parser()
                 .setSigningKey(jwtSecret)
                 .parseClaimsJws(token)
                 .getBody();
             return claims.get("tenantId", String.class);
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Authentication Tests
        - Login success
        - Login failure (invalid credentials)
        - Logout (token blacklist)
        - Token refresh
     
     ✅ Authorization Tests
        - RBAC: User with role can access
        - RBAC: User without role denied
        - ABAC: Attribute match allowed
        - ABAC: Attribute mismatch denied
     
     ✅ JWT Tests
        - Token generation
        - Token validation
        - Token expiry
        - Token claims extraction
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - IAM overview
        - OAuth 2.0 flow
        - JWT structure
        - How to test
     
     📄 AUTHENTICATION-GUIDE.md
        - Login flow
        - Azure AD B2C setup
        - MFA configuration
     
     📄 AUTHORIZATION-GUIDE.md
        - RBAC roles
        - ABAC attributes
        - Permission model

Success Criteria:
  ✅ Authentication working (Azure AD B2C)
  ✅ JWT generation/validation working
  ✅ RBAC enforced
  ✅ ABAC enforced
  ✅ Tests pass (80%+ coverage)
  ✅ Documentation complete

Context Sufficiency: ✅ SUFFICIENT
  - Complete security spec in docs/21
  - OAuth 2.0 flow detailed
  - JWT structure provided
  - RBAC/ABAC models clear

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Feature 3.1 (Tenant Management) - for tenant lookup
  ✅ Azure AD B2C (configured)
```

---

### Feature 3.3: Audit Service

```yaml
Feature ID: 3.3
Feature Name: Audit Service
Agent Name: Audit Agent
Phase: 3 (Platform Services)
Estimated Time: 3 days

Role & Expertise:
  You are a Backend Engineer with expertise in event-driven architecture,
  immutable audit logs, CosmosDB (NoSQL), and compliance (7-year retention).

Task:
  Build the Audit Service - consumes ALL events from the system, persists
  immutable audit logs to CosmosDB, provides audit trail API, and supports
  compliance reporting.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #20 section)
     📄 docs/21-SECURITY-ARCHITECTURE.md (Section: Audit Trail)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 3.3)
  
  2. Domain Models (from Phase 0):
     ✅ AuditLog.java (Immutable)
     ✅ AuditEvent.java (Value Object)
  
  3. Event Schemas (from Phase 0):
     ✅ ALL 25+ events (audit service listens to ALL)
  
  4. Database: Azure CosmosDB (NOT PostgreSQL)
     - Container: audit_log
     - Partition Key: tenant_id
     - TTL: 7 years (2,555 days)
     - Indexing: eventType, entityId, timestamp
  
  5. Audit Log Structure:
     ```json
     {
       "auditId": "audit-uuid",
       "eventId": "event-uuid",
       "eventType": "PaymentInitiatedEvent",
       "entityType": "Payment",
       "entityId": "PAY-2025-XXXXXX",
       "action": "CREATE",
       "actor": {
         "userId": "user-123",
         "tenantId": "BANK-001",
         "ipAddress": "192.168.1.1",
         "userAgent": "Mozilla/5.0..."
       },
       "changes": {
         "before": null,
         "after": {
           "amount": 10000.00,
           "status": "INITIATED"
         }
       },
       "timestamp": "2025-10-12T10:00:00Z",
       "correlationId": "corr-uuid",
       "tenantId": "BANK-001"
     }
     ```
  
  6. API Endpoints to Implement:
     GET    /api/v1/audit/entity/{entityId} (get audit trail for entity)
     POST   /api/v1/audit/search (search audit logs)
     GET    /api/v1/audit/export (export for compliance)
  
  7. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Azure CosmosDB SDK
     - Azure Service Bus (event consumption)
     - Spring Data CosmosDB
     - Reactive (Spring WebFlux) - for high throughput

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Audit Flow
        ```
        ALL Events (25+ types)
            ↓
        Audit Service (consumes all)
            ↓
        Transform to AuditLog
            ↓
        Persist to CosmosDB (immutable)
            ↓
        Indexed by: tenant_id, eventType, entityId, timestamp
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - AuditEventConsumer (consumes ALL events)
        - AuditService
        - AuditLogTransformer (event → audit log)
        - AuditRepository (CosmosDB)
        - AuditQueryService (search)
     
     📋 CosmosDB Schema
        - Container: audit_log
        - Partition key: /tenantId
        - Indexes: eventType, entityId, timestamp
        - TTL: 7 years
  
  3. Implementation:
     📁 /services/audit-service/
        ├─ src/main/java/com/payments/audit/
        │   ├─ AuditServiceApplication.java
        │   ├─ consumer/
        │   │   └─ AuditEventConsumer.java (ALL events)
        │   ├─ service/
        │   │   ├─ AuditService.java
        │   │   ├─ AuditLogTransformer.java
        │   │   └─ AuditQueryService.java
        │   ├─ repository/
        │   │   └─ AuditRepository.java (CosmosDB)
        │   ├─ model/
        │   │   ├─ AuditLog.java (immutable)
        │   │   └─ AuditQuery.java
        │   ├─ controller/
        │   │   └─ AuditController.java
        │   ├─ config/
        │   │   ├─ CosmosDBConfig.java
        │   │   └─ ServiceBusConfig.java
        │   └─ exception/
        │       └─ AuditNotFoundException.java
        ├─ src/test/java/
        │   ├─ service/AuditServiceTest.java
        │   ├─ consumer/AuditEventConsumerTest.java
        │   └─ integration/AuditIntegrationTest.java (with CosmosDB emulator)
        ├─ Dockerfile
        └─ README.md
     
     Key Implementation (AuditService.java):
     ```java
     @Service
     @Slf4j
     public class AuditService {
         
         @Autowired
         private AuditRepository auditRepository;
         
         public void createAuditLog(DomainEvent event) {
             log.debug("Creating audit log: eventType={}", event.getEventType());
             
             // Transform event to audit log
             AuditLog auditLog = AuditLog.builder()
                 .auditId(UUID.randomUUID().toString())
                 .eventId(event.getEventId())
                 .eventType(event.getEventType())
                 .entityType(extractEntityType(event))
                 .entityId(extractEntityId(event))
                 .action(extractAction(event))
                 .actor(extractActor(event))
                 .changes(extractChanges(event))
                 .timestamp(event.getTimestamp())
                 .correlationId(event.getCorrelationId())
                 .tenantId(event.getTenantId())
                 .build();
             
             // Persist (immutable, no updates allowed)
             auditRepository.save(auditLog);
             
             log.info("Audit log created: auditId={}, entityId={}", 
                 auditLog.getAuditId(), auditLog.getEntityId());
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Event Consumption Tests
        - All 25+ event types consumed
        - Transformation correct
        - Persistence successful
     
     ✅ Query Tests
        - Search by entityId
        - Search by eventType
        - Search by date range
        - Tenant isolation (RLS-like)
     
     ✅ CosmosDB Tests (with emulator)
        - CRUD operations
        - TTL enforcement (7 years)
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - Audit service overview
        - CosmosDB setup
        - Query API
     
     📄 AUDIT-TRAIL-GUIDE.md
        - Audit log structure
        - Retention policy (7 years)
        - Compliance reporting

Success Criteria:
  ✅ All events consumed
  ✅ CosmosDB persistence working
  ✅ Immutability enforced (no updates)
  ✅ Query API functional
  ✅ 7-year TTL configured
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT
  - Complete audit spec in docs/21
  - Event schemas (all 25+)
  - CosmosDB patterns

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Azure CosmosDB (provisioned)
```

---

### Feature 3.4: Notification Service / IBM MQ Adapter

```yaml
Feature ID: 3.4
Feature Name: Notification Service (IBM MQ Adapter)
Agent Name: Notification Agent
Phase: 3 (Platform Services)
Estimated Time: 3 days

Role & Expertise:
  You are a Backend Engineer with expertise in IBM MQ, JMS (Java Message Service),
  fire-and-forget messaging, and non-persistent queues.

Task:
  Build the Notification Service (IBM MQ Adapter) - consumes payment events
  (PaymentCompletedEvent, PaymentFailedEvent) and forwards notification requests
  to a remote notifications engine via IBM MQ (non-persistent, fire-and-forget).

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #16 section - Option 2)
     📄 docs/25-IBM-MQ-NOTIFICATIONS.md (COMPLETE FILE - 1,600 lines)
        - IBM MQ integration architecture
        - Fire-and-forget pattern
        - Non-persistent messaging
        - Message format
        - Configuration
        - Remote engine capabilities
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 3.4)
  
  2. Design Philosophy:
     ```
     Core Function:         Non-Core Function:
     Payment Processing     Notifications
     ├─ MUST succeed       ├─ CAN fail
     ├─ ACID transactions  ├─ Fire-and-forget
     ├─ Persistent         ├─ Non-persistent
     ├─ Synchronous        ├─ Asynchronous
     └─ Zero data loss     └─ Best-effort delivery
     
     Therefore: Payment succeeds even if notification fails ✅
     ```
  
  3. IBM MQ Configuration:
     - Queue Manager: PAYMENTS_QM
     - Queue: PAYMENTS.NOTIFICATIONS.OUT
     - Delivery Mode: NON_PERSISTENT (fire-and-forget)
     - Put Timeout: 1 second max (no blocking)
     - Connection: TCP/IP (host:port)
  
  4. Message Format (sent to IBM MQ):
     ```json
     {
       "messageType": "NOTIFICATION_REQUEST",
       "notificationType": "PAYMENT_COMPLETED",
       "recipient": {
         "customerId": "CUST-12345",
         "phone": "+27821234567",
         "email": "customer@example.com"
       },
       "payload": {
         "paymentId": "PAY-67890",
         "amount": 10000.00,
         "currency": "ZAR",
         "status": "COMPLETED"
       },
       "templateId": "payment_completed_v1",
       "timestamp": "2025-10-12T10:00:00Z"
     }
     ```
  
  5. Technology Stack:
     - Java 17, Spring Boot 3.2
     - IBM MQ JMS Spring Boot Starter
     - Azure Service Bus (event consumption)
     - NO database (fire-and-forget)
  
  6. Configuration:
     application.yml:
       ibm:
         mq:
           queue-manager: PAYMENTS_QM
           host: ibmmq.payments.io
           port: 1414
           channel: PAYMENTS.CHANNEL
           notification-queue: PAYMENTS.NOTIFICATIONS.OUT
           delivery-mode: NON_PERSISTENT  # Fire-and-forget
           put-timeout: 1000  # 1 second max

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Fire-and-Forget Flow
        ```
        PaymentCompletedEvent
            ↓
        Notification Service
            ↓
        Build NotificationRequest
            ↓
        Put to IBM MQ (NON_PERSISTENT, 1s timeout)
            ↓
        If success: Log "notification sent"
        If failure: Log "notification failed (ignored)"
            ↓
        Continue (don't throw exception)
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - NotificationEventConsumer
        - NotificationService
        - IbmMqAdapter (JMS)
        - NotificationRequestBuilder
     
     📋 IBM MQ Integration
        - JMS ConnectionFactory
        - JMS Template (Spring)
        - Non-persistent delivery mode
        - 1 second put timeout
  
  3. Implementation:
     📁 /services/notification-service/
        ├─ src/main/java/com/payments/notification/
        │   ├─ NotificationServiceApplication.java
        │   ├─ consumer/
        │   │   └─ PaymentEventConsumer.java
        │   ├─ service/
        │   │   ├─ NotificationService.java
        │   │   └─ IbmMqAdapter.java
        │   ├─ builder/
        │   │   └─ NotificationRequestBuilder.java
        │   ├─ model/
        │   │   ├─ NotificationRequest.java
        │   │   └─ NotificationType.java (Enum)
        │   ├─ config/
        │   │   ├─ IbmMqConfig.java
        │   │   └─ ServiceBusConfig.java
        │   └─ exception/
        │       └─ NotificationException.java
        ├─ src/test/java/
        │   ├─ service/NotificationServiceTest.java
        │   ├─ adapter/IbmMqAdapterTest.java (with embedded ActiveMQ)
        │   └─ integration/NotificationIntegrationTest.java
        ├─ Dockerfile
        └─ README.md
     
     Key Implementation (IbmMqAdapter.java):
     ```java
     @Service
     @Slf4j
     public class IbmMqAdapter {
         
         @Autowired
         private JmsTemplate jmsTemplate;
         
         public boolean sendNotification(NotificationRequest notification) {
             try {
                 // Send to IBM MQ (NON_PERSISTENT, fire-and-forget)
                 jmsTemplate.send(session -> {
                     TextMessage message = session.createTextMessage(toJson(notification));
                     message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                     message.setJMSPriority(4);
                     message.setJMSExpiration(System.currentTimeMillis() + 300000); // 5 min
                     return message;
                 });
                 
                 log.info("Notification sent to IBM MQ: {}", notification.getNotificationType());
                 return true;
             } catch (JmsException e) {
                 // Don't throw - payment already succeeded
                 log.warn("Failed to send notification (fire-and-forget, ignoring): {}", 
                     e.getMessage());
                 return false;
             }
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Notification Service Tests
        - PaymentCompletedEvent → notification sent
        - PaymentFailedEvent → notification sent
        - IBM MQ failure → log warning (don't throw)
     
     ✅ IBM MQ Adapter Tests (with embedded ActiveMQ)
        - Message sent successfully
        - Connection failure (graceful)
        - Timeout (1 second)
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - Notification service overview
        - IBM MQ setup
        - Fire-and-forget pattern
     
     📄 IBM-MQ-SETUP.md
        - Queue manager setup
        - Queue creation
        - Connection configuration

Success Criteria:
  ✅ IBM MQ connection working
  ✅ Notifications sent (non-persistent)
  ✅ Fire-and-forget (no exceptions on failure)
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT
  - Complete IBM MQ spec in docs/25
  - Fire-and-forget pattern clear
  - JMS configuration examples

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ IBM MQ (deployed or mocked)
```

---

### Feature 3.5: Reporting Service

```yaml
Feature ID: 3.5
Feature Name: Reporting Service
Agent Name: Reporting Agent
Phase: 3 (Platform Services)
Estimated Time: 4 days

Role & Expertise:
  You are a Backend Engineer with expertise in reporting, analytics, data
  warehousing (Azure Synapse), SQL query optimization, and business intelligence.

Task:
  Build the Reporting Service - generates transaction reports, analytics
  dashboards, compliance reports, and integrates with Azure Synapse Analytics
  for data warehousing.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #17 section)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 3.5)
  
  2. Report Types:
     - Daily Summary Report (total volume, amount, success rate)
     - Tenant Transaction Report (per tenant, per day/month)
     - Clearing System Report (per clearing system)
     - Reconciliation Report (exceptions, mismatches)
     - Compliance Report (SARB reporting)
     - Settlement Report (nostro/vostro positions)
  
  3. Data Sources:
     - PostgreSQL databases (all payment/transaction tables)
     - Azure Synapse (data warehouse for historical data)
     - Redis (real-time metrics cache)
  
  4. API Endpoints to Implement:
     GET    /api/v1/reports/daily-summary
     GET    /api/v1/reports/tenant/{tenantId}/transactions
     POST   /api/v1/reports/custom (custom report with SQL)
     GET    /api/v1/analytics/transaction-volume
     GET    /api/v1/analytics/settlement-status
     POST   /api/v1/reports/export (CSV/Excel/PDF)
  
  5. Azure Synapse Integration:
     - ETL Pipeline: PostgreSQL → Synapse (nightly)
     - SQL Pool: Dedicated or serverless
     - PolyBase: Query external data
     - Materialized Views: Pre-aggregated data
  
  6. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring Data JPA (PostgreSQL)
     - Azure Synapse SDK
     - Apache POI (Excel export)
     - iText (PDF export)
     - JasperReports (report templates)
     - Redis (metrics cache)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Reporting Architecture
        ```
        Real-time Data (PostgreSQL)
            ↓
        Reporting Service (queries)
            ↓
        Generate Report (JasperReports)
            ↓
        Export (CSV/Excel/PDF)
        
        Historical Data (Azure Synapse)
            ↓
        ETL Pipeline (nightly)
            ↓
        Reporting Service (analytics queries)
            ↓
        Dashboards (aggregated data)
        ```
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - ReportController
        - ReportService
        - AnalyticsService
        - ReportGenerator (JasperReports)
        - ExportService (CSV, Excel, PDF)
        - SynapseClient
        - ReportRepository
     
     📋 Report Templates
        - daily_summary.jrxml
        - tenant_transactions.jrxml
        - compliance_report.jrxml
  
  3. Implementation:
     📁 /services/reporting-service/
        ├─ src/main/java/com/payments/reporting/
        │   ├─ ReportingServiceApplication.java
        │   ├─ controller/
        │   │   ├─ ReportController.java
        │   │   └─ AnalyticsController.java
        │   ├─ service/
        │   │   ├─ ReportService.java
        │   │   ├─ AnalyticsService.java
        │   │   ├─ ReportGenerator.java
        │   │   └─ ExportService.java
        │   ├─ client/
        │   │   └─ SynapseClient.java
        │   ├─ repository/
        │   │   ├─ ReportRepository.java
        │   │   └─ AnalyticsRepository.java
        │   ├─ model/
        │   │   ├─ Report.java
        │   │   └─ ReportMetadata.java
        │   ├─ dto/
        │   │   ├─ DailySummaryReport.java
        │   │   ├─ TenantTransactionReport.java
        │   │   └─ CustomReportRequest.java
        │   ├─ config/
        │   │   ├─ JasperConfig.java
        │   │   ├─ SynapseConfig.java
        │   │   └─ RedisConfig.java
        │   └─ exception/
        │       └─ ReportGenerationException.java
        ├─ src/main/resources/
        │   └─ reports/
        │       ├─ daily_summary.jrxml
        │       ├─ tenant_transactions.jrxml
        │       └─ compliance_report.jrxml
        ├─ src/test/java/
        │   ├─ service/ReportServiceTest.java
        │   ├─ service/AnalyticsServiceTest.java
        │   └─ integration/ReportingIntegrationTest.java
        ├─ Dockerfile
        └─ README.md
  
  4. Unit Testing:
     ✅ Report Generation Tests
        - Daily summary report
        - Tenant transaction report
        - Custom report
     
     ✅ Export Tests
        - CSV export
        - Excel export
        - PDF export
     
     ✅ Analytics Tests
        - Transaction volume
        - Settlement status
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - Reporting overview
        - Available reports
        - Export formats
     
     📄 REPORT-TEMPLATES.md
        - JasperReports templates
        - How to create custom reports

Success Criteria:
  ✅ Reports generated successfully
  ✅ Export formats working (CSV, Excel, PDF)
  ✅ Azure Synapse integration working
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT
  - Report types defined in docs/02
  - Azure Synapse patterns clear
  - Export examples provided

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Azure Synapse (provisioned)
```

---

## Phase 4: Advanced Features (5 Features)

### Feature 4.1: Batch Processing Service

```yaml
Feature ID: 4.1
Feature Name: Batch Processing Service
Agent Name: Batch Processing Agent
Phase: 4 (Advanced Features)
Estimated Time: 5 days

Role & Expertise:
  You are a Backend Engineer with expertise in Spring Batch, bulk payment processing,
  file parsing (CSV, Excel, XML, JSON), parallel processing, fault tolerance, and
  SFTP integration.

Task:
  Build the Batch Processing Service - processes bulk payment files uploaded by
  clients or received from clearing systems. Supports multiple file formats,
  parallel processing, fault tolerance, chunk-based processing, and SFTP integration.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #12 section)
     📄 docs/28-BATCH-PROCESSING.md (COMPLETE FILE - 2,000 lines)
        - Spring Batch architecture
        - File format specifications (CSV, Excel, XML, JSON)
        - Chunk-based processing
        - Parallel processing (multi-threaded)
        - Fault tolerance (skip, retry, restart)
        - SFTP integration
        - Job scheduling
        - Monitoring & reporting
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 4.1)
  
  2. Supported File Formats:
     - CSV (delimiter: comma, semicolon, pipe)
     - Excel (XLS, XLSX)
     - XML (custom schema)
     - JSON (array of payments)
     - Fixed-width (legacy systems)
  
  3. Batch File Example (CSV):
     ```csv
     PaymentId,DebtorAccount,CreditorAccount,Amount,Currency,PaymentType,Reference
     BAT-001,ACC-123,ACC-456,1000.00,ZAR,EFT,Invoice 1001
     BAT-002,ACC-124,ACC-457,2000.00,ZAR,EFT,Invoice 1002
     BAT-003,ACC-125,ACC-458,3000.00,ZAR,RTC,Invoice 1003
     ```
  
  4. Spring Batch Architecture:
     ```
     Job (Batch Payment Processing Job)
       ↓
     Step 1: File Validation
       - Validate file format
       - Validate headers
       - Check file size
       ↓
     Step 2: Payment Processing (Chunk-based)
       - ItemReader: Read file (chunk size: 100)
       - ItemProcessor: Validate + Transform payment
       - ItemWriter: Submit to Payment Initiation Service
       ↓
     Step 3: Generate Report
       - Success count
       - Failure count
       - Error report (CSV)
     ```
  
  5. Chunk-Based Processing:
     - Chunk Size: 100 payments per transaction
     - Parallel Processing: 5 threads
     - Fault Tolerance:
       - Skip on validation error (continue processing)
       - Retry on transient error (3 attempts)
       - Restart from last checkpoint on crash
  
  6. API Endpoints to Implement:
     POST   /api/v1/batch/upload (upload file)
     GET    /api/v1/batch/jobs (list batch jobs)
     GET    /api/v1/batch/jobs/{jobId} (get job status)
     GET    /api/v1/batch/jobs/{jobId}/report (download error report)
     POST   /api/v1/batch/jobs/{jobId}/restart (restart failed job)
  
  7. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring Batch 5.x
     - Apache POI (Excel parsing)
     - Jackson (JSON parsing)
     - JAXB (XML parsing)
     - Apache Camel (file routing)
     - SFTP Client (JSch)
     - PostgreSQL (job metadata)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Batch Processing Flow
        ```
        Client/SFTP → Upload File
            ↓
        Batch Service → Validate file format
            ↓
        Spring Batch Job → Launch
            ↓
        Step 1: File Validation
            ↓
        Step 2: Chunk Processing (100 payments/chunk, 5 threads)
            - Read chunk
            - Validate each payment
            - Transform to PaymentRequest
            - Submit to Payment Initiation API
            ↓
        Step 3: Generate Report
            - Success count
            - Failure count
            - Error CSV (failed payments)
        ```
     
     📊 Component Diagram
        - Batch Controller (upload, status)
        - Job Launcher
        - File Validator
        - Payment Reader (ItemReader)
        - Payment Processor (ItemProcessor)
        - Payment Writer (ItemWriter)
        - Report Generator
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - BatchController
        - BatchService
        - FileUploadService
        - FileValidator
        - PaymentBatchJob (Spring Batch Job)
        - PaymentItemReader (CSV, Excel, XML, JSON)
        - PaymentItemProcessor (validation, transformation)
        - PaymentItemWriter (submit to Payment Initiation)
        - ErrorReportGenerator
        - SFTPFilePoller
     
     📋 Spring Batch Job Configuration
        ```java
        @Bean
        public Job paymentBatchJob(JobRepository jobRepository,
                                    Step validationStep,
                                    Step processingStep,
                                    Step reportStep) {
            return new JobBuilder("paymentBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(validationStep)
                .next(processingStep)
                .next(reportStep)
                .build();
        }
        
        @Bean
        public Step processingStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    ItemReader<PaymentRecord> reader,
                                    ItemProcessor<PaymentRecord, PaymentRequest> processor,
                                    ItemWriter<PaymentRequest> writer) {
            return new StepBuilder("processingStep", jobRepository)
                .<PaymentRecord, PaymentRequest>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(1000)
                .skip(ValidationException.class)
                .retryLimit(3)
                .retry(TransientException.class)
                .taskExecutor(taskExecutor()) // 5 threads
                .build();
        }
        ```
  
  3. Implementation:
     📁 /services/batch-processing-service/
        ├─ src/main/java/com/payments/batch/
        │   ├─ BatchProcessingApplication.java
        │   ├─ controller/
        │   │   └─ BatchController.java
        │   ├─ service/
        │   │   ├─ BatchService.java
        │   │   ├─ FileUploadService.java
        │   │   └─ FileValidator.java
        │   ├─ batch/
        │   │   ├─ PaymentBatchJob.java
        │   │   ├─ reader/
        │   │   │   ├─ CsvPaymentReader.java
        │   │   │   ├─ ExcelPaymentReader.java
        │   │   │   ├─ XmlPaymentReader.java
        │   │   │   └─ JsonPaymentReader.java
        │   │   ├─ processor/
        │   │   │   └─ PaymentItemProcessor.java
        │   │   └─ writer/
        │   │       └─ PaymentItemWriter.java
        │   ├─ sftp/
        │   │   └─ SFTPFilePoller.java
        │   ├─ report/
        │   │   └─ ErrorReportGenerator.java
        │   ├─ model/
        │   │   ├─ PaymentRecord.java
        │   │   ├─ BatchJobMetadata.java
        │   │   └─ BatchJobStatus.java (Enum)
        │   ├─ dto/
        │   │   ├─ FileUploadRequest.java
        │   │   └─ BatchJobResponse.java
        │   ├─ config/
        │   │   ├─ BatchConfig.java
        │   │   ├─ SFTPConfig.java
        │   │   └─ TaskExecutorConfig.java
        │   └─ exception/
        │       ├─ FileValidationException.java
        │       └─ BatchProcessingException.java
        ├─ src/test/java/
        │   ├─ batch/PaymentBatchJobTest.java
        │   ├─ reader/CsvPaymentReaderTest.java
        │   ├─ processor/PaymentItemProcessorTest.java
        │   └─ integration/BatchIntegrationTest.java
        ├─ Dockerfile
        └─ README.md
     
     Key Implementation (PaymentItemProcessor.java):
     ```java
     @Component
     @Slf4j
     public class PaymentItemProcessor implements ItemProcessor<PaymentRecord, PaymentRequest> {
         
         @Autowired
         private ValidationService validationService;
         
         @Override
         public PaymentRequest process(PaymentRecord record) throws Exception {
             log.debug("Processing payment record: {}", record.getPaymentId());
             
             // 1. Validate payment
             validationService.validate(record);
             
             // 2. Transform to PaymentRequest
             PaymentRequest request = PaymentRequest.builder()
                 .amount(record.getAmount())
                 .currency(record.getCurrency())
                 .debtorAccountId(record.getDebtorAccount())
                 .creditorAccountId(record.getCreditorAccount())
                 .paymentType(record.getPaymentType())
                 .reference(record.getReference())
                 .build();
             
             log.debug("Payment processed: {}", record.getPaymentId());
             return request;
         }
     }
     ```
  
  4. Unit Testing:
     ✅ Batch Job Tests
        - Job execution success (100 payments)
        - Job execution with skips (validation errors)
        - Job execution with retries (transient errors)
        - Job restart from checkpoint
     
     ✅ File Reader Tests
        - CSV parsing (valid, invalid)
        - Excel parsing (XLS, XLSX)
        - XML parsing
        - JSON parsing
     
     ✅ Processor Tests
        - Validation success
        - Validation failure (skip)
        - Transformation correct
     
     ✅ Writer Tests
        - Submit to Payment Initiation API
        - Handle API errors
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
        - Batch processing overview
        - Supported file formats
        - How to upload files
        - Job monitoring
     
     📄 FILE-FORMAT-SPECS.md
        - CSV format specification
        - Excel format specification
        - XML schema
        - JSON schema
     
     📄 BATCH-PROCESSING-GUIDE.md
        - Chunk-based processing
        - Fault tolerance (skip, retry, restart)
        - Parallel processing
        - Error handling

Success Criteria:
  ✅ Batch job processes 10,000 payments successfully
  ✅ All file formats supported (CSV, Excel, XML, JSON)
  ✅ Fault tolerance working (skip, retry, restart)
  ✅ Parallel processing (5 threads)
  ✅ SFTP integration functional
  ✅ Error report generated
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT
  - Complete batch spec in docs/28
  - Spring Batch patterns provided
  - File format examples
  - Chunk processing examples

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Feature 1.1 (Payment Initiation) - for submitting payments
```

---

### Feature 4.2: Settlement Service

```yaml
Feature ID: 4.2
Feature Name: Settlement Service
Agent Name: Settlement Agent
Phase: 4 (Advanced Features)
Estimated Time: 4 days

Role & Expertise:
  You are a Backend Engineer with expertise in financial settlement, nostro/vostro
  accounting, multi-currency settlement, liquidity management, and reconciliation.

Task:
  Build the Settlement Service - manages settlement with clearing systems,
  tracks nostro/vostro positions, handles multi-currency settlement, monitors
  liquidity, and provides settlement reporting.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #13 section)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 4.2)
  
  2. Settlement Concepts:
     - Nostro Account: Our account at another bank (asset)
     - Vostro Account: Other bank's account with us (liability)
     - Net Settlement: Sum of all debits and credits
     - Gross Settlement: Individual transaction settlement (RTGS)
  
  3. Settlement Flow:
     ```
     ClearingCompletedEvent (from clearing systems)
         ↓
     Settlement Service
         ↓
     1. Determine settlement account (nostro/vostro)
     2. Calculate net/gross settlement amount
     3. Check liquidity (sufficient funds?)
     4. Update nostro/vostro position
     5. Publish SettlementCompletedEvent
     ```
  
  4. Nostro/Vostro Positions:
     ```
     Nostro Position (Our account at SARB):
     ├─ Opening Balance: R 100,000,000
     ├─ Debits (payments out): R 50,000,000
     ├─ Credits (payments in): R 30,000,000
     └─ Closing Balance: R 80,000,000
     
     Vostro Position (Bank A's account with us):
     ├─ Opening Balance: R 20,000,000
     ├─ Debits (payments out): R 10,000,000
     ├─ Credits (payments in): R 15,000,000
     └─ Closing Balance: R 25,000,000
     ```
  
  5. API Endpoints to Implement:
     GET    /api/v1/settlement/nostro/positions
     GET    /api/v1/settlement/vostro/positions
     POST   /api/v1/settlement/reconcile
     GET    /api/v1/settlement/liquidity/status
     GET    /api/v1/settlement/reports/daily
  
  6. Technology Stack:
     - Java 17, Spring Boot 3.2
     - PostgreSQL (settlement data)
     - Redis (real-time positions cache)
     - Azure Service Bus (event consumption)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Settlement Flow Diagram
     📊 Nostro/Vostro Accounting Model
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - SettlementEventConsumer
        - SettlementService
        - NostroService
        - VostroService
        - LiquidityService
        - SettlementRepository
     
     📋 Database Schema
        - nostro_accounts
        - vostro_accounts
        - settlement_transactions
  
  3. Implementation:
     📁 /services/settlement-service/
        ├─ src/main/java/com/payments/settlement/
        │   ├─ SettlementServiceApplication.java
        │   ├─ consumer/
        │   │   └─ ClearingEventConsumer.java
        │   ├─ service/
        │   │   ├─ SettlementService.java
        │   │   ├─ NostroService.java
        │   │   ├─ VostroService.java
        │   │   └─ LiquidityService.java
        │   ├─ repository/
        │   │   ├─ NostroRepository.java
        │   │   ├─ VostroRepository.java
        │   │   └─ SettlementRepository.java
        │   ├─ model/
        │   │   ├─ NostroAccount.java
        │   │   ├─ VostroAccount.java
        │   │   └─ SettlementTransaction.java
        │   ├─ controller/
        │   │   └─ SettlementController.java
        │   ├─ config/
        │   │   ├─ RedisConfig.java
        │   │   └─ ServiceBusConfig.java
        │   └─ exception/
        │       └─ InsufficientLiquidityException.java
        ├─ src/test/java/
        │   ├─ service/SettlementServiceTest.java
        │   └─ integration/SettlementIntegrationTest.java
        ├─ Dockerfile
        └─ README.md
  
  4. Unit Testing:
     ✅ Settlement Tests
        - Nostro debit/credit
        - Vostro debit/credit
        - Liquidity check
        - Insufficient funds
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
     📄 SETTLEMENT-GUIDE.md

Success Criteria:
  ✅ Nostro/vostro positions accurate
  ✅ Liquidity monitoring working
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Phase 2 (Clearing Adapters) - for clearing events
```

---

### Feature 4.3: Reconciliation Service

```yaml
Feature ID: 4.3
Feature Name: Reconciliation Service
Agent Name: Reconciliation Agent
Phase: 4 (Advanced Features)
Estimated Time: 4 days

Role & Expertise:
  You are a Backend Engineer with expertise in financial reconciliation, exception
  handling, matching algorithms, and dispute resolution.

Task:
  Build the Reconciliation Service - reconciles payment transactions with clearing
  system responses, identifies exceptions (mismatches, missing transactions),
  generates reconciliation reports, and manages dispute resolution.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #14 section)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 4.3)
  
  2. Reconciliation Types:
     - Inbound Reconciliation: Clearing responses vs our records
     - Outbound Reconciliation: Our submissions vs clearing confirmations
     - Settlement Reconciliation: Nostro/vostro vs clearing statements
  
  3. Reconciliation Flow:
     ```
     Daily Job (Scheduled 06:00)
         ↓
     1. Fetch internal transactions (T-1)
     2. Fetch clearing statements (SFTP)
     3. Match transactions (by reference)
     4. Identify exceptions:
        - Missing in clearing
        - Missing in internal
        - Amount mismatch
        - Status mismatch
     5. Generate reconciliation report
     6. Create exceptions for investigation
     ```
  
  4. Matching Algorithm:
     - Primary Key: Payment Reference
     - Secondary Keys: Amount, Date, Debtor Account
     - Tolerance: Amount ±0.01 (rounding)
  
  5. Exception Types:
     - Missing in Clearing (payment sent but no confirmation)
     - Missing in Internal (clearing received but no internal record)
     - Amount Mismatch (R1000 vs R1000.01)
     - Status Mismatch (completed vs failed)
     - Duplicate (same payment twice)
  
  6. API Endpoints to Implement:
     POST   /api/v1/reconciliation/run (manual reconciliation)
     GET    /api/v1/reconciliation/exceptions
     GET    /api/v1/reconciliation/reports/{date}
     PUT    /api/v1/reconciliation/exceptions/{id}/resolve
  
  7. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring Batch (for batch reconciliation)
     - PostgreSQL (exceptions, reports)
     - SFTP (download clearing statements)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Reconciliation Flow Diagram
     📊 Matching Algorithm
  
  2. LLD (Low-Level Design):
     📋 Class Diagram
        - ReconciliationService
        - MatchingEngine
        - ExceptionHandler
        - ReportGenerator
     
     📋 Batch Job Configuration (Spring Batch)
  
  3. Implementation:
     📁 /services/reconciliation-service/
        ├─ src/main/java/com/payments/reconciliation/
        │   ├─ ReconciliationServiceApplication.java
        │   ├─ service/
        │   │   ├─ ReconciliationService.java
        │   │   ├─ MatchingEngine.java
        │   │   └─ ExceptionHandler.java
        │   ├─ batch/
        │   │   └─ ReconciliationJob.java
        │   ├─ repository/
        │   │   ├─ ExceptionRepository.java
        │   │   └─ ReconciliationReportRepository.java
        │   ├─ model/
        │   │   ├─ ReconciliationException.java
        │   │   └─ ReconciliationReport.java
        │   ├─ controller/
        │   │   └─ ReconciliationController.java
        │   └─ sftp/
        │       └─ ClearingStatementDownloader.java
        ├─ src/test/java/
        │   ├─ service/ReconciliationServiceTest.java
        │   └─ matching/MatchingEngineTest.java
        ├─ Dockerfile
        └─ README.md
  
  4. Unit Testing:
     ✅ Matching Tests
        - Exact match
        - Amount mismatch (within tolerance)
        - Missing in clearing
        - Missing in internal
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
     📄 RECONCILIATION-GUIDE.md

Success Criteria:
  ✅ Matching algorithm accurate (99%+)
  ✅ Exceptions identified
  ✅ Reports generated
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Phase 2 (Clearing Adapters) - for clearing data
```

---

### Feature 4.4: Internal API Gateway Service (Optional with Istio)

```yaml
Feature ID: 4.4
Feature Name: Internal API Gateway Service
Agent Name: Internal Gateway Agent
Phase: 4 (Advanced Features)
Estimated Time: 3 days

Role & Expertise:
  You are a Backend Engineer with expertise in Spring Cloud Gateway, API routing,
  load balancing, circuit breakers, and service discovery.

Task:
  Build the Internal API Gateway Service - routes internal service-to-service
  API calls, provides load balancing, circuit breaking, and service discovery.
  **NOTE: This service is OPTIONAL if using Istio service mesh.**

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #18 section - marked OPTIONAL)
     📄 docs/32-GATEWAY-ARCHITECTURE-CLARIFICATION.md
        - 4 gateway layers explained
        - Internal API Gateway vs Istio
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 4.4)
  
  2. Responsibilities (if NOT using Istio):
     - Route internal API calls between services
     - Load balancing (round-robin, random)
     - Circuit breaking (Resilience4j)
     - Service discovery (Kubernetes DNS or Eureka)
     - Request tracing (correlation ID propagation)
  
  3. Routing Rules:
     ```yaml
     routes:
       - id: payment-initiation
         uri: http://payment-initiation-service:8080
         predicates:
           - Path=/internal/payments/**
       
       - id: validation
         uri: http://validation-service:8080
         predicates:
           - Path=/internal/validation/**
       
       - id: account-adapter
         uri: http://account-adapter-service:8080
         predicates:
           - Path=/internal/accounts/**
     ```
  
  4. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring Cloud Gateway 4.x
     - Resilience4j (circuit breaker)
     - Kubernetes Service Discovery

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Routing Architecture
     📊 Circuit Breaker Pattern
  
  2. LLD (Low-Level Design):
     📋 Route Configuration
     📋 Circuit Breaker Configuration
  
  3. Implementation:
     📁 /services/internal-api-gateway/
        ├─ src/main/java/com/payments/gateway/
        │   ├─ InternalGatewayApplication.java
        │   ├─ config/
        │   │   ├─ GatewayConfig.java
        │   │   └─ CircuitBreakerConfig.java
        │   └─ filter/
        │       ├─ CorrelationIdFilter.java
        │       └─ LoggingFilter.java
        ├─ src/main/resources/
        │   └─ application.yml (route configuration)
        ├─ src/test/java/
        │   └─ gateway/GatewayRoutingTest.java
        ├─ Dockerfile
        └─ README.md
  
  4. Unit Testing:
     ✅ Routing Tests
        - Route to correct service
        - Load balancing
        - Circuit breaker triggers
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md
     📄 ROUTING-GUIDE.md
     📄 ISTIO-ALTERNATIVE.md (explains when to use Istio instead)

Success Criteria:
  ✅ Routing working
  ✅ Circuit breaker working
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE

Notes:
  ⚠️ OPTIONAL: Use Istio service mesh instead for production
  ⚠️ Istio provides routing, load balancing, circuit breaking natively
```

---

### Feature 4.5: BFF Layer (3 Backend-for-Frontend Services)

```yaml
Feature ID: 4.5
Feature Name: BFF Layer (Backend-for-Frontend)
Agent Name: BFF Agent
Phase: 4 (Advanced Features)
Estimated Time: 5 days (3 BFFs: Web, Mobile, Partner)

Role & Expertise:
  You are a Full-Stack Engineer with expertise in GraphQL (Web BFF), REST API
  optimization (Mobile BFF), API aggregation, and client-specific API design.

Task:
  Build 3 Backend-for-Frontend (BFF) services:
  1. Web BFF (GraphQL) - optimized for React web app
  2. Mobile BFF (REST lightweight) - optimized for mobile apps
  3. Partner BFF (REST comprehensive) - optimized for partner integrations

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (BFF Layer section)
     📄 docs/17-BFF-BACKEND-FOR-FRONTEND.md (COMPLETE FILE - 1,800 lines)
        - BFF pattern overview
        - Why 3 BFFs (Web, Mobile, Partner)
        - GraphQL for Web
        - REST for Mobile (lightweight)
        - REST for Partner (comprehensive)
        - API aggregation patterns
        - Performance optimization
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 4.5)
  
  2. BFF Responsibilities:
     - Aggregate data from multiple microservices
     - Transform data to client-specific format
     - Optimize for client needs (Web: rich data, Mobile: minimal data)
     - Handle authentication/authorization (JWT validation)
     - Implement caching (Redis)
  
  3. Web BFF (GraphQL):
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
     
     type Account {
       id: ID!
       accountNumber: String!
       accountName: String!
       balance: Float!
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
  
  4. Mobile BFF (REST Lightweight):
     ```json
     GET /api/v1/mobile/payments/{id}
     Response:
     {
       "id": "PAY-001",
       "amount": 1000.00,
       "currency": "ZAR",
       "status": "COMPLETED",
       "createdAt": "2025-10-12T10:00:00Z"
     }
     (Minimal data, no nested objects)
     ```
  
  5. Partner BFF (REST Comprehensive):
     ```json
     GET /api/v1/partner/payments/{id}
     Response:
     {
       "id": "PAY-001",
       "amount": 1000.00,
       "currency": "ZAR",
       "status": "COMPLETED",
       "debtorAccount": {
         "id": "ACC-123",
         "accountNumber": "1234567890",
         "accountName": "John Doe",
         "balance": 50000.00
       },
       "creditorAccount": {
         "id": "ACC-456",
         "accountNumber": "9876543210",
         "accountName": "Jane Smith",
         "balance": 30000.00
       },
       "auditTrail": [ ... ],
       "fees": { ... },
       "clearingDetails": { ... },
       "createdAt": "2025-10-12T10:00:00Z"
     }
     (Comprehensive data, all nested objects)
     ```
  
  6. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring GraphQL (Web BFF)
     - Spring WebFlux (Reactive for all 3 BFFs)
     - Redis (caching)
     - JWT (authentication)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 BFF Architecture Diagram (3 BFFs)
     📊 API Aggregation Flow
  
  2. LLD (Low-Level Design):
     📋 GraphQL Schema (Web BFF)
     📋 REST API Contract (Mobile BFF)
     📋 REST API Contract (Partner BFF)
  
  3. Implementation:
     📁 /services/web-bff/
        ├─ src/main/java/com/payments/bff/web/
        │   ├─ WebBffApplication.java
        │   ├─ graphql/
        │   │   ├─ PaymentQueryResolver.java
        │   │   ├─ PaymentMutationResolver.java
        │   │   └─ AccountQueryResolver.java
        │   ├─ service/
        │   │   ├─ PaymentAggregationService.java
        │   │   └─ AccountAggregationService.java
        │   ├─ client/
        │   │   ├─ PaymentServiceClient.java
        │   │   └─ AccountServiceClient.java
        │   ├─ config/
        │   │   ├─ GraphQLConfig.java
        │   │   └─ RedisConfig.java
        │   └─ security/
        │       └─ JwtAuthenticationFilter.java
        ├─ src/main/resources/
        │   └─ graphql/
        │       └─ schema.graphqls
        ├─ Dockerfile
        └─ README.md
     
     📁 /services/mobile-bff/
        ├─ src/main/java/com/payments/bff/mobile/
        │   ├─ MobileBffApplication.java
        │   ├─ controller/
        │   │   ├─ PaymentController.java (lightweight)
        │   │   └─ AccountController.java (lightweight)
        │   ├─ service/
        │   │   └─ PaymentAggregationService.java
        │   ├─ dto/
        │   │   ├─ PaymentSummary.java (lightweight)
        │   │   └─ AccountSummary.java (lightweight)
        │   ├─ config/
        │   │   └─ WebFluxConfig.java
        │   └─ security/
        │       └─ JwtAuthenticationFilter.java
        ├─ Dockerfile
        └─ README.md
     
     📁 /services/partner-bff/
        ├─ src/main/java/com/payments/bff/partner/
        │   ├─ PartnerBffApplication.java
        │   ├─ controller/
        │   │   ├─ PaymentController.java (comprehensive)
        │   │   └─ AccountController.java (comprehensive)
        │   ├─ service/
        │   │   └─ PaymentAggregationService.java
        │   ├─ dto/
        │   │   ├─ PaymentDetail.java (comprehensive)
        │   │   └─ AccountDetail.java (comprehensive)
        │   ├─ config/
        │   │   └─ WebFluxConfig.java
        │   └─ security/
        │       └─ ApiKeyAuthenticationFilter.java
        ├─ Dockerfile
        └─ README.md
  
  4. Unit Testing:
     ✅ Web BFF Tests
        - GraphQL query resolution
        - GraphQL mutation execution
        - Data aggregation
     
     ✅ Mobile BFF Tests
        - Lightweight response
        - Performance (latency < 100ms)
     
     ✅ Partner BFF Tests
        - Comprehensive response
        - API key authentication
     
     Target Coverage: 80%+
  
  5. Documentation:
     📄 README.md (per BFF)
     📄 WEB-BFF-GRAPHQL-GUIDE.md
     📄 MOBILE-BFF-OPTIMIZATION.md
     📄 PARTNER-BFF-API-REFERENCE.md

Success Criteria:
  ✅ All 3 BFFs deployed
  ✅ GraphQL working (Web BFF)
  ✅ REST APIs working (Mobile, Partner BFFs)
  ✅ Data aggregation correct
  ✅ Performance optimized
  ✅ Tests pass (80%+ coverage)

Context Sufficiency: ✅ SUFFICIENT
  - Complete BFF spec in docs/17 (1,800 lines)
  - GraphQL schema provided
  - API contracts clear
  - Aggregation patterns explained

Dependencies:
  ✅ Phase 0 (Foundation) - COMPLETE
  ✅ Phase 1 (Core Services) - for data aggregation
  ✅ Feature 3.2 (IAM Service) - for JWT validation
```

---

## Phase 5: Infrastructure (5 Features)

### Feature 5.1: Service Mesh (Istio)

```yaml
Feature ID: 5.1
Feature Name: Service Mesh (Istio)
Agent Name: Service Mesh Agent
Phase: 5 (Infrastructure)
Estimated Time: 5 days

Role & Expertise:
  You are a Platform Engineer with expertise in Istio service mesh, mTLS,
  traffic management, observability, circuit breaking, and Kubernetes networking.

Task:
  Deploy and configure Istio service mesh for the Payments Engine - provides
  mTLS encryption, traffic management, observability, circuit breaking, and
  advanced deployment patterns (canary, blue-green).

Context Provided:

  1. Architecture Documents:
     📄 docs/19-SERVICE-MESH-ISTIO.md (COMPLETE FILE - 2,500 lines)
        - Istio architecture overview
        - Service mesh benefits
        - mTLS (mutual TLS) encryption
        - Traffic management (routing, load balancing)
        - Circuit breaking, retries, timeouts
        - Observability (distributed tracing, metrics)
        - Canary deployments
        - Fault injection (chaos testing)
     
     📄 docs/32-GATEWAY-ARCHITECTURE-CLARIFICATION.md
        - Istio replaces Internal API Gateway
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 5.1)
  
  2. Istio Components:
     - Istiod (control plane): Configuration, certificate management
     - Envoy Proxy (data plane): Sidecar injected into each pod
     - Istio Ingress Gateway: Entry point for external traffic
     - Istio Egress Gateway: Exit point for external calls
  
  3. Key Features to Configure:
     
     **1. mTLS (Mutual TLS)**
     ```yaml
     apiVersion: security.istio.io/v1beta1
     kind: PeerAuthentication
     metadata:
       name: default
       namespace: payments
     spec:
       mtls:
         mode: STRICT  # Enforce mTLS for all services
     ```
     
     **2. Traffic Management**
     ```yaml
     apiVersion: networking.istio.io/v1beta1
     kind: VirtualService
     metadata:
       name: payment-initiation
     spec:
       hosts:
       - payment-initiation-service
       http:
       - route:
         - destination:
             host: payment-initiation-service
             subset: v1
           weight: 90
         - destination:
             host: payment-initiation-service
             subset: v2
           weight: 10  # Canary: 10% traffic to v2
     ```
     
     **3. Circuit Breaking**
     ```yaml
     apiVersion: networking.istio.io/v1beta1
     kind: DestinationRule
     metadata:
       name: payment-initiation
     spec:
       host: payment-initiation-service
       trafficPolicy:
         connectionPool:
           tcp:
             maxConnections: 100
           http:
             http1MaxPendingRequests: 50
             maxRequestsPerConnection: 2
         outlierDetection:
           consecutiveErrors: 5
           interval: 30s
           baseEjectionTime: 30s
     ```
     
     **4. Retries & Timeouts**
     ```yaml
     apiVersion: networking.istio.io/v1beta1
     kind: VirtualService
     metadata:
       name: payment-initiation
     spec:
       http:
       - route:
         - destination:
             host: payment-initiation-service
         timeout: 10s
         retries:
           attempts: 3
           perTryTimeout: 3s
           retryOn: 5xx,reset,connect-failure
     ```
  
  4. Technology Stack:
     - Istio 1.20+
     - Kubernetes 1.28+
     - Helm (for installation)
     - Kiali (service mesh visualization)
     - Jaeger (distributed tracing - integrated)
     - Prometheus (metrics - integrated)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Istio Architecture Diagram
        - Control plane (Istiod)
        - Data plane (Envoy sidecars)
        - Ingress/Egress gateways
     
     📊 mTLS Flow Diagram
        - Certificate issuance
        - Certificate rotation (every 24 hours)
        - Pod-to-pod mTLS encryption
  
  2. LLD (Low-Level Design):
     📋 Istio Resource Definitions
        - PeerAuthentication (mTLS)
        - VirtualService (routing, retries, timeouts)
        - DestinationRule (circuit breaking, load balancing)
        - Gateway (ingress/egress)
        - ServiceEntry (external services)
     
     📋 Sidecar Injection Strategy
        - Automatic injection (namespace label)
        - Manual injection (istioctl)
  
  3. Implementation:
     📁 /infrastructure/istio/
        ├─ installation/
        │   ├─ istio-operator.yaml
        │   ├─ istio-values.yaml (Helm values)
        │   └─ install.sh
        ├─ security/
        │   ├─ peer-authentication.yaml (mTLS STRICT)
        │   ├─ authorization-policy.yaml (RBAC)
        │   └─ request-authentication.yaml (JWT validation)
        ├─ traffic-management/
        │   ├─ virtual-services/
        │   │   ├─ payment-initiation-vs.yaml
        │   │   ├─ validation-vs.yaml
        │   │   └─ ... (20 services)
        │   ├─ destination-rules/
        │   │   ├─ payment-initiation-dr.yaml
        │   │   ├─ validation-dr.yaml
        │   │   └─ ... (20 services)
        │   └─ gateways/
        │       ├─ istio-ingressgateway.yaml
        │       └─ istio-egressgateway.yaml
        ├─ observability/
        │   ├─ kiali.yaml (service mesh dashboard)
        │   ├─ jaeger.yaml (distributed tracing)
        │   └─ prometheus.yaml (metrics)
        ├─ fault-injection/
        │   ├─ delay-injection.yaml (latency testing)
        │   └─ abort-injection.yaml (failure testing)
        ├─ canary-deployments/
        │   ├─ payment-initiation-canary.yaml
        │   └─ rollout-strategy.yaml
        └─ README.md
  
  4. Testing:
     ✅ mTLS Verification
        - Verify certificate issuance (istioctl proxy-status)
        - Test encrypted traffic (tcpdump)
        - Verify certificate rotation (24h)
     
     ✅ Traffic Management Tests
        - Canary deployment (10% → 50% → 100%)
        - Blue-green deployment
        - A/B testing
     
     ✅ Circuit Breaker Tests
        - Trigger consecutive errors (5+)
        - Verify pod ejection
        - Verify recovery after 30s
     
     ✅ Fault Injection Tests
        - Inject 5s delay (latency testing)
        - Inject 503 error (failure testing)
     
     Target: All services running with Istio sidecars
  
  5. Documentation:
     📄 README.md
        - Istio overview
        - Installation guide
        - How to verify mTLS
        - Troubleshooting
     
     📄 ISTIO-TRAFFIC-MANAGEMENT.md
        - VirtualService examples
        - DestinationRule examples
        - Canary deployment guide
     
     📄 ISTIO-SECURITY.md
        - mTLS configuration
        - Authorization policies
        - Certificate management
     
     📄 ISTIO-OBSERVABILITY.md
        - Kiali dashboard
        - Jaeger tracing
        - Prometheus metrics

Success Criteria:
  ✅ Istio installed successfully
  ✅ All 20 services have Envoy sidecars injected
  ✅ mTLS enabled (STRICT mode)
  ✅ Circuit breakers configured
  ✅ Canary deployment successful
  ✅ Kiali dashboard accessible
  ✅ Distributed tracing working (Jaeger)

Context Sufficiency: ✅ SUFFICIENT
  - Complete Istio spec in docs/19 (2,500 lines)
  - All resource types explained
  - Traffic management patterns
  - Security configurations

Dependencies:
  ✅ Kubernetes cluster (AKS) - READY
  ✅ All 20 microservices deployed
```

---

### Feature 5.2: Monitoring Stack (Prometheus, Grafana, Jaeger)

```yaml
Feature ID: 5.2
Feature Name: Monitoring Stack (Prometheus, Grafana, Jaeger)
Agent Name: Monitoring Agent
Phase: 5 (Infrastructure)
Estimated Time: 4 days

Role & Expertise:
  You are a Platform Engineer with expertise in Prometheus, Grafana, Jaeger,
  OpenTelemetry, metrics collection, alerting, and observability.

Task:
  Deploy and configure the complete monitoring stack - Prometheus (metrics),
  Grafana (dashboards), Jaeger (distributed tracing), and alerting (Alertmanager).

Context Provided:

  1. Architecture Documents:
     📄 docs/18-DISTRIBUTED-TRACING.md (COMPLETE FILE - 1,500 lines)
        - OpenTelemetry instrumentation
        - Distributed tracing architecture
        - Jaeger deployment
        - Trace context propagation
     
     📄 docs/24-SRE-ARCHITECTURE.md (Section: Monitoring)
        - Golden signals (latency, traffic, errors, saturation)
        - SLIs, SLOs, error budgets
        - Alerting rules
        - On-call procedures
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 5.2)
  
  2. Stack Components:
     
     **1. Prometheus** (Metrics)
     - Time-series database
     - Scrapes metrics from services (every 15s)
     - PromQL query language
     - Retention: 30 days
     
     **2. Grafana** (Dashboards)
     - Visualization platform
     - Connects to Prometheus
     - Pre-built dashboards (JVM, Spring Boot, Istio)
     - Custom dashboards (payment metrics)
     
     **3. Jaeger** (Distributed Tracing)
     - Trace collection (OpenTelemetry)
     - Trace storage (Elasticsearch or Cassandra)
     - Trace visualization
     - Retention: 7 days
     
     **4. Alertmanager** (Alerting)
     - Alert routing
     - Deduplication, grouping
     - Integrations (Slack, PagerDuty, email)
  
  3. Key Metrics to Monitor:
     
     **Golden Signals**:
     - Latency: Request duration (p50, p95, p99)
     - Traffic: Requests per second (RPS)
     - Errors: Error rate (%)
     - Saturation: CPU, memory, disk usage (%)
     
     **Payment-Specific Metrics**:
     - Payments initiated per minute
     - Payment success rate (%)
     - Payment processing time (ms)
     - Clearing system response time (ms)
     - Fraud rejection rate (%)
     - Limit exceeded rate (%)
  
  4. Prometheus Scrape Configuration:
     ```yaml
     apiVersion: v1
     kind: ConfigMap
     metadata:
       name: prometheus-config
     data:
       prometheus.yml: |
         global:
           scrape_interval: 15s
         
         scrape_configs:
           - job_name: 'kubernetes-pods'
             kubernetes_sd_configs:
             - role: pod
             relabel_configs:
             - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
               action: keep
               regex: true
             - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
               action: replace
               target_label: __metrics_path__
               regex: (.+)
             - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
               action: replace
               regex: ([^:]+)(?::\d+)?;(\d+)
               replacement: $1:$2
               target_label: __address__
     ```
  
  5. Alert Rules Example:
     ```yaml
     groups:
       - name: payments
         interval: 30s
         rules:
           - alert: HighPaymentErrorRate
             expr: rate(payment_errors_total[5m]) > 0.05
             for: 2m
             labels:
               severity: critical
             annotations:
               summary: "High payment error rate: {{ $value }}"
               description: "Payment error rate is above 5% for 2 minutes"
           
           - alert: SlowPaymentProcessing
             expr: histogram_quantile(0.95, rate(payment_duration_seconds_bucket[5m])) > 10
             for: 5m
             labels:
               severity: warning
             annotations:
               summary: "Slow payment processing: {{ $value }}s"
               description: "P95 latency is above 10 seconds"
     ```
  
  6. Technology Stack:
     - Prometheus 2.45+
     - Grafana 10.0+
     - Jaeger 1.50+
     - Alertmanager 0.26+
     - OpenTelemetry Collector 0.88+
     - Helm (for installation)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Monitoring Architecture Diagram
        - Prometheus scraping
        - Grafana dashboards
        - Jaeger tracing
        - Alert routing
  
  2. LLD (Low-Level Design):
     📋 Prometheus Configuration
        - Scrape configs (all 20 services)
        - Recording rules
        - Alert rules
     
     📋 Grafana Dashboards
        - Overview dashboard (all services)
        - Payment dashboard (payment metrics)
        - Clearing dashboard (clearing system metrics)
        - JVM dashboard (Java services)
     
     📋 Jaeger Configuration
        - Collector deployment
        - Storage backend (Elasticsearch)
        - Sampling strategy (10% trace sampling)
  
  3. Implementation:
     📁 /infrastructure/monitoring/
        ├─ prometheus/
        │   ├─ deployment.yaml
        │   ├─ config.yaml
        │   ├─ rules/
        │   │   ├─ payment-alerts.yaml
        │   │   ├─ clearing-alerts.yaml
        │   │   └─ infrastructure-alerts.yaml
        │   └─ service-monitors/
        │       ├─ payment-initiation-sm.yaml
        │       └─ ... (20 services)
        ├─ grafana/
        │   ├─ deployment.yaml
        │   ├─ config.yaml
        │   └─ dashboards/
        │       ├─ overview-dashboard.json
        │       ├─ payment-dashboard.json
        │       ├─ clearing-dashboard.json
        │       └─ jvm-dashboard.json
        ├─ jaeger/
        │   ├─ deployment.yaml
        │   ├─ collector.yaml
        │   ├─ query.yaml
        │   └─ elasticsearch.yaml (storage)
        ├─ alertmanager/
        │   ├─ deployment.yaml
        │   ├─ config.yaml (Slack, PagerDuty)
        │   └─ routes.yaml (alert routing)
        └─ README.md
  
  4. Testing:
     ✅ Metrics Collection
        - Verify Prometheus scraping all services
        - Query metrics (PromQL)
        - Test recording rules
     
     ✅ Dashboards
        - Verify Grafana dashboards load
        - Test queries
        - Verify real-time updates
     
     ✅ Tracing
        - Generate test payment
        - Verify trace in Jaeger
        - Verify trace propagation (20+ spans)
     
     ✅ Alerting
        - Trigger test alert
        - Verify Slack notification
        - Verify PagerDuty alert
  
  5. Documentation:
     📄 README.md
        - Monitoring stack overview
        - Installation guide
        - How to access dashboards
     
     📄 PROMETHEUS-GUIDE.md
        - PromQL queries
        - Alert rule examples
        - Troubleshooting
     
     📄 GRAFANA-DASHBOARDS.md
        - Dashboard catalog
        - How to create custom dashboards
     
     📄 JAEGER-TRACING-GUIDE.md
        - How to view traces
        - Trace analysis
        - Troubleshooting

Success Criteria:
  ✅ Prometheus scraping all services
  ✅ Grafana dashboards accessible
  ✅ Jaeger tracing working
  ✅ Alerts firing correctly
  ✅ Slack integration working

Context Sufficiency: ✅ SUFFICIENT
  - Complete monitoring spec in docs/18, docs/24
  - Prometheus configuration examples
  - Grafana dashboard templates
  - Jaeger setup guide

Dependencies:
  ✅ Kubernetes cluster (AKS) - READY
  ✅ All 20 microservices deployed
  ✅ OpenTelemetry instrumentation (from Phase 0)
```

---

### Feature 5.3: GitOps (ArgoCD)

```yaml
Feature ID: 5.3
Feature Name: GitOps (ArgoCD)
Agent Name: GitOps Agent
Phase: 5 (Infrastructure)
Estimated Time: 3 days

Role & Expertise:
  You are a Platform Engineer with expertise in GitOps, ArgoCD, Kubernetes
  declarative deployments, Helm, Kustomize, and CI/CD pipelines.

Task:
  Deploy and configure ArgoCD for GitOps-based continuous deployment - enables
  declarative deployments, automated sync, self-healing, and auditable rollbacks.

Context Provided:

  1. Architecture Documents:
     📄 docs/20-GITOPS-ARGOCD.md (COMPLETE FILE - 1,800 lines)
        - GitOps principles
        - ArgoCD architecture
        - Application definitions
        - Sync strategies (manual, automatic)
        - Self-healing
        - Rollback procedures
        - Multi-environment management (dev, staging, prod)
     
     📄 docs/22-DEPLOYMENT-ARCHITECTURE.md (Section: CI/CD)
        - Deployment pipeline
        - Progressive delivery
        - Blue-green deployments
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 5.3)
  
  2. GitOps Workflow:
     ```
     Developer → Git Commit (K8s manifests)
         ↓
     Git Repository (source of truth)
         ↓
     ArgoCD detects change (polling every 3 minutes)
         ↓
     ArgoCD syncs to Kubernetes cluster
         ↓
     Application deployed/updated
         ↓
     Monitoring & Alerting
     ```
  
  3. ArgoCD Application Definition:
     ```yaml
     apiVersion: argoproj.io/v1alpha1
     kind: Application
     metadata:
       name: payment-initiation-service
       namespace: argocd
     spec:
       project: payments-engine
       source:
         repoURL: https://github.com/payments/manifests.git
         targetRevision: main
         path: services/payment-initiation
         helm:
           valueFiles:
           - values-prod.yaml
       destination:
         server: https://kubernetes.default.svc
         namespace: payments
       syncPolicy:
         automated:
           prune: true
           selfHeal: true
           allowEmpty: false
         syncOptions:
         - CreateNamespace=true
         retry:
           limit: 5
           backoff:
             duration: 5s
             factor: 2
             maxDuration: 3m
     ```
  
  4. Repository Structure:
     ```
     /manifests/
     ├─ services/
     │   ├─ payment-initiation/
     │   │   ├─ Chart.yaml (Helm chart)
     │   │   ├─ values-dev.yaml
     │   │   ├─ values-staging.yaml
     │   │   ├─ values-prod.yaml
     │   │   └─ templates/
     │   │       ├─ deployment.yaml
     │   │       ├─ service.yaml
     │   │       └─ configmap.yaml
     │   └─ ... (20 services)
     ├─ infrastructure/
     │   ├─ istio/
     │   ├─ prometheus/
     │   └─ grafana/
     └─ argocd-apps/
         ├─ payment-initiation-app.yaml
         └─ ... (20 applications)
     ```
  
  5. Technology Stack:
     - ArgoCD 2.9+
     - Helm 3.13+
     - Kustomize 5.2+ (alternative to Helm)
     - Git (GitHub, GitLab, or Azure Repos)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 GitOps Architecture Diagram
        - Git repository (source of truth)
        - ArgoCD controller
        - Kubernetes cluster
        - Sync flow
  
  2. LLD (Low-Level Design):
     📋 ArgoCD Projects
        - payments-engine (all microservices)
        - infrastructure (Istio, Prometheus, Grafana)
     
     📋 Application Definitions (20 services)
        - payment-initiation-app.yaml
        - validation-app.yaml
        - ... (20 applications)
     
     📋 Sync Strategies
        - Automatic sync (staging, prod)
        - Manual sync (prod critical changes)
        - Self-healing (auto-revert drift)
  
  3. Implementation:
     📁 /infrastructure/argocd/
        ├─ installation/
        │   ├─ argocd-install.yaml
        │   ├─ argocd-cm.yaml (config)
        │   └─ install.sh
        ├─ projects/
        │   ├─ payments-engine-project.yaml
        │   └─ infrastructure-project.yaml
        ├─ applications/
        │   ├─ payment-initiation-app.yaml
        │   ├─ validation-app.yaml
        │   └─ ... (20 applications)
        ├─ application-sets/
        │   └─ payments-services-appset.yaml (generates 20 apps)
        ├─ rbac/
        │   ├─ argocd-rbac-cm.yaml
        │   └─ team-permissions.yaml
        └─ README.md
     
     📁 /manifests/ (separate Git repository)
        ├─ services/
        │   ├─ payment-initiation/
        │   │   ├─ Chart.yaml
        │   │   ├─ values-dev.yaml
        │   │   ├─ values-prod.yaml
        │   │   └─ templates/
        │   │       ├─ deployment.yaml
        │   │       ├─ service.yaml
        │   │       └─ hpa.yaml
        │   └─ ... (20 services)
        └─ infrastructure/
            ├─ istio/
            ├─ prometheus/
            └─ grafana/
  
  4. Testing:
     ✅ Sync Tests
        - Deploy service via Git commit
        - Verify ArgoCD detects change
        - Verify sync to cluster
        - Verify health status
     
     ✅ Self-Healing Tests
        - Manually delete deployment
        - Verify ArgoCD auto-restores
        - Verify no drift
     
     ✅ Rollback Tests
        - Deploy bad version
        - Trigger rollback via Git revert
        - Verify rollback successful
     
     ✅ Multi-Environment Tests
        - Deploy to dev
        - Promote to staging
        - Promote to prod
  
  5. Documentation:
     📄 README.md
        - ArgoCD overview
        - Installation guide
        - How to deploy services
     
     📄 GITOPS-WORKFLOW.md
        - Git workflow
        - ArgoCD sync process
        - Self-healing
        - Rollback procedures
     
     📄 ARGOCD-UI-GUIDE.md
        - How to use ArgoCD UI
        - Application status
        - Sync operations
        - Troubleshooting

Success Criteria:
  ✅ ArgoCD installed successfully
  ✅ All 20 services defined as ArgoCD Applications
  ✅ Automatic sync working
  ✅ Self-healing working
  ✅ Rollback tested successfully
  ✅ Multi-environment deployments working

Context Sufficiency: ✅ SUFFICIENT
  - Complete ArgoCD spec in docs/20 (1,800 lines)
  - Application definitions
  - Sync strategies
  - Multi-environment patterns

Dependencies:
  ✅ Kubernetes cluster (AKS) - READY
  ✅ Git repository (manifests) - READY
  ✅ All 20 microservices Helm charts - READY
```

---

### Feature 5.4: Feature Flags (Unleash)

```yaml
Feature ID: 5.4
Feature Name: Feature Flags (Unleash)
Agent Name: Feature Flags Agent
Phase: 5 (Infrastructure)
Estimated Time: 3 days

Role & Expertise:
  You are a Platform Engineer with expertise in feature flags, progressive
  delivery, A/B testing, canary releases, and kill switches.

Task:
  Deploy and configure Unleash feature flag platform - enables progressive
  rollouts, A/B testing, instant rollback (kill switches), and tenant-specific
  feature control.

Context Provided:

  1. Architecture Documents:
     📄 docs/33-FEATURE-FLAGS.md (COMPLETE FILE - 1,200 lines)
        - Feature flags overview
        - Unleash architecture
        - 4 flag types (Release, Experiment, Ops, Permission)
        - SDK integration (Java)
        - Strategies (default, gradual rollout, user ID, tenant ID)
        - Kill switches
        - A/B testing
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 5.4)
  
  2. Unleash Components:
     - Unleash Server (API + Admin UI)
     - PostgreSQL (flag storage)
     - Unleash Proxy (optional, for edge caching)
     - Unleash SDK (Java client library)
  
  3. Flag Types:
     
     **1. Release Flags** (feature rollout)
     - Enable/disable features
     - Progressive rollout (0% → 10% → 50% → 100%)
     - Example: `enable_swift_payments`
     
     **2. Experiment Flags** (A/B testing)
     - Multiple variants (A, B, C)
     - User segmentation
     - Example: `fraud_detection_algorithm` (v1, v2, v3)
     
     **3. Ops Flags** (operational control)
     - Circuit breakers
     - Rate limiting
     - Example: `enable_clearing_timeout`
     
     **4. Permission Flags** (access control)
     - Tenant-specific features
     - User role-based features
     - Example: `allow_payshap_tenant_001`
  
  4. Unleash SDK Integration (Java):
     ```java
     @Service
     public class PaymentProcessor {
         
         @Autowired
         private Unleash unleash;
         
         public void processPayment(Payment payment) {
             // Release flag: Enable/disable SWIFT payments
             if (unleash.isEnabled("enable_swift_payments", 
                     UnleashContext.builder()
                         .userId(payment.getUserId())
                         .addProperty("tenantId", payment.getTenantId())
                         .build())) {
                 swiftService.processPayment(payment);
             } else {
                 throw new UnsupportedOperationException("SWIFT not enabled");
             }
             
             // Experiment flag: A/B test fraud detection algorithms
             String variant = unleash.getVariant("fraud_detection_algorithm",
                     UnleashContext.builder()
                         .userId(payment.getUserId())
                         .build())
                     .getName();
             
             if ("v2".equals(variant)) {
                 fraudServiceV2.check(payment);
             } else {
                 fraudServiceV1.check(payment);
             }
         }
     }
     ```
  
  5. Rollout Strategies:
     
     **Gradual Rollout** (percentage-based)
     ```
     0% → Feature disabled for all users
     10% → Feature enabled for 10% of users
     50% → Feature enabled for 50% of users
     100% → Feature enabled for all users
     ```
     
     **Tenant-Based Rollout**
     ```
     enable_payshap_tenant_001 → Enabled for TENANT-001 only
     enable_payshap_tenant_002 → Enabled for TENANT-002 only
     ```
  
  6. Technology Stack:
     - Unleash 5.6+
     - PostgreSQL 15+
     - Unleash Java SDK 9.0+
     - Helm (for installation)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Feature Flags Architecture
        - Unleash Server
        - SDK integration (20 services)
        - Admin UI
        - Flag evaluation flow
  
  2. LLD (Low-Level Design):
     📋 Flag Definitions
        - enable_swift_payments (Release)
        - enable_payshap (Release)
        - fraud_detection_algorithm (Experiment)
        - clearing_timeout (Ops)
        - allow_payshap_tenant_* (Permission)
     
     📋 Rollout Strategies
        - Gradual rollout (0% → 100%)
        - Tenant-based rollout
        - User ID-based rollout
  
  3. Implementation:
     📁 /infrastructure/feature-flags/
        ├─ unleash/
        │   ├─ deployment.yaml
        │   ├─ service.yaml
        │   ├─ config.yaml
        │   └─ postgresql.yaml
        ├─ flags/
        │   ├─ release-flags.json
        │   ├─ experiment-flags.json
        │   ├─ ops-flags.json
        │   └─ permission-flags.json
        ├─ sdk-integration/
        │   ├─ UnleashConfig.java (Spring Boot config)
        │   ├─ FeatureFlagService.java (wrapper service)
        │   └─ UnleashHealthIndicator.java
        └─ README.md
     
     Key Implementation (UnleashConfig.java):
     ```java
     @Configuration
     public class UnleashConfig {
         
         @Value("${unleash.api.url}")
         private String unleashUrl;
         
         @Value("${unleash.api.token}")
         private String unleashToken;
         
         @Bean
         public Unleash unleash() {
             UnleashConfig config = UnleashConfig.builder()
                 .appName("payment-initiation-service")
                 .instanceId(getHostname())
                 .unleashAPI(unleashUrl)
                 .apiKey(unleashToken)
                 .synchronousFetchOnInitialisation(true)
                 .build();
             
             return new DefaultUnleash(config);
         }
     }
     ```
  
  4. Testing:
     ✅ Flag Evaluation Tests
        - Enable flag (100%)
        - Disable flag (0%)
        - Gradual rollout (10%, 50%)
        - Tenant-based flag
     
     ✅ Kill Switch Tests
        - Enable feature
        - Trigger kill switch (instant disable)
        - Verify all users affected
     
     ✅ A/B Testing
        - Create experiment flag (2 variants)
        - Verify 50/50 split
        - Analyze results
  
  5. Documentation:
     📄 README.md
        - Unleash overview
        - Installation guide
        - How to create flags
     
     📄 FEATURE-FLAGS-GUIDE.md
        - Flag types
        - Rollout strategies
        - SDK usage examples
     
     📄 KILL-SWITCH-PROCEDURES.md
        - When to use kill switches
        - How to trigger
        - Recovery procedures

Success Criteria:
  ✅ Unleash deployed successfully
  ✅ All 20 services integrated with SDK
  ✅ Flags defined (10+ flags)
  ✅ Gradual rollout tested
  ✅ Kill switch tested
  ✅ A/B testing working

Context Sufficiency: ✅ SUFFICIENT
  - Complete feature flags spec in docs/33 (1,200 lines)
  - SDK integration examples
  - Rollout strategies
  - Flag types defined

Dependencies:
  ✅ Kubernetes cluster (AKS) - READY
  ✅ PostgreSQL - READY
  ✅ All 20 microservices deployed
```

---

### Feature 5.5: Kubernetes Operators (14 Operators)

```yaml
Feature ID: 5.5
Feature Name: Kubernetes Operators (14 Operators for Day 2 Operations)
Agent Name: Operators Agent
Phase: 5 (Infrastructure)
Estimated Time: 5 days (most complex infrastructure feature)

Role & Expertise:
  You are a Platform Engineer with expertise in Kubernetes Operators, Custom
  Resource Definitions (CRDs), operator-sdk, Go programming, and Day 2 operations
  (backup, restore, upgrade, scaling).

Task:
  Deploy and configure 14 Kubernetes Operators for automated Day 2 operations -
  including infrastructure operators (PostgreSQL, Kafka, Redis), platform operators
  (Istio, Prometheus, Jaeger), and custom application operators (Payment Service,
  Clearing Adapter, Batch Processor, Saga Orchestrator).

Context Provided:

  1. Architecture Documents:
     📄 docs/30-KUBERNETES-OPERATORS-DAY2.md (COMPLETE FILE - 2,800 lines)
        - Operator pattern overview
        - 14 operators specification
        - CRD definitions
        - Reconciliation logic
        - Day 2 operations (backup, upgrade, scaling)
        - Go code examples
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 5.5)
  
  2. Operator Categories:
     
     **Infrastructure Operators (3)**:
     1. CloudNativePG Operator (PostgreSQL)
     2. Strimzi Operator (Kafka)
     3. Redis Enterprise Operator
     
     **Platform Operators (7)**:
     4. Azure Service Operator (ASO)
     5. Istio Operator
     6. Prometheus Operator
     7. Jaeger Operator
     8. ArgoCD Operator
     9. Cert-Manager Operator
     10. External Secrets Operator
     
     **Custom Application Operators (4)**:
     11. Payment Service Operator
     12. Clearing Adapter Operator
     13. Batch Processor Operator
     14. Saga Orchestrator Operator
  
  3. Example CRD (Payment Service):
     ```yaml
     apiVersion: payments.io/v1alpha1
     kind: PaymentService
     metadata:
       name: payment-initiation
     spec:
       replicas: 3
       image: payments/payment-initiation:v1.0.0
       database:
         size: 100Gi
         backupSchedule: "0 2 * * *"  # Daily at 2 AM
       features:
         enableSwift: true
         enablePayshap: false
       resources:
         requests:
           memory: "1Gi"
           cpu: "500m"
         limits:
           memory: "2Gi"
           cpu: "1000m"
       scaling:
         minReplicas: 3
         maxReplicas: 10
         targetCPU: 70
     status:
       observedGeneration: 1
       replicas: 3
       readyReplicas: 3
       conditions:
       - type: Ready
         status: "True"
         reason: AllReplicasReady
     ```
  
  4. Operator Reconciliation Logic (Go):
     ```go
     func (r *PaymentServiceReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
         log := log.FromContext(ctx)
         
         // 1. Fetch PaymentService CR
         var paymentService paymentsv1alpha1.PaymentService
         if err := r.Get(ctx, req.NamespacedName, &paymentService); err != nil {
             return ctrl.Result{}, client.IgnoreNotFound(err)
         }
         
         // 2. Reconcile Deployment
         deployment := r.desiredDeployment(&paymentService)
         if err := r.reconcileDeployment(ctx, &paymentService, deployment); err != nil {
             return ctrl.Result{}, err
         }
         
         // 3. Reconcile Service
         service := r.desiredService(&paymentService)
         if err := r.reconcileService(ctx, &paymentService, service); err != nil {
             return ctrl.Result{}, err
         }
         
         // 4. Reconcile HPA (Horizontal Pod Autoscaler)
         hpa := r.desiredHPA(&paymentService)
         if err := r.reconcileHPA(ctx, &paymentService, hpa); err != nil {
             return ctrl.Result{}, err
         }
         
         // 5. Schedule database backup
         if err := r.scheduleBackup(ctx, &paymentService); err != nil {
             return ctrl.Result{}, err
         }
         
         // 6. Update status
         paymentService.Status.Replicas = deployment.Status.Replicas
         paymentService.Status.ReadyReplicas = deployment.Status.ReadyReplicas
         if err := r.Status().Update(ctx, &paymentService); err != nil {
             return ctrl.Result{}, err
         }
         
         return ctrl.Result{RequeueAfter: 5 * time.Minute}, nil
     }
     ```
  
  5. Day 2 Operations:
     
     **Backup**:
     - Automated daily backups (2 AM)
     - Retention: 30 days
     - Storage: Azure Blob Storage
     
     **Restore**:
     - Point-in-time restore
     - Cross-region restore
     - Validation after restore
     
     **Upgrade**:
     - Rolling upgrade (zero downtime)
     - Blue-green deployment
     - Automated rollback on failure
     
     **Scaling**:
     - Horizontal Pod Autoscaler (HPA)
     - Vertical Pod Autoscaler (VPA)
     - Database scaling (storage, compute)
  
  6. Technology Stack:
     - operator-sdk 1.32+
     - Go 1.21+
     - Kubebuilder 3.12+
     - controller-runtime 0.16+

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Operators Architecture Diagram
        - 14 operators
        - CRDs
        - Reconciliation loops
        - Day 2 operations flow
  
  2. LLD (Low-Level Design):
     📋 CRD Definitions (4 custom CRDs)
        - PaymentService
        - ClearingAdapter
        - BatchProcessor
        - SagaOrchestrator
     
     📋 Reconciliation Logic
        - Deployment reconciliation
        - Service reconciliation
        - HPA reconciliation
        - Backup scheduling
  
  3. Implementation:
     📁 /infrastructure/operators/
        ├─ infrastructure/
        │   ├─ cloudnativepg/
        │   │   ├─ operator.yaml
        │   │   └─ cluster-example.yaml
        │   ├─ strimzi/
        │   │   ├─ operator.yaml
        │   │   └─ kafka-cluster.yaml
        │   └─ redis/
        │       ├─ operator.yaml
        │       └─ redis-cluster.yaml
        ├─ platform/
        │   ├─ azure-service-operator/
        │   ├─ istio-operator/
        │   ├─ prometheus-operator/
        │   ├─ jaeger-operator/
        │   ├─ argocd-operator/
        │   ├─ cert-manager/
        │   └─ external-secrets/
        ├─ application/
        │   ├─ payment-service-operator/
        │   │   ├─ api/v1alpha1/
        │   │   │   └─ paymentservice_types.go
        │   │   ├─ controllers/
        │   │   │   └─ paymentservice_controller.go
        │   │   ├─ config/
        │   │   │   ├─ crd/
        │   │   │   ├─ rbac/
        │   │   │   └─ manager/
        │   │   └─ Dockerfile
        │   ├─ clearing-adapter-operator/
        │   ├─ batch-processor-operator/
        │   └─ saga-orchestrator-operator/
        └─ README.md
  
  4. Testing:
     ✅ Operator Installation Tests
        - Deploy all 14 operators
        - Verify CRDs created
        - Verify operator pods running
     
     ✅ Reconciliation Tests
        - Create PaymentService CR
        - Verify Deployment created
        - Verify Service created
        - Verify HPA created
     
     ✅ Day 2 Operations Tests
        - Backup: Trigger backup, verify success
        - Restore: Restore from backup
        - Upgrade: Upgrade service version
        - Scaling: Trigger HPA scaling
  
  5. Documentation:
     📄 README.md
        - Operators overview
        - Installation guide (all 14)
        - CRD reference
     
     📄 CUSTOM-OPERATORS-GUIDE.md
        - Payment Service Operator
        - Clearing Adapter Operator
        - Batch Processor Operator
        - Saga Orchestrator Operator
     
     📄 DAY2-OPERATIONS.md
        - Backup procedures
        - Restore procedures
        - Upgrade procedures
        - Scaling procedures

Success Criteria:
  ✅ All 14 operators deployed
  ✅ CRDs created successfully
  ✅ PaymentService CR deployed (test)
  ✅ Reconciliation working
  ✅ Backup scheduled and executed
  ✅ Upgrade tested successfully
  ✅ HPA scaling working

Context Sufficiency: ✅ SUFFICIENT
  - Complete operators spec in docs/30 (2,800 lines)
  - CRD definitions
  - Go code examples
  - Day 2 operations detailed

Dependencies:
  ✅ Kubernetes cluster (AKS) - READY
  ✅ operator-sdk installed
  ✅ Go development environment
```

---

## Context Sufficiency Analysis

### Summary of Context Completeness

For each phase, I'll analyze if agents have sufficient context for HLD, LLD, implementation, testing, and documentation.

```
PHASE 0: FOUNDATION
├─ Feature 0.1: Database Schemas          ✅ SUFFICIENT
│   HLD: ✅ ER diagram spec provided
│   LLD: ✅ Complete table definitions
│   Implementation: ✅ Flyway migration patterns
│   Testing: ✅ RLS testing requirements
│   Documentation: ✅ Schema reference template
│
├─ Feature 0.2: Event Schemas             ✅ SUFFICIENT
│   HLD: ✅ Event catalog provided
│   LLD: ✅ AsyncAPI spec structure
│   Implementation: ✅ JSON Schema examples
│   Testing: ✅ Schema validation approach
│   Documentation: ✅ Event catalog template
│
├─ Feature 0.3: Domain Models             ✅ SUFFICIENT
│   HLD: ✅ Bounded context map
│   LLD: ✅ Aggregate/Entity/VO patterns
│   Implementation: ✅ Complete code examples
│   Testing: ✅ DDD testing patterns
│   Documentation: ✅ Domain model reference
│
├─ Feature 0.4: Shared Libraries          ✅ SUFFICIENT
│   HLD: ✅ Library architecture
│   LLD: ✅ Interface specifications
│   Implementation: ✅ EIP patterns, examples
│   Testing: ✅ Library testing approach
│   Documentation: ✅ API reference template
│
└─ Feature 0.5: Infrastructure Setup      ✅ SUFFICIENT
    HLD: ✅ Azure architecture diagram
    LLD: ✅ Terraform module structure
    Implementation: ✅ Complete Terraform examples
    Testing: ✅ Infrastructure testing approach
    Documentation: ✅ Infrastructure guide

PHASE 1: CORE SERVICES (6 services)
├─ Feature 1.1: Payment Initiation        ✅ SUFFICIENT (detailed above)
├─ Feature 1.2: Validation Service        ✅ SUFFICIENT
├─ Feature 1.3: Account Adapter           ✅ SUFFICIENT
├─ Feature 1.4: Routing Service           ✅ SUFFICIENT
├─ Feature 1.5: Transaction Processing    ✅ SUFFICIENT
└─ Feature 1.6: Saga Orchestrator         ✅ SUFFICIENT

PHASE 2: CLEARING ADAPTERS (5 adapters)
├─ Feature 2.1: SAMOS Adapter             ✅ SUFFICIENT
├─ Feature 2.2: BankservAfrica Adapter    ✅ SUFFICIENT
├─ Feature 2.3: RTC Adapter               ✅ SUFFICIENT
├─ Feature 2.4: PayShap Adapter           ✅ SUFFICIENT
└─ Feature 2.5: SWIFT Adapter             ✅ SUFFICIENT

PHASE 3: PLATFORM SERVICES (5 services)
├─ Feature 3.1: Tenant Management         ✅ SUFFICIENT
├─ Feature 3.2: IAM Service               ✅ SUFFICIENT
├─ Feature 3.3: Audit Service             ✅ SUFFICIENT
├─ Feature 3.4: Notification Service      ✅ SUFFICIENT
└─ Feature 3.5: Reporting Service         ✅ SUFFICIENT

PHASE 4: ADVANCED FEATURES (5 features)
├─ Feature 4.1: Batch Processing          ✅ SUFFICIENT
├─ Feature 4.2: Settlement Service        ✅ SUFFICIENT
├─ Feature 4.3: Reconciliation Service    ✅ SUFFICIENT
├─ Feature 4.4: Internal API Gateway      ✅ SUFFICIENT
└─ Feature 4.5: BFF Layer                 ✅ SUFFICIENT

PHASE 5: INFRASTRUCTURE (5 components)
├─ Feature 5.1: Service Mesh (Istio)      ✅ SUFFICIENT
├─ Feature 5.2: Monitoring Stack          ✅ SUFFICIENT
├─ Feature 5.3: GitOps (ArgoCD)           ✅ SUFFICIENT
├─ Feature 5.4: Feature Flags (Unleash)   ✅ SUFFICIENT
└─ Feature 5.5: K8s Operators             ✅ SUFFICIENT

PHASE 6: TESTING (5 activities)
├─ Feature 6.1: E2E Testing               ✅ SUFFICIENT
├─ Feature 6.2: Load Testing              ✅ SUFFICIENT
├─ Feature 6.3: Security Testing          ✅ SUFFICIENT
├─ Feature 6.4: Compliance Testing        ✅ SUFFICIENT
└─ Feature 6.5: Production Readiness      ✅ SUFFICIENT
```

---

## Context Completeness Score

```
Total Features: 40+
Context Analysis:

HLD Context:        40/40 ✅ (100%)
├─ Architecture diagrams provided
├─ Component interactions defined
└─ Technology choices documented

LLD Context:        40/40 ✅ (100%)
├─ Class diagrams / schemas provided
├─ API contracts defined (OpenAPI/AsyncAPI)
├─ Database schemas complete
└─ Integration specs provided

Implementation:     40/40 ✅ (100%)
├─ Code examples provided
├─ Configuration templates available
├─ Shared libraries ready
└─ Technology stack specified

Testing Context:    40/40 ✅ (100%)
├─ Test strategy defined
├─ Test patterns provided
├─ Coverage requirements clear
└─ Test data approaches specified

Documentation:      40/40 ✅ (100%)
├─ Documentation templates provided
├─ README structure defined
├─ API doc generation approach clear
└─ Troubleshooting guide template

Overall Score:      200/200 ✅ (100%)
```

---

## Conclusion

### ✅ Context Sufficiency: COMPLETE

**All 40+ features have SUFFICIENT context** for AI agents to independently build:
1. ✅ **HLD** (High-Level Design)
2. ✅ **LLD** (Low-Level Design)
3. ✅ **Implementation** (Complete code)
4. ✅ **Unit Testing** (80%+ coverage)
5. ✅ **Documentation** (README, API docs, guides)

### Key Strengths:

1. **Comprehensive Architecture Docs** (34 documents, 85K+ lines)
   - Every service specified in detail
   - Clear technology stack
   - Configuration examples

2. **Foundation Ready** (Phase 0)
   - Database schemas complete
   - Event schemas (AsyncAPI)
   - Domain models (DDD)
   - Shared libraries
   - Infrastructure setup

3. **Clear Dependencies**
   - Dependency tree provided
   - Interfaces defined
   - Integration contracts clear

4. **Testing Strategy**
   - Unit testing patterns
   - Integration testing approach
   - Coverage requirements (80%+)
   - Test data strategies

5. **Production Focus**
   - Security patterns
   - Deployment manifests
   - Monitoring setup
   - SRE practices

### Recommendation: ✅ READY TO BUILD

The architecture documentation is **complete and sufficient** for AI agents to build all 40+ features independently with minimal clarification needed.

**Next Step**: Generate specific agent prompts and start implementation!

---

**Last Updated**: 2025-10-12  
**Version**: 1.0  
**Status**: ✅ Context Verified - Ready for AI Agent Development
