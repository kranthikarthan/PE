# Clearing Adapter Implementation Tickets
## Comprehensive Backlog for Production Readiness

**Project**: South African Payment Engine - Clearing Adapters  
**Created**: October 19, 2025  
**Total Tickets**: 25  
**Estimated Effort**: 120 Story Points (4-6 weeks with 2 developers)  

---

## 📊 **TICKET SUMMARY**

| Priority | Epic | Tickets | Story Points | Duration |
|----------|------|---------|--------------|----------|
| P0 - Blocker | ISO 20022 Implementation | 5 | 34 | Week 1-2 |
| P0 - Blocker | External Integration | 4 | 26 | Week 1-2 |
| P1 - Critical | SA Compliance | 6 | 30 | Week 2-3 |
| P2 - High | PayShap Integration | 3 | 13 | Week 3 |
| P2 - High | Observability | 3 | 8 | Week 4 |
| P3 - Medium | Performance & Testing | 4 | 9 | Week 5-6 |

**Total**: 25 tickets, 120 story points

---

# 🔴 **P0 - BLOCKER TICKETS** (Must Complete Before Production)

---

## **EPIC 1: ISO 20022 Implementation**

### **[PE-301] Replace String Concatenation with JAXB for ISO 20022 Messages**

**Epic**: ISO 20022 Implementation  
**Priority**: P0 - Blocker  
**Story Points**: 8  
**Assignee**: Backend Developer (Senior)  
**Sprint**: Sprint 1  

#### **Description**
Replace all string concatenation-based ISO 20022 message generation with proper JAXB/Jackson XML binding to ensure compliance with ISO 20022 standards and South African clearing systems (SAMOS, RTC, PayShap, SWIFT).

#### **Current State**
```java
// ❌ CURRENT: String concatenation - fragile and non-compliant
private String generatePacs008Message(Object paymentData) {
    return String.format(
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="%s:pacs.008.001.08">
            <FIToFICstmrCdtTrf>
        """, ...);
}
```

#### **Target State**
```java
// ✅ TARGET: JAXB with proper ISO 20022 bindings
@Service
public class Iso20022MessageBuilder {
    @Autowired private Marshaller iso20022Marshaller;
    
    public String buildPacs008(PaymentRequest request) {
        Document document = new Document();
        FIToFICstmrCdtTrf creditTransfer = new FIToFICstmrCdtTrf();
        // Build object model using JAXB generated classes
        return iso20022Marshaller.marshal(document);
    }
}
```

#### **Technical Requirements**
1. Add Maven dependencies for JAXB and ISO 20022
2. Generate JAXB classes from ISO 20022 XSD schemas
3. Create `Iso20022MessageBuilder` service for each adapter
4. Implement proper namespace handling
5. Add element ordering as per ISO 20022 spec

#### **Acceptance Criteria**
- [ ] JAXB dependencies added to all clearing adapter POMs
- [ ] ISO 20022 JAXB classes generated for:
  - [ ] pacs.008.001.08 (Customer Credit Transfer)
  - [ ] pacs.002.001.10 (Payment Status Report)
  - [ ] pacs.004.001.09 (Payment Return)
  - [ ] camt.054.001.08 (Bank to Customer Debit/Credit Notification)
- [ ] `Iso20022MessageBuilder` implemented for SAMOS
- [ ] `Iso20022MessageBuilder` implemented for RTC
- [ ] `Iso20022MessageBuilder` implemented for PayShap
- [ ] `Iso20022MessageBuilder` implemented for SWIFT
- [ ] All string concatenation removed
- [ ] Unit tests with XSD validation pass
- [ ] Integration tests with sample messages pass

#### **Dependencies**
```xml
<!-- Add to clearing adapter POMs -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>3.0.1</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>3.0.2</version>
</dependency>
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>org.eclipse.persistence.moxy</artifactId>
    <version>3.0.3</version>
</dependency>
```

#### **XSD Schema Generation**
```bash
# Generate JAXB classes from ISO 20022 schemas
xjc -p com.payments.iso20022.pacs008 \
    -d src/main/java \
    iso20022-schemas/pacs.008.001.08.xsd

xjc -p com.payments.iso20022.pacs002 \
    -d src/main/java \
    iso20022-schemas/pacs.002.001.10.xsd
```

#### **Testing Requirements**
- [ ] Unit tests for each message type builder
- [ ] XSD validation tests
- [ ] Namespace validation tests
- [ ] Element ordering validation tests
- [ ] Round-trip marshalling/unmarshalling tests

#### **Documentation**
- [ ] Update architecture documentation
- [ ] Create ISO 20022 message building guide
- [ ] Document JAXB configuration
- [ ] Add code examples to README files

#### **Estimated Time**: 2 weeks (10 working days)

---

### **[PE-302] Add XSD Validation for ISO 20022 Messages**

**Epic**: ISO 20022 Implementation  
**Priority**: P0 - Blocker  
**Story Points**: 5  
**Assignee**: Backend Developer  
**Sprint**: Sprint 1  
**Depends On**: PE-301  

#### **Description**
Implement comprehensive XSD validation for all ISO 20022 messages before submission to clearing systems to prevent message rejection and ensure compliance.

#### **Technical Requirements**
1. Download official ISO 20022 XSD schemas
2. Configure JAXB/Jackson validators
3. Create `Iso20022Validator` service
4. Add pre-submission validation
5. Implement validation error handling

#### **Implementation**
```java
@Service
public class Iso20022Validator {
    
    private final SchemaFactory schemaFactory;
    private final Map<String, Schema> schemaCache;
    
    public ValidationResult validate(String xml, Iso20022MessageType messageType) {
        try {
            Schema schema = getSchema(messageType);
            Validator validator = schema.newValidator();
            
            Source source = new StreamSource(new StringReader(xml));
            validator.validate(source);
            
            return ValidationResult.valid();
        } catch (SAXException e) {
            return ValidationResult.invalid(
                "XSD validation failed: " + e.getMessage(),
                extractValidationErrors(e)
            );
        }
    }
    
    private Schema getSchema(Iso20022MessageType messageType) {
        return schemaCache.computeIfAbsent(
            messageType.getSchemaName(),
            this::loadSchema
        );
    }
}
```

#### **Acceptance Criteria**
- [ ] XSD schemas downloaded and stored in `src/main/resources/xsd/`
- [ ] `Iso20022Validator` service created
- [ ] Schema caching implemented
- [ ] Pre-submission validation added to all services:
  - [ ] `SamosPaymentService.submitPayment()`
  - [ ] `RtcPaymentService.submitPayment()`
  - [ ] `PayShapPaymentService.submitPayment()`
  - [ ] `SwiftPaymentService.submitPayment()`
- [ ] Validation error handling implemented
- [ ] Error messages mapped to business-friendly descriptions
- [ ] Metrics for validation failures added
- [ ] All tests pass with valid and invalid messages

#### **Testing Requirements**
- [ ] Test with valid ISO 20022 messages
- [ ] Test with invalid messages (missing required fields)
- [ ] Test with incorrect element ordering
- [ ] Test with invalid data types
- [ ] Test schema caching performance

#### **Estimated Time**: 1 week (5 working days)

---

### **[PE-303] Implement UETR (Unique End-to-End Transaction Reference) Generation**

**Epic**: ISO 20022 Implementation  
**Priority**: P0 - Blocker  
**Story Points**: 3  
**Assignee**: Backend Developer  
**Sprint**: Sprint 1  

#### **Description**
Implement UETR generation as required by SARB SAMOS and SWIFT for end-to-end transaction tracking and reconciliation.

#### **SARB Requirement**
Per docs/06-SOUTH-AFRICA-CLEARING.md:
```xml
<!-- MANDATORY field for SAMOS/SWIFT -->
<UETR>550e8400-e29b-41d4-a716-446655440000</UETR>
```

