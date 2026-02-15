# Agent Instructions for Notaire Project

## Project Overview
Multi-module Maven project refactoring a Java Swing monolith to microservices architecture:
- **backend-api**: Spring Boot REST API (Java 21, PostgreSQL)
- **frontend-swing**: Swing GUI client 
- **notaire-shared**: Shared DTOs and common code
- **Database**: PostgreSQL in Docker container

## Build Commands

### Maven Commands
```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl backend-api
mvn clean install -pl frontend-swing
mvn clean install -pl notaire-shared

# Run backend API
cd backend-api && mvn spring-boot:run

# Package for deployment
mvn clean package

# Skip tests during build
mvn clean install -DskipTests
```

### Testing Commands
```bash
# Run HTTP API integration tests
bash test.sh

# Run specific endpoint test
cd test/http && bash 01-auth.sh

# Run all endpoint tests
cd test/http && bash test-all-endpoints-v2.sh

# Run JUnit tests (when implemented)
mvn test

# Run tests for specific module
mvn test -pl backend-api
```

### Development Commands
```bash
# Start application (uses Docker Compose)
bash start.sh

# Stop application
bash stop.sh

# View logs
bash logs.sh

# Access Swagger UI
http://localhost:8080/swagger-ui.html
```

## Code Style Guidelines

### Java Conventions
- **Java Version**: 21
- **Indentation**: 4 spaces
- **Line Limit**: 120 characters
- **Braces**: Opening brace on same line, always use braces for control blocks
- **Spacing**: Space after keywords, around operators
- **Imports**: No wildcard imports, order: java, javax, third-party, own packages

### Naming Conventions
- **Classes**: PascalCase (DocumentController, NotaryService)
- **Methods/Variables**: camelCase with auxiliary verbs (isLoading, hasError)
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: lowercase without underscores (com.licensis.notaire.module)
- **DTOs**: EntityRequestDTO, EntityResponseDTO
- **Exceptions**: SpecificException (ResourceNotFoundException)

### Architecture Rules

#### Backend (Spring Boot API)
- **Package Structure**: com.licensis.notaire.backend.{controller,service,repository,entity,dto,exception}
- **Controllers**: @RestController, constructor injection, return ResponseEntity<T>
- **Services**: @Service, business logic only, @Transactional methods
- **Repositories**: JpaRepository<Entity, ID>, no business logic
- **DTOs**: Required for all API endpoints, use javax.validation annotations
- **No Swing dependencies** allowed in backend

#### Frontend (Swing Client)
- **Package Structure**: com.licensis.notaire.gui.{client,view,controller,model,util}
- **No direct database access** - use REST client only
- **Remove business logic** - keep only presentation logic
- **Async Operations**: Use SwingWorker for API calls
- **Error Handling**: Show user-friendly messages in JOptionPane

#### REST API Design
- **URL Pattern**: /api/v1/resource (plural nouns)
- **HTTP Methods**: GET (read), POST (create), PUT (update), DELETE (delete)
- **Response Format**: JSON with consistent structure
- **Error Responses**: {error, message, timestamp, path}
- **Documentation**: Swagger/OpenAPI annotations required

### Error Handling Patterns
- **Backend**: Custom exceptions with @ControllerAdvice, log exceptions appropriately
- **Frontend**: Catch API exceptions, show JOptionPane with user-friendly message
- **Validation**: Client-side for UX, server-side for security
- **Logging**: Use SLF4J, don't log sensitive data

### Code Quality Rules
- **SOLID Principles**: Single responsibility, open/closed, Liskov substitution, interface segregation, dependency inversion
- **Method Size**: Maximum 20-30 lines, one concept per method
- **Parameters**: Maximum 3-4 parameters per method
- **Class Size**: Keep classes small and focused
- **DRY**: Avoid code duplication through modularization
- **Immutability**: Prefer immutable objects, use final when appropriate

### Testing Guidelines
- **Backend**: Unit tests for services, integration tests for repositories, API tests with MockMvc
- **Frontend**: Unit tests for API client with WireMock, manual GUI testing
- **Coverage**: Minimum 80% for business logic
- **Test Structure**: AAA pattern (Arrange, Act, Assert), descriptive test names

### Database Rules
- **JPA**: Use Spring Data JPA, lazy loading for relationships
- **Entities**: Implement equals()/hashCode() based on ID, PostgreSQL-specific types when needed
- **Transactions**: Service layer with @Transactional
- **Connection Pooling**: HikariCP (configured by Spring Boot)

### Security Guidelines
- **Input Validation**: Validate all inputs, sanitize user input
- **SQL**: Use prepared statements (JPA handles this)
- **Authentication**: Implement JWT/OAuth2/Basic Auth for API
- **Credentials**: Never hardcode, use environment variables
- **HTTPS**: Required in production

### Migration Rules (Monolith → Microservices)
- **Phase 1**: Database migration (MySQL → PostgreSQL)
- **Phase 2**: Create backend module with REST endpoints
- **Phase 3**: Refactor GUI to use API client
- **Backwards Compatibility**: Keep legacy code during migration

### Performance Guidelines
- **Backend**: Connection pooling, pagination, caching for frequently accessed data
- **Frontend**: Lazy loading, background threads for API calls, virtual scrolling for large datasets
- **Database**: Index columns used in WHERE clauses, monitor slow queries

### Documentation Requirements
- **API**: Swagger/OpenAPI documentation with examples
- **Code**: Javadoc for public APIs, comment the "why" not the "what"
- **README**: Setup instructions, Docker configuration
- **Migration**: Document breaking changes

### Dependencies Management
- **Spring Boot**: 3.2.9 parent
- **Java**: 21
- **Database**: PostgreSQL with HikariCP
- **Documentation**: SpringDoc OpenAPI
- **Utilities**: Lombok for boilerplate reduction
- **Testing**: JUnit 5, Mockito, Testcontainers

### Git Conventions
- **Commit Messages**: type(scope): description (feat, fix, docs, style, refactor, test, chore)
- **Branches**: feature/, bugfix/, hotfix/
- **Code Review**: Mandatory before merge
- **Commits**: Atomic and descriptive, focused on single concern

### Docker Configuration
- **Database**: postgres:15-alpine with environment variables
- **Backend**: eclipse-temurin:21-jre-alpine, health check at /actuator/health
- **Compose**: Define services, network, volumes, environment variables in .env

### Development Workflow
1. Start application with `bash start.sh`
2. Run tests with `bash test.sh`
3. Check Swagger UI at http://localhost:8080/swagger-ui.html
4. Build with `mvn clean install`
5. Follow migration progress in MIGRATION_*.md files

### Prohibited Patterns
- **Backend**: Swing dependencies, direct database access from controllers
- **Frontend**: Direct JDBC connections, SQL queries, business logic in event handlers
- **General**: Hardcoded credentials, ignored exceptions, wildcard imports
- **Architecture**: Tight coupling between layers, circular dependencies

### Linting and Formatting
- **No traditional linting tools configured** - follow Cursor rules in `.cursor/rules/`
- **IDE Configuration**: VS Code settings in `.vscode/settings.json`
- **Code Review**: Enforce patterns from `.cursor/rules/programming/RULE.md`

This file serves as the comprehensive guide for agentic coding agents working in this repository. Always refer to these guidelines when making changes to the codebase.