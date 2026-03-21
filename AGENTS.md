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

# Skip tests during build
mvn clean install -DskipTests

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
```
Package: com.licensis.notaire.{api,service,jpa,negocio,dto}
- api/         → @RestController, @RequestMapping("/api/v1/...")
- service/     → @Service, @Transactional, business logic only
- jpa/         → JpaController.getInstancia() singleton pattern
- negocio/     → Entity classes (Usuario, Persona, Presupuesto, etc.)
- dto/         → DtoUsuario, DtoPersona for API transfer
```

### Frontend (Swing)
```
Package: com.licensis.notaire.gui
- No direct database access — use REST client only
- No business logic — presentation logic only
- Use SwingWorker for API calls, show errors in JOptionPane
```

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

```java
@DisplayName("Presupuesto Entity Tests")
class PresupuestoEntityTest {

    @Nested
    @DisplayName("CU01 - Preparar Presupuesto - Unit Tests")
    class PrepararPresupuestoTests {

        @Test
        @DisplayName("Should create presupuesto with required fields")
        void shouldCreatePresupuestoWithRequiredFields() {
            // Arrange
            Presupuesto presupuesto = new Presupuesto();
            
            // Act
            presupuesto.setNumero(1001);
            
            // Assert
            assertThat(presupuesto.getNumero()).isEqualTo(1001);
        }
    }
}
```

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
