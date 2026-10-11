# Use cases — PlantUML diagrams

UML use-case diagrams, grouped by business module, transcribed from the legacy
diagrams (`.jpg`) produced by the original monolith's modeling tool.
The original `.jpg` files were archived under
[`docs/000-archive/200-architecture/204-diagrams/Casos de Uso/`](../../../000-archive/200-architecture/204-diagrams/Casos%20de%20Uso/)
after they were checked as fully readable and transcribed without loss.

Each use case keeps its `CUxx` number, consistent with the business catalog in
[`docs/100-business/102-use-cases/`](../../../100-business/102-use-cases/).
`<<include>>` / `<<extend>>` relationships and actors match the corresponding
legacy diagram. The `.puml` filenames stay as archived.

## Actors

- [`actores.puml`](actores.puml) — actor hierarchy (Person, Client, Notary,
  Receptionist, Administrator). The stored notary type remains `Escribano`.

## Diagrams by module

| Diagram | Business module |
|----------|--------------------|
| [`clientes.puml`](clientes.puml) | Clients |
| [`gestiones-documentacion.puml`](gestiones-documentacion.puml) | Documentation |
| [`gestiones-escrituras.puml`](gestiones-escrituras.puml) | Deeds |
| [`gestiones-inscripciones.puml`](gestiones-inscripciones.puml) | Registration |
| [`gestiones-testimonios.puml`](gestiones-testimonios.puml) | Testimonies |
| [`gestiones-gestion.puml`](gestiones-gestion.puml) | Management |
| [`pagos.puml`](pagos.puml) | Payments |
| [`presupuestos.puml`](presupuestos.puml) | Budgets |
| [`protocolos.puml`](protocolos.puml) | Protocols (includes the Folios submodule) |
| [`administracion-conceptos.puml`](administracion-conceptos.puml) | Administration → Concepts |
| [`administracion-documentos.puml`](administracion-documentos.puml) | Administration → Document types |
| [`administracion-escribanos.puml`](administracion-escribanos.puml) | Administration → Notaries |
| [`administracion-estados-de-gestion.puml`](administracion-estados-de-gestion.puml) | Administration → Management statuses |
| [`administracion-plantilla-presupuestos.puml`](administracion-plantilla-presupuestos.puml) | Administration → Budget templates |
| [`administracion-tipos-de-folio.puml`](administracion-tipos-de-folio.puml) | Administration → Folio types |
| [`administracion-tramites.puml`](administracion-tramites.puml) | Administration → Procedures |
| [`administracion-usuarios.puml`](administracion-usuarios.puml) | Administration → Users |

> **Note:** the legacy `.jpg` `Protocolos - Folios.jpg` was a redundant crop of the
> Folios submodule already included in `Protocolos.jpg` / `protocolos.puml`. It did
> not produce a separate `.puml`.

## Why there is no sequence diagram per module

A sequence diagram per module (one for each `.puml` in this folder) was considered.
The source (`DeedController`, `WorkflowTransitionController`,
`WorkflowValidationController`, and the rest) shows that almost every backend
controller follows the same generic CRUD pattern already documented in
[SAD §6.2](../../201-SAD/sad.md#62-create-a-deed-management)
(`Controller` → `Repository`/`Service` → `AuditAspect` → PostgreSQL).
Copying that pattern into 19 more diagrams would add no new information.
The only subsystem with a different structure is the workflow engine
(CU70–72: defining and validating state graphs, not executing a transition live),
already covered by
[`workflow-gestion.puml`](../Diagrama%20de%20Estados/workflow-gestion.puml)
and [`transicion-de-estados.puml`](../Diagrama%20de%20Estados/transicion-de-estados.puml).

## Render

```bash
plantuml "clientes.puml"
```

## Navigation

- [← Diagrams](../)
- [SAD](../../201-SAD/sad.md)
- [Use cases (business)](../../../100-business/102-use-cases/)
