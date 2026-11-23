# <!-- change title -->

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2466 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2466_size_renames` |
| Gate 1 status | passed |

## Objetivo

`aisdlc size` counts a renamed file as a full delete plus a full add because `repo.numstat()` runs `git diff --numstat --no-renames`. This inflates the review-budget count and causes false-positive gate failures (e.g., PR #2465 reported 4576 counted lines for a 598+/84− diff).

## What Changes

- `repo.numstat()` will detect renames using `git diff -M --numstat -z` so that a pure rename counts 0 lines and a rename with edits counts only the edited lines.
- The path reported (and matched against `[size] exclude`) is the **new** path of a renamed file.
- No breaking changes to the `aisdlc size` CLI interface.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A pure rename counts 0 lines in the review budget | CU76 | New |
| A rename with edits counts only the edited lines, reported under the new path | CU76 | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-framework`: Requirement "The review budget is checked on the diff" — add two scenarios for rename handling (pure rename = 0 lines; rename with edits = edited lines only, new path).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `framework/ai-sdlc` | yes | `repo.numstat()` rename detection; tests; changelog |

### Surface area

- Entities: n/a
- Endpoints: n/a
- Database (Flyway `V{n}`): n/a
- Configuration / `.env`: n/a
- Dependencies: n/a

### Write set

- `framework/ai-sdlc/aisdlc/repo.py`
- `framework/ai-sdlc/aisdlc/diffchecks.py`
- `framework/ai-sdlc/tests/test_diffchecks.py`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/fix-2466-size-renames/**`
- `docs/openspec/specs/ai-sdlc-framework/spec.md`

### Architecture review

No architectural change. This is a bug fix in the internal `repo.numstat()` helper. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/CHANGELOG.md` | One line under `[Unreleased] ### Fixed` |

## Out of Scope

- Other kit gates (commits, tdd, protected, scope, evidence) — out of scope.
- Copied files (`-C` flag) — out of scope; only renames (`-M`) are addressed.
