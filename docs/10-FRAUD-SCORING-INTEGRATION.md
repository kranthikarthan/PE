# Fraud Scoring API Integration

## Overview

The Payments Engine integrates with an **external fraud scoring API** to provide real-time fraud risk assessment for all payment transactions. This document describes the integration architecture, API specifications, and implementation details.

---

## Architecture

### Integration Point

```
┌─────────────────────────────────────────────────────────────┐
│  Payment Initiation                                          │
│  (User submits payment)                                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Validation Service                                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Step 1: Check Customer Limits                        │  │
│  │  Step 2: Call Fraud Scoring API ⭐                    │  │
│  │  Step 3: Evaluate Fraud Score                         │  │
│  │  Step 4: Check Compliance (KYC, FICA)                 │  │
│  └───────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
        ▼                         ▼
┌──────────────────┐    ┌──────────────────────┐
│  Fraud Scoring   │    │  Fallback:           │
│  API (External)  │    │  Rule-Based          │
│  - Real-time     │    │  Detection           │
│  - ML-based      │    │  (if API fails)      │
│  - Score 0.0-1.0 │    │                      │
└──────────────────┘    └──────────────────────┘
```

---

## Fraud Scoring API Specification

### Provider Options

| Provider | Type | Features | Typical Cost |
|----------|------|----------|--------------|
| **Simility (PayPal)** | SaaS | ML-based, device fingerprinting | $0.02-0.05/call |
| **Feedzai** | SaaS/On-Prem | Real-time, AI-powered | $0.03-0.08/call |
| **SAS Fraud Management** | Enterprise | Advanced analytics, rules+ML | Enterprise pricing |
| **DataVisor** | SaaS | Unsupervised ML, collective intelligence | $0.02-0.06/call |
| **Custom In-House** | Self-hosted | Full control, lower variable cost | Infrastructure cost |

**Assumption**: Using third-party SaaS provider for this design.

---

## API Contract

### Base URL
```
Production: https://fraud-api.provider.com/api/v1
Sandbox: https://sandbox.fraud-api.provider.com/api/v1
```

### Authentication

**Method 1: API Key**
```http
POST /api/v1/score
Authorization: Bearer {api_key}
X-Client-ID: payments-engine-prod
```

**Method 2: OAuth 2.0**
```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id={client_id}
&client_secret={client_secret}
```

---

### Endpoint: Score Transaction

```http
POST /api/v1/score
Content-Type: application/json
Authorization: Bearer {api_key}
X-Request-ID: {correlation_id}
X-Idempotency-Key: {payment_id}
```

#### Request Payload

```json
{
  "transactionId": "PAY-2025-XXXXXX",
  "timestamp": "2025-10-11T10:30:00Z",
  "customer": {
    "customerId": "CUST-123456",
    "customerSince": "2023-01-15",
    "kycStatus": "VERIFIED",
    "ficaStatus": "COMPLIANT",
    "riskProfile": "STANDARD"
  },
  "sourceAccount": {
    "accountNumber": "1234567890",
    "accountType": "CURRENT",
    "accountAge_days": 365,
    "accountStatus": "ACTIVE"
  },
  "destinationAccount": {
    "accountNumber": "0987654321",
    "bankCode": "ABSA",
    "accountHolderName": "Jane Smith"
  },
  "transaction": {
    "amount": 10000.00,
    "currency": "ZAR",
    "paymentType": "RTC",
    "reference": "Payment for invoice #12345",
    "description": "Business payment"
  },
  "context": {
    "channel": "WEB",
    "deviceFingerprint": "fp_device_hash_xxxxx",
    "ipAddress": "192.168.1.100",
    "geolocation": {
      "country": "ZA",
      "region": "Gauteng",
      "city": "Johannesburg",
      "coordinates": {
        "latitude": -26.2041,
        "longitude": 28.0473
      }
    },
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "sessionId": "session-uuid-xxxxx",
    "sessionAge_minutes": 15
  },
  "historicalData": {
    "transactionCount_24h": 3,
    "transactionCount_7days": 15,
    "transactionCount_30days": 45,
    "totalVolume_24h": 25000.00,
    "totalVolume_7days": 95000.00,
    "totalVolume_30days": 380000.00,
    "averageTransactionAmount": 8444.44,
    "lastTransactionTimestamp": "2025-10-10T14:22:00Z",
    "previousFraudIncidents": 0,
    "chargebackCount": 0
  }
}
```

