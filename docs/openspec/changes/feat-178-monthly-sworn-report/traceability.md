# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #178 | OPEN (board/label writes 403; existing CU25 issue) |
| Use Case | CU25 — Generar Declaración Jurada del mes | exists |
| Specification | `docs/openspec/changes/feat-178-monthly-sworn-report/` | Gate 1 draft |
| Branch | `cursor/178-monthly-sworn-statement-5fcb` | from `origin/main` `d073d2ad` (after #1598 `11efc425` + 0.2.1) |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Month filter and deed row content | `MonthlySwornStatementReportBuilderTest` | written |
| Titular/substitute from substitutions | `MonthlySwornStatementReportBuilderTest` | written |
| Tax/annex stay N/A | `MonthlySwornStatementReportBuilderTest` | written |
| Empty period is 200 + empty-state PDF | builder + integration | written |
| Seeded month appears in extracted PDF text | integration | written |
| Year+month HTTP contract | Bruno `02-monthly-sworn-statement.yml` | kept year+month 200 |
| UI download with seeded period | Playwright CU25-GW01 | unskipped |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU25 – Generar Declaración Jurada del mes.md` | yes | this branch |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | yes | this branch |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | yes | this branch |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | this branch |
| `docs/300-development/303-testing/INVENTORY.md` | yes | this branch |
| `CHANGELOG.md` | yes | this branch |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-178-monthly-sworn-report` |
| 2 | Failing tests written, test cases designed | yes | builder compile-red then implement |
| 3 | Suite green, coverage held, docs updated | yes | targeted Maven + Vitest + reliability |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Issue #178 is OPEN with CU25 in the body. Adding `in-progress` / Delivery Board
**In Progress** returns HTTP 403 from this integration. Work proceeds on the
existing issue. No new issue was invented.

Slice 3 does not invent Owner tax rates, legal footer, or signature language.
Missing tax/annex/substitute values are the shared `N/A` fallback.
