Voenix Go Backend (Gin + GORM)

This is a new Go backend targeting a gradual migration from the existing Python/FastAPI service. It currently implements `auth`, `vat`, `supplier` admin CRUD, the public `country` listing endpoint, and image upload/serving endpoints compatible with the Python module.

Endpoints
- POST `/api/auth/login` – Accepts JSON `{ email|username, password }` or form fields. Creates a DB-backed session and sets `session_id` HttpOnly cookie.
- GET `/api/auth/session` – Returns current session info if authenticated; otherwise 401.
- POST `/api/auth/logout` – Deletes the DB-backed session and clears cookie.
- GET `/public/*` – Serves static files from `${STORAGE_ROOT}/public` (e.g. images at `/public/images/...`).
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
  - Static files under `${STORAGE_ROOT}/public` are served at `/public/*`.
- `FRONTEND_DIST` – optional path to a built React app (`frontend/dist`). When set, backend-go serves the SPA at `/` with `/assets/*` statics and an SPA fallback for non-`/api` and non-`/public` routes.
- `GOOGLE_API_KEY` – required for `/api/ai/images` Gemini integration.
- `GEMINI_IMAGE_MODEL` – optional Gemini image model (default `gemini-2.5-flash-image-preview`).
- `TEST_MODE` – when `true`, forces the internal mock AI generator and bypasses all external AI calls (useful for tests/offline dev).

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

Serve the frontend from backend-go
- Build the UI: `cd frontend && npm run build` (outputs `frontend/dist`).
- Set `FRONTEND_DIST` to that directory. From repo root, a typical value is `./frontend/dist`.
- Start backend-go. It will:
  - Serve `/assets/*` from the dist folder
  - Serve `/` and unknown non-`/api`/`/public` routes via `index.html` (SPA fallback)
  - Keep `/api/*` and `/public/*` endpoints unchanged

Docker images
- Default (full image with frontend baked in):
  - Build from repo root (Dockerfile expects both backend-go/ and frontend/ in context):
    - `docker build -f backend-go/Dockerfile -t voenix/backend-go:latest .`
  - What this does:
    - Builds Go server from `backend-go/`
    - Builds React app from `frontend/` into `/app/web`
    - Sets `FRONTEND_DIST=/app/web` so the server serves the SPA at `/`
  - Run: `docker run --rm -p 8081:8081 voenix/backend-go:latest`

- API‑only image (no UI baked in):
  - From `backend-go/` directory: `docker build --target runtime -t voenix/backend-go:api .`
  - Or from repo root: `docker build -f backend-go/Dockerfile --target runtime -t voenix/backend-go:api .`
  - Run with external UI or set `FRONTEND_DIST` via a bind mount.

Mount a prebuilt dist into the API‑only image
- `docker run --rm -p 8081:8081 -v $(pwd)/frontend/dist:/app/web -e FRONTEND_DIST=/app/web voenix/backend-go:api`


Hot reload (Air)
- Install Air once: `go install github.com/air-verse/air@latest`
- From this directory: `air` (uses `.air.toml`), or explicitly `air -c .air.toml`
- Configured to build a temp binary (`./tmp/server`) for faster restarts.
- Air rebuilds on changes under `internal/` and `cmd/` and restarts the server.
- Prefer `go run`? Swap the `[build]` section in `.air.toml` accordingly.

Makefile shortcuts
- `make dev` – run with Air (hot reload)
- `make run` – run without hot reload

Password hash helper
- Generate hashes compatible with the Go backend's `VerifyPassword`:
  - `go run ./cmd/hashpass --password 'YourPassword' --iterations 200000`
  - Or via stdin: `echo -n 'YourPassword' | go run ./cmd/hashpass`
  - Output format: `pbkdf2_sha256$<iterations>$<salt_b64>$<hash_b64>` (32‑byte key)

Notes
- Cookie is set with `HttpOnly`, `SameSite=Lax`, `Secure=false` by default (match dev behavior of Python app). Adjust in `auth/handlers.go` if needed.
- Password verification supports `pbkdf2_sha256$<iterations>$<salt_b64>$<hash_b64>` and a legacy plain-text fallback.