#### Response Payload (Success - Low Risk)

```json
{
  "transactionId": "PAY-2025-XXXXXX",
  "fraudScore": 0.15,
  "riskLevel": "LOW",
  "recommendation": "APPROVE",
  "confidence": 0.92,
  "decision": "ALLOW",
  "fraudIndicators": [
    {
      "category": "VELOCITY",
      "indicator": "TRANSACTION_FREQUENCY",
      "score": 0.05,
      "weight": 0.20,
      "description": "Normal transaction frequency",
      "status": "PASS"
    },
    {
      "category": "AMOUNT",
      "indicator": "AMOUNT_DEVIATION",
      "score": 0.10,
      "weight": 0.25,
      "description": "Amount within expected range",
      "status": "PASS"
    },
    {
      "category": "GEOLOCATION",
      "indicator": "LOCATION_CONSISTENCY",
      "score": 0.00,
      "weight": 0.15,
      "description": "Transaction from usual location",
      "status": "PASS"
    },
    {
      "category": "DEVICE",
      "indicator": "DEVICE_FINGERPRINT",
      "score": 0.00,
      "weight": 0.10,
      "description": "Known device",
      "status": "PASS"
    },
    {
      "category": "PATTERN",
      "indicator": "BEHAVIORAL_ANALYSIS",
      "score": 0.00,
      "weight": 0.30,
      "description": "Consistent with customer behavior",
      "status": "PASS"
    }
  ],
  "reasons": [],
  "recommendedActions": [],
  "metadata": {
    "modelVersion": "v2.5.1",
    "processingTime_ms": 245,
    "rulesEvaluated": 25,
    "dataPointsAnalyzed": 150
  }
}
```

#### Response Payload (High Risk - Rejection)

```json
{
  "transactionId": "PAY-2025-XXXXXX",
  "fraudScore": 0.85,
  "riskLevel": "CRITICAL",
  "recommendation": "REJECT",
  "confidence": 0.88,
  "decision": "BLOCK",
  "fraudIndicators": [
    {
      "category": "AMOUNT",
      "indicator": "UNUSUAL_AMOUNT",
      "score": 0.45,
      "weight": 0.25,
      "description": "Amount significantly higher than average",
      "status": "FAIL",
      "details": {
        "requestedAmount": 100000.00,
        "averageAmount": 8000.00,
        "deviationMultiplier": 12.5
      }
    },
    {
      "category": "VELOCITY",
      "indicator": "SUSPICIOUS_VELOCITY",
      "score": 0.30,
      "weight": 0.20,
      "description": "Multiple high-value transactions in short period",
      "status": "FAIL",
      "details": {
        "transactionsLast10Min": 5,
        "threshold": 2
      }
    },
    {
      "category": "GEOLOCATION",
      "indicator": "GEOLOCATION_MISMATCH",
      "score": 0.10,
      "weight": 0.15,
      "description": "Transaction from unusual location",
      "status": "WARN",
      "details": {
        "currentCountry": "US",
        "usualCountry": "ZA",
        "impossibleTravel": false
      }
    }
  ],
  "reasons": [
    "Transaction amount is 12.5x higher than customer average",
    "5 transactions attempted in last 10 minutes",
    "Transaction from foreign IP address",
    "Device fingerprint not recognized"
  ],
  "recommendedActions": [
    "BLOCK_TRANSACTION",
    "NOTIFY_CUSTOMER",
    "FLAG_FOR_REVIEW",
    "TEMPORARY_ACCOUNT_LOCK"
  ],
  "metadata": {
    "modelVersion": "v2.5.1",
    "processingTime_ms": 312,
    "rulesEvaluated": 25,
    "dataPointsAnalyzed": 150
  }
}
```

#### Error Response

```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Missing required field: customerId",
    "field": "customer.customerId",
    "timestamp": "2025-10-11T10:30:00Z"
  }
}
```

---

## Risk Thresholds & Decision Logic

### Fraud Score Interpretation

