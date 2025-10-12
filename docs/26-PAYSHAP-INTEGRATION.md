# PayShap Integration - South African Instant Payments

## Overview

**PayShap** is South Africa's real-time, person-to-person (P2P) instant payment system launched in 2023. It enables instant transfers using just a mobile number or email address, with funds available immediately 24/7/365.

This document describes the complete integration architecture for PayShap within the Payments Engine.

---

## What is PayShap?

### Key Characteristics

```
PayShap:
├─ Operator: BankservAfrica (South African Payments Association)
├─ Launch: March 2023
├─ Type: Real-time instant payments (24/7/365)
├─ Identifier: Mobile number or email address
├─ Settlement: Immediate (real-time gross settlement)
├─ Standards: ISO 20022 messaging
├─ Limit: R 3,000 per transaction (as of 2023)
├─ Participants: All major South African banks
└─ Use Case: Person-to-person payments
```

### How PayShap Works

```
Sender                PayShap System              Recipient
  │                         │                        │
  │ 1. Initiate payment     │                        │
  │    (mobile/email)       │                        │
  ├────────────────────────>│                        │
  │                         │ 2. Lookup recipient    │
  │                         │    (Proxy Registry)    │
  │                         ├───────────────────────>│
  │                         │                        │
  │                         │ 3. Debit sender bank   │
  │                         │    Credit recipient    │
  │                         │    (RTGS settlement)   │
  │                         │                        │
  │ 4. Confirmation         │                        │
  │<────────────────────────┤                        │
  │                         │ 5. Notification        │
  │                         ├───────────────────────>│
  │                         │                        │
```

### PayShap vs Other Systems

| Feature | PayShap | EFT | RTC | SAMOS |
|---------|---------|-----|-----|-------|
| **Type** | Instant P2P | Batch ACH | Real-time | High-value RTGS |
| **Settlement** | Immediate | Next day | Real-time | Real-time |
| **Identifier** | Mobile/Email | Account # | Account # | Account # |
| **Limit** | R 3,000/txn | No limit | R 5M/txn | No limit |
| **Availability** | 24/7/365 | Business hours | 24/7/365 | Business hours |
| **Use Case** | P2P payments | Bill payments | Urgent payments | High-value |
| **Standard** | ISO 20022 | ISO 8583 | ISO 20022 | ISO 20022 |

---

## Architecture Integration

### PayShap Adapter Service

