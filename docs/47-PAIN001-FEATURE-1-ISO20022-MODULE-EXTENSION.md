# Feature 1: ISO 20022 Module Extension for pain.001

## Overview

This is the first small feature in the ISO 20022 pain.001 implementation plan. We'll extend the existing ISO 20022 module to support pain.001 messages, starting with the foundation components.

**Feature Scope**: Add pain.001 support to the ISO 20022 domain module  
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
│   ├── Iso20022MessageType.java          // ❌ Missing pain.001
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
    // ❌ MISSING: pain.001.001.09 - Customer Credit Transfer Initiation
}
```

---

## Feature Requirements

### 1. Add pain.001 to Message Types
- **Add pain.001.001.09 to Iso20022MessageType enum**
- **Add pain.001 namespace and package configuration**
- **Add pain.001 XSD schema file**

### 2. Create pain.001 Message Builder
- **Build pain.001 XML messages from internal DTOs**
- **Support all required pain.001 fields**
- **Handle South African banking requirements**

### 3. Create pain.001 Message Parser
- **Parse pain.001 XML messages to internal DTOs**
- **Validate pain.001 message structure**
- **Extract payment information**

### 4. Add pain.001 Validation
- **XSD schema validation for XML messages**
- **Message structure validation**
- **Business rule validation**

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
    /** pain.001.001.09 - Customer Credit Transfer Initiation */
    PAIN_001("pain.001.001.09",
             "com.payments.iso20022.pain001",
             "urn:iso:std:iso:20022:tech:xsd:pain.001.001.09",
             "pain.001.001.09.xsd",
             "Customer Credit Transfer Initiation"),
    
    PACS_008("pacs.008.001.08", 
             "com.payments.iso20022.pacs008",
             "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08",
             "pacs.008.001.08.xsd",
             "FI to FI Customer Credit Transfer"),
    // ... other types
}
```

### 2. Add pain.001 XSD Schema

#### File: `domain-models/iso20022/src/main/resources/xsd/pain.001.001.09.xsd`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"
           targetNamespace="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09"
           elementFormDefault="qualified">
    
    <!-- pain.001.001.09 schema definition -->
    <!-- This is a simplified version for development -->
    <!-- Production should use official ISO 20022 schema -->
    
    <xs:element name="Document" type="Document"/>
    
    <xs:complexType name="Document">
        <xs:sequence>
            <xs:element name="CstmrCdtTrfInitn" type="CustomerCreditTransferInitiationV09"/>
        </xs:sequence>
    </xs:complexType>
    
    <xs:complexType name="CustomerCreditTransferInitiationV09">
        <xs:sequence>
            <xs:element name="GrpHdr" type="GroupHeader48"/>
            <xs:element name="PmtInf" type="PaymentInstruction22" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Group Header -->
    <xs:complexType name="GroupHeader48">
        <xs:sequence>
            <xs:element name="MsgId" type="Max35Text"/>
            <xs:element name="CreDtTm" type="ISODateTime"/>
            <xs:element name="NbOfTxs" type="Max15NumericText"/>
            <xs:element name="CtrlSum" type="DecimalNumber"/>
            <xs:element name="InitgPty" type="PartyIdentification43"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Payment Instruction -->
    <xs:complexType name="PaymentInstruction22">
        <xs:sequence>
            <xs:element name="PmtInfId" type="Max35Text"/>
            <xs:element name="PmtMtd" type="PaymentMethod3Code"/>
            <xs:element name="BtchBookg" type="BatchBookingIndicator"/>
            <xs:element name="NbOfTxs" type="Max15NumericText"/>
            <xs:element name="CtrlSum" type="DecimalNumber"/>
            <xs:element name="PmtTpInf" type="PaymentTypeInformation26" minOccurs="0"/>
            <xs:element name="ReqdExctnDt" type="ISODate"/>
            <xs:element name="Dbtr" type="PartyIdentification43"/>
            <xs:element name="DbtrAcct" type="CashAccount24" minOccurs="0"/>
            <xs:element name="DbtrAgt" type="BranchAndFinancialInstitutionIdentification5"/>
            <xs:element name="CdtTrfTx" type="CreditTransferTransaction25" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>
    
    <!-- Credit Transfer Transaction -->
    <xs:complexType name="CreditTransferTransaction25">
        <xs:sequence>
            <xs:element name="PmtId" type="PaymentIdentification3"/>
            <xs:element name="Amt" type="AmountType3Choice"/>
            <xs:element name="CdtrAgt" type="BranchAndFinancialInstitutionIdentification5" minOccurs="0"/>
            <xs:element name="Cdtr" type="PartyIdentification43"/>
            <xs:element name="CdtrAcct" type="CashAccount24" minOccurs="0"/>
            <xs:element name="RmtInf" type="RemittanceInformation11" minOccurs="0"/>
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
    
    <xs:simpleType name="Max15NumericText">
        <xs:restriction base="xs:string">
            <xs:pattern value="[0-9]{1,15}"/>
        </xs:restriction>
    </xs:simpleType>
    
    <xs:simpleType name="DecimalNumber">
        <xs:restriction base="xs:decimal"/>
    </xs:simpleType>
    
    <!-- Additional types would be defined here -->
    <!-- This is a simplified schema for development -->
    
