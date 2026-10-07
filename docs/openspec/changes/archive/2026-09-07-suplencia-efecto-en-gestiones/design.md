> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Suplencia` (`negocio/Suplencia.java`) links a `fkIdSuplente` and a
`fkIdSuplantado` `Persona` over a `fechaInicio`/`fechaFin` window, with full
CRUD via `SuplenciaController`/`SuplenciaRepository` and its own screen
(`frontend/src/app/dashboard/suplencias/page.tsx`, `useSuplencias.ts`). No
service or controller reads `Suplencia` when assigning an escribano to a
gestión: `GestionController.applyGestionFields` sets
`gestion.setFkIdPersonaEscribano(dependencies.escribano())` from the
requested `escribanoId` directly, in both
`POST /api/v1/gestiones/complete-case` and
`PUT /api/v1/gestiones/{id}/complete-case`. `SuplenciaRepository` already
exposes `findByFkIdSuplantadoIdPersona` and
`findByFechaInicioBeforeAndFechaFinAfter`, but nothing combines them.

Separately, `Persona.registroEscribano` (`negocio/Persona.java`) already
exists as a plain nullable field, round-tripped by `DtoPersona`, but
`frontend/src/app/dashboard/personas/page.tsx` has no field for it — CU48
and CU51 describe searching a persona and setting/editing this number, with
no dedicated screen location today. See `proposal.md` — Objetivo for the
full business motivation.

## Goals / Non-Goals

**Goals:**
- When a gestión is created or edited with a given escribano, check whether
  that escribano has an active `Suplencia` as `fkIdSuplantado` for the
  gestión's date, and if so, assign the suplente instead.
- Record the redirection in the gestión's `observaciones`.
- Add a place in the persona screen to set or change
  `Persona.registroEscribano` (CU48, CU51).

**Non-Goals:**
- Reassigning gestiones that already exist when a new suplencia is
  registered — only new creates/edits are affected (proposal.md — Out of
  Scope).
- Any notification (email, push) to the suplente — out of scope for this
  business finding (proposal.md — Out of Scope).
- Touching the gestión workflow/state engine
  (`WorkflowValidationService`) — this change only affects
  `fkIdPersonaEscribano`, not `fkIdEstadoDeGestion` (proposal.md — Out of
  Scope).

## Decisions

- **New `GestionSuplenciaService` instead of inlining the query in
  `GestionController`.** Keeps the business rule (which escribano is
  actually responsible for a given date) testable in isolation and
  reusable from both `createCompleteCase` and `updateCompleteCase`, matching
  the existing `service` layer convention (`GestionArchiveDebtService`,
  `GestionQueryService`) that `GestionController` already depends on.
  Alternative considered: add the check directly inside
  `applyGestionFields` — rejected, `applyGestionFields` is a static field
  mapper with no repository access today, and mixing a repository lookup
  into it would blur its single responsibility.
- **Add one combined repository method to `SuplenciaRepository`
  (`findByFkIdSuplantadoIdPersonaAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual`)**
  instead of composing the two existing finder methods in the service.
  Alternative considered: fetch all suplencias for the suplantado via
  `findByFkIdSuplantadoIdPersona` and filter dates in Java — rejected, a
  derived query lets the database do the date-range filtering directly and
  matches the existing `findByFechaInicioBeforeAndFechaFinAfter` pattern
  already in the repository.
  Use `gestion.getFechaInicio()` (already set to `new Date()` before
  `applyGestionFields` runs in `createCompleteCase`) as the date to check
  against, so a suplencia registered for a future or past date does not
  affect a gestión created today.
- **Record the redirection in `observaciones` instead of `Historial`.**
  `Historial` is never written by any real flow in this codebase — nothing
  calls `historialRepository.save(...)` today — so building this feature on
  top of it would build on dead code. `observaciones` is a plain field
  already round-tripped by `GestionDeEscritura`/`DtoGestion` and already
  shown on the gestión detail, giving an immediately visible trail without
  depending on a mechanism this codebase doesn't otherwise use. Alternative
  considered: write to `Historial` — rejected for that reason; fixing
  `Historial` itself is out of scope for this change.
- **Expose `registroEscribano` as a field in the existing persona
  edit/detail flow, not a separate screen.** CU48/CU51 both describe the
  same flow — search a `Persona`, then set/edit one field — so there's no
  business reason to duplicate `frontend/src/app/dashboard/personas` with a
  parallel "escribanos" screen; `suplencias` already reuses `Persona`
  search this way for `fkIdSuplente`/`fkIdSuplantado`. Alternative
  considered: a new `/dashboard/escribanos` screen — rejected as
  unnecessary duplication of the persona search/edit flow that already
  exists.

## Riesgos / Trade-offs

- [Una gestión creada con fecha de inicio distinta a "hoy" (si en el futuro
  se permite retroactividad) podría no coincidir con la ventana de la
  suplencia activa esperada por el usuario] → Mitigación: se usa
  explícitamente `gestion.getFechaInicio()`, no la fecha del servidor, así
  el comportamiento es consistente incluso si se agrega esa capacidad más
  adelante; hoy `createCompleteCase` siempre usa `new Date()`.
- [Si un escribano tiene dos suplencias activas superpuestas para la misma
  fecha (dato inconsistente cargado previamente), el resultado depende de
  cuál devuelva primero la consulta] → Mitigación: fuera de alcance —
  prevenir suplencias superpuestas es una validación de `CU22` que no forma
  parte de este hallazgo; se documenta como riesgo residual de baja
  probabilidad.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Creación de gestión sin suplencia activa | unit | `GestionSuplenciaServiceTest#shouldAssignRequestedEscribanoWhenNoActiveSuplencia` |
