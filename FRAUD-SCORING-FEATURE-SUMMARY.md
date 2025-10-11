# Fraud Scoring API Integration - Feature Summary

## 📋 Overview

This document summarizes the **External Fraud Scoring API Integration** feature added to the Payments Engine. This provides real-time, ML-based fraud risk assessment for all payment transactions.

---

## ✨ What Was Added

### 1. External Fraud Scoring API Integration

**Purpose**: Real-time fraud risk assessment using third-party AI/ML-based fraud detection service

**Providers Supported**:
- Simility (PayPal)
- Feedzai
- SAS Fraud Management
- DataVisor
- Custom in-house solutions

**Integration Type**: Synchronous REST API

---

## 🎯 Key Features

### ✅ Real-Time Fraud Scoring
- Fraud score for every transaction (0.0 - 1.0 scale)
- Response time: < 500ms (p95)
- Comprehensive risk indicators
- ML-based anomaly detection

### ✅ Risk-Based Decision Logic

| Score Range | Risk Level | Decision | Action |
|-------------|------------|----------|--------|
| 0.0 - 0.3 | LOW | Auto-approve | Process normally |
| 0.3 - 0.6 | MEDIUM | Auto-approve | Monitor closely |
| 0.6 - 0.8 | HIGH | Verify | Require 2FA/OTP |
| 0.8 - 1.0 | CRITICAL | Auto-reject | Block + notify |

### ✅ Resilience Patterns
- **Circuit Breaker**: Prevent cascading failures
- **Retry Logic**: 3 attempts with exponential backoff
- **Fallback Strategies**:
  - **Fail-Open**: Allow with monitoring (default)
  - **Fail-Close**: Reject all transactions
  - **Rule-Based**: Use internal fraud rules

### ✅ Comprehensive Input Data
- Customer profile and history
- Transaction details
- Device fingerprint
- IP address and geolocation
- Session information
- Behavioral patterns

### ✅ Detailed Fraud Indicators
- Velocity checks (transaction frequency)
- Amount anomalies
- Geolocation analysis
- Device recognition
- Behavioral patterns
- Recipient risk assessment

---

## 📝 Documentation Updates

### Updated Documents

#### 1. **01-ASSUMPTIONS.md**
- ✅ Added Section 4.4: Fraud Prevention (expanded)
- ✅ Defined fraud API provider options
- ✅ Specified fraud score range and risk thresholds
- ✅ Added authentication methods
- ✅ Defined fallback strategies
- ✅ Listed input data requirements
- ✅ Added cost assumptions ($0.01-0.05 per call)

#### 2. **02-MICROSERVICES-BREAKDOWN.md**
- ✅ Updated Validation Service responsibilities
- ✅ Added "External Integration: Fraud Scoring API" section
- ✅ Added complete API request/response examples
- ✅ Added high-risk response example
- ✅ Added Java integration code (300+ lines):
  - FraudScoringService class
  - Circuit breaker implementation
  - Fallback methods
  - Rule-based fraud detection
  - REST client configuration
- ✅ Added fraud score evaluation logic
- ✅ Updated technology stack

#### 3. **05-DATABASE-SCHEMAS.md**
- ✅ Enhanced `fraud_detection_log` table with:
  - risk_level, recommendation, confidence
  - fraud_indicators, fraud_reasons (JSONB)
  - model_version, api_response_time_ms
  - fallback_used flag
  - api_request_data, api_response_data (full audit)
- ✅ Added `fraud_api_metrics` table (performance monitoring)
- ✅ Added `fraud_rules` table (fallback rule-based detection)
- ✅ Added sample fraud rules

