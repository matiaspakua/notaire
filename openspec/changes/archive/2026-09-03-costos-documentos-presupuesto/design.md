> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`DocumentoPresentado` (`negocio/DocumentoPresentado.java`) already has
`importeAPagar` (Float) and `fechaPago` — the field exists, it is simply
never read by any total/balance calculation (confirmed by grepping the
codebase: no reference outside the entity and its DTO). The relationship
path already exists to reach it from a presupuesto:
`Presupuesto.tramiteList` (`OneToMany`, mappedBy `fkIdPresupuesto`) →
`Tramite.documentoPresentadoList` (`OneToMany`, mappedBy `fkIdTramite`) →
`DocumentoPresentado.importeAPagar`. `PagoService.calcularTotalPresupuesto`
(private) only iterates `Presupuesto.itemList` today.

`PlantillaPresupuesto` (`negocio/PlantillaPresupuesto.java`) already models,
per `TipoDeTramite`, a list of `Concepto`s via a composite key
(`PlantillaPresupuestoPK`: `fk_id_tipo_tramite` + `fk_id_concepto`) — but
`TipoDeDocumento` has no cost field, and no structure links a
`TipoDeTramite` to a `TipoDeDocumento` with an expected cost.
`PlantillaPresupuestoController` still delegates to the legacy
`jpa.PlantillaPresupuestoJpaController`; per `CLAUDE.md`, new data access
must use `repository`, not `jpa` — this change does not extend that legacy
controller, it introduces a new, modern-pattern sibling. See proposal.md —
Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Make the existing `DocumentoPresentado.importeAPagar` count toward its
  presupuesto's total/balance.
- Let a `TipoDeTramite`'s presupuesto template define an expected fixed or
  variable cost per `TipoDeDocumento`.

**Non-Goals:**
- Adding a new cost field to `DocumentoPresentado`/`TipoDeDocumento` —
  `importeAPagar` already exists and is sufficient for this change.
- Auto-applying the template's expected cost to a real `DocumentoPresentado`
  when it is created — the template only defines the expected amount
  (administration, CU39); wiring it into document creation is a future,
  separate change (see Out of Scope in proposal.md).
- The #820 gestión-level financial summary — not implemented; this change's
  total/balance impact is picked up automatically once #820 exists, since
  #820 already aggregates per presupuesto (`GestionArchiveDebtService`).

## Decisions

- **`PagoService.calcularTotalPresupuesto` sums `importeAPagar` across
  `presupuesto.getTramiteList()[].getDocumentoPresentadoList()`, staying a
  private method in `PagoService`.** Alternative considered: expose it as a
  separate `PagoService.calcularCostoDocumentos` and have the caller add it
  — rejected, every other total component (items, percentage) is already
  folded into one private method; splitting it would force every caller of
  `calcularTotalPresupuesto` (`calcularSaldoPendiente`, `procesarPago`) to
  remember to add a second term.
- **New `PlantillaCostoDocumento` entity with composite key
  (`fk_id_tipo_tramite` + `fk_id_tipo_documento`), mirroring
  `PlantillaPresupuesto`/`PlantillaPresupuestoPK`.** Alternative considered:
  add `fk_id_tipo_documento` as an optional column on `PlantillaPresupuesto`
  itself — rejected, `PlantillaPresupuesto` is keyed by `Concepto`, a
  different domain concept from `TipoDeDocumento`; overloading its PK would
  make a `PlantillaPresupuesto` row ambiguously about a concept or a
  document type. A sibling entity keeps both concerns independently
  queryable, consistent with how `Item` (CU71) and `PlantillaPresupuesto`
  (CU39) are already two independent entities that both feed into the same
  presupuesto.
