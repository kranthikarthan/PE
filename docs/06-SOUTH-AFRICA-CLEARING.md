# South African Clearing Systems Integration Guide

## Overview
This document provides detailed information about integrating with South African payment clearing systems, including technical specifications, message formats, and operational procedures.

---

## South African Payment Landscape

### Key Clearing Systems

1. **SAMOS** (South African Multiple Option Settlement)
   - **Owner**: South African Reserve Bank (SARB)
   - **Purpose**: Real-Time Gross Settlement (RTGS)
   - **Use Case**: High-value, time-critical payments

2. **BankservAfrica**
   - **Owner**: BankservAfrica (Payments Association of South Africa)
   - **Purpose**: Retail payment clearing
   - **Services**: ACH, EFT, DebiCheck, RTC

3. **SASWITCH**
   - **Owner**: BankservAfrica
   - **Purpose**: Card payment switching
   - **Use Case**: ATM, POS, card-based transactions

4. **RTC** (Real-Time Clearing)
   - **Owner**: BankservAfrica
   - **Purpose**: Instant retail payments
   - **Use Case**: Person-to-person, merchant payments

---

## 1. SAMOS Integration

### Overview
SAMOS is South Africa's Real-Time Gross Settlement (RTGS) system operated by the South African Reserve Bank.

### Technical Specifications

| Attribute | Value |
|-----------|-------|
| **Protocol** | SWIFT (FIN/InterAct) |
| **Message Format** | ISO 20022 XML |
| **Operating Hours** | 08:00 - 15:30 CAT (Monday-Friday, excluding public holidays) |
| **Settlement** | Real-time, transaction-by-transaction |
| **Payment Limit** | Typically > R5 million (recommended) |
| **Settlement Window** | Immediate |
| **Availability** | 99.9% uptime SLA |

### Message Types

#### Outgoing Messages (Bank → SARB)

**pacs.008.001.08 - Customer Credit Transfer**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>PAY-2025-XXXXXX</MsgId>
      <CreDtTm>2025-10-11T10:30:00Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <TtlIntrBkSttlmAmt Ccy="ZAR">10000000.00</TtlIntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-11</IntrBkSttlmDt>
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
      <InstgAgt>
        <FinInstnId>
          <BICFI>FIRNZAJJ</BICFI>  <!-- Example BIC -->
        </FinInstnId>
      </InstgAgt>
      <InstdAgt>
        <FinInstnId>
          <BICFI>ABSA ZAJJ</BICFI>  <!-- Destination bank BIC -->
        </FinInstnId>
      </InstdAgt>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <InstrId>PAY-2025-XXXXXX</InstrId>
        <EndToEndId>PAY-2025-XXXXXX-E2E</EndToEndId>
        <TxId>TXN-2025-XXXXXX</TxId>
      </PmtId>
      <IntrBkSttlmAmt Ccy="ZAR">10000000.00</IntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-11</IntrBkSttlmDt>
      <ChrgBr>SLEV</ChrgBr>
      <Dbtr>
        <Nm>John Doe</Nm>
        <PstlAdr>
          <Ctry>ZA</Ctry>
        </PstlAdr>
      </Dbtr>
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>1234567890</Id>
          </Othr>
        </Id>
      </DbtrAcct>
      <DbtrAgt>
        <FinInstnId>
          <BICFI>FIRN ZAJJ</BICFI>
        </FinInstnId>
      </DbtrAgt>
      <CdtrAgt>
        <FinInstnId>
          <BICFI>ABSA ZAJJ</BICFI>
        </FinInstnId>
      </CdtrAgt>
      <Cdtr>
        <Nm>Jane Smith</Nm>
        <PstlAdr>
          <Ctry>ZA</Ctry>
        </PstlAdr>
      </Cdtr>
      <CdtrAcct>
        <Id>
          <Othr>
            <Id>0987654321</Id>
          </Othr>
        </Id>
      </CdtrAcct>
      <RmtInf>
        <Ustrd>Payment for invoice #12345</Ustrd>
      </RmtInf>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

#### Incoming Messages (SARB → Bank)

