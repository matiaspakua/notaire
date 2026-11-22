## ADDED Requirements

### Requirement: OpenAPI contract documents config file endpoints and catalog delete

`openapi/factory-control.openapi.yaml` SHALL document `GET /factory/config/files`,
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
