# Translate the remaining codebase surface to English

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1490 |
| Use Case | CU75 — Database Management and Migrations |
| Branch | `cursor/english-codebase-01e0` |
| Gate 1 status | passed |

## Objetivo

The domain model is already English (epic #973), but the public HTTP paths, dashboard routes, leftover columns, the `identificaciones` table, and a set of type names, comments, and developer-facing messages are still Spanish. Operators and contributors cannot read one language across the backend, frontend, database, and tests.

## What Changes

- **BREAKING** — Spanish `/api/v1` path segments become English. In-repo clients (frontend, Bruno, Playwright, the CU-API matrix) move in the same change.
- **BREAKING** — Dashboard route folders become English (`/dashboard/personas` → `/dashboard/people`, and the same for the other Spanish segments).
- New Flyway `V45` renames the remaining Spanish tables and columns. Applied migrations V1–V44 stay untouched.
- Spanish type names (`AdministradorJpa`, `CarpetasEnWaitException`, `DtoValido`, `IPersistenciaJpa`, and the Spanish DTO records) become English.
- Developer-facing OpenAPI descriptions and exception messages that are still Spanish become English where this change touches them.
- The `es` and `en` message catalogs stay. They are the product localization, not untranslated source.
- Persisted business vocabulary (status names such as `Iniciada`, seed catalog labels, person names) stays. It is stored domain data.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Schema changes ship only as a new Flyway version; applied scripts stay immutable | CU75 | Made explicit |
| A renamed column keeps its data, constraints, and indexes | CU75 | Made explicit |
| Public HTTP and dashboard paths are English | #1490, continuation of #973 | New |
| The Spanish UI catalog remains available through the language switcher | CU76 / ADR-015 (i18n) | Made explicit |

## Capabilities

### New Capabilities

- `english-source-surface`: English HTTP paths, dashboard routes, and remaining schema identifiers, with the localization catalogs and stored domain vocabulary left in place.

### Modified Capabilities

- None. No existing OpenSpec requirement changes its behavior beyond the path and schema names this new capability owns.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller paths, security matchers, entity column names, Flyway V45, OpenAPI, Bruno |
| `frontend` | yes | Dashboard route folders, API URLs, hook identifiers that embed Spanish path names |
| `frontend-swing` | no | Retired |
| `infra` / observability | no | No dashboard or exporter query uses the renamed columns |
| CI/CD (`.github/workflows`) | no | Existing gates cover the break via `accepted-breaking-changes.d` |

### Surface area

- Entities: `Identification` (`identificaciones`), `Person.fecha_nacimiento` and `fk_id_tipo_identificacion`, workflow `nombre`/`descripcion`/`activo`/`tipo`/`posicion_*`/`condicion`, and the Spanish foreign keys still mapped on procedures, folders, payments, items, copies, testimony movements, registration drafts, and audit records.
- Endpoints: every `/api/v1` resource whose segment is still Spanish (users, managements, procedures, budgets, deeds, payments, reports, and the rest of the map in design.md).
- Database (Flyway `V45__rename_remaining_spanish_identifiers.sql`): renames only. No data rewrite.
- Configuration / `.env`: none.
- Dependencies: none.

### Architecture review

Follows the existing architecture: Flyway is the schema source of truth, controllers stay in `adapter.in.web`, and the frontend keeps talking to the API over HTTP. No new ADR. The HTTP rename is a breaking contract change recorded in `CHANGELOG.md` and `backend-api/openapi/accepted-breaking-changes.d/1490-english-http-surface.txt`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | Endpoint column uses the English paths |
| `CHANGELOG.md` | `[Unreleased]` breaking entry for the HTTP and route rename |
| `backend-api/openapi/openapi.yaml` | Regenerated or path-updated to the English surface |
| `backend-api/api-test/COVERAGE.md` | Resource paths listed in English |

## Out of Scope

- Translating `docs/100-business` use-case prose. Those documents stay the Spanish business source of truth.
- Translating `deprecated/` and `docs/000-archive/`.
- Translating `es.json` values into English. That catalog is the Spanish locale.
- Rewriting stored status and catalog labels (`Iniciada`, `Compraventa`, `En trámite`).
- Editing applied Flyway scripts, including the copies under `src/test/resources/db/migration` and `init-db`.
