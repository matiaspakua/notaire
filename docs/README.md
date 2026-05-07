# Notaire Project - Complete Documentation

Bienvenido a la documentación completa del proyecto Notaire. Esta guía proporciona toda la información necesaria para entender, desarrollar, y operar el sistema.

> Nota: la documentación principal del sistema está centralizada en `/docs`. Los servicios también tienen README propios en `backend-api/README.md`, `frontend-swing/README.md` e `init-db/README.md`.

## 📖 Documentation Structure

Nuestra documentación está organizada según el SDLC (Software Development Lifecycle):

### [01. Business Documentation](01-business/)
**Entender el negocio y los requisitos**

- Requirements & Use Cases - Especificación funcional completa (73 casos de uso)
- Actors & Stakeholders - Identificación de actores del sistema
- Data Model - Diccionario de datos y modelo entidad-relación
- User Manuals - Guías de usuario y administrador

**¿Cuándo usar?**
- Eres un stakeholder o product owner
- Necesitas entender qué hace el sistema
- Buscas funcionalidades específicas

### [02. Architecture Documentation](02-architecture/)
**Entender cómo está diseñado el sistema**

- **[Architecture Decision Records (ADRs)](02-architecture/01-adr/)** - Decisiones arquitectónicas documentadas
  - ADR-001: Microservices Architecture
  - ADR-002: Module Structure (Maven)
  - ADR-003: REST API Versioning
  - ADR-004: Database Migration (MySQL → PostgreSQL)
  - ADR-005: Modern Frontend Migration
  - ADR-006: Testing Strategy
  - ADR-007: Database Schema Versioning (Flyway)
  - ADR-008: Security & Authentication

- **Architecture Overview** - Visión general del sistema
- **Diagrams** - Diagramas de arquitectura y flujos
- **Patterns** - Patrones de diseño y mejores prácticas

**¿Cuándo usar?**
- Eres un arquitecto o senior engineer
- Tomas decisiones técnicas importantes
- Necesitas entender por qué el sistema está diseñado así

### [03. Development Documentation](03-development/)
**Cómo desarrollar en el proyecto**

- **[Development Setup](03-development/01-setup/)** - Configurar ambiente local
  - Java 21 installation
  - Docker & PostgreSQL setup
  - IDE configuration
  
- **[Build & Deploy](03-development/02-build/)** - Cómo buildear y deployar
  - Maven commands
  - Docker builds
  - Docker Compose deployment

- **[Testing Guide](03-development/03-testing/)** - Estrategia de testing
  - Unit testing
  - Integration testing
  - Coverage requirements (80% minimum)

- **[Code Standards](03-development/04-code-standards/)** - Estándares de código
  - Naming conventions
  - Formatting rules
  - Import ordering

**¿Cuándo usar?**
- Eres desarrollador en el proyecto
- Necesitas setup local
- Quieres contribuir código

### [04. Operations Documentation](04-operations/)
**Cómo operar y mantener el sistema**

- **[DevSecOps Pipeline](04-operations/01-devsecops/)** - CI/CD pipeline
- **[Deployment Guide](04-operations/02-deployment/)** - Cómo deployar a producción
- **[Security](04-operations/03-security/)** - Estándares de seguridad
- **[Monitoring](04-operations/04-monitoring/)** - Monitoreo y alertas

**¿Cuándo usar?**
- Eres DevOps o SRE
- Necesitas deployar el sistema
- Debes monitorear el sistema

### [05. API Documentation](05-api/)
**Referencia de API REST**

- **API Overview** - Descripción de endpoints
- **Endpoints** - Referencia completa de cada endpoint
- **Schemas** - DTOs y estructuras de respuesta

**¿Cuándo usar?**
- Desarrollas integraciones con la API
- Necesitas referencia de endpoints
- Buscas esquemas de respuesta

### [06. Learning Resources](06-learning/)
**Recursos para aprender sobre el proyecto**

- **[Onboarding Guide](06-learning/01-onboarding/)** - Guía para nuevos miembros
- **[Architecture Overview](06-learning/02-architecture-overview/)** - Visión general técnica
- **[Refactoring Guide](06-learning/03-refactoring-guide/)** - Plan de migración del monolito

**¿Cuándo usar?**
- Eres nuevo en el proyecto
- Necesitas una introducción
- Quieres entender el plan de refactoring

## 🚀 Quick Start

### For Developers

