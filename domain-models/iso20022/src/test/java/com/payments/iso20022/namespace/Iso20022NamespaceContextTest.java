package com.payments.iso20022.namespace;

import static org.assertj.core.api.Assertions.*;

import com.payments.iso20022.config.Iso20022MessageType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for Iso20022NamespaceContext */
@DisplayName("ISO 20022 Namespace Context Tests")
class Iso20022NamespaceContextTest {

  @Test
  @DisplayName("Should get standard namespace for pacs.008")
  void shouldGetStandardNamespaceForPacs008() {
    // When
    String namespace = Iso20022NamespaceContext.getStandardNamespace(Iso20022MessageType.PACS_008);

    // Then
    assertThat(namespace).isEqualTo("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
  }

  @Test
  @DisplayName("Should get standard namespace for all message types")
  void shouldGetStandardNamespaceForAllMessageTypes() {
    // Then
    assertThat(Iso20022NamespaceContext.getStandardNamespace(Iso20022MessageType.PACS_008))
        .isNotEmpty();
    assertThat(Iso20022NamespaceContext.getStandardNamespace(Iso20022MessageType.PACS_002))
        .isNotEmpty();
    assertThat(Iso20022NamespaceContext.getStandardNamespace(Iso20022MessageType.PACS_004))
        .isNotEmpty();
    assertThat(Iso20022NamespaceContext.getStandardNamespace(Iso20022MessageType.CAMT_054))
        .isNotEmpty();
  }

  @Test
  @DisplayName("Should get supported namespaces including alternatives")
  void shouldGetSupportedNamespacesIncludingAlternatives() {
    // When
    List<String> namespaces =
        Iso20022NamespaceContext.getSupportedNamespaces(Iso20022MessageType.PACS_008);

    // Then
    assertThat(namespaces).isNotEmpty();
    assertThat(namespaces).contains("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
    assertThat(namespaces).hasSizeGreaterThan(1); // Should include alternatives
  }

  @Test
  @DisplayName("Should detect message type from standard namespace")
  void shouldDetectMessageTypeFromStandardNamespace() {
    // Given
    String namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08";

    // When
    Iso20022MessageType detected = Iso20022NamespaceContext.detectMessageType(namespace);

    // Then
    assertThat(detected).isEqualTo(Iso20022MessageType.PACS_008);
  }

  @Test
  @DisplayName("Should detect message type from alternative namespace")
  void shouldDetectMessageTypeFromAlternativeNamespace() {
    // Given
    String namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10";

    // When
    Iso20022MessageType detected = Iso20022NamespaceContext.detectMessageType(namespace);

    // Then
    assertThat(detected).isEqualTo(Iso20022MessageType.PACS_008);
  }

  @Test
  @DisplayName("Should detect all message types from their namespaces")
  void shouldDetectAllMessageTypesFromTheirNamespaces() {
    // Given
    String pacs008 = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08";
    String pacs002 = "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10";
    String pacs004 = "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09";
    String camt054 = "urn:iso:std:iso:20022:tech:xsd:camt.054.001.08";

    // Then
    assertThat(Iso20022NamespaceContext.detectMessageType(pacs008))
        .isEqualTo(Iso20022MessageType.PACS_008);
    assertThat(Iso20022NamespaceContext.detectMessageType(pacs002))
        .isEqualTo(Iso20022MessageType.PACS_002);
    assertThat(Iso20022NamespaceContext.detectMessageType(pacs004))
        .isEqualTo(Iso20022MessageType.PACS_004);
    assertThat(Iso20022NamespaceContext.detectMessageType(camt054))
        .isEqualTo(Iso20022MessageType.CAMT_054);
  }

  @Test
  @DisplayName("Should return null for unknown namespace")
  void shouldReturnNullForUnknownNamespace() {
    // Given
    String unknown = "urn:unknown:namespace";

    // When
    Iso20022MessageType detected = Iso20022NamespaceContext.detectMessageType(unknown);

    // Then
    assertThat(detected).isNull();
  }

  @Test
  @DisplayName("Should return null for null or empty namespace")
  void shouldReturnNullForNullOrEmptyNamespace() {
    // Then
    assertThat(Iso20022NamespaceContext.detectMessageType(null)).isNull();
    assertThat(Iso20022NamespaceContext.detectMessageType("")).isNull();
  }

  @Test
  @DisplayName("Should check if namespace is supported")
  void shouldCheckIfNamespaceIsSupported() {
    // Given
    String standardNs = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08";
    String alternativeNs = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10";
    String unsupportedNs = "urn:unknown:namespace";

    // Then
    assertThat(Iso20022NamespaceContext.isSupported(standardNs, Iso20022MessageType.PACS_008))
        .isTrue();
    assertThat(Iso20022NamespaceContext.isSupported(alternativeNs, Iso20022MessageType.PACS_008))
        .isTrue();
    assertThat(Iso20022NamespaceContext.isSupported(unsupportedNs, Iso20022MessageType.PACS_008))
        .isFalse();
  }

  @Test
  @DisplayName("Should get standard prefix for message type")
  void shouldGetStandardPrefixForMessageType() {
    // Then
    assertThat(Iso20022NamespaceContext.getStandardPrefix(Iso20022MessageType.PACS_008))
        .isEqualTo("pacs");
    assertThat(Iso20022NamespaceContext.getStandardPrefix(Iso20022MessageType.PACS_002))
        .isEqualTo("pacs");
    assertThat(Iso20022NamespaceContext.getStandardPrefix(Iso20022MessageType.CAMT_054))
        .isEqualTo("camt");
  }

  @Test
  @DisplayName("Should implement NamespaceContext interface")
  void shouldImplementNamespaceContextInterface() {
    // Given
    Iso20022NamespaceContext context = new Iso20022NamespaceContext();

    // When
    String pacsUri = context.getNamespaceURI("pacs");
    String camtUri = context.getNamespaceURI("camt");

    // Then
    assertThat(pacsUri).isNotNull();
    assertThat(camtUri).isNotNull();
  }

  @Test
  @DisplayName("Should handle XML standard namespaces")
  void shouldHandleXmlStandardNamespaces() {
    // Given
    Iso20022NamespaceContext context = new Iso20022NamespaceContext();

    // When/Then
    assertThat(context.getNamespaceURI("xml")).isEqualTo("http://www.w3.org/XML/1998/namespace");
    assertThat(context.getNamespaceURI("xmlns")).isEqualTo("http://www.w3.org/2000/xmlns/");
    assertThat(context.getPrefix("http://www.w3.org/XML/1998/namespace")).isEqualTo("xml");
    assertThat(context.getPrefix("http://www.w3.org/2000/xmlns/")).isEqualTo("xmlns");
  }

  @Test
  @DisplayName("Should get prefix from namespace URI")
  void shouldGetPrefixFromNamespaceUri() {
    // Given
    Iso20022NamespaceContext context = new Iso20022NamespaceContext();
    String pacs008Ns = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08";

    // When
    String prefix = context.getPrefix(pacs008Ns);

    // Then
    assertThat(prefix).isNotNull();
    assertThat(prefix).isIn("pacs", "iso"); // Could be either
  }

  @Test
  @DisplayName("Should throw exception for null prefix in getNamespaceURI")
  void shouldThrowExceptionForNullPrefixInGetNamespaceUri() {
    // Given
    Iso20022NamespaceContext context = new Iso20022NamespaceContext();

    // Then
    assertThatThrownBy(() -> context.getNamespaceURI(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Prefix cannot be null");
  }

  @Test
  @DisplayName("Should throw exception for null namespace URI in getPrefix")
  void shouldThrowExceptionForNullNamespaceUriInGetPrefix() {
    // Given
    Iso20022NamespaceContext context = new Iso20022NamespaceContext();

    // Then
    assertThatThrownBy(() -> context.getPrefix(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Namespace URI cannot be null");
  }

  @Test
  @DisplayName("Should get all registered prefixes")
  void shouldGetAllRegisteredPrefixes() {
    // When
    var prefixes = Iso20022NamespaceContext.getAllPrefixes();

    // Then
    assertThat(prefixes).isNotEmpty();
    assertThat(prefixes).contains("pacs", "camt");
  }

  @Test
  @DisplayName("Should get all registered namespace URIs")
  void shouldGetAllRegisteredNamespaceUris() {
    // When
    var namespaces = Iso20022NamespaceContext.getAllNamespaceUris();

    // Then
    assertThat(namespaces).isNotEmpty();
    assertThat(namespaces)
        .anyMatch(ns -> ns.contains("pacs.008"))
        .anyMatch(ns -> ns.contains("camt.054"));
  }
}
