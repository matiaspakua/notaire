# Adapt the estado-actual tie test to the #804 transition rule

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1198 |
| Use Case | CU13 – gestion status and history (also CU83) |
| Branch | `fix/1198_tie_test_after_804` |
| Gate 1 status | draft — hotfix: `main` is red (CI failure on `31caed44`, CD skipped) |

## Objetivo

Restore a green `main`. PR #1200 (#1198) added a test that changes a gestion's status with a PUT; PR #1199
(#804) then made that a 400 by design. Each PR passed CI alone; combined they fail.

## What Changes

- Rewrite `shouldReturnLaterHistoryRowWhenDatesTie` so it appends the second History row directly,
  the convention #804 adopted for the neighbouring test, and forces both rows to one instant.
- No production code change; the #1198 fix is untouched.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Status changes on an existing gestion go through `POST /gestiones/{id}/transition`, not PUT | #804 | Respected, not changed |
| Ties on history date resolve to the higher id | #1198 | Unchanged |

## Capabilities

### New Capabilities

- `estado-actual-tie-test`: the tie-break regression test stays valid under the #804 rule.

### Modified Capabilities

- (none; the `estado-actual-tie-break` requirement from the original #1198 change is unchanged)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes (tests only) | One integration test rewritten |
| `frontend` | no | — |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |

### Surface area

- Entities / Endpoints / Flyway / `.env` / dependencies: none

### Architecture review

Test-only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | None: no user-visible change; the #1198 entry stands |
| `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md` | None: #1198 already listed |

## Out of Scope

- Any production code; #804's guard.
- A process change to catch merge-order conflicts between green PRs (noted in the PR as a gap).
