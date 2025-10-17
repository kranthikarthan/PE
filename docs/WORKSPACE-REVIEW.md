# Payments Engine – Workspace Review

Scope: Full repository review across architecture, code quality, dependencies, observability, security, CI/CD, tests, and deployment.

## Executive Summary

- Strengths
  - Clear domain-driven design with rich domain modules and aggregates.
  - Solid testing strategy with Testcontainers, Flyway schema verification, and JPA verification.
  - Robust CI with quality gates (Spotless, Checkstyle, SpotBugs, OWASP), coverage thresholds (JaCoCo), and matrix builds.
  - Good observability foundations (Actuator, Prometheus, OpenTelemetry), plus Docker/K8s manifests for full-stack local/dev.
  - Documentation depth is exceptional (architecture, patterns, flows, event schemas).

- Key Risks / Gaps
  - Dependency/version drift across modules (inconsistent Spring Boot and Springdoc versions).
  - Manual OpenTelemetry setup in multiple places; not unified on Spring Boot 3 Micrometer Tracing.
  - Docker Compose port mappings inconsistent with container `EXPOSE 8080` (health checks and ports mismatch).
  - Security posture not consistently enforced at service boundaries (no common `SecurityFilterChain` policy surfaced).
  - Event consistency: Outbox pattern referenced in docs but not implemented in runtime services.

- Top 5 Immediate Fixes
  1) Centralize versions in the parent POM and remove per-module overrides for Boot/Springdoc/OTel.
  2) Standardize observability on Micrometer Tracing + OTel bridge and exporters.
  3) Fix Docker Compose ports to map `<host>:8080` for all services; align health checks.
  4) Add base `SecurityFilterChain` config per service (public health, auth for APIs), even if permissive by profile.
  5) Implement transactional outbox for event publishing (Kafka) to ensure exactly-once semantics.

## Architecture & Design

- Monorepo with aggregator POM and multi-module microservices: `pom.xml:31` (Spring Boot BOM)
- Domain model modules: DDD aggregates for payment, transaction, validation, shared value objects.
- Services: Payment Initiation, Validation, Account Adapter, Routing, Transaction Processing, Saga Orchestrator, plus adapter services (SAMOS, SWIFT, BANKSERV, RTC, PAYSHAP).
- Event-driven with Kafka; rich event schema docs under `event-schemas/` and extensive architectural documentation under `docs/`.

Observations
- DDD implementation is strong (entity modeling, value objects, domain events). Example: `domain-models/payment-initiation/.../Payment.java:1` enforces state transitions and records domain events.
- Some JPA entities use Lombok `@Data` (not ideal for JPA). Example: `domain-models/transaction-processing/.../Transaction.java:19`. Prefer explicit getters/setters and controlled equals/hashCode.
- Saga orchestration module exists; ensure compensation workflows align with event contracts and idempotency.

Recommendations
- Introduce a shared “platform-starter” (internal BOM + auto-config) for common service wiring (logging, tracing, metrics, security, error handling) to eliminate drift.
- Enforce event versioning strategy in code (publish headers and payload metadata consistently) to match `event-schemas`.

## Build & Dependency Management

Findings
- Parent Spring Boot property: `pom.xml:31` → 3.2.5. Child overrides exist, e.g., Payment Initiation sets 3.2.0 at `payment-initiation-service/pom.xml:23`.
- Springdoc property in parent: `pom.xml:38` → 2.3.0, but some modules pin 2.5.0, e.g., `routing-service/pom.xml:121` (+ line 122 shows version tag 2.5.0).
- OTel libs pinned per-module in several services rather than inherited from a single place.

Recommendations
- Centralize all versions via the parent POM’s `<dependencyManagement>` and remove child overrides (Boot, Springdoc, OTel, Jackson, Lombok, Testcontainers, Resilience4j).
- Keep all Spring Boot starters unversioned (inherit from Boot BOM) and align Spring Cloud via imported BOM only (avoid per-dependency versions in `shared-config/pom.xml`).
- Consider a repository BOM module (e.g., `payments-bom`) so all modules import the same pinned versions cleanly.

