# Implementation Plan: Registrar movimientos de documentación de entidades externas

**Branch**: `feat/863_cu10-movimientos-documentacion-entidades-externas` | **Date**: 2026-08-28 | **Spec**: `spec.md`

**Input**: Feature specification `speckit/specs/002-cu10-movimientos-documentacion-entidades-externas/spec.md`

## Summary

Add a `DocumentoEntidadExternaService` + two `GestionController` endpoints
(`GET`/`PUT` under `/api/v1/gestiones/{id}/documentos-entidades-externas`)
that expose and update the `DocumentoPresentado` rows a gestión must receive
from external entities, plus a best-effort automatic transition to
"Documentacion Completa" once all of them are delivered. A dedicated
frontend screen consumes both endpoints. No backend entity or Flyway
migration is needed — the model already supports every field.

## Technical Context

**Language/Version**: Java 21, Spring Boot 4.1.0 (backend); TypeScript 5, React 19, Next.js 16 (frontend).

**Primary Dependencies**: Spring Data JPA (`repository` package), `@tanstack/react-query`, existing `@/theme` design system.

**Storage**: PostgreSQL 16, existing tables (`documentos_presentados`, `tramites`, `inmuebles`, `gestiones_de_escritura`); no migration.

**Testing**: JUnit 5 + Mockito (unit), `ServiceIntegrationTest`/`@SpringBootTest` + H2 (integration), Playwright (E2E).

**Target Platform**: Backend REST API + Next.js dashboard.

**Project Type**: Web application (existing `backend-api/` + `frontend/` split).

**Performance Goals**: Same budget as other gestión detail screens (<2s interactive).

**Constraints**: New data access goes in `repository` (never `jpa`); DTOs named `DtoEntityName`; REST under `/api/v1/...`; frontend must follow `FormContainer`/`FormSection`/`FormField`/`FormActions` and `theme/tokens.ts`.

**Scale/Scope**: One new service, one repository query method, two endpoints, three new DTOs, one new frontend screen + hook, one E2E spec. No new entities.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check Phase 1 design.*

- Issue #863 open, linked to Use Case CU-10 (#163) — Gate 1 satisfied.
- TDD required: unit tests (`DocumentoEntidadExternaServiceTest`) and integration tests (`GestionDocumentosEntidadesExternasIntegrationTest`) written and observed failing before implementation.
- UI change → Playwright E2E mandatory (`.claude/rules/ai-agent-workflow.md` Step 2 table).
- No Flyway migration → `.claude/rules/database-migrations.md` not triggered.
- New REST endpoints → OpenAPI/Swagger annotations required (`@Operation` on both endpoints) and UI-endpoint traceability entry.
- New data access uses `repository` package (`DocumentoPresentadoRepository.findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega`), never `jpa`.

No violations to justify — Complexity Tracking table intentionally omitted.

## Project Structure

### Documentation (this feature)

```text
speckit/specs/002-cu10-movimientos-documentacion-entidades-externas/
├── spec.md
├── plan.md
├── tasks.md
└── traceability.md
```

### Source Code (repository root)

```text
notaire-shared/
└── src/main/java/com/licensis/notaire/dto/
    ├── DtoDocumentoEntidadExterna.java             # NEW
    ├── DtoGestionDocumentosEntidadesExternas.java  # NEW
    └── DtoMovimientoDocumentoEntidadExterna.java   # NEW

backend-api/
├── src/main/java/com/licensis/notaire/
│   ├── service/DocumentoEntidadExternaService.java          # NEW
│   ├── repository/DocumentoPresentadoRepository.java        # +1 query method
│   └── api/GestionController.java                            # +2 endpoints
└── src/test/java/com/licensis/notaire/
    ├── unit/DocumentoEntidadExternaServiceTest.java          # NEW
    ├── unit/AdditionalControllersTest.java                    # updated constructor wiring
    └── integration/GestionDocumentosEntidadesExternasIntegrationTest.java  # NEW

frontend/
├── src/
│   ├── hooks/useDocumentosEntidadExterna.ts        # NEW
│   ├── app/dashboard/documentos-entidades-externas/
│   │   └── page.tsx                                 # NEW — CU10 screen
│   └── types/index.ts                               # +DTO types
└── tests/e2e/
    └── cu10-documentos-entidades-externas.spec.ts    # NEW — Playwright E2E
```

