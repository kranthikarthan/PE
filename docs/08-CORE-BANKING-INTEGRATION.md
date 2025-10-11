# Core Banking System Integration Guide

## Overview

This document provides detailed specifications for integrating the Payments Engine with external core banking systems. The Payments Engine does **NOT** store account balances or master data - it acts as an orchestration layer that coordinates with multiple "Store of Value" systems.

---

## Architecture Principle

### Separation of Concerns

```
┌─────────────────────────────────────────────────────────────┐
│             PAYMENTS ENGINE                                  │
│  (Orchestration, Workflow, Clearing Integration)            │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │    Account Adapter Service                           │  │
│  │    • Routes requests to backend systems              │  │
│  │    • Aggregates responses                            │  │
│  │    • Caches data temporarily                         │  │
│  │    • Handles circuit breaking                        │  │
│  │    • Does NOT store balances                         │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Current     │ │   Savings    │ │  Investment  │
│  Accounts    │ │   System     │ │   System     │
│  System      │ │              │ │              │
└──────────────┘ └──────────────┘ └──────────────┘
     │                │                │
     ▼                ▼                ▼
[Account Balances] [Account Balances] [Account Balances]
[Transactions]     [Transactions]     [Transactions]
[Account Master]   [Account Master]   [Account Master]
```

**Key Principle**: Account balances and transaction history are **owned** by the core banking systems, not by the Payments Engine.

---

## External Core Banking Systems

### 1. Current Accounts System

**Purpose**: Manages transactional current accounts and cheque accounts

**Account Types Managed**:
- CURRENT
- CHEQUE
- BUSINESS_CURRENT

**API Base URL**: `https://current-accounts.bank.internal/api/v1`

**API Endpoints**:

```yaml
GET /accounts/{accountNumber}
  Description: Get account details
  Response:
    {
      "accountNumber": "1234567890",
      "accountHolder": "John Doe",
      "accountType": "CURRENT",
      "status": "ACTIVE",
      "balance": 10000.00,
      "availableBalance": 9500.00,
      "currency": "ZAR",
      "overdraftLimit": 5000.00
    }

POST /accounts/{accountNumber}/debit
  Description: Debit (withdraw) from account
  Request:
    {
      "idempotencyKey": "uuid",
      "amount": 1000.00,
      "currency": "ZAR",
      "reference": "PAY-2025-XXXXXX",
      "description": "Payment to merchant",
      "transactionDate": "2025-10-11T10:30:00Z"
    }
  Response:
    {
      "transactionId": "TXN-CURRENT-12345",
      "status": "COMPLETED",
      "newBalance": 9000.00,
      "timestamp": "2025-10-11T10:30:01Z"
    }

POST /accounts/{accountNumber}/credit
  Description: Credit (deposit) to account
  Request:
    {
      "idempotencyKey": "uuid",
      "amount": 1000.00,
      "currency": "ZAR",
      "reference": "PAY-2025-XXXXXX",
      "description": "Payment received",
      "transactionDate": "2025-10-11T10:30:00Z"
    }
  Response:
    {
      "transactionId": "TXN-CURRENT-12346",
      "status": "COMPLETED",
      "newBalance": 11000.00,
      "timestamp": "2025-10-11T10:30:01Z"
    }

POST /accounts/{accountNumber}/holds
  Description: Place temporary hold/reserve on funds
  Request:
    {
      "idempotencyKey": "uuid",
      "amount": 1000.00,
      "reference": "PAY-2025-XXXXXX",
      "expiryMinutes": 30
    }
  Response:
    {
      "holdId": "HOLD-CURRENT-789",
      "status": "PLACED",
      "expiresAt": "2025-10-11T11:00:00Z"
    }

DELETE /accounts/holds/{holdId}
  Description: Release hold
  Response: 204 No Content

GET /accounts/{accountNumber}/transactions
  Description: Get transaction history
  Query Parameters: fromDate, toDate, limit, offset
  Response:
    {
      "transactions": [...]
    }
```

