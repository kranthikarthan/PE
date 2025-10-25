# Feature 1: ISO 20022 Module Extension for pain.002

## Overview

This is the first small feature in the ISO 20022 pain.002 implementation plan. We'll extend the existing ISO 20022 module to support pain.002 (Payment Status Report) messages, starting with the foundation components.

**Feature Scope**: Add pain.002 support to the ISO 20022 domain module  
**Estimated Effort**: 3-4 days  
**Dependencies**: None (foundation feature)  
**Risk Level**: Low (isolated changes)

---

## Current State Analysis

### Existing ISO 20022 Module
```java
// Current: domain-models/iso20022/src/main/java/com/payments/iso20022/
├── config/
│   ├── Iso20022JaxbConfig.java
│   ├── Iso20022MessageType.java          // ❌ Missing pain.002
│   └── Iso20022NamespacePrefixMapper.java
├── service/
│   └── Iso20022MarshallerService.java
├── util/
│   └── UetrGenerator.java
└── validation/
    ├── Iso20022Validator.java
    └── ValidationResult.java
```

### Current Message Types Supported
```java
public enum Iso20022MessageType {
    PACS_008("pacs.008.001.08", ...),  // ✅ FI to FI Credit Transfer
    PACS_002("pacs.002.001.10", ...),  // ✅ Payment Status Report
    PACS_004("pacs.004.001.09", ...),  // ✅ Payment Return
    CAMT_054("camt.054.001.08", ...);  // ✅ Settlement Notification
    // ❌ MISSING: pain.002.001.10 - Payment Status Report
}
```

---

## Feature Requirements

### 1. Add pain.002 to Message Types
- **Add pain.002.001.10 to Iso20022MessageType enum**
- **Add pain.002 namespace and package configuration**
- **Add pain.002 XSD schema file**

### 2. Create pain.002 Message Builder
- **Build pain.002 XML messages from internal status reports**
- **Support all required pain.002 fields**
- **Handle status code mapping**

### 3. Create pain.002 Message Parser
- **Parse pain.002 XML messages to internal status reports**
- **Validate pain.002 message structure**
- **Extract status information**

### 4. Add pain.002 Validation
- **XSD schema validation for XML messages**
- **Message structure validation**
- **Status code validation**

---

## Technical Implementation

### 1. Update Iso20022MessageType.java

#### Current Implementation
```java
public enum Iso20022MessageType {
    PACS_008("pacs.008.001.08", 
             "com.payments.iso20022.pacs008",
             "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08",
             "pacs.008.001.08.xsd",
             "FI to FI Customer Credit Transfer"),
    // ... other types
}
```

#### Updated Implementation
```java
public enum Iso20022MessageType {
    /** pain.002.001.10 - Payment Status Report */
    PAIN_002("pain.002.001.10",
             "com.payments.iso20022.pain002",
             "urn:iso:std:iso:20022:tech:xsd:pain.002.001.10",
             "pain.002.001.10.xsd",
             "Payment Status Report"),
    
    PACS_008("pacs.008.001.08", 
             "com.payments.iso20022.pacs008",
             "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08",
             "pacs.008.001.08.xsd",
             "FI to FI Customer Credit Transfer"),
    // ... other types
}
```

### 2. Add pain.002 XSD Schema

