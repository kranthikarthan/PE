# AI Agent Prompt Templates - Complete Context per Feature

## Overview

This document provides **detailed prompt templates** for each of the **52 features** (40 original + 12 Phase 7), ensuring each AI agent has sufficient context to complete:

1. **HLD (High-Level Design)** - Architecture overview
2. **LLD (Low-Level Design)** - Detailed design, class diagrams
3. **Implementation** - Complete working code
4. **Unit Testing** - 80%+ test coverage
5. **Documentation** - README, API docs, setup guide

**Purpose**: Enable any AI coding agent to independently build a feature with minimal clarification needed.

---

## ⚠️ CRITICAL: Generic Coding Guardrails

**ALL AI AGENTS MUST FOLLOW THESE GUARDRAILS FOR EVERY FEATURE**

These guardrails ensure code quality, security, performance, and maintainability across the entire Payments Engine.

### 🔒 Security Guardrails

1. **NO Hardcoded Secrets**
   - ❌ NEVER hardcode passwords, API keys, tokens, or credentials in code
   - ✅ ALWAYS use Azure Key Vault, environment variables, or configuration files
   - ✅ Example: `@Value("${azure.storage.connection-string}")` ✅
   - ❌ Example: `String password = "myPassword123"` ❌

2. **SQL Injection Prevention**
   - ❌ NEVER use string concatenation for SQL queries
   - ✅ ALWAYS use parameterized queries or JPA/JPQL
   - ✅ Example: `@Query("SELECT p FROM Payment p WHERE p.id = :id")` ✅
   - ❌ Example: `"SELECT * FROM payments WHERE id = '" + id + "'"` ❌

3. **Input Validation**
   - ✅ ALWAYS validate all user input using Bean Validation (`@Valid`, `@NotNull`, `@Pattern`)
   - ✅ Sanitize input to prevent XSS, XXE, and injection attacks
   - ✅ Use whitelisting approach (allow known-good) over blacklisting

4. **Authentication & Authorization**
   - ✅ ALWAYS check authentication (JWT token) on all endpoints except health checks
   - ✅ ALWAYS verify authorization (RBAC) before performing operations
   - ✅ ALWAYS propagate tenant context (`X-Tenant-ID`) for multi-tenancy

5. **Sensitive Data Handling**
   - ✅ ALWAYS encrypt PII (email, ID numbers) at rest using AES-256
   - ✅ ALWAYS mask account numbers in logs (e.g., `ACC-****7890`)
   - ❌ NEVER log passwords, tokens, or full card numbers
   - ✅ Use HTTPS/TLS 1.2+ for all communications

### 🏗️ Code Quality Guardrails

6. **SOLID Principles**
   - ✅ Single Responsibility: One class = one responsibility
   - ✅ Open/Closed: Open for extension, closed for modification
   - ✅ Liskov Substitution: Subtypes must be substitutable
   - ✅ Interface Segregation: Small, focused interfaces
   - ✅ Dependency Inversion: Depend on abstractions, not concretions

7. **Clean Code**
   - ✅ Use descriptive variable/method names (`calculateTotalAmount` not `calc`)
   - ✅ Keep methods short (< 20 lines), classes focused (< 300 lines)
   - ✅ Avoid deep nesting (max 3 levels)
   - ✅ Use early returns to reduce nesting
   - ✅ Add JavaDoc for all public methods and classes

8. **Error Handling**
   - ✅ ALWAYS use specific exceptions (`PaymentNotFoundException` not `RuntimeException`)
   - ✅ ALWAYS log errors with context (payment ID, tenant ID, correlation ID)
   - ✅ Use `@ControllerAdvice` for global exception handling
   - ❌ NEVER swallow exceptions silently
   - ✅ Include correlation ID in all error responses for traceability

9. **Logging**
   - ✅ Use SLF4J + Logback for all logging
   - ✅ Log levels: ERROR (failures), WARN (recoverable), INFO (key events), DEBUG (details)
   - ✅ ALWAYS include correlation ID, tenant ID in log context (MDC)
   - ❌ NEVER log PII, passwords, tokens, or full account numbers
   - ✅ Example: `log.info("Payment initiated: paymentId={}, tenantId={}", paymentId, tenantId)` ✅

### ⚡ Performance Guardrails

10. **Database Best Practices**
    - ✅ ALWAYS use pagination for list queries (`Pageable`, max 100 records)
    - ✅ ALWAYS add database indexes on foreign keys and query columns
    - ❌ NEVER use `SELECT *`, always specify columns
    - ✅ Use `@Transactional(readOnly = true)` for read operations
    - ✅ Avoid N+1 queries (use `@EntityGraph` or JOIN FETCH)

11. **Caching**
    - ✅ Cache frequently accessed, rarely changed data (Redis, 60s-300s TTL)
    - ✅ Use `@Cacheable` for read operations, `@CacheEvict` for writes
    - ✅ Include tenant ID in cache key for multi-tenancy
    - ❌ NEVER cache sensitive data (passwords, tokens) without encryption

12. **API Design**
    - ✅ Use RESTful conventions (GET, POST, PUT, DELETE)
    - ✅ Return appropriate HTTP status codes (200, 201, 400, 404, 500)
    - ✅ Version APIs (`/api/v1/payments`)
    - ✅ Use pagination, filtering, sorting for list endpoints
    - ✅ Implement rate limiting (100 requests/minute per user)

### 🧪 Testing Guardrails

13. **Test Coverage**
    - ✅ MINIMUM 80% code coverage (unit + integration tests)
    - ✅ Test happy path, edge cases, and failure scenarios
    - ✅ Use meaningful test names: `shouldRejectPaymentWhenAmountExceedsLimit`
    - ✅ Use `@DataJpaTest` for repository tests, `@WebMvcTest` for controller tests

14. **Test Best Practices**
    - ✅ Use test data builders/factories for readability
    - ✅ Avoid test interdependencies (each test is isolated)
    - ✅ Mock external dependencies (use `@MockBean`, WireMock)
    - ✅ Test with multiple tenants to validate RLS (Row-Level Security)

### 📚 Documentation Guardrails

15. **Code Documentation**
    - ✅ Add JavaDoc for all public classes, methods, and interfaces
    - ✅ Document assumptions, limitations, and edge cases
    - ✅ Include examples in JavaDoc for complex methods
    - ✅ Use `@param`, `@return`, `@throws` tags

16. **API Documentation**
    - ✅ Use OpenAPI 3.0 (`@OpenAPIDefinition`, `@Operation`)
    - ✅ Document all request/response schemas
    - ✅ Include error response examples (400, 404, 500)
    - ✅ Generate Swagger UI for interactive testing

17. **README.md**
    - ✅ Include: Overview, Tech Stack, Prerequisites, Setup, Testing, Troubleshooting
    - ✅ Add clear build/run instructions (`mvn spring-boot:run`)
    - ✅ Document environment variables and configuration

### 🔧 Configuration Guardrails

18. **Configuration Management**
    - ✅ Use `application.yml` for configuration (NOT properties files)
    - ✅ Separate configs per environment (`application-dev.yml`, `application-prod.yml`)
    - ✅ Use Spring Boot profiles (`@Profile("prod")`)
    - ✅ Externalize all configurable values (URLs, timeouts, limits)

19. **Dependency Management**
    - ✅ Use Spring Boot BOM for version management
    - ✅ Keep dependencies up-to-date (check for CVEs)
    - ✅ Minimize transitive dependencies
    - ✅ Use `<dependencyManagement>` in parent POM

### 🎯 Multi-Tenancy Guardrails

20. **Tenant Isolation**
    - ✅ ALWAYS validate `X-Tenant-ID` header on all requests
    - ✅ ALWAYS propagate tenant context using `TenantContextHolder`
    - ✅ Use Row-Level Security (RLS) for database queries
    - ✅ Include `tenant_id` in all database queries
    - ❌ NEVER allow cross-tenant data access

### 🚨 Failure Scenarios

21. **Resilience Patterns**
    - ✅ Use circuit breakers for external calls (`@CircuitBreaker`)
    - ✅ Implement retry logic with exponential backoff (`@Retry`)
    - ✅ Set timeouts for all external calls (5-10 seconds)
    - ✅ Provide fallback methods for degraded functionality
    - ✅ Use bulkhead pattern to limit concurrent requests

### 📊 Observability Guardrails

22. **Monitoring & Tracing**
    - ✅ Expose `/actuator/health`, `/actuator/metrics` endpoints
    - ✅ Add custom metrics using Micrometer (`Counter`, `Gauge`, `Timer`)
    - ✅ Use OpenTelemetry for distributed tracing
    - ✅ Propagate correlation ID across all service calls

23. **Health Checks**
    - ✅ Implement liveness probe (service is running)
    - ✅ Implement readiness probe (service is ready to accept traffic)
    - ✅ Check dependencies (database, message bus) in readiness probe

### ⚠️ VIOLATIONS = REJECTED CODE

**If ANY guardrail is violated, the code MUST be fixed before proceeding.**

**Review this section before starting implementation of ANY feature.**

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

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  [Feature-specific security, performance, and quality requirements]
  
  These are IN ADDITION to the Generic Coding Guardrails above.
  ALL guardrails (generic + specific) MUST be followed.

📊 DATA STRUCTURES & ALGORITHMS GUIDANCE:
  See: docs/37-DSA-GUIDANCE-ALL-FEATURES.md for recommended data structures,
  algorithms, time/space complexity considerations for this feature.

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

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **Idempotency** (CRITICAL):
     - ✅ MUST support idempotency using `X-Idempotency-Key` header
     - ✅ Store idempotency keys in Redis (24-hour TTL)
     - ✅ If duplicate key → return cached response (409 Conflict or 200 OK)
     - ✅ Generate payment ID only ONCE per idempotency key
  
  2. **Input Validation** (CRITICAL):
     - ✅ MUST validate all fields using `@Valid` annotation
     - ✅ Amount: MUST be > 0, max 2 decimal places
     - ✅ Currency: MUST be in [ZAR, USD, EUR, GBP]
     - ✅ Account IDs: MUST match pattern `^[A-Z]+-[0-9]+$`
     - ✅ Reference: MUST be 1-50 characters, alphanumeric only
     - ❌ NEVER accept null or empty required fields
  
  3. **Security**:
     - ✅ MUST validate JWT token on ALL endpoints except `/health`
     - ✅ MUST extract and validate `X-Tenant-ID` header
     - ✅ MUST propagate tenant context using `TenantContextHolder`
     - ✅ MUST include `tenant_id` in database INSERT
     - ❌ NEVER allow payment creation without tenant context
  
  4. **Event Publishing**:
     - ✅ MUST publish `PaymentInitiatedEvent` to Azure Service Bus
     - ✅ Include correlation ID in event for tracing
     - ✅ Use `@Transactional` to ensure atomicity (DB + Event)
     - ✅ If event publish fails → rollback database transaction
     - ✅ Set message TTL (time-to-live) to 24 hours
  
  5. **Payment ID Generation**:
     - ✅ Format: `PAY-{YYYY}-{NNNNNN}` (e.g., `PAY-2025-000123`)
     - ✅ MUST be unique across all tenants
     - ✅ Use database sequence for `NNNNNN` (6 digits, zero-padded)
     - ❌ NEVER use UUID or random strings (not user-friendly)
  
  6. **Error Handling**:
     - ✅ Return 400 Bad Request for validation errors (with field details)
     - ✅ Return 409 Conflict for duplicate idempotency key
     - ✅ Return 500 Internal Server Error for unexpected failures
     - ✅ Include correlation ID in all error responses
     - ✅ Log all errors with payment details (masked account numbers)
  
  7. **Performance**:
     - ✅ Target: < 500ms response time (95th percentile)
     - ✅ Use connection pooling (HikariCP, max 20 connections)
     - ✅ Async event publishing (non-blocking)
     - ✅ Add database index on `payment_id`, `tenant_id`, `created_at`
  
  8. **Logging**:
     - ✅ Log: `Payment initiated: paymentId={}, tenantId={}, amount={}, correlationId={}`
     - ❌ NEVER log full account numbers (mask: `ACC-****7890`)
     - ✅ Use MDC for correlation ID, tenant ID
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

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

### Feature 1.2: Validation Service

```yaml
Feature ID: 1.2
Feature Name: Validation Service
Agent Name: Validation Agent
Phase: 1 (Core Services)
Estimated Time: 4 days

Role & Expertise:
  You are a Backend Engineer with expertise in Java Spring Boot, business rules
  validation, Drools rules engine, and microservices patterns.

Task:
  Build the Validation Service - validates all payment requests against business
  rules, regulatory requirements, and Drools-based dynamic rules. Publishes
  PaymentValidatedEvent or PaymentRejectedEvent.

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **Drools Rules** (CRITICAL):
     - ✅ MUST define at least 10 validation rules (amount, currency, account, sanctions)
     - ✅ Use rule salience (priority) to control execution order
     - ✅ MUST store rules in Git repository (version control)
     - ✅ MUST support hot reload without service restart
     - ❌ NEVER hardcode business logic in Java (use Drools DRL files)
  
  2. **Rule Validation**:
     - ✅ MUST test each rule individually (unit test per rule)
     - ✅ MUST validate rule syntax before deployment (KIE validation)
     - ✅ If rule syntax error → log error, use fallback rules
     - ❌ NEVER deploy broken rules to production
  
  3. **Performance**:
     - ✅ Cache compiled rules in Redis (5-minute TTL)
     - ✅ Use KIE container reuse (don't create per request)
     - ✅ Target: < 200ms rule execution time
     - ✅ Limit rule complexity (max 10 conditions per rule)
  
  4. **Error Handling**:
     - ✅ If validation fails → collect ALL errors (not just first)
     - ✅ Return detailed error messages (which rule failed, why)
     - ✅ Publish `PaymentRejectedEvent` with rejection reasons
     - ✅ Update payment status to `REJECTED` in database
  
  5. **Event Consumption**:
     - ✅ MUST consume `PaymentInitiatedEvent` from Azure Service Bus
     - ✅ Use `@ServiceBusQueueTrigger` annotation
     - ✅ If validation fails → do NOT retry (publish rejection event)
     - ✅ If rule execution fails → retry 3 times, then DLQ (Dead Letter Queue)
  
  6. **Logging**:
     - ✅ Log: `Payment validated: paymentId={}, result={}, rulesExecuted={}`
     - ✅ Log: `Payment rejected: paymentId={}, errors={}`
     - ✅ Log rule execution time for performance monitoring
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #2)
        - Validation responsibilities
        - API endpoints (REST + event consumer)
        - Technology stack
     
     📄 docs/31-DROOLS-RULES-ENGINE.md (COMPLETE FILE - 2,000 lines)
        - Drools integration
        - Rule definitions (KIE base, sessions)
        - Hot reload rules from Git
        - Rule types (Payment Amount, Currency, Account Status, Sanctions)
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 1.2)
  
  2. Validation Rules (Drools):
     
     **Rule 1: Minimum Payment Amount**
     ```drl
     package com.payments.validation.rules;
     
     import com.payments.domain.payment.Payment;
     import com.payments.domain.payment.ValidationResult;
     
     rule "Minimum Payment Amount"
       when
         $payment: Payment(amount.value < 1.00)
       then
         ValidationResult result = ValidationResult.reject(
           "AMOUNT_TOO_LOW", 
           "Payment amount must be at least R1.00"
         );
         $payment.addValidationError(result);
     end
     ```
     
     **Rule 2: Maximum Payment Amount by Type**
     ```drl
     rule "EFT Maximum Amount"
       when
         $payment: Payment(
           paymentType == PaymentType.EFT,
           amount.value > 1000000.00
         )
       then
         ValidationResult result = ValidationResult.reject(
           "AMOUNT_EXCEEDS_LIMIT",
           "EFT payments cannot exceed R1,000,000"
         );
         $payment.addValidationError(result);
     end
     
     rule "RTC Maximum Amount"
       when
         $payment: Payment(
           paymentType == PaymentType.RTC,
           amount.value > 5000.00
         )
       then
         ValidationResult result = ValidationResult.reject(
           "RTC_LIMIT_EXCEEDED",
           "RTC payments cannot exceed R5,000"
         );
         $payment.addValidationError(result);
     end
     
     rule "PayShap Maximum Amount"
       when
         $payment: Payment(
           paymentType == PaymentType.PAYSHAP,
           amount.value > 3000.00
         )
       then
         ValidationResult result = ValidationResult.reject(
           "PAYSHAP_LIMIT_EXCEEDED",
           "PayShap payments cannot exceed R3,000"
         );
         $payment.addValidationError(result);
     end
     ```
     
     **Rule 3: Currency Validation**
     ```drl
     rule "Valid Currency"
       when
         $payment: Payment(currency not in ("ZAR", "USD", "EUR", "GBP"))
       then
         ValidationResult result = ValidationResult.reject(
           "INVALID_CURRENCY",
           "Currency must be ZAR, USD, EUR, or GBP"
         );
         $payment.addValidationError(result);
     end
     ```
  
  3. Validation Flow:
     ```
     PaymentInitiatedEvent received
         ↓
     Load Payment from database
         ↓
     Execute Drools rules (KIE session)
         ↓
     Check ValidationResult
         ↓
     If PASSED → Publish PaymentValidatedEvent
     If REJECTED → Publish PaymentRejectedEvent
     ```
  
  4. Event Consumption & Publishing:
     ```java
     @Service
     public class ValidationEventHandler {
         
         @Autowired
         private ValidationService validationService;
         
         @Autowired
         private EventPublisher eventPublisher;
         
         @ServiceBusQueueTrigger(
             name = "paymentInitiated",
             queueName = "payment-initiated",
             connection = "ServiceBusConnectionString"
         )
         public void handlePaymentInitiated(
             @ServiceBusMessage PaymentInitiatedEvent event,
             final ExecutionContext context
         ) {
             log.info("Validating payment: {}", event.getPaymentId());
             
             ValidationResult result = validationService.validate(
                 event.getPaymentId()
             );
             
             if (result.isValid()) {
                 // Publish PaymentValidatedEvent
                 PaymentValidatedEvent validatedEvent = PaymentValidatedEvent.builder()
                     .paymentId(event.getPaymentId())
                     .validatedAt(Instant.now())
                     .build();
                 
                 eventPublisher.publish("payment-validated", validatedEvent);
                 log.info("Payment validated: {}", event.getPaymentId());
             } else {
                 // Publish PaymentRejectedEvent
                 PaymentRejectedEvent rejectedEvent = PaymentRejectedEvent.builder()
                     .paymentId(event.getPaymentId())
                     .rejectionReason(result.getErrors())
                     .rejectedAt(Instant.now())
                     .build();
                 
                 eventPublisher.publish("payment-rejected", rejectedEvent);
                 log.error("Payment rejected: {}", result.getErrors());
             }
         }
     }
     ```
  
  5. Technology Stack:
     - Java 17
     - Spring Boot 3.2
     - Drools 8.44+ (rules engine)
     - Azure Service Bus (event consumer/publisher)
     - PostgreSQL (rules storage)
     - Redis (rules caching)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Validation Architecture
        - Event consumption (PaymentInitiatedEvent)
        - Drools rules execution
        - Event publishing (Validated/Rejected)
  
  2. LLD (Low-Level Design):
     📋 Validation Rules (Drools DRL)
        - 10+ validation rules (amount, currency, accounts, sanctions)
     
     📋 ValidationService API
        - validatePayment(paymentId) → ValidationResult
        - reloadRules() → void (hot reload from Git)
  
  3. Implementation:
     📁 /validation-service/
        ├─ src/main/java/
        │   ├─ controller/
        │   │   └─ ValidationController.java (REST API)
        │   ├─ service/
        │   │   ├─ ValidationService.java
        │   │   ├─ DroolsService.java (KIE container)
        │   │   └─ RuleReloadService.java (Git integration)
        │   ├─ event/
        │   │   ├─ ValidationEventHandler.java
        │   │   ├─ PaymentInitiatedConsumer.java
        │   │   └─ ValidationEventPublisher.java
        │   ├─ config/
        │   │   ├─ DroolsConfig.java (KIE base)
        │   │   └─ ServiceBusConfig.java
        │   └─ domain/
        │       └─ ValidationResult.java
        ├─ src/main/resources/
        │   ├─ rules/
        │   │   ├─ payment-amount-rules.drl
        │   │   ├─ currency-rules.drl
        │   │   ├─ account-rules.drl
        │   │   └─ sanctions-rules.drl
        │   └─ application.yml
        └─ README.md
  
  4. Testing:
     ✅ Unit Tests (Drools rules)
        - Test each rule with valid/invalid inputs
        - Test rule priority/salience
     
     ✅ Integration Tests
        - Consume PaymentInitiatedEvent → assert PaymentValidatedEvent
        - Test rule hot reload (update DRL file)
     
     Target: 80% code coverage
  
  5. Documentation:
     📄 README.md
        - Validation service overview
        - Drools rules catalog
        - How to add new rules
     
     📄 API.md
        - REST API endpoints
        - Event subscriptions

Success Criteria:
  ✅ All validation rules defined (10+ rules)
  ✅ Drools integration working
  ✅ Hot reload tested (update rule → reload → validate)
  ✅ Event consumption/publishing working
  ✅ 80% code coverage

Context Sufficiency: ✅ SUFFICIENT
  - Complete Drools spec in docs/31 (2,000 lines)
  - Validation rules examples provided
  - Event schemas defined (Phase 0)

Dependencies:
  ✅ Phase 0 (Domain Models, Event Schemas) - READY
  ✅ Feature 1.1 (Payment Initiation Service) - READY
```

---

### Feature 1.3: Account Adapter Service

