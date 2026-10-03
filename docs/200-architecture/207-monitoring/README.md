# Monitoring & Observability - Notaire Project

## Overview

This document describes the monitoring and observability infrastructure for the Notaire
system. The backend API and PostgreSQL database are monitored through a unified
observability stack: **Prometheus**, **Grafana**, **Loki**, **Promtail**, and
**postgres-exporter**, fronted by a **Homer** landing page. The stack is defined in
`infra/observability/docker-compose.yml`; see [`infra/README.md`](../../../infra/README.md) for the
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
┌───────────────────────┐                            │
│   Grafana :3001       │◄────────────────────────────┘
│   Dashboards:         │
│   - notaire-backend   │        ┌──────────────────────┐
│   - notaire-postgres  │◄───────┤   Loki :3100          │
│   - notaire-logs      │        │   (log storage)       │
│   - notaire-auth      │        └───────────┬────────────┘
└───────────────────────┘                    ▲
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
- **Configuration:** `infra/observability/prometheus/postgres_exporter.yml`
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
**Promtail Configuration:** `infra/observability/loki/promtail-config.yaml`

**Scraped Containers:** all `notaire-*`, `notary-*`, and `devsecops-*` containers.

**Log Labels:** `container`, `container_name`, `service`, `app`, `level`.

**Grafana Dashboard:** `notaire-logs` (pre-provisioned) — query in Grafana → Explore →
Loki datasource: `{container_name="notary-backend"} | json`.

### 4. Auth & Security Monitoring

**Metrics Source:** the same backend Micrometer registry as section 1 (custom login
counters), plus Loki for raw log lines.

**Grafana Dashboard:** `notaire-auth` (pre-provisioned)
- Login attempts — total rate
- Successful logins
- Failed logins (bad credentials)
- Inactive user attempts
- Login outcomes over time
- Login failure rate (% of attempts)
- All operations (login by status)
- Recent login errors (from logs)

Backs the `HighLoginFailureRate` and `SuspiciousLoginActivity` alerts (see
[Alerting Rules](#alerting-rules) below).

## Operating the stack

Setup, credentials, startup, health checks, dashboards and troubleshooting are
maintained with the infrastructure code, not duplicated here:

- [Preparation](../../../infra/docs/PREPARATION.md) · [Configuration](../../../infra/docs/CONFIGURATION.md)
  · [Operation](../../../infra/docs/OPERATION.md)
- Backend API authentication: [API Authentication Guide](../206-security/API-AUTHENTICATION-GUIDE.md)

## Alerting Rules and Code Quality

Alert rules (`HighLoginFailureRate`, `SuspiciousLoginActivity`, `BackendDown`,
`HighJvmHeapUsage`) are defined in `infra/observability/prometheus/alert-rules.yml`
and tabulated in [infra DEFINITION](../../../infra/docs/DEFINITION.md#alert-rules-prometheusalert-rulesyml).
SonarQube analysis is run as described in [infra OPERATION](../../../infra/docs/OPERATION.md#code-quality-sonarqube).

---

**Related Documents:**
- [Infrastructure README](../../../infra/README.md) — complete infra setup (source of truth for ports/credentials)
- [DevSecOps Pipeline](../208-devsecops/README.md) — CI/CD pipeline documentation
- [Deployment Guide](../209-deployment/README.md) — deployment procedures
- [Security Policy](../206-security/README.md) — security overview
