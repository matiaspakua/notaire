# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1264 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1264-release-please-token/` | Gate 1 draft |
| Branch | `fix/1264_release_please_token` | created from updated `main` |
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
| Workflow uses the default token | `scripts/test_semver_release_process.py` | passing |
| History is bootstrapped | `scripts/test_semver_release_process.py` | passing |
| Runbook documents the setting and manual checks | `scripts/test_semver_release_process.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `docs/300-development/RELEASE.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1264-release-please-token` |
| 2 | Failing tests written, test cases designed | yes | guard tests updated first and observed failing against the previous secret wiring |
| 3 | Suite green, coverage held, docs updated | partial | semver guard, CI workflow invariants, concurrency, changelog and links guards green; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`scripts/run_pipeline.sh` needs Docker, unavailable on the agent box. Acceptance (first release cut) happens after merge.
