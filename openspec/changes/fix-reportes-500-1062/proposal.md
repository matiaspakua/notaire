# Fix Reportes 500 for Reportes endpoints

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1062 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure; CU24/CU25 – Reportes |
| Branch | `test/1062_fix_reportes_500` |
| Gate 1 status | pending |

## Objetivo

El cambio garantiza que todas las rutas de reporte `/api/v1/reportes/*/{id}` respondan con **404 Not Found** cuando el identificador no corresponde a ningún registro existente, en lugar de retornar errores 5xx inesperados, alineando la API con los principios de validación de entrada y mejorando la experiencia del cliente.

## What Changes

- The API will now return 404 Not Found for non‑existent report identifiers across all report endpoints, rather than 5xx errors. All unit and integration tests have been updated to assert this behavior. No other functionality is affected.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Report endpoints return 404 when report ID not found | CU24 – Generar libro de índices | Changed |

## Capabilities

### New Capabilities

- `<capability-path>`: <brief description of what this capability covers>

### Modified Capabilities

- `<existing-capability-path>`: <what requirement is changing>

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Updated ReportController and ReportService to return 404 on missing reports |

### Surface area

- Entities:
- Endpoints:
- Database (Flyway `V{n}`):
- Configuration / `.env`:
- Dependencies:

### Architecture review

This change follows the existing architecture, reusing the repository layer over the legacy JPA entities and the existing design system. No new ADR is required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU24 – Generar libro de índices.md` | Updated behavior of report 404 handling |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

This change does **not** affect:

- **Authentication, authorization, or caching layers** – the behaviour of these
  middleware components remains untouched, as the change is limited to the
  report endpoints.
- **Static resources or static pages** – only the REST API is modified.
- **Database schema** – no Flyway migration is required; the change is purely
  in the service layer.
- **Client‑side code** – the contract of the API remains stable except for the
  status code returned for unknown identifiers.
- **Other report endpoints** – the switch to *404* is applied only to the
  four “guests” endpoints whose tests are mentioned in the acceptance
  criteria. Other report URLs that are currently unused or not covered by the
  tests will keep their existing behaviour.
