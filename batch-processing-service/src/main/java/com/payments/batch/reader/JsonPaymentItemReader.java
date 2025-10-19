package com.payments.batch.reader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.batch.domain.PaymentRecord;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * JSON payment item reader for modern payment formats.
 *
 * <p>This reader processes JSON files containing payment data in various formats,
 * supporting both single payment objects and arrays of payments. It handles
 * nested JSON structures and provides flexible field mapping.
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>Single payment object support
 *   <li>Array of payments support
 *   <li>Nested JSON structure handling
 *   <li>Flexible field mapping
 *   <li>Robust error handling and logging
 * </ul>
 *
 * <p><b>Supported JSON Formats:</b>
 * <ul>
 *   <li>Single payment object: <code>{"paymentId": "PAY001", ...}</code>
 *   <li>Array of payments: <code>[{"paymentId": "PAY001", ...}, ...]</code>
 *   <li>Nested structure: <code>{"payments": [{"paymentId": "PAY001", ...}]}</code>
 * </ul>
 *
 * @since PE-402
 */
@Slf4j
public class JsonPaymentItemReader implements PaymentItemReaderInterface {
  
  private final Resource resource;
  private final ObjectMapper objectMapper;
  private final DateTimeFormatter dateFormatter;
  
  private List<PaymentRecord> paymentRecords;
  private int currentIndex;
  private boolean finished;
  
  /**
   * Creates a JSON reader for payment data.
   *
   * @param resource the JSON file resource
   */
  public JsonPaymentItemReader(Resource resource) {
    this.resource = resource;
    this.objectMapper = new ObjectMapper();
    this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    this.currentIndex = 0;
    this.finished = false;
  }
  
  @Override
  public void open() throws Exception {
    log.info("Opening JSON file: {}", resource.getFilename());
    
    try (InputStream inputStream = resource.getInputStream()) {
      this.paymentRecords = extractPaymentRecords(inputStream);
      this.currentIndex = 0;
      this.finished = false;
      
      log.info("JSON file opened successfully. Found {} payment records", paymentRecords.size());
      
    } catch (Exception e) {
      log.error("Failed to open JSON file: {}", resource.getFilename(), e);
      throw new Exception("Failed to open JSON file: " + e.getMessage(), e);
    }
  }
  
  @Override
  public PaymentRecord read() throws Exception {
    if (finished || paymentRecords.isEmpty()) {
      return null;
    }
    
    if (currentIndex >= paymentRecords.size()) {
      finished = true;
      return null;
    }
    
    PaymentRecord record = paymentRecords.get(currentIndex);
    currentIndex++;
    
    log.debug("Read payment record {} of {}: {}", 
        currentIndex, paymentRecords.size(), record.getPaymentId());
    
    return record;
  }
  
  @Override
  public void close() throws Exception {
    log.debug("Closed JSON file: {}", resource.getFilename());
  }
  
  /**
   * Extracts all payment records from the JSON document.
   *
   * @param inputStream the JSON input stream
   * @return list of payment records
   * @throws Exception if extraction fails
   */
  private List<PaymentRecord> extractPaymentRecords(InputStream inputStream) throws Exception {
    List<PaymentRecord> records = new ArrayList<>();
    
    try {
      JsonNode rootNode = objectMapper.readTree(inputStream);
      
      if (rootNode.isArray()) {
        // Handle array of payments
        records.addAll(parsePaymentArray(rootNode));
      } else if (rootNode.isObject()) {
        // Handle single payment or nested structure
        if (rootNode.has("payments") && rootNode.get("payments").isArray()) {
          // Handle nested structure: {"payments": [...]}
          records.addAll(parsePaymentArray(rootNode.get("payments")));
        } else {
          // Handle single payment object
          PaymentRecord record = parsePaymentObject(rootNode);
          if (record != null) {
            records.add(record);
          }
        }
      }
      
      log.debug("Extracted {} payment records from JSON", records.size());
      
    } catch (Exception e) {
      log.error("Failed to parse JSON file: {}", e.getMessage(), e);
      throw new Exception("Failed to parse JSON file: " + e.getMessage(), e);
    }
    
    return records;
  }
  
  /**
   * Parses an array of payment objects.
   *
   * @param arrayNode the JSON array node
   * @return list of payment records
   */
  private List<PaymentRecord> parsePaymentArray(JsonNode arrayNode) {
    List<PaymentRecord> records = new ArrayList<>();
    
    if (arrayNode.isArray()) {
      for (JsonNode paymentNode : arrayNode) {
        PaymentRecord record = parsePaymentObject(paymentNode);
        if (record != null) {
          records.add(record);
        }
      }
    }
    
    return records;
  }
  
