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
mvn test -pl frontend-swing

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
| frontend-swing | Swing GUI client |
| notary-shared | Shared DTOs and code |

## Common Issues

### Dependency Resolution
- Use `-am` flag to build dependent modules first
- Run `mvn dependency:resolve` to download dependencies

### Test Failures
- Use `-DfailIfNoTests=false` for modules without tests
- Use `-Dmaven.test.failure.ignore=true` to continue on test failures

### Coverage
- JaCoCo requires 80% minimum coverage
- Check report at: `target/site/jacoco/index.html`