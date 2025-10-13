# Clearing System Interaction Flows - Per Payment Rail

## Overview

This document provides a **detailed understanding** of how the Payments Engine interacts with each clearing system in South Africa and internationally. Each payment rail has unique characteristics, message formats, timing, settlement processes, and reconciliation requirements.

**Purpose**: Enable AI agents to understand the complete end-to-end flow for each payment rail when building clearing adapters.

**Clearing Systems Covered**:
1. SAMOS (RTGS) - High-value real-time gross settlement
2. BankservAfrica (EFT/ACH) - Low-value batch processing
3. RTC (Real-Time Clearing) - Real-time low-value payments
4. PayShap - Instant P2P payments (mobile/email proxy)
5. SWIFT - International cross-border payments

---

## Table of Contents

1. [SAMOS (RTGS) Interaction Flow](#1-samos-rtgs-interaction-flow)
2. [BankservAfrica (EFT/ACH) Interaction Flow](#2-bankservafrica-eftach-interaction-flow)
3. [RTC (Real-Time Clearing) Interaction Flow](#3-rtc-real-time-clearing-interaction-flow)
4. [PayShap Interaction Flow](#4-payshap-interaction-flow)
5. [SWIFT Interaction Flow](#5-swift-interaction-flow)
6. [Comparison Matrix](#6-comparison-matrix)
7. [Error Handling Patterns](#7-error-handling-patterns)
8. [Reconciliation Patterns](#8-reconciliation-patterns)

---

## 1. SAMOS (RTGS) Interaction Flow

**System**: South African Multiple Option Settlement  
**Type**: Real-Time Gross Settlement (RTGS)  
**Use Case**: High-value payments (> R 5 million), urgent payments  
**Operator**: South African Reserve Bank (SARB)  
**Settlement**: Real-time, immediate, irrevocable  
**Operating Hours**: 07:00 - 17:00 (South African business days)

### 1.1 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PAYMENTS ENGINE                                 │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Payment Initiation Service                                     │ │
│  │  └─> Validation Service (Amount > R5M, RTGS eligible)          │ │
│  │       └─> Routing Service (Route to SAMOS)                     │ │
│  │            └─> SAMOS Adapter Service                            │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ ISO 20022 (pacs.008)
                                │ Over HTTPS + mTLS
                                │ Digital Signature (XMLDSig)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         SAMOS SYSTEM (SARB)                          │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Message Validation                                             │ │
│  │  └─> Liquidity Check (Sender's Reserve Account)                │ │
│  │       └─> Settlement (Immediate)                                │ │
│  │            └─> Notification (Both parties)                      │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.2 Detailed Interaction Flow

#### Step 1: Payment Initiation (T+0)

```
Time: 08:30:00 (Example)

Payment Engine:
1. Receive payment request from channel
   {
     "amount": 10000000.00,  // R 10 million
     "currency": "ZAR",
     "debitAccount": "62001234567",
     "creditAccount": "62009876543",
     "beneficiaryBank": "632005",
     "reference": "Property Purchase INV-12345",
     "urgency": "URGENT"
   }

2. Validation Service checks:
   ✅ Amount > R 5M → RTGS eligible
   ✅ Within SAMOS hours (07:00 - 17:00)
   ✅ Business day (not weekend/holiday)
   ✅ Liquidity available (via Core Banking API)
   ✅ Limit checks passed
   ✅ Fraud score acceptable

3. Routing Service:
   Decision: Route to SAMOS (high-value + urgent)
   
4. Reserve liquidity:
   - Call Core Banking: Debit R 10M from customer account
   - Hold in nostro account pending SAMOS confirmation
```

#### Step 2: Message Construction (T+0 + 2 seconds)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>BANK001-20251013-000123</MsgId>
      <CreDtTm>2025-10-13T08:30:02</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <TtlIntrBkSttlmAmt Ccy="ZAR">10000000.00</TtlIntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-13</IntrBkSttlmDt>
      <SttlmInf>
        <SttlmMtd>CLRG</SttlmMtd>
        <ClrSys>
          <Cd>SAMOS</Cd>
        </ClrSys>
      </SttlmInf>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <InstrId>BANK001-TXN-000123</InstrId>
        <EndToEndId>PAY-2025-000123</EndToEndId>
        <TxId>SAMOS-2025-000123</TxId>
      </PmtId>
      <IntrBkSttlmAmt Ccy="ZAR">10000000.00</IntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-13</IntrBkSttlmDt>
      <Dbtr>
        <Nm>ABC Corporation (Pty) Ltd</Nm>
        <Id>
          <OrgId>
            <Othr>
              <Id>2005/123456/07</Id>
            </Othr>
          </OrgId>
        </Id>
      </Dbtr>
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>62001234567</Id>
          </Othr>
        </Id>
      </DbtrAcct>
      <DbtrAgt>
        <FinInstnId>
          <ClrSysMmbId>
            <MmbId>632005</MmbId>  <!-- Sending bank SARB code -->
          </ClrSysMmbId>
        </FinInstnId>
      </DbtrAgt>
      <CdtrAgt>
        <FinInstnId>
          <ClrSysMmbId>
            <MmbId>632010</MmbId>  <!-- Receiving bank SARB code -->
          </ClrSysMmbId>
        </FinInstnId>
      </CdtrAgt>
      <Cdtr>
        <Nm>XYZ Properties (Pty) Ltd</Nm>
      </Cdtr>
      <CdtrAcct>
        <Id>
          <Othr>
            <Id>62009876543</Id>
          </Othr>
        </Id>
      </CdtrAcct>
      <Purp>
        <Cd>PROP</Cd>  <!-- Property Purchase -->
      </Purp>
      <RmtInf>
        <Ustrd>Property Purchase INV-12345</Ustrd>
      </RmtInf>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

**Security Layer**:
```xml
<!-- Add XMLDSig digital signature -->
<Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
  <SignedInfo>
    <CanonicalizationMethod Algorithm="..."/>
    <SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/>
    <Reference URI="">
      <DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/>
      <DigestValue>BASE64_HASH</DigestValue>
    </Reference>
  </SignedInfo>
  <SignatureValue>BASE64_SIGNATURE</SignatureValue>
  <KeyInfo>
    <X509Data>
      <X509Certificate>BANK_CERTIFICATE</X509Certificate>
    </X509Data>
  </KeyInfo>
</Signature>
```

#### Step 3: Transmission to SAMOS (T+0 + 3 seconds)

```
Protocol: HTTPS POST with mTLS
Endpoint: https://samos.resbank.co.za/api/v1/payments
Headers:
  Content-Type: application/xml
  X-Message-Type: pacs.008.001.08
  X-Bank-Code: 632005
  X-Message-Id: BANK001-20251013-000123
  Authorization: Bearer <JWT_TOKEN>

Certificate: Client certificate (issued by SARB)
Timeout: 30 seconds
```

**SAMOS Adapter Code** (Java Spring Boot):
```java
@Service
public class SamosAdapter {
    
    @Autowired
    private RestTemplate samosRestTemplate; // Pre-configured with mTLS
    
    @Autowired
    private XmlSignatureService xmlSignatureService;
    
    public SamosResponse sendPayment(PaymentMessage payment) {
        // 1. Build ISO 20022 pacs.008 message
        String xml = buildPacs008Message(payment);
        
        // 2. Sign with XMLDSig
        String signedXml = xmlSignatureService.signXml(xml);
        
        // 3. Send to SAMOS
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set("X-Message-Type", "pacs.008.001.08");
        headers.set("X-Bank-Code", bankCode);
        headers.set("X-Message-Id", payment.getMessageId());
        
        HttpEntity<String> request = new HttpEntity<>(signedXml, headers);
        
        try {
            ResponseEntity<String> response = samosRestTemplate.postForEntity(
                samosEndpoint + "/api/v1/payments",
                request,
                String.class
            );
            
            // 4. Parse response (pacs.002 - Payment Status Report)
            return parsePacs002Response(response.getBody());
            
        } catch (HttpClientErrorException e) {
            // Handle 4xx errors (validation failures)
            throw new SamosValidationException(e.getMessage());
        } catch (HttpServerErrorException e) {
            // Handle 5xx errors (SAMOS system issues)
            throw new SamosSystemException(e.getMessage());
        } catch (ResourceAccessException e) {
            // Handle timeout
            throw new SamosTimeoutException(e.getMessage());
        }
    }
}
```

#### Step 4: SAMOS Processing (T+0 + 5 seconds)

**SAMOS Internal Flow**:
```
1. Message Receipt & Validation (< 1 second)
   ✅ XML schema validation (ISO 20022 pacs.008)
   ✅ Digital signature verification (XMLDSig)
   ✅ Bank authentication (mTLS certificate)
   ✅ Message ID uniqueness (duplicate check)
   ✅ Mandatory fields present
   ✅ Amount format correct (2 decimal places)
   ✅ Bank codes valid (SARB member check)

2. Business Validation (< 1 second)
   ✅ Sender bank has settlement account
   ✅ Receiver bank has settlement account
   ✅ Settlement date is today (RTGS = same day)
   ✅ Within operating hours (07:00 - 17:00)

3. Liquidity Check (< 1 second)
   Query: Sender bank's reserve account balance
   Required: R 10,000,000.00
   Available: R 150,000,000.00 ✅
   
   If insufficient → Reject with error code "NOAS" (Not Sufficient Funds)

4. Settlement (< 2 seconds)
   ⚡ IMMEDIATE and IRREVOCABLE
   
   Reserve Account Movements:
   Dr. Bank 632005 (Sender)     R 10,000,000.00
   Cr. Bank 632010 (Receiver)   R 10,000,000.00
   
   Status: SETTLED (cannot be reversed)

5. Confirmation Generation (< 1 second)
   - Generate settlement reference: SAMOS-2025-1013-00123
   - Timestamp: 2025-10-13T08:30:08
   - Build pacs.002 (Payment Status Report)
```

#### Step 5: SAMOS Response (T+0 + 8 seconds)

**Success Response** (pacs.002 - Payment Status Report):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
  <FIToFIPmtStsRpt>
    <GrpHdr>
      <MsgId>SAMOS-20251013-000123</MsgId>
      <CreDtTm>2025-10-13T08:30:08</CreDtTm>
    </GrpHdr>
    <OrgnlGrpInfAndSts>
      <OrgnlMsgId>BANK001-20251013-000123</OrgnlMsgId>
      <OrgnlMsgNmId>pacs.008.001.08</OrgnlMsgNmId>
      <GrpSts>ACCP</GrpSts>  <!-- Accepted, Settlement Completed -->
    </OrgnlGrpInfAndSts>
    <TxInfAndSts>
      <OrgnlInstrId>BANK001-TXN-000123</OrgnlInstrId>
      <OrgnlEndToEndId>PAY-2025-000123</OrgnlEndToEndId>
      <OrgnlTxId>SAMOS-2025-000123</OrgnlTxId>
      <TxSts>ACSC</TxSts>  <!-- AcceptedSettlementCompleted -->
      <StsRsnInf>
        <Rsn>
          <Cd>SETT</Cd>  <!-- Settled -->
        </Rsn>
      </StsRsnInf>
      <AccptncDtTm>2025-10-13T08:30:08</AccptncDtTm>
      <ClrSysRef>SAMOS-2025-1013-00123</ClrSysRef>  <!-- SAMOS settlement ref -->
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

**HTTP Response**:
```
Status: 200 OK
Content-Type: application/xml

Headers:
  X-SAMOS-Settlement-Ref: SAMOS-2025-1013-00123
  X-Settlement-Timestamp: 2025-10-13T08:30:08Z
  X-Settlement-Status: COMPLETED
```

**Failure Response Example** (Insufficient Funds):
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
  <FIToFIPmtStsRpt>
    <GrpHdr>
      <MsgId>SAMOS-20251013-000124</MsgId>
      <CreDtTm>2025-10-13T08:30:09</CreDtTm>
    </GrpHdr>
    <TxInfAndSts>
      <OrgnlInstrId>BANK001-TXN-000124</OrgnlInstrId>
      <TxSts>RJCT</TxSts>  <!-- Rejected -->
      <StsRsnInf>
        <Rsn>
          <Cd>NOAS</Cd>  <!-- Not Sufficient Funds -->
        </Rsn>
        <AddtlInf>Insufficient funds in settlement account</AddtlInf>
      </StsRsnInf>
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

#### Step 6: Payments Engine Post-Processing (T+0 + 9 seconds)

```java
// SAMOS Adapter receives response
public void processResponse(SamosResponse response) {
    if (response.getStatus() == TransactionStatus.ACSC) {
        // SUCCESS - Settlement completed
        
        // 1. Update payment status
        paymentRepository.updateStatus(
            response.getOriginalEndToEndId(),
            PaymentStatus.SETTLED,
            response.getSettlementTimestamp()
        );
        
        // 2. Store clearing reference
        paymentRepository.storeClearingReference(
            response.getOriginalEndToEndId(),
            response.getClearingSystemReference() // SAMOS-2025-1013-00123
        );
        
        // 3. Publish event
        eventPublisher.publish("payment-settled", PaymentSettledEvent.builder()
            .paymentId(response.getOriginalEndToEndId())
            .clearingSystem("SAMOS")
            .settlementReference(response.getClearingSystemReference())
            .settlementTimestamp(response.getSettlementTimestamp())
            .build()
        );
        
        // 4. Trigger notification
        notificationService.sendSettlementConfirmation(
            response.getOriginalEndToEndId()
        );
        
        // 5. Update account balance (confirm debit)
        // Note: Money already debited, this confirms it
        coreBankingService.confirmDebit(
            payment.getDebitAccount(),
            payment.getAmount(),
            response.getClearingSystemReference()
        );
        
    } else if (response.getStatus() == TransactionStatus.RJCT) {
        // FAILURE - Rejected
        
        // 1. Update payment status
        paymentRepository.updateStatus(
            response.getOriginalEndToEndId(),
            PaymentStatus.FAILED,
            LocalDateTime.now()
        );
        
        // 2. Store rejection reason
        paymentRepository.storeRejectionReason(
            response.getOriginalEndToEndId(),
            response.getReasonCode(), // e.g., "NOAS"
            response.getAdditionalInfo()
        );
        
        // 3. Reverse liquidity reservation
        coreBankingService.reverseDebit(
            payment.getDebitAccount(),
            payment.getAmount(),
            "SAMOS rejection: " + response.getReasonCode()
        );
        
        // 4. Publish event
        eventPublisher.publish("payment-failed", PaymentFailedEvent.builder()
            .paymentId(response.getOriginalEndToEndId())
            .clearingSystem("SAMOS")
            .reasonCode(response.getReasonCode())
            .reasonDescription(response.getAdditionalInfo())
            .build()
        );
        
        // 5. Notify customer
        notificationService.sendPaymentFailureNotification(
            response.getOriginalEndToEndId(),
            response.getReasonCode()
        );
    }
}
```

#### Step 7: Reconciliation (End of Day)

**SAMOS provides**:
- Settlement file (ISO 20022 camt.053 - Bank Statement)
- Contains all settled transactions for the day
- Sent at 17:30 (after close of business)

```xml
<!-- camt.053 - Bank Account Statement -->
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.053.001.08">
  <BkToCstmrStmt>
    <Stmt>
      <Id>SAMOS-STMT-20251013</Id>
      <CreDtTm>2025-10-13T17:30:00</CreDtTm>
      <Acct>
        <Id>
          <Othr>
            <Id>632005-RESERVE</Id>  <!-- Bank's reserve account -->
          </Othr>
        </Id>
      </Acct>
      <Bal>
        <Tp>
          <CdOrPrtry>
            <Cd>OPBD</Cd>  <!-- Opening Balance -->
          </CdOrPrtry>
        </Tp>
        <Amt Ccy="ZAR">150000000.00</Amt>
        <CdtDbtInd>CRDT</CdtDbtInd>
        <Dt>
          <Dt>2025-10-13</Dt>
        </Dt>
      </Bal>
      <Bal>
        <Tp>
          <CdOrPrtry>
            <Cd>CLBD</Cd>  <!-- Closing Balance -->
          </CdOrPrtry>
        </Tp>
        <Amt Ccy="ZAR">140000000.00</Amt>  <!-- R 10M net outflow -->
        <CdtDbtInd>CRDT</CdtDbtInd>
        <Dt>
          <Dt>2025-10-13</Dt>
        </Dt>
      </Bal>
      <Ntry>
        <Amt Ccy="ZAR">10000000.00</Amt>
        <CdtDbtInd>DBIT</CdtDbtInd>  <!-- Debit -->
        <Sts>
          <Cd>BOOK</Cd>  <!-- Booked -->
        </Sts>
        <BookgDt>
          <Dt>2025-10-13</Dt>
        </BookgDt>
        <ValDt>
          <Dt>2025-10-13</Dt>
        </ValDt>
        <BkTxCd>
          <Prtry>
            <Cd>RTGS_PAYMENT</Cd>
          </Prtry>
        </BkTxCd>
        <NtryDtls>
          <TxDtls>
            <Refs>
              <MsgId>BANK001-20251013-000123</MsgId>
              <EndToEndId>PAY-2025-000123</EndToEndId>
              <ClrSysRef>SAMOS-2025-1013-00123</ClrSysRef>
            </Refs>
            <RltdPties>
              <Cdtr>
                <Nm>XYZ Properties (Pty) Ltd</Nm>
              </Cdtr>
              <CdtrAcct>
                <Id>
                  <Othr>
                    <Id>62009876543</Id>
                  </Othr>
                </Id>
              </CdtrAcct>
            </RltdPties>
            <RmtInf>
              <Ustrd>Property Purchase INV-12345</Ustrd>
            </RmtInf>
          </TxDtls>
        </NtryDtls>
      </Ntry>
    </Stmt>
  </BkToCstmrStmt>
</Document>
```

**Reconciliation Service** (Java Spring Boot):
```java
@Service
public class SamosReconciliationService {
    
    @Scheduled(cron = "0 0 18 * * MON-FRI") // 18:00 daily
    public void reconcileSamosTransactions() {
        // 1. Download camt.053 statement from SAMOS
        String statement = samosAdapter.downloadStatement(LocalDate.now());
        
        // 2. Parse statement
        List<StatementEntry> samosEntries = parseCamt053(statement);
        
        // 3. Get our internal records
        List<Payment> ourPayments = paymentRepository.findByDateAndClearingSystem(
            LocalDate.now(),
            "SAMOS"
        );
        
        // 4. Match transactions
        ReconciliationReport report = new ReconciliationReport();
        
        for (StatementEntry entry : samosEntries) {
            Optional<Payment> matchedPayment = ourPayments.stream()
                .filter(p -> p.getEndToEndId().equals(entry.getEndToEndId()))
                .findFirst();
            
            if (matchedPayment.isPresent()) {
                // Matched
                Payment payment = matchedPayment.get();
                
                // Verify amounts match
                if (payment.getAmount().compareTo(entry.getAmount()) == 0) {
                    report.addMatched(payment, entry);
                } else {
                    report.addAmountMismatch(payment, entry);
                }
                
                ourPayments.remove(matchedPayment.get());
            } else {
                // Entry in SAMOS but not in our system (incoming payment)
                report.addUnmatched(entry);
            }
        }
        
        // 5. Remaining in ourPayments are "missing from SAMOS"
        for (Payment payment : ourPayments) {
            report.addMissingFromSamos(payment);
        }
        
        // 6. Store reconciliation report
        reconciliationRepository.save(report);
        
        // 7. Alert if mismatches found
        if (!report.isFullyReconciled()) {
            alertService.sendReconciliationAlert(report);
        }
    }
}
```

### 1.3 SAMOS Timing Characteristics

| Event | Timing | Notes |
|-------|--------|-------|
| Message submission | T+0 + 3s | From payment initiation to SAMOS API call |
| SAMOS validation | < 1 second | XML, signature, business rules |
| Liquidity check | < 1 second | Query reserve account |
| Settlement | < 2 seconds | **Immediate and irrevocable** |
| Confirmation response | T+0 + 8s | Total end-to-end |
| Reconciliation file | 17:30 daily | camt.053 statement |

**Total SLA**: < 10 seconds from submission to confirmation ✅

### 1.4 SAMOS Error Codes

| Code | Description | Action |
|------|-------------|--------|
| ACSC | AcceptedSettlementCompleted | ✅ Success |
| ACCP | Accepted | ✅ Success (intermediate) |
| RJCT | Rejected | ❌ Failure |
| NOAS | No Settlement Account | ❌ Reverse debit, notify |
| AM05 | Duplication | ❌ Duplicate message ID |
| NARR | Narrative (various) | ❌ Check addtlInf |
| FF01 | Format Error | ❌ XML schema invalid |
| BE04 | Debtor Account Closed | ❌ Account issue |
| AC03 | Invalid Creditor Account | ❌ Beneficiary issue |

### 1.5 SAMOS Key Characteristics

✅ **Strengths**:
- Real-time settlement (< 10 seconds)
- Irrevocable (cannot be reversed)
- Guaranteed finality
- SARB-operated (trusted)
- ISO 20022 standard

⚠️ **Limitations**:
- High-value only (> R 5M typically)
- Business hours only (07:00-17:00)
- No retries (immediate accept/reject)
- Requires sufficient liquidity in reserve account

🔐 **Security**:
- mTLS (client certificate authentication)
- XMLDSig (digital signature)
- Message-level encryption
- SARB-issued certificates only

---

## 2. BankservAfrica (EFT/ACH) Interaction Flow

**System**: BankservAfrica Automated Clearing House  
**Type**: Batch Processing (ACH/EFT)  
**Use Case**: Low-value bulk payments (salaries, suppliers, debit orders)  
**Operator**: BankservAfrica (Pty) Ltd  
**Settlement**: T+0 or T+1 (batch-dependent)  
**Volume**: 10,000 - 100,000 payments per file

### 2.1 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PAYMENTS ENGINE                                 │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Batch Processing Service                                       │ │
│  │  └─> Payment Aggregation (Group by cut-off time)               │ │
│  │       └─> BankservAfrica Adapter Service                        │ │
│  │            └─> File Generation (ACH format)                     │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ ACH File Format (Proprietary)
                                │ OR ISO 8583 (for some transactions)
                                │ Via SFTP + PGP Encryption
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    BANKSERVAFRICA SYSTEM                             │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  File Validation                                                │ │
│  │  └─> Batch Processing (Multiple cut-offs per day)              │ │
│  │       └─> Inter-Bank Settlement (T+0 or T+1)                   │ │
│  │            └─> Response File Generation                         │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Detailed Interaction Flow

#### Step 1: Payment Aggregation (Throughout the day)

```
Batch Windows (Daily Cut-Offs):
- 08:00 (Early morning batch) - T+0 settlement
- 11:00 (Mid-morning batch) - T+0 settlement
- 14:00 (Afternoon batch) - T+0 settlement
- 17:00 (Final batch) - T+1 settlement

Example: Aggregating payments for 11:00 cut-off

Time: 10:45 - Start aggregation
Time: 10:55 - Generate file
Time: 10:58 - Upload to BankservAfrica
Time: 11:00 - Cut-off (file processing starts)
```

**Spring Batch Job** (Java):
```java
@Configuration
public class BankservBatchConfiguration {
    
    @Bean
    public Job exportBankservFileJob(JobBuilderFactory jobs, Step step1) {
        return jobs.get("exportBankservFileJob")
            .incrementer(new RunIdIncrementer())
            .start(step1)
            .build();
    }
    
    @Bean
    public Step step1(StepBuilderFactory steps,
                      ItemReader<Payment> reader,
                      ItemProcessor<Payment, AchRecord> processor,
                      ItemWriter<AchRecord> writer) {
        return steps.get("step1")
            .<Payment, AchRecord>chunk(1000) // Process 1000 at a time
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
    
    @Bean
    public ItemReader<Payment> reader() {
        return new JpaPagingItemReaderBuilder<Payment>()
            .name("paymentReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT p FROM Payment p WHERE p.clearingSystem = 'BANKSERV' " +
                        "AND p.status = 'PENDING' AND p.cutOffTime = :cutOffTime")
            .parameterValues(Map.of("cutOffTime", LocalTime.of(11, 0)))
            .pageSize(1000)
            .build();
    }
    
    @Bean
    public ItemProcessor<Payment, AchRecord> processor() {
        return payment -> {
            // Convert Payment to ACH record format
            AchRecord record = new AchRecord();
            record.setRecordType("D"); // Debit
            record.setTransactionCode("26"); // EFT Credit
            record.setReceivingBankCode(payment.getBeneficiaryBankCode());
            record.setReceivingAccountNumber(payment.getBeneficiaryAccount());
            record.setAmount(payment.getAmount().multiply(new BigDecimal("100")).longValue()); // Cents
            record.setReference(payment.getReference());
            record.setActionDate(payment.getValueDate().format(DateTimeFormatter.BASIC_ISO_DATE));
            return record;
        };
    }
    
    @Bean
    public ItemWriter<AchRecord> writer() {
        return new FlatFileItemWriterBuilder<AchRecord>()
            .name("achFileWriter")
            .resource(new FileSystemResource("output/BANKSERV-" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".txt"))
            .lineAggregator(new AchRecordLineAggregator())
            .build();
    }
}
```

#### Step 2: File Generation (T-0 - 5 minutes before cut-off)

**ACH File Format** (Proprietary Fixed-Width):

```
Header Record (Type 1):
Position   Length  Description                Example
1-1        1       Record Type                1
2-2        1       User Code                  A
3-7        5       User Branch Code           01234
8-15       8       Creation Date              20251013
16-35      20      User Name                  BANK001 PAYMENTS
36-43      8       File Sequence Number       00000123
44-50      7       User Generation Number     0000001
51-80      30      Filler                     (spaces)

Detail Records (Type 2):
Position   Length  Description                Example
1-1        1       Record Type                2
2-3        2       Transaction Code           26 (EFT Credit)
4-9        6       Receiving Bank Branch      632010
10-20      11      Beneficiary Account        62009876543
21-22      2       Account Type               01 (Savings)
23-34      12      Amount (cents)             000001000000 (R 10,000.00)
35-54      20      Beneficiary Name           JOHN DOE
55-74      20      Paying Company Name        ABC CORP
75-94      20      Reference                  SALARY OCT 2025
95-102     8       Action Date                20251013
103-112    10      Homing Branch              6320050123
113-124    12      Homing Account             620012345678
125-126    2       Filler
127-127    1       Trace Number Type          0
128-133    6       Trace Number               000001
134-160    27      Filler

Trailer Record (Type 9):
Position   Length  Description                Example
1-1        1       Record Type                9
2-11       10      Number of Detail Records   0000012345
12-23      12      Total Debits (cents)       012345678900
24-35      12      Total Credits (cents)      000000000000
36-47      12      Control Hash               012345678901
48-80      33      Filler
```

**Sample File**:
```
1A0123420251013BANK001 PAYMENTS    0000012300000001                              
226632010620098765430100000100000JOHN DOE            ABC CORP            SALARY OCT 2025     202510136320050123620012345678  0000001                       
226632011620098765440100000150000JANE SMITH          ABC CORP            SALARY OCT 2025     202510136320050123620012345678  0000002                       
226632012620098765450100000120000MIKE JONES          ABC CORP            SALARY OCT 2025     202510136320050123620012345678  0000003                       
...
9000000123401234567890000000000000123456789                                                    
```

#### Step 3: File Encryption & Transmission (T-0 - 3 minutes)

**PGP Encryption**:
```java
@Service
public class FileEncryptionService {
    
    public File encryptFile(File plainTextFile, String recipientPublicKeyPath) throws Exception {
        // 1. Load BankservAfrica's public key
        PGPPublicKey recipientPublicKey = loadPublicKey(recipientPublicKeyPath);
        
        // 2. Encrypt file
        FileInputStream fis = new FileInputStream(plainTextFile);
        FileOutputStream fos = new FileOutputStream(plainTextFile.getAbsolutePath() + ".pgp");
        
        // Create encrypted data generator
        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
            new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256)
                .setWithIntegrityPacket(true)
                .setSecureRandom(new SecureRandom())
                .setProvider("BC")
        );
        
        encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(recipientPublicKey)
            .setProvider("BC"));
        
        // Encrypt
        OutputStream encryptedOut = encGen.open(fos, new byte[4096]);
        Streams.pipeAll(fis, encryptedOut);
        encryptedOut.close();
        fos.close();
        fis.close();
        
        return new File(plainTextFile.getAbsolutePath() + ".pgp");
    }
}
```

**Digital Signature**:
```java
public File signFile(File file, String privateKeyPath, String passphrase) throws Exception {
    // 1. Load our private key
    PGPSecretKey secretKey = loadSecretKey(privateKeyPath);
    PGPPrivateKey privateKey = secretKey.extractPrivateKey(
        new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(passphrase.toCharArray())
    );
    
    // 2. Create signature generator
    PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
        new JcaPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256)
            .setProvider("BC")
    );
    
    signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);
    
    // 3. Generate signature
    FileInputStream fis = new FileInputStream(file);
    byte[] buffer = new byte[4096];
    int bytesRead;
    
    while ((bytesRead = fis.read(buffer)) != -1) {
        signatureGenerator.update(buffer, 0, bytesRead);
    }
    fis.close();
    
    PGPSignature signature = signatureGenerator.generate();
    
    // 4. Write detached signature file
    FileOutputStream sigOut = new FileOutputStream(file.getAbsolutePath() + ".sig");
    signature.encode(new BCPGOutputStream(sigOut));
    sigOut.close();
    
    return new File(file.getAbsolutePath() + ".sig");
}
```

**SFTP Upload**:
```java
@Service
public class BankservSftpService {
    
    @Value("${bankserv.sftp.host}")
    private String sftpHost;
    
    @Value("${bankserv.sftp.port}")
    private int sftpPort;
    
    @Value("${bankserv.sftp.username}")
    private String sftpUsername;
    
    @Value("${bankserv.sftp.private-key-path}")
    private String privateKeyPath;
    
    public void uploadFile(File file, File signatureFile) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath); // SSH key authentication
        
        Session session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
        session.setConfig("StrictHostKeyChecking", "yes");
        session.setConfig("PreferredAuthentications", "publickey");
        session.connect();
        
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        
        try {
            // 1. Upload encrypted file
            channelSftp.cd("/incoming");
            channelSftp.put(file.getAbsolutePath(), file.getName());
            
            // 2. Upload signature file
            channelSftp.put(signatureFile.getAbsolutePath(), signatureFile.getName());
            
            log.info("Successfully uploaded {} to BankservAfrica", file.getName());
            
        } finally {
            channelSftp.disconnect();
            session.disconnect();
        }
    }
}
```

#### Step 4: BankservAfrica Processing (11:00 - 13:00)

**BankservAfrica Internal Flow**:
```
11:00 - Cut-off time, start processing
11:00-11:15 - File collection from all banks
11:15-11:30 - File decryption and validation
  ✅ PGP decryption
  ✅ Signature verification
  ✅ File format validation
  ✅ Header/trailer record checks
  ✅ Control hash validation
  ✅ Duplicate file check (sequence number)

11:30-12:00 - Transaction validation
  ✅ Bank codes valid
  ✅ Account numbers valid format
  ✅ Amounts positive and reasonable
  ✅ Action dates valid
  ✅ Reference fields populated

12:00-12:30 - Inter-bank netting
  Calculate net position per bank:
  Bank A: +R 1.5M (net receiver)
  Bank B: -R 800K (net payer)
  Bank C: -R 700K (net payer)

12:30-13:00 - Settlement
  Instruct SARB to move funds between reserve accounts
  (via SAMOS or direct settlement instruction)

13:00 - Settlement complete, generate response files
```

#### Step 5: Response File Generation (13:00)

**Response File Format** (ACH format):

```
Header Record (Type 1):
1A01234202510

13BANKSERVAFRICA        0000012300000001                              

Detail Records (Type 2 - Accepted):
226632010620098765430100000100000JOHN DOE            ABC CORP            SALARY OCT 2025     20251013632005012362001234567800000001     0

Detail Records (Type 3 - Rejected):
326632011620098765440100000150000JANE SMITH          ABC CORP            SALARY OCT 2025     202510136320050123620012345678000000022001INVALID ACCOUNT

Rejection Codes:
2001 - Invalid account number
2002 - Account closed
2003 - Beneficiary name mismatch
2004 - Bank not participant
2005 - Insufficient funds (if pre-funded required)
2006 - Duplicate transaction
```

**SFTP Download & Processing**:
```java
@Service
public class BankservResponseProcessor {
    
    @Scheduled(cron = "0 0 13,17 * * MON-FRI") // 13:00 and 17:00
    public void downloadAndProcessResponseFiles() {
        // 1. Connect to SFTP
        ChannelSftp sftp = sftpService.connect();
        
        // 2. List files in /outgoing directory
        List<String> files = sftp.ls("/outgoing/*.pgp");
        
        for (String encryptedFile : files) {
            // 3. Download encrypted file
            File localFile = sftp.download(encryptedFile);
            
            // 4. Decrypt
            File decryptedFile = encryptionService.decryptFile(
                localFile,
                ourPrivateKeyPath,
                passphrase
            );
            
            // 5. Parse response file
            List<ResponseRecord> responses = parseResponseFile(decryptedFile);
            
            // 6. Process each response
            for (ResponseRecord response : responses) {
                Payment payment = paymentRepository.findByTraceNumber(response.getTraceNumber());
                
                if (response.isAccepted()) {
                    // SUCCESS
                    payment.setStatus(PaymentStatus.SETTLED);
                    payment.setSettlementDate(response.getActionDate());
                    payment.setClearingReference(response.getClearingReference());
                    
                    // Publish event
                    eventPublisher.publish("payment-settled", new PaymentSettledEvent(payment));
                    
                    // Notify customer
                    notificationService.sendSettlementConfirmation(payment);
                    
                } else {
                    // REJECTED
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setRejectionCode(response.getRejectionCode());
                    payment.setRejectionReason(response.getRejectionReason());
                    
                    // Reverse debit if already done
                    coreBankingService.reverseDebit(
                        payment.getDebitAccount(),
                        payment.getAmount(),
                        "BankservAfrica rejection: " + response.getRejectionCode()
                    );
                    
                    // Publish event
                    eventPublisher.publish("payment-failed", new PaymentFailedEvent(payment));
                    
                    // Notify customer
                    notificationService.sendPaymentFailureNotification(payment);
                }
                
                paymentRepository.save(payment);
            }
            
            // 7. Archive file
            archiveService.archiveFile(decryptedFile, "bankserv-responses", LocalDate.now());
            
            // 8. Delete from SFTP
            sftp.delete(encryptedFile);
        }
    }
}
```

#### Step 6: Reconciliation (End of Day)

```java
@Service
public class BankservReconciliationService {
    
    @Scheduled(cron = "0 0 18 * * MON-FRI") // 18:00 daily
    public void reconcileBankservTransactions() {
        LocalDate today = LocalDate.now();
        
        // 1. Get all payments sent to BankservAfrica today
        List<Payment> sentPayments = paymentRepository.findByClearingSystemAndDate(
            "BANKSERV",
            today
        );
        
        // 2. Get all response records received today
        List<ResponseRecord> responses = responseRepository.findByDate(today);
        
        // 3. Match by trace number
        ReconciliationReport report = new ReconciliationReport();
        
        for (Payment payment : sentPayments) {
            Optional<ResponseRecord> matchedResponse = responses.stream()
                .filter(r -> r.getTraceNumber().equals(payment.getTraceNumber()))
                .findFirst();
            
            if (matchedResponse.isPresent()) {
                ResponseRecord response = matchedResponse.get();
                
                // Verify payment status matches response
                if (payment.getStatus() == PaymentStatus.SETTLED && response.isAccepted()) {
                    report.addMatched(payment, response);
                } else if (payment.getStatus() == PaymentStatus.FAILED && !response.isAccepted()) {
                    report.addMatched(payment, response);
                } else {
                    report.addStatusMismatch(payment, response);
                }
                
                responses.remove(matchedResponse.get());
            } else {
                // Payment sent but no response received
                report.addMissingResponse(payment);
            }
        }
        
        // 4. Remaining responses are "unexpected responses"
        for (ResponseRecord response : responses) {
            report.addUnexpectedResponse(response);
        }
        
        // 5. Download settlement report from BankservAfrica
        File settlementReport = sftpService.downloadSettlementReport(today);
        SettlementData settlementData = parseSettlementReport(settlementReport);
        
        // 6. Verify net settlement amount
        BigDecimal ourCalculatedNetAmount = calculateNetAmount(sentPayments);
        BigDecimal bankservNetAmount = settlementData.getNetAmount();
        
        if (ourCalculatedNetAmount.compareTo(bankservNetAmount) != 0) {
            report.addSettlementMismatch(ourCalculatedNetAmount, bankservNetAmount);
        }
        
        // 7. Store report
        reconciliationRepository.save(report);
        
        // 8. Alert if issues
        if (!report.isFullyReconciled()) {
            alertService.sendReconciliationAlert(report);
        }
    }
}
```

### 2.3 BankservAfrica Timing Characteristics

| Event | Timing | Notes |
|-------|--------|-------|
| Payment aggregation | 10 minutes before cut-off | Batch window |
| File generation | 5 minutes | 10,000 payments typical |
| File upload (SFTP) | 2 minutes | Including encryption |
| BankservAfrica processing | 2 hours | Cut-off to settlement |
| Response file available | Cut-off + 2 hours | Download via SFTP |
| End of day reconciliation | 18:00 | Full day reconciliation |

**Cut-Off Times**:
- 08:00 → Settlement by 10:00 (T+0)
- 11:00 → Settlement by 13:00 (T+0)
- 14:00 → Settlement by 16:00 (T+0)
- 17:00 → Settlement by 10:00 next day (T+1)

### 2.4 BankservAfrica Error Codes

| Code | Description | Action |
|------|-------------|--------|
| 0000 | Accepted | ✅ Success |
| 2001 | Invalid Account Number | ❌ Reverse, notify |
| 2002 | Account Closed | ❌ Reverse, notify |
| 2003 | Beneficiary Name Mismatch | ⚠️ Manual review |
| 2004 | Bank Not Participant | ❌ Reverse, reroute |
| 2005 | Insufficient Funds | ❌ Reverse, retry |
| 2006 | Duplicate Transaction | ⚠️ Check original |
| 2007 | Invalid Bank Code | ❌ Configuration error |
| 2008 | Action Date Invalid | ❌ Date in past |

### 2.5 BankservAfrica Key Characteristics

✅ **Strengths**:
- High volume capacity (100K+ per file)
- Low cost per transaction
- Batch efficiency
- T+0 settlement (for early cut-offs)
- Established infrastructure

⚠️ **Limitations**:
- Batch processing (not real-time)
- Cut-off times rigid
- 2-hour processing window
- Response delayed
- File-based (not API)

🔐 **Security**:
- SFTP with SSH key authentication
- PGP encryption (file-level)
- Detached digital signatures
- File integrity checks (control hash)

---

## 3. RTC (Real-Time Clearing) Interaction Flow

**System**: Real-Time Clearing  
**Type**: Real-time payment switching  
**Use Case**: Low-value real-time payments (< R 5 million)  
**Operator**: BankservAfrica (RTC division)  
**Settlement**: Near real-time (< 30 seconds)  
**Operating Hours**: 24/7/365

### 3.1 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PAYMENTS ENGINE                                 │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Payment Initiation Service                                     │ │
│  │  └─> Validation Service (Real-time validation)                 │ │
│  │       └─> Routing Service (Route to RTC)                       │ │
│  │            └─> RTC Adapter Service                              │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ ISO 20022 (pain.001 / pacs.008)
                                │ Over HTTPS + mTLS
                                │ Synchronous API
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         RTC SYSTEM                                   │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Message Validation (< 1 second)                                │ │
│  │  └─> Routing to Beneficiary Bank                               │ │
│  │       └─> Beneficiary Bank Validation                          │ │
│  │            └─> Settlement (< 30 seconds)                        │ │
│  │                 └─> Confirmation to both banks                  │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 Detailed Interaction Flow

#### Step 1: Payment Initiation (T+0)

```
Time: 14:32:15 (Any time, 24/7)

Payment Request:
{
  "amount": 15000.00,  // R 15,000
  "currency": "ZAR",
  "debitAccount": "62001234567",
  "creditAccount": "62009876543",
  "beneficiaryBank": "632010",
  "beneficiaryName": "John Doe",
  "reference": "Invoice Payment INV-789",
  "urgency": "NORMAL"
}

Validation:
✅ Amount < R 5M (RTC eligible)
✅ 24/7 available
✅ Beneficiary bank RTC participant
✅ Account balance sufficient
✅ Limit checks passed
✅ Fraud score acceptable
```

#### Step 2: ISO 20022 Message Construction

**pain.001 (Payment Initiation)**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09">
  <CstmrCdtTrfInitn>
    <GrpHdr>
      <MsgId>BANK001-RTC-20251013-143215-000456</MsgId>
      <CreDtTm>2025-10-13T14:32:15</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <CtrlSum>15000.00</CtrlSum>
      <InitgPty>
        <Nm>Bank 001</Nm>
        <Id>
          <OrgId>
            <Othr>
              <Id>632005</Id>
            </Othr>
          </OrgId>
        </Id>
      </InitgPty>
    </GrpHdr>
    <PmtInf>
      <PmtInfId>PMT-RTC-000456</PmtInfId>
      <PmtMtd>TRF</PmtMtd>
      <ReqdExctnDt>
        <Dt>2025-10-13</Dt>
      </ReqdExctnDt>
      <Dbtr>
        <Nm>ABC Corporation (Pty) Ltd</Nm>
      </Dbtr>
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>62001234567</Id>
          </Othr>
        </Id>
      </DbtrAcct>
      <DbtrAgt>
        <FinInstnId>
          <ClrSysMmbId>
            <MmbId>632005</MmbId>
          </ClrSysMmbId>
        </FinInstnId>
      </DbtrAgt>
      <CdtTrfTxInf>
        <PmtId>
          <EndToEndId>PAY-2025-000456</EndToEndId>
        </PmtId>
        <Amt>
          <InstdAmt Ccy="ZAR">15000.00</InstdAmt>
        </Amt>
        <CdtrAgt>
          <FinInstnId>
            <ClrSysMmbId>
              <MmbId>632010</MmbId>
            </ClrSysMmbId>
          </FinInstnId>
        </CdtrAgt>
        <Cdtr>
          <Nm>John Doe</Nm>
        </Cdtr>
        <CdtrAcct>
          <Id>
            <Othr>
              <Id>62009876543</Id>
            </Othr>
          </Id>
        </CdtrAcct>
        <RmtInf>
          <Ustrd>Invoice Payment INV-789</Ustrd>
        </RmtInf>
      </CdtTrfTxInf>
    </PmtInf>
  </CstmrCdtTrfInitn>
</Document>
```

#### Step 3: Transmission to RTC (T+0 + 1 second)

```java
@Service
public class RtcAdapter {
    
    @Autowired
    private RestTemplate rtcRestTemplate; // Pre-configured with mTLS
    
    @Value("${rtc.endpoint}")
    private String rtcEndpoint;
    
    public RtcResponse sendPayment(Payment payment) {
        // 1. Build pain.001 message
        String xml = buildPain001Message(payment);
        
        // 2. Send to RTC (Synchronous call)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set("X-Message-Type", "pain.001.001.09");
        headers.set("X-Bank-Code", bankCode);
        headers.set("X-Message-Id", payment.getMessageId());
        headers.set("X-Request-Timestamp", Instant.now().toString());
        
        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        
        // 3. Call RTC (with 30-second timeout)
        try {
            ResponseEntity<String> response = rtcRestTemplate.exchange(
                rtcEndpoint + "/api/v1/payments",
                HttpMethod.POST,
                request,
                String.class
            );
            
            // 4. Parse response (pain.002 - Payment Status Report)
            return parsePain002Response(response.getBody());
            
        } catch (HttpClientErrorException e) {
            // 4xx errors (validation failures)
            throw new RtcValidationException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            // 5xx errors (RTC system issues)
            throw new RtcSystemException(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            // Timeout (> 30 seconds)
            throw new RtcTimeoutException("RTC did not respond within 30 seconds");
        }
    }
}
```

#### Step 4: RTC Processing (T+0 + 2-10 seconds)

**RTC Internal Flow**:
```
1. Sender Bank Validation (< 1 second)
   ✅ Message format (ISO 20022 pain.001)
   ✅ mTLS certificate valid
   ✅ Bank code registered
   ✅ Message ID unique
   ✅ Amount within limits (< R 5M)

2. Routing to Beneficiary Bank (< 1 second)
   - Lookup bank code: 632010
   - Route message to Bank 010's RTC endpoint
   - Forward payment instruction

3. Beneficiary Bank Validation (< 5 seconds)
   Bank 010 performs:
   ✅ Account number exists
   ✅ Account is active (not closed/blocked)
   ✅ Beneficiary name matches (optional verification)
   ✅ Account can receive credits
   
   If ALL validations pass:
     → Accept payment
   If ANY validation fails:
     → Reject with reason code

4. Settlement Instruction (< 2 seconds)
   If accepted:
     - RTC instructs settlement
     - Move funds between bank reserve accounts
     - Update clearing balances

5. Confirmation (< 1 second)
   - Generate pain.002 (Payment Status Report)
   - Send to Sender Bank (our bank)
   - Send notification to Beneficiary Bank
```

#### Step 5: RTC Response (T+0 + 10 seconds)

**Success Response** (pain.002):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.002.001.10">
  <CstmrPmtStsRpt>
    <GrpHdr>
      <MsgId>RTC-20251013-143225-000456</MsgId>
      <CreDtTm>2025-10-13T14:32:25</CreDtTm>
    </GrpHdr>
    <OrgnlGrpInfAndSts>
      <OrgnlMsgId>BANK001-RTC-20251013-143215-000456</OrgnlMsgId>
      <OrgnlMsgNmId>pain.001.001.09</OrgnlMsgNmId>
      <GrpSts>ACCP</GrpSts>  <!-- Accepted -->
    </OrgnlGrpInfAndSts>
    <OrgnlPmtInfAndSts>
      <OrgnlPmtInfId>PMT-RTC-000456</OrgnlPmtInfId>
      <PmtInfSts>ACSC</PmtInfSts>  <!-- AcceptedSettlementCompleted -->
      <StsRsnInf>
        <Rsn>
          <Cd>SETT</Cd>  <!-- Settled -->
        </Rsn>
      </StsRsnInf>
      <TxInfAndSts>
        <OrgnlEndToEndId>PAY-2025-000456</OrgnlEndToEndId>
        <TxSts>ACSC</TxSts>
        <AccptncDtTm>2025-10-13T14:32:25</AccptncDtTm>
        <ClrSysRef>RTC-2025-1013-143225-000456</ClrSysRef>
      </TxInfAndSts>
    </OrgnlPmtInfAndSts>
  </CstmrPmtStsRpt>
</Document>
```

**Failure Response Example** (Invalid Account):
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.002.001.10">
  <CstmrPmtStsRpt>
    <GrpHdr>
      <MsgId>RTC-20251013-143226-000457</MsgId>
      <CreDtTm>2025-10-13T14:32:26</CreDtTm>
    </GrpHdr>
    <OrgnlPmtInfAndSts>
      <OrgnlPmtInfId>PMT-RTC-000457</OrgnlPmtInfId>
      <TxInfAndSts>
        <OrgnlEndToEndId>PAY-2025-000457</OrgnlEndToEndId>
        <TxSts>RJCT</TxSts>  <!-- Rejected -->
        <StsRsnInf>
          <Rsn>
            <Cd>AC03</Cd>  <!-- Invalid Creditor Account Number -->
          </Rsn>
          <AddtlInf>Account number does not exist or is closed</AddtlInf>
        </StsRsnInf>
      </TxInfAndSts>
    </OrgnlPmtInfAndSts>
  </CstmrPmtStsRpt>
</Document>
```

#### Step 6: Payments Engine Post-Processing

```java
public void processRtcResponse(RtcResponse response) {
    Payment payment = paymentRepository.findByEndToEndId(response.getOriginalEndToEndId());
    
    if (response.getStatus() == TransactionStatus.ACSC) {
        // SUCCESS
        payment.setStatus(PaymentStatus.SETTLED);
        payment.setSettlementTimestamp(response.getAcceptanceTimestamp());
        payment.setClearingReference(response.getClearingSystemReference());
        
        // Publish event
        eventPublisher.publish("payment-settled", new PaymentSettledEvent(payment));
        
        // Notify customer (SMS/Email/Push)
        notificationService.sendRealTimeConfirmation(payment);
        
        // Confirm debit in core banking
        coreBankingService.confirmDebit(
            payment.getDebitAccount(),
            payment.getAmount(),
            response.getClearingSystemReference()
        );
        
    } else if (response.getStatus() == TransactionStatus.RJCT) {
        // FAILURE
        payment.setStatus(PaymentStatus.FAILED);
        payment.setRejectionCode(response.getReasonCode());
        payment.setRejectionReason(response.getAdditionalInfo());
        
        // Reverse debit
        coreBankingService.reverseDebit(
            payment.getDebitAccount(),
            payment.getAmount(),
            "RTC rejection: " + response.getReasonCode()
        );
        
        // Publish event
        eventPublisher.publish("payment-failed", new PaymentFailedEvent(payment));
        
        // Notify customer
        notificationService.sendRealTimeFailureNotification(payment);
    }
    
    paymentRepository.save(payment);
}
```

### 3.3 RTC Timing Characteristics

| Event | Timing | Notes |
|-------|--------|-------|
| Message submission | T+0 + 1s | From initiation to RTC API |
| RTC validation | < 1 second | Message validation |
| Beneficiary bank validation | < 5 seconds | Account verification |
| Settlement | < 2 seconds | Fund movement |
| Confirmation response | T+0 + 10s | Total end-to-end |
| Reconciliation | Hourly | Throughout the day |

**Total SLA**: < 30 seconds (typically 10-15 seconds) ✅

### 3.4 RTC Error Codes

| Code | Description | Action |
|------|-------------|--------|
| ACSC | AcceptedSettlementCompleted | ✅ Success |
| ACCP | Accepted | ✅ Intermediate success |
| RJCT | Rejected | ❌ Failure |
| AC03 | Invalid Creditor Account | ❌ Account doesn't exist |
| AC04 | Account Closed | ❌ Account closed |
| AC06 | Account Blocked | ❌ Account blocked |
| AM04 | Insufficient Funds | ⚠️ Rare (pre-validated) |
| AG01 | Transaction Forbidden | ❌ Compliance issue |
| DUPL | Duplicate | ❌ Already processed |

### 3.5 RTC Key Characteristics

✅ **Strengths**:
- Real-time (< 30 seconds)
- 24/7/365 availability
- Synchronous confirmation
- Account validation before settlement
- ISO 20022 standard
- Lower cost than SAMOS

⚠️ **Limitations**:
- Low-value only (< R 5 million)
- Single retry only (if beneficiary bank timeout)
- Requires both banks RTC-enabled
- Network dependency (99.9% SLA)

🔐 **Security**:
- mTLS (certificate authentication)
- Message-level encryption
- API key authentication
- Request signing (optional)

---

## 4. PayShap Interaction Flow

**System**: PayShap (Rapid Payments Programme)  
**Type**: Instant P2P payments  
**Use Case**: Person-to-Person payments using mobile number or email as proxy  
**Operator**: PayShap (BankservAfrica initiative)  
**Settlement**: Real-time (< 10 seconds)  
**Limit**: R 3,000 per transaction  
**Operating Hours**: 24/7/365

### 4.1 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PAYMENTS ENGINE                                 │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Payment Initiation Service                                     │ │
│  │  └─> PayShap Proxy Resolution (Mobile/Email → Account)         │ │
│  │       └─> Validation Service (R 3,000 limit)                   │ │
│  │            └─> PayShap Adapter Service                          │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ ISO 20022 (pacs.008)
                                │ Over HTTPS + OAuth 2.0 + mTLS
                                │ REST API (JSON wrapper)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         PAYSHAP SYSTEM                               │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Proxy Registry Lookup                                          │ │
│  │  └─> Account Resolution (Mobile → Account Number + Bank)       │ │
│  │       └─> Routing to Beneficiary Bank                          │ │
│  │            └─> Settlement (< 10 seconds)                        │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.2 Proxy Registration Flow (One-Time Setup)

**Customer Registration**:
```
Customer uses mobile app/internet banking:
1. Select "Register for PayShap"
2. Enter mobile number: +27 82 123 4567
3. Select primary account: 62001234567
4. Confirm registration

Backend Flow:
```

```java
@Service
public class PayShapProxyService {
    
    public void registerProxy(String mobileNumber, String accountNumber, String customerId) {
        // 1. Validate mobile number (South African format)
        if (!mobileNumber.matches("\\+27[0-9]{9}")) {
            throw new InvalidMobileNumberException();
        }
        
        // 2. Verify account belongs to customer
        Account account = coreBankingService.getAccount(accountNumber);
        if (!account.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccountException();
        }
        
        // 3. Call PayShap API to register proxy
        PayShapProxyRequest request = PayShapProxyRequest.builder()
            .proxyType("MOBILE")
            .proxyValue(mobileNumber)
            .accountNumber(accountNumber)
            .bankCode(ourBankCode)
            .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getOAuthToken()); // OAuth 2.0 token
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<PayShapProxyRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<PayShapProxyResponse> response = restTemplate.exchange(
            payShapEndpoint + "/api/v1/proxy/register",
            HttpMethod.POST,
            entity,
            PayShapProxyResponse.class
        );
        
        if (response.getStatusCode() == HttpStatus.CREATED) {
            // 4. Store proxy registration locally
            proxyRepository.save(ProxyRegistration.builder()
                .proxyType("MOBILE")
                .proxyValue(mobileNumber)
                .accountNumber(accountNumber)
                .registrationDate(LocalDateTime.now())
                .status("ACTIVE")
                .build());
            
            // 5. Notify customer
            notificationService.sendProxyRegistrationConfirmation(customerId, mobileNumber);
        }
    }
}
```

**PayShap Proxy Registration API**:
```json
POST /api/v1/proxy/register
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc...
Content-Type: application/json

{
  "proxyType": "MOBILE",
  "proxyValue": "+27821234567",
  "accountNumber": "62001234567",
  "bankCode": "632005",
  "accountHolderName": "John Doe",
  "accountType": "CURRENT"
}

Response 201 Created:
{
  "proxyId": "PSHP-PROXY-000123",
  "status": "ACTIVE",
  "registrationTimestamp": "2025-10-13T14:00:00Z",
  "expiryDate": null
}
```

### 4.3 Payment Flow (Using Proxy)

#### Step 1: Payment Initiation

```
Customer sends money using mobile number:

From: Mobile App
To: +27 83 987 6543 (Beneficiary mobile - no account number needed!)
Amount: R 500.00
Reference: Lunch payment
```

**API Call**:
```json
POST /api/v1/payments/payshap
Authorization: Bearer customer_token
Content-Type: application/json

{
  "debitAccount": "62001234567",
  "beneficiaryProxy": {
    "type": "MOBILE",
    "value": "+27839876543"
  },
  "amount": 500.00,
  "currency": "ZAR",
  "reference": "Lunch payment"
}
```

#### Step 2: Proxy Resolution

```java
@Service
public class PayShapAdapter {
    
    public PayShapPaymentResponse sendPayment(PayShapPaymentRequest request) {
        // 1. Resolve proxy to account number
        ProxyResolutionResponse resolution = resolveProxy(request.getBeneficiaryProxy());
        
        if (!resolution.isFound()) {
            throw new ProxyNotFoundException("Beneficiary not registered on PayShap");
        }
        
        // 2. Build ISO 20022 pacs.008 message
        String xml = buildPacs008WithProxy(
            request,
            resolution.getAccountNumber(),
            resolution.getBankCode(),
            resolution.getAccountHolderName()
        );
        
        // 3. Send to PayShap
        return sendToPayShap(xml);
    }
    
    private ProxyResolutionResponse resolveProxy(ProxyIdentifier proxy) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getOAuthToken());
        
        ResponseEntity<ProxyResolutionResponse> response = restTemplate.exchange(
            payShapEndpoint + "/api/v1/proxy/resolve?type=" + proxy.getType() + 
                "&value=" + proxy.getValue(),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ProxyResolutionResponse.class
        );
        
        return response.getBody();
    }
}
```

**Proxy Resolution API**:
```
GET /api/v1/proxy/resolve?type=MOBILE&value=%2B27839876543
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc...

Response 200 OK:
{
  "found": true,
  "accountNumber": "62009876543",
  "bankCode": "632010",
  "accountHolderName": "Jane Smith",
  "accountType": "SAVINGS"
}
```

#### Step 3: ISO 20022 Message with Proxy

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>BANK001-PSHP-20251013-140530-000789</MsgId>
      <CreDtTm>2025-10-13T14:05:30</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <TtlIntrBkSttlmAmt Ccy="ZAR">500.00</TtlIntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-13</IntrBkSttlmDt>
      <SttlmInf>
        <SttlmMtd>CLRG</SttlmMtd>
        <ClrSys>
          <Cd>PAYSHAP</Cd>
        </ClrSys>
      </SttlmInf>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <EndToEndId>PAY-2025-000789</EndToEndId>
      </PmtId>
      <IntrBkSttlmAmt Ccy="ZAR">500.00</IntrBkSttlmAmt>
      <Dbtr>
        <Nm>John Doe</Nm>
      </Dbtr>
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>62001234567</Id>
          </Othr>
        </Id>
      </DbtrAcct>
      <DbtrAgt>
        <FinInstnId>
          <ClrSysMmbId>
            <MmbId>632005</MmbId>
          </ClrSysMmbId>
        </FinInstnId>
      </DbtrAgt>
      <CdtrAgt>
        <FinInstnId>
          <ClrSysMmbId>
            <MmbId>632010</MmbId>
          </ClrSysMmbId>
        </FinInstnId>
      </CdtrAgt>
      <Cdtr>
        <Nm>Jane Smith</Nm>
      </Cdtr>
      <CdtrAcct>
        <Id>
          <Othr>
            <Id>62009876543</Id>  <!-- Resolved from proxy -->
          </Othr>
        </Id>
      </CdtrAcct>
      <Purp>
        <Cd>CASH</Cd>  <!-- P2P Payment -->
      </Purp>
      <RmtInf>
        <Ustrd>Lunch payment</Ustrd>
      </RmtInf>
      <!-- PayShap-specific extensions -->
      <SplmtryData>
        <Envlp>
          <ProxyUsed>
            <Type>MOBILE</Type>
            <Value>+27839876543</Value>
          </ProxyUsed>
        </Envlp>
      </SplmtryData>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

#### Step 4: PayShap Processing (< 10 seconds)

```
1. Message Validation (< 1 second)
   ✅ OAuth token valid
   ✅ mTLS certificate valid
   ✅ Amount ≤ R 3,000 (PayShap limit)
   ✅ Proxy resolution successful
   ✅ Message format valid

2. Routing (< 1 second)
   - Route to Bank 632010
   - Forward payment instruction

3. Beneficiary Bank Processing (< 5 seconds)
   ✅ Account 62009876543 exists
   ✅ Account active
   ✅ Can receive credits
   
4. Settlement (< 2 seconds)
   - Immediate fund movement
   - Update clearing balances

5. Push Notification (< 1 second)
   - PayShap sends SMS to beneficiary: +27 83 987 6543
   - "You received R 500 from John Doe. Ref: Lunch payment"
```

#### Step 5: Response

**Success Response**:
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
  <FIToFIPmtStsRpt>
    <GrpHdr>
      <MsgId>PAYSHAP-20251013-140540-000789</MsgId>
      <CreDtTm>2025-10-13T14:05:40</CreDtTm>
    </GrpHdr>
    <TxInfAndSts>
      <OrgnlEndToEndId>PAY-2025-000789</OrgnlEndToEndId>
      <TxSts>ACSC</TxSts>  <!-- AcceptedSettlementCompleted -->
      <AccptncDtTm>2025-10-13T14:05:40</AccptncDtTm>
      <ClrSysRef>PSHP-2025-1013-140540-000789</ClrSysRef>
      <StsRsnInf>
        <Rsn>
          <Cd>SETT</Cd>
        </Rsn>
      </StsRsnInf>
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

**Beneficiary Notification** (Automatic from PayShap):
```
SMS to +27 83 987 6543:
"You received R500.00 from John Doe (via Bank 001).
 Reference: Lunch payment
 Account: ...6543
 Time: 14:05
 - PayShap"
```

### 4.4 PayShap OAuth 2.0 Authentication

```java
@Service
public class PayShapOAuthService {
    
    @Value("${payshap.oauth.token-url}")
    private String tokenUrl;
    
    @Value("${payshap.oauth.client-id}")
    private String clientId;
    
    @Value("${payshap.oauth.client-secret}")
    private String clientSecret;
    
    private String cachedToken;
    private LocalDateTime tokenExpiry;
    
    public String getOAuthToken() {
        // Check if cached token is still valid
        if (cachedToken != null && tokenExpiry.isAfter(LocalDateTime.now().plusMinutes(1))) {
            return cachedToken;
        }
        
        // Request new token
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "payment:send payment:query proxy:resolve");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<OAuthTokenResponse> response = restTemplate.postForEntity(
            tokenUrl,
            request,
            OAuthTokenResponse.class
        );
        
        OAuthTokenResponse tokenResponse = response.getBody();
        cachedToken = tokenResponse.getAccessToken();
        tokenExpiry = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());
        
        return cachedToken;
    }
}
```

**OAuth Token Request**:
```
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=bank001-payshap-client
&client_secret=SUPER_SECRET_KEY
&scope=payment:send payment:query proxy:resolve

Response 200 OK:
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "token_type": "Bearer",
  "expires_in": 300,  // 5 minutes
  "scope": "payment:send payment:query proxy:resolve"
}
```

### 4.5 PayShap Key Characteristics

✅ **Strengths**:
- Instant (< 10 seconds)
- 24/7/365 availability
- Mobile-to-mobile (no account numbers needed)
- Built-in notifications (SMS)
- Low cost
- Proxy registry (mobile/email)
- User-friendly

⚠️ **Limitations**:
- R 3,000 per transaction limit
- Requires proxy registration (one-time)
- P2P focus (not for business payments typically)
- Both banks must be PayShap participants

🔐 **Security**:
- OAuth 2.0 token authentication
- mTLS (certificate authentication)
- 5-minute token expiry
- Proxy privacy (account numbers hidden)

---

## 5. SWIFT Interaction Flow

**System**: SWIFT (Society for Worldwide Interbank Financial Telecommunication)  
**Type**: International cross-border payments  
**Use Case**: International wire transfers  
**Operator**: SWIFT SCRL (Belgium)  
**Settlement**: T+0 to T+5 (varies by correspondent banking)  
**Operating Hours**: 24/7/365  
**Network**: SWIFTNet

### 5.1 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PAYMENTS ENGINE                                 │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Payment Initiation Service                                     │ │
│  │  └─> FX Rate Service (ZAR → USD/EUR/etc.)                      │ │
│  │       └─> Sanctions Screening Service (MANDATORY)              │ │
│  │            └─> SWIFT Adapter Service                            │ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ SWIFT MT103 (legacy) OR
                                │ ISO 20022 pacs.008 (modern MX)
                                │ Over SWIFTNet (RMA/LAU auth)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         SWIFT NETWORK                                │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  Message Validation                                             │ │
│  │  └─> Routing via BIC codes                                     │ │
│  │       └─> Correspondent Bank(s)                                │ │
│  │            └─> Beneficiary Bank                                 │ │
│  │                 └─> Confirmation (MT910/pacs.002)               │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 Detailed Interaction Flow

#### Step 1: Payment Initiation with FX

```
Customer Request:
{
  "debitAccount": "62001234567",
  "debitCurrency": "ZAR",
  "debitAmount": 100000.00,  // R 100,000
  "beneficiaryName": "ABC Corporation Inc",
  "beneficiaryAccount": "GB29NWBK60161331926819",  // IBAN
  "beneficiaryBank": "NWBKGB2L",  // BIC/SWIFT code
  "beneficiaryBankName": "NatWest Bank PLC",
  "beneficiaryBankAddress": "London, United Kingdom",
  "creditCurrency": "GBP",  // British Pounds
  "purpose": "Payment for goods",
  "chargeBearer": "SHA"  // Shared charges
}

Pre-Processing:
```

```java
@Service
public class SwiftPaymentService {
    
    public SwiftPaymentResponse initiatePayment(SwiftPaymentRequest request) {
        // 1. Get FX rate (ZAR → GBP)
        FxRate fxRate = fxRateService.getRate("ZAR", "GBP");
        BigDecimal creditAmount = request.getDebitAmount().divide(fxRate.getRate(), 2, RoundingMode.HALF_UP);
        
        // R 100,000 ÷ 21.5 = £4,651.16
        
        // 2. MANDATORY: Sanctions screening
        SanctionsResult sanctions = sanctionsScreeningService.screenTransaction(
            request.getBeneficiaryName(),
            request.getBeneficiaryAccount(),
            request.getBeneficiaryBank(),
            creditAmount,
            "GBP"
        );
        
        if (sanctions.isMatch()) {
            // CRITICAL: Cannot proceed - sanctions hit
            throw new SanctionsViolationException(
                "Transaction blocked: Beneficiary matches sanctions list (" + 
                sanctions.getMatchedList() + ")"
            );
        }
        
        // 3. Validate correspondent banking route
        CorrespondentRoute route = correspondentBankService.getRoute(
            ourBankBic,  // e.g., "ABSAZAJJ" (Absa South Africa)
            request.getBeneficiaryBank()  // e.g., "NWBKGB2L"
        );
        
        // 4. Calculate fees
        SwiftFees fees = calculateSwiftFees(request, route);
        // Our bank fee: R 500
        // Correspondent bank fee: £25
        // Beneficiary bank fee: £15
        
        // 5. Debit customer account (debit amount + our fees)
        coreBankingService.debit(
            request.getDebitAccount(),
            request.getDebitAmount().add(fees.getOurFee()),
            "SWIFT payment + fees"
        );
        
        // 6. Build SWIFT message
        String swiftMessage = buildSwiftMT103(request, creditAmount, fxRate, route);
        
        // 7. Send to SWIFT
        return swiftAdapter.sendMessage(swiftMessage);
    }
}
```

#### Step 2: Sanctions Screening (MANDATORY)

```java
@Service
public class SanctionsScreeningService {
    
    @Autowired
    private RestTemplate sanctionsApiClient;
    
    public SanctionsResult screenTransaction(String name, String account, 
                                            String bic, BigDecimal amount, 
                                            String currency) {
        // Screen against multiple sanctions lists:
        // - OFAC (US Treasury - Office of Foreign Assets Control)
        // - UN Security Council Sanctions List
        // - EU Consolidated List
        // - SARB Sanctions List (South Africa)
        // - Local bank watch list
        
        SanctionsScreeningRequest request = SanctionsScreeningRequest.builder()
            .entityName(name)
            .accountNumber(account)
            .bankIdentifier(bic)
            .amount(amount)
            .currency(currency)
            .transactionType("CROSS_BORDER_WIRE")
            .build();
        
        ResponseEntity<SanctionsResult> response = sanctionsApiClient.postForEntity(
            sanctionsApiUrl + "/api/v1/screen",
            request,
            SanctionsResult.class
        );
        
        SanctionsResult result = response.getBody();
        
        // Log for compliance
        auditService.logSanctionsScreening(
            request,
            result,
            result.isMatch() ? "BLOCKED" : "CLEARED"
        );
        
        return result;
    }
}
```

**Sanctions API Response**:
```json
{
  "screeningId": "SCR-2025-1013-000456",
  "timestamp": "2025-10-13T14:10:00Z",
  "isMatch": false,  // No sanctions match
  "matchDetails": [],
  "listsScreened": [
    "OFAC_SDN",
    "UN_CONSOLIDATED",
    "EU_CONSOLIDATED",
    "SARB_SANCTIONS"
  ],
  "riskScore": 0,
  "status": "CLEARED"
}
```

**If Sanctions Match Found**:
```json
{
  "screeningId": "SCR-2025-1013-000457",
  "timestamp": "2025-10-13T14:11:00Z",
  "isMatch": true,  // MATCH FOUND!
  "matchDetails": [
    {
      "list": "OFAC_SDN",
      "matchedName": "ABC CORPORATION INC",
      "matchScore": 98,  // 98% match
      "sanctionType": "BLOCKED",
      "addedDate": "2024-05-15",
      "reason": "Terrorism financing"
    }
  ],
  "status": "BLOCKED",
  "action": "DO_NOT_PROCESS"
}
```

#### Step 3: SWIFT MT103 Message Construction

**MT103 (Single Customer Credit Transfer) - Legacy Format**:
```
{1:F01ABSAZAJJAXXX0000000000}  // Basic Header (sender BIC)
{2:I103NWBKGB2LXXXXN}          // Application Header (MT103 to NatWest)
{3:{108:SWIFT REF 123456}}     // User Header (message reference)
{4:
:20:PAY2025101314100456        // Transaction Reference
:23B:CRED                      // Bank Operation Code (Credit)
:32A:251013GBP4651,16          // Value Date, Currency, Amount
:33B:ZAR100000,00              // Original Currency and Amount
:50K:/62001234567              // Ordering Customer (our customer)
ABC CORPORATION PTY LTD
123 MAIN STREET
JOHANNESBURG, 2000
SOUTH AFRICA
:52A:ABSAZAJJXXX               // Ordering Institution (our bank)
:53A:CITIUS33XXX               // Sender's Correspondent (Citibank New York)
:57A:NWBKGB2LXXX               // Account With Institution (NatWest)
:59:/GB29NWBK60161331926819    // Beneficiary Customer
ABC CORPORATION INC
456 HIGH STREET
LONDON, SW1A 1AA
UNITED KINGDOM
:70:PAYMENT FOR GOODS          // Remittance Information
INVOICE INV-12345
:71A:SHA                       // Details of Charges (Shared)
:72:/ACC/SANCTIONS CLEARED     // Sender to Receiver Information
SANCTIONS SCREENING ID: SCR-2025-1013-000456
-}
{5:{CHK:ABCD1234EFGH}}         // Trailer (checksum)
```

**pacs.008 (Modern ISO 20022 MX format)** - Equivalent:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:swift:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>PAY2025101314100456</MsgId>
      <CreDtTm>2025-10-13T14:10:04Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <TtlIntrBkSttlmAmt Ccy="GBP">4651.16</TtlIntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-13</IntrBkSttlmDt>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <InstrId>PAY2025101314100456</InstrId>
        <EndToEndId>PAY2025101314100456</EndToEndId>
        <TxId>SWIFT-2025-000456</TxId>
      </PmtId>
      <IntrBkSttlmAmt Ccy="GBP">4651.16</IntrBkSttlmAmt>
      <IntrBkSttlmDt>2025-10-13</IntrBkSttlmDt>
      <ChrgBr>SHAR</ChrgBr>  <!-- Shared charges -->
      <InstgAgt>
        <FinInstnId>
          <BICFI>ABSAZAJJXXX</BICFI>  <!-- Our bank -->
        </FinInstnId>
      </InstgAgt>
      <InstdAgt>
        <FinInstnId>
          <BICFI>NWBKGB2LXXX</BICFI>  <!-- NatWest -->
        </FinInstnId>
      </InstdAgt>
      <IntrmyAgt1>
        <FinInstnId>
          <BICFI>CITIUS33XXX</BICFI>  <!-- Citibank correspondent -->
        </FinInstnId>
      </IntrmyAgt1>
      <Dbtr>
        <Nm>ABC Corporation Pty Ltd</Nm>
        <PstlAdr>
          <StrtNm>123 Main Street</StrtNm>
          <TwnNm>Johannesburg</TwnNm>
          <Ctry>ZA</Ctry>
        </PstlAdr>
      </Dbtr>
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>62001234567</Id>
          </Othr>
        </Id>
      </DbtrAcct>
      <Cdtr>
        <Nm>ABC Corporation Inc</Nm>
        <PstlAdr>
          <StrtNm>456 High Street</StrtNm>
          <TwnNm>London</TwnNm>
          <PstCd>SW1A 1AA</PstCd>
          <Ctry>GB</Ctry>
        </PstlAdr>
      </Cdtr>
      <CdtrAcct>
        <Id>
          <IBAN>GB29NWBK60161331926819</IBAN>
        </Id>
      </CdtrAcct>
      <RmtInf>
        <Ustrd>PAYMENT FOR GOODS INVOICE INV-12345</Ustrd>
      </RmtInf>
      <!-- Sanctions screening reference -->
      <SplmtryData>
        <Envlp>
          <SanctionsCleared>
            <ScreeningId>SCR-2025-1013-000456</ScreeningId>
            <Status>CLEARED</Status>
          </SanctionsCleared>
        </Envlp>
      </SplmtryData>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

#### Step 4: SWIFT Transmission

```java
@Service
public class SwiftAdapter {
    
    @Autowired
    private SwiftNetConnection swiftNetConnection;
    
    public SwiftResponse sendMT103(String mt103Message) {
        // 1. Authenticate to SWIFTNet
        // Uses RMA (Relationship Management Application)
        // and LAU (Login Authentication)
        swiftNetConnection.authenticate();
        
        // 2. Send message
        SwiftMessage message = SwiftMessage.builder()
            .messageType("MT103")
            .senderBic(ourBankBic)
            .receiverBic(beneficiaryBankBic)
            .content(mt103Message)
            .priority("NORMAL")  // or "URGENT"
            .build();
        
        SwiftAck ack = swiftNetConnection.send(message);
        
        // 3. Process acknowledgment
        if (ack.isAccepted()) {
            // Message accepted by SWIFT network
            return SwiftResponse.builder()
                .status("ACCEPTED")
                .swiftReference(ack.getReference())
                .timestamp(ack.getTimestamp())
                .build();
        } else {
            // Message rejected
            throw new SwiftValidationException(ack.getErrorCode(), ack.getErrorDescription());
        }
    }
}
```

#### Step 5: SWIFT Network Processing

**Multi-Hop Routing**:
```
1. Our Bank (ABSAZAJJXXX) - Johannesburg, South Africa
   └─> Sends MT103
   
2. SWIFT Network validates message
   ✅ MT103 format valid
   ✅ BIC codes valid
   ✅ Mandatory fields present
   └─> Routes to correspondent bank

3. Correspondent Bank (CITIUS33XXX) - Citibank New York, USA
   └─> Receives MT103
   └─> Performs intermediary processing
   └─> Deducts correspondent fee (£25)
   └─> Forwards to final beneficiary bank

4. Beneficiary Bank (NWBKGB2LXXX) - NatWest London, UK
   └─> Receives MT103
   └─> Validates beneficiary account (GB29NWBK60161331926819)
   └─> Credits beneficiary account (£4,651.16 - £15 fee = £4,636.16)
   └─> Sends MT910 confirmation
```

**Timing**:
- SWIFT Network: < 5 minutes (message delivery)
- Correspondent Bank: 0-24 hours (processing)
- Beneficiary Bank: 0-48 hours (credit to account)
- **Total**: T+0 to T+3 (typically T+1 for major banks)

#### Step 6: Confirmations

**MT910 (Confirmation of Credit)**:
```
{1:F01NWBKGB2LAXXX0000000000}
{2:O910ABSAZAJJXXXXN}
{4:
:20:NWBK20251014000789         // NatWest's reference
:21:PAY2025101314100456        // Related reference (our MT103 ref)
:32A:251014GBP4636,16          // Value date and amount credited
:52A:NWBKGB2LXXX               // Account servicing institution
:59:/GB29NWBK60161331926819    // Beneficiary account
ABC CORPORATION INC
:72:/FEES/GBP15,00             // Fee deducted
-}
```

**Payments Engine Processing**:
```java
@Service
public class SwiftConfirmationProcessor {
    
    public void processMT910(String mt910Message) {
        MT910 confirmation = parseMT910(mt910Message);
        
        // 1. Find original payment
        Payment payment = paymentRepository.findBySwiftReference(
            confirmation.getRelatedReference()  // PAY2025101314100456
        );
        
        // 2. Update status
        payment.setStatus(PaymentStatus.CREDITED_TO_BENEFICIARY);
        payment.setBeneficiaryCreditDate(confirmation.getValueDate());
        payment.setBeneficiaryCreditAmount(confirmation.getAmount());
        payment.setBeneficiaryBankFee(confirmation.getFeeAmount());
        
        // 3. Calculate total cost
        // Debit: R 100,500 (R 100,000 + R 500 our fee)
        // Credit: £4,636.16 (£4,651.16 - £15 beneficiary fee)
        // Correspondent fee: £25 (already deducted)
        
        paymentRepository.save(payment);
        
        // 4. Publish event
        eventPublisher.publish("swift-payment-completed", 
            new SwiftPaymentCompletedEvent(payment));
        
        // 5. Notify customer
        notificationService.sendSwiftCompletionNotification(payment);
    }
}
```

#### Step 7: Reconciliation

**SWIFT Statement (MT940) - End of Day**:
```
{1:F01ABSAZAJJAXXX0000000000}
{2:O940ABSAZAJJXXXXN}
{4:
:20:STMT-2025-10-14            // Statement reference
:25:ABSAZAJJXXX/USD001         // Account (our USD nostro account)
:28C:00001/001                 // Statement number
:60F:C251013USD1000000,00      // Opening balance
:61:2510140CT100000,00NSWIFTPAY2025101314100456// Transaction (Debit)
:86:Customer payment to UK
Beneficiary: ABC Corporation Inc
Our ref: PAY2025101314100456
:62F:C251014USD900000,00       // Closing balance
:64:C251014USD900000,00        // Available balance
-}
```

### 5.3 SWIFT Key Characteristics

✅ **Strengths**:
- Global reach (11,000+ banks, 200+ countries)
- Trusted and secure (SWIFTNet PKI)
- Standardized messaging (MT/MX)
- Correspondent banking network
- Audit trail and tracking
- ISO 20022 migration (modern MX messages)

⚠️ **Limitations**:
- Slow (T+1 to T+3 typically)
- Expensive (multiple fees: sender, correspondent, beneficiary)
- Store-and-forward (not real-time)
- Requires correspondent banking relationships
- Complex routing (multi-hop)
- No amount guarantee (fees deducted at each hop)

🔐 **Security**:
- SWIFTNet PKI certificates
- RMA (Relationship Management Application)
- LAU (Login Authentication)
- Message encryption
- Digital signatures
- **Mandatory sanctions screening**

---

## 6. Comparison Matrix

### 6.1 Overview Comparison

| Attribute | SAMOS | BankservAfrica | RTC | PayShap | SWIFT |
|-----------|-------|----------------|-----|---------|-------|
| **Type** | RTGS | ACH/Batch | Real-time | Instant P2P | International Wire |
| **Settlement** | Real-time | T+0 / T+1 | < 30 sec | < 10 sec | T+1 to T+3 |
| **Operator** | SARB | BankservAfrica | BankservAfrica | PayShap | SWIFT SCRL |
| **Amount Limit** | > R 5M | No limit | < R 5M | R 3,000 | No limit |
| **Hours** | 07:00-17:00 | Cut-offs | 24/7 | 24/7 | 24/7 |
| **Format** | ISO 20022 | Proprietary | ISO 20022 | ISO 20022 | MT/MX |
| **Protocol** | HTTPS + mTLS | SFTP + PGP | HTTPS + mTLS | HTTPS + OAuth | SWIFTNet |
| **Cost** | High | Low | Medium | Low | Very High |
| **Reversibility** | Irrevocable | Hard | Hard | Hard | Hard |
| **Retry** | No | Yes (3x) | Once | No | Yes (3x) |

### 6.2 Technical Comparison

| Attribute | SAMOS | BankservAfrica | RTC | PayShap | SWIFT |
|-----------|-------|----------------|-----|---------|-------|
| **API Type** | REST | File-based (SFTP) | REST | REST | Proprietary |
| **Message Format** | XML (ISO 20022) | Fixed-width text | XML (ISO 20022) | JSON + XML | MT (text) / MX (XML) |
| **Authentication** | mTLS + XMLDSig | SSH key + PGP | mTLS | OAuth 2.0 + mTLS | RMA + LAU |
| **Timeout** | 30 seconds | N/A (async) | 30 seconds | 30 seconds | N/A (async) |
| **Response Type** | Synchronous | Asynchronous (file) | Synchronous | Synchronous | Asynchronous |
| **Sanctions Screening** | Recommended | Recommended | Recommended | Recommended | **MANDATORY** |
| **Settlement Method** | Immediate | Batch netting | Real-time | Real-time | Correspondent |

### 6.3 Use Case Recommendation

| Scenario | Recommended Rail | Reason |
|----------|------------------|--------|
| High-value urgent (> R 5M) | **SAMOS** | Real-time, irrevocable, SARB-operated |
| Salary payments (10K employees) | **BankservAfrica** | High volume, batch efficiency, low cost |
| Instant customer payment | **RTC** | Real-time, low-value, 24/7 |
| P2P mobile payment | **PayShap** | Mobile-to-mobile, no account numbers, instant |
| International payment | **SWIFT** | Cross-border, global reach, trusted |
| B2B supplier payment (same day) | **RTC or BankservAfrica** | Depends on urgency and cut-off times |
| Property purchase | **SAMOS** | High-value, immediate settlement, irrevocable |

---

## 7. Error Handling Patterns

### 7.1 Common Error Scenarios

#### Scenario 1: Insufficient Funds

**SAMOS**:
```
Error: NOAS (No Settlement Account Funds)
Action:
1. Update payment status: FAILED
2. Reverse customer debit immediately
3. Notify customer: "Payment failed - insufficient settlement funds. Please try again."
4. Alert ops team if recurring (liquidity issue)
```

**BankservAfrica**:
```
Response Code: 2005 (Insufficient Funds)
Action:
1. Mark payment as REJECTED in reconciliation
2. Reverse customer debit
3. Offer retry option if customer now has funds
```

#### Scenario 2: Invalid Account

**RTC**:
```
Error: AC03 (Invalid Creditor Account Number)
Action:
1. Update payment status: FAILED
2. Reverse customer debit immediately
3. Notify customer: "Beneficiary account number is invalid. Please verify and retry."
4. Log for fraud detection (potential typo or fraudulent account)
```

**PayShap**:
```
Error: PROXY_NOT_FOUND
Action:
1. Return error before debiting customer
2. Notify customer: "Beneficiary mobile number not registered on PayShap. Ask beneficiary to register."
3. Suggest alternative payment rail (RTC/EFT)
```

#### Scenario 3: Timeout

**SAMOS/RTC**:
```
Error: HTTP 504 Gateway Timeout (> 30 seconds)
Action:
1. Mark payment as PENDING_CONFIRMATION
2. DO NOT reverse debit yet (unknown state)
3. Query payment status using original reference:
   GET /api/v1/payments/status?ref=PAY-2025-000456
4. If settled: Update to SETTLED
5. If not found after 5 minutes: Reverse debit, mark FAILED
6. Alert ops team for manual reconciliation
```

**SWIFT**:
```
Error: No MT910 confirmation received after 3 business days
Action:
1. Query SWIFT tracker: https://www.swift.com/our-solutions/compliance-and-shared-services/financial-crime-compliance/sanctions-screening/tracker
2. Contact correspondent bank
3. If still pending: Escalate to relationship manager
4. If lost: Initiate SWIFT investigation (can take 2-4 weeks)
```

#### Scenario 4: Sanctions Match

**SWIFT Only**:
```
Sanctions Result: MATCH (OFAC SDN List)
Action:
1. IMMEDIATELY halt payment processing
2. DO NOT DEBIT customer account
3. Update payment status: BLOCKED_SANCTIONS
4. Notify compliance officer
5. File SAR (Suspicious Activity Report) if required
6. Inform customer: "Payment cannot be processed due to compliance requirements."
7. DO NOT disclose specific sanctions list or reason
```

### 7.2 Retry Strategy Matrix

| Clearing System | Retry Allowed? | Max Retries | Backoff Strategy |
|-----------------|----------------|-------------|------------------|
| SAMOS | ❌ No | 0 | N/A (Immediate accept/reject) |
| BankservAfrica | ✅ Yes | 3 | Exponential (1min, 5min, 15min) |
| RTC | ⚠️ Once | 1 | Only on beneficiary bank timeout |
| PayShap | ❌ No | 0 | N/A (Instant accept/reject) |
| SWIFT | ✅ Yes | 3 | Manual (after investigation) |

---

## 8. Reconciliation Patterns

### 8.1 Daily Reconciliation Workflow

**Time**: 18:00 daily (after all cut-offs)

```java
@Scheduled(cron = "0 0 18 * * MON-FRI")
public void performDailyReconciliation() {
    LocalDate today = LocalDate.now();
    
    // 1. SAMOS Reconciliation
    reconcileSamos(today);
    
    // 2. BankservAfrica Reconciliation
    reconcileBankserv(today);
    
    // 3. RTC Reconciliation
    reconcileRtc(today);
    
    // 4. PayShap Reconciliation
    reconcilePayShap(today);
    
    // 5. SWIFT Reconciliation (last 3 days)
    reconcileSwift(today.minusDays(3), today);
    
    // 6. Generate consolidated report
    generateConsolidatedReport(today);
    
    // 7. Alert if mismatches
    if (hasMismatches()) {
        alertOpsTeam();
    }
}
```

### 8.2 Three-Way Reconciliation

**For Each Payment**:
```
Match 3 sources:
1. Our Database (payments table)
2. Clearing System Statement (camt.053 or equivalent)
3. Core Banking System (nostro account movements)

If all 3 match:
  ✅ Status: RECONCILED
  
If 2 match, 1 differs:
  ⚠️ Status: INVESTIGATE
  Action: Manual review
  
If all 3 differ:
  🚨 Status: CRITICAL_MISMATCH
  Action: Immediate escalation
```

### 8.3 Reconciliation KPIs

| KPI | Target | Alert Threshold |
|-----|--------|-----------------|
| Match Rate | > 99.9% | < 99% |
| Unmatched Payments | < 5 per day | > 10 per day |
| Amount Mismatch | < R 1,000 | > R 10,000 |
| Settlement Timing Variance | < 1 hour | > 4 hours |
| Reconciliation Duration | < 30 minutes | > 60 minutes |

---

## Summary

This document provides comprehensive interaction flows for all 5 clearing systems:

1. **SAMOS (RTGS)**: High-value, real-time, irrevocable, 07:00-17:00, ISO 20022, mTLS
2. **BankservAfrica (EFT)**: Batch processing, file-based, SFTP+PGP, multiple cut-offs, T+0/T+1
3. **RTC**: Real-time, low-value, 24/7, synchronous API, < 30 seconds
4. **PayShap**: Instant P2P, proxy-based (mobile/email), R 3,000 limit, OAuth 2.0, < 10 seconds
5. **SWIFT**: International, multi-hop routing, correspondent banking, T+1-T+3, **mandatory sanctions**

**Key Takeaways**:
- Each rail has unique characteristics, timing, and error handling
- SAMOS and PayShap are irrevocable (no retries)
- BankservAfrica is async (response files)
- RTC and PayShap are instant confirmation
- SWIFT requires sanctions screening (mandatory)
- Reconciliation is critical (daily, three-way matching)
- Error handling varies by rail (timeout strategies differ)

**For AI Agents**: This document provides sufficient context to build clearing adapters with complete understanding of message formats, timing, error codes, and reconciliation requirements per payment rail.

---

**Document Version**: 1.0  
**Created**: 2025-10-13  
**Total Lines**: 3,200+  
**Coverage**: 5 clearing systems, complete flows, error handling, reconciliation  
**Status**: ✅ Complete
