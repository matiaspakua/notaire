> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

On tip, English entity names are `SubmittedDocument` / `DocumentType`.
`fk_id_document_type` already exists (V29). Procedure FK is
`@ManyToOne(optional = true)`. Legacy `getDto()` still unboxes procedure
unconditionally. DocumentType inverse `mappedBy = "fkIdDocumentType"` points
at an Integer column field, not a relation.

## Goals / Non-Goals

**Goals:**

- Real `@ManyToOne` / `@OneToMany` pair for DocumentType ↔ SubmittedDocument.
- Null-safe legacy `getDto()` for optional procedure.
- Compatibility ID accessors so controller / Reingreso / repository callers keep
  compiling with minimal churn.
- Prove with failing-then-green unit tests (TDD).

**Non-Goals:**

- New Flyway migration.
- REST path or JSON contract changes.
- Frontend / Playwright work.
- Renaming `fkIdProcedure` property.

## Decisions

1. **Property name `documentType`** — matches `ProcedureTemplate` /
   `DocumentCostTemplate`; `mappedBy = "documentType"`.
2. **No Flyway** — join column already `fk_id_document_type`.
3. **Compatibility accessors** — `getFkIdDocumentType` / `setFkIdDocumentType`
   (int/Integer) and `getFkIdDocumentTypeNullable` delegate to
   `documentType.getIdDocumentType()` / `new DocumentType(id)`.
4. **Remove dead DtoDocumentType in getDto** — `DtoSubmittedDocument` has a
   private `fkDocumentType` field with no accessors; do not invent DTO API here.
5. **Cascade on DocumentType OneToMany** — drop `CascadeType.ALL` for submitted
   documents (deleting a type must not cascade-delete presented docs); keep
   inverse collection for navigation / in-use checks via repository query.
6. **Repository rename** — `existsByFkIdDocumentType` →
   `existsByDocumentTypeIdDocumentType` (Spring Data path through association).

## Riesgos / Trade-offs

- [Compatibility setter creates detached DocumentType by id] → Same pattern as
  other legacy setters; controller already loads type separately for due dates.
- [Lazy DocumentType on getDto if we later populate DTO] → Not populating DTO
  field; toResponse still uses repository findById via nullable id accessor.
- [existsBy rename breaks mocks] → Update `SimpleControllersTest` stubs.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| DocumentType association is mapped on SubmittedDocument | unit | `SubmittedDocumentEntityTest` |
| DocumentType inverse mappedBy matches owning property | unit | `SubmittedDocumentEntityTest` (annotation reflection) |
| getDto with null procedure does not NPE | unit | `SubmittedDocumentEntityTest` |
| getDto with procedure present includes procedure DTO | unit | `SubmittedDocumentEntityTest` |

- New unit tests: extend `SubmittedDocumentEntityTest` (CU72 coverage)
- New integration tests: none required for this mapping fix (H2 bootstrap already
  covered by existing SubmittedDocumentControllerTest / DocumentType in-use)
- Coverage impact: small gain on getDto branches; ratchet floor held

## Regression Strategy

- Existing tests affected: `EntitiesBasicTest` (if it touches SubmittedDocument
  type id), `SimpleControllersTest` (existsBy mock name),
  `SubmittedDocumentControllerTest`, Reingreso tests (accessors).
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: n/a for this change (no contract change); skip or note
- Legacy paths at risk: `BusinessController` getDto call sites — null-guard fixes
  them; `jpa` package unused for this field

## Playwright Strategy

- n/a — no UI surface (JPA entity / legacy DTO only; CU72 UI unchanged)

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: code-only deploy
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): create documento-presentado without
  procedureId; confirm 201; tipo-de-documento in-use still true when linked

## Rollback Strategy

- Revert safe: yes — code-only; column unchanged
- Database rollback: none needed
- Data written under the new behavior after revert: same FK ints
- Blast radius if rollback is delayed: low (mapping-only)

## Migration Plan

1. OpenSpec Gate 1 + failing unit tests
2. Entity + DocumentType mappedBy + repository method + call-site mocks
3. Green suite + CU72 / CHANGELOG
4. Draft PR; coordinator merges after heavy CI
