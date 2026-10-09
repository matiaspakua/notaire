# Accepted OpenAPI breaking changes: one file per pull request (#1315, slice 2)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1315 |
| Use Case | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas |
| Branch | `fix/1315_accepted_breaking_changes_dir` (stacked on #1382) |
| Gate 1 status | draft |

## Objetivo

Slice 1 (#1330) kept `backend-api/openapi/accepted-breaking-changes.txt` empty on main and failed a pull request carrying an entry whose break was already merged. Every pull request that accepts a break therefore edits the same lines, and main empties them after each merge: every merge conflicts with every other open pull request. On 2026-10-09/10, #1335, #1342, #1370, #1371, #1372, #1373 and #1374 each needed two conflict-only merges from main, all in this one file. The gate must stay as strict while accepting a break stops touching shared lines.

## What Changes

- `backend-api/openapi/accepted-breaking-changes.txt` is replaced by the directory `backend-api/openapi/accepted-breaking-changes.d/`: each pull request adds its own `<issue>-<slug>.txt`; a README documents the rule.
- `workspace/sdlc/check-accepted-breaking-changes.py` assembles and checks the directory:
  - a file unchanged on the base ref belongs to a merged pull request: never ignored (its break is in the base spec; it could only hide a later break with the same text), listed for deletion, deleted with `--prune`;
  - every other file belongs to the pull request: each entry must match a current breaking change (oasdiff err-ignore rule), sit under a `# #<issue>` comment, and the file name must start with the issue number;
  - `--write-ignore OUT` writes only the pull request's entries, the list `oasdiff breaking --err-ignore` reads;
  - the legacy single list fails with a migration hint.
- `openapi-contract.yml`: step "Accepted breaking changes: assemble and check" before the diff, which ignores `.openapi-ci/accepted-breaking-changes.txt`.
- `preflight.sh`: same assembly and diff; `--fix` prunes merged files.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| One accepted-breaking file per pull request, named `<issue>-<slug>.txt` | #1315 | New |
| A file unchanged on the base is merged and never ignored | #1315 | New (replaces "the next PR must remove it") |
| The effective accepted list on main is empty; entries are justified (issue comment, CHANGELOG, Owner approval) | #1315 slice 1 | Unchanged |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `openapi-breaking-change-gate`: accepted breaks are listed per pull request, and accepting one does not conflict with other open pull requests.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `workspace` | yes | checker, preflight, guard tests |
| `backend-api` | yes | `openapi/accepted-breaking-changes.d/` replaces the single list (`openapi.yaml` unchanged) |
| `.github` | yes | `openapi-contract.yml` steps |
| `docs` | yes | devsecops README, TEST-PLAN |

### Surface area

- Endpoints: none
- CI: `openapi-contract.yml` (assemble step before the diff; the separate stale step is folded into it)
- Entities / Flyway / Configuration: none

### Architecture review

Same stdlib script and oasdiff JSON rule as slice 1 (ADR-026, `workspace/sdlc`). Ownership is decided with `git show <base-ref>:<file>`, so CI needs the base ref, which the job already fetches. Stacked on #1382 because that PR rewrites the same workflow steps (oasdiff binary instead of the Docker action).

### Follow-up

Open pull requests that still edit the single list (#1342, #1373, #1374, and the UI PRs if they accept breaks) get a modify/delete conflict on it when they next merge main after this lands: move their entries to `accepted-breaking-changes.d/<issue>-<slug>.txt` and delete the old file in that merge. No other migration is needed.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
| `docs/200-architecture/208-devsecops/README.md` | `openapi-contract.yml` row and OpenAPI contract row |
| `docs/300-development/303-testing/TEST-PLAN.md` | API contract row |
| `backend-api/openapi/accepted-breaking-changes.d/README.md` | new |
