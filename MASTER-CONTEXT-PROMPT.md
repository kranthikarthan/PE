# MASTER CONTEXT PROMPT FOR CURSOR AGENTS

## 🎯 You are working on a microservices payments engine with 22 microservices. Before implementing ANY feature:

### 1. **READ THESE FIRST** (in order):
- `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` - **PRIMARY**: Complete 8-phase feature breakdown with 50 features
- `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md` - Complete system design
- `docs/implementation/feature-breakdown-tree.yaml` - YAML export for programmatic orchestration
- `domain-models/[relevant-domain]/` - Study existing patterns
- `contracts/` - Check existing DTOs and APIs
- `[similar-service]/` - Reference existing implementations

### 2. **FOLLOW ESTABLISHED PATTERNS**:
- **Always check the Enhanced Feature Breakdown Tree for phase dependencies**
- **Follow the 8-phase implementation strategy (Phase 0-7)**
- Use existing DTOs from contracts/ before creating new ones
- Follow domain model structure in domain-models/
- Maintain consistency with existing service patterns
- Use established naming conventions
- Follow saga patterns for distributed transactions
- Implement proper event handling
- **Follow AI agent orchestration patterns**

### 3. **IMPLEMENTATION CHECKLIST**:
- [ ] **Read Enhanced Feature Breakdown Tree**
- [ ] **Identify feature's phase (0-7) and dependencies**
- [ ] **Verify all phase dependencies are complete**
- [ ] **Ensure working in correct phase order**
- [ ] Check if DTO already exists in contracts/
- [ ] Verify domain model structure matches existing patterns
- [ ] Ensure imports are correct (use existing services as reference)
- [ ] Follow established package structure
- [ ] Maintain consistency with existing code style
- [ ] Implement proper event flow and saga patterns
- [ ] Use existing value objects and entities
- [ ] **Ensure phase-specific requirements are met**
- [ ] **Follow AI agent orchestration patterns**

### 4. **BEFORE CREATING NEW FILES**:
- **Check the Enhanced Feature Breakdown Tree for your specific feature**
- **Verify phase dependencies are met**
- Search for existing similar implementations
- Check if functionality already exists
- Verify you're not duplicating existing patterns
- Study the most similar existing service
- Follow its implementation patterns
- **Ensure feature aligns with phase requirements**

## 🏗️ Architecture Context

### Domain-Driven Design (DDD)
- **Aggregates**: Study existing aggregates in domain-models/
- **Value Objects**: Follow Money, TenantContext patterns
- **Entities**: Follow Transaction, Payment patterns
- **Domain Services**: Follow existing service patterns

### Event Sourcing & CQRS
- **Events**: Follow TransactionEvent patterns
- **Commands**: Follow existing command patterns
- **Queries**: Follow existing query patterns
- **Sagas**: Follow saga orchestration patterns

### Microservices Patterns
- **Service Communication**: Kafka events, REST APIs
- **Data Consistency**: Saga patterns for distributed transactions
- **Caching**: Redis patterns in routing-service
- **Monitoring**: Telemetry patterns in shared-telemetry

## 📋 Package Structure to Follow

```
com.payments.[service]
├── domain/          # Domain models and business logic
├── service/         # Application services
├── repository/      # Data access layer
├── controller/      # REST controllers
├── config/          # Configuration classes
├── event/           # Event handling
└── exception/       # Custom exceptions
```

## 🔍 Naming Conventions

### Entities
- `TransactionEntity`, `PaymentEntity`
- Follow existing entity patterns

### DTOs
- `TransactionDto`, `PaymentDto`
- Check contracts/ for existing DTOs first

### Services
- `TransactionService`, `PaymentService`
- Follow existing service patterns

### Repositories
- `TransactionRepository`, `PaymentRepository`
- Follow existing repository patterns

### Events
- `TransactionCreatedEvent`, `PaymentProcessedEvent`
- Follow existing event patterns

## ⚠️ Critical Rules

