# Software Architecture Document (SAD) — Notaire

> **arc42 Template v8.2** — based on [docs.arc42.org](https://docs.arc42.org/home/)
>
> **Version:** 3.0 | **Date:** 2026-08-18 | **Author:** Architecture Team

---

## Table of Contents

1. [Introduction and Goals](#1-introduction-and-goals)
2. [Constraints](#2-constraints)
3. [Context and Scope](#3-context-and-scope)
4. [Solution Strategy](#4-solution-strategy)
5. [Building Block View](#5-building-block-view)
6. [Runtime View](#6-runtime-view)
7. [Deployment View](#7-deployment-view)
8. [Cross-cutting Concepts](#8-cross-cutting-concepts)
9. [Architectural Decisions](#9-architectural-decisions)
10. [Quality Requirements](#10-quality-requirements)
11. [Risks and Technical Debt](#11-risks-and-technical-debt)
12. [Glossary](#12-glossary)

---

## 1. Introduction and Goals

### 1.1 Requirements Overview

**Notaire** is a notarial management system for Argentine escribanías (notary offices). It manages the complete lifecycle of notarial operations including:

- **Gestiones de Escritura** — Notarial deed management workflows
- **Personas** — People registry (individuals and legal entities involved in deeds)
- **Escrituras** — Deed registry with folios, copias, and testimonios
- **Presupuestos** — Budget/quote management with payment tracking
- **Trámites** — Procedure management linked to people and gestiones
- **Auditoría** — Complete audit trail for legal compliance

The system originated as a monolithic Java Swing desktop application with direct MySQL access. It is being modernized into a three-tier web architecture while preserving all 73 original use cases.

### 1.2 Quality Goals

| Priority | Quality Goal | Scenario |
|----------|-------------|----------|
| 1 | **Maintainability** | New features delivered in < 6 months; developers onboard in < 2 weeks |
| 2 | **Testability** | ≥ 80% code coverage; all changes validated by unit, integration, and E2E tests |
| 3 | **Security** | JWT authentication, coarse-grained authorization (authenticated-only; no per-role enforcement yet), full audit trail for legal compliance |
| 4 | **Scalability** | Backend scales horizontally via stateless containers |
| 5 | **Observability** | Real-time metrics, structured logs, and alerts via LPG stack |

### 1.3 Stakeholders

| Role | Expectations |
|------|-------------|
| **Escribano (Notary)** | Reliable system for daily operations; data integrity; legal compliance |
| **Gestor (Clerk)** | Efficient workflow for managing gestiones and trámites |
| **Architect / Lead Dev** | Clean architecture; testable; documented decisions (ADRs) |
| **DevOps / SRE** | Observable system; automated deployments; container orchestration |
| **Product Owner** | Feature velocity; migration from legacy without data loss |

---

## 2. Constraints

### 2.1 Technical Constraints

| Constraint | Description |
|-----------|-------------|
| Java 21 LTS | Backend runtime — mandated for long-term support |
| Spring Boot 4.1.0 | Backend framework — latest stable release |
| PostgreSQL 16 | Database — migrated from MySQL 5.7 |
| Next.js 16 / React 19 | Frontend framework with App Router |
| Docker / Docker Compose | Containerization for all services |
| Flyway | Database schema versioning — single source of truth |
| Maven 3.9+ | Build system for Java modules |

### 2.2 Organizational Constraints

| Constraint | Description |
|-----------|-------------|
| CONSTITUTION.md | Engineering process mandated for every change |
| OpenSpec SDLC | All changes require proposal → specs → design → tasks workflow |
| TDD | Failing tests written before implementation |
| Conventional Commits | Commit message format enforced by git hooks |
| ADR process | Architectural decisions documented before implementation |

### 2.3 Legal Constraints

| Constraint | Description |
|-----------|-------------|
| Audit trail | All CRUD operations must be recorded for legal compliance |
| Data retention | Notarial records must be preserved indefinitely |
| Access control | Authenticated access to sensitive notarial data (per-role enforcement not yet implemented) |

---

## 3. Context and Scope

### 3.1 Business Context

```plantuml
@startuml
title System Context — Notaire (Business View)

actor "Escribano\n(Notario)" as Escribano
actor "Gestor\n(Empleado)" as Gestor
actor "Administrador" as Admin

rectangle "Sistema Notaire" as Notaire {
}

database "Base de Datos\nNotarial" as DB

Escribano --> Notaire : Gestiones, Escrituras,\nPresupuestos, Reportes
Gestor --> Notaire : Trámites, Personas,\nDocumentos, Pagos
Admin --> Notaire : Usuarios, Roles,\nConfiguración, Auditoría

Notaire --> DB : Persistencia
@enduml
```

**External Interfaces:**

| Interface | Description |
|-----------|-------------|
| Web Browser | Next.js frontend served at `:3000` |
| REST API | Spring Boot backend at `:8080/api/v1` |
| pgAdmin | Database administration at `:5050` |
| Grafana | Monitoring dashboards at `:3001` |

### 3.2 Technical Context

```plantuml
@startuml
title System Context — Notaire (Technical View)

node "User Browser" as Browser
node "Legacy Swing Client\n(deprecated)" as Swing #LightGray

node "Frontend Container\n(Next.js 16 / :3000)" as FE
node "Backend Container\n(Spring Boot 4.1 / :8080)" as BE
database "PostgreSQL 16\n(:5432)" as DB

node "Observability Stack" {
  [Prometheus :9090] as Prom
  [Grafana :3001] as Graf
  [Loki :3100] as Loki
}

Browser --> FE : HTTPS
FE --> BE : HTTP/JSON\n(JWT Bearer)
Swing .down.> BE : HTTP/JSON\n(JWT Bearer)
BE --> DB : JDBC\n(Flyway migrations)

Prom --> BE : /actuator/prometheus
Loki --> BE : Log scraping
Graf --> Prom : Queries
Graf --> Loki : Log queries
@enduml
```

---

## 4. Solution Strategy

### 4.1 Technology Decisions

| Decision | Rationale | ADR |
|----------|-----------|-----|
| Spring Boot 4.1 + Java 21 | Modern LTS stack with strong ecosystem | ADR-001 |
| Maven multi-module | Separation of shared, backend, and frontend concerns | ADR-002 |
| URL-path API versioning (`/api/v1`) | Simple, explicit, cacheable | ADR-003 |
| PostgreSQL 16 (from MySQL 5.7) | Superior JSON support, CTEs, full-text search | ADR-004 |
| Next.js 16 + React 19 | Modern SSR/SSG, App Router, TypeScript | ADR-005 |
| TDD + JaCoCo ≥ 80% + Playwright | Quality assurance at all levels | ADR-006 |
| Flyway migrations | Versioned, reproducible schema evolution | ADR-007 |
| JWT + Spring Security | Stateless auth for multi-client support | ADR-008 |
| LPG observability stack | Lightweight, Kubernetes-ready monitoring | ADR-009 |
| `@ControllerAdvice` error handling | Uniform error responses across all endpoints | ADR-010 |
| Token-based design system | Consistent Apple-inspired UI across all forms | ADR-011 |

### 4.2 Migration Strategy

The modernization follows a **strangler fig pattern** — new features are built exclusively in the modern stack while legacy components are incrementally replaced.

```plantuml
@startuml
title Migration Strategy — Strangler Fig Pattern

rectangle "Phase 1\nAnalysis" #LightBlue {
  (Document legacy)
  (Catalog 73 use cases)
  (Schema analysis)
}

rectangle "Phase 2\nFoundation" #LightGreen {
  (notaire-shared DTOs)
  (Maven multi-module)
  (PostgreSQL + Docker)
}

rectangle "Phase 3\nBackend API" #LightYellow {
  (Spring Boot REST)
  (Spring Data JPA)
  (JWT + RBAC)
  (Flyway)
}

rectangle "Phase 4\nModern Frontend" #Orange {
  (Next.js 16)
  (Design tokens)
  (TanStack Query)
}

rectangle "Phase 5\nObservability" #Pink {
  (Prometheus + Grafana)
  (SonarQube)
  (Structured logging)
}

rectangle "Phase 6\nDeprecation" #Gray {
  (Retire Swing)
  (Remove jpa package)
}

"Phase 1\nAnalysis" -right-> "Phase 2\nFoundation"
"Phase 2\nFoundation" -right-> "Phase 3\nBackend API"
"Phase 3\nBackend API" -right-> "Phase 4\nModern Frontend"
"Phase 4\nModern Frontend" -down-> "Phase 5\nObservability"
"Phase 5\nObservability" -left-> "Phase 6\nDeprecation"
@enduml
```

### 4.3 Current Migration Status

| Phase | Status | Details |
|-------|--------|---------|
| Phase 1: Analysis | ✅ Complete | Legacy documented in `deprecated-frontend-swing/`, 73 use cases cataloged in `docs/100-business/102-use-cases/` |
| Phase 2: Foundation | ✅ Complete | `notaire-shared`, Maven multi-module, Docker Compose |
| Phase 3: Backend API | ✅ Complete | 31 controllers, 31 repositories, 32 entities, Flyway V1→V14 |
| Phase 4: Frontend | 🔄 In Progress | Next.js 16 app with login, dashboard, auditoria pages |
| Phase 5: Observability | ✅ Complete | Full LPG stack + SonarQube + Homer dashboard |
| Phase 6: Deprecation | ⬜ Planned | `deprecated-frontend-swing` excluded from Maven build, `jpa` package targeted |

---

## 5. Building Block View

### 5.1 Level 1 — System Decomposition

```plantuml
@startuml
title Building Block View — Level 1

package "Notaire System" {

  package "frontend\n(Next.js 16)" as FE {
    [App Router Pages]
    [React Components]
    [Theme / Design Tokens]
    [API Client + Hooks]
  }

  package "backend-api\n(Spring Boot 4.1)" as BE {
    [REST API Layer]
    [Business Services]
    [Data Access Layer]
    [Security & Config]
  }

  package "notaire-shared\n(Java Library)" as Shared {
    [DTOs]
    [JPA Helpers]
  }

  database "PostgreSQL 16" as DB
}

FE --> BE : HTTP/JSON (JWT)
BE --> DB : Flyway + Hibernate
BE ..> Shared : compile dependency
@enduml
```

### 5.2 Level 2 — Backend API Internal Structure

```plantuml
@startuml
title Building Block View — Level 2: backend-api

package "com.licensis.notaire" {

  package "api\n(31 REST Controllers)" as api {
    [GestionController]
    [PersonaController]
    [EscrituraController]
    [PresupuestoController]
    [UsuarioController]
    [ReporteController]
    [FolioController]
    [PagoController]
    [WorkflowDefinitionController]
    [WorkflowNodeController]
    [WorkflowTransitionController]
    [WorkflowValidationController]
    [... +19 more]
  }

  package "service\n(Business Logic)" as svc {
    [PagoService]
    [PersonaService]
    [EscrituraService]
    [PresupuestoService]
    [ReporteService]
    [RegistroAuditoriaService]
    [WorkflowTraceService]
    [WorkflowValidationService]
    [GestionQueryService]
    [AdministradorJpa]
    [AdministradorValidaciones]
  }

  package "repository\n(31 Spring Data JPA)" as repo {
    [GestionDeEscrituraRepository]
    [PersonaRepository]
    [EscrituraRepository]
    [PresupuestoRepository]
    [UsuarioRepository]
    [... +26 more]
  }

  package "negocio\n(32 Domain Entities)" as dom #LightYellow {
    [GestionDeEscritura]
    [Persona]
    [Escritura]
    [Presupuesto]
    [Tramite]
    [Usuario]
    [WorkflowDefinition]
    [WorkflowNode]
    [WorkflowTransition]
    [... +31 more]
  }

  package "jpa\n(Legacy — being replaced)" as jpa #LightGray {
    [26 JpaController classes]
    [exceptions/]
    [interfaz/]
  }

  package "config" as cfg {
    [SecurityAndCorsConfig]
    [JwtAuthenticationFilter]
    [JwtTokenService]
    [GlobalExceptionHandler]
    [OpenApiConfig]
    [ObservabilityConfig]
    [DataInitializer]
    [ProductionCredentialsGuard]
  }

  package "security" as sec {
    [LoginAttemptService]
    [PasswordEncoderUtil]
  }

  package "exception" as exc {
    [NotaireException]
    [BusinessValidationException]
    [ResourceNotFoundException]
    [ErrorResponse]
  }

  package "audit" as aud {
    [AuditoriaAspect]
    [AuditModuleResolver]
    [AuditOperationDescriber]
  }

  package "observability" as obs {
    [ApplicationHealthIndicator]
    [MetricsUtil]
    [SharedModuleMetrics]
    [StructuredLogger]
  }
}

api -down-> svc : delegates
svc -down-> repo : persists
repo -down-> dom : maps
svc .right.> jpa : legacy fallback
cfg -left-> sec : JWT config
aud --> repo : audit trail
obs ..> api : health/metrics
@enduml
```

### 5.3 Level 2 — Frontend Internal Structure

```plantuml
@startuml
title Building Block View — Level 2: frontend (Next.js 16)

package "frontend/src" {

  package "app\n(Next.js App Router)" as app {
    [layout.tsx — Root layout]
    [page.tsx — Home redirect]
    [login/ — Authentication page]
    [dashboard/ — Main dashboard]
    [auditoria/ — Audit trail view]
    [globals.css]
    [providers.tsx]
  }

  package "components" as comp {
    [layout/ — Page layouts]
    [ui/ — Design system components]
    [shared/ — Reusable elements]
    [motion/ — Animations]
  }

  package "hooks\n(20 TanStack Query hooks)" as hooks {
    [useGestiones]
    [usePersonas]
    [useEscrituras]
    [usePresupuestos]
    [usePagos]
    [useUsuarios]
    [useWorkflow]
    [useReportes]
    [useAuditoria]
    [... +11 more]
  }

  package "theme" as theme {
    [tokens.ts — Design tokens]
    [index.ts — Theme utilities]
    [form-patterns.tsx — Form components]
  }

  package "lib" as lib {
    [api-client.ts — HTTP client]
    [query-client.ts — TanStack setup]
    [logger.ts — Client logging]
    [utils.ts]
  }

  package "i18n" as i18n {
    [Locale files]
  }

  package "store" as store {
    [Client state]
  }

  package "types" as types {
    [TypeScript interfaces]
  }
}

app --> comp : renders
app --> hooks : data fetching
hooks --> lib : API calls
comp --> theme : styling
@enduml
```

### 5.4 Level 2 — Shared Module

```plantuml
@startuml
title Building Block View — Level 2: notaire-shared

package "notaire-shared" {
  package "com.licensis.notaire.dto" {
    [DTO classes — API contracts]
    [exceptions/ — Shared exceptions]
    [interfaces/ — DTO interfaces]
  }

  package "com.licensis.notaire.jpa" {
    [JPA helpers — Reusable persistence]
  }
}

note right of "notaire-shared"
  Compile dependency for backend-api.
  Contains API contracts shared between
  backend and any Java client.
end note
@enduml
```

### 5.5 Core Domain Model

```plantuml
@startuml
title Core Domain Model — Notaire

entity "GestionDeEscritura" as gestion {
  * id : Long <<PK>>
  --
  numero : String
  fecha : Date
  estado_id : Long <<FK>>
  escritura_id : Long <<FK>>
  workflow_id : Long <<FK>>
}

entity "Escritura" as escritura {
  * id : Long <<PK>>
  --
  numero : Integer
  fecha : Date
}

entity "Persona" as persona {
  * id : Long <<PK>>
  --
  nombre : String
  apellido : String
  dni : String
  email : String
}

entity "Presupuesto" as presupuesto {
  * id : Long <<PK>>
  --
  monto : BigDecimal
  fecha : Date
}

entity "Tramite" as tramite {
  * id : Long <<PK>>
  --
  nombre : String
  numero : String
  tipo_tramite_id : Long <<FK>>
}

entity "TipoDeTramite" as tipoTramite {
  * id : Long <<PK>>
  --
  nombre : String
  workflow_id : Long <<FK>>
}

entity "Folio" as folio {
  * id : Long <<PK>>
  --
  numero : String
  tipo_id : Long <<FK>>
}

entity "Pago" as pago {
  * id : Long <<PK>>
  --
  monto : BigDecimal
  fecha : Date
}

entity "Testimonio" as testimonio {
  * id : Long <<PK>>
}

entity "DocumentoPresentado" as doc {
  * id : Long <<PK>>
}

entity "Usuario" as usuario {
  * id : Long <<PK>>
  --
  username : String
  password : String (BCrypt)
}

entity "Rol" as rol {
  * id : Long <<PK>>
  --
  nombre : String
}

entity "RegistroAuditoria" as audit {
  * id : Long <<PK>>
  --
  accion : String
  modulo : String
  usuario : String
  timestamp : Timestamp
}

entity "WorkflowDefinition" as wfDef {
  * id : Long <<PK>>
  --
  nombre : String
}

entity "WorkflowNode" as wfNode {
  * id : Long <<PK>>
  --
  nombre : String
  tipo : NodeType
}

entity "WorkflowTransition" as wfTrans {
  * id : Long <<PK>>
  --
  from_node_id : Long <<FK>>
  to_node_id : Long <<FK>>
}

gestion ||--o{ tramite
gestion ||--o{ presupuesto
gestion }o--|| escritura
gestion ||--o{ folio
gestion ||--o{ doc
tramite }o--|| tipoTramite
tramite }o--o{ persona
presupuesto ||--o{ pago
escritura ||--o{ testimonio
usuario }o--|| rol
usuario ||--o{ audit
wfDef ||--o{ wfNode
wfDef ||--o{ wfTrans
tipoTramite }o--o| wfDef
@enduml
```

---

## 6. Runtime View

### 6.1 User Authentication Flow

```plantuml
@startuml
title Runtime — User Authentication

participant "Browser" as B
participant "Next.js\nFrontend" as FE
participant "SecurityAndCorsConfig\n+ JwtAuthenticationFilter" as Sec
participant "UsuarioController" as Ctrl
participant "LoginAttemptService\n(in-memory, per-username)" as Login
participant "AuditoriaAspect" as Audit
participant "JwtTokenService" as JWT
participant "PostgreSQL" as DB

B -> FE : Login form submit
FE -> Sec : POST /api/v1/usuario/login\n{nombre, contrasenia}
Sec -> Ctrl : Pass through (public endpoint)
Ctrl -> Login : isLocked(nombre)
Login --> Ctrl : locked? (ConcurrentHashMap lookup, no DB access)
alt Too many attempts (5 failures / 15 min lockout)
  Ctrl --> FE : 429 Too Many Requests
else
  Ctrl -> DB : findByNombre(nombre)
  DB --> Ctrl : Usuario entity
  Ctrl -> Ctrl : BCrypt.matches(contrasenia)\n(or legacy MD5 auto-migration)
  alt Valid credentials
    Ctrl -> Login : onLoginSucceeded(nombre)
    Ctrl -> JWT : generateToken(nombre)
    JWT --> Ctrl : JWT string
    Ctrl -> Audit : AOP around login() —\nINSERT registro_auditoria (LOGIN)
    Audit -> DB : INSERT registro_auditoria
    Ctrl --> FE : 200 {valido, token, idUsuario,\nnombre, estado, tipo, version, personas}
    FE -> FE : Store token in localStorage\n(Zustand persist, useAuthStore)
    FE --> B : Redirect to /dashboard
  else Invalid credentials
    Ctrl -> Login : onLoginFailed(nombre)
    Ctrl --> FE : 200 {valido: false}
  end
end
@enduml
```

### 6.2 Create Gestión de Escritura

```plantuml
@startuml
title Runtime — Crear nueva Gestión de Escritura

participant "Escribano" as User
participant "Frontend\n(Next.js)" as Client
participant "JwtAuthFilter" as Auth
participant "GestionController" as Ctrl
participant "AuditoriaAspect" as Audit
participant "GestionDeEscrituraRepository" as Repo
participant "PostgreSQL" as DB

User -> Client : Fill gestión form + submit
Client -> Auth : POST /api/v1/gestion\nAuthorization: Bearer <jwt>
Auth -> Auth : Validate JWT signature + expiry
Auth -> Ctrl : Authorized request (user context)
Ctrl -> Repo : save(entity)\n(controller calls repository directly —\nno intermediate service layer for this endpoint)
Repo -> DB : INSERT INTO gestion_de_escritura
DB --> Repo : Generated ID
Repo --> Ctrl : Saved entity
Ctrl -> Audit : AOP around create() —\nINSERT INTO registro_auditoria\n(CREATE, gestion, user, timestamp)
Audit -> DB : INSERT INTO registro_auditoria
Ctrl --> Client : 201 Created {entity}
Client --> User : Show success + gestión number
@enduml
```

### 6.3 Report Generation

```plantuml
@startuml
title Runtime — Generate Report

participant "User" as U
participant "Frontend" as FE
participant "ReporteController" as Ctrl
participant "ReporteService" as Svc
participant "JasperReports\n(JasperFillManager/ExportManager)" as Jasper
participant "PostgreSQL\n(via DataSource, JDBC)" as DB

U -> FE : Select report + parameters
FE -> Ctrl : GET /api/v1/reportes/{report-name}/{params}\n(one endpoint per report, e.g. /presupuesto/{id})
Ctrl -> Svc : generarReporte...(params)
Svc -> DB : JDBC query via .jasper template\n(src/main/resources/reportes/)
DB --> Svc : Result set
Svc -> Jasper : JasperFillManager.fillReport() +\nJasperExportManager.exportReportToPdf()
Jasper --> Svc : byte[] (PDF)
Svc --> Ctrl : byte[] (PDF)
Ctrl --> FE : 200 application/pdf
FE --> U : Open/download PDF
@enduml
```

---

## 7. Deployment View

### 7.1 Development Environment

```plantuml
@startuml
title Deployment View — Docker Compose (Development)

node "Docker Host (Developer Machine)" {

  node "docker-compose.yml\n(Application Stack)" {
    artifact "notaire-frontend\n:3000" as FE <<Next.js 16>>
    artifact "notaire-backend\n:8080" as BE <<Spring Boot 4.1>>
    database "notary-postgres\n:5432" as DB <<PostgreSQL 16>>
    artifact "notary-pgadmin\n:5050" as PGA <<pgAdmin 4>>
  }

  node "infra/docker-compose.yml\n(DevSecOps Stack)" {
    artifact "devsecops-prometheus\n:9090" as Prom <<Prometheus>>
    artifact "devsecops-grafana\n:3001" as Graf <<Grafana>>
    artifact "devsecops-loki\n:3100" as Loki <<Loki>>
    artifact "devsecops-promtail" as Tail <<Promtail>>
    artifact "devsecops-sonarqube\n:9000" as Sonar <<SonarQube>>
    artifact "devsecops-postgres-exporter\n:9187" as PGExp <<postgres-exporter>>
    artifact "devsecops-dashboard\n:8888" as Homer <<Homer>>
    database "devsecops-sonar-db" as SonarDB <<PostgreSQL 15>>
  }
}

actor "Developer" as Dev

Dev --> FE : http://localhost:3000
Dev --> BE : http://localhost:8080
Dev --> PGA : http://localhost:5050
Dev --> Graf : http://localhost:3001
Dev --> Prom : http://localhost:9090
Dev --> Sonar : http://localhost:9000
Dev --> Homer : http://localhost:8888

FE --> BE : NEXT_PUBLIC_API_URL
BE --> DB : JDBC + Flyway
PGA --> DB : Admin queries

Prom --> BE : /actuator/prometheus
Prom --> PGExp : :9187/metrics
PGExp --> DB : pg_monitor role (V12 migration)
Tail --> Loki : Push logs (Docker socket)
Graf --> Prom : Metric dashboards
Graf --> Loki : Log queries
Sonar --> SonarDB : Analysis storage
@enduml
```

### 7.2 Network Configuration

| Network | Services | Purpose |
|---------|----------|---------|
| `notary-network` | postgres, backend, pgadmin, frontend | Application communication |
| `devsecops-network` | prometheus, grafana, loki, sonarqube | DevSecOps internal |
| `notaire_notary-network` (external) | Shared between app and infra stacks | Cross-stack monitoring |

### 7.3 Container Resource Limits

| Container | Memory Limit | Memory Reserved |
|-----------|-------------|----------------|
| `notaire-backend` | 512 MB | 256 MB |
| All others | Docker defaults | — |

### 7.4 Startup Dependencies

```plantuml
@startuml
title Container Startup Order

[postgres] as DB
[backend] as BE
[frontend] as FE
[pgadmin] as PGA
[prometheus] as Prom
[grafana] as Graf
[loki] as Loki

DB --> BE : depends_on (healthy)
BE --> FE : depends_on (healthy)
DB --> PGA : depends_on (healthy)
DB --> Prom : via postgres-exporter
Prom --> Graf : depends_on
Loki --> Graf : depends_on
@enduml
```

---

## 8. Cross-cutting Concepts

### 8.1 Security

**Authentication:**
- JWT tokens issued by `JwtTokenService` upon successful login.
- `JwtAuthenticationFilter` validates tokens on every request.
- Token storage: `localStorage` via Zustand `persist` middleware (`useAuthStore`,
  Next.js); a separate non-JWT `notaire-auth-status` cookie exists only so the
  Next.js middleware can route-guard pages. Swing client stores the token in-memory
  (`RestClient`, static field).
- `LoginAttemptService` locks out a username after repeated failed attempts
  (in-memory, single-instance — not a general API rate limiter).
- `ProductionCredentialsGuard` blocks startup if default passwords are used in production.

**Authorization:**
- Coarse-grained only: every `/api/**` request must be authenticated (valid JWT);
  there is no per-role authorization yet — no `@PreAuthorize` annotations exist in
  the codebase.
- The `Rol` entity/table exists for future RBAC; extending enforcement means
  replacing `.anyRequest().authenticated()` with per-route role checks in
  `SecurityAndCorsConfig` (see [API Authentication Guide](../206-security/API-AUTHENTICATION-GUIDE.md#extending-rbac)).

**API Security:**
- CORS configured in `SecurityAndCorsConfig`.
- All endpoints under `/api/v1/**` require JWT (except login).
- Actuator endpoints secured with separate credentials.

### 8.2 Error Handling

All errors follow a uniform response structure defined in `ErrorResponse`:

```json
{
  "timestamp": "2026-08-18T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/persona",
  "code": "VAL_001",
  "details": { "field": "dni", "message": "Must be numeric" }
}
```

**Exception Hierarchy:**
- `NotaireException` — Base exception
- `BusinessValidationException` — 400 Bad Request
- `ResourceNotFoundException` — 404 Not Found
- Global handling via `GlobalExceptionHandler` (`@ControllerAdvice`)

### 8.3 Audit Trail

- `AuditoriaAspect` (Spring AOP) intercepts annotated controller methods.
- `AuditModuleResolver` maps controllers to functional modules.
- `AuditOperationDescriber` generates human-readable operation descriptions.
- Records stored in `registro_auditoria` table.
- Queryable via `RegistroAuditoriaController` → `/api/v1/auditoria`.
- Frontend audit view at `/auditoria` page.

### 8.4 Database Schema Management

- **Flyway** is the single source of truth for database schema.
- Migrations located at `backend-api/src/main/resources/db/migration/`.
- Docker starts PostgreSQL **empty**; Flyway applies V1→V14 sequentially on backend startup.
- Historical `init-db/` scripts archived at `docs/archive/init-db/`.
- Repeatable migrations (`R__`) used for reversible operations.

**Current Migrations:**

| Version | Description |
|---------|------------|
| V1 | Initial schema (all tables) |
| V2 | Initial seed data |
| V3 | Add missing entity columns |
| V4 | Fix missing schema columns |
| V5 | Fix movimientos_testimonio schema |
| V6 | Make documentos_presentados tramite optional |
| V7 | Add workflow tables |
| V8 | Add workflow to tipo_tramite |
| V9 | Add roles and permissions |
| V10 | Seed workflow demo data |
| V11 | Align conceptos version with init-db |
| V12 | Create postgres_exporter role (pg_monitor) |
| V13 | Make tramites nombre/numero optional |
| V14 | Drop presupuestos FK to tramite |
| R14 | Restore presupuestos FK (repeatable) |

### 8.5 Observability

**Metrics (Prometheus):**
- Spring Boot Actuator exposes `/actuator/prometheus`.
- Custom metrics via `MetricsUtil` and `SharedModuleMetrics`.
- `ApplicationHealthIndicator` for custom health checks.
- `postgres-exporter` provides database-level metrics.

**Logging (Loki):**
- Structured JSON logging via `StructuredLogger`.
- Logback configuration with JSON appender.
- Promtail collects logs from Docker containers.
- Centralized viewing in Grafana.

**Dashboards (Grafana :3001):**
- Application metrics (JVM, HTTP, custom).
- Database metrics (connections, query time, table sizes).
- Log exploration and alerting.

**Code Quality (SonarQube :9000):**
- Static analysis on every build.
- Coverage: enforced ratchet floor via JaCoCo (raised as coverage improves; long-term
  target 80% line / 80% branch — see [Code Quality](../../300-development/303-testing/README.md)).

### 8.6 Design System (Frontend)

- **Single source of truth**: `frontend/src/theme/tokens.ts`.
- **Design language**: Apple-inspired (macOS Sequoia / iOS 18).
- **Token categories**: Colors (neutral 0-900, semantic), typography (SF Pro), spacing (8px grid), border-radius, shadows, transitions.
- **Form pattern**: `FormContainer → FormSection → FormField → FormActions`.
- **Rule**: No hardcoded CSS values — all styling via tokens.

### 8.7 API Design

- All endpoints under `/api/v1/` prefix.
- RESTful resource naming (Spanish domain: `/gestion`, `/persona`, `/escritura`).
- OpenAPI 3.0 documentation via `OpenApiConfig` at `/swagger-ui.html`.
- Response wrapping with DTOs from `notaire-shared`.
- Pagination support on list endpoints.

### 8.8 Testing Strategy

| Level | Tool | Scope | Target |
|-------|------|-------|--------|
| Unit (backend) | JUnit 5 + Mockito | Service/repository logic | Enforced ratchet floor (target 80%) |
| Unit / Component (frontend) | Vitest + Testing Library | `frontend/src/**/*.test.ts(x)` | 19+ test files |
| Integration | Spring Boot Test | Controller + DB (H2/PG) | API contract validation |
| API | Bruno, yml collection format (`backend-api/api-test/`) | Full API endpoints, per use case | See `CU-API-MATRIX.csv` |
| E2E (UI/UX) | Playwright | Frontend user flows, per use case (`cuNN-*.spec.ts`) | 33+ specs, critical paths |
| Static Analysis | Checkstyle + SpotBugs + SonarQube | Code quality | Zero critical issues |

Full pyramid and use-case traceability documented in
[`docs/300-development/303-testing/README.md`](../../300-development/303-testing/README.md).

### 8.9 Legacy Coexistence (`jpa` package)

The `jpa` package contains 26 `*JpaController` classes — a legacy data-access layer from the original Swing application. These classes contain raw EntityManager operations and are being incrementally replaced by:

1. **Spring Data JPA Repositories** (`repository` package) — declarative query methods.
2. **Service classes** (`service` package) — business logic extracted from JPA controllers.

**Migration rule**: New code **must not** use `jpa` package classes. Services may temporarily delegate to them as fallback during the transition.

---

## 9. Architectural Decisions

All decisions are documented as Architecture Decision Records (ADRs) in `docs/200-architecture/202-ADR/`.

| ADR | Title | Status | Scope |
|-----|-------|--------|-------|
| [ADR-001](../202-ADR/ADR-001-microservices-architecture.md) | Microservices Architecture | Accepted | System architecture |
| [ADR-002](../202-ADR/ADR-002-module-structure.md) | Module Structure | Accepted | Code organization |
| [ADR-003](../202-ADR/ADR-003-rest-api-versioning.md) | REST API Versioning | Accepted | API evolution |
| [ADR-004](../202-ADR/ADR-004-database-migration.md) | Database Migration | Accepted | Data persistence |
| [ADR-005](../202-ADR/ADR-005-modern-frontend-migration.md) | Modern Frontend Migration | Accepted | Frontend architecture |
| [ADR-006](../202-ADR/ADR-006-testing-strategy.md) | Testing Strategy | Accepted | QA & validation |
| [ADR-007](../202-ADR/ADR-007-database-schema-versioning-flyway.md) | Database Schema Versioning (Flyway) | Accepted | Data persistence |
| [ADR-008](../202-ADR/ADR-008-security-authentication.md) | Security & Authentication | Accepted | Security |
| [ADR-009](../202-ADR/ADR-009-logging-monitoring.md) | Logging & Monitoring | Accepted | Observability |
| [ADR-010](../202-ADR/ADR-010-error-handling.md) | Error Handling | Accepted | Resilience |
| [ADR-011](../202-ADR/ADR-011-centralized-design-system.md) | Centralized Design System | Accepted | UI/UX |
| [ADR-012](../202-ADR/ADR-012-ci-cd-pipeline.md) | CI/CD Pipeline Strategy | Accepted | DevOps |
| [ADR-013](../202-ADR/ADR-013-audit-trail.md) | Audit Trail Implementation | Accepted | Compliance |
| [ADR-014](../202-ADR/ADR-014-workflow-engine.md) | Workflow Engine | Accepted | Business logic |
| [ADR-015](../202-ADR/ADR-015-internationalization.md) | Internationalization | Accepted | UX |
| [ADR-016](../202-ADR/ADR-016-observability-stack.md) | Observability Stack Topology | Accepted | Observability |
| [ADR-017](../202-ADR/ADR-017-container-base-images.md) | Container / Base-Image Strategy | Accepted | DevOps |
| [ADR-018](../202-ADR/ADR-018-rate-limiting-policy.md) | Rate-Limiting Policy | Accepted | Security |
| [ADR-019](../202-ADR/ADR-019-secrets-management.md) | Secrets Management | Accepted | Security |
| [ADR-020](../202-ADR/ADR-020-openapi-exposure-policy.md) | OpenAPI Exposure Policy | Accepted | Security & API |

---

## 10. Quality Requirements

### 10.1 Quality Tree

```plantuml
@startuml
title Quality Tree — Notaire

rectangle "System Quality" {
  rectangle "Functional\nSuitability" as FS
  rectangle "Reliability" as R
  rectangle "Security" as S
  rectangle "Maintainability" as M
  rectangle "Performance\nEfficiency" as P
  rectangle "Operability" as O
}

FS --> (Correctness)
FS --> (Completeness)

R --> (Availability 99.5%)
R --> (Fault Tolerance)

S --> (Authentication JWT)
S --> (Authorization: authenticated-only)
S --> (Audit Trail)

M --> (Modularity)
M --> (Testability ≥80%)
M --> (Analyzability)

P --> (Response <500ms)
P --> (Resource limits)

O --> (Observability LPG)
O --> (Deployability Docker)
@enduml
```

### 10.2 Quality Scenarios

| ID | Quality Attribute | Scenario | Metric |
|----|------------------|----------|--------|
| QS-01 | Performance | API response for single-entity CRUD | < 500ms avg |
| QS-02 | Performance | Report generation with complex queries | < 3s |
| QS-03 | Availability | System uptime during business hours | 99.5% |
| QS-04 | Security | All create/update/delete operations (and logins) logged in audit trail; read-only GETs excluded | 100% coverage of mutating operations |
| QS-05 | Security | Failed login attempts trigger a per-username lockout (`LoginAttemptService`, single-instance, not a general API rate limiter) | 5 failed attempts → 15 min lockout |
| QS-06 | Testability | Code coverage across backend | ≥ 80% |
| QS-07 | Maintainability | New feature development time | < 6 months |
| QS-08 | Maintainability | Developer onboarding time | < 2 weeks |
| QS-09 | Scalability | Backend horizontal scaling | Stateless containers |
| QS-10 | Deployability | Full stack startup time | < 3 minutes |

---

## 11. Risks and Technical Debt

### 11.1 Current Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Legacy `jpa` package coexists with modern `repository` | High — code duplication, inconsistent patterns | Certain | Incremental migration per entity; new code uses only `repository` |
| `ControllerNegocio.java` (5,337 lines, ~193 KB) — God class in `negocio` | High — unmaintainable, untestable | Certain | Extract to service classes; scheduled for refactoring |
| `deprecated-frontend-swing` still referenced but excluded from build | Low — confusion for new developers | Low | Document deprecation; remove after Next.js frontend is complete |
| No production deployment target defined | Medium — no deployment pipeline to production | Medium | Define production Docker Compose or Kubernetes manifests |
| Default credentials in `.env` | Critical — security risk if deployed as-is | Medium | `ProductionCredentialsGuard` blocks startup with defaults |

### 11.2 Technical Debt Inventory

| Item | Location | Severity | Effort |
|------|----------|----------|--------|
| 26 legacy JPA controllers | `com.licensis.notaire.jpa` | High | Large — one per entity |
| `ControllerNegocio.java` God class (5,337 lines, ~193 KB) | `negocio/ControllerNegocio.java` | Critical | Large |
| `AdministradorJpa` in service layer | `service/AdministradorJpa.java` | Medium | Medium |
| `AdministradorValidaciones` mixed concerns | `service/AdministradorValidaciones.java` | Medium | Medium |
| Missing service classes for some entities | `service/` | Medium | Medium |
| `deprecated-frontend-swing` module deprecated but present | `deprecated-frontend-swing/` | Low | Small |
| `ConstantesGui.java` in audit package | `audit/ConstantesGui.java` | Low | Small |

### 11.3 Evolution Roadmap

1. **Short term** — Complete Next.js frontend pages for all 73 use cases.
2. **Short term** — Extract `ControllerNegocio` logic into dedicated service classes.
3. **Medium term** — Replace all `jpa` package controllers with `repository` + `service`.
4. **Medium term** — Remove `deprecated-frontend-swing` module.
5. **Long term** — Add Kubernetes deployment manifests for production.
6. **Long term** — Implement WebSocket support for real-time workflow updates.
7. **Long term** — Add batch processing for report generation.

---

## 12. Glossary

| Term | Definition |
|------|-----------|
| **Escribanía** | Argentine notary office |
| **Escribano** | Licensed notary public (primary system user) |
| **Gestor** | Clerk/assistant who manages daily procedures |
| **Gestión de Escritura** | Notarial deed management case — the core business entity |
| **Escritura** | Notarial deed (legal document) |
| **Folio** | Numbered page in the notarial protocol book |
| **Testimonio** | Certified copy of a notarial deed |
| **Copia** | Copy of a folio or deed |
| **Presupuesto** | Budget/quote for notarial services |
| **Trámite** | Administrative procedure linked to a gestión |
| **Persona** | Individual or legal entity involved in notarial acts |
| **Inmueble** | Real estate property involved in notarial acts |
| **API REST** | HTTP/JSON interface between frontend and backend |
| **DTO** | Data Transfer Object — API contract between layers |
| **JPA** | Java Persistence API (Hibernate implementation) |
| **JWT** | JSON Web Token for stateless authentication |
| **RBAC** | Role-Based Access Control |
| **Flyway** | Database schema migration tool |
| **LPG Stack** | Loki + Prometheus + Grafana observability stack |
| **TDD** | Test-Driven Development |
| **ADR** | Architecture Decision Record |
| **OpenSpec** | Specification tool for SDLC workflow |

---

## References

### Project Documents
- [CONSTITUTION.md](../../../CONSTITUTION.md) — Engineering process
- [AGENTS.md](../../../AGENTS.md) — Agent configuration
- [CHANGELOG.md](../../../CHANGELOG.md) — Version history
- [ADR Index](../202-ADR/README.md) — All architectural decisions
- [Design System](../203-design/FRONTEND-DESIGN-SYSTEM.md) — Frontend design guide
- [ADR-007](../202-ADR/ADR-007-database-schema-versioning-flyway.md) — Flyway implementation details

### External References
- [arc42 Template](https://docs.arc42.org/home/)
- [Spring Boot 4.1 Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Next.js Documentation](https://nextjs.org/docs)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL 16 Documentation](https://www.postgresql.org/docs/16/)

### Diagram Sources
All PlantUML diagram sources are in `docs/200-architecture/204-diagrams/`:
- `architecture-legacy.puml` — Legacy monolithic architecture
- `architecture-target.puml` — Target three-tier architecture
- `deployment-docker.puml` — Docker Compose deployment
- `backend-package-structure.puml` — Backend internal structure
- `data-model-core.puml` — Core entity relationships
- `migration-phases.puml` — Migration phases overview
- [`Casos de Uso/`](../204-diagrams/Casos%20de%20Uso/) — Use-case diagrams grouped by module (gestiones, administración, clientes, pagos, protocolos, presupuestos)
