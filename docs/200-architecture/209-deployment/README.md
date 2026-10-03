# Deployment Guide - Notaire Project

## Overview

This guide covers how to deploy the complete Notaire system: the application stack and the
observability/quality infrastructure stack.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                 Dev stack (docker-compose.yml)                       │
│  PostgreSQL :5432 · Backend :8080 · Frontend :3000 · pgAdmin :5050 │
└────────────────────────────┬───────────────────────────────────────┘
                              │ (shared network: notary-network)
┌────────────────────────────┴───────────────────────────────────────┐
│                          Infra Stack                                 │
│                    (infra/docker-compose.yml)                        │
│  Prometheus :9090 · Grafana :3001 · Loki · SonarQube · Homer …     │
└───────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│           Production stack (docker-compose.prod.yml)                 │
│                                                                      │
│   Host :80 → reverse-proxy → frontend (/) + backend (/api,/actuator)│
│   postgres / backend / frontend: internal only (no host ports)       │
│   No pgAdmin · ENVIRONMENT=production · Flyway baseline off          │
└───────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│     Staging Kustomize (deploy/kustomize/overlays/staging — #901)     │
│                                                                      │
│   Same four services as prod compose (no pgAdmin)                    │
│   postgres/backend/frontend: ClusterIP · reverse-proxy: LoadBalancer │
│   Images: GHCR SHA tags · Secrets: placeholders only                 │
│   CD remains publish-only — apply manifests manually to a cluster    │
└───────────────────────────────────────────────────────────────────────┘
```

## Deployment Steps

### Prerequisites
- Docker and Docker Compose v2+
- Java 21+ (for local development)
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
bash scripts/start.sh
# or, from repo root:
docker-compose up -d

# Verify services are running
docker-compose ps
```

This starts the **dev** stack:
- **PostgreSQL 16** on port 5432
- **Backend API** on port 8080
- **Frontend (Next.js)** on port 3000
- **pgAdmin** on port 5050 (started by default; skip with `bash scripts/start.sh --no-admin`)

> Production must use `docker-compose.prod.yml` (below) — never the published
> Postgres/pgAdmin ports from the dev compose.
### 3. Start Monitoring & Quality Infrastructure

```bash
bash infra/scripts/start-infra.sh
# or, from infra/:
cd infra
docker-compose up -d
```

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

### docker-compose.prod.yml (production entrypoint — issue #1044)
- **Services**: `postgres`, `backend`, `frontend`, `reverse-proxy` (**no pgAdmin**)
- **Host ports**: only the reverse proxy (`:80`); postgres/backend/frontend stay on the Docker network
- **Secrets**: required via `${VAR:?...}` — compose fails fast if `.env` is incomplete; no `admin` defaults
- **Backend**: `ENVIRONMENT=production` (activates `ProductionCredentialsGuard`); least-privilege env (no Grafana/pgAdmin/exporter credential keys); `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false`
- **Proxy config**: `deploy/nginx/nginx.conf` — `/` → frontend, `/api/` and `/actuator/` → backend
- **TLS**: terminate TLS in front of this proxy (or extend the nginx config); full certbot/ACME productization is issue #254
- **Backups**: automated backup productization remains issue #256. CI verification
  of backup→restore→smoke is scaffolded in `.github/workflows/backup-restore-smoke.yml`
  (#1067) and **skips with an explicit #256 message** until `scripts/backup-postgres.sh`
  exists (no false-green restore).

### deploy/kustomize (staging manifests — issue #901)
- **Base** (`deploy/kustomize/base`): same four services as `docker-compose.prod.yml`
  (postgres, backend, frontend, reverse-proxy) — **no pgAdmin**
- **Staging overlay** (`deploy/kustomize/overlays/staging`): GHCR SHA image tags for
  backend/frontend; `environment: staging` labels
- **Service isolation**: postgres/backend/frontend are `ClusterIP` (internal-only);
  reverse-proxy is the sole external `LoadBalancer`
- **Secrets**: `notaire-secrets` uses **placeholders only** (`PLACEHOLDER_SET_AT_APPLY_TIME`);
  replace before apply — never commit real credentials
- **Backend posture**: `ENVIRONMENT=production`, `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false`
- **CD**: `.github/workflows/cd.yml` stays **publish-only** (GHCR). Manifest apply is
  operator-owned; there is no automated “deploy to cluster” job yet
- **Validate**: `python3 scripts/test_staging_kustomize.py` (requires `kustomize` on PATH)

### Infra docker-compose.yml
- **Services**: `dashboard` (Homer), `sonarqube`, `sonar-db`, `prometheus`, `postgres-exporter`,
  `grafana`, `loki`, `promtail`
- **Networks**: `devsecops-network` + `notaire-app-network` (external, joins the app stack)
- **Volumes**: All service data persisted
- **Configuration files**: Pre-configured in `infra/<service>/` subdirectories

## Environment Configuration

All credentials live in a single, git-ignored `.env` file at the repo root (copy `.env.example`).
`docker-compose.yml`, `docker-compose.prod.yml`, and `infra/docker-compose.yml` read from it —
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

2. Start the production stack:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
docker compose -f docker-compose.prod.yml ps
```

3. Verify through the reverse proxy only:

```bash
curl -fsS http://localhost/                  # frontend via reverse proxy
curl -fsS http://localhost/actuator/health   # backend via reverse proxy
# Postgres (:5432), backend (:8080), frontend (:3000), and pgAdmin must NOT be published on the host.
```

4. Confirm Flyway baseline-on-migrate is off in the prod file (`SPRING_FLYWAY_BASELINE_ON_MIGRATE=false`).

## Staging deployment (Kustomize — issue #901)

Reproducible staging topology mirroring `docker-compose.prod.yml`. Requires
`kustomize` v5+ and a Kubernetes cluster (not provided by this repo).

1. Pin GHCR SHA tags published by CD (replace `sha-PLACEHOLDER`):

```bash
cd deploy/kustomize/overlays/staging
kustomize edit set image \
  notaire-backend=ghcr.io/<owner>/notaire/backend:<git-sha> \
  notaire-frontend=ghcr.io/<owner>/notaire/frontend:<git-sha>
```

2. Replace Secret placeholders (do **not** commit real values):

```bash
kubectl -n notaire create secret generic notaire-secrets \
  --from-literal=POSTGRES_DB=... \
  --from-literal=POSTGRES_USER=... \
  --from-literal=POSTGRES_PASSWORD=... \
  --from-literal=SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/... \
  --from-literal=JWT_SECRET=... \
  --from-literal=ACTUATOR_USER=... \
  --from-literal=ACTUATOR_PASSWORD=... \
  --from-literal=APP_ADMIN_USER=... \
  --from-literal=APP_ADMIN_PASSWORD=... \
  --from-literal=POSTGRES_EXPORTER_USER=... \
  --from-literal=POSTGRES_EXPORTER_PASSWORD=... \
  --dry-run=client -o yaml | kubectl apply -f -
```

3. Render and apply:

```bash
kustomize build deploy/kustomize/overlays/staging | kubectl apply -f -
# or: kubectl apply -k deploy/kustomize/overlays/staging
python3 scripts/test_staging_kustomize.py
```

4. Reach the app only via the reverse-proxy LoadBalancer / ingress. Postgres,
   backend, and frontend remain ClusterIP. CD image publish alone does **not**
   imply the cluster was updated — apply is a separate, manual step until a real
   deploy job exists.

## Production Considerations

For production deployment, ensure:

1. **Use `docker-compose.prod.yml`** — not the dev compose with published DB/admin ports
2. **Change default credentials** — do not reuse `.env.example` values; prod compose requires them via `${VAR:?}`
3. **Enable HTTPS** — terminate TLS in front of the reverse proxy (issue #254)
4. **Set a strong `JWT_SECRET`** — see [API Authentication Guide](../206-security/API-AUTHENTICATION-GUIDE.md)
5. **Database backups** — configure periodic `pg_dump` backups (issue #256); after
   that lands, the scheduled backup→restore→smoke workflow (#1067) will exercise restore
6. **Resource limits** — set Docker resource constraints
7. **Log rotation** — configure Docker log rotation
8. **Monitoring alerts** — configure Prometheus alerting rules (`infra/prometheus/alert-rules.yml`)

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
