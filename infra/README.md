# 🛠️ DevSecOps Infrastructure - Notaire

This directory contains the infrastructure necessary to support the Secure Software Development Life Cycle (S-SDLC) of the Notaire project. It is designed to run in parallel with the application, providing tools for code analysis, security scanning, artifact management, and observability.

**All services are configured with default credentials: `admin` / `admin`** (except Nexus which uses `admin` / `admin123`).

## 🧰 Tools Stack

### 🛡️ Security & Quality (DevSecOps)
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **SonarQube (CE)** | SAST | Static analysis for code quality and security vulnerabilities. | 9000 | admin/admin |
| **Dependency-Track** | SCA | Analysis of third-party library vulnerabilities (SBOM). | 8085 | - |
| **DefectDojo** | Vulnerability Mgmt | Aggregates findings from various tools into a single dashboard. | 8084 | - |
| **OWASP ZAP** | DAST | Dynamic analysis of the running application. | - | - |
| **Trivy** | Container/FS Scan | Scans Docker images and the filesystem for vulnerabilities. | - | - |

### 🏗️ CI/CD & Artifacts
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **Jenkins** | Orchestration | Automates builds, tests, and security scans. | 8082 | admin/admin |
| **Nexus Repository** | Artifacts | Private repository for Maven dependencies and Docker images. | 8081 | admin/admin123 |

### 📊 Observability (Monitoring & Logging)
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **Prometheus** | Metrics | Time-series database for service and infrastructure metrics. | 9090 | - |
| **Grafana** | Visualization | Dashboards for Prometheus metrics and Loki logs. | 3001 | admin/admin |
| **Loki** | Logging | Log aggregation system (similar to ELK but more lightweight). | 3100 | - |

### 🗄️ Database Monitoring
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **PostgreSQL Exporter** | DB Metrics | PostgreSQL metrics collector for Prometheus. | 9187 | - |

### 🕹️ Centralized Control Center
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **Homer Hub** | Landing Page | A single entry point to navigate all DevSecOps services. | 80 | - |

## 📋 Monitored Services

The infrastructure monitors the following Notaire services:

| Service | Type | Monitoring Method | Prometheus Job |
|---------|------|-------------------|----------------|
| **Backend API** | Spring Boot | Actuator / Micrometer | `notaire-backend` |
| **PostgreSQL** | Database | postgres-exporter | `notaire-postgres` |
| **notaire-shared** | Shared Module | Micrometer (via Backend) | `notaire-shared` |

---

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- Minimum 8GB RAM recommended (this stack is comprehensive)
- The Notaire application must be running first (see root `docker-compose.yml`)

### Starting the Infrastructure

1. **Start the application network** (in the root):
   ```bash
   cd /path/to/notaire
   docker-compose up -d
   ```

2. **Start the infra**:
   ```bash
   cd /path/to/notaire/infra
   docker-compose up -d
   ```

3. **Access the Hub**: Open `http://localhost:80` in your browser.

### Starting Order
The infrastructure is designed to connect to the existing `notary-network`. Start services in this order:

1. Notaire application (PostgreSQL, Backend, pgAdmin)
2. Infrastructure (Prometheus, Grafana, Loki, Jenkins, etc.)

---

## 🛡️ Testing & Validation

### Full Health Check
To verify that all Notaire services and infrastructure are running correctly:

```bash
bash infra/scripts/check-infra.sh
```

This script checks:
- **DevSecOps**: SonarQube, Jenkins, Nexus, Dependency-Track
- **Monitoring**: Prometheus, Grafana, Loki, PostgreSQL Exporter
- **Notaire**: Backend API health, liveness, readiness, metrics, Swagger, pgAdmin, PostgreSQL

### Quick Service Checks

```bash
# Backend API health
curl http://localhost:8080/actuator/health

# Prometheus targets (should show all services as UP)
curl http://localhost:9090/api/v1/targets

# Grafana health
curl http://localhost:3001/api/health

# PostgreSQL exporter metrics
curl http://localhost:9187/metrics | head -20

# Loki readiness
curl http://localhost:3100/ready
```

---

## 📊 Dashboards

Grafana is pre-provisioned with the following dashboards:

