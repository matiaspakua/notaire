# Notaire — Access Credentials

> **Single source of truth:** all credentials live in the git-ignored **`.env`**
> file at the repository root. Copy `.env.example` → `.env` and edit there.
> The values below are the defaults shipped in `.env.example`.

---

## Notaire Application

| Service | URL | Username | Password | `.env` keys |
|---------|-----|----------|----------|-------------|
| **Frontend** | http://localhost:3000 | `admin` | `admin` | `APP_ADMIN_USER` / `APP_ADMIN_PASSWORD` |
| **Backend API** | http://localhost:8080 | – | – | Swagger: `/swagger-ui.html` |
| **Backend Actuator/Metrics** | http://localhost:8080/actuator/prometheus | `admin` | `admin` | `ACTUATOR_USER` / `ACTUATOR_PASSWORD` |
| **Audit Module (UI)** | http://localhost:3000/dashboard/auditoria | (app login) | (app login) | – |
| **PostgreSQL** | localhost:5432 | `admin` | `admin` | `POSTGRES_USER` / `POSTGRES_PASSWORD` |
| **pgAdmin** | http://localhost:5050 | `admin@notaire.com` | `admin` | `PGADMIN_DEFAULT_EMAIL` / `PGADMIN_DEFAULT_PASSWORD` |

---

## Observability

| Service | URL | Username | Password | `.env` keys |
|---------|-----|----------|----------|-------------|
| **Homer Dashboard** | http://localhost:8888 | – | – | – |
| **Grafana** | http://localhost:3001 | `admin` | `admin` | `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` |
| **Prometheus** | http://localhost:9090 | – | – | – |
| **Loki** | http://localhost:3100 | – | – | – |
| **PostgreSQL Exporter** | http://localhost:9187/metrics | – | – | – |

---

## Code Quality

| Service | URL | Username | Password | `.env` keys |
|---------|-----|----------|----------|-------------|
| **SonarQube** | http://localhost:9000 | `admin` | `Admin@123456` | `SONAR_ADMIN_USER` / `SONAR_ADMIN_PASSWORD` |

> SonarQube forces a password change on first login. `scripts/run-sonar.sh`
> performs this automatically and generates an analysis token, stored back in
> `.env` as `SONAR_TOKEN`.

---

## Docker Networks

| Network | Services |
|---------|----------|
| `notaire_notary-network` | postgres, backend, frontend, pgadmin (+ infra attaches here) |
| `infra_devsecops-network` | grafana, prometheus, loki, promtail, postgres-exporter, sonarqube, sonar-db, dashboard |

---

## Quick Start

```bash
cp .env.example .env          # one-time: create your local credentials file
bash scripts/start-all.sh     # start application + infrastructure
open http://localhost:8888    # Homer hub — links to everything
bash infra/scripts/check-infra.sh   # verify all services are healthy
```
