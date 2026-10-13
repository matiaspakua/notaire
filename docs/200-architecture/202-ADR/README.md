# Architecture Decision Records (ADRs)

Architecture Decision Records document the important architecture decisions for Notaire: the context, the options considered, and the trade-offs of each decision.

## ADR index

| # | Title | Status | Date | Scope |
|---|-------|--------|------|-------|
| [001](ADR-001-microservices-architecture.md) | Microservices architecture | Accepted | 2024-03-20 | System architecture |
| [002](ADR-002-module-structure.md) | Module structure | Accepted | 2024-03-20 | Code organization |
| [003](ADR-003-rest-api-versioning.md) | REST API versioning | Accepted | 2024-03-20 | API evolution |
| [004](ADR-004-database-migration.md) | Database migration | Accepted | 2024-03-20 | Data persistence |
| [005](ADR-005-modern-frontend-migration.md) | Modern frontend migration | Accepted | 2026-04-13 | Frontend architecture |
| [006](ADR-006-testing-strategy.md) | Testing strategy | Accepted | 2026-04-13 | QA and validation |
| [007](ADR-007-database-schema-versioning-flyway.md) | Database schema versioning (Flyway) | Accepted | 2026-04-14 | Data persistence |
| [008](ADR-008-security-authentication.md) | Security and authentication | Accepted | 2024-04-28 | Security |
| [009](ADR-009-logging-monitoring.md) | Logging and monitoring | Accepted | 2024-04-28 | Observability |
| [010](ADR-010-error-handling.md) | Error handling | Accepted | 2024-04-28 | Resilience |
| [011](ADR-011-centralized-design-system.md) | Centralized design system | Accepted | 2026-05-12 | UI/UX |
| [012](ADR-012-ci-cd-pipeline.md) | CI/CD pipeline (GitHub Actions) | Accepted | 2026-03-24 | DevOps |
| [013](ADR-013-audit-trail.md) | Audit trail (Spring AOP) | Accepted | 2026-05-12 | Security and compliance |
| [014](ADR-014-workflow-engine.md) | Workflow engine (data-driven) | Accepted | 2026-06-09 | Domain model |
| [015](ADR-015-internationalization.md) | Internationalization (next-intl) | Accepted | 2026-05-23 | Frontend |
| [016](ADR-016-observability-stack.md) | Observability stack topology | Accepted | 2026-08-19 | Observability |
| [017](ADR-017-container-base-images.md) | Container and base-image strategy | Accepted | 2026-08-19 | DevOps |
| [018](ADR-018-rate-limiting-policy.md) | Rate-limiting policy | Accepted | 2026-08-19 | Security |
| [019](ADR-019-secrets-management.md) | Secrets management | Accepted | 2026-08-19 | Security |
| [020](ADR-020-openapi-exposure-policy.md) | OpenAPI exposure policy | Accepted | 2026-08-19 | Security and API |
| [021](ADR-021-hexagonal-architecture-pilot.md) | Hexagonal architecture pilot (payment slice) | Accepted | 2026-09-13 | Code organization |
| [022](ADR-022-git-history-rewrite-and-large-binaries.md) | Git history rewrite and large binaries | Accepted | 2026-10-03 | DevOps and repo hygiene |
| [023](ADR-023-rest-resource-naming.md) | REST resource naming conventions | Accepted | 2026-10-03 | API design |
| [024](ADR-024-repository-topology.md) | Repository topology — evidence-gated decomposition | Proposed | 2026-10-05 | Repository structure |
| [025](ADR-025-retire-notaire-shared.md) | Retire the notaire-shared module | Accepted | 2026-10-05 | Code organization |
| [026](ADR-026-module-separation.md) | Module separation inside the repository | Accepted | 2026-10-06 | Code organization |
| [027](ADR-027-mermaid-diagrams.md) | Mermaid as the canonical diagram language | Accepted | 2026-10-10 | Documentation |
| [028](ADR-028-release-and-version-identity.md) | Release and version identity | Accepted | 2026-10-13 | DevOps and release |

Dates for ADR-005, ADR-006, and ADR-007 match the date in each decision file.

## Status legend

- **Proposed** — under evaluation, not implemented yet
- **Accepted** — approved and in use
- **Deprecated** — was valid, and has been retired
- **Superseded by** — replaced by another ADR

## How to read an ADR

Each ADR uses this structure:

### Header

- **Status**: Proposed, Accepted, Deprecated, or Superseded
- **Date**: when it was proposed
- **Deciders**: who made the decision
- **Related**: connected ADRs

### Body

