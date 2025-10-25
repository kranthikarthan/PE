# Java Class vs Database Alignment Rules

## Executive Summary

This document establishes comprehensive rules for ensuring Java class and database schema alignment to catch issues at design time rather than runtime. These rules prevent common mismatches that lead to runtime errors, data corruption, and performance issues.

**Purpose**: Prevent Java-Database misalignment issues at design time  
**Scope**: All entity classes, database schemas, and migrations  
**Enforcement**: Code review, automated testing, and CI/CD validation

---

## Rule Categories

### 1. Naming Convention Alignment

#### Rule 1.1: Table Names
```java
// ✅ CORRECT - Exact match
@Entity
@Table(name = "pain001_messages")
public class Pain001Message {
    // ...
}

// ❌ WRONG - Mismatched naming
@Entity
@Table(name = "pain_001_messages")  // Different convention
public class Pain001Message {
    // ...
}
```

**Enforcement**:
- Table names must match exactly between `@Table(name = "...")` and database schema
- Use consistent naming convention (snake_case for database, PascalCase for Java)
- Validate in unit tests: `assertThat(entityClass.getAnnotation(Table.class).name()).isEqualTo("expected_table_name")`

#### Rule 1.2: Column Names
```java
// ✅ CORRECT - Exact match with proper mapping
@Entity
@Table(name = "pain001_messages")
public class Pain001Message {
    
    @Column(name = "message_id")
    private String messageId;
    
    @Column(name = "creation_date_time")
    private Instant creationDateTime;
    
    @Column(name = "number_of_transactions")
    private Integer numberOfTransactions;
}

// ❌ WRONG - Mismatched naming
@Entity
public class Pain001Message {
    @Column(name = "messageId")  // Wrong naming convention
    private String messageId;
    
    @Column(name = "creationDateTime")  // Wrong naming convention
    private Instant creationDateTime;
}
```

**Enforcement**:
- Column names must match exactly between `@Column(name = "...")` and database schema
- Use snake_case for database columns, camelCase for Java fields
- Validate in unit tests: `assertThat(field.getAnnotation(Column.class).name()).isEqualTo("expected_column_name")`

### 2. Data Type Alignment

#### Rule 2.1: String Length Constraints
```java
// ✅ CORRECT - Length constraints match database
@Entity
@Table(name = "pain001_messages")
public class Pain001Message {
    
    @Column(name = "message_id", length = 35)
    @Size(max = 35, message = "Message ID must not exceed 35 characters")
    private String messageId;
    
    @Column(name = "initiating_party_name", length = 140)
    @Size(max = 140, message = "Initiating party name must not exceed 140 characters")
    private String initiatingPartyName;
}

// ❌ WRONG - Missing or mismatched length constraints
@Entity
public class Pain001Message {
    @Column(name = "message_id")  // Missing length constraint
    private String messageId;
    
    @Column(name = "initiating_party_name", length = 100)  // Wrong length
    private String initiatingPartyName;
}
```

**Enforcement**:
- `@Column(length = X)` must match database `VARCHAR(X)`
- `@Size(max = X)` must match `@Column(length = X)`
- Validate in unit tests: `assertThat(field.getAnnotation(Column.class).length()).isEqualTo(expectedLength)`

#### Rule 2.2: Numeric Precision and Scale
```java
// ✅ CORRECT - Precision and scale match database
@Entity
@Table(name = "pain001_messages")
public class Pain001Message {
    
    @Column(name = "control_sum", precision = 19, scale = 2)
    @DecimalMax(value = "9999999999999999999.99", message = "Control sum exceeds maximum value")
    @DecimalMin(value = "0.00", message = "Control sum must be positive")
    private BigDecimal controlSum;
    
    @Column(name = "number_of_transactions")
    @Min(value = 1, message = "Number of transactions must be at least 1")
    @Max(value = 999999999, message = "Number of transactions exceeds maximum")
    private Integer numberOfTransactions;
}

// ❌ WRONG - Mismatched precision/scale
@Entity
public class Pain001Message {
    @Column(name = "control_sum", precision = 10, scale = 2)  // Wrong precision
    private BigDecimal controlSum;
    
    @Column(name = "number_of_transactions", precision = 5)  // Wrong type for INTEGER
    private Integer numberOfTransactions;
}
```

