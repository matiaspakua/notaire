> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`WorkflowDefinition`, `WorkflowNode` and `WorkflowTransition` already exist
(CU83, ADR-014) and `WorkflowValidationService` already validates that a
definition is structurally consistent (one initial node, at least one
reachable final node). `WorkflowTraceService.buildTrace(gestionId)` already
reads `Historial` joined with those nodes/transitions to compute a
per-node status — but nothing writes to `Historial` from any real flow,
confirmed by grepping the codebase for `historialRepository.save`. Every
Historial-based screen (`GET /{id}/estado-actual`, `GET /{id}/workflow-trace`)
reads from a table nothing populates. `GestionController.update()`
(`PUT /{id}`) saves the raw `GestionDeEscritura` entity, including
`estado`, without checking it against any `WorkflowTransition`.
`GestionArchiveDebtService.archivar(idGestion)` (issue #819, already
merged) sets `estado` to "Archivada" by name lookup, computing/warning the
pending balance, but does not validate the transition or write
`Historial` either. See `proposal.md` — Objetivo for full business
motivation.

## Goals / Non-Goals

**Goals:**
- Add a `GestionTransitionService` that validates a proposed estado change
  against the `WorkflowTransition`s of the gestión's tipo de trámite
  `WorkflowDefinition`, and a dedicated action endpoint to trigger it.
- Add a `GestionBitacoraService` that writes a `Historial` entry on
  creation, valid transition, and archiving, and exposes it for
  consultation (CU13).
