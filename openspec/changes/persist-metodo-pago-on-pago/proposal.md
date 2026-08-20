# Persist metodoPago on Pago

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #792 |
| Use Case | CU15 — Procesar pago (`docs/01-business/02-use-cases/03_CU - Casos de Uso/CU15 – Procesar pago.md`) |
| Branch | `fix/792_persist-metodo-pago-on-pago` |
| Gate 1 status | pending |

## Objetivo

The payment form already collects and displays a `metodoPago` (Efectivo/Transferencia/etc.)
value, but the backend has no field to hold it — `Pago`, `DtoPago`, `PagoController`, and the
schema silently discard it on every create/update. Staff believe the payment method is
recorded because the UI shows it back to them (`p.metodoPago ?? "—"`), but it is never
persisted, so the value shown after a page reload is never real data.

## What Changes

- Add a `metodo_pago` column to the `pagos` table via a new Flyway migration.
- Add a `metodoPago` field (with getter/setter) to `negocio/Pago.java`, mapped end-to-end
  through `getDto()`/`setAtributos()` to `DtoPago` (used by the legacy `ControllerNegocio`
  path as well as the REST path).
- Add `metodoPago` to `notaire-shared/dto/DtoPago.java`.
- Add `metodoPago` to `PagoController.PagoRequest` and thread it through
  `PagoService.procesarPago(...)` and `PagoService.editarPago(...)` so create and update both
  persist it.
- No frontend change is required — `frontend/src/app/dashboard/pagos/page.tsx` already
  sends/reads `metodoPago`; it will simply start round-tripping real data.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The payment method used to settle a `Pago` must be recorded and retrievable with the payment. | CU15 — Procesar pago | Made explicit (was implied by the existing UI field, never enforced end-to-end) |

## Capabilities

### New Capabilities
- `pagos`: Payment processing for a `Presupuesto` (CU15) — creating, editing, and querying
  `Pago` records, including the payment method used.

### Modified Capabilities
_None — no existing spec file covers `pagos` yet; this is the first spec for it._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `negocio/Pago.java`, `api/PagoController.java` (`PagoRequest`), `service/PagoService.java`, Flyway migration |
| `frontend` | no | Already sends/reads `metodoPago`; no code change needed |
| `frontend-swing` | no | Removed module, not applicable |
| `notaire-shared` | yes | `dto/DtoPago.java` gains the `metodoPago` field |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Pago` (new `metodoPago` field)
- Endpoints: `POST /api/v1/pagos`, `POST /api/v1/pagos/params`, `PUT /api/v1/pagos/{id}`
  (existing endpoints, extended request/response shape — not new endpoints)
- Database (Flyway `V16`): new migration adding `metodo_pago VARCHAR` (nullable, additive) to
  `pagos`
- Configuration / `.env`: none
- Dependencies: none
- Not BREAKING: the new field is additive and nullable; existing clients that omit it keep
  working exactly as before.

### Architecture review

Follows the existing architecture: change lives in `service`/`api`/`negocio` (REST path) plus
the legacy `negocio.ControllerNegocio`/`DtoPago` path it already shares with `Pago.getDto()`/
`setAtributos()`, and uses Flyway as the single source of truth for the schema change. No new
architectural pattern is introduced — no ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/01-business/02-use-cases/03_CU - Casos de Uso/CU15 – Procesar pago.md` | Confirm/update the flow description to state the payment method is now persisted with the payment |
| `CHANGELOG.md` | Add an `[Unreleased]` entry: payment method is now persisted and returned with each `Pago` |

## Out of Scope

- Constraining `metodoPago` to a fixed enum (Efectivo/Transferencia/Cheque/etc.) — the field
  stays free text for this change, matching current frontend behavior. Enumeration is a
  separate, future concern if the business asks for it.
- Emitting a payment receipt/comprobante — tracked separately per the exploration report
  (`openspec/explore_functional_report.md` §3.2, candidates other than F2).
- Any change to `PagoController.PagoRequest` validation beyond adding the new field.
