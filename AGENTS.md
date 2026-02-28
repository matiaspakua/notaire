# Agent Instructions for Notaire Project

## Project Overview
Multi-module Maven project refactoring a Java Swing monolith to microservices architecture:
- **backend-api**: Spring Boot REST API (Java 21, PostgreSQL)
- **frontend-swing**: Swing GUI client 
- **notaire-shared**: Shared DTOs and common code
- **Database**: PostgreSQL in Docker container

## Build & Run Commands
```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl backend-api

# Run backend API
cd backend-api && mvn spring-boot:run

# Package for deployment
mvn clean package

# Skip tests during build
mvn clean install -DskipTests

# Start/Stop application
bash scripts/start.sh
bash scripts/stop.sh
bash scripts/logs.sh

# Access Swagger UI
http://localhost:8080/swagger-ui.html
```

### Testing Commands
```bash
# Run all JUnit tests
mvn test

# Run tests for specific module
mvn test -pl backend-api

# Run a single test class
mvn test -Dtest=DocumentServiceTest

# Run a single test method
mvn test -Dtest=DocumentServiceTest#shouldCreateDocument

# Run tests matching a pattern
mvn test -Dtest="*ServiceTest"

# Run HTTP API integration tests
bash scripts/test.sh
```

## Code Style Guidelines

### Java Conventions
- **Java Version**: 21
- **Indentation**: 4 spaces
- **Line Limit**: 120 characters
- **Braces**: Opening brace on same line, always use braces for control blocks
- **Spacing**: Space after keywords (if (), while (), for ()), spaces around operators
- **Imports**: No wildcard imports, order: java, javax, third-party, own packages
- **@Override**: Use consistently for overridden methods
- **One statement per line**

### Critical Java Pitfalls (MUST AVOID)
- `==` compares references, not content — always use `.equals()` for strings
- Override `equals()` must also override `hashCode()` — HashMap/HashSet break otherwise
- `Optional.get()` throws if empty — use `orElse()`, `orElseGet()`, or `ifPresent()`
- Modifying while iterating throws `ConcurrentModificationException` — use Iterator.remove()
- Type erasure: generic type info gone at runtime — can't do `new T()` or `instanceof List<String>`
- `volatile` ensures visibility, not atomicity — `count++` still needs synchronization
- Unboxing null throws NPE — `Integer i = null; int x = i;` crashes
- `Integer == Integer` uses reference for values outside -128 to 127 — use `.equals()`
- Try-with-resources auto-closes — implement `AutoCloseable`, Java 7+
- Inner classes hold reference to outer — use static nested class if not needed
- Streams are single-use — can't reuse after terminal operation

### Naming Conventions
- **Classes**: PascalCase (DocumentController, NotaryService)
- **Methods/Variables**: camelCase with auxiliary verbs (isLoading, hasError)
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: lowercase without underscores (com.licensis.notaire.module)
- **DTOs**: EntityRequestDTO, EntityResponseDTO
- **Exceptions**: SpecificException (ResourceNotFoundException)

### Architecture Rules

#### Backend (Spring Boot API)
- **Package**: com.licensis.notaire.backend.{controller,service,repository,entity,dto,exception}
- **Controllers**: @RestController, constructor injection, return ResponseEntity<T>
- **Services**: @Service, business logic only, @Transactional
- **Repositories**: JpaRepository<Entity, ID>, no business logic
- **DTOs**: Required for all endpoints, use javax.validation
- **Dependency Injection**: Use constructor injection with final fields, avoid @Autowired on fields
- **Use interfaces**: For dependencies to enable mocking and alternative implementations

#### Frontend (Swing Client)
- **Package**: com.licensis.notaire.gui.{client,view,controller,model,util}
- **No direct database access** - use REST client only
- **Remove business logic** - keep only presentation logic
- **Async**: Use SwingWorker for API calls, show errors in JOptionPane

#### REST API Design
- **URL Pattern**: /api/v1/resource (plural nouns)
- **HTTP Methods**: GET (read), POST (create), PUT (update), DELETE (delete)
- **Response Format**: JSON, Error Responses: {error, message, timestamp, path}
- **Documentation**: Swagger/OpenAPI annotations required