#### **Implementation**
```java
@Service
public class UetrGenerationService {
    
    /**
     * Generate RFC 4122 compliant UETR (UUID v4)
     * Format: 8-4-4-4-12 hexadecimal digits
     */
    public String generateUetr() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    
    /**
     * Validate UETR format
     */
    public boolean isValidUetr(String uetr) {
        try {
            UUID.fromString(uetr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Generate and persist UETR for payment tracking
     */
    @Transactional
    public PaymentUetr createPaymentUetr(String paymentId, String clearingSystem) {
        String uetr = generateUetr();
        
        PaymentUetr paymentUetr = PaymentUetr.builder()
            .uetr(uetr)
            .paymentId(paymentId)
            .clearingSystem(clearingSystem)
            .generatedAt(Instant.now())
            .build();
        
        return paymentUetrRepository.save(paymentUetr);
    }
}
```

#### **Database Schema**
```sql
CREATE TABLE payment_uetr_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uetr UUID NOT NULL UNIQUE,
    payment_id VARCHAR(100) NOT NULL,
    clearing_system VARCHAR(50) NOT NULL,
    message_id VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    settled_at TIMESTAMP,
    
    INDEX idx_uetr (uetr),
    INDEX idx_payment_id (payment_id),
    INDEX idx_clearing_system (clearing_system),
    INDEX idx_status (status)
);
```

#### **Acceptance Criteria**
- [ ] `UetrGenerationService` created
- [ ] UETR generation follows RFC 4122 (UUID v4)
- [ ] UETR validation implemented
- [ ] Database table `payment_uetr_tracking` created
- [ ] UETR added to all ISO 20022 messages:
  - [ ] SAMOS pacs.008
  - [ ] RTC pacs.008
  - [ ] PayShap pacs.008
  - [ ] SWIFT pacs.008
- [ ] UETR tracking API endpoints created:
  - [ ] `GET /api/v1/payments/uetr/{uetr}`
  - [ ] `GET /api/v1/payments/{paymentId}/uetr`
- [ ] End-to-end tracking dashboard updated
- [ ] All tests pass

#### **Testing Requirements**
- [ ] Test UETR format validation
- [ ] Test UETR uniqueness
- [ ] Test UETR in ISO 20022 messages
- [ ] Test UETR tracking queries
- [ ] Performance test: 10,000 UETR generations/sec

#### **Estimated Time**: 3 days

---

### **[PE-304] Add Settlement Account Management for SAMOS**

**Epic**: ISO 20022 Implementation  
**Priority**: P0 - Blocker  
**Story Points**: 8  
**Assignee**: Backend Developer (Senior)  
**Sprint**: Sprint 1  

#### **Description**
Implement settlement account management for SAMOS RTGS payments, including account validation, balance tracking, and settlement information in ISO 20022 messages.

#### **SARB Requirement**
Per docs/06-SOUTH-AFRICA-CLEARING.md:
```xml
<!-- MANDATORY for SAMOS -->
<SttlmInf>
    <SttlmMtd>INDA</SttlmMtd>
    <SttlmAcct>
        <Id>
            <Othr>
                <Id>SETTLEMENT-ACCOUNT-ID</Id>
            </Othr>
        </Id>
    </SttlmAcct>
</SttlmInf>
```

#### **Technical Requirements**
1. Create settlement account configuration
2. Implement account balance tracking
3. Add liquidity management alerts
4. Implement settlement confirmation processing
5. Add daily reconciliation

#### **Database Schema**
```sql
CREATE TABLE samos_settlement_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    settlement_account_id VARCHAR(100) NOT NULL UNIQUE,
    account_name VARCHAR(200) NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- RTGS_SETTLEMENT
    currency VARCHAR(3) NOT NULL DEFAULT 'ZAR',
    bank_code VARCHAR(10) NOT NULL,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0,
    reserved_balance DECIMAL(18,2) NOT NULL DEFAULT 0,
    minimum_balance DECIMAL(18,2) NOT NULL,
    alert_threshold DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- ACTIVE, SUSPENDED, CLOSED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_account_id (settlement_account_id)
);

CREATE TABLE samos_settlement_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    settlement_account_id VARCHAR(100) NOT NULL,
    uetr UUID NOT NULL,
    payment_id VARCHAR(100) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- DEBIT, CREDIT, RESERVE, RELEASE
    amount DECIMAL(18,2) NOT NULL,
    balance_before DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    transaction_timestamp TIMESTAMP NOT NULL,
    settlement_date DATE NOT NULL,
    reference VARCHAR(200),
    status VARCHAR(50) NOT NULL,
    
    INDEX idx_account_id (settlement_account_id),
    INDEX idx_uetr (uetr),
    INDEX idx_payment_id (payment_id),
    INDEX idx_transaction_timestamp (transaction_timestamp)
);
```

#### **Implementation**
```java
@Service
public class SamosSettlementAccountService {
    
    /**
     * Check if sufficient funds available for payment
     */
    public boolean hasSufficientFunds(String settlementAccountId, BigDecimal amount) {
        SamosSettlementAccount account = getAccount(settlementAccountId);
        return account.getAvailableBalance()
            .subtract(account.getReservedBalance())
            .compareTo(amount) >= 0;
    }
    
    /**
     * Reserve funds for payment submission
     */
    @Transactional
    public void reserveFunds(String settlementAccountId, String paymentId, 
                           BigDecimal amount, String uetr) {
        SamosSettlementAccount account = getAccount(settlementAccountId);
        
        if (!hasSufficientFunds(settlementAccountId, amount)) {
            throw new InsufficientFundsException(
                "Insufficient funds in settlement account: " + settlementAccountId);
        }
        
        BigDecimal balanceBefore = account.getAvailableBalance();
        account.setReservedBalance(account.getReservedBalance().add(amount));
        
        // Record transaction
        SamosSettlementTransaction transaction = SamosSettlementTransaction.builder()
            .settlementAccountId(settlementAccountId)
            .uetr(UUID.fromString(uetr))
            .paymentId(paymentId)
            .transactionType(TransactionType.RESERVE)
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(account.getAvailableBalance())
            .transactionTimestamp(Instant.now())
            .settlementDate(LocalDate.now())
            .status("RESERVED")
            .build();
        
        settlementTransactionRepository.save(transaction);
        settlementAccountRepository.save(account);
        
        // Alert if below threshold
        if (account.getAvailableBalance()
                .subtract(account.getReservedBalance())
                .compareTo(account.getAlertThreshold()) < 0) {
            alertService.sendLowBalanceAlert(account);
        }
    }
    
    /**
     * Confirm settlement and release funds
     */
    @Transactional
    public void confirmSettlement(String uetr, String settlementStatus) {
        SamosSettlementTransaction transaction = 
            settlementTransactionRepository.findByUetr(UUID.fromString(uetr))
                .orElseThrow(() -> new SettlementTransactionNotFoundException(uetr));
        
        SamosSettlementAccount account = getAccount(transaction.getSettlementAccountId());
        
        if ("ACSC".equals(settlementStatus)) { // Accepted Settlement Completed
            // Debit available balance
            account.setAvailableBalance(
                account.getAvailableBalance().subtract(transaction.getAmount())
            );
            // Release reserved amount
            account.setReservedBalance(
                account.getReservedBalance().subtract(transaction.getAmount())
            );
            transaction.setStatus("SETTLED");
        } else if ("RJCT".equals(settlementStatus)) { // Rejected
            // Release reserved amount only
            account.setReservedBalance(
                account.getReservedBalance().subtract(transaction.getAmount())
            );
            transaction.setStatus("REJECTED");
        }
        
        settlementAccountRepository.save(account);
        settlementTransactionRepository.save(transaction);
    }
}
```

#### **Acceptance Criteria**
- [ ] Database tables created
- [ ] `SamosSettlementAccountService` implemented
- [ ] Settlement account configuration API:
  - [ ] `POST /api/v1/samos/settlement-accounts`
  - [ ] `GET /api/v1/samos/settlement-accounts/{id}`
  - [ ] `GET /api/v1/samos/settlement-accounts/{id}/balance`
  - [ ] `GET /api/v1/samos/settlement-accounts/{id}/transactions`
