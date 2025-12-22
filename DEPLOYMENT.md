# Notaire REST API - Deployment Guide

## Overview

This guide provides instructions for deploying and managing the Notaire application, which has been migrated from a legacy Swing GUI to a modern Spring Boot REST API with PostgreSQL backend.

## Architecture

- **Backend API**: Spring Boot 3.2.9 with Java 21 LTS
- **Database**: PostgreSQL 16
- **Build Tool**: Maven 3.9
- **Containerization**: Docker & Docker Compose
- **API Documentation**: Swagger/OpenAPI 2.0

## Prerequisites

- Docker (20.10+) and Docker Compose (2.0+)
- Maven 3.9+ (for local development)
- Java 21 LTS (for local development)
- Bash shell
- cURL (for testing)

## Quick Start

### Option 1: Using Provided Scripts (Recommended)

```bash
# Start the entire application stack
bash start.sh

# In another terminal, run the test suite
bash test.sh

# View logs
bash logs.sh backend

# Stop the application
bash stop.sh
```

### Option 2: Manual Docker Compose

```bash
# Build backend Docker image
docker-compose build

# Start all services
docker-compose up -d

# Verify services are running
docker-compose ps

# View logs
docker-compose logs -f backend

# Stop services
docker-compose down
```

## Available Services

### Backend API
- **URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

### PostgreSQL Database
- **Host**: localhost:5432
- **Database**: notaire
- **Username**: notaire
- **Password**: (see environment.env)

### PgAdmin (Optional)
- **URL**: http://localhost:5050
- **Username**: admin@pgadmin.org
- **Password**: admin
- **Start with**: `docker-compose --profile admin up -d`

## REST API Endpoints

All endpoints follow RESTful conventions:

```
GET    /api/v1/{entity}           - List all entities
GET    /api/v1/{entity}/{id}      - Get entity by ID
POST   /api/v1/{entity}           - Create new entity
PUT    /api/v1/{entity}/{id}      - Update entity
DELETE /api/v1/{entity}/{id}      - Delete entity
```

### Available Entities

- `conceptos` - Conceptos
- `personas` - Personas
- `tramites` - Trámites
- `escrituras` - Escrituras
- `presupuestos` - Presupuestos
- `items` - Items
- `folios` - Folios
- `testimonios` - Testimonios
- `pagos` - Pagos
- `historial` - Historial
- `inmuebles` - Inmuebles
- `usuarios` - Usuarios
- And 8 more...

## Testing the API

### Using Provided Test Scripts

```bash
# Test all endpoints
bash test.sh

# View test source code
cat test/http/test-all-endpoints-v2.sh
```

### Manual Testing with cURL

```bash
# List all conceptos
curl http://localhost:8080/api/v1/conceptos

# Get specific concepto
curl http://localhost:8080/api/v1/conceptos/1

# Create new concepto
curl -X POST http://localhost:8080/api/v1/conceptos \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "New Concepto"}'

# Update concepto
curl -X PUT http://localhost:8080/api/v1/conceptos/1 \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Updated Concepto"}'

# Delete concepto
curl -X DELETE http://localhost:8080/api/v1/conceptos/1
```

## Troubleshooting

### Services Won't Start

```bash
# Check Docker daemon
docker ps

# View detailed logs
docker-compose logs backend
docker-compose logs postgres

# Clean up and retry
docker-compose down -v
docker system prune
bash start.sh
```

### Database Connection Issues

```bash
# Verify PostgreSQL is running
docker-compose exec postgres pg_isready

# Check database exists
docker-compose exec postgres psql -U notaire -c "\l"

# View database logs
docker-compose logs postgres
```

### API Not Responding

```bash
# Check if port 8080 is in use
netstat -tlnp | grep 8080

# Check API logs
docker-compose logs -f backend

# Test connectivity
curl -v http://localhost:8080/swagger-ui.html
```

## Development

### Building Locally

```bash
# Build entire project
mvn clean package -DskipTests

# Build only backend
mvn clean package -DskipTests -pl backend-api

# Run backend locally (requires PostgreSQL)
java -jar backend-api/target/backend-api-1.0-SNAPSHOT.jar
```

### Modifying the API

1. Update entity in the appropriate JPA Controller (`backend-api/src/main/java/com/licensis/notaire/jpa/`)
2. REST endpoint automatically picks up changes via Spring Data JPA
3. Swagger documentation updates automatically

### Database Schema Changes

1. Modify SQL files in `init-db/`
2. Stop services: `docker-compose down -v` (removes volumes)
3. Restart services: `bash start.sh` (reinitializes database)

## Deployment

### Docker Hub

```bash
# Build and tag image
docker build -t yourusername/notaire-backend:1.0 -f Dockerfile.backend .

# Push to registry
docker push yourusername/notaire-backend:1.0
```

### Kubernetes (Future)

The Docker image can be deployed to any Kubernetes cluster:

```bash
# Create deployment
kubectl apply -f k8s-deployment.yaml

# Scale replicas
kubectl scale deployment notaire-backend --replicas=3
```

## Environment Variables

See `environment.env` for configuration:

```env
POSTGRES_USER=notaire
POSTGRES_PASSWORD=notaire-password
POSTGRES_DB=notaire
NOTAIRE_DATABASE_HOST=postgres
NOTAIRE_DATABASE_PORT=5432
```

## Performance Monitoring

### CPU & Memory Usage

```bash
docker stats backend postgres
```

### Database Queries

```bash
# Connect to database
docker-compose exec postgres psql -U notaire -d notaire

# View running queries
SELECT * FROM pg_stat_activity;
```

## Security Considerations

⚠️ **Important for Production:**

1. Change all default passwords in `environment.env`
2. Use strong PostgreSQL credentials
3. Enable HTTPS/TLS for API endpoints
4. Implement API authentication (JWT, OAuth2)
5. Use network policies to restrict access
6. Regular database backups
7. Update Java/Maven/Docker to latest LTS versions

## Migration Notes

This application was migrated from:
- **Java 8** → **Java 21 LTS**
- **Spring Boot 2.7** → **Spring Boot 3.2**
- **javax** → **jakarta** packages
- **Monolithic Swing GUI** → **REST API + PostgreSQL**

19 out of 26 entities are exposed via REST API. The 7 remaining entities have composite primary keys and require custom handling (future work).

## Support

For issues or questions:

1. Check logs: `bash logs.sh backend`
2. Review Swagger docs: http://localhost:8080/swagger-ui.html
3. Check database: `docker-compose exec postgres psql -U notaire`
4. Review test results: `bash test.sh`

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [Docker Compose](https://docs.docker.com/compose/)
- [PostgreSQL](https://www.postgresql.org/docs/)
