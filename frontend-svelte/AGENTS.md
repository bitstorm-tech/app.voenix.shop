# Repository Guidelines

We standardise on Svelte 5.x for this app; keep dependencies and language features aligned with that major release.

## Project Structure & Module Organization

The SvelteKit app lives in `src/`, with page routes under `src/routes` and layout-specific assets in their folders. Shared UI, data helpers, and static assets sit in `src/lib`, with localization content managed through `project.inlang` and the `messages/` bundle. Public favicon, robots, and mock uploads belong in `static/`. Keep Vitest fixtures beside the code they exercise, such as `src/lib/example.spec.ts`.

## Build, Test, and Development Commands

- `npm run dev` — start the Vite dev server with hot module reload.
- `npm run build` — compile the production bundle into `.svelte-kit/output`.
- `npm run preview` — serve the built app locally for smoke checks.
- `npm run check` — run `svelte-check` against the repo `tsconfig.json`.
- `npm run lint` — enforce Prettier formatting and ESLint rules.
- `npm run test` — execute the Vitest suite once; use `npm run test:unit` to iterate.

## Coding Style & Naming Conventions

Prettier (2-space indent) and ESLint with the Svelte plugin gate formatting and import order; run `npm run format` before committing sweeping edits. Export Svelte components with PascalCase names, keep helper modules in camelCase, and organise feature code under the relevant route directory. Prefer Tailwind utility classes in markup and reserve `app.css` for globals.

## Testing Guidelines

Write Vitest specs as `ComponentName.spec.ts` or `.test.ts` files next to the implementation, reusing utilities from `vitest-setup-client.ts`. Cover success and failure paths with table-driven cases where practical. Leverage Testing Library patterns for DOM work, and scaffold future Playwright coverage with skipped tests annotated `@todo` so they surface in reports. Always run `npm run test` plus `npm run lint` before opening a PR.

## Commit & Pull Request Guidelines

Recent history shows short imperative subjects (`Add initial Svelte frontend`) and optional requirement tags (`6-PreviewStep: …`). Continue that style, expanding in the body with rationale, risk, and manual QA notes. Pull requests should summarise changes, link issues, attach UI screenshots or recordings, and call out localisation or configuration impacts. Include rollout/rollback steps when touching `hooks.server.ts` or external services, and request reviewers from both frontend and localisation when strings move.

## Security & Configuration Tips

Keep secrets in untracked `.env` files; never commit credentials. Regenerate translation artifacts with `npx @inlang/paraglide-js` whenever entries in `messages/` change. Audit `static/` for stale assets before releases to keep bundles lean.