```
┌──────────────────────────────────────────────────────────────┐
│                    Payments Engine                            │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Payment Initiation Service                                  │
│         │                                                     │
│         ├──> Routing Service                                 │
│         │         │                                           │
│         │         ├──> PayShap Adapter Service               │
│         │         │         │                                 │
│         │         │         ├──> PayShap Proxy Lookup        │
│         │         │         │         │                       │
│         │         │         │         └──> BankservAfrica     │
│         │         │         │              Proxy Registry     │
│         │         │         │                                 │
│         │         │         ├──> ISO 20022 Message Builder   │
│         │         │         │    (pacs.008, pacs.002)         │
│         │         │         │                                 │
│         │         │         └──> PayShap Gateway              │
│         │         │                   │                       │
│         │         │                   └──> BankservAfrica     │
│         │         │                        PayShap System     │
│         │         │                                           │
│         │         └──> Settlement Service                     │
│         │                (real-time settlement)               │
│         │                                                     │
│         └──> Saga Orchestrator                                │
│                (orchestrate PayShap payment flow)             │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

#### 1. PayShap Adapter Service

**Purpose**: Gateway to PayShap system

**Responsibilities**:
- Proxy lookup (mobile/email → account mapping)
- ISO 20022 message construction (pacs.008, pacs.002, pacs.004)
- PayShap gateway communication (REST/SOAP)
- Real-time settlement coordination
- Response handling (success, failure, timeout)
- Idempotency (prevent duplicate payments)

**Technology Stack**:
- Spring Boot 3.x
- Spring WebFlux (reactive for real-time)
- ISO 20022 library (JAXB/Jackson)
- Resilience4j (circuit breaker, retry)
- PostgreSQL (adapter state)

---

## PayShap Message Formats (ISO 20022)

### 1. Customer Credit Transfer (pacs.008)

**Purpose**: Initiate payment via PayShap

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <!-- Group Header -->
    <GrpHdr>
      <MsgId>PAYSHAP-2025-101-000001</MsgId>
      <CreDtTm>2025-10-11T14:30:00Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <TtlIntrBkSttlmAmt Ccy="ZAR">1500.00</TtlIntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-11</IntrBkSttlmDt>
      <SttlmInf>
        <SttlmMtd>CLRG</SttlmMtd>
        <ClrSys>
          <Prtry>PAYSHAP</Prtry>
        </ClrSys>
      </SttlmInf>
    </GrpHdr>
    
    <!-- Credit Transfer Transaction Information -->
    <CdtTrfTxInf>
      <PmtId>
        <EndToEndId>E2E-PAY-67890</EndToEndId>
        <TxId>TXN-550e8400</TxId>
        <UETR>550e8400-e29b-41d4-a716-446655440000</UETR>
      </PmtId>
      
      <IntrBkSttlmAmt Ccy="ZAR">1500.00</IntrBkSttlmAmt>
      
      <!-- Debtor (Sender) -->
      <Dbtr>
        <Nm>John Doe</Nm>
        <Id>
          <OrgId>
            <Othr>
              <Id>19850101****001</Id> <!-- ID Number masked -->
              <SchmeNm>
                <Prtry>SAID</Prtry>
              </SchmeNm>
            </Othr>
          </OrgId>
        </Id>
      </Dbtr>
      
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>1234567890</Id>
            <SchmeNm>
              <Prtry>BBAN</Prtry>
            </SchmeNm>
          </Othr>
        </Id>
      </DbtrAcct>
      
      <DbtrAgt>
        <FinInstnId>
          <ClrSysMmbId>
            <MmbId>001234</MmbId> <!-- Bank code -->
          </ClrSysMmbId>
        </FinInstnId>
      </DbtrAgt>
      
      <!-- Creditor (Recipient) via PayShap Proxy -->
      <Cdtr>
        <Nm>Jane Smith</Nm>
        <Id>
          <OrgId>
            <Othr>
              <Id>+27821234567</Id> <!-- Mobile number -->
              <SchmeNm>
                <Prtry>MSISDN</Prtry>
              </SchmeNm>
            </Othr>
          </OrgId>
        </Id>
      </Cdtr>
      
      <CdtrAcct>
        <Id>
          <Othr>
            <Id>PROXY:+27821234567</Id> <!-- PayShap proxy -->
            <SchmeNm>
              <Prtry>PAYSHAP_PROXY</Prtry>
            </SchmeNm>
          </Othr>
        </Id>
      </CdtrAcct>
      
      <!-- Purpose -->
      <Purp>
        <Prtry>PAYMENT</Prtry>
      </Purp>
      
      <!-- Remittance Information -->
      <RmtInf>
        <Ustrd>Birthday gift</Ustrd>
      </RmtInf>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

### 2. Payment Status Report (pacs.002)

**Purpose**: Acknowledgment from PayShap system

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
  <FIToFIPmtStsRpt>
    <!-- Group Header -->
    <GrpHdr>
      <MsgId>PAYSHAP-ACK-2025-101-000001</MsgId>
      <CreDtTm>2025-10-11T14:30:05Z</CreDtTm>
    </GrpHdr>
    
    <!-- Original Group Information -->
    <OrgnlGrpInfAndSts>
      <OrgnlMsgId>PAYSHAP-2025-101-000001</OrgnlMsgId>
      <OrgnlMsgNmId>pacs.008.001.08</OrgnlMsgNmId>
      <GrpSts>ACCP</GrpSts> <!-- Accepted -->
    </OrgnlGrpInfAndSts>
    
    <!-- Transaction Information and Status -->
    <TxInfAndSts>
      <OrgnlEndToEndId>E2E-PAY-67890</OrgnlEndToEndId>
      <OrgnlTxId>TXN-550e8400</OrgnlTxId>
      <OrgnlUETR>550e8400-e29b-41d4-a716-446655440000</OrgnlUETR>
      
      <TxSts>ACSC</TxSts> <!-- AcceptedSettlementCompleted -->
      
      <StsRsnInf>
        <Rsn>
          <Cd>G000</Cd> <!-- Accepted -->
        </Rsn>
        <AddtlInf>Payment processed successfully</AddtlInf>
      </StsRsnInf>
      
      <AccptncDtTm>2025-10-11T14:30:05Z</AccptncDtTm>
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

### 3. Payment Return (pacs.004)

**Purpose**: Failed payment or reversal

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09">
  <PmtRtr>
    <GrpHdr>
      <MsgId>PAYSHAP-RTN-2025-101-000001</MsgId>
      <CreDtTm>2025-10-11T14:30:10Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
    </GrpHdr>
    
    <TxInf>
      <RtrId>RTN-550e8400</RtrId>
      <OrgnlTxRef>
        <OrgnlEndToEndId>E2E-PAY-67890</OrgnlEndToEndId>
        <OrgnlTxId>TXN-550e8400</OrgnlTxId>
      </OrgnlTxRef>
      
      <RtrdIntrBkSttlmAmt Ccy="ZAR">1500.00</RtrdIntrBkSttlmAmt>
      
      <RtrRsnInf>
        <Rsn>
          <Cd>AC04</Cd> <!-- Closed account -->
        </Rsn>
        <AddtlInf>Recipient account closed</AddtlInf>
      </RtrRsnInf>
    </TxInf>
  </PmtRtr>
</Document>
```

