# Fix SubmittedDocument DocumentType mapping and getDto null-guard

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #801 |
| Use Case | CU72 – Gestión de Documentos Presentados |
| Branch | `cursor/fix-801-submitted-document-mapping-69d3` |
| Gate 1 status | passed |

## Objetivo

`SubmittedDocument.fkIdDocumentType` is a plain `@Column Integer` while
`DocumentType.submittedDocumentCollection` declares `@OneToMany(mappedBy =
"fkIdDocumentType")` against a non-relation field — a broken inverse that
Hibernate cannot wire as a true association. Separately, legacy
`SubmittedDocument.getDto()` calls `fkIdProcedure.getDto()` with no null guard
even though the procedure FK is optional (V6), so null-procedure documents NPE
on any legacy DTO path. Fix both integrity defects under CU72.

## What Changes

- Replace `SubmittedDocument` Integer column field with `@ManyToOne DocumentType`
  - `@JoinColumn(name = "fk_id_document_type")` (property `documentType`).
- Fix `DocumentType` `mappedBy` to `documentType`; soften Cascade ALL on that
  OneToMany if too aggressive for submitted documents.
- Keep compatibility accessors `getFkIdDocumentType` / setters that delegate to
  the association for existing call sites.
- Null-guard optional procedure in `getDto()` (mirror Procedure optional-relation
  style); remove dead unused `DtoDocumentType` construction or populate from the
  association when the DTO contract requires it.
- Unit regression tests (TDD): null-procedure `getDto` does not NPE; DocumentType
  association mapping assertions.
- Update CU72 permanent docs + `CHANGELOG.md`.
- **No Flyway** — column `fk_id_document_type` already exists (V29).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Submitted document MAY omit a procedure FK; DTO conversion MUST NOT NPE | CU72 / V6 optional procedure | Made explicit |
| Submitted document DocumentType association MUST be a real JPA relation (ManyToOne / OneToMany inverse) | CU72 data integrity (#801) | New (technical integrity) |
| Document type referential checks (in-use) MUST still see submitted documents linked by FK | CU72 / DocumentTypeController | Made explicit |

## Capabilities

### New Capabilities

- `submitted-document-relation-integrity`: JPA integrity for
  `SubmittedDocument`↔`DocumentType` and null-safe legacy `getDto()` when
  procedure is absent.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Entity mapping, repository derived query rename, controller may use association; unit tests |
| `frontend` | no | — |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | maybe | Only if `DtoSubmittedDocument` needs DocumentType accessors to populate association in getDto |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `SubmittedDocument`, `DocumentType`
- Endpoints: no path/contract change; `/api/v1/documento-presentado` and
  tipo-de-documento in-use checks keep working via compatibility accessors /
  updated Spring Data property path
- Database (Flyway `V{n}`): none (reuse `fk_id_document_type`)
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** no — REST JSON unchanged; Java call sites keep ID accessors

### Architecture review

Follows existing JPA relation patterns (`ProcedureTemplate.documentType`,
`SubmittedDocument.fkIdProcedure`). No ADR — mapping correction, not a new
architectural choice. Prefer `repository` over legacy `jpa`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU72 – Gestión de Documentos Presentados.md` | Note optional procedure + DocumentType association integrity / #801 |
| `CHANGELOG.md` | Fixed entry under `[Unreleased]` for #801 |

## Out of Scope

- Flyway schema changes or data backfill
- Renaming Spanish REST path `/api/v1/documento-presentado`
- Frontend UI changes or Playwright new specs
- Broader rename of `fkIdProcedure` property on `SubmittedDocument`
- Cascade redesign of other DocumentType OneToMany collections
