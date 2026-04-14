# Development - Notaire Project

Documentación para desarrolladores: setup, build, testing, y contribución.

> Nota: la documentación de ejecución de pruebas y test automation está consolidada en `docs/03-development/03-testing/`.

## Quick Start

### 1. Setup Local Environment
```bash
# Clone repository
git clone https://github.com/matiaspakua/notaire.git
cd notaire

# Install dependencies (Maven)
mvn clean install

# Start database + backend
bash scripts/start.sh

# Check services
bash scripts/logs.sh
```

**Detailed:** [Development Setup](01-setup/README.md)

### 2. Run Backend API
```bash
# Development mode
cd backend-api
mvn spring-boot:run

# Swagger UI available at:
# http://localhost:8080/swagger-ui.html
```

### 3. Run Frontend GUI
```bash
# Build and run Swing GUI
cd frontend-swing
mvn clean install
java -jar target/frontend-swing-1.0.0.jar
```

### 4. Run Tests
```bash
# All tests
mvn test -pl backend-api

# Unit tests only
mvn test -pl backend-api -Dtest="**/unit/*"

# Integration tests (requires PostgreSQL running)
mvn test -pl backend-api -Dtest="**/integration/*"

# Coverage check
mvn jacoco:check -pl backend-api
```

## Development Guide

### [01. Environment Setup](01-setup/)
- Java 21 installation
- Docker setup (PostgreSQL, pgAdmin)
- IDE configuration (VS Code, IntelliJ)
- Local database initialization

### [02. Build & Deploy](02-build/)
- Maven build commands
- Building individual modules
- Docker container builds
- Docker Compose deployment

### [03. Testing](03-testing/)
- Unit testing standards
- Integration testing setup
- E2E testing with Swing
- Code coverage requirements (80% minimum)

### [04. Code Standards](04-code-standards/)
- Naming conventions
- Code formatting (120 char lines, 4-space indent)
- Import ordering
- Javadoc requirements

## Project Structure

```
notaire/
├── backend-api/              # Spring Boot REST API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/licensis/notaire/
│   │   │   │   ├── api/              # REST Controllers
│   │   │   │   ├── service/          # Business logic
│   │   │   │   ├── repository/       # Data access
│   │   │   │   ├── negocio/          # Domain entities
│   │   │   │   ├── exception/        # Custom exceptions
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   └── config/           # Spring config
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── reportes/         # JasperReports
│   │   │       └── liquibase/        # DB migrations
│   │   └── test/
│   │       ├── unit/                 # Unit tests
│   │       └── integration/          # Integration tests
│   └── pom.xml
│
├── frontend-swing/           # Java Swing GUI
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/licensis/notaire/
│   │   │       ├── gui/              # Swing frames/panels
│   │   │       ├── client/           # REST client
│   │   │       ├── controller/       # GUI controllers
│   │   │       ├── model/            # GUI models
│   │   │       └── util/             # Utilities
│   │   └── test/
│   └── pom.xml
│
├── notaire-shared/           # Shared DTOs & utils
│   ├── src/
│   │   └── main/
│   │       └── java/com/licensis/notaire/shared/
│   │           ├── dto/              # Common DTOs
│   │           ├── constant/         # Constants
│   │           └── util/             # Shared utilities
│   └── pom.xml
│
├── .claude/                  # Claude Code configuration
│   ├── CLAUDE.md             # Project guidance
│   ├── rules/                # Code standards
│   └── skills/               # Reusable skills
│
├── docs/                     # Documentation
│   ├── 01-business/          # Business requirements
│   ├── 02-architecture/      # Architecture & ADRs
│   ├── 03-development/       # Developer guides (THIS)
│   ├── 04-operations/        # Deployment & ops
│   ├── 05-api/               # API documentation
│   └── 06-learning/          # Learning resources
│
├── init-db/                  # Database initialization scripts
├── scripts/                  # Utility scripts
│   ├── start.sh              # Start PostgreSQL + backend
│   ├── stop.sh               # Stop services
│   ├── logs.sh               # View logs
│   └── test.sh               # Run integration tests
│
├── docker-compose.yml        # Multi-container setup
├── pom.xml                   # Maven aggregator
└── README.md                 # Main project README
```

## Development Workflow

### 1. Feature Development

