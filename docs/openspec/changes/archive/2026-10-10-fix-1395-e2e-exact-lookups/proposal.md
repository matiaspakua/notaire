# E2E rows and options found by exact text, not substring (#1395)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1395 |
| Use Case | CU08 – Verificar testimonio; CU11 – Ingresar para inscripción; CU39 – Cargar ítems desde la plantilla; CU81 – Protocolo auxiliar |
| Branch | `fix/e2e_exact_row_lookup` |
| Gate 1 status | draft |

## Objetivo

TS-0012 and TS-0081 found rows with a number regex that also matched other rows' numbers once the dev DB grew (strict-mode violations, a different test failing each run); presupuesto-plantilla picked the last "Tipo Tramite E2E" option, often another parallel test's type. Test-only change.

## What Changes

- New `testing/e2e/tests/setup/rows.ts`: `rowWithCell(page, text)` returns the row with a cell whose whole text equals `text`.
- TS-0012 (7 lookups) and TS-0081 (2 lookups) use `rowWithCell`.
- presupuesto-plantilla creates its procedure type with a unique name and selects that exact option.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An E2E lookup matches its own record, never a substring of another | #1395 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `e2e-test-isolation`: E2E specs find their own records independently of other data in the database.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `testing` | yes | Playwright helpers and three specs |
| `frontend` | no | no production change |
| `backend-api` | no | no change |

### Surface area

- Playwright specs only

### Architecture review

No architecture change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `none` | test-only change |
