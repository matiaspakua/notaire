# Exponer la relación pago ↔ presupuesto ↔ gestión y el resumen financiero

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #820 |
| Use Case | CU-47 (Consultar Pago, #200); CU-02 (Iniciar Gestión, #155) |
| Branch | `feat/820_expose-pago-presupuesto-gestion-summary` |
| Gate 1 status | pending |

## Objetivo

Un pago ya se guarda contra un presupuesto en el backend, pero esa relación no
es visible ni consultable de punta a punta: `Pago.getPresupuesto()` está
anotado `@JsonIgnore`, y no existe ningún endpoint que arme, por presupuesto o
por gestión, el resumen que CU47 exige (total, saldo, lista de pagos). Esto
deja a la escribanía sin forma de ver, desde una gestión, cuánto se
presupuestó, cuánto se cobró y cuánto falta — el hallazgo de negocio 1.2 de
`openspec/explore.md`. Este cambio es además la base que necesitan los
issues #821 (cuotas), #822 (descuentos/recargos) y #823 (costos de
documentos), que dependen de que el saldo de una gestión sea calculable y
consultable de punta a punta.

## What Changes

- El pago expone su presupuesto asociado en la API (se remueve `@JsonIgnore`
  de `Pago.getPresupuesto()` o se introduce un DTO de respuesta explícito que
  lo incluya).
- Nuevo endpoint que, dado un presupuesto, devuelve el detalle que pide CU47:
  número de gestión, encabezado, total, saldo, y la lista de pagos (número,
  monto, fecha, observaciones).
- Nuevo endpoint que, dada una gestión, devuelve el resumen financiero
  agregado: total presupuestado, total cobrado y saldo, sumando todos los
  presupuestos vinculados a sus trámites. Reutiliza el cálculo de saldo
  agregado ya existente (`GestionArchiveDebtService`, de #819) en lugar de
  duplicarlo.
- La pantalla de consulta de pagos (CU47) muestra esta información sin
  navegación adicional.

**BREAKING CHANGES:** Ninguno a nivel de contrato existente — los endpoints
actuales (`GET /api/v1/pagos/...`, `GET /api/v1/gestiones/{id}/saldo-pendiente`)
se mantienen; este cambio añade payload y endpoints nuevos.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Al consultar un presupuesto, el sistema debe presentar número de gestión, encabezado, total, saldo y la lista de pagos asociados. | CU47 | Made explicit |
| El costo y saldo de un trámite se calculan en base a su presupuesto. | RF-21 | Made explicit |
| El saldo de una gestión es la suma del saldo pendiente de los presupuestos vinculados a sus trámites. | RF-22 (ya implementado en `GestionArchiveDebtService`, #819) | Made explicit (reuso, no nueva regla) |

## Capabilities

### New Capabilities
- `pago-presupuesto-gestion-summary`: expone la relación pago↔presupuesto en la
  API y el resumen financiero (total, cobrado, saldo, lista de pagos) por
  presupuesto y por gestión, según CU47.

### Modified Capabilities
_None — `pagos` (persistencia de `metodoPago`) y `gestion-archive-debt-check`
(cálculo de saldo agregado) no cambian de comportamiento; este cambio los
reutiliza y expone, no los modifica._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Remueve/reemplaza `@JsonIgnore` en `Pago.getPresupuesto()` (o introduce DTO explícito); nuevos endpoints `GET /api/v1/presupuestos/{id}/resumen` y `GET /api/v1/gestiones/{id}/resumen-financiero`. |
| `frontend` | yes | Pantalla/vista de CU47 (consulta de pagos) que muestra el resumen sin navegación adicional. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Pago` (deja de ocultar `presupuesto` en la serialización JSON).
- Endpoints: `GET /api/v1/presupuestos/{id}/resumen` (nuevo, CU47); `GET
  /api/v1/gestiones/{id}/resumen-financiero` (nuevo, total presupuestado/
  cobrado/saldo). Los endpoints de saldo existentes
  (`/api/v1/pagos/presupuesto/{id}/saldo`,
  `/api/v1/gestiones/{id}/saldo-pendiente`) se mantienen sin cambios.
- Database (Flyway `V{n}`): ninguna — no se agregan columnas, solo se exponen
  relaciones y cálculos ya persistidos.
- Configuration / `.env`: none.
- Dependencies: none new.

### Architecture review

Sigue el layering existente (`service` para el armado del resumen, `api` para
los controllers, DTOs explícitos en vez de exponer entidades JPA directamente
donde aplique) — no introduce un patrón arquitectónico nuevo. No requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU47 – Consultar Pago.md` | Confirmar que el paso 6 (detalle presentado) queda cubierto por el endpoint nuevo; anotar el endpoint en Referencias Cruzadas si el documento lo registra. |
| `CHANGELOG.md` | Entry: se puede consultar, por presupuesto y por gestión, el resumen financiero (total, cobrado, saldo) y la lista de pagos. |

## Out of Scope

- Pagos parciales / en cuotas con seguimiento de plan (#821) — este cambio
  provee el saldo consultable que #821 necesita, no el circuito de cuotas.
- Descuentos y recargos con motivo estructurado (#822).
- Costos adicionales de documentos vinculados al presupuesto (#823).
- Selector de presupuesto + saldo en el formulario de cobro (#796) y
  persistencia del método de pago (#792, ya resuelto) — resuelven el
  formulario de carga, no la consulta expuesta por este cambio.
- Generación e impresión de recibos (#23).