| Score Range | Risk Level | Decision | Action |
|-------------|------------|----------|--------|
| 0.0 - 0.3 | LOW | Auto-approve | Process normally |
| 0.3 - 0.6 | MEDIUM | Auto-approve | Add monitoring flag |
| 0.6 - 0.8 | HIGH | Require verification | SMS OTP / 2FA |
| 0.8 - 1.0 | CRITICAL | Auto-reject | Block + notify customer |

### Decision Flow

```java
public ValidationDecision evaluateFraudScore(FraudScoreResponse fraudScore) {
    
    double score = fraudScore.getFraudScore();
    
    if (score >= 0.8) {
        // CRITICAL - Auto-reject
        return ValidationDecision.reject(
            "FRAUD_RISK_CRITICAL",
            "Transaction rejected due to high fraud risk",
            fraudScore
        );
    } else if (score >= 0.6) {
        // HIGH - Require additional verification
        return ValidationDecision.requireVerification(
            "ADDITIONAL_VERIFICATION_REQUIRED",
            "Please verify this transaction via SMS OTP",
            fraudScore
        );
    } else if (score >= 0.3) {
        // MEDIUM - Approve with monitoring
        return ValidationDecision.approveWithMonitoring(
            "APPROVED_WITH_MONITORING",
            fraudScore
        );
    } else {
        // LOW - Auto-approve
        return ValidationDecision.approve(fraudScore);
    }
}
```

---

## Integration Implementation

### Configuration

```yaml
# application.yml
fraud:
  api:
    enabled: true
    provider: SIMILITY  # or FEEDZAI, SAS, CUSTOM
    base-url: https://fraud-api.provider.com/api/v1
    auth-type: API_KEY  # or OAUTH2
    api-key: ${FRAUD_API_KEY}  # From Azure Key Vault
    timeout-ms: 5000
    
    # Circuit Breaker
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      wait-duration-in-open-state: 60s
      sliding-window-size: 10
    
    # Retry
    retry:
      max-attempts: 3
      wait-duration: 1s
      exponential-backoff-multiplier: 2
    
    # Fallback Strategy
    fallback:
      strategy: FAIL_OPEN  # or FAIL_CLOSE, RULE_BASED
      rule-based-enabled: true
    
    # Monitoring
    monitoring:
      alert-on-high-failure-rate: true
      alert-threshold-percentage: 10
```

### REST Client Configuration

```java
@Configuration
public class FraudApiClientConfig {
    
    @Value("${fraud.api.base-url}")
    private String fraudApiBaseUrl;
    
    @Value("${fraud.api.timeout-ms}")
    private int timeoutMs;
    
    @Bean
    public RestTemplate fraudApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configure timeout
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        
        // Connection pooling
        PoolingHttpClientConnectionManager connectionManager = 
            new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);
        
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build();
        factory.setHttpClient(httpClient);
        
        restTemplate.setRequestFactory(factory);
        
        // Add interceptors
        restTemplate.getInterceptors().add(new FraudApiLoggingInterceptor());
        restTemplate.getInterceptors().add(new FraudApiMetricsInterceptor());
        
        return restTemplate;
    }
}
```

### Fraud Scoring Service