  /**
   * Parses a single payment object from JSON node.
   *
   * @param paymentNode the JSON payment node
   * @return payment record or null if parsing fails
   */
  private PaymentRecord parsePaymentObject(JsonNode paymentNode) {
    try {
      if (!paymentNode.isObject()) {
        log.warn("Expected payment object, got: {}", paymentNode.getNodeType());
        return null;
      }
      
      String paymentId = getStringValue(paymentNode, "paymentId", "id", "payment_id");
      String debtorAccount = getStringValue(paymentNode, "debtorAccount", "debtor_account", "fromAccount", "from_account");
      String creditorAccount = getStringValue(paymentNode, "creditorAccount", "creditor_account", "toAccount", "to_account");
      String amount = getStringValue(paymentNode, "amount", "value", "sum");
      String currency = getStringValue(paymentNode, "currency", "ccy", "currencyCode");
      String valueDate = getStringValue(paymentNode, "valueDate", "value_date", "date", "executionDate");
      String paymentReference = getStringValue(paymentNode, "paymentReference", "reference", "ref", "description", "desc");
      String debtorName = getStringValue(paymentNode, "debtorName", "debtor_name", "fromName", "from_name");
      String creditorName = getStringValue(paymentNode, "creditorName", "creditor_name", "toName", "to_name");
      String debtorBankCode = getStringValue(paymentNode, "debtorBankCode", "debtor_bank_code", "fromBankCode");
      String creditorBankCode = getStringValue(paymentNode, "creditorBankCode", "creditor_bank_code", "toBankCode");
      String paymentType = getStringValue(paymentNode, "paymentType", "payment_type", "type");
      
      return PaymentRecord.builder()
          .paymentId(paymentId)
          .debtorAccount(debtorAccount)
          .creditorAccount(creditorAccount)
          .amount(parseBigDecimal(amount))
          .currency(currency)
          .valueDate(parseDate(valueDate))
          .paymentReference(paymentReference)
          .debtorName(debtorName)
          .creditorName(creditorName)
          .debtorBankCode(debtorBankCode)
          .creditorBankCode(creditorBankCode)
          .paymentType(paymentType)
          .lineNumber(currentIndex)
          .build();
          
    } catch (Exception e) {
      log.warn("Failed to parse JSON payment record: {}", e.getMessage());
      return null;
    }
  }
  
  /**
   * Gets a string value from JSON node, trying multiple field names.
   *
   * @param node the JSON node
   * @param fieldNames the possible field names to try
   * @return the field value or null if not found
   */
  private String getStringValue(JsonNode node, String... fieldNames) {
    for (String fieldName : fieldNames) {
      if (node.has(fieldName)) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode.isTextual()) {
          return fieldNode.asText().trim();
        } else if (fieldNode.isNumber()) {
          return fieldNode.asText();
        } else if (fieldNode.isNull()) {
          return null;
        }
      }
    }
    return null;
  }
  
  /**
   * Parses a string field, handling null and empty values.
   *
   * @param value the string value
   * @return parsed string or null if empty
   */
  private String parseString(String value) {
    if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value)) {
      return null;
    }
    return value.trim();
  }
  
  /**
   * Parses a decimal amount field.
   *
   * @param value the amount string
   * @return parsed amount
   */
  private BigDecimal parseBigDecimal(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    
    String cleanValue = value.trim();
    if (cleanValue.isEmpty()) {
      return null;
    }
    
    try {
      return new BigDecimal(cleanValue);
    } catch (NumberFormatException e) {
      log.warn("Could not parse amount '{}': {}", cleanValue, e.getMessage());
      return null;
    }
  }
  
  /**
   * Parses a date field.
   *
   * @param value the date string
   * @return parsed date or null if empty
   */
  private LocalDate parseDate(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    
    String cleanValue = value.trim();
    
    try {
      return LocalDate.parse(cleanValue, dateFormatter);
    } catch (Exception e) {
      try {
        return LocalDate.parse(cleanValue);
      } catch (Exception e2) {
        log.warn("Could not parse date '{}': {}", cleanValue, e2.getMessage());
        return null;
      }
    }
  }
  
  @Override
  public int getCurrentLineNumber() {
    return currentIndex;
  }
  
  @Override
  public boolean isFinished() {
    return finished;
  }
  
  @Override
  public String getResourceName() {
    return resource.getFilename();
  }
}
