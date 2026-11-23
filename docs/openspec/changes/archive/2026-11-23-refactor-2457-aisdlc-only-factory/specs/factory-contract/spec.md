## MODIFIED Requirements

### Requirement: One OpenAPI tree documents A.3 writes

`openapi/aisdlc-factory.openapi.yaml` SHALL remain the single factory contract
tree. It SHALL document `If-Match` on catalog PUT, `schema_version` /
`created_by` on the entity envelope, workflow body v2 schemas, and 409
version-conflict Problem responses. No second OpenAPI write tree SHALL be added.

#### Scenario: If-Match and schema fields appear in OpenAPI

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares If-Match on catalog PUT and envelope
  fields `schema_version` and `created_by` without introducing a second tree

### Requirement: One OpenAPI tree documents Project Run and RunRequest

`openapi/aisdlc-factory.openapi.yaml` SHALL remain the single factory contract
tree. It SHALL document Project, Run, and RunRequest schemas plus
`/factory/projects` and `/factory/runs` paths (including events and cancel).
No second OpenAPI tree SHALL be added.

#### Scenario: OpenAPI Project Run RunRequest schemas present

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares Project, Run, and RunRequest schemas and
  the project/run paths without introducing a second tree

### Requirement: OpenAPI contract documents config file endpoints and catalog delete

`openapi/aisdlc-factory.openapi.yaml` SHALL document `GET /factory/config/files`,
`GET /factory/config/files/{path}`, and `DELETE /factory/catalogs/{kind}/{id}` paths,
including schemas `ConfigFileTree`, `ConfigFileContent`, and error responses
(409 version_conflict, 428 if_match_required). No second OpenAPI tree SHALL be added.

#### Scenario: OpenAPI declares config file paths

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares `/factory/config/files` and
  `/factory/config/files/{path}` with ConfigFileTree and ConfigFileContent schemas

#### Scenario: OpenAPI declares catalog delete path

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI declares `DELETE /factory/catalogs/{kind}/{id}` with
  409 and 428 error responses

#### Scenario: no second OpenAPI tree

- **WHEN** `aisdlc contract check` runs after this change
- **THEN** the factory OpenAPI remains a single tree

## ADDED Requirements

### Requirement: The contract file carries the kit's name

The factory contract SHALL live at `openapi/aisdlc-factory.openapi.yaml` in the kit and in the packaged wheel
(`aisdlc/_kit/openapi/aisdlc-factory.openapi.yaml`). The HTTP paths it declares SHALL NOT change because of the
rename. The kit's `openapi/` directory SHALL hold no other contract file.

#### Scenario: aisdlc contract resolves the renamed file

- **WHEN** `aisdlc contract show` runs
- **THEN** it names `openapi/aisdlc-factory.openapi.yaml` and `aisdlc contract check` passes

#### Scenario: the wheel ships the renamed contract

- **WHEN** the kit wheel is built
- **THEN** it contains `aisdlc/_kit/openapi/aisdlc-factory.openapi.yaml` and no other contract file
