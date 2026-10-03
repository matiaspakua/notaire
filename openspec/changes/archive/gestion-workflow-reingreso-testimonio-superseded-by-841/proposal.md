# El motor de workflow no puede representar el bucle de reingreso post-firma

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #841 |
| Use Case | CU83 – Definir Workflow de Estados y Transiciones (#451, #453, #454, #455); CU06 – Firmar escritura (#159); CU07 – Generar testimonio (#160); CU11 – Ingresar para inscripción (#164); CU44 – Reingresar testimonio (#197) |
| Branch | `feat/841_gestion-workflow-reingreso-testimonio` |
| Gate 1 status | pending |

## Objetivo

El diagrama de estados de una gestión (`transicion-de-estados.puml`) modela
el circuito post-firma con un bucle: un testimonio puede volver observado y
reingresar a inscripción un número no acotado de veces. El motor de
workflow (CU83) solo puede modelar nodos mutuamente excluyentes atados 1 a 1
a un `EstadoDeGestion` — no tiene forma de representar "este testimonio ya
reingresó N veces" sin, en el mejor de los casos, aplanarlo a un tope fijo.
Esto significa que el diagrama animado de la pantalla principal
(`WorkflowTracker.tsx`) nunca podrá imitar el ciclo completo del `.puml`,
ni siquiera una vez aplicados los cambios #832 (circuito legal post-firma)
y #833 (bitácora y validación de transiciones) — ambos necesarios pero no
suficientes, porque ninguno toca el modelo de datos del workflow en sí
(`openspec/explore.md`, hallazgo 10).

## What Changes

- Nuevos estados en `EstadoDeGestion` para los sub-pasos del circuito
  post-firma que hoy no tienen ninguno: "Testimonio Generado", "Testimonio
  Ingresado a Inscripción", "Testimonio Retirado" (los estados de firma,
  anulación y "no pasó" ya existen: #6–#9).
- El `WorkflowDefinition` estándar (sembrado en
  `V10__seed_workflow_demo_data.sql`) se extiende con los `WorkflowNode`s y
  `WorkflowTransition`s correspondientes a esos nuevos estados, de forma
  que un administrador pueda configurar el circuito completo hasta antes
  del bucle de reingreso.
- El bucle de reingreso en sí **no** se modela como nodos adicionales del
  workflow — se decide representarlo mostrando `MovimientoTestimonio` (que
  #832 introduce) como una línea de tiempo secundaria dentro del mismo
  `WorkflowTracker`, asociada al nodo "Testimonio Ingresado a Inscripción",
  en lugar de forzarlo dentro del grafo de nodos mutuamente excluyentes.
  Ver Reglas de negocio y design.md — Decisions para el porqué de esta
  opción sobre la alternativa de aplanar el bucle a un tope fijo de
  estados.
- `WorkflowTraceService.buildTrace` se extiende para incluir, junto al
  trace de nodos existente, los movimientos de testimonio de la gestión
  (si los tiene), sin cambiar la forma del trace de nodos que ya consume
  el resto de la UI.

**BREAKING CHANGES:** Ninguno — `WorkflowNode`, `WorkflowTransition`,
`Historial` y `WorkflowTracker.tsx` se mantienen; este cambio agrega
estados/nodos nuevos y una capa de visualización adicional sobre datos que
#832 ya va a producir.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| El circuito post-firma hasta el ingreso a inscripción se representa como estados de gestión ("Testimonio Generado", "Testimonio Ingresado a Inscripción", "Testimonio Retirado"), igual que el resto del ciclo de vida de la gestión. | CU83 | New |
| El bucle de reingreso de un testimonio observado no se representa como un estado de gestión — se muestra como el historial de `MovimientoTestimonio` asociado al nodo de inscripción vigente. | CU44, RF-33 | New |
| El diagrama animado de la pantalla principal muestra, para una gestión con testimonio en curso, tanto su progreso en el grafo de nodos como el conteo de reingresos del movimiento actual. | CU83 | New |

## Capabilities

### New Capabilities
- `workflow-testimonio-movimiento-tracker`: extiende el trace de workflow de
  una gestión (`WorkflowTraceService.buildTrace`) para incluir los
  movimientos de testimonio asociados, y extiende `WorkflowTracker.tsx`
  para mostrarlos como una línea de tiempo secundaria en el nodo de
  inscripción.

### Modified Capabilities
_None — no existe spec previo para el trace de workflow ni para el
tracker; ambos se documentan por primera vez como parte de esta
capability nueva. El modelo de datos de `EstadoDeGestion`/`WorkflowNode`
no cambia de forma, solo se le agregan filas/nodos nuevos._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Flyway: nuevos `EstadoDeGestion` y `WorkflowNode`/`WorkflowTransition` de seed; `WorkflowTraceService.buildTrace` incluye movimientos de testimonio. |
| `frontend` | yes | `WorkflowTracker.tsx` renderiza la línea de tiempo secundaria de movimientos de testimonio en el nodo de inscripción. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `EstadoDeGestion` (filas nuevas), `WorkflowNode`/`WorkflowTransition`
  (filas nuevas de seed), sin cambio de esquema en ninguna; `MovimientoTestimonio`
  se lee, no se modifica (ya lo crea #832).
- Endpoints: `GET /api/v1/gestiones/{id}/workflow-trace` (comportamiento
  extendido, misma firma — agrega un campo opcional de movimientos).
- Database (Flyway `V{n}`): sí — nueva migración de seed (`{n}` a resolver
  al implementar) que agrega estados y nodos/transiciones; no altera
  columnas ni tablas existentes.
- Configuration / `.env`: none.
- Dependencies: none new — reutiliza `MovimientoTestimonio` de #832.

### Architecture review

Sigue el layering existente (`service` para el trace, componente de
frontend existente extendido) sobre el motor de workflow ya implementado
bajo [ADR-014](../../200-architecture/202-ADR/ADR-014-workflow-engine.md).
No requiere un ADR nuevo — es una extensión de datos y de presentación
sobre el mismo motor, no un patrón arquitectónico distinto.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | Anotar que el workflow estándar ahora cubre el circuito post-firma hasta inscripción, y que el reingreso se muestra como movimiento, no como nodo. |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | Documentar la línea de tiempo secundaria de movimientos de testimonio en el tracker. |
| `CHANGELOG.md` | Entry: el diagrama animado de la gestión ahora refleja el circuito post-firma, incluidos los reingresos de un testimonio observado. |

## Out of Scope

- El circuito de negocio en sí (firmar, generar/verificar testimonio,
  ingresar a inscripción, retirar/reingresar) — issue #832, prerequisito
  de este cambio.
- La validación de transiciones contra el workflow y la escritura real de
  `Historial` — issue #833, prerequisito de este cambio.
- Cualquier límite o alerta sobre "demasiados reingresos" de un testimonio
  — no está en el SRS ni en el `.puml`; el bucle es abierto por diseño.
