# Documentación de Desarrollo

Guías operativas para desarrolladores: setup del entorno, estándares de código y estrategia de
testing. Complementa `CLAUDE.md` y `CONSTITUTION.md` (raíz del repo), que definen el flujo de
trabajo obligatorio.

## Estructura

| Carpeta / Archivo | Contenido |
|---------|-----------|
| [`DEVELOPMENT-PLAN.md`](DEVELOPMENT-PLAN.md) | Proceso SDLC, convenciones y roadmap de migración de arquitectura (documento síntesis) |
| [`303-testing/TEST-PLAN.md`](303-testing/TEST-PLAN.md) | Plan de testing maestro: niveles de test, catálogo por caso de uso, reporting |
| [`DEPLOYMENT-PLAN.md`](DEPLOYMENT-PLAN.md) | Entornos, proceso de release/promoción, rollback y checklist de release |
| [`301-setup/`](301-setup/) | Instalación, estructura de módulos y comandos de desarrollo (backend y frontend) |
| [`302-code-standards/`](302-code-standards/) | Estándares de código: DTO mapping, manejo de errores, JPA lazy loading, transacciones Spring |
| [`303-testing/`](303-testing/) | Suites de test, matriz CU↔API y guías de QA |
| [`templates/`](templates/) | Plantillas para especificaciones OpenSpec |
| [`ASSESSMENT-2026-10.md`](ASSESSMENT-2026-10.md) | Evaluación integral del sistema (octubre 2026): resultados de tests, hallazgos por dimensión e issues asociados |
| [`REPO-SPLIT-PLAN.md`](REPO-SPLIT-PLAN.md) | Plan por fases y criterios de salida para la topología de repositorios (#1197, ADR-024) |
| [`MODULE-OWNERSHIP.md`](MODULE-OWNERSHIP.md) | In-repo module map (ADR-026): paths, fleets, verify, Mermaid deps — #1197 Phase 0 |
| [`REPO-METRICS-BASELINE.md`](REPO-METRICS-BASELINE.md) | Reproducible repo metrics for topology decisions (#1256 / #1417) |
| [`CI-PREFLIGHT.md`](CI-PREFLIGHT.md) | Mapeo de checks locales (`workspace/sdlc/preflight.sh`) a jobs de CI |
| [`304-ai-sdlc-cloud/`](304-ai-sdlc-cloud/) | Cursor Cloud AI SDLC fleet: foreman, specialists, env checklist, [CI merge gate](304-ai-sdlc-cloud/CI-MERGE-GATE.md), validation (not `local-ai/`) |

OpenSpec ↔ Constitution: ver [`docs/openspec/NOTAIRE-ADAPTATIONS.md`](../openspec/NOTAIRE-ADAPTATIONS.md)
(SpecKit, retirado en #1083: [`docs/000-archive/speckit/`](../000-archive/speckit/)).

## Antes de tocar código

1. Leer [`CONSTITUTION.md`](../../CONSTITUTION.md) y
   [`.claude/rules/ai-agent-workflow.md`](../../.claude/rules/ai-agent-workflow.md) — flujo
   obligatorio (issue + caso de uso → branch → TDD → tests → commit → PR).
2. Ejecutar `bash workspace/sdlc/preflight.sh` antes de cada push (ver
   [`CI-PREFLIGHT.md`](CI-PREFLIGHT.md); `mvn verify` solo no es suficiente).

## Navigation

- [← Documentación](../)
- [Negocio](../100-business/)
- [Arquitectura](../200-architecture/)
