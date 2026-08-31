<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
Criterion (Gate 1) and must be traceable to a test in traceability.md. Business
rules belong here in normative form (SHALL/MUST); permanent Use Case
documentation remains the source of truth — cite it, do not duplicate it. -->

## Purpose

Defines the data type and creation/update behavior of an Inmueble's `valuación
fiscal` (assessed property value) field, so that the entity, DTO, and frontend
all agree with the Postgres schema (`inmuebles.valuacion_fiscal real`, since
`V1__initial_schema.sql`). See CU69 – Gestión de Inmuebles for the field's
business meaning.

## ADDED Requirements

### Requirement: Inmueble valuación fiscal is a numeric field

The system SHALL represent an Inmueble's `valuacionFiscal` as a numeric value
(matching the Postgres `real` column) end to end: entity, DTO, and the REST
API request/response body.

#### Scenario: Create Inmueble with a numeric valuación fiscal

- **WHEN** `POST /api/v1/inmueble` is called with a JSON body whose
  `valuacionFiscal` is a number (e.g. `150000.5`)
- **THEN** the Inmueble is created and persisted with that value, and the
  response body returns `valuacionFiscal` as a JSON number equal to the
  value sent

#### Scenario: Create Inmueble without a valuación fiscal

- **WHEN** `POST /api/v1/inmueble` is called with `valuacionFiscal` omitted
  or `null`
- **THEN** the Inmueble is created successfully with `valuacionFiscal`
  persisted as `null`

<!-- An "update valuación fiscal" scenario was deliberately not included:
InmuebleController's PUT /api/v1/inmueble/{id} path throws an unrelated
NullPointerException (tramiteList, see Issue #880) for any Inmueble update,
independent of this field's type. Tracked separately, out of scope here. -->