**pacs.002.001.10 - Payment Status Report**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
  <FIToFIPmtStsRpt>
    <GrpHdr>
      <MsgId>SARB-STATUS-XXXXXX</MsgId>
      <CreDtTm>2025-10-11T10:30:05Z</CreDtTm>
    </GrpHdr>
    <TxInfAndSts>
      <OrgnlInstrId>PAY-2025-XXXXXX</OrgnlInstrId>
      <OrgnlEndToEndId>PAY-2025-XXXXXX-E2E</OrgnlEndToEndId>
      <OrgnlTxId>TXN-2025-XXXXXX</OrgnlTxId>
      <TxSts>ACSC</TxSts>  <!-- Accepted Settlement Completed -->
      <StsRsnInf>
        <Rsn>
          <Cd>AC01</Cd>  <!-- Success code -->
        </Rsn>
      </StsRsnInf>
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

### Status Codes

| Code | Description | Action |
|------|-------------|--------|
| ACSC | Accepted Settlement Completed | Payment successful |
| ACSP | Accepted Settlement in Process | Payment processing |
| RJCT | Rejected | Payment failed |
| PDNG | Pending | Awaiting settlement |

### Rejection Reasons

| Code | Description | Resolution |
|------|-------------|------------|
| AC01 | Incorrect account number | Verify account details |
| AC04 | Closed account | Contact beneficiary |
| AC06 | Blocked account | Contact beneficiary bank |
| AM05 | Duplication | Check for duplicate submission |
| BE01 | Inconsistent debtor/creditor details | Verify all details |
| RR01 | Missing debtor account | Provide complete information |
| RR04 | Regulatory reason | Check compliance |

### Integration Steps

1. **SWIFT Connectivity**
   ```
   Option 1: Direct SWIFT connection (requires SWIFT membership)
   Option 2: SWIFT Service Bureau (via BankservAfrica or other providers)
   ```

2. **Testing Environment**
   - SARB provides a test environment for integration
   - Test BIC codes and accounts available
   - Sandbox environment for ISO 20022 message testing

3. **Certification Process**
   - Message format validation
   - Connectivity testing
   - End-to-end transaction testing
   - Security and compliance review
   - SARB approval required

4. **Operational Procedures**
   - Daily reconciliation with SARB
   - Settlement account monitoring
   - Liquidity management (ensure sufficient funds in settlement account)
   - Queue management during high-volume periods

### Error Handling

```java
@Service
public class SamosAdapter {
    
    public void submitPayment(PaymentRequest request) {
        try {
            // Generate ISO 20022 message
            Pacs008 message = buildPacs008(request);
            
            // Send via SWIFT
            SwiftResponse response = swiftClient.send(message);
            
            // Handle acknowledgment
            if (response.isAcknowledged()) {
                publishEvent(new ClearingSubmittedEvent(request.getPaymentId()));
                
                // Wait for settlement confirmation (async)
                waitForSettlement(request.getPaymentId(), Duration.ofMinutes(5));
            } else {
                handleRejection(request, response.getRejectionReason());
            }
            
        } catch (SwiftException e) {
            // Connection error - retry
            retryWithBackoff(request, e);
        } catch (ValidationException e) {
            // Message format error - reject payment
            rejectPayment(request, "Invalid message format");
        }
    }
    
    private void handleRejection(PaymentRequest request, String reason) {
        // Log rejection
        log.error("SAMOS rejected payment {}: {}", request.getPaymentId(), reason);
        
        // Update transaction status
        transactionService.updateStatus(request.getPaymentId(), "FAILED", reason);
        
        // Publish failure event
        publishEvent(new PaymentFailedEvent(request.getPaymentId(), reason));
        
        // Trigger saga compensation
        sagaOrchestrator.compensate(request.getSagaId());
    }
}
```

### Fees

- **SAMOS Transaction Fee**: ~R30-50 per transaction
- **SWIFT Message Fee**: ~$0.50 per message
- **Monthly SWIFT Connectivity**: ~$500-1000

---

## 2. BankservAfrica Integration

### Overview
BankservAfrica operates multiple payment streams for retail payments.

### 2.1 ACH/EFT (Batch Processing)

#### Technical Specifications

| Attribute | Value |
|-----------|-------|
| **Protocol** | TCP/IP, SFTP |
| **Message Format** | Proprietary (BankservAfrica format) |
| **Operating Hours** | 24/7 (batch cutoffs at specific times) |
| **Batch Cutoffs** | 08:00, 10:00, 12:00, 14:00 CAT |
| **Settlement** | T+1 (next business day) |
| **Payment Limit** | No limit, but typically < R5 million |
| **File Format** | Fixed-width text file |

#### Batch File Format

**Header Record (Type 01)**
```
01SENDER_BANK_CODE20251011BATCH_REFERENCE  PADDING...
```

