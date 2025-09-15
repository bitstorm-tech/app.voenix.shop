# Default final image: runtime-with-frontend (serves SPA)
# - Build from the repository root so the Docker context includes both
#   backend/ and frontend/ directories.
# - To build API-only image from backend/ directory, pass --target runtime.

# --- Builder stage ---
FROM golang:1.25-alpine AS builder

WORKDIR /app

# Install build essentials for CGO (required by go-sqlite3)
RUN apk add --no-cache build-base ca-certificates

# Cache dependencies first
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the source
COPY . .

# Build the server binary (CGO needed for sqlite)
RUN CGO_ENABLED=1 go build -o /out/server ./cmd/server


# --- Runtime stage (Alpine) ---
FROM alpine:3.22 AS runtime

WORKDIR /app

# Install CA certificates for outbound HTTPS (e.g., Gemini API)
RUN apk add --no-cache ca-certificates

# Create non-root user and storage directory
RUN addgroup -S appuser && adduser -S -u 10001 -G appuser appuser \
    && mkdir -p /app/storage \
    && chown -R appuser:appuser /app

# Copy compiled binary and migrations directory
COPY --from=builder /out/server /usr/local/bin/server
COPY --from=builder /app/internal/database/migrations /app/internal/database/migrations

# Defaults (override at runtime as needed)
ENV ADDR=:8081 \
    STORAGE_ROOT=/app/storage \
    MIGRATIONS_URL=file:///app/internal/database/migrations

EXPOSE 8081

USER appuser

ENTRYPOINT ["/usr/local/bin/server"]


# --- Optional: Build from repo root and include frontend dist ---
# Usage (from repo root):
#   docker build -f backend/Dockerfile --target runtime-with-frontend -t voenix/backend:full .

# Build Go server when build context is the repository root (copies backend subdir)
FROM golang:1.25-alpine AS backend-builder
WORKDIR /app
RUN apk add --no-cache build-base ca-certificates
COPY backend/go.mod backend/go.sum ./
RUN go mod download
COPY backend/ .
RUN CGO_ENABLED=1 go build -o /out/server ./cmd/server

# Build frontend (Vite) from repo root
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
# Install deps with lockfile when present
COPY frontend/package.json frontend/package-lock.json* ./
RUN if [ -f package-lock.json ]; then npm ci --no-audit --no-fund; else npm install --no-audit --no-fund; fi
COPY frontend/ .
RUN npm run build

# Final runtime with baked-in frontend dist and env to serve it
FROM alpine:3.22 AS runtime-with-frontend
WORKDIR /app
RUN apk add --no-cache ca-certificates magic-wormhole-rs \
    && addgroup -S appuser && adduser -S -u 10001 -G appuser appuser \
    && mkdir -p /app/storage \
    && chown -R appuser:appuser /app

COPY --from=backend-builder /out/server /usr/local/bin/server
COPY --from=backend-builder /app/internal/database/migrations /app/internal/database/migrations
COPY --from=frontend-builder /frontend/dist /app/web

ENV ADDR=:8081 \
    STORAGE_ROOT=/app/storage \
    MIGRATIONS_URL=file:///app/internal/database/migrations \
    FRONTEND_DIST=/app/web

EXPOSE 8081
USER appuser
ENTRYPOINT ["/usr/local/bin/server"]
