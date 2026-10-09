# In-house report PDFs on the current schema, without JasperReports (#567)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #567 |
| Use Case | CU01 – Preparar Presupuesto; CU03 – Lista documentos y certificados necesarios; CU09 – Registrar deudas documentos de Cliente; CU13 – Ver historial de gestión; CU42 – Informar próximos vencimientos |
| Branch | `fix/567_inhouse_report_pdfs` |
| Gate 1 status | draft |

## Objetivo

Six report endpoints were JasperReports 3.5.3 templates whose SQL targets the legacy MySQL schema, so every call answered 500, and the 2009 library pulled in itext 2.1.0, bcprov-jdk14 136, commons-collections 2.1 and commons-beanutils 1.8.0. The Owner chose Option B on #567: drop JasperReports and generate the PDFs in-house from the current schema, keeping the endpoints and an OpenAPI contract that matches the implementation.

## What Changes

- New outbound port `ReportRenderer` with the framework-free `ReportDocument` model (title, field and table sections, totals).
- `PdfBoxReportRenderer` adapter on Apache PDFBox 3.0.8 (Apache License 2.0): A4, wrapped text, page breaks repeating table headers, `Página i de n` footer, WinAnsi-safe text.
- `ReportDocumentFactory` builds the six reports from the current domain (budget totals from the CU47 summary); missing aggregates are 404.
- `ReportService` delegates to them; `DataSource`/JDBC/Jasper code removed.
- `jasperreports` 3.5.3 removed from `pom.xml`; `.jasper` templates, their copy in `backend-api/resources/reportes` and the unused `.jrxml` files deleted.
- OpenAPI: 200/400/404/500 documented on the six endpoints, descriptions say what each PDF contains; CU24/CU25/CU50 no longer described as Jasper templates.
- Frontend: the document debt hook sends `numberManagement` (it sent `numeroGestion` and always got 400).
- Bruno `report-pdfs`: happy paths and 404 probes; no request accepts 500 any more.
- Docs: SAD runtime view 6.3, AGENTS.md, README, REST API reference, input-validation strategy.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Budget report total and balance equal the CU47 budget summary | CU01, CU47 | Made explicit |
| A report for a missing budget, procedure type, management, submitted document or management number is a 404 | #567 | New |
| A document with an amount and no payment date is shown as `Pago pendiente` and counts towards the debt total | CU09 (legacy report) | Made explicit |

## Capabilities

### New Capabilities

- `report-pdfs`: PDF reports generated from the current domain model.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | report port, PDFBox adapter, `ReportDocumentFactory`, `ReportService`, `ReportController`, pom, OpenAPI, Bruno |
| `frontend` | yes | `useReportes.ts` debt query parameter |
| `docs` | yes | SAD, REST API reference, security strategy |
| `contracts` | no | allowlist unchanged (see design) |

### Surface area

- Endpoints: `GET /api/v1/reportes/presupuesto/{id}`, `presupuesto-inmuebles/{id}`, `lista-documentos-tramite`, `historial-gestion/{id}`, `documentos-por-vencer/{id}`, `consultar-deuda-documentos` now answer PDFs (were 500); 400/404/500 documented
- Dependencies: `+ org.apache.pdfbox:pdfbox:3.0.8`, `- jasperreports:jasperreports:3.5.3`
- Entities / Flyway / Configuration: none (two derived repository finders added)

### Architecture review

Hexagonal, as ADR-021: the use case builds a `ReportDocument` and depends only on the `ReportRenderer` outbound port; PDFBox lives in `adapter.out.pdf`. Reports read the current JPA repositories inside a read-only transaction.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed, Removed and Security entries |
| `backend-api/openapi/openapi.yaml` | responses and descriptions |
| `docs/200-architecture/201-SAD/sad.md` | runtime view 6.3 |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | report rows |
| `AGENTS.md`, `README.md` | report stack |
