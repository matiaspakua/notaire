# Costos de documentos (sellos, impuestos) integrados al presupuesto y su plantilla

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #823 |
| Use Case | CU-27 – Ingresar Nuevo Tipo de Documento (#180); CU-39 – Crear Plantilla Presupuesto (#192) |
| Branch | `feat/823_costos-documentos-presupuesto` |
| Gate 1 status | pending |

## Objetivo

`DocumentoPresentado` (`backend-api/.../negocio/DocumentoPresentado.java`) ya
tiene un campo `importeAPagar` (monto) junto a `fechaPago` — no es cierto que
le falte un monto, como sugiere la descripción original del hallazgo
(`openspec/explore.md`, hallazgo 1.5); la grounding de este propose lo
confirma. El problema real es que `importeAPagar` es un dato aislado: ningún
código lo suma al total ni al saldo pendiente del presupuesto
(`PagoService.calcularTotalPresupuesto` solo itera `Presupuesto.itemList`,
nunca `Presupuesto.tramiteList[].documentoPresentadoList[].importeAPagar`),
pese a que la relación `Presupuesto → Tramite → DocumentoPresentado` ya
existe en el modelo (`Presupuesto.tramiteList`, `Tramite.documentoPresentadoList`).
Por otro lado, `PlantillaPresupuesto` (CU39) solo modela gastos por
`Concepto` (`fk_id_tipo_tramite` + `fk_id_concepto`); no hay ninguna
estructura que permita definir, para un tipo de trámite, un gasto fijo o
variable asociado a un tipo de documento (`TipoDeDocumento` no tiene ningún
campo de costo), pese a que RF-04 menciona explícitamente "gastos fijos y
variables como impuestos y sellos".

## What Changes

- `PagoService.calcularTotalPresupuesto` SHALL incluir en el total del
  presupuesto la suma de `importeAPagar` de los `DocumentoPresentado`
  asociados a sus trámites (campo existente, hoy no utilizado en ningún
  cálculo).
- Se agrega `PlantillaCostoDocumento` (clave compuesta `fk_id_tipo_tramite` +
  `fk_id_tipo_documento`, mismo patrón que `PlantillaPresupuesto`) que
  permite definir, para un tipo de trámite, un gasto fijo (monto) o variable
  (porcentaje) esperado por tipo de documento — administrable desde la
  misma pantalla de plantillas de presupuesto (CU39).
- CU27 documenta que el costo de un documento (`importeAPagar`) ya existente
  se refleja en el presupuesto de su trámite; CU39 documenta la nueva
  definición de gastos fijos/variables por tipo de documento.

**BREAKING CHANGES:** Ninguno a nivel de API existente — `importeAPagar` es
un campo que ya existe y no cambia de forma; sumarlo al total solo afecta a
presupuestos cuyos trámites tienen documentos con `importeAPagar` distinto
de cero (hoy, al no sumarse en ningún lado, todo presupuesto existente tiene
efectivamente `importeAPagar` en 0 a efectos de este cálculo o ya lo estaba
ignorando).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un documento presentado puede tener un costo asociado (sello, impuesto) con un monto, además de su fecha de pago. | CU-27 | Made explicit — `importeAPagar` ya existe en `DocumentoPresentado`; este cambio lo conecta al presupuesto. |
| El total del presupuesto de un trámite incluye el costo de los documentos presentados asociados a ese trámite. | CU-27, RF-04 | New |
| Al definir la plantilla de presupuesto de un tipo de trámite, se puede indicar un gasto fijo o variable esperado por tipo de documento. | CU-39, RF-04 | New |

## Capabilities

### New Capabilities
- `costos-documentos-presupuesto`: conecta el costo ya existente de un
  documento presentado (`importeAPagar`) al total del presupuesto de su
  trámite, y permite definir gastos fijos/variables por tipo de documento en
  la plantilla de presupuesto de un tipo de trámite.

### Modified Capabilities
Ninguna — no existe una capability principal para `Presupuesto`/plantillas
en `openspec/specs/`. `presupuesto-plantillas-y-catalogo-items` (#834, sin
mergear) cubre cargar `Item`s desde `PlantillaPresupuesto`/`Concepto` o
desde el catálogo de `Item` al crear un presupuesto real — no toca
`TipoDeDocumento`, `DocumentoPresentado` ni ninguna definición de costo por
documento, sin solapamiento de requisitos.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `PagoService.calcularTotalPresupuesto` suma `importeAPagar` de los documentos de los trámites del presupuesto; nueva entidad `PlantillaCostoDocumento` + `repository`/`service`/`api` (mismo patrón que `PlantillaPresupuesto`). |
| `frontend` | yes | Pantalla de plantillas de presupuesto (`frontend/src/app/dashboard/administracion/plantillas`, CU39) agrega una sección para definir gastos fijos/variables por tipo de documento. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: nueva `PlantillaCostoDocumento` (clave compuesta
  `fk_id_tipo_tramite` + `fk_id_tipo_documento`, `monto_fijo` nullable,
  `porcentaje_variable` nullable). `DocumentoPresentado` no cambia de
  esquema — solo se empieza a leer `importeAPagar` en un cálculo nuevo.
- Endpoints: nuevo CRUD `GET/POST/PUT/DELETE /api/v1/plantilla-costos-documento`
  (mismo patrón que `/api/v1/plantilla-presupuestos`); endpoints existentes
  de `/api/v1/presupuestos/{id}` y de saldo (`/api/v1/pagos/presupuesto/{id}/saldo`)
  cambian su valor de retorno cuando el presupuesto tiene documentos con
  costo, sin cambiar de forma.
- Database (Flyway `V{n}`): nueva migración
  `V{n}__create_plantilla_costos_documento.sql` creando la tabla
  `plantilla_costos_documento`.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.

### Architecture review

`PlantillaCostoDocumento` sigue el mismo patrón ya usado por
`PlantillaPresupuesto` (entidad de clave compuesta, `repository`/`service`/`api`
en las capas existentes); no introduce un patrón arquitectónico nuevo, no
requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|-------------------|
| `docs/100-business/102-use-cases/CU27 – Ingresar Nuevo Tipo de Documento.md` | Documentar que el costo de un documento (`importeAPagar`) se refleja en el presupuesto de su trámite. |
| `docs/100-business/102-use-cases/CU39 – Crear Plantilla Presupuesto.md` | Documentar la definición de gastos fijos/variables por tipo de documento. |
| `CHANGELOG.md` | Entry: el costo de los documentos presentados se refleja en el total del presupuesto; las plantillas de presupuesto pueden definir gastos por tipo de documento. |

## Out of Scope

- Integrar este costo con el resumen financiero de gestión de #820
  (`GET /api/v1/gestiones/{id}/resumen-financiero`) — ese endpoint no está
  implementado; como #820 ya agrega el saldo por presupuesto
  (`GestionArchiveDebtService.calcularSaldoPendiente` vía
  `PagoService.calcularSaldoPendiente`), este cambio queda automáticamente
  reflejado en el resumen de gestión el día que #820 se implemente, sin
  bloquear este propose ni requerir cambios adicionales en ese momento.
- Aplicar automáticamente el gasto fijo/variable de la plantilla como
  `importeAPagar` al crear un `DocumentoPresentado` real — la plantilla solo
  define el gasto esperado (administración, CU39); cargarlo automáticamente
  en un documento real es una extensión futura, análoga a la que
  `presupuesto-plantillas-y-catalogo-items` (#834) hace para `Item`s, pero
  no la exige ningún Acceptance Criterion de la Issue #823.
- Cambios a `presupuesto-plantillas-y-catalogo-items` (#834) — sin
  solapamiento, ver Capabilities.
