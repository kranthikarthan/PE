package com.payments.bankservafricaadapter.exception;

/** Exception thrown when BankservAfrica adapter operations are invalid */
public class InvalidBankservAfricaAdapterException extends RuntimeException {

  public InvalidBankservAfricaAdapterException(String message) {
    super(message);
  }

  public InvalidBankservAfricaAdapterException(String message, Throwable cause) {
    super(message, cause);
  }
}
