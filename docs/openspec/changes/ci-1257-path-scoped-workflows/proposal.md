# Path-scoped CI so docs/OpenSpec/agent-only PRs skip Java and E2E

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1257 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/ci-1257-path-scoped-cf98` |
| Gate 1 status | draft |
| Parent | #1197 Phase 0.2 / ADR-024 |

## Why

41% of recent PRs touch no application code but still pay ~12 min of Java + Playwright.
Phase 0.2 of #1197 needs path classification so docs/OpenSpec/agent-only PRs stop starving
runners, without breaking branch-protection required check names.

## Objetivo

Classify changed paths and skip irrelevant heavy jobs while keeping the six required check
names green via aggregators that treat intentional `skipped` as success.

## What Changes

- Add an always-run `changes` path-filter job (`dorny/paths-filter@v3`) to `ci.yml`,
  `frontend-ci.yml`, `playwright-e2e.yml`, and `openapi-contract.yml`.
- Gate leaf jobs with `if:` on filter outputs; non-PR events force all outputs `true`.
- Suite aggregators `CI`, `Frontend CI`, `Playwright E2E` (and `OpenAPI Contract` suite)
  accept `success|skipped`.
- Extend `workspace/tests/test_ci_workflow_invariants.py`.
- Update `docs/300-development/CI-PREFLIGHT.md`, REPO-SPLIT-PLAN P0.2 note, CONSTITUTION §10/§13.
- `CHANGELOG.md` Unreleased entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Docs-only PRs must not run Java or Playwright leaf jobs | #1257, #1197 AC | Made explicit |
| Required check names remain stable for branch protection | #1257 | Made explicit |
| `main` / dispatch / schedule always run the full suite | #1257 | Made explicit |
| Never use workflow-level `on.paths` that starve required checks on main | #1257 | New |

## Capabilities

### New Capabilities

- `path-scoped-ci`: path classifier + aggregators skip irrelevant heavy jobs on PRs.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `workspace` | yes | workflow invariant tests; sdlc docs pointers |
| `docs` | yes | CI-PREFLIGHT, REPO-SPLIT-PLAN note, CONSTITUTION |
| `(.github)` | yes | four workflows (workspace `extra_paths`) |
| `backend-api` / `frontend` / `testing` | no code | runtime CI scheduling only |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: GitHub Action `dorny/paths-filter@v3` (pin in workflow)

### Architecture review

No production API change. Aligns with ADR-024 Phase 0.2 and ADR-026 module boundaries
(docs vs product paths).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/CI-PREFLIGHT.md` | path-scoped behavior |
| `docs/300-development/REPO-SPLIT-PLAN.md` | P0.2 progress note |
| `CONSTITUTION.md` | path-scoped CI agent rule + tooling map |
| `CHANGELOG.md` | Unreleased |
