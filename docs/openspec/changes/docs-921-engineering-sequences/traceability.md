# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #921 | open |
| Use Case | CU44 – Reingresar testimonio | exists |
| Related | CU08, CU11, CU12, CU43, CU76 | exists |
| Specification | `docs/openspec/changes/docs-921-engineering-sequences/` | Gate 1 |
| Branch | `cursor/engineering-doc-gaps-853b` | created |
| Tasks | `tasks.md` | in progress |
| Commits | this branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | Process Checks | pending |

## Requirement coverage

`skip_specs: true`.

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Verify, inscription, and both re-entries are named | `docs/tests/test_gestion_sequences.py` | passing |
| Observada has no return and re-entry does not move the management | `docs/tests/test_gestion_sequences.py` | passing |
| The case schema has an artifact id | `docs/tests/test_gestion_sequences.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/203-design/GESTION.md` | yes | this change |
| `docs/100-business/EVALUACION-DOCUMENTACION.md` | yes | this change |
| `docs/300-development/EVALUACION-INGENIERIA.md` | yes | this change |
| `docs/300-development/INGENIERIA.md` | yes | this change |
| `docs/300-development/README.md` | yes | this change |
| `docs/README.md` | yes | this change |
| `CHANGELOG.md` | yes | this change |
| `docs/tests/test_gestion_sequences.py` | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue #921 open, plan complete | yes | `validate-sdlc-plan.sh` |
| 2 | Failing test observed, then the sequences | yes | three failures, then green |
| 3 | Named documents are in the diff | yes | this ledger |
| 4 | CI green and review approved | pending | — |
| 5 | Merged; #921 still open | pending | — |

## Exceptions

None.
