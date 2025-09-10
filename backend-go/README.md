Voenix Go Backend (Gin + GORM)

This is a new Go backend targeting a gradual migration from the existing Python/FastAPI service. It currently implements `auth`, `vat`, `supplier` admin CRUD, the public `country` listing endpoint, and image upload/serving endpoints compatible with the Python module.

Endpoints
- POST `/api/auth/login` – Accepts JSON `{ email|username, password }` or form fields. Creates a DB-backed session and sets `session_id` HttpOnly cookie.
- GET `/api/auth/session` – Returns current session info if authenticated; otherwise 401.
- POST `/api/auth/logout` – Deletes the DB-backed session and clears cookie.
- GET `/api/public/countries` – Public list of countries `{ id, name, createdAt, updatedAt }`.
 - POST `/api/admin/images` – Admin upload: `file` + (`request` JSON or discrete `imageType`, `cropX|Y|Width|Height`). Converts to PNG and stores under `STORAGE_ROOT`.
 - GET `/api/admin/images/prompt-test/:filename` – Serve admin prompt test image.
 - DELETE `/api/admin/images/prompt-test/:filename` – Delete admin prompt test image.
- GET `/api/user/images/:filename` – Serve current user's private image.
- GET `/api/user/images` – List current user's images with pagination and sorting.
 - POST `/api/ai/images` – Admin: Upload `image` + `prompt` (+ optional `n`, `provider`). Forwards to Gemini to edit/manipulate, stores PNGs under `STORAGE_ROOT/private/images/0_prompt-test`, returns base64 images.

Models
- `users`, `roles`, `user_roles` (many-to-many), `sessions` tables are modeled to match the Python SQLModel definitions.

Configuration
- `DATABASE_URL` – DSN string. If not set, defaults to `sqlite://./app.db` (file-based sqlite relative to the process working directory). Postgres URLs are also supported.
- `AUTO_MIGRATE` – if `true`, runs GORM automigration on startup for auth, VAT, countries, and suppliers tables.
- `SESSION_TTL_SECONDS` – optional override for session expiry (default 7 days).
- `ADDR` – address/port to bind (default `:8081`).
- `CORS_ALLOWED_ORIGINS` – comma-separated list of allowed origins (include `*` to allow any; dev only). Uses gin-contrib/cors.
 - `STORAGE_ROOT` – required filesystem root for image storage (e.g. `./storage`). The server creates subdirectories as needed:
   - `${STORAGE_ROOT}/public/images`
   - `${STORAGE_ROOT}/private/images`
 - `GOOGLE_API_KEY` – required for `/api/ai/images` Gemini integration.
 - `GEMINI_IMAGE_MODEL` – optional Gemini image model (default `gemini-2.5-flash-image-preview`).

.env support
- The server loads environment variables from `.env` automatically if present.
- Search order: current working directory `.env`, then `backend-go/.env` (useful when running from repo root).
- You can specify a custom path with `ENV_FILE=/path/to/.env`.
- See `backend-go/.env.example` for a template.

Running
1) Ensure Go 1.22+
2) Copy `.env.example` to `.env` and adjust as needed (or export vars)
3) Install modules once: `go get` (will fetch dependencies)
4) `go run ./cmd/server` (or `make run`)

Hot reload (Air)
- Install Air once: `go install github.com/air-verse/air@latest`
- From this directory: `air` (uses `.air.toml`), or explicitly `air -c .air.toml`
- Configured to build a temp binary (`./tmp/server`) for faster restarts.
- Air rebuilds on changes under `internal/` and `cmd/` and restarts the server.
- Prefer `go run`? Swap the `[build]` section in `.air.toml` accordingly.

Makefile shortcuts
- `make dev` – run with Air (hot reload)
- `make run` – run without hot reload

Notes
- Cookie is set with `HttpOnly`, `SameSite=Lax`, `Secure=false` by default (match dev behavior of Python app). Adjust in `auth/handlers.go` if needed.
- Password verification supports `pbkdf2_sha256$<iterations>$<salt_b64>$<hash_b64>` and a legacy plain-text fallback.
