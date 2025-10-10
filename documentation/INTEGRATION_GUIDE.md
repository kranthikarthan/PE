# Payment Engine Integration Guide

## Overview

This guide provides comprehensive instructions for integrating with the Payment Engine API. It covers authentication, common integration patterns, SDK usage, webhook handling, and best practices for building robust payment integrations.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [Integration Patterns](#integration-patterns)
4. [SDK Usage](#sdk-usage)
5. [Webhook Integration](#webhook-integration)
6. [Error Handling](#error-handling)
7. [Testing](#testing)
8. [Best Practices](#best-practices)
9. [Examples](#examples)

## Getting Started

### Prerequisites

- API credentials (API key and secret)
- Understanding of RESTful APIs and HTTP
- Development environment with internet access
- SSL/TLS support for secure communications

### Base URLs

| Environment | Base URL |
|-------------|----------|
| Production | `https://api.payment-engine.com` |
| Staging | `https://staging-api.payment-engine.com` |
| Sandbox | `https://sandbox-api.payment-engine.com` |

### API Versioning

The Payment Engine API uses URL-based versioning:
- Current version: `v1`
- Full endpoint format: `{base_url}/api/v1/{resource}`

## Authentication

### OAuth 2.0 Flow

The Payment Engine uses OAuth 2.0 with JWT tokens for authentication.

#### Step 1: Obtain Access Token

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "your-username",
  "password": "your-password"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "refresh_token_here",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "transaction:read transaction:create account:read"
}
```

#### Step 2: Use Access Token

Include the access token in the Authorization header for all API requests:

```http
GET /api/v1/transactions
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Step 3: Refresh Token

When the access token expires, use the refresh token to obtain a new one:

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refresh_token": "refresh_token_here"
}
```

### API Key Authentication (Alternative)

For server-to-server integrations, you can use API key authentication:

```http
GET /api/v1/transactions
X-API-Key: your-api-key
```

## Integration Patterns

### 1. Synchronous Payment Processing

For real-time payment processing where you need immediate confirmation:

```javascript
async function processPayment(paymentData) {
  try {
    const response = await paymentClient.transactions.create({
      fromAccountId: paymentData.fromAccount,
      toAccountId: paymentData.toAccount,
      paymentTypeId: 'RTP', // Real-time payment
      amount: paymentData.amount,
      description: paymentData.description
    });
    
    // For synchronous payments, check status immediately
    if (response.status === 'COMPLETED') {
      return { success: true, transactionId: response.id };
    } else if (response.status === 'FAILED') {
      return { success: false, error: response.failureReason };
    }
    
    // Poll for status if still processing
    return await pollTransactionStatus(response.id);
  } catch (error) {
    return { success: false, error: error.message };
  }
}
```

### 2. Asynchronous Payment Processing

For batch processing or non-urgent payments:

```javascript
async function submitPaymentForProcessing(paymentData) {
  try {
    const response = await paymentClient.transactions.create({
      fromAccountId: paymentData.fromAccount,
      toAccountId: paymentData.toAccount,
      paymentTypeId: 'ACH_CREDIT', // Asynchronous payment
      amount: paymentData.amount,
      description: paymentData.description,
      externalReference: paymentData.orderId
    });
    
    // Store transaction ID for later status checking
    await storeTransactionReference(paymentData.orderId, response.id);
    
    return { 
      submitted: true, 
      transactionId: response.id,
      expectedProcessingTime: '24 hours'
    };
  } catch (error) {
    return { submitted: false, error: error.message };
  }
}
```

### 3. Account Balance Checking

Before processing payments, check account balances:

```javascript
async function checkAccountBalance(accountId, requiredAmount) {
  try {
    const balance = await paymentClient.accounts.getBalance(accountId);
    
    return {
      hasSufficientFunds: balance.availableBalance >= requiredAmount,
      availableBalance: balance.availableBalance,
      currency: balance.currencyCode
    };
  } catch (error) {
    throw new Error(`Failed to check balance: ${error.message}`);
  }
}
```

### 4. Transaction Status Monitoring

Implement polling or webhook-based status monitoring:

```javascript
async function pollTransactionStatus(transactionId, maxAttempts = 10) {
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      const status = await paymentClient.transactions.getStatus(transactionId);
      
      if (status.status === 'COMPLETED') {
        return { success: true, transaction: status };
      } else if (status.status === 'FAILED') {
        return { success: false, error: status.failureReason };
      }
      
      // Wait before next poll (exponential backoff)
      await sleep(Math.pow(2, attempt) * 1000);
    } catch (error) {
      if (attempt === maxAttempts) throw error;
    }
  }
  
  throw new Error('Transaction status polling timed out');
}
```

## SDK Usage

### JavaScript/Node.js SDK

#### Installation
```bash
npm install @payment-engine/sdk
```

#### Basic Usage
```javascript
const PaymentEngine = require('@payment-engine/sdk');

// Initialize client
const client = new PaymentEngine({
  apiKey: process.env.PAYMENT_ENGINE_API_KEY,
  baseUrl: 'https://api.payment-engine.com',
  timeout: 30000
});

// Create a transaction
async function createPayment() {
  try {
    const transaction = await client.transactions.create({
      fromAccountId: 'acc_123',
      toAccountId: 'acc_456',
      paymentTypeId: 'wire_domestic',
      amount: 1000.00,
      currencyCode: 'USD',
      description: 'Invoice payment',
      metadata: {
        invoiceId: 'INV-001',
        customerId: 'CUST-123'
      }
    });
    
    console.log('Transaction created:', transaction.id);
    return transaction;
  } catch (error) {
    console.error('Payment failed:', error.message);
    throw error;
  }
}

// Get transaction status
async function checkPaymentStatus(transactionId) {
  try {
    const status = await client.transactions.getStatus(transactionId);
    return status;
  } catch (error) {
    console.error('Status check failed:', error.message);
    throw error;
  }
}
```

#### Advanced Configuration
```javascript
const client = new PaymentEngine({
  apiKey: process.env.PAYMENT_ENGINE_API_KEY,
  baseUrl: 'https://api.payment-engine.com',
  timeout: 30000,
  retries: 3,
  retryDelay: 1000,
  onRequest: (config) => {
    console.log('Making request:', config.method, config.url);
  },
  onResponse: (response) => {
    console.log('Received response:', response.status);
  },
  onError: (error) => {
    console.error('Request failed:', error.message);
  }
});
```

### Python SDK

#### Installation
```bash
pip install payment-engine-sdk
```

#### Basic Usage
```python
from payment_engine import PaymentEngineClient, CreateTransactionRequest

# Initialize client
client = PaymentEngineClient(
    api_key=os.environ['PAYMENT_ENGINE_API_KEY'],
    base_url='https://api.payment-engine.com',
    timeout=30
)

# Create a transaction
def create_payment():
    try:
        request = CreateTransactionRequest(
            from_account_id='acc_123',
            to_account_id='acc_456',
            payment_type_id='wire_domestic',
            amount=1000.00,
            currency_code='USD',
            description='Invoice payment',
            metadata={
                'invoice_id': 'INV-001',
                'customer_id': 'CUST-123'
            }
        )
        
        transaction = client.transactions.create(request)
        print(f'Transaction created: {transaction.id}')
        return transaction
    except Exception as error:
        print(f'Payment failed: {error}')
        raise

# Get transaction status
def check_payment_status(transaction_id):
    try:
        status = client.transactions.get_status(transaction_id)
        return status
    except Exception as error:
        print(f'Status check failed: {error}')
        raise
```

### Java SDK

#### Maven Dependency
```xml
<dependency>
    <groupId>com.paymentengine</groupId>
    <artifactId>payment-engine-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Basic Usage
```java
import com.paymentengine.PaymentEngineClient;
import com.paymentengine.model.*;

// Initialize client
PaymentEngineClient client = new PaymentEngineClient.Builder()
    .apiKey(System.getenv("PAYMENT_ENGINE_API_KEY"))
    .baseUrl("https://api.payment-engine.com")
    .timeout(Duration.ofSeconds(30))
    .build();

// Create a transaction
public Transaction createPayment() {
    try {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
            .fromAccountId("acc_123")
            .toAccountId("acc_456")
            .paymentTypeId("wire_domestic")
            .amount(new BigDecimal("1000.00"))
            .currencyCode("USD")
            .description("Invoice payment")
            .metadata(Map.of(
                "invoiceId", "INV-001",
                "customerId", "CUST-123"
            ))
            .build();
        
        Transaction transaction = client.transactions().create(request);
        System.out.println("Transaction created: " + transaction.getId());
        return transaction;
    } catch (PaymentEngineException e) {
        System.err.println("Payment failed: " + e.getMessage());
        throw e;
    }
}

// Get transaction status
public TransactionStatus checkPaymentStatus(String transactionId) {
    try {
        return client.transactions().getStatus(transactionId);
    } catch (PaymentEngineException e) {
        System.err.println("Status check failed: " + e.getMessage());
        throw e;
    }
}
```

## Webhook Integration

Webhooks provide real-time notifications about transaction events.

### Setting Up Webhooks

1. **Configure Webhook URL**: Set your webhook endpoint in the developer portal
2. **Verify Webhook Signature**: Implement signature verification for security
3. **Handle Webhook Events**: Process incoming webhook notifications

### Webhook Signature Verification

#### Node.js Example
```javascript
const crypto = require('crypto');

function verifyWebhookSignature(payload, signature, secret) {
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(payload, 'utf8')
    .digest('hex');
  
  const expectedHeader = `sha256=${expectedSignature}`;
  
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expectedHeader)
  );
}

// Express.js webhook handler
app.post('/webhooks/payment-engine', express.raw({ type: 'application/json' }), (req, res) => {
  const signature = req.headers['x-webhook-signature'];
  const payload = req.body;
  
  if (!verifyWebhookSignature(payload, signature, process.env.WEBHOOK_SECRET)) {
    return res.status(401).send('Invalid signature');
  }
  
  const event = JSON.parse(payload);
  handleWebhookEvent(event);
  
  res.status(200).send('OK');
});
```

#### Python Example
```python
import hmac
import hashlib

def verify_webhook_signature(payload, signature, secret):
    expected_signature = hmac.new(
        secret.encode('utf-8'),
        payload.encode('utf-8'),
        hashlib.sha256
    ).hexdigest()
    
    expected_header = f"sha256={expected_signature}"
    
    return hmac.compare_digest(expected_header, signature)

# Flask webhook handler
@app.route('/webhooks/payment-engine', methods=['POST'])
def handle_webhook():
    signature = request.headers.get('X-Webhook-Signature')
    payload = request.get_data(as_text=True)
    
    if not verify_webhook_signature(payload, signature, os.environ['WEBHOOK_SECRET']):
        return 'Invalid signature', 401
    
    event = request.get_json()
    handle_webhook_event(event)
    
    return 'OK', 200
```

### Webhook Event Handling

```javascript
function handleWebhookEvent(event) {
  switch (event.eventType) {
    case 'transaction.created':
      console.log('Transaction created:', event.data.transactionId);
      // Update internal records
      break;
      
    case 'transaction.completed':
      console.log('Transaction completed:', event.data.transactionId);
      // Mark order as paid, send confirmation email, etc.
      updateOrderStatus(event.data.transactionReference, 'paid');
      sendConfirmationEmail(event.data);
      break;
      
    case 'transaction.failed':
      console.log('Transaction failed:', event.data.transactionId);
      // Handle payment failure
      handlePaymentFailure(event.data);
      break;
      
    case 'account.balance.updated':
      console.log('Account balance updated:', event.data.accountId);
      // Update cached balance information
      updateCachedBalance(event.data);
      break;
      
    default:
      console.log('Unknown event type:', event.eventType);
  }
}
```

### Webhook Retry Logic

Implement idempotent webhook handling to manage retries:

```javascript
const processedEvents = new Set();

function handleWebhookEvent(event) {
  // Check if event already processed (idempotency)
  if (processedEvents.has(event.eventId)) {
    console.log('Event already processed:', event.eventId);
    return;
  }
  
  try {
    // Process the event
    processEvent(event);
    
    // Mark as processed
    processedEvents.add(event.eventId);
    
    // Persist to database for durability
    saveProcessedEventId(event.eventId);
  } catch (error) {
    console.error('Error processing webhook event:', error);
    throw error; // This will cause webhook to be retried
  }
}
```

## Error Handling

### Error Response Format

```json
{
  "error": {
    "code": "INSUFFICIENT_FUNDS",
    "message": "Account does not have sufficient funds for this transaction",
    "details": {
      "accountId": "acc_123",
      "requestedAmount": 1000.00,
      "availableBalance": 500.00
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req_abc123"
  }
}
```

### Common Error Codes

| Code | HTTP Status | Description | Action |
|------|-------------|-------------|---------|
| `INVALID_REQUEST` | 400 | Request validation failed | Fix request parameters |
| `UNAUTHORIZED` | 401 | Authentication required | Refresh access token |
| `FORBIDDEN` | 403 | Insufficient permissions | Check API key permissions |
| `NOT_FOUND` | 404 | Resource not found | Verify resource ID |
| `INSUFFICIENT_FUNDS` | 422 | Account has insufficient funds | Check account balance |
| `INVALID_ACCOUNT_STATUS` | 422 | Account is not active | Verify account status |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests | Implement backoff strategy |
| `INTERNAL_ERROR` | 500 | Internal server error | Retry request |
| `SERVICE_UNAVAILABLE` | 503 | Service temporarily unavailable | Retry with backoff |

### Retry Strategy

Implement exponential backoff for retryable errors:

```javascript
async function makeRequestWithRetry(requestFn, maxRetries = 3) {
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await requestFn();
    } catch (error) {
      const isRetryable = [429, 500, 502, 503, 504].includes(error.status);
      
      if (!isRetryable || attempt === maxRetries) {
        throw error;
      }
      
      // Exponential backoff: 1s, 2s, 4s, ...
      const delay = Math.pow(2, attempt - 1) * 1000;
      await sleep(delay);
    }
  }
}
```

## Testing

### Test Environment

Use the sandbox environment for testing:
- Base URL: `https://sandbox-api.payment-engine.com`
- Test credentials provided in developer portal
- Pre-populated test accounts and payment types

### Test Scenarios

#### 1. Successful Payment Flow
```javascript
describe('Successful Payment Flow', () => {
  it('should create and complete a payment', async () => {
    // Create transaction
    const transaction = await client.transactions.create({
      fromAccountId: 'test_acc_001',
      toAccountId: 'test_acc_002',
      paymentTypeId: 'rtp',
      amount: 100.00,
      description: 'Test payment'
    });
    
    expect(transaction.status).toBe('PENDING');
    
    // Wait for completion (in test environment, this is immediate)
    await sleep(1000);
    
    const status = await client.transactions.getStatus(transaction.id);
    expect(status.status).toBe('COMPLETED');
  });
});
```

#### 2. Insufficient Funds Scenario
```javascript
describe('Insufficient Funds', () => {
  it('should fail when account has insufficient funds', async () => {
    try {
      await client.transactions.create({
        fromAccountId: 'test_acc_empty',
        toAccountId: 'test_acc_002',
        paymentTypeId: 'rtp',
        amount: 1000.00,
        description: 'Test payment'
      });
      
      fail('Expected error to be thrown');
    } catch (error) {
      expect(error.code).toBe('INSUFFICIENT_FUNDS');
      expect(error.status).toBe(422);
    }
  });
});
```

### Load Testing

Use tools like Artillery or k6 for load testing:

```javascript
// k6 load test script
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 10 }, // Ramp up
    { duration: '5m', target: 10 }, // Stay at 10 users
    { duration: '2m', target: 0 },  // Ramp down
  ],
};

export default function () {
  const payload = JSON.stringify({
    fromAccountId: 'test_acc_001',
    toAccountId: 'test_acc_002',
    paymentTypeId: 'rtp',
    amount: Math.random() * 1000,
    description: 'Load test payment'
  });
  
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${__ENV.API_TOKEN}`
    },
  };
  
  const response = http.post(
    'https://sandbox-api.payment-engine.com/api/v1/transactions',
    payload,
    params
  );
  
  check(response, {
    'status is 201': (r) => r.status === 201,
    'transaction created': (r) => r.json('id') !== null,
  });
}
```

## Best Practices

### 1. Security

- **Always use HTTPS** for API communications
- **Store API keys securely** (environment variables, key vaults)
- **Implement webhook signature verification**
- **Use least privilege principle** for API key permissions
- **Rotate API keys regularly**

### 2. Reliability

- **Implement retry logic** with exponential backoff
- **Use idempotency keys** for critical operations
- **Handle rate limiting gracefully**
- **Monitor API response times and error rates**
- **Implement circuit breaker patterns**

### 3. Performance

- **Cache frequently accessed data** (account balances, payment types)
- **Use connection pooling** for HTTP clients
- **Implement request batching** where possible
- **Monitor and optimize API usage patterns**

### 4. Monitoring

- **Log all API interactions** (requests, responses, errors)
- **Set up alerts** for high error rates or slow responses
- **Monitor webhook delivery success**
- **Track business metrics** (transaction success rates, volumes)

### 5. Error Handling

- **Implement comprehensive error handling**
- **Provide meaningful error messages** to users
- **Log errors with sufficient context**
- **Have fallback mechanisms** for critical operations

### 6. Testing

- **Write comprehensive unit tests**
- **Implement integration tests** with test environment
- **Perform load testing** before production deployment
- **Test error scenarios** and edge cases

## Examples

### E-commerce Integration

```javascript
class PaymentProcessor {
  constructor(paymentEngineClient) {
    this.client = paymentEngineClient;
  }
  