**Transaction Record (Type 02)**
```
02DEBIT_ACCOUNT CREDIT_ACCOUNT0000100000ZARPAY-2025-XXXXXXPayment for invoice...
```

**Trailer Record (Type 99)**
```
990000100000000100000PADDING...
```

#### Example Batch File
```
01ABSA     20251011BATCH-001          
020123456789098765432100001000000ZARPAY-2025-000001Payment for Invoice #001
020123456789012345678900000500000ZARPAY-2025-000002Salary payment      
9900001500000000000002
```

**Field Specifications**:
- Record Type: 2 characters
- Debit Account: 10 characters
- Credit Account: 10 characters
- Amount: 13 characters (11 digits + 2 decimals, no decimal point)
- Currency: 3 characters
- Reference: 20 characters
- Description: 30 characters

### 2.2 RTC (Real-Time Clearing)

#### Technical Specifications

| Attribute | Value |
|-----------|-------|
| **Protocol** | REST API (HTTPS) |
| **Message Format** | ISO 20022 XML (pacs.008) |
| **Operating Hours** | 24/7/365 |
| **Settlement** | Real-time (T+0) |
| **Payment Limit** | R5 million per transaction |
| **Response Time** | < 10 seconds (95th percentile) |
| **Availability** | 99.9% uptime SLA |

#### API Endpoints

**Base URL**: `https://api.rtc.bankserv.co.za/v1`

**Authentication**: OAuth 2.0 Client Credentials

```http
POST /auth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=YOUR_CLIENT_ID
&client_secret=YOUR_CLIENT_SECRET
&scope=rtc:payments:write
```

**Response**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1Ni...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

**Submit Payment**:
```http
POST /payments
Authorization: Bearer {access_token}
Content-Type: application/xml
X-Request-ID: {unique-request-id}
X-Idempotency-Key: {idempotency-key}

<ISO 20022 pacs.008 message>
```

**Response (Success)**:
```json
{
  "paymentId": "RTC-2025-XXXXXX",
  "status": "ACCEPTED",
  "transactionReference": "TXN-2025-XXXXXX",
  "timestamp": "2025-10-11T10:30:00Z",
  "estimatedSettlementTime": "2025-10-11T10:30:10Z"
}
```

**Response (Failure)**:
```json
{
  "paymentId": "RTC-2025-XXXXXX",
  "status": "REJECTED",
  "errorCode": "AC01",
  "errorMessage": "Invalid account number",
  "timestamp": "2025-10-11T10:30:00Z"
}
```

**Get Payment Status**:
```http
GET /payments/{paymentId}
Authorization: Bearer {access_token}
```

#### ISO 20022 Message (RTC)
Same structure as SAMOS pacs.008, but with RTC-specific elements:
- Faster processing expected
- Immediate settlement
- Enhanced debtor/creditor information

### 2.3 DebiCheck (Debit Order Authentication)

#### Overview
DebiCheck is an electronic debit order system with enhanced security and authentication.

#### Technical Specifications

| Attribute | Value |
|-----------|-------|
| **Protocol** | USSD, Mobile App, Web |
| **Message Format** | Proprietary |
| **Authentication** | Consumer authenticates mandate |
| **Use Case** | Recurring debit orders |
| **Processing** | T+1 |

#### Integration Flow

1. **Mandate Creation**
   ```java
   MandateRequest mandate = MandateRequest.builder()
       .creditorAccount("1234567890")
       .debtorAccount("0987654321")
       .maxAmount(new BigDecimal("5000.00"))
       .frequency("MONTHLY")
       .startDate(LocalDate.now())
       .endDate(LocalDate.now().plusYears(1))
       .build();
   
   MandateResponse response = debicheckClient.createMandate(mandate);
   ```

2. **Consumer Authentication**
   - Consumer receives USSD push or notification
   - Consumer reviews mandate details
   - Consumer accepts or rejects mandate
   - BankservAfrica notifies creditor of outcome

3. **Debit Order Processing**
   ```java
   DebitOrderRequest debitOrder = DebitOrderRequest.builder()
       .mandateReference(mandate.getMandateReference())
       .amount(new BigDecimal("1000.00"))
       .collectionDate(LocalDate.now().plusDays(3))
       .build();
   
   DebitOrderResponse response = debicheckClient.submitDebitOrder(debitOrder);
   ```

### Integration Code Examples

#### ACH/EFT Batch Submission

