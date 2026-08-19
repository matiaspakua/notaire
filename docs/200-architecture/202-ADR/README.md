# Architecture Decision Records (ADRs)

Los Architecture Decision Records documentan las decisiones arquitectónicas importantes del proyecto Notaire, incluyendo el contexto, opciones consideradas, y trade-offs de cada decisión.

## ADR Index

| # | Title | Status | Date | Scope |
|---|-------|--------|------|-------|
| [001](ADR-001-microservices-architecture.md) | Microservices Architecture | Accepted | 2024-03-20 | System architecture |
| [002](ADR-002-module-structure.md) | Module Structure | Accepted | 2024-03-20 | Code organization |
| [003](ADR-003-rest-api-versioning.md) | REST API Versioning | Accepted | 2024-03-20 | API evolution |
| [004](ADR-004-database-migration.md) | Database Migration | Accepted | 2024-03-20 | Data persistence |
| [005](ADR-005-modern-frontend-migration.md) | Modern Frontend Migration | Accepted | 2024-04-10 | Frontend architecture |
| [006](ADR-006-testing-strategy.md) | Testing Strategy | Accepted | 2024-04-12 | QA & validation |
| [007](ADR-007-database-schema-versioning-flyway.md) | Database Schema Versioning (Flyway) | Accepted | 2024-04-13 | Data persistence |
| [008](ADR-008-security-authentication.md) | Security & Authentication | Accepted | 2024-04-28 | Security |
| [009](ADR-009-logging-monitoring.md) | Logging & Monitoring | Accepted | 2024-04-28 | Observability |
| [010](ADR-010-error-handling.md) | Error Handling | Accepted | 2024-04-28 | Resilience |
| [011](ADR-011-centralized-design-system.md) | Centralized Design System | Accepted | 2026-05-12 | UI/UX |
| [012](ADR-012-ci-cd-pipeline.md) | CI/CD Pipeline (GitHub Actions) | Accepted | 2026-03-24 | DevOps |
| [013](ADR-013-audit-trail.md) | Audit Trail (Spring AOP) | Accepted | 2026-05-12 | Security & Compliance |
| [014](ADR-014-workflow-engine.md) | Workflow Engine (data-driven) | Accepted | 2026-06-09 | Domain model |
| [015](ADR-015-internationalization.md) | Internationalization (next-intl) | Accepted | 2026-05-23 | Frontend |
| [016](ADR-016-observability-stack.md) | Observability Stack Topology | Accepted | 2026-08-19 | Observability |
| [017](ADR-017-container-base-images.md) | Container / Base-Image Strategy | Accepted | 2026-08-19 | DevOps |
| [018](ADR-018-rate-limiting-policy.md) | Rate-Limiting Policy | Accepted | 2026-08-19 | Security |
| [019](ADR-019-secrets-management.md) | Secrets Management | Accepted | 2026-08-19 | Security |
| [020](ADR-020-openapi-exposure-policy.md) | OpenAPI Exposure Policy | Accepted | 2026-08-19 | Security & API |

## ADR Status Legend

- **Proposed** - Opción bajo evaluación, sin implementación aún
- **Accepted** - Aprobado por equipo, actualmente implementado
- **Deprecated** - Fue válido pero fue supercedido
- **Superseded by** - Reemplazado por otro ADR

## How to Read ADRs

Cada ADR sigue esta estructura:

### Header
- **Status**: Proposed | Accepted | Deprecated | Superseded
- **Date**: Cuándo fue propuesto
- **Deciders**: Quién tomó la decisión
- **Related**: ADRs relacionados

### Body

1. **Context** - Qué problema intentamos resolver y cuáles son las restricciones
2. **Decision** - Qué alternativa elegimos y por qué
3. **Options Considered** - Alternativas evaluadas con trade-offs
4. **Trade-off Analysis** - Análisis detallado de los trade-offs clave
5. **Consequences** - Qué se vuelve más fácil, más difícil, y qué necesita seguimiento
6. **Implementation Details** - Específicos técnicos para implementar la decisión
7. **Related ADRs** - Otras decisiones conectadas

## Decision Categories

### System Architecture (2 ADRs)
- **ADR-001**: Migración de monolito a microservicios con 3 capas
- **ADR-005**: Migración a Next.js para el nuevo frontend web

