# ADR-012: CI/CD Pipeline con GitHub Actions

**Status:** Accepted
**Date:** 2026-03-24
**Deciders:** Matías Miguez
**Related:** ADR-006 (Testing Strategy), ADR-007 (Flyway), ADR-009 (Logging & Monitoring)

## Context

El proyecto necesita validar automáticamente cada cambio (build, tests, cobertura, seguridad, calidad de código) antes de fusionarlo a `main`, y publicar artefactos desplegables sin intervención manual. La suite de calidad incluye Checkstyle, SpotBugs, JaCoCo, Trivy y Spotless, además de tests unitarios, de integración, HTTP (`integration-test/`) y E2E Playwright — demasiados pasos para ejecutar consistentemente a mano en cada PR.

## Decision

Usar **GitHub Actions** como única plataforma de CI/CD, con workflows separados por responsabilidad en `.github/workflows/`:

| Workflow | Responsabilidad |
|----------|------------------|
| `ci.yml` | Pipeline principal: `build`, `unit-tests`, `integration-tests`, `coverage`, `security` (Trivy), `docker-build`, `quality` (Checkstyle/SpotBugs/Spotless), `generate-reports`, `publish-reports` |
| `pr-validation.yml` | Gates específicos de pull request (título convencional, issue vinculado, tamaño del diff) |
| `playwright-e2e.yml` | Suite E2E Playwright del frontend |
| `e2e-swing.yml` | Suite E2E heredada del cliente Swing (legacy, en desuso progresivo) |
| `frontend-ci.yml` | Lint/build/test del módulo `frontend/` (Next.js) |
| `performance-test.yml` | Pruebas de carga/rendimiento bajo demanda |
| `cd.yml` | `build-and-publish`, `release`, `update-description`, `generate-report`, `publish-report` — build y publicación de imágenes Docker tras un `ci.yml` exitoso en `main` |
| `test-coverage-report.yml` | Comentario de cobertura JaCoCo en el PR |
| `deploy-github-page.yml` | Publicación de documentación/reportes estáticos |
| `claude.yml` / `opencode.yml` / `copilot-setup-steps.yml` | Integración de agentes de IA con el repositorio |

### Por qué GitHub Actions (y no Jenkins/GitLab CI/CircleCI)

| Criterio | GitHub Actions | Jenkins | GitLab CI |
|----------|----------------|---------|-----------|
| Alojamiento | Nativo en GitHub (donde vive el código) | Requiere servidor propio | Requiere GitLab (no es donde vive el código) |
| Costo inicial | Incluido en el plan de GitHub | Infraestructura a mantener | Requiere migrar de plataforma |
| Curva de aprendizaje | YAML declarativo, gran catálogo de actions reutilizables | Groovy/Jenkinsfile, plugins propios | YAML propio, similar en complejidad |
| Integración con PRs/Issues | Nativa (checks, comentarios automáticos) | Requiere plugins | Nativa solo si se migra a GitLab |

**Decisión:** GitHub Actions, por ser la opción de menor fricción dado que el repositorio ya vive en GitHub y el flujo de trabajo (`.claude/rules/ai-agent-workflow.md`) depende de Issues y PRs de GitHub.

## Consequences

### Positivos

- Cada PR recibe automáticamente el resultado de build, tests, cobertura y seguridad antes de poder fusionarse.
- `scripts/preflight.sh` replica localmente los mismos gates que corren en CI, evitando sorpresas (ver `docs/300-development/CI-PREFLIGHT.md`).
- La publicación de imágenes Docker (`cd.yml`) solo ocurre tras un `ci.yml` exitoso en `main`, evitando publicar builds rotos.

### Negativos

- Spotless corre solo en el job "Code Lint" de CI, no está enlazado al ciclo de vida de Maven — un desarrollador puede tener `mvn verify` en verde localmente y aun así fallar en CI por formato (ver issue #705). Mitigado por `scripts/preflight.sh --fix`.
- Múltiples workflows YAML incrementan el costo de mantenimiento: cualquier gate nuevo debe añadirse tanto en `.github/workflows/` como en `scripts/preflight.sh` en el mismo PR (regla explícita en `CLAUDE.md`).

## Related ADRs

- ADR-006: Testing Strategy
- ADR-007: Database Schema Versioning (Flyway)
- ADR-009: Logging & Monitoring
