# A submitted document is created with its type and its procedure (slice of #655)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #655 |
| Use Case | CU04 – Registrar documentación cliente; CU72 – Gestionar documentos presentados |
| Branch | `fix/655_submitted_document_required_fields` |
| Gate 1 status | draft |

## Objetivo

`POST /api/v1/documento-presentado` accepted a body without `typeId` or `procedureId` (even `{}`) and stored an empty, orphan document. The Owner decided the type is required (#655) and asked whether the procedure is really needed from the user's point of view. Analysis: it is. CU04 and CU72 define a submitted document as one presented by the client for a gestión, through its trámite, and every reader of documents goes through that link. Make both required on create, in the API, the contract and the UI, without breaking documents already stored.

## What Changes

- `SubmittedDocumentController`: POST takes `SubmittedDocumentCreateRequest` with `@NotNull typeId` and `@NotNull procedureId` (`@Valid`, 400 naming the field; `requiredMode = REQUIRED` in the contract). PUT keeps the all-optional `SubmittedDocumentRequest`. The `fk_id_tramite` column stays nullable (no migration).
- Documentos page: type, gestión and trámite are marked required and Save stays disabled until type and trámite are chosen. The gestión/trámite selectors also appear when editing a document that has no trámite yet, so legacy documents can be linked. The rules live in `lib/documento-presentado-form.ts`; i18n helper texts say the link is required.
- `openapi.yaml` regenerated; `accepted-breaking-changes.txt`: two `request property became required` entries, justified.
- Tests: validation integration tests (missing type, missing procedure, empty body, contract), existing tests send both fields; frontend unit test; Bruno setup/teardown trámite and two reject requests; Playwright TS-0097, TS-0090 and the TS-0033 helper send a trámite and a type.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A submitted document is created with its document type | #655 (Owner decision, Run 7) | New |
| A submitted document is created for a trámite of a gestión | CU04, CU72, #655 analysis | New (create only) |
| Documents stored before the rule stay readable and editable | #655 | Preserved (nullable column, partial PUT) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `submitted-documents`: Registering and maintaining the documents clients submit for a gestión's trámites.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `SubmittedDocumentController`, OpenAPI, tests, Bruno |
| `frontend` | yes | Documentos page, form helper, types, i18n |
| `testing` | yes | Playwright TS-0097, TS-0090, api-helpers |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `POST /api/v1/documento-presentado` (typeId and procedureId required; breaking, accepted); `PUT` unchanged
- Entities / Flyway: none (column stays nullable)
- UI: Documentos create/edit dialog

### Architecture review

Same pattern as `PaymentController` (#655 CU15): a create record with bean-validation constraints next to a partial update record. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | BREAKING Changed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.txt` | two entries |
