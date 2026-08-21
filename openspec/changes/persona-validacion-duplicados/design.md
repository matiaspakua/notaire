> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Persona` (`negocio/Persona.java`) already has `numeroIdentificacion` and
a required `fkIdTipoIdentificacion`. `PersonaRepository` already exposes
`findByNumeroIdentificacion(String)`, used today only by
`PersonaService.buscar()` for search. `PersonaService.save()` — reached
by `POST /api/v1/personas` and `PUT /api/v1/personas/{id}` via
`PersonaController` — calls `personaRepository.save(entity)` directly
with no duplicate check. The frontend form
(`frontend/src/app/dashboard/personas/page.tsx`) has no client-side
check either. See `proposal.md` — Objetivo for the full business
motivation.

## Goals / Non-Goals

**Goals:**
- Make `PersonaService.save()` reject a create when another `Persona`
  already has the same `TipoIdentificacion` + `numeroIdentificacion`.
- Make the same check apply on update, excluding the persona being
  edited from the comparison.
- Surface the rejection through `PersonaController` as `409 Conflict`
  with the existing persona's ID, and show it clearly in the frontend
  form.

**Non-Goals:**
- Merging already-duplicated `Persona` records in the database — this
  change prevents new duplicates, it does not clean up existing ones
  (proposal.md — Out of Scope).
- A database-level unique constraint (Flyway) — kept as an
  application-level check so the error response can carry the existing
  persona's ID (proposal.md — Out of Scope, Decisions below).

## Decisions

- **Application-level validation in `PersonaService.save()` instead of a
  database unique constraint.** A unique constraint would only produce a
  generic constraint-violation error; validating in the service lets the
  response include the existing persona's ID so the UI can link to it
  (proposal.md — What Changes). Alternative considered: add a Flyway
  migration with a unique index on `(fk_id_tipo_identificacion,
  numero_identificacion)` — rejected for this change because it can't by
  itself carry the "which persona already has it" context the UX needs;
  it remains a reasonable defense-in-depth addition for a future change,
  noted as a residual risk below.
- **Reuse `PersonaRepository.findByNumeroIdentificacion` instead of
  adding a new repository method.** It already exists and is exercised
  by `PersonaService.buscar()`; the duplicate check only needs to add
  the `TipoIdentificacion` comparison and, on update, exclude the
  current persona's own ID. Alternative considered: add a new
  `findByFkIdTipoIdentificacionAndNumeroIdentificacion` derived query —
  considered but `findByNumeroIdentificacion` already narrows enough
  candidates in practice (numero_identificacion is rarely reused across
  tipos), and reusing it avoids growing the repository surface for a
  single extra comparison done in the service.
- **`409 Conflict` instead of `400 Bad Request` for the rejection.** A
  duplicate is a state conflict with existing data, not a malformed
  request — matches the existing `409` documented on
  `PersonaController.createPersona`'s `@ApiResponses`. Alternative
  considered: `400` — rejected as inconsistent with the controller's
  existing documented contract.

## Riesgos / Trade-offs

- [Sin constraint de base de datos, dos inserciones concurrentes con el
  mismo documento podrían ambas pasar la validación de aplicación antes
  de que la primera confirme] → Mitigación: se documenta como riesgo
  residual de baja probabilidad (altas de persona no son de alta
  concurrencia); un constraint único a nivel de base queda como mejora
  futura si se detecta el caso en producción (ver Decisions).
- [Los duplicados ya existentes en la base no se corrigen con este
  cambio] → Mitigación: fuera de alcance explícito (proposal.md — Out of
  Scope); no bloquea el valor de prevenir nuevos duplicados.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Alta exitosa con documento no registrado | unit | `PersonaServiceTest#shouldCreatePersonaWhenDocumentNotRegistered` |
| Rechazo de alta con documento ya registrado | unit | `PersonaServiceTest#shouldRejectCreateWhenDocumentAlreadyRegistered` |
| Edición exitosa sin cambiar el documento | unit | `PersonaServiceTest#shouldUpdatePersonaWithoutChangingDocument` |
| Rechazo de edición hacia un documento de otra persona | unit | `PersonaServiceTest#shouldRejectUpdateWhenDocumentBelongsToAnotherPersona` |
| Alta y edición de punta a punta contra la base | integration | `PersonaControllerIntegrationTest` (extendida) |

- Extend existing unit test (`src/test/java/.../unit/`):
  `PersonaServiceTest` with the four scenarios above (Mockito, repository
  mocked).
- Extend existing integration test (`src/test/java/.../integration/`):
  `PersonaControllerIntegrationTest` with create/update duplicate
  scenarios (PostgreSQL-backed, follow existing `ApiIntegrationTest`
  pattern).
- Coverage impact: additive branches in existing, already-covered
  service/controller methods keep the change at or above the JaCoCo
  ratchet floor (`mvn jacoco:check -pl backend-api`); no existing code
  loses coverage.

## Regression Strategy

- Existing tests affected: `PersonaServiceTest` and
  `PersonaControllerIntegrationTest` gain new scenarios; their existing
  scenarios (non-duplicate create/update/search) must keep passing
  unmodified.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none — the
  change stays inside `service`/`api`, `frontend-swing` no longer exists
  (CLAUDE.md — Project Overview).

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  `persona-validacion-duplicados.spec.ts`.
- Golden path covered: crear una persona con un documento nuevo → editar
  esa misma persona sin cambiar su documento.
- Edge / error paths covered: crear una persona con el documento de una
  ya existente (bloqueado, mensaje visible con enlace a la ficha
  existente); editar una persona hacia el documento de otra (bloqueado).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — `numero_identificacion` y
  `fk_id_tipo_identificacion` ya existen (proposal.md — Database).
- Deployment order / coupling: backend y frontend se despliegan juntos
  (el mensaje de error nuevo es interpretado únicamente por el formulario
  actualizado); no requiere orden especial ni ventana de compatibilidad.
- Configuration or `.env` keys to add: none.
- Feature flag: no — la validación se aplica de inmediato al desplegar,
  no hay comportamiento previo que preservar detrás de un flag.
- Smoke test after deploy (Gate 5): intentar crear una persona de prueba
  con un documento ya cargado en el ambiente desplegado y confirmar que
  se rechaza; `GET /actuator/health` sigue en verde.

## Rollback Strategy

- Revert safe: yes — el cambio es una validación adicional dentro de un
  método existente; revertir el commit vuelve a permitir duplicados sin
  afectar el esquema ni los datos ya cargados.
- Database rollback: none needed — no hay migración Flyway asociada.
- Data written under the new behavior after revert: ninguna — el cambio
  solo rechaza escrituras, no crea datos nuevos que revertir.
- Blast radius if rollback is delayed: bajo — el peor caso es que se
  vuelva a poder cargar un duplicado, que es la situación actual sin
  este cambio.

## Migration Plan

n/a — no requiere rollout escalonado más allá del despliegue conjunto de
backend y frontend descrito en Deployment Strategy.