1. **Context** — the problem and the constraints
2. **Decision** — the chosen option and why
3. **Options considered** — alternatives and their trade-offs
4. **Trade-off analysis** — the main trade-offs in detail
5. **Consequences** — what becomes easier, what becomes harder, and what needs follow-up
6. **Implementation details** — technical notes for carrying out the decision
7. **Related ADRs** — other connected decisions

## Decision categories

### System architecture

- **ADR-001**: move from the monolith to a layered service
- **ADR-005**: Next.js for the web frontend

### Code organization

- **ADR-002**: multi-module Maven layout (partly replaced by ADR-025)
- **ADR-021**: hexagonal pilot (the payment and budget slice)
- **ADR-025**: retire `notaire-shared`; `backend-api` owns its DTOs
- **ADR-026**: module separation inside this repository

### API design

- **ADR-003**: REST versioning by URL path
- **ADR-023**: resource naming (language, plurals, search, actions, 201 + Location)

### Repository structure

- **ADR-024**: topology (staged, evidence-gated extraction of satellites; the core stays a monorepo)

### Data persistence

- **ADR-004**: MySQL to PostgreSQL 16
- **ADR-007**: schema versioning with Flyway

### Quality and reliability

- **ADR-006**: testing strategy (unit, integration, E2E)
- **ADR-008**: security and authentication (JWT, RBAC)
- **ADR-010**: global error handling

### Observability

- **ADR-009**: centralized logging and monitoring (Loki, Prometheus, Grafana)
- **ADR-016**: observability stack topology (Prometheus, Grafana, Loki, SonarQube)

### DevOps

- **ADR-012**: CI/CD with GitHub Actions
- **ADR-017**: container base images (Alpine, multi-stage, non-root)
- **ADR-022**: large-binary hygiene and a deferred `git filter-repo` decision (#1050)

### Security and compliance

- **ADR-013**: cross-cutting audit with Spring AOP
- **ADR-018**: rate limiting (login lockout only, no general API limit)
- **ADR-019**: secrets (one `.env`, `ProductionCredentialsGuard`)
- **ADR-020**: OpenAPI and Swagger exposure (public except in production)

### Domain model

- **ADR-014**: configurable workflow engine for managements

### Frontend

- **ADR-011**: centralized design system
- **ADR-015**: internationalization with next-intl. Message keys such as `suplencias` and `reportes` stay Spanish.

### Documentation

- **ADR-027**: Mermaid for active diagrams

## Principles drawn from the accepted ADRs

### 1. Separation of concerns

- Frontend and backend are separate
- They talk only through the REST API
- The database is reached only from the backend

### 2. Scalability

- The backend is stateless so it can scale horizontally
- Database connections are pooled
- The API is versioned so clients are not broken by evolution

### 3. Maintainability

- Code is organized in Maven modules
- Layers run from controllers to services to repositories to the database
- Responsibilities have a clear boundary

### 4. Current platform

From the root `pom.xml` and Compose:

- Java 26
- Spring Boot 4.1.1
- PostgreSQL 16
- Docker

## ADR lifecycle

```text
Proposed
    │ (analysis)
    ↓
Accepted
    │ (implementation)
    ├─→ Deprecated (retired)
    │
    └─→ Superseded (by a new ADR)
```

### Proposing a new ADR

1. **Name the decision**: which architecture question needs an answer?
2. **Create the file**: `ADR-NNN-title.md`
3. **Write it fully**:
   - A clear context (problem and constraints)
   - At least two or three options
   - A trade-off analysis
   - Consequences and follow-up
4. **Get agreement**: team review
5. **Implement**: follow the recorded decision
6. **Update this README**: add the row and the status

## Decisions that cut across several ADRs

### Layered architecture

- ADR-001 (layered service)
- ADR-002 (package structure)
- ADR-006 (tests at each layer)

### Data integrity

- ADR-004 (PostgreSQL)
- ADR-007 (Flyway)
- ADR-010 (error handling)

### API evolution

- ADR-003 (versioning)
- ADR-008 (authentication)
- ADR-023 (resource names)

## Targets used to judge the decisions

These are targets, not a measured report:

- **Performance**: API response times under 500 ms on average
- **Reliability**: 99.5% uptime
- **Maintainability**: new features shipped inside six months
- **Quality**: 80% code coverage, fewer than five critical bugs per quarter
- **Developer velocity**: feature delivery rate

## References

- [Documenting Architecture Decisions - Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [ADR GitHub](https://adr.github.io/)
- [12-Factor App](https://12factor.net/)

## Navigation

- [SAD (Software Architecture Document)](../201-SAD/sad.md)
- [Diagrams](../204-diagrams/)
- [Design](../203-design/)
