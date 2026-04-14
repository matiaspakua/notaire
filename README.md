# Notaire

Proyecto de modernización de un sistema de administración de escribanía.

Se compone de tres servicios principales:

- `backend-api/` — Servicio REST con Spring Boot
- `frontend-swing/` — Cliente de escritorio en Java Swing
- `init-db/` — Scripts de PostgreSQL para inicializar la base de datos

## Documentación

Toda la documentación técnica y de negocio está centralizada en `/docs`.

- `/docs/README.md` — índice general de documentación
- `/docs/01-business/` — requisitos, casos de uso, modelo de datos y manuales
- `/docs/02-architecture/` — diseño del sistema, ADRs y diagramas
- `/docs/03-development/` — setup, build, pruebas y estándares
- `/docs/04-operations/` — CI/CD, despliegue y seguridad
- `/docs/05-api/` — documentación de la API REST
- `/docs/06-learning/` — onboarding y recursos de aprendizaje

## Iniciar el proyecto

```bash
bash scripts/start.sh
```

## Documentación de cada servicio

- `backend-api/README.md`
- `frontend-swing/README.md`
- `init-db/README.md`

## Módulos relevantes

- `backend-api/` — API REST y lógica de negocio
- `frontend-swing/` — aplicación de usuario
- `notaire-shared/` — código compartido y DTOs
- `init-db/` — scripts de base de datos
- `scripts/` — herramientas de soporte y arranque

## Enlaces rápidos

- `/docs/README.md`
- `/docs/03-development/03-testing/` — documentación de pruebas
- `/docs/05-api/` — referencia de API
