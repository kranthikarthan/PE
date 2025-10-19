package com.payments.batch.reader;

import com.payments.batch.domain.PaymentRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter that wraps PaymentItemReaderInterface implementations to work with Spring Batch.
 *
 * <p>This adapter provides a bridge between the new file format readers and Spring Batch's
 * ItemReader interface, allowing seamless integration with the batch processing framework.
 *
 * @since PE-402
 */
@Slf4j
public class PaymentItemReaderAdapter extends PaymentItemReader {
  
  private final PaymentItemReaderInterface delegate;
  private boolean opened = false;
  
  /**
   * Creates an adapter for the given payment item reader.
   *
   * @param delegate the payment item reader to wrap
   */
  public PaymentItemReaderAdapter(PaymentItemReaderInterface delegate) {
    // Call parent constructor with dummy values since we'll override the behavior
    super(null, ",", false);
    this.delegate = delegate;
  }
  
  @Override
  public PaymentRecord read() throws Exception {
    if (!opened) {
      delegate.open();
      opened = true;
      log.debug("Opened payment item reader: {}", delegate.getResourceName());
    }
    
    if (delegate.isFinished()) {
      return null;
    }
    
    PaymentRecord record = delegate.read();
    if (record != null) {
      log.debug("Read payment record: {} from line {}", 
          record.getPaymentId(), delegate.getCurrentLineNumber());
    }
    
    return record;
  }
  
  @Override
  public void open() throws Exception {
    if (!opened) {
      delegate.open();
      opened = true;
      log.debug("Opened payment item reader: {}", delegate.getResourceName());
    }
  }
  
  @Override
  public void close() {
    if (opened) {
      try {
        delegate.close();
        opened = false;
        log.debug("Closed payment item reader: {}", delegate.getResourceName());
      } catch (Exception e) {
        log.error("Error closing payment item reader: {}", delegate.getResourceName(), e);
      }
    }
  }
  
  /**
   * Gets the current line number from the underlying reader.
   *
   * @return the current line number
   */
  public int getCurrentLineNumber() {
    return delegate.getCurrentLineNumber();
  }
  
  /**
   * Checks if the underlying reader is finished.
   *
   * @return true if finished, false otherwise
   */
  public boolean isFinished() {
    return delegate.isFinished();
  }
  
  /**
   * Gets the resource name from the underlying reader.
   *
   * @return the resource name
   */
  public String getResourceName() {
    return delegate.getResourceName();
  }
}
