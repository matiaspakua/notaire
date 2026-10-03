# prod-docker-compose Specification

## Purpose

Provide a production docker-compose artifact that hardens host exposure and
secrets for the Notaire app stack. Source: #1044; owners CU78 (security /
isolation) and CU75 (Flyway production migration posture).

## Requirements

### Requirement: Production compose has no pgAdmin and internal-only data plane

The repository MUST include `docker-compose.prod.yml` (or an equivalently named
production compose/override documented as the production entrypoint). That file
MUST NOT define a `pgadmin` service. PostgreSQL, backend, and frontend MUST NOT
publish host ports; only a reverse-proxy service MAY publish host ports.

#### Scenario: Prod compose file exists without pgAdmin

- **WHEN** an operator or validator loads the production compose file
- **THEN** the file exists at the documented path and defines no `pgadmin`
  service

#### Scenario: Database and app services publish no host ports

- **WHEN** the production compose services `postgres`, `backend`, and
  `frontend` are inspected
- **THEN** none of them declare a host `ports` mapping

#### Scenario: Only reverse proxy publishes host ports

- **WHEN** the production compose services are inspected for published ports
- **THEN** at least one reverse-proxy service publishes host ports and no
  non-proxy service publishes host ports

### Requirement: Production environment activates with required secrets

The production backend service MUST set `ENVIRONMENT=production` (so
`app.environment` / `ProductionCredentialsGuard` activate). Every secret the
prod stack requires MUST be referenced with Compose `${VAR:?...}` (or
equivalent fail-if-unset) and MUST NOT fall back to `admin` or empty defaults.
Each service MUST receive only the environment variables it needs
(least-privilege): the backend MUST NOT be given Grafana, pgAdmin, or
postgres-exporter credentials.

#### Scenario: Backend runs with ENVIRONMENT production

- **WHEN** the production compose backend environment is inspected
- **THEN** `ENVIRONMENT` is set to `production` (not `development`)

#### Scenario: Secrets required without insecure defaults

- **WHEN** the production compose environment blocks for credential variables
  (at least `POSTGRES_PASSWORD`, `JWT_SECRET`, `ACTUATOR_PASSWORD`,
  `APP_ADMIN_PASSWORD`, and datasource username/password wiring) are inspected
- **THEN** each uses `${VAR:?...}` (or equivalent) and does not use
  `${VAR:-admin}` / empty defaults

#### Scenario: Backend env is least-privilege

- **WHEN** the production compose backend `environment` map is inspected
- **THEN** it does not include `PGADMIN_DEFAULT_PASSWORD`,
  `GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD`,
  `POSTGRES_EXPORTER_USER`, or `POSTGRES_EXPORTER_PASSWORD`

### Requirement: Flyway baseline-on-migrate is disabled in production

Production compose MUST NOT enable Flyway baseline-on-migrate. The backend
environment MUST set `SPRING_FLYWAY_BASELINE_ON_MIGRATE` to `false`, or omit
the property so the framework default (disabled) applies — it MUST NOT be
`true`.

#### Scenario: Flyway baseline-on-migrate disabled in prod

- **WHEN** the production compose backend environment is inspected
- **THEN** `SPRING_FLYWAY_BASELINE_ON_MIGRATE` is absent or explicitly `false`
  (never `true`)

### Requirement: Deployment guide documents the production compose

Permanent deployment documentation MUST instruct operators how to copy/set
required secrets, start the production compose, and understand that Postgres
and admin UIs are not exposed on the host.

#### Scenario: Deployment guide documents prod compose

- **WHEN** a reviewer reads `docs/200-architecture/209-deployment/README.md`
  after the change
- **THEN** it documents the production compose path, required secrets posture,
  absence of pgAdmin, reverse-proxy-only host ports, and Flyway baseline-off
