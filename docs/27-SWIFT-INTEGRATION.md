# SWIFT Integration - International Payments

## Overview

**SWIFT (Society for Worldwide Interbank Financial Telecommunication)** is the global standard for international cross-border payments. This document describes the complete integration architecture for SWIFT messaging within the Payments Engine.

---

## What is SWIFT?

### Key Characteristics

```
SWIFT:
├─ Network: 11,000+ financial institutions in 200+ countries
├─ Messages/Day: 44+ million (2023)
├─ Standards: ISO 15022 (legacy), ISO 20022 (modern)
├─ Message Types: MT (legacy), MX (XML, ISO 20022)
├─ Use Cases: International payments, trade finance, securities
├─ Settlement: Via correspondent banks (not instant)
├─ Security: High (encrypted, authenticated, audited)
└─ Compliance: AML, sanctions screening, KYC
```

### SWIFT Message Types

| MT Message | MX Message (ISO 20022) | Purpose |
|------------|------------------------|---------|
| MT103 | pacs.008 | Customer credit transfer |
| MT202 | pacs.009 | FI to FI payment |
| MT199 | camt.056 | Free format message |
| MT900 | camt.054 | Confirmation of debit |
| MT910 | camt.054 | Confirmation of credit |
| MT940 | camt.053 | Account statement |
| MT950 | camt.053 | Statement message |

---

## Architecture Integration

### SWIFT Adapter Service

