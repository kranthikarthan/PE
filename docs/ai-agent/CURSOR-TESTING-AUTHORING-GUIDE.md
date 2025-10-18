### Cursor Testing Authoring Guide (Non-Drifting, Fast-Feedback)

This guide instructs AI agents (and humans) to author tests that compile and pass on first run, minimize rebuild cycles, and avoid documentation drift by aligning with the repository’s existing testing architecture.

---

### Scope and Goals
- **Prevent recompile/test-fix loops**: produce tests that run green or fail with actionable, deterministic messages.
- **Avoid drift**: reuse existing utilities, conventions, and plugin lifecycles already configured in the repo.
- **Stay consistent**: follow the project’s testing architecture and naming/placement rules.

---

### Architecture Alignment (Do Not Drift)
- **Build + Runners**: Maven with `maven-surefire-plugin` for unit tests and `maven-failsafe-plugin` for integration tests.
- **Profiles**: Tests run with `application-test.yml` in each module under `src/test/resources`.
- **Testcontainers**: Integration tests across services use Testcontainers where applicable.
- **Existing Docs to Honor**:
  - `docs/23-TESTING-ARCHITECTURE.md`
  - `docs/architecture/TESTING-ARCHITECTURE-SUMMARY.md`
  - Adapter-specific best practices (e.g. `SWIFT-ADAPTER-TEST-BEST-PRACTICES.md`)

Agents must prefer existing patterns from these documents over introducing new frameworks or styles.

---

### Test Types and Placement
- **Unit tests**: place under `src/test/java/...` alongside the module under test; run via Surefire. No containers, no network, no DB.
- **Integration tests**: place under `src/test/java/...` and name with `*IT` or reside in an `integration` package; run via Failsafe. Use Testcontainers or embedded infra as per module precedent.
- **Contract tests** (where present): follow current module conventions (e.g., `contract` packages already in repo).

---

### Naming and Structure
- **Method names**: `should<ExpectedBehavior>_When<StateUnderTest>`
- **AAA pattern**: Arrange – Act – Assert
- **Independence**: Tests must be order-independent, stateless across methods, and deterministic.
- **Fixtures**: Prefer builders/factories over hardcoded literals; reuse any existing test data builders if present.

Example skeleton (JUnit 5):
```java
@Test
void shouldRejectPayment_WhenInsufficientBalance() {
    // Arrange
    var request = validPaymentRequestBuilder().withAmount(bigDecimal("1000.00")).build();

    // Act
    var result = paymentService.validate(request);

    // Assert
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).contains("INSUFFICIENT_FUNDS");
}
```

---

### Fast-Feedback Rules (Minimize Recompiles)
- **Scope small**: add tests closest to the changed class first; avoid cross-module changes unless required.
- **Mock externally**: in unit tests, mock external collaborators; avoid Spring context if possible.
- **Limit context loads**: prefer slice tests or plain unit tests; only spin up `@SpringBootTest` for integration.
- **Deterministic data**: avoid `now()` without fixing clock; avoid randomness unless seeded.
- **Stable assertions**: assert on business values, not formatting or incidental details.

---

### Integration Testing Conventions
- **Annotation**: prefer `@SpringBootTest` for full wiring only when necessary; otherwise narrower slices.
- **Containers**: use Testcontainers images aligned with existing modules; do not introduce new images unless justified.
- **Isolation**: clean state before each test (`deleteAll` or truncate); unique resource names per test when touching external systems.
- **Config**: rely on `application-test.yml`; do not hardcode ports/hosts.

---

### Execution Contract for Cursor (Authoring Checklist)
Before writing or updating a test, the agent must:
1. Identify target module and class under test; confirm existing patterns in that module’s tests.
2. Choose test type: unit (Surefire) vs integration (Failsafe) based on dependencies.
3. Reuse existing builders/utilities and naming patterns from the module.
4. Ensure imports and dependencies already exist in the module’s `pom.xml`; if missing, add minimal required entries consistent with repo standards.
5. Place files under the correct package path and `src/test/java` tree.
6. For integration tests, prefer existing Testcontainers setup.

Pre-commit validation steps the agent must perform:
1. Run `mvn -q -DskipITs -pl <module> -am test` to validate unit tests compile and pass.
2. Run integration tests for that module only: `mvn -q -Dit.test=*IT -pl <module> -am verify`.
3. If failures occur, fix locally with minimal changes; avoid cascading edits across modules.

---

### Non-Drift Rules
- Do not alter global test plugins or CI defaults in root `pom.xml` unless explicitly requested.
- Do not introduce new test frameworks when JUnit 5/Mockito/AssertJ/Testcontainers already suffice.
- Keep configuration in `application-test.yml`; avoid environment-specific assumptions.
- Follow existing package structures (e.g., `integration`, `contract`, `unit`).

---

### Flakiness Prevention
- Time: fix the clock or inject a `Clock` bean in tests.
- Concurrency: use latches where necessary; assert eventual consistency with bounded waits.
- IO/External: stub/mocks for unit, short-lived containers for integration; avoid network calls outbound.

---

### When Adding New Tests
- Prefer unit tests first for new logic.
- Add integration tests only for new boundaries or when bugs escaped unit tests.
- Document any new builder/utilities in the module’s `README.md` if applicable.

---

### Commands Reference
- Unit only for a module (skip ITs):
```bash
mvn -q -DskipITs -pl <module> -am test
```
- Integration only for a module:
```bash
mvn -q -Dit.test=*IT -pl <module> -am verify
```
- All tests (repo):
```bash
mvn -q verify
```

---

### Definition of Done (for Tests Authored by Cursor)
- Tests compile and pass locally for the target module with the commands above.
- No new frameworks introduced; patterns match module precedent.
- No CI configuration changes required.
- Tests are deterministic and isolated; reruns are stable.
- Naming, placement, and structure follow this guide and existing docs.


