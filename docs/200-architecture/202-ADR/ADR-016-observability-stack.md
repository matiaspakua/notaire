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
(`infra/docker-compose.yml`), deployed independently from the application
stack (`docker-compose.yml`), and bridged via one external Docker network
(`notaire_notary-network`) so both stacks can resolve each other's
containers by name.

### Topology
| Container | Image | Port | Role |
|-----------|-------|------|------|
| `devsecops-prometheus` | prom/prometheus | 9090 | Scrapes `backend:8080/actuator/prometheus` (Basic auth) and `postgres-exporter:9187`; evaluates `infra/prometheus/alert-rules.yml` |
| `devsecops-grafana` | grafana/grafana | 3001 | Dashboards (`notaire-backend`, `notaire-postgres`, `notaire-logs`), provisioned from `infra/grafana/` |
| `devsecops-loki` | grafana/loki | 3100 | Log aggregation, queried by Grafana (`{container_name="notary-backend"}`) |
| `devsecops-promtail` | grafana/promtail | — | Tails Docker container logs (socket-mounted) and pushes to Loki |
| `devsecops-postgres-exporter` | prometheuscommunity/postgres-exporter | 9187 | Database-level metrics, connects to the app's PostgreSQL via the `pg_monitor` role (Flyway `V12`) |
| `devsecops-sonarqube` | sonarqube | 9000 | Static analysis (`bash infra/scripts/run-sonar.sh`), backed by its own `devsecops-sonar-db` (PostgreSQL 15) |
| `devsecops-dashboard` (Homer) | b4bz/homer | 8888 | Landing page linking every service |

### Startup ordering
The application stack must be started first (`bash scripts/start.sh`), then
the infra stack (`bash infra/scripts/start-infra.sh`) — or both together via
`bash scripts/start-all.sh`. This is because Prometheus/postgres-exporter
scrape the running backend/database on the shared external network; starting
infra first leaves scrape targets unreachable until the app stack joins.

### Alerting
Prometheus evaluates alert rules natively (see ADR-009) but **no
Alertmanager is deployed** — alerts are visible in Prometheus's `/alerts` UI
and can be queried from Grafana, but nothing routes them to a notification
channel today.

## Options Considered
- **Single Compose file for app + observability**: Rejected — couples
  unrelated lifecycles (a developer working only on backend/frontend
  shouldn't need Prometheus/Grafana/SonarQube running) and infra containers
  are heavier than the app stack.
- **Managed/cloud observability (Datadog, New Relic, Grafana Cloud)**:
  Rejected to stay self-hosted and provider-agnostic (consistent with
  ADR-009's rationale).

## Consequences
- **Pros**: App and observability stacks can be started/stopped
  independently; matches local-dev and CI needs (CI doesn't need the infra
  stack for `mvn verify`).
- **Cons**: Two Compose files to keep in sync (shared network name,
  credentials via the single root `.env`); no Alertmanager means alert rules
  are passive until a notification integration is added.
