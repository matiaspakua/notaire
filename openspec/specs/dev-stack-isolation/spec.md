# dev-stack-isolation Specification

## Purpose
Let several dev stacks coexist by making container names and host ports
overridable with unchanged defaults. Source: #1186; owner CU76.
## Requirements
### Requirement: Container names are overridable

Each container of `docker-compose.yml` MUST take its name from
`${NOTAIRE_<SERVICE>_CONTAINER_NAME:-<current name>}`.

#### Scenario: Container names are overridable

- **WHEN** `docker-compose.yml` is inspected
- **THEN** postgres, backend, pgadmin and frontend each use an overridable
  container name whose default is the current name

### Requirement: Host ports are overridable

Host-side ports MUST take `${<SERVICE>_PORT:-<current port>}`; container-side
ports MUST NOT change.

#### Scenario: Host ports are overridable

- **WHEN** `docker-compose.yml` is inspected
- **THEN** postgres, backend, pgadmin and frontend publish an overridable host
  port mapped to the unchanged container port

### Requirement: Defaults are unchanged

Without overrides the rendered configuration MUST equal today's names and ports.

#### Scenario: Defaults render today's names and ports

- **WHEN** `docker compose config` runs with no overrides
- **THEN** it renders `notary-postgres`, `notary-backend`, `notary-pgadmin`,
  `notaire-frontend` and host ports 5432, 8080, 5050, 3000

#### Scenario: Overrides are rendered

- **WHEN** `docker compose config` runs with the override variables set
- **THEN** the rendered names and host ports use the overrides and the container
  ports are still 5432, 8080, 80 and 3000

### Requirement: start.sh follows the configured ports

`scripts/start.sh` MUST health-check and print URLs for the configured host ports,
not literal ones.

#### Scenario: start.sh follows configured ports

- **WHEN** `scripts/start.sh` is inspected
- **THEN** its health checks and printed URLs use the configured port variables
  instead of literal `localhost:8080`, `:5050`, `:3000` or `:5432`

### Requirement: The new keys are documented

The override keys MUST be documented in `.env.example` and the deployment guide,
including the limitation for the observability stack.

#### Scenario: New keys are documented

- **WHEN** `.env.example` and the deployment guide are read
- **THEN** both list the eight override keys, and the guide states that the
  observability stack targets the default names

