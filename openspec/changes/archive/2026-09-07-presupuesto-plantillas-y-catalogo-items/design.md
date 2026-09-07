> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`PlantillaPresupuesto` (`negocio/PlantillaPresupuesto.java`) already links
a `TipoDeTramite` to a `Concepto` (nombre, valor, porcentaje) and is
already administrable end-to-end at
`frontend/src/app/dashboard/administracion/plantillas`. `Item`
(`negocio/Item.java`) already models a reusable catalog entry (nombre,
valor, porcentaje, observaciones) with its own CRUD at
`frontend/src/app/dashboard/items` and `ItemController`, and can already
be linked to a `Presupuesto` via the nullable `fk_id_presupuesto` column.
Neither is reachable from `frontend/src/app/dashboard/presupuestos`,
where the only price input is `monto` (mapped to
`Presupuesto.montoInmueble`, confirmed via
`@JsonProperty("monto") getMontoInmueble()` — the property of the
inmueble, unrelated to trámite cost). See `proposal.md` — Objetivo for
the full business motivation.

## Goals / Non-Goals

**Goals:**
- Add a `PresupuestoPlantillaService` that, given a presupuesto and a
  `TipoDeTramite`, copies that tipo's `PlantillaPresupuesto` conceptos
  into new `Item`s attached to the presupuesto.
- Add a `PresupuestoCatalogoItemsService` that, given a presupuesto and a
  list of existing `Item` IDs, creates presupuesto-attached copies of
  those catalog items.
- Add the frontend affordances (tipo de trámite selector + "cargar de
  plantilla" action, catalog item picker, ítems breakdown table) to
  `frontend/src/app/dashboard/presupuestos`.

**Non-Goals:**
- Any change to `PlantillaPresupuesto` or `Item` CRUD, or their existing
  admin screens (CU39/CU55/CU49/CU71 already cover them).
- Recalculating already-loaded ítems when the source plantilla changes
  later (proposal.md — Reglas de negocio: explicit snapshot behavior).
- Changing what `monto`/`montoInmueble` means or how it's computed.

## Decisions

- **New `service` classes (`PresupuestoPlantillaService`,
  `PresupuestoCatalogoItemsService`) instead of adding methods to
  `PresupuestoController` directly.** Keeps the "copy conceptos as items"
  and "copy catalog items" logic testable in isolation and reusable if a
  future screen needs the same operation. Alternative considered: inline
  the copy logic in `PresupuestoController` — rejected because it would
  mix orchestration (HTTP) with the copying business rule.
- **Items loaded from a plantilla or catalog are copies (new `Item` rows
  with `fk_id_presupuesto` set), not references to the original
  `Concepto`/`Item`.** Matches the existing `Item` schema (it already has
  its own nombre/valor/porcentaje columns, not a foreign key to
  `Concepto`), and matches the explicit "snapshot, no recalculation" rule
  in proposal.md. Alternative considered: store a reference to the
  originating `Concepto`/`Item` and resolve values at read time —
  rejected because it would require a schema change and contradicts the
  business rule that a presupuesto's price is fixed at the moment it is
  quoted.
- **Two action endpoints (`POST .../items-desde-plantilla`,
  `POST .../items-desde-catalogo`) instead of a single generic "add
  items" endpoint.** Each has a distinct input shape (a `tipoTramiteId`
  vs. a list of `idItem`) and a distinct rejection reason, matching the
  scenario-level acceptance criteria in the delta specs. Alternative
  considered: one endpoint with a discriminator field — rejected as
  unnecessary indirection for two clearly distinct actions.

## Riesgos / Trade-offs

- [Si un tipo de trámite no tiene `PlantillaPresupuesto` configurada, el
  usuario no tiene de dónde partir] → Mitigación: el endpoint rechaza
  explícitamente con un mensaje claro (ver spec — Rechazo cuando el tipo
  de trámite no tiene plantilla); la administración de plantillas
  (CU39) ya permite crearla, fuera de alcance de este cambio.
- [Copiar ítems como snapshot significa que un error de precio en la
  plantilla, corregido después, no se propaga a presupuestos ya
  cargados] → Mitigación: es el comportamiento de negocio esperado
  (proposal.md — Reglas de negocio); se documenta explícitamente en la
  spec y en CU39 (Documentation Impact), no es un defecto de esta
  implementación.
