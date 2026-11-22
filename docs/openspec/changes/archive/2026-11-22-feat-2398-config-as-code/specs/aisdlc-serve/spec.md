## ADDED Requirements

### Requirement: Serve host dispatches config file paths and catalog delete

`aisdlc serve` SHALL dispatch `GET /factory/config/files` and
`GET /factory/config/files/{path}` to the config_files service layer, and SHALL dispatch
`DELETE /factory/catalogs/{kind}/{id}` (when `--write` is set) to the catalogs delete
function with write scope, CSRF, and If-Match validation.

#### Scenario: GET config/files routes to config_files

- **WHEN** serve receives `GET /factory/config/files` with a valid Bearer read token
- **THEN** it returns 200 with the config tree JSON from the service layer

#### Scenario: GET config/files/{path} routes to config_files

- **WHEN** serve receives `GET /factory/config/files/.aisdlc/aisdlc.toml` with a valid
  Bearer read token
- **THEN** it returns 200 with the file content JSON from the service layer

#### Scenario: DELETE catalog entry routes to catalogs.delete

- **WHEN** serve runs with `--write` and receives `DELETE /factory/catalogs/skills/s`
  with valid write scope, CSRF, and If-Match
- **THEN** it calls `catalogs.delete()` and returns the result or an error
