# Auto-list required documents when a trámite is confirmed on a gestión (#773 remaining)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #773 |
| Use Case | CU02 – Iniciar Gestión (also CU03, CU04, CU72) |
| Branch | `cursor/feat-773-required-docs-on-confirm-69d3` |
| Gate 1 status | validated (`validate-sdlc-plan.sh` exit 0); apply in progress |

## Objetivo

After PR #1240, a client document can be registered against a trámite and shown
in the case summary, but confirming a trámite via complete-case still does not
materialize the CU03 required-document checklist. CU02 and the user manual require
that list at the moment the gestión's trámite is confirmed. This remaining #773
acceptance criterion blocks closing the issue and unblocks #774.

## What Changes

- On `POST/PUT /api/v1/managements/complete-case`, after a **new** procedure is
  persisted (and its folder generated), the system materializes one pending
  `SubmittedDocument` per `ProcedureTemplate` row for that procedure type
  (`delivered=false`, `reentered=false`, linked to the procedure).
- Materialization is **idempotent**: document types already present for that
  procedure are skipped; PUT complete-case that only updates an existing
  procedure does not wipe or re-seed registered client docs.
- After confirm, `GET /api/v1/managements/{id}/case-summary` lists those pending
  required documents (same `documents` shape as #1240) so the gestión surface
  shows the checklist without visiting CU03/CU43 screens.
- Unit + integration tests prove seed-on-confirm and idempotency; Bruno extends
  complete-case + case-summary; a focused E2E asserts the checklist appears after
  complete-case (tutorial full-journey rewrite remains out of scope).
- English identifiers/logs/messages on every touched line; OpenAPI regenerated.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| After confirming trámites on a gestión, the system generates the list of required documents/certificates per trámite | CU02, user manual | Made explicit in product behavior (was on-demand only via CU03/CU43) |
| Required document types for a procedure type come from `ProcedureTemplate` | CU03 | Made explicit at confirm time |
| Pending required docs appear on the case with the presented-document matrix | CU04, CU72 | New (materialized pending rows) |
| Confirm must not destroy already-registered client documents | CU04 | Made explicit (idempotent seed) |

## Capabilities

### New Capabilities

- `required-docs-on-confirm`: Materialize and surface the CU03 checklist when a
  trámite is confirmed on a gestión via complete-case.

### Modified Capabilities

- (none — prior `document-procedure-link` from `feat-773-documentos-tramite`
  stays; this change adds the confirm-time seed that that slice deferred)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Seed service hooked from complete-case; tests; OpenAPI; Bruno |
| `frontend` | yes (light) | After complete-case success, surface checklist via existing case-summary (or equivalent); Englishize touched strings |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `SubmittedDocument`, `Procedure`, `ProcedureTemplate`, `DocumentType`, `DeedManagement`
- Endpoints: existing `POST/PUT /api/v1/managements/complete-case` (behavior change: side-effect seed); existing `GET /api/v1/managements/{id}/case-summary` (populated sooner). **Not BREAKING** — additive rows; response schema unchanged.
- Database (Flyway `V{n}`): **none** — reuse `submitted_documents` + `procedure_templates` (tip today: V45). Ask foreman before inventing a junction table.
- Configuration / `.env`: none
- Dependencies: #772 CLOSED; prior #773 slice #1240 MERGED; soft: plantilla data for the procedure type

### Architecture review

Compose a small application service (repository layer, not legacy `jpa`) called
from the existing complete-case path after `ProcedureFolderService`. Reuse the
template lookup pattern in `DocumentReentryService`. No ADR — behavioral
completion of CU02/#773, not a new architectural pattern.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU02 – Iniciar Gestión.md` | Implementation note: required-doc list materializes on complete-case |
| `docs/100-business/102-use-cases/CU03 – Lista documentos y certificados necesarios.md` | Note that CU03 catalog feeds confirm-time seed; on-demand screen remains |
| `docs/100-business/102-use-cases/CU04 – Registrar documentación cliente.md` | Pending checklist appears on case before client registration |
| `docs/100-business/102-use-cases/CU72 – Gestión de Documentos Presentados.md` | Required + presented matrix includes pending seeded rows |
| `backend-api/openapi/openapi.yaml` | Regenerated if annotations/docs change |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Map new/updated E2E to CU02/CU03/CU04 |
| `CHANGELOG.md` | Unreleased entry for confirm-time required-doc list (wait for #1518 CHANGELOG lock) |

## Out of Scope

- Full #773 slice already shipped in #1240 (Documentos contract + procedure link +
  case-summary submitted docs) — archive `feat-773-documentos-tramite` when
  closing #773 if still active.
- #1241 (`PUT /submitted-documents/{id}` replace-whole-document).
- Full tutorial-journey E2E rewrite (stale `01-first-case-tutorial.spec.ts`);
  only a focused post-confirm checklist assertion.
- #774 deed/testimony/payment hub (depends on this closing).
- ADR-023 REST renames (#1245).
- Owner decisions #1445 / #1438 / #1226.