  async processOrderPayment(order) {
    try {
      // Check account balance first
      const balance = await this.client.accounts.getBalance(order.paymentAccount);
      
      if (balance.availableBalance < order.total) {
        throw new Error('Insufficient funds');
      }
      
      // Create payment transaction
      const transaction = await this.client.transactions.create({
        fromAccountId: order.paymentAccount,
        toAccountId: order.merchantAccount,
        paymentTypeId: order.paymentMethod,
        amount: order.total,
        currencyCode: order.currency,
        description: `Payment for order ${order.id}`,
        externalReference: order.id,
        metadata: {
          orderId: order.id,
          customerId: order.customerId,
          items: order.items.map(item => ({
            id: item.id,
            name: item.name,
            price: item.price,
            quantity: item.quantity
          }))
        }
      });
      
      // Update order status
      await this.updateOrderStatus(order.id, 'payment_processing', transaction.id);
      
      return {
        success: true,
        transactionId: transaction.id,
        status: transaction.status
      };
      
    } catch (error) {
      await this.updateOrderStatus(order.id, 'payment_failed', null, error.message);
      
      return {
        success: false,
        error: error.message
      };
    }
  }
  
  async handlePaymentWebhook(event) {
    switch (event.eventType) {
      case 'transaction.completed':
        await this.handlePaymentSuccess(event.data);
        break;
        
      case 'transaction.failed':
        await this.handlePaymentFailure(event.data);
        break;
    }
  }
  
