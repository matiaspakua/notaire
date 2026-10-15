# Testimony copy PDF (#119)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #119 |
| Use Case | CU08 — Verificar Testimonio / RF-94 Impresión de Testimonios |
| Branch | `cursor/119-testimony-copy-5fcb` |
| Gate 1 status | passed |

## Objetivo

RF-94 / CU08 still download a one-page placeholder that only names the testimony number and that it is verified. The printed copy must show recorded testimony and deed fields so the notary can issue a copy after verification. Do not invent special-sheet legal geometry, a signature block, or Owner official-form wording.

## What Changes

- Replace `ReportService.generateTestimonyCopyReport` placeholder with a `ReportDocument` assembled behind `TestimonyCopyReportPort` / `adapter.out.report.TestimonyCopyReportAdapter` (hexagon R2: no new application → business/repository/@Component edges).
- Print only recorded values: testimony number, verified/observed, notes, copy count, deed number, deed date, deed body when present. Missing values stay the shared `N/A` fallback.
- Keep HTTP 400 when the testimony is not verified and HTTP 404 when it does not exist.
- Filename stays `testimonio_{id}_copia.pdf`. Extend Playwright CU08 copy download with extracted-text assertions.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A printed copy is issued only for a verified testimony and contains recorded testimony/deed fields; unverified stays 400 | RF-94, CU08 | New |

## Capabilities

### New Capabilities
- `testimony-copy-report`: CU08 verified-testimony PDF from recorded fields; no invented legal form

### Modified Capabilities

- `pdf-report-design-system`: CU08 copy consumes the shared renderer with recorded fields.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | outbound port + adapter, ReportService wiring, unit/integration tests |
| `frontend` | yes | filename and download hook stay; Vitest if filename/text contract changes |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: Testimony, Deed, Copy (read-only)
- Endpoints: `GET /api/v1/reports/testimonies/{idTestimony}/copies` (same path)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Hexagon R2 shrink-only: new assembler is `TestimonyCopyReportPort` + `adapter.out.report.TestimonyCopyReportAdapter`. `ReportService` injects the port. Do not add a new application `@Component` builder that depends on `business.*` or `repository.*`. Do not expand the hexagon baseline.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU08 – Verificar Testimonio.md` | Printed-copy fields, #119, no invented form |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | CU08 no longer a placeholder |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | Copy PDF content contract |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | CU08 download text assertion |
| `docs/300-development/303-testing/INVENTORY.md` | regenerated |
| `CHANGELOG.md` | Unreleased #119 |

## Out of Scope

- Owner-approved special-sheet geometry, watermark, or signature language
- Invented full-deed legal form when `deed.body` is empty
- Object-level report authorization
- Print-quality logo / PDF/UA-1 claim
- CU15 receipt, CU80 notebook cover, CU82 registration-draft slices
