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
| Prometheus | metrics; scrapes backend Actuator and the exporter; evaluates alert rules | 9090 |
| Alertmanager | receives Prometheus alerts (local receiver, grouping, inhibition) | 9093 |
| Grafana | dashboards over Prometheus and Loki | 3001 |
| Loki + Promtail | structured JSON log storage and Docker log shipping | 3100 |
| PostgreSQL exporter | database metrics for Prometheus | 9187 |
| SonarQube CE (+ own database) | static analysis of backend and frontend, quality gate "Notaire way" | 9000 |
| Homer | landing page linking every service | 8888 |

Jenkins, Nexus and Dependency-Track were removed in 2026-06: not wired to the
running application.

### What is monitored

| Target | Method | Prometheus job |
|--------|--------|----------------|
| Backend API | Actuator/Micrometer `/actuator/prometheus`, Basic auth | `notaire-backend` |
| PostgreSQL | postgres-exporter | `notaire-postgres` |
| Grafana, Loki | `/metrics` | `grafana`, `loki` |
| Tempo, Alertmanager, Promtail, collector self-metrics | `/metrics` | `tempo-metrics`, `alertmanager-metrics`, `promtail-metrics`, `otel-collector-self` |
| Tempo (traces, via the collector) | OTLP gRPC `tempo:4317`, query `tempo:3200` (Grafana datasource `tempo`) | n/a |
| OTel Collector (OTLP from the backend) | `otel-collector:8889` | `otel-collector` |
| Frontend (Node runtime, scrape counter) | `/api/metrics` | `frontend-metrics` |
| Containers / host (Docker VM) | cAdvisor / node-exporter | `cadvisor`, `node-exporter` |
| All stack services (11) | blackbox exporter HTTP/TCP probes, `probe_success{service=...}` | `probe-<service>` |

### Dashboards (provisioned in Grafana)

| UID | Shows |
|-----|-------|
| `notaire-infra-resources` | Docker host CPU, memory, load, disk, network; aggregate container usage |
| `notaire-frontend` | Node runtime health, proxied-request counts by status, frontend logs |
| `notaire-observability-health` | Prometheus, Loki, Tempo, Alertmanager, Promtail, collector: up, throughput, failures |
| `notaire-traces` | recent backend traces (Tempo) and request log lines linked to them |
| `notaire-stack-status` | overview: availability of every service, firing alerts, errors from Loki |
| `notaire-status` | overview: frontend, backend, PostgreSQL, pgAdmin, API rate/5xx/p95, alerts, errors |
| `notaire-backend` | JVM, HTTP, DB pool, log volume |
| `notaire-postgres` | connections, transactions, size |
| `notaire-logs` | backend and frontend logs from Loki |
| `notaire-auth` | login and security signals |

### Log-to-trace link

Promtail stores `traceId` and `spanId` as Loki structured metadata (`allow_structured_metadata: true`); the Loki
datasource's `TraceID` derived field (matcher type `label`) links to Tempo. In provisioning files the link is written
`$${__value.raw}`, because Grafana expands `${...}` there.

### Logging

The backend writes structured JSON to stdout only and never keeps log files.
Promtail discovers the Docker containers, parses the JSON envelope and pushes it
to Loki; query `{container_name="notary-backend"}` in Grafana. A backend started
directly on the host (`mvn spring-boot:run`) logs to the terminal only and is not
shipped; use `bash workspace/stack/start.sh` for centralized logs.

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
