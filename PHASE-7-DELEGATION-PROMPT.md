## 🚀 PHASE 7 DELEGATION PROMPT — Operations & Channel Management

**Date**: October 20, 2025  
**Status**: Ready for Phase 7 Implementation  
**Target Agents**: Backend Ops APIs, Metrics, React Ops Portal, Channel/Clearing Onboarding  
**Estimated Duration**: 6-9 days (12 agents in parallel)  
**Priority**: P0

---

### 📋 Executive Summary

You are being delegated Phase 7: Operations & Channel Management. This phase closes the 60% gap for operations capabilities and delivers a React-based Operations Portal plus self-service onboarding for channels and clearing systems.

- **New Services**: #21 Operations Management, #22 Metrics Aggregation  
- **Enhanced Services**: Payment Initiation (#1), Saga Orchestrator (#6), Reporting (#15), Reconciliation (#12)  
- **React UIs**: Service Management, Payment Repair, Transaction Enquiries, Reconciliation & Monitoring, Channel Onboarding, Clearing System Onboarding  
- **APIs**: ~38 new `/api/ops/v1/*` endpoints  

Primary references (read first):
- `docs/implementation/PHASE-7-SUMMARY.md` — Phase 7 summary and KPIs
- `docs/40-PHASE-7-DETAILED-DESIGN.md` — Full design (APIs, UI structure, schemas, security, testing)
- `docs/implementation/feature-breakdown-tree-phase7.yaml` — Agent/task breakdown
- `NEW-AGENT-PROMPT.md` — Context-first workflow and guardrails

---

### 🧭 Scope & Objectives

1) Deliver two new microservices with complete APIs, tests, telemetry, and deployment.  
2) Enhance four existing services with `/ops/v1/*` endpoints and RBAC.  
3) Build the React Operations Portal and onboarding UIs aligned to API specs.  
4) Achieve testing and performance targets; integrate metrics, tracing, and alerting.  

Success criteria checklists are defined in `docs/implementation/PHASE-7-SUMMARY.md` and section 8/10 of `docs/40-PHASE-7-DETAILED-DESIGN.md`.

---

### 📚 Mandatory Reading (Open in this order)

1. `NEW-AGENT-PROMPT.md`  
2. `CURSOR-AGENT-CONTEXT.md`  
3. `AGENT-WORKFLOW.md`  
4. `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` (Phase dependencies)  
5. `docs/implementation/PHASE-7-SUMMARY.md`  
6. `docs/40-PHASE-7-DETAILED-DESIGN.md`  
7. `docs/implementation/feature-breakdown-tree-phase7.yaml`  
8. Testing references:  
   - `docs/23-TESTING-ARCHITECTURE.md`  
   - `docs/architecture/TESTING-ARCHITECTURE-SUMMARY.md`  
   - `CURSOR-RULES-PHASE-6-TESTING.md` and `.cursor/rules/testing-rules.mdc`

Architecture context (skim as needed):  
`docs/00-ARCHITECTURE-OVERVIEW.md`, `docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md`, `docs/architecture/COMPLETE-ARCHITECTURE-SUMMARY.md`, `docs/02-MICROSERVICES-BREAKDOWN.md`.

---

### 🧱 Prerequisites & Dependencies

- Phase 5 infrastructure in place: Istio, Prometheus, Grafana, Jaeger, Unleash, Kafka, PostgreSQL, Redis  
  Reference: `PHASE-5-INFRASTRUCTURE-DELEGATION-PROMPT.md`
- Phase 6 testing readiness and patterns: E2E, load/security/compliance guardrails  
  Reference: `CURSOR-RULES-PHASE-6-TESTING.md`, `.cursor/rules/testing-rules.mdc`
- Service contracts and domain models reused: `contracts/`, `domain-models/`

---

### 🗂️ Phase 7 Features → Agent Tasks

Use `docs/implementation/feature-breakdown-tree-phase7.yaml` for assignments. Deliverables include APIs, DTO alignment, validation, security, observability, unit/integration/E2E tests, and deployment manifests.

