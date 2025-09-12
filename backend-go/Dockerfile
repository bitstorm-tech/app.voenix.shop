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
