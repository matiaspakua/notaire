# Traceability — fix-budget-person-pg-json

## Chain

| Layer | Reference |
|-------|-----------|
| Issue | #1130 |
| Use Case | CU01 |
| Spec | `specs/budget-person-response/spec.md` |
| Design | `design.md` |
| Tasks | `tasks.md` |
| Branch | `cursor/fix-budget-person-pg-json-69d3` |
| Commits | `61b223a1` restore nested person; `8934b884` PG assert docs; `aa1fadd6` OpenSpec |
| PR | https://github.com/matiaspakua/notaire/pull/1132 |
| Release | main restore after merge |

## Requirement coverage

| Acceptance | Evidence |
|------------|----------|
| Nested `$.person.personId` when linked | PG IT create/get/update/list |
| `$.person` absent when omitted | PG IT `shouldCreateWithoutPersonWhenOmitted` |
| Request still accepts nested person | Bruno `budgets/01-create.yml` |

## Permanent documentation updated

- Frontend `Presupuesto.person?: DtoPerson` already correct — no change needed.
- OpenSpec delta records the restore.

## Gate log

| Gate | Status |
|------|--------|
| Gate 1 Specification | passed (this folder) |
| Gate 2 Tests | PG IT red→green target |
| Gate 3 Docs | OpenSpec |
| Gate 4 CI | pending PR |
| Gate 5 Smoke | after merge to main |

## Exceptions

None.
