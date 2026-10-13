# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #177 | OPEN (board/label writes 403; existing CU24 issue; Owner Continue) |
| Use Case | CU24 — Generar libro de índices | exists |
| Specification | `docs/openspec/changes/feat-177-index-book-report/` | Gate 1 draft |
| Branch | `cursor/177-index-book-report-5fcb` | created from `origin/main` `17e9f15f` |
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
| Sorted person rows with deed, first folio, day/month | `IndexBookReportBuilderTest` | passing |
| Budget-client fallback when personList is empty | `IndexBookReportBuilderTest` | passing |
| Optional notary / protocol filters | `IndexBookReportBuilderTest` | passing |
| Empty period is 200 + empty-state PDF | `IndexBookReportBuilderTest`, `IndexBookReportIntegrationTest` | passing |
| Seeded period appears in extracted PDF text | `IndexBookReportIntegrationTest` | passing |
| Year-only HTTP contract | Bruno `01-index-book.yml` | pending CI |
| UI download with seeded period | Playwright CU24-GW01 | pending CI |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU24 – Generar libro de índices.md` | pending | — |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | pending | — |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | pending | — |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash workspace/sdlc/validate-sdlc-plan.sh feat-177-index-book-report` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Issue #177 is OPEN with CU24 in the body. Adding `in-progress` / Delivery Board
**In Progress** returns HTTP 403 from this integration. The Owner authorized
Continue on the existing issue. No new issue was invented.

Slice 2 does not invent Owner legal footer or signature language. Empty period
is an explicit English empty-state line on the shared design system, not a
legal form.
