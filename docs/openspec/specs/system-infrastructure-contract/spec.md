# system-infrastructure-contract Specification

## Purpose
Notaire obtains observability and deploy infrastructure from an independent
multi-system product repository and publishes a local system descriptor so that
product can manage Notaire via scrape, logs and traces.
## Requirements
### Requirement: Infrastructure path is a git submodule of the product repository

Notaire MUST record `infra/` as a git submodule whose URL is
`https://github.com/matiaspakua/infrastructure.git`, and MUST NOT vendor a
second copy of that tree in the monorepo.

#### Scenario: gitmodules points at the infrastructure product

- **WHEN** a developer inspects `.gitmodules`
- **THEN** an entry with `path = infra` names the `matiaspakua/infrastructure` repository

#### Scenario: module verify still runs from the same path

- **WHEN** the submodule is initialized and `bash infra/verify.sh` runs with a system descriptor
- **THEN** the infrastructure module gate executes from that path

### Requirement: Notaire publishes a system descriptor for infrastructure management

Notaire MUST provide `ops/system.yaml` that declares at least system id, external
Docker network name, scrape targets for backend and frontend metrics, and OTLP
resource attributes, matching the running application compose identity.

#### Scenario: descriptor matches application network and containers

- **WHEN** the seam test reads `ops/system.yaml` and root `docker-compose.yml`
- **THEN** the descriptor network name and scrape container targets are consistent with the application compose file

#### Scenario: start-infra consumes SYSTEM_DESCRIPTOR

- **WHEN** a developer starts the observability stack from Notaire via the documented start scripts
- **THEN** the infrastructure scripts resolve `SYSTEM_DESCRIPTOR` to Notaire's `ops/system.yaml` (or an explicit override)

