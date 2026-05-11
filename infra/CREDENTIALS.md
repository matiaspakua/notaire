# Notaire DevSecOps — Access Credentials

> Generated: 2026-05-11
> Defaults for all services: **admin / admin** (unless noted otherwise)

---

## Notaire Application

| Service | URL | Username | Password | Notes |
|---------|-----|----------|----------|-------|
| **Backend API** | http://localhost:8080 | — | — | No auth (dev mode). Swagger: http://localhost:8080/swagger-ui.html |
| **Frontend** | http://localhost:3000 | — | — | Next.js web app, no auth |
| **PostgreSQL** | localhost:5432 | `admin` | `admin` | Database: `notaire` |
| **pgAdmin** | http://localhost:5050 | `admin@notaire.com` | `admin` | PostgreSQL web admin |

---

## Security & Quality

| Service | URL | Username | Password | Notes |
|---------|-----|----------|----------|-------|
| **SonarQube** | http://localhost:9000 | `admin` | `Admin@123456` | Forces password change on first login |
| **Dependency-Track** (API) | http://localhost:8083 | `admin` | Check logs | Register first user via web UI at http://localhost:8085 |
| **Dependency-Track** (Frontend) | http://localhost:8085 | — | — | Web UI for Dependency-Track |

---

## CI/CD & Artifacts

| Service | URL | Username | Password | Notes |
|---------|-----|----------|----------|-------|
| **Jenkins** | http://localhost:8082 | `admin` | `admin` | Configured via JCasC |
| **Nexus** | http://localhost:8081 | `admin` | `admin123` | Sonatype Nexus Repository |

---

## Observability

| Service | URL | Username | Password | Notes |
|---------|-----|----------|----------|-------|
| **Grafana** | http://localhost:3001 | `admin` | `admin` | Dashboards & metrics |
| **Prometheus** | http://localhost:9090 | — | — | No auth (metrics scraper) |
| **Loki** | http://localhost:3100 | — | — | No auth (log aggregation) |
| **Homer Dashboard** | http://localhost:80 | — | — | Centralized dashboard (no auth) |

---

## Environment Reference

| Variable | Value |
|----------|-------|
| `POSTGRES_DB` | notaire |
| `POSTGRES_USER` | admin |
| `POSTGRES_PASSWORD` | admin |
| `NOTAIRE_DB_URL` | jdbc:postgresql://postgres:5432/notaire |
| `BACKEND_API_URL` | http://backend:8080 |
| `SONAR_HOST_URL` | http://sonarqube:9000 |
| `NEXUS_URL` | http://nexus:8081 |
| `PROMETHEUS_URL` | http://prometheus:9090 |
| `GRAFANA_URL` | http://grafana:3000 |
| `LOKI_URL` | http://loki:3100 |

---

## Docker Networks

| Network | Services |
|---------|----------|
| `notaire_notary-network` | postgres, backend, frontend, pgadmin |
| `infra_devsecops-network` | sonarqube, jenkins, nexus, grafana, prometheus, loki, dtrack |

---

## Quick Start

```bash
# Access dashboard (start here)
open http://localhost:80

# Check all services status
docker ps

# View logs
docker logs devsecops-sonarqube   # SonarQube
docker logs devsecops-jenkins     # Jenkins
docker logs devsecops-dtrack-api  # Dependency-Track
```