---

## PayShap Proxy Registry

### Proxy Lookup Service

**Purpose**: Resolve mobile/email to bank account

```java
@Service
@Slf4j
public class PayShapProxyService {
    
    private final RestTemplate payShapClient;
    private final ProxyCache proxyCache;
    
    /**
     * Lookup recipient account from mobile number or email.
     * Uses PayShap Proxy Registry.
     */
    @Cacheable(value = "payshap-proxy", unless = "#result == null")
    public ProxyLookupResult lookupProxy(String proxyId, ProxyType proxyType) {
        log.info("Looking up PayShap proxy: type={}, id={}", proxyType, maskProxy(proxyId));
        
        try {
            // Call PayShap Proxy Registry API
            ProxyLookupRequest request = ProxyLookupRequest.builder()
                .proxyId(proxyId)
                .proxyType(proxyType)
                .build();
            
            ProxyLookupResponse response = payShapClient.postForObject(
                "/proxy/lookup",
                request,
                ProxyLookupResponse.class
            );
            
            if (response != null && response.isFound()) {
                ProxyLookupResult result = ProxyLookupResult.builder()
                    .proxyId(proxyId)
                    .accountNumber(response.getAccountNumber())
                    .bankCode(response.getBankCode())
                    .accountHolderName(response.getAccountHolderName())
                    .status(ProxyStatus.ACTIVE)
                    .build();
                
                log.info("Proxy found: proxy={}, bank={}", 
                    maskProxy(proxyId), response.getBankCode());
                
                return result;
            } else {
                log.warn("Proxy not found: {}", maskProxy(proxyId));
                return null;
            }
            
        } catch (Exception e) {
            log.error("Proxy lookup failed: proxy={}, error={}", 
                maskProxy(proxyId), e.getMessage());
            throw new ProxyLookupException("Failed to lookup proxy", e);
        }
    }
    
    /**
     * Register customer's mobile/email with PayShap.
     */
    public void registerProxy(ProxyRegistrationRequest request) {
        // Call PayShap Proxy Registry API
        payShapClient.postForObject(
            "/proxy/register",
            request,
            ProxyRegistrationResponse.class
        );
    }
    
    private String maskProxy(String proxy) {
        if (proxy.startsWith("+27")) {
            return proxy.substring(0, 5) + "****" + proxy.substring(proxy.length() - 3);
        } else if (proxy.contains("@")) {
            String[] parts = proxy.split("@");
            return parts[0].substring(0, 2) + "****@" + parts[1];
        }
        return "****";
    }
}
```

### Proxy Types

```java
public enum ProxyType {
    MSISDN,    // Mobile number (+27821234567)
    EMAIL,     // Email address (user@example.com)
    ID_NUMBER  // SA ID number (future)
}
```

---

## Implementation

### PayShap Adapter Service

