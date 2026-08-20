# Verificar deuda pendiente al archivar una gestión

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #819 |
| Use Case | CU-16 (Archivar Gestión, #169); RF-22 (issue #22); RF-37 (issue #37) |
| Branch | `feat/819_verify-debt-on-gestion-archive` |
| Gate 1 status | pending |

## Objetivo

El diagrama de estados de una gestión (`transicion-de-estados.puml`) y CU16
(Archivar Gestión) tratan el archivado como un simple cambio de estado, sin
ningún paso de verificación de dinero. RF-22 exige advertir de cualquier deuda
al finalizar la gestión y RF-37 exige que el archivado deje constancia de si
quedaron deudas sin cancelar — ninguna de las dos exigencias existe hoy en el
flujo real.

## What Changes

- Al solicitar archivar una gestión, el sistema calcula el saldo pendiente de
  los presupuestos asociados a sus trámites.
- Si existe deuda, se advierte al usuario antes de confirmar el archivado
  (advertencia, no bloqueo — el usuario puede confirmar igual).
- El registro de archivado persiste si la gestión quedó con deudas sin
  cancelar, no solo el cambio de estado.
- El diagrama de estados se actualiza para mostrar el punto de verificación de
  deuda antes de "Archivar gestión".

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Al archivar una gestión, se debe advertir al usuario si existe deuda pendiente en cualquiera de sus presupuestos. | RF-22 | Made explicit |
| El archivado de una gestión debe indicar si quedaron deudas sin cancelar. | RF-37 | Made explicit |
| El archivado no se bloquea por la existencia de deuda; la advertencia es informativa. | RF-22 ("advertir", no "impedir") | Made explicit |

## Capabilities

### New Capabilities
- `gestion-archive-debt-check`: cálculo de saldo pendiente de una gestión y
  advertencia/registro de deuda al momento de archivarla.

### Modified Capabilities
_None — no existing capability spec under `openspec/specs/` covers gestión
archiving or debt calculation yet (`openspec/specs/` is currently empty)._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | New saldo-check service call in the archive-gestión flow; archive record gains a "deuda pendiente" flag. |
| `frontend` | yes | Archive confirmation dialog shows a debt warning when saldo > 0. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Gestion` (or equivalent archive-tracking entity) gains a persisted
  "deuda pendiente al archivar" indicator.
- Endpoints: existing archive-gestión endpoint changes response/request to
  surface saldo pendiente and accept confirmation; may add a
  `GET /api/v1/gestiones/{id}/saldo` read endpoint if no equivalent exists.
- Database (Flyway `V{n}`): new migration adding the debt-at-archive column(s)
  if the archive record does not already support it.
- Configuration / `.env`: none.
- Dependencies: none new.

### Architecture review

Follows existing layering (`service` for the saldo calculation, `repository`
for data access, `api` for the controller change) — no new architectural
pattern introduced. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | Add debt-verification step to Curso de Eventos / Excepciones. |
| `docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.puml` | Add the debt-check point before "Archivar gestión". |
| `CHANGELOG.md` | Entry: gestión archiving now warns about and records pending debt. |

## Out of Scope

- Blocking archiving hard when debt exists — RF-22 asks to "advertir", not to
  prevent; a hard block is not part of this change.
- Installment/cuota tracking (issue #821) and discount/surcharge modeling
  (issue #822) — this change only establishes the saldo-check point they will
  later feed into.
- Exposing the pago↔presupuesto↔gestión relationship end-to-end (issue #820) —
  this change consumes an existing saldo calculation; it does not build the
  full financial summary view.
