# Payment Engine API Documentation

## Overview

The Payment Engine provides a comprehensive set of REST APIs for managing payment transactions, accounts, and related banking operations. This document covers all available endpoints, request/response formats, authentication, and integration guidelines.

## Base URL

- **Production**: `https://api.payment-engine.com`
- **Staging**: `https://staging-api.payment-engine.com`
- **Development**: `http://localhost:8080`

## Authentication

The Payment Engine uses OAuth 2.0 with JWT tokens for authentication. All API requests (except authentication endpoints) require a valid Bearer token.

### Authentication Flow

1. **Login**: POST `/api/v1/auth/login`
2. **Refresh Token**: POST `/api/v1/auth/refresh`
3. **Logout**: POST `/api/v1/auth/logout`

### Example Authentication

```bash
# Login
curl -X POST https://api.payment-engine.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your-username",
    "password": "your-password"
  }'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600,
  "user": {
    "id": "user-id",
    "username": "your-username",
    "permissions": ["transaction:read", "transaction:create"]
  }
}

# Using the token
curl -X GET https://api.payment-engine.com/api/v1/transactions \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## API Endpoints

### Transactions

#### Create Transaction

Creates a new payment transaction.

**Endpoint**: `POST /api/v1/transactions`

**Required Permissions**: `transaction:create`

**Request Body**:
```json
{
  "externalReference": "EXT-REF-123",
  "fromAccountId": "550e8400-e29b-41d4-a716-446655440001",
  "toAccountId": "550e8400-e29b-41d4-a716-446655440002",
  "paymentTypeId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 1000.00,
  "currencyCode": "USD",
  "description": "Payment for services",
  "metadata": {
    "orderId": "ORDER-123",
    "customerId": "CUST-456"
  }
}
```

**Response**:
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440001",
  "transactionReference": "TXN-2024-001",
  "externalReference": "EXT-REF-123",
  "fromAccountId": "550e8400-e29b-41d4-a716-446655440001",
  "toAccountId": "550e8400-e29b-41d4-a716-446655440002",
  "paymentTypeId": "660e8400-e29b-41d4-a716-446655440001",
  "amount": 1000.00,
  "currencyCode": "USD",
  "feeAmount": 2.50,
  "status": "PENDING",
  "transactionType": "TRANSFER",
  "description": "Payment for services",
  "metadata": {
    "orderId": "ORDER-123",
    "customerId": "CUST-456"
  },
  "initiatedAt": "2024-01-15T10:30:00Z",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

#### Get Transaction

Retrieves a transaction by ID.

**Endpoint**: `GET /api/v1/transactions/{transactionId}`

**Required Permissions**: `transaction:read`

**Response**:
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440001",
  "transactionReference": "TXN-2024-001",
  "status": "COMPLETED",
  "amount": 1000.00,
  "completedAt": "2024-01-15T10:30:15Z"
}
```

#### Get Transaction Status

Retrieves the current status of a transaction.

**Endpoint**: `GET /api/v1/transactions/{transactionId}/status`

**Required Permissions**: `transaction:read`

**Response**:
```json
{
  "transactionId": "bb0e8400-e29b-41d4-a716-446655440001",
  "transactionReference": "TXN-2024-001",
  "status": "COMPLETED",
  "amount": 1000.00,
  "currencyCode": "USD",
  "lastUpdated": "2024-01-15T10:30:15Z"
}
```

#### Search Transactions

Searches transactions with various criteria.

**Endpoint**: `GET /api/v1/transactions/search`

**Required Permissions**: `transaction:read`

**Query Parameters**:
- `transactionReference` (optional): Transaction reference to search for
- `accountId` (optional): Account ID involved in transactions
- `status` (optional): Transaction status (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED)
- `paymentTypeId` (optional): Payment type ID
- `minAmount` (optional): Minimum transaction amount
- `maxAmount` (optional): Maximum transaction amount
- `startDate` (optional): Start date (ISO 8601 format)
- `endDate` (optional): End date (ISO 8601 format)
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size
- `sortBy` (optional, default: createdAt): Sort field
- `sortDirection` (optional, default: desc): Sort direction (asc/desc)

**Example Request**:
```bash
GET /api/v1/transactions/search?status=COMPLETED&startDate=2024-01-01T00:00:00Z&endDate=2024-01-31T23:59:59Z&page=0&size=10
```

