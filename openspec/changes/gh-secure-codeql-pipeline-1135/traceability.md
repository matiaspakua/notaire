# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens — never pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1135 | open |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento | exists |
| Specification | `openspec/changes/gh-secure-codeql-pipeline-1135/` (`skip_specs: true`) | drafted |
| Branch | `cursor/gh-secure-codeql-pipeline-2d5b` | created |
| Tasks | `tasks.md` | in progress |
| Commits | `d47f7d98` ci(security): add CodeQL and the gh-secure baseline | in progress |
| Pull Request | #1136 | open |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — pipeline only, no release artifact | pending |
| Smoke test | first CodeQL workflow run uploads SARIF | pending |

## Requirement coverage

n/a — `skip_specs: true`. Acceptance Criteria are on Issue #1135.

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| CodeQL workflow covers Java, JavaScript/TypeScript, and Actions | Review `.github/workflows/codeql.yml` matrix | pending CI |
| Dependabot watches frontend npm | Review `.github/dependabot.yml` | drafted |
| SECURITY.md points at private reporting | Review `SECURITY.md` | drafted |
| Admin script installs gh-secure and leaves branch protection opt-in | `bash -n` and `--help` | pending |
| Preflight lists CodeQL as GitHub-hosted only | `bash scripts/preflight.sh --list` | pending |
| Branch protection is not enabled by this change | No protection API call in the diff | drafted |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/208-devsecops/README.md` | yes | `d47f7d98` |
| `docs/300-development/CI-PREFLIGHT.md` | yes | `d47f7d98` |
| `CHANGELOG.md` | yes | `d47f7d98` |
| `SECURITY.md` | yes | `d47f7d98` |

## Gate log

| Gate | Status | Evidence |
|------|--------|----------|
| Gate 1 — Specification | passed | `validate-sdlc-plan.sh` |
| Gate 2 — Tests | n/a | no application code |
| Gate 3 — Documentation | passed | files in this change |
| Gate 4 — CI and review | pending | pull request |
| Gate 5 — Smoke and close | pending | CodeQL run on the pull request |

## Exceptions

- Issue labels: `gh issue edit --add-label in-progress` returned HTTP 403 (`Resource not accessible by integration`). The issue stays unlabeled.
- Repository security settings that need administration (secret-scanning push protection, Dependabot security updates, branch protection) are not applied in this change. `scripts/enable-gh-secure.sh --apply` is the owner step.
