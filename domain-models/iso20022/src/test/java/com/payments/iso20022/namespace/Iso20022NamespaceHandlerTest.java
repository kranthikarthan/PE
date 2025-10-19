package com.payments.iso20022.namespace;

import static org.assertj.core.api.Assertions.*;

import com.payments.iso20022.config.Iso20022MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for Iso20022NamespaceHandler */
@DisplayName("ISO 20022 Namespace Handler Tests")
class Iso20022NamespaceHandlerTest {

  private static final String PACS008_WITH_NAMESPACE =
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

  private static final String PACS008_WITHOUT_NAMESPACE =
      """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document>
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>TEST-001</MsgId>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

  private static final String PACS008_WITH_PREFIX =
      """
            <?xml version="1.0" encoding="UTF-8"?>
            <pacs:Document xmlns:pacs="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <pacs:FIToFICstmrCdtTrf>
                    <pacs:GrpHdr>
                        <pacs:MsgId>TEST-001</pacs:MsgId>
                    </pacs:GrpHdr>
                </pacs:FIToFICstmrCdtTrf>
            </pacs:Document>
            """;

  private static final String PACS008_ALTERNATIVE_VERSION =
      """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>TEST-001</MsgId>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

  @Test
  @DisplayName("Should detect message type from XML with namespace")
  void shouldDetectMessageTypeFromXmlWithNamespace() {
    // When
    Iso20022MessageType detected =
        Iso20022NamespaceHandler.detectMessageType(PACS008_WITH_NAMESPACE);

    // Then
    assertThat(detected).isEqualTo(Iso20022MessageType.PACS_008);
  }

  @Test
  @DisplayName("Should detect message type from XML with alternative version")
  void shouldDetectMessageTypeFromXmlWithAlternativeVersion() {
    // When
    Iso20022MessageType detected =
        Iso20022NamespaceHandler.detectMessageType(PACS008_ALTERNATIVE_VERSION);

    // Then
    assertThat(detected).isEqualTo(Iso20022MessageType.PACS_008);
  }

  @Test
  @DisplayName("Should return null for XML without namespace")
  void shouldReturnNullForXmlWithoutNamespace() {
    // When
    Iso20022MessageType detected =
        Iso20022NamespaceHandler.detectMessageType(PACS008_WITHOUT_NAMESPACE);

    // Then
    assertThat(detected).isNull();
  }

  @Test
  @DisplayName("Should return null for null or empty XML")
  void shouldReturnNullForNullOrEmptyXml() {
    // Then
    assertThat(Iso20022NamespaceHandler.detectMessageType(null)).isNull();
    assertThat(Iso20022NamespaceHandler.detectMessageType("")).isNull();
  }