#### 4. **10-FRAUD-SCORING-INTEGRATION.md** (NEW)
- ✅ Complete integration guide
- ✅ Provider comparison table
- ✅ API contract specifications
- ✅ Request/response payloads with examples
- ✅ Risk thresholds and decision logic
- ✅ Complete Java implementation code
- ✅ Circuit breaker configuration
- ✅ Fallback strategies (3 options)
- ✅ Rule-based fraud detection (backup)
- ✅ Fraud indicators catalog
- ✅ Monitoring & observability
- ✅ Cost management strategies
- ✅ Testing examples (unit + integration)
- ✅ Security considerations
- ✅ Disaster recovery procedures

#### 5. **README.md**
- ✅ Added reference to 10-FRAUD-SCORING-INTEGRATION.md

#### 6. **QUICK-REFERENCE.md**
- ✅ Added fraud scoring integration document reference

---

## 🔄 Fraud Check Flow

### Integration Flow

```
1. Payment Initiated
   ↓
2. Validation Service: Check Customer Limits
   ↓
3. Validation Service: CALL FRAUD SCORING API ⭐ NEW
   │
   ├─ Build fraud request with:
   │  - Customer profile
   │  - Transaction details
   │  - Device fingerprint
   │  - IP address & geolocation
   │  - Historical patterns
   │
   ├─ POST https://fraud-api.com/api/v1/score
   │  Authorization: Bearer {api_key}
   │  Timeout: 5 seconds
   │  Retry: 3 attempts
   │
   ├─ Receive fraud score response:
   │  {
   │    "fraudScore": 0.15,
   │    "riskLevel": "LOW",
   │    "recommendation": "APPROVE"
   │  }
   │
   └─ Evaluate Score ⭐ NEW
      │
      ├─ Score 0.8-1.0 (CRITICAL):
      │  └─ Auto-REJECT payment
      │     └─ Publish: ValidationFailedEvent (FRAUD_RISK_CRITICAL)
      │
      ├─ Score 0.6-0.8 (HIGH):
      │  └─ Require additional verification (2FA/OTP)
      │     └─ Hold payment for user verification
      │
      ├─ Score 0.3-0.6 (MEDIUM):
      │  └─ Auto-APPROVE with monitoring flag
      │     └─ Continue to next step
      │
      └─ Score 0.0-0.3 (LOW):
         └─ Auto-APPROVE
            └─ Continue to next step
   ↓
4. Validation Service: Check Compliance (KYC, FICA)
   ↓
5. If ALL PASS: Publish PaymentValidatedEvent
```

### Fallback Flow (API Unavailable)

```
Fraud API Call
   ↓
Circuit Breaker OPEN (after 5 failures) ⭐
   ↓
Activate Fallback Strategy:
   │
   ├─ FAIL_OPEN:
   │  └─ Assign score: 0.5 (MEDIUM)
   │     └─ Allow transaction with monitoring
   │
   ├─ FAIL_CLOSE:
   │  └─ Reject all transactions
   │     └─ Return error to user
   │
   └─ RULE_BASED:
      └─ Use internal fraud rules
         - Velocity check
         - Amount anomaly check
         - Geolocation check
         - Time-of-day check
         └─ Calculate score from rules
            └─ Continue validation
```

---

## 🗄️ Database Tables Added

### fraud_detection_log (Enhanced)
```sql
payment_id, customer_id
fraud_score (0.0-1.0)
risk_level (LOW, MEDIUM, HIGH, CRITICAL)
recommendation (APPROVE, REQUIRE_VERIFICATION, REJECT)
confidence (0.0-1.0)
fraud_indicators (JSONB)
fraud_reasons (JSONB)
model_version
api_response_time_ms
fallback_used (BOOLEAN)
api_request_data (JSONB)
api_response_data (JSONB)
detected_at
```

### fraud_api_metrics (NEW)
```sql
metric_timestamp
total_calls
successful_calls
failed_calls
timeout_calls
fallback_calls
avg_response_time_ms
p95_response_time_ms
p99_response_time_ms
circuit_breaker_status
error_rate
```

### fraud_rules (NEW)
```sql
rule_id, rule_name
rule_type (VELOCITY, AMOUNT, GEOLOCATION, DEVICE, PATTERN)
rule_condition (JSONB)
risk_score_contribution
priority, active
```