```bash
# Create feature branch
git checkout -b ISSUE-ID/feat/short-name

# Make changes following code standards
# Edit code in backend-api, frontend-swing, or notaire-shared

# Test locally
mvn test -pl backend-api
mvn checkstyle:check -pl backend-api

# Commit with conventional commits
git commit -m "feat(api): add endpoint for escrituras

Closes #ISSUE-ID"

# Push and create PR
git push origin ISSUE-ID/feat/short-name
```

### 2. Code Review Checklist

Before merging, verify:
- [ ] All tests passing (`mvn test`)
- [ ] Code quality checks passing (`mvn checkstyle:check`)
- [ ] Coverage >= 80% (`mvn jacoco:report`)
- [ ] No SpotBugs issues (`mvn spotbugs:check`)
- [ ] PR linked to GitHub issue
- [ ] Commit messages follow Conventional Commits
- [ ] Documentation updated if needed

### 3. Deployment

```bash
# Build release
mvn clean install -DskipTests

# Create Docker images
docker-compose build

# Deploy to environment
docker-compose up -d

# Verify health
curl http://localhost:8080/actuator/health
```

## Key Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Runtime** | Java 21 LTS | Application runtime |
| **Framework** | Spring Boot 4.0.4 | Backend framework |
| **Build Tool** | Maven 3.9 | Dependency & build management |
| **Database** | PostgreSQL 16 | Data persistence |
| **ORM** | Hibernate 6 | Object-relational mapping |
| **API Docs** | OpenAPI 3.0 / Swagger | API documentation |
| **Testing** | JUnit 5, Mockito, AssertJ | Testing framework |
| **Code Quality** | JaCoCo, Checkstyle, SpotBugs | Quality checks |
| **Container** | Docker | Application containerization |
| **Orchestration** | Docker Compose | Local development |

## Common Tasks

### Update Dependencies
```bash
# Check for updates
mvn versions:display-dependency-updates

# Update version in parent pom
mvn versions:set -DnewVersion=1.1.0
```

### Add New Feature
1. Create GitHub issue
2. Create feature branch: `ISSUE-ID/feat/name`
3. Implement feature following architecture
4. Write tests (80% coverage required)
5. Create PR linked to issue
6. Get code review approval
7. Merge and deploy

### Debug Test Failures
```bash
# Run single test with debug output
mvn test -pl backend-api -Dtest=MyTest -DargLine="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# Run integration tests
mvn test -pl backend-api -Dtest="**/integration/*"
```

### Performance Profiling
```bash
# Generate JFR recording
java -XX:+UnlockCommercialFeatures \
     -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=myrecording.jfr \
     -jar backend-api/target/notaire-backend-api.jar
```

## Troubleshooting

### PostgreSQL Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# View logs
docker logs notaire-postgres

# Reset database
docker-compose down -v
docker-compose up -d postgres
```

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Tests Failing Locally but Passing in CI
```bash
# Run exact same Maven command as CI
mvn clean verify -pl backend-api

# Check coverage report
open backend-api/target/site/jacoco/index.html
```

## Contributing

### Code Standards
- See [04. Code Standards](04-code-standards/) for detailed guidelines
- Follow Conventional Commits for commit messages
- 80% test coverage minimum
- 120 character line limit
- 4-space indentation

### Pull Request Process
1. Create feature branch from `main`
2. Implement feature + tests
3. Ensure all checks pass
4. Link PR to GitHub issue
5. Request code review
6. Address feedback
7. Merge when approved

### Git Workflow

```
main (stable)
  ↑
  └─ ISSUE-ID/feat/name (feature branch)
         ↓
       (develop feature)
       (write tests)
       (code review)
         ↓
       (merge to main)
```

## Resources & References

- [CLAUDE.md](../../CLAUDE.md) - Project guidance & rules
- [Architecture Guide](../02-architecture/) - System architecture
- [Code Quality Standards](../../.claude/rules/code-quality.md)
- [Programming Guide](../../.claude/rules/programming.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/)

## Getting Help

- **Architecture questions**: Check [Architecture ADRs](../02-architecture/01-adr/)
- **Code standards**: Check [Code Standards](04-code-standards/)
- **Build issues**: Check [Build Guide](02-build/)
- **Testing**: Check [Testing Guide](03-testing/)
- **Project guidance**: Check [CLAUDE.md](../../CLAUDE.md)

## Navigation

- [← Back to Docs](../)
- [Business Documentation](../01-business/)
- [Architecture](../02-architecture/)
- [Operations](../04-operations/)
- [API Reference](../05-api/)