```
┌──────────────────────────────────────────────────────────────────┐
│                    Payments Engine                                │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Payment Initiation Service                                      │
│         │                                                         │
│         ├──> Routing Service                                     │
│         │         │                                               │
│         │         ├──> SWIFT Adapter Service                     │
│         │         │         │                                     │
│         │         │         ├──> Sanctions Screening             │
│         │         │         │    (OFAC, UN, EU lists)            │
│         │         │         │                                     │
│         │         │         ├──> FX Rate Service                 │
│         │         │         │    (currency conversion)           │
│         │         │         │                                     │
│         │         │         ├──> Message Builder                 │
│         │         │         │    (MT103, pacs.008)               │
│         │         │         │                                     │
│         │         │         ├──> SWIFT Alliance Gateway          │
│         │         │         │         │                           │
│         │         │         │         └──> SWIFTNet              │
│         │         │         │              (SWIFT Network)       │
│         │         │         │                                     │
│         │         │         └──> Correspondent Bank Routing      │
│         │         │              (determine intermediary banks)  │
│         │         │                                               │
│         │         └──> Settlement Service                         │
│         │                (nostro/vostro reconciliation)          │
│         │                                                         │
│         └──> Saga Orchestrator                                    │
│                (orchestrate international payment flow)           │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

#### 1. SWIFT Adapter Service

**Purpose**: Gateway to SWIFT network

**Responsibilities**:
- Message construction (MT103, pacs.008)
- Sanctions screening (mandatory)
- Correspondent bank routing
- FX rate application
- SWIFT Alliance Gateway communication
- Message tracking and confirmations
- Compliance reporting (AML, CTR)

**Technology Stack**:
- Spring Boot 3.x
- SWIFT Alliance Gateway (SAG) SDK
- ISO 20022 library (JAXB)
- Sanctions screening engine
- PostgreSQL (message store)

---

## SWIFT Message Formats

### 1. MT103 (Customer Credit Transfer) - Legacy

**Purpose**: Send international payment

```
:20:2025101100001              <- Transaction reference
:23B:CRED                      <- Bank operation code
:32A:251011ZAR100000.00        <- Value date, currency, amount
:33B:ZAR100000.00              <- Original currency and amount
:50K:/1234567890               <- Ordering customer (account)
JOHN DOE
123 MAIN STREET
JOHANNESBURG
ZA
:52A:ABSAZAJJXXX               <- Ordering institution (bank BIC)
:56A:CHASUS33XXX               <- Intermediary bank (correspondent)
:57A:CITIUS33XXX               <- Account with institution
:59:/0987654321                <- Beneficiary (account)
JANE SMITH
456 PARK AVENUE
NEW YORK NY 10022
US
:70:INVOICE PAYMENT INV-12345  <- Remittance information
:71A:OUR                       <- Details of charges (OUR/BEN/SHA)
:72:/INS/CHASUS33               <- Sender to receiver information
//ACC/CITIUS33
```

### 2. pacs.008 (Customer Credit Transfer) - ISO 20022

**Purpose**: Modern XML-based international payment

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <!-- Group Header -->
    <GrpHdr>
      <MsgId>SWIFT-2025-101-000001</MsgId>
      <CreDtTm>2025-10-11T14:30:00Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <TtlIntrBkSttlmAmt Ccy="USD">10000.00</TtlIntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-11</IntrBkSttlmDt>
      <SttlmInf>
        <SttlmMtd>INDA</SttlmMtd>
        <InstgRmbrsmntAgt>
          <FinInstnId>
            <BICFI>CHASUS33XXX</BICFI>
          </FinInstnId>
        </InstgRmbrsmntAgt>
      </SttlmInf>
    </GrpHdr>
    
    <!-- Credit Transfer Transaction -->
    <CdtTrfTxInf>
      <PmtId>
        <InstrId>SWIFT-2025-101-000001</InstrId>
        <EndToEndId>E2E-PAY-67890</EndToEndId>
        <TxId>TXN-550e8400</TxId>
        <UETR>550e8400-e29b-41d4-a716-446655440000</UETR>
      </PmtId>
      
      <IntrBkSttlmAmt Ccy="USD">10000.00</IntrBkSttlmAmt>
      
      <ChrgBr>SHAR</ChrgBr> <!-- Shared charges -->
      
      <!-- Debtor (Sender) -->
      <Dbtr>
        <Nm>John Doe</Nm>
        <PstlAdr>
          <StrtNm>Main Street</StrtNm>
          <BldgNb>123</BldgNb>
          <TwnNm>Johannesburg</TwnNm>
          <Ctry>ZA</Ctry>
        </PstlAdr>
      </Dbtr>
      
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>1234567890</Id>
          </Othr>
        </Id>
        <Ccy>ZAR</Ccy>
      </DbtrAcct>
      
      <DbtrAgt>
        <FinInstnId>
          <BICFI>ABSAZAJJXXX</BICFI>
          <Nm>Absa Bank</Nm>
        </FinInstnId>
      </DbtrAgt>
      
      <!-- Intermediary Bank (Correspondent) -->
      <IntrmyAgt1>
        <FinInstnId>
          <BICFI>CHASUS33XXX</BICFI>
          <Nm>JP Morgan Chase</Nm>
        </FinInstnId>
      </IntrmyAgt1>
      
      <!-- Creditor Agent (Beneficiary Bank) -->
      <CdtrAgt>
        <FinInstnId>
          <BICFI>CITIUS33XXX</BICFI>
          <Nm>Citibank</Nm>
        </FinInstnId>
      </CdtrAgt>
      
      <!-- Creditor (Beneficiary) -->
      <Cdtr>
        <Nm>Jane Smith</Nm>
        <PstlAdr>
          <StrtNm>Park Avenue</StrtNm>
          <BldgNb>456</BldgNb>
          <TwnNm>New York</TwnNm>
          <PstCd>10022</PstCd>
          <Ctry>US</Ctry>
        </PstlAdr>
      </Cdtr>
      
      <CdtrAcct>
        <Id>
          <Othr>
            <Id>0987654321</Id>
          </Othr>
        </Id>
        <Ccy>USD</Ccy>
      </CdtrAcct>
      
      <!-- Purpose -->
      <Purp>
        <Cd>GDDS</Cd> <!-- Purchase of goods -->
      </Purp>
      
      <!-- Remittance Information -->
      <RmtInf>
        <Ustrd>Invoice payment INV-12345</Ustrd>
      </RmtInf>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

### 3. MT199 (Free Format Message)

**Purpose**: Request payment status or send free text

```
:20:STATUS-REQ-001
:21:2025101100001              <- Related reference
:79:PLEASE ADVISE STATUS OF
PAYMENT REFERENCE 2025101100001
SENT TO BENEFICIARY JANE SMITH
ACCOUNT 0987654321
```

---

## Implementation

### SWIFT Adapter Service

```java
@Service
@Slf4j
public class SwiftAdapterService {
    
    private final SanctionsScreeningService sanctionsService;
    private final FxRateService fxRateService;
    private final SwiftMessageBuilder messageBuilder;
    private final SwiftAllianceGateway swiftGateway;
    private final CorrespondentBankService correspondentService;
    
