---
name: Agent Task Ticket
about: Structured ticket for GPT‑5 high to implement
title: "feat: <short imperative summary>"
labels: ["agent", "enhancement"]
assignees: []
---


> **Note for the agent:** Follow the **Output contract** in the preamble (see `AGENTS.md`). If something is ambiguous, proceed with sensible defaults, list **Assumptions**, and continue unless blocked by secrets/irreversible schema changes.


## Title
<short imperative summary>


## Goal
<what outcome the user sees / business impact in 1–2 sentences>


## Context
<links to files, routes, models, user flows; interfaces/contracts if any>


## Constraints & Non-Goals
- <performance/security/SLA limits, UX constraints>
- Non-goals: <explicitly out of scope>


## Acceptance Criteria (Gherkin)
- Given <state>, when <action>, then <observable result>.
- Edge cases: <list>


## Inputs & Interfaces
- **Request/Response shapes:** JSON/DTOs/form fields
- **DB schema deltas:** tables/columns/indexes
- **Events/Jobs/Policies:** touched/added


## Env & Tooling
- PHP/Laravel versions; Pest/Larastan settings; Vite build notes
- Feature flags (e.g., `FEATURE_FOO` default off, rollout %)
- External deps/mocks


## Deliverables
- Working feature with tests; `pint` & `phpstan` passing
- **Unified diff** + **full file contents** per Output contract


## Run Commands
```bash
php artisan migrate
php artisan test --filter <SomethingTest>
phpstan analyse
npm run build