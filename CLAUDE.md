# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Voenix Shop is an e-commerce platform for creating custom mugs with AI-generated images. The application consists of:
- **Backend**: Go (Gin + GORM) API server with PostgreSQL/SQLite database
- **Frontend**: React/TypeScript SPA (Vite) with multi-step wizard for mug customization
- **Frontend-NextJS**: Experimental Next.js frontend (keep shared types in sync before promoting features)

## Common Development Commands

### Backend (Go)
```bash
cd backend
make dev        # Run with hot reload via Air
make run        # Run server normally
make build      # Build server binary
make check      # Run fmt, vet, lint, test, build
make test       # Run tests only
make fmt        # Format code
make lint       # Run golangci-lint

# Individual commands
go run ./cmd/server      # Run server
go run ./cmd/migrate     # Run migrations
go test ./cmd/... ./internal/...  # Run tests
```

### Frontend (React/Vite)
```bash
cd frontend
npm install             # Install dependencies
npm run dev            # Start dev server (port 3000)
npm run build          # Production build with type checking
npm run preview        # Preview production build
npm run type-check     # TypeScript type checking only
npm run lint           # Run ESLint
npm run format         # Run Prettier
```

### Frontend-NextJS (Experimental)
```bash
cd frontend-nextjs
npm install
npm run dev            # Start dev server (port 3000)
npm run build         # Production build
npm run lint          # Run linter
```

## Architecture

### Backend Structure
```
backend/
├── cmd/
│   ├── server/     # Main server entry point
│   ├── migrate/    # Database migration tool
│   └── hashpass/   # Password hash generator
├── internal/       # Application logic
│   ├── ai/         # AI image generation (Gemini)
│   ├── article/    # Product/mug management
│   ├── auth/       # Authentication & sessions
│   ├── cart/       # Shopping cart logic
│   ├── country/    # Country data
│   ├── database/   # DB connection & migrations
│   ├── image/      # Image upload/storage
│   ├── order/      # Order processing
│   ├── pdf/        # PDF generation
│   ├── prompt/     # AI prompt management
│   ├── supplier/   # Supplier management
│   └── vat/        # VAT/tax handling
```

### Frontend Structure
```
frontend/src/
├── components/
│   ├── ui/         # Reusable UI components
│   └── editor/     # Mug editor components
│       ├── steps/  # Wizard steps (1-6)
│       └── shared/ # Shared editor components
├── pages/          # Route pages
│   ├── admin/      # Admin panel pages
│   └── ...         # Public pages
├── hooks/          # Custom React hooks
├── stores/         # Zustand state stores
├── types/          # TypeScript types
└── api/            # API client utilities
```

### Key Backend Services

- **Auth**: Session-based authentication with HTTP-only cookies (`session_id`)
- **Storage**: Files stored under `${STORAGE_ROOT}` (configured via env)
  - `public/images/`: Public static files served at `/public/*`
  - `private/images/`: User-specific images with auth check
- **AI Integration**: Gemini API for image generation/manipulation
- **Database**: GORM with PostgreSQL/SQLite support, auto-migrations on startup

### Frontend Wizard Flow

1. **ImageUploadStep**: Upload custom image
2. **PromptSelectionStep**: Select AI prompt for generation
3. **MugSelectionStep**: Choose mug type and variant
4. **UserDataStep**: Enter customer information
5. **ImageGenerationStep**: Generate AI image
6. **PreviewStep**: 3D preview with Three.js

### Admin Panel Routes

- `/admin/articles/*`: Mug and category management
- `/admin/prompts/*`: Prompt management and testing
- `/admin/orders/*`: Order tracking
- `/admin/logistics/*`: Suppliers and shipping
- `/admin/users`: User administration

## Environment Configuration

### Backend (.env)
```bash
DATABASE_URL=postgres://user:pass@localhost/dbname  # or sqlite://./app.db
AUTO_MIGRATE=true                    # Run migrations on startup
ADDR=:8080                          # Server port
STORAGE_ROOT=./storage              # File storage root
FRONTEND_DIST=./frontend/dist      # Optional: serve frontend
GOOGLE_API_KEY=your-key            # For AI image generation
GEMINI_IMAGE_MODEL=gemini-2.5-flash-image-preview
SESSION_TTL_SECONDS=604800         # 7 days
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
TEST_MODE=false                     # Use mock AI when true
```

### Frontend
Frontend expects backend API at `http://localhost:8080` (hardcoded in development).

## Quality Standards

### Backend
- Run `make check` before committing (includes fmt, vet, lint, test, build)
- Use table-driven tests in `*_test.go` files
- Follow Go conventions: PascalCase exports, camelCase locals
- Keep related functionality in feature-scoped files (e.g., `inventory_service.go`)

### Frontend
- Always run at the end of implementation:
  ```bash
  npm run type-check
  npm run lint
  npm run format
  ```
- TypeScript strict mode must pass
- All components must be responsive (mobile + desktop)
- Use Tailwind CSS v4 for styling
- Use Radix UI components for accessibility
- Validate forms with proper error handling

## Database Migrations

Migrations are in `backend/internal/database/migrations/`:
- Format: `NNNNNN_description.up.sql` and `.down.sql`
- Must work on both PostgreSQL and SQLite
- Applied automatically on server startup when `AUTO_MIGRATE=true`
- Manual migration: `go run ./cmd/migrate`

## API Endpoints

Key public endpoints:
- `POST /api/auth/login`: Login with email/username + password
- `GET /api/auth/session`: Get current session
- `POST /api/auth/logout`: Logout
- `GET /api/public/countries`: List countries
- `GET /public/*`: Serve static files

Admin endpoints (require authentication):
- `POST /api/admin/images`: Upload image with cropping
- `POST /api/ai/images`: Generate AI images
- CRUD endpoints for articles, prompts, suppliers, VAT, etc.

## Testing

### Backend
```bash
cd backend
make test  # or go test ./...
```

### Frontend
```bash
cd frontend
npm run type-check
# Vitest setup pending - manual UI validation required
```

## Current Development Focus

Active requirement: `requirements/REQ-cart-prompt-pricing.md`
- Adding prompt pricing to cart items
- Updating cart totals to include prompt prices
- Frontend display of price breakdowns

See `TODO.md` for additional planned features.

## Important Notes

1. **Prompt Pricing**: Prompts can have prices via `prompts.price_id -> prices` table
2. **Cart Logic**: Items track `PriceAtTime` and `OriginalPrice` for price change detection
3. **Image Storage**: All images stored under `STORAGE_ROOT` with public/private separation
4. **Session Management**: DB-backed sessions with configurable TTL
5. **AI Integration**: Gemini for image generation, with TEST_MODE for offline development

## Agents

Custom agents available in `.claude/agents/`:
- `frontend-architect`: Architectural guidance for React/TypeScript frontend
- `complexity-eliminator`: Code simplification and complexity reduction

Use these agents for major refactoring or architectural decisions.