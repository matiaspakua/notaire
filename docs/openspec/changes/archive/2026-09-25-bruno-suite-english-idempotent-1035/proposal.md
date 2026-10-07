# Test: translate the Bruno API suite to English and make it idempotent

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1035 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `test/1035_bruno_suite_english_idempotent` |
| Gate 1 status | passed |

## Objetivo

The backend has been translated to English (classes, tables, JSON properties), but
the Bruno suite in `backend-api/api-test/` still uses Spanish folder, file, request,
test and variable names, Spanish descriptions and Spanish test data, and its only
environment is misspelled (`Developmen`). The suite is also not idempotent: on
2026-09-24 a run against an already-used database failed 21/152 requests because
fixtures used hard-coded unique values and read a stale 409 field. This change makes
the suite English, self-cleaning and repeatable, and traces every request to its
Use Case and Requirements.

## What Changes

- Rename every resource folder and request file to English, following the backend's
  English domain names (`conceptos` → `concepts`, `pagos` → `payments`,
  `plantilla-presupuesto` → `budget-templates`, ...). URL paths and JSON keys are
  **not** changed where the API still exposes Spanish ones (`/api/v1/pagos`,
  `encabezado`, `valido`) — the suite tests the real contract.
- Translate request names, test names, descriptions and free-text test data to English;
  rename runtime variables to namespaced English snake_case (`concepto_id` →
  `concept_id`, the shared `_id` → one variable per resource).
- Rename environment `Developmen` → `Development` and update every caller
  (CI workflow, `scripts/preflight.sh`, docs).
- Fix stale / misspelled fields: `idPersonaExistente` → `existingPersonId`
  (409 body), `diasVencimiento` → `dueDays` (document type), budget search
  `?q=` → `?status=` (the endpoint's real parameter; the old query was ignored).
- Idempotency: every created row uses a per-run unique value (timestamp-derived
  document numbers, usernames, names, folio/management numbers) and every fixture
  the suite creates is deleted by the suite. No seeded row is mutated or deleted
  (the budget-template lifecycle previously created/deleted the seeded (1,1) row;
  it now builds its own procedure type and concept).
- Every request description ends with a `Traceability:` line citing its Use Case(s)
  and Requirement(s) (RF numbers from the Requirement ↔ Use Case matrix).

## Reglas de negocio

No business rule changes. The suite asserts existing rules only, cited from the
Use Cases (CU01, CU11, CU13, CU15, CU17, CU20–CU23, CU26–CU41, CU45–CU49, CU54–CU73,
CU78, CU79) and `docs/100-business/104-traceability/`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — test-suite and documentation change only; no application behavior changes.
`skip_specs: true` is set in `.openspec.yaml`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | tests only | `api-test/` Bruno collection rewritten; no `src/` change |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | Bruno step uses `--env Development` |

### Surface area

- Entities: none
- Endpoints: none (tests only)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

No architectural change. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/api-test/README.md` | English layout, environment name, idempotency + traceability conventions |
| `backend-api/api-test/COVERAGE.md` | Resource table with new folder names, current request/test counts |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | `Bruno_Test` paths renamed; newly covered rows marked |
| `docs/300-development/303-testing/README.md`, `test-coverage/TEST-COVERAGE-STRATEGY.md` | `--env Development` |
| `.github/workflows/playwright-e2e.yml`, `scripts/preflight.sh` | `--env Development` |
| `CHANGELOG.md` | Entry under Unreleased |

## Out of Scope

- Translating REST URL paths or JSON keys that the backend still exposes in Spanish —
  that is a backend API contract change, tracked under the backend translation work.
- Adding coverage for the 16 controllers without Bruno tests (#953).
- Deleting rows leaked into existing databases by earlier non-idempotent runs.
