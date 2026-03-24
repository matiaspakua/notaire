# Code Quality Tools

This document describes the code quality tools used in the Notaire project.

## Overview

The project uses the following code quality tools:

| Tool | Purpose | Config File |
|------|---------|-------------|
| **JaCoCo** | Code coverage analysis | `pom.xml` |
| **Checkstyle** | Code style enforcement | `checkstyle.xml` |
| **SpotBugs** | Static bug detection | `spotbugs-exclude.xml` |
| **Trivy** | Vulnerability scanning | Built-in |

## JaCoCo - Code Coverage

JaCoCo is used to measure code coverage during test execution.

### Configuration

Minimum coverage requirements (configured in `pom.xml`):
- **Line coverage**: 80%
- **Branch coverage**: 80%

### Running Coverage

```bash
# Run tests with coverage
mvn test -pl backend-api

# Generate HTML report
mvn jacoco:report -pl backend-api

# View report
open backend-api/target/site/jacoco/index.html
```

### CI Integration

Coverage reports are:
- Generated in CI pipeline
- Uploaded as artifacts
- Commented on PRs via madrapps/jacoco-action

## Checkstyle - Code Style

Checkstyle enforces coding standards and conventions.

### Configuration

The configuration is in `backend-api/checkstyle.xml`:

- **Indentation**: 4 spaces
- **Line length**: 120 characters
- **No wildcard imports**
- **Naming conventions**: camelCase for variables, PascalCase for classes
- **Import ordering**: java, javax, third-party, own packages

### Running Checkstyle

```bash
# Check for violations
mvn checkstyle:check -pl backend-api

# Generate report
mvn checkstyle:checkstyle -pl backend-api

# View report
open backend-api/target/checkstyle-result.html
```

### Ignoring Files

To skip checkstyle for specific files, add to the class JavaDoc:
```java
/**
 * @checkstyle ignore for 10 lines
 */
```

## SpotBugs - Bug Detection

SpotBugs performs static analysis to find potential bugs.

### Configuration

The configuration is in `backend-api/spotbugs-exclude.xml`.

### Known Issues

- SpotBugs does not support Java 21+ bytecode in some versions
- In CI, use Java 21: `mvn spotbugs:check -pl backend-api -DskipSpotBugs=false`
- Locally, SpotBugs is skipped by default due to Java compatibility

### Running SpotBugs

```bash
# Run SpotBugs (requires Java 21)
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false

# Generate XML report
mvn spotbugs:xml -pl backend-api

# View report
open backend-api/target/spotbugsXml.xml
```

### CI Integration

In GitHub Actions, SpotBugs runs with Java 21 and reports are uploaded as artifacts.

## Trivy - Security Scanning

Trivy scans for vulnerabilities in dependencies and Docker images.

### Running Trivy

```bash
# Scan filesystem
trivy fs .

# Scan Docker image
trivy image ghcr.io/matiaspakua/notaire/backend:latest

# Scan with specific severity
trivy fs . --severity HIGH,CRITICAL
```

### CI Integration

Trivy runs automatically in:
- CI pipeline (filesystem scan)
- CD pipeline (Docker image scan)
- Results uploaded as SARIF to GitHub Security tab

## Running All Quality Checks

```bash
# Run all checks
mvn verify -pl backend-api

# Skip tests but run static analysis
mvn verify -pl backend-api -DskipTests

# Run with full reports
mvn site -pl backend-api
```

## IDE Integration

### VS Code

Install extensions:
- Checkstyle for Java
- SonarLint
- Error Prone

### IntelliJ IDEA

- Install CheckStyle-IDEA plugin
- Import checkstyle.xml configuration
- Enable SpotBugs plugin

## CI Pipeline

The complete quality gate in CI:

1. **Build** - Compilation
2. **Unit Tests** - JUnit tests
3. **Integration Tests** - Spring Boot tests
4. **Coverage** - JaCoCo (80% minimum)
5. **Security** - Trivy vulnerability scan
6. **Code Quality** - Checkstyle + SpotBugs

All checks must pass before merging to main.
