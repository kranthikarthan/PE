# Clearing System Onboarding & Configuration - Design Document

## Overview

This document details the **self-service clearing system onboarding** feature, allowing banks/FIs to configure their integration with South African and international clearing systems through a React frontend, supporting multiple patterns, formats, security mechanisms, and retry policies per payment rail.

**Feature ID**: New Feature (Phase 7 Enhancement)  
**Version**: 1.0  
**Date**: 2025-10-12  
**Status**: ✅ DESIGNED - Ready for Implementation

---

## Table of Contents

1. [Business Requirements](#business-requirements)
2. [Clearing Systems Overview](#clearing-systems-overview)
3. [World Standards](#world-standards)
4. [South African Standards](#south-african-standards)
5. [Configuration Options](#configuration-options)
6. [Architecture Design](#architecture-design)
7. [Database Schema](#database-schema)
8. [Backend Implementation](#backend-implementation)
9. [Frontend Implementation](#frontend-implementation)
10. [Security Patterns](#security-patterns)
11. [Testing Strategy](#testing-strategy)

---

## 1. Business Requirements

### 1.1 Current Limitations

**Hardcoded Integration**:
- ❌ Clearing adapter settings hardcoded in application.yml
- ❌ No self-service configuration
- ❌ Cannot change communication pattern without code deployment
- ❌ Single security mechanism per clearing system
- ❌ Fixed retry policy

**Example (Current - Hardcoded)**:
```yaml
# application.yml
clearing:
  samos:
    endpoint: https://samos.sarb.co.za/rtgs
    format: ISO_20022
    pattern: SYNCHRONOUS
    security: MTLS
    certificate-path: /etc/certs/samos.p12
    timeout: 30000
    retry-count: 3
```

---

### 1.2 Required Capabilities

**Self-Service Onboarding**:
1. ✅ Configure clearing system connection via React UI
2. ✅ Select communication pattern (sync vs async)
3. ✅ Select message format (XML, JSON, ISO 8583, ISO 20022)
4. ✅ Configure security mechanism (mTLS, OAuth 2.0, API Key, Certificate)
5. ✅ Configure retry policy (count, backoff, timeout)
6. ✅ Test connection before activation
7. ✅ Manage multiple environments (dev, UAT, prod)

---

## 2. Clearing Systems Overview

### 2.1 South African Payment Rails

| System | Owner | Payment Type | Standard | Typical Pattern | Format |
|--------|-------|--------------|----------|-----------------|--------|
| **SAMOS** | SARB | RTGS (High-Value) | ISO 20022 | Synchronous | XML (pacs.008) |
| **BankservAfrica** | BankservAfrica | ACH/EFT (Batch) | Proprietary + ISO 8583 | Asynchronous | Fixed-length + XML |
| **RTC** | BankservAfrica | Real-Time Clearing | ISO 8583 + ISO 20022 | Synchronous | XML/Binary |
| **PayShap** | BankservAfrica | Instant P2P | ISO 20022 | Synchronous | JSON (REST) + XML |
| **SWIFT** | SWIFT | International | ISO 15022/20022 | Asynchronous | XML (MT103/pacs.008) |

---

### 2.2 International Standards

| Standard | Description | Format | Use Case |
|----------|-------------|--------|----------|
| **ISO 20022** | Universal financial messaging | XML | RTGS, SWIFT, PayShap |
| **ISO 8583** | Card/ATM transactions | Binary/ASCII | RTC, Card payments |
| **SWIFT MT** | SWIFT Message Types (legacy) | Fixed-length | International payments (MT103) |
| **SWIFT MX** | SWIFT XML messages (new) | XML (ISO 20022) | International payments (pacs.008) |
| **FIX Protocol** | Financial Information eXchange | Tag-value | Securities trading |

---

## 3. World Standards

### 3.1 ISO 20022 (Universal Financial Messaging)

**Regions**: Global (adopted by 70+ countries)  
**Format**: XML  
**Message Types**:
- `pacs.008` - Customer Credit Transfer
- `pacs.009` - Financial Institution Credit Transfer
- `pacs.002` - Payment Status Report
- `pain.001` - Customer Credit Transfer Initiation
- `camt.053` - Bank-to-Customer Statement

**Example (pacs.008)**:
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>SAMOS-2025-001</MsgId>
      <CreDtTm>2025-10-12T10:00:00Z</CreDtTm>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <InstrId>TXN-123</InstrId>
        <EndToEndId>E2E-456</EndToEndId>
      </PmtId>
      <IntrBkSttlmAmt Ccy="ZAR">1000.00</IntrBkSttlmAmt>
      <Dbtr><Nm>John Doe</Nm></Dbtr>
      <DbtrAcct><Id><IBAN>ZA1234567890</IBAN></Id></DbtrAcct>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>
```

**Communication Pattern**: Synchronous (RTGS) or Asynchronous (SWIFT)  
**Security**: mTLS (mutual TLS) + Digital Signatures  
**Transport**: HTTPS, SFTP, MQ Series

---

### 3.2 SWIFT (Society for Worldwide Interbank Financial Telecommunication)

**Regions**: Global (11,000+ institutions, 200+ countries)  
**Formats**: MT (legacy), MX (ISO 20022)  
**Message Types**:
- `MT103` - Single Customer Credit Transfer (legacy)
- `pacs.008` - Customer Credit Transfer (ISO 20022)

**Example (MT103 - Legacy)**:
```
{1:F01ABCDZAJJXXXX0000000000}{2:O1030919210312XYZDZAJJXXXX00000000002103120919N}
{3:{108:MT103}}
{4:
:20:TRANSACTION-123
:23B:CRED
:32A:210312ZAR1000,00
:50K:JOHN DOE
ACCOUNT 1234567890
:59:JANE SMITH
ACCOUNT 0987654321
:71A:SHA
-}
```

**Example (pacs.008 - ISO 20022)**:
```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
  <FIToFICstmrCdtTrf>
    <!-- Same structure as ISO 20022 above -->
  </FIToFICstmrCdtTrf>
</Document>
```

**Communication Pattern**: Asynchronous (store-and-forward)  
**Security**: SWIFTNet PKI, Relationship Management Application (RMA)  
**Transport**: SWIFTNet, FileAct, InterAct

---

### 3.3 ISO 8583 (Card/ATM Transactions)

**Regions**: Global (card networks: Visa, Mastercard)  
**Format**: Binary or ASCII  
**Message Types**:
- `0200` - Financial Transaction Request
- `0210` - Financial Transaction Response
- `0420` - Reversal Request
- `0800` - Network Management Request

**Example (0200 - Authorization Request)**:
```
ISO 8583 Message:
MTI: 0200 (Authorization Request)
Bitmap: 7234000102C08801
Field 2: 4111111111111111 (PAN)
Field 3: 000000 (Processing Code: Purchase)
Field 4: 000000001000 (Amount: R10.00)
Field 7: 1012100000 (Transmission Date/Time)
Field 11: 123456 (STAN)
Field 12: 100000 (Local Time)
Field 13: 1012 (Local Date)
Field 37: 123456789012 (Retrieval Reference Number)
Field 41: 12345678 (Terminal ID)
Field 49: 710 (Currency Code: ZAR)
```

**Communication Pattern**: Synchronous (real-time authorization)  
**Security**: PIN encryption (3DES), MAC (Message Authentication Code)  
**Transport**: TCP/IP, TLS

---

## 4. South African Standards

### 4.1 SAMOS (South African Multiple Option Settlement)

**Owner**: South African Reserve Bank (SARB)  
**Purpose**: Real-Time Gross Settlement (RTGS) for high-value payments (>R5 million)  
**Standard**: ISO 20022  
**Format**: XML (pacs.008, pacs.009, camt.053)

**Configuration Options**:
```yaml
Communication Pattern: SYNCHRONOUS
Message Format: ISO_20022_XML
Security: MTLS + DIGITAL_SIGNATURE
Transport: HTTPS REST API
Timeout: 30 seconds (hard limit by SARB)
Retry: 0 (no retries allowed for RTGS)
Idempotency: Required (via MsgId)
```

**Endpoints**:
```
POST https://samos.sarb.co.za/api/v1/payments          (Submit payment)
GET  https://samos.sarb.co.za/api/v1/payments/{msgId}  (Query status)
POST https://samos.sarb.co.za/api/v1/payments/{msgId}/cancel  (Cancel payment)
```

**Security Requirements**:
- ✅ Mutual TLS (client certificate issued by SARB)
- ✅ Digital signature (XMLDSig) on pacs.008 message
- ✅ IP whitelisting (bank's static IP)

**Response Codes**:
```
ACSC - AcceptedSettlementCompleted
RJCT - Rejected
PDNG - Pending
ACCP - AcceptedCustomerProfile
```

---

### 4.2 BankservAfrica (ACH/EFT)

**Owner**: BankservAfrica  
**Purpose**: Automated Clearing House for bulk EFT payments  
**Standard**: Proprietary + ISO 8583  
**Format**: Fixed-length records (ACH file) + XML acknowledgment

**Configuration Options**:
```yaml
Communication Pattern: ASYNCHRONOUS (file-based)
Message Format: ACH_FIXED_LENGTH + XML_ACK
Security: SFTP + PGP_ENCRYPTION
Transport: SFTP (Secure File Transfer Protocol)
Timeout: N/A (batch processing)
Retry: 3 attempts (file upload)
Batch Window: 08:00-16:00 SAST (Mon-Fri)
```

**File Format (ACH - Fixed Length)**:
```
Record Type 1 (Header):
Pos 1-1:   Record Type = "1"
Pos 2-11:  Generation Number (0000000001)
Pos 12-19: Date (YYYYMMDD)
Pos 20-35: Originating Bank Code (ABSA0001234567)

Record Type 2 (Transaction):
Pos 1-1:   Record Type = "2"
Pos 2-3:   Transaction Code (20 = Debit)
Pos 4-13:  Account Number (1234567890)
Pos 14-23: Amount (0000001000 = R10.00)
Pos 24-53: Beneficiary Name (JOHN DOE)
```

**Endpoints**:
```
SFTP: sftp://ach.bankserv.co.za/uploads/      (Upload ACH file)
SFTP: sftp://ach.bankserv.co.za/downloads/    (Download acknowledgment)
REST: https://api.bankserv.co.za/ach/status   (Query batch status)
```

**Security Requirements**:
- ✅ SFTP with SSH key authentication
- ✅ PGP encryption (ACH file encrypted with BankservAfrica's public key)
- ✅ Digital signature (detached .sig file)

**Response Files**:
```
ACH_ACK_20251012_001.xml  (Acknowledgment: Accepted, Rejected, Partial)
ACH_RECON_20251012_001.xml (Reconciliation: Settled, Failed)
```

---

### 4.3 RTC (Real-Time Clearing)

**Owner**: BankservAfrica  
**Purpose**: Real-time low-value payments (<R5 million)  
**Standard**: ISO 8583 + ISO 20022  
**Format**: Binary (ISO 8583) or XML (ISO 20022)

**Configuration Options**:
```yaml
Communication Pattern: SYNCHRONOUS
Message Format: ISO_8583_BINARY or ISO_20022_XML
Security: MTLS + MAC
Transport: TCP/IP over TLS or HTTPS REST API
Timeout: 10 seconds
Retry: 1 retry (if timeout or network error)
Idempotency: Required (via STAN - System Trace Audit Number)
```

**ISO 8583 Message Flow**:
```
Bank → RTC:  0200 (Authorization Request)
RTC → Bank:  0210 (Authorization Response)
Bank → RTC:  0420 (Reversal - if needed)
RTC → Bank:  0430 (Reversal Response)
```

**Endpoints**:
```
TCP: rtc.bankserv.co.za:7000            (ISO 8583 binary)
REST: https://rtc-api.bankserv.co.za/v1/payments  (ISO 20022 XML over REST)
```

**Security Requirements**:
- ✅ Mutual TLS (client certificate)
- ✅ MAC (Message Authentication Code) on ISO 8583 messages
- ✅ IP whitelisting

**Response Codes** (ISO 8583):
```
00 - Approved
01 - Refer to card issuer
05 - Do not honour
51 - Insufficient funds
91 - Issuer unavailable
```

---

### 4.4 PayShap (Instant P2P Payments)

**Owner**: BankservAfrica (operates on behalf of PASA)  
**Purpose**: Instant person-to-person payments (24/7/365)  
**Standard**: ISO 20022 + REST API  
**Format**: JSON (REST API) or XML (ISO 20022)

**Configuration Options**:
```yaml
Communication Pattern: SYNCHRONOUS
Message Format: JSON_REST or ISO_20022_XML
Security: OAUTH_2_0 + MTLS
Transport: HTTPS REST API
Timeout: 5 seconds (instant payments)
Retry: 0 (no retries for instant payments)
Idempotency: Required (via transaction ID)
```

**REST API Endpoints**:
```
POST https://api.payshap.co.za/v1/payments         (Initiate payment)
GET  https://api.payshap.co.za/v1/payments/{txnId} (Query status)
POST https://api.payshap.co.za/v1/proxy/lookup     (Lookup recipient by phone/email)
```

**Request Format (JSON)**:
```json
{
  "transactionId": "PAYSHAP-2025-001",
  "amount": {
    "value": 100.00,
    "currency": "ZAR"
  },
  "debtor": {
    "name": "John Doe",
    "account": {
      "iban": "ZA1234567890"
    }
  },
  "creditor": {
    "name": "Jane Smith",
    "proxyId": "+27821234567",  // Phone number
    "proxyType": "MOBILE"
  },
  "endToEndId": "E2E-456",
  "timestamp": "2025-10-12T10:00:00Z"
}
```

**Response Format (JSON)**:
```json
{
  "transactionId": "PAYSHAP-2025-001",
  "status": "COMPLETED",
  "completedAt": "2025-10-12T10:00:01Z",
  "responseCode": "00",
  "responseMessage": "Payment successful"
}
```

**Security Requirements**:
- ✅ OAuth 2.0 (client credentials grant)
- ✅ Mutual TLS (client certificate)
- ✅ JWT tokens (short-lived, 5 minutes)
- ✅ IP whitelisting

---

### 4.5 SWIFT (International Payments)

**Owner**: SWIFT (Society for Worldwide Interbank Financial Telecommunication)  
**Purpose**: International cross-border payments  
**Standard**: ISO 15022 (MT103) or ISO 20022 (pacs.008)  
**Format**: SWIFT MT (fixed-length) or MX (XML)

**Configuration Options**:
```yaml
Communication Pattern: ASYNCHRONOUS (store-and-forward)
Message Format: SWIFT_MT103 or ISO_20022_XML
Security: SWIFTNET_PKI + RMA
Transport: SWIFTNet (FileAct, InterAct) or HTTPS (SWIFT gpi)
Timeout: N/A (asynchronous)
Retry: 3 attempts (with exponential backoff)
Sanctions Screening: MANDATORY (OFAC, UN, EU)
```

**Message Types**:
```
MT103 - Single Customer Credit Transfer (legacy)
MT202 - Financial Institution Transfer
MT199 - Free Format Message
pacs.008 - Customer Credit Transfer (ISO 20022)
pacs.002 - Payment Status Report
```

**Endpoints**:
```
SWIFTNet: swift://fileact/payments         (FileAct - file transfer)
SWIFTNet: swift://interact/messages        (InterAct - real-time messaging)
SWIFT gpi: https://gpi.swift.com/v1/payments  (SWIFT gpi - tracker)
```

**Security Requirements**:
- ✅ SWIFTNet PKI (SWIFT-issued certificates)
- ✅ Relationship Management Application (RMA) - bilateral key exchange
- ✅ Digital signatures (LAU - Login Authentication)
- ✅ Sanctions screening (mandatory before submission)

**SWIFT gpi (Global Payments Innovation)**:
- Real-time payment tracking
- Same-day settlement (for gpi banks)
- Full transparency (fees, FX rates)
- Stop and recall functionality

---

## 5. Configuration Options

### 5.1 Configuration Matrix

| Configuration | Options | Description |
|---------------|---------|-------------|
| **Communication Pattern** | SYNCHRONOUS, ASYNCHRONOUS, BATCH | Request/response, store-forward, file-based |
| **Message Format** | ISO_20022_XML, ISO_8583_BINARY, SWIFT_MT, JSON_REST, ACH_FIXED | Message encoding |
| **Transport Protocol** | HTTPS_REST, TCP_TLS, SFTP, MQ_SERIES, SWIFTNET | Network transport |
| **Security Mechanism** | MTLS, OAUTH_2_0, API_KEY, CERTIFICATE_BASED, SWIFTNET_PKI | Authentication |
| **Retry Policy** | Count (0-10), Backoff (LINEAR, EXPONENTIAL), Timeout (5-300s) | Error handling |
| **Idempotency** | REQUIRED, OPTIONAL | Duplicate prevention |
| **Sanctions Screening** | MANDATORY, OPTIONAL, DISABLED | Compliance check |

---

### 5.2 Per Clearing System Configuration

#### SAMOS (RTGS)
```yaml
clearing_system: SAMOS
communication_pattern: SYNCHRONOUS
message_format: ISO_20022_XML
transport: HTTPS_REST
security:
  mechanism: MTLS
  certificate_path: /etc/certs/samos-client.p12
  certificate_password: ${SAMOS_CERT_PASSWORD}
  digital_signature: REQUIRED
  signature_algorithm: RSA_SHA256
endpoint:
  url: https://samos.sarb.co.za/api/v1/payments
  timeout: 30000  # 30 seconds (SARB hard limit)
retry:
  enabled: false  # No retries for RTGS
idempotency:
  enabled: true
  key_field: MsgId  # ISO 20022 message ID
sanctions_screening:
  enabled: false  # Not required for domestic RTGS
```

#### BankservAfrica (EFT/ACH)
```yaml
clearing_system: BANKSERV_EFT
communication_pattern: ASYNCHRONOUS
message_format: ACH_FIXED_LENGTH
transport: SFTP
security:
  mechanism: SFTP_KEY
  ssh_key_path: /etc/ssh/bankserv_rsa
  pgp_encryption: REQUIRED
  pgp_public_key_path: /etc/pgp/bankserv.pub
endpoint:
  url: sftp://ach.bankserv.co.za/uploads/
  batch_window_start: "08:00"  # SAST
  batch_window_end: "16:00"
retry:
  enabled: true
  max_attempts: 3
  backoff: EXPONENTIAL
  initial_delay: 60000  # 1 minute
idempotency:
  enabled: true
  key_field: GenerationNumber + TransactionId
```

#### RTC (Real-Time Clearing)
```yaml
clearing_system: RTC
communication_pattern: SYNCHRONOUS
message_format: ISO_8583_BINARY
transport: TCP_TLS
security:
  mechanism: MTLS
  certificate_path: /etc/certs/rtc-client.p12
  mac_algorithm: HMAC_SHA256
  mac_key: ${RTC_MAC_KEY}
endpoint:
  url: rtc.bankserv.co.za:7000
  timeout: 10000  # 10 seconds
retry:
  enabled: true
  max_attempts: 1  # Only 1 retry
  backoff: NONE
idempotency:
  enabled: true
  key_field: STAN  # System Trace Audit Number (Field 11)
```

#### PayShap (Instant P2P)
```yaml
clearing_system: PAYSHAP
communication_pattern: SYNCHRONOUS
message_format: JSON_REST
transport: HTTPS_REST
security:
  mechanism: OAUTH_2_0
  token_endpoint: https://auth.payshap.co.za/oauth/token
  client_id: ${PAYSHAP_CLIENT_ID}
  client_secret: ${PAYSHAP_CLIENT_SECRET}
  mtls_enabled: true
  certificate_path: /etc/certs/payshap-client.p12
endpoint:
  url: https://api.payshap.co.za/v1/payments
  timeout: 5000  # 5 seconds (instant payments)
retry:
  enabled: false  # No retries for instant payments
idempotency:
  enabled: true
  key_field: transactionId
```

#### SWIFT (International)
```yaml
clearing_system: SWIFT
communication_pattern: ASYNCHRONOUS
message_format: ISO_20022_XML  # or SWIFT_MT103
transport: SWIFTNET_FILEACT
security:
  mechanism: SWIFTNET_PKI
  certificate_path: /etc/swift/swift-client.p12
  rma_enabled: true  # Relationship Management Application
  lau_enabled: true  # Login Authentication
endpoint:
  url: swift://fileact/payments
  timeout: N/A  # Asynchronous (store-and-forward)
retry:
  enabled: true
  max_attempts: 3
  backoff: EXPONENTIAL
  initial_delay: 300000  # 5 minutes
sanctions_screening:
  enabled: true  # MANDATORY for SWIFT
  providers:
    - OFAC  # US Office of Foreign Assets Control
    - UN    # United Nations
    - EU    # European Union
  action_on_match: REJECT
idempotency:
  enabled: true
  key_field: EndToEndId  # ISO 20022 end-to-end ID
```

---

## 6. Architecture Design

### 6.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│              REACT OPERATIONS PORTAL (Clearing Admin)                    │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  Clearing System Onboarding Wizard (5 Steps):                     │  │
│  │  1. Select Clearing System (SAMOS, BankservAfrica, RTC, etc.)    │  │
│  │  2. Configure Communication Pattern (Sync/Async)                  │  │
│  │  3. Configure Security (mTLS, OAuth, Certificates)               │  │
│  │  4. Configure Retry Policy                                        │  │
│  │  5. Test Connection & Activate                                    │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │ REST API
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  TENANT MANAGEMENT SERVICE (Enhanced)                    │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  ClearingSystemConfigurationController                            │  │
│  │  ├─ POST /api/v1/clearing-systems (Create config)                │  │
│  │  ├─ GET /api/v1/clearing-systems (List configs)                  │  │
│  │  ├─ PUT /api/v1/clearing-systems/{id} (Update config)            │  │
│  │  └─ POST /api/v1/clearing-systems/{id}/test (Test connection)    │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │ Load Config
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        CLEARING ADAPTERS                                 │
│  ┌───────────┬───────────┬──────────┬──────────┬──────────┐             │
│  │  SAMOS    │ Bankserv  │   RTC    │ PayShap  │  SWIFT   │             │
│  │  Adapter  │  Adapter  │ Adapter  │ Adapter  │ Adapter  │             │
│  └─────┬─────┴─────┬─────┴────┬─────┴────┬─────┴────┬─────┘             │
│        │           │          │          │          │                    │
│  ┌─────▼───────────▼──────────▼──────────▼──────────▼─────┐             │
│  │  DynamicClearingAdapter (Config-Driven)                 │             │
│  │  ├─ Load config from database                           │             │
│  │  ├─ Apply communication pattern (sync/async)            │             │
│  │  ├─ Apply message format (XML/JSON/Binary)              │             │
│  │  ├─ Apply security mechanism (mTLS/OAuth/etc.)          │             │
│  │  └─ Apply retry policy                                  │             │
│  └──────────────────────────────────────────────────────────┘             │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │ Submit Payment
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      CLEARING SYSTEMS (External)                         │
│  ┌───────────┬───────────┬──────────┬──────────┬──────────┐             │
│  │  SAMOS    │ Bankserv  │   RTC    │ PayShap  │  SWIFT   │             │
│  │  (SARB)   │  (BsA)    │  (BsA)   │  (BsA)   │ (Global) │             │
│  └───────────┴───────────┴──────────┴──────────┴──────────┘             │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### 6.2 Onboarding Flow Sequence

```
Admin → React UI → Backend API → Database → Adapter Config → Test → Activate

1. Admin opens clearing system onboarding wizard
2. Step 1: Select clearing system (SAMOS, BankservAfrica, RTC, PayShap, SWIFT)
3. Step 2: Select communication pattern (Sync/Async/Batch)
4. Step 3: Select message format (XML/JSON/Binary/Fixed-length)
5. Step 4: Configure security:
   - mTLS: Upload client certificate (.p12)
   - OAuth 2.0: Enter client ID/secret
   - API Key: Enter API key
   - SSH Key: Upload SSH key (for SFTP)
6. Step 5: Configure retry policy:
   - Max attempts (0-10)
   - Backoff strategy (None, Linear, Exponential)
   - Timeout (5s-300s)
7. Step 6: Configure idempotency (field name)
8. Step 7: Test connection:
   - Backend sends test message to clearing system
   - Clearing system responds with success/failure
   - Display result in UI
9. Step 8: Activate configuration
10. Backend saves to database
11. Clearing adapter reloads configuration
```

---

## 7. Database Schema

### 7.1 New Table: Clearing System Configurations

```sql
CREATE TABLE clearing_system_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) NOT NULL,
    
    -- Clearing System Identification
    clearing_system VARCHAR(50) NOT NULL,  -- SAMOS, BANKSERV_EFT, RTC, PAYSHAP, SWIFT
    clearing_system_name VARCHAR(100) NOT NULL,  -- Display name
    payment_rail VARCHAR(50) NOT NULL,  -- RTGS, ACH, RTC, INSTANT_P2P, INTERNATIONAL
    
    -- Communication Pattern
    communication_pattern VARCHAR(20) NOT NULL,  -- SYNCHRONOUS, ASYNCHRONOUS, BATCH
    message_format VARCHAR(50) NOT NULL,  -- ISO_20022_XML, ISO_8583_BINARY, SWIFT_MT103, JSON_REST, ACH_FIXED
    transport_protocol VARCHAR(50) NOT NULL,  -- HTTPS_REST, TCP_TLS, SFTP, MQ_SERIES, SWIFTNET
    
    -- Endpoint Configuration
    endpoint_url VARCHAR(500) NOT NULL,
    endpoint_timeout_ms INTEGER DEFAULT 30000,
    
    -- Security Configuration
    security_mechanism VARCHAR(50) NOT NULL,  -- MTLS, OAUTH_2_0, API_KEY, CERTIFICATE_BASED, SWIFTNET_PKI
    security_config JSONB,  -- Certificate paths, OAuth credentials, API keys, etc.
    
    -- Retry Configuration
    retry_enabled BOOLEAN DEFAULT TRUE,
    retry_max_attempts INTEGER DEFAULT 3,
    retry_backoff_strategy VARCHAR(20) DEFAULT 'EXPONENTIAL',  -- NONE, LINEAR, EXPONENTIAL
    retry_initial_delay_ms INTEGER DEFAULT 1000,
    
    -- Idempotency Configuration
    idempotency_enabled BOOLEAN DEFAULT TRUE,
    idempotency_key_field VARCHAR(50),  -- MsgId, STAN, transactionId, etc.
    
    -- Sanctions Screening (for SWIFT)
    sanctions_screening_enabled BOOLEAN DEFAULT FALSE,
    sanctions_providers VARCHAR(100)[],  -- ['OFAC', 'UN', 'EU']
    
    -- Batch Configuration (for ACH/EFT)
    batch_window_start TIME,  -- 08:00 SAST
    batch_window_end TIME,    -- 16:00 SAST
    batch_enabled BOOLEAN DEFAULT FALSE,
    
    -- Environment
    environment VARCHAR(20) NOT NULL DEFAULT 'PRODUCTION',  -- DEV, UAT, PRODUCTION
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',  -- ACTIVE, INACTIVE, TESTING
    last_tested_at TIMESTAMP,
    last_test_result VARCHAR(20),  -- SUCCESS, FAILURE
    last_test_error TEXT,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    
    -- Constraints
    CONSTRAINT fk_clearing_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
    CONSTRAINT uk_tenant_clearing_env UNIQUE (tenant_id, clearing_system, environment),
    CONSTRAINT chk_clearing_system CHECK (clearing_system IN ('SAMOS', 'BANKSERV_EFT', 'BANKSERV_RTC', 'PAYSHAP', 'SWIFT')),
    CONSTRAINT chk_communication_pattern CHECK (communication_pattern IN ('SYNCHRONOUS', 'ASYNCHRONOUS', 'BATCH')),
    CONSTRAINT chk_security_mechanism CHECK (security_mechanism IN ('MTLS', 'OAUTH_2_0', 'API_KEY', 'CERTIFICATE_BASED', 'SWIFTNET_PKI', 'SFTP_KEY'))
);

-- Indexes
CREATE INDEX idx_clearing_tenant ON clearing_system_configurations(tenant_id);
CREATE INDEX idx_clearing_system ON clearing_system_configurations(clearing_system);
CREATE INDEX idx_clearing_status ON clearing_system_configurations(status);
CREATE INDEX idx_clearing_environment ON clearing_system_configurations(environment);

COMMENT ON TABLE clearing_system_configurations IS 
'Stores self-service clearing system configurations per tenant, supporting multiple patterns, formats, and security mechanisms';
```

---

### 7.2 Security Configuration (JSONB)

**Example for SAMOS (mTLS)**:
```json
{
  "mechanism": "MTLS",
  "certificatePath": "/etc/certs/samos-client.p12",
  "certificatePassword": "${SAMOS_CERT_PASSWORD}",
  "digitalSignature": {
    "enabled": true,
    "algorithm": "RSA_SHA256",
    "keyPath": "/etc/keys/samos-sign.key"
  },
  "ipWhitelist": ["196.23.45.67", "196.23.45.68"]
}
```

**Example for PayShap (OAuth 2.0 + mTLS)**:
```json
{
  "mechanism": "OAUTH_2_0",
  "tokenEndpoint": "https://auth.payshap.co.za/oauth/token",
  "clientId": "${PAYSHAP_CLIENT_ID}",
  "clientSecret": "${PAYSHAP_CLIENT_SECRET}",
  "tokenLifetimeSeconds": 300,
  "mtls": {
    "enabled": true,
    "certificatePath": "/etc/certs/payshap-client.p12",
    "certificatePassword": "${PAYSHAP_CERT_PASSWORD}"
  }
}
```

**Example for BankservAfrica (SFTP + PGP)**:
```json
{
  "mechanism": "SFTP_KEY",
  "sshKeyPath": "/etc/ssh/bankserv_rsa",
  "sshKeyPassphrase": "${BANKSERV_SSH_PASSPHRASE}",
  "pgpEncryption": {
    "enabled": true,
    "publicKeyPath": "/etc/pgp/bankserv.pub",
    "signatureEnabled": true,
    "signatureKeyPath": "/etc/pgp/bank-private.key"
  }
}
```

**Example for SWIFT (SWIFTNet PKI)**:
```json
{
  "mechanism": "SWIFTNET_PKI",
  "certificatePath": "/etc/swift/swift-client.p12",
  "certificatePassword": "${SWIFT_CERT_PASSWORD}",
  "rma": {
    "enabled": true,
    "keyExchangeAlgorithm": "RSA_2048"
  },
  "lau": {
    "enabled": true,
    "loginAuthenticationKey": "${SWIFT_LAU_KEY}"
  }
}
```

---

## 8. Backend Implementation

### 8.1 JPA Entity

```java
@Entity
@Table(name = "clearing_system_configurations")
public class ClearingSystemConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    // Clearing System
    @Enumerated(EnumType.STRING)
    @Column(name = "clearing_system", nullable = false)
    private ClearingSystem clearingSystem;
    
    @Column(name = "clearing_system_name", nullable = false)
    private String clearingSystemName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_rail", nullable = false)
    private PaymentRail paymentRail;
    
    // Communication
    @Enumerated(EnumType.STRING)
    @Column(name = "communication_pattern", nullable = false)
    private CommunicationPattern communicationPattern;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_format", nullable = false)
    private MessageFormat messageFormat;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_protocol", nullable = false)
    private TransportProtocol transportProtocol;
    
    // Endpoint
    @Column(name = "endpoint_url", nullable = false)
    private String endpointUrl;
    
    @Column(name = "endpoint_timeout_ms")
    private Integer endpointTimeoutMs = 30000;
    
    // Security
    @Enumerated(EnumType.STRING)
    @Column(name = "security_mechanism", nullable = false)
    private SecurityMechanism securityMechanism;
    
    @Type(JsonBinaryType.class)
    @Column(name = "security_config", columnDefinition = "jsonb")
    private SecurityConfig securityConfig;
    
    // Retry
    @Column(name = "retry_enabled")
    private Boolean retryEnabled = true;
    
    @Column(name = "retry_max_attempts")
    private Integer retryMaxAttempts = 3;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "retry_backoff_strategy")
    private BackoffStrategy retryBackoffStrategy = BackoffStrategy.EXPONENTIAL;
    
    @Column(name = "retry_initial_delay_ms")
    private Integer retryInitialDelayMs = 1000;
    
    // Idempotency
    @Column(name = "idempotency_enabled")
    private Boolean idempotencyEnabled = true;
    
    @Column(name = "idempotency_key_field")
    private String idempotencyKeyField;
    
    // Sanctions Screening
    @Column(name = "sanctions_screening_enabled")
    private Boolean sanctionsScreeningEnabled = false;
    
    @Type(JsonBinaryType.class)
    @Column(name = "sanctions_providers", columnDefinition = "varchar[]")
    private List<String> sanctionsProviders;
    
    // Batch
    @Column(name = "batch_window_start")
    private LocalTime batchWindowStart;
    
    @Column(name = "batch_window_end")
    private LocalTime batchWindowEnd;
    
    @Column(name = "batch_enabled")
    private Boolean batchEnabled = false;
    
    // Environment
    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private Environment environment = Environment.PRODUCTION;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClearingSystemStatus status = ClearingSystemStatus.INACTIVE;
    
    @Column(name = "last_tested_at")
    private Instant lastTestedAt;
    
    @Column(name = "last_test_result")
    private String lastTestResult;
    
    @Column(name = "last_test_error")
    private String lastTestError;
    
    // Metadata
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    // Getters, setters, equals, hashCode
}

// Enums
public enum ClearingSystem {
    SAMOS,           // SARB RTGS
    BANKSERV_EFT,    // BankservAfrica ACH/EFT
    BANKSERV_RTC,    // BankservAfrica Real-Time Clearing
    PAYSHAP,         // PayShap Instant P2P
    SWIFT            // SWIFT International
}

public enum PaymentRail {
    RTGS,            // Real-Time Gross Settlement
    ACH,             // Automated Clearing House
    RTC,             // Real-Time Clearing
    INSTANT_P2P,     // Instant Person-to-Person
    INTERNATIONAL    // Cross-border
}

public enum CommunicationPattern {
    SYNCHRONOUS,     // Request/response
    ASYNCHRONOUS,    // Store-and-forward
    BATCH            // File-based
}

public enum MessageFormat {
    ISO_20022_XML,   // ISO 20022 XML (pacs.008)
    ISO_8583_BINARY, // ISO 8583 binary
    ISO_8583_ASCII,  // ISO 8583 ASCII
    SWIFT_MT103,     // SWIFT MT103 (legacy)
    SWIFT_MX,        // SWIFT MX (ISO 20022)
    JSON_REST,       // JSON over REST API
    ACH_FIXED,       // Fixed-length ACH records
    CUSTOM           // Custom format
}

public enum TransportProtocol {
    HTTPS_REST,      // HTTPS REST API
    TCP_TLS,         // TCP over TLS
    SFTP,            // Secure File Transfer Protocol
    MQ_SERIES,       // IBM MQ Series
    SWIFTNET_FILEACT,// SWIFTNet FileAct
    SWIFTNET_INTERACT// SWIFTNet InterAct
}

public enum SecurityMechanism {
    MTLS,            // Mutual TLS
    OAUTH_2_0,       // OAuth 2.0 (client credentials)
    API_KEY,         // API Key in header
    CERTIFICATE_BASED, // Certificate-based authentication
    SWIFTNET_PKI,    // SWIFTNet PKI
    SFTP_KEY         // SFTP with SSH key
}

public enum BackoffStrategy {
    NONE,            // No backoff (immediate retry)
    LINEAR,          // Linear backoff (1s, 2s, 3s)
    EXPONENTIAL      // Exponential backoff (1s, 2s, 4s, 8s)
}

public enum Environment {
    DEV,
    UAT,
    PRODUCTION
}

public enum ClearingSystemStatus {
    ACTIVE,
    INACTIVE,
    TESTING
}
```

---

### 8.2 REST API

```java
@RestController
@RequestMapping("/api/v1/clearing-systems")
public class ClearingSystemConfigurationController {
    
    @Autowired
    private ClearingSystemConfigurationService clearingSystemService;
    
    /**
     * Create clearing system configuration
     * POST /api/v1/clearing-systems
     */
    @PostMapping
    public ResponseEntity<ClearingSystemConfiguration> createClearingSystem(
        @RequestBody CreateClearingSystemRequest request,
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestHeader("X-User-ID") String userId
    ) {
        ClearingSystemConfiguration config = clearingSystemService.create(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(config);
    }
    
    /**
     * List clearing system configurations for tenant
     * GET /api/v1/clearing-systems
     */
    @GetMapping
    public ResponseEntity<List<ClearingSystemConfiguration>> listClearingSystems(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestParam(required = false) Environment environment
    ) {
        List<ClearingSystemConfiguration> configs = clearingSystemService.list(tenantId, environment);
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Get clearing system configuration
     * GET /api/v1/clearing-systems/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClearingSystemConfiguration> getClearingSystem(
        @PathVariable UUID id,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        ClearingSystemConfiguration config = clearingSystemService.get(id, tenantId);
        return ResponseEntity.ok(config);
    }
    
    /**
     * Update clearing system configuration
     * PUT /api/v1/clearing-systems/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClearingSystemConfiguration> updateClearingSystem(
        @PathVariable UUID id,
        @RequestBody UpdateClearingSystemRequest request,
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestHeader("X-User-ID") String userId
    ) {
        ClearingSystemConfiguration config = clearingSystemService.update(id, request, tenantId, userId);
        return ResponseEntity.ok(config);
    }
    
    /**
     * Test clearing system connection
     * POST /api/v1/clearing-systems/{id}/test
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<TestConnectionResult> testConnection(
        @PathVariable UUID id,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        TestConnectionResult result = clearingSystemService.testConnection(id, tenantId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Activate clearing system
     * POST /api/v1/clearing-systems/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateClearingSystem(
        @PathVariable UUID id,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        clearingSystemService.activate(id, tenantId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Deactivate clearing system
     * POST /api/v1/clearing-systems/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateClearingSystem(
        @PathVariable UUID id,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        clearingSystemService.deactivate(id, tenantId);
        return ResponseEntity.ok().build();
    }
}

// Request/Response DTOs
@Data
public class CreateClearingSystemRequest {
    private ClearingSystem clearingSystem;
    private String clearingSystemName;
    private PaymentRail paymentRail;
    private CommunicationPattern communicationPattern;
    private MessageFormat messageFormat;
    private TransportProtocol transportProtocol;
    private String endpointUrl;
    private Integer endpointTimeoutMs;
    private SecurityMechanism securityMechanism;
    private SecurityConfig securityConfig;
    private RetryConfig retryConfig;
    private IdempotencyConfig idempotencyConfig;
    private SanctionsScreeningConfig sanctionsScreeningConfig;
    private BatchConfig batchConfig;
    private Environment environment;
}

@Data
public class TestConnectionResult {
    private Boolean success;
    private String message;
    private Instant testedAt;
    private Long responseTimeMs;
    private String errorDetails;
}
```

---

## 9. Frontend Implementation

### 9.1 Clearing System Onboarding Wizard

```tsx
// src/pages/ClearingSystemOnboarding/ClearingSystemOnboardingPage.tsx

import React, { useState } from 'react';
import {
  Stepper,
  Step,
  StepLabel,
  Box,
  Button,
  Paper,
  Typography,
} from '@mui/material';
import { useForm, FormProvider } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import ClearingSystemSelector from './ClearingSystemSelector';
import CommunicationPatternConfig from './CommunicationPatternConfig';
import SecurityConfig from './SecurityConfig';
import RetryPolicyConfig from './RetryPolicyConfig';
import ReviewAndTest from './ReviewAndTest';
import { clearingSystemApi } from '../../api/clearingSystemApi';

const steps = [
  'Select Clearing System',
  'Communication Pattern',
  'Security Configuration',
  'Retry Policy',
  'Review & Test',
];

export default function ClearingSystemOnboardingPage() {
  const [activeStep, setActiveStep] = useState(0);
  const methods = useForm();
  
  const createClearingSystemMutation = useMutation({
    mutationFn: (data: any) => clearingSystemApi.createClearingSystem(data),
  });
  
  const renderStepContent = (step: number) => {
    switch (step) {
      case 0: return <ClearingSystemSelector />;
      case 1: return <CommunicationPatternConfig />;
      case 2: return <SecurityConfig />;
      case 3: return <RetryPolicyConfig />;
      case 4: return <ReviewAndTest />;
    }
  };
  
  const handleNext = () => {
    setActiveStep((prev) => prev + 1);
  };
  
  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };
  
  const handleSubmit = methods.handleSubmit((data) => {
    createClearingSystemMutation.mutate(data);
  });
  
  return (
    <FormProvider {...methods}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          Clearing System Onboarding
        </Typography>
        
        <Stepper activeStep={activeStep} sx={{ mt: 3, mb: 3 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        <Box sx={{ mt: 3 }}>
          {renderStepContent(activeStep)}
        </Box>
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
          <Button
            disabled={activeStep === 0}
            onClick={handleBack}
          >
            Back
          </Button>
          <Button
            variant="contained"
            onClick={activeStep === steps.length - 1 ? handleSubmit : handleNext}
          >
            {activeStep === steps.length - 1 ? 'Create Configuration' : 'Next'}
          </Button>
        </Box>
      </Paper>
    </FormProvider>
  );
}
```

---

### 9.2 Step 1: Clearing System Selector

```tsx
// src/pages/ClearingSystemOnboarding/ClearingSystemSelector.tsx

import React from 'react';
import {
  Grid,
  Card,
  CardActionArea,
  CardContent,
  Typography,
  Chip,
  Box,
  Stack,
} from '@mui/material';
import { useFormContext } from 'react-hook-form';
import {
  AccountBalance as BankIcon,
  Speed as SpeedIcon,
  Public as GlobalIcon,
} from '@mui/icons-material';

export default function ClearingSystemSelector() {
  const { watch, setValue } = useFormContext();
  const selectedSystem = watch('clearingSystem');
  
  const clearingSystems = [
    {
      value: 'SAMOS',
      label: 'SAMOS (RTGS)',
      description: 'Real-Time Gross Settlement by SARB',
      icon: <BankIcon fontSize="large" color="primary" />,
      paymentRail: 'RTGS',
      pattern: 'SYNCHRONOUS',
      format: 'ISO_20022_XML',
      security: 'MTLS',
      valueThreshold: '>R5 million',
      recommended: true,
    },
    {
      value: 'BANKSERV_EFT',
      label: 'BankservAfrica (EFT/ACH)',
      description: 'Automated Clearing House for bulk payments',
      icon: <BankIcon fontSize="large" color="secondary" />,
      paymentRail: 'ACH',
      pattern: 'BATCH',
      format: 'ACH_FIXED',
      security: 'SFTP_KEY',
      valueThreshold: '<R5 million',
    },
    {
      value: 'BANKSERV_RTC',
      label: 'RTC (Real-Time Clearing)',
      description: 'Real-time low-value payments',
      icon: <SpeedIcon fontSize="large" color="success" />,
      paymentRail: 'RTC',
      pattern: 'SYNCHRONOUS',
      format: 'ISO_8583_BINARY',
      security: 'MTLS',
      valueThreshold: '<R5 million',
      recommended: true,
    },
    {
      value: 'PAYSHAP',
      label: 'PayShap',
      description: 'Instant P2P payments (24/7/365)',
      icon: <SpeedIcon fontSize="large" color="warning" />,
      paymentRail: 'INSTANT_P2P',
      pattern: 'SYNCHRONOUS',
      format: 'JSON_REST',
      security: 'OAUTH_2_0',
      valueThreshold: '<R5,000 (instant)',
      recommended: true,
    },
    {
      value: 'SWIFT',
      label: 'SWIFT',
      description: 'International cross-border payments',
      icon: <GlobalIcon fontSize="large" color="info" />,
      paymentRail: 'INTERNATIONAL',
      pattern: 'ASYNCHRONOUS',
      format: 'ISO_20022_XML',
      security: 'SWIFTNET_PKI',
      valueThreshold: 'Any (international)',
    },
  ];
  
  const handleSelect = (system: any) => {
    setValue('clearingSystem', system.value);
    setValue('paymentRail', system.paymentRail);
    setValue('communicationPattern', system.pattern);
    setValue('messageFormat', system.format);
    setValue('securityMechanism', system.security);
  };
  
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Select Clearing System
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Choose the clearing system you want to integrate with
      </Typography>
      
      <Grid container spacing={2}>
        {clearingSystems.map((system) => (
          <Grid item xs={12} sm={6} md={4} key={system.value}>
            <Card
              sx={{
                height: '100%',
                border: selectedSystem === system.value ? 2 : 1,
                borderColor: selectedSystem === system.value ? 'primary.main' : 'divider',
              }}
            >
              <CardActionArea
                onClick={() => handleSelect(system)}
                sx={{ height: '100%', p: 2 }}
              >
                <CardContent>
                  <Stack spacing={2}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {system.icon}
                      <Typography variant="h6">{system.label}</Typography>
                      {system.recommended && (
                        <Chip label="Recommended" color="success" size="small" />
                      )}
                    </Box>
                    
                    <Typography variant="body2" color="text.secondary">
                      {system.description}
                    </Typography>
                    
                    <Box>
                      <Chip label={system.pattern} size="small" variant="outlined" sx={{ mr: 0.5 }} />
                      <Chip label={system.format} size="small" variant="outlined" sx={{ mr: 0.5 }} />
                      <Chip label={system.security} size="small" variant="outlined" />
                    </Box>
                    
                    <Typography variant="caption" color="text.secondary">
                      <strong>Value Threshold:</strong> {system.valueThreshold}
                    </Typography>
                  </Stack>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
```

---

## 10. Security Patterns

### 10.1 Security Pattern Matrix

| Clearing System | Security Mechanism | Certificate Required | Token Required | Encryption | Signature |
|----------------|-------------------|---------------------|----------------|------------|-----------|
| **SAMOS** | mTLS + Digital Signature | ✅ Client cert (SARB-issued) | ❌ | TLS 1.2+ | XMLDSig (RSA-SHA256) |
| **BankservAfrica (EFT)** | SFTP + PGP | ❌ | ❌ | PGP (RSA 2048) | PGP detached sig |
| **RTC** | mTLS + MAC | ✅ Client cert | ❌ | TLS 1.2+ | HMAC-SHA256 |
| **PayShap** | OAuth 2.0 + mTLS | ✅ Client cert | ✅ JWT (5 min) | TLS 1.2+ | JWS (optional) |
| **SWIFT** | SWIFTNet PKI + RMA | ✅ SWIFT-issued cert | ❌ | 3DES/AES | LAU (Login Auth) |

---

### 10.2 Certificate Management

**Certificate Upload UI**:
```tsx
<Controller
  name="securityConfig.certificatePath"
  control={control}
  render={({ field }) => (
    <Box>
      <Typography variant="body2" gutterBottom>
        Upload Client Certificate (.p12 or .pfx)
      </Typography>
      <input
        type="file"
        accept=".p12,.pfx"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) {
            // Upload certificate to backend
            uploadCertificate(file).then((path) => {
              field.onChange(path);
            });
          }
        }}
      />
      <Typography variant="caption" color="text.secondary">
        Certificate will be securely stored in Azure Key Vault
      </Typography>
    </Box>
  )}
/>
```

**Certificate Storage** (Backend):
```java
@Service
public class CertificateStorageService {
    
    @Autowired
    private KeyVaultClient keyVaultClient;
    
    public String storeCertificate(MultipartFile file, String tenantId, ClearingSystem system) {
        // 1. Validate certificate
        validateCertificate(file);
        
        // 2. Generate Key Vault secret name
        String secretName = String.format("cert-%s-%s", tenantId, system.name().toLowerCase());
        
        // 3. Store in Azure Key Vault
        keyVaultClient.setSecret(secretName, Base64.getEncoder().encodeToString(file.getBytes()));
        
        // 4. Return reference path
        return String.format("keyvault://%s", secretName);
    }
}
```

---

## 11. Testing Strategy

### 11.1 Connection Test

**Test Message per Clearing System**:

**SAMOS** (ISO 20022 Test):
```java
public TestConnectionResult testSAMOSConnection(ClearingSystemConfiguration config) {
    // Build test message (pacs.008)
    String testMessage = """
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
          <FIToFICstmrCdtTrf>
            <GrpHdr>
              <MsgId>TEST-SAMOS-%s</MsgId>
              <CreDtTm>%s</CreDtTm>
            </GrpHdr>
          </FIToFICstmrCdtTrf>
        </Document>
        """.formatted(UUID.randomUUID().toString(), Instant.now().toString());
    
    // Send test message
    HttpResponse<String> response = httpClient.send(
        HttpRequest.newBuilder()
            .uri(URI.create(config.getEndpointUrl() + "/test"))
            .header("Content-Type", "application/xml")
            .POST(HttpRequest.BodyPublishers.ofString(testMessage))
            .build(),
        HttpResponse.BodyHandlers.ofString()
    );
    
    // Parse response
    return TestConnectionResult.builder()
        .success(response.statusCode() == 200)
        .message(response.statusCode() == 200 ? "Connection successful" : "Connection failed")
        .responseTimeMs(/* measure */)
        .build();
}
```

---

## Conclusion

**Clearing System Onboarding** provides:
- ✅ Self-service configuration via React UI
- ✅ Support for 5 South African clearing systems (SAMOS, BankservAfrica, RTC, PayShap, SWIFT)
- ✅ Configurable communication patterns (Sync/Async/Batch)
- ✅ Multiple message formats (ISO 20022, ISO 8583, SWIFT MT, JSON, Fixed-length)
- ✅ Flexible security mechanisms (mTLS, OAuth 2.0, SFTP, SWIFTNet PKI)
- ✅ Configurable retry policies (count, backoff, timeout)
- ✅ Test connection before activation
- ✅ Multi-environment support (Dev, UAT, Prod)

**Status**: ✅ **DESIGN COMPLETE** - Ready for Implementation

---

**Document Version**: 1.0  
**Created**: 2025-10-12  
**Total Lines**: 2,000+  
**Related Documents**:
- `docs/06-SOUTH-AFRICA-CLEARING.md` (Clearing Systems Overview)
- `docs/02-MICROSERVICES-BREAKDOWN.md` (Clearing Adapters)
- `docs/40-PHASE-7-DETAILED-DESIGN.md` (Phase 7 Design)
