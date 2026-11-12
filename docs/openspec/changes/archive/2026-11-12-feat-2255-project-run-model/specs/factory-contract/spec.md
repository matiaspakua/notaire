## ADDED Requirements

### Requirement: One OpenAPI tree documents Project Run and RunRequest

`openapi/factory-control.openapi.yaml` SHALL remain the single factory contract
tree. It SHALL document Project, Run, and RunRequest schemas plus
`/factory/projects` and `/factory/runs` paths (including events and cancel).
No second OpenAPI tree SHALL be added.

#### Scenario: OpenAPI Project Run RunRequest schemas present

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares Project, Run, and RunRequest schemas and
  the project/run paths without introducing a second tree
