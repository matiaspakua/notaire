> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`TipoDeDocumento` (`negocio/TipoDeDocumento.java`) already models `vence`
(boolean), `diasVencimiento` (Integer) and `quienEntrega` (String), fully
round-tripped by `DtoTipoDeDocumento` and accepted/returned by
`TipoDeDocumentoController` (`POST`/`PUT /api/v1/tipo-de-documento`). Only the
frontend admin screen
(`frontend/src/app/dashboard/administracion/documentos/page.tsx`, `EMPTY:
Partial<TipoDeDocumento> = { nombre: "" }`) and the frontend type
(`frontend/src/types/index.ts` — `TipoDeDocumento` interface, currently only
`idTipoDocumento`/`nombre`) never expose these fields.

Separately, `DocumentoPresentado` (`negocio/DocumentoPresentado.java`) has its
own `vence`/`diasVencimiento`/`fechaVencimiento`/`quienEntrega` columns, but
`DocumentoPresentadoController.toEntity()` hardcodes
`entity.setVence(false)` and `entity.setQuienEntrega("")` on every create and
update, regardless of `request.tipoId()`. `TipoDeDocumentoController`
already injects `TipoDeDocumentoRepository` for `toResponse()`;
`DocumentoPresentadoController` already injects the same repository for its
own `toResponse()`, so no new dependency is needed to also use it in
`toEntity()`. See proposal.md — Objetivo for the full business motivation.

## Goals / Non-Goals

**Goals:**
- Let the admin screen set `vence`, `diasVencimiento` and `quienEntrega` on a
  `TipoDeDocumento`, for both create (CU27) and edit (CU32).
- Make `DocumentoPresentadoController.toEntity()` copy those three fields
  from the associated `TipoDeDocumento` and compute `fechaVencimiento`
  instead of hardcoding empty/false values.

**Non-Goals:**
- Building the CU42 "próximos vencimientos" list/report itself (proposal.md
  — Out of Scope) — this change only unblocks the data it depends on.
- Backfilling existing `DocumentoPresentado` rows created before this change
  (proposal.md — Out of Scope).
- Changing `PUT /api/v1/documento-presentado` to re-sync `vence`/
  `quienEntrega` when a document is edited without changing its `tipoId` —
  `toEntity()` is shared by create and update today, and update already
  re-derives all fields from the request the same way create does, so
  editing a `DocumentoPresentadoRequest` with the same `tipoId` naturally
  re-applies the same inherited values; no special-casing needed.

## Decisions

- **Inherit fields inside `DocumentoPresentadoController.toEntity()`,
  reusing the already-injected `TipoDeDocumentoRepository`, instead of a new
  service class.** `toEntity()` already has access to `tipoRepository` (used
  today only in `toResponse()`); the inheritance logic is a single
  `findById(request.tipoId())` plus three field copies and one date
  computation — below the complexity threshold where extracting a service
  would earn its own indirection (`.claude/rules/programming.md` — KISS,
  YAGNI). Alternative considered: a `DocumentoPresentadoVencimientoService` —
  rejected as premature abstraction for three field copies used from a
  single call site.
- **Compute `fechaVencimiento` in the controller using `java.util.Calendar`
  (`fechaIngreso` + `diasVencimiento` days), only when `vence` is true and
  `fechaIngreso` is present.** Matches the existing `DATE_FORMAT`-based date
  handling already in this controller; no new date library dependency.
  Alternative considered: compute it in `DocumentoPresentado` itself
  (entity-level derived field) — rejected, entities in this codebase are
  plain JPA mapped fields with no derived/computed setters elsewhere, and
  `fechaVencimiento` is a real, independently-settable column (confirmed via
  `DocumentoPresentado.findByFechaVencimiento` named query), not a
  view-projection.
- **`tipoId` not found still creates the document without inherited fields
  (current behavior for the FK itself is unchanged) — no new validation
  error introduced.** `toEntity()` today accepts any `tipoId` and only
  resolves it in `toResponse()`; validating FK existence at create time is a
  separate, pre-existing concern not touched by this change. If
  `tipoRepository.findById(request.tipoId())` returns empty (e.g. `tipoId`
  null or invalid), `vence`/`diasVencimiento`/`quienEntrega`/
  `fechaVencimiento` simply stay at their default (unset) values, same as
  today.