```java
@Service
@Slf4j
public class FraudScoringService {
    
    @Autowired
    private RestTemplate fraudApiRestTemplate;
    
    @Autowired
    private FraudDetectionLogRepository fraudLogRepository;
    
    @Autowired
    private CustomerProfileService customerProfileService;
    
    @Value("${fraud.api.base-url}")
    private String fraudApiBaseUrl;
    
    @Value("${fraud.api.api-key}")
    private String fraudApiKey;
    
    @CircuitBreaker(name = "fraudApi", fallbackMethod = "fraudApiFallback")
    @Retry(name = "fraudApi")
    @Bulkhead(name = "fraudApi", type = Bulkhead.Type.SEMAPHORE)
    @Timed(value = "fraud.api.call", extraTags = {"operation", "score"})
    public FraudScoreResponse scoreFraudRisk(PaymentValidationRequest request) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Build fraud scoring request
            FraudScoringRequest fraudRequest = buildFraudRequest(request);
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(fraudApiKey);
            headers.set("X-Request-ID", request.getCorrelationId());
            headers.set("X-Idempotency-Key", request.getPaymentId());
            headers.set("X-Client-ID", "payments-engine-prod");
            
            HttpEntity<FraudScoringRequest> httpEntity = 
                new HttpEntity<>(fraudRequest, headers);
            
            // Call fraud API
            log.info("Calling fraud API for payment {}", request.getPaymentId());
            
            ResponseEntity<FraudScoreResponse> response = fraudApiRestTemplate.postForEntity(
                fraudApiBaseUrl + "/score",
                httpEntity,
                FraudScoreResponse.class
            );
            
            FraudScoreResponse fraudScore = response.getBody();
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log fraud check
            logFraudCheck(request.getPaymentId(), fraudRequest, fraudScore, responseTime, false);
            
            // Record metrics
            recordMetrics(true, responseTime);
            
            log.info("Fraud score for payment {}: {} ({})", 
                request.getPaymentId(), 
                fraudScore.getFraudScore(), 
                fraudScore.getRiskLevel());
            
            return fraudScore;
            
        } catch (HttpClientErrorException e) {
            // 4xx errors - bad request
            log.error("Fraud API returned error for payment {}: {}", 
                request.getPaymentId(), e.getMessage());
            recordMetrics(false, System.currentTimeMillis() - startTime);
            throw new FraudApiException("Invalid request to fraud API", e);
            
        } catch (HttpServerErrorException e) {
            // 5xx errors - server error
            log.error("Fraud API server error for payment {}", request.getPaymentId(), e);
            recordMetrics(false, System.currentTimeMillis() - startTime);
            throw new FraudApiException("Fraud API server error", e);
            
        } catch (ResourceAccessException e) {
            // Timeout or connection error
            log.error("Fraud API timeout/connection error for payment {}", 
                request.getPaymentId(), e);
            recordMetrics(false, System.currentTimeMillis() - startTime);
            throw new FraudApiException("Fraud API unavailable", e);
        }
    }
    
    /**
     * Fallback method when fraud API is unavailable
     */
    private FraudScoreResponse fraudApiFallback(
        PaymentValidationRequest request, 
        Exception e
    ) {
        log.warn("Fraud API unavailable for payment {}, using fallback strategy", 
            request.getPaymentId());
        
        recordMetrics(false, 0);
        
        // Get fallback strategy from configuration
        String fallbackStrategy = fraudFallbackStrategy; // FAIL_OPEN, FAIL_CLOSE, RULE_BASED
        
        switch (fallbackStrategy) {
            case "FAIL_OPEN":
                // Allow transaction but flag for review
                FraudScoreResponse failOpen = FraudScoreResponse.builder()
                    .transactionId(request.getPaymentId())
                    .fraudScore(0.5)
                    .riskLevel("MEDIUM")
                    .recommendation("APPROVE_WITH_MONITORING")
                    .confidence(0.0)
                    .reasons(List.of("Fraud API unavailable - fail-open strategy"))
                    .fallbackUsed(true)
                    .build();
                
                logFraudCheck(request.getPaymentId(), null, failOpen, 0, true);
                return failOpen;
                
            case "FAIL_CLOSE":
                // Reject transaction
                throw new FraudApiUnavailableException(
                    "Fraud API unavailable and fail-close strategy enabled");
                
            case "RULE_BASED":
                // Use rule-based fraud detection
                return ruleBasedFraudDetection(request);
                
            default:
                throw new IllegalStateException("Unknown fallback strategy: " + fallbackStrategy);
        }
    }
    
    /**
     * Rule-based fraud detection (fallback)
     */
    private FraudScoreResponse ruleBasedFraudDetection(PaymentValidationRequest request) {
        log.info("Using rule-based fraud detection for payment {}", request.getPaymentId());
        
        double totalScore = 0.0;
        List<String> reasons = new ArrayList<>();
        List<FraudIndicator> indicators = new ArrayList<>();
        
        // Rule 1: Velocity check
        int txCountLastHour = getTransactionCountLastHour(request.getCustomerId());
        if (txCountLastHour > 10) {
            double velocityScore = Math.min(0.3, txCountLastHour / 20.0);
            totalScore += velocityScore;
            reasons.add(String.format("High velocity: %d transactions in last hour", txCountLastHour));
            indicators.add(new FraudIndicator("VELOCITY", "HIGH_FREQUENCY", velocityScore));
        }
        
        // Rule 2: Unusual amount
        double avgAmount = getAverageTransactionAmount(request.getCustomerId());
        if (request.getAmount().doubleValue() > avgAmount * 5) {
            double amountScore = 0.25;
            totalScore += amountScore;
            reasons.add(String.format("Unusual amount: %.2fx higher than average", 
                request.getAmount().doubleValue() / avgAmount));
            indicators.add(new FraudIndicator("AMOUNT", "UNUSUAL_AMOUNT", amountScore));
        }
        
        // Rule 3: Foreign IP address
        if (!isFromSouthAfrica(request.getIpAddress())) {
            totalScore += 0.20;
            reasons.add("Transaction from foreign IP address");
            indicators.add(new FraudIndicator("GEOLOCATION", "FOREIGN_IP", 0.20));
        }
        
        // Rule 4: After hours transaction
        if (isAfterHours()) {
            totalScore += 0.10;
            reasons.add("Transaction during unusual hours");
            indicators.add(new FraudIndicator("PATTERN", "UNUSUAL_TIME", 0.10));
        }
        
        // Cap score at 1.0
        totalScore = Math.min(totalScore, 1.0);
        
        String riskLevel = determineRiskLevel(totalScore);
        String recommendation = totalScore < 0.7 ? "APPROVE" : "REJECT";
        
        FraudScoreResponse response = FraudScoreResponse.builder()
            .transactionId(request.getPaymentId())
            .fraudScore(totalScore)
            .riskLevel(riskLevel)
            .recommendation(recommendation)
            .confidence(0.6) // Lower confidence for rule-based
            .fraudIndicators(indicators)
            .reasons(reasons)
            .fallbackUsed(true)
            .metadata(Map.of("method", "RULE_BASED"))
            .build();
        
        logFraudCheck(request.getPaymentId(), null, response, 0, true);
        
        return response;
    }
    
    private FraudScoringRequest buildFraudRequest(PaymentValidationRequest request) {
        return FraudScoringRequest.builder()
            .transactionId(request.getPaymentId())
            .timestamp(Instant.now())
            .customer(buildCustomerInfo(request.getCustomerId()))
            .sourceAccount(buildSourceAccountInfo(request))
            .destinationAccount(buildDestinationAccountInfo(request))
            .transaction(buildTransactionInfo(request))
            .context(buildContextInfo(request))
            .historicalData(buildHistoricalData(request.getCustomerId()))
            .build();
    }
    
    private void logFraudCheck(
        String paymentId, 
        FraudScoringRequest request,
        FraudScoreResponse response, 
        long responseTime,
        boolean fallbackUsed
    ) {
        FraudDetectionLog log = FraudDetectionLog.builder()
            .paymentId(paymentId)
            .customerId(request != null ? request.getCustomer().getCustomerId() : null)
            .fraudScore(response.getFraudScore())
            .riskLevel(response.getRiskLevel())
            .recommendation(response.getRecommendation())
            .confidence(response.getConfidence())
            .fraudIndicators(objectMapper.valueToTree(response.getFraudIndicators()))
            .fraudReasons(objectMapper.valueToTree(response.getReasons()))
            .modelVersion(response.getMetadata().get("modelVersion"))
            .apiResponseTimeMs(responseTime)
            .fallbackUsed(fallbackUsed)
            .apiRequestData(request != null ? objectMapper.valueToTree(request) : null)
            .apiResponseData(objectMapper.valueToTree(response))
            .build();
        
        fraudLogRepository.save(log);
    }
}
```

