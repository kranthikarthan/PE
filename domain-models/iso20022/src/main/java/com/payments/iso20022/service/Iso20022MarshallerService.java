package com.payments.iso20022.service;

import com.payments.iso20022.config.Iso20022JaxbConfig;
import com.payments.iso20022.config.Iso20022MessageType;
import com.payments.iso20022.validation.Iso20022Validator;
import com.payments.iso20022.validation.ValidationResult;
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
 * <p>Provides XML marshalling and unmarshalling for ISO 20022 messages using JAXB with optional XSD
 * validation.
 */
@Slf4j
public class Iso20022MarshallerService {

  private final Iso20022Validator validator;
  private final boolean validateByDefault;

  /** Constructor with default validator */
  public Iso20022MarshallerService() {
    this(new Iso20022Validator(), false);
  }

  /**
   * Constructor with custom validator
   *
   * @param validator The validator to use
   * @param validateByDefault Whether to validate by default
   */
  public Iso20022MarshallerService(Iso20022Validator validator, boolean validateByDefault) {
    this.validator = validator;
    this.validateByDefault = validateByDefault;
  }

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

  /**
   * Marshal with XSD validation
   *
   * @param document The ISO 20022 document object
   * @param messageType The message type
   * @return XML string
   * @throws Iso20022ValidationException if validation fails
   */
  public String marshalWithValidation(Object document, Iso20022MessageType messageType) {
    String xml = marshal(document, messageType);

    // Validate
    ValidationResult result = validator.validate(xml, messageType);

    if (!result.isValid()) {
      log.error("Marshalled XML failed validation: {}", result.getErrorSummary());
      throw new Iso20022ValidationException(
          "XSD validation failed for " + messageType.getMessageId(), result);
    }

    log.debug("Marshalled and validated {} message successfully", messageType.getMessageId());
    return xml;
  }

  /**
   * Unmarshal with XSD validation
   *
   * @param xml The XML string
   * @param messageType The message type
   * @return ISO 20022 document object
   * @throws Iso20022ValidationException if validation fails
   */
  public <T> T unmarshalWithValidation(String xml, Iso20022MessageType messageType) {
    // Validate first
    ValidationResult result = validator.validate(xml, messageType);

    if (!result.isValid()) {
      log.error("XML failed validation before unmarshalling: {}", result.getErrorSummary());
      throw new Iso20022ValidationException(
          "XSD validation failed for " + messageType.getMessageId(), result);
    }

    // Unmarshal
    T document = unmarshal(xml, messageType);

    log.debug("Validated and unmarshalled {} message successfully", messageType.getMessageId());
    return document;
  }

  /**
   * Validate XML without marshalling/unmarshalling
   *
   * @param xml The XML string to validate
   * @param messageType The message type
   * @return Validation result
   */
  public ValidationResult validate(String xml, Iso20022MessageType messageType) {
    return validator.validate(xml, messageType);
  }

  /**
   * Validate with strict mode (warnings as errors)
   *
   * @param xml The XML string to validate
   * @param messageType The message type
   * @return Validation result
   */
  public ValidationResult validateStrict(String xml, Iso20022MessageType messageType) {
    return validator.validateStrict(xml, messageType);
  }

  /**
   * Get the validator instance
   *
   * @return The validator
   */
  public Iso20022Validator getValidator() {
    return validator;
  }

  /** ISO 20022 Marshalling Exception */
  public static class Iso20022MarshallingException extends RuntimeException {
    public Iso20022MarshallingException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** ISO 20022 Validation Exception */
  public static class Iso20022ValidationException extends RuntimeException {
    private final ValidationResult validationResult;

    public Iso20022ValidationException(String message, ValidationResult validationResult) {
      super(message + ": " + validationResult.getMessage());
      this.validationResult = validationResult;
    }

    public ValidationResult getValidationResult() {
      return validationResult;
    }
  }
}
