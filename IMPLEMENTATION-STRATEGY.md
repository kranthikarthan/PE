# IMPLEMENTATION STRATEGY FOR CURSOR AGENTS

## 🎯 Smart Feature Implementation Approach

### Step 1: Context Analysis (5 minutes)
1. **Read Enhanced Feature Breakdown Tree**
   - Study `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` - **PRIMARY REFERENCE**
   - Identify your feature's phase (0-7) and dependencies
   - Understand the 8-phase implementation strategy

2. **Phase Validation**
   - Verify all phase dependencies are complete
   - Ensure you're working in the correct phase order
   - Check phase-specific KPIs and requirements

3. **Understand Business Domain**
   - Read relevant domain model in `domain-models/`
   - Study existing business rules and patterns
   - Identify required entities and value objects

4. **Identify Existing Similar Implementations**
   - Search for similar services in the codebase
   - Study their implementation patterns
   - Identify reusable components

5. **Check Existing Contracts**
   - Look in `contracts/` for existing DTOs
   - Verify API contracts and interfaces
   - Understand data flow and transformations

### Step 2: Phase Validation (2 minutes)
1. **Verify Phase Dependencies**
   - Check if all previous phase requirements are complete
   - Ensure you're working in the correct phase order
   - Validate feature aligns with phase requirements

2. **Review Phase-Specific Requirements**
   - Check phase-specific KPIs and success criteria
   - Understand AI agent orchestration requirements
   - Review phase-specific patterns and technologies

### Step 3: Pattern Recognition (3 minutes)
1. **Find Most Similar Service**
   - Identify the service with the most similar functionality
   - Study its implementation patterns
   - Understand its architecture and design

2. **Study Implementation Patterns**
   - Review service layer patterns
   - Study repository layer patterns
   - Understand event handling patterns
   - Check saga orchestration patterns

3. **Identify Reusable Components**
   - Look for existing DTOs that can be reused
   - Identify common patterns that can be followed
   - Find existing utilities and helpers

4. **Plan Implementation Approach**
   - Map the feature to existing domain structure
   - Plan service integration points
   - Consider event flow and saga patterns
   - **Ensure AI agent orchestration requirements are met**

### Step 4: Implementation (15-30 minutes)
1. **Start with Domain Models**
   - Follow existing patterns in `domain-models/`
   - Use established value objects and entities
   - Maintain consistency with existing domain structure

2. **Create/Use Existing DTOs**
   - Check `contracts/` for existing DTOs first
   - Create new DTOs only if necessary
   - Follow established naming conventions

3. **Implement Service Layer**
   - Follow existing service patterns
   - Use established naming conventions
   - Implement proper business logic

4. **Add Repository Layer**
   - Follow existing repository patterns
   - Use established query methods
   - Maintain consistency with existing repositories

5. **Implement Event Handling**
   - Follow existing event patterns
   - Use established event types
   - Implement proper saga patterns

6. **Ensure Phase-Specific Requirements**
   - **Validate against phase-specific KPIs**
   - **Follow AI agent orchestration patterns**
   - **Ensure feature meets phase requirements**

### Step 5: Integration (5 minutes)
1. **Ensure Proper Imports**
   - Reference existing services for import patterns
   - Use established import conventions
   - Avoid circular dependencies

2. **Follow Package Structure**
   - Use established package structure
   - Maintain consistency with existing code
   - Follow naming conventions

3. **Test Integration Points**
   - Verify service dependencies
   - Test event flow
   - Validate saga patterns
   - Check API endpoints

4. **Validate Phase Requirements**
   - **Validate against phase-specific KPIs**
   - **Ensure AI agent orchestration requirements satisfied**
   - **Verify feature meets phase requirements**

## 🏗️ Feature-Specific Implementation Strategies

