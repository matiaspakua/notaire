# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #901 | open → implement in progress |
| Use Case | CU77 – Operations Monitoring and Incident Management | exists (add #901 to GitHub ID table at implement) |
| Related | #1044 CLOSED (prod compose); #1043 CLOSED (frontend GHCR); #254 TLS; #256 backups; #306 SLO; #288 runbooks | referenced |
| Specification | `openspec/changes/feat-901-staging-deploy-manifests/` | Gate 1 validated |
| Branch | `cursor/feat-901-staging-deploy-manifests-69d3` | created from `origin/main` |
| Tasks | `tasks.md` | implement complete (local); Gate 4–5 pending |
| Commits | `c89957e7`, `9b926831`, `711b5c8d` | pushed |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1176 | draft |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Base manifests exist for the four app services | `scripts/test_staging_kustomize.py` | covered |
| Staging overlay stays within the same service set | same | covered |
| Postgres is not publicly published | same | covered |
| No committed credential values | same + PR review | covered |
| Backend ENVIRONMENT is production (or documented equivalent) | same | covered |
| Flyway baseline-on-migrate disabled in manifests | same | covered |
| Static validator fails when manifests are incomplete | same (red-then-green) | covered |
| Static validator passes on the shipped manifests | same | covered |
| Deployment guide documents manifests and compose relationship | same + docs | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/209-deployment/README.md` | yes | (this change) |
| `docs/300-development/DEPLOYMENT-PLAN.md` | yes | (this change) |
| `docs/100-business/102-use-cases/CU77 – Operations Monitoring and Incident Management.md` | yes | (this change) |
| SAD §11.1 / §11.3 pointer | yes | (this change) |
| `CHANGELOG.md` | yes | (this change) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh feat-901-staging-deploy-manifests` |
| 2 | Failing tests written, test cases designed | yes | `scripts/test_staging_kustomize.py` failed before manifests; green after |
| 3 | Suite green, coverage held, docs updated | yes (local) | `python3 scripts/test_staging_kustomize.py`; `python3 scripts/test_prod_compose.py` |
| 4 | CI green, review approved, no conflicts | pending | heavy CI via coordinator |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Queue ahead cleared (#1067 merged). #1043 CLOSED — staging overlay uses
GHCR SHA tags for backend and frontend.