---

## Fraud Indicators

### Standard Indicators Evaluated

| Category | Indicator | Weight | Description |
|----------|-----------|--------|-------------|
| **VELOCITY** | Transaction frequency | 20% | Number of transactions in time window |
| **VELOCITY** | Amount velocity | 20% | Total amount in time window |
| **AMOUNT** | Amount deviation | 25% | Deviation from customer's average |
| **AMOUNT** | Round amount | 5% | Transaction amount is round number |
| **GEOLOCATION** | Location consistency | 15% | Transaction from usual location |
| **GEOLOCATION** | Impossible travel | 15% | Geographic impossibility |
| **DEVICE** | Device fingerprint | 10% | Device recognition |
| **DEVICE** | New device | 10% | First time device |
| **PATTERN** | Time of day | 10% | Unusual transaction time |
| **PATTERN** | Behavioral anomaly | 30% | Deviation from normal behavior |
| **RECIPIENT** | Known beneficiary | 10% | Previous transactions to recipient |
| **RECIPIENT** | High-risk recipient | 20% | Recipient flagged as risky |

---

## Monitoring & Observability

### Key Metrics

```java
// Metrics to track
@Timed("fraud.api.call.duration")
@Counted("fraud.api.call.count")
public FraudScoreResponse scoreFraudRisk(PaymentValidationRequest request) {
    // ... implementation
}

// Custom metrics
meterRegistry.gauge("fraud.api.circuit.breaker.state", circuitBreakerState);
meterRegistry.counter("fraud.api.fallback.count").increment();
meterRegistry.timer("fraud.api.response.time").record(duration);
```