```yaml
Feature ID: 1.3
Feature Name: Account Adapter Service
Agent Name: Account Adapter Agent
Phase: 1 (Core Services)
Estimated Time: 4 days

Role & Expertise:
  You are a Backend Engineer with expertise in Java Spring Boot, REST client
  integration, Resilience4j (circuit breaker), and external system integration.

Task:
  Build the Account Adapter Service - integrates with multiple external core
  banking systems (current, savings, investment, card, loan accounts) to perform
  debit/credit operations, balance checks, and account validation.

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  0. **⚠️ ARCHITECTURAL DECISION: Resilience4j for EXTERNAL Calls Only** (CRITICAL):
     - ✅ MUST use Resilience4j (`@CircuitBreaker`, `@Retry`, `@Timeout`) for calls TO **external core banking systems** (NORTH-SOUTH traffic)
     - ❌ DO NOT use Resilience4j for calls FROM other services TO Account Adapter (EAST-WEST traffic) - Istio handles this
     - 📖 See: `docs/36-RESILIENCE-PATTERNS-DECISION.md` for complete guidance
     - **Rule**: "If the call goes OUTSIDE Kubernetes, use Resilience4j. Otherwise, use Istio."
     - ✅ External calls: Account Adapter → Core Banking System (use Resilience4j)
     - ❌ Internal calls: Payment Service → Account Adapter (DO NOT use Resilience4j, Istio handles)
  
  1. **Circuit Breaker** (CRITICAL):
     - ✅ MUST use `@CircuitBreaker` for ALL **external** system calls (core banking systems)
     - ✅ Circuit opens after 5 consecutive failures (50% threshold)
     - ✅ Half-open state after 30 seconds (test 1 request)
     - ✅ MUST provide fallback method for every circuit breaker
     - ✅ Fallback: Return `SERVICE_UNAVAILABLE` error, do NOT fail silently
  
  2. **Retry Strategy** (CRITICAL):
     - ✅ MUST use `@Retry` with exponential backoff (1s, 2s, 4s)
     - ✅ Maximum 3 retry attempts per request
     - ✅ DO NOT retry for 4xx errors (client errors) - only 5xx
     - ✅ Include `X-Retry-Count` header in retry attempts
     - ❌ NEVER retry non-idempotent operations without idempotency key
  
  3. **Idempotency** (CRITICAL):
     - ✅ MUST send `X-Idempotency-Key` header for ALL debit/credit operations
     - ✅ Use payment ID as idempotency key
     - ✅ If duplicate request → external system returns cached response
     - ✅ Store idempotency responses in Redis (24-hour TTL)
  
  4. **OAuth 2.0 Authentication**:
     - ✅ MUST use OAuth 2.0 client credentials flow
     - ✅ Cache access tokens until expiry (Redis)
     - ✅ Refresh token before expiry (5 minutes buffer)
     - ❌ NEVER request new token for every API call
     - ✅ If token refresh fails → use circuit breaker fallback
  
  5. **Timeout**:
     - ✅ MUST set timeout for ALL external calls (5 seconds default)
     - ✅ Use `RestTemplate` with configured `ClientHttpRequestFactory`
     - ✅ If timeout → trigger circuit breaker
     - ✅ Log timeout events for SRE monitoring
  
  6. **Bulkhead**:
     - ✅ MUST use `@Bulkhead` to limit concurrent requests (max 10 per system)
     - ✅ Reject requests if bulkhead full (503 Service Unavailable)
     - ✅ Separate bulkhead per external system (5 bulkheads total)
  
  7. **Caching**:
     - ✅ Cache balance queries in Redis (60-second TTL)
     - ✅ Include account ID + tenant ID in cache key
     - ✅ Invalidate cache on debit/credit operations
     - ❌ NEVER cache debit/credit responses (operational data)
  
  8. **Error Handling**:
     - ✅ If external system returns 400 → return to caller (invalid request)
     - ✅ If external system returns 500 → retry + circuit breaker
     - ✅ If external system returns 503 → circuit breaker
     - ✅ Log all external system errors with request/response details
  
  9. **Account Routing**:
     - ✅ Extract account type from account ID prefix (e.g., `CURRENT-12345`)
     - ✅ Supported types: CURRENT, SAVINGS, INVESTMENT, CARD, LOAN
     - ✅ If unknown type → throw `UnsupportedAccountTypeException`
  
  10. **Monitoring**:
      - ✅ Track circuit breaker state changes (closed → open → half-open)
      - ✅ Track retry attempts per external system
      - ✅ Track cache hit ratio (target: > 80%)
      - ✅ Expose custom metrics: `external_system_calls_total`, `circuit_breaker_state`
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #3)
        - Account Adapter responsibilities
        - External system integration patterns
        - API endpoints
     
     📄 docs/08-CORE-BANKING-INTEGRATION.md (COMPLETE FILE - 2,500 lines)
        - External core banking systems (5 systems)
        - REST API contracts (debit, credit, balance check)
        - OAuth 2.0 authentication
        - Idempotency patterns
        - Circuit breaker (Resilience4j)
        - Caching strategy (Redis)
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 1.3)
  
  2. External Core Banking Systems (5 Systems):
     
     **System 1: Current Account System**
     - Base URL: https://current-accounts.bank.example.com
     - Endpoints:
       - POST /api/v1/debit (debit operation)
       - POST /api/v1/credit (credit operation)
       - GET /api/v1/balance/{accountId} (balance check)
     - Auth: OAuth 2.0 (client credentials)
     
     **System 2: Savings Account System**
     - Base URL: https://savings-accounts.bank.example.com
     - Same endpoints as above
     
     **System 3: Investment Account System**
     - Base URL: https://investments.bank.example.com
     - Same endpoints as above
     
     **System 4: Card System**
     - Base URL: https://cards.bank.example.com
     - Same endpoints as above
     
     **System 5: Loan System (Homeloan, Car Loan)**
     - Base URL: https://loans.bank.example.com
     - Same endpoints as above
  
  3. REST Client Integration (Debit Example):
     ```java
     @Service
     public class CurrentAccountClient {
         
         @Autowired
         private RestTemplate restTemplate;
         
         @Autowired
         private OAuth2AuthorizedClientManager authorizedClientManager;
         
         @CircuitBreaker(name = "currentAccountService", fallbackMethod = "debitFallback")
         @Retry(name = "currentAccountService", fallbackMethod = "debitFallback")
         @Bulkhead(name = "currentAccountService")
         @Cacheable(value = "balances", key = "#accountId", unless = "#result == null")
         public DebitResponse debit(String accountId, BigDecimal amount, String idempotencyKey) {
             
             String accessToken = getAccessToken();
             
             DebitRequest request = DebitRequest.builder()
                 .accountId(accountId)
                 .amount(amount)
                 .currency("ZAR")
                 .reference("PAYMENT-DEBIT")
                 .idempotencyKey(idempotencyKey)
                 .build();
             
             HttpHeaders headers = new HttpHeaders();
             headers.setBearerAuth(accessToken);
             headers.set("X-Idempotency-Key", idempotencyKey);
             headers.setContentType(MediaType.APPLICATION_JSON);
             
             HttpEntity<DebitRequest> entity = new HttpEntity<>(request, headers);
             
             ResponseEntity<DebitResponse> response = restTemplate.exchange(
                 "https://current-accounts.bank.example.com/api/v1/debit",
                 HttpMethod.POST,
                 entity,
                 DebitResponse.class
             );
             
             return response.getBody();
         }
         
         // Circuit breaker fallback
         private DebitResponse debitFallback(String accountId, BigDecimal amount, 
                                              String idempotencyKey, Exception e) {
             log.error("Circuit breaker activated for debit operation", e);
             return DebitResponse.failure("SERVICE_UNAVAILABLE", 
                                          "Current account system is temporarily unavailable");
         }
         
         // OAuth 2.0 token retrieval
         private String getAccessToken() {
             OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                 .withClientRegistrationId("current-account-system")
                 .principal("payments-engine")
                 .build();
             
             OAuth2AuthorizedClient authorizedClient = 
                 authorizedClientManager.authorize(authorizeRequest);
             
             return authorizedClient.getAccessToken().getTokenValue();
         }
     }
     ```
  
  4. Account Routing Logic:
     ```java
     @Service
     public class AccountAdapterService {
         
         @Autowired
         private CurrentAccountClient currentAccountClient;
         
         @Autowired
         private SavingsAccountClient savingsAccountClient;
         
         @Autowired
         private InvestmentAccountClient investmentAccountClient;
         
         @Autowired
         private CardClient cardClient;
         
         @Autowired
         private LoanClient loanClient;
         
         public DebitResponse debit(String accountId, BigDecimal amount, String idempotencyKey) {
             
             // Determine account type from account ID prefix
             String accountType = extractAccountType(accountId);
             
             return switch (accountType) {
                 case "CURRENT" -> currentAccountClient.debit(accountId, amount, idempotencyKey);
                 case "SAVINGS" -> savingsAccountClient.debit(accountId, amount, idempotencyKey);
                 case "INVESTMENT" -> investmentAccountClient.debit(accountId, amount, idempotencyKey);
                 case "CARD" -> cardClient.debit(accountId, amount, idempotencyKey);
                 case "LOAN" -> loanClient.debit(accountId, amount, idempotencyKey);
                 default -> throw new UnsupportedOperationException("Unknown account type: " + accountType);
             };
         }
         
         private String extractAccountType(String accountId) {
             // Account ID format: {TYPE}-{NUMBER}
             // Example: CURRENT-12345, SAVINGS-67890
             return accountId.split("-")[0];
         }
     }
     ```
  
  5. Resilience Patterns:
     - **Circuit Breaker**: Open circuit after 5 consecutive failures
     - **Retry**: 3 attempts with exponential backoff (1s, 2s, 4s)
     - **Timeout**: 5 seconds per request
     - **Bulkhead**: Max 10 concurrent requests per system
     - **Cache**: Balance queries cached for 60 seconds (Redis)
  
  6. Technology Stack:
     - Java 17
     - Spring Boot 3.2
     - RestTemplate / WebClient
     - Resilience4j (circuit breaker, retry, bulkhead)
     - Spring Security OAuth2 Client
     - Redis (caching)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Account Adapter Architecture
        - 5 external system integrations
        - Circuit breaker patterns
        - OAuth 2.0 authentication
        - Caching strategy
  
  2. LLD (Low-Level Design):
     📋 REST Client per System (5 clients)
        - CurrentAccountClient
        - SavingsAccountClient
        - InvestmentAccountClient
        - CardClient
        - LoanClient
     
     📋 Resilience Configuration
        - Circuit breaker thresholds
        - Retry policies
        - Timeout values
  
  3. Implementation:
     📁 /account-adapter-service/
        ├─ src/main/java/
        │   ├─ controller/
        │   │   └─ AccountAdapterController.java
        │   ├─ service/
        │   │   ├─ AccountAdapterService.java (routing)
        │   │   └─ IdempotencyService.java
        │   ├─ client/
        │   │   ├─ CurrentAccountClient.java
        │   │   ├─ SavingsAccountClient.java
        │   │   ├─ InvestmentAccountClient.java
        │   │   ├─ CardClient.java
        │   │   └─ LoanClient.java
        │   ├─ config/
        │   │   ├─ RestTemplateConfig.java
        │   │   ├─ OAuth2ClientConfig.java
        │   │   ├─ Resilience4jConfig.java
        │   │   └─ RedisConfig.java (caching)
        │   └─ dto/
        │       ├─ DebitRequest.java
        │       ├─ DebitResponse.java
        │       ├─ CreditRequest.java
        │       └─ CreditResponse.java
        └─ src/main/resources/
            └─ application.yml (OAuth2 config, circuit breaker config)
  
  4. Testing:
     ✅ Unit Tests (circuit breaker)
        - Test circuit open after 5 failures
        - Test fallback method invoked
        - Test circuit half-open → closed
     
     ✅ Integration Tests (MockServer)
        - Mock external system responses
        - Test debit/credit operations
        - Test OAuth2 token retrieval
     
     ✅ Performance Tests
        - Test 100 concurrent requests (bulkhead)
        - Verify cache hit ratio (> 80%)
     
     Target: 80% code coverage
  
  5. Documentation:
     📄 README.md
        - Account Adapter overview
        - External systems catalog (5 systems)
        - How to add new system
     
     📄 API.md
        - REST API endpoints
        - Request/response examples
     
     📄 RESILIENCE.md
        - Circuit breaker configuration
        - Retry policies
        - Troubleshooting

Success Criteria:
  ✅ All 5 external systems integrated
  ✅ OAuth 2.0 authentication working
  ✅ Circuit breaker tested (open → half-open → closed)
  ✅ Idempotency working (duplicate request → cached response)
  ✅ Caching working (Redis, 60s TTL)
  ✅ 80% code coverage

Context Sufficiency: ✅ SUFFICIENT
  - Complete integration spec in docs/08 (2,500 lines)
  - REST API contracts defined
  - Resilience patterns documented

Dependencies:
  ✅ Phase 0 (Domain Models) - READY
  ✅ External core banking systems (mocked for testing)
```

---

### Feature 1.4: Routing Service

```yaml
Feature ID: 1.4
Feature Name: Routing Service
Agent Name: Routing Agent
Phase: 1 (Core Services)
Estimated Time: 3 days

Role & Expertise:
  You are a Backend Engineer with expertise in Java Spring Boot, routing logic,
  Drools rules engine, and event-driven architecture.

Task:
  Build the Routing Service - determines which clearing system (SAMOS,
  BankservAfrica, RTC, PayShap, SWIFT) to route each payment to based on
  payment type, amount, currency, and business rules.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #4)
        - Routing responsibilities
        - Decision logic
        - API endpoints
     
     📄 docs/31-DROOLS-RULES-ENGINE.md (Routing section)
        - Drools-based routing rules
        - Dynamic rule updates
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 1.4)
  
  2. Routing Rules (Drools):
     
     **Rule 1: SAMOS (RTGS - Real-Time Gross Settlement)**
     ```drl
     rule "Route to SAMOS"
       salience 10
       when
         $payment: Payment(
           amount.value >= 100000.00,  // >= R100,000
           currency == Currency.ZAR
         )
       then
         $payment.setRoutingDecision(
           RoutingDecision.create(ClearingSystem.SAMOS, "High value payment")
         );
     end
     ```
     
     **Rule 2: BankservAfrica (ACH/EFT)**
     ```drl
     rule "Route to BankservAfrica"
       salience 8
       when
         $payment: Payment(
           paymentType == PaymentType.EFT,
           amount.value < 100000.00,
           currency == Currency.ZAR
         )
       then
         $payment.setRoutingDecision(
           RoutingDecision.create(ClearingSystem.BANKSERV_AFRICA, "Standard EFT")
         );
     end
     ```
     
     **Rule 3: RTC (Real-Time Clearing)**
     ```drl
     rule "Route to RTC"
       salience 9
       when
         $payment: Payment(
           paymentType == PaymentType.RTC,
           amount.value <= 5000.00,  // RTC limit: R5,000
           currency == Currency.ZAR
         )
       then
         $payment.setRoutingDecision(
           RoutingDecision.create(ClearingSystem.RTC, "Real-time clearing")
         );
     end
     ```
     
     **Rule 4: PayShap (Instant P2P)**
     ```drl
     rule "Route to PayShap"
       salience 9
       when
         $payment: Payment(
           paymentType == PaymentType.PAYSHAP,
           amount.value <= 3000.00,  // PayShap limit: R3,000
           currency == Currency.ZAR
         )
       then
         $payment.setRoutingDecision(
           RoutingDecision.create(ClearingSystem.PAYSHAP, "Instant P2P payment")
         );
     end
     ```
     
     **Rule 5: SWIFT (International)**
     ```drl
     rule "Route to SWIFT"
       salience 10
       when
         $payment: Payment(
           currency in (Currency.USD, Currency.EUR, Currency.GBP)
         )
       then
         $payment.setRoutingDecision(
           RoutingDecision.create(ClearingSystem.SWIFT, "International payment")
         );
     end
     ```
  
  3. Routing Flow:
     ```
     PaymentValidatedEvent received
         ↓
     Load Payment from database
         ↓
     Execute Drools routing rules (KIE session)
         ↓
     RoutingDecision determined
         ↓
     Publish PaymentRoutedEvent
     ```
  
  4. Event Handler:
     ```java
     @Service
     public class RoutingEventHandler {
         
         @Autowired
         private RoutingService routingService;
         
         @Autowired
         private EventPublisher eventPublisher;
         
         @ServiceBusQueueTrigger(
             name = "paymentValidated",
             queueName = "payment-validated",
             connection = "ServiceBusConnectionString"
         )
         public void handlePaymentValidated(
             @ServiceBusMessage PaymentValidatedEvent event,
             final ExecutionContext context
         ) {
             log.info("Routing payment: {}", event.getPaymentId());
             
             RoutingDecision decision = routingService.route(event.getPaymentId());
             
             // Publish PaymentRoutedEvent
             PaymentRoutedEvent routedEvent = PaymentRoutedEvent.builder()
                 .paymentId(event.getPaymentId())
                 .clearingSystem(decision.getClearingSystem())
                 .routingReason(decision.getReason())
                 .routedAt(Instant.now())
                 .build();
             
             eventPublisher.publish("payment-routed", routedEvent);
             
             log.info("Payment routed to {}: {}", 
                      decision.getClearingSystem(), 
                      event.getPaymentId());
         }
     }
     ```
  
  5. Technology Stack:
     - Java 17
     - Spring Boot 3.2
     - Drools 8.44+ (rules engine)
     - Azure Service Bus (event consumer/publisher)
     - Redis (rules caching)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Routing Architecture
        - Event consumption (PaymentValidatedEvent)
        - Drools rules execution
        - RoutingDecision
        - Event publishing (PaymentRoutedEvent)
  
  2. LLD (Low-Level Design):
     📋 Routing Rules (Drools DRL)
        - SAMOS rule (>= R100,000)
        - BankservAfrica rule (EFT, < R100,000)
        - RTC rule (<= R5,000)
        - PayShap rule (<= R3,000)
        - SWIFT rule (USD, EUR, GBP)
     
     📋 RoutingService API
        - route(paymentId) → RoutingDecision
        - reloadRules() → void
  
  3. Implementation:
     📁 /routing-service/
        ├─ src/main/java/
        │   ├─ controller/
        │   │   └─ RoutingController.java
        │   ├─ service/
        │   │   ├─ RoutingService.java
        │   │   └─ DroolsService.java
        │   ├─ event/
        │   │   ├─ RoutingEventHandler.java
        │   │   ├─ PaymentValidatedConsumer.java
        │   │   └─ RoutingEventPublisher.java
        │   ├─ config/
        │   │   ├─ DroolsConfig.java
        │   │   └─ ServiceBusConfig.java
        │   └─ domain/
        │       ├─ RoutingDecision.java
        │       └─ ClearingSystem.java (Enum)
        ├─ src/main/resources/
        │   ├─ rules/
        │   │   ├─ samos-routing.drl
        │   │   ├─ bankserv-routing.drl
        │   │   ├─ rtc-routing.drl
        │   │   ├─ payshap-routing.drl
        │   │   └─ swift-routing.drl
        │   └─ application.yml
        └─ README.md
  
  4. Testing:
     ✅ Unit Tests (Drools rules)
        - Test each routing rule
        - Test rule priority (salience)
     
     ✅ Integration Tests
        - Consume PaymentValidatedEvent → assert PaymentRoutedEvent
        - Test rule hot reload
     
     Target: 80% code coverage
  
  5. Documentation:
     📄 README.md
        - Routing service overview
        - Routing rules catalog
        - How to add new routing rule
     
     📄 API.md
        - REST API endpoints
        - Event subscriptions

Success Criteria:
  ✅ All 5 routing rules defined (SAMOS, BankservAfrica, RTC, PayShap, SWIFT)
  ✅ Drools integration working
  ✅ Event consumption/publishing working
  ✅ 80% code coverage

Context Sufficiency: ✅ SUFFICIENT
  - Complete Drools spec in docs/31 (routing section)
  - Routing rules examples provided
  - Clearing system specs (docs/02)

Dependencies:
  ✅ Phase 0 (Domain Models, Event Schemas) - READY
  ✅ Feature 1.2 (Validation Service) - READY
```

---

### Feature 1.5: Transaction Processing Service

```yaml
Feature ID: 1.5
Feature Name: Transaction Processing Service
Agent Name: Transaction Processing Agent
Phase: 1 (Core Services)
Estimated Time: 5 days (most complex core service)

Role & Expertise:
  You are a Backend Engineer with expertise in Java Spring Boot, distributed
  transactions, event-driven architecture, and complex orchestration logic.

Task:
  Build the Transaction Processing Service - the core orchestrator that executes
  the complete payment flow: account debit, fraud check, limit check, fee
  calculation, and clearing submission. Uses event-driven orchestration pattern.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #5)
        - Transaction Processing responsibilities
        - Orchestration logic
        - API endpoints
     
     📄 docs/07-SAGA-PATTERN.md (COMPLETE FILE - 2,500 lines)
        - Saga orchestration pattern
        - Compensation logic (rollback)
        - State machine (PENDING → PROCESSING → COMPLETED)
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 1.5)
  
  2. Transaction Processing Flow (Orchestrated):
     ```
     PaymentRoutedEvent received
         ↓
     1. Debit debtor account (Account Adapter)
         ↓ (if failed → compensate)
     2. Check fraud score (Fraud Service)
         ↓ (if HIGH_RISK → reject, compensate debit)
     3. Check/update limits (Limit Service)
         ↓ (if exceeded → reject, compensate debit)
     4. Calculate fees (Fee Service)
         ↓
     5. Submit to clearing system (Clearing Adapter)
         ↓
     If ALL steps succeed → Publish TransactionCompletedEvent
     If ANY step fails → Publish TransactionFailedEvent + compensate
     ```
  
  3. Orchestration Service:
     ```java
     @Service
     @Slf4j
     public class TransactionOrchestrationService {
         
         @Autowired
         private AccountAdapterClient accountAdapterClient;
         
         @Autowired
         private FraudServiceClient fraudServiceClient;
         
         @Autowired
         private LimitServiceClient limitServiceClient;
         
         @Autowired
         private FeeServiceClient feeServiceClient;
         
         @Autowired
         private ClearingAdapterClient clearingAdapterClient;
         
         @Autowired
         private EventPublisher eventPublisher;
         
         @Transactional
         public void processTransaction(String paymentId, ClearingSystem clearingSystem) {
             
             TransactionState state = new TransactionState(paymentId);
             
             try {
                 // Step 1: Debit debtor account
                 log.info("Step 1: Debiting account for payment: {}", paymentId);
                 DebitResponse debitResponse = accountAdapterClient.debit(
                     payment.getDebtorAccountId(),
                     payment.getAmount(),
                     paymentId // idempotency key
                 );
                 
                 if (!debitResponse.isSuccess()) {
                     throw new TransactionException("Debit failed: " + debitResponse.getErrorMessage());
                 }
                 state.setDebitCompleted(true);
                 
                 // Step 2: Check fraud score
                 log.info("Step 2: Checking fraud score for payment: {}", paymentId);
                 FraudScoreResponse fraudResponse = fraudServiceClient.checkFraud(paymentId);
                 
                 if (fraudResponse.getRiskLevel() == RiskLevel.HIGH) {
                     throw new TransactionException("Payment blocked due to HIGH fraud risk");
                 }
                 
                 // Step 3: Check/update limits
                 log.info("Step 3: Checking limits for payment: {}", paymentId);
                 LimitCheckResponse limitResponse = limitServiceClient.checkAndUpdateLimit(
                     payment.getTenantId(),
                     payment.getPaymentType(),
                     payment.getAmount()
                 );
                 
                 if (!limitResponse.isWithinLimit()) {
                     throw new TransactionException("Payment exceeds limit: " + limitResponse.getMessage());
                 }
                 state.setLimitUpdated(true);
                 
                 // Step 4: Calculate fees
                 log.info("Step 4: Calculating fees for payment: {}", paymentId);
                 FeeCalculationResponse feeResponse = feeServiceClient.calculateFee(
                     payment.getPaymentType(),
                     payment.getAmount()
                 );
                 
                 // Step 5: Submit to clearing system
                 log.info("Step 5: Submitting to clearing system: {}", clearingSystem);
                 ClearingSubmissionResponse clearingResponse = clearingAdapterClient.submit(
                     paymentId,
                     clearingSystem
                 );
                 
                 if (!clearingResponse.isSuccess()) {
                     throw new TransactionException("Clearing submission failed: " + clearingResponse.getErrorMessage());
                 }
                 
                 // All steps succeeded → Publish TransactionCompletedEvent
                 TransactionCompletedEvent completedEvent = TransactionCompletedEvent.builder()
                     .paymentId(paymentId)
                     .clearingSystem(clearingSystem)
                     .completedAt(Instant.now())
                     .build();
                 
                 eventPublisher.publish("transaction-completed", completedEvent);
                 log.info("Transaction completed successfully: {}", paymentId);
                 
             } catch (Exception e) {
                 log.error("Transaction failed: {}", paymentId, e);
                 
                 // Compensate (rollback) completed steps
                 compensate(state);
                 
                 // Publish TransactionFailedEvent
                 TransactionFailedEvent failedEvent = TransactionFailedEvent.builder()
                     .paymentId(paymentId)
                     .failureReason(e.getMessage())
                     .failedAt(Instant.now())
                     .build();
                 
                 eventPublisher.publish("transaction-failed", failedEvent);
             }
         }
         
         private void compensate(TransactionState state) {
             log.info("Compensating transaction: {}", state.getPaymentId());
             
             // Compensate in reverse order
             if (state.isLimitUpdated()) {
                 log.info("Compensating: Reverting limit update");
                 limitServiceClient.revertLimit(state.getPaymentId());
             }
             
             if (state.isDebitCompleted()) {
                 log.info("Compensating: Crediting debtor account (reversal)");
                 accountAdapterClient.credit(
                     payment.getDebtorAccountId(),
                     payment.getAmount(),
                     state.getPaymentId() + "-REVERSAL"
                 );
             }
             
             log.info("Compensation completed: {}", state.getPaymentId());
         }
     }
     ```
  
  4. Transaction State (for compensation):
     ```java
     @Data
     public class TransactionState {
         private String paymentId;
         private boolean debitCompleted = false;
         private boolean limitUpdated = false;
         
         public TransactionState(String paymentId) {
             this.paymentId = paymentId;
         }
     }
     ```
  
  5. Technology Stack:
     - Java 17
     - Spring Boot 3.2
     - Azure Service Bus (event consumer/publisher)
     - RestTemplate (client integrations)
     - PostgreSQL (transaction state persistence)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Transaction Processing Architecture
        - Event consumption (PaymentRoutedEvent)
        - 5-step orchestration flow
        - Compensation logic (rollback)
        - Event publishing (Completed/Failed)
  
  2. LLD (Low-Level Design):
     📋 Orchestration Flow
        - Step 1: Debit account
        - Step 2: Fraud check
        - Step 3: Limit check
        - Step 4: Fee calculation
        - Step 5: Clearing submission
     
     📋 Compensation Logic
        - Revert limit update
        - Credit debtor account (reversal)
  
  3. Implementation:
     📁 /transaction-processing-service/
        ├─ src/main/java/
        │   ├─ controller/
        │   │   └─ TransactionController.java
        │   ├─ service/
        │   │   ├─ TransactionOrchestrationService.java
        │   │   ├─ CompensationService.java
        │   │   └─ TransactionStateService.java
        │   ├─ client/
        │   │   ├─ AccountAdapterClient.java
        │   │   ├─ FraudServiceClient.java
        │   │   ├─ LimitServiceClient.java
        │   │   ├─ FeeServiceClient.java
        │   │   └─ ClearingAdapterClient.java
        │   ├─ event/
        │   │   ├─ TransactionEventHandler.java
        │   │   ├─ PaymentRoutedConsumer.java
        │   │   └─ TransactionEventPublisher.java
        │   ├─ domain/
        │   │   ├─ TransactionState.java
        │   │   └─ CompensationAction.java
        │   └─ config/
        │       └─ ServiceBusConfig.java
        └─ README.md
  
  4. Testing:
     ✅ Unit Tests (orchestration logic)
        - Test happy path (all steps succeed)
        - Test failure at each step
        - Test compensation logic
     
     ✅ Integration Tests (with mocks)
        - Mock all 5 client services
        - Test PaymentRoutedEvent → TransactionCompletedEvent
        - Test failure → TransactionFailedEvent + compensation
     
     Target: 85% code coverage
  
  5. Documentation:
     📄 README.md
        - Transaction Processing overview
        - Orchestration flow diagram
        - Compensation logic
     
     📄 API.md
        - REST API endpoints
        - Event subscriptions

Success Criteria:
  ✅ All 5 orchestration steps implemented
  ✅ Compensation logic working (rollback on failure)
  ✅ Event consumption/publishing working
  ✅ 85% code coverage

Context Sufficiency: ✅ SUFFICIENT
  - Complete Saga spec in docs/07 (2,500 lines)
  - Orchestration pattern detailed
  - Compensation logic explained

Dependencies:
  ✅ Phase 0 (Domain Models, Event Schemas) - READY
  ✅ Feature 1.3 (Account Adapter) - READY
  ✅ Feature 1.4 (Routing Service) - READY
  ✅ Fraud Service (Phase 1.x or Platform Services)
  ✅ Limit Service (Phase 1.x or Platform Services)
  ✅ Fee Service (Phase 1.x or Platform Services)
```