| Creación de gestión con suplencia activa | unit | `GestionSuplenciaServiceTest#shouldAssignSuplenteWhenEscribanoHasActiveSuplencia` |
| Edición de gestión con suplencia activa | integration | `GestionControllerIntegrationTest#shouldRedirectToSuplenteWhenUpdatingGestionEscribano` |
| Observaciones registran el redireccionamiento | unit | `GestionSuplenciaServiceTest#shouldRecordRedirectionInObservaciones` |
| Alta de registro de escribano | unit | `PersonaServiceTest#shouldRegisterEscribanoCredentialOnExistingPersona` |
| Modificación de registro de escribano | unit | `PersonaServiceTest#shouldUpdateEscribanoCredentialOnExistingPersona` |

- Extend/add unit tests (`src/test/java/.../unit/`):
  `GestionSuplenciaServiceTest` (new, Mockito with `SuplenciaRepository`
  mocked); `PersonaServiceTest` (extended — `registroEscribano` already
  round-trips through the existing `save()`/`update()`, so these tests
  confirm the existing path, not new service code).
- Extend integration tests (`src/test/java/.../integration/`):
  `GestionControllerIntegrationTest` with a suplencia-redirect scenario on
  `PUT .../complete-case` (PostgreSQL-backed).
- Coverage impact: `GestionSuplenciaService` is new code and must be
  covered by its own unit tests; all other changes are additive branches in
  already-covered methods. Keeps the change at or above the JaCoCo ratchet
  floor (`mvn jacoco:check -pl backend-api`).

## Regression Strategy

- Existing tests affected: none of `GestionController`'s existing
  create/update tests change behavior when no active suplencia exists — the
  new check is a no-op in that case (design.md — Decisions).
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none — the
  change stays inside `service`/`api`, `frontend-swing` no longer exists
  (CLAUDE.md — Project Overview); `SuplenciaJpaController` (legacy `jpa`
  package) is read-only via `SuplenciaRepository` in the new service, not
  extended.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  `persona-credencial-escribano.spec.ts`;
  `gestion-suplencia-redirect.spec.ts` (or extend the existing gestiones
  E2E spec if one exists for `complete-case`).
- Golden path covered: registrar el número de registro de escribano de una
  persona existente; crear una gestión con un escribano sin suplencia
  activa (comportamiento sin cambios).
- Edge / error paths covered: crear una gestión con un escribano que tiene
  una suplencia activa, y verificar en pantalla que la gestión quedó
  asignada al suplente con la observación correspondiente.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — todos los campos usados ya existen
  (proposal.md — Database).
- Deployment order / coupling: backend y frontend se despliegan juntos (el
  aviso de redirección en la UI depende del `observaciones` nuevo); no
  requiere orden especial ni ventana de compatibilidad.
- Configuration or `.env` keys to add: none.
- Feature flag: no — la redirección aplica de inmediato al desplegar; no
  hay comportamiento previo que preservar detrás de un flag (una suplencia
  activa es, por definición, una decisión de negocio ya tomada por el
  usuario al registrarla).
- Smoke test after deploy (Gate 5): registrar una suplencia de prueba
  vigente hoy, crear una gestión con el escribano suplantado y confirmar
  que queda asignada al suplente; `GET /actuator/health` sigue en verde.

## Rollback Strategy

- Revert safe: yes — el cambio agrega una consulta antes de asignar el
  escribano; revertir el commit vuelve a asignar siempre el escribano
  solicitado, sin afectar datos ya cargados.
- Database rollback: none needed — no hay migración Flyway asociada.
- Data written under the new behavior after revert: las gestiones ya
  redirigidas mantienen su `fkIdPersonaEscribano` y `observaciones` tal
  como quedaron — no se revierten datos, solo el comportamiento futuro.
- Blast radius if rollback is delayed: bajo — el peor caso es que se siga
  redirigiendo gestiones a suplentes cuando no correspondía, un dato
  visible y corregible manualmente vía edición de la gestión.

## Migration Plan

n/a — no requiere rollout escalonado más allá del despliegue conjunto de
backend y frontend descrito en Deployment Strategy.
