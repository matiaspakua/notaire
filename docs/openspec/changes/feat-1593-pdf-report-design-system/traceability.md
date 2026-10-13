# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1593 | OPEN (board/label writes 403; Matias authorized Continue with CU76) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-1593-pdf-report-design-system/` | Gate 1 draft |
| Branch | `feat/1593_pdf-report-design-system` | created from updated `main` |
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
| Metadata and language | `PdfBoxReportRendererTest` | passing |
| Embedded Unicode font | `PdfBoxReportRendererTest`, `ReportAssetCatalogTest` | passing |
| Wordmark logo contract | `ReportAssetCatalogTest`, `PdfBoxReportRendererTest` | passing |
| Repeating header and Página X de Y | `PdfBoxReportRendererTest` | passing |
| Geometry / safe bounds | `PdfReportStructuralRenderTest` | passing |
| Wrapping and sanitizer | `PdfBoxReportRendererTest`, `ReportTextSanitizerTest` | passing |
| Deterministic clock/zone | `PdfBoxReportRendererTest`, `ReportLocaleFormatsTest` | passing |
| Empty state | `PdfBoxReportRendererTest` | passing |
| Response headers | `PdfResponseFactoryTest`, `ReportControllerTest` | passing |
| SAD <3s | `PdfBoxReportRendererTest` | passing |
| Placeholder reports use shared renderer | `ReportServicePaymentReceiptTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | yes | branch |
| `docs/200-architecture/201-SAD/sad.md` | yes | branch |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | yes | branch |
| `CHANGELOG.md` | yes | branch |
| CU76 | yes | branch |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1593-pdf-report-design-system` |
| 2 | Failing tests written, test cases designed | yes | new tests failed to compile before production types existed |
| 3 | Suite green, coverage held, docs updated | yes | targeted backend tests + Checkstyle |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Issue #1593 is OPEN with CU76 in the body. Adding `in-progress` / Delivery Board
**In Progress** returns HTTP 403 from this integration (`addLabelsToLabelable`).
The Owner authorized Continue after the board-sync paste. No new issue was invented.

Slice 1 does not add Playwright. There is no UI change; the representative visual
fixture is the JUnit 144 DPI structural raster. Heavy UI download coverage is a
later slice.
