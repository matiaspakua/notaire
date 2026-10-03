# Add the GitHub Security Lab baseline to the pipeline

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1135 |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/gh-secure-codeql-pipeline-2d5b` |
| Gate 1 status | passed |

## Objetivo

The repository has Trivy and SpotBugs, and no CodeQL analysis. Private
vulnerability reporting is already on. Dependabot version updates cover Maven
and GitHub Actions, not the frontend npm lockfile. Repository administration
(secret-scanning push protection, Dependabot security updates, branch
protection, CodeQL default setup) cannot be changed with the cloud integration
token (HTTP 403). This change puts code scanning in the pipeline and gives an
admin a repeatable way to enable the remaining GitHub Security Lab settings.

## What Changes

- Add `.github/workflows/codeql.yml` for Java (manual Maven compile), JavaScript/TypeScript, and GitHub Actions.
- Add `scripts/enable-gh-secure.sh`, which installs `GitHubSecurityLab/gh-secure` and can enable vulnerability reporting, secret-scanning push protection, and Dependabot. Branch protection stays opt-in. The script does not enable CodeQL default setup.
- Add the npm ecosystem under `.github/dependabot.yml` for `frontend/`.
- Add `SECURITY.md` pointing at private vulnerability reporting.
- Record the GitHub-hosted CodeQL gate in `scripts/preflight.sh`, `docs/300-development/CI-PREFLIGHT.md`, and `docs/200-architecture/208-devsecops/README.md`.

No application code. Branch protection is not enabled.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Continuous vulnerability scanning of source code is part of the security baseline. | CU78 — Alcance Técnico: "Escaneo continuo de vulnerabilidades en dependencias y código fuente." | Made explicit (CodeQL in CI) |

## Capabilities

### New Capabilities

None. `skip_specs: true` — the change adds a CI workflow and repository security tooling, not a product behavior delta.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Analyzed by CodeQL; source unchanged |
| `frontend` | no | Analyzed by CodeQL; Dependabot watches `frontend/` |
| `notaire-shared` | no | Compiled as part of the Maven reactor during the Java CodeQL build |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | New `codeql.yml` |
| `docs/200-architecture/208-devsecops/` | yes | Pipeline description |
| `docs/300-development/CI-PREFLIGHT.md` | yes | CodeQL is GitHub-hosted only |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none added to the application; CI uses `github/codeql-action` v4

### Architecture review

No application-architecture change. No ADR. Code scanning uploads SARIF to GitHub code scanning and does not change runtime behavior.

## Documentation Impact

| Document | Change |
|----------|--------|
| `docs/200-architecture/208-devsecops/README.md` | Document `codeql.yml`, `SECURITY.md`, and `scripts/enable-gh-secure.sh` |
| `docs/300-development/CI-PREFLIGHT.md` | List the CodeQL workflow and that preflight does not run it |
| `CHANGELOG.md` | Unreleased note for the pipeline baseline |
| `SECURITY.md` | New private-reporting policy |
