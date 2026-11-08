# report-r2-ports Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1849-report-r2-ports. Update Purpose after archive.

## Requirements

### Requirement: Report generation is ports-only at the application boundary

Report HTTP adapters SHALL call `GenerateReportsUseCase` (PDF bytes). Entity-bound report builders and `ReportService` SHALL live under `adapter.out.report`. Application ports SHALL NOT expose `business.*` document-cost template entities.

#### Scenario: Application report R2 edges are gone
- **WHEN** ArchUnit R2 runs after this slice
- **THEN** no baseline edges originate from `application.usecase.report` builders/`ReportService` or `DocumentCostTemplateRepositoryPort`

#### Scenario: Inbound adapters do not import outbound report adapters
- **WHEN** ArchUnit R5 runs after this slice
- **THEN** `ReportController` and `NotebookController` have no edges to `adapter.out.report.ReportService`

#### Scenario: PDF REST behaviour unchanged
- **WHEN** clients call `/api/v1/reports/*` and notebook cover PDF
- **THEN** status codes and PDF generation outcomes match the pre-slice behaviour
