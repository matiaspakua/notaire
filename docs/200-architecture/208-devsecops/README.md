# DevSecOps Pipeline

## Overview

This document describes the DevSecOps pipeline implemented in the Notaire project using GitHub Actions.

## Pipeline Architecture

The core build/test/deploy pipeline is split across two workflows, backed by several
supporting workflows for linting, E2E, and reporting:

1. **CI (Continuous Integration)**: `.github/workflows/ci.yml`
2. **CD (Continuous Deployment)**: `.github/workflows/cd.yml`

See [Other Workflows](#other-workflows) below for `pr-validation.yml` (Spotless/Checkstyle
lint gate), `frontend-ci.yml`, `playwright-e2e.yml`, `e2e-swing.yml`, and the rest.

---

## CI Workflow: Build, Test & Security

### Triggers

- Every pull request into `main`
- Every push to `main`
- Manual workflow dispatch

> Report/wiki-publishing jobs (`generate-reports`, `publish-reports`) are guarded to run
> only on push-to-`main` or manual dispatch — a PR run never publishes reports or pushes
> commits to `main`.

> **Test enforcement policy**: test failures FAIL the pipeline. No
> `continue-on-error` or `-Dmaven.test.failure.ignore` on test steps. The only
> tolerated flag is `-Dsurefire.failIfNoSpecifiedTests=false`, which allows
> modules with no tests matching a filter (`notaire-shared`); it never masks a
> failing test. The only acceptable skip is an environment limitation (e.g.
> Testcontainers tests auto-skip when Docker is unavailable — they run in CI).

### Jobs

#### 1. Build & Compile
- Sets up JDK 21 (Temurin distribution)
- Builds all modules with Maven
- Extracts project version for downstream jobs

#### 2. Unit Tests (with coverage)
- Backend API only: everything outside the `integration` package
  (`-Dtest='!**/integration/**'`); `deprecated-frontend-swing` is excluded from
  the root Maven reactor and not built by this pipeline
- Uploads `unit-test-report` artifact: surefire XML + JaCoCo coverage report
- Publishes test results with dorny/test-reporter

#### 3. Integration Tests (with coverage)
- H2-based tests: `-Dtest='**/integration/**'`
- Testcontainers/PostgreSQL tests: `-Ppg-integration` (Flyway schema validation)
- Uploads `integration-test-report` artifact: surefire XML + JaCoCo coverage report

#### 4. Coverage Gate (`mvn verify`)
- Runs the full suite once; `jacoco:check` enforces the ratchet floor
  (70% line / 25% branch as of Phase 8; target 80/80)
- Uploads combined `jacoco-report` and `coverage-snapshot` artifacts

#### 5. Security Scan
- Runs Trivy vulnerability scanner on source code (report-only)
- Uploads JSON results as artifact

#### 6. Build Docker Image
- Runs only after unit, integration and coverage jobs succeed
- Builds Docker image using Buildx
- Does NOT push (push only happens on CD, gated on CI success)

#### 7. Code Quality (SpotBugs)
- Runs SpotBugs static analysis (report-only)
- Generates XML report for review

#### 8. Generate Markdown Reports
- Runs only on push-to-`main` or manual dispatch
- Aggregates test/coverage results into Markdown summaries

#### 9. Publish to Wiki
- Runs only on push-to-`main` or manual dispatch
- Publishes the generated reports to the GitHub Wiki

### Permissions

```yaml
permissions:
  contents: read
  pull-requests: write
  security-events: write
```

---

## CD Workflow: Build & Publish Docker

### Triggers

- `workflow_run`: after **CI - Build, Test & Security** completes successfully
  on `main` (the image is never published if any test job failed)
- Version tags (`v*`)
- Manual workflow dispatch

### Jobs

#### 1. Build & Publish Docker Image
- Builds multi-platform Docker image
- Logs into GHCR (GitHub Container Registry)
- Generates SBOM (Software Bill of Materials) with Trivy
- Tags with branch, semver, and sha

#### 2. Create GitHub Release
- Triggered only on version tags (`v*`)
- Uses softprops/action-gh-release
- Generates release notes automatically

