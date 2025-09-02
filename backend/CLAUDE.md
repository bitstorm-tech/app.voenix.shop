# Backend CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the backend code in this repository.

## Backend Overview

The backend is a Kotlin Spring Boot REST API that provides e-commerce functionality for the Voenix Shop application. The codebase is currently migrating to Spring Modulith architecture for better modularity and maintainability.

## Development Commands

```bash
cd backend
./gradlew bootRun        # Run the Spring Boot application (port 8080)
./gradlew build          # Build the project
./gradlew test           # Run tests
./gradlew clean build    # Clean rebuild
./gradlew ktlintCheck    # Run linter
./gradlew ktlintFormat   # Run code formatter
```

## Quality Assurance

- **Use backend-expert agent** for all Kotlin/Spring Boot implementations
- Check for compiler errors at the end of the implementation in the backend folder
- Run the linter and formatter at the end of the implementation in the backend folder
- Fix all linter errors when they arise
- Write unit tests for new services and controllers
- Validate all API inputs with proper DTOs and annotations
- Handle exceptions gracefully with appropriate HTTP status codes
- Follow RESTful conventions for API design

## Architecture

The backend follows Spring Modulith architecture with Domain-Driven Design principles, providing clear module boundaries and managed dependencies:

### Module Architecture Principles

1. **Module Boundaries**:
   - Modules expose public APIs through all classes in the root package
   - Internal implementation details are in the `internal` package
   - Cross-module communication only through public APIs

2. **Dependency Rules**:
   - Only depend on classes in the modules' root packages

### Key Architectural Patterns

- **Spring Modulith**: Modular monolith with module verification
- **Hexagonal Architecture**: Within each module
- **Repository Pattern**: Data access through Spring Data JPA
- **DTO Pattern**: Separate DTOs for API contracts
- **Service Layer Pattern**: Business logic in service implementations

## Database Configuration

- PostgreSQL on localhost:5432
- Database name: `voenix`
- Migrations in `resources/db/changelog/`
- Write SQL statements in lowercase
- JPA Hibernate with automatic schema validation

## Key Technologies

- **Kotlin 2.2.0** with JDK 21
- **Spring Boot 3.5.4** framework
- **Spring Modulith 1.4.2** for modular architecture
- **Spring Security** for authentication
- **Spring Data JPA** for data access
- **PostgreSQL 42.7** database
- **Flyway 11.10** for migrations
- **Ktor Client** for HTTP requests
- **Apache PDFBox 3.0** for PDF generation
- **ZXing** for QR code generation
- **Scrimage** for image processing
- **Jackson** for JSON handling
- **BCrypt** for password hashing
- **Ktlint** for code formatting

## Security Configuration

### Authentication & Authorization
- **Spring Security** configuration for endpoint protection
- **Role-based access control** (USER, ADMIN roles)
- **Stateless sessions** with token validation on each request
- **CORS configuration** for frontend-backend communication

### Security Features
- **Password encryption** using BCrypt
- **Protected admin routes** requiring ADMIN role
- **Session management** with automatic token refresh
- **Logout functionality** that invalidates tokens
- **Request validation** and input sanitization

## Spring Modulith Development Guidelines

Spring Modulith provides a structured approach to building modular monoliths with clear boundaries and managed dependencies. The codebase follows a simplified module structure that keeps public APIs in the root package.

### Module Structure

Each module follows this simplified structure:

```
shop/modulename/
├── ModuleService.kt         # Public service interface
├── ModuleDto.kt             # Public DTOs  
├── ModuleRequest.kt         # Public Request DTOs
└── internal/                # Private implementation
    ├── entity/              # JPA entities
    ├── repository/          # Spring Data repositories
    ├── service/             # Service implementations
    └── web/                 # REST controllers
```

### Creating a New Module

1. **Create the module package** under `com.jotoai.voenix.shop.modulename`

2. **Define the public service interface** in the root package:
```kotlin
// modulename/ModuleService.kt
interface ModuleService {
    // Query operations
    fun getAllItems(): List<ModuleDto>
    fun getItemById(id: Long): ModuleDto
    
    // Command operations  
    fun createItem(request: CreateItemRequest): ModuleDto
    fun updateItem(id: Long, request: UpdateItemRequest): ModuleDto
    fun deleteItem(id: Long)
}
```

3. **Create DTOs** in the root package for data transfer:
```kotlin
// modulename/ModuleDto.kt
data class ModuleDto(
    val id: Long,
    val name: String,
    // other fields
)

// modulename/ModuleRequest.kt
data class CreateItemRequest(
    val name: String,
    // other fields
)
```

4. **Implement the service** in the internal package:
```kotlin
// modulename/internal/service/ModuleServiceImpl.kt
@Service
internal class ModuleServiceImpl(
    private val repository: ModuleRepository
) : ModuleService {
    // Implementation
}
```

### Internal Package Organization

The `internal` package contains all private implementation details:

- **entity/**: JPA entities that are never exposed outside the module
- **repository/**: Spring Data repositories for data access
- **service/**: Service implementations of the public interfaces
- **web/**: REST controllers that handle HTTP requests

Important principles:
- Keep entities in the internal package - never expose them
- Use DTOs for all inter-module communication
- Controllers should delegate all logic to services
- Repositories should only be used by services within the module

### Module Communication

Modules communicate through their public service interfaces:

```kotlin
// In article module, using supplier and VAT services
@Service
internal class ArticleServiceImpl(
    private val supplierService: SupplierService,  // From supplier module
    private val vatService: VatService             // From VAT module
) : ArticleService {
    
    fun calculatePrice(articleId: Long): BigDecimal {
        val article = getArticle(articleId)
        val supplier = supplierService.getSupplierById(article.supplierId)
        val vat = vatService.getDefaultVat()
        
        // Business logic using data from other modules
        return calculateFinalPrice(article, supplier, vat)
    }
}
```

### Real Examples from Codebase

#### Supplier Module (Simplified Structure)
```kotlin
// supplier/SupplierService.kt - Unified service interface
interface SupplierService {
    fun getAllSuppliers(): List<SupplierDto>
    fun getSupplierById(id: Long): SupplierDto
    fun existsById(id: Long): Boolean
    fun createSupplier(request: CreateSupplierRequest): SupplierDto
    fun updateSupplier(id: Long, request: UpdateSupplierRequest): SupplierDto
    fun deleteSupplier(id: Long)
}

// supplier/internal/service/SupplierServiceImpl.kt
@Service
internal class SupplierServiceImpl(
    private val repository: SupplierRepository
) : SupplierService {
    // Implementation using repository
}
```

#### VAT Module (Minimal API)
```kotlin
// vat/api/VatService.kt
interface VatService {
    fun getDefaultVat(): ValueAddedTaxDto?
    fun getAllVats(): List<ValueAddedTaxDto>
    fun createVat(request: CreateValueAddedTaxRequest): ValueAddedTaxDto
}
```

### Testing Modules

Run module verification tests to ensure boundaries are respected:

```bash
# Run all module tests
./gradlew test --tests "*ModulithTest"

# Verify specific module
./gradlew test --tests "SupplierModulithTest"
```

Module tests automatically verify:
- No cyclic dependencies between modules
- Internal packages are not accessed from outside
- Module boundaries are properly maintained

### Common Pitfalls and Solutions

#### Circular Dependencies
**Problem**: Module A needs Module B, and Module B needs Module A
**Solution**: Extract shared logic to a common module or reconsider module boundaries

#### Exposing Internal Classes
**Problem**: Entity or repository classes used in public APIs
**Solution**: Always use DTOs in public interfaces, keep entities internal

#### Cross-Module Database Access
**Problem**: Multiple modules accessing the same database tables
**Solution**: Each module should own its tables; use service APIs for data access

#### Missing Internal Modifier
**Problem**: Implementation classes accidentally public
**Solution**: Mark all implementation classes as `internal` in Kotlin

## Development Notes

1. **Kotlin/Java Version**: Backend requires JDK 21+ with Kotlin 2.2.0
2. **Database**: Ensure PostgreSQL is running before starting backend
3. **Testing**: Run tests with `./gradlew test`
4. **Module Tests**: Run `./gradlew test --tests "*ModulithTest"` to verify module boundaries
5. **Linting**: Backend uses Ktlint - always fix linting errors
6. **Environment**: Use `.env` files for configuration (supported by spring-dotenv)

## Database Migrations

- Located in `backend/src/main/resources/db/changelog/`
- Auto-run via Flyway on application startup
- Create new migrations with sequential versioning
- Use lowercase SQL statements

## Testing Workflow

```bash
cd backend
./gradlew test          # Run all tests
./gradlew ktlintCheck   # Check code style
./gradlew ktlintFormat  # Format code
```

## Backend Setup

1. **Database Setup**:
   - Install PostgreSQL and create database `voenix_java`
   - Database migrations run automatically on backend startup

2. **Run Backend**:
   ```bash
   cd backend
   ./gradlew build
   ./gradlew bootRun   # Runs on http://localhost:8080
   ```

3. **Environment Configuration**:
   - Create `.env` file in backend directory
   - Configure OpenAI API key for image generation
   - Set database connection parameters if different from defaults

## Test Mode Feature

The backend includes a Test Mode feature for AI image generation that allows development and testing without making costly API calls to OpenAI.

### Configuration

Add the following configuration to enable test mode:

```properties
# Enable test mode (default: false)
app.test-mode=true
```

### How Test Mode Works

When `app.test-mode=true`:

1. **TestModeImageGenerationStrategy** is activated instead of **OpenAIImageGenerationStrategy**
2. Image generation requests return the **original uploaded image** N times (based on request.n parameter)
3. **No external API calls** are made to OpenAI
4. **Prompt validation** still occurs to ensure business logic works correctly
5. **Mock URLs** are generated for prompt testing (e.g., `https://test-mode.voenix.shop/images/mock-{uuid}.png`)

### Use Cases

- **Development**: Avoid API costs during feature development
- **Testing**: Reliable, fast tests without external dependencies  
- **CI/CD**: Integration tests that don't require API keys
- **Demo/Staging**: Show functionality without real AI generation

### Production Configuration

For production, either:
- Set `app.test-mode=false` 
- Omit the property entirely (defaults to false)
- Ensure `OPENAI_API_KEY` environment variable is properly configured

### Testing

The test mode feature includes comprehensive unit and integration tests:

- **TestModeImageGenerationStrategyTest**: Tests mock image generation logic
- **OpenAIImageGenerationStrategyTest**: Tests real strategy business logic (without HTTP calls)
- **ImageGenerationStrategySelectionTest**: Tests proper strategy selection based on configuration
- **OpenAIImageServiceTest**: Tests service layer delegation to strategies

Run tests with:
```bash
./gradlew test
```