# Shard Playwright E2E to shorten the PR critical path

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1258 |
| Use Case | CU76 |
| Branch | `cursor/ci-1258-playwright-shards-cf98` |
| Gate 1 status | complete |
| Parent | #1197 Phase 0.3 |
| Depends on | #1257 path-scoped CI (merged) |

## Objetivo

Playwright UI E2E is on the PR critical path. Sharding to 3 workers targets ≤6 min
E2E wall-clock and helps overall PR ≤8 min, without weakening the merge gate.

## What Changes

- `playwright-e2e.yml`: matrix `shard: [1,2,3]` with `--shard=i/3`
- `e2e-merge-reports` job named `UI E2E Tests (Playwright)` (heavy-CI check name)
- Blob reporter in CI `playwright.config.ts`; merge-reports fail-closed
- Aggregator needs merge job; path-scoped skip still accepted
- `check-heavy-ci.sh` accepts `skip`/`skipped` for path-scoped docs PRs
- Invariant tests for matrix + merge job

## Reglas de negocio

| Rule | Source | Change |
|------|--------|--------|
| Heavy CI requires Playwright success before merge | CONSTITUTION / CI-MERGE-GATE | Unchanged (merge job keeps check name) |
| Docs-only PRs skip E2E leaves | #1257 | Made explicit in check-heavy-ci skip accept |
| E2E wall ≤6 min target | #1258 / #1197 P0.3 | Made explicit |

## Capabilities

### New Capabilities

- `playwright-shards`: sharded E2E with merged report and fail-closed merge job.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `testing` | yes | blob reporter in Playwright config |
| `workspace` | yes | invariants; check-heavy-ci skip accept |
| `.github` | yes | playwright-e2e.yml shards + merge |
| `docs` | yes | OpenSpec + CI-PREFLIGHT / CHANGELOG |

### Surface area

- Entities / Endpoints / Flyway / Configuration / Dependencies: none
- **BREAKING** for API clients: no

### Architecture review

CI-only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CI-PREFLIGHT.md` / `CI-MERGE-GATE.md` | Shard + skip notes |
| `REPO-SPLIT-PLAN.md` P0.3 | Status |
| `CHANGELOG.md` | Unreleased |
