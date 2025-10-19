package com.payments.iso20022.namespace;

import com.payments.iso20022.config.Iso20022MessageType;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * ISO 20022 Namespace Handler
 *
 * <p>Utility methods for handling ISO 20022 namespaces in XML messages. Supports:
 *
 * <ul>
 *   <li>Namespace detection from XML
 *   <li>Namespace stripping (for legacy systems)
 *   <li>Namespace normalization (convert between versions)
 *   <li>Namespace validation
 *   <li>Prefix handling
 * </ul>
 */
@Slf4j
public class Iso20022NamespaceHandler {

  // Regex patterns for namespace detection
  private static final Pattern NAMESPACE_PATTERN =
      Pattern.compile("xmlns(?::[a-zA-Z0-9]+)?=\"([^\"]+)\"");
  private static final Pattern MESSAGE_TYPE_PATTERN =
      Pattern.compile("(pacs|camt|pain|acmt)\\.(\\d{3})\\.(\\d{3})\\.(\\d{2})");
  private static final Pattern ROOT_ELEMENT_PATTERN = Pattern.compile("<([a-zA-Z0-9:]+:)?Document");

  /**
   * Detect ISO 20022 message type from XML content
   *
   * @param xml The XML content
   * @return Detected message type or null
   */
  public static Iso20022MessageType detectMessageType(String xml) {
    if (xml == null || xml.isEmpty()) {
      return null;
    }

    try {
      // Extract namespace from XML
      String namespace = extractNamespace(xml);
      if (namespace != null) {
        Iso20022MessageType messageType = Iso20022NamespaceContext.detectMessageType(namespace);
        if (messageType != null) {
          return messageType;
        }
      }

      // Fallback: Try to extract from namespace URI pattern
      Matcher matcher = MESSAGE_TYPE_PATTERN.matcher(xml);
      if (matcher.find()) {
        String messageId = matcher.group(1) + "." + matcher.group(2);
        for (Iso20022MessageType type : Iso20022MessageType.values()) {
          if (type.getMessageId().equals(messageId)) {
            return type;
          }
        }
      }

      return null;

    } catch (Exception e) {
      log.warn("Failed to detect message type from XML: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extract namespace URI from XML
   *
   * @param xml The XML content
   * @return Namespace URI or null
   */
  public static String extractNamespace(String xml) {
    if (xml == null || xml.isEmpty()) {
      return null;
    }

    // Find xmlns declaration in root element
    Matcher matcher = NAMESPACE_PATTERN.matcher(xml);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return null;
  }

  /**
   * Extract namespace prefix from XML (if any)
   *
   * @param xml The XML content
   * @return Namespace prefix or null if default namespace
   */
  public static String extractPrefix(String xml) {
    if (xml == null || xml.isEmpty()) {
      return null;
    }

    Matcher matcher = ROOT_ELEMENT_PATTERN.matcher(xml);
    if (matcher.find()) {
      String rootElement = matcher.group(1);
      if (rootElement != null) {
        // Remove trailing colon
        return rootElement.substring(0, rootElement.length() - 1);
      }
    }

    return null;
  }

  /**
   * Strip namespace from XML (for legacy systems that don't support namespaces)
   *
   * @param xml The XML with namespace
   * @return XML without namespace declarations
   */
  public static String stripNamespace(String xml) {
    if (xml == null || xml.isEmpty()) {
      return xml;
    }

    try {
      // Remove xmlns declarations
      String result = xml.replaceAll("\\s+xmlns(:[a-zA-Z0-9]+)?=\"[^\"]+\"", "");

      // Remove namespace prefixes from opening tags (keep element name only)
      result = result.replaceAll("<[a-zA-Z0-9]+:([a-zA-Z0-9]+)", "<$1");

      // Remove namespace prefixes from closing tags
      result = result.replaceAll("</[a-zA-Z0-9]+:([a-zA-Z0-9]+)", "</$1");

      log.debug("Stripped namespace from XML ({} → {} chars)", xml.length(), result.length());
      return result;

    } catch (Exception e) {
      log.error("Failed to strip namespace from XML: {}", e.getMessage(), e);
      return xml;
    }
  }

  /**
   * Add namespace to XML (for systems that require explicit namespaces)
   *
   * @param xml The XML without namespace
   * @param messageType The message type
   * @return XML with namespace
   */
  public static String addNamespace(String xml, Iso20022MessageType messageType) {
    if (xml == null || xml.isEmpty()) {
      return xml;
    }

    try {
      String namespace = Iso20022NamespaceContext.getStandardNamespace(messageType);

      // Check if namespace already exists
      if (xml.contains("xmlns=")) {
        return xml;
      }

      // Add namespace to root element
      String result = xml.replaceFirst("<Document", "<Document xmlns=\"" + namespace + "\"");

      log.debug("Added namespace to XML: {}", namespace);
      return result;

    } catch (Exception e) {
      log.error("Failed to add namespace to XML: {}", e.getMessage(), e);
      return xml;
    }
  }

  /**
   * Normalize namespace (convert to standard version)
   *
   * @param xml The XML with any namespace version
   * @param messageType The message type
   * @return XML with standard namespace
   */
  public static String normalizeNamespace(String xml, Iso20022MessageType messageType) {
    if (xml == null || xml.isEmpty()) {
      return xml;
    }

    try {
      String currentNamespace = extractNamespace(xml);
      String standardNamespace = Iso20022NamespaceContext.getStandardNamespace(messageType);

      // If already using standard namespace, return as is
      if (standardNamespace.equals(currentNamespace)) {
        return xml;
      }

      // Replace old namespace with standard one
      if (currentNamespace != null) {
        String result = xml.replace(currentNamespace, standardNamespace);
        log.debug("Normalized namespace: {} → {}", currentNamespace, standardNamespace);
        return result;
      }

      // If no namespace, add standard one
      return addNamespace(xml, messageType);

    } catch (Exception e) {
      log.error("Failed to normalize namespace: {}", e.getMessage(), e);
      return xml;
    }
  }

  /**
   * Validate namespace for message type
   *
   * @param xml The XML content
   * @param messageType The expected message type
   * @return true if namespace is valid
   */
  public static boolean validateNamespace(String xml, Iso20022MessageType messageType) {
    if (xml == null || xml.isEmpty()) {
      return false;
    }

    try {
      String namespace = extractNamespace(xml);
      if (namespace == null) {
        log.warn("No namespace found in XML for {}", messageType.getMessageId());
        return false;
      }

      boolean isSupported = Iso20022NamespaceContext.isSupported(namespace, messageType);

      if (!isSupported) {
        log.warn("Unsupported namespace {} for {}", namespace, messageType.getMessageId());
      }

      return isSupported;

    } catch (Exception e) {
      log.error("Failed to validate namespace: {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * Add namespace prefix to XML
   *
   * @param xml The XML without prefix
   * @param prefix The prefix to add (e.g., "pacs")
   * @param messageType The message type
   * @return XML with prefixed elements
   */
  public static String addPrefix(String xml, String prefix, Iso20022MessageType messageType) {
    if (xml == null || xml.isEmpty() || prefix == null) {
      return xml;
    }

    try {
      String namespace = Iso20022NamespaceContext.getStandardNamespace(messageType);

      // Parse XML
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xml)));

      // Add prefix to root element
      Element root = doc.getDocumentElement();
      doc.renameNode(root, namespace, prefix + ":" + root.getLocalName());
      root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespace);

      // Transform back to string
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));

      return writer.toString();

    } catch (Exception e) {
      log.error("Failed to add prefix to XML: {}", e.getMessage(), e);
      return xml;
    }
  }

  /**
   * Remove namespace prefix from XML
   *
   * @param xml The XML with prefix
   * @return XML without prefix
   */
  public static String removePrefix(String xml) {
    if (xml == null || xml.isEmpty()) {
      return xml;
    }

    try {
      // Remove prefix from elements (keep namespace)
      String result = xml.replaceAll("<([a-zA-Z0-9]+):", "<");
      result = result.replaceAll("</([a-zA-Z0-9]+):", "</");

      // Remove prefix declaration
      result = result.replaceAll("\\s+xmlns:[a-zA-Z0-9]+=\"[^\"]+\"", "");

      log.debug("Removed prefix from XML");
      return result;

    } catch (Exception e) {
      log.error("Failed to remove prefix from XML: {}", e.getMessage(), e);
      return xml;
    }
  }

  /**
   * Get namespace info for debugging
   *
   * @param xml The XML content
   * @return Namespace information string
   */
  public static String getNamespaceInfo(String xml) {
    StringBuilder info = new StringBuilder();
    info.append("Namespace Info:\n");
    info.append("  Namespace URI: ").append(extractNamespace(xml)).append("\n");
    info.append("  Prefix: ").append(extractPrefix(xml)).append("\n");

    Iso20022MessageType messageType = detectMessageType(xml);
    String messageTypeStr = messageType != null ? messageType.name() : "null";
    info.append("  Message Type: ").append(messageTypeStr).append("\n");

    return info.toString();
  }

  /**
   * Check if XML uses namespaces
   *
   * @param xml The XML content
   * @return true if namespaces are used
   */
  public static boolean hasNamespace(String xml) {
    return xml != null && xml.contains("xmlns");
  }

  /**
   * Check if XML uses namespace prefix
   *
   * @param xml The XML content
   * @return true if prefix is used
   */
  public static boolean hasPrefix(String xml) {
    return extractPrefix(xml) != null;
  }
}
