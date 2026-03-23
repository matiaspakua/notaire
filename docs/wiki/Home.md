# Proceso de Desarrollo - Notaire

## Descripción General

Este proyecto es un sistema de gestión notarial que está siendo modernizado desde un monolito Java Swing hacia una arquitectura de microservicios con REST API.

## Arquitectura del Proyecto

```
notaire/
├── backend-api/        # Spring Boot REST API (Java 21)
├── frontend-swing/     # Swing GUI Client
├── notaire-shared/     # DTOs y código común
└── docs/              # Documentación
```

## Stack Tecnológico

| Componente | Tecnología |
|------------|-----------|
| Backend | Spring Boot 3.x, Java 21 |
| Base de Datos | PostgreSQL 15 |
| ORM | Spring Data JPA |
| Frontend | Java Swing (legacy) |
| Contenedores | Docker |
| API Documentation | Swagger/OpenAPI |

## Flujo de Desarrollo

### 1. Configuración Inicial

```bash
# Clonar repositorio
git clone https://github.com/matiaspakua/notaire.git
cd notaire

# Iniciar ambiente
bash scripts/start.sh

# Compilar proyecto
mvn clean install
```

### 2. Desarrollo de Funcionalidades

1. **Requerimientos**: Issues en GitHub con label `requerimiento-funcional`
2. **Casos de Uso**: Especificaciones en `docs/business/03_CU - Casos de Uso/`
3. **Implementación**: Desarrollo en módulos `backend-api/` y `frontend-swing/`
4. **Testing**: Tests unitarios e integración con JUnit 5

### 3. Refactoring del Monolito

Ver [Refactoring Plan](Refactoring-Plan) para detalles completos.

### 4. Commits y Pull Requests

```bash
# Crear branch
git checkout -b feature/rf-XX-descripcion

# Commit (seguir convencional commits)
git commit -m "feat(backend): add new endpoint for RF-XX"

# Push y PR
git push -u origin feature/rf-XX-descripcion
```

## Comandos Útiles

```bash
# Compilar
mvn clean install

# Ejecutar tests
mvn test

# Ver cobertura
mvn jacoco:report

# Iniciar aplicación
bash scripts/start.sh
```

## Recursos

- [Documentación de Negocio](Business-Documentation)
- [Plan de Refactoring](Refactoring-Plan)
- [Requerimientos](https://github.com/matiaspakua/notaire/labels/requerimiento-funcional)
