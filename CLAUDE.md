# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Voenix Shop is a full-stack e-commerce application for creating custom mugs with AI-generated images. The codebase is split into two main applications:

- **Backend**: Kotlin Spring Boot REST API (`/backend`)
- **Frontend**: React TypeScript SPA with Vite (`/frontend`)

## Common Development Commands

The test credentials to login into the admin site are `a@a` / `test`.

### Backend (Spring Boot)

```bash
cd backend
./gradlew bootRun        # Run the Spring Boot application (port 8080)
./gradlew build          # Build the project
./gradlew test           # Run tests
./gradlew clean build    # Clean rebuild
./gradlew ktlintCheck    # Run linter
./gradlew ktlintFormat   # Run code formatter
```

### Frontend (React + Vite)

```bash
cd frontend
npm install              # Install dependencies
npm run dev              # Start development server (port 3000)
npm run build            # Production build with type checking
npm run type-check       # TypeScript type checking only
npm run preview          # Preview production build
npm run lint             # Run linter
npm run format           # Run code formatter
```

## Quality Assurance

### Common
- Skeptical mode: question everything, suggest simpler explanations, stay grounded
- Use and spawn subagents to run tasks in parallel whenever possible
- ALWAYS read the latest documentation from context7 mcp server
- Use the puppeteer mcp server to check if the implementation looks right in the browser
- Use `git mv` to move files that are under version control
- Don't write useless, unnecessary or redundant comments -> only use comments to describe complex logic
- Document WHY decisions were made, not just WHAT the code does

### Frontend
- Don't use `React.memo`, `useCallback` or `useMemo` since the React compiler handles these optimizations automatically
- Run the formatter in the frontend folder for all new or changed files at the end of the implementation
- Make all React components and web pages responsive → working on Mobile and Desktop
- Use TypeScript strict mode - ensure proper typing for all components
- Validate forms with proper error handling and user feedback
- Test admin features with different role permissions

### Backend
- Check for compiler errors at the end of the implementation in the backend folder
- Run the linter and formatter at the end of the implementation in the backend folder
- Fix all linter errors when they arise
- Write unit tests for new services and controllers
- Validate all API inputs with proper DTOs and annotations
- Handle exceptions gracefully with appropriate HTTP status codes
- Follow RESTful conventions for API design


## Architecture & Code Structure

### Backend Architecture

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
│   └── service/        # Auth services (JWT, user details)
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
- JWT-based authentication with Spring Security
- Layered architecture: Controller → Service → Repository
- DTOs for API contracts, entities for persistence
- Builder pattern for complex entities
- Spring Data JPA with custom queries
- Flyway for database migrations

**Database Configuration:**
- PostgreSQL on localhost:5432
- Database name: `voenix_java`
- Migrations in `resources/db/changelog/`
- Write SQL statements in lowercase
- JPA Hibernate with automatic schema validation

### Frontend Architecture

The frontend is a multi-step wizard for mug customization:

```
frontend/src/
├── components/
│   ├── ui/             # Reusable UI components (Button, Card, etc.)
│   └── editor/         # Editor-specific components
│       ├── steps/      # Wizard step components (1-6)
│       └── shared/     # Shared editor components
├── hooks/              # Custom React hooks
├── lib/                # Utilities and helpers
├── pages/              # Route pages (Home, About, Editor)
└── types/              # TypeScript type definitions
```

**Wizard Flow (6 steps):**
1. **ImageUploadStep**: Upload custom image
2. **PromptSelectionStep**: Select AI prompt for image generation
3. **MugSelectionStep**: Choose mug type
4. **UserDataStep**: Enter user information
5. **ImageGenerationStep**: Generate AI image
6. **PreviewStep**: 3D preview with Three.js

**State Management:**
- **Zustand** for global state management (auth, user session)
- Context API with reducer pattern for wizard state in `EditorProvider`
- Type-safe stores with TypeScript interfaces
- Auth state persisted across page refreshes via session checks

**Key Technologies:**

Frontend:
- **React 19.1.0** with TypeScript 5.7
- **Vite 6.0** for fast development and building
- **Tailwind CSS v4** for styling
- **Radix UI** for accessible components
- **Zustand** for state management
- **React Router v7** for routing
- **Three.js + React Three Fiber** for 3D mug preview
- **react-image-crop** for image cropping
- **Axios** for API communication
- **React Hook Form** for form handling
- **Lucide React** for icons

Backend:
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
- **JWT** for authentication tokens
- **BCrypt** for password hashing
- **Ktlint** for code formatting

### Admin Panel

The application includes a comprehensive admin panel accessible at `/admin/*` routes:

```
frontend/src/pages/admin/
├── Articles/           # Article management
│   ├── Mugs.tsx       # List and manage mugs
│   ├── NewOrEditMug.tsx # Create/edit mug with tabs
│   └── ArticleCategories.tsx # Manage categories
├── Prompts/            # Prompt management
│   ├── Prompts.tsx     # List and manage prompts
│   ├── NewOrEditPrompt.tsx # Create/edit prompts
│   ├── PromptCategories.tsx # Categories & subcategories
│   ├── PromptTester.tsx # Test prompts with OpenAI
│   └── Slots.tsx       # Manage prompt slots
├── Orders/             # Order management
│   └── CompletedOrders.tsx # View completed orders
├── Logistics/          # Shipping and suppliers
│   ├── Suppliers.tsx   # Manage suppliers
│   └── Logistics.tsx   # Logistics configuration
└── Users.tsx           # User management

```

**Admin Features:**
- **Mugs Management**: Full CRUD with variants, costs, shipping details
- **Prompt System**: Categories, subcategories, slots for dynamic prompts
- **Prompt Tester**: Live testing of prompts with OpenAI integration
- **Order Tracking**: View and manage completed orders
- **Supplier Management**: Track suppliers and logistics
- **User Administration**: Manage users and permissions

**Admin Components Structure:**
- Reusable form components for create/edit operations
- Tab-based interfaces for complex entities (mugs)
- Data tables with sorting and filtering
- Modal dialogs for quick actions
- Protected routes with role-based access

## Important Development Notes

1. **Package Manager**: Frontend uses npm (package-lock.json present)
2. **Kotlin/Java Version**: Backend requires JDK 21+ with Kotlin 2.2.0
3. **TypeScript**: Strict mode enabled - ensure proper typing
4. **API Communication**: Frontend expects backend on http://localhost:8080
5. **Database**: Ensure PostgreSQL is running before starting backend
6. **Testing**: Backend tests recently added - run with `./gradlew test`
7. **Linting**: Backend uses Ktlint, frontend needs ESLint configuration
8. **Environment**: Use `.env` files for configuration (supported by spring-dotenv)

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

**Note**: All `/api/admin/*` endpoints require ADMIN role authentication via JWT.

## Security

### Authentication & Authorization
- **JWT-based authentication** with access and refresh tokens
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

### Frontend Security
- **Protected routes** using React Router guards
- **Automatic redirect** to login for unauthorized access
- **Token storage** in HTTP-only cookies (via backend)
- **Session persistence** across page refreshes
- **Role-based UI rendering** for admin features

### Security Best Practices
- Never store sensitive data in localStorage
- All API calls use authentication headers
- Input validation on both frontend and backend
- Error messages don't expose sensitive information
- Regular security audits and dependency updates

## Development Workflow

### Setting Up Development Environment
1. **Database Setup**:
   - Install PostgreSQL and create database `voenix_java`
   - Database migrations run automatically on backend startup

2. **Backend Setup**:
   ```bash
   cd backend
   ./gradlew build
   ./gradlew bootRun   # Runs on http://localhost:8080
   ```

3. **Frontend Setup**:
   ```bash
   cd frontend
   npm install
   npm run dev         # Runs on http://localhost:3000
   ```

### Working with Admin Features
1. **Create Admin User**: Use the admin API or database seed
2. **Access Admin Panel**: Navigate to `/admin` routes
3. **Test OpenAI Integration**: 
   - Configure OpenAI API key in backend `.env`
   - Use the Prompt Tester at `/admin/prompts/tester`

### Database Migrations
- Located in `backend/src/main/resources/db/changelog/`
- Auto-run via Flyway on application startup
- Create new migrations with sequential versioning
- Use lowercase SQL statements

### Testing Workflow
1. **Backend Testing**:
   ```bash
   cd backend
   ./gradlew test
   ```

2. **Frontend Type Checking**:
   ```bash
   cd frontend
   npm run type-check
   ```

3. **Linting and Formatting**:
   ```bash
   # Backend
   ./gradlew ktlintCheck
   ./gradlew ktlintFormat
   
   # Frontend
   npm run lint
   npm run format
   ```

### Deployment Considerations
- Set production environment variables
- Configure JWT secret keys
- Enable HTTPS for production
- Set up proper CORS origins
- Configure session timeout values
- Enable production logging profiles

## Migration Context

This project was migrated from Laravel to Spring Boot, and then from Java to Kotlin (per git history). The frontend appears to be newly created with modern React patterns.