- **New `service/PlantillaCostoDocumentoService` and
  `repository/PlantillaCostoDocumentoRepository`, not an extension of the
  legacy `jpa.PlantillaPresupuestoJpaController`.** Alternative considered:
  add methods to the existing `PlantillaPresupuestoController`/
  `PlantillaPresupuestoJpaController` — rejected, that controller already
  delegates to the legacy `jpa` package (`CLAUDE.md`: "New code should use
  `repository`, not `jpa`"); this change does not extend that deviation, it
  follows the modern service/repository pattern already used by
  `PagoService`/`ItemService` (#822).
- **`monto_fijo` and `porcentaje_variable` as two nullable columns on
  `PlantillaCostoDocumento`, exactly one populated per row.** Alternative
  considered: a single `monto` column with a `tipo` discriminator
  (`FIJO`/`VARIABLE`) — rejected, `Item` already establishes the
  `valor`/`porcentaje` two-column shape for the same fixed-vs-percentage
  concept; reusing that shape keeps the codebase's cost-modeling pattern
  consistent instead of introducing a second one.

## Riesgos / Trade-offs

- [Sumar `importeAPagar` a un presupuesto ya existente puede cambiar su
  saldo pendiente retroactivamente si ya tenía documentos con costo
  cargado] → Mitigation: hasta hoy ese campo nunca se sumaba, por lo que
  ningún saldo mostrado hasta ahora lo incluía; se documenta explícitamente
  en el CHANGELOG como un cambio de cálculo, no un bug oculto, y no se
  aplica ningún backfill de datos (el campo ya estaba ahí).
- [Una `PlantillaCostoDocumento` con `monto_fijo` y `porcentaje_variable`
  ambos nulos o ambos cargados sería ambigua] → Mitigation: `service`
  valida que exactamente uno de los dos esté presente antes de guardar,
  igual que el patrón ya usado por `Item.valor`/`Item.porcentaje` (uno
  obligatorio, el otro opcional pero mutuamente significativo).

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Presupuesto con un documento con costo asociado | unit | `PagoServiceTest#shouldIncludeDocumentCostInPresupuestoTotal` |
| Presupuesto con varios documentos con costo | unit | `PagoServiceTest#shouldSumMultipleDocumentCostsInPresupuestoTotal` |
| Presupuesto sin documentos con costo | unit | `PagoServiceTest#shouldNotChangeTotalWhenNoDocumentsHaveCost` |
| Definir un gasto fijo por tipo de documento | unit | `PlantillaCostoDocumentoServiceTest#shouldAcceptFixedCostForTipoDocumento` |
| Definir un gasto variable por tipo de documento | unit | `PlantillaCostoDocumentoServiceTest#shouldAcceptVariableCostForTipoDocumento` |
| Consultar los gastos por tipo de documento de una plantilla | integration | `PlantillaCostoDocumentoControllerTest#shouldReturnCostosByTipoTramite` |

- New unit tests (`src/test/java/.../unit/`): extend `PagoServiceTest`; new
  `PlantillaCostoDocumentoServiceTest`.
- New integration tests (`src/test/java/.../integration/`): new
  `PlantillaCostoDocumentoControllerTest`.
- Coverage impact: new logic fully covered by the table above; expected to
  hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `PagoServiceTest`/`PagoServiceIntegrationTest`
  cases that build a `Presupuesto` without `Tramite`s or without
  `DocumentoPresentado`s must keep computing the same total as before —
  covered by `PagoServiceTest#shouldNotChangeTotalWhenNoDocumentsHaveCost`.
- `PlantillaPresupuestoController`/`PlantillaPresupuestoJpaController` are
  not touched — no regression risk there.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: `jpa.PlantillaPresupuestoJpaController` is not
  extended or modified by this change.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `plantilla-costos-documento.spec.ts`.
- Golden path covered: definir un gasto fijo por tipo de documento en la
  plantilla de un tipo de trámite y verlo listado; definir un gasto
  variable y verlo listado.
- Edge / error paths covered: una plantilla de tipo de trámite sin gastos
  por documento definidos muestra una lista vacía sin romper la pantalla.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes —
  `V{n}__create_plantilla_costos_documento.sql` (`{n}` resolved at
  implementation time from the latest applied migration, currently `V16`;
  see `.claude/rules/database-migrations.md`), creating
  `plantilla_costos_documento` (`fk_id_tipo_tramite`, `fk_id_tipo_documento`
  composite PK, `monto_fijo` nullable, `porcentaje_variable` nullable).
- Deployment order / coupling: single deploy; migration runs before the
  application starts (Flyway `ddl-auto=none`).
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): cargar un documento con `importeAPagar`
  en un trámite de un presupuesto de prueba y verificar que
  `GET /api/v1/pagos/presupuesto/{id}/saldo` lo incluye; definir un gasto
  fijo por tipo de documento en una plantilla y consultarlo; `GET
  /actuator/health` en verde.

## Rollback Strategy

- Revert safe: yes — reverting the application code stops reading
  `importeAPagar` into the total (back to today's behavior) and removes the
  new endpoints; `PlantillaCostoDocumento` rows written during the period
  remain in the database, unused but harmless.
- Database rollback: not required for a revert; if the table must be
  removed, a new forward migration drops it (never edit `V{n}` in place).
- Data written under the new behavior after revert: documentos con
  `importeAPagar` cargado durante el período — el campo sigue siendo válido
  pero, tras el revert, deja de sumarse al total hasta que el código se
  reaplique.
- Blast radius if rollback is delayed: medio — cualquier presupuesto con
  documentos con costo verá su saldo pendiente recalculado hacia arriba
  hasta el revert.

## Open Questions

Ninguna.
