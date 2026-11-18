# feat: page the submitted-document list

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #596 |
| Use Case | CU72 — Submitted documents |
| Branch | `cursor/596-submitted-documents-page-47dc` |
| Gate 1 status | passed |

## Objetivo

`GET /api/v1/submitted-documents` returned every document. The documents screen loaded that array. This slice returns one page (default 20, newest id first) and the screen reads that page.

## What Changes

- The list endpoint returns a Spring page with `page`, `size` and `sort`.
- The documents screen asks for `idSubmittedDocument,desc` and shows the pager.
- OpenAPI, generated types and the Bruno list check match the page.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The submitted-document register is one page, default 20, newest id first | CU72; #596 | Changed |

## Capabilities

### New Capabilities

- `submitted-document-list-page`: the list returns `content` and `totalElements`.

### Modified Capabilities

- The unpaged array response is replaced for this one endpoint.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Query port, adapter, controller, OpenAPI, Bruno |
| `frontend` | yes | Documents screen and HTTP gateway |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Endpoints: `GET /api/v1/submitted-documents` is a page
- Database: none
- Clients that read a bare array must read `content`.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/document/SubmittedDocumentController.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/document/SubmittedDocumentPersistenceAdapter.java`
- `backend-api/src/main/java/com/licensis/notaire/application/**/document/**`
- `backend-api/openapi/openapi.yaml`
- `backend-api/api-test/submitted-documents/02-list.yml`
- `frontend/src/app/dashboard/documents/page.tsx`
- `frontend/src/hooks/useSubmittedDocuments.ts`
- `frontend/src/types/api.generated.ts`
- `docs/openspec/changes/feat-596-submitted-documents-page/**`

### Architecture review

Follows the copies page port (ADR-030). No new ADR. Located with repolens search before the first edit.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/openapi/openapi.yaml` | page, size, sort and `PageSubmittedDocumentResponse` |
| `CHANGELOG.md` | n/a |

## Out of Scope

- Other growing lists. #596 stays open.
- #580.
