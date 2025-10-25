# Database Impact Analysis: pain.002 Implementation

## Executive Summary

This document analyzes the database impact of implementing ISO 20022 pain.002 (Payment Status Report) support, including new tables, schema changes, and data migration requirements. pain.002 messages provide status updates on previously submitted payment instructions (pain.001).

**Critical Finding**: pain.002 implementation requires significant database schema changes to support status report storage, message correlation, and status code mapping.

---

## Current Database Schema Analysis

### Existing Payment Tables (V2, V16)

```sql
-- Current payment tables
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    payment_id VARCHAR(255) UNIQUE NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    business_unit_id VARCHAR(255) NOT NULL,
    source_account VARCHAR(11) NOT NULL,
    destination_account VARCHAR(11) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference VARCHAR(35),
    payment_type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    initiated_by VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Current Limitations for pain.002 Support

1. **Missing Status Report Storage**: No tables to store pain.002 status reports
2. **Missing Message Correlation**: No way to link pain.002 to original pain.001
3. **Missing Status Code Mapping**: No ISO 20022 status code storage
4. **Missing Charge Information**: No fee/charge reporting storage
5. **Missing Status History**: No historical status tracking

---

## Required Database Schema Changes

### 1. New pain.002 Status Report Tables

#### Table: `pain002_status_reports`
```sql
CREATE TABLE pain002_status_reports (
    id UUID PRIMARY KEY,
    message_id VARCHAR(35) UNIQUE NOT NULL,        -- ISO 20022 MsgId
    creation_date_time TIMESTAMP NOT NULL,         -- ISO 20022 CreDtTm
    original_message_id VARCHAR(35) NOT NULL,       -- ISO 20022 OrgnlMsgId
    original_message_name_id VARCHAR(35),          -- ISO 20022 OrgnlMsgNmId
    original_creation_date_time TIMESTAMP,        -- ISO 20022 OrgnlCreDtTm
    group_status VARCHAR(10) NOT NULL,            -- ISO 20022 GrpSts
    message_format VARCHAR(10) NOT NULL,           -- 'XML' or 'JSON'
    raw_message TEXT NOT NULL,                     -- Full pain.002 message
    parsed_message JSONB,                         -- Parsed message structure
    validation_status VARCHAR(20) NOT NULL,       -- 'PENDING', 'VALID', 'INVALID'
    validation_errors JSONB,                      -- Validation error details
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_message_format CHECK (message_format IN ('XML', 'JSON')),
    CONSTRAINT chk_validation_status CHECK (validation_status IN ('PENDING', 'VALID', 'INVALID')),
    CONSTRAINT chk_group_status CHECK (group_status IN ('ACCP', 'RJCT', 'PDNG'))
);
```

#### Table: `pain002_transaction_status`
```sql
CREATE TABLE pain002_transaction_status (
    id UUID PRIMARY KEY,
    pain002_report_id UUID NOT NULL REFERENCES pain002_status_reports(id),
    status_id VARCHAR(35) NOT NULL,                -- ISO 20022 StsId
    original_instruction_id VARCHAR(35),           -- ISO 20022 OrgnlInstrId
    original_end_to_end_id VARCHAR(35),            -- ISO 20022 OrgnlEndToEndId
    transaction_status VARCHAR(10) NOT NULL,       -- ISO 20022 TxSts
    status_reason_code VARCHAR(10),                -- ISO 20022 StsRsnInf.Rsn.Cd
    additional_information VARCHAR(500),           -- ISO 20022 StsRsnInf.AddtlInf
    charges_amount DECIMAL(19,2),                 -- ISO 20022 ChrgsInf.Amt.InstdAmt
    charges_currency VARCHAR(3),                  -- ISO 20022 ChrgsInf.Amt.InstdAmt.Ccy
    charges_agent_bic VARCHAR(11),                -- ISO 20022 ChrgsInf.Agt.BICFI
    charges_agent_name VARCHAR(140),              -- ISO 20022 ChrgsInf.Agt.Nm
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_transaction_status CHECK (transaction_status IN ('ACSC', 'RJCT', 'PDNG', 'CANC', 'PART'))
);
```

#### Table: `pain001_pain002_correlation`
```sql
CREATE TABLE pain001_pain002_correlation (
    id UUID PRIMARY KEY,
    pain001_message_id VARCHAR(35) NOT NULL,      -- Original pain.001 message ID
    pain002_message_id VARCHAR(35) NOT NULL,      -- Generated pain.002 message ID
    original_instruction_id VARCHAR(35) NOT NULL, -- ISO 20022 OrgnlInstrId
    original_end_to_end_id VARCHAR(35) NOT NULL, -- ISO 20022 OrgnlEndToEndId
    status_report_id VARCHAR(35) NOT NULL,        -- ISO 20022 StsId
    correlation_created_at TIMESTAMP NOT NULL,    -- When correlation was created
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT uk_pain001_pain002_correlation UNIQUE (pain001_message_id, pain002_message_id),
    CONSTRAINT uk_original_instruction UNIQUE (original_instruction_id),
    CONSTRAINT uk_original_end_to_end UNIQUE (original_end_to_end_id)
);
```

### 2. Status Code Mapping Tables

#### Table: `iso20022_status_codes`
```sql
CREATE TABLE iso20022_status_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,              -- ISO 20022 status code
    description VARCHAR(255) NOT NULL,             -- Human-readable description
    category VARCHAR(20) NOT NULL,                 -- 'GROUP', 'TRANSACTION'
    is_active BOOLEAN DEFAULT TRUE,                -- Whether code is currently active
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_category CHECK (category IN ('GROUP', 'TRANSACTION'))
);