**Response**:
```json
{
  "content": [
    {
      "id": "bb0e8400-e29b-41d4-a716-446655440001",
      "transactionReference": "TXN-2024-001",
      "status": "COMPLETED",
      "amount": 1000.00
    }
  ],
  "totalElements": 150,
  "totalPages": 15,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

#### Cancel Transaction

Cancels a pending or processing transaction.

**Endpoint**: `POST /api/v1/transactions/{transactionId}/cancel`

**Required Permissions**: `transaction:update`

**Request Body**:
```json
{
  "reason": "Customer requested cancellation"
}
```

**Response**:
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440001",
  "status": "CANCELLED",
  "metadata": {
    "cancellation_reason": "Customer requested cancellation",
    "cancelled_at": "2024-01-15T10:35:00Z"
  }
}
```

### Accounts

#### Get Account

Retrieves account information by ID.

**Endpoint**: `GET /api/v1/accounts/{accountId}`

**Required Permissions**: `account:read`

**Response**:
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440001",
  "accountNumber": "ACC001001",
  "customerId": "770e8400-e29b-41d4-a716-446655440001",
  "accountTypeId": "550e8400-e29b-41d4-a716-446655440001",
  "currencyCode": "USD",
  "balance": 15000.00,
  "availableBalance": 15000.00,
  "status": "ACTIVE",
  "openedDate": "2024-01-01",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

#### Get Account Balance

Retrieves the current balance of an account.

**Endpoint**: `GET /api/v1/accounts/{accountId}/balance`

**Required Permissions**: `account:read`