- [ ] Fund reservation logic implemented
- [ ] Settlement confirmation processing implemented
- [ ] Low balance alerts configured
- [ ] Daily reconciliation job scheduled
- [ ] Settlement account included in ISO 20022 messages
- [ ] All tests pass

#### **Testing Requirements**
- [ ] Test fund reservation
- [ ] Test insufficient funds handling
- [ ] Test settlement confirmation
- [ ] Test settlement rejection
- [ ] Test low balance alerts
- [ ] Test concurrent reservations
- [ ] Performance test: 1000 transactions/sec

#### **Estimated Time**: 2 weeks (10 working days)

---

### **[PE-305] Implement Proper ISO 20022 Namespace Handling**

**Epic**: ISO 20022 Implementation  
**Priority**: P0 - Blocker  
**Story Points**: 5  
**Assignee**: Backend Developer  
**Sprint**: Sprint 2  
**Depends On**: PE-301  

#### **Description**
Implement correct ISO 20022 namespace handling to ensure messages are accepted by clearing systems.

#### **Current Issue**
```java
// ❌ WRONG: Missing or incorrect namespace
<Document>
    <FIToFICstmrCdtTrf>
```

#### **Correct Format**
```xml
<!-- ✅ CORRECT: Proper namespace declaration -->
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <FIToFICstmrCdtTrf>
```

#### **Implementation**
```java
@Configuration
public class Iso20022MarshallerConfig {
    
    @Bean
    public Marshaller pacs008Marshaller() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(
            "com.payments.iso20022.pacs008"
        );
        
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(
            Marshaller.JAXB_SCHEMA_LOCATION,
            "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08 pacs.008.001.08.xsd"
        );
        
        // Set namespace prefix mapper
        marshaller.setProperty(
            "com.sun.xml.bind.namespacePrefixMapper",
            new Iso20022NamespacePrefixMapper()
        );
        
        return marshaller;
    }
}

public class Iso20022NamespacePrefixMapper extends NamespacePrefixMapper {
    
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, 
                                     boolean requirePrefix) {
        if ("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08".equals(namespaceUri)) {
            return "";  // Default namespace
        }
        return suggestion;
    }
}
```

#### **Acceptance Criteria**
- [ ] Namespace configuration for all message types
- [ ] Namespace prefix mapper implemented
- [ ] XSD schema location added
- [ ] Generated XML validated against clearing system requirements
- [ ] All tests pass with namespace validation

#### **Estimated Time**: 1 week (5 working days)

---

## **EPIC 2: External Clearing Network Integration**

### **[PE-306] Implement SAMOS Clearing Network Client with mTLS**

**Epic**: External Integration  
**Priority**: P0 - Blocker  
**Story Points**: 8  
**Assignee**: Backend Developer (Senior)  
**Sprint**: Sprint 2  

#### **Description**
Implement OpenFeign client for SAMOS RTGS system with mutual TLS (mTLS) authentication using SARB-issued certificates.

#### **SARB Requirements**
Per docs/06-SOUTH-AFRICA-CLEARING.md:
- Protocol: SWIFT (FIN/InterAct) or HTTPS REST
- Authentication: mTLS with SARB-issued client certificates
- Endpoint: https://samos.sarb.co.za/rtgs
- Timeout: 30 seconds (hard limit)
- No retries allowed for RTGS

#### **Implementation**
```java
@FeignClient(
    name = "samos-clearing-network",
    url = "${samos.endpoint}",
    configuration = SamosMutualTlsConfig.class,
    fallback = SamosClearingFallback.class
)
public interface SamosClearingClient {
    
    @PostMapping(
        value = "/api/v1/payments",
        consumes = "application/xml",
        produces = "application/xml"
    )
    @Headers({
        "Content-Type: application/xml",
        "X-Message-Type: pacs.008",
        "X-Bank-Code: {bankCode}",
        "X-Certificate-Serial: {certSerial}"
    })
    SamosPaymentResponse submitPayment(
        @RequestBody String pacs008Xml,
        @Header("X-Bank-Code") String bankCode,
        @Header("X-Certificate-Serial") String certSerial,
        @Header("X-UETR") String uetr
    );
    
    @GetMapping("/api/v1/payments/{messageId}/status")
    SamosStatusResponse getPaymentStatus(
        @PathVariable String messageId,
        @Header("X-Bank-Code") String bankCode
    );
    
    @PostMapping("/api/v1/payments/{messageId}/cancel")
    SamosCancellationResponse cancelPayment(
        @PathVariable String messageId,
        @RequestBody SamosCancellationRequest request,
        @Header("X-Bank-Code") String bankCode
    );
}
```

#### **mTLS Configuration**
```java
@Configuration
public class SamosMutualTlsConfig {
    
    @Value("${samos.certificate.path}")
    private String certificatePath;
    
    @Value("${samos.certificate.password}")
    private String certificatePassword;
    
    @Value("${samos.truststore.path}")
    private String truststorePath;
    
    @Value("${samos.truststore.password}")
    private String truststorePassword;
    
    @Bean
    public Client samosFeignClient() throws Exception {
        // Load client certificate (SARB-issued)
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = new FileInputStream(certificatePath)) {
            keyStore.load(is, certificatePassword.toCharArray());
        }
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        );
        kmf.init(keyStore, certificatePassword.toCharArray());
        
        // Load truststore (SARB CA certificates)
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = new FileInputStream(truststorePath)) {
            trustStore.load(is, truststorePassword.toCharArray());
        }
        
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        );
        tmf.init(trustStore);
        
        // Configure SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
            kmf.getKeyManagers(),
            tmf.getTrustManagers(),
            new SecureRandom()
        );
        
        // Create Feign client with mTLS
        return new Client.Default(
            sslContext.getSocketFactory(),
            (hostname, session) -> true  // TODO: Implement proper hostname verification
        );
    }
    
    @Bean
    public Request.Options samosRequestOptions() {
        return new Request.Options(
            30, TimeUnit.SECONDS,  // Connect timeout
            30, TimeUnit.SECONDS   // Read timeout (SARB limit)
        );
    }
}
```

#### **Fallback Implementation**
```java
@Component
@Slf4j
public class SamosClearingFallback implements SamosClearingClient {
    
    @Override
    public SamosPaymentResponse submitPayment(String pacs008Xml, String bankCode,
                                              String certSerial, String uetr) {
        log.error("SAMOS clearing network unavailable - fallback triggered for UETR: {}", uetr);
        
        // Return error response
        return SamosPaymentResponse.builder()
            .status("NETWORK_ERROR")
            .errorCode("SAMOS_UNAVAILABLE")
            .errorMessage("SAMOS clearing network is temporarily unavailable")
            .timestamp(Instant.now())
            .build();
    }
    
    @Override
    public SamosStatusResponse getPaymentStatus(String messageId, String bankCode) {
        log.error("SAMOS status query unavailable - fallback triggered");
        throw new SamosClearingNetworkUnavailableException(
            "Cannot query payment status - SAMOS network unavailable"
        );
    }
    
    @Override
    public SamosCancellationResponse cancelPayment(String messageId, 
                                                   SamosCancellationRequest request,
                                                   String bankCode) {
        log.error("SAMOS cancellation unavailable - fallback triggered");
        throw new SamosClearingNetworkUnavailableException(
            "Cannot cancel payment - SAMOS network unavailable"
        );
    }
}
```

