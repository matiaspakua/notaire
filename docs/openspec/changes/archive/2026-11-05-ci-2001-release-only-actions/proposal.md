# Release-only GitHub Actions

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2001 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/ci-2001-release-only-d385` |
| Gate 1 status | passed |

## Objetivo

GitHub Actions minutes, artifact storage, and GHCR images are spent on every pull request and every merge to `main`, even though agents already run the same gates locally. Product CI and package publish move to a version release; day-to-day proof is `bash workspace/sdlc/preflight.sh --full`.

## What Changes

- **BREAKING** for the merge gate: workflows `ci.yml`, `frontend-ci.yml`, `playwright-e2e.yml`, `pr-validation.yml`, `pages-ci.yml`, `openapi-contract.yml`, `database-vv.yml`, `sdlc-process.yml`, and `test-coverage-report.yml` no longer run on `pull_request` or on push to a branch. Playwright drops its weekday cron. Process checks drop the daily main-health cron. They run on tags `v*` / `notaire-v*` and `workflow_dispatch`.
- CodeQL drops `pull_request` and push-to-main. The weekly Monday schedule stays, plus the same tags and `workflow_dispatch`.
- `cd.yml` drops `workflow_run`, so a merge to `main` no longer publishes images. Tag push and `workflow_dispatch` still publish. `:latest` moves on a version-tag publish after the immutable SHA tag.
- Release Please keeps running on push to `main`. A tag created with `GITHUB_TOKEN` does not start `push.tags` workflows (#1599), so the existing dispatch step starts `cd.yml` and the heavy release workflows on that tag.
- `deploy-github-page.yml` deploys on push to `main` and `workflow_dispatch`. It no longer waits for CI-on-main.
- The mandatory agent gate for a pull request is `bash workspace/sdlc/preflight.sh --full`. A run without `--full` is not sufficient to open a PR. There is no GitHub re-run of that suite on the pull request.
- The in-repo `protect-main` desired ruleset drops required status checks that those workflows will no longer report. Applying it stays an Owner action (#1040) after merge.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A pull request is ready when `bash workspace/sdlc/preflight.sh --full` has passed locally. GitHub does not re-run that suite on the pull request. | CU76; CONSTITUTION §5 step 19 (this change) | Changed |
| Product packages (GHCR images, SBOMs, Release assets) are published on a version tag or a manual CD dispatch, not on every merge to `main`. | CU76; CONSTITUTION §11 | Changed |
| The public documentation site updates on every push to `main` without a product release. | CU76; GitHub Pages | Changed |
| CodeQL still runs weekly between releases. Local preflight does not run CodeQL. | CU76 | Made explicit |
| Only the Owner merges this Constitution change, and only the Owner updates the live `protect-main` ruleset. | CONSTITUTION §5 step 21; #1040 | Made explicit |

## Capabilities

### New Capabilities

- `release-only-github-actions`: when product CI, contract, database V&V, process checks, coverage reports, and CodeQL run, and that a pull request is gated locally with `preflight.sh --full`.

### Modified Capabilities

- `cd-pin-tested-sha`: CD no longer publishes from a CI `workflow_run` on `main`. It publishes the tag commit (`github.sha`) and moves `:latest` only after the SHA tag exists.
- `github-pages-site-shell`: the Pages app is checked by local `preflight.sh` (and by `pages-ci.yml` on a version tag), not by a pull-request workflow. The site still deploys on push to `main`.
- `protect-main-ruleset`: the desired ruleset no longer requires the five status checks that pull requests will stop reporting. PR-only merges, no force-push, and no deletion stay.
- `release-asset-attachment`: the release-please dispatch still starts CD on the tag, and also starts the heavy release workflows, because a `GITHUB_TOKEN` tag push does not start `on.push.tags`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No API or schema change |
| `frontend` | no | No UI change |
| `frontend-swing` | no | Retired module, untouched |
| `infra` / observability | no | Scheduled ZAP, k6, and backup workflows stay |
| CI/CD (`.github/workflows`) | yes | Trigger policy, CD publish conditions, Pages deploy, release-please dispatch |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- Not BREAKING for API clients. BREAKING for anyone who treats GitHub pull-request checks as the merge gate.

### Architecture review

This follows the existing CI layout (same jobs, same aggregators, same local scripts). No ADR: the change is when workflows run, not a new architectural style. Hexagonal boundaries and Flyway are untouched.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | Step 19, Foreman merge rule, §§9–13: local `--full` is the PR gate; CD publishes on release; Pages deploy on push to `main` |
| `CONSTITUTION-AGENT-CARD.md` | Preflight `--full` and no per-PR GitHub suite |
| `AGENTS.md` | Same mandatory command |
| `.claude/rules/ai-agent-workflow.md` | Gate 4 readiness |
| `.claude/skills/steward/SKILL.md` | Do not wait for checks that no longer run |
| `docs/300-development/CI-PREFLIGHT.md` | `--full` is mandatory; default is not enough |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | Local gate replaces per-PR heavy CI |
| `docs/300-development/RELEASE.md` | Tag publish, `GITHUB_TOKEN` dispatch, ruleset follow-up |
| `docs/300-development/README.md` | Pre-push command |
| `docs/200-architecture/208-devsecops/README.md` | Ruleset desired state |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | When the pipeline runs and how CD publishes |
| `github-page/README.md` | Pages checks are local / release, deploy is push to `main` |
| `CHANGELOG.md` | `[Unreleased]` entry |
| `security/rulesets/protect-main.desired.json` | Drop required status checks that PRs will not report |

## Out of Scope

- Merging the pull request (only Matias merges).
- Applying the live GitHub ruleset (Owner, issue #1040, after merge).
- Issue #1994 (E2E mobile lists).
- Changing cleanup, ZAP, performance, or backup schedules.
- Changing `copilot-setup-steps.yml` (it runs only when that file changes) or `opencode.yml` (comment-triggered).
