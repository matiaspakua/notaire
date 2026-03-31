# DevSecOps Pipeline

## Overview

This document describes the DevSecOps pipeline implemented in the Notaire project using GitHub Actions.

## Pipeline Architecture

The pipeline is divided into two main workflows:

1. **CI (Continuous Integration)**: `.github/workflows/ci.yml`
2. **CD (Continuous Deployment)**: `.github/workflows/cd.yml`

---

## CI Workflow: Build, Test & Security

### Triggers

- Push to `main`, `develop`, `feature/**`, `bugfix/**` branches
- Pull requests to `main`
- Manual workflow dispatch

### Jobs

#### 1. Build & Compile
- Sets up JDK 21 (Temurin distribution)
- Builds all modules with Maven
- Extracts project version for downstream jobs

#### 2. Unit Tests
- Runs unit tests for `backend-api` and `frontend-swing` modules
- Uses `-am` flag to build dependent modules first
- Generates JUnit XML reports
- Publishes test results with dorny/test-reporter

#### 3. Integration Tests
- Runs integration tests for backend API
- Uses Testcontainers for database isolation
- Generates coverage reports

#### 4. Code Coverage (JaCoCo)
- Runs all tests with coverage enabled
- Generates JaCoCo HTML and XML reports
- Posts coverage comment on PRs (requires 80% minimum)
- Uses madrapps/jacoco-report action

#### 5. Security Scan
- Runs Trivy vulnerability scanner on source code
- Generates SARIF format results
- Uploads results to GitHub Security tab
- Checks for CRITICAL and HIGH severity vulnerabilities

#### 6. Build Docker Image
- Builds Docker image using Buildx
- Scans image with Trivy for vulnerabilities
- Does NOT push (push only happens on CD)

#### 7. Code Quality (SpotBugs)
- Runs SpotBugs static analysis
- Generates XML report for review

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

- Push to `main` branch
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
| Frontend Swing | `frontend-swing/target/surefire-reports/*.xml` |

### Coverage Reports

- Location: `backend-api/target/site/jacoco/`
- Format: HTML and XML
- Minimum threshold: 80%

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
