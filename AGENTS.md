# Repository Guidelines

- We prefer simple, lean solutions delivered quickly over over-engineered complexity.
- When renaming or moving tracked files or directories, always use `git mv` instead of `mv` so history stays intact.

## AGENTS Compliance (Nested AGENTS.md)
- Before editing any file, the agent must perform an "AGENTS check":
    - Search the repo for all files named `AGENTS.md`.
    - For each file you plan to touch, determine the applicable `AGENTS.md` by choosing the nearest one in its directory tree (deeper files take precedence over parent/root files).
    - Read all applicable `AGENTS.md` files.
- In case of conflicting instructions, follow the most deeply nested `AGENTS.md` for the files being changed. Direct user instructions still take precedence over any `AGENTS.md`.

## Project Structure & Module Organization
- `backend/`: Go services in `cmd/server`, `cmd/migrate`, with internal app logic under `internal/`. Temporary build artifacts in `bin/` and `tmp/`.
- `frontend/`: Vite + React storefront; source under `src/`, build output under `dist/` for nginx.
- `frontend-nextjs/`: Next.js exploration; keep shared types in sync before promoting features.
- `storage/`: Local dev assets (`public/` seeds, `private/` uploads). Keep large generated files out of git.

## Build, Test, and Development Commands
- Backend: `make dev` (hot reload via Air), `make run`, `make build`, `make check` (fmt, vet, lint, test, build).
- Backend tests: `make test` or `go test ./cmd/... ./internal/...`.
- Frontend (Vite): `npm run dev`, `npm run build`, `npm run preview`, `npm run lint`, `npm run type-check`.
- Next.js: `npm run dev --workspace frontend-nextjs`, `npm run build --workspace frontend-nextjs`.

## Coding Style & Naming Conventions
- Go: format with `gofmt`; keep files scoped to a feature (`inventory_service.go`), PascalCase exports, camelCase locals, `_test.go` for suites.
- TypeScript/React: ESLint + Prettier enforce 2-space indent and import order; components in `src/components/` use kebab-case filenames and PascalCase exports.
- Styling: Tailwind-first; custom utilities in `src/styles/`. Document shared animations before reuse.

## Testing Guidelines
- Backend: colocate table-driven `_test.go` suites; exercise HTTP handlers with `httptest` and seed fixtures under `testdata/` when needed.
- Run `make test` before PR; add integration coverage for new database flows.
- Frontend: add Testing Library specs as `*.test.tsx` beside components. Run `npm run lint` and `npm run type-check`; record manual UI validation until Vitest harness is configured.

## Commit & Pull Request Guidelines
- Commits use short imperative subjects (e.g., `Add prompt price to cart`) and reference requirement IDs in bodies when applicable.
- PRs include a summary, linked issues or requirements, screenshots for UI changes, migration and storage notes, plus rollback steps. Request reviewers for every affected surface (backend, frontend, infra).
