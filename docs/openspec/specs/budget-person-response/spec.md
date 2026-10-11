# budget-person-response Specification

## Purpose

A `Budget` response includes the linked client as a nested `person`, so pickers can render the name without a second request. Source: archived change fix-budget-person-pg-json.

## Requirements

### Requirement: Budget responses expose the nested person association

When a `Budget` is linked to a client, the API MUST return a nested
`person` with `personId`, `name`, and `lastName`
(`person: { "personId": <id>, "name": <first>, "lastName": <last> }`) on create,
update, get-by-id, and list payloads — matching frontend `DtoPerson` so pickers
can render `lastName, name` without a second round-trip. Create uses
POST /api/v1/budgets. When not linked, the
`person` property MUST be omitted from JSON.

#### Scenario: Create with nested person returns person.personId

- **WHEN** a client POSTs `/api/v1/budgets` with
  `"person": { "personId": N }`
- **THEN** the response is HTTP 201 and includes `$.person.personId` equal to N

#### Scenario: Create with nested person returns person name fields

- **WHEN** a client POSTs `/api/v1/budgets` with
  `"person": { "personId": N }` for a client whose first and last names are set
- **THEN** the response includes `$.person.name` and `$.person.lastName`
  matching that client

#### Scenario: Create without person omits the person field

- **WHEN** a client POSTs `/api/v1/budgets` without a person association
- **THEN** the response is HTTP 201 and `$.person` does not exist

#### Scenario: Get by id returns the nested person when linked

- **WHEN** a linked `Budget` is fetched via GET `/api/v1/budgets/{id}`
- **THEN** the body includes `$.person.personId` matching the linked client
  plus `$.person.name` and `$.person.lastName`
