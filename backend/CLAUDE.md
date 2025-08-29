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

### Spring Modulith Structure

The application is organized into self-contained modules with explicit boundaries:

```
backend/src/main/kotlin/com/jotoai/voenix/shop/
├── article/            # Article Module (in migration to Modulith)
│   ├── api/            # Public interfaces and DTOs
│   └── internal/       # Private implementation
├── auth/               # Authentication Module (@ApplicationModule)
│   ├── config/         # Security configuration
│   ├── dto/            # Auth DTOs
│   └── service/        # Auth services
├── common/             # Common Utilities Module (@ApplicationModule, OPEN)
│   ├── config/         # Shared configuration
│   ├── dto/            # Common DTOs
│   └── exception/      # Global exception handling
├── country/            # Country Module (@ApplicationModule)
│   ├── api/            # Public interfaces
│   └── internal/       # Private implementation
├── domain/             # Domain Module (@ApplicationModule, OPEN)
│   ├── cart/           # Shopping cart logic
│   ├── openai/         # OpenAI integration
│   └── orders/         # Order management
├── image/              # Image Module (@ApplicationModule)
│   ├── api/            # Public interfaces
│   └── internal/       # Private implementation
├── pdf/                # PDF Module (@ApplicationModule)
│   ├── api/            # Public interfaces
│   └── internal/       # Private implementation
├── prompt/             # Prompt Module (@ApplicationModule)
│   ├── api/            # Public interfaces
│   └── internal/       # Private implementation
├── supplier/           # Supplier Module (@ApplicationModule)
│   ├── api/            # Public interfaces
│   └── internal/       # Private implementation
├── user/               # User Module (@ApplicationModule)
│   ├── api/            # Public interfaces
│   └── internal/       # Private implementation
└── vat/                # VAT Module (@ApplicationModule)
    ├── api/            # Public interfaces
    └── internal/       # Private implementation
```

### Module Architecture Principles

1. **Module Boundaries**:
   - Each module is annotated with `@ApplicationModule` in its `package-info.java`
   - Modules expose public APIs through the `api` package
   - Internal implementation details are in the `internal` package
   - Cross-module communication only through public APIs

2. **Module Types**:
   - **Standard Modules**: Encapsulated with strict boundaries (e.g., user, vat, supplier)
   - **Open Modules**: Can be accessed by all modules (common, domain)
   - **API Module**: Special module containing REST controllers

3. **Dependency Rules**:
   - Modules declare allowed dependencies explicitly
   - Only depend on other modules' `api` packages
   - Common module has no dependencies
   - Domain module can depend on other modules' APIs

4. **Module Communication Patterns**:
   - **Direct API calls**: Through Facade and QueryService interfaces
   - **Shared DTOs**: Located in module's `api.dto` packages

### Key Architectural Patterns

- **Spring Modulith**: Modular monolith with module verification
- **Hexagonal Architecture**: Within each module (api/internal separation)
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

### Creating a New Module

1. **Create module package structure**:
   ```
   shop/newmodule/
   ├── api/              # Public interfaces
   │   ├── dto/          # Data transfer objects
   │   └── exceptions/   # Module-specific exceptions
   ├── internal/         # Private implementation
   │   ├── entity/       # JPA entities
   │   ├── repository/   # Spring Data repositories
   │   └── service/      # Service implementations
   └── package-info.java # Module definition with @ApplicationModule
   ```

2. **Define module boundaries** in `package-info.java`:
   ```java
   @org.springframework.modulith.ApplicationModule(
       displayName = "Module Name",
       allowedDependencies = {"common", "othermodule::api"}
   )
   package com.jotoai.voenix.shop.newmodule;
   ```

3. **Create public API interfaces**:
   - `ModuleFacade` for write operations (commands)
   - `ModuleQueryService` for read operations (queries)
   - DTOs in `api.dto` package

4. **Implement internal services** that implement the public interfaces

### Module Testing

Run module verification tests:
```bash
./gradlew test --tests "*ModulithTest"
```

Module tests verify:
- Module boundaries and dependencies
- No cyclic dependencies
- API exposure rules
- Internal package protection

### Module Communication

1. **Direct API Calls**:
   ```kotlin
   @Service
   class MyService(
       private val userQueryService: UserQueryService  // Inject other module's API
   )
   ```
2. **Shared Types**:
   - Use common module for shared exceptions and utilities
   - Module-specific DTOs in `api.dto` packages

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