    /**
     * Process international SWIFT payment.
     */
    @Transactional
    public SwiftPaymentResult processPayment(SwiftPaymentRequest request) {
        log.info("Processing SWIFT payment: paymentId={}, currency={}, amount={}", 
            request.getPaymentId(), request.getCurrency(), request.getAmount());
        
        // Step 1: Sanctions screening (MANDATORY)
        SanctionsScreeningResult screening = sanctionsService.screen(
            SanctionsScreeningRequest.builder()
                .debtorName(request.getDebtorName())
                .creditorName(request.getCreditorName())
                .creditorCountry(request.getCreditorCountry())
                .build()
        );
        
        if (screening.isHit()) {
            log.warn("Sanctions hit detected: paymentId={}, reasons={}", 
                request.getPaymentId(), screening.getReasons());
            return SwiftPaymentResult.blocked(
                request.getPaymentId(),
                "SANCTIONS_HIT",
                screening.getReasons()
            );
        }
        
        // Step 2: FX rate (if currency conversion needed)
        FxRate fxRate = null;
        if (!request.getCurrency().equals("ZAR")) {
            fxRate = fxRateService.getRate("ZAR", request.getCurrency());
            log.info("FX rate: ZAR/{} = {}", request.getCurrency(), fxRate.getRate());
        }
        
        // Step 3: Determine correspondent bank routing
        CorrespondentBankRoute route = correspondentService.determineRoute(
            request.getDebtorBankBic(),
            request.getCreditorBankBic(),
            request.getCurrency()
        );
        
        log.info("Correspondent route: {} -> {} -> {}", 
            request.getDebtorBankBic(),
            route.getIntermediaryBankBic(),
            request.getCreditorBankBic()
        );
        
        // Step 4: Build SWIFT message (MT103 or pacs.008)
        String swiftMessage = messageBuilder.buildCustomerCreditTransfer(
            SwiftMessageRequest.builder()
                .paymentId(request.getPaymentId())
                .debtorAccount(request.getDebtorAccount())
                .debtorName(request.getDebtorName())
                .debtorAddress(request.getDebtorAddress())
                .debtorBankBic(request.getDebtorBankBic())
                .creditorAccount(request.getCreditorAccount())
                .creditorName(request.getCreditorName())
                .creditorAddress(request.getCreditorAddress())
                .creditorBankBic(request.getCreditorBankBic())
                .intermediaryBankBic(route.getIntermediaryBankBic())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .chargeBearer(ChargeBearer.SHARED)  // SHA
                .remittanceInfo(request.getRemittanceInfo())
                .fxRate(fxRate)
                .build()
        );
        
        // Step 5: Send to SWIFT network via Alliance Gateway
        SwiftGatewayResponse response = swiftGateway.sendMessage(
            swiftMessage,
            request.getPaymentId()
        );
        
        // Step 6: Process response
        if (response.isAccepted()) {
            log.info("SWIFT payment accepted: paymentId={}, uetr={}", 
                request.getPaymentId(), response.getUetr());
            
            return SwiftPaymentResult.success(
                request.getPaymentId(),
                response.getUetr(),
                response.getSwiftReference(),
                route.getEstimatedSettlementDays()
            );
        } else {
            log.warn("SWIFT payment rejected: paymentId={}, reason={}", 
                request.getPaymentId(), response.getReasonCode());
            
            return SwiftPaymentResult.failure(
                request.getPaymentId(),
                response.getReasonCode(),
                response.getReasonDescription()
            );
        }
    }
}
```

### Sanctions Screening Service

```java
@Service
@Slf4j
public class SanctionsScreeningService {
    
    private final SanctionsListRepository sanctionsRepo;
    
    /**
     * Screen against OFAC, UN, EU sanctions lists.
     * Uses fuzzy matching algorithm.
     */
    public SanctionsScreeningResult screen(SanctionsScreeningRequest request) {
        log.info("Screening: debtor={}, creditor={}, country={}", 
            request.getDebtorName(), 
            request.getCreditorName(),
            request.getCreditorCountry());
        
        List<SanctionsHit> hits = new ArrayList<>();
        
        // Screen creditor name
        List<SanctionsEntry> matches = sanctionsRepo.fuzzyMatch(
            request.getCreditorName(),
            MATCH_THRESHOLD
        );
        
        for (SanctionsEntry entry : matches) {
            hits.add(SanctionsHit.builder()
                .matchedName(entry.getName())
                .matchScore(entry.getMatchScore())
                .listName(entry.getListName())  // OFAC, UN, EU
                .reason(entry.getReason())
                .build());
        }
        
        // Screen creditor country (country sanctions)
        if (isCountrySanctioned(request.getCreditorCountry())) {
            hits.add(SanctionsHit.builder()
                .matchedName(request.getCreditorCountry())
                .listName("COUNTRY_SANCTIONS")
                .reason("Country under comprehensive sanctions")
                .build());
        }
        
        boolean isHit = !hits.isEmpty();
        
        if (isHit) {
            log.warn("Sanctions hit detected: hits={}", hits.size());
        }
        
        return SanctionsScreeningResult.builder()
            .isHit(isHit)
            .hits(hits)
            .screenedAt(Instant.now())
            .build();
    }
    
