# AI Agent Task Breakdown & Orchestration Plan

## Overview
This document provides a detailed breakdown of tasks for AI coding agents to build the payments engine in a modular, parallelizable manner. Each task is designed to be completed independently with minimal context.

---

## AI Agent Development Philosophy

### Core Principles
1. **Single Responsibility**: Each agent builds ONE microservice
2. **Clear Contracts**: All interfaces defined upfront (OpenAPI/AsyncAPI)
3. **Mock Dependencies**: Agents use mocks for external dependencies
4. **Self-Validation**: Each agent includes unit tests (80%+ coverage)
5. **Documentation**: Each agent creates README with setup instructions

### Agent Constraints
- **Max Context**: 500 lines of core business logic per service
- **Build Time**: 2-4 hours per service
- **Dependencies**: Only interfaces, no actual implementations
- **Testing**: Comprehensive unit tests, basic integration tests

---

## Development Phases

### Phase 0: Foundation Setup (Master Agent)
**Duration**: 2 hours  
**Dependencies**: None

#### Task 0.1: Initialize Project Structure
```bash
payments-engine/
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── infrastructure/
│   ├── terraform/
│   └── kubernetes/
├── shared/
│   ├── common-lib/
│   ├── event-contracts/
│   └── api-contracts/
├── services/
│   ├── payment-initiation/
│   ├── validation/
│   └── [... other services]
├── frontend/
│   └── web-portal/
├── docs/
├── docker-compose.yml
└── README.md
```

**Deliverables**:
- Project structure created
- Maven/Gradle parent POM
- Common dependencies configured
- CI/CD pipeline skeleton

**AI Agent Instructions**:
```yaml
task: "Initialize multi-module Spring Boot project"
structure:
  - Create parent pom.xml with Spring Boot 3.x
  - Define common dependencies (Spring Web, JPA, Actuator, etc.)
  - Setup Maven modules structure
  - Create .gitignore for Java/Maven projects
  - Create docker-compose.yml for local development
validation:
  - mvn clean install should succeed
  - All modules should compile
```

---

#### Task 0.2: Create Common Libraries
**Assigned to**: Agent Foundation-1

**Deliverables**:
- Error handling framework
- Logging framework
- Event publishing utilities
- API response wrappers
- Common DTOs

**Code to Generate**:

1. **Error Handling** (`common-lib/src/main/java/com/payments/common/exception/`)
```java
// GlobalExceptionHandler.java
// PaymentException.java
// ErrorResponse.java
// ErrorCode.java enum
```

2. **Event Publishing** (`common-lib/src/main/java/com/payments/common/events/`)
```java
// EventPublisher.java interface
// AzureServiceBusEventPublisher.java implementation
// EventMetadata.java
// Event.java base class
```

3. **API Response** (`common-lib/src/main/java/com/payments/common/api/`)
```java
// ApiResponse.java
// PagedResponse.java
// ErrorResponse.java
```

**AI Agent Instructions**:
```yaml
task: "Create common utilities library"
requirements:
  - Spring Boot 3.x compatible
  - Include GlobalExceptionHandler with @ControllerAdvice
  - Include EventPublisher interface with Azure Service Bus implementation
  - Include standard API response wrappers
  - Add comprehensive JavaDoc
  - Minimum 80% test coverage
dependencies:
  - Spring Boot Starter
  - Azure Service Bus SDK
  - Lombok
testing:
  - Unit tests for all utilities
  - Mock Azure Service Bus in tests
```

---

#### Task 0.3: Define Event Contracts
**Assigned to**: Agent Foundation-2

**Deliverables**:
- Event POJOs for all events (from 03-EVENT-SCHEMAS.md)
- Jackson serialization configuration
- Event builders
- Event validation

**Code to Generate** (`event-contracts/src/main/java/com/payments/events/`):
```java
// payment/PaymentInitiatedEvent.java
// payment/PaymentValidatedEvent.java
// payment/PaymentCompletedEvent.java
// [... all other events]
// EventMetadata.java
// EventType.java enum
```

**AI Agent Instructions**:
```yaml
task: "Generate event contract classes from AsyncAPI spec"
input: "docs/03-EVENT-SCHEMAS.md"
requirements:
  - Create POJO for each event in AsyncAPI
  - Use Lombok @Data, @Builder annotations
  - Add Jackson annotations for JSON serialization
  - Add JSR-303 validation annotations
  - Include EventMetadata in all events
testing:
  - Test JSON serialization/deserialization
  - Test validation constraints
```

---

#### Task 0.4: Define API Contracts
**Assigned to**: Agent Foundation-3

**Deliverables**:
- OpenAPI 3.0 specifications for all services
- Generated API clients (OpenAPI Generator)
- Request/Response DTOs

**AI Agent Instructions**:
```yaml
task: "Create OpenAPI specifications and generate clients"
requirements:
  - OpenAPI 3.0 spec for each service
  - Use openapi-generator-maven-plugin
  - Generate Java client stubs
  - Include request/response DTOs
  - Add JSR-303 validation
validation:
  - Specs validate with swagger-cli
  - Generated code compiles
  - Include example requests/responses
```

---

#### Task 0.5: Setup Infrastructure as Code
**Assigned to**: Agent Foundation-4

**Deliverables**:
- Terraform scripts for Azure resources
- Kubernetes manifests for services
- Helm charts