-- Insert standard ISO 20022 status codes
INSERT INTO iso20022_status_codes (id, code, description, category) VALUES
(UUID(), 'ACCP', 'Accepted', 'GROUP'),
(UUID(), 'RJCT', 'Rejected', 'GROUP'),
(UUID(), 'PDNG', 'Pending', 'GROUP'),
(UUID(), 'ACSC', 'Accepted Settlement Completed', 'TRANSACTION'),
(UUID(), 'RJCT', 'Rejected', 'TRANSACTION'),
(UUID(), 'PDNG', 'Pending', 'TRANSACTION'),
(UUID(), 'CANC', 'Cancelled', 'TRANSACTION'),
(UUID(), 'PART', 'Partially Accepted', 'TRANSACTION');
```

#### Table: `internal_status_mapping`
```sql
CREATE TABLE internal_status_mapping (
    id UUID PRIMARY KEY,
    internal_status VARCHAR(50) NOT NULL,          -- Internal payment status
    iso20022_group_status VARCHAR(10),            -- ISO 20022 group status
    iso20022_transaction_status VARCHAR(10),      -- ISO 20022 transaction status
    status_reason_code VARCHAR(10),               -- ISO 20022 reason code
    additional_information VARCHAR(500),          -- Additional status information
    is_active BOOLEAN DEFAULT TRUE,               -- Whether mapping is active
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT uk_internal_status UNIQUE (internal_status),
    CONSTRAINT fk_iso20022_group_status FOREIGN KEY (iso20022_group_status) REFERENCES iso20022_status_codes(code),
    CONSTRAINT fk_iso20022_transaction_status FOREIGN KEY (iso20022_transaction_status) REFERENCES iso20022_status_codes(code)
);

-- Insert standard status mappings
INSERT INTO internal_status_mapping (id, internal_status, iso20022_group_status, iso20022_transaction_status, status_reason_code) VALUES
(UUID(), 'COMPLETED', 'ACCP', 'ACSC', 'NARR'),
(UUID(), 'FAILED', 'RJCT', 'RJCT', 'NARR'),
(UUID(), 'PROCESSING', 'PDNG', 'PDNG', 'NARR'),
(UUID(), 'CANCELLED', 'RJCT', 'CANC', 'NARR'),
(UUID(), 'PARTIALLY_COMPLETED', 'ACCP', 'PART', 'NARR');
```

### 3. Enhanced Existing Tables

#### Updated Table: `payments`
```sql
-- Add pain.002 correlation fields
ALTER TABLE payments ADD COLUMN pain002_message_id VARCHAR(35);
ALTER TABLE payments ADD COLUMN pain002_correlation_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN iso20022_status_code VARCHAR(10);
ALTER TABLE payments ADD COLUMN status_reason_code VARCHAR(10);
ALTER TABLE payments ADD COLUMN status_additional_info VARCHAR(500);
ALTER TABLE payments ADD COLUMN charges_amount DECIMAL(19,2);
ALTER TABLE payments ADD COLUMN charges_currency VARCHAR(3);
ALTER TABLE payments ADD COLUMN charges_agent_bic VARCHAR(11);

-- Add foreign key constraints
ALTER TABLE payments ADD CONSTRAINT fk_payments_iso20022_status 
    FOREIGN KEY (iso20022_status_code) REFERENCES iso20022_status_codes(code);

