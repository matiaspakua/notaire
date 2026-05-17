# Architecture - Notaire Project

Documentación de arquitectura, decisiones de diseño (ADRs) y patrones del proyecto Notaire.

## Quick Links

- **[Architecture Overview](02-overview/architecture-overview.md)** - Descripción general del sistema
- **[Architecture Decision Records](01-adr/README.md)** - Decisiones arquitectónicas documentadas
- **[Diagrams](03-diagrams/)** - Diagramas de arquitectura y flujos
- **[Patterns & Best Practices](04-patterns/)** - Patrones y mejores prácticas

## Architecture Decision Records (ADRs)

Los ADRs documentan las decisiones arquitectónicas importantes del proyecto. Cada ADR incluye contexto, opciones consideradas, y trade-offs.

### Core Architecture Decisions

1. **[ADR-001: Microservices Architecture](01-adr/ADR-001-microservices-architecture.md)**
   - Migración de monolito Swing a arquitectura de tres capas
   - Status: Accepted
   - Backend API REST + Frontend Swing cliente + Base de datos centralizada

2. **[ADR-002: Module Structure](01-adr/ADR-002-module-structure.md)**
   - Estructura de módulos Maven multi-módulo
   - Status: Accepted
   - backend-api, frontend-swing, notaire-shared

3. **[ADR-003: REST API Versioning](01-adr/ADR-003-rest-api-versioning.md)**
   - Estrategia de versionado de API REST
   - Status: Accepted
   - URL path versioning: `/api/v1`, `/api/v2`

4. **[ADR-004: Database Migration](01-adr/ADR-004-database-migration.md)**
   - Migración de MySQL a PostgreSQL 16
   - Status: Accepted
   - PostgreSQL con Liquibase para schema management

## Architecture Overview

### High-Level View

```
┌─────────────────────────────────────────────────────────────┐
│                    Notaire System                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │   Frontend       │         │                  │          │
│  │  (Next.js Web)   │────────>│  REST API        │          │
│  │                  │  HTTP   │  (Spring Boot)   │          │
│  │  - Dashboard     │<────────│  - Controllers   │          │
│  │  - Gestiones     │         │  - Services      │          │
│  │  - Escrituras    │         │  - Repositories  │          │
│  │  - Presupuestos  │         │  - Entities      │          │
│  └──────────────────┘         └────────┬─────────┘          │
│                                        │                    │
│                                        │ SQL                │
│                                        ↓                    │
│                              ┌──────────────────┐            │
│                              │   PostgreSQL     │            │
│                              │   Database 16    │            │
│                              │                  │            │
│                              │  - Presupuestos  │            │
│                              │  - Personas      │            │
│                              │  - Escrituras    │            │
│                              │  - Gestiones     │            │
│                              │  - Inmuebles     │            │
│                              │  - Audit Log     │            │
│                              └──────────────────┘            │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Deployment:                                               │
│  - Frontend: Docker container (Next.js)                    │
│  - Backend: Docker container (Spring Boot)                 │
│  - Database: Docker container (PostgreSQL)                 │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure (Backend)

```
com.licensis.notaire/
├── api/                      # REST Controllers
│   ├── PresupuestoController
│   ├── PersonaController
│   ├── EscrituraController
│   └── ...
├── service/                  # Business Logic
│   ├── PresupuestoService
│   ├── PersonaService
│   ├── EscrituraService
│   └── ...
├── repository/               # Data Access (Spring Data JPA)
│   ├── PresupuestoRepository
│   ├── PersonaRepository
│   ├── EscrituraRepository
│   └── ...
├── negocio/                  # Domain Entities (@Entity)
│   ├── Presupuesto
│   ├── Persona
│   ├── Escritura
│   └── ...
├── exception/                # Custom Exceptions
│   ├── NotaireException
│   ├── ResourceNotFoundException
│   └── BusinessValidationException
├── dto/                      # Data Transfer Objects
│   ├── DtoPresupuesto
│   ├── DtoPersona
│   └── ApiResponse
└── config/                   # Spring Configuration
    ├── JpaConfig
    ├── SecurityConfig
    └── CorsConfig
