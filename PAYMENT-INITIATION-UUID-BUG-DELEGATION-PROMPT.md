# Payment Initiation API UUID Bug Fix - Delegation Prompt

## Context
The Payment Engine load testing is failing due to a UUID parsing error in the payment-initiation-service. The error occurs when the API receives a payment initiation request with a reference field.

## Error Details
- **Error Message**: `Invalid UUID string: TEST-REF-001`
- **Location**: PaymentInitiationController
- **Request**: Payment initiation with reference field
- **Status**: 400 Bad Request

## Current State
1. ✅ Database schema fixed (added missing columns)
2. ✅ Gatling load test framework implemented
3. ✅ Request format aligned with PaymentInitiationRequest contract
4. ❌ **BLOCKER**: API incorrectly parsing reference field as UUID

## Problem Analysis
The issue appears to be in the domain model deserialization:
- `PaymentReference` class is `@Embeddable` with `@Value` (immutable)
- Hibernate/JPA may be having trouble deserializing immutable value objects
- The error suggests something is trying to parse the reference string as a UUID

## Files to Investigate
1. `domain-models/payment-initiation/src/main/java/com/payments/domain/payment/PaymentReference.java`
2. `domain-models/payment-initiation/src/main/java/com/payments/domain/payment/Payment.java` (line 64-65)
3. `payment-initiation-service/src/main/java/com/payments/paymentinitiation/entity/PaymentEntity.java` (line 72)
4. `payment-initiation-service/src/main/java/com/payments/paymentinitiation/mapper/PaymentMapper.java` (line 66)

## Potential Solutions
1. **Fix PaymentReference JPA annotations** - Make it mutable for Hibernate
2. **Add custom AttributeConverter** for PaymentReference
3. **Check for custom deserializers** that might be parsing as UUID
4. **Verify JSON deserialization** in PaymentInitiationRequest

## Success Criteria
- Load test passes with 200/201 responses
- No UUID parsing errors in logs
- Payment initiation API accepts reference strings properly

## Test Command
```bash
mvn -f load-tests/pom.xml gatling:test
```

## Next Steps
1. Identify the exact source of UUID parsing
2. Fix the deserialization issue
3. Verify load test passes
4. Update TODO: `debug-gatling-request` → `completed`

## Current TODO Status
- `load-testing`: in_progress
- `debug-gatling-request`: in_progress (BLOCKER)
- `fix-db-schema`: completed