**Azure Resources to Create**:
- AKS cluster
- Azure Service Bus namespace (topics/subscriptions)
- Azure PostgreSQL Flexible Servers
- Azure Cache for Redis
- Azure Key Vault
- Azure Application Insights

**AI Agent Instructions**:
```yaml
task: "Create Terraform and Kubernetes configurations"
requirements:
  - Terraform for Azure resource provisioning
  - Kubernetes manifests for each microservice
  - Helm chart for entire application
  - Use variables for environment-specific values
  - Include README with deployment instructions
outputs:
  - infrastructure/terraform/main.tf
  - infrastructure/kubernetes/deployments/
  - infrastructure/helm/payments-engine/
```

---

### Phase 1: Independent Services (Parallel Execution)
**Duration**: 2-4 hours per agent  
**Dependencies**: Phase 0 complete

These services have NO dependencies on other services and can be built completely in parallel.

---

#### Task 1.1: Account Adapter Service
**Assigned to**: Agent Service-1  
**Priority**: High  
**Estimated Time**: 3 hours

**Service Specification**:
- **Purpose**: Orchestrate calls to external core banking systems (Current, Savings, Investment, Cards, Loans, etc.)
- **Database**: PostgreSQL (account_routing, backend_systems, api_call_log tables) + Redis (caching)
- **API Endpoints**: 7 REST endpoints (proxy to backend systems)
- **Events Published**: FundsReservedEvent, InsufficientFundsEvent
- **Events Consumed**: None
- **Key Responsibility**: Acts as an adapter/orchestrator, NOT a system of record

**Folder Structure**:
```
services/account-service/
├── src/
│   ├── main/
│   │   ├── java/com/payments/account/
│   │   │   ├── AccountServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── AccountController.java
│   │   │   ├── service/
│   │   │   │   ├── AccountService.java
│   │   │   │   └── AccountServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   ├── AccountRepository.java
│   │   │   │   └── AccountHoldRepository.java
│   │   │   ├── model/
│   │   │   │   ├── Account.java
│   │   │   │   └── AccountHold.java
│   │   │   ├── dto/
│   │   │   │   ├── AccountDTO.java
│   │   │   │   ├── CreateHoldRequest.java
│   │   │   │   └── VerifyAccountRequest.java
│   │   │   └── config/
│   │   │       └── DatabaseConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           └── V1__init_schema.sql
│   └── test/
│       └── java/com/payments/account/
│           ├── AccountServiceTest.java
│           ├── AccountControllerTest.java
│           └── AccountRepositoryTest.java
├── Dockerfile
├── pom.xml
└── README.md
```

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-1"
task: "Build Account Adapter Service microservice"
reference_documents:
  - docs/02-MICROSERVICES-BREAKDOWN.md (Section 3)
  - docs/01-ASSUMPTIONS.md (Section 1.5 - External Core Banking Systems)

requirements:
  service_name: "account-adapter-service"
  port: 8081
  database: 
    type: "PostgreSQL + Redis"
    tables: ["account_routing", "backend_systems", "account_cache", "api_call_log", "circuit_breaker_state"]
  
  external_systems:
    - Current Accounts System (CURRENT, CHEQUE)
    - Savings System (SAVINGS, MONEY_MARKET)
    - Investment System (INVESTMENT, UNIT_TRUST)
    - Card System (CREDIT_CARD, DEBIT_CARD)
    - Home Loan System (HOME_LOAN, MORTGAGE)
    - Car Loan System (CAR_LOAN, VEHICLE_FINANCE)
    - Personal Loan System (PERSONAL_LOAN)
    - Business Banking (BUSINESS_CURRENT, BUSINESS_SAVINGS)
  
  api_endpoints:
    - "GET /api/v1/accounts/{accountNumber}" (proxy to backend)
    - "POST /api/v1/accounts/{accountNumber}/debit" (proxy to backend)
    - "POST /api/v1/accounts/{accountNumber}/credit" (proxy to backend)
    - "POST /api/v1/accounts/{accountNumber}/holds" (proxy to backend)
    - "DELETE /api/v1/accounts/holds/{holdId}" (proxy to backend)
    - "POST /api/v1/accounts/verify" (proxy to backend)
    - "GET /api/v1/accounts/route/{accountNumber}" (routing info)
  
  business_logic:
    - Route account requests to appropriate backend system based on account routing table
    - Handle circuit breaking for backend system failures
    - Cache account data to reduce backend calls (TTL: 30 seconds)
    - Log all API calls to external systems for audit
    - Support idempotency for debit/credit operations
    - Aggregate responses from multiple backend systems if needed
    - **IMPORTANT**: DO NOT store account balances or transaction data
  
  backend_integration:
    - Use RestTemplate with OAuth 2.0 client credentials
    - Configure circuit breaker with Resilience4j
    - Add retry logic (3 attempts, exponential backoff)
    - Timeout: 5 seconds for backend calls
    - Add idempotency headers (Idempotency-Key)
  
  events_to_publish:
    - "FundsReservedEvent" (when hold placed via backend)
    - "InsufficientFundsEvent" (when backend returns insufficient funds)
  
  technology_stack:
    - Spring Boot 3.x
    - Spring Cloud (Circuit Breaker, Resilience4j)
    - Spring Data JPA (for routing metadata)
    - PostgreSQL driver
    - Redis (Spring Cache)
    - RestTemplate / WebClient
    - OAuth 2.0 Client
    - Flyway (database migrations)
    - Azure Service Bus SDK (event publishing)
    - Spring Boot Actuator
    - Spring Boot Test
    - WireMock (mock backend systems in tests)
  
  validation_rules:
    - Account number: 10-20 digits
    - Amount: positive, max 999999999.99
    - Backend system must be active and healthy
  
  caching:
    - Cache account data (TTL: 30 seconds)
    - Cache routing metadata (TTL: 5 minutes)
    - Use Redis for distributed cache
  
  error_handling:
    - AccountNotFoundException
    - BackendSystemUnavailableException
    - CircuitBreakerOpenException
    - BackendTimeoutException
    - InvalidRoutingException
  
  circuit_breaker_config:
    - Failure threshold: 5 failures
    - Wait duration: 60 seconds
    - Ring buffer size: 10
    - Fallback: return cached data if available
  
  testing_requirements:
    - Unit tests for service layer (all business logic)
    - Mock backend system calls using WireMock
    - Test circuit breaker behavior
    - Test retry logic
    - Test caching
    - Controller tests (MockMvc)
    - Minimum 80% code coverage
    - Test scenarios:
      - Successful backend call (debit/credit)
      - Backend system timeout
      - Backend system returns error
      - Circuit breaker open (fallback to cache)
      - Idempotency (duplicate request)
      - Routing to correct backend system
  
  documentation:
    - README.md with:
      - Service description (emphasize adapter role)
      - List of external backend systems
      - API endpoints (brief)
      - How to run locally
      - How to configure backend systems
      - How to run tests
      - Environment variables required
    - OpenAPI spec generation (springdoc-openapi)
    - Inline JavaDoc for public methods

