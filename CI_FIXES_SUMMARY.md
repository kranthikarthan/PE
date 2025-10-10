# CI Failure Fixes - Summary

## Overview
Fixed 4 critical bugs identified in the PR review that were causing CI failures and would prevent the application from starting properly.

## Issues Fixed

### 1. ✅ Kafka Replication Factor Configuration (P1 - High Priority)
**Problem:** Hard-coded `REPLICATION_FACTOR = 3` caused Kafka topic creation to fail with single-broker dev environment.

**Root Cause:** `docker-compose.dev.yml` runs only 1 Kafka broker, but topic config required 3 replicas → "Replication factor 3 larger than available brokers 1" error.

**Solution:**
- Made Kafka topic configuration environment-aware and configurable via application properties
- Added `@Value` annotations in `KafkaTopicConfig.java`:
  - `spring.kafka.topic.partitions` (default: 3)
  - `spring.kafka.topic.replication-factor` (default: 1)
  - `spring.kafka.topic.min-insync-replicas` (default: 1)
- Updated all 10 topic definitions to use configurable values
- Set dev profile: `replication-factor: 1, min-insync-replicas: 1`
- Set prod profile: `replication-factor: 3, min-insync-replicas: 2`

**Files Changed:**
- `services/payment-processing/src/main/java/com/paymentengine/paymentprocessing/config/KafkaTopicConfig.java`
- `services/payment-processing/src/main/resources/application-dev.yml`
- `services/payment-processing/src/main/resources/application-prod.yml`

---

### 2. ✅ Missing Tenant Table in Database Migration (High Severity)
**Problem:** Foreign key constraint `fk_tenant` in `transaction_repair` table referenced non-existent `tenants` table, causing Flyway migration to fail.

**Root Cause:** Migration script assumed tenants table existed but never created it.

**Solution:**
- Added `tenants` table creation at the beginning of `V1__initial_schema.sql`:
  - Columns: `id` (PK), `name`, `status`, `configuration` (JSONB), timestamps
  - Status constraint: `ACTIVE`, `SUSPENDED`, `INACTIVE`
  - Indexed on status for performance
- Pre-populated with default tenant: `('default', 'Default Tenant', 'ACTIVE')`
- Removed redundant commented-out tenant insert

**Files Changed:**
- `services/payment-processing/src/main/resources/db/migration/V1__initial_schema.sql`

---

### 3. ✅ Idempotency Interceptor Implementation (High Severity)
**Problem:** 
1. `storeProcessedRequest()` method was never automatically called → idempotency keys not stored
2. Unhandled `JsonProcessingException` when serializing request body could cause silent failures

**Root Cause:** Interceptor only had `preHandle()` but no `afterCompletion()` to capture and store responses.

**Solution:**
- Implemented `afterCompletion()` method to:
  - Capture successful responses (2xx status codes)
  - Extract request body from `ContentCachingRequestWrapper`
  - Automatically call `storeProcessedRequest()` after successful processing
  - Clean up MDC context
- Added proper exception handling for `JsonProcessingException` with fallback to `toString()`
- Added `Collections` import for empty maps
- Made error handling more resilient (log but don't throw)

**Files Changed:**
- `services/payment-processing/src/main/java/com/paymentengine/paymentprocessing/interceptor/IdempotencyInterceptor.java`

**Behavior:**
- Requests with `X-Idempotency-Key` header now properly store responses
- Duplicate requests return cached responses with `X-Idempotency-Replay: true` header
- Only POST, PUT, PATCH methods with 2xx status codes are stored

---

### 4. ✅ JWT Role Claims Not Mapped to Authorities (P1 - High Priority)
**Problem:** `@PreAuthorize("hasRole(...)")` guards in controllers (e.g., `TenantManagementController`, `IncomingMessageController`) always denied access because role claims were extracted but never converted to Spring authorities.

**Root Cause:** `extractAuthorities()` method extracted `roles` claim but only returned scope-based authorities.

**Solution:**
- Updated `extractAuthorities()` in `SecurityConfig.java` to:
  1. Extract and map `scope` claims → `SCOPE_*` authorities (for `hasAuthority()`)
  2. Extract and map `roles` claims → `ROLE_*` authorities (for `hasRole()`)
  3. Combine both into single authority collection
- Now supports both scope-based and role-based authorization

**Files Changed:**
- `services/payment-processing/src/main/java/com/paymentengine/paymentprocessing/security/SecurityConfig.java`

**Example:**
```json
JWT Claims:
{
  "scope": ["iso20022:send", "iso20022:validate"],
  "roles": ["ADMIN", "OPERATOR"]
}

Authorities Created:
- SCOPE_iso20022:send
- SCOPE_iso20022:validate
- ROLE_ADMIN
- ROLE_OPERATOR
```

---

## Verification Steps

### Build & Compile
```bash
./mvnw clean compile
```

### Start Dev Environment (Single Broker Kafka)
```bash
make up
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run Database Migrations
```bash
make db-migrate
```

### Test JWT Authorization
```bash
# Should now work with proper role claims in JWT
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8082/payment-processing/api/v1/tenants
```

---

## Impact Assessment

| Issue | Severity | Impact | Status |
|-------|----------|--------|--------|
| Kafka Replication Factor | **P1** | Application fails to start in dev | ✅ **FIXED** |
| Missing Tenant Table | **High** | Database migration fails | ✅ **FIXED** |
| Idempotency Interceptor | **High** | Duplicate request protection not working | ✅ **FIXED** |
| JWT Role Claims | **P1** | All role-based auth fails (403 Forbidden) | ✅ **FIXED** |

---

## Configuration Reference

### Development (Single Broker)
```yaml
spring:
  kafka:
    topic:
      partitions: 3
      replication-factor: 1
      min-insync-replicas: 1
```

### Production (Multi-Broker Cluster)
```yaml
spring:
  kafka:
    topic:
      partitions: ${KAFKA_TOPIC_PARTITIONS:3}
      replication-factor: ${KAFKA_TOPIC_REPLICATION_FACTOR:3}
      min-insync-replicas: ${KAFKA_TOPIC_MIN_INSYNC_REPLICAS:2}
```

---

## Breaking Changes
**None.** All changes are backward compatible and improve reliability.

---

## Next Steps
1. ✅ All critical bugs fixed
2. Run full CI pipeline to verify fixes
3. Test end-to-end flow with dev environment
4. Deploy to staging for integration testing

---

## Related ADRs
- ADR-0002: Kafka for Event-Driven Architecture
- ADR-0003: Request Idempotency Pattern

---

**Generated:** 2025-10-10  
**Author:** Cursor Agent  
**PR:** Bootstrap and develop spring boot application
