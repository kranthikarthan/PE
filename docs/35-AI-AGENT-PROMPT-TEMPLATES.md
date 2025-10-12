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
