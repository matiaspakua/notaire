> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #837 exists, labeled, and linked to CU27/CU32/CU42
- [x] 1.2 Use Case documentation (CU27, CU32, CU42) exists and is accurate
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/tipo-documento-vencimiento-config/spec.md`, `specs/documento-presentado-herencia-vencimiento/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR: n/a — sigue el layering existente (`api` sobre `negocio`/`repository`), no introduce un patrón arquitectónico nuevo (design.md — Decisions)
- [x] 1.6 Move Issue #837 to IN PROGRESS (`gh issue edit 837 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/837_tipo-documento-vencimiento-config`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from the Requirement coverage table in `traceability.md` (happy path, edge cases, error paths)
- [x] 3.2 n/a — `TipoDeDocumentoController` already persists `vence`/`diasVencimiento`/`quienEntrega` via `DtoTipoDeDocumento` (no new backend branching there); alta/edición coverage moved to Playwright (7.1) since the only new logic on that side is the UI form (see traceability.md)
- [x] 3.3 Extend `DocumentoPresentadoControllerTest` with `shouldInheritVencimientoFromTipoDeDocumento`, `shouldNotSetVencimientoWhenTipoDoesNotVence`, `shouldComputeFechaVencimientoFromFechaIngresoAndDiasVencimiento`, `shouldNotComputeFechaVencimientoWithoutFechaIngreso`
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=TipoDeDocumentoReferentialIntegrityTest,DocumentoPresentadoControllerTest`
- [x] 3.5 Confirm every `#### Scenario:` in the two delta specs maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [x] 4.1 `frontend/src/types/index.ts`: agregar `vence?: boolean`, `diasVencimiento?: number`, `quienEntrega?: string` a la interfaz `TipoDeDocumento`
- [x] 4.2 `frontend/src/app/dashboard/administracion/documentos/page.tsx`: agregar campos `vence` (checkbox), `diasVencimiento` (numérico, visible solo si `vence`) y `quienEntrega` (texto) al formulario de alta/edición, usando `FormField`
- [x] 4.3 `DocumentoPresentadoController.toEntity()`: buscar el `TipoDeDocumento` por `request.tipoId()` vía `tipoRepository` y, si existe, copiar `vence`, `diasVencimiento` y `quienEntrega`
- [x] 4.4 `DocumentoPresentadoController.toEntity()`: calcular `fechaVencimiento` como `fechaIngreso + diasVencimiento` días cuando `vence` es `true` y `fechaIngreso` fue informada
- [x] 4.5 Confirmar en Swagger UI que `POST/PUT /api/v1/tipo-de-documento` y `POST/PUT /api/v1/documento-presentado` documentan estos campos

## 5. Actualizar tests existentes

- [x] 5.1 Confirmar que los tests existentes de `DocumentoPresentadoControllerTest` (creación/edición con tipo `vence=false`, el caso por defecto) no cambian de comportamiento — este cambio es aditivo (design.md — Regression Strategy)
- [x] 5.2 Si algún fixture existente dependía implícitamente de `vence` siempre `false` sin importar el tipo, actualizar ese fixture para usar explícitamente un tipo con `vence=false`, sin debilitar ninguna aserción
- [x] 5.3 n/a — ningún test queda obsoleto

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/tipo-documento-vencimiento-config.spec.ts` (design.md — Playwright Strategy)
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the changed screen at 320px, 768px and 1024px — ad-hoc Playwright viewport check, no horizontal overflow, all fields reachable
- [x] 7.4 Golden path + edge/error paths from design.md — Playwright Strategy covered

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CU27/CU32 already described these fields accurately — no change needed; added a herencia note to `CU42 – Informar próximos vencimientos.md` (proposal.md — Documentation Impact)
- [x] 8.2 Update OpenAPI/Swagger annotations for the changed endpoints and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — alta/edición de vencimiento y responsable en tipos de documento; herencia automática en documentos presentados
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — n/a, no document is superseded by this change
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #837`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin feat/837_tipo-documento-vencimiento-config`
- [x] 10.2 Open the PR titled `[#837] feat: permitir cargar vencimiento y responsable en tipos de documento`, referencing Issue #837 and CU27/CU32/CU42
- [x] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [x] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main`
- [x] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [x] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Run the smoke test from design.md — Deployment Strategy (tipo de documento con vence activado, documento presentado hereda vencimiento; `GET /actuator/health` en verde)
- [x] 12.2 Verify the rollback path described in design.md — Rollback Strategy is still available
- [x] 12.3 Close GitHub Issue #837, referencing the PR
- [x] 12.4 Archive the change: `openspec archive tipo-documento-vencimiento-config`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [x] PR created, CI green, review approved (Gate 4)
- [x] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [x] `traceability.md` complete from Issue through Release
