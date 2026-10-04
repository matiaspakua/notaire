# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #956 | open → in progress |
| Use Case | CU84 – Login al sistema | exists |
| Related | #1224 (requirement issue), #902 (requirements tracker) | referenced |
| Specification | `openspec/changes/docs-956-cu84-template/` | Gate 1 draft |
| Branch | `docs/956_cu84_template` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Every requirement row has a valid unique GitHub ID | `scripts/test_business_docs_traceability.py` | pending |
| Login appears exactly once in the requirements | `scripts/test_business_docs_traceability.py` | pending |
| Every Use Case file has Referencias Cruzadas and GitHub ID rows | `scripts/test_business_docs_traceability.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU84 - Login.md` | pending | — |
| `docs/100-business/101-requirements/requerimientos.csv` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
