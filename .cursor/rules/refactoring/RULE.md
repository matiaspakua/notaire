---
title: Monolith to Microservices Refactoring Rules
description: Rules for refactoring Java Swing monolith to Docker-based REST API architecture
alwaysApply: true
---

## Architecture Overview

- Legacy: Monolithic Java 1.6 application with direct database access and tightly coupled Swing GUI
- Target: Three-tier architecture with PostgreSQL database, Spring Boot REST API backend, and standalone Swing GUI client
- Communication: Replace direct method calls with RESTful HTTP requests
- Deployment: Database and backend as Docker containers, GUI as standalone Java application
- Database: Migrate from MySQL to PostgreSQL with schema compatibility

## Module Structure

### Backend Module (REST API)
- Package: com.notaria.backend
- Framework: Spring Boot 3.x with Java 17+
- Responsibilities: Business logic, data validation, database access, REST endpoints
- No Swing dependencies allowed
- Must be stateless and horizontally scalable
- All business logic migrated from GUI to backend services

### GUI Module (Swing Client)
- Package: com.notaria.gui
- Framework: Java Swing with modern look and feel
- Responsibilities: User interface, input validation, API client communication
- No direct database access allowed
- No business logic - only presentation logic
- Must use REST client to communicate with backend

### Database Module
- PostgreSQL 15+ in Docker container
- Schema migration from MySQL to PostgreSQL
- No direct access from GUI module
- Only backend module can access database
- Use connection pooling (HikariCP)

## REST API Design

### Endpoint Conventions
- Base URL: /api/v1
- Resource naming: plural nouns (documents, notaries, clients)
- HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete), PATCH (partial update)
- Status codes: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 500 (Internal Error)
- Response format: JSON with consistent structure
- Error responses: {error: string, message: string, timestamp: string, path: string}

### URL Structure
- Collections: GET /api/v1/documents
- Single resource: GET /api/v1/documents/{id}
- Nested resources: GET /api/v1/notaries/{id}/documents
- Filtering: GET /api/v1/documents?status=active&date=2025-01-01
- Pagination: GET /api/v1/documents?page=0&size=20&sort=createdAt,desc
- Search: GET /api/v1/documents/search?query=contract

### Request/Response DTOs
- Create DTOs for all API requests and responses
- Package: com.notaria.backend.dto
- Naming: EntityRequestDTO, EntityResponseDTO
- Use javax.validation annotations (@NotNull, @NotBlank, @Size, @Email)
- No entity objects in API layer - always use DTOs
- Map entities to DTOs using MapStruct or manual mappers

## Backend Implementation

### Controller Layer
- Package: com.notaria.backend.controller
- Naming: EntityController (DocumentController, NotaryController)
- Annotations: @RestController, @RequestMapping("/api/v1/resource")
- Inject services via constructor injection
- Validate input with @Valid annotation
- Return ResponseEntity<T> with appropriate status codes
- Handle pagination with Pageable parameter
- Use @PathVariable for URL parameters, @RequestParam for query parameters

### Service Layer
- Package: com.notaria.backend.service
- Naming: EntityService interface, EntityServiceImpl implementation
- Annotations: @Service on implementation
- All business logic resides here
- Transactional methods with @Transactional
- Throw custom exceptions for business errors
- No HTTP-specific code (no HttpServletRequest, ResponseEntity)
- Services should be stateless

### Repository Layer
- Package: com.notaria.backend.repository
- Naming: EntityRepository
- Extend JpaRepository<Entity, ID> or CrudRepository<Entity, ID>
- Use Spring Data JPA query methods
- Custom queries with @Query annotation
- No business logic in repositories
- Only data access operations

### Entity Layer
- Package: com.notaria.backend.entity
- Naming: Entity classes (Document, Notary, Client)
- Annotations: @Entity, @Table, @Id, @GeneratedValue, @Column
- Use JPA relationships (@OneToMany, @ManyToOne, @ManyToMany)
- Implement equals() and hashCode() based on ID
- No Swing-related code
- PostgreSQL-specific types when needed (@Type)

### Exception Handling
- Package: com.notaria.backend.exception
- Create custom exceptions: ResourceNotFoundException, BusinessValidationException, DatabaseException
- Global exception handler with @ControllerAdvice
- Map exceptions to appropriate HTTP status codes
- Return consistent error response format
- Log exceptions with appropriate levels

