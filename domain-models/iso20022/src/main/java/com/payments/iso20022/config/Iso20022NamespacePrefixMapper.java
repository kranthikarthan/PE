package com.payments.iso20022.config;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

/**
 * ISO 20022 Namespace Prefix Mapper
 *
 * <p>Configures namespace prefixes for ISO 20022 XML messages to ensure correct namespace handling
 * as required by South African clearing systems.
 */
public class Iso20022NamespacePrefixMapper extends NamespacePrefixMapper {

  private final Iso20022MessageType messageType;

  public Iso20022NamespacePrefixMapper(Iso20022MessageType messageType) {
    this.messageType = messageType;
  }

  @Override
  public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
    // ISO 20022 standard: Use default namespace (no prefix) for main message namespace
    if (messageType.getNamespace().equals(namespaceUri)) {
      return "";
    }

    // XSI namespace for schema location
    if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
      return "xsi";
    }

    // Use suggestion for other namespaces
    return suggestion;
  }

  @Override
  public String[] getPreDeclaredNamespaceUris() {
    return new String[] {messageType.getNamespace(), "http://www.w3.org/2001/XMLSchema-instance"};
  }
}
