# Registration draft PDF (#116)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #116 |
| Use Case | CU82 — Generar Minuta de Inscripción / RF-91 |
| Branch | `cursor/116-registration-draft-5fcb` |
| Gate 1 status | passed |

## Objetivo

RF-91 / CU82 still downloads a one-page placeholder that only names the draft number, deed number, and status. The printed minuta must show recorded draft, deed, party, and property registry/cadastral fields. Do not invent official-form geometry, a signature block, or Owner legal wording.

## What Changes

- Replace `ReportService.generateRegistrationDraftReport` placeholder with a `ReportDocument` assembled behind `RegistrationDraftReportPort` / `adapter.out.report.RegistrationDraftReportAdapter` (hexagon R2).
- Print only recorded values: draft number/status/generation date/price, deed number/date, parties, cadastral designation, registration number, volume/folio, boundaries, fiscal appraisal, address. Missing values stay the shared `N/A` fallback.
- Keep HTTP 404 when the draft does not exist. Do not add a new 400 on print; cadastral completeness is already enforced at `POST /registration-drafts`.
- Filename stays `minuta_inscripcion_{id}.pdf`. Extend Playwright TS-0082 download with extracted-text assertions.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A generated registration draft PDF contains recorded draft, deed, party, and property fields; missing values are N/A; unknown id stays 404 | RF-91, CU82 | New |

## Capabilities

### New Capabilities
- `registration-draft-report`: CU82 registration-draft PDF from recorded fields; no invented official form

### Modified Capabilities
- `pdf-report-design-system`: CU82 print consumes the shared renderer with recorded fields.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | outbound port + adapter, ReportService wiring, unit/integration tests |
| `frontend` | yes | filename and download hook stay; Playwright TS-0082 text assertion |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: RegistrationDraft, Deed, Procedure, Property, Person (read-only)
- Endpoints: `GET /api/v1/reports/registration-draft/{idRegistrationDraft}` (same path)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Hexagon R2 shrink-only: new assembler is `RegistrationDraftReportPort` + `adapter.out.report.RegistrationDraftReportAdapter`. `ReportService` injects the port. Do not add a new application `@Component` builder that depends on `business.*` or `repository.*`. Do not expand the hexagon baseline.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md` | Printed-draft fields, #116, no invented form |
| `docs/200-architecture/201-SAD/pdf-report-design-system.md` | CU82 no longer a placeholder |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | Registration-draft PDF content contract |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | TS-0082 download text assertion |
| `docs/300-development/303-testing/INVENTORY.md` | regenerated |
| `CHANGELOG.md` | Unreleased #116 |
| `CHANGELOG.md` | Unreleased #116 |

## Out of Scope

- Owner-approved official minuta form geometry, watermark, or signature language
- Invented grantor/grantee role labels when the domain has no recorded role
- Object-level report authorization
- Print-quality logo / PDF/UA-1 claim
- Changing generate/submit/observe/register workflow
- CU15 receipt and CU80 notebook-cover slices