deliverables:
  - Complete Spring Boot microservice
  - Database migration scripts (Flyway)
  - REST client configurations for each backend system
  - Circuit breaker configuration
  - Unit and integration tests (with WireMock mocks)
  - Dockerfile
  - README.md
  - OpenAPI specification (auto-generated)

validation_checklist:
  - "mvn clean install" succeeds
  - All tests pass
  - Service starts without errors
  - Health endpoint accessible: GET /actuator/health
  - OpenAPI UI accessible: GET /swagger-ui.html
  - Can route requests to mock backend systems
  - Circuit breaker opens after failures
  - Caching works correctly
  - Idempotency prevents duplicate backend calls

estimated_time: "3 hours"
complexity: "MEDIUM"
```

**Sample Data for Testing**:
```sql
-- Backend system configuration
INSERT INTO backend_systems (system_id, system_name, base_url, auth_type, timeout_ms, is_active, health_status)
VALUES 
  ('CURRENT_ACCOUNTS', 'Current Accounts System', 'https://current-accounts.bank.internal/api/v1', 'OAUTH2', 5000, TRUE, 'HEALTHY'),
  ('SAVINGS', 'Savings System', 'https://savings.bank.internal/api/v1', 'OAUTH2', 5000, TRUE, 'HEALTHY'),
  ('INVESTMENTS', 'Investment System', 'https://investments.bank.internal/api/v1', 'OAUTH2', 5000, TRUE, 'HEALTHY');

-- Account routing
INSERT INTO account_routing (account_number, backend_system, account_type, base_url, is_active)
VALUES 
  ('1234567890', 'CURRENT_ACCOUNTS', 'CURRENT', 'https://current-accounts.bank.internal/api/v1', TRUE),
  ('0987654321', 'SAVINGS', 'SAVINGS', 'https://savings.bank.internal/api/v1', TRUE),
  ('1122334455', 'INVESTMENTS', 'INVESTMENT', 'https://investments.bank.internal/api/v1', TRUE);
```

**WireMock Stub for Testing**:
```java
// Mock backend system response
stubFor(get(urlEqualTo("/api/v1/accounts/1234567890"))
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"accountNumber\":\"1234567890\",\"balance\":10000.00,\"status\":\"ACTIVE\"}")));
```

---

#### Task 1.2: Notification Service
**Assigned to**: Agent Service-2  
**Priority**: Medium  
**Estimated Time**: 2 hours

**Service Specification**:
- **Purpose**: Send SMS, Email, Push notifications
- **Database**: PostgreSQL (notifications, notification_templates)
- **API Endpoints**: 3 REST endpoints
- **Events Consumed**: PaymentCompletedEvent, PaymentFailedEvent

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-2"
task: "Build Notification Service microservice"

requirements:
  service_name: "notification-service"
  port: 8082
  
  notification_channels:
    - SMS (mock Twilio API)
    - Email (mock SendGrid API)
    - Push (mock Azure Notification Hub)
  
  api_endpoints:
    - "POST /api/v1/notifications/send"
    - "GET /api/v1/notifications/{notificationId}"
    - "GET /api/v1/notifications/templates"
  
  event_consumers:
    - PaymentCompletedEvent → Send success notification
    - PaymentFailedEvent → Send failure notification
  
  templates:
    - PAYMENT_SUCCESS: "Your payment of R{amount} to {recipient} was successful. Ref: {paymentId}"
    - PAYMENT_FAILED: "Your payment of R{amount} failed. Reason: {reason}. Ref: {paymentId}"
  
  business_logic:
    - Template variable substitution
    - Retry logic for failed notifications (3 attempts)
    - Delivery status tracking
    - Rate limiting per user (max 10 SMS per hour)
  
  testing:
    - Mock external APIs (Twilio, SendGrid, Azure)
    - Test template rendering
    - Test event consumption
    - Test retry logic

deliverables:
  - Complete Spring Boot microservice
  - Event consumers
  - Unit and integration tests
  - Dockerfile
  - README.md

estimated_time: "2 hours"
complexity: "LOW"
```

