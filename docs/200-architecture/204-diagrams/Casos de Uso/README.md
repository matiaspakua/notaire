# Casos de Uso — Diagramas PlantUML

Diagramas de casos de uso UML, agrupados por módulo de negocio, transcritos fielmente desde los
diagramas legacy (`.jpg`) que se generaron con la herramienta de modelado del monolito original.
Los `.jpg` originales fueron archivados en
[`docs/000-archive/200-architecture/204-diagrams/Casos de Uso/`](../../../../000-archive/200-architecture/204-diagrams/Casos%20de%20Uso/)
tras verificar que eran totalmente legibles y transcribirlos sin pérdida de información.

Cada caso de uso está identificado con su número `CUxx`, coherente con el catálogo de casos de uso
de negocio en [`docs/100-business/102-use-cases/`](../../../100-business/102-use-cases/). Las
relaciones `<<include>>` / `<<extend>>` y los actores reproducen exactamente los del diagrama
legacy correspondiente.

## Actores

- [`actores.puml`](actores.puml) — jerarquía de actores del sistema (Persona, Cliente, Escribano,
  Recepcionista/Gestor, Administrador).

## Diagramas por módulo

| Diagrama | Módulo de negocio |
|----------|--------------------|
| [`clientes.puml`](clientes.puml) | Clientes |
| [`gestiones-documentacion.puml`](gestiones-documentacion.puml) | Documentación |
| [`gestiones-escrituras.puml`](gestiones-escrituras.puml) | Escrituras |
| [`gestiones-inscripciones.puml`](gestiones-inscripciones.puml) | Inscripciones |
| [`gestiones-testimonios.puml`](gestiones-testimonios.puml) | Testimonios |
| [`gestiones-gestion.puml`](gestiones-gestion.puml) | Gestión |
| [`pagos.puml`](pagos.puml) | Pagos |
| [`presupuestos.puml`](presupuestos.puml) | Presupuestos |
| [`protocolos.puml`](protocolos.puml) | Protocolos (incluye submódulo Folios) |
| [`administracion-conceptos.puml`](administracion-conceptos.puml) | Administración → Conceptos |
| [`administracion-documentos.puml`](administracion-documentos.puml) | Administración → Tipos de documento |
| [`administracion-escribanos.puml`](administracion-escribanos.puml) | Administración → Escribanos |
| [`administracion-estados-de-gestion.puml`](administracion-estados-de-gestion.puml) | Administración → Estados de Gestión |
| [`administracion-plantilla-presupuestos.puml`](administracion-plantilla-presupuestos.puml) | Administración → Plantillas de Presupuestos |
| [`administracion-tipos-de-folio.puml`](administracion-tipos-de-folio.puml) | Administración → Tipos de Folio |
| [`administracion-tramites.puml`](administracion-tramites.puml) | Administración → Trámites |
| [`administracion-usuarios.puml`](administracion-usuarios.puml) | Administración → Usuarios |

> **Nota:** el `.jpg` legacy `Protocolos - Folios.jpg` era un recorte redundante del submódulo
> Folios ya incluido íntegramente en `Protocolos.jpg` / `protocolos.puml`; no generó un `.puml`
> independiente.

## Renderizar

```bash
plantuml "clientes.puml"
```

## Navigation

- [← Diagrams](../)
- [SAD](../../201-SAD/sad.md)
- [Use Cases (negocio)](../../../100-business/102-use-cases/)