---

## 📡 API Integration Details

### Fraud API Request

```http
POST https://fraud-api.provider.com/api/v1/score
Content-Type: application/json
Authorization: Bearer {api_key}
X-Request-ID: {correlation_id}

{
  "transactionId": "PAY-2025-XXXXXX",
  "customer": { ... },
  "sourceAccount": { ... },
  "destinationAccount": { ... },
  "transaction": {
    "amount": 10000.00,
    "paymentType": "RTC"
  },
  "context": {
    "deviceFingerprint": "...",
    "ipAddress": "192.168.1.100",
    "geolocation": { ... }
  },
  "historicalData": { ... }
}
```

### Fraud API Response

```json
{
  "fraudScore": 0.15,
  "riskLevel": "LOW",
  "recommendation": "APPROVE",
  "confidence": 0.92,
  "fraudIndicators": [
    {
      "category": "VELOCITY",
      "score": 0.05,
      "description": "Normal transaction frequency"
    }
  ],
  "reasons": [],
  "modelVersion": "v2.5.1"
}
```

---

## 🛡️ Fraud Indicators Analyzed

### Input Data Categories

1. **Customer Profile**
   - Customer age (days since registration)
   - KYC/FICA status
   - Previous fraud incidents
   - Risk profile

2. **Transaction Details**
   - Amount and currency
   - Payment type
   - Reference and description

3. **Account Information**
   - Account age
   - Account type and status
   - Source and destination accounts

4. **Context Data**
   - Device fingerprint
   - IP address
   - Geolocation (country, city, coordinates)
   - User agent
   - Session information

5. **Historical Patterns**
   - Transaction count (24h, 7d, 30d)
   - Total volume (24h, 7d, 30d)
   - Average transaction amount
   - Last transaction timestamp
   - Chargeback history

### Fraud Indicators Evaluated

| Category | Indicator | Weight | Description |
|----------|-----------|--------|-------------|
| VELOCITY | Transaction frequency | 20% | Too many transactions in short time |
| VELOCITY | Amount velocity | 20% | High total amount in time window |
| AMOUNT | Amount deviation | 25% | Transaction amount unusual for customer |
| AMOUNT | Round amount | 5% | Suspiciously round numbers |
| GEOLOCATION | Location consistency | 15% | Transaction from unusual location |
| GEOLOCATION | Impossible travel | 15% | Physical impossibility |
| DEVICE | Device fingerprint | 10% | Unknown or suspicious device |
| DEVICE | New device | 10% | First-time device |
| PATTERN | Time anomaly | 10% | Transaction at unusual time |
| PATTERN | Behavioral deviation | 30% | Doesn't match customer pattern |
| RECIPIENT | Known beneficiary | 10% | First-time recipient |
| RECIPIENT | High-risk recipient | 20% | Recipient on watchlist |

---

## 🔧 Implementation Components

### Java Classes

1. **FraudScoringService.java**
   - Main service for fraud API integration
   - Circuit breaker implementation
   - Fallback logic
   - ~200 lines

2. **FraudScoringRequest.java**
   - Request DTO for fraud API
   - Builder pattern
   - ~100 lines

3. **FraudScoreResponse.java**
   - Response DTO from fraud API
   - Includes indicators and reasons
   - ~80 lines

4. **FraudApiClientConfig.java**
   - REST client configuration
   - Timeouts, connection pooling
   - ~50 lines

5. **FraudIndicator.java**
   - Model for fraud indicators
   - ~30 lines

### Configuration Files

```yaml
# application.yml
fraud:
  api:
    enabled: true
    provider: SIMILITY
    base-url: https://fraud-api.simility.com/api/v1
    api-key: ${FRAUD_API_KEY}
    timeout-ms: 5000
    
    circuit-breaker:
      failure-rate-threshold: 50
      wait-duration-in-open-state: 60s
    
    retry:
      max-attempts: 3
      wait-duration: 1s
    
    fallback:
      strategy: FAIL_OPEN
      rule-based-enabled: true
```

