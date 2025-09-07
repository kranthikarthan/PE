# Transaction Repair System Implementation

## Overview

This document describes the comprehensive implementation of a **Transaction Repair System** that handles debit/credit API orchestration with failure handling, timeout management, and corrective actions. The system provides the ability to call debit APIs and, if successful, subsequently call credit APIs hosted on the bank's own core banking system. If debit fails, credit processing is stopped. If debit times out, transactions are marked for review later. The React frontend shows these transactions that need repair and allows applying corrective actions when needed.

## Key Features Implemented

### ✅ **Debit-Credit Orchestration**
- **Sequential Processing**: Debit operation must succeed before credit operation is attempted
- **Failure Handling**: If debit fails, credit processing is automatically stopped
- **Timeout Management**: Configurable timeouts for both debit and credit operations
- **Comprehensive Logging**: Detailed logging of all orchestration steps
- **Transaction Tracking**: Complete audit trail of all operations

### ✅ **Transaction Repair Management**
- **Automatic Repair Creation**: Failed transactions are automatically marked for repair
- **Repair Classification**: Different repair types (DEBIT_FAILED, CREDIT_FAILED, TIMEOUT, etc.)
- **Priority Management**: Configurable priority levels for repair processing
- **Assignment System**: Repairs can be assigned to specific users
- **Status Tracking**: Complete status lifecycle management

### ✅ **Corrective Action Engine**
- **Retry Operations**: Retry debit, credit, or both operations
- **Reversal Operations**: Reverse successful operations when needed
- **Manual Processing**: Manual debit/credit processing capabilities
- **Escalation**: Escalate complex cases for higher-level review
- **Cancellation**: Cancel transactions when appropriate

### ✅ **Timeout and Failure Handling**
- **Configurable Timeouts**: Per-operation timeout configuration
- **Exponential Backoff**: Intelligent retry with exponential backoff
- **Timeout Detection**: Automatic detection and handling of timeouts
- **Manual Review**: Timeout cases marked for manual review
- **Scheduled Processing**: Automated retry and timeout handling

### ✅ **React Frontend Management Interface**
- **Transaction Repair Dashboard**: Comprehensive view of all repairs
- **Statistics and Analytics**: Real-time statistics and metrics
- **Filtering and Search**: Advanced filtering and search capabilities
- **Corrective Action Interface**: Visual interface for applying corrective actions
- **Assignment Management**: User assignment and tracking
- **Detailed Views**: Complete transaction and repair details

## Architecture Components

### 1. Database Schema

