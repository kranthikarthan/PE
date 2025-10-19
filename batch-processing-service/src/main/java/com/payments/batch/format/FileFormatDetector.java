package com.payments.batch.format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Service for detecting file formats based on file extension and content analysis.
 *
 * <p>This service provides intelligent file format detection that goes beyond simple
 * extension matching to analyze file content and MIME types for accurate format
 * identification.
 *
 * <p><b>Detection Strategy:</b>
 * <ol>
 *   <li>Primary: File extension analysis
 *   <li>Secondary: MIME type detection
 *   <li>Tertiary: Content analysis (magic bytes, structure)
 * </ol>
 *
 * <p><b>Supported Formats:</b>
 * <ul>
 *   <li>CSV - Comma-separated values
 *   <li>EXCEL - Microsoft Excel (.xlsx, .xls)
 *   <li>XML - ISO 20022 XML messages
 *   <li>JSON - JSON payment format
 * </ul>
 *
 * @since PE-402
 */
@Slf4j
@Component
public class FileFormatDetector {
  
  private static final int CONTENT_ANALYSIS_BYTES = 1024;
  
  /**
   * Detects the file format of the given file.
   *
   * @param filePath the path to the file to analyze
   * @return the detected file format, or empty if detection fails
   * @throws IOException if the file cannot be read
   */
  public Optional<FileFormat> detectFormat(Path filePath) throws IOException {
    if (filePath == null || !Files.exists(filePath)) {
      log.warn("File path is null or does not exist: {}", filePath);
      return Optional.empty();
    }
    
    log.debug("Detecting format for file: {}", filePath);
    
    // Strategy 1: Extension-based detection
    Optional<FileFormat> formatByExtension = detectByExtension(filePath);
    if (formatByExtension.isPresent()) {
      log.debug("Format detected by extension: {}", formatByExtension.get());
      return formatByExtension;
    }
    
    // Strategy 2: MIME type detection
    Optional<FileFormat> formatByMimeType = detectByMimeType(filePath);
    if (formatByMimeType.isPresent()) {
      log.debug("Format detected by MIME type: {}", formatByMimeType.get());
      return formatByMimeType;
    }
    
    // Strategy 3: Content analysis
    Optional<FileFormat> formatByContent = detectByContent(filePath);
    if (formatByContent.isPresent()) {
      log.debug("Format detected by content analysis: {}", formatByContent.get());
      return formatByContent;
    }
    
    log.warn("Could not detect file format for: {}", filePath);
    return Optional.empty();
  }
  
  /**
   * Detects format based on file extension.
   *
   * @param filePath the file path
   * @return the detected format or empty
   */
  private Optional<FileFormat> detectByExtension(Path filePath) {
    String fileName = filePath.getFileName().toString().toLowerCase();
    
    // Check if file is empty first
    try {
      if (Files.size(filePath) == 0) {
        return Optional.empty();
      }
    } catch (IOException e) {
      log.warn("Could not check file size: {}", filePath, e);
    }
    
    for (FileFormat format : FileFormat.values()) {
      if (fileName.endsWith("." + format.getExtension())) {
        return Optional.of(format);
      }
    }
    
    // Special handling for Excel files
    if (fileName.endsWith(".xls")) {
      return Optional.of(FileFormat.EXCEL);
    }
    
    return Optional.empty();
  }
  
  /**
   * Detects format based on MIME type.
   *
   * @param filePath the file path
   * @return the detected format or empty
   */
  private Optional<FileFormat> detectByMimeType(Path filePath) {
    try {
      String mimeType = Files.probeContentType(filePath);
      if (mimeType == null) {
        return Optional.empty();
      }
      
      log.debug("Detected MIME type: {}", mimeType);
      
      for (FileFormat format : FileFormat.values()) {
        if (format.getMimeType().equals(mimeType)) {
          return Optional.of(format);
        }
      }
      
      // Additional MIME type mappings
      if (mimeType.equals("application/vnd.ms-excel")) {
        return Optional.of(FileFormat.EXCEL);
      }
      
    } catch (IOException e) {
      log.warn("Failed to detect MIME type for file: {}", filePath, e);
    }
    
    return Optional.empty();
  }
  