#### File: `domain-models/iso20022/src/main/resources/xsd/pain.002.001.10.xsd`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns="urn:iso:std:iso:20022:tech:xsd:pain.002.001.10"
           targetNamespace="urn:iso:std:iso:20022:tech:xsd:pain.002.001.10"
           elementFormDefault="qualified">
    
    <!-- pain.002.001.10 schema definition -->
    <!-- This is a simplified version for development -->
    <!-- Production should use official ISO 20022 schema -->
    
    <xs:element name="Document" type="Document"/>
    
    <xs:complexType name="Document">
        <xs:sequence>
            <xs:element name="PmtStsRpt" type="PaymentStatusReportV10"/>
        </xs:sequence>
    </xs:complexType>
    
    <xs:complexType name="PaymentStatusReportV10">
        <xs:sequence>
            <xs:element name="GrpHdr" type="GroupHeader53"/>
            <xs:element name="TxInfAndSts" type="PaymentTransactionInformation25" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Group Header -->
    <xs:complexType name="GroupHeader53">
        <xs:sequence>
            <xs:element name="MsgId" type="Max35Text"/>
            <xs:element name="CreDtTm" type="ISODateTime"/>
            <xs:element name="OrgnlGrpInfAndSts" type="OriginalGroupInformation3"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Original Group Information -->
    <xs:complexType name="OriginalGroupInformation3">
        <xs:sequence>
            <xs:element name="OrgnlMsgId" type="Max35Text"/>
            <xs:element name="OrgnlMsgNmId" type="Max35Text"/>
            <xs:element name="OrgnlCreDtTm" type="ISODateTime" minOccurs="0"/>
            <xs:element name="GrpSts" type="TransactionGroupStatus3Code"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Payment Transaction Information -->
    <xs:complexType name="PaymentTransactionInformation25">
        <xs:sequence>
            <xs:element name="StsId" type="Max35Text"/>
            <xs:element name="OrgnlInstrId" type="Max35Text" minOccurs="0"/>
            <xs:element name="OrgnlEndToEndId" type="Max35Text" minOccurs="0"/>
            <xs:element name="TxSts" type="TransactionIndividualStatus3Code"/>
            <xs:element name="StsRsnInf" type="StatusReasonInformation9" minOccurs="0" maxOccurs="unbounded"/>
            <xs:element name="ChrgsInf" type="ChargesInformation5" minOccurs="0" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Status Reason Information -->
    <xs:complexType name="StatusReasonInformation9">
        <xs:sequence>
            <xs:element name="Rsn" type="StatusReason6Choice"/>
            <xs:element name="AddtlInf" type="Max105Text" minOccurs="0"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Status Reason Choice -->
    <xs:complexType name="StatusReason6Choice">
        <xs:choice>
            <xs:element name="Cd" type="ExternalStatusReason1Code"/>
            <xs:element name="Prtry" type="Max35Text"/>
        </xs:choice>
    </xs:complexType>
    
    <!-- Charges Information -->
    <xs:complexType name="ChargesInformation5">
        <xs:sequence>
            <xs:element name="Amt" type="ActiveOrHistoricCurrencyAndAmount"/>
            <xs:element name="Agt" type="BranchAndFinancialInstitutionIdentification4"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Basic Types -->
    <xs:simpleType name="Max35Text">
        <xs:restriction base="xs:string">
            <xs:maxLength value="35"/>
        </xs:restriction>
    </xs:simpleType>
    
    <xs:simpleType name="ISODateTime">
        <xs:restriction base="xs:dateTime"/>
    </xs:simpleType>
    
    <xs:simpleType name="Max105Text">
        <xs:restriction base="xs:string">
            <xs:maxLength value="105"/>
        </xs:restriction>
    </xs:simpleType>
    
    <!-- Status Code Enumerations -->
    <xs:simpleType name="TransactionGroupStatus3Code">
        <xs:restriction base="xs:string">
            <xs:enumeration value="ACCP"/>
            <xs:enumeration value="RJCT"/>
            <xs:enumeration value="PDNG"/>
        </xs:restriction>
    </xs:simpleType>
    
    <xs:simpleType name="TransactionIndividualStatus3Code">
        <xs:restriction base="xs:string">
            <xs:enumeration value="ACSC"/>
            <xs:enumeration value="RJCT"/>
            <xs:enumeration value="PDNG"/>
            <xs:enumeration value="CANC"/>
            <xs:enumeration value="PART"/>
        </xs:restriction>
    </xs:simpleType>
    
    <!-- Additional types would be defined here -->
    <!-- This is a simplified schema for development -->
    
