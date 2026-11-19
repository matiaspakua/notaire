# fix: page the testimony movement list

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #596 |
| Use Case | CU74 — Performance and Caching Strategy |
| Branch | `cursor/596-movement-page-47dc` |
| Gate 1 status | passed |

## Objetivo

`GET /api/v1/testimony-movements` returned every row. The UI does not call that list; it reads movements nested in testimonies. This slice pages the API list.

## What Changes

- Return a Spring page, default 20, newest id first.
- Whitelist sort fields.
- Bruno and the tests that required a JSON array now read `content`.
- Record the Maven Central 429 fallback in the factory learning log.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The list endpoint does not return the whole table | #596 | Changed |
| Unknown sort names use newest id | this change | New |
| The history panel still uses nested movements on testimonies | allowlist #1250 | Made explicit |

## Capabilities

### New Capabilities

- `testimony-movement-page`: paged GET list.

### Modified Capabilities

- None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Controller, port, adapter, OpenAPI, Bruno |
| `frontend` | yes | generated API types only |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: `GET /api/v1/testimony-movements` response changes from an array to a page
- **BREAKING** for clients that read the body as an array. The UI does not call this list.

### Write set

- Controller, port, use case, adapter, `MovementPageSort`
- OpenAPI, generated types, Bruno list, three tests
- factory learning log

### Architecture review

Same page shape as copies. No UI pagination component, because the screen does not call this endpoint.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| OpenAPI | the list response |
| factory learning log | Maven Central 429 fallback |

## Out of Scope

- Paging `GET /api/v1/testimonies`, properties, or items. Those lists are also used as full catalogs or client-side filters. #596 stays open.
