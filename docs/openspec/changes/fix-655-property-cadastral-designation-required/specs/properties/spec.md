# properties — delta

## Purpose

Registering and maintaining the properties (inmuebles) involved in a deed.

## MODIFIED Requirements

### Requirement: Cadastral designation required

`POST /api/v1/inmueble` and `PUT /api/v1/inmueble/{id}` SHALL reject a body whose `cadastralDesignation` is missing, null, empty or blank with 400 naming the field, and SHALL change nothing; the contract SHALL mark it required on both.

#### Scenario: Create without designation

- **WHEN** a create sends {}, or a missing, null, empty or blank designation
- **THEN** the response is 400 naming cadastralDesignation and no property is stored

#### Scenario: Update without designation

- **WHEN** a full update omits the designation
- **THEN** the response is 400 and the stored property is unchanged

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** the request schema of POST and PUT lists cadastralDesignation as required

### Requirement: Form enforces the designation

The Inmuebles dialog SHALL keep Save disabled while the nomenclatura catastral is blank.

#### Scenario: Form

- **WHEN** the user fills other fields but leaves the nomenclatura blank or only spaces
- **THEN** Save is disabled, and it is enabled once a designation is typed
