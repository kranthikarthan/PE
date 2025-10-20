# E2E Testing Framework - WireMock Stubs Implementation

## Overview

This document describes the comprehensive WireMock stub implementations for clearing systems in the Payments Engine E2E testing framework.

## WireMock Stub Files Structure

```
e2e-tests/src/test/resources/wiremock/clearing-systems/
├── samos/
│   ├── payment-submit-success.json
│   ├── payment-submit-failure.json
│   └── payment-submit-timeout.json
├── rtc/
│   ├── payment-submit-success.json
│   └── payment-submit-failure.json
├── payshap/
│   └── payment-submit-success.json
├── swift/
│   └── payment-submit-success.json
└── bankservafrica/
    ├── ach-file-upload-success.json
    └── ach-file-upload-failure.json
```

## Clearing Systems Implemented

### 1. SAMOS (South African Multiple Option Settlement)
- **Purpose**: RTGS (Real-Time Gross Settlement) system
- **Endpoints**: `/api/v1/payments`
- **Format**: XML (ISO 20022)
- **Scenarios**: Success, Failure, Timeout
- **Response Time**: 30 seconds (timeout scenario)

### 2. RTC (Real-Time Clearing)
- **Purpose**: Real-time payment processing
- **Endpoints**: `/api/v1/rtc/payments`
- **Format**: JSON
- **Scenarios**: Success, Failure
- **Response Time**: 5 seconds

### 3. PayShap
- **Purpose**: P2P (Person-to-Person) payments
- **Endpoints**: `/api/v1/payshap/payments`
- **Format**: JSON
- **Scenarios**: Success
- **Features**: QR codes, mobile integration

### 4. SWIFT
- **Purpose**: International payments
- **Endpoints**: `/api/v1/swift/messages`
- **Format**: XML (ISO 20022)
- **Scenarios**: Success
- **Features**: MT103 messages, sanctions screening

### 5. BankservAfrica
- **Purpose**: ACH/EFT processing
- **Endpoints**: `/api/v1/ach/files`
- **Format**: Multipart form data
- **Scenarios**: Success, Failure
- **Features**: File upload, batch processing

## WireMock Stub Features

### Request Matching
- **Method**: POST for all payment submissions
- **Headers**: Content-Type, Authorization, Client-ID
- **Body Patterns**: JSON path matching, XPath matching for XML
- **URL Patterns**: Specific endpoints for each clearing system

### Response Templates
- **Dynamic Values**: Random UUIDs, timestamps, reference numbers
- **JSON Path Extraction**: Extract values from request body
- **Date Formatting**: ISO 8601 timestamps
- **Response Transformers**: Template-based response generation

### Scenario Coverage
- **Success Scenarios**: Successful payment processing
- **Failure Scenarios**: Validation errors, rejection reasons
- **Timeout Scenarios**: Delayed responses for timeout testing
- **Error Handling**: Proper HTTP status codes and error messages

## Integration Test Coverage

### TestDataBuilder Enhancements
- **Clearing System Mocks**: Setup methods for each clearing system
- **Test Data Generation**: Payment requests, account data, tenant context
- **Mock Configuration**: WireMock stub setup and teardown

### ClearingService Implementation
- **Payment Routing**: Route payments to appropriate clearing systems
- **Response Processing**: Handle clearing system responses
- **Status Tracking**: Track payment status through clearing process
- **Fee Calculation**: Calculate clearing fees for each system

### NotificationService Integration
- **Event Publishing**: Send clearing events and notifications
- **Status Updates**: Track payment status changes
- **System Alerts**: Handle clearing system alerts and errors

## Usage Examples

### Running E2E Tests
```bash
# Run all E2E tests
mvn test -f e2e-tests/pom.xml

# Run specific clearing system tests
mvn test -f e2e-tests/pom.xml -Dtest=ClearingAdapterIntegrationTest

# Run with specific profile
mvn test -f e2e-tests/pom.xml -Dspring.profiles.active=e2e
```

### WireMock Configuration
```java
@Bean
public WireMockServer clearingSystemMock() {
    return new WireMockServer(
        WireMockConfiguration.options()
            .port(8091)
            .usingFilesUnderDirectory("src/test/resources/wiremock/clearing-systems")
    );
}
```

### Test Data Setup
```java
@BeforeEach
void setUp() {
    testDataBuilder.setupClearingSystemMocks();
    notificationService.clearNotifications();
}
```

## Response Templates

### SAMOS Success Response
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12">
  <FIToFIPmtStsRpt>
    <GrpHdr>
      <MsgId>{{randomValue length=36 type='ALPHANUMERIC'}}</MsgId>
      <CreDtTm>{{now format='yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\''}}</CreDtTm>
    </GrpHdr>
    <TxInfAndSts>
      <TxSts>ACSC</TxSts>
      <StsRsnInf>
        <Rsn>
          <Cd>NARR</Cd>
          <Prtry>Payment successfully processed and settled</Prtry>
        </Rsn>
      </StsRsnInf>
    </TxInfAndSts>
  </FIToFIPmtStsRpt>
</Document>
```

### RTC Success Response
```json
{
  "responseId": "{{randomValue length=36 type='ALPHANUMERIC'}}",
  "paymentId": "{{jsonPath '$.paymentId'}}",
  "status": "SUCCESS",
  "statusCode": "ACSC",
  "statusDescription": "RTC payment successfully processed",
  "processingTime": "{{now format='yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\''}}",
  "settlementTime": "{{now format='yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\''}}",
  "fees": {
    "value": "2.50",
    "currency": "ZAR"
  }
}
```

## Testing Scenarios

### 1. Payment Initiation Flow
- Create payment request
- Route to appropriate clearing system
- Verify clearing system response
- Check notification events

### 2. Multiple Clearing Systems
- Test payment routing to different systems
- Verify system-specific responses
- Check fee calculations
- Validate notification events

### 3. Error Handling
- Test validation failures
- Test system timeouts
- Test rejection scenarios
- Verify error notifications

### 4. Integration Testing
- Test end-to-end payment flows
- Verify clearing system integration
- Check notification delivery
- Validate status tracking

## Best Practices

### WireMock Stub Design
- Use realistic response templates
- Include proper error scenarios
- Test timeout conditions
- Validate request matching

### Test Data Management
- Use consistent test data
- Clean up after tests
- Isolate test scenarios
- Verify mock behavior

### Integration Testing
- Test real integration points
- Verify end-to-end flows
- Check error handling
- Validate notifications

## Troubleshooting

### Common Issues
1. **WireMock not starting**: Check port configuration
2. **Stub not matching**: Verify request patterns
3. **Response not generated**: Check template syntax
4. **Test failures**: Verify mock setup

### Debugging Tips
- Enable WireMock logging
- Check request/response logs
- Verify stub matching
- Test individual scenarios

## Future Enhancements

### Planned Features
- **Performance Testing**: Load testing with WireMock
- **Chaos Engineering**: Simulate system failures
- **Contract Testing**: API contract validation
- **Monitoring Integration**: Metrics and alerting

### Additional Clearing Systems
- **Visa Direct**: International card payments
- **Mastercard Send**: P2P card payments
- **Ripple**: Cryptocurrency payments
- **Stellar**: Cross-border payments

## Conclusion

The WireMock stub implementation provides comprehensive testing coverage for all clearing systems in the Payments Engine. The framework supports realistic testing scenarios, proper error handling, and integration with the broader E2E testing infrastructure.

The implementation follows best practices for test automation, provides clear documentation, and supports the full range of payment processing scenarios required for production readiness.