**Enforcement**:
- `@Column(precision = X, scale = Y)` must match database `DECIMAL(X,Y)`
- `@DecimalMax` and `@DecimalMin` must align with precision/scale
- Validate in unit tests: `assertThat(field.getAnnotation(Column.class).precision()).isEqualTo(expectedPrecision)`

#### Rule 2.3: Boolean Mapping
```java
// ✅ CORRECT - Boolean mapping
@Entity
@Table(name = "pain001_payment_information")
public class Pain001PaymentInformation {
    
    @Column(name = "batch_booking")
    private Boolean batchBooking;
    
    @Column(name = "iso20022_compliant")
    private Boolean iso20022Compliant;
}

// ❌ WRONG - Wrong type for boolean
@Entity
public class Pain001PaymentInformation {
    @Column(name = "batch_booking")
    private String batchBooking;  // Wrong type
}
```

**Enforcement**:
- Boolean fields must use `Boolean` or `boolean` type
- Database `BOOLEAN` maps to Java `Boolean`
- Validate in unit tests: `assertThat(field.getType()).isEqualTo(Boolean.class)`

### 3. Constraint Alignment

#### Rule 3.1: Check Constraints
```java
// ✅ CORRECT - Enum values match check constraints
@Entity
@Table(name = "pain001_messages")
public class Pain001Message {
    
    @Column(name = "message_format")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Message format is required")
    private MessageFormat messageFormat;
    
    @Column(name = "validation_status")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Validation status is required")
    private ValidationStatus validationStatus;
}

// Enum definitions matching database constraints
public enum MessageFormat {
    XML, JSON  // Must match CHECK constraint values
}

public enum ValidationStatus {
    PENDING, VALID, INVALID  // Must match CHECK constraint values
}

// ❌ WRONG - Enum values don't match constraints
public enum MessageFormat {
    XML, JSON, BINARY  // BINARY not in CHECK constraint
}
```

**Enforcement**:
- Enum values must exactly match database CHECK constraint values
- Validate in unit tests: `assertThat(enumClass.getEnumConstants()).containsExactly(expectedValues)`
- Database constraint: `CONSTRAINT chk_message_format CHECK (message_format IN ('XML', 'JSON'))`

#### Rule 3.2: Foreign Key Constraints
```java
// ✅ CORRECT - Proper foreign key mapping
@Entity
@Table(name = "pain001_payment_information")
public class Pain001PaymentInformation {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pain001_message_id", nullable = false)
    @NotNull(message = "Pain001 message is required")
    private Pain001Message pain001Message;
}

// ❌ WRONG - Missing foreign key constraint
@Entity
public class Pain001PaymentInformation {
    @Column(name = "pain001_message_id")
    private String pain001MessageId;  // Should be entity reference
}
```

**Enforcement**:
- Foreign keys must use `@ManyToOne`, `@OneToOne`, etc. with `@JoinColumn`
- `@JoinColumn(name = "...")` must match database foreign key column
- `nullable = false` must match database `NOT NULL` constraint
- Validate in unit tests: `assertThat(field.getAnnotation(JoinColumn.class).name()).isEqualTo("expected_fk_column")`

#### Rule 3.3: Unique Constraints
```java
// ✅ CORRECT - Unique constraint mapping
@Entity
@Table(name = "pain001_messages", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_pain001_msg_id", columnNames = {"message_id"})
       })
public class Pain001Message {
    
    @Column(name = "message_id", unique = true)
    @NotBlank(message = "Message ID is required")
    private String messageId;
}

// ❌ WRONG - Missing unique constraint
@Entity
public class Pain001Message {
    @Column(name = "message_id")  // Missing unique constraint
    private String messageId;
}
```

**Enforcement**:
- `@Column(unique = true)` must match database `UNIQUE` constraint
- `@UniqueConstraint` must match database unique constraint definition
- Validate in unit tests: `assertThat(field.getAnnotation(Column.class).unique()).isTrue()`

### 4. Index Alignment