---

#### Task 1.3: Routing Service
**Assigned to**: Agent Service-3  
**Priority**: High  
**Estimated Time**: 2 hours

**Service Specification**:
- **Purpose**: Determine payment routing (SAMOS/RTC/ACH)
- **Database**: Redis (routing rules cache)
- **API Endpoints**: 2 REST endpoints
- **Events Consumed**: PaymentValidatedEvent
- **Events Published**: RoutingDeterminedEvent

**Routing Rules**:
1. Amount > R5,000,000 → SAMOS (RTGS)
2. Amount <= R5,000,000 AND High Priority → RTC
3. Amount <= R5,000,000 AND Normal Priority → ACH/EFT
4. After 15:30 CAT → RTC only
5. Same bank → Internal transfer

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-3"
task: "Build Routing Service microservice"

requirements:
  service_name: "routing-service"
  port: 8083
  
  routing_engine:
    - Rule-based engine (use Drools or custom)
    - Rules loaded from database/configuration
    - Rules cached in Redis
  
  api_endpoints:
    - "POST /api/v1/routing/determine"
    - "GET /api/v1/routing/rules"
  
  business_logic:
    - Evaluate rules in priority order
    - Consider amount, time, priority, destination bank
    - Return routing decision with estimated completion time
  
  testing:
    - Test all routing rules
    - Test rule priority
    - Test caching
    - Test edge cases (boundary amounts, cutoff times)

deliverables:
  - Complete Spring Boot microservice
  - Routing rules engine
  - Unit and integration tests
  - Dockerfile
  - README.md

estimated_time: "2 hours"
complexity: "MEDIUM"
```

---

#### Task 1.4: Reporting Service
**Assigned to**: Agent Service-4  
**Priority**: Low  
**Estimated Time**: 3 hours

**Service Specification**:
- **Purpose**: Generate transaction reports and analytics
- **Database**: PostgreSQL + Azure Synapse (read from data warehouse)
- **API Endpoints**: 4 REST endpoints
- **Report Types**: Transaction Summary, Compliance Report, Analytics Dashboard

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-4"
task: "Build Reporting Service microservice"

requirements:
  service_name: "reporting-service"
  port: 8084
  
  report_types:
    - TRANSACTION_SUMMARY: Daily/Weekly/Monthly transaction summary
    - COMPLIANCE_REPORT: Regulatory reports (SARB, FICA)
    - ANALYTICS_DASHBOARD: Real-time metrics
  
  export_formats:
    - PDF (use JasperReports)
    - Excel (use Apache POI)
    - CSV
  
  business_logic:
    - Asynchronous report generation
    - Store reports in Azure Blob Storage
    - Generate download links with expiry
  
  testing:
    - Test report generation for all types
    - Test export formats
    - Mock data warehouse queries

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

### Phase 2: Core Payment Services (Sequential with some parallelism)
**Duration**: 3-4 hours per agent  
**Dependencies**: Phase 1 complete

---

#### Task 2.1: Payment Initiation Service
**Assigned to**: Agent Service-5  
**Priority**: Critical  
**Estimated Time**: 3 hours  
**Dependencies**: Account Service (for balance checks)

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-5"
task: "Build Payment Initiation Service microservice"

requirements:
  service_name: "payment-initiation-service"
  port: 8085
  
  api_endpoints:
    - "POST /api/v1/payments"
    - "GET /api/v1/payments/{paymentId}"
    - "GET /api/v1/payments" (list with pagination)
  
  business_logic:
    - Generate unique payment ID (PAY-YYYY-XXXXXXXXXX)
    - Idempotency using idempotencyKey
    - Basic validation (field presence, format)
    - Publish PaymentInitiatedEvent
    - Store payment request in database
  
  validations:
    - Source and destination accounts must be different
    - Amount must be positive
    - Currency must be ZAR
    - Reference max 200 characters
  
  dependencies:
    - Account Service: Mock for now (will integrate later)
  
  testing:
    - Test payment creation
    - Test idempotency (duplicate request)
    - Test pagination
    - Test event publishing
    - Test validation errors

deliverables:
  - Complete Spring Boot microservice
  - Database schema and migrations
  - Unit and integration tests
  - Dockerfile
  - README.md

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

#### Task 2.2: Validation Service
**Assigned to**: Agent Service-6  
**Priority**: Critical  
**Estimated Time**: 3 hours  
**Dependencies**: Account Service

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-6"
task: "Build Validation Service microservice"

requirements:
  service_name: "validation-service"
  port: 8086
  
  validation_rules:
    - Daily limit: R50,000 per account
    - Single transaction: R5,000,000 max for RTC
    - Account status: ACTIVE only
    - KYC status: VERIFIED only
    - FICA status: COMPLIANT only
    - Fraud score: < 0.7
    - Velocity check: Max 10 transactions/hour
  
  integrations:
    - Account Service: Check account status
    - Fraud API: Mock external fraud detection service
  
  business_logic:
    - Evaluate all rules
    - Calculate fraud score
    - Determine risk level (LOW, MEDIUM, HIGH, CRITICAL)
    - Publish PaymentValidatedEvent or ValidationFailedEvent
  
  caching:
    - Cache validation rules in Redis (TTL: 5 minutes)
  
  testing:
    - Test each validation rule
    - Test fraud score calculation
    - Test caching
    - Test integration with Account Service (mock)

estimated_time: "3 hours"
complexity: "HIGH"
```

