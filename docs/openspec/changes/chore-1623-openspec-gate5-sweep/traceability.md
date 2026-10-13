# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1623 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-1623-openspec-gate5-sweep/` | drafted |
| Branch | `chore/1623_archive-shipped-openspec` | created |
| Tasks | `tasks.md` | in progress |
| Commits |  | pending |
| Pull Request |  | pending |
| CI run |  | pending |
| Merge commit |  | pending |
| Release / tag |  | pending |
| Smoke test |  | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No CLOSED-issue change stays active | `check-openspec-archive-hygiene.py --inventory-only` | passing |
| Every active change conforms | `validate-sdlc-plan.sh` (30 changes) | passing |
| Main specs valid | `openspec validate --specs --strict` (168/168) | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> |  |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1623; `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | yes | inventory 32 leftovers; plan validation 32 ✗ (before) |
| 3 | Suite green, coverage held, docs updated | yes | all three validators green; 168/168 specs |
| 4 | CI green, review approved, no conflicts | | pending |
| 5 | Deployed, smoke test passed, Issue closed | | pending (issue stays open for later slices) |

## Exceptions

<!-- Constitution §12 allows exceptions only in extreme circumstances and with
     explicit human approval. Record any exception here: what was skipped, who
     approved it, and why. An empty section means no exception was taken. -->

None.
