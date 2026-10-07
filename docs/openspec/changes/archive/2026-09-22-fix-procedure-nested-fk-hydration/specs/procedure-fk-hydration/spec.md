<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## Purpose

Guarantees that creating or updating a `Procedure` (trámite) via the REST
API always persists and returns the real, currently-persisted state of
every referenced association (Deed, Property, DeedManagement, Budget,
ProcedureType) — never a client-supplied placeholder — so downstream
workflows (notably CU82's minuta de inscripción generation, which reads
the linked Deed's real status) can rely on the data they read back.

## ADDED Requirements

### Requirement: Procedure creation resolves FK references to real persisted state
`POST /api/v1/tramites` SHALL accept plain association ids
(`idProcedureType`, `idProperty`, `idDeed`, `idManagement`, `idBudget`) and
SHALL resolve each provided id against its own persisted row before saving
the `Procedure`, rather than trusting any client-supplied nested object.

#### Scenario: Creating a Procedure with a real Deed id returns the Deed's actual state
- **WHEN** a client `POST`s `/api/v1/tramites` with `idProcedureType` set to
  an existing type and `idDeed` set to the id of a Deed whose `status` is
  `"Firmada"` and whose `number` is non-zero
- **THEN** the response's embedded Deed reflects `status: "Firmada"` and the
  real `number` — not a default/blank Deed

#### Scenario: A subsequent GET reflects the same real association state
- **WHEN** a client `GET`s `/api/v1/tramites/{id}` for a Procedure created
  with a real `idDeed`
- **THEN** the response's embedded Deed reflects the same real, persisted
  state as at creation time, not defaults

#### Scenario: idProcedureType is required
- **WHEN** a client `POST`s `/api/v1/tramites` without `idProcedureType`
- **THEN** the response is `400 Bad Request`

#### Scenario: A non-existent referenced id is rejected
- **WHEN** a client `POST`s `/api/v1/tramites` with an `idDeed` (or
  `idProperty`, `idManagement`, `idBudget`) that does not exist
- **THEN** the response is `404 Not Found`

### Requirement: Procedure update resolves FK references the same way
`PUT /api/v1/tramites/{id}` SHALL apply the same resolution rules as
creation for any association id present in the request body.

#### Scenario: Updating a Procedure with a real Deed id returns the Deed's actual state
- **WHEN** a client `PUT`s `/api/v1/tramites/{id}` with `idDeed` set to the
  id of a Deed whose `status` is `"Firmada"`
- **THEN** the response's embedded Deed reflects `status: "Firmada"`, not a
  default/blank Deed
