# UETR (Unique End-to-End Transaction Reference) Implementation Guide

## Overview

The Unique End-to-End Transaction Reference (UETR) is a 36-character alphanumeric code that uniquely identifies a payment transaction from initiation to completion across all systems and participants in the payment chain. This implementation provides comprehensive UETR support across the entire payment engine system.

## Table of Contents

1. [UETR Format and Structure](#uetr-format-and-structure)
2. [Core Services](#core-services)
3. [Message Processing Integration](#message-processing-integration)
4. [Fraud Risk Assessment Integration](#fraud-risk-assessment-integration)
5. [Audit Logging Integration](#audit-logging-integration)
6. [React Frontend Integration](#react-frontend-integration)
7. [API Endpoints](#api-endpoints)
8. [Database Schema](#database-schema)
9. [Configuration](#configuration)
10. [Monitoring and Observability](#monitoring-and-observability)
11. [Best Practices](#best-practices)
12. [Troubleshooting](#troubleshooting)

## UETR Format and Structure

### Format
```
XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
```

### Components
- **Timestamp Prefix (8 chars)**: YYYYMMDD format
- **System ID (4 chars)**: PE01 (Payment Engine System ID)
- **Message Type ID (4 chars)**: P001, P008, P002, C054, etc.
- **Sequence Number (4 chars)**: 4-digit hexadecimal number
- **UUID Suffix (16 chars)**: 16-character alphanumeric string

### Example
```
20241201-PE01-P001-A1B2-550E8400E29B41D4
```

## Core Services

### 1. UetrGenerationService

**Location**: `/workspace/services/shared/src/main/java/com/paymentengine/shared/service/UetrGenerationService.java`

**Key Methods**:
- `generateUetr(String messageType, String tenantId)`: Generate new UETR
- `generateResponseUetr(String originalUetr, String responseMessageType)`: Generate response UETR
- `isValidUetr(String uetr)`: Validate UETR format
- `extractTimestamp(String uetr)`: Extract timestamp from UETR
- `extractSystemId(String uetr)`: Extract system ID from UETR
- `extractMessageType(String uetr)`: Extract message type from UETR
- `areRelatedUetrs(String uetr1, String uetr2)`: Check if UETRs are related

**Usage Example**:
```java
@Autowired
private UetrGenerationService uetrGenerationService;

// Generate new UETR
String uetr = uetrGenerationService.generateUetr("PAIN001", "tenant-123");

// Validate UETR
boolean isValid = uetrGenerationService.isValidUetr(uetr);

// Extract components
String timestamp = uetrGenerationService.extractTimestamp(uetr);
String systemId = uetrGenerationService.extractSystemId(uetr);
```

### 2. UetrTrackingService

**Location**: `/workspace/services/shared/src/main/java/com/paymentengine/shared/service/UetrTrackingService.java`

**Key Methods**:
- `trackUetr(String uetr, String messageType, String tenantId, String transactionReference, String direction)`: Track new UETR
- `updateUetrStatus(String uetr, String status, String statusReason, String processingSystem)`: Update UETR status
- `linkRelatedUetrs(String originalUetr, String relatedUetr, String relationshipType)`: Link related UETRs
- `getUetrTracking(String uetr)`: Get UETR tracking information
- `getUetrJourney(String uetr)`: Get UETR journey (all related UETRs)
- `searchUetrs(...)`: Search UETRs by criteria
- `getUetrStatistics(...)`: Get UETR statistics

**Usage Example**:
```java
@Autowired
private UetrTrackingService uetrTrackingService;

// Track new UETR
UetrTrackingRecord record = uetrTrackingService.trackUetr(
    uetr, "PAIN001", tenantId, messageId, "INBOUND");

// Update status
uetrTrackingService.updateUetrStatus(uetr, "PROCESSING", 
    "Message being processed", "Middleware Service");

// Get journey
UetrJourney journey = uetrTrackingService.getUetrJourney(uetr);
```

## Message Processing Integration

### Scheme Processing Service

**Location**: `/workspace/services/middleware/src/main/java/com/paymentengine/middleware/service/impl/SchemeProcessingServiceImpl.java`

**UETR Integration Points**:

1. **UETR Generation**: Generated at the start of payment processing
2. **UETR Tracking**: Tracked throughout the payment lifecycle
3. **Status Updates**: Updated at each processing stage
4. **Message Transformation**: Included in PACS.008 messages

**Key Changes**:
```java
// Generate UETR for end-to-end tracking
uetr = uetrGenerationService.generateUetr("PAIN001", tenantId);

// Set UETR in payment info for use in transformation
paymentInfo.setUetr(uetr);

// Track UETR in the system
uetrTrackingService.trackUetr(uetr, "PAIN001", tenantId, messageId, "INBOUND");
uetrTrackingService.updateUetrStatus(uetr, "RECEIVED", "PAIN.001 message received", "Middleware Service");

// Update status throughout processing
uetrTrackingService.updateUetrStatus(uetr, "APPROVED", "Fraud/risk assessment passed", "Fraud Risk Monitoring Service");
uetrTrackingService.updateUetrStatus(uetr, "PROCESSING", "Routing to clearing system: " + clearingSystemCode, "Middleware Service");
uetrTrackingService.updateUetrStatus(uetr, "COMPLETED", "Payment processing completed successfully", "Middleware Service");
```

### Payment Info Integration

**Location**: `/workspace/services/middleware/src/main/java/com/paymentengine/middleware/service/Pain001ToPacs008TransformationService.java`

**UETR Field Addition**:
```java
class PaymentInfo {
    private String uetr;
    
    public String getUetr() { return uetr; }
    public void setUetr(String uetr) { this.uetr = uetr; }
}
```

### PACS.008 Message Integration

**Location**: `/workspace/services/middleware/src/main/java/com/paymentengine/middleware/service/impl/Pain001ToPacs008TransformationServiceImpl.java`

**UETR in Payment Identification**:
```java
// Payment Identification
Map<String, Object> pmtId = new HashMap<>();
pmtId.put("InstrId", paymentInfo.getInstructionId());
pmtId.put("EndToEndId", paymentInfo.getEndToEndId());
pmtId.put("TxId", "TX-" + System.currentTimeMillis());
// Add UETR for end-to-end tracking
if (paymentInfo.getUetr() != null) {
    pmtId.put("UETR", paymentInfo.getUetr());
}
cdtTrfTxInf.put("PmtId", pmtId);
```

## Fraud Risk Assessment Integration

### Fraud Risk Monitoring Service

**Location**: `/workspace/services/middleware/src/main/java/com/paymentengine/middleware/service/impl/FraudRiskMonitoringServiceImpl.java`

**UETR Integration**:
```java
@Override
public CompletableFuture<FraudRiskAssessment> assessPaymentRisk(
        String transactionReference,
        String tenantId,
        String paymentType,
        String localInstrumentationCode,
        String clearingSystemCode,
        FraudRiskConfiguration.PaymentSource paymentSource,
        Map<String, Object> paymentData,
        String uetr) {
    
    logger.info("Starting fraud/risk assessment for transaction: {}, UETR: {}, tenant: {}, paymentType: {}, paymentSource: {}", 
               transactionReference, uetr, tenantId, paymentType, paymentSource);
    
    // Assessment logic with UETR tracking
}
```

**Benefits**:
- UETR correlation in fraud assessments
- Enhanced audit trail for fraud decisions
- Better tracking of fraud-related transactions

## Audit Logging Integration

### Audit Logging Service

**Location**: `/workspace/services/middleware/src/main/java/com/paymentengine/middleware/audit/AuditLoggingService.java`

**UETR Support**:
```java
// UETR field in AuditEvent
private String uetr;

// UETR builder method
public Builder uetr(String uetr) {
    event.uetr = uetr;
    return this;
}

// Enhanced message processing logging
public void logMessageProcessingEvent(String messageId, String messageType, String tenantId,
                                    String action, boolean success, long processingTimeMs,
                                    Map<String, Object> metadata, String uetr) {
    // Log with UETR
}

// Dedicated UETR tracking events
public void logUetrTrackingEvent(String uetr, String messageType, String tenantId,
                               String action, boolean success, String status,
                               String processingSystem, Map<String, Object> metadata) {
    // Log UETR-specific events
}
```

## React Frontend Integration

### UETR Management Component

**Location**: `/workspace/frontend/src/components/UetrManagement.tsx`

**Features**:
- UETR search and tracking
- UETR generation
- UETR validation
- UETR statistics
- UETR journey visualization

**Key Functionality**:
```typescript
// Search UETR
const handleSearch = async () => {
    const response = await axios.get(`/api/v1/uetr/track/${searchUetr}`);
    if (response.data.found) {
        setSelectedUetr(response.data.trackingRecord);
    }
};

// Generate UETR
const handleGenerateUetr = async () => {
    const response = await axios.post('/api/v1/uetr/generate', null, {
        params: { messageType: generateMessageType, tenantId: generateTenantId }
    });
    setGeneratedUetr(response.data.uetr);
};

// View UETR journey
const handleViewJourney = async (uetr: string) => {
    const response = await axios.get(`/api/v1/uetr/journey/${uetr}`);
    setUetrJourney(response.data.journey);
};
```

## API Endpoints

### UETR Controller

**Location**: `/workspace/services/middleware/src/main/java/com/paymentengine/middleware/controller/UetrController.java`

**Endpoints**:

1. **POST /api/v1/uetr/generate**
   - Generate new UETR
   - Parameters: messageType, tenantId
   - Response: Generated UETR with metadata

2. **GET /api/v1/uetr/validate/{uetr}**
   - Validate UETR format
   - Response: Validation result with extracted components

3. **GET /api/v1/uetr/track/{uetr}**
   - Get UETR tracking information
   - Response: UETR tracking record

4. **GET /api/v1/uetr/journey/{uetr}**
   - Get UETR journey (all related UETRs)
   - Response: UETR journey with all steps

5. **GET /api/v1/uetr/search**
   - Search UETRs by criteria
   - Parameters: tenantId, messageType, status, dateFrom, dateTo
   - Response: List of matching UETR records

6. **GET /api/v1/uetr/statistics**
   - Get UETR statistics
   - Parameters: tenantId, dateFrom, dateTo
   - Response: UETR statistics

7. **GET /api/v1/uetr/related/{uetr1}/{uetr2}**
   - Check if two UETRs are related
   - Response: Relationship information

## Database Schema

### UETR Tracking Table

```sql
CREATE TABLE uetr_tracking_records (
    id UUID PRIMARY KEY,
    uetr VARCHAR(36) NOT NULL UNIQUE,
    message_type VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    status_reason TEXT,
    processing_system VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_direction CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    CONSTRAINT chk_status CHECK (status IN ('INITIATED', 'RECEIVED', 'PROCESSING', 'APPROVED', 'REJECTED', 'MANUAL_REVIEW', 'SENT_TO_CLEARING', 'COMPLETED', 'FAILED'))
);

-- Indexes
CREATE INDEX idx_uetr_tracking_uetr ON uetr_tracking_records(uetr);
CREATE INDEX idx_uetr_tracking_tenant_id ON uetr_tracking_records(tenant_id);
CREATE INDEX idx_uetr_tracking_transaction_reference ON uetr_tracking_records(transaction_reference);
CREATE INDEX idx_uetr_tracking_status ON uetr_tracking_records(status);
CREATE INDEX idx_uetr_tracking_created_at ON uetr_tracking_records(created_at);
```

### UETR Relationships Table

```sql
CREATE TABLE uetr_relationships (
    id UUID PRIMARY KEY,
    original_uetr VARCHAR(36) NOT NULL,
    related_uetr VARCHAR(36) NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (original_uetr) REFERENCES uetr_tracking_records(uetr),
    FOREIGN KEY (related_uetr) REFERENCES uetr_tracking_records(uetr),
    
    CONSTRAINT chk_relationship_type CHECK (relationship_type IN ('REQUEST_RESPONSE', 'CANCELLATION', 'AMENDMENT', 'INQUIRY_RESPONSE'))
);

-- Indexes
CREATE INDEX idx_uetr_relationships_original ON uetr_relationships(original_uetr);
CREATE INDEX idx_uetr_relationships_related ON uetr_relationships(related_uetr);
```

## Configuration

### Application Properties

```yaml
# UETR Configuration
app:
  uetr:
    enabled: true
    generation:
      system-id: "PE01"
      timestamp-format: "yyyyMMdd"
    tracking:
      enabled: true
      cache-ttl: 3600 # seconds
    validation:
      strict-format: true
```

### Message Type Mappings

```yaml
app:
  uetr:
    message-types:
      PAIN001: "P001"
      PACS008: "P008"
      PACS002: "P002"
      PAIN002: "P002"
      CAMT054: "C054"
      CAMT055: "C055"
      CAMT056: "C056"
      CAMT029: "C029"
```

## Monitoring and Observability

### Metrics

**UETR Generation Metrics**:
- `uetr_generated_total`: Total UETRs generated
- `uetr_generation_duration_seconds`: UETR generation time
- `uetr_validation_total`: UETR validations performed

**UETR Tracking Metrics**:
- `uetr_tracking_total`: Total UETRs tracked
- `uetr_status_changes_total`: UETR status changes
- `uetr_processing_duration_seconds`: UETR processing time

**UETR Statistics Metrics**:
- `uetr_completed_total`: Completed UETRs
- `uetr_failed_total`: Failed UETRs
- `uetr_pending_total`: Pending UETRs

### Logging

**UETR Logging Format**:
```json
{
  "timestamp": "2024-12-01T10:30:00Z",
  "level": "INFO",
  "logger": "UetrGenerationService",
  "message": "Generated UETR: 20241201-PE01-P001-A1B2-550E8400E29B41D4 for messageType: PAIN001, tenantId: tenant-123",
  "uetr": "20241201-PE01-P001-A1B2-550E8400E29B41D4",
  "messageType": "PAIN001",
  "tenantId": "tenant-123"
}
```

### Dashboards

**UETR Dashboard Components**:
- UETR generation rate
- UETR processing status distribution
- UETR processing time trends
- UETR error rates
- UETR journey visualization

## Best Practices

### 1. UETR Generation
- Always generate UETR at the start of payment processing
- Use consistent message type mappings
- Include tenant context in generation
- Validate UETR format before use

### 2. UETR Tracking
- Track UETR at every processing stage
- Update status with meaningful reasons
- Link related UETRs (request/response pairs)
- Maintain audit trail for all status changes

### 3. UETR in Messages
- Include UETR in all ISO 20022 messages
- Use UETR for end-to-end correlation
- Maintain UETR consistency across message transformations
- Validate UETR in incoming messages

### 4. Performance
- Cache UETR validation results
- Use async processing for UETR tracking
- Batch UETR status updates when possible
- Monitor UETR processing performance

### 5. Security
- Log UETR access for audit purposes
- Validate UETR ownership by tenant
- Encrypt UETR in sensitive contexts
- Monitor for UETR manipulation attempts

## Troubleshooting

### Common Issues

1. **Invalid UETR Format**
   - Check UETR length (must be 36 characters)
   - Verify format: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
   - Ensure proper character set (alphanumeric)

2. **UETR Not Found**
   - Check if UETR was properly tracked
   - Verify tenant isolation
   - Check database connectivity

3. **UETR Generation Failures**
   - Check system ID configuration
   - Verify message type mappings
   - Check timestamp generation

4. **UETR Tracking Issues**
   - Verify tracking service is enabled
   - Check database permissions
   - Monitor for tracking service errors

### Debug Commands

```bash
# Check UETR format
curl -X GET "http://localhost:8080/api/v1/uetr/validate/20241201-PE01-P001-A1B2-550E8400E29B41D4"

# Search UETRs by tenant
curl -X GET "http://localhost:8080/api/v1/uetr/search?tenantId=tenant-123"

# Get UETR statistics
curl -X GET "http://localhost:8080/api/v1/uetr/statistics?tenantId=tenant-123"

# View UETR journey
curl -X GET "http://localhost:8080/api/v1/uetr/journey/20241201-PE01-P001-A1B2-550E8400E29B41D4"
```

### Monitoring Queries

```sql
-- UETR processing status
SELECT status, COUNT(*) as count 
FROM uetr_tracking_records 
WHERE created_at >= NOW() - INTERVAL '1 day'
GROUP BY status;

-- UETR processing time
SELECT 
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at))) as avg_processing_seconds,
    MAX(EXTRACT(EPOCH FROM (updated_at - created_at))) as max_processing_seconds
FROM uetr_tracking_records 
WHERE status = 'COMPLETED' 
AND created_at >= NOW() - INTERVAL '1 day';

-- Failed UETRs
SELECT uetr, status_reason, created_at 
FROM uetr_tracking_records 
WHERE status = 'FAILED' 
AND created_at >= NOW() - INTERVAL '1 day'
ORDER BY created_at DESC;
```

## Conclusion

The UETR implementation provides comprehensive end-to-end transaction tracking across the entire payment engine system. It enables:

- **Unique Transaction Identification**: Every payment has a unique, traceable identifier
- **End-to-End Tracking**: Complete visibility into payment processing lifecycle
- **Enhanced Audit Trail**: Comprehensive logging of all UETR-related activities
- **Fraud Detection Integration**: UETR correlation in fraud risk assessments
- **Operational Monitoring**: Real-time visibility into payment processing status
- **Compliance Support**: Detailed audit trails for regulatory requirements

The implementation follows ISO 20022 standards and provides a robust foundation for payment transaction tracking and reconciliation across all systems and participants in the payment chain.