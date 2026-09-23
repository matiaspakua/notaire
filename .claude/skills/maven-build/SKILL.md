---
name: maven-build
description: Build, test and package Java Maven projects. Use for compiling, running tests, or packaging the application.
---

# Maven Build Skill

This skill provides guidance for Maven build operations in the Notaire project.

## Available Commands

```bash
# Build entire project
mvn clean install

# Build specific module with dependencies
mvn clean install -pl <module> -am

# Package without tests
mvn clean package -DskipTests

# Run tests for specific module
mvn test -pl backend-api

# Run specific test class
mvn test -Dtest=ClassNameTest

# Run tests with coverage
mvn test
mvn jacoco:report

# Check for issues
mvn spotbugs:check
mvn checkstyle:check
```

## Project Modules

| Module | Description |
|--------|-------------|
| backend-api | Spring Boot REST API |
| notaire-shared | Shared DTOs and code |

`frontend-swing` was removed from the repository (see `CLAUDE.md`); do not
recreate it or reference it in build commands. All new client work belongs
in `frontend/` (Next.js), which is built with `npm`/`next`, not Maven.

## Common Issues

### Dependency Resolution
- Use `-am` flag to build dependent modules first
- Run `mvn dependency:resolve` to download dependencies

### Test Failures
- Use `-DfailIfNoTests=false` for modules without tests
- Use `-Dmaven.test.failure.ignore=true` to continue on test failures

### Coverage
- JaCoCo enforces a ratchet floor of 70% line / 25% branch coverage at `mvn verify`; 80% line/branch is the long-term target (see `.claude/rules/code-quality.md`)
- Check report at: `target/site/jacoco/index.html`