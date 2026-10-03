<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## ADDED Requirements

### Requirement: SubmittedDocument DocumentType is a JPA association
The system SHALL map `SubmittedDocument` to `DocumentType` with a `@ManyToOne`
association on property `documentType` using join column `fk_id_document_type`.
`DocumentType.submittedDocumentCollection` MUST declare
`mappedBy = "documentType"` so the inverse collection is a valid Hibernate
relation. Per CU72 / #801.

#### Scenario: DocumentType association is mapped on SubmittedDocument
- **WHEN** a `SubmittedDocument` is assigned a `DocumentType` via
  `setDocumentType` (or a compatibility ID setter that delegates to the
  association)
- **THEN** `getDocumentType()` returns that type and compatibility ID accessors
  expose the type's id

#### Scenario: DocumentType inverse mappedBy matches owning property
- **WHEN** the `DocumentType` entity OneToMany for submitted documents is
  inspected
- **THEN** its `mappedBy` value equals `documentType`

### Requirement: Legacy getDto null-guards optional procedure
The system SHALL convert a `SubmittedDocument` to `DtoSubmittedDocument` via
`getDto()` without throwing when `fkIdProcedure` is null (procedure is optional
since V6). When procedure is present, the DTO MUST include the procedure DTO;
when absent, the procedure field MUST be null. Dead unused
`DtoDocumentType` construction MUST NOT remain. Per CU72 / #801.

#### Scenario: getDto with null procedure does not NPE
- **WHEN** `getDto()` is called on a `SubmittedDocument` whose procedure
  association is null
- **THEN** a `DtoSubmittedDocument` is returned and its procedure field is null
  (no `NullPointerException`)

#### Scenario: getDto with procedure present includes procedure DTO
- **WHEN** `getDto()` is called on a `SubmittedDocument` with a non-null
  procedure that has the minimum fields required for `Procedure.getDto()`
- **THEN** the returned DTO's procedure field is non-null
