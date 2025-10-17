# NEW CURSOR AGENT PROMPT

## 🎯 **Copy and paste this prompt when creating a new Cursor agent:**

---

**You are working on a microservices payments engine project. Before implementing ANY feature, you MUST follow this context-first approach:**

### **1. LOAD PROJECT CONTEXT FIRST:**
```bash
# Run the context loader (choose one):
powershell -ExecutionPolicy Bypass -File scripts/agent-context.ps1
# OR
scripts\agent-context.bat
# OR
./scripts/agent-context.sh
```

### **2. READ THESE CRITICAL FILES (in order):**
1. **`CURSOR-AGENT-CONTEXT.md`** - Project overview and critical files
2. **`AGENT-WORKFLOW.md`** - Context loading sequence and implementation workflow
3. **`MASTER-CONTEXT-PROMPT.md`** - Master implementation prompt
4. **`AGENT-CONTEXT-CHECKLIST.md`** - Validation checklist
5. **`IMPLEMENTATION-STRATEGY.md`** - Smart feature implementation approach
6. **`ANTI-PATTERNS-TO-AVOID.md`** - Common mistakes to avoid
7. **`docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`** - **NEW**: Complete feature breakdown with 50 features across 8 phases

### **3. UNDERSTAND THE PROJECT:**
- **Architecture**: Microservices payments engine with 22 microservices
- **Tech Stack**: Spring Boot 3.x, Java 17, Maven, Docker, Kafka, PostgreSQL, Redis, Istio
- **Patterns**: DDD, CQRS, Saga, Event Sourcing, Service Mesh, Reactive Architecture
- **Current Phase**: Feature Implementation Phases (Phase 0-7)
- **Total Features**: 50 features across 8 feature phases

### **4. CRITICAL RULES - NEVER VIOLATE:**
- **ALWAYS check `contracts/` for existing DTOs before creating new ones**
- **ALWAYS study `domain-models/` for business logic and patterns**
- **ALWAYS find similar implementations in existing services**
- **ALWAYS follow established patterns from similar services**
- **ALWAYS maintain consistency with existing code**
- **ALWAYS understand the business logic before coding**
- **ALWAYS check the Enhanced Feature Breakdown Tree for phase dependencies**
- **ALWAYS follow the 8-phase implementation strategy (Phase 0-7)**

### **5. IMPLEMENTATION WORKFLOW:**
1. **Context Analysis** - Read feature requirements, understand domain, find similar implementations
2. **Feature Phase Identification** - Determine which feature phase (0-7) your feature belongs to
3. **Dependency Check** - Verify all dependencies from previous feature phases are complete
4. **Pattern Recognition** - Study existing services, identify reusable components
5. **Implementation** - Follow established patterns, use existing DTOs, maintain consistency
6. **Integration** - Ensure proper imports, follow package structure, test integration points
7. **Feature Phase Validation** - Ensure feature meets phase-specific requirements and KPIs

### **6. QUALITY GATES:**
- [ ] Context fully loaded and understood
- [ ] **Enhanced Feature Breakdown Tree reviewed**
- [ ] **Phase dependencies verified**
- [ ] Patterns identified and studied
- [ ] Existing DTOs checked in contracts/
- [ ] Implementation plan created
- [ ] Follow established patterns
- [ ] Maintain naming consistency
- [ ] Use existing DTOs when possible
- [ ] Implement proper error handling
- [ ] **Phase-specific KPIs met**
- [ ] **AI agent orchestration requirements satisfied**

### **7. BEFORE CREATING ANY NEW FILES:**
- **Check the Enhanced Feature Breakdown Tree for your specific feature**
- **Verify feature phase dependencies are met**
- Search for existing similar implementations
- Check if functionality already exists
- Verify you're not duplicating existing patterns
- Study the most similar existing service
- Follow its implementation patterns
- **Ensure feature aligns with feature phase requirements**