**Structure Decision**: Reuse the existing `backend-api` service/repository/controller
layering. On the frontend, this Use Case is gestión-scoped but the codebase
has no nested `frontend/src/app/dashboard/gestiones/[id]/...` route
convention for workflow-style actions — `gestiones/page.tsx` (bitácora,
transition) and `movimientos-testimonio/page.tsx` (own top-level page) both
use a flat `DataTable` + `Dialog`-keyed-by-entity-id pattern instead. CU10
follows the same shape: a new top-level page listing gestiones, a row action
opens a `Dialog` that fetches `GET .../documentos-entidades-externas` for
that gestión (header + document table), and a nested `Dialog`/form registers
one document's movement (mirrors `movimientos-testimonio`'s "retirar"
sub-dialog). `useDocumentosEntidadExterna.ts` mirrors `usePlantillaTramite.ts`.

## Complexity Tracking

*No Constitution Check violations — table intentionally empty.*

## Testing Strategy *(mandatory — CONSTITUTION.md Gate 3)*

| Spec Scenario | Test Level | Test Class / Spec |
|----------------|-----------|--------------------|
| US1 scenario 1 (detalle con nomenclatura catastral) | Integration | `GestionDocumentosEntidadesExternasIntegrationTest#shouldListDocumentosEntidadesExternas` |
| US1 scenario 2 (gestión inexistente → 404) | Integration | `GestionDocumentosEntidadesExternasIntegrationTest#shouldReturn404WhenGestionDoesNotExist` |
| US2 scenario 1 (registrar movimiento válido) | Integration | `GestionDocumentosEntidadesExternasIntegrationTest#shouldRegistrarMovimiento` |
| US2 scenario 2 (documento no pertenece → 400) | Integration | `GestionDocumentosEntidadesExternasIntegrationTest#shouldReturn400WhenDocumentDoesNotBelongToGestion` |
| US2 scenario 3 (documento inexistente → 404) | Integration | `GestionDocumentosEntidadesExternasIntegrationTest#shouldReturn404WhenDocumentDoesNotExist` |
| US3 scenario 1 (transición automática exitosa) | Unit | `DocumentoEntidadExternaServiceTest$IntentarCompletarDocumentacionTests#shouldTransitionGestionWhenAllDocumentsDelivered` |
| US3 scenario 2 (workflow no define transición → no falla) | Unit + Integration | `DocumentoEntidadExternaServiceTest$IntentarCompletarDocumentacionTests#shouldSwallowBusinessValidationExceptionOnAutoTransition`, `GestionDocumentosEntidadesExternasIntegrationTest#shouldMarkAllDocumentsDeliveredWithoutFailingWhenNoWorkflow` |
| Frontend hook data fetching | Unit (Vitest) | `frontend/src/hooks/useDocumentosEntidadExterna.test.tsx` |
| E2E golden path | Playwright | `frontend/tests/e2e/cu10-documentos-entidades-externas.spec.ts` |

## Regression Strategy *(mandatory)*

New service/endpoints are additive — no existing controller method or DTO is
changed. `AdditionalControllersTest` required updating its `GestionController`
constructor mock wiring (new 15th constructor parameter), which is a
compile-fix, not a behavior change to existing tests. Full backend suite
(`mvn test -pl backend-api`) re-run to confirm no regression: 1595 tests, 0
failures, 0 errors. Full frontend suite (`npm run test`, `npx tsc --noEmit`,
`npm run lint`) re-run before PR.

## Playwright Strategy *(mandatory — UI change)*

New spec `frontend/tests/e2e/cu10-documentos-entidades-externas.spec.ts` covering:
1. Open a gestión's CU10 screen → detail (número, encabezado, nomenclatura catastral) and document list render.
2. Register a movement for a document → form submits, updated data reflected.
3. Mark the last pending document as delivered → UI reflects the (possible) automatic state transition without a client-side error.
Screens verified at 320px, 768px, 1024px per `.claude/rules/ui-ux-design.md`.

## Deployment Strategy *(mandatory)*

Additive change only — new endpoints, new screen, no changes to existing
routes or schema. Ships via the normal `cd.yml` pipeline once merged, no
feature flag or special rollout sequence needed.

## Rollback Strategy *(mandatory)*

Revert the PR. No destructive migration, no data impact — the change adds a
service, two endpoints, and one screen; it does not modify existing
persisted data shapes.
