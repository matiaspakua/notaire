# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2413 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-2413-affected-preflight/` | <!-- --> |
| Branch | `chore/2413_affected_preflight` | <!-- --> |
| Tasks | `tasks.md` | <!-- n/N complete --> |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A factory-only branch skips the product suites | `workspace/tests/test_preflight_affected.py` | pending |
| A backend branch runs the backend and the server-backed suites | `workspace/tests/test_preflight_affected.py` | pending |
| A docs branch runs Pages | `workspace/tests/test_preflight_affected.py` | pending |
| Changing the gate runs everything | `workspace/tests/test_preflight_affected.py` | pending |
| No comparison base runs everything | `workspace/tests/test_preflight_affected.py` | pending |
| Same commit pushed again | `workspace/tests/test_preflight_hook_cache.py` | pending |
| A new commit runs the gate | `workspace/tests/test_preflight_hook_cache.py` | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> | <!-- SHA --> |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | <!-- --> | <!-- --> |
| 2 | Failing tests written, test cases designed | <!-- --> | <!-- --> |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

None. CONSTITUTION.md and the hook are Owner-protected paths; the Owner requested this change and merges it.
