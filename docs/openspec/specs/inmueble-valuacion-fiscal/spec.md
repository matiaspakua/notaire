# inmueble-valuacion-fiscal Specification

## Purpose
Defines the data type and creation/update behavior of an Property's `valuación
fiscal` (assessed property value) field, so that the entity, DTO, and frontend
all agree with the Postgres schema (`inmuebles.valuacion_fiscal real`, since
`V1__initial_schema.sql`). See CU69 – Gestión de Inmuebles for the field's
business meaning.
## Requirements
### Requirement: Property valuación fiscal is a numeric field

The system SHALL represent an Property's `valuacionFiscal` as a numeric value
(matching the Postgres `real` column) end to end: entity, DTO, and the REST
API request/response body.

#### Scenario: Create Property with a numeric valuación fiscal

- **WHEN** `POST /api/v1/properties` is called with a JSON body whose
  `valuacionFiscal` is a number (e.g. `150000.5`)
- **THEN** the Property is created and persisted with that value, and the
  response body returns `valuacionFiscal` as a JSON number equal to the
  value sent

#### Scenario: Create Property without a valuación fiscal

- **WHEN** `POST /api/v1/properties` is called with `valuacionFiscal` omitted
  or `null`
- **THEN** the Property is created successfully with `valuacionFiscal`
  persisted as `null`

<!-- An "update valuación fiscal" scenario was deliberately not included:
InmuebleController's PUT /api/v1/properties/{id} path throws an unrelated
NullPointerException (tramiteList, see Issue #880) for any Property update,
independent of this field's type. Tracked separately, out of scope here. -->

