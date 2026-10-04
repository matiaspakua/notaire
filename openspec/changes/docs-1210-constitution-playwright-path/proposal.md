# Amend the Constitution for the Playwright suite in testing/e2e

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1210 (follows #1192, PR #1212; umbrella #1190) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `docs/1210_constitution_playwright_path` (stacked on `refactor/1192_playwright_to_testing` until #1212 merges) |
| Gate 1 status | draft — Constitution §12: dedicated PR, reviewed by the Owner |

## Objetivo

#1192 moved the Playwright suite to `testing/e2e`. `CONSTITUTION.md` still names `frontend/tests/e2e/`
and `cd frontend && npx playwright test`, so the highest-authority document contradicts the repository.
This change fixes the wording, nothing else.

## What Changes

- §4 (test folders), §5 step 15 (command), §7 (test table) and §13 (tooling map) point at `testing/e2e`.
- The `CONSTITUTION.md` exemption in `scripts/test_testing_standalone.py` is removed, so the stale-path
  guard now covers the Constitution.
- No process step, gate or rule is added or removed.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An amendment is a dedicated PR reviewed by the code owner | Constitution §12 | Respected |
| The Constitution MUST NOT contradict the repository layout | Constitution preamble | Made explicit (guard) |

## Capabilities

### New Capabilities

- `constitution-e2e-location`: the Constitution names the real Playwright location, enforced by a guard.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs | yes | `CONSTITUTION.md` wording |
| Scripts / tests | yes | the guard loses its `CONSTITUTION.md` exemption |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Wording only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | §4, §5 step 15, §7 paths and command; "Last reviewed" date |
| `CHANGELOG.md` | one line |