#### **Configuration Properties**
```yaml
# application.yml
samos:
  endpoint: ${SAMOS_ENDPOINT:https://samos.sarb.co.za/rtgs}
  certificate:
    path: ${SAMOS_CERT_PATH:/etc/certs/samos-client.p12}
    password: ${SAMOS_CERT_PASSWORD}
    serial: ${SAMOS_CERT_SERIAL}
  truststore:
    path: ${SAMOS_TRUSTSTORE_PATH:/etc/certs/sarb-truststore.p12}
    password: ${SAMOS_TRUSTSTORE_PASSWORD}
  timeout-seconds: 30
  bank-code: ${BANK_CODE}
  
# Resilience4j - NO RETRIES for RTGS
resilience4j:
  circuitbreaker:
    instances:
      samos-clearing-network:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      samos-clearing-network:
        max-attempts: 1  # NO RETRIES for RTGS
  timelimiter:
    instances:
      samos-clearing-network:
        timeout-duration: 30s  # SARB hard limit
```

#### **Acceptance Criteria**
- [ ] `SamosClearingClient` interface created
- [ ] mTLS configuration implemented
- [ ] SARB certificate integration tested
- [ ] Fallback implementation created
- [ ] Payment submission endpoint working
- [ ] Status query endpoint working
- [ ] Cancellation endpoint working
- [ ] 30-second timeout enforced
- [ ] No retry configured (RTGS requirement)
- [ ] Circuit breaker configured
- [ ] Integration tests with SAMOS UAT environment pass
- [ ] Certificate expiry monitoring added
- [ ] All unit tests pass

#### **Testing Requirements**
- [ ] Test with valid SARB certificate
- [ ] Test with expired certificate
- [ ] Test with invalid certificate
- [ ] Test timeout handling
- [ ] Test circuit breaker behavior
- [ ] Test fallback scenarios
- [ ] Load test: 100 concurrent requests

#### **Security Checklist**
- [ ] Certificate private key encrypted at rest
- [ ] Certificate password stored in secure vault
- [ ] Certificate serial number logged
- [ ] Certificate expiry alerts configured
- [ ] Hostname verification implemented
- [ ] TLS 1.3 enforced

#### **Documentation**
- [ ] Certificate installation guide
- [ ] mTLS configuration documentation
- [ ] Troubleshooting guide
- [ ] Runbook for certificate renewal

#### **Estimated Time**: 2 weeks (10 working days)

---

### **[PE-307] Implement BankservAfrica SFTP Client with PGP Encryption**

**Epic**: External Integration  
**Priority**: P0 - Blocker  
**Story Points**: 8  
**Assignee**: Backend Developer (Senior)  
**Sprint**: Sprint 2  

#### **Description**
Implement SFTP client for BankservAfrica ACH/EFT file submission with PGP encryption and digital signatures.

#### **BankservAfrica Requirements**
Per docs/06-SOUTH-AFRICA-CLEARING.md:
- Protocol: SFTP with SSH key authentication
- Encryption: PGP (RSA 2048) with BankservAfrica public key
- Signature: PGP detached signature (.sig file)
- Upload path: sftp://ach.bankserv.co.za/uploads/
- Download path: sftp://ach.bankserv.co.za/downloads/
- File format: Fixed-length ACH records

#### **Implementation**
```java
@Service
@Slf4j
public class BankservAfricaSftpService {
    
    @Autowired
    private JSch jsch;
    
    @Autowired
    private PgpEncryptionService pgpService;
    
    @Value("${bankservafrica.sftp.host}")
    private String sftpHost;
    
    @Value("${bankservafrica.sftp.port}")
    private int sftpPort;
    
    @Value("${bankservafrica.sftp.username}")
    private String sftpUsername;
    
    @Value("${bankservafrica.ssh.key.path}")
    private String sshKeyPath;
    
    @Value("${bankservafrica.ssh.key.passphrase}")
    private String sshKeyPassphrase;
    
    /**
     * Upload ACH batch file to BankservAfrica
     */
    @CircuitBreaker(name = "bankservafrica-sftp")
    @Retry(name = "bankservafrica-sftp")
    public BankservAfricaUploadResult uploadAchFile(
        String fileName,
        byte[] fileContent,
        String batchId
    ) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            log.info("Uploading ACH file to BankservAfrica: {} ({} bytes)", 
                     fileName, fileContent.length);
            
            // 1. PGP encrypt file content
            byte[] encryptedContent = pgpService.encrypt(
                fileContent,
                pgpService.loadBankservAfricaPublicKey()
            );
            
            // 2. Generate detached signature
            byte[] signature = pgpService.sign(
                encryptedContent,
                pgpService.loadBankPrivateKey()
            );
            
            // 3. Setup SSH session
            session = createSshSession();
            
            // 4. Open SFTP channel
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            // 5. Upload encrypted file
            String remotePath = "/uploads/" + fileName + ".pgp";
            sftpChannel.put(
                new ByteArrayInputStream(encryptedContent),
                remotePath,
                ChannelSftp.OVERWRITE
            );
            
            // 6. Upload signature file
            String sigPath = "/uploads/" + fileName + ".sig";
            sftpChannel.put(
                new ByteArrayInputStream(signature),
                sigPath,
                ChannelSftp.OVERWRITE
            );
            
            log.info("Successfully uploaded ACH file and signature: {}", fileName);
            
            return BankservAfricaUploadResult.builder()
                .batchId(batchId)
                .fileName(fileName)
                .fileSize(fileContent.length)
                .encryptedSize(encryptedContent.length)
                .remotePath(remotePath)
                .uploadTimestamp(Instant.now())
                .status("UPLOADED")
                .build();
            
        } catch (JSchException | SftpException e) {
            log.error("Failed to upload ACH file: {}", fileName, e);
            throw new BankservAfricaSftpException("Upload failed", e);
        } finally {
            cleanup(sftpChannel, session);
        }
    }
    
    /**
     * Download acknowledgment file from BankservAfrica
     */
    @CircuitBreaker(name = "bankservafrica-sftp")
    @Retry(name = "bankservafrica-sftp")
    public BankservAfricaAckFile downloadAcknowledgment(String fileName) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            log.info("Downloading ACK file from BankservAfrica: {}", fileName);
            
            // 1. Setup SSH session
            session = createSshSession();
            
            // 2. Open SFTP channel
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            // 3. Download encrypted ACK file
            String remotePath = "/downloads/" + fileName + ".pgp";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            sftpChannel.get(remotePath, baos);
            byte[] encryptedContent = baos.toByteArray();
            
            // 4. Download signature
            String sigPath = "/downloads/" + fileName + ".sig";
            ByteArrayOutputStream sigBaos = new ByteArrayOutputStream();
            sftpChannel.get(sigPath, sigBaos);
            byte[] signature = sigBaos.toByteArray();
            
            // 5. Verify signature
            boolean signatureValid = pgpService.verify(
                encryptedContent,
                signature,
                pgpService.loadBankservAfricaPublicKey()
            );
            
            if (!signatureValid) {
                throw new BankservAfricaSignatureException(
                    "Invalid signature for ACK file: " + fileName
                );
            }
            
            // 6. Decrypt content
            byte[] decryptedContent = pgpService.decrypt(
                encryptedContent,
                pgpService.loadBankPrivateKey()
            );
            
            // 7. Parse ACK file
            BankservAfricaAckFile ackFile = parseAckFile(
                new String(decryptedContent, StandardCharsets.UTF_8)
            );
            
            log.info("Successfully downloaded and parsed ACK file: {}", fileName);
            
            return ackFile;
            
        } catch (JSchException | SftpException e) {
            log.error("Failed to download ACK file: {}", fileName, e);
            throw new BankservAfricaSftpException("Download failed", e);
        } finally {
            cleanup(sftpChannel, session);
        }
    }
    
    private Session createSshSession() throws JSchException {
        Session session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
        session.setConfig("StrictHostKeyChecking", "yes");
        session.setConfig("PreferredAuthentications", "publickey");
        
        // Add SSH private key
        jsch.addIdentity(
            sshKeyPath,
            sshKeyPassphrase.getBytes(StandardCharsets.UTF_8)
        );
        
        session.connect(30000);  // 30 second timeout
        return session;
    }
    
    private void cleanup(ChannelSftp channel, Session session) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
```

