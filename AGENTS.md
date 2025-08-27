# Repository Guidelines

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

## Security & Configuration Tips
- Backend reads `.env` via spring-dotenv; configure DB (`voenix_java`) and API keys there.
- Frontend talks to API at `http://localhost:8080`. Do not store secrets in `localStorage`.
- Validate inputs on both sides; keep error messages non-sensitive.
- When migrating modules, preserve Spring Modulith boundaries and update docs.