**Response**:
```json
{
  "accountId": "880e8400-e29b-41d4-a716-446655440001",
  "accountNumber": "ACC001001",
  "balance": 15000.00,
  "availableBalance": 15000.00,
  "currencyCode": "USD",
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

#### Get Account Transactions

Retrieves transactions for a specific account.

**Endpoint**: `GET /api/v1/accounts/{accountId}/transactions`

**Required Permissions**: `account:read`

**Query Parameters**:
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Response**:
```json
{
  "content": [
    {
      "id": "bb0e8400-e29b-41d4-a716-446655440001",
      "transactionReference": "TXN-2024-001",
      "amount": 1000.00,
      "status": "COMPLETED",
      "transactionType": "DEBIT",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

### Payment Types

#### Get Payment Types

Retrieves all available payment types.

**Endpoint**: `GET /api/v1/payment-types`

**Required Permissions**: `payment-type:read`

**Query Parameters**:
- `active` (optional): Filter by active status (true/false)

**Response**:
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "code": "ACH_CREDIT",
    "name": "ACH Credit Transfer",
    "description": "Automated Clearing House credit transfer",
    "isSynchronous": false,
    "maxAmount": 1000000.00,
    "minAmount": 0.01,
    "processingFee": 2.50,
    "isActive": true,
    "configuration": {
      "processing_time_hours": 24,
      "cutoff_time": "15:00"
    }
  }
]
```

#### Get Payment Type

Retrieves a specific payment type by ID.

**Endpoint**: `GET /api/v1/payment-types/{paymentTypeId}`

**Required Permissions**: `payment-type:read`

**Response**:
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "code": "ACH_CREDIT",
  "name": "ACH Credit Transfer",
  "description": "Automated Clearing House credit transfer",
  "isSynchronous": false,
  "maxAmount": 1000000.00,
  "minAmount": 0.01,
  "processingFee": 2.50,
  "isActive": true
}
```

## Error Handling

The API uses standard HTTP status codes and returns error details in JSON format.

### Error Response Format

```json
{
  "error": {
    "code": "INSUFFICIENT_FUNDS",
    "message": "Account does not have sufficient funds for this transaction",
    "details": {
      "accountId": "880e8400-e29b-41d4-a716-446655440001",
      "requestedAmount": 1000.00,
      "availableBalance": 500.00
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/v1/transactions"
  }
}
```

### Common Error Codes

| HTTP Status | Error Code | Description |
|-------------|------------|-------------|
| 400 | INVALID_REQUEST | Request validation failed |
| 401 | UNAUTHORIZED | Authentication required |
| 403 | FORBIDDEN | Insufficient permissions |
| 404 | NOT_FOUND | Resource not found |
| 409 | CONFLICT | Resource conflict |
| 422 | INSUFFICIENT_FUNDS | Account has insufficient funds |
| 422 | INVALID_ACCOUNT_STATUS | Account is not active |
| 422 | PAYMENT_TYPE_INACTIVE | Payment type is not active |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests |
| 500 | INTERNAL_ERROR | Internal server error |
| 503 | SERVICE_UNAVAILABLE | Service temporarily unavailable |

## Rate Limiting

The API implements rate limiting to ensure fair usage:

- **Default Limit**: 1000 requests per hour per API key
- **Burst Limit**: 100 requests per minute
- **Headers**: Rate limit information is included in response headers

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1642248000
```

## Webhooks

The Payment Engine can send webhook notifications for transaction events.

### Webhook Events

- `transaction.created`
- `transaction.updated`
- `transaction.completed`
- `transaction.failed`
- `account.balance.updated`

### Webhook Payload Example

```json
{
  "eventId": "evt_1234567890",
  "eventType": "transaction.completed",
  "timestamp": "2024-01-15T10:30:15Z",
  "data": {
    "transactionId": "bb0e8400-e29b-41d4-a716-446655440001",
    "transactionReference": "TXN-2024-001",
    "status": "COMPLETED",
    "amount": 1000.00,
    "currencyCode": "USD"
  }
}
```

### Webhook Security

Webhooks are signed using HMAC-SHA256. Verify the signature using the `X-Webhook-Signature` header.

```python
import hmac
import hashlib

def verify_webhook_signature(payload, signature, secret):
    expected_signature = hmac.new(
        secret.encode('utf-8'),
        payload.encode('utf-8'),
        hashlib.sha256
    ).hexdigest()
    
    return hmac.compare_digest(f"sha256={expected_signature}", signature)
```

## SDKs and Libraries

### JavaScript/Node.js

```bash
npm install @payment-engine/sdk
```

```javascript
const PaymentEngine = require('@payment-engine/sdk');

const client = new PaymentEngine({
  apiKey: 'your-api-key',
  baseUrl: 'https://api.payment-engine.com'
});

// Create a transaction
const transaction = await client.transactions.create({
  fromAccountId: 'account-1',
  toAccountId: 'account-2',
  paymentTypeId: 'payment-type-1',
  amount: 1000.00,
  description: 'Payment for services'
});
```

### Python

```bash
pip install payment-engine-sdk
```

```python
from payment_engine import PaymentEngineClient

client = PaymentEngineClient(
    api_key='your-api-key',
    base_url='https://api.payment-engine.com'
)

# Create a transaction
transaction = client.transactions.create(
    from_account_id='account-1',
    to_account_id='account-2',
    payment_type_id='payment-type-1',
    amount=1000.00,
    description='Payment for services'
)
```

### Java

```xml
<dependency>
    <groupId>com.paymentengine</groupId>
    <artifactId>payment-engine-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
import com.paymentengine.PaymentEngineClient;
import com.paymentengine.model.CreateTransactionRequest;

PaymentEngineClient client = new PaymentEngineClient.Builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.payment-engine.com")
    .build();

CreateTransactionRequest request = CreateTransactionRequest.builder()
    .fromAccountId("account-1")
    .toAccountId("account-2")
    .paymentTypeId("payment-type-1")
    .amount(new BigDecimal("1000.00"))
    .description("Payment for services")
    .build();

Transaction transaction = client.transactions().create(request);
```

## Testing

### Test Environment

- **Base URL**: `https://staging-api.payment-engine.com`
- **Test Credentials**: Available in developer portal
- **Test Data**: Pre-populated test accounts and payment types

### Sample Test Scenarios

1. **Successful Transaction**:
   - Use test accounts with sufficient balance
   - Verify transaction status progression
   - Check account balance updates

2. **Insufficient Funds**:
   - Attempt transaction with amount exceeding balance
   - Verify error response and error code

3. **Invalid Payment Type**:
   - Use inactive payment type ID
   - Verify appropriate error response

## Support

- **Documentation**: [https://docs.payment-engine.com](https://docs.payment-engine.com)
- **Developer Portal**: [https://developers.payment-engine.com](https://developers.payment-engine.com)
- **Support Email**: [api-support@payment-engine.com](mailto:api-support@payment-engine.com)
- **Status Page**: [https://status.payment-engine.com](https://status.payment-engine.com)