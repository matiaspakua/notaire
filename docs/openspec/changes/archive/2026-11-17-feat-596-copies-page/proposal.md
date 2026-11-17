# feat: page the copies list

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #596 |
| Use Case | CU70 — Copies |
| Branch | `cursor/596-copies-page-47dc` |
| Gate 1 status | passed |

## Objetivo

`GET /api/v1/copies` returned every copy. The copies screen loaded that array. This slice returns one page (default 20, newest id first) and the screen reads that page.

## What Changes

- `GET /api/v1/copies` returns a Spring page with `page`, `size` and `sort`.
- The copies screen asks for `idCopy,desc` and shows the shared pager.
- OpenAPI, generated types and the Bruno list check match the page.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The copy register is one page, default 20, newest id first | CU70; #596 | Changed |

## Capabilities

### New Capabilities

- `copy-list-page`: `GET /api/v1/copies` returns `content` and `totalElements`.

### Modified Capabilities

- The unpaged array response is replaced for this one endpoint.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Query port, adapter, controller, OpenAPI, Bruno |
| `frontend` | yes | Copies screen and HTTP gateway |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Endpoints: `GET /api/v1/copies` response is a page
- Database: none
- Clients that read a bare array must read `content`.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/copy/CopyController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/copy/**`
- `backend-api/src/main/java/com/licensis/notaire/application/**/copy/**`
- `backend-api/openapi/openapi.yaml`
- `backend-api/api-test/copies/02-list.yml`
- `frontend/src/app/dashboard/copies/page.tsx`
- `frontend/src/hooks/useCopies.ts`
- `frontend/src/adapters/http/copy/copy-http-gateway.ts`
- `frontend/src/core/copy/application/**`
- `frontend/src/types/api.generated.ts`
- `docs/openspec/changes/feat-596-copies-page/**`

### Architecture review

Follows the payments page port (ADR-030). No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/openapi/openapi.yaml` | page, size, sort and `PageCopyResponse` |
| `CHANGELOG.md` | n/a |

## Out of Scope

- Other growing lists. #596 stays open.
- #580.
