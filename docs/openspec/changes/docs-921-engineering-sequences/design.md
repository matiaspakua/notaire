> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`TestimonyMovementService` and `DocumentReentryService` already implement
CU08, CU11, CU12, CU43, and CU44. `DOC-CICLO` §3.4 has the testimony state
machine. `DOC-GESTION` §3.4 did not match those calls.

## Goals / Non-Goals

**Goals:**

- One sequence per call the services accept on this path.
- Keep states in `DOC-CICLO` only.

**Non-Goals:**

- A new state machine, a new ADR, or a product change.
- Rewriting CU01–CU87.

## Decisions

The sequences go in `DOC-GESTION`, which is already the case schema, and
receive the id `DOC-GESTION`. A second lifecycle file would split the source.
The catalog of what exists is `EVALUACION-INGENIERIA.md`, next to the method,
so the availability answer is not only in a pull request.

## Riesgos / Trade-offs

[CU43 still names `GET/POST /api/v1/gestiones/{id}/reingreso-documentacion`, CU44 still names `POST /api/v1/movimiento-testimonio/{id}/reenter`, CU10 still names `GET/PUT /api/v1/gestiones/{id}/documentos-entidades-externas[/{idDocumentoPresentado}]`, CU53 and CU83 still name `POST /api/v1/gestiones/{id}/transition`, CU22 still names `PUT /api/v1/gestiones/complete-case`, CU83 still names `GET /api/v1/gestiones/{id}/workflow-trace`, CU13 still names `GET /api/v1/gestiones/{id}/historial` and `GET /api/v1/gestiones/{id}/estado-actual`, and CU53 still names `PUT /api/v1/gestiones/{id}` and `PUT /api/v1/gestiones/{id}/complete-case`] → The live call is in
`DOC-GESTION`. Rewriting every file is a different change.
[CU01 and CU39 still name `POST /api/v1/presupuestos/{id}/items-desde-plantilla`] → The live call is in
`presupuesto-from-template`.
[CU01, CU39, and CU71 still name `/dashboard/presupuestos`] → The live screen is in
`presupuesto-from-template`.
[CU10 still names `/dashboard/documentos-entidades-externas`] → The live screen is in
`DOC-GESTION`.
[CU43 still names `/dashboard/reingreso-documentacion`] → The live screen is in
`DOC-GESTION`.
[CU22 and CU59 still name `/dashboard/suplencias`] → The live screen is in
`dashboard-nav-canonical-routes`.
[CU24, CU25, and CU50 still name `/dashboard/reportes`] → The live screen is in
`dashboard-nav-canonical-routes`.
[CU23 still names `/dashboard/auditoria` and `/dashboard/administracion/auditoria`] → The live screen is in
`dashboard-nav-canonical-routes`.
[CU22 still names `PUT /api/v1/gestiones`] → The live call is in
`gestion-asignacion-suplencia`.
[CU78 still names `/dashboard/administracion/**`] → The live screen is in
`admin-route-guard`.
[CU83 and CU13 still name `PUT /gestiones/{id}` and `PUT /gestiones/{id}/complete-case`] → The live call is in
`DOC-GESTION`.
[CU13 still names `POST /gestiones` and `GET /gestiones/{id}/estado-actual`] → The live call is in
`DOC-GESTION`.
[EVALUACION-DOCUMENTACION still names `POST /api/v1/gestion` and `gestiones_de_escrituras`] → The live call is in
`DOC-GESTION`.
[CU43 still names `ReingresoDocumentacionService` and CU10 still names `DocumentoEntidadExternaService`] → The live classes are
`DocumentReentryService` and `ExternalEntityDocumentService`.
[CU43 still names `PlantillaTramite`] → The live class is
`ProcedureTemplate`.
[CU43 and CU42 still name `DocumentoPresentado`] → The live class is
`SubmittedDocument`.
[CU83 and CU39 still name `TipoDeTramite`] → The live class is
`ProcedureType`.
[CU83 still names `EstadoDeGestion`] → The live class is
`ManagementStatus`.
[CU39 still names `PlantillaPresupuesto`] → The live class is
`BudgetTemplate`.
[CU39 still names `PlantillaCostoDocumento`] → The live class is
`DocumentCostTemplate`.
[CU42 still names `GET /api/v1/documento-presentado/proximos-vencimientos?dias=N` and `/dashboard/proximos-vencimientos`] → The live call is in
`upcoming-expirations`.
[CU71 still names `POST /api/v1/presupuestos/{id}/items-desde-catalogo`] → The live call is in
`presupuesto-catalogo-items`.
[CU45 still names `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos`] → The live call is in
`descuentos-recargos-presupuesto`.
[CU47 still names `GET /api/v1/presupuestos/{id}/resumen`] → The live call is in
`pago-presupuesto-gestion-summary`.
[CU47 still names `GET /api/v1/pagos/presupuesto/{id}/estado`] → The live call is in
`pagos-parciales-cuotas`.
[Dictionary stays stale] → #1222, recorded as not a missing sequence.

## Testing Strategy

`docs/tests/test_gestion_sequences.py` asserts the four calls and the
`DOC-GESTION` id. It fails while those strings are absent.

| Acceptance criterion | Test |
|----------------------|------|
| Verify, inscription, testimony re-entry, and document re-entry are named | `test_testimony_path_includes_verify_and_inscription` |
| Observada still has no return, and re-entry does not move the management | `test_reentry_does_not_invent_a_return_from_observada` |
| The case schema has an artifact id | `test_case_schema_has_an_artifact_id` |

## Regression Strategy

`docs/tests/test_docs_links.py`, `docs/tests/test_changelog_structure.py`,
and `python3 workspace/sdlc/check-doc-traceability.py origin/main`.

## Playwright Strategy

No UI surface. Playwright is not required.

## Deployment Strategy

Documentation only. No image and no migration.

## Rollback Strategy

Revert the documentation commit. No data change.