#### **PGP Encryption Service**
```java
@Service
@Slf4j
public class PgpEncryptionService {
    
    @Value("${bankservafrica.pgp.public-key-path}")
    private String bankservAfricaPublicKeyPath;
    
    @Value("${bank.pgp.private-key-path}")
    private String bankPrivateKeyPath;
    
    @Value("${bank.pgp.private-key-passphrase}")
    private String bankPrivateKeyPassphrase;
    
    public byte[] encrypt(byte[] data, PGPPublicKey publicKey) throws Exception {
        ByteArrayOutputStream encOut = new ByteArrayOutputStream();
        
        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
            new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256)
                .setWithIntegrityPacket(true)
                .setSecureRandom(new SecureRandom())
                .setProvider("BC")
        );
        
        encGen.addMethod(
            new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
                .setProvider("BC")
        );
        
        OutputStream cOut = encGen.open(encOut, new byte[4096]);
        cOut.write(data);
        cOut.close();
        
        return encOut.toByteArray();
    }
    
    public byte[] decrypt(byte[] encryptedData, PGPPrivateKey privateKey) 
        throws Exception {
        // Implement PGP decryption
    }
    
    public byte[] sign(byte[] data, PGPPrivateKey privateKey) throws Exception {
        // Implement PGP detached signature
    }
    
    public boolean verify(byte[] data, byte[] signature, PGPPublicKey publicKey) 
        throws Exception {
        // Implement signature verification
    }
    
    public PGPPublicKey loadBankservAfricaPublicKey() throws Exception {
        try (InputStream is = new FileInputStream(bankservAfricaPublicKeyPath)) {
            return readPublicKey(is);
        }
    }
    
    public PGPPrivateKey loadBankPrivateKey() throws Exception {
        try (InputStream is = new FileInputStream(bankPrivateKeyPath)) {
            return readPrivateKey(is, bankPrivateKeyPassphrase);
        }
    }
}
```

#### **Dependencies**
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpg-jdk18on</artifactId>
    <version>1.76</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.76</version>
</dependency>
```

#### **Configuration Properties**
```yaml
bankservafrica:
  sftp:
    host: ${BANKSERVAFRICA_SFTP_HOST:ach.bankserv.co.za}
    port: ${BANKSERVAFRICA_SFTP_PORT:22}
    username: ${BANKSERVAFRICA_SFTP_USERNAME}
  ssh:
    key:
      path: ${BANKSERVAFRICA_SSH_KEY_PATH:/etc/ssh/bankserv_rsa}
      passphrase: ${BANKSERVAFRICA_SSH_KEY_PASSPHRASE}
  pgp:
    public-key-path: ${BANKSERVAFRICA_PGP_PUBLIC_KEY:/etc/pgp/bankserv.pub}
  upload-path: /uploads/
  download-path: /downloads/
  
bank:
  pgp:
    private-key-path: ${BANK_PGP_PRIVATE_KEY:/etc/pgp/bank-private.key}
    private-key-passphrase: ${BANK_PGP_PASSPHRASE}
```

#### **Acceptance Criteria**
- [ ] SFTP client implemented
- [ ] SSH key authentication working
- [ ] PGP encryption implemented (RSA 2048)
- [ ] PGP detached signatures implemented
- [ ] File upload tested
- [ ] File download tested
- [ ] Signature verification working
- [ ] ACK file parsing implemented
- [ ] Circuit breaker configured
- [ ] Retry logic configured
- [ ] Integration tests with BankservAfrica UAT pass
- [ ] All unit tests pass

#### **Testing Requirements**
- [ ] Test SFTP connection
- [ ] Test PGP encryption/decryption
- [ ] Test signature generation/verification
- [ ] Test file upload
- [ ] Test file download
- [ ] Test connection failures
- [ ] Test invalid signatures
- [ ] Load test: 100 files/hour

#### **Security Checklist**
- [ ] SSH private key encrypted
- [ ] PGP private key encrypted
- [ ] Passphrases in secure vault
- [ ] StrictHostKeyChecking enabled
- [ ] Known hosts file configured
- [ ] Key rotation procedure documented

#### **Estimated Time**: 2 weeks (10 working days)

---

### **[PE-308] Implement PayShap Proxy Registry Client**

**Epic**: External Integration  
**Priority**: P0 - Blocker  
**Story Points**: 5  
**Assignee**: Backend Developer  
**Sprint**: Sprint 3  

#### **Description**
Implement REST client for PayShap Proxy Registry to lookup recipient account details from mobile number or email address.

#### **PayShap Requirements**
Per docs/26-PAYSHAP-INTEGRATION.md:
- Proxy types: Mobile (MSISDN), Email
- Lookup endpoint: https://api.payshap.co.za/v1/proxy/lookup
- Authentication: OAuth 2.0 + mTLS
- Cache TTL: 1 hour
- Response time: < 3 seconds

#### **Implementation**
```java
@FeignClient(
    name = "payshap-proxy-registry",
    url = "${payshap.proxy.registry.url}",
    configuration = PayShapProxyRegistryConfig.class
)
public interface PayShapProxyRegistryClient {
    
    @PostMapping("/v1/proxy/lookup")
    ProxyLookupResponse lookupProxy(
        @RequestBody ProxyLookupRequest request,
        @Header("Authorization") String authorization
    );
    
    @PostMapping("/v1/proxy/register")
    ProxyRegistrationResponse registerProxy(
        @RequestBody ProxyRegistrationRequest request,
        @Header("Authorization") String authorization
    );
    
    @DeleteMapping("/v1/proxy/{proxyId}")
    void deregisterProxy(
        @PathVariable String proxyId,
        @Header("Authorization") String authorization
    );
}

@Service
@Slf4j
public class PayShapProxyService {
    
    @Autowired
    private PayShapProxyRegistryClient proxyClient;
    
    @Autowired
    private PayShapOAuth2TokenService tokenService;
    
    /**
     * Lookup recipient account from mobile/email
     * Results cached for 1 hour
     */
    @Cacheable(
        value = "payshap-proxy",
        key = "#proxyId + '_' + #proxyType",
        unless = "#result == null"
    )
    @CircuitBreaker(name = "payshap-proxy", fallbackMethod = "lookupProxyFallback")
    @Retry(name = "payshap-proxy")
    @TimeLimiter(name = "payshap-proxy")
    public CompletableFuture<ProxyLookupResult> lookupProxy(
        String proxyId,
        ProxyType proxyType
    ) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Looking up PayShap proxy: type={}, id={}", 
                     proxyType, maskProxy(proxyId));
            
            ProxyLookupRequest request = ProxyLookupRequest.builder()
                .proxyId(proxyId)
                .proxyType(proxyType)
                .build();
            
            String authHeader = tokenService.getAuthorizationHeader();
            
            ProxyLookupResponse response = proxyClient.lookupProxy(
                request, authHeader
            );
            
            if (response.isFound()) {
                log.info("Proxy found: proxy={}, bank={}", 
                         maskProxy(proxyId), response.getBankCode());
                
                return ProxyLookupResult.builder()
                    .proxyId(proxyId)
                    .proxyType(proxyType)
                    .accountNumber(response.getAccountNumber())
                    .bankCode(response.getBankCode())
                    .accountHolderName(response.getAccountHolderName())
                    .status(ProxyStatus.ACTIVE)
                    .cachedAt(Instant.now())
                    .build();
            }
            
            log.warn("Proxy not found: {}", maskProxy(proxyId));
            return null;
        });
    }
    
    public CompletableFuture<ProxyLookupResult> lookupProxyFallback(
        String proxyId,
        ProxyType proxyType,
        Exception ex
    ) {
        log.error("Proxy lookup failed: {} - {}", 
                  maskProxy(proxyId), ex.getMessage());
        return CompletableFuture.failedFuture(
            new ProxyLookupException("Proxy registry unavailable", ex)
        );
    }
    
    /**
     * Mask proxy ID for logging (POPIA compliance)
     */
    private String maskProxy(String proxy) {
        if (proxy.startsWith("+27")) {
            // Mobile: +27821234567 -> +2782****567
            return proxy.substring(0, 5) + "****" + 
                   proxy.substring(proxy.length() - 3);
        } else if (proxy.contains("@")) {
            // Email: user@example.com -> us****@example.com
            String[] parts = proxy.split("@");
            return parts[0].substring(0, 2) + "****@" + parts[1];
        }
        return "****";
    }
}
```

#### **Cache Configuration**
```java
@Configuration
@EnableCaching
public class PayShapCacheConfig {
    