</xs:schema>
```

### 3. Create pain.001 Message Builder

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain001/Pain001MessageBuilder.java`
```java
package com.payments.iso20022.pain001;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.shared.Money;
import com.payments.contracts.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Builder for ISO 20022 pain.001 messages
 * 
 * <p>Builds pain.001 Customer Credit Transfer Initiation messages
 * from internal payment initiation requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain001MessageBuilder {
    
    private final Iso20022MarshallerService marshallerService;
    
    /**
     * Build pain.001 XML message from payment initiation request
     * 
     * @param request Payment initiation request
     * @param tenantContext Tenant context
     * @return pain.001 XML message
     */
    public String buildPain001Xml(PaymentInitiationRequest request, TenantContext tenantContext) {
        try {
            log.info("Building pain.001 XML message for payment: {}", request.getPaymentId());
            
            // Create pain.001 document structure
            Document document = createPain001Document(request, tenantContext);
            
            // Marshal to XML
            String xml = marshallerService.marshalToXml(document, Iso20022MessageType.PAIN_001);
            
            log.info("Successfully built pain.001 XML message");
            return xml;
            
        } catch (Exception e) {
            log.error("Failed to build pain.001 XML message", e);
            throw new Pain001MessageBuilderException("Failed to build pain.001 XML message", e);
        }
    }
    
    /**
     * Create pain.001 document from payment request
     */
    private Document createPain001Document(PaymentInitiationRequest request, TenantContext tenantContext) {
        Document document = new Document();
        
        // Create Customer Credit Transfer Initiation
        CustomerCreditTransferInitiationV09 cstmrCdtTrfInitn = new CustomerCreditTransferInitiationV09();
        
        // Set Group Header
        cstmrCdtTrfInitn.setGrpHdr(createGroupHeader(request, tenantContext));
        
        // Set Payment Information
        cstmrCdtTrfInitn.getPmtInf().add(createPaymentInformation(request, tenantContext));
        
        document.setCstmrCdtTrfInitn(cstmrCdtTrfInitn);
        
        return document;
    }
    
    /**
     * Create Group Header
     */
    private GroupHeader48 createGroupHeader(PaymentInitiationRequest request, TenantContext tenantContext) {
        GroupHeader48 grpHdr = new GroupHeader48();
        
        // Message ID
        grpHdr.setMsgId("PAYMENT-INIT-" + request.getPaymentId().getValue());
        
        // Creation Date Time
        grpHdr.setCreDtTm(Instant.now());
        
        // Number of Transactions
        grpHdr.setNbOfTxs("1");
        
        // Control Sum
        grpHdr.setCtrlSum(request.getAmount().getAmount());
        
        // Initiating Party
        grpHdr.setInitgPty(createInitiatingParty(tenantContext));
        
        return grpHdr;
    }
    
    /**
     * Create Initiating Party
     */
    private PartyIdentification43 createInitiatingParty(TenantContext tenantContext) {
        PartyIdentification43 initgPty = new PartyIdentification43();
        
        // Party Name
        initgPty.setNm(tenantContext.getTenantId() + " - " + tenantContext.getBusinessUnitId());
        
        // Party ID
        OrganisationIdentification8 orgId = new OrganisationIdentification8();
        OtherIdentification1 othr = new OtherIdentification1();
        othr.setId(tenantContext.getTenantId());
        othr.setSchmeNm(new SchemeName1Choice());
        othr.getSchmeNm().setPrtry("TENANT_ID");
        orgId.setOthr(othr);
        
        PartyIdentification43.PartyIdentification43Choice choice = new PartyIdentification43.PartyIdentification43Choice();
        choice.setOrgId(orgId);
        initgPty.setId(choice);
        
        return initgPty;
    }
    
    /**
     * Create Payment Information
     */
    private PaymentInstruction22 createPaymentInformation(PaymentInitiationRequest request, TenantContext tenantContext) {
        PaymentInstruction22 pmtInf = new PaymentInstruction22();
        
        // Payment Information ID
        pmtInf.setPmtInfId("PMT-INF-" + request.getPaymentId().getValue());
        
        // Payment Method
        pmtInf.setPmtMtd(PaymentMethod3Code.TRF);
        
        // Batch Booking
        pmtInf.setBtchBookg(false);
        
        // Number of Transactions
        pmtInf.setNbOfTxs("1");
        
        // Control Sum
        pmtInf.setCtrlSum(request.getAmount().getAmount());
        
        // Payment Type Information
        pmtInf.setPmtTpInf(createPaymentTypeInformation(request));
        
        // Required Execution Date
        pmtInf.setReqdExctnDt(LocalDate.now());
        
        // Debtor
        pmtInf.setDbtr(createDebtor(request));
        
        // Debtor Account
        pmtInf.setDbtrAcct(createDebtorAccount(request));
        
        // Debtor Agent
        pmtInf.setDbtrAgt(createDebtorAgent(request));
        
        // Credit Transfer Transaction
        pmtInf.getCdtTrfTx().add(createCreditTransferTransaction(request));
        
        return pmtInf;
    }
    
    /**
     * Create Payment Type Information
     */
    private PaymentTypeInformation26 createPaymentTypeInformation(PaymentInitiationRequest request) {
        PaymentTypeInformation26 pmtTpInf = new PaymentTypeInformation26();
        
        // Service Level
        ServiceLevel8Choice svcLvl = new ServiceLevel8Choice();
        svcLvl.setCd("SEPA"); // Default to SEPA, can be customized
        pmtTpInf.setSvcLvl(svcLvl);
        
        // Local Instrument
        LocalInstrument2Choice lclInstrm = new LocalInstrument2Choice();
        lclInstrm.setCd("INSTANT"); // Default to INSTANT, can be customized
        pmtTpInf.setLclInstrm(lclInstrm);
        
        return pmtTpInf;
    }
    
    /**
     * Create Debtor
     */
    private PartyIdentification43 createDebtor(PaymentInitiationRequest request) {
        PartyIdentification43 dbtr = new PartyIdentification43();
        
        // Debtor Name
        dbtr.setNm("Debtor - " + request.getSourceAccount());
        
        // Debtor ID
        OrganisationIdentification8 orgId = new OrganisationIdentification8();
        OtherIdentification1 othr = new OtherIdentification1();
        othr.setId(request.getSourceAccount());
        othr.setSchmeNm(new SchemeName1Choice());
        othr.getSchmeNm().setPrtry("ACCOUNT");
        orgId.setOthr(othr);
        
        PartyIdentification43.PartyIdentification43Choice choice = new PartyIdentification43.PartyIdentification43Choice();
        choice.setOrgId(orgId);
        dbtr.setId(choice);
        
        return dbtr;
    }
    
    /**
     * Create Debtor Account
     */
    private CashAccount24 createDebtorAccount(PaymentInitiationRequest request) {
        CashAccount24 dbtrAcct = new CashAccount24();
        
        // Account ID
        AccountIdentification4Choice id = new AccountIdentification4Choice();
        GenericAccountIdentification1 othr = new GenericAccountIdentification1();
        othr.setId(request.getSourceAccount());
        othr.setSchmeNm(new AccountSchemeName1Choice());
        othr.getSchmeNm().setPrtry("ACCOUNT");
        id.setOthr(othr);
        dbtrAcct.setId(id);
        
        return dbtrAcct;
    }
    
    /**
     * Create Debtor Agent
     */
    private BranchAndFinancialInstitutionIdentification5 createDebtorAgent(PaymentInitiationRequest request) {
        BranchAndFinancialInstitutionIdentification5 dbtrAgt = new BranchAndFinancialInstitutionIdentification5();
        
        // Financial Institution ID
        FinancialInstitutionIdentification8 finInstnId = new FinancialInstitutionIdentification8();
        finInstnId.setBICFI("ABSAZAJJXXX"); // Default BIC, should be determined from account
        dbtrAgt.setFinInstnId(finInstnId);
        
        return dbtrAgt;
    }
    
    /**
     * Create Credit Transfer Transaction
     */
    private CreditTransferTransaction25 createCreditTransferTransaction(PaymentInitiationRequest request) {
        CreditTransferTransaction25 cdtTrfTx = new CreditTransferTransaction25();
        
        // Payment ID
        cdtTrfTx.setPmtId(createPaymentIdentification(request));
        
        // Amount
        cdtTrfTx.setAmt(createAmount(request.getAmount()));
        
        // Creditor Agent
        cdtTrfTx.setCdtrAgt(createCreditorAgent(request));
        
        // Creditor
        cdtTrfTx.setCdtr(createCreditor(request));
        
        // Creditor Account
        cdtTrfTx.setCdtrAcct(createCreditorAccount(request));
        
        // Remittance Information
        cdtTrfTx.setRmtInf(createRemittanceInformation(request));
        
        return cdtTrfTx;
    }
    
    /**
     * Create Payment Identification
     */
    private PaymentIdentification3 createPaymentIdentification(PaymentInitiationRequest request) {
        PaymentIdentification3 pmtId = new PaymentIdentification3();
        
        // Instruction ID
        pmtId.setInstrId("INSTR-" + request.getPaymentId().getValue());
        
        // End-to-End ID
        pmtId.setEndToEndId("E2E-" + request.getPaymentId().getValue());
        
        return pmtId;
    }
    
    /**
     * Create Amount
     */
    private AmountType3Choice createAmount(Money amount) {
        AmountType3Choice amt = new AmountType3Choice();
        
        // Instructed Amount
        ActiveOrHistoricCurrencyAndAmount instdAmt = new ActiveOrHistoricCurrencyAndAmount();
        instdAmt.setCcy(amount.getCurrency().getCurrencyCode());
        instdAmt.setValue(amount.getAmount());
        amt.setInstdAmt(instdAmt);
        
        return amt;
    }
    
    /**
     * Create Creditor Agent
     */
    private BranchAndFinancialInstitutionIdentification5 createCreditorAgent(PaymentInitiationRequest request) {
        BranchAndFinancialInstitutionIdentification5 cdtrAgt = new BranchAndFinancialInstitutionIdentification5();
        
        // Financial Institution ID
        FinancialInstitutionIdentification8 finInstnId = new FinancialInstitutionIdentification8();
        finInstnId.setBICFI("SBZAZAJJXXX"); // Default BIC, should be determined from account
        cdtrAgt.setFinInstnId(finInstnId);
        
        return cdtrAgt;
    }
    
    /**
     * Create Creditor
     */
    private PartyIdentification43 createCreditor(PaymentInitiationRequest request) {
        PartyIdentification43 cdtr = new PartyIdentification43();
        
        // Creditor Name
        cdtr.setNm("Creditor - " + request.getDestinationAccount());
        
        // Creditor ID
        OrganisationIdentification8 orgId = new OrganisationIdentification8();
        OtherIdentification1 othr = new OtherIdentification1();
        othr.setId(request.getDestinationAccount());
        othr.setSchmeNm(new SchemeName1Choice());
        othr.getSchmeNm().setPrtry("ACCOUNT");
        orgId.setOthr(othr);
        
        PartyIdentification43.PartyIdentification43Choice choice = new PartyIdentification43.PartyIdentification43Choice();
        choice.setOrgId(orgId);
        cdtr.setId(choice);
        
        return cdtr;
    }
    
    /**
     * Create Creditor Account
     */
    private CashAccount24 createCreditorAccount(PaymentInitiationRequest request) {
        CashAccount24 cdtrAcct = new CashAccount24();
        
        // Account ID
        AccountIdentification4Choice id = new AccountIdentification4Choice();
        GenericAccountIdentification1 othr = new GenericAccountIdentification1();
        othr.setId(request.getDestinationAccount());
        othr.setSchmeNm(new AccountSchemeName1Choice());
        othr.getSchmeNm().setPrtry("ACCOUNT");
        id.setOthr(othr);
        cdtrAcct.setId(id);
        
        return cdtrAcct;
    }
    
    /**
     * Create Remittance Information
     */
    private RemittanceInformation11 createRemittanceInformation(PaymentInitiationRequest request) {
        RemittanceInformation11 rmtInf = new RemittanceInformation11();
        
        // Unstructured
        rmtInf.getUstrd().add(request.getReference());
        
        return rmtInf;
    }
}
```

