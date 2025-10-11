# Customer Limit Management System

## Overview

The Payments Engine includes a comprehensive **Customer Limit Management System** that enforces transaction limits at multiple levels to manage risk, ensure compliance, and prevent fraud. This document describes the limit checking functionality in detail.

---

## Key Features

### ✅ Multi-Level Limit Enforcement
- **Daily Limits**: Maximum amount per day across all payment types
- **Monthly Limits**: Maximum amount per month across all payment types
- **Per Payment Type Limits**: Different limits for EFT, RTC, RTGS, Debit Orders
- **Per Transaction Limits**: Maximum amount for a single transaction
- **Transaction Count Limits**: Maximum number of transactions per period

### ✅ Real-Time Limit Checking
- Limits checked **before** payment execution
- Instant validation response (< 100ms)
- Prevents overspending beyond configured limits

### ✅ Limit Reservation System
- Temporary "reservation" of limits during payment processing
- Prevents concurrent transactions from exceeding limits
- Auto-expiry of reservations if payment not completed

### ✅ Customer Profile-Based Limits
- Pre-configured limit profiles for different customer segments
- Customizable per-customer limit overrides
- Support for limit increases (with approval workflow)

---

## Limit Profiles

### Default Limit Configurations

| Customer Profile | Daily Limit | Monthly Limit | Per Transaction | Max Txns/Day |
|------------------|-------------|---------------|-----------------|--------------|
| **Individual - Standard** | R50,000 | R200,000 | R25,000 | 100 |
| **Individual - Premium** | R100,000 | R500,000 | R50,000 | 200 |
| **SME** | R500,000 | R2,000,000 | R250,000 | 500 |
| **Corporate** | R5,000,000 | R50,000,000 | R5,000,000 | 1,000 |

### Payment Type Specific Limits

Example for **Individual - Premium** profile:

| Payment Type | Daily Limit | Per Transaction | Max Txns/Day |
|--------------|-------------|-----------------|--------------|
| **EFT** | R50,000 | R10,000 | 50 |
| **RTC** | R100,000 | R50,000 | 100 |
| **RTGS** | R100,000 | R100,000 | 20 |
| **Debit Order** | R25,000 | R5,000 | 20 |

---

## How It Works

### 1. Limit Check Flow

```
┌─────────────────────────────────────────────────────────────┐
│  1. Payment Request Received                                 │
│     Amount: R10,000, Type: EFT                              │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│  2. Validation Service: Check Customer Limits                │
│     - Load customer limit configuration                      │
│     - Load current usage (daily, monthly, by type)          │
│     - Calculate available limits                             │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Perform Limit Checks                                     │
│     ✓ Daily: Used R45,000, Limit R100,000, Available R55,000│
│     ✓ Monthly: Used R180,000, Limit R500,000, Avail R320,000│
│     ✓ EFT Type: Used R15,000, Limit R50,000, Avail R35,000 │
│     ✓ Per Txn: R10,000 <= R50,000                           │
│                                                               │
│     Decision: ALL CHECKS PASSED ✓                            │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Reserve Limit (Temporary Hold)                           │
│     - Create reservation: RES-XXXXX                          │
│     - Mark R10,000 as "reserved"                            │
│     - Expiry: 30 minutes                                     │
│     - Status: RESERVED                                       │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│  5. Payment Processing Continues...                          │
│     - Validation passed                                      │
│     - Reserve funds on account                               │
│     - Route to clearing system                               │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│  6a. Payment SUCCESS                                         │
│      - Consume reservation (convert to actual usage)         │
│      - Update daily_used: R45,000 → R55,000                 │
│      - Update monthly_used: R180,000 → R190,000             │
│      - Publish: LimitConsumedEvent                           │
└──────────────────────────────────────────────────────────────┘

                  OR

┌─────────────────────────────────────────────────────────────┐
│  6b. Payment FAILED / CANCELLED                              │
│      - Release reservation (restore available limit)         │
│      - Limits unchanged                                      │
│      - Publish: LimitReleasedEvent                           │
└──────────────────────────────────────────────────────────────┘
```

### 2. Limit Exceeded Scenario

