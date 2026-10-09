# testimonies — delta

## Purpose

Generating, verifying and tracking testimonies (certified copies of deeds).

## MODIFIED Requirements

### Requirement: Bare testimony requires its deed

`POST /api/v1/testimonio` SHALL reject a body without `deed` or without `deed.idDeed` with 400 naming the field, SHALL answer 404 when the deed does not exist, and SHALL store nothing in both cases; the contract SHALL mark `deed` required on POST and not on PUT.

#### Scenario: Create without deed

- **WHEN** a create sends {} or a body without deed
- **THEN** the response is 400 naming deed and no testimony is stored

#### Scenario: Deed without id

- **WHEN** a create sends a deed without idDeed
- **THEN** the response is 400 naming idDeed

#### Scenario: Unknown deed

- **WHEN** a create references a deed id that does not exist
- **THEN** the response is 404 and no testimony is stored

#### Scenario: Contract

- **WHEN** the OpenAPI document is generated
- **THEN** the POST request schema lists deed as required and the PUT schema does not
