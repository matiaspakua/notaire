# openapi-generated-types

Frontend TypeScript API types are generated from the committed OpenAPI document and guarded against drift.

## ADDED Requirements

### Requirement: One-command type generation

The frontend MUST provide an npm script that regenerates `src/types/api.generated.ts`
from `backend-api/openapi/openapi.yaml`.

#### Scenario: Regenerate types

- **WHEN** `npm run openapi:types` is run in `frontend/`
- **THEN** `src/types/api.generated.ts` is written or updated and exits 0

### Requirement: CI fails on stale generated types

Frontend CI MUST fail when the committed generated types file does not match a fresh
regeneration from the committed OpenAPI artifact.

#### Scenario: Drift detected

- **WHEN** `api.generated.ts` is out of date relative to `openapi.yaml`
- **THEN** the Frontend CI drift check fails

### Requirement: High-traffic hooks use generated types

Dashboard, gestiones, documentos, and presupuestos hooks MUST import API response/request
types from the generated module (directly or via a thin re-export), so a renamed backend
field fails `tsc`.

#### Scenario: Renamed field breaks typecheck

- **WHEN** a schema field used by a migrated hook is renamed in OpenAPI and types are regenerated
- **THEN** `npm run typecheck` fails until call sites are updated
