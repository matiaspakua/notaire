# Stop CI bots committing reports to main

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1041 |
| Use Case | **CU76** – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-1041-no-bot-commits-main-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement **after #1042 merges** (queue: #1051 → #1046 → #1042 → #1041) |

## Objetivo

CI Bot “docs: add … report” commits make up roughly a quarter of `main`
history (~464 of ~1641 commits as of Gate 1 prep). Every successful merge
triggers report pushes from `ci.yml`, `cd.yml`, and `playwright-e2e.yml` into
`docs/wiki/cicd-reports/` (166 tracked files), using
`git pull --rebase; git push … || echo "Push skipped"` with `contents: write`.
Those races silently drop reports, force write tokens on `main`, and
`[skip ci]` bot SHAs confuse PR check status. Stop committing reports into
git; publish them as workflow artifacts, job summaries, and/or GitHub Pages
instead.

## What Changes

- Remove report **commit / push** steps from
  `.github/workflows/ci.yml` (`publish-reports`),
  `.github/workflows/cd.yml` (`publish-report`), and
  `.github/workflows/playwright-e2e.yml` (`coverage-report`).
- Keep (or add) `actions/upload-artifact` + `$GITHUB_STEP_SUMMARY` for the
  same markdown; optionally surface latest reports via the existing
  `deploy-github-page.yml` Pages path (download artifacts — no git commit).
- Confirm `pr-validation.yml` stays commit-free for wiki/CI reports (already
  noted under #1117 / #1041).
- Drop job-level `contents: write` where it existed only for report pushes
  (retain CD `release` job write for GitHub Releases).
- Stop tracking `docs/wiki/cicd-reports/` (`git rm` from index), add it to
  `.gitignore`, and update DevSecOps / ADR / invariant tests that assume
  wiki commits.
- Add a static workflow unittest proving no CI Bot report commits remain.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| CI/CD/E2E MUST NOT commit generated reports onto `main` (or PR heads) | CU76; #1041 AC; #1117 | New (enforce) / Made explicit for PR path |
| Reports MUST remain discoverable as workflow artifacts and/or job summary and/or GitHub Pages | #1041 AC | Changed (channel, not git history) |
| `docs/wiki/cicd-reports/` MUST be untracked and git-ignored | #1041 AC | New |
| Workflow jobs that only published wiki commits MUST NOT hold `contents: write` | #1041 AC; least privilege | Changed |
| CD GitHub Release creation MAY keep `contents: write` | existing `cd.yml` `release` job | Made explicit (out of report-publish scope) |

## Capabilities

### New Capabilities

- `ci-no-bot-report-commits`: CI/CD/E2E/PR workflows never git-commit
  `docs/wiki/cicd-reports/`; reports ship as artifacts / summaries / Pages;
  directory ignored and untracked; report jobs drop unused `contents: write`.

### Modified Capabilities

- (none under `openspec/specs/` today cover wiki report commit behavior;
  related #778 Pages deploy remains the pattern for non-git publish)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `ci.yml`, `cd.yml`, `playwright-e2e.yml`; assert `pr-validation.yml`; optional Pages wiring |
| Scripts / tests | yes | New/extended static unittest; update `test_ci_workflow_invariants.py` docstring/asserts that assume wiki commits |
| Docs / wiki tree | yes | Untrack `docs/wiki/cicd-reports/`; `.gitignore`; DevSecOps + CHANGELOG |
| `github-page/` | optional | Only if implement chooses Pages surfacing of markdown reports |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (Actions versions retained unless a documented bump)

### Architecture review

No application-architecture change. Strengthens git history hygiene and
`main` ruleset least-privilege for CU76. Aligns report publish with the
existing Pages model (`deploy-github-page.yml` already uses
`contents: read` + `pages: write`). Complements #1042 (CD pin-to-tested-SHA)
by removing a major source of tip-of-`main` drift after CI.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/208-devsecops/README.md` | Replace “publish to `docs/wiki/cicd-reports/` on main” with artifact / summary / Pages |
| `docs/200-architecture/202-ADR/ADR-012-ci-cd-pipeline.md` | Note report jobs no longer commit; Channels = artifacts/Pages |
| `docs/300-development/DEPLOYMENT-PLAN.md` | Drop / rewrite checklist item that CD report lands in git wiki path |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Note non-git report publish for #1041 if CU text still implies wiki commits |
| `CHANGELOG.md` | `[Unreleased]` devops/ci entry for #1041 |

## Out of Scope

- Rewriting historical bot commits out of git history (filter-repo / force-push).
- Changing CD Docker build/publish, Trivy, or cosign (owned by #1042 / other issues).
- Deleting `deprecated-frontend-swing` or Dependabot overrides (#1046).
- HttpOnly JWT / CSP product work (#1051).
- Expanding `publish_wiki_reports.py` into a new wiki-git publisher (do not
  reintroduce commits via that script).
- Starting implement / PR before **#1042** merges.
