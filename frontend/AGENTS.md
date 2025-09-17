# Repository Guidelines

## Project Structure & Module Organization

- `src/` holds the Vite React app: UI in `components/`, routed screens in `pages/`, data hooks in `hooks/`, state stores in `stores/`, and shared types in `types/`.
- `src/api/` centralizes client adapters; mirror backend contract types when adding endpoints.
- `src/locales/` and `i18n.ts` drive copy; keep keys alphabetical and avoid free-text strings in components.
- Build artefacts land in `dist/`; never commit its contents.

## Build, Test & Development Commands

- `npm run dev` starts the Vite dev server with hot reload.
- `npm run build` performs TypeScript checking and produces the production bundle in `dist/`.
- `npm run preview` serves the last build for smoke testing; pair with `npm run build` locally before sharing links.
- `npm run lint` runs ESLint (auto-fixes staged by default); review changes before commit.
- `npm run type-check` validates stricter TS signatures without emitting JS.

## Coding Style & Naming Conventions

- Follow 2-space indentation enforced by Prettier; run `npm run format` for bulk fixes in `src/`.
- Components export PascalCase symbols from kebab-case filenames (e.g., `product-card.tsx` → `ProductCard`).
- Favor Tailwind utility classes; place bespoke styles in `src/index.css` with clear prefixes.
- Keep route definitions in `src/routes/` flat and co-locate feature helpers under the same folder.

## Testing Guidelines

- UI specs live beside features under `__tests__/` as `*.test.tsx` using React Testing Library + Jest DOM.
- Until the Vitest runner is finalized, record manual validation steps in PRs and rely on `npm run lint` + `npm run type-check` for regression coverage.
- Add fixtures under `src/testdata/` if a scenario needs reusable mocks (create the folder as needed).

## Commit & Pull Request Guidelines

- Commits use short, imperative subjects ("Add cart thumbnail") and mention requirement IDs when applicable.
- PRs include: summary, linked issues/REQ docs, screenshots for visual changes, QA notes (manual + automated), and any migration or i18n updates.
- Request reviewers for every affected surface (frontend, shared types, translations) and note rollback steps when shipping risky UI flows.

## Agent Workflow Notes

- Perform the AGENTS check before modifying files; prefer the most local instructions when conflicts arise.
- Keep changes lean and reversible—share prototypes via feature flags or preview builds instead of long-lived branches.
- Consider a task complete only after `npm run lint`, `npm run type-check`, and `npm run lint` finish without errors.
- Run `npm run format` at the very end of every task.
