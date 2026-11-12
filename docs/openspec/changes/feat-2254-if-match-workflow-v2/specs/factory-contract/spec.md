## ADDED Requirements

### Requirement: One OpenAPI tree documents A.3 writes

`openapi/factory-control.openapi.yaml` SHALL remain the single factory contract
tree. It SHALL document `If-Match` on catalog PUT, `schema_version` /
`created_by` on the entity envelope, workflow body v2 schemas, and 409
version-conflict Problem responses. No second OpenAPI write tree SHALL be added.

#### Scenario: If-Match and schema fields appear in OpenAPI

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares If-Match on catalog PUT and envelope
  fields `schema_version` and `created_by` without introducing a second tree
