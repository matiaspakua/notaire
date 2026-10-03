# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1108 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/seed-openspec-templates-reject-unfilled-1108/` | Gate 1 writing |
| Branch | `cursor/chore-1108-openspec-template-seed-30a2` | created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | c2dcb85c | done |
| Pull Request | #1116 | open (draft) |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Seed copies four templates when absent | `scripts/tests/test_validate_sdlc_plan.py` | passing |
| Seed leaves an existing file alone | same | passing |
| Seed fills known Issue / Use Case / Branch | same | passing |
| Leftover HTML-comment section body rejected | same | passing |
| Filled section body accepted | same | passing |
| Rejection names file and heading | same | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `openspec/NOTAIRE-ADAPTATIONS.md` | yes | pending commit |
| `docs/300-development/templates/specification-template.md` | yes | pending commit |
| `openspec/schemas/notaire-sdlc/schema.yaml` | yes | pending commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | this folder + `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | RED then GREEN on `test_validate_sdlc_plan.py` |
| 3 | Suite green, coverage held, docs updated | pending | script unit tests green; docs updated |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