```
┌─────────────────────────────────────────────────────────────┐
│  Payment Request: R50,000 (EFT)                             │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│  Check Limits:                                               │
│  ✓ Daily: Available R55,000 >= R50,000 (OK)                │
│  ✗ EFT Type: Available R35,000 < R50,000 (FAIL)            │
│                                                               │
│  Decision: LIMIT EXCEEDED ✗                                  │
└─────────────────┬───────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│  Reject Payment                                              │
│  - Status: VALIDATION_FAILED                                 │
│  - Reason: PAYMENT_TYPE_LIMIT_EXCEEDED                       │
│  - Message: "EFT daily limit exceeded. Available: R35,000"  │
│  - Publish: ValidationFailedEvent                            │
│                                                               │
│  NO limit reservation created                                │
│  NO compensation needed                                      │
└──────────────────────────────────────────────────────────────┘
```

---

## API Endpoints

### Check Customer Limits

```http
GET /api/v1/limits/customer/{customerId}
```

**Response**:
```json
{
  "customerId": "CUST-123456",
  "customerProfile": "INDIVIDUAL_PREMIUM",
  "limits": {
    "daily": {
      "limit": 100000.00,
      "used": 45000.00,
      "available": 55000.00,
      "resetsAt": "2025-10-12T00:00:00Z"
    },
    "monthly": {
      "limit": 500000.00,
      "used": 180000.00,
      "available": 320000.00,
      "resetsAt": "2025-11-01T00:00:00Z"
    },
    "perTransaction": {
      "limit": 50000.00
    },
    "byPaymentType": [
      {
        "paymentType": "EFT",
        "dailyLimit": 50000.00,
        "dailyUsed": 15000.00,
        "dailyAvailable": 35000.00
      },
      {
        "paymentType": "RTC",
        "dailyLimit": 100000.00,
        "dailyUsed": 30000.00,
        "dailyAvailable": 70000.00
      }
    ]
  }
}
```

### Check if Sufficient Limit

```http
POST /api/v1/limits/customer/{customerId}/check
Content-Type: application/json

{
  "amount": 10000.00,
  "paymentType": "EFT"
}
```

**Response**:
```json
{
  "sufficient": true,
  "dailyLimitCheck": {
    "limit": 100000.00,
    "used": 45000.00,
    "available": 55000.00,
    "afterTransaction": 45000.00,
    "withinLimit": true
  },
  "monthlyLimitCheck": {
    "limit": 500000.00,
    "used": 180000.00,
    "available": 320000.00,
    "afterTransaction": 310000.00,
    "withinLimit": true
  },
  "paymentTypeLimitCheck": {
    "paymentType": "EFT",
    "limit": 50000.00,
    "used": 15000.00,
    "available": 35000.00,
    "afterTransaction": 25000.00,
    "withinLimit": true
  }
}
```

### Update Customer Limits (Admin)

```http
PUT /api/v1/limits/customer/{customerId}
Content-Type: application/json

{
  "dailyLimit": 150000.00,
  "monthlyLimit": 600000.00,
  "perTransactionLimit": 75000.00,
  "paymentTypeLimits": [
    {
      "paymentType": "EFT",
      "dailyLimit": 75000.00
    },
    {
      "paymentType": "RTC",
      "dailyLimit": 150000.00
    }
  ]
}
```

---

## Database Schema

### customer_limits
Stores limit configuration per customer

