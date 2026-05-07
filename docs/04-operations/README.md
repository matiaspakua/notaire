# Operations Documentation - Notaire Project

Documentación para operación, deployment, y mantenimiento del sistema Notaire.

## 📋 Contents

### [01. DevSecOps Pipeline](01-devsecops/)
**CI/CD Pipeline y automatización**

- GitHub Actions workflow
- Build automation
- Testing automation
- Security scanning (Trivy)
- Deployment stages

### [02. Deployment Guide](02-deployment/)
**Cómo deployar el sistema**

- Docker container builds
- Docker Compose for local development (application + monitoring)
- Database migrations
- Configuration management
- Network setup (notary-network + devsecops-network)

### [03. Security](03-security/)
**Estándares de seguridad y compliance**

- Authentication & Authorization
- Data protection
- API security
- Dependency vulnerabilities
- OWASP compliance

### [04. Monitoring](04-monitoring/)
**Observabilidad y alertas (NUEVO - Mayo 2026)**

- **Complete monitoring stack**: Prometheus + Grafana + Loki
- **Backend API monitoring**: JVM, HTTP, HikariCP, custom metrics
- **PostgreSQL monitoring**: Connections, transactions, cache, business entities
- **notaire-shared monitoring**: DTO serialization, validation, errors
- **Log aggregation**: All container logs shipped to Loki
- **Pre-configured dashboards**: notaire-backend, notaire-postgres
- **All credentials**: admin/admin for all services
- **Health check script**: `bash infra/scripts/check-infra.sh`

## 🎯 Quick Start

### Deploy Full Stack (Application + Monitoring)
```bash
# 1. Start the application
docker-compose up -d

# 2. Start the monitoring infrastructure
cd infra && docker-compose up -d

# 3. Verify everything is running
bash infra/scripts/check-infra.sh
```

### Deploy to Local Development
```bash
bash scripts/start.sh
bash scripts/logs.sh
```

### Check System Health
```bash
# Application health
curl http://localhost:8080/actuator/health

# Infrastructure health
bash infra/scripts/check-infra.sh
```

### View Logs
```bash
bash scripts/logs.sh
docker logs notaire-backend
docker logs notaire-postgres
```

### Access Monitoring Dashboards
```bash
# Grafana (pre-configured dashboards)
open http://localhost:3001  # admin/admin

# Prometheus targets
open http://localhost:9090/targets

# Central Hub
open http://localhost:80
```

## 📊 System Requirements

| Component | Requirement |
|-----------|------------|
| **Java** | 21 LTS |
| **RAM** | 4GB minimum (2GB app + 2GB infra) |
| **Disk** | 20GB free space |
| **Network** | Ports: 5432 (DB), 8080 (API), 5050 (pgAdmin), 3001 (Grafana), 9090 (Prometheus), 3100 (Loki), 8082 (Jenkins), 9000 (SonarQube) |

## 🔄 Release Process

1. **Preparation** - Feature complete, tests passing
2. **Build** - Create Docker images: `mvn clean install && docker-compose build`
3. **Staging** - Deploy to staging environment with monitoring
4. **Testing** - Smoke tests, performance tests, verify monitoring metrics
5. **Production** - Deploy to production with full monitoring stack
6. **Monitoring** - Watch metrics and logs in Grafana/Loki
7. **Rollback** - If needed, quick rollback available

## 🚨 Incident Response

For incidents:
1. Check [Monitoring](04-monitoring/) for alerts
2. Review logs in Grafana Explore (Loki datasource)
3. Check Prometheus alerts at http://localhost:9090/alerts
4. Assess impact and severity
5. Follow rollback procedure if needed
6. Document incident

## 📞 Support

- **Deployment issues**: Check [Deployment Guide](02-deployment/)
- **Security questions**: Check [Security Guide](03-security/)
- **Monitoring alerts**: Check [Monitoring Guide](04-monitoring/)
- **Infrastructure**: Check [Infra README](../../infra/README.md)
- **General issues**: Check logs with `bash scripts/logs.sh`

## 📖 Navigation

- **[← Back to Docs](../)** - Volver a índice principal
- **[Business](../01-business/)** - Requisitos
- **[Architecture](../02-architecture/)** - Diseño
- **[Development](../03-development/)** - Desarrollo
