# CURSOR AGENT WORKFLOW GUIDE

## 🔄 Context Loading Sequence

### Phase 1: Project Understanding (5 minutes)
1. **Read Enhanced Feature Breakdown Tree**
   - `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` - **PRIMARY REFERENCE**
   - Identify your feature's phase (0-7) and dependencies
   - Understand the 8-phase implementation strategy

2. **Read Project Structure**
   - `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md`
   - `docs/architecture/COMPLETE-ARCHITECTURE-SUMMARY.md`
   - Understand the microservices architecture

3. **Understand Current Phase**
   - `docs/implementation/feature-breakdown-tree.yaml`
   - `docs/implementation/PHASE1-IMPLEMENTATION-SUMMARY.md`
   - Know what's already implemented

4. **Study Domain Models**
   - `domain-models/shared/` - Core concepts
   - `domain-models/[relevant-domain]/` - Specific domain
   - Understand business rules and patterns

### Phase 2: Phase Validation (2 minutes)
1. **Verify Phase Dependencies**
   - Check if all previous phase requirements are complete
   - Ensure you're working in the correct phase order
   - Validate feature aligns with phase requirements

2. **Review Phase-Specific Requirements**
   - Check phase-specific KPIs and success criteria
   - Understand AI agent orchestration requirements
   - Review phase-specific patterns and technologies

### Phase 3: Pattern Recognition (3 minutes)
1. **Find Similar Implementations**
   - Look for existing services with similar functionality
   - Study their implementation patterns
   - Identify reusable components

2. **Check Existing Contracts**
   - `contracts/` - Look for existing DTOs
   - Check API interfaces and contracts
   - Understand data flow patterns

3. **Study Service Patterns**
   - Review existing service implementations
   - Understand repository patterns
   - Study event handling patterns

### Phase 4: Implementation Planning (2 minutes)
1. **Map Feature to Domain**
   - Identify required domain objects
   - Plan entity relationships
   - Consider business rules

2. **Plan Integration Points**
   - Identify service dependencies
   - Plan event flow
   - Consider saga patterns

3. **Check DTO Requirements**
   - Look for existing DTOs in contracts/
   - Plan new DTOs if needed
   - Ensure consistency

4. **Validate AI Agent Requirements**
   - Ensure feature meets AI agent orchestration requirements
   - Check phase-specific agent assignments
   - Review parallelization opportunities

## 🎯 Feature Implementation Workflow

### Step 1: Context Analysis
```markdown
Before starting any feature:
1. Read the Enhanced Feature Breakdown Tree
2. Identify your feature's phase (0-7) and dependencies
3. Verify all phase dependencies are complete
4. Understand the business domain
5. Identify existing similar implementations
6. Check for existing DTOs and contracts
```

### Step 2: Phase Validation
```markdown
1. Ensure you're working in the correct phase order
2. Verify feature aligns with phase requirements
3. Check phase-specific KPIs and success criteria
4. Review AI agent orchestration requirements
```

### Step 3: Pattern Matching
```markdown
1. Find the most similar existing service
2. Study its implementation patterns
3. Identify reusable components
4. Plan the implementation approach
```

### Step 4: Implementation
```markdown
1. Start with domain models (follow existing patterns)
2. Create/use existing DTOs from contracts/
3. Implement service layer (follow existing patterns)
4. Add repository layer (follow existing patterns)
5. Implement event handling (follow saga patterns)
6. Ensure phase-specific requirements are met
```

### Step 5: Integration
```markdown
1. Ensure proper imports (reference existing services)
2. Follow established package structure
3. Maintain consistency with existing code
4. Test integration points
5. Validate against phase-specific KPIs
```

## 🔍 Context Validation Checklist

### ✅ Phase Validation
- [ ] **Read Enhanced Feature Breakdown Tree**
- [ ] **Identify feature's phase (0-7) and dependencies**
- [ ] **Verify all phase dependencies are complete**
- [ ] **Ensure working in correct phase order**
- [ ] **Check phase-specific KPIs and requirements**

### ✅ Domain Understanding
- [ ] Read relevant domain model in domain-models/
- [ ] Understand the business logic and rules
- [ ] Check existing domain patterns
- [ ] Identify required entities and value objects

### ✅ Contract Analysis
- [ ] Check contracts/ for existing DTOs
- [ ] Verify API contracts and interfaces
- [ ] Understand data flow and transformations
- [ ] Plan new DTOs if needed

### ✅ Pattern Recognition
- [ ] Study similar implementations in other services
- [ ] Follow established architectural patterns
- [ ] Maintain consistency with existing code
- [ ] Use established naming conventions

### ✅ Implementation Planning
- [ ] Map feature to existing domain structure
- [ ] Identify required DTOs (check if they exist)
- [ ] Plan service integration points
- [ ] Consider event flow and saga patterns
- [ ] **Validate AI agent orchestration requirements**

## 🚀 Quick Reference Commands

### Context Loading
```bash
# Load project context
./scripts/agent-context.sh

# Check existing patterns
find . -name "*Service.java" -path "*/src/main/java/*"
find . -name "*Repository.java" -path "*/src/main/java/*"
find . -name "*Entity.java" -path "*/src/main/java/*"
```

### Pattern Analysis
```bash
# Find similar implementations
grep -r "class.*Service" --include="*.java" .
grep -r "class.*Repository" --include="*.java" .
grep -r "class.*Entity" --include="*.java" .
```

### Contract Checking
```bash
# Check existing DTOs
find contracts/ -name "*.java" | grep -i dto
find contracts/ -name "*.java" | grep -i request
find contracts/ -name "*.java" | grep -i response
```

## 📋 Implementation Templates

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

## ⚠️ Quality Gates

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

Remember: **Context First, Phase-Aware Implementation Second**. Always understand the existing patterns and phase dependencies before creating new ones.
