# Temporary disables and re-enable plan (Phase 4)

Keep this list updated so we can restore everything to production-grade defaults.

## audit-service
- Controller slice tests:
  - Disabled: Full auto-config; using `@WebMvcTest` with exclusions (security, persistence, Kafka) and standalone MockMvc.
  - Re-enable plan: Switch to `@AutoConfigureMockMvc(addFilters = true)` with security enabled; keep slice scope minimal but avoid excluding core auto-config. Add Testcontainers-backed Kafka for E2E (no mocks).

## batch-processing-service
- Spring Cloud Config in tests:
  - Disabled: Strict import check by using `spring.config.import="optional:configserver:,optional:consul:"`.
  - Re-enable plan: Provide a Config Server stub/Testcontainers for tests or move config to local `application-test.yml` and remove optional import.
- Controller slice tests:
  - Disabled: Security auto-config excluded; using `@WithMockUser` + `csrf()`.
  - Re-enable plan: Keep security filters active in slices (`addFilters = true`) and configure permitted endpoints as needed.

## settlement-service
- Vault in tests:
  - Disabled: `spring.cloud.vault.enabled=false` in `application-test.yml`.
  - Re-enable plan: Add Vault Testcontainers or profile-based conditional config; only disable where not required.
- Spring Cloud Config in tests:
  - Disabled: `spring.config.import="optional:configserver:,optional:consul:"`.
  - Re-enable plan: Same as batch — provide stub/config or consolidate test config locally.
- Event publisher selection:
  - Temporary: `KafkaSettlementEventPublisher` marked `@Primary` over in-memory.
  - Hardening plan: Replace with `@Profile`/`@ConditionalOnProperty` split: Kafka vs in-memory per environment.

## reconciliation-service
- Vault in tests:
  - Disabled: `spring.cloud.vault.enabled=false` via `@DynamicPropertySource`/`application-test.yml`.
  - Re-enable plan: Add Vault Testcontainers or conditional profile; scope disables to tests only.
- Spring Cloud Config in tests:
  - Disabled: Optional config imports.
  - Re-enable plan: Same as above services.
- Controller slice tests:
  - Disabled: Security auto-config excluded; using `@WithMockUser` + `csrf()`.
  - Re-enable plan: Enable security filters in slices and adjust authorities.

## cross-cutting
- Test security filters in slices:
  - Disabled across multiple services via excluding `SecurityAutoConfiguration`/`SecurityFilterAutoConfiguration`.
  - Re-enable plan: Use `@AutoConfigureMockMvc(addFilters = true)` and configure `@WithMockUser` roles/authorities; keep endpoints secure.
- Config server dependency in tests:
  - Disabled via optional imports to avoid runtime dependency.
  - Re-enable plan: Introduce a lightweight config stub or Testcontainers-based config server if required; otherwise keep all needed config in `application-test.yml`.

## follow-ups
- Replace `@Primary` bean selection with `@Profile`/`@ConditionalOnProperty` for all in-memory vs Kafka publishers.
- Add Testcontainers Vault support or refactor to avoid Vault during tests when not under security-sensitive paths.
- Consolidate test config: minimize dynamic property overrides scattered in tests; prefer `application-test.yml`.

> Note: Do not commit disables to production profiles. Keep them test-only and temporary.