```sql
CREATE TABLE customer_limits (
    customer_id VARCHAR(50) PRIMARY KEY,
    customer_profile VARCHAR(50) NOT NULL,
    daily_limit DECIMAL(18,2) NOT NULL,
    monthly_limit DECIMAL(18,2) NOT NULL,
    per_transaction_limit DECIMAL(18,2) NOT NULL,
    max_transactions_per_day INTEGER DEFAULT 100,
    is_active BOOLEAN DEFAULT TRUE,
    effective_from DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### payment_type_limits
Stores limits per payment type per customer

```sql
CREATE TABLE payment_type_limits (
    limit_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    daily_limit DECIMAL(18,2) NOT NULL,
    per_transaction_limit DECIMAL(18,2),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_customer_payment_type UNIQUE (customer_id, payment_type)
);
```

### customer_limit_usage
Tracks used limits (daily/monthly)

```sql
CREATE TABLE customer_limit_usage (
    usage_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    usage_date DATE NOT NULL,
    daily_used DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    daily_transaction_count INTEGER NOT NULL DEFAULT 0,
    monthly_used DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_customer_usage_date UNIQUE (customer_id, usage_date)
);
```

### payment_type_limit_usage
Tracks used limits per payment type

```sql
CREATE TABLE payment_type_limit_usage (
    usage_id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    usage_date DATE NOT NULL,
    daily_used DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT uk_customer_payment_type_usage UNIQUE (customer_id, payment_type, usage_date)
);
```

### limit_reservations
Temporary holds on limits during payment processing

```sql
CREATE TABLE limit_reservations (
    reservation_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    payment_id VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(18,2) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    released_at TIMESTAMP
);
```

---

## Events

### LimitConsumedEvent

Published when a payment completes successfully and the reserved limit is converted to actual usage.

```json
{
  "eventType": "LimitConsumedEvent",
  "eventId": "evt-uuid",
  "customerId": "CUST-123456",
  "paymentId": "PAY-2025-XXXXXX",
  "amount": 10000.00,
  "paymentType": "EFT",
  "reservationId": "RES-XXXXX",
  "dailyUsedBefore": 45000.00,
  "dailyUsedAfter": 55000.00,
  "monthlyUsedBefore": 180000.00,
  "monthlyUsedAfter": 190000.00,
  "consumedAt": "2025-10-11T10:35:00Z"
}
```

### LimitReleasedEvent

Published when a payment fails or is cancelled and the reserved limit is released.

```json
{
  "eventType": "LimitReleasedEvent",
  "eventId": "evt-uuid",
  "customerId": "CUST-123456",
  "paymentId": "PAY-2025-XXXXXX",
  "amount": 10000.00,
  "paymentType": "EFT",
  "reservationId": "RES-XXXXX",
  "releaseReason": "PAYMENT_FAILED",
  "releasedAt": "2025-10-11T10:35:00Z"
}
```

### ValidationFailedEvent (Limit Exceeded)

```json
{
  "eventType": "ValidationFailedEvent",
  "eventId": "evt-uuid",
  "paymentId": "PAY-2025-XXXXXX",
  "customerId": "CUST-123456",
  "failureReason": "DAILY_LIMIT_EXCEEDED",
  "validationResult": {
    "valid": false,
    "failureReasons": [
      {
        "code": "DAILY_LIMIT_EXCEEDED",
        "message": "Daily transaction limit exceeded",
        "currentLimit": 100000.00,
        "usedAmount": 95000.00,
        "requestedAmount": 10000.00,
        "availableAmount": 5000.00
      }
    ]
  }
}
```

---

## Error Codes

| Error Code | Description | User Action |
|------------|-------------|-------------|
| **DAILY_LIMIT_EXCEEDED** | Customer exceeded daily transaction limit | Wait until tomorrow or contact support for limit increase |
| **MONTHLY_LIMIT_EXCEEDED** | Customer exceeded monthly transaction limit | Wait until next month or contact support |
| **PAYMENT_TYPE_LIMIT_EXCEEDED** | Exceeded limit for specific payment type (e.g., EFT) | Use different payment type or wait until limit resets |
| **PER_TRANSACTION_LIMIT_EXCEEDED** | Single transaction amount too large | Split into multiple smaller transactions |
| **TRANSACTION_COUNT_EXCEEDED** | Too many transactions today | Wait until tomorrow |

---

## Auto-Reset Logic

### Daily Limits
- **Reset Time**: Midnight (00:00 CAT) every day
- **Implementation**: Scheduled job runs at midnight
- **Database**: Creates new record in `customer_limit_usage` for new date
- **Old Records**: Retained for 90 days, then archived

### Monthly Limits
- **Reset Time**: 00:00 CAT on the 1st of each month
- **Implementation**: Same scheduled job as daily reset
- **Calculation**: `monthly_used` aggregates all `daily_used` values for the month

### Scheduled Job (Cron)

```java
@Scheduled(cron = "0 0 0 * * *", zone = "Africa/Johannesburg")
public void resetDailyLimits() {
    log.info("Daily limit reset job started");
    
    // Create new usage records for today (will start at 0.00 used)
    // Old records automatically become "yesterday's" data
    
    // Cleanup old reservations (expired > 24 hours ago)
    limitReservationRepository.deleteExpired(LocalDateTime.now().minusDays(1));
    
    log.info("Daily limit reset job completed");
}
```

---

## Concurrency Handling

### Problem: Race Conditions

Two concurrent payments could both check the limit and see "available: R10,000", then both proceed, exceeding the limit.

### Solution: Database-Level Locking

```sql
-- Use pessimistic locking when checking/updating limits
SELECT * FROM customer_limit_usage 
WHERE customer_id = ? AND usage_date = CURRENT_DATE 
FOR UPDATE;

