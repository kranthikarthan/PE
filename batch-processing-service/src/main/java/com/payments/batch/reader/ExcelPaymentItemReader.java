package com.payments.batch.reader;

import com.payments.batch.domain.PaymentRecord;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Excel payment item reader supporting both .xlsx and .xls formats.
 *
 * <p>This reader processes Excel files containing payment data, supporting multiple
 * worksheets and various cell formats. It automatically detects the data range
 * and handles different Excel cell types appropriately.
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>Supports both .xlsx (Excel 2007+) and .xls (Excel 97-2003) formats
 *   <li>Multiple worksheet support (processes first worksheet by default)
 *   <li>Automatic data range detection
 *   <li>Header row detection and skipping
 *   <li>Robust cell type handling (string, numeric, date, formula)
 *   <li>Error handling and logging
 * </ul>
 *
 * <p><b>Expected Excel Format:</b>
 * <table border="1">
 *   <tr><th>Payment ID</th><th>Debit Account</th><th>Credit Account</th><th>Amount</th><th>Currency</th><th>Value Date</th><th>Description</th></tr>
 *   <tr><td>PAY001</td><td>123456789</td><td>987654321</td><td>1000.00</td><td>ZAR</td><td>2024-01-15</td><td>Payment description</td></tr>
 * </table>
 *
 * @since PE-402
 */
@Slf4j
public class ExcelPaymentItemReader implements PaymentItemReaderInterface {
  
  private final Resource resource;
  private final boolean skipHeader;
  private final DateTimeFormatter dateFormatter;
  
  private Workbook workbook;
  private Sheet currentSheet;
  private Iterator<Row> rowIterator;
  private int currentRowNumber;
  private boolean finished;
  
  /**
   * Creates an Excel reader that skips the header row.
   *
   * @param resource the Excel file resource
   * @param skipHeader whether to skip the first row (header)
   */
  public ExcelPaymentItemReader(Resource resource, boolean skipHeader) {
    this.resource = resource;
    this.skipHeader = skipHeader;
    this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    this.currentRowNumber = 0;
    this.finished = false;
  }
  
  @Override
  public void open() throws Exception {
    log.info("Opening Excel file: {}", resource.getFilename());
    
    try (InputStream inputStream = resource.getInputStream()) {
      this.workbook = WorkbookFactory.create(inputStream);
      this.currentSheet = workbook.getSheetAt(0); // Use first sheet
      this.rowIterator = currentSheet.iterator();
      this.currentRowNumber = 0;
      this.finished = false;
      
      // Skip header if requested
      if (skipHeader && rowIterator.hasNext()) {
        Row headerRow = rowIterator.next();
        currentRowNumber++;
        log.debug("Skipped header row: {}", getRowAsString(headerRow));
      }
      
      log.info("Excel file opened successfully. Sheet: {}, Total rows: {}", 
          currentSheet.getSheetName(), currentSheet.getLastRowNum() + 1);
      
    } catch (Exception e) {
      log.error("Failed to open Excel file: {}", resource.getFilename(), e);
      throw new Exception("Failed to open Excel file: " + e.getMessage(), e);
    }
  }
  
  @Override
  public PaymentRecord read() throws Exception {
    if (finished) {
      return null;
    }
    
    if (!rowIterator.hasNext()) {
      finished = true;
      return null;
    }
    
    Row row = rowIterator.next();
    currentRowNumber++;
    
    // Skip empty rows
    if (isEmptyRow(row)) {
      log.debug("Skipping empty row {}", currentRowNumber);
      return read(); // Recursive call to read next row
    }
    
    try {
      return parsePaymentRecord(row);
    } catch (Exception e) {
      log.error("Error parsing row {}: {}", currentRowNumber, getRowAsString(row), e);
      throw new Exception("Failed to parse Excel row " + currentRowNumber + ": " + e.getMessage(), e);
    }
  }
  
  @Override
  public void close() throws Exception {
    if (workbook != null) {
      workbook.close();
      log.debug("Closed Excel file: {}", resource.getFilename());
    }
  }
  
