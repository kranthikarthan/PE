package com.payments.iso20022.namespace;

import com.payments.iso20022.config.Iso20022MessageType;
import java.util.*;
import javax.xml.namespace.NamespaceContext;

/**
 * ISO 20022 Namespace Context
 *
 * <p>Provides namespace prefix mappings for ISO 20022 messages. Handles multiple versions and
 * clearing system-specific prefixes.
 *
 * <p>Standard ISO 20022 namespaces:
 *
 * <ul>
 *   <li>pacs.008: urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08 (and .10)
 *   <li>pacs.002: urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10 (and .12)
 *   <li>pacs.004: urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09 (and .11)
 *   <li>camt.054: urn:iso:std:iso:20022:tech:xsd:camt.054.001.08 (and .09)
 * </ul>
 */
public class Iso20022NamespaceContext implements NamespaceContext {

  // Standard ISO 20022 namespace URIs
  private static final Map<Iso20022MessageType, String> STANDARD_NAMESPACES = new HashMap<>();

  // Alternative namespace URIs (older versions)
  private static final Map<Iso20022MessageType, List<String>> ALTERNATIVE_NAMESPACES =
      new HashMap<>();

  // Common prefix mappings
  private static final Map<String, String> PREFIX_TO_URI = new HashMap<>();
  private static final Map<String, String> URI_TO_PREFIX = new HashMap<>();

  static {
    // Standard namespaces (latest versions)
    STANDARD_NAMESPACES.put(
        Iso20022MessageType.PACS_008, "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08");
    STANDARD_NAMESPACES.put(
        Iso20022MessageType.PACS_002, "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10");
    STANDARD_NAMESPACES.put(
        Iso20022MessageType.PACS_004, "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09");
    STANDARD_NAMESPACES.put(
        Iso20022MessageType.CAMT_054, "urn:iso:std:iso:20022:tech:xsd:camt.054.001.08");

    // Alternative namespaces (backward compatibility)
    ALTERNATIVE_NAMESPACES.put(
        Iso20022MessageType.PACS_008,
        List.of(
            "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10", // Newer version
            "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.07", // Older version
            "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.06"));

    ALTERNATIVE_NAMESPACES.put(
        Iso20022MessageType.PACS_002,
        List.of(
            "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12", // Newer version
            "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.09", // Older version
            "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.08"));

    ALTERNATIVE_NAMESPACES.put(
        Iso20022MessageType.PACS_004,
        List.of(
            "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.11", // Newer version
            "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.08", // Older version
            "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.07"));

    ALTERNATIVE_NAMESPACES.put(
        Iso20022MessageType.CAMT_054,
        List.of(
            "urn:iso:std:iso:20022:tech:xsd:camt.054.001.09", // Newer version
            "urn:iso:std:iso:20022:tech:xsd:camt.054.001.07", // Older version
            "urn:iso:std:iso:20022:tech:xsd:camt.054.001.06"));

    // Standard prefix mappings
    PREFIX_TO_URI.put("pacs", "urn:iso:std:iso:20022:tech:xsd");
    PREFIX_TO_URI.put("camt", "urn:iso:std:iso:20022:tech:xsd");
    PREFIX_TO_URI.put("pain", "urn:iso:std:iso:20022:tech:xsd");
    PREFIX_TO_URI.put("iso", "urn:iso:std:iso:20022:tech:xsd");

    // Reverse mapping
    URI_TO_PREFIX.put("urn:iso:std:iso:20022:tech:xsd", "iso");

    // Add all standard namespaces to prefix mapping
    for (Map.Entry<Iso20022MessageType, String> entry : STANDARD_NAMESPACES.entrySet()) {
      String namespace = entry.getValue();
      String prefix = extractPrefix(entry.getKey().getMessageId());
      PREFIX_TO_URI.put(prefix, namespace);
      URI_TO_PREFIX.put(namespace, prefix);
    }

    // Add all alternative namespaces to prefix mapping
    for (Map.Entry<Iso20022MessageType, List<String>> entry : ALTERNATIVE_NAMESPACES.entrySet()) {
      String prefix = extractPrefix(entry.getKey().getMessageId());
      for (String namespace : entry.getValue()) {
        URI_TO_PREFIX.put(namespace, prefix);
      }
    }
  }

