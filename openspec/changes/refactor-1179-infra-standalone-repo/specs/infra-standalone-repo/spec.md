<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Make `infra/` a single, self-contained, documented folder that can be split into
its own repository without further changes. Source: #1179; owner CU77.

## ADDED Requirements

### Requirement: Infrastructure assets live under infra/

The observability stack, Kubernetes manifests, reverse-proxy configuration, k6
load tests and infra scripts MUST live under `infra/`. The legacy locations
`deploy/` and `performance-test/` MUST NOT exist.

#### Scenario: All infra assets live under infra/

- **WHEN** the repository tree is inspected
- **THEN** `infra/observability`, `infra/deploy/kustomize`,
  `infra/performance/k6` and `infra/scripts` exist

#### Scenario: Legacy locations no longer exist

- **WHEN** the repository tree is inspected
- **THEN** `deploy/` (including `deploy/nginx`), `performance-test/` and the old `infra/docker-compose.yml`,
  `infra/prometheus`, `infra/grafana`, `infra/loki`, `infra/dashboard` do not exist

#### Scenario: Stale E2E suite removed

- **WHEN** the repository tree is inspected
- **THEN** `infra/tests/` does not exist

### Requirement: infra/ is self-contained

Files under `infra/` MUST NOT reference paths outside `infra/` (no `../` escapes
in compose mounts or scripts). The only permitted coupling to the application is
the documented one: the external app Docker network, the scraped backend
endpoints and the root env file passed explicitly.

#### Scenario: infra/ does not reference paths outside itself

- **WHEN** compose files and scripts under `infra/` are scanned
- **THEN** none resolve a path above the `infra/` directory, except the
  documented optional env-file fallback

#### Scenario: infra/ ships its own env example without secrets

- **WHEN** `infra/.env.example` is read
- **THEN** it lists every variable the infra stack needs and contains no real
  credential values

### Requirement: Reverse-proxy configuration has a single source

The reverse-proxy `nginx.conf` MUST exist exactly once, at
`infra/deploy/kustomize/base/nginx.conf`. The Kubernetes ConfigMap MUST be
generated from that file and `docker-compose.prod.yml` MUST mount the same file.

#### Scenario: nginx.conf has a single source

- **WHEN** the repository is searched for `nginx.conf` files and the Kustomize
  base is built
- **THEN** only `infra/deploy/kustomize/base/nginx.conf` exists, the rendered
  ConfigMap content equals that file, and `docker-compose.prod.yml` mounts it

### Requirement: Infra specifics are documented in infra/

`infra/README.md` and `infra/docs/{PREPARATION,CONFIGURATION,DEFINITION,OPERATION}.md`
MUST exist and document how to prepare, configure, define and run the infra.
`docs/` MUST link to `infra/` and MUST NOT duplicate those how-to details.

#### Scenario: Infrastructure documentation set exists and is linked from docs/

- **WHEN** the documentation is inspected
- **THEN** the five infra documents exist and `docs/200-architecture/207-monitoring/README.md`
  and `docs/200-architecture/209-deployment/README.md` link to them

### Requirement: Consumers follow the new paths

Workflows, scripts, compose files, agent rule files and guard tests MUST
reference the new paths; no file outside `docs/000-archive`, `openspec/changes/archive`
and `CHANGELOG.md` may reference a legacy path.

#### Scenario: Consumers point to the new paths

- **WHEN** the tracked files are searched for the legacy paths
- **THEN** no active file references `deploy/kustomize`, `deploy/nginx`,
  `performance-test/` or `infra/docker-compose.yml`

#### Scenario: Stack still starts and manifests still validate

- **WHEN** the existing static validators and `docker compose config` run
- **THEN** `test_staging_kustomize.py`, `test_prod_compose.py`,
  `test_infra_prometheus_hardening.py`, `test_performance_test_assets.py` and
  config rendering of the infra compose pass