## GUI Client Implementation

### REST Client Configuration
- Package: com.notaria.gui.client
- Use RestTemplate or WebClient for HTTP communication
- Create ApiClient interface with methods matching backend endpoints
- Implement RestApiClient with proper error handling
- Configure base URL from properties file
- Implement connection timeout and retry logic
- Handle network errors gracefully with user-friendly messages

### API Client Methods
- Method naming: getDocuments(), createDocument(), updateDocument(), deleteDocument()
- Return DTOs or domain objects, not HTTP responses
- Throw custom exceptions on API errors
- Parse JSON responses to Java objects
- Include authentication headers if required
- Log API calls for debugging (without sensitive data)

### Refactoring GUI Code
- Remove all direct database access (JDBC, Hibernate sessions)
- Remove all business logic from GUI classes
- Replace direct method calls with API client calls
- Update event handlers to call API methods
- Implement loading indicators for async operations
- Show error messages from API responses
- Validate input on client side before sending to API

### GUI Package Structure
- com.notaria.gui.client: REST client implementation
- com.notaria.gui.view: Swing panels and frames
- com.notaria.gui.controller: GUI controllers/presenters
- com.notaria.gui.model: GUI-specific models (not entities)
- com.notaria.gui.util: Utility classes for GUI

### Async Operations
- Use SwingWorker for long-running API calls
- Update GUI on Event Dispatch Thread (EDT)
- Show progress indicators during API requests
- Implement cancel functionality for long operations
- Handle timeouts gracefully

## Data Migration

### Database Schema
- Create SQL migration scripts for PostgreSQL
- Map MySQL types to PostgreSQL types (INT→INTEGER, DATETIME→TIMESTAMP, TEXT→TEXT)
- Update AUTO_INCREMENT to SERIAL or IDENTITY
- Migrate stored procedures to PostgreSQL syntax
- Create indexes for frequently queried columns
- Use Flyway or Liquibase for version control

### Data Transfer
- Export data from MySQL in SQL format
- Transform data for PostgreSQL compatibility
- Import data maintaining referential integrity
- Verify data integrity after migration
- Create backup before migration
- Test queries in PostgreSQL

## Docker Configuration

### Backend Dockerfile
- Base image: eclipse-temurin:17-jre-alpine or openjdk:17-jre-slim
- Copy JAR file to container
- Expose port 8080
- Set Java options: -Xmx512m -Xms256m
- Use non-root user for security
- Health check endpoint: /actuator/health

### Database Dockerfile
- Base image: postgres:15-alpine
- Set environment variables: POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD
- Expose port 5432
- Mount volume for data persistence
- Copy initialization scripts to /docker-entrypoint-initdb.d/
- Configure pg_hba.conf for access control

### Docker Compose
- Define services: postgres, backend
- Network configuration for service communication
- Volume mounts for database persistence
- Environment variables in .env file (not in compose file)
- Depends_on to ensure postgres starts before backend
- Restart policies: restart: unless-stopped
- Port mapping: only expose necessary ports to host

## Configuration Management

### Backend Configuration
- application.yml for Spring Boot configuration
- Separate profiles: dev, test, prod
- Database connection: spring.datasource.url, username, password
- JPA settings: spring.jpa.hibernate.ddl-auto=validate (not create or update in prod)
- Logging configuration: logging.level.com.notaria=DEBUG
- Server port: server.port=8080
- Externalize secrets using environment variables

### GUI Configuration
- Properties file: application.properties
- API base URL: api.base.url=http://localhost:8080/api/v1
- Connection timeout: api.timeout=30000
- Retry configuration: api.retry.max=3
- Read configuration at startup
- Allow override with system properties

## Security Considerations

### API Security
- Implement authentication (JWT, OAuth2, or Basic Auth)
- Add security headers (CORS, CSRF protection)
- Validate all input on server side
- Use HTTPS in production
- Implement rate limiting
- Sanitize user input to prevent injection
- Don't expose internal error details in API responses

### Database Security
- Use strong passwords (not 'password' or 'admin')
- Limit database user permissions (no GRANT ALL)
- Use prepared statements (JPA handles this)
- Encrypt sensitive data at rest
- Enable SSL for database connections
- Regular security updates for PostgreSQL