```

## Key Design Principles

### Separation of Concerns

**Frontend (Web)**
- Responsable: Interfaz de usuario moderna, responsive
- Prohibido: Lógica de negocio pesada, acceso directo a BD
- Comunicación: Única vía REST API

**Backend (API)**
- Responsable: Lógica de negocio, persistencia, seguridad, reportes
- Prohibido: Código de GUI o presentación web
- Comunicación: REST + Base de datos

**Database**
- Responsable: Persistencia de datos
- Acceso: Solo vía Backend (Hibernate/JPA)
- Versioning: Flyway para schema migrations

### Layered Architecture

```
┌──────────────────────┐
│   Presentation       │  (Next.js App, Controllers)
├──────────────────────┤
│   Business Logic     │  (Services, domain rules)
├──────────────────────┤
│   Data Access        │  (Repositories, JPA)
├──────────────────────┤
│   Database           │  (PostgreSQL)
└──────────────────────┘
```

### Non-Functional Requirements

| Aspecto | Objetivo | Implementación |
|---------|----------|-----------------|
| **Availability** | 99.5% uptime | Docker orchestration, health checks |
| **Performance** | <500ms avg response | Database indexing, efficient queries |
| **Scalability** | Horizontal scaling | Stateless backend, containerization |
| **Security** | Authentication + Authorization | JWT, RBAC, audit logging |
| **Maintainability** | <6 month time-to-market | Clean code, comprehensive tests |
| **Observability** | Full system visibility | Loki, Prometheus, Grafana |

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Frontend** | Next.js (React) | 15.x |
| **Backend** | Spring Boot | 4.0.4 |
| **Runtime** | Java | 21 LTS |
| **Database** | PostgreSQL | 16.x |
| **Build Tool** | Maven | 3.9.x |
| **Container** | Docker | 24.x |
| **ORM** | Hibernate | 6.x |
| **API Docs** | OpenAPI 3.0 | Latest |
| **Monitoring** | Loki, Prometheus, Grafana | Latest |

## Communication Patterns

### Request/Response Flow

```
Frontend (Next.js)
    │
    │ HTTP Request (JSON payload)
    ↓
REST Controller
    │
    │ Delegate to service
    ↓
Business Service
    │
    │ Data validation & rules
    ↓
Repository (Spring Data JPA)
    │
    │ Hibernate ORM mapping
    ↓
PostgreSQL Database
    │
    ↓ (Result)
Repository
    │
    ↓ (Entity → DTO conversion)
Business Service
    │
    ↓ (Wrapped in ApiResponse)
REST Controller
    │
    │ HTTP Response (JSON)
    ↓
Frontend (Next.js)
```

## Related Documentation

- [Development Setup](../03-development/01-setup/) - Cómo configurar el ambiente
- [Building & Testing](../03-development/02-build/) - Cómo buildear y testear
- [API Reference](../05-api/) - Endpoints y schemas
- [Refactoring Plan](../06-learning/03-refactoring-guide/) - Plan de migración del monolito

## ADR Index

| ID | Title | Status | Date |
|---|---|---|---|
| ADR-001 | Microservices Architecture | Accepted | 2024-03-20 |
| ADR-002 | Module Structure | Accepted | 2024-03-20 |
| ADR-003 | REST API Versioning | Accepted | 2024-03-20 |
| ADR-004 | Database Migration | Accepted | 2024-03-20 |
| ADR-005 | Modern Frontend Migration | Accepted | 2024-04-10 |
| ADR-006 | Testing Strategy | Accepted | 2024-04-12 |
| ADR-007 | Database Schema Versioning (Flyway) | Accepted | 2024-04-13 |
| ADR-008 | Security & Authentication | Accepted | 2024-04-28 |
| ADR-009 | Logging & Monitoring | Accepted | 2024-04-28 |
| ADR-010 | Error Handling | Accepted | 2024-04-28 |

## Glossary

- **ADR** - Architecture Decision Record
- **API** - Application Programming Interface (REST in this project)
- **DTO** - Data Transfer Object
- **JPA** - Java Persistence API (Hibernate implementation)
- **ORM** - Object-Relational Mapping
- **REST** - Representational State Transfer
- **RBAC** - Role-Based Access Control

## Contributing

Para documentar nuevas decisiones arquitectónicas:

1. Crear nuevo ADR siguiendo template en [ADR-001](01-adr/ADR-001-microservices-architecture.md)
2. Incluir Context, Decision, Options Considered, Trade-offs, Consequences
3. Registrar en este README y actualizar ADR Index
4. Obtener aprobación del equipo antes de marcar como "Accepted"

## See Also

- [CLAUDE.md](../../CLAUDE.md) - Guía del proyecto
- [Refactoring Rules](../../.claude/rules/refactoring.md) - Reglas de refactoring
- [Code Quality Guide](../../.claude/rules/code-quality.md) - Estándares de calidad
