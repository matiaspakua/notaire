# infra

**Purpose:** Observability (Prometheus, Grafana, Loki), SonarQube, Kubernetes manifests and the k6 load test.

**Verify:** `bash infra/verify.sh` (module checks only; the full gate is `bash workspace/sdlc/preflight.sh`).

## Contract (what other modules may rely on)

- Compose stack under `infra/observability/`; Kustomize base and staging overlay
- Drill-down dashboards `notaire-infra-resources`, `notaire-frontend`, `notaire-observability-health`, `notaire-traces`
- Overview dashboards `notaire-stack-status` (whole stack) and `notaire-status` (Notaire only)
- Specialist dashboards `notaire-backend`, `notaire-postgres`, `notaire-auth`, `notaire-logs`
- Local Grafana/SonarQube: anonymous browse (Grafana Admin, Sonar `forceAuthentication=false`), ports bound to `127.0.0.1` (#1413; guard `infra/tests/test_local_tools_no_login.py`). Prod/Kustomize stay authenticated.

## Seams (what this module reads from outside)

- Backend image name, container names (`notary-backend`), Actuator endpoints and credentials variables

## Must not

- Contain application code
- Hard-code credentials (they live in the git-ignored `.env`)

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
