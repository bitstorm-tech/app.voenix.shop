# Repository Guidelines

## Project Structure & Module Organization
- `cmd/server/` – main entrypoint (`main.go`).
- `internal/auth/` – login/session, roles, middleware.
- `internal/supplier/` – admin CRUD for suppliers and countries.
- `internal/vat/` – admin CRUD for VAT rates.
- `internal/database/` – DB opener, DSN resolution, auto‑migrate, session TTL.
- `internal/util/` – helpers (e.g., password verification).
- `.env` / `.env.example` – environment configuration; loaded automatically.
- `tmp/` – hot‑reload build output (Air).

## Build, Test, and Development Commands
- Prereqs: Go 1.22+ (module targets 1.23), SQLite (optional), Postgres (optional).
- Install deps once: `go get` (from repo root).
- Run (no reload): `make run` or `go run ./cmd/server`.
- Hot reload (Air): `go install github.com/air-verse/air@latest` then `make dev`.
- Format: `make fmt` (runs `go fmt ./...`).
- Test: `go test ./...` (add `-v` or `-cover` as needed).

## Coding Style & Naming Conventions
- Use `gofmt`/`go fmt` and idiomatic Go. No manual formatting.
- Packages: short, lowercase (`auth`, `database`). Files: lowercase with underscores if needed.
- Exported identifiers use PascalCase; unexported use camelCase.
- Errors: return `error` values; prefer wrapping/context where helpful; avoid panics in handlers.
- HTTP handlers should validate input and return structured JSON errors (`{"detail": "..."}`).

## Testing Guidelines
- Place tests alongside code as `*_test.go` using the standard `testing` package.
- Prefer table‑driven tests and focused unit tests for handlers, services, and utilities.
- Run `go test ./...` locally; aim to cover auth flows, DSN parsing, and model validations.

## Commit & Pull Request Guidelines
- Commits: short, imperative summaries (e.g., "Add VAT update handler", "Refactor auth session logic").
- PRs: include purpose, linked issues, steps to verify, and sample requests.
  - Example: `curl -X POST localhost:8081/api/auth/login -d 'email=a@b.com&password=...'`.
- Keep diffs focused; update `README.md` or `.env.example` when changing config or behavior.
- Ensure `make fmt` and `go build ./...` succeed before requesting review.

## Security & Configuration Tips
- Env vars: `DATABASE_URL` (sqlite or Postgres), `AUTO_MIGRATE=true` for migrations, `SESSION_TTL_SECONDS`, `ADDR` (default `:8081`), `CORS_ALLOWED_ORIGINS`.
- `.env` is auto‑loaded (current dir and `backend-go/.env`); use `ENV_FILE` to override.
- Cookies are `HttpOnly`/`SameSite=Lax`; set `Secure=true` behind HTTPS in production.
- Avoid logging secrets and PII; validate and sanitize all inputs.

## Quality Checks
Run these commands after each code change in the Golang backend `backend-go` and again before requesting review. The task is only considered successful when there are no linter and formatting errors.
- `make vet`
- `make lint`
- `make build`
- `make fmt`
