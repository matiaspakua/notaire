# Documentación de Desarrollo

Guías operativas para desarrolladores: setup del entorno, estándares de código y estrategia de
testing. Complementa `CLAUDE.md` y `CONSTITUTION.md` (raíz del repo), que definen el flujo de
trabajo obligatorio.

## Estructura

| Carpeta / Archivo | Contenido |
|---------|-----------|
| [`301-setup/`](301-setup/) | Instalación, estructura de módulos y comandos de desarrollo (backend y frontend) |
| [`302-code-standards/`](302-code-standards/) | Estándares de código: DTO mapping, manejo de errores, JPA lazy loading, transacciones Spring |
| [`303-testing/`](303-testing/) | Plan de testing E2E, matriz CU↔API y entregables de QA |
| [`templates/`](templates/) | Plantillas para especificaciones OpenSpec |
| [`CI-PREFLIGHT.md`](CI-PREFLIGHT.md) | Mapeo de checks locales (`scripts/preflight.sh`) a jobs de CI |
| [`OPENSPEC-CONSTITUTION-BRIDGE.md`](OPENSPEC-CONSTITUTION-BRIDGE.md) | Cómo OpenSpec implementa las fases de `CONSTITUTION.md` |

## Antes de tocar código

1. Leer [`CONSTITUTION.md`](../../CONSTITUTION.md) y
   [`.claude/rules/ai-agent-workflow.md`](../../.claude/rules/ai-agent-workflow.md) — flujo
   obligatorio (issue + caso de uso → branch → TDD → tests → commit → PR).
2. Ejecutar `bash scripts/preflight.sh` antes de cada push (ver
   [`CI-PREFLIGHT.md`](CI-PREFLIGHT.md); `mvn verify` solo no es suficiente).

## Navigation

- [← Documentación](../)
- [Negocio](../100-business/)
- [Arquitectura](../200-architecture/)