### Payment Processing Features
```markdown
## Context Analysis
1. Study `transaction-processing-service/` patterns
2. Review `payment-initiation-service/` patterns
3. Check `contracts/` for existing payment DTOs
4. Understand payment domain in `domain-models/`

## Pattern Recognition
1. Follow transaction processing patterns
2. Use existing payment DTOs
3. Implement proper saga patterns
4. Handle payment events correctly

## Implementation
1. Start with payment domain models
2. Use existing payment DTOs
3. Implement payment service layer
4. Add payment repository layer
5. Implement payment event handling
```

### Validation Features
```markdown
## Context Analysis
1. Study `validation-service/` patterns
2. Review existing validation patterns
3. Check `contracts/` for existing validation DTOs
4. Understand validation domain in `domain-models/`

## Pattern Recognition
1. Follow validation service patterns
2. Use existing validation DTOs
3. Implement proper validation rules
4. Handle validation events correctly

## Implementation
1. Start with validation domain models
2. Use existing validation DTOs
3. Implement validation service layer
4. Add validation repository layer
5. Implement validation event handling
```

### Routing Features
```markdown
## Context Analysis
1. Study `routing-service/` patterns
2. Review existing routing patterns
3. Check `contracts/` for existing routing DTOs
4. Understand routing domain in `domain-models/`

## Pattern Recognition
1. Follow routing service patterns
2. Use existing routing DTOs
3. Implement proper decision logic
4. Handle routing events correctly

## Implementation
1. Start with routing domain models
2. Use existing routing DTOs
3. Implement routing service layer
4. Add routing repository layer
5. Implement routing event handling
```

### Saga Features
```markdown
## Context Analysis
1. Study `saga-orchestrator/` patterns
2. Review existing saga patterns
3. Check `contracts/` for existing saga DTOs
4. Understand saga domain in `domain-models/`

## Pattern Recognition
1. Follow saga orchestration patterns
2. Use existing saga DTOs
3. Implement proper orchestration
4. Handle saga events correctly

## Implementation
1. Start with saga domain models
2. Use existing saga DTOs
3. Implement saga service layer
4. Add saga repository layer
5. Implement saga event handling
```

## 🔍 Implementation Quality Gates

### Before Implementation
- [ ] Context fully loaded and understood
- [ ] Patterns identified and studied
- [ ] Existing DTOs checked
- [ ] Implementation plan created

### During Implementation
- [ ] Follow established patterns
- [ ] Maintain naming consistency
- [ ] Use existing DTOs when possible
- [ ] Implement proper error handling

### After Implementation
- [ ] Test integration points
- [ ] Verify event flow
- [ ] Check saga patterns
- [ ] Validate against existing code

## 🚀 Quick Implementation Templates

### Service Implementation Template
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class [Feature]Service {
    
    private final [Feature]Repository repository;
    private final [Feature]EventPublisher eventPublisher;
    private final [Feature]Validator validator;
    
    // Follow existing service patterns
    // Use established naming conventions
    // Implement proper event handling
}
```

### Repository Implementation Template
```java
@Repository
public interface [Feature]Repository extends JpaRepository<[Feature]Entity, [Feature]Id> {
    
    // Follow existing repository patterns
    // Use established query methods
    // Maintain consistency with existing repositories
}
```

### Event Handling Template
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class [Feature]EventHandler {
    
    // Follow existing event handling patterns
    // Use established event types
    // Implement proper saga patterns
}
```

## ⚠️ Common Implementation Pitfalls

### ❌ Don't Do This:
- Create new DTOs without checking contracts/
- Implement features without understanding domain models
- Copy-paste code without understanding patterns
- Create inconsistent naming conventions
- Ignore existing architectural patterns
- Create circular dependencies
- Forget to handle events properly
- Ignore transaction boundaries

### ✅ Do This Instead:
- Always check contracts/ for existing DTOs
- Study domain models before implementing
- Follow established patterns from similar services
- Maintain consistency with existing code
- Understand the business logic before coding
- Use existing value objects and entities
- Follow saga patterns for distributed transactions
- Implement proper event handling

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

## 📞 Implementation Resources

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