  async handlePaymentSuccess(transactionData) {
    const orderId = transactionData.externalReference;
    
    // Update order status
    await this.updateOrderStatus(orderId, 'paid', transactionData.transactionId);
    
    // Send confirmation email
    await this.sendOrderConfirmation(orderId);
    
    // Update inventory
    await this.updateInventory(orderId);
    
    // Trigger fulfillment
    await this.triggerFulfillment(orderId);
  }
  
  async handlePaymentFailure(transactionData) {
    const orderId = transactionData.externalReference;
    
    // Update order status
    await this.updateOrderStatus(orderId, 'payment_failed', transactionData.transactionId, transactionData.failureReason);
    
    // Send failure notification
    await this.sendPaymentFailureNotification(orderId);
    
    // Release reserved inventory
    await this.releaseInventoryReservation(orderId);
  }
}
```

### Subscription Payment Integration

```javascript
class SubscriptionPaymentManager {
  constructor(paymentEngineClient) {
    this.client = paymentEngineClient;
  }
  
  async processRecurringPayment(subscription) {
    try {
      const transaction = await this.client.transactions.create({
        fromAccountId: subscription.paymentAccountId,
        toAccountId: subscription.merchantAccountId,
        paymentTypeId: 'ach_debit',
        amount: subscription.amount,
        currencyCode: subscription.currency,
        description: `Subscription payment for ${subscription.planName}`,
        externalReference: subscription.id,
        metadata: {
          subscriptionId: subscription.id,
          planId: subscription.planId,
          billingPeriod: subscription.currentPeriod,
          customerId: subscription.customerId
        }
      });
      
      // Update subscription payment status
      await this.updateSubscriptionPayment(subscription.id, {
        transactionId: transaction.id,
        status: 'processing',
        attemptedAt: new Date()
      });
      
      return transaction;
      
    } catch (error) {
      // Handle payment failure
      await this.handleSubscriptionPaymentFailure(subscription, error);
      throw error;
    }
  }
  
