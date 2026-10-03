# Configurable host ports and container names for the dev stack

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1186 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `chore/1186_docker_stack_isolation` |
| Gate 1 status | draft — Owner approved the scope ("proceed with your recommendation") |

## Objetivo

Allow several dev stacks to run side by side (QA runs, concurrent agent sessions)
by making the hardcoded container names and host ports of `docker-compose.yml`
overridable from `.env`, with defaults identical to today.

## What Changes

- `docker-compose.yml`: `container_name` becomes `${NOTAIRE_<SERVICE>_CONTAINER_NAME:-<current>}`
  and host ports become `${<SERVICE>_PORT:-<current>}` (container ports unchanged).
- `scripts/start.sh`: health checks and the printed URLs use the configured ports.
- `.env.example` and the deployment guide document the new keys and their limits.
- New static guard `scripts/test_dev_stack_isolation.py`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Defaults MUST render exactly the current names and ports | #1186; observability scrapes `notary-backend`/`notary-postgres` | Made explicit |
| Overrides MUST change only host-side ports and container names | #1186 | New |
| `start.sh` MUST follow the configured ports | #1186 | New |
| Secrets stay in `.env`; no credential values introduced | Constitution P9 | Made explicit |

## Capabilities

### New Capabilities

- `dev-stack-isolation`: overridable host ports and container names for the dev compose stack.

### Modified Capabilities

- (none under `openspec/specs/`)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | Removed module |
| `notaire-shared` | no | — |
| `docker-compose.yml`, `scripts/start.sh` | yes | Parametrised names/ports |
| CI/CD | no | Defaults unchanged, workflows unaffected |

### Surface area

- Entities / Endpoints / Flyway: none
- Configuration / `.env`: 8 new optional keys in `.env.example`
- Dependencies: none

### Architecture review

Configuration only; no ADR. The observability stack (`infra/`) joins the network
`notaire_notary-network` and scrapes `notary-backend` and `notary-postgres`, so it
only supports the default names; this is documented, not changed.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `.env.example` | Document the eight optional keys |
| `docs/200-architecture/209-deployment/README.md` | Dev compose section: parallel stacks, `COMPOSE_PROJECT_NAME`, observability limitation |
| `CHANGELOG.md` | `[Unreleased]` entry |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Add #1186 to the GitHub ID table |

## Out of Scope

- `docker-compose.prod.yml` (internal-only ports) and `docker-compose.cloud.yml` (host networking).
- Making the observability stack follow custom names.
- Resuming the #921 documentation-audit plan found in the same stash.
