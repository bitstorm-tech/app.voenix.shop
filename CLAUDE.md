# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Voenix Shop is a full-stack e-commerce application for creating custom mugs with AI-generated images. The codebase is split into two main applications:

- **Backend**: Kotlin Spring Boot REST API (`/backend`)
- **Frontend**: React TypeScript SPA with Vite (`/frontend`)

## Common Development Commands

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
- After every third changed file and at the end of the implementation, check for TypeScript errors in the frontend folder and fix them
- Make all React components and web pages responsive → working on Mobile and Desktop
- Don't use `React.memo`, `useCallback` or `useMemo` since the React compiler handles these optimizations automatically

### Backend
- Check for compiler errors at the end of the implementation in the backend folder
- Run the linter and formater at the end of the implementation in the backend folder
- Fix all linter errors when they arise


## Architecture & Code Structure

### Backend Architecture

The backend follows Domain-Driven Design with feature packages:

```
backend/src/main/kotlin/com/jotoai/voenix/shop/
├── common/              # Shared DTOs and exception handling
│   ├── dto/            # ErrorResponse, ValidationError DTOs
│   └── exception/      # GlobalExceptionHandler, custom exceptions
├── prompts/            # Prompt management feature
│   ├── entity/         # Prompt JPA entity
│   ├── repository/     # PromptRepository (Spring Data JPA)
│   ├── service/        # PromptService business logic
│   ├── controller/     # PromptController REST endpoints
│   └── dto/           # CreatePromptRequest, PromptResponse DTOs
└── users/              # User management feature (similar structure)
```

**Key patterns:**
- RESTful APIs under `/api/*` prefix
- DTOs for request/response contracts
- Global exception handling with proper HTTP status codes
- Builder pattern for entities
- Spring Data JPA for database access
- Flyway for database migrations (auto-run on startup)

**Database Configuration:**
- PostgreSQL on localhost:5432
- Database name: `voenix_java`
- Migrations in `resources/db/changelog/`
- Write SQL statements in lowercase

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
- Context API with reducer pattern for wizard state
- Centralized state in `EditorProvider`
- Type-safe actions and state updates

**Key Technologies:**
- React 19.1.0 with TypeScript
- Vite for fast development and building
- Tailwind CSS v4 for styling
- Radix UI for accessible components
- Three.js + React Three Fiber for 3D mug preview
- react-image-crop for image cropping

## Important Development Notes

1. **Package Manager**: Frontend uses npm (package-lock.json present)
2. **Kotlin/Java Version**: Backend requires JDK 21+ with Kotlin 2.1.0
3. **TypeScript**: Strict mode enabled - ensure proper typing
4. **API Communication**: Frontend expects backend on http://localhost:8080
5. **Database**: Ensure PostgreSQL is running before starting backend
6. **No Tests**: Currently no test files exist - add tests when implementing features
7. **No Linting Config**: ESLint installed but not configured - add .eslintrc when needed

## API Endpoints

### Prompts
- `GET /api/prompts` - List all prompts
- `POST /api/prompts` - Create new prompt
- `GET /api/prompts/{id}` - Get prompt by ID
- `PUT /api/prompts/{id}` - Update prompt
- `DELETE /api/prompts/{id}` - Delete prompt

### Users
- `GET /api/users` - List all users
- `POST /api/users` - Create new user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

## Migration Context

This project was migrated from Laravel to Spring Boot, and then from Java to Kotlin (per git history). The frontend appears to be newly created with modern React patterns.