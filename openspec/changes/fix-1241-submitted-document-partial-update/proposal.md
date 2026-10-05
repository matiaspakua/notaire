# Updating a submitted document keeps what the request does not carry

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1241 |
| Use Case | CU72 – Gestión de Documentos Presentados |
| Branch | `fix/1241_documento_presentado_partial_update` |
| Gate 1 status | draft |

## Objetivo

`PUT /api/v1/documento-presentado/{id}` builds a new entity from the request and saves it under the stored id, so every field the request omits is erased: the name becomes empty, the prepared, released, flagged and reentered flags reset, the trámite link, the dates, the amount, the card number and the notes are lost. Editing a document on the Documentos screen therefore wipes its progress. Apply the request onto the stored document instead.

## What Changes

- `update` loads the stored `SubmittedDocument` and changes only the fields present in the request: type, delivered flag, name, delivered-by, date and trámite link.
- When the type or the entry date changes, the type-derived fields (expires, due days, due date) are recomputed as on create.
- `create` keeps its defaults (empty name, delivered false, flags false) through the same request-applying method, so create and update share one code path.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An update changes only the fields it carries | CU72, #1241 | Made explicit |

## Capabilities

### New Capabilities

- `submitted-document-update`: Partial update of a submitted document.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `SubmittedDocumentController` update and request mapping |
| `frontend` | no | the Documentos screen already resends `procedureId` and keeps working with a partial body |
| Docs / scripts / CI | yes | CHANGELOG |

### Surface area

- Endpoints: `PUT /api/v1/documento-presentado/{id}` keeps its path, body and status codes; an omitted field no longer clears the stored value
- Entities / Flyway / Configuration / Dependencies: none
- BREAKING: a client that relied on omitting a field to clear it can no longer do so; none exists in the repository

### Architecture review

No architectural change; the controller still owns the mapping, which is later slices' concern (#577).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one entry |