---

## 💰 Cost Analysis

### Pricing

**Per-Transaction Cost**: $0.01 - $0.05

**Monthly Cost Estimate**:
- Volume: 50 million transactions
- Cost per call: $0.02
- **Total: $1,000,000/month**

### Cost Optimization Strategies

1. **Selective Scoring** - Only score transactions above threshold
   ```yaml
   minimum-amount-to-score: 1000.00  # Don't score < R1,000
   # Savings: ~30% if 30% of transactions are < R1,000
   ```

2. **Sampling** - Score subset of low-value transactions
   ```yaml
   sampling:
     low-value-threshold: 5000.00
     sample-rate: 0.10  # Score 10% of low-value
   # Savings: ~20% if 40% are low-value
   ```

3. **Caching** - Cache scores for 1 minute
   ```yaml
   caching:
     ttl-seconds: 60
   # Savings: ~5% for duplicate attempts
   ```

4. **Volume Discounts** - Negotiate with provider
   ```
   Tier 1: 0-10M calls/month: $0.025/call
   Tier 2: 10-50M calls/month: $0.020/call
   Tier 3: 50M+ calls/month: $0.015/call
   # Savings: ~25% at high volume
   ```

**Optimized Cost**: ~$500,000/month (50% reduction)

---

## 🔄 Payment Flow Integration

### Updated Validation Flow

```
1. Payment Initiated
   ↓
2. Validation: Check Customer Limits
   - Daily, monthly, payment type limits
   - Reserve limit if sufficient
   ↓
3. Validation: CALL FRAUD API ⭐ NEW
   - Build comprehensive fraud request
   - Include customer profile, history, context
   - POST to fraud API
   - Response: fraudScore, riskLevel, indicators
   ↓
4. Validation: EVALUATE FRAUD SCORE ⭐ NEW
   │
   ├─ Score ≥ 0.8 (CRITICAL):
   │  └─ Auto-REJECT
   │     - Reason: FRAUD_RISK_CRITICAL
   │     - Release limit reservation
   │     - Log incident
   │     - Notify customer
   │
   ├─ Score 0.6-0.8 (HIGH):
   │  └─ Require Verification
   │     - Send OTP/2FA challenge
   │     - Hold payment
   │     - Wait for user confirmation
   │
   ├─ Score 0.3-0.6 (MEDIUM):
   │  └─ Approve with Monitoring
   │     - Flag for review
   │     - Continue processing
   │
   └─ Score 0.0-0.3 (LOW):
      └─ Auto-APPROVE
         - Continue processing
   ↓
5. Validation: Check Compliance (KYC, FICA)
   ↓
6. Publish: PaymentValidatedEvent (includes fraud score)
```

---

## 🗄️ Database Impact

### Tables Added: 2

1. **fraud_api_metrics** - Monitor API performance
2. **fraud_rules** - Fallback rule-based detection

### Tables Enhanced: 1

1. **fraud_detection_log** - Expanded with 10 new fields

### Storage Impact

**Fraud Detection Log**:
- Records per year: 50M transactions × 365 days = ~18 billion records
- Average size per record: ~2 KB (with JSONB)
- **Total size**: ~36 TB per year
- **Mitigation**: Archive records older than 90 days to cold storage

---

## 📊 Monitoring & Alerts

### Key Metrics

1. **Fraud API Availability**: Target 99.9%
2. **Fraud API Response Time**: p95 < 500ms
3. **Fraud API Success Rate**: > 99%
4. **Fallback Rate**: < 1%
5. **Circuit Breaker State**: Monitor open/close transitions
6. **Fraud Rejection Rate**: Track % rejected due to fraud
7. **False Positive Rate**: Monitor and tune thresholds

### Dashboards