-- Update with atomic increment
UPDATE customer_limit_usage 
SET daily_used = daily_used + ?,
    daily_transaction_count = daily_transaction_count + 1,
    last_updated = CURRENT_TIMESTAMP
WHERE customer_id = ? AND usage_date = CURRENT_DATE;
```

### Alternative: Optimistic Locking

```java
@Version
private Long version;

// JPA automatically handles version checking
// If version mismatch, throws OptimisticLockException
// Application retries the transaction
```

---

## Monitoring & Alerts

### Key Metrics

1. **Limit Usage Percentage**: `(daily_used / daily_limit) * 100`
   - Alert at 80% usage
   - Warning at 90% usage

2. **Limit Exceeded Rate**: `(rejected payments / total payments) * 100`
   - Target: < 5%

3. **Reservation Expiry Rate**: `(expired reservations / total reservations) * 100`
   - Target: < 1%

4. **Average Time to Consume Reservation**: Payment processing time
   - Target: < 30 seconds

### Dashboard Queries

```sql
-- Customers approaching daily limit (>80%)
SELECT customer_id, 
       daily_limit, 
       daily_used,
       (daily_used / daily_limit * 100) AS usage_percentage
FROM customer_limit_summary
WHERE usage_percentage > 80;

-- Top customers by transaction volume
SELECT customer_id,
       daily_transaction_count,
       daily_used
FROM customer_limit_usage
WHERE usage_date = CURRENT_DATE
ORDER BY daily_used DESC
LIMIT 10;

-- Limit exceeded events today
SELECT COUNT(*) 
FROM validation_results
WHERE validated_at >= CURRENT_DATE
  AND validation_status = 'INVALID'
  AND failed_rules::text LIKE '%LIMIT_EXCEEDED%';
```

---

## Frontend Integration

### Display Limit Information

```typescript
interface CustomerLimits {
  daily: {
    limit: number;
    used: number;
    available: number;
    percentage: number;
    resetsAt: string;
  };
  monthly: {
    limit: number;
    used: number;
    available: number;
    percentage: number;
    resetsAt: string;
  };
  byPaymentType: Array<{
    paymentType: string;
    dailyLimit: number;
    dailyUsed: number;
    dailyAvailable: number;
  }>;
}

// Display in UI
<LimitCard>
  <h3>Daily Limit</h3>
  <ProgressBar value={limits.daily.percentage} />
  <p>Used: R{limits.daily.used.toFixed(2)} / R{limits.daily.limit.toFixed(2)}</p>
  <p>Available: R{limits.daily.available.toFixed(2)}</p>
  <p>Resets at: {formatTime(limits.daily.resetsAt)}</p>
</LimitCard>
```

### Pre-Payment Validation

```typescript
async function validatePaymentAmount(amount: number, paymentType: string) {
  const response = await api.post(`/api/v1/limits/customer/${customerId}/check`, {
    amount,
    paymentType
  });
  
  if (!response.sufficient) {
    // Show user-friendly error
    if (!response.dailyLimitCheck.withinLimit) {
      showError(`Daily limit exceeded. Available: R${response.dailyLimitCheck.available.toFixed(2)}`);
    } else if (!response.paymentTypeLimitCheck.withinLimit) {
      showError(`${paymentType} limit exceeded. Try another payment method.`);
    }
    return false;
  }
  
  return true;
}
```

---

## Benefits

### ✅ Risk Management
- Prevents customers from overspending
- Limits exposure to fraud
- Configurable controls per customer segment

### ✅ Regulatory Compliance
- Demonstrates financial controls
- Audit trail of all limit changes
- Supports AML/CFT requirements

### ✅ Customer Experience
- Transparent limit information
- Real-time feedback on available limits
- Self-service limit viewing

### ✅ Operational Efficiency
- Automated limit enforcement
- Reduces manual monitoring
- Configurable without code changes

---

## Related Documents

- **[01-ASSUMPTIONS.md](01-ASSUMPTIONS.md)** - Section 1.4.1 (Customer Limits Assumptions)
- **[02-MICROSERVICES-BREAKDOWN.md](02-MICROSERVICES-BREAKDOWN.md)** - Section 2 (Validation Service)
- **[03-EVENT-SCHEMAS.md](03-EVENT-SCHEMAS.md)** - LimitConsumedEvent, LimitReleasedEvent
- **[05-DATABASE-SCHEMAS.md](05-DATABASE-SCHEMAS.md)** - Section 2 (Validation DB - Limit Tables)

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
