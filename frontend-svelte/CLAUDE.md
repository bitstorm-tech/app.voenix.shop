# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a SvelteKit application using Svelte 5, Vite, and Tailwind CSS v4. The project uses the Node adapter for deployment and includes i18n support via Paraglide.

## Development Commands

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production
- `npm run preview` - Preview production build locally
- `npm run check` - Run SvelteKit sync and type checking with svelte-check
- `npm run check:watch` - Run type checking in watch mode
- `npm run lint` - Check code formatting with Prettier and run ESLint
- `npm run format` - Format all files with Prettier
- `npm test` - Run unit tests (Vitest) once
- `npm run test:unit` - Run unit tests in watch mode

## Testing Architecture

The project uses Vitest with two separate test configurations:

- **Client tests**: Run in Playwright browser environment (`*.svelte.test.ts` or `*.svelte.spec.ts` files in `src/`), exclude `src/lib/server/`
- **Server tests**: Run in Node environment (all other `*.test.ts` or `*.spec.ts` files in `src/`)
- Client test setup is configured in `vitest-setup-client.ts`

## Project Structure

- `src/routes/` - SvelteKit file-based routing; `+page.svelte` for pages, `+layout.svelte` for layouts
- `src/lib/` - Reusable components and utilities; importable via `$lib` alias
- `src/lib/paraglide/` - Auto-generated i18n runtime (do not edit manually)
- `messages/` - i18n message files (`en.json`, `de.json`)
- `project.inlang/` - Paraglide i18n configuration
- `static/` - Static assets served from root
- `.svelte-kit/` - Generated SvelteKit files (do not edit)

## Internationalization (i18n)

- Uses Paraglide (via `@inlang/paraglide-js`) for type-safe i18n
- Message files in `messages/{locale}.json` are compiled to `src/lib/paraglide/`
- Supported locales: `en` (base), `de`
- Middleware configured in `src/hooks.server.ts` handles locale detection and page chunk transformation
- The `%paraglide.lang%` placeholder in HTML is replaced with current locale

## Styling Conventions

- Tailwind CSS v4 via `@tailwindcss/vite` plugin
- Global styles in `src/app.css`
- Prettier configured with `useTabs: true`, `singleQuote: true`, `printWidth: 100`
- Prettier plugins: `prettier-plugin-svelte`, `prettier-plugin-tailwindcss`

## Code Style

- ESLint configured with TypeScript, Svelte, and Prettier integration
- `no-undef` rule disabled (TypeScript handles this)
- Svelte files use TypeScript via `<script lang="ts">`
- Strict TypeScript settings enabled in `tsconfig.json`

## SvelteKit Configuration

- Adapter: `@sveltejs/adapter-node` (for Node.js deployment)
- Preprocessor: `vitePreprocess` for TypeScript, PostCSS, etc.
- Path aliases: `$lib` maps to `src/lib` (configured by SvelteKit)