    @Bean
    public CacheManager payShapCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))  // 1 hour TTL
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

#### **DTOs**
```java
@Data
@Builder
public class ProxyLookupRequest {
    private String proxyId;          // +27821234567 or user@email.com
    private ProxyType proxyType;     // MSISDN or EMAIL
}

@Data
public class ProxyLookupResponse {
    private boolean found;
    private String accountNumber;
    private String bankCode;
    private String accountHolderName;
    private ProxyStatus status;
}

@Data
@Builder
public class ProxyLookupResult {
    private String proxyId;
    private ProxyType proxyType;
    private String accountNumber;
    private String bankCode;
    private String accountHolderName;
    private ProxyStatus status;
    private Instant cachedAt;
}

public enum ProxyType {
    MSISDN,     // Mobile number
    EMAIL,      // Email address
    ID_NUMBER   // SA ID number (future)
}

public enum ProxyStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING
}
```

#### **Configuration Properties**
```yaml
payshap:
  proxy:
    registry:
      url: ${PAYSHAP_PROXY_REGISTRY_URL:https://api.payshap.co.za}
      timeout: 3000  # 3 seconds
      cache-ttl: 3600  # 1 hour
  oauth2:
    token-uri: ${PAYSHAP_TOKEN_URI:https://auth.payshap.co.za/oauth/token}
    client-id: ${PAYSHAP_CLIENT_ID}
    client-secret: ${PAYSHAP_CLIENT_SECRET}
    
resilience4j:
  circuitbreaker:
    instances:
      payshap-proxy:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      payshap-proxy:
        max-attempts: 2
        wait-duration: 500ms
  timelimiter:
    instances:
      payshap-proxy:
        timeout-duration: 3s
```

#### **Acceptance Criteria**
- [ ] Feign client implemented
- [ ] OAuth 2.0 authentication working
- [ ] Proxy lookup for mobile numbers working
- [ ] Proxy lookup for email addresses working
- [ ] Caching implemented (1 hour TTL)
- [ ] Cache eviction working
- [ ] POPIA-compliant logging (masked proxy IDs)
- [ ] Circuit breaker configured
- [ ] Retry logic configured
- [ ] Timeout configured (3 seconds)
- [ ] Integration tests with PayShap UAT pass
- [ ] All unit tests pass

#### **Testing Requirements**
- [ ] Test mobile number lookup
- [ ] Test email lookup
- [ ] Test proxy not found
- [ ] Test cache hit/miss
- [ ] Test cache expiry
- [ ] Test timeout handling
- [ ] Test circuit breaker
- [ ] Performance test: 1000 lookups/sec

#### **Estimated Time**: 1 week (5 working days)

---

### **[PE-309] Implement SWIFT Network Client (Optional)**

**Epic**: External Integration  
**Priority**: P0 - Blocker (if SWIFT required)  
**Story Points**: 5  
**Assignee**: Backend Developer  
**Sprint**: Sprint 3  

#### **Description**
Implement SWIFT network client for international payments (if required).

**Note**: Only implement if international payments are in scope. Otherwise, mark as "Won't Do" and document for future implementation.

#### **SWIFT Requirements**
Per docs/27-SWIFT-INTEGRATION.md:
- Protocol: SWIFTNet (FileAct/InterAct) or SWIFT gpi API
- Authentication: SWIFTNet PKI certificates
- Message types: MT103 (legacy) or pacs.008 (ISO 20022)
- Sanctions screening: Mandatory (OFAC, UN, EU)

#### **Decision Point**
- [ ] Confirm if international payments are required for Phase 2
- [ ] If NO: Mark ticket as "Won't Do" and defer to Phase 4
- [ ] If YES: Implement SWIFT client

#### **Estimated Time**: 1 week (5 working days) - IF REQUIRED

---

## **EPIC 3: South African Clearing System Compliance**

### **[PE-310] Implement SAMOS Operating Hours Validation**

**Epic**: SA Compliance  
**Priority**: P1 - Critical  
**Story Points**: 3  
**Assignee**: Backend Developer  
**Sprint**: Sprint 3  

#### **Description**
Implement operating hours validation for SAMOS RTGS to prevent payment submission outside business hours.

#### **SARB Requirements**
Per docs/06-SOUTH-AFRICA-CLEARING.md:
- **Operating Hours**: 08:00 - 15:30 CAT (Central Africa Time)
- **Operating Days**: Monday - Friday (excluding public holidays)
- **Timezone**: Africa/Johannesburg (CAT = UTC+2)

#### **Implementation**
```java
@Service
@Slf4j
public class SamosOperatingHoursService {
    
    private static final LocalTime SAMOS_OPEN = LocalTime.of(8, 0);   // 08:00
    private static final LocalTime SAMOS_CLOSE = LocalTime.of(15, 30); // 15:30
    private static final ZoneId SOUTH_AFRICA_TIMEZONE = ZoneId.of("Africa/Johannesburg");
    
    @Autowired
    private SouthAfricanPublicHolidayService holidayService;
    
    /**
     * Check if SAMOS is currently operating
     */
    public boolean isOperating() {
        ZonedDateTime now = ZonedDateTime.now(SOUTH_AFRICA_TIMEZONE);
        return isOperatingAt(now);
    }
    
    /**
     * Check if SAMOS is operating at specific time
     */
    public boolean isOperatingAt(ZonedDateTime dateTime) {
        // Check if weekend
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        
        // Check if public holiday
        LocalDate date = dateTime.toLocalDate();
        if (holidayService.isPublicHoliday(date)) {
            return false;
        }
        
        // Check operating hours
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(SAMOS_OPEN) && !time.isAfter(SAMOS_CLOSE);
    }
    
    /**
     * Validate payment can be submitted
     */
    public void validateOperatingHours() {
        if (!isOperating()) {
            ZonedDateTime now = ZonedDateTime.now(SOUTH_AFRICA_TIMEZONE);
            
            String reason = getClosureReason(now);
            
            throw new SamosNotOperatingException(
                String.format(
                    "SAMOS is not operating: %s. Operating hours: Mon-Fri 08:00-15:30 CAT",
                    reason
                )
            );
        }
    }
    
    /**
     * Get next operating time
     */
    public ZonedDateTime getNextOperatingTime() {
        ZonedDateTime now = ZonedDateTime.now(SOUTH_AFRICA_TIMEZONE);
        ZonedDateTime next = now;
        
        // If after close, move to next day at open
        if (now.toLocalTime().isAfter(SAMOS_CLOSE)) {
            next = now.plusDays(1)
                .withHour(SAMOS_OPEN.getHour())
                .withMinute(SAMOS_OPEN.getMinute())
                .withSecond(0)
                .withNano(0);
        }
        
        // Skip weekends and holidays
        while (!isOperatingAt(next)) {
            next = next.plusDays(1);
        }
        
        return next;
    }
    
    /**
     * Get time until next operating window
     */
    public Duration getTimeUntilNextOperating() {
        ZonedDateTime now = ZonedDateTime.now(SOUTH_AFRICA_TIMEZONE);
        ZonedDateTime next = getNextOperatingTime();
        return Duration.between(now, next);
    }
    
    private String getClosureReason(ZonedDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return "Weekend";
        }
        
        LocalDate date = dateTime.toLocalDate();
        if (holidayService.isPublicHoliday(date)) {
            return "Public Holiday: " + holidayService.getHolidayName(date);
        }
        
        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(SAMOS_OPEN)) {
            return "Before operating hours (opens 08:00 CAT)";
        } else if (time.isAfter(SAMOS_CLOSE)) {
            return "After operating hours (closes 15:30 CAT)";
        }
        
        return "Unknown";
    }
}
```

