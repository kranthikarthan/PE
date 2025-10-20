# Payment Processing E2E Tests

@payment-processing @transaction-processing
Feature: Payment Processing
  As a payment system
  I want to process payments through the transaction processing service
  So that payments are properly recorded and accounts are updated

  Background:
    Given the payment system is running
    And test tenant "TENANT-TEST-001" is configured
    And test account "ACC-TEST-001" has balance "1000.00"
    And test account "ACC-TEST-002" has balance "500.00"
    And the transaction processing service is available

  @processing-success @double-entry
  Scenario: Successful Payment Processing with Double Entry
    Given a validated payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | PROC-TEST-001 |
    When the payment is submitted for processing
    Then the transaction should be created
    And the debit entry should be recorded for "ACC-TEST-001"
    And the credit entry should be recorded for "ACC-TEST-002"
    And the source account balance should be "900.00"
    And the destination account balance should be "600.00"
    And the transaction should be marked as processed
    And a processing success event should be published

  @processing-failure @insufficient-funds
  Scenario: Payment Processing with Insufficient Funds
    Given a validated payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 1500.00 |
      | Currency | ZAR |
      | Reference | PROC-TEST-002 |
    When the payment is submitted for processing
    Then the transaction should be created
    And the account balance check should fail
    And the transaction should be marked as failed
    And a processing failure event should be published
    And the account balances should remain unchanged

  @processing-failure @account-locked
  Scenario: Payment Processing with Locked Account
    Given a validated payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | PROC-TEST-003 |
    And the source account is locked
    When the payment is submitted for processing
    Then the transaction should be created
    And the account lock check should fail
    And the transaction should be marked as failed
    And a processing failure event should be published
    And the account balances should remain unchanged

  @processing-success @fee-calculation
  Scenario: Payment Processing with Fee Calculation
    Given a validated payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | PROC-TEST-004 |
    And the payment fee is "5.00"
    When the payment is submitted for processing
    Then the transaction should be created
    And the debit entry should be recorded for "ACC-TEST-001" with amount "105.00"
    And the credit entry should be recorded for "ACC-TEST-002" with amount "100.00"
    And the fee entry should be recorded with amount "5.00"
    And the source account balance should be "895.00"
    And the destination account balance should be "600.00"
    And the transaction should be marked as processed

  @processing-success @multi-currency
  Scenario: Payment Processing with Currency Conversion
    Given a validated payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | USD |
      | Reference | PROC-TEST-005 |
    And the exchange rate is "18.50"
    When the payment is submitted for processing
    Then the transaction should be created
    And the currency conversion should be applied
    And the debit entry should be recorded with converted amount "1850.00"
    And the credit entry should be recorded with converted amount "1850.00"
    And the transaction should be marked as processed

  @processing-success @batch-processing
  Scenario: Batch Payment Processing
    Given 10 validated payment requests are created
    And each payment has amount "50.00"
    When all payments are submitted for batch processing
    Then all transactions should be created
    And all debit entries should be recorded
    And all credit entries should be recorded
    And the source account balance should be "500.00"
    And the destination account balance should be "1000.00"
    And all transactions should be marked as processed
    And a batch processing success event should be published

  @processing-failure @system-error
  Scenario: Payment Processing with System Error
    Given a validated payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | PROC-TEST-006 |
    And the transaction processing service encounters an error
    When the payment is submitted for processing
    Then the transaction should be created
    And the processing should fail
    And the transaction should be marked as failed
    And a system error event should be published
    And the account balances should remain unchanged

  @processing-success @compensation
  Scenario: Payment Processing with Compensation
    Given a validated payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | PROC-TEST-007 |
    And the payment is initially processed successfully
    And a downstream service fails
    When the compensation process is triggered
    Then the transaction should be reversed
    And the debit entry should be reversed
    And the credit entry should be reversed
    And the source account balance should be "1000.00"
    And the destination account balance should be "500.00"
    And the transaction should be marked as compensated
    And a compensation event should be published

  @performance @processing-throughput
  Scenario: High Volume Payment Processing
    Given 1000 validated payment requests are created
    And each payment has amount between "10.00" and "100.00"
    When all payments are submitted for processing
    Then all transactions should be processed within "120" seconds
    And the processing success rate should be "99" percent
    And the average processing time should be less than "500" milliseconds
    And no transaction should be lost during processing
    And all account balances should be correctly updated
