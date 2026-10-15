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
| Issue | #119 | OPEN (board/label writes 403) |
| Use Case | CU08 — Verificar Testimonio / RF-94 Impresión de Testimonios | exists |
| Specification | `docs/openspec/changes/feat-119-testimony-copy-report/` | passed |
| Branch | `cursor/119-testimony-copy-5fcb` | created |
| Tasks | `tasks.md` | in progress |
| Commits | this branch | in progress |
| Pull Request | #1721 | draft |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Verified testimony prints recorded deed body | TestimonyCopyReportAdapterTest | passing |
| Missing deed fields stay N/A | TestimonyCopyReportAdapterTest | passing |
| Unverified testimony is 400 | TestimonyCopyReportIntegrationTest | passing |
| Unknown testimony is 404 | TestimonyCopyReportIntegrationTest | passing |
| Observed notes appear | TestimonyCopyReportAdapterTest | passing |
| Issue-copy download contains deed text | Playwright TS-0031 | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU08 – Verificar Testimonio.md` | no | pending |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | no | pending |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | no | pending |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | no | pending |
| `docs/300-development/303-testing/INVENTORY.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #119, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Issue #119 is OPEN. Adding `in-progress` / Delivery Board In Progress returns
HTTP 403 from this integration. Work proceeds on the existing RF-94 issue.
No new issue was invented. This slice does not invent Owner special-sheet
geometry, signature language, or official-form wording.
