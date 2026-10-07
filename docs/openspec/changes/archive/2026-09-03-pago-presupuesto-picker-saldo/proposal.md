# Dar al formulario de cobro un picker de presupuesto y saldo pendiente visible

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #796 |
| Use Case | CU15 – Procesar pago (#168), pasos 2-5 (búsqueda y selección de presupuesto) y paso 11 (cálculo y muestra del saldo pendiente) |
| Branch | `feat/796_pago-presupuesto-picker-saldo` |
| Gate 1 status | pending |

## Objetivo

CU15 (pasos 2-5) describe que el Recepcionista/Escribano busca al Cliente,
el sistema presenta los presupuestos pendientes asociados y el
Recepcionista/Escribano selecciona uno de la lista para ver sus datos. El
formulario de cobro (`frontend/src/app/dashboard/pagos/page.tsx`) no
implementa nada de esto: "Presupuesto ID" es un `<Input type="number">` en
el que se tipea un ID a ciegas, sin lista, sin buscar por cliente, sin
verificar que exista, y sin mostrar el saldo pendiente antes de confirmar el
pago. El backend ya expone lo necesario
(`GET /api/v1/presupuestos/{id}/resumen`, que incluye `saldoPendiente`, y ya
tiene un hook de frontend sin usar en esta pantalla: `usePresupuestoResumen`);
lo que falta es conectarlo a la UI de cobro.

## What Changes

- El campo numérico "Presupuesto ID" del formulario de cobro se reemplaza
  por un picker (`<Select>`, mismo patrón ya usado en
  `dashboard/gestiones/page.tsx` para elegir presupuesto) que lista los
  presupuestos existentes identificando cliente y número, en lugar de pedir
  un ID de memoria.
- Al seleccionar un presupuesto en el picker, el formulario muestra su
  saldo pendiente actual (vía `usePresupuestoResumen`, ya existente) antes
  de que el monto se confirme.
- No se modifica ningún endpoint del backend: `GET /presupuestos/{id}/resumen`
  y `GET /pagos/presupuesto/{id}/saldo` ya existen y ya están probados; este
  cambio es exclusivamente de `frontend`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| El Recepcionista/Escribano debe poder seleccionar el presupuesto de una lista en lugar de escribir su ID | CU15, pasos 3-4 | Made explicit — la regla ya existe en el Caso de Uso, la UI actual no la implementa |
| El saldo pendiente del presupuesto seleccionado debe mostrarse antes de confirmar el pago | CU15, paso 11 (hoy se calcula y muestra recién después de registrar el pago) | Changed — se anticipa la visibilidad del saldo a la selección del presupuesto, no sólo tras guardar |

## Capabilities

### New Capabilities
- `pago-presupuesto-picker-saldo`: el formulario de cobro permite elegir un
  presupuesto de una lista (no un ID a mano) y muestra su saldo pendiente
  antes de registrar el pago.

### Modified Capabilities
(ninguna — `pagos` cubre el procesamiento/persistencia del pago en sí, no
la selección de presupuesto en la UI; no se toca ningún requirement
existente de esa capability)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | endpoints reutilizados sin cambios (`GET /presupuestos/{id}/resumen`, `GET /pagos/presupuesto/{id}/saldo`) |
| `frontend` | yes | `pagos/page.tsx` reemplaza el input de ID por un `<Select>` de presupuestos y muestra el saldo pendiente del seleccionado (`usePresupuestoResumen`) |
| `frontend-swing` | no | módulo eliminado |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: ninguna; `Pago`, `Presupuesto` sin cambios de esquema.
- Endpoints: ninguno nuevo ni modificado — se consumen
  `GET /api/v1/presupuestos/{id}/resumen` y `GET /api/v1/presupuestos` (ya
  usado por `usePresupuestos()`), ambos ya documentados en Swagger.
- Database (Flyway `V{n}`): ninguna.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva — se reutiliza el componente `<Select>` del
  design system ya existente (`components/ui/select.tsx`); no se agrega un
  Combobox/typeahead nuevo (ver Out of Scope).

### Architecture review

Sigue el patrón ya establecido en `gestiones/page.tsx`: un `<Select>`
poblado desde un hook de React Query existente (`usePresupuestos`), sin
lógica de negocio en el cliente. El saldo se obtiene de un hook de lectura
ya existente (`usePresupuestoResumen`), no se duplica el cálculo en el
frontend. No es un cambio arquitectónico; no requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | Paso 5 debe incluir explícitamente el saldo pendiente entre los datos mostrados del presupuesto seleccionado (hoy sólo lista gestión, encabezado, número, trámite, ítems y total) |
| `CHANGELOG.md` | Entrada en `[Unreleased]` — "Pagos: el formulario de cobro permite elegir el presupuesto de una lista y muestra su saldo pendiente antes de confirmar el pago" |

## Out of Scope

- **Búsqueda por tipo/número de identificación del cliente** (CU15, paso 2
  menciona buscar también por identificación del cliente, no sólo por
  nombre/número de presupuesto): el picker de este cambio lista los
  presupuestos existentes con cliente y número visibles, pero no agrega un
  campo de búsqueda por documento de identidad — no hay endpoint de backend
  para eso hoy. Se deja para un issue de seguimiento si el volumen de
  presupuestos lo justifica.
- **Combobox con filtro de texto libre ("typeahead")**: el design system no
  tiene hoy un componente Combobox/`cmdk` (sólo `<Select>`); agregarlo es
  una decisión de design system más amplia que excede este cambio. Este
  cambio reutiliza `<Select>` (con su typeahead nativo por teclado), igual
  que `gestiones/page.tsx`.
- **`pago-limite-saldo-pendiente` (#848)**: valida en el backend que el
  monto de un pago no exceda el saldo pendiente; es un cambio independiente
  y complementario — este cambio sólo hace visible el saldo, no lo valida.
- **`payment-financial-tracking` (#820)**: expone el resumen financiero
  agregado por gestión; ya mergeado (PR #845) y no se toca acá.