#### **Public Holiday Service**
```java
@Service
public class SouthAfricanPublicHolidayService {
    
    // South African public holidays 2025
    private static final Map<LocalDate, String> PUBLIC_HOLIDAYS_2025 = Map.ofEntries(
        Map.entry(LocalDate.of(2025, 1, 1), "New Year's Day"),
        Map.entry(LocalDate.of(2025, 3, 21), "Human Rights Day"),
        Map.entry(LocalDate.of(2025, 4, 18), "Good Friday"),
        Map.entry(LocalDate.of(2025, 4, 21), "Family Day"),
        Map.entry(LocalDate.of(2025, 4, 27), "Freedom Day"),
        Map.entry(LocalDate.of(2025, 5, 1), "Workers' Day"),
        Map.entry(LocalDate.of(2025, 6, 16), "Youth Day"),
        Map.entry(LocalDate.of(2025, 8, 9), "National Women's Day"),
        Map.entry(LocalDate.of(2025, 9, 24), "Heritage Day"),
        Map.entry(LocalDate.of(2025, 12, 16), "Day of Reconciliation"),
        Map.entry(LocalDate.of(2025, 12, 25), "Christmas Day"),
        Map.entry(LocalDate.of(2025, 12, 26), "Day of Goodwill")
    );
    
    public boolean isPublicHoliday(LocalDate date) {
        return PUBLIC_HOLIDAYS_2025.containsKey(date);
    }
    
    public String getHolidayName(LocalDate date) {
        return PUBLIC_HOLIDAYS_2025.get(date);
    }
    
    public List<LocalDate> getPublicHolidays(int year) {
        // TODO: Implement dynamic holiday calculation for future years
        return new ArrayList<>(PUBLIC_HOLIDAYS_2025.keySet());
    }
}
```

#### **Controller Integration**
```java
@RestController
@RequestMapping("/api/v1/samos/operating-hours")
public class SamosOperatingHoursController {
    
    @Autowired
    private SamosOperatingHoursService operatingHoursService;
    
    @GetMapping("/status")
    public OperatingHoursStatus getStatus() {
        boolean isOperating = operatingHoursService.isOperating();
        
        return OperatingHoursStatus.builder()
            .isOperating(isOperating)
            .currentTime(ZonedDateTime.now(ZoneId.of("Africa/Johannesburg")))
            .operatingHours("08:00 - 15:30 CAT (Mon-Fri)")
            .nextOperatingTime(
                isOperating ? null : operatingHoursService.getNextOperatingTime()
            )
            .timeUntilNextOperating(
                isOperating ? null : operatingHoursService.getTimeUntilNextOperating()
            )
            .build();
    }
    
    @GetMapping("/validate")
    public ResponseEntity<ValidationResult> validateOperatingHours() {
        try {
            operatingHoursService.validateOperatingHours();
            return ResponseEntity.ok(ValidationResult.valid());
        } catch (SamosNotOperatingException e) {
            return ResponseEntity.ok(
                ValidationResult.invalid(e.getMessage())
            );
        }
    }
}
```

#### **Integration with Payment Service**
```java
@Service
public class SamosPaymentService {
    
    @Autowired
    private SamosOperatingHoursService operatingHoursService;
    
    public SamosPaymentResult submitPayment(SamosPaymentRequest request) {
        // STEP 1: Validate operating hours
        operatingHoursService.validateOperatingHours();
        
        // STEP 2: Continue with payment submission...
    }
}
```

#### **Configuration Properties**
```yaml
samos:
  operating-hours:
    start-time: "08:00"
    end-time: "15:30"
    timezone: "Africa/Johannesburg"
    operating-days:
      - MONDAY
      - TUESDAY
      - WEDNESDAY
      - THURSDAY
      - FRIDAY
```

#### **Acceptance Criteria**
- [ ] `SamosOperatingHoursService` implemented
- [ ] `SouthAfricanPublicHolidayService` implemented
- [ ] Operating hours validation added to payment submission
- [ ] Weekend validation working
- [ ] Public holiday validation working
- [ ] Timezone handling correct (Africa/Johannesburg)
- [ ] Operating hours API endpoints created
- [ ] Error messages user-friendly
- [ ] Metrics for rejected payments added
- [ ] All unit tests pass
- [ ] Integration tests pass

#### **Testing Requirements**
- [ ] Test during operating hours
- [ ] Test before opening (07:00 CAT)
- [ ] Test after closing (16:00 CAT)
- [ ] Test on weekend
- [ ] Test on public holiday
- [ ] Test timezone handling
- [ ] Test next operating time calculation

#### **Estimated Time**: 3 days

---

### **[PE-311] Implement BankservAfrica Batch Window Validation**

**Epic**: SA Compliance  
**Priority**: P1 - Critical  
**Story Points**: 3  
**Assignee**: Backend Developer  
**Sprint**: Sprint 3  

#### **Description**
Implement batch processing window validation for BankservAfrica ACH/EFT submissions.

#### **BankservAfrica Requirements**
Per docs/06-SOUTH-AFRICA-CLEARING.md:
- **Batch Windows**: 08:00, 10:00, 12:00, 14:00 CAT
- **Processing Hours**: 08:00 - 16:00 CAT (Mon-Fri)
- **Settlement**: T+1 (next business day)

#### **Implementation**
```java
@Service
@Slf4j
public class BankservAfricaBatchWindowService {
    
    private static final LocalTime WINDOW_START = LocalTime.of(8, 0);  // 08:00
    private static final LocalTime WINDOW_END = LocalTime.of(16, 0);   // 16:00
    private static final ZoneId SOUTH_AFRICA_TIMEZONE = ZoneId.of("Africa/Johannesburg");
    
    private static final List<LocalTime> BATCH_CUTOFF_TIMES = List.of(
        LocalTime.of(8, 0),   // 08:00
        LocalTime.of(10, 0),  // 10:00
        LocalTime.of(12, 0),  // 12:00
        LocalTime.of(14, 0)   // 14:00
    );
    
    @Autowired
    private SouthAfricanPublicHolidayService holidayService;
    
    /**
     * Get next available batch cutoff time
     */
    public ZonedDateTime getNextBatchCutoff() {
        ZonedDateTime now = ZonedDateTime.now(SOUTH_AFRICA_TIMEZONE);
        LocalTime currentTime = now.toLocalTime();
        
        // Find next cutoff today
        for (LocalTime cutoff : BATCH_CUTOFF_TIMES) {
            if (currentTime.isBefore(cutoff)) {
                return now.with(cutoff);
            }
        }
        
        // All cutoffs passed today, return first cutoff tomorrow
        return now.plusDays(1)
            .with(BATCH_CUTOFF_TIMES.get(0))
            .withSecond(0)
            .withNano(0);
    }
    
    /**
     * Calculate settlement date (T+1)
     */
    public LocalDate calculateSettlementDate(ZonedDateTime submissionTime) {
        LocalDate submissionDate = submissionTime.toLocalDate();
        LocalDate settlementDate = submissionDate.plusDays(1);
        
        // Skip weekends and holidays
        while (isNonBusinessDay(settlementDate)) {
            settlementDate = settlementDate.plusDays(1);
        }
        
        return settlementDate;
    }
    
    /**
     * Validate batch can be submitted
     */
    public BatchWindowValidationResult validateBatchWindow() {
        ZonedDateTime now = ZonedDateTime.now(SOUTH_AFRICA_TIMEZONE);
        
        // Check if business day
        if (isNonBusinessDay(now.toLocalDate())) {
            return BatchWindowValidationResult.invalid(
                "Cannot submit batch on non-business day"
            );
        }
        
        // Check if within processing hours
        LocalTime currentTime = now.toLocalTime();
        if (currentTime.isBefore(WINDOW_START) || currentTime.isAfter(WINDOW_END)) {
            return BatchWindowValidationResult.invalid(
                String.format(
                    "Batch processing hours: 08:00-16:00 CAT. Current time: %s",
                    currentTime
                )
            );
        }
        
        ZonedDateTime nextCutoff = getNextBatchCutoff();
        LocalDate settlementDate = calculateSettlementDate(nextCutoff);
        
        return BatchWindowValidationResult.valid(nextCutoff, settlementDate);
    }
    
    private boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || 
               dayOfWeek == DayOfWeek.SUNDAY || 
               holidayService.isPublicHoliday(date);
    }
}

@Data
@Builder
public class BatchWindowValidationResult {
    private boolean valid;
    private String message;
    private ZonedDateTime nextCutoffTime;
    private LocalDate settlementDate;
    private Duration timeUntilCutoff;
    
    public static BatchWindowValidationResult valid(
        ZonedDateTime nextCutoff,
        LocalDate settlementDate
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Johannesburg"));
        
        return BatchWindowValidationResult.builder()
            .valid(true)
            .message("Batch window open")
            .nextCutoffTime(nextCutoff)
            .settlementDate(settlementDate)
            .timeUntilCutoff(Duration.between(now, nextCutoff))
            .build();
    }
    
    public static BatchWindowValidationResult invalid(String message) {
        return BatchWindowValidationResult.builder()
            .valid(false)
            .message(message)
            .build();
    }
}
```

