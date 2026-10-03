# Testing — Notaire

Framework de testing centralizado del proyecto: unit, integración, API,
frontend, E2E UI (Playwright) y trazabilidad por Caso de Uso.

Para el plan maestro de testing (niveles de test, catálogo por caso de uso,
proceso de reporting), ver [`TEST-PLAN.md`](TEST-PLAN.md). Este README es el
inventario de suites y comandos.

```text
Unit → Integration → API (Bruno) → Frontend (Vitest) → E2E UI/UX (Playwright, por Caso de Uso)
```

## Suites de test

| Suite | Ubicación | Volumen | Comando |
|-------|-----------|---------|---------|
| Unit (JUnit 5) | `backend-api/src/test/java/.../unit/` | 73 clases | `mvn test -pl backend-api -Dtest="**/unit/*"` |
| Integration (Spring Boot / H2 + PostgreSQL) | `backend-api/src/test/java/.../integration/` | 59 clases | `mvn test -pl backend-api -Dtest="**/integration/*"` |
| API (Bruno YAML) | `backend-api/api-test/` | 164 requests, 21 resource folders (idempotent) | `cd backend-api/api-test && bru run . -r --env Development` |
| Frontend unit/component (Vitest) | `frontend/src/**/*.test.ts(x)` | 19+ archivos | `cd frontend && npm test` |
| E2E UI/UX (Playwright) | `frontend/tests/e2e/` | 33 specs, por Caso de Uso (`cuNN-*.spec.ts`) | `cd frontend && npm run test:e2e` |
| HTTP (cURL, legacy smoke) | `testing/http/` | 10 scripts | `bash testing/http/test-all-endpoints-v2.sh` |
| E2E Swing (Robot) | `testing/e2e-swing/` | **RETIRED** (#811 / ADR-012) | Do not run; see suite README |

Backend: 132 test classes, ~1,483 `@Test` methods combined (unit + integration).

## Referencias detalladas

| Documento | Contenido |
|-----------|-----------|
| [`CU-API-MATRIX.csv`](CU-API-MATRIX.csv) | Trazabilidad Caso de Uso → módulo → entidad/operación → controller/endpoint → test Bruno → issue. Drift guard: `python3 scripts/validate-cu-api-matrix.py` (#1064) |
| [`FRONTEND-TESTING-GUIDE.md`](FRONTEND-TESTING-GUIDE.md) | Convenciones de testing Vitest y estructura de specs E2E Playwright |
| [`api-test/README.md`](api-test/README.md) | Guía de pruebas manuales HTTP/curl y patrones de testing de la API |
| [`test-coverage/TEST-COVERAGE-STRATEGY.md`](test-coverage/TEST-COVERAGE-STRATEGY.md) | Estrategia de cobertura por capa y automatización de reportes |
| [`backend-api/api-test/COVERAGE.md`](../../../backend-api/api-test/COVERAGE.md) | Estado actual de la suite Bruno (pass/fail, defectos encontrados) |

## Quick start

```bash
# Backend: unit + integration
mvn test -pl backend-api

# Backend: solo unit (rápido, ~10s)
mvn test -pl backend-api -Dtest="**/unit/*"

# Cobertura backend
mvn jacoco:report -pl backend-api && open backend-api/target/site/jacoco/index.html

# Frontend: unit + coverage
cd frontend && npm run test:coverage

# E2E Playwright (requiere stack completo: bash scripts/start.sh)
cd frontend && npm run test:e2e
npm run test:e2e:headed   # modo interactivo

# API (Bruno, requiere backend en :8080)
cd backend-api/api-test && bru run . -r --env Development

# Réplica local de todos los gates de CI
bash scripts/preflight.sh --full
```

## Cobertura

Piso obligatorio (ratchet floor) y objetivo a largo plazo, ver
[`.claude/rules/code-quality.md`](../../../.claude/rules/code-quality.md):
70% línea / 25% branch (piso, enforced vía `mvn verify`), 80%/80% (objetivo).
Actual: ~84% línea / ~74% branch (backend, `jpa`/`service.Administrador*` excluidos).

## CI/CD

Mapeo completo de checks locales ↔ jobs de CI:
[`CI-PREFLIGHT.md`](../CI-PREFLIGHT.md). En resumen:

- **PR**: unit + integration (H2 + PostgreSQL), Checkstyle, Spotless (job "Code Lint"), SpotBugs, Playwright E2E
- **Merge a `main`**: suite completa + cobertura (ratchet floor) + Trivy
- **Release**: build de imagen Docker + escaneo de imagen

## Reportes

```bash
open backend-api/target/site/jacoco/index.html   # cobertura backend (JaCoCo)
open frontend/coverage/index.html                 # cobertura frontend (Vitest)
open frontend/playwright-report/index.html         # reporte E2E Playwright
```

Dashboard agregado (GitHub Pages, actualizado por CI): ver
[`.github/workflows/test-coverage-report.yml`](../../../.github/workflows/test-coverage-report.yml).

## Swing client and Robot E2E (retired)

The Swing modules (`frontend-swing` / `deprecated-frontend-swing`) were removed
from the repository (#1046). `.github/workflows/e2e-swing.yml` is retired
(ADR-012 / #1083 / #811). Assets under `testing/e2e-swing/` are hard-deprecated
in place — do **not** wire them into CI or rebuild Swing. Active UI E2E is
Playwright (`frontend/tests/e2e/`).

## Navigation

- [← Desarrollo](../)
- [Test Plan](TEST-PLAN.md)
- [Arquitectura](../../200-architecture/)
