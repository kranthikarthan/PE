# Microservices Detailed Breakdown

## Purpose
This document provides detailed specifications for each microservice, including responsibilities, APIs, dependencies, and database schemas. Each service is designed to be built independently by AI agents.

---

## Service Matrix Overview

| # | Service Name | Lines of Code | AI Agent Time | Database | Dependencies |
|---|--------------|---------------|---------------|----------|--------------|
| 1 | Payment Initiation | 400 | 3h | PostgreSQL | Validation Service |
| 2 | Validation Service | 450 | 3h | PostgreSQL + Redis | Account Service, Fraud API |
| 3 | Account Service | 350 | 2h | PostgreSQL | - |
| 4 | Routing Service | 300 | 2h | Redis | - |
| 5 | Transaction Processing | 500 | 4h | PostgreSQL | Settlement Service |
| 6 | Clearing Adapter (SAMOS) | 400 | 3h | PostgreSQL | - |
| 7 | Clearing Adapter (Bankserv) | 400 | 3h | PostgreSQL | - |
| 8 | Clearing Adapter (RTC) | 400 | 3h | PostgreSQL | - |
| 9 | Settlement Service | 450 | 3h | PostgreSQL | - |
| 10 | Reconciliation Service | 400 | 3h | PostgreSQL | - |
| 11 | Notification Service | 250 | 2h | PostgreSQL | - |
| 12 | Reporting Service | 350 | 3h | PostgreSQL + Synapse | - |
| 13 | Saga Orchestrator | 500 | 4h | PostgreSQL | All Core Services |
| 14 | API Gateway | 300 | 2h | Redis | - |
| 15 | IAM Service | 400 | 3h | PostgreSQL + Azure AD | - |
| 16 | Audit Service | 300 | 2h | CosmosDB | - |

---

## 1. Payment Initiation Service

### Responsibilities
- Accept payment requests from frontend/API channels
- Generate unique payment IDs
- Perform basic validation (field presence, format)
- Publish `PaymentInitiatedEvent` to event bus
- Store initial payment request

### API Endpoints

```yaml
POST /api/v1/payments
  Description: Initiate a new payment
  Request Body:
    {
      "idempotencyKey": "uuid",
      "sourceAccount": "1234567890",
      "destinationAccount": "0987654321",
      "amount": 1000.00,
      "currency": "ZAR",
      "reference": "Payment for Invoice #123",
      "paymentType": "EFT" | "RTC" | "RTGS",
      "debitOrderDetails": { ... } // optional
    }
  Response: 201 Created
    {
      "paymentId": "PAY-2025-XXXXXX",
      "status": "INITIATED",
      "timestamp": "2025-10-11T10:30:00Z"
    }

GET /api/v1/payments/{paymentId}
  Description: Get payment status
  Response: 200 OK
    {
      "paymentId": "PAY-2025-XXXXXX",
      "status": "PROCESSING",
      "amount": 1000.00,
      "currency": "ZAR",
      "createdAt": "2025-10-11T10:30:00Z",
      "updatedAt": "2025-10-11T10:30:05Z"
    }

GET /api/v1/payments
  Description: List payments (with pagination)
  Query Params: page, size, status, fromDate, toDate
  Response: 200 OK
    {
      "content": [...],
      "totalElements": 1000,
      "totalPages": 10,
      "number": 0
    }
```

### Events Published
```json
{
  "eventType": "PaymentInitiatedEvent",
  "eventId": "evt-uuid",
  "paymentId": "PAY-2025-XXXXXX",
  "timestamp": "2025-10-11T10:30:00Z",
  "payload": {
    "sourceAccount": "1234567890",
    "destinationAccount": "0987654321",
    "amount": 1000.00,
    "currency": "ZAR",
    "paymentType": "EFT"
  }
}
```

