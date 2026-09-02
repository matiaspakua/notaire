> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`PagoService.procesarPago` (`service/PagoService.java`) already registers a
payment against a presupuesto without checking whether the amount covers the
remaining balance — `calcularSaldoPendiente` (also in `PagoService`) already
computes the numeric pending balance, and `GestionArchiveDebtService`
(`service/GestionArchiveDebtService.java`, from #819) already reuses it to
warn about debt when archiving a gestión. `PagoController` exposes
`GET /api/v1/pagos/presupuesto/{idPresupuesto}` (array of `Pago`) and
`GET /api/v1/pagos/presupuesto/{idPresupuesto}/saldo` (numeric balance), each
covered by existing tests that assert their current shape
(`PagoControllerTest#shouldGetPagosByPresupuesto` asserts `$[0].monto`,
i.e. an array). See proposal.md — Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Compute a payment status (`SIN_PAGOS` / `PARCIAL` / `SALDADO`) for a
  presupuesto from its existing pending balance.
- Expose that status through a new, additive endpoint.
- Confirm partial payments were already accepted by `procesarPago` and keep
  that behavior unchanged.

**Non-Goals:**
- Modeling a fixed-installment payment plan (RF-22 explicitly forbids it).
- Changing the response shape of any existing `Pago`-related endpoint.
- The aggregate financial summary endpoints from #820
  (`/presupuestos/{id}/resumen`, `/gestiones/{id}/resumen-financiero`) — not
  yet implemented; this change does not depend on them.

## Decisions

- **New endpoint `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado`,
  not a reshaped `GET /api/v1/pagos/presupuesto/{idPresupuesto}`.**
  Alternative considered: add the status field to the existing list
  endpoint's response — rejected, `PagoControllerTest#shouldGetPagosByPresupuesto`
  and two integration tests (`BusinessWorkflowIntegrationTest`,
  `RemainingControllersIntegrationTest`) assert that endpoint returns a bare
  JSON array; reshaping it to an object would be a breaking change to a
  contract other tests already rely on. A sibling additive endpoint mirrors
  the existing precedent of `/pagos/presupuesto/{id}/saldo` next to the list
  endpoint.
- **`calcularEstadoPago` lives in `PagoService`, reusing `calcularSaldoPendiente`
  and a new `pagoRepository.countByFkIdPresupuestoIdPresupuesto`.**
  Alternative considered: compute the status in `PresupuestoService` —
  rejected, `PagoService` already owns every payment/balance calculation
  (`calcularSaldoPendiente`, `calcularTotalPresupuesto`); keeping the status
  next to them avoids splitting payment logic across two services.
  Alternative considered: derive "sin pagos" from `saldoPendiente == total`
  instead of counting payments — rejected, a presupuesto could reach that
  exact balance after a payment of zero net effect (edit that reduces a
  prior payment to 0 is not possible today, but a presupuesto whose total is
  0 would be indistinguishable); counting registered payments is the direct,
  unambiguous signal.
- **No new entity or column.** The status is computed on read from `Pago`
  and `Presupuesto` already in `main`, consistent with `calcularSaldoPendiente`
  already being computed rather than persisted.

## Riesgos / Trade-offs

- [Concurrencia: dos pagos registrados casi simultáneamente sobre el mismo
  presupuesto podrían hacer que una consulta de estado lea un saldo
  transitorio] → Mitigation: mismo riesgo que ya asume
  `calcularSaldoPendiente` hoy en producción (usado por
  `GestionArchiveDebtService`); este cambio no lo introduce ni lo agrava, y
  no se aborda aquí (fuera de alcance — ver Out of Scope en proposal.md).
- [Si en el futuro se persiste un estado de pago en `Presupuesto` (por
  ejemplo para reportes históricos), este diseño computado quedaría
  duplicado] → Mitigation: no se persiste nada en este cambio; si surge esa
  necesidad, se evalúa entonces sin migrar datos retroactivamente.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Pago que cubre el total del presupuesto | unit | `PagoServiceTest#shouldAcceptPaymentCoveringFullTotal` |
| Pago parcial que no cubre el total | unit | `PagoServiceTest#shouldAcceptPartialPaymentBelowSaldo` |
| Secuencia de pagos parciales que suman el total | integration | `PagoServiceIntegrationTest#shouldAcceptSequenceOfPartialPaymentsReachingTotal` |
| Presupuesto sin pagos registrados | unit | `PagoServiceTest#shouldReturnSinPagosStatusWhenNoPaymentsRegistered` |
| Presupuesto parcialmente abonado | unit | `PagoServiceTest#shouldReturnParcialStatusWhenBalancePending` |
| Presupuesto saldado | unit | `PagoServiceTest#shouldReturnSaldadoStatusWhenBalanceZero` |
| Consultar el estado de un presupuesto parcialmente abonado | integration | `PagoControllerTest#shouldReturnEstadoParcialForPresupuesto` |
| Consultar el estado de un presupuesto saldado | integration | `PagoControllerTest#shouldReturnEstadoSaldadoForPresupuesto` |
| Consultar el estado de un presupuesto inexistente | integration | `PagoControllerTest#shouldReturnNotFoundWhenPresupuestoDoesNotExist` |

- New unit tests (`src/test/java/.../unit/`): extend `PagoServiceTest`.
- New/extended integration tests (`src/test/java/.../integration/`): extend
  `PagoServiceIntegrationTest` and `PagoControllerTest`.
- Coverage impact: new logic fully covered by the table above; expected to
  hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: none require modification —
  `PagoControllerTest#shouldGetPagosByPresupuesto`,
  `BusinessWorkflowIntegrationTest`, `RemainingControllersIntegrationTest`
  and `UseCaseDomainsIntegrationTest` keep passing unchanged because the
  existing list/saldo endpoints are not touched.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package does not expose payment status.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `pagos-estado.spec.ts`.
- Golden path covered: registrar un pago parcial sobre un presupuesto y ver
  la etiqueta "parcialmente abonado"; registrar el pago restante y ver que
  cambia a "saldado".
- Edge / error paths covered: presupuesto recién creado sin pagos muestra
  "sin pagos"; consultar el estado de un presupuesto inexistente no rompe
  la pantalla.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — no new columns or tables.
- Deployment order / coupling: single deploy; the new endpoint is additive
  and does not require a maintenance window.
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): registrar un pago parcial sobre un
  presupuesto de prueba, consultar `GET
  /api/v1/pagos/presupuesto/{id}/estado` y verificar "parcialmente
  abonado"; registrar el pago restante y verificar "saldado"; `GET
  /actuator/health` en verde.

## Rollback Strategy

- Revert safe: yes — no schema changes; reverting the code removes the new
  endpoint and `calcularEstadoPago`, leaving `procesarPago` and the existing
  endpoints exactly as they are today.
- Database rollback: not applicable — no migration to revert.
- Data written under the new behavior after revert: pagos parciales
  registrados durante el período — siguen siendo pagos válidos por el resto
  del sistema tras el revert; solo deja de estar disponible la consulta de
  estado computado.
- Blast radius if rollback is delayed: bajo — ningún otro cambio depende de
  `calcularEstadoPago` o del nuevo endpoint.

## Open Questions

Ninguna.
