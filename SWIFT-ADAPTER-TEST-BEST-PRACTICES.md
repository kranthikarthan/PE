# SWIFT Adapter Service - Spring Boot Testing Best Practices

## Overview

This document provides comprehensive guidelines for writing high-quality unit and integration tests for Spring Boot applications, with specific examples from the SWIFT Adapter Service.

---

## Table of Contents

1. [Test Pyramid](#test-pyramid)
2. [Unit Testing](#unit-testing)
3. [Integration Testing](#integration-testing)
4. [Test Data Management](#test-data-management)
5. [Mocking Best Practices](#mocking-best-practices)
6. [Exception Handling Tests](#exception-handling-tests)
7. [Test Organization](#test-organization)
8. [Naming Conventions](#naming-conventions)
9. [Common Anti-Patterns](#common-anti-patterns)
10. [Real Examples](#real-examples)

---

## Test Pyramid

### Definition

The test pyramid consists of three layers:

```
        /\
       /  \       End-to-End Tests (5%)
      /    \      - Slow
     /------\     - Expensive
    /        \    - Full environment
   /          \
  /____________\
 /              \    Integration Tests (15%)
/                \   - Medium speed
/                /   - Database/Container
\              /     - Multiple components
 \            /
  \__________/
   /        \      Unit Tests (80%)
  /          \     - Fast
 /            \    - Cheap
/              \   - Mocked dependencies
/______________\   - Single component
```

### Why It Matters

- **Unit Tests (80%)**: Write most tests here. They're fast and isolate logic.
- **Integration Tests (15%)**: Test component interactions with real dependencies.
- **End-to-End Tests (5%)**: Only test critical flows end-to-end.

---

## Unit Testing

### Definition

Unit tests verify a single class/method in isolation with all dependencies mocked.

### Key Principles

1. **One test, one behavior**
2. **Fast execution** (< 1 second per test)
3. **Deterministic** (no flakiness)
4. **Isolated** (no shared state)
5. **Independent** (can run in any order)

### Example: SwiftAdapterService Unit Test

```java
@ExtendWith(MockitoExtension.class)
class SwiftAdapterServiceTest {
  
  @Mock private SwiftAdapterRepository mockRepository;
  @Mock private TracingService mockTracingService;
  private SwiftAdapterService swiftAdapterService;
  
  @BeforeEach
  void setUp() {
    swiftAdapterService = new SwiftAdapterService(mockRepository, mockTracingService);
  }
  
  @Test
  @DisplayName("Should create adapter successfully with valid input")
  void shouldCreateAdapterSuccessfully() throws ExecutionException, InterruptedException {
    // ✓ GIVEN: Set up mocks and expected data
    var adapterId = ClearingAdapterId.generate();
    var expectedAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
        .withId(adapterId)
        .build();
    
    when(mockRepository.save(any(SwiftAdapter.class))).thenReturn(expectedAdapter);
    when(mockTracingService.executeInSpan(anyString(), anyMap(), any()))
        .thenAnswer(invocation -> invocation.getArgument(2, Supplier.class).get());
    
    // ✓ WHEN: Execute the behavior
    var result = swiftAdapterService.createAdapter(...).get();
    
    // ✓ THEN: Verify behavior
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(adapterId);
    verify(mockRepository, times(1)).save(any());
  }
}
```

### Test Structure (AAA Pattern)

```
Arrange  - Set up test data and mocks
Act      - Execute the code under test
Assert   - Verify the results
```

### Mocking Best Practices

```java
// ✓ GOOD: Mock external dependencies
@Mock private DatabaseService mockDb;
@Mock private HttpClient mockHttp;

// ✓ GOOD: Use Mockito for behavior verification
when(mockDb.findById(id)).thenReturn(Optional.of(entity));
verify(mockDb, times(1)).findById(id);

// ❌ BAD: Don't mock the class under test
@Mock private SwiftAdapterService mockService; // WRONG!

// ❌ BAD: Don't over-mock
// Mock only external dependencies, not internal logic
```

---

## Integration Testing

### Definition

Integration tests verify multiple components working together with real dependencies.

### When to Use

- Testing service + repository + database
- Testing API endpoints with real Spring context
- Testing cross-component interactions

### Example: SwiftAdapterIntegrationTest

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class SwiftAdapterIntegrationTest {
  
  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine");
  
  @Autowired private SwiftAdapterService service;
  @Autowired private SwiftAdapterRepository repository;
  
  @BeforeEach
  void setUp() {
    repository.deleteAll();  // ✓ Clean state between tests
  }
  
  @Test
  @DisplayName("Should create and persist adapter to database")
  void shouldCreateAndPersistAdapter() throws Exception {
    // ✓ Uses real database via Testcontainers
    var adapter = service.createAdapter(...).get();
    
    Optional<SwiftAdapter> found = repository.findById(adapter.getId());
    assertThat(found).isPresent();
  }
}
```

### Best Practices

```java
// ✓ GOOD: Test persistence
Optional<SwiftAdapter> found = repository.findById(adapterId);
assertThat(found).isPresent();

// ✓ GOOD: Clean up before each test
@BeforeEach
void setUp() {
  repository.deleteAll();
}

// ✓ GOOD: Use real dependencies
@Autowired private SwiftAdapterService service;

// ❌ BAD: Full Spring Boot test for logic that should be unit tested
@SpringBootTest
class SimpleBehaviorTest {
  // This should be a unit test with mocks!
}

// ❌ BAD: No cleanup between tests
@Test
void test1() { repository.save(adapter1); }
@Test
void test2() { /* adapter1 from test1 still exists! */ }
```

---

## Test Data Management

### Problem

```java
// ❌ BAD: Hardcoded test data everywhere
@Test
void shouldCreateAdapter() {
  SwiftAdapter adapter = SwiftAdapter.builder()
      .id(ClearingAdapterId.of("swift-test-001"))  // Hardcoded
      .adapterName("Test SWIFT Adapter")            // Hardcoded
      .endpoint("https://test.swift.com/api/v1")   // Hardcoded
      .apiVersion("1.0")                            // Hardcoded
      .timeoutSeconds(30)                           // Hardcoded
      .build();
}

// When domain model changes, must update ALL test classes!
```

### Solution: Test Data Builders

```java
// ✓ GOOD: Reusable test data builder
public class SwiftAdapterTestDataBuilder {
  public static SwiftAdapterTestDataBuilder aSwiftAdapter() {
    return new SwiftAdapterTestDataBuilder();
  }
  
  public SwiftAdapterTestDataBuilder withAdapterName(String name) {
    this.adapterName = name;
    return this;
  }
  
  public SwiftAdapter build() {
    return SwiftAdapter.builder()
        .id(id)
        .adapterName(adapterName)
        .endpoint(endpoint)
        .build();
  }
}

// ✓ Usage: Clean, readable, maintainable
@Test
void shouldCreateAdapter() {
  SwiftAdapter adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
      .withAdapterName("Custom Name")
      .build();
  
  assertThat(adapter.getAdapterName()).isEqualTo("Custom Name");
}
```

### Benefits

1. **Single source of truth** for test data
2. **Easy maintenance** when domain changes
3. **Clear intent** in tests
4. **Reduced duplication**

---

## Mocking Best Practices

### Mock vs Real

```java
// ✓ GOOD: Mock external HTTP calls
@Mock private FeignClient mockFeignClient;

@Test
void shouldHandleHttpError() {
  when(mockFeignClient.callExternalApi())
      .thenThrow(FeignException.class);
  
  assertThatThrownBy(() -> service.doSomething())
      .isInstanceOf(SomeException.class);
}

// ✓ GOOD: Real database in integration test
@SpringBootTest
@Testcontainers
class IntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>();
  
  @Autowired private Repository repository;
  
  @Test
  void shouldPersist() {
    repository.save(entity);
    // Uses REAL database
  }
}
```

### Mockito Patterns

```java
// ✓ GOOD: Verify behavior
when(repository.findById(id)).thenReturn(Optional.of(entity));
service.doSomething(id);
verify(repository).findById(id);

// ✓ GOOD: Multiple calls
when(repository.save(any()))
    .thenReturn(entity1)
    .thenReturn(entity2);

// ✓ GOOD: Exception throwing
when(repository.findById(id)).thenThrow(DatabaseException.class);

// ✓ GOOD: Argument matching
verify(repository).save(argThat(adapter ->
    adapter.getId().equals(expectedId) &&
    adapter.getStatus().equals(ACTIVE)
));

// ❌ BAD: Verifying same mock multiple times
verify(repository, times(2)).findById(id);
verify(repository, times(1)).save(any());
// Better: verify in one test per behavior
```

---

## Exception Handling Tests

### Testing Exceptions

```java
// ✓ GOOD: Assert exception is thrown
@Test
@DisplayName("Should throw exception when adapter not found")
void shouldThrowNotFoundWhenAdapterNotFound() {
  // Given
  when(repository.findById(id)).thenReturn(Optional.empty());
  
  // When & Then
  assertThatThrownBy(() ->
      service.updateAdapterConfiguration(id, ...)
  )
      .isInstanceOf(SwiftAdapterNotFoundException.class)
      .hasMessageContaining("not found");
}

// ✓ GOOD: Assert no exception
@Test
void shouldHandleEdgeCaseWithoutException() {
  assertThatNoException().isThrownBy(() ->
      service.doSomething(null)
  );
}

// ✓ GOOD: Chain assertions
assertThatThrownBy(() -> service.delete(id))
    .isInstanceOf(SwiftAdapterNotFoundException.class)
    .hasMessageContaining("id");

// ❌ BAD: Catching exception (hard to verify)
@Test
void shouldThrowException() {
  try {
    service.doSomething();
    fail("Should have thrown exception");  // Fragile
  } catch (Exception e) {
    // Hard to verify details
  }
}
```

---

## Test Organization

### Package Structure

```
swift-adapter-service/src/test/java/
├── com/payments/swiftadapter/
│   ├── unit/
│   │   ├── service/
│   │   │   └── SwiftAdapterServiceTest.java
│   │   ├── controller/
│   │   │   └── SwiftAdapterControllerTest.java
│   │   └── exception/
│   │       └── ExceptionHandlingTest.java
│   ├── integration/
│   │   └── SwiftAdapterIntegrationTest.java
│   └── fixtures/
│       ├── SwiftAdapterTestDataBuilder.java
│       └── TenantContextTestDataBuilder.java
└── resources/
    └── application-test.yml
```

### Nested Test Classes

```java
@DisplayName("SwiftAdapterService Tests")
class SwiftAdapterServiceTest {
  
  @Nested
  @DisplayName("Create Adapter Tests")
  class CreateAdapterTests {
    @Test void shouldCreateAdapter() { }
    @Test void shouldValidateInput() { }
  }
  
  @Nested
  @DisplayName("Update Adapter Tests")
  class UpdateAdapterTests {
    @Test void shouldUpdate() { }
    @Test void shouldThrowWhenNotFound() { }
  }
}
```

Benefits:
- Clear organization
- Better IDE navigation
- Logical grouping
- Professional test reports

---

## Naming Conventions

### Test Method Naming

```java
// ✓ GOOD: Clear, descriptive
@Test
@DisplayName("Should create adapter successfully with valid input")
void shouldCreateAdapterSuccessfullyWithValidInput() { }

// ✓ GOOD: Describes both action and expected result
@Test
void shouldThrowExceptionWhenAdapterNotFound() { }

@Test
void shouldReturnEmptyListWhenNoAdaptersExist() { }

// ❌ BAD: Too vague
@Test
void test1() { }

@Test
void testAdapter() { }

// ❌ BAD: Doesn't describe expected behavior
@Test
void adapterTest() { }
```

### Test Class Naming

```java
// ✓ GOOD: Test class ends with "Test"
class SwiftAdapterServiceTest { }

// ✓ GOOD: Integration test clearly labeled
class SwiftAdapterIntegrationTest { }

// ✓ GOOD: Specific test type
class SwiftAdapterControllerTest { }
class ExceptionHandlingTest { }

// ❌ BAD: Vague names
class SwiftAdapterTests { }
class ServiceTest { }
```

---

## Common Anti-Patterns

### Anti-Pattern 1: Testing Implementation Details

```java
// ❌ BAD: Testing internal implementation
@Test
void shouldCallRepositorySave() {
  service.create(adapter);
  verify(repository, times(1)).save(any());  // Over-specified
}

// ✓ GOOD: Test behavior
@Test
void shouldPersistAdapter() {
  SwiftAdapter created = service.create(adapter);
  assertThat(created).isNotNull();
  assertThat(created.getId()).isNotNull();
}
```

### Anti-Pattern 2: Test Interdependence

```java
// ❌ BAD: Tests depend on each other
@Test
void test1_Create() { id = service.create(...); }

@Test
void test2_Update() { service.update(id, ...); }  // Uses id from test1

// ✓ GOOD: Each test is independent
@Test
void shouldCreate() {
  var id = service.create(...);
  assertThat(id).isNotNull();
}

@Test
void shouldUpdate() {
  var id = service.create(...);  // Create fresh data
  service.update(id, ...);
}
```

### Anti-Pattern 3: Not Testing Edge Cases

```java
// ❌ BAD: Only happy path
@Test
void shouldCreate() {
  var adapter = service.create(validData);
  assertThat(adapter).isNotNull();
}

// ✓ GOOD: Test edge cases
@Test void shouldCreateWithValidInput() { }
@Test void shouldThrowWhenNameIsNull() { }
@Test void shouldThrowWhenEndpointIsEmpty() { }
@Test void shouldHandleMinimumTimeout() { }
@Test void shouldHandleMaximumTimeout() { }
```

### Anti-Pattern 4: Skipping Tests

```java
// ❌ BAD: Disabled tests
@Test
@Disabled("TODO: Fix this later")
void shouldHandleError() { }

// ✓ GOOD: Document why and create issue
@Test
@Disabled("Issue #123: Fix timeout handling")
void shouldHandleError() { }

// ✓ BETTER: Fix it now
@Test
void shouldHandleError() {
  when(repository.findById(id)).thenThrow(TimeoutException.class);
  assertThatThrownBy(() -> service.get(id))
      .isInstanceOf(TimeoutException.class);
}
```

---

## Real Examples

### Example 1: Controller Test with MockMvc

```java
@WebMvcTest(SwiftAdapterController.class)
class SwiftAdapterControllerTest {
  
  @Autowired private MockMvc mockMvc;
  @MockBean private SwiftAdapterService swiftAdapterService;
  
  @Test
  @DisplayName("Should create adapter and return 201 CREATED")
  void shouldCreateAdapterAndReturn201() throws Exception {
    // Given
    var createdAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().build();
    when(swiftAdapterService.createAdapter(any(), any(), anyString(), anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(createdAdapter));
    
    String requestJson = """
        {
          "tenantId": "tenant-001",
          "adapterName": "Test Adapter",
          "endpoint": "https://test.com"
        }
        """;
    
    // When & Then
    mockMvc.perform(post("/api/v1/swift-adapters")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.adapterName").value("Test SWIFT Adapter"));
  }
}
```

### Example 2: Exception Handling Test

```java
@ExtendWith(MockitoExtension.class)
class ExceptionHandlingTest {
  
  @Test
  @DisplayName("Should throw exception when adapter not found during update")
  void shouldThrowNotFoundExceptionWhenUpdatingNonExistent() {
    // Given
    var nonExistentId = ClearingAdapterId.generate();
    when(mockRepository.findById(nonExistentId)).thenReturn(Optional.empty());
    
    // When & Then
    assertThatThrownBy(() ->
        service.updateAdapterConfiguration(
            nonExistentId, "https://endpoint.com", "1.0", 30, 3, true, 100, "09:00", "17:00", "user")
    )
        .isInstanceOf(SwiftAdapterNotFoundException.class)
        .hasMessageContaining("not found");
  }
}
```

### Example 3: Test Data Builder Usage

```java
// Create adapter with defaults
SwiftAdapter adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().build();

// Create adapter with custom values
SwiftAdapter customAdapter = SwiftAdapterTestDataBuilder.aSwiftAdapter()
    .withAdapterName("Custom SWIFT")
    .withTimeoutSeconds(60)
    .withEncryptionEnabled(false)
    .inactive()
    .build();

// Use in test
@Test
void shouldUpdateInactiveAdapter() {
  SwiftAdapter inactive = SwiftAdapterTestDataBuilder.aSwiftAdapter().inactive().build();
  when(repository.findById(id)).thenReturn(Optional.of(inactive));
  
  SwiftAdapter updated = service.activateAdapter(id, "admin");
  assertThat(updated.isActive()).isTrue();
}
```

---

## Checklist for Writing Good Tests

- [ ] Test has a clear, descriptive name
- [ ] Follows AAA pattern (Arrange, Act, Assert)
- [ ] Tests one behavior per test method
- [ ] Mocks external dependencies, not class under test
- [ ] Uses test data builders for consistency
- [ ] Verifies behavior, not implementation details
- [ ] Handles both happy path and edge cases
- [ ] Tests exception scenarios
- [ ] Uses @DisplayName for clarity
- [ ] Organized with @Nested classes
- [ ] Fast execution (< 1 second for unit tests)
- [ ] No test interdependencies
- [ ] No hardcoded test data
- [ ] Proper setup/teardown in @BeforeEach
- [ ] Verifies mocks were called correctly

---

## Running Tests

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=SwiftAdapterServiceTest
```

### Run tests with coverage
```bash
mvn test jacoco:report
```

### Run integration tests only
```bash
mvn test -Dgroups=integration
```

---

## Test Coverage Goals

| Component | Target | Status |
|-----------|--------|--------|
| Unit Tests | 80% | ✓ |
| Integration Tests | 15% | ✓ |
| End-to-End Tests | 5% | ✓ |
| **Total** | **> 80%** | ✓ |

---

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [Test Driven Development](https://en.wikipedia.org/wiki/Test-driven_development)