1. **Setup**: [Development Setup Guide](03-development/01-setup/)
2. **Build**: [Build & Deploy Guide](03-development/02-build/)
3. **Code**: Follow [Code Standards](03-development/04-code-standards/)
4. **Test**: Run tests with [Testing Guide](03-development/03-testing/)

### For Architects

1. **Read**: [Architecture Overview](02-architecture/README.md)
2. **Review**: [Architecture Decision Records](02-architecture/01-adr/)
3. **Understand**: [Diagrams](02-architecture/03-diagrams/)
4. **Consult**: [CLAUDE.md](../CLAUDE.md) for design principles

### For Operations

1. **Deploy**: [Deployment Guide](04-operations/02-deployment/)
2. **Monitor**: [Monitoring Guide](04-operations/04-monitoring/)
3. **Secure**: [Security Guide](04-operations/03-security/)
4. **Pipeline**: [DevSecOps Guide](04-operations/01-devsecops/)

### For Product/Business

1. **Understand**: [Business Documentation](01-business/README.md)
2. **Explore**: [Use Cases](01-business/02-use-cases/)
3. **Refer**: [Data Model](01-business/04-data-model/)

## 📋 Documentation Map

```
Documentación por Rol:

┌─────────────────────────────────────────────────────────┐
│                Product Owner / Stakeholder              │
├─────────────────────────────────────────────────────────┤
│  Business (requirements, use cases, data model)         │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│         Architect / Senior Engineer                     │
├─────────────────────────────────────────────────────────┤
│  Architecture (ADRs, design decisions, patterns)        │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│              Developer / Engineer                       │
├─────────────────────────────────────────────────────────┤
│  Development (setup, build, testing, code standards)    │
│  API (endpoints, schemas)                               │
│  Learning (refactoring guide)                           │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│         DevOps / SRE / Infrastructure                   │
├─────────────────────────────────────────────────────────┤
│  Operations (deployment, monitoring, security, CI/CD)   │
└─────────────────────────────────────────────────────────┘
```

## 🏗️ System Overview

### Architecture

```
┌─────────────────────────────────────────────┐
│        Frontend (Java Swing)                │
│   - Presupuestos                            │
│   - Personas                                │
│   - Escrituras                              │
│   - Gestiones                               │
└────────────────┬────────────────────────────┘
                 │ REST API (HTTP/JSON)
                 ↓
┌─────────────────────────────────────────────┐
│     Backend (Spring Boot 4.0.4)             │
│   - Controllers (REST endpoints)            │
│   - Services (business logic)               │
│   - Repositories (data access)              │
│   - Entities (domain model)                 │
│   - Observability (metrics, logs, health)   │
├────────────────┬────────────────────────────┤
│   Actuator     │   Micrometer               │
│   /actuator/*  │   Prometheus metrics       │
└────────┬───────┴───────────┬────────────────┘
         │                   │
         ▼                   ▼
┌─────────────────────────────────────────────┐
│   Database (PostgreSQL 16)                  │
│   - Tables, indexes, constraints            │
│   - Audit logging                           │
│   - pg_stat_queries                         │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│       Monitoring Infrastructure              │
│   ┌──────────┐  ┌──────────┐               │
│   │Prometheus│  │  Grafana │               │
│   │  :9090   │  │  :3001   │               │
│   └─────┬────┘  └────┬─────┘               │
│         │             │                     │
│   ┌─────┴────┐  ┌────┴─────┐               │
│   │   Loki    │  │postgres- │               │
│   │  :3100    │  │exporter  │               │
│   └──────────┘  │  :9187   │               │
│                 └──────────┘               │
└─────────────────────────────────────────────┘
```

### Technology Stack

| Component | Technology |
|-----------|-----------|
| Backend Framework | Spring Boot 4.0.4 |
| Language | Java 21 LTS |
| Build Tool | Maven 3.9.x |
| Database | PostgreSQL 16 |
| ORM | Hibernate 6.x |
| Frontend | Java Swing |
| Containerization | Docker |
| Orchestration | Docker Compose |
| API Docs | OpenAPI 3.0 |
| Testing | JUnit 5, Mockito, AssertJ |
| Code Quality | JaCoCo, Checkstyle, SpotBugs |
| **Metrics** | **Prometheus + Micrometer** |
| **Dashboards** | **Grafana** |
| **Log Aggregation** | **Loki + Promtail** |
| **CI/CD** | **Jenkins + GitHub Actions** |

## 📚 Key Documents