```java
@Service
public class BankservAchAdapter {
    
    public void submitBatch(List<PaymentRequest> payments) {
        // Create batch file
        String batchFile = generateBatchFile(payments);
        
        // Upload via SFTP
        sftpClient.upload(batchFile, "/incoming/batch-" + LocalDate.now() + ".txt");
        
        // Log submission
        log.info("Submitted batch with {} transactions", payments.size());
        
        // Wait for acknowledgment file (async)
        scheduleAcknowledgmentCheck(batchFile);
    }
    
    private String generateBatchFile(List<PaymentRequest> payments) {
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append(String.format("01%-10s%s%-20s%s\n",
            "BANK001",
            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
            generateBatchReference(),
            " ".repeat(100)
        ));
        
        // Transactions
        for (PaymentRequest payment : payments) {
            sb.append(String.format("02%-10s%-10s%013d%s%-20s%-30s\n",
                payment.getSourceAccount(),
                payment.getDestinationAccount(),
                payment.getAmount().multiply(new BigDecimal(100)).longValue(),
                payment.getCurrency(),
                payment.getPaymentId(),
                truncate(payment.getReference(), 30)
            ));
        }
        
        // Trailer
        long totalAmount = payments.stream()
            .map(PaymentRequest::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .multiply(new BigDecimal(100))
            .longValue();
        
        sb.append(String.format("99%013d%010d%s\n",
            totalAmount,
            payments.size(),
            " ".repeat(100)
        ));
        
        return sb.toString();
    }
}
```

#### RTC API Integration

```java
@Service
public class BankservRtcAdapter {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${rtc.api.url}")
    private String rtcApiUrl;
    
    public PaymentResponse submitPayment(PaymentRequest payment) {
        try {
            // Get OAuth token
            String token = authenticate();
            
            // Generate ISO 20022 message
            String iso20022Message = generatePacs008(payment);
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.set("X-Request-ID", UUID.randomUUID().toString());
            headers.set("X-Idempotency-Key", payment.getIdempotencyKey());
            
            // Submit payment
            HttpEntity<String> request = new HttpEntity<>(iso20022Message, headers);
            ResponseEntity<RtcResponse> response = restTemplate.postForEntity(
                rtcApiUrl + "/payments",
                request,
                RtcResponse.class
            );
            
            // Handle response
            if (response.getStatusCode().is2xxSuccessful()) {
                RtcResponse rtcResponse = response.getBody();
                publishEvent(new ClearingSubmittedEvent(payment.getPaymentId(), rtcResponse.getPaymentId()));
                return PaymentResponse.success(rtcResponse);
            } else {
                throw new RtcException("Payment submission failed: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            log.error("RTC API error", e);
            throw new RtcException("Failed to submit payment to RTC", e);
        }
    }
    
    private String authenticate() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "rtc:payments:write");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
            rtcApiUrl + "/auth/token",
            request,
            TokenResponse.class
        );
        
        return response.getBody().getAccessToken();
    }
}
```

### Fees

- **ACH/EFT Transaction Fee**: R1-3 per transaction
- **RTC Transaction Fee**: R3-5 per transaction
- **DebiCheck Mandate Fee**: R5 per mandate
- **Monthly Connectivity**: R5,000-10,000

---

## 3. SASWITCH Integration

### Overview
SASWITCH is the national card switch for South Africa, routing card transactions between banks and card schemes.

### Technical Specifications

| Attribute | Value |
|-----------|-------|
| **Protocol** | ISO 8583 over TCP/IP |
| **Message Format** | ISO 8583 (1987/1993/2003) |
| **Operating Hours** | 24/7/365 |
| **Transaction Types** | Authorization, Financial, Reversal, Reconciliation |
| **Response Time** | < 3 seconds |

### ISO 8583 Message Format

#### Authorization Request (0100)

```
0100                            // Message Type Indicator
7020058000C000000000000000000004 // Bitmap
1234567890123456                // Field 2: PAN (16 digits)
000000                          // Field 3: Processing Code
000000100000                    // Field 4: Amount (12 digits)
1011103015                      // Field 7: Transmission Date/Time
000001                          // Field 11: STAN (System Trace Audit Number)
103015                          // Field 12: Local Time
1011                            // Field 13: Local Date
2512                            // Field 14: Expiry Date
6011                            // Field 18: Merchant Type
484                             // Field 22: POS Entry Mode
00                              // Field 25: POS Condition Code
01234567890123456789            // Field 32: Acquiring Institution ID
01234567890123456789            // Field 41: Card Acceptor Terminal ID
Merchant Name          ZA       // Field 43: Card Acceptor Name/Location
```

