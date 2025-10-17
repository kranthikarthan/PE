package com.payments.bankservafricaadapter.exception;

/** Exception thrown when BankservAfrica EFT message operations are invalid */
public class InvalidBankservAfricaEftMessageException extends RuntimeException {

  public InvalidBankservAfricaEftMessageException(String message) {
    super(message);
  }

  public InvalidBankservAfricaEftMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
