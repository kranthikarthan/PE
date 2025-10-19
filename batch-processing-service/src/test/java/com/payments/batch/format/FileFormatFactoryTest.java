package com.payments.batch.format;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.payments.batch.reader.PaymentItemReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Unit tests for FileFormatFactory.
 *
 * @since PE-402
 */
@DisplayName("File Format Factory Tests")
class FileFormatFactoryTest {
  
  private FileFormatFactory factory;
  private FileFormatDetector mockDetector;
  
  @TempDir
  Path tempDir;
  
  @BeforeEach
  void setUp() {
    mockDetector = mock(FileFormatDetector.class);
    factory = new FileFormatFactory(mockDetector);
  }
  
  @Test
  @DisplayName("Should create CSV reader for CSV format")
  void shouldCreateCsvReaderForCsvFormat() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("payments.csv");
    Files.write(csvFile, "paymentId,amount,currency\nPAY001,100.00,ZAR".getBytes());
    Resource resource = new ClassPathResource(csvFile.toString());
    
    when(mockDetector.detectFormat(any(Path.class)))
        .thenReturn(Optional.of(FileFormat.CSV));
    
    // When
    PaymentItemReaderInterface reader = factory.createReader(resource, ",", true);
    
    // Then
    assertNotNull(reader);
    assertTrue(reader instanceof com.payments.batch.reader.CsvPaymentItemReader);
  }
  
  @Test
  @DisplayName("Should create Excel reader for Excel format")
  void shouldCreateExcelReaderForExcelFormat() throws IOException {
    // Given
    Path excelFile = tempDir.resolve("payments.xlsx");
    Files.write(excelFile, "Excel content".getBytes());
    Resource resource = new ClassPathResource(excelFile.toString());
    
    when(mockDetector.detectFormat(any(Path.class)))
        .thenReturn(Optional.of(FileFormat.EXCEL));
    
    // When
    PaymentItemReaderInterface reader = factory.createReader(resource, ",", true);
    
    // Then
    assertNotNull(reader);
    assertTrue(reader instanceof com.payments.batch.reader.ExcelPaymentItemReader);
  }
  
  @Test
  @DisplayName("Should create XML reader for XML format")
  void shouldCreateXmlReaderForXmlFormat() throws IOException {
    // Given
    Path xmlFile = tempDir.resolve("payments.xml");
    Files.write(xmlFile, "<?xml version=\"1.0\"?><Document></Document>".getBytes());
    Resource resource = new ClassPathResource(xmlFile.toString());
    
    when(mockDetector.detectFormat(any(Path.class)))
        .thenReturn(Optional.of(FileFormat.XML));
    
    // When
    PaymentItemReaderInterface reader = factory.createReader(resource, ",", true);
    
    // Then
    assertNotNull(reader);
    assertTrue(reader instanceof com.payments.batch.reader.XmlPaymentItemReader);
  }
  
  @Test
  @DisplayName("Should create JSON reader for JSON format")
  void shouldCreateJsonReaderForJsonFormat() throws IOException {
    // Given
    Path jsonFile = tempDir.resolve("payments.json");
    Files.write(jsonFile, "{\"paymentId\": \"PAY001\"}".getBytes());
    Resource resource = new ClassPathResource(jsonFile.toString());
    
    when(mockDetector.detectFormat(any(Path.class)))
        .thenReturn(Optional.of(FileFormat.JSON));
    
    // When
    PaymentItemReaderInterface reader = factory.createReader(resource, ",", true);
    
    // Then
    assertNotNull(reader);
    assertTrue(reader instanceof com.payments.batch.reader.JsonPaymentItemReader);
  }
  
  @Test
  @DisplayName("Should throw exception for unsupported format")
  void shouldThrowExceptionForUnsupportedFormat() throws IOException {
    // Given
    Path unknownFile = tempDir.resolve("payments.unknown");
    Files.write(unknownFile, "Unknown content".getBytes());
    Resource resource = new ClassPathResource(unknownFile.toString());
    
    when(mockDetector.detectFormat(any(Path.class)))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(UnsupportedOperationException.class, () -> {
      factory.createReader(resource, ",", true);
    });
  }
  
  @Test
  @DisplayName("Should throw exception for null resource")
  void shouldThrowExceptionForNullResource() {
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      factory.createReader(null, ",", true);
    });
  }
  
  @Test
  @DisplayName("Should throw exception for non-existent resource")
  void shouldThrowExceptionForNonExistentResource() {
    // Given
    Resource resource = new ClassPathResource("non-existent.csv");
    
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      factory.createReader(resource, ",", true);
    });
  }
  
  @Test
  @DisplayName("Should get supported formats")
  void shouldGetSupportedFormats() {
    // When
    FileFormat[] formats = factory.getSupportedFormats();
    
    // Then
    assertNotNull(formats);
    assertEquals(4, formats.length);
    assertTrue(java.util.Arrays.asList(formats).contains(FileFormat.CSV));
    assertTrue(java.util.Arrays.asList(formats).contains(FileFormat.EXCEL));
    assertTrue(java.util.Arrays.asList(formats).contains(FileFormat.XML));
    assertTrue(java.util.Arrays.asList(formats).contains(FileFormat.JSON));
  }
  
  @Test
  @DisplayName("Should check if format is supported")
  void shouldCheckIfFormatIsSupported() {
    // When & Then
    assertTrue(factory.isFormatSupported(FileFormat.CSV));
    assertTrue(factory.isFormatSupported(FileFormat.EXCEL));
    assertTrue(factory.isFormatSupported(FileFormat.XML));
    assertTrue(factory.isFormatSupported(FileFormat.JSON));
    assertFalse(factory.isFormatSupported(null));
  }
  
  @Test
  @DisplayName("Should detect format without creating reader")
  void shouldDetectFormatWithoutCreatingReader() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("payments.csv");
    Files.write(csvFile, "paymentId,amount,currency\nPAY001,100.00,ZAR".getBytes());
    Resource resource = new ClassPathResource(csvFile.toString());
    
    when(mockDetector.detectFormat(any(Path.class)))
        .thenReturn(Optional.of(FileFormat.CSV));
    
    // When
    Optional<FileFormat> format = factory.detectFormat(resource);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.CSV, format.get());
  }
}
