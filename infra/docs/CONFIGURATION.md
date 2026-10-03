# Configuring the infrastructure

Everything configurable, and where. Template: [`../.env.example`](../.env.example).
**Never commit `infra/.env` or real credentials.**

## Environment variables

| Variable | Used by | Placeholder in `.env.example` |
|----------|---------|-------------------------------|
| `POSTGRES_DB` | postgres-exporter connection string | `notaire` |
| `POSTGRES_EXPORTER_USER` / `POSTGRES_EXPORTER_PASSWORD` | exporter login (role from Flyway V12, `pg_monitor` only) | `notaire_exporter` / `admin` |
| `ACTUATOR_USER` / `ACTUATOR_PASSWORD` | Prometheus scrape of `/actuator/prometheus` | `admin` / `admin` |
| `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` | Grafana admin login | `admin` / `admin` |
| `SONAR_DB_USER` / `SONAR_DB_PASSWORD` | SonarQube's own database | `sonar` / `sonar` |
| `SONAR_ADMIN_USER` / `SONAR_ADMIN_PASSWORD` | SonarQube admin (changed on first login by `run-sonar.sh`) | `admin` / `Admin@123456` |
| `SONAR_TOKEN` | analysis token, written back by `run-sonar.sh` | empty |

The placeholders are for local development. Change every one of them before any
shared or production use.

### Script overrides

| Variable | Default | Purpose |
|----------|---------|---------|
| `INFRA_ENV_FILE` | none | explicit env file, highest precedence |
| `NOTAIRE_APP_NETWORK` | `notaire_notary-network` | application Docker network to join |
| `NOTAIRE_APP_DIR` | `..` (monorepo parent) | application checkout for `run-sonar.sh`; also the root `.env` fallback |

## Credentials and URLs (local defaults)

| Service | URL | Login |
|---------|-----|-------|
| Homer | http://localhost:8888 | – |
| Grafana | http://localhost:3001 | `GRAFANA_ADMIN_*` |
| Prometheus | http://localhost:9090 | – |
| Loki | http://localhost:3100 | – |
| PostgreSQL exporter | http://localhost:9187/metrics | – |
| SonarQube | http://localhost:9000 | `SONAR_ADMIN_*` |

Application credentials (frontend, API, pgAdmin) belong to the application
repository; see its `.env.example`.

## Configuration files

| File | Configures |
|------|-----------|
| `observability/docker-compose.yml` | services, ports, volumes, networks (project name pinned to `infra`) |
| `observability/prometheus/prometheus.yml` | scrape jobs `notaire-backend`, `notaire-postgres`, `grafana`, `loki` |
| `observability/prometheus/alert-rules.yml` | alert rules |
| `observability/prometheus/postgres_exporter.yml` | custom exporter queries |
| `observability/grafana/grafana.ini` | Grafana server settings |
| `observability/grafana/provisioning/` | datasources and dashboards (auto-provisioned) |
| `observability/loki/local-config.yaml`, `promtail-config.yaml` | log storage and shipping |
| `observability/dashboard/config.yml` | Homer landing page links |
| `infra/deploy/kustomize/base/nginx.conf` | the one reverse-proxy config (compose + Kubernetes) |

## Known limitation

The `basic_auth` block in `prometheus.yml` is a literal `admin`/`admin`, not read
from `ACTUATOR_*`. If you change the backend's Actuator credentials, edit that
block too.

## Pinned images

Every `image:` in `observability/docker-compose.yml` is pinned to a minor version
or digest — no `:latest`. Bump deliberately in a PR; policy in
[ADR-017](../../docs/200-architecture/202-ADR/ADR-017-container-base-images.md),
guard: `python3 scripts/test_image_pins_and_dependabot.py`.

## Compose project name

`observability/docker-compose.yml` sets `name: infra`. Do not remove it: the
project name prefixes volumes and networks (`infra_grafana_data`,
`infra_devsecops-network`), and changing it orphans existing data.
