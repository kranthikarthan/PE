# ANTI-PATTERNS TO AVOID

## ❌ Don't Do This - Common Mistakes

### 1. **Skipping Phase Dependency Verification**
```java
// ❌ WRONG - Working on features out of phase order
// Trying to implement Phase 2 features before Phase 0 is complete
// Ignoring the Enhanced Feature Breakdown Tree

// ✅ CORRECT - Always check phase dependencies first
// Read docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md
// Verify all phase dependencies are complete
// Ensure working in correct phase order
```

### 2. **Ignoring the Enhanced Feature Breakdown Tree**
```java
// ❌ WRONG - Not consulting the Enhanced Feature Breakdown Tree
// Working without understanding phase requirements
// Missing AI agent orchestration requirements

// ✅ CORRECT - Always consult the Enhanced Feature Breakdown Tree
// Identify feature's phase (0-7) and dependencies
// Check phase-specific KPIs and requirements
// Follow AI agent orchestration patterns
```

### 3. **Creating New DTOs Without Checking Contracts**
```java
// ❌ WRONG - Creating new DTO without checking
public class PaymentRequestDto {
    private String amount;
    private String currency;
    // ... other fields
}

// ✅ CORRECT - Check contracts/ first
// Look in contracts/ for existing PaymentRequestDto
// Use existing DTOs when possible
```

### 4. **Implementing Features Without Understanding Domain Models**
```java
// ❌ WRONG - Implementing without understanding domain
@Service
public class PaymentService {
    public void processPayment(String amount, String currency) {
        // Direct implementation without understanding domain
    }
}

// ✅ CORRECT - Study domain models first
@Service
public class PaymentService {
    public void processPayment(Money amount, TenantContext tenantContext) {
        // Follow existing domain patterns
    }
}
```

### 5. **Copy-Pasting Code Without Understanding Patterns**
```java
// ❌ WRONG - Copy-pasting without understanding
@Service
public class TransactionService {
    // Copied from another service without understanding
    // Inconsistent patterns and naming
}

// ✅ CORRECT - Study and understand patterns
@Service
public class TransactionService {
    // Follow established patterns
    // Maintain consistency with existing code
}
```

### 4. **Creating Inconsistent Naming Conventions**
```java
// ❌ WRONG - Inconsistent naming
public class paymentService {  // lowercase
    private PaymentRepository paymentRepo;  // abbreviated
    public void process_payment() {  // snake_case
    }
}

// ✅ CORRECT - Consistent naming
public class PaymentService {  // PascalCase
    private PaymentRepository paymentRepository;  // full name
    public void processPayment() {  // camelCase
    }
}
```

### 5. **Ignoring Existing Architectural Patterns**
```java
// ❌ WRONG - Ignoring existing patterns
@Service
public class PaymentService {
    // Direct database access without repository pattern
    // No event handling
    // No proper error handling
}

// ✅ CORRECT - Follow established patterns
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    
    // Follow established patterns
    // Implement proper event handling
    // Use repository pattern
}
```

### 6. **Creating Circular Dependencies**
```java
// ❌ WRONG - Circular dependencies
@Service
public class PaymentService {
    @Autowired
    private TransactionService transactionService;
}

@Service
public class TransactionService {
    @Autowired
    private PaymentService paymentService;  // Circular dependency!
}
```

### 7. **Forgetting to Handle Events Properly**
```java
// ❌ WRONG - No event handling
@Service
public class PaymentService {
    public void processPayment(Payment payment) {
        // Process payment but don't publish events
        // Other services won't know about the payment
    }
}

// ✅ CORRECT - Proper event handling
@Service
public class PaymentService {
    private final PaymentEventPublisher eventPublisher;
    
    public void processPayment(Payment payment) {
        // Process payment
        eventPublisher.publishPaymentProcessed(payment);
    }
}
```

### 8. **Ignoring Transaction Boundaries**
```java
// ❌ WRONG - No transaction boundaries
@Service
public class PaymentService {
    public void processPayment(Payment payment) {
        // Multiple database operations without transaction
        // Data inconsistency risk
    }
}

// ✅ CORRECT - Proper transaction boundaries
@Service
@Transactional
public class PaymentService {
    public void processPayment(Payment payment) {
        // All operations within transaction
        // Data consistency guaranteed
    }
}
```