---

### Feature 1.6: Saga Orchestrator Service

```yaml
Feature ID: 1.6
Feature Name: Saga Orchestrator Service
Agent Name: Saga Orchestrator Agent
Phase: 1 (Core Services)
Estimated Time: 5 days (most complex core service)

Role & Expertise:
  You are a Backend Engineer with expertise in Java Spring Boot, Saga pattern,
  distributed transactions, state machines, and event-driven architecture.

Task:
  Build the Saga Orchestrator Service - manages the complete payment Saga
  lifecycle using a persistent state machine. Handles orchestration, compensation,
  retries, and failure recovery for all payment flows.

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **State Machine** (CRITICAL):
     - ✅ MUST persist saga state to database BEFORE and AFTER each transition
     - ✅ Use pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) to prevent race conditions
     - ✅ MUST support state recovery (service restart → resume from last state)
     - ✅ Validate state transitions (e.g., cannot go from COMPLETED to INITIATED)
     - ❌ NEVER skip state persistence (risk of data loss)
  
  2. **Compensation Logic** (CRITICAL):
     - ✅ MUST track compensation actions in reverse order
     - ✅ MUST execute compensations even if some fail (best-effort)
     - ✅ Log each compensation action (success/failure)
     - ✅ If compensation fails → mark saga as `COMPENSATION_FAILED`, alert SRE
     - ✅ Store compensation history for audit trail
  
  3. **Idempotency** (CRITICAL):
     - ✅ MUST handle duplicate events gracefully (check saga state)
     - ✅ If saga already in terminal state (COMPLETED/COMPENSATED) → ignore event
     - ✅ If saga in progress → check current state, continue from there
     - ❌ NEVER re-execute completed steps
  
  4. **Retry Strategy**:
     - ✅ Retry failed sagas 3 times with 1-minute interval
     - ✅ Use `@Scheduled` job to find and retry failed sagas
     - ✅ If retry count exhausted → mark as `ABANDONED`, create alert
     - ✅ Track retry count in saga state (`retry_count` column)
  
  5. **Timeout**:
     - ✅ MUST set timeout for each step (5-10 seconds per external call)
     - ✅ If step times out → mark as FAILED, trigger compensation
     - ✅ Total saga timeout: 60 seconds (all steps combined)
     - ✅ If total timeout → compensate and mark as ABANDONED
  
  6. **Event Publishing**:
     - ✅ Publish events for state transitions: `SagaCompletedEvent`, `SagaFailedEvent`
     - ✅ Include correlation ID for tracing
     - ✅ If event publish fails → retry 3 times, then log error (continue saga)
  
  7. **Concurrency**:
     - ✅ Use database locking to prevent concurrent saga updates
     - ✅ If lock timeout → retry saga processing
     - ✅ Avoid deadlocks (always acquire locks in same order)
  
  8. **Monitoring**:
     - ✅ Track saga duration per state (identify bottlenecks)
     - ✅ Track compensation rate (% of sagas compensated)
     - ✅ Track retry rate, abandoned rate
     - ✅ Expose metrics: `saga_total`, `saga_completed`, `saga_failed`, `saga_compensated`
  
  9. **Error Handling**:
     - ✅ If ANY step fails → log detailed error (step name, reason, payment ID)
     - ✅ Include full stack trace in error logs (DEBUG level)
     - ✅ Update saga with failure reason in database
  
  10. **Logging**:
      - ✅ Log each state transition: `Saga transitioning: paymentId={}, from={}, to={}`
      - ✅ Log compensation: `Compensating saga: paymentId={}, actions={}`
      - ✅ Include correlation ID in all logs
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (Service #6)
        - Saga Orchestrator responsibilities
        - State machine design
        - API endpoints
     
     📄 docs/07-SAGA-PATTERN.md (COMPLETE FILE - 2,500 lines)
        - Saga orchestration vs choreography
        - State machine (9 states)
        - Compensation logic
        - Retry strategies
        - Failure recovery
        - Kafka option (exactly-once semantics)
     
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 1.6)
  
  2. Saga State Machine (9 States):
     ```
     INITIATED → VALIDATED → ROUTED → DEBITED → FRAUD_CHECKED → 
     LIMIT_CHECKED → FEE_CALCULATED → SUBMITTED → COMPLETED
     
     Failure states:
     - REJECTED (validation failed)
     - FAILED (any step failed)
     - COMPENSATING (rolling back)
     - COMPENSATED (rollback complete)
     ```
  
  3. Saga State Machine Implementation:
     ```java
     @Service
     @Slf4j
     public class SagaOrchestrator {
         
         @Autowired
         private SagaStateRepository sagaStateRepository;
         
         @Autowired
         private AccountAdapterClient accountAdapterClient;
         
         @Autowired
         private FraudServiceClient fraudServiceClient;
         
         @Autowired
         private LimitServiceClient limitServiceClient;
         
         @Autowired
         private FeeServiceClient feeServiceClient;
         
         @Autowired
         private ClearingAdapterClient clearingAdapterClient;
         
         @Autowired
         private EventPublisher eventPublisher;
         
         @Transactional
         public void handlePaymentInitiated(PaymentInitiatedEvent event) {
             
             // Create Saga state
             SagaState saga = SagaState.create(event.getPaymentId());
             saga.transitionTo(SagaStatus.INITIATED);
             sagaStateRepository.save(saga);
             
             log.info("Saga initiated: {}", event.getPaymentId());
             
             // Start orchestration
             orchestrate(saga);
         }
         
         private void orchestrate(SagaState saga) {
             
             try {
                 // Step 1: Wait for validation
                 if (saga.getStatus() == SagaStatus.INITIATED) {
                     saga.transitionTo(SagaStatus.VALIDATING);
                     sagaStateRepository.save(saga);
                     // Event: PaymentValidatedEvent → triggers next step
                     return;
                 }
                 
                 // Step 2: Wait for routing
                 if (saga.getStatus() == SagaStatus.VALIDATED) {
                     saga.transitionTo(SagaStatus.ROUTING);
                     sagaStateRepository.save(saga);
                     // Event: PaymentRoutedEvent → triggers next step
                     return;
                 }
                 
                 // Step 3: Debit account
                 if (saga.getStatus() == SagaStatus.ROUTED) {
                     log.info("Step 3: Debiting account for saga: {}", saga.getPaymentId());
                     
                     DebitResponse response = accountAdapterClient.debit(
                         saga.getPayment().getDebtorAccountId(),
                         saga.getPayment().getAmount(),
                         saga.getPaymentId()
                     );
                     
                     if (response.isSuccess()) {
                         saga.transitionTo(SagaStatus.DEBITED);
                         saga.addCompensationAction(CompensationAction.CREDIT_ACCOUNT);
                         sagaStateRepository.save(saga);
                     } else {
                         throw new SagaException("Debit failed: " + response.getErrorMessage());
                     }
                 }
                 
                 // Step 4: Fraud check
                 if (saga.getStatus() == SagaStatus.DEBITED) {
                     log.info("Step 4: Fraud check for saga: {}", saga.getPaymentId());
                     
                     FraudScoreResponse response = fraudServiceClient.checkFraud(
                         saga.getPaymentId()
                     );
                     
                     if (response.getRiskLevel() != RiskLevel.HIGH) {
                         saga.transitionTo(SagaStatus.FRAUD_CHECKED);
                         sagaStateRepository.save(saga);
                     } else {
                         throw new SagaException("Payment blocked due to HIGH fraud risk");
                     }
                 }
                 
                 // Step 5: Limit check
                 if (saga.getStatus() == SagaStatus.FRAUD_CHECKED) {
                     log.info("Step 5: Limit check for saga: {}", saga.getPaymentId());
                     
                     LimitCheckResponse response = limitServiceClient.checkAndUpdateLimit(
                         saga.getPayment().getTenantId(),
                         saga.getPayment().getPaymentType(),
                         saga.getPayment().getAmount()
                     );
                     
                     if (response.isWithinLimit()) {
                         saga.transitionTo(SagaStatus.LIMIT_CHECKED);
                         saga.addCompensationAction(CompensationAction.REVERT_LIMIT);
                         sagaStateRepository.save(saga);
                     } else {
                         throw new SagaException("Payment exceeds limit");
                     }
                 }
                 
                 // Step 6: Fee calculation
                 if (saga.getStatus() == SagaStatus.LIMIT_CHECKED) {
                     log.info("Step 6: Fee calculation for saga: {}", saga.getPaymentId());
                     
                     FeeCalculationResponse response = feeServiceClient.calculateFee(
                         saga.getPayment().getPaymentType(),
                         saga.getPayment().getAmount()
                     );
                     
                     saga.setFeeAmount(response.getFeeAmount());
                     saga.transitionTo(SagaStatus.FEE_CALCULATED);
                     sagaStateRepository.save(saga);
                 }
                 
                 // Step 7: Submit to clearing
                 if (saga.getStatus() == SagaStatus.FEE_CALCULATED) {
                     log.info("Step 7: Clearing submission for saga: {}", saga.getPaymentId());
                     
                     ClearingSubmissionResponse response = clearingAdapterClient.submit(
                         saga.getPaymentId(),
                         saga.getClearingSystem()
                     );
                     
                     if (response.isSuccess()) {
                         saga.transitionTo(SagaStatus.SUBMITTED);
                         sagaStateRepository.save(saga);
                     } else {
                         throw new SagaException("Clearing submission failed");
                     }
                 }
                 
                 // Step 8: Wait for clearing response
                 if (saga.getStatus() == SagaStatus.SUBMITTED) {
                     saga.transitionTo(SagaStatus.COMPLETED);
                     sagaStateRepository.save(saga);
                     
                     // Publish SagaCompletedEvent
                     SagaCompletedEvent completedEvent = SagaCompletedEvent.builder()
                         .paymentId(saga.getPaymentId())
                         .completedAt(Instant.now())
                         .build();
                     
                     eventPublisher.publish("saga-completed", completedEvent);
                     log.info("Saga completed: {}", saga.getPaymentId());
                 }
                 
             } catch (Exception e) {
                 log.error("Saga failed: {}", saga.getPaymentId(), e);
                 saga.transitionTo(SagaStatus.FAILED);
                 saga.setFailureReason(e.getMessage());
                 sagaStateRepository.save(saga);
                 
                 // Start compensation
                 compensate(saga);
             }
         }
         
         private void compensate(SagaState saga) {
             log.info("Starting compensation for saga: {}", saga.getPaymentId());
             
             saga.transitionTo(SagaStatus.COMPENSATING);
             sagaStateRepository.save(saga);
             
             // Execute compensation actions in reverse order
             List<CompensationAction> actions = saga.getCompensationActions();
             Collections.reverse(actions);
             
             for (CompensationAction action : actions) {
                 try {
                     switch (action) {
                         case REVERT_LIMIT:
                             log.info("Compensating: Reverting limit");
                             limitServiceClient.revertLimit(saga.getPaymentId());
                             break;
                         
                         case CREDIT_ACCOUNT:
                             log.info("Compensating: Crediting account");
                             accountAdapterClient.credit(
                                 saga.getPayment().getDebtorAccountId(),
                                 saga.getPayment().getAmount(),
                                 saga.getPaymentId() + "-COMPENSATION"
                             );
                             break;
                     }
                 } catch (Exception e) {
                     log.error("Compensation action failed: {}", action, e);
                     // Continue with other compensation actions
                 }
             }
             
             saga.transitionTo(SagaStatus.COMPENSATED);
             sagaStateRepository.save(saga);
             
             // Publish SagaCompensatedEvent
             SagaCompensatedEvent compensatedEvent = SagaCompensatedEvent.builder()
                 .paymentId(saga.getPaymentId())
                 .failureReason(saga.getFailureReason())
                 .compensatedAt(Instant.now())
                 .build();
             
             eventPublisher.publish("saga-compensated", compensatedEvent);
             log.info("Saga compensated: {}", saga.getPaymentId());
         }
     }
     ```
  
  4. Saga State Entity:
     ```java
     @Entity
     @Table(name = "saga_states")
     @Data
     public class SagaState {
         
         @Id
         private String paymentId;
         
         @Enumerated(EnumType.STRING)
         private SagaStatus status;
         
         @ManyToOne(fetch = FetchType.LAZY)
         @JoinColumn(name = "payment_id")
         private Payment payment;
         
         @Enumerated(EnumType.STRING)
         private ClearingSystem clearingSystem;
         
         @Column(name = "fee_amount")
         private BigDecimal feeAmount;
         
         @Column(name = "failure_reason")
         private String failureReason;
         
         @ElementCollection
         @Enumerated(EnumType.STRING)
         private List<CompensationAction> compensationActions = new ArrayList<>();
         
         @Column(name = "created_at")
         private Instant createdAt;
         
         @Column(name = "updated_at")
         private Instant updatedAt;
         
         public static SagaState create(String paymentId) {
             SagaState saga = new SagaState();
             saga.setPaymentId(paymentId);
             saga.setStatus(SagaStatus.INITIATED);
             saga.setCreatedAt(Instant.now());
             saga.setUpdatedAt(Instant.now());
             return saga;
         }
         
         public void transitionTo(SagaStatus newStatus) {
             log.info("Saga {} transitioning from {} to {}", 
                      paymentId, status, newStatus);
             this.status = newStatus;
             this.updatedAt = Instant.now();
         }
         
         public void addCompensationAction(CompensationAction action) {
             this.compensationActions.add(action);
         }
     }
     ```
  
  5. Failure Recovery (Retry):
     ```java
     @Scheduled(fixedDelay = 60000) // Every 1 minute
     public void retryFailedSagas() {
         List<SagaState> failedSagas = sagaStateRepository.findByStatus(SagaStatus.FAILED);
         
         for (SagaState saga : failedSagas) {
             if (saga.getRetryCount() < 3) {
                 log.info("Retrying failed saga: {}", saga.getPaymentId());
                 saga.incrementRetryCount();
                 saga.transitionTo(saga.getPreviousStatus());
                 sagaStateRepository.save(saga);
                 
                 // Re-run orchestration
                 orchestrate(saga);
             } else {
                 log.error("Saga exhausted retries: {}", saga.getPaymentId());
                 saga.transitionTo(SagaStatus.ABANDONED);
                 sagaStateRepository.save(saga);
             }
         }
     }
     ```
  
  6. Technology Stack:
     - Java 17
     - Spring Boot 3.2
     - Spring State Machine (state machine framework)
     - Azure Service Bus (default) or Kafka (option)
     - PostgreSQL (saga state persistence)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Saga Orchestrator Architecture
        - Event-driven orchestration
        - 9-state state machine
        - Compensation logic
        - Retry/recovery logic
  
  2. LLD (Low-Level Design):
     📋 State Machine (9 states)
        - State transitions
        - Event triggers
        - Compensation actions
     
     📋 Orchestration API
        - orchestrate(saga) → void
        - compensate(saga) → void
        - retryFailedSagas() → void
  
  3. Implementation:
     📁 /saga-orchestrator-service/
        ├─ src/main/java/
        │   ├─ controller/
        │   │   └─ SagaController.java (admin API)
        │   ├─ service/
        │   │   ├─ SagaOrchestrator.java (main orchestrator)
        │   │   ├─ CompensationService.java
        │   │   └─ RetryService.java
        │   ├─ statemachine/
        │   │   ├─ SagaStateMachine.java
        │   │   ├─ SagaStatus.java (Enum)
        │   │   └─ StateTransition.java
        │   ├─ client/
        │   │   ├─ AccountAdapterClient.java
        │   │   ├─ FraudServiceClient.java
        │   │   ├─ LimitServiceClient.java
        │   │   ├─ FeeServiceClient.java
        │   │   └─ ClearingAdapterClient.java
        │   ├─ event/
        │   │   ├─ SagaEventHandler.java
        │   │   └─ SagaEventPublisher.java
        │   ├─ domain/
        │   │   ├─ SagaState.java (Entity)
        │   │   └─ CompensationAction.java (Enum)
        │   ├─ repository/
        │   │   └─ SagaStateRepository.java
        │   └─ config/
        │       ├─ StateMachineConfig.java
        │       └─ ServiceBusConfig.java
        └─ README.md
  
  4. Testing:
     ✅ Unit Tests (state machine)
        - Test state transitions
        - Test compensation triggers
        - Test retry logic
     
     ✅ Integration Tests
        - Test full saga (happy path)
        - Test saga with failure (compensation)
        - Test retry after failure
     
     Target: 85% code coverage
  
  5. Documentation:
     📄 README.md
        - Saga Orchestrator overview
        - State machine diagram
        - Compensation logic
     
     📄 SAGA-PATTERN.md
        - Orchestration vs choreography
        - When to use Saga
        - Troubleshooting

Success Criteria:
  ✅ 9-state state machine implemented
  ✅ Orchestration working (all 7 steps)
  ✅ Compensation working (rollback on failure)
  ✅ Retry working (3 attempts)
  ✅ Saga state persisted (PostgreSQL)
  ✅ 85% code coverage

Context Sufficiency: ✅ SUFFICIENT
  - Complete Saga spec in docs/07 (2,500 lines)
  - State machine design detailed
  - Compensation patterns explained
  - Retry strategies defined

Dependencies:
  ✅ Phase 0 (Domain Models, Event Schemas) - READY
  ✅ Feature 1.1 (Payment Initiation) - READY
  ✅ Feature 1.2 (Validation Service) - READY
  ✅ Feature 1.3 (Account Adapter) - READY
  ✅ Feature 1.4 (Routing Service) - READY
  ✅ All platform services (Fraud, Limit, Fee) - READY
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

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:

  0. **⚠️ ARCHITECTURAL DECISION: Resilience4j for EXTERNAL Calls Only** (CRITICAL):
     - ✅ MUST use Resilience4j (`@CircuitBreaker`, `@Retry`, `@Timeout`) for calls TO **external SAMOS RTGS system** (NORTH-SOUTH traffic)
     - ❌ DO NOT use Resilience4j for calls FROM other services TO SAMOS Adapter (EAST-WEST traffic) - Istio handles this
     - 📖 See: `docs/36-RESILIENCE-PATTERNS-DECISION.md` for complete guidance
     - **Rule**: "If the call goes OUTSIDE Kubernetes, use Resilience4j. Otherwise, use Istio."
     - ✅ External calls: SAMOS Adapter → SAMOS RTGS System (use Resilience4j)
     - ❌ Internal calls: Routing Service → SAMOS Adapter (DO NOT use Resilience4j, Istio handles)

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

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **Sanctions Screening** (CRITICAL - REGULATORY REQUIREMENT):
     - ✅ MUST perform sanctions screening BEFORE submitting to SWIFT
     - ✅ Screen against: OFAC, UN, EU sanctions lists
     - ✅ Screen: Beneficiary name, country, bank name, bank country
     - ✅ If match found → IMMEDIATELY block payment, set status `SANCTIONS_BLOCKED`
     - ✅ MUST raise compliance alert for manual review
     - ❌ NEVER bypass sanctions screening (legal violation, heavy fines)
     - ✅ Cache sanctions lists in Redis (1-hour TTL, daily refresh)
  
  2. **SWIFT Message Format** (CRITICAL):
     - ✅ Support both MT103 (legacy) and pacs.008 (ISO 20022)
     - ✅ Validate MT103 format: 35 characters per line, specific field positions
     - ✅ Validate pacs.008 XML: against ISO 20022 XSD schema
     - ✅ If validation fails → reject payment, return detailed error
     - ❌ NEVER send malformed messages to SWIFT (risk of rejection)
  
  3. **FX Rate Conversion**:
     - ✅ Fetch real-time FX rates from external provider (e.g., OpenExchangeRates API)
     - ✅ Cache rates for 5 minutes (Redis)
     - ✅ If FX API unavailable → use fallback rates (last known good)
     - ✅ If no fallback → reject payment with `FX_RATE_UNAVAILABLE`
     - ✅ Include FX rate, fee in payment record (audit trail)
  
  4. **Correspondent Bank Routing**:
     - ✅ Determine correspondent bank based on beneficiary country
     - ✅ Use BIC (Bank Identifier Code) for routing
     - ✅ If correspondent bank unknown → reject payment
     - ✅ Store correspondent bank relationships in database
  
  5. **SWIFT gpi Tracking**:
     - ✅ Generate UETR (Unique End-to-End Transaction Reference)
     - ✅ Format: UUID v4 (e.g., `550e8400-e29b-41d4-a716-446655440000`)
     - ✅ Include UETR in MT103 field 121, pacs.008 `TxId`
     - ✅ Store UETR for payment tracking
  
  6. **Security**:
     - ✅ Use mTLS (mutual TLS) for SWIFT Alliance Lite2 connection
     - ✅ Store SWIFT credentials in Azure Key Vault
     - ✅ Validate SWIFT response signature (message authentication)
     - ❌ NEVER log SWIFT credentials or full message content (contains PII)
  
  7. **Error Handling**:
     - ✅ If SWIFT rejects (NAK) → parse rejection reason, update payment status
     - ✅ If SWIFT timeout (> 30s) → mark as `PENDING_SWIFT`, retry
     - ✅ If sanctions match → do NOT retry, permanently block
     - ✅ Log all SWIFT errors with UETR for traceability
  
  8. **Compliance**:
     - ✅ MUST comply with FICA (Financial Intelligence Centre Act)
     - ✅ Record all SWIFT payments for 5 years (audit trail)
     - ✅ If beneficiary in high-risk country → flag for manual review
     - ✅ Implement AML (Anti-Money Laundering) checks
  
  9. **Performance**:
     - ✅ Target: < 5s response time (including sanctions screening)
     - ✅ Parallel execution: sanctions + FX lookup (async)
     - ✅ Batch SWIFT submissions if possible (10 messages/batch)
  
  10. **Monitoring**:
      - ✅ Track sanctions screening rate (% blocked)
      - ✅ Track SWIFT submission success rate (> 98%)
      - ✅ Track FX API availability
      - ✅ Expose metrics: `swift_payments_total`, `sanctions_blocked_total`, `fx_lookup_failures`
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

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

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **File Security** (CRITICAL):
     - ✅ Validate file size: max 50 MB (prevent DoS)
     - ✅ Validate file type: allow only [CSV, XLSX, XML, JSON, TXT]
     - ✅ Scan file for viruses/malware (ClamAV or Azure Defender)
     - ✅ Validate file structure BEFORE processing (schema validation)
     - ❌ NEVER process files without validation (risk of XXE, billion laughs)
  
  2. **File Content Validation** (CRITICAL):
     - ✅ Validate each record: amount > 0, valid currency, valid account IDs
     - ✅ Skip invalid records, log to error file
     - ✅ Generate summary report: total, processed, skipped, failed
     - ✅ If > 10% records invalid → reject entire file
  
  3. **SFTP Security**:
     - ✅ Use SSH key authentication (NOT password)
     - ✅ Store SSH private key in Azure Key Vault
     - ✅ Validate SFTP server fingerprint (prevent MITM)
     - ✅ Use secure file permissions (600 for private key)
     - ❌ NEVER download files from untrusted sources
  
  4. **Chunk Processing** (CRITICAL):
     - ✅ Chunk size: 100 records (configurable)
     - ✅ Process chunks in parallel (thread pool: 5 threads)
     - ✅ Commit after each chunk (fault tolerance)
     - ✅ If chunk fails → skip chunk, continue with next
     - ✅ Track failed records in separate error file
  
  5. **File Path Injection Prevention**:
     - ✅ Validate file paths (no `../` traversal)
     - ✅ Use whitelist for allowed directories
     - ✅ Sanitize file names (alphanumeric, hyphens, underscores only)
     - ❌ NEVER use user input directly in file paths
  
  6. **XML Security** (XXE Prevention):
     - ✅ Disable external entity processing (`XMLInputFactory.setProperty`)
     - ✅ Disable DTD processing
     - ✅ Use secure XML parser configuration
     - ❌ NEVER trust external XML files without validation
  
  7. **Performance**:
     - ✅ Target: 1,000 records/minute
     - ✅ Use streaming for large files (don't load entire file in memory)
     - ✅ Delete processed files after 7 days (storage management)
     - ✅ Monitor memory usage (prevent OOM)
  
  8. **Error Reporting**:
     - ✅ Generate error file: {originalFile}_errors.csv
     - ✅ Include: line number, record data, error reason
     - ✅ Upload error file to SFTP server (for client review)
     - ✅ Send notification on batch completion (success/failure)
  
  9. **Idempotency**:
     - ✅ Use file hash (SHA-256) as idempotency key
     - ✅ If same file uploaded again → return cached result
     - ✅ Store processed file hashes in database (30-day retention)
  
  10. **Monitoring**:
      - ✅ Track: files processed, records processed, error rate
      - ✅ Expose metrics: `batch_files_total`, `batch_records_total`, `batch_errors_total`
      - ✅ Alert if error rate > 5%
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

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

### Feature 4.5: Web BFF (GraphQL for React)

```yaml
Feature ID: 4.5
Feature Name: Web BFF (GraphQL for React Web App)
Agent Name: Web BFF Agent
Phase: 4 (Advanced Features)
Estimated Time: 2 days

Role & Expertise:
  You are a Full-Stack Engineer with expertise in GraphQL, Spring Boot GraphQL,
  API aggregation, and client-specific API design for React web apps.

Task:
  Build the Web Backend-for-Frontend (BFF) service using GraphQL to aggregate
  data from multiple microservices and optimize for React web app clients.
  NOTE: This is 1 of 3 BFFs (Web, Mobile, Partner) being built separately.

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

