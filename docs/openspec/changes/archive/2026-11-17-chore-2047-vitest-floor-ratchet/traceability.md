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
| Issue | #2047 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-2047-vitest-floor-ratchet/` | drafted |
| Branch | `cursor/2047-vitest-floor-ratchet-47dc` | created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | `6b143a5f` | recorded |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Guard rejects a statement floor below the ratchet | `frontend/src/tests/unit/vitest-coverage-thresholds.test.ts` | passing |
| Permanent docs quote the enforced floors | `frontend/src/tests/unit/vitest-coverage-thresholds.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CONSTITUTION.md` | yes | `6b143a5f` |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | `6b143a5f` |
| `docs/300-development/303-testing/README.md` | yes | `6b143a5f` |
| `.claude/rules/code-quality.md` | yes | `6b143a5f` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` and `validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | yes | guard test: 2 failed, then 3 passed |
| 3 | Suite green, coverage held, docs updated | no | pending |
| 4 | CI green, review approved, no conflicts | no | pending |
| 5 | Deployed, smoke test passed, Issue closed | no | Owner merges; #2047 stays open |

## Exceptions

No exception. GitHub issue writes from this environment return 403, so the in-progress label is not applied. The pull request body is the status record. #2047 stays open because the Owner target is still above the enforced floors; the pull request uses `Refs #2047`.