```
Fraud Scoring Dashboard
├── API Health
│   ├── Availability: 99.95% ✅
│   ├── Avg Response Time: 245ms
│   ├── Success Rate: 99.8%
│   └── Circuit Breaker: CLOSED ✅
│
├── Fraud Statistics (Last 24h)
│   ├── Total Scored: 2.1M transactions
│   ├── LOW Risk: 1.9M (90%)
│   ├── MEDIUM Risk: 180K (8.5%)
│   ├── HIGH Risk: 20K (1%)
│   ├── CRITICAL Risk: 1K (0.05%)
│   └── Rejected: 1K (0.05%)
│
└── Cost Tracking
    ├── API Calls Today: 2.1M
    ├── Cost per Call: $0.02
    ├── Total Cost: $42,000
    └── Monthly Projection: $1.26M
```

---

## 🧪 Testing Strategy

### Unit Tests

```java
@Test
void shouldScoreLowRiskTransaction() {
    // Mock API response
    mockFraudApiResponse(0.15, "LOW", "APPROVE");
    
    FraudScoreResponse result = fraudService.scoreFraudRisk(request);
    
    assertThat(result.getFraudScore()).isEqualTo(0.15);
    assertThat(result.getRiskLevel()).isEqualTo("LOW");
}

@Test
void shouldRejectHighRiskTransaction() {
    mockFraudApiResponse(0.85, "CRITICAL", "REJECT");
    
    ValidationResult result = validationService.validatePayment(request);
    
    assertThat(result.isValid()).isFalse();
    assertThat(result.getFailureReason()).isEqualTo("FRAUD_RISK_CRITICAL");
}

@Test
void shouldUseFallbackWhenApiUnavailable() {
    // Simulate API failure
    when(restTemplate.postForEntity(...))
        .thenThrow(new ResourceAccessException("Timeout"));
    
    FraudScoreResponse result = fraudService.scoreFraudRisk(request);
    
    assertThat(result.isFallbackUsed()).isTrue();
    assertThat(result.getFraudScore()).isEqualTo(0.5);
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureWireMock(port = 9092)
class FraudApiIntegrationTest {
    
    @Test
    void shouldCallExternalFraudApi() {
        // Setup WireMock
        stubFor(post("/api/v1/score")
            .willReturn(okJson(fraudResponse)));
        
        // Test
        FraudScoreResponse result = fraudService.scoreFraudRisk(request);
        
        // Verify
        verify(postRequestedFor(urlEqualTo("/api/v1/score"))
            .withHeader("Authorization", containing("Bearer")));
    }
}
```

---

## 📈 Performance Impact

### Latency Added

| Step | Time | Cumulative |
|------|------|------------|
| Limit check | 50ms | 50ms |
| **Fraud API call** ⭐ | **250ms** | **300ms** |
| Compliance check | 50ms | 350ms |
| Total validation | - | ~350ms |

**Previous**: ~100ms  
**New**: ~350ms  
**Impact**: +250ms (+250%)

**Mitigation**:
- Parallel execution (call fraud API + account adapter simultaneously)
- Aggressive caching
- Async fraud scoring for low-value transactions (future enhancement)

---

## 🔐 Security & Privacy

### Data Shared with Fraud API

**Sent**:
- ✅ Customer ID (anonymized)
- ✅ Transaction amount and type
- ✅ Account numbers (last 4 digits only - configurable)
- ✅ Device fingerprint
- ✅ IP address and geolocation
- ✅ Aggregated historical data

**NOT Sent**:
- ❌ Account balances
- ❌ Full account numbers (optional masking)
- ❌ Customer PII (names, addresses, ID numbers)
- ❌ Payment descriptions (optional)
- ❌ Sensitive personal information

### API Key Management

- ✅ Store in Azure Key Vault
- ✅ Rotate every 90 days
- ✅ Different keys per environment
- ✅ Never log API keys
- ✅ Use HTTPS/TLS 1.3 only

---

## ✅ Benefits

