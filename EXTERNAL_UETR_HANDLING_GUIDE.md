# External UETR Handling Guide

## Overview

The Payment Engine now supports receiving UETRs from external systems (clients or clearing systems) or generating new UETRs if none are provided. This capability ensures seamless integration with external payment systems while maintaining end-to-end transaction tracking.

## Table of Contents

1. [External UETR Support](#external-uetr-support)
2. [PAIN.001 Processing](#pain001-processing)
3. [PACS.008 Processing](#pacs008-processing)
4. [UETR Extraction and Validation](#uetr-extraction-and-validation)
5. [API Endpoints](#api-endpoints)
6. [React Frontend Integration](#react-frontend-integration)
7. [Configuration](#configuration)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)

## External UETR Support

### Key Features

- **Flexible UETR Handling**: Accept external UETRs or generate new ones
- **Automatic Validation**: Validate external UETR format before use
- **Source Tracking**: Track whether UETR is from client or clearing system
- **Fallback Generation**: Generate new UETR if external one is invalid
- **Comprehensive Logging**: Log all UETR decisions and sources

### UETR Processing Flow

```
Incoming Message → Extract UETR → Validate Format → Use External or Generate New → Track UETR
```

## PAIN.001 Processing

### Enhanced PAIN.001 Processing

**Location**: `/workspace/services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/impl/SchemeProcessingServiceImpl.java`

**Key Changes**:

1. **UETR Extraction**: Extract UETR from incoming PAIN.001 message
2. **Validation**: Validate external UETR format
3. **Decision Logic**: Use external UETR if valid, generate new if invalid/blank
4. **Logging**: Comprehensive logging of UETR decisions

**Implementation**:

```java
// Extract UETR from incoming PAIN.001 message or generate if blank
String externalUetr = uetrGenerationService.extractUetrFromMessage(pain001Message, "PAIN001");
uetr = uetrGenerationService.getOrGenerateUetr("PAIN001", tenantId, externalUetr);

if (externalUetr != null && !externalUetr.trim().isEmpty()) {
    if (uetr.equals(externalUetr)) {
        logger.info("Using external UETR from client: {} for PAIN.001 messageId: {}, tenantId: {}", 
                   uetr, messageId, tenantId);
    } else {
        logger.warn("External UETR from client was invalid: {}, generated new UETR: {} for PAIN.001 messageId: {}, tenantId: {}", 
                   externalUetr, uetr, messageId, tenantId);
    }
} else {
    logger.info("No UETR in PAIN.001 message, generated new UETR: {} for messageId: {}, tenantId: {}", 
               uetr, messageId, tenantId);
}
```

### UETR Extraction from PAIN.001

**Location**: `/workspace/services/shared/src/main/java/com/paymentengine/shared/service/UetrGenerationService.java`

**Extraction Logic**:

```java
private String extractUetrFromPain001(Map<String, Object> message) {
    try {
        Map<String, Object> cstmrCdtTrfInitn = (Map<String, Object>) message.get("CstmrCdtTrfInitn");
        if (cstmrCdtTrfInitn == null) return null;
        
        List<Map<String, Object>> pmtInfList = (List<Map<String, Object>>) cstmrCdtTrfInitn.get("PmtInf");
        if (pmtInfList == null || pmtInfList.isEmpty()) return null;
        
        Map<String, Object> pmtInf = pmtInfList.get(0);
        List<Map<String, Object>> cdtTrfTxInfList = (List<Map<String, Object>>) pmtInf.get("CdtTrfTxInf");
        if (cdtTrfTxInfList == null || cdtTrfTxInfList.isEmpty()) return null;
        
        Map<String, Object> cdtTrfTxInf = cdtTrfTxInfList.get(0);
        Map<String, Object> pmtId = (Map<String, Object>) cdtTrfTxInf.get("PmtId");
        if (pmtId == null) return null;
        
        return (String) pmtId.get("UETR");
    } catch (Exception e) {
        logger.error("Error extracting UETR from PAIN.001 message", e);
        return null;
    }
}
```

## PACS.008 Processing

### Incoming PACS.008 Processing

**Location**: `/workspace/services/payment-processing/src/main/java/com/paymentengine/payment-processing/service/impl/SchemeProcessingServiceImpl.java`

**New Method**: `processIncomingPacs008`

**Key Features**:

1. **External UETR Support**: Extract and validate UETR from clearing system
2. **Fallback Generation**: Generate new UETR if external one is invalid
3. **Status Tracking**: Track UETR through processing stages
4. **Response Generation**: Generate PACS.002 response

**Implementation**:

```java
public CompletableFuture<Map<String, Object>> processIncomingPacs008(
        Map<String, Object> pacs008Message,
        String tenantId) {
    
    return CompletableFuture.supplyAsync(() -> {
        // Extract UETR from incoming PACS.008 message or generate if blank
        String externalUetr = uetrGenerationService.extractUetrFromMessage(pacs008Message, "PACS008");
        uetr = uetrGenerationService.getOrGenerateUetr("PACS008", tenantId, externalUetr);
        
        if (externalUetr != null && !externalUetr.trim().isEmpty()) {
            if (uetr.equals(externalUetr)) {
                logger.info("Using external UETR from clearing system: {} for PACS.008 message, tenantId: {}", 
                           uetr, tenantId);
            } else {
                logger.warn("External UETR from clearing system was invalid: {}, generated new UETR: {} for PACS.008 message, tenantId: {}", 
                           externalUetr, uetr, tenantId);
            }
        } else {
            logger.info("No UETR in PACS.008 message from clearing system, generated new UETR: {} for tenantId: {}", 
                       uetr, tenantId);
        }
        
        // Track UETR and process message
        uetrTrackingService.trackUetr(uetr, "PACS008", tenantId, messageId, "INBOUND");
        uetrTrackingService.updateUetrStatus(uetr, "RECEIVED", "PACS.008 message received from clearing system", "Payment Processing Service");
        
        // Process and return result
        return result;
    });
}
```

### UETR Extraction from PACS.008

**Extraction Logic**:

```java
private String extractUetrFromPacs008(Map<String, Object> message) {
    try {
        Map<String, Object> fiToFICustomerCreditTransfer = (Map<String, Object>) message.get("FIToFICstmrCdtTrf");
        if (fiToFICustomerCreditTransfer == null) return null;
        
        List<Map<String, Object>> cdtTrfTxInfList = (List<Map<String, Object>>) fiToFICustomerCreditTransfer.get("CdtTrfTxInf");
        if (cdtTrfTxInfList == null || cdtTrfTxInfList.isEmpty()) return null;
        
        Map<String, Object> cdtTrfTxInf = cdtTrfTxInfList.get(0);
        Map<String, Object> pmtId = (Map<String, Object>) cdtTrfTxInf.get("PmtId");
        if (pmtId == null) return null;
        
        return (String) pmtId.get("UETR");
    } catch (Exception e) {
        logger.error("Error extracting UETR from PACS.008 message", e);
        return null;
    }
}
```

## UETR Extraction and Validation

### Enhanced UETR Generation Service

**Location**: `/workspace/services/shared/src/main/java/com/paymentengine/shared/service/UetrGenerationService.java`

**New Methods**:

1. **`getOrGenerateUetr()`**: Main method for UETR handling
2. **`extractUetrFromMessage()`**: Extract UETR from any ISO 20022 message
3. **Message-specific extraction methods**: For each message type

**Core Logic**:

```java
public String getOrGenerateUetr(String messageType, String tenantId, String externalUetr) {
    // If external UETR is provided and valid, use it
    if (externalUetr != null && !externalUetr.trim().isEmpty() && isValidUetr(externalUetr)) {
        logger.info("Using external UETR: {} for messageType: {}, tenantId: {}", 
                   externalUetr, messageType, tenantId);
        return externalUetr;
    }
    
    // Generate new UETR if external UETR is not provided or invalid
    if (externalUetr != null && !externalUetr.trim().isEmpty()) {
        logger.warn("External UETR provided but invalid: {} for messageType: {}, tenantId: {}. Generating new UETR.", 
                   externalUetr, messageType, tenantId);
    } else {
        logger.info("No external UETR provided for messageType: {}, tenantId: {}. Generating new UETR.", 
                   messageType, tenantId);
    }
    
    return generateUetr(messageType, tenantId);
}
```

### Supported Message Types

The system supports UETR extraction from:

- **PAIN.001**: Customer Credit Transfer Initiation
- **PACS.008**: Financial Institution Credit Transfer
- **PACS.002**: Financial Institution Payment Status Report
- **PAIN.002**: Customer Payment Status Report
- **CAMT.054**: Bank Notification
- **CAMT.055**: Payment Cancellation Request

## API Endpoints

### Incoming Message Controller

**Location**: `/workspace/services/payment-processing/src/main/java/com/paymentengine/payment-processing/controller/IncomingMessageController.java`

**Endpoints**:

1. **POST /api/v1/incoming/pacs008**
   - Process incoming PACS.008 from clearing system
   - Parameters: tenantId
   - Body: PACS.008 message
   - Response: Processing result with UETR information

2. **POST /api/v1/incoming/pacs002**
   - Process incoming PACS.002 from clearing system
   - Parameters: tenantId
   - Body: PACS.002 message

3. **POST /api/v1/incoming/camt054**
   - Process incoming CAMT.054 bank notification
   - Parameters: tenantId
   - Body: CAMT.054 message

4. **POST /api/v1/incoming/camt055**
   - Process incoming CAMT.055 cancellation request
   - Parameters: tenantId
   - Body: CAMT.055 message

### Enhanced UETR Controller

**Location**: `/workspace/services/payment-processing/src/main/java/com/paymentengine/payment-processing/controller/UetrController.java`

**New Endpoints**:

1. **POST /api/v1/uetr/process-external**
   - Process external UETR from client or clearing system
   - Parameters: uetr, messageType, tenantId, source
   - Response: Processing result with tracking information

**Enhanced Endpoints**:

1. **GET /api/v1/uetr/validate/{uetr}**
   - Enhanced validation with external/internal classification
   - Response includes: isValid, isExternal, source, isTracked, trackingStatus

## React Frontend Integration

### Enhanced UETR Management Component

**Location**: `/workspace/frontend/src/components/UetrManagement.tsx`

**New Features**:

1. **External UETR Tab**: Dedicated tab for processing external UETRs
2. **Enhanced Validation**: Shows external/internal classification
3. **Source Tracking**: Displays UETR source information
4. **Processing Results**: Shows detailed processing results

**External UETR Processing Form**:

```typescript
// External UETR processing state
const [externalUetr, setExternalUetr] = useState('');
const [externalMessageType, setExternalMessageType] = useState('PAIN001');
const [externalTenantId, setExternalTenantId] = useState('');
const [externalSource, setExternalSource] = useState('CLIENT');
const [externalProcessingResult, setExternalProcessingResult] = useState<any>(null);

// Process external UETR
const handleProcessExternalUetr = async () => {
    const response = await axios.post('/api/v1/uetr/process-external', null, {
        params: {
            uetr: externalUetr,
            messageType: externalMessageType,
            tenantId: externalTenantId,
            source: externalSource
        }
    });
    setExternalProcessingResult(response.data);
};
```

**Enhanced Validation Display**:

```typescript
{validationResult && (
    <Alert severity={validationResult.isValid ? "success" : "error"}>
        <Typography variant="body1">
            UETR is {validationResult.isValid ? 'valid' : 'invalid'}
        </Typography>
        {validationResult.isValid && (
            <Box sx={{ mt: 2 }}>
                <Typography variant="body2">
                    <strong>Source:</strong> {validationResult.source}
                </Typography>
                <Typography variant="body2">
                    <strong>External:</strong> {validationResult.isExternal ? 'Yes' : 'No'}
                </Typography>
                <Typography variant="body2">
                    <strong>Tracked in System:</strong> {validationResult.isTracked ? 'Yes' : 'No'}
                </Typography>
                {validationResult.isTracked && (
                    <>
                        <Typography variant="body2">
                            <strong>Status:</strong> {validationResult.trackingStatus}
                        </Typography>
                        <Typography variant="body2">
                            <strong>Tenant ID:</strong> {validationResult.tenantId}
                        </Typography>
                    </>
                )}
            </Box>
        )}
    </Alert>
)}
```

## Configuration

### Application Properties

```yaml
# External UETR Configuration
app:
  uetr:
    external:
      enabled: true
      validation:
        strict-format: true
        allow-invalid-fallback: true
      logging:
        external-usage: true
        validation-failures: true
      sources:
        client: "CLIENT"
        clearing-system: "CLEARING_SYSTEM"
```

### Message Type Configuration

```yaml
app:
  uetr:
    message-types:
      PAIN001:
        extraction-path: "CstmrCdtTrfInitn.PmtInf[0].CdtTrfTxInf[0].PmtId.UETR"
        validation: true
      PACS008:
        extraction-path: "FIToFICstmrCdtTrf.CdtTrfTxInf[0].PmtId.UETR"
        validation: true
      PACS002:
        extraction-path: "Document.FIToFIPmtStsRpt.TxInfAndSts[0].OrgnlTxId.OrgnlUETR"
        validation: true
```

## Best Practices

### 1. External UETR Handling

- **Always Validate**: Validate external UETR format before use
- **Log Decisions**: Log whether external UETR was used or new one generated
- **Track Sources**: Track whether UETR came from client or clearing system
- **Fallback Gracefully**: Generate new UETR if external one is invalid

### 2. Message Processing

- **Extract Early**: Extract UETR at the beginning of message processing
- **Validate Format**: Ensure UETR follows proper format
- **Track Throughout**: Track UETR through all processing stages
- **Link Related**: Link related UETRs (request/response pairs)

### 3. Error Handling

- **Invalid UETR**: Log warning and generate new UETR
- **Missing UETR**: Log info and generate new UETR
- **Processing Errors**: Update UETR status to failed
- **Validation Errors**: Return clear error messages

### 4. Performance

- **Cache Validation**: Cache UETR validation results
- **Async Processing**: Use async processing for UETR tracking
- **Batch Updates**: Batch UETR status updates when possible
- **Monitor Performance**: Monitor UETR processing performance

### 5. Security

- **Validate Ownership**: Ensure UETR belongs to correct tenant
- **Audit Access**: Log all UETR access and modifications
- **Encrypt Sensitive**: Encrypt UETR in sensitive contexts
- **Monitor Abuse**: Monitor for UETR manipulation attempts

## Troubleshooting

### Common Issues

1. **External UETR Not Extracted**
   - Check message structure and extraction path
   - Verify UETR field exists in message
   - Check extraction method implementation

2. **Invalid External UETR**
   - Check UETR format (36 characters, proper structure)
   - Verify character set (alphanumeric only)
   - Check for special characters or spaces

3. **UETR Generation Failures**
   - Check system ID configuration
   - Verify message type mappings
   - Check timestamp generation

4. **Tracking Issues**
   - Verify tracking service is enabled
   - Check database connectivity
   - Monitor for tracking service errors

### Debug Commands

```bash
# Test UETR extraction from message
curl -X POST "http://localhost:8080/api/v1/incoming/pacs008?tenantId=tenant-123" \
  -H "Content-Type: application/json" \
  -d '{"FIToFICstmrCdtTrf": {"CdtTrfTxInf": [{"PmtId": {"UETR": "20241201-EXT01-P008-A1B2-550E8400E29B41D4"}}]}}'

# Validate external UETR
curl -X GET "http://localhost:8080/api/v1/uetr/validate/20241201-EXT01-P008-A1B2-550E8400E29B41D4"

# Process external UETR
curl -X POST "http://localhost:8080/api/v1/uetr/process-external" \
  -d "uetr=20241201-EXT01-P008-A1B2-550E8400E29B41D4&messageType=PACS008&tenantId=tenant-123&source=CLEARING_SYSTEM"
```

### Monitoring Queries

```sql
-- External UETR usage
SELECT 
    CASE 
        WHEN uetr LIKE '%-PE01-%' THEN 'Internal'
        ELSE 'External'
    END as uetr_source,
    COUNT(*) as count
FROM uetr_tracking_records 
WHERE created_at >= NOW() - INTERVAL '1 day'
GROUP BY uetr_source;

-- UETR validation failures
SELECT uetr, status_reason, created_at 
FROM uetr_tracking_records 
WHERE status = 'FAILED' 
AND status_reason LIKE '%invalid%'
AND created_at >= NOW() - INTERVAL '1 day'
ORDER BY created_at DESC;

-- External UETR processing time
SELECT 
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at))) as avg_processing_seconds
FROM uetr_tracking_records 
WHERE uetr NOT LIKE '%-PE01-%'
AND status = 'COMPLETED'
AND created_at >= NOW() - INTERVAL '1 day';
```

## Conclusion

The external UETR handling capability provides:

- **Seamless Integration**: Works with external systems that provide UETRs
- **Flexible Processing**: Accepts external UETRs or generates new ones
- **Comprehensive Tracking**: Tracks UETR source and processing history
- **Robust Validation**: Validates external UETRs before use
- **Enhanced Monitoring**: Provides detailed insights into UETR usage

This implementation ensures that the Payment Engine can work with any external system, whether they provide UETRs or not, while maintaining complete end-to-end transaction tracking and audit capabilities.