| Document | Purpose | Audience |
|----------|---------|----------|
| [CLAUDE.md](../CLAUDE.md) | Project guidance & rules | All |
| [ADR-001](02-architecture/01-adr/ADR-001-microservices-architecture.md) | Why 3-tier architecture? | Architects, Seniors |
| [ADR-002](02-architecture/01-adr/ADR-002-module-structure.md) | Maven module structure | Developers, Architects |
| [ADR-003](02-architecture/01-adr/ADR-003-rest-api-versioning.md) | API versioning strategy | Developers, Architects |
| [ADR-004](02-architecture/01-adr/ADR-004-database-migration.md) | MySQL → PostgreSQL | Architects, DevOps |
| [Development Setup](03-development/01-setup/) | Local environment | Developers |
| [Code Standards](03-development/04-code-standards/) | Naming, formatting rules | Developers |
| [Testing Guide](03-development/03-testing/) | Unit & integration tests | QA, Developers |
| [Monitoring Guide](04-operations/04-monitoring/) | Prometheus, Grafana, Loki setup | DevOps, SRE |
| [Deployment Guide](04-operations/02-deployment/) | Full deployment instructions | DevOps, Developers |
| [Infra README](../infra/README.md) | Infrastructure services setup | DevOps |

## 🤝 Contributing

Antes de contribuir código:

1. **Read**: [Code Standards](03-development/04-code-standards/)
2. **Understand**: [CLAUDE.md](../CLAUDE.md)
3. **Follow**: [Development Guide](03-development/)
4. **Check**: [Architecture ADRs](02-architecture/01-adr/) for design decisions

### Documentation Contributions

Para mejorar documentación:

1. Ubicar el archivo correcto en la carpeta adecuada
2. Seguir estructura Markdown consistente
3. Incluir ejemplos prácticos cuando sea posible
4. Mantener actualizado con cambios de código
5. Crear PR con cambios documentados

## 📞 Getting Help

| Pregunta | Recurso |
|----------|---------|
| ¿Cómo configuro el ambiente? | [Development Setup](03-development/01-setup/) |
| ¿Cuál es la arquitectura del sistema? | [Architecture ADRs](02-architecture/01-adr/) |
| ¿Cuáles son los estándares de código? | [Code Standards](03-development/04-code-standards/) |
| ¿Cómo escribo tests? | [Testing Guide](03-development/03-testing/) |
| ¿Cómo deployar? | [Deployment Guide](04-operations/02-deployment/) |
| ¿Cómo monitorear el sistema? | [Monitoring Guide](04-operations/04-monitoring/) |
| ¿Dónde están las credenciales? | [Infra README](../infra/README.md#credentials) |
| ¿Qué hace cada caso de uso? | [Use Cases](01-business/02-use-cases/) |
| ¿Cuál es el modelo de datos? | [Data Model](01-business/04-data-model/) |

## 📊 Documentation Status

| Section | Status | Updated |
|---------|--------|---------|
| Business (Requirements, Use Cases) | ✅ Complete | 2024-04-13 |
| Architecture (ADRs 001-004) | ✅ Complete | 2024-04-13 |
| Development (Setup, Build, Testing) | ✅ Complete | 2024-04-13 |
| Operations (Deployment, Monitoring, Security) | ✅ Complete | 2026-05-07 |
| API Reference | 🟡 In Progress | - |
| Learning Resources | 🟡 In Progress | - |

*Status Leyenda: ✅ Completo | 🟡 En Progreso | ❌ Pendiente*

## 🔄 Document Versioning

Documentación sigue el mismo versionado que el código:
- **Cambios menores**: v1.1.0 (formatting, clarification)
- **Nuevas secciones**: v1.2.0 (guides, ADRs)
- **Reestructuración mayor**: v2.0.0 (reorganización completa)

Último actualizado: **2026-05-07**

## 📖 Navigation

- **[Business](01-business/)** - Requisitos y casos de uso
- **[Architecture](02-architecture/)** - ADRs y decisiones de diseño
- **[Development](03-development/)** - Setup, build, testing
- **[Operations](04-operations/)** - Deployment, monitoring, security
- **[API](05-api/)** - Referencia de endpoints
- **[Learning](06-learning/)** - Guías de aprendizaje

---

**¿Eres nuevo en el proyecto?** → [Onboarding Guide](06-learning/01-onboarding/)

**¿Necesitas ayuda rápida?** → Usa la tabla de [Getting Help](#-getting-help) arriba

**¿Quieres contribuir?** → Lee [Contributing](#-contributing)