---

#### Task 2.3: Transaction Processing Service
**Assigned to**: Agent Service-7  
**Priority**: Critical  
**Estimated Time**: 4 hours  
**Dependencies**: Account Service, Settlement Service

**Service Specification**:
- **Purpose**: Create transactions, manage state machine, event sourcing
- **Database**: PostgreSQL (transactions, transaction_events, ledger_entries)
- **Pattern**: Event Sourcing + CQRS

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-7"
task: "Build Transaction Processing Service microservice"

requirements:
  service_name: "transaction-processing-service"
  port: 8087
  
  state_machine:
    states: [CREATED, VALIDATED, PROCESSING, CLEARING, COMPLETED, FAILED, COMPENSATING, REVERSED]
    transitions:
      - CREATED → VALIDATED
      - VALIDATED → PROCESSING
      - PROCESSING → CLEARING
      - CLEARING → COMPLETED
      - Any → FAILED
      - FAILED → COMPENSATING
      - COMPENSATING → REVERSED
  
  event_sourcing:
    - Store all state changes as events
    - Rebuild state from events
    - Support temporal queries
  
  double_entry_bookkeeping:
    - Every transaction creates two ledger entries
    - Debit entry: source account
    - Credit entry: destination account
    - Balance verification
  
  testing:
    - Test state machine transitions
    - Test event sourcing (replay events)
    - Test double-entry bookkeeping
    - Test compensation logic

estimated_time: "4 hours"
complexity: "HIGH"
```

---

### Phase 3: Clearing Adapters (Parallel)
**Duration**: 3 hours per agent  
**Dependencies**: Phase 2 complete

---

#### Task 3.1: SAMOS Clearing Adapter
**Assigned to**: Agent Service-8  
**Priority**: High  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-8"
task: "Build SAMOS Clearing Adapter microservice"

requirements:
  service_name: "clearing-adapter-samos"
  port: 8088
  
  message_format: "ISO 20022 (pacs.008, pacs.002)"
  protocol: "SWIFT (mock for now)"
  
  business_logic:
    - Convert transaction to ISO 20022 format
    - Send to SAMOS (mock SWIFT connection)
    - Wait for acknowledgment
    - Parse response
    - Publish ClearingSubmittedEvent, ClearingCompletedEvent
  
  operating_hours:
    - 08:00-15:30 CAT
    - Reject submissions outside hours
  
  testing:
    - Test ISO 20022 message generation
    - Test message parsing
    - Test operating hours check
    - Mock SWIFT connection

estimated_time: "3 hours"
complexity: "HIGH"
```

---

#### Task 3.2: BankservAfrica Clearing Adapter
**Assigned to**: Agent Service-9  
**Priority**: High  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-9"
task: "Build BankservAfrica Clearing Adapter microservice"

requirements:
  service_name: "clearing-adapter-bankserv"
  port: 8089
  
  message_format: "ISO 8583 + Proprietary"
  protocol: "TCP/IP (mock for now)"
  
  batch_cutoffs: ["08:00", "10:00", "12:00", "14:00"]
  
  business_logic:
    - Convert transaction to BankservAfrica format
    - Batch transactions until cutoff
    - Send batch to BankservAfrica
    - Handle batch acknowledgment
  
  testing:
    - Test message formatting
    - Test batch creation
    - Test cutoff timing
    - Mock TCP connection

