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
│  │  (Swing GUI)     │────────>│  REST API        │          │
│  │                  │  HTTP   │  (Spring Boot)   │          │
│  │  - Presupuestos  │<────────│  - Controllers   │          │
│  │  - Personas      │         │  - Services      │          │
│  │  - Escrituras    │         │  - Repositories  │          │
│  │  - Gestiones     │         │  - Entities      │          │
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
│                              │  - Audit Log     │            │
│                              └──────────────────┘            │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Deployment:                                               │
│  - Frontend: Standalone Java application                   │
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

**Frontend (GUI)**
- Responsable: Interfaz de usuario, presentación
- Prohibido: Lógica de negocio, acceso a BD
- Comunicación: Única vía REST API

**Backend (API)**
- Responsable: Lógica de negocio, persistencia, seguridad
- Prohibido: Imports de Swing, código de GUI
- Comunicación: REST + Base de datos

**Database**
- Responsable: Persistencia de datos
- Acceso: Solo vía Backend (Hibernate/JPA)
- Versioning: Liquibase para schema migrations

### Layered Architecture

```
┌──────────────────────┐
│   Presentation       │  (Controllers, API endpoints)
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
| **Availability** | 99.5% uptime | Graceful shutdown, health checks |
| **Performance** | <500ms avg response | Database indexing, caching |
| **Scalability** | Horizontal scaling | Stateless backend, connection pooling |
| **Security** | Authentication + Authorization | JWT, RBAC, audit logging |
| **Maintainability** | <6 month time-to-market | Clean code, comprehensive tests |
| **Observability** | Full system visibility | Structured logging, metrics, tracing |

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Frontend** | Java Swing | 21 LTS |
| **Backend** | Spring Boot | 4.0.4 |
| **Runtime** | Java | 21 LTS |
| **Database** | PostgreSQL | 16.x |
| **Build Tool** | Maven | 3.9.x |
| **Container** | Docker | 24.x |
| **ORM** | Hibernate | 6.x |
| **API Docs** | OpenAPI 3.0 | Latest |

## Communication Patterns

### Request/Response Flow

```
Frontend (Swing)
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
Frontend (Swing)
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
| ADR-005 | Testing Strategy | *Pending* | - |
| ADR-006 | Security & Authentication | *Pending* | - |
| ADR-007 | Logging & Monitoring | *Pending* | - |
| ADR-008 | Error Handling | *Pending* | - |

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
