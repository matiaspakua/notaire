# Deployment Guide - Notaire Project

## Overview

This guide covers how to deploy the complete Notaire system: the application stack and the
observability/quality infrastructure stack.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Notaire Stack                               │
│                     (docker-compose.yml - root)                      │
│                                                                        │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐          │
│  │PostgreSQL│   │ Backend  │   │ Frontend │   │ pgAdmin  │          │
│  │  :5432   │   │  :8080   │   │  :3000   │   │  :5050   │          │
│  └──────────┘   └──────────┘   └──────────┘   └──────────┘          │
└────────────────────────────┬───────────────────────────────────────┘
                              │ (shared network: notary-network)
┌────────────────────────────┴───────────────────────────────────────┐
│                          Infra Stack                                 │
│                    (infra/docker-compose.yml)                        │
│                                                                        │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐          │
│  │Prometheus│   │ Grafana  │   │   Loki   │   │ Promtail │          │
│  │  :9090   │   │  :3001   │   │  :3100   │   │          │          │
│  └──────────┘   └──────────┘   └──────────┘   └──────────┘          │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐                         │
│  │SonarQube │   │postgres- │   │  Homer   │                         │
│  │  :9000   │   │exporter  │   │  :8888   │                         │
│  └──────────┘   │  :9187   │   └──────────┘                         │
│                  └──────────┘                                        │
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

### 2. Start the Application Stack

```bash
bash scripts/start.sh
# or, from repo root:
docker-compose up -d

# Verify services are running
docker-compose ps
```

This starts:
- **PostgreSQL 16** on port 5432
- **Backend API** on port 8080
- **Frontend (Next.js)** on port 3000
- **pgAdmin** on port 5050 (started by default; skip with `bash scripts/start.sh --no-admin`)

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

### Root docker-compose.yml
- **Services**: `postgres`, `backend`, `frontend`, `pgadmin`
- **Network**: `notary-network` (bridge)
- **Volumes**: `postgres_data`, `pgadmin_data`
- **Backend health check**: `/actuator/health`
- **Environment variables**: Configured via `.env` file (see `.env.example`)

### Infra docker-compose.yml
- **Services**: `dashboard` (Homer), `sonarqube`, `sonar-db`, `prometheus`, `postgres-exporter`,
  `grafana`, `loki`, `promtail`
- **Networks**: `devsecops-network` + `notaire-app-network` (external, joins the app stack)
- **Volumes**: All service data persisted
- **Configuration files**: Pre-configured in `infra/<service>/` subdirectories

## Environment Configuration

All credentials live in a single, git-ignored `.env` file at the repo root (copy `.env.example`).
Both `docker-compose.yml` and `infra/docker-compose.yml` read from it — never hard-code secrets
in compose files or docs.

Key variables include `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`,
`ACTUATOR_USER`/`ACTUATOR_PASSWORD` (Prometheus scrape auth), and `NEXT_PUBLIC_API_URL`
(frontend build arg).

## Production Considerations

For production deployment, ensure:

1. **Change default credentials** — do not reuse `.env.example` values
2. **Enable HTTPS** — terminate TLS in front of the backend and frontend (e.g. reverse proxy)
3. **Set a strong `JWT_SECRET`** — see [API Authentication Guide](../206-security/API-AUTHENTICATION-GUIDE.md)
4. **Database backups** — configure periodic `pg_dump` backups
5. **Resource limits** — set Docker resource constraints
6. **Log rotation** — configure Docker log rotation
7. **Monitoring alerts** — configure Prometheus alerting rules (`infra/prometheus/alert-rules.yml`)

## Rollback Procedure

```bash
# Stop all services
docker-compose down

# Remove specific volumes if needed
docker-compose down -v

# Restore database from backup
docker exec -i notary-postgres psql -U admin notaire < backup.sql

# Restart previous version
docker-compose up -d
```

## Related Documentation

- [Monitoring Guide](../207-monitoring/README.md)
- [DevSecOps Pipeline](../208-devsecops/README.md)
- [Architecture Overview](../README.md)