### Monitoring Dashboard

**Fraud API Health Dashboard**:
1. **Availability**: % uptime (target: 99.9%)
2. **Response Time**: p50, p95, p99 (target: p95 < 500ms)
3. **Success Rate**: % successful calls (target: > 99%)
4. **Fallback Rate**: % calls using fallback (target: < 1%)
5. **Circuit Breaker State**: CLOSED, OPEN, HALF_OPEN
6. **Score Distribution**: Histogram of fraud scores
7. **Rejection Rate**: % transactions rejected due to fraud

### Alerts

| Alert | Condition | Severity | Action |
|-------|-----------|----------|--------|
| Fraud API Down | > 5 consecutive failures | CRITICAL | Switch to fallback |
| High Response Time | p95 > 2 seconds | HIGH | Investigate performance |
| High Rejection Rate | > 10% rejected | MEDIUM | Review thresholds |
| Fallback Active | Using fallback > 5 min | HIGH | Check API health |
| Circuit Breaker Open | CB state = OPEN | CRITICAL | Investigate root cause |

---

## Cost Management

### Pricing Model

**Per-Transaction Pricing**: $0.01 - $0.05 per API call

**Monthly Cost Estimate**:
- **Volume**: 50 million transactions/month
- **Cost per call**: $0.02
- **Total**: $1,000,000/month

**Optimization Strategies**:
1. **Tiered Pricing**: Negotiate volume discounts
2. **Selective Scoring**: Only score transactions > threshold amount
3. **Caching**: Cache scores for duplicate transactions (1-minute TTL)
4. **Sampling**: Score 100% for high-value, 10% for low-value
5. **Batching**: Batch API calls where possible

### Cost-Optimized Configuration

```yaml
fraud:
  api:
    # Score only transactions above threshold
    minimum-amount-to-score: 1000.00  # Don't score < R1,000
    
    # Sampling for low-value transactions
    sampling:
      enabled: true
      low-value-threshold: 5000.00
      low-value-sample-rate: 0.10  # Score 10% of low-value txns
    
    # Caching
    caching:
      enabled: true
      ttl-seconds: 60
      cache-size: 10000
```

---

## Testing

### Unit Tests

```java
@Test
public void shouldScoreFraudRisk() {
    // Mock fraud API response
    FraudScoreResponse mockResponse = FraudScoreResponse.builder()
        .fraudScore(0.15)
        .riskLevel("LOW")
        .recommendation("APPROVE")
        .build();
    
    when(fraudApiRestTemplate.postForEntity(anyString(), any(), eq(FraudScoreResponse.class)))
        .thenReturn(ResponseEntity.ok(mockResponse));
    
    // Test
    FraudScoreResponse result = fraudScoringService.scoreFraudRisk(request);
    
    assertThat(result.getFraudScore()).isEqualTo(0.15);
    assertThat(result.getRiskLevel()).isEqualTo("LOW");
}

@Test
public void shouldUseFallbackWhenApiUnavailable() {
    // Mock API failure
    when(fraudApiRestTemplate.postForEntity(anyString(), any(), eq(FraudScoreResponse.class)))
        .thenThrow(new ResourceAccessException("Connection timeout"));
    
    // Test fallback
    FraudScoreResponse result = fraudScoringService.scoreFraudRisk(request);
    
    assertThat(result.isFallbackUsed()).isTrue();
    assertThat(result.getRiskLevel()).isEqualTo("MEDIUM");
}
```