### Feature 4.6: Mobile BFF (REST Lightweight)

```yaml
Feature ID: 4.6
Feature Name: Mobile BFF (REST Lightweight for Mobile Apps)
Agent Name: Mobile BFF Agent
Phase: 4 (Advanced Features)
Estimated Time: 1.5 days

Role & Expertise:
  You are a Full-Stack Engineer with expertise in REST APIs, mobile optimization,
  lightweight payload design, and Spring Boot REST controllers.

Task:
  Build the Mobile Backend-for-Frontend (BFF) service using REST API to aggregate
  data from multiple microservices with lightweight payloads optimized for mobile apps (3G networks).
  NOTE: This is 1 of 3 BFFs (Web, Mobile, Partner) being built separately.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (BFF Layer section)
     📄 docs/17-BFF-BACKEND-FOR-FRONTEND.md (COMPLETE FILE - Mobile BFF section)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 4.6)
  
  2. Mobile BFF Characteristics:
     - LIGHTWEIGHT responses (< 5 KB per request)
     - NO nested objects (flat structure)
     - Minimal fields only (id, amount, status, timestamp)
     - Optimized for 3G networks
     - Fast response times (< 200ms p95)
  
  3. Sample REST API:
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
     (No nested objects, minimal data)
     
     GET /api/v1/mobile/payments?page=0&size=10
     Response:
     {
       "payments": [ ... ],
       "totalElements": 100,
       "totalPages": 10
     }
     ```
  
  4. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring WebFlux (Reactive)
     - Redis (caching, 60s TTL)
     - JWT (authentication)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Mobile BFF Architecture Diagram
     📊 API Aggregation Flow
  
  2. LLD (Low-Level Design):
     📋 REST API Contract (OpenAPI 3.0)
     📋 Data transformation logic
  
  3. Implementation:
     📁 /services/mobile-bff/
        ├─ src/main/java/com/payments/bff/mobile/
        │   ├─ MobileBffApplication.java
        │   ├─ controller/
        │   │   ├─ MobilePaymentController.java
        │   │   └─ MobileAccountController.java
        │   ├─ service/
        │   │   ├─ LightweightPaymentService.java
        │   │   └─ LightweightAccountService.java
        │   ├─ client/
        │   │   ├─ PaymentServiceClient.java
        │   │   └─ AccountServiceClient.java
        │   ├─ dto/
        │   │   ├─ MobilePaymentResponse.java (LIGHTWEIGHT)
        │   │   └─ MobileAccountResponse.java
        │   ├─ config/
        │   │   ├─ WebClientConfig.java
        │   │   └─ RedisConfig.java
        │   └─ security/
        │       └─ JwtAuthenticationFilter.java
        ├─ src/main/resources/
        │   └─ application.yml
        └─ src/test/java/
            └─ ...
  
  4. Unit Tests:
     ✅ Test lightweight payload generation (response size < 5 KB)
     ✅ Test API aggregation from multiple services
     ✅ Test Redis caching (60s TTL)
     ✅ Test JWT authentication
     ✅ Test error handling (fallback to cached data)
     ✅ Target: > 80% code coverage
  
  5. Integration Tests:
     ✅ Test Mobile BFF → Payment Service (WireMock)
     ✅ Test Mobile BFF → Account Service (WireMock)
     ✅ Test Redis caching (Testcontainers)
  
  6. Dockerization:
     📦 Dockerfile (multi-stage build)
     📦 docker-compose.yml (mobile-bff + redis)
  
  7. Kubernetes Deployment:
     ☸ deployment.yaml (HPA: 2-10 replicas)
     ☸ service.yaml (ClusterIP)
     ☸ configmap.yaml
     ☸ secret.yaml

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:

  1. Payload Size: ALL responses MUST be < 5 KB (fail build if larger)
  2. No Nested Objects: Flat structure only (e.g., `status` not `payment.status`)
  3. Performance: API response time < 200ms (p95)
  4. 3G Optimization: Test on simulated 3G network (slow connection)
  5. Security: JWT validation MANDATORY (reject unauthenticated requests)
  6. Caching: Redis cache hit rate > 70% (production)
  7. Error Handling: Return cached data if upstream fails (stale-while-revalidate)
  8. Logging: Log response size per request (for monitoring)

Definition of Done:

  ✅ All 10+ REST endpoints functional
  ✅ Payload size < 5 KB per response (validated)
  ✅ API response time < 200ms (p95) (load tested)
  ✅ Redis caching working (hit rate > 70%)
  ✅ JWT authentication working
  ✅ Unit test coverage > 80%
  ✅ Integration tests pass
  ✅ Swagger UI accessible
  ✅ Service deploys to AKS
  ✅ HPA scaling tested (2-10 replicas)
  ✅ 3G network tested (simulated)

Success Criteria:

  📊 API latency < 200ms (p95)
  📊 Payload size < 5 KB (all responses)
  📊 Cache hit rate > 70%
  📊 Uptime > 99.9% (production)

Dependencies:
  ✅ Phase 0 complete (Infrastructure, Domain Models, Event Schemas)
  ✅ Payment Initiation Service deployed
  ✅ Account Adapter Service deployed
  ✅ Redis cache available
```

---

### Feature 4.7: Partner BFF (REST Comprehensive)

```yaml
Feature ID: 4.7
Feature Name: Partner BFF (REST Comprehensive for Partner Integrations)
Agent Name: Partner BFF Agent
Phase: 4 (Advanced Features)
Estimated Time: 1.5 days

Role & Expertise:
  You are a Full-Stack Engineer with expertise in REST APIs, partner integrations,
  comprehensive API design, rate limiting, and Spring Boot REST controllers.

Task:
  Build the Partner Backend-for-Frontend (BFF) service using REST API to aggregate
  data from multiple microservices with comprehensive payloads for partner integrations.
  NOTE: This is 1 of 3 BFFs (Web, Mobile, Partner) being built separately.

Context Provided:

  1. Architecture Documents:
     📄 docs/02-MICROSERVICES-BREAKDOWN.md (BFF Layer section)
     📄 docs/17-BFF-BACKEND-FOR-FRONTEND.md (COMPLETE FILE - Partner BFF section)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 4.7)
  
  2. Partner BFF Characteristics:
     - COMPREHENSIVE responses (all fields, nested objects, audit trail)
     - Rate limiting (100 requests/minute per partner)
     - Throttling (1000 requests/hour per partner)
     - Partner-specific configurations
     - Full audit trail included
  
  3. Sample REST API:
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
       "auditTrail": [
         {"timestamp": "2025-10-12T10:00:00Z", "action": "INITIATED", ...},
         {"timestamp": "2025-10-12T10:00:05Z", "action": "VALIDATED", ...}
       ],
       "fees": {"amount": 10.00, "currency": "ZAR"},
       "clearingDetails": {"clearingSystem": "SAMOS", "transactionId": "TXN-001"},
       "createdAt": "2025-10-12T10:00:00Z",
       "updatedAt": "2025-10-12T10:00:10Z"
     }
     (Comprehensive data, all nested objects, full audit trail)
     ```
  
  4. Technology Stack:
     - Java 17, Spring Boot 3.2
     - Spring WebFlux (Reactive)
     - Resilience4j (rate limiting, throttling)
     - Redis (caching, rate limiting)
     - JWT (authentication)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Partner BFF Architecture Diagram
     📊 API Aggregation Flow
     📊 Rate Limiting Strategy
  
  2. LLD (Low-Level Design):
     📋 REST API Contract (OpenAPI 3.0)
     📋 Rate limiting configuration
     📋 Data transformation logic
  
  3. Implementation:
     📁 /services/partner-bff/
        ├─ src/main/java/com/payments/bff/partner/
        │   ├─ PartnerBffApplication.java
        │   ├─ controller/
        │   │   ├─ PartnerPaymentController.java (with @RateLimiter)
        │   │   └─ PartnerAccountController.java
        │   ├─ service/
        │   │   ├─ ComprehensivePaymentService.java
        │   │   └─ ComprehensiveAccountService.java
        │   ├─ client/
        │   │   ├─ PaymentServiceClient.java
        │   │   ├─ AccountServiceClient.java
        │   │   └─ AuditServiceClient.java
        │   ├─ dto/
        │   │   ├─ ComprehensivePaymentResponse.java (ALL FIELDS)
        │   │   └─ ComprehensiveAccountResponse.java
        │   ├─ config/
        │   │   ├─ WebClientConfig.java
        │   │   ├─ RateLimiterConfig.java (Resilience4j)
        │   │   └─ RedisConfig.java
        │   └─ security/
        │       └─ JwtAuthenticationFilter.java
        ├─ src/main/resources/
        │   └─ application.yml (rate limiting rules)
        └─ src/test/java/
            └─ ...
  
  4. Unit Tests:
     ✅ Test comprehensive payload generation (all fields, nested objects)
     ✅ Test rate limiting (100 req/min per partner)
     ✅ Test throttling (1000 req/hour per partner)
     ✅ Test Redis caching
     ✅ Test JWT authentication
     ✅ Target: > 80% code coverage
  
  5. Integration Tests:
     ✅ Test Partner BFF → Payment Service (WireMock)
     ✅ Test Partner BFF → Account Service (WireMock)
     ✅ Test rate limiting (simulate 101 requests in 1 minute → 429 error)
  
  6. Dockerization:
     📦 Dockerfile (multi-stage build)
     📦 docker-compose.yml (partner-bff + redis)
  
  7. Kubernetes Deployment:
     ☸ deployment.yaml (HPA: 2-10 replicas)
     ☸ service.yaml (ClusterIP)
     ☸ configmap.yaml (rate limits per partner)
     ☸ secret.yaml

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:

  1. Rate Limiting: ENFORCE 100 requests/minute per partner (use Resilience4j)
  2. Throttling: ENFORCE 1000 requests/hour per partner
  3. Comprehensive Data: Include ALL fields, nested objects, audit trail
  4. Performance: API response time < 500ms (p95) (despite large payloads)
  5. Security: JWT validation MANDATORY + partner-specific scopes
  6. Error Handling: Return 429 (Too Many Requests) when rate limit exceeded
  7. Logging: Log partner ID, rate limit usage per request
  8. Partner Configuration: Support partner-specific rate limits (Redis config)

Definition of Done:

  ✅ All 10+ REST endpoints functional
  ✅ Rate limiting working (100 req/min per partner tested)
  ✅ Throttling working (1000 req/hour per partner tested)
  ✅ Comprehensive responses (all fields included)
  ✅ API response time < 500ms (p95) (load tested)
  ✅ Redis caching working
  ✅ JWT authentication working
  ✅ Unit test coverage > 80%
  ✅ Integration tests pass
  ✅ Swagger UI accessible
  ✅ Service deploys to AKS
  ✅ HPA scaling tested (2-10 replicas)

Success Criteria:

  📊 API latency < 500ms (p95)
  📊 Rate limiting effective (0% violations)
  📊 Throttling effective (0% violations)
  📊 Uptime > 99.9% (production)

Dependencies:
  ✅ Phase 0 complete (Infrastructure, Domain Models, Event Schemas)
  ✅ Payment Initiation Service deployed
  ✅ Account Adapter Service deployed
  ✅ Audit Service deployed
  ✅ Redis cache available
```

---

### Feature 5.2: Prometheus Setup (Metrics Collection)

```yaml
Feature ID: 5.2
Feature Name: Prometheus Setup (Metrics Collection & Alerting)
Agent Name: Prometheus Agent
Phase: 5 (Infrastructure)
Estimated Time: 1.5 days
Note: This is 1 of 3 monitoring tools (Prometheus, Grafana, Jaeger) being deployed separately

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

### Feature 5.3: Grafana Dashboards (Visualization)

```yaml
Feature ID: 5.3
Feature Name: Grafana Dashboards (Metrics Visualization)
Agent Name: Grafana Agent
Phase: 5 (Infrastructure)
Estimated Time: 1.5 days
Note: This is 1 of 3 monitoring tools (Prometheus, Grafana, Jaeger) being deployed separately

Role & Expertise:
  You are a Platform Engineer with expertise in Grafana, dashboard design,
  metrics visualization, and Kubernetes monitoring.

Task:
  Deploy Grafana and create comprehensive dashboards to visualize metrics from
  Prometheus for all 20 microservices.

Context Provided:

  1. Architecture Documents:
     📄 docs/08-DEPLOYMENT-ARCHITECTURE.md (Monitoring section)
     📄 docs/11-SRE-ARCHITECTURE.md (Monitoring Golden Signals section)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 5.3)
  
  2. Grafana Deployment:
     - Helm chart for Grafana
     - Persistent storage (10 GB)
     - LoadBalancer service (port 3000)
     - OAuth 2.0 integration (Azure AD)
  
  3. Dashboard Requirements:
     - 20+ dashboards (1 per microservice + 5 system dashboards)
     - Golden Signals (Latency, Traffic, Errors, Saturation)
     - RED metrics (Rate, Errors, Duration)
     - Resource utilization (CPU, Memory, Network)
     - Business metrics (TPS, payment success rate, etc.)
  
  4. Technology Stack:
     - Grafana 10.x
     - Helm chart
     - Prometheus datasource

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Grafana Architecture Diagram
     📊 Dashboard Organization Strategy
  
  2. LLD (Low-Level Design):
     📋 Dashboard specifications (20+ dashboards)
     📋 Alert panel configurations
     📋 Variable definitions
  
  3. Implementation:
     📁 /infrastructure/grafana/
        ├─ helm-values.yaml (Grafana configuration)
        ├─ dashboards/
        │   ├─ payment-initiation-dashboard.json
        │   ├─ validation-dashboard.json
        │   ├─ account-adapter-dashboard.json
        │   ├─ ... (20+ dashboards)
        │   ├─ system-overview-dashboard.json
        │   ├─ golden-signals-dashboard.json
        │   └─ slo-dashboard.json
        ├─ datasources/
        │   └─ prometheus-datasource.yaml
        └─ README.md (dashboard guide)
  
  4. Dashboard Testing:
     ✅ Test all 20+ dashboards load < 3 seconds
     ✅ Test Prometheus datasource connection
     ✅ Test panel queries (no errors)
     ✅ Test variables (tenant filter, service filter)
     ✅ Test auto-refresh (5 seconds)
  
  5. Kubernetes Deployment:
     ☸ Helm install (Grafana chart)
     ☸ PVC for dashboard persistence
     ☸ ConfigMap for dashboard JSONs
     ☸ Secret for OAuth 2.0

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:

  1. Dashboard Count: MINIMUM 20+ dashboards (1 per microservice + 5 system)
  2. Load Time: All dashboards MUST load < 3 seconds
  3. Data Refresh: Auto-refresh every 5 seconds (real-time monitoring)
  4. Queries: NO slow queries (< 1 second per panel)
  5. Variables: Support tenant filter, service filter, time range
  6. Security: OAuth 2.0 authentication MANDATORY
  7. Persistence: Dashboard JSON persisted in Git (version control)
  8. SLO Tracking: Include SLO dashboard (p95 latency, uptime, error rate)

Definition of Done:

  ✅ Grafana deployed to AKS
  ✅ Prometheus datasource configured
  ✅ 20+ dashboards created
  ✅ All dashboards load < 3 seconds
  ✅ Auto-refresh working (5 seconds)
  ✅ OAuth 2.0 authentication working
  ✅ Dashboard JSONs persisted in Git
  ✅ SLO dashboard functional
  ✅ Golden Signals dashboard functional
  ✅ Alert panels functional

Success Criteria:

  📊 Dashboard load time < 3 seconds
  📊 Data refresh interval: 5 seconds (real-time)
  📊 Uptime > 99.9% (production)
  📊 User satisfaction > 90% (dashboard usability)

Dependencies:
  ✅ Prometheus deployed (Feature 5.2)
  ✅ All 20 microservices deployed
  ✅ AKS cluster available
```

---

### Feature 5.4: Jaeger Distributed Tracing (OpenTelemetry)

```yaml
Feature ID: 5.4
Feature Name: Jaeger Distributed Tracing (OpenTelemetry)
Agent Name: Jaeger Agent
Phase: 5 (Infrastructure)
Estimated Time: 1.5 days
Note: This is 1 of 3 monitoring tools (Prometheus, Grafana, Jaeger) being deployed separately

Role & Expertise:
  You are a Platform Engineer with expertise in Jaeger, OpenTelemetry,
  distributed tracing, and Kubernetes observability.

Task:
  Deploy Jaeger and configure OpenTelemetry instrumentation for distributed tracing
  across all 20 microservices.

Context Provided:

  1. Architecture Documents:
     📄 docs/16-DISTRIBUTED-TRACING.md (COMPLETE FILE - OpenTelemetry, Jaeger)
     📄 docs/08-DEPLOYMENT-ARCHITECTURE.md (Monitoring section)
     📄 docs/04-AI-AGENT-TASK-BREAKDOWN.md (Task 5.4)
  
  2. Jaeger Deployment:
     - Helm chart for Jaeger
     - Elasticsearch backend (trace storage)
     - Jaeger Query UI (port 16686)
     - Jaeger Collector (gRPC port 14250)
  
  3. OpenTelemetry Configuration:
     - Auto-instrumentation (Spring Boot Actuator)
     - Manual instrumentation (@WithSpan annotations)
     - Trace sampling (10% default, 100% for errors)
     - Context propagation (W3C Trace Context)
  
  4. Technology Stack:
     - Jaeger 1.x
     - OpenTelemetry Java SDK
     - Elasticsearch (trace storage, 7 days retention)
     - Helm chart

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Jaeger Architecture Diagram
     📊 Trace Flow Diagram
  
  2. LLD (Low-Level Design):
     📋 OpenTelemetry SDK configuration
     📋 Trace sampling strategy
     📋 Context propagation headers
  
  3. Implementation:
     📁 /infrastructure/jaeger/
        ├─ helm-values.yaml (Jaeger configuration)
        ├─ elasticsearch-config.yaml
        ├─ sampling-config.yaml (10% default, 100% errors)
        └─ README.md
     
     📁 /shared-libraries/tracing/
        ├─ OpenTelemetryConfig.java
        ├─ TracingInterceptor.java
        └─ SpanAttributes.java
  
  4. OpenTelemetry Integration:
     ✅ Update all 20 microservices with OpenTelemetry SDK
     ✅ Configure Jaeger exporter (gRPC endpoint)
     ✅ Add @WithSpan annotations (critical methods)
     ✅ Test trace propagation (cross-service calls)
  
  5. Testing:
     ✅ Test end-to-end tracing (payment initiation → clearing)
     ✅ Test trace sampling (10% sampled)
     ✅ Test error tracing (100% sampled)
     ✅ Test Jaeger UI (search, visualization)
     ✅ Test trace query latency (< 1 second)
  
  6. Kubernetes Deployment:
     ☸ Helm install (Jaeger chart)
     ☸ Elasticsearch StatefulSet (7 days retention)
     ☸ Jaeger Collector deployment
     ☸ Jaeger Query UI service (LoadBalancer)

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:

  1. Trace Sampling: 10% default, 100% for errors (MANDATORY)
  2. Trace Retention: 7 days in Elasticsearch
  3. Trace Query Latency: < 1 second (Jaeger UI)
  4. Context Propagation: W3C Trace Context standard (MANDATORY)
  5. Performance: OpenTelemetry SDK overhead < 5ms per request
  6. Storage: Elasticsearch storage < 100 GB (7 days)
  7. Security: Jaeger UI behind OAuth 2.0 (Azure AD)
  8. Testing: End-to-end tracing validated (all 20 services)

Definition of Done:

  ✅ Jaeger deployed to AKS
  ✅ Elasticsearch backend configured (7 days retention)
  ✅ OpenTelemetry SDK integrated (all 20 microservices)
  ✅ Trace sampling working (10% default, 100% errors)
  ✅ Context propagation working (W3C Trace Context)
  ✅ Jaeger UI accessible (OAuth 2.0)
  ✅ End-to-end tracing validated
  ✅ Trace query latency < 1 second
  ✅ Elasticsearch storage < 100 GB
  ✅ No performance impact (< 5ms overhead)

Success Criteria:

  📊 Trace query latency < 1 second
  📊 Trace sampling rate: 10% (configurable)
  📊 OpenTelemetry overhead < 5ms per request
  📊 Uptime > 99.9% (production)
  📊 Storage < 100 GB (7 days retention)

Dependencies:
  ✅ Phase 0 complete (OpenTelemetry shared library)
  ✅ All 20 microservices deployed
  ✅ Elasticsearch deployed (or Azure Cosmos DB)
  ✅ AKS cluster available
```

---

### Feature 5.5: GitOps (ArgoCD)

```yaml
Feature ID: 5.5
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

### Feature 5.6: Feature Flags (Unleash)

```yaml
Feature ID: 5.6
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

### Feature 5.7: Kubernetes Operators (14 Operators)

```yaml
Feature ID: 5.7
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

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **Custom Operators (Go Code)** (CRITICAL):
     - ✅ MUST use operator-sdk or Kubebuilder for scaffolding
     - ✅ MUST define CRDs (Custom Resource Definitions) for 4 operators
     - ✅ Follow Kubernetes naming conventions (lowercase, hyphens)
     - ✅ Implement reconciliation loop with exponential backoff
     - ❌ NEVER block reconciliation (use non-blocking operations)
  
  2. **Reconciliation Logic** (CRITICAL):
     - ✅ MUST be idempotent (safe to call multiple times)
     - ✅ Requeue after 5 minutes for periodic reconciliation
     - ✅ If reconciliation fails → log error, requeue with backoff
     - ✅ Use `ctrl.Result{RequeueAfter: 5*time.Minute}` for success
     - ✅ Limit reconciliation time (< 30 seconds)
  
  3. **CRD Validation** (CRITICAL):
     - ✅ Define OpenAPI v3 schema for CRD validation
     - ✅ Validate required fields (replicas, image, database size)
     - ✅ Validate ranges (replicas: 1-10, CPU: 100m-2000m)
     - ✅ Use kubebuilder validation tags: `+kubebuilder:validation:Minimum=1`
     - ❌ NEVER allow invalid CRDs to be created
  
  4. **Resource Management**:
     - ✅ Create Deployment, Service, HPA per PaymentService CR
     - ✅ Use owner references (for cascading deletion)
     - ✅ Apply resource quotas (CPU, memory limits)
     - ✅ If resource creation fails → update CR status with error
  
  5. **Backup Automation**:
     - ✅ Schedule daily backups (2 AM UTC)
     - ✅ Retention: 30 days (delete older backups)
     - ✅ Store backups in Azure Blob Storage
     - ✅ Validate backup integrity (test restore)
     - ✅ If backup fails → alert SRE team (PagerDuty)
  
  6. **Upgrade Automation**:
     - ✅ Support rolling upgrades (zero downtime)
     - ✅ Update pods one at a time (wait for readiness)
     - ✅ If upgrade fails → automatically rollback
     - ✅ Update CR status: `Upgrading`, `Upgraded`, `UpgradeFailed`
  
  7. **Scaling Automation**:
     - ✅ Support HPA (Horizontal Pod Autoscaler) creation
     - ✅ Min replicas: 3, max replicas: 10
     - ✅ Target CPU: 70%
     - ✅ Update CR status with actual replica count
  
  8. **Status Updates** (CRITICAL):
     - ✅ MUST update CR status after every operation
     - ✅ Include: observedGeneration, replicas, readyReplicas, conditions
     - ✅ Use status conditions (type: Ready, status: True/False, reason)
     - ✅ If status update fails → log error, continue reconciliation
  
  9. **RBAC** (CRITICAL):
     - ✅ Define minimal RBAC permissions (least privilege)
     - ✅ Use ServiceAccount per operator
     - ✅ Limit to specific namespaces (not cluster-wide unless required)
     - ❌ NEVER use cluster-admin role
  
  10. **Monitoring**:
      - ✅ Expose operator metrics (reconciliations_total, errors_total)
      - ✅ Track CRD count, reconciliation duration
      - ✅ Integrate with Prometheus (operator metrics endpoint)
  
  11. **Testing**:
      - ✅ Write unit tests for reconciliation logic (Go test)
      - ✅ Write integration tests using envtest (Kubebuilder)
      - ✅ Test CRD create, update, delete operations
      - ✅ Test operator failure recovery
  
  12. **Error Handling**:
      - ✅ If Kubernetes API call fails → return error, requeue
      - ✅ Log all errors with CR name, namespace
      - ✅ Update CR status with error message
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

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

## Phase 6: Testing (5 Features)

### Feature 6.1: End-to-End (E2E) Testing