-- Add indexes for performance
CREATE INDEX idx_payments_pain002_message_id ON payments(pain002_message_id);
CREATE INDEX idx_payments_iso20022_status_code ON payments(iso20022_status_code);
CREATE INDEX idx_payments_status_reason_code ON payments(status_reason_code);
```

### 4. Status History and Audit Tables

#### Table: `payment_status_history`
```sql
CREATE TABLE payment_status_history (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id),
    previous_status VARCHAR(50),                   -- Previous payment status
    current_status VARCHAR(50) NOT NULL,          -- Current payment status
    iso20022_status_code VARCHAR(10),            -- ISO 20022 status code
    status_reason_code VARCHAR(10),               -- Reason for status change
    additional_information VARCHAR(500),          -- Additional status information
    changed_by VARCHAR(255),                     -- User or system that changed status
    changed_at TIMESTAMP NOT NULL,                -- When status was changed
    correlation_id VARCHAR(255),                 -- For tracing
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_payment_status_history_iso20022 
        FOREIGN KEY (iso20022_status_code) REFERENCES iso20022_status_codes(code)
);
```

#### Table: `pain002_audit_log`
```sql
CREATE TABLE pain002_audit_log (
    id UUID PRIMARY KEY,
    pain002_report_id UUID NOT NULL REFERENCES pain002_status_reports(id),
    action VARCHAR(50) NOT NULL,                  -- 'GENERATED', 'SENT', 'DELIVERED', 'FAILED'
    action_details JSONB,                        -- Action-specific details
    performed_by VARCHAR(255),                   -- User or system that performed action
    performed_at TIMESTAMP NOT NULL,             -- When action was performed
    correlation_id VARCHAR(255),                  -- For tracing
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_action CHECK (action IN ('GENERATED', 'SENT', 'DELIVERED', 'FAILED'))
);
```

---

## Java Class vs Database Alignment Rules

### 1. pain.002 Entity Classes

#### Pain002StatusReport Entity
```java
@Entity
@Table(name = "pain002_status_reports", 
       indexes = {
           @Index(name = "idx_pain002_msg_id", columnList = "message_id"),
           @Index(name = "idx_pain002_original_msg_id", columnList = "original_message_id"),
           @Index(name = "idx_pain002_group_status", columnList = "group_status"),
           @Index(name = "idx_pain002_creation_dt", columnList = "creation_date_time")
       })
public class Pain002StatusReport {
    
    @Id
    private UUID id;
    
    @Column(name = "message_id", length = 35, nullable = false, unique = true)
    @NotBlank(message = "Message ID is required")
    @Size(max = 35, message = "Message ID must not exceed 35 characters")
    private String messageId;
    
    @Column(name = "creation_date_time", nullable = false)
    @NotNull(message = "Creation date time is required")
    private Instant creationDateTime;
    
    @Column(name = "original_message_id", length = 35, nullable = false)
    @NotBlank(message = "Original message ID is required")
    @Size(max = 35, message = "Original message ID must not exceed 35 characters")
    private String originalMessageId;
    
    @Column(name = "original_message_name_id", length = 35)
    @Size(max = 35, message = "Original message name ID must not exceed 35 characters")
    private String originalMessageNameId;
    
    @Column(name = "original_creation_date_time")
    private Instant originalCreationDateTime;
    
    @Column(name = "group_status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Group status is required")
    private Iso20022GroupStatus groupStatus;
    
