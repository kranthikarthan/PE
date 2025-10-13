# Channel Integration Mechanisms - Bank Front-End Channels

## Executive Summary

This document explains **how bank's customer-facing channels** (mobile banking, web banking, branch systems, ATMs, USSD, corporate portals) integrate with the Payments Engine, and the **synchronous vs asynchronous patterns** used.

**Key Finding**: The architecture uses a **HYBRID pattern**:
- ✅ **SYNCHRONOUS** for payment initiation (request/response)
- ✅ **ASYNCHRONOUS** for internal processing (event-driven)
- ✅ **ASYNCHRONOUS** for notifications (webhooks, WebSocket, push)

---

## Table of Contents

1. [Channel Types](#channel-types)
2. [Integration Architecture](#integration-architecture)
3. [Synchronous Pattern (Payment Initiation)](#synchronous-pattern-payment-initiation)
4. [Asynchronous Pattern (Status Updates)](#asynchronous-pattern-status-updates)
5. [BFF Layer Integration](#bff-layer-integration)
6. [Request Flow (Detailed)](#request-flow-detailed)
7. [API Specifications](#api-specifications)
8. [Authentication & Authorization](#authentication--authorization)
9. [Error Handling](#error-handling)
10. [Performance Characteristics](#performance-characteristics)

---

## 1. Channel Types

### 1.1 Customer Channels (Front-End)

| Channel | Type | Protocol | BFF | Use Case |
|---------|------|----------|-----|----------|
| **Web Banking** | Desktop browser | GraphQL | Web BFF | Account holders making payments via web |
| **Mobile Banking** | Mobile app (iOS/Android) | REST | Mobile BFF | Account holders making payments via mobile |
| **USSD** | Feature phone | REST | Mobile BFF | Unbanked/feature phone payments |
| **Branch System** | Internal staff app | REST | Partner BFF | Teller-initiated payments |
| **ATM** | Self-service kiosk | REST | Partner BFF | Cash withdrawal/payment at ATM |
| **Corporate Portal** | Corporate clients | REST | Partner BFF | Bulk payments, payroll |
| **Partner APIs** | Third-party systems | REST | Partner BFF | B2B integrations |

**Total Channels**: 7 types, **all integrate via BFF layer**

---

## 2. Integration Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         BANK'S CUSTOMER CHANNELS                             │
├────────────┬──────────────┬────────────┬──────────────┬────────────────────┤
│ Web        │ Mobile       │ Branch     │ Corporate    │ Partner APIs       │
│ Banking    │ Banking      │ System     │ Portal       │ (3rd party)        │
│ (React)    │ (iOS/Android)│ (Desktop)  │ (Web)        │ (B2B)              │
└──────┬─────┴──────┬───────┴──────┬─────┴──────┬───────┴──────┬─────────────┘
       │            │              │            │              │
       │ GraphQL    │ REST         │ REST       │ REST         │ REST
       │            │ (lightweight)│ (standard) │ (bulk)       │ (comprehensive)
       │            │              │            │              │
       ▼            ▼              ▼            ▼              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                  AZURE APPLICATION GATEWAY (Layer 1)                         │
│  • WAF (OWASP Top 10)                                                        │
│  • SSL/TLS termination                                                       │
│  • DDoS protection                                                           │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│              AZURE API MANAGEMENT (APIM) (Layer 2)                           │
│  • OAuth 2.0 / Azure AD B2C authentication                                   │
│  • API key management                                                        │
│  • Rate limiting (per channel: 1000 req/min)                                │
│  • API versioning (/v1, /v2)                                                 │
└───────┬──────────────────┬───────────────────┬──────────────────────────────┘
        │                  │                   │
        ▼                  ▼                   ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────────────────┐
│  Web BFF     │   │ Mobile BFF   │   │   Partner API BFF        │
│  (GraphQL)   │   │ (REST Light) │   │   (REST Comprehensive)   │
│  Port: 8090  │   │ Port: 8091   │   │   Port: 8092             │
└──────┬───────┘   └──────┬───────┘   └──────┬───────────────────┘
       │                  │                   │
       └──────────────────┴───────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                 PAYMENTS ENGINE (20 Microservices)                           │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ #1: Payment Initiation → #2: Validation → #3: Account Adapter       │  │
│  │ #4: Routing → #5: Transaction Processing → #6-10: Clearing Adapters │  │
│  │ #11: Settlement → #12: Reconciliation → #13: Notification           │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Synchronous Pattern (Payment Initiation)

### 3.1 Request/Response Flow

**Pattern**: **SYNCHRONOUS** (client waits for response)

**Why Synchronous?**
- ✅ Client needs immediate acknowledgment (payment accepted or rejected)
- ✅ Client needs payment ID for tracking
- ✅ User expects immediate feedback ("Payment submitted successfully")
- ✅ Prevents duplicate submissions (idempotency key in request)

**Timeout**: 30 seconds (client-side), 10 seconds (BFF to Payment Initiation)

---

### 3.2 Synchronous Flow (Step-by-Step)

```
Step 1: Channel sends payment request
───────────────────────────────────────────────────────────────────────────────
Mobile App → Mobile BFF (POST /api/mobile/v1/payments)
{
  "idempotencyKey": "uuid-1234",
  "amount": 1000.00,
  "currency": "ZAR",
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "reference": "Electricity payment"
}

Step 2: BFF validates and forwards to Payment Initiation
───────────────────────────────────────────────────────────────────────────────
Mobile BFF → Payment Initiation Service (Internal REST)
POST /api/v1/payments
{
  "paymentId": "PAY-2025-XXXXXX",  // Generated by BFF
  "tenantId": "TENANT-001",
  "customerId": "CUST-123",
  "amount": 1000.00,
  ...
}

Step 3: Payment Initiation validates and stores
───────────────────────────────────────────────────────────────────────────────
Payment Initiation Service:
1. Check idempotency key (prevent duplicates)
2. Validate fields (amount, accounts, currency)
3. Store payment in database (status: INITIATED)
4. Publish PaymentInitiatedEvent to event bus (ASYNC)
5. Return synchronous response to BFF

Step 4: BFF returns response to channel
───────────────────────────────────────────────────────────────────────────────
Mobile BFF → Mobile App (Response in 500ms-2s)
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "INITIATED",
  "message": "Payment submitted successfully",
  "trackingUrl": "https://api.payments.io/payments/PAY-2025-XXXXXX/status"
}

Step 5: User sees confirmation
───────────────────────────────────────────────────────────────────────────────
Mobile App displays:
"✅ Payment submitted successfully
 Payment ID: PAY-2025-XXXXXX
 You will receive an SMS once the payment is completed."
```

**Key Point**: 
- Request is **SYNCHRONOUS** (client waits for acknowledgment)
- Internal processing is **ASYNCHRONOUS** (validation, debit, clearing, settlement happen via events)

---

### 3.3 What Happens After Synchronous Response?

Once the client receives the synchronous response, the **asynchronous processing** begins:

```
Time: T+0s (Synchronous)
───────────────────────────────────────────────────────────────────────────────
Payment Initiation publishes PaymentInitiatedEvent
Status: INITIATED
Client: Receives "Payment submitted" response

Time: T+0.5s (Asynchronous)
───────────────────────────────────────────────────────────────────────────────
Validation Service consumes PaymentInitiatedEvent
- Validates amount, account numbers, limits
- Calls Fraud API (sync call, 200ms)
- Publishes PaymentValidatedEvent
Status: VALIDATED

Time: T+1s (Asynchronous)
───────────────────────────────────────────────────────────────────────────────
Routing Service consumes PaymentValidatedEvent
- Determines clearing system (SAMOS, RTC, PayShap)
- Publishes RoutingDeterminedEvent
Status: ROUTED

Time: T+1.5s (Asynchronous)
───────────────────────────────────────────────────────────────────────────────
Account Adapter consumes RoutingDeterminedEvent
- Calls external core banking system (debit account)
- Publishes FundsDebitedEvent
Status: DEBITED

Time: T+2s (Asynchronous)
───────────────────────────────────────────────────────────────────────────────
Clearing Adapter consumes FundsDebitedEvent
- Submits to clearing system (SAMOS/RTC/PayShap)
- Publishes ClearingSubmittedEvent
Status: SUBMITTED_TO_CLEARING

Time: T+5-60s (Asynchronous - depends on clearing system)
───────────────────────────────────────────────────────────────────────────────
Clearing Adapter receives clearing acknowledgment
- Updates payment status
- Publishes ClearingCompletedEvent
Status: COMPLETED

Time: T+60s (Asynchronous)
───────────────────────────────────────────────────────────────────────────────
Notification Service consumes PaymentCompletedEvent
- Sends SMS to customer: "Payment PAY-2025-XXXXXX completed successfully"
- Sends push notification to mobile app
- Sends webhook to channel (if registered)
Status: COMPLETED + NOTIFIED
```

**Total Time**: 
- **Synchronous response**: 500ms-2s (payment accepted)
- **Full completion**: 5-60s (depends on clearing system)

---

## 4. Asynchronous Pattern (Status Updates)

### 4.1 Notification Mechanisms

Channels can receive **asynchronous status updates** via:

1. **Polling** (simple, but inefficient)
2. **Webhooks** (recommended for partner APIs)
3. **WebSocket Subscriptions** (real-time for web/mobile)
4. **Push Notifications** (mobile only)
5. **SMS/Email** (customer notifications)

---

### 4.2 Polling (Status Check)

**Pattern**: Client polls for status updates

**API**:
```bash
# Client polls every 5 seconds
GET /api/v1/payments/{paymentId}/status

Response:
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "COMPLETED",
  "lastUpdated": "2025-10-12T10:05:00Z",
  "events": [
    { "status": "INITIATED", "timestamp": "2025-10-12T10:00:00Z" },
    { "status": "VALIDATED", "timestamp": "2025-10-12T10:00:01Z" },
    { "status": "DEBITED", "timestamp": "2025-10-12T10:00:02Z" },
    { "status": "SUBMITTED_TO_CLEARING", "timestamp": "2025-10-12T10:00:03Z" },
    { "status": "COMPLETED", "timestamp": "2025-10-12T10:05:00Z" }
  ]
}
```

**Pros**: Simple to implement  
**Cons**: Inefficient (many unnecessary requests)

**Use Case**: Legacy systems that don't support webhooks

---

### 4.3 Webhooks (Asynchronous Callbacks)

**Pattern**: Payments Engine calls channel's webhook endpoint when status changes

**Registration**:
```bash
# Channel registers webhook during payment initiation
POST /api/partner/v1/payments
{
  "amount": 1000.00,
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "callbackUrl": "https://partner.bank.com/webhooks/payment-status",
  "callbackHeaders": {
    "Authorization": "Bearer secret-token-xyz"
  }
}
```

**Webhook Calls** (sent by Notification Service):

```bash
# Webhook 1: Payment validated
POST https://partner.bank.com/webhooks/payment-status
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "VALIDATED",
  "timestamp": "2025-10-12T10:00:01Z"
}

# Webhook 2: Payment completed
POST https://partner.bank.com/webhooks/payment-status
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "COMPLETED",
  "timestamp": "2025-10-12T10:05:00Z",
  "clearingReference": "SAMOS-REF-123"
}

# Webhook 3: Payment failed
POST https://partner.bank.com/webhooks/payment-status
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "FAILED",
  "timestamp": "2025-10-12T10:00:05Z",
  "failureReason": "Insufficient funds",
  "errorCode": "LIMIT_EXCEEDED"
}
```

**Retry Logic**: 
- Retry 3 times (1s, 5s, 30s delays)
- If all retries fail, channel must poll for status

**Use Case**: Partner APIs, corporate portals, batch processing

---

### 4.4 WebSocket Subscriptions (Real-Time)

**Pattern**: Web/Mobile apps subscribe to real-time updates via GraphQL subscriptions

**Web BFF GraphQL Subscription**:
```graphql
subscription PaymentUpdated($paymentId: ID!) {
  paymentUpdated(paymentId: $paymentId) {
    paymentId
    status
    timestamp
    message
  }
}
```

**Client-Side (React)**:
```typescript
// React component subscribes to payment updates
const { data, loading } = useSubscription(PAYMENT_UPDATED_SUBSCRIPTION, {
  variables: { paymentId: 'PAY-2025-XXXXXX' }
});

// When status changes, component automatically re-renders
useEffect(() => {
  if (data?.paymentUpdated?.status === 'COMPLETED') {
    showSuccessNotification('Payment completed successfully!');
  }
}, [data]);
```

**Server-Side (Web BFF)**:
```java
@Component
public class PaymentSubscriptionHandler {
    
    @Autowired
    private Sinks.Many<PaymentStatusUpdate> paymentSink;
    
    @SubscriptionMapping("paymentUpdated")
    public Flux<PaymentStatusUpdate> paymentUpdated(@Argument String paymentId) {
        return paymentSink.asFlux()
            .filter(update -> update.getPaymentId().equals(paymentId));
    }
    
    // Called by event handler when PaymentStatusChangedEvent is received
    @EventListener
    public void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        PaymentStatusUpdate update = PaymentStatusUpdate.builder()
            .paymentId(event.getPaymentId())
            .status(event.getStatus())
            .timestamp(event.getTimestamp())
            .build();
        
        paymentSink.tryEmitNext(update); // Push to all subscribers
    }
}
```

**Protocol**: WebSocket (ws:// or wss://)  
**Use Case**: Web banking, mobile banking (real-time updates)

---

### 4.5 Push Notifications (Mobile Only)

**Pattern**: Mobile apps receive push notifications via Firebase (Android) or APNs (iOS)

**Registration**:
```bash
# Mobile app registers push token during login
POST /api/mobile/v1/users/{userId}/push-token
{
  "platform": "android",  // or "ios"
  "token": "firebase-token-xyz"
}
```

**Push Notification** (sent by Notification Service):
```json
{
  "to": "firebase-token-xyz",
  "notification": {
    "title": "Payment Completed",
    "body": "Your payment of R 1,000 to John Doe has been completed.",
    "icon": "payment_success",
    "sound": "default"
  },
  "data": {
    "paymentId": "PAY-2025-XXXXXX",
    "status": "COMPLETED",
    "amount": 1000.00
  }
}
```

**Use Case**: Mobile banking (background notifications)

---

### 4.6 SMS/Email (Customer Notifications)

**Pattern**: Customers receive SMS/email notifications (not for channels)

**SMS Notification**:
```
Dear Customer,
Your payment of R 1,000 to John Doe has been completed.
Payment ID: PAY-2025-XXXXXX
Time: 2025-10-12 10:05 AM
```

**Email Notification**:
```html
<h2>Payment Confirmation</h2>
<p>Your payment has been completed successfully.</p>
<table>
  <tr><td>Payment ID:</td><td>PAY-2025-XXXXXX</td></tr>
  <tr><td>Amount:</td><td>R 1,000.00</td></tr>
  <tr><td>Recipient:</td><td>John Doe (0987654321)</td></tr>
  <tr><td>Time:</td><td>2025-10-12 10:05 AM</td></tr>
</table>
```

**Use Case**: Customer confirmations (not channel integrations)

---

## 5. BFF Layer Integration

### 5.1 Web BFF (GraphQL for Web Banking)

**Client**: React web application  
**Protocol**: GraphQL over HTTP  
**Port**: 8090

**GraphQL Schema**:
```graphql
type Query {
  # Dashboard query (aggregates payment + account data)
  paymentDashboard(customerId: ID!): PaymentDashboard!
  
  # Individual payment query
  payment(paymentId: ID!): Payment
  
  # List payments with filters
  payments(
    filter: PaymentFilter
    pagination: Pagination
  ): PaymentConnection!
}

type Mutation {
  # Initiate payment (SYNCHRONOUS)
  initiatePayment(input: InitiatePaymentInput!): PaymentResponse!
  
  # Cancel payment (SYNCHRONOUS)
  cancelPayment(paymentId: ID!): PaymentResponse!
}

type Subscription {
  # Subscribe to payment status updates (ASYNCHRONOUS)
  paymentUpdated(paymentId: ID!): PaymentStatusUpdate!
  
  # Subscribe to all payments for a customer
  paymentsUpdated(customerId: ID!): PaymentStatusUpdate!
}

type PaymentResponse {
  paymentId: ID!
  status: PaymentStatus!
  message: String!
  trackingUrl: String!
}

type PaymentStatusUpdate {
  paymentId: ID!
  status: PaymentStatus!
  timestamp: DateTime!
  message: String
}
```

**Example Flow**:
```typescript
// React component initiates payment
const [initiatePayment] = useMutation(INITIATE_PAYMENT_MUTATION);

const handleSubmit = async () => {
  // SYNCHRONOUS request
  const result = await initiatePayment({
    variables: {
      input: {
        amount: 1000.00,
        debtorAccount: '1234567890',
        creditorAccount: '0987654321',
        reference: 'Electricity'
      }
    }
  });
  
  // Get payment ID from synchronous response
  const paymentId = result.data.initiatePayment.paymentId;
  
  // Subscribe to ASYNCHRONOUS updates
  subscribeToPaymentUpdates(paymentId);
};

const subscribeToPaymentUpdates = (paymentId) => {
  const { data } = useSubscription(PAYMENT_UPDATED_SUBSCRIPTION, {
    variables: { paymentId }
  });
  
  // Real-time updates
  console.log('Payment status:', data?.paymentUpdated?.status);
};
```

---

### 5.2 Mobile BFF (REST Lightweight for Mobile Banking)

**Client**: iOS/Android mobile app  
**Protocol**: REST (JSON)  
**Port**: 8091

**Key Features**:
- Lightweight payloads (minimal data transfer)
- Offline support (queue payments, submit when online)
- Push notification registration
- Image optimization (compress QR codes, receipts)

**API Endpoints**:
```bash
# Initiate payment (SYNCHRONOUS)
POST /api/mobile/v1/payments
Request:
{
  "amount": 1000.00,
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "reference": "Electricity"
}

Response (200 OK in 500ms-2s):
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "INITIATED",
  "message": "Payment submitted successfully"
}

# Check payment status (POLLING)
GET /api/mobile/v1/payments/{paymentId}/status

Response:
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "COMPLETED",
  "timestamp": "2025-10-12T10:05:00Z"
}

# Register push token (for ASYNCHRONOUS notifications)
POST /api/mobile/v1/users/{userId}/push-token
{
  "platform": "android",
  "token": "firebase-token-xyz"
}
```

---

### 5.3 Partner BFF (REST Comprehensive for Corporate/Partner)

**Client**: Corporate portals, partner systems  
**Protocol**: REST (JSON)  
**Port**: 8092

**Key Features**:
- Bulk operations (batch payments)
- Webhook registration
- Comprehensive responses (all payment details)
- CSV/Excel upload support

**API Endpoints**:
```bash
# Initiate single payment (SYNCHRONOUS)
POST /api/partner/v1/payments
Request:
{
  "amount": 1000.00,
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "reference": "Invoice-12345",
  "callbackUrl": "https://partner.com/webhook"  // For ASYNC updates
}

Response (201 Created in 1-3s):
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "INITIATED",
  "submittedAt": "2025-10-12T10:00:00Z",
  "estimatedCompletion": "2025-10-12T10:05:00Z",
  "trackingUrl": "https://api.payments.io/payments/PAY-2025-XXXXXX",
  "webhookRegistered": true
}

# Initiate batch payments (ASYNCHRONOUS)
POST /api/partner/v1/payments/batch
Request:
{
  "batchId": "BATCH-2025-001",
  "payments": [
    { "amount": 1000.00, "debtorAccount": "...", "creditorAccount": "..." },
    { "amount": 2000.00, "debtorAccount": "...", "creditorAccount": "..." },
    ... (1000 payments)
  ],
  "callbackUrl": "https://partner.com/webhook/batch"
}

Response (202 Accepted in 200ms):
{
  "batchId": "BATCH-2025-001",
  "status": "PROCESSING",
  "totalPayments": 1000,
  "submittedAt": "2025-10-12T10:00:00Z",
  "estimatedCompletion": "2025-10-12T10:15:00Z",
  "trackingUrl": "https://api.payments.io/batch/BATCH-2025-001"
}

# Check batch status (POLLING)
GET /api/partner/v1/payments/batch/{batchId}/status

Response:
{
  "batchId": "BATCH-2025-001",
  "status": "COMPLETED",
  "totalPayments": 1000,
  "successCount": 995,
  "failedCount": 5,
  "completedAt": "2025-10-12T10:12:00Z"
}
```

**Webhook Callback** (ASYNCHRONOUS):
```bash
POST https://partner.com/webhook/batch
{
  "batchId": "BATCH-2025-001",
  "status": "COMPLETED",
  "totalPayments": 1000,
  "successCount": 995,
  "failedCount": 5,
  "failedPayments": [
    { "paymentId": "PAY-2025-123", "failureReason": "Insufficient funds" },
    { "paymentId": "PAY-2025-456", "failureReason": "Invalid account" }
  ]
}
```

---

## 6. Request Flow (Detailed)

### 6.1 Web Banking Payment Flow

```
User Action: "Pay R 1,000 to John Doe"
───────────────────────────────────────────────────────────────────────────────

Step 1: React app sends GraphQL mutation (SYNCHRONOUS)
───────────────────────────────────────────────────────────────────────────────
POST https://api.payments.io/graphql
Authorization: Bearer <JWT token from Azure AD B2C>
X-Idempotency-Key: uuid-1234-5678
X-Correlation-ID: corr-uuid-abcd

{
  "query": "mutation InitiatePayment($input: InitiatePaymentInput!) { 
    initiatePayment(input: $input) { 
      paymentId status message 
    } 
  }",
  "variables": {
    "input": {
      "amount": 1000.00,
      "currency": "ZAR",
      "debtorAccount": "1234567890",
      "creditorAccount": "0987654321",
      "reference": "Electricity payment"
    }
  }
}

Step 2: Azure Application Gateway (Layer 1)
───────────────────────────────────────────────────────────────────────────────
- Check WAF rules (block if malicious)
- SSL termination (HTTPS → HTTP)
- Forward to Azure API Management

Step 3: Azure API Management (Layer 2)
───────────────────────────────────────────────────────────────────────────────
- Validate JWT token (Azure AD B2C)
- Check rate limit (1000 req/min per user)
- Log request
- Forward to Web BFF

Step 4: Web BFF (Layer 3)
───────────────────────────────────────────────────────────────────────────────
- Parse GraphQL query
- Extract tenantId from JWT token
- Validate input (amount > 0, valid account format)
- Generate paymentId: "PAY-2025-XXXXXX"
- Call Payment Initiation Service (internal REST call)

POST http://payment-initiation-service:8080/api/v1/payments
X-Tenant-ID: TENANT-001
X-User-ID: USER-123
X-Correlation-ID: corr-uuid-abcd
{
  "paymentId": "PAY-2025-XXXXXX",
  "tenantId": "TENANT-001",
  "customerId": "CUST-123",
  "amount": 1000.00,
  "currency": "ZAR",
  "debtorAccount": "1234567890",
  "creditorAccount": "0987654321",
  "reference": "Electricity payment",
  "idempotencyKey": "uuid-1234-5678"
}

Step 5: Payment Initiation Service
───────────────────────────────────────────────────────────────────────────────
Time: 0ms
- Check idempotency key in Redis (prevent duplicate)
- Validate required fields
- Store payment in PostgreSQL (status: INITIATED)

Time: 50ms
- Publish PaymentInitiatedEvent to Azure Service Bus
  {
    "eventId": "EVENT-UUID",
    "eventType": "PaymentInitiatedEvent",
    "paymentId": "PAY-2025-XXXXXX",
    "timestamp": "2025-10-12T10:00:00.100Z",
    "payload": { ... }
  }

Time: 100ms
- Return synchronous response to Web BFF
  {
    "paymentId": "PAY-2025-XXXXXX",
    "status": "INITIATED",
    "message": "Payment submitted successfully"
  }

Step 6: Web BFF returns GraphQL response
───────────────────────────────────────────────────────────────────────────────
Time: 500ms (total round-trip)
{
  "data": {
    "initiatePayment": {
      "paymentId": "PAY-2025-XXXXXX",
      "status": "INITIATED",
      "message": "Payment submitted successfully"
    }
  }
}

Step 7: React app displays confirmation
───────────────────────────────────────────────────────────────────────────────
"✅ Payment submitted successfully
 Payment ID: PAY-2025-XXXXXX
 
 Track your payment status below."

Step 8: React app subscribes to WebSocket (ASYNCHRONOUS)
───────────────────────────────────────────────────────────────────────────────
const { data } = useSubscription(PAYMENT_UPDATED_SUBSCRIPTION, {
  variables: { paymentId: 'PAY-2025-XXXXXX' }
});

Step 9: Asynchronous processing begins (Event-Driven)
───────────────────────────────────────────────────────────────────────────────
Time: 500ms - Validation Service processes PaymentInitiatedEvent
Time: 1s - Routing Service determines clearing system
Time: 1.5s - Account Adapter debits account
Time: 2s - Clearing Adapter submits to SAMOS
Time: 60s - Clearing Adapter receives acknowledgment
Status: COMPLETED

Step 10: WebSocket push to React app (ASYNCHRONOUS)
───────────────────────────────────────────────────────────────────────────────
React app receives:
{
  "paymentUpdated": {
    "paymentId": "PAY-2025-XXXXXX",
    "status": "COMPLETED",
    "timestamp": "2025-10-12T10:01:00Z"
  }
}

React app displays:
"✅ Payment completed successfully!"
```

---

## 7. API Specifications

### 7.1 REST API (Mobile BFF, Partner BFF)

**Base URL**: 
- Mobile: `https://api.payments.io/api/mobile/v1`
- Partner: `https://api.payments.io/api/partner/v1`

**Authentication**: 
- OAuth 2.0 Bearer token (Azure AD B2C)
- API key (for partner APIs)

**Headers**:
```http
Authorization: Bearer <JWT token>
X-API-Key: <partner API key>  # Partner APIs only
X-Idempotency-Key: <UUID>  # Required for POST
X-Correlation-ID: <UUID>  # Optional (for tracing)
X-Tenant-ID: <Tenant ID>  # Extracted from JWT
```

**Endpoints**:

```yaml
# Payment Initiation (SYNCHRONOUS)
POST /payments
Request:
  amount: float (required, > 0)
  currency: string (required, ISO 4217)
  debtorAccount: string (required, 10 digits)
  creditorAccount: string (required, 10 digits)
  reference: string (optional, max 140 chars)
  callbackUrl: string (optional, HTTPS URL)  # Partner APIs only

Response (201 Created):
  paymentId: string
  status: enum (INITIATED)
  submittedAt: datetime
  trackingUrl: string
  
Response (400 Bad Request):
  error: string
  code: string (INVALID_AMOUNT, INVALID_ACCOUNT, etc.)

Response (429 Too Many Requests):
  error: "Rate limit exceeded"
  retryAfter: integer (seconds)

# Payment Status (POLLING)
GET /payments/{paymentId}/status

Response (200 OK):
  paymentId: string
  status: enum (INITIATED, VALIDATED, DEBITED, SUBMITTED_TO_CLEARING, COMPLETED, FAILED)
  lastUpdated: datetime
  events: array of status transitions

# Cancel Payment (SYNCHRONOUS)
POST /payments/{paymentId}/cancel

Response (200 OK):
  paymentId: string
  status: enum (CANCELLED)
  cancelledAt: datetime
  
Response (409 Conflict):
  error: "Payment already completed, cannot cancel"
```

---

### 7.2 GraphQL API (Web BFF)

**Endpoint**: `https://api.payments.io/graphql`

**Schema**:
```graphql
type Query {
  payment(paymentId: ID!): Payment
  payments(filter: PaymentFilter, pagination: Pagination): PaymentConnection
}

type Mutation {
  initiatePayment(input: InitiatePaymentInput!): PaymentResponse!
  cancelPayment(paymentId: ID!): CancelResponse!
}

type Subscription {
  paymentUpdated(paymentId: ID!): PaymentStatusUpdate!
}

input InitiatePaymentInput {
  amount: Float!
  currency: String!
  debtorAccount: String!
  creditorAccount: String!
  reference: String
}

type PaymentResponse {
  paymentId: ID!
  status: PaymentStatus!
  message: String!
  trackingUrl: String!
}

type PaymentStatusUpdate {
  paymentId: ID!
  status: PaymentStatus!
  timestamp: DateTime!
  message: String
}

enum PaymentStatus {
  INITIATED
  VALIDATED
  DEBITED
  SUBMITTED_TO_CLEARING
  COMPLETED
  FAILED
  CANCELLED
}
```

---

## 8. Authentication & Authorization

### 8.1 Azure AD B2C Integration

**Flow**: OAuth 2.0 Authorization Code Flow

```
Step 1: User logs into Web/Mobile Banking
───────────────────────────────────────────────────────────────────────────────
User → Bank's Login Page (React)
- Enter username + password
- Bank redirects to Azure AD B2C

Step 2: Azure AD B2C authenticates user
───────────────────────────────────────────────────────────────────────────────
Azure AD B2C:
- Validates credentials
- MFA (if enabled)
- Returns authorization code

Step 3: Bank exchanges code for token
───────────────────────────────────────────────────────────────────────────────
Bank → Azure AD B2C:
POST /oauth2/v2.0/token
{
  "grant_type": "authorization_code",
  "client_id": "payments-web-client",
  "client_secret": "secret",
  "code": "auth-code-xyz",
  "redirect_uri": "https://portal.payments.io/callback"
}

Response:
{
  "access_token": "eyJhbGciOiJSUzI1...",  # JWT token
  "token_type": "Bearer",
  "expires_in": 3600,  # 1 hour
  "refresh_token": "refresh-token-xyz"
}

Step 4: Bank includes token in API requests
───────────────────────────────────────────────────────────────────────────────
POST https://api.payments.io/graphql
Authorization: Bearer eyJhbGciOiJSUzI1...

Step 5: Azure API Management validates token
───────────────────────────────────────────────────────────────────────────────
APIM:
- Validates JWT signature (public key from Azure AD B2C)
- Checks expiry
- Extracts claims (tenantId, userId, roles)
- Forwards to BFF
```

**JWT Token Claims**:
```json
{
  "sub": "USER-123",  # User ID
  "tenantId": "TENANT-001",  # Tenant ID
  "customerId": "CUST-123",  # Customer ID
  "roles": ["CUSTOMER", "PREMIUM"],  # User roles
  "iss": "https://login.microsoftonline.com/tenant-id/v2.0",
  "aud": "payments-api",
  "exp": 1696780800,  # Expiry timestamp
  "iat": 1696777200  # Issued at
}
```

---

### 8.2 API Key (Partner APIs Only)

**Flow**: API key in header

```
Partner → Partner BFF:
GET /api/partner/v1/payments/{paymentId}
X-API-Key: partner-api-key-xyz

Partner BFF:
- Validates API key against database
- Rate limit check (per partner)
- Extract tenantId from API key
- Forward to Payment Initiation
```

---

## 9. Error Handling

### 9.1 Synchronous Errors

**HTTP Status Codes**:

| Status | Scenario | Response |
|--------|----------|----------|
| **200 OK** | Successful GET request | Payment details |
| **201 Created** | Payment initiated successfully | Payment ID + status |
| **400 Bad Request** | Invalid request (validation error) | Error message + code |
| **401 Unauthorized** | Missing or invalid JWT token | "Authentication required" |
| **403 Forbidden** | User lacks permission | "Access denied" |
| **404 Not Found** | Payment ID not found | "Payment not found" |
| **409 Conflict** | Duplicate idempotency key | "Payment already submitted" |
| **422 Unprocessable Entity** | Business logic error (insufficient funds) | Error message + code |
| **429 Too Many Requests** | Rate limit exceeded | "Retry after 60 seconds" |
| **500 Internal Server Error** | Server error | "Internal error, please retry" |
| **503 Service Unavailable** | Service down (circuit breaker open) | "Service temporarily unavailable" |

**Error Response Format**:
```json
{
  "error": "Insufficient funds",
  "code": "LIMIT_EXCEEDED",
  "timestamp": "2025-10-12T10:00:00Z",
  "correlationId": "corr-uuid-abcd",
  "details": {
    "availableBalance": 500.00,
    "requestedAmount": 1000.00
  }
}
```

---

### 9.2 Asynchronous Errors (Status = FAILED)

**Failure Reasons**:

| Failure Reason | Description | Recoverable? |
|----------------|-------------|--------------|
| **VALIDATION_FAILED** | Invalid account number | ❌ No |
| **FRAUD_DETECTED** | High fraud score | ❌ No |
| **LIMIT_EXCEEDED** | Customer limit exceeded | ❌ No (retry tomorrow) |
| **INSUFFICIENT_FUNDS** | Debit account has insufficient balance | ❌ No (add funds) |
| **ACCOUNT_BLOCKED** | Account is blocked/frozen | ❌ No |
| **CLEARING_TIMEOUT** | Clearing system timeout | ✅ Yes (retry) |
| **CLEARING_REJECTED** | Clearing system rejected | ❌ No |
| **SYSTEM_ERROR** | Internal error | ✅ Yes (retry) |

**Webhook/WebSocket Notification**:
```json
{
  "paymentId": "PAY-2025-XXXXXX",
  "status": "FAILED",
  "timestamp": "2025-10-12T10:00:05Z",
  "failureReason": "INSUFFICIENT_FUNDS",
  "failureMessage": "Debit account has insufficient funds",
  "errorCode": "LIMIT_EXCEEDED",
  "retryable": false
}
```

---

## 10. Performance Characteristics

### 10.1 Latency Targets

| Operation | Target Latency | Max Latency | Timeout |
|-----------|----------------|-------------|---------|
| **Payment Initiation (sync response)** | 500ms | 2s | 30s (client) |
| **Payment Status Check (polling)** | 200ms | 500ms | 10s |
| **Webhook Delivery** | 1s | 5s | 30s |
| **WebSocket Push** | 100ms | 500ms | N/A |
| **Full Payment Completion (async)** | 5-10s (RTC/PayShap) | 60s (SAMOS) | N/A |

---

### 10.2 Throughput Targets

| Channel | TPS (Transactions Per Second) | Daily Volume |
|---------|-------------------------------|--------------|
| **Web Banking** | 500 TPS | 10M payments/day |
| **Mobile Banking** | 1000 TPS | 20M payments/day |
| **Partner APIs** | 200 TPS | 5M payments/day |
| **Branch Systems** | 50 TPS | 1M payments/day |
| **Total** | **1750 TPS** | **36M payments/day** |

---

### 10.3 Rate Limiting

| Channel | Rate Limit | Burst Capacity | Window |
|---------|-----------|----------------|--------|
| **Web Banking (per user)** | 100 req/min | 200 req/min | 60s |
| **Mobile Banking (per user)** | 50 req/min | 100 req/min | 60s |
| **Partner APIs (per partner)** | 1000 req/min | 2000 req/min | 60s |
| **Batch Upload (per partner)** | 10 batches/hour | 20 batches/hour | 3600s |

---

## Summary

### Key Takeaways

1. **HYBRID Pattern**:
   - ✅ **SYNCHRONOUS** for payment initiation (request/response in 500ms-2s)
   - ✅ **ASYNCHRONOUS** for internal processing (event-driven, 5-60s)
   - ✅ **ASYNCHRONOUS** for status updates (webhooks, WebSocket, push)

2. **Integration Layers**:
   - Layer 1: Azure Application Gateway (WAF, SSL, DDoS)
   - Layer 2: Azure API Management (versioning, rate limiting)
   - Layer 3: BFF Layer (Web, Mobile, Partner - client-optimized)
   - Layer 4: Internal microservices (event-driven)

3. **Channel Support**:
   - ✅ Web Banking (GraphQL)
   - ✅ Mobile Banking (REST lightweight)
   - ✅ Branch Systems (REST)
   - ✅ Corporate Portals (REST bulk)
   - ✅ Partner APIs (REST comprehensive + webhooks)

4. **Performance**:
   - Sync response: 500ms-2s
   - Full completion: 5-60s (depends on clearing system)
   - Throughput: 1750 TPS (36M payments/day)

5. **Error Handling**:
   - Synchronous errors: HTTP status codes + error response
   - Asynchronous errors: WebSocket/webhook with failure reason

---

**Document Version**: 1.0  
**Last Updated**: 2025-10-12  
**Status**: ✅ Complete  
**Related Documents**: 
- `docs/15-BFF-IMPLEMENTATION.md`
- `docs/32-GATEWAY-ARCHITECTURE-CLARIFICATION.md`
- `docs/02-MICROSERVICES-BREAKDOWN.md`
