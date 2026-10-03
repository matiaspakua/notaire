# What the infrastructure defines

The contents of `infra/` and how they fit the Notaire system.
Architecture rationale lives in the project documentation
([SAD](../../docs/200-architecture/201-SAD/sad.md),
[ADR-016](../../docs/200-architecture/202-ADR/ADR-016-observability-stack.md));
this page describes what is defined here.

## Layout

```text
infra/
  README.md  .env.example
  observability/   docker-compose.yml + prometheus/ grafana/ loki/ dashboard/
  deploy/
    kustomize/     base/ (4 services + nginx.conf)  overlays/staging/
  performance/
    k6/            load-test.js
  scripts/         common.sh start-infra.sh check-infra.sh run-sonar.sh generate-report.sh
  docs/            PREPARATION  CONFIGURATION  DEFINITION  OPERATION
```

## Observability and quality stack

| Tool | Role | Port |
|------|------|------|
| Prometheus | metrics; scrapes backend Actuator and the exporter | 9090 |
| Grafana | dashboards over Prometheus and Loki | 3001 |
| Loki + Promtail | structured JSON log storage and Docker log shipping | 3100 |
| PostgreSQL exporter | database metrics for Prometheus | 9187 |
| SonarQube CE (+ own database) | static analysis of the backend | 9000 |
| Homer | landing page linking every service | 8888 |

Jenkins, Nexus and Dependency-Track were removed in 2026-06: not wired to the
running application.

### What is monitored

| Target | Method | Prometheus job |
|--------|--------|----------------|
| Backend API | Actuator/Micrometer `/actuator/prometheus`, Basic auth | `notaire-backend` |
| PostgreSQL | postgres-exporter | `notaire-postgres` |
| Grafana, Loki | `/metrics` | `grafana`, `loki` |

### Dashboards (provisioned in Grafana)

| UID | Shows |
|-----|-------|
| `notaire-backend` | JVM, HTTP, DB pool, log volume |
| `notaire-postgres` | connections, transactions, size |
| `notaire-logs` | backend and frontend logs from Loki |
| `notaire-auth` | login and security signals |

### Alert rules (`prometheus/alert-rules.yml`)

| Alert | Condition | Severity |
|-------|-----------|----------|
| HighLoginFailureRate | >50% of logins fail over 2m | Warning |
| SuspiciousLoginActivity | bad-credential rate above ~30/min for 1m | Critical |
| BackendDown | `up{job="notaire-backend"}` is 0 for 1m | Critical |
| HighJvmHeapUsage | JVM heap above 85% for 5m | Warning |

## Deployment artifacts (`deploy/`)

| Artifact | Definition |
|----------|-----------|
| `kustomize/base` | postgres, backend, frontend, reverse-proxy — the same four services as the application's `docker-compose.prod.yml`; no pgAdmin; secrets are placeholders |
| `kustomize/overlays/staging` | GHCR SHA image tags and `environment: staging` labels |
| `kustomize/base/nginx.conf` | the **only** reverse-proxy config; Kustomize builds the ConfigMap from it and `docker-compose.prod.yml` mounts the same file |

Service isolation: postgres, backend and frontend are `ClusterIP`; only the
reverse proxy is a `LoadBalancer`. CD (`cd.yml`) is publish-only — applying the
manifests is a manual operator step.

## Performance (`performance/k6`)

`load-test.js` exercises the highest-traffic authenticated read endpoints with
CU74 thresholds (p95 < 2 s, < 1% failures). It runs weekly and on demand via
`.github/workflows/performance-test.yml`.

## Coupling with the application

`infra/` is self-contained except for these seams, which a repository split must
replace:

| Seam | Today | After a split |
|------|-------|---------------|
| Docker network | stack joins `notaire_notary-network` and scrapes `notary-backend`, `notary-postgres` by name | unchanged; the application must publish that network |
| Reverse-proxy config | root `docker-compose.prod.yml` mounts `infra/deploy/kustomize/base/nginx.conf` | publish a proxy image, or have the application repository vendor the file |
| Env fallback | scripts fall back to the application's root `.env` | drop the fallback; use `infra/.env` |
| Sonar analysis | `run-sonar.sh` runs Maven in the application checkout | set `NOTAIRE_APP_DIR` |
| Images | Kustomize pulls `ghcr.io/<owner>/notaire/{backend,frontend}` produced by the application CD | unchanged |
| CI | `.github/workflows/performance-test.yml` references `infra/performance/k6` | move the workflow with the folder |
| Docs links | `../../docs/...` links into project documentation | replace with absolute URLs |
