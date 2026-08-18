# Documentación de Negocio

Requerimientos, casos de uso, actores y trazabilidad del dominio notarial, base para toda
especificación técnica posterior (OpenSpec, arquitectura, implementación).

## Estructura

| Carpeta | Contenido |
|---------|-----------|
| [`101-requirements/`](101-requirements/) | Relevamiento del sistema (RS) y especificación de requerimientos (SRS) |
| [`102-use-cases/`](102-use-cases/) | Catálogo de casos de uso de negocio (`CUxx`), uno por documento |
| [`103-actors/`](103-actors/) | Actores del sistema y su jerarquía |
| [`104-traceability/`](104-traceability/) | Matriz de trazabilidad Requerimientos ↔ Casos de Uso |
| [`105-manuals/`](105-manuals/) | Manuales de instalación, sistema y usuario |

## Flujo de trazabilidad

```
Requerimientos (SRS) → Casos de Uso (CUxx) → Matriz de Trazabilidad → Issues GitHub → Código
```

Todo cambio de código debe estar asociado a un Caso de Uso existente (ver
[`.claude/rules/ai-agent-workflow.md`](../../.claude/rules/ai-agent-workflow.md)). Si no existe,
debe crearse aquí antes de crear el issue correspondiente.

## Navigation

- [← Documentación](../)
- [Arquitectura](../200-architecture/)
- [Desarrollo](../300-development/)