- Make `GestionArchiveDebtService.archivar` reuse the same transition
  validation and write its own `Historial` entry, without changing its
  existing pending-debt calculation/warning behavior (issue #819).
- Add the frontend "Cambiar estado" action (destination limited to valid
  workflow targets) and a bitácora view to
  `frontend/src/app/dashboard/gestiones`.

**Non-Goals:**
- Changing `WorkflowDefinition`/`WorkflowNode`/`WorkflowTransition` CRUD or
  their structural validation (CU83 already covers it) — this change only
  consumes those definitions.
- Restricting the generic `PUT /{id}` from accepting any `estado` value —
  out of scope for this change (documented as a known limitation in
  Riesgos below, same pattern as issue #832's design.md).
- Changing the pending-balance calculation or debt warning (issue #819).

## Decisions

- **New `GestionTransitionService` and `GestionBitacoraService` classes
  instead of adding methods to `GestionController` or the legacy `jpa`
  package.** Keeps a single place that owns "is this transition valid"
  and a single place that owns "write/read Historial", reused by both the
  new transition endpoint and the existing `archivar` flow, instead of
  duplicating the check. Alternative considered: inline the validation in
  `GestionController` and `GestionArchiveDebtService` separately —
  rejected because it would duplicate the same workflow-lookup logic in
  two places and diverge over time.
- **`GestionArchiveDebtService.archivar` calls `GestionTransitionService`
  internally rather than the frontend calling transition then archive as
  two separate requests.** Archiving already has its own explicit
  endpoint and UI action (issue #819); requiring two round-trips would
  change that existing UX for no benefit. Alternative considered: have
  the frontend call `POST /transicionar` with destino "Archivada" and
  drop the dedicated archivar endpoint — rejected because it would
  discard the existing pending-debt warning flow built in #819.
- **Action endpoint `POST /api/v1/gestiones/{id}/transicionar` instead of
  validating inside the generic `PUT /{id}`.** Same rationale as issue
  #832's design.md: a dedicated action endpoint can express "the client
  wants to transition estado" distinctly from "the client wants to edit
  other fields", and matches CU83/CU13's separate-action semantics.
  Alternative considered: validate `estado` changes inside `PUT /{id}` —
  rejected because it would still allow every other field to change
  silently in the same call, same reasoning as design.md's parallel
  decision in `escritura-post-firma-legal-cycle`.

## Riesgos / Trade-offs

- [El endpoint `PUT /{id}` genérico sigue permitiendo poner `estado` en
  cualquier valor sin pasar por `GestionTransitionService`, evitando la
  regla nueva] → Mitigación: fuera de alcance de este cambio (ver
  Non-Goals); se documenta como limitación conocida para un cambio
  posterior que restrinja o audite el PUT genérico, igual que en
  `escritura-post-firma-legal-cycle`.
- [Concurrencia: dos transiciones simultáneas sobre la misma gestión
  podrían ambas leer el mismo estado actual antes de que la primera
  confirme] → Mitigación: la transición se ejecuta dentro de una
  transacción (`@Transactional`) que relee el estado antes de escribir;
  la segunda solicitud falla la validación de precondición al confirmar.
- [Gestiones ya existentes en producción no tienen ninguna entrada de
  `Historial` (la bitácora nunca se pobló hasta ahora), así que su
  primera entrada visible será la del primer cambio de estado posterior
  a este deploy, no su alta original] → Mitigación: se documenta como
  limitación conocida; no se hace backfill retroactivo porque no hay
  forma confiable de reconstruir la fecha real de cada estado pasado.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Transición válida se aplica | unit | `GestionTransitionServiceTest#shouldApplyValidTransition` |
| Transición inválida es rechazada | unit | `GestionTransitionServiceTest#shouldRejectInvalidTransition` |
| Gestión sin workflow definido rechaza cualquier transición | unit | `GestionTransitionServiceTest#shouldRejectTransitionWhenNoWorkflowDefinition` |
| Alta de gestión registra su estado inicial | unit | `GestionBitacoraServiceTest#shouldRecordHistorialOnCreate` |
| Transición válida registra el nuevo estado | unit | `GestionBitacoraServiceTest#shouldRecordHistorialOnValidTransition` |
| Archivado registra el estado archivado | unit | `GestionBitacoraServiceTest#shouldRecordHistorialOnArchive` |
| Consulta devuelve el historial completo ordenado | unit | `GestionBitacoraServiceTest#shouldReturnOrderedHistorial` |
| Archiving succeeds when the transition to Archivada is valid | unit | `GestionArchiveDebtServiceTest#shouldArchiveWhenTransitionValid` |
| Archiving is rejected when the transition to Archivada is invalid | unit | `GestionArchiveDebtServiceTest#shouldRejectArchiveWhenTransitionInvalid` |
| Transicionar y archivar de punta a punta contra la base | integration | `GestionTransitionControllerIntegrationTest`, `GestionArchiveDebtControllerIntegrationTest` |
| Consultar bitácora de punta a punta contra la base | integration | `GestionBitacoraControllerIntegrationTest` |

- New unit tests (`src/test/java/.../unit/`): `GestionTransitionServiceTest`,
  `GestionBitacoraServiceTest`; extend existing
  `GestionArchiveDebtServiceTest` with the two new scenarios above.
- New integration tests (`src/test/java/.../integration/`):
  `GestionTransitionControllerIntegrationTest`,
  `GestionBitacoraControllerIntegrationTest`; extend existing
  `GestionArchiveDebtControllerIntegrationTest` (PostgreSQL-backed,
  follow existing `ApiIntegrationTest` pattern).
- Coverage impact: additive-only new service/controller code with unit +
  integration coverage on every branch keeps the change at or above the
  JaCoCo ratchet floor (`mvn jacoco:check -pl backend-api`); no existing
  code loses coverage.

## Regression Strategy

- Existing tests affected: `GestionArchiveDebtServiceTest` and
  `GestionArchiveDebtControllerIntegrationTest` gain new scenarios but
  their existing pending-debt scenarios (issue #819) are unchanged and
  must keep passing unmodified.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none — new
  code lives in `service`/`api`, `frontend-swing` no longer exists
  (CLAUDE.md — Project Overview).

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  `gestion-cambiar-estado.spec.ts`, `gestion-bitacora.spec.ts`.
- Golden path covered: abrir una gestión → cambiar su estado a un destino
  válido → ver la nueva entrada en la bitácora → archivar la gestión
  (transición válida a "Archivada") → ver la entrada de archivado en la
  bitácora.
- Edge / error paths covered: el selector de "Cambiar estado" solo ofrece
  destinos válidos (sin opción inválida que probar en UI); intentar
  archivar una gestión cuyo estado actual no permite transición a
  "Archivada" (bloqueado, con mensaje visible).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — `Historial`, `WorkflowNode` y
  `WorkflowTransition` ya existen con todos los campos necesarios
  (proposal.md — Database).
- Deployment order / coupling: backend y frontend se despliegan juntos
  (la acción "Cambiar estado" y la vista de bitácora son consumidas
  únicamente por los endpoints nuevos); no requiere orden especial ni
  ventana de compatibilidad.
- Configuration or `.env` keys to add: none.
- Feature flag: no — el alcance es aditivo (nuevos endpoints/pantallas
  sobre acciones que ya existen) y no reemplaza ningún flujo existente.
- Smoke test after deploy (Gate 5): transicionar una gestión de prueba en
  el ambiente desplegado, confirmar que la bitácora muestra la nueva
  entrada, y archivar otra gestión de prueba confirmando que también
  queda registrada; `GET /actuator/health` sigue en verde.

## Rollback Strategy

- Revert safe: yes — el cambio es aditivo (nuevos endpoints/servicios/
  pantallas) más una validación agregada dentro de `archivar`; revertir
  el commit no afecta los endpoints CRUD existentes ni el esquema.
- Database rollback: none needed — no hay migración Flyway asociada.
- Data written under the new behavior after revert: las entradas de
  `Historial` creadas por las transiciones/archivados nuevos quedan tal
  cual en la base; un revert deja de escribir nuevas entradas pero no
  corrompe ni pierde las existentes.
- Blast radius if rollback is delayed: bajo — mientras el problema no sea
  de integridad de datos, el peor caso es que la bitácora vuelva a
  quedar vacía para gestiones nuevas, que es la situación actual sin
  este cambio.

## Migration Plan

n/a — no requiere rollout escalonado más allá del despliegue conjunto de
backend y frontend descrito en Deployment Strategy.