    @Column(name = "message_format", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Message format is required")
    private MessageFormat messageFormat;
    
    @Column(name = "raw_message", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Raw message is required")
    private String rawMessage;
    
    @Column(name = "parsed_message", columnDefinition = "JSONB")
    private String parsedMessage;
    
    @Column(name = "validation_status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Validation status is required")
    private ValidationStatus validationStatus;
    
    @Column(name = "validation_errors", columnDefinition = "JSONB")
    private String validationErrors;
    
    @OneToMany(mappedBy = "pain002StatusReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pain002TransactionStatus> transactionStatuses;
    
    @OneToMany(mappedBy = "pain002StatusReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pain002AuditLog> auditLogs;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

#### Pain002TransactionStatus Entity
```java
@Entity
@Table(name = "pain002_transaction_status",
       indexes = {
           @Index(name = "idx_pain002_tx_status_id", columnList = "status_id"),
           @Index(name = "idx_pain002_tx_original_instr_id", columnList = "original_instruction_id"),
           @Index(name = "idx_pain002_tx_original_e2e_id", columnList = "original_end_to_end_id"),
           @Index(name = "idx_pain002_tx_status", columnList = "transaction_status")
       })
public class Pain002TransactionStatus {
    
    @Id
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pain002_report_id", nullable = false)
    @NotNull(message = "Pain002 status report is required")
    private Pain002StatusReport pain002StatusReport;
    
    @Column(name = "status_id", length = 35, nullable = false)
    @NotBlank(message = "Status ID is required")
    @Size(max = 35, message = "Status ID must not exceed 35 characters")
    private String statusId;
    
    @Column(name = "original_instruction_id", length = 35)
    @Size(max = 35, message = "Original instruction ID must not exceed 35 characters")
    private String originalInstructionId;
    
    @Column(name = "original_end_to_end_id", length = 35)
    @Size(max = 35, message = "Original end to end ID must not exceed 35 characters")
    private String originalEndToEndId;
    
    @Column(name = "transaction_status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction status is required")
    private Iso20022TransactionStatus transactionStatus;
    
    @Column(name = "status_reason_code", length = 10)
    @Size(max = 10, message = "Status reason code must not exceed 10 characters")
    private String statusReasonCode;
    
    @Column(name = "additional_information", length = 500)
    @Size(max = 500, message = "Additional information must not exceed 500 characters")
    private String additionalInformation;
    
    @Column(name = "charges_amount", precision = 19, scale = 2)
    @DecimalMax(value = "9999999999999999999.99", message = "Charges amount exceeds maximum value")
    @DecimalMin(value = "0.00", message = "Charges amount must be positive")
    private BigDecimal chargesAmount;
    
    @Column(name = "charges_currency", length = 3)
    @Size(max = 3, message = "Charges currency must not exceed 3 characters")
    private String chargesCurrency;
    
    @Column(name = "charges_agent_bic", length = 11)
    @Size(max = 11, message = "Charges agent BIC must not exceed 11 characters")
    private String chargesAgentBic;
    
    @Column(name = "charges_agent_name", length = 140)
    @Size(max = 140, message = "Charges agent name must not exceed 140 characters")
    private String chargesAgentName;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

#### Pain001Pain002Correlation Entity
```java
@Entity
@Table(name = "pain001_pain002_correlation",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_pain001_pain002_correlation", 
                           columnNames = {"pain001_message_id", "pain002_message_id"}),
           @UniqueConstraint(name = "uk_original_instruction", 
                           columnNames = {"original_instruction_id"}),
           @UniqueConstraint(name = "uk_original_end_to_end", 
                           columnNames = {"original_end_to_end_id"})
       })
public class Pain001Pain002Correlation {
    
    @Id
    private UUID id;
    
    @Column(name = "pain001_message_id", length = 35, nullable = false)
    @NotBlank(message = "Pain001 message ID is required")
    @Size(max = 35, message = "Pain001 message ID must not exceed 35 characters")
    private String pain001MessageId;
    
    @Column(name = "pain002_message_id", length = 35, nullable = false)
    @NotBlank(message = "Pain002 message ID is required")
    @Size(max = 35, message = "Pain002 message ID must not exceed 35 characters")
    private String pain002MessageId;
    
    @Column(name = "original_instruction_id", length = 35, nullable = false)
    @NotBlank(message = "Original instruction ID is required")
    @Size(max = 35, message = "Original instruction ID must not exceed 35 characters")
    private String originalInstructionId;
    
    @Column(name = "original_end_to_end_id", length = 35, nullable = false)
    @NotBlank(message = "Original end to end ID is required")
    @Size(max = 35, message = "Original end to end ID must not exceed 35 characters")
    private String originalEndToEndId;
    
    @Column(name = "status_report_id", length = 35, nullable = false)
    @NotBlank(message = "Status report ID is required")
    @Size(max = 35, message = "Status report ID must not exceed 35 characters")
    private String statusReportId;
    
    @Column(name = "correlation_created_at", nullable = false)
    @NotNull(message = "Correlation created at is required")
    private Instant correlationCreatedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 2. Status Code Mapping Entities

#### Iso20022StatusCode Entity
```java
@Entity
@Table(name = "iso20022_status_codes",
       indexes = {
           @Index(name = "idx_iso20022_status_code", columnList = "code"),
           @Index(name = "idx_iso20022_status_category", columnList = "category"),
           @Index(name = "idx_iso20022_status_active", columnList = "is_active")
       })
public class Iso20022StatusCode {
    
    @Id
    private UUID id;
    
    @Column(name = "code", length = 10, nullable = false, unique = true)
    @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    private String code;
    
    @Column(name = "description", length = 255, nullable = false)
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @Column(name = "category", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    private StatusCodeCategory category;
    
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Is active is required")
    private Boolean isActive;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

#### InternalStatusMapping Entity
```java
@Entity
@Table(name = "internal_status_mapping",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_internal_status", columnNames = {"internal_status"})
       })
public class InternalStatusMapping {
    
    @Id
    private UUID id;
    
    @Column(name = "internal_status", length = 50, nullable = false, unique = true)
    @NotBlank(message = "Internal status is required")
    @Size(max = 50, message = "Internal status must not exceed 50 characters")
    private String internalStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iso20022_group_status")
    private Iso20022StatusCode iso20022GroupStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iso20022_transaction_status")
    private Iso20022StatusCode iso20022TransactionStatus;
    
    @Column(name = "status_reason_code", length = 10)
    @Size(max = 10, message = "Status reason code must not exceed 10 characters")
    private String statusReasonCode;
    
    @Column(name = "additional_information", length = 500)
    @Size(max = 500, message = "Additional information must not exceed 500 characters")
    private String additionalInformation;
    
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Is active is required")
    private Boolean isActive;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

---

## Migration Strategy

### Phase 1: Create pain.002 Tables (Week 1)
```sql
-- Migration V20__Create_pain002_tables.sql
-- 1. Create pain002_status_reports table
-- 2. Create pain002_transaction_status table
-- 3. Create pain001_pain002_correlation table
-- 4. Create iso20022_status_codes table
-- 5. Create internal_status_mapping table
-- 6. Create payment_status_history table
-- 7. Create pain002_audit_log table
```

### Phase 2: Enhance Existing Tables (Week 2)
```sql
-- Migration V21__Enhance_payments_table_for_pain002.sql
-- 1. Add pain002_message_id column
-- 2. Add pain002_correlation_id column
-- 3. Add iso20022_status_code column
-- 4. Add status_reason_code column
-- 5. Add status_additional_info column
-- 6. Add charges_amount column
-- 7. Add charges_currency column
-- 8. Add charges_agent_bic column
-- 9. Create indexes
-- 10. Add foreign key constraints
```

### Phase 3: Data Migration (Week 3)
```sql
-- Migration V22__Migrate_existing_payments_to_pain002.sql
-- 1. Insert standard ISO 20022 status codes
-- 2. Insert standard status mappings
-- 3. Update existing payments with default values
-- 4. Create status history records for existing payments
-- 5. Create audit records for existing payments
```

---

## Performance Considerations

### 1. Indexing Strategy
- **Primary Keys**: UUID with proper clustering
- **Foreign Keys**: Indexed for join performance
- **Status Queries**: Indexed based on common query patterns
- **Correlation Queries**: Composite indexes for pain.001 to pain.002 lookups
- **Time-based Queries**: Indexed for status history queries

### 2. Partitioning Strategy
- **Time-based Partitioning**: For pain002_status_reports table
- **Status-based Partitioning**: For payment_status_history table
- **Tenant-based Partitioning**: For multi-tenant data

### 3. Archival Strategy
- **Retention Policy**: 7 years for status reports and audit data
- **Archival Process**: Move old data to archive tables
- **Cleanup Process**: Remove expired data

---

## Monitoring and Alerting

### 1. Database Metrics
- **Table Size Growth**: Monitor pain002_status_reports table growth
- **Index Usage**: Monitor index efficiency
- **Query Performance**: Monitor slow queries
- **Connection Pool**: Monitor database connections

### 2. Data Quality Metrics
- **Status Report Generation Rate**: Monitor status report generation
- **Correlation Success Rate**: Monitor pain.001 to pain.002 correlation
- **Status Code Mapping Accuracy**: Monitor status code mapping
- **Data Completeness**: Monitor required field population

---

## Conclusion

The pain.002 implementation requires significant database schema changes to support status report storage, message correlation, and status code mapping. The Java class vs database alignment rules ensure that issues are caught at design time rather than runtime.

**Key Recommendations**:
1. **Implement alignment rules** to prevent runtime issues
2. **Create comprehensive migration strategy** for schema changes
3. **Add proper indexing** for performance
4. **Implement monitoring** for data quality and performance
5. **Plan for data archival** and retention

**Estimated Database Effort**: 2-3 weeks for complete schema implementation

---

**Document Version**: 1.0  
**Created**: 2025-01-27  
**Last Updated**: 2025-01-27  
**Status**: P0-Critical - Required for pain.002 implementation