1) 7.1 Operations Management Service (NEW, #21)
- APIs: `/api/ops/v1/services`, `/circuit-breakers`, `/feature-flags`, service restarts/scaling  
- Integrations: K8s Java Client, Spring Boot Actuator, Resilience4j metrics, Unleash SDK  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (2.1, 4.1, 6, 7), security mapping (6.1, OpsSecurityConfig)  

2) 7.2 Metrics Aggregation Service (NEW, #22)
- APIs: `/api/ops/v1/metrics/*`, `/alerts/*`  
- Integrations: Prometheus (PromQL), WebFlux, Redis cache, TimescaleDB  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (2.2, 4.2, 7, 9)

3) 7.3 Payment Repair APIs (Enhance #1)
- APIs: `/api/ops/v1/payments/*` (failed list, retry, compensate, override, bulk)  
- Event: Republish `PaymentInitiatedEvent` on retry; audit trail  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (2.3)

4) 7.4 Saga Management APIs (Enhance #6)
- APIs: `/api/ops/v1/sagas/*` (pending, detail, history, resume, compensate)  
- State machine integration; audit, RBAC  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (2.4)

5) 7.5 Transaction Search APIs (Enhance #15)
- APIs: `/api/ops/v1/transactions/*` (search, detail, audit, events, export)  
- Elasticsearch integration and projections  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (2.5)

6) 7.6 Reconciliation Management APIs (Enhance #12)
- APIs: `/api/ops/v1/reconciliation/*` (unmatched, aging, manual/bulk match)  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (2.6)

7) 7.7 React Ops Portal — Service Management UI
- Pages/components: status grid, detail, circuit breaker, feature flags, pod management  
- Data: auto-refresh, role-based controls  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (3.1)

8) 7.8 React Ops Portal — Payment Repair UI  
9) 7.9 React Ops Portal — Transaction Enquiries UI  
10) 7.10 React Ops Portal — Reconciliation & Monitoring UI  
- Build per component structures and flows in `docs/40-PHASE-7-DETAILED-DESIGN.md` (3.2–3.4)

11) 7.11 Channel Onboarding UI  
12) 7.12 Clearing System Onboarding UI  
- 4-step channel wizard and 5-step clearing wizard, response patterns (Webhook, Kafka, WebSocket, Polling, Push), security configs  
- References: `docs/40-PHASE-7-DETAILED-DESIGN.md` (3.5–3.6) and `docs/39-CHANNEL-INTEGRATION-MECHANISMS.md`

---

### 🔐 Security & RBAC (Must Implement)

- Enforce JWT on all `/api/ops/v1/*` endpoints; map roles to permissions as in `docs/40-PHASE-7-DETAILED-DESIGN.md` (6.1).  
- Roles: `PLATFORM_ADMIN`, `OPS_ADMIN`, `OPS_OPERATOR`, `OPS_VIEWER`.  
- Mask sensitive data; add audit logs for ops actions (`operations_audit_log` schema).  

---

### 🗄️ Database & Schemas

- New tables: `channel_configurations`, `operations_audit_log`  
- Implement via Flyway/Liquibase migrations in relevant services.  
- Reference SQL: `docs/40-PHASE-7-DETAILED-DESIGN.md` (5.1).  

---

### 📦 Build, Run, and Local Dev (Cursor Agents)

1) Load context and rules  
   - Run one: `scripts/agent-context.ps1` | `scripts\agent-context.bat` | `./scripts/agent-context.sh`  
   - Read: `NEW-AGENT-PROMPT.md`, `AGENT-WORKFLOW.md`, `CURSOR-AGENT-CONTEXT.md`

2) Backend services  
   - Follow patterns from existing services (`payment-initiation-service`, etc.)  
   - Use constructor injection, Bean Validation, DTOs from `contracts/`, and structured logging.  
   - Add OpenAPI docs and health/metrics endpoints.

3) Frontend Ops Portal  
   - React 18 + TypeScript + MUI v5, React Query, Axios.  
   - Implement per component structure as specified; add tests with RTL + Cypress.

4) Observability  
   - Add Micrometer + Prometheus metrics and OpenTelemetry tracing as specified in design.  
   - Ensure dashboards can ingest new metrics; expose key counters/histograms.

---

### 🧪 Testing Requirements

Follow Phase 6 guardrails. Targets: >80% coverage overall; >95% critical paths.

- Unit tests: JUnit 5, Mockito, AssertJ, Spring test slices; RTL for React.  
- Integration tests: Testcontainers for Postgres/Redis; WireMock as needed.  
- E2E tests: Cypress specs for each UI flow (Payment Repair, Saga resume, Reconciliation matching, Channel/Clearing wizards).  
- Load testing: Validate 100 concurrent ops users; dashboard refresh at 5s; APIs p95 targets per KPIs.

References: `docs/23-TESTING-ARCHITECTURE.md`, `docs/architecture/TESTING-ARCHITECTURE-SUMMARY.md`, `CURSOR-RULES-PHASE-6-TESTING.md`.

---

### 📊 KPIs & SLAs (Selected)

Backend (per `docs/implementation/PHASE-7-SUMMARY.md`):
- Ops service health aggregation < 500 ms; feature flag toggle < 2 s; pod restart < 30 s  
- Transaction advanced search < 500 ms; export 1,000 records < 6 s  

Frontend:
- Dashboard load < 2 s; real-time updates < 5 s  
- Drag-and-drop matching < 2 s; wizard completion < 5 min  

---

### 🚀 Delivery Plan (Parallelization)

Weeks 1–2: Backend features (7.1–7.6) in parallel → publish OpenAPI, migrations, tests.  
Weeks 3–4: React Ops Portal + Monitoring + Channel/Clearing onboarding UIs in parallel.  
Week 5: Integration + E2E + Load tests; UAT with ops team.  
Week 6: Staging → prod (blue/green); monitor.

Dependency matrix in `docs/40-PHASE-7-DETAILED-DESIGN.md` (10.2) governs sequencing.

---

### ✅ Completion Checklist

Backend
- [ ] #21 Ops Mgmt service deployed with RBAC, metrics, tracing, alerts  
- [ ] #22 Metrics Aggregation service deployed with dashboards and alerts  
- [ ] Payment Repair, Saga Mgmt, Transaction Search, Reconciliation ops APIs live  

Frontend
- [ ] Service Mgmt, Payment Repair, Enquiries, Recon & Monitoring UIs live  
- [ ] Channel and Clearing onboarding UIs live with test connection  

Quality
- [ ] OpenAPI updated; >80% coverage; E2E/Cypress green; load tests within SLOs  
- [ ] Audit logs; no PII in logs; rate limiting; security review passed  

---

### 🔎 Deep Links (Key Sections)

- Architecture and flows: `docs/40-PHASE-7-DETAILED-DESIGN.md` (1.1–1.2)  
- Backend APIs and example code: `docs/40-PHASE-7-DETAILED-DESIGN.md` (2.x, 4.x)  
- React component structures: `docs/40-PHASE-7-DETAILED-DESIGN.md` (3.1–3.6)  
- Schemas: `docs/40-PHASE-7-DETAILED-DESIGN.md` (5.1)  
- Security & RBAC: `docs/40-PHASE-7-DETAILED-DESIGN.md` (6)  
- Deployment manifests & RBAC: `docs/40-PHASE-7-DETAILED-DESIGN.md` (7)  
- Testing strategy and examples: `docs/40-PHASE-7-DETAILED-DESIGN.md` (8)  
- Monitoring & tracing: `docs/40-PHASE-7-DETAILED-DESIGN.md` (9)  
- Roadmap & dependencies: `docs/40-PHASE-7-DETAILED-DESIGN.md` (10)

---

### 🧭 Guardrails (Do Not Violate)

- Reuse DTOs from `contracts/`; align with `domain-models/`.  
- Constructor injection only; Bean Validation on all inputs; meaningful error hierarchy.  
- Structured JSON logging; avoid sensitive data.  
- Follow established patterns in existing services; keep naming consistent.  
- Enforce RBAC and audit for all ops actions.  

Reference: `NEW-AGENT-PROMPT.md` (Critical Rules), user rules in project root.

---

### 📎 Appendix — Quick Commands

Context Loader (choose one):
```bash
powershell -ExecutionPolicy Bypass -File scripts/agent-context.ps1
scripts\agent-context.bat
./scripts/agent-context.sh
```

Run all tests (example):
```bash
mvn -q -T 1C -DskipITs=false -DskipUTs=false verify
```

Frontend (example):
```bash
cd web-bff-service && npm ci && npm run build && cd ../
# If Ops Portal is a separate app, follow docs/40-PHASE-7-DETAILED-DESIGN.md structure
```

---

This prompt aggregates everything required for agents to implement and test Phase 7 thoroughly. If a reference is missing, search the repo using the context-first workflow and align with the Phase 7 detailed design.


