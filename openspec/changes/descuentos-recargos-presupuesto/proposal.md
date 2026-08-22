# Descuentos y recargos con motivo estructurado en ítems de presupuesto

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #822 |
| Use Case | CU-45 – Modificar Presupuesto (#198); CU-71 – Gestión de Items (#300) |
| Branch | `feat/822_descuentos-recargos-presupuesto` |
| Gate 1 status | pending |

## Objetivo

`Item` (`backend-api/.../negocio/Item.java`) modela hoy un `valor`, un
`porcentaje` opcional y una `observaciones` de texto libre — no distingue un
descuento de un recargo, ni exige un motivo cuando corresponde. Ningún RF ni
Caso de Uso menciona "descuento" o "recargo" explícitamente (confirmado por
grep sobre `negocio/`), pese a ser operaciones habituales al modificar un
presupuesto (CU45) o gestionar sus ítems (CU71). `PagoService.calcularTotalPresupuesto`
— única función que suma el total de un presupuesto a partir de sus ítems —
solo suma valores, nunca resta: no hay forma de aplicar un descuento sin
falsear el total real. El ítem tampoco tiene forma de registrar el motivo de
un descuento o recargo de manera obligatoria y estructurada, ni de reportar
cuánto se descontó o recargó y por qué (`openspec/explore.md`, hallazgo 1.4).

## What Changes

- `Item` incorpora un tipo (`NORMAL`, `DESCUENTO`, `RECARGO`); un ítem sin
  tipo explícito se trata como `NORMAL` (comportamiento actual, sin cambio).
- Un ítem de tipo `DESCUENTO` o `RECARGO` SHALL requerir un motivo
  estructurado (obligatorio, no la `observaciones` opcional existente).
- `PagoService.calcularTotalPresupuesto` SHALL restar el valor de los ítems
  `DESCUENTO` y sumar el de los `RECARGO` (en vez de sumar siempre), tanto
  para el monto fijo como para el cálculo por porcentaje.
- Se introduce `ItemService` (hoy `ItemController` accede directo a
  `ItemRepository`, sin capa de servicio) que valida el motivo obligatorio
  antes de guardar y expone un reporte de descuentos/recargos por
  presupuesto.
- CU45 y CU71 documentan el nuevo tipo de ítem, su motivo y su efecto en el
  total del presupuesto.

**BREAKING CHANGES:** Ninguno a nivel de API — `tipo` es un campo nuevo con
valor por defecto `NORMAL` que preserva el comportamiento actual (suma) para
todo ítem existente o no clasificado; el cambio de signo en el cálculo del
total solo aplica a ítems explícitamente marcados `DESCUENTO`/`RECARGO`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un ítem puede clasificarse como normal, descuento o recargo. | CU-45, CU-71 | New |
| Un ítem de tipo descuento o recargo requiere un motivo estructurado no vacío. | CU-45, CU-71 | New |
| El total del presupuesto resta los ítems de tipo descuento y suma los de tipo recargo, junto con los normales. | CU-45 | New |
| Un ítem sin tipo explícito (o de tipo normal) se comporta exactamente como hoy: suma al total. | CU-71 | Made explicit |

## Capabilities

### New Capabilities
- `descuentos-recargos-presupuesto`: clasifica los ítems de un presupuesto
  como normal, descuento o recargo con motivo estructurado, y ajusta el
  cálculo del total y el reporte por presupuesto en consecuencia.

### Modified Capabilities
Ninguna — no existe una capability principal para `Item`/presupuesto en
`openspec/specs/`; esta es la primera especificación formal sobre su tipo y
efecto en el total. `presupuesto-plantillas-y-catalogo-items` (#834, sin
mergear) cubre cómo se agregan ítems desde plantillas/catálogo, no cómo se
clasifican ni cómo afectan el total — sin solapamiento de requisitos.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `Item` agrega `tipo` y `motivo`; nuevo `service/ItemService` (valida motivo obligatorio, refactoriza `ItemController` para usarlo en vez de `ItemRepository` directo); `PagoService.calcularTotalPresupuesto` aplica el signo según `tipo`; nuevo endpoint de reporte por presupuesto. |
| `frontend` | yes | `frontend/src/types/index.ts` (`Item` no coincide hoy con la entidad real — se corrige al agregar `tipo`/`motivo`); pantalla de ítems de presupuesto (CU71) permite elegir tipo y exige motivo cuando corresponde. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Item` agrega `tipo` (String/enum) y `motivo` (String,
  obligatorio condicional).
- Endpoints: `POST`/`PUT /api/v1/items` validan el motivo obligatorio
  cuando `tipo` es `DESCUENTO`/`RECARGO` (mismo path, nueva validación);
  nuevo `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos`
  para el reporte.
- Database (Flyway `V{n}`): nueva migración `V{n}__add_tipo_motivo_to_items.sql`
  agregando las columnas `tipo` (default `NORMAL`) y `motivo` (nullable) a
  `items`.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.

### Architecture review

Introduce `service/ItemService` por primera vez, siguiendo el mismo patrón
ya establecido por `PagoService`/`PresupuestoService` (regla de negocio en
`service`, HTTP en `api`, acceso a datos en `repository`) — no un patrón
arquitectónico nuevo, corrige una desviación existente (`ItemController`
accediendo directo al repositorio) en vez de perpetuarla; no requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|-------------------|
| `docs/100-business/102-use-cases/CU45 – Modificar Presupuesto.md` | Documentar el tipo de ítem (descuento/recargo) y su efecto en el total. |
| `docs/100-business/102-use-cases/CU71 – Gestión de Items.md` | Documentar el tipo de ítem, el motivo obligatorio y el reporte de descuentos/recargos. |
| `CHANGELOG.md` | Entry: ítems de presupuesto pueden clasificarse como descuento o recargo con motivo. |

## Out of Scope

- Aprobación o autorización de descuentos por encima de un umbral — no hay
  ningún RF ni CU que lo exija hoy.
- Integrar el reporte de descuentos/recargos con el resumen financiero de
  gestión de #820 (`GET /api/v1/gestiones/{id}/resumen-financiero`) — ese
  endpoint todavía no está implementado; este cambio expone su propio
  reporte por presupuesto, independiente.
- Cambios a `presupuesto-plantillas-y-catalogo-items` (#834) — cómo se
  agregan ítems desde plantillas queda sin cambios.
