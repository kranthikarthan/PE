package com.payments.batch.format;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for FileFormatDetector.
 *
 * @since PE-402
 */
@DisplayName("File Format Detector Tests")
class FileFormatDetectorTest {
  
  private FileFormatDetector detector;
  
  @TempDir
  Path tempDir;
  
  @BeforeEach
  void setUp() {
    detector = new FileFormatDetector();
  }
  
  @Test
  @DisplayName("Should detect CSV format by extension")
  void shouldDetectCsvFormatByExtension() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("payments.csv");
    Files.write(csvFile, "paymentId,amount,currency\nPAY001,100.00,ZAR".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(csvFile);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.CSV, format.get());
  }
  
  @Test
  @DisplayName("Should detect Excel format by extension")
  void shouldDetectExcelFormatByExtension() throws IOException {
    // Given
    Path excelFile = tempDir.resolve("payments.xlsx");
    Files.write(excelFile, "Excel content".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(excelFile);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.EXCEL, format.get());
  }
  
  @Test
  @DisplayName("Should detect XML format by extension")
  void shouldDetectXmlFormatByExtension() throws IOException {
    // Given
    Path xmlFile = tempDir.resolve("payments.xml");
    Files.write(xmlFile, "<?xml version=\"1.0\"?><Document></Document>".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(xmlFile);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.XML, format.get());
  }
  
  @Test
  @DisplayName("Should detect JSON format by extension")
  void shouldDetectJsonFormatByExtension() throws IOException {
    // Given
    Path jsonFile = tempDir.resolve("payments.json");
    Files.write(jsonFile, "{\"paymentId\": \"PAY001\"}".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(jsonFile);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.JSON, format.get());
  }
  
  @Test
  @DisplayName("Should detect CSV format by content analysis")
  void shouldDetectCsvFormatByContent() throws IOException {
    // Given
    Path csvFile = tempDir.resolve("payments.txt");
    Files.write(csvFile, "paymentId,amount,currency\nPAY001,100.00,ZAR".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(csvFile);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.CSV, format.get());
  }
  
  @Test
  @DisplayName("Should detect XML format by content analysis")
  void shouldDetectXmlFormatByContent() throws IOException {
    // Given
    Path xmlFile = tempDir.resolve("payments.txt");
    Files.write(xmlFile, "<?xml version=\"1.0\"?><Document></Document>".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(xmlFile);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.XML, format.get());
  }
  
  @Test
  @DisplayName("Should detect JSON format by content analysis")
  void shouldDetectJsonFormatByContent() throws IOException {
    // Given
    Path jsonFile = tempDir.resolve("payments.txt");
    Files.write(jsonFile, "{\"paymentId\": \"PAY001\"}".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(jsonFile);
    
    // Then
    assertTrue(format.isPresent());
    assertEquals(FileFormat.JSON, format.get());
  }
  
  @Test
  @DisplayName("Should return empty for unsupported format")
  void shouldReturnEmptyForUnsupportedFormat() throws IOException {
    // Given
    Path unknownFile = tempDir.resolve("payments.unknown");
    Files.write(unknownFile, "Some unknown content".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(unknownFile);
    
    // Then
    assertFalse(format.isPresent());
  }
  
  @Test
  @DisplayName("Should return empty for null file path")
  void shouldReturnEmptyForNullFilePath() throws IOException {
    // When
    Optional<FileFormat> format = detector.detectFormat(null);
    
    // Then
    assertFalse(format.isPresent());
  }
  
  @Test
  @DisplayName("Should return empty for non-existent file")
  void shouldReturnEmptyForNonExistentFile() throws IOException {
    // Given
    Path nonExistentFile = tempDir.resolve("non-existent.csv");
    
    // When
    Optional<FileFormat> format = detector.detectFormat(nonExistentFile);
    
    // Then
    assertFalse(format.isPresent());
  }
  
  @Test
  @DisplayName("Should handle empty file")
  void shouldHandleEmptyFile() throws IOException {
    // Given
    Path emptyFile = tempDir.resolve("empty.csv");
    Files.write(emptyFile, "".getBytes());
    
    // When
    Optional<FileFormat> format = detector.detectFormat(emptyFile);
    
    // Then
    // Empty files should not be detected as any specific format
    assertFalse(format.isPresent());
  }
}
