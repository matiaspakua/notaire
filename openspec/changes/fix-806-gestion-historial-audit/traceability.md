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
| Issue | #806 | open (in-progress label ACL denied for bot) |
| Use Case | CU13, CU02, CU53 | exists |
| Related | #833 (happy-path bitácora — shipped) | referenced |
| Specification | `openspec/changes/fix-806-gestion-historial-audit/` | Gate 1 complete |
| Branch | `cursor/fix-806-gestion-historial-audit-69d3` | active |
| Tasks | `tasks.md` | planning complete; implementation pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Plain create with status writes initial History | `ManagementHistorialOrphanWriteIntegrationTest` | pending |
| Plain create without status writes no History | same | pending |
| Plain update that changes status writes History | same | pending |
| Plain update that keeps status writes no History | same | pending |
| Complete-case update that changes status writes History | same | pending |
| Complete-case update that keeps status writes no History | same | pending |
| estado-actual from History when rows exist | same | pending |
| estado-actual entity-status fallback when History empty | same | pending |
| estado-actual 404 when gestión missing | same | pending |
| estado-actual 404 when status null and History empty | same | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-806-gestion-historial-audit` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).
