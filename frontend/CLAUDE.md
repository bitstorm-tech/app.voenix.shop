# Frontend CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with the frontend code in this repository.

## Frontend Overview

The frontend is a React TypeScript single-page application (SPA) built with Vite that provides a multi-step wizard for creating custom mugs with AI-generated images.

## Development Commands

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

- **Use frontend-expert agent** for all React/TypeScript implementations
- Don't use `React.memo`, `useCallback` or `useMemo` since the React compiler handles these optimizations automatically
- Run the formatter in the frontend folder for all new or changed files at the end of the implementation
- Make all React components and web pages responsive → working on Mobile and Desktop
- Use TypeScript strict mode - ensure proper typing for all components
- Validate forms with proper error handling and user feedback
- Test admin features with different role permissions

## Architecture

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

## Key Technologies

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

## Admin Panel

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

## Frontend Security

- **Protected routes** using React Router guards
- **Automatic redirect** to login for unauthorized access
- **Token storage** in HTTP-only cookies (via backend)
- **Session persistence** across page refreshes
- **Role-based UI rendering** for admin features

## Development Notes

1. **Package Manager**: Frontend uses npm (package-lock.json present)
2. **TypeScript**: Strict mode enabled - ensure proper typing
3. **API Communication**: Frontend expects backend on http://localhost:8080
4. **Linting**: Frontend needs ESLint configuration
5. **Responsive Design**: All components must work on mobile and desktop

## Frontend Setup

```bash
cd frontend
npm install
npm run dev         # Runs on http://localhost:3000
```

## Testing Workflow

```bash
cd frontend
npm run type-check  # TypeScript type checking
npm run lint        # Run linter
npm run format      # Format code
```

## Working with Admin Features

1. **Access Admin Panel**: Navigate to `/admin` routes
2. **Test Prompt Generation**: Use the Prompt Tester at `/admin/prompts/tester`
3. **Manage Content**: Use the admin interfaces for mugs, prompts, and users

## Important UI/UX Guidelines

- Use Tailwind CSS v4 for all styling
- Leverage Radix UI components for accessibility
- Ensure all forms have proper validation and error messages
- Implement loading states for async operations
- Make all interfaces responsive for mobile and desktop
- Use Lucide React icons consistently throughout the app
- Follow the existing component patterns in the codebase