# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1095 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/triage-gate-proofs-1095/` | written |
| Branch | `chore/1095_triage_gate_proofs` | created |
| Tasks | `tasks.md` | groups 1-2 done |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Triage restores values the harness derived (2 scenarios) | `local-ai/sdlc/tests/test_triage_check.py` | pending |
| Triage proofs must be able to fail (2 scenarios) | `local-ai/sdlc/tests/test_triage_check.py` | pending |
| Promised tests need a tests phase (2 scenarios) | `local-ai/sdlc/tests/test_triage_check.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1095, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI harness.
  The tests are Python `unittest` suites. The `gate_triage` wiring is shell glue
  checked by `bash -n` and a `RECHECK=1` replay of the stored #1064 retry files.