```yaml
Feature ID: 6.1
Feature Name: End-to-End (E2E) Testing Framework
Agent Name: E2E Testing Agent
Phase: 6 (Testing)
Estimated Time: 4 days

Role & Expertise:
  You are a QA Automation Engineer with expertise in E2E testing, Cucumber,
  RestAssured, Selenium, API testing, and behavior-driven development (BDD).

Task:
  Build a comprehensive E2E testing framework that validates complete payment
  flows across all 20 microservices, from payment initiation through clearing
  and settlement, including all edge cases and failure scenarios.

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **Test Coverage** (CRITICAL):
     - ✅ MUST cover all 5 payment types (EFT, RTC, PayShap, SWIFT, Batch)
     - ✅ MUST test happy path AND failure scenarios for each type
     - ✅ MUST test multi-tenant isolation (no cross-tenant data access)
     - ✅ Minimum 50 E2E scenarios across all payment flows
     - ✅ Critical path coverage: > 95%
  
  2. **Test Data Management** (CRITICAL):
     - ✅ Use dedicated test tenants (TENANT-TEST-001, TENANT-TEST-002)
     - ✅ Use test accounts with known balances (reset before each test)
     - ✅ Clean up test data after each test run
     - ❌ NEVER use production data in tests
     - ✅ Use test data builders for readability
  
  3. **Test Isolation**:
     - ✅ Each test MUST be independent (no shared state)
     - ✅ Use `@BeforeEach` to set up test data
     - ✅ Use `@AfterEach` to clean up test data
     - ❌ NEVER rely on test execution order
     - ✅ Use unique payment IDs per test (avoid conflicts)
  
  4. **Async Testing**:
     - ✅ Use Awaitility for async assertions (max 30 seconds wait)
     - ✅ Poll for payment status every 500ms
     - ✅ If timeout → fail test with clear message
     - ✅ Test event propagation (PaymentInitiated → Validated → Routed → Submitted)
  
  5. **Failure Scenario Testing**:
     - ✅ MUST test insufficient balance (account adapter returns error)
     - ✅ MUST test limit exceeded (limit service rejects)
     - ✅ MUST test fraud rejection (high-risk score)
     - ✅ MUST test clearing timeout (clearing adapter timeout)
     - ✅ MUST test compensation flow (Saga rollback)
  
  6. **Mock External Systems**:
     - ✅ Use WireMock for external systems (core banking, fraud API)
     - ✅ Mock different responses (success, failure, timeout)
     - ✅ Simulate network delays (latency injection)
     - ❌ NEVER call real external systems in E2E tests
  
  7. **Test Execution**:
     - ✅ Target: < 30 minutes total execution time
     - ✅ Run tests in parallel (5 threads minimum)
     - ✅ Generate Allure reports (HTML, screenshots)
     - ✅ Integrate with CI/CD (Azure Pipelines, fail build on test failure)
  
  8. **Flaky Tests**:
     - ❌ ZERO flaky tests allowed (tests must be deterministic)
     - ✅ If test fails intermittently → investigate and fix (don't ignore)
     - ✅ Use retry only for infrastructure issues (not logic issues)
  
  9. **Logging**:
     - ✅ Log test name, duration, result (pass/fail)
     - ✅ Log payment IDs for failed tests (for debugging)
     - ✅ Include screenshots for UI tests (if applicable)
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

Context Provided:

  1. Architecture Documents:
     📄 docs/23-TESTING-ARCHITECTURE.md (COMPLETE FILE - 2,000 lines)
        - Test pyramid (80% unit, 15% integration, 5% E2E)
        - E2E testing strategy
        - Test automation tools
        - BDD with Cucumber
        - Contract testing
        - Test data management
     
     📄 docs/34-FEATURE-BREAKDOWN-TREE.md (Phase 6)
        - E2E test scenarios
     
     📄 docs/00-ARCHITECTURE-OVERVIEW.md
        - Complete payment flow (20 services)
  
  2. E2E Test Scenarios:
     
     **Happy Path Scenarios**:
     1. EFT Payment (end-to-end)
     2. RTC Payment (real-time clearing)
     3. PayShap Payment (instant P2P)
     4. SWIFT Payment (international)
     5. Batch Payment Processing
     
     **Failure Scenarios**:
     6. Insufficient balance (account adapter)
     7. Limit exceeded (limit service)
     8. Fraud rejection (fraud service)
     9. Clearing timeout (clearing adapter)
     10. Compensation flow (Saga rollback)
  
  3. Cucumber BDD Example:
     ```gherkin
     Feature: EFT Payment Processing
       As a customer
       I want to initiate an EFT payment
       So that I can transfer money to another account
     
     Background:
       Given the payment system is up and running
       And tenant "TENANT-001" is onboarded
       And user "john.doe@example.com" is authenticated
       And account "ACC-12345" has balance R10,000
     
     Scenario: Successful EFT payment
       Given I am on the payment initiation page
       When I enter the following payment details:
         | Field                | Value          |
         | Payment Type         | EFT            |
         | Debtor Account       | ACC-12345      |
         | Creditor Account     | ACC-67890      |
         | Amount               | R500.00        |
         | Reference            | Invoice 001    |
       And I submit the payment
       Then I should see payment status "PROCESSING"
       And the payment should be validated
       And the account balance should be reserved
       And the limit should be checked
       And fraud scoring should return "LOW_RISK"
       And the payment should be routed to BankservAfrica
       And the clearing adapter should submit the payment
       And the payment status should be "SUBMITTED"
       And I should receive a notification "Payment submitted successfully"
       And the audit log should contain the payment record
     
     Scenario: Payment fails due to insufficient balance
       Given account "ACC-12345" has balance R100
       When I submit a payment of R500
       Then I should see error "INSUFFICIENT_BALANCE"
       And the payment status should be "FAILED"
       And the account balance should not be debited
       And I should receive a notification "Payment failed: Insufficient balance"
     
     Scenario: Payment fails due to fraud detection
       Given I submit a suspicious payment pattern
       When fraud scoring returns "HIGH_RISK"
       Then the payment should be rejected
       And the payment status should be "FRAUD_REJECTED"
       And I should receive a notification "Payment blocked due to fraud"
       And the compliance team should be alerted
     ```
  
  4. RestAssured API Test Example:
     ```java
     @Test
     public void testSuccessfulEftPayment() {
         // 1. Initiate payment
         PaymentRequest request = PaymentRequest.builder()
             .paymentType("EFT")
             .debtorAccountId("ACC-12345")
             .creditorAccountId("ACC-67890")
             .amount(new BigDecimal("500.00"))
             .currency("ZAR")
             .reference("Invoice 001")
             .build();
         
         String paymentId = given()
             .header("Authorization", "Bearer " + authToken)
             .header("X-Tenant-ID", "TENANT-001")
             .contentType(ContentType.JSON)
             .body(request)
         .when()
             .post("/api/v1/payments")
         .then()
             .statusCode(201)
             .body("status", equalTo("PROCESSING"))
             .extract()
             .path("paymentId");
         
         // 2. Wait for validation
         await().atMost(5, SECONDS).until(() -> 
             getPaymentStatus(paymentId).equals("VALIDATED")
         );
         
         // 3. Verify fraud check
         given()
             .header("Authorization", "Bearer " + authToken)
         .when()
             .get("/api/v1/payments/" + paymentId + "/fraud-score")
         .then()
             .statusCode(200)
             .body("riskLevel", equalTo("LOW_RISK"));
         
         // 4. Verify clearing submission
         await().atMost(10, SECONDS).until(() -> 
             getPaymentStatus(paymentId).equals("SUBMITTED")
         );
         
         // 5. Verify audit log
         given()
             .header("Authorization", "Bearer " + authToken)
         .when()
             .get("/api/v1/audit/payments/" + paymentId)
         .then()
             .statusCode(200)
             .body("events", hasSize(greaterThan(5)));
     }
     ```
  
  5. Technology Stack:
     - Cucumber 7.14+ (BDD)
     - RestAssured 5.3+ (API testing)
     - JUnit 5 (test runner)
     - Awaitility 4.2+ (async assertions)
     - TestContainers 1.19+ (test infrastructure)
     - Allure 2.24+ (test reporting)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 E2E Test Architecture
        - Test execution flow
        - Test data management
        - Test environment setup
        - CI/CD integration
  
  2. LLD (Low-Level Design):
     📋 Test Scenarios (50+ scenarios)
        - Happy path (10 scenarios)
        - Failure scenarios (20 scenarios)
        - Edge cases (10 scenarios)
        - Performance scenarios (10 scenarios)
     
     📋 Test Data Strategy
        - Test tenants, users, accounts
        - Payment test data
        - Clearing system mocks
  
  3. Implementation:
     📁 /e2e-tests/
        ├─ src/test/resources/features/
        │   ├─ payment-initiation.feature
        │   ├─ eft-payment.feature
        │   ├─ rtc-payment.feature
        │   ├─ payshap-payment.feature
        │   ├─ swift-payment.feature
        │   ├─ batch-payment.feature
        │   ├─ fraud-detection.feature
        │   ├─ limit-checking.feature
        │   ├─ saga-compensation.feature
        │   └─ multi-tenant.feature
        ├─ src/test/java/steps/
        │   ├─ PaymentSteps.java
        │   ├─ ValidationSteps.java
        │   ├─ FraudSteps.java
        │   ├─ ClearingSteps.java
        │   └─ AuditSteps.java
        ├─ src/test/java/api/
        │   ├─ PaymentApiTest.java
        │   ├─ ValidationApiTest.java
        │   ├─ AccountApiTest.java
        │   └─ TenantApiTest.java
        ├─ src/test/java/config/
        │   ├─ TestConfig.java
        │   ├─ TestDataManager.java
        │   └─ TestContainersConfig.java
        ├─ src/test/java/utils/
        │   ├─ AuthHelper.java
        │   ├─ PaymentHelper.java
        │   └─ WaitHelper.java
        └─ README.md
  
  4. Testing:
     ✅ All 50+ E2E scenarios pass
     ✅ Test execution time < 30 minutes
     ✅ Test parallelization working (5 threads)
     ✅ Test reports generated (Allure)
     ✅ CI/CD integration (Azure Pipelines)
  
  5. Documentation:
     📄 README.md
        - E2E testing overview
        - How to run tests
        - Test data setup
     
     📄 E2E-TEST-SCENARIOS.md
        - Complete scenario catalog
        - Expected results
        - Test data requirements
     
     📄 TROUBLESHOOTING.md
        - Common test failures
        - How to debug

Success Criteria:
  ✅ 50+ E2E scenarios implemented
  ✅ All scenarios pass in CI/CD
  ✅ Test coverage > 95% (critical paths)
  ✅ Test execution < 30 minutes
  ✅ Zero flaky tests

Context Sufficiency: ✅ SUFFICIENT
  - Complete testing spec in docs/23 (2,000 lines)
  - Payment flow documented
  - All microservices APIs known

Dependencies:
  ✅ All 20 microservices deployed - READY
  ✅ Test environment (staging) - READY
  ✅ Test data - READY
```

---

### Feature 6.2: Load Testing

```yaml
Feature ID: 6.2
Feature Name: Load & Performance Testing
Agent Name: Performance Testing Agent
Phase: 6 (Testing)
Estimated Time: 3 days

Role & Expertise:
  You are a Performance Engineer with expertise in load testing, Gatling,
  JMeter, performance tuning, bottleneck analysis, and scalability testing.

Task:
  Build a comprehensive load testing framework using Gatling to validate the
  Payments Engine can handle 1,000 TPS (transactions per second) with p95
  latency < 3 seconds, and identify performance bottlenecks.

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **Performance SLOs** (CRITICAL):
     - ✅ MUST validate: 1,000 TPS sustained for 5 minutes
     - ✅ MUST validate: p95 latency < 3 seconds
     - ✅ MUST validate: p99 latency < 5 seconds
     - ✅ MUST validate: Error rate < 1%
     - ✅ If ANY SLO violated → test FAILS
  
  2. **Load Test Scenarios**:
     - ✅ MUST implement all 5 scenarios (sustained, peak, spike, endurance, stress)
     - ✅ Ramp up gradually (not instant 1,000 TPS)
     - ✅ Use realistic payment data (CSV with 10K records)
     - ✅ Mix payment types (70% EFT, 20% RTC, 10% PayShap)
  
  3. **Resource Monitoring**:
     - ✅ Monitor CPU, memory, disk during load test
     - ✅ Track database connection pool usage (< 80%)
     - ✅ Track message bus queue depth
     - ✅ Identify bottlenecks (slowest service, slowest DB query)
  
  4. **Bottleneck Analysis** (CRITICAL):
     - ✅ MUST identify top 5 slowest API endpoints
     - ✅ MUST identify top 5 slowest database queries
     - ✅ MUST document bottlenecks in PERFORMANCE-REPORT.md
     - ✅ Provide tuning recommendations (JVM, DB, HPA)
  
  5. **HPA Validation**:
     - ✅ Verify Horizontal Pod Autoscaler scales pods under load
     - ✅ Target: CPU < 70% per pod (after scaling)
     - ✅ Verify scale-up (2,000 TPS → more pods)
     - ✅ Verify scale-down (load drops → fewer pods)
  
  6. **Grafana Dashboards**:
     - ✅ Create Gatling dashboard in Grafana (real-time metrics)
     - ✅ Track: TPS, response time (p50/p95/p99), error rate
     - ✅ Correlate with system metrics (CPU, memory, DB)
  
  7. **Test Environment**:
     - ✅ Use staging environment (prod-like configuration)
     - ❌ NEVER run load tests against production
     - ✅ Ensure staging has same resources as prod (pods, DB)
  
  8. **Error Handling**:
     - ✅ If error rate > 1% → FAIL test immediately
     - ✅ Capture error details (status code, error message)
     - ✅ Include in PERFORMANCE-REPORT.md
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

Context Provided:

  1. Architecture Documents:
     📄 docs/23-TESTING-ARCHITECTURE.md (Performance Testing section)
        - Load testing strategy
        - Gatling framework
        - Performance SLOs
        - Bottleneck analysis
     
     📄 docs/24-SRE-ARCHITECTURE.md (SLOs)
        - Target: 1,000 TPS
        - Latency: p95 < 3s, p99 < 5s
        - Error rate: < 1%
        - Availability: 99.95%
  
  2. Performance Requirements:
     
     **Load Targets**:
     - Sustained load: 500 TPS (8 hours)
     - Peak load: 1,000 TPS (1 hour)
     - Spike load: 2,000 TPS (5 minutes)
     
     **Latency Targets**:
     - p50: < 1 second
     - p95: < 3 seconds
     - p99: < 5 seconds
     
     **Resource Limits**:
     - CPU: < 70% per pod
     - Memory: < 80% per pod
     - Database connections: < 80% pool
  
  3. Gatling Load Test Example:
     ```scala
     class PaymentLoadTest extends Simulation {
       
       val httpProtocol = http
         .baseUrl("https://api-staging.payments.example.com")
         .header("Authorization", "Bearer ${authToken}")
         .header("X-Tenant-ID", "TENANT-001")
         .acceptHeader("application/json")
       
       val feeder = csv("payment-data.csv").circular
       
       val eftPayment = scenario("EFT Payment")
         .feed(feeder)
         .exec(
           http("Initiate EFT Payment")
             .post("/api/v1/payments")
             .body(ElFileBody("eft-payment-template.json")).asJson
             .check(status.is(201))
             .check(jsonPath("$.paymentId").saveAs("paymentId"))
         )
         .pause(1)
         .exec(
           http("Check Payment Status")
             .get("/api/v1/payments/${paymentId}")
             .check(status.is(200))
             .check(jsonPath("$.status").in("PROCESSING", "VALIDATED", "SUBMITTED"))
         )
       
       val rtcPayment = scenario("RTC Payment")
         .feed(feeder)
         .exec(
           http("Initiate RTC Payment")
             .post("/api/v1/payments")
             .body(ElFileBody("rtc-payment-template.json")).asJson
             .check(status.is(201))
         )
       
       setUp(
         eftPayment.inject(
           nothingFor(5.seconds),
           rampUsersPerSec(10).to(500).during(2.minutes),  // Ramp up
           constantUsersPerSec(500).during(10.minutes),     // Sustained
           rampUsersPerSec(500).to(1000).during(1.minute), // Peak
           constantUsersPerSec(1000).during(5.minutes)      // Spike
         ),
         rtcPayment.inject(
           nothingFor(5.seconds),
           constantUsersPerSec(100).during(15.minutes)
         )
       ).protocols(httpProtocol)
         .assertions(
           global.responseTime.percentile3.lt(3000),  // p95 < 3s
           global.responseTime.percentile4.lt(5000),  // p99 < 5s
           global.successfulRequests.percent.gt(99)   // Success > 99%
         )
     }
     ```
  
  4. Load Test Scenarios:
     
     **Scenario 1: Sustained Load** (10 minutes)
     - 500 TPS (EFT payments)
     - Validate: p95 < 3s, error rate < 1%
     
     **Scenario 2: Peak Load** (5 minutes)
     - 1,000 TPS (mixed: 70% EFT, 20% RTC, 10% PayShap)
     - Validate: p95 < 3s, error rate < 1%
     
     **Scenario 3: Spike Test** (5 minutes)
     - 0 → 2,000 TPS (instant spike)
     - Validate: system recovers, HPA scales pods
     
     **Scenario 4: Endurance Test** (8 hours)
     - 500 TPS (constant)
     - Validate: no memory leaks, no degradation
     
     **Scenario 5: Stress Test** (find breaking point)
     - Ramp up until failure
     - Identify max TPS (target: > 1,500 TPS)
  
  5. Technology Stack:
     - Gatling 3.9+ (load testing)
     - Grafana (real-time monitoring)
     - Prometheus (metrics collection)
     - InfluxDB (Gatling metrics storage)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Load Testing Architecture
        - Test scenarios
        - Load injection strategy
        - Monitoring integration
        - Bottleneck analysis
  
  2. LLD (Low-Level Design):
     📋 Load Test Scenarios (5 scenarios)
        - Sustained, peak, spike, endurance, stress
     
     📋 Performance SLOs
        - TPS targets, latency targets, error rates
  
  3. Implementation:
     📁 /load-tests/
        ├─ simulations/
        │   ├─ SustainedLoadTest.scala
        │   ├─ PeakLoadTest.scala
        │   ├─ SpikeTest.scala
        │   ├─ EnduranceTest.scala
        │   └─ StressTest.scala
        ├─ data/
        │   ├─ payment-data.csv (10K records)
        │   ├─ eft-payment-template.json
        │   ├─ rtc-payment-template.json
        │   └─ payshap-payment-template.json
        ├─ dashboards/
        │   ├─ gatling-dashboard.json (Grafana)
        │   └─ performance-slo-dashboard.json
        ├─ scripts/
        │   ├─ run-load-test.sh
        │   └─ analyze-results.sh
        └─ README.md
  
  4. Testing:
     ✅ Sustained load test passes (500 TPS, 10 min)
     ✅ Peak load test passes (1,000 TPS, 5 min)
     ✅ Spike test passes (2,000 TPS, 5 min)
     ✅ Endurance test passes (500 TPS, 8 hours)
     ✅ Stress test identifies max TPS (> 1,500)
  
  5. Documentation:
     📄 README.md
        - Load testing overview
        - How to run tests
        - How to interpret results
     
     📄 PERFORMANCE-REPORT.md
        - Test results (TPS, latency, errors)
        - Bottlenecks identified
        - Recommendations
     
     📄 TUNING-GUIDE.md
        - JVM tuning
        - Database tuning
        - HPA tuning

Success Criteria:
  ✅ 500 TPS sustained: p95 < 3s, error < 1%
  ✅ 1,000 TPS peak: p95 < 3s, error < 1%
  ✅ 2,000 TPS spike: system recovers
  ✅ 8-hour endurance: no degradation
  ✅ Max TPS > 1,500

Context Sufficiency: ✅ SUFFICIENT
  - Complete performance spec in docs/23, docs/24
  - SLOs defined (TPS, latency, errors)
  - API endpoints known

Dependencies:
  ✅ Staging environment (prod-like) - READY
  ✅ Monitoring (Prometheus, Grafana) - READY
  ✅ HPA configured - READY
```

---

### Feature 6.3: Security Testing

```yaml
Feature ID: 6.3
Feature Name: Security Testing (SAST, DAST, Penetration)
Agent Name: Security Testing Agent
Phase: 6 (Testing)
Estimated Time: 4 days

Role & Expertise:
  You are a Security Engineer with expertise in SAST, DAST, penetration testing,
  OWASP Top 10, vulnerability scanning, and compliance validation (PCI-DSS).

Task:
  Build a comprehensive security testing framework covering SAST (static),
  DAST (dynamic), container scanning, secrets scanning, and penetration testing
  to ensure the Payments Engine is secure and compliant with PCI-DSS.

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **SAST (Static Analysis)** (CRITICAL):
     - ✅ MUST achieve SonarQube Security Rating: A (no vulnerabilities)
     - ✅ MUST have 0 CRITICAL or HIGH vulnerabilities
     - ✅ Code coverage: > 80%
     - ✅ MUST fix all bugs before deployment
     - ❌ NEVER ignore SonarQube quality gate failures
  
  2. **DAST (Dynamic Analysis)** (CRITICAL):
     - ✅ MUST run OWASP ZAP full scan (active attacks)
     - ✅ MUST have 0 CRITICAL vulnerabilities
     - ✅ MUST have 0 HIGH vulnerabilities
     - ✅ Test all OWASP Top 10 vulnerabilities
     - ✅ If vulnerability found → document, remediate, re-scan
  
  3. **Container Security** (CRITICAL):
     - ✅ MUST run Trivy scan on ALL container images
     - ✅ MUST have 0 CRITICAL vulnerabilities
     - ✅ Use minimal base images (Alpine, Distroless)
     - ✅ Scan for: OS vulnerabilities, library vulnerabilities, misconfigurations
     - ❌ NEVER deploy images with CRITICAL vulnerabilities
  
  4. **Secrets Scanning** (CRITICAL):
     - ✅ MUST run Gitleaks scan on entire codebase
     - ✅ MUST have 0 secrets exposed (passwords, API keys, tokens)
     - ✅ Scan: code, config files, environment variables, Docker files
     - ✅ If secret found → IMMEDIATELY rotate, remove from Git history
     - ❌ NEVER commit secrets to Git
  
  5. **OWASP Top 10 Testing** (CRITICAL):
     - ✅ MUST test all 10 OWASP categories:
       1. Broken Access Control → test RBAC, tenant isolation
       2. Cryptographic Failures → test encryption, TLS
       3. Injection → test SQL injection, XSS, XXE
       4. Insecure Design → test business logic flaws
       5. Security Misconfiguration → test default passwords, exposed endpoints
       6. Vulnerable Components → test outdated libraries
       7. Identification/Authentication → test JWT, OAuth
       8. Software/Data Integrity → test dependency integrity
       9. Security Logging → test audit trail completeness
       10. SSRF → test external URL validation
  
  6. **Authentication Testing**:
     - ✅ Test missing JWT → 401 Unauthorized
     - ✅ Test invalid JWT → 401 Unauthorized
     - ✅ Test expired JWT → 401 Unauthorized
     - ✅ Test token without required scopes → 403 Forbidden
  
  7. **Authorization Testing**:
     - ✅ Test cross-tenant access → 403 Forbidden
     - ✅ Test RBAC: user without role → 403 Forbidden
     - ✅ Test privilege escalation attempts
  
  8. **Injection Testing**:
     - ✅ Test SQL injection in all input fields
     - ✅ Test XSS in payment reference/description
     - ✅ Test XXE in XML payment files (SWIFT, Batch)
     - ✅ Test command injection in SFTP paths
  
  9. **PCI-DSS Compliance** (CRITICAL):
     - ✅ Validate Requirement 6.5.1-6.5.10 (secure coding)
     - ✅ Validate Requirement 11.3 (penetration testing)
     - ✅ Document compliance evidence in PCI-DSS-COMPLIANCE.md
  
  10. **Reporting**:
      - ✅ Generate comprehensive SECURITY-TEST-REPORT.md
      - ✅ Include: SAST results, DAST results, vulnerabilities found, remediation plan
      - ✅ Assign severity (CRITICAL, HIGH, MEDIUM, LOW)
      - ✅ Provide fix recommendations
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

Context Provided:

  1. Architecture Documents:
     📄 docs/21-SECURITY-ARCHITECTURE.md (COMPLETE FILE - 3,500 lines)
        - Zero-Trust model
        - Defense-in-Depth (7 layers)
        - Security testing requirements
        - OWASP Top 10 mitigations
        - PCI-DSS compliance
     
     📄 docs/23-TESTING-ARCHITECTURE.md (Security Testing section)
        - SAST (SonarQube)
        - DAST (OWASP ZAP)
        - Container scanning (Trivy)
        - Secrets scanning (Gitleaks)
  
  2. Security Testing Scope:
     
     **SAST (Static Application Security Testing)**:
     - Code quality: SonarQube
     - Security vulnerabilities: Semgrep
     - Dependency scanning: OWASP Dependency-Check
     - Secrets scanning: Gitleaks, Trufflehog
     
     **DAST (Dynamic Application Security Testing)**:
     - OWASP ZAP (automated scan)
     - API security testing
     - Authentication/authorization bypass
     - Injection attacks (SQL, XSS, XXE)
     
     **Container Security**:
     - Trivy (image scanning)
     - Grype (vulnerability scanning)
     - Base image vulnerabilities
     
     **Penetration Testing**:
     - Manual penetration testing
     - OWASP Top 10 validation
     - Business logic flaws
  
  3. SonarQube Quality Gate:
     ```yaml
     sonar-project.properties:
       sonar.projectKey=payments-engine
       sonar.sources=src/main/java
       sonar.tests=src/test/java
       sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
       
       # Quality Gate Thresholds
       sonar.qualitygate.wait=true
       sonar.coverage.threshold=80
       sonar.security.rating=A
       sonar.bugs.threshold=0
       sonar.vulnerabilities.threshold=0
       sonar.code_smells.threshold=10
     ```
  
  4. OWASP ZAP Scan:
     ```bash
     # Passive scan (no attacks)
     docker run -t owasp/zap2docker-stable zap-baseline.py \
       -t https://api-staging.payments.example.com \
       -r zap-baseline-report.html
     
     # Active scan (with attacks)
     docker run -t owasp/zap2docker-stable zap-full-scan.py \
       -t https://api-staging.payments.example.com \
       -r zap-full-report.html \
       -z "-config api.key=${ZAP_API_KEY}"
     
     # API scan (OpenAPI spec)
     docker run -t owasp/zap2docker-stable zap-api-scan.py \
       -t https://api-staging.payments.example.com/v3/api-docs \
       -f openapi \
       -r zap-api-report.html
     ```
  
  5. Trivy Container Scan:
     ```bash
     # Scan Docker image
     trivy image --severity HIGH,CRITICAL \
       payments/payment-initiation-service:v1.0.0
     
     # Scan with exit code (fail on HIGH/CRITICAL)
     trivy image --exit-code 1 --severity CRITICAL \
       payments/payment-initiation-service:v1.0.0
     
     # Generate report
     trivy image --format json --output trivy-report.json \
       payments/payment-initiation-service:v1.0.0
     ```
  
  6. Security Test Scenarios:
     
     **Authentication & Authorization**:
     - Missing JWT token → 401 Unauthorized
     - Invalid JWT token → 401 Unauthorized
     - Expired JWT token → 401 Unauthorized
     - Missing tenant header → 403 Forbidden
     - Invalid tenant → 403 Forbidden
     - RBAC: user without permission → 403 Forbidden
     
     **Injection Attacks**:
     - SQL injection in payment reference
     - XSS in payment description
     - XXE in XML payment file (SWIFT)
     
     **Sensitive Data Exposure**:
     - PII not encrypted in transit (HTTPS required)
     - Account numbers not masked in logs
     - JWT contains sensitive data
     
     **Rate Limiting**:
     - 100 requests per minute → throttled
     - DDoS simulation → blocked by Azure Application Gateway
  
  7. Technology Stack:
     - SonarQube 10.2+ (SAST)
     - OWASP ZAP 2.14+ (DAST)
     - Trivy 0.45+ (container scanning)
     - Gitleaks 8.18+ (secrets scanning)
     - Semgrep 1.45+ (code scanning)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Security Testing Architecture
        - SAST, DAST, container, penetration
        - CI/CD integration
        - Vulnerability management
  
  2. LLD (Low-Level Design):
     📋 Security Test Scenarios (100+ tests)
        - Authentication (20 tests)
        - Authorization (20 tests)
        - Injection (20 tests)
        - Sensitive data (20 tests)
        - Rate limiting (10 tests)
        - OWASP Top 10 (10 tests)
  
  3. Implementation:
     📁 /security-tests/
        ├─ sast/
        │   ├─ sonar-project.properties
        │   ├─ semgrep-rules.yaml
        │   └─ dependency-check.sh
        ├─ dast/
        │   ├─ zap-baseline-scan.sh
        │   ├─ zap-full-scan.sh
        │   ├─ zap-api-scan.sh
        │   └─ zap-rules.conf
        ├─ container/
        │   ├─ trivy-scan.sh
        │   ├─ grype-scan.sh
        │   └─ allowed-vulnerabilities.yaml
        ├─ secrets/
        │   ├─ gitleaks.toml
        │   └─ trufflehog-scan.sh
        ├─ penetration/
        │   ├─ auth-tests.py
        │   ├─ injection-tests.py
        │   ├─ sensitive-data-tests.py
        │   └─ rate-limiting-tests.py
        └─ README.md
  
  4. Testing:
     ✅ SAST: SonarQube Quality Gate passes (A rating)
     ✅ DAST: OWASP ZAP scan (0 HIGH/CRITICAL)
     ✅ Container: Trivy scan (0 CRITICAL)
     ✅ Secrets: Gitleaks scan (0 secrets found)
     ✅ Penetration: All OWASP Top 10 tests pass
  
  5. Documentation:
     📄 README.md
        - Security testing overview
        - How to run scans
        - How to interpret results
     
     📄 SECURITY-TEST-REPORT.md
        - SAST results
        - DAST results
        - Vulnerabilities found
        - Remediation plan
     
     📄 PCI-DSS-COMPLIANCE.md
        - PCI-DSS requirements
        - Compliance validation
        - Audit evidence

Success Criteria:
  ✅ SonarQube: Security rating A, 0 vulnerabilities
  ✅ OWASP ZAP: 0 HIGH/CRITICAL vulnerabilities
  ✅ Trivy: 0 CRITICAL vulnerabilities
  ✅ Gitleaks: 0 secrets exposed
  ✅ Penetration: All OWASP Top 10 tests pass

Context Sufficiency: ✅ SUFFICIENT
  - Complete security spec in docs/21 (3,500 lines)
  - OWASP Top 10 mitigations documented
  - PCI-DSS requirements known

Dependencies:
  ✅ SonarQube server - READY
  ✅ OWASP ZAP - READY
  ✅ Trivy, Gitleaks - READY
```