</xs:schema>
```

### 3. Create pain.002 Message Builder

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain002/Pain002MessageBuilder.java`
```java
package com.payments.iso20022.pain002;

import com.payments.contracts.payment.PaymentStatusReportRequest;
import com.payments.contracts.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Builder for ISO 20022 pain.002 messages
 * 
 * <p>Builds pain.002 Payment Status Report messages
 * from internal payment status reports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain002MessageBuilder {
    
    private final Iso20022MarshallerService marshallerService;
    
    /**
     * Build pain.002 XML message from payment status report request
     * 
     * @param request Payment status report request
     * @param tenantContext Tenant context
     * @return pain.002 XML message
     */
    public String buildPain002Xml(PaymentStatusReportRequest request, TenantContext tenantContext) {
        try {
            log.info("Building pain.002 XML message for payment: {}", request.getPaymentId());
            
            // Create pain.002 document structure
            Document document = createPain002Document(request, tenantContext);
            
            // Marshal to XML
            String xml = marshallerService.marshalToXml(document, Iso20022MessageType.PAIN_002);
            
            log.info("Successfully built pain.002 XML message");
            return xml;
            
        } catch (Exception e) {
            log.error("Failed to build pain.002 XML message", e);
            throw new Pain002MessageBuilderException("Failed to build pain.002 XML message", e);
        }
    }
    
    /**
     * Create pain.002 document from status report request
     */
    private Document createPain002Document(PaymentStatusReportRequest request, TenantContext tenantContext) {
        Document document = new Document();
        
        // Create Payment Status Report
        PaymentStatusReportV10 pmtStsRpt = new PaymentStatusReportV10();
        
        // Set Group Header
        pmtStsRpt.setGrpHdr(createGroupHeader(request, tenantContext));
        
        // Set Transaction Information and Status
        pmtStsRpt.getTxInfAndSts().add(createTransactionStatus(request));
        
        document.setPmtStsRpt(pmtStsRpt);
        
        return document;
    }
    
    /**
     * Create Group Header
     */
    private GroupHeader53 createGroupHeader(PaymentStatusReportRequest request, TenantContext tenantContext) {
        GroupHeader53 grpHdr = new GroupHeader53();
        
        // Message ID
        grpHdr.setMsgId("STATUS-REPORT-" + request.getPaymentId());
        
        // Creation Date Time
        grpHdr.setCreDtTm(Instant.now());
        
        // Original Group Information and Status
        grpHdr.setOrgnlGrpInfAndSts(createOriginalGroupInformation(request));
        
        return grpHdr;
    }
    
    /**
     * Create Original Group Information
     */
    private OriginalGroupInformation3 createOriginalGroupInformation(PaymentStatusReportRequest request) {
        OriginalGroupInformation3 orgnlGrpInfAndSts = new OriginalGroupInformation3();
        
        // Original Message ID
        orgnlGrpInfAndSts.setOrgnlMsgId(request.getOriginalMessageId());
        
        // Original Message Name ID
        orgnlGrpInfAndSts.setOrgnlMsgNmId("pain.001.001.09");
        
        // Original Creation Date Time
        orgnlGrpInfAndSts.setOrgnlCreDtTm(request.getOriginalCreationDateTime());
        
        // Group Status
        orgnlGrpInfAndSts.setGrpSts(mapToGroupStatus(request.getPaymentStatus()));
        
        return orgnlGrpInfAndSts;
    }
    
    /**
     * Create Transaction Status
     */
    private PaymentTransactionInformation25 createTransactionStatus(PaymentStatusReportRequest request) {
        PaymentTransactionInformation25 txInfAndSts = new PaymentTransactionInformation25();
        
        // Status ID
        txInfAndSts.setStsId("STATUS-" + UUID.randomUUID().toString().substring(0, 8));
        
        // Original Instruction ID
        txInfAndSts.setOrgnlInstrId(request.getOriginalInstructionId());
        
        // Original End-to-End ID
        txInfAndSts.setOrgnlEndToEndId(request.getOriginalEndToEndId());
        
        // Transaction Status
        txInfAndSts.setTxSts(mapToTransactionStatus(request.getPaymentStatus()));
        
        // Status Reason Information
        if (request.getStatusReason() != null) {
            txInfAndSts.getStsRsnInf().add(createStatusReasonInformation(request));
        }
        
        // Charges Information
        if (request.getChargesAmount() != null && request.getChargesAmount().compareTo(BigDecimal.ZERO) > 0) {
            txInfAndSts.getChrgsInf().add(createChargesInformation(request));
        }
        
        return txInfAndSts;
    }
    
    /**
     * Create Status Reason Information
     */
    private StatusReasonInformation9 createStatusReasonInformation(PaymentStatusReportRequest request) {
        StatusReasonInformation9 stsRsnInf = new StatusReasonInformation9();
        
        // Status Reason
        StatusReason6Choice rsn = new StatusReason6Choice();
        rsn.setCd(request.getStatusReasonCode());
        stsRsnInf.setRsn(rsn);
        
        // Additional Information
        if (request.getStatusAdditionalInformation() != null) {
            stsRsnInf.setAddtlInf(request.getStatusAdditionalInformation());
        }
        
        return stsRsnInf;
    }
    
    /**
     * Create Charges Information
     */
    private ChargesInformation5 createChargesInformation(PaymentStatusReportRequest request) {
        ChargesInformation5 chrgsInf = new ChargesInformation5();
        
        // Amount
        ActiveOrHistoricCurrencyAndAmount amt = new ActiveOrHistoricCurrencyAndAmount();
        amt.setCcy(request.getChargesCurrency());
        amt.setValue(request.getChargesAmount());
        chrgsInf.setAmt(amt);
        
        // Agent
        BranchAndFinancialInstitutionIdentification4 agt = new BranchAndFinancialInstitutionIdentification4();
        FinancialInstitutionIdentification7 finInstnId = new FinancialInstitutionIdentification7();
        finInstnId.setBICFI(request.getChargesAgentBic());
        agt.setFinInstnId(finInstnId);
        chrgsInf.setAgt(agt);
        
        return chrgsInf;
    }
    
    /**
     * Map internal status to ISO 20022 group status
     */
    private TransactionGroupStatus3Code mapToGroupStatus(PaymentStatus internalStatus) {
        return switch (internalStatus) {
            case COMPLETED -> TransactionGroupStatus3Code.ACCP;
            case FAILED -> TransactionGroupStatus3Code.RJCT;
            case PROCESSING -> TransactionGroupStatus3Code.PDNG;
            default -> TransactionGroupStatus3Code.PDNG;
        };
    }
    
    /**
     * Map internal status to ISO 20022 transaction status
     */
    private TransactionIndividualStatus3Code mapToTransactionStatus(PaymentStatus internalStatus) {
        return switch (internalStatus) {
            case COMPLETED -> TransactionIndividualStatus3Code.ACSC;
            case FAILED -> TransactionIndividualStatus3Code.RJCT;
            case PROCESSING -> TransactionIndividualStatus3Code.PDNG;
            case CANCELLED -> TransactionIndividualStatus3Code.CANC;
            case PARTIALLY_COMPLETED -> TransactionIndividualStatus3Code.PART;
            default -> TransactionIndividualStatus3Code.PDNG;
        };
    }
}
```

