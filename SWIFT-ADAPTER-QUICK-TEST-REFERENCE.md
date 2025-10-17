# SWIFT Adapter - Quick Test Reference Guide

Quick lookup for common testing patterns and solutions.

---

## 🚀 Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=SwiftAdapterServiceTest

# Unit tests only
mvn test -Dtest=**/unit/**

# Integration tests only  
mvn test -Dtest=**/integration/**

# With coverage
mvn test jacoco:report
```

---

## 📋 Test Template (Unit Test)

```java
@ExtendWith(MockitoExtension.class)
class SwiftAdapterServiceTest {
  
  @Mock private SwiftAdapterRepository mockRepository;
  @Mock private TracingService mockTracingService;
  private SwiftAdapterService service;
  
  @BeforeEach
  void setUp() {
    service = new SwiftAdapterService(mockRepository, mockTracingService);
  }
  
  @Test
  @DisplayName("Should create adapter successfully")
  void shouldCreateAdapterSuccessfully() {
    // Arrange
    var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().build();
    when(mockRepository.save(any())).thenReturn(adapter);
    
    // Act
    var result = service.createAdapter(...);
    
    // Assert
    assertThat(result).isNotNull();
    verify(mockRepository, times(1)).save(any());
  }
}
```

---

## 📋 Test Template (Controller Test)

```java
@WebMvcTest(SwiftAdapterController.class)
class SwiftAdapterControllerTest {
  
  @Autowired private MockMvc mockMvc;
  @MockBean private SwiftAdapterService service;
  
  @Test
  @DisplayName("Should return 200 OK")
  void shouldReturnOk() throws Exception {
    // Given
    when(service.getAdapter(any())).thenReturn(Optional.of(adapter));
    
    // When & Then
    mockMvc.perform(get("/api/v1/swift-adapters/id-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.adapterName").value("Test"));
  }
}
```

---

## 🔨 Test Data Builder

```java
// Default values
SwiftAdapter adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().build();

// Custom values
SwiftAdapter custom = SwiftAdapterTestDataBuilder.aSwiftAdapter()
    .withAdapterName("Custom")
    .withTimeoutSeconds(60)
    .withEncryptionEnabled(false)
    .inactive()
    .build();

// Tenant context
TenantContext context = TenantContextTestDataBuilder.aTenantContext()
    .withTenantId("custom-tenant")
    .build();
```

---

## ✅ Common Assertions

```java
// Null checks
assertThat(result).isNotNull();
assertThat(result).isNull();

// Equality
assertThat(result).isEqualTo(expected);
assertThat(result).isNotEqualTo(other);

// Collections
assertThat(list).isEmpty();
assertThat(list).hasSize(5);
assertThat(list).contains(item);
assertThat(list).containsExactlyInAnyOrder(item1, item2);

// Strings
assertThat(text).isNotBlank();
assertThat(text).startsWith("prefix");
assertThat(text).contains("substring");

// Optional
assertThat(optional).isPresent();
assertThat(optional).isEmpty();

// Numbers
assertThat(number).isPositive();
assertThat(number).isGreaterThan(5);
assertThat(number).isLessThanOrEqualTo(10);

// Booleans
assertThat(flag).isTrue();
assertThat(flag).isFalse();

// Exceptions
assertThatThrownBy(() -> service.delete(id))
    .isInstanceOf(NotFoundException.class)
    .hasMessageContaining("not found");

assertThatNoException().isThrownBy(() -> service.create());
```

---

## 🎯 Mockito Cheat Sheet

```java
// Setup mocks
@Mock private Repository repo;
@Mock private ExternalService external;

// Return values
when(repo.findById(id)).thenReturn(Optional.of(entity));
when(repo.save(any())).thenReturn(savedEntity);
when(repo.findAll()).thenReturn(List.of(entity1, entity2));

// Throw exceptions
when(repo.delete(id)).thenThrow(DatabaseException.class);

// Multiple returns
when(repo.save(any()))
    .thenReturn(entity1)
    .thenReturn(entity2);

// Argument matching
when(repo.save(argThat(e -> e.getId().equals(id))))
    .thenReturn(entity);

// Verify called
verify(repo, times(1)).findById(id);
verify(repo, never()).delete(any());
verify(repo, atLeastOnce()).save(any());

// Verify order
InOrder inOrder = inOrder(repo, service);
inOrder.verify(repo).findById(id);
inOrder.verify(service).process();

// Answer (custom behavior)
when(repo.save(any()))
    .thenAnswer(invocation -> {
      SwiftAdapter adapter = invocation.getArgument(0);
      adapter.setId(ClearingAdapterId.generate());
      return adapter;
    });
```

---

## 🌐 MockMvc HTTP Testing

```java
// GET request
mockMvc.perform(get("/api/v1/adapters/123"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id").value("123"));

// POST request
mockMvc.perform(post("/api/v1/adapters")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"name":"Test"}"""))
    .andExpect(status().isCreated());

// PUT request
mockMvc.perform(put("/api/v1/adapters/123")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
    .andExpect(status().isOk());

// DELETE request
mockMvc.perform(delete("/api/v1/adapters/123"))
    .andExpect(status().isNoContent());

// Status codes
.andExpect(status().isOk())              // 200
.andExpect(status().isCreated())         // 201
.andExpect(status().isNoContent())       // 204
.andExpect(status().isBadRequest())      // 400
.andExpect(status().isNotFound())        // 404
.andExpect(status().isInternalServerError()) // 500

// JSON assertions
.andExpect(jsonPath("$.id").value("123"))
.andExpect(jsonPath("$.name").value("Test"))
.andExpect(jsonPath("$.items").isArray())
.andExpect(jsonPath("$.items.length()").value(2))
.andExpect(jsonPath("$.items[0].id").value("1"))

// Headers
.andExpect(header().exists("Location"))
.andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
```

---

## 🔍 Common Test Scenarios

### Test Happy Path
```java
@Test
void shouldCreateAdapterSuccessfully() {
  when(repo.save(any())).thenReturn(adapter);
  var result = service.create(adapter);
  assertThat(result).isNotNull();
  verify(repo).save(any());
}
```

### Test Not Found
```java
@Test
void shouldThrowNotFoundWhenAdapterDoesNotExist() {
  when(repo.findById(id)).thenReturn(Optional.empty());
  assertThatThrownBy(() -> service.update(id, data))
      .isInstanceOf(NotFoundException.class);
}
```

### Test Validation
```java
@Test
void shouldRejectNullInput() {
  assertThatThrownBy(() -> service.create(null))
      .isInstanceOf(NullPointerException.class);
}
```

### Test Exception Handling
```java
@Test
void shouldHandleDatabaseException() {
  when(repo.save(any())).thenThrow(DatabaseException.class);
  assertThatThrownBy(() -> service.create(adapter))
      .isInstanceOf(ServiceException.class);
}
```

### Test Persistence
```java
@Test
void shouldPersistToDatabse() {
  repository.save(adapter);
  Optional<SwiftAdapter> found = repository.findById(adapter.getId());
  assertThat(found).isPresent();
}
```

---

## 🏗️ Test Organization with @Nested

```java
@DisplayName("SwiftAdapterService")
class SwiftAdapterServiceTest {
  
  @Nested
  @DisplayName("Create")
  class CreateTests {
    @Test void shouldCreate() { }
    @Test void shouldThrowWhenNull() { }
  }
  
  @Nested
  @DisplayName("Update")
  class UpdateTests {
    @Test void shouldUpdate() { }
    @Test void shouldThrowWhenNotFound() { }
  }
}
```

---

## 📝 Test Naming

```java
// ✅ GOOD
void shouldCreateAdapterWithValidInput() { }
void shouldThrowExceptionWhenAdapterNotFound() { }
void shouldReturnEmptyListWhenNoAdaptersExist() { }

// ❌ BAD
void test1() { }
void testAdapter() { }
void adapterTest() { }
```

---

## 🔄 Test Lifecycle

```java
class TestExample {
  
  // Runs once before all tests
  @BeforeAll
  static void setupClass() { }
  
  // Runs before each test
  @BeforeEach
  void setup() {
    mockRepository = mock(Repository.class);
    service = new Service(mockRepository);
  }
  
  @Test
  void testSomething() { }
  
  // Runs after each test
  @AfterEach
  void cleanup() {
    reset(mockRepository);
  }
  
  // Runs once after all tests
  @AfterAll
  static void teardownClass() { }
}
```

---

## 🎨 Parameterized Tests

```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "null"})
void shouldRejectInvalidInput(String input) {
  assertThatThrownBy(() -> service.create(input))
      .isInstanceOf(InvalidInputException.class);
}

@ParameterizedTest
@CsvSource({
  "1,    ACTIVE",
  "2,    INACTIVE",
  "3,    PENDING"
})
void shouldMapStatus(int code, String status) {
  assertThat(mapper.toStatus(code)).isEqualTo(status);
}
```

---

## 🛠️ Integration Test Template

```java
@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
class SwiftAdapterIntegrationTest {
  
  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine");
  
  @Autowired private SwiftAdapterService service;
  @Autowired private SwiftAdapterRepository repository;
  
  @BeforeEach
  void setUp() {
    repository.deleteAll();
  }
  
  @Test
  void shouldCreateAndPersist() throws Exception {
    var adapter = service.create(...);
    Optional<SwiftAdapter> found = repository.findById(adapter.getId());
    assertThat(found).isPresent();
  }
}
```

---

## 🚨 Common Mistakes

```java
// ❌ DON'T: Mock class under test
@Mock private SwiftAdapterService service;

// ✅ DO: Mock dependencies only
@Mock private SwiftAdapterRepository repo;

// ❌ DON'T: Multiple behaviors per test
@Test
void testMultipleThings() {
  service.create(...);
  service.update(...);
  service.delete(...);
}

// ✅ DO: One behavior per test
@Test void shouldCreate() { }
@Test void shouldUpdate() { }
@Test void shouldDelete() { }

// ❌ DON'T: Hardcode test data
var adapter = new SwiftAdapter("id", "name", "endpoint", ...);

// ✅ DO: Use builders
var adapter = SwiftAdapterTestDataBuilder.aSwiftAdapter().build();

// ❌ DON'T: Complex assertions
assertThat(result.getId()).isNotNull();
assertThat(result.getName()).isNotNull();
// 20 more assertions...

// ✅ DO: Clear, focused assertions
assertThat(result).isNotNull();
assertThat(result.getId()).isEqualTo(expectedId);
```

---

## 📚 File Locations

```
swift-adapter-service/src/test/java/com/payments/swiftadapter/
├── unit/
│   ├── service/SwiftAdapterServiceTest.java
│   ├── controller/SwiftAdapterControllerTest.java
│   └── exception/ExceptionHandlingTest.java
├── integration/
│   └── SwiftAdapterIntegrationTest.java
└── fixtures/
    ├── SwiftAdapterTestDataBuilder.java
    └── TenantContextTestDataBuilder.java
```

---

## 🔗 Resources

- **Full Guide:** `SWIFT-ADAPTER-TEST-BEST-PRACTICES.md`
- **Issue Analysis:** `SWIFT-ADAPTER-TEST-ISSUES-ANALYSIS.md`
- **Remediation:** `SWIFT-ADAPTER-TEST-REMEDIATION-SUMMARY.md`
- **JUnit 5:** https://junit.org/junit5/
- **Mockito:** https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Spring Testing:** https://spring.io/guides/gs/testing-web/

---

## 🎓 Learning Path

1. Read: `SWIFT-ADAPTER-TEST-BEST-PRACTICES.md` (full guide)
2. Review: Example tests in `unit/service/`, `unit/controller/`, `unit/exception/`
3. Study: Test data builders usage
4. Practice: Write a new test following the templates
5. Verify: Run tests with `mvn test`

---

**Last Updated:** October 2025  
**Version:** 1.0