### 4. Create pain.001 Message Parser

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain001/Pain001MessageParser.java`
```java
package com.payments.iso20022.pain001;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.shared.Money;
import com.payments.contracts.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Parser for ISO 20022 pain.001 messages
 * 
 * <p>Parses pain.001 Customer Credit Transfer Initiation messages
 * to internal payment initiation requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain001MessageParser {
    
    /**
     * Parse pain.001 XML message to payment initiation request
     * 
     * @param pain001Xml pain.001 XML message
     * @param tenantContext Tenant context
     * @return Payment initiation request
     */
    public PaymentInitiationRequest parsePain001Xml(String pain001Xml, TenantContext tenantContext) {
        try {
            log.info("Parsing pain.001 XML message");
            
            // Parse XML to Document object
            Document document = parseXmlToDocument(pain001Xml);
            
            // Extract payment information
            PaymentInitiationRequest request = extractPaymentRequest(document, tenantContext);
            
            log.info("Successfully parsed pain.001 XML message");
            return request;
            
        } catch (Exception e) {
            log.error("Failed to parse pain.001 XML message", e);
            throw new Pain001MessageParserException("Failed to parse pain.001 XML message", e);
        }
    }
    
    /**
     * Parse XML to Document object
     */
    private Document parseXmlToDocument(String pain001Xml) {
        // Implementation would use JAXB to parse XML
        // This is a simplified version
        throw new UnsupportedOperationException("XML parsing not yet implemented");
    }
    
    /**
     * Extract payment request from pain.001 document
     */
    private PaymentInitiationRequest extractPaymentRequest(Document document, TenantContext tenantContext) {
        // Extract payment information from pain.001 document
        // This is a simplified version
        throw new UnsupportedOperationException("Payment extraction not yet implemented");
    }
}
```

### 5. Create pain.001 Validator

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain001/Pain001Validator.java`
```java
package com.payments.iso20022.pain001;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for ISO 20022 pain.001 messages
 * 
 * <p>Validates pain.001 messages against XSD schema and business rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain001Validator {
    
    private final Iso20022Validator iso20022Validator;
    
    /**
     * Validate pain.001 XML message against XSD schema
     * 
     * @param pain001Xml pain.001 XML message
     * @return Validation result
     */
    public ValidationResult validateXml(String pain001Xml) {
        try {
            log.info("Validating pain.001 XML message against XSD schema");
            
            // Validate against XSD schema
            ValidationResult xsdResult = validateAgainstXsd(pain001Xml);
            if (!xsdResult.isValid()) {
                return xsdResult;
            }
            
            // Validate business rules
            ValidationResult businessResult = validateBusinessRules(pain001Xml);
            if (!businessResult.isValid()) {
                return businessResult;
            }
            
            log.info("pain.001 XML message validation successful");
            return ValidationResult.valid();
            
        } catch (Exception e) {
            log.error("Failed to validate pain.001 XML message", e);
            return ValidationResult.invalid("Validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate against XSD schema
     */
    private ValidationResult validateAgainstXsd(String pain001Xml) {
        try {
            // Load XSD schema
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(getClass().getResource("/xsd/pain.001.001.09.xsd"));
            
            // Create validator
            Validator validator = schema.newValidator();
            
            // Validate XML
            validator.validate(new StreamSource(new ByteArrayInputStream(pain001Xml.getBytes())));
            
            return ValidationResult.valid();
            
        } catch (Exception e) {
            log.error("XSD validation failed", e);
            return ValidationResult.invalid("XSD validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate business rules
     */
    private ValidationResult validateBusinessRules(String pain001Xml) {
        List<String> errors = new ArrayList<>();
        
        // Add business rule validations here
        // For example:
        // - Check required fields
        // - Validate amounts
        // - Check account formats
        // - Validate dates
        
        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            return ValidationResult.invalid("Business rule validation failed: " + String.join(", ", errors));
        }
    }
}
```