```java
@Service
@Slf4j
public class PayShapAdapterService {
    
    private final PayShapProxyService proxyService;
    private final ISO20022MessageBuilder messageBuilder;
    private final PayShapGatewayClient gatewayClient;
    private final PaymentRepository paymentRepository;
    
    /**
     * Process PayShap payment.
     */
    @Transactional
    public PayShapPaymentResult processPayment(PayShapPaymentRequest request) {
        log.info("Processing PayShap payment: paymentId={}", request.getPaymentId());
        
        // Step 1: Validate amount (R 3,000 limit)
        validateAmount(request.getAmount());
        
        // Step 2: Lookup recipient proxy (mobile/email → account)
        ProxyLookupResult proxy = proxyService.lookupProxy(
            request.getRecipientProxy(),
            request.getProxyType()
        );
        
        if (proxy == null) {
            throw new ProxyNotFoundException("Recipient not found on PayShap");
        }
        
        // Step 3: Build ISO 20022 message (pacs.008)
        String pacs008Message = messageBuilder.buildCreditTransfer(
            PayShapMessageRequest.builder()
                .paymentId(request.getPaymentId())
                .debtorAccount(request.getDebtorAccount())
                .debtorName(request.getDebtorName())
                .creditorAccount(proxy.getAccountNumber())
                .creditorName(proxy.getAccountHolderName())
                .creditorBankCode(proxy.getBankCode())
                .amount(request.getAmount())
                .currency("ZAR")
                .reference(request.getReference())
                .build()
        );
        
        // Step 4: Send to PayShap gateway
        PayShapGatewayResponse response = gatewayClient.sendPayment(
            pacs008Message,
            request.getPaymentId()
        );
        
        // Step 5: Process response
        if (response.isAccepted()) {
            log.info("PayShap payment accepted: paymentId={}, uetr={}", 
                request.getPaymentId(), response.getUetr());
            
            return PayShapPaymentResult.success(
                request.getPaymentId(),
                response.getUetr(),
                response.getSettlementTimestamp()
            );
        } else {
            log.warn("PayShap payment rejected: paymentId={}, reason={}", 
                request.getPaymentId(), response.getReasonCode());
            
            return PayShapPaymentResult.failure(
                request.getPaymentId(),
                response.getReasonCode(),
                response.getReasonDescription()
            );
        }
    }
    
    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("3000.00")) > 0) {
            throw new PayShapLimitExceededException(
                "PayShap limit is R 3,000 per transaction");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
    }
}
```

### PayShap Gateway Client

```java
@Component
@Slf4j
public class PayShapGatewayClient {
    
    private final WebClient webClient;
    
    @Autowired
    public PayShapGatewayClient(
        @Value("${payshap.gateway.url}") String gatewayUrl,
        @Value("${payshap.gateway.timeout}") int timeout
    ) {
        this.webClient = WebClient.builder()
            .baseUrl(gatewayUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/xml")
            .build();
    }
    
    /**
     * Send payment to PayShap gateway (non-blocking).
     */
    public PayShapGatewayResponse sendPayment(String iso20022Message, UUID paymentId) {
        try {
            return webClient.post()
                .uri("/payshap/payments")
                .header("X-Payment-ID", paymentId.toString())
                .header("X-Bank-Code", "001234") // Our bank code
                .bodyValue(iso20022Message)
                .retrieve()
                .bodyToMono(PayShapGatewayResponse.class)
                .timeout(Duration.ofSeconds(10))
                .block();
                
        } catch (WebClientException e) {
            log.error("PayShap gateway communication failed: paymentId={}", 
                paymentId, e);
            throw new PayShapGatewayException("Gateway communication failed", e);
        }
    }
}
```

---

## Database Schema

```sql
-- PayShap Adapter State
CREATE TABLE payshap_payments (
    payment_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    debtor_account VARCHAR(50) NOT NULL,
    debtor_name VARCHAR(200) NOT NULL,
    creditor_proxy VARCHAR(100) NOT NULL,  -- Mobile/email
    creditor_proxy_type VARCHAR(20) NOT NULL,  -- MSISDN, EMAIL
    creditor_account VARCHAR(50),  -- Resolved account
    creditor_bank_code VARCHAR(10),
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    reference VARCHAR(200),
    status VARCHAR(20) NOT NULL,  -- PENDING, ACCEPTED, SETTLED, FAILED
    uetr UUID,  -- Unique End-to-End Transaction Reference
    iso20022_message TEXT,  -- pacs.008 message sent
    response_message TEXT,  -- pacs.002 response received
    reason_code VARCHAR(10),
    reason_description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP,
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_uetr (uetr),
    INDEX idx_created_at (created_at)
);

-- Proxy Cache (for performance)
CREATE TABLE payshap_proxy_cache (
    proxy_id VARCHAR(100) PRIMARY KEY,
    proxy_type VARCHAR(20) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    bank_code VARCHAR(10) NOT NULL,
    account_holder_name VARCHAR(200),
    status VARCHAR(20) NOT NULL,  -- ACTIVE, INACTIVE
    cached_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    
    INDEX idx_account_number (account_number),
    INDEX idx_expires_at (expires_at)
);
```

---

## Configuration

