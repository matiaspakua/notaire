# Backend API - Notaire

`backend-api` es el servicio REST que implementa la lógica de negocio y expone los endpoints HTTP de Notaire.

- Java 21
- Spring Boot 4
- PostgreSQL 16
- Endpoints en `/api/v1`

## Ejecutar en desarrollo

```bash
cd backend-api
mvn spring-boot:run
```

## Pruebas

```bash
mvn test
mvn jacoco:check -pl backend-api
```

## Docker

La imagen del backend se construye con `backend-api/Dockerfile`. Es un build
multi-etapa **cuyo contexto es la raíz del repositorio** (no `backend-api/`),
porque necesita los módulos `notaire-shared` y `frontend-swing` para compilar:

```bash
# desde la raíz del repo
docker build -f backend-api/Dockerfile -t notaire-backend .
# o vía compose (lo usa scripts/start.sh)
docker compose build backend
```

`backend-api/Dockerfile.slim` es una variante que copia un JAR ya compilado
(actualmente sin referenciar). El `.dockerignore` y `docker-compose.yml` viven
en la raíz porque el contexto de build y la orquestación son a nivel de repo.

## Documentación relevante

- `/docs/200-architecture/` — diseño de arquitectura y documentación de la API REST (`203-design/`)
- `/docs/300-development/` — guías de desarrollo y pruebas
