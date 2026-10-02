## ADDED Requirements

### Requirement: Budget responses expose nested person association

When a presupuesto is linked to a client, the API MUST return
`person: { "personId": <id> }` on create, update, get-by-id, and list payloads.
When not linked, the `person` property MUST be omitted from JSON.

#### Scenario: Create with nested person returns person.personId

- **WHEN** a client POSTs `/api/v1/presupuestos` with
  `"person": { "personId": N }`
- **THEN** the response is HTTP 201 and includes `$.person.personId` equal to N

#### Scenario: Create without person omits person field

- **WHEN** a client POSTs `/api/v1/presupuestos` without a person association
- **THEN** the response is HTTP 201 and `$.person` does not exist

#### Scenario: Get by id returns nested person when linked

- **WHEN** a linked presupuesto is fetched via GET `/api/v1/presupuestos/{id}`
- **THEN** the body includes `$.person.personId` matching the linked client
