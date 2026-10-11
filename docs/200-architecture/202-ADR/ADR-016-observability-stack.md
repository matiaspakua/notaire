# ADR-016: Observability Stack Topology

## Status

Accepted

## Context

ADR-009 established the Loki-Prometheus-Grafana (LPG) pattern but did not
document the concrete container topology, network boundaries, or how the
`infra/` stack relates to the application stack (`docker-compose.yml`). As
the number of observability containers grew (Prometheus, Grafana, Loki,
Promtail, SonarQube, postgres-exporter, Homer), a dedicated ADR is needed to
record the deployment shape and its cross-network wiring.

## Decision

The observability stack lives in a **separate Compose file**
(`infra/observability/docker-compose.yml`), deployed independently from the application
stack (`docker-compose.yml`), and bridged via one external Docker network
(`notaire_notary-network`) so both stacks can resolve each other's
containers by name.

### Topology

| Container | Image | Port | Role |
|-----------|-------|------|------|
| `devsecops-prometheus` | prom/prometheus | 9090 | Scrapes `backend:8080/actuator/prometheus` (Basic auth) and `postgres-exporter:9187`; evaluates `infra/observability/prometheus/alert-rules.yml` |
| `devsecops-otel-collector` | otel/opentelemetry-collector-contrib | — | Single OTLP ingress (4317/4318); the backend pushes Micrometer metrics here and Prometheus scrapes `:8889`. The Actuator scrape is kept |
| `devsecops-alertmanager` | prom/alertmanager | 9093 | Receives alerts from Prometheus; local receiver, grouping and inhibition only |
| `devsecops-tempo` | grafana/tempo | — | Trace store (24h local retention); fed by the collector's `traces` pipeline, queried by Grafana (datasource `tempo`, linked to Loki) |
| `devsecops-node-exporter` / `devsecops-cadvisor` | prom/node-exporter, cadvisor | — | Host (Docker VM) and per-container metrics; read-only host mounts, no Docker socket |
| `devsecops-blackbox-exporter` | prom/blackbox-exporter | — | HTTP/TCP availability probes (`probe_success`) for every stack service; feeds the overview dashboards and the `ServiceDown` alert |
| `devsecops-grafana` | grafana/grafana | 3001 | Dashboards (`notaire-stack-status`, `notaire-status`, `notaire-backend`, `notaire-postgres`, `notaire-auth`, `notaire-logs`), provisioned from `infra/observability/grafana/` |
| `devsecops-loki` | grafana/loki | 3100 | Log aggregation, queried by Grafana (`{container_name="notary-backend"}`) |
| `devsecops-promtail` | grafana/promtail | — | Tails Docker container logs (socket-mounted) and pushes to Loki |
| `devsecops-postgres-exporter` | prometheuscommunity/postgres-exporter | 9187 | Database-level metrics, connects to the app's PostgreSQL via the `pg_monitor` role (Flyway `V12`) |
| `devsecops-sonarqube` | sonarqube | 9000 | Static analysis (`bash infra/scripts/run-sonar.sh`), backed by its own `devsecops-sonar-db` (PostgreSQL 15) |
| `devsecops-dashboard` (Homer) | b4bz/homer | 8888 | Landing page linking every service |

### Startup ordering

The application stack must be started first (`bash workspace/stack/start.sh`), then
the infra stack (`bash infra/scripts/start-infra.sh`) — or both together via
`bash workspace/stack/start-all.sh`. This is because Prometheus/postgres-exporter
scrape the running backend/database on the shared external network; starting
infra first leaves scrape targets unreachable until the app stack joins.

### Alerting

Prometheus evaluates alert rules natively (see ADR-009) and sends them to
**Alertmanager** (`devsecops-alertmanager`, :9093; issue #1465, amending the earlier
"not deployed" decision). Alerts are grouped by `alertname` and `service`, warnings are
inhibited while a critical alert of the same `service` fires, and the only receiver is
local: alerts are visible in the Alertmanager UI and in Grafana (datasource
`alertmanager`). No external channel (email, chat, paging) is configured because it needs
credentials, which is an owner decision. Rules live in `prometheus/alert-rules.yml` and
`prometheus/alert-rules-stack.yml`.

## Options Considered

- **Single Compose file for app + observability**: Rejected — couples
  unrelated lifecycles (a developer working only on backend/frontend
  shouldn't need Prometheus/Grafana/SonarQube running) and infra containers
  are heavier than the app stack.
- **Managed/cloud observability (Datadog, New Relic, Grafana Cloud)**:
  Rejected to stay self-hosted and provider-agnostic (consistent with
  ADR-009's rationale).

### Local authentication (issue #1413)

On the **local** observability compose only:

- Grafana enables anonymous access with the Admin org role and hides the login
  form (`infra/observability/grafana/grafana.ini`).
- SonarQube sets `SONAR_FORCEAUTHENTICATION=false` so projects can be browsed
  without a login; analysis still uses a token (`infra/scripts/run-sonar.sh`) and
  administration still requires an account.
- Grafana (`3001`) and SonarQube (`9000`) host ports bind to `127.0.0.1` so
  removing the login barrier does not expose the tools on the LAN.

`docker-compose.prod.yml` and `infra/deploy/kustomize` do not adopt anonymous
access. Production keeps credential-gated entry where those tools are deployed.

## Consequences

- **Pros**: App and observability stacks can be started/stopped
  independently; matches local-dev and CI needs (CI doesn't need the infra
  stack for `mvn verify`); local Grafana/Sonar open at a glance for CU77.
- **Cons**: Two Compose files to keep in sync (shared network name,
  credentials via the single root `.env`); alerts reach only a local
  receiver until an external notification integration is added; local
  anonymous Admin is safe only because of the loopback bind.