#### Transaction Repairs Table
```sql
CREATE TABLE payment_engine.transaction_repairs (
    id UUID PRIMARY KEY,
    transaction_reference VARCHAR(100) NOT NULL,
    parent_transaction_id VARCHAR(100),
    tenant_id VARCHAR(50) NOT NULL,
    repair_type VARCHAR(50) NOT NULL, -- DEBIT_FAILED, CREDIT_FAILED, DEBIT_TIMEOUT, etc.
    repair_status VARCHAR(50) NOT NULL, -- PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, etc.
    failure_reason VARCHAR(500),
    error_code VARCHAR(50),
    error_message VARCHAR(1000),
    from_account_number VARCHAR(50),
    to_account_number VARCHAR(50),
    amount DECIMAL(15,2),
    currency VARCHAR(3),
    payment_type VARCHAR(50),
    debit_status VARCHAR(50), -- PENDING, SUCCESS, FAILED, TIMEOUT, CANCELLED
    credit_status VARCHAR(50), -- PENDING, SUCCESS, FAILED, TIMEOUT, CANCELLED
    debit_reference VARCHAR(100),
    credit_reference VARCHAR(100),
    debit_response JSONB,
    credit_response JSONB,
    original_request JSONB,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    timeout_at TIMESTAMP,
    priority INTEGER DEFAULT 1,
    assigned_to VARCHAR(100),
    corrective_action VARCHAR(50), -- RETRY_DEBIT, RETRY_CREDIT, etc.
    corrective_action_details JSONB,
    resolution_notes VARCHAR(2000),
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### 2. Backend Services

#### Debit-Credit Orchestration Service
**File**: `services/middleware/src/main/java/com/paymentengine/middleware/service/DebitCreditOrchestrationService.java`

**Features**:
- Orchestrates debit and credit operations sequentially
- Handles failures and timeouts gracefully
- Creates repair records for failed operations
- Supports configurable timeouts and retry policies
- Comprehensive error handling and logging

**Key Methods**:
```java
public CompletableFuture<OrchestrationResult> processDebitCreditTransaction(DebitCreditTransactionRequest request)
private DebitResult processDebit(DebitCreditTransactionRequest request)
private CreditResult processCredit(DebitCreditTransactionRequest request)
private void handleDebitFailure(DebitCreditTransactionRequest request, DebitResult debitResult)
private void handleCreditFailure(DebitCreditTransactionRequest request, DebitResult debitResult, CreditResult creditResult)
```

#### Transaction Repair Service
**File**: `services/middleware/src/main/java/com/paymentengine/middleware/service/TransactionRepairService.java`

**Features**:
- Manages transaction repair lifecycle
- Applies corrective actions
- Handles retry logic with exponential backoff
- Provides comprehensive repair statistics
- Scheduled processing for retries and timeouts

**Key Methods**:
```java
public CompletableFuture<CorrectiveActionResult> applyCorrectiveAction(UUID id, CorrectiveAction correctiveAction, Map<String, Object> details, String appliedBy)
public TransactionRepair assignTransactionRepair(UUID id, String assignedTo)
public TransactionRepair resolveTransactionRepair(UUID id, String resolvedBy, String resolutionNotes)
public Map<String, Object> getTransactionRepairStatistics(String tenantId)
@Scheduled(fixedDelay = 60000) public void processRetryOperations()
@Scheduled(fixedDelay = 300000) public void handleTimeouts()
```

### 3. Frontend Components

#### Transaction Repair Management Component
**File**: `frontend/src/components/TransactionRepairManagement.tsx`

**Features**:
- Comprehensive dashboard with statistics
- Advanced filtering and search capabilities
- Corrective action application interface
- Detailed transaction and repair views
- Real-time status updates
- User assignment management

**Key Sections**:
- **Statistics Dashboard**: Real-time metrics and KPIs
- **Repairs List**: Filterable and searchable repairs table
- **Corrective Action Dialog**: Visual interface for applying actions
- **Details Dialog**: Complete transaction and repair information
- **Assignment Management**: User assignment and tracking

## Usage Scenarios

### 1. Normal Debit-Credit Flow

```java
// Create debit-credit transaction request
DebitCreditTransactionRequest request = new DebitCreditTransactionRequest();
request.setTransactionReference("TXN-001");
request.setFromAccountNumber("ACC-001");
request.setToAccountNumber("ACC-002");
request.setAmount(new BigDecimal("1000.00"));
request.setCurrency("USD");
request.setTenantId("tenant1");
request.setDebitTimeoutSeconds(60);
request.setCreditTimeoutSeconds(60);

// Process transaction
CompletableFuture<OrchestrationResult> result = orchestrationService.processDebitCreditTransaction(request);

