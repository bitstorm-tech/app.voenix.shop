# Backend Test Improvements Execution Plan

## Executive Summary

Current test coverage is critically low at ~8% with only 6 test files covering 71+ components. This plan outlines immediate, short-term, and long-term improvements to achieve production-quality test coverage of 70%+.

## Phase 1: Critical Infrastructure (Week 1)

### 1.1 Test Coverage Reporting
```kotlin
// build.gradle.kts additions
plugins {
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
```

### 1.2 Test Data Builders
Create `src/test/kotlin/com/jotoai/voenix/shop/util/TestDataBuilders.kt`:
```kotlin
object TestDataBuilders {
    fun userDto(
        id: Long = 1L,
        email: String = "test@example.com",
        firstName: String? = null,
        lastName: String? = null
    ) = UserDto(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = null,
        createdAt = OffsetDateTime.now(),
        updatedAt = OffsetDateTime.now()
    )
    
    fun registerRequest(
        email: String = "new@example.com",
        password: String = "Test123!@#"
    ) = RegisterRequest(email = email, password = password)
    
    fun mugDto(
        id: Long = 1L,
        name: String = "Test Mug",
        variants: List<PublicMugVariantDto> = listOf(mugVariantDto())
    ) = PublicMugDto(id = id, name = name, /* ... */, variants = variants)
    
    fun mugVariantDto(
        id: Long = 1L,
        isDefault: Boolean = true,
        price: BigDecimal = BigDecimal("9.99")
    ) = PublicMugVariantDto(id = id, isDefault = isDefault, price = price, /* ... */)
}
```

### 1.3 Test Fixtures & Base Classes
Create `src/test/kotlin/com/jotoai/voenix/shop/util/BaseIntegrationTest.kt`:
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc
    
    @Autowired
    protected lateinit var objectMapper: ObjectMapper
    
    protected fun performPost(url: String, body: Any) = 
        mockMvc.perform(
            post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
}
```

## Phase 2: Critical Business Path Tests (Week 2-3)

### 2.1 Authentication & Authorization Tests

#### AuthServiceTest.kt
```kotlin
@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
    // Test scenarios:
    // - Successful login with valid credentials
    // - Login failure with invalid password
    // - Login failure with non-existent user
    // - Account lockout after failed attempts
    // - Session creation and management
    // - Logout functionality
    // - Password encoding verification
}
```

#### AuthControllerIntegrationTest.kt
```kotlin
class AuthControllerIntegrationTest : BaseIntegrationTest() {
    // Test scenarios:
    // - Complete registration flow
    // - Duplicate email registration
    // - Login → Session → Logout flow
    // - Session expiration
    // - Concurrent login attempts
    // - SQL injection attempts
    // - XSS prevention
}
```

### 2.2 Order Processing Tests

#### OrderServiceTest.kt
```kotlin
// Test scenarios:
// - Order creation with valid data
// - VAT calculation for different countries
// - Price calculation with shipping
// - Inventory validation
// - Order status transitions
// - Payment processing integration
// - Order cancellation logic
```

#### OrderControllerIntegrationTest.kt
```kotlin
// Test scenarios:
// - Complete order flow: cart → checkout → payment → confirmation
// - Order with multiple items
// - International orders with VAT
// - Out of stock handling
// - Payment failure recovery
```

### 2.3 Image Generation Tests

#### ImageServiceTest.kt
```kotlin
// Test scenarios:
// - Image upload validation (size, format)
// - User generation limits enforcement
// - Image storage and retrieval
// - Cleanup of temporary files
// - Error handling for corrupt images
```

#### OpenAIServiceTest.kt
```kotlin
// Test scenarios:
// - Successful image generation (mocked)
// - API timeout handling
// - Rate limit handling
// - Invalid prompt handling
// - Retry logic verification
// - Cost tracking
```

## Phase 3: Comprehensive Coverage (Week 4-5)

### 3.1 Repository Tests
Create tests for all repositories with custom queries:

```kotlin
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    lateinit var userRepository: UserRepository
    
    @Test
    fun `findByEmail should return user when exists`() {
        // Test custom queries
    }
    
    @Test
    fun `findActiveUsers should exclude deleted accounts`() {
        // Test complex criteria
    }
}
```

### 3.2 Controller Tests
Add tests for all remaining controllers:
- AdminUserControllerTest
- AdminArticleControllerTest  
- AdminPromptControllerTest
- AdminImageControllerTest
- UserProfileControllerTest
- PublicImageControllerTest

### 3.3 Service Tests
Add tests for all remaining services:
- UserService
- ArticleService
- PromptService
- PDFService
- EmailService
- FileStorageService

## Phase 4: Test Quality Improvements (Week 6)

### 4.1 Refactor Existing Tests

#### AuthControllerTest.kt Improvements
```kotlin
// Before:
val userDto = UserDto(id = 1L, email = "newuser@example.com", 
    firstName = null, lastName = null, phoneNumber = null,
    createdAt = OffsetDateTime.now(), updatedAt = OffsetDateTime.now())