### 6. Create Exception Classes

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain001/Pain001MessageBuilderException.java`
```java
package com.payments.iso20022.pain001;

/**
 * Exception thrown when pain.001 message building fails
 */
public class Pain001MessageBuilderException extends RuntimeException {
    
    public Pain001MessageBuilderException(String message) {
        super(message);
    }
    
    public Pain001MessageBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### File: `domain-models/iso20022/src/main/java/com/payments/iso20022/pain001/Pain001MessageParserException.java`
```java
package com.payments.iso20022.pain001;

/**
 * Exception thrown when pain.001 message parsing fails
 */
public class Pain001MessageParserException extends RuntimeException {
    
    public Pain001MessageParserException(String message) {
        super(message);
    }
    
    public Pain001MessageParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## Testing Implementation

### 1. Unit Tests

#### File: `domain-models/iso20022/src/test/java/com/payments/iso20022/pain001/Pain001MessageBuilderTest.java`
```java
package com.payments.iso20022.pain001;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.shared.Money;
import com.payments.contracts.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class Pain001MessageBuilderTest {
    
    @Mock
    private Iso20022MarshallerService marshallerService;
    
    private Pain001MessageBuilder messageBuilder;
    
    @BeforeEach
    void setUp() {
        messageBuilder = new Pain001MessageBuilder(marshallerService);
    }
    
    @Test
    void shouldBuildValidXmlMessage() {
        // Given
        PaymentInitiationRequest request = createPaymentRequest();
        TenantContext tenantContext = createTenantContext();
        
        // When
        String xml = messageBuilder.buildPain001Xml(request, tenantContext);
        
        // Then
        assertNotNull(xml);
        assertTrue(xml.contains("Document"));
        assertTrue(xml.contains("CstmrCdtTrfInitn"));
    }
    
    @Test
    void shouldThrowExceptionWhenBuildingFails() {
        // Given
        PaymentInitiationRequest request = createPaymentRequest();
        TenantContext tenantContext = createTenantContext();
        
        // When/Then
        assertThrows(Pain001MessageBuilderException.class, () -> {
            messageBuilder.buildPain001Xml(request, tenantContext);
        });
    }
    
    private PaymentInitiationRequest createPaymentRequest() {
        return PaymentInitiationRequest.builder()
                .paymentId(PaymentId.of("PAY-2025-001"))
                .idempotencyKey("IDEMPOTENCY-001")
                .sourceAccount("1234567890")
                .destinationAccount("0987654321")
                .amount(Money.builder()
                        .amount(new BigDecimal("1000.00"))
                        .currency(Currency.getInstance("ZAR"))
                        .build())
                .reference("Payment reference")
                .paymentType(PaymentType.EFT)
                .priority(Priority.NORMAL)
                .tenantContext(createTenantContext())
                .initiatedBy("user@example.com")
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

#### File: `domain-models/iso20022/src/test/java/com/payments/iso20022/pain001/Pain001IntegrationTest.java`
```java
package com.payments.iso20022.pain001;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.shared.Money;
import com.payments.contracts.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class Pain001IntegrationTest {
    
    @Test
    void shouldBuildAndValidatePain001Message() {
        // Given
        PaymentInitiationRequest request = createPaymentRequest();
        TenantContext tenantContext = createTenantContext();
        
        Pain001MessageBuilder builder = new Pain001MessageBuilder(marshallerService);
        Pain001Validator validator = new Pain001Validator(iso20022Validator);
        
        // When
        String xml = builder.buildPain001Xml(request, tenantContext);
        ValidationResult result = validator.validateXml(xml);
        
        // Then
        assertTrue(result.isValid());
        assertNotNull(xml);
    }
    
    private PaymentInitiationRequest createPaymentRequest() {
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
1. **Add pain.001 to enum**
2. **Update namespace and package configuration**
3. **Add XSD schema file**
4. **Test enum changes**

### Step 2: Create Message Builder (Day 2)
1. **Create Pain001MessageBuilder class**
2. **Implement XML building logic**
3. **Add error handling**
4. **Create unit tests**

### Step 3: Create Message Parser (Day 3)
1. **Create Pain001MessageParser class**
2. **Implement XML parsing logic**
3. **Add error handling**
4. **Create unit tests**

### Step 4: Create Validator (Day 4)
1. **Create Pain001Validator class**
2. **Implement XSD validation**
3. **Add business rule validation**
4. **Create unit tests**

### Step 5: Integration Testing (Day 5)
1. **Create integration tests**
2. **Test end-to-end flow**
3. **Validate against real pain.001 messages**
4. **Performance testing**

---

## Success Criteria

### Functional Requirements
- ✅ pain.001 message building from internal requests
- ✅ pain.001 message parsing to internal requests
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

1. **Feature 2**: Add JSON support for pain.001 messages
2. **Feature 3**: Create channel pain.001 adapters
3. **Feature 4**: Update payment initiation service
4. **Feature 5**: Add end-to-end testing

---

**Feature Version**: 1.0  
**Created**: 2025-01-27  
**Estimated Effort**: 3-4 days  
**Dependencies**: None  
**Risk Level**: Low