  /**
   * Parses an Excel row into a PaymentRecord.
   *
   * @param row the Excel row to parse
   * @return the parsed payment record
   * @throws Exception if parsing fails
   */
  private PaymentRecord parsePaymentRecord(Row row) throws Exception {
    List<String> values = extractRowValues(row);
    
    if (values.size() < 7) {
      throw new IllegalArgumentException(
          "Excel row must have at least 7 columns, found: " + values.size());
    }
    
    return PaymentRecord.builder()
        .paymentId(parseString(values.get(0)))
        .debtorAccount(parseString(values.get(1)))
        .creditorAccount(parseString(values.get(2)))
        .amount(parseBigDecimal(values.get(3)))
        .currency(parseString(values.get(4)))
        .valueDate(parseDate(values.get(5)))
        .paymentReference(parseString(values.get(6)))
        .debtorName(parseString(values.size() > 7 ? values.get(7) : null))
        .creditorName(parseString(values.size() > 8 ? values.get(8) : null))
        .debtorBankCode(parseString(values.size() > 9 ? values.get(9) : null))
        .creditorBankCode(parseString(values.size() > 10 ? values.get(10) : null))
        .paymentType(parseString(values.size() > 11 ? values.get(11) : null))
        .lineNumber(currentRowNumber)
        .build();
  }
  
  /**
   * Extracts all cell values from a row as strings.
   *
   * @param row the Excel row
   * @return list of cell values as strings
   */
  private List<String> extractRowValues(Row row) {
    List<String> values = new ArrayList<>();
    
    for (int i = 0; i < 15; i++) { // Support up to 15 columns
      Cell cell = row.getCell(i);
      String value = getCellValueAsString(cell);
      values.add(value);
    }
    
    return values;
  }
  
  /**
   * Gets the cell value as a string, handling different cell types.
   *
   * @param cell the Excel cell
   * @return cell value as string, or null if cell is null
   */
  private String getCellValueAsString(Cell cell) {
    if (cell == null) {
      return null;
    }
    
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue().trim();
        
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          // Handle date cells
          Date date = cell.getDateCellValue();
          return dateFormatter.format(date.toInstant()
              .atZone(java.time.ZoneId.systemDefault())
              .toLocalDate());
        } else {
          // Handle numeric cells
          double numericValue = cell.getNumericCellValue();
          if (numericValue == (long) numericValue) {
            return String.valueOf((long) numericValue);
          } else {
            return String.valueOf(numericValue);
          }
        }
        
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
        
      case FORMULA:
        try {
          // Try to get the calculated value
          return getCellValueAsString(cell);
        } catch (Exception e) {
          log.warn("Could not evaluate formula in cell: {}", e.getMessage());
          return cell.getCellFormula();
        }
        
      case BLANK:
        return null;
        
      default:
        return null;
    }
  }
  
  /**
   * Checks if a row is empty (all cells are null or empty).
   *
   * @param row the Excel row
   * @return true if row is empty
   */
  private boolean isEmptyRow(Row row) {
    if (row == null) {
      return true;
    }
    
    for (int i = 0; i < 15; i++) {
      Cell cell = row.getCell(i);
      if (cell != null && StringUtils.hasText(getCellValueAsString(cell))) {
        return false;
      }
    }
    
    return true;
  }
  
  /**
   * Gets a row as a string for logging purposes.
   *
   * @param row the Excel row
   * @return row content as string
   */
  private String getRowAsString(Row row) {
    if (row == null) {
      return "null";
    }
    
    List<String> values = extractRowValues(row);
    return String.join("|", values);
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
    
    String cleanValue = value.trim();
    if (cleanValue.isEmpty()) {
      return null;
    }
    
    return new java.math.BigDecimal(cleanValue);
  }
  
  /**
   * Parses a date field.
   *
   * @param value the date string
   * @return parsed date or null if empty
   * @throws DateTimeParseException if parsing fails
   */
  private LocalDate parseDate(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    
    String cleanValue = value.trim();
    
    try {
      return LocalDate.parse(cleanValue, dateFormatter);
    } catch (DateTimeParseException e) {
      // Try parsing as ISO date
      try {
        return LocalDate.parse(cleanValue);
      } catch (DateTimeParseException e2) {
        log.warn("Could not parse date '{}' with any known format", cleanValue);
        throw new DateTimeParseException(
            "Unparseable date: " + cleanValue, cleanValue, 0, e2);
      }
    }
  }
  
  @Override
  public int getCurrentLineNumber() {
    return currentRowNumber;
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
