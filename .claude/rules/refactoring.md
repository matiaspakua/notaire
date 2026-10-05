---
title: Refactoring Rules — Current Architecture
description: Rules for refactoring within the current Notaire stack (Spring Boot 4.1 + Next.js)
alwaysApply: true
---

## Architecture Overview

- **Legacy origin:** Monolithic Java 1.6 desktop app with direct DB access and a tightly coupled Swing GUI (removed from the tree under #1046; history remains in git — do not recreate it).
- **Current target:** Three-tier system — PostgreSQL 16, Spring Boot 4.1 REST API (`backend-api`), Next.js 16 web client (`frontend/`).
- **Communication:** Clients call the API over HTTPS/HTTP JSON (`/api/v1/...`); no direct DB access from any client.
- **Deployment:** Database, backend, and frontend as Docker services; secrets only in the git-ignored `.env`.
- **Schema:** Flyway is the single source of truth (`ddl-auto=none`). Never edit applied migrations — add a new `V{n}__…`.

## Module Structure

### Backend (`backend-api`)

- Package root: `com.licensis.notaire`
- Framework: Spring Boot 4.1 with Java 21
- Responsibilities: business logic, validation, persistence, REST endpoints, security
- No Swing (or any desktop UI) dependencies
- Stateless and horizontally scalable
- Prefer Spring Data `repository` over the legacy `jpa` package for new data access

### DTO contracts (in `backend-api`)

- DTOs live in `backend-api` (`com.licensis.notaire.dto`); the `notaire-shared` module was retired (ADR-025)
- External services and clients consume the REST API (`/api/v1`, OpenAPI), never Java DTO classes
- Naming: `DtoEntityName` (e.g. `DtoUsuario`, `DtoPersona`) — never `*RequestDTO` / `*ResponseDTO` suffixes

### Frontend (`frontend/`)

- Next.js 16 + React 19 + TypeScript + Tailwind
- Responsibilities: UI, client-side UX validation, API client calls
- No JDBC/SQL and no business rules in event handlers
- Use the design system: `frontend/src/theme/tokens.ts` and
  `FormContainer → FormSection → FormField → FormActions`

### Database

- PostgreSQL 16 in Docker
- Only the backend accesses the database (HikariCP)
- Clients never hold connection strings or run SQL

## Package Layout (backend)

| Package | Role |
|---------|------|
| `adapter.in.web` | REST controllers (`@RestController`) |
| `service` / `application` | Use-case / application services |
| `repository` | Spring Data JPA — **use for new code** |
| `business` / `domain` | JPA entities and domain model |
| `jpa` | Legacy `*JpaController` data-access — **do not extend**; migrate callers to `repository` + `service` |
| `config` | Spring configuration |
| `security` | Authn/authz (JWT) |

When hexagonal ports/adapters already exist for a capability (`application.port`,
`adapter.out`), extend that shape rather than inventing a parallel layer.

## REST API Design

### Endpoint conventions

- Base URL: `/api/v1`
- Resource naming: plural nouns
- Methods: GET / POST / PUT / DELETE / PATCH
- Status codes: 200, 201, 204, 400, 401, 403, 404, 409, 500 as appropriate
- JSON responses; consistent error body (no internal stack traces to clients)

### URL structure

- Collections: `GET /api/v1/documents`
- Single resource: `GET /api/v1/documents/{id}`
- Nested: `GET /api/v1/notaries/{id}/documents`
- Filtering / pagination / search via query params (`page`, `size`, `sort`, …)

### DTOs

- Package: `com.licensis.notaire.dto` (in `backend-api`)
- Naming: `DtoEntityName`
- Bean Validation (`jakarta.validation`) on request DTOs
- Never expose JPA entities directly from controllers
- Map entities ↔ DTOs with dedicated mappers (manual or MapStruct)

Every new REST endpoint must be reachable from the UI at least once and
documented in OpenAPI/Swagger (CONSTITUTION.md §4).

## Backend Implementation Rules

### Controllers

- Thin: validate input (`@Valid`), call a service, return `ResponseEntity`
- Constructor injection only
- No business logic, no `EntityManager`, no direct repository calls when a
  service already owns the use case

### Services

- Own business rules and transactions (`@Transactional` where needed)
- No HTTP types (`HttpServletRequest`, `ResponseEntity`) inside services
- Throw domain/application exceptions; map them in `@ControllerAdvice`

### Repositories

- Extend `JpaRepository` / Spring Data interfaces
- Query methods or `@Query` only — no business rules

### Legacy `jpa` package

- Treat as migration debt: wrap or replace with `repository` + `service`
- Do not add new `*JpaController` classes or expand their APIs
- Prefer extracting behaviour into services covered by unit tests first (TDD)

## Frontend Implementation Rules

- Call the backend through the existing API client utilities under `frontend/src/lib/`
- Handle loading, error, and empty states explicitly
- Keep auth tokens out of logs and local storage patterns that contradict
  `docs/200-architecture/206-security/`
- For forms, follow `.claude/rules/ui-ux-design.md` and the frontend design skill

## Docker & Configuration

- Backend image: Eclipse Temurin 21 JRE; expose 8080; health via `/actuator/health`
- Postgres image: `postgres:16`; data on a named volume
- Compose services read credentials from `.env` (never hard-code secrets)
- Profiles: `dev`, `test`, `prod`; production must not use `ddl-auto=create/update`

## Security

- Authenticate API calls with JWT (Bearer); authorize on the server
- Validate and sanitize all input server-side
- HTTPS in production; no secrets in source or docs
- Do not log credentials, tokens, or PII

## Testing (mandatory with every refactor)

- TDD: write a failing test, watch it fail, then implement
- Backend: unit tests under `…/unit/`, integration under `…/integration/`
- Coverage must stay at or above the JaCoCo ratchet floor (see
  `.claude/rules/code-quality.md`); 80% line/branch is the target
- Frontend UI changes require Playwright E2E under `testing/e2e/tests/`
- Do not `@Disabled` tests without documented justification

## Refactoring Strategy (ongoing)

1. **Prefer `repository` + `service`** over extending `jpa`.
2. **Keep controllers thin**; move logic down, covered by tests.
3. **One concern per change** (KIS / SRP); remove dead and duplicate code in the
   same PR that makes it unreachable.
4. **Do not resurrect Swing** or any desktop client module.
5. **Update permanent docs** when behaviour or structure changes; archive
   superseded docs under `docs/000-archive/` (CONSTITUTION.md §8).

## Naming Conventions

### Backend

- Controllers: `FooController`
- Services: `FooService` (interfaces only when there is a real second impl)
- Repositories: `FooRepository`
- Entities: domain names (`Person`, `Deed`, …)
- DTOs: `DtoPerson`, `DtoDeed`, …
- Exceptions: `FooNotFoundException`, `InvalidFooException`

### Frontend

- Pages under `frontend/src/app/`
- Shared UI under `frontend/src/components/`
- Hooks under `frontend/src/hooks/`; stores under `frontend/src/store/`

## Logging

- Backend: SLF4J + Logback; structured JSON in containers
- INFO for business events, DEBUG for detail, ERROR for failures
- Never log secrets or full auth headers

## Documentation Requirements

- OpenAPI/Swagger for endpoints
- Flyway migration comments / ADR when the change is architectural
- Keep `CLAUDE.md` / `AGENTS.md` / these rules aligned with the running stack;
  if a rule and CONSTITUTION.md disagree, CONSTITUTION.md wins
