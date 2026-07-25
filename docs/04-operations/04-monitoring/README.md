# Monitoring & Observability - Notaire Project

> ℹ️ **As of 2026-06, the live source of truth for the infra stack is
> [`infra/README.md`](../../../infra/README.md).** The stack was slimmed to
> **Observability (Prometheus, Grafana, Loki, Promtail, postgres-exporter,
> Homer) + SonarQube**; Jenkins, Nexus and Dependency-Track were removed. All
> credentials live in the git-ignored root `.env`. Start everything with
> `bash scripts/start-all.sh`. Some legacy references below may mention removed
> tools.

## Overview

This document describes the complete monitoring and observability infrastructure for the Notaire system. The entire system (backend API, PostgreSQL database, notaire-shared module) is monitored through a unified observability stack consisting of **Prometheus**, **Grafana**, **Loki**, and **Grafana Dashboard**.

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Notaire Services                           │
├───────────────────┬────────────────────┬─────────────────────┤
│   Backend API     │   PostgreSQL DB    │  notaire-shared DTO │
│   (Spring Boot)   │   (PostgreSQL 16)  │  (shared module)    │
│   :8080           │   :5432            │  (via backend)      │
├────────┬──────────┴─────────┬──────────┴──────────┬──────────┤
│        │                    │                      │          │
│   Actuator Metrics    pg_stat_queries          DTO Metrics    │
│   /actuator/prometheus│                    (Micrometer)       │
│        │                    │                      │          │
└────────┼────────────────────┼──────────────────────┼──────────┘
         │                    │                      │
         ▼                    ▼                      ▼
┌──────────────────────────────────────────────────────────────┐
│                     Prometheus :9090                          │
│              (Metrics Collection & Storage)                   │
├───────────┬────────────────────┬─────────────────────────────┤
│           │                    │                              │
│   notaire-backend        postgres-exporter           prometheus │
│   job                    job :9187                   self     │
│   /actuator/prometheus   queries.yml                          │
└───────────┼────────────────────┼──────────────────────────────┘
            │                    │
            ▼                    ▼
┌──────────────────────────────────────────────────────────────┐
│                       Grafana :3001                          │
│          (Dashboards & Visualizations)                       │
│                                                              │
│   ┌─────────────────┐  ┌─────────────────────┐              │
│   │ Backend Dashboard│  │ PostgreSQL Dashboard │              │
│   │ (notaire-backend)│  │ (notaire-postgres)  │              │
│   └─────────────────┘  └─────────────────────┘              │
│   ┌─────────────────────────────────────────┐               │
│   │          Loki Datasource                 │               │
│   │     (Container Logs)                     │               │
│   └─────────────────────────────────────────┘               │
└──────────────────────────────────────────────────────────────┘
                         ▲
                         │
┌──────────────────────────────────────────────────────────────┐
│                     Loki :3100                                │
│          (Log Aggregation & Storage)                          │
│                         ▲                                     │
│                         │                                     │
│                    Promtail                                    │
│          (Log Collector - Docker)                              │
└──────────────────────────────────────────────────────────────┘
                         ▲
                         │
              ┌──────────┴──────────┐
              │                     │
       notary-backend         notary-postgres
       container logs         container logs
```

## Services Overview

### 1. Backend API Monitoring

**Metrics Collection:** Spring Boot Actuator with Micrometer
- **Endpoint:** `http://localhost:8080/actuator/prometheus`
- **Auth:** Basic Auth (`admin` / `admin`)
- **Scrape Interval:** Every 10 seconds

**Metrics Exposed:**
- JVM metrics (memory, GC, threads, classes)
- HTTP request metrics (rate, duration, errors)
- Database connection pool (HikariCP)
- Logback event rate
- Custom business metrics (operation counts, durations, API errors)
- Cache hit/miss ratios
- Notaire-shared DTO metrics

**Grafana Dashboard:** `notaire-backend` (pre-provisioned)
- API Request Rate & Response Time (P95)
- JVM Memory Usage & Garbage Collection
- Active Threads & Database Connection Pool
- HTTP Error Rate & Application Health
- Uptime & CPU Usage
- Log Rate by Level
- Custom Business Metrics

### 2. PostgreSQL Database Monitoring

**Metrics Collection:** postgres-exporter
- **Endpoint:** `http://localhost:9187/metrics`
- **Configuration:** `infra/prometheus/postgres_exporter.yml`
- **Connection:** `postgresql://<POSTGRES_EXPORTER_USER>:<POSTGRES_EXPORTER_PASSWORD>@postgres:5432/notaire`
  — connects as a dedicated least-privilege role (granted only `pg_monitor`, created by Flyway
  migration `V12`), **not** the application's own admin datasource credentials (issue #675).
  Set `POSTGRES_EXPORTER_USER`/`POSTGRES_EXPORTER_PASSWORD` in `.env`.

**Metrics Exposed:**
- Database size and connection count
- Transaction commit/rollback rates
- Cache hit ratio
- Block I/O rates
- **Custom business queries:**
  - Total escrituras by status
  - Total presupuestos
  - Total usuarios and personas
  - Connection count by state

**Grafana Dashboard:** `notaire-postgres` (pre-provisioned)
- Database Size & Active Connections
- Cache Hit Ratio
- Transaction Rates
- Connections by State
- Business Entity Counts
- Block I/O Rates

### 3. notaire-shared Module Monitoring

