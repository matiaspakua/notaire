> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Item` (`negocio/Item.java`) has `valor`, an optional `porcentaje` and free-text
`observaciones` — no way to mark an item as a discount or a surcharge, and no
mandatory reason field. `ItemController` (`api/ItemController.java`) has no
service layer: every CRUD method calls `ItemRepository` directly inside a
try/catch, unlike `PagoController`/`PresupuestoController` which delegate to a
`service`. `PagoService.calcularTotalPresupuesto` (private) always adds every
item's value (plus its `porcentaje` increase when present) — there is no
subtraction path. See proposal.md — Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Classify an `Item` as `NORMAL`, `DESCUENTO` or `RECARGO`, defaulting to
  `NORMAL` for backward compatibility.
- Require a structured, non-empty reason (`motivo`) only for `DESCUENTO`/`RECARGO`.
- Make `PagoService.calcularTotalPresupuesto` subtract `DESCUENTO` items and
  add `RECARGO` items (both fixed-value and percentage-based).
- Introduce `service/ItemService` as the place this validation and the new
  report live, correcting `ItemController`'s existing repository-direct
  deviation instead of adding validation logic to the controller.

**Non-Goals:**
- Approval/authorization workflow for large discounts — no RF/CU requires it.
- Wiring the discount/recargo report into the #820 financial summary
  endpoints — those are not implemented yet; this report is independent.
- Changing how items are added from templates/catalog (#834) — out of scope.

## Decisions

- **`tipo` as a String column with application-level validation (`NORMAL`,
  `DESCUENTO`, `RECARGO`), defaulting to `NORMAL`, not a new required field
  on every existing caller.** Alternative considered: a Java `enum` mapped
  with `@Enumerated(EnumType.STRING)` — kept as the Java-side representation
  (`TipoItem` enum) but the column allows `NULL`/legacy rows to be treated as
  `NORMAL` at read time, so every `Item` created before this migration keeps
  summing into the total exactly as it does today.
- **Validation (motivo obligatorio) lives in the new `ItemService`, not in a
  Bean Validation annotation on `Item`.** Alternative considered:
  `@AssertTrue`/cross-field `@NotBlank` on the entity — rejected, the
  constraint is conditional on `tipo` and existing project convention keeps
  business rules in `service`, not on `@Entity` classes (see
  `PagoService`/`PresupuestoService`); `ItemController` moving to
  `ItemService` also fixes its pre-existing deviation from that pattern in
  the same change, rather than adding a second, entity-level validation path.
- **`PagoService.calcularTotalPresupuesto` changes its per-item sign based on
  `item.getTipo()`, kept as a private method in `PagoService`.** Alternative
  considered: move total calculation into `ItemService` — rejected,
  `PagoService` already owns every payment/balance calculation
  (`calcularSaldoPendiente`, `calcularTotalPresupuesto`); splitting it across
  two services would require `PagoService` to call `ItemService` for a
  calculation that only touches data `PagoService` already loads via
  `Presupuesto.getItemList()`.
- **New endpoint `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos`
  in `ItemController`, backed by `ItemService`, not folded into the existing
  `GET /api/v1/items/presupuesto/{idPresupuesto}`.** Alternative considered:
  filter the existing list endpoint with a query parameter — rejected, no
  existing test asserts a fixed shape for that endpoint the way
  `PagoControllerTest#shouldGetPagosByPresupuesto` does for `/pagos`, but a
  dedicated endpoint keeps the "all items" and "discounts/surcharges only"
  concerns separate and mirrors the additive-endpoint precedent used for
  `pagos-parciales-cuotas` (#821, `/pagos/presupuesto/{id}/estado`).

## Riesgos / Trade-offs

- [Un `Item` de tipo `DESCUENTO` con `valor` mayor al total acumulado de
  ítems normales podría dejar el total del presupuesto en negativo] →
  Mitigation: no hay ningún RF que exija impedirlo hoy; se documenta como
  comportamiento esperado (un total negativo es una señal visible de un
  descuento mal cargado) y queda fuera de alcance introducir un límite (ver
  Out of Scope en proposal.md).
- [Ítems existentes en producción no tienen `tipo` — la migración debe
  garantizar que ninguno cambie su efecto en el total] → Mitigation: la
  columna se agrega con `DEFAULT 'NORMAL'`, y `calcularTotalPresupuesto`
  trata `tipo == null` igual que `tipo == NORMAL` (suma), preservando el
  comportamiento actual sin necesidad de un backfill de datos.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Crear un ítem normal sin tipo explícito | unit | `ItemServiceTest#shouldTreatItemWithoutTypeAsNormal` |
| Crear un ítem de tipo descuento | unit | `ItemServiceTest#shouldAcceptDiscountItemWithReason` |
| Crear un ítem de tipo recargo | unit | `ItemServiceTest#shouldAcceptSurchargeItemWithReason` |
| Rechazar un descuento sin motivo | unit | `ItemServiceTest#shouldRejectDiscountItemWithoutReason` |
| Rechazar un recargo sin motivo | unit | `ItemServiceTest#shouldRejectSurchargeItemWithoutReason` |
| Aceptar un ítem normal sin motivo | unit | `ItemServiceTest#shouldAcceptNormalItemWithoutReason` |
| Total con un ítem de descuento | unit | `PagoServiceTest#shouldSubtractDiscountItemFromTotal` |
| Total con un ítem de recargo | unit | `PagoServiceTest#shouldAddSurchargeItemToTotal` |
| Total sin descuentos ni recargos | unit | `PagoServiceTest#shouldSumOnlyNormalItemsWhenNoDiscountsOrSurcharges` |
| Consultar descuentos y recargos de un presupuesto con ambos | integration | `ItemControllerTest#shouldReturnDiscountsAndSurchargesForPresupuesto` |
| Consultar descuentos y recargos de un presupuesto sin ninguno | integration | `ItemControllerTest#shouldReturnEmptyListWhenNoDiscountsOrSurcharges` |
| Consultar descuentos y recargos de un presupuesto inexistente | integration | `ItemControllerTest#shouldReturnNotFoundWhenPresupuestoDoesNotExistForReport` |

- New unit tests (`src/test/java/.../unit/`): new `ItemServiceTest`; extend
  `PagoServiceTest`.
- New integration tests (`src/test/java/.../integration/`): new
  `ItemControllerTest` (does not exist today — `ItemController` currently has
  no dedicated test class).
- Coverage impact: new logic fully covered by the table above; expected to
  hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `ItemController`'s existing endpoints
  (`getAll`, `getById`, `getByPresupuesto`, `create`, `update`, `delete`) move
  from calling `ItemRepository` directly to calling `ItemService` — their
  observable HTTP behavior (status codes, response bodies) does not change,
  so any existing caller keeps passing; no existing `Item`-related test class
  was found to update (`ItemController` has none today).
- `PagoServiceTest`/`PagoServiceIntegrationTest` cases that build a
  `Presupuesto` with `Item`s of default (unset) `tipo` must keep summing to
  the same total as before — covered by
  `PagoServiceTest#shouldSumOnlyNormalItemsWhenNoDiscountsOrSurcharges`.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package does not expose `Item` or its
  total calculation.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `items-descuentos-recargos.spec.ts`.
- Golden path covered: agregar un ítem de tipo descuento con motivo a un
  presupuesto y ver que el total baja en ese valor; agregar un ítem de tipo
  recargo con motivo y ver que el total sube.
- Edge / error paths covered: intentar guardar un descuento o recargo sin
  motivo muestra un error de validación y no se guarda; un presupuesto sin
  descuentos ni recargos no muestra ninguno en el reporte.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes —
  `V{n}__add_tipo_motivo_to_items.sql` (`{n}` resolved at implementation time
  from the latest applied migration, currently `V16`; see
  `.claude/rules/database-migrations.md`), adding `tipo` (`VARCHAR`,
  `NOT NULL DEFAULT 'NORMAL'`) and `motivo` (`VARCHAR`, nullable) to `items`.
- Deployment order / coupling: single deploy; migration runs before the
  application starts (Flyway `ddl-auto=none`), backend and frontend deploy
  together since the frontend `Item` type gains the new fields.
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): crear un ítem de tipo descuento con
  motivo sobre un presupuesto de prueba, verificar que el total baja;
  consultar `GET /api/v1/items/presupuesto/{id}/descuentos-recargos` y
  verificar que lo lista; `GET /actuator/health` en verde.

## Rollback Strategy

- Revert safe: yes — reverting the application code leaves the new `tipo`/
  `motivo` columns unused (still `DEFAULT 'NORMAL'`/`NULL`) but does not
  break `Item` reads/writes on the reverted code, since it never selects
  those columns.
- Database rollback: not required for a revert; if the columns must be
  removed, a new forward migration drops them (never edit `V{n}` in place,
  per `.claude/rules/database-migrations.md`).
- Data written under the new behavior after revert: ítems de tipo descuento
  o recargo creados durante el período — su `valor` sigue siendo válido pero,
  tras el revert, vuelve a sumarse siempre en `calcularTotalPresupuesto`
  hasta que el código se reaplique.
- Blast radius if rollback is delayed: bajo — solo afecta presupuestos que
  ya tienen ítems de tipo descuento/recargo cargados.

## Open Questions

Ninguna.