    private static final double MATCH_THRESHOLD = 0.85;  // 85% similarity
    
    private boolean isCountrySanctioned(String countryCode) {
        // Check comprehensive sanctions (e.g., Iran, North Korea, Syria)
        Set<String> sanctionedCountries = Set.of("IR", "KP", "SY", "CU");
        return sanctionedCountries.contains(countryCode);
    }
}
```

### SWIFT Alliance Gateway Client

```java
@Component
@Slf4j
public class SwiftAllianceGateway {
    
    private final SwiftMessageTransmitter transmitter;
    
    /**
     * Send message to SWIFT network via Alliance Gateway.
     */
    public SwiftGatewayResponse sendMessage(String message, UUID paymentId) {
        try {
            // Sign message (SWIFT LAU - Local Authentication)
            String signedMessage = signMessage(message);
            
            // Transmit via SWIFT Alliance Gateway
            SwiftTransmitResponse response = transmitter.transmit(
                TransmitRequest.builder()
                    .message(signedMessage)
                    .messageType(detectMessageType(message))
                    .priority(SwiftPriority.NORMAL)
                    .deliveryMode(DeliveryMode.STORE_AND_FORWARD)
                    .reference(paymentId.toString())
                    .build()
            );
            
            log.info("SWIFT message transmitted: paymentId={}, swiftRef={}", 
                paymentId, response.getSwiftReference());
            
            return SwiftGatewayResponse.builder()
                .accepted(true)
                .uetr(response.getUetr())
                .swiftReference(response.getSwiftReference())
                .build();
                
        } catch (SwiftException e) {
            log.error("SWIFT transmission failed: paymentId={}", paymentId, e);
            
            return SwiftGatewayResponse.builder()
                .accepted(false)
                .reasonCode(e.getErrorCode())
                .reasonDescription(e.getMessage())
                .build();
        }
    }
    
    private String signMessage(String message) {
        // SWIFT LAU (Local Authentication Unit) signing
        // Implementation depends on SWIFT Alliance Gateway SDK
        return message;  // Simplified
    }
    
    private String detectMessageType(String message) {
        if (message.contains("<FIToFICstmrCdtTrf>")) {
            return "pacs.008";
        } else if (message.contains(":20:")) {
            return "MT103";
        }
        return "UNKNOWN";
    }
}
```

---

## Database Schema

```sql
-- SWIFT Payments
CREATE TABLE swift_payments (
    payment_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    
    -- Debtor (Sender)
    debtor_account VARCHAR(50) NOT NULL,
    debtor_name VARCHAR(200) NOT NULL,
    debtor_address TEXT,
    debtor_bank_bic VARCHAR(11) NOT NULL,
    
    -- Creditor (Beneficiary)
    creditor_account VARCHAR(50) NOT NULL,
    creditor_name VARCHAR(200) NOT NULL,
    creditor_address TEXT,
    creditor_bank_bic VARCHAR(11) NOT NULL,
    creditor_country VARCHAR(2) NOT NULL,
    
    -- Payment details
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    charge_bearer VARCHAR(10) NOT NULL,  -- OUR, BEN, SHA
    remittance_info TEXT,
    
    -- Routing
    intermediary_bank_bic VARCHAR(11),
    correspondent_account VARCHAR(50),
    
    -- FX
    original_amount DECIMAL(15,2),
    original_currency VARCHAR(3),
    fx_rate DECIMAL(10,6),
    
    -- SWIFT details
    swift_reference VARCHAR(16),  -- SWIFT transaction reference
    uetr UUID,  -- Unique End-to-End Transaction Reference
    message_type VARCHAR(20),  -- MT103, pacs.008
    swift_message TEXT,  -- Full SWIFT message
    
    -- Sanctions
    sanctions_screened BOOLEAN NOT NULL DEFAULT FALSE,
    sanctions_hit BOOLEAN,
    sanctions_details JSONB,
    
    -- Status
    status VARCHAR(20) NOT NULL,  -- PENDING, SENT, CONFIRMED, FAILED
    reason_code VARCHAR(10),
    reason_description TEXT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    estimated_settlement_date DATE,
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_uetr (uetr),
    INDEX idx_swift_reference (swift_reference),
    INDEX idx_creditor_country (creditor_country),
    INDEX idx_created_at (created_at)
);