### GUI Security
- Store API credentials securely (not in plain text)
- Implement session timeout
- Clear sensitive data from memory
- Validate SSL certificates
- Don't log sensitive information

## Testing Strategy

### Backend Testing
- Unit tests for services with mocked repositories
- Integration tests for repositories with test database
- API tests with MockMvc or TestRestTemplate
- Test DTOs validation with @WebMvcTest
- Use @SpringBootTest for full integration tests
- Test database with Testcontainers (PostgreSQL container)
- Minimum 80% code coverage for business logic

### GUI Testing
- Unit tests for API client with WireMock
- Manual testing for Swing components
- Test error handling scenarios
- Test with mock API responses
- Verify loading states and error messages

## Migration Strategy

### Phase 1: Database Migration
- Setup PostgreSQL Docker container
- Create database schema
- Migrate data from MySQL
- Verify data integrity
- Update connection strings

### Phase 2: Create Backend Module
- Setup Spring Boot project
- Create entity classes from existing domain
- Implement repositories
- Create services with business logic from GUI
- Implement REST controllers with DTOs
- Add exception handling
- Write unit and integration tests
- Dockerize backend application

### Phase 3: Refactor GUI Module
- Create separate Maven/Gradle module for GUI
- Implement REST client
- Remove database dependencies
- Replace direct calls with API client calls
- Remove business logic
- Test GUI with backend API
- Update build configuration

### Phase 4: Integration Testing
- Test full flow: GUI → Backend → Database
- Verify all features work correctly
- Performance testing
- Security testing
- Error handling verification

## Backwards Compatibility

- Keep old code in separate package (com.notaria.legacy) during migration
- Create feature flags to toggle between old and new implementation
- Gradual migration: one module at a time
- Maintain both implementations until new version is stable
- Document breaking changes

## Code Organization Rules

### What Goes in Backend
- All database access code
- Business rules and validation
- Complex calculations
- Data transformations
- Security and authorization logic
- Transaction management

### What Goes in GUI
- Swing components and panels
- User input handling
- Data display and formatting
- Client-side validation (for UX, not security)
- Navigation logic
- API client invocation

### What to Remove
- Direct JDBC connections from GUI
- SQL queries in GUI classes
- Business logic in event handlers
- Hardcoded database credentials
- Tight coupling between layers

## Naming Conventions

### Backend
- Controllers: DocumentController, NotaryController
- Services: DocumentService, NotaryService
- Repositories: DocumentRepository, NotaryRepository
- Entities: Document, Notary, Client
- DTOs: DocumentRequestDTO, DocumentResponseDTO
- Exceptions: DocumentNotFoundException, InvalidDocumentException

### GUI
- Frames: MainFrame, DocumentManagementFrame
- Panels: DocumentListPanel, DocumentFormPanel
- Client: ApiClient, RestApiClient
- Models: DocumentTableModel, NotaryComboBoxModel

## Error Handling Patterns


### GUI Error Handling
- Catch API exceptions and show JOptionPane with user-friendly message
- Log technical details for debugging
- Provide retry option for recoverable errors
- Disable actions during processing
- Show connection status indicator

## Performance Optimization

### Backend
- Use database connection pooling
- Implement pagination for large result sets
- Add caching for frequently accessed data (Spring Cache)
- Use lazy loading for JPA relationships
- Index database columns used in WHERE clauses
- Monitor slow queries and optimize

### GUI
- Cache API responses when appropriate
- Implement lazy loading for large lists
- Use background threads for API calls
- Optimize Swing rendering for large datasets
- Implement virtual scrolling for large tables

## Logging

### Backend Logging
- Use SLF4J with Logback
- Log levels: INFO for business events, DEBUG for detailed flow, ERROR for exceptions
- Log API requests and responses (without sensitive data)
- Include correlation IDs for request tracking
- Structured logging with JSON format in production

### GUI Logging
- Log API calls and responses
- Log exceptions with stack traces
- Don't log user credentials or sensitive data
- Use java.util.logging or SLF4J
- Log to file for troubleshooting

## Documentation Requirements

- Document all REST endpoints with Swagger/OpenAPI
- Include example requests and responses
- Document authentication requirements
- API versioning strategy
- Database schema documentation
- Docker setup instructions in README
- Migration guide from old to new architecture