### Error Handling
- **Backend**: Custom exceptions with @ControllerAdvice, log exceptions appropriately
- **Frontend**: Catch API exceptions, show JOptionPane with user-friendly message
- **Validation**: Client-side for UX, server-side for security
- **Logging**: Use SLF4J, don't log sensitive data
- **Never ignore exceptions silently**: Catch specific exceptions, propagate with context
- **Use Optional<T>**: For nullable return values, use orElse/orElseThrow instead of null

### Code Quality & Testing
- **SOLID Principles**: Single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **Method Size**: Maximum 20-30 lines, one concept per method
- **Parameters**: Maximum 3-4 parameters per method
- **Class Size**: Keep classes small and focused
- **DRY**: Avoid code duplication through modularization
- **Immutability**: Prefer immutable objects, use final when appropriate
- **Type Safety**: Use generics, avoid raw types, use Optional<T> for nullable returns
- **Conditional Logic**: Limit to 2 levels depth, use guard clauses over nested else
- **Empty Collections**: Return empty collections instead of null (Collections.emptyList())
- **Never ignore exceptions silently**: Catch specific exceptions, propagate with context
- **Testing**: AAA pattern (Arrange-Act-Assert), 80% coverage minimum, descriptive test names
- **Use AssertJ**: For fluent assertions in tests
- **Test Naming**: Use @DisplayName and descriptive method names (shouldReturnXWhenY)
- **Use @ParameterizedTest**: For data-driven tests with @ValueSource
- **Use @Nested**: To organize related tests in inner classes
- **Mocking**: Use @Mock for dependencies, @InjectMocks for the service under test

### Database & Security
- **JPA**: Use Spring Data JPA, lazy loading for relationships
- **Entities**: Implement equals()/hashCode() based on ID, PostgreSQL-specific types when needed
- **Transactions**: Service layer with @Transactional
- **Connection Pooling**: HikariCP (configured by Spring Boot)
- **Input Validation**: Validate all inputs, sanitize user input
- **SQL**: Use prepared statements (JPA handles this)
- **Authentication**: Implement JWT/OAuth2/Basic Auth for API
- **Credentials**: Never hardcode, use environment variables
- **HTTPS**: Required in production

### Performance & Migration Guidelines
- **Backend**: Connection pooling, pagination, caching for frequently accessed data
- **Frontend**: Lazy loading, background threads for API calls, virtual scrolling for large datasets
- **Database**: Index columns used in WHERE clauses, monitor slow queries
- **Migration Phases**: Database (MySQL→PostgreSQL) → Backend REST API → Refactor GUI to API client

### Dependencies & Docker
- **Spring Boot**: 3.2.9 parent, **Java**: 21
- **Database**: PostgreSQL with HikariCP
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Docker**: postgres:15-alpine, eclipse-temurin:21-jre-alpine
- **API Docs**: Swagger/OpenAPI at /swagger-ui.html
- **Code**: Javadoc for public APIs, comment the "why" not the "what"

### Development Workflow
1. Start application with `bash script/start.sh`
2. Run tests with `bash script/test.sh`
3. Build with `mvn clean install`
4. Check Swagger UI at http://localhost:8080/swagger-ui.html
5. Follow migration progress in MIGRATION_*.md files

### Modern Java Features (JDK 21)
- **Records**: Use for immutable DTOs and data carriers (auto-generated equals/hashCode/toString)
- **Pattern Matching**: Use `instanceof` pattern matching and switch expressions
- **Virtual Threads**: Use for high-throughput concurrent operations
- **Text Blocks**: Use for multiline strings (JSON, SQL in code)
- **Sealed Classes**: Use for controlled inheritance hierarchies

### Prohibited Patterns
- **Backend**: Swing dependencies, direct database access from controllers
- **Frontend**: Direct JDBC connections, SQL queries, business logic in event handlers
- **General**: Hardcoded credentials, ignored exceptions, wildcard imports
- **Architecture**: Tight coupling between layers, circular dependencies
