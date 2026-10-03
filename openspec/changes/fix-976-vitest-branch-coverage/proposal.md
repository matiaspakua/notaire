# Frontend Vitest coverage floor — root cause, ratchet, and policy docs

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #976 |
| Use Case | **CU76** – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-976-vitest-branch-coverage-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue ahead of this pack |

## Objetivo

Issue #976 reported Frontend CI Vitest failing on `main` because branch
coverage (5.85%) sat under the enforced 6% global threshold. On current
`origin/main` (2026-10-03 tip researched for this pack) `npx vitest run
--coverage` already reports ~8.6% branches and recent Frontend CI runs are
green — so the immediate red CI signal is cleared. Remaining Gate 1 work is to
**document the root cause**, set a deliberate raise-only ratchet floor with
comfortable headroom under measured coverage (mirroring JaCoCo), and record the
policy in permanent quality docs so the floor cannot be silently lowered.

## What Changes

- Record root cause in the change design + permanent docs: threshold was an
  aspirational/low floor set 2026-07-29 with headroom; later frontend growth
  briefly dipped branches under 6%; subsequent tests raised coverage above the
  floor again (re-measure at implement time on updated `main`).
- Raise `frontend/vitest.config.ts` coverage thresholds to a deliberate floor
  **below** freshly measured coverage (recommended starting point at prep time:
  branches ≥ 8 with statements/lines/functions similarly ratcheted — exact
  numbers chosen after implement-time measurement, never above measured, never
  lowered without ADR/exception).
- Add/extend a small guard test or CI note proving thresholds remain
  raise-only / documented.
- Document the frontend Vitest ratchet policy in
  `.claude/rules/code-quality.md` and/or
  `docs/300-development/303-testing/FRONTEND-TESTING-GUIDE.md` (and CHANGELOG).
- Confirm `Frontend CI — Build, Typecheck & Test` stays green on `main` after
  the threshold bump.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Frontend Vitest global coverage thresholds are a raise-only ratchet floor | CU76; #976 AC; mirrors JaCoCo policy | Made explicit |
| Thresholds MUST stay below measured coverage with documented headroom | CU76; #976 AC | New (policy) |
| Lowering a floor requires a documented reason (ADR or sdlc exception) | CU76; Constitution P8 | Made explicit |
| Root cause of the historical <6% failure MUST be written down | #976 AC | New (docs) |

## Capabilities

### New Capabilities

- `frontend-vitest-coverage-floor`: documented raise-only Vitest coverage
  thresholds, root-cause note, and CI-green confirmation for frontend unit
  coverage.

### Modified Capabilities

- (none under `openspec/specs/` today encode the Vitest floor policy)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | `vitest.config.ts` thresholds; optional unit tests; docs |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | maybe | only if workflow comments/docs need alignment; job already runs `--coverage` |

### Surface area

- Entities / endpoints / Flyway / `.env`: none
- Dependencies: none
- **BREAKING**: none for API; PRs that shrink coverage below the new floor will
  fail Vitest until tests are added (intentional)

### Architecture review

Follows existing Vitest + JaCoCo ratchet pattern. No new ADR unless the team
chooses to **lower** a threshold (not expected). Not an application
architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `.claude/rules/code-quality.md` | Frontend Vitest raise-only floors + current numbers |
| `docs/300-development/303-testing/FRONTEND-TESTING-GUIDE.md` (or TEST-COVERAGE-STRATEGY) | Policy + how to ratchet |
| `frontend/vitest.config.ts` comments | Align comment dates/numbers with new floor |
| `CHANGELOG.md` | chore/test entry for floor raise + root cause |

## Out of Scope

- Driving frontend coverage to the aspirational 80% product target in this
  change (ratchet only).
- Backend JaCoCo changes.
- Playwright E2E product coverage.
- #1050 / #1058 feature work.