estimated_time: "3 hours"
complexity: "HIGH"
```

---

#### Task 3.3: RTC Clearing Adapter
**Assigned to**: Agent Service-10  
**Priority**: High  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-10"
task: "Build RTC Clearing Adapter microservice"

requirements:
  service_name: "clearing-adapter-rtc"
  port: 8090
  
  message_format: "ISO 20022"
  protocol: "REST API (mock for now)"
  availability: "24/7/365"
  response_time: "< 10 seconds"
  
  business_logic:
    - Convert transaction to ISO 20022
    - Send to RTC API
    - Wait for response (max 10 seconds)
    - Handle timeout
    - Publish events
  
  testing:
    - Test ISO 20022 generation
    - Test API call
    - Test timeout handling
    - Mock RTC API

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

### Phase 4: Supporting Services
**Duration**: 3 hours per agent  
**Dependencies**: Phase 3 complete

---

#### Task 4.1: Settlement Service
**Assigned to**: Agent Service-11  
**Priority**: High  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-11"
task: "Build Settlement Service microservice"

requirements:
  service_name: "settlement-service"
  port: 8091
  
  business_logic:
    - Calculate net settlement positions
    - Create settlement batches
    - Generate settlement files
    - Submit to clearing systems
    - Track settlement status
  
  settlement_frequency:
    - SAMOS: Real-time
    - RTC: Real-time (T+0)
    - ACH: Next day (T+1)
  
  testing:
    - Test settlement calculation
    - Test batch creation
    - Test file generation

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

#### Task 4.2: Reconciliation Service
**Assigned to**: Agent Service-12  
**Priority**: Medium  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-12"
task: "Build Reconciliation Service microservice"

requirements:
  service_name: "reconciliation-service"
  port: 8092
  
  business_logic:
    - Match internal transactions with clearing responses
    - Identify discrepancies (amount mismatch, missing transactions)
    - Generate exception reports
    - Support manual resolution
  
  reconciliation_types:
    - Daily reconciliation (end of day)
    - Real-time reconciliation (per transaction)
    - Manual reconciliation (ad-hoc)
  
  testing:
    - Test matching logic
    - Test exception detection
    - Test reconciliation reports

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

### Phase 5: Orchestration (Critical)
**Duration**: 4 hours  
**Dependencies**: All core services complete

---

#### Task 5.1: Saga Orchestrator Service
**Assigned to**: Agent Service-13  
**Priority**: Critical  
**Estimated Time**: 4 hours  
**Dependencies**: ALL Phase 2 and Phase 3 services

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-13"
task: "Build Saga Orchestrator Service microservice"

requirements:
  service_name: "saga-orchestrator-service"
  port: 8093
  
  saga_pattern: "Orchestration-based (not choreography)"
  
  saga_definition:
    name: "PaymentSaga"
    steps:
      1. ValidatePayment (ValidationService)
      2. ReserveFunds (AccountService)
      3. DetermineRouting (RoutingService)
      4. CreateTransaction (TransactionProcessingService)
      5. SubmitToClearing (ClearingAdapterService)
      6. ProcessSettlement (SettlementService)
      7. SendNotification (NotificationService)
    
    compensations:
      7. None
      6. ReverseSettlement
      5. CancelClearing
      4. CancelTransaction
      3. None
      2. ReleaseFunds
      1. None
  
  state_management:
    - Store saga state in PostgreSQL
    - Track current step
    - Store step results
    - Handle timeouts (30 seconds per step)
  
  business_logic:
    - Execute steps sequentially
    - On failure, trigger compensation (reverse order)
    - Publish saga events
    - Handle retries (3 attempts per step)
  
  testing:
    - Test happy path (all steps succeed)
    - Test failure scenarios (each step fails)
    - Test compensation logic
    - Test timeouts
    - Test concurrent sagas

deliverables:
  - Complete Spring Boot microservice
  - Saga state machine implementation
  - Compensation logic
  - Unit and integration tests
  - Dockerfile
  - README.md

estimated_time: "4 hours"
complexity: "VERY HIGH"
```

---

### Phase 6: Gateway & Security
**Duration**: 3 hours per agent  
**Dependencies**: All services complete

---

#### Task 6.1: API Gateway Service
**Assigned to**: Agent Service-14  
**Priority**: High  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-14"
task: "Build API Gateway using Spring Cloud Gateway"

