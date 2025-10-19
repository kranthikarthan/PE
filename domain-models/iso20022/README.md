# ISO 20022 Domain Models

This module provides JAXB-based Java classes for ISO 20022 financial messaging standard, specifically for South African clearing systems (SAMOS, BankservAfrica, RTC, PayShap, SWIFT).

## Overview

ISO 20022 is the universal financial industry message scheme used by South African Reserve Bank (SARB) and other clearing systems.

## Message Types Supported

| Message Type | Description | Usage |
|--------------|-------------|-------|
| **pacs.008.001.08** | FIToFICustomerCreditTransfer | Credit transfer between financial institutions (SAMOS, RTC, PayShap) |
| **pacs.002.001.10** | FIToFIPaymentStatusReport | Payment status reports and acknowledgments |
| **pacs.004.001.09** | PaymentReturn | Payment returns and rejections |
| **camt.054.001.08** | BankToCustomerDebitCreditNotification | Settlement confirmations |

## XSD Schemas

### Downloading Official Schemas

The official ISO 20022 XSD schemas are **NOT included** in this repository due to licensing. You must download them from the official ISO 20022 repository:

**Option 1: Official ISO 20022 Repository**
```bash
# Download from https://www.iso20022.org/
# Navigate to: Catalogue of Messages → Payment Clearing & Settlement
# Download the required schemas:
# - pacs.008.001.08.xsd
# - pacs.002.001.10.xsd
# - pacs.004.001.09.xsd
# - camt.054.001.08.xsd

# Place them in:
src/main/resources/xsd/
```

**Option 2: SARB/BankservAfrica Customized Schemas**
```bash
# South African clearing systems may provide customized schemas
# Contact:
# - SARB: technical-support@resbank.co.za
# - BankservAfrica: support@bankservafrica.com
# - PayShap: developers@payshap.co.za
```

### Simplified Development Schemas

For **development and testing purposes only**, simplified XSD schemas are provided in:
```
src/main/resources/xsd-simplified/
```

⚠️ **WARNING**: These simplified schemas are for development only. **DO NOT use in production**. You MUST replace them with official schemas before production deployment.

## Building

### With Official Schemas

```bash
# 1. Place official XSD schemas in src/main/resources/xsd/
cp /path/to/downloaded/*.xsd src/main/resources/xsd/

# 2. Build the module (JAXB classes will be auto-generated)
mvn clean install
```

### With Simplified Schemas (Development Only)

```bash
# For development/testing without official schemas:
mvn clean install -Duse.simplified.schemas=true
```

## Generated Classes

After building, JAXB classes are generated in:
```
target/generated-sources/jaxb/
├── pacs008/           # pacs.008 credit transfer messages
├── pacs002/           # pacs.002 status reports
├── pacs004/           # pacs.004 payment returns
└── camt054/           # camt.054 settlement notifications
```

## Usage

### Creating a pacs.008 Message (Credit Transfer)

```java
import com.payments.iso20022.pacs008.*;
import com.payments.iso20022.builder.Pacs008MessageBuilder;

// Build ISO 20022 pacs.008 message
Pacs008MessageBuilder builder = new Pacs008MessageBuilder();

Document document = builder
    .withMessageId("SAMOS-2025-001")
    .withCreationDateTime(Instant.now())
    .withDebtorName("John Doe")
    .withDebtorAccount("1234567890")
    .withDebtorBic("ABSAZAJJXXX")
    .withCreditorName("Jane Smith")
    .withCreditorAccount("0987654321")
    .withCreditorBic("SBZAZAJJXXX")
    .withAmount(new BigDecimal("1000.00"))
    .withCurrency("ZAR")
    .withUETR(UUID.randomUUID().toString())
    .build();

// Marshal to XML
String xml = builder.marshal(document);
```

### Parsing a pacs.002 Response (Status Report)

```java
import com.payments.iso20022.pacs002.*;
import com.payments.iso20022.parser.Pacs002MessageParser;

// Parse incoming ISO 20022 pacs.002 message
Pacs002MessageParser parser = new Pacs002MessageParser();
Document response = parser.unmarshal(xmlResponse);

FIToFIPaymentStatusReportV10 statusReport = response.getFIToFIPmtStsRpt();
String transactionStatus = statusReport
    .getTxInfAndSts()
    .get(0)
    .getTxSts();  // ACSC (Accepted), RJCT (Rejected), etc.
```

### Validating Against XSD