### Code Organization (1 ADR)
- **ADR-002**: Estructura Maven multi-módulo

### API Design (1 ADR)
- **ADR-003**: REST API versioning con URL path versioning

### Data Persistence (2 ADRs)
- **ADR-004**: Migración MySQL → PostgreSQL 16
- **ADR-007**: Gestión de versiones de esquema con Flyway

### Quality & Reliability (3 ADRs)
- **ADR-006**: Estrategia de testing (unitario, integración, E2E)
- **ADR-008**: Seguridad y autenticación (JWT, RBAC)
- **ADR-010**: Manejo de errores global

### Observability (2 ADRs)
- **ADR-009**: Logging y monitoreo centralizado (LPG Stack)
- **ADR-016**: Topología del stack de observabilidad (Prometheus/Grafana/Loki/SonarQube)

### DevOps (2 ADRs)
- **ADR-012**: Pipeline CI/CD con GitHub Actions
- **ADR-017**: Estrategia de imágenes base de contenedores (Alpine, multi-stage, non-root)

### Security & Compliance (4 ADRs)
- **ADR-013**: Auditoría transversal con Spring AOP
- **ADR-018**: Política de rate-limiting (solo login lockout, sin límite general de API)
- **ADR-019**: Gestión de secretos (.env único, ProductionCredentialsGuard)
- **ADR-020**: Política de exposición de OpenAPI/Swagger (público salvo producción)

### Domain Model (1 ADR)
- **ADR-014**: Motor de workflow configurable para gestiones

### Frontend (1 ADR adicional)
- **ADR-015**: Internacionalización con next-intl

## Key Architectural Principles

Basados en los ADRs implementados:

### 1. Separation of Concerns
- Frontend y Backend completamente desacoplados
- Comunicación única vía REST API
- Database solo accesible desde Backend

### 2. Scalability
- Backend stateless para horizontal scaling
- Database connection pooling
- API versionado para evolución sin romper clientes

### 3. Maintainability
- Código organizado en módulos Maven independientes
- Layered architecture (controllers → services → repositories → database)
- Clear responsibility boundaries

### 4. Modern Technology Stack
- Java 21 LTS (long-term support)
- Spring Boot 4.1.0 (framework moderno)
- PostgreSQL 16 (robust RDBMS)
- Docker (containerization)

## ADR Lifecycle

```
Proposed
    │ (Analysis)
    ↓
Accepted
    │ (Implementation)
    ├─→ Deprecated (replaced)
    │
    └─→ Superseded (by new ADR)
```

### Proposing a New ADR

1. **Identify the decision**: Qué pregunta arquitectónica necesita resolverse?
2. **Create ADR file**: `ADR-NNN-title.md`
3. **Document thoroughly**:
   - Context claro (qué problema, restricciones)
   - Mínimo 2-3 opciones consideradas
   - Trade-off analysis detailed
   - Consequences y follow-up actions

4. **Get approval**: Team review and consensus
5. **Implement**: Follow the decision documented
6. **Update this README**: Add to index and track status

## Cross-Cutting Decisions

Algunas decisiones afectan múltiples ADRs:

### Layered Architecture
Implementado en:
- ADR-001 (3-layer architecture)
- ADR-002 (package structure)
- ADR-005 (testing at each layer)

### Data Integrity
Implementado en:
- ADR-004 (PostgreSQL migration)
- ADR-008 (error handling & recovery)

### API Evolution
Implementado en:
- ADR-003 (versioning)
- ADR-006 (authentication)

## Metrics & Monitoring

Para evaluar si nuestras decisiones fueron correctas, monitorearemos:

- **Performance**: API response times <500ms avg
- **Reliability**: 99.5% uptime target
- **Maintainability**: <6 month time-to-market para features nuevas
- **Quality**: 80% code coverage, <5 critical bugs/quarter
- **Developer velocity**: Feature delivery rate

## References

- [Documenting Architecture Decisions - Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [ADR GitHub - ADF](https://adr.github.io/)
- [12-Factor App](https://12factor.net/)

## Navigation

- [SAD (Software Architecture Document)](../201-SAD/sad.md)
- [Diagrams](../204-diagrams/)
- [Design System](../203-design/)
