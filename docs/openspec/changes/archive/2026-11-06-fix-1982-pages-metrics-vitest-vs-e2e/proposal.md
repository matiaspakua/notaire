# Fix github-page metrics: Vitest vs Playwright counts (#1982)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1982 |
| Use Case | CU76 |
| Branch | `fix/1982_pages-metrics-vitest-vs-e2e` |
| Gate 1 status | passed |

## Objetivo

`frontendTestClasses` currently counts Playwright E2E specs; Vitest unit tests are invisible. UI labels E2E with that field. DEFAULTS and committed `metrics.json` are stale.

## Why

Portfolio metrics must not mislabel Playwright as frontend unit tests; charts and fallbacks must match CU76 QA inventory meaning.

## What Changes

- Count Vitest under `frontendTestClasses`; add `e2eSpecFiles` for Playwright
- Update Coverage UI labels; refresh DEFAULTS + committed `metrics.json`
- Generator writes both counts; `testClasses` = backend + Vitest + E2E

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| frontendTestClasses means Vitest files under frontend/src | Issue #1982 | Made explicit |

## Capabilities

### New Capabilities

- `github-page-metrics-test-counts`: Vitest vs E2E fields are distinct

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `github-page` | yes | metrics schema, UI, defaults, committed JSON |
| `workspace` | yes | generate-github-page-metrics.sh |

### Surface area

- Pages site metrics display only

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased note |

## Out of Scope

["Live e2eTests percentage formula (#760)"]
