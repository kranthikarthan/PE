package com.payments.bankservafricaadapter.exception;

/** Exception thrown when BankservAfrica settlement record operations are invalid */
public class InvalidBankservAfricaSettlementRecordException extends RuntimeException {

  public InvalidBankservAfricaSettlementRecordException(String message) {
    super(message);
  }

  public InvalidBankservAfricaSettlementRecordException(String message, Throwable cause) {
    super(message, cause);
  }
}
