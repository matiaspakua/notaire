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

## Documentación relevante

- `/docs/02-architecture/` — diseño de arquitectura
- `/docs/05-api/` — documentación de la API REST
- `/docs/03-development/` — guías de desarrollo y pruebas
