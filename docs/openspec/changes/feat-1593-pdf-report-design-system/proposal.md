# Accessible deterministic PDF design system (#1593)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1593 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure (supporting RF #7, #17, #23, #48–54, #103, #117, #120 without changing their business content) |
| Branch | `feat/1593_pdf-report-design-system` |
| Gate 1 status | draft |

## Objetivo

Establish one shared, accessible, deterministic PDF design system so every current
report uses the same page anatomy, embedded Unicode font, text wordmark, clock/zone,
and HTTP response headers. This is Slice 1 of the PDF modernization plan. Individual
business-content expansions (CU24/CU25/CU50 and legal forms) stay in later issues.

## What Changes

- Backend-owned `ReportTheme` tokens (A4 portrait/landscape, 18/16 mm margins, type
  scale, grid, brand accent) consumed only by the PDF adapter.
- Embedded Liberation Sans (SIL OFL 1.1) for user/domain text; Spanish/English
  fixtures must not become `?`.
- Classpath logo contract: text wordmark "Notaire" until the Owner approves a
  print-quality asset. The 100×100 legacy PNG is not used.
- Repeating header (wordmark, report type, identifier) and footer (`Página X de Y`,
  product version, generated timestamp in `America/Argentina/Buenos_Aires`).
- `ReportDocument.Identity` for type/identifier/period/empty-state/orientation.
- Deterministic `Clock` + `es-AR` metadata (`Lang` = `es-AR`).
- Seven hand-built Helvetica byte-stream PDFs migrate onto `ReportDocument` + the
  shared renderer, keeping existing field content.
- Centralized PDF headers: `application/pdf`, safe quoted filename, `Content-Length`,
  `Cache-Control: private, no-store`, `X-Content-Type-Options: nosniff`.
- Structured 500 via `ErrorResponses.serverError` instead of a bodyless 500.
- Generation metrics (`report_generation_total`, duration, pages, rows) without PII.
- Tests for metadata, fonts, geometry, wrapping, pagination, sanitizer, headers,
  empty states, and a 144 DPI structural raster (not pixel-only equality).
- Permanent SAD/security/report-design docs and CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Shared print tokens and page anatomy for every PDF | CU76, RF #74–78 | New |
| Default locale `es-AR`; dates `dd/MM/yyyy`; money `$ 1.234,56` | CU76 / existing factory | Made explicit |
| Generated timestamp uses injected clock and Buenos Aires zone | CU76, PDF-DS-06 | New |
| Embedded open-license font; no Standard 14 replacement for Spanish/English | CU76, PDF-DS-02 | New |
| Text wordmark until Owner-approved print logo | CU76, Owner decision pending | New |
| Existing report business fields stay the same in Slice 1 | CU01/03/09/13/15/42 and placeholders | Made explicit |
| Legal forms, authorization matrix, PDF/UA claim, legacy logo approval | Owner | Out of scope |

## Capabilities

### New Capabilities

- `pdf-report-design-system`: shared theme, assets, chrome, headers, metrics, and tests.

### Modified Capabilities

- (none in `docs/openspec/specs/`; this introduces the capability)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | theme, renderer, assets, headers, factory identity, service placeholders, metrics, tests, docs |
| `frontend` | no | HTTP contract unchanged except extra cache/nosniff headers |
| `testing` | no | no new Playwright in Slice 1 (no UI change; visual fixture is JUnit raster) |
| `docs` | yes | SAD, security input-validation, report design standard, CHANGELOG, OpenSpec |
| `contracts` | no | endpoint reachability unchanged (#1363 preserved) |

### Surface area

- Endpoints: same `/api/v1/reports/**` and `GET /api/v1/notebooks/{id}/cover`
- Response headers gain `Cache-Control`, `X-Content-Type-Options`, `Content-Length`
- 500 on wrapped report endpoints becomes structured `ErrorResponse` JSON
- Footer label changes from English `Page X of Y` to `Página X de Y`
- Flyway: none
- Dependencies: Liberation Sans packaged as resources (no new Maven artifact)

### Architecture review

Keep `ReportController → ReportService → ReportDocumentFactory → ReportRenderer`.
Theme and PDFBox stay in `adapter.out.pdf`. Application/domain remain framework-free.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | New design standard |
| `docs/200-architecture/201-SAD/sad.md` | Point at the design system |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | Font/sanitize contract |
| `CHANGELOG.md` | Unreleased feature |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Link Slice 1 issue |
