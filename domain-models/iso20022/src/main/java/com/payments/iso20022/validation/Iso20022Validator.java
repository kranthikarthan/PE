package com.payments.iso20022.validation;

import com.payments.iso20022.config.Iso20022MessageType;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ISO 20022 XSD Validator
 *
 * <p>Validates ISO 20022 messages against their XSD schemas to ensure compliance with South African
 * clearing system requirements.
 */
@Slf4j
public class Iso20022Validator {

  private static final Map<Iso20022MessageType, Schema> SCHEMA_CACHE = new HashMap<>();
  private static final SchemaFactory SCHEMA_FACTORY =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

  /**
   * Validate ISO 20022 XML message against XSD schema
   *
   * @param xml The XML message to validate
   * @param messageType The ISO 20022 message type
   * @return Validation result with errors if any
   */
  public ValidationResult validate(String xml, Iso20022MessageType messageType) {
    log.debug("Validating {} message ({} chars)", messageType.getMessageId(), xml.length());

    try {
      // Get cached schema
      Schema schema = getSchema(messageType);

      // Create validator
      Validator validator = schema.newValidator();

      // Collect validation errors
      ValidationErrorHandler errorHandler = new ValidationErrorHandler();
      validator.setErrorHandler(errorHandler);

      // Validate
      validator.validate(new StreamSource(new StringReader(xml)));

      // Check for errors
      if (errorHandler.hasErrors()) {
        log.warn(
            "{} validation failed with {} errors",
            messageType.getMessageId(),
            errorHandler.getErrors().size());

        return ValidationResult.invalid(
            messageType,
            "XSD validation failed: " + errorHandler.getFirstError(),
            errorHandler.getErrors());
      }

      log.debug("{} validation successful", messageType.getMessageId());
      return ValidationResult.valid(messageType);

    } catch (Exception e) {
      log.error("Failed to validate {} message: {}", messageType.getMessageId(), e.getMessage(), e);
      return ValidationResult.error(
          messageType, "Validation error: " + e.getMessage(), List.of(e.getMessage()));
    }
  }

  /**
   * Validate with strict mode (warnings treated as errors)
   *
   * @param xml The XML message to validate
   * @param messageType The ISO 20022 message type
   * @return Validation result
   */
  public ValidationResult validateStrict(String xml, Iso20022MessageType messageType) {
    ValidationResult result = validate(xml, messageType);

    // In strict mode, any warning or error fails validation
    if (result.hasWarnings()) {
      return ValidationResult.invalid(
          messageType, "Strict validation failed with warnings", result.getValidationErrors());
    }

    return result;
  }

  /**
   * Get or load schema from cache
   *
   * @param messageType The message type
   * @return Cached or loaded schema
   */
  private synchronized Schema getSchema(Iso20022MessageType messageType) {
    return SCHEMA_CACHE.computeIfAbsent(messageType, this::loadSchema);
  }

  /**
   * Load XSD schema from classpath
   *
   * @param messageType The message type
   * @return Loaded schema
   */
  private Schema loadSchema(Iso20022MessageType messageType) {
    try {
      log.info("Loading XSD schema for: {}", messageType.getMessageId());

      String schemaPath = "/xsd/" + messageType.getSchemaFile();
      InputStream schemaStream = getClass().getResourceAsStream(schemaPath);

      if (schemaStream == null) {
        throw new SchemaNotFoundException(
            "Schema not found: " + schemaPath + " for " + messageType.getMessageId());
      }

      Schema schema = SCHEMA_FACTORY.newSchema(new StreamSource(schemaStream));

      log.info("Loaded XSD schema for: {}", messageType.getMessageId());
      return schema;

    } catch (SAXException e) {
      log.error("Failed to load schema for {}: {}", messageType.getMessageId(), e.getMessage(), e);
      throw new SchemaLoadException("Failed to load schema: " + messageType.getMessageId(), e);
    }
  }

  /** Clear schema cache (for testing/reloading) */
  public static synchronized void clearCache() {
    SCHEMA_CACHE.clear();
    log.info("Cleared XSD schema cache");
  }

  /**
   * Check if schema is cached
   *
   * @param messageType The message type
   * @return true if schema is cached
   */
  public boolean isSchemaCached(Iso20022MessageType messageType) {
    return SCHEMA_CACHE.containsKey(messageType);
  }

  /** Preload all schemas (for startup optimization) */
  public void preloadSchemas() {
    log.info("Preloading all ISO 20022 XSD schemas");

    for (Iso20022MessageType messageType : Iso20022MessageType.values()) {
      try {
        getSchema(messageType);
      } catch (Exception e) {
        log.warn("Failed to preload schema for {}: {}", messageType.getMessageId(), e.getMessage());
      }
    }

    log.info("Preloaded {} schemas", SCHEMA_CACHE.size());
  }

  /** SAX Error Handler to collect validation errors */
  private static class ValidationErrorHandler implements ErrorHandler {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    @Override
    public void warning(SAXParseException exception) {
      String message =
          String.format(
              "WARNING at line %d, column %d: %s",
              exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      warnings.add(message);
      log.warn("XSD validation warning: {}", message);
    }

    @Override
    public void error(SAXParseException exception) {
      String message =
          String.format(
              "ERROR at line %d, column %d: %s",
              exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      errors.add(message);
      log.error("XSD validation error: {}", message);
    }

    @Override
    public void fatalError(SAXParseException exception) {
      String message =
          String.format(
              "FATAL ERROR at line %d, column %d: %s",
              exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      errors.add(message);
      log.error("XSD validation fatal error: {}", message);
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }

    public boolean hasWarnings() {
      return !warnings.isEmpty();
    }

    public List<String> getErrors() {
      List<String> allErrors = new ArrayList<>(errors);
      allErrors.addAll(warnings);
      return allErrors;
    }

    public String getFirstError() {
      return errors.isEmpty() ? warnings.get(0) : errors.get(0);
    }
  }

  /** Schema Not Found Exception */
  public static class SchemaNotFoundException extends RuntimeException {
    public SchemaNotFoundException(String message) {
      super(message);
    }
  }

  /** Schema Load Exception */
  public static class SchemaLoadException extends RuntimeException {
    public SchemaLoadException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