### **8. COMMON PITFALLS TO AVOID:**
- ❌ Creating new DTOs without checking contracts/
- ❌ Implementing without understanding domain models
- ❌ **Skipping feature phase dependency verification**
- ❌ **Ignoring the Enhanced Feature Breakdown Tree**
- ❌ Copy-pasting without understanding patterns
- ❌ Creating inconsistent naming conventions
- ❌ Ignoring existing architectural patterns
- ❌ Creating circular dependencies
- ❌ Forgetting to handle events properly
- ❌ Ignoring transaction boundaries
- ❌ **Working on features out of feature phase order**
- ❌ **Not following AI agent orchestration patterns**

### **9. SUCCESS FORMULA:**
**Context First, Feature Phase-Aware Implementation Second** - Always understand existing patterns and feature phase dependencies before creating new ones.

### **10. REFERENCE POINTS:**
- **Architecture**: `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md`
- **Feature Breakdown**: `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` - **PRIMARY REFERENCE**
- **Implementation**: `docs/implementation/feature-breakdown-tree.yaml`
- **Domain Models**: `domain-models/` for business logic
- **Contracts**: `contracts/` for existing DTOs
- **Services**: Study existing microservices for patterns
- **Feature Phase Dependencies**: Check Enhanced Feature Breakdown Tree for dependencies

**Remember: This project has extensive context documentation. Use it to work efficiently and avoid common mistakes. Always follow the established patterns and maintain consistency with existing code.**

---

## 🚀 **Additional Context for 8-Feature Phase Implementation Strategy:**

### **Current Project Status:**
- **Feature Phase 0**: Foundation (Sequential) - Database, Events, Domain Models, Libraries, Infrastructure
- **Feature Phase 1**: Core Services (Parallel) - Payment Initiation, Validation, Account Adapter, Routing, Transaction Processing, Saga Orchestrator
- **Feature Phase 2**: Clearing Adapters (Parallel) - SAMOS, BankservAfrica, RTC, PayShap, SWIFT
- **Feature Phase 3**: Platform Services (Parallel) - Tenant Management, IAM, Audit, Notification, Reporting
- **Feature Phase 4**: Advanced Features (Parallel) - Batch Processing, Settlement, Reconciliation, API Gateway, BFFs
- **Feature Phase 5**: Infrastructure (Parallel) - Service Mesh (Istio), Monitoring, GitOps, Feature Flags, K8s Operators
- **Feature Phase 6**: Integration & Testing (Sequential) - E2E, Load, Security, Compliance, Production Readiness
- **Feature Phase 7**: Operations & Channel Management (Parallel) - Operations Portal, Metrics, React UIs, Onboarding

### **Key Implementation Areas by Feature Phase:**
1. **Feature Phase 0**: Foundation - Database schemas, event schemas, domain models, shared libraries
2. **Feature Phase 1**: Core Services - Payment processing, validation, account integration, saga orchestration
3. **Feature Phase 2**: Clearing Adapters - External clearing system integrations
4. **Feature Phase 3**: Platform Services - Multi-tenancy, security, audit, notifications
5. **Feature Phase 4**: Advanced Features - Batch processing, settlement, reconciliation, API gateways
6. **Feature Phase 5**: Infrastructure - Service mesh, monitoring, GitOps, feature flags
7. **Feature Phase 6**: Testing - Comprehensive testing and production readiness
8. **Feature Phase 7**: Operations - Operations portal, channel management, self-service onboarding

### **Critical Success Factors:**
- **Feature Phase Dependencies** - Always verify previous feature phase completion
- **Parallel Execution** - Maximize parallelization within feature phases
- **AI Agent Orchestration** - Follow 50-agent assignment strategy
- **Pattern Consistency** - Follow established patterns
- **DTO Reuse** - Use existing DTOs from contracts/
- **Event Flow** - Proper event handling and saga patterns
- **Service Integration** - Correct microservice communication
- **Domain Understanding** - Business logic and rules

### **Expected Outcomes:**
- **50 Features** across 8 feature phases with AI agent orchestration
- **25-40 Days** total implementation time with parallelization
- **90% Reduction** in iteration cycles
- **80% Faster** feature implementation
- **95% Consistency** in code patterns
- **85% Reduction** in import/DTO issues

**Start by running the context loader and reading the Enhanced Feature Breakdown Tree. This will save hours of iterations and ensure you work efficiently from the beginning.**
