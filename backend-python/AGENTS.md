# Repository Guidelines

## Project Structure & Module Organization
- `src/main.py`: FastAPI app entrypoint (`app`), CORS, health route.
- `src/database.py`: SQLAlchemy engine + session factory; loads `.env` via `python-dotenv`.
- `src/vat/`: Domain module with router (`vat_api.py`) and SQLModel entity (`vat_entity.py`).
- `.env`: Local config (e.g., `DATABASE_URL`); defaults to `sqlite:///./app.db`.
- `pyproject.toml`: Dependencies and Python version; `uv.lock` present (uses `uv`).

## Build, Test, and Development Commands
- Setup (preferred): `uv sync` — creates/updates `.venv` from `pyproject.toml`.
- Run (dev, reload): `uv run fastapi dev src/main.py` (or `uv run uvicorn src.main:app --reload`).
- Lint: `uvx ruff check .` — static checks; fix with `uvx ruff check . --fix`.
- Format: `uvx ruff format` — apply opinionated formatting.
- Alt without `uv`: create venv, `pip install 'fastapi[standard]' psycopg[binary] python-dotenv sqlmodel`, then `uvicorn src.main:app --reload`.

## Coding Style & Naming Conventions
- Python style: format with Ruff formatter; keep imports sorted; aim for 88–100 col width.
- Use absolute imports instead of relative imports
- Never use inline imports; place all imports at module top.
- Naming: modules/files `snake_case`, classes `PascalCase`, functions/vars `snake_case`.
- Routers: place under `src/<domain>/<name>_api.py`; models/entities under `*_entity.py`.

## Testing Guidelines
- Framework: `pytest` with FastAPI `TestClient` (install: `uv add --dev pytest httpx` or `pip install pytest httpx`).
- Layout: `tests/` with files `test_*.py`; keep unit tests close to domain modules.
- Run: `uv run pytest -q`.

## Commit & Pull Request Guidelines
- Commits: short, imperative summaries (e.g., "Add VAT router", "Fix DB URL parsing"); group related changes.
- PRs: clear description, linked issues, steps to verify (e.g., curl examples), and screenshots for API/UI consumers when relevant.
- Quality: run lint/format and ensure server starts clean before requesting review.

## Security & Configuration Tips
- Secrets: use `.env` (loaded by `python-dotenv`); never commit secrets.
- Database: set `DATABASE_URL` (Postgres via `psycopg[binary]` or default SQLite).
- CORS: configured in `src/main.py`; restrict origins in production.

## Quality Checks
Run these after each Python code change and again before requesting review. The task is only considered successful when there are no linter and formatting errors.
- `uvx ruff check --fix`
- `uvx ruff format`

Tip: if not using `uvx`, you can run Ruff from your virtualenv, e.g., `.venv/bin/ruff check --fix` and `.venv/bin/ruff format`.
