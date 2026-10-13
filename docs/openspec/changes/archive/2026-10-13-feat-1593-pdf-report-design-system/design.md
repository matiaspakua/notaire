# Design

## Context

#567 replaced six Jasper templates with PDFBox and `ReportDocument`. Seven other
surfaces still emit hand-built Helvetica byte streams. There is no shared theme,
embedded font, logo contract, deterministic clock, or consistent PDF headers.
Issue #1593 / CU76 is Slice 1 of the modernization plan: the shared foundation
before business-content slices.

## Goals / Non-Goals

Goals: one adapter-owned theme; embedded OFL font; text wordmark; repeating
chrome; injected clock/zone; centralized headers/metrics; migrate the seven
hand-built PDFs onto `ReportDocument` without changing their business fields;
tests for structure, fonts, and determinism.

Non-goals: CU24/CU25/CU50 query content; legal form language; object-level
authorization; PDF/UA-1 claim; Owner-approved print logo; procedure-document UI
(#1250); documents-due UI (#1363); Jasper removal (#1438).

## Decisions

- Keep the hexagonal port: `ReportDocument` stays framework-free; PDFBox stays
  in `adapter.out.pdf`.
- Package Liberation Sans Regular/Bold as immutable classpath resources (SIL OFL
  1.1). Do not add a Maven font artifact.
- Draw a text wordmark. Do not embed or upscale `docs/images/logoRojoLetraMediano.png`.
- Default locale `es-AR` and footer `Página X de Y` (existing English `Page X of Y`
  is a design-system correction, not a content expansion).
- Inject `Clock` rather than `LocalDateTime.now()`. Provide a `reportClock` bean
  at `America/Argentina/Buenos_Aires`.
- Centralize headers in `PdfResponseFactory`. Wrapped 500s use `ErrorResponses.serverError`
  (safe generic body, CWE-209).
- Record metrics in the adapter (type, outcome, duration, pages, rows). Never log
  names, IDs, or document bodies.
- Set catalog language `es-AR` and MarkInfo.marked. Do not claim PDF/UA.
- Representative visual fixture is a 144 DPI structural raster in JUnit, not a
  Playwright pixel baseline. Slice 1 has no UI change.

## Riesgos / Trade-offs

- Footer language change updates two existing renderer assertions.
- Payment-receipt unit tests that read raw ASCII streams must use PDFTextStripper
  after the Helvetica byte-stream is removed.
- Structured 500 is a small HTTP contract change (body appears where it was empty).
- Embedded fonts increase PDF size versus Standard 14; accepted for glyph coverage.
- Board/label coherence for #1593 cannot be written (403). Owner authorized Continue.

## Testing Strategy

Written first: extend `PdfBoxReportRendererTest` and add `ReportThemeTest`,
`ReportAssetCatalogTest`, `ReportTextSanitizerTest`, `ReportLocaleFormatsTest`,
`PdfResponseFactoryTest`, `PdfReportStructuralRenderTest`,
`ReportGenerationRecorderTest`. Observe failures (missing language, Helvetica,
`Page` footer, no wordmark, no shared headers). Then implement.

## Regression Strategy

`mvn test -pl backend-api -Dtest=PdfBoxReportRendererTest,PdfReportStructuralRenderTest,ReportThemeTest,ReportAssetCatalogTest,ReportTextSanitizerTest,ReportLocaleFormatsTest,PdfResponseFactoryTest,ReportGenerationRecorderTest,ReportControllerTest,ReportServicePaymentReceiptTest,InHouseReportPdfIntegrationTest`
then module verify as far as the environment allows. Pipeline/preflight before push.

## Playwright Strategy

None in Slice 1. No UI, route, or filename change. Download E2E is a later slice.

## Deployment Strategy

Backend only. Fonts ship inside the API jar. Rollback is reverting the PR.

## Rollback Strategy

Revert the merge. No schema or config migration.
