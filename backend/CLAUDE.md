# Backend CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the backend code in this repository.

## Backend Overview

The backend is a Kotlin Spring Boot REST API that provides e-commerce functionality for the Voenix Shop application.

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

The backend follows Domain-Driven Design with clear separation between API layer and domain layer:

```
backend/src/main/kotlin/com/jotoai/voenix/shop/
├── api/                 # API Controllers
│   ├── admin/          # Admin-only endpoints
│   │   ├── articles/   # Article management controllers
│   │   ├── images/     # Image handling controllers
│   │   ├── prompts/    # Prompt management controllers
│   │   ├── services/   # OpenAI and PDF controllers
│   │   └── users/      # User management controllers
│   ├── auth/           # Authentication endpoints
│   └── user/           # User-specific endpoints
├── auth/               # Authentication domain
│   ├── config/         # Security configuration
│   ├── dto/            # Auth DTOs (login, register, tokens)
│   ├── entity/         # User and Role entities
│   └── service/        # Auth services
├── common/             # Shared utilities
│   ├── config/         # App-wide configuration
│   ├── dto/            # Common DTOs
│   └── exception/      # Global exception handling
└── domain/             # Core business domains
    ├── articles/       # Article management
    │   ├── categories/ # Categories and subcategories
    │   └── mugs/       # Mug-specific logic
    ├── images/         # Image processing services
    ├── openai/         # OpenAI integration (DALL-E)
    ├── pdf/            # PDF generation services
    ├── prompts/        # Prompt management
    └── users/          # User domain logic
```

**Key patterns:**
- RESTful APIs with role-based access control
- Layered architecture: Controller → Service → Repository
- DTOs for API contracts, entities for persistence
- Builder pattern for complex entities
- Spring Data JPA with custom queries
- Flyway for database migrations

## Database Configuration

- PostgreSQL on localhost:5432
- Database name: `voenix_java`
- Migrations in `resources/db/changelog/`
- Write SQL statements in lowercase
- JPA Hibernate with automatic schema validation

## Key Technologies

- **Kotlin 2.2.0** with JDK 21
- **Spring Boot 3.5.3** framework
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

## API Endpoints

### Authentication (`/api/auth`)
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/session` - Check session status

### User (`/api/user`)
- `GET /api/user/profile` - Get user profile
- `PUT /api/user/profile` - Update user profile
- `GET /api/user/session` - Get session info
- `POST /api/user/logout` - Logout user
- `DELETE /api/user/account` - Delete user account

### Admin Users (`/api/admin/users`)
- `GET /api/admin/users` - List all users
- `GET /api/admin/users/{id}` - Get user by ID
- `POST /api/admin/users` - Create new user
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Delete user

### Admin Articles - Mugs (`/api/admin/articles/mugs`)
- `GET /api/admin/articles/mugs` - List all mugs
- `GET /api/admin/articles/mugs/{id}` - Get mug by ID
- `POST /api/admin/articles/mugs` - Create new mug
- `PUT /api/admin/articles/mugs/{id}` - Update mug
- `DELETE /api/admin/articles/mugs/{id}` - Delete mug

### Admin Articles - Categories (`/api/admin/articles/categories`)
- `GET /api/admin/articles/categories` - List all categories
- `GET /api/admin/articles/categories/{id}` - Get category by ID
- `POST /api/admin/articles/categories` - Create new category
- `PUT /api/admin/articles/categories/{id}` - Update category
- `DELETE /api/admin/articles/categories/{id}` - Delete category

### Admin Articles - Subcategories (`/api/admin/articles/subcategories`)
- `GET /api/admin/articles/subcategories` - List all subcategories
- `GET /api/admin/articles/subcategories/{id}` - Get subcategory by ID
- `GET /api/admin/articles/subcategories/category/{categoryId}` - Get subcategories by category
- `POST /api/admin/articles/subcategories` - Create new subcategory
- `PUT /api/admin/articles/subcategories/{id}` - Update subcategory
- `DELETE /api/admin/articles/subcategories/{id}` - Delete subcategory

### Admin Prompts (`/api/admin/prompts`)
- `GET /api/admin/prompts` - List all prompts
- `GET /api/admin/prompts/{id}` - Get prompt by ID
- `POST /api/admin/prompts` - Create new prompt
- `PUT /api/admin/prompts/{id}` - Update prompt
- `DELETE /api/admin/prompts/{id}` - Delete prompt

### Admin Prompt Categories (`/api/admin/prompts/categories`)
- `GET /api/admin/prompts/categories` - List all prompt categories
- `GET /api/admin/prompts/categories/{id}` - Get category by ID
- `POST /api/admin/prompts/categories` - Create new category
- `PUT /api/admin/prompts/categories/{id}` - Update category
- `DELETE /api/admin/prompts/categories/{id}` - Delete category

### Admin Prompt Subcategories (`/api/admin/prompts/subcategories`)
- `GET /api/admin/prompts/subcategories` - List all subcategories
- `GET /api/admin/prompts/subcategories/{id}` - Get subcategory by ID
- `GET /api/admin/prompts/subcategories/category/{categoryId}` - Get by category
- `POST /api/admin/prompts/subcategories` - Create new subcategory
- `PUT /api/admin/prompts/subcategories/{id}` - Update subcategory
- `DELETE /api/admin/prompts/subcategories/{id}` - Delete subcategory

### Admin Slots (`/api/admin/prompts/slots`)
- `GET /api/admin/prompts/slots` - List all slots
- `GET /api/admin/prompts/slots/{id}` - Get slot by ID
- `GET /api/admin/prompts/slots/type/{typeId}` - Get slots by type
- `POST /api/admin/prompts/slots` - Create new slot
- `PUT /api/admin/prompts/slots/{id}` - Update slot
- `DELETE /api/admin/prompts/slots/{id}` - Delete slot

### Admin Slot Types (`/api/admin/prompts/slot-types`)
- `GET /api/admin/prompts/slot-types` - List all slot types
- `GET /api/admin/prompts/slot-types/{id}` - Get slot type by ID
- `POST /api/admin/prompts/slot-types` - Create new slot type
- `PUT /api/admin/prompts/slot-types/{id}` - Update slot type
- `DELETE /api/admin/prompts/slot-types/{id}` - Delete slot type

### Admin Images (`/api/admin/images`)
- `POST /api/admin/images/upload` - Upload image (multipart/form-data)
- `GET /api/admin/images/{filename}` - Get image by filename
- `DELETE /api/admin/images/{filename}` - Delete image

### Admin OpenAI Services (`/api/admin/openai`)
- `POST /api/admin/openai/image-edit` - Edit image with DALL-E (multipart/form-data)
- `POST /api/admin/openai/test-prompt` - Test prompt with image generation

### Admin PDF Services (`/api/admin/pdf`)
- `POST /api/admin/pdf/generate` - Generate PDF document

**Note**: All `/api/admin/*` endpoints require ADMIN role authentication.

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

## Development Notes

1. **Kotlin/Java Version**: Backend requires JDK 21+ with Kotlin 2.2.0
2. **Database**: Ensure PostgreSQL is running before starting backend
3. **Testing**: Run tests with `./gradlew test`
4. **Linting**: Backend uses Ktlint - always fix linting errors
5. **Environment**: Use `.env` files for configuration (supported by spring-dotenv)

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