**Error Responses**:
```json
{
  "errorCode": "INSUFFICIENT_FUNDS",
  "message": "Account has insufficient funds",
  "availableBalance": 500.00,
  "requestedAmount": 1000.00
}
```

---

### 2. Savings System

**Purpose**: Manages savings accounts and money market accounts

**Account Types Managed**:
- SAVINGS
- MONEY_MARKET
- BUSINESS_SAVINGS

**API Base URL**: `https://savings.bank.internal/api/v1`

**API Endpoints**: Same structure as Current Accounts System

**Special Considerations**:
- May have transaction limits (e.g., max 6 withdrawals per month)
- May have different interest calculation logic
- May require notice period for large withdrawals

---

### 3. Investment System

**Purpose**: Manages investment portfolios and unit trusts

**Account Types Managed**:
- INVESTMENT
- UNIT_TRUST
- SHARE_TRADING

**API Base URL**: `https://investments.bank.internal/api/v1`

**Special Considerations**:
- Debits may require settlement period (T+3)
- May have market hours restrictions
- May require additional authorization for large transactions

---

### 4. Card System

**Purpose**: Manages credit card and debit card accounts

**Account Types Managed**:
- CREDIT_CARD
- DEBIT_CARD
- PREPAID_CARD

**API Base URL**: `https://cards.bank.internal/api/v1`

**Special Considerations**:
- Credits typically reflect within 24 hours
- May have daily transaction limits
- May require card verification for large transactions

---

### 5. Home Loan System

**Purpose**: Manages home loan and mortgage accounts

**Account Types Managed**:
- HOME_LOAN
- MORTGAGE
- BOND

**API Base URL**: `https://home-loans.bank.internal/api/v1`

**Special Considerations**:
- Credits reduce loan balance
- Debits typically not allowed (loan is a liability account)
- May have early repayment penalties

---

### 6. Car Loan System

**Purpose**: Manages vehicle finance accounts

**Account Types Managed**:
- CAR_LOAN
- VEHICLE_FINANCE

**API Base URL**: `https://vehicle-finance.bank.internal/api/v1`

**Special Considerations**:
- Credits reduce loan balance
- Similar restrictions as home loans

---

### 7. Personal Loan System

**Purpose**: Manages personal loans

**Account Types Managed**:
- PERSONAL_LOAN
- OVERDRAFT

**API Base URL**: `https://personal-loans.bank.internal/api/v1`

---

### 8. Business Banking System

**Purpose**: Manages corporate and business accounts

**Account Types Managed**:
- BUSINESS_CURRENT
- BUSINESS_SAVINGS
- MERCHANT_ACCOUNT

**API Base URL**: `https://business-banking.bank.internal/api/v1`

**Special Considerations**:
- May require dual authorization for large transactions
- May have batch payment capabilities
- May have different fee structures

---

## Standard API Contract

All core banking systems adhere to a standardized API contract:

### Authentication

**Method**: OAuth 2.0 Client Credentials Flow

```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id={payments_engine_client_id}
&client_secret={client_secret}
&scope=accounts:read accounts:write
```