### 4. Create pain.002 Message Parser

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain002/Pain002MessageParser.java`
```java
package com.payments.iso20022.pain002;

import com.payments.contracts.payment.PaymentStatusReportRequest;
import com.payments.contracts.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Parser for ISO 20022 pain.002 messages
 * 
 * <p>Parses pain.002 Payment Status Report messages
 * to internal payment status report requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain002MessageParser {
    
    /**
     * Parse pain.002 XML message to payment status report request
     * 
     * @param pain002Xml pain.002 XML message
     * @param tenantContext Tenant context
     * @return Payment status report request
     */
    public PaymentStatusReportRequest parsePain002Xml(String pain002Xml, TenantContext tenantContext) {
        try {
            log.info("Parsing pain.002 XML message");
            
            // Parse XML to Document object
            Document document = parseXmlToDocument(pain002Xml);
            
            // Extract status report information
            PaymentStatusReportRequest request = extractStatusReportRequest(document, tenantContext);
            
            log.info("Successfully parsed pain.002 XML message");
            return request;
            
        } catch (Exception e) {
            log.error("Failed to parse pain.002 XML message", e);
            throw new Pain002MessageParserException("Failed to parse pain.002 XML message", e);
        }
    }
    
    /**
     * Parse XML to Document object
     */
    private Document parseXmlToDocument(String pain002Xml) {
        // Implementation would use JAXB to parse XML
        // This is a simplified version
        throw new UnsupportedOperationException("XML parsing not yet implemented");
    }
    
    /**
     * Extract status report request from pain.002 document
     */
    private PaymentStatusReportRequest extractStatusReportRequest(Document document, TenantContext tenantContext) {
        // Extract status report information from pain.002 document
        // This is a simplified version
        throw new UnsupportedOperationException("Status report extraction not yet implemented");
    }
}
```

### 5. Create pain.002 Validator

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain002/Pain002Validator.java`
```java
package com.payments.iso20022.pain002;

import com.payments.iso20022.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for ISO 20022 pain.002 messages
 * 
 * <p>Validates pain.002 messages against XSD schema and business rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain002Validator {
    
    private final Iso20022Validator iso20022Validator;
    
    /**
     * Validate pain.002 XML message against XSD schema
     * 
     * @param pain002Xml pain.002 XML message
     * @return Validation result
     */
    public ValidationResult validateXml(String pain002Xml) {
        try {
            log.info("Validating pain.002 XML message against XSD schema");
            
            // Validate against XSD schema
            ValidationResult xsdResult = validateAgainstXsd(pain002Xml);
            if (!xsdResult.isValid()) {
                return xsdResult;
            }
            
            // Validate business rules
            ValidationResult businessResult = validateBusinessRules(pain002Xml);
            if (!businessResult.isValid()) {
                return businessResult;
            }
            
            log.info("pain.002 XML message validation successful");
            return ValidationResult.valid();
            
        } catch (Exception e) {
            log.error("Failed to validate pain.002 XML message", e);
            return ValidationResult.invalid("Validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate against XSD schema
     */
    private ValidationResult validateAgainstXsd(String pain002Xml) {
        try {
            // Load XSD schema
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(getClass().getResource("/xsd/pain.002.001.10.xsd"));
            
            // Create validator
            Validator validator = schema.newValidator();
            
            // Validate XML
            validator.validate(new StreamSource(new ByteArrayInputStream(pain002Xml.getBytes())));
            
            return ValidationResult.valid();
            
        } catch (Exception e) {
            log.error("XSD validation failed", e);
            return ValidationResult.invalid("XSD validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate business rules
     */
    private ValidationResult validateBusinessRules(String pain002Xml) {
        List<String> errors = new ArrayList<>();
        
        // Add business rule validations here
        // For example:
        // - Check required fields
        // - Validate status codes
        // - Check date formats
        // - Validate amounts
        
        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            return ValidationResult.invalid("Business rule validation failed: " + String.join(", ", errors));
        }
    }
}
```

