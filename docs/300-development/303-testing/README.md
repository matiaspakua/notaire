# Testing — Notaire

Framework de testing centralizado del proyecto: unit, integración, API,
frontend, E2E UI (Playwright) y trazabilidad por Caso de Uso.

Para el plan maestro de testing (niveles de test, catálogo por caso de uso,
proceso de reporting), ver [`TEST-PLAN.md`](TEST-PLAN.md). Este README es el
inventario de suites y comandos.

```
Unit → Integration → API (Bruno) → Frontend (Vitest) → E2E UI/UX (Playwright, por Caso de Uso)
```

## Suites de test

| Suite | Ubicación | Volumen | Comando |
|-------|-----------|---------|---------|
| Unit (JUnit 5) | `backend-api/src/test/java/.../unit/` | 73 clases | `mvn test -pl backend-api -Dtest="**/unit/*"` |
| Integration (Spring Boot / H2 + PostgreSQL) | `backend-api/src/test/java/.../integration/` | 59 clases | `mvn test -pl backend-api -Dtest="**/integration/*"` |
| API (Bruno YAML) | `backend-api/api-test/` | 104 requests, 16 recursos | `cd backend-api/api-test && bru run . -r --env Developmen` |
| Frontend unit/component (Vitest) | `frontend/src/**/*.test.ts(x)` | 19+ archivos | `cd frontend && npm test` |
| E2E UI/UX (Playwright) | `frontend/tests/e2e/` | 33 specs, por Caso de Uso (`cuNN-*.spec.ts`) | `cd frontend && npm run test:e2e` |
| HTTP (cURL, legacy smoke) | `testing/http/` | 10 scripts | `bash testing/http/test-all-endpoints-v2.sh` |
| E2E Swing (Robot Framework, **deprecado**) | `testing/e2e-swing/tests/` | 7 suites | `cd testing/e2e-swing && robot tests/` |

Backend: 132 clases de test, ~1.483 métodos `@Test` combinados (unit + integration).

## Referencias detalladas

| Documento | Contenido |
|-----------|-----------|
| [`CU-API-MATRIX.csv`](CU-API-MATRIX.csv) | Trazabilidad Caso de Uso → módulo → entidad/operación → controller/endpoint → test Bruno → issue |
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
cd backend-api/api-test && bru run . -r --env Developmen

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

## Cliente Swing (deprecado)

`deprecated-frontend-swing` está excluido del reactor Maven raíz — sus tests
corren de forma independiente:

```bash
cd deprecated-frontend-swing && mvn test
```

Los suites E2E de Robot Framework (`testing/e2e-swing/`) validan este cliente
legacy; no se amplían con nueva cobertura (ver `CLAUDE.md`).

## Navigation

- [← Desarrollo](../)
- [Test Plan](TEST-PLAN.md)
- [Arquitectura](../../200-architecture/)
