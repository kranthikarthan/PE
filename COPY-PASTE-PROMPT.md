# COPY-PASTE PROMPT FOR NEW CURSOR AGENTS

## 🎯 **Copy and paste this when creating a new Cursor agent:**

---

**You are working on a microservices payments engine. Follow this context-first approach:**

### **1. LOAD CONTEXT:**
```bash
powershell -ExecutionPolicy Bypass -File scripts/agent-context.ps1
```

### **2. READ THESE FILES:**
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

## 🚀 **For Next Feature Phase Implementation:**

### **Current Status:**
- **Feature Phase 0**: Foundation (Sequential) - Database, Events, Domain Models, Libraries, Infrastructure
- **Feature Phase 1**: Core Services (Parallel) - Payment Initiation, Validation, Account Adapter, Routing, Transaction Processing, Saga Orchestrator
- **Feature Phase 2**: Clearing Adapters (Parallel) - SAMOS, BankservAfrica, RTC, PayShap, SWIFT
- **Feature Phase 3**: Platform Services (Parallel) - Tenant Management, IAM, Audit, Notification, Reporting
- **Feature Phase 4**: Advanced Features (Parallel) - Batch Processing, Settlement, Reconciliation, API Gateway, BFFs
- **Feature Phase 5**: Infrastructure (Parallel) - Service Mesh (Istio), Monitoring, GitOps, Feature Flags, K8s Operators
- **Feature Phase 6**: Integration & Testing (Sequential) - E2E, Load, Security, Compliance, Production Readiness
- **Feature Phase 7**: Operations & Channel Management (Parallel) - Operations Portal, Metrics, React UIs, Onboarding

### **Key Areas by Feature Phase:**
1. **Feature Phase 0**: Foundation - Database schemas, event schemas, domain models, shared libraries
2. **Feature Phase 1**: Core Services - Payment processing, validation, account integration, saga orchestration
3. **Feature Phase 2**: Clearing Adapters - External clearing system integrations
4. **Feature Phase 3**: Platform Services - Multi-tenancy, security, audit, notifications
5. **Feature Phase 4**: Advanced Features - Batch processing, settlement, reconciliation, API gateways
6. **Feature Phase 5**: Infrastructure - Service mesh, monitoring, GitOps, feature flags
7. **Feature Phase 6**: Testing - Comprehensive testing and production readiness
8. **Feature Phase 7**: Operations - Operations portal, channel management, self-service onboarding

### **Expected Outcomes:**
- **90% Reduction** in iteration cycles
- **80% Faster** feature implementation
- **95% Consistency** in code patterns
- **85% Reduction** in import/DTO issues

**Start by running the context loader and reading the documentation. This will save hours of iterations and ensure you work efficiently from the beginning.**
