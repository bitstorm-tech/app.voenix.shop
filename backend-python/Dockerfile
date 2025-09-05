# Multi-stage Dockerfile for backend-python (FastAPI + SQLModel)
# Uses uv + pyproject/uv.lock for reproducible installs

# --- Builder: resolve and install deps with uv ---
FROM ghcr.io/astral-sh/uv:python3.13-bookworm AS builder
WORKDIR /app

# Leverage Docker layer caching for dependency install
COPY backend-python/pyproject.toml ./pyproject.toml
COPY backend-python/uv.lock ./uv.lock

# Create a virtualenv at /app/.venv and install production deps (frozen lock)
RUN uv sync --frozen --no-dev

# Copy application source
COPY backend-python/src ./src


# --- Runtime: slim Python with prebuilt virtualenv ---
FROM python:3.13-slim AS runtime
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

# Copy prebuilt venv and application code from builder
COPY --from=builder /app/.venv /app/.venv
COPY --from=builder /app/src /app/src

# Expose app port
EXPOSE 8000

# Use the venv binaries without activating the shell
ENV PATH="/app/.venv/bin:$PATH"

# Default to uvicorn. Override with `docker run ... <CMD>` as needed.
CMD [
  "uvicorn",
  "src.main:app",
  "--host","0.0.0.0",
  "--port","8000"
]

