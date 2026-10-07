# Pagos parciales con seguimiento de saldo

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #821 |
| Use Case | CU-15 – Procesar Pago (#168); CU-47 – Consultar Pago (#200) |
| Branch | `feat/821_pagos-parciales-cuotas` |
| Gate 1 status | pending |

## Objetivo

RF-22 exige poder abonar un presupuesto en cuotas sin montos fijos
predefinidos y advertir de cualquier deuda al finalizar la gestión.
`PagoService.procesarPago` (`backend-api/.../service/PagoService.java`) ya
acepta pagos repetidos sobre el mismo presupuesto sin exigir que un único
pago cubra el saldo total — el pago parcial ya es técnicamente posible hoy,
pero es un efecto accidental de la ausencia de validación, no un flujo
reconocido: `procesarPago` nunca calcula ni expone si, tras un pago, el
presupuesto quedó parcialmente abonado o saldado, y CU15/CU47 no lo
documentan como curso válido. `PagoService.calcularSaldoPendiente` y el
endpoint `GET /api/v1/pagos/presupuesto/{id}/saldo` ya calculan el saldo
numérico, y `GestionArchiveDebtService.calcularSaldoPendiente` ya lo reutiliza
para advertir de deuda al archivar una gestión (#819, mergeado) — falta el
paso intermedio: distinguir explícitamente "sin pagos", "parcialmente
abonado" y "saldado" al consultar (CU47), sin modelar un plan de cuotas de
montos fijos, que RF-22 prohíbe explícitamente.

## What Changes

- `PagoService` expone un estado de pago computado por presupuesto —
  `SIN_PAGOS`, `PARCIAL` o `SALDADO` — derivado del saldo pendiente ya
  calculado por `calcularSaldoPendiente`, sin persistir ningún dato nuevo.
- Nuevo endpoint `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado`
  (CU47, consulta de pagos) expone ese estado, para distinguir
  visualmente un presupuesto parcialmente abonado de uno saldado sin
  modificar la forma de la respuesta de los endpoints existentes.
- El formulario/listado de consulta de pagos (frontend) muestra el estado
  con una etiqueta visible (no solo color, ver `.claude/rules/ui-ux-design.md`
  — accesibilidad).
- CU15 documenta el pago parcial como curso alternativo válido (no como
  excepción); CU47 documenta el nuevo estado de pago.

**BREAKING CHANGES:** Ninguno — no se modela un plan de cuotas con montos
fijos predefinidos (RF-22 lo prohíbe explícitamente); el estado se expone
en un endpoint nuevo, sin cambiar el esquema ni la forma de la respuesta
de `GET /api/v1/pagos/presupuesto/{idPresupuesto}` (endpoint existente,
verificado contra `PagoControllerTest#shouldGetPagosByPresupuesto`, que
asume forma de arreglo).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un presupuesto sin pagos registrados tiene estado `SIN_PAGOS`; con pagos y saldo pendiente mayor a cero tiene estado `PARCIAL`; con saldo pendiente igual a cero tiene estado `SALDADO`. | RF-22 | New |
| Un pago no está obligado a cubrir el saldo total del presupuesto en un único registro. | RF-22 | Made explicit — ya era el comportamiento de `procesarPago`, nunca se validó ni se documentó como flujo intencional |
| El pago parcial es un curso alternativo válido de CU15, no una excepción del flujo. | CU-15 | New (documentación) |

## Capabilities

### New Capabilities
- `pagos-parciales-cuotas`: calcula y expone el estado de pago
  (`SIN_PAGOS`/`PARCIAL`/`SALDADO`) de un presupuesto a partir de su saldo
  pendiente, consultable junto con sus pagos (CU47).

### Modified Capabilities
Ninguna — el capability de resumen financiero de #820
(`pago-presupuesto-gestion-summary`) todavía no está mergeado a
`openspec/specs/`; este cambio no depende de su spec formal, solo reutiliza
código ya existente en `main` (`PagoService.calcularSaldoPendiente`).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `PagoService` agrega `calcularEstadoPago(idPresupuesto)`, reutilizando `calcularSaldoPendiente`; `PagoController` agrega `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado`. |
| `frontend` | yes | `frontend/src/app/dashboard/pagos/page.tsx` (y `usePagos.ts`) muestran el estado de pago con una etiqueta visible junto al listado de pagos de un presupuesto. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: ninguna — no se agrega columna ni entidad nueva; el estado es
  computado en memoria a partir de `Pago`/`Presupuesto` ya existentes.
- Endpoints: nuevo `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado`,
  aditivo — no modifica la respuesta de ningún endpoint existente.
- Database (Flyway `V{n}`): ninguna.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.

### Architecture review

Sigue el layering existente (`service` calcula el estado reutilizando
`calcularSaldoPendiente`, `api` lo expone en la respuesta). No introduce un
patrón arquitectónico nuevo; no requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|-------------------|
| `docs/100-business/102-use-cases/CU15 – Procesar Pago.md` | Documentar el pago parcial como curso alternativo válido, sin exigir cubrir el saldo total en un único pago. |
| `docs/100-business/102-use-cases/CU47 – Consultar Pago.md` | Documentar el nuevo estado de pago (`SIN_PAGOS`/`PARCIAL`/`SALDADO`) visible al consultar pagos de un presupuesto. |
| `CHANGELOG.md` | Entry: consulta de pagos distingue presupuestos parcialmente abonados de saldados. |

## Out of Scope

- Modelar un plan de cuotas con montos fijos predefinidos o cronograma —
  RF-22 lo prohíbe explícitamente; el seguimiento es sobre saldo acumulado.
- El resumen financiero agregado por presupuesto/gestión (`GET
  /api/v1/presupuestos/{id}/resumen`, `GET
  /api/v1/gestiones/{id}/resumen-financiero`) — eso es #820, todavía sin
  implementar; este cambio no lo bloquea ni lo requiere, solo reutiliza
  `calcularSaldoPendiente` ya existente en `main`.
- La advertencia de deuda al archivar una gestión — ya implementada por
  #819 (`GestionArchiveDebtService`, mergeado), sin cambios en este alcance.
