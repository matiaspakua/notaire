# 🛠️ DevSecOps Infrastructure - Notaire

This directory contains the infrastructure necessary to support the Secure Software Development Life Cycle (S-SDLC) of the Notaire project. It is designed to run in parallel with the application, providing tools for code analysis, security scanning, artifact management, and observability.

## 🧰 Tools Stack

### 🛡️ Security & Quality (DevSecOps)
| Tool | Category | Description | Port |
|------|----------|-------------|------|
| **SonarQube (CE)** | SAST | Static analysis for code quality and security vulnerabilities. | 9000 |
| **Dependency-Track** | SCA | Analysis of third-party library vulnerabilities (Software Bill of Materials - SBOM). | 8083 |
| **DefectDojo** | Vulnerability Mgmt | Aggregates findings from Sonar, Trivy, ZAP, etc., into a single dashboard. | 8084 |
| **OWASP ZAP** | DAST | Dynamic analysis of the running application to find security flaws. | - |
| **Trivy** | Container/FS Scan | Scans Docker images and the filesystem for vulnerabilities. | - |

### 🏗️ CI/CD & Artifacts
| Tool | Category | Description | Port |
|------|----------|-------------|------|
| **Jenkins** | Orchestration | Automates builds, tests, and security scans. | 8082 |
| **Nexus Repository** | Artifacts | Private repository for Maven dependencies and Docker images. | 8081 |

### 📊 Observability (Monitoring & Logging)
| Tool | Category | Description | Port |
|------|----------|-------------|------|
| **Prometheus** | Metrics | Time-series database for service and infrastructure metrics. | 9090 |
| **Grafana** | Visualization | Dashboards for Prometheus metrics and Loki logs. | 3001 |
| **Loki** | Logging | Log aggregation system (similar to ELK but more lightweight). | 3100 |

### 🕹️ Centralized Control Center
| Tool | Category | Description | Port |
|------|----------|-------------|------|
| **Homer Hub** | Landing Page | A single entry point to navigate all DevSecOps services. | 80 |

---

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- Minimum 8GB RAM recommended (this stack is comprehensive)

### Running the Infrastructure
1. **Start the application network** (in the root): `docker-compose up -d`
2. **Start the infra**:
   ```bash
   cd infra
   docker-compose up -d
   ```
3. **Access the Hub**: Open `http://localhost:80` in your browser.

---

## 🛡️ Testing & Validation

To verify that the entire infrastructure is running correctly, use the provided health-check script:
```bash
bash infra/scripts/check-infra.sh
```
This script will ping each service's health endpoint and report its status.

---

## 📊 Reporting System

The infrastructure includes a report generator that aggregates security findings into Markdown and HTML formats.

### Generating a Security Report
Run the following script:
```bash
bash infra/scripts/generate-report.sh
```
The report will include:
- Infrastructure health status.
- Container vulnerability findings (via Trivy).
- SAST summary (SonarQube).
- SCA overview (Dependency-Track).

Reports are saved in `/infra/reports/`.

### Integration with Notaire

#### 1. Static Analysis (SonarQube)
Configure your `pom.xml` or run via Maven:
```bash
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<token>
```

#### 2. Vulnerability Management (SCA & Containers)
- **Dependency-Track**: Access at `http://localhost:8085`. Upload your `bom.xml` (generated via `cyclonedx-maven-plugin`) to track third-party vulnerabilities.
- **Trivy**: Integrated into Jenkins. Use it to scan your Docker images:
  ```bash
  trivy image notary-backend:latest
  ```

#### 3. Dynamic Analysis (DAST)
- **OWASP ZAP**: Can be run as a containerized scan against the running API:
  ```bash
  docker run -t ghcr.io/zaproxy/zaproxy:stable zap-api-scan.py -t http://backend:8080/v3/api-docs -f openapi
  ```

#### 4. Metrics & Monitoring (Prometheus/Grafana)
- **Prometheus**: Access at `http://localhost:9090`. Scrapes Actuator metrics from the backend.
- **Grafana**: Access at `http://localhost:3001` (admin/admin). 
  - Pre-configured with Prometheus and Loki data sources.
  - Recommended: Import the "Spring Boot 2.1 Statistics" dashboard (ID: 6756).

#### 5. Log Aggregation (Loki/Promtail)
- **Loki**: Receives logs from Promtail.
- **Promtail**: Automatically discovers all Docker containers and ships their logs to Loki. In Grafana, use the "Explore" view and select the "Loki" datasource to search logs by container name.

---

## 🛠️ Automated CI Pipeline (Example)

A typical Jenkins pipeline for this project would look like:

1. **Build**: `mvn clean install`
2. **SAST**: `mvn sonar:sonar`
3. **SCA**: `mvn cyclonedx:makeAggregateBom` -> Upload to Dependency-Track
4. **Container Scan**: `trivy image notary-backend:latest`
5. **DAST**: Trigger ZAP API Scan
6. **Deploy**: Push to Nexus and deploy to environment.

#### 6. Frontend Security (Next.js)
- **SAST**: SonarQube automatically scans `.ts`, `.tsx`, and `.js` files.
- **SCA**: Use `npm audit` or Trivy to scan the `frontend` directory:
  ```bash
  trivy fs ./frontend
  ```
- **Secret Scanning**: Use **Trufflehog** or **Gitleaks** (not included in compose but recommended) to ensure no secrets are committed in the frontend environment variables.

---

## 📐 Architecture Diagram (Simplified)

```
[ Developer ] --push--> [ GitHub / Gitea ]
                             |
                      [ Jenkins Pipeline ]
                             |
        +--------------------+--------------------+
        |                    |                    |
 [ SonarQube ]        [ Trivy Scan ]      [ Dependency-Track ]
 (SAST/Quality)       (Container/SCA)          (SCA/SBOM)
        |                    |                    |
        +----------+---------+----------+---------+
                   |                    |
            [ Deploy Hub ]       [ Nexus Repo ]
           (Jenkins/Git)        (Artifacts/Images)
                   |
            [ Deployment ]
                   |
        +----------+----------+
        |                     |
 [ Prometheus ] <--- [ Notaire App ] ---> [ Loki ]
   (Metrics)           (Services)         (Logs)
        |                     |             |
        +----------+----------+-------------+
                   |
              [ Grafana ]
             (Dashboards)
```

---

## ⚠️ Important Note on Networking

The infrastructure connects to the `notary-network` (used by the main application) as an **external** network. 
1. Start the main application first: `docker-compose up -d` (in the root).
2. Start the infra: `docker-compose up -d` (in /infra).

If you want to run the infra independently, you can create the network manually:
```bash
docker network create notary-network
```
