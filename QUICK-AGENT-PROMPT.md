# QUICK AGENT PROMPT - COPY & PASTE

## 🎯 **Use this prompt when creating a new Cursor agent:**

---

**You are working on a microservices payments engine. Follow this context-first approach:**

### **1. LOAD CONTEXT FIRST:**
```bash
powershell -ExecutionPolicy Bypass -File scripts/agent-context.ps1
```

### **2. READ THESE FILES (in order):**
1. `CURSOR-AGENT-CONTEXT.md` - Project overview
2. `AGENT-WORKFLOW.md` - Implementation workflow
3. `MASTER-CONTEXT-PROMPT.md` - Master prompt
4. `IMPLEMENTATION-STRATEGY.md` - Smart implementation
5. `ANTI-PATTERNS-TO-AVOID.md` - Common mistakes

### **3. CRITICAL RULES:**
- **ALWAYS check `contracts/` for existing DTOs before creating new ones**
- **ALWAYS study `domain-models/` for business logic and patterns**
- **ALWAYS find similar implementations in existing services**
- **ALWAYS follow established patterns from similar services**
- **ALWAYS maintain consistency with existing code**

### **4. IMPLEMENTATION WORKFLOW:**
1. **Context Analysis** - Read requirements, understand domain, find similar implementations
2. **Pattern Recognition** - Study existing services, identify reusable components
3. **Implementation** - Follow established patterns, use existing DTOs, maintain consistency
4. **Integration** - Ensure proper imports, follow package structure, test integration points

### **5. BEFORE CREATING ANY NEW FILES:**
- Search for existing similar implementations
- Check if functionality already exists
- Study the most similar existing service
- Follow its implementation patterns

### **6. COMMON PITFALLS TO AVOID:**
- ❌ Creating new DTOs without checking contracts/
- ❌ Implementing without understanding domain models
- ❌ Copy-pasting without understanding patterns
- ❌ Creating inconsistent naming conventions
- ❌ Ignoring existing architectural patterns

### **7. SUCCESS FORMULA:**
**Context First, Implementation Second** - Always understand existing patterns before creating new ones.

**Remember: This project has extensive context documentation. Use it to work efficiently and avoid common mistakes. Always follow the established patterns and maintain consistency with existing code.**

---

## 🚀 **For Next Phase Implementation:**

### **Current Status:**
- **Phase 1**: Core infrastructure implemented
- **Phase 2**: Advanced features and integrations
- **Phase 3**: Optimization and scaling

### **Key Areas:**
1. **Payment Processing** - Core transaction handling
2. **Validation Services** - Business rule validation
3. **Saga Orchestration** - Distributed transaction management
4. **Routing Services** - Payment routing decisions
5. **Event Handling** - Event sourcing and CQRS patterns

### **Expected Outcomes:**
- **90% Reduction** in iteration cycles
- **80% Faster** feature implementation
- **95% Consistency** in code patterns
- **85% Reduction** in import/DTO issues

**Start by running the context loader and reading the documentation. This will save hours of iterations and ensure you work efficiently from the beginning.**
