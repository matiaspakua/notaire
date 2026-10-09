# Design

## Context

Issue #567 asked to upgrade or replace JasperReports 3.5.3 (2009). Investigation showed the templates query legacy MySQL tables (`presupuestos`, `tramites`, `IFNULL`, `DATE_FORMAT`) that no longer exist, so the six endpoints never worked on the current PostgreSQL schema.

## Goals / Non-Goals

Goal: working reports for the six endpoints and no JasperReports. Non-goals: real content for the CU24/CU25/CU50 placeholders, a UI for `documentos-por-vencer` (#1250 bucket E), migrating the seven other text PDFs to PDFBox.

## Decisions

Library: Apache PDFBox 3.0.8 (Apache License 2.0) over OpenPDF (LGPL/MPL dual) for the permissive licence; behind a port so it can be swapped. Report content: each legacy report was mapped to the current domain; columns with no current equivalent (archive and binder numbers in the history report) were dropped, and budget totals come from the CU47 summary instead of a stored total. `documentos-por-vencer/{id}` keeps its per-document contract. Management numbers are not unique, so the debt report covers every management with that number. The reachability allowlist is left untouched to avoid a conflict with #1325, which rewrites its reasons; the bucket E entry should become bucket C (UI backlog) once both are merged.

## Riesgos / Trade-offs

Reports look different from the 2009 templates; they were unusable (500) anyway. PDFBox adds about 3.5 MB (pdfbox, pdfbox-io, fontbox) and removes the larger Jasper tree.

## Testing Strategy

`InHouseReportPdfIntegrationTest` (8 cases, PDF text extracted with PDFBox) written first and observed failing (all six endpoints 500, unknown name/number 500, Jasper on the classpath); `PdfBoxReportRendererTest` (6 cases) failed to compile before the adapter existed; `useReportes-contract.test.ts` failed on the debt query parameter.

## Regression Strategy

Full backend suite; frontend unit tests; Bruno `report-pdfs` (15/15) and `payments`, `budgets`, `managements`, `history`, `submitted-documents`, `procedure-types` (67/67) against this backend on PostgreSQL 17; Playwright TS-0020 reports workflow.

## Playwright Strategy

TS-0020 (reports admin workflow) re-run on this backend; no new UI.

## Deployment Strategy

Backend and frontend; no migration.

## Rollback Strategy

Revert the merge commit (re-adds JasperReports and the broken reports).