  @Test
  @DisplayName("Should extract namespace from XML")
  void shouldExtractNamespaceFromXml() {
    // When
    String namespace = Iso20022NamespaceHandler.extractNamespace(PACS008_WITH_NAMESPACE);

    // Then
    assertThat(namespace).isEqualTo("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
  }

  @Test
  @DisplayName("Should extract namespace from XML with prefix")
  void shouldExtractNamespaceFromXmlWithPrefix() {
    // When
    String namespace = Iso20022NamespaceHandler.extractNamespace(PACS008_WITH_PREFIX);

    // Then
    assertThat(namespace).isEqualTo("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
  }

  @Test
  @DisplayName("Should return null when extracting namespace from XML without namespace")
  void shouldReturnNullWhenExtractingNamespaceFromXmlWithoutNamespace() {
    // When
    String namespace = Iso20022NamespaceHandler.extractNamespace(PACS008_WITHOUT_NAMESPACE);

    // Then
    assertThat(namespace).isNull();
  }

  @Test
  @DisplayName("Should extract prefix from XML")
  void shouldExtractPrefixFromXml() {
    // When
    String prefix = Iso20022NamespaceHandler.extractPrefix(PACS008_WITH_PREFIX);

    // Then
    assertThat(prefix).isEqualTo("pacs");
  }

  @Test
  @DisplayName("Should return null when extracting prefix from XML without prefix")
  void shouldReturnNullWhenExtractingPrefixFromXmlWithoutPrefix() {
    // When
    String prefix = Iso20022NamespaceHandler.extractPrefix(PACS008_WITH_NAMESPACE);

    // Then
    assertThat(prefix).isNull();
  }

  @Test
  @DisplayName("Should strip namespace from XML")
  void shouldStripNamespaceFromXml() {
    // When
    String stripped = Iso20022NamespaceHandler.stripNamespace(PACS008_WITH_NAMESPACE);

    // Then
    assertThat(stripped).doesNotContain("xmlns");
    assertThat(stripped).contains("<Document>");
    assertThat(stripped).contains("<GrpHdr>");
  }

  @Test
  @DisplayName("Should strip namespace and prefix from XML")
  void shouldStripNamespaceAndPrefixFromXml() {
    // When
    String stripped = Iso20022NamespaceHandler.stripNamespace(PACS008_WITH_PREFIX);

    // Then
    assertThat(stripped).doesNotContain("xmlns");
    assertThat(stripped).doesNotContain("pacs:");
    assertThat(stripped).contains("<Document>");
    assertThat(stripped).contains("<GrpHdr>");
  }

  @Test
  @DisplayName("Should handle null or empty XML in stripNamespace")
  void shouldHandleNullOrEmptyXmlInStripNamespace() {
    // Then
    assertThat(Iso20022NamespaceHandler.stripNamespace(null)).isNull();
    assertThat(Iso20022NamespaceHandler.stripNamespace("")).isEmpty();
  }

  @Test
  @DisplayName("Should add namespace to XML")
  void shouldAddNamespaceToXml() {
    // When
    String withNamespace =
        Iso20022NamespaceHandler.addNamespace(
            PACS008_WITHOUT_NAMESPACE, Iso20022MessageType.PACS_008);

    // Then
    assertThat(withNamespace).contains("xmlns=");
    assertThat(withNamespace).contains("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
  }

  @Test
  @DisplayName("Should not add namespace if already present")
  void shouldNotAddNamespaceIfAlreadyPresent() {
    // When
    String result =
        Iso20022NamespaceHandler.addNamespace(PACS008_WITH_NAMESPACE, Iso20022MessageType.PACS_008);

    // Then
    assertThat(result).isEqualTo(PACS008_WITH_NAMESPACE);
  }

  @Test
  @DisplayName("Should handle null or empty XML in addNamespace")
  void shouldHandleNullOrEmptyXmlInAddNamespace() {
    // Then
    assertThat(Iso20022NamespaceHandler.addNamespace(null, Iso20022MessageType.PACS_008)).isNull();
    assertThat(Iso20022NamespaceHandler.addNamespace("", Iso20022MessageType.PACS_008)).isEmpty();
  }

  @Test
  @DisplayName("Should normalize namespace to standard version")
  void shouldNormalizeNamespaceToStandardVersion() {
    // When
    String normalized =
        Iso20022NamespaceHandler.normalizeNamespace(
            PACS008_ALTERNATIVE_VERSION, Iso20022MessageType.PACS_008);

    // Then
    assertThat(normalized).contains("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
    assertThat(normalized).doesNotContain("pacs.008.001.10");
  }

  @Test
  @DisplayName("Should not change XML if already using standard namespace")
  void shouldNotChangeXmlIfAlreadyUsingStandardNamespace() {
    // When
    String normalized =
        Iso20022NamespaceHandler.normalizeNamespace(
            PACS008_WITH_NAMESPACE, Iso20022MessageType.PACS_008);

    // Then
    assertThat(normalized).isEqualTo(PACS008_WITH_NAMESPACE);
  }

  @Test
  @DisplayName("Should add namespace if missing during normalization")
  void shouldAddNamespaceIfMissingDuringNormalization() {
    // When
    String normalized =
        Iso20022NamespaceHandler.normalizeNamespace(
            PACS008_WITHOUT_NAMESPACE, Iso20022MessageType.PACS_008);

    // Then
    assertThat(normalized).contains("xmlns=");
    assertThat(normalized).contains("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
  }

  @Test
  @DisplayName("Should validate correct namespace")
  void shouldValidateCorrectNamespace() {
    // When
    boolean isValid =
        Iso20022NamespaceHandler.validateNamespace(
            PACS008_WITH_NAMESPACE, Iso20022MessageType.PACS_008);

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Should validate alternative namespace version")
  void shouldValidateAlternativeNamespaceVersion() {
    // When
    boolean isValid =
        Iso20022NamespaceHandler.validateNamespace(
            PACS008_ALTERNATIVE_VERSION, Iso20022MessageType.PACS_008);

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Should reject invalid namespace")
  void shouldRejectInvalidNamespace() {
    // Given
    String invalidXml =
        """
                <Document xmlns="urn:invalid:namespace">
                    <GrpHdr></GrpHdr>
                </Document>
                """;

    // When
    boolean isValid =
        Iso20022NamespaceHandler.validateNamespace(invalidXml, Iso20022MessageType.PACS_008);

    // Then
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Should reject XML without namespace")
  void shouldRejectXmlWithoutNamespace() {
    // When
    boolean isValid =
        Iso20022NamespaceHandler.validateNamespace(
            PACS008_WITHOUT_NAMESPACE, Iso20022MessageType.PACS_008);

    // Then
    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("Should check if XML has namespace")
  void shouldCheckIfXmlHasNamespace() {
    // Then
    assertThat(Iso20022NamespaceHandler.hasNamespace(PACS008_WITH_NAMESPACE)).isTrue();
    assertThat(Iso20022NamespaceHandler.hasNamespace(PACS008_WITH_PREFIX)).isTrue();
    assertThat(Iso20022NamespaceHandler.hasNamespace(PACS008_WITHOUT_NAMESPACE)).isFalse();
    assertThat(Iso20022NamespaceHandler.hasNamespace(null)).isFalse();
  }

  @Test
  @DisplayName("Should check if XML has prefix")
  void shouldCheckIfXmlHasPrefix() {
    // Then
    assertThat(Iso20022NamespaceHandler.hasPrefix(PACS008_WITH_PREFIX)).isTrue();
    assertThat(Iso20022NamespaceHandler.hasPrefix(PACS008_WITH_NAMESPACE)).isFalse();
    assertThat(Iso20022NamespaceHandler.hasPrefix(PACS008_WITHOUT_NAMESPACE)).isFalse();
  }

  @Test
  @DisplayName("Should get namespace info for debugging")
  void shouldGetNamespaceInfoForDebugging() {
    // When
    String info = Iso20022NamespaceHandler.getNamespaceInfo(PACS008_WITH_NAMESPACE);

    // Then
    assertThat(info).contains("Namespace URI:");
    assertThat(info).contains("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
    assertThat(info).contains("Message Type:");
    assertThat(info).contains("PACS_008");
  }

  @Test
  @DisplayName("Should handle malformed XML gracefully")
  void shouldHandleMalformedXmlGracefully() {
    // Given
    String malformed = "<Document>Malformed";

    // Then - Should not throw exceptions
    assertThatCode(() -> Iso20022NamespaceHandler.detectMessageType(malformed))
        .doesNotThrowAnyException();
    assertThatCode(() -> Iso20022NamespaceHandler.extractNamespace(malformed))
        .doesNotThrowAnyException();
    assertThatCode(() -> Iso20022NamespaceHandler.stripNamespace(malformed))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Should handle complex nested XML structures")
  void shouldHandleComplexNestedXmlStructures() {
    // Given
    String complexXml =
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
                                <EndToEndId>E2E-001</EndToEndId>
                            </PmtId>
                            <IntrBkSttlmAmt Ccy="ZAR">1000.00</IntrBkSttlmAmt>
                        </CdtTrfTxInf>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

    // When
    String stripped = Iso20022NamespaceHandler.stripNamespace(complexXml);
    String namespace = Iso20022NamespaceHandler.extractNamespace(complexXml);
    Iso20022MessageType detected = Iso20022NamespaceHandler.detectMessageType(complexXml);

    // Then
    assertThat(stripped).doesNotContain("xmlns");
    assertThat(namespace).isNotNull();
    assertThat(detected).isEqualTo(Iso20022MessageType.PACS_008);
  }
}
