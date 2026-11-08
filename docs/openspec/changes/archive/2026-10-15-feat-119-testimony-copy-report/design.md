> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Slices 1–4 shipped the shared PDF system and CU24/CU25/CU50. CU08 copy is still `renderPlaceholder` after a verified-only check. #119 (RF-94) is the open print issue; #161 (CU08) is closed. RF-94 asks for special sheets and a signed complete deed; the domain has testimony number, verified/observed/notes, copy list, and deed number/date/body only.

## Goals / Non-Goals

**Goals:** Assemble a real CU08 copy PDF from recorded fields; keep 400/404; hexagon-clean port/adapter; extend existing Playwright download.
**Non-Goals:** Inventing special-sheet legal form, signature, or official footer; changing the verify workflow; object-level auth.

## Decisions

- Outbound port + adapter (same pattern as CU50 after hexagonal slices). Application stays free of new JPA/@Component edges.
- Content is recorded fields only. Deed body is printed when present; otherwise `N/A` — do not invent deed text.
- Observed/notes print only when those columns are set.
- Copy count is `copyList.size()` when loaded; otherwise `N/A`.
- Keep filename `testimonio_{idTestimony}_copia.pdf` (existing UI/E2E).
- Empty/missing testimony stays 404; unverified stays 400. No empty-state 200 for a missing id.
- Playwright extends the existing TS-0031 golden-path download: `%PDF-` plus extracted testimony number and deed body.

## Riesgos / Trade-offs

- RF-94 special-sheet / signature remain Owner-pending; this slice is domain-data only.
- Deed body may be short test text; do not treat it as an official form.
- Hexagon R2 will fail if the assembler is left in `application.usecase.report` with `@Component` + JPA.

## Testing Strategy

Failing tests first: `TestimonyCopyReportAdapterTest` (verified rows, unverified rejected by service, missing N/A, no invented signature), extend `TestimonyCopyReportIntegrationTest` to assert extracted PDF text, keep 400/404, Vitest download filename, Playwright TS-0031 text assertion.

## Regression Strategy

`mvn test -pl backend-api -Dtest=TestimonyCopyReportAdapterTest,TestimonyCopyReportIntegrationTest,HexagonArchitectureTest,AdditionalControllersTest,ReportServicePaymentReceiptTest` then targeted Vitest testimony hooks and Playwright TS-0031.

## Playwright Strategy

Extend TS-0031 golden path: after `btn-issue-copy-*`, assert filename `testimonio_*_copia.pdf`, `%PDF-`, and extracted text contains the seeded deed body (`E2E content {number}`). Keep CU08-GW02 blocked-until-verified.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
