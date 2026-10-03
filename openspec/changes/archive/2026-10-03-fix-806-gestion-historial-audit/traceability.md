# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #806 | open (in-progress label ACL denied for bot) |
| Use Case | CU13, CU02, CU53 | exists / CU13 updated |
| Related | #833 (happy-path bitácora — shipped) | referenced |
| Specification | `openspec/changes/fix-806-gestion-historial-audit/` | Gate 1 complete |
| Branch | `cursor/fix-806-gestion-historial-audit-69d3` | active |
| Tasks | `tasks.md` | implementation + docs done; merge pending |
| Commits | `7be4b1c2` openspec; `e69ea156` tests; `6f16ae06` fix; `a7fda4df` docs | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1194 | draft |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh 1194`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Alta de gestión registra su estado inicial | #833 / complete-case create paths | covered (existing) |
| Transición válida registra el nuevo estado | ManagementTransition* / #833 | covered (existing) |
| Archivado registra el estado archivado | ManagementArchive* / #833 | covered (existing) |
| Plain create with status writes initial History | `ManagementHistorialOrphanWriteIntegrationTest#shouldWriteHistoryOnPlainCreateWithStatus` | passing |
| Plain create without status writes no History | `...#shouldNotWriteHistoryOnPlainCreateWithoutStatus` | passing |
| Plain update that changes status writes History | `...#shouldWriteHistoryOnPlainUpdateStatusChange` | passing |
| Plain update that keeps status writes no History | `...#shouldNotWriteHistoryOnPlainUpdateSameStatus` | passing |
| Complete-case update that changes status writes History | `...#shouldWriteHistoryOnCompleteCaseUpdateStatusChange` | passing |
| Complete-case update that keeps status writes no History | `...#shouldNotWriteHistoryOnCompleteCaseUpdateSameStatus` | passing |
| estado-actual from History when rows exist | `...#shouldReturnEstadoActualFromHistoryWhenRowsExist` | passing |
| estado-actual entity-status fallback when History empty | `...#shouldReturnEstadoActualFallbackWhenHistoryEmpty` | passing |
| estado-actual 404 when gestión missing | `...#shouldReturn404EstadoActualWhenManagementMissing` | passing |
| estado-actual 404 when status null and History empty | `...#shouldReturn404EstadoActualWhenStatusNullAndHistoryEmpty` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md` | yes | `a7fda4df` |
| `CHANGELOG.md` | yes | `a7fda4df` |
| `backend-api/api-test/COVERAGE.md` | yes | `a7fda4df` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-806-gestion-historial-audit` |
| 2 | Failing tests written, test cases designed | yes | TDD red: 5/10 failures before fix; then green |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` — 1712 tests, 0 failures; instr ~84.7% / branch ~73.5% |
| 4 | CI green, review approved, no conflicts | pending | draft PR #1194 |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).
