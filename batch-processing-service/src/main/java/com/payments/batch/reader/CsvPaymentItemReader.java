package com.payments.batch.reader;

import com.payments.batch.domain.PaymentRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Enhanced CSV payment item reader with configurable delimiters and date formats.
 *
 * <p>This reader extends the base functionality to support various CSV formats
 * commonly used in payment processing systems. It handles different delimiters,
 * date formats, and provides robust error handling for malformed data.
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>Configurable delimiter (comma, semicolon, tab, pipe)
 *   <li>Multiple date format support
 *   <li>Header row detection and skipping
 *   <li>Robust error handling and logging
 *   <li>Line number tracking for error reporting
 * </ul>
 *
 * <p><b>Expected CSV Format:</b>
 * <pre>
 * paymentId,debitAccount,creditAccount,amount,currency,valueDate,description
 * PAY001,123456789,987654321,1000.00,ZAR,2024-01-15,Payment description
 * </pre>
 *
 * @since PE-402
 */
@Slf4j
public class CsvPaymentItemReader implements PaymentItemReaderInterface {
  
  private final Resource resource;
  private final String delimiter;
  private final boolean skipHeader;
  private final List<DateTimeFormatter> dateFormatters;
  
  private BufferedReader reader;
  private int currentLineNumber;
  private boolean headerSkipped;
  private boolean finished;
  
  // Default date formats commonly used in payment files
  private static final String[] DEFAULT_DATE_FORMATS = {
      "yyyy-MM-dd",           // ISO format
      "dd/MM/yyyy",           // European format
      "MM/dd/yyyy",           // US format
      "yyyy-MM-dd HH:mm:ss",  // ISO with time
      "dd-MM-yyyy",           // European with dashes
      "yyyy/MM/dd"            // Alternative ISO
  };
  
  /**
   * Creates a CSV reader with default comma delimiter.
   *
   * @param resource the CSV file resource
   * @param skipHeader whether to skip the first row (header)
   */
  public CsvPaymentItemReader(Resource resource, boolean skipHeader) {
    this(resource, ",", skipHeader);
  }
  
  /**
   * Creates a CSV reader with specified delimiter.
   *
   * @param resource the CSV file resource
   * @param delimiter the field delimiter (comma, semicolon, tab, pipe)
   * @param skipHeader whether to skip the first row (header)
   */
  public CsvPaymentItemReader(Resource resource, String delimiter, boolean skipHeader) {
    this.resource = resource;
    this.delimiter = delimiter != null ? delimiter : ",";
    this.skipHeader = skipHeader;
    this.dateFormatters = initializeDateFormatters();
    this.currentLineNumber = 0;
    this.headerSkipped = false;
    this.finished = false;
  }
  
  /**
   * Initializes the date formatters for parsing various date formats.
   *
   * @return list of configured date formatters
   */
  private List<DateTimeFormatter> initializeDateFormatters() {
    List<DateTimeFormatter> formatters = new ArrayList<>();
    
    for (String pattern : DEFAULT_DATE_FORMATS) {
      try {
        formatters.add(DateTimeFormatter.ofPattern(pattern));
      } catch (Exception e) {
        log.warn("Invalid date format pattern: {}", pattern, e);
      }
    }
    
    return formatters;
  }
  