  /**
   * Detects format based on content analysis.
   *
   * @param filePath the file path
   * @return the detected format or empty
   */
  private Optional<FileFormat> detectByContent(Path filePath) {
    try {
      byte[] content = Files.readAllBytes(filePath);
      if (content.length == 0) {
        return Optional.empty();
      }
      
      // Analyze first few bytes for magic numbers
      return analyzeContent(content);
      
    } catch (IOException e) {
      log.warn("Failed to read file content for analysis: {}", filePath, e);
      return Optional.empty();
    }
  }
  
  /**
   * Analyzes file content to determine format.
   *
   * @param content the file content bytes
   * @return the detected format or empty
   */
  private Optional<FileFormat> analyzeContent(byte[] content) {
    // Excel file detection (ZIP-based format)
    if (isExcelFile(content)) {
      return Optional.of(FileFormat.EXCEL);
    }
    
    // XML detection
    if (isXmlFile(content)) {
      return Optional.of(FileFormat.XML);
    }
    
    // JSON detection
    if (isJsonFile(content)) {
      return Optional.of(FileFormat.JSON);
    }
    
    // CSV detection (fallback for text files)
    if (isCsvFile(content)) {
      return Optional.of(FileFormat.CSV);
    }
    
    return Optional.empty();
  }
  
  /**
   * Checks if the content represents an Excel file.
   *
   * @param content the file content
   * @return true if Excel file
   */
  private boolean isExcelFile(byte[] content) {
    // Excel files (.xlsx) are ZIP archives with specific structure
    if (content.length < 4) {
      return false;
    }
    
    // Check for ZIP signature (PK)
    if (content[0] == 0x50 && content[1] == 0x4B) {
      // Additional check for Excel-specific ZIP structure
      String contentStr = new String(content, 0, Math.min(content.length, CONTENT_ANALYSIS_BYTES));
      return contentStr.contains("xl/") || contentStr.contains("worksheets/");
    }
    
    // Check for legacy Excel format (.xls)
    if (content.length >= 8) {
      // OLE2 signature for .xls files
      return content[0] == (byte) 0xD0 && content[1] == (byte) 0xCF && 
             content[2] == 0x11 && content[3] == (byte) 0xE0;
    }
    
    return false;
  }
  
  /**
   * Checks if the content represents an XML file.
   *
   * @param content the file content
   * @return true if XML file
   */
  private boolean isXmlFile(byte[] content) {
    String contentStr = new String(content, 0, Math.min(content.length, CONTENT_ANALYSIS_BYTES));
    contentStr = contentStr.trim();
    
    return contentStr.startsWith("<?xml") || 
           contentStr.startsWith("<Document") ||
           contentStr.startsWith("<pacs.008") ||
           contentStr.startsWith("<pacs.002") ||
           contentStr.startsWith("<pacs.004");
  }
  
  /**
   * Checks if the content represents a JSON file.
   *
   * @param content the file content
   * @return true if JSON file
   */
  private boolean isJsonFile(byte[] content) {
    String contentStr = new String(content, 0, Math.min(content.length, CONTENT_ANALYSIS_BYTES));
    contentStr = contentStr.trim();
    
    return contentStr.startsWith("{") || contentStr.startsWith("[");
  }
  
  /**
   * Checks if the content represents a CSV file.
   *
   * @param content the file content
   * @return true if CSV file
   */
  private boolean isCsvFile(byte[] content) {
    String contentStr = new String(content, 0, Math.min(content.length, CONTENT_ANALYSIS_BYTES));
    
    // Look for CSV patterns: comma-separated values, quoted strings
    // Check for multiple commas and alphanumeric content
    long commaCount = contentStr.chars().filter(ch -> ch == ',').count();
    boolean hasAlphanumeric = contentStr.matches(".*[a-zA-Z0-9].*");
    
    return commaCount >= 2 && hasAlphanumeric;
  }
}
