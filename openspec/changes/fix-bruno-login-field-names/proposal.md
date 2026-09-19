# Fix Bruno API job login field names in CI

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1008 |
| Use Case | None — CI/CD defect, not a business behavior change (precedent: #566) |
| Branch | `ci/1008_fix-bruno-login-field-names` |
| Gate 1 status | passed |

## Objetivo

The "API Tests (Bruno)" job in `playwright-e2e.yml` fails on every run because
its login step still POSTs `{"nombre": "admin", "contrasenia": "admin"}` to
`/api/v1/usuarios/login`, which has expected `{"name": ..., "password": ...}`
since the #977 mass-rename. The job has been silently red since #977 merged
(confirmed live on `main` after #1007, run 35445844763) while the sibling
"UI E2E Tests (Playwright)" job in the same workflow passes — nobody
downstream of #977 touched this workflow file because the rename only swept
`.java`/`.ts` sources.

## What Changes

- `.github/workflows/playwright-e2e.yml`: the Bruno login curl payload changes
  from `{"nombre":"admin","contrasenia":"admin"}` to
  `{"name":"admin","password":"admin"}`.

No other behavior changes.

## Reglas de negocio

None. This is a CI configuration defect fix; it introduces, alters, and makes
explicit no business rule.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
None — no spec-level (application) behavior changes; `skip_specs: true` is
set in `.openspec.yaml`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `playwright-e2e.yml`'s Bruno-job login payload field names corrected |

### Surface area

- Entities: none
- Endpoints: none added/changed; corrects a caller of the existing
  `POST /api/v1/usuarios/login` (unchanged since #977)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- Breaking for API clients: no — the endpoint itself is untouched; only a CI
  script's request body is corrected to match it

### Architecture review

Not architectural. No ADR required. Follows existing convention: the fix
mirrors how the same login call is already made correctly elsewhere in this
same workflow file's Playwright global-setup and in `scripts/preflight.sh`'s
Bruno-token step (`{"name": "admin", "password": "admin"}`).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | n/a — not user-visible; internal CI defect, not a shipped feature or fix visible to end users |

No other permanent documentation references this workflow step's payload.

## Out of Scope

- Any other stale Spanish-field-name occurrence outside this one workflow step
  (Acceptance Criteria requires a repo-wide grep to confirm none exist; if any
  are found, they are out of scope for this change and get their own Issue).
- The broader E2E/Bruno test-suite content itself (already fixed under
  #1006/#1007) — this change only fixes CI's ability to *run* that suite.
