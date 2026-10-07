# infra

**Purpose:** Observability (Prometheus, Grafana, Loki), SonarQube, Kubernetes manifests and the k6 load test.

**Verify:** `bash infra/verify.sh` (module checks only; the full gate is `bash workspace/sdlc/preflight.sh`).

## Contract (what other modules may rely on)

- Compose stack under `infra/observability/`; Kustomize base and staging overlay
- Dashboards `notaire-backend`, `notaire-postgres`, `notaire-logs`

## Seams (what this module reads from outside)

- Backend image name, container names (`notary-backend`), Actuator endpoints and credentials variables

## Must not

- Contain application code
- Hard-code credentials (they live in the git-ignored `.env`)

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