  @Override
  public void open() throws Exception {
    log.info("Opening CSV file: {} with delimiter: '{}'", resource.getFilename(), delimiter);
    
    this.reader = new BufferedReader(
        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
    this.currentLineNumber = 0;
    this.headerSkipped = false;
    this.finished = false;
    
    // Skip header if requested
    if (skipHeader) {
      String headerLine = reader.readLine();
      if (headerLine != null) {
        currentLineNumber++;
        headerSkipped = true;
        log.debug("Skipped header line: {}", headerLine);
      }
    }
  }
  
  @Override
  public PaymentRecord read() throws Exception {
    if (finished) {
      return null;
    }
    
    String line = reader.readLine();
    if (line == null) {
      finished = true;
      return null;
    }
    
    currentLineNumber++;
    
    // Skip empty lines
    if (!StringUtils.hasText(line.trim())) {
      log.debug("Skipping empty line {}", currentLineNumber);
      return read(); // Recursive call to read next line
    }
    
    try {
      return parsePaymentRecord(line);
    } catch (Exception e) {
      log.error("Error parsing line {}: {}", currentLineNumber, line, e);
      throw new Exception("Failed to parse CSV line " + currentLineNumber + ": " + e.getMessage(), e);
    }
  }
  
  @Override
  public void close() throws Exception {
    if (reader != null) {
      reader.close();
      log.debug("Closed CSV file: {}", resource.getFilename());
    }
  }
  
  /**
   * Parses a CSV line into a PaymentRecord.
   *
   * @param line the CSV line to parse
   * @return the parsed payment record
   * @throws Exception if parsing fails
   */
  private PaymentRecord parsePaymentRecord(String line) throws Exception {
    String[] fields = parseCsvLine(line);
    
    if (fields.length < 7) {
      throw new IllegalArgumentException(
          "CSV line must have at least 7 fields, found: " + fields.length);
    }
    
    return PaymentRecord.builder()
        .paymentId(parseString(fields[0]))
        .debtorAccount(parseString(fields[1]))
        .creditorAccount(parseString(fields[2]))
        .amount(parseBigDecimal(fields[3]))
        .currency(parseString(fields[4]))
        .valueDate(parseDate(fields[5]))
        .paymentReference(parseString(fields[6]))
        .debtorName(parseString(fields.length > 7 ? fields[7] : null))
        .creditorName(parseString(fields.length > 8 ? fields[8] : null))
        .debtorBankCode(parseString(fields.length > 9 ? fields[9] : null))
        .creditorBankCode(parseString(fields.length > 10 ? fields[10] : null))
        .paymentType(parseString(fields.length > 11 ? fields[11] : null))
        .lineNumber(currentLineNumber)
        .build();
  }
  
  /**
   * Parses a CSV line handling quoted fields and escaped characters.
   *
   * @param line the CSV line
   * @return array of field values
   */
  private String[] parseCsvLine(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder currentField = new StringBuilder();
    boolean inQuotes = false;
    boolean escapeNext = false;
    
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      
      if (escapeNext) {
        currentField.append(c);
        escapeNext = false;
      } else if (c == '\\') {
        escapeNext = true;
      } else if (c == '"') {
        inQuotes = !inQuotes;
      } else if (c == delimiter.charAt(0) && !inQuotes) {
        fields.add(currentField.toString().trim());
        currentField = new StringBuilder();
      } else {
        currentField.append(c);
      }
    }
    
    // Add the last field
    fields.add(currentField.toString().trim());
    
    return fields.toArray(new String[0]);
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
   * @throws NumberFormatException if parsing fails
   */
  private java.math.BigDecimal parseBigDecimal(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    
    // Remove currency symbols and whitespace
    String cleanValue = value.replaceAll("[^\\d.,-]", "").trim();
    
    if (cleanValue.isEmpty()) {
      return null;
    }
    
    // Handle different decimal separators
    if (cleanValue.contains(",") && !cleanValue.contains(".")) {
      // European format: 1.234,56
      cleanValue = cleanValue.replace(".", "").replace(",", ".");
    }
    
    return new java.math.BigDecimal(cleanValue);
  }
  
  /**
   * Parses a date field using multiple format attempts.
   *
   * @param value the date string
   * @return parsed date or null if empty
   * @throws DateTimeParseException if no format matches
   */
  private LocalDate parseDate(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    
    String cleanValue = value.trim();
    
    // Try each date formatter
    for (DateTimeFormatter formatter : dateFormatters) {
      try {
        return LocalDate.parse(cleanValue, formatter);
      } catch (DateTimeParseException e) {
        // Continue to next formatter
      }
    }
    
    // If all formatters fail, try parsing as ISO date
    try {
      return LocalDate.parse(cleanValue);
    } catch (DateTimeParseException e) {
      log.warn("Could not parse date '{}' with any known format", cleanValue);
      throw new DateTimeParseException(
          "Unparseable date: " + cleanValue, cleanValue, 0, e);
    }
  }
  
  @Override
  public int getCurrentLineNumber() {
    return currentLineNumber;
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
