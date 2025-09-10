# syntax=docker/dockerfile:1

# --- Builder stage ---
FROM golang:1.23-bookworm AS builder

WORKDIR /app

# Install build essentials for CGO (required by go-sqlite3)
RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Cache dependencies first
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the source
COPY . .

# Build the server binary (CGO needed for sqlite)
RUN CGO_ENABLED=1 go build -o /out/server ./cmd/server


# --- Runtime stage ---
FROM debian:bookworm-slim AS runtime

WORKDIR /app

# Install CA certificates for outbound HTTPS (e.g., Gemini API)
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user and storage directory
RUN useradd -r -u 10001 -g root appuser \
    && mkdir -p /app/storage \
    && chown -R appuser:root /app

# Copy compiled binary
COPY --from=builder /out/server /usr/local/bin/server

# Defaults (override at runtime as needed)
ENV ADDR=:8081 \
    STORAGE_ROOT=/app/storage

EXPOSE 8081

USER appuser

ENTRYPOINT ["/usr/local/bin/server"]

