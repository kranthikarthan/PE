package com.payments.batch.reader;

import com.payments.batch.domain.PaymentRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.Resource;

/**
 * ItemReader for reading payment records from CSV files.
 *
 * <p>This reader supports configurable delimiters, handles large files efficiently using streaming,
 * and provides line-number tracking for error reporting.
 *
 * <p><b>Features:</b>
 *
 * <ul>
 *   <li>Streaming read for large files (memory efficient)
 *   <li>Configurable CSV delimiter (comma, semicolon, pipe, tab)
 *   <li>Header row detection and skipping
 *   <li>Line number tracking for error reporting
 *   <li>Date parsing with multiple format support
 * </ul>
 *
 * @since PE-401
 */
@Slf4j
public class PaymentItemReader extends FlatFileItemReader<PaymentRecord> {

  private static final DateTimeFormatter[] DATE_FORMATTERS = {
    DateTimeFormatter.ISO_LOCAL_DATE, // 2024-01-15
    DateTimeFormatter.ofPattern("yyyy/MM/dd"), // 2024/01/15
    DateTimeFormatter.ofPattern("dd-MM-yyyy"), // 15-01-2024
    DateTimeFormatter.ofPattern("dd/MM/yyyy"), // 15/01/2024
    DateTimeFormatter.ofPattern("MMM dd yyyy") // Jan 15 2024
  };

  private int currentLineNumber = 0;
  private int linesToSkip = 0;

  /**
   * Creates a PaymentItemReader with the specified file resource and delimiter.
   *
   * @param resource the CSV file resource to read
   * @param delimiter the field delimiter (e.g., ",", ";", "|", "\t")
   * @param skipHeaderLine whether to skip the first line (header)
   */
  public PaymentItemReader(Resource resource, String delimiter, boolean skipHeaderLine) {
    this.linesToSkip = skipHeaderLine ? 1 : 0;
    setResource(resource);
    setLinesToSkip(linesToSkip);
    setLineMapper(createLineMapper(delimiter));
    setName("paymentItemReader");
  }

  @Override
  public PaymentRecord read() throws Exception {
    PaymentRecord record = super.read();
    if (record != null) {
      currentLineNumber++;
      record.setLineNumber(currentLineNumber + linesToSkip);
    }
    return record;
  }

  /**
   * Creates a line mapper for parsing CSV lines into PaymentRecord objects.
   *
   * @param delimiter the field delimiter
   * @return configured line mapper
   */
  private LineMapper<PaymentRecord> createLineMapper(String delimiter) {
    DefaultLineMapper<PaymentRecord> lineMapper = new DefaultLineMapper<>();

    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter(delimiter);
    tokenizer.setNames(
        "paymentId",
        "debtorAccount",
        "debtorName",
        "creditorAccount",
        "creditorName",
        "amount",
        "currency",
        "paymentReference",
        "valueDate",
        "debtorBankCode",
        "creditorBankCode",
        "paymentType");
    tokenizer.setStrict(false); // Allow missing fields

    lineMapper.setLineTokenizer(tokenizer);
    lineMapper.setFieldSetMapper(new PaymentRecordFieldSetMapper());

    return lineMapper;
  }

  /** Custom FieldSetMapper for PaymentRecord with enhanced date parsing and error handling. */
  private static class PaymentRecordFieldSetMapper
      extends BeanWrapperFieldSetMapper<PaymentRecord> {

    public PaymentRecordFieldSetMapper() {
      setTargetType(PaymentRecord.class);
    }

    @Override
    public PaymentRecord mapFieldSet(FieldSet fieldSet) {
      try {
        PaymentRecord record =
            PaymentRecord.builder()
                .paymentId(fieldSet.readString("paymentId"))
                .debtorAccount(fieldSet.readString("debtorAccount"))
                .debtorName(fieldSet.readString("debtorName"))
                .creditorAccount(fieldSet.readString("creditorAccount"))
                .creditorName(fieldSet.readString("creditorName"))
                .amount(parseAmount(fieldSet.readString("amount")))
                .currency(fieldSet.readString("currency"))
                .paymentReference(fieldSet.readString("paymentReference"))
                .valueDate(parseDate(fieldSet.readString("valueDate")))
                .debtorBankCode(fieldSet.readString("debtorBankCode"))
                .creditorBankCode(fieldSet.readString("creditorBankCode"))
                .paymentType(fieldSet.readString("paymentType"))
                .build();

        return record;
      } catch (Exception e) {
        log.error("Error mapping field set to PaymentRecord: {}", e.getMessage());
        // Return partial record with error for downstream validation
        PaymentRecord errorRecord = new PaymentRecord();
        errorRecord.setValidationErrors("Parse error: " + e.getMessage());
        return errorRecord;
      }
    }

    private BigDecimal parseAmount(String amountStr) {
      if (amountStr == null || amountStr.trim().isEmpty()) {
        return null;
      }
      try {
        // Remove common currency symbols and whitespace
        String cleaned = amountStr.replaceAll("[^0-9.,\\-]", "").trim();
        return new BigDecimal(cleaned);
      } catch (NumberFormatException e) {
        log.warn("Failed to parse amount: {}", amountStr);
        return null;
      }
    }

    private LocalDate parseDate(String dateStr) {
      if (dateStr == null || dateStr.trim().isEmpty()) {
        return null;
      }

      String cleaned = dateStr.trim();
      for (DateTimeFormatter formatter : DATE_FORMATTERS) {
        try {
          return LocalDate.parse(cleaned, formatter);
        } catch (DateTimeParseException ignored) {
          // Try next formatter
        }
      }

      log.warn("Failed to parse date: {}", dateStr);
      return null;
    }
  }
}