### 6. Create Exception Classes

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain002/Pain002MessageBuilderException.java`
```java
package com.payments.iso20022.pain002;

/**
 * Exception thrown when pain.002 message building fails
 */
public class Pain002MessageBuilderException extends RuntimeException {
    
    public Pain002MessageBuilderException(String message) {
        super(message);
    }
    
    public Pain002MessageBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain002/Pain002MessageParserException.java`
```java
package com.payments.iso20022.pain002;

/**
 * Exception thrown when pain.002 message parsing fails
 */
public class Pain002MessageParserException extends RuntimeException {
    
    public Pain002MessageParserException(String message) {
        super(message);
    }
    
    public Pain002MessageParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## Testing Implementation

### 1. Unit Tests

#### File: `domain-models/iso20022/src/test/java/com/payments/iso20022/pain002/Pain002MessageBuilderTest.java`
```java
package com.payments.iso20022.pain002;

import com.payments.contracts.payment.PaymentStatusReportRequest;
import com.payments.contracts.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Pain002MessageBuilderTest {
    
    @Mock
    private Iso20022MarshallerService marshallerService;
    
    private Pain002MessageBuilder messageBuilder;
    
    @BeforeEach
    void setUp() {
        messageBuilder = new Pain002MessageBuilder(marshallerService);
    }
    
    @Test
    void shouldBuildValidXmlMessage() {
        // Given
        PaymentStatusReportRequest request = createStatusReportRequest();
        TenantContext tenantContext = createTenantContext();
        
        // When
        String xml = messageBuilder.buildPain002Xml(request, tenantContext);
        
        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("Document"));
        assertTrue(xml.contains("PmtStsRpt"));
        assertTrue(xml.contains("GrpHdr"));
        assertTrue(xml.contains("TxInfAndSts"));
    }
    
    @Test
    void shouldThrowExceptionWhenBuildingFails() {
        // Given
        PaymentStatusReportRequest request = createStatusReportRequest();
        TenantContext tenantContext = createTenantContext();
        
        // When/Then
        assertThrows(Pain002MessageBuilderException.class, () -> {
            messageBuilder.buildPain002Xml(request, tenantContext);
        });
    }
    
    private PaymentStatusReportRequest createStatusReportRequest() {
        return PaymentStatusReportRequest.builder()
                .paymentId("PAY-2025-001")
                .originalMessageId("PAYMENT-INIT-2025-001")
                .originalInstructionId("INSTR-001")
                .originalEndToEndId("E2E-001")
                .paymentStatus(PaymentStatus.COMPLETED)
                .statusReasonCode("NARR")
                .statusAdditionalInformation("Payment processed successfully")
                .chargesAmount(new BigDecimal("5.00"))
                .chargesCurrency("ZAR")
                .chargesAgentBic("ABSAZAJJXXX")
                .build();
    }
    
    private TenantContext createTenantContext() {
        return TenantContext.builder()
                .tenantId("TENANT-001")
                .businessUnitId("BU-001")
                .build();
    }
}
```

### 2. Integration Tests

#### File: `domain-models/iso20022/src/test/java/com/payments/iso20022/pain002/Pain002IntegrationTest.java`
```java
package com.payments.iso20022.pain002;

import com.payments.contracts.payment.PaymentStatusReportRequest;
import com.payments.contracts.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class Pain002IntegrationTest {
    
    @Test
    void shouldBuildAndValidatePain002Message() {
        // Given
        PaymentStatusReportRequest request = createStatusReportRequest();
        TenantContext tenantContext = createTenantContext();
        
        Pain002MessageBuilder builder = new Pain002MessageBuilder(marshallerService);
        Pain002Validator validator = new Pain002Validator(iso20022Validator);
        
        // When
        String xml = builder.buildPain002Xml(request, tenantContext);
        ValidationResult result = validator.validateXml(xml);
        
        // Then
        assertTrue(result.isValid());
        assertNotNull(xml);
    }
    
    private PaymentStatusReportRequest createStatusReportRequest() {
        // Implementation
    }
    
    private TenantContext createTenantContext() {
        // Implementation
    }
}
```

---

## Implementation Steps

### Step 1: Update Iso20022MessageType (Day 1)
1. **Add pain.002 to enum**
2. **Update namespace and package configuration**
3. **Add XSD schema file**
4. **Test enum changes**

### Step 2: Create Message Builder (Day 2)
1. **Create Pain002MessageBuilder class**
2. **Implement XML building logic**
3. **Add error handling**
4. **Create unit tests**

### Step 3: Create Message Parser (Day 3)
1. **Create Pain002MessageParser class**
2. **Implement XML parsing logic**
3. **Add error handling**
4. **Create unit tests**

### Step 4: Create Validator (Day 4)
1. **Create Pain002Validator class**
2. **Implement XSD validation**
3. **Add business rule validation**
4. **Create unit tests**

### Step 5: Integration Testing (Day 5)
1. **Create integration tests**
2. **Test end-to-end flow**
3. **Validate against real pain.002 messages**
4. **Performance testing**

---

## Success Criteria

### Functional Requirements
- ✅ pain.002 message building from internal status reports
- ✅ pain.002 message parsing to internal status reports
- ✅ XSD schema validation
- ✅ Business rule validation
- ✅ Error handling and logging

### Non-Functional Requirements
- ✅ Performance: < 50ms per message building/parsing
- ✅ Reliability: 99.9% success rate
- ✅ Maintainability: Clear code structure and documentation
- ✅ Testability: Comprehensive unit and integration tests

### Quality Requirements
- ✅ Code coverage: > 80%
- ✅ Documentation: Complete JavaDoc and README
- ✅ Error handling: Proper exception handling
- ✅ Logging: Comprehensive logging for debugging

---

## Next Steps

After completing this feature:

1. **Feature 2**: Add JSON support for pain.002 messages
2. **Feature 3**: Create status report generation service
3. **Feature 4**: Update payment initiation service
5. **Feature 5**: Add end-to-end testing

---

**Feature Version**: 1.0  
**Created**: 2025-01-27  
**Estimated Effort**: 3-4 days  
**Dependencies**: None  
**Risk Level**: Low