### Integration Tests

Use WireMock to mock fraud API:

```java
@SpringBootTest
@AutoConfigureWireMock(port = 9091)
class FraudScoringIntegrationTest {
    
    @Test
    void shouldIntegrateWithFraudApi() {
        // Setup WireMock stub
        stubFor(post(urlEqualTo("/api/v1/score"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "transactionId": "PAY-2025-XXXXXX",
                        "fraudScore": 0.15,
                        "riskLevel": "LOW",
                        "recommendation": "APPROVE"
                    }
                    """)));
        
        // Test
        FraudScoreResponse response = fraudScoringService.scoreFraudRisk(request);
        
        assertThat(response.getFraudScore()).isEqualTo(0.15);
        
        // Verify WireMock received request
        verify(postRequestedFor(urlEqualTo("/api/v1/score"))
            .withHeader("Authorization", containing("Bearer")));
    }
}
```

---

## Security Considerations

### API Key Management

- ✅ Store API key in **Azure Key Vault**
- ✅ Rotate keys every 90 days
- ✅ Use separate keys for dev/staging/production
- ✅ Never log API keys

### Data Privacy

- ✅ **Mask sensitive data** in logs
- ✅ Send only necessary data to fraud API
- ✅ Comply with POPIA (data protection)
- ✅ Document data shared with third party

### Network Security

- ✅ Use HTTPS/TLS 1.3 for all calls
- ✅ Whitelist fraud API IP addresses
- ✅ Use VNet service endpoints (if possible)
- ✅ Certificate pinning for additional security

---

## Disaster Recovery

### Fraud API Unavailable

**Scenario**: Fraud API is completely down

**Response**:
1. Circuit breaker opens after 5 failures
2. Activate fallback strategy:
   - **FAIL_OPEN**: Allow transactions with MEDIUM risk flag
   - **RULE_BASED**: Use internal rule-based detection
3. Alert operations team
4. Monitor fallback fraud rate
5. Restore to fraud API when available

### SLA Management

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Availability | 99.9% | < 99% |
| Response Time | p95 < 500ms | p95 > 2s |
| Error Rate | < 0.1% | > 1% |
| Fallback Rate | < 1% | > 5% |

---

## Configuration Examples

### Environment Variables

```bash
# Fraud API Configuration
FRAUD_API_ENABLED=true
FRAUD_API_PROVIDER=SIMILITY
FRAUD_API_BASE_URL=https://fraud-api.simility.com/api/v1
FRAUD_API_KEY_VAULT_REF=/keyvault/fraud-api-key
FRAUD_API_TIMEOUT_MS=5000

# Fallback Configuration
FRAUD_FALLBACK_STRATEGY=FAIL_OPEN
FRAUD_RULE_BASED_ENABLED=true

# Circuit Breaker
FRAUD_CB_FAILURE_THRESHOLD=50
FRAUD_CB_WAIT_DURATION_SECONDS=60

# Cost Optimization
FRAUD_MIN_AMOUNT_TO_SCORE=1000.00
FRAUD_SAMPLING_ENABLED=false
```

### Kubernetes Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: fraud-api-secrets
  namespace: payments
type: Opaque
data:
  api-key: <base64-encoded-api-key>
  client-id: <base64-encoded-client-id>
```

---

## Related Documents

- **[01-ASSUMPTIONS.md](01-ASSUMPTIONS.md)** - Section 4.4 (Fraud Prevention Assumptions)
- **[02-MICROSERVICES-BREAKDOWN.md](02-MICROSERVICES-BREAKDOWN.md)** - Section 2 (Validation Service)
- **[05-DATABASE-SCHEMAS.md](05-DATABASE-SCHEMAS.md)** - Section 2 (Fraud Detection Tables)

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
