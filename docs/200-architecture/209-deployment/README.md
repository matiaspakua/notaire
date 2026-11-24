# Deployment Guide - Notaire Project

## Overview

This guide covers how to deploy the complete Notaire system: the application stack and the
observability/quality infrastructure stack.

## Architecture

Generated from the [LikeC4 model](../210-architecture-model/README.md) (ADR-033); interactive on
[GitHub Pages](https://matiaspakua.github.io/notaire/architecture-model/#/view/dev/).

### Development (`docker-compose.yml` + `infra/observability/docker-compose.yml`)

<!-- likec4-view: dev -->
```mermaid
---
title: "Deployment: development"
---
flowchart TB
  subgraph dev_appNet["notary-network"]
    subgraph dev_appNet_pgadminHost["notary-pgadmin :5050"]
      dev_appNet_pgadminHost_pgadmin["pgAdmin<br/><i>pgAdmin 4</i>"]
    end
    subgraph dev_appNet_frontendHost["notaire-frontend :3000"]
      dev_appNet_frontendHost_frontend["Web frontend<br/><i>Next.js 16, React 19, TypeScript</i>"]
    end
    subgraph dev_appNet_backendHost["notary-backend :8080"]
      dev_appNet_backendHost_backend["Backend API<br/><i>Spring Boot 4.1, Java 26</i>"]
    end
    subgraph dev_appNet_dbHost["notary-postgres :5432<br/><i>postgres:16.15, volume postgres_data</i>"]
      dev_appNet_dbHost_database[("Notarial database<br/><i>PostgreSQL 16</i>")]
    end
  end
  subgraph dev_opsNet["devsecops-network"]
    subgraph dev_opsNet_grafanaHost["grafana"]
      dev_opsNet_grafanaHost_grafana["Grafana<br/><i>Grafana 12.3</i>"]
    end
    subgraph dev_opsNet_promHost["prometheus :9090"]
      dev_opsNet_promHost_prometheus["Prometheus<br/><i>Prometheus 3.15</i>"]
      dev_opsNet_promHost_alertmanager["Alertmanager<br/><i>Alertmanager 0.34</i>"]
    end
    subgraph dev_opsNet_logsHost["loki :3100 + promtail"]
      dev_opsNet_logsHost_loki["Loki<br/><i>Loki 3.7 + Promtail</i>"]
    end
    subgraph dev_opsNet_exportersHost["exporters"]
      dev_opsNet_exportersHost_exporters["Exporters<br/><i>postgres-exporter, blackbox, node-exporter, cAdvisor</i>"]
    end
    subgraph dev_opsNet_collectorHost["otel-collector"]
      dev_opsNet_collectorHost_otelCollector["OpenTelemetry Collector<br/><i>otel-collector-contrib 0.162</i>"]
    end
    subgraph dev_opsNet_tracesHost["tempo"]
      dev_opsNet_tracesHost_tempo["Tempo<br/><i>Tempo 2.10</i>"]
    end
  end
  dev_appNet_frontendHost_frontend -- "Proxies /api/v1 with the httpOnly JWT cookie (+2)" --> dev_appNet_backendHost_backend
  dev_appNet_backendHost_backend -- "Reads and writes, Flyway migrates on start (+3)" --> dev_appNet_dbHost_database
  dev_appNet_pgadminHost_pgadmin -- "Inspects (dev only)" --> dev_appNet_dbHost_database
  dev_opsNet_promHost_prometheus -- "Fires alerts" --> dev_opsNet_promHost_alertmanager
  dev_opsNet_collectorHost_otelCollector -. "Exports traces" .-> dev_opsNet_tracesHost_tempo
  dev_opsNet_promHost_prometheus -. "Scrapes" .-> dev_opsNet_exportersHost_exporters
  dev_opsNet_grafanaHost_grafana -- "Queries metrics" --> dev_opsNet_promHost_prometheus
  dev_opsNet_grafanaHost_grafana -- "Queries logs" --> dev_opsNet_logsHost_loki
  dev_opsNet_grafanaHost_grafana -- "Queries traces" --> dev_opsNet_tracesHost_tempo
  dev_appNet_frontendHost_frontend -. "Browser traces via /api/otlp (+1)" .-> dev_opsNet_collectorHost_otelCollector
  dev_opsNet_promHost_prometheus -. "Scrapes /api/metrics" .-> dev_appNet_frontendHost_frontend
  dev_appNet_backendHost_backend -. "OTLP traces (+1)" .-> dev_opsNet_collectorHost_otelCollector
  dev_opsNet_promHost_prometheus -. "Scrapes /actuator/prometheus (+2)" .-> dev_appNet_backendHost_backend
  dev_opsNet_exportersHost_exporters -- "Reads pg_stat (+1)" --> dev_appNet_dbHost_database
  classDef c_primary fill:#dbeafe,stroke:#1d4ed8,color:#0f172a
  classDef c_indigo fill:#e0e7ff,stroke:#4338ca,color:#0f172a
  class dev_appNet_pgadminHost_pgadmin,dev_opsNet_grafanaHost_grafana,dev_opsNet_promHost_prometheus,dev_opsNet_logsHost_loki,dev_opsNet_promHost_alertmanager,dev_opsNet_exportersHost_exporters,dev_appNet_frontendHost_frontend,dev_appNet_backendHost_backend,dev_opsNet_collectorHost_otelCollector,dev_opsNet_tracesHost_tempo c_primary
  class dev_appNet_dbHost_database c_indigo
```
<!-- /likec4-view -->

### Production (`docker-compose.prod.yml`)

Host `:80` → reverse proxy → frontend (`/`) and backend (`/api`). PostgreSQL, backend and frontend publish no
host port; no pgAdmin; `ENVIRONMENT=production`; Flyway baseline off. TLS terminates in front of the proxy.
The proxy forwards only `/actuator/health` ([#2487](https://github.com/matiaspakua/notaire/issues/2487)). Open
finding (red): the backend gets no MFA key, MFA policy or `COOKIE_SECURE`
([#2488](https://github.com/matiaspakua/notaire/issues/2488)).

<!-- likec4-view: prod -->
```mermaid
---
title: "Deployment: production"
---
flowchart TB
  subgraph prod_edge["Public edge"]
    prod_edge_proxyHost_ingress["Reverse proxy<br/><i>nginx 1.27</i>"]
  end
  subgraph prod_private["notary-network (private)"]
    subgraph prod_private_feHost["frontend<br/><i>ghcr.io/matiaspakua/notaire/frontend</i>"]
      prod_private_feHost_frontend["Web frontend<br/><i>Next.js 16, React 19, TypeScript</i>"]
    end
    subgraph prod_private_beHost["backend<br/><i>ghcr.io/matiaspakua/notaire/backend</i>"]
      prod_private_beHost_backend["Backend API<br/><i>Spring Boot 4.1, Java 26</i>"]
    end
    subgraph prod_private_pgHost["postgres<br/><i>postgres:16, volume postgres_data</i>"]
      prod_private_pgHost_database[("Notarial database<br/><i>PostgreSQL 16</i>")]
    end
  end
  prod_private_feHost_frontend -- "Proxies /api/v1 with the httpOnly JWT cookie (+2)" --> prod_private_beHost_backend
  prod_private_beHost_backend -- "Reads and writes, Flyway migrates on start (+3)" --> prod_private_pgHost_database
  prod_edge_proxyHost_ingress -- "Routes / (UI)" --> prod_private_feHost_frontend
  prod_edge_proxyHost_ingress -- "Routes /api (+2)" --> prod_private_beHost_backend
  classDef c_primary fill:#dbeafe,stroke:#1d4ed8,color:#0f172a
  classDef c_indigo fill:#e0e7ff,stroke:#4338ca,color:#0f172a
  class prod_edge_proxyHost_ingress,prod_private_feHost_frontend,prod_private_beHost_backend c_primary
  class prod_private_pgHost_database c_indigo
  linkStyle 3 stroke:#b91c1c,stroke-width:2px,color:#b91c1c
```
<!-- /likec4-view -->

### Staging (`infra/deploy/kustomize/overlays/staging`, #901)

Same four services, one replica each; PostgreSQL, backend and frontend are `ClusterIP`, the proxy is a
`LoadBalancer`. Images are pinned to the release tag; secrets are placeholders. CD stays publish-only: apply the
manifests to a cluster by hand. The PostgreSQL pod keeps its data on `emptyDir`, so a restart loses it
([#2489](https://github.com/matiaspakua/notaire/issues/2489)).

<!-- likec4-view: staging -->
```mermaid
---
title: "Deployment: staging (Kubernetes)"
---
flowchart TB
  subgraph staging_cluster_proxyPod["reverse-proxy (Service LoadBalancer :80)"]
    staging_cluster_proxyPod_ingress["Reverse proxy<br/><i>nginx 1.27</i>"]
  end
  subgraph staging_cluster_fePod["frontend (ClusterIP :3000)"]
    staging_cluster_fePod_frontend["Web frontend<br/><i>Next.js 16, React 19, TypeScript</i>"]
  end
  subgraph staging_cluster_bePod["backend (ClusterIP :8080)"]
    staging_cluster_bePod_backend["Backend API<br/><i>Spring Boot 4.1, Java 26</i>"]
  end
  subgraph staging_cluster_pgPod["postgres (ClusterIP :5432)"]
    staging_cluster_pgPod_database[("Notarial database<br/><i>PostgreSQL 16</i>")]
  end
  staging_cluster_proxyPod_ingress -- "Routes / (UI)" --> staging_cluster_fePod_frontend
  staging_cluster_proxyPod_ingress -- "Routes /api (+2)" --> staging_cluster_bePod_backend
  staging_cluster_fePod_frontend -- "Proxies /api/v1 with the httpOnly JWT cookie (+2)" --> staging_cluster_bePod_backend
  staging_cluster_bePod_backend -- "Reads and writes, Flyway migrates on start (+3)" --> staging_cluster_pgPod_database
  classDef c_primary fill:#dbeafe,stroke:#1d4ed8,color:#0f172a
  classDef c_indigo fill:#e0e7ff,stroke:#4338ca,color:#0f172a
  class staging_cluster_proxyPod_ingress,staging_cluster_fePod_frontend,staging_cluster_bePod_backend c_primary
  class staging_cluster_pgPod_database c_indigo
  linkStyle 1 stroke:#b91c1c,stroke-width:2px,color:#b91c1c
```
<!-- /likec4-view -->

## Deployment Steps

### Prerequisites

- Docker and Docker Compose v2+
- Java 26 (the release pinned in the root `pom.xml`) (for local development)
- Maven 3.9+ (for local builds)

### 1. Build Application

```bash
# Build entire project
mvn clean install -DskipTests

# Build only backend + shared module
mvn clean install -pl backend-api -am -DskipTests
```

### 2. Start the Application Stack (development)

```bash
bash workspace/stack/start.sh
# or, from repo root:
docker-compose up -d

# Verify services are running
docker-compose ps
```

This starts the **dev** stack:

- **PostgreSQL 16** on port 5432
- **Backend API** on port 8080
- **Frontend (Next.js)** on port 3000
- **pgAdmin** on port 5050 (started by default; skip with `bash workspace/stack/start.sh --no-admin`)

> Production must use `docker-compose.prod.yml` (below) — never the published
> Postgres/pgAdmin ports from the dev compose.
>
### 3. Start Monitoring & Quality Infrastructure

```bash
bash infra/scripts/start-infra.sh
```

Prerequisites, environment file and operation details: [infra docs](../../../infra/README.md).

This starts:

- **Prometheus** on port 9090
- **Grafana** on port 3001 (credentials via `.env`)
- **Loki + Promtail** — log aggregation (queried at port 3100)
- **SonarQube** on port 9000
- **PostgreSQL Exporter** on port 9187
- **Homer** on port 8888 — landing page linking every service

### 4. Verify Complete Deployment

```bash
# Check all services
curl http://localhost:8080/actuator/health   # Backend
curl http://localhost:3000                   # Frontend
curl http://localhost:9090/-/ready           # Prometheus
curl http://localhost:3001/api/health        # Grafana
curl http://localhost:3100/ready             # Loki
```

## Docker Compose Details

### Root docker-compose.yml (development)

- **Services**: `postgres`, `backend`, `frontend`, `pgadmin`
- **Network**: `notary-network` (bridge)
- **Volumes**: `postgres_data`, `pgadmin_data`
- **Backend health check**: `/actuator/health`
- **Environment variables**: Configured via `.env` file (see `.env.example`)
- **Not for production** — publishes Postgres/pgAdmin/app ports and uses
  `${VAR:-admin}` defaults for local ergonomics

#### Parallel dev stacks (issue #1186)

Host ports and container names can be overridden from `.env` (or the shell) so a second stack
runs beside the first. Defaults are unchanged.

| Variable | Default |
|----------|---------|
| `POSTGRES_PORT` / `BACKEND_PORT` / `PGADMIN_PORT` / `FRONTEND_PORT` | `5432` / `8080` / `5050` / `3000` |
| `NOTAIRE_POSTGRES_CONTAINER_NAME` | `notary-postgres` |
| `NOTAIRE_BACKEND_CONTAINER_NAME` | `notary-backend` |
| `NOTAIRE_PGADMIN_CONTAINER_NAME` | `notary-pgadmin` |
| `NOTAIRE_FRONTEND_CONTAINER_NAME` | `notaire-frontend` |

```bash
COMPOSE_PROJECT_NAME=notaire_alt BACKEND_PORT=18080 FRONTEND_PORT=13000 POSTGRES_PORT=15432 \
PGADMIN_PORT=15050 NOTAIRE_POSTGRES_CONTAINER_NAME=alt-postgres NOTAIRE_BACKEND_CONTAINER_NAME=alt-backend \
NOTAIRE_PGADMIN_CONTAINER_NAME=alt-pgadmin NOTAIRE_FRONTEND_CONTAINER_NAME=alt-frontend \
bash workspace/stack/start.sh
```

A distinct `COMPOSE_PROJECT_NAME` also separates volumes and the network. The observability
stack (`infra/`) scrapes `notary-backend` and `notary-postgres` on `notaire_notary-network`, so it
only works with the default container names. `workspace/stack/start.sh` defaults
`COMPOSE_PROJECT_NAME` to `notaire` so git worktrees still publish that network; override it
only for intentional parallel stacks. `docker-compose.prod.yml` and
`docker-compose.cloud.yml` are not affected.

### docker-compose.prod.yml (production entrypoint — issue #1044)

- **Services**: `postgres`, `backend`, `frontend`, `reverse-proxy` (**no pgAdmin**)
- **Host ports**: only the reverse proxy (`:80`); postgres/backend/frontend stay on the Docker network
- **Secrets**: required via `${VAR:?...}` — compose fails fast if `.env` is incomplete; no `admin` defaults
- **MFA and session cookie** (#2488): `NOTAIRE_MFA_ENCRYPTION_KEY` is required (`infra/scripts/ensure-mfa-encryption-key.sh .env` writes one); `NOTAIRE_MFA_POLICY` defaults to `OPTIONAL` (ADR-032); `COOKIE_SECURE` defaults to `true` because TLS terminates in front of the proxy (set `false` only on a plain-HTTP lab). The Kustomize base reads the key from `notaire-secrets`
- **Backend**: `ENVIRONMENT=production` (activates `ProductionCredentialsGuard`); least-privilege env (no Grafana/pgAdmin/exporter credential keys); `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false`
- **Proxy config**: `infra/deploy/kustomize/base/nginx.conf` — `/` → frontend, `/api/` and `/actuator/` → backend
- **TLS**: terminate TLS in front of this proxy (or extend the nginx config); see [HTTPS-TLS-RUNBOOK.md](../206-security/HTTPS-TLS-RUNBOOK.md) (#254)
- **Backups**: automated backup productization remains issue #256. CI verification
  of backup→restore→smoke is scaffolded in `.github/workflows/backup-restore-smoke.yml`
  (#1067) and **skips with an explicit #256 message** until `infra/scripts/backup-postgres.sh`
  exists (no false-green restore).

### infra/deploy/kustomize (staging manifests — issue #901)

- **Base** (`infra/deploy/kustomize/base`): same four services as `docker-compose.prod.yml`
  (postgres, backend, frontend, reverse-proxy) — **no pgAdmin**
- **Staging overlay** (`infra/deploy/kustomize/overlays/staging`): GHCR **semver**
  image tags for backend/frontend (release-please markers, #1591);
  `app.kubernetes.io/version` on workloads; `environment: staging` labels
- **Service isolation**: postgres/backend/frontend are `ClusterIP` (internal-only);
  reverse-proxy is the sole external `LoadBalancer`
- **Secrets**: `notaire-secrets` uses **placeholders only** (`PLACEHOLDER_SET_AT_APPLY_TIME`);
  replace before apply — never commit real credentials
- **Backend posture**: `ENVIRONMENT=production`, `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false`
- **CD**: `.github/workflows/cd.yml` stays **publish-only** (GHCR). Manifest apply is
  operator-owned; there is no automated “deploy to cluster” job yet
- **Validate**: `python3 infra/tests/test_staging_kustomize.py` (requires `kustomize` on PATH)

### Infrastructure stack (`infra/`)

Observability and quality services (Homer, SonarQube, Prometheus, postgres-exporter, Grafana,
Loki, Promtail) are defined in `infra/observability/docker-compose.yml`; what each service
does and how it couples to the application is in
[infra DEFINITION](../../../infra/docs/DEFINITION.md).

## Environment Configuration

All credentials live in a single, git-ignored `.env` file at the repo root (copy `.env.example`).
`docker-compose.yml`, `docker-compose.prod.yml`, and `infra/observability/docker-compose.yml` read from it —
never hard-code secrets in compose files or docs.

Key variables include `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`,
`ACTUATOR_USER`/`ACTUATOR_PASSWORD` (Prometheus scrape auth), `APP_ADMIN_USER` /
`APP_ADMIN_PASSWORD`, and (for Flyway V12 / infra exporter) `POSTGRES_EXPORTER_USER` /
`POSTGRES_EXPORTER_PASSWORD`. See the production section in `.env.example`.

### Frontend upstream URL (issue #1055)

- Browser traffic stays on same-origin `/api/v1` (HttpOnly cookie path, #1051).
- The Next.js App Router Route Handler BFF reads **runtime** `BACKEND_URL`
  (server-only) on each request — one frontend image works across environments
  without baking `next.config` rewrite destinations at `next build`.
- Compose sets `BACKEND_URL=http://backend:8080/api/v1` (dev/prod/cloud). Do
  **not** put Docker-internal hosts in `NEXT_PUBLIC_API_URL`; the public login
  page must not display backend infrastructure URLs (CU78).

## Production deployment (docker-compose.prod.yml)

1. Copy and harden secrets (do **not** keep `.env.example` `admin` placeholders):

```bash
cp .env.example .env
# Set strong unique values for POSTGRES_*, JWT_SECRET, ACTUATOR_*, APP_ADMIN_*,
# and POSTGRES_EXPORTER_* (used as Flyway placeholders; not injected as Grafana/pgAdmin env on the backend).
```

1. Start the production stack:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
docker compose -f docker-compose.prod.yml ps
```

1. Verify through the reverse proxy only:

```bash
curl -fsS http://localhost/                  # frontend via reverse proxy
curl -fsS http://localhost/actuator/health   # backend via reverse proxy
# Postgres (:5432), backend (:8080), frontend (:3000), and pgAdmin must NOT be published on the host.
```

1. Confirm Flyway baseline-on-migrate is off in the prod file (`SPRING_FLYWAY_BASELINE_ON_MIGRATE=false`).

## Staging deployment (Kustomize — issue #901)

Step-by-step apply (image pinning, Secret creation, render and validate) lives in
[infra OPERATION](../../../infra/docs/OPERATION.md#staging-deployment-kustomize). The staging
topology mirrors `docker-compose.prod.yml`; CD stays publish-only, so applying the manifests
is a manual operator step.

## Production Considerations

For production deployment, ensure:

1. **Use `docker-compose.prod.yml`** — not the dev compose with published DB/admin ports
2. **Change default credentials** — do not reuse `.env.example` values; prod compose requires them via `${VAR:?}`
3. **Enable HTTPS** — [HTTPS-TLS-RUNBOOK.md](../206-security/HTTPS-TLS-RUNBOOK.md) (#254)
4. **Set a strong `JWT_SECRET`** — see [API Authentication Guide](../206-security/API-AUTHENTICATION-GUIDE.md)
5. **Database backups** — configure periodic `pg_dump` backups (issue #256); after
   that lands, the scheduled backup→restore→smoke workflow (#1067) will exercise restore
6. **Resource limits** — set Docker resource constraints
7. **Log rotation** — configure Docker log rotation
8. **Monitoring alerts** — configure Prometheus alerting rules (`infra/observability/prometheus/alert-rules.yml`)

## Rollback Procedure

```bash
# Stop production stack
docker compose -f docker-compose.prod.yml down

# Or stop the development stack
docker-compose down

# Remove specific volumes if needed
docker compose -f docker-compose.prod.yml down -v

# Restore database from backup (container must be running / on the compose network)
docker exec -i notary-postgres psql -U "$POSTGRES_USER" "$POSTGRES_DB" < backup.sql

# Restart previous version
docker compose -f docker-compose.prod.yml --env-file .env up -d
```

## Related Documentation

- [Monitoring Guide](../207-monitoring/README.md)
- [DevSecOps Pipeline](../208-devsecops/README.md)
- [Architecture Overview](../README.md)