#### Authorization Response (0110)

```
0110                            // Message Type Indicator
7020058000C000000000000000000004 // Bitmap
00                              // Field 39: Response Code (00 = Approved)
AUTH123456                      // Field 38: Authorization ID
```

### Response Codes

| Code | Description | Action |
|------|-------------|--------|
| 00 | Approved | Transaction successful |
| 01 | Refer to card issuer | Contact issuer |
| 05 | Do not honor | Declined |
| 14 | Invalid card number | Verify PAN |
| 51 | Insufficient funds | Declined |
| 54 | Expired card | Request new card |
| 55 | Incorrect PIN | Retry or decline |
| 91 | Issuer unavailable | Retry later |

### Integration Example

```java
@Service
public class SaswitchAdapter {
    
    public AuthorizationResponse authorizeTransaction(CardTransaction transaction) {
        try {
            // Build ISO 8583 message
            ISOMsg msg = new ISOMsg();
            msg.setMTI("0100");
            msg.set(2, transaction.getPan());
            msg.set(3, "000000");  // Purchase
            msg.set(4, String.format("%012d", transaction.getAmount().multiply(new BigDecimal(100)).longValue()));
            msg.set(7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss")));
            msg.set(11, generateSTAN());
            msg.set(12, LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
            msg.set(13, LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd")));
            msg.set(14, transaction.getExpiryDate());
            msg.set(22, "051");  // Chip card
            msg.set(32, acquiringInstitutionId);
            msg.set(41, transaction.getTerminalId());
            msg.set(42, transaction.getMerchantId());
            
            // Send message
            ISOMsg response = channel.send(msg);
            
            // Parse response
            String responseCode = response.getString(39);
            String authId = response.getString(38);
            
            if ("00".equals(responseCode)) {
                return AuthorizationResponse.approved(authId);
            } else {
                return AuthorizationResponse.declined(responseCode);
            }
            
        } catch (Exception e) {
            log.error("SASWITCH error", e);
            throw new SaswitchException("Transaction failed", e);
        }
    }
}
```

---

## Testing Strategy

### 1. Unit Testing
- Mock clearing system responses
- Test message generation
- Test error handling

### 2. Integration Testing
- Use clearing system test environments
- Test end-to-end flows
- Validate message formats

### 3. Certification Testing
- Complete certification requirements for each system
- Submit test transactions
- Validate with clearing system operators

---

## Compliance & Regulations

### PASA (Payments Association of South Africa)
- Membership required for direct clearing participation
- Compliance with PASA rules and standards
- Regular audits and reporting

### SARB (South African Reserve Bank)
- Authorization required for SAMOS participation
- Compliance with NPS Act
- Capital requirements
- Liquidity management

### Data Residency
- All payment data must remain in South Africa
- No cross-border data transfers without consent
- Compliance with POPIA

---

## Operational Procedures

### Daily Operations

1. **Morning Checks (07:30 CAT)**
   - Verify SAMOS connectivity
   - Check settlement account balance
   - Review overnight batch processing

2. **Batch Processing**
   - Submit batches at cutoff times
   - Monitor acknowledgments
   - Handle rejections

3. **Real-Time Monitoring**
   - Monitor RTC transaction flow
   - Track success/failure rates
   - Alert on anomalies

4. **End-of-Day (16:00 CAT)**
   - Reconcile all transactions
   - Generate settlement reports
   - Archive transaction logs

### Incident Response

1. **Clearing System Downtime**
   - Queue transactions locally
   - Switch to alternate clearing system
   - Notify users of delay

2. **Message Rejection**
   - Investigate rejection reason
   - Correct and resubmit if possible
   - Notify customer if unable to resolve

3. **Settlement Failure**
   - Contact clearing system support
   - Investigate liquidity issues
   - Escalate to management

---

## Monitoring & Alerting

### Key Metrics

1. **Transaction Success Rate**: > 99%
2. **Average Processing Time**: < 5 seconds (RTC)
3. **Batch Acknowledgment Time**: < 30 minutes
4. **Settlement Success Rate**: > 99.9%

### Alerts

- **Critical**: Clearing system unavailable
- **High**: Success rate < 95%
- **Medium**: Processing time > 10 seconds
- **Low**: Batch acknowledgment delayed

---

**Next**: See `07-AZURE-INFRASTRUCTURE.md` for cloud infrastructure design
