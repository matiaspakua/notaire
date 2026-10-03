# Remove orphaned files that deliver no value

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #585 (scope widened by the Owner on 2026-10-03) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/585_remove_orphans` |
| Gate 1 status | draft — Owner approved the scope ("remove orphan or unused scripts, files and any other temporary file") |

## Objetivo

Delete tracked files that nothing builds, runs or documents as live, so searches and QA
hand-over are not polluted: nine cURL scripts no runner calls, and one unused test constant.
Add a guard so orphans cannot accumulate again in `testing/`. `deprecated-src.old/` is kept
as historical data (Owner decision on PR #1207).

## What Changes

- Delete the nine orphaned scripts `testing/integration/http/01-auth.sh` … `08-items.sh` and
  `test-all-endpoints.sh`.
- Remove the unused `COMPOSE_FILES` constant from `scripts/test_image_pins_and_dependabot.py`.
- Add an orphan guard to `scripts/test_testing_standalone.py`: every script under `testing/`
  must be reachable from the runner, apart from a short documented exemption list.
- Update the docs that described the removed scripts.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A tracked script under `testing/` MUST be invoked by the runner or another suite script, or be on the documented exemption list | #585; Owner instruction; Constitution P5 (remove dead code) | New |
| `deprecated-src.old/` is historical data and MUST stay in the tree | Owner decision on PR #1207 | Made explicit |
| Removing a file MUST NOT break a build, workflow, guard or live doc link | Constitution P8 | Made explicit |
| `testing/e2e-swing/` stays until the retirement spec is amended | `openspec/specs/swing-e2e-retirement` | Made explicit |

## Capabilities

### New Capabilities

- `no-orphan-files`: no unreachable QA scripts under `testing/`.

### Modified Capabilities

- (none under `openspec/specs/`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |
| `testing/` | yes | Nine scripts deleted; docs updated |
| `scripts/` | yes | New guard; unused constant removed |

### Surface area

- Entities / Endpoints / Flyway / `.env` / dependencies: none

### Architecture review

Deletion only. No ADR. The history of every removed file stays reachable in git.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/api-test/README.md` | Drop the structure and usage sections describing the removed scripts; point to `testing/` |
| `docs/300-development/303-testing/README.md` | Integration row: one suite script plus the stack smoke |
| `testing/docs/DEFINITION.md` | Remove the "legacy scripts" row; they are gone |
| `CHANGELOG.md` | `[Unreleased]` entry |
| `openspec/specs/persona-validacion-duplicados/spec.md` | Complete #799's hand-fold: restore the three scenarios of the "Database uniqueness" requirement (the spec failed strict validation without them) and the database-level sentence of its MODIFIED requirement; both copied from the archived delta; owner-approved on 2026-10-03 |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | #585 is already listed; confirm |

## Out of Scope

- `testing/e2e-swing/` (needs an amendment of `swing-e2e-retirement` and `repo-hygiene`; separate decision).
- `testing/scripts/generate-coverage-report.sh` (used by `test-coverage-report.yml`).
- `deprecated-src.old/` (422 files): kept by the Owner as historical data; #585's original request to move or delete it is declined.
- Rewriting history to purge the large files (ADR-022 covers that).
- Untracked local files (`logs/`, `.env`, caches), which are not in the repository.
