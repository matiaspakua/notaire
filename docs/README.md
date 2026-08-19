# Documentación — Notaire

Índice de navegación de toda la documentación del proyecto Notaire, organizada por fase del
ciclo de vida: negocio, arquitectura y desarrollo.

## Estructura

| Carpeta | Contenido |
|---------|-----------|
| [`100-business/`](100-business/) | Requerimientos, casos de uso, actores y trazabilidad de negocio |
| [`200-architecture/`](200-architecture/) | SAD (arc42), ADRs, diseño, diagramas, modelo de datos, seguridad, monitoreo, DevSecOps y deployment |
| [`300-development/`](300-development/) | Guías de setup, estándares de código y estrategia de testing |
| [`000-archive/`](000-archive/) | Documentos y artefactos legacy superados, conservados por trazabilidad histórica |

## Punto de entrada recomendado

1. **Negocio**: [Casos de Uso](100-business/102-use-cases/) — qué hace el sistema y para quién.
2. **Arquitectura**: [SAD](200-architecture/201-SAD/sad.md) — visión arc42 completa del sistema.
3. **Desarrollo**: [`CLAUDE.md`](../CLAUDE.md) y [`CONSTITUTION.md`](../CONSTITUTION.md) — flujo de
   trabajo obligatorio para cualquier cambio de código.

## Convenciones

- Toda carpeta numerada (`1XX-`, `2XX-`, `3XX-`) sigue el orden de lectura sugerido dentro de su fase.
- Los diagramas se mantienen en PlantUML (`.puml`), versionables como texto; los `.svg` renderizados
  se commitean junto al fuente.
- Los documentos superados (formatos legacy como `.drawio`/`.jpg`, o contenido reemplazado) se mueven
  a `000-archive/` con un `README.md` que enlaza al equivalente vigente, y se deja una nota de
  redirección en la carpeta original.