## ✅ Do This Instead - Best Practices

### 1. **Always Check Contracts First**
```java
// ✅ CORRECT - Check existing DTOs
// Look in contracts/ for existing DTOs
// Use existing DTOs when possible
// Create new DTOs only when necessary
```

### 2. **Study Domain Models Before Implementing**
```java
// ✅ CORRECT - Understand domain first
// Read domain-models/ for business logic
// Study existing domain patterns
// Follow established domain structure
```

### 3. **Follow Established Patterns**
```java
// ✅ CORRECT - Follow existing patterns
// Study similar implementations
// Understand the patterns
// Maintain consistency
```

### 4. **Maintain Consistent Naming**
```java
// ✅ CORRECT - Consistent naming
public class PaymentService {  // PascalCase for classes
    private PaymentRepository paymentRepository;  // camelCase for fields
    public void processPayment() {  // camelCase for methods
    }
}
```

### 5. **Follow Architectural Patterns**
```java
// ✅ CORRECT - Follow established patterns
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    
    // Follow established patterns
    // Implement proper event handling
    // Use repository pattern
}
```

### 6. **Avoid Circular Dependencies**
```java
// ✅ CORRECT - Use events for communication
@Service
public class PaymentService {
    private final PaymentEventPublisher eventPublisher;
    
    public void processPayment(Payment payment) {
        // Process payment
        eventPublisher.publishPaymentProcessed(payment);
    }
}

@Service
public class TransactionService {
    @EventListener
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        // Handle payment event
    }
}
```

### 7. **Implement Proper Event Handling**
```java
// ✅ CORRECT - Proper event handling
@Service
public class PaymentService {
    private final PaymentEventPublisher eventPublisher;
    
    public void processPayment(Payment payment) {
        // Process payment
        eventPublisher.publishPaymentProcessed(payment);
    }
}
```

### 8. **Use Proper Transaction Boundaries**
```java
// ✅ CORRECT - Proper transaction boundaries
@Service
@Transactional
public class PaymentService {
    public void processPayment(Payment payment) {
        // All operations within transaction
        // Data consistency guaranteed
    }
}
```

## 🔍 Quality Gates to Prevent Anti-Patterns

### Before Implementation
- [ ] Check contracts/ for existing DTOs
- [ ] Study domain models in domain-models/
- [ ] Find similar implementations
- [ ] Understand existing patterns

### During Implementation
- [ ] Follow established naming conventions
- [ ] Use existing DTOs when possible
- [ ] Implement proper event handling
- [ ] Use repository pattern
- [ ] Implement proper error handling

### After Implementation
- [ ] Check for circular dependencies
- [ ] Verify event flow
- [ ] Test transaction boundaries
- [ ] Validate against existing code

## 🚀 Quick Reference Commands

### Check Existing DTOs
```bash
# Check contracts/ for existing DTOs
find contracts/ -name "*.java" | grep -i dto
find contracts/ -name "*.java" | grep -i request
find contracts/ -name "*.java" | grep -i response
```

### Check Existing Patterns
```bash
# Check existing service patterns
find . -name "*Service.java" -path "*/src/main/java/*"
find . -name "*Repository.java" -path "*/src/main/java/*"
find . -name "*Entity.java" -path "*/src/main/java/*"
```

### Check Import Patterns
```bash
# Check import patterns
grep -r "import com.payments.domain" --include="*.java" .
grep -r "import com.payments.contracts" --include="*.java" .
grep -r "import org.springframework" --include="*.java" .
```

## 📞 Resources to Avoid Anti-Patterns

### Architecture Resources
- Check `docs/architecture/` for system design
- Study `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md`

### Implementation Resources
- Study existing services in each microservice directory
- Follow patterns in `domain-models/`
- Use contracts in `contracts/`

### Domain Resources
- Review `domain-models/` for business logic
- Study existing domain patterns

### API Resources
- Check `contracts/` for existing DTOs
- Study existing API patterns

### Integration Resources
- Study `saga-orchestrator/` for saga patterns
- Follow event handling patterns

Remember: **Context First, Implementation Second**. Always understand the existing patterns before creating new ones.
