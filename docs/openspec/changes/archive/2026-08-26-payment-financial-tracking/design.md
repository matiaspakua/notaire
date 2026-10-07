> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See `proposal.md` — Objetivo. Three relevant facts about the current codebase
shape this design:

- The payment↔presupuesto relationship is already persisted but never
  surfaced: `Pago.getPresupuesto()` is annotated `@JsonIgnore`
  (`backend-api/.../negocio/Pago.java:118`), so every existing
  `GET /api/v1/pagos/**` endpoint (`PagoController`) omits it.
- Both saldo calculations this change needs already exist and are covered by
  tests from prior changes — they are reused here, not reimplemented:
  `PagoService.calcularSaldoPendiente(Integer idPresupuesto)`
  (`GET /api/v1/pagos/presupuesto/{id}/saldo`, from #792) and
  `GestionArchiveDebtService.calcularSaldoPendiente(Integer idGestion)`
  (`GET /api/v1/gestiones/{id}/saldo-pendiente`, from #819).
- CU47 (Consultar Pago, #200) expects a screen that, given a presupuesto,
  shows gestión número, encabezado, presupuesto número, total, saldo and the
  list of payments (número, monto, fecha, observaciones) — no endpoint
  assembles that combined payload today, and no frontend screen calls one.

## Goals / Non-Goals

**Goals:**
- Include the associated presupuesto identifier on every API response
  representing a `Pago`.
- Provide a presupuesto-level financial summary matching CU47 step 6
  (`GET /api/v1/presupuestos/{id}/resumen`).
- Provide a gestión-level aggregate financial summary — total presupuestado,
  total cobrado, saldo — reusing the existing saldo aggregation
  (`GET /api/v1/gestiones/{id}/resumen-financiero`).
- Surface this summary on the CU47 consulta-de-pagos screen without extra
  navigation.

**Non-Goals:**
- Installment plans / cuota tracking — issue #821.
- Discount/surcharge modeling with structured motive — issue #822.
- Document-level additional costs (sellos, impuestos) — issue #823.
- Presupuesto picker and saldo display inside the payment-entry form — issue
  #796 (a different screen: entry, not consultation).
- Receipt generation/printing — issue #23.
- Changing or deprecating the existing
  `GET /api/v1/pagos/presupuesto/{id}/saldo` and
  `GET /api/v1/gestiones/{id}/saldo-pendiente` endpoints — both are kept
  as-is (see Decisions).

## Decisions

- **Add a `DtoPagoResponse` rather than removing `@JsonIgnore` from `Pago`.**
  Alternative considered: drop `@JsonIgnore` on the entity directly. Rejected
  — that would expose `Presupuesto`'s full object graph (including its own
  back-references) through Jackson's default serialization, and the project
  convention is DTOs at the API boundary, not entities
  (`.claude/rules/refactoring.md` §"Request/Response DTOs" — "No entity
  objects in API layer"). The DTO carries only `idPresupuesto`, not the
  nested object.
- **Reuse `PagoService.calcularSaldoPendiente` and
  `GestionArchiveDebtService.calcularSaldoPendiente` for the saldo side of
  both new summaries, rather than writing new balance-calculation logic.**
  Alternative considered: a single SQL aggregate query per summary. Rejected
  for this change — the existing per-presupuesto and per-gestión
  calculations are already implemented and tested; a database-side rewrite
  is deferred until profiling shows the application-level loop is a problem
  (same reasoning already recorded for #819).
- **Both new endpoints coexist with, and do not replace, the existing saldo
  endpoints.** `/presupuestos/{id}/resumen` does not deprecate
  `/pagos/presupuesto/{id}/saldo`; `/gestiones/{id}/resumen-financiero` does
  not change `/gestiones/{id}/saldo-pendiente` (still used by the archive
  flow from #819). Avoids a breaking change; revisit only if a future client
  audit shows an old endpoint unused.

## Riesgos / Trade-offs

- [Assembling the gestión-level summary in application code (Java loop over
  trámites/presupuestos) instead of SQL could be slow for a gestión with many
  trámites] → Mitigation: gestiones in this domain have a small, bounded
  number of trámites (notarial case load); the same trade-off was already
  accepted for #819's aggregate saldo. Revisit with a SQL aggregate only if
  profiling shows it matters.
- [Two saldo endpoints per resource (old bare-Float, new full-summary) could
  drift if only one gets updated in a future change] → Mitigation: the new
  summary endpoints call the existing saldo methods rather than duplicating
  the calculation, so there is exactly one source of truth for the balance
  itself; only the payload shape differs.
- [Introducing `DtoPagoResponse` changes the JSON shape of existing
  `GET /api/v1/pagos/**` responses] → Mitigation: this is intentional and
  additive (adds `idPresupuesto`, keeps existing fields); covered by updating
  the existing `PagoControllerTest`/`PagoIntegrationTest` assertions (group 5
  in tasks.md) rather than being a silent breaking change.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Retrieving a single payment includes its presupuesto | unit/integration | `PagoControllerTest` |
| Listing payments by presupuesto includes the presupuesto on each entry | unit/integration | `PagoControllerTest` |
| Creating a payment returns the associated presupuesto | unit/integration | `PagoControllerTest` |
| Presupuesto with no payments | unit | `PresupuestoResumenServiceTest` |
| Presupuesto with one payment | unit | `PresupuestoResumenServiceTest` |
| Presupuesto with multiple payments | unit | `PresupuestoResumenServiceTest` |
| Requesting the summary of a non-existent presupuesto | integration | `PresupuestoResumenControllerTest` |
| Gestión with a single trámite and presupuesto | unit | `GestionResumenFinancieroServiceTest` |
| Gestión with multiple trámites and presupuestos | unit | `GestionResumenFinancieroServiceTest` |
| Gestión with no payments registered | unit | `GestionResumenFinancieroServiceTest` |

- New unit tests (`src/test/java/.../unit/`): `PresupuestoResumenServiceTest`,
  `GestionResumenFinancieroServiceTest`, covering the summary-assembly logic
  in isolation (mocked `PagoService`/`GestionArchiveDebtService`/repositories).
- New integration tests (`src/test/java/.../integration/`):
  `PresupuestoResumenControllerTest`, `GestionResumenFinancieroControllerTest`
  covering both endpoints end-to-end, plus updated `PagoControllerTest`/
  `PagoIntegrationTest` cases for the new `DtoPagoResponse` shape.
- Coverage impact: new service and controller code must stay at or above the
  JaCoCo ratchet floor (currently ~84% line / ~74% branch); no reduction is
  acceptable.

## Regression Strategy

- Existing tests affected: `PagoControllerTest`/`PagoIntegrationTest` cases
  that assert the current `List<Pago>`/`Pago` response shape must be updated
  for the new `DtoPagoResponse` shape (see tasks.md group 5). No other
  existing behavior changes — `calcularSaldoPendiente` on either service is
  called, not modified.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh` (add requests for the
  two new endpoints)
- Legacy paths at risk: none — this change only touches the modern
  `api`/`service` layers already used by `PagoController` and
  `GestionController`.

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`: a new or extended spec for
  the CU47 consulta-de-pagos screen.
- Golden path covered: select a presupuesto with payments and see gestión,
  total, saldo and the payment list rendered without additional navigation.
- Edge / error paths covered: presupuesto with no payments (saldo equals
  total, empty list); gestión with multiple presupuestos (aggregate summary
  sums correctly); unknown presupuesto id (not-found handled in the UI).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — both summaries are computed from data
  already persisted; no new column or table is introduced.
- Deployment order / coupling: backend and frontend changes ship together in
  the same release; no ordering constraint since both new endpoints are
  purely additive.
- Configuration or `.env` keys to add: none.
- Feature flag: no — additive, backward-compatible change.
- Smoke test after deploy (Gate 5): call
  `GET /api/v1/presupuestos/{id}/resumen` and
  `GET /api/v1/gestiones/{id}/resumen-financiero` against a known record and
  confirm the payload matches persisted data; walk the CU47 screen.

## Rollback Strategy

- Revert safe: yes — both endpoints and the `DtoPagoResponse` change are
  additive; no existing endpoint behavior is removed, and no schema changes
  exist to reverse.
- Database rollback: none needed — no migration was introduced.
- Data written under the new behavior after revert: none — these are
  read-only endpoints; nothing is persisted by this change that would need
  cleanup.
- Blast radius if rollback is delayed: none for other callers — existing
  `/pagos/**`, `/pagos/presupuesto/{id}/saldo` and
  `/gestiones/{id}/saldo-pendiente` endpoints are unaffected; only the new
  endpoints and the CU47 screen would be unavailable.

## Open Questions

None — both decisions previously open (DTO vs. `@JsonIgnore` removal;
whether to deprecate the existing saldo endpoints) are resolved above under
Decisions.