result.thenAccept(orchestrationResult -> {
    if (orchestrationResult.getStatus() == OrchestrationStatus.SUCCESS) {
        // Both debit and credit successful
        logger.info("Transaction completed successfully");
    } else if (orchestrationResult.getStatus() == OrchestrationStatus.PARTIAL_SUCCESS) {
        // Debit successful, credit failed - repair created
        logger.warn("Credit failed after successful debit - repair created");
    } else {
        // Debit failed - credit stopped, repair created
        logger.error("Debit failed - credit processing stopped");
    }
});
```

### 2. Debit Failure Handling

When debit fails:
1. **Credit Processing Stopped**: Credit operation is not attempted
2. **Repair Record Created**: Transaction marked for repair with type `DEBIT_FAILED`
3. **Error Details Captured**: Complete error information stored
4. **Notification Sent**: Appropriate notifications sent to stakeholders
5. **Corrective Actions Available**: Various corrective actions can be applied

### 3. Credit Failure Handling

When debit succeeds but credit fails:
1. **Debit Success Recorded**: Debit operation marked as successful
2. **Credit Failure Captured**: Credit failure details stored
3. **Repair Record Created**: Transaction marked for repair with type `CREDIT_FAILED`
4. **Partial Success Status**: Orchestration marked as partial success
5. **Corrective Actions Available**: Focus on credit retry or reversal

### 4. Timeout Handling

When operations timeout:
1. **Timeout Detection**: Automatic timeout detection
2. **Repair Creation**: Transaction marked for repair with timeout type
3. **Manual Review**: Timeout cases marked for manual review
4. **Retry Scheduling**: Automatic retry with exponential backoff
5. **Escalation**: High-priority timeout cases escalated

### 5. Corrective Action Application

```java
// Apply corrective action
Map<String, Object> actionDetails = Map.of(
    "retryReason", "Credit timeout - retrying with extended timeout",
    "timeoutSeconds", 120
);

CompletableFuture<CorrectiveActionResult> result = repairService.applyCorrectiveAction(
    repairId, 
    CorrectiveAction.RETRY_CREDIT, 
    actionDetails, 
    "admin@example.com"
);

