package com.payments.batch.format;

import com.payments.batch.reader.CsvPaymentItemReader;
import com.payments.batch.reader.ExcelPaymentItemReader;
import com.payments.batch.reader.JsonPaymentItemReader;
import com.payments.batch.reader.PaymentItemReaderInterface;
import com.payments.batch.reader.XmlPaymentItemReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Factory for creating appropriate payment item readers based on file format.
 *
 * <p>This factory uses the {@link FileFormatDetector} to determine the file format
 * and returns the appropriate reader implementation for processing payment files.
 *
 * <p><b>Supported Readers:</b>
 * <ul>
 *   <li>{@link CsvPaymentItemReader} - For CSV files
 *   <li>{@link ExcelPaymentItemReader} - For Excel files (.xlsx, .xls)
 *   <li>{@link XmlPaymentItemReader} - For XML files (ISO 20022)
 *   <li>{@link JsonPaymentItemReader} - For JSON files
 * </ul>
 *
 * @since PE-402
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileFormatFactory {
  
  private final FileFormatDetector formatDetector;
  
  /**
   * Creates a payment item reader for the specified file.
   *
   * @param resource the file resource to process
   * @param delimiter the delimiter for CSV files (ignored for other formats)
   * @param skipHeader whether to skip the header row (for tabular formats)
   * @return the appropriate reader for the file format
   * @throws IOException if the file cannot be read or format cannot be detected
   * @throws UnsupportedOperationException if the file format is not supported
   */
  public PaymentItemReaderInterface createReader(Resource resource, String delimiter, boolean skipHeader) 
      throws IOException {
    
    if (resource == null || !resource.exists()) {
      throw new IllegalArgumentException("Resource is null or does not exist");
    }
    
    Path filePath = resource.getFile().toPath();
    Optional<FileFormat> format = formatDetector.detectFormat(filePath);
    
    if (format.isEmpty()) {
      throw new UnsupportedOperationException(
          "Unsupported file format for: " + filePath.getFileName());
    }
    
    FileFormat detectedFormat = format.get();
    log.info("Creating {} reader for file: {}", detectedFormat, filePath.getFileName());
    
    return createReaderForFormat(detectedFormat, resource, delimiter, skipHeader);
  }
  
  /**
   * Creates a reader for a specific file format.
   *
   * @param format the detected file format
   * @param resource the file resource
   * @param delimiter the delimiter (for CSV files)
   * @param skipHeader whether to skip header
   * @return the appropriate reader
   */
  private PaymentItemReaderInterface createReaderForFormat(
      FileFormat format, 
      Resource resource, 
      String delimiter, 
      boolean skipHeader) {
    
    switch (format) {
      case CSV:
        return new CsvPaymentItemReader(resource, delimiter, skipHeader);
        
      case EXCEL:
        return new ExcelPaymentItemReader(resource, skipHeader);
        
      case XML:
        return new XmlPaymentItemReader(resource);
        
      case JSON:
        return new JsonPaymentItemReader(resource);
        
      default:
        throw new UnsupportedOperationException(
            "No reader implementation for format: " + format);
    }
  }
  
  /**
   * Gets the supported file formats.
   *
   * @return array of supported formats
   */
  public FileFormat[] getSupportedFormats() {
    return FileFormat.values();
  }
  
  /**
   * Checks if a file format is supported.
   *
   * @param format the format to check
   * @return true if supported, false otherwise
   */
  public boolean isFormatSupported(FileFormat format) {
    return format != null;
  }
  
  /**
   * Gets the file format for a given file without creating a reader.
   *
   * @param resource the file resource
   * @return the detected format or empty if not supported
   * @throws IOException if the file cannot be read
   */
  public Optional<FileFormat> detectFormat(Resource resource) throws IOException {
    if (resource == null || !resource.exists()) {
      return Optional.empty();
    }
    
    Path filePath = resource.getFile().toPath();
    return formatDetector.detectFormat(filePath);
  }
}