### Database Schema
```sql
CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    source_account VARCHAR(50) NOT NULL,
    destination_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference VARCHAR(200),
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    INDEX idx_source_account (source_account),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Web (REST)
- Spring Data JPA
- PostgreSQL
- Azure Service Bus SDK

### AI Agent Instructions
1. Create Spring Boot project with dependencies
2. Implement PaymentInitiationController
3. Implement PaymentService (business logic)
4. Implement PaymentRepository
5. Implement EventPublisher for Azure Service Bus
6. Add validation annotations
7. Write unit tests (80% coverage)
8. Generate OpenAPI spec
9. Create Dockerfile
10. Document in README.md

---

## 2. Validation Service

### Responsibilities
- Validate business rules (daily limits, transaction limits)
- Check account status and KYC compliance
- Integrate with fraud detection system
- Check FICA compliance
- Publish `PaymentValidatedEvent` or `ValidationFailedEvent`

### API Endpoints

```yaml
POST /api/v1/validate/payment
  Description: Validate a payment
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "sourceAccount": "1234567890",
      "destinationAccount": "0987654321",
      "amount": 1000.00,
      "paymentType": "EFT"
    }
  Response: 200 OK
    {
      "valid": true,
      "validationErrors": [],
      "fraudScore": 0.15,
      "riskLevel": "LOW"
    }

GET /api/v1/validate/rules
  Description: Get current validation rules
  Response: 200 OK
    {
      "rules": [
        {
          "ruleId": "RULE-001",
          "name": "Daily transaction limit",
          "type": "LIMIT",
          "value": 50000.00
        }
      ]
    }
```

### Events Consumed
- `PaymentInitiatedEvent`

### Events Published
```json
{
  "eventType": "PaymentValidatedEvent",
  "paymentId": "PAY-2025-XXXXXX",
  "validationResult": {
    "valid": true,
    "fraudScore": 0.15,
    "riskLevel": "LOW"
  }
}
```

### Validation Rules (Examples)
1. **Daily Limit**: Maximum R50,000 per day per account
2. **Single Transaction**: Maximum R5,000,000 for RTC
3. **Account Status**: Must be ACTIVE
4. **KYC Status**: Must be VERIFIED
5. **FICA Status**: Must be COMPLIANT
6. **Fraud Score**: Must be < 0.7 (70%)
7. **Velocity Check**: Max 10 transactions per hour

### Database Schema
```sql
CREATE TABLE validation_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    rule_condition JSONB NOT NULL,
    priority INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE validation_results (
    validation_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    validation_status VARCHAR(20) NOT NULL,
    fraud_score DECIMAL(5,4),
    risk_level VARCHAR(20),
    failed_rules JSONB,
    validated_at TIMESTAMP NOT NULL,
    INDEX idx_payment_id (payment_id)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Redis (caching rules)
- External Fraud API (REST client)

---

## 3. Account Service

### Responsibilities
- Manage account information
- Check account balances
- Place holds/reserves on accounts
- Release holds
- Verify account ownership

### API Endpoints

```yaml
GET /api/v1/accounts/{accountNumber}
  Description: Get account details
  Response: 200 OK
    {
      "accountNumber": "1234567890",
      "accountHolder": "John Doe",
      "accountType": "CURRENT",
      "status": "ACTIVE",
      "balance": 10000.00,
      "availableBalance": 9500.00,
      "currency": "ZAR"
    }

POST /api/v1/accounts/{accountNumber}/holds
  Description: Place a hold on account
  Request Body:
    {
      "amount": 1000.00,
      "reference": "PAY-2025-XXXXXX",
      "expiryMinutes": 30
    }
  Response: 201 Created
    {
      "holdId": "HOLD-XXXXX",
      "status": "PLACED"
    }

DELETE /api/v1/accounts/holds/{holdId}
  Description: Release a hold
  Response: 204 No Content

POST /api/v1/accounts/verify
  Description: Verify account ownership
  Request Body:
    {
      "accountNumber": "1234567890",
      "idNumber": "8001010000000"
    }
  Response: 200 OK
    {
      "verified": true,
      "accountHolder": "John Doe"
    }
```

### Database Schema
```sql
CREATE TABLE accounts (
    account_number VARCHAR(50) PRIMARY KEY,
    account_holder VARCHAR(200) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance DECIMAL(18,2) NOT NULL,
    available_balance DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_status (status)
);

CREATE TABLE account_holds (
    hold_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    reference VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    placed_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    released_at TIMESTAMP,
    INDEX idx_account_number (account_number),
    INDEX idx_expires_at (expires_at),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Redis (caching balances)

---

## 4. Routing Service

### Responsibilities
- Determine payment channel (SAMOS, RTC, ACH/EFT)
- Select appropriate clearing system
- Load balance across clearing connections
- Apply routing rules based on amount, time, destination

### API Endpoints

```yaml
POST /api/v1/routing/determine
  Description: Determine routing for payment
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "amount": 1000.00,
      "destinationBank": "FNB",
      "paymentType": "EFT",
      "priority": "NORMAL" | "HIGH"
    }
  Response: 200 OK
    {
      "clearingSystem": "RTC",
      "channel": "BANKSERV_RTC",
      "estimatedCompletionTime": "2025-10-11T10:35:00Z"
    }
