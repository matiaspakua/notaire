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
| Issue | #853 | open (implement in progress) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | CU02 / CU14 / CU19 surfaces exercised via entity helpers | referenced |
| Specification | `openspec/changes/test-853-gestion-escritura-unit/` | Gate 1 validated |
| Branch | `cursor/test-853-gestion-escritura-unit-69d3` | active |
| Tasks | `tasks.md` | Gate 2–3 in progress |
| Commits | `ba4f8a2f`, `a0437995`, `8c29fe7b`, `5160b757` | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1211 | draft |
| CI run | pending on head `5160b757` | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| getDto null status no NPE | `getDtoWithNullStatusDoesNotThrow` | covered |
| getDtoNotary null notary | `getDtoNotaryWithNullNotaryReturnsNull` | covered |
| getDtoNotary null identification type | `getDtoNotaryWithNullIdentificationTypeDoesNotThrow` | covered |
| setAtributos null status | `setAtributosWithNullStatusLeavesPriorStatus` | covered |
| getDto happy path | `getDtoWithStatusAndNotaryMapsBoth` | covered |
| Constructor empty lists | `defaultConstructorInitializesEmptyLists`, `idConstructorInitializesEmptyLists` | covered |
| Person getDto null identification type | `getDtoWithNullIdentificationTypeDoesNotThrow` | covered |
| Person getDto null DeedManagementList | `getDtoWithNullDeedManagementListDoesNotThrow` | covered |
| Full DeedManagementEntityTest green | `mvn -Dtest=DeedManagementEntityTest` (22 methods) | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh test-853-gestion-escritura-unit` |
| 2 | Failing tests written, test cases designed | yes | TDD red observed: 6 failures + 2 errors before null-guards |
| 3 | Suite green, coverage held, docs updated | in progress | `DeedManagementEntityTest,PersonEntityTest` 33/33 green |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Queue through `#799/#800/#805/#841` cleared; implement started from
`origin/main` tip `52bdcb4a`.