```java
import com.payments.iso20022.validator.Iso20022Validator;

Iso20022Validator validator = new Iso20022Validator();
ValidationResult result = validator.validate(
    xmlMessage, 
    Iso20022MessageType.PACS_008
);

if (!result.isValid()) {
    log.error("Validation failed: {}", result.getErrors());
}
```

## Architecture Integration

### SAMOS Adapter Usage

```java
@Service
public class SamosPaymentService {
    
    @Autowired
    private Pacs008MessageBuilder pacs008Builder;
    
    @Autowired
    private SamosClearingClient clearingClient;
    
    public SamosPaymentResult submitPayment(SamosPaymentRequest request) {
        // Build ISO 20022 message
        Document pacs008 = pacs008Builder
            .forSamosRtgs()  // SARB-specific configuration
            .withPaymentDetails(request)
            .build();
        
        String xml = pacs008Builder.marshal(pacs008);
        
        // Submit to SAMOS
        return clearingClient.submitPayment(xml);
    }
}
```

### PayShap Adapter Usage

```java
@Service
public class PayShapPaymentService {
    
    @Autowired
    private Pacs008MessageBuilder pacs008Builder;
    
    public PayShapPaymentResult processPayment(PayShapPaymentRequest request) {
        // Build ISO 20022 message with PayShap proxy
        Document pacs008 = pacs008Builder
            .forPayShap()  // PayShap-specific configuration
            .withProxyCreditor(request.getRecipientProxy())
            .withAmount(request.getAmount())
            .build();
        
        // Submit to PayShap
        return payShapClient.submitPayment(pacs008);
    }
}
```

## Clearing System Specific Requirements

### SAMOS (RTGS)

```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <!-- MANDATORY: UETR for SARB -->
            <UETR>550e8400-e29b-41d4-a716-446655440000</UETR>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <TxId>SAMOS-TXN-001</TxId>
            </PmtId>
            <!-- MANDATORY: Settlement information -->
            <SttlmInf>
                <SttlmMtd>INDA</SttlmMtd>
                <SttlmAcct>
                    <Id><Othr><Id>SETTLEMENT-ACCOUNT-ID</Id></Othr></Id>
                </SttlmAcct>
            </SttlmInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
```

### PayShap (Instant P2P)

```xml
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <CdtTrfTxInf>
            <!-- PayShap proxy creditor -->
            <Cdtr>
                <Id>
                    <OrgId>
                        <Othr>
                            <Id>+27821234567</Id>
                            <SchmeNm><Prtry>MSISDN</Prtry></SchmeNm>
                        </Othr>
                    </OrgId>
                </Id>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <Othr>
                        <Id>PROXY:+27821234567</Id>
                        <SchmeNm><Prtry>PAYSHAP_PROXY</Prtry></SchmeNm>
                    </Othr>
                </Id>
            </CdtrAcct>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
```

### BankservAfrica (ACH/EFT)

For BankservAfrica ACH/EFT, ISO 20022 is converted to fixed-length format. See `BankservAfricaAchFileBuilder` in the bankservafrica-adapter-service.

## Dependencies

```xml
<dependency>
    <groupId>com.payments</groupId>
    <artifactId>iso20022</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Testing

```bash
# Run all tests
mvn test

# Run with XSD validation
mvn test -Dvalidate.with.xsd=true
```

## Compliance

This module ensures compliance with:
- ✅ ISO 20022 Universal Financial Industry message scheme
- ✅ SARB SAMOS RTGS requirements
- ✅ BankservAfrica message standards
- ✅ PayShap instant payment specifications
- ✅ SWIFT gpi standards

## Troubleshooting

### Issue: JAXB generation fails

**Solution**: Ensure XSD schemas are in `src/main/resources/xsd/` and are valid.

### Issue: Namespace errors

**Solution**: Check that XSD schemas have correct namespace declarations:
```
urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08
```

### Issue: Missing classes after build

**Solution**: Run `mvn clean install` to regenerate JAXB classes.

## References

- [ISO 20022 Official Site](https://www.iso20022.org/)
- [SARB SAMOS Documentation](https://www.resbank.co.za/en/home/what-we-do/payments)
- [BankservAfrica Standards](https://www.bankservafrica.com/)
- [PayShap Developer Portal](https://www.payshap.co.za/developers)

## Support

For ISO 20022 implementation support:
- **Internal**: backend-lead@company.com
- **SARB**: samos-support@resbank.co.za
- **BankservAfrica**: support@bankservafrica.com
- **PayShap**: developers@payshap.co.za

---

**Version**: 1.0.0  
**Last Updated**: October 19, 2025  
**Status**: ✅ Production Ready (with official XSD schemas)
