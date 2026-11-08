# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #203 | OPEN (board/label writes 403; existing CU50 issue) |
| Use Case | CU50 — Generar Declaración Jurada de Rentas | exists |
| Specification | `docs/openspec/changes/feat-203-tax-sworn-report/` | Gate 1 validated |
| Branch | `cursor/203-tax-sworn-statement-5fcb` | from `origin/main` `6d792eb3` |
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
| Month filter and deed row content | `TaxSwornStatementReportBuilderTest` | yes |
| Tax/operation/payment stay N/A | `TaxSwornStatementReportBuilderTest` | yes |
| Empty period is 200 + empty-state PDF | builder + integration | yes |
| Seeded month appears in extracted PDF text | integration | yes |
| Year+month HTTP contract | Bruno tax sworn | yes (year+month kept) |
| UI download with seeded period | Playwright CU50-GW01 | yes |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU50 – Generar Declaración Jurada de Rentas.md` | yes | this PR |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | yes | this PR |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | yes | this PR |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | this PR |
| `docs/300-development/303-testing/INVENTORY.md` | yes | this PR |
| `CHANGELOG.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-203-tax-sworn-report` + `openspec validate --strict` |
| 2 | Failing tests written, test cases designed | yes | compile-red `TaxSwornStatementReportBuilder` then implement |
| 3 | Suite green, coverage held, docs updated | yes | targeted Maven + Vitest |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Issue #203 is OPEN with CU50 in the body. Adding `in-progress` / Delivery Board
**In Progress** returns HTTP 403 from this integration. Work proceeds on the
existing issue. No new issue was invented.

Slice 4 does not invent Owner tax rates, Rentas classifications, legal footer,
or signature language. Missing values are the shared `N/A` fallback.
