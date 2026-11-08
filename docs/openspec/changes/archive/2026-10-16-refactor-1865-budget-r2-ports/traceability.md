# Traceability

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1865 | open (child of #1693) |
| Use Case | CU39 / CU60 / CU71 (epic #1693 / ADR-030) | exists |
| Specification | `docs/openspec/changes/archive/2026-10-16-refactor-1865-budget-r2-ports/` | archived |
| Branch | `cursor/1865-budget-r2-a603` | created |
| Tasks | `tasks.md` | groups 1–8 complete |
| Commits | pending | pending |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Application budget R2 edges are gone | HexagonArchitectureTest R2 shrink | passing (R2 124→104) |
| Inbound adapters do not import outbound budget helpers | HexagonArchitectureTest R5 | passing |
| Budget REST behaviour unchanged | BudgetServiceTest / BudgetCatalogItemsServiceTest / BudgetTemplateServiceTest / BudgetStatusVocabularyTest / ItemServiceTest | passing |

## Permanent documentation updated

| Document | Updated |
|----------|---------|
| ADR-030 | yes — budget R2 slice (#1865) |
| CHANGELOG.md | n/a (#1766) |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #1865, this folder |
| 2 | yes | focused unit + HexagonArchitectureTest |
| 3 | yes | ADR-030 slice; OpenSpec archived |

## Exceptions

None.