## Configuration & Profiles

Findings
- `shared-config` adds Spring Cloud Config, Vault, Consul, Kubernetes Config starters directly, with explicit versions.
- Secrets helper present in `shared-config` (`SecretManager.java`) using custom javax.crypto routines.

Recommendations
- Gate heavy config dependencies behind profiles to avoid pulling them where not needed.
- Prefer standard secret providers (Vault/Jasypt) and Spring’s property encryption over custom crypto. If keeping `SecretManager`, narrow scope and ensure FIPS-friendly algorithms and rotations are externalized.

## Observability

Findings
- Mixed approach: direct OpenTelemetry SDK config in `shared-telemetry` (`TracingConfig.java`) and service-local OTel dependencies. Prometheus registry present.

Recommendations
- Standardize on Spring Boot 3 Micrometer Tracing with OTel bridge: `micrometer-tracing-bridge-otel` + `otel exporter` (Zipkin/OTLP/Jaeger), configured via properties. Remove direct SDK wiring unless truly required.
- Provide a minimal auto-config that registers correlation IDs (trace/span) and consistent logging pattern JSON encoder.

## Security

Findings
- Limited visible enforcement of authentication/authorization for public APIs; mostly actuator endpoints present.
- OAuth2 references exist in adapters (e.g., account adapter), but not consistently across services.

Recommendations
- Add a baseline `SecurityFilterChain` per service to restrict everything by default, then explicitly allow actuator health/info and desired API routes. Provide profiles to relax for local/dev.
- Centralize security defaults (CORS, content security headers, CSRF strategy for APIs) in a shared starter.
- Use properties/secret stores for credentials; scrub any defaults from docs intended for non-local environments.

## Data & Migrations

Findings
- Flyway SQL migrations per service and shared verification in `schema-verification` using Postgres Testcontainers.
- Domain model and schema alignment is verified; JPA verification module present.

Recommendations
- Keep migration naming and ownership per bounded context; avoid cross-service schema entanglement.
- For high-throughput flows, add outbox tables and migrations for event publishing.

## Messaging & Events

Findings
- Kafka listeners/producers are set up (e.g., `validation-service` Kafka config) with JSON serialization. Group IDs and manual acks are configured.
- Documentation includes outbox pattern; not visibly implemented in runtime modules.

Recommendations
- Implement the transactional outbox pattern: persist event records within the same DB transaction and use a background publisher to Kafka. This ensures exactly-once across failures.
- Standardize Kafka config in a shared auto-config with sane defaults (retries, idempotence, consumer error handling, dead letter topics, headers for tracing/tenant context).

## Testing & Quality Gates

Findings
- Unit tests and integration tests present with Testcontainers for Postgres/Kafka.
- JaCoCo coverage rule at parent POM (80% instruction coverage) and solid code quality gates in CI.

Recommendations
- Add contract tests for public APIs per service using REST Assured + Spring MockMvc slices; generate and verify OpenAPI schemas.
- Consider consumer-driven contract tests where applicable (e.g., Pact/CDC) for inter-service integration.

## CI/CD

Findings
- `.github/workflows/ci.yml` implements quality gates, compile matrix (Java 17/21), unit tests per module, integration tests with services, coverage and packaging.
- Codecov upload present; potential minor encoding artifacts in echo output.

Recommendations
- Add caching for Maven wrapper if used; consider build scan publishing if allowed.
- Consider adding SAST/secret scanning (e.g., Gitleaks) and license scanning.
- Optionally integrate SonarQube for unified dashboards if available.

## Containers & Orchestration