| Dashboard | UID | Description |
|-----------|-----|-------------|
| **Notaire Backend API** | `notaire-backend` | Spring Boot metrics (JVM, HTTP, DB pool, logs) |
| **Notaire PostgreSQL** | `notaire-postgres` | Database metrics (connections, transactions, size, business entities) |

Access at: http://localhost:3001 (login: admin/admin)

### Data Sources (Pre-configured)
- **Prometheus**: http://prometheus:9090 (default)
- **Loki**: http://loki:3100

---

## 📝 Logging

Loki aggregates logs from all Docker containers. Use Grafana Explore with the Loki datasource:

1. Open http://localhost:3001/explore
2. Select "Loki" datasource
3. Query logs by label: `{container="notary-backend"}`
4. Available labels: `container`, `service`, `app`, `level`

---

## 🔐 Credentials Quick Reference

| Service | URL | Username | Password |
|---------|-----|----------|----------|
| Grafana | http://localhost:3001 | `admin` | `admin` |
| Jenkins | http://localhost:8082 | `admin` | `admin` |
| SonarQube | http://localhost:9000 | `admin` | `admin` |
| Nexus | http://localhost:8081 | `admin` | `admin123` |
| Backend (Actuator) | http://localhost:8080 | `admin` | `admin` |
| PostgreSQL | localhost:5432 | `admin` | `admin` |
| pgAdmin | http://localhost:5050 | `admin@notaire.com` | `admin` |

---

## 📊 Reporting System

The infrastructure includes a report generator that aggregates security findings into Markdown and HTML formats.

### Generating a Security Report
```bash
bash infra/scripts/generate-report.sh
```

The report will include:
- Infrastructure health status
- Container vulnerability findings (via Trivy)
- SAST summary (SonarQube)
- SCA overview (Dependency-Track)

Reports are saved in `/infra/reports/`.

---

## 🔗 Integration with Notaire

### 1. Static Analysis (SonarQube)
```bash
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<token>
```

### 2. Vulnerability Management (SCA & Containers)
- **Dependency-Track**: Access at `http://localhost:8085`. Upload your `bom.xml` (generated via `cyclonedx-maven-plugin`).
- **Trivy**: Scan Docker images:
  ```bash
  trivy image notary-backend:latest
  ```

### 3. Dynamic Analysis (DAST)
```bash
docker run -t ghcr.io/zaproxy/zaproxy:stable zap-api-scan.py \
  -t http://backend:8080/v3/api-docs -f openapi
```

### 4. Metrics & Monitoring (Prometheus/Grafana)
- **Prometheus**: http://localhost:9090 — Scrapes Actuator metrics from the backend every 10s.
- **Grafana**: http://localhost:3001 (`admin/admin`) — Pre-configured with Prometheus and Loki.
- **Dashboards**: Backend API and PostgreSQL dashboards pre-installed.

### 5. Log Aggregation (Loki/Promtail)
- **Loki**: Receives logs from Promtail at http://loki:3100.
- **Promtail**: Discovers all Docker containers and ships their logs to Loki.
- **Grafana Explore**: Query logs by container: `{container="notary-backend"}`

---

## 🛠️ Automated CI Pipeline (Example)

A typical Jenkins pipeline for this project:

1. **Build**: `mvn clean install -pl backend-api -am`
2. **SAST**: `mvn sonar:sonar`
3. **SCA**: `mvn cyclonedx:makeAggregateBom` → Upload to Dependency-Track
4. **Container Scan**: `trivy image notary-backend:latest`
5. **DAST**: Trigger ZAP API Scan
6. **Deploy**: Push to Nexus and deploy to environment

---

## ⚠️ Important Note on Networking

The infrastructure connects to the `notary-network` (used by the main application) as an **external** network.

1. Start the main application first: `docker-compose up -d` (in the root).
2. Start the infra: `docker-compose up -d` (in `/infra`).

If you want to run the infra independently, create the network manually:
```bash
docker network create notary-network
```

## 📖 Additional Documentation

For detailed monitoring setup and usage, see:
- [Monitoring Guide](../docs/04-operations/04-monitoring/README.md)
- [Operations README](../docs/04-operations/README.md)
- [DevSecOps Pipeline](../docs/04-operations/01-devsecops/README.md)