**Metrics Collection:** Via backend Micrometer registry
- **Endpoint:** `http://localhost:8080/actuator/prometheus` (same as backend)
- **Prefix:** `notaire_shared_*`

**Metrics Exposed:**
- DTO serialization count and duration
- DTO deserialization count and duration
- DTO validation count and duration
- Serialization/validation error counts
- Active DTO gauge
- Shared module version

### 4. Log Aggregation (Loki + Promtail)

**Loki Endpoint:** `http://localhost:3100`
**Promtail Configuration:** `infra/loki/promtail-config.yaml`

**Scraped Containers:**
- All `notaire-*` and `notary-*` containers
- All `devsecops-*` containers
- System logs from `/var/log/`

**Log Labels:**
- `container` - Container name
- `service` - Service identifier
- `app` - Application name
- `level` - Log level (for JSON logs)

### 5. CI/CD Pipeline (Jenkins)

**Jenkins URL:** `http://localhost:8082`
**Credentials:** `admin` / `admin`
**Configuration:** `infra/jenkins/jenkins.yaml` (JCasC)

**Pre-configured Pipeline:** `notaire-ci-pipeline`
- Build (`mvn clean install`)
- SAST (`mvn sonar:sonar`)
- SCA (CycloneDX SBOM upload)
- Container scan (Trivy)
- Docker build and publish

## Access Credentials

| Service | URL | Username | Password |
|---------|-----|----------|----------|
| **Grafana** | http://localhost:3001 | `admin` | `admin` |
| **Prometheus** | http://localhost:9090 | - | - |
| **Loki** | http://localhost:3100 | - | - |
| **Jenkins** | http://localhost:8082 | `admin` | `admin` |
| **SonarQube** | http://localhost:9000 | `admin` | `admin` |
| **Nexus** | http://localhost:8081 | `admin` | `admin123` |
| **pgAdmin** | http://localhost:5050 | `admin@notaire.com` | `admin` |
| **Backend API** | http://localhost:8080 | `admin` (actuator) | `admin` (actuator) |
| **PostgreSQL** | localhost:5432 | `admin` | `admin` |
| **Dashboard** | http://localhost:80 | - | - |

## Running the Complete Stack

### 1. Start Notaire Services
```bash
# Start the application services (PostgreSQL, Backend, Frontend)
cd /path/to/notaire
docker-compose up -d
```

### 2. Start Infrastructure Services
```bash
# Start monitoring and DevSecOps infrastructure
cd /path/to/notaire/infra
docker-compose up -d
```

### 3. Verify Everything is Running
```bash
# Run the comprehensive health check
bash infra/scripts/check-infra.sh
```

### 4. Access Dashboards
- **Homer Dashboard:** http://localhost:80 (central hub)
- **Backend Metrics:** http://localhost:3001/d/notaire-backend
- **PostgreSQL Metrics:** http://localhost:3001/d/notaire-postgres
- **Logs:** http://localhost:3001/explore (select Loki datasource)

## Troubleshooting

### Prometheus can't scrape backend
1. Verify backend is running: `curl http://localhost:8080/actuator/health`
2. Check Prometheus targets: http://localhost:9090/targets
3. Verify authentication: `curl -u admin:admin http://localhost:8080/actuator/prometheus`
4. Check network connectivity: `docker exec devsecops-prometheus ping backend`

### Loki not receiving logs
1. Verify Loki is running: `curl http://localhost:3100/ready`
2. Check Promtail logs: `docker logs devsecops-promtail`
3. Verify Promtail config mounts are correct
4. Check log labels in Grafana Explore with Loki datasource

### Grafana dashboards not showing
1. Verify datasources: http://localhost:3001/datasources
2. Check provisioning logs: `docker logs devsecops-grafana`
3. Verify dashboard JSON is valid in `infra/grafana/provisioning/dashboards/`

### PostgreSQL exporter not collecting metrics
1. Verify exporter is running: `curl http://localhost:9187/metrics`
2. Check database connection in exporter environment
3. Verify custom queries in `infra/prometheus/postgres_exporter.yml`

## Alerting Rules (Future)

The following alerting rules should be configured in Prometheus Alertmanager:

| Alert Name | Condition | Severity |
|------------|-----------|----------|
| BackendDown | `probe_success{job="notaire-backend"} == 0` | Critical |
| HighErrorRate | `rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1` | Warning |
| HighMemoryUsage | `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9` | Warning |
| DatabaseDown | `pg_up == 0` | Critical |
| HighDbConnections | `pg_connections_total > 100` | Warning |
| LowCacheHitRatio | `cache_hit_ratio < 0.95` | Warning |

## Integration with DevSecOps

### SonarQube Integration
```bash
# Run SonarQube analysis
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<token>
```

### Dependency-Track Integration
```bash
# Generate SBOM and upload
mvn cyclonedx:makeAggregateBom
# Upload bom.xml to Dependency-Track at http://localhost:8085
```

### Trivy Container Scanning
```bash
# Scan Docker images
trivy image notary-backend:latest
trivy image notary-postgres:latest

# Scan filesystem
trivy fs .
```

---

**Related Documents:**
- [Infrastructure README](../../../infra/README.md) - Complete infra setup
- [DevSecOps Pipeline](../01-devsecops/README.md) - CI/CD pipeline documentation
- [Deployment Guide](../02-deployment/README.md) - Deployment procedures
- [Operations README](../README.md) - Operations overview
