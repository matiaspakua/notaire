# Monitoring & Observability - Notaire Project

## Overview

This document describes the monitoring and observability infrastructure for the Notaire
system. The backend API and PostgreSQL database are monitored through a unified
observability stack: **Prometheus**, **Grafana**, **Loki**, **Promtail**, and
**postgres-exporter**, fronted by a **Homer** landing page. The stack is defined in
`infra/docker-compose.yml`; see [`infra/README.md`](../../../infra/README.md) for the
authoritative, up-to-date service list, ports, and credentials.

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      Notaire Application                     │
├───────────────────────────┬────────────────────────────────┤
│  Backend API (Spring Boot) │  PostgreSQL 16                  │
│  :8080                     │  :5432                          │
└──────────┬──────────────────┴───────────────┬─────────────────┘
           │ /actuator/prometheus              │ pg_stat_* views
           │ (Micrometer, Basic Auth)          │
           ▼                                    ▼
┌──────────────────────────────────────────────────────────────┐
│                      Prometheus :9090                        │
│   Scrapes: notaire-backend, notaire-postgres (via exporter)  │
└──────────┬──────────────────────────────────────┬─────────────┘
           │                                        │
           ▼                                        │
┌────────────────────┐                              │
│   Grafana :3001     │◄─────────────────────────────┘
│   Dashboards:        │
│   - notaire-backend  │        ┌──────────────────────┐
│   - notaire-postgres │◄───────┤   Loki :3100          │
│   - notaire-logs     │        │   (log storage)       │
└──────────────────────┘        └───────────┬────────────┘
                                             ▲
                                             │
                                  ┌──────────┴──────────┐
                                  │      Promtail        │
                                  │ (Docker log shipper)  │
                                  └──────────┬──────────┘
                                             │
                          ┌──────────────────┴──────────────────┐
                          │  notary-backend / notary-postgres    │
                          │         container logs               │
                          └───────────────────────────────────────┘

                    ┌──────────────────────────┐
                    │  Homer Hub :8888          │
                    │  (landing page linking     │
                    │   every service above)     │
                    └──────────────────────────┘
```

## Services Overview

### 1. Backend API Monitoring

**Metrics Collection:** Spring Boot Actuator with Micrometer
- **Endpoint:** `http://localhost:8080/actuator/prometheus`
- **Auth:** Basic Auth via `ACTUATOR_USER` / `ACTUATOR_PASSWORD` (set in `.env`)
- **Scrape Interval:** Every 10 seconds

**Metrics Exposed:**
- JVM metrics (memory, GC, threads, classes)
- HTTP request metrics (rate, duration, errors)
- Database connection pool (HikariCP)
- Logback event rate
- Custom business metrics (operation counts, durations, API errors, login attempts)

**Grafana Dashboard:** `notaire-backend` (pre-provisioned)
- API request rate & response time (P95)
- JVM memory usage & garbage collection
- Active threads & database connection pool
- HTTP error rate & application health
- Log rate by level

### 2. PostgreSQL Database Monitoring

