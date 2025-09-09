Voenix Go Backend (Gin + GORM)

This is a new Go backend targeting a gradual migration from the existing Python/FastAPI service. For now, it implements only the `auth` module equivalent to `backend-python/src/auth`.

Endpoints
- POST `/api/auth/login` – Accepts JSON `{ email|username, password }` or form fields. Creates a DB-backed session and sets `session_id` HttpOnly cookie.
- GET `/api/auth/session` – Returns current session info if authenticated; otherwise 401.
- POST `/api/auth/logout` – Deletes the DB-backed session and clears cookie.

Models
- `users`, `roles`, `user_roles` (many-to-many), `sessions` tables are modeled to match the Python SQLModel definitions.

Configuration
- `DATABASE_URL` – DSN string. If not set, defaults to `sqlite://./app.db` (file-based sqlite relative to the process working directory). Postgres URLs are also supported.
- `AUTO_MIGRATE` – if `true`, runs GORM automigration on startup for the auth tables.
- `SESSION_TTL_SECONDS` – optional override for session expiry (default 7 days).
- `ADDR` – address/port to bind (default `:8081`).
- `CORS_ALLOWED_ORIGINS` – comma-separated list of allowed origins (include `*` to allow any; dev only). Uses gin-contrib/cors.

.env support
- The server loads environment variables from `.env` automatically if present.
- Search order: current working directory `.env`, then `backend-go/.env` (useful when running from repo root).
- You can specify a custom path with `ENV_FILE=/path/to/.env`.
- See `backend-go/.env.example` for a template.

Running
1) Ensure Go 1.22+
2) Copy `.env.example` to `.env` and adjust as needed (or export vars)
3) Install modules once: `go get` (will fetch dependencies)
4) `go run ./cmd/server`

Notes
- Cookie is set with `HttpOnly`, `SameSite=Lax`, `Secure=false` by default (match dev behavior of Python app). Adjust in `auth/handlers.go` if needed.
- Password verification supports `pbkdf2_sha256$<iterations>$<salt_b64>$<hash_b64>` and a legacy plain-text fallback.
