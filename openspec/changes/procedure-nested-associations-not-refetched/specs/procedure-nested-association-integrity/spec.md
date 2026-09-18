# Specification: Procedure nested association integrity

**Issue:** #981
**Use Case:** CU82 — Generar y hacer seguimiento de la minuta de inscripción

## Summary

`POST`/`PUT /api/v1/tramites` must persist and return the real,
previously-persisted state of every referenced association
(`fkIdDeed`, `fkIdProperty`, `fkIdBudget`, `fkIdManagement`,
`fkIdProcedureType`), not the blank/default transient object Jackson
constructs from a client-supplied `{"idX": N}` nested reference.

## Scope

**In scope:**

- `ProcedurePersistenceAdapter.save()` re-fetching each non-null nested
  association by id before delegating to `ProcedureRepository.save()`.
- Unit tests for the adapter (fake/mocked repositories).
- An integration test reproducing the bug end-to-end against Postgres.

**Out of scope:**

- Any REST contract change (URLs, request/response shape, status codes).
- The frontend (already sends the correct nested-reference shape).
- `TS-0082-minuta-inscripcion-feature.spec.ts` itself.

## Acceptance Scenarios

### Scenario 1: Creating a Procedure with a nested Deed reference preserves the real Deed state

**Given:** A `Deed` exists with `status: "Firmada"`, a real `number`
**When:** `POST /api/v1/tramites` is called with
`{"fkIdDeed": {"idDeed": <the Deed's id>}, ...}`
**Then:** The response's `fkIdDeed` reflects the real, persisted
`status`/`number` — not `"Sin Firmar"`/`0`

### Scenario 2: Reading the Procedure back after creation reflects the real association state

**Given:** A `Procedure` was created per Scenario 1
**When:** `GET /api/v1/tramites/{id}` is called
**Then:** The response's `fkIdDeed` reflects the real, persisted Deed state

### Scenario 3: Updating a Procedure with a nested Property reference preserves the real Property state

**Given:** A `Property` exists with real `cadastralDesignation`/`address` data;
a `Procedure` already references a different Property
**When:** `PUT /api/v1/tramites/{id}` is called with a nested
`{"fkIdProperty": {"idProperty": <the real Property's id>}}`
**Then:** The response's `fkIdProperty` reflects the real, persisted
Property state — not blank/default fields

### Scenario 4: Referencing a non-existent association id fails cleanly

**Given:** No `Deed` exists with id `999999`
**When:** `POST /api/v1/tramites` is called with `{"fkIdDeed": {"idDeed": 999999}, ...}`
**Then:** The request fails (500, consistent with the controller's existing
error-handling contract for persistence failures) rather than silently
persisting a placeholder
