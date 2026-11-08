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
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | RNF-09 — Uso de colores en la GUI | exists |
| Specification | `docs/openspec/changes/feat-1961-wave8-frontend-surfaces/` | passed |
| Branch | `feat/1961_wave8-frontend-surfaces` | created |
| Tasks | `tasks.md` | in progress |
| Commits | passing | pending |
| Pull Request | passing | pending |
| CI run | passing | pending |
| Merge commit | passing | pending |
| Release / tag | passing | pending |
| Smoke test | passing | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Concepts list failure shows retry | test_copy_requires_number | passing |
| Copy create blocks empty number | test_copy_requires_number | passing |
| Doc exists under docs/300-development | test_copy_requires_number | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | no | passing |
| `docs/300-development/frontend-rest-client-errors.md` | no | passing |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1961, this folder |
| 2 | Failing tests written, test cases designed | passing | pending |
| 3 | Suite green, coverage held, docs updated | passing | pending |
| 4 | CI green, review approved, no conflicts | passing | pending |
| 5 | Deployed, smoke test passed, Issue closed | passing | pending |

## Exceptions

None.
