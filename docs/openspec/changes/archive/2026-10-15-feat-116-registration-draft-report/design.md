> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Slices 1–6 shipped the shared PDF system and CU24/CU25/CU50/CU08. CU82 print is still `renderPlaceholder` after a find-or-404. #116 (RF-91) is the open print issue; CU82 GitHub ID `#313` in the CU doc is a PR, not this issue. RF-91/CU82 ask for an official normalized form; the domain has draft number/status/dates/price/registry notes plus deed and property registry/cadastral columns only. Party roles (transmitente/adquirente) are not a stored field.

## Goals / Non-Goals

**Goals:** Assemble a real CU82 minuta PDF from recorded fields; keep 404; hexagon-clean port/adapter; extend existing Playwright download.
**Non-Goals:** Inventing official-form legal geometry, signature, or grantor/grantee labels; changing generate/submit/observe/register; object-level auth.

## Decisions

- Outbound port + adapter (same pattern as CU08/CU50). Application stays free of new JPA/@Component edges.
- Content is recorded fields only. Missing values are `N/A`.
- Do not add a print-time 400 for incomplete cadastral data; `POST /registration-drafts` already rejects that.
- Parties are distinct full names from procedures on the deed (`personList`, else budget client). No invented role labels.
- Keep filename `minuta_inscripcion_{idRegistrationDraft}.pdf`.
- Playwright extends TS-0082 golden path: `%PDF-` plus extracted cadastral designation.

## Riesgos / Trade-offs

- Official minuta form / signature remain Owner-pending; this slice is domain-data only.
- Hexagon R2 will fail if the assembler is left in `application.usecase.report` with `@Component` + JPA.

## Testing Strategy

Failing tests first: `RegistrationDraftReportAdapterTest` (recorded rows, missing N/A, 404, no invented signature), new `RegistrationDraftReportIntegrationTest` extracted PDF text + 404, Playwright TS-0082 text assertion.

## Regression Strategy

`mvn test -pl backend-api -Dtest=RegistrationDraftReportAdapterTest,RegistrationDraftReportIntegrationTest,HexagonArchitectureTest,ReportServicePaymentReceiptTest` then Playwright TS-0082.

## Playwright Strategy

Extend TS-0082 golden path: after generate, click `btn-download-registration-draft`, assert filename `minuta_inscripcion_*.pdf`, `%PDF-`, and extracted text contains the seeded cadastral designation.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