**Response**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1Ni...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "accounts:read accounts:write"
}
```

**Token Usage**:
```http
GET /api/v1/accounts/1234567890
Authorization: Bearer eyJhbGciOiJSUzI1Ni...
```

### Request Headers

All requests to core banking systems must include:

```http
Authorization: Bearer {access_token}
Content-Type: application/json
Idempotency-Key: {uuid}
X-Correlation-Id: {correlation_id}
X-Request-Id: {request_id}
X-Client-Name: PaymentsEngine
X-Client-Version: 1.0.0
```

### Response Headers

All responses include:

```http
Content-Type: application/json
X-Request-Id: {request_id}
X-Response-Time: {milliseconds}
X-Transaction-Id: {backend_transaction_id}
```

### Idempotency

All `POST`, `PUT`, `PATCH` operations support idempotency:
- Include `Idempotency-Key` header with UUID
- System stores key for 24 hours
- Duplicate requests return original response

### Error Handling

**Standard Error Response**:
```json
{
  "errorCode": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2025-10-11T10:30:00Z",
  "path": "/api/v1/accounts/1234567890/debit",
  "requestId": "req-uuid",
  "details": {
    "field": "amount",
    "reason": "Amount exceeds daily limit"
  }
}
```

**Standard Error Codes**:

| Code | HTTP Status | Description | Action |
|------|-------------|-------------|--------|
| ACCOUNT_NOT_FOUND | 404 | Account does not exist | Verify account number |
| INSUFFICIENT_FUNDS | 400 | Insufficient balance | Inform user |
| ACCOUNT_SUSPENDED | 403 | Account is suspended | Contact support |
| ACCOUNT_CLOSED | 403 | Account is closed | Use different account |
| INVALID_AMOUNT | 400 | Amount validation failed | Check amount |
| DAILY_LIMIT_EXCEEDED | 400 | Daily transaction limit exceeded | Try tomorrow |
| TRANSACTION_LIMIT_EXCEEDED | 400 | Per-transaction limit exceeded | Reduce amount |
| DUPLICATE_TRANSACTION | 409 | Idempotency key already processed | Return original response |
| SYSTEM_ERROR | 500 | Internal system error | Retry with backoff |
| TIMEOUT | 504 | Request timeout | Retry |

---

## Account Routing Configuration

The Account Adapter Service uses a routing table to determine which backend system to call:

### Routing Table Structure

```sql
CREATE TABLE account_routing (
    account_number VARCHAR(50) PRIMARY KEY,
    backend_system VARCHAR(50) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    base_url VARCHAR(200) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_verified TIMESTAMP
);
```

### Routing Logic

```java
public AccountRouting determineBackendSystem(String accountNumber) {
    // 1. Lookup in routing table
    Optional<AccountRouting> routing = routingRepository.findByAccountNumber(accountNumber);
    
    if (routing.isPresent()) {
        return routing.get();
    }
    
    // 2. If not found, query all backend systems (discovery)
    for (BackendSystem system : backendSystems) {
        if (accountExistsInSystem(accountNumber, system)) {
            // Cache routing for future use
            AccountRouting newRouting = new AccountRouting();
            newRouting.setAccountNumber(accountNumber);
            newRouting.setBackendSystem(system.getSystemId());
            newRouting.setBaseUrl(system.getBaseUrl());
            routingRepository.save(newRouting);
            return newRouting;
        }
    }
    
    throw new AccountNotFoundException(accountNumber);
}
```

### Account Number Prefixing (Alternative Approach)

Some implementations use account number prefixes to determine routing:

| Prefix | Backend System | Example |
|--------|----------------|---------|
| 10 | Current Accounts | 1012345678 |
| 20 | Savings | 2012345678 |
| 30 | Investment | 3012345678 |
| 40 | Cards | 4012345678 |
| 50 | Home Loans | 5012345678 |
| 60 | Car Loans | 6012345678 |
| 70 | Personal Loans | 7012345678 |
| 80 | Business Banking | 8012345678 |

---

## Circuit Breaker Pattern

### Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      currentAccountsSystem:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
        permitted-number-of-calls-in-half-open-state: 3
        sliding-window-size: 10
        sliding-window-type: COUNT_BASED
        minimum-number-of-calls: 5
      
      savingsSystem:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
```

### Circuit Breaker States

```
CLOSED → OPEN → HALF_OPEN → CLOSED
   ↓                            ↑
   └────────────────────────────┘
```

1. **CLOSED**: Normal operation, requests pass through
2. **OPEN**: Failure threshold exceeded, requests fail immediately
3. **HALF_OPEN**: After wait duration, allow test requests
4. **CLOSED**: If test requests succeed, return to normal

### Fallback Strategy

