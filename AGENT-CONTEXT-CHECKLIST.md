# AGENT CONTEXT CHECKLIST

## 🔍 Before Implementing Any Feature

### ✅ Phase Validation
- [ ] **Read Enhanced Feature Breakdown Tree** (`docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`)
- [ ] **Identify feature's phase (0-7) and dependencies**
- [ ] **Verify all phase dependencies are complete**
- [ ] **Ensure working in correct phase order**
- [ ] **Check phase-specific KPIs and requirements**

### ✅ Domain Understanding
- [ ] Read relevant domain model in `domain-models/`
- [ ] Understand the business logic and rules
- [ ] Check existing domain patterns
- [ ] Identify required entities and value objects
- [ ] Study existing aggregates and their relationships

### ✅ Contract Analysis
- [ ] Check `contracts/` for existing DTOs
- [ ] Verify API contracts and interfaces
- [ ] Understand data flow and transformations
- [ ] Plan new DTOs if needed
- [ ] Ensure consistency with existing contracts

### ✅ Pattern Recognition
- [ ] Study similar implementations in other services
- [ ] Follow established architectural patterns
- [ ] Maintain consistency with existing code
- [ ] Use established naming conventions
- [ ] Follow established package structure

### ✅ Implementation Planning
- [ ] Map feature to existing domain structure
- [ ] Identify required DTOs (check if they exist)
- [ ] Plan service integration points
- [ ] Consider event flow and saga patterns
- [ ] Plan repository patterns
- [ ] **Ensure feature aligns with phase requirements**
- [ ] **Validate AI agent orchestration requirements**

## 🏗️ Architecture Validation

### ✅ Domain-Driven Design (DDD)
- [ ] Aggregates follow existing patterns
- [ ] Value objects are reused when possible
- [ ] Entities follow established patterns
- [ ] Domain services follow existing patterns
- [ ] Business rules are properly encapsulated

### ✅ Event Sourcing & CQRS
- [ ] Events follow existing event patterns
- [ ] Commands follow existing command patterns
- [ ] Queries follow existing query patterns
- [ ] Sagas follow saga orchestration patterns
- [ ] Event handling is properly implemented

### ✅ Microservices Patterns
- [ ] Service communication follows established patterns
- [ ] Data consistency uses saga patterns
- [ ] Caching follows Redis patterns
- [ ] Monitoring follows telemetry patterns
- [ ] Service boundaries are respected

## 📋 Implementation Quality Gates

### ✅ Code Quality
- [ ] Follow established naming conventions
- [ ] Maintain consistent package structure
- [ ] Use proper imports (reference existing services)
- [ ] Implement proper error handling
- [ ] Follow established coding standards
- [ ] **Ensure phase-specific requirements are met**
- [ ] **Follow AI agent orchestration patterns**

### ✅ Integration Quality
- [ ] Service dependencies are properly managed
- [ ] Event flow follows established patterns
- [ ] Saga patterns are correctly implemented
- [ ] Repository patterns are consistent
- [ ] Controller patterns follow REST conventions
- [ ] **Validate against phase-specific KPIs**
- [ ] **Ensure AI agent orchestration requirements satisfied**

### ✅ Testing Quality
- [ ] Unit tests follow existing patterns
- [ ] Integration tests are properly structured
- [ ] Test data follows established patterns
- [ ] Mocking follows existing patterns
- [ ] Test coverage meets established standards

## 🔍 Feature-Specific Checklists

### ✅ Payment Processing Features
- [ ] Study `transaction-processing-service/` patterns
- [ ] Follow `payment-initiation-service/` patterns
- [ ] Use existing DTOs from `contracts/`
- [ ] Implement proper saga patterns
- [ ] Handle events correctly

### ✅ Validation Features
- [ ] Study `validation-service/` patterns
- [ ] Follow existing validation patterns
- [ ] Use existing validation DTOs
- [ ] Implement proper validation rules
- [ ] Handle validation events

### ✅ Routing Features
- [ ] Study `routing-service/` patterns
- [ ] Follow existing routing patterns
- [ ] Use existing routing DTOs
- [ ] Implement proper decision logic
- [ ] Handle routing events

### ✅ Saga Features
- [ ] Study `saga-orchestrator/` patterns
- [ ] Follow existing saga patterns
- [ ] Use existing saga DTOs
- [ ] Implement proper orchestration
- [ ] Handle saga events

## 🚀 Quick Validation Commands

### Context Validation
```bash
# Check if DTOs exist
find contracts/ -name "*.java" | grep -i dto
find contracts/ -name "*.java" | grep -i request
find contracts/ -name "*.java" | grep -i response

# Check existing patterns
find . -name "*Service.java" -path "*/src/main/java/*"
find . -name "*Repository.java" -path "*/src/main/java/*"
find . -name "*Entity.java" -path "*/src/main/java/*"
```

### Pattern Validation
```bash
# Check existing implementations
grep -r "class.*Service" --include="*.java" .
grep -r "class.*Repository" --include="*.java" .
grep -r "class.*Entity" --include="*.java" .
```

### Import Validation
```bash
# Check import patterns
grep -r "import com.payments.domain" --include="*.java" .
grep -r "import com.payments.contracts" --include="*.java" .
grep -r "import org.springframework" --include="*.java" .
```

## ⚠️ Common Issues to Avoid

### ❌ Import Issues
- [ ] Missing imports for domain objects
- [ ] Incorrect import paths
- [ ] Unused imports
- [ ] Circular import dependencies

### ❌ DTO Issues
- [ ] Creating new DTOs when existing ones exist
- [ ] Inconsistent DTO naming
- [ ] Missing DTO validation
- [ ] Incorrect DTO mapping

### ❌ Entity Issues
- [ ] Inconsistent entity naming
- [ ] Missing entity relationships
- [ ] Incorrect entity mapping
- [ ] Missing entity validation

### ❌ Service Issues
- [ ] Inconsistent service naming
- [ ] Missing service dependencies
- [ ] Incorrect service patterns
- [ ] Missing service validation

### ❌ Event Issues
- [ ] Missing event handling
- [ ] Incorrect event patterns
- [ ] Missing event validation
- [ ] Incorrect event flow

## 🔄 Continuous Improvement

### After Each Implementation
1. **Success Analysis**: What worked well?
2. **Pattern Recognition**: What patterns were followed correctly?
3. **Improvement Areas**: What could be done better?
4. **Context Updates**: What new context should be added?
5. **Documentation Updates**: What documentation needs improvement?

### Regular Updates
1. **New Patterns**: Add successful patterns to context
2. **Anti-Patterns**: Document patterns to avoid
3. **Best Practices**: Update implementation guidelines
4. **Domain Changes**: Update domain model documentation
5. **Integration Points**: Update service integration patterns

## 📞 Validation Resources

### Phase & Feature Validation
- **PRIMARY**: Check `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`
- Review the 8-phase implementation strategy
- Check phase dependencies and requirements

### Architecture Validation
- Check `docs/architecture/` for system design
- Study `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md`

### Implementation Validation
- Study existing services in each microservice directory
- Follow patterns in `domain-models/`
- Use contracts in `contracts/`

### Domain Validation
- Review `domain-models/` for business logic
- Study existing domain patterns

### API Validation
- Check `contracts/` for existing DTOs
- Study existing API patterns

### Integration Validation
- Study `saga-orchestrator/` for saga patterns
- Follow event handling patterns

### AI Agent Orchestration Validation
- Review the Enhanced Feature Breakdown Tree for agent assignments
- Check phase-specific AI agent requirements

Remember: **Context First, Phase-Aware Implementation Second**. Always validate your understanding and phase dependencies before implementing.