Findings
- Dockerfiles are multi-stage and correct; all services `EXPOSE 8080` (e.g., `docker/validation-service/Dockerfile:53`, `docker/routing-service/Dockerfile:51`).
- Docker Compose port mappings are inconsistent with container ports and health checks:
  - Validation Service maps `8082:8082` with health on 8080 (`docker-compose.yml:129`, `docker-compose.yml:141`).
  - Routing Service maps `8084:8084` (`docker-compose.yml:197`) with health on 8084; container exposes 8080.
  - Transaction Processing maps `8085:8084` with health on 8085 (`docker-compose.yml:231`, `docker-compose.yml:243`).
  - Saga Orchestrator maps `8086:8086` (`docker-compose.yml:265`) with container `EXPOSE 8080` by default.

Recommendations
- Standardize all Compose mappings as `<host>:8080` for each service unless `server.port` is explicitly set in service properties.
- Keep health checks consistent with container port 8080.
- Validate Kubernetes manifests are aligned (they currently expose 8080 and use `/actuator/health`).

## Code-level Observations & Suggestions

- Controllers
  - Avoid per-endpoint try/catch and defer to a global exception handler. Example: `payment-initiation-service/src/main/java/.../PaymentInitiationController.java:1` can rely entirely on the global handler to simplify controller code and standardize error responses.

- Entities
  - Replace Lombok `@Data` on JPA entities (e.g., `domain-models/transaction-processing/.../Transaction.java:19`) with explicit annotations (`@Getter/@Setter`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`) to avoid unintended equals/hashCode pitfalls.

- Telemetry
  - Consolidate tracing into a shared auto-config with Micrometer Tracing and exporters set via properties. Remove manual OpenTelemetry SDK plumbing from services where possible.

- Contracts
  - Align `contracts` module dependency versions with parent POM. Avoid pinning Jackson/Swagger versions in the child if parent BOMs manage them.

## Prioritized Action Plan

- Immediate (Week 1–2)
  - Align versions in parent POM and remove child overrides for Boot/Springdoc/OTel (refs: `pom.xml:31`, `pom.xml:38`, `payment-initiation-service/pom.xml:23`, `routing-service/pom.xml:121`).
  - Fix Docker Compose port mappings and health checks (refs: `docker-compose.yml:129`, `docker-compose.yml:141`, `docker-compose.yml:197`, `docker-compose.yml:231`, `docker-compose.yml:243`, `docker-compose.yml:265`).
  - Add a baseline `SecurityFilterChain` per service with profile-based policies.

- Short Term (Weeks 3–6)
  - Introduce Micrometer Tracing + OTel bridge in `shared-telemetry`; remove direct SDK in services.
  - Implement transactional outbox for event publishing; add Flyway migrations and publishers.
  - Add API contract tests per service; generate OpenAPI docs in CI.

- Medium Term (Quarter)
  - Create a shared internal platform starter (BOM + auto-config) for logging, tracing, metrics, security, error handling, Kafka config.
  - Expand SAST/secret scanning; consider SonarQube or similar.
  - Harden secret management using Vault/KMS; phase out any custom crypto except for limited, vetted use cases.

## File References (Examples)

- Parent Spring Boot version: `pom.xml:31`
- Parent Springdoc version: `pom.xml:38`
- Child Spring Boot override: `payment-initiation-service/pom.xml:23`
- Child Springdoc override: `routing-service/pom.xml:121`
- Dockerfiles expose port 8080: `docker/validation-service/Dockerfile:53`, `docker/routing-service/Dockerfile:51`
- Docker Compose mismatches: `docker-compose.yml:129`, `docker-compose.yml:141`, `docker-compose.yml:197`, `docker-compose.yml:231`, `docker-compose.yml:243`, `docker-compose.yml:265`
- Lombok `@Data` on JPA: `domain-models/transaction-processing/src/main/java/com/payments/domain/transaction/Transaction.java:19`

---

Prepared by: Principal Spring Boot Architect – Repository review and remediation plan
Date: (auto-generated)

