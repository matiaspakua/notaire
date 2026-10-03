<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Amend the `infra-standalone-repo` spec: load tests are system-level V&V and move to `testing/`
(#1191).

## MODIFIED Requirements

### Requirement: Infrastructure assets live under infra/

The observability stack, Kubernetes manifests, reverse-proxy configuration and infra scripts
MUST live under `infra/`. Load tests MUST live under `testing/performance`. The legacy
locations `deploy/` and `performance-test/` MUST NOT exist.

#### Scenario: All infra assets live under infra/

- **WHEN** the repository tree is inspected
- **THEN** `infra/observability`, `infra/deploy/kustomize` and `infra/scripts` exist, and
  `infra/performance` does not

#### Scenario: Legacy locations no longer exist

- **WHEN** the repository tree is inspected
- **THEN** `deploy/` (including `deploy/nginx`), `performance-test/` and the old `infra/docker-compose.yml`,
  `infra/prometheus`, `infra/grafana`, `infra/loki`, `infra/dashboard` do not exist

#### Scenario: Stale E2E suite removed

- **WHEN** the repository tree is inspected
- **THEN** `infra/tests/` does not exist
