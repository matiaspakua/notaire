# Agent Instructions for Notaire Project

## Project Overview
Multi-module Maven project refactoring a Java Swing monolith to microservices:
- **backend-api**: Spring Boot REST API (Java 21, PostgreSQL)
- **frontend-swing**: Swing GUI client (REST client only)
- **notaire-shared**: Shared DTOs and common code

## Build & Run Commands

```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl backend-api -am  # -am builds dependencies too

# Package for deployment
mvn clean package
```

### Testing Commands
```bash
# Run all tests with coverage report
mvn test
mvn test -pl backend-api  # specific module
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
mvn jacoco:report -pl backend-api  # generates target/site/jacoco/index.html

# HTTP API integration tests (requires running API)
bash scripts/test.sh
```

### Application Commands
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

## Code Style Guidelines

### Java Conventions
- **Java Version**: 21
- **Indentation**: 4 spaces
- **Line Limit**: 120 characters
- **Braces**: Same line, always use braces for control blocks
- **Spacing**: Space after keywords (`if ()`, `while ()`, `for ()`), spaces around operators
- **No wildcard imports**
- **Import order**: java, javax, third-party, own packages

### Naming Conventions
| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `UsuarioController`, `PresupuestoService` |
| Methods/variables | camelCase with aux verbs | `isLoading`, `hasError`, `getActiveUsers` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `com.licensis.notaire.api` |
| DTOs | DtoEntityName | `DtoUsuario`, `DtoPersona` |
| Tests | *Test suffix | `PresupuestoEntityTest` |
| Test methods | shouldXxxYyy | `shouldCreatePresupuestoWithRequiredFields` |

### Critical Java Pitfalls (MUST AVOID)
- `==` compares references, not content — use `.equals()` for strings
- Override `equals()` must also override `hashCode()` — HashMap/HashSet break
- `Optional.get()` throws if empty — use `orElse()`, `orElseGet()`, `ifPresent()`
- Modifying while iterating throws `ConcurrentModificationException`
- `volatile` ensures visibility, not atomicity — `count++` needs synchronization
- Unboxing null throws NPE — `Integer i = null; int x = i;` crashes
- `Integer == Integer` uses reference for values outside -128 to 127 — use `.equals()`
- Try-with-resources auto-closes — implement `AutoCloseable`, Java 7+

## Architecture Rules

### Backend (Spring Boot)

Package: com.licensis.notaire.{api,service,jpa,negocio,dto}
- api/         → @RestController, @RequestMapping("/api/v1/...")
- service/     → @Service, @Transactional, business logic only
- jpa/         → JpaController.getInstancia() singleton pattern
- negocio/     → Entity classes (Usuario, Persona, Presupuesto, etc.)
- dto/         → DtoUsuario, DtoPersona for API transfer

### Frontend (Swing)

Package: com.licensis.notaire.gui
- No direct database access — use REST client only
- No business logic — presentation logic only
- Use SwingWorker for API calls, show errors in JOptionPane

### REST API Design
- **URL**: `/api/v1/resource` (plural nouns)
- **HTTP**: GET (read), POST (create), PUT (update), DELETE (delete)
- **Annotations**: Use `@Operation` and `@Tag` from springdoc-openapi

## Error Handling & Validation
- **Backend**: Return `ResponseEntity` with appropriate status codes (200, 201, 400, 404, 500)
- **Logging**: Use SLF4J `Logger`, parameterized logging (`log.info("msg {}", var)`)
- **Never ignore exceptions silently**
- **Return empty collections**, not null (`Collections.emptyList()`)
- **Use Optional<T>** for nullable returns

## Testing Guidelines
- **Pattern**: AAA (Arrange-Act-Assert)
- **Assertions**: AssertJ fluent API (`assertThat(...).isEqualTo(...)`)
- **Organization**: Use `@Nested` classes for related tests
- **Naming**: `@DisplayName` + descriptive method names
- **Coverage**: Minimum 80% (JaCoCo enforces this)
- **Use `@ParameterizedTest`** for data-driven tests

## Database
- **Engine**: PostgreSQL 15 in Docker
- **ORM**: Spring Data JPA with EclipseLink
- **Pattern**: Singleton JpaController with `getInstancia()`
- **Transactions**: Service layer with `@Transactional`
- **Entities**: Implement `equals()`/`hashCode()` based on ID

## Prohibited Patterns
- Backend: Swing dependencies, direct database access from controllers
- Frontend: Direct JDBC connections, SQL queries, business logic in event handlers
- General: Hardcoded credentials, ignored exceptions, wildcard imports

## Git, Branches, Commits and PR's rules
When modifying Java code in this repository, always follow this workflow.

1. Ensure the current branch is clean and committed; do not proceed until all pending work is committed and pushed or explicitly confirmed by the user.
2. Before editing any file, create a feature branch:
`git checkout -b <TASK-ID>/[feat/fix/add]/<short-task-name>.`

3. Never commit directly to main, master, or any other protected branch.
4. Use clear, conventional commit messages, for example:
   `<TASK-ID>/feat: ... (new features)`
   `<TASK-ID>/fix: ... (bug fixes)`
   `<TASK-ID>/refactor: ... (code restructuring without behavior change)`

5. After completing your changes:
5.1. run the test suite (mandatory)
5.2. run linters and static analysis tools

6. verify that the Java project builds successfully (e.g., mvn test, mvn package or Gradle equivalents).
7. Commit and push the changes in the current feature branch.
8. Once the full TASK/ISSUE is completed, push changes to remote and create a PR with a descriptive menssage.

# Agent Session and Memory Management
1. After each completed agent execution or development session, ensure traceability and persistent context.
2. Export the agent session for audit and replay:
   `opencode export (or equivalent)`
3. Store a Markdown summary at:
   `<root-project>docs/agent-sessions/<date>-session.md (use an ISO-like format, for example 2026-03-24-session.md).`
4. The session summary must include at least:
4.1. session objective and high-level outcome
4.2. list of files created, modified, or deleted
4.3. key commands executed (build, test, tooling, scripts)
4.4. any known limitations, TODOs, or follow-up actions the next agent/developer should know.