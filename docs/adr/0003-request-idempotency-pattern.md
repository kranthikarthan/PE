# ADR 0003: Request Idempotency Pattern

## Status
Accepted

## Context
In a distributed payment processing system, network failures, client retries, and duplicate requests can lead to:
- Duplicate payment processing
- Double-charging customers
- Data inconsistencies
- Financial reconciliation issues

We need a mechanism to ensure that the same request is processed exactly once, even if submitted multiple times.

## Decision
We will implement the **Idempotency Key Pattern** using the `X-Idempotency-Key` HTTP header.

### Implementation:
1. **Idempotency Key**: Client provides unique key in `X-Idempotency-Key` header
2. **Key Storage**: Store processed keys in PostgreSQL with TTL (24 hours default)
3. **Response Caching**: Cache the full response for replay
4. **Hash-Based Detection**: Secondary duplicate detection via request body hash
5. **Tenant Isolation**: Keys are scoped per tenant

### Flow:
```
1. Client sends request with X-Idempotency-Key: "key-123"
2. Interceptor checks if key exists for tenant
3. If exists and not expired:
   → Return cached response (200 OK)
   → Add X-Idempotency-Replay: true header
4. If not exists:
   → Process request normally
   → Store key + response
   → Return response
```

## Consequences

### Positive:
- ✅ Prevents duplicate payment processing
- ✅ Safe retry mechanism for clients
- ✅ Automatic response replay for duplicates
- ✅ No business logic changes required
- ✅ Works with any HTTP method (POST, PUT, PATCH)
- ✅ Configurable TTL per use case
- ✅ Tenant-isolated for multi-tenancy

### Negative:
- ❌ Database storage overhead
- ❌ Small latency increase for key lookup
- ❌ Requires client to generate unique keys
- ❌ TTL management and cleanup needed

### Neutral:
- ⚠️ Clients must implement key generation (UUID recommended)
- ⚠️ Old keys must be purged periodically
- ⚠️ Not suitable for truly idempotent operations (GET)

## Alternatives Considered

### Redis-Based Storage
- ✅ Faster lookups
- ✅ Built-in TTL
- ❌ Less durable (memory-based)
- ❌ Additional infrastructure

Decision: Use PostgreSQL for durability; Redis can be added later for performance

### Hash-Only Approach
- ✅ No client changes needed
- ❌ Hash collisions possible
- ❌ Can't distinguish intentional duplicate from retry

Decision: Use as secondary check, not primary

### At-Most-Once Kafka
- ✅ Built into Kafka
- ❌ Only covers Kafka, not HTTP
- ❌ Doesn't prevent initial duplicate

Decision: Use Kafka idempotence separately

## Implementation Details

### Database Schema:
```sql
CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) UNIQUE,
    tenant_id VARCHAR(100),
    endpoint VARCHAR(500),
    request_hash VARCHAR(64),
    response_body JSONB,
    response_status INTEGER,
    processed_at TIMESTAMP,
    expires_at TIMESTAMP
);
```

### Interceptor:
- `IdempotencyInterceptor` handles pre-processing check
- Stores keys in `IdempotencyKeyRepository`
- 24-hour default TTL
- Configurable via `payment-processing.idempotency.ttl-hours`

### Headers:
- **Client sends**: `X-Idempotency-Key: <uuid>`
- **Server responds**: `X-Idempotency-Replay: true` (if cached)
- **Server responds**: `X-Original-Request-Time: <timestamp>` (if cached)

### Configuration:
```yaml
payment-processing:
  idempotency:
    enabled: true
    ttl-hours: 24
```

## Usage Example

### Client Request:
```bash
curl -X POST http://localhost:8082/payment-processing/api/v1/iso20022/... \
  -H "X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000" \
  -H "X-Tenant-ID: tenant-001" \
  -H "Authorization: Bearer <token>" \
  -d @payload.json
```

### First Request:
```
HTTP 200 OK
{"messageId": "MSG-001", "status": "SUCCESS", ...}
```

### Duplicate Request (same key):
```
HTTP 200 OK
X-Idempotency-Replay: true
X-Original-Request-Time: 2025-10-10T12:00:00
{"messageId": "MSG-001", "status": "SUCCESS", ...}
```

## Monitoring

### Metrics to Track:
- `idempotency.keys.created` - New keys stored
- `idempotency.keys.replayed` - Duplicate requests detected
- `idempotency.keys.expired` - Keys cleaned up
- `idempotency.storage.size` - Current key count

### Alerts:
- High replay rate (> 10%) - may indicate client issues
- Storage growth - may need cleanup tuning

## References
- [Stripe Idempotency](https://stripe.com/docs/api/idempotent_requests)
- [PayPal Idempotency Best Practices](https://developer.paypal.com/docs/api/reference/api-responses/#idempotency)
- [RFC 7231 - Idempotent Methods](https://tools.ietf.org/html/rfc7231#section-4.2.2)

## Date
2025-10-10

## Author
Payment Engine Team