---

### Feature 6.4: Compliance Testing

```yaml
Feature ID: 6.4
Feature Name: Compliance Testing (POPIA, FICA, PCI-DSS, SARB)
Agent Name: Compliance Testing Agent
Phase: 6 (Testing)
Estimated Time: 3 days

Role & Expertise:
  You are a Compliance Specialist with expertise in South African regulations
  (POPIA, FICA, SARB), PCI-DSS, audit trails, data residency, and compliance
  validation testing.

Task:
  Build a comprehensive compliance testing framework that validates the Payments
  Engine adheres to POPIA (data protection), FICA (financial intelligence), 
  PCI-DSS (card security), and SARB (central bank) regulations.

⚠️ SPECIFIC GUARDRAILS FOR THIS FEATURE:
  
  1. **POPIA Compliance** (CRITICAL - LEGAL REQUIREMENT):
     - ✅ MUST verify PII encrypted at rest (email, ID numbers)
     - ✅ MUST verify PII masked in logs (account numbers: `ACC-****7890`)
     - ✅ MUST verify consent management (explicit consent recorded)
     - ✅ MUST verify data retention (7 years for financial records)
     - ✅ MUST verify right to erasure (delete user data on request)
     - ❌ If ANY POPIA test fails → BLOCK production deployment
  
  2. **FICA Compliance** (CRITICAL - LEGAL REQUIREMENT):
     - ✅ MUST verify KYC (Know Your Customer) validation
     - ✅ MUST verify sanctions screening (SWIFT payments)
     - ✅ MUST verify suspicious transaction reporting (STR)
     - ✅ MUST verify record keeping (5 years minimum)
     - ✅ If sanctions match found → MUST block payment, alert compliance team
     - ❌ If ANY FICA test fails → BLOCK production deployment
  
  3. **PCI-DSS Compliance** (CRITICAL - LEGAL REQUIREMENT):
     - ✅ MUST verify Requirement 3: Cardholder data encrypted
     - ✅ MUST verify Requirement 4: TLS 1.2+ for transmission
     - ✅ MUST verify Requirement 6: Secure code (SAST/DAST passed)
     - ✅ MUST verify Requirement 8: Unique IDs (RBAC implemented)
     - ✅ MUST verify Requirement 10: All access logged (audit trail)
     - ✅ MUST verify Requirement 11: Security testing (quarterly)
     - ❌ If ANY PCI-DSS test fails → BLOCK production deployment
  
  4. **SARB Compliance** (CRITICAL - LEGAL REQUIREMENT):
     - ✅ MUST verify settlement reporting (real-time)
     - ✅ MUST verify liquidity management
     - ✅ MUST verify clearing system compliance
     - ✅ If reporting fails → alert finance team
  
  5. **Test Evidence** (CRITICAL):
     - ✅ Capture test results (pass/fail) with screenshots
     - ✅ Generate COMPLIANCE-REPORT.md with evidence
     - ✅ Generate AUDIT-EVIDENCE.md for auditors
     - ✅ Include: test name, expected result, actual result, timestamp
     - ✅ Store evidence for 3 years (audit requirement)
  
  6. **Data Encryption Testing**:
     - ✅ Query database directly (bypass application)
     - ✅ Verify email not in plaintext (starts with `ENC:`)
     - ✅ Verify ID number hashed (SHA-256, 64 characters)
     - ✅ Use TestContainers for database access
  
  7. **Sanctions Screening Testing**:
     - ✅ Test with known sanctioned entities (test data)
     - ✅ Verify payment blocked with status `SANCTIONS_BLOCKED`
     - ✅ Verify compliance alert raised
     - ✅ Test all sanctioned countries (Iran, North Korea, Syria, etc.)
  
  8. **Audit Trail Testing**:
     - ✅ Verify ALL payment operations logged
     - ✅ Verify log contains: user ID, tenant ID, timestamp, IP, user agent
     - ✅ Verify log immutability (cannot be modified)
     - ✅ Query CosmosDB directly to verify audit events
  
  9. **Test Reporting**:
     - ✅ Generate compliance report: POPIA ✅, FICA ✅, PCI-DSS ✅, SARB ✅
     - ✅ If ANY regulation fails → mark as ❌ FAIL
     - ✅ Document non-compliance in detail
  
  10. **Regulatory Updates**:
      - ✅ Review tests quarterly (regulations change)
      - ✅ Update tests when regulations updated
      - ✅ Maintain compliance test suite (living document)
  
  These are IN ADDITION to the Generic Coding Guardrails.
  ALL guardrails MUST be followed.

Context Provided:

  1. Architecture Documents:
     📄 docs/21-SECURITY-ARCHITECTURE.md (Compliance section)
        - POPIA (Protection of Personal Information Act)
        - FICA (Financial Intelligence Centre Act)
        - PCI-DSS (Payment Card Industry Data Security Standard)
        - SARB (South African Reserve Bank)
     
     📄 docs/23-TESTING-ARCHITECTURE.md (Compliance Testing section)
        - Compliance test automation
        - Audit trail validation
  
  2. Compliance Requirements:
     
     **POPIA (Data Protection)**:
     - Consent management (explicit consent for PII)
     - Data minimization (collect only required data)
     - Right to erasure (GDPR-like)
     - Data breach notification (< 72 hours)
     - Data encryption (at rest, in transit)
     - Data retention (7 years for financial records)
     
     **FICA (Anti-Money Laundering)**:
     - Customer due diligence (KYC)
     - Suspicious transaction reporting (STR)
     - Record keeping (5 years)
     - Sanctions screening (SWIFT mandatory)
     
     **PCI-DSS (Card Security)**:
     - Requirement 3: Protect stored cardholder data
     - Requirement 4: Encrypt transmission (TLS 1.2+)
     - Requirement 6: Secure code (SAST/DAST)
     - Requirement 8: Unique IDs (RBAC)
     - Requirement 10: Log all access (audit trail)
     - Requirement 11: Regular security testing
     
     **SARB (Central Bank Regulations)**:
     - Real-time settlement reporting
     - Liquidity management
     - Clearing system compliance
  
  3. Compliance Test Examples:
     
     **POPIA Test: Data Encryption**
     ```java
     @Test
     public void testPII_IsEncrypted_InDatabase() {
         // Given: User with PII
         User user = createUser("john.doe@example.com", "John", "Doe", "8001015009087");
         
         // When: Query database directly
         String rawData = jdbcTemplate.queryForObject(
             "SELECT email FROM users WHERE id = ?", 
             String.class, 
             user.getId()
         );
         
         // Then: Email should be encrypted (not plaintext)
         assertThat(rawData).isNotEqualTo("john.doe@example.com");
         assertThat(rawData).startsWith("ENC:");  // Encryption prefix
         
         // And: ID number should be hashed
         String rawIdNumber = jdbcTemplate.queryForObject(
             "SELECT id_number FROM users WHERE id = ?", 
             String.class, 
             user.getId()
         );
         assertThat(rawIdNumber).isNotEqualTo("8001015009087");
         assertThat(rawIdNumber).hasSize(64);  // SHA-256 hash
     }
     
     @Test
     public void testPII_NotLogged() {
         // Given: Payment with PII
         PaymentRequest request = PaymentRequest.builder()
             .accountNumber("1234567890")
             .email("john.doe@example.com")
             .build();
         
         // When: Process payment
         paymentService.processPayment(request);
         
         // Then: Logs should not contain PII
         String logs = getApplicationLogs();
         assertThat(logs).doesNotContain("1234567890");
         assertThat(logs).doesNotContain("john.doe@example.com");
         assertThat(logs).contains("ACC-****7890");  // Masked
     }
     ```
     
     **FICA Test: Sanctions Screening**
     ```java
     @Test
     public void testSWIFT_PaymentRequiresSanctionsScreening() {
         // Given: SWIFT payment
         PaymentRequest request = PaymentRequest.builder()
             .paymentType("SWIFT")
             .creditorName("Sanctioned Entity")
             .creditorCountry("IR")  // Iran (sanctioned)
             .build();
         
         // When: Submit payment
         PaymentResponse response = paymentService.processPayment(request);
         
         // Then: Payment should be blocked
         assertThat(response.getStatus()).isEqualTo("SANCTIONS_BLOCKED");
         
         // And: Compliance alert should be raised
         verify(complianceService).raiseAlert(
             eq("SANCTIONS_VIOLATION"),
             contains("Sanctioned Entity")
         );
     }
     ```
     
     **PCI-DSS Test: Audit Trail**
     ```java
     @Test
     public void testAllPaymentAccess_IsAudited() {
         // Given: User accesses payment
         String paymentId = createPayment();
         
         // When: Query payment
         paymentService.getPayment(paymentId);
         
         // Then: Audit log should contain access record
         AuditEvent event = auditService.getLatestEvent(paymentId);
         assertThat(event.getAction()).isEqualTo("PAYMENT_ACCESSED");
         assertThat(event.getUserId()).isEqualTo(currentUser.getId());
         assertThat(event.getTenantId()).isEqualTo(currentTenant.getId());
         assertThat(event.getTimestamp()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
         assertThat(event.getIpAddress()).isNotNull();
         assertThat(event.getUserAgent()).isNotNull();
     }
     ```
  
  4. Technology Stack:
     - JUnit 5 (test runner)
     - TestContainers (database tests)
     - Mockito (mocking)
     - Custom compliance validators

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Compliance Testing Architecture
        - POPIA, FICA, PCI-DSS, SARB tests
        - Audit trail validation
        - Data protection validation
  
  2. LLD (Low-Level Design):
     📋 Compliance Test Scenarios (80+ tests)
        - POPIA: 30 tests (encryption, consent, retention)
        - FICA: 20 tests (KYC, sanctions, STR)
        - PCI-DSS: 20 tests (encryption, audit, secure code)
        - SARB: 10 tests (reporting, liquidity)
  
  3. Implementation:
     📁 /compliance-tests/
        ├─ popia/
        │   ├─ DataEncryptionTest.java
        │   ├─ ConsentManagementTest.java
        │   ├─ RightToErasureTest.java
        │   ├─ DataRetentionTest.java
        │   └─ PIIMaskingTest.java
        ├─ fica/
        │   ├─ KYCValidationTest.java
        │   ├─ SanctionsScreeningTest.java
        │   ├─ SuspiciousTransactionTest.java
        │   └─ RecordKeepingTest.java
        ├─ pci-dss/
        │   ├─ CardDataEncryptionTest.java
        │   ├─ TLSVersionTest.java
        │   ├─ AuditTrailTest.java
        │   ├─ RBACTest.java
        │   └─ SecureCodeTest.java
        ├─ sarb/
        │   ├─ SettlementReportingTest.java
        │   ├─ LiquidityManagementTest.java
        │   └─ ClearingComplianceTest.java
        └─ README.md
  
  4. Testing:
     ✅ POPIA: All 30 tests pass
     ✅ FICA: All 20 tests pass
     ✅ PCI-DSS: All 20 tests pass
     ✅ SARB: All 10 tests pass
  
  5. Documentation:
     📄 README.md
        - Compliance testing overview
        - How to run tests
        - Compliance checklist
     
     📄 COMPLIANCE-REPORT.md
        - POPIA compliance: ✅ PASS
        - FICA compliance: ✅ PASS
        - PCI-DSS compliance: ✅ PASS
        - SARB compliance: ✅ PASS
     
     📄 AUDIT-EVIDENCE.md
        - Test results (screenshots, logs)
        - Compliance attestation
        - Audit trail samples

Success Criteria:
  ✅ All 80+ compliance tests pass
  ✅ POPIA: Data encrypted, consent managed
  ✅ FICA: Sanctions screening, KYC validated
  ✅ PCI-DSS: Audit trail complete
  ✅ SARB: Reporting accurate

Context Sufficiency: ✅ SUFFICIENT
  - Complete compliance spec in docs/21
  - POPIA, FICA, PCI-DSS, SARB requirements documented
  - Data protection patterns known

Dependencies:
  ✅ All 20 microservices deployed - READY
  ✅ Audit service - READY
  ✅ Compliance service - READY
```

---

### Feature 6.5: Production Readiness Testing

```yaml
Feature ID: 6.5
Feature Name: Production Readiness Testing (Chaos, DR, Failover)
Agent Name: Production Readiness Agent
Phase: 6 (Testing)
Estimated Time: 4 days

Role & Expertise:
  You are a Site Reliability Engineer (SRE) with expertise in chaos engineering,
  disaster recovery (DR), failover testing, and production readiness validation.

Task:
  Build a comprehensive production readiness testing framework covering chaos
  engineering (Chaos Mesh), disaster recovery, multi-region failover, and
  operational readiness to ensure the Payments Engine is production-ready.

Context Provided:

  1. Architecture Documents:
     📄 docs/24-SRE-ARCHITECTURE.md (COMPLETE FILE - 3,000 lines)
        - SLOs, error budgets
        - Incident management
        - Disaster recovery (RPO/RTO)
        - Chaos engineering
     
     📄 docs/23-TESTING-ARCHITECTURE.md (Chaos Engineering section)
        - Chaos Mesh framework
        - Failure scenarios
  
  2. Production Readiness Criteria:
     
     **Chaos Engineering**:
     - Pod failure (random pod killed)
     - Network latency (inject 500ms delay)
     - Network partition (split brain)
     - Database failure (PostgreSQL down)
     - Service failure (clearing adapter down)
     - Resource exhaustion (CPU/memory spike)
     
     **Disaster Recovery**:
     - Database backup/restore (RPO: 15 minutes)
     - Point-in-time recovery (PITR)
     - Cross-region failover (RTO: 1 hour)
     - Data consistency validation
     
     **Operational Readiness**:
     - Health checks (all services)
     - Monitoring dashboards (Grafana)
     - Alerting (Slack, PagerDuty)
     - Runbooks (incident response)
     - On-call rotation (PagerDuty)
  
  3. Chaos Mesh Experiments:
     
     **Experiment 1: Pod Failure**
     ```yaml
     apiVersion: chaos-mesh.org/v1alpha1
     kind: PodChaos
     metadata:
       name: payment-initiation-pod-kill
     spec:
       action: pod-kill
       mode: one
       selector:
         namespaces:
           - payments
         labelSelectors:
           app: payment-initiation-service
       scheduler:
         cron: "@every 10m"
     ```
     
     **Experiment 2: Network Latency**
     ```yaml
     apiVersion: chaos-mesh.org/v1alpha1
     kind: NetworkChaos
     metadata:
       name: clearing-adapter-latency
     spec:
       action: delay
       mode: all
       selector:
         namespaces:
           - payments
         labelSelectors:
           app: bankservAfrica-adapter
       delay:
         latency: "500ms"
         correlation: "50"
         jitter: "100ms"
       duration: "5m"
     ```
     
     **Experiment 3: Database Failure**
     ```yaml
     apiVersion: chaos-mesh.org/v1alpha1
     kind: PodChaos
     metadata:
       name: postgresql-failure
     spec:
       action: pod-failure
       mode: one
       selector:
         namespaces:
           - payments
         labelSelectors:
           app: postgresql
       duration: "2m"
     ```
  
  4. Disaster Recovery Tests:
     
     **Test 1: Database Backup & Restore**
     ```bash
     # 1. Create backup
     kubectl exec -n payments postgresql-0 -- \
       pg_dump -U postgres payments > backup.sql
     
     # 2. Corrupt database (simulate disaster)
     kubectl exec -n payments postgresql-0 -- \
       psql -U postgres -c "DROP DATABASE payments;"
     
     # 3. Restore from backup
     kubectl exec -n payments postgresql-0 -- \
       psql -U postgres < backup.sql
     
     # 4. Validate data integrity
     kubectl exec -n payments postgresql-0 -- \
       psql -U postgres payments -c "SELECT COUNT(*) FROM payments;"
     ```
     
     **Test 2: Multi-Region Failover**
     ```bash
     # 1. Primary region (South Africa North) fails
     az aks stop --name payments-prod-san --resource-group payments-rg
     
     # 2. Traffic Manager detects failure (health check)
     # 3. Routes traffic to secondary region (South Africa West)
     
     # 4. Validate: All services up in secondary region
     kubectl get pods -n payments --context=prod-saw
     
     # 5. Validate: Database replicated (PostgreSQL streaming replication)
     kubectl exec -n payments postgresql-0 --context=prod-saw -- \
       psql -U postgres payments -c "SELECT COUNT(*) FROM payments;"
     
     # 6. Validate: Payment processing works
     curl -X POST https://api.payments.example.com/v1/payments \
       -H "Authorization: Bearer $TOKEN" \
       -d '{"paymentType": "EFT", "amount": 100}'
     ```
  
  5. Operational Readiness Checklist:
     ```
     ✅ Health Checks
        ✅ All 20 services have /health endpoints
        ✅ Kubernetes liveness probes configured
        ✅ Kubernetes readiness probes configured
     
     ✅ Monitoring
        ✅ Prometheus scraping all services
        ✅ Grafana dashboards (4) created
        ✅ Jaeger tracing enabled
     
     ✅ Alerting
        ✅ 15+ alert rules configured
        ✅ Slack integration working
        ✅ PagerDuty integration working
     
     ✅ Runbooks
        ✅ Payment service down (runbook created)
        ✅ Database down (runbook created)
        ✅ Clearing adapter timeout (runbook created)
     
     ✅ Disaster Recovery
        ✅ Backup strategy defined (daily, 30-day retention)
        ✅ Restore tested (PITR working)
        ✅ Multi-region failover tested (RTO: 1 hour)
     
     ✅ Security
        ✅ SAST passed (SonarQube A rating)
        ✅ DAST passed (OWASP ZAP 0 HIGH/CRITICAL)
        ✅ Secrets rotated (every 90 days)
     
     ✅ Compliance
        ✅ POPIA compliant
        ✅ FICA compliant (KYC, sanctions)
        ✅ PCI-DSS compliant
     ```
  
  6. Technology Stack:
     - Chaos Mesh 2.6+ (chaos engineering)
     - Litmus 3.5+ (alternative chaos tool)
     - Azure Traffic Manager (multi-region failover)
     - pg_dump/pg_restore (PostgreSQL backup/restore)

Expected Deliverables:

  1. HLD (High-Level Design):
     📊 Production Readiness Architecture
        - Chaos experiments
        - DR architecture (multi-region)
        - Failover flow
  
  2. LLD (Low-Level Design):
     📋 Chaos Experiments (10 experiments)
        - Pod failure, network latency, partition, DB failure
     
     📋 DR Tests (5 tests)
        - Backup/restore, PITR, cross-region failover
  
  3. Implementation:
     📁 /production-readiness-tests/
        ├─ chaos-engineering/
        │   ├─ pod-kill.yaml
        │   ├─ network-latency.yaml
        │   ├─ network-partition.yaml
        │   ├─ database-failure.yaml
        │   ├─ service-failure.yaml
        │   └─ resource-exhaustion.yaml
        ├─ disaster-recovery/
        │   ├─ backup-restore-test.sh
        │   ├─ pitr-test.sh
        │   ├─ multi-region-failover-test.sh
        │   └─ data-consistency-test.sh
        ├─ operational-readiness/
        │   ├─ health-check-validator.sh
        │   ├─ monitoring-validator.sh
        │   ├─ alerting-validator.sh
        │   └─ runbook-validator.sh
        ├─ runbooks/
        │   ├─ payment-service-down.md
        │   ├─ database-down.md
        │   ├─ clearing-adapter-timeout.md
        │   └─ incident-response-template.md
        └─ README.md
  
  4. Testing:
     ✅ All 10 chaos experiments pass (system recovers)
     ✅ Backup/restore: RPO < 15 minutes
     ✅ Multi-region failover: RTO < 1 hour
     ✅ Operational readiness checklist: 100% complete
  
  5. Documentation:
     📄 README.md
        - Production readiness overview
        - How to run chaos experiments
        - How to test DR
     
     📄 PRODUCTION-READINESS-REPORT.md
        - Chaos test results
        - DR test results
        - Operational readiness checklist
     
     📄 RUNBOOK-CATALOG.md
        - All incident runbooks
        - Escalation procedures

Success Criteria:
  ✅ All chaos experiments: system recovers
  ✅ Database backup/restore: RPO < 15 min
  ✅ Multi-region failover: RTO < 1 hour
  ✅ Operational readiness: 100% checklist
  ✅ Production approved by SRE team

Context Sufficiency: ✅ SUFFICIENT
  - Complete SRE spec in docs/24 (3,000 lines)
  - DR architecture documented
  - Chaos engineering patterns known

Dependencies:
  ✅ Chaos Mesh installed - READY
  ✅ Multi-region AKS clusters - READY
  ✅ Azure Traffic Manager - READY
  ✅ PostgreSQL replication - READY
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

## Phase 7: Operations & Channel Management (12 Features) 🆕

### Feature 7.1: Operations Management Service

```yaml
Feature ID: 7.1
Agent Name: Operations-Management-Agent
Phase: 7
Type: NEW Backend Service (#21)
Priority: P0
Dependencies: Tenant Management (3.1), All Phase 1-5 Services
Estimated LOC: 1,200
Estimated Days: 5-7
Template: PROMPT-7.1-OPERATIONS-MANAGEMENT-SERVICE
```

**YOU ARE**: Operations Management Service Agent

**ROLE**: Build the Operations Management Service - a centralized management layer for the operations team to monitor service health, manage circuit breakers, toggle feature flags, and control Kubernetes pods.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Operations Management Service)
   - Lines 1-300 (complete service specification)
   - Service health monitoring (20 microservices)
   - Circuit breaker management (Resilience4j integration)
   - Feature flag management (Unleash SDK integration)
   - K8s pod management (restart, scale, view logs)

2. Related Documents:
   📄 docs/02-MICROSERVICES-BREAKDOWN.md (All services overview)
   📄 docs/17-FEATURE-FLAGS.md (Unleash architecture)
   📄 docs/36-RESILIENCE-PATTERNS-DECISION.md (Circuit breaker patterns)

3. API Specifications:
   ```yaml
   Endpoints (10):
     GET  /api/ops/v1/services                   (List all 22 services)
     GET  /api/ops/v1/services/{id}/health       (Service health details)
     GET  /api/ops/v1/services/{id}/metrics      (Prometheus metrics)
     GET  /api/ops/v1/circuit-breakers           (List all circuit breakers)
     POST /api/ops/v1/circuit-breakers/{id}/open (Force open circuit)
     POST /api/ops/v1/circuit-breakers/{id}/close (Force close circuit)
     GET  /api/ops/v1/feature-flags              (List all feature flags)
     POST /api/ops/v1/feature-flags/{id}/toggle  (Toggle feature flag)
     GET  /api/ops/v1/pods                       (List K8s pods)
     POST /api/ops/v1/pods/{id}/restart          (Restart pod)
   ```

4. Technology Stack:
   ```yaml
   Framework: Spring Boot 3.2+
   Language: Java 17+
   Client Libraries:
     - Kubernetes Java Client (io.fabric8)
     - Unleash Client SDK
     - Resilience4j
     - Prometheus Client
   Database: PostgreSQL (operations_audit_log table)
   Observability: OpenTelemetry, Micrometer
   ```

5. Spring Boot Guidance:
   ```java
   // Use K8s Java Client for pod management
   import io.fabric8.kubernetes.client.KubernetesClient;
   
   @Service
   public class PodManagementService {
       @Autowired
       private KubernetesClient kubernetesClient;
       
       public void restartPod(String podName) {
           kubernetesClient.pods()
               .inNamespace("payments-engine")
               .withName(podName)
               .delete();
       }
   }
   
   // Use Unleash SDK for feature flags
   import io.getunleash.Unleash;
   
   @Service
   public class FeatureFlagService {
       @Autowired
       private Unleash unleash;
       
       public boolean isFeatureEnabled(String featureName, String tenantId) {
           return unleash.isEnabled(featureName, Map.of("tenantId", tenantId));
       }
   }
   ```

6. Database Schema:
   ```sql
   CREATE TABLE operations_audit_log (
       id UUID PRIMARY KEY,
       tenant_id VARCHAR(50) NOT NULL,
       operation_type VARCHAR(50) NOT NULL,  -- CIRCUIT_BREAKER_TOGGLE, FEATURE_FLAG_TOGGLE, POD_RESTART
       target_resource VARCHAR(100) NOT NULL,
       performed_by VARCHAR(100) NOT NULL,
       timestamp TIMESTAMP NOT NULL,
       details JSONB
   );
   ```

**Task**:
  Build the Operations Management Service that provides REST APIs for:
  1. Service health monitoring (aggregate from all 22 services)
  2. Circuit breaker management (open/close circuit breakers)
  3. Feature flag management (toggle features via Unleash)
  4. Kubernetes pod management (restart, view logs, scale)
  
  This service is READ-HEAVY for monitoring and WRITE for management actions.

**Implementation Requirements**:

1. **Service Health Monitoring**:
   - ✅ Aggregate health from all 22 microservices (`/actuator/health`)
   - ✅ Cache health data (30-second TTL)
   - ✅ Return status: UP, DOWN, DEGRADED, UNKNOWN

2. **Circuit Breaker Management**:
   - ✅ Integrate with Resilience4j circuit breakers
   - ✅ Provide APIs to force open/close circuit breakers
   - ✅ Audit all circuit breaker changes

3. **Feature Flag Management**:
   - ✅ Integrate with Unleash for feature flag queries
   - ✅ Provide APIs to toggle feature flags
   - ✅ Validate tenant-specific flags

4. **Pod Management**:
   - ✅ Use K8s Java Client to list/restart/scale pods
   - ✅ Restrict to `payments-engine` namespace only
   - ✅ Audit all pod operations

5. **Security**:
   - ✅ Require `OPS_ADMIN` role for all write operations
   - ✅ Require `OPS_VIEWER` role for read operations
   - ✅ Audit all management actions

**Definition of Done**:
- ✅ All 10 REST endpoints implemented and tested
- ✅ Unit tests (80%+ coverage)
- ✅ Integration tests (Testcontainers for K8s, Unleash)
- ✅ Kubernetes RBAC configured for pod management
- ✅ Audit logging for all operations
- ✅ README with setup guide

---

### Feature 7.2: Metrics Aggregation Service

```yaml
Feature ID: 7.2
Agent Name: Metrics-Aggregation-Agent
Phase: 7
Type: NEW Backend Service (#22)
Priority: P0
Dependencies: All Phase 1-5 Services (for metrics collection)
Estimated LOC: 1,000
Estimated Days: 4-6
Template: PROMPT-7.2-METRICS-AGGREGATION-SERVICE
```

**YOU ARE**: Metrics Aggregation Service Agent

**ROLE**: Build the Metrics Aggregation Service - aggregates Prometheus metrics from all 22 microservices, provides real-time dashboards, and manages alerts.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Metrics Aggregation Service)
   - Lines 301-600 (complete service specification)
   - Real-time metrics aggregation
   - Dashboard data APIs
   - Alert management

2. API Specifications:
   ```yaml
   Endpoints (10):
     GET  /api/metrics/v1/summary                (Overall system metrics)
     GET  /api/metrics/v1/services/{id}/metrics  (Per-service metrics)
     GET  /api/metrics/v1/payments/volume        (Payment volume over time)
     GET  /api/metrics/v1/payments/success-rate  (Success rate by type)
     GET  /api/metrics/v1/latency/p95            (p95 latency by service)
     GET  /api/metrics/v1/errors/top             (Top errors by count)
     GET  /api/metrics/v1/alerts                 (Active alerts)
     POST /api/metrics/v1/alerts/{id}/acknowledge (Acknowledge alert)
     GET  /api/metrics/v1/dashboard/realtime     (Real-time dashboard data)
     GET  /api/metrics/v1/dashboard/historical   (Historical trends)
   ```

3. Technology Stack:
   ```yaml
   Framework: Spring Boot 3.2+ with WebFlux (reactive)
   Language: Java 17+
   Client Libraries:
     - Prometheus Java Client
     - Spring WebFlux (reactive)
     - Micrometer
   Database: PostgreSQL (alert_history table)
   Cache: Redis (for real-time metrics)
   ```

4. Spring Boot Guidance:
   ```java
   // Use WebFlux for reactive, non-blocking metrics aggregation
   import org.springframework.web.reactive.function.client.WebClient;
   import reactor.core.publisher.Flux;
   
   @Service
   public class MetricsAggregationService {
       private final WebClient webClient;
       
       public Flux<ServiceMetrics> aggregateMetrics() {
           return Flux.fromIterable(getAllServiceUrls())
               .flatMap(url -> webClient.get()
                   .uri(url + "/actuator/prometheus")
                   .retrieve()
                   .bodyToMono(String.class)
                   .map(this::parsePrometheusMetrics)
               );
       }
   }
   ```

5. Metrics to Aggregate:
   ```yaml
   Payment Metrics:
     - payments_total (by type, status, tenant)
     - payments_amount_total (by currency)
     - payments_duration_seconds (p50, p95, p99)
   
   System Metrics:
     - http_requests_total (by endpoint, status)
     - jvm_memory_used_bytes
     - jvm_threads_live
     - resilience4j_circuitbreaker_state
   
   Business Metrics:
     - active_tenants_count
     - daily_payment_volume
     - fraud_detection_rate
   ```

**Task**:
  Build the Metrics Aggregation Service that aggregates Prometheus metrics from all 22 microservices and provides real-time dashboards and alerts.

**Implementation Requirements**:

1. **Metrics Aggregation**:
   - ✅ Scrape metrics from all 22 services (`/actuator/prometheus`)
   - ✅ Parse Prometheus format (use Micrometer)
   - ✅ Aggregate metrics by tenant, payment type, service
   - ✅ Cache in Redis (60-second TTL)

2. **Real-Time Dashboards**:
   - ✅ Provide WebSocket or SSE for real-time updates
   - ✅ Return JSON format for React frontend
   - ✅ Support time-series data (last 24 hours)

3. **Alert Management**:
   - ✅ Define alert rules (error rate > 5%, latency > 10s)
   - ✅ Trigger alerts when thresholds exceeded
   - ✅ Store alert history in PostgreSQL

4. **Performance**:
   - ✅ Use WebFlux for non-blocking I/O
   - ✅ Aggregate metrics in parallel (Flux.flatMap)
   - ✅ Cache aggregated metrics (Redis)

**Definition of Done**:
- ✅ All 10 REST endpoints implemented
- ✅ Unit tests (80%+ coverage)
- ✅ Integration tests (Testcontainers)
- ✅ WebSocket/SSE for real-time updates
- ✅ README with setup guide

---

### Feature 7.3: Payment Repair APIs

```yaml
Feature ID: 7.3
Agent Name: Payment-Repair-Agent
Phase: 7
Type: Enhanced Backend (Payment Initiation Service)
Priority: P0
Dependencies: Payment Initiation (1.1), Saga Orchestrator (1.6)
Estimated LOC: 800
Estimated Days: 3-4
Template: PROMPT-7.3-PAYMENT-REPAIR-APIs
```

**YOU ARE**: Payment Repair APIs Agent

**ROLE**: Enhance the Payment Initiation Service with payment repair APIs for the operations team to retry failed payments, compensate, and manage stuck payments.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Payment Repair APIs)
   - Lines 601-900 (complete API specification)
   - Failed payment retrieval
   - Retry mechanisms
   - Compensation APIs

2. API Specifications:
   ```yaml
   Endpoints (8):
     GET  /api/v1/payments/failed                   (List failed payments)
     GET  /api/v1/payments/{id}/failure-details     (Get failure details)
     POST /api/v1/payments/{id}/retry               (Retry failed payment)
     POST /api/v1/payments/{id}/compensate          (Compensate payment)
     POST /api/v1/payments/{id}/cancel              (Cancel payment)
     GET  /api/v1/payments/{id}/saga-state          (Get saga state)
     POST /api/v1/payments/{id}/force-complete      (Force complete payment)
     POST /api/v1/payments/{id}/reprocess           (Reprocess from scratch)
   ```

3. Failure Reasons (handle all):
   ```yaml
   ACCOUNT_DEBIT_FAILED:
     - Insufficient funds
     - Account locked
     - Core banking system unavailable
   
   CLEARING_SYSTEM_FAILED:
     - Timeout
     - Network error
     - Clearing system rejection
   
   SAGA_INCOMPLETE:
     - Saga stuck in intermediate state
     - Compensation partially failed
   
   FRAUD_DETECTED:
     - High fraud score
     - Manual review required
   ```

**Task**:
  Enhance the Payment Initiation Service with payment repair APIs for failed/stuck payments. This is CRITICAL for operations team to resolve payment issues.

**Implementation Requirements**:

1. **Failed Payment Retrieval**:
   - ✅ Query payments with status: FAILED, COMPENSATED, STUCK
   - ✅ Return failure reason, timestamp, affected saga steps
   - ✅ Support filtering by tenant, payment type, date range

2. **Retry Logic**:
   - ✅ Validate payment can be retried (idempotency check)
   - ✅ Re-execute saga from failed step
   - ✅ Update payment status to RETRYING
   - ✅ Audit retry action

3. **Compensation Logic**:
   - ✅ Execute compensation saga (reverse all completed steps)
   - ✅ Credit account if debited
   - ✅ Update payment status to COMPENSATED

4. **Security**:
   - ✅ Require `OPS_PAYMENT_REPAIR` role
   - ✅ Audit all repair actions
   - ✅ Verify tenant context

**Definition of Done**:
- ✅ All 8 REST endpoints implemented
- ✅ Unit tests (80%+ coverage)
- ✅ Integration tests (test retry/compensate flows)
- ✅ Audit logging for all repair actions
- ✅ README with repair guide

---

### Feature 7.4: Saga Management APIs

```yaml
Feature ID: 7.4
Agent Name: Saga-Management-Agent
Phase: 7
Type: Enhanced Backend (Saga Orchestrator Service)
Priority: P0
Dependencies: Saga Orchestrator (1.6)
Estimated LOC: 600
Estimated Days: 2-3
Template: PROMPT-7.4-SAGA-MANAGEMENT-APIs
```

**YOU ARE**: Saga Management APIs Agent

**ROLE**: Enhance the Saga Orchestrator Service with management APIs for the operations team to view saga state, manually advance/rollback sagas, and resolve stuck sagas.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Saga Management APIs)
   - Lines 901-1200 (complete API specification)
   - Saga state visualization
   - Manual saga control
   - Stuck saga resolution

2. API Specifications:
   ```yaml
   Endpoints (6):
     GET  /api/saga/v1/sagas                      (List all sagas)
     GET  /api/saga/v1/sagas/{id}/state           (Get saga state diagram)
     POST /api/saga/v1/sagas/{id}/advance         (Manually advance saga)
     POST /api/saga/v1/sagas/{id}/rollback        (Manually rollback saga)
     POST /api/saga/v1/sagas/{id}/reset           (Reset saga to initial state)
     GET  /api/saga/v1/sagas/stuck                (List stuck sagas)
   ```

3. Saga States (handle all):
   ```yaml
   INITIATED:       Saga started
   STEP_1_COMPLETE: Validation complete
   STEP_2_COMPLETE: Account debited
   STEP_3_COMPLETE: Routing complete
   STEP_4_COMPLETE: Clearing submitted
   COMPLETED:       All steps complete
   COMPENSATING:    Rollback in progress
   COMPENSATED:     All steps rolled back
   FAILED:          Saga permanently failed
   STUCK:           Saga stuck for >5 minutes
   ```

**Task**:
  Enhance the Saga Orchestrator Service with management APIs for manual saga control. This is CRITICAL for resolving stuck sagas.

**Implementation Requirements**:

1. **Saga State Retrieval**:
   - ✅ Query saga state from database
   - ✅ Return state diagram (JSON format for visualization)
   - ✅ Include timestamp, current step, completion percentage

2. **Manual Saga Control**:
   - ✅ Advance saga: move to next step (if validation passes)
   - ✅ Rollback saga: execute compensation for all completed steps
   - ✅ Reset saga: set to initial state (for retry)

3. **Stuck Saga Detection**:
   - ✅ Query sagas stuck for >5 minutes
   - ✅ Provide resolution options (advance, rollback, reset)

4. **Security**:
   - ✅ Require `OPS_SAGA_ADMIN` role
   - ✅ Audit all saga management actions

**Definition of Done**:
- ✅ All 6 REST endpoints implemented
- ✅ Unit tests (80%+ coverage)
- ✅ Integration tests (test manual saga control)
- ✅ Audit logging
- ✅ README with saga management guide

---

### Feature 7.5: Transaction Search APIs

```yaml
Feature ID: 7.5
Agent Name: Transaction-Search-Agent
Phase: 7
Type: Enhanced Backend (Reporting Service)
Priority: P0
Dependencies: Reporting Service (3.5)
Estimated LOC: 700
Estimated Days: 3-4
Template: PROMPT-7.5-TRANSACTION-SEARCH-APIs
```

**YOU ARE**: Transaction Search APIs Agent

**ROLE**: Enhance the Reporting Service with advanced transaction search APIs for the operations team to query payments by multiple criteria, view transaction details, and export results.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Transaction Search APIs)
   - Lines 1201-1500 (complete API specification)
   - Advanced search filters
   - Transaction detail view
   - Export functionality

2. API Specifications:
   ```yaml
   Endpoints (5):
     POST /api/reporting/v1/transactions/search     (Advanced search)
     GET  /api/reporting/v1/transactions/{id}       (Transaction detail)
     GET  /api/reporting/v1/transactions/{id}/audit (Audit trail)
     POST /api/reporting/v1/transactions/export     (Export to CSV/Excel)
     GET  /api/reporting/v1/transactions/stats      (Search statistics)
   ```

3. Search Filters (support all):
   ```yaml
   Filters:
     - tenantId (required for multi-tenancy)
     - paymentId, transactionId
     - paymentType (EFT, RTC, RTGS, SWIFT, PAYSHAP)
     - status (INITIATED, PROCESSING, COMPLETED, FAILED)
     - dateRange (from, to)
     - amountRange (min, max)
     - debtorAccount, creditorAccount
     - clearingSystem (SAMOS, BANKSERV, RTC, PAYSHAP, SWIFT)
     - reference (partial match)
   ```

4. Technology Stack:
   ```yaml
   Framework: Spring Boot 3.2+
   Search: Spring Data JPA with Specifications
   Export: Apache POI (Excel), OpenCSV (CSV)
   ```

5. Spring Boot Guidance:
   ```java
   // Use Spring Data JPA Specifications for dynamic search
   import org.springframework.data.jpa.domain.Specification;
   
   @Service
   public class TransactionSearchService {
       
       public Page<Payment> search(TransactionSearchRequest request, Pageable pageable) {
           Specification<Payment> spec = Specification.where(null);
           
           if (request.getPaymentType() != null) {
               spec = spec.and((root, query, cb) -> 
                   cb.equal(root.get("paymentType"), request.getPaymentType()));
           }
           
           if (request.getDateRange() != null) {
               spec = spec.and((root, query, cb) -> 
                   cb.between(root.get("createdAt"), 
                       request.getDateRange().getFrom(), 
                       request.getDateRange().getTo()));
           }
           
           // ... add more filters
           
           return paymentRepository.findAll(spec, pageable);
       }
   }
   ```

**Task**:
  Enhance the Reporting Service with advanced transaction search APIs. This is CRITICAL for operations team to investigate payment issues.

**Implementation Requirements**:

1. **Advanced Search**:
   - ✅ Support all 10+ search filters
   - ✅ Use Spring Data JPA Specifications for dynamic queries
   - ✅ Paginate results (default 50 per page)
   - ✅ Sort by any field

2. **Transaction Detail View**:
   - ✅ Return complete payment details
   - ✅ Include saga state, clearing response, audit trail
   - ✅ Mask sensitive data (account numbers)

3. **Export Functionality**:
   - ✅ Export to CSV or Excel (Apache POI)
   - ✅ Include all search results (up to 10,000 rows)
   - ✅ Async export for large datasets (>1,000 rows)

4. **Performance**:
   - ✅ Add database indexes on searchable fields
   - ✅ Use read replicas for search queries
   - ✅ Cache frequently searched results (Redis, 5-minute TTL)

**Definition of Done**:
- ✅ All 5 REST endpoints implemented
- ✅ Unit tests (80%+ coverage)
- ✅ Integration tests (Testcontainers)
- ✅ Database indexes created
- ✅ README with search guide

---

### Feature 7.6: Reconciliation Management APIs

```yaml
Feature ID: 7.6
Agent Name: Reconciliation-Management-Agent
Phase: 7
Type: Enhanced Backend (Reconciliation Service)
Priority: P1
Dependencies: Reconciliation Service (4.3)
Estimated LOC: 500
Estimated Days: 2-3
Template: PROMPT-7.6-RECONCILIATION-MANAGEMENT-APIs
```

**YOU ARE**: Reconciliation Management APIs Agent

**ROLE**: Enhance the Reconciliation Service with management APIs for the operations team to view unmatched payments, manually match payments, and resolve reconciliation issues.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Reconciliation Management APIs)
   - Lines 1501-1783 (complete API specification)
   - Unmatched payment queries
   - Manual matching
   - Reconciliation reports

2. API Specifications:
   ```yaml
   Endpoints (6):
     GET  /api/reconciliation/v1/unmatched          (List unmatched payments)
     POST /api/reconciliation/v1/match              (Manually match payment)
     POST /api/reconciliation/v1/unmatch            (Unmatch payment)
     GET  /api/reconciliation/v1/reports/daily      (Daily reconciliation report)
     GET  /api/reconciliation/v1/reports/exceptions (Exception report)
     POST /api/reconciliation/v1/rerun              (Rerun reconciliation)
   ```

3. Reconciliation States:
   ```yaml
   MATCHED:       Payment matched with clearing response
   UNMATCHED:     Payment not matched (>24 hours old)
   PARTIAL_MATCH: Amount mismatch (internal vs clearing)
   DUPLICATE:     Multiple clearing responses for same payment
   ORPHAN:        Clearing response with no internal payment
   ```

**Task**:
  Enhance the Reconciliation Service with management APIs for manual reconciliation. This is IMPORTANT for operations team to resolve exceptions.

**Implementation Requirements**:

1. **Unmatched Payment Queries**:
   - ✅ Query payments with status: UNMATCHED, PARTIAL_MATCH, DUPLICATE
   - ✅ Filter by tenant, clearing system, date range
   - ✅ Return matching suggestions (fuzzy match on amount, reference)

2. **Manual Matching**:
   - ✅ Allow ops team to manually link payment to clearing response
   - ✅ Validate match (amount within tolerance)
   - ✅ Update reconciliation status
   - ✅ Audit manual match action

3. **Reconciliation Reports**:
   - ✅ Daily report: matched count, unmatched count, total amount
   - ✅ Exception report: list all unmatched payments
   - ✅ Export to CSV/Excel

4. **Security**:
   - ✅ Require `OPS_RECONCILIATION_ADMIN` role
   - ✅ Audit all manual actions

**Definition of Done**:
- ✅ All 6 REST endpoints implemented
- ✅ Unit tests (80%+ coverage)
- ✅ Integration tests
- ✅ Audit logging
- ✅ README with reconciliation guide

---

### Feature 7.7: React Ops Portal - Service Management UI

```yaml
Feature ID: 7.7
Agent Name: React-Service-Management-Agent
Phase: 7
Type: React Frontend
Priority: P0
Dependencies: Operations Management Service (7.1)
Estimated LOC: 1,500
Estimated Days: 4-5
Template: PROMPT-7.7-REACT-SERVICE-MANAGEMENT-UI
```

**YOU ARE**: React Service Management UI Agent

**ROLE**: Build the Service Management UI in the React Operations Portal for monitoring all 22 microservices, managing circuit breakers, toggling feature flags, and controlling Kubernetes pods.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Frontend UIs - Service Management)
   - Lines 1784-2100 (complete UI specification)
   - Service health dashboard
   - Circuit breaker management
   - Feature flag toggles
   - Pod management

2. UI Components (Material-UI):
   ```tsx
   Components:
     - ServiceHealthDashboard (grid of 22 service cards)
     - ServiceDetailDialog (health, metrics, logs)
     - CircuitBreakerList (list of all circuit breakers)
     - CircuitBreakerToggle (open/close circuit)
     - FeatureFlagList (list of all feature flags)
     - FeatureFlagToggle (enable/disable flag)
     - PodList (list of all K8s pods)
     - PodManagement (restart, scale, view logs)
   ```

3. Backend APIs (Operations Management Service):
   ```
   GET  /api/ops/v1/services
   GET  /api/ops/v1/services/{id}/health
   GET  /api/ops/v1/circuit-breakers
   POST /api/ops/v1/circuit-breakers/{id}/open
   GET  /api/ops/v1/feature-flags
   POST /api/ops/v1/feature-flags/{id}/toggle
   GET  /api/ops/v1/pods
   POST /api/ops/v1/pods/{id}/restart
   ```

4. Technology Stack:
   ```yaml
   Framework: React 18+ with TypeScript
   UI Library: Material-UI (MUI) 5+
   State Management: React Query (TanStack Query)
   Routing: React Router 6+
   Real-Time: WebSocket or SSE
   ```

5. React Guidance:
   ```tsx
   // Use React Query for data fetching
   import { useQuery } from '@tanstack/react-query';
   
   function ServiceHealthDashboard() {
     const { data: services } = useQuery({
       queryKey: ['services'],
       queryFn: () => fetch('/api/ops/v1/services').then(res => res.json()),
       refetchInterval: 30000, // Refresh every 30 seconds
     });
     
     return (
       <Grid container spacing={2}>
         {services?.map(service => (
           <Grid item xs={12} sm={6} md={4} key={service.id}>
             <ServiceCard service={service} />
           </Grid>
         ))}
       </Grid>
     );
   }
   ```

**Task**:
  Build the Service Management UI for the React Operations Portal. This is the PRIMARY dashboard for operations team.

**Implementation Requirements**:

1. **Service Health Dashboard**:
   - ✅ Display all 22 services in a grid
   - ✅ Color-coded status (green=UP, red=DOWN, yellow=DEGRADED)
   - ✅ Auto-refresh every 30 seconds
   - ✅ Click to view service details

2. **Circuit Breaker Management**:
   - ✅ List all circuit breakers with state (CLOSED, OPEN, HALF_OPEN)
   - ✅ Toggle buttons to force open/close
   - ✅ Show metrics (failure rate, slow call rate)

3. **Feature Flag Management**:
   - ✅ List all feature flags with current state (enabled/disabled)
   - ✅ Toggle switches to enable/disable
   - ✅ Show tenant-specific overrides

4. **Pod Management**:
   - ✅ List all K8s pods with status (Running, Pending, Failed)
   - ✅ Restart button for each pod
   - ✅ View logs button (opens dialog with logs)

**Definition of Done**:
- ✅ All UI components implemented
- ✅ Unit tests (React Testing Library)
- ✅ E2E tests (Cypress)
- ✅ Responsive design (mobile-friendly)
- ✅ README with UI guide

---

### Feature 7.8: React Ops Portal - Payment Repair UI

```yaml
Feature ID: 7.8
Agent Name: React-Payment-Repair-Agent
Phase: 7
Type: React Frontend
Priority: P0
Dependencies: Payment Repair APIs (7.3), Saga Management APIs (7.4)
Estimated LOC: 1,800
Estimated Days: 5-6
Template: PROMPT-7.8-REACT-PAYMENT-REPAIR-UI
```

**YOU ARE**: React Payment Repair UI Agent

**ROLE**: Build the Payment Repair UI in the React Operations Portal for viewing failed payments, retrying payments, compensating payments, and managing saga state.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Frontend UIs - Payment Repair)
   - Lines 2101-2400 (complete UI specification)
   - Failed payment list
   - Payment detail view
   - Retry/compensate actions
   - Saga state visualization

2. UI Components:
   ```tsx
   Components:
     - FailedPaymentList (table with filters)
     - PaymentDetailDialog (full payment details)
     - SagaStateVisualization (step-by-step diagram)
     - PaymentActionButtons (retry, compensate, cancel)
     - BulkPaymentRepair (bulk retry/compensate)
   ```

3. Backend APIs:
   ```
   GET  /api/v1/payments/failed
   GET  /api/v1/payments/{id}/failure-details
   POST /api/v1/payments/{id}/retry
   POST /api/v1/payments/{id}/compensate
   GET  /api/saga/v1/sagas/{id}/state
   POST /api/saga/v1/sagas/{id}/advance
   ```

4. React Guidance:
   ```tsx
   // Use React Query for mutations
   import { useMutation } from '@tanstack/react-query';
   
   function PaymentRepairActions({ paymentId }) {
     const retryMutation = useMutation({
       mutationFn: (id: string) => 
         fetch(`/api/v1/payments/${id}/retry`, { method: 'POST' }),
       onSuccess: () => {
         queryClient.invalidateQueries(['payments']);
         toast.success('Payment retry initiated');
       },
     });
     
     return (
       <Button onClick={() => retryMutation.mutate(paymentId)}>
         Retry Payment
       </Button>
     );
   }
   ```

**Task**:
  Build the Payment Repair UI for failed/stuck payments. This is CRITICAL for operations team to resolve payment issues.

**Implementation Requirements**:

1. **Failed Payment List**:
   - ✅ Table with columns: Payment ID, Type, Amount, Status, Failure Reason, Date
   - ✅ Filters: Tenant, Payment Type, Date Range, Failure Reason
   - ✅ Pagination (50 per page)
   - ✅ Click to view payment details

2. **Payment Detail View**:
   - ✅ Display full payment details (debtor, creditor, amount, reference)
   - ✅ Display failure reason, timestamp, affected saga steps
   - ✅ Show saga state visualization (step-by-step diagram)

3. **Retry/Compensate Actions**:
   - ✅ Retry button (re-execute saga from failed step)
   - ✅ Compensate button (reverse all completed steps)
   - ✅ Cancel button (mark payment as cancelled)
   - ✅ Confirmation dialogs for all actions

4. **Bulk Repair**:
   - ✅ Select multiple failed payments
   - ✅ Bulk retry or bulk compensate
   - ✅ Show progress bar for bulk actions

**Definition of Done**:
- ✅ All UI components implemented
- ✅ Unit tests (React Testing Library)
- ✅ E2E tests (Cypress)
- ✅ README with repair guide

---

### Feature 7.9: React Ops Portal - Transaction Enquiries UI

```yaml
Feature ID: 7.9
Agent Name: React-Transaction-Enquiries-Agent
Phase: 7
Type: React Frontend
Priority: P0
Dependencies: Transaction Search APIs (7.5)
Estimated LOC: 1,500
Estimated Days: 4-5
Template: PROMPT-7.9-REACT-TRANSACTION-ENQUIRIES-UI
```

**YOU ARE**: React Transaction Enquiries UI Agent

**ROLE**: Build the Transaction Enquiries UI in the React Operations Portal for advanced transaction search, viewing transaction details, and exporting results.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Frontend UIs - Transaction Enquiries)
   - Lines 2401-2700 (complete UI specification)
   - Advanced search form
   - Transaction results table
   - Transaction detail view
   - Export functionality

2. UI Components:
   ```tsx
   Components:
     - AdvancedSearchForm (10+ filter fields)
     - TransactionResultsTable (paginated results)
     - TransactionDetailDialog (full transaction details)
     - ExportDialog (CSV/Excel export)
     - SavedSearches (save/load search criteria)
   ```

3. Backend APIs:
   ```
   POST /api/reporting/v1/transactions/search
   GET  /api/reporting/v1/transactions/{id}
   GET  /api/reporting/v1/transactions/{id}/audit
   POST /api/reporting/v1/transactions/export
   ```

4. Search Filters:
   ```tsx
   interface TransactionSearchRequest {
     tenantId: string;
     paymentId?: string;
     paymentType?: 'EFT' | 'RTC' | 'RTGS' | 'SWIFT' | 'PAYSHAP';
     status?: 'INITIATED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
     dateRange?: { from: Date; to: Date };
     amountRange?: { min: number; max: number };
     debtorAccount?: string;
     creditorAccount?: string;
     reference?: string;
   }
   ```

**Task**:
  Build the Transaction Enquiries UI for advanced payment search. This is CRITICAL for operations team to investigate payment issues.

**Implementation Requirements**:

1. **Advanced Search Form**:
   - ✅ Support all 10+ search filters
   - ✅ Date range picker (react-datepicker)
   - ✅ Amount range inputs (min, max)
   - ✅ Auto-complete for account numbers

2. **Transaction Results Table**:
   - ✅ Display search results in a table
   - ✅ Columns: Payment ID, Type, Amount, Status, Date
   - ✅ Pagination (50 per page)
   - ✅ Sort by any column
   - ✅ Click to view transaction details

3. **Transaction Detail View**:
   - ✅ Display full transaction details
   - ✅ Show saga state, clearing response, audit trail
   - ✅ Mask account numbers (ACC-****7890)

4. **Export Functionality**:
   - ✅ Export button (CSV or Excel)
   - ✅ Show progress bar for large exports
   - ✅ Download file when ready

**Definition of Done**:
- ✅ All UI components implemented
- ✅ Unit tests (React Testing Library)
- ✅ E2E tests (Cypress)
- ✅ README with search guide

---

### Feature 7.10: React Ops Portal - Reconciliation & Monitoring UI

```yaml
Feature ID: 7.10
Agent Name: React-Reconciliation-Monitoring-Agent
Phase: 7
Type: React Frontend
Priority: P1
Dependencies: Reconciliation Management APIs (7.6), Metrics Aggregation Service (7.2)
Estimated LOC: 1,200
Estimated Days: 4-5
Template: PROMPT-7.10-REACT-RECONCILIATION-MONITORING-UI
```

**YOU ARE**: React Reconciliation & Monitoring UI Agent

**ROLE**: Build the Reconciliation & Monitoring UI in the React Operations Portal for viewing unmatched payments, manually matching payments, and real-time system monitoring.

**Context Provided**:

1. Architecture Documents:
   📄 docs/40-PHASE-7-DETAILED-DESIGN.md (Section: Frontend UIs - Reconciliation & Monitoring)
   - Lines 2701-3000 (complete UI specification)
   - Unmatched payment list
   - Manual matching interface
   - Real-time dashboard
   - Alert management

2. UI Components:
   ```tsx
   Components:
     - UnmatchedPaymentList (table with matching suggestions)
     - ManualMatchDialog (match payment to clearing response)
     - ReconciliationReportChart (daily matched vs unmatched)
     - RealtimeDashboard (payment volume, success rate, latency)
     - AlertList (active alerts with acknowledge button)
   ```

3. Backend APIs:
   ```
   GET  /api/reconciliation/v1/unmatched
   POST /api/reconciliation/v1/match
   GET  /api/reconciliation/v1/reports/daily
   GET  /api/metrics/v1/dashboard/realtime
   GET  /api/metrics/v1/alerts
   POST /api/metrics/v1/alerts/{id}/acknowledge
   ```

**Task**:
  Build the Reconciliation & Monitoring UI for manual reconciliation and real-time system monitoring.

**Implementation Requirements**:

1. **Unmatched Payment List**:
   - ✅ Display unmatched payments in a table
   - ✅ Show matching suggestions (fuzzy match)
   - ✅ Manual match button (opens dialog)

2. **Manual Matching**:
   - ✅ Dialog to select clearing response
   - ✅ Validate match (amount within tolerance)
   - ✅ Confirmation dialog
   - ✅ Update reconciliation status

3. **Real-Time Dashboard**:
   - ✅ Display real-time metrics (payment volume, success rate, latency)
   - ✅ Use WebSocket or SSE for auto-updates
   - ✅ Charts (Recharts or Chart.js)

4. **Alert Management**:
   - ✅ Display active alerts
   - ✅ Acknowledge button (marks alert as seen)
   - ✅ Show alert history

**Definition of Done**:
- ✅ All UI components implemented
- ✅ Unit tests (React Testing Library)
- ✅ E2E tests (Cypress)
- ✅ README with reconciliation guide

---

### Feature 7.11: Channel Onboarding UI

```yaml
Feature ID: 7.11
Agent Name: React-Channel-Onboarding-Agent
Phase: 7
Type: React Frontend
Priority: P1
Dependencies: Tenant Management (3.1), Notification Service (3.4)
Estimated LOC: 1,300
Estimated Days: 3-4
Template: PROMPT-7.11-CHANNEL-ONBOARDING-UI
```

**YOU ARE**: React Channel Onboarding UI Agent

**ROLE**: Build the Channel Onboarding UI in the React Operations Portal for self-service channel setup, allowing channels to configure their response pattern (Webhook, Kafka, WebSocket, Polling, Push) and payment-type-specific Kafka topics.

**Context Provided**:

1. Architecture Documents:
   📄 docs/39-CHANNEL-INTEGRATION-MECHANISMS.md (Section 11: Channel Onboarding)
   - Lines 1986-3650 (complete onboarding specification)
   - 4-step wizard: Basic Info, Response Pattern, Pattern Config, Review
   - Support for 5 response patterns: Webhook, Kafka, WebSocket, Polling, Push
   - Payment-type-specific Kafka topics (EFT, RTC, SWIFT, etc.)

2. UI Components:
   ```tsx
   Components:
     - ChannelOnboardingWizard (4-step stepper)
     - BasicInfoStep (channel name, type, contact)
     - ResponsePatternStep (select pattern: Webhook, Kafka, WebSocket, Polling, Push)
     - WebhookConfiguration (webhook URL, security, retry)
     - KafkaConfiguration (topic, consumer group, partitions, payment-type topics)
     - WebSocketConfiguration (connection URL, auth)
     - ReviewStep (review all config, test connection)
     - ChannelListPage (list all channels, edit, delete)
   ```

3. Backend APIs:
   ```
   POST /api/v1/channel-configurations
   GET  /api/v1/channel-configurations
   PUT  /api/v1/channel-configurations/{id}
   POST /api/v1/channel-configurations/{id}/test
   DELETE /api/v1/channel-configurations/{id}
   
   POST /api/v1/channel-kafka-topics (for payment-type topics)
   GET  /api/v1/channel-kafka-topics/{channelId}
   ```

4. Response Patterns:
   ```yaml
   WEBHOOK:
     - URL, security (HMAC, OAuth, mTLS), retry policy
   
   KAFKA:
     - Default topic (all payment types) OR
     - Per-payment-type topics (EFT, RTC, SWIFT, etc.)
     - Consumer group, partition count
   
   WEBSOCKET:
     - Connection URL, auth token
   
   POLLING:
     - Poll interval (seconds)
   
   PUSH:
     - Push notification service (FCM, APNS)
   ```

5. React Guidance:
   ```tsx
   // Use React Hook Form for wizard
   import { useForm, FormProvider } from 'react-hook-form';
   
   function ChannelOnboardingWizard() {
     const [activeStep, setActiveStep] = useState(0);
     const methods = useForm<ChannelConfiguration>();
     
     return (
       <FormProvider {...methods}>
         <Stepper activeStep={activeStep}>
           {steps.map((label) => (
             <Step key={label}><StepLabel>{label}</StepLabel></Step>
           ))}
         </Stepper>
         {renderStepContent(activeStep)}
       </FormProvider>
     );
   }
   ```

6. Kafka Payment-Type Topics UI:
   ```tsx
   // Toggle for payment-type-specific topics
   <FormControlLabel
     control={<Switch checked={usePaymentTypeTopics} />}
     label="Use separate Kafka topics per payment type"
   />
   
   {usePaymentTypeTopics && (
     <Stack spacing={2}>
       {paymentTypes.map(type => (
         <Accordion key={type.value}>
           <AccordionSummary>{type.label}</AccordionSummary>
           <AccordionDetails>
             <TextField label="Topic Name" placeholder={`payments.responses.${type.value.toLowerCase()}.${tenantId}.${channelId}`} />
             <TextField label="Consumer Group" />
             <TextField label="Partition Count" type="number" />
           </AccordionDetails>
         </Accordion>
       ))}
     </Stack>
   )}
   ```

**Task**:
  Build the Channel Onboarding UI for self-service channel configuration. This allows channels to configure their response pattern without backend team involvement.

**Implementation Requirements**:

1. **4-Step Wizard**:
   - ✅ Step 1: Basic Info (channel name, type, contact)
   - ✅ Step 2: Response Pattern (select from 5 patterns)
   - ✅ Step 3: Pattern Configuration (dynamic form based on selected pattern)
   - ✅ Step 4: Review & Test (display config, test connection button)

2. **Kafka Configuration**:
   - ✅ Default topic for all payment types OR
   - ✅ Toggle to enable per-payment-type topics
   - ✅ If enabled, show accordions for each payment type (EFT, RTC, SWIFT, etc.)
   - ✅ Allow customization of topic name, consumer group, partition count

3. **Channel Management**:
   - ✅ List all channels in a table
   - ✅ Edit button (opens wizard in edit mode)
   - ✅ Delete button (with confirmation)
   - ✅ Test button (test channel connection)

4. **Validation**:
   - ✅ Validate all required fields
   - ✅ Test connection before saving
   - ✅ Show success/error messages

**Definition of Done**:
- ✅ All UI components implemented
- ✅ Unit tests (React Testing Library)
- ✅ E2E tests (Cypress)
- ✅ README with onboarding guide

---

### Feature 7.12: Clearing System Onboarding UI 🆕

```yaml
Feature ID: 7.12
Agent Name: React-Clearing-Onboarding-Agent
Phase: 7
Type: React Frontend
Priority: P0
Dependencies: Tenant Management (3.1), Clearing Adapters (Phase 2)
Estimated LOC: 1,500
Estimated Days: 5-7
Template: PROMPT-7.12-CLEARING-SYSTEM-ONBOARDING-UI
```

**YOU ARE**: React Clearing System Onboarding UI Agent

**ROLE**: Build the Clearing System Onboarding UI in the React Operations Portal for self-service clearing system configuration, allowing tenants to configure integration with SAMOS, BankservAfrica, RTC, PayShap, and SWIFT with configurable communication patterns, security mechanisms, and retry policies.

**Context Provided**:

1. Architecture Documents:
   📄 docs/42-CLEARING-SYSTEM-ONBOARDING.md (Complete specification)
   - Lines 1-2732 (complete onboarding specification)
   - 5-step wizard: System Selection, Communication Pattern, Security Config, Retry Policy, Review & Test
   - Support for 5 clearing systems: SAMOS, BankservAfrica (EFT), RTC, PayShap, SWIFT
   - 3 communication patterns: Synchronous, Asynchronous, Batch
   - 6 security mechanisms: mTLS, OAuth 2.0, SFTP, Certificate-based, SWIFTNet PKI, API Key

2. Clearing Systems Supported:
   ```yaml
   SAMOS (RTGS):
     - Pattern: SYNCHRONOUS
     - Format: ISO_20022_XML (pacs.008)
     - Security: MTLS + DIGITAL_SIGNATURE
     - Timeout: 30 seconds
     - Retry: 0 (no retries)
   
   BankservAfrica (EFT/ACH):
     - Pattern: BATCH (file-based)
     - Format: ACH_FIXED_LENGTH
     - Security: SFTP_KEY + PGP_ENCRYPTION
     - Batch Window: 08:00-16:00 SAST
     - Retry: 3 attempts
   
   RTC (Real-Time Clearing):
     - Pattern: SYNCHRONOUS
     - Format: ISO_8583_BINARY or ISO_20022_XML
     - Security: MTLS + MAC
     - Timeout: 10 seconds
     - Retry: 1 retry
   
   PayShap (Instant P2P):
     - Pattern: SYNCHRONOUS
     - Format: JSON_REST
     - Security: OAUTH_2_0 + MTLS
     - Timeout: 5 seconds
     - Retry: 0 (instant payments)
   
   SWIFT (International):
     - Pattern: ASYNCHRONOUS
     - Format: SWIFT_MT103 or ISO_20022_XML
     - Security: SWIFTNET_PKI + RMA
     - Retry: 3 attempts (exponential backoff)
     - Sanctions Screening: MANDATORY
   ```

3. UI Components:
   ```tsx
   Components:
     - ClearingSystemOnboardingWizard (5-step stepper)
     - ClearingSystemSelector (select from 5 systems)
     - CommunicationPatternConfig (select sync/async/batch)
     - SecurityConfig (dynamic form based on selected mechanism)
     - RetryPolicyConfig (retry count, backoff strategy, timeout)
     - ReviewAndTest (review config, test connection button)
     - ClearingSystemListPage (list all clearing configs, edit, delete)
   ```

4. Backend APIs:
   ```
   POST /api/v1/clearing-systems
   GET  /api/v1/clearing-systems
   GET  /api/v1/clearing-systems/{id}
   PUT  /api/v1/clearing-systems/{id}
   POST /api/v1/clearing-systems/{id}/test
   POST /api/v1/clearing-systems/{id}/activate
   POST /api/v1/clearing-systems/{id}/deactivate
   ```

5. React Guidance:
   ```tsx
   // Use React Hook Form for wizard
   import { useForm, FormProvider } from 'react-hook-form';
   
   function ClearingSystemOnboardingWizard() {
     const [activeStep, setActiveStep] = useState(0);
     const methods = useForm<ClearingSystemConfiguration>();
     
     const steps = [
       'Select Clearing System',
       'Communication Pattern',
       'Security Configuration',
       'Retry Policy',
       'Review & Test',
     ];
     
     return (
       <FormProvider {...methods}>
         <Stepper activeStep={activeStep}>
           {steps.map((label) => (
             <Step key={label}><StepLabel>{label}</StepLabel></Step>
           ))}
         </Stepper>
         {renderStepContent(activeStep)}
       </FormProvider>
     );
   }
   ```

6. Step 1: Clearing System Selector:
   ```tsx
   // Display 5 clearing systems as cards
   const clearingSystems = [
     {
       value: 'SAMOS',
       label: 'SAMOS (RTGS)',
       description: 'Real-Time Gross Settlement by SARB',
       icon: <BankIcon />,
       pattern: 'SYNCHRONOUS',
       format: 'ISO_20022_XML',
       security: 'MTLS',
       recommended: true,
     },
     // ... other systems
   ];
   
   return (
     <Grid container spacing={2}>
       {clearingSystems.map(system => (
         <Grid item xs={12} sm={6} md={4} key={system.value}>
           <Card onClick={() => handleSelect(system)}>
             {system.icon}
             <Typography variant="h6">{system.label}</Typography>
             {system.recommended && <Chip label="Recommended" />}
           </Card>
         </Grid>
       ))}
     </Grid>
   );
   ```

7. Step 3: Security Configuration (Dynamic):
   ```tsx
   // mTLS Configuration
   function MtlsConfig() {
     return (
       <Stack spacing={2}>
         <Typography>Upload Client Certificate (.p12 or .pfx)</Typography>
         <input type="file" accept=".p12,.pfx" onChange={handleCertUpload} />
         <TextField type="password" label="Certificate Password" />
         <FormControlLabel
           control={<Switch />}
           label="Enable Digital Signature (XMLDSig)"
         />
       </Stack>
     );
   }
   
   // OAuth 2.0 Configuration
   function OAuth2Config() {
     return (
       <Stack spacing={2}>
         <TextField label="Token Endpoint URL" placeholder="https://auth.payshap.co.za/oauth/token" />
         <TextField label="Client ID" />
         <TextField type="password" label="Client Secret" />
         <FormControlLabel
           control={<Switch />}
           label="Enable mTLS (in addition to OAuth 2.0)"
         />
       </Stack>
     );
   }
   
   // SFTP + PGP Configuration
   function SftpKeyConfig() {
     return (
       <Stack spacing={2}>
         <Typography>Upload SSH Private Key (id_rsa)</Typography>
         <input type="file" accept=".pem,.key,id_rsa" />
         <Typography>Upload PGP Public Key (BankservAfrica's key)</Typography>
         <input type="file" accept=".asc,.pub" />
       </Stack>
     );
   }
   ```

8. Step 4: Retry Policy Configuration:
   ```tsx
   function RetryPolicyConfig() {
     const clearingSystem = watch('clearingSystem');
     
     const getRecommendedRetry = (system: string) => {
       const recommendations = {
         SAMOS: { attempts: 0, backoff: 'NONE', reason: 'RTGS - no retries allowed' },
         BANKSERV_EFT: { attempts: 3, backoff: 'EXPONENTIAL', reason: 'Batch processing allows retries' },
         BANKSERV_RTC: { attempts: 1, backoff: 'NONE', reason: 'Real-time - single retry only' },
         PAYSHAP: { attempts: 0, backoff: 'NONE', reason: 'Instant - no retries' },
         SWIFT: { attempts: 3, backoff: 'EXPONENTIAL', reason: 'Asynchronous - retries recommended' },
       };
       return recommendations[system];
     };
     
     const recommended = getRecommendedRetry(clearingSystem);
     
     return (
       <Stack spacing={2}>
         <Alert severity="info">
           <strong>Recommended for {clearingSystem}</strong>: {recommended.attempts} retries with {recommended.backoff} backoff
           <br />
           <Typography variant="caption">{recommended.reason}</Typography>
         </Alert>
         
         <FormControlLabel
           control={<Switch />}
           label="Enable Retry on Failure"
         />
         
         <TextField
           label="Maximum Retry Attempts"
           type="number"
           defaultValue={recommended.attempts}
         />
         
         <Select label="Backoff Strategy" defaultValue={recommended.backoff}>
           <MenuItem value="NONE">None (Immediate retry)</MenuItem>
           <MenuItem value="LINEAR">Linear (1s, 2s, 3s, 4s)</MenuItem>
           <MenuItem value="EXPONENTIAL">Exponential (1s, 2s, 4s, 8s)</MenuItem>
         </Select>
       </Stack>
     );
   }
   ```

**Task**:
  Build the Clearing System Onboarding UI for self-service clearing system configuration. This allows tenants to configure integration with South African clearing systems (SAMOS, BankservAfrica, RTC, PayShap, SWIFT) without backend team involvement.

**Implementation Requirements**:

1. **5-Step Wizard**:
   - ✅ Step 1: Select Clearing System (5 options: SAMOS, BankservAfrica, RTC, PayShap, SWIFT)
   - ✅ Step 2: Communication Pattern (Sync, Async, Batch)
   - ✅ Step 3: Security Configuration (dynamic form based on selected mechanism)
   - ✅ Step 4: Retry Policy (count, backoff strategy, timeout)
   - ✅ Step 5: Review & Test (display config, test connection button)

2. **Dynamic Security Forms**:
   - ✅ mTLS: Upload certificate (.p12), password, digital signature toggle
   - ✅ OAuth 2.0: Token endpoint, client ID, client secret, mTLS toggle
   - ✅ SFTP: SSH key upload, PGP public key upload
   - ✅ SWIFTNet PKI: SWIFT certificate upload

3. **Recommended Defaults**:
   - ✅ Pre-populate recommended values based on selected clearing system
   - ✅ Show info alerts with recommended retry policies
   - ✅ Validate configuration before saving

4. **Test Connection**:
   - ✅ Test button (sends test message to clearing system)
   - ✅ Display success/failure result
   - ✅ Show response time

5. **Clearing System Management**:
   - ✅ List all clearing system configurations in a table
   - ✅ Edit button (opens wizard in edit mode)
   - ✅ Activate/Deactivate button
   - ✅ Delete button (with confirmation)

**Definition of Done**:
- ✅ All UI components implemented
- ✅ Unit tests (React Testing Library)
- ✅ E2E tests (Cypress)
- ✅ Certificate upload functionality
- ✅ Test connection functionality
- ✅ README with onboarding guide

---

## Conclusion

### ✅ Context Sufficiency: COMPLETE

**All 52 features (40 original + 12 Phase 7) have SUFFICIENT context** for AI agents to independently build:
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
