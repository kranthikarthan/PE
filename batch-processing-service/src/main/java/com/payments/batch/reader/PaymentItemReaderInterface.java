package com.payments.batch.reader;

import com.payments.batch.domain.PaymentRecord;

/**
 * Common interface for all payment item readers.
 *
 * <p>This interface provides a consistent contract for reading payment records
 * from different file formats (CSV, Excel, XML, JSON).
 *
 * @since PE-402
 */
public interface PaymentItemReaderInterface {
  
  /**
   * Opens the reader and prepares it for reading.
   *
   * @throws Exception if the reader cannot be opened
   */
  void open() throws Exception;
  
  /**
   * Reads the next payment record.
   *
   * @return the next payment record, or null if no more records
   * @throws Exception if reading fails
   */
  PaymentRecord read() throws Exception;
  
  /**
   * Closes the reader and releases resources.
   *
   * @throws Exception if closing fails
   */
  void close() throws Exception;
  
  /**
   * Gets the current line/row number being processed.
   *
   * @return the current line number
   */
  int getCurrentLineNumber();
  
  /**
   * Checks if the reader has finished reading all records.
   *
   * @return true if finished, false otherwise
   */
  boolean isFinished();
  
  /**
   * Gets the name of the resource being read.
   *
   * @return the resource name
   */
  String getResourceName();
}
