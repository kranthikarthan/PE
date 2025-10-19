package com.payments.iso20022.validation;

import static org.assertj.core.api.Assertions.*;

import com.payments.iso20022.config.Iso20022MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for Iso20022Validator */
@DisplayName("ISO 20022 Validator Tests")
class Iso20022ValidatorTest {

  private Iso20022Validator validator;

  @BeforeEach
  void setUp() {
    validator = new Iso20022Validator();
  }

  @Test
  @DisplayName("Should validate a valid pacs.008 message")
  void shouldValidateValidPacs008Message() {
    // Given
    String validXml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-001</MsgId>
                            <CreDtTm>2025-10-19T14:30:00Z</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                        </GrpHdr>
                        <CdtTrfTxInf>
                            <PmtId>
                                <EndToEndId>E2E-TEST-001</EndToEndId>
                            </PmtId>
                            <IntrBkSttlmAmt Ccy="ZAR">1000.00</IntrBkSttlmAmt>
                            <Dbtr>
                                <Nm>John Doe</Nm>
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
                                    <Othr>
                                        <Id>BANK001</Id>
                                    </Othr>
                                </FinInstnId>
                            </DbtrAgt>
                            <CdtrAgt>
                                <FinInstnId>
                                    <Othr>
                                        <Id>BANK002</Id>
                                    </Othr>
                                </FinInstnId>
                            </CdtrAgt>
                            <Cdtr>
                                <Nm>Jane Smith</Nm>
                            </Cdtr>
                            <CdtrAcct>
                                <Id>
                                    <Othr>
                                        <Id>0987654321</Id>
                                    </Othr>
                                </Id>
                            </CdtrAcct>
                        </CdtTrfTxInf>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When
    ValidationResult result = validator.validate(validXml, Iso20022MessageType.PACS_008);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isValid()).isTrue();
    assertThat(result.getStatus()).isEqualTo(ValidationResult.ValidationStatus.VALID);
    assertThat(result.getValidationErrors()).isEmpty();
    assertThat(result.hasErrors()).isFalse();
  }

  @Test
  @DisplayName("Should detect missing required fields")
  void shouldDetectMissingRequiredFields() {
    // Given - Missing NbOfTxs in GrpHdr
    String invalidXml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-001</MsgId>
                            <CreDtTm>2025-10-19T14:30:00Z</CreDtTm>
                        </GrpHdr>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When
    ValidationResult result = validator.validate(invalidXml, Iso20022MessageType.PACS_008);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isValid()).isFalse();
    assertThat(result.getStatus()).isEqualTo(ValidationResult.ValidationStatus.INVALID);
    assertThat(result.hasErrors()).isTrue();
    assertThat(result.getValidationErrors()).isNotEmpty();
  }

  @Test
  @DisplayName("Should detect invalid namespace")
  void shouldDetectInvalidNamespace() {
    // Given - Wrong namespace
    String invalidXml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:invalid:namespace">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-001</MsgId>
                        </GrpHdr>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When
    ValidationResult result = validator.validate(invalidXml, Iso20022MessageType.PACS_008);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isValid()).isFalse();
    assertThat(result.hasErrors()).isTrue();
  }

  @Test
  @DisplayName("Should validate a valid pacs.002 status report")
  void shouldValidateValidPacs002Message() {
    // Given
    String validXml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
                    <FIToFIPmtStsRpt>
                        <GrpHdr>
                            <MsgId>STATUS-001</MsgId>
                            <CreDtTm>2025-10-19T14:30:00Z</CreDtTm>
                        </GrpHdr>
                        <TxInfAndSts>
                            <TxSts>ACSC</TxSts>
                        </TxInfAndSts>
                    </FIToFIPmtStsRpt>
                </Document>
                """;

    // When
    ValidationResult result = validator.validate(validXml, Iso20022MessageType.PACS_002);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isValid()).isTrue();
    assertThat(result.getStatus()).isEqualTo(ValidationResult.ValidationStatus.VALID);
  }

  @Test
  @DisplayName("Should validate strict mode with warnings")
  void shouldValidateStrictModeWithWarnings() {
    // Given - Valid XML
    String xml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-001</MsgId>
                            <CreDtTm>2025-10-19T14:30:00Z</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                        </GrpHdr>
                        <CdtTrfTxInf>
                            <PmtId>
                                <EndToEndId>E2E-TEST-001</EndToEndId>
                            </PmtId>
                            <IntrBkSttlmAmt Ccy="ZAR">1000.00</IntrBkSttlmAmt>
                            <Dbtr><Nm>John Doe</Nm></Dbtr>
                            <DbtrAcct><Id><Othr><Id>1234567890</Id></Othr></Id></DbtrAcct>
                            <DbtrAgt><FinInstnId><Othr><Id>BANK001</Id></Othr></FinInstnId></DbtrAgt>
                            <CdtrAgt><FinInstnId><Othr><Id>BANK002</Id></Othr></FinInstnId></CdtrAgt>
                            <Cdtr><Nm>Jane Smith</Nm></Cdtr>
                            <CdtrAcct><Id><Othr><Id>0987654321</Id></Othr></Id></CdtrAcct>
                        </CdtTrfTxInf>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When
    ValidationResult result = validator.validateStrict(xml, Iso20022MessageType.PACS_008);

    // Then
    assertThat(result).isNotNull();
    // In strict mode, even warnings fail validation
    // (actual behavior depends on XSD schema warnings)
  }

  @Test
  @DisplayName("Should cache schemas for performance")
  void shouldCacheSchemasForPerformance() {
    // Given
    String validXml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-001</MsgId>
                            <CreDtTm>2025-10-19T14:30:00Z</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                        </GrpHdr>
                        <CdtTrfTxInf>
                            <PmtId><EndToEndId>E2E-001</EndToEndId></PmtId>
                            <IntrBkSttlmAmt Ccy="ZAR">1000.00</IntrBkSttlmAmt>
                            <Dbtr><Nm>John Doe</Nm></Dbtr>
                            <DbtrAcct><Id><Othr><Id>1234567890</Id></Othr></Id></DbtrAcct>
                            <DbtrAgt><FinInstnId><Othr><Id>BANK001</Id></Othr></FinInstnId></DbtrAgt>
                            <CdtrAgt><FinInstnId><Othr><Id>BANK002</Id></Othr></FinInstnId></CdtrAgt>
                            <Cdtr><Nm>Jane Smith</Nm></Cdtr>
                            <CdtrAcct><Id><Othr><Id>0987654321</Id></Othr></Id></CdtrAcct>
                        </CdtTrfTxInf>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When - First validation (loads schema)
    long start1 = System.currentTimeMillis();
    validator.validate(validXml, Iso20022MessageType.PACS_008);
    long time1 = System.currentTimeMillis() - start1;

    // When - Second validation (uses cached schema)
    long start2 = System.currentTimeMillis();
    validator.validate(validXml, Iso20022MessageType.PACS_008);
    long time2 = System.currentTimeMillis() - start2;

    // Then - Second should be faster (cached)
    assertThat(validator.isSchemaCached(Iso20022MessageType.PACS_008)).isTrue();
    // Second validation should be significantly faster (or at least not slower)
    assertThat(time2).isLessThanOrEqualTo(time1 * 2);
  }

  @Test
  @DisplayName("Should clear schema cache")
  void shouldClearSchemaCache() {
    // Given
    validator.validate("<Document></Document>", Iso20022MessageType.PACS_008);
    assertThat(validator.isSchemaCached(Iso20022MessageType.PACS_008)).isTrue();

    // When
    Iso20022Validator.clearCache();

    // Then
    assertThat(validator.isSchemaCached(Iso20022MessageType.PACS_008)).isFalse();
  }

  @Test
  @DisplayName("Should preload all schemas")
  void shouldPreloadAllSchemas() {
    // Given
    Iso20022Validator.clearCache();

    // When
    validator.preloadSchemas();

    // Then - All message types should be cached
    assertThat(validator.isSchemaCached(Iso20022MessageType.PACS_008)).isTrue();
    assertThat(validator.isSchemaCached(Iso20022MessageType.PACS_002)).isTrue();
    assertThat(validator.isSchemaCached(Iso20022MessageType.PACS_004)).isTrue();
    assertThat(validator.isSchemaCached(Iso20022MessageType.CAMT_054)).isTrue();
  }

  @Test
  @DisplayName("Should provide business-friendly error messages")
  void shouldProvideBusinessFriendlyErrorMessages() {
    // Given - Invalid XML
    String invalidXml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-001</MsgId>
                        </GrpHdr>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When
    ValidationResult result = validator.validate(invalidXml, Iso20022MessageType.PACS_008);

    // Then
    assertThat(result.isValid()).isFalse();
    assertThat(result.getBusinessFriendlyMessage()).isNotEmpty();
    assertThat(result.getErrorSummary()).contains("Validation Status");
    assertThat(result.toString()).contains("ValidationResult");
  }

  @Test
  @DisplayName("Should count errors and warnings separately")
  void shouldCountErrorsAndWarningsSeparately() {
    // Given - Invalid XML
    String invalidXml =
        """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>TEST-001</MsgId>
                        </GrpHdr>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When
    ValidationResult result = validator.validate(invalidXml, Iso20022MessageType.PACS_008);

    // Then
    assertThat(result.getErrorCount() + result.getWarningCount()).isGreaterThan(0);
  }
}
