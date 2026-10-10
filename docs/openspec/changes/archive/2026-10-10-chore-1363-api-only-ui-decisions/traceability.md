# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1363, #1364 | open → in progress |
| Use Case | CU42 – Documentos por vencer; CU44 – Reingreso testimonio; CU76 – Validación (CONSTITUTION section 4) | exists |
| Specification | `docs/openspec/changes/chore-1363-api-only-ui-decisions/` | Gate 1 draft |
| Branch | `chore/1363_api_only_ui_decisions` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No pending decision | `contracts/tests/test_api_reachability.py` | passing |
| Decided entries | `contracts/tests/test_api_reachability.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `contracts/api-reachability-allowlist.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh chore-1363-api-only-ui-decisions` |
| 2 | Failing tests written, test cases designed | yes | `test_owner_decisions_are_recorded` observed failing on the bucket E report entry |
| 3 | Suite green, coverage held, docs updated | yes | `bash contracts/verify.sh` green (13 tests) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; no UI change, so no Playwright run.