#### **Acceptance Criteria**
- [ ] Batch window validation implemented
- [ ] Cutoff times (08:00, 10:00, 12:00, 14:00) enforced
- [ ] Processing hours (08:00-16:00) enforced
- [ ] Settlement date calculation (T+1) working
- [ ] Weekend/holiday handling working
- [ ] Validation added to batch submission
- [ ] API endpoints created
- [ ] All tests pass

#### **Testing Requirements**
- [ ] Test before first cutoff (07:00)
- [ ] Test between cutoffs (09:00, 11:00, 13:00)
- [ ] Test after last cutoff (15:00)
- [ ] Test settlement date calculation
- [ ] Test weekend handling
- [ ] Test holiday handling

#### **Estimated Time**: 3 days

---

### **[PE-312] Implement PayShap R3,000 Transaction Limit**

**Epic**: SA Compliance  
**Priority**: P1 - Critical  
**Story Points**: 2  
**Assignee**: Backend Developer  
**Sprint**: Sprint 3  

#### **Description**
Implement R3,000 per-transaction limit enforcement for PayShap instant payments.

#### **PayShap Requirements**
Per docs/26-PAYSHAP-INTEGRATION.md:
- **Transaction Limit**: R3,000 per transaction
- **Currency**: ZAR only
- **Enforcement**: Pre-submission validation

#### **Implementation**
```java
@Service
@Slf4j
public class PayShapLimitValidationService {
    
    private static final BigDecimal PAYSHAP_TRANSACTION_LIMIT = new BigDecimal("3000.00");
    private static final String PAYSHAP_CURRENCY = "ZAR";
    
    /**
     * Validate payment amount against PayShap limits
     */
    public void validateAmount(BigDecimal amount, String currency) {
        // Validate currency
        if (!PAYSHAP_CURRENCY.equals(currency)) {
            throw new PayShapInvalidCurrencyException(
                String.format(
                    "PayShap only supports ZAR currency. Provided: %s",
                    currency
                )
            );
        }
        
        // Validate amount is positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PayShapInvalidAmountException(
                "Payment amount must be positive"
            );
        }
        
        // Validate against transaction limit
        if (amount.compareTo(PAYSHAP_TRANSACTION_LIMIT) > 0) {
            throw new PayShapLimitExceededException(
                String.format(
                    "PayShap transaction limit exceeded. Limit: R%s, Requested: R%s",
                    PAYSHAP_TRANSACTION_LIMIT,
                    amount
                )
            );
        }
        
        log.debug("PayShap amount validation passed: R{}", amount);
    }
    
    /**
     * Get available limit
     */
    public BigDecimal getTransactionLimit() {
        return PAYSHAP_TRANSACTION_LIMIT;
    }
    
    /**
     * Check if amount is within limit
     */
    public boolean isWithinLimit(BigDecimal amount) {
        return amount.compareTo(PAYSHAP_TRANSACTION_LIMIT) <= 0;
    }
}
```

#### **Integration with Payment Service**
```java
@Service
public class PayShapPaymentProcessingService {
    
    @Autowired
    private PayShapLimitValidationService limitValidationService;
    
    public PayShapPaymentResult processPayment(PayShapPaymentRequest request) {
        // STEP 1: Validate amount limit
        limitValidationService.validateAmount(
            request.getAmount(),
            request.getCurrency()
        );
        
        // STEP 2: Continue with payment processing...
    }
}
```

#### **Acceptance Criteria**
- [ ] R3,000 limit enforcement implemented
- [ ] Currency validation (ZAR only) implemented
- [ ] Positive amount validation implemented
- [ ] Clear error messages for limit exceeded
- [ ] Validation added to payment processing
- [ ] API endpoint for limit info created
- [ ] Metrics for rejected payments added
- [ ] All tests pass

#### **Testing Requirements**
- [ ] Test amount = R3,000 (boundary - should pass)
- [ ] Test amount = R3,000.01 (should fail)
- [ ] Test amount = R5,000 (should fail)
- [ ] Test negative amount (should fail)
- [ ] Test zero amount (should fail)
- [ ] Test non-ZAR currency (should fail)

#### **Estimated Time**: 2 days

---

### **[PE-313] Add SARB Settlement Window Enforcement**

**Epic**: SA Compliance  
**Priority**: P1 - Critical  
**Story Points**: 3  
**Assignee**: Backend Developer  
**Sprint**: Sprint 3  

#### **Description**
Enforce SARB settlement window for SAMOS RTGS payments to prevent late submissions.

#### **Technical Requirements**
- No retries for RTGS (already configured)
- Settlement window: Immediate
- Timeout: 30 seconds (already configured)
- Add settlement tracking and confirmation

#### **Implementation**
```java
@Service
@Slf4j
public class SamosSettlementWindowService {
    
    private static final Duration SETTLEMENT_TIMEOUT = Duration.ofSeconds(30);
    
    @Autowired
    private SamosSettlementAccountService settlementAccountService;
    
    /**
     * Monitor settlement confirmation
     */
    @Transactional
    public void awaitSettlementConfirmation(String uetr, Duration timeout) {
        Instant startTime = Instant.now();
        Instant deadline = startTime.plus(timeout);
        
        while (Instant.now().isBefore(deadline)) {
            // Check if settlement confirmed
            Optional<SamosSettlementTransaction> transaction = 
                settlementTransactionRepository.findByUetr(UUID.fromString(uetr));
            
            if (transaction.isPresent() && 
                "SETTLED".equals(transaction.get().getStatus())) {
                log.info("Settlement confirmed for UETR: {}", uetr);
                return;
            }
            
            // Wait 1 second before next check
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SettlementTimeoutException(
                    "Settlement confirmation interrupted"
                );
            }
        }
        
        // Timeout reached
        throw new SettlementTimeoutException(
            String.format(
                "Settlement confirmation timeout after %s seconds for UETR: %s",
                timeout.getSeconds(),
                uetr
            )
        );
    }
}
```

#### **Acceptance Criteria**
- [ ] Settlement window tracking implemented
- [ ] 30-second timeout enforced
- [ ] Settlement confirmation processing added
- [ ] Timeout handling implemented
- [ ] Metrics added
- [ ] All tests pass

#### **Estimated Time**: 3 days

---

Continue in next message due to length limits...

Would you like me to continue with the remaining tickets (PE-314 through PE-325)?
