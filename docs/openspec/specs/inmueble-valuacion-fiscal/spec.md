# inmueble-valuacion-fiscal Specification

## Purpose

Defines the data type and the create and update behavior of a property's
fiscal appraisal (`fiscalAppraisal`), so that the entity, DTO, and frontend
agree with the live Postgres column `properties.fiscal_valuation`
`NUMERIC(19, 2)`. See CU69 – Gestión de Inmuebles for the field's business
meaning.

## Requirements

### Requirement: Property fiscal appraisal is a numeric field

The system SHALL represent a property's `fiscalAppraisal` as a numeric value
(matching `properties.fiscal_valuation`) end to end: entity, DTO, and the
REST API request and response body.

#### Scenario: Create a property with a numeric fiscal appraisal

- **WHEN** `POST /api/v1/properties` is called with a JSON body whose
  `fiscalAppraisal` is a number (for example `150000.5`)
- **THEN** the property is created and persisted with that value, and the
  response body returns `fiscalAppraisal` as a JSON number equal to the
  value sent

#### Scenario: Create a property without a fiscal appraisal

- **WHEN** `POST /api/v1/properties` is called with `fiscalAppraisal` omitted
  or `null`
- **THEN** the property is created successfully with `fiscalAppraisal`
  persisted as `null`

#### Scenario: Update a property's fiscal appraisal

- **WHEN** `PUT /api/v1/properties/{id}` is called with a numeric
  `fiscalAppraisal`
- **THEN** the property is updated and the stored `fiscalAppraisal` equals
  the value sent
