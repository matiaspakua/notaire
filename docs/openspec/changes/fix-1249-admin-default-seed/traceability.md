# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1249 | open → in progress |
| Use Case | CU78 – Autenticación; CU84 – Gestión de credenciales | exists |
| Specification | `docs/openspec/changes/fix-1249-admin-default-seed/` | Gate 1 draft |
| Branch | `fix/1249_admin_default_credentials_seed` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | local, not pushed |
| Pull Request | — | pending (Owner approval) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Default password in staging | `DataInitializerTest` | passing |
| Blank password in production | `DataInitializerTest` | passing |
| Custom password in staging | `DataInitializerTest` | passing |
| Default password in test | `DataInitializerTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `ADR-019-secrets-management.md` | yes | branch commit |
| `.env.example` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1249-admin-default-seed` |
| 2 | Failing tests written, test cases designed | yes | 4 new `DataInitializerTest` cases observed failing before the change |
| 3 | Suite green, coverage held, docs updated | partial | backend `mvn test` green locally; `run_pipeline.sh` not run (no Docker on the box) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`scripts/run_pipeline.sh` needs Docker, unavailable on the agent box; to be run before the PR.