### Risk Reduction
- ✅ Real-time fraud detection
- ✅ ML-based anomaly detection
- ✅ Reduce fraud losses by 80-90%
- ✅ Lower chargeback rates

### Customer Protection
- ✅ Protect customer accounts from fraud
- ✅ Block suspicious transactions automatically
- ✅ Notify customers of high-risk activities
- ✅ Build customer trust

### Operational Efficiency
- ✅ Automated fraud screening
- ✅ Reduce manual review workload
- ✅ Configurable risk thresholds
- ✅ Comprehensive audit trail

### Compliance
- ✅ Demonstrate fraud controls to regulators
- ✅ Support AML/CFT requirements
- ✅ Complete audit trail
- ✅ Meet SARB expectations

---

## 🎯 Configuration Options

### Risk Threshold Tuning

```yaml
fraud:
  risk-thresholds:
    low: 0.3
    medium: 0.6
    high: 0.8
  
  actions:
    critical-risk: REJECT
    high-risk: REQUIRE_VERIFICATION
    medium-risk: APPROVE_WITH_MONITORING
    low-risk: APPROVE
```

### Selective Scoring

```yaml
fraud:
  selective-scoring:
    enabled: true
    always-score-above-amount: 5000.00
    never-score-below-amount: 100.00
    sample-rate-between: 0.50  # Score 50% of mid-range
```

---

## 📚 Related Documents

- **[01-ASSUMPTIONS.md](docs/01-ASSUMPTIONS.md)** - Section 4.4 (Fraud Prevention)
- **[02-MICROSERVICES-BREAKDOWN.md](docs/02-MICROSERVICES-BREAKDOWN.md)** - Section 2 (Validation Service - Fraud Integration)
- **[05-DATABASE-SCHEMAS.md](docs/05-DATABASE-SCHEMAS.md)** - Section 2 (Fraud Tables)
- **[10-FRAUD-SCORING-INTEGRATION.md](docs/10-FRAUD-SCORING-INTEGRATION.md)** - Complete integration guide

---

## 📊 Summary Statistics

- **New API Endpoint**: 1 (external fraud scoring API)
- **Database Tables Enhanced**: 1
- **Database Tables Added**: 2
- **New Configuration Properties**: 15+
- **Java Classes Added**: 5
- **Lines of Integration Code**: ~400
- **Documents Updated**: 5
- **New Documents**: 1 (10-FRAUD-SCORING-INTEGRATION.md)
- **Fallback Strategies**: 3
- **Risk Levels**: 4
- **Fraud Indicators**: 12+

---

## 🎓 Implementation Checklist

### Phase 1: Setup
- [ ] Select fraud API provider
- [ ] Sign up for API access
- [ ] Store API key in Azure Key Vault
- [ ] Configure network connectivity

### Phase 2: Development
- [ ] Implement FraudScoringService
- [ ] Configure REST client
- [ ] Implement circuit breaker
- [ ] Add fallback logic
- [ ] Integrate with Validation Service

### Phase 3: Database
- [ ] Apply database migrations
- [ ] Create fraud_api_metrics table
- [ ] Create fraud_rules table
- [ ] Enhance fraud_detection_log table
- [ ] Add seed data for fallback rules

### Phase 4: Testing
- [ ] Unit tests with mocked API
- [ ] Integration tests with WireMock
- [ ] Load testing (performance impact)
- [ ] Failover testing (circuit breaker)
- [ ] End-to-end testing

### Phase 5: Monitoring
- [ ] Set up fraud API metrics
- [ ] Create monitoring dashboards
- [ ] Configure alerts
- [ ] Test fallback scenarios

### Phase 6: Production
- [ ] Deploy to production
- [ ] Monitor fraud detection rate
- [ ] Tune risk thresholds
- [ ] Optimize costs

---

**Feature Status**: ✅ Architecture Complete - Ready for Implementation

**Last Updated**: 2025-10-11  
**Version**: 1.0
