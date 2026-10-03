# DevSecOps Pipeline

## Overview

This document describes the DevSecOps pipeline implemented in the Notaire project using GitHub Actions.

## Pipeline Architecture

The core build/test/deploy pipeline is split across two workflows, backed by several
supporting workflows for linting, E2E, and reporting:

1. **CI (Continuous Integration)**: `.github/workflows/ci.yml`
2. **CD (Continuous Deployment)**: `.github/workflows/cd.yml`

See [Other Workflows](#other-workflows) below for `pr-validation.yml` (Spotless/Checkstyle
lint gate), `frontend-ci.yml`, `playwright-e2e.yml`, `sdlc-process.yml`, and the rest.

---

## CI Workflow: Build, Test & Security

### Triggers

- Every pull request into `main`
- Every push to `main`
- Manual workflow dispatch

> Report jobs (`generate-reports`, `publish-reports`) are guarded to run only on
> push-to-`main` or manual dispatch. They publish workflow artifacts and
> `$GITHUB_STEP_SUMMARY` (and may be mirrored on GitHub Pages) — they never
> git-commit into `docs/wiki/cicd-reports/` (issue #1041).

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

#### 9. Publish report artifact
- Runs only on push-to-`main` or manual dispatch
- Surfaces the generated markdown via `$GITHUB_STEP_SUMMARY` and an Actions
  artifact (no git commits; see issue #1041)

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

- `workflow_run`: after **CI - Build, Test & Security** completes on `main`.
  Job `build-and-publish` runs only when
  `github.event.workflow_run.conclusion == 'success'` (non-success CI skips
  publish). Under `workflow_run`, `github.sha` is the tip of the default branch
  at CD schedule time — **not** the commit CI tested.
- Version tags (`v*`)
- Manual workflow dispatch

### Pin to CI-tested SHA (#1042)

On `workflow_run`, `build-and-publish` must publish the exact commit CI tested:

1. **Checkout** `ref: ${{ github.event.workflow_run.head_sha || github.sha }}`
   (tag / `workflow_dispatch` fall back to `github.sha`).
2. **Resolve publish SHA** from `workflow_run.head_sha` (or `github.sha` otherwise);
   immutable image tags use that SHA (full + short). Do not rely on metadata
   `type=sha` alone under `workflow_run`.
3. **Push immutable SHA-tagged image first**, then **move `latest`** to the same
   digest only after that push succeeds (`docker buildx imagetools create`).
4. Wiki/report jobs may still check out `main` for docs commits; they must not
   redefine which git SHA the image was built from.

Guarded by `scripts/test_cd_pin_tested_sha.py`.

### Jobs

#### 1. Build & Publish Docker Image
- Checks out the CI-tested SHA on `workflow_run` (see pin above)
- Builds Docker image and logs into GHCR (GitHub Container Registry)
- Pushes immutable tags (branch/semver when applicable + publish SHA); then tags
  `latest` to the same digest when appropriate
- Generates SBOM (Software Bill of Materials) with Trivy

#### 2. Create GitHub Release
- Triggered only on version tags (`v*`)
- Uses softprops/action-gh-release
- Generates release notes automatically

#### 3. Update Container Registry Description
- Updates Docker Hub/GHCR description
- Requires DOCKERHUB_USERNAME and DOCKERHUB_TOKEN secrets
- Conditional execution (skipped if secrets not configured)

#### 4. Generate CD Report
- Runs after build-and-publish, release, and update-description complete (always,
  unless the build job itself was skipped)
- Aggregates their outcomes into a Markdown summary (`reports/cd-report.md`)

#### 5. Publish CD report artifact
- Runs only when report generation succeeded, on `workflow_run` success or manual
  dispatch
- Surfaces `cd-report.md` via `$GITHUB_STEP_SUMMARY` and an Actions artifact
  (no git commits into `docs/wiki/cicd-reports/`; issue #1041). Does not change
  the image publish SHA.

---

## Security Features

### Vulnerability Scanning

| Tool | Purpose | Type |
|------|---------|------|
| Trivy | Scan source code and Docker images | OS and library vulnerabilities |
| SpotBugs | Static code analysis | Code quality bugs |
| CodeQL (`codeql.yml`) | Java, JavaScript/TypeScript, and GitHub Actions | Code scanning alerts (does not fail the job on findings); see [advanced vs default](#codeql-advanced-vs-default-setup) |

### Security Best Practices Implemented

1. **Least Privilege Permissions**: Jobs only request required permissions
2. **Secret Handling**: Uses GitHub secrets for sensitive data
3. **Container Security**: Scans Docker images before publishing
4. **SBOM Generation**: Creates Software Bill of Materials for traceability

### CodeQL advanced vs default setup

GitHub supports **either** Code Scanning default setup **or** an advanced
workflow (`.github/workflows/codeql.yml`), not both for the same repo.

If default setup is enabled while `codeql.yml` uploads SARIF, Analyze fails with
a processing rejection such as *"CodeQL analyses from advanced configurations
cannot be processed when the default setup is enabled"*.

Fleet / ops rules:

1. Prefer the advanced workflow in-repo (languages, build steps, schedule).
2. Set `wait-for-processing: false` on `github/codeql-action/analyze` so a
   leftover default-setup conflict does not fail the job after a successful
   analysis+upload; findings land once default setup is off.
3. Disable default setup (admin): `bash scripts/enable-gh-secure.sh --apply`
   (script PATCHes `code-scanning/default-setup` to `not-configured`), or
   Settings → Code security → Code scanning → disable default setup.
4. Do **not** pass `code-scanning` to gh-secure’s enable list — that turns
   default setup back on and re-breaks advanced SARIF.

Status / dry-run:

```bash
bash scripts/enable-gh-secure.sh            # status
bash scripts/enable-gh-secure.sh --dry-run  # preview
bash scripts/enable-gh-secure.sh --apply    # admin/maintain token
```

Merge-when-green still requires heavy CI (Integration, Coverage, Bruno,
Playwright) via `bash scripts/check-heavy-ci.sh <pr>` — see
[CI merge gate](../../300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md).
CodeQL is not a substitute for that gate.

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
| `playwright-e2e.yml` | PR into `main`, push to `main`, schedule (weekdays 06:00 UTC), manual dispatch | API tests (Bruno) + full Playwright UI E2E suite against a live stack (PostgreSQL + backend + frontend); test failures **fail the pipeline** (blocking on PRs, not report-only) |
| `sdlc-process.yml` | PR opened/synchronized/reopened/labeled | CONSTITUTION process gates: commit messages, TDD evidence, `sdlc-exception` label, agent-rule file lint, plus the scripts' self-tests |
| `test-coverage-report.yml` | Daily schedule (02:00 UTC), manual dispatch | Publishes a standalone coverage report artifact |
| `performance-test.yml` | Weekly schedule (Mondays 04:00 UTC) | k6 load test |
| `deploy-github-page.yml` | After CI succeeds on `main` | Publishes the GitHub Pages documentation site |
| `claude.yml` / `opencode.yml` | Issue/PR comment events | AI coding-agent triggers (Claude Code, OpenCode) |
| `copilot-setup-steps.yml` | Push/PR touching itself, manual dispatch | Environment setup used by GitHub Copilot coding agent |
| `codeql.yml` | PR into `main`, push to `main`, weekly schedule, manual dispatch | CodeQL advanced setup. Findings upload to the Security tab. Do not also enable default setup — see [CodeQL advanced vs default setup](#codeql-advanced-vs-default-setup) |

---

## GitHub Security Lab baseline

[gh-secure](https://github.com/GitHubSecurityLab/gh-secure) turns on repository
settings. The pieces that belong in git are already in the tree:

| Setting | Where it lives |
|---------|----------------|
| Code scanning | `.github/workflows/codeql.yml` (advanced setup). Do not also enable default setup. |
| Dependabot version updates | `.github/dependabot.yml` (Maven, npm under `frontend/`, GitHub Actions) |
| Private vulnerability reporting | Already enabled. Policy: `SECURITY.md` |
| Secret-scanning push protection, Dependabot alerts and security updates | GitHub settings. An admin runs `bash scripts/enable-gh-secure.sh --apply` |
| Branch protection / ruleset | Classic branch protection is **not** enabled by the script unless `--with-branch-protection` is passed (and must stay unused). Active ruleset `protect-main` (id `24128115`) on `~DEFAULT_BRANCH` enforces PR-only merges, required checks `CI` / `Frontend CI` / `Playwright E2E` / `Code Lint` / `PR Validation`, and blocks force-push/deletion; `bypass_actors` empty after #1041. Desired state: `scripts/rulesets/protect-main.desired.json`. Admin apply: `bash scripts/apply-protect-main-ruleset.sh --apply` then `bash scripts/assert-protect-main-ruleset.sh` (#1040) |

```bash
bash scripts/enable-gh-secure.sh            # status
bash scripts/enable-gh-secure.sh --dry-run  # preview
bash scripts/enable-gh-secure.sh --apply    # needs admin or maintain
```

## Future Enhancements

1. Add OWASP ZAP for API security testing
2. Implement Snyk for additional vulnerability scanning
3. Add dependency review action
4. Add secret scanning with GitLeaks (push protection is the GitHub setting above)
5. Implement SLSA provenance attestation

---

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Trivy GitHub Action](https://github.com/aquasecurity/trivy-action)
- [SpotBugs Maven Plugin](https://spotbugs.github.io/)
- [JaCoCo GitHub Action](https://github.com/madrapps/jacoco-report)
