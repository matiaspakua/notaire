# 🛠️ Observability & Quality Infrastructure — Notaire

This directory contains the infrastructure that runs **alongside** the Notaire
application, providing **observability** (metrics, logs, dashboards) and
**code quality** analysis. Every service here is wired to the running Notaire
stack and shows real data.

> **Credentials live in a single, git-ignored `.env` file at the repo root.**
> Copy `.env.example` → `.env` and adjust if needed. Nothing here hard-codes
> secrets.

## 🧰 Tools Stack

### 📊 Observability (Monitoring & Logging)
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **Prometheus** | Metrics | Scrapes backend (Actuator/Micrometer) + Postgres metrics. | 9090 | – |
| **Grafana** | Visualization | Dashboards for Prometheus metrics and Loki logs. | 3001 | `$GRAFANA_ADMIN_USER` / `$GRAFANA_ADMIN_PASSWORD` |
| **Loki** | Logging | Aggregates structured JSON logs from all containers. | 3100 | – |
| **Promtail** | Log shipper | Discovers Docker containers and ships logs to Loki. | – | – |
| **PostgreSQL Exporter** | DB Metrics | Exposes Notaire DB metrics for Prometheus. | 9187 | – |

### 🛡️ Security & Quality
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **SonarQube (CE)** | SAST | Static analysis of the backend (bugs, smells, coverage). | 9000 | `$SONAR_ADMIN_USER` / `$SONAR_ADMIN_PASSWORD` |

### 🕹️ Centralized Control Center
| Tool | Category | Description | Port | Credentials |
|------|----------|-------------|------|-------------|
| **Homer Hub** | Landing Page | A single entry point to navigate every service. | 8888 | – |

> **Removed (2026-06):** Jenkins, Nexus and Dependency-Track were removed —
> they were not wired to the running application and are out of scope for the
> observability + quality goal. Re-add them to `docker-compose.yml` if needed.

## 📋 What is Monitored

| Service | Type | Monitoring Method | Prometheus Job |
|---------|------|-------------------|----------------|
| **Backend API** | Spring Boot | Actuator / Micrometer (`/actuator/prometheus`, Basic auth) | `notaire-backend` |
| **PostgreSQL** | Database | postgres-exporter | `notaire-postgres` |
| **Grafana** | Self | `/metrics` | `grafana` |
| **Loki** | Self | `/metrics` | `loki` |

**Business audit trail** (create / update / delete operations and logins) is
recorded by the backend `AuditoriaAspect` into the `registro_auditoria` table
and surfaced in the application UI at **`/dashboard/auditoria`**. The acting
user is attributed from the authenticated JWT identity, not from a
client-supplied header.

---

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- ~6 GB RAM (SonarQube needs ~2 GB)
- A `.env` file at the repo root (`cp .env.example .env`)

### Start everything (application + infrastructure)
```bash
bash scripts/start-all.sh           # app, then infra
```

### Or start them independently
```bash
bash scripts/start.sh               # 1) application (creates the app network)
bash infra/scripts/start-infra.sh         # 2) observability + SonarQube
```
The infra stack attaches to the application's Docker network
(`notaire_notary-network`, declared `external`), so the application must be up
first.

### Access the Hub
Open **http://localhost:8888**.

---

## 🛡️ Health Check
```bash
bash infra/scripts/check-infra.sh
```
Checks Homer, SonarQube, Prometheus, Grafana, Loki, Promtail, PostgreSQL
Exporter, and all Notaire application endpoints.

### Quick checks
```bash
curl http://localhost:8080/actuator/health                 # backend health
curl -u admin:admin http://localhost:8080/actuator/prometheus | head   # backend metrics
curl http://localhost:9090/api/v1/targets                  # prometheus targets (all UP)
curl http://localhost:3001/api/health                      # grafana
curl http://localhost:3100/ready                           # loki
curl http://localhost:9187/metrics | head                  # postgres exporter
```

---

## 📊 Dashboards (pre-provisioned in Grafana)

| Dashboard | UID | Description |
|-----------|-----|-------------|
| **Notaire Backend API** | `notaire-backend` | JVM, HTTP, DB pool, log volume |
| **Notaire PostgreSQL** | `notaire-postgres` | Connections, transactions, size |
| **Notaire Logs** | `notaire-logs` | Backend & frontend logs from Loki |

Data sources (auto-provisioned): **Prometheus** (`http://prometheus:9090`),
**Loki** (`http://loki:3100`).

---

## 📝 Logging

The backend logs structured JSON (Logback `LogstashEncoder`) to stdout. Promtail
ships every container's logs to Loki. Query in Grafana → Explore → Loki:

```
{container_name="notary-backend"} | json
{service="notaire"}
```
Available labels: `container`, `container_name`, `service`, `app`, `level`.

---

## 🔍 Code Quality (SonarQube)

```bash
bash infra/scripts/run-sonar.sh
```
This waits for SonarQube, handles the first-login password change, generates an
analysis token (saved to `.env` as `SONAR_TOKEN`), runs the backend test suite
with JaCoCo, and submits the analysis. View results at
**http://localhost:9000/dashboard?id=notaire-backend**.

---

## 📊 Reporting
```bash
bash infra/scripts/generate-report.sh   # Markdown + HTML in infra/reports/
```

---

## ⚠️ Networking

The infra connects to `notaire_notary-network` as an **external** network.
Start the application first so the network exists. `infra/scripts/start-infra.sh`
verifies this and fails fast with guidance if it is missing.

## 📖 Additional Documentation
- [Monitoring Guide](../docs/04-operations/04-monitoring/README.md)
- [Operations README](../docs/04-operations/README.md)
- Credentials reference: [`CREDENTIALS.md`](CREDENTIALS.md)
