<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

When a trámite is confirmed on a gestión via complete-case, materialize the CU03
required-document checklist from `ProcedureTemplate` so the case summary surfaces
pending documents without visiting on-demand CU03/CU43 screens. Source: #773
remaining AC after #1240; CU02 / CU03 / CU04 / CU72.

## ADDED Requirements

### Requirement: Confirming a trámite seeds its required documents

When complete-case creates a new procedure on a gestión, the system MUST create
one pending `SubmittedDocument` for each document type in that procedure type's
`ProcedureTemplate`, linked to the new procedure, with `delivered=false` and
`reentered=false`, copying name/expiry/due/deliveredBy from the document type.

#### Scenario: Complete-case seeds pending required documents from procedure template

- **WHEN** a client confirms a gestión via `POST /api/v1/managements/complete-case`
  with a procedure type that has procedure-template document types
- **THEN** each template document type has a corresponding pending submitted
  document linked to the new procedure

#### Scenario: Empty template yields no seeded documents

- **WHEN** complete-case creates a procedure whose type has no procedure-template rows
- **THEN** no submitted documents are created for that procedure

### Requirement: Case summary surfaces the checklist after confirm

After confirm, the case summary MUST list the seeded pending documents so the
gestión surface shows required documentation status without a separate CU03 call.

#### Scenario: Case summary lists pending required documents after confirm

- **WHEN** complete-case has seeded pending documents for the gestión's trámite
- **THEN** `GET /api/v1/managements/{id}/case-summary` includes those documents
  with their type, procedure, and undelivered status flags

### Requirement: Seeding is idempotent and safe for updates

The system MUST NOT duplicate document types already present for a procedure, and
MUST NOT remove or replace client-registered documents when complete-case updates
an existing gestión that already has procedures.

#### Scenario: Idempotent seed skips existing document types for the procedure

- **WHEN** seed runs again for a procedure that already has a submitted document
  for a given document type
- **THEN** no additional row is created for that document type

#### Scenario: PUT complete-case on existing procedure does not wipe registered docs

- **WHEN** `PUT /api/v1/managements/{id}/complete-case` updates a gestión that
  already has a procedure with registered or pending documents
- **THEN** those documents remain; seed does not delete them
