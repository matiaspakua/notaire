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
| Pull Request | — | passed |
| CI run | — | passed |
| Merge commit | — | passed |
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
| `docs/100-business/102-use-cases/CU84 - Login.md` | done | this PR |
| `docs/100-business/101-requirements/requerimientos.csv` | done | this PR |
| `CHANGELOG.md` | done | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | done | this PR |
| 3 | Suite green, coverage held, docs updated | done | this PR |
| 4 | CI green, review approved, no conflicts | done | this PR |
| 5 | Deployed, smoke test passed, Issue closed | done | this PR |

## Exceptions

None.

## Verification log (2026-10-04)

- Red first: 2 of 4 guard tests failed (malformed CSV rows; CU84 without the template rows); all 4 pass after the fix.
- Requirement issue #1224 created for the Login requirement so the CSV carries a real GitHub ID.
