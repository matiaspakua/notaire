# Cargar un presupuesto real desde la plantilla de su tipo de trámite y el catálogo de ítems

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #834 |
| Use Case | CU39 – Crear Plantilla Presupuesto (#192); CU55 – Modificar Plantilla Presupuesto (#208); CU49 – Eliminar Plantilla Presupuesto (#202); CU71 – Gestión de Items (#300) |
| Branch | `feat/834_presupuesto-plantillas-y-catalogo-items` |
| Gate 1 status | pending |

## Objetivo

`PlantillaPresupuesto` (CU39/CU55/CU49) ya modela, por `TipoDeTramite`, la
lista de `Concepto`s (nombre, valor, porcentaje) que componen su
presupuesto estándar, administrable en
`frontend/src/app/dashboard/administracion/plantillas`. `Item` (CU71) ya
es un catálogo reutilizable (nombre, valor, porcentaje, observaciones),
administrable en `frontend/src/app/dashboard/items`, y ya puede asociarse
a un presupuesto vía `fk_id_presupuesto` (`ItemController`,
`Presupuesto.itemList`). Pero la pantalla donde se carga un presupuesto
real (`frontend/src/app/dashboard/presupuestos`) no consulta ninguna de
las dos: el único campo de precio es "monto" (`Presupuesto.montoInmueble`,
el valor del inmueble, no el costo del trámite), sin selector de tipo de
trámite que traiga su plantilla ni forma de agregar ítems del catálogo.
Dos personas cotizando el mismo tipo de trámite hoy escriben un número a
mano cada una, sin fuente de precio común (`openspec/explore.md`,
hallazgo 4).

## What Changes

- Al crear un presupuesto, el usuario selecciona un `TipoDeTramite`; el
  sistema ofrece cargar automáticamente, como `Item`s del presupuesto, los
  `Concepto`s de la `PlantillaPresupuesto` de ese tipo de trámite (nombre,
  valor y porcentaje heredados del concepto), en lugar de partir de una
  lista vacía.
- La pantalla de presupuesto ofrece agregar ítems adicionales eligiéndolos
  del catálogo existente (`Item`, CU71) en lugar de solo poder crear un
  `Item` nuevo desde cero.
- El presupuesto muestra el desglose de sus ítems (de plantilla y del
  catálogo) con su subtotal, además del campo "monto" (valor del
  inmueble) ya existente, que no cambia de significado.
- No se agrega ninguna entidad nueva: se conecta la pantalla real de
  presupuesto a `PlantillaPresupuesto` e `Item`, que ya existen y ya
  tienen pantalla de administración propia y funcional.

**BREAKING CHANGES:** Ninguno — `Presupuesto`, `Item`, `PlantillaPresupuesto`
y `Concepto` se mantienen sin cambios de esquema; este cambio añade una
vía adicional (desde la plantilla o el catálogo) para poblar
`Presupuesto.itemList`, que hoy solo se puede llenar creando ítems desde
cero.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Al elegir un tipo de trámite para un presupuesto, el sistema ofrece cargar los conceptos de su plantilla como ítems del presupuesto. | CU39, RF-04, RF-64 a RF-67 | New |
| Los ítems cargados desde la plantilla conservan el nombre, valor y porcentaje del concepto de origen al momento de la carga (no se recalculan si la plantilla cambia después). | CU39 | New |
| El usuario puede agregar al presupuesto ítems adicionales existentes del catálogo (CU71), sin necesidad de recrearlos. | CU71, RF-07 | New |
| El campo "monto" del presupuesto sigue representando el valor del inmueble (`montoInmueble`) y no se ve afectado por la carga de ítems de plantilla o catálogo. | Presupuesto.montoInmueble | Sin cambios |

## Capabilities

### New Capabilities
- `presupuesto-plantilla`: al elegir un tipo de trámite para un
  presupuesto, ofrece cargar los conceptos de su `PlantillaPresupuesto`
  como ítems del presupuesto.
- `presupuesto-catalogo-items`: permite agregar a un presupuesto ítems
  existentes tomados del catálogo (`Item`), en lugar de solo poder crear
  ítems nuevos desde cero.

### Modified Capabilities
Ninguna — `Item` y `PlantillaPresupuesto` (CRUD, CU39/CU55/CU49/CU71) no
cambian; este cambio solo agrega una forma de poblar `Presupuesto.itemList`
desde ellos.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nuevo endpoint `POST /api/v1/presupuestos/{id}/items-desde-plantilla?tipoTramiteId=` que copia los conceptos de la `PlantillaPresupuesto` del tipo de trámite indicado como nuevos `Item`s del presupuesto; nuevo endpoint `POST /api/v1/presupuestos/{id}/items-desde-catalogo` (body: lista de `idItem`) que asocia copias de ítems existentes del catálogo al presupuesto. |
| `frontend` | yes | Selector de tipo de trámite y acción "Cargar ítems de la plantilla" al crear un presupuesto; selector de ítems del catálogo (`useItems()`) para agregarlos al presupuesto; tabla de desglose de ítems con subtotal, en `frontend/src/app/dashboard/presupuestos`. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Presupuesto`, `Item`, `PlantillaPresupuesto`, `Concepto`
  (sin cambios de esquema; se agregan operaciones de negocio que
  combinan las que ya existen).
- Endpoints (nuevos): `POST /api/v1/presupuestos/{id}/items-desde-plantilla`;
  `POST /api/v1/presupuestos/{id}/items-desde-catalogo`.
- Endpoints (sin cambios): `POST /api/v1/items`, `GET /api/v1/items`,
  `GET /api/v1/plantilla-presupuestos` siguen usándose tal cual desde sus
  pantallas de administración existentes.
- Database (Flyway `V{n}`): ninguna — `Item.fk_id_presupuesto` ya es
  nullable y ya soporta esta relación.
- Configuration / `.env`: none.
- Dependencies: none new.

### Architecture review

Sigue el layering existente (`service` para la lógica de copiar
conceptos/ítems, `api` para los endpoints de acción). No introduce un
patrón arquitectónico nuevo, no requiere un nuevo ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU39 – Crear Plantilla Presupuesto.md` | Anotar en Referencias Cruzadas que la plantilla ahora se consume desde la carga real de presupuesto. |
| `docs/100-business/102-use-cases/CU71 – Gestión de Items.md` | Anotar en Referencias Cruzadas el nuevo endpoint de asociación de ítems del catálogo a un presupuesto. |
| `CHANGELOG.md` | Entry: al cargar un presupuesto real se pueden traer los ítems de la plantilla del tipo de trámite y agregar ítems del catálogo existente. |

## Out of Scope

- El CRUD de `PlantillaPresupuesto` e `Item` en sí (CU39/CU55/CU49/CU71 ya
  cubren su propia administración) — este cambio solo consume esas
  entidades desde la carga real de presupuesto.
- Recalcular automáticamente los ítems ya cargados si la plantilla cambia
  después (ver Reglas de negocio — se documenta como comportamiento
  explícito, no un gap a resolver aquí).
- Cualquier cambio al significado o cálculo de `monto`/`montoInmueble`.