  async handleSubscriptionPaymentFailure(subscription, error) {
    const failureCount = subscription.consecutiveFailures + 1;
    
    // Update subscription with failure information
    await this.updateSubscriptionPayment(subscription.id, {
      status: 'failed',
      failureReason: error.message,
      consecutiveFailures: failureCount,
      lastFailureAt: new Date()
    });
    
    // Implement retry logic
    if (failureCount < 3) {
      // Schedule retry
      await this.schedulePaymentRetry(subscription.id, failureCount);
    } else {
      // Suspend subscription after 3 failures
      await this.suspendSubscription(subscription.id, 'payment_failure');
      
      // Send suspension notification
      await this.sendSubscriptionSuspensionNotification(subscription);
    }
  }
}
```

## Support

- **Developer Portal**: [https://developers.payment-engine.com](https://developers.payment-engine.com)
- **API Documentation**: [https://docs.payment-engine.com/api](https://docs.payment-engine.com/api)
- **SDK Documentation**: [https://docs.payment-engine.com/sdks](https://docs.payment-engine.com/sdks)
- **Support Email**: [integration-support@payment-engine.com](mailto:integration-support@payment-engine.com)
- **Community Forum**: [https://community.payment-engine.com](https://community.payment-engine.com)
- **Status Page**: [https://status.payment-engine.com](https://status.payment-engine.com)