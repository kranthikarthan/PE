### Phase 4 Build Execution Playbook (Cursor)

Purpose: Provide a precise, non-drifting plan for Cursor to implement Phase 4 features with first-try green tests, aligned to existing repo conventions and testing guide.

References (authoritative):
- `docs/34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`
- `docs/02-MICROSERVICES-BREAKDOWN.md`
- `docs/00-ARCHITECTURE-OVERVIEW.md`
- `docs/03-EVENT-SCHEMAS.md`
- `docs/14-DDD-IMPLEMENTATION.md`
- `docs/28-BATCH-PROCESSING.md`
- `docs/ai-agent/CURSOR-TESTING-AUTHORING-GUIDE.md`
- `COPY-PASTE-PROMPT.md`

Do not drift from these sources. If conflicts arise, prefer `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md` for scope, then `02-MICROSERVICES-BREAKDOWN.md` for service-level details.

---

### Non-Drift Guardrails
- Reuse existing build/test plugins: Maven `surefire` (unit) and `failsafe` (integration).
- Follow `docs/ai-agent/CURSOR-TESTING-AUTHORING-GUIDE.md` for test placement, naming, AAA pattern, and deterministic tests.
- Use `application-test.yml` per module; do not hardcode ports/hosts.
- Prefer existing patterns (JUnit 5, Mockito, AssertJ, Testcontainers). Do not introduce new frameworks.
- Only add minimal `pom.xml` entries needed; no root changes unless explicitly instructed.

---

### Execution Order (inside Phase 4)
- Parallel friendly. Recommended sequencing (if needed):
  1) `4.2 Settlement` before `4.3 Reconciliation` (feeds data),
  2) BFFs (`4.5-4.7`) after core service endpoints they aggregate exist.
  3) `4.4 Internal API Gateway` is optional per gateway clarification; only build if required.

---

### Build and Test Commands (per module)
- Unit tests only:
```bash
mvn -q -DskipITs -pl <module> -am test
```
- Integration tests only (Failsafe, `*IT`):
```bash
mvn -q -Dit.test=*IT -pl <module> -am verify
```
- All tests (repo):
```bash
mvn -q verify
```

---

### 4.1 Batch Processing Service
- Inputs: `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`, `02-MICROSERVICES-BREAKDOWN.md`, `28-BATCH-PROCESSING.md`, `14-DDD-IMPLEMENTATION.md`
- Suggested module path: `services/batch-processing-service`
- Key scope: Spring Batch job(s) to process bulk files (CSV/Excel/XML/JSON), optional SFTP, parallel chunks.
- DoD:
  - REST endpoints to submit batch and query status
  - Spring Batch job with chunked processing and retry
  - Idempotency for re-uploads
  - 80%+ unit coverage; integration tests with Testcontainers (PostgreSQL)
- KPIs:
  - Process 10K+ records/file
  - p95 step duration < target in docs
- Tests:
  - Unit: job configuration, mappers, processors
  - IT: end-to-end job execution against Postgres (Testcontainers)
- Commands:
  - Unit: `mvn -q -DskipITs -pl services/batch-processing-service -am test`
  - IT: `mvn -q -Dit.test=*IT -pl services/batch-processing-service -am verify`

### 4.2 Settlement Service
- Inputs: `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`, `02-MICROSERVICES-BREAKDOWN.md`, `00-ARCHITECTURE-OVERVIEW.md`, `03-EVENT-SCHEMAS.md`, `14-DDD-IMPLEMENTATION.md`
- Suggested module path: `services/settlement-service`
- Key scope: settlement batches, net positions, finalize/complete flows.
- DoD:
  - Endpoints: create batch, finalize batch, query positions
  - Persistence schema from `02-MICROSERVICES-BREAKDOWN.md`
  - Events published per `03-EVENT-SCHEMAS.md`
  - 80%+ unit coverage; IT with Postgres
- KPIs:
  - Batch creation latency and netting correctness per docs
- Tests:
  - Unit: calculations, state transitions
  - IT: batch lifecycle with Testcontainers (Postgres)
- Commands:
  - Unit: `mvn -q -DskipITs -pl services/settlement-service -am test`
  - IT: `mvn -q -Dit.test=*IT -pl services/settlement-service -am verify`

### 4.3 Reconciliation Service
- Inputs: `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`, `02-MICROSERVICES-BREAKDOWN.md`, `00-ARCHITECTURE-OVERVIEW.md`, `03-EVENT-SCHEMAS.md`
- Suggested module path: `services/reconciliation-service`
- Key scope: match internal transactions with clearing responses, exception handling, reports.
- DoD:
  - Reconciliation run endpoint, results APIs, exceptions listing
  - DB schema as per docs (runs, exceptions)
  - 80%+ unit coverage; IT validation of matching logic