**Metrics Collection:** postgres-exporter
- **Endpoint:** `http://localhost:9187/metrics`
- **Configuration:** `infra/prometheus/postgres_exporter.yml`
- **Connection:** connects with a dedicated, least-privilege role (granted only
  `pg_monitor`, created by Flyway migration `V12`) — **not** the application's own
  admin datasource credentials (issue #675). Set `POSTGRES_EXPORTER_USER` /
  `POSTGRES_EXPORTER_PASSWORD` in `.env`.

**Grafana Dashboard:** `notaire-postgres` (pre-provisioned)
- Database size & connection count
- Transaction commit/rollback rates
- Query performance

### 3. Log Aggregation (Loki + Promtail)

**Loki Endpoint:** `http://localhost:3100`
**Promtail Configuration:** `infra/loki/promtail-config.yaml`

**Scraped Containers:** all `notaire-*`, `notary-*`, and `devsecops-*` containers.

**Log Labels:** `container`, `container_name`, `service`, `app`, `level`.

**Grafana Dashboard:** `notaire-logs` (pre-provisioned) — query in Grafana → Explore →
Loki datasource: `{container_name="notary-backend"} | json`.

## Access Credentials

See [`infra/CREDENTIALS.md`](../../../infra/CREDENTIALS.md) for the full, current list.
Summary:

| Service | URL | Credentials |
|---------|-----|--------------|
| **Homer Hub** | http://localhost:8888 | – |
| **Grafana** | http://localhost:3001 | `$GRAFANA_ADMIN_USER` / `$GRAFANA_ADMIN_PASSWORD` |
| **Prometheus** | http://localhost:9090 | – |
| **Loki** | http://localhost:3100 | – |
| **SonarQube** | http://localhost:9000 | `$SONAR_ADMIN_USER` / `$SONAR_ADMIN_PASSWORD` |
| **pgAdmin** | http://localhost:5050 | `$PGADMIN_EMAIL` / `$PGADMIN_PASSWORD` |
| **Backend API** | http://localhost:8080 | JWT bearer (see [API Authentication Guide](../206-security/API-AUTHENTICATION-GUIDE.md)) |

## Getting Started

```bash
# Start application + infrastructure together
bash scripts/start-all.sh

# Or independently — application must be up first (infra joins its network)
bash scripts/start.sh
bash infra/scripts/start-infra.sh

# Health check
bash infra/scripts/check-infra.sh
```

### Access Dashboards
- **Homer Dashboard:** http://localhost:8888 (central hub)
- **Backend Metrics:** http://localhost:3001/d/notaire-backend
- **PostgreSQL Metrics:** http://localhost:3001/d/notaire-postgres
- **Logs:** http://localhost:3001/explore (select Loki datasource)

## Troubleshooting

### Prometheus can't scrape backend
1. Verify backend running: `curl http://localhost:8080/actuator/health`
2. Check Prometheus targets: http://localhost:9090/targets
3. Verify authentication: `curl -u $ACTUATOR_USER:$ACTUATOR_PASSWORD http://localhost:8080/actuator/prometheus`
4. Check network connectivity: `docker exec devsecops-prometheus ping backend`

### Loki not receiving logs
1. Verify Loki running: `curl http://localhost:3100/ready`
2. Check Promtail logs: `docker logs devsecops-promtail`
3. Verify Promtail config mounts are correct
4. Check log labels in Grafana Explore Loki datasource

### Grafana dashboards not showing
1. Verify datasources: http://localhost:3001/datasources
2. Check provisioning logs: `docker logs devsecops-grafana`
3. Verify dashboard JSON valid in `infra/grafana/provisioning/dashboards/`

### PostgreSQL exporter not collecting metrics
1. Verify exporter running: `curl http://localhost:9187/metrics`
2. Check database connection in exporter environment
3. Verify custom queries in `infra/prometheus/postgres_exporter.yml`

## Alerting Rules

Configured in `infra/prometheus/alert-rules.yml`:

| Alert Name | Condition | Severity |
|------------|-----------|----------|
| BackendDown | backend health probe fails | Critical |
| SuspiciousLoginActivity | bad-credential login rate exceeds 30/min | Warning |
| HighErrorRate | HTTP 5xx rate exceeds threshold | Warning |
| HighMemoryUsage | JVM heap usage exceeds threshold | Warning |
| DatabaseDown | postgres-exporter target down | Critical |

## Code Quality (SonarQube)

```bash
bash infra/scripts/run-sonar.sh
```

Waits for SonarQube, handles the first-login password change, generates an analysis
token, runs the backend test suite with JaCoCo, and submits the analysis. View results
at http://localhost:9000/dashboard?id=notaire-backend.

---

**Related Documents:**
- [Infrastructure README](../../../infra/README.md) — complete infra setup (source of truth for ports/credentials)
- [DevSecOps Pipeline](../208-devsecops/README.md) — CI/CD pipeline documentation
- [Deployment Guide](../209-deployment/README.md) — deployment procedures
- [Security Policy](../206-security/README.md) — security overview
