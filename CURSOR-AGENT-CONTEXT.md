# CURSOR AGENT CONTEXT GUIDE

## 🎯 Project Overview
- **Architecture**: Microservices payments engine with 22 microservices
- **Tech Stack**: Spring Boot 3.x, Java 17, Maven, Docker, Kafka, PostgreSQL, Redis, Istio
- **Patterns**: DDD, CQRS, Saga, Event Sourcing, Service Mesh, Reactive Architecture
- **Current Phase**: 8-Phase Implementation Strategy (Phase 0-7) with 50 features
- **AI Orchestration**: 50 AI agents working across 8 phases with parallelization

## 📚 Critical Files to Read First (In Order)

### 1. System Architecture
- `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md` - Complete system design
- `docs/architecture/COMPLETE-ARCHITECTURE-SUMMARY.md` - Architecture summary
- `docs/architecture/MODERN-ARCHITECTURE-SUMMARY.md` - Modern patterns

### 2. Implementation Context
- `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` - **PRIMARY**: Complete 8-phase feature breakdown with 50 features
- `docs/implementation/feature-breakdown-tree.yaml` - YAML export for programmatic orchestration
- `docs/implementation/PHASE1-IMPLEMENTATION-SUMMARY.md` - Phase 1 implementation status
- `docs/implementation/PHASE2-IMPLEMENTATION-SUMMARY.md` - Phase 2 implementation status
- `docs/implementation/PHASE3-IMPLEMENTATION-SUMMARY.md` - Phase 3 implementation status

### 3. Domain Models (Study These First)
- `domain-models/shared/` - Core domain concepts (Money, TenantContext, etc.)
- `domain-models/transaction-processing/` - Transaction domain patterns
- `domain-models/payment-initiation/` - Payment initiation patterns
- `domain-models/saga-orchestrator/` - Saga orchestration patterns
- `domain-models/validation/` - Validation domain patterns

### 4. API Contracts
- `contracts/` - All DTOs, interfaces, and API contracts
- Study existing DTOs before creating new ones

### 5. Existing Service Implementations
- `payment-initiation-service/` - Reference implementation
- `transaction-processing-service/` - Transaction processing patterns
- `validation-service/` - Validation service patterns
- `saga-orchestrator/` - Saga orchestration patterns
- `routing-service/` - Routing and decision patterns

## 🏗️ Architecture Patterns to Follow

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

## 📋 Coding Standards

### Package Structure
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

### Naming Conventions
- **Entities**: `TransactionEntity`, `PaymentEntity`
- **DTOs**: `TransactionDto`, `PaymentDto`
- **Services**: `TransactionService`, `PaymentService`
- **Repositories**: `TransactionRepository`, `PaymentRepository`
- **Events**: `TransactionCreatedEvent`, `PaymentProcessedEvent`

### Import Patterns
- **Domain**: `com.payments.domain.*`
- **Shared**: `com.payments.domain.shared.*`
- **Contracts**: `com.payments.contracts.*`
- **Spring**: `org.springframework.*`
- **Lombok**: `lombok.*`

## 🔍 Before Implementing Any Feature

### 1. Context Analysis
- [ ] **Read the Enhanced Feature Breakdown Tree** (`docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`)
- [ ] **Identify your feature's phase (0-7) and dependencies**
- [ ] Understand the business domain and rules
- [ ] Identify existing similar implementations
- [ ] Check for existing DTOs and contracts

### 2. Phase Validation
- [ ] **Verify all phase dependencies are complete**
- [ ] **Check if you're working in the correct phase order**
- [ ] **Ensure feature aligns with phase requirements**
- [ ] **Review phase-specific KPIs and success criteria**

### 3. Pattern Recognition
- [ ] Find the most similar existing service
- [ ] Study its implementation patterns
- [ ] Identify reusable components
- [ ] Plan the implementation approach

### 4. Implementation Planning
- [ ] Map feature to existing domain structure
- [ ] Identify required DTOs (check if they exist)
- [ ] Plan service integration points
- [ ] Consider event flow and saga patterns
- [ ] **Ensure AI agent orchestration requirements are met**

## ⚠️ Common Pitfalls to Avoid

### ❌ Don't Do This:
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

### ✅ Do This Instead:
- Always check contracts/ for existing DTOs
- **Always verify phase dependencies first**
- **Always consult the Enhanced Feature Breakdown Tree**
- Study domain models before implementing
- **Follow the 8-phase implementation strategy**
- Follow established patterns from similar services
- Maintain consistency with existing code
- Understand the business logic before coding
- Use existing value objects and entities
- Follow saga patterns for distributed transactions
- Implement proper event handling
- **Follow AI agent orchestration patterns**

## 🚀 Quick Start Checklist

### For New Features:
1. **Read Enhanced Feature Breakdown Tree**: Identify phase and dependencies
2. **Verify Phase Dependencies**: Ensure all previous phase requirements are met
3. **Read Context**: Study the relevant domain models and existing implementations
4. **Check Contracts**: Look for existing DTOs in contracts/
5. **Follow Patterns**: Use established patterns from similar services
6. **Implement Consistently**: Maintain naming and structure consistency
7. **Test Integration**: Ensure proper event flow and saga patterns
8. **Validate Phase Requirements**: Ensure feature meets phase-specific KPIs

### For Bug Fixes:
1. **Understand Context**: Read the relevant service documentation
2. **Study Patterns**: Look at similar implementations
3. **Maintain Consistency**: Follow established patterns
4. **Test Thoroughly**: Ensure changes don't break existing functionality

## 📞 Need Help?

- **Phase & Feature Questions**: Check `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`
- **Architecture Questions**: Check docs/architecture/
- **Implementation Patterns**: Study existing services
- **Domain Questions**: Review domain-models/
- **API Questions**: Check contracts/
- **Integration Questions**: Study saga-orchestrator/
- **AI Agent Orchestration**: Review the Enhanced Feature Breakdown Tree for agent assignments

Remember: **Context First, Phase-Aware Implementation Second**. Always understand the existing patterns and phase dependencies before creating new ones.
