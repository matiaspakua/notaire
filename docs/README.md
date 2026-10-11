# Documentación — Notaire

Índice de navegación de toda la documentación del proyecto Notaire, organizada por fase del
ciclo de vida: negocio, arquitectura y desarrollo.

## Estructura

| Carpeta | Contenido |
|---------|-----------|
| [`100-business/`](100-business/) | Requerimientos, casos de uso, actores y trazabilidad de negocio |
| [`200-architecture/`](200-architecture/) | SAD (arc42), ADRs, diseño, diagramas, modelo de datos, seguridad, monitoreo, DevSecOps y deployment |
| [`300-development/`](300-development/) | Guías de setup, estándares de código, testing y [AI SDLC Cloud fleet](300-development/304-ai-sdlc-cloud/); auditoría vigente: [DOCUMENTATION-AUDIT-2026-10.md](300-development/DOCUMENTATION-AUDIT-2026-10.md) |
| [`github/`](github/) | Organización de GitHub (tablero, milestones, labels) y auditoría de preparación para producción 2026-09 |
| [`000-archive/`](000-archive/) | Documentos y artefactos legacy superados, conservados por trazabilidad histórica |

## Punto de entrada recomendado

1. **Negocio**: [Casos de Uso](100-business/102-use-cases/) — qué hace el sistema y para quién.
2. **Ciclo de vida**: [CICLO-DE-VIDA.md](200-architecture/203-design/CICLO-DE-VIDA.md) — estados,
   secuencias y vistas C4 del producto.
3. **Método**: [INGENIERIA.md](300-development/INGENIERIA.md) — artefactos, trazabilidad y procesos.
4. **Arquitectura**: [SAD](200-architecture/201-SAD/sad.md) — visión arc42 completa del sistema.
5. **Desarrollo**: [`CLAUDE.md`](../CLAUDE.md) y [`CONSTITUTION.md`](../CONSTITUTION.md) — flujo de
   trabajo obligatorio para cualquier cambio de código.
6. **Gestión**: [Organización de GitHub](github/README.md) — dónde se sigue el trabajo y cómo se etiqueta.

## Convenciones

- Toda carpeta numerada (`1XX-`, `2XX-`, `3XX-`) sigue el orden de lectura sugerido dentro de su fase.
- Los diagramas activos van en Mermaid dentro del Markdown ([ADR-027](200-architecture/202-ADR/ADR-027-mermaid-diagrams.md)).
  Los `.puml` que quedan son legado.
- Los documentos superados (formatos legacy como `.drawio`/`.jpg`, o contenido reemplazado) se mueven
  a `000-archive/` con un `README.md` que enlaza al equivalente vigente, y se deja una nota de
  redirección en la carpeta original.
