# CURSOR AGENT CONTEXT SYSTEM

## 🎯 Overview
This system provides comprehensive context for Cursor agents to work efficiently on the Payments Engine project, reducing iteration cycles and ensuring consistency.

## 📚 Documentation Files

### Core Context Files
- **`CURSOR-AGENT-CONTEXT.md`** - Main context guide with project overview and critical files
- **`AGENT-WORKFLOW.md`** - Context loading sequence and implementation workflow
- **`MASTER-CONTEXT-PROMPT.md`** - Master prompt for intelligent implementation
- **`AGENT-CONTEXT-CHECKLIST.md`** - Validation checklist for quality gates
- **`IMPLEMENTATION-STRATEGY.md`** - Smart feature implementation approach
- **`ANTI-PATTERNS-TO-AVOID.md`** - Common mistakes and how to avoid them

### Helper Scripts
- **`scripts/agent-context.sh`** - Bash script for Git Bash on Windows
- **`scripts/agent-context.ps1`** - PowerShell script for Windows
- **`scripts/agent-context.bat`** - Batch script for Windows

## 🚀 Quick Start for Cursor Agents

### 1. Load Context
```bash
# For Git Bash on Windows
./scripts/agent-context.sh

# For PowerShell on Windows
powershell -ExecutionPolicy Bypass -File scripts/agent-context.ps1

# For Windows Command Prompt
scripts\agent-context.bat
```

### 2. Read Core Documentation
1. **`CURSOR-AGENT-CONTEXT.md`** - Project overview and critical files
2. **`AGENT-WORKFLOW.md`** - Implementation workflow
3. **`MASTER-CONTEXT-PROMPT.md`** - Master implementation prompt

### 3. Follow Implementation Process
1. **Context Analysis** - Understand requirements and existing patterns
2. **Pattern Recognition** - Find similar implementations
3. **Implementation** - Follow established patterns
4. **Integration** - Ensure proper integration

## 🏗️ Architecture Context

### Project Structure
```
PE/
├── docs/                    # Organized documentation
│   ├── architecture/        # System design
│   ├── implementation/      # Implementation details
│   ├── features/           # Feature documentation
│   └── sessions/           # Session summaries
├── domain-models/          # Domain models and business logic
├── contracts/              # DTOs and API contracts
├── [microservices]/        # Individual services
└── scripts/               # Helper scripts
```

### Critical Files to Read First
1. **`docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md`** - Complete system design
2. **`docs/implementation/feature-breakdown-tree.yaml`** - Feature roadmap
3. **`domain-models/shared/`** - Core domain concepts
4. **`contracts/`** - Existing DTOs and APIs
5. **Existing services** - Reference implementations

## 🔍 Implementation Strategy

### Before Any Implementation
1. **Read Context** - Study relevant documentation
2. **Check Contracts** - Look for existing DTOs
3. **Study Patterns** - Find similar implementations
4. **Plan Approach** - Map to existing structure

### During Implementation
1. **Follow Patterns** - Use established patterns
2. **Maintain Consistency** - Follow naming conventions
3. **Use Existing DTOs** - Check contracts/ first
4. **Implement Properly** - Follow architectural patterns

### After Implementation
1. **Test Integration** - Verify integration points
2. **Check Quality** - Validate against existing code
3. **Update Context** - Learn from implementation

## 📋 Quality Gates

### Context Validation
- [ ] Project architecture understood
- [ ] Domain models studied
- [ ] Existing patterns identified
- [ ] Implementation plan created

### Implementation Quality
- [ ] Follow established patterns
- [ ] Maintain naming consistency
- [ ] Use existing DTOs when possible
- [ ] Implement proper error handling

### Integration Quality
- [ ] Service dependencies managed
- [ ] Event flow implemented
- [ ] Saga patterns followed
- [ ] Repository patterns consistent

## ⚠️ Common Pitfalls to Avoid

### ❌ Don't Do This
- Create new DTOs without checking contracts/
- Implement without understanding domain models
- Copy-paste without understanding patterns
- Create inconsistent naming
- Ignore existing architectural patterns

### ✅ Do This Instead
- Always check contracts/ for existing DTOs
- Study domain models before implementing
- Follow established patterns from similar services
- Maintain consistency with existing code
- Understand business logic before coding

## 🔄 Continuous Improvement

### After Each Implementation
1. **Success Analysis** - What worked well?
2. **Pattern Recognition** - What patterns were followed?
3. **Improvement Areas** - What could be better?
4. **Context Updates** - What new context is needed?
5. **Documentation Updates** - What docs need improvement?

### Regular Updates
1. **New Patterns** - Add successful patterns
2. **Anti-Patterns** - Document patterns to avoid
3. **Best Practices** - Update implementation guidelines
4. **Domain Changes** - Update domain documentation
5. **Integration Points** - Update service integration patterns

## 📞 Resources

### Architecture Resources
- `docs/architecture/` - System design documentation
- `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md` - Complete architecture

### Implementation Resources
- `docs/implementation/` - Implementation details
- `docs/implementation/feature-breakdown-tree.yaml` - Feature roadmap

### Domain Resources
- `domain-models/` - Business logic and domain models
- `domain-models/shared/` - Core domain concepts

### API Resources
- `contracts/` - DTOs and API contracts
- Existing service implementations

### Integration Resources
- `saga-orchestrator/` - Saga orchestration patterns
- Event handling patterns in existing services

## 🎯 Success Metrics

### Efficiency Improvements
- **Reduced Iterations** - Fewer back-and-forth cycles
- **Faster Implementation** - Quicker feature delivery
- **Better Quality** - Consistent, maintainable code
- **Reduced Errors** - Fewer import issues, DTO problems

### Quality Improvements
- **Consistent Patterns** - Following established patterns
- **Proper Integration** - Correct service integration
- **Event Handling** - Proper event flow
- **Saga Patterns** - Correct distributed transaction handling

## 🚀 Getting Started

### For New Agents
1. **Read this README** - Understand the system
2. **Run context loader** - `./scripts/agent-context.sh`
3. **Study core docs** - Read the context files
4. **Follow workflow** - Use the implementation strategy
5. **Validate quality** - Use the checklist

### For Existing Agents
1. **Update context** - Read latest documentation
2. **Refresh patterns** - Study recent implementations
3. **Validate approach** - Use the checklist
4. **Implement efficiently** - Follow established patterns

Remember: **Context First, Implementation Second**. Always understand the existing patterns before creating new ones.
