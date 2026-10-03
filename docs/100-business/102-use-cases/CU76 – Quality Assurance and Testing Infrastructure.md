# CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas (Quality Assurance and Testing Infrastructure)

## Información del Caso de Uso

| Atributo | Detalle |
|---|---|
| **Caso de Uso** | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas |
| **Actores** | Equipo de QA, Equipo de Desarrollo, Pipeline CI/CD |
| **Propósito** | Proveer una infraestructura integral de pruebas automatizadas (unitarias, integración y E2E Playwright) y validación de estándares de interfaz gráfica para asegurar la calidad y estabilidad de todas las funcionalidades notariales. |
| **Descripción** | Define las prácticas de prueba, estándares visuales de formularios secuenciales y control de calidad requeridos para validar cada caso de uso y requerimiento del sistema. |
| **Tipo** | Soporte / Calidad |
| **Referencias Cruzadas** | RF #74 (Aspecto visual), RF #75 (Diseño de ventanas), RF #76 (Diseño de campos y combos), RF #77 (Especificación de campos a completar), RF #78 (Uso de colores en la GUI), RF #79 (Seguimiento del trabajo sobre ventanas), RF #80 (Identificación de sesión), RF #86 (Java VM), RF #87 (Sistema operativo), RF #90 (Metodología de desarrollo), RF #91 (Modelo de desarrollo), RF #92 (Lenguaje de programación) |
| **GitHub ID** | #276, #295, #296, #594, #1047, #1042, #1041, #1043, #1050 |

## Alcance de Calidad e Interfaz

- Pruebas unitarias de servicios y lógica de dominio con JUnit 5 y Mockito.
- Pruebas de integración con base de datos real en contenedores (Testcontainers/PostgreSQL).
- Pruebas End-to-End (E2E) con Playwright para validar flujos de usuario completos.
- Validación de accesibilidad, navegación por teclado (Tab) y formularios secuenciales claros.
- Monitoreo continuo de cobertura de código con JaCoCo (meta ≥ 80%).
- Workflow de performance k6 (`performance-test.yml`) con script en
  `performance-test/k6/load-test.js`, validación de assets en
  `scripts/test_performance_test_assets.py`, y publicación del artefacto
  `k6-load-test-results` / `summary.json` (issue #1047; no es gate por PR).
- Publicación CD a GHCR anclada al SHA que CI probó en `main`
  (`workflow_run.head_sha`), con `latest` movido solo después del push del
  tag SHA; se omite publicar si CI no concluyó `success` (issue #1042;
  `scripts/test_cd_pin_tested_sha.py`).
- Publicación CD de **backend y frontend** a GHCR con SBOM CycloneDX, firma
  cosign keyless y attest del SBOM; proceso semver automatizado
  (release-please) que corta tags `v*`, rueda `CHANGELOG.md` `[Unreleased]`
  y deriva versiones Maven/npm del tag (issue #1043;
  `scripts/test_frontend_ghcr_publish.py`,
  `scripts/test_semver_release_process.py`; runbook
  `docs/300-development/RELEASE.md`).
- Informes CI/CD/E2E publicados como artefactos de Actions,
  `$GITHUB_STEP_SUMMARY` y/o GitHub Pages — **nunca** como commits de bot
  en `docs/wiki/cicd-reports/` (issue #1041;
  `scripts/test_no_bot_report_commits.py`).
- Higiene del repositorio: `.gitignore` sin ban global `*.txt`, `.serena/`
  local-only, PDF del Manual de Usuario vía Release `docs-manuals` (no blob
  ordinario), ADR-022 difiere `git filter-repo` (issue #1050;
  `scripts/test_repo_hygiene.py`).

## Ciclo de Verificación de Calidad

| Paso | Actor | Sistema |
|---|---|---|
| 1 | El desarrollador implementa pruebas unitarias e integración siguiendo TDD. | Ejecuta la suite de pruebas locales (`mvn test`, `mvn verify`). |
| 2 | Se realiza un cambio en la interfaz gráfica de usuario. | Se ejecutan las pruebas E2E con Playwright simulando la interacción en formularios. |
| 3 | El pipeline de CI/CD procesa la integración del código. | Valida Checkstyle, SpotBugs, cobertura JaCoCo (≥ 80%), **ESLint del frontend (bloqueante, incl. jsx-a11y; #1048)** y suite completa de tests. |
| 4 | El sistema valida la consistencia visual y de sesión. | Verifica que el nombre del usuario y el estado del trámite se visualicen en todo momento en pantalla. |

## Confiabilidad E2E (Playwright) — #1066

| Regla | Detalle |
|---|---|
| Auto-arranque de datos | Suites E2E crean sus fixtures vía helpers API; no `test.skip()` por tablas vacías. |
| Esperas web-first | Prohibido `waitForTimeout` como espera de corrección; assert sobre UI/URL/respuesta. |
| Skips intencionales | Todo `test.skip` por gap de producto cita un issue abierto (p. ej. #1146). |
| Retries CI | Como máximo **1** retry en CI; triaje vía `trace: on-first-retry` + artefactos. |

## Criterios de Aceptación

- [x] Cobertura de código superior al 80% verificada por JaCoCo.
- [x] Pruebas E2E de Playwright implementadas para los flujos críticos de negocio.
- [x] Interfaz gráfica validada con navegación secuencial por teclado y combos predefinidos.
- [x] Verificación de identificación permanente de sesión de usuario en pantalla.
- [x] Suites E2E se auto-abastecen de datos y no ocultan flakiness con sleeps/retries (#1066).
- [x] Controles icon-only del dashboard exponen nombre accesible traducido (`aria-label`) para tecnologías de asistencia y selectores `getByRole` (WCAG 2.1 SC 4.1.2; issue #1057; E2E TS-0096).
- [x] ESLint del frontend es un gate **bloqueante** en `frontend-ci.yml` y en `scripts/preflight.sh` (`eslint src --max-warnings=0` / `npm run lint`), con reglas `jsx-a11y` activas vía `eslint-config-next` (issue #1048; #701 cerrado).
- [x] Workflow semanal de carga k6 operativo (script restaurado, umbrales CU74,
  artefacto `summary.json`; issue #1047; schedule + `workflow_dispatch` únicamente).
- [x] CD en `workflow_run` construye y etiqueta la imagen con el SHA probado por
  CI (`head_sha`), mueve `latest` solo tras ese push, y no publica si CI no fue
  `success` (issue #1042).
- [x] Workflows CI/CD/E2E/PR no commitean informes generados al repositorio;
  `docs/wiki/cicd-reports/` está ignorado y sin tracking; jobs de publicación
  de informes no piden `contents: write` (issue #1041).
- [x] Imagen frontend publicada a GHCR con SBOM + cosign (parity con backend) y
  proceso semver documentado/automatizado con versiones Maven/npm derivadas
  del tag (issue #1043).
- [x] Higiene de repo: ignore rules permiten `requirements.txt`, `.serena/` no
  trackeado, CODEOWNERS sin `frontend-swing`, PDF de usuario fuera de blobs
  ordinarios, ADR-022 con decisión de rewrite (issue #1050).