#### Rule 4.1: Performance Indexes
```java
// ✅ CORRECT - Indexes match database
@Entity
@Table(name = "pain001_messages", 
       indexes = {
           @Index(name = "idx_pain001_msg_id", columnList = "message_id"),
           @Index(name = "idx_pain001_creation_dt", columnList = "creation_date_time"),
           @Index(name = "idx_pain001_validation_status", columnList = "validation_status")
       })
public class Pain001Message {
    
    @Column(name = "message_id")
    private String messageId;
    
    @Column(name = "creation_date_time")
    private Instant creationDateTime;
    
    @Column(name = "validation_status")
    private ValidationStatus validationStatus;
}

// ❌ WRONG - Missing indexes
@Entity
@Table(name = "pain001_messages")  // Missing index definitions
public class Pain001Message {
    // ...
}
```

**Enforcement**:
- `@Index` definitions must match database indexes
- Index names must match exactly
- Column lists must match exactly
- Validate in unit tests: `assertThat(entityClass.getAnnotation(Table.class).indexes()).hasSize(expectedIndexCount)`

### 5. Validation Alignment

#### Rule 5.1: Bean Validation vs Database Constraints
```java
// ✅ CORRECT - Validation annotations match database constraints
@Entity
@Table(name = "pain001_messages")
public class Pain001Message {
    
    @Column(name = "message_id", length = 35, nullable = false, unique = true)
    @NotBlank(message = "Message ID is required")
    @Size(max = 35, message = "Message ID must not exceed 35 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]{1,35}$", message = "Message ID format is invalid")
    private String messageId;
    
    @Column(name = "control_sum", precision = 19, scale = 2, nullable = false)
    @NotNull(message = "Control sum is required")
    @DecimalMax(value = "9999999999999999999.99", message = "Control sum exceeds maximum value")
    @DecimalMin(value = "0.00", message = "Control sum must be positive")
    private BigDecimal controlSum;
}

// ❌ WRONG - Validation doesn't match constraints
@Entity
public class Pain001Message {
    @Column(name = "message_id", length = 35)
    @Size(max = 50, message = "Message ID must not exceed 50 characters")  // Wrong size
    private String messageId;
}
```

**Enforcement**:
- `@Size(max = X)` must match `@Column(length = X)`
- `@DecimalMax` and `@DecimalMin` must align with precision/scale
- `@NotNull` must match `nullable = false`
- Validate in unit tests: `assertThat(field.getAnnotation(Size.class).max()).isEqualTo(field.getAnnotation(Column.class).length())`

### 6. Audit and Timestamp Alignment

#### Rule 6.1: Audit Fields
```java
// ✅ CORRECT - Audit fields with proper annotations
@Entity
@Table(name = "pain001_messages")
@EntityListeners(AuditingEntityListener.class)
public class Pain001Message {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;
    
    @Column(name = "created_by")
    @CreatedBy
    private String createdBy;
    
    @Column(name = "updated_by")
    @LastModifiedBy
    private String updatedBy;
}

// ❌ WRONG - Missing audit annotations
@Entity
public class Pain001Message {
    @Column(name = "created_at")
    private Instant createdAt;  // Missing @CreatedDate
    
    @Column(name = "updated_at")
    private Instant updatedAt;  // Missing @LastModifiedDate
}
```

**Enforcement**:
- Audit fields must have proper JPA annotations (`@CreatedDate`, `@LastModifiedDate`, etc.)
- `updatable = false` for created fields
- `nullable = false` for required audit fields
- Validate in unit tests: `assertThat(field.getAnnotation(CreatedDate.class)).isNotNull()`

---

## Automated Validation Rules

