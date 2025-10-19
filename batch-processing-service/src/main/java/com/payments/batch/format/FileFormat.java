package com.payments.batch.format;

/**
 * Enumeration of supported file formats for batch payment processing.
 *
 * <p>Each format has specific characteristics and requires different parsing strategies:
 *
 * <ul>
 *   <li><b>CSV</b>: Comma-separated values, most common format for bulk payments
 *   <li><b>EXCEL</b>: Microsoft Excel files (.xlsx, .xls), often used by business users
 *   <li><b>XML</b>: ISO 20022 XML messages, standard for international payments
 *   <li><b>JSON</b>: Modern JSON format, used by API-based payment systems
 * </ul>
 *
 * @since PE-402
 */
public enum FileFormat {
  
  /**
   * Comma-Separated Values format.
   * 
   * <p>Most common format for bulk payment files. Supports configurable delimiters
   * and header rows. Used by most banking systems for batch processing.
   */
  CSV("csv", "text/csv", "Comma-Separated Values"),
  
  /**
   * Microsoft Excel format.
   * 
   * <p>Supports both .xlsx (Excel 2007+) and .xls (Excel 97-2003) formats.
   * Often used by business users who create payment files in Excel.
   */
  EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Microsoft Excel"),
  
  /**
   * XML format for ISO 20022 messages.
   * 
   * <p>Standard format for international payments and financial messaging.
   * Supports complex payment structures and regulatory compliance.
   */
  XML("xml", "application/xml", "ISO 20022 XML"),
  
  /**
   * JSON format for modern payment systems.
   * 
   * <p>Used by API-based payment systems and modern fintech applications.
   * Supports nested payment structures and metadata.
   */
  JSON("json", "application/json", "JSON Payment Format");
  
  private final String extension;
  private final String mimeType;
  private final String description;
  
  FileFormat(String extension, String mimeType, String description) {
    this.extension = extension;
    this.mimeType = mimeType;
    this.description = description;
  }
  
  /**
   * Gets the file extension for this format.
   *
   * @return the file extension (e.g., "csv", "xlsx")
   */
  public String getExtension() {
    return extension;
  }
  
  /**
   * Gets the MIME type for this format.
   *
   * @return the MIME type (e.g., "text/csv", "application/json")
   */
  public String getMimeType() {
    return mimeType;
  }
  
  /**
   * Gets the human-readable description of this format.
   *
   * @return the format description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Determines if this format supports header rows.
   *
   * @return true if headers are supported, false otherwise
   */
  public boolean supportsHeaders() {
    return this == CSV || this == EXCEL;
  }
  
  /**
   * Determines if this format supports multiple sheets/worksheets.
   *
   * @return true if multiple sheets are supported, false otherwise
   */
  public boolean supportsMultipleSheets() {
    return this == EXCEL;
  }
  
  /**
   * Determines if this format is structured (XML/JSON) vs tabular (CSV/Excel).
   *
   * @return true if structured format, false if tabular
   */
  public boolean isStructured() {
    return this == XML || this == JSON;
  }
  
  /**
   * Gets the default delimiter for tabular formats.
   *
   * @return the default delimiter, or null for non-tabular formats
   */
  public String getDefaultDelimiter() {
    return this == CSV ? "," : null;
  }
}