```

### Routing Rules (Examples)
1. **Amount > R5,000,000**: Route to SAMOS (RTGS)
2. **Amount <= R5,000,000 AND Priority = HIGH**: Route to RTC
3. **Amount <= R5,000,000 AND Priority = NORMAL**: Route to ACH/EFT
4. **After 15:30 CAT**: Route to RTC (SAMOS closed)
5. **Destination Bank = Same Bank**: Route to internal transfer

### Database Schema
```sql
CREATE TABLE routing_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    priority INTEGER NOT NULL,
    condition_json JSONB NOT NULL,
    target_system VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE routing_decisions (
    decision_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    selected_system VARCHAR(50) NOT NULL,
    rule_applied VARCHAR(50),
    decided_at TIMESTAMP NOT NULL,
    INDEX idx_payment_id (payment_id)
);
```

### Technology Stack
- Spring Boot 3.x
- Redis (caching routing tables)
- Rule engine (Drools or custom)

---

## 5. Transaction Processing Service

### Responsibilities
- Create transaction records (double-entry bookkeeping)
- Manage transaction state machine
- Update transaction status
- Implement event sourcing for transactions
- Coordinate with settlement service

### API Endpoints

```yaml
POST /api/v1/transactions
  Description: Create a transaction
  Request Body:
    {
      "paymentId": "PAY-2025-XXXXXX",
      "debitAccount": "1234567890",
      "creditAccount": "0987654321",
      "amount": 1000.00,
      "currency": "ZAR",
      "transactionType": "PAYMENT"
    }
  Response: 201 Created
    {
      "transactionId": "TXN-2025-XXXXXX",
      "status": "CREATED"
    }

GET /api/v1/transactions/{transactionId}
  Description: Get transaction details
  Response: 200 OK

PATCH /api/v1/transactions/{transactionId}/status
  Description: Update transaction status
  Request Body:
    {
      "status": "COMPLETED",
      "reason": "Cleared successfully"
    }
```

### Transaction State Machine
```
CREATED → VALIDATED → PROCESSING → CLEARING → COMPLETED
                ↓           ↓           ↓
              FAILED      FAILED     FAILED
                            ↓
                       COMPENSATING → REVERSED
```

### Database Schema
```sql
CREATE TABLE transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    payment_id VARCHAR(50) NOT NULL,
    debit_account VARCHAR(50) NOT NULL,
    credit_account VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status)
);

