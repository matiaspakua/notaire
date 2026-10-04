# Wire the top-level guards into CI

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1209 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `ci/1209_wire_guards` (stacked on `refactor/1192_playwright_to_testing`) |
| Gate 1 status | draft |

## Objetivo

CI and `preflight.sh` run `python3 -m unittest discover -s scripts/tests`. Twelve top-level
`scripts/test_*.py` guards had no wrapper there, so nothing enforced them. This change wires them
and adds a meta-guard so the gap cannot recur.

## What Changes

- A wrapper in `scripts/tests/` for each unwrapped guard, in the established style.
- `scripts/tests/test_guard_wrappers.py`: fails when a top-level guard has no wrapper (exemptions need a reason; none are used).
- `test_staging_kustomize.py` skips instead of erroring when `kustomize` is absent.
- `CHANGELOG.md` repeated `###` headings under `[Unreleased]` merged; the #1185 structure guard was already failing on `main`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A guard CI never runs is not a guard | #1209 | Made explicit |

## Capabilities

### New Capabilities

- `guard-wrapper-coverage`: every top-level guard is discovered by CI.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Scripts / tests | yes | wrappers, meta-guard, kustomize skip |
| Docs | yes | CHANGELOG headings |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Test plumbing only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | headings merged; one entry |
