# Code Quality Tools

This document describes the code quality tools used in the Notaire project.

## Overview

The project uses the following code quality tools:

| Tool | Purpose | Config File |
|------|---------|-------------|
| **JaCoCo** | Backend code coverage analysis | `pom.xml` |
| **Vitest coverage** | Frontend unit coverage ratchet | `frontend/vitest.config.ts` |
| **ESLint boundaries** | Frontend hexagon import rules with a shrink-only baseline (#1696) | `frontend/eslint.config.mjs`, `frontend/eslint-suppressions.json` |
| **Checkstyle** | Code style enforcement | `checkstyle.xml` |
| **SpotBugs** | Static bug detection | `spotbugs-exclude.xml` |
| **Trivy** | Vulnerability scanning | Built-in |
| **ArchUnit** | Backend hexagon dependency rules with a shrink-only baseline (#1695) | `HexagonArchitectureTest`, `src/test/resources/architecture/hexagon-baseline.txt` |
| **SonarQube** | Backend + frontend analysis, quality gate on new code (`bash infra/scripts/run-sonar.sh`) | `backend-api/pom.xml`, `frontend/sonar-project.properties` |

### SonarQube new-code and SCM (#1680)

The quality gate judges **new code** only. `infra/scripts/run-sonar.sh` must mount
the monorepo (so `.git` is visible) and set `sonar.scm.provider=git` for the
frontend scanner container. Mounting only `frontend/` leaves SCM undetected, so
Community treats the whole tree as new and the gate fails with legacy
violations. Backend analysis forces `-Dsonar.projectKey=notaire-backend` so the
Maven reactor root does not publish under `notaire-root`.

### SonarQube severity ratchet (#1737)

The **Notaire way** gate (applied by `run-sonar.sh`) also fails when **new**
`BLOCKER`, `CRITICAL`, or `MAJOR` violations exceed zero
(`new_blocker_violations` / `new_critical_violations` / `new_major_violations`).
Overall (legacy) severity inventory remains under [#1535](https://github.com/matiaspakua/notaire/issues/1535);
jpa `S4973` / SpotBugs RC wait for hexagonal [#1693](https://github.com/matiaspakua/notaire/issues/1693).
After a local Sonar run, print open severity counts with:

```bash
python3 infra/scripts/sonar-severity-report.py
```

## JaCoCo - Code Coverage

JaCoCo is used to measure code coverage during test execution.

### Configuration

Coverage is split into an **aspirational target** and an **enforced ratchet floor**:

- **Long-term target**: 80% line / 80% branch.
- **Enforced gate** (`jacoco:check`, bound to `mvn verify`, BUNDLE scope, legacy
  `jpa`/`service.Administrador*` packages excluded): a ratchet floor of
  **80% line / 65% branch** as of 2026-09-28 (#1063) (actual: ~84% line / ~74% branch
  as of 2026-07-23). The build fails if coverage drops below the floor.
- **Policy**: raise the floor as coverage improves; never lower it. The historical
  per-class 80% rule was bound to `<phase>none</phase>` and never ran, so the prior
  "80% enforced" claim was inaccurate.

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

## ESLint — Frontend hexagon boundaries (shrink-only)

`npm run lint` (CI job Frontend CI) enforces the import boundaries of ADR-030 (epic #1693):

- `src/app/**`, `src/components/**`, `src/hooks/**` may not import `lib/api-client` or call `fetch`; they call
  use cases. Only `adapters/http` may import the client.
- `src/core/**` may not import React, Next, TanStack, `lib`, adapters or UI code, nor use `fetch` or browser
  globals.

Violations that exist today are recorded by ESLint's own suppression file `frontend/eslint-suppressions.json`
(counted per file and rule). A **new** violation fails the lint, and so does a suppression whose violation no
longer exists (exit code 2). After fixing a violation, shrink the baseline and commit the smaller file:

```bash
cd frontend && npx eslint src --prune-suppressions
```

Never add suppressions by hand; `--suppress-rule` is only for the first generation. The goal is an empty file.
`frontend/src/tests/unit/architecture-boundaries.test.ts` pins the rules.

## Vitest — Frontend coverage floor (raise-only)

Frontend unit coverage uses Vitest `@vitest/coverage-v8` with global thresholds in
`frontend/vitest.config.ts`. Same policy as JaCoCo: **raise-only ratchet floor**.

### Configuration (as of 2026-10-14, #1674)

| Metric | Enforced floor | Measured 2026-10-14 (#1674) |
|--------|----------------|----------------------------------|
| Statements | **76%** | 78.72% |
| Branches | **68%** | 70.11% |
| Functions | **72%** | 74.16% |
| Lines | **78%** | 80.02% |

- **Long-term target**: 80% line / 80% branch (aspirational; not this gate).
- **Policy**: raise floors as coverage improves with ≥1 percentage point headroom
  (2 points since #1663) under a fresh `npx vitest run --coverage` measurement; **never lower** a floor
  without an ADR or documented SDLC exception.
- **Guard**: `frontend/src/tests/unit/vitest-coverage-thresholds.test.ts` asserts
  configured thresholds stay ≥ the documented floors.

### Root cause of the historical <6% branch failure (#976)

In 2026-07-29 the branch floor was set to **6%** as a low aspirational ratchet with
headroom. By 2026-09, coverage denominators grew faster than tests (untouched
branches in new UI), and measured branches briefly sat at **5.85%**, failing
Frontend CI on `main` even for unrelated PRs. Later unit tests (auth, admin-access,
hooks, proxy/BFF, etc.) restored branches above 6%. #976 closes the remaining gap by
**raising** floors toward current reality and documenting the raise-only rule so the
floor cannot silently rot or be lowered to paper over a red build.

### Running Coverage

```bash
cd frontend
npx vitest run --coverage
# or: npm run test:coverage
open coverage/index.html
```

### CI Integration

Frontend CI runs Vitest with `--coverage`; the job fails if any metric dips under
the configured thresholds.

## ArchUnit — Hexagon guard (shrink-only)

`HexagonArchitectureTest` (epic #1693, ADR-030) runs in `mvn test` and checks six rules on production classes:
R1 inbound adapters do not reach repositories, legacy `jpa` or entities; R2 the application layer depends on
domain and ports only (Spring is limited to `@Service` and `@Transactional`, ADR-021); R3 the domain depends on
the JDK only; R4 and R5 inbound and outbound adapters do not know each other; R6 nothing new depends on the
legacy `jpa` and `service` packages.

Violations that exist today are listed in `src/test/resources/architecture/hexagon-baseline.txt`
(`R<n> | origin -> target`). The rule is a ratchet: a **new** violation fails the build, and so does a baseline
entry that no longer exists. After fixing a violation run

```bash
mvn test -pl backend-api -Dtest=HexagonArchitectureTest -Darchitecture.baseline=shrink
```

and commit the smaller file. Never add a line by hand; `-Darchitecture.baseline=init` is only for the first
generation. The goal is an empty baseline.

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

- SpotBugs does not support the newest Java bytecode in some versions
- In CI, use Java 26: `mvn spotbugs:check -pl backend-api -DskipSpotBugs=false`
- Locally, SpotBugs is skipped by default due to Java compatibility

### Running SpotBugs

```bash
# Run SpotBugs (requires Java 26)
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false

# Generate XML report
mvn spotbugs:xml -pl backend-api

# View report
open backend-api/target/spotbugsXml.xml
```

### CI Integration

In GitHub Actions, SpotBugs runs with Java 26 and reports are uploaded as artifacts.

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
4. **Coverage** - JaCoCo (backend) + Vitest thresholds (frontend); raise-only floors; 80% target
5. **Security** - Trivy vulnerability scan
6. **Code Quality** - Checkstyle + SpotBugs

All checks must pass before merging to main.
