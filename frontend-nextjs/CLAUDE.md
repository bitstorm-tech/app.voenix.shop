# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Next.js 15 frontend application for Voenix Shop, an e-commerce platform for custom AI-generated mugs and merchandise. Built with TypeScript, React 19, and Tailwind CSS v4.

## Common Development Commands

```bash
# Install dependencies
npm install

# Run development server with Turbopack
npm run dev          # Runs on http://localhost:3000

# Build for production
npm run build

# Run production server
npm run start

# Lint code
npm run lint

# Type checking
npx tsc --noEmit
```

## Architecture

### App Router Structure
- **`src/app/`**: Next.js App Router pages
  - `(auth)/`: Authentication routes (login)
  - `(protected)/`: Protected routes requiring authentication
  - `api/`: API route handlers
  - Route groups use parentheses for organization without affecting URLs

### Key Components
- **`src/components/`**: Reusable UI components
  - `ui/`: Base UI components (shadcn/ui style)
  - `admin/`: Admin-specific components
  - `auth/`: Authentication components

### Libraries & State Management
- **`src/lib/`**: Utility functions and configurations
  - `api/server.ts`: Server-side API client with session handling
  - `auth/`: Authentication utilities (session, CSRF)
  - `utils.ts`: Common utilities including `cn()` for className merging

### Type Definitions
- **`src/types/`**: TypeScript interfaces for domain models

## Authentication Flow

1. **Middleware** (`src/middleware.ts`): Handles route protection and session validation
   - Validates SESSION cookie with backend
   - Redirects unauthenticated users to login
   - Requires ADMIN role for admin routes
   - Implements session caching for performance

2. **Session Management**: 
   - Server-side validation via backend API
   - Client-side utilities in `lib/auth/session.ts`
   - CSRF protection for mutations

## API Communication

- Backend API URL configured via `BACKEND_URL` environment variable
- Server components use `serverApi` from `lib/api/server.ts`
- Automatic session cookie forwarding
- Structured error handling with `ApiError` class

## Styling Approach

- **Tailwind CSS v4**: Using latest CSS-first configuration
- **CSS Variables**: Theme tokens defined in `global.css` using OKLCH color space
- **Dark Mode**: Supported via CSS custom properties
- **Component Variants**: Using `class-variance-authority` (CVA)
- **Utility Function**: `cn()` for merging classes with `tailwind-merge`

## UI Components

The project uses a shadcn/ui-inspired component library with:
- Radix UI primitives for accessibility
- Consistent styling patterns
- TypeScript props interfaces
- Forwardref support where needed

## Environment Configuration

```bash
# .env or .env.local
BACKEND_URL=http://localhost:8080
```

## Development Guidelines

1. **File Organization**:
   - Use route groups `(name)` for logical grouping
   - Keep components close to where they're used
   - Shared components in `src/components/`

2. **TypeScript**:
   - Strict mode enabled
   - Use type imports: `import type { ... }`
   - Define interfaces in `src/types/`

3. **Server Components**:
   - Default to server components
   - Use `'use client'` directive only when needed
   - Leverage server-side data fetching

4. **Error Handling**:
   - Use error.tsx files for error boundaries
   - Handle API errors gracefully
   - Display user-friendly error messages

5. **Performance**:
   - React Compiler enabled for optimization
   - Turbopack for faster development builds
   - Implement proper loading states

## Security Considerations

- Never expose sensitive data in client components
- CSRF tokens for state-changing operations
- Session validation in middleware
- Security headers configured in middleware
- Input validation on both client and server

## Testing

Currently no test framework is configured. When adding tests, consider:
- Jest + React Testing Library for unit/integration tests
- Playwright or Cypress for E2E tests
- Mock Service Worker (MSW) for API mocking