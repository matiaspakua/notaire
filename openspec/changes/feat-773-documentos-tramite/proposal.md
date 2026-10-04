# Make the Documentos screen work against the API and link documents to a trámite and its gestión (slice of #773)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #773 |
| Use Case | CU04 – Registrar documentación cliente (#157); CU72 – Gestión de Documentos Presentados (#163); CU03 – Lista documentos y certificados necesarios (#156) |
| Branch | `feat/773_required_docs` |
| Gate 1 status | draft |

## Objetivo

The Documentos screen sends tipoId, fecha and entregado while the API reads typeId, date and delivered, and it reads fkDocumentType and dateEntry while the API returns type and date, so documents are created without type or date and listed without them. Fix that contract, let a document be linked to a trámite, and show the documents of a gestión in its case summary.

## What Changes

- The Documentos screen and its hooks send `typeId`, `date`, `delivered` and `procedureId` and read `type`, `date`, `delivered` and `procedureId`, matching `SubmittedDocumentController`.
- The create form gets optional Gestión and Trámite selectors (the trámites of the chosen gestión come from the existing CU43 endpoint); the list shows the linked trámite.
- `SubmittedDocumentResponse` also returns `procedureId`.
- `GET /gestiones/{id}/resumen-caso` adds `documents`: name, type, trámite, prepared, released, observed, delivered, reentered and due date of every document of the gestión's trámites; the case summary dialog lists them.
- #773 stays open: listing the required documents automatically at the moment a trámite is confirmed is not part of this slice (CU43 and the Documentos necesarios screen list them on demand).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A submitted document linked to a trámite appears in the case summary of that trámite's gestión with its status flags | CU04, CU72, #773 | New |
| The screen and the API share one field contract | #773 | Made explicit |

## Capabilities

### New Capabilities

- `document-procedure-link`: Register a client's document against a trámite and see it from the gestión.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | response field, repository query, case summary |
| `frontend` | yes | contract fix, selectors, dialog list |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | notaire-shared DTO, OpenAPI artifact, E2E spec, CU04 doc |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Contract fix plus a read model extension; no schema change and no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU04 – Registrar documentación cliente.md` | implementation note |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `CHANGELOG.md` | one entry |