result.thenAccept(actionResult -> {
    if (actionResult.isSuccess()) {
        logger.info("Corrective action applied successfully");
    } else {
        logger.error("Corrective action failed: " + actionResult.getMessage());
    }
});
```

## Corrective Actions

### 1. Retry Operations
- **RETRY_DEBIT**: Retry the failed debit operation
- **RETRY_CREDIT**: Retry the failed credit operation
- **RETRY_BOTH**: Retry both debit and credit operations

### 2. Reversal Operations
- **REVERSE_DEBIT**: Reverse the successful debit operation
- **REVERSE_CREDIT**: Reverse the successful credit operation
- **REVERSE_BOTH**: Reverse both debit and credit operations

### 3. Manual Processing
- **MANUAL_CREDIT**: Process credit manually
- **MANUAL_DEBIT**: Process debit manually
- **MANUAL_BOTH**: Process both operations manually

### 4. Administrative Actions
- **CANCEL_TRANSACTION**: Cancel the entire transaction
- **ESCALATE**: Escalate for higher-level review
- **NO_ACTION**: No corrective action needed

## React Frontend Features

### 1. Dashboard Statistics
- **Total Repairs**: Overall repair count
- **Pending Repairs**: Repairs awaiting action
- **High Priority**: Critical repairs requiring immediate attention
- **Resolved Repairs**: Successfully resolved repairs
- **Ready for Retry**: Repairs ready for automatic retry
- **Timed Out**: Repairs that have timed out
- **Needs Manual Review**: Repairs requiring manual intervention

### 2. Advanced Filtering
- **Status Filter**: Filter by repair status (Pending, Assigned, In Progress, etc.)
- **Type Filter**: Filter by repair type (Debit Failed, Credit Failed, Timeout, etc.)
- **Priority Filter**: Filter by priority level (High, Medium, Low)
- **Search**: Search by transaction reference, account numbers, error messages

### 3. Corrective Action Interface
- **Action Selection**: Visual selection of corrective actions
- **Action Details**: JSON editor for action-specific details
- **Resolution Notes**: Text area for resolution documentation
- **Validation**: Form validation with error handling
- **Confirmation**: Confirmation dialogs for critical actions

### 4. Detailed Views
- **Transaction Information**: Complete transaction details
- **Operation Status**: Debit and credit operation status
- **Error Details**: Comprehensive error information
- **Retry Information**: Retry count and next retry time
- **Assignment Information**: User assignment and resolution details

## Scheduled Processing

### 1. Retry Processing (Every Minute)
```java
@Scheduled(fixedDelay = 60000)
@Async
public void processRetryOperations() {
    List<TransactionRepair> readyForRetry = getTransactionRepairsReadyForRetry();
    
    for (TransactionRepair repair : readyForRetry) {
        int delayMinutes = (int) Math.pow(2, repair.getRetryCount()) * 5; // 5, 10, 20, 40 minutes
        repair.markForRetry(delayMinutes);
        transactionRepairRepository.save(repair);
    }
}
```

### 2. Timeout Handling (Every 5 Minutes)
```java
@Scheduled(fixedDelay = 300000)
@Async
public void handleTimeouts() {
    List<TransactionRepair> timedOut = getTimedOutTransactionRepairs();
    
    for (TransactionRepair repair : timedOut) {
        repair.setRepairType(TransactionRepair.RepairType.MANUAL_REVIEW);
        repair.setRepairStatus(TransactionRepair.RepairStatus.PENDING);
        repair.setPriority(8); // High priority
        repair.setFailureReason("Transaction repair timed out");
        transactionRepairRepository.save(repair);
    }
}
```

## Benefits

### 1. **Reliability**
- Comprehensive failure handling
- Automatic retry mechanisms
- Timeout detection and management
- Graceful degradation

### 2. **Transparency**
- Complete audit trail
- Real-time status tracking
- Detailed error information
- Comprehensive logging

### 3. **Flexibility**
- Multiple corrective actions
- Configurable timeouts and retries
- Priority-based processing
- User assignment system

### 4. **Efficiency**
- Automated retry processing
- Scheduled timeout handling
- Priority-based queue management
- Bulk operations support

### 5. **User Experience**
- Intuitive React interface
- Real-time updates
- Advanced filtering and search
- Comprehensive statistics

### 6. **Compliance**
- Complete audit trail
- Resolution documentation
- User tracking
- Time-stamped operations

## Security Considerations

1. **Access Control**: Role-based access to repair management
2. **Audit Logging**: Complete audit trail of all actions
3. **Data Protection**: Sensitive data encryption and protection
4. **User Authentication**: Secure user authentication and authorization
5. **Action Validation**: Validation of all corrective actions
6. **Approval Workflows**: Multi-level approval for critical actions

## Monitoring and Alerting

1. **Repair Metrics**: Track repair creation and resolution rates
2. **Performance Monitoring**: Monitor orchestration performance
3. **Error Tracking**: Track and alert on error patterns
4. **Timeout Monitoring**: Monitor timeout rates and patterns
5. **User Activity**: Track user actions and assignments
6. **System Health**: Monitor system health and performance

## Future Enhancements

1. **Machine Learning**: AI-powered repair recommendations
2. **Advanced Analytics**: Predictive analytics for failure patterns
3. **Workflow Automation**: Automated workflow for common repairs
4. **Integration**: Integration with external monitoring systems
5. **Mobile Support**: Mobile interface for repair management
6. **API Enhancements**: RESTful API for external integrations

## Conclusion

The Transaction Repair System provides a comprehensive solution for managing debit/credit API orchestration with robust failure handling, timeout management, and corrective actions. It ensures that debit operations must succeed before credit operations are attempted, automatically stops credit processing when debit fails, and marks timeout cases for review. The React frontend provides an intuitive interface for managing repairs and applying corrective actions, while the backend services handle the complex orchestration logic with comprehensive error handling and automated processing.

The system is designed for enterprise-scale deployments with multi-tenant support, comprehensive security, extensive monitoring, and automated processing capabilities. It represents a significant advancement in transaction processing reliability and provides the tools necessary for effective transaction repair management.