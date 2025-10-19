package com.payments.iso20022.config;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * JAXB Configuration for ISO 20022 Messages
 *
 * <p>Provides configured JAXB marshallers and unmarshallers for ISO 20022 message types.
 */
@Slf4j
public class Iso20022JaxbConfig {

  private static final Map<Iso20022MessageType, JAXBContext> CONTEXT_CACHE = new HashMap<>();

  /**
   * Get JAXB context for message type
   *
   * @param messageType The ISO 20022 message type
   * @return JAXB context
   * @throws JAXBException if context creation fails
   */
  public static synchronized JAXBContext getJaxbContext(Iso20022MessageType messageType)
      throws JAXBException {
    return CONTEXT_CACHE.computeIfAbsent(messageType, Iso20022JaxbConfig::createJaxbContext);
  }

  /**
   * Create JAXB context for message type
   *
   * @param messageType The ISO 20022 message type
   * @return JAXB context
   */
  private static JAXBContext createJaxbContext(Iso20022MessageType messageType) {
    try {
      log.info("Creating JAXB context for: {}", messageType);
      String contextPath = messageType.getPackageName();
      return JAXBContext.newInstance(contextPath);
    } catch (JAXBException e) {
      log.error("Failed to create JAXB context for: {}", messageType, e);
      throw new RuntimeException("Failed to create JAXB context for: " + messageType, e);
    }
  }

  /**
   * Create configured marshaller
   *
   * @param messageType The ISO 20022 message type
   * @return Configured marshaller
   * @throws JAXBException if marshaller creation fails
   */
  public static Marshaller createMarshaller(Iso20022MessageType messageType) throws JAXBException {
    JAXBContext context = getJaxbContext(messageType);
    Marshaller marshaller = context.createMarshaller();

    // Configure marshaller
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    marshaller.setProperty(
        Marshaller.JAXB_SCHEMA_LOCATION,
        messageType.getNamespace() + " " + messageType.getSchemaFile());

    // Set namespace prefix mapper
    marshaller.setProperty(
        "org.glassfish.jaxb.namespacePrefixMapper", new Iso20022NamespacePrefixMapper(messageType));

    log.debug("Created marshaller for: {}", messageType);
    return marshaller;
  }

  /**
   * Create configured unmarshaller
   *
   * @param messageType The ISO 20022 message type
   * @return Configured unmarshaller
   * @throws JAXBException if unmarshaller creation fails
   */
  public static Unmarshaller createUnmarshaller(Iso20022MessageType messageType)
      throws JAXBException {
    JAXBContext context = getJaxbContext(messageType);
    Unmarshaller unmarshaller = context.createUnmarshaller();

    log.debug("Created unmarshaller for: {}", messageType);
    return unmarshaller;
  }

  /** Clear JAXB context cache (for testing/reloading) */
  public static void clearCache() {
    CONTEXT_CACHE.clear();
    log.info("Cleared JAXB context cache");
  }
}