requirements:
  service_name: "api-gateway"
  port: 8080
  
  features:
    - Request routing to microservices
    - Authentication (OAuth2 JWT)
    - Rate limiting (100 req/min per user)
    - Request/response logging
    - CORS configuration
    - Circuit breaker pattern
  
  routes:
    - /api/v1/payments/** → payment-initiation-service
    - /api/v1/accounts/** → account-service
    - /api/v1/transactions/** → transaction-processing-service
    - [... all other services]
  
  security:
    - Validate JWT tokens
    - Extract user info from token
    - Add user context to requests
  
  testing:
    - Test routing
    - Test rate limiting
    - Test authentication
    - Test circuit breaker

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

#### Task 6.2: IAM Service
**Assigned to**: Agent Service-15  
**Priority**: High  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-15"
task: "Build IAM Service for authentication and authorization"

requirements:
  service_name: "iam-service"
  port: 8094
  
  features:
    - User registration
    - User login (OAuth2 password grant)
    - Token refresh
    - User profile management
    - Role-based access control (RBAC)
  
  integration:
    - Azure AD B2C for identity provider
    - JWT tokens (15-minute expiry)
    - Refresh tokens (7-day expiry)
  
  roles:
    - USER: Initiate payments, view own transactions
    - ADMIN: View all transactions, generate reports
    - OPERATOR: Handle exceptions, manual reconciliation
  
  testing:
    - Test registration and login
    - Test token generation and validation
    - Test RBAC
    - Mock Azure AD B2C

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

#### Task 6.3: Audit Service
**Assigned to**: Agent Service-16  
**Priority**: Medium  
**Estimated Time**: 2 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Service-16"
task: "Build Audit Service for compliance and logging"

requirements:
  service_name: "audit-service"
  port: 8095
  database: "Azure CosmosDB"
  
  audit_events:
    - API_CALL: All API requests/responses
    - STATE_CHANGE: Entity state changes
    - SECURITY_EVENT: Login, logout, auth failures
    - BUSINESS_EVENT: Payment created, completed, failed
  
  business_logic:
    - Consume all events from event bus
    - Store in CosmosDB (append-only)
    - Provide query API for audit trail
  
  retention: "7 years (regulatory requirement)"
  
  testing:
    - Test event consumption
    - Test CosmosDB storage
    - Test query API

estimated_time: "2 hours"
complexity: "LOW"
```

---

### Phase 7: Frontend (Parallel)
**Duration**: 4-6 hours per agent  
**Dependencies**: API Gateway complete

---

#### Task 7.1: Payment Initiation UI
**Assigned to**: Agent Frontend-1  
**Priority**: High  
**Estimated Time**: 4 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Frontend-1"
task: "Build Payment Initiation UI component"

requirements:
  framework: "React 18 + TypeScript"
  ui_library: "Material-UI (MUI)"
  
  pages:
    - Payment Form: Create new payment
      - Fields: Source account, Destination account, Amount, Reference, Payment type
      - Validation: Client-side validation
      - Submit button
    
    - Payment Confirmation: Review before submit
      - Display all details
      - Confirm/Cancel buttons
    
    - Payment Success: Success message
      - Payment ID
      - Transaction details
      - Download receipt button
  
  state_management: "Redux Toolkit"
  api_integration: "RTK Query"
  
  features:
    - Form validation (React Hook Form + Zod)
    - Account lookup (autocomplete)
    - Amount formatting (currency)
    - Error handling (display API errors)
  
  testing:
    - Unit tests (React Testing Library)
    - Integration tests (MSW for API mocking)
    - E2E tests (Playwright)

deliverables:
  - React components
  - Redux slices
  - API hooks (RTK Query)
  - Tests
  - Storybook stories

estimated_time: "4 hours"
complexity: "MEDIUM"
```

---

#### Task 7.2: Transaction History UI
**Assigned to**: Agent Frontend-2  
**Priority**: High  
**Estimated Time**: 3 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Frontend-2"
task: "Build Transaction History UI component"

requirements:
  pages:
    - Transaction List: Paginated list of transactions
      - Filters: Date range, Status, Amount range
      - Sort: By date, amount
      - Search: By reference, payment ID
    
    - Transaction Details: View single transaction
      - All transaction fields
      - Status history (timeline)
      - Download receipt
  
  features:
    - Pagination (Material-UI DataGrid)
    - Real-time updates (WebSocket)
    - Export to CSV/PDF
  
  testing:
    - Test filtering and sorting
    - Test pagination
    - Test real-time updates

estimated_time: "3 hours"
complexity: "MEDIUM"
```

---

#### Task 7.3: Reporting Dashboard UI
**Assigned to**: Agent Frontend-3  
**Priority**: Medium  
**Estimated Time**: 5 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Frontend-3"
task: "Build Reporting Dashboard UI"

requirements:
  pages:
    - Dashboard: Overview metrics
      - Total transactions (today, week, month)
      - Total volume (amount)
      - Success rate
      - Charts: Transaction volume over time
    
    - Report Generator: Create custom reports
      - Select report type
      - Date range
      - Export format
      - Download report
  
  charts: "Recharts or Apache ECharts"
  
  features:
    - Real-time metrics
    - Interactive charts
    - Report scheduling (future)
  
  testing:
    - Test chart rendering
    - Test report generation

estimated_time: "5 hours"
complexity: "MEDIUM"
```

---

#### Task 7.4: Admin Console UI
**Assigned to**: Agent Frontend-4  
**Priority**: Low  
**Estimated Time**: 6 hours

**AI Agent Instructions**:
```yaml
agent_id: "Agent Frontend-4"
task: "Build Admin Console UI"

requirements:
  pages:
    - User Management: CRUD users
    - Role Management: Assign roles
    - Validation Rules: Configure rules
    - Routing Rules: Configure routing
    - Reconciliation: Handle exceptions
    - System Health: Monitor services
  
  features:
    - Role-based access control
    - Audit trail viewer
    - System metrics
  
  testing:
    - Test CRUD operations
    - Test RBAC
    - Test metrics display

estimated_time: "6 hours"
complexity: "HIGH"
```

---

### Phase 8: Integration & Consolidation (Master Agent)
**Duration**: 8-16 hours  
**Dependencies**: All phases complete

---

#### Task 8.1: Integration Testing
**Assigned to**: Master Agent  
**Priority**: Critical  
**Estimated Time**: 6 hours

**Tasks**:
1. **End-to-End Testing**
   - Test complete payment flow (initiation to completion)
   - Test failure scenarios
   - Test compensation logic
   - Test concurrent payments

2. **Performance Testing**
   - Load test with 10,000 TPS
   - Stress test to find breaking points
   - Soak test (24 hours)

3. **Security Testing**
   - OWASP ZAP scan
   - Penetration testing
   - SQL injection tests
   - XSS tests

4. **Compatibility Testing**
   - Browser compatibility (Chrome, Firefox, Safari, Edge)
   - Mobile responsiveness
   - API versioning

**Deliverables**:
- Test reports
- Performance benchmarks
- Security audit report

---

#### Task 8.2: Consolidation & Documentation
**Assigned to**: Master Agent  
**Priority**: Critical  
**Estimated Time**: 4 hours

**Tasks**:
1. **Code Review**
   - Review all microservices
   - Ensure coding standards
   - Check test coverage
   - Verify documentation

2. **Integration Fixes**
   - Fix integration issues
   - Resolve dependency conflicts
   - Align API contracts

3. **Documentation Consolidation**
   - Update architecture diagrams
   - Create deployment guide
   - Create user manual
   - Create API documentation portal

4. **DevOps Setup**
   - Configure CI/CD pipelines
   - Setup monitoring and alerting
   - Configure log aggregation
   - Setup automated backups

**Deliverables**:
- Consolidated codebase
- Complete documentation
- Deployment scripts
- Runbooks

---

#### Task 8.3: Deployment
**Assigned to**: Master Agent  
**Priority**: Critical  
**Estimated Time**: 6 hours

**Tasks**:
1. **Infrastructure Provisioning**
   - Run Terraform scripts
   - Provision Azure resources
   - Configure networking

2. **Application Deployment**
   - Build Docker images
   - Push to container registry
   - Deploy to AKS
   - Configure ingress

3. **Data Migration**
   - Create production databases
   - Run Flyway migrations
   - Load initial data

4. **Smoke Testing**
   - Test all services are running
   - Test health endpoints
   - Test critical paths
   - Verify monitoring

**Deliverables**:
- Production environment
- Deployment logs
- Smoke test results

---

## AI Agent Communication Protocol

### Agent Input Format
Each agent receives:
```json
{
  "agentId": "Agent Service-1",
  "taskId": "1.1",
  "taskDescription": "Build Account Service microservice",
  "referenceDocuments": [
    "docs/02-MICROSERVICES-BREAKDOWN.md",
    "docs/01-ASSUMPTIONS.md"
  ],
  "dependencies": {
    "libraries": ["common-lib", "event-contracts"],
    "services": []
  },
  "requirements": { ... },
  "deliverables": [ ... ],
  "estimatedTime": "2 hours"
}
```

### Agent Output Format
Each agent produces:
```json
{
  "agentId": "Agent Service-1",
  "taskId": "1.1",
  "status": "COMPLETED",
  "artifacts": [
    "services/account-service/",
    "services/account-service/README.md",
    "services/account-service/Dockerfile"
  ],
  "testResults": {
    "unitTests": { "total": 45, "passed": 45, "coverage": 85.5 },
    "integrationTests": { "total": 12, "passed": 12, "coverage": 72.3 }
  },
  "apiSpec": "services/account-service/openapi.yaml",
  "issues": [],
  "completedAt": "2025-10-11T14:30:00Z",
  "actualTime": "1.5 hours"
}
```

---

## Dependency Graph

```
Phase 0 (Foundation)
  ├─ 0.1 → 0.2, 0.3, 0.4, 0.5
  └─ 0.2, 0.3 → All Phase 1 tasks

Phase 1 (Independent Services) - Parallel
  ├─ 1.1 Account Service
  ├─ 1.2 Notification Service
  ├─ 1.3 Routing Service
  └─ 1.4 Reporting Service

Phase 2 (Core Payment Services)
  ├─ 2.1 Payment Initiation (depends on 1.1)
  ├─ 2.2 Validation Service (depends on 1.1)
  └─ 2.3 Transaction Processing (depends on 1.1, 4.1)

Phase 3 (Clearing Adapters) - Parallel
  ├─ 3.1 SAMOS Adapter (depends on 2.3)
  ├─ 3.2 Bankserv Adapter (depends on 2.3)
  └─ 3.3 RTC Adapter (depends on 2.3)

Phase 4 (Supporting Services)
  ├─ 4.1 Settlement Service (depends on Phase 3)
  └─ 4.2 Reconciliation Service (depends on Phase 3)

Phase 5 (Orchestration)
  └─ 5.1 Saga Orchestrator (depends on Phase 2, 3, 4)

Phase 6 (Gateway & Security) - Parallel
  ├─ 6.1 API Gateway (depends on Phase 5)
  ├─ 6.2 IAM Service
  └─ 6.3 Audit Service

Phase 7 (Frontend) - Parallel
  ├─ 7.1 Payment Initiation UI (depends on 6.1)
  ├─ 7.2 Transaction History UI (depends on 6.1)
  ├─ 7.3 Reporting Dashboard UI (depends on 6.1)
  └─ 7.4 Admin Console UI (depends on 6.1)

Phase 8 (Consolidation)
  └─ 8.1, 8.2, 8.3 (depends on all phases)
```

---

## Summary

| Phase | Services | Agents | Total Time | Can Parallelize |
|-------|----------|--------|------------|-----------------|
| 0 | Foundation | 4 | 8h | Partial (2-4, 2-5 parallel) |
| 1 | Independent | 4 | 10h | Yes (all parallel) |
| 2 | Core Payment | 3 | 10h | Partial (2.1, 2.2 parallel) |
| 3 | Clearing | 3 | 9h | Yes (all parallel) |
| 4 | Supporting | 2 | 6h | Yes (both parallel) |
| 5 | Orchestration | 1 | 4h | No |
| 6 | Gateway | 3 | 8h | Yes (all parallel) |
| 7 | Frontend | 4 | 18h | Yes (all parallel) |
| 8 | Consolidation | 1 | 16h | No |
| **Total** | **23 services** | **25 agents** | **89h** | **~40h with parallelism** |

**With maximum parallelization: ~40-50 agent-hours**

---

**Next**: See `05-DATABASE-SCHEMAS.md` for complete database designs
**Next**: See `06-SOUTH-AFRICA-CLEARING.md` for clearing system details