```java
@CircuitBreaker(name = "currentAccountsSystem", fallbackMethod = "getAccountFallback")
public AccountDTO getAccount(String accountNumber) {
    return callBackendSystem(accountNumber);
}

private AccountDTO getAccountFallback(String accountNumber, Exception e) {
    // Try cache first
    Optional<AccountDTO> cached = cacheService.getCachedAccount(accountNumber);
    if (cached.isPresent()) {
        log.warn("Using cached account data for {}", accountNumber);
        return cached.get();
    }
    
    // No cache available
    throw new ServiceUnavailableException("Backend system unavailable");
}
```

---

## Caching Strategy

### Cache Layers

1. **L1 Cache (In-Memory)**: Caffeine cache, 10-second TTL
2. **L2 Cache (Distributed)**: Redis, 30-second TTL

### Cache Keys

```
account:{accountNumber}:details
account:{accountNumber}:balance
routing:{accountNumber}
```

### Cache Invalidation

```java
@CacheEvict(value = "accounts", key = "#accountNumber")
public void invalidateAccountCache(String accountNumber) {
    log.info("Invalidating cache for account {}", accountNumber);
}

// Invalidate after debit/credit
@CacheEvict(value = "accounts", key = "#accountNumber")
public DebitResponse debitAccount(String accountNumber, DebitRequest request) {
    // ... debit logic
}
```

---

## Monitoring & Observability

### Key Metrics

```java
@Timed(value = "backend.api.call", extraTags = {"system", "currentAccounts"})
public AccountDTO callCurrentAccountsSystem(String accountNumber) {
    // ... API call
}
```

**Metrics to Track**:
- `backend.api.call.count`: Number of API calls per backend system
- `backend.api.call.duration`: Response time (avg, p50, p95, p99)
- `backend.api.call.errors`: Error count per backend system
- `backend.circuit.breaker.state`: Circuit breaker state changes
- `backend.cache.hit.ratio`: Cache hit percentage

### Health Checks

```java
@Component
public class BackendSystemHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        Map<String, Health> systemHealthMap = new HashMap<>();
        
        for (BackendSystem system : backendSystems) {
            try {
                HttpResponse response = callHealthEndpoint(system);
                if (response.getStatusCode() == 200) {
                    systemHealthMap.put(system.getSystemId(), Health.up().build());
                } else {
                    systemHealthMap.put(system.getSystemId(), Health.down().build());
                }
            } catch (Exception e) {
                systemHealthMap.put(system.getSystemId(), 
                    Health.down().withException(e).build());
            }
        }
        
        boolean allHealthy = systemHealthMap.values().stream()
            .allMatch(h -> h.getStatus() == Status.UP);
        
        return allHealthy ? Health.up() : Health.down();
    }
}
```

### Distributed Tracing

All calls to backend systems include trace context:

```http
X-B3-TraceId: 80f198ee56343ba8
X-B3-SpanId: 05e3ac9a4f6e3b90
X-B3-ParentSpanId: 05e3ac9a4f6e3b90
X-B3-Sampled: 1
```

---

## Security Considerations

### 1. OAuth Token Management

- Tokens cached with 5-minute buffer before expiry
- Automatic token refresh
- Secure storage in Azure Key Vault

### 2. mTLS (Mutual TLS)

For highly secure environments:
- Client certificate authentication
- Certificate rotation every 90 days

### 3. API Rate Limiting

```yaml
rate-limiting:
  per-backend-system:
    requests-per-second: 1000
    burst: 100
```

### 4. Request Signing

For additional security, requests can be signed:
```
Authorization: Bearer {token}
X-Signature: {HMAC-SHA256(request_body + timestamp)}
X-Timestamp: {unix_timestamp}
```

---

## Disaster Recovery

### Backend System Unavailability

**Scenario**: One backend system is completely down

**Strategy**:
1. Circuit breaker opens immediately
2. Return cached data (up to 30 seconds old)
3. Queue write operations (debit/credit) for later processing
4. Notify operations team
5. Fallback to manual processing if extended outage

### Data Consistency