#### 3. Update Container Registry Description
- Updates Docker Hub/GHCR description
- Requires DOCKERHUB_USERNAME and DOCKERHUB_TOKEN secrets
- Conditional execution (skipped if secrets not configured)

---

## Security Features

### Vulnerability Scanning

| Tool | Purpose | Type |
|------|---------|------|
| Trivy | Scan source code and Docker images | OS and library vulnerabilities |
| SpotBugs | Static code analysis | Code quality bugs |

### Security Best Practices Implemented

1. **Least Privilege Permissions**: Jobs only request required permissions
2. **Secret Handling**: Uses GitHub secrets for sensitive data
3. **Container Security**: Scans Docker images before publishing
4. **SBOM Generation**: Creates Software Bill of Materials for traceability

---

## Test Reporting

### Test Report Locations

| Module | Path |
|--------|------|
| Backend API | `backend-api/target/surefire-reports/*.xml` |

### Coverage Reports

- Location: `backend-api/target/site/jacoco/`
- Format: HTML and XML
- Enforced ratchet floor (raised as coverage improves; see [Code Quality](../../300-development/303-testing/README.md)); long-term target 80% line / 80% branch

---

## Configuration

### Environment Variables

```yaml
env:
  JAVA_VERSION: '21'
  MAVEN_OPTS: -Xmx1024m -XX:MaxMetaspaceSize=512m
```

### Required Secrets

| Secret | Purpose |
|--------|---------|
| `DOCKERHUB_USERNAME` | Docker Hub authentication |
| `DOCKERHUB_TOKEN` | Docker Hub access token |

---

## Troubleshooting

### Common Issues

1. **Dependency Resolution Errors**: Ensure `-am` flag is used to build dependent modules
2. **Test Reports Not Found**: Check that `surefire.failIfNoSpecifiedTests=false` is set
3. **Security Events Permission Denied**: Add `security-events: write` to workflow permissions

### Viewing Results

- **Tests**: GitHub PR check results
- **Coverage**: PR comment or artifacts
- **Security**: GitHub Security tab
- **Docker**: GHCR package registry

---

## Other Workflows

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `pr-validation.yml` | PR opened/synchronized, manual dispatch | Fast-feedback gate: Spotless format check (`mvn spotless:check`, job "Code Lint"), Checkstyle, other PR-blocking checks — this is the only place Spotless runs (see [CI Preflight](../../300-development/CI-PREFLIGHT.md)) |
| `frontend-ci.yml` | PR into `main`, push to `main`, manual dispatch | Next.js build, typecheck, unit tests |
| `playwright-e2e.yml` | Schedule (weekdays 06:00 UTC), manual dispatch | Full Playwright E2E suite against a running stack |
| `e2e-swing.yml` | Manual dispatch (optional `suite` input) | Robot Framework E2E tests for the deprecated Swing client |
| `test-coverage-report.yml` | Daily schedule (02:00 UTC), manual dispatch | Publishes a standalone coverage report artifact |
| `performance-test.yml` | Weekly schedule (Mondays 04:00 UTC) | k6 load test |
| `deploy-github-page.yml` | After CI succeeds on `main` | Publishes the GitHub Pages documentation site |
| `claude.yml` / `opencode.yml` | Issue/PR comment events | AI coding-agent triggers (Claude Code, OpenCode) |
| `copilot-setup-steps.yml` | Push/PR touching itself, manual dispatch | Environment setup used by GitHub Copilot coding agent |

---

## Future Enhancements

1. Add OWASP ZAP for API security testing
2. Implement Snyk for additional vulnerability scanning
3. Add dependency review action
4. Implement GitHub Advanced Security
5. Add secret scanning with GitLeaks
6. Implement SLSA provenance attestation

---

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Trivy GitHub Action](https://github.com/aquasecurity/trivy-action)
- [SpotBugs Maven Plugin](https://spotbugs.github.io/)
- [JaCoCo GitHub Action](https://github.com/madrapps/jacoco-report)
