# ADR-012: CI/CD pipeline with GitHub Actions

**Status:** Accepted
**Date:** 2026-03-24
**Deciders:** Matías Miguez
**Related:** ADR-006 (Testing Strategy), ADR-007 (Flyway), ADR-009 (Logging & Monitoring)

## Context

The project needs to check every change automatically (build, tests, coverage, security, code quality) before merging it to `main`, and to publish deployable artifacts without a manual step. The quality suite includes Checkstyle, SpotBugs, JaCoCo, Trivy, and Spotless, plus unit tests, integration tests, HTTP tests (`testing/`), and Playwright E2E — too many steps to run consistently by hand on every pull request.

## Decision

Use **GitHub Actions** as the only CI/CD platform, with workflows split by responsibility under `.github/workflows/`:

| Workflow | Responsibility |
|----------|------------------|
| `ci.yml` | Main pipeline: `build`, `unit-tests`, `integration-tests`, `coverage`, `security` (Trivy), `docker-build`, `quality` (Checkstyle/SpotBugs/Spotless), `generate-reports`, `publish-reports` (artifact + `$GITHUB_STEP_SUMMARY` only — no git commits of reports; #1041) |
| `pr-validation.yml` | Pull-request gates (conventional title, linked issue, diff size) |
| `playwright-e2e.yml` | Frontend Playwright E2E suite |
| ~~`e2e-swing.yml`~~ | Retired in #1083; leftovers closed in #811 (hygiene + docs). Swing client absent. |
| `frontend-ci.yml` | Lint, build, and test for the `frontend/` module (Next.js) |
| `performance-test.yml` | Load and performance tests on demand |
| `cd.yml` | `build-and-publish`, `release`, `update-description`, `generate-report`, `publish-report` — build and publish Docker images after a successful `ci.yml` on `main`; report jobs use artifacts and the step summary (no commits to `docs/wiki/cicd-reports/`; #1041) |
| `test-coverage-report.yml` | JaCoCo coverage comment on the pull request |
| `deploy-github-page.yml` | Publish static documentation and reports (includes an optional markdown mirror of CI/CD and E2E under `/cicd-reports/`; `contents: read`) |
| `claude.yml` / `opencode.yml` / `copilot-setup-steps.yml` | AI agent integration with the repository |

### Why GitHub Actions (not Jenkins, GitLab CI, or CircleCI)

| Criterion | GitHub Actions | Jenkins | GitLab CI |
|----------|----------------|---------|-----------|
| Hosting | Native on GitHub (where the code lives) | Needs its own server | Needs GitLab (not where the code lives) |
| Initial cost | Included in the GitHub plan | Infrastructure to maintain | Requires moving platforms |
| Learning curve | Declarative YAML, a large catalog of reusable actions | Groovy/Jenkinsfile, its own plugins | Its own YAML, similar complexity |
| Pull request and issue integration | Native (checks, automatic comments) | Needs plugins | Native only if the repo moves to GitLab |

**Decision:** GitHub Actions, because it is the lowest-friction option: the repository already lives on GitHub, and the workflow (`.claude/rules/ai-agent-workflow.md`) depends on GitHub issues and pull requests.

## Consequences

### Positive

- Every pull request automatically gets build, test, coverage, and security results before it can merge.
- `workspace/sdlc/preflight.sh` runs the same gates locally that CI runs, so failures are not a surprise (see `docs/300-development/CI-PREFLIGHT.md`).
- Docker image publication (`cd.yml`) happens only after a successful `ci.yml` on `main`, so broken builds are not published.

### Negative

- Spotless runs only in the CI "Code Lint" job. It is not bound to the Maven lifecycle, so a developer can have a green local `mvn verify` and still fail CI on formatting (see issue #705). Mitigated by `workspace/sdlc/preflight.sh --fix`.
- Several YAML workflows raise the maintenance cost: any new gate must be added in `.github/workflows/` and in `workspace/sdlc/preflight.sh` in the same pull request (an explicit rule in `CLAUDE.md`).

### Report publish channel (issue #1041)

Generated CI/CD/E2E markdown must **not** be committed into the application
repository. Discoverability channels are workflow artifacts,
`$GITHUB_STEP_SUMMARY`, and GitHub Pages (`deploy-github-page.yml`).
`docs/wiki/cicd-reports/` is gitignored and untracked. Report-publish jobs use
`contents: read`; the CD `release` job may retain `contents: write` for GitHub
Releases only.

## Related ADRs

- ADR-006: Testing Strategy
- ADR-007: Database Schema Versioning (Flyway)
- ADR-009: Logging & Monitoring