- **Frontend: add `vence`, `diasVencimiento`, `quienEntrega` to the
  `TipoDeDocumento` interface in `frontend/src/types/index.ts`, and to the
  admin form using the existing `FormField`/checkbox/number-input patterns
  already used elsewhere in `administracion/`.** Keeps the type as the single
  source of truth for the shape already returned by the backend DTO.
  `diasVencimiento` is shown conditionally (only when the `vence` checkbox is
  checked), matching the entity's own conditional relevance.

## Riesgos / Trade-offs

- [Un tipo de documento existente en uso (`in-use`) no puede modificarse hoy
  — el usuario no podrá cargar retroactivamente `vence`/`diasVencimiento`/
  `quienEntrega` en tipos ya usados] → Mitigación: comportamiento preexistente
  de `TipoDeDocumentoController.update()` (bloqueo con 409), documentado como
  escenario en `specs/tipo-documento-vencimiento-config/spec.md`; no es una
  regresión de este cambio. El usuario deberá crear un tipo de documento
  nuevo con los datos completos, tal como ya deben hacerlo hoy para cualquier
  otro campo.
- [Los documentos presentados creados antes de este cambio quedan con
  `vence = false` para siempre, aunque su tipo de documento tenga `vence =
  true`] → Mitigación: declarado explícitamente fuera de alcance
  (proposal.md — Out of Scope); un backfill es un cambio de datos separado,
  de menor urgencia que desbloquear la carga hacia adelante.
- [Si `diasVencimiento` es `null` en un tipo de documento con `vence = true`
  (dato inconsistente cargado antes de este cambio, o el checkbox se activó
  sin completar el número), el cálculo de `fechaVencimiento` no puede
  hacerse] → Mitigación: `fechaVencimiento` simplemente no se calcula en ese
  caso (no se lanza excepción); el formulario nuevo exige `diasVencimiento`
  cuando `vence` está marcado (spec — Alta de tipo de documento que vence),
  por lo que este caso solo puede darse con datos preexistentes.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Alta de tipo de documento que vence | integration | `TipoDeDocumentoReferentialIntegrityTest#shouldCreateTipoDeDocumentoWithVencimiento` |
| Alta de tipo de documento que no vence | integration | `TipoDeDocumentoReferentialIntegrityTest#createTipoDocumento` (existing private helper, exercised by every test in the class — already covers `vence=false` creation) |
| Modificación de vencimiento y responsable | integration | `TipoDeDocumentoReferentialIntegrityTest#shouldUpdateVencimientoAndQuienEntrega` |
| Modificación bloqueada por tipo de documento en uso | integration | `TipoDeDocumentoReferentialIntegrityTest#shouldReturn409WhenEditingTipoDocumentoInUse` (already exists, no change needed) |
| Alta de documento presentado de un tipo que vence | integration | `DocumentoPresentadoControllerTest#shouldInheritVencimientoFromTipoDeDocumento` |
| Alta de documento presentado de un tipo que no vence | integration | `DocumentoPresentadoControllerTest#shouldNotSetVencimientoWhenTipoDoesNotVence` |
| Cálculo de fecha de vencimiento | integration | `DocumentoPresentadoControllerTest#shouldComputeFechaVencimientoFromFechaIngresoAndDiasVencimiento` |
| Sin fecha de ingreso no hay fecha de vencimiento | integration | `DocumentoPresentadoControllerTest#shouldNotComputeFechaVencimientoWithoutFechaIngreso` |

- Extended integration tests (`src/test/java/.../integration/`):
  `TipoDeDocumentoReferentialIntegrityTest` (existing file — add
  `shouldCreateTipoDeDocumentoWithVencimiento` and
  `shouldUpdateVencimientoAndQuienEntrega`, following its existing
  `createTipoDocumento()`/MockMvc pattern); `DocumentoPresentadoControllerTest`
  (existing file, extended with the four new scenarios above, seeding a
  `TipoDeDocumento` with `vence = true` via `TipoDeDocumentoRepository`).
