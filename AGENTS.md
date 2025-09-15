# Repository Guidelines

## AGENTS Compliance (Nested AGENTS.md)
- Before editing any file, the agent must perform an "AGENTS check":
  - Search the repo for all files named `AGENTS.md`.
  - For each file you plan to touch, determine the applicable `AGENTS.md` by choosing the nearest one in its directory tree (deeper files take precedence over parent/root files).
  - Read all applicable `AGENTS.md` files.
- In case of conflicting instructions, follow the most deeply nested `AGENTS.md` for the files being changed. Direct user instructions still take precedence over any `AGENTS.md`.

## Project Structure & Module Organization
- `backend/`: Kotlin Spring Boot (Spring Modulith). Tests in `backend/src/test/kotlin`.
- `frontend/`: React + TypeScript (Vite). UI, state, and assets in `frontend/src`.
- `frontend-nextjs/`: Legacy/experimental Next.js app (do not modify unless scoped).
- Shared docs: `CLAUDE.md`, `CHANGELOG.md`, `requirements/`, `docker-compose.yml`.

## Build, Test, and Development Commands
- Backend: `cd backend && ./gradlew build` – compile and package.
- Backend (run): `./gradlew bootRun` – start API at `http://localhost:8080`.
- Backend (tests): `./gradlew test` – run JUnit 5 tests.
- Frontend: `cd frontend && npm install && npm run dev` – start app at `http://localhost:3000`.
- Frontend (build): `npm run build` and preview with `npm run preview`.

## Quality Gates
A task is only considered completed if all of the following succeed. Run commands from the respective app directories unless noted.

### Backend (from `backend/`)
- `./gradlew build` returns without errors
- `./gradlew ktlintFormat` returns without errors

### Frontend (from `frontend/`)
- `npm run type-check` returns without errors
- `npm run build` returns without errors
- `npm run format` is executed

If any of these commands return errors, fix them immediately before considering the task done.

## Coding Style & Naming Conventions
- Kotlin: `ktlint` and `detekt` enforced. Run `./gradlew ktlintCheck`, `./gradlew ktlintFormat`, `./gradlew detekt`.
- TypeScript/React: `eslint` + `prettier`. Run `npm run lint`, `npm run format`, `npm run type-check`.
- Naming: React components `PascalCase`, hooks `useCamelCase`, files match export; Kotlin packages under `com.jotoai.voenix.*`.

## Testing Guidelines
- Backend: JUnit 5 with Spring Boot; module tests for Modulith boundaries. Run `./gradlew test`.
- Frontend: Testing Library available; no default `test` script. Prefer component tests colocated under `src/**/__tests__` or `*.test.tsx`.
- Aim for meaningful coverage of domain logic and critical UI flows.

## Commit & Pull Request Guidelines
- Commits: short, imperative summaries (e.g., “Fix detekt findings”, “Refactor auth module”). Group related changes.
- PRs: clear description, linked issues, screenshots for UI changes, and steps to verify. Ensure CI passes and format/lint checks are clean.
- Renames: use `git mv` instead of `mv` to preserve history and avoid stale paths.

## Security & Configuration Tips
- Backend reads `.env` via spring-dotenv; configure DB (`voenix_java`) and API keys there.
- Frontend talks to API at `http://localhost:8080`. Do not store secrets in `localStorage`.
- Validate inputs on both sides; keep error messages non-sensitive.
- When migrating modules, preserve Spring Modulith boundaries and update docs.
