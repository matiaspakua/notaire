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
- Docker Compose for local development
- Kubernetes deployment (si aplica)
- Database migrations
- Configuration management

### [03. Security](03-security/)
**Estándares de seguridad y compliance**

- Authentication & Authorization
- Data protection
- API security
- Dependency vulnerabilities
- OWASP compliance

### [04. Monitoring](04-monitoring/)
**Observabilidad y alertas**

- Logging strategy
- Metrics collection
- Health checks
- Alert rules
- SLA monitoring

## 🎯 Quick Start

### Deploy to Local Development
```bash
bash scripts/start.sh
bash scripts/logs.sh
```

### Deploy to Production
See [Deployment Guide](02-deployment/)

### Check System Health
```bash
curl http://localhost:8080/actuator/health
```

### View Logs
```bash
bash scripts/logs.sh
docker logs notaire-backend
docker logs notaire-postgres
```

## 📊 System Requirements

| Component | Requirement |
|-----------|------------|
| **Java** | 21 LTS |
| **RAM** | 2GB minimum, 4GB recommended |
| **Disk** | 10GB free space |
| **Network** | Ports 8080 (API), 5432 (DB), 5050 (pgAdmin) |

## 🔄 Release Process

1. **Preparation** - Feature complete, tests passing
2. **Build** - Create Docker images
3. **Staging** - Deploy to staging environment
4. **Testing** - Smoke tests, performance tests
5. **Production** - Deploy to production
6. **Monitoring** - Watch metrics and logs
7. **Rollback** - If needed, quick rollback available

## 🚨 Incident Response

For incidents:
1. Check [Monitoring](04-monitoring/) for alerts
2. Review logs: `bash scripts/logs.sh`
3. Assess impact and severity
4. Follow rollback procedure if needed
5. Document incident

## 📞 Support

- **Deployment issues**: Check [Deployment Guide](02-deployment/)
- **Security questions**: Check [Security Guide](03-security/)
- **Monitoring alerts**: Check [Monitoring Guide](04-monitoring/)
- **General issues**: Check logs with `bash scripts/logs.sh`

## 📖 Navigation

- **[← Back to Docs](../)** - Volver a índice principal
- **[Business](../01-business/)** - Requisitos
- **[Architecture](../02-architecture/)** - Diseño
- **[Development](../03-development/)** - Desarrollo