// After:
val userDto = TestDataBuilders.userDto(email = "newuser@example.com")
```

#### AuthRegistrationIntegrationTest.kt Fixes
```kotlin
// Remove dangerous deleteAll()
// Use @DirtiesContext or @Sql for data setup
@Sql(scripts = ["/sql/clean-users.sql"], 
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
```

#### AbstractSecurityTest.kt Refactoring
- Split into smaller, focused utility classes
- Replace `!!` with safe calls or elvis operators
- Use builder pattern for request construction

### 4.2 Parameterized Tests
```kotlin
@ParameterizedTest
@ValueSource(strings = ["", " ", "invalid", "user@", "@example.com"])
fun `register should reject invalid emails`(email: String) {
    val request = TestDataBuilders.registerRequest(email = email)
    // Test validation
}
```

### 4.3 Custom Assertions
```kotlin
object CustomAssertions {
    fun MockHttpServletResponse.assertSuccessResponse() {
        assertEquals(200, status)
        assertNotNull(contentAsString)
    }
    
    fun MockHttpServletResponse.assertErrorResponse(expectedStatus: Int) {
        assertEquals(expectedStatus, status)
        assertTrue(contentAsString.contains("error"))
    }
}
```

## Phase 5: Advanced Testing (Week 7-8)

### 5.1 Integration Test Suite with TestContainers
```kotlin
@SpringBootTest
@Testcontainers
class FullStackIntegrationTest {
    @Container
    val postgres = PostgreSQLContainer<Nothing>("postgres:15")
        .apply {
            withDatabaseName("voenix_test")
            withUsername("test")
            withPassword("test")
        }
    
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }
}
```

### 5.2 Contract Testing
```kotlin
@Test
fun `API contract test for mug endpoint`() {
    // Use Spring Cloud Contract or Pact
    // Verify API contracts between frontend and backend
}
```

### 5.3 Performance Tests
```kotlin
@Test
@Timeout(value = 2, unit = TimeUnit.SECONDS)
fun `image generation should complete within timeout`() {
    // Performance assertions
}
```

## Phase 6: Continuous Improvement (Ongoing)

### 6.1 Test Documentation
Create `docs/testing-guide.md`:
- Testing philosophy and principles
- How to write effective tests
- Test naming conventions
- When to use mocks vs real implementations
- Performance testing guidelines

### 6.2 CI/CD Integration
```yaml
# .github/workflows/test.yml
- name: Run tests with coverage
  run: ./gradlew test jacocoTestReport
  
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    file: ./build/reports/jacoco/test/jacocoTestReport.xml
    fail_ci_if_error: true
    minimum_coverage: 70
```

### 6.3 Test Monitoring
- Set up test execution time tracking
- Monitor flaky tests
- Regular test suite health reviews
- Mutation testing with PIT

## Success Metrics

### Coverage Goals
- **Immediate (2 weeks)**: 30% coverage
- **Short-term (1 month)**: 50% coverage  
- **Target (2 months)**: 70%+ coverage

### Quality Metrics
- All tests run in < 5 minutes
- No flaky tests
- Clear test names and documentation
- Balanced unit/integration/e2e ratio (60/30/10)

## Priority Order

1. **Week 1**: Infrastructure + Test data builders
2. **Week 2**: Auth & Security tests
3. **Week 3**: Order & Payment tests
4. **Week 4**: Image & OpenAI tests
5. **Week 5**: Remaining controllers & services
6. **Week 6**: Test refactoring & quality
7. **Week 7-8**: Advanced testing features

## Risk Mitigation

### Risks
- Existing code may need refactoring for testability
- External service mocking complexity
- Test execution time increases
- Developer resistance to writing tests

### Mitigations
- Gradual refactoring during test writing
- Invest in good mocking infrastructure early
- Parallel test execution configuration
- Team training on testing best practices
- Make tests a requirement for PR approval

## Conclusion

This plan transforms the test suite from minimal coverage to production-ready quality. The phased approach ensures critical business paths are tested first while building sustainable testing practices for long-term maintainability.