# Backend API — Notaire

`backend-api` is the REST service that implements the business logic and exposes the Notaire HTTP endpoints.

- Java 26
- Spring Boot 4
- PostgreSQL 16
- Endpoints under `/api/v1`

## Run in development

```bash
cd backend-api
mvn spring-boot:run
```

## Tests

```bash
mvn test
mvn jacoco:check -pl backend-api
```

## Docker

The backend image is built with `backend-api/Dockerfile`. It is a multi-stage
build **whose context is the repository root** (not `backend-api/`), because it
needs the root reactor (parent POM) to compile:

```bash
# from the repository root
docker build -f backend-api/Dockerfile -t notaire-backend .
# or through compose (workspace/stack/start.sh uses this)
docker compose build backend
```

`backend-api/Dockerfile.slim` is a variant that copies an already compiled JAR
(currently unreferenced). `.dockerignore` and `docker-compose.yml` live at the
root because the build context and the orchestration are repository-wide.

## Related documentation

- `/docs/200-architecture/` — architecture design and REST API documentation (`203-design/`)
- `/docs/300-development/` — development and testing guides