### 1. Unit Test Validation
```java
@Test
void shouldValidateEntityDatabaseAlignment() {
    // Test table name alignment
    Table tableAnnotation = Pain001Message.class.getAnnotation(Table.class);
    assertThat(tableAnnotation.name()).isEqualTo("pain001_messages");
    
    // Test column name alignment
    Field messageIdField = Pain001Message.class.getDeclaredField("messageId");
    Column columnAnnotation = messageIdField.getAnnotation(Column.class);
    assertThat(columnAnnotation.name()).isEqualTo("message_id");
    assertThat(columnAnnotation.length()).isEqualTo(35);
    
    // Test validation alignment
    Size sizeAnnotation = messageIdField.getAnnotation(Size.class);
    assertThat(sizeAnnotation.max()).isEqualTo(columnAnnotation.length());
    
    // Test enum constraint alignment
    Field messageFormatField = Pain001Message.class.getDeclaredField("messageFormat");
    Class<?> enumType = messageFormatField.getType();
    Object[] enumConstants = enumType.getEnumConstants();
    assertThat(enumConstants).containsExactly(MessageFormat.XML, MessageFormat.JSON);
}
```

### 2. Integration Test Validation
```java
@Test
@Transactional
void shouldValidateDatabaseSchemaAlignment() {
    // Test that entity can be persisted
    Pain001Message message = new Pain001Message();
    message.setMessageId("TEST-MSG-001");
    message.setCreationDateTime(Instant.now());
    message.setNumberOfTransactions(1);
    message.setControlSum(new BigDecimal("1000.00"));
    message.setMessageFormat(MessageFormat.XML);
    message.setValidationStatus(ValidationStatus.PENDING);
    
    Pain001Message saved = pain001MessageRepository.save(message);
    assertThat(saved.getId()).isNotNull();
    
    // Test that constraints are enforced
    assertThatThrownBy(() -> {
        Pain001Message invalidMessage = new Pain001Message();
        invalidMessage.setMessageId("A".repeat(36));  // Exceeds length
        pain001MessageRepository.save(invalidMessage);
    }).isInstanceOf(DataIntegrityViolationException.class);
}
```

### 3. Schema Validation Tests
```java
@Test
void shouldValidateSchemaConstraints() {
    // Test that database constraints match entity constraints
    String createTableSql = extractCreateTableSql("pain001_messages");
    
    assertThat(createTableSql).contains("message_id VARCHAR(35) NOT NULL UNIQUE");
    assertThat(createTableSql).contains("control_sum DECIMAL(19,2) NOT NULL");
    assertThat(createTableSql).contains("CONSTRAINT chk_message_format CHECK (message_format IN ('XML', 'JSON'))");
    assertThat(createTableSql).contains("CONSTRAINT chk_validation_status CHECK (validation_status IN ('PENDING', 'VALID', 'INVALID'))");
}
```

---

## CI/CD Validation Pipeline

### 1. Pre-commit Hooks
```bash
#!/bin/bash
# pre-commit hook for Java-Database alignment validation

# Run unit tests for entity alignment
mvn test -Dtest=*EntityAlignmentTest

# Run schema validation tests
mvn test -Dtest=*SchemaValidationTest

# Run integration tests for database alignment
mvn test -Dtest=*DatabaseAlignmentTest

# Fail if any tests fail
if [ $? -ne 0 ]; then
    echo "Java-Database alignment validation failed"
    exit 1
fi
```

### 2. Build Pipeline Validation
```yaml
# .github/workflows/java-database-alignment.yml
name: Java-Database Alignment Validation

on: [push, pull_request]

jobs:
  validate-alignment:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run alignment tests
        run: |
          mvn clean test -Dtest=*EntityAlignmentTest
          mvn clean test -Dtest=*SchemaValidationTest
          mvn clean test -Dtest=*DatabaseAlignmentTest
      
      - name: Generate alignment report
        run: |
          mvn surefire-report:report
          # Generate detailed alignment report
```

