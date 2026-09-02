# Rechazar pagos que excedan el saldo pendiente del presupuesto

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #848 |
| Use Case | CU15 – Procesar pago (#168) / RF-18 "Abonar trámite" (issue #20, sub-requerimiento RF-18.2 "Abonar presupuestos") |
| Branch | `fix/848_reject-payments-exceeding-saldo` |
| Gate 1 status | pending |

## Objetivo

`PagoService.procesarPago` calcula `saldoPendiente` pero nunca lo compara
contra el monto que se está por registrar — su propio Javadoc afirma que
"valida que el monto no exceda el total", pero el único chequeo real es
`monto <= 0`. Un pago mal tipeado o directamente incorrecto se acepta igual
que uno válido, sin que nadie se entere en el momento del cobro (CU15, paso
11: el sistema "calcula el saldo pendiente y lo muestra", pero hoy no actúa
sobre ese cálculo). Este cambio hace que el sistema efectivamente aplique la
regla que ya dice implementar.

## What Changes

- `PagoService.procesarPago` valida `monto` contra
  `calcularSaldoPendiente(idPresupuesto)` antes de persistir el pago, y
  rechaza el pago (excepción de negocio dedicada) cuando `monto` excede el
  saldo pendiente.
- `PagoController` (`POST /pagos`, `POST /pagos/params`) traduce esa
  excepción a `409 Conflict` — código que el propio controlador ya
  documenta en `@ApiResponses` pero que ningún flujo dispara hoy — en lugar
  del `400 Bad Request` genérico que hoy captura toda `IllegalArgumentException`.
- El formulario de cobro (`frontend/src/app/dashboard/pagos/page.tsx`)
  distingue esa respuesta `409` de un error genérico de guardado y muestra
  un mensaje específico ("el monto excede el saldo pendiente") en lugar del
  `errorSave` genérico actual.
- No se toca `editarPago` (edición de un pago existente) — ver Out of Scope.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un pago no puede registrarse por un monto mayor al saldo pendiente del presupuesto al que se aplica | CU15, paso 11 (el sistema calcula y muestra el saldo pendiente al procesar el pago) | Made explicit — el cálculo ya existe en código, la validación no |
| Un pago igual al saldo pendiente (saldo exacto) es válido y no debe rechazarse | CU15 (pagar "por completo") | Made explicit |

## Capabilities

### New Capabilities
- `pago-limite-saldo-pendiente`: valida que el monto de un pago no exceda
  el saldo pendiente del presupuesto al que se aplica, tanto en el backend
  (`PagoService`/`PagoController`) como en el formulario de cobro.

### Modified Capabilities
(ninguna — no existe una spec previa para el circuito de pagos que esta
capability deba modificar; `payment-financial-tracking` (#820) cubre el
resumen agregado por gestión, un concern distinto)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `PagoService.procesarPago` valida saldo; nueva excepción de negocio; `PagoController` mapea esa excepción a 409 |
| `frontend` | yes | `pagos/page.tsx` distingue el error 409 y muestra un mensaje específico |
| `frontend-swing` | no | módulo eliminado |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: ninguna nueva; `Pago`, `Presupuesto` sin cambios de esquema.
- Endpoints: `POST /api/v1/pagos`, `POST /api/v1/pagos/params` — cambia el
  código de respuesta de un pago que excede el saldo (antes `201` sin
  validar; ahora `409 Conflict`). **BREAKING** para cualquier cliente que
  hoy dependa de que un pago por cualquier monto se acepte siempre — es el
  comportamiento que este issue considera un bug, no una API estable a
  preservar.
- Database (Flyway `V{n}`): ninguna — no hay cambio de esquema.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.

### Architecture review

Sigue la arquitectura existente: la regla de negocio vive en `service`
(no en el controller ni en el frontend), y el controller sólo traduce la
excepción a un código HTTP — mismo patrón que el manejo actual de
`IllegalArgumentException` → 400. No es un cambio arquitectónico; no
requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | Paso 11 debe reflejar que el sistema puede rechazar el pago cuando el monto excede el saldo pendiente, no sólo "calcularlo y mostrarlo" |
| `CHANGELOG.md` | Entrada en `[Unreleased]` — "Pagos: se rechaza un pago cuyo monto excede el saldo pendiente del presupuesto" |

## Out of Scope

- **`editarPago` (edición de un pago existente)**: aplicar la misma
  validación ahí requiere excluir el propio pago editado del cálculo de
  "ya pagado" (si no, editar un pago sin cambiar su monto se vería a sí
  mismo como saldo ya consumido y podría rechazarse incorrectamente). Es
  una regla relacionada pero con una diferencia de cálculo real — se deja
  para un issue de seguimiento, no se resuelve acá.
- **Picker de presupuesto + visibilidad de saldo en el formulario de
  cobro**: cubierto por #796 (abierto, sin `propose` todavío). Este cambio
  no depende de #796 — agrega el mensaje de error específico usando el
  campo "Presupuesto ID" tal como existe hoy.
- **Permitir explícitamente un sobrepago intencional** (ej. redondeo,
  propina): el hallazgo original (#848) deja abierta esa posibilidad como
  alternativa a rechazar; este cambio opta por rechazar siempre, que es la
  lectura más simple y segura de CU15/RF-18.2 y la que exige la menor
  superficie de UI nueva. Si el negocio decide que un sobrepago controlado
  debe permitirse, es un cambio de regla de negocio distinto, a trackear
  aparte.
- **`payment-financial-tracking` (#820)**: expone el resumen financiero
  agregado por gestión; no se toca ni se depende de él acá.