- Coverage impact: all new logic lives in two existing, already-covered
  controller methods (`toEntity()`, plus the `create`/`update` DTO mapping
  already exercised by `TipoDeDocumentoController`), so the change adds
  branches to already-covered methods rather than uncovered new classes.
  Keeps the change at or above the JaCoCo ratchet floor
  (`mvn jacoco:check -pl backend-api`).

## Regression Strategy

- Existing tests affected: `DocumentoPresentadoControllerTest`'s existing
  create/update tests (`shouldCreateDocumentoPresentadoWithDtoFields`,
  `shouldCreateDocumentoPresentadoEntregado`, `shouldUpdateDocumentoPresentado`)
  must keep passing unchanged when their fixture `TipoDeDocumento` has
  `vence = false` (the default) — the new inheritance is a no-op in that
  case. If any existing fixture there implicitly relied on `vence` always
  being `false` regardless of the tipo, that fixture must be updated to use
  a `vence = false` tipo explicitly, not have its assertion weakened.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none — the
  change stays inside `api`/`negocio` (`repository` is read-only, already
  injected); `frontend-swing` no longer exists (CLAUDE.md — Project
  Overview); `DocumentoPresentadoJpaController`/`TipoDeDocumentoJpaController`
  (legacy `jpa` package) are not touched.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  `tipo-documento-vencimiento.spec.ts` (new, or extend an existing
  `administracion/documentos` E2E spec if one already covers this screen).
- Golden path covered: crear un tipo de documento marcando "vence", con días
  de vigencia y responsable; editar un tipo de documento existente (no en
  uso) cambiando esos tres campos.
- Edge / error paths covered: crear un tipo de documento sin marcar "vence"
  (el campo de días no debe pedirse/mostrarse); intentar editar un tipo de
  documento en uso y verificar que la UI muestra el mensaje de bloqueo ya
  devuelto por el backend.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — todas las columnas usadas
  (`TipoDeDocumento.vence/diasVencimiento/quienEntrega`,
  `DocumentoPresentado.vence/diasVencimiento/fechaVencimiento/quienEntrega`)
  ya existen (proposal.md — Surface area).
- Deployment order / coupling: backend y frontend se despliegan juntos (el
  formulario de administración depende de que el backend ya acepte/devuelva
  estos campos, lo cual ya ocurre hoy); no requiere ventana de compatibilidad
  especial.
- Configuration or `.env` keys to add: none.
- Feature flag: no — el comportamiento nuevo solo se activa cuando un tipo de
  documento tiene `vence = true`, algo que antes era imposible de cargar; no
  hay comportamiento previo que preservar detrás de un flag.
- Smoke test after deploy (Gate 5): crear un tipo de documento con `vence`
  activado desde la UI, crear un documento presentado de ese tipo, y
  confirmar en la respuesta de `GET /api/v1/documento-presentado/{id}` que
  `vence`, `diasVencimiento`, `quienEntrega` y `fechaVencimiento` quedaron
  poblados; `GET /actuator/health` sigue en verde.

## Rollback Strategy

- Revert safe: yes — el cambio agrega lectura de un repositorio ya inyectado
  y asignación de campos antes vacíos/`false`; revertir el commit vuelve a
  hardcodear `vence = false` / `quienEntrega = ""`, sin afectar datos ya
  guardados.
- Database rollback: none needed — no hay migración Flyway asociada.
- Data written under the new behavior after revert: los documentos
  presentados ya creados con `vence`/`diasVencimiento`/`quienEntrega`/
  `fechaVencimiento` heredados mantienen esos valores — no se revierten
  datos, solo el comportamiento de creación futura.
- Blast radius if rollback is delayed: bajo — el peor caso es que
  "informar próximos vencimientos" (cuando exista) siga sin datos para
  documentos presentados creados durante la ventana, un problema visible y
  corregible manualmente editando el documento.

## Migration Plan

n/a — no requiere rollout escalonado más allá del despliegue conjunto de
backend y frontend descrito en Deployment Strategy.