  /**
   * Get standard namespace URI for message type
   *
   * @param messageType The message type
   * @return Standard namespace URI
   */
  public static String getStandardNamespace(Iso20022MessageType messageType) {
    return STANDARD_NAMESPACES.get(messageType);
  }

  /**
   * Get all supported namespaces for message type (standard + alternatives)
   *
   * @param messageType The message type
   * @return List of supported namespace URIs
   */
  public static List<String> getSupportedNamespaces(Iso20022MessageType messageType) {
    List<String> namespaces = new ArrayList<>();
    namespaces.add(STANDARD_NAMESPACES.get(messageType));
    namespaces.addAll(ALTERNATIVE_NAMESPACES.getOrDefault(messageType, Collections.emptyList()));
    return namespaces;
  }

  /**
   * Detect message type from namespace URI
   *
   * @param namespaceUri The namespace URI
   * @return Detected message type or null if not recognized
   */
  public static Iso20022MessageType detectMessageType(String namespaceUri) {
    if (namespaceUri == null || namespaceUri.isEmpty()) {
      return null;
    }

    // Check standard namespaces first
    for (Map.Entry<Iso20022MessageType, String> entry : STANDARD_NAMESPACES.entrySet()) {
      if (namespaceUri.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    // Check alternative namespaces
    for (Map.Entry<Iso20022MessageType, List<String>> entry : ALTERNATIVE_NAMESPACES.entrySet()) {
      if (entry.getValue().contains(namespaceUri)) {
        return entry.getKey();
      }
    }

    // Try to extract from URI pattern (e.g., "pacs.008.001.08" from URI)
    for (Iso20022MessageType messageType : Iso20022MessageType.values()) {
      if (namespaceUri.contains(messageType.getMessageId())) {
        return messageType;
      }
    }

    return null;
  }

  /**
   * Check if namespace is supported for message type
   *
   * @param namespaceUri The namespace URI
   * @param messageType The message type
   * @return true if namespace is supported
   */
  public static boolean isSupported(String namespaceUri, Iso20022MessageType messageType) {
    return getSupportedNamespaces(messageType).contains(namespaceUri);
  }

  /**
   * Get standard prefix for message type
   *
   * @param messageType The message type
   * @return Standard prefix (e.g., "pacs", "camt")
   */
  public static String getStandardPrefix(Iso20022MessageType messageType) {
    return extractPrefix(messageType.getMessageId());
  }

  /** Extract prefix from message ID (e.g., "pacs" from "pacs.008") */
  private static String extractPrefix(String messageId) {
    int dotIndex = messageId.indexOf('.');
    return dotIndex > 0 ? messageId.substring(0, dotIndex) : messageId;
  }

  // NamespaceContext implementation

  @Override
  public String getNamespaceURI(String prefix) {
    if (prefix == null) {
      throw new IllegalArgumentException("Prefix cannot be null");
    }

    // Handle XML standard prefixes
    if (prefix.equals("xml")) {
      return "http://www.w3.org/XML/1998/namespace";
    } else if (prefix.equals("xmlns")) {
      return "http://www.w3.org/2000/xmlns/";
    }

    // Handle ISO 20022 prefixes
    String uri = PREFIX_TO_URI.get(prefix);
    return uri != null ? uri : javax.xml.XMLConstants.NULL_NS_URI;
  }

  @Override
  public String getPrefix(String namespaceURI) {
    if (namespaceURI == null) {
      throw new IllegalArgumentException("Namespace URI cannot be null");
    }

    // Handle XML standard namespaces
    if (namespaceURI.equals("http://www.w3.org/XML/1998/namespace")) {
      return "xml";
    } else if (namespaceURI.equals("http://www.w3.org/2000/xmlns/")) {
      return "xmlns";
    }

    return URI_TO_PREFIX.get(namespaceURI);
  }

  @Override
  public Iterator<String> getPrefixes(String namespaceURI) {
    String prefix = getPrefix(namespaceURI);
    return prefix != null
        ? Collections.singletonList(prefix).iterator()
        : Collections.emptyIterator();
  }

  /**
   * Get all registered prefixes
   *
   * @return Set of all registered prefixes
   */
  public static Set<String> getAllPrefixes() {
    return new HashSet<>(PREFIX_TO_URI.keySet());
  }

  /**
   * Get all registered namespace URIs
   *
   * @return Set of all registered namespace URIs
   */
  public static Set<String> getAllNamespaceUris() {
    return new HashSet<>(URI_TO_PREFIX.keySet());
  }
}
