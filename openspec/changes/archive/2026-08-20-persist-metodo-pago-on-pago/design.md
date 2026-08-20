> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Pago.java` (entity), `DtoPago.java` (shared DTO, still used by the legacy
`ControllerNegocio.darAltaPago`/`buscarPagosPresupuesto` path), and
`PagoController.PagoRequest` currently have no field for the payment method. The
frontend (`frontend/src/app/dashboard/pagos/page.tsx`) already sends and renders
`metodoPago`, but the backend silently drops it on every create/update. See
proposal.md — Objetivo for the full problem statement.

## Goals / Non-Goals

**Goals:**
- Add a durable `metodoPago` field to the `Pago` domain object, both REST DTOs
  (`Pago` returned directly by `PagoController`, and `DtoPago` used by the legacy
  path), and the database schema.
- Thread the field through both write paths: `PagoService.procesarPago(...)` and
  `PagoService.editarPago(...)`.

**Non-Goals:**
- Constraining `metodoPago` to a fixed set of values (enum) — see proposal.md —
  Out of Scope.
- Adding a payment receipt/comprobante — separate, out of scope.
- Changing how `frontend/src/app/dashboard/pagos/page.tsx` collects or renders
  the field — it already does both correctly; only the backend is silently
  discarding the value today.

## Decisions

- **Column type: `text`, nullable** on `pagos`. Alternative considered: a
  `VARCHAR(n)` with a length cap. Rejected because every other free-text column
  in this schema (`observaciones`, `nombre`, `estado`, ...) uses `text` with no
  length cap (`V1__initial_schema.sql`); introducing `varchar` here would be an
  unjustified inconsistency for a field with no stated length requirement.
- **Nullable, no default.** Alternative considered: `NOT NULL DEFAULT ''`.
  Rejected — every `Pago` row created before this migration has no real payment
  method; defaulting to an empty string would misrepresent that as "recorded but
  blank" instead of "never recorded", and the frontend already renders a missing
  value as `—`. Nullable preserves that distinction.
- **Field carried on the entity/DTOs as `String`, not an enum.** Matches the
  Non-Goal above and the existing free-text `Input` on the frontend form; a
  future change can introduce an enum without touching this migration if the
  business asks for it (additive).
- **Extend the existing `PagoRequest` record and `procesarPago`/`editarPago`
  signatures** rather than adding a new endpoint. Alternative considered: a
  dedicated `PATCH /pagos/{id}/metodo-pago` endpoint. Rejected — the field is
  just another attribute of a payment, not an independent resource or workflow
  step; CU15 already treats "procesar pago" and "editar pago" as the two write
  operations, and adding a third endpoint for one field would fragment that.
- **Overload `PagoService.procesarPago(...)`/`editarPago(...)` with a 5-arg
  variant taking `String metodoPago`**, keeping the existing 4-arg signatures
  as thin delegates (`metodoPago = null`). Alternative considered: modify the
  4-arg signatures in place and update every call site. Rejected — ~40 existing
  call sites across `PagoServiceTest` (both packages), `PagoServiceIntegrationTest`,
  and `PagoControllerTest` exercise balance/delete/find behavior unrelated to
  `metodoPago`; forcing them all to pass a 5th argument would contradict the
  Regression Strategy below ("done by adding, not by changing existing
  assertions") for no behavioral benefit. `PagoController` calls the 5-arg
  overload directly on the two write paths that need it.
- **Update both `Pago.getDto()`/`setAtributos()` and `DtoPago`** even though the
  REST controller returns `Pago` directly, not `DtoPago`. Alternative considered:
  updating only the REST-facing `Pago` entity and leaving `DtoPago`/
  `ControllerNegocio.darAltaPago` untouched. Rejected — `ControllerNegocio` is
  still the write path for any caller that goes through it, and leaving the
  legacy DTO out of sync would silently reintroduce the same bug it has today
  for a second entry point.

## Riesgos / Trade-offs

- [Existing `pagos` rows have no `metodo_pago` value] → Mitigation: the column
  is nullable and additive; historical rows keep displaying `—` in the UI
  exactly as they do today, with no backfill required or attempted.
- [Free-text field allows inconsistent values, e.g. "efectivo" vs "Efectivo"] →
  Mitigation: accepted for this change per the Non-Goals; tracked as a possible
  future enum migration if the business raises it. Not mitigated here.
- [`DtoPago`/`ControllerNegocio` is legacy code with limited test coverage] →
  Mitigation: `AdditionalControllersTest`-style fixtures for the legacy path
  are checked in Regression Strategy below rather than assumed safe.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Processing a payment with a payment method | unit + integration | `PagoServiceTest#shouldPersistMetodoPagoWhenProcesarPago`, `PagoIntegrationTest#shouldReturnMetodoPagoOnCreate` |
