# AGENTS.md - Agent Coding Guidelines for Notaire

This file provides essential information for agentic coding agents operating in the Notaire repository.

## Project Overview

Multi-module Maven project refactoring a Java Swing monolith to microservices:
- **backend-api**: Spring Boot REST API (Java 21, PostgreSQL)
- **frontend-swing**: Swing GUI client (REST client only)
- **notaire-shared**: Shared DTOs and common code

## Build Commands

```bash
# Build entire project
mvn clean install

# Build specific module with dependencies (-am builds dependencies too)
mvn clean install -pl backend-api -am

# Package for deployment
mvn clean package
```

## Testing Commands

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl backend-api
mvn test -pl frontend-swing

# Single test class
mvn test -Dtest=PresupuestoEntityTest

# Single test method
mvn test -Dtest=PresupuestoEntityTest#shouldCreatePresupuestoWithRequiredFields

# Test pattern matching
mvn test -Dtest="*ControllerTest"
mvn test -Dtest="*ServiceTest,*RepositoryTest"

# Run unit tests only
mvn test -Dtest="**/unit/*"

# Run integration tests only
mvn test -Dtest="**/integration/*"

# Check JaCoCo coverage (80% minimum required)
mvn jacoco:check -pl backend-api
mvn jacoco:report -pl backend-api

# HTTP API integration tests (requires running API)
bash scripts/test.sh
```

## Code Quality & Linting

```bash
# Run all checks (tests + static analysis)
mvn verify -pl backend-api

# Checkstyle
mvn checkstyle:check -pl backend-api

# SpotBugs (requires Java 21 locally)
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false

# Full site generation with all reports
mvn site -pl backend-api
```

## Application Commands

```bash
# Start application and database
bash scripts/start.sh

# Stop application
bash scripts/stop.sh

# View logs
bash scripts/logs.sh

# Run backend directly
cd backend-api && mvn spring-boot:run

# Access Swagger UI: http://localhost:8080/swagger-ui.html
```

## Java Code Style Guidelines

### General Conventions
- **Java Version**: 21
- **Indentation**: 4 spaces (no tabs)
- **Line Limit**: 120 characters
- **Braces**: Same line, always use braces for control blocks
- **Spacing**: Space after keywords (`if ()`, `while ()`, `for ()`), spaces around operators

### Naming Conventions
| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `UsuarioController`, `PresupuestoService` |
| Methods/variables | camelCase | `isLoading`, `hasError`, `getActiveUsers` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `com.licensis.notaire.api` |
| DTOs | DtoEntityName | `DtoUsuario`, `DtoPersona` |
| Tests | *Test suffix | `PresupuestoEntityTest` |
| Test methods | shouldXxxYyy | `shouldCreatePresupuestoWithRequiredFields` |

### Imports
- **No wildcard imports** (e.g., `import java.util.*`)
- **Import order**: java → javax → third-party → own packages

### Critical Java Pitfalls (MUST AVOID)
- `==` compares references, not content — use `.equals()` for strings
- Override `equals()` must also override `hashCode()` — HashMap/HashSet break
- `Optional.get()` throws if empty — use `orElse()`, `orElseGet()`, `ifPresent()`
- Modifying while iterating throws `ConcurrentModificationException` — use Iterator.remove()
- Unboxing null throws NPE — `Integer i = null; int x = i;` crashes
- `Integer == Integer` uses reference for values outside -128 to 127 — use `.equals()`
- Try-with-resources for AutoCloseable — implement `AutoCloseable`, Java 7+

### Error Handling
- Return `ResponseEntity` with appropriate status codes (200, 201, 400, 404, 500)
- Use SLF4J `Logger`, parameterized logging (`log.info("msg {}", var)`)
- **Never ignore exceptions silently**
- Return empty collections, not null (`Collections.emptyList()`)
- Use `Optional<T>` for nullable returns

### Architecture Packages
- **Backend**: `com.licensis.notaire.{api,service,jpa,negocio,dto}`
- **Frontend**: `com.licensis.notaire.gui` (REST client only, no business logic)

### REST API Design
- **URL**: `/api/v1/resource` (plural nouns)
- **HTTP methods**: GET (read), POST (create), PUT (update), DELETE (delete)
- Use `@Operation` and `@Tag` from springdoc-openapi

### Testing Guidelines
- **Pattern**: AAA (Arrange-Act-Assert)
- **Assertions**: AssertJ fluent API (`assertThat(...).isEqualTo(...)`)
- **Organization**: Use `@Nested` classes for related tests
- **Naming**: `@DisplayName` + descriptive method names
- **Coverage**: Minimum 80% (JaCoCo enforces this)

### Database
- **Engine**: PostgreSQL 15 in Docker
- **ORM**: Spring Data JPA with EclipseLink
- **Entities**: Implement `equals()`/`hashCode()` based on ID

## Prohibited Patterns
- Backend: Swing dependencies, direct database access from controllers
- Frontend: Direct JDBC connections, SQL queries, business logic in event handlers
- General: Hardcoded credentials, ignored exceptions, wildcard imports

## Git Workflow

1. Ensure branch is clean before editing
2. Create feature branch: `git checkout -b <TASK-ID>/[feat/fix/add]/<short-task-name>`
3. Never commit directly to main/master
4. Use conventional commits: `<TASK-ID>/feat: ...`, `<TASK-ID>/fix: ...`
5. Run test suite and static analysis before committing
6. Verify build succeeds: `mvn test` or `mvn package`
7. Create PR with descriptive message referencing issue (e.g., TASK-123)