- [Agregar muchos ítems del catálogo en una sola operación podría crear
  un número grande de filas `Item` en una sola transacción] → Mitigación:
  el endpoint recibe una lista de IDs y valida cada uno antes de copiar
  (spec — Rechazo al referenciar un ítem de catálogo inexistente);
  tamaño de lista no requiere paginación para el volumen esperado de un
  presupuesto (decenas de ítems, no miles).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Carga exitosa desde una plantilla existente | unit | `PresupuestoPlantillaServiceTest#shouldLoadItemsFromPlantilla` |
| Ítems cargados no se recalculan si la plantilla cambia después | unit | `PresupuestoPlantillaServiceTest#shouldNotRecalculateLoadedItemsWhenPlantillaChanges` |
| Rechazo cuando el tipo de trámite no tiene plantilla | unit | `PresupuestoPlantillaServiceTest#shouldRejectWhenNoPlantillaForTipoTramite` |
| Agregado exitoso de un ítem del catálogo | unit | `PresupuestoCatalogoItemsServiceTest#shouldAddSingleCatalogItem` |
| Agregado de varios ítems del catálogo en una sola operación | unit | `PresupuestoCatalogoItemsServiceTest#shouldAddMultipleCatalogItems` |
| Rechazo al referenciar un ítem de catálogo inexistente | unit | `PresupuestoCatalogoItemsServiceTest#shouldRejectUnknownCatalogItem` |
| Ambos flujos de punta a punta contra la base | integration | `PresupuestoPlantillaControllerIntegrationTest`, `PresupuestoCatalogoItemsControllerIntegrationTest` |

- New unit tests (`src/test/java/.../unit/`):
  `PresupuestoPlantillaServiceTest`, `PresupuestoCatalogoItemsServiceTest`
  (Mockito, repositories mocked).
- New integration tests (`src/test/java/.../integration/`):
  `PresupuestoPlantillaControllerIntegrationTest`,
  `PresupuestoCatalogoItemsControllerIntegrationTest` (PostgreSQL-backed,
  follow existing `ApiIntegrationTest` pattern).
- Coverage impact: additive-only new service/controller code with unit +
  integration coverage on every branch keeps the change at or above the
  JaCoCo ratchet floor (`mvn jacoco:check -pl backend-api`); no existing
  code loses coverage.

## Regression Strategy

- Existing tests affected: none require behavior changes —
  `PlantillaPresupuestoController`/`ItemController` and their existing
  test suites are untouched; `PresupuestoController`'s existing CRUD
  tests keep passing unmodified.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none — new
  code lives in `service`/`api`, `frontend-swing` no longer exists
  (CLAUDE.md — Project Overview).

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  `presupuesto-plantilla.spec.ts`, `presupuesto-catalogo-items.spec.ts`.
- Golden path covered: crear un presupuesto → elegir tipo de trámite →
  cargar ítems de su plantilla → agregar un ítem adicional del catálogo →
  ver el desglose con subtotal.
- Edge / error paths covered: elegir un tipo de trámite sin plantilla
  (acción de carga deshabilitada o error visible); intentar agregar un
  ítem de catálogo que ya no existe (bloqueado, con mensaje visible).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — `Item.fk_id_presupuesto` ya existe y ya
  es nullable (proposal.md — Database).
- Deployment order / coupling: backend y frontend se despliegan juntos
  (los nuevos endpoints son consumidos únicamente por la pantalla de
  presupuestos); no requiere orden especial ni ventana de compatibilidad.
- Configuration or `.env` keys to add: none.
- Feature flag: no — el alcance es aditivo (nuevas acciones sobre una
  pantalla existente) y no reemplaza ningún flujo existente.
- Smoke test after deploy (Gate 5): crear un presupuesto de prueba,
  cargar los ítems de la plantilla de un tipo de trámite existente, y
  agregar un ítem del catálogo; confirmar que `GET /actuator/health`
  sigue en verde.

## Rollback Strategy

- Revert safe: yes — el cambio es aditivo (nuevos endpoints/servicios/
  acciones de UI); revertir el commit no afecta los endpoints CRUD
  existentes ni el esquema.
- Database rollback: none needed — no hay migración Flyway asociada.
- Data written under the new behavior after revert: los `Item`s creados
  como copia de una plantilla o de un ítem del catálogo quedan tal cual
  en la base (mismos campos que ya existían); un revert deja de exponer
  las acciones de carga pero no corrompe ni pierde esos datos.
- Blast radius if rollback is delayed: bajo — mientras el problema no sea
  de integridad de datos, el peor caso es que la carga de presupuestos
  siga siendo manual, que es la situación actual sin este cambio.

## Migration Plan

n/a — no requiere rollout escalonado más allá del despliegue conjunto de
backend y frontend descrito en Deployment Strategy.