```yaml
# application.yml
payshap:
  enabled: true
  
  gateway:
    url: https://payshap-gateway.bankservafrica.com
    timeout: 10000  # 10 seconds
    
  limits:
    per-transaction: 3000.00  # R 3,000
    daily-count: 100  # Max 100 PayShap payments per day per customer
    
  proxy:
    cache-ttl: 3600  # 1 hour
    lookup-timeout: 5000  # 5 seconds
    
  retry:
    max-attempts: 3
    backoff-delay: 1000  # 1 second
    
  participant:
    bank-code: "001234"  # Our bank code in PayShap
    bank-name: "Our Bank"
```

---

## Error Handling

### PayShap Reason Codes

| Code | Description | Action |
|------|-------------|--------|
| G000 | Accepted | Success |
| AC01 | Incorrect account number | Retry with correct proxy |
| AC04 | Closed account | Notify customer |
| AC06 | Blocked account | Notify customer |
| AM04 | Insufficient funds | Retry or notify customer |
| BE05 | Unrecognized creditor | Proxy not found |
| DUPL | Duplicate payment | Idempotency check |
| CUST | Requested by customer | Reversal |
| TECH | Technical error | Retry |

### Retry Strategy

```java
@Retryable(
    value = {PayShapGatewayException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public PayShapPaymentResult processPayment(PayShapPaymentRequest request) {
    // Implementation
}
```

---

## Monitoring & Alerts

### Metrics

```yaml
# Prometheus Metrics
payshap.payments.total:
  type: counter
  tags: [status, reason_code]
  
payshap.payments.duration:
  type: timer
  tags: [status]
  
payshap.proxy.lookup.total:
  type: counter
  tags: [found, proxy_type]
  
payshap.gateway.errors.total:
  type: counter
  tags: [error_type]
```

### Alerts

```yaml
# Prometheus Alerts
- alert: PayShapHighFailureRate
  expr: |
    (
      sum(rate(payshap_payments_total{status="failed"}[5m]))
      /
      sum(rate(payshap_payments_total[5m]))
    ) > 0.05
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: High PayShap failure rate (> 5%)
    
- alert: PayShapGatewayDown
  expr: up{job="payshap-gateway"} == 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: PayShap gateway unreachable
```

---

## Testing

### Unit Tests

```java
@Test
void processPayment_Success() {
    // Given
    PayShapPaymentRequest request = createTestRequest();
    when(proxyService.lookupProxy(any(), any()))
        .thenReturn(createProxyResult());
    when(gatewayClient.sendPayment(any(), any()))
        .thenReturn(createSuccessResponse());
    
    // When
    PayShapPaymentResult result = payShapAdapter.processPayment(request);
    
    // Then
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getUetr()).isNotNull();
}

@Test
void processPayment_ProxyNotFound() {
    // Given
    PayShapPaymentRequest request = createTestRequest();
    when(proxyService.lookupProxy(any(), any())).thenReturn(null);
    
    // When/Then
    assertThatThrownBy(() -> payShapAdapter.processPayment(request))
        .isInstanceOf(ProxyNotFoundException.class);
}

@Test
void processPayment_ExceedsLimit() {
    // Given
    PayShapPaymentRequest request = createTestRequest();
    request.setAmount(new BigDecimal("5000.00"));  // > R 3,000 limit
    
    // When/Then
    assertThatThrownBy(() -> payShapAdapter.processPayment(request))
        .isInstanceOf(PayShapLimitExceededException.class);
}
```

---

## Summary

### Key Features

✅ **Real-Time Payments**: Instant settlement 24/7/365  
✅ **Proxy-Based**: Use mobile/email instead of account number  
✅ **ISO 20022**: Standard messaging format  
✅ **R 3,000 Limit**: Per transaction limit  
✅ **Idempotency**: Prevent duplicate payments  
✅ **Circuit Breaker**: Resilient gateway communication  
✅ **Caching**: Proxy lookup cache for performance  

### Integration Points

1. **Proxy Registry**: Lookup mobile/email → account mapping
2. **Payment Gateway**: Send/receive ISO 20022 messages
3. **Settlement System**: Real-time gross settlement
4. **Notification**: Send confirmations to sender/recipient

### Compliance

- ✅ ISO 20022 messaging standards
- ✅ BankservAfrica PayShap rules
- ✅ POPIA (data privacy)
- ✅ FICA (customer identification)
- ✅ SARB regulations

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Internal  
**Related Documents**:
- `06-SOUTH-AFRICA-CLEARING.md` (Other SA clearing systems)
- `03-EVENT-SCHEMAS.md` (Event definitions)
