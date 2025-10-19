package com.payments.iso20022.service;

import com.payments.iso20022.config.Iso20022JaxbConfig;
import com.payments.iso20022.config.Iso20022MessageType;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * ISO 20022 Marshaller Service
 *
 * <p>Provides XML marshalling and unmarshalling for ISO 20022 messages using JAXB.
 */
@Slf4j
public class Iso20022MarshallerService {

  /**
   * Marshal ISO 20022 object to XML string
   *
   * @param document The ISO 20022 document object
   * @param messageType The message type
   * @return XML string
   * @throws Iso20022MarshallingException if marshalling fails
   */
  public String marshal(Object document, Iso20022MessageType messageType) {
    try {
      log.debug("Marshalling {} message", messageType.getMessageId());

      Marshaller marshaller = Iso20022JaxbConfig.createMarshaller(messageType);
      StringWriter writer = new StringWriter();

      marshaller.marshal(document, writer);

      String xml = writer.toString();

      log.debug(
          "Successfully marshalled {} message ({} chars)",
          messageType.getMessageId(),
          xml.length());
      return xml;

    } catch (JAXBException e) {
      log.error("Failed to marshal {} message: {}", messageType.getMessageId(), e.getMessage(), e);
      throw new Iso20022MarshallingException(
          "Failed to marshal " + messageType.getMessageId() + " message", e);
    }
  }

  /**
   * Unmarshal XML string to ISO 20022 object
   *
   * @param xml The XML string
   * @param messageType The message type
   * @return ISO 20022 document object
   * @throws Iso20022MarshallingException if unmarshalling fails
   */
  @SuppressWarnings("unchecked")
  public <T> T unmarshal(String xml, Iso20022MessageType messageType) {
    try {
      log.debug("Unmarshalling {} message", messageType.getMessageId());

      Unmarshaller unmarshaller = Iso20022JaxbConfig.createUnmarshaller(messageType);
      StringReader reader = new StringReader(xml);

      Object result = unmarshaller.unmarshal(reader);

      // Handle JAXBElement wrapper
      if (result instanceof JAXBElement) {
        result = ((JAXBElement<?>) result).getValue();
      }

      log.debug("Successfully unmarshalled {} message", messageType.getMessageId());
      return (T) result;

    } catch (JAXBException e) {
      log.error(
          "Failed to unmarshal {} message: {}", messageType.getMessageId(), e.getMessage(), e);
      throw new Iso20022MarshallingException(
          "Failed to unmarshal " + messageType.getMessageId() + " message", e);
    }
  }

  /**
   * Marshal with custom formatting
   *
   * @param document The ISO 20022 document object
   * @param messageType The message type
   * @param formatted Whether to format the output XML
   * @return XML string
   * @throws Iso20022MarshallingException if marshalling fails
   */
  public String marshal(Object document, Iso20022MessageType messageType, boolean formatted) {
    try {
      Marshaller marshaller = Iso20022JaxbConfig.createMarshaller(messageType);
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);

      StringWriter writer = new StringWriter();
      marshaller.marshal(document, writer);

      return writer.toString();

    } catch (JAXBException e) {
      throw new Iso20022MarshallingException(
          "Failed to marshal " + messageType.getMessageId() + " message", e);
    }
  }

  /**
   * Marshal to compact XML (no formatting)
   *
   * @param document The ISO 20022 document object
   * @param messageType The message type
   * @return Compact XML string (no whitespace)
   */
  public String marshalCompact(Object document, Iso20022MessageType messageType) {
    return marshal(document, messageType, false);
  }

  /**
   * Marshal to formatted XML (pretty-print)
   *
   * @param document The ISO 20022 document object
   * @param messageType The message type
   * @return Formatted XML string
   */
  public String marshalFormatted(Object document, Iso20022MessageType messageType) {
    return marshal(document, messageType, true);
  }

  /** ISO 20022 Marshalling Exception */
  public static class Iso20022MarshallingException extends RuntimeException {
    public Iso20022MarshallingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
