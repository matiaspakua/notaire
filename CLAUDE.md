# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Rules & Standards (always enforced)

@.claude/rules/general.md
@.claude/rules/programming.md
@.claude/rules/code-quality.md
@.claude/rules/refactoring.md

## Project Overview

Multi-module Maven project refactoring a Java Swing monolith to microservices. Spring Boot 4.0.4, Java 21, PostgreSQL 16.

**Modules:**
- `backend-api` — Spring Boot REST API (main development target)
- `frontend-swing` — Swing GUI client (REST client only, no business logic)
- `notaire-shared` — Shared DTOs and common code

## Build & Run Commands

```bash
# Build all modules
mvn clean install

# Build backend only (with shared dependency)
mvn clean install -pl backend-api -am

# Start database + backend (Docker)
bash scripts/start.sh
# Stop
bash scripts/stop.sh
# Logs
bash scripts/logs.sh

# Run backend directly (needs local PostgreSQL on 5432)
cd backend-api && mvn spring-boot:run
```

**Ports:** Backend API `8080`, PostgreSQL `5432`, pgAdmin `5050`
**Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Testing Commands

```bash
# All tests
mvn test -pl backend-api

# Single test class or method
mvn test -pl backend-api -Dtest=PresupuestoEntityTest
mvn test -pl backend-api -Dtest=PresupuestoEntityTest#shouldCreatePresupuestoWithRequiredFields

# Unit tests only
mvn test -pl backend-api -Dtest="**/unit/*"

# Integration tests only (require running PostgreSQL; H2-based tests work standalone)
mvn test -pl backend-api -Dtest="**/integration/*"

# Coverage check (80% minimum enforced by JaCoCo)
mvn jacoco:check -pl backend-api
mvn jacoco:report -pl backend-api  # HTML report at backend-api/target/site/jacoco/index.html

# Code quality
mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
mvn verify -pl backend-api  # all checks

# HTTP integration tests (requires running API)
bash scripts/test.sh
```

## Backend Architecture (`backend-api`)

Package root: `com.licensis.notaire`

| Package | Role |
|---------|------|
| `api` | REST controllers (`@RestController`), one per domain entity |
| `service` | Thin services (`EscrituraService`, `PersonaService`, `RegistroAuditoriaService`) |
| `jpa` | Legacy-style JPA controllers — heavy data-access classes wrapping entity queries |
| `negocio` | Domain/entity classes (`@Entity`) — the core data model |
| `repository` | Spring Data JPA repositories (`JpaRepository`) |
| `config` | Spring configuration beans |

**Key architectural note:** The `jpa` package contains `*JpaController` classes (not REST controllers) — these are large data-access classes migrated from the original monolith. They are being superseded by the `repository` package (Spring Data repos). New code should use `repository`, not `jpa`.

**Database:** PostgreSQL 16 via Docker. Schema managed by `init-db/` SQL scripts. `ddl-auto=update` locally, `none` in Docker. ORM: Hibernate (PostgreSQLDialect).

**Reports:** JasperReports (`.jasper`/`.jrxml`) in `src/main/resources/reportes/`. The `ReporteController` handles report generation.

**Tests:** Organized under `src/test/java/.../unit/` and `integration/`. `ApiH2IntegrationTest` uses H2 in-memory; `ApiIntegrationTest` requires a running PostgreSQL.

## Key Conventions

- DTOs named `DtoEntityName` (e.g., `DtoUsuario`, `DtoPersona`)
- REST URLs: `/api/v1/resource` (plural nouns)
- Test methods: `shouldXxxYyy` with `@DisplayName`; use AssertJ (`assertThat(...)`)
- No wildcard imports; import order: java → javax → third-party → own packages
- Line limit 120 chars, 4-space indent

## Git Workflow

- Branch: `<TASK-ID>/[feat|fix|add]/<short-name>`
- Never commit directly to `main`
- Conventional commits: `<TASK-ID>/feat: ...`, `<TASK-ID>/fix: ...`
- Run `mvn test -pl backend-api` before committing

## Prohibited

- Backend: Swing imports, direct DB access from controllers
- Frontend: JDBC/SQL, business logic in event handlers
- General: Hardcoded credentials, ignored exceptions, wildcard imports