-- Sanctions List (cached)
CREATE TABLE sanctions_list (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    aliases TEXT[],  -- Array of aliases
    list_name VARCHAR(50) NOT NULL,  -- OFAC, UN, EU
    reason TEXT,
    country_code VARCHAR(2),
    effective_date DATE,
    last_updated TIMESTAMP NOT NULL,
    
    INDEX idx_name_gin (name gin_trgm_ops),  -- For fuzzy matching
    INDEX idx_list_name (list_name)
);

-- Correspondent Bank Routing
CREATE TABLE correspondent_banks (
    id BIGSERIAL PRIMARY KEY,
    our_bank_bic VARCHAR(11) NOT NULL,
    beneficiary_bank_bic VARCHAR(11) NOT NULL,
    intermediary_bank_bic VARCHAR(11) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    nostro_account VARCHAR(50),  -- Our account at intermediary
    estimated_days INT NOT NULL DEFAULT 2,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    UNIQUE (our_bank_bic, beneficiary_bank_bic, currency),
    INDEX idx_currency (currency)
);
```

---

## Configuration

```yaml
# application.yml
swift:
  enabled: true
  
  alliance-gateway:
    url: https://swift-gateway.internal
    timeout: 30000  # 30 seconds
    
  participant:
    bic: ABSAZAJJXXX  # Our bank BIC
    bank-name: "Absa Bank"
    country: ZA
    
  sanctions:
    enabled: true
    lists: [OFAC, UN, EU, UK]
    match-threshold: 0.85  # 85% similarity
    cache-ttl: 3600  # 1 hour
    
  fx:
    provider: bloomberg  # or reuters
    cache-ttl: 300  # 5 minutes
    
  correspondent:
    default-settlement-days: 2
    
  retry:
    max-attempts: 2
    backoff-delay: 5000  # 5 seconds
    
  compliance:
    aml-threshold: 10000.00  # USD 10,000
    ctr-threshold: 10000.00  # Currency Transaction Report
```

---

## Monitoring & Alerts

### Metrics

```yaml
# Prometheus Metrics
swift.payments.total:
  type: counter
  tags: [status, currency, country]
  
swift.payments.duration:
  type: timer
  tags: [currency]
  
swift.sanctions.hits.total:
  type: counter
  tags: [list_name]
  
swift.gateway.errors.total:
  type: counter
  tags: [error_type]
```

### Alerts

```yaml
- alert: SwiftSanctionsHitRate
  expr: |
    sum(rate(swift_sanctions_hits_total[1h])) > 5
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: High rate of sanctions hits (manual review required)
    
- alert: SwiftGatewayDown
  expr: up{job="swift-gateway"} == 0
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: SWIFT Alliance Gateway unreachable
```

---

## Compliance

### Mandatory Requirements

1. **Sanctions Screening**: All payments must be screened
2. **AML Reporting**: Large transactions (> USD 10,000)
3. **KYC**: Customer identification required
4. **Record Keeping**: 7 years minimum
5. **Audit Trail**: Complete payment history

### Regulatory Bodies

- **OFAC** (US): Office of Foreign Assets Control
- **UN**: United Nations Security Council sanctions
- **EU**: European Union sanctions
- **FATF**: Financial Action Task Force guidelines

---

## Summary

### Key Features

✅ **ISO 20022**: Modern XML messaging (pacs.008)  
✅ **MT Messages**: Legacy support (MT103)  
✅ **Sanctions Screening**: OFAC, UN, EU lists  
✅ **FX Conversion**: Real-time rates  
✅ **Correspondent Routing**: Automatic intermediary bank selection  
✅ **Compliance**: AML, CTR, KYC  
✅ **UETR**: Unique transaction tracking  

### Typical Flow

1. Customer initiates international payment
2. System screens for sanctions (mandatory)
3. Determine correspondent bank route
4. Apply FX rate (if needed)
5. Build SWIFT message (MT103 or pacs.008)
6. Send via SWIFT Alliance Gateway
7. Track via UETR
8. Confirm settlement (2-5 days)

### Cost & Timeline

- **Setup Cost**: $50K-100K (SWIFT membership, gateway)
- **Annual Cost**: $10K-50K (depending on volume)
- **Settlement Time**: 2-5 business days (not instant)
- **Transaction Fee**: $15-40 per payment

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Internal  
**Related Documents**:
- `06-SOUTH-AFRICA-CLEARING.md`
- `26-PAYSHAP-INTEGRATION.md`