CREATE TABLE transaction_events (
    event_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    INDEX idx_transaction_id (transaction_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);

CREATE TABLE ledger_entries (
    entry_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    entry_type VARCHAR(10) NOT NULL, -- DEBIT or CREDIT
    amount DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_account_number (account_number)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL (with event store)
- Event sourcing library

---

## 6-8. Clearing Adapter Services (SAMOS, Bankserv, RTC)

### Shared Responsibilities
- Format messages for clearing system (ISO 20022 or ISO 8583)
- Send messages to clearing system
- Receive responses and acknowledgments
- Handle timeouts and retries
- Parse clearing responses

### Common API Endpoints

```yaml
POST /api/v1/clearing/submit
  Description: Submit payment to clearing system
  Request Body:
    {
      "transactionId": "TXN-2025-XXXXXX",
      "paymentDetails": { ... }
    }
  Response: 202 Accepted
    {
      "clearingReference": "CLR-XXXXX",
      "status": "SUBMITTED"
    }

GET /api/v1/clearing/{clearingReference}/status
  Description: Get clearing status
  Response: 200 OK
    {
      "clearingReference": "CLR-XXXXX",
      "status": "COMPLETED",
      "settledAt": "2025-10-11T10:35:00Z"
    }
```

### 6. SAMOS Adapter Specifics
- **Format**: ISO 20022 (pacs.008, pacs.002)
- **Protocol**: SWIFT
- **Threshold**: > R5 million
- **Operating Hours**: 08:00-15:30 CAT

### 7. BankservAfrica Adapter Specifics
- **Format**: Proprietary + ISO 8583
- **Protocol**: TCP/IP
- **Batch Cutoffs**: 08:00, 10:00, 12:00, 14:00 CAT
- **Settlement**: T+0 (RTC), T+1 (ACH)

### 8. RTC Adapter Specifics
- **Format**: ISO 20022
- **Protocol**: REST API
- **Availability**: 24/7/365
- **Response Time**: < 10 seconds

### Common Database Schema
```sql
CREATE TABLE clearing_submissions (
    submission_id VARCHAR(50) PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    clearing_reference VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    request_message TEXT NOT NULL,
    response_message TEXT,
    submitted_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    completed_at TIMESTAMP,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_status (status)
);
```

### Technology Stack
- Spring Boot 3.x
- ISO 20022 library (jaxb)
- ISO 8583 library (jPOS)
- HTTP/TCP clients

---

## 9. Settlement Service

### Responsibilities
- Calculate net settlement positions
- Generate settlement files
- Track settlement batches
- Reconcile with clearing systems
- Update account balances post-settlement

### API Endpoints

```yaml
POST /api/v1/settlement/batches
  Description: Create settlement batch
  Request Body:
    {
      "batchDate": "2025-10-11",
      "clearingSystem": "RTC"
    }
  Response: 201 Created
    {
      "batchId": "BATCH-2025-XXXXXX",
      "status": "PENDING"
    }

GET /api/v1/settlement/batches/{batchId}
  Description: Get settlement batch details

POST /api/v1/settlement/batches/{batchId}/finalize
  Description: Finalize and submit settlement batch

GET /api/v1/settlement/positions
  Description: Get current settlement positions
  Response: 200 OK
    {
      "positions": [
        {
          "account": "1234567890",
          "netPosition": -5000.00,
          "currency": "ZAR"
        }
      ]
    }
```

### Database Schema
```sql
CREATE TABLE settlement_batches (
    batch_id VARCHAR(50) PRIMARY KEY,
    batch_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_debit DECIMAL(18,2) NOT NULL,
    total_credit DECIMAL(18,2) NOT NULL,
    net_position DECIMAL(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    finalized_at TIMESTAMP,
    INDEX idx_batch_date (batch_date),
    INDEX idx_status (status)
);

CREATE TABLE settlement_transactions (
    settlement_txn_id VARCHAR(50) PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    settlement_status VARCHAR(20) NOT NULL,
    included_at TIMESTAMP NOT NULL,
    FOREIGN KEY (batch_id) REFERENCES settlement_batches(batch_id)
);
```

### Technology Stack
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Batch processing (Spring Batch)

---

## 10. Reconciliation Service

### Responsibilities
- Match internal transactions with clearing system responses
- Identify exceptions and discrepancies
- Generate reconciliation reports
- Handle dispute management
- Automated reconciliation for matched items

### API Endpoints

```yaml
POST /api/v1/reconciliation/run
  Description: Run reconciliation process
  Request Body:
    {
      "date": "2025-10-11",
      "clearingSystem": "RTC"
    }
  Response: 202 Accepted
    {
      "reconciliationId": "RECON-2025-XXXXXX",
      "status": "RUNNING"
    }

GET /api/v1/reconciliation/{reconciliationId}
  Description: Get reconciliation results

GET /api/v1/reconciliation/exceptions
  Description: Get unmatched transactions
  Response: 200 OK
    {
      "exceptions": [
        {
          "transactionId": "TXN-2025-XXXXXX",
          "reason": "Amount mismatch",
          "status": "PENDING_REVIEW"
        }
      ]
    }
```

### Database Schema
```sql
CREATE TABLE reconciliation_runs (
    reconciliation_id VARCHAR(50) PRIMARY KEY,
    run_date DATE NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_transactions INTEGER NOT NULL,
    matched_count INTEGER NOT NULL,
    unmatched_count INTEGER NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    INDEX idx_run_date (run_date)
);

CREATE TABLE reconciliation_exceptions (
    exception_id VARCHAR(50) PRIMARY KEY,
    reconciliation_id VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(50),
    clearing_reference VARCHAR(100),
    exception_type VARCHAR(50) NOT NULL,
    exception_reason TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    FOREIGN KEY (reconciliation_id) REFERENCES reconciliation_runs(reconciliation_id)
);
```

---

## 11. Notification Service

### Responsibilities
- Send SMS notifications
- Send email notifications
- Send push notifications (PWA)
- Webhook callbacks to external systems
- Manage notification templates

### API Endpoints

```yaml
POST /api/v1/notifications/send
  Description: Send notification
  Request Body:
    {
      "recipientId": "user-123",
      "channel": "SMS" | "EMAIL" | "PUSH",
      "templateId": "PAYMENT_SUCCESS",
      "parameters": {
        "paymentId": "PAY-2025-XXXXXX",
        "amount": "1000.00"
      }
    }
  Response: 202 Accepted
    {
      "notificationId": "NOTIF-XXXXX",
      "status": "QUEUED"
    }
```

### Events Consumed
- `PaymentCompletedEvent`
- `PaymentFailedEvent`
- `ValidationFailedEvent`

### Database Schema
```sql
CREATE TABLE notifications (
    notification_id VARCHAR(50) PRIMARY KEY,
    recipient_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    template_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_status (status)
);

CREATE TABLE notification_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    template_content TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Technology Stack
- Spring Boot 3.x
- Azure Notification Hub
- Twilio (SMS)
- SendGrid (Email)

---

## 12. Reporting Service

### Responsibilities
- Generate transaction reports
- Analytics and dashboards
- Compliance reports (SARB, FICA)
- Export data in various formats (PDF, Excel, CSV)
- Scheduled report generation

### API Endpoints

```yaml
POST /api/v1/reports/generate
  Description: Generate report
  Request Body:
    {
      "reportType": "TRANSACTION_SUMMARY",
      "dateRange": {
        "from": "2025-10-01",
        "to": "2025-10-11"
      },
      "format": "PDF" | "EXCEL" | "CSV",
      "filters": { ... }
    }
  Response: 202 Accepted
    {
      "reportId": "RPT-2025-XXXXXX",
      "status": "GENERATING"
    }

GET /api/v1/reports/{reportId}
  Description: Get report status

GET /api/v1/reports/{reportId}/download
  Description: Download generated report
  Response: Binary file
```

### Technology Stack
- Spring Boot 3.x
- Azure Synapse Analytics (data warehouse)
- JasperReports or Apache POI (report generation)

---

## 13. Saga Orchestrator Service

### Responsibilities
- Coordinate distributed transactions across services
- Implement compensation logic for failures
- Manage saga state machine
- Handle timeouts and retries
- Ensure eventual consistency

### Saga Definition Example

```yaml
PaymentSaga:
  steps:
    - name: ValidatePayment
      service: ValidationService
      action: POST /api/v1/validate/payment
      compensation: None
      
    - name: ReserveFunds
      service: AccountService
      action: POST /api/v1/accounts/{accountNumber}/holds
      compensation: DELETE /api/v1/accounts/holds/{holdId}
      
    - name: DetermineRouting
      service: RoutingService
      action: POST /api/v1/routing/determine
      compensation: None
      
    - name: CreateTransaction
      service: TransactionProcessingService
      action: POST /api/v1/transactions
      compensation: PATCH /api/v1/transactions/{transactionId}/status (CANCELLED)
      
    - name: SubmitToClearing
      service: ClearingAdapterService
      action: POST /api/v1/clearing/submit
      compensation: POST /api/v1/clearing/cancel
      
    - name: ProcessSettlement
      service: SettlementService
      action: POST /api/v1/settlement/process
      compensation: POST /api/v1/settlement/reverse
      
    - name: SendNotification
      service: NotificationService
      action: POST /api/v1/notifications/send
      compensation: None
```

### API Endpoints

```yaml
POST /api/v1/sagas/start
  Description: Start a new saga
  Request Body:
    {
      "sagaType": "PAYMENT_SAGA",
      "payload": { ... }
    }
  Response: 201 Created
    {
      "sagaId": "SAGA-2025-XXXXXX",
      "status": "RUNNING"
    }

GET /api/v1/sagas/{sagaId}
  Description: Get saga status

POST /api/v1/sagas/{sagaId}/compensate
  Description: Manually trigger compensation
```

### Database Schema
```sql
CREATE TABLE sagas (
    saga_id VARCHAR(50) PRIMARY KEY,
    saga_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_step VARCHAR(50),
    payload JSONB NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    INDEX idx_status (status)
);

CREATE TABLE saga_steps (
    step_id VARCHAR(50) PRIMARY KEY,
    saga_id VARCHAR(50) NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_status VARCHAR(20) NOT NULL,
    request_data JSONB,
    response_data JSONB,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    FOREIGN KEY (saga_id) REFERENCES sagas(saga_id)
);
```

### Technology Stack
- Spring Boot 3.x
- State machine library (Spring Statemachine or custom)
- PostgreSQL

---

## 14. API Gateway Service

### Responsibilities
- Route requests to appropriate microservices
- Authentication and authorization
- Rate limiting
- Request/response logging
- API versioning
- CORS handling

### Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: payment-service
          uri: lb://payment-initiation-service
          predicates:
            - Path=/api/v1/payments/**
          filters:
            - name: RateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
            - name: CircuitBreaker
              args:
                name: paymentServiceCircuitBreaker
        
        - id: account-service
          uri: lb://account-service
          predicates:
            - Path=/api/v1/accounts/**
```

### Technology Stack
- Spring Cloud Gateway
- Spring Security OAuth2 Resource Server
- Redis (rate limiting)

---

## 15. IAM Service

### Responsibilities
- User authentication (OAuth2/OIDC)
- User authorization (RBAC)
- Token management (JWT)
- User profile management
- Integration with Azure AD B2C

### API Endpoints

```yaml
POST /api/v1/auth/login
  Description: Authenticate user
  Request Body:
    {
      "username": "user@example.com",
      "password": "********"
    }
  Response: 200 OK
    {
      "accessToken": "jwt-token",
      "refreshToken": "refresh-token",
      "expiresIn": 900
    }

POST /api/v1/auth/refresh
  Description: Refresh access token

POST /api/v1/auth/logout
  Description: Logout user

GET /api/v1/users/me
  Description: Get current user profile
```

### Technology Stack
- Spring Boot 3.x
- Spring Security OAuth2
- Azure AD B2C
- JWT library

---

## 16. Audit Service

### Responsibilities
- Log all API calls
- Log all events
- Compliance audit trail
- Log retention and archival
- Audit report generation

### Database Schema
```sql
-- CosmosDB document structure
{
  "id": "audit-uuid",
  "eventType": "API_CALL",
  "timestamp": "2025-10-11T10:30:00Z",
  "userId": "user-123",
  "service": "PaymentInitiationService",
  "action": "POST /api/v1/payments",
  "requestData": { ... },
  "responseStatus": 201,
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "correlationId": "corr-uuid"
}
```

### Technology Stack
- Spring Boot 3.x
- Azure CosmosDB
- Azure Monitor

---

## Service Communication Matrix

| Service | Calls | Called By | Events Published | Events Consumed |
|---------|-------|-----------|------------------|-----------------|
| Payment Initiation | Validation | API Gateway | PaymentInitiatedEvent | - |
| Validation | Account, Fraud API | Saga Orchestrator | PaymentValidatedEvent | PaymentInitiatedEvent |
| Account | - | Validation, Saga | FundsReservedEvent | - |
| Routing | - | Saga Orchestrator | RoutingDeterminedEvent | PaymentValidatedEvent |
| Transaction Processing | Settlement | Saga Orchestrator | TransactionCreatedEvent | RoutingDeterminedEvent |
| Clearing Adapters | External Systems | Saga Orchestrator | ClearingSubmittedEvent | TransactionCreatedEvent |
| Settlement | - | Transaction Processing | SettlementCompleteEvent | ClearingCompletedEvent |
| Notification | SMS/Email APIs | - | - | PaymentCompletedEvent |
| Saga Orchestrator | All Core Services | Event Bus | SagaCompletedEvent | All Events |

---

## Build Order for AI Agents

### Phase 1: Foundation (Parallel)
1. Common Libraries (error handling, logging, DTOs)
2. API Gateway skeleton
3. Event Bus setup (Azure Service Bus topics/subscriptions)
4. Database setup scripts

### Phase 2: Independent Services (Parallel)
5. Account Service
6. Validation Service
7. Routing Service
8. Notification Service

### Phase 3: Core Processing (Sequential)
9. Payment Initiation Service
10. Transaction Processing Service
11. Clearing Adapters (can be parallel)

### Phase 4: Supporting Services (Parallel)
12. Settlement Service
13. Reconciliation Service
14. Reporting Service

### Phase 5: Orchestration
15. Saga Orchestrator (requires all core services)

### Phase 6: Security & Monitoring
16. IAM Service
17. Audit Service

---

**Next**: See `03-API-CONTRACTS.md` for detailed OpenAPI specifications
**Next**: See `04-EVENT-SCHEMAS.md` for AsyncAPI event schemas
