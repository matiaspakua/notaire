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
>
> **Test enforcement policy**: test failures FAIL the pipeline. No
> `continue-on-error` or `-Dmaven.test.failure.ignore` on test steps. The only
> tolerated flag is `-Dsurefire.failIfNoSpecifiedTests=false`, which allows
> modules with no tests matching a filter; it never masks a
> failing test. The only acceptable skip is an environment limitation (e.g.
> Testcontainers tests auto-skip when Docker is unavailable — they run in CI).

### Jobs

#### 1. Build & Compile

- Sets up JDK 21 (Temurin distribution)
- Builds all modules with Maven
- Extracts project version for downstream jobs

#### 2. Unit Tests (with coverage)

- Backend API only: everything outside the `integration` package
  (`-Dtest='!**/integration/**'`); Swing modules were removed (#1046) and must
  not be rebuilt by this pipeline (#811)
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

### Images (backend + frontend, #1043)

Matrix job `build-and-publish` publishes both:

| Component | Dockerfile | GHCR repository |
|-----------|------------|-----------------|
| `backend` | `./backend-api/Dockerfile` (context `.`) | `ghcr.io/<owner>/notaire/backend` |
| `frontend` | `./frontend/Dockerfile` (context `./frontend`) | `ghcr.io/<owner>/notaire/frontend` |

Each matrix leg generates a CycloneDX SBOM (`sbom-backend` / `sbom-frontend`
artifacts), cosign keyless-signs the digest, and attests the SBOM
(`cosign attest --type cyclonedx`). Same supply-chain controls as the former
backend-only path (#681); frontend publish closes the gap tracked in #1043.

### Pin to CI-tested SHA (#1042)

On `workflow_run`, `build-and-publish` must publish the exact commit CI tested
(both matrix legs):

1. **Checkout** `ref: ${{ github.event.workflow_run.head_sha || github.sha }}`
   (tag / `workflow_dispatch` fall back to `github.sha`).
2. **Resolve publish SHA** from `workflow_run.head_sha` (or `github.sha` otherwise);
   immutable image tags use that SHA (full + short). Do not rely on metadata
   `type=sha` alone under `workflow_run`.
3. **Push immutable SHA-tagged image first**, then **move `latest`** to the same
   digest only after that push succeeds (`docker buildx imagetools create`).
4. Report jobs publish artifacts / job summary only; they must not redefine
   which git SHA the image was built from (#1041).

Guarded by `workspace/tests/test_cd_pin_tested_sha.py` and
`workspace/tests/test_frontend_ghcr_publish.py`.

### Semver releases (release-please, #1043)

- Workflow: `.github/workflows/release-please.yml` + `release-please-config.json`
- Opens a release PR on Conventional Commits; merge creates `vX.Y.Z` + GitHub
  Release and bumps Maven (`pom.xml` + module parent versions) and
  `frontend/package.json` to `X.Y.Z`, rolling `CHANGELOG.md`.
- CD on `v*` publishes both images; the CD `release` job **attaches SBOM
  assets** only (release-please owns Release creation — no duplicate softprops
  notes).
- Operator runbook: [docs/300-development/RELEASE.md](../../300-development/RELEASE.md).
- Respects protect-main (#1040): release PRs use required checks; no durable
  Actions bypass.

Guarded by `workspace/tests/test_semver_release_process.py`.

### Jobs

#### 1. Build & Publish Docker Image (matrix)

- Checks out the CI-tested SHA on `workflow_run` (see pin above)
- Builds and pushes **backend** and **frontend** images to GHCR
- Pushes immutable tags (branch/semver when applicable + publish SHA); then tags
  `latest` to the same digest when appropriate
- Generates CycloneDX SBOM (Trivy), cosign sign, cosign attest per image

#### 2. Attach SBOM assets to GitHub Release

- Triggered only on version tags (`v*`)
- Downloads `sbom-backend` / `sbom-frontend` artifacts
- Uses softprops/action-gh-release to attach SBOM files to the release already
  created by release-please (`generate_release_notes: false`)

#### 3. Update Container Registry Description

- Updates Docker Hub/GHCR description (backend)
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
3. Disable default setup (admin): `bash security/enable-gh-secure.sh --apply`
   (script PATCHes `code-scanning/default-setup` to `not-configured`), or
   Settings → Code security → Code scanning → disable default setup.
4. Do **not** pass `code-scanning` to gh-secure’s enable list — that turns
   default setup back on and re-breaks advanced SARIF.

Status / dry-run:

```bash
bash security/enable-gh-secure.sh            # status
bash security/enable-gh-secure.sh --dry-run  # preview
bash security/enable-gh-secure.sh --apply    # admin/maintain token
```

Merge-when-green still requires heavy CI (Integration, Coverage, Bruno,
Playwright) via `bash workspace/sdlc/check-heavy-ci.sh <pr>` — see
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
  JAVA_VERSION: '26'
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
| `dast-zap.yml` | Weekly schedule (Mondays 05:00 UTC) + manual dispatch | OWASP ZAP baseline DAST against a live API (report artifact; warn-first policy — does not gate every PR). See [DAST / OpenAPI / backup-restore](#dast--openapi--backup-restore-issue-1067). |
| `openapi-contract.yml` | PR into `main` + manual dispatch | Regenerates OpenAPI from springdoc, fails if `backend-api/openapi/openapi.yaml` is stale, and fails on breaking changes vs the base branch (`oasdiff`) unless they are listed in `backend-api/openapi/accepted-breaking-changes.txt` |
| `backup-restore-smoke.yml` | Weekly schedule (Sundays 03:00 UTC) + manual dispatch | Backup→restore→smoke once `#256` lands (`infra/scripts/backup-postgres.sh`); until then skips with an explicit blocked-on-#256 notice (no false-green restore) |
| `deploy-github-page.yml` | After CI succeeds on `main` | Publishes the GitHub Pages documentation site |
| `claude.yml` / `opencode.yml` | Issue/PR comment events | AI coding-agent triggers (Claude Code, OpenCode) |
| `copilot-setup-steps.yml` | Push/PR touching itself, manual dispatch | Environment setup used by GitHub Copilot coding agent |
| `codeql.yml` | PR into `main`, push to `main`, weekly schedule, manual dispatch | CodeQL advanced setup. Findings upload to the Security tab. Do not also enable default setup — see [CodeQL advanced vs default setup](#codeql-advanced-vs-default-setup) |

### DAST / OpenAPI / backup-restore (issue #1067)

| Gate | How to run | Policy |
|------|------------|--------|
| **OWASP ZAP baseline** | Actions → `DAST — OWASP ZAP Baseline` (schedule/`workflow_dispatch`) | Targets `http://localhost:8080` after starting the API + Postgres service. Uploads the ZAP report artifact. **Warn-first** (`fail_action: false`) until an allowlist/ratchet is agreed; Trivy SCA in `ci.yml` remains. Full prose operator guide may remain #281. |
| **OpenAPI contract** | Every PR (`openapi-contract.yml`); locally: `bash backend-api/tools/export-openapi.sh`, and `bash workspace/sdlc/preflight.sh` runs the breaking diff and the stale-entry check when `oasdiff` is installed | Committed SSOT: `backend-api/openapi/openapi.yaml`. After API changes, regenerate and commit in the same PR. CI fails on stale artifact or `oasdiff` ERR-level breaking diffs vs base. An intended break is accepted by adding the exact `oasdiff` line to `backend-api/openapi/accepted-breaking-changes.txt` under a `# #<issue>` comment that says why no working client breaks, plus a CHANGELOG entry; the Owner approves it in review. Any break not listed still fails. The list is **empty by default** (#1315): an entry lives only in the PR that introduces its break. After the merge the base already contains that break, so on every later PR `workspace/sdlc/check-accepted-breaking-changes.py` (step "Accepted breaking list has no stale entries", and preflight) fails until the entry and its comment are removed. |
| **Backup→restore smoke** | Schedule/`workflow_dispatch` | Gated on #256. Sentinel path: `infra/scripts/backup-postgres.sh`. While absent, the job exits 0 with a clear skip/blocked message and does **not** claim a successful restore. |

Guarded by `python3 workspace/tests/test_dast_contract_backup_assets.py` (also under `scripts/tests/` for Process Checks).

---

## GitHub Security Lab baseline

[gh-secure](https://github.com/GitHubSecurityLab/gh-secure) turns on repository
settings. The pieces that belong in git are already in the tree:

| Setting | Where it lives |
|---------|----------------|
| Code scanning | `.github/workflows/codeql.yml` (advanced setup). Do not also enable default setup. |
| Dependabot version updates | `.github/dependabot.yml` (Maven, npm under `frontend/`, Docker for `/backend-api` + `/frontend`, GitHub Actions) |
| Private vulnerability reporting | Already enabled. Policy: `SECURITY.md` |
| Secret-scanning push protection, Dependabot alerts and security updates | GitHub settings. An admin runs `bash security/enable-gh-secure.sh --apply` |
| Branch protection / ruleset | Classic branch protection is **not** enabled by the script unless `--with-branch-protection` is passed (and must stay unused). Active ruleset `protect-main` (id `24128115`) on `~DEFAULT_BRANCH` enforces PR-only merges, required checks `CI` / `Frontend CI` / `Playwright E2E` / `Code Lint` / `PR Validation`, and blocks force-push/deletion; `bypass_actors` empty after #1041. Desired state: `security/rulesets/protect-main.desired.json`. Admin apply: `bash security/apply-protect-main-ruleset.sh --apply` then `bash security/assert-protect-main-ruleset.sh` (#1040) |

```bash
bash security/enable-gh-secure.sh            # status
bash security/enable-gh-secure.sh --dry-run  # preview
bash security/enable-gh-secure.sh --apply    # needs admin or maintain
```

## Future Enhancements

1. Ratchet OWASP ZAP baseline from warn-first to fail on CRITICAL/HIGH (allowlist as needed) — CI job already landed in #1067
2. Implement Snyk for additional vulnerability scanning
3. Add dependency review action
4. Add secret scanning with GitLeaks (push protection is the GitHub setting above)
5. Implement SLSA provenance attestation
6. Enable backup→restore→smoke execution when #256 ships `infra/scripts/backup-postgres.sh`

---

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Trivy GitHub Action](https://github.com/aquasecurity/trivy-action)
- [SpotBugs Maven Plugin](https://spotbugs.github.io/)
- [JaCoCo GitHub Action](https://github.com/madrapps/jacoco-report)