| Retrieving a payment reflects the stored payment method | integration | `PagoIntegrationTest#shouldReturnStoredMetodoPagoOnGetById` |
| Editing a payment's payment method | unit + integration | `PagoServiceTest#shouldUpdateMetodoPagoWhenEditarPago`, `PagoIntegrationTest#shouldPersistUpdatedMetodoPago` |
| Processing a payment without a payment method | unit | `PagoServiceTest#shouldAllowNullMetodoPagoOnProcesarPago` |
| Editing the payment method of a non-existent payment | unit | `PagoServiceTest#shouldThrowWhenEditingMetodoPagoOfMissingPago` |

- New unit tests (`src/test/java/.../unit/`): `PagoServiceTest` additions above
  (mocked repository).
- New integration tests (`src/test/java/.../integration/`): `PagoIntegrationTest`
  additions above (PostgreSQL, via `mvn test -Ppg-integration` and the default
  H2-backed suite where applicable).
- Coverage impact: additive getters/setters and pass-through parameters only;
  expected to hold or slightly raise the JaCoCo ratchet floor, not lower it.

## Regression Strategy

- Existing tests affected: `PagoServiceTest` and `PagoIntegrationTest` fixtures
  that construct a `Pago`/`PagoRequest` with positional/builder calls need the
  new field added to their setup — done by adding, not by changing existing
  assertions. Any `ControllerNegocio`/`DtoPago` fixture test must keep passing
  unchanged since the new field is optional there too.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: `ControllerNegocio.darAltaPago`/`buscarPagosPresupuesto`
  (via `DtoPago`) — covered explicitly above, not assumed safe by omission.

## Playwright Strategy

n/a - no UI surface: `frontend/src/app/dashboard/pagos/page.tsx` already sends
and renders `metodoPago`; this change only makes the backend stop discarding it,
so existing Playwright coverage for the pagos screen continues to exercise the
same UI flow without needing new specs.

## Deployment Strategy

- Flyway migration required: yes (`V16__add_metodo_pago_to_pagos.sql`)
- Deployment order / coupling: standard — migration and code ship together in
  the same release; the column is nullable so the migration is safe to apply
  before, during, or after the code deploy with no downtime coupling.
- Configuration or `.env` keys to add: none.
- Feature flag: no — additive, backward-compatible field.
- Smoke test after deploy (Gate 5): process a test payment with a `metodoPago`
  value against the target environment via `POST /api/v1/pagos`, then `GET` it
  back and confirm the value round-trips.

## Rollback Strategy

- Revert safe: yes — the column is additive and nullable; reverting the code
  leaves the column in place (unused) with no behavioral impact on the reverted
  version, which never reads or writes it.
- Database rollback: none needed — the column can be left in place after a code
  revert; no `R{n}` rollback script is required for an additive nullable column.
- Data written under the new behavior after revert: `metodo_pago` values already
  persisted remain in the column, simply unread by the reverted code — no data
  loss, no corruption.
- Blast radius if rollback is delayed: none — the change is additive and does
  not alter any existing response shape consumers depend on beyond one new
  optional field.

## Open Questions

None — scope, column type, and nullability are decided above.