### 3. Code Review Checklist
```markdown
## Java-Database Alignment Review Checklist

### Naming Convention Alignment
- [ ] Table names match exactly between `@Table(name = "...")` and database schema
- [ ] Column names match exactly between `@Column(name = "...")` and database schema
- [ ] Consistent naming convention (snake_case for database, camelCase for Java)

### Data Type Alignment
- [ ] String length constraints match between `@Column(length = X)` and `VARCHAR(X)`
- [ ] Numeric precision/scale match between `@Column(precision = X, scale = Y)` and `DECIMAL(X,Y)`
- [ ] Boolean mapping is correct (`BOOLEAN` → `Boolean`)

### Constraint Alignment
- [ ] Check constraints match between enum values and database CHECK constraints
- [ ] Foreign key constraints use proper JPA annotations (`@ManyToOne`, `@JoinColumn`)
- [ ] Unique constraints match between `@Column(unique = true)` and database UNIQUE

### Index Alignment
- [ ] Index definitions match between `@Index` and database indexes
- [ ] Index names match exactly
- [ ] Column lists match exactly

### Validation Alignment
- [ ] Bean validation annotations match database constraints
- [ ] `@Size(max = X)` matches `@Column(length = X)`
- [ ] `@NotNull` matches `nullable = false`

### Audit Alignment
- [ ] Audit fields have proper JPA annotations (`@CreatedDate`, `@LastModifiedDate`)
- [ ] `updatable = false` for created fields
- [ ] `nullable = false` for required audit fields
```

---

## Common Anti-Patterns to Avoid

### 1. Naming Mismatches
```java
// ❌ ANTI-PATTERN - Inconsistent naming
@Entity
@Table(name = "pain_001_messages")  // snake_case
public class Pain001Message {
    @Column(name = "messageId")  // camelCase
    private String messageId;
}

// ✅ CORRECT - Consistent naming
@Entity
@Table(name = "pain001_messages")  // snake_case
public class Pain001Message {
    @Column(name = "message_id")  // snake_case
    private String messageId;
}
```

### 2. Type Mismatches
```java
// ❌ ANTI-PATTERN - Wrong type mapping
@Entity
public class Pain001Message {
    @Column(name = "control_sum")
    private String controlSum;  // Wrong type for DECIMAL
}

// ✅ CORRECT - Proper type mapping
@Entity
public class Pain001Message {
    @Column(name = "control_sum", precision = 19, scale = 2)
    private BigDecimal controlSum;  // Correct type for DECIMAL
}
```

### 3. Constraint Mismatches
```java
// ❌ ANTI-PATTERN - Validation doesn't match constraints
@Entity
public class Pain001Message {
    @Column(name = "message_id", length = 35)
    @Size(max = 50)  // Wrong size
    private String messageId;
}

// ✅ CORRECT - Validation matches constraints
@Entity
public class Pain001Message {
    @Column(name = "message_id", length = 35)
    @Size(max = 35)  // Matches length
    private String messageId;
}
```

---

## Enforcement Tools

### 1. Custom Annotation Processor
```java
@SupportedAnnotationTypes({"javax.persistence.Entity"})
public class DatabaseAlignmentProcessor extends AbstractProcessor {
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            validateEntityAlignment((TypeElement) element);
        }
        return true;
    }
    
    private void validateEntityAlignment(TypeElement entityClass) {
        // Validate table name alignment
        // Validate column name alignment
        // Validate constraint alignment
        // Generate validation report
    }
}
```

### 2. Maven Plugin
```xml
<plugin>
    <groupId>com.payments</groupId>
    <artifactId>database-alignment-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>validate-alignment</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 3. IDE Plugin
```java
// IntelliJ IDEA plugin for real-time validation
public class DatabaseAlignmentInspection extends BaseJavaLocalInspectionTool {
    
    @Override
    public ProblemDescriptor[] checkMethod(@NotNull PsiMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
        // Check entity alignment in real-time
        return new ProblemDescriptor[0];
    }
}
```

---

## Conclusion

These Java class vs database alignment rules ensure that issues are caught at design time rather than runtime, preventing data corruption, performance issues, and runtime errors. The rules are enforced through automated testing, CI/CD validation, and code review processes.

**Key Benefits**:
- **Early Detection**: Catch alignment issues during development
- **Consistency**: Ensure consistent naming and structure
- **Quality**: Prevent runtime errors and data corruption
- **Maintainability**: Easier to maintain and evolve schemas
- **Performance**: Optimize database performance through proper indexing

**Next Steps**:
1. **Implement alignment rules** in existing codebase
2. **Add automated validation** to CI/CD pipeline
3. **Train development team** on alignment rules
4. **Monitor compliance** through metrics and reports

---

**Document Version**: 1.0  
**Created**: 2025-01-27  
**Last Updated**: 2025-01-27  
**Status**: P1-High - Required for pain.001 implementation