### ❌ NEVER Do This:
- Create new DTOs without checking contracts/
- **Skip phase dependency verification**
- **Ignore the Enhanced Feature Breakdown Tree**
- Implement features without understanding domain models
- **Work on features out of phase order**
- Copy-paste code without understanding patterns
- Create inconsistent naming conventions
- Ignore existing architectural patterns
- Create circular dependencies
- Forget to handle events properly
- Ignore transaction boundaries
- **Not follow AI agent orchestration patterns**

### ✅ ALWAYS Do This:
- **Always verify phase dependencies first**
- **Always consult the Enhanced Feature Breakdown Tree**
- Check contracts/ for existing DTOs first
- Study domain models before implementing
- **Follow the 8-phase implementation strategy**
- Follow established patterns from similar services
- Maintain consistency with existing code
- Understand the business logic before coding
- Use existing value objects and entities
- Follow saga patterns for distributed transactions
- Implement proper event handling
- **Follow AI agent orchestration patterns**

## 🚀 Implementation Strategy

### Step 1: Context Analysis
1. **Read the Enhanced Feature Breakdown Tree**
2. **Identify your feature's phase (0-7) and dependencies**
3. **Verify all phase dependencies are complete**
4. Understand the business domain and rules
5. Identify existing similar implementations
6. Check for existing DTOs and contracts

### Step 2: Phase Validation
1. **Ensure you're working in the correct phase order**
2. **Verify feature aligns with phase requirements**
3. **Check phase-specific KPIs and success criteria**
4. **Review AI agent orchestration requirements**

### Step 3: Pattern Recognition
1. Find the most similar existing service
2. Study its implementation patterns
3. Identify reusable components
4. Plan the implementation approach

### Step 4: Implementation
1. Start with domain models (follow existing patterns)
2. Create/use existing DTOs from contracts/
3. Implement service layer (follow existing patterns)
4. Add repository layer (follow existing patterns)
5. Implement event handling (follow saga patterns)
6. **Ensure phase-specific requirements are met**

### Step 5: Integration
1. Ensure proper imports (reference existing services)
2. Follow established package structure
3. Maintain consistency with existing code
4. Test integration points
5. **Validate against phase-specific KPIs**

## 🔍 Quality Gates

### Before Implementation
- [ ] **Enhanced Feature Breakdown Tree reviewed**
- [ ] **Phase dependencies verified**
- [ ] Context fully loaded and understood
- [ ] Patterns identified and studied
- [ ] Existing DTOs checked
- [ ] Implementation plan created

### During Implementation
- [ ] Follow established patterns
- [ ] Maintain naming consistency
- [ ] Use existing DTOs when possible
- [ ] Implement proper error handling
- [ ] **Ensure phase-specific requirements are met**
- [ ] **Follow AI agent orchestration patterns**

### After Implementation
- [ ] Test integration points
- [ ] Verify event flow
- [ ] Check saga patterns
- [ ] Validate against existing code
- [ ] **Validate against phase-specific KPIs**
- [ ] **Ensure AI agent orchestration requirements satisfied**

## 📞 Reference Points

### Phase & Feature Questions
- **PRIMARY**: Check `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`
- Review the 8-phase implementation strategy
- Check phase dependencies and requirements

### Architecture Questions
- Check `docs/architecture/` for system design
- Study `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md`

### Implementation Patterns
- Study existing services in each microservice directory
- Follow patterns in `domain-models/`
- Use contracts in `contracts/`

### Domain Questions
- Review `domain-models/` for business logic
- Study existing domain patterns

### API Questions
- Check `contracts/` for existing DTOs
- Study existing API patterns

### Integration Questions
- Study `saga-orchestrator/` for saga patterns
- Follow event handling patterns

### AI Agent Orchestration
- Review the Enhanced Feature Breakdown Tree for agent assignments
- Check phase-specific AI agent requirements

## 🎯 Remember

**Context First, Phase-Aware Implementation Second**. Always understand the existing patterns and phase dependencies before creating new ones. This will save hours of iterations and ensure consistency across the entire project.