- KPIs:
  - Match rate and execution duration targets from docs
- Tests:
  - Unit: matching algorithms with edge cases
  - IT: end-to-end reconciliation with seeded data (Postgres)
- Commands:
  - Unit: `mvn -q -DskipITs -pl services/reconciliation-service -am test`
  - IT: `mvn -q -Dit.test=*IT -pl services/reconciliation-service -am verify`

### 4.4 Internal API Gateway Service (Optional)
- Inputs: `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`, `00-ARCHITECTURE-OVERVIEW.md` (gateway clarification)
- Build only if mesh features are not available (Istio not deployed).
- Suggested module path: `services/internal-api-gateway`
- DoD:
  - Route configs for internal services; rate limiters; circuit breakers
  - Security for internal-only routes
  - Contract tests for routes
- KPIs:
  - Route latency and error budgets per docs
- Tests:
  - Unit: route predicates/filters
  - Contract: WireMock-based routing expectations
- Commands:
  - Unit: `mvn -q -DskipITs -pl services/internal-api-gateway -am test`

### 4.5 Web BFF - GraphQL
- Inputs: `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`, `02-MICROSERVICES-BREAKDOWN.md`
- Suggested module path: `bff/web-bff-graphql`
- Key scope: GraphQL schema, resolvers aggregating payment/account data.
- DoD:
  - GraphQL schema with queries/mutations per docs
  - Resolvers calling underlying services
  - 80%+ unit coverage; IT for resolver integration (Testcontainers if needed)
- KPIs:
  - Resolver latency targets (p95)
- Tests:
  - Unit: resolver logic
  - IT: GraphQL queries against test app context
- Commands:
  - Unit: `mvn -q -DskipITs -pl bff/web-bff-graphql -am test`
  - IT: `mvn -q -Dit.test=*IT -pl bff/web-bff-graphql -am verify`

### 4.6 Mobile BFF - REST (Lightweight)
- Inputs: `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`, `02-MICROSERVICES-BREAKDOWN.md`
- Suggested module path: `bff/mobile-bff-rest`
- Key scope: minimal, mobile-optimized payloads; caching where appropriate.
- DoD:
  - Lean endpoints returning essential fields only
  - 80%+ unit coverage; IT for endpoint behavior
- KPIs:
  - p95 < 200ms; payload size < 5KB
- Tests:
  - Unit: controllers/services
  - IT: HTTP-level tests with Testcontainers Postgres if needed
- Commands:
  - Unit: `mvn -q -DskipITs -pl bff/mobile-bff-rest -am test`
  - IT: `mvn -q -Dit.test=*IT -pl bff/mobile-bff-rest -am verify`

### 4.7 Partner BFF - REST (Comprehensive)
- Inputs: `34-FEATURE-BREAKDOWN-TREE-ENHANCED.md`, `02-MICROSERVICES-BREAKDOWN.md`
- Suggested module path: `bff/partner-bff-rest`
- Key scope: comprehensive payloads, rate limiting, resilience.
- DoD:
  - Endpoints with full detail aggregation
  - RateLimiter/CB configured per docs
  - 80%+ unit coverage; IT with WireMock for upstream calls
- KPIs:
  - p95 < 500ms; rate limiting thresholds enforced
- Tests:
  - Unit: controllers/services, resilience behavior
  - Contract: WireMock stubs for dependent services
- Commands:
  - Unit: `mvn -q -DskipITs -pl bff/partner-bff-rest -am test`
  - IT: `mvn -q -Dit.test=*IT -pl bff/partner-bff-rest -am verify`

---

### Common Testing Checklist (apply to all Phase 4 modules)
1) Follow `CURSOR-TESTING-AUTHORING-GUIDE.md` strictly (naming, AAA, determinism).
2) Unit first, then integration. Keep tests independent and order-agnostic.
3) Testcontainers for Postgres/Redis only when needed; reuse existing images/config.
4) Avoid external calls in unit tests; use mocks/WireMock.
5) Ensure green on module-only runs before repo-wide verify.

---

### Definition of Done (Phase 4, aggregate)
- All Phase 4 modules compile and pass unit + integration tests in isolation.
- No root build config changes required; module `pom.xml` only.
- KPIs per feature documented and verified by tests where feasible.
- No new frameworks introduced; conventions consistent with repo.


