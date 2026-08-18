# Deployment Guide - Notaire Project

## Overview

This guide covers how to deploy the complete Notaire system including the application stack and the monitoring infrastructure.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Notaire Stack                          │
│  (docker-compose.yml - root)                             │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │PostgreSQL│  │  Backend  │  │  pgAdmin │              │
│  │  :5432   │  │  :8080   │  │  :5050   │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└────────────────────┬────────────────────────────────────┘
                     │ (shared network: notary-network)
┌────────────────────┴────────────────────────────────────┐
│                    Infra Stack                            │
│  (infra/docker-compose.yml)                              │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │Prometheus│  │  Grafana │  │   Loki   │              │
│  │  :9090   │  │  :3001   │  │  :3100   │              │
│  └──────────┘  └──────────┘  └──────────┘              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │  Jenkins │  │SonarQube │  │postgres- │              │
│  │  :8082   │  │  :9000   │  │exporter  │              │
│  └──────────┘  └──────────┘  │  :9187   │              │
│                              └──────────┘              │
└─────────────────────────────────────────────────────────┘
```

## Deployment Steps

### Prerequisites
- Docker and Docker Compose v2+
- Java 21+ (for local development)
- Maven 3.9+ (for local builds)

### 1. Build the Application

```bash
# Build the entire project
mvn clean install -DskipTests

# Build only backend with shared module
mvn clean install -pl backend-api -am -DskipTests
```

### 2. Start the Application Stack

```bash
# From project root
docker-compose up -d

# Verify services are running
docker-compose ps
```

This starts:
- **PostgreSQL 16** on port 5432 (admin/admin)
- **Backend API** on port 8080
- **pgAdmin** on port 5050

### 3. Start the Monitoring Infrastructure

```bash
# From infra directory
cd infra
docker-compose up -d

# Verify infrastructure is running
bash scripts/check-infra.sh
```

This starts:
- **Prometheus** on port 9090
- **Grafana** on port 3001 (admin/admin)
- **Loki** on port 3100
- **Jenkins** on port 8082 (admin/admin)
- **SonarQube** on port 9000 (admin/admin)
- **PostgreSQL Exporter** on port 9187

### 4. Verify Complete Deployment

```bash
# Check all services
curl http://localhost:8080/actuator/health   # Backend
curl http://localhost:9090/-/ready           # Prometheus
curl http://localhost:3001/api/health        # Grafana
curl http://localhost:3100/ready             # Loki
```

## Docker Compose Details

### Root docker-compose.yml
- **Network**: `notary-network` (bridge)
- **Volumes**: `postgres_data`, `pgadmin_data`
- **Backend health check**: `/actuator/health`
- **Environment variables**: Configured via `.env` file

### Infra docker-compose.yml
- **Networks**: `devsecops-network` + `notary-network` (external)
- **Volumes**: All service data persisted
- **Configuration files**: Pre-configured in respective subdirectories
- **Credentials**: admin/admin for all services

## Environment Configuration

### .env File
```env
# PostgreSQL Configuration
POSTGRES_DB=notaire
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
```

### Backend Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/notaire` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `admin` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `admin` | Database password |
| `ENVIRONMENT` | `development` | Environment profile |

## Production Considerations

For production deployment, ensure:

1. **Change default passwords** - Do not use admin/admin in production
2. **Enable HTTPS** - Use TLS for all services
3. **Database backups** - Configure periodic backups
4. **Resource limits** - Set Docker resource constraints
5. **Log rotation** - Configure Docker log rotation
6. **Monitoring alerts** - Configure alerting rules
7. **Scaling** - Consider horizontal scaling for the backend

## Rollback Procedure

```bash
# Stop all services
docker-compose down

# Remove specific volumes if needed
docker-compose down -v

# Restore database from backup
docker exec -i notary-postgres psql -U admin notaire < backup.sql

# Restart with previous version
docker-compose up -d
```

## Related Documentation

- [Monitoring Guide](../04-monitoring/README.md)
- [DevSecOps Pipeline](../01-devsecops/README.md)
- [Operations Overview](../README.md)