**Two-Phase Commit Not Used**: 
- Payments Engine uses Saga pattern for distributed transactions
- If backend system fails during payment, compensation is triggered
- Example: If debit succeeds but credit fails, debit is reversed

---

## Testing Strategy

### Mock Backend Systems

Use WireMock for testing:

```java
@SpringBootTest
@AutoConfigureWireMock(port = 9090)
class AccountAdapterServiceTest {
    
    @Test
    void shouldDebitAccount() {
        // Setup mock response
        stubFor(post(urlEqualTo("/api/v1/accounts/1234567890/debit"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"transactionId\":\"TXN-123\",\"newBalance\":9000.00}")));
        
        // Test
        DebitResponse response = accountAdapter.debitAccount("1234567890", debitRequest);
        
        assertThat(response.getTransactionId()).isEqualTo("TXN-123");
    }
}
```

### Contract Testing

Use Pact for consumer-driven contract testing:
- Payments Engine defines expected contracts
- Backend systems verify they meet contracts

---

## Deployment Configuration

### Environment Variables

```yaml
# Current Accounts System
BACKEND_CURRENT_ACCOUNTS_URL: https://current-accounts.bank.internal/api/v1
BACKEND_CURRENT_ACCOUNTS_OAUTH_URL: https://current-accounts.bank.internal/oauth/token
BACKEND_CURRENT_ACCOUNTS_CLIENT_ID: payments-engine
BACKEND_CURRENT_ACCOUNTS_CLIENT_SECRET_KEY: /keyvault/current-accounts-secret

# Savings System
BACKEND_SAVINGS_URL: https://savings.bank.internal/api/v1
BACKEND_SAVINGS_OAUTH_URL: https://savings.bank.internal/oauth/token
BACKEND_SAVINGS_CLIENT_ID: payments-engine
BACKEND_SAVINGS_CLIENT_SECRET_KEY: /keyvault/savings-secret

# ... (repeat for all systems)

# Circuit Breaker
CIRCUIT_BREAKER_FAILURE_THRESHOLD: 5
CIRCUIT_BREAKER_WAIT_DURATION_SECONDS: 60

# Caching
CACHE_TTL_SECONDS: 30
REDIS_HOST: payments-redis.redis.cache.windows.net
REDIS_PORT: 6380
```

---

## Performance Optimization

### Parallel Requests

When querying multiple accounts:

```java
public List<AccountDTO> getMultipleAccounts(List<String> accountNumbers) {
    return accountNumbers.parallelStream()
        .map(this::getAccount)
        .collect(Collectors.toList());
}
```

### Batch Operations

Some backend systems support batch operations:

```http
POST /api/v1/accounts/batch/debit
{
  "transactions": [
    {"accountNumber": "1234567890", "amount": 100.00},
    {"accountNumber": "0987654321", "amount": 200.00}
  ]
}
```

---

## Summary

The Payments Engine **delegates** all account management to external core banking systems:
- ✅ No account balances stored in Payments Engine
- ✅ No transaction history stored (except for payment orchestration)
- ✅ Account Adapter acts as intelligent proxy/orchestrator
- ✅ Circuit breakers prevent cascading failures
- ✅ Caching reduces load on backend systems
- ✅ Standard API contract across all systems

This architecture provides:
- **Separation of Concerns**: Clear boundaries between systems
- **Scalability**: Backend systems scale independently
- **Resilience**: Circuit breakers and caching provide fault tolerance
- **Flexibility**: New account types can be added by onboarding new backend systems

---

**Related Documents**:
- [01-ASSUMPTIONS.md](01-ASSUMPTIONS.md) - Section 1.5 (External Core Banking Systems)
- [02-MICROSERVICES-BREAKDOWN.md](02-MICROSERVICES-BREAKDOWN.md) - Section 3 (Account Adapter Service)
- [05-DATABASE-SCHEMAS.md](05-DATABASE-SCHEMAS.md) - Section 3 (Account Adapter Database)
