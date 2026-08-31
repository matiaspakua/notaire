# Implementation Plan: Presupuesto persistence must keep its client association

**Branch**: `fix/883_presupuesto-persona-association` | **Date**: 2026-08-31 | **Spec**: `spec.md`

**Input**: Feature specification `speckit/specs/004-cu01-presupuesto-persona-fix/spec.md`

## Summary

`PresupuestoController.create`/`.update` bind directly to the raw
`Presupuesto` JPA entity (`@RequestBody Presupuesto entity`), whose client
relation field is `fkIdPersona`, while the frontend (and `DtoPresupuesto`)
use `persona`. Jackson silently drops the unknown `persona` key, so every
Presupuesto created or edited from the real UI loses its client link; reads
(`GET`) have the mirror-image problem, returning `fkIdPersona` instead of
`persona`. The fix aligns the JSON contract with the field name the frontend
already sends/expects, using the exact pattern the entity already applies to
`montoInmueble` (`@JsonProperty("monto")` on its getter/setter): add
`@JsonProperty("persona")` to `Presupuesto.getFkIdPersona()` /
`.setFkIdPersona()`. No new entity, no DTO change, no Flyway migration, no
controller rewrite — smallest possible fix consistent with the codebase's
own existing convention for this exact class of bug.

## Technical Context

**Language/Version**: Java 21, Spring Boot 4.1.0 (backend); TypeScript 5, React 19, Next.js 16 (frontend, unaffected — already sends/expects `persona`).

**Primary Dependencies**: Jackson (`com.fasterxml.jackson.annotation.JsonProperty`, already imported in `Presupuesto.java`), Spring Data JPA.

**Storage**: PostgreSQL 16, existing `presupuestos` table, no schema change.

**Testing**: JUnit 5 + `@SpringBootTest` + real PostgreSQL (`@Tag("pg-integration")`, mirrors the #879 precedent — this bug class needs a real DB-backed round trip through the actual REST JSON contract, not just an H2 entity test) plus MockMvc; Playwright (E2E, golden path + list/search).

**Target Platform**: Backend REST API (`/api/v1/presupuestos`) consumed by `frontend/src/app/dashboard/presupuestos/page.tsx`.

**Project Type**: Web application (existing `backend-api/` + `frontend/` split); this change touches only `backend-api` and its tests.

**Performance Goals**: N/A — no behavior change beyond correct field binding.

**Constraints**: Follow the existing `@JsonProperty` alias pattern already used in the same class for `monto`/`montoInmueble`; do not introduce a DTO-binding rewrite of the controller (out of scope per spec's Assumptions — larger blast radius, not needed to satisfy FR-001..FR-004).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Issue #883 references CU01 — Preparar Presupuesto. ✅
- TDD: new `PresupuestoPersonaAssociationPgIntegrationTest` written and
  observed failing before the `@JsonProperty` change. ✅
- No new entity/DTO/migration — Flyway untouched. ✅
- Existing tests that constructed requests using the buggy `fkIdPersona` JSON
  key (`PresupuestoResumenControllerTest`, `GestionResumenFinancieroControllerTest`,
  `GestionArchiveIntegrationTest`, `GestionControllerIntegrationTest`) must be
  updated to `persona` as part of this fix (Regression Strategy below).
- UI already calls this endpoint (`usePresupuestos.ts` → `/presupuestos`);
  no new endpoint, so no new traceability entry needed beyond the fix itself.

No violations requiring justification — Complexity Tracking table omitted.

## Project Structure

### Documentation (this feature)

```text
speckit/specs/004-cu01-presupuesto-persona-fix/
├── spec.md
├── plan.md
├── tasks.md
└── traceability.md
```

### Source Code (repository root)

```text
backend-api/
├── src/main/java/com/licensis/notaire/negocio/
│   └── Presupuesto.java                                   # +2 @JsonProperty("persona") annotations
└── src/test/java/com/licensis/notaire/
    ├── integration/PresupuestoPersonaAssociationPgIntegrationTest.java  # NEW
    ├── integration/PresupuestoResumenControllerTest.java   # updated: fkIdPersona -> persona
    ├── integration/GestionResumenFinancieroControllerTest.java  # updated: fkIdPersona -> persona
    ├── integration/GestionArchiveIntegrationTest.java       # updated: fkIdPersona -> persona
    └── integration/GestionControllerIntegrationTest.java    # updated: fkIdPersona -> persona (createPresupuesto helper)

frontend/
└── tests/e2e/
    └── cu01-presupuesto-persona.spec.ts                     # NEW — Playwright E2E
```

**Structure Decision**: Single-file production change (`Presupuesto.java`),
mirroring the minimal-diff shape of the #879 fix. No frontend source change
needed — the frontend already sends/reads `persona`; only backend tests that
encoded the old (buggy) contract need updating.

## Testing Strategy *(mandatory — CONSTITUTION.md §3)*

| Scenario | Test Type | Test Name |
|----------|-----------|-----------|
| US1 scenario 1 (create with `persona` → linked) | PG Integration | `PresupuestoPersonaAssociationPgIntegrationTest#shouldPersistPersonaAssociationOnCreate` |
| US1 scenario 2 (edit to add `persona` → linked) | PG Integration | `PresupuestoPersonaAssociationPgIntegrationTest#shouldPersistPersonaAssociationOnUpdate` |
| US1 scenario 3 (omit `persona` → still succeeds) | PG Integration | `PresupuestoPersonaAssociationPgIntegrationTest#shouldCreateWithoutPersonaWhenOmitted` |
| US2 scenario 1 (`GET /{id}` returns `persona`) | PG Integration | `PresupuestoPersonaAssociationPgIntegrationTest#shouldReturnPersonaFieldOnGetById` |
| US2 scenario 2 (`GET` list reflects presence/absence correctly) | PG Integration | `PresupuestoPersonaAssociationPgIntegrationTest#shouldReflectPersonaAcrossListedPresupuestos` |
| E2E golden path (create from UI, verify persisted client) | Playwright | `frontend/tests/e2e/cu01-presupuesto-persona.spec.ts` |

## Regression Strategy *(mandatory)*

The four existing tests that build Presupuesto request bodies with
`"fkIdPersona": {"idPersona": %d}` were written against the pre-fix (buggy)
contract and must be updated to `"persona": {"idPersona": %d}` so they keep
exercising the real client-linking behavior instead of silently creating
Presupuestos with no client (which is exactly the bug). This is a required
compile/behavior-preserving update, not a scope change — each of those tests'
actual assertions (about Resumen/Archive/Gestion behavior) are unaffected.
Full backend suite (`mvn verify -pl backend-api`) re-run to confirm no other
regression; JaCoCo ratchet floor must hold.

## Playwright Strategy *(mandatory — UI change)*

New spec `frontend/tests/e2e/cu01-presupuesto-persona.spec.ts` covering:

1. Create a Presupuesto from `/dashboard/presupuestos` selecting an existing
   cliente → success message, table row shows the cliente's apellido.
2. Search presupuestos by cliente apellido (CU60) → the just-created
   Presupuesto is found, proving the read side also carries the association.

## Deployment Strategy *(mandatory)*

No infrastructure change. Standard `docker compose build backend && docker
compose up -d backend` picks up the fix; no Flyway migration to apply, no
config/env changes.

## Rollback Strategy *(mandatory)*

Pure code revert (`git revert` of the fix commit) — no data migrated, no
schema changed, so rollback is a straightforward redeploy of the prior
`Presupuesto.java`.
