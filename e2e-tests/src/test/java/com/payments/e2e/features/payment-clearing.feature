# Payment Clearing E2E Tests

@payment-clearing @clearing-adapters
Feature: Payment Clearing
  As a payment system
  I want to clear payments through appropriate clearing systems
  So that payments are settled between financial institutions

  Background:
    Given the payment system is running
    And test tenant "TENANT-TEST-001" is configured
    And all clearing adapters are available
    And clearing systems are mocked

  @clearing-success @samos-clearing
  Scenario: Successful SAMOS Clearing
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | CLEAR-TEST-001 |
    And the payment is routed to SAMOS clearing
    When the payment is submitted to SAMOS
    Then the SAMOS adapter should format the message correctly
    And the message should be submitted to SAMOS
    And the SAMOS system should acknowledge the payment
    And the payment should be marked as submitted
    And a clearing submission event should be published

  @clearing-success @rtc-clearing
  Scenario: Successful RTC Clearing
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | CLEAR-TEST-002 |
    And the payment is routed to RTC clearing
    When the payment is submitted to RTC
    Then the RTC adapter should format the message correctly
    And the message should be submitted to RTC
    And the RTC system should acknowledge the payment
    And the payment should be marked as submitted
    And a clearing submission event should be published

  @clearing-success @payshap-clearing
  Scenario: Successful PayShap Clearing
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | CLEAR-TEST-003 |
    And the payment is routed to PayShap clearing
    When the payment is submitted to PayShap
    Then the PayShap adapter should format the message correctly
    And the message should be submitted to PayShap
    And the PayShap system should acknowledge the payment
    And the payment should be marked as submitted
    And a clearing submission event should be published

  @clearing-success @swift-clearing
  Scenario: Successful SWIFT Clearing
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | USD |
      | Reference | CLEAR-TEST-004 |
    And the payment is routed to SWIFT clearing
    When the payment is submitted to SWIFT
    Then the SWIFT adapter should format the MT103 message correctly
    And the message should be submitted to SWIFT
    And the SWIFT system should acknowledge the payment
    And the payment should be marked as submitted
    And a clearing submission event should be published

  @clearing-failure @clearing-timeout
  Scenario: Clearing System Timeout
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | CLEAR-TEST-005 |
    And the payment is routed to SAMOS clearing
    And the SAMOS system is configured to timeout
    When the payment is submitted to SAMOS
    Then the SAMOS adapter should timeout
    And the payment should be marked as failed
    And a clearing timeout event should be published
    And the payment should be queued for retry

  @clearing-failure @clearing-rejection
  Scenario: Clearing System Rejection
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | CLEAR-TEST-006 |
    And the payment is routed to SAMOS clearing
    And the SAMOS system is configured to reject the payment
    When the payment is submitted to SAMOS
    Then the SAMOS adapter should receive a rejection
    And the payment should be marked as rejected
    And a clearing rejection event should be published
    And the payment should be queued for manual review

  @clearing-success @clearing-completion
  Scenario: Clearing System Completion
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | CLEAR-TEST-007 |
    And the payment is routed to SAMOS clearing
    And the payment is submitted to SAMOS
    When the SAMOS system completes the payment
    Then the SAMOS adapter should receive a completion notification
    And the payment should be marked as completed
    And a clearing completion event should be published
    And the payment should be ready for settlement

  @clearing-success @multi-clearing
  Scenario: Payment Routing to Multiple Clearing Systems
    Given a processed payment request is created
      | Field | Value |
      | From Account | ACC-TEST-001 |
      | To Account | ACC-TEST-002 |
      | Amount | 100.00 |
      | Currency | ZAR |
      | Reference | CLEAR-TEST-008 |
    And the payment can be routed to multiple clearing systems
    When the payment is submitted for clearing
    Then the routing service should select the optimal clearing system
    And the payment should be submitted to the selected clearing system
    And the payment should be marked as submitted
    And a clearing submission event should be published

  @clearing-success @batch-clearing
  Scenario: Batch Payment Clearing
    Given 10 processed payment requests are created
    And each payment has amount "50.00"
    And all payments are routed to SAMOS clearing
    When all payments are submitted for batch clearing
    Then all payments should be submitted to SAMOS
    And all payments should be acknowledged by SAMOS
    And all payments should be marked as submitted
    And a batch clearing submission event should be published

  @performance @clearing-throughput
  Scenario: High Volume Payment Clearing
    Given 1000 processed payment requests are created
    And each payment has amount between "10.00" and "100.00"
    And payments are routed to different clearing systems
    When all payments are submitted for clearing
    Then all payments should be cleared within "300" seconds
    And the clearing success rate should be "98" percent
    And the average clearing time should be less than "2" seconds
    And no payment should be lost